package org.docshare.mvc.except;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import org.docshare.log.Log;

import freemarker.core.Environment;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

public class FreeMarkerHandler implements TemplateExceptionHandler  {

	@Override
	public void handleTemplateException(TemplateException e,
			Environment env, Writer out) throws TemplateException {
		ByteArrayOutputStream ba  = new ByteArrayOutputStream();
		try {
			out.write("Freemarker Error: "+ e.getMessage());
		} catch (IOException e1) {
			e1.printStackTrace();
		}
//		try {
//			
//			e.printStackTrace(new PrintWriter(ba));
////			String msg = e.getMessage().replace("\n", "<br>\n");
////			String msg2 = msg.replace("The following has evaluated to null or missing"	, "发生错误时因为下面的变量是空的。你可以通过加默认值的方法来避免这个错误,如${haha!\"我是默认值\"}");
////			msg2 = msg2.replaceAll("in template \"(.+?)\" at line (\\d+?), column (\\d+?)", "  模板<span style='color:green;background-color:black'>$1</span>  的<span style='color:green;background-color:black'> $2 行 $3 列</span>");
////			out.write("[错误]"+msg2);
////			out.write("<br>~~~~~~~~~~~~~~~~~~~~~~~~<br>你觉得错误提示天生就是中文的吗？太天真了。下面是原文<br>");
////			out.write(msg);
////			out.write(ba.toString());
//			out.write("");
//		} catch (UnsupportedEncodingException e1) {
//			Log.e(e1);
//		} catch (IOException e2) {
//			Log.e(e2);
//		}
//		
	}

}
