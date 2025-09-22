package com.yxw.expression.resolve.enums;

import cn.hutool.core.util.StrUtil;

import java.util.Optional;

public enum ComparisonEnum {

    EQUAL("EQUAL", "等于"),
    NOT_EQUAL("NOT_EQUAL", "不等于"),
    GREATER_THAN("GREATER_THAN", "大于"),
    LESS_THAN("LESS_THAN", "小于"),
    GREATER_THAN_OR_EQUAL("GREATER_THAN_OR_EQUAL", "大于等于"),
    LESS_THAN_OR_EQUAL("LESS_THAN_OR_EQUAL", "小于等于"),
    CONTAINS("CONTAINS", "包含"),
    STARTS_WITH("STARTS_WITH", "以...开头"),
    ENDS_WITH("ENDS_WITH", "以...结尾"),

    // 两个日期的年|月|日比较
    EQUAL_YEAR("EQUAL_YEAR", "年相等"),
    EQUAL_MONTH("EQUAL_MONTH", "月相等"),
    EQUAL_DAY("EQUAL_DAY", "日相等"),

    // 两个日期差值比较
    DIFFERENCE_IN_YEARS("DIFFERENCE_IN_YEARS", "年差值"),
    DIFFERENCE_IN_MONTHS("DIFFERENCE_IN_MONTHS", "月差值"),
    DIFFERENCE_IN_DAYS("DIFFERENCE_IN_DAYS", "日差值"),

    // 数值差值比较
    DIFFERENCE_IN_VALUE("DIFFERENCE_IN_VALUE", "数值差值"),

    // null和empty
    IS_NULL("IS_NULL", "为空"),
    IS_NOT_NULL("IS_NOT_NULL", "不为空"),
    IS_EMPTY("IS_EMPTY", "字符串为空"),
    IS_NOT_EMPTY("IS_NOT_EMPTY", "字符串不为空"),

    ;

    private final String code;
    private final String desc;


    ComparisonEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static Optional<ComparisonEnum> getByCode(String code) {
        if(StrUtil.isEmpty(code)){
            return Optional.empty();
        }
        for (ComparisonEnum value : ComparisonEnum.values()) {
            if (code.equals(value.getCode())) {
                return Optional.of(value);
            }
        }
        return Optional.empty();
    }
}
