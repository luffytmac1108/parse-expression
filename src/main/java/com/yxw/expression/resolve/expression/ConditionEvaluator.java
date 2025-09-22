package com.yxw.expression.resolve.expression;

import cn.hutool.core.convert.Convert;

import cn.hutool.core.util.StrUtil;
import com.yxw.expression.resolve.component.FetchTableData;
import com.yxw.expression.resolve.enums.ComparisonEnum;
import com.yxw.expression.resolve.enums.EventTypeEnum;
import com.yxw.expression.resolve.utils.DateConverter;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.thymeleaf.util.StringUtils;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Component
public class ConditionEvaluator {

    private static final Logger log = LoggerFactory.getLogger(ConditionEvaluator.class);

    private static final String CONTEXT = "context";
    private static final String LITERAL = "literal";
    private static final String DATABASE = "database";

    private static final String OPERATOR_AND = "AND";
    private static final String OPERATOR_OR = "OR";

    @Resource
    private FetchTableData fetchTableData;

    /**
     * 对整个表达式树进行求值
     *
     * @param node        表达式树的根节点，可以是 LogicNode 或 ConditionNode
     * @param contextData 上下文数据，用于获取 "context" 来源的值
     * @return 表达式的布尔值结果
     */
    public boolean evaluate(BaseNode node, Map<String, Object> contextData) {
        if (node instanceof LogicNode lNode) {
            return evaluateLogicNode(lNode, contextData);
        } else if (node instanceof ConditionNode cNode) {
            // 这里调用处理单个条件的逻辑
            // 这是一个简单的示例，实际实现需要更复杂
            return evaluateConditionNode(cNode, contextData);
        }
        throw new IllegalArgumentException("Unsupported node type: " + node.getClass().getName());
    }

    /**
     * 逻辑节点判断
     */
    private boolean evaluateLogicNode(LogicNode logicNode, Map<String, Object> contextData) {
        String operator = logicNode.getOperator();
        List<BaseNode> children = logicNode.getChildren();

        if (operator.equals(OPERATOR_AND)) {
            // AND 操作符的逻辑：如果任何一个子节点为 false，则整个表达式为 false
            for (BaseNode child : children) {
                if (!evaluate(child, contextData)) {
                    return false;
                }
            }
            return true;
        } else if (operator.equals(OPERATOR_OR)) {
            // OR 操作符的逻辑：如果任何一个子节点为 true，则整个表达式为 true
            for (BaseNode child : children) {
                if (evaluate(child, contextData)) {
                    return true;
                }
            }
            return false;
        } else {
            throw new IllegalArgumentException("Unsupported operator: " + operator);
        }
    }

    /**
     * 条件节点判断
     * @param node
     * @param context
     * @return
     */
    private boolean evaluateConditionNode(ConditionNode node, Map<String, Object> context) {
        try {
            // 获取左右两个操作数的实际值
            Object leftValue = getOperandValue(node.getLeft(), context);
            Object rightValue = getOperandValue(node.getRight(), context);
            ComparisonEnum comparisonEnum = ComparisonEnum.getByCode(node.getComparison().toUpperCase())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid comparison operator: " + node.getComparison()));
            return switch (comparisonEnum) {
                case EQUAL -> customizeEquals(leftValue, rightValue);
                case NOT_EQUAL -> !Objects.equals(leftValue, rightValue);

                case GREATER_THAN -> compareValues(leftValue, rightValue) > 0;
                case LESS_THAN -> compareValues(leftValue, rightValue) < 0;
                case GREATER_THAN_OR_EQUAL -> compareValues(leftValue, rightValue) >= 0;
                case LESS_THAN_OR_EQUAL -> compareValues(leftValue, rightValue) <= 0;

                // 字符串 包含、开头、结尾
                case CONTAINS, STARTS_WITH, ENDS_WITH ->
                        containsStartWithEndWith(leftValue, rightValue, comparisonEnum);

                // 两个日期的年|月|日比较
                case EQUAL_YEAR -> compareDatesByUnit(leftValue, rightValue, ChronoUnit.YEARS);
                case EQUAL_MONTH -> compareDatesByUnit(leftValue, rightValue, ChronoUnit.MONTHS);
                case EQUAL_DAY -> compareDatesByUnit(leftValue, rightValue, ChronoUnit.DAYS);

                // 两个日期差值比较
                case DIFFERENCE_IN_YEARS ->
                        compareDateDifference(leftValue, rightValue, node.getValue(), node.getValueComparison(), ChronoUnit.YEARS);
                case DIFFERENCE_IN_MONTHS ->
                        compareDateDifference(leftValue, rightValue, node.getValue(), node.getValueComparison(), ChronoUnit.MONTHS);
                case DIFFERENCE_IN_DAYS ->
                        compareDateDifference(leftValue, rightValue, node.getValue(), node.getValueComparison(), ChronoUnit.DAYS);

                // 数值差值比较
                case DIFFERENCE_IN_VALUE ->
                        compareNumericDifference(leftValue, rightValue, node.getValue(), node.getValueComparison());

                // 新增：NULL 和 EMPTY 检查
                case IS_NULL -> leftValue == null;
                case IS_NOT_NULL -> leftValue != null;
                case IS_EMPTY -> isRightString(leftValue) && ((String) leftValue).isEmpty();
                case IS_NOT_EMPTY -> isRightString(leftValue) && !((String) leftValue).isEmpty();
            };
        } catch (Exception e) {
            //如果在比较过程中发生异常，返回false
            log.warn("Exception occurred during evaluation: {}", e.getMessage());
            return false;
        }
    }

