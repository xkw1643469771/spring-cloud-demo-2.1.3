package com.xumou.demo.test.utils;


import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

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
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
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

}

