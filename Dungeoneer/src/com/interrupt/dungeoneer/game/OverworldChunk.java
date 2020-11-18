package com.interrupt.dungeoneer.game;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.interrupt.dungeoneer.GameManager;
import com.interrupt.dungeoneer.editor.EditorMarker;
import com.interrupt.dungeoneer.entities.*;
import com.interrupt.dungeoneer.game.terrain.threading.OverworldGenerationThread;
import com.interrupt.dungeoneer.generator.DungeonGenerator;
import com.interrupt.dungeoneer.generator.GenInfo;
import com.interrupt.dungeoneer.generator.GenInfo.Markers;
import com.interrupt.dungeoneer.generator.GenTheme;
import com.interrupt.dungeoneer.gfx.GlRenderer;
import com.interrupt.dungeoneer.gfx.TesselationThreadRunnable;
import com.interrupt.dungeoneer.gfx.WorldChunk;
import com.interrupt.dungeoneer.serializers.KryoSerializer;
import com.interrupt.dungeoneer.serializers.v2.ThreadSafeLevelSerializer;
import com.interrupt.dungeoneer.tiles.Tile;
import com.interrupt.managers.EntityManager;
import com.interrupt.utils.JsonUtil;

import java.util.Random;

public class OverworldChunk implements Pool.Poolable {

	public transient Tile[] tiles;
	public int width;
	public int height;

	public int xChunk;
	public int yChunk;

	public String tileToLoad = null;

	public transient boolean needsInitializing = true;
	public transient boolean refresh = false;
	public transient boolean needsDeleting = false;

	public transient Array<Entity> entities = new Array<Entity>();
	public transient Array<Entity> non_collidable_entities = new Array<Entity>();
	public transient Array<Entity> static_entities = new Array<Entity>();

	private transient WorldChunk worldChunk;

	public OverworldChunk () { }

	public boolean wasCreated = false;
	public boolean isDirty = false;

	Random r = null;

	private static float SkyHeight = 100;

	public Vector2 areaSize = null;

	// threading!
	private transient ThreadSafeLevelSerializer serializer = new ThreadSafeLevelSerializer();
	private transient OverworldGenerationThread generationRunnable = new OverworldGenerationThread();
	private transient TesselationThreadRunnable tesselationRunnable = new TesselationThreadRunnable();

	public OverworldChunk(int width, int height) {
		this.width = width;
		this.height = height;
		makeTiles();
	}

	public void makeTiles() {
		tiles = new Tile[width * height];
		for(int x = 0; x < width; x++) {
			for(int y = 0; y < width; y++) {
				Tile t = new Tile();
				tiles[x + y * width] = t;
			}
		}
	}

	public void generateOnThread(OverworldLevel level) {
		generationRunnable.init(this, level, true);
		Game.threadPool.submit(generationRunnable);
	}

	public void generateOnThisThread(OverworldLevel level) {
		generationRunnable.init(this, level, false);
		generationRunnable.run();
	}

