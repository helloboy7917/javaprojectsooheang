package 입력;

import java.util.Scanner;

public class ConsoleUI {

    private final Scanner scanner = new Scanner(System.in);

    public void start() {

        System.out.print("첫 번째 이름 입력: ");
        Person a = new Person(scanner.nextLine());

        System.out.print("두 번째 이름 입력: ");
        Person b = new Person(scanner.nextLine());

        CompatibilityTest test = new NameCompatibilityTest();

        int score = test.calculate(a, b);

        System.out.println("궁합 점수: " + score + "%");
    }
}