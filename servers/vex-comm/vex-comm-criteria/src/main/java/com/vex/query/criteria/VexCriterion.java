package com.vex.query.criteria;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = VexExpression.class, name = "expression"),
    @JsonSubTypes.Type(value = VexPredicate.class, name = "predicate")
})
public interface VexCriterion {
    
    default boolean checkEmpty() {
        return false;
    }
}