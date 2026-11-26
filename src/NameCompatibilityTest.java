public class NameCompatibilityTest extends CompatibilityTest {

    @Override
    public CompatibilityResult calculate(Person a, Person b) {

        char[] ch1 = a.getName().toCharArray();
        char[] ch2 = b.getName().toCharArray();



        int[] digitsA = new int[ch1.length];
        int[] digitsB = new int[ch2.length];
        for (int i = 0; i < ch1.length; i++) digitsA[i] = HangulStrokeCalculator.get_Namehuk(ch1[i]);
        for (int i = 0; i < ch2.length; i++) digitsB[i] = HangulStrokeCalculator.get_Namehuk(ch2[i]);
        return CompatibilityResult.forScore(calculateCompatibilityScore(digitsA, digitsB));


    }






}
