package org.pacos.core.component.variable.generator;

import com.oracle.truffle.js.scriptengine.GraalJSScriptEngine;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

public class DynamicJavaScriptRunner {

    private DynamicJavaScriptRunner() {

    }

    private static final ScriptEngine jsEngine = new ScriptEngineManager().getEngineByName("graal.js");

    public static String runCode(String code) throws DynamicJavaScriptException {
        try {
            jsEngine.eval("function process(arg){\n" + code + "\n};");
            return ((GraalJSScriptEngine) jsEngine).invokeFunction("process", "") + "";
        } catch (Exception e) {
            throw new DynamicJavaScriptException(e.getMessage());
        }
    }
}
