#YangMVC

# ����Ŀ��

@copyright ��ͬ�� ��������Ȩ�� 

���Ŀ���ת�أ����뱣����Ȩ��Ϣ��



SSH������ø��ӡ����á�������Ϊ�ⲻ��һ�����Ӧ���е����ӡ����Ӧ��ʹ�ü򵥡����ü򵥡������ࡣ���ǲ���Django��һЩ���ԣ���д�����MVC+ORM��ܡ�

#����
1. ������Ĭ��Լ���������˴���������
2. ʹ�ô˿�ܿ���Ч�ʻ�ܸ�
3. ֧���ӳټ��ؼ�����List
4. ��JSTL�޷����

#YangMVC�ĵ��������-HelloWorld����
```
public class IndexController extends Controller {
	public void index(){
		output("Hello��YangMVC");
	}
}
```
�������þ�����ʾһ�仰����ͼ


![��������ӵ���ʾ](http://img.blog.csdn.net/20160520185330567)


IndexController������Ӧ�õĸ�Ŀ¼�µ����� index�������������Ŀ¼�µ�Ĭ������


#YangMVC��һ��Demo
��org.demo���½������ࣺ
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
��WebRoot/view/book/�½���һ��index.jsp
���к��ĵĴ���Ϊ
```html
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
(�˴�ʡ��һ���޹ص�HTML���룩
<table class="table table-bordered">
	<c:forEach var="b" items="${books }">
	<tr>
 		<td>${b.id }</td>
		<td>${b.name }</td>
		<td>${b.author }</td>
		<td>${b.chaodai }</td>
		<td>${b.tm_year }</td>
		<td>
			<a href='book/edit?id=${b.id}'>�༭</a>
			<a href='book/del?id=${b.id}'>ɾ��</a>
		
		</td>
	</tr>
	</c:forEach>
</table>
```

һ����ʾ�б����ҳ�ʹ˸㶨������Ӧ��Ŀ¼�µ�book/Ŀ¼������ʾ�����
![����дͼƬ����](http://img.blog.csdn.net/20160520174109801)

˵����

���BookController��һ��������������ÿһ��������������Ӧһ����ҳ����������Ӧ������Ҫ������Ϊ˽�еģ�

Model��DBTool������ORM��ܵĺ��ġ�Model��ʾģ�ͣ������������ݿ�����Ӧ���ڴ���һ��Modelʱ����ָ����Ӧ�ı����� 

�����Hibernate��ͬ��Hibernate��ҪԤ�������������ݿ��Ķ�Ӧ�࣬ �����Model�������κα�������������ҪԤ�������κ�һ���ࡣ ������YangMVC�е�ORM���������ڡ�

```DBTool tool = Model.tool("book");```

������ʹ��Model�ľ�̬����tool��ȡһ��DBTool����tool����Ĳ���book�����ݿ�ı�����
����DBTool�ͺ�book�����˹����� 

```LasyList list = tool.all().limit(0, 30);```

����ǿ쿴�����Ǹ�LasyList��һ��֧��������ػ��Ƶ��б�����List������࣬��Ҳ������Ϊʲô����JSTL��ʹ��foreach������ԭ��

�������ǵ�����tool��all()���������ģ��ѵ�Ҫ����book����������ݣ��ֵܲ��ú��£������ʱ������û�н����κ����ݵĶ�д��ָʾ��¼������Ҫ����book�������������һ��Ϣ�� all()�����᷵��һ��LasyList������ô��Ƶ�ԭ�������Ǻ�����Ը�һ�����Ĺ��˷������������Ǳ�̡����ǿ���д�������Ķ�����
```
list = tool.all().gt("id", 12).lt("id", 33).eq("name","haha").like("author","��");
```
 ��������൱��ִ��������SQL��䣺
```java
 select * from book where id>12 and id<33 and name='haha' and author like '%��%'
```

������������У� all()���ص�LasyList�ֵ���������limit��������һ����Ȼû�������������ݿ⡣
��ô�������ݿ�����￪ʼ�أ� �����ȡ����б��һ��ʱ��

һ��List������ʹ��ö�ٵķ���������
```java
for(Model m : list){
	
}
```
Ҳ����ʹ��get���������ʡ���
```
Model m = list.get(12)
```
������ʾ�������һ��Ԫ�أ�Model��ʱ�����ݿ��ѯ�Ż�����������Ҳ���ǽ��������ݷŵ��ڴ��С�������ͨ������for�ķ���ö��ʱ����ʵ����ͨ��ResultSet��next�α����ƶ����������ܸ�Ч��Ҳ���������õ����ݿ������

```
put("book",list)
```
�÷�������ѯ�õ���book����request�У���jsp��ҳ�оͿ���ʹ��JSTL��ʹ��������Ϊ����һ��List��������forEachȥ��������

Model ��һ�������Ӧ�����ݿ���һ�У�һ����¼����Model��һ��Map�����࣡������������JSTL�У������ʹ��
${ b.name } �ķ�ʽ��������Ϊb��Model ��name� ���൱��
```
   Model m = ....
   m.get("name")
```
�ǲ��Ǻܷ��㣿���� ����Ƿǳ�����ġ���

#�ڶ���Demo
����鼮ҳ��
```java
	public void add(){
		DBTool tool = Model.tool("book");
		//�����ύ����
		if(isPost()){ //isPost
			Model m = tool.create(); //�����µ�
			Log.d(m);
			paramToModel(m);
			tool.save(m);
			put("msg","��ӳɹ�");
		}

		//��ʾ����
		renderForm(tool.create());
	}
```
��Ӧ��/view/book/add.jsp (����Ĭ�϶�Ӧ��ģ���ַ���ĺ�������
```html
  <div style="margin-left:100px">
  <h1>����鼮 ${msg }</h1>
  ${book_form }
  </div>
```
![����дͼƬ����](http://img.blog.csdn.net/20160520181929054)

��������ӿ�������ʵ�Ƕ�Ӧ����ҳ�档 ���յ�Get�����ʱ����ʾ�������û��ύ����ʱ�����������������ʾ���������ǵ�Ȼ���԰�������ҳ��д��������ͬ�ķ����У�
���ǻ���ʹ��Model.tool��ȡһ��DBTool�� 

��������ʾ������һ�仰
```
		renderForm(tool.create());
```
tool��create�����᷵��һ��Model������������book�����������Ϊtool��book���������
�������Model���ݸ�renderForm������������������book����Ԫ�����Զ�����һ�����
��ż��

��ô���Form���뵽��ҳ��ʲôλ���أ� �� ${book_form } ������ҳ�� ���ɡ�
	

���������POST����ʹ��isPost()�������жϣ�
ʹ��tool��create��������һ���µ�Model, ���컹����������Model����ķ�ʽ���������ϣ�����룬�뾡��ʹ�����ַ�ʽ��
paramToModel(m) ,����������Զ����ұ��У����������ݿ��ֶ���ƥ�������Զ���ֵ��Model����Ӧ��ǲ��Ǻܷ��㡣����

������Struts�Ǳ��ߵĹ��ܶ��塣 �ᱼ�������� 

���ֱ�ӵ���tool��save�������䱣�浽���ݿ��У�OK�ˣ����´󼪣�

ϸ�ĵ�С���ѻ��ʣ� ���ݿ��е��ֶ�������Ӣ�ĵ���name��Ϊʲô����ҳ����ʾ�������ģ�����
�����ҵ����ݿ�����
```sql
CREATE TABLE `book` (
  `id` int(11) NOT NULL auto_increment COMMENT '���',
  `file_name` varchar(50) default NULL,
  `name` varchar(50) default NULL COMMENT '����',
  `author` varchar(50) default NULL COMMENT '����',
  `chaodai` varchar(50) default NULL COMMENT '����',
  `tm_year` varchar(50) default NULL COMMENT '���',
  `about` longtext COMMENT '���',
  `type` varchar(50) default NULL COMMENT '����',
  `catalog_id` int(11) default NULL COMMENT '����',
  PRIMARY KEY  (`id`),
  KEY `catalog` USING BTREE (`catalog_id`)
) ENGINE=InnoDB AUTO_INCREMENT=912 DEFAULT CHARSET=utf8;
```
�����������£�����ͨ�����ֶμ�ע��ʵ�ֵ���һ�㡣ֻҪ�㽫���ݿ������ע�ͣ����ͻ��Զ���ȡע�Ͳ���ʾ������û��ע�͵��ֶΣ������ʾ�ֶ��������Ǹ����۵�file_name

���ˣ��⼸�д���͸㶨��������ͱ��Ĵ���

#������demo-�༭���Զ��������޸ı���
ϸ�ĵ����ѷ��֣������ǰ���CRUD���߼������ġ������Ǳ༭��ҳ��
```java
	public void edit() throws NullParamException{

		DBTool tool = Model.tool("book");
		//�����ύ����
		if(isPost()){ //isPost
			Model m = tool.get(paramInt("id"));
			Log.d(m);
			paramToModel(m);
			tool.save(m);
			put("msg","�޸ĳɹ�");
		}

		//��ʾ����
		Integer id = paramInt("id");
		checkNull("id", id);
		renderForm(tool.get(id));

	}
```
HTMLҳ�����/view/book/edit.jsp�У����Ĵ���ֻ�ǽ�add.jsp�е���Ӷ��ָ�Ϊ��"�༭�����֡�
```
  <div style="margin-left:100px">
  <h1>�༭�鼮 ${msg }</h1>
  ${book_form }
  </div>
```

������볤��һ�㣬 ��17�С�������YangMVC�ģ��Ѿ��㹻�����ˡ�����Ȼ��������ҳ������
����԰���ʾ���Ĵ���ʹ�����ķֵ�����������д��

�ȿ���ʾ���ݡ� ����ʹ��paramInt������ȡURL����id�����Ǿ���Ҫ�༭idָ�����鼮��
����checkNull�����һ�¡� ���ҵĿ��������У��������ֲ�����飬������������Ǳ����еģ����checkNull�������ͻ��׳�һ���쳣�� ��������Ŀ���ǲ�Ҫ�����ֲ��������������������߼����ⲻ�����쳣֮���Դ��ڵ�����ô��

���ȱ�����������ҳ�����ʾ˵ȱ�����������
����ʹ��tool.get(id)��������ȡһ��Model��һ����¼������������Ǹ��ݱ����������в�ѯ�����صĲ����б����һ�������Model�����������ҽ�������Ӧ�����������������ݿ������ġ�
renderForm����һ��model�����model�������ݣ��ͻᱻ��ʾ������

���������༭����д���ˡ�

�е������ʣ����������Ĭ�ϵı���ô�죿 �����Լ�дһ���������ģ��������ˡ�ֻ�����������������������ɱ����ɳ�����Ȼ�������ͼ�޸ľͳ��ˡ���Ҳ��ʡ����ʱ�䰡������Form������֡�

#���ĸ�DEMO-ɾ��
```java
	public void del(){
		Integer id = paramInt("id");
		Model.tool("book").del(id);
		jump("index");
		
		
	}
```
���ƾ��������ˣ� ��ȡ����id��������tool��del����ɾ�������һ�����ǵ�һ�μ���������ת����ת��ͬĿ¼�µ�index���Ĭ��ҳ����ʾ�����鼮�б�








#����
1. �½�һ��Web
2.  Project��MyEclipseΪ����
2. ����������jar�ŵ�WebRoot/Web-INF����
yangmvc-1.0.jar
fastjson-1.2.0.jar
mysql-connector-java-5.1.23-bin.jar
3. ��web.xml�У�web-app��ǩ�ڣ�����
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
������Ҫ���õĶ��������ˡ�����������Ҫ˵��
**MVCFilter**������MVC��ܵ���ڡ���������ɶMVC��ܶ��ⲻ�������
����controller��template����������
**controller** ������������λ�õİ����� ����������org.demo
**template**������ģ�壨��ͼ���ĵط������·���������WebRoot����վ��Ŀ¼�ġ�
�������������(/view)��WebRoot�µ�viewĿ¼�� 

**dbhost dbname dbusr dbpwd** �����ݿ�� ��ַ�����ݿ������û��������롣Ŀǰ���MVC���ֻ֧��MySQL������������������ݿ��֧�֡�


**ע�⣬ģ��Ŀ¼��template���������õ�ֵ����/��ͷ����/view��**
#����������
��������һ��Java�࣬�������ɷ�������YangMVC������У���������ÿһ�������ķ�����ӳ���Ӧһ����ҳ������һ��Java�����д�ܶ����ҳ�� �����������Ȼ����Ҳ������һ����������ֻдһ��������֧����ҳ����û����(�ѩn��)b��

���еĿ�������Ҫ�̳� org.docshare.mvc.Controller ����ࡣ�䵱�����������ķ���Ӧ����û�в���û�з���ֵ�ġ�������demo��ʾ��
```
public class IndexController extends Controller {
	public void index(){
		output("Hello��YangMVC");
	}
}
```
��Щ��������Ҫд���������ƶ���package�У�������package�С����������������
```
    <init-param>
      <param-name>controller</param-name>
      <param-value>org.demo</param-value>
    </init-param>
```
�����Ϊorg.demo���еĿ�������Ҫж��������ڡ��������д�����棬�����������O(��_��)O~��




# ·��ӳ��
��ν·��ӳ�����Ҫ�� һ��������(һ��Java�ࣩ��һ����ַ���������� �û�����ĳ��ַʱ������Զ����ÿ�������ĳ��������

��Ϊ��������˼��ϣ�����þ������٣����������·��ӳ����ͨ��������ϵ�ġ�

����Ӧ�õĸ�Ŀ¼Ϊ 
http://localhost:8080/YangMVC/

����org.demo�£����Ŀ¼������web.xml�����ã��ɼ���һ�ڣ���һ��BookController��
��ô������·����  http://localhost:8080/YangMVC/book/ 
�û��������·��ʱ����ܻ����BookController ��index���������û�����������ᱨ��

index�������Դ���ĳ��·���µ�Ĭ����ҳ����վ��б�ܽ�β�Ķ������ĳ�����index������������

book�����ַ������һ����ĸ��д������׷��Controller������
book ��·������-> Book -> BookController��������
�����·����������Ĭ�Ϲ�����


�������վ����뷽�������Է���BookController�� �κ�һ������������
�� http://localhost:8080/YangMVC/book/edit ��BookController��edit����������

��Ҫע����ǣ������д���� http://localhost:8080/YangMVC/book/edit/  (����һ����վ����һ��б�ܣ��� ������Ӧ���� book.EditController�µ�index���� ������BookController�µ�edit������

#����������
##�������
### output����
```
	output("Hello��YangMVC");
```
����������һ���ı�����ҳ�ϣ�������У������ر����������Ϊ����ر����������㲻Ҫ���������Ρ��������Ҫ�����Σ��Խ����ݷŵ�StringBuffer�У�Ȼ��ͳһ�����
###render����
```
	public void paramDemo(){
		put("a", "sss");
		render("/testrd.jsp");
		
	}
```
�����testrd.jsp��ģ��Ŀ¼��/view)Ŀ¼�µġ� /view/testrd.jsp
����Ĳ���Ӧ���������ģ��Ŀ¼�����·����

	render����ʹ�ò����ƶ�����ҳ��һ������JSTL��jsp�ļ������������������ͨ��put���ƶ��������������ϸ����
	
### render()����
	���render������û�в����ģ�����ʹ��Ĭ��ģ�壬������ģ�岻���ڣ��ͻ���ʾ����
```
	public void renderDemo(){
		request.setAttribute("a", "sss");
		render();
		
	}
```
������ controller Ϊorg.demo , templateΪ/view  ��������¡�
	org.demo.IndexController��renderDemo�������Ӧ/view/renderDemo.jsp
	֮����ģ�������ģ���Ŀ¼�£�����Ϊ���IndexController�Ǵ���Ӧ�ø�Ŀ¼�ġ������ж�Ӧ��ϵ��
	
�����org.demo.BookController,����Ӧ app��Ŀ¼�µ� /book/ Ŀ¼��
	����add������Ӧ·�� /book/add
	���Ӧ����Ϊhello,��ô���·��Ӧ����  /hello/book/add 

#### outputJSON ����
�÷���������ת��ΪJSON��������ҳ�����
```java
	public void jsonDemo(){
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("id", 12);
		map.put("name", "Yang MVC");
		map.put("addtm",new Date());
		
		outputJSON(map);
	}
```
	��������Գ�����ʵ��������ж�������һ��Map�����һ�������outputJSON�������List��Map���κ�Java�����ڲ�ת����ʹ��fastjsonʵ�ֵġ�

####  �Զ����ɲ����һ����
```
public void renderForm(Model m,String template,String postTo)
```
�ú��������ģ�Ͷ�Ӧ�ı�ṹ���Զ�����һ���������������ݷ��� �����_form �У���book�������� book_form �С�
����ҳ�У�ֱ��д ${book_form}�Ϳ��Խ�������ȥ��

template�ƶ���Ӧ��ģ���ļ�������ʡ�ԣ�ʡ�Ժ���Ĭ�Ϲ������ģ���ļ���
postTo�趨 ���ύ����ҳ������ʡ�ԣ�Ĭ����"",����ǰ��ҳ��Controller����

##��ȡ�����ķ���
1. param(String p)      ��ȡ����p��ֵ����String���ͷ���
2. paramInt(String p) ��ȡ����p��ֵ����Int���ͷ��أ���������������������쳣
3. public Model paramToModel(Model m)
	��������ƥ���ԭ�򣬽���ģ���в�������ͬ�Ĳ�����ֵ����ģ���С������ظ�ģ�͡�
���ռ������ݵ�ģ���е��������ֻ���Ϳ���ֱ�ӽ������ݿ�����ˡ�
4. paramWithDefault ��ȡ��������ͬʱ����Ĭ��ֵ�����û��������򷵻�Ĭ��ֵ��
##��鷽��
public void checkNull(String name,Object obj)  
���obj�Ƿ�Ϊnull��������׳�NullParamException�쳣��


#ORM���

## Model��DBTool
Model �����Ӧ���ݿ�ı��������һ�������а󶨡�DBTool�൱��������DAO�ࡣ
### ����һ��DBTool����
```
		DBTool tool = Model.tool("book");
```
����book�����ݿ������֡�

### ����һ���յ�Model
```
		DBTool tool = Model.tool("book");
		Model m = tool.create(); //�����µ�

```
### ����������ȡһ��Model
```
			Model m = tool.get(12);

```
### ��ѯ�������е���
```
		LasyList list = tool.all();
```
	all����һ��LasyList������������ڴ��²�û�������������ݿ��ѯ��ֻ����ҳ��������ȡʱ�Ż��ȡ���ݿ⡣����������Lasy��ԭ�򡣴˴������Django��ʵ�ֻ��ơ�
###��ѯ��limit���
```
	LasyList list = tool.all().limit(30);
	list = tool.all().limit(10,30);
	
```
###��ѯ�ĵ�ʽԼ��
```
	tool.all().eq("name","���ݸ�Ŀ")
```
###��ѯ�Ĳ���ʽԼ��
```
	tool.all().gt("id",12) //id < 12
	tool.all().lt("id",33) //id <33
	tool.all().gte("id",12) //id>=12
	tool.all().lte("id",33) //id<=33
	tool.all().ne("id",33) //�����
	
```
### ģ����ѯ
```
	tool.all().like("name","����")
```
	�������������а������ݵ��顣����һ��LasyList
###����
```
	tool.all().orderby("id",true);
```
	����id���������С� �����false�����ǽ���
	
	
###������ѯ
  ��Ϊ��Щ����Ĺ���������ȫ�����᷵��һ��LasyList���� ���Կ��Բ��ü����ķ�ʽ���и��Ӳ�ѯ���磺
  
```
list = tool.all().gt("id", 12).lt("id", 33).eq("name","haha").like("author","��");
```
 ��������൱��ִ��������SQL��䣺
```java
 select * from book where id>12 and id<33 and name='haha' and author like '%��%'
```

##Model����ع���
model ��һ���̳���Map&lt;String,Object&gt; ���࣬���Զ���
Model m;
���������ҳ��ʹ��${m.name}�ķ�ʽ����������name����Ӧ��ֵ���൱��m.get("name")
����д����JSTL�зǳ����á���Model�̳�Map�ĳ��Ծ����ڴˣ�������JSTL��ʹ�á�

���Ҳ��ע�⵽��LasyList��һ���̳���List&lt;Model&gt; ����.
���ʹ�ò�����LasyList����Model��JSTL�з��ʶ���Ϊ�ı�����

### �������еļ�ֵ����DAO�������������)
```
	model.keySet();
```
###����ĳһ�����Ե�ֵ
```
	model.get(key)
```
###����ĳһ�����Ե�ֵ
```
	model.put(key,value)
```
