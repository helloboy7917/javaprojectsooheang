import java.util.List;
import java.util.Objects;

/** A score or descriptive compatibility result. A missing score is represented by null. */
public final class CompatibilityResult {
    private final Integer score;
    private final List<String> goodPoints;
    private final List<String> cautionPoints;

    public CompatibilityResult(Integer score, List<String> goodPoints, List<String> cautionPoints) {
        this.score = score;
        this.goodPoints = List.copyOf(Objects.requireNonNull(goodPoints));
        this.cautionPoints = List.copyOf(Objects.requireNonNull(cautionPoints));
    }

    public static CompatibilityResult forScore(int score) {
        return new CompatibilityResult(score, List.of(), List.of());
    }

    public static CompatibilityResult forDescription(List<String> goodPoints, List<String> cautionPoints) {
        return new CompatibilityResult(null, goodPoints, cautionPoints);
    }

    public Integer getScore() { return score; }
    public List<String> getGoodPoints() { return goodPoints; }
    public List<String> getCautionPoints() { return cautionPoints; }
}
