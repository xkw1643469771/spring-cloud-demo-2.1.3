package com.xumou.demo.test.script.js;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xumou.demo.test.utils.ScriptUtils;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import org.junit.Before;
import org.junit.Test;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class JavaScriptDemo {

    public static final String JS_PATH = "script/js/";
    public static final ObjectMapper OM = new ObjectMapper();
    public static ScriptObjectMirror common;
    public static ExecutorService es;

    ScriptEngineManager manager;
    ScriptEngine engine;
    Map<String, ScriptObjectMirror> ruleMap;

    @Before
    public void before(){
        manager = new ScriptEngineManager();
        engine = manager.getEngineByName("js");
        ruleMap = new ConcurrentHashMap<>();
        common = (ScriptObjectMirror) readJs("common").call(null);
        common.freeze();
        initRules();
        es = Executors.newFixedThreadPool(10);
    }

    public void initRules() {
        ScriptObjectMirror obj = readWarp("rules");
        Set<Map.Entry<String, Object>> entries = obj.entrySet();
        for (Map.Entry<String, Object> entry : entries) {
            if(entry.getValue() instanceof ScriptObjectMirror){
                ScriptObjectMirror temp = (ScriptObjectMirror) entry.getValue();
                if(temp.isFunction()){
                    ruleMap.put(entry.getKey(), temp);
                }
            }
        }
    }

    // 开始测试 =========================================================================================================

    @Test
    public void test1(){
        Map<String, Object> map = new HashMap<>();
        map.put("name", 123);
        map.put("arr", Arrays.asList(1,2,3));
        System.out.println(call("test1", map));
        ScriptUtils.sleep(1000);
    }

    @Test
    public void test2() throws Exception{
        List<String> list = new LinkedList<>();
        for (int i = 0; i < 4; i++) {
            list.add(String.valueOf(Math.random()));
        }
        System.out.println(call("test2", list));
    }

    // 通用封装 =========================================================================================================

    public Object call(String funName, Object ... args){
        ScriptObjectMirror fun = ruleMap.get(funName);
        if(fun == null){
            throw new RuntimeException("不存在");
        }
        return fun.call(common, args);
    }

    public void setRule(String funName, ScriptObjectMirror fun){
        if(fun.isFunction()){
            ruleMap.put(funName, fun);
        }
    }

    public void setRule(String funName, String fileName){
        ScriptObjectMirror fun = readJs(fileName);
        if(fun.isFunction()){
            ruleMap.put(funName, fun);
        }
    }

    public ScriptObjectMirror readWarp(String name){
        ScriptObjectMirror mirror = readJs(name);
        if(mirror.isFunction()){
            return (ScriptObjectMirror) mirror.call(null, common);
        }
        return null;
    }

    public ScriptObjectMirror readJs(String name){
        String js = ScriptUtils.readResource(JS_PATH.concat(name).concat(".js"));
        return getJs(js);
    }

    public ScriptObjectMirror getJs(String funJs){
        Object eval = eval(funJs);
        if(eval instanceof ScriptObjectMirror){
            return (ScriptObjectMirror) eval;
        }
        return null;
    }

    public Object eval(String text){
        try {
            return engine.eval("(".concat(text).concat(")"));
        } catch (ScriptException e) {
            return null;
        }
    }

    // js 中调用 ========================================================================================================

    public static Object jsCall(){
        Map<String, Object> map = new HashMap<>();
        map.put("name", "Tom");
        map.put("age", 34);
        map.put("sex", "男");
        map.put("arr", Arrays.asList(1,2,3,4,5,6));
        return map;
    }

    public static void jsCall(Object obj){
        if(obj instanceof ScriptObjectMirror){
            ScriptObjectMirror sObj = (ScriptObjectMirror) obj;
            if(sObj.isFunction()){
                es.execute(() -> {
                    ScriptUtils.sleep(100);
                    sObj.call(common, "SUCCESS");
                });
            }
        }
    }

    public static void debug(Object obj){
        System.out.println(obj);
    }

    public static Object call(ScriptObjectMirror fun, Object ... args){
        if(fun.isFunction())
            return fun.call(common, args);
        else
            throw new RuntimeException("不是方法");
    }
}

