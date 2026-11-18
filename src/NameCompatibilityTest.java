public class NameCompatibilityTest extends CompatibilityTest {

    @Override
    public int calculate(Person a, Person b) {

        char[] ch1 = a.getName().toCharArray();
        char[] ch2 = b.getName().toCharArray();



        int[] arr = new int[ch1.length + ch2.length];
        int idx = 0;
        int maxLen = Math.max(ch1.length, ch2.length); //2


        for (int l = 0; l < maxLen; l++) {
            if (l < ch1.length) {
                arr[idx++] = HangulStrokeCalculator.get_Namehuk(ch1[l]);
            }
            if (l < ch2.length) {
                arr[idx++] = HangulStrokeCalculator.get_Namehuk(ch2[l]);
            }
        }

        for (int len = arr.length; len > 2; len--) {
            for (int o = 0; o < len - 1; o++) {
                arr[o] = (arr[o] + arr[o + 1]) % 10;
            }
        }

        int score = arr[0] * 10 + arr[1];
        return score;


    }






}
