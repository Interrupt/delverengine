package com.interrupt.dungeoneer.gfx.shaders;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.ArrayMap;

public class ShaderData {
    public String name = "";
    public String vertex = null;
    public String fragment = null;
    public ArrayMap<String, Object> attributes = new ArrayMap<String, Object>();
    public String[] textures = null;

    public Texture.TextureFilter textureFilter = Texture.TextureFilter.Linear;

    public ShaderData() { }
}