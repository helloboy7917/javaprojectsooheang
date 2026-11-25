import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/** Lazily loads the bundled data once for this manager and reuses it. */
public class MbtiDataManager {
    private MbtiCompatibilityData data;

    private synchronized MbtiCompatibilityData data() {
        if (data == null) {
            try (InputStream stream = MbtiDataManager.class.getResourceAsStream("/mbti_compatibility.json")) {
                if (stream == null) throw new IOException("MBTI JSON 리소스를 찾을 수 없습니다.");
                try (Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                    MbtiCompatibilityData loaded = new Gson().fromJson(reader, MbtiCompatibilityData.class);
                    if (loaded == null || loaded.meta == null || loaded.types == null || loaded.combinations == null) {
                        throw new IOException("MBTI JSON의 구조가 올바르지 않습니다.");
                    }
                    data = loaded;
                }
            } catch (JsonParseException e) {
                throw new IllegalStateException("MBTI JSON을 해석할 수 없습니다.", e);
            } catch (IOException e) {
                throw new UncheckedIOException("MBTI 데이터를 읽을 수 없습니다.", e);
            }
        }
        return data;
    }

    public static String normalizeType(String type) {
        return type == null ? "" : type.trim().toUpperCase(Locale.ROOT);
    }

    public boolean isValidType(String type) {
        return data().types.containsKey(normalizeType(type));
    }

    public MbtiCompatibilityData.Combination getCombination(String key) {
        MbtiCompatibilityData.Combination combination = data().combinations.get(key);
        if (combination == null) throw new IllegalArgumentException("해당 MBTI 조합을 찾을 수 없습니다: " + key);
        return combination;
    }

    public String getNotice() {
        return data().meta.notice;
    }
}
