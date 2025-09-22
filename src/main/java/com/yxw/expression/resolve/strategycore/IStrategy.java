package com.yxw.expression.resolve.strategycore;

import com.yxw.expression.resolve.enums.base.BaseEnum;

public interface IStrategy {

    /**
     * 策略的发动条件
     * 已枚举为发动条件，双层数组，一层数组中的枚举为与关系，二层数组中的为或关系
     *
     * [{A,B}, {C}] ---> 需要枚举 A和B 同时拥有才能命中，或者仅拥有C也能命中
     */
    BaseEnum[][] launchConditions();

    /**
     * 是否是默认的处理器
     *
     * @return
     */
    default boolean isDefault() {
        return false;
    }
}
