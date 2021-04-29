package com.mul.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @ProjectName: router
 * @Package: com.mul.annotation
 * @ClassName: TabChild
 * @Author: zdd
 * @CreateDate: 2020/7/2 17:47
 * @Description: tab的子级
 * @UpdateUser: 更新者
 * @UpdateDate: 2020/7/2 17:47
 * @UpdateRemark: 更新说明
 * @Version: 1.0.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface TabChild {
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

    /**
     * 是否为activity
     *
     * @return true为activity false为fragment
     */
    boolean isFragment() default true;

    /**
     * json文件的名字
     *
     * @return 存储数据的文件名
     */
    String jsonName() default "";

    /**
     * 入口的pageUrl
     *
     * @return 入口路由地址
     */
    String entrancePageUrl() default "";

    /**
     * 路由路径
     *
     * @return 路由地址
     */
    String pageUrl() default "";

    /**
     * 默认控件大小
     *
     * @return 控件的宽高
     */
    int size() default 20;

    /**
     * 是否显示
     *
     * @return true为需要显示，false为不需要显示
     */
    boolean enable() default true;

    /**
     * 所在位置
     *
     * @return 是第几个页面
     */
    int index() default 0;

    /**
     * 默认标题内容
     *
     * @return 标题
     */
    String title() default "";

    /**
     * 默认颜色
     *
     * @return 颜色
     */
    String tintColor() default "";
}
