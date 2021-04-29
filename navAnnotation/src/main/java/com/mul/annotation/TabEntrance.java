package com.mul.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @ProjectName: router
 * @Package: com.mul.annotation
 * @ClassName: TabEntrance
 * @Author: zdd
 * @CreateDate: 2020/7/2 16:03
 * @Description: 底部tab的入口类
 * @UpdateUser: 更新者
 * @UpdateDate: 2020/7/2 16:03
 * @UpdateRemark: 更新说明
 * @Version: 1.0.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface TabEntrance {
    /**
     * json文件的名字
     *
     * @return 存储数据的文件名
     */
    String jsonName() default "";

    /**
     * 路由路径
     *
     * @return 路由地址
     */
    String pageUrl() default "";

    /**
     * 被选中的颜色
     *
     * @return 选中的颜色
     */
    String activeColor() default "#333333";

    /**
     * 未被选中的颜色
     *
     * @return 未被选中时的颜色
     */
    String unActiveColor() default "#999999";

    /**
     * 图片地址
     *
     * @return 图片地址列表
     */
    int[] icons();

    /**
     * 默认选中项
     *
     * @return 默认第几个被选中
     */
    int selectTab() default 0;

    /**
     * 是否让icon跟随纯色走
     */
    boolean isIcon() default false;
}
