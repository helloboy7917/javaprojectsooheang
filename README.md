# javaprojectsooheang

IntelliJ에서 `src/Main.java`의 실행 버튼을 누른 뒤 메뉴에서
`1`을 선택하면 이름 궁합, `2`를 선택하면 MBTI 궁합을 볼 수 있습니다.
MBTI 유형은 `INFP`처럼 입력하며 대소문자와 입력 순서는 상관없습니다.
메뉴에서 `0`을 입력하면 종료합니다.

MBTI 데이터는 `src/main/resources/mbti_compatibility.json`에 있으며,
`MbtiDataManager`가 처음 사용할 때 클래스패스에서 읽어 재사용합니다.
IntelliJ에서 `pom.xml`을 Maven 프로젝트로 열고 의존성을 가져오면
Gson과 resources가 자동으로 설정됩니다.
