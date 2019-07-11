package com.xumou.demo.test.script.lua;

import com.xumou.demo.test.utils.ScriptUtils;
import org.junit.Test;
import org.luaj.vm2.Globals;
import org.luaj.vm2.lib.jse.JsePlatform;

public class LuaDemo {

    @Test
    public void test(){
        Globals globals = JsePlatform.standardGlobals();
        ScriptUtils.timerStart();
        globals.load(new StringBuilder()
                .append("a=0;")
                .append("for i = 0,10000*10000,1 do")
                .append("   a = a / 2;")
                .append("   a = a + i*i;")
                .append("end;")
                .append("print(a)")
                .toString()).call();
        ScriptUtils.timerEnd();
    }

}

