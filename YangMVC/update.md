# 2020-7-5

## 1 添加了通过return输出的功能。如

```java
//相当于output("hello")        
public String retstr(){
	return "hello";
}
//相当于outputJSON(obj);
public Object retjson(){
	return MapHelper.toMap("yang",123,"wang",444);
}
//相当于renderFreemarker("/index.html")
public Object retfm(){
	return freemarker("/index.html");
}

```

## 2 添加了后处理拦截器。这个拦截器将会处理控制器函数return的对象。
   第二个参数为插入到头部还是尾部，这会影响后处理器的执行顺序
   Config.addPostInterceptor(new XXXPostIntercepter(), false); 

## 3 put函数可以加可变长参数
   这个特性是为了加速编程。
   如 put("yang",12,"zhang",33,"liu",new Book());

## 4 项目开发环境迁移到了eclipse mars中。 抛弃了myeclipse

## 5 根据参数名称自动注入控制器函数，如访问
     hello?a=12&b=zzz
```java

     public String hello(int a,String b){
        return "a ="+a+" b = "+b;
     }

```
     页面会输出 a = 12 b = zzz
     无需调用输出函数，无需注解


## 6 修改了配置读取逻辑。 优先从web.properties中读取，不论是web项目还是boot项目。


