package org.docshare.yangmvc.mvc;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.docshare.yangmvc.util.BeanUtil;

public class Config {
	private static final Logger logger = LoggerFactory.getLogger(Config.class);

	private static  String prop_file = "/web.properties";
	@SuppressWarnings("unused")
	private static long lastUpdate = 0;
	/**
	 * 数据库主机名或IP地址
	 */
	public static  String dbhost = "localhost";
	/**
	 * 数据库用户名
	 */
	public static  String dbusr ="root";
	/**
	 * 数据库密码
	 */
	public static  String dbpwd = "123456";
	/**
	 * 数据库名称
	 */
	public static String dbname = "621m";
	
	/**
	 * 端口号
	 */
	public static String dbport = "3306";
	
	public static String encoding = "utf-8";
	
	/**
	 *  数据库类型
	 */
	public static String dbtype ="mysql";

	public static String dbschema="public";
	/**
	 * 是否打开实时Reload功能
	 */
	public static boolean reloadable = true;

	public static boolean reload_config = false;

	/**
	 * 更新数据库时，是否把 null 值写成 NULL。
	 * 默认 false：与旧行为一致（null 列被跳过，无法把列改成 NULL）。
	 * 需要"把某列清空"时请用 Model.putNull(col)，并把这个开关置为 true。
	 */
	public static boolean db_update_null = false;

	/**
	 * 请求结束时是否关闭 JDBC 连接（配合连接池使用）。
	 * 默认 false：保持旧的"线程内复用一条连接"行为。
	 */
	public static boolean db_close_per_request = false;

	/**
	 * 列名是否必须存在于表元数据中。
	 * 默认 false：只做危险字符校验（任何正常列名都能通过）。
	 */
	public static boolean ident_strict = false;

	/**
	 * 是否允许把父类的 public 方法当成 URL 接口（历史行为）。
	 * 默认 true。确认业务不依赖继承来的 action 后可以置为 false。
	 */
	public static boolean inherited_action = true;

	/**
	 * 单例模式的严格检查：控制器调用 setSingle(true) 时，如果它自己声明了实例字段，
	 * 默认只打 WARN；置为 true 则直接抛异常（建议只在开发/测试环境开）。
	 */
	public static boolean strict_singleton = false;

	/**
	 * 全局单例：true 时**所有控制器**都按单例处理（等价于每个控制器都调 {@code setSingle(true)}）。
	 *
	 * <p>默认 false。开启前请确认控制器没有实例字段（可先打开 {@link #strict_singleton} 跑一轮）；
	 * 单个控制器可以用 {@code setSingle(false)} 退出全局单例。
	 */
	public static boolean single_all_controllers = false;

	/**
	 * 连接探活的时间窗（毫秒）：在这段时间内复用连接时不再调用 {@code isValid()}。
	 * 原来每条 SQL 前都 ping 一次（一次网络往返）；默认 30 秒。
	 */
	public static int db_valid_interval_ms = 30000;

	/**
	 * 自研语句池的上限（按 SQL 条数）。超过后按 LRU 淘汰并 close。
	 */
	public static int db_statement_cache_size = 128;

	/**
	 * {@code Model.get("xxx")} 是否允许隐式外键查询（列里没有 xxx、但有 xxx_id 时现场查库）。
	 * 默认 true（旧行为）；排查 N+1 时可以置为 false。
	 */
	public static boolean model_fk_lookup = true;

	/**
	 * 内置连接池大小（配合 {@code conn_pool=org.docshare.yangmvc.orm.mysql.SimpleConnectionPool}
	 * 与 {@link #db_close_per_request}=true 使用）。
	 */
	public static int db_pool_size = 16;

	/** 内置连接池借连接的超时（毫秒）。 */
	public static int db_pool_timeout_ms = 5000;

	/** 内置连接池空闲连接的保留时间（毫秒），超过后借出时丢弃。 */
	public static int db_pool_idle_ms = 300000;

	/**
	 * 表结构（列元数据）自动刷新间隔（毫秒）。0 = 不自动刷新（默认，保持旧行为）。
	 * 运行中会 ALTER TABLE 的环境可以设成 60000。
	 */
	public static int db_meta_refresh_ms = 0;

	/**
	 * web.properties 的解码方式：auto / utf-8 / gbk / gb18030 ...
	 * auto：先按 UTF-8 严格解码，失败后退回 ISO-8859-1（与旧行为一致）。
	 */
	public static String prop_encoding = "auto";

	/**
	 * 热加载：后台扫描 class 文件的间隔（毫秒）。0 = 不启后台线程，退回"请求时检查当前类"的模式。
	 */
	public static int reload_scan_interval_ms = 1000;

	/**
	 * 热加载：抖动窗口（毫秒）。一次编译改动多个 class 时，等安静下来再整代替换。
	 */
	public static int reload_debounce_ms = 500;

