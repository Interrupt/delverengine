package com.interrupt.managers;

import com.badlogic.gdx.utils.ArrayMap;
import com.interrupt.dungeoneer.gfx.shaders.ShaderData;

public class ShaderManager {
    private static ShaderManager singleton = new ShaderManager();
    public static boolean loaded = false;

    public static void setSingleton(ShaderManager singleton) {
        ShaderManager.singleton = singleton;
        loaded = true;
    }

    public static ShaderManager getShaderManager() {
        return ShaderManager.singleton;
    }

    public ShaderData getShader(String shader) {
        ShaderData data = shaders.get(shader);
        return data;
    }

    public ArrayMap<String, ShaderData> shaders = new ArrayMap<String, ShaderData>();
}
