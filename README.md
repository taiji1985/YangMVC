#YangMVC

# 开发目的

@copyright 杨同峰 保留所有权利 

本文可以转载，但请保留版权信息。



SSH框架配置复杂、难用。个人认为这不是一个框架应该有的样子。框架应该使用简单、配置简单、代码简洁。于是参照Django的一些特性，编写了这个MVC+ORM框架。

#特性
1. 大量的默认约定，避免了大量的配置
2. 使用此框架开发效率会很高
3. 支持延迟加载技术的List
4. 和JSTL无缝兼容

#YangMVC的第零个例子-HelloWorld程序
```
public class IndexController extends Controller {
	public void index(){
		output("Hello　YangMVC");
	}
}
```
他的作用就是显示一句话。如图


![第零个例子的显示](http://img.blog.csdn.net/20160520185330567)


IndexController来处理应用的根目录下的请求。 index方法来处理这个目录下的默认请求。


#YangMVC第一个Demo
在org.demo包下建立此类：
```java
public class BookController extends Controller {
	public void index(){
		DBTool tool = Model.tool("book");
		LasyList list = tool.all().limit(0, 30);
		put("books", list);
		render();
	}
}
```
在WebRoot/view/book/下建立一个index.jsp
其中核心的代码为
```html
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
(此处省略一堆无关的HTML代码）
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
```

一个显示列表的网页就此搞定。访问应用目录下的book/目录即可显示出结果
![这里写图片描述](http://img.blog.csdn.net/20160520174109801)

说明：

这个BookController是一个控制器，它的每一个公共方法都对应一个网页（如果不想对应，你需要将其设为私有的）

Model和DBTool是整个ORM框架的核心。Model表示模型，它用来与数据库表相对应。在创建一个Model时，会指定对应的表名。 

这里和Hibernate不同，Hibernate需要预先生成所有数据库表的对应类， 而这个Model可以与任何表格关联，而不需要预先生成任何一个类。 这正是YangMVC中的ORM的优势所在。

```DBTool tool = Model.tool("book");```

程序中使用Model的静态方法tool获取一个DBTool对象，tool传入的参数book是数据库的表名。
这样DBTool就和book表建立了关联。 

```LasyList list = tool.all().limit(0, 30);```

伙计们快看，这是个LasyList，一个支持懒惰加载机制的列表。它是List类的子类，这也就是它为什么能在JSTL中使用foreach变量的原因。

首先我们调用了tool的all()方法，天哪，难道要加载book表的所有数据，兄弟不用害怕，在这个时候，它并没有进行任何数据的读写，指示记录了现在要访问book表的所有数据这一信息。 all()方法会返回一个LasyList对象。这么设计的原因是我们后面可以跟一连串的过滤方法。方便我们编程。我们可以写出这样的东西：
```
list = tool.all().gt("id", 12).lt("id", 33).eq("name","haha").like("author","王");
```
 这个例子相当于执行了如下SQL语句：
```java
 select * from book where id>12 and id<33 and name='haha' and author like '%王%'
```

在上面的例子中， all()返回的LasyList又调用了它的limit方法，这一步仍然没有真正访问数据库。
那么访问数据库从哪里开始呢？ 从你获取这个列表的一项时。

一个List，可以使用枚举的方法来访问
```java
for(Model m : list){
	
}
```
也可以使用get方法来访问。如
```
Model m = list.get(12)
```
在你访问具体它的一个元素（Model）时，数据库查询才会启动。而且也不是将所有数据放到内存中。比如你通过上面for的方法枚举时，其实它是通过ResultSet的next游标在移动，所以它很高效！也避免了无用的数据库操作。

```
put("book",list)
```
该方法将查询得到的book塞入request中，在jsp网页中就可以使用JSTL来使用它。因为它是一个List，所以用forEach去访问他。

Model 的一个对象对应于数据库表的一行（一条记录），Model是一个Map的子类！！！，所以在JSTL中，你可以使用
${ b.name } 的方式来访问名为b的Model 的name项。 它相当于
```
   Model m = ....
   m.get("name")
```
是不是很方便？？？ 真的是非常方便的。。

#第二个Demo
添加书籍页面
```java
	public void add(){
		DBTool tool = Model.tool("book");
		//处理提交数据
		if(isPost()){ //isPost
			Model m = tool.create(); //创建新的
			Log.d(m);
			paramToModel(m);
			tool.save(m);
			put("msg","添加成功");
		}

		//显示数据
		renderForm(tool.create());
	}
```
对应的/view/book/add.jsp (这是默认对应的模板地址）的核心内容
```html
  <div style="margin-left:100px">
  <h1>添加书籍 ${msg }</h1>
  ${book_form }
  </div>
```
![这里写图片描述](http://img.blog.csdn.net/20160520181929054)

上面的例子控制器其实是对应两个页面。 在收到Get请求的时候显示表单，在用户提交数据时，做插入操作，并显示表单。（我们当然可以把这两个页面写到两个不同的方法中）
我们还是使用Model.tool获取一个DBTool。 

先来看显示表单，就一句话
```
		renderForm(tool.create());
```
tool的create方法会返回一个Model对象，这个对象和book表相关联（因为tool和book表关联）。
并将这个Model传递给renderForm方法。这个方法会根据book表格的元数据自动创建一个表格。
哇偶！

那么这个Form插入到网页的什么位置呢？ 将 ${book_form } 放入网页中 即可。
	

如果来的是POST请求（使用isPost()方法来判断）
使用tool的create方法创建一个新的Model, 尽快还有其他创建Model对象的方式，但如果你希望插入，请尽量使用这种方式。
paramToModel(m) ,这个方法会自动查找表单中，名字与数据库字段名匹配的项，并自动赋值给Model的相应项。是不是很方便。。。

想起了Struts那悲催的功能定义。 泪奔。。。。 

随后直接调用tool的save方法将其保存到数据库中！OK了！万事大吉！

细心的小朋友会问： 数据库中的字段名都是英文的如name，为什么在网页上显示的是中文？？？
看看我的数据库表格定义
```sql
CREATE TABLE `book` (
  `id` int(11) NOT NULL auto_increment COMMENT '编号',
  `file_name` varchar(50) default NULL,
  `name` varchar(50) default NULL COMMENT '名称',
  `author` varchar(50) default NULL COMMENT '作者',
  `chaodai` varchar(50) default NULL COMMENT '朝代',
  `tm_year` varchar(50) default NULL COMMENT '年代',
  `about` longtext COMMENT '简介',
  `type` varchar(50) default NULL COMMENT '类型',
  `catalog_id` int(11) default NULL COMMENT '分类',
  PRIMARY KEY  (`id`),
  KEY `catalog` USING BTREE (`catalog_id`)
) ENGINE=InnoDB AUTO_INCREMENT=912 DEFAULT CHARSET=utf8;
```
真相大白与天下，我是通过给字段加注释实现的这一点。只要你将数据库表格加上注释，它就会自动获取注释并显示，对于没有注释的字段，则会显示字段名。如那个扎眼的file_name

好了，这几行代码就搞定了输入表单和表单的处理。

#第三个demo-编辑（自动创建的修改表单）
细心的朋友发现，我们是按照CRUD的逻辑来将的。下面是编辑网页。
```java
	public void edit() throws NullParamException{

		DBTool tool = Model.tool("book");
		//处理提交数据
		if(isPost()){ //isPost
			Model m = tool.get(paramInt("id"));
			Log.d(m);
			paramToModel(m);
			tool.save(m);
			put("msg","修改成功");
		}

		//显示数据
		Integer id = paramInt("id");
		checkNull("id", id);
		renderForm(tool.get(id));

	}
```
HTML页面放在/view/book/edit.jsp中，核心代码只是将add.jsp中的添加二字改为了"编辑“二字。
```
  <div style="margin-left:100px">
  <h1>编辑书籍 ${msg }</h1>
  ${book_form }
  </div>
```

这个代码长了一点， 有17行。对于用YangMVC的，已经算够长的了。它仍然是两个网页！！！
你可以吧显示表单的代码和处理表单的分到两个方法中写。

先看显示数据。 首先使用paramInt方法获取URL参数id，我们就是要编辑id指定的书籍。
调用checkNull来检查一下。 在我的开发生涯中，遇到各种参数检查，所以这个功能是必须有的，如果checkNull不过，就会抛出一个异常。 这样做的目的是不要让这种参数检查干扰我们正常的逻辑。这不就是异常之所以存在的意义么？

如果缺少这个参数，页面会提示说缺少这个参数。
下面使用tool.get(id)方法来获取一个Model（一条记录）。这个方法是根据表格的主键进行查询，返回的不是列表而是一个具体的Model对象。在这里我建议主键应当是整数、且是数据库自增的。
renderForm传入一个model，这个model中有数据，就会被显示出来。

就这样。编辑功能写好了。

有的朋友问，如果不想用默认的表单怎么办？ 那你自己写一个表单在你的模板里就是了。只不过，你可以先用这个方法吧表单生成出来，然后按你的意图修改就成了。这也节省大量时间啊。做过Form的请举手。

#第四个DEMO-删除
```java
	public void del(){
		Integer id = paramInt("id");
		Model.tool("book").del(id);
		jump("index");
		
		
	}
```
瞧瞧就这点代码了， 获取参数id，并调用tool的del方法删除。最后一句我们第一次见，就是跳转。跳转到同目录下的index这个默认页（显示的是书籍列表）








#配置
1. 新建一个Web
2.  Project（MyEclipse为例）
2. 将以下三个jar放到WebRoot/Web-INF下面
yangmvc-1.0.jar
fastjson-1.2.0.jar
mysql-connector-java-5.1.23-bin.jar
3. 在web.xml中（web-app标签内）加入
```
  <filter>
    <filter-name>yangmvc</filter-name>
    <filter-class>org.docshare.mvc.MVCFilter</filter-class>
    <init-param>
      <param-name>controller</param-name>
      <param-value>org.demo</param-value>
    </init-param>
    <init-param>
      <param-name>template</param-name>
      <param-value>/view</param-value>
    </init-param>
  </filter>
  
  <filter-mapping>
    <filter-name>yangmvc</filter-name>
    <url-pattern>/*</url-pattern>
  </filter-mapping>
  <context-param>
    <param-name>dbhost</param-name>
    <param-value>localhost</param-value>
  </context-param>
  <context-param>
    <param-name>dbusr</param-name>
    <param-value>root</param-value>
  </context-param>
  <context-param>
    <param-name>dbpwd</param-name>
    <param-value>123456</param-value>
  </context-param>
  <context-param>
    <param-name>dbname</param-name>
    <param-value>mvc_demo</param-value>
  </context-param>
  
```
所有需要配置的都在这里了。这里做个简要说明
**MVCFilter**是我们MVC框架的入口。（不管是啥MVC框架都免不了这个）
它有controller和template两个参数。
**controller** 是你控制器存放位置的包名。 比如这里是org.demo
**template**是你存放模板（视图）的地方。这个路径是相对于WebRoot即网站根目录的。
比如这里的配置(/view)是WebRoot下的view目录。 

**dbhost dbname dbusr dbpwd** 是数据库的 地址、数据库名、用户名和密码。目前这个MVC框架只支持MySQL，后续会添加其他数据库的支持。


**注意，模板目录（template参数所配置的值）以/开头，如/view。**
#控制器创建
控制器是一个Java类，类有若干方法。在YangMVC的设计中，控制器的每一个公共的方法都映射对应一个网页。这样一个Java类可以写很多的网页。 方便管理。（当然，你也可以在一个控制器中只写一个方法来支持网页，这没问题(⊙﹏⊙)b）

所有的控制器都要继承 org.docshare.mvc.Controller 这个类。充当控制器方法的方法应当是没有参数没有返回值的。如上面demo所示。
```
public class IndexController extends Controller {
	public void index(){
		output("Hello　YangMVC");
	}
}
```
这些控制器都要写在配置所制定的package中，或者子package中。如在上面的配置中
```
    <init-param>
      <param-name>controller</param-name>
      <param-value>org.demo</param-value>
    </init-param>
```
这个包为org.demo所有的控制器都要卸载这个包内。（你可以写到外面，但它不会管用O(∩_∩)O~）




# 路径映射
所谓路径映射就是要将 一个控制器(一个Java类）和一个网址建立关联。 用户访问某网址时，框架自动调用控制器的某个函数。

因为本框架设计思想希望配置尽可能少，所以这里的路径映射是通过命名关系的。

假设应用的根目录为 
http://localhost:8080/YangMVC/

如在org.demo下（这个目录可以在web.xml中配置，可见上一节）有一个BookController。
那么这个类的路径是  http://localhost:8080/YangMVC/book/ 
用户访问这个路径时，框架会调用BookController 的index方法。如果没有这个方法则会报错。

index方法用以处理某个路径下的默认网页（网站以斜杠结尾的都会调用某个类的index方法来处理）。

book这个地址，将第一个字母大写，后面追加Controller。于是
book （路径名）-> Book -> BookController（类名）
这就是路径和类名的默认关联。


在这个网站后加入方法名可以访问BookController的 任何一个公共方法。
如 http://localhost:8080/YangMVC/book/edit 与BookController的edit方法关联。

需要注意的是，如果你写的是 http://localhost:8080/YangMVC/book/edit/  (比上一个网站多了一个斜杠）， 则它对应的是 book.EditController下的index方法 而不是BookController下的edit方法。

#控制器方法
##输出方法
### output方法
```
	output("Hello　YangMVC");
```
这个方法输出一个文本到网页上（输出流中），并关闭输出流。因为它会关闭流，所以你不要调用它两次。你如果需要输出多次，以将内容放到StringBuffer中，然后统一输出。
###render方法
```
	public void paramDemo(){
		put("a", "sss");
		render("/testrd.jsp");
		
	}
```
这里的testrd.jsp是模板目录（/view)目录下的。 /view/testrd.jsp
这里的参数应该是相对于模板目录的相对路径。

	render方法使用参数制定的网页（一个包含JSTL的jsp文件），将其输出。可以通过put来制定参数。下面会详细讲。
	
### render()方法
	这个render方法是没有参数的，它会使用默认模板，如果这个模板不存在，就会提示错误。
```
	public void renderDemo(){
		request.setAttribute("a", "sss");
		render();
		
	}
```
在配置 controller 为org.demo , template为/view  这种情况下。
	org.demo.IndexController的renderDemo方法会对应/view/renderDemo.jsp
	之所以模板存在于模板根目录下，是因为这个IndexController是处理应用根目录的。他们有对应关系。
	
如果是org.demo.BookController,它对应 app根目录下的 /book/ 目录。
	它的add方法对应路径 /book/add
	如果应用名为hello,那么完成路径应该是  /hello/book/add 

#### outputJSON 方法
该方法将参数转化为JSON，并向网页输出。
```java
	public void jsonDemo(){
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("id", 12);
		map.put("name", "Yang MVC");
		map.put("addtm",new Date());
		
		outputJSON(map);
	}
```
	这个代码稍长，其实上面的所有都是生成一个Map，最后一句输出。outputJSON可以输出List，Map和任何Java对象。内部转换是使用fastjson实现的。

####  自动生成并输出一个表单
```
public void renderForm(Model m,String template,String postTo)
```
该函数会根据模型对应的表结构，自动生成一个表单，并将其内容放入 表格名_form 中，如book表会输出到 book_form 中。
在网页中，直接写 ${book_form}就可以将表单放下去。

template制定对应的模板文件，可以省略，省略后按照默认规则查找模板文件。
postTo设定 表单提交的网页，可以省略，默认是"",即当前网页（Controller）。

##获取参数的方法
1. param(String p)      获取参数p的值，以String类型返回
2. paramInt(String p) 获取参数p的值，以Int类型返回，如果不是整数，则会出现异常
3. public Model paramToModel(Model m)
	根据名称匹配的原则，将与模型中参数名相同的参数的值放入模型中。并返回该模型。
是收集表单数据到模型中的神器，手机后就可以直接进行数据库操作了。
4. paramWithDefault 获取参数，但同时带上默认值，如果没这个参数则返回默认值。
##检查方法
public void checkNull(String name,Object obj)  
检查obj是否为null，如果是抛出NullParamException异常。


#ORM框架

## Model与DBTool
Model 对象对应数据库的表格，它会与一个表格进行绑定。DBTool相当于是它的DAO类。
### 创建一个DBTool对象
```
		DBTool tool = Model.tool("book");
```
其中book是数据库表的名字。

### 创建一个空的Model
```
		DBTool tool = Model.tool("book");
		Model m = tool.create(); //创建新的

```
### 根据主键读取一个Model
```
			Model m = tool.get(12);

```
### 查询表中所有的行
```
		LasyList list = tool.all();
```
	all返回一个LasyList对象。这个对象在此事并没有真正进行数据库查询，只有在页面真正读取时才会读取数据库。这是它叫做Lasy的原因。此处借鉴了Django的实现机制。
###查询的limit语句
```
	LasyList list = tool.all().limit(30);
	list = tool.all().limit(10,30);
	
```
###查询的等式约束
```
	tool.all().eq("name","本草纲目")
```
###查询的不等式约束
```
	tool.all().gt("id",12) //id < 12
	tool.all().lt("id",33) //id <33
	tool.all().gte("id",12) //id>=12
	tool.all().lte("id",33) //id<=33
	tool.all().ne("id",33) //不相等
	
```
### 模糊查询
```
	tool.all().like("name","本草")
```
	查找所有名字中包含本草的书。返回一个LasyList
###排序
```
	tool.all().orderby("id",true);
```
	按照id的增序排列。 如果是false，则是降序。
	
	
###级联查询
  因为这些上面的过滤器函数全部都会返回一个LasyList对象， 所以可以采用级联的方式进行复杂查询。如：
  
```
list = tool.all().gt("id", 12).lt("id", 33).eq("name","haha").like("author","王");
```
 这个例子相当于执行了如下SQL语句：
```java
 select * from book where id>12 and id<33 and name='haha' and author like '%王%'
```

##Model的相关功能
model 是一个继承自Map&lt;String,Object&gt; 的类，所以对于
Model m;
你可以在网页中使用${m.name}的方式来访问它的name键对应的值。相当于m.get("name")
这种写法在JSTL中非常有用。让Model继承Map的初衷就在于此：方便在JSTL中使用。

大家也许注意到了LasyList是一个继承自List&lt;Model&gt; 的类.
这就使得不管是LasyList还是Model在JSTL中访问都极为的便利。

### 访问所有的键值（即DAO对象的所有属性)
```
	model.keySet();
```
###访问某一个属性的值
```
	model.get(key)
```
###设置某一个属性的值
```
	model.put(key,value)
```