	public void Init(OverworldLevel level) {
		needsInitializing = false;
		long seed = xChunk * 123456 + yChunk * 352525;
		seed += level.seed / 2;
		r = new Random(seed);

		// Overworld chunks keep track of their own renderer chunks
		if(worldChunk == null) {
			worldChunk = new WorldChunk(GameManager.renderer);
			worldChunk.overworldChunk = this;
		}

		worldChunk.setSize(width, height);
		worldChunk.setOffset(xChunk * width, yChunk * height);
		worldChunk.setOverworldChunk(this);

		if(tiles == null) makeTiles();

		if(tileToLoad == null && level.areasOfInterest.size > 0) {
			boolean shouldMakeArea = r.nextFloat() <= level.randomInterestChance;
			if(shouldMakeArea) {
				tileToLoad = level.areasOfInterest.get(r.nextInt(level.areasOfInterest.size));
			}
		}

		for(int x = 0; x < width; x++) {
			for(int y = 0; y < width; y++) {
				Tile t = tiles[x + y * width];
				t.ceilTex = 36;
				t.floorTex = 27;
				t.wallTex = 4;
				t.renderSolid = false;
				t.blockMotion = false;
				t.ceilHeight = SkyHeight;
				t.floorHeight = -0.5f;
				t.floorTex = 27;
			}
		}

		entities.clear();
		static_entities.clear();
		non_collidable_entities.clear();

		refreshLandscape(level);

		if(tileToLoad != null) {
			Level openLevel = serializer.loadLevel(Game.findInternalFileInMods(tileToLoad));

			if(openLevel != null) {

				areaSize = new Vector2(openLevel.width, openLevel.height);

				int xOffset = (int) ((width - openLevel.width) * 0.5f);
				int yOffset = (int) ((height - openLevel.height) * 0.5f);
				float zOffset = -0;

				if (xOffset != 0 || yOffset != 0) {
					zOffset = getTile(width / 2, height / 2).getMaxFloorHeight();
				}

				int rotTimes = r.nextInt(2);
				for (int i = 0; i < rotTimes; i++) {
					openLevel.rotate90();
				}

				pasteTiles(openLevel, xOffset, yOffset, zOffset);
				flattenForInterest(openLevel.width, openLevel.height, xOffset, yOffset, zOffset, level);

				// make entities for this level!
				if (!wasCreated) {
					level.spawnEntitiesFromMarkers(openLevel.editorMarkers, xChunk * width + xOffset, yChunk * height + yOffset);
				}

				SpawnEntities(openLevel.entities, xChunk * width + xOffset, yChunk * height + yOffset, zOffset, level);
				SpawnEntities(openLevel.non_collidable_entities, xChunk * width + xOffset, yChunk * height + yOffset, zOffset,  level);
				SpawnEntities(openLevel.static_entities, xChunk * width + xOffset, yChunk * height + yOffset, zOffset, level);
			}
		}

		for(int x = 0; x < width; x++) {
			for(int y = 0; y < width; y++) {
				Tile t = tiles[x + y * width];
				if(t != null) {
					t.ceilTex = 36;
					t.ceilHeight = SkyHeight;
					t.init(Level.Source.LEVEL_START);
				}
			}
		}

		decorateChunk(level, level.genTheme);
	}

