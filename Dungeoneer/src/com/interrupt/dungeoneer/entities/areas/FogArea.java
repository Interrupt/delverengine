package com.interrupt.dungeoneer.entities.areas;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.interrupt.dungeoneer.GameManager;
import com.interrupt.dungeoneer.annotations.EditorProperty;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.game.Level;
import com.interrupt.dungeoneer.gfx.GlRenderer;
import com.interrupt.dungeoneer.gfx.drawables.DrawableMesh;

public class FogArea extends Area {
    public FogArea() { hidden = true; spriteAtlas = "editor"; tex = 11; isStatic = false; isDynamic = true; }

    @EditorProperty
    public Color fogColor = new Color();

    @EditorProperty
    public float fogStart = 1;

    @EditorProperty
    public float fogEnd = 4;

    @EditorProperty
    public float viewDistance = 20;

    @EditorProperty
    public float changeTime = 300;

    private float levelFogStart = 2;
    private float levelFogEnd = 20;
    private float levelViewDistance = 20;
    private Color levelFogColor = new Color();

    public boolean playerIsInZone = false;
    public boolean isFading = false;

    private float timeInZone = 0;

    private Color workColor = new Color();

    public void init(Level level, Level.Source source) {
        levelFogStart = level.fogStart;
        levelFogEnd = level.fogEnd;
        levelFogColor.set(level.fogColor);
        levelViewDistance = level.viewDistance;
    }

    @Override
    public void tick(Level level, float delta) {
        if(Game.instance != null && Game.instance.player != null && Game.instance.level != null) {
            playerIsInZone = level.entitiesAreEncroaching(this, Game.instance.player);
        }

        GlRenderer renderer = GameManager.renderer;
        if(renderer.cutsceneCamera != null) {
            playerIsInZone = level.entitiesAreEncroaching(this, renderer.cutsceneCamera);
        }

        if(playerIsInZone) {
            timeInZone += delta;
            isFading = true;
        }
        else if(isFading) {
            timeInZone -= delta;
        }

        if(timeInZone < 0) {
            timeInZone = 0;
            isFading = false;

            // reset back to default
            GlRenderer.fogColor.set(levelFogColor);
            GlRenderer.fogEnd = levelFogEnd;
            GlRenderer.fogStart = levelFogStart;
            GlRenderer.viewDistance = levelViewDistance;
        }

        if(isFading) {
            if (timeInZone > changeTime) timeInZone = changeTime;
            float lerp = Interpolation.fade.apply(timeInZone / changeTime);

            // fade smoothly between values
            workColor.set(levelFogColor);
            GlRenderer.fogColor.set(workColor.lerp(fogColor, lerp));
            GlRenderer.fogStart = Interpolation.linear.apply(levelFogStart, fogStart, lerp);
            GlRenderer.fogEnd = Interpolation.linear.apply(levelFogEnd, fogEnd, lerp);
            GlRenderer.viewDistance = Interpolation.linear.apply(levelViewDistance, viewDistance, lerp);
        }
    }
}
