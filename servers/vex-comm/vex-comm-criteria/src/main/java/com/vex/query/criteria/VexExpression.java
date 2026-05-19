package com.vex.query.criteria;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class VexExpression implements VexCriterion {
    private String field;
    private VexOperator op;
    private Object value;

    public static VexExpression of(String field, VexOperator op, Object value) {
        VexExpression expr = new VexExpression();
        expr.field = field;
        expr.op = op;
        expr.value = value;
        return expr;
    }

    public static VexExpression eq(String field, Object value) {
        return of(field, VexOperator.eq, value);
    }

    public static VexExpression neq(String field, Object value) {
        return of(field, VexOperator.neq, value);
    }

    public static VexExpression gt(String field, Object value) {
        return of(field, VexOperator.gt, value);
    }

    public static VexExpression gte(String field, Object value) {
        return of(field, VexOperator.gte, value);
    }

    public static VexExpression lt(String field, Object value) {
        return of(field, VexOperator.lt, value);
    }

    public static VexExpression lte(String field, Object value) {
        return of(field, VexOperator.lte, value);
    }

    public static VexExpression exp(String field, Object value) {
        return of(field, VexOperator.exp, value);
    }

    public static VexExpression notExp(String field, Object value) {
        return of(field, VexOperator.not_exp, value);
    }

    public static VexExpression in(String field, Object value) {
        return of(field, VexOperator.in, value);
    }

    public static VexExpression notIn(String field, Object value) {
        return of(field, VexOperator.not_in, value);
    }

    public static VexExpression between(String field, Object lo, Object hi) {
        return of(field, VexOperator.between, new Object[]{lo, hi});
    }

    public static VexExpression isNull(String field) {
        return of(field, VexOperator.is_null, null);
    }

    public static VexExpression isNotNull(String field) {
        return of(field, VexOperator.is_not_null, null);
    }
}