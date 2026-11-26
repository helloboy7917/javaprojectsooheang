public abstract class CompatibilityTest {
    public abstract CompatibilityResult calculate(Person a, Person b);

    protected int calculateCompatibilityScore(int[] digitsA, int[] digitsB) {
        if (digitsA == null || digitsB == null || digitsA.length + digitsB.length < 2) {
            throw new IllegalArgumentException("계산하려면 숫자가 두 개 이상 필요합니다.");
        }
        int[] digits = new int[digitsA.length + digitsB.length];
        int index = 0;
        for (int i = 0; i < Math.max(digitsA.length, digitsB.length); i++) {
            if (i < digitsA.length) digits[index++] = digitsA[i];
            if (i < digitsB.length) digits[index++] = digitsB[i];
        }
        for (int length = digits.length; length > 2; length--) {
            for (int i = 0; i < length - 1; i++) {
                digits[i] = (digits[i] + digits[i + 1]) % 10;
            }
        }
        return digits[0] * 10 + digits[1];
    }
}