	public void refreshLandscape(OverworldLevel level) {
		int xOffset = xChunk * width;
		int yOffset = yChunk * height;

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < width; y++) {
				Tile t = tiles[x + y * width];
				level.fillTileFromLandscape(x + xOffset, y + yOffset, t);
			}
		}
	}

	Vector3 t1 = new Vector3();
	public void decorateChunk(Level level, GenTheme genTheme) {
		// first pass - add geninfo objects, paint tile textures
		for(int x = 0; x < width; x++) {
			for(int y = 0; y < height; y++) {

				Tile cur = getTileOrNull(x,y);

				if(cur == null) continue;

				// now pick tile textures
				if(level.wallPainter != null) {
					String k = Integer.toString(cur.wallTex);
					if(level.wallPainter.containsKey(k)) {
						Array<Float> list = level.wallPainter.get(k);
						float f = list.get(r.nextInt(list.size));
						cur.wallTex = (byte)f;
					}

					k = Integer.toString(cur.wallBottomTex != null ? cur.wallBottomTex : cur.wallTex);
					if(level.wallPainter.containsKey(k)) {
						Array<Float> list = level.wallPainter.get(k);
						float f = list.get(r.nextInt(list.size));
						cur.wallBottomTex = (byte)f;
					}
				}
				else {
					if(genTheme != null) {
						cur.eastTex = genTheme.getWallTexture(cur.eastTex != null ? cur.eastTex : cur.wallTex, cur.eastTexAtlas != null ? cur.eastTexAtlas : cur.wallTexAtlas);
						cur.westTex = genTheme.getWallTexture(cur.westTex != null ? cur.westTex : cur.wallTex, cur.westTexAtlas != null ? cur.westTexAtlas : cur.wallTexAtlas);
						cur.northTex = genTheme.getWallTexture(cur.northTex != null ? cur.northTex : cur.wallTex, cur.northTexAtlas != null ? cur.northTexAtlas : cur.wallTexAtlas);
						cur.southTex = genTheme.getWallTexture(cur.southTex != null ? cur.southTex : cur.wallTex, cur.southTexAtlas != null ? cur.southTexAtlas : cur.wallTexAtlas);
						cur.bottomEastTex = genTheme.getWallTexture(cur.bottomEastTex != null ? cur.bottomEastTex : cur.wallBottomTex != null ? cur.wallBottomTex : cur.wallTex, cur.bottomEastTexAtlas != null ? cur.bottomEastTexAtlas != null ? cur.bottomEastTexAtlas : cur.wallBottomTexAtlas : cur.wallTexAtlas);
						cur.bottomWestTex = genTheme.getWallTexture(cur.bottomWestTex != null ? cur.bottomWestTex : cur.wallBottomTex != null ? cur.wallBottomTex : cur.wallTex, cur.bottomWestTexAtlas != null ? cur.bottomWestTexAtlas != null ? cur.bottomWestTexAtlas : cur.wallBottomTexAtlas : cur.wallTexAtlas);
						cur.bottomNorthTex = genTheme.getWallTexture(cur.bottomNorthTex != null ? cur.bottomNorthTex : cur.wallBottomTex != null ? cur.wallBottomTex : cur.wallTex, cur.bottomNorthTexAtlas != null ? cur.bottomNorthTexAtlas != null ? cur.bottomNorthTexAtlas : cur.wallBottomTexAtlas : cur.wallTexAtlas);
						cur.bottomSouthTex = genTheme.getWallTexture(cur.bottomSouthTex != null ? cur.bottomSouthTex : cur.wallBottomTex != null ? cur.wallBottomTex : cur.wallTex, cur.bottomSouthTexAtlas != null ? cur.bottomSouthTexAtlas != null ? cur.bottomSouthTexAtlas : cur.wallBottomTexAtlas : cur.wallTexAtlas);
						cur.wallBottomTex = genTheme.getWallTexture(cur.wallBottomTex != null ? cur.wallBottomTex : cur.wallTex, cur.westTexAtlas != null ? cur.westTexAtlas : cur.wallTexAtlas);
						cur.wallTex = genTheme.getWallTexture(cur.wallTex, cur.wallTexAtlas);
					}
				}

				if(level.floorPainter != null) {
					String k = Integer.toString(cur.floorTex);
					if(level.floorPainter.containsKey(k)) {
						Array<Float> list = level.floorPainter.get(k);
						float f = list.get(r.nextInt(list.size));
						cur.floorTex = (byte)f;
					}
				}
				else {
					if(genTheme != null)
						cur.floorTex = genTheme.getFloorTexture(cur.floorTex, cur.floorTexAtlas);
				}

				if(level.ceilPainter != null) {
					String k = Integer.toString(cur.ceilTex);
					if(level.ceilPainter.containsKey(k)) {
						Array<Float> list = level.ceilPainter.get(k);
						float f = list.get(r.nextInt(list.size));
						cur.ceilTex = (byte)f;
					}
				}
				else {
					if(genTheme != null)
						cur.ceilTex = genTheme.getCeilingTexture(cur.ceilTex, cur.ceilTexAtlas);
				}

				// modify how much we generate based on the graphics detail level
				float graphicsQualitySpawnMod = (1 + Options.instance.graphicsDetailLevel) / 2f;
				graphicsQualitySpawnMod *= (graphicsQualitySpawnMod * 0.5f);

				// the gen info list spawns entities on tiles with matching textures
				if(genTheme != null && genTheme.genInfos != null && cur.CanSpawnHere()) {

					for(GenInfo info : genTheme.genInfos) {

						boolean wallmatch = info.wallTex == null || (cur.wallTex == info.wallTex && Level.checkIfTextureAtlasesMatch(cur.wallTexAtlas, info.textureAtlas));
						boolean ceilmatch = info.ceilTex == null || (cur.ceilTex == info.ceilTex && Level.checkIfTextureAtlasesMatch(cur.ceilTexAtlas, info.textureAtlas));
						boolean floormatch = info.floorTex == null || (cur.floorTex == info.floorTex && Level.checkIfTextureAtlasesMatch(cur.floorTexAtlas, info.textureAtlas));

						if( wallmatch && ceilmatch && floormatch && (info.chance >= 1 || (r.nextFloat() <= info.chance * graphicsQualitySpawnMod)) ) {

							// add any marker
							if(info.marker != null && info.marker != Markers.none) {
								level.editorMarkers.add( new EditorMarker(info.marker, x,y) );
							}

							// copy and place the entity, if one was given
							if(info.spawns != null) {
								try {
									int num = 1;
									if(info.clusterCount > 1) num = r.nextInt(info.clusterCount) + 1;
									if(info.performanceControlledClustering) num *= Options.instance.gfxQuality;
									for(int i = 0; i < num; i++) {
										Array<Entity> copyList = (Array<Entity>) KryoSerializer.copyObject(info.spawns);

										for(Entity copy : copyList) {
											copy.x = x + 0.5f;
											copy.y = y + 0.5f;

											if(info.clusterSpread != 0) {
												copy.x += (r.nextFloat() * (info.clusterSpread * 2f)) - info.clusterSpread;
												copy.y += (r.nextFloat() * (info.clusterSpread * 2f)) - info.clusterSpread;
											}

											copy.z = cur.getFloorHeightThreadSafe(copy.x, copy.y, t1) + 0.5f;

											if(info.attachCeiling) {
												if(copy instanceof Sprite)
													copy.z = cur.getCeilHeight(copy.x, copy.y) - 0.5f;
												else
													copy.z = (cur.getCeilHeight(copy.x, copy.y) + 0.5f) - copy.collision.z;
											}
											if(info.attachWall) level.decorateWallWith(copy, false, true);

											if(!copy.isDynamic) {
												static_entities.add(copy);
											} else if(!copy.isSolid) {
												non_collidable_entities.add(copy);
											}
											else {
												entities.add(copy);
											}
										}
									}
								}
								catch (Exception ex) {
									//whoah?!
								}
							}
						}
					}
				}
			}
		}
	}

	public void doLighting(Level level) {
		for(int x = 0; x < width; x++) {
			for(int y = 0; y < width; y++) {
				Tile t = getTileOrNull(x,y);
				if(t == null) continue;
			}
		}

		for(int i = 0; i < non_collidable_entities.size; i++)
		{
			if(non_collidable_entities.get(i) instanceof Light && non_collidable_entities.get(i).isActive)
			{
				Light t = (Light)non_collidable_entities.get(i);
				level.lightSpatialHash.AddLight(t);
			}
		}

		// light some entities
		for(Entity e : entities) {
			e.updateLight(level);
		}
		for(Entity e : non_collidable_entities) {
			e.updateLight(level);
		}
		for(Entity e : static_entities) {
			e.updateLight(level);
		}
	}

	private Tile skirtTile = new Tile();
	public Tile getSkirtTile(int x, int y) {

		skirtTile.ceilTex = 36;
		skirtTile.floorTex = 27;
		skirtTile.wallTex = 4;
		skirtTile.renderSolid = false;
		skirtTile.blockMotion = false;
		skirtTile.ceilHeight = SkyHeight;
		skirtTile.floorTex = 27;

		OverworldLevel level = (OverworldLevel)Game.instance.level;
		level.fillTileFromLandscape(x + xChunk * width, y + yChunk * height, skirtTile);

		return skirtTile;
	}

	public Tile getTile(int x, int y)
	{
		if(x < 0 || x >= width || y < 0 || y >= height){
			return getSkirtTile(x, y);
		}
		if(tiles == null || tiles[x + y * width] == null) {
			return getSkirtTile(x, y);
		}
		return tiles[x + y * width];
	}

	public void setTile(int x, int y, Tile t) {
		if(x >= 0 && x < width && y >= 0 && y < height) {
			tiles[x + y * width] = t;
		}
	}

	public Tile getTileOrNull(int x, int y)
	{
		if(x < 0 || x >= width || y < 0 || y >= height)
			return getSkirtTile(x, y);

		if(tiles == null || tiles[x + y * width] == null)
			return getSkirtTile(x, y);

		return tiles[x + y * width];
	}

	public void runGenInfo(Level level) {
		Tile.solidWall.wallTex = 36;
		Tile.solidWall.ceilTex = 36;
		Tile.solidWall.floorTex = 1;

		int xOffset = xChunk * width;
		int yOffset = yChunk * height;

		GenTheme genTheme = DungeonGenerator.GetGenData("OUTDOOR");

		// first pass - add geninfo objects, paint tile textures
		for(int x = 0; x < width; x++) {
			for(int y = 0; y < height; y++) {

				Tile cur = getTileOrNull(x,y);
				if(cur == null) continue;

				// the gen info list spawns entities on tiles with matching textures
				if(genTheme.genInfos != null && !cur.IsSolid()) {

					for(int gi = 0; gi < genTheme.genInfos.size; gi++) {
						GenInfo info = genTheme.genInfos.get(gi);

						boolean wallmatch = info.wallTex == null || cur.wallTex == info.wallTex;
						boolean ceilmatch = info.ceilTex == null || cur.ceilTex == info.ceilTex;
						boolean floormatch = info.floorTex == null || cur.floorTex == info.floorTex;

						if( wallmatch && ceilmatch && floormatch && (info.chance >= 1 || r.nextFloat() <= info.chance) ) {

							// add any marker
							if(info.marker != null && info.marker != Markers.none) {
								level.editorMarkers.add( new EditorMarker(info.marker, x + xOffset, y + yOffset) );
							}

							// copy and place the entity, if one was given
							if(info.spawns != null) {
								try {
									int num = 1;
									if(info.clusterCount > 1) num = r.nextInt(info.clusterCount) + 1;
									if(info.performanceControlledClustering) num *= Options.instance.gfxQuality;
									for(int i = 0; i < num; i++) {
										String s = JsonUtil.toJson(info.spawns);
										Array<Entity> copyList = JsonUtil.fromJson(info.spawns.getClass(), s);

										for(Entity copy : copyList) {
											copy.x = xOffset + x + 0.5f;
											copy.y = yOffset + y + 0.5f;

											if(info.clusterSpread != 0) {
												copy.x += (r.nextFloat() * (info.clusterSpread * 2f)) - info.clusterSpread;
												copy.y += (r.nextFloat() * (info.clusterSpread * 2f)) - info.clusterSpread;
											}

											copy.z = cur.getFloorHeightThreadSafe((float)copy.x - xOffset, (float)copy.y - yOffset, t1) + 0.5f;

											if(info.attachCeiling) {
												if(copy instanceof Sprite)
													copy.z = cur.ceilHeight - 0.5f;
												else
													copy.z = (cur.ceilHeight + 0.5f) - copy.collision.z;
											}
											if(info.attachWall) level.decorateWallWith(copy, false, false);

											if(copy instanceof Sprite || copy instanceof Model)
												static_entities.add(copy);
											else if(!copy.isSolid)
												non_collidable_entities.add(copy);
											else
												entities.add(copy);
										}
									}
								}
								catch (Exception ex) {
									//whoah?!
								}
							}
						}
					}
				}

				// now pick tile textures
				if(level.wallPainter != null) {
					String k = Integer.toString(cur.wallTex);
					if(level.wallPainter.containsKey(k)) {
						Array<Float> list = level.wallPainter.get(k);
						float f = list.get(r.nextInt(list.size));
						cur.wallTex = (byte)f;
					}
				}

				if(level.floorPainter != null) {
					String k = Integer.toString(cur.floorTex);
					if(level.floorPainter.containsKey(k)) {
						Array<Float> list = level.floorPainter.get(k);
						float f = list.get(r.nextInt(list.size));
						cur.floorTex = (byte)f;
					}
				}

				if(level.ceilPainter != null) {
					String k = Integer.toString(cur.ceilTex);
					if(level.ceilPainter.containsKey(k)) {
						Array<Float> list = level.ceilPainter.get(k);
						float f = list.get(r.nextInt(list.size));
						cur.ceilTex = (byte)f;
					}
				}
			}
		}
	}

	private void flattenForInterest(int tileWidth, int tileHeight, int xOffset, int yOffset, float zOffset, OverworldLevel level) {
		for(int x = 0; x < width; x++) {
			for(int y = 0; y < height; y++) {

				if(x >= xOffset && x < xOffset + tileWidth && y >= yOffset && y < yOffset + tileHeight)
					continue;

				Tile c = getTile(x, y);
				c.slopeNW = flattenForInterest(x + 1, y, tileWidth, tileHeight, xOffset, yOffset, c.slopeNW, zOffset);
				c.slopeNE = flattenForInterest(x, y, tileWidth, tileHeight, xOffset, yOffset, c.slopeNE, zOffset);
				c.slopeSW = flattenForInterest(x + 1, y + 1, tileWidth, tileHeight, xOffset, yOffset, c.slopeSW, zOffset);
				c.slopeSE = flattenForInterest(x, y + 1, tileWidth, tileHeight, xOffset, yOffset, c.slopeSE, zOffset);

				level.clampTileSlope(c);
			}
		}
	}

	Vector2 checkLoc = new Vector2();
	private float flattenForInterest(int x, int y, int tileWidth, int tileHeight, int xOffset, int yOffset, float startHeight, float zOffset) {
		checkLoc.set(x, y);
		if(x < xOffset) checkLoc.x = xOffset;
		if(x > xOffset + tileWidth) checkLoc.x = xOffset + tileWidth;
		if(y < yOffset) checkLoc.y = yOffset;
		if(y > yOffset + tileHeight) checkLoc.y = yOffset + tileHeight;

		float len = checkLoc.sub(x, y).len();
		len = Math.min(len, xOffset);
		len /= xOffset;
		len = 1 - len;

		return Interpolation.exp5.apply(startHeight, zOffset, len);
	}

	public void spawnMonster(Level level)
	{
		Player player = Game.instance.player;

		if(Game.GetMonsterManager() == null) return;

		Random r = new Random();
		int xPos = r.nextInt(width);
		int yPos = r.nextInt(height);

		Tile t = getTileOrNull(xPos,yPos);
		if(t != null && t.IsFree() && !t.data.isWater) {

			if(level.checkEntityCollision(xPos + 0.5f + xChunk * width, yPos + 0.5f + yChunk * height, 0, 0.5f) == null)
			{
				Monster m = null;
				m = Game.GetMonsterManager().GetRandomMonster("DUNGEON");

				if(m != null)
				{
					m.x = xPos + 0.5f + xChunk * width;
					m.y = yPos + 0.5f + yChunk * height;
					m.z = t.getFloorHeightThreadSafe(0.5f, 0.5f, t1) + 0.5f;
					m.Init(level, player.level);
					level.entities.add(m);
				}
			}
		}
	}

	public void init(OverworldLevel level) {
		Level.Source source = !wasCreated ? Level.Source.LEVEL_START : Level.Source.LEVEL_LOAD;
		for (int i = 0; i < non_collidable_entities.size; i++) {
			non_collidable_entities.get(i).init(level, source);
		}
		for (int i = 0; i < entities.size; i++) {
			entities.get(i).init(level, source);
		}
		for (int i = 0; i < static_entities.size; i++) {
			static_entities.get(i).init(level, source);
		}

		wasCreated = true;
	}

	public void decorate(OverworldLevel level) {
		for(int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {

				// Don't plant on generated locations!
				if(areaSize != null) {
					int xOffset = (int)((width - areaSize.x) * 0.5f);
					int yOffset = (int)((height - areaSize.y) * 0.5f);

					if(x >= xOffset && y >= yOffset && x < xOffset + areaSize.x && y < yOffset + areaSize.y)
						continue;
				}

				Tile t = getTileOrNull(x, y);
				if (t == null) continue;

				// raise some rock pillars out of the ground
				if(x != 0 && y != 0 && x != width - 1 && y != height - 1) {
					float f = r.nextFloat();
					if (r.nextFloat() > 0.8f) {
						if (r.nextBoolean()) {
							t.floorHeight += 0.2f;
						} else if (r.nextBoolean()) {
							t.floorHeight += 0.4f;
						} else if (r.nextBoolean()) {
							t.floorHeight += 0.6f;
						} else if (r.nextBoolean()) {
							t.floorHeight += 2;
						}
					}
				}

				// see if we can plant something here
				float terrainHeight = t.getMinFloorHeight() + 0.5f;
				Entity planted = level.getFoliageFor(x + xChunk * width, y + yChunk * height, terrainHeight, r, serializer);

				if(planted != null) {
					planted.rotate(0, 0, r.nextFloat() * 360f);
					SpawnEntity(planted, level);
				}
			}
		}
	}

	public void SpawnEntity(Entity entity, Level level) {
		if(entity == null) return;

		if(entity instanceof Prefab) {
			Entity newEntity = EntityManager.instance.getEntity(((Prefab) entity).category, ((Prefab) entity).name);
			newEntity.x = entity.x;
			newEntity.y = entity.y;
			newEntity.z = entity.z;
			SpawnEntity(newEntity, level);
		}
		else if(entity instanceof Group) {
			Array<Entity> entities = ((Group) entity).entities;
			for(int i = 0; i < entities.size; i++) {
				Entity e = entities.get(i);
				e.x += entity.x;
				e.y += entity.y;
				e.z += entity.z;
				SpawnEntity(e, level);
			}
		}
		else {
			if (!entity.isSolid)
				non_collidable_entities.add(entity);
			if (entity.isDynamic)
				entities.add(entity);
			else
				static_entities.add(entity);
		}
	}

	private void SpawnEntities(Array<Entity> entities, int xOffset, int yOffset, float zOffset, OverworldLevel level) {
		for(int i = 0; i < entities.size; i++) {
			Entity e = entities.get(i);
			e.x += xOffset;
			e.y += yOffset;
			e.z += zOffset;
			SpawnEntity(e, level);
		}
	}

	public void pasteTiles(Level clip, int offsetx, int offsety, float zOffset) {
		for(int x = 0; x < clip.width; x++) {
			for(int y = 0; y < clip.height; y++) {
				setTile(x + offsetx,y + offsety, clip.getTileOrNull(x, y));
				getTile(x + offsetx,y + offsety).floorHeight += zOffset;
			}
		}
	}

	public void setWorldChunk(WorldChunk worldChunk) {
		this.worldChunk = worldChunk;
		this.worldChunk.overworldChunk = this;
	}

	public WorldChunk getWorldChunk() {
		return worldChunk;
	}

	@Override
	public void reset() {
		needsInitializing = true;
	}

	public void dispose() {
		if(worldChunk != null) {

			entities.clear();
			static_entities.clear();
			non_collidable_entities.clear();

			GameManager.renderer.chunks.removeValue(worldChunk, true);

			if(worldChunk.staticMeshBatch != null) {
				for (int i = 0; i < worldChunk.staticMeshBatch.size; i++) {
					Array<Mesh> meshes = worldChunk.staticMeshBatch.getValueAt(i);
					GlRenderer.staticMeshPool.freeMeshes(meshes);
				}
				worldChunk.staticMeshBatch.clear();
			}
		}
	}

	public void TesselateOnThread(final OverworldLevel level, GlRenderer glRenderer) {
		tesselationRunnable.init(this, level, glRenderer);
		Game.threadPool.submit(tesselationRunnable);
	}

	public void TesselateOnThisThread(final OverworldLevel level, GlRenderer glRenderer) {
		tesselationRunnable.init(this, level, glRenderer);
		tesselationRunnable.run();
	}

	public boolean entityIsInChunk(Player p) {
		int startX = xChunk * width;
		int startY = yChunk * width;
		int endX = startX + width;
		int endY = startY + width;
		return p.x >= startX && p.x < endX && p.y >= startY && p.y < endY;
	}
}
