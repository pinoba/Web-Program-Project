<%@ page pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%-- 영양 막대 5줄. include 전에 n(Nutrition 또는 Recipe)을 c:set 으로 정해 둔다. --%>
<div class="nutri">
  <c:forEach var="row" items="${n.rows}">
    <div class="nutri-row ${row.over ? 'over' : ''}" data-key="${row.key}">
      <span class="nutri-label">${row.label}</span>
      <span class="nutri-value"><span class="v"><fmt:formatNumber value="${row.value}" maxFractionDigits="1" /></span>${row.unit}</span>
      <span class="bar"><span class="bar-fill" style="width:${row.barWidth}%"></span></span>
      <span class="nutri-pct"><span class="p">${row.pct}</span>%</span>
    </div>
  </c:forEach>
</div>
