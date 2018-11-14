package com.interrupt.dungeoneer.entities.spells;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.interrupt.dungeoneer.Audio;
import com.interrupt.dungeoneer.entities.*;
import com.interrupt.dungeoneer.entities.items.ItemModification;
import com.interrupt.dungeoneer.entities.items.Weapon;
import com.interrupt.dungeoneer.game.Colors;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.game.Level;
import com.interrupt.dungeoneer.game.Options;

import java.util.Random;

public class EnchantWeapon extends Spell {

    public EnchantWeapon() { }

    @Override
    public void doCast(Entity owner, Vector3 direction, Vector3 position) {

        if(owner instanceof Player) {
            Player p = (Player)owner;

            Item held = p.GetHeldItem();
            if(held != null && (held instanceof Weapon)) {
                if(held.prefixEnchantment == null) {
                    ItemModification mod = new ItemModification();
                    held.prefixEnchantment = mod;
                    mod.name = "Enchanted";
                }
                held.prefixEnchantment.increaseAttackMod(Game.rand.nextInt(2) + 1);
            }
        }
    }

    @Override
    protected void doCastEffect(Vector3 pos, Level level, Entity owner) {

        Random r = Game.rand;
        int particleCount = 20;
        particleCount *= Options.instance.gfxQuality;
        if(particleCount <= 0) particleCount = 1;

        for(int i = 0; i < particleCount; i++)
        {
            int speed = r.nextInt(45) + 10;
            Particle part = new Particle(pos.x + 0.5f + r.nextFloat() - 0.5f, pos.y + 0.5f + r.nextFloat() - 0.5f, pos.z + r.nextFloat() * 0.9f - 0.45f, 0f, 0f, 0f, 0, Colors.ENCHANT_ITEM, true);
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

        level.SpawnNonCollidingEntity( new DynamicLight(pos.x + 0.5f,pos.y + 0.5f,pos.z, new Vector3(Colors.ENCHANT_ITEM.r * 2f, Colors.ENCHANT_ITEM.g * 2f, Colors.ENCHANT_ITEM.b * 2f)).startLerp(new Vector3(0,0,0), 40, true).setHaloMode(Entity.HaloMode.BOTH) );

        Audio.playPositionedSound("trap_tele.mp3", new Vector3(pos.x, pos.y, pos.z), 0.6f, 12f);
    }
}
