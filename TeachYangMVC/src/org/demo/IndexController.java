package org.demo;

import org.docshare.mvc.Controller;
/**
 * ʹ��IndexController�������Ŀ¼�µ�����
 *   / ����IndexController�� index����
 *   /test  ��ӦIndexController�µ�test����
 *   /test/ ��Ӧ����TestController�µ�index����
 *   /book/ ��Ӧ��BookController��index����
 *   �����mvc������İ����ͰѰ���·�����ϡ�
 *   org.demo.dd.WaterController ��Ӧ�ľ��� /dd/water
 *   ���ǿ���ʹ��û�в�����render����������test����ʹ�ö�Ӧ��ϵ
 *   ����Ҳ����ʹ�ô�������render����������ָ��һ��jsp�ļ���Ϊ������ͼ��
 * @author Administrator
 *
 */
public class IndexController extends Controller {
	//Ĭ�ϵ����ҳ
	public void index(){
		output("hello");
	}
	/**
	 * һ�����������԰����������
	 * ������֣� ��url�и�����ͬ��url�Ϳ����������ǡ�
	 */
	public void test(){
		render("/tt.jsp");
	}
	class Ret{
		public String s="sss";
		public String d="fff";
	}
	public void json(){
		
		outputJSON(new Ret());
	}
	
	public void testp(){
		String a = param("a");
		output(a);
	}
}
