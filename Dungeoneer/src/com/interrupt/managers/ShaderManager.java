package com.interrupt.managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.ArrayMap;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.gfx.GlRenderer;
import com.interrupt.dungeoneer.gfx.shaders.ShaderData;
import com.interrupt.dungeoneer.gfx.shaders.ShaderInfo;

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
        return shaders.get(shader);
    }

    // Returns a cached shader, or tries to create one
    public ShaderInfo getCompiledShader(String shader) {
        ShaderInfo loadedShader = loadedShaders.get(shader);

        // If this is loaded already, just return it
        if(loadedShader != null) {
            return loadedShader;
        }

        // Not loaded yet, try to make it
        return makeDynamicShader(shader);
    }

    // Data definitions of shaders
    public ArrayMap<String, ShaderData> shaders = new ArrayMap<String, ShaderData>();

    // Compiled and ready to use shaders
    public ArrayMap<String, ShaderInfo> loadedShaders = new ArrayMap<String, ShaderInfo>();

    public void clearCompiledShaders() {
        loadedShaders.clear();
    }

    // Try to go load a shader from just its name
    private ShaderInfo makeDynamicShader(String shader) {
        ShaderInfo shaderInfo;

        try {
            ShaderData data = ShaderManager.getShaderManager().getShader(shader);
            String vertexShader = shader + ".vert";
            String fragmentShader = shader + ".frag";

            if (data != null) {
                if (data.vertex != null) vertexShader = data.vertex;
                if (data.fragment != null) fragmentShader = data.fragment;
            }

            ShaderProgram sp = loadShader(GlRenderer.getShaderPrefix(), vertexShader, fragmentShader);
            shaderInfo = new ShaderInfo(sp);

            if (data != null) {
                // set attributes
                if (data.attributes != null) {
                    for (int i = 0; i < data.attributes.size; i++) {
                        Object value = data.attributes.getValueAt(i);

                        // Floats and Integers might come in as a String, ugh...
                        if (value instanceof String) {
                            try {
                                Integer val = Integer.parseInt((String) value);
                                value = val;
                            } catch (Exception ex) {
                            }

                            try {
                                Float val = Float.parseFloat((String) value);
                                value = val;
                            } catch (Exception ex) {
                            }
                        }

                        shaderInfo.setAttribute(data.attributes.getKeyAt(i), value);
                    }
                }

                // set textures
                if (data.textures != null) {
                    shaderInfo.setTextures(data.textures, data.textureFilter);
                }
            }

            loadedShaders.put(shader, shaderInfo);
            return shaderInfo;
        }
        catch(Exception ex) {
            return null;
        }
    }

    // Load a shader, and throw an exception if it doesn't compile properly
    public ShaderProgram loadShader(String prefix, String vertexShaderFilename, String fragmentShaderFilename) {
        String maxDynamicLightsString = Integer.toString(GlRenderer.MAX_DYNAMIC_LIGHTS);

        try {
            ShaderProgram shaderProgram = new ShaderProgram(Game.modManager.findFile("shaders/" + prefix + vertexShaderFilename).readString().replace("{{MAX_DYNAMIC_LIGHTS}}", maxDynamicLightsString), Game.modManager.findFile("shaders/" + prefix + fragmentShaderFilename).readString().replace("{{MAX_DYNAMIC_LIGHTS}}", maxDynamicLightsString));
            if (!shaderProgram.isCompiled()) {
                Gdx.app.log("DelverShaders", shaderProgram.getLog());
                throw new GdxRuntimeException("Couldn't compile shader (" + prefix + vertexShaderFilename + "," + prefix + fragmentShaderFilename + "): " + shaderProgram.getLog());
            }
            return shaderProgram;
        }
        catch(Exception ex) {
            // Try without a prefix
            if(!prefix.equals("")) {
                return loadShader("", vertexShaderFilename, fragmentShaderFilename);
            }

            Gdx.app.log("DelverShaders", String.format("Warning: Failed to load shader: ( %s, %s )", vertexShaderFilename, fragmentShaderFilename));

            return new ShaderProgram(
                    "uniform mat4 u_projectionViewMatrix; attribute vec4 a_position; void main() { gl_Position = u_projectionViewMatrix * a_position; }",
                    "void main() { gl_FragColor = vec4(1, 0, 1, 1); }"
            );
        }
    }
}
