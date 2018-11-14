package com.interrupt.helpers;

import com.interrupt.dungeoneer.rpg.Stats;

public class Upgrade {
	public String name = "BOOST";
	public Stats stats = new Stats(0,0,0,0,0,0);
	
	public Upgrade() { }
	public Upgrade(String name, Stats stats) { this.name = name; this.stats = stats; }
}
