package com.xumou.demo.test.script.expr;

import com.greenpineyu.fel.Expression;
import com.greenpineyu.fel.FelEngine;
import com.greenpineyu.fel.FelEngineImpl;
import com.greenpineyu.fel.context.FelContext;
import com.greenpineyu.fel.context.MapContext;
import com.xumou.demo.test.utils.ScriptUtils;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import org.junit.Test;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.HashMap;
import java.util.Map;

/**
 * 计算引擎， 可以用来计算搭配公式计算费用
 */
public class FelEngineDemo {

    // Fel 初步使用
    @Test
    public void test1(){
        FelEngine felEngine = new FelEngineImpl();
        FelContext context = felEngine.getContext();
        context.set("单价", 100);
        context.set("数量", 34);
        Object eval = felEngine.eval("单价*数量");
        System.out.println(eval);
    }

    // Fel 效率对比1： 每次新建引擎
    @Test
    public void test2() throws ScriptException {
        ScriptUtils.timerStart();
        for (int i = 0; i < 100; i++) {
            // 922 毫秒
            ScriptEngine engine = new ScriptEngineManager().getEngineByName("js");
            engine.put("单价", i);
            engine.put("数量", i);
            Object eval1 = engine.eval("单价*数量");

            //1681 毫秒
//            FelEngine felEngine = new FelEngineImpl();
//            FelContext context = felEngine.getContext();
//            context.set("单价", i);
//            context.set("数量", i);
//            Object eval = felEngine.eval("单价*数量");
        }
        ScriptUtils.timerEnd();
    }

    // Fel 效率对比2 ： 预先得到引擎
    @Test
    public void test3() throws ScriptException {
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("js");
        FelEngine felEngine = new FelEngineImpl();
        ScriptUtils.timerStart();
        for (int i = 0; i < 10000*1000; i++) {
            //用时： 50569 毫秒
//            engine.put("单价", i);
//            engine.put("数量", i);
//            Object eval1 = engine.eval("单价*数量");

            //用时： 32710 毫秒
            FelContext context = felEngine.getContext();
            context.set("单价", 1.0);
            context.set("数量", 1.0);
            Object eval = felEngine.eval("单价*数量");
        }
        ScriptUtils.timerEnd();
    }

    // Fel 效率对比3 预编译
    @Test
    public void test4() throws ScriptException {
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("js");
        ScriptObjectMirror fun = (ScriptObjectMirror) engine.eval(
                "(function(map){return map.单价*map.数量})");
        Map map = new HashMap();

        FelEngine felEngine = new FelEngineImpl();
        FelContext context = felEngine.getContext();
        context.set("单价", 1.0);
        context.set("数量", 1.0);
        Expression compile = felEngine.compile("单价*数量", context);

        Object eval = null;
        ScriptUtils.timerStart();
        for (int i = 0; i < 10000*1000; i++) {
            // 用时： 1284 毫秒 传map 线程安全， 需要改变公式
            // 用时：  886 毫秒 直接传参 线程安全, 需要定义入参, 不灵活
//            map.put("单价", i);
//            map.put("数量", i);
//            eval = fun.call(null, map);

            // 用时： 235 毫秒 灵活, 速度快，但有类型转换问题，使用了基本类型计算, 会丢失精度
            // 0.9 版本可以支持大数值， 但是效率低了
            context.set("单价", (double)i);
            context.set("数量", (double)i);
            eval = compile.eval(context);
        }
        ScriptUtils.timerEnd();
        System.out.println(eval);
    }

    // Fel 效率对比3 还是预编译
    @Test
    public void test5() throws ScriptException {
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("js");
        String funStr = "(function(){return obj;})";
        ScriptObjectMirror func = (ScriptObjectMirror) engine.eval(funStr.replace("obj",
                "a*b"));

        FelEngine felEngine = new FelEngineImpl();
        FelContext context = felEngine.getContext();
        context.set("a", 1.0);
        context.set("b", 1.0);
        Expression compile = felEngine.compile("a*b", context);

        Object eval = null;
        ScriptUtils.timerStart();
        for (int i = 0; i < 10000*1000; i++) {
            // 用时： 2543 毫秒： 提前编译，速度快，灵活,无类型问题, js有精度问题
            engine.put("a", i);
            engine.put("b", i);
            eval = func.call(null);

            // 用时：  240 毫秒： 提前编译，速度快，灵活，但需要考虑类型问题
//            context.set("a", (double) i);
//            context.set("b", (double) i);
//            eval = compile.eval(context);
        }
        ScriptUtils.timerEnd();
        System.out.println(eval);
    }

    // FelEngine 批量计算，预编译
    @Test
    public void test7(){
        FelContext c1 = new MapContext();
        c1.set("单价", 1);
        c1.set("数量", 1);
        Expression e1 = FelEngine.instance.compile("单价*数量", c1);

        FelContext c2 = new MapContext();
        c2.set("单价", 1.0);
        c2.set("数量", 1.0);
        Expression e2 = FelEngine.instance.compile("单价*数量", c2);

        c1.set("单价", 2);
        c1.set("数量", 3);
        e1.eval(c1);
        System.out.println(e1.eval(c1));

        c2.set("单价", 4*1.0);
        c2.set("数量", 5*1.0);
        System.out.println(e2.eval(c2));

        System.out.println(e1.eval(c1));
    }
}
