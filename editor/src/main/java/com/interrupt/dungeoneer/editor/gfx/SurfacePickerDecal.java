package com.interrupt.dungeoneer.editor.gfx;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.graphics.g3d.decals.DecalMaterial;
import com.badlogic.gdx.math.Vector3;

import java.util.Random;

public class SurfacePickerDecal extends Decal {

    public Vector3 top_left_offset = new Vector3();
    public Vector3 top_right_offset = new Vector3();
    public Vector3 bot_left_offset = new Vector3();
    public Vector3 bot_right_offset = new Vector3();

    public static SurfacePickerDecal newDecal (float width, float height, TextureRegion textureRegion) {
        SurfacePickerDecal decal = new SurfacePickerDecal();
        decal.setTextureRegion(textureRegion);
        decal.setBlending(DecalMaterial.NO_BLEND, DecalMaterial.NO_BLEND);
        decal.dimensions.x = width;
        decal.dimensions.y = height;
        decal.setColor(1, 1, 1, 1);
        return decal;
    }

    public SurfacePickerDecal() {
        super();
    }

    public void setTopLeftOffset(float x, float y, float z) {
        top_left_offset.set(x, y, z).scl(scale.x);
        updated = false;
    }

    public void setTopRightOffset(float x, float y, float z) {
        top_right_offset.set(x, y, z).scl(scale.x);
        updated = false;
    }

    public void setBottomLeftOffset(float x, float y, float z) {
        bot_left_offset.set(x, y, z).scl(scale.x);
        updated = false;
    }

    public void setBottomRightOffset(float x, float y, float z) {
        bot_right_offset.set(x, y, z).scl(scale.x);
        updated = false;
    }

    @Override
    protected void resetVertices () {
        super.resetVertices();

        // left top
        vertices[X1] += top_left_offset.x;
        vertices[Y1] += top_left_offset.y;
        vertices[Z1] += top_left_offset.z;
        // right top
        vertices[X2] += top_right_offset.x;
        vertices[Y2] += top_right_offset.y;
        vertices[Z2] += top_right_offset.z;
        // left bot
        vertices[X3] += bot_left_offset.x;
        vertices[Y3] += bot_left_offset.y;
        vertices[Z3] += bot_left_offset.z;
        // right bot
        vertices[X4] += bot_right_offset.x;
        vertices[Y4] += bot_right_offset.y;
        vertices[Z4] += bot_right_offset.z;
    }
}
