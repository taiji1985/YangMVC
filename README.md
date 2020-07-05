[toc]

# YangMVC
# 下载地址

https://git.oschina.net/yangtf/YangMVC/attach_files

请在此页下载版本号最高的版本


# 更新日志

https://gitee.com/yangtf/YangMVC/blob/master/YangMVC/update.md

# 简介

YangMVC是一个高效的、轻量级MVC和ORM框架。 你只需要想你的项目中丢入一个jar包，即可完成绝大多数Web网站的开发。

YangMVC的网址和控制器类之间采用了默认命名约定的方法，减少了配置。如IndexController对应网站根目录， BookController对应/book目录。

YangMVC提供的 ORM即为易用。

LasyList list = Model.tool("book").all().gt("id",12).lt("id,33);

这句话相当于sql语句 select * from book where id>12 and id<33

它得到了一个线性表（数组），可以直接在JSTL和FreeMarker中使用循环来枚举。。

如果有复杂sql需要些，你可以直接写sql，它同样会返回LasyList，而不是ResultSet。


ORM框架不需要预先生成任何POJO类，所有表都映射为内置的Model类，即一个Model对象对应数据库表中的一行。。。 Model可以适应任何的表和视图。

如果你确实需要将数据库中的数据转化为一个特定的java对象（POJO类),那么Model提供方法可以直接转化为你需要的类。用起来也非常方便。

使用这个框架，你可以以即为高效的速度推进你的项目，而不需要为了写一个功能，去改java文件，改俩个xml文件（用SSH的同学可以冒一个泡，是不是这样）

数据库的设计在初期常有不完善的地方，如果使用Hibernate这种框架，那么就需要在修改数据库后重新生成Java类。而对应的DAO类也要对应修改，这简直是噩梦。。。

iBatis是你喜欢的，但它需要sql语句。YangMVC不用。。。
# 应用场景
## 1 App 的服务端程序 或者Vue+后端架构的后端程序
![](images/bootdemo.jpg)
这种架构下，你一般只需要输出JSON。你可以创建一个java工程，拖入一个yangmvc-xxx-boot.jar, 新建一个控制器。直接运行！
```java
public class IndexController extends Controller{
	public void index(){
		//T("book") 等价于Model.tool
		//L("book") 等价于 Model.tool("book").all()
		LasyList list = L("book");
		outputJSON(list);
	}
	public void add(){
		Model book = T("book").create();
		paramToModel(book); //自动收集 参数到book对象， 如参数height会保存到book的height属性中。
		book.save();
		output("ok");
	}
	public void del(){
		int id = paramInt("id"，-1); //带默认值，自动转换类型
		if(id<0) {
			output("fail");
			return;
		}
		T("book").del(id); //便捷的主键删除
		output("ok");
	}
}
```
访问http://127.0.0.1:1985/ 可以获得book表对应的json数据。

http://127.0.0.1:1985/add 添加数据对应的接口地址

http://127.0.0.1:1985/del?id=12 删除数据对应的接口地址

## 2 开发Web项目

整个web项目，你只需要手工引入一个jar包就是yangmvc-版本号.jar
配置上，只需要在web.xml中加入数据库地址等必须的配置信息即可。

# 交流群

您可以加交流群 QQ 753780493 


# 开始使用YangMVC


首先您需要配置开发环境。 您需要一个JDK1.7以上的版本。且需要一个Eclipse或MyEclipse。

如果你只有Eclipse，请看 [这个教程](https://gitee.com/yangtf/YangMVC/wikis/A01-%E9%85%8D%E7%BD%AE-boot%E7%89%88) 。 

如果使用MyEclipse请看  [A01的配置](https://gitee.com/yangtf/YangMVC/wikis/A01-%E9%85%8D%E7%BD%AE)



# 更新日志

## 2018-6-5
今后版本号以生成时间命名： 
yangmvc-2018-6-5.jar 
boot版本为 yangmvc-boot-2018-6-5.jar

添加了生成POJO类的功能，使用方法如下


请输入数据库信息，如与中括号中相同，可以直接打回车请输入服务器域名或ip默认为: [localhost]: 

执行命令 

java -jar yangmvc-2018-6-5.jar 

请输入数据库名默认为: [mvc_demo]:  

请输入端口号默认为: [3306]: 

请输入密码默认为: [123456]: 

请输入用户名默认为: [root]: 

....

请输入需要生成的类的包名:org.yang

随后就会将代码生成到您输入的报名对应的目录下，

如输入org.yang,则代码生成到src/org/yang 中。



## V2.3.6 

童鞋发现了一个bug，就是tomcat6.0 使用YangMVC 无法上传文件，经测试得知原因为， YangMVC在上传时使用了高版本的Servlet-api,而Tomcat6 不支持，所以。。。去掉了对高版本api的使用。   使其在独立的tomcat6.0上运行无任何问题。。。 

## V2.3.5 
1. 允许使用关键字作为表名和字段名
2. 在Model中添加 getStr /getInt / getLong这些便捷的方法不需要自己强转了。

## V2.0 更新内容
  1. 增加了gzip传输功能
  2. 修正了已知错误


# 完整的文档请看

https://gitee.com/yangtf/YangMVC/wikis/pages

# javadoc 地址

http://yangtf.gitee.io/yangmvc/YangMVC/doc/index.html

# 最核心的几个类的Javadoc

基本上来说，只要掌握这三个核心的类，就可以自如使用这个框架。

控制器Controller类

http://yangtf.gitee.io/yangmvc/YangMVC/doc/org/docshare/mvc/Controller.html

DBTool类
http://yangtf.gitee.io/yangmvc/YangMVC/doc/org/docshare/orm/DBTool.html

LasyList类
http://yangtf.gitee.io/yangmvc/YangMVC/doc/index.html


