<%@ page pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%-- 레시피 카드 한 개. forEach 의 var="r" 를 사용 --%>
<a class="card" href="${ctx}/recipe?id=${r.id}">
  <div class="card-img">
    <c:if test="${not empty r.imgMain}">
      <img src="<c:out value='${r.imgMain}'/>" alt="" loading="lazy" onerror="this.remove()">
    </c:if>
    <c:if test="${not empty r.category}"><span class="chip chip-cat"><c:out value="${r.categoryLabel}" /></span></c:if>
  </div>
  <div class="card-body">
    <h3 class="card-title"><c:out value="${r.name}" /></h3>
    <p class="card-kcal"><fmt:formatNumber value="${r.kcal}" maxFractionDigits="1" /> kcal</p>
    <c:if test="${not empty r.matchedIngredients}">
      <div class="matched">
        <span class="matched-label">포함 재료</span>
        <c:forEach var="ing" items="${r.matchedIngredients}">
          <span class="chip chip-ing"><c:out value="${ing}" /></span>
        </c:forEach>
      </div>
    </c:if>
  </div>
</a>
