import java.io.UncheckedIOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

public class ConsoleUI {

    private final Scanner scanner = new Scanner(System.in);
    private final MbtiDataManager dataManager = new MbtiDataManager();

    public void start() {
        while (true) {
            System.out.println("\n===== 친구 궁합 테스트 =====");
            System.out.println("1. 이름 획수 궁합\n2. MBTI 궁합\n3. 생년월일 궁합\n0. 종료");
            String choice = readLine("선택: ");
            if (choice == null || choice.equals("0")) return;
            switch (choice) {
                case "1": showNameCompatibility(); break;
                case "2": showMbtiCompatibility(); break;
                case "3": showBirthCompatibility(); break;
                default: System.out.println("0, 1, 2, 3 중에서 선택해 주세요.");
            }
        }
    }

    private void showBirthCompatibility() {
        String firstName = readName("첫 번째 사람의 이름 입력: ");
        if (firstName == null) return;
        LocalDate firstDate = readBirthDate("첫 번째 사람의 생년월일 입력 (YYYY-MM-DD): ");
        if (firstDate == null) return;
        String secondName = readName("두 번째 사람의 이름 입력: ");
        if (secondName == null) return;
        LocalDate secondDate = readBirthDate("두 번째 사람의 생년월일 입력 (YYYY-MM-DD): ");
        if (secondDate == null) return;
        CompatibilityTest test = new BirthCompatibilityTest();
        System.out.println("\n" + firstName + "와 " + secondName + "의 생년월일 궁합");
        runTest(test, new Person(firstName, firstDate), new Person(secondName, secondDate));
    }

    private String readName(String prompt) {
        while (true) {
            String name = readLine(prompt);
            if (name == null || !name.isEmpty()) return name;
            System.out.println("이름을 입력해 주세요.");
        }
    }

    private LocalDate readBirthDate(String prompt) {
        while (true) {
            String input = readLine(prompt);
            if (input == null) return null;
            if (input.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}")) {
                try {
                    LocalDate date = LocalDate.parse(input);
                    if (date.getYear() >= 1) return date;
                } catch (DateTimeParseException ignored) {
                    // Ask for another date below.
                }
            }
            System.out.println("올바른 날짜를 YYYY-MM-DD 형식으로 입력해 주세요.");
        }
    }

    private void showNameCompatibility() {
        String first = readLine("첫 번째 이름 입력: ");
        if (first == null) return;
        String second = readLine("두 번째 이름 입력: ");
        if (second == null) return;
        if (first.isEmpty() || second.isEmpty()) {
            System.out.println("이름을 모두 입력해 주세요.");
            return;
        }
        CompatibilityTest test = new NameCompatibilityTest();
        runTest(test, new Person(first), new Person(second));
    }

    private void showMbtiCompatibility() {
        try {
            String first = readMbti("첫 번째 MBTI 입력: ");
            if (first == null) return;
            String second = readMbti("두 번째 MBTI 입력: ");
            if (second == null) return;
            CompatibilityTest test = new MbtiCompatibilityTest(dataManager);
            System.out.println("\n" + MbtiDataManager.normalizeType(first) + "와 "
                    + MbtiDataManager.normalizeType(second) + "의 MBTI 궁합");
            runTest(test, Person.withMbti(first), Person.withMbti(second));
        } catch (UncheckedIOException | IllegalStateException | IllegalArgumentException e) {
            System.out.println("MBTI 궁합을 확인할 수 없습니다: " + e.getMessage());
        }
    }

    private void runTest(CompatibilityTest test, Person first, Person second) {
        CompatibilityResult result = test.calculate(first, second);
        if (result.getScore() != null) System.out.println("궁합 점수: " + result.getScore() + "%");
        if (!result.getGoodPoints().isEmpty()) {
            System.out.println("\n좋은 점:");
            for (String point : result.getGoodPoints()) System.out.println("- " + point);
        }
        if (!result.getCautionPoints().isEmpty()) {
            System.out.println("\n주의할 점:");
            for (String point : result.getCautionPoints()) System.out.println("- " + point);
        }
    }

    private String readMbti(String prompt) {
        while (true) {
            String input = readLine(prompt);
            if (input == null) return null;
            if (dataManager.isValidType(input)) return input;
            System.out.println("올바른 MBTI 유형을 입력해 주세요. 예: INFP, ESTJ");
        }
    }

    private String readLine(String prompt) {
        System.out.print(prompt);
        return scanner.hasNextLine() ? scanner.nextLine().trim() : null;
    }
}