    private boolean customizeEquals(Object obj1, Object obj2){
        //如果是判断两个对象是否相等，我们尽可能的去兼容判断
        // 比如字符串 "123456" 和 123456 我们认为是相等，因为前端我们没有去指定数据类型
        // 所以当比较双方类型不一致的时候，我们尝试将其转换为字符串进行比较一下
        if(obj1 != null && obj2 != null && !obj1.getClass().equals(obj2.getClass())) {
            String obj1Str = Convert.toStr(obj1);
            String obj2Str = Convert.toStr(obj2);
            if (Objects.equals(obj1Str, obj2Str)) {
                return true;
            }
        }
        return Objects.equals(obj1, obj2);
    }

    /**
     * 根据 Operand 获取其对应的实际值
     */
    private Object getOperandValue(Operand operand, Map<String, Object> context) {
        if(Objects.isNull(operand) || Objects.isNull(context) || context.isEmpty()){
            return null;
        }
        if (CONTEXT.equalsIgnoreCase(operand.getSource())) {
            //如果context中不包含字段，直接抛出异常，对应就会返回false
            if(!context.containsKey(operand.getField())){
                throw new IllegalArgumentException("Context does not contain key: " + operand.getField());
            }
            //从context上下文中获取
            return context.get(operand.getField());
        } else if (LITERAL.equalsIgnoreCase(operand.getSource())) {
            //从字面量中获取
            return operand.getValue();
        } else if (DATABASE.equalsIgnoreCase(operand.getSource())){
            //从数据库中获取，使用策略模式，将配置的表通过唯一健获取相关的数据
            if(StrUtil.isEmpty(operand.getTable()) || StrUtil.isEmpty(operand.getField())) {
                throw new IllegalArgumentException("Table or field is empty");
            }
            //根据表名关联到相关的事件
            Optional<EventTypeEnum> etOpt = EventTypeEnum.getByTable(operand.getTable());
            if(etOpt.isEmpty()){
                throw new IllegalArgumentException("Unsupported table name: " + operand.getTable());
            }
            //根据表名和条件获取数据
            Map<String, Object> data = fetchTableData.fetchData(etOpt.get(), context);
            if(Objects.isNull(data) || data.isEmpty()){
                return null;
            }
            return data.get(operand.getField());
        } else {
            throw new IllegalArgumentException("Unsupported operand source: " + operand.getSource());
        }
    }

    private int compareValues(Object obj1, Object obj2) {
        if (obj1 == null && obj2 == null) {
            // 两个都为 null，认为它们相等
            return 0;
        }
        if (obj1 == null) {
            // 左边为 null，右边不为 null，则左边小于右边
            return -1;
        }
        if (obj2 == null) {
            // 左边不为 null，右边为 null，则左边大于右边
            return 1;
        }

        // 1. 处理数字类型
        if (obj1 instanceof Number num1 && obj2 instanceof Number num2) {
            return Double.compare(num1.doubleValue(), num2.doubleValue());
        }
        // 2. 处理通用字符串类型
        if (obj1 instanceof String str1 && obj2 instanceof String str2) {
            return str1.compareTo(str2);
        }
        // 3. 处理布尔类型
        if (obj1 instanceof Boolean b1 && obj2 instanceof Boolean b2) {
            return Boolean.compare(b1, b2);
        }

        // 4. 处理日期
        try{
            ZonedDateTime date1 = DateConverter.convertToZonedDateTime(obj1);
            ZonedDateTime date2 = DateConverter.convertToZonedDateTime(obj2);
            return date1.compareTo(date2);
        } catch (Exception e) {
            //do nothing
        }

        // 如果类型不匹配或不支持，直接抛出异常
        throw new IllegalArgumentException("Unsupported types for comparison: 【" + obj1 + "】 and 【" + obj2 + "】");
    }

