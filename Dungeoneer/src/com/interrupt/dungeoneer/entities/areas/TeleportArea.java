package com.interrupt.dungeoneer.entities.areas;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.interrupt.dungeoneer.Audio;
import com.interrupt.dungeoneer.annotations.EditorProperty;
import com.interrupt.dungeoneer.entities.*;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.game.Level;
import com.interrupt.dungeoneer.game.Options;

import java.util.Random;

public class TeleportArea extends Area {
    public TeleportArea() { hidden = true; spriteAtlas = "editor"; tex = 11; isStatic = false; isDynamic = true; }

    /** WarpMarker id to teleport to. */
    @EditorProperty
    public String toWarpMarkerId = null;

    /** Keep relative position offset? */
    @EditorProperty
    public boolean preserveOffset = true;

    /** Show teleport particle effect? */
    @EditorProperty
    public boolean doTeleportEffect = false;

    private final transient Vector3 t_vector3 = new Vector3();

    @Override
    public void tick(Level level, float delta) {
        Array<Entity> touching = level.getEntitiesEncroaching(this);
        for(int i = 0; i < touching.size; i++) {
            Entity e = touching.get(i);
            if(!e.isStatic) {
                teleportEntity(e, level);
            }
        }
    }

    /**
     * Teleport given entity to specified WarpMarker.
     *
     * @param e Target Entity to teleport
     * @param level Level containing target Entity
     */
    private void teleportEntity(Entity e, Level level) {
        if(toWarpMarkerId == null || toWarpMarkerId.isEmpty()) {
            return;
        }

        doEffect(t_vector3.set(e.x, e.y, e.z), level);
        putEntityAtWarpMarker(e, level);
        doEffect(t_vector3.set(e.x, e.y, e.z), level);
    }

    /**
     * Helper method to handle positioning a teleported entity.
     *
     * @param e Target Entity to teleport
     * @param level Level containing target Entity
     */
    private void putEntityAtWarpMarker(Entity e, Level level) {
        Array<Entity> found = level.getEntitiesById(toWarpMarkerId);

        // Try a more fuzzy search, as a fallback
        if(found.size == 0) {
            found = level.getEntitiesLikeId(toWarpMarkerId);

            if(found.size == 0) {
                return;
            }
        }

        float xOffset = e.x - x;
        float yOffset = e.y - y;

        Entity warpTo = found.first();
        e.setPosition(warpTo);

        if(preserveOffset) {
            e.x += xOffset;
            e.y += yOffset;

            return;
        }

        Vector3 rotation = warpTo.getRotation();
        if (e instanceof Player) {
            Player player = (Player) e;
            player.rot = (float) Math.toRadians(rotation.z + 90f);
        }
        else {
            e.setRotation(rotation.x, rotation.y, rotation.z);
        }
    }

    private void doEffect(Vector3 pos, Level level) {
        if(!doTeleportEffect) return;

        Random r = Game.rand;
        int particleCount = 20;
        particleCount *= Options.instance.gfxQuality;
        if(particleCount <= 0) particleCount = 1;

        for(int i = 0; i < particleCount; i++)
        {
            int speed = r.nextInt(45) + 10;
            Particle part = new Particle(pos.x + r.nextFloat() - 0.5f, pos.y + r.nextFloat() - 0.5f, pos.z + r.nextFloat() * 0.9f - 0.45f, 0f, 0f, 0f, 0, Color.ORANGE, true);
            part.floating = true;
            part.playAnimation(8, 12, speed);
            part.checkCollision = false;
            level.SpawnNonCollidingEntity(part);
        }

        level.SpawnNonCollidingEntity( new DynamicLight(pos.x,pos.y,pos.z, new Vector3(Color.ORANGE.r * 2f, Color.ORANGE.g * 2f, Color.ORANGE.b * 2f)).startLerp(new Vector3(0,0,0), 40, true).setHaloMode(Entity.HaloMode.BOTH) );

        Audio.playPositionedSound("trap_tele.mp3", new Vector3(pos.x, pos.y, pos.z), 0.6f, 12f);
    }

    @Override
    public void makeEntityIdUnique(String idPrefix) {
        super.makeEntityIdUnique(idPrefix);
        toWarpMarkerId = makeUniqueIdentifier(toWarpMarkerId, idPrefix);
    }
}
