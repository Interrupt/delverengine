package com.interrupt.dungeoneer.tiles;

import java.io.Serializable;
import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Plane.PlaneSide;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.interrupt.dungeoneer.collision.Collision;
import com.interrupt.dungeoneer.entities.Entity;
import com.interrupt.dungeoneer.entities.Player;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.game.Level;
import com.interrupt.dungeoneer.gfx.Tesselator;
import com.interrupt.helpers.FloatTuple;
import com.interrupt.helpers.TileEdges;
import com.interrupt.helpers.Tuple;
import com.interrupt.managers.TileManager;

public class Tile implements Serializable {
	private static final long serialVersionUID = 7907569533774959788L;

	public enum TileSpaceType { EMPTY, SOLID, OPEN_NW, OPEN_NE, OPEN_SW, OPEN_SE };
	public TileSpaceType tileSpaceType = TileSpaceType.EMPTY;
	public transient boolean drawCeiling = true;
	public transient boolean drawWalls = true;
	public transient TileData data = TileManager.DEFAULT_TILEDATA;
	
	public byte tileType = 0;
	
	public boolean blockMotion = false;
	public boolean renderSolid = false;
	public boolean hide = false;
	
	public byte wallTex = 0;
    public String wallTexAtlas = null;

	public Byte wallBottomTex = null;
    public String wallBottomTexAtlas = null;
	
	public byte floorTex = 2;
	public byte ceilTex = 1;
	
	public byte floorTexRot = 0;
    public String floorTexAtlas = null;

	public byte ceilTexRot = 0;
    public String ceilTexAtlas = null;

    public Byte northTex = null;
    public Byte southTex  = null;
    public Byte eastTex = null;
    public Byte westTex = null;

    public Byte bottomNorthTex = null;
    public Byte bottomSouthTex = null;
    public Byte bottomEastTex = null;
    public Byte bottomWestTex = null;

    public String northTexAtlas = null;
    public String southTexAtlas = null;
    public String eastTexAtlas = null;
    public String westTexAtlas = null;

    public String bottomNorthTexAtlas = null;
    public String bottomSouthTexAtlas = null;
    public String bottomEastTexAtlas = null;
    public String bottomWestTexAtlas = null;

    public TileMaterials materials;
	
	public float floorHeight = -0.5f;
	public float ceilHeight = 0.5f;
	
	public boolean seen = false;
	
	public static Tile solidWall = Tile.NewSolidTile();
	public static Tile emptyWall = Tile.EmptyTile();
	
	public float slopeNW, slopeNE, slopeSE, slopeSW;
	public float ceilSlopeNW, ceilSlopeNE, ceilSlopeSE, ceilSlopeSW;
	
	public transient boolean canNav = true;
    public transient boolean canTeleportHere = true;

    public boolean isLocked = false;
	
	private static transient Plane PLANE_SE = new Plane(new Vector3(0.5f,0.5f,0), -0.701710677f);
	private static transient Plane PLANE_SW = new Plane(new Vector3(-0.5f,0.5f,0), 0f);
	private static transient Plane PLANE_NE = new Plane(new Vector3(0.5f,-0.5f,0), 0f);
	private static transient Plane PLANE_NW = new Plane(new Vector3(-0.5f,-0.5f,0), -0.701710677f);
	
	private static transient Vector3 tempVector1 = new Vector3();
	private static transient Vector3 tempVector2 = new Vector3();
	private static transient Vector3 tempVector3 = new Vector3();
	private static transient Vector3 tempVector4 = new Vector3();
	private static transient Vector3 tempVector5 = new Vector3();
	
	static {
		PLANE_SE.d = -PLANE_SE.normal.x;
		PLANE_NW.d = -PLANE_NW.normal.x;
	}
	
	public Tile()
	{
		floorTex = 2;
		ceilTex = 1;
		wallTex = 0;
	}
	
	public static Tile NewSolidTile() {
		Tile t = new Tile();
		t.blockMotion = true;
		t.renderSolid = true;
		t.wallTex = 0;
		t.tileSpaceType = TileSpaceType.SOLID;
		t.data = new TileData();
		return t;
	}
	
	public static Tile EmptyTile() {
		Tile t = new Tile();
		t.blockMotion = false;
		t.renderSolid = false;
		t.ceilHeight = 30;
		t.floorHeight = -30;
		t.wallTex = 36;
		t.data = new TileData();
		return t;
	}
	
	public static ExitTile NewExitTile() {
		ExitTile t = new ExitTile();
		t.blockMotion = true;
		t.renderSolid = true;
		t.wallTex = 0;
		t.tileSpaceType = TileSpaceType.SOLID;
		return t;
	}
	
	public boolean IsFree()
	{
		return !blockMotion;
	}
	
	public boolean IsSolid()
	{
		return renderSolid || tileSpaceType == TileSpaceType.SOLID;
	}
	
	public boolean CanSpawnHere()
	{
		return !renderSolid && !blockMotion && !data.isWater && tileSpaceType == TileSpaceType.EMPTY && hasRoomFor(0.75f) && canNav && data.entitiesCanSpawn && canTeleportHere;
	}

	public boolean CanDecorateHere() {
		return !renderSolid && !blockMotion && tileSpaceType == TileSpaceType.EMPTY && hasRoomFor(0.75f) && canNav && data.entitiesCanSpawn;
	}
	
	public float getFloorHeight()
	{
		// if water, lower the height a bit
		if(data.isWater) return floorHeight - 0.4f;
		if(data.darkenFloor) return floorHeight - 30f;
		return floorHeight;
	}
	
	public float getMaxFloorHeight()
	{
		float maxCorner = slopeNW;
		if(maxCorner < slopeNE) maxCorner = slopeNE;
		if(maxCorner < slopeSW) maxCorner = slopeSW;
		if(maxCorner < slopeSE) maxCorner = slopeSE;
		return maxCorner + floorHeight;
	}

