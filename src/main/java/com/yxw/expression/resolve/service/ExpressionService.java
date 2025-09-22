package com.yxw.expression.resolve.service;

import cn.hutool.core.util.StrUtil;
import com.yxw.expression.resolve.expression.BaseNode;
import com.yxw.expression.resolve.expression.ConditionEvaluator;
import com.yxw.expression.resolve.utils.JsonUtils;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;

@Service
public class ExpressionService {

    private static final Logger log = LoggerFactory.getLogger(ExpressionService.class);

    @Resource
    private ConditionEvaluator conditionEvaluator;

    public boolean doEvaluateExpression(String expressionJson, Map<String, Object> contextMap) {
        try {
            //如果没有配置表达式，则直接返回true，代表逻辑继续往下执行
            if (StrUtil.isEmpty(expressionJson)) {
                return true;
            }
            // 解析表达式
            BaseNode expression = JsonUtils.jsonToObject(expressionJson, BaseNode.class);
            if(Objects.isNull(expression)){
                log.error("解析模板中的表达式失败, expressionJson: {}", expressionJson);
                return false;
            }
            // 执行表达式，如果表达式返回true，代表逻辑继续往下执行，返回false则代表逻辑结束
            return conditionEvaluator.evaluate(expression, contextMap);
        } catch (Exception e) {
            log.error("Error evaluating expression, expressionJson: {}, exception info: {}", expressionJson, e.getMessage());
            return false;
        }
    }
}