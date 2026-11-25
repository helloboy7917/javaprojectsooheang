public class Person {
    private String name;
    private String mbti;

    public Person(String name) {
        this.name = name;
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
}
