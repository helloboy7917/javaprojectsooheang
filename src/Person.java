import java.time.LocalDate;

public class Person {
    private String name;
    private String mbti;
    private LocalDate birthDate;

    public Person(String name) {
        this.name = name;
    }

    public Person(String name, LocalDate birthDate) {
        this(name);
        this.birthDate = birthDate;
    }

    public static Person withMbti(String mbti) {
        Person person = new Person(null);
        person.setMbti(mbti);
        return person;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getMbti() {
        return mbti;
    }

    public void setMbti(String mbti) {
        this.mbti = mbti;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }
}
