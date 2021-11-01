package com.interrupt.dungeoneer.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.interrupt.dungeoneer.GameManager;
import com.interrupt.dungeoneer.editor.EditorMarker;
import com.interrupt.dungeoneer.entities.*;
import com.interrupt.dungeoneer.game.terrain.OverworldTerrainGenerator;
import com.interrupt.dungeoneer.gfx.Material;
import com.interrupt.dungeoneer.gfx.WorldChunk;
import com.interrupt.dungeoneer.partitioning.LightSpatialHash;
import com.interrupt.dungeoneer.serializers.v2.ThreadSafeLevelSerializer;
import com.interrupt.dungeoneer.tiles.Tile;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class OverworldLevel extends Level {

	public Map<String, OverworldChunk> chunks = new HashMap<String, OverworldChunk>();
	private int chunkWidth = 25;

	public long seed;

	public HashMap<String,String> areas = new HashMap<String, String>();

	public float wanderingMonsterChance = 0.2f;
	public float randomInterestChance = 0.1f;
	public Array<String> areasOfInterest = new Array<String>();

	public OverworldTerrainGenerator terrainGenerator = new OverworldTerrainGenerator();

	public Color timeOfDayAmbientLightColor = new Color(Color.WHITE);

	// pool overworld chunks!
	private Array<OverworldChunk> freeChunks = new Array<OverworldChunk>();

	private transient boolean firstLoad = true;

	public OverworldLevel()
	{
		fogStart = 35;
		fogEnd = 50;
		viewDistance = 100;
		darkness = 1;
		fogColor.set(231f / 255f, 213f / 255f, 86f / 255f, 1f);

		lightSpatialHash = new LightSpatialHash(1);

		seed = new Random().nextInt();
	}

	public OverworldLevel(int chunkWidth) {
		// make a blank level for the editor
		this.chunkWidth = chunkWidth;

		this.theme = "DUNGEON";

		tiles = new Tile[width * height];
		entities = new Array<Entity>();
		non_collidable_entities = new Array<Entity>();
		static_entities = new Array<Entity>();

		isLoaded=true;

		fogStart = 35;
		fogEnd = 50;
		viewDistance = 100;
		darkness = 1;
		fogColor.set(231f / 255f, 213f / 255f, 86f / 255f, 1f);
		skyLightColor = new Color(Color.WHITE);

		defaultWallTex = 36;

		floorPainter = new HashMap<String, Array<Float>>();
		floorPainter.put("2", new Array<Float>(new Float[] {2f, 2f, 2f, 2f, 2f, 2f, 12f, 33f}));

		seed = new Random().nextInt();
	}

	public OverworldLevel(Game game) {
		isLoaded = false;
		needsSaving = true;
		generated = true;

		seed = new Random().nextInt();
	}

	public void load(Source source) {
		needsSaving = true;
		isLoaded = true;

		entities = new Array<Entity>();
		non_collidable_entities = new Array<Entity>();
		static_entities = new Array<Entity>();

		Tile.solidWall.wallTex = (byte) defaultWallTex;

		if(source != Source.EDITOR) editorMarkers.clear();
		init(source);

		if(source != Source.EDITOR) GameManager.renderer.makeMapTextureForLevel(this);
	}

	public void init(Source source) {
		// can't init until loaded
		if(!isLoaded) return;

		viewDistance = 100;

		Tile.solidWall.wallTex = 0;

		// set default wall texture
		if(wallPainter != null) {
			if(wallPainter.containsKey("0") && wallPainter.get("0").size > 0)
				Tile.solidWall.wallTex = (byte) Math.round((wallPainter.get("0").get(0)));
		}

		Tile.solidWall.wallTex = (byte) defaultWallTex;

		if(GameManager.renderer != null) GameManager.renderer.setLevelToRender(this);

		// init the drawables
		for(int i = 0; i < entities.size; i++) {
			entities.get(i).init(this, source);
		}
		for(int i = 0; i < non_collidable_entities.size; i++) {
			non_collidable_entities.get(i).init(this, source);
		}
		for(int i = 0; i < static_entities.size; i++) {
			static_entities.get(i).init(this, source);
		}
	}

	Tile ephemeralTile = new Tile();
	public Tile getTile(int x, int y)
	{
		int xChunk = (int)Math.floor((float)x / (float)chunkWidth);
		int yChunk = (int)Math.floor((float)y / (float)chunkWidth);

		OverworldChunk c = GetChunk(xChunk, yChunk);
		if(c == null) {
			ephemeralTile.drawWalls = false;
			ephemeralTile.drawCeiling = false;
			ephemeralTile.ceilHeight = 60;
			ephemeralTile.floorHeight = -10;
			ephemeralTile.renderSolid = false;
			ephemeralTile.blockMotion = false;

			return ephemeralTile;
		}
		else {
			return c.getTile(x - (xChunk * chunkWidth), y - (yChunk * chunkWidth));
		}
	}

	public void setTile(int x, int y, Tile t) {
		int xChunk = (int)Math.floor((float)x / (float)chunkWidth);
		int yChunk = (int)Math.floor((float)y / (float)chunkWidth);

		OverworldChunk c = GetChunk(xChunk, yChunk);
		if(c != null) {
			c.setTile(x - (xChunk * chunkWidth), y - (yChunk * chunkWidth), t);
		}
	}

	public Tile getTileOrNull(int x, int y)
	{
		int xChunk = (int)Math.floor((float)x / (float)chunkWidth);
		int yChunk = (int)Math.floor((float)y / (float)chunkWidth);

		OverworldChunk c = GetChunk(xChunk, yChunk);
		if(c != null)
			return c.getTileOrNull(x - (xChunk * chunkWidth), y - (yChunk * chunkWidth));

		return null;
	}

	public void tick(float delta) {
		Player p = Game.instance.player;
		UpdateChunks(p.x + 0.5f,p.y + 0.5f);

		for(OverworldChunk chunk : chunks.values()) {
			for(int i = 0; i < chunk.entities.size; i++)
			{
				Entity e = chunk.entities.get(i);
				e.tick(this, delta);

				if(!e.isActive) toDelete.add(e);
			}

			// remove deleted entities
			for(int i = 0 ; i < toDelete.size; i++) chunk.entities.removeValue(toDelete.get(i),true);
			toDelete.clear();

			/* --- Tick Particles -- */
			for(int i = 0; i < chunk.non_collidable_entities.size; i++)
			{
				Entity e = chunk.non_collidable_entities.get(i);
				e.tick(this, delta);

				if(!e.isActive) toDelete.add(e);
			}

			// remove deleted entities
			for(int i = 0 ; i < toDelete.size; i++) chunk.non_collidable_entities.removeValue(toDelete.get(i),true);
			toDelete.clear();

			/* --- Tick Static Entities -- */
			for(int i = 0; i < chunk.static_entities.size; i++)
			{
				Entity e = chunk.static_entities.get(i);
				e.tick(this, delta);

				if(!e.isActive) toDelete.add(e);
			}

			// remove deleted entities
			for(int i = 0 ; i < toDelete.size; i++) chunk.static_entities.removeValue(toDelete.get(i),true);
			toDelete.clear();
		}

		// Update Time Of Day lighting
		//float timeOfDayMod = (float)Math.sin(GlRenderer.time * 0.75f);
		//timeOfDayAmbientLightColor.set(timeOfDayMod * 0.65f, timeOfDayMod * 0.65f, timeOfDayMod * 0.65f, 0f);
		timeOfDayAmbientLightColor.set(Color.BLACK);

		super.tick(delta);
	}

	public OverworldChunk GetChunk(Integer x, Integer y) {
		return chunks.get(x + "," + y);
	}

	public void SetChunk(Integer x, Integer y, OverworldChunk chunk) {
		chunk.xChunk = x;
		chunk.yChunk = y;
		chunks.put(x + "," + y, chunk);
	}

	public void RemoveChunk(Integer x, Integer y) {
		chunks.remove(x + "," + y);
	}

	public void RemoveFarChunks(int viewDistance, int xChunk, int yChunk) {
		// mark all chunks as needing deleted
		for(OverworldChunk c : chunks.values()) {
			if(Game.rand.nextFloat() > 0.9f)
				c.needsDeleting = true;
		}

		// now mark active chunks to not delete
		for(int x = xChunk - viewDistance; x <= xChunk + viewDistance; x++) {
			for(int y = yChunk - viewDistance; y <= yChunk + viewDistance; y++) {
				OverworldChunk c = GetChunk(x,y);
				if(c != null) {
					c.needsDeleting = false;
				}
			}
		}

		// now clear old chunks!
		overworldChunksToRemove.clear();
		for(OverworldChunk c : chunks.values()) {
			if(c.needsDeleting) overworldChunksToRemove.add(c);
		}
		for(OverworldChunk c : overworldChunksToRemove) {
			Gdx.app.log("DelverOutdoors", "Removing OverworldChunk");

			if(!freeChunks.contains(c, true))
				freeChunks.add(c);

			// clean up!
			c.dispose();

			RemoveChunk(c.xChunk, c.yChunk);
		}

		// might need to clear renderer chunks too
		rendererChunksToRemove.clear();
		for(WorldChunk c : GameManager.renderer.chunks) {
			if(!chunks.values().contains(c.overworldChunk)) {
				rendererChunksToRemove.add(c);

				if(!freeChunks.contains(c.overworldChunk, true))
					freeChunks.add(c.overworldChunk);

				Gdx.app.log("DelverOutdoors", "This chunk needs to die!");
			}
		}
		for(WorldChunk c : rendererChunksToRemove) {
			GameManager.renderer.chunks.removeValue(c, true);
		}
	}

	public void MakeNewChunks(int viewDistance, int xChunk, int yChunk)
	{
		int madeCount = 0;

		for(int x = xChunk - viewDistance; x <= xChunk + viewDistance; x++) {
			for(int y = yChunk - viewDistance; y <= yChunk + viewDistance; y++) {
				OverworldChunk c = GetChunk(x, y);

				if (c == null) {
					if(firstLoad || (madeCount < 1 && Game.rand.nextFloat() > 0.9f)) {
						if (freeChunks.size > 0) {
							c = freeChunks.pop();
							if (c != null) {
								c.reset();
							}
						} else {
							c = new OverworldChunk(chunkWidth, chunkWidth);
						}
						madeCount++;
					}
				}

				if (c != null && c.needsInitializing) {
					c.needsInitializing = false;
					SetChunk(x, y, c);
					makeNewChunk(x, y, c);
				}
			}
		}

		firstLoad = false;
	}

	Array<OverworldChunk> overworldChunksToRemove = new Array<OverworldChunk>();
	Array<WorldChunk> rendererChunksToRemove = new Array<WorldChunk>();
	public void UpdateChunks(float xx, float yy) {

		int xChunk = (int)Math.floor(xx / (float)chunkWidth);
		int yChunk = (int)Math.floor(yy / (float)chunkWidth);
		int viewDistance = 3;

		RemoveFarChunks(viewDistance, xChunk, yChunk);
		MakeNewChunks(viewDistance, xChunk, yChunk);

		width = chunkWidth * 4;
		height = chunkWidth * 4;
	}

	public void makeNewChunk(final int x, final int y, final OverworldChunk c) {
		Gdx.app.log("DelverOutdoors", "Creating new Chunk");

		Player p = Game.instance.player;
		if(c.entityIsInChunk(p) || firstLoad) {
			c.generateOnThisThread(this);
		}
		else {
			c.generateOnThread(this);
		}
	}

	public void madeNewChunkAt(int x, int y) {
		// Update Adjacent
		/*for(int xc = x-1; xc < x+1; xc++) {
			for(int yc = y-1; yc < y+1; yc++) {
				if(xc == x || yc == y) {
					OverworldChunk adjacent = GetChunk(xc, yc);
					if (adjacent != null) adjacent.isDirty = true;
				}
			}
		}*/
	}

	public String getLocationAt(int x, int y) {

		String location = areas.get(x + "," + y);

		if(location != null) {
			return location;
		}

		return null;
	}

	public void spawnMonster()
	{

	}

	public void updatePlayerSmell(int x, int y, float z, int iteration)
	{

	}

	public short getPlayerSmellAt(int x, int y)
	{
		return 0;
	}

	public void resetPlayerSmell() {

	}

	protected void updateSeenTiles(Player player) {

	}

	public void updateSpatialHash(Player player) {
		super.updateSpatialHash(player);
		for(OverworldChunk chunk : chunks.values()) {
			for(int i = 0; i < chunk.entities.size; i++)
			{
				Entity e = chunk.entities.get(i);
				if(e.isActive && e.isSolid) spatialhash.AddEntity(e);
				else if (e instanceof Item || e instanceof Door || e instanceof Stairs) spatialhash.AddEntity(e);
			}
		}
	}

	public void updateStaticSpatialHash() {
		super.updateStaticSpatialHash();
		for(OverworldChunk chunk : chunks.values()) {
			for (int i = 0; i < chunk.static_entities.size; i++) {
				Entity e = chunk.static_entities.get(i);
				if (e.isActive && e.isSolid) staticSpatialhash.AddEntity(e);
			}
		}
	}

	Material t_tileMaterial = new Material();
	public void fillTileFromLandscape(int x, int y, Tile t) {
		t.slopeNE = getLandscapeHeightAt(x, y, t_tileMaterial);
		t.slopeSE = getLandscapeHeightAt(x, y + 1);
		t.slopeNW = getLandscapeHeightAt(x + 1, y);
		t.slopeSW = getLandscapeHeightAt(x + 1, y + 1);

		clampTileSlope(t);

		if(t_tileMaterial != null) {
			t.floorTexAtlas = t_tileMaterial.texAtlas;
			t.floorTex = t_tileMaterial.tex;
		}
	}

	public float getFloatLandscapeHeightAt(float x, float y) {
		return terrainGenerator.getHeightAt(x, y, null);
	}

	public float getLandscapeHeightAt(int x, int y) {
		return terrainGenerator.getHeightAt(x, y, null);
	}

	public float getLandscapeHeightAt(int x, int y, Material outMaterial) {
		return terrainGenerator.getHeightAt(x, y, outMaterial);
	}

	public void clampTileSlope(Tile t) {
		float maxFloorHeight = t.getMaxFloorHeight();
		if(maxFloorHeight - t.slopeNE > 0.8f) t.slopeNE = maxFloorHeight - (maxFloorHeight - t.slopeNE) * 0.1f;
		if(maxFloorHeight - t.slopeNW > 0.8f) t.slopeNW = maxFloorHeight - (maxFloorHeight - t.slopeNW) * 0.1f;
		if(maxFloorHeight - t.slopeSE > 0.8f) t.slopeSE = maxFloorHeight - (maxFloorHeight - t.slopeSE) * 0.1f;
		if(maxFloorHeight - t.slopeSW > 0.8f) t.slopeSW = maxFloorHeight - (maxFloorHeight - t.slopeSW) * 0.1f;
	}

	public Entity getFoliageFor(float x, float y, float z, Random r, ThreadSafeLevelSerializer serializer) {
		return terrainGenerator.findEntityFor(x, y, z, r, serializer);
	}

	public void spawnEntitiesFromMarkers(Array<EditorMarker> markers, int xOffset, int yOffset) {
		addEntitiesFromMarkers(markers, new Array<Vector2>(), null, new Array<Vector2>(), genTheme, xOffset, yOffset);
	}

	public OverworldChunk getChunkAt(float x, float y) {
		int xChunk = (int)Math.floor(x / (float)chunkWidth);
		int yChunk = (int)Math.floor(y / (float)chunkWidth);
		return GetChunk(xChunk, yChunk);
	}

	public void updateLightSpatialHash() {
		updateSpatialLightsFrom(entities);
		updateSpatialLightsFrom(non_collidable_entities);
		updateSpatialLightsFrom(static_entities);
	}

	public void updateSpatialLightsFrom(Array<Entity> entities) {
		for(int i = 0; i < entities.size; i++)
		{
			Entity e = entities.get(i);
			if(e instanceof Light && e.isActive)
			{
				Light t = (Light)e;
				lightSpatialHash.AddLight(t);
			}
		}
	}

	public void SpawnEntity(Entity e) {
		entities.add(e);
		e.init(this, Source.LEVEL_START);
	}

	public void SpawnNonCollidingEntity(Entity e) {
		non_collidable_entities.add(e);
		e.init(this, Source.LEVEL_START);
	}

	public void SpawnStaticEntity(Entity e) {
		static_entities.add(e);
		e.init(this, Source.LEVEL_START);
	}

	@Override
	public Color getLightColorAt(float x, float y, float z, Vector3 normal, Color c) {
		c.set(ambientColor);

		// sky lights get a bit of faked ambient occlusion
		if(Options.instance.graphicsDetailLevel >= 2) {
			Tile cur = getTile((int) x, (int) y);
			if (cur.skyCeiling()) {
				for (int xx = -1; xx <= 1; xx++) {
					for (int yy = -1; yy <= 1; yy++) {
						float worldX = x + xx * 0.75f;
						float worldY = y + yy * 0.75f;
						Tile t = getTile((int) worldX, (int) worldY);
						if (t.skyCeiling() && t.getMinFloorHeight() - 0.25f <= z) {
							c.add(skyLightColor.r * 0.125f, skyLightColor.g * 0.125f, skyLightColor.b * 0.125f, 0f);
						}
					}
				}
			}
		}

		c.a = 1f;
		return c;
	}

	/*public OverworldChunk getChunkFromPool() {
		OverworldChunk c = chunkPool.obtain();
		usedChunksInPool.add(c);
		return c;
	}

	public void putChunkBackInPool(OverworldChunk c) {
		chunkPool.free(c);
	}*/
}
