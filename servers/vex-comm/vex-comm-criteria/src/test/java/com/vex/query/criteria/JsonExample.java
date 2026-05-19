package com.vex.query.criteria;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonExample {

    public static void main(String[] args) throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        VexQueryCriteria query = VexQueryCriteria.of()
            .select("id", "username", "email")
            .filter(VexPredicate.and(
                VexExpression.eq("status", "ACTIVE"),
                VexExpression.gte("age", 18),
                VexPredicate.or(
                    VexExpression.exp("username", "%张%"),
                    VexExpression.exp("username", "%李%")
                )
            ))
            .orderBy(VexSortOrder.desc("createTime"))
            .paging(VexPageRequest.of(0, 20));

        String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(query);
        System.out.println("=== 序列化结果 ===");
        System.out.println(json);

        VexQueryCriteria deserialized = mapper.readValue(json, VexQueryCriteria.class);
        System.out.println("\n=== 反序列化成功 ===");
        System.out.println("过滤条件是否为空: " + deserialized.getFilter().checkEmpty());
    }
}