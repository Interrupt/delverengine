package com.interrupt.dungeoneer.entities.areas;

import com.interrupt.dungeoneer.GameManager;
import com.interrupt.dungeoneer.annotations.EditorProperty;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.game.Level;
import com.interrupt.dungeoneer.gfx.GlRenderer;
import com.interrupt.dungeoneer.gfx.drawables.DrawableMesh;

public class SkyboxArea extends Area {
    public SkyboxArea() { hidden = true; spriteAtlas = "editor"; tex = 11; isStatic = false; isDynamic = true; }

    /** Path to mesh file of skybox. */
    @EditorProperty( group = "Visual", type = "FILE_PICKER", params = "meshes")
    public String skyboxMesh = "meshes/sky.obj";

    /** Path to texture file for skybox. */
    @EditorProperty( group = "Visual", type = "FILE_PICKER", params = "")
    public String skyboxTexture = "sky.png";

    public boolean playerIsInZone = false;

    DrawableMesh skybox = null;
    DrawableMesh defaultSkybox = null;

    public void init(Level level, Level.Source source) {
        skybox = new DrawableMesh();
        skybox.meshFile = skyboxMesh;
        skybox.textureFile = skyboxTexture;
    }

    @Override
    public void tick(Level level, float delta) {
        boolean wasInZone = playerIsInZone;

        if(Game.instance != null && Game.instance.player != null && Game.instance.level != null) {
            playerIsInZone = level.entitiesAreEncroaching(this, Game.instance.player);
        }

        GlRenderer renderer = GameManager.renderer;
        if(renderer.cutsceneCamera != null) {
            playerIsInZone = level.entitiesAreEncroaching(this, renderer.cutsceneCamera);
        }


        if(wasInZone != playerIsInZone || (playerIsInZone && GlRenderer.skybox != skybox))
            GlRenderer.skybox = playerIsInZone ? skybox : level.skybox;
    }
}
