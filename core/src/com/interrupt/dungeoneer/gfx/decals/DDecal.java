package com.interrupt.dungeoneer.gfx.decals;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.graphics.g3d.decals.DecalMaterial;
import com.interrupt.dungeoneer.gfx.TextureAtlas;

public class DDecal extends Decal {

    public DDecal() { super(); }

    public TextureAtlas textureAtlas = null;

    public TextureAtlas getTextureAtlas() {
        return textureAtlas;
    }

    public void setTextureAtlas(TextureAtlas atlas) {
        this.textureAtlas = atlas;
    }

    public static DDecal newDecal (float width, float height, TextureRegion textureRegion) {
        return newDecal(width, height, textureRegion, DecalMaterial.NO_BLEND, DecalMaterial.NO_BLEND);
    }

    public static DDecal newDecal (float width, float height, TextureRegion textureRegion, int srcBlendFactor, int dstBlendFactor) {
        DDecal decal = new DDecal();
        decal.setTextureRegion(textureRegion);
        decal.setBlending(srcBlendFactor, dstBlendFactor);
        decal.dimensions.x = width;
        decal.dimensions.y = height;
        decal.setColor(1, 1, 1, 1);
        return decal;
    }
}
