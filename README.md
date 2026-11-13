# 밥심 — 영양 기반 레시피 검색 서비스

Dynamic Web Project → Convert to Maven Project 구조 · Tomcat 9 (javax.servlet) · Servlet + JSP(EL, JSTL) · H2 내장형 DB

## 1. 이클립스에서 실행하기

1. `src/main/resources/api.properties.example` 을 복사해 `api.properties` 로 이름을 바꾸고 `apiKey=` 뒤에 식약처 인증키 입력
   (`api.properties` 는 `.gitignore` 에 있어 GitHub 에 올라가지 않음)
2. File → Import → General → **Existing Projects into Workspace** → 압축 푼 `bapsim` 폴더 선택
   (안 되면 File → Import → Maven → Existing Maven Projects)
3. 프로젝트 우클릭 → Maven → Update Project (Alt+F5)
4. 프로젝트 우클릭 → Run As → Run on Server → Tomcat v9.0
5. http://localhost:8080/bapsim/ 접속
6. `admin / admin1234` 로 로그인 → 상단 **관리자** → **API 데이터 적재 실행**

- 서버가 시작될 때 `InitServlet` 의 `init()` 이 테이블과 관리자 계정을 만듭니다. DB 파일 위치: `사용자 홈\bapsim\bapsimdb.mv.db`
- DB 를 처음부터 다시 만들려면 톰캣을 끄고 `사용자 홈\bapsim` 폴더를 지우면 됩니다.
- Server 의 Tomcat 이름이 `Apache Tomcat v9.0` 이 아니면 프로젝트 Properties → Targeted Runtimes 에서 체크하세요.

## 2. 폴더 구조 (수업 실습과 같은 방식)

```
bapsim
├─ pom.xml                       JSTL 1.2, H2 (Convert to Maven Project 형식)
├─ src/main/java/com/ac/web      자바 클래스 전부 (한 패키지)
│   ├─ 서블릿(컨트롤)   InitServlet MainServlet SearchServlet RecipeServlet FavoriteApiServlet
│   │                  LoginServlet JoinServlet IdCheckServlet LogoutServlet MypageServlet AdminServlet
│   ├─ 자바빈(모델)     Member Recipe Nutrition NutriRow Paging SearchCondition
│   ├─ DB 처리         DBUtil MemberDAO RecipeDAO FavoriteDAO RecipeSaveDAO
│   └─ 기타            Config RecipeParser RecipeLoader PasswordUtil JsonUtil
└─ src/main/webapp
    ├─ index.jsp main.jsp search.jsp recipe.jsp
    │  login.jsp join.jsp mypage.jsp admin.jsp error.jsp   (화면 = 뷰)
    ├─ inc/       header.jsp footer.jsp recipeCard.jsp paging.jsp nutritionBars.jsp  (<%@ include %> 조각)
    ├─ css/style.css   js/*.js
    └─ WEB-INF/   web.xml schema.sql
```

**흐름 (6장 MVC 실습과 동일)**: 브라우저 → 서블릿(`@WebServlet`)이 파라미터 처리·DAO 호출 → `request.setAttribute()` → `RequestDispatcher.forward("xxx.jsp")` → JSP 가 EL·JSTL 로 출력

- JSP 주소(예: `/bapsim/recipe.jsp`)로 바로 열면 데이터가 없으므로 첫 줄의 `<c:if>` + `<c:redirect>` 로 서블릿 주소로 보냅니다.
- 로그인 확인은 각 서블릿에서 `session.getAttribute("loginMember")` 로 직접 합니다.
- AJAX 응답(JSON)은 `JsonUtil` 로 문자열을 직접 만듭니다. 외부 JSON 라이브러리를 쓰지 않습니다.
- 공공데이터 API 는 XML 형식으로 받아 JDK 기본 XML 파서(DOM)로 읽습니다.

## 3. URL 정리

