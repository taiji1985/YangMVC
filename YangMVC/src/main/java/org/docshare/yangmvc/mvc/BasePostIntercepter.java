package org.docshare.yangmvc.mvc;

/**
 * 这个类适合做最后一个后处理器。
 * 这个后处理器是默认在后处理列表中的。
 * @author HP
 *
 */
public class BasePostIntercepter extends Interceptor {

	@Override
	public Object postProcess(String uri, Controller c, Object ret) {
		if(ret == null)return null;
		if(ret instanceof String){
			c.output((String) ret);
		}else if(ret instanceof Controller.JSP){
			Controller.JSP jsp = (Controller.JSP)ret;
			if(jsp.path == null) c.render();
			else c.render(jsp.path);
		}else if(ret instanceof Controller.FreeMarker){
			Controller.FreeMarker fm = (Controller.FreeMarker)ret;
			c.renderFreeMarker(fm.path);
		}else{
			c.outputJSON(ret);
		}
		return null;		
	}

}
