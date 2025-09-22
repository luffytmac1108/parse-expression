package com.yxw.expression.resolve.expression;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;

/**
 * 表达式节点的基类
 * @author luffytmac
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = LogicNode.class, name = "LOGIC"),
        @JsonSubTypes.Type(value = ConditionNode.class, name = "CONDITION")
})

@Data
public abstract class BaseNode {
    private String type;
}
