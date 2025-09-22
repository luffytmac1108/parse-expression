package com.yxw.expression.resolve.enums;

import cn.hutool.core.util.StrUtil;
import com.yxw.expression.resolve.enums.base.BaseEnum;
import lombok.Getter;

import java.util.Optional;

public enum EventTypeEnum implements BaseEnum {

    TABLE1("table1", "table1", "table1"),
    TABLE2("table2", "table2", "table2"),

    ;

    @Getter
    private final String name;

    @Getter
    private final String table;

    @Getter
    private final String code;

    EventTypeEnum(String name, String table, String code) {
        this.name = name;
        this.table = table;
        this.code = code;
    }

    public static Optional<EventTypeEnum> getByCode(String code) {
        if(StrUtil.isEmpty(code)){
            return Optional.empty();
        }
        for (EventTypeEnum value : EventTypeEnum.values()) {
            if (code.equals(value.getCode())) {
                return Optional.of(value);
            }
        }
        return Optional.empty();
    }

    public static Optional<EventTypeEnum> getByTable(String table) {
        if(StrUtil.isEmpty(table)){
            return Optional.empty();
        }
        for (EventTypeEnum value : EventTypeEnum.values()) {
            if (table.equals(value.getTable())) {
                return Optional.of(value);
            }
        }
        return Optional.empty();
    }

    @Override
    public String getValue() {
        return String.valueOf(code);
    }
}
