package com.interrupt.dungeoneer.entities.spells;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.interrupt.dungeoneer.Audio;
import com.interrupt.dungeoneer.entities.*;
import com.interrupt.dungeoneer.entities.items.Armor;
import com.interrupt.dungeoneer.entities.items.ItemModification;
import com.interrupt.dungeoneer.entities.items.Potion;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.game.Level;
import com.interrupt.dungeoneer.game.Options;

import java.util.Random;

public class Identify extends Spell {

    public Identify() { }

    @Override
    public void doCast(Entity owner, Vector3 direction, Vector3 position) {

        if(owner instanceof Player) {
            Player p = (Player)owner;

            for(Item i : p.inventory) {
                if(i != null && !i.identified) {
                    p.history.identified(i);
                    i.identified = true;
                }

                // identify potions
                if(i != null && (i instanceof Potion)) {
                    Potion potion = (Potion)i;
                    if(!p.discoveredPotions.contains(potion.potionType, true)) {
                        p.history.identified(i);
                        p.discoveredPotions.add(potion.potionType);
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
            level.SpawnNonCollidingEntity(part);
        }

        level.SpawnNonCollidingEntity( new DynamicLight(pos.x + 0.5f,pos.y + 0.5f,pos.z, new Vector3(spellColor.r * 2f, spellColor.g * 2f, spellColor.b * 2f)).startLerp(new Vector3(0,0,0), 40, true).setHaloMode(Entity.HaloMode.BOTH) );

        Audio.playPositionedSound("trap_tele.mp3", new Vector3(pos.x, pos.y, pos.z), 0.6f, 12f);
    }
}
