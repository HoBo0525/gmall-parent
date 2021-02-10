package com.atguigu.gmall.common.cache;

import java.lang.annotation.*;

/**
 * @author Hobo
 * @create 2021-02-09 21:16
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface GmallCache {
    //缓存前缀
    String prefix() default "cache";
}
