package com.volleyball.volleyballcommunitybackend.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AdminOperation {
    String action();           // 操作类型，如 DELETE_POST, BAN_USER
    String targetType();       // 对象类型，如 USER, POST, COMMENT
}
