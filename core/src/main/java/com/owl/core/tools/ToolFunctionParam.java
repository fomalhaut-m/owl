package com.owl.core.tools;

import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.PARAMETER, ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@ToolParam
public @interface ToolFunctionParam {


    /// 参数是否必填
    @AliasFor(annotation = ToolParam.class, attribute = "required")
    boolean required() default true;

    /// 参数描述
    @AliasFor(annotation = ToolParam.class, attribute = "description")
    String description() default "";
}
