package com.xumou.demo.test.script.js;

import com.xumou.demo.test.utils.ScriptUtils;
import org.junit.Before;
import org.junit.Test;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class JavaScriptDemo {

    public static final String NAME_JS = "js";
    public static final String SRC_PATH = "./src/main/resources/script/js/";

    ScriptEngine scriptEngine;

    @Before
    public void init() throws ScriptException {
        ScriptEngineManager manager = new ScriptEngineManager();
        scriptEngine = manager.getEngineByName(NAME_JS);
        setAttr("test", "test.js");
    }

    // 测试是否线程安全, 不安全
    @Test
    public void test1() throws ScriptException {
        scriptEngine.eval("var abc = (function(){ var count = 0; return function(){ count++; return count.toString(); }})()");
        execute(() -> {
            for (int i = 0; i < 10000; i++) {
                System.out.println(scriptEngine.eval("abc()"));
            }
        });
        execute(() -> {
            for (int i = 0; i < 10000; i++) {
                System.out.println(scriptEngine.eval("abc()"));
            }
        });
        for (int i = 0; i < 10000; i++) {
            System.out.println(scriptEngine.eval("abc()"));
        }
    }

    // 读入文件
    @Test
    public void test2() throws ScriptException {
        scriptEngine.eval("test.clearObj({a:1,b:2})");
    }

    // =================================================================================================================

    public void setAttr(String name, String filename) throws ScriptException {
        String str = ScriptUtils.readStrTrim(SRC_PATH, filename);
        scriptEngine.getContext().setAttribute(name, scriptEngine.eval(str), ScriptContext.ENGINE_SCOPE);
    }

    // =================================================================================================================

    ExecutorService es = Executors.newFixedThreadPool(10);

    public void execute(Run run){
        es.execute(run);
    }

    interface Run extends Runnable{
        void run2() throws Exception;
        default void run(){
            try {
                run2();
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
    }


}

