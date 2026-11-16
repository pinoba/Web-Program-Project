<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%-- JSP 주소로 바로 열면(서블릿을 거치지 않으면) 서블릿으로 보냄 --%>
<c:if test="${empty requestScope['javax.servlet.forward.request_uri']}"><c:redirect url="/ingredient" /></c:if>
<c:set var="pageTitle" value="재료로 검색" />
<%@ include file="inc/header.jsp" %>

<section class="ing-panel">
  <h1>가지고 있는 재료를 입력하세요</h1>
  <p class="muted">재료를 고르면 태그로 추가되고, 입력한 재료가 하나라도 들어간 레시피를 보여줍니다. (최대 10개)</p>

  <form id="ingForm" action="${ctx}/ingredient" method="get" autocomplete="off">
    <div class="ing-input-row">
      <div class="ac-wrap">
        <input type="text" id="ingInput" placeholder="예: 두부" aria-label="재료 입력"
               role="combobox" aria-autocomplete="list" aria-expanded="false" aria-controls="acList">
        <ul id="acList" class="ac-list" role="listbox" hidden></ul>
      </div>
      <button type="submit" class="btn btn-primary">검색</button>
    </div>

    <%-- 고른 재료 태그 (× 로 제거). hidden input 으로 함께 전송 --%>
    <div id="tagBox" class="tag-box">
      <c:forEach var="ing" items="${ingredients}">
        <span class="tag" data-name="<c:out value='${ing}'/>">
          <c:out value="${ing}" />
          <button type="button" class="tag-x" aria-label="<c:out value='${ing}'/> 제거">×</button>
          <input type="hidden" name="ing" value="<c:out value='${ing}'/>">
        </span>
      </c:forEach>
    </div>
    <p id="ingMsg" class="form-msg" aria-live="polite"></p>
  </form>
</section>

<c:if test="${not empty result}">
  <section class="section">
    <div class="section-head">
      <h2>검색 결과 <span class="count"><fmt:formatNumber value="${result.total}" />건</span></h2>
    </div>
    <c:choose>
      <c:when test="${result.total == 0}">
        <div class="empty">입력한 재료가 들어간 레시피가 없습니다.</div>
      </c:when>
      <c:otherwise>
        <div class="grid">
          <c:forEach var="r" items="${result.items}">
            <%@ include file="inc/recipeCard.jsp" %>
          </c:forEach>
        </div>
      </c:otherwise>
    </c:choose>

    <c:url var="pageBase" value="/ingredient">
      <c:forEach var="ing" items="${ingredients}"><c:param name="ing" value="${ing}" /></c:forEach>
    </c:url>
    <%@ include file="inc/paging.jsp" %>
  </section>
</c:if>

<%@ include file="inc/footer.jsp" %>
<script src="${ctx}/js/ingredient.js"></script>
</body>
</html>
