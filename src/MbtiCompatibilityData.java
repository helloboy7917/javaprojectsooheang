import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/** The structure of the bundled MBTI compatibility data. */
public class MbtiCompatibilityData {
    public Meta meta;
    public Map<String, MbtiType> types;
    public Map<String, Combination> combinations;

    public static MbtiCompatibilityData load() throws IOException {
        InputStream stream = MbtiCompatibilityData.class.getResourceAsStream("/mbti_compatibility.json");
        if (stream == null) {
            throw new IOException("Resource not found: /mbti_compatibility.json");
        }
        try (Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            MbtiCompatibilityData data = new Gson().fromJson(reader, MbtiCompatibilityData.class);
            if (data == null || data.meta == null || data.types == null || data.combinations == null) {
                throw new IOException("Invalid MBTI compatibility data structure");
            }
            return data;
        } catch (JsonParseException e) {
            throw new IOException("Could not parse MBTI compatibility data", e);
        }
    }

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
