package com.mul.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @ProjectName: router
 * @Package: com.mul.annotation
 * @ClassName: FragmentDestination
 * @Author: zdd
 * @CreateDate: 2020/7/1 15:26
 * @Description: java类作用描述
 * @UpdateUser: 更新者
 * @UpdateDate: 2020/7/1 15:26
 * @UpdateRemark: 更新说明
 * @Version: 1.0.0
 */
@Target(ElementType.TYPE) // 只可以在头部使用
@Retention(RetentionPolicy.SOURCE)
public @interface FragmentDestination {
    /**
     * pageUrl
     *
     * @return 路由地址
     */
    String pageUrl();

    /**
     * 是否需要登录
     *
     * @return false为不需要 true为需要
     */
    boolean needLogin() default false;

    /**
     * 是否启动页
     *
     * @return false为不是启动页 true为是
     */
    boolean asStarter() default false;
}
