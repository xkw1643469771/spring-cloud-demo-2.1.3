package com.xumou.demo.test.scan;

import org.junit.Test;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ScannerClassTest {

    @Test
    public void springReadClassPath() throws IOException {
        // 对应 ClassPathBeanDefinitionScanner.scanCandidateComponents()
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources("classpath*:com/xumou/**/*.class");
        for (Resource resource : resources) {
            System.out.println(resource.getURL());
        }
    }

    @Test
    public void customReadClassPath() throws IOException {
        String location = ScannerClassTest.class.getProtectionDomain().getCodeSource().getLocation().toString();
        if(location.startsWith("file:/")){
            File file = new File(location.substring(5));
            LinkedList<File> list = new LinkedList<>();
            list.add(file);
            while(!list.isEmpty()){
                File temp = list.removeFirst();
                if(temp.isDirectory()){
                    for (File listFile : temp.listFiles()) {
                        list.add(listFile);
                    }
                }else{
                    if(temp.getAbsolutePath().endsWith(".class"))
                        System.out.println(temp);
                }
            }
        }else if(location.startsWith("jar:file:/")){
            String jarPath = location.substring(9);
            int idx1 = jarPath.indexOf("!/");
            int idx2 = jarPath.indexOf("!/", idx1 + 1);
            JarFile jarFile = new JarFile(jarPath.substring(0, idx1));
            String idxPath = jarPath.substring(idx1 + 2, idx2) + "/";
            Enumeration<JarEntry> entries = jarFile.entries();
            while(entries.hasMoreElements()){
                JarEntry jarEntry = entries.nextElement();
                if(jarEntry.toString().startsWith(idxPath) && jarEntry.toString().endsWith(".class"))
                    System.out.println(jarEntry);
            }
        }

    }


}
