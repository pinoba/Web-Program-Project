<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%-- JSP 주소로 바로 열면(서블릿을 거치지 않으면) 서블릿으로 보냄 --%>
<c:if test="${empty requestScope['javax.servlet.forward.request_uri']}"><c:redirect url="/mypage" /></c:if>
<c:set var="pageTitle" value="마이페이지" />
<%@ include file="inc/header.jsp" %>

<div class="mypage-head">
  <h1 class="page-title"><c:out value="${me.nickname}" /> 님의 페이지</h1>
  <form action="${ctx}/logout" method="post">
    <button type="submit" class="btn btn-ghost">로그아웃</button>
  </form>
</div>

<section class="section">
  <div class="section-head">
    <h2>즐겨찾기 <span id="favCount" class="count">${fn:length(favorites)}</span></h2>
  </div>
  <div id="favGrid" class="grid">
    <c:forEach var="r" items="${favorites}">
      <div class="fav-item" data-recipe-id="${r.id}">
        <%@ include file="inc/recipeCard.jsp" %>
        <button type="button" class="btn btn-outline btn-sm fav-remove">♥ 해제</button>
      </div>
    </c:forEach>
  </div>
  <div id="favEmpty" class="empty small" ${empty favorites ? '' : 'hidden'}>즐겨찾기한 레시피가 없습니다.</div>
</section>

<section class="section">
  <div class="section-head">
    <h2>최근 본 레시피</h2>
    <span class="muted small">최근 5개 · 로그아웃하면 초기화됩니다</span>
  </div>
  <c:choose>
    <c:when test="${empty sessionScope.recentRecipes}">
      <div class="empty small">최근 본 레시피가 없습니다.</div>
    </c:when>
    <c:otherwise>
      <ul class="recent-list">
        <c:forEach var="r" items="${sessionScope.recentRecipes}">
          <li>
            <a href="${ctx}/recipe?id=${r.id}">
              <span class="recent-name"><c:out value="${r.name}" /></span>
              <span class="muted"><c:out value="${r.categoryLabel}" /> · <fmt:formatNumber value="${r.kcal}" maxFractionDigits="1" /> kcal</span>
            </a>
          </li>
        </c:forEach>
      </ul>
    </c:otherwise>
  </c:choose>
</section>

<%@ include file="inc/footer.jsp" %>
<script src="${ctx}/js/mypage.js"></script>
</body>
</html>
