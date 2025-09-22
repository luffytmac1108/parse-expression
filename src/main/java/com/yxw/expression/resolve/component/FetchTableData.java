package com.yxw.expression.resolve.component;

import com.yxw.expression.resolve.enums.EventTypeEnum;
import com.yxw.expression.resolve.strategycore.StrategyContext;
import com.yxw.expression.resolve.strategycore.handler.EventHandler;
import com.yxw.expression.resolve.utils.BeanToMapConverter;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import java.util.Map;

/**
 * 利用策略模式进行数据获取，每个event对应不同的表，每张表对应一个策略，策略中定义了获取数据的方式
 * @author luffytmac
 */
@Component
public class FetchTableData {

    @Resource
    private StrategyContext strategyContext;

    /**
     * 根据表名和条件获取数据，最后将实体数据转换为一个map数据返回
     */
    public Map<String, Object> fetchData(EventTypeEnum event, Map<String, Object> contextMap) {
        Assert.notNull(event, "event can not be null");
        //纳入处理的表，可能每一张获取数据的方式都有所不用，所以这里需要按照event类型进行数据处理

        if(event == EventTypeEnum.TABLE1){
            String id = (String) contextMap.get("table1_id");
            Object tableData = strategyContext.get(EventHandler.class, EventTypeEnum.TABLE1).findByKey(id);
            return BeanToMapConverter.beanToSnakeCaseMap(tableData);
        } else if (event == EventTypeEnum.TABLE2){
            String id = (String) contextMap.get("table2_id");
            Object tableData = strategyContext.get(EventHandler.class, EventTypeEnum.TABLE2).findByKey(id);
            return BeanToMapConverter.beanToSnakeCaseMap(tableData);
        } else {
            //其他的event表
            String otherId = (String) contextMap.get("other_id");
            Object otherTableData = strategyContext.get(EventHandler.class, event).findByKey(otherId);
            return BeanToMapConverter.beanToSnakeCaseMap(otherTableData);
        }

        //如果后面还有其他的表，或者是按照ID进行查询，在这里处理就行
    }
}