	/**
	 * 热加载：不参与热加载的包。
	 * 支持两种写法：{@code *.service}（包名中任意一段叫 service）与
	 * {@code org.docshare.l2exam.oss}（完整包前缀）。命中者一律由父加载器加载（改了要重启）。
	 */
	public static String reload_exclude_packages = "*.service,*.pojo";

	/**
	 * 热加载：class 文件所在目录。空 = 自动探测（bin / target/classes / CodeSource）。
	 */
	public static String class_root = null;
	
	/**
	 * 模板相对于WebRoot的目录路径
	 */
	public static  String template;
	//做一点向前兼容 ，名字由 tpl_base改为template
	public static String tpl_base(){ return template;}
	public static void tpl_base(String s){template = s;}
	

	public static String dateFormat=null; //输出的date的格式化
	public static String dbEncoding="utf8";
	public static String pageEncoding="utf-8";
	
	/**
	 * 控制器根包名
	 */
	public static  String controller;
	//做一点向前兼容 ,名字由 ctr_base改为了controller
	public static String ctr_base(){return controller;}
	public static void ctr_base(String s){controller = s;}
	
	/**
	 * 是否使用SSL连接，默认为false
	 */
	public static boolean useSSL = false;
	
	public static ArrayList<URLFilter> urlFilters = new ArrayList<>();
	
	public static String str() {
		String ret=  "Config [\n\tdbhost=" + dbhost 
				+", \n\tdbname=" + dbname 
				+ ", \n\tdbusr=" + dbusr 
				+ ", \n\tdbpwd="+ dbpwd 
				+ ", \n\tport = "+ dbport 
				+ ", \n\ttemplate=" + template 
				+ ", \n\tcontroller=" + controller
				+",\n\treloadable="+reloadable
				+",\n\tuseSSL="+useSSL
				+",\n\tdbtype="+dbtype
				+",\n\tdbEncoding="+dbEncoding
				+",\n\tinterceptors="+getInteNames(interceptors)
				+", \n\tpost-process="+getInteNames(postInterceptors)
				+ "\n]";
		StringBuilder sb =new StringBuilder();
		sb.append(ret);
		if(pro == null){
			loadProperties("/web.properties");
		}

		for(Object s: pro.keySet()){
			sb.append("\n"+s+"="+pro.getProperty(s+""));
		}
		
		return sb.toString();
		
		
	}
	public static ArrayList<Interceptor> interceptors =new ArrayList<Interceptor>();
	
	public static void addInterceptor(Interceptor interceptor){
		if(interceptors.contains(interceptor)){
			return;
		}
		interceptors.add(interceptor);
		logger.debug("Config.addInterceptor added, name ="+interceptor.name());
	}
	
	public static void removeInterceptor(Interceptor interceptor){
		interceptors.remove(interceptor);
	}
	
	/**
	 * 在运行后执行. 比如将obj转json之类
	 */
	static ArrayList<Interceptor> postInterceptors =new ArrayList<Interceptor>();
	static{
		//默认将根据控制器的返回值的输出转换为响应的形式。
		addPostInterceptor(new BasePostIntercepter(), false);
	}
	/**
	 * 添加后处理程序。 程序是按照从前到后的执行顺序来实现的。前一个的输出作为后一个的输入。<br>
	 * 如果有一个输出为null，则后续的处理不再执行。
	 * @param interceptor 后处理类
	 * @param addToFirst  是否放到第一个。
	 */
	public static void addPostInterceptor(Interceptor interceptor,boolean addToFirst){
		if(postInterceptors.contains(interceptor)){
			return;
		}
		if(addToFirst){
			postInterceptors.add(0, interceptor);
		}else{
			postInterceptors.add(interceptor);
		}
		logger.debug("Config.addPostInterceptor added, name ="+interceptor.name());
	}
	
	public static void removePostInterceptor(Interceptor interceptor){
		postInterceptors.remove(interceptor);
	}
	
