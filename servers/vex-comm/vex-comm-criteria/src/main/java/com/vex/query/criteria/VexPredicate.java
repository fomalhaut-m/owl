package com.vex.query.criteria;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class VexPredicate implements VexCriterion {
    private VexLogic logic = VexLogic.and;
    private VexCriterion[] expressions;

    @Override
    public boolean checkEmpty() {
        return expressions == null || expressions.length == 0;
    }

    public static VexPredicate of(VexCriterion... expressions) {
        VexPredicate p = new VexPredicate();
        p.logic = VexLogic.and;
        p.expressions = expressions;
        return p;
    }

    public static VexPredicate and(VexCriterion... expressions) {
        VexPredicate p = new VexPredicate();
        p.logic = VexLogic.and;
        p.expressions = expressions;
        return p;
    }

    public static VexPredicate or(VexCriterion... expressions) {
        VexPredicate p = new VexPredicate();
        p.logic = VexLogic.or;
        p.expressions = expressions;
        return p;
    }

}