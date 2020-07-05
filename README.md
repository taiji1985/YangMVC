[toc]

# YangMVC
# ���ص�ַ

https://git.oschina.net/yangtf/YangMVC/attach_files

���ڴ�ҳ���ذ汾����ߵİ汾


# ������־

https://gitee.com/yangtf/YangMVC/blob/master/YangMVC/update.md

# ���

YangMVC��һ����Ч�ġ�������MVC��ORM��ܡ� ��ֻ��Ҫ�������Ŀ�ж���һ��jar����������ɾ������Web��վ�Ŀ�����

YangMVC����ַ�Ϳ�������֮�������Ĭ������Լ���ķ��������������á���IndexController��Ӧ��վ��Ŀ¼�� BookController��Ӧ/bookĿ¼��

YangMVC�ṩ�� ORM��Ϊ���á�

LasyList list = Model.tool("book").all().gt("id",12).lt("id,33);

��仰�൱��sql��� select * from book where id>12 and id<33

���õ���һ�����Ա����飩������ֱ����JSTL��FreeMarker��ʹ��ѭ����ö�١���

����и���sql��ҪЩ�������ֱ��дsql����ͬ���᷵��LasyList��������ResultSet��


ORM��ܲ���ҪԤ�������κ�POJO�࣬���б�ӳ��Ϊ���õ�Model�࣬��һ��Model�����Ӧ���ݿ���е�һ�С����� Model������Ӧ�κεı����ͼ��

�����ȷʵ��Ҫ�����ݿ��е�����ת��Ϊһ���ض���java����POJO��),��ôModel�ṩ��������ֱ��ת��Ϊ����Ҫ���ࡣ������Ҳ�ǳ����㡣

ʹ�������ܣ�������Լ�Ϊ��Ч���ٶ��ƽ������Ŀ��������ҪΪ��дһ�����ܣ�ȥ��java�ļ���������xml�ļ�����SSH��ͬѧ����ðһ���ݣ��ǲ���������

���ݿ������ڳ��ڳ��в����Ƶĵط������ʹ��Hibernate���ֿ�ܣ���ô����Ҫ���޸����ݿ����������Java�ࡣ����Ӧ��DAO��ҲҪ��Ӧ�޸ģ����ֱ��ج�Ρ�����

iBatis����ϲ���ģ�������Ҫsql��䡣YangMVC���á�����
# Ӧ�ó���
## 1 App �ķ���˳��� ����Vue+��˼ܹ��ĺ�˳���
![](images/bootdemo.jpg)
���ּܹ��£���һ��ֻ��Ҫ���JSON������Դ���һ��java���̣�����һ��yangmvc-xxx-boot.jar, �½�һ����������ֱ�����У�
```java
public class IndexController extends Controller{
	public void index(){
		//T("book") �ȼ���Model.tool
		//L("book") �ȼ��� Model.tool("book").all()
		LasyList list = L("book");
		outputJSON(list);
	}
	public void add(){
		Model book = T("book").create();
		paramToModel(book); //�Զ��ռ� ������book���� �����height�ᱣ�浽book��height�����С�
		book.save();
		output("ok");
	}
	public void del(){
		int id = paramInt("id"��-1); //��Ĭ��ֵ���Զ�ת������
		if(id<0) {
			output("fail");
			return;
		}
		T("book").del(id); //��ݵ�����ɾ��
		output("ok");
	}
}
```
����http://127.0.0.1:1985/ ���Ի��book���Ӧ��json���ݡ�

http://127.0.0.1:1985/add ������ݶ�Ӧ�Ľӿڵ�ַ

http://127.0.0.1:1985/del?id=12 ɾ�����ݶ�Ӧ�Ľӿڵ�ַ

## 2 ����Web��Ŀ

����web��Ŀ����ֻ��Ҫ�ֹ�����һ��jar������yangmvc-�汾��.jar
�����ϣ�ֻ��Ҫ��web.xml�м������ݿ��ַ�ȱ����������Ϣ���ɡ�

# ����Ⱥ

�����Լӽ���Ⱥ QQ 753780493 


# ��ʼʹ��YangMVC


��������Ҫ���ÿ��������� ����Ҫһ��JDK1.7���ϵİ汾������Ҫһ��Eclipse��MyEclipse��

�����ֻ��Eclipse���뿴 [����̳�](https://gitee.com/yangtf/YangMVC/wikis/A01-%E9%85%8D%E7%BD%AE-boot%E7%89%88) �� 

���ʹ��MyEclipse�뿴  [A01������](https://gitee.com/yangtf/YangMVC/wikis/A01-%E9%85%8D%E7%BD%AE)



# ������־

## 2018-6-5
���汾��������ʱ�������� 
yangmvc-2018-6-5.jar 
boot�汾Ϊ yangmvc-boot-2018-6-5.jar

���������POJO��Ĺ��ܣ�ʹ�÷�������


���������ݿ���Ϣ����������������ͬ������ֱ�Ӵ�س������������������ipĬ��Ϊ: [localhost]: 

ִ������ 

java -jar yangmvc-2018-6-5.jar 

���������ݿ���Ĭ��Ϊ: [mvc_demo]:  

������˿ں�Ĭ��Ϊ: [3306]: 

����������Ĭ��Ϊ: [123456]: 

�������û���Ĭ��Ϊ: [root]: 

....

��������Ҫ���ɵ���İ���:org.yang

���ͻὫ�������ɵ�������ı�����Ӧ��Ŀ¼�£�

������org.yang,��������ɵ�src/org/yang �С�



## V2.3.6 

ͯЬ������һ��bug������tomcat6.0 ʹ��YangMVC �޷��ϴ��ļ��������Ե�֪ԭ��Ϊ�� YangMVC���ϴ�ʱʹ���˸߰汾��Servlet-api,��Tomcat6 ��֧�֣����ԡ�����ȥ���˶Ը߰汾api��ʹ�á�   ʹ���ڶ�����tomcat6.0���������κ����⡣���� 

## V2.3.5 
1. ����ʹ�ùؼ�����Ϊ�������ֶ���
2. ��Model����� getStr /getInt / getLong��Щ��ݵķ�������Ҫ�Լ�ǿת�ˡ�

## V2.0 ��������
  1. ������gzip���书��
  2. ��������֪����


# �������ĵ��뿴

https://gitee.com/yangtf/YangMVC/wikis/pages

# javadoc ��ַ

http://yangtf.gitee.io/yangmvc/YangMVC/doc/index.html

# ����ĵļ������Javadoc

��������˵��ֻҪ�������������ĵ��࣬�Ϳ�������ʹ�������ܡ�

������Controller��

http://yangtf.gitee.io/yangmvc/YangMVC/doc/org/docshare/mvc/Controller.html

DBTool��
http://yangtf.gitee.io/yangmvc/YangMVC/doc/org/docshare/orm/DBTool.html

LasyList��
http://yangtf.gitee.io/yangmvc/YangMVC/doc/index.html


