import java.io.IOException;
import java.util.Locale;
import java.util.Scanner;

public class ConsoleUI {

    private final Scanner scanner = new Scanner(System.in);

    public void start() {
        while (true) {
            System.out.println("\n1. 이름 궁합  2. MBTI 궁합  0. 종료");
            String choice = readLine("선택: ");
            if (choice == null || choice.equals("0")) return;
            switch (choice) {
                case "1": showNameCompatibility(); break;
                case "2": showMbtiCompatibility(); break;
                default: System.out.println("0, 1, 2 중에서 선택해 주세요.");
            }
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
        System.out.println("궁합 점수: " + test.calculate(new Person(first), new Person(second)) + "%");
    }

    private void showMbtiCompatibility() {
        MbtiCompatibilityData data;
        try {
            data = MbtiCompatibilityData.load();
        } catch (IOException e) {
            System.out.println("MBTI 데이터를 읽을 수 없습니다: " + e.getMessage());
            return;
        }
        String first = readMbti("첫 번째 MBTI 입력: ", data);
        if (first == null) return;
        String second = readMbti("두 번째 MBTI 입력: ", data);
        if (second == null) return;

        String key = first.compareTo(second) <= 0 ? first + "-" + second : second + "-" + first;
        MbtiCompatibilityData.Combination combination = data.combinations.get(key);
        if (combination == null) {
            System.out.println("해당 MBTI 조합을 찾을 수 없습니다.");
            return;
        }
        System.out.println("\n" + first + "와 " + second + "의 MBTI 궁합");
        System.out.println("\n좋은 점:");
        for (String point : combination.goodPoints) System.out.println("- " + point);
        System.out.println("\n주의할 점:");
        for (String point : combination.cautionPoints) System.out.println("- " + point);
        if (data.meta.notice != null) System.out.println("\n" + data.meta.notice);
    }

    private String readMbti(String prompt, MbtiCompatibilityData data) {
        while (true) {
            String input = readLine(prompt);
            if (input == null) return null;
            String type = input.toUpperCase(Locale.ROOT);
            if (data.types.containsKey(type)) return type;
            System.out.println("올바른 MBTI 유형을 입력해 주세요. 예: INFP, ESTJ");
        }
    }

    private String readLine(String prompt) {
        System.out.print(prompt);
        return scanner.hasNextLine() ? scanner.nextLine().trim() : null;
    }
}
