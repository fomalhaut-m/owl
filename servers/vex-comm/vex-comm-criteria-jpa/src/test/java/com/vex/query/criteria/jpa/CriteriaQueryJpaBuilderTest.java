package com.vex.query.criteria.jpa;

import com.vex.query.criteria.*;
import jakarta.persistence.criteria.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * JPA Criteria 查询构建器单元测试
 * <p>
 * 测试 {@link CriteriaQueryJpaBuilder} 的各种查询构建场景，
 * 包括字段选择、过滤条件、排序等功能。
 * </p>
 *
 * @author Lingma Team
 * @since 1.0
 */
@DisplayName("CriteriaQueryJpaBuilder 单元测试")
class CriteriaQueryJpaBuilderTest {

    private CriteriaBuilder criteriaBuilder;
    private CriteriaQuery<TestEntity> criteriaQuery;
    private Root<TestEntity> root;
    private Path<Object> path;
    private Predicate predicate;

    @BeforeEach
    void setUp() {
        // 创建 Mock 对象
        criteriaBuilder = mock(CriteriaBuilder.class);
        criteriaQuery = mock(CriteriaQuery.class);
        root = mock(Root.class);
        path = mock(Path.class);
        predicate = mock(Predicate.class);

        // 配置基本行为
        when(criteriaBuilder.createQuery(TestEntity.class)).thenReturn(criteriaQuery);
        when(criteriaQuery.from(TestEntity.class)).thenReturn(root);
        when(root.get(anyString())).thenReturn(path);
        when(criteriaBuilder.equal(any(), any())).thenReturn(predicate);
        when(criteriaBuilder.notEqual(any(), any())).thenReturn(predicate);
        when(criteriaBuilder.greaterThan(any(Expression.class), any(Comparable.class))).thenReturn(predicate);
        when(criteriaBuilder.greaterThanOrEqualTo(any(Expression.class), any(Comparable.class))).thenReturn(predicate);
        when(criteriaBuilder.lessThan(any(Expression.class), any(Comparable.class))).thenReturn(predicate);
        when(criteriaBuilder.lessThanOrEqualTo(any(Expression.class), any(Comparable.class))).thenReturn(predicate);
        when(criteriaBuilder.like(any(Expression.class), anyString())).thenReturn(predicate);
        when(criteriaBuilder.and(any(Predicate[].class))).thenReturn(predicate);
        when(criteriaBuilder.or(any(Predicate[].class))).thenReturn(predicate);
        when(criteriaBuilder.not(any(Predicate.class))).thenReturn(predicate);
        when(criteriaBuilder.conjunction()).thenReturn(predicate);
    }

    @Nested
    @DisplayName("构建器创建测试")
    class BuilderCreation {

        @Test
        @DisplayName("应该能够创建构建器实例")
        void testCreateBuilder() {
            CriteriaQueryJpaBuilder<TestEntity> builder = CriteriaQueryJpaBuilder.forEntity(TestEntity.class);
            assertNotNull(builder);
        }

        @Test
        @DisplayName("应该能够初始化 CriteriaQuery")
        void testInitializeQuery() {
            CriteriaQueryJpaBuilder<TestEntity> builder = CriteriaQueryJpaBuilder.forEntity(TestEntity.class);
            builder.from(criteriaBuilder);

            verify(criteriaBuilder).createQuery(TestEntity.class);
        }
    }

    @Nested
    @DisplayName("字段选择测试")
    class FieldSelection {

        @Test
        @DisplayName("不指定 select 和 exclude 时应查询所有字段")
        void testSelectAllFields() {
            VexQueryCriteria criteria = VexQueryCriteria.of();

            CriteriaQueryJpaBuilder<TestEntity> builder = CriteriaQueryJpaBuilder.forEntity(TestEntity.class);
            builder.from(criteriaBuilder);
            builder.apply(criteria, criteriaBuilder);

            verify(criteriaQuery).select(root);
        }

        @Test
        @DisplayName("指定 select 字段时应只查询指定字段")
        void testSelectSpecificFields() {
            VexQueryCriteria criteria = VexQueryCriteria.of().select("id", "name");

            CriteriaQueryJpaBuilder<TestEntity> builder = CriteriaQueryJpaBuilder.forEntity(TestEntity.class);
            builder.from(criteriaBuilder);
            builder.apply(criteria, criteriaBuilder);

            verify(root, times(2)).get(anyString());
            verify(criteriaQuery).multiselect(any(Selection[].class));
        }

        @Test
        @DisplayName("指定 exclude 字段时应排除指定字段")
        void testExcludeFields() {
            VexQueryCriteria criteria = VexQueryCriteria.of().exclude("password");

            CriteriaQueryJpaBuilder<TestEntity> builder = CriteriaQueryJpaBuilder.forEntity(TestEntity.class);
            builder.from(criteriaBuilder);
            builder.apply(criteria, criteriaBuilder);

            verify(criteriaQuery).multiselect(any(Selection[].class));
        }
    }

    @Nested
    @DisplayName("相等操作符测试")
    class EqualityOperators {

        @Test
        @DisplayName("应该构建 eq 条件")
        void testEqOperator() {
            VexQueryCriteria criteria = VexQueryCriteria.of()
                    .filter(VexExpression.eq("name", "John"));

            CriteriaQueryJpaBuilder<TestEntity> builder = CriteriaQueryJpaBuilder.forEntity(TestEntity.class);
            builder.from(criteriaBuilder);
            builder.apply(criteria, criteriaBuilder);

            verify(criteriaBuilder).equal(path, "John");
            verify(criteriaQuery).where(predicate);
        }

        @Test
        @DisplayName("应该构建 neq 条件")
        void testNeqOperator() {
            VexQueryCriteria criteria = VexQueryCriteria.of()
                    .filter(VexExpression.neq("status", "deleted"));

            CriteriaQueryJpaBuilder<TestEntity> builder = CriteriaQueryJpaBuilder.forEntity(TestEntity.class);
            builder.from(criteriaBuilder);
            builder.apply(criteria, criteriaBuilder);

            verify(criteriaBuilder).notEqual(path, "deleted");
            verify(criteriaQuery).where(predicate);
        }

        @Test
        @DisplayName("eq 操作符应该允许 null 值")
        void testEqWithNullValue() {
            VexQueryCriteria criteria = VexQueryCriteria.of()
                    .filter(VexExpression.eq("deletedAt", null));

            assertDoesNotThrow(() -> {
                CriteriaQueryJpaBuilder<TestEntity> builder = CriteriaQueryJpaBuilder.forEntity(TestEntity.class);
                builder.from(criteriaBuilder);
                builder.apply(criteria, criteriaBuilder);
            });
        }
    }

    @Nested
    @DisplayName("比较操作符测试")
    class ComparisonOperators {

        @Test
        @DisplayName("应该构建 gt 条件")
        void testGtOperator() {
            VexQueryCriteria criteria = VexQueryCriteria.of()
                    .filter(VexExpression.gt("age", 18));

            CriteriaQueryJpaBuilder<TestEntity> builder = CriteriaQueryJpaBuilder.forEntity(TestEntity.class);
            builder.from(criteriaBuilder);
            builder.apply(criteria, criteriaBuilder);

            verify(criteriaBuilder).greaterThan(any(Expression.class), eq(18));
            verify(criteriaQuery).where(predicate);
        }

        @Test
        @DisplayName("应该构建 gte 条件")
        void testGteOperator() {
            VexQueryCriteria criteria = VexQueryCriteria.of()
                    .filter(VexExpression.gte("score", 60));

            CriteriaQueryJpaBuilder<TestEntity> builder = CriteriaQueryJpaBuilder.forEntity(TestEntity.class);
            builder.from(criteriaBuilder);
            builder.apply(criteria, criteriaBuilder);

            verify(criteriaBuilder).greaterThanOrEqualTo(any(Expression.class), eq(60));
        }

