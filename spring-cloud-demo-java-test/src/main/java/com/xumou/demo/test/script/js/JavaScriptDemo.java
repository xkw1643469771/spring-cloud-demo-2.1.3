package com.xumou.demo.test.script.js;

import com.xumou.demo.test.utils.ScriptUtils;
import org.junit.Before;
import org.junit.Test;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

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
        ScriptUtils.execute(() -> {
            for (int i = 0; i < 10000; i++) {
                System.out.println(scriptEngine.eval("abc()"));
            }
        });
        ScriptUtils.execute(() -> {
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

    // 测试时间
    @Test
    public void test3() throws ScriptException{
        ScriptUtils.timerStart();
        scriptEngine.eval(new StringBuilder()
                .append("var count = 0;")
                .append("for(var i = 0; i <= 10000*10000; i++){")
                .append("   count /= 2;")
                .append("   count += i * i;")
                .append("}")
                .append("print(count)")
                .toString());
        ScriptUtils.timerEnd();
    }

    // 测试时间
    @Test
    public void test4() throws ScriptException{
        ScriptUtils.timerStart();
        long count = 0;
        for(long i = 0; i <= 10000*10000; i++){
            count /= 2;
            count += i * i;
        }
        System.out.println(count);
        ScriptUtils.timerEnd();
    }

    // =================================================================================================================

    public void setAttr(String name, String filename) throws ScriptException {
        String str = ScriptUtils.readStrTrim(SRC_PATH, filename);
        scriptEngine.getContext().setAttribute(name, scriptEngine.eval(str), ScriptContext.ENGINE_SCOPE);
    }

}

