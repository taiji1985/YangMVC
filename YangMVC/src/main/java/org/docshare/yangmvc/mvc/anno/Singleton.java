package org.docshare.yangmvc.mvc.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 声明这个控制器按单例处理（等价于在构造函数里调 {@code setSingle(true)}）。
 *
 * <pre>
 * &#64;Singleton
 * public class BookController extends Controller { ... }
 * </pre>
 *
 * <p>优先级：{@code setSingle(...)} 的显式设置 &gt; 本注解 &gt;
 * {@link org.docshare.yangmvc.mvc.Config#single_all_controllers}。
 * 所以：
 * <ul>
 *   <li>注解了但仍然想每请求新实例 → 在构造函数里 {@code setSingle(false)}；</li>
 *   <li>标在基类上（{@code @Inherited}）会对所有子类生效，慎用。</li>
 * </ul>
 *
 * <p><b>前提</b>：控制器里不能有实例字段（请求状态请放在 Controller 提供的
 * request/param/sess 等 ThreadLocal 上下文里）。否则会在创建时触发单例字段审计
 * （默认 WARN，{@link org.docshare.yangmvc.mvc.Config#strict_singleton}=true 时抛异常）。
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Singleton {
}
