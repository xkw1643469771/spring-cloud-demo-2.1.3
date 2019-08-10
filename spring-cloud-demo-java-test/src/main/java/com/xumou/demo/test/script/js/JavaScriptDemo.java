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

public class JavaScriptDemo {

    public static final String JS_PATH = "script/js/";
    ScriptEngineManager manager;
    ScriptEngine engine;
    Map<String, ScriptObjectMirror> ruleMap;
    ScriptObjectMirror common;

    @Before
    public void before(){
        manager = new ScriptEngineManager();
        engine = manager.getEngineByName("js");
        ruleMap = new ConcurrentHashMap<>();
        common = readJs("common");
    }

    @Test
    public void test1(){
        setRule("name", "rule");
        Map<String, Object> map = new HashMap<>();
        map.put("name", 123);
        map.put("arr", Arrays.asList(1,2,3,4,5,6,6,6,6,7));
        System.out.println(call("name", map));
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

    public static void jsCall(){
        System.out.println("js 调用 java");
    }

}

