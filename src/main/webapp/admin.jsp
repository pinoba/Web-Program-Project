<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%-- JSP 주소로 바로 열면(서블릿을 거치지 않으면) 서블릿으로 보냄 --%>
<c:if test="${empty requestScope['javax.servlet.forward.request_uri']}"><c:redirect url="/admin" /></c:if>
<c:set var="pageTitle" value="관리자" />
<%@ include file="inc/header.jsp" %>

<h1 class="page-title">공공데이터 적재</h1>

<section class="panel admin-panel">
  <p>현재 저장된 레시피 <strong id="dbCount"><fmt:formatNumber value="${recipeCount}" /></strong>건</p>
  <p class="muted small">식약처 조리식품 레시피 API(COOKRCP01)를 1~1,000건, 1,001건~끝 두 번 나눠 호출합니다.
     이미 있는 레시피는 중복 저장하지 않고 갱신합니다.</p>

  <button type="button" id="loadBtn" class="btn btn-primary" ${running ? 'disabled' : ''}>API 데이터 적재 실행</button>

  <%-- 진행 상황 --%>
  <div class="progress-wrap">
    <div class="progress"><div id="progFill" class="progress-fill" style="width:0%"></div></div>
    <p id="progMsg" class="muted"><c:out value="${message}" /></p>
  </div>

  <%-- 적재 결과 --%>
  <table class="result-table">
    <thead>
      <tr><th>전체</th><th>신규 저장</th><th>갱신</th><th>실패</th><th>열량 이상치</th></tr>
    </thead>
    <tbody>
      <tr>
        <td id="stTotal">${total}</td>
        <td id="stInserted">${inserted}</td>
        <td id="stUpdated">${updated}</td>
        <td id="stFailed">${failed}</td>
        <td id="stMismatch">${mismatch}</td>
      </tr>
    </tbody>
  </table>
  <p class="muted small">열량 이상치: 표기 열량과 탄수화물·단백질·지방 환산 열량의 차이가 큰 레시피 (상세 화면에 안내 문구 표시)</p>
</section>

<%@ include file="inc/footer.jsp" %>
<script src="${ctx}/js/admin.js"></script>
</body>
</html>