	private static String getInteNames(ArrayList<Interceptor> list){
		StringBuilder sb =new StringBuilder();
		sb.append("{");
		boolean isFirst = true;
		for(Interceptor i :list){
			if(isFirst){
				isFirst = false;
			}else{
				sb.append(" , ");
			}
			sb.append(i.name());
		}
		sb.append("}");
		return sb.toString();
	}
	private static InputStream getPropertiesStream(String PROP_FILE){
		try {
			
			
			File f = new File(PROP_FILE); // ./web.propertes
			if(f.exists()){
				logger.info("read prop from {}", f.getAbsolutePath());
				lastUpdate = f.lastModified();
				return new FileInputStream(f);
			}
			
			f = new File("."+PROP_FILE);
			if(f.exists()){
				logger.info("read prop from {}", f.getAbsolutePath());
				lastUpdate = f.lastModified();
				return new FileInputStream(f);
			}
			logger.debug("class loader name "+Config.class.getClassLoader().toString());
			logger.info("read prop from classpath {}", PROP_FILE);

			return Config.class.getResourceAsStream(PROP_FILE);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
		
	}

	/**
	 * 读取 properties，并按 {@link #prop_encoding} 的策略解码。
	 *
	 * <p>历史实现用的是 {@code Properties.load(InputStream)}，它按 ISO-8859-1 解码，
	 * 于是 UTF-8 的中文值会变成乱码。这里改成先读字节再自己解码：
	 * 先严格按 UTF-8，失败后退回 ISO-8859-1（与旧行为一致），
	 * 如果文件里声明了 {@code prop_encoding=gbk} 之类，再按它重新解码一次。
	 */
	static Properties readProperties(InputStream in) throws IOException {
		if (in == null) {
			throw new IOException("InputStream is null");
		}
		ByteArrayOutputStream bos = new ByteArrayOutputStream(1024);
		byte[] buf = new byte[4096];
		int n;
		while ((n = in.read(buf)) > 0) {
			bos.write(buf, 0, n);
		}
		byte[] raw = bos.toByteArray();

		String text = decodeStrictUtf8(raw);
		boolean utf8 = text != null;
		if (!utf8) {
			text = new String(raw, StandardCharsets.ISO_8859_1);
			logger.warn("{} is not valid UTF-8, fallback to ISO-8859-1; convert it to UTF-8 to avoid garbled text", prop_file);
		}

		Properties ret = new Properties();
		ret.load(new StringReader(text));

		String enc = ret.getProperty("prop_encoding", "auto");
		if (!utf8 && enc != null) {
			enc = enc.trim();
			if (!enc.isEmpty() && !"auto".equalsIgnoreCase(enc)
					&& !"utf-8".equalsIgnoreCase(enc) && !"utf8".equalsIgnoreCase(enc)) {
				try {
					Properties p2 = new Properties();
					p2.load(new StringReader(new String(raw, enc)));
					ret = p2;
					logger.info("{} decoded as {}", prop_file, enc);
				} catch (Exception e) {
					logger.warn("can not decode {} as {}, keep ISO-8859-1 result", prop_file, enc);
				}
			}
		}
		return ret;
	}

	private static String decodeStrictUtf8(byte[] raw) {
		try {
			return StandardCharsets.UTF_8.newDecoder()
					.onMalformedInput(CodingErrorAction.REPORT)
					.onUnmappableCharacter(CodingErrorAction.REPORT)
					.decode(ByteBuffer.wrap(raw)).toString();
		} catch (CharacterCodingException e) {
			return null;
		}
	}
	
	static Properties pro=null;
	public static String getProperty(String key,String def){
		if(pro == null){
			return null;
		}
		
		return pro.getProperty(key,def);
	}
	
	/**
	 * 读取properties文件
	 * @param PROP_FILE 属性文件的路径
	 * @return 加载是否成功
	 */
	public static boolean loadProperties(String PROP_FILE){
		InputStream in = null;
		if(PROP_FILE != null){
			Config.prop_file  = PROP_FILE;
		}else{
			PROP_FILE = Config.prop_file;
		}
		try {
			in = getPropertiesStream(PROP_FILE);
			if(in == null){
				logger.error("Config file NOT found : web.properties ");
				return false;
			}else{
				logger.info("Config  found ! ");
			}

			pro = readProperties(in);
			
			logger.info("web.properties loaded ");
			BeanUtil.prop2StaticField(pro, Config.class);

			if(reload_config){
				startReloadThread();
			}

			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}finally {
			try {
				in.close();
			} catch (Exception e2) {
			}
		}


	}
	/**
	 * 从输入流读取properties配置
	 * @param in 输入流
	 * @return 加载是否成功
	 */
	public static boolean loadPropertiesFromStream(InputStream in){
		if(in == null){
			logger.error("InputStream is null");
			return false;
		}
		
		try {
			pro = readProperties(in);
			
			logger.info("Properties loaded from InputStream");
			BeanUtil.prop2StaticField(pro, Config.class);
			
			if(reload_config){
				startReloadThread();
			}
			
			return true;
		} catch (IOException e) {
			logger.error("Failed to load properties from InputStream: " + e.getMessage());
			e.printStackTrace();
			return false;
		} finally {
			try {
				in.close();
			} catch (Exception e2) {
				// ignore
			}
		}
	}

	private static Thread reloadThread = null;
	private static void startReloadThread(){
		reloadThread = new Thread(()->{
			try {
				Thread.sleep(5000);
				logger.debug("reload cfg");
				Config.loadProperties(null);
			} catch (InterruptedException e) {
				return;
			}

		});
		reloadThread.start();
	}
	
	public static void main(String[] args) {
		Config.loadProperties("/web.properties");
	}

	// conn_pool  如果为null，则不使用连接池。如果指定了，则使用对应的连接池进行连接。
}
