import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Scanner;

public class ConsoleUI {
    private final Scanner scanner = new Scanner(System.in);
    private final MbtiDataManager mbtiData = new MbtiDataManager();
    private final Path friendsPath;
    private FriendDataManager friends;

    public ConsoleUI() { this(Paths.get("data/friends.json")); }
    public ConsoleUI(Path friendsPath) { this.friendsPath = friendsPath; }

    public void start() {
        try {
            // 시작할 때 친구 파일을 읽고, 없으면 빈 파일을 만든다.
            friends = new FriendDataManager(friendsPath);
        } catch (IOException e) {
            System.out.println("친구 데이터를 열 수 없습니다: " + e.getMessage());
            return;
        }
        while (true) {
            System.out.println("\n========================================");
            System.out.println("          FRIEND COMPATIBILITY");
            System.out.println("             친구 궁합 테스트");
            System.out.println("========================================");
            System.out.println("  1. 친구 등록\n  2. 등록된 친구 목록\n  3. 친구 정보 관리");
            System.out.println("  4. 궁합 테스트\n  5. 궁합 순위\n\n  0. 종료");
            System.out.println("----------------------------------------");
            String choice = read("  메뉴 선택 > ");
            if (choice == null || choice.equals("0")) return;
            try {
                switch (choice) {
                    case "1": register(); break;
                    case "2": listFriends(); break;
                    case "3": manage(); break;
                    case "4": compatibility(); break;
                    case "5": ranking(); break;
                    default: System.out.println("메뉴 번호를 다시 입력해 주세요.");
                }
            } catch (IOException | IllegalArgumentException | IllegalStateException | UncheckedIOException e) {
                System.out.println("작업을 완료할 수 없습니다: " + e.getMessage());
            }
        }
    }

    private void register() throws IOException {
        System.out.println("\n---------- 친구 등록 ----------");
        String name = required("이름: ");
        if (name == null) return;
        String date = dateInput("생년월일 (YYYY-MM-DD, 생략 가능): ", true);
        if (date == null) return;
        String mbti = mbtiInput("MBTI (생략 가능): ", true);
        if (mbti == null) return;
        friends.add(name, date.isEmpty() ? null : LocalDate.parse(date), mbti);
        System.out.println("\n  [완료] 친구 등록이 완료되었습니다.");
    }

    private void listFriends() {
        ArrayList<Friend> list = friends.getFriends();
        System.out.println("\n---------- 친구 목록 ----------");
        if (list.isEmpty()) System.out.println("등록된 친구가 없습니다.");
        for (Friend friend : list) printFriend(friend);
    }

    private void printFriend(Friend f) {
        System.out.println("ID " + f.getId() + " | " + f.getName() + " | 생년월일: "
                + (f.getBirthDate() == null ? "미등록" : f.getBirthDate()) + " | MBTI: "
                + (f.getMbti() == null ? "미등록" : f.getMbti()));
    }

    private void manage() throws IOException {
        String action = read("\n---------- 친구 정보 관리 ----------\n  1. 상세 조회\n  2. 수정\n  3. 삭제\n  0. 돌아가기\n\n  메뉴 선택 > ");
        if (action == null || action.equals("0")) return;
        if (!action.equals("1") && !action.equals("2") && !action.equals("3")) {
            System.out.println("메뉴 번호를 다시 입력해 주세요."); return;
        }
        Friend friend = selectFriend("친구 이름: ");
        if (friend == null) return;
        if (action.equals("1")) { printFriend(friend); return; }
        if (action.equals("3")) {
            String confirm = read("정말 삭제하시겠습니까? (y/n): ");
            if (confirm != null && confirm.equalsIgnoreCase("y")) {
                friends.delete(friend.getId()); System.out.println("친구를 삭제했습니다.");
            }
            return;
        }
        printFriend(friend);
        String name = read("새 이름 (Enter: 유지): ");
        if (name == null) return;
        if (name.isEmpty()) name = friend.getName();
        String date = dateInput("새 생년월일 (Enter: 유지, -: 삭제): ", true);
        if (date == null) return;
        LocalDate newDate = date.isEmpty() ? friend.getBirthDate() : date.equals("-") ? null : LocalDate.parse(date);
        String mbti = mbtiInput("새 MBTI (Enter: 유지, -: 삭제): ", true);
        if (mbti == null) return;
        String newMbti = mbti.isEmpty() ? friend.getMbti() : mbti.equals("-") ? null : mbti;
        friends.update(friend.getId(), name, newDate, newMbti);
        System.out.println("친구 정보를 수정했습니다.");
    }

    // 이름 검색 결과가 여러 명이면 번호를 선택해 고유 ID로 구분한다.
    private Friend selectFriend(String prompt) {
        String name = required(prompt);
        if (name == null) return null;
        ArrayList<Friend> matches = friends.findByName(name);
        if (matches.isEmpty()) { System.out.println("등록되지 않은 친구입니다."); return null; }
        if (matches.size() == 1) return matches.get(0);
        System.out.println("같은 이름의 친구가 있습니다.");
        for (int i = 0; i < matches.size(); i++) {
            System.out.print((i + 1) + ". "); printFriend(matches.get(i));
        }
        while (true) {
            String number = read("번호 선택 (0: 취소): ");
            if (number == null || number.equals("0")) return null;
            try {
                int index = Integer.parseInt(number) - 1;
                if (index >= 0 && index < matches.size()) return matches.get(index);
            } catch (NumberFormatException ignored) { }
            System.out.println("올바른 번호를 입력해 주세요.");
        }
    }

