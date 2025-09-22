package com.yxw.expression.resolve.strategycore;

import com.yxw.expression.resolve.enums.base.BaseEnum;
import lombok.Getter;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StrategyActionHolder<T> {

    Map<BaseEnum, List<StrategyAction<T>>> actionMapping = new HashMap<>();

    @Getter
    T defaultStrategy;

    Class<T> type;

    public StrategyActionHolder(Class<T> type) {
        this.type = type;
        this.defaultStrategy = genDefaultStrategy(type);
    }

    private T genDefaultStrategy(Class<T> type) {
        return (T) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class[]{type}, (proxy, method, args) -> {
            String name = method.getName();
            if ("toString".equals(name)){
                return type.getName() + "_emptyProxy";
            }
            return null;
        });
    }

    public void addAction(BaseEnum baseEnum, StrategyAction<T> strategyAction) {
        actionMapping.computeIfAbsent(baseEnum, key -> new ArrayList<>()).add(strategyAction);
    }

    public void setDefaultStrategy(Object defaultStrategy) {
        this.defaultStrategy = (T) defaultStrategy;
    }

    public List<StrategyAction<T>> getStrategyAction(BaseEnum baseEnum) {
        return actionMapping.get(baseEnum);
    }
}
