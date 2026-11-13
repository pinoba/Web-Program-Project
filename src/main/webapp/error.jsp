<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="pageTitle" value="오류" />
<%@ include file="inc/header.jsp" %>

<section class="auth-box center">
  <c:choose>
    <c:when test="${requestScope['javax.servlet.error.status_code'] == 404}">
      <h1>페이지를 찾을 수 없어요</h1>
      <p class="muted">주소가 바뀌었거나 없는 레시피입니다.</p>
    </c:when>
    <c:otherwise>
      <h1>잠시 문제가 생겼어요</h1>
      <p class="muted">요청을 처리하는 중 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.</p>
    </c:otherwise>
  </c:choose>
  <a class="btn btn-primary" href="${ctx}/main">메인으로</a>
</section>

<%@ include file="inc/footer.jsp" %>
</body>
</html>