	public float getMinFloorHeight() {
		float minCorner = slopeNW;
		if(minCorner > slopeNE) minCorner = slopeNE;
		if(minCorner > slopeSW) minCorner = slopeSW;
		if(minCorner > slopeSE) minCorner = slopeSE;
		return minCorner + floorHeight;
	}
	
	public float getFloorHeight(float x, float y) {
		
		// check if flat, can skip most checks if it is
		if(slopeNE == slopeNW && slopeNE == slopeSE && slopeNE == slopeSW) {
			float heightMod = 0;
			if(data.darkenFloor) heightMod = -30f;
			if(data.isWater) heightMod = -0.4f;
			return slopeNE + floorHeight + heightMod;
		}
		
		x = (float) (x - Math.floor(x));
		y = (float) (y - Math.floor(y));

		// check which part of the triangle we're in
		if (x + y < 1) {        // lower right
			Vector3 AA = tempVector1.set(0, 0, getNEFloorHeight());
			Vector3 AB = tempVector2.set(1 - 0, 0 - 0, getNWFloorHeight() - getNEFloorHeight());
			Vector3 AC = tempVector3.set(0 - 0, 1 - 0, getSEFloorHeight() - getNEFloorHeight());

			AA.add(AB.scl(x));
			AA.add(AC.scl(y));
			return data.isWater ? AA.z - 0.4f : AA.z;
		} else {                // upper left
			x = (x - 1) * -1f;
			y = (y - 1) * -1f;

			Vector3 AA = tempVector1.set(1, 1, getSWFloorHeight());
			Vector3 AB = tempVector2.set(0 - 1, 1 - 1, getSEFloorHeight() - getSWFloorHeight());
			Vector3 AC = tempVector3.set(1 - 1, 0 - 1, getNWFloorHeight() - getSWFloorHeight());

			AA.add(AB.scl(x));
			AA.add(AC.scl(y));
			return data.isWater ? AA.z - 0.4f : AA.z;
		}
	}

	public float getFloorHeightThreadSafe(float x, float y, Vector3 t1) {

		// check if flat, can skip most checks if it is
		if(slopeNE == slopeNW && slopeNE == slopeSE && slopeNE == slopeSW) return data.isWater ? slopeNE + floorHeight - 0.4f : slopeNE + floorHeight;

		x = (float) (x - Math.floor(x));
		y = (float) (y - Math.floor(y));

		// check which part of the triangle we're in
		if (x + y < 1) {        // lower right
			Vector3 AA = t1.set(0, 0, getNEFloorHeight());
			AA.add((1 - 0) * x, (0 - 0) * x, (getNWFloorHeight() - getNEFloorHeight()) * x);
			AA.add((0 - 0) * y, (1 - 0) * y, (getSEFloorHeight() - getNEFloorHeight()) * y);
			return data.isWater ? AA.z - 0.4f : AA.z;
		} else {                // upper left
			x = (x - 1) * -1f;
			y = (y - 1) * -1f;

			Vector3 AA = t1.set(1, 1, getSWFloorHeight());
			AA.add((0 - 1) * x, (1 - 1) * x, (getSEFloorHeight() - getSWFloorHeight()) * x);
			AA.add((1 - 1) * y, (0 - 1) * y, (getNWFloorHeight() - getSWFloorHeight()) * y);
			return data.isWater ? AA.z - 0.4f : AA.z;
		}
	}
	
	public float getCeilHeight(float x, float y) {
		
		// check if flat, can skip most checks if it is
		if(ceilSlopeNE == ceilSlopeNW && ceilSlopeNE == ceilSlopeSE && ceilSlopeNE == ceilSlopeSW) return ceilSlopeNE + ceilHeight;
		
		x = (float) (x - Math.floor(x));
		y = (float) (y - Math.floor(y));
		
		// check which part of the triangle we're in
		if (x + y < 1) {        // lower right
			Vector3 AA = tempVector1.set(0, 0, getNECeilHeight());
			Vector3 AB = tempVector2.set(1 - 0, 0 - 0, getNWCeilHeight() - getNECeilHeight());
			Vector3 AC = tempVector3.set(0 - 0, 1 - 0, getSECeilHeight() - getNECeilHeight());

			AA.add(AB.scl(x));
			AA.add(AC.scl(y));
			return AA.z;
		} else {                // upper left
			x = (x - 1) * -1f;
			y = (y - 1) * -1f;

			Vector3 AA = tempVector1.set(1, 1, getSWCeilHeight());
			Vector3 AB = tempVector2.set(0 - 1, 1 - 1, getSECeilHeight() - getSWCeilHeight());
			Vector3 AC = tempVector3.set(1 - 1, 0 - 1, getNWCeilHeight() - getSWCeilHeight());

			AA.add(AB.scl(x));
			AA.add(AC.scl(y));
			return AA.z;
		}
	}
	
	public void getFloorNormal(float x, float y, Vector3 normal) {
		// check if flat, can skip most checks if it is
		if(slopeNE == slopeNW && slopeNE == slopeSE && slopeNE == slopeSW) {
			normal.set(0, 0, 1);
			return;
		}
		
		x = (float) (x - Math.floor(x));
		y = (float) (y - Math.floor(y));
		
		// check which part of the triangle we're in
		if(x + y < 1) {		// lower right
			Vector3 AB = tempVector1.set(1 - 0, 0 - 0, getNWFloorHeight() - getNEFloorHeight());
			Vector3 AC = tempVector2.set(0 - 0, 1 - 0, getSEFloorHeight() - getNEFloorHeight());
			
			normal.set(AB.crs(AC).nor());
			return;
		}
		else { 				// upper left
			Vector3 AB = tempVector1.set(0 - 1, 1 - 1, getSEFloorHeight() - getSWFloorHeight());
			Vector3 AC = tempVector2.set(1 - 1, 0 - 1, getNWFloorHeight() - getSWFloorHeight());
			
			normal.set(AB.crs(AC).nor());
			return;
		}
	}
	
	public float getCeilingHeight()
	{
		return ceilHeight;
	}
	
	public void use()
	{
	}
	
	public boolean isWater() {
		return data != null && data.isWater && !blockMotion;
	}
	
	public boolean isSky() {
		return !drawWalls;
	}
	
	public boolean skyCeiling() {
		return !drawCeiling;
	}
	
	public float getNWFloorHeight() {
		return floorHeight + slopeNW;
	}
	
	public float getNEFloorHeight() {
		return floorHeight + slopeNE;
	}
	
	public float getSEFloorHeight() {
		return floorHeight + slopeSE;
	}
	
	public float getSWFloorHeight() {
		return floorHeight + slopeSW;
	}
	
	public float getNWCeilHeight() {
		return ceilHeight + ceilSlopeNW;
	}
	
	public float getNECeilHeight() {
		return ceilHeight + ceilSlopeNE;
	}
	
	public float getSECeilHeight() {
		return ceilHeight + ceilSlopeSE;
	}
	
	public float getSWCeilHeight() {
		return ceilHeight + ceilSlopeSW;
	}
	
	public boolean IsHigher(TileEdges direction, Tile toCompare) {
		if(direction == TileEdges.North) {
			if(getNWFloorHeight() > toCompare.getSWFloorHeight() || getNEFloorHeight() > toCompare.getSEFloorHeight()) return true;
			return false;
		}
		else if(direction == TileEdges.South) {
			if(getSWFloorHeight() > toCompare.getNWFloorHeight() || getSEFloorHeight() > toCompare.getNEFloorHeight()) return true;
			return false;
		}
		else if(direction == TileEdges.East) {
			if(getNEFloorHeight() > toCompare.getNWFloorHeight() || getSEFloorHeight() > toCompare.getSWFloorHeight()) return true;
			return false;
		}
		else if(direction == TileEdges.West) {
			if(getNWFloorHeight() > toCompare.getNEFloorHeight() || getSWFloorHeight() > toCompare.getSEFloorHeight()) return true;
			return false;
		}
		
		return false;
	}
	
	public boolean IsCeilLower(TileEdges direction, Tile toCompare) {
		if(direction == TileEdges.North) {
			if(getNWCeilHeight() < toCompare.getSWCeilHeight() || getNECeilHeight() < toCompare.getSECeilHeight()) return true;
			return false;
		}
		else if(direction == TileEdges.South) {
			if(getSWCeilHeight() < toCompare.getNWCeilHeight() || getSECeilHeight() < toCompare.getNECeilHeight()) return true;
			return false;
		}
		else if(direction == TileEdges.East) {
			if(getNECeilHeight() < toCompare.getNWCeilHeight() || getSECeilHeight() < toCompare.getSWCeilHeight()) return true;
			return false;
		}
		else if(direction == TileEdges.West) {
			if(getNWCeilHeight() < toCompare.getNECeilHeight() || getSWCeilHeight() < toCompare.getSECeilHeight()) return true;
			return false;
		}
		
		return false;
	}
	
	// rotates the slopes of the tile 90 degrees clockwise
	public void rotate90() {
		float nw = slopeNW;
		float ne = slopeNE;
		float se = slopeSE;
		float sw = slopeSW;
		
		slopeNE = se;
		slopeSE = sw;
		slopeSW = nw;
		slopeNW = ne;
		
		nw = ceilSlopeNW;
		ne = ceilSlopeNE;
		se = ceilSlopeSE;
		sw = ceilSlopeSW;
		
		ceilSlopeNE = se;
		ceilSlopeSE = sw;
		ceilSlopeSW = nw;
		ceilSlopeNW = ne;
		
		if(tileSpaceType == TileSpaceType.OPEN_NW) tileSpaceType = TileSpaceType.OPEN_NE;
		else if(tileSpaceType == TileSpaceType.OPEN_NE) tileSpaceType = TileSpaceType.OPEN_SE;
		else if(tileSpaceType == TileSpaceType.OPEN_SE) tileSpaceType = TileSpaceType.OPEN_SW;
		else if(tileSpaceType == TileSpaceType.OPEN_SW) tileSpaceType = TileSpaceType.OPEN_NW;
		
		floorTexRot++;
		floorTexRot %= 4;
		
		ceilTexRot++;
		ceilTexRot %= 4;

        Byte nt = northTex;
        Byte et = eastTex;
        Byte wt = westTex;
        Byte st = southTex;

        String nA = northTexAtlas;
        String eA = eastTexAtlas;
        String wA = westTexAtlas;
        String sA = southTexAtlas;

        northTex = et;
        eastTex = st;
        southTex = wt;
        westTex = nt;

        northTexAtlas = eA;
        eastTexAtlas = sA;
        southTexAtlas = wA;
        westTexAtlas = nA;

        Byte nbt = bottomNorthTex;
        Byte ebt = bottomEastTex;
        Byte wbt = bottomWestTex;
        Byte sbt = bottomSouthTex;

        String nbA = bottomNorthTexAtlas;
        String ebA = bottomEastTexAtlas;
        String wbA = bottomWestTexAtlas;
        String sbA = bottomSouthTexAtlas;

        bottomNorthTex= ebt;
        bottomEastTex = sbt;
        bottomSouthTex = wbt;
        bottomWestTex = nbt;

        bottomNorthTexAtlas = ebA;
        bottomEastTexAtlas = sbA;
        bottomSouthTexAtlas = wbA;
        bottomWestTexAtlas = nbA;

        if(materials != null) {
        	materials.rotate90();
        }
	}

	public static Tile copy(Tile tocopy) {
		if(tocopy == null) return null;
		
		Tile t = new Tile();
		t.blockMotion = tocopy.blockMotion;
		t.ceilHeight = tocopy.ceilHeight;
		t.ceilTex = tocopy.ceilTex;
		t.floorHeight = tocopy.floorHeight;
		t.floorTex = tocopy.floorTex;
		t.hide = tocopy.hide;
		t.renderSolid = tocopy.renderSolid;
		t.slopeNE = tocopy.slopeNE;
		t.slopeNW = tocopy.slopeNW;
		t.slopeSE = tocopy.slopeSE;
		t.slopeSW = tocopy.slopeSW;
		t.tileSpaceType = tocopy.tileSpaceType;
		t.wallTex = tocopy.wallTex;
        t.wallBottomTex = tocopy.wallBottomTex;
        t.eastTex = tocopy.eastTex;
        t.westTex = tocopy.westTex;
        t.northTex = tocopy.northTex;
        t.southTex = tocopy.southTex;
        t.bottomEastTex = tocopy.bottomEastTex;
        t.bottomWestTex = tocopy.bottomWestTex;
        t.bottomNorthTex = tocopy.bottomNorthTex;
        t.bottomSouthTex = tocopy.bottomSouthTex;
        t.floorTexAtlas = tocopy.floorTexAtlas;
        t.ceilTexAtlas = tocopy.ceilTexAtlas;
        t.wallTexAtlas = tocopy.wallTexAtlas;
        t.wallBottomTexAtlas = tocopy.wallBottomTexAtlas;
        t.bottomEastTexAtlas = tocopy.bottomEastTexAtlas;
        t.bottomWestTexAtlas = tocopy.bottomWestTexAtlas;
        t.bottomNorthTexAtlas = tocopy.bottomNorthTexAtlas;
        t.bottomSouthTexAtlas = tocopy.bottomSouthTexAtlas;
        t.isLocked = tocopy.isLocked;
		
		return t;
	}
	
	public static void copy(Tile source, Tile destination) {
		if(source == null || destination == null) return;
		
		destination.blockMotion = source.blockMotion;
		destination.ceilHeight = source.ceilHeight;
		destination.ceilTex = source.ceilTex;
		destination.floorHeight = source.floorHeight;
		destination.floorTex = source.floorTex;
		destination.hide = source.hide;
		destination.renderSolid = source.renderSolid;
		destination.slopeNE = source.slopeNE;
		destination.slopeNW = source.slopeNW;
		destination.slopeSE = source.slopeSE;
		destination.slopeSW = source.slopeSW;
		destination.ceilSlopeNE = source.ceilSlopeNE;
		destination.ceilSlopeNW = source.ceilSlopeNW;
		destination.ceilSlopeSE = source.ceilSlopeSE;
		destination.ceilSlopeSW = source.ceilSlopeSW;
		destination.tileSpaceType = source.tileSpaceType;
		destination.wallTex = source.wallTex;
        destination.wallBottomTex = source.wallBottomTex;
        destination.eastTex = source.eastTex;
        destination.westTex = source.westTex;
        destination.northTex = source.northTex;
        destination.southTex = source.southTex;
        destination.bottomEastTex = source.bottomEastTex;
        destination.bottomWestTex = source.bottomWestTex;
        destination.bottomNorthTex = source.bottomNorthTex;
        destination.bottomSouthTex = source.bottomSouthTex;
        destination.floorTexAtlas = source.floorTexAtlas;
        destination.ceilTexAtlas = source.ceilTexAtlas;
        destination.wallTexAtlas = source.wallTexAtlas;
        destination.wallBottomTexAtlas = source.wallBottomTexAtlas;
        destination.bottomEastTexAtlas = source.bottomEastTexAtlas;
        destination.bottomWestTexAtlas = source.bottomWestTexAtlas;
        destination.bottomNorthTexAtlas = source.bottomNorthTexAtlas;
        destination.bottomSouthTexAtlas = source.bottomSouthTexAtlas;
        destination.ceilTexRot = source.ceilTexRot;
        destination.floorTexRot = source.floorTexRot;
        destination.isLocked = source.isLocked;
	}

	public boolean isFlat() {
		if(slopeNW == slopeNE && slopeNW == slopeSE && slopeNW == slopeSW) return true;
		return false;
	}
	
	public FloatTuple getCeilingPair(TileEdges dir, Tesselator.TuplePool pool) {
		if(dir == TileEdges.South) {
			return pool.get(getNECeilHeight(), getNWCeilHeight());
		}
		else if(dir == TileEdges.North) {
			return pool.get(getSWCeilHeight(), getSECeilHeight());
		}
		else if(dir == TileEdges.East) {
			return pool.get(getNWCeilHeight(), getSWCeilHeight());
		}
		else if(dir == TileEdges.West) {
			return pool.get(getSECeilHeight(), getNECeilHeight());
		}
		return null;
	}

	public FloatTuple getFloorPair(TileEdges dir, Tesselator.TuplePool pool) {
		if(dir == TileEdges.South) {
			return pool.get(getNEFloorHeight(), getNWFloorHeight());
		}
		else if(dir == TileEdges.North) {
			return pool.get(getSWFloorHeight(), getSEFloorHeight());
		}
		else if(dir == TileEdges.East) {
			return pool.get(getNWFloorHeight(), getSWFloorHeight());
		}
		else if(dir == TileEdges.West) {
			return pool.get(getSEFloorHeight(), getNEFloorHeight());
		}
		return null;
	}

	public FloatTuple getCeilingPair(TileEdges dir) {
		if(dir == TileEdges.South) {
			return new FloatTuple(getNECeilHeight(), getNWCeilHeight());
		}
		else if(dir == TileEdges.North) {
			return new FloatTuple(getSWCeilHeight(), getSECeilHeight());
		}
		else if(dir == TileEdges.East) {
			return new FloatTuple(getNWCeilHeight(), getSWCeilHeight());
		}
		else if(dir == TileEdges.West) {
			return new FloatTuple(getSECeilHeight(), getNECeilHeight());
		}
		return null;
	}

	public FloatTuple getFloorPair(TileEdges dir) {
		if(dir == TileEdges.South) {
			return new FloatTuple(getNEFloorHeight(), getNWFloorHeight());
		}
		else if(dir == TileEdges.North) {
			return new FloatTuple(getSWFloorHeight(), getSEFloorHeight());
		}
		else if(dir == TileEdges.East) {
			return new FloatTuple(getNWFloorHeight(), getSWFloorHeight());
		}
		else if(dir == TileEdges.West) {
			return new FloatTuple(getSEFloorHeight(), getNEFloorHeight());
		}
		return null;
	}

	public static TileEdges opposite(TileEdges dir) {
		if(dir == TileEdges.South) return TileEdges.North;
		else if(dir == TileEdges.North) return TileEdges.South;
		else if(dir == TileEdges.East) return TileEdges.West;
		else if(dir == TileEdges.West) return TileEdges.East;
		return null;
	}
	
	public boolean collidesWithAngles(float startX, float startY, float x, float y, Vector3 collision, int tileX, int tileY, Collision hitLoc)
	{
		Plane checkPlane = null;
		
		if(tileSpaceType == TileSpaceType.OPEN_SE) {
			checkPlane = PLANE_SE;
		}
		else if(tileSpaceType == TileSpaceType.OPEN_SW)
		{
			checkPlane = PLANE_SW;
		}
		if(tileSpaceType == TileSpaceType.OPEN_NE) {
			checkPlane = PLANE_NE;
		}
		if(tileSpaceType == TileSpaceType.OPEN_NW) {
			checkPlane = PLANE_NW;
		}
		
		if(checkPlane != null)
		{
			float flooredX = (float) (x - Math.floor(x));
			float flooredY = (float) (y - Math.floor(y));
			
			Vector3 point = tempVector1.set(flooredX, flooredY, 0);
			
			if(checkPlane.testPoint(point) == PlaneSide.Back) {
				if(hitLoc != null) hitLoc.setHitNormal(checkPlane.normal);
				return true;
			}
			
			// check corners
			if((tileSpaceType == TileSpaceType.OPEN_SE || tileSpaceType == TileSpaceType.OPEN_NE)) {
				if((tileSpaceType == TileSpaceType.OPEN_SE && startX > tileY) || (tileSpaceType == TileSpaceType.OPEN_NE && startY < tileY))
				{
					if(startX - 0.5f < tileX - 1 + collision.x) return true;
				}

				if((tileSpaceType == TileSpaceType.OPEN_SE && startX > tileX))
				{
					if(startY - 0.5f < tileY - 1 + collision.y) return true;
				}
			
				if((tileSpaceType == TileSpaceType.OPEN_NE && startX > tileX))
				{
					if(startY - 0.5f > tileY - collision.y) return true;
				}
			}
			else if((tileSpaceType == TileSpaceType.OPEN_SW || tileSpaceType == TileSpaceType.OPEN_NW)) {
				if((tileSpaceType == TileSpaceType.OPEN_NW && startY < tileY) || (tileSpaceType == TileSpaceType.OPEN_SW && startY > tileY))
				{
					if(startX - 0.5f > tileX - collision.x) return true;
				}
				
				if((tileSpaceType == TileSpaceType.OPEN_NW && startX < tileX))
				{
					if(startY - 0.5f > tileY - collision.y) return true;
				}
				
				if((tileSpaceType == TileSpaceType.OPEN_SW && startX < tileX))
				{
					if(startY - 0.5f < tileY - 1 + collision.y) return true;
				}
			}
		}
		
		return false;
	}
	
	public boolean pointBehindAngle(float x, float y)
	{
		Plane checkPlane = null;
		
		if(tileSpaceType == TileSpaceType.OPEN_SE) {
			checkPlane = PLANE_SE;
		}
		else if(tileSpaceType == TileSpaceType.OPEN_SW)
		{
			checkPlane = PLANE_SW;
		}
		if(tileSpaceType == TileSpaceType.OPEN_NE) {
			checkPlane = PLANE_NE;
		}
		if(tileSpaceType == TileSpaceType.OPEN_NW) {
			checkPlane = PLANE_NW;
		}
		
		if(checkPlane != null)
		{
			float flooredX = (float) (x - Math.floor(x));
			float flooredY = (float) (y - Math.floor(y));
			
			Vector3 point = tempVector2.set(flooredX, flooredY, 0);
			
			if(checkPlane.testPoint(point) == PlaneSide.Back) {
				return true;
			}
		}
		
		return false;
	}
	
