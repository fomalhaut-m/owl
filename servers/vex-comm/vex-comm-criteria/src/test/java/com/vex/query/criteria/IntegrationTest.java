package com.vex.query.criteria;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("集成测试")
class IntegrationTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("完整查询流程：构建 -> 序列化 -> 反序列化 -> 验证")
    void testCompleteQueryFlow() throws Exception {
        VexQueryCriteria original = VexQueryCriteria.of()
            .select("id", "username", "email", "age")
            .exclude("password", "salt")
            .filter(VexPredicate.and(
                VexExpression.eq("status", "ACTIVE"),
                VexExpression.gte("age", 18),
                VexExpression.lte("age", 65),
                VexPredicate.or(
                    VexExpression.exp("username", "%张%"),
                    VexExpression.exp("username", "%李%")
                ),
                VexExpression.in("city", new String[]{"北京", "上海", "广州"})
            ))
            .orderBy(
                VexSortOrder.desc("createTime"),
                VexSortOrder.asc("username")
            )
            .paging(VexPageRequest.of(0, 20));

        String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(original);
        assertNotNull(json);

        VexQueryCriteria deserialized = objectMapper.readValue(json, VexQueryCriteria.class);
        assertNotNull(deserialized);
        
        assertArrayEquals(original.getSelect(), deserialized.getSelect());
        assertArrayEquals(original.getExclude(), deserialized.getExclude());
        
        assertNotNull(deserialized.getFilter());
        assertEquals(VexLogic.and, deserialized.getFilter().getLogic());
        assertEquals(5, deserialized.getFilter().getExpressions().length);
        
        VexCriterion firstExpr = deserialized.getFilter().getExpressions()[0];
        assertInstanceOf(VexExpression.class, firstExpr);
        VexExpression statusExpr = (VexExpression) firstExpr;
        assertEquals("status", statusExpr.getField());
        assertEquals(VexOperator.eq, statusExpr.getOp());
        assertEquals("ACTIVE", statusExpr.getValue());
        
        VexCriterion secondExpr = deserialized.getFilter().getExpressions()[1];
        assertInstanceOf(VexExpression.class, secondExpr);
        VexExpression ageExpr = (VexExpression) secondExpr;
        assertEquals("age", ageExpr.getField());
        assertEquals(VexOperator.gte, ageExpr.getOp());
        assertEquals(18, ageExpr.getValue());
    }
}