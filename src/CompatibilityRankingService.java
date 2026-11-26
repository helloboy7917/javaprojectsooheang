import java.util.ArrayList;
import java.util.List;

public class CompatibilityRankingService {
    public static class Ranking {
        public final Friend first;
        public final Friend second;
        public final int score;
        public int rank;
        Ranking(Friend first, Friend second, int score) { this.first = first; this.second = second; this.score = score; }
    }

    public ArrayList<Ranking> calculate(List<Friend> friends, boolean birth) {
        ArrayList<Ranking> result = new ArrayList<>();
        CompatibilityTest test = birth ? new BirthCompatibilityTest() : new NameCompatibilityTest();
        // 중첩 for문에서 j를 i+1부터 시작해 각 조합을 한 번만 계산한다.
        for (int i = 0; i < friends.size(); i++) {
            for (int j = i + 1; j < friends.size(); j++) {
                Friend a = friends.get(i), b = friends.get(j);
                if (birth && (a.getBirthDate() == null || b.getBirthDate() == null)) continue;
                if (compareFriend(a, b) > 0) { Friend temp = a; a = b; b = temp; }
                int score = test.calculate(a.toPerson(), b.toPerson()).getScore();
                result.add(new Ranking(a, b, score));
            }
        }
        // 높은 점수 우선, 동점이면 이름과 ID 순서로 선택 정렬한다.
        for (int i = 0; i < result.size(); i++) {
            int best = i;
            for (int j = i + 1; j < result.size(); j++) if (compare(result.get(j), result.get(best)) < 0) best = j;
            Ranking temp = result.get(i); result.set(i, result.get(best)); result.set(best, temp);
        }
        // 같은 점수는 공동 순위, 다음 점수는 앞에 나온 조합 수만큼 순위를 건너뛴다.
        for (int i = 0; i < result.size(); i++) {
            result.get(i).rank = i > 0 && result.get(i).score == result.get(i - 1).score ? result.get(i - 1).rank : i + 1;
        }
        return result;
    }
    private int compare(Ranking a, Ranking b) {
        if (a.score != b.score) return Integer.compare(b.score, a.score);
        int first = a.first.getName().compareTo(b.first.getName());
        if (first != 0) return first;
        int second = a.second.getName().compareTo(b.second.getName());
        if (second != 0) return second;
        int firstId = Integer.compare(a.first.getId(), b.first.getId());
        return firstId != 0 ? firstId : Integer.compare(a.second.getId(), b.second.getId());
    }
    private int compareFriend(Friend a, Friend b) {
        int name = a.getName().compareTo(b.getName());
        return name != 0 ? name : Integer.compare(a.getId(), b.getId());
    }
}
