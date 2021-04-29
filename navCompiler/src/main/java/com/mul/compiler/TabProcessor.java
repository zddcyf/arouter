package com.mul.compiler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.auto.service.AutoService;
import com.mul.annotation.TabChild;
import com.mul.annotation.TabEntrance;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.annotation.Annotation;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

/**
 * @ProjectName: router
 * @Package: com.mul.annotation
 * @ClassName: TabProcessor
 * @Author: zdd
 * @CreateDate: 2020/7/2 19:54
 * @Description: java类作用描述
 * @UpdateUser: 更新者
 * @UpdateDate: 2020/7/2 19:54
 * @UpdateRemark: 更新说明
 * @Version: 1.0.0
 */
@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes({"com.mul.annotation.TabEntrance", "com.mul.annotation.TabChild"})
public class TabProcessor extends AbstractProcessor {
    private Messager messager;
    private Filer filer;
    private final String defaultModuleName = "app";
    private String dynamicModuleName;
    private String FILE_PATH;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        messager = processingEnvironment.getMessager();
        filer = processingEnvironment.getFiler();
        dynamicModuleName = processingEnvironment.getOptions().get("DYNAMIC_MODULE_NAME");
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        Set<? extends Element> elementsTabEntrace = roundEnvironment.getElementsAnnotatedWith(TabEntrance.class);
        Set<? extends Element> elementsTabChild = roundEnvironment.getElementsAnnotatedWith(TabChild.class);

        if (!elementsTabEntrace.isEmpty() || !elementsTabChild.isEmpty()) {
            messager.printMessage(Diagnostic.Kind.NOTE, "当前数据elementsTabEntrace:" + elementsTabEntrace.toString());
            messager.printMessage(Diagnostic.Kind.NOTE, "当前数据elementsTabChild:" + elementsTabChild.toString());
            HashMap<String, TabEntrance> tabEntranceMap = new HashMap<>();
            insertTabEntrance(elementsTabEntrace, TabEntrance.class, tabEntranceMap);
            messager.printMessage(Diagnostic.Kind.NOTE, "当前数据tabEntranceMap:" + tabEntranceMap.toString());
            HashMap<TabEntrance, HashMap<String, TabChild>> tabEntranceListHashMap = insertTabChild(elementsTabChild, TabChild.class, tabEntranceMap);
            messager.printMessage(Diagnostic.Kind.NOTE, "当前数据tabEntranceListHashMap:" + tabEntranceListHashMap.toString());
            writeFile(tabEntranceListHashMap);
        }

