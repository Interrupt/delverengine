package com.interrupt.dungeoneer.gfx.shaders;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.ArrayMap;

public class ShaderData {
    /** Shader name. */
    public String name = "";

    /** Filepath to vertex shader. */
    public String vertex = null;

    /** Filepath to fragment shader. */
    public String fragment = null;

    /** Shader attributes. */
    public ArrayMap<String, Object> attributes = new ArrayMap<String, Object>();

    /** Array of texture filepaths to send to shader. */
    public String[] textures = null;

    /** Texture filter. */
    public Texture.TextureFilter textureFilter = Texture.TextureFilter.Linear;

    public ShaderData() { }
}