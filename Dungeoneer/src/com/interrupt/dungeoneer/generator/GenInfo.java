package com.interrupt.dungeoneer.generator;

import java.util.HashMap;

import com.badlogic.gdx.utils.Array;
import com.interrupt.dungeoneer.entities.Entity;
import com.interrupt.dungeoneer.entities.Sprite;
import com.interrupt.dungeoneer.entities.items.Sword;

public class GenInfo {
	public enum Markers { none, torch, monster, loot, key, boss, decor, decorPile, door, stairDown, stairUp, playerStart, exitLocation, secret }
	public enum CornerPlacement { never, sometimes, only}

	/** Used to document GenInfo object. */
	public String comment;

	public String pixel;

	/** Marker to place. */
	public Markers marker = Markers.none;

	/** Only place on tiles with this specific wall texture id. */
	public Byte wallTex;

	/** Only place on tiles with this specific floor texture id. */
	public Byte floorTex;

	/** Only place on tiles with this specific ceiling texture id. */
	public Byte ceilTex;

    /** TextureAtlas name for specifying tiles. */
    public String textureAtlas;

	/** Array of Entities to create at this tile. */
	public Array<Entity> spawns;

	/** Amount of Entities to create. */
	public int clusterCount = 1;

	/** Maximum radius of random placement. */
	public float clusterSpread;

	/** Attach Entities to ceiling. */
	public boolean attachCeiling = false;

	/** Attach Entities to wall. */
	public boolean attachWall = false;

	/** Attach Entities to floor. */
	public boolean attachFloor = true;

	/** Behavior for placing Entities in a corner tile. */
	public CornerPlacement placeInCorner = CornerPlacement.sometimes;

	/** How far to place Entities from wall. */
	public float wallOffset = 0.0f;

	/** Spawn chance. */
	public float chance = 1;

	/** Clustering amount controlled by graphics settings. */
	public boolean performanceControlledClustering = false;

	public boolean solid = false;
}