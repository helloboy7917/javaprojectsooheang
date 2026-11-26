import static org.junit.Assert.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class FriendFeaturesTest {
    @Rule public TemporaryFolder temporary = new TemporaryFolder();

    @Test public void saveReloadUpdateDeleteAndDuplicateNames() throws Exception {
        Path path = temporary.getRoot().toPath().resolve("friends.json");
        FriendDataManager manager = new FriendDataManager(path);
        manager.add("가상친구", null, null);
        manager.add("가상친구", LocalDate.of(2000, 2, 29), "infp");
        assertTrue(Files.exists(path));
        manager = new FriendDataManager(path);
        assertEquals(2, manager.findByName("가상친구").size());
        assertNull(manager.findById(1).getBirthDate());
        assertEquals("INFP", manager.findById(2).getMbti());
        manager.update(1, "다른친구", LocalDate.of(2001, 1, 1), null);
        manager.delete(2);
        manager.add("세번째", null, null);
        manager = new FriendDataManager(path);
        assertEquals("다른친구", manager.findById(1).getName());
        assertNull(manager.findById(2));
        assertNotNull(manager.findById(3));
    }

    @Test public void corruptFileIsNotOverwritten() throws Exception {
        Path path = temporary.getRoot().toPath().resolve("friends.json");
        Files.writeString(path, "{broken", StandardCharsets.UTF_8);
        assertThrows(java.io.IOException.class, () -> new FriendDataManager(path));
        assertEquals("{broken", Files.readString(path));
    }

    @Test public void rankingCreatesUniquePairsAndUpdates() throws Exception {
        Path path = temporary.getRoot().toPath().resolve("friends.json");
        FriendDataManager manager = new FriendDataManager(path);
        manager.add("가", LocalDate.of(2000, 1, 1), null);
        manager.add("나", LocalDate.of(2001, 1, 1), null);
        manager.add("다", null, null);
        manager.add("라", null, null);
        CompatibilityRankingService service = new CompatibilityRankingService();
        ArrayList<CompatibilityRankingService.Ranking> rows = service.calculate(manager.getFriends(), false);
        assertEquals(6, rows.size());
        java.util.HashSet<String> pairs = new java.util.HashSet<>();
        for (int i = 0; i < rows.size(); i++) {
            CompatibilityRankingService.Ranking row = rows.get(i);
            assertTrue(pairs.add(row.first.getId() + ":" + row.second.getId()));
            if (i > 0) {
                assertTrue(rows.get(i - 1).score >= row.score);
                assertEquals(rows.get(i - 1).score == row.score ? rows.get(i - 1).rank : i + 1, row.rank);
            }
        }
        assertEquals(1, service.calculate(manager.getFriends(), true).size());
        manager.update(3, "다", LocalDate.of(2002, 1, 1), null);
        assertEquals(3, service.calculate(manager.getFriends(), true).size());
        manager.delete(4);
        assertEquals(3, service.calculate(manager.getFriends(), false).size());
    }

    @Test public void registeredFriendsAndRankingConsoleFlow() throws Exception {
        Path path = temporary.getRoot().toPath().resolve("friends.json");
        String input = "1\n가상일\n2007-03-15\nINFP\n1\n가상이\n2007-04-12\nENTJ\n"
                + "4\n2\n2\n가상일\n가상이\n5\n2\n0\n";
        String output = runConsole(path, input);
        assertTrue(output.contains("친구 등록이 완료되었습니다"));
        assertTrue(output.contains("등록된 친구: 가상일 / 가상이"));
        assertTrue(output.contains("좋은 점:"));
        assertTrue(output.contains("44점"));
        assertEquals(2, new FriendDataManager(path).getFriends().size());
    }

    @Test public void missingFieldsAndSameFriendAreReported() throws Exception {
        Path path = temporary.getRoot().toPath().resolve("friends.json");
        FriendDataManager manager = new FriendDataManager(path);
        manager.add("가상일", null, null); manager.add("가상이", null, null);
        String output = runConsole(path, "4\n2\n2\n가상일\n가상이\n4\n3\n2\n가상일\n가상이\n4\n1\n2\n가상일\n가상일\n0\n");
        assertTrue(output.contains("MBTI가 등록되지 않은"));
        assertTrue(output.contains("생년월일이 등록되지 않은"));
        assertTrue(output.contains("같은 친구는 두 번"));
    }

    @Test public void duplicateNameSelectionAndMissingName() throws Exception {
        Path path = temporary.getRoot().toPath().resolve("friends.json");
        FriendDataManager manager = new FriendDataManager(path);
        manager.add("동명", null, "INFP"); manager.add("동명", null, "ENTJ");
        String output = runConsole(path, "4\n2\n2\n동명\n1\n동명\n2\n4\n1\n2\n없는친구\n0\n");
        assertTrue(output.contains("같은 이름의 친구가 있습니다"));
        assertTrue(output.contains("INFP와 ENTJ의 MBTI 궁합"));
        assertTrue(output.contains("등록되지 않은 친구"));
    }

    @Test public void scoresAreSymmetricAndTiesShareRanks() {
        CompatibilityTest names = new NameCompatibilityTest();
        Person a = new Person("가나다"), b = new Person("라마바");
        assertEquals(names.calculate(a, b).getScore(), names.calculate(b, a).getScore());
        CompatibilityTest birth = new BirthCompatibilityTest();
        a.setBirthDate(LocalDate.of(2007, 3, 15)); b.setBirthDate(LocalDate.of(2007, 4, 12));
        assertEquals(birth.calculate(a, b).getScore(), birth.calculate(b, a).getScore());
        ArrayList<Friend> friends = new ArrayList<>();
        friends.add(new Friend(1, "A", null, null)); friends.add(new Friend(2, "B", null, null));
        friends.add(new Friend(3, "C", null, null)); friends.add(new Friend(4, "가", null, null));
        ArrayList<CompatibilityRankingService.Ranking> rows = new CompatibilityRankingService().calculate(friends, false);
        boolean tied = false;
        for (int i = 1; i < rows.size(); i++) {
            if (rows.get(i).score == rows.get(i - 1).score) {
                assertEquals(rows.get(i - 1).rank, rows.get(i).rank); tied = true;
            } else assertEquals(i + 1, rows.get(i).rank);
        }
        assertTrue(tied);
    }

    @Test public void consoleShowsAtMostTenRankingPairs() throws Exception {
        Path path = temporary.getRoot().toPath().resolve("friends.json");
        FriendDataManager manager = new FriendDataManager(path);
        for (int i = 0; i < 6; i++) manager.add("친구" + i, null, null);
        assertEquals(15, new CompatibilityRankingService().calculate(manager.getFriends(), false).size());
        String output = runConsole(path, "5\n1\n0\n");
        assertTrue(output.contains("궁합 TOP 10"));
        int rankingLines = 0;
        for (String line : output.split("\\R")) {
            if (line.matches("[0-9]+위  .*")) rankingLines++;
        }
        assertEquals(10, rankingLines);
    }

    @Test public void consoleKeepsOrClearsOptionalFieldsWhenEditing() throws Exception {
        Path path = temporary.getRoot().toPath().resolve("friends.json");
        FriendDataManager manager = new FriendDataManager(path);
        manager.add("친구", LocalDate.of(2000, 2, 29), "INFP");
        runConsole(path, "3\n2\n친구\n\n\n-\n0\n");
        Friend saved = new FriendDataManager(path).findById(1);
        assertEquals("친구", saved.getName());
        assertEquals(LocalDate.of(2000, 2, 29), saved.getBirthDate());
        assertNull(saved.getMbti());
    }

    private String runConsole(Path path, String input) throws Exception {
        java.io.InputStream oldIn = System.in; PrintStream oldOut = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
            System.setOut(new PrintStream(output, true, StandardCharsets.UTF_8.name()));
            new ConsoleUI(path).start();
        } finally { System.setIn(oldIn); System.setOut(oldOut); }
        return output.toString(StandardCharsets.UTF_8.name());
    }
}
