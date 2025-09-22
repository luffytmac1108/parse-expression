package com.yxw.expression.resolve.strategycore;

import com.yxw.expression.resolve.enums.base.BaseEnum;
import lombok.Getter;

import java.util.Set;

@Getter
public class StrategyAction<T> {

    private final BaseEnum[] baseEnum;

    T strategy;

    public StrategyAction(Object strategy, Class<T> type, BaseEnum[] baseEnum) {
        this.strategy = (T) strategy;
        this.baseEnum = baseEnum;
    }

    public boolean simpleMatch() {
        return baseEnum.length == 1;
    }

    public boolean match(Set<BaseEnum> paramSet) {
        for (BaseEnum anEnum : baseEnum) {
            if (!paramSet.contains(anEnum)) {
                return false;
            }
        }
        return true;
    }
}
