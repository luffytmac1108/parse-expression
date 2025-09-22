package com.yxw.expression.resolve.expression;

import lombok.Data;

/**
 * 表达式中的一个操作数，定义了数据来源和字段名。
 * @author luffytmac
 */
@Data
public class Operand {

    /**
     * 数据来源：1、context（上下文中获取）；2、literal（固定值）；3、database
     */
    private String source;

    /**
     * 如果选择的是数据库，那么table就会有值
     */
    private String table;

    /**
     * 字段名，如果选择context或者database，都会定义字段名称，比如is_active
     */
    private String field;

    /**
     * 如果是固定值，这里放的是固定值的数据，可能是数字，可能是字符串，可能是日期
     */
    private Object value;
}