import java.util.List;
import java.util.Map;

/** The structure of the bundled MBTI compatibility data. */
public class MbtiCompatibilityData {
    public Meta meta;
    public Map<String, MbtiType> types;
    public Map<String, Combination> combinations;

    public static class Meta {
        public String title;
        public String description;
        public int typeCount;
        public int combinationCount;
        public String keyFormat;
        public String notice;
    }

    public static class MbtiType {
        public String description;
        public String strength;
        public String caution;
    }

    public static class Combination {
        public String typeA;
        public String typeB;
        public List<String> goodPoints;
        public List<String> cautionPoints;
    }
}
