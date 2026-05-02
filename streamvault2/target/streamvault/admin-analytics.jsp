<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>StreamVault – Admin Analytics</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>

<nav>
  <span class="nav-brand">StreamVault</span>
  <div class="nav-links">
    <a href="${pageContext.request.contextPath}/home">Browse</a>
    <a href="${pageContext.request.contextPath}/admin-analytics">Analytics</a>
    <a href="${pageContext.request.contextPath}/logout" class="btn-logout">Sign Out</a>
  </div>
</nav>

<div class="container">
  <div class="page-header">
    <h1>⚡ Admin Analytics</h1>
    <p>Platform insights — MySQL + MongoDB (Phase 3)</p>
  </div>

  <!-- ── 1. Top 10 Content ─────────────────────────────────────────────── -->
  <div class="section-title">🏆 Top 10 Content by Views (MySQL)</div>
  <div style="overflow-x:auto;">
    <table class="data-table">
      <thead><tr><th>Rank</th><th>Title</th><th>Total Views</th></tr></thead>
      <tbody>
        <c:set var="rank" value="${1}"/>
        <c:forEach var="row" items="${top10}">
          <tr>
            <td>#${rank}</td>
            <td><c:out value="${row[0]}"/></td>
            <td><c:out value="${row[1]}"/></td>
          </tr>
          <c:set var="rank" value="${rank + 1}"/>
        </c:forEach>
        <c:if test="${empty top10}">
          <tr><td colspan="3" style="color:var(--muted);text-align:center;">
            No watch data yet — start watching content to populate this.
          </td></tr>
        </c:if>
      </tbody>
    </table>
  </div>

  <!-- ── 2. Revenue by Plan ────────────────────────────────────────────── -->
  <div class="section-title">💰 Revenue by Subscription Plan (MySQL)</div>
  <div style="overflow-x:auto;">
    <table class="data-table">
      <thead><tr><th>Plan</th><th>Total Revenue ($)</th><th>Payments</th></tr></thead>
      <tbody>
        <c:forEach var="row" items="${revenue}">
          <tr>
            <td><c:out value="${row[0]}"/></td>
            <td>$<c:out value="${row[1]}"/></td>
            <td><c:out value="${row[2]}"/></td>
          </tr>
        </c:forEach>
        <c:if test="${empty revenue}">
          <tr><td colspan="3" style="color:var(--muted);text-align:center;">No payment data yet.</td></tr>
        </c:if>
      </tbody>
    </table>
  </div>

  <!-- ── 3. Rating Ranking RANK() ──────────────────────────────────────── -->
  <div class="section-title">⭐ Content Rating Ranking — RANK() (MySQL)</div>
  <div style="overflow-x:auto;">
    <table class="data-table">
      <thead><tr><th>Rank</th><th>Title</th><th>Avg Rating</th></tr></thead>
      <tbody>
        <c:forEach var="row" items="${ratings}">
          <tr>
            <td>#<c:out value="${row[2]}"/></td>
            <td><c:out value="${row[0]}"/></td>
            <td>⭐ <c:out value="${row[1]}"/></td>
          </tr>
        </c:forEach>
        <c:if test="${empty ratings}">
          <tr><td colspan="3" style="color:var(--muted);text-align:center;">No reviews yet.</td></tr>
        </c:if>
      </tbody>
    </table>
  </div>

  <!-- ── 4. Churn Risk ─────────────────────────────────────────────────── -->
  <div class="section-title">⚠️ Churn-Risk Users — Inactive 30+ Days (MySQL)</div>
  <div style="overflow-x:auto;">
    <table class="data-table">
      <thead>
        <tr><th>ID</th><th>Name</th><th>Email</th><th>Country</th><th>Last Watch</th></tr>
      </thead>
      <tbody>
        <c:forEach var="row" items="${churnRisk}">
          <tr>
            <td><c:out value="${row[0]}"/></td>
            <td><c:out value="${row[1]}"/></td>
            <td><c:out value="${row[2]}"/></td>
            <td><c:out value="${row[3]}"/></td>
            <td style="color:var(--accent);"><c:out value="${row[4]}"/></td>
          </tr>
        </c:forEach>
        <c:if test="${empty churnRisk}">
          <tr><td colspan="5" style="color:var(--muted);text-align:center;">All users are active!</td></tr>
        </c:if>
      </tbody>
    </table>
  </div>

  <!-- ── 5. MongoDB Top 10 ─────────────────────────────────────────────── -->
  <div class="section-title">
    🍃 Top 10 by Completed Views
    <span class="mongo-badge">MongoDB</span>
  </div>
  <div style="overflow-x:auto;">
    <table class="data-table">
      <thead><tr><th>Content ID</th><th>Completed Views</th></tr></thead>
      <tbody>
        <c:forEach var="row" items="${mongoTop10}">
          <tr>
            <td><c:out value="${row[0]}"/></td>
            <td><c:out value="${row[1]}"/></td>
          </tr>
        </c:forEach>
      </tbody>
    </table>
  </div>

  <!-- ── 6. MongoDB Genre by Country ──────────────────────────────────── -->
  <div class="section-title">
    🌍 Genre Popularity by Country
    <span class="mongo-badge">MongoDB</span>
  </div>
  <div style="overflow-x:auto;">
    <table class="data-table">
      <thead><tr><th>Country</th><th>Genre</th><th>Views</th></tr></thead>
      <tbody>
        <c:forEach var="row" items="${genreCountry}">
          <tr>
            <td><c:out value="${row[0]}"/></td>
            <td><c:out value="${row[1]}"/></td>
            <td><c:out value="${row[2]}"/></td>
          </tr>
        </c:forEach>
      </tbody>
    </table>
  </div>

</div>
<script src="${pageContext.request.contextPath}/js/main.js"></script>
</body>
</html>
