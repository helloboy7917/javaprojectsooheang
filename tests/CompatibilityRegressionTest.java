import static org.junit.Assert.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

public class CompatibilityRegressionTest {
    @Rule public TemporaryFolder temporary = new TemporaryFolder();
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
            System.setIn(new ByteArrayInputStream("4\n2\n1\nINVALID\nenfp\nintj\n0\n".getBytes(StandardCharsets.UTF_8)));
            System.setOut(new PrintStream(output, true, StandardCharsets.UTF_8.name()));
            new ConsoleUI(temporary.getRoot().toPath().resolve("friends.json")).start();
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

    @Test
    public void birthUsesSharedAlgorithmAndPreservesInputOrder() throws Exception {
        CompatibilityTest test = new BirthCompatibilityTest();
        Person first = new Person("A", LocalDate.of(2007, 3, 15));
        Person second = new Person("B", LocalDate.of(2007, 4, 12));
        assertEquals(Integer.valueOf(44), test.calculate(first, second).getScore());
        assertEquals(Integer.valueOf(44), test.calculate(second, first).getScore());
        Method method = BirthCompatibilityTest.class.getDeclaredMethod("toDigits", LocalDate.class);
        method.setAccessible(true);
        assertArrayEquals(new int[] {2, 0, 0, 7, 0, 3, 1, 5}, (int[]) method.invoke(test, first.getBirthDate()));
        assertArrayEquals(new int[] {2, 0, 0, 7, 0, 4, 1, 2}, (int[]) method.invoke(test, second.getBirthDate()));
        for (int year : new int[] {1, 2000, 2024, 9999}) {
            int score = test.calculate(new Person("A", LocalDate.of(year, 1, 1)), second).getScore();
            assertTrue(score >= 0 && score <= 99);
        }
        assertEquals(CompatibilityTest.class, BirthCompatibilityTest.class.getSuperclass());
        assertEquals(CompatibilityTest.class, NameCompatibilityTest.class.getSuperclass());
    }

    @Test
    public void missingBirthDateIsRejected() {
        CompatibilityTest test = new BirthCompatibilityTest();
        assertThrows(IllegalArgumentException.class,
                () -> test.calculate(new Person("A"), new Person("B", LocalDate.of(2000, 2, 29))));
        assertThrows(IllegalArgumentException.class,
                () -> test.calculate(new Person("A", LocalDate.of(0, 1, 1)), new Person("B", LocalDate.now())));
    }

    @Test
    public void birthConsoleRetriesInvalidDatesAndAcceptsLeapDay() throws Exception {
        java.io.InputStream originalIn = System.in;
        PrintStream originalOut = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            String input = "4\n3\n1\nA\n\n2007/03/15\n2007-02-29\n2000-02-29\nB\n2007-04-12\n0\n";
            System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
            System.setOut(new PrintStream(output, true, StandardCharsets.UTF_8.name()));
            new ConsoleUI(temporary.getRoot().toPath().resolve("friends.json")).start();
        } finally {
            System.setIn(originalIn);
            System.setOut(originalOut);
        }
        String text = output.toString(StandardCharsets.UTF_8.name());
        assertEquals(3, text.split("올바른 날짜를", -1).length - 1);
        assertTrue(text.contains("A와 B의 생년월일 궁합"));
        assertTrue(text.contains("궁합 점수:"));
    }
}
