<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html lang='zh-CN'>
<head>
<meta charset='utf-8'>
<title>My JSP 'add.jsp' starting page</title>

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
	<form class='yangmvc_form' method='post' action=''>
		<div>
			<label>作者</label> <input type='text' name='author' value=''></input>

		</div>
		<div>
			<label>添加时间</label> <input type='text' name='addtm' value=''></input>

		</div>
		<div>
			<label>朝代</label> <input type='text' name='chaodai' value=''></input>

		</div>
		<div>
			<label>名称</label> <input type='text' name='name' value=''></input>

		</div>
		<div>
			<label>简介</label> <input type='text' name='about' value=''></input>

		</div>
		<div>
			<label>类型</label> <input type='text' name='type' value=''></input>

		</div>
		<div>
			<label>文件名</label> <input type='text' name='file_name' value=''></input>

		</div>
		<div>
			<label>年代</label> <input type='text' name='tm_year' value=''></input>

		</div>
		<div>
			<label>分类</label> <input type='text' name='catalog_id' value=''></input>

		</div>
		<div>
			<label></label> <input type='submit' value='添加'></input>

		</div>
	</form>
</body>
</html>
