package com.springbatch.monitor.utils;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;

/**
 * 日期时间工具类 - 支持多种日期格式解析
 */
public class DateTimeUtils {
    
    // 支持的日期时间格式列表（按优先级排序）
    private static final List<DateTimeFormatter> DATETIME_FORMATTERS = Arrays.asList(
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),     // 完整格式
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),        // 不含秒
        DateTimeFormatter.ofPattern("yyyy-MM-dd H:mm:ss"),      // 单位小时
        DateTimeFormatter.ofPattern("yyyy-MM-dd H:mm"),         // 单位小时不含秒
        DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"),     // 斜杠分隔
        DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm"),        // 斜杠分隔不含秒
        DateTimeFormatter.ofPattern("yyyy/MM/dd H:mm:ss"),      // 斜杠分隔单位小时
        DateTimeFormatter.ofPattern("yyyy/MM/dd H:mm")          // 斜杠分隔单位小时不含秒
    );
    
    // 支持的日期格式列表
    private static final List<DateTimeFormatter> DATE_FORMATTERS = Arrays.asList(
        DateTimeFormatter.ofPattern("yyyy-MM-dd"),              // 标准日期格式
        DateTimeFormatter.ofPattern("yyyy/MM/dd"),              // 斜杠分隔日期
        DateTimeFormatter.ofPattern("yyyy-M-d"),                // 单位月日
        DateTimeFormatter.ofPattern("yyyy/M/d")                 // 斜杠分隔单位月日
    );
    
    /**
     * 解析开始时间字符串为Timestamp
     * 如果只提供日期，则时间部分设为 00:00:00
     * 
     * @param dateTimeStr 时间字符串
     * @return Timestamp对象，解析失败返回null
     */
    public static Timestamp parseStartDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.trim().isEmpty()) {
            return null;
        }
        
        String trimmed = dateTimeStr.trim();
        
        // 首先尝试解析为完整的日期时间
        LocalDateTime dateTime = parseDateTime(trimmed);
        if (dateTime != null) {
            return Timestamp.valueOf(dateTime);
        }
        
        // 如果失败，尝试解析为日期，然后设置时间为00:00:00
        LocalDate date = parseDate(trimmed);
        if (date != null) {
            return Timestamp.valueOf(date.atStartOfDay());
        }
        
        return null;
    }
    
    /**
     * 解析结束时间字符串为Timestamp
     * 如果只提供日期，则时间部分设为 23:59:59
     * 
     * @param dateTimeStr 时间字符串
     * @return Timestamp对象，解析失败返回null
     */
    public static Timestamp parseEndDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.trim().isEmpty()) {
            return null;
        }
        
        String trimmed = dateTimeStr.trim();
        
        // 首先尝试解析为完整的日期时间
        LocalDateTime dateTime = parseDateTime(trimmed);
        if (dateTime != null) {
            return Timestamp.valueOf(dateTime);
        }
        
        // 如果失败，尝试解析为日期，然后设置时间为23:59:59
        LocalDate date = parseDate(trimmed);
        if (date != null) {
            return Timestamp.valueOf(date.atTime(23, 59, 59));
        }
        
        return null;
    }
    
    /**
     * 解析日期时间字符串
     * 
     * @param dateTimeStr 日期时间字符串
     * @return LocalDateTime对象，解析失败返回null
     */
    private static LocalDateTime parseDateTime(String dateTimeStr) {
        for (DateTimeFormatter formatter : DATETIME_FORMATTERS) {
            try {
                return LocalDateTime.parse(dateTimeStr, formatter);
            } catch (DateTimeParseException e) {
                // 继续尝试下一个格式
            }
        }
        return null;
    }
    
    /**
     * 解析日期字符串
     * 
     * @param dateStr 日期字符串
     * @return LocalDate对象，解析失败返回null
     */
    private static LocalDate parseDate(String dateStr) {
        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
            try {
                return LocalDate.parse(dateStr, formatter);
            } catch (DateTimeParseException e) {
                // 继续尝试下一个格式
            }
        }
        return null;
    }
    
    /**
     * 获取支持的日期时间格式说明
     * 
     * @return 格式说明字符串
     */
    public static String getSupportedFormats() {
        return "支持的格式:\n" +
               "• 完整时间: 2020-06-20 14:30:00, 2020-06-20 14:30\n" +
               "• 仅日期: 2020-06-20 (开始时间自动设为00:00:00，结束时间自动设为23:59:59)\n" +
               "• 斜杠格式: 2020/06/20 14:30:00, 2020/06/20\n" +
               "• 单位数字: 2020-6-20, 2020/6/20";
    }
    
    /**
     * 验证日期时间字符串格式
     * 
     * @param dateTimeStr 日期时间字符串
     * @return 验证结果信息
     */
    public static ValidationResult validateDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.trim().isEmpty()) {
            return new ValidationResult(true, "");
        }
        
        String trimmed = dateTimeStr.trim();
        
        // 尝试解析为日期时间
        if (parseDateTime(trimmed) != null) {
            return new ValidationResult(true, "有效的日期时间格式");
        }
        
        // 尝试解析为日期
        if (parseDate(trimmed) != null) {
            return new ValidationResult(true, "有效的日期格式");
        }
        
        return new ValidationResult(false, "无效的日期格式。" + getSupportedFormats());
    }
    
    /**
     * 验证结果类
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String message;
        
        public ValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public String getMessage() {
            return message;
        }
    }
}
