package org.docshare.yangmvc.mvc.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记某个 public 方法<b>不是</b> URL 接口。
 *
 * <p>YangMVC 的约定是"控制器里的 public 方法就是接口"，但控制器经常会放一些
 * 给子类/自己调用的 public 工具方法（例如 BaseController 里的辅助方法），
 * 这些方法会被 URL 直接调到。加上这个注解就能把它们排除掉。
 *
 * <pre>
 * public class BaseController extends Controller {
 *     &#64;NotAction
 *     public Ret pageRet(LasyList list) { ... }
 * }
 * </pre>
 *
 * 也可以标在类上，表示这个类的所有 public 方法都不作为接口（用于中间基类）。
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface NotAction {
}
