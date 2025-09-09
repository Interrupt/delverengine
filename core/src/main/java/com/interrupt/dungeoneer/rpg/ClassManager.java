package com.interrupt.dungeoneer.rpg;

import com.badlogic.gdx.utils.Array;

public class ClassManager {

	private Array<Class> classes = new Array<Class>();
	
	public ClassManager() {
		classes.add(new Class("ROGUE", new Stats(4, 4, 4, 4, 4, 4), "DAGGER,FOOD,WAND,POTION", false));
		classes.add(new Class("ARCHER", new Stats(2, 2, 6, 6, 2, 4), "BOW,FOOD,WAND,POTION", true));
		classes.add(new Class("WIZARD", new Stats(2, 2, 2, 6, 6, 4), "BOOK,FOOD,WAND,POTION", true));
	}
	
	public Array<Class> getClasses() {
		return classes;
	}
}