        @Test
        @DisplayName("应该构建 lt 条件")
        void testLtOperator() {
            VexQueryCriteria criteria = VexQueryCriteria.of()
                    .filter(VexExpression.lt("price", 100));

            CriteriaQueryJpaBuilder<TestEntity> builder = CriteriaQueryJpaBuilder.forEntity(TestEntity.class);
            builder.from(criteriaBuilder);
            builder.apply(criteria, criteriaBuilder);

            verify(criteriaBuilder).lessThan(any(Expression.class), eq(100));
        }

        @Test
        @DisplayName("应该构建 lte 条件")
        void testLteOperator() {
            VexQueryCriteria criteria = VexQueryCriteria.of()
                    .filter(VexExpression.lte("quantity", 10));

            CriteriaQueryJpaBuilder<TestEntity> builder = CriteriaQueryJpaBuilder.forEntity(TestEntity.class);
            builder.from(criteriaBuilder);
            builder.apply(criteria, criteriaBuilder);

            verify(criteriaBuilder).lessThanOrEqualTo(any(Expression.class), eq(10));
        }
    }

    @Nested
    @DisplayName("模糊匹配操作符测试")
    class LikeOperators {

        @Test
        @DisplayName("应该构建 exp 条件")
        void testExpOperator() {
            VexQueryCriteria criteria = VexQueryCriteria.of()
                .filter(VexExpression.exp("name", "%John%"));
            
            CriteriaQueryJpaBuilder<TestEntity> builder = CriteriaQueryJpaBuilder.forEntity(TestEntity.class);
            builder.from(criteriaBuilder);
            builder.apply(criteria, criteriaBuilder);
            
            verify(criteriaBuilder).like(any(Expression.class), eq("%John%"));
        }

        @Test
        @DisplayName("应该构建 not_exp (NOT LIKE) 条件")
        void testNotExpOperator() {
            VexQueryCriteria criteria = VexQueryCriteria.of()
                .filter(VexExpression.notExp("email", "%spam%"));
            
            CriteriaQueryJpaBuilder<TestEntity> builder = CriteriaQueryJpaBuilder.forEntity(TestEntity.class);
            builder.from(criteriaBuilder);
            builder.apply(criteria, criteriaBuilder);
            
            verify(criteriaBuilder).like(any(Expression.class), eq("%spam%"));
            verify(criteriaBuilder).not(any(Predicate.class));
        }
    }

    @Nested
    @DisplayName("集合操作符测试")
    class InOperators {

        @Test
        @DisplayName("应该构建 in 条件")
        void testInOperator() {
            List<Integer> ids = List.of(1, 2, 3);
            VexQueryCriteria criteria = VexQueryCriteria.of()
                    .filter(VexExpression.in("id", ids));

            CriteriaQueryJpaBuilder<TestEntity> builder = CriteriaQueryJpaBuilder.forEntity(TestEntity.class);
            builder.from(criteriaBuilder);
            builder.apply(criteria, criteriaBuilder);

            verify(path).in(ids);
        }

        @Test
        @DisplayName("应该构建 not_in 条件")
        void testNotInOperator() {
            Set<String> statuses = Set.of("inactive", "deleted");
            VexQueryCriteria criteria = VexQueryCriteria.of()
                    .filter(VexExpression.notIn("status", statuses));

            CriteriaQueryJpaBuilder<TestEntity> builder = CriteriaQueryJpaBuilder.forEntity(TestEntity.class);
            builder.from(criteriaBuilder);
            builder.apply(criteria, criteriaBuilder);

            verify(path).in(statuses);
            verify(criteriaBuilder).not(any(Predicate.class));
        }
    }

    @Nested
    @DisplayName("范围操作符测试")
    class BetweenOperator {

