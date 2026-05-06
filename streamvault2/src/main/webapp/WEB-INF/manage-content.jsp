<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html>
<head>
  <title>Manage Content</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>

<h1>Manage Content</h1>

<p>
  <a href="${pageContext.request.contextPath}/home">Back to Browse</a>
</p>

<h2>Add New Content</h2>

<form method="post" action="#">
  <input type="text" name="title" placeholder="Title">
  <input type="text" name="contentType" placeholder="Movie / Series / Documentary">
  <input type="text" name="language" placeholder="Language">
  <button type="button">Add Content</button>
</form>

<hr>

<h2>All Content</h2>

<table border="1" cellpadding="8">
  <tr>
    <th>ID</th>
    <th>Title</th>
    <th>Type</th>
    <th>Year</th>
    <th>Language</th>
    <th>Studio</th>
    <th>Actions</th>
  </tr>

  <c:forEach var="item" items="${contentList}">
    <tr>
      <td>${item.contentId}</td>
      <td>${item.title}</td>
      <td>${item.contentType}</td>
      <td>${item.releaseYear}</td>
      <td>${item.language}</td>
      <td>${item.studioName}</td>
      <td>
        <button>Edit</button>
        <button>Delete</button>
      </td>
    </tr>
  </c:forEach>
</table>

</body>
</html>
