import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.ArrayList;

public class FriendDataManager {
    private final Path path;
    private final Gson gson;
    private final MbtiDataManager mbtiData = new MbtiDataManager();
    private Store store;

    private static class Store {
        int nextId = 1;
        ArrayList<Friend> friends = new ArrayList<>();
    }

    public FriendDataManager(Path path) throws IOException {
        this.path = path;
        // LocalDate를 JSON 문자열로 읽고 쓰도록 Gson에 알려준다.
        gson = new GsonBuilder().setPrettyPrinting().registerTypeAdapter(LocalDate.class, new TypeAdapter<LocalDate>() {
            public void write(JsonWriter out, LocalDate date) throws IOException { out.value(date.toString()); }
            public LocalDate read(JsonReader in) throws IOException { return LocalDate.parse(in.nextString()); }
        }.nullSafe()).create();
        load();
    }

    private void load() throws IOException {
        if (Files.notExists(path)) {
            store = new Store();
            save(store);
            return;
        }
        // 오류가 발생하면 빈 목록으로 덮어쓰지 않고 호출자에게 알린다.
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            JsonElement json = JsonParser.parseReader(reader);
            if (!json.isJsonObject() || !json.getAsJsonObject().has("nextId") || !json.getAsJsonObject().has("friends"))
                throw new IOException("친구 파일 형식이 올바르지 않습니다.");
            Store loaded = gson.fromJson(json, Store.class);
            if (loaded == null || loaded.friends == null || loaded.nextId < 1) throw new IOException("친구 파일 형식이 올바르지 않습니다.");
            for (int i = 0; i < loaded.friends.size(); i++) {
                Friend f = loaded.friends.get(i);
                if (f == null || f.getId() < 1 || f.getId() >= loaded.nextId || f.getName() == null || f.getName().trim().isEmpty())
                    throw new IOException("친구 파일에 잘못된 정보가 있습니다.");
                for (int j = 0; j < i; j++) if (loaded.friends.get(j).getId() == f.getId()) throw new IOException("친구 ID가 중복됩니다.");
            }
            store = loaded;
        } catch (JsonParseException | IllegalArgumentException e) {
            throw new IOException("친구 파일을 읽을 수 없습니다. 파일 내용을 확인해 주세요.", e);
        }
    }

    private void save(Store updated) throws IOException {
        Path absolute = path.toAbsolutePath();
        Files.createDirectories(absolute.getParent());
        Path temp = absolute.resolveSibling(absolute.getFileName() + ".tmp");
        // 먼저 임시 파일에 저장한 뒤 기존 파일을 교체한다.
        try (Writer writer = Files.newBufferedWriter(temp, StandardCharsets.UTF_8)) { gson.toJson(updated, writer); }
        Files.move(temp, absolute, StandardCopyOption.REPLACE_EXISTING);
    }

    public ArrayList<Friend> getFriends() { return new ArrayList<>(store.friends); }

    // 같은 이름이 여럿일 수 있으므로 일치하는 친구를 모두 반환한다.
    public ArrayList<Friend> findByName(String name) {
        ArrayList<Friend> matches = new ArrayList<>();
        for (Friend friend : store.friends) if (friend.getName().equals(name)) matches.add(friend);
        return matches;
    }

    public Friend findById(int id) {
        for (Friend friend : store.friends) if (friend.getId() == id) return friend;
        return null;
    }

    public void add(String name, LocalDate date, String mbti) throws IOException {
        validate(name, date, mbti);
        Store updated = copy();
        updated.friends.add(new Friend(updated.nextId++, name.trim(), date, normalize(mbti)));
        save(updated);
        store = updated;
    }

    public void update(int id, String name, LocalDate date, String mbti) throws IOException {
        validate(name, date, mbti);
        Store updated = copy();
        for (int i = 0; i < updated.friends.size(); i++) {
            if (updated.friends.get(i).getId() == id) {
                updated.friends.set(i, new Friend(id, name.trim(), date, normalize(mbti)));
                save(updated); store = updated; return;
            }
        }
        throw new IllegalArgumentException("친구를 찾을 수 없습니다.");
    }

    public void delete(int id) throws IOException {
        Store updated = copy();
        for (int i = 0; i < updated.friends.size(); i++) {
            if (updated.friends.get(i).getId() == id) {
                updated.friends.remove(i); save(updated); store = updated; return;
            }
        }
        throw new IllegalArgumentException("친구를 찾을 수 없습니다.");
    }

    private Store copy() {
        Store result = new Store(); result.nextId = store.nextId; result.friends = new ArrayList<>(store.friends); return result;
    }
    private String normalize(String mbti) { return mbti == null || mbti.trim().isEmpty() ? null : MbtiDataManager.normalizeType(mbti); }
    private void validate(String name, LocalDate date, String mbti) {
        if (name == null || name.trim().isEmpty()) throw new IllegalArgumentException("이름이 필요합니다.");
        if (date != null && (date.getYear() < 1 || date.getYear() > 9999)) throw new IllegalArgumentException("생년월일을 확인해 주세요.");
        String normalized = normalize(mbti);
        if (normalized != null && !mbtiData.isValidType(normalized)) throw new IllegalArgumentException("올바른 MBTI가 아닙니다.");
    }
}