    private void compatibility() {
        String type = read("\n---------- 궁합 테스트 ----------\n  1. 이름 획수 궁합\n  2. MBTI 궁합\n  3. 생년월일 궁합\n  0. 돌아가기\n\n  메뉴 선택 > ");
        if (type == null || type.equals("0")) return;
        if (!type.equals("1") && !type.equals("2") && !type.equals("3")) { System.out.println("메뉴 번호를 확인해 주세요."); return; }
        String method = read("\n---------- 검사 방식 선택 ----------\n  1. 직접 정보 입력\n  2. 등록된 친구 정보 사용\n  0. 돌아가기\n\n  메뉴 선택 > ");
        if (method == null || method.equals("0")) return;
        Person a, b;
        if (method.equals("2")) {
            Friend first = selectFriend("첫 번째 친구 이름: ");
            if (first == null) return;
            Friend second = selectFriend("두 번째 친구 이름: ");
            if (second == null) return;
            if (first.getId() == second.getId()) { System.out.println("같은 친구는 두 번 선택할 수 없습니다."); return; }
            if (type.equals("2") && (first.getMbti() == null || second.getMbti() == null)) {
                System.out.println("MBTI가 등록되지 않은 친구가 있습니다."); return;
            }
            if (type.equals("3") && (first.getBirthDate() == null || second.getBirthDate() == null)) {
                System.out.println("생년월일이 등록되지 않은 친구가 있습니다."); return;
            }
            // 저장한 정보를 Person으로 바꾸어 기존 테스트에 전달한다.
            a = first.toPerson(); b = second.toPerson();
            System.out.println("등록된 친구: " + first.getName() + " / " + second.getName());
        } else if (method.equals("1")) {
            a = directPerson(type, "첫 번째");
            if (a == null) return;
            b = directPerson(type, "두 번째");
            if (b == null) return;
        } else { System.out.println("메뉴 번호를 확인해 주세요."); return; }
        CompatibilityTest test = type.equals("1") ? new NameCompatibilityTest()
                : type.equals("2") ? new MbtiCompatibilityTest(mbtiData) : new BirthCompatibilityTest();
        CompatibilityResult result = test.calculate(a, b);
        if (type.equals("2")) System.out.println(MbtiDataManager.normalizeType(a.getMbti()) + "와 " + MbtiDataManager.normalizeType(b.getMbti()) + "의 MBTI 궁합");
        else if (type.equals("3")) System.out.println(a.getName() + "와 " + b.getName() + "의 생년월일 궁합");
        printResult(result);
    }

    private Person directPerson(String type, String label) {
        if (type.equals("2")) {
            String mbti = mbtiInput(label + " MBTI: ", false);
            return mbti == null ? null : Person.withMbti(mbti);
        }
        String name = required(label + " 이름: ");
        if (name == null) return null;
        Person person = new Person(name);
        if (type.equals("3")) {
            String date = dateInput(label + " 생년월일 (YYYY-MM-DD): ", false);
            if (date == null) return null;
            person.setBirthDate(LocalDate.parse(date));
        }
        return person;
    }

    private void printResult(CompatibilityResult result) {
        if (result.getScore() != null) System.out.println("\n  ★ 궁합 점수: " + result.getScore() + "점 ★");
        if (!result.getGoodPoints().isEmpty()) {
            System.out.println("\n  [ 좋은 점: ]");
            for (String point : result.getGoodPoints()) System.out.println("- " + point);
        }
        if (!result.getCautionPoints().isEmpty()) {
            System.out.println("\n  [ 주의할 점: ]");
            for (String point : result.getCautionPoints()) System.out.println("- " + point);
        }
    }

    private void ranking() {
        String choice = read("\n---------- 궁합 순위 ----------\n  1. 이름 궁합 순위\n  2. 생년월일 궁합 순위\n  0. 돌아가기\n\n  메뉴 선택 > ");
        if (choice == null || choice.equals("0")) return;
        if (!choice.equals("1") && !choice.equals("2")) { System.out.println("메뉴 번호를 확인해 주세요."); return; }
        ArrayList<CompatibilityRankingService.Ranking> rows = new CompatibilityRankingService().calculate(friends.getFriends(), choice.equals("2"));
        System.out.println("\n========================================\n             궁합 TOP 10\n========================================");
        if (rows.isEmpty()) System.out.println("순위를 계산할 친구가 충분하지 않습니다.");
        for (int i = 0; i < rows.size() && i < 10; i++) {
            CompatibilityRankingService.Ranking row = rows.get(i);
            System.out.println(row.rank + "위  " + row.first.getName() + "(ID " + row.first.getId() + ") ↔ "
                    + row.second.getName() + "(ID " + row.second.getId() + ")  " + row.score + "점");
        }
    }

    private String required(String prompt) {
        while (true) {
            String value = read(prompt);
            if (value == null || !value.isEmpty()) return value;
            System.out.println("값을 입력해 주세요.");
        }
    }
    private String dateInput(String prompt, boolean optional) {
        while (true) {
            String value = read(prompt);
            if (value == null || (optional && (value.isEmpty() || (prompt.contains("삭제") && value.equals("-"))))) return value;
            try {
                if (value.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}") && LocalDate.parse(value).getYear() >= 1) return value;
            } catch (DateTimeParseException ignored) { }
            System.out.println("올바른 날짜를 YYYY-MM-DD 형식으로 입력해 주세요.");
        }
    }
    private String mbtiInput(String prompt, boolean optional) {
        while (true) {
            String value = read(prompt);
            if (value == null || (optional && (value.isEmpty() || (prompt.contains("삭제") && value.equals("-"))))) return value;
            if (mbtiData.isValidType(value)) return MbtiDataManager.normalizeType(value);
            System.out.println("올바른 MBTI 유형을 입력해 주세요. 예: INFP, ESTJ");
        }
    }
    private String read(String prompt) {
        System.out.print(prompt);
        return scanner.hasNextLine() ? scanner.nextLine().trim() : null;
    }
}