	public boolean checkAngledWallCollision(float startX, float startY, float x, float y, int tileX, int tileY, Entity e)
	{
		Plane checkPlane = null;
		
		float backToX = x - startX;
		float backToY = y - startY;
		
		if(tileSpaceType == TileSpaceType.OPEN_SE) {
			checkPlane = PLANE_SE;
		}
		else if(tileSpaceType == TileSpaceType.OPEN_SW)
		{
			checkPlane = PLANE_SW;
		}
		if(tileSpaceType == TileSpaceType.OPEN_NE) {
			checkPlane = PLANE_NE;
		}
		if(tileSpaceType == TileSpaceType.OPEN_NW) {
			checkPlane = PLANE_NW;
		}
		
		boolean hitSide = false;
		boolean hitCorner = false;
		
		float wallOffset = 0.01f;
		
		// check solid bits (East and West)
		if(tileSpaceType == TileSpaceType.OPEN_NW || tileSpaceType == TileSpaceType.OPEN_SW)
		{
			if(startX - 0.5f - e.collision.x > tileX) {
				e.xa = 0;
				e.x = tileX + e.collision.x + 0.5f + wallOffset - backToX;
				hitSide = true;
			}
		}
		else if(tileSpaceType == TileSpaceType.OPEN_NE || tileSpaceType == TileSpaceType.OPEN_SE)
		{
			if(startX + 0.5f + e.collision.x < tileX) {
				e.xa = 0;
				e.x = tileX - e.collision.x - 0.5f - wallOffset - backToX;
				hitSide = true;
			}
		}
		
		// check solid bets (North and South)
		if(tileSpaceType == TileSpaceType.OPEN_NW || tileSpaceType == TileSpaceType.OPEN_NE)
		{
			if(startY - 0.5f - e.collision.y > tileY) {
				e.ya = 0;
				e.y = tileY + e.collision.y + 0.5f + wallOffset - backToY;
				hitSide = true;
			}
		}
		else if(tileSpaceType == TileSpaceType.OPEN_SW || tileSpaceType == TileSpaceType.OPEN_SE)
		{
			if(startY + 0.5f + e.collision.y < tileY) {
				e.ya = 0;
				e.y = tileY - 0.5f - e.collision.y - wallOffset - backToY;
				hitSide = true;
			}
		}
		if(hitSide) return true;
		
		if((tileSpaceType == TileSpaceType.OPEN_SE || tileSpaceType == TileSpaceType.OPEN_NE)) {
			
			if((tileSpaceType == TileSpaceType.OPEN_SE && e.y > tileY) || (tileSpaceType == TileSpaceType.OPEN_NE && e.y < tileY))
			{
				if(startX - 0.5f < tileX - 1 + e.collision.x) {
					e.ya = 0;
					hitCorner = true;
				}
			}

			if((tileSpaceType == TileSpaceType.OPEN_SE && e.x > tileX))
			{
				if(startY - 0.5f < tileY - 1 + e.collision.y) {
					e.xa = 0;
					hitCorner = true;
				}
			}
		
			if((tileSpaceType == TileSpaceType.OPEN_NE && e.x > tileX))
			{
				if(startY - 0.5f > tileY - e.collision.y) {
					e.xa = 0;
					hitCorner = true;
				}
			}
		}
		else if((tileSpaceType == TileSpaceType.OPEN_SW || tileSpaceType == TileSpaceType.OPEN_NW)) {
			if((tileSpaceType == TileSpaceType.OPEN_NW && e.y < tileY) || (tileSpaceType == TileSpaceType.OPEN_SW && e.y > tileY))
			{
				if(startX - 0.5f > tileX - e.collision.x) {
					e.ya = 0;
					hitCorner = true;
				}
			}
			
			if((tileSpaceType == TileSpaceType.OPEN_NW && e.x < tileX))
			{
				if(startY - 0.5f > tileY - e.collision.y) {
					e.xa = 0;
					hitCorner = true;
				}
			}
			
			if((tileSpaceType == TileSpaceType.OPEN_SW && e.x < tileX))
			{
				if(startY - 0.5f < tileY - 1 + e.collision.y) {
					e.xa = 0;
					hitCorner = true;
				}
			}
		}
		else {
			return false;
		}
		if(hitCorner) return true;
		
		if(checkPlane != null)
		{
			float flooredX = (float) (x - Math.floor(x));
			float flooredY = (float) (y - Math.floor(y));
			
			Vector3 point = tempVector1.set(flooredX, flooredY, 0);
			
			if(checkPlane.testPoint(point) == PlaneSide.Back)
			{
				Vector3 onPlane = ProjectPointOnPlane(point, checkPlane);
				
				float angleOffset = 0.0001f;
				
				if(tileSpaceType == TileSpaceType.OPEN_SE) {
					onPlane.x += angleOffset;
					onPlane.y += angleOffset;
					
					e.x = tileX - backToX + onPlane.x + 1f;
					e.y = tileY - backToY + onPlane.y + 1f;
				}
				else if(tileSpaceType == TileSpaceType.OPEN_SW) {
					onPlane.x -= angleOffset;
					onPlane.y += angleOffset;
					
					e.x = tileX - backToX + onPlane.x;
					e.y = tileY - backToY + onPlane.y;
				}
				else if(tileSpaceType == TileSpaceType.OPEN_NE) {
					onPlane.x += angleOffset;
					onPlane.y -= angleOffset;
					
					e.x = tileX - backToX + onPlane.x;
					e.y = tileY - backToY + onPlane.y;
				}
				else if(tileSpaceType == TileSpaceType.OPEN_NW) {
					onPlane.x -= angleOffset;
					onPlane.y -= angleOffset;
					
					e.x = tileX - backToX + onPlane.x + 1f;
					e.y = tileY - backToY + onPlane.y + 1f;
				}
				
				return true;
			}
		}
		
		return false;
	}
	
	public static Vector3 ProjectPointOnPlane(Vector3 point, Plane plane)
	{

	  // Plane's formula is A + B + C + D = 0 where (A, B, C) is the plane's normal

	  // and D is distance to origin along plane's normal. Therefore, D * (A, B, C) is a point on the plane.

	  Vector3 pointOnPlane = tempVector1.set(plane.normal).scl(plane.d / plane.normal.len());

	
	  // Vector from some point on the plane to the passed in point.

	  Vector3 testVector = pointOnPlane.add(tempVector2.set(point).scl(-1f));

	 

	  // Cos(theta) = A.B / |A||B|

	  float cosTheta = tempVector3.set(testVector).nor().dot(new Vector3(plane.normal).scl(-1f));
	 

	  // using Cos(theta) = Adjacent / Hypotenuse.

	  // We know Hypotenuse = testVector and Adjacent = (projectedPoint - point).

	  //Vector3 projectedPoint = new Vector3(point).add(new Vector3(plane.normal).scl((testVector.len() * cosTheta)).scl(-1f));
	  
	  Vector3 newTestVec = tempVector4.set(plane.normal).scl(testVector.len() * cosTheta).scl(-1f);
	  
	  Vector3 projectedPoint = tempVector5.set(point).add(newTestVec);

	  return projectedPoint;

	}

