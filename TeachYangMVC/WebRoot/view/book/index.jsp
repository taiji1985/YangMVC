<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang='zh-CN'>
<head>
<meta charset='utf-8'>

    <title>My JSP 'book.jsp' starting page</title>
    
	<meta http-equiv="pragma" content="no-cache">
	<meta http-equiv="cache-control" content="no-cache">
	<meta http-equiv="expires" content="0">    
	<meta http-equiv="keywords" content="keyword1,keyword2,keyword3">
	<meta http-equiv="description" content="This is my page">
	<!--
	<link rel="stylesheet" type="text/css" href="styles.css">
	-->

  </head>
  
  <body>
    ${msg } <br> 
    <table border=1>
    <c:forEach var='b' items='${books}'>
    	<tr>
    		<td>${b.name }&nbsp;</td>
    		<td>${b.author }&nbsp;</td>
    		<td>${b.chaodai }&nbsp;</td>
    	</tr>
    </c:forEach>
    </table>
    ${page_data }
  </body>
</html>
