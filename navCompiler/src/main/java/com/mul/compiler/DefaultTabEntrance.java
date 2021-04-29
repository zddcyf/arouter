package com.mul.compiler;

import com.mul.annotation.TabEntrance;

import java.lang.annotation.Annotation;

/**
 * @ProjectName: router
 * @Package: com.mul.annotation
 * @ClassName: DefaultTabEntrance
 * @Author: zdd
 * @CreateDate: 2020/7/22 11:07
 * @Description: java类作用描述
 * @UpdateUser: 更新者
 * @UpdateDate: 2020/7/22 11:07
 * @UpdateRemark: 更新说明
 * @Version: 1.0.0
 */
public class DefaultTabEntrance implements TabEntrance {
    private String jsonName;
    private String clzName;

    public void setJsonName(String jsonName) {
        this.jsonName = jsonName;
    }

    public void setClzName(String clzName) {
        this.clzName = clzName;
    }

    @Override
    public String jsonName() {
        return jsonName;
    }

    @Override
    public String pageUrl() {
        return "";
    }

    @Override
    public String activeColor() {
        return "#333333";
    }

    @Override
    public String unActiveColor() {
        return "#999999";
    }

    @Override
    public int[] icons() {
        return new int[0];
    }

    @Override
    public int selectTab() {
        return 0;
    }

    @Override
    public boolean isIcon() {
        return false;
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        return null;
    }
}