package com.vex.query.criteria;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JSON 序列化测试")
class JsonSerializationTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Nested
    @DisplayName("VexExpression 序列化")
    class ExpressionSerialization {

        @Test
        @DisplayName("序列化等于表达式")
        void testSerializeEqExpression() throws Exception {
            VexExpression expr = VexExpression.eq("status", "ACTIVE");
            String json = objectMapper.writeValueAsString(expr);

            assertNotNull(json);
            assertTrue(json.contains("\"type\":\"expression\""));
            assertTrue(json.contains("\"field\":\"status\""));
            assertTrue(json.contains("\"op\":\"eq\""));
            assertTrue(json.contains("\"value\":\"ACTIVE\""));
        }

        @Test
        @DisplayName("反序列化等于表达式")
        void testDeserializeEqExpression() throws Exception {
            String json = """
                {
                    "type":"expression",
                    "field":"status",
                    "op":"eq",
                    "value":"ACTIVE"
                }
                """;
            VexExpression expr = objectMapper.readValue(json, VexExpression.class);

            assertEquals("status", expr.getField());
            assertEquals(VexOperator.eq, expr.getOp());
            assertEquals("ACTIVE", expr.getValue());
        }
    }

    @Nested
    @DisplayName("VexPredicate 序列化")
    class PredicateSerialization {

        @Test
        @DisplayName("序列化 AND 组合")
        void testSerializeAndPredicate() throws Exception {
            VexPredicate predicate = VexPredicate.and(
                VexExpression.eq("status", "ACTIVE"),
                VexExpression.gte("age", 18)
            );

            String json = objectMapper.writeValueAsString(predicate);
            assertNotNull(json);
            assertTrue(json.contains("\"type\":\"predicate\""));
            assertTrue(json.contains("\"logic\":\"and\""));
        }

        @Test
        @DisplayName("反序列化嵌套组合")
        void testDeserializeNestedPredicate() throws Exception {
            String json = """
                {
                    "type":"predicate",
                    "logic":"and",
                    "expressions":[
                        {"type":"expression","field":"status","op":"eq","value":"ACTIVE"},
                        {
                            "type":"predicate",
                            "logic":"or",
                            "expressions":[
                                {"type":"expression","field":"name","op":"exp","value":"%张%"},
                                {"type":"expression","field":"name","op":"exp","value":"%李%"}
                            ]
                        }
                    ]
                }
                """;
            VexPredicate predicate = objectMapper.readValue(json, VexPredicate.class);

            assertEquals(VexLogic.and, predicate.getLogic());
            assertEquals(2, predicate.getExpressions().length);
            
            VexCriterion second = predicate.getExpressions()[1];
            assertInstanceOf(VexPredicate.class, second);
            VexPredicate nested = (VexPredicate) second;
            assertEquals(VexLogic.or, nested.getLogic());
        }
    }

    @Nested
    @DisplayName("VexQueryCriteria 完整序列化")
    class QueryCriteriaSerialization {

        @Test
        @DisplayName("完整查询序列化")
        void testSerializeQueryCriteria() throws Exception {
            VexQueryCriteria query = VexQueryCriteria.of()
                .select("id", "username")
                .filter(VexPredicate.and(
                    VexExpression.eq("status", "ACTIVE"),
                    VexExpression.gte("age", 18)
                ))
                .orderBy(VexSortOrder.desc("createTime"))
                .paging(VexPageRequest.of(0, 20));

            String json = objectMapper.writeValueAsString(query);
            assertNotNull(json);
        }

        @Test
        @DisplayName("完整查询反序列化")
        void testDeserializeQueryCriteria() throws Exception {
            String json = """
                {
                    "select":["id","username"],
                    "filter":{
                        "type":"predicate",
                        "logic":"and",
                        "expressions":[
                            {"type":"expression","field":"status","op":"eq","value":"ACTIVE"}
                        ]
                    },
                    "orderBy":[{"property":"createTime","direction":"DESC"}],
                    "paging":{"page":0,"size":20}
                }
                """;
            VexQueryCriteria query = objectMapper.readValue(json, VexQueryCriteria.class);

            assertEquals(2, query.getSelect().length);
            assertEquals(VexLogic.and, query.getFilter().getLogic());
            assertEquals(1, query.getOrderBy().length);
            assertEquals(0, query.getPaging().getPage());
            assertEquals(20, query.getPaging().getSize());
        }
    }

    @Nested
    @DisplayName("VexSortOrder 和 VexPageRequest 序列化")
    class SortAndPageSerialization {

        @Test
        @DisplayName("序列化排序")
        void testSerializeSortOrder() throws Exception {
            VexSortOrder sort = VexSortOrder.desc("createTime");
            String json = objectMapper.writeValueAsString(sort);
            
            assertTrue(json.contains("\"property\":\"createTime\""));
            assertTrue(json.contains("\"direction\":\"DESC\""));
        }

        @Test
        @DisplayName("反序列化分页")
        void testDeserializePageRequest() throws Exception {
            String json = "{\"page\":1,\"size\":50}";
            VexPageRequest paging = objectMapper.readValue(json, VexPageRequest.class);
            
            assertEquals(1, paging.getPage());
            assertEquals(50, paging.getSize());
        }
    }
}