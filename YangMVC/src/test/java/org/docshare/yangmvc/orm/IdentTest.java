package org.docshare.yangmvc.orm;

import static org.junit.Assert.assertThrows;

import org.docshare.yangmvc.mvc.except.MVCException;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * P0-4：列名会直接拼进 SQL（值才是参数化的），必须校验，否则可以注入。
 */
public class IdentTest {

	@Test
	public void normalNamesPass() {
		Ident.assertSafe("id");
		Ident.assertSafe("addtm");
		Ident.assertSafe("col_2");
		Ident.assertSafe("用户");
		Ident.assertSafeList("id,name,addtm");
	}

	@Test
	public void emptyNameRejected() {
		assertThrows(MVCException.class, () -> Ident.assertSafe(null));
		assertThrows(MVCException.class, () -> Ident.assertSafe(""));
		assertThrows(MVCException.class, () -> Ident.assertSafe("   "));
	}

	@Test
	public void dangerousNamesRejected() {
		String[] bad = {
				"a`b", "a'b", "a\"b", "a;b", "a b", "a--b", "a/*b", "a*/b",
				"a)b", "a(b", "a/b", "a\\b", "a#b", "id = 1 or 1=1",
				// Access 用 [列名] 引用标识符，']' 能提前闭合，必须拦掉
				"a]b", "a[b"
		};
		for (String s : bad) {
			assertThrows("should reject: " + s, MVCException.class, () -> Ident.assertSafe(s));
		}
	}

	@Test
	public void listRejectsWhenAnyItemIsBad() {
		assertThrows(MVCException.class, () -> Ident.assertSafeList("id,name`x"));
	}

	@Test
	public void lasyListValidatesColumnNames() {
		DBTool tool = Mockito.mock(DBTool.class);
		LasyList list = new LasyList(tool, "usr");

		assertThrows(MVCException.class, () -> list.eq("id` or 1=1 --", 1));
		assertThrows(MVCException.class, () -> list.like("name'", "x"));
		assertThrows(MVCException.class, () -> list.orderby("id; drop table usr", false));
		assertThrows(MVCException.class, () -> list.addOrderby("(select sleep(5))", true));
		assertThrows(MVCException.class, () -> list.mlike("id,`x`", "v"));
	}

	@Test
	public void customConstraintStaysRaw() {
		// custom() 是"我就要写原生 SQL"的逃生口，保持原样（文档已标注风险）
		DBTool tool = Mockito.mock(DBTool.class);
		LasyList list = new LasyList(tool, "usr");
		list.custom("poster_id = 1 or teacher_id = 1");
	}
}
