package com.desert.router.annotations;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.CLASS)//保留策略 编译器
@Target({ElementType.TYPE})//修饰的元素 目标是类或者接口
public @interface Destination {
    /**
     * 当前界面的url
     *
     * @return 页面URL
     */
    String url();

    /**
     * 对于当前界面的描述 description
     *
     * @return 例如 “商品首页”
     */
    String description();
}