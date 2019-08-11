package com.xumou.demo.test.script.js;

import com.xumou.demo.test.utils.ScriptUtils;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import org.junit.Before;
import org.junit.Test;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class JavaScriptDemo {

    public static final String JS_PATH = "script/js/";
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
        common = readJs("common");
        es = Executors.newFixedThreadPool(10);
    }

    @Test
    public void test1(){
        setRule("name", "rule");
        Map<String, Object> map = new HashMap<>();
        map.put("name", 123);
        map.put("arr", Arrays.asList(1,2,3));
        System.out.println(call("name", map));
        ScriptUtils.sleep(1000);
    }

    public Object test(int len, Object obj){
        Integer integer = Integer.valueOf(obj.toString());
        for (int i = 0; i < len; i++) {
            if(integer.equals(i)){
                return String.valueOf(i);
            }
        }
        return -1;
    }

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

    public Object eval(String text){
        try {
            return engine.eval("(".concat(text).concat(")"));
        } catch (ScriptException e) {
            return null;
        }
    }

    public ScriptObjectMirror getJs(String funJs){
        try {
            Object eval = engine.eval("(".concat(funJs).concat(")"));
            if(eval instanceof ScriptObjectMirror){
                return (ScriptObjectMirror) eval;
            }
        } catch (ScriptException e) {
            e.printStackTrace();
        }
        return null;
    }

    public ScriptObjectMirror readJs(String name){
        String js = ScriptUtils.readResource(JS_PATH.concat(name).concat(".js"));
        try {
            Object eval = engine.eval("(".concat(js).concat(")"));
            if(eval instanceof ScriptObjectMirror){
                return (ScriptObjectMirror) eval;
            }
        } catch (ScriptException e) {
            e.printStackTrace();
        }
        return null;
    }

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
}