        @Test
        @DisplayName("应该构建 between 条件")
        void testBetweenOperator() {
            VexQueryCriteria criteria = VexQueryCriteria.of()
                    .filter(VexExpression.between("age", 18, 60));

            CriteriaQueryJpaBuilder<TestEntity> builder = CriteriaQueryJpaBuilder.forEntity(TestEntity.class);
            builder.from(criteriaBuilder);
            builder.apply(criteria, criteriaBuilder);

            verify(criteriaBuilder).between(any(Path.class), any(Comparable.class), any(Comparable.class));
        }
    }

    @Nested
    @DisplayName("空值判断操作符测试")
    class NullOperators {

        @Test
        @DisplayName("应该构建 is_null 条件")
        void testIsNullOperator() {
            VexQueryCriteria criteria = VexQueryCriteria.of()
                    .filter(VexExpression.isNull("deletedAt"));

            CriteriaQueryJpaBuilder<TestEntity> builder = CriteriaQueryJpaBuilder.forEntity(TestEntity.class);
            builder.from(criteriaBuilder);
            builder.apply(criteria, criteriaBuilder);

            verify(criteriaBuilder).isNull(path);
        }

        @Test
        @DisplayName("应该构建 is_not_null 条件")
        void testIsNotNullOperator() {
            VexQueryCriteria criteria = VexQueryCriteria.of()
                    .filter(VexExpression.isNotNull("createdAt"));

            CriteriaQueryJpaBuilder<TestEntity> builder = CriteriaQueryJpaBuilder.forEntity(TestEntity.class);
            builder.from(criteriaBuilder);
            builder.apply(criteria, criteriaBuilder);

            verify(criteriaBuilder).isNotNull(path);
        }
    }

    @Nested
    @DisplayName("逻辑组合测试")
    class LogicalCombinations {

        @Test
        @DisplayName("应该构建 AND 组合条件")
        void testAndLogic() {
            VexPredicate filter = VexPredicate.and(
                VexExpression.gt("age", 18),
                VexExpression.eq("status", "active")
            );
            VexQueryCriteria criteria = VexQueryCriteria.of().filter(filter);
            
            CriteriaQueryJpaBuilder<TestEntity> builder = CriteriaQueryJpaBuilder.forEntity(TestEntity.class);
            builder.from(criteriaBuilder);
            builder.apply(criteria, criteriaBuilder);
            
            verify(criteriaBuilder).and(any(Predicate[].class));
        }

        @Test
        @DisplayName("应该构建 OR 组合条件")
        void testOrLogic() {
            VexPredicate filter = VexPredicate.or(
                VexExpression.eq("role", "admin"),
                VexExpression.eq("role", "manager")
            );
            VexQueryCriteria criteria = VexQueryCriteria.of().filter(filter);
            
            CriteriaQueryJpaBuilder<TestEntity> builder = CriteriaQueryJpaBuilder.forEntity(TestEntity.class);
            builder.from(criteriaBuilder);
            builder.apply(criteria, criteriaBuilder);
            
            verify(criteriaBuilder).or(any(Predicate[].class));
        }

        @Test
        @DisplayName("应该支持嵌套的逻辑组合")
        void testNestedLogic() {
            VexPredicate filter = VexPredicate.or(
                VexPredicate.and(
                    VexExpression.gt("age", 18),
                    VexExpression.eq("status", "active")
                ),
                VexExpression.in("role", List.of("admin", "manager"))
            );
            VexQueryCriteria criteria = VexQueryCriteria.of().filter(filter);
            
            assertDoesNotThrow(() -> {
                CriteriaQueryJpaBuilder<TestEntity> builder = CriteriaQueryJpaBuilder.forEntity(TestEntity.class);
                builder.from(criteriaBuilder);
                builder.apply(criteria, criteriaBuilder);
            });
        }
    }

    @Nested
    @DisplayName("排序测试")
    class OrderBy {

        @Test
        @DisplayName("应该应用升序排序")
        void testAscOrder() {
            VexQueryCriteria criteria = VexQueryCriteria.of()
                .orderBy(VexSortOrder.asc("name"));
            
            CriteriaQueryJpaBuilder<TestEntity> builder = CriteriaQueryJpaBuilder.forEntity(TestEntity.class);
            builder.from(criteriaBuilder);
            builder.apply(criteria, criteriaBuilder);
            
            verify(criteriaBuilder).asc(path);
            verify(criteriaQuery).orderBy(anyList());
        }

