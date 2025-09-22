package com.yxw.expression.resolve.expression;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;

/**
 * 条件节点
 * @author luffytmac
 */
@Data
@JsonTypeName("CONDITION")
public class ConditionNode extends BaseNode {

    //左侧操作数
    private Operand left;

    //右侧操作数
    private Operand right;

    //比较操作符
    private String comparison;

    // 差值比较
    private Number value;

    //比较操作符，如 GREATER_THAN
    private String valueComparison;

    // 可以在构造函数中初始化 type
    public ConditionNode() {
        setType("CONDITION");
    }
}