package org.docshare.yangmvc.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

import org.docshare.yangmvc.log.Log;
import org.junit.Test;

/**
 * P0-6：原来的 static SimpleDateFormat 不是线程安全的，并发下会抛异常或格式化出错。
 */
public class TimeHelperTest {

	@Test
	public void formatOutputStaysTheSame() throws Exception {
		Date d = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2026-10-18 09:05:03");
		assertEquals("2026-10-18 09:05:03", TimeHelper.format(d));
	}

	@Test
	public void parseRoundTrip() {
		Date d = TimeHelper.s2d("2026-10-18 09:05:03");
		assertNotNull(d);
		assertEquals("2026-10-18 09:05:03", TimeHelper.format(d));
	}

	@Test
	public void nullIsNull() {
		assertNull(TimeHelper.format(null));
		assertNull(TimeHelper.s2d(null));
	}

	@Test
	public void concurrentFormatIsConsistent() throws Exception {
		final Date d = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2026-01-02 03:04:05");
		final String expect = "2026-01-02 03:04:05";
		final int threads = 16;
		final int loops = 2000;
		final Set<String> bad = ConcurrentHashMap.newKeySet();
		final CountDownLatch start = new CountDownLatch(1);
		Thread[] ts = new Thread[threads];
		for (int i = 0; i < threads; i++) {
			ts[i] = new Thread(() -> {
				try {
					start.await();
				} catch (InterruptedException ignored) {
					return;
				}
				for (int j = 0; j < loops; j++) {
					try {
						String s = TimeHelper.format(d);
						if (!expect.equals(s)) {
							bad.add(s);
						}
						String now = Log.now();
						if (now == null || now.length() != 19) {
							bad.add(String.valueOf(now));
						}
					} catch (Throwable t) {
						bad.add(t.getClass().getName());
					}
				}
			});
			ts[i].start();
		}
		start.countDown();
		for (Thread t : ts) {
			t.join();
		}
		assertEquals("并发下不应出现异常或错误结果: " + bad, 0, bad.size());
	}
}
