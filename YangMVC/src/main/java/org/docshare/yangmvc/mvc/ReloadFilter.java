package org.docshare.yangmvc.mvc;

/**
 * 热加载的"哪些类由子加载器定义"判定。
 *
 * <p>规则：
 * <ul>
 *   <li>必须在 {@code basePackage} 之下；</li>
 *   <li>且不能被 {@link Config#reload_exclude_packages} 命中（默认排除 service / pojo ——
 *       它们被父加载器加载，改了要重启，但避免出现"父子各一份"）。</li>
 * </ul>
 *
 * <p>排除项支持两种写法：{@code *.service}（包路径中任意一段等于 service）、
 * {@code org.docshare.l2exam.oss}（完整包前缀）。
 */
final class ReloadFilter {

	private ReloadFilter() {
	}

	static boolean isReloadable(String className, String basePackage) {
		if (className == null || className.isEmpty()) {
			return false;
		}
		String base = basePackage;
		if (base == null || base.isEmpty()) {
			base = Config.controller;
		}
		if (base == null || base.isEmpty()) {
			return false;
		}
		if (!className.startsWith(base + ".")) {
			return false;
		}
		return !isExcluded(className);
	}

	static boolean isExcluded(String className) {
		String list = Config.reload_exclude_packages;
		if (list == null) {
			return false;
		}
		list = list.trim();
		if (list.isEmpty()) {
			return false;
		}

		int dot = className.lastIndexOf('.');
		String pkg = dot > 0 ? className.substring(0, dot) : "";
		String dottedPkg = "." + pkg + ".";

		for (String raw : list.split(",")) {
			String e = raw.trim();
			if (e.isEmpty()) {
				continue;
			}
			if (e.startsWith("*.")) {
				String seg = e.substring(2).trim();
				if (!seg.isEmpty() && dottedPkg.contains("." + seg + ".")) {
					return true;
				}
			} else if (className.equals(e) || className.startsWith(e + ".")) {
				return true;
			}
		}
		return false;
	}
}
