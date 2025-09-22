package com.yxw.expression.resolve.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * 日期转换工具类，主要用于将多种日期类型转换为 ZonedDateTime。方便进行日期的转换和比较。
 */
public class DateConverter {

    private static final Logger log = LoggerFactory.getLogger(DateConverter.class);

    // 使用 ISO 8601 标准的内置格式化器，它支持带毫秒和不带毫秒的 UTC 时间
    private static final DateTimeFormatter ISO_INSTANT_FORMATTER = DateTimeFormatter.ISO_INSTANT;

    // 解析本地时间，例如 2025-10-26 12:00:00
    private static final DateTimeFormatter LOCAL_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // 解析本地日期，例如 2025-10-26
    private static final DateTimeFormatter LOCAL_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * 将多种日期类型转换为 ZonedDateTime。
     *
     * @param dateValue 待转换的日期值
     * @return 转换后的 ZonedDateTime 对象
     * @throws IllegalArgumentException 如果传入的日期格式不支持
     */
    public static ZonedDateTime convertToZonedDateTime(Object dateValue) {
        if (dateValue == null) {
            return null;
        }
        if (dateValue instanceof Long timestamp) {
            //Long 类型的时间戳（通常是毫秒数）本身是不带任何时区信息的。它只是一个数字，代表从 1970年1月1日 00:00:00 UTC（协调世界时）开始经过的毫秒数。
            return ZonedDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.of("UTC"));
        }
        if (dateValue instanceof Date date) {
            // Date 对象本质上是一个时间戳（从 1970-01-01T00:00:00Z 开始的毫秒数）
            return date.toInstant().atZone(ZoneId.of("UTC"));
        }
        if (dateValue instanceof LocalDateTime localDateTime) {
            // LocalDateTime 本身不包含任何时区信息。它只是一组日期和时间的字段（年、月、日、时、分、秒），是“本地”时间。
            ZoneId systemZone = ZoneId.systemDefault();
            ZonedDateTime systemZonedDateTime = ZonedDateTime.of(localDateTime, systemZone);
            return systemZonedDateTime.toInstant().atZone(ZoneId.of("UTC"));
        }
        if (dateValue instanceof ZonedDateTime zonedDateTime) {
            // ZonedDateTime 本身已经包含时区信息，可以平滑的转换成UTC
            return zonedDateTime.withZoneSameInstant(ZoneId.of("UTC"));
        }
        if (dateValue instanceof String dateString) {
            // 1. 尝试用 ISO_INSTANT 格式解析（例如 2025-08-26T00:00:00Z）
            return tryParse(() -> Instant.from(ISO_INSTANT_FORMATTER.parse(dateString)).atZone(ZoneId.of("UTC")))
                    // 2. 如果失败，尝试用本地日期时间格式解析（例如 2025-08-26 10:30:00）
                    .or(() -> tryParse(() -> LocalDateTime.parse(dateString, LOCAL_DATE_TIME_FORMATTER).atZone(ZoneId.of("UTC"))))
                    // 3. 如果失败，尝试用本地日期格式解析（例如 2025-08-26）这里使用 LocalDate.parse() 并转换为 ZonedDateTime
                    .or(() -> tryParse(() -> LocalDate.parse(dateString, LOCAL_DATE_FORMATTER).atStartOfDay(ZoneId.of("UTC"))))
                    .orElseThrow(() -> new IllegalArgumentException("Unsupported Date String format: " + dateValue));

        }
        throw new IllegalArgumentException("Unsupported Date format: " + dateValue.getClass().getName());
    }

    /**
     * 封装解析逻辑，返回 Optional 以便链式调用。
     */
    private static Optional<ZonedDateTime> tryParse(Supplier<ZonedDateTime> supplier) {
        try {
            return Optional.of(supplier.get());
        } catch (DateTimeParseException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }


    public static void main(String[] args) {
        // 获取系统默认时区
        ZoneId systemZone = ZoneId.systemDefault();
        log.info("系统默认时区: {}", systemZone);

        // 假设你接收到的一个 ZonedDateTime 对象，带有时区信息（例如：纽约时间）
        ZonedDateTime newYorkDateTime = ZonedDateTime.of(
                2025, 9, 22, 20, 30, 0, 0,
                ZoneId.of("America/New_York")
        );
        log.info("原始时区: {}", newYorkDateTime.getZone());
        log.info("原始时间: {}", newYorkDateTime);

        // 关键步骤：使用 withZoneSameInstant() 转换到 UTC
        ZonedDateTime utcDateTime = newYorkDateTime.withZoneSameInstant(ZoneId.of("UTC"));
        log.info("转换到【UTC】后的时区: {}", utcDateTime.getZone());
        log.info("转换到【UTC】后的时间: {}", utcDateTime);

        // 关键步骤：使用 withZoneSameInstant() 转换到 Asia/Shanghai
        ZonedDateTime shanghaiDateTime =  newYorkDateTime.withZoneSameInstant(ZoneId.of("Asia/Shanghai"));
        log.info("转换到【Asia/Shanghai】后的时区: {}", shanghaiDateTime.getZone());
        log.info("转换到【Asia/Shanghai】后的时间: {}", shanghaiDateTime);
    }
}