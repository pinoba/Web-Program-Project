<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%-- JSP 주소로 바로 열면(서블릿을 거치지 않으면) 서블릿으로 보냄 --%>
<c:if test="${empty requestScope['javax.servlet.forward.request_uri']}"><c:redirect url="/main" /></c:if>
<c:set var="pageTitle" value="오늘 뭐 먹지?" />
<%@ include file="inc/header.jsp" %>

<section class="hero">
  <p class="hero-eyebrow">영양 기반 레시피 검색</p>
  <h1>오늘 뭐 먹지?</h1>
  <form class="hero-search" action="${ctx}/search" method="get">
    <input type="search" name="q" placeholder="메뉴명 또는 해시태그 검색 (예: 두부, 다이어트)" aria-label="메뉴명 또는 해시태그 검색" autofocus>
    <button type="submit" class="btn btn-primary">검색</button>
  </form>

  <div class="category-row" aria-label="카테고리 바로가기">
    <c:forEach var="cat" items="밥,국&찌개,반찬,일품,후식,기타">
      <c:url var="catUrl" value="/search"><c:param name="cat" value="${cat}" /></c:url>
      <a class="cat-btn" href="${catUrl}"><c:out value="${fn:replace(cat, '&', '·')}" /></a>
    </c:forEach>
  </div>
</section>

<c:choose>
  <c:when test="${recipeCount == 0}">
    <div class="notice">
      아직 저장된 레시피가 없습니다. 관리자 계정으로 로그인한 뒤 <a href="${ctx}/admin">관리자 페이지</a>에서 공공데이터를 적재하세요.
    </div>
  </c:when>
  <c:otherwise>
    <section class="section">
      <div class="section-head">
        <h2>오늘의 랜덤 추천</h2>
        <span class="muted">접속할 때마다 바뀌어요 · 전체 <fmt:formatNumber value="${recipeCount}" />개 레시피</span>
      </div>
      <div class="grid">
        <c:forEach var="r" items="${randomList}">
          <%@ include file="inc/recipeCard.jsp" %>
        </c:forEach>
      </div>
    </section>

    <section class="section">
      <div class="section-head">
        <h2>저칼로리 추천</h2>
        <c:url var="lowUrl" value="/search"><c:param name="kcal" value="200" /><c:param name="sort" value="kcal" /></c:url>
        <a class="more" href="${lowUrl}">200kcal 이하 전체 보기 →</a>
      </div>
      <div class="grid">
        <c:forEach var="r" items="${lowKcalList}">
          <%@ include file="inc/recipeCard.jsp" %>
        </c:forEach>
      </div>
    </section>
  </c:otherwise>
</c:choose>

<%@ include file="inc/footer.jsp" %>
</body>
</html>
