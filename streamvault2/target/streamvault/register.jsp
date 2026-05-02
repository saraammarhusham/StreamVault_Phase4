<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>StreamVault – Create Account</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<div class="auth-wrap">
  <div class="auth-box" style="max-width:520px;">
    <span class="logo">StreamVault</span>
    <h1>Create Account</h1>

    <c:if test="${not empty error}">
      <div class="alert alert-error"><c:out value="${error}"/></div>
    </c:if>

    <form action="${pageContext.request.contextPath}/register" method="post">

      <div class="form-group">
        <label for="fullName">Full Name</label>
        <input class="form-control" type="text" id="fullName" name="fullName"
               placeholder="Your full name" required>
      </div>

      <div class="form-group">
        <label for="email">Email Address</label>
        <input class="form-control" type="email" id="email" name="email"
               placeholder="you@example.com" required>
      </div>

      <div class="form-group">
        <label for="password">
          Password <span style="color:var(--muted);font-size:.8rem;">(min 8 characters)</span>
        </label>
        <input class="form-control" type="password" id="password" name="password"
               placeholder="At least 8 characters" required minlength="8">
        <span id="pw-strength" style="font-size:.82rem;margin-top:.3rem;display:block;"></span>
      </div>

      <div style="display:grid;grid-template-columns:1fr 1fr;gap:1rem;">
        <div class="form-group">
          <label for="country">Country</label>
          <input class="form-control" type="text" id="country" name="country"
                 placeholder="UAE" required>
        </div>
        <div class="form-group">
          <label for="dob">Date of Birth</label>
          <input class="form-control" type="date" id="dob" name="dob" required>
        </div>
      </div>

      <div class="form-group">
        <label for="planId">Subscription Plan</label>
        <c:choose>
          <c:when test="${not empty plans}">
            <select class="form-control" id="planId" name="planId" required>
              <option value="">— Choose a plan —</option>
              <c:forEach var="plan" items="${plans}">
                <option value="${plan[0]}">
                  <c:out value="${plan[1]}"/> – $<c:out value="${plan[2]}"/>/mo
                </option>
              </c:forEach>
            </select>
          </c:when>
          <c:otherwise>
            <div class="alert alert-error" style="margin-bottom:.6rem;font-size:.85rem;">
              ⚠️ <strong>Database not connected.</strong>
              Open <code>DatabaseConnection.java</code>, set <code>DB_PASSWORD</code>
              to your MySQL root password, then restart Tomcat.
              Make sure you ran <strong>streamvault_database.sql</strong> in MySQL Workbench first.
            </div>
            <select class="form-control" id="planId" name="planId" required>
              <option value="">— Choose a plan —</option>
              <option value="1">Basic – $9.99/mo</option>
              <option value="2">Standard – $15.99/mo</option>
              <option value="3">Premium – $19.99/mo</option>
            </select>
          </c:otherwise>
        </c:choose>
      </div>

      <button type="submit" class="btn btn-primary" style="margin-top:.5rem;">
        Create Account
      </button>
    </form>

    <p style="text-align:center;margin-top:1.5rem;font-size:.9rem;color:var(--muted);">
      Already have an account?
      <a href="${pageContext.request.contextPath}/login">Sign in</a>
    </p>
  </div>
</div>
<script src="${pageContext.request.contextPath}/js/main.js"></script>
</body>
</html>
