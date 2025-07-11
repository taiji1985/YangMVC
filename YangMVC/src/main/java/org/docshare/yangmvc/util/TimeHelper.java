package org.docshare.yangmvc.util;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

public class TimeHelper {
	static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	public static String format(Date end) {
		return simpleDateFormat.format(end);
	}
	public static Date extractBirthdayFromIdCard(String idCardNumber) {
        try {
            String birthdayString = idCardNumber.substring(6, 14);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
            Date birthday = dateFormat.parse(birthdayString);
            return birthday;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
	
   public static long localDateTimetoTimestamp(LocalDateTime localDateTime) {
        // 将 LocalDateTime 转换为指定时区（东八区）的 ZonedDateTime
        ZonedDateTime zonedDateTime = localDateTime.atZone(ZoneId.systemDefault());
        // 将 ZonedDateTime 转换为时间戳（毫秒）
        return zonedDateTime.toInstant().toEpochMilli();
   }
   public static long toTimestamp(Object o){
	   if(o == null) return 0;
	   if(o instanceof Long){
		   return (long)o;
	   }else if(o instanceof Date){
		   Date date =(Date)o;
		   return date.getTime();
	   }else if(o instanceof LocalDateTime){
		   return localDateTimetoTimestamp((LocalDateTime)o);
	   }else{
		   return 0;
	   }
   }
   
   public static Date toDate(Object o){
	   if(o == null) return null;
	   if(o instanceof Long){
		   return new Date((long)o);
	   }else if(o instanceof Date){
		  return (Date) o;
	   }else if(o instanceof LocalDateTime){
		   return localDateTimetoDate((LocalDateTime)o);
	   }else if(o instanceof String){
		   return s2d((String)o);
	   }else{
		   return null;
	   }
   }
   
   /**
    * 字符串转LocalDateTime方法 (String to LocalDateTime)
    * 支持多种常见的日期格式
    */
   public static LocalDateTime s2ldt(String dateStr){
	   if(dateStr == null || dateStr.trim().isEmpty()) return null;
	   
	   dateStr = dateStr.trim();
	   
	   try {
		   // 尝试默认格式 yyyy-MM-dd HH:mm:ss
		   if(dateStr.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}")){
			   return LocalDateTime.parse(dateStr, java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
		   }
		   
		   // 尝试日期格式 yyyy-MM-dd (默认时间为00:00:00)
		   if(dateStr.matches("\\d{4}-\\d{2}-\\d{2}")){
			   return LocalDateTime.parse(dateStr + " 00:00:00", java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
		   }
		   
		   // 尝试时间戳格式
		   if(dateStr.matches("\\d+")){
			   long timestamp = Long.parseLong(dateStr);
			   // 如果是秒级时间戳，转换为毫秒
			   if(timestamp < 10000000000L){
				   timestamp *= 1000;
			   }
			   return LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
		   }
		   
		   // 尝试ISO格式
		   if(dateStr.contains("T")){
			   return LocalDateTime.parse(dateStr.replace("T", " ").substring(0, 19), java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
		   }
		   
		   // 尝试其他常见格式
		   String[] patterns = {
			   "yyyy/MM/dd HH:mm:ss",
			   "yyyy/MM/dd",
			   "dd/MM/yyyy",
			   "MM/dd/yyyy",
			   "yyyyMMdd"
		   };
		   
		   for(String pattern : patterns){
			   try {
				   java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern(pattern);
				   if(pattern.contains("HH")){
					   return LocalDateTime.parse(dateStr, formatter);
				   } else {
					   return LocalDateTime.parse(dateStr + " 00:00:00", java.time.format.DateTimeFormatter.ofPattern(pattern + " HH:mm:ss"));
				   }
			   } catch (Exception ignored) {
				   // 继续尝试下一个格式
			   }
		   }
		   
	   } catch (Exception e) {
		   e.printStackTrace();
	   }
	   
	   return null;
   }
   
   /**
    * 字符串转日期方法 (String to Date)
    * 支持多种常见的日期格式
    */
   public static Date s2d(String dateStr){
	   if(dateStr == null || dateStr.trim().isEmpty()) return null;
	   
	   dateStr = dateStr.trim();
	   
	   try {
		   // 尝试默认格式 yyyy-MM-dd HH:mm:ss
		   if(dateStr.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}")){
			   return simpleDateFormat.parse(dateStr);
		   }
		   
		   // 尝试日期格式 yyyy-MM-dd
		   if(dateStr.matches("\\d{4}-\\d{2}-\\d{2}")){
			   SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			   return dateFormat.parse(dateStr);
		   }
		   
		   // 尝试时间戳格式
		   if(dateStr.matches("\\d+")){
			   long timestamp = Long.parseLong(dateStr);
			   // 如果是秒级时间戳，转换为毫秒
			   if(timestamp < 10000000000L){
				   timestamp *= 1000;
			   }
			   return new Date(timestamp);
		   }
		   
		   // 尝试其他常见格式
		   String[] patterns = {
			   "yyyy/MM/dd HH:mm:ss",
			   "yyyy/MM/dd",
			   "dd/MM/yyyy",
			   "MM/dd/yyyy",
			   "yyyyMMdd",
			   "yyyy-MM-dd'T'HH:mm:ss"
		   };
		   
		   for(String pattern : patterns){
			   try {
				   SimpleDateFormat sdf = new SimpleDateFormat(pattern);
				   return sdf.parse(dateStr);
			   } catch (Exception ignored) {
				   // 继续尝试下一个格式
			   }
		   }
		   
	   } catch (Exception e) {
		   e.printStackTrace();
	   }
	   
	   return null;
    }
    
    /**
     * 字符串转ZonedDateTime方法 (String to ZonedDateTime)
     * 支持多种常见的日期格式
     */
    public static ZonedDateTime s2zdt(String dateStr){
	   LocalDateTime ldt = s2ldt(dateStr);
	   return ldt != null ? ldt.atZone(ZoneId.systemDefault()) : null;
    }
    
    /**
     * 字符串转OffsetDateTime方法 (String to OffsetDateTime)
     * 支持多种常见的日期格式
     */
    public static OffsetDateTime s2odt(String dateStr){
	   if(dateStr == null || dateStr.trim().isEmpty()) return null;
	   
	   dateStr = dateStr.trim();
	   
	   try {
		   // 尝试ISO格式 (带时区偏移)
		   if(dateStr.contains("+") || dateStr.matches(".*-\\d{2}:\\d{2}$")){
			   return OffsetDateTime.parse(dateStr);
		   }
		   
		   // 转换为LocalDateTime然后使用系统默认偏移
		   LocalDateTime ldt = s2ldt(dateStr);
		   return ldt != null ? ldt.atOffset(OffsetDateTime.now().getOffset()) : null;
		   
	   } catch (Exception e) {
		   e.printStackTrace();
	   }
	   
	   return null;
    }
    
private static Date localDateTimetoDate(LocalDateTime o) {
	if(o == null) return null;
	// 将 LocalDateTime 转换为 ZonedDateTime
    ZonedDateTime zonedDateTime = o.atZone(ZoneId.systemDefault());
    // 将 ZonedDateTime 转换为 Instant
    java.time.Instant instant = zonedDateTime.toInstant();
    // 将 Instant 转换为 Date
    return Date.from(instant);
}
}
