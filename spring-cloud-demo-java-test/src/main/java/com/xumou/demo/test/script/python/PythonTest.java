package com.xumou.demo.test.script.python;

import com.xumou.demo.test.utils.ScriptUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;

public class PythonTest {

    public static void main(String[] args) {
        System.out.println(execPy("test"));
    }

    private static String execPy(String name){
        try {
            Process process = Runtime.getRuntime().exec(py(name));
            String err = ScriptUtils.readStr(process.getErrorStream());
            Optional.ofNullable(err).ifPresent(e -> {
                if(e.trim().length() > 0){
                    throw new RuntimeException(e);
                }
            });
            return ScriptUtils.readStr(process.getInputStream());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String py(String name, String ... args){
        return String.format("python ".concat(pyPath(name)), args);
    }

    private static String pyPath(String pyName) {
        URL url = PythonTest.class.getClassLoader().getResource("");
        return new File(url.getFile(), "script/py/".concat(pyName).concat(".py")).getAbsolutePath();
    }

}
