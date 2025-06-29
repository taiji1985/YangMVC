package org.docshare.yangmvc.util;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
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
	   }else{
		   return null;
	   }
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
