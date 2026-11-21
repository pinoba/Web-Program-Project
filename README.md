# 밥심 — 영양 기반 레시피 검색 서비스

Maven 웹 프로젝트 · Tomcat 9 (javax.servlet) · Servlet + JSP(EL, JSTL) · H2 내장형 DB

## 1. 이클립스에서 실행하기

1. `src/main/resources/api.properties.example` 을 복사해 `api.properties` 로 이름을 바꾸고 `apiKey=` 뒤에 식약처 인증키 입력
   (`api.properties` 는 `.gitignore` 에 있어 GitHub 에 올라가지 않음)
2. File → Import → General → **Existing Projects into Workspace** → 내려받은(clone) `bapsim` 폴더 선택
   (안 되면 File → Import → Maven → Existing Maven Projects)
3. 프로젝트 우클릭 → Maven → Update Project (Alt+F5)
4. 프로젝트 우클릭 → Run As → Run on Server → Tomcat v9.0
5. http://localhost:8080/bapsim/ 접속
6. `admin / admin1234` 로 로그인 → 상단 **관리자** → **API 데이터 적재 실행**

- 서버가 시작될 때 `InitServlet` 의 `init()` 이 테이블과 관리자 계정을 만듭니다. DB 파일 위치: `사용자 홈\bapsim\bapsimdb.mv.db`
- DB 를 처음부터 다시 만들려면 톰캣을 끄고 `사용자 홈\bapsim` 폴더를 지우면 됩니다.
- Server 의 Tomcat 이름이 `Apache Tomcat v9.0` 이 아니면 프로젝트 Properties → Targeted Runtimes 에서 체크하세요.

## 2. 폴더 구조

```
bapsim
├─ pom.xml                       JSTL 1.2, H2
├─ src/main/java/com/ac/web      자바 클래스 전부 (한 패키지)
│   ├─ 서블릿(컨트롤)   InitServlet MainServlet SearchServlet IngredientServlet RecipeServlet
│   │                  FavoriteApiServlet MemberServlet MypageServlet AdminServlet
│   ├─ 자바빈(모델)     Member Recipe Nutrition NutriRow Paging SearchCondition
│   ├─ DB 처리         DBUtil MemberDAO RecipeDAO IngredientDAO FavoriteDAO RecipeSaveDAO
│   └─ 기타            Config RecipeParser RecipeLoader PasswordUtil JsonUtil
└─ src/main/webapp
    ├─ index.jsp main.jsp search.jsp ingredient.jsp recipe.jsp
    │  login.jsp join.jsp mypage.jsp admin.jsp error.jsp   (화면 = 뷰)
    ├─ inc/       header.jsp footer.jsp recipeCard.jsp paging.jsp nutritionBars.jsp  (<%@ include %> 조각)
    ├─ css/style.css   js/*.js
    └─ WEB-INF/   web.xml schema.sql
```

**요청 흐름 (MVC)**: 브라우저 → 서블릿(`@WebServlet`)이 파라미터 처리·DAO 호출 → `request.setAttribute()` → `RequestDispatcher.forward("xxx.jsp")` → JSP 가 EL·JSTL 로 출력

- JSP 주소(예: `/bapsim/recipe.jsp`)로 바로 열면 데이터가 없으므로 첫 줄의 `<c:if>` + `<c:redirect>` 로 서블릿 주소로 보냅니다.
- 로그인 확인은 각 서블릿에서 `session.getAttribute("loginMember")` 로 직접 합니다.
- AJAX 응답(JSON)은 `JsonUtil` 로 문자열을 직접 만듭니다. 외부 JSON 라이브러리를 쓰지 않습니다.
- 공공데이터 API 는 XML 형식으로 받아 JDK 기본 XML 파서(DOM)로 읽습니다.

## 3. URL 정리

| URL | 서블릿 → JSP | 기능 | 로그인 |
|---|---|---|---|
| `/` `/main` | MainServlet → main.jsp | 메인 (랜덤·저칼로리 추천) | - |
| `/search` | SearchServlet → search.jsp | 음식 종류 (검색·필터·정렬·페이징) | - |
| `/ingredient` | IngredientServlet → ingredient.jsp | 재료로 검색 | - |
| `/api/ingredients` | IngredientServlet | 재료 자동완성 (AJAX) | - |
| `/recipe?id=` | RecipeServlet → recipe.jsp | 음식 상세 + 최근 본 레시피(세션) 기록 | - |
| `/api/favorite` | FavoriteApiServlet | 즐겨찾기 저장·해제 (AJAX) | 필요 |
| `/login` `/join` | MemberServlet → login.jsp, join.jsp | 로그인·회원가입 | - |
| `/logout` | MemberServlet | 로그아웃 (세션 종료) | - |
| `/api/idcheck` | MemberServlet | 아이디 중복 확인 (AJAX) | - |
| `/mypage` | MypageServlet → mypage.jsp | 즐겨찾기·최근 본 레시피 | 필요 |
| `/admin` `/admin/load` `/admin/status` | AdminServlet → admin.jsp | 적재 화면·실행·진행 상황 | 관리자 |

## 4. 구현 포인트

- **데이터 정제** `RecipeParser`: 제각각인 재료 문자열(●, 그룹명:, [1인분], 괄호 분량)에서 재료명만 추출, 조리 단계 번호·끝 영문자 제거
- **별도 스레드 적재 + 진행 막대** `RecipeLoader`: 1,000건씩 나눠 호출, 레시피 1건 = 1트랜잭션, 실패 시 롤백, 관리자 화면은 0.5초마다 상태 조회
- **동적 SQL** `RecipeDAO.search`: 검색어·필터 조합에 따라 WHERE 절 생성, 정렬은 정해진 값만 허용, 모든 값은 PreparedStatement 바인딩
- **AJAX 4종**: 재료 자동완성, 아이디 중복 확인, 즐겨찾기, 적재 진행 상황
- **서블릿 생명주기 활용** `InitServlet.init()`: 서버 시작 시 DB 초기화
- **보안**: 비밀번호 솔트+SHA-256, 로그인 시 세션 ID 변경, 서블릿마다 권한 확인, 로그인 후 이동 주소는 내부 경로만 허용, 출력은 `<c:out>` 으로 XSS 방지
