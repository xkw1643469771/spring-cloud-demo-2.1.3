package com.xumou.demo.test.utils;


import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ScriptUtils {

    public static String readStr(String dir, String filename){
        return readStr(new File(dir, filename));
    }

    public static String readStrTrim(String dir, String filename){
        String result = readStr(new File(dir, filename));
        result = result.replaceAll("/\\*[\\s\\S]*\\*/","");
        result = result.replaceAll("//[^\n]*\n","");
        result = result.replaceAll("[\\r\\n]*", "");
        result = result.replaceAll(" [\\ ]*"," ");
        return result;
    }

    public static String readStr(File file){
        try {
            return readStr(new FileInputStream(file));
        }catch (Exception e){
            return "";
        }
    }

    public static String readResource(String name){
        return readStr(ClassLoader.getSystemResourceAsStream(name));
    }

    public static String readStr(InputStream is){
        return readStr(is, "UTF-8");
    }

    public static String readStr(InputStream is, String enCode){
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(is, enCode));
            String str = null;
            StringBuilder sb = new StringBuilder();
            while((str = br.readLine()) != null){
                sb.append(str).append("\n");
            }
            return sb.toString();
        }catch (Exception e){
            e.printStackTrace();
            return "";
        }finally {
            close(br);
        }
    }

    private static void close(Closeable closeable){
        try {
            closeable.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // =================================================================================================================

    private static ExecutorService es = Executors.newCachedThreadPool();

    public static void execute(Run run){
        es.execute(() -> {
            try {
                run.run();
            }catch (Exception e){
                e.printStackTrace();
            }
        });
    }

    public interface Run{
        void run() throws Exception;
    }

    // =================================================================================================================

    private static ThreadLocal<LinkedList> timer = new ThreadLocal<>();

    public static void timerStart(){
        LinkedList list = timer.get();
        if(list==null){
            timer.set(new LinkedList());
            list = timer.get();
        }
        list.add(System.currentTimeMillis());
    }

    public static void timerEnd(){
        try{
            LinkedList<Long> list = timer.get();
            long s1 = list.getLast();
            long s2 = System.currentTimeMillis();
            System.out.println("用时： " + (s2 - s1) + " 毫秒");
        }catch (Exception e){
            System.err.println("没有拿到开始时间");
        }
    }

    public static void sleep(long millis){
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}

