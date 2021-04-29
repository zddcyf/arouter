package com.mul.compiler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.google.auto.service.AutoService;
import com.mul.annotation.ActivityDestination;
import com.mul.annotation.FragmentDestination;

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
 * @ClassName: NavProcessor
 * @Author: zdd
 * @CreateDate: 2020/7/1 15:27
 * @Description: 注解处理器
 * @UpdateUser: 更新者
 * @UpdateDate: 2020/7/1 15:27
 * @UpdateRemark: 更新说明
 * @Version: 1.0.0
 */
@AutoService(Processor.class) // 注解处理器标配
@SupportedSourceVersion(SourceVersion.RELEASE_8) // 源码类型
@SupportedAnnotationTypes({"com.mul.annotation.ActivityDestination"
        , "com.mul.annotation.FragmentDestination"}) // 需要处理的注解的类型
public class NavProcessor extends AbstractProcessor {
    private Messager messager;
    private Filer filer;
    private static final String OUTPUT_FILE_NAME = "destnation.json";
    private String dynamicModuleName;
    private String FILE_PATH;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        messager = processingEnvironment.getMessager(); // 可以打印运行时日志
        filer = processingEnvironment.getFiler(); // 可以生成文件
        dynamicModuleName = processingEnvironment.getOptions().get("DYNAMIC_MODULE_NAME");
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        // 获取activity注解类的集合
        Set<? extends Element> activityElement = roundEnvironment.getElementsAnnotatedWith(ActivityDestination.class);
        // 获取fragment注解类的集合
        Set<? extends Element> fragmentElement = roundEnvironment.getElementsAnnotatedWith(FragmentDestination.class);
        if (!activityElement.isEmpty() || !fragmentElement.isEmpty()) {
            getFilePath();

            Map<String, JSONObject> destMap = new HashMap<>();

            handlerDestination(activityElement, ActivityDestination.class, destMap);
            handlerDestination(fragmentElement, FragmentDestination.class, destMap);

            String readFile = readFile();
            if (null != readFile && !"".equals(readFile)) {
                HashMap<String, JSONObject> lruMap = JSON.parseObject(readFile
                        , new TypeReference<HashMap<String, JSONObject>>() {
                        }.getType());
                for (Map.Entry<String, JSONObject> entry : lruMap.entrySet()) {
                    String url = entry.getValue().getString("pageUrl");
                    if (!destMap.containsKey(url)) {
                        destMap.put(url, entry.getValue());
                    }
                }
            }

            // 生成文件到// app/src/main/accets
            FileOutputStream fos = null;
            OutputStreamWriter osw = null;

            try {
                File assetsPathFile = new File(FILE_PATH);
                if (!assetsPathFile.exists()) {
                    boolean mMkdirs = assetsPathFile.mkdirs();
                    messager.printMessage(Diagnostic.Kind.NOTE, assetsPathFile.getAbsolutePath() + "不存在，创建:" + mMkdirs);
                }
                File outputFile = new File(assetsPathFile, OUTPUT_FILE_NAME);
                String content = JSON.toJSONString(destMap);


                if (outputFile.exists()) {
                    boolean mDelete = outputFile.delete();
                    messager.printMessage(Diagnostic.Kind.NOTE, outputFile.getAbsolutePath() + "存在，删除:" + mDelete);
                }
                boolean mNewFile = outputFile.createNewFile();
                messager.printMessage(Diagnostic.Kind.NOTE, "OUTPUT_FILE_NAME重新创建:" + mNewFile);

                fos = new FileOutputStream(outputFile);
                osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
                osw.write(content);
                osw.flush();
            } catch (IOException e) {
                e.printStackTrace();
                messager.printMessage(Diagnostic.Kind.NOTE, "IOException:" + e.getMessage());
            } finally {
                if (null != osw) {
                    try {
                        osw.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if (null != fos) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return true;
    }

    private String readFile() {
        FileInputStream fis = null;
        InputStreamReader isr = null;
        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();
        try {
            fis = new FileInputStream(FILE_PATH + OUTPUT_FILE_NAME);
            isr = new InputStreamReader(fis);
            br = new BufferedReader(isr);
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
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

    private void getFilePath() {
        try {
            FileObject resource = filer.createResource(StandardLocation.CLASS_OUTPUT, "", OUTPUT_FILE_NAME);
            // 位置：
            String resourcePath = resource.toUri().getPath();
            messager.printMessage(Diagnostic.Kind.NOTE, "getFilePathresourcePath:" + resourcePath);
            String appPath = resourcePath.substring(0, resourcePath.indexOf(dynamicModuleName) + dynamicModuleName.length() + 1);
            messager.printMessage(Diagnostic.Kind.NOTE, "getFilePathappPath:" + appPath);
            String mDefaultModuleName = "app";
            if (!dynamicModuleName.contains(mDefaultModuleName)) {
                appPath = appPath.replace(dynamicModuleName, mDefaultModuleName);
            }
            FILE_PATH = String.format("%s%s", appPath, "src/main/assets/");
            messager.printMessage(Diagnostic.Kind.NOTE, "getFilePathassetsPath:" + dynamicModuleName + FILE_PATH);
        } catch (IOException e) {
            e.printStackTrace();
            messager.printMessage(Diagnostic.Kind.NOTE, "getFilePath获取文件路径失败:" + e.getMessage());
        }
    }

    private void handlerDestination(Set<? extends Element> elements, Class<? extends Annotation> annotationClzs, Map<String, JSONObject> destMap) {
        for (Element element : elements) {
            TypeElement typeElement = (TypeElement) element;
            String clzName = typeElement.getQualifiedName().toString();
            int id = Math.abs(clzName.hashCode());
            String pageUrl = null;
            boolean needLogin = false;
            boolean asStarter = false;
            boolean isFragment = false;

            Annotation annotation = typeElement.getAnnotation(annotationClzs);
            if (annotation instanceof ActivityDestination) {
                isFragment = false;
                ActivityDestination activityDestination = (ActivityDestination) annotation;
                pageUrl = activityDestination.pageUrl();
                needLogin = activityDestination.needLogin();
                asStarter = activityDestination.asStarter();
            } else if (annotation instanceof FragmentDestination) {
                isFragment = true;
                FragmentDestination fragmentDesttination = (FragmentDestination) annotation;
                pageUrl = fragmentDesttination.pageUrl();
                needLogin = fragmentDesttination.needLogin();
                asStarter = fragmentDesttination.asStarter();
            }

            if (destMap.containsKey(pageUrl)) {
                messager.printMessage(Diagnostic.Kind.ERROR, "NavPorcessor不同的页面不允许使用想的pageUrl：" + clzName);
            } else {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("clzName", clzName);
                jsonObject.put("id", id);
                jsonObject.put("isFragment", isFragment);
                jsonObject.put("pageUrl", pageUrl);
                jsonObject.put("needLogin", needLogin);
                jsonObject.put("asStarter", asStarter);
                destMap.put(pageUrl, jsonObject);
            }
        }
    }
}
