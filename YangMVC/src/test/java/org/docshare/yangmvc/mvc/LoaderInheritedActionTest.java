package org.docshare.yangmvc.mvc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.lang.reflect.Method;

import org.docshare.yangmvc.mvc.anno.NotAction;
import org.junit.Test;

/**
 * P0-7：父类的 public 方法不能被当成 URL 接口。
 *
 * <p>旧实现里判断父类是不是框架 Controller 的包名写错了（少了 yangmvc），
 * 导致"直接继承 Controller"的控制器会把 output/param/download/jump 等方法暴露成接口。
 */
public class LoaderInheritedActionTest {

	public static class BaseCtl extends Controller {
		public String shared() {
			return "base";
		}

		@NotAction
		public String helper() {
			return "helper";
		}
	}

	public static class SubCtl extends BaseCtl {
		public String own() {
			return "own";
		}
	}

	public static class DirectCtl extends Controller {
		public String own() {
			return "own";
		}
	}

	@Test
	public void ownMethodIsFound() {
		Method m = Loader.findActionMethod(SubCtl.class, "own");
		assertNotNull(m);
		assertEquals(SubCtl.class, m.getDeclaringClass());
	}

	@Test
	public void inheritedMethodIsFoundButDeclaredInParent() {
		Method m = Loader.findActionMethod(SubCtl.class, "shared");
		assertNotNull(m);
		assertEquals(BaseCtl.class, m.getDeclaringClass());
	}

	@Test
	public void controllerHelperMethodsAreNotActions() {
		assertNull(Loader.findActionMethod(DirectCtl.class, "output"));
		assertNull(Loader.findActionMethod(DirectCtl.class, "param"));
		assertNull(Loader.findActionMethod(DirectCtl.class, "download"));
		assertNull(Loader.findActionMethod(DirectCtl.class, "jump"));
		assertNull(Loader.findActionMethod(DirectCtl.class, "render"));
	}

	@Test
	public void notActionAnnotationHidesInheritedMethod() {
		assertNull(Loader.findActionMethod(SubCtl.class, "helper"));
	}

	@Test
	public void inheritedActionCanBeTurnedOffByConfig() {
		boolean old = Config.inherited_action;
		Config.inherited_action = false;
		try {
			assertNull(Loader.findActionMethod(SubCtl.class, "shared"));
		} finally {
			Config.inherited_action = old;
		}
	}
}
