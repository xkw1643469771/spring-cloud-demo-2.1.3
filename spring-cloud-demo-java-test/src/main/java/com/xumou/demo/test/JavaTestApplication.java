package com.xumou.demo.test;

import com.xumou.demo.test.scan.ScannerClassTest;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;

@SpringBootApplication
public class JavaTestApplication {

    public static void main(String[] args) throws IOException {
        ScannerClassTest test = new ScannerClassTest();
        test.customReadClassPath();
        test.springReadClassPath();
    }

}
