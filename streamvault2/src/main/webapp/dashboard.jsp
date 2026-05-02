<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>StreamVault – My Dashboard</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>

<nav>
  <span class="nav-brand">StreamVault</span>
  <div class="nav-links">
    <a href="${pageContext.request.contextPath}/home">Browse</a>
    <a href="${pageContext.request.contextPath}/dashboard">Dashboard</a>
    <c:if test="${sessionScope.role == 'admin'}">
      <a href="${pageContext.request.contextPath}/admin-analytics">Analytics</a>
    </c:if>
    <a href="${pageContext.request.contextPath}/logout" class="btn-logout">Sign Out</a>
  </div>
</nav>

<div class="container">
  <div class="page-header">
    <h1>My Dashboard</h1>
    <p>
      Hello, <strong><c:out value="${sessionScope.user.fullName}"/></strong>
      &nbsp;·&nbsp; <c:out value="${sessionScope.user.email}"/>
    </p>
  </div>

  <div class="dash-grid">

    <!-- ── Active Plan ───────────────────────────────────────────────── -->
    <div class="dash-card">
      <h3>📋 Current Plan</h3>
      <c:choose>
        <c:when test="${not empty subscription}">
          <div class="plan-name"><c:out value="${subscription[0]}"/></div>
          <div class="plan-price">
            $<c:out value="${subscription[1]}"/> / month
            <span class="plan-badge"><c:out value="${subscription[3]}"/></span>
          </div>
          <div style="margin-top:1rem;font-size:.88rem;color:var(--muted);">
            <div>Started: <c:out value="${subscription[4]}"/></div>
            <div>Renews: <c:out value="${subscription[5]}"/></div>
            <div style="margin-top:.5rem;"><c:out value="${subscription[2]}"/></div>
          </div>
        </c:when>
        <c:otherwise>
          <p style="color:var(--muted);">No active subscription found.</p>
        </c:otherwise>
      </c:choose>
    </div>

    <!-- ── Account Info ──────────────────────────────────────────────── -->
    <div class="dash-card">
      <h3>👤 Account</h3>
      <table style="width:100%;font-size:.9rem;border-collapse:collapse;">
        <tr>
          <td style="color:var(--muted);padding:.4rem 0;width:40%;">Name</td>
          <td><c:out value="${sessionScope.user.fullName}"/></td>
        </tr>
        <tr>
          <td style="color:var(--muted);padding:.4rem 0;">Email</td>
          <td><c:out value="${sessionScope.user.email}"/></td>
        </tr>
        <tr>
          <td style="color:var(--muted);padding:.4rem 0;">Country</td>
          <td><c:out value="${sessionScope.user.country}"/></td>
        </tr>
        <tr>
          <td style="color:var(--muted);padding:.4rem 0;">Role</td>
          <td><c:out value="${sessionScope.user.role}"/></td>
        </tr>
      </table>
    </div>

  </div>

  <!-- ── Payment History ───────────────────────────────────────────────── -->
  <h2 style="margin:2rem 0 1rem;">💳 Billing History</h2>
  <c:choose>
    <c:when test="${empty payments}">
      <p style="color:var(--muted);">No payment records found.</p>
    </c:when>
    <c:otherwise>
      <div style="overflow-x:auto;">
        <table class="data-table">
          <thead>
            <tr>
              <th>#</th><th>Date</th><th>Plan</th><th>Amount</th><th>Status</th>
            </tr>
          </thead>
          <tbody>
            <c:forEach var="p" items="${payments}">
              <tr>
                <td>#<c:out value="${p[0]}"/></td>
                <td><c:out value="${p[2]}"/></td>
                <td><c:out value="${p[4]}"/></td>
                <td>$<c:out value="${p[1]}"/></td>
                <td>
                  <span class="status-<c:out value='${p[3]}'/>">
                    <c:out value="${p[3]}"/>
                  </span>
                </td>
              </tr>
            </c:forEach>
          </tbody>
        </table>
      </div>
    </c:otherwise>
  </c:choose>

  <!-- ── Watch History ─────────────────────────────────────────────────── -->
  <h2 style="margin:2rem 0 1rem;">▶ Watch History (Last 30 Days)</h2>
  <c:choose>
    <c:when test="${empty watchHistory}">
      <p style="color:var(--muted);">No recent watch activity. Start browsing!</p>
      <a href="${pageContext.request.contextPath}/home"
         class="btn btn-primary" style="display:inline-block;margin-top:1rem;width:auto;padding:.5rem 1.5rem;">
        Browse Content
      </a>
    </c:when>
    <c:otherwise>
      <div style="overflow-x:auto;">
        <table class="data-table">
          <thead>
            <tr>
              <th>Title</th><th>Type</th><th>Date Watched</th>
              <th>Progress</th><th>Done?</th><th>Device</th>
            </tr>
          </thead>
          <tbody>
            <c:forEach var="wh" items="${watchHistory}">
              <tr>
                <td>
                  <a href="${pageContext.request.contextPath}/content?id=<c:out value='${wh[0]}'/>">
                    <c:out value="${wh[1]}"/>
                  </a>
                </td>
                <td><c:out value="${wh[2]}"/></td>
                <td><c:out value="${wh[3]}"/></td>
                <td>
                  <div style="display:flex;align-items:center;gap:.5rem;">
                    <div class="progress-bar-wrap">
                      <div class="progress-bar" style="width:<c:out value='${wh[4]}'/>%;"></div>
                    </div>
                    <span style="font-size:.82rem;"><c:out value="${wh[4]}"/>%</span>
                  </div>
                </td>
                <td><c:out value="${wh[5]}"/></td>
                <td><c:out value="${wh[6]}"/></td>
              </tr>
            </c:forEach>
          </tbody>
        </table>
      </div>
    </c:otherwise>
  </c:choose>

</div>
<script src="${pageContext.request.contextPath}/js/main.js"></script>
</body>
</html>
