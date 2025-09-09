package com.interrupt.dungeoneer.statuseffects;

public class ShieldEffect extends StatusEffect {
	public ShieldEffect() { }
	
	public ShieldEffect(String name, float damageMod, float magicDamageMod, int time) {
		this.name = name;
		this.timer = time;
		this.statusEffectType = StatusEffectType.SHIELD;
		
		this.damageMod = damageMod;
		this.magicDamageMod = magicDamageMod;
	}
}
