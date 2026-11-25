import static org.junit.Assert.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import org.junit.Test;

public class CompatibilityRegressionTest {
    @Test
    public void nameResultUsesExistingAlgorithmThroughBaseClass() {
        CompatibilityTest test = new NameCompatibilityTest();
        CompatibilityResult result = test.calculate(new Person("Alice"), new Person("Bob"));
        assertEquals(Integer.valueOf(0), result.getScore());
        assertTrue(result.getGoodPoints().isEmpty());
        assertTrue(result.getCautionPoints().isEmpty());
        assertEquals(Integer.valueOf(55), test.calculate(new Person("가나다"), new Person("라마바")).getScore());
    }

    @Test
    public void dataLoadsLazilyAndIsReusedForAllPairs() throws Exception {
        MbtiDataManager manager = new MbtiDataManager();
        Field field = MbtiDataManager.class.getDeclaredField("data");
        field.setAccessible(true);
        assertNull(field.get(manager));
        assertTrue(manager.isValidType(" enfp "));
        MbtiCompatibilityData data = (MbtiCompatibilityData) field.get(manager);
        assertEquals(16, data.types.size());
        assertEquals(136, data.combinations.size());
        CompatibilityTest test = new MbtiCompatibilityTest(manager);
        for (String first : data.types.keySet()) {
            for (String second : data.types.keySet()) {
                CompatibilityResult result = test.calculate(Person.withMbti(first), Person.withMbti(second));
                assertNull(result.getScore());
                assertFalse(result.getGoodPoints().isEmpty());
                assertFalse(result.getCautionPoints().isEmpty());
            }
        }
        assertSame(data, field.get(manager));
        assertFalse(manager.isValidType("INVALID"));
    }

    @Test
    public void reversedAndLowercaseMbtiHaveSameResult() {
        CompatibilityTest test = new MbtiCompatibilityTest(new MbtiDataManager());
        CompatibilityResult forward = test.calculate(Person.withMbti("enfp"), Person.withMbti("intj"));
        CompatibilityResult reverse = test.calculate(Person.withMbti("INTJ"), Person.withMbti("ENFP"));
        assertEquals(forward.getGoodPoints(), reverse.getGoodPoints());
        assertEquals(forward.getCautionPoints(), reverse.getCautionPoints());
    }

    @Test
    public void invalidInputIsRetriedInConsole() throws Exception {
        java.io.InputStream originalIn = System.in;
        PrintStream originalOut = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            System.setIn(new ByteArrayInputStream("2\nINVALID\nenfp\nintj\n0\n".getBytes(StandardCharsets.UTF_8)));
            System.setOut(new PrintStream(output, true, StandardCharsets.UTF_8.name()));
            new ConsoleUI().start();
        } finally {
            System.setIn(originalIn);
            System.setOut(originalOut);
        }
        String text = output.toString(StandardCharsets.UTF_8.name());
        assertTrue(text.contains("올바른 MBTI 유형을 입력해 주세요"));
        assertTrue(text.contains("ENFP와 INTJ의 MBTI 궁합"));
        assertTrue(text.contains("좋은 점:"));
        assertTrue(text.contains("주의할 점:"));
    }
}