	public boolean isTileEdgeVisible(TileEdges dir, Tile c) {
		
		// check the direction we're coming from
		if(c.tileSpaceType == TileSpaceType.OPEN_NW)
		{
			if(dir == TileEdges.East || dir == TileEdges.North) return false;
		}
		else if(c.tileSpaceType == TileSpaceType.OPEN_NE)
		{
			if(dir == TileEdges.West || dir == TileEdges.North) return false;
		}
		else if(c.tileSpaceType == TileSpaceType.OPEN_SW)
		{
			if(dir == TileEdges.East || dir == TileEdges.South) return false;
		}
		else if(c.tileSpaceType == TileSpaceType.OPEN_SE)
		{
			if(dir == TileEdges.West || dir == TileEdges.South) return false;
		}
		
		if(IsSolid()) return true;
		
		// check the direction opening to
		if(tileSpaceType == TileSpaceType.OPEN_NE)
		{
			if(dir == TileEdges.East || dir == TileEdges.South) return true;
		}
		else if(tileSpaceType == TileSpaceType.OPEN_NW)
		{
			if(dir == TileEdges.West || dir == TileEdges.South) return true;
		}
		else if(tileSpaceType == TileSpaceType.OPEN_SE)
		{
			if(dir == TileEdges.East || dir == TileEdges.North) return true;
		}
		else if(tileSpaceType == TileSpaceType.OPEN_SW)
		{
			if(dir == TileEdges.West || dir == TileEdges.North) return true;
		}
		
		return false;
	}
	
	public boolean isNorthSolid(){
		return (tileSpaceType==TileSpaceType.SOLID)||(tileSpaceType==TileSpaceType.OPEN_SE)||(tileSpaceType==TileSpaceType.OPEN_SW)||renderSolid||floorAndCeilingAreSameHeight();
	}
	public boolean isSouthSolid(){
		return (tileSpaceType==TileSpaceType.SOLID)||(tileSpaceType==TileSpaceType.OPEN_NE)||(tileSpaceType==TileSpaceType.OPEN_NW)||renderSolid||floorAndCeilingAreSameHeight();
	}
	public boolean isEastSolid(){
		return (tileSpaceType==TileSpaceType.SOLID)||(tileSpaceType==TileSpaceType.OPEN_NW)||(tileSpaceType==TileSpaceType.OPEN_SW)||renderSolid||floorAndCeilingAreSameHeight();
	}
	public boolean isWestSolid(){
		return (tileSpaceType==TileSpaceType.SOLID)||(tileSpaceType==TileSpaceType.OPEN_SE)||(tileSpaceType==TileSpaceType.OPEN_NE)||renderSolid||floorAndCeilingAreSameHeight();
	}

    public byte getWallTex(TileEdges dir) {
        if(dir == TileEdges.North && northTex != null) return northTex;
        else if(dir == TileEdges.South && southTex != null) return southTex;
        else if(dir == TileEdges.East && eastTex != null) return eastTex;
        else if(dir == TileEdges.West && westTex != null) return westTex;
        return wallTex;
    }

    public String getWallTexAtlas(TileEdges dir) {
        if(dir == TileEdges.North && northTexAtlas != null) return northTexAtlas;
        else if(dir == TileEdges.South && southTexAtlas != null) return southTexAtlas;
        else if(dir == TileEdges.East && eastTexAtlas != null) return eastTexAtlas;
        else if(dir == TileEdges.West && westTexAtlas != null) return westTexAtlas;
        else if(wallTexAtlas != null) return wallTexAtlas;
        return null;
    }

    public byte getWallBottomTex(TileEdges dir) {
        if(dir == TileEdges.North && bottomNorthTex != null) return bottomNorthTex;
        else if(dir == TileEdges.South && bottomSouthTex != null) return bottomSouthTex;
        else if(dir == TileEdges.East && bottomEastTex != null) return bottomEastTex;
        else if(dir == TileEdges.West && bottomWestTex != null) return bottomWestTex;
        else if(wallBottomTex != null) return wallBottomTex;
        return wallTex;
    }

    public String getWallBottomTexAtlas(TileEdges dir) {
        if(dir == TileEdges.North && bottomNorthTexAtlas != null) return bottomNorthTexAtlas;
        else if(dir == TileEdges.South && bottomSouthTexAtlas != null) return bottomSouthTexAtlas;
        else if(dir == TileEdges.East && bottomEastTexAtlas != null) return bottomEastTexAtlas;
        else if(dir == TileEdges.West && bottomWestTexAtlas != null) return bottomWestTexAtlas;
        else if(wallBottomTexAtlas != null) return wallBottomTexAtlas;
        else if(wallTexAtlas != null) return wallTexAtlas;
        return null;
    }

    public float getWallYOffset(TileEdges dir) {
		Float found = null;

		if(materials != null) {
			TileSurface s = materials.getTopSurface(dir);
			if(s != null) {
				found = s.yOffset;
			}
		}

		if(found == null) {
			return 0;
		}

		return found;
	}

	public void offsetTopWallSurfaces(TileEdges dir, float val) {
		if(materials == null)
			materials = new TileMaterials();

		TileSurface s = materials.getTopSurface(dir);
		if(s == null) {
			s = new TileSurface();
			materials.setTopSurface(dir, s);
		}

		s.yOffset += val;
	}

	public float getBottomWallYOffset(TileEdges dir) {
		Float found = null;

		if(materials != null) {
			TileSurface s = materials.getBottomSurface(dir);
			if(s != null) {
				found = s.yOffset;
			}
		}

		if(found == null) {
			return 0;
		}

		return found;
	}

	public void offsetBottomWallSurfaces(TileEdges dir, float val) {
		if(materials == null)
			materials = new TileMaterials();

		TileSurface s = materials.getTopSurface(dir);
		if(s == null) {
			s = new TileSurface();
			materials.setBottomSurface(dir, s);
		}

		s.yOffset += val;
	}

