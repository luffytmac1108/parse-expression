package com.yxw.expression.resolve.expression;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;

import java.util.List;

/**
 * 逻辑表达式节点，如 AND, OR
 * @author luffytmac
 */
@Data
@JsonTypeName("LOGIC")
public class LogicNode extends BaseNode {

    //逻辑操作符：AND 或 OR
    private String operator;

    //子表达式节点列表，可以是logic节点，也可以是condition节点
    private List<BaseNode> children;

    // 可以在构造函数中初始化 type
    public LogicNode() {
        setType("LOGIC");
    }
}