
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class BirthCompatibilityTest extends CompatibilityTest {
    @Override
    public CompatibilityResult calculate(Person a, Person b) {
        if (a == null || b == null) throw new IllegalArgumentException("두 사람이 필요합니다.");
        int[] first = toDigits(a.getBirthDate());
        int[] second = toDigits(b.getBirthDate());
        return CompatibilityResult.forScore(calculateCompatibilityScore(first, second));
    }

    private int[] toDigits(LocalDate date) {
        if (date == null || date.getYear() < 1 || date.getYear() > 9999) {
            throw new IllegalArgumentException("생년월일은 0001년부터 9999년 사이여야 합니다.");
        }
        String formatted = date.format(DateTimeFormatter.BASIC_ISO_DATE);
        int[] digits = new int[8];
        if (formatted.length() != digits.length) {
            throw new IllegalArgumentException("생년월일은 8자리여야 합니다.");
        }
        for (int i = 0; i < digits.length; i++) digits[i] = formatted.charAt(i) - '0';
        return digits;
    }
}