	public void offsetTopWallSurfaces(float amount) {
		if(materials == null)
			materials = new TileMaterials();

		for(int i = 0; i < TileEdges.values().length; i++) {
			TileEdges edge = TileEdges.values()[i];
			TileSurface surface = materials.getTopSurface(edge);
			if(surface == null) {
				surface = new TileSurface();
				materials.setTopSurface(edge, surface);
			}
			surface.yOffset += amount;
		}
	}

	public void offsetBottomWallSurfaces(float amount) {
		if(materials == null)
			materials = new TileMaterials();

		for(int i = 0; i < TileEdges.values().length; i++) {
			TileEdges edge = TileEdges.values()[i];
			TileSurface surface = materials.getBottomSurface(edge);
			if(surface == null) {
				surface = new TileSurface();
				materials.setBottomSurface(edge, surface);
			}
			surface.yOffset += amount;
		}
	}

    public void setWallTexture(TileEdges dir, byte tex, String atlas) {
		if(dir == TileEdges.North) {
			northTex = tex;
			northTexAtlas = atlas;
		}
		else if(dir == TileEdges.South) {
			southTex = tex;
			southTexAtlas = atlas;
		}
		else if(dir == TileEdges.East) {
			eastTex = tex;
			eastTexAtlas = atlas;
		}
		else if(dir == TileEdges.West) {
			westTex = tex;
			westTexAtlas = atlas;
		}
	}

	public void setBottomWallTexture(TileEdges dir, byte tex, String atlas) {
		if(dir == TileEdges.North) {
			bottomNorthTex = tex;
			bottomNorthTexAtlas = atlas;
		}
		else if(dir == TileEdges.South) {
			bottomSouthTex = tex;
			bottomSouthTexAtlas = atlas;
		}
		else if(dir == TileEdges.East) {
			bottomEastTex = tex;
			bottomEastTexAtlas = atlas;
		}
		else if(dir == TileEdges.West) {
			bottomWestTex = tex;
			bottomWestTexAtlas = atlas;
		}
	}

	public void init(Level.Source source) {
		data = TileManager.instance.getDataForTile(this);
		drawCeiling = TileManager.instance.drawCeiling(this);
		drawWalls = TileManager.instance.drawWalls(this);

        if(source == Level.Source.LEVEL_START) {
            // make unwalkable areas act like they're totally solid
            if (floorAndCeilingAreSameHeight()) {
                blockMotion = true;
            }
        }
	}
	
	public void packHeights() {
		float maxCeilHeight = Math.max(Math.max(Math.max(ceilSlopeNE, ceilSlopeNW), ceilSlopeSE), ceilSlopeSW);
		float minFloorHeight = Math.min(Math.min(Math.min(slopeNE, slopeNW), slopeSE), slopeSW);
		
		float ceilMod = maxCeilHeight;
		float floorMod = minFloorHeight;
		
		ceilHeight += maxCeilHeight;
		floorHeight += minFloorHeight;
		
		ceilSlopeNE -= ceilMod;
		ceilSlopeNW -= ceilMod;
		ceilSlopeSE -= ceilMod;
		ceilSlopeSW -= ceilMod;
		
		slopeNE -= floorMod;
		slopeNW -= floorMod;
		slopeSE -= floorMod;
		slopeSW -= floorMod;
	}
	
	public float getMinOpenHeight() {
		float min = (ceilSlopeNE + ceilHeight) - (slopeNE + floorHeight);
		min = Math.min(min, (ceilSlopeNW + ceilHeight) - (slopeNW + floorHeight));
		min = Math.min(min, (ceilSlopeSE + ceilHeight) - (slopeSE + floorHeight));
		min = Math.min(min, (ceilSlopeSW + ceilHeight) - (slopeSW + floorHeight));
		return min;
	}
	
	public boolean hasRoomFor(float collisionHeight) {
		return collisionHeight <= getMinOpenHeight();
	}

    public void compressFloorAndCeiling(boolean useFloorHeights) {
        if(useFloorHeights) {
            if (slopeNE + floorHeight > ceilSlopeNE + ceilHeight) {
                ceilSlopeNE += (slopeNE + floorHeight) - (ceilSlopeNE + ceilHeight);
            }
            if (slopeNW + floorHeight > ceilSlopeNW + ceilHeight) {
                ceilSlopeNW += (slopeNW + floorHeight) - (ceilSlopeNW + ceilHeight);
            }
            if (slopeSE + floorHeight > ceilSlopeSE + ceilHeight) {
                ceilSlopeSE += (slopeSE + floorHeight) - (ceilSlopeSE + ceilHeight);
            }
            if (slopeSW + floorHeight > ceilSlopeSW + ceilHeight) {
                ceilSlopeSW += (slopeSW + floorHeight) - (ceilSlopeSW + ceilHeight);
            }
        }
        else {
            if (slopeNE + floorHeight > ceilSlopeNE + ceilHeight) {
                slopeNE += (ceilSlopeNE + ceilHeight) - (slopeNE + floorHeight);
            }
            if (slopeNW + floorHeight > ceilSlopeNW + ceilHeight) {
                slopeNW += (ceilSlopeNW + ceilHeight) - (slopeNW + floorHeight);
            }
            if (slopeSE + floorHeight > ceilSlopeSE + ceilHeight) {
                slopeSE += (ceilSlopeSE + ceilHeight) - (slopeSE + floorHeight);
            }
            if (slopeSW + floorHeight > ceilSlopeSW + ceilHeight) {
                slopeSW += (ceilSlopeSW + ceilHeight) - (slopeSW + floorHeight);
            }
        }
    }

    public boolean floorAndCeilingAreSameHeight() {
        return slopeNE + floorHeight == ceilSlopeNE + ceilHeight
                && slopeNW + floorHeight == ceilSlopeNW + ceilHeight
                && slopeSE + floorHeight == ceilSlopeSE + ceilHeight
                && slopeSW + floorHeight == ceilSlopeSW + ceilHeight;
    }

    public void lockTile() {
		isLocked = true;
	}

	public boolean isTileLocked() {
		return isLocked;
	}
}
