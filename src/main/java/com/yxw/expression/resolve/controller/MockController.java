package com.yxw.expression.resolve.controller;

import com.yxw.expression.resolve.request.EvaluationRequest;
import com.yxw.expression.resolve.service.ExpressionService;
import jakarta.annotation.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MockController {

    @Resource
    private ExpressionService expressionService;

    @PostMapping("/validate")
    public ResponseEntity<Boolean> validate(@RequestBody EvaluationRequest request) {
        try {
            boolean b = expressionService.doEvaluateExpression(request.getExpressionJson(), request.getContext());
            return ResponseEntity.ok(b);
        }catch (Exception e){
            return ResponseEntity.internalServerError().body(false);
        }
    }
}