| URL | 서블릿 → JSP | 기능 | 로그인 |
|---|---|---|---|
| `/` `/main` | MainServlet → main.jsp | 메인 (랜덤·저칼로리 추천) | - |
| `/search` | SearchServlet → search.jsp | 음식 종류 (검색·필터·정렬·페이징) | - |
| `/recipe?id=` | RecipeServlet → recipe.jsp | 음식 상세 + 최근 본 레시피(세션) 기록 | - |
| `/api/favorite` | FavoriteApiServlet | 즐겨찾기 저장·해제 (AJAX) | 필요 |
| `/login` `/join` | LoginServlet, JoinServlet → login.jsp, join.jsp | 로그인·회원가입 | - |
| `/logout` | LogoutServlet | 로그아웃 (세션 종료) | - |
| `/api/idcheck` | IdCheckServlet | 아이디 중복 확인 (AJAX) | - |
| `/mypage` | MypageServlet → mypage.jsp | 즐겨찾기·최근 본 레시피 | 필요 |
| `/admin` `/admin/load` `/admin/status` | AdminServlet → admin.jsp | 적재 화면·실행·진행 상황 | 관리자 |

## 4. 단계별 구현 순서

각 단계가 끝나면 서버를 띄워 "확인"을 해 보고 다음 단계로 넘어갑니다.

**1단계 — 프로젝트·DB 설정**
Dynamic Web Project 생성 → Convert to Maven Project → pom.xml 의존성 추가, Config, DBUtil, WEB-INF/schema.sql, InitServlet
확인: 서버 실행 시 콘솔에 `<bapsim> DB 초기화 완료`, 사용자 홈에 `bapsim` 폴더 생성

**2단계 — 공통 화면 틀**
index.jsp, MainServlet(추천 없이), main.jsp, inc/header.jsp, inc/footer.jsp, css/style.css, js/common.js
확인: `/bapsim/` 접속 시 헤더와 메인 화면

**3단계 — 회원 기능**
Member, PasswordUtil, MemberDAO, LoginServlet, JoinServlet, IdCheckServlet, LogoutServlet, JsonUtil, login.jsp, join.jsp, js/join.js
확인: 회원가입 → 중복확인 → 로그인 → 헤더에 닉네임 표시, admin 계정 로그인

**4단계 — 공공데이터 적재 (관리자)**
RecipeParser, RecipeSaveDAO, RecipeLoader, AdminServlet, admin.jsp, js/admin.js
확인: 적재 후 1,156건 / 조리 단계가 깨끗하게 들어갔는지 / 일반 회원은 /admin 접근 시 메인으로

**5단계 — 요리 검색 (음식 종류)**
Recipe, Nutrition, Paging, SearchCondition, RecipeDAO(search·findRandom·findLowKcal), SearchServlet, search.jsp, inc/recipeCard.jsp, inc/paging.jsp
확인: '두부' 검색 → 필터 적용 → 정렬을 바꿔도 필터 유지 → 페이지 이동

**6단계 — 음식 상세**
RecipeDAO.findById, NutriRow, RecipeServlet(최근 본 레시피 세션), inc/nutritionBars.jsp, recipe.jsp
확인: 영양 막대, 재료, 조리 순서, 열량 이상치 안내 (두부곤약조림)

**7단계 — 즐겨찾기·마이페이지**
FavoriteDAO, FavoriteApiServlet, MypageServlet, js/recipe.js, mypage.jsp, js/mypage.js
확인: 비로그인 시 로그인 화면으로 → 즐겨찾기 저장·해제가 새로고침 없이 동작 → 로그아웃 시 최근 본 레시피 초기화

**8단계 — 마무리**
error.jsp (404/500, web.xml 의 error-page), 화면 점검, 데모 영상 시나리오 리허설

## 5. 데모 영상에서 강조할 만한 부분 (구현 난이도)

- **데이터 정제** `RecipeParser`: 조리 단계 번호·끝 영문자 제거, 열량 이상치 검사
- **별도 스레드 적재 + 진행 막대** `RecipeLoader`: 1,000건씩 나눠 호출, 레시피 1건 = 1트랜잭션, 실패 시 롤백, 관리자 화면은 0.5초마다 상태 조회
- **동적 SQL** `RecipeDAO.search`: 검색어·필터 조합에 따라 WHERE 절 생성, 정렬은 정해진 값만 허용, 모든 값은 PreparedStatement 바인딩
- **AJAX 3종**: 아이디 중복 확인, 즐겨찾기, 적재 진행 상황
- **서블릿 생명주기 활용** `InitServlet.init()`: 서버 시작 시 DB 초기화
- **보안**: 비밀번호 솔트+SHA-256, 로그인 시 세션 ID 변경, 서블릿마다 권한 확인, 로그인 후 이동 주소는 내부 경로만 허용, 출력은 `<c:out>` 으로 XSS 방지
