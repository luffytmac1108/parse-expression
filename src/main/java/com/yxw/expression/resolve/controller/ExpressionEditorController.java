package com.yxw.expression.resolve.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ExpressionEditorController {

    @GetMapping("/editor11")
    public String showExpressionEditor11() {
        return "expression_editor11";
    }

    @GetMapping("/cube21")
    public String cube21() {
        return "cube21";
    }
}