package com.vex.owl.queryer;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 操作符接口
 * <p>
 * 定义比较操作符的通用行为，所有操作符枚举需实现此接口
 *
 * @see OperatorEnum
 */
public interface Operator {

    /**
     * 获取操作符的字符串表示
     *
     * @return 操作符代码，如 EQ 返回 "EQ"
     */
    String code();

    /**
     * 操作符枚举
     *
     * @see Operator
     */
    @Getter
    @AllArgsConstructor
    enum OperatorEnum implements Operator {
        /**
         * 等于
         */
        EQ("EQ"),

        /**
         * 不等于
         */
        NEQ("NEQ"),

        /**
         * 大于
         */
        GT("GT"),

        /**
         * 大于等于
         */
        GTE("GTE"),

        /**
         * 小于
         */
        LT("LT"),

        /**
         * 小于等于
         */
        LTE("LTE"),

        /**
         * 模糊匹配
         */
        EXP("EXP"),

        /**
         * 模糊匹配（取反）
         */
        NOT_EXP("NOT_EXP"),

        /**
         * 在集合中
         */
        IN("IN"),

        /**
         * 不在集合中
         */
        NOT_IN("NOT_IN"),

        /**
         * 范围
         */
        BETWEEN("BETWEEN"),

        /**
         * 为空
         */
        IS_NULL("IS_NULL"),

        /**
         * 不为空
         */
        IS_NOT_NULL("IS_NOT_NULL");

        /**
         * 操作符代码
         */
        private final String code;

        @Override
        public String code() {
            return code;
        }
    }
}
