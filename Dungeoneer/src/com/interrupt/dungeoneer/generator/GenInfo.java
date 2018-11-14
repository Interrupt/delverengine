package com.interrupt.dungeoneer.generator;

import java.util.HashMap;

import com.badlogic.gdx.utils.Array;
import com.interrupt.dungeoneer.entities.Entity;
import com.interrupt.dungeoneer.entities.Sprite;
import com.interrupt.dungeoneer.entities.items.Sword;

public class GenInfo {
	public enum Markers { none, torch, monster, loot, key, boss, decor, decorPile, door, stairDown, stairUp, playerStart, exitLocation, secret }
	public enum CornerPlacement { never, sometimes, only}

	public String comment;
	public String pixel;
	public Markers marker = Markers.none;
	public Byte wallTex;
	public Byte floorTex;
	public Byte ceilTex;
    public String textureAtlas;
	public Array<Entity> spawns;
	public int clusterCount = 1;
	public float clusterSpread;
	public boolean attachCeiling = false;
	public boolean attachWall = false;
	public boolean attachFloor = true;
	public CornerPlacement placeInCorner = CornerPlacement.sometimes;
	public float wallOffset = 0.0f;
	public float chance = 1;
	public boolean performanceControlledClustering = false;
	public boolean solid = false;
}