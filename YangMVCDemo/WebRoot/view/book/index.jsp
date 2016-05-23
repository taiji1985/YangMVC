<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%
String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <head>
    <base href="<%=basePath%>">
    <title>My JSP 'index.jsp' starting page</title>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta http-equiv="pragma" content="no-cache">
	<meta http-equiv="cache-control" content="no-cache">
	<meta http-equiv="expires" content="0">    
	<meta http-equiv="keywords" content="keyword1,keyword2,keyword3">
	<meta http-equiv="description" content="This is my page">

	<link href="http://libs.baidu.com/bootstrap/3.0.3/css/bootstrap.min.css" rel="stylesheet">
	<script src="http://libs.baidu.com/jquery/2.0.0/jquery.min.js"></script>
	<script src="http://libs.baidu.com/bootstrap/3.0.3/js/bootstrap.min.js"></script>
	<link href="view/book/mvc.css" rel="stylesheet">

  </head>
  
  <body>
  <h1>books 
  [<a href='book/add'>添加书籍</a>]
  </h1>
<table class="table table-bordered">
	<c:forEach var="b" items="${books }">
	<tr>
 		<td>${b.id }</td>
		<td>${b.name }</td>
		<td>${b.author }</td>
		<td>${b.chaodai }</td>
		<td>${b.tm_year }</td>
		<td>
			<a href='book/edit?id=${b.id}'>编辑</a>
			<a href='book/del?id=${b.id}'>删除</a>
		
		</td>
	</tr>
	</c:forEach>
</table>

${page_data }
 
  </body>
</html>
