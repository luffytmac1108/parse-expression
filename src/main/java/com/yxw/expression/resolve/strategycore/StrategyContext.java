package com.yxw.expression.resolve.strategycore;

import com.google.common.collect.Sets;
import com.yxw.expression.resolve.enums.base.BaseEnum;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class StrategyContext implements BeanPostProcessor {

    Map<Class<?>, StrategyActionHolder> mapping = new HashMap<>();

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        //bean 是否实现了 IStrategy 接口
        if (bean instanceof IStrategy) {
            IStrategy strategy = (IStrategy) bean;
            // 使用strategy对象
            init(strategy);
        }
        return bean;
    }

    private void init(IStrategy strategy) {
        //获取策略对象实现的所有直接接口
        Class<?>[] interfaces = strategy.getClass().getInterfaces();
        // 只找一层，不支持接口多级继承 IStrategy
        for (Class<?> anInterface : interfaces) {
            //检查 anInterface 是否是 IStrategy 接口或其子接口
            if (IStrategy.class.isAssignableFrom(anInterface)) {
                addActionMapping(strategy, anInterface);
            }
        }
    }

    /**
     * 添加接口和对应实现类的关系
     */
    private void addActionMapping(IStrategy strategy, Class<?> actionInterface) {

        StrategyActionHolder<?> strategyActionHolder = mapping.computeIfAbsent(actionInterface, StrategyActionHolder::new);

        if (strategy.isDefault()) {
            strategyActionHolder.setDefaultStrategy(strategy);
        }
        BaseEnum[][] baseEnums = strategy.launchConditions();
        if (baseEnums == null || baseEnums.length == 0) {
            return;
        }

        for (BaseEnum[] baseEnum : baseEnums) {
            StrategyAction strategyAction = new StrategyAction<>(strategy, actionInterface, baseEnum);
            for (BaseEnum anEnum : baseEnum) {
                strategyActionHolder.addAction(anEnum, strategyAction);
            }
        }
    }

    /**
     * 获取对应的策略行为
     *
     * @param type   要获取的行为类型
     * @param params 枚举参数
     */
    public <T> T get(Class<T> type, BaseEnum... params) {

        StrategyActionHolder<T> strategyActionHolder = mapping.get(type);
        if (strategyActionHolder == null) {
            throw new IllegalArgumentException("类型[" + type.getName() + "] 还没有注册到 StrategyContext 中");
        }

        Set<BaseEnum> paramSet = null;

        for (BaseEnum param : params) {
            List<StrategyAction<T>> strategyActions = strategyActionHolder.getStrategyAction(param);
            if (CollectionUtils.isEmpty(strategyActions)) {
                continue;
            }
            for (StrategyAction<T> strategyAction : strategyActions) {
                // 是否是简单匹配，如果是的话直接返回了
                if (strategyAction.simpleMatch()) {
                    return strategyAction.getStrategy();
                }

                // 不是简单匹配，需要匹配 与的关系，把参数构建成一个 set 去匹配
                if (paramSet == null) {
                    paramSet = Sets.newHashSet(param);
                }
                if (strategyAction.match(paramSet)){
                    return strategyAction.getStrategy();
                }
            }
        }
        // 匹配不上，返回默认值
        return strategyActionHolder.getDefaultStrategy();
    }
}
