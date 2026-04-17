package com.owl.core.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Tool
public @interface ToolFunction {
    //👇 关键：AliasFor 把值同步到 @Tool 的 name
    /// 函数名称
    @AliasFor(annotation = Tool.class, attribute = "name")
    String name() default "";

    /// 函数描述
    @AliasFor(annotation = Tool.class, attribute = "description")
    String description() default "";
}
