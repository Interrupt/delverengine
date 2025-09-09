package com.interrupt.dungeoneer.entities.areas;

import com.badlogic.gdx.utils.Array;
import com.interrupt.dungeoneer.annotations.EditorProperty;
import com.interrupt.dungeoneer.entities.Actor;
import com.interrupt.dungeoneer.entities.Entity;
import com.interrupt.dungeoneer.game.Level;
import com.interrupt.dungeoneer.statuseffects.StatusEffect;

public class StatusEffectArea extends Area {
    /** Status effect type. */
    @EditorProperty
    public StatusEffect.StatusEffectType effectType = StatusEffect.StatusEffectType.POISON;

    /** Show particle effect? */
    @EditorProperty
    public boolean showParticleEffect = true;

    /** Status effect duration. */
    @EditorProperty
    public float statusEffectTime = 20f;

    /** Does effect stop when exiting the area? */
    public boolean stopOnExit = false;

    public Array<Entity> entitiesEncroaching = new Array<Entity>();

    public StatusEffectArea() {
        this.hidden = true;
        this.spriteAtlas = "editor";
        this.tex = 11;
        this.isStatic = false;
        this.isDynamic = true;
        this.collision.set(0.5f, 0.5f, 0.25f);
    }

    @Override
    public void tick(Level level, float delta) {
        Array<Entity> es = level.getEntitiesEncroaching(this);

        // Entering area
        for (Entity e : es) {
            // Only Actors care about Status Effects
            if (!(e instanceof Actor)) {
                continue;
            }

            this.applyStatusEffect((Actor)e);
        }
    }

    private void applyStatusEffect(Actor a) {

        if(a.statusEffects != null && a.statusEffects.size > 0) {
            for(int i = 0; i < a.statusEffects.size; i++) {
                StatusEffect e = a.statusEffects.get(i);
                if(e.statusEffectType == effectType) {
                    if(e.timer <= statusEffectTime)
                        e.timer = statusEffectTime;
                    return;
                }
            }
        }

        StatusEffect se = StatusEffect.getStatusEffect(this.effectType);
        if (se == null) {
            return;
        }

        se.showParticleEffect = this.showParticleEffect;
        se.timer = statusEffectTime;

        if (!a.isStatusEffectActive(se)) {
            a.addStatusEffect(se);
        }
    }
}
