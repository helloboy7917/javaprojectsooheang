package 입력;

public class HangulStrokeCalculator {

    protected static final int[] namehuk1 = {2, 4, 2, 3, 6, 5, 4, 4, 8, 2, 4, 1, 3, 6, 4, 3, 4, 4, 3};
    protected static final int[] namehuk2 = {2, 3, 3, 4, 2, 3, 3, 4, 2, 4, 5, 3, 3, 2, 4, 5, 3, 3, 1, 2, 1};
    protected static final int[] namehuk3 = {0, 2, 4, 4, 2, 5, 5, 3, 5, 7, 9, 9, 7, 9, 9, 8, 4, 4, 6, 2, 4, 1, 3, 4, 3, 4, 4, 3};

    public static int get_Namehuk(char n){
        int code = n - 44032;//'가' 를 기준으로 뺌
        if (code<0||code>11171){
            return 0;//한글이 아닌 경우 0을 반환
        }

        int cho = code/588;//초성 구하기
        int jung = (code%588)/28;//중성 구하기
        int jong = code%28;//종성 구하기

        return namehuk1[cho] + namehuk2[jung] + namehuk3[jong];
    }


}
