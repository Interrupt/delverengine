package com.interrupt.managers;

import com.badlogic.gdx.Gdx;
import party.iroiro.luajava.Lua;
import party.iroiro.luajava.luajit.LuaJit;

public class ScriptingManager {
    public static ScriptingManager instance = null;
    public static void setSingleton(ScriptingManager _instance) { instance = _instance; }

    public ScriptingManager() {
        Gdx.app.log("DelverScripting", "Initializing Lua script manager");
        try {
            Lua lua = new LuaJit();
            lua.openLibraries();
            lua.run("System = java.import('java.lang.System')");
            lua.run("System.out:println('Hello World from Lua!')");
        } catch (LinkageError linkageError) {
            linkageError.printStackTrace();
        }
    }
}