        return true;
    }

    private String readFile(String jsonFileName) {
        FileInputStream fis = null;
        InputStreamReader isr = null;
        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();
        try {
            fis = new FileInputStream(FILE_PATH + jsonFileName);
            isr = new InputStreamReader(fis);
            br = new BufferedReader(isr);
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        } catch (Exception e) {
            e.getMessage();
            messager.printMessage(Diagnostic.Kind.NOTE, "readFile文件读取IOException:" + e.getMessage());
        } finally {
            try {
                if (null != br) {
                    br.close();
                }
                if (null != isr) {
                    isr.close();
                }
                if (null != fis) {
                    fis.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    private void getFilePath(String jsonFileName) {
        try {
            FileObject resource = filer.createResource(StandardLocation.CLASS_OUTPUT, "", jsonFileName);
            // 位置：
            String resourcePath = resource.toUri().getPath();
            messager.printMessage(Diagnostic.Kind.NOTE, "getFilePathresourcePath:" + resourcePath);
            String appPath = resourcePath.substring(0, resourcePath.indexOf(dynamicModuleName) + dynamicModuleName.length() + 1);
            messager.printMessage(Diagnostic.Kind.NOTE, "getFilePathappPath:" + appPath);
            if (!dynamicModuleName.contains(defaultModuleName)) {
                appPath = appPath.replace(dynamicModuleName, defaultModuleName);
            }
            FILE_PATH = String.format("%s%s", appPath, "src/main/assets/");
            messager.printMessage(Diagnostic.Kind.NOTE, "getFilePathassetsPath:" + dynamicModuleName + FILE_PATH);
        } catch (IOException e) {
            e.printStackTrace();
            messager.printMessage(Diagnostic.Kind.NOTE, "getFilePath获取文件路径失败:" + e.getMessage());
        }
    }

    /**
     * 先把数据key填充完整
     *
     * @param tabEntrances   获取到的tab列表
     * @param tabEntranceClz 是那个标签
     * @param tabEntranceMap 存储标签的列表
     */
    private void insertTabEntrance(Set<? extends Element> tabEntrances, Class<? extends Annotation> tabEntranceClz, HashMap<String, TabEntrance> tabEntranceMap) {
        for (Element element : tabEntrances) {
            TypeElement typeElement = (TypeElement) element;
            Annotation annotation = typeElement.getAnnotation(tabEntranceClz);
            TabEntrance tab = (TabEntrance) annotation;
            tabEntranceMap.put(tab.pageUrl(), tab);
        }
    }

    /**
     * 将所有的子列表放置到对应的key中
     *
     * @param elementsTabChilds 子tab
     * @param tabChildClz       tab标签class
     * @param tabEntranceMap    存储数据源
     * @return 回去组装好的数据
     */
    private HashMap<TabEntrance, HashMap<String, TabChild>> insertTabChild(Set<? extends Element> elementsTabChilds, Class<? extends Annotation> tabChildClz, HashMap<String, TabEntrance> tabEntranceMap) {
        HashMap<TabEntrance, HashMap<String, TabChild>> tabMap = new HashMap<>();
        HashMap<String, TabChild> tabChilds = new HashMap<>();
        for (Element element : elementsTabChilds) {
            TypeElement typeElement = (TypeElement) element;
            Annotation annotation = typeElement.getAnnotation(tabChildClz);
            TabChild tabChild = (TabChild) annotation;
            TabEntrance tabEntrance = tabEntranceMap.get(tabChild.entrancePageUrl());
            if (null == tabEntrance) {
                com.mul.compiler.DefaultTabEntrance defaultTabEntrance = new DefaultTabEntrance();
                if (tabChild.jsonName().equals("") || tabChild.jsonName() == null) {
                    messager.printMessage(Diagnostic.Kind.ERROR, "请在tabChild中配置数据库名字");
                }
                defaultTabEntrance.setJsonName(tabChild.jsonName());
                tabEntrance = defaultTabEntrance;
                tabEntranceMap.put(tabChild.entrancePageUrl(), tabEntrance);
            }
            String clzName = typeElement.getQualifiedName().toString();
            if (tabMap.containsKey(tabEntrance)) {
                tabChilds = tabMap.get(tabEntrance);
            } else {
                tabChilds.clear();
            }
            tabChilds.put(clzName, tabChild);
            tabMap.put(tabEntrance, tabChilds);
        }

        if (tabMap.isEmpty()) {
            for (Map.Entry<String, TabEntrance> entry : tabEntranceMap.entrySet()) {
                tabMap.put(entry.getValue(), new HashMap<>());
            }
        }
        return tabMap;
    }

    /**
     * 将数据转换成json串
     *
     * @param tabMap 数据源
     */
    private void writeFile(HashMap<TabEntrance, HashMap<String, TabChild>> tabMap) {
        for (TabEntrance tabEntrance : tabMap.keySet()) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("activeColor", tabEntrance.activeColor());
            jsonObject.put("unActiveColor", tabEntrance.unActiveColor());
            jsonObject.put("selectTab", tabEntrance.selectTab());
            jsonObject.put("isIcon", tabEntrance.isIcon());
            if (tabEntrance.icons().length > 0) {
                JSONArray icons = new JSONArray();
                for (int icon : tabEntrance.icons()) {
                    icons.add(icon);
                }
                jsonObject.put("icons", icons);
            }
            JSONArray jsonArray = new JSONArray();
            for (Map.Entry<String, TabChild> mEntry : tabMap.get(tabEntrance).entrySet()) {
                TabChild tabChild = mEntry.getValue();
                JSONObject childJson = new JSONObject();
                childJson.put("size", tabChild.size());
                childJson.put("enable", tabChild.enable());
                childJson.put("index", tabChild.index());
                childJson.put("entrancePageUrl", tabChild.entrancePageUrl());
                childJson.put("pageUrl", tabChild.pageUrl());
                childJson.put("title", tabChild.title());
                childJson.put("tintColor", tabChild.tintColor());
                childJson.put("id", Math.abs(mEntry.getKey().hashCode()));
                childJson.put("clzName", mEntry.getKey());
                childJson.put("needLogin", tabChild.needLogin());
                childJson.put("asStarter", tabChild.asStarter());
                childJson.put("isFragment", tabChild.isFragment());
                jsonArray.add(childJson);
            }
            jsonObject.put("tabs", jsonArray);

            writeFile(tabEntrance.jsonName(), jsonObject);
        }
    }

    /**
     * 将json写入到文件
     *
     * @param jsonFileName json名字
     * @param jsonObject   json串
     */
    public void writeFile(String jsonFileName, JSONObject jsonObject) {
        getFilePath(jsonFileName);

        String readFile = readFile(jsonFileName);
        if (null != readFile && !"".equals(readFile)) {
            JSONObject readJson = JSON.parseObject(readFile);
            JSONArray readTabs = readJson.getJSONArray("tabs");
            JSONArray jsonTabs = jsonObject.getJSONArray("tabs");

            JSONArray readIcons = null;
            JSONArray icons = null;
            if (readJson.containsKey("icons")) {
                readIcons = readJson.getJSONArray("icons");
            }
            if (jsonObject.containsKey("icons")) {
                icons = jsonObject.getJSONArray("icons");
            }
            if (icons == null && readIcons != null) {
                messager.printMessage(Diagnostic.Kind.NOTE, "icons==null;readIcons的长度" + readIcons.size());
                jsonObject.put("icons", readIcons);
            }

            for (int i = 0; i < readTabs.size(); i++) {
                JSONObject object = readTabs.getJSONObject(i);
                String mPageUrl = object.getString("pageUrl");
                if (!jsonTabs.toString().contains(mPageUrl)) {
                    jsonTabs.add(object);
                }
            }
            jsonObject.put("tabs", jsonTabs);
        }

        messager.printMessage(Diagnostic.Kind.NOTE, "当前运行的module" + defaultModuleName);

        // 生成文件到// app/src/main/accets
        FileOutputStream fos = null;
        OutputStreamWriter osw = null;

        try {
            File assetsPathFile = new File(FILE_PATH);
            if (!assetsPathFile.exists()) {
                boolean mMkdirs = assetsPathFile.mkdirs();
                messager.printMessage(Diagnostic.Kind.NOTE, assetsPathFile.getAbsolutePath() + "不存在，创建:" + mMkdirs);
            }
            File outputFile = new File(assetsPathFile, jsonFileName);
            String content = JSON.toJSONString(jsonObject);

            if (outputFile.exists()) {
                boolean mDelete = outputFile.delete();
                messager.printMessage(Diagnostic.Kind.NOTE, outputFile.getAbsolutePath() + "存在，删除:" + mDelete);
            }
            boolean mNewFile = outputFile.createNewFile();
            messager.printMessage(Diagnostic.Kind.NOTE, outputFile.getAbsolutePath() + "重新创建:" + mNewFile);

            fos = new FileOutputStream(outputFile);
            osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
            osw.write(content);
            osw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != osw) {
                    osw.close();
                }

                if (null != fos) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
