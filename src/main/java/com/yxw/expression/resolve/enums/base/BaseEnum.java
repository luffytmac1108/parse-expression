package com.yxw.expression.resolve.enums.base;

import com.fasterxml.jackson.annotation.JsonValue;

public interface BaseEnum {

    @JsonValue
    String getValue();

    default String getExcelValue() {
        String value = getValue();
        if (value == null) {
            return null;
        }
        return value.toUpperCase();
    }

    default boolean match(String value) {
        String enumCode = getValue();
        if (value == null || enumCode == null) {
            return false;
        }
        value = value.toUpperCase();
        return enumCode.toUpperCase().equals(value);
    }
}