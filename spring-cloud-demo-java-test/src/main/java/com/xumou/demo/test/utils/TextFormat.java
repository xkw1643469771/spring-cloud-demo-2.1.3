package com.xumou.demo.test.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class TextFormat {

    public static void main(String[] args) throws IOException {
        replaceEnglish("Kitty Carstairs");
    }

    public static void replaceEnglish(String name)  {
            replace("F:\\english\\"+name+".txt");

    }

    public static void replace(String name)  {
        try {
            replace(name, 80);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void replace(String name, int len) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(name)));
        StringBuilder sb = new StringBuilder();
        for (String i = br.readLine(); i != null ; i = br.readLine()) {
            int start = 0, end = 0;
            while(true){
                end += len;
                end = i.indexOf(' ', end);
                if(end == -1){
                    sb.append(i.substring(start)).append('\n');
                    break;
                }
                end++;
                sb.append(i.substring(start, end)).append('\n');
                start = end;
            }
            sb.append('\n');
        }
        br.close();
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(name)));
        bw.write(sb.toString());
        bw.flush();
        bw.close();
    }

}
