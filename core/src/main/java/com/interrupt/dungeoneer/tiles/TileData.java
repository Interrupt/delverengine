package com.interrupt.dungeoneer.tiles;

import com.badlogic.gdx.graphics.Color;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.interrupt.dungeoneer.entities.Actor;
import com.interrupt.dungeoneer.entities.items.Weapon.DamageType;
import com.interrupt.dungeoneer.serializers.KryoSerializer;
import com.interrupt.dungeoneer.statuseffects.StatusEffect;
import com.interrupt.managers.EntityManager;

public class TileData {
	/** Is tile water? */
	public boolean isWater = false;

	/** Is tile lava? */
	public boolean isLava = false;

	/** Damage to deal to Actors in tile. */
	public int hurts = 0;

	/** Type of damage to deal. */
	public DamageType damageType = DamageType.FIRE;

	/** Particle sprite index. */
	public int particleTex = 0;

	/** Draw particle without shading? */
	public boolean particleFullbrite = false;

	/** Particle tint color. */
	public Color particleColor = new Color(Color.WHITE);

	/** Allow Entities to spawn on tile? */
	public boolean entitiesCanSpawn = true;

	/** */
	public boolean darkenFloor = false;

	/** Draw ceiling? */
	public boolean drawCeiling = true;

	/** Draw walls? */
	public boolean drawWalls = true;

	/** Texture index for vertical flowing water/lava. */
	public Integer flowTex = null;

    /** Texture index for water/lava edge treatment. */
    public Integer edgeTex = null;

    /** Texture index for vertical flowing water/lava edge treatment. */
    public Integer flowEdgeTex = null;

	/** Color of light emitted from tile. */
	public Color lightColor = null;

	/** Color of tile on map. */
	public Color mapColor = null;

	/** Friction for tile. */
	public float friction = 1;

	/** Walking sound filepath. */
	public String walkSound = "footsteps/feet_default_01.mp3,footsteps/feet_default_02.mp3,footsteps/feet_default_03.mp3,footsteps/feet_default_04.mp3,footsteps/feet_default_05.mp3,footsteps/feet_default_06.mp3,footsteps/feet_default_07.mp3,footsteps/feet_default_08.mp3,footsteps/feet_default_09.mp3";

    /** Ambient sound filepath. */
    public String ambientSound = null;

    /** Ambient sound volume. */
    public float ambientSoundVolume = 1f;

    /** Ambient sound radius. */
    public float ambientSoundRadius = 5f;

    /** Flowing sound volume. */
    public Float flowingSoundVolume = null;


    public StatusEffect applyStatusEffect = null;

	public void applyStatusEffect(Actor a) {
		if(applyStatusEffect == null)
			return;

		// Update this status effect if it's already going
		if(a.statusEffects != null && a.statusEffects.size > 0) {
			for(int i = 0; i < a.statusEffects.size; i++) {
				StatusEffect e = a.statusEffects.get(i);
				if(e.name.equals(applyStatusEffect.name)) {
					e.timer = Math.max(e.timer, applyStatusEffect.timer);
					return;
				}
			}
		}

		// Otherwise, apply it
		StatusEffect copied = (StatusEffect)KryoSerializer.copyObject(applyStatusEffect);
		if (!a.isStatusEffectActive(copied)) {
			a.addStatusEffect(copied);
		}
	}
}