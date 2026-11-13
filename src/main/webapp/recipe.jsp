<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%-- JSP 주소로 바로 열면(서블릿을 거치지 않으면) 서블릿으로 보냄 --%>
<c:if test="${empty requestScope['javax.servlet.forward.request_uri']}"><c:redirect url="/main" /></c:if>
<c:set var="pageTitle" value="${recipe.name}" />
<%@ include file="inc/header.jsp" %>

<article class="detail">
  <header class="detail-head">
    <div class="detail-img">
      <c:if test="${not empty recipe.imgMain}">
        <img src="<c:out value='${recipe.imgMain}'/>" alt="<c:out value='${recipe.name}'/>" onerror="this.remove()">
      </c:if>
    </div>
    <div class="detail-info">
      <p class="detail-meta">
        <c:out value="${recipe.categoryLabel}" />
        <c:if test="${not empty recipe.cookWay}"> · <c:out value="${recipe.cookWay}" /></c:if>
        · <fmt:formatNumber value="${recipe.kcal}" maxFractionDigits="1" /> kcal
        <c:if test="${not empty recipe.weight and recipe.weight != '0'}"> · 1인분 <c:out value="${recipe.weight}" />g</c:if>
      </p>
      <h1 class="detail-title"><c:out value="${recipe.name}" /></h1>
      <c:if test="${not empty recipe.hashTag}">
        <c:url var="tagUrl" value="/search"><c:param name="q" value="${recipe.hashTag}" /></c:url>
        <a class="chip chip-tag" href="${tagUrl}">#<c:out value="${recipe.hashTag}" /></a>
      </c:if>

      <%-- 2. 즐겨찾기 (AJAX) --%>
      <div class="detail-actions">
        <button type="button" id="favBtn" class="btn btn-outline ${favorited ? 'is-on' : ''}"
                data-recipe-id="${recipe.id}" aria-pressed="${favorited ? 'true' : 'false'}">
          <span class="ico">${favorited ? '♥' : '♡'}</span> <span class="txt">${favorited ? '즐겨찾기됨' : '즐겨찾기'}</span>
        </button>
      </div>
    </div>
  </header>

  <div class="detail-grid">
    <%-- 1. 영양성분: 1일 기준치 대비 막대 --%>
    <section class="panel">
      <h2 class="panel-title">영양 정보 <span class="muted small">성인 1일 기준치 대비</span></h2>
      <c:set var="n" value="${recipe}" />
      <%@ include file="inc/nutritionBars.jsp" %>
      <c:if test="${recipe.kcalMismatch}">
        <p class="warn-note">
          ⚠ 표기 열량(<fmt:formatNumber value="${recipe.kcal}" maxFractionDigits="1" />kcal)과
          탄수화물·단백질·지방으로 환산한 열량(약 <fmt:formatNumber value="${recipe.calcKcal}" maxFractionDigits="0" />kcal)이
          크게 다릅니다. 원자료 값을 그대로 표시합니다.
        </p>
      </c:if>
    </section>

    <section class="panel">
      <h2 class="panel-title">재료</h2>
      <ul class="parts">
        <c:forEach var="line" items="${recipe.partsLines}">
          <li><c:out value="${line}" /></li>
        </c:forEach>
      </ul>
    </section>
  </div>

  <%-- 3. 조리 순서: 번호 순서대로 글로 표시 --%>
  <section class="panel">
    <h2 class="panel-title">조리 순서</h2>
    <ol class="steps">
      <c:forEach var="step" items="${recipe.steps}">
        <li><c:out value="${step}" /></li>
      </c:forEach>
    </ol>
  </section>

  <c:if test="${not empty recipe.tip}">
    <section class="panel tip">
      <h2 class="panel-title">저감 조리 팁</h2>
      <p><c:out value="${recipe.tip}" /></p>
    </section>
  </c:if>
</article>

<%@ include file="inc/footer.jsp" %>
<script src="${ctx}/js/recipe.js"></script>
</body>
</html>
