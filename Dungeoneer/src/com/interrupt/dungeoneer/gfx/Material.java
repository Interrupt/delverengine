package com.interrupt.dungeoneer.gfx;

public class Material {
    /** TextureAtlas name. */
    public String texAtlas = "t1";

    /** TextureAtlas index. */
    public byte tex = 0;
    public float raiseCorners = 1f;
    public float heightNoiseAmount = 0f;
    public float heightNoiseFrequency = 2f;
    public float tileNoiseAmount = 0f;

    public Material() { }

    public Material(String texAtlas, byte tex) {
        this.texAtlas = texAtlas;
        this.tex = tex;
    }

    public boolean equals(String otherTexAtlas, byte otherTex) {
        if(otherTexAtlas == null) otherTexAtlas = "t1";
        return otherTexAtlas.equals(texAtlas) && otherTex == this.tex;
    }
}
