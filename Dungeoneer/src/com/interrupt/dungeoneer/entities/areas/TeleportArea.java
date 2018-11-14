package com.interrupt.dungeoneer.entities.areas;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.interrupt.dungeoneer.Audio;
import com.interrupt.dungeoneer.annotations.EditorProperty;
import com.interrupt.dungeoneer.entities.DynamicLight;
import com.interrupt.dungeoneer.entities.Entity;
import com.interrupt.dungeoneer.entities.Particle;
import com.interrupt.dungeoneer.entities.Player;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.game.Level;
import com.interrupt.dungeoneer.game.Options;
import com.interrupt.dungeoneer.tiles.Tile;

import java.util.Random;

public class TeleportArea extends Area {
    public TeleportArea() { hidden = true; spriteAtlas = "editor"; tex = 11; isStatic = false; isDynamic = true; }

    @EditorProperty
    public String toWarpMarkerId = null;

    @EditorProperty
    public boolean preserveOffset = true;

    @EditorProperty
    public boolean doTeleportEffect = false;

    private transient Vector3 t_vector3 = new Vector3();

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

    public void teleportEntity(Entity e, Level level) {
        if(toWarpMarkerId != null) {
            float xTarget = e.x - x;
            float yTarget = e.y - y;
            float offset = 0;

            if(e instanceof Player) {
                xTarget += 0.5f;
                yTarget += 0.5f;
                offset = 0.5f;
            }

            doEffect(t_vector3.set(e.x + offset, e.y + offset, e.z), level);

            putEntityAtWarpMarker(e, level, toWarpMarkerId);

            if(preserveOffset) {
                e.x += xTarget;
                e.y += yTarget;
            }

            doEffect(t_vector3.set(e.x + offset, e.y + offset, e.z), level);
        }
    }

    public void putEntityAtWarpMarker(Entity e, Level level, String warpMarkerId) {
        if(warpMarkerId != null) {
            Array<Entity> found = level.getEntitiesById(warpMarkerId);

            // try a more fuzzy search, as a fallback
            if(found.size == 0) {
                found = level.getEntitiesLikeId(warpMarkerId);
            }

            if(found.size > 0) {
                Entity warpTo = found.first();
                if(e instanceof Player) {
                    Player player = (Player)e;
                    player.x = warpTo.x - 0.5f;
                    player.y = warpTo.y - 0.5f;
                    player.z = warpTo.z;

                    if(!preserveOffset)
                        player.rot = (float) Math.toRadians(warpTo.getRotation().z + 90f);
                }
                else {
                    e.x = warpTo.x;
                    e.y = warpTo.y;
                    e.z = warpTo.z;

                    if(!preserveOffset) {
                        Vector3 rot = warpTo.getRotation();
                        e.setRotation(rot.x, rot.y, rot.z);
                    }
                }
            }
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
