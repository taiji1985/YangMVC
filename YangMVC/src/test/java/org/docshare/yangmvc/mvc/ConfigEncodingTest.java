package org.docshare.yangmvc.mvc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.junit.After;
import org.junit.Test;

/**
 * P0-2：web.properties 中文值不能乱码。
 *
 * <p>旧实现用 {@code Properties.load(InputStream)}，按 ISO-8859-1 解码，
 * UTF-8 的中文值会变成 {@code \u00e6\u0099\u00ba...} 这种乱码。
 */
public class ConfigEncodingTest {

	private final String oldPropEncoding = Config.prop_encoding;

	@After
	public void restore() {
		Config.prop_encoding = oldPropEncoding;
	}

	private static File write(String content, Charset charset) throws Exception {
		File f = File.createTempFile("yangmvc-prop-", ".properties");
		f.deleteOnExit();
		Files.write(f.toPath(), content.getBytes(charset));
		return f;
	}

	@Test
	public void utf8ChineseValueIsReadable() throws Exception {
		File f = write("mail_from_name=智达二级考试系统\nreloadable=false\n", StandardCharsets.UTF_8);

		assertTrue(Config.loadProperties(f.getAbsolutePath()));

		assertEquals("智达二级考试系统", Config.getProperty("mail_from_name", null));
		assertEquals("false", Config.getProperty("reloadable", null));
	}

	@Test
	public void iso88591FileStillLoads() throws Exception {
		File f = write("name=caf\u00e9\n", StandardCharsets.ISO_8859_1);

		assertTrue(Config.loadProperties(f.getAbsolutePath()));

		assertEquals("café", Config.getProperty("name", null));
	}

	@Test
	public void gbkFileIsDecodedWhenPropEncodingSaysSo() throws Exception {
		File f = write("prop_encoding=gbk\nname=智达\n", Charset.forName("GBK"));

		assertTrue(Config.loadProperties(f.getAbsolutePath()));

		assertEquals("gbk", Config.getProperty("prop_encoding", null));
		assertEquals("智达", Config.getProperty("name", null));
	}

	@Test
	public void unicodeEscapeStillWorks() throws Exception {
		// 老写法（反斜杠 + uXXXX 转义）必须继续可用，否则会破坏已有配置
		String bs = "\\";
		File f = write("name=" + bs + "u667a" + bs + "u8fbe\n", StandardCharsets.UTF_8);

		assertTrue(Config.loadProperties(f.getAbsolutePath()));

		assertNotNull(Config.getProperty("name", null));
		assertEquals("智达", Config.getProperty("name", null));
	}
}