        @Test
        @DisplayName("应该应用降序排序")
        void testDescOrder() {
            VexQueryCriteria criteria = VexQueryCriteria.of()
                .orderBy(VexSortOrder.desc("createTime"));
            
            CriteriaQueryJpaBuilder<TestEntity> builder = CriteriaQueryJpaBuilder.forEntity(TestEntity.class);
            builder.from(criteriaBuilder);
            builder.apply(criteria, criteriaBuilder);
            
            verify(criteriaBuilder).desc(path);
            verify(criteriaQuery).orderBy(anyList());
        }

        @Test
        @DisplayName("应该支持多字段排序")
        void testMultipleOrderBy() {
            VexQueryCriteria criteria = VexQueryCriteria.of()
                .orderBy(
                    VexSortOrder.desc("createTime"),
                    VexSortOrder.asc("name")
                );
            
            CriteriaQueryJpaBuilder<TestEntity> builder = CriteriaQueryJpaBuilder.forEntity(TestEntity.class);
            builder.from(criteriaBuilder);
            builder.apply(criteria, criteriaBuilder);
            
            verify(criteriaBuilder, times(1)).asc(any());
            verify(criteriaBuilder, times(1)).desc(any());
        }
    }

    @Nested
    @DisplayName("异常处理测试")
    class ExceptionHandling {

        @Test
        @DisplayName("字段不存在时应抛出友好的异常")
        void testFieldNotExist() {
            when(root.get("invalidField")).thenThrow(new IllegalArgumentException("Unable to locate Attribute"));
            
            VexQueryCriteria criteria = VexQueryCriteria.of()
                .filter(VexExpression.eq("invalidField", "value"));
            
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> {
                    CriteriaQueryJpaBuilder<TestEntity> builder = CriteriaQueryJpaBuilder.forEntity(TestEntity.class);
                    builder.from(criteriaBuilder);
                    builder.apply(criteria, criteriaBuilder);
                }
            );
            
            assertTrue(exception.getMessage().contains("invalidField"));
            assertTrue(exception.getMessage().contains("TestEntity"));
        }

        @Test
        @DisplayName("类型校验失败时应抛出异常")
        void testTypeValidationFailure() {
            VexQueryCriteria criteria = VexQueryCriteria.of()
                .filter(VexExpression.exp("age", 123));
            
            assertThrows(
                IllegalArgumentException.class,
                () -> {
                    CriteriaQueryJpaBuilder<TestEntity> builder = CriteriaQueryJpaBuilder.forEntity(TestEntity.class);
                    builder.from(criteriaBuilder);
                    builder.apply(criteria, criteriaBuilder);
                }
            );
        }
    }

    @Nested
    @DisplayName("静态工厂方法测试")
    class StaticFactoryMethod {

        @Test
        @DisplayName("buildQuery 应该能够构建完整的查询")
        void testBuildQuery() {
            VexQueryCriteria criteria = VexQueryCriteria.of()
                .filter(VexExpression.gt("age", 18))
                .orderBy(VexSortOrder.desc("createTime"));
            
            CriteriaQuery<TestEntity> query = CriteriaQueryJpaBuilder.buildQuery(
                TestEntity.class,
                criteria,
                criteriaBuilder
            );
            
            assertNotNull(query);
            verify(criteriaBuilder).createQuery(TestEntity.class);
            verify(criteriaQuery).from(TestEntity.class);
            verify(criteriaQuery).where(any(Predicate.class));
            verify(criteriaQuery).orderBy(anyList());
        }
    }

    /**
     * 测试实体类
     */
    static class TestEntity {
        private Long id;
        private String name;
        private Integer age;
        private String status;

        // Getter and Setter methods would be here
    }
}
