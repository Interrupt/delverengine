package com.interrupt.dungeoneer.entities.spells;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.interrupt.dungeoneer.Audio;
import com.interrupt.dungeoneer.entities.*;
import com.interrupt.dungeoneer.entities.items.Potion;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.game.Level;
import com.interrupt.dungeoneer.game.Options;
import com.interrupt.dungeoneer.tiles.Tile;

import java.util.Random;

public class FillMap extends Spell {

    public FillMap() { }

    @Override
    public void doCast(Entity owner, Vector3 direction, Vector3 position) {

        // mark all tiles as seen!
        if(owner instanceof Player) {
            Level level = Game.instance.level;

            for(int mx = 0; mx < level.width; mx++) {
                for(int my = 0; my < level.height; my++) {
                    Tile t = level.getTileOrNull(mx, my);
                    if(t != null && !t.seen) {
                        t.seen = true;
                        level.dirtyMapTiles.add(new Vector2(mx, my));
                    }
                }
            }
        }
    }

    @Override
    protected void doCastEffect(Vector3 pos, Level level, Entity owner) {

        Random r = Game.rand;
        int particleCount = 20;
        particleCount *= Options.instance.gfxQuality;
        if(particleCount <= 0) particleCount = 1;

        Color spellColor = new Color(0.5f, 1f, 0.4f, 1f);

        for(int i = 0; i < particleCount; i++)
        {
            int speed = r.nextInt(45) + 10;
            Particle part = new Particle(pos.x + 0.5f + r.nextFloat() - 0.5f, pos.y + 0.5f + r.nextFloat() - 0.5f, pos.z + r.nextFloat() * 0.9f - 0.45f, 0f, 0f, 0f, 0, spellColor, true);
            part.floating = true;
            part.playAnimation(8, 12, speed);
            part.checkCollision = false;

            if(owner instanceof Player) {
                // Push the particles in the camera direction to be more visible to the player
                part.x += Game.camera.direction.x * 0.5f;
                part.y += Game.camera.direction.z * 0.5f;
                part.z += Game.camera.direction.y * 0.5f - 0.25f;
            }

            level.SpawnNonCollidingEntity(part);
        }

        level.SpawnNonCollidingEntity( new DynamicLight(pos.x + 0.5f,pos.y + 0.5f,pos.z, new Vector3(spellColor.r * 2f, spellColor.g * 2f, spellColor.b * 2f)).startLerp(new Vector3(0,0,0), 40, true).setHaloMode(Entity.HaloMode.BOTH) );

        Audio.playPositionedSound("trap_tele.mp3", new Vector3(pos.x, pos.y, pos.z), 0.6f, 12f);
    }
}
