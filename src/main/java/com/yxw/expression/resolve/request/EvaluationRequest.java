package com.yxw.expression.resolve.request;

import lombok.Data;

import java.util.Map;

@Data
public class EvaluationRequest {

    private Map<String, Object> context;

    private String expressionJson;
}