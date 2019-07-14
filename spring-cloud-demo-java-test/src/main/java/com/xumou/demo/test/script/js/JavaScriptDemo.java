package com.xumou.demo.test.script.js;

import com.xumou.demo.test.utils.ScriptUtils;
import org.junit.Before;
import org.junit.Test;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

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

    // 测试js时间
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

    // 测试java时间
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

    //和java交互
    @Test
    public void test5() throws ScriptException {
        Map map = new HashMap();
        map.put("a", "123");
        map.put("b", "123");
        map.put("c", new BigDecimal("12312312312321313131312313123123123123"));
        scriptEngine.put("a", map);
        scriptEngine.eval("test.params(a.a,a.b,a.c)");
    }

    //长整数
    @Test
    public void test6() throws ScriptException {
        Object eval = scriptEngine.eval("var a = test.bigNumber();");
        System.out.println(scriptEngine.get("a"));
        System.out.println(new BigDecimal(scriptEngine.get("a").toString()));
    }

    //测试语法
    @Test
    public void test7() throws ScriptException {
        Object eval = scriptEngine.eval("var a = {}; a[123]=342342; a['123123']=123123; var b = {}; a[b] = 423432;print(a[b])");
    }
    // =================================================================================================================

    public void setAttr(String name, String filename) throws ScriptException {
        String str = ScriptUtils.readStrTrim(SRC_PATH, filename);
        scriptEngine.getContext().setAttribute(name, scriptEngine.eval(str), ScriptContext.ENGINE_SCOPE);
    }

}

