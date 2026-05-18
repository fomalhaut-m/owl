package com.vex.owl.queryer;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 逻辑组合接口
 * <p>
 * 定义条件逻辑组合的通用行为，支持 AND、OR、NOT 三种逻辑运算
 *
 * @see LogicEnum
 */
public interface Logic {

    /**
     * 获取逻辑运算符的字符串表示
     *
     * @return 逻辑运算符代码，如 AND 返回 "AND"
     */
    String code();

    /**
     * 逻辑组合枚举
     *
     * @see Logic
     */
    @AllArgsConstructor
    enum LogicEnum implements Logic {
        /**
         * 条件与，多个条件同时满足
         */
        AND("AND"),

        /**
         * 条件或，多个条件满足其一
         */
        OR("OR"),

        /**
         * 条件非，对条件取反
         */
        NOT("NOT");

        /**
         * 逻辑运算符代码
         */
        private final String code;

        @Override
        public String code() {
            return code;
        }
    }
}
