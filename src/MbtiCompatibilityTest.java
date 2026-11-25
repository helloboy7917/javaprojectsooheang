import java.util.Objects;

public class MbtiCompatibilityTest extends CompatibilityTest {
    private final MbtiDataManager dataManager;

    public MbtiCompatibilityTest(MbtiDataManager dataManager) {
        this.dataManager = Objects.requireNonNull(dataManager);
    }

    @Override
    public CompatibilityResult calculate(Person a, Person b) {
        String first = MbtiDataManager.normalizeType(a.getMbti());
        String second = MbtiDataManager.normalizeType(b.getMbti());
        if (!dataManager.isValidType(first) || !dataManager.isValidType(second)) {
            throw new IllegalArgumentException("올바른 MBTI 유형을 입력해 주세요.");
        }
        String key = first.compareTo(second) <= 0 ? first + "-" + second : second + "-" + first;
        MbtiCompatibilityData.Combination combination = dataManager.getCombination(key);
        return CompatibilityResult.forDescription(combination.goodPoints, combination.cautionPoints);
    }
}