    /**
     * 专门用于日期粒度比较的辅助方法。
     */
    private boolean compareDatesByUnit(Object obj1, Object obj2, ChronoUnit unit) {
        ZonedDateTime date1 = DateConverter.convertToZonedDateTime(obj1);
        ZonedDateTime date2 = DateConverter.convertToZonedDateTime(obj2);
        if (date1 == null && date2 == null) {
            // 两个都为 null，认为它们相等
            return true;
        }
        if (date1 == null || date2 == null) {
            // 一边为null，另一边不为 null，则不相等
            return false;
        }
        return switch (unit) {
            case YEARS -> date1.getYear() == date2.getYear();
            case MONTHS -> date1.getMonth() == date2.getMonth();
            case DAYS -> date1.getDayOfMonth() == date2.getDayOfMonth();
            default -> throw new IllegalArgumentException("Unsupported date comparison unit: " + unit);
        };
    }

    /**
     * 比较两个日期之间的差值。
     */
    private boolean compareDateDifference(Object obj1, Object obj2, Number comparedValue, String valueComparison, ChronoUnit unit) {
        ComparisonEnum comparisonEnum = ComparisonEnum.getByCode(valueComparison)
                .orElseThrow(() -> new IllegalArgumentException("Unsupported value comparison operator: " + valueComparison));
        ZonedDateTime date1 = DateConverter.convertToZonedDateTime(obj1);
        ZonedDateTime date2 = DateConverter.convertToZonedDateTime(obj2);
        if (date1 == null && date2 == null) {
            // 两个都为 null，认为它们相等
            return true;
        }
        if (date1 == null || date2 == null) {
            // 一边为null，另一边不为 null，则不相等
            return false;
        }
        long diff = unit.between(date2, date1);
        return switch (comparisonEnum) {
            case EQUAL -> diff == comparedValue.longValue();
            case NOT_EQUAL -> diff != comparedValue.longValue();
            case GREATER_THAN -> diff > comparedValue.longValue();
            case LESS_THAN -> diff < comparedValue.longValue();
            case GREATER_THAN_OR_EQUAL -> diff >= comparedValue.longValue();
            case LESS_THAN_OR_EQUAL -> diff <= comparedValue.longValue();
            default -> throw new IllegalArgumentException("Unsupported value comparison operator: " + valueComparison);
        };
    }

    /**
     * 比较两个数值之间的差值。
     *
     * @param obj1 第一个数值对象
     * @param obj2 第二个数值对象
     * @param comparedValue 用于比较的固定数值
     * @param valueComparison 比较操作符，如 "GREATER_THAN"
     * @return 比较结果
     */
    private boolean compareNumericDifference(Object obj1, Object obj2, Number comparedValue, String valueComparison) {
        //差值比较，如果一个对象为 null，没办法完成数值比较，直接返回 false
        if (obj1 == null || obj2 == null) {
            return false;
        }
        ComparisonEnum comparisonEnum = ComparisonEnum.getByCode(valueComparison)
                .orElseThrow(() -> new IllegalArgumentException("Unsupported value comparison operator: " + valueComparison));
        if (!(obj1 instanceof Number) || !(obj2 instanceof Number)) {
            throw new IllegalArgumentException("Numeric difference comparison requires number types.");
        }
        double diff = Math.abs(((Number) obj1).doubleValue() - ((Number) obj2).doubleValue());
        return switch (comparisonEnum) {
            case EQUAL -> diff == comparedValue.doubleValue();
            case NOT_EQUAL -> diff != comparedValue.doubleValue();
            case GREATER_THAN -> diff > comparedValue.doubleValue();
            case LESS_THAN -> diff < comparedValue.doubleValue();
            case GREATER_THAN_OR_EQUAL -> diff >= comparedValue.doubleValue();
            case LESS_THAN_OR_EQUAL -> diff <= comparedValue.doubleValue();
            default -> throw new IllegalArgumentException("Unsupported value comparison operator: " + valueComparison);
        };
    }

    private boolean containsStartWithEndWith(Object leftValue, Object rightValue, ComparisonEnum comparisonEnum){
        // 安全地将 Object 转换为 String
        String leftStr = safeToString(leftValue);
        String rightStr = safeToString(rightValue);
        return switch (comparisonEnum) {
            case CONTAINS -> StringUtils.contains(leftStr, rightStr);
            case STARTS_WITH -> StringUtils.startsWith(leftStr, rightStr);
            case ENDS_WITH -> StringUtils.endsWith(leftStr, rightStr);
            default -> throw new IllegalArgumentException("Unsupported String comparison operator: " + comparisonEnum);
        };
    }

    private boolean isRightString(Object obj) {
        if(obj == null) {
            return false;
        }
        return obj instanceof String;
    }

    public String safeToString(Object value) {
        if (value == null) {
            return null;
        }
        return value.toString();
    }
}
