package com.interrupt.dungeoneer.rpg;

import com.badlogic.gdx.utils.Array;

public class Class {
	public String name = "ROGUE";
	public Stats baseStats = new Stats();
	public boolean locked = false;
	
	public String startsWith = "";
	
	public Class() { }
	
	public Class(String name, Stats stats, String startsWith, boolean locked) {
		this.name = name;
		this.baseStats = stats;
		this.locked = locked;
		this.startsWith = startsWith;
	}
}
