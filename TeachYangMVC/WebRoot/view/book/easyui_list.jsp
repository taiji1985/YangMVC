<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Basic CRUD Application - jQuery EasyUI CRUD Demo</title>
    <link rel="stylesheet" type="text/css" href="http://www.jeasyui.com/easyui/themes/default/easyui.css">
    <link rel="stylesheet" type="text/css" href="http://www.jeasyui.com/easyui/themes/icon.css">
    <link rel="stylesheet" type="text/css" href="http://www.jeasyui.com/easyui/themes/color.css">
    <link rel="stylesheet" type="text/css" href="http://www.jeasyui.com/easyui/demo/demo.css">
    <script type="text/javascript" src="http://code.jquery.com/jquery-1.6.min.js"></script>
    <script type="text/javascript" src="http://www.jeasyui.com/easyui/jquery.easyui.min.js"></script>
</head>
<body>
    <h2>Basic CRUD Application</h2>
    <p>Click the buttons on datagrid toolbar to do crud actions.</p>
    
    <table id="dg" title="My Users" class="easyui-datagrid" style="width:700px;height:250px"
            url="easyui_data"
            toolbar="#toolbar" pagination="true"
            sortName="id" sortOrder="asc"
            rownumbers="true" fitColumns="true" singleSelect="true">
        <thead>
            <tr>
                <th field="id" width="50" sortable="true">编号</th>
                <th field="name" width="50" sortable="true">书名</th>
                <th field="author" width="50">作者</th>
                <th field="chaodai" width="50" sortable="true">朝代</th>
            </tr>
        </thead>
    </table>
    <div id="toolbar">
        <a href="javascript:void(0)" class="easyui-linkbutton" iconCls="icon-add" plain="true" onclick="newUser()">添加书籍</a>
        <a href="javascript:void(0)" class="easyui-linkbutton" iconCls="icon-edit" plain="true" onclick="editUser()">修改书籍</a>
        <a href="javascript:void(0)" class="easyui-linkbutton" iconCls="icon-remove" plain="true" onclick="destroyUser()">删除书籍</a>
    </div>
    
    <div id="dlg" class="easyui-dialog" style="width:600px"
            closed="true" buttons="#dlg-buttons">
        <form id="fm" method="post" novalidate style="margin:0;padding:20px 50px">

            <div style="margin-bottom:20px;font-size:14px;border-bottom:1px solid #ccc">书籍信息</div>
            <div style="margin-bottom:10px">
                <input name="name" class="easyui-textbox" required="true" label="书名:" style="width:80%">
            </div>
            <div style="margin-bottom:10px">
                <input name="author" class="easyui-textbox" required="true" label="作者:" style="width:80%">
            </div>
            <div style="margin-bottom:10px">
                <input name="chaodai" class="easyui-textbox" required="true" label="朝代:" style="width:80%">
            </div>
           
        </form>
    </div>
    <div id="dlg-buttons">
        <a href="javascript:void(0)" class="easyui-linkbutton c6" iconCls="icon-ok" onclick="saveUser()" style="width:90px">Save</a>
        <a href="javascript:void(0)" class="easyui-linkbutton" iconCls="icon-cancel" onclick="javascript:$('#dlg').dialog('close')" style="width:90px">Cancel</a>
    </div>
    <script type="text/javascript">
        var url;
        function newUser(){
            $('#dlg').dialog('open').dialog('center').dialog('setTitle','添加书籍');
            $('#fm').form('clear');
            url = 'easyui_add'; //是相对路径，为什么那么简单？ 当前的这个界面和提供数据的是处于同一目录
        }
        function editUser(){
            var row = $('#dg').datagrid('getSelected'); //获取选中行
            if (row){
                $('#dlg').dialog('open').dialog('center').dialog('setTitle','修改书籍'); //创建了对话框
                $('#fm').form('load',row); //加载了选中行的数据
                url = 'easyui_edit?id='+row.id; //是为了标志选中了哪一行，id为数据中的主键
            }
        }
        function saveUser(){
            $('#fm').form('submit',{
                url: url,
                onSubmit: function(){
                    return $(this).form('validate');
                },
                success: function(result){
                    var result = eval('('+result+')');
                    if (result.succ != "ok"){ //如果succ不是ok就输出错误
                        $.messager.show({
                            title: 'Error',
                            msg: result.msg
                        });
                    } else { //成功以后，就关闭对话框，并且刷新datagrid
                        $('#dlg').dialog('close');        // close the dialog
                        $('#dg').datagrid('reload');    // reload the user data
                        $.messager.show({
                            title: '成功',
                            msg: result.msg
                        });
                    }
                }
            });
        }
        function destroyUser(){
            var row = $('#dg').datagrid('getSelected');
            if (row){
                $.messager.confirm('确认','你是否要删除此书籍?',function(r){
                    if (r){
                        $.post('easyui_del',{id:row.id},function(result){
                            if (result.succ == 'ok'){
                                $('#dg').datagrid('reload');    // reload the user data
                                $.messager.show({    // show error message
                                    title: '成功',
                                    msg: result.msg
                                });
                            } else {
                                $.messager.show({    // show error message
                                    title: '错误',
                                    msg: result.msg
                                });
                            }
                        },'json');
                    }
                });
            }
        }
    </script>
</body>
</html>