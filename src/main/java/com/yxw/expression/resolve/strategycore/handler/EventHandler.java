package com.yxw.expression.resolve.strategycore.handler;

import com.yxw.expression.resolve.strategycore.IStrategy;

/**
 * 这是一个策略接口，用于处理所有的外部系统发送过来的事件
 *
 * 为什么会有这个策略接口？
 * 系统收到每一种事件的处理逻辑都不一样，所以需要一个策略接口，根据事件类型，采用不同的策略处理
 *
 * @author: luffftmac
 * @date: 2020/8/14 10:28
 */
public interface EventHandler extends IStrategy {


    <T> T findByKey(String keyId);
}
