package com.yxw.expression.resolve.utils;

import lombok.extern.slf4j.Slf4j;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class BeanToMapConverter {
    /**
     * 将驼峰命名转换为下划线命名。
     * 例如：ticketNo -> ticket_no
     */
    public static String camelToSnakeCase(String camelCaseStr) {
        StringBuilder sb = new StringBuilder();
        for (char c : camelCaseStr.toCharArray()) {
            if (Character.isUpperCase(c)) {
                sb.append("_").append(Character.toLowerCase(c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * 将Java Bean转换为下划线命名为key的Map
     */
    public static Map<String, Object> beanToSnakeCaseMap(Object bean) {
        Map<String, Object> map = new HashMap<>();
        if(bean == null){
            return map;
        }
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(bean.getClass());
            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
            for (PropertyDescriptor property : propertyDescriptors) {
                String propertyName = property.getName();
                if (!"class".equals(propertyName)) {
                    Method reader = property.getReadMethod();
                    if (reader != null) {
                        //比如：ticketNo -> ticket_no
                        String snakeCaseName = camelToSnakeCase(propertyName);
                        Object value = reader.invoke(bean);
                        map.put(snakeCaseName, value);
                    }
                }
            }
        } catch (Exception e) {
            log.error("beanToSnakeCaseMap error {}" ,e.getMessage(), e);
            return map;
        }
        return map;
    }
}