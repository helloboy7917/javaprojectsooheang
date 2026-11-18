package 입력;

import java.util.Scanner;

import static 입력.Matcher.Matcher;

public class option {

    public static void option1(){
        Input n = new Input("",100);
        Input m = new Input("", 100);
        Scanner in = new Scanner(System.in);

        System.out.print("1번째 이름 입력 : ");
        String i = in.nextLine();
        n.setName(i);

        System.out.print("나이 입력 : ");
        int h = in.nextInt();
        n.setAge(h);

        in.nextLine();// 버퍼에 남은 개행 문자(\n) 제거!
        System.out.println();

        System.out.print("2번째 이름 입력 :");
        String j = in.nextLine();
        m.setName(j);

        System.out.print("나이 입력 :");
        int k = in.nextInt();
        m.setAge(k);

        char[] ch1 = n.getName().toCharArray();
        char[] ch2 = m.getName().toCharArray();

        System.out.println("궁합 점수: " + Matcher(ch1,ch2) + "%");
    }



}
