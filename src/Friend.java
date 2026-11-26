import java.time.LocalDate;

/** 파일에 저장되는 친구 정보. ID는 이름이 같아도 서로를 구별한다. */
public class Friend {
    private int id;
    private String name;
    private LocalDate birthDate;
    private String mbti;

    public Friend(int id, String name, LocalDate birthDate, String mbti) {
        this.id = id;
        this.name = name;
        this.birthDate = birthDate;
        this.mbti = mbti;
    }
    public int getId() { return id; }
    public String getName() { return name; }
    public LocalDate getBirthDate() { return birthDate; }
    public String getMbti() { return mbti; }

    // 저장된 친구의 정보를 기존 궁합 계산용 Person 객체로 전달한다.
    public Person toPerson() {
        Person person = new Person(name, birthDate);
        person.setMbti(mbti);
        return person;
    }
}
