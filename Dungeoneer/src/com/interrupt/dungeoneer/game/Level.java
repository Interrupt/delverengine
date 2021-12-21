package com.interrupt.dungeoneer.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Array;
import com.interrupt.dungeoneer.Audio;
import com.interrupt.dungeoneer.GameManager;
import com.interrupt.dungeoneer.collision.Collision;
import com.interrupt.dungeoneer.collision.Collision.CollisionType;
import com.interrupt.dungeoneer.editor.EditorMarker;
import com.interrupt.dungeoneer.entities.*;
import com.interrupt.dungeoneer.entities.Door.DoorState;
import com.interrupt.dungeoneer.entities.Entity.CollidesWith;
import com.interrupt.dungeoneer.entities.Entity.EntityType;
import com.interrupt.dungeoneer.entities.Stairs.StairDirection;
import com.interrupt.dungeoneer.entities.items.Gold;
import com.interrupt.dungeoneer.entities.items.Key;
import com.interrupt.dungeoneer.entities.items.QuestItem;
import com.interrupt.dungeoneer.entities.triggers.ButtonDecal;
import com.interrupt.dungeoneer.entities.triggers.Trigger;
import com.interrupt.dungeoneer.generator.DungeonGenerator;
import com.interrupt.dungeoneer.generator.GenInfo;
import com.interrupt.dungeoneer.generator.GenInfo.Markers;
import com.interrupt.dungeoneer.generator.GenTheme;
import com.interrupt.dungeoneer.gfx.GlRenderer;
import com.interrupt.dungeoneer.gfx.TextureAtlas;
import com.interrupt.dungeoneer.gfx.drawables.DrawableMesh;
import com.interrupt.dungeoneer.partitioning.LightSpatialHash;
import com.interrupt.dungeoneer.partitioning.SpatialHash;
import com.interrupt.dungeoneer.serializers.KryoSerializer;
import com.interrupt.dungeoneer.tiles.Tile;
import com.interrupt.dungeoneer.tiles.Tile.TileSpaceType;
import com.interrupt.dungeoneer.tiles.TileMaterials;
import com.interrupt.helpers.TileEdges;
import com.interrupt.managers.EntityManager;
import com.interrupt.managers.TileManager;
import com.interrupt.utils.JsonUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Random;

public class Level {

    public enum DungeonTheme {
    	TEST,
		DUNGEON,
		CAVE,
		HIDEOUT,
		GOBLINS,
		DEMONS,
		UNDEAD
	}

	public enum Source { LEVEL_START, LEVEL_LOAD, EDITOR, SPAWNED }
	private enum Direction { NORTH, SOUTH, EAST, WEST }

	public int width, height;
	public Tile[] tiles;
	public TileMaterials[] tileMaterials;

	public Array<Entity> entities;
	public Array<Entity> non_collidable_entities;
	public Array<Entity> static_entities = new Array<Entity>();

	/** Name of level. */
	public String levelName;

    public String levelId = null;

	protected transient Array<Entity> toDelete = new Array<Entity>();

	private transient short[] smellMap = null;

	public Integer playerStartX, playerStartY, playerStartRot = null;

	public Stairs up;
	public Stairs down;

	/** Should generator make stairs down. */
	public boolean makeStairsDown = true;

	/** Unused. */
	public float darkness = 1;

	/** Starting distance of fog. */
	public float fogStart;

	/** Ending distance of fog. */
	public float fogEnd;

    private static final Color defaultFogColor = new Color(0, 0, 0, 1);

	/** Color of fog. */
	public Color fogColor = new Color(defaultFogColor);

    private static final Color defaultAmbientColor = new Color(0, 0, 0, 1);

    /** Color of ambient light. */
    public Color ambientColor = new Color(defaultAmbientColor);

    private static final float defaultViewDistance = 15f;

	/** Camera far draw distance. */
	public float viewDistance = defaultViewDistance;

    private static final Color defaultSkyLightColor = new Color(0.5f,0.5f,0.5f,0);

	/** Color from skybox. */
	public Color skyLightColor = new Color(defaultSkyLightColor);

    private static final Color defaultShadowColor = new Color(0.5f, 0.4f, 0.85f, 1f);

	/** Color of shadows. */
	public Color shadowColor = new Color(defaultShadowColor);

	public boolean isLoaded = false;
	public boolean needsSaving = true;

	/** Depth where level is placed. */
	public int dungeonLevel;

	/** Theme to apply to level. */
	public String theme;

	/** Comma separated list of mp3 filepaths. */
	public String music;

	/** Comma separated list of mp3 filepaths. */
	public String actionMusic;

	/** Play music on a loop. */
	public Boolean loopMusic = true;

	/** Ambient sound filepath. */
	public String ambientSound = null;

    private static final float defaultAmbientSoundVolume = 0.5f;

	/** Ambient sound volume. */
	public Float ambientSoundVolume = defaultAmbientSoundVolume;

	/** Array of additional themes to pull monsters from. */
	public Array<String> alternateMonsterThemes = null;

	/** Skybox mesh. */
	public DrawableMesh skybox = null;

	/** Filepath to level file. Used for non-generated levels. */
	public String levelFileName;

	public String levelHeightFile;

	/** Is the level procedurally generated from room pieces. */
	public boolean generated = false;

	/** Name of room generator type to use. */
    public String roomGeneratorType = null;

    /** Chance any given room is procedurally generated. */
    public float roomGeneratorChance = 0.4f;

	/** Are monsters spawned on this level. */
	public boolean spawnMonsters = true;

	/** Table of spawn rates. */
	public SpawnRate spawnRates = null;

	/** Array of trap prefab names. */
	public String[] traps = {"ProximitySpikes"};

	private float monsterSpawnTimer = 0;

	public transient boolean mapIsDirty = true;

	/** Default wall texture index. */
	protected int defaultWallTex = 0;

	protected int defaultWallAccentTex = 11;

	/** Default ceiling texture index. */
	protected int defaultCeilTex = 1;

	/** Default floor texture index. */
	protected int defaultFloorTex = 2;

	protected int[] wallTextures = null;
	protected int[] wallAccentTextures = null;
	protected int[] ceilTextures = null;
	protected int[] floorTextures = null;

	/** Wall TexturePainter */
	protected HashMap<String, Array<Float>> wallPainter = null;

	/** Floor TexturePainter */
	protected HashMap<String, Array<Float>> floorPainter = null;

	/** Ceiling TexturePainter */
	protected HashMap<String, Array<Float>> ceilPainter = null;

	public Array<EditorMarker> editorMarkers = new Array<EditorMarker>();

	public transient boolean rendererDirty = true;

	public transient boolean isDirty = false;
	public transient Array<Vector2> dirtyMapTiles = new Array<Vector2>();

	public transient SpatialHash spatialhash = new SpatialHash(2);
	public transient SpatialHash staticSpatialhash = new SpatialHash(2);
	public transient LightSpatialHash lightSpatialHash = new LightSpatialHash(1);

	public transient GenTheme genTheme = null;

	public transient int lastPlayerTileX = 0;
	public transient int lastPlayerTileY = 0;

	private int entity_index = 0;

	private transient Array<Entity> triggerCache = new Array<Entity>();
	private transient Array<Entity> collisionCache = new Array<Entity>();
	private transient Ray calcRay = new Ray(new Vector3(), new Vector3());

	public String objectivePrefab = null;

	/** Loading screen background image filepath. */
	public String loadingScreenBackground = null;

	public boolean spawnEncounterDuringChase = true;

	public HashMap<String, String> textureAtlasOverrides = null;
	public HashMap<String, String> spriteAtlasOverrides = null;

	public Level()
	{
	}

	public Level(int width, int height) {
		// make a blank level for the editor
		this.width = width;
		this.height = height;

		tiles = new Tile[width * height];
		tileMaterials = new TileMaterials[width * height];

		entities = new Array<Entity>();
		non_collidable_entities = new Array<Entity>();
		static_entities = new Array<Entity>();

		isLoaded=true;
		init(Source.LEVEL_START);
	}

	public Level(int dungeonLevel, DungeonTheme theme, String levelFileName, String levelHeightFile, float darkness, Game game, float fogStart, float fogEnd) {
		this.levelName = levelFileName;
		this.darkness = darkness;
		this.fogStart = fogStart;
		this.fogEnd = fogEnd;
		this.dungeonLevel = dungeonLevel;
		this.theme = theme.toString();
		this.levelFileName = levelFileName;
		this.levelHeightFile = levelHeightFile;
		isLoaded = false;
		needsSaving = true;
	}

	public Level(int dungeonLevel, DungeonTheme theme, float darkness, Game game, float fogStart, float fogEnd) {
		this.levelName = "GEN";
		this.darkness = darkness;
		this.fogStart = fogStart;
		this.fogEnd = fogEnd;
		this.dungeonLevel = dungeonLevel;
		this.theme = theme.toString();
		this.generated = true;
		isLoaded = false;
		needsSaving = true;
	}

	public void loadForEditor(String levelFileName, String levelHeightFile) {
		this.levelName = "EDITED";
		this.levelFileName = levelFileName;
		this.levelHeightFile = levelHeightFile;
		this.dungeonLevel = 1;
		this.theme = DungeonTheme.TEST.toString();
		isLoaded = false;

		load(Source.EDITOR);
		entities.clear();
		non_collidable_entities.clear();
		static_entities.clear();
	}

	public void loadForSplash(String levelFileName) {
		this.levelName = "EDITED";
		this.levelFileName = levelFileName;
		this.levelHeightFile = null;
		this.dungeonLevel = 1;
		this.theme = DungeonTheme.TEST.toString();
		isLoaded = false;
		load(Source.EDITOR);
	}

	public void loadFromEditor() {
		needsSaving = false;
		spawnMonsters = false;

		Array<Entity> copyEntities = new Array<>(100);
		Array<Entity> copyNonCollidableEntities = new Array<>(100);
		Array<Entity> copyStaticEntities = new Array<>(100);

		for(int i = 0; i < entities.size; i++) {
			Entity copy = entities.get(i);
			if (!copy.checkDetailLevel() || (copy.spawnChance < 1f && Game.rand.nextFloat() > copy.spawnChance))
				continue;

			if(!copy.isDynamic)
				copyStaticEntities.add(copy);
			else if(!copy.isSolid)
				copyNonCollidableEntities.add(copy);
			else
				copyEntities.add(copy);
		}

		entities = copyEntities;
		non_collidable_entities = copyNonCollidableEntities;
		static_entities = copyStaticEntities;

		genTheme = DungeonGenerator.GetGenData(theme);

		loadSurprises(genTheme);

		initPrefabs(Source.LEVEL_START);

		addEntitiesFromMarkers(editorMarkers, new Array<>(), new Boolean[width * height], new Array<>(), genTheme, 0, 0);
		decorateLevel();

		init(Source.LEVEL_START);

		editorMarkers.clear();

		updateLights(Source.LEVEL_START);
		updateStaticSpatialHash();
	}

	public void generate(Source source) {
		Random levelRand = new Random();

		entities = new Array<>();
		non_collidable_entities = new Array<>();
		static_entities = new Array<>();

		// Generate level
		Boolean isValid = false;

		while(isValid == false) {
			Gdx.app.log("DelverGenerator", "Making level");

			DungeonGenerator generator;
			Level generatedLevel = null;

			Progression progression = null;
			if(Game.instance != null) {
				progression = Game.instance.progression;
			}

			if(progression == null) {
				progression = new Progression();
			}

			// Try to generate a level
			try {
				generator = new DungeonGenerator(new Random(), dungeonLevel);
				generatedLevel = generator.MakeDungeon(theme, roomGeneratorType, roomGeneratorChance, progression);
				isValid = checkIsValidLevel(generatedLevel, dungeonLevel);
			}
			catch(Exception ex) {
				Gdx.app.error("DelverGenerator", ex.getMessage());
			}

			// Did we make a valid level? Try again if not.
			if (!isValid || generatedLevel == null) {
				Gdx.app.log("DelverGenerator", "Bad level. Trying again");
				continue;
			}

			progression.markDungeonAreaAsSeen(theme);

			// use data from the generated level
			width = generatedLevel.width;
			height = generatedLevel.height;
			tiles = generatedLevel.tiles;
			tileMaterials = generatedLevel.tileMaterials;

			editorMarkers = generatedLevel.editorMarkers;
			genTheme = generatedLevel.genTheme;

			entities = generatedLevel.entities;
		}

		// when generating, keep track of where the possible stair locations are
		Array<Vector2> stairLocations = new Array<Vector2>();

		Tile.solidWall.wallTex = (byte) defaultWallTex;
		Tile.solidWall.wallBottomTex = (byte) defaultWallTex;
		Tile.solidWall.ceilTex = (byte) defaultCeilTex;
		Tile.solidWall.floorTex = (byte) defaultFloorTex;

		// mark some locations as trap-free
		Array<Vector2> trapAvoidLocs = new Array<Vector2>();

		initPrefabs(Source.EDITOR);

		// keep a list of places to avoid when making traps
		Boolean canMakeTrap[] = new Boolean[width * height];
		for(int i = 0; i < width * height; i++) canMakeTrap[i] = true;

		if(stairLocations != null && stairLocations.size > 0) {
			trapAvoidLocs.addAll(stairLocations);
		}

		// place stairs if needed, need to know their locations before generating entities
		if(stairLocations.size > 0 && generated && source != Source.EDITOR) {
			if(makeStairsDown) {
				// if this is the first floor, only add the stairs down. Otherwise make up and down
				Vector2 downLoc = stairLocations.get(levelRand.nextInt(stairLocations.size));
				stairLocations.removeValue(downLoc, true);

				// stairs down
				Tile downTile = getTile((int) downLoc.x, (int) downLoc.y);
			}
		}

		for(int x = 0; x < width; x++) {
			for(int y = 0; y < width; y++) {
				Tile c = getTileOrNull(x,y);

				// make traps on empty and flat areas
				if(c != null && canMakeTrap[x + y * width] && c.CanSpawnHere() && c.isFlat()) {

					boolean makeTrap = levelRand.nextFloat() <= 0.012f;

					if(makeTrap) {

						boolean canMake = true;
						for(Vector2 avoidLoc : trapAvoidLocs) {
							if( Math.abs(x - avoidLoc.x) < 6 && Math.abs(y - avoidLoc.y) < 6 ) {
								canMake = false;
							}
						}

						if(canMake && traps != null && traps.length > 0) {
							Prefab p = new Prefab("Traps", traps[levelRand.nextInt(traps.length)]);
							p.x = x + 0.5f;
							p.y = y + 0.5f;
							p.z = c.getFloorHeight(x, y) + 0.5f;
							entities.add(p);
							p.init(this, source);
						}
					}
				}
			}
		}
	}

	private void overrideTileTextures(Tile cur) {
		if(cur != null && textureAtlasOverrides != null) {
			cur.eastTexAtlas = getOverrideAtlas(cur.eastTexAtlas, false);
			cur.westTexAtlas = getOverrideAtlas(cur.westTexAtlas, false);
			cur.northTexAtlas = getOverrideAtlas(cur.northTexAtlas, false);
			cur.southTexAtlas = getOverrideAtlas(cur.southTexAtlas, false);
			cur.bottomEastTexAtlas = getOverrideAtlas(cur.bottomEastTexAtlas, false);
			cur.bottomWestTexAtlas = getOverrideAtlas(cur.bottomWestTexAtlas, false);
			cur.bottomNorthTexAtlas = getOverrideAtlas(cur.bottomNorthTexAtlas, false);
			cur.bottomSouthTexAtlas = getOverrideAtlas(cur.bottomSouthTexAtlas, false);
			cur.wallBottomTexAtlas = getOverrideAtlas(cur.wallBottomTexAtlas, true);
			cur.wallTexAtlas = getOverrideAtlas(cur.wallTexAtlas, true);
			cur.floorTexAtlas = getOverrideAtlas(cur.floorTexAtlas, true);
			cur.ceilTexAtlas = getOverrideAtlas(cur.ceilTexAtlas, true);
		}
	}

	private String getOverrideAtlas(String original, boolean overrideNull) {
		if(textureAtlasOverrides == null || textureAtlasOverrides.size() == 0)
			return original;

		// might need to replace the default texture atlas
		if(original == null && overrideNull) {
			String override = textureAtlasOverrides.get("t1");
			if(override != null)
				return override;
		}

		// check if there's an override for this atlas
		String override = textureAtlasOverrides.get(original);
		return override != null ? override : original;
	}

	private void overrideSpriteAtlas(Entity e) {
		if(e != null && spriteAtlasOverrides != null) {
			String original = e.spriteAtlas;
			if(original != null) {
				String override = spriteAtlasOverrides.get(original);
				if(override != null) {
					e.spriteAtlas = override;
					if(e.drawable != null) e.drawable.refresh();
				}
			}
		}
	}

	// Called after a level was unserialized from bytes or a file
	public void postLoad() {
		// Make sure the tile materials array matches the size of the tiles, and is filled
		if(tileMaterials == null || tileMaterials.length != tiles.length) {
			tileMaterials = new TileMaterials[tiles.length];
		}

		for(int i = 0; i < tiles.length; i++) {
			if (tiles[i] == null)
				continue;

			tiles[i].materials = tileMaterials[i];
		}
	}

	public void load() {
		load(Source.LEVEL_START);
	}

	public void load(Source source) {
		needsSaving = true;
		isLoaded = true;

		Random levelRand = new Random();

		entities = new Array<Entity>();
		non_collidable_entities = new Array<Entity>();
		static_entities = new Array<Entity>();

		if(!generated) {
			Level openLevel = null;
			FileHandle levelFileHandle = Game.findInternalFileInMods(levelFileName);

			if(levelFileName.endsWith(".dat") || levelFileName.endsWith(".json")) {
				openLevel = JsonUtil.fromJson(Level.class, levelFileHandle);
			}
			else {
				openLevel = KryoSerializer.loadLevel(levelFileHandle);
			}

			width = openLevel.width;
			height = openLevel.height;
			tiles = openLevel.tiles;
			tileMaterials = openLevel.tileMaterials;

			editorMarkers = openLevel.editorMarkers;
			genTheme = DungeonGenerator.GetGenData(theme);

            // Only set the following fields if the current values are the default.
            if (levelName == null || levelName.isEmpty()) {
                levelName = openLevel.levelName;
            }

            if (ambientColor.equals(defaultAmbientColor)) {
                ambientColor.set(openLevel.ambientColor);
            }

            if (fogColor.equals(defaultFogColor)) {
                fogColor.set(openLevel.fogColor);
            }

            if (fogStart == 0) {
                fogStart = openLevel.fogStart;
            }

            if (fogEnd == 0) {
                fogEnd = openLevel.fogEnd;
            }

            if (skyLightColor.equals(defaultSkyLightColor)) {
                skyLightColor.set(openLevel.skyLightColor);
            }

            if (shadowColor.equals(defaultShadowColor)) {
                shadowColor.set(openLevel.shadowColor);
            }

            if (openLevel.skybox != null && skybox == null) {
                skybox = new DrawableMesh();
                skybox.meshFile = openLevel.skybox.meshFile;
                skybox.textureFile = openLevel.skybox.textureFile;
                skybox.isDirty = true;
            }

            if (music == null || music.isEmpty()) {
                music = openLevel.music;
            }

            if (actionMusic == null || actionMusic.isEmpty()) {
                actionMusic = openLevel.actionMusic;
            }

            loopMusic |= openLevel.loopMusic;

            if (ambientSound == null || ambientSound.isEmpty()) {
                ambientSound = openLevel.ambientSound;
            }

            if (ambientSoundVolume == defaultAmbientSoundVolume) {
                ambientSoundVolume = openLevel.ambientSoundVolume;
            }

            if (viewDistance == defaultViewDistance) {
                viewDistance = openLevel.viewDistance;
            }

			for(int i = 0; i < openLevel.entities.size; i++) {
				Entity copy = openLevel.entities.get(i);
				if(!copy.checkDetailLevel() || (copy.spawnChance < 1f && Game.rand.nextFloat() > copy.spawnChance)) continue;

				if(!copy.isDynamic)
					static_entities.add(copy);
				else if(!copy.isSolid && !(copy instanceof ButtonDecal))
					non_collidable_entities.add(copy);
				else
					entities.add(copy);
			}
		}
		else {
			// Or generate one!
			Boolean isValid = false;

			while(isValid == false) {
				Gdx.app.log("DelverGenerator", "Making level");

                DungeonGenerator generator;
                Level generated = null;

				Progression progression = null;
				if(Game.instance != null) {
					progression = Game.instance.progression;
				}

				if(progression == null) {
					progression = new Progression();
				}

                // Try to generate a level
				try {
                    generator = new DungeonGenerator(new Random(), dungeonLevel);
                    generated = generator.MakeDungeon(theme, roomGeneratorType, roomGeneratorChance, progression);
                    isValid = checkIsValidLevel(generated, dungeonLevel);
                }
                catch(Exception ex) {
                    Gdx.app.error("DelverGenerator", ex.getMessage());
                }

                // Did we make a valid level? Try again if not.
                if (!isValid || generated == null) {
                    Gdx.app.log("DelverGenerator", "Bad level. Trying again");
                    continue;
                }

                progression.markDungeonAreaAsSeen(theme);

				// use data from the generated level
				width = generated.width;
				height = generated.height;
				tiles = generated.tiles;
				tileMaterials = generated.tileMaterials;

				editorMarkers = generated.editorMarkers;
				genTheme = generated.genTheme;

				for(int i = 0; i < generated.entities.size; i++) {
					Entity copy = generated.entities.get(i);
					if(!copy.checkDetailLevel() || (copy.spawnChance < 1f && Game.rand.nextFloat() > copy.spawnChance)) continue;

					if(!copy.isDynamic)
						static_entities.add(copy);
					else if(!copy.isSolid)
						non_collidable_entities.add(copy);
					else
						entities.add(copy);
				}

				generated = null;
			}
		}

		loadSurprises(genTheme);

		// when generating, keep track of where the possible stair locations are
		Array<Vector2> stairLocations = new Array<Vector2>();

		Tile.solidWall.wallTex = (byte) defaultWallTex;
		Tile.solidWall.wallBottomTex = (byte) defaultWallTex;
		Tile.solidWall.ceilTex = (byte) defaultCeilTex;
		Tile.solidWall.floorTex = (byte) defaultFloorTex;

		// mark some locations as trap-free
		Array<Vector2> trapAvoidLocs = new Array<Vector2>();

		initPrefabs(Source.LEVEL_START);

		decorateLevel();

		// keep a list of places to avoid when making traps
		Boolean canMakeTrap[] = new Boolean[width * height];
		for(int i = 0; i < width * height; i++) canMakeTrap[i] = true;

		addEntitiesFromMarkers(editorMarkers, trapAvoidLocs, canMakeTrap, stairLocations, genTheme, 0, 0);

		// second pass - make stairs and traps
		if(generated) {

			if(stairLocations != null && stairLocations.size > 0) {
				trapAvoidLocs.addAll(stairLocations);
			}

			// place stairs if needed, need to know their locations before generating entities
			if(stairLocations.size > 0 && generated && source != Source.EDITOR) {

				if(makeStairsDown) {
					// if this is the first floor, only add the stairs down. Otherwise make up and down
					Vector2 downLoc = stairLocations.get(levelRand.nextInt(stairLocations.size));
					stairLocations.removeValue(downLoc, true);

					// stairs down
					Tile downTile = getTile((int) downLoc.x, (int) downLoc.y);
					down = spawnStairs(StairDirection.down, (int) downLoc.x, (int) downLoc.y, downTile.floorHeight);
				}

				for(int i = 0; i< stairLocations.size; i++) {
					if(objectivePrefab != null && !objectivePrefab.isEmpty()) {
						// We have an objective to try to spawn on this level!
						try {
							String[] prefabInfo = objectivePrefab.split("/+");
							Entity objective = EntityManager.instance.getEntity(prefabInfo[0], prefabInfo[1]);
							if(objective != null) {
								objective.x = stairLocations.get(i).x + 0.5f;
								objective.y = stairLocations.get(i).y + 0.5f;
								entities.add(objective);
							}
						}
						catch(Exception ex) {
							Gdx.app.error("Delver", "Could not spawn objective item: " + objectivePrefab);
						}

						objectivePrefab = null;
					}
					else {
						// Make good loot!
						int num = levelRand.nextInt(5);
						Item itm = null;

						if (num == 0) {
							itm = Game.GetItemManager().GetRandomArmor(Game.instance.player.level + levelRand.nextInt(2));
						} else if (num == 1) {
							itm = Game.GetItemManager().GetRandomWeapon(Game.instance.player.level + levelRand.nextInt(2));
						} else if (num == 2) {
							itm = Game.GetItemManager().GetRandomWand();
						} else if (num == 3) {
							itm = Game.GetItemManager().GetRandomPotion();
						} else if (num == 4) {
							itm = Game.GetItemManager().GetRandomRangedWeapon(Game.instance.player.level + levelRand.nextInt(2));
						}

						if (itm != null) {
							itm.x = stairLocations.get(i).x + 0.5f;
							itm.y = stairLocations.get(i).y + 0.5f;
							entities.add(itm);
						}
					}
				}
			}

			init(source);

			for(int x = 0; x < width; x++) {
				for(int y = 0; y < width; y++) {
					Tile c = getTileOrNull(x,y);

					// make traps on empty and flat areas
					if(c != null && canMakeTrap[x + y * width] && c.CanSpawnHere() && c.isFlat()) {

						boolean makeTrap = levelRand.nextFloat() <= 0.012f;

						if(makeTrap) {

							boolean canMake = true;
							for(Vector2 avoidLoc : trapAvoidLocs) {
								if( Math.abs(x - avoidLoc.x) < 6 && Math.abs(y - avoidLoc.y) < 6 ) {
									canMake = false;
								}
							}

							if(canMake && traps != null && traps.length > 0) {
								Prefab p = new Prefab("Traps", traps[levelRand.nextInt(traps.length)]);
								p.x = x + 0.5f;
								p.y = y + 0.5f;
								p.z = c.getFloorHeight(x, y) + 0.5f;
								p.collision.set(0.49f, 0.49f, 0.5f);

								// Make sure there is actually room to do this
								if(checkEntityCollision(p.x, p.y, p.z, p.collision, null) == null) {
									entities.add(p);
									p.init(this, source);
								}
							}
						}
					}
				}
			}
		}
		else {
			init(source);
		}

		// done with the theme stuff
		genTheme = null;

		if(source != Source.EDITOR) {
			editorMarkers.clear();
			GameManager.renderer.makeMapTextureForLevel(this);
		}
	}

	private boolean adjacentToOpenSpace(int x, int y) {
		Tile e = getTileOrNull(x - 1, y);
		if(e != null && e.IsFree()) return true;

		Tile n = getTileOrNull(x, y - 1);
		if(n != null && n.IsFree()) return true;

		Tile w = getTileOrNull(x + 1, y);
		if(w != null && w.IsFree()) return true;

		Tile s = getTileOrNull(x, y + 1);
		if(s != null && s.IsFree()) return true;

		return false;
	}

	private Tile getNeighbor(int x, int y, Direction direction) {
		switch (direction) {
			case NORTH:
				return getTileOrNull(x, y - 1);

			case SOUTH:
				return getTileOrNull(x, y + 1);

			case EAST:
				return getTileOrNull(x - 1, y);

			case WEST:
				return getTileOrNull(x + 1, y);
		}

		return null;
	}

	private boolean isCorner(int x, int y) {
		Tile northTile = this.getNeighbor(x, y, Direction.NORTH);
		Tile southTile = this.getNeighbor(x, y, Direction.SOUTH);
		Tile eastTile = this.getNeighbor(x, y, Direction.EAST);
		Tile westTile = this.getNeighbor(x, y, Direction.WEST);
		Tile currTile = this.getTileOrNull(x, y);

		Tile northEastTile = this.getTile(x - 1, y - 1);
		Tile northWestTile = this.getTile(x + 1, y - 1);
		Tile southEastTile = this.getTile(x - 1, y + 1);
		Tile southWestTile = this.getTile(x + 1, y + 1);

		boolean isNorthTileSolid = northTile == null || !northTile.IsFree();
		boolean isSouthTileSolid = southTile == null || !southTile.IsFree();
		boolean isEastTileSolid = eastTile == null || !eastTile.IsFree();
		boolean isWestTileSolid = westTile == null || !westTile.IsFree();
		boolean isCurrentTileSolid = currTile == null || !currTile.IsFree();

		boolean isNorthEastTileSolid = !northEastTile.IsFree();
		boolean isNorthWestTileSolid = !northWestTile.IsFree();
		boolean isSouthWestTileSolid = !southWestTile.IsFree();
		boolean isSouthEastTileSolid = !southEastTile.IsFree();

		boolean northEastCorner = isNorthTileSolid && isEastTileSolid && !isSouthWestTileSolid;
		boolean northWestCorner = isNorthTileSolid && isWestTileSolid && !isSouthEastTileSolid;
		boolean southEastCorner = isSouthTileSolid && isEastTileSolid && !isNorthWestTileSolid;
		boolean southWestCorner = isSouthTileSolid && isWestTileSolid && !isNorthEastTileSolid;

		return !isCurrentTileSolid && (northEastCorner || northWestCorner || southEastCorner || southWestCorner);
	}

	public void decorateLevel() {
		/* FIRST PASS
		 *   Paint the tile textures. This is completed before picking GenInfos because the GenInfos depend on the
		 *   texture and atlas. This is especially true of the wall based GenInfos.
		 */
		for(int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				Tile cur = getTileOrNull(x, y);

				if(cur == null) {
					// if there is open space next to this, make this a real tile to paint it
					if(adjacentToOpenSpace(x,y)) {
						cur = Tile.NewSolidTile();
						setTile(x, y, cur);
					}
				}

				if(cur == null) continue;

				// now pick tile textures
				if(wallPainter != null) {
					String k = Integer.toString(cur.wallTex);
					if(wallPainter.containsKey(k)) {
						Array<Float> list = wallPainter.get(k);
						float f = list.get(Game.rand.nextInt(list.size));
						cur.wallTex = (byte)f;
					}

					k = Integer.toString(cur.wallBottomTex != null ? cur.wallBottomTex : cur.wallTex);
					if(wallPainter.containsKey(k)) {
						Array<Float> list = wallPainter.get(k);
						float f = list.get(Game.rand.nextInt(list.size));
						cur.wallBottomTex = (byte)f;
					}
				}
				else {
					if(genTheme != null) {
						cur.eastTex = genTheme.getWallTexture(cur.getWallTex(TileEdges.East), cur.getWallTexAtlas(TileEdges.East));
						cur.westTex = genTheme.getWallTexture(cur.getWallTex(TileEdges.West), cur.getWallTexAtlas(TileEdges.West));
						cur.northTex = genTheme.getWallTexture(cur.getWallTex(TileEdges.North), cur.getWallTexAtlas(TileEdges.North));
						cur.southTex = genTheme.getWallTexture(cur.getWallTex(TileEdges.South), cur.getWallTexAtlas(TileEdges.South));
						cur.bottomEastTex = genTheme.getWallTexture(cur.getWallBottomTex(TileEdges.East), cur.getWallBottomTexAtlas(TileEdges.East));
						cur.bottomWestTex = genTheme.getWallTexture(cur.getWallBottomTex(TileEdges.West), cur.getWallBottomTexAtlas(TileEdges.West));
						cur.bottomNorthTex = genTheme.getWallTexture(cur.getWallBottomTex(TileEdges.North), cur.getWallBottomTexAtlas(TileEdges.North));
						cur.bottomSouthTex = genTheme.getWallTexture(cur.getWallBottomTex(TileEdges.South), cur.getWallBottomTexAtlas(TileEdges.South));
						cur.wallBottomTex = genTheme.getWallTexture(cur.wallBottomTex != null ? cur.wallBottomTex : cur.wallTex, cur.wallBottomTexAtlas != null ? cur.wallBottomTexAtlas : cur.wallTexAtlas);
						cur.wallTex = genTheme.getWallTexture(cur.wallTex, cur.wallTexAtlas);
					}
				}

				if(floorPainter != null) {
					String k = Integer.toString(cur.floorTex);
					if(floorPainter.containsKey(k)) {
						Array<Float> list = floorPainter.get(k);
						float f = list.get(Game.rand.nextInt(list.size));
						cur.floorTex = (byte)f;
					}
				}
				else {
					if(genTheme != null)
						cur.floorTex = genTheme.getFloorTexture(cur.floorTex, cur.floorTexAtlas);
				}

				if(ceilPainter != null) {
					String k = Integer.toString(cur.ceilTex);
					if(ceilPainter.containsKey(k)) {
						Array<Float> list = ceilPainter.get(k);
						float f = list.get(Game.rand.nextInt(list.size));
						cur.ceilTex = (byte)f;
					}
				}
				else {
					if(genTheme != null)
						cur.ceilTex = genTheme.getCeilingTexture(cur.ceilTex, cur.ceilTexAtlas);
				}
			}
		}

		/* SECOND PASS
		 *   Place the GenInfo objects.
		 */
		for(int x = 0; x < width; x++) {
			for(int y = 0; y < height; y++) {

				Tile cur = getTileOrNull(x,y);
				if(cur == null) continue;

				// modify how much we generate based on the graphics detail level
				float graphicsQualitySpawnMod = (1 + Options.instance.graphicsDetailLevel) / 2f;
				graphicsQualitySpawnMod *= (graphicsQualitySpawnMod * 0.5f);

				// the gen info list spawns entities on tiles with matching textures
				if(genTheme != null && genTheme.genInfos != null && cur.CanDecorateHere()) {

					for(GenInfo info : genTheme.genInfos) {
						if (info.placeInCorner != GenInfo.CornerPlacement.sometimes) {
							boolean inCorner = this.isCorner(x, y);

							if (inCorner && info.placeInCorner == GenInfo.CornerPlacement.never || !inCorner && info.placeInCorner == GenInfo.CornerPlacement.only) {
								continue;
							}
						}

						// Check each neighbor tiles' upper and lower textures for a match
						boolean northWallMatch = false;
						boolean southWallMatch = false;
						boolean eastWallMatch = false;
						boolean westWallMatch = false;

						if (info.wallTex != null) {
							Tile northTile = getNeighbor(x, y, Direction.NORTH);
							if (northTile != null) {
								boolean isWall = (northTile.renderSolid && northTile.blockMotion) || northTile.ceilHeight == northTile.floorHeight;

								if (isWall) {
									byte northWallTex = northTile.southTex != null ? northTile.southTex : northTile.wallTex;
									byte northBottomWallTex = northTile.wallTex;
									String northWallAtlas = (northTile.southTex != null && northTile.southTexAtlas != null) ? northTile.southTexAtlas : northTile.wallTexAtlas;
									String northBottomWallAtlas = "";

									if (northTile.bottomSouthTex != null) {
										northBottomWallTex = northTile.bottomSouthTex;
										northBottomWallAtlas = northTile.bottomSouthTexAtlas;
									}
									else if (northTile.wallBottomTex != null) {
										northBottomWallTex = northTile.wallBottomTex;
										northBottomWallAtlas = northTile.wallBottomTexAtlas;
									}

									if ((info.wallTex == null || info.wallTex == northWallTex) &&  info.textureAtlas != null && info.textureAtlas.equals(northWallAtlas)) {
										northWallMatch = true;
									}
									else if (!northTile.renderSolid && (info.wallTex == null || info.wallTex == northBottomWallTex) && info.textureAtlas != null && info.textureAtlas.equals(northBottomWallAtlas)) {
										northWallMatch = true;
									}
								}
							}

							Tile southTile = getNeighbor(x, y, Direction.SOUTH);
							if (southTile != null) {
								boolean isWall = (southTile.renderSolid && southTile.blockMotion) || southTile.ceilHeight == southTile.floorHeight;

								if (isWall) {
									byte southWallTex = southTile.northTex != null ? southTile.northTex : southTile.wallTex;
									byte southBottomWallTex = southTile.wallTex;
									String southWallAtlas = (southTile.northTex != null && southTile.northTexAtlas != null) ? southTile.northTexAtlas : southTile.wallTexAtlas;
									String southBottomWallAtlas = "";

									if (southTile.bottomNorthTex != null) {
										southBottomWallTex = southTile.bottomNorthTex;
										southBottomWallAtlas = southTile.bottomNorthTexAtlas;
									}
									else if (southTile.wallBottomTex != null) {
										southBottomWallTex = southTile.wallBottomTex;
										southBottomWallAtlas = southTile.wallBottomTexAtlas;
									}

									if ((info.wallTex == null || info.wallTex == southWallTex) && info.textureAtlas != null && info.textureAtlas.equals(southWallAtlas)) {
										southWallMatch = true;
									}
									else if (!southTile.renderSolid && (info.wallTex == null || info.wallTex == southBottomWallTex) && info.textureAtlas != null && info.textureAtlas.equals(southBottomWallAtlas)) {
										southWallMatch = true;
									}
								}
							}

							Tile eastTile = getNeighbor(x, y, Direction.EAST);
							if (eastTile != null) {
								boolean isWall = (eastTile.renderSolid && eastTile.blockMotion) || eastTile.ceilHeight == eastTile.floorHeight;

								if (isWall) {
									byte eastWallTex = eastTile.westTex != null ? eastTile.westTex : eastTile.wallTex;
									byte eastBottomWallTex = eastTile.wallTex;
									String eastWallAtlas = (eastTile.westTex != null && eastTile.westTexAtlas != null) ? eastTile.westTexAtlas : eastTile.wallTexAtlas;
									String eastBottomWallAtlas = "";

									if (eastTile.bottomWestTex != null) {
										eastBottomWallTex = eastTile.bottomWestTex;
										eastBottomWallAtlas = eastTile.bottomWestTexAtlas;
									}
									else if (eastTile.wallBottomTex != null) {
										eastBottomWallTex = eastTile.wallBottomTex;
										eastBottomWallAtlas = eastTile.wallTexAtlas;
									}

									if ((info.wallTex == null || info.wallTex == eastWallTex) && info.textureAtlas != null && info.textureAtlas.equals(eastWallAtlas)) {
										eastWallMatch = true;
									}
									else if (!eastTile.renderSolid && (info.wallTex == null || info.wallTex == eastBottomWallTex) && info.textureAtlas != null && info.textureAtlas.equals(eastBottomWallAtlas)) {
										eastWallMatch = true;
									}
								}
							}

							Tile westTile = getNeighbor(x, y, Direction.WEST);
							if (westTile != null) {
								boolean isWall = (westTile.renderSolid && westTile.blockMotion) || westTile.ceilHeight == westTile.floorHeight;

								if (isWall) {
									byte westWallTex = westTile.eastTex != null ? westTile.eastTex : westTile.wallTex;
									byte westBottomWallTex = westTile.wallTex;
									String westWallAtlas = (westTile.eastTex != null && westTile.eastTexAtlas != null) ? westTile.eastTexAtlas : westTile.wallTexAtlas;
									String westBottomWallAtlas = "";

									if (westTile.bottomEastTex != null) {
										westBottomWallTex = westTile.bottomEastTex;
										westBottomWallAtlas = westTile.bottomEastTexAtlas;
									}
									else if (westTile.wallBottomTex != null) {
										westBottomWallTex = westTile.wallBottomTex;
										westBottomWallAtlas = westTile.bottomWestTexAtlas;
									}

									if ((info.wallTex == null || info.wallTex == westWallTex) && info.textureAtlas != null && info.textureAtlas.equals(westWallAtlas)) {
										westWallMatch = true;
									}

									if (!westTile.renderSolid && (info.wallTex == null || info.wallTex == westBottomWallTex) && info.textureAtlas != null && info.textureAtlas.equals(westBottomWallAtlas)) {
										westWallMatch = true;
									}
								}
							}
						}

						boolean wallMatch = info.wallTex == null || northWallMatch || southWallMatch || eastWallMatch || westWallMatch;
						boolean ceilmatch = info.ceilTex == null || (cur.ceilTex == info.ceilTex && checkIfTextureAtlasesMatch(cur.ceilTexAtlas, info.textureAtlas));
						boolean floormatch = info.floorTex == null || (cur.floorTex == info.floorTex && checkIfTextureAtlasesMatch(cur.floorTexAtlas, info.textureAtlas));

						// don't spawn solid objects on Markers
						boolean canSpawnSolid = true;
						for(int i = 0; i < editorMarkers.size; i++) {
							EditorMarker m = editorMarkers.get(i);
							if(m.x == x && m.y == y) {
								canSpawnSolid = false;
							}
						}

						if( wallMatch && ceilmatch && floormatch && (info.chance >= 1 || (Game.rand.nextFloat() <= info.chance * graphicsQualitySpawnMod)) ) {

							// add any marker
							if(info.marker != null && info.marker != Markers.none) {
								editorMarkers.add( new EditorMarker(info.marker, x,y) );
							}

							// copy and place the entity, if one was given
							if(info.spawns != null) {
								try {
									int num = 1;
									if(info.clusterCount > 1) num = Game.rand.nextInt(info.clusterCount) + 1;
									if(info.performanceControlledClustering) num *= Options.instance.gfxQuality;
									for(int i = 0; i < num; i++) {
										Array<Entity> copyList = (Array<Entity>) KryoSerializer.copyObject(info.spawns);

										for(Entity copy : copyList) {
											// might need to skip this.
											if(!canSpawnSolid) {
												if(copy.isSolid || (copy instanceof Group))
													continue;
											}

											copy.x = x + 0.5f;
											copy.y = y + 0.5f;

											if(info.clusterSpread != 0) {
												copy.x += (Game.rand.nextFloat() * (info.clusterSpread * 2f)) - info.clusterSpread;
												copy.y += (Game.rand.nextFloat() * (info.clusterSpread * 2f)) - info.clusterSpread;
											}

											if (info.attachFloor) {
												copy.z = cur.getFloorHeight(copy.x, copy.y) + 0.5f;
											}
											else {
												copy.z += cur.getFloorHeight(copy.x, copy.y) + 0.5f;
											}

											if(info.attachCeiling) {
												if(copy instanceof Sprite) {
													copy.z = cur.getCeilHeight(copy.x, copy.y) - 0.5f;
												}
												else {
													copy.z = (cur.getCeilHeight(copy.x, copy.y) + 0.5f) - copy.collision.z;
												}
											}
											if(info.attachWall) {
												if (northWallMatch) {
													Entity northCopy = (Entity) KryoSerializer.copyObject(copy);
													decorateWallWith(northCopy, Direction.NORTH, false, true, info.wallOffset);
													this.addEntity(northCopy);
												}

												if (southWallMatch) {
													Entity southCopy = (Entity) KryoSerializer.copyObject(copy);
													decorateWallWith(southCopy, Direction.SOUTH, false, true, info.wallOffset);
													this.addEntity(southCopy);
												}

												if (eastWallMatch) {
													Entity eastCopy = (Entity) KryoSerializer.copyObject(copy);
													decorateWallWith(eastCopy, Direction.EAST, false, true, info.wallOffset);
													this.addEntity(eastCopy);
												}

												if (westWallMatch) {
													Entity westCopy = (Entity) KryoSerializer.copyObject(copy);
													decorateWallWith(westCopy, Direction.WEST, false, true, info.wallOffset);
													this.addEntity(westCopy);
												}

												// We've already added a copy, clean this one up
												copy.isActive = false;
												continue;
											}

											if(copy instanceof Group) {
												copy.updateDrawable();
												((Group)copy).updateCollision();
											}

											if(copy.isSolid || copy instanceof Group) {
												boolean isFree = isFree(copy.x - 0.5f, copy.y - 0.5f, copy.z, copy.collision, copy.stepHeight, copy.floating, null);
												boolean isEntityFree = checkEntityCollision(copy.x - 0.5f, copy.y - 0.5f, copy.z, copy.collision, null) == null;

												// Don't spawn this if something is already here!
												if (!isFree || !isEntityFree) {
													copy.isActive = false;
													continue;
												}
											}

											this.addEntity(copy);
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

				// Might need to repaint this tile
				overrideTileTextures(cur);
			}
		}
	}

	protected void loadSurprises(GenTheme theme) {
		EntityManager.instance.surprises = theme.surprises;
	}

	protected void addEntitiesFromMarkers(Array<EditorMarker> markers, Array<Vector2> trapAvoidLocs, Boolean[] canMakeTrap, Array<Vector2> stairLocations, GenTheme genTheme, int xOffset, int yOffset) {

		updateSpatialHash(null);
		updateStaticSpatialHash();

		if(spawnRates == null) spawnRates = new SpawnRate();

		// add entities from markers
		if(markers != null && markers.size > 0) {
			for(EditorMarker marker : markers) {

				int x = marker.x + xOffset;
				int y = marker.y + yOffset;

				Vector2 offset = new Vector2(0, 0);
				Tile atTile = this.getTileOrNull(x, y);
				float floorPos = atTile != null ? atTile.getFloorHeight(marker.x + xOffset, marker.y + yOffset) : 0;
				float ceilPos = atTile != null ? atTile.ceilHeight : 0;

				if(atTile != null) {
					if(atTile.tileSpaceType == TileSpaceType.OPEN_NE) {
						offset.set(0.3f, -0.3f);
					}
					else if(atTile.tileSpaceType == TileSpaceType.OPEN_NW) {
						offset.set(-0.3f, -0.3f);
					}
					else if(atTile.tileSpaceType == TileSpaceType.OPEN_SE) {
						offset.set(0.3f, 0.3f);
					}
					else if(atTile.tileSpaceType == TileSpaceType.OPEN_SW) {
						offset.set(-0.3f, 0.3f);
					}
				}

				if(marker.type == Markers.playerStart) {
					playerStartX = x;
					playerStartY = y;
					playerStartRot = marker.rot;
					trapAvoidLocs.add(new Vector2(x,y));
				}
				else if(marker.type == Markers.torch) {
					if(spawnRates != null && Game.rand.nextFloat() > spawnRates.lights) continue;

					Entity t = null;
					if(genTheme != null && genTheme.spawnLights != null && genTheme.spawnLights.size > 0) {
						Entity light = genTheme.spawnLights.get(Game.rand.nextInt(genTheme.spawnLights.size));
						t = (Light) KryoSerializer.copyObject(light);
					}
					else {
						Entity torch = EntityManager.instance.getEntity("Lights", "Torch");
						if(torch != null) {
							t = torch;
						}
						else {
							// orange default torch
							t = new Torch(x + 0.5f + offset.x, y + 0.5f + offset.y, 4, new Color(1f, 0.8f, 0.4f, 1f));
						}
					}

					if(t != null) {
						t.x = x + 0.5f + offset.x;
						t.y = y + 0.5f + offset.y;
						t.z = floorPos + 0.5f;

						Entity light = decorateWallWith(t, false, true);
						if(light != null && light.isActive) {
							SpawnNonCollidingEntity(light);
						}
					}
				}
				else if(marker.type == Markers.stairDown) {
					down = spawnStairs(StairDirection.down, x, y, floorPos);
					trapAvoidLocs.add(new Vector2(x,y));
				}
				else if(marker.type == Markers.stairUp) {
					up = spawnStairs(StairDirection.up, x, y, ceilPos);
					trapAvoidLocs.add(new Vector2(x,y));
				}
				else if(marker.type == Markers.boss) {
					// make the boss and the orb
					Item orb = new QuestItem(x + 0.5f + offset.x, y + 0.5f + offset.y);
					orb.z = floorPos + 0.5f;
					entities.add(orb);

					// grab a monster from the BOSS category
					Monster m = Game.GetMonsterManager().GetRandomMonster("BOSS");
					if(m != null) {
						m.x = x + 0.5f;
						m.y = y + 0.5f;
						m.z = floorPos + 0.5f;
						m.Init(this, 20);
						SpawnEntity(m);
					}

					if(canMakeTrap != null) {
						if (x >= 0 && x < width && y >= 0 && y < height)
							canMakeTrap[x + y * width] = false;
					}
				}
				else if(marker.type == Markers.door) {
					Door door = null;

					if(genTheme != null && genTheme.doors != null && genTheme.doors.size > 0) {
						door = new Door(genTheme.doors.get(Game.rand.nextInt(genTheme.doors.size)));
					}

					if(door == null) {
						door = new Door(x + 0.5f, y + 0.5f, 10);
					}

					door.x = x + 0.5f;
					door.y = y + 0.5f;
					door.z = floorPos + 0.5f;

					SpawnEntity(door);
					door.placeFromPrefab(this);

					// Make sure traps cannot spawn around door locations.
					if(canMakeTrap != null && x >= 0 && x < width && y >= 0 && y < height) {
						canMakeTrap[x + y * width] = false;
					}
				}
				else if(marker.type == Markers.decor || (marker.type == Markers.decorPile && genTheme != null && genTheme.decorations != null)) {
					if(spawnRates != null && Game.rand.nextFloat() > spawnRates.decor) continue;

					// try to pull a decoration from the genTheme, or just make one from the item list
					Entity d = null;

					if(genTheme != null && genTheme.decorations != null) {
						if(genTheme.decorations.size > 0)
							d = EntityManager.instance.Copy(genTheme.decorations.get(Game.rand.nextInt(genTheme.decorations.size)));
					}
					else {
						d = Game.GetItemManager().GetRandomDecoration();
					}

					if( d != null ) {
						float rx = (Game.rand.nextFloat() * (1 - d.collision.x * 2f)) - (1 - d.collision.x * 2f) / 2f;
						float ry = (Game.rand.nextFloat() * (1 - d.collision.y * 2f)) - (1 - d.collision.y * 2f) / 2f;
						d.x = x + rx + 0.5f + offset.x;
						d.y = y + ry + 0.5f + offset.y;
						d.z = atTile.getFloorHeight(d.x, d.y) + 0.5f;

						SpawnEntity(d);
					}

					if(canMakeTrap != null) {
						if (x >= 0 && x < width && y >= 0 && y < height)
							canMakeTrap[x + y * width] = false;
					}
				}
				else if(marker.type == Markers.decorPile) {
					if(spawnRates != null && Game.rand.nextFloat() > spawnRates.decor) continue;

					int num = Game.rand.nextInt(3) + 1;
					for(int i = 0; i < num; i++)
					{
						Entity d = Game.GetItemManager().GetRandomDecoration();
						if( d != null ) {
							float rx = (Game.rand.nextFloat() * (1 - d.collision.x * 2f)) - (1 - d.collision.x * 2f) / 2f;
							float ry = (Game.rand.nextFloat() * (1 - d.collision.y * 2f)) - (1 - d.collision.y * 2f) / 2f;
							d.x = x + rx + 0.5f + offset.x;
							d.y = y + ry + 0.5f + offset.y;
							d.z = atTile.getFloorHeight(d.x, d.y) + 0.5f;

							SpawnEntity(d);
						}
					}

					if(canMakeTrap != null) {
						if (x >= 0 && x < width && y >= 0 && y < height)
							canMakeTrap[x + y * width] = false;
					}
				}
				else if(marker.type == Markers.monster) {

					if(spawnRates != null && Game.rand.nextFloat() > spawnRates.monsters) continue;
					String levelTheme = theme;

					// Rarely, grab monsters from another theme if set
					if(alternateMonsterThemes != null && alternateMonsterThemes.size > 0) {
						if(Game.rand.nextFloat() < 0.1f) {
							levelTheme = alternateMonsterThemes.random();
						}
					}

					Monster m = Game.GetMonsterManager().GetRandomMonster(levelTheme);

					if(m != null)
					{
						m.x = x + 0.5f + offset.x;
						m.y = y + 0.5f + offset.y;
						m.z = floorPos + 0.5f;
						m.Init(this, Game.instance.player.level);
						SpawnEntity(m);
					}

					if(canMakeTrap != null) {
						if (x >= 0 && x < width && y >= 0 && y < height)
							canMakeTrap[x + y * width] = false;
					}
				}
				else if(marker.type == Markers.key)
				{
					// make a key
					Key key = new Key(x + 0.5f + offset.x, y + 0.5f + offset.y);
					key.z = floorPos + 0.5f;
					SpawnEntity(key);

					if(canMakeTrap != null) {
						if (x >= 0 && x < width && y >= 0 && y < height)
							canMakeTrap[x + y * width] = false;

						trapAvoidLocs.add(new Vector2(x, y));
					}
				}
				else if(marker.type == Markers.loot)
				{
					if(spawnRates != null && Game.rand.nextFloat() > spawnRates.loot) continue;

					// loot!
					Item itm = Game.GetItemManager().GetLevelLoot(Game.instance.player.level);

					if(itm != null) {
						itm.x = x + 0.5f + offset.x;
						itm.y = y + 0.5f + offset.y;
						itm.z = floorPos + 0.5f;

						SpawnEntity(itm);
					}

					if(canMakeTrap != null) {
						if (x >= 0 && x < width && y >= 0 && y < height)
							canMakeTrap[x + y * width] = false;
					}
				}
				else if(marker.type == Markers.exitLocation)
				{
					if(generated) stairLocations.add(new Vector2(x,y));
				}
				else if(marker.type == Markers.secret)
				{

					Item itm = null;
					if(Game.rand.nextBoolean()) {
						itm = new Gold(Game.rand.nextInt((dungeonLevel * 3) + 1) + 10);
					}
					else {
						// Make good loot!
						int num = Game.rand.nextInt(5);

						if (num == 0) {
							itm = Game.GetItemManager().GetRandomArmor(Game.instance.player.level + Game.rand.nextInt(2));
						} else if (num == 1) {
							itm = Game.GetItemManager().GetRandomWeapon(Game.instance.player.level + Game.rand.nextInt(2));
						} else if (num == 2) {
							itm = Game.GetItemManager().GetRandomWand();
						} else if (num == 3) {
							itm = Game.GetItemManager().GetRandomPotion();
						} else if (num == 4) {
							itm = Game.GetItemManager().GetRandomRangedWeapon(Game.instance.player.level + Game.rand.nextInt(2));
						}
					}

					if(itm != null) {
						itm.x = x + 0.5f + offset.x;
						itm.y = y + 0.5f + offset.y;
						itm.z = floorPos + 0.5f;
						SpawnEntity(itm);
					}

					// Add the secret trigger
					Trigger secretTrigger = new Trigger();
					secretTrigger.message = "A secret has been revealed!";
					secretTrigger.x = x + 0.5f + offset.x;
					secretTrigger.y = y + 0.5f + offset.y;
					secretTrigger.z = floorPos + 0.5f;
					secretTrigger.collision.x = 0.65f;
					secretTrigger.collision.y = 0.65f;
					secretTrigger.collision.z = 1f;
					secretTrigger.triggerType = Trigger.TriggerType.PLAYER_TOUCHED;
					secretTrigger.triggerResets = false;
					secretTrigger.messageTime = 2f;
					secretTrigger.triggerSound = "ui/ui_secret_found.mp3";
					secretTrigger.isSecret = true;
					SpawnEntity(secretTrigger);

					// Add a monster, rarely
					if(Game.rand.nextFloat() > 0.85f) {
						Monster m = Game.GetMonsterManager().GetRandomMonster(theme);

						if(m != null)
						{
							m.x = x + 0.5f + offset.x;
							m.y = y + 0.5f + offset.y;
							m.z = floorPos + 0.5f;
							m.Init(this, Game.instance.player.level);
							SpawnEntity(m);
						}
					}

					if(canMakeTrap != null) {
						if (x >= 0 && x < width && y >= 0 && y < height)
							canMakeTrap[x + y * width] = false;
					}
				}
			}
		}
	}

	private static Boolean checkIsValidLevel(Level tocheck, int dungeonlevel) {
		if(tocheck == null) return false;

		Array<Level> levels = Game.buildLevelLayout();
		if(dungeonlevel < levels.size) {
			// look for exit markers
			for(EditorMarker m : tocheck.editorMarkers) {
				if(m.type == Markers.exitLocation || m.type == Markers.stairDown) return true;
			}

			// look for exit entities
			for(Entity e : tocheck.entities) {
				if(e instanceof Stairs && ((Stairs)e).direction == StairDirection.down) return true;
			}
		}
		else {
			return true;
		}

		return false;
	}

	public void initPrefabs(Source source) {
		// init any prefabs
		// this is a separate pass as things like Monsters might want to check their collisions!

		for(int i = 0; i < entities.size; i++) {
			Entity e = entities.get(i);
			if(e instanceof Group) {
				e.init(this, source);
			}
		}
		for(int i = 0; i < non_collidable_entities.size; i++) {
			Entity e = non_collidable_entities.get(i);
			if(e instanceof Group) {
				e.init(this, source);
			}
		}
		for(int i = 0; i < static_entities.size; i++) {
			Entity e = static_entities.get(i);
			if(e instanceof Group) {
				e.init(this, source);
			}
		}

		updateSpatialHash(null);
		updateStaticSpatialHash();
	}

	public void init(Source source) {
		// can't init until loaded
		if(!isLoaded) return;

		Tile.solidWall.wallTex = 0;

		// set default wall texture
		if(wallPainter != null) {
			if(wallPainter.containsKey("0") && wallPainter.get("0").size > 0)
				Tile.solidWall.wallTex = (byte) Math.round((wallPainter.get("0").get(0)));
		}

		// initialize tiles
		for(int i = 0; i < tiles.length; i++) {
			if(tiles[i] != null) {
				tiles[i].init(source);
			}
		}

		// init the drawables
		initEntities(entities, source);
		initEntities(non_collidable_entities, source);
		initEntities(static_entities, source);

		// light the map!
		updateLights(source);

		updateStaticSpatialHash();

		if(GameManager.renderer != null) GameManager.renderer.setLevelToRender(this);
		this.isDirty = true;

		if(spawnEncounterDuringChase && Game.instance != null && Game.instance.player != null) {
			spawnChaseEncounter();
		}

		Game.pathfinding.InitForLevel(this);
	}

	public void initEntities(Array<Entity> entityList, Source source) {
		if(entityList == null)
			return;

		for(int i = 0; i < entityList.size; i++) {
			Entity e = entityList.get(i);

			// Might need to override the sprite atlas
			if(source == Source.LEVEL_START && spriteAtlasOverrides != null) {
				overrideSpriteAtlas(e);
			}

			if(e.drawable != null) {
				e.drawable.refresh();
			}

			e.init(this, source);
		}
	}

	public Tile getTile(int x, int y)
	{
		if(x < 0 || x >= width || y < 0 || y >= height) return Tile.solidWall;
		if(tiles[x + y * width] == null) return Tile.solidWall;
		return tiles[x + y * width];
	}

	public void setTile(int x, int y, Tile t) {
		if(x >= 0 && x < width && y >= 0 && y < height) {
			tiles[x + y * width] = t;
		}

		if(t != null) {
			t.init(Source.EDITOR);
		}
	}

	public void setTileIfUnlocked(int x, int y, Tile t) {
		if(x >= 0 && x < width && y >= 0 && y < height) {
			Tile existing = tiles[x + y * width];
			if(existing == null || !existing.isLocked) {
				tiles[x + y * width] = t;
			}
		}

		if(t != null) {
			t.init(Source.EDITOR);
		}
	}

    public boolean inBounds(int x, int y) {
        if(x < 0 || x >= width || y < 0 || y >= height) return false;
        return true;
    }

	public Tile getTileOrNull(int x, int y)
	{
		if(x < 0 || x >= width || y < 0 || y >= height) return null;
		if(tiles[x + y * width] == null) return null;
		return tiles[x + y * width];
	}

	public Tile findWaterTile(float x, float y, float z, Vector3 collision) {
		Tile foundTile = null;
		for(float xx = -collision.x; xx <= collision.x; xx += collision.x * 2f) {
			for(float yy = -collision.y; yy <= collision.y; yy += collision.y * 2f) {
				Tile tile = getTileOrNull((int)(x + xx), (int)(y + yy));
				if(tile != null && tile.data.isWater && z < tile.floorHeight + 0.5f) {
					if(foundTile == null) foundTile = tile;
					else if(foundTile.floorHeight < tile.floorHeight) foundTile = tile;
				}
			}
		}
		return foundTile;
	}

    public float maxLowerWallHeight(int x, int y) {
        float max = 0;
        Tile c = getTileOrNull(x, y);
        if(c != null) {
            for (int xx = x - 1; xx <= x + 1; xx++) {
                for (int yy = y - 1; yy <= y + 1; yy++) {
                    Tile checking = getTileOrNull(xx, yy);
                    if(checking != null) {
                        float wallheight = c.floorHeight - checking.floorHeight;
                        if(wallheight > max) max = wallheight;
                    }
                }
            }
        }

        return max;
    }

	public Entity decorateWallWith(Entity sprite, boolean putOnFloor, boolean deleteIfNotPlaceable)
	{
		int x = (int)(sprite.x);
		int y = (int)(sprite.y);

		// offset the sprite to stick to a wall, or null if there are none adjacent
		Tile c = getTile(x, y);
		Tile w = getTile(x - 1, y);
		Tile e = getTile(x + 1, y);
		Tile n = getTile(x, y - 1);
		Tile s = getTile(x, y + 1);

		float sPosR = 0.4999f - (sprite.collision.y);
		float sPosR_diagonal = (float) Math.sqrt(sprite.collision.x*sprite.collision.x);
		float rot = 0;

		//first try to put the object on a near-by solid wall
		if(w.isEastSolid() && !c.isWestSolid()) {
			w.lockTile();
			sprite.x = x + 0.5f;
			sprite.x -= sPosR;
			rot = 90;
		}
		else if(e.isWestSolid() && !c.isEastSolid()) {
			e.lockTile();
			sprite.x = x + 0.5f;
			sprite.x += sPosR;
			rot = 270;
		}
		else if(n.isSouthSolid() && !c.isNorthSolid()) {
			n.lockTile();
			sprite.y = y + 0.5f;
			sprite.y -= sPosR;
		}
		else if(s.isNorthSolid() && !c.isSouthSolid()) {
			s.lockTile();
			sprite.y = y + 0.5f;
			sprite.y += sPosR;
			rot = 180;
		} //if that fails, look for a square with a higher floor to act as a wall
		else if (w.floorHeight-c.floorHeight>=0.9f && !c.isWestSolid() && !w.data.isWater){
			w.lockTile();
			sprite.x = x + 0.5f;
			sprite.x -= sPosR;
			rot = 90;
		} else if (e.floorHeight-c.floorHeight>=0.9f && !c.isEastSolid() && !e.data.isWater){
			e.lockTile();
			sprite.x = x + 0.5f;
			sprite.x += sPosR;
			rot = 270;
		} else if (n.floorHeight-c.floorHeight>=0.9f && !c.isNorthSolid() && !n.data.isWater){
			n.lockTile();
			sprite.y = y + 0.5f;
			sprite.y -= sPosR;
		} else if (s.floorHeight-c.floorHeight>=0.9f && !c.isSouthSolid() && !s.data.isWater){
			s.lockTile();
			sprite.y = y + 0.5f;
			sprite.y += sPosR;
			rot = 180;
		} //if that fails, check to see if this square is a diagonal
		else if (c.tileSpaceType==TileSpaceType.OPEN_NE) {
			c.lockTile();
			sprite.x = x + 0.5f + sPosR_diagonal;
			sprite.y = y + 0.5f - sPosR_diagonal;
			rot=315;
		}
		else if (c.tileSpaceType==TileSpaceType.OPEN_NW) {
			c.lockTile();
			sprite.x = x + 0.5f - sPosR_diagonal;
			sprite.y = y + 0.5f - sPosR_diagonal;
			rot=45;
		}
		else if (c.tileSpaceType==TileSpaceType.OPEN_SE) {
			c.lockTile();
			sprite.x = x + 0.5f + sPosR_diagonal;
			sprite.y = y + 0.5f + sPosR_diagonal;
			rot=225;
		}
		else if (c.tileSpaceType==TileSpaceType.OPEN_SW) {
			c.lockTile();
			sprite.x = x + 0.5f - sPosR_diagonal;
			sprite.y = y + 0.5f + sPosR_diagonal;
			rot=135;
		}
        else {
            if(deleteIfNotPlaceable) sprite.isActive = false;
            if(putOnFloor) sprite.z -= 0.25; // fine, put it on the floor
        }

		if(sprite instanceof DirectionalEntity) {
			// rotate
            DirectionalEntity m = (DirectionalEntity)sprite;

            float extraRot = rot;
            for(int i = 0; i < rot; i += 90) {
                m.rotate90Reversed();
                extraRot -= 90;
            }

            // apply any extra rotation
            m.rotation.z += extraRot;
		}

		return sprite;
	}

	public Entity decorateWallWith(Entity sprite, Direction direction, boolean putOnFloor, boolean deleteIfNotPlaceable, float wallOffset)
	{
		int x = (int)(sprite.x);
		int y = (int)(sprite.y);
		boolean wasSolid = sprite.isSolid;

		// Don't place if a marker is present
		for(int i = 0; i < this.editorMarkers.size; i++) {
			EditorMarker em = this.editorMarkers.get(i);

			if (em.x == x && em.y == y) {
				sprite.isActive = false;
				return sprite;
			}
		}

		if (sprite instanceof Group) {
			Group g = (Group)sprite;
			g.updateDrawable();
			g.updateCollision();
		}

		float sPosR = 0.4999f - (sprite.collision.y) - wallOffset;
		float sPosR_diagonal = (float) Math.sqrt(sprite.collision.x*sprite.collision.x);
		float rot = 0;

		if(direction == Direction.EAST) {
			sprite.x = x + 0.5f;
			sprite.x -= sPosR;
			rot = 90;
		}
		else if(direction == Direction.WEST) {
			sprite.x = x + 0.5f;
			sprite.x += sPosR;
			rot = 270;
		}
		else if(direction == Direction.NORTH) {
			sprite.y = y + 0.5f;
			sprite.y -= sPosR;
		}
		else if(direction == Direction.SOUTH) {
			sprite.y = y + 0.5f;
			sprite.y += sPosR;
			rot = 180;
		}
		else {
			if(deleteIfNotPlaceable) sprite.isActive = false;
			if(putOnFloor) sprite.z -= 0.25; // fine, put it on the floor
		}


		if (sprite instanceof  Group) {
			Group g = (Group)sprite;

			float extraRot = rot;
			for(int i = 0; i < rot; i += 90) {
				g.rotate90Reversed();
				extraRot -= 90;
			}

			// apply any extra rotation
			g.rotation.z += extraRot;
			g.isSolid = true;
			g.updateDrawable();
			g.updateCollision();
		}
		else if(sprite instanceof DirectionalEntity) {
			// rotate
			DirectionalEntity m = (DirectionalEntity)sprite;

			float extraRot = rot;
			for(int i = 0; i < rot; i += 90) {
				m.rotate90Reversed();
				extraRot -= 90;
			}

			// apply any extra rotation
			m.rotation.z += extraRot;
		}

		// Don't place outside of level
		if (!this.entityContainedInTile(sprite)) {
			sprite.isActive = false;
			sprite.isSolid = wasSolid;

			return sprite;
		}

		// Don't overlap existing geo
		Array<Entity> entitiesToCheck = this.spatialhash.getEntitiesAt(sprite.x, sprite.y, Math.max(sprite.collision.x, sprite.collision.y));
		for(int i = 0; i < entitiesToCheck.size; i++) {
			if (this.entitiesIntersect(entitiesToCheck.get(i), sprite)) {
				sprite.isActive = false;
				sprite.isSolid = wasSolid;
				return sprite;
			}
		}

		Array<Entity> staticEntitiesToCheck = this.staticSpatialhash.getEntitiesAt(sprite.x, sprite.y, Math.max(sprite.collision.x, sprite.collision.y));
		for(int i = 0; i < staticEntitiesToCheck.size; i++) {
			if (this.entitiesIntersect(staticEntitiesToCheck.get(i), sprite)) {
				sprite.isActive = false;
				sprite.isSolid = wasSolid;
				return sprite;
			}
		}

		for(int i = 0; i < this.non_collidable_entities.size; i++) {
			if (this.entitiesIntersect(non_collidable_entities.get(i), sprite)) {
				sprite.isActive = false;
				sprite.isSolid = wasSolid;
				return sprite;
			}
		}

		return sprite;
	}

	public BoundingBox getAABB(BoundingBox b, Entity e) {
		if(e instanceof Player) {
			return getAABB(b, e, 0.5f, 0.5f, -0.5f);
		}
		else {
			return getAABB(b, e, 0f, 0f, -0.5f);
		}
	}

	Vector3 t_bbTempMin = new Vector3();
	Vector3 t_bbTempMax = new Vector3();
	public BoundingBox getAABB(BoundingBox b, Entity e, float xOffset, float yOffset, float zOffset) {
		b.set(t_bbTempMin.set(e.x - e.collision.x + xOffset, e.z + zOffset, e.y - e.collision.y + yOffset), t_bbTempMax.set(e.x + e.collision.x + xOffset, e.z + e.collision.z + zOffset, e.y + e.collision.y + yOffset));
		return b;
	}

	BoundingBox t_intersectCheck1 = new BoundingBox();
	BoundingBox t_intersectCheck2 = new BoundingBox();
	public boolean entitiesIntersect(Entity e, Entity f) {
		t_intersectCheck1 = getAABB(t_intersectCheck1, e);
		t_intersectCheck2 = getAABB(t_intersectCheck2, f);
		return t_intersectCheck1.intersects(t_intersectCheck2);
	}

	BoundingBox t_intersectCheck3 = new BoundingBox();
	public boolean entityContainedInTile(Entity e) {
		BoundingBox eBB = getAABB(t_intersectCheck1, e);
		int minX = (int)eBB.min.x;
		int maxX = (int)eBB.max.x;
		int minY = (int)eBB.min.z;
		int maxY = (int)eBB.max.z;

		for (int x = minX; x <= maxX; x++) {
			for (int y = minY; y <= maxY; y++) {
				Tile t = getTile(x, y);

				//if (t == null || t.blockMotion) {
				//	return false;
				//}

				if (eBB.max.y > t.ceilHeight) {
					return false;
				}

				if (eBB.min.y < t.floorHeight) {
					return false;
				}
			}
		}

		return true;
	}

	public void cleanupLightCache() {
		for(Entity e : entities) {
			if(e instanceof Light) {
				((Light)e).clearCanSee();
			}
		}

		for(Entity e : non_collidable_entities) {
			if(e instanceof Light) {
				((Light)e).clearCanSee();
			}
		}

		for(Entity e : static_entities) {
			if(e instanceof Light) {
				((Light)e).clearCanSee();
			}
		}
	}

	private void updateLight(Entity entity) {
		if (entity == null) {
			return;
		}

		if(entity instanceof Light && entity.isActive)
		{
			Light t = (Light)entity;
			lightSpatialHash.AddLight(t);
		}

		if (!(entity instanceof Group)) {
			return;
		}

		Group group = (Group)entity;
		for (Entity e : group.entities) {
			updateLight(e);
		}
	}

	public void updateLights(Source source)
	{
		lightSpatialHash.Flush();

		// light markers
		if(source == Source.EDITOR) {
			for(int i = 0; i < editorMarkers.size; i++) {
				if(editorMarkers.get(i).type == Markers.torch) {
					EditorMarker t = editorMarkers.get(i);

					Light l = new Light();
					l.x = t.x + 0.5f;
					l.y = t.y + 0.5f;
					l.z = getTile(t.x,t.y).floorHeight;
					l.lightColor.set(new Color(1f, 0.8f, 0.4f, 1f));
					l.range = 3.0f;

					lightSpatialHash.AddLight(l);
				}
			}
		}

		// light emitting entities
		for(int i = 0; i < entities.size; i++)
		{
			Entity e = entities.get(i);
			updateLight(e);
		}

		for(int i = 0; i < non_collidable_entities.size; i++)
		{
			Entity e = non_collidable_entities.get(i);
			if(e instanceof Light && e.isActive)
			{
				Light t = (Light)e;
				lightSpatialHash.AddLight(t);
			}

			// make some dynamic lights static for the fallback renderer
			if(Gdx.gl20 == null) {
				if(e instanceof DynamicLight && e.isActive) {
					DynamicLight t = (DynamicLight)e;
					//lightRadius(t.x, t.y, t.z, t.range * 0.8f, t.color, false);

					Light light = new Light();
					light.x = t.x;
					light.y = t.y;
					light.z = t.z;
					light.lightColor = new Color(t.lightColor.x,t.lightColor.y,t.lightColor.z, 0f);
					light.range = t.range;

					lightSpatialHash.AddLight(light);
				}
			}
		}

		for(int i = 0; i < static_entities.size; i++)
		{
			Entity e = static_entities.get(i);
			updateLight(e);
		}

		// light some entities
		for(Entity e : entities) {
			e.updateLight(this);
		}
		for(Entity e : non_collidable_entities) {
			e.updateLight(this);
		}
		for(Entity e : static_entities) {
			e.updateLight(this);
		}
	}

	public float calculateLightColorAt(float posx, float posy, float posz, Vector3 normal, Color c)
	{
		Color t = calculateLightColorAt(posx, posz, posy, c);
		if(t == null) return Color.BLACK.toFloatBits();
		return t.toFloatBits();
	}

	private static Vector3 t_light_Calc_pos = new Vector3();
	public Color calculateLightColorAt(float x, float y, float z, Color c)
	{
		Vector3 pos = t_light_Calc_pos.set(x,y,z);
		c.set(ambientColor);

		// light markers
		for(int i = 0; i < editorMarkers.size; i++) {
			if(editorMarkers.get(i).type == Markers.torch) {
				EditorMarker t = editorMarkers.get(i);
				if(canSee((float)t.x + 0.5f, (float)t.y + 0.5f, pos.x, pos.y) ) {
					c.add(attenuateLightColor(pos.x,pos.y,0.5f, (float)t.x + 0.5f, (float)t.y + 0.5f, 0.5f, 3f, CachePools.getColor(1f, 0.8f, 0.4f, 1f)));
				}
			}
		}

		// light emitting entities
		for(int i = 0; i < entities.size; i++)
		{
			Entity e = entities.get(i);
			if(e instanceof Light && e.isActive)
			{
				Light t = (Light)e;
				if(Math.abs(x - t.x) <= t.range && Math.abs(y - t.y) <= t.range) {
					if(canSee(t.x, t.y, pos.x, pos.y) ) {
						if(t.invertLight)
							c.sub(attenuateLightColor(pos.x,pos.y,pos.z, t.x, t.y, t.z, t.range, t.getColor()));
						else
							c.add(attenuateLightColor(pos.x,pos.y,pos.z, t.x, t.y, t.z, t.range, t.getColor()));
					}
				}
			}
		}

		for(int i = 0; i < non_collidable_entities.size; i++)
		{
			Entity e = non_collidable_entities.get(i);
			if(e instanceof Light && e.isActive)
			{
				Light t = (Light)e;

				if(Math.abs(x - t.x) <= t.range && Math.abs(y - t.y) <= t.range) {

					if(canSee(t.x, t.y, pos.x, pos.y) ) {
						if(t.invertLight)
							c.sub(attenuateLightColor(pos.x,pos.y,pos.z, t.x, t.y, t.z, t.range, t.getColor()));
						else
							c.add(attenuateLightColor(pos.x,pos.y,pos.z, t.x, t.y, t.z, t.range, t.getColor()));
					}
				}
			}
		}

		for(int i = 0; i < static_entities.size; i++)
		{
			Entity e = static_entities.get(i);
			if(e instanceof Light && e.isActive)
			{
				Light t = (Light)e;

				if(Math.abs(x - t.x) <= t.range && Math.abs(y - t.y) <= t.range) {

					if(canSee(t.x, t.y, pos.x, pos.y) ) {
						if(t.invertLight)
							c.sub(attenuateLightColor(pos.x,pos.y,pos.z, t.x, t.y, t.z, t.range, t.getColor()));
						else
							c.add(attenuateLightColor(pos.x,pos.y,pos.z, t.x, t.y, t.z, t.range, t.getColor()));
					}
				}
			}
		}

		// some tiles should emit light. do that!
		for(int lx = 0; lx < width; lx++) {
			for(int ly = 0; ly < height; ly++) {
				Tile t = getTile(lx,ly);
				Color lightColor = TileManager.instance.getLightColor(t);
				if(lightColor != null) {
					float range = 1.6f;
					if(Math.abs(x - lx + 0.5f) <= range && Math.abs(y - ly + 0.5f) <= range)
						c.add(attenuateLightColor(pos.x,pos.y,pos.z, lx + 0.5f, ly + 0.5f, t.floorHeight, range, lightColor));
				}
				if(t.skyCeiling()) {
					float range = 1.6f;
					if(Math.abs(x - lx + 0.5f) <= range && Math.abs(y - ly + 0.5f) <= range)
						c.add(attenuateLightColor(pos.x,pos.y,0, lx + 0.5f, ly + 0.5f, 0, range, skyLightColor));
				}
			}
		}

		return c;
	}

	Color ambientTileLighting = new Color();
	Color tempLightColor = new Color();
	public Color getAmbientTileLighting(float x, float y, float z) {
		ambientTileLighting.set(Color.BLACK);
		for (int xx = -1; xx <= 1; xx += 1) {
			for (int yy = -1; yy <= 1; yy += 1) {
				int xTile = xx + (int) x;
				int yTile = yy + (int) y;

				Tile t = getTile(xTile, yTile);

				if (t != null) {
					Color l = TileManager.instance.getLightColor(t);
					if (l != null) {
						tempLightColor.set(0, 0, 0, 1f);
						tempLightColor = attenuateAreaLightColor(x, y, z, xTile + 0.5f, yTile + 0.5f, t.floorHeight + 0.5f, 1.75f, l, tempLightColor);
						ambientTileLighting.add(tempLightColor);
					}
				}
			}
		}
		return ambientTileLighting;
	}

	protected transient Color t_getLightColorAtCalcColor = new Color();
	public Color getLightColorAt(float x, float y, float z, Vector3 normal, Color c) {
		if(!GameManager.renderer.enableLighting) return c.set(Color.WHITE);

		c.set(ambientColor);

		// sky lights get a bit of faked ambient occlusion
		if(Options.instance.graphicsDetailLevel >= 2) {
			Tile cur = getTile((int) x, (int) y);
			if (cur.skyCeiling() || Options.instance.graphicsDetailLevel >= 3) {
				for (int xx = -1; xx <= 1; xx++) {
					for (int yy = -1; yy <= 1; yy++) {
						float worldX = x + xx * 0.75f;
						float worldY = y + yy * 0.75f;
						Tile t = getTile((int) worldX, (int) worldY);
						if (t.skyCeiling() && t.getFloorHeight(worldX, worldY) - 0.25f <= z) {
							c.add(skyLightColor.r * 0.125f, skyLightColor.g * 0.125f, skyLightColor.b * 0.125f, 0f);
						}
					}
				}
			}
		}
		else {
			Tile cur = getTile((int) x, (int) y);
			if (cur.skyCeiling()) {
				c.add(skyLightColor.r, skyLightColor.g, skyLightColor.b, 0f);
			}
		}

		try {
			c.add(getAmbientTileLighting(x, y, z));
		}
		catch (Exception ex) {
			Gdx.app.log("getAmbientTileLighting", ex.getMessage());
		}

		Array<Light> lights = lightSpatialHash.getLightsAt(x, y);
		if(lights != null) {
			for (int i = 0; i < lights.size; i++) {
				Light l = lights.get(i);

				Color cached = l.getCachedLightColor(x, y, z);
				if(cached != null) {
					if(!l.invertLight)
						c.add(cached);
					else
						c.sub(cached);
				}
				else {
					float shadowMul = 1f;

					if (l.shadowTiles) {
						shadowMul = l.canSeeHowMuch(this, x, y, z);
						if (shadowMul != 1f && shadowMul != 0f) {
							float sm = shadowMul * l.range;
							sm = (l.range) - sm;
							sm *= 0.3f;
							shadowMul = Math.min(1, Math.max(0, 1 - sm));
							shadowMul *= 0.5f;
						}
					}

					if (shadowMul != 0f) {
						if (l.lightFullHeight || Options.instance.graphicsDetailLevel == 1) {
							Color lightColor = attenuateAreaLightColor(x, y, z, l.x, l.y, l.z, l.range, l.getColor(), t_getLightColorAtCalcColor).mul(shadowMul);
							if (l.invertLight)
								c.sub(lightColor);
							else
								c.add(lightColor);

							//l.cacheLightColor(x, y, z, lightColor);
						} else {
							Color lightColor = attenuateLightColor(x, y, z, l.x, l.y, l.z, l.range, l.getColor()).mul(shadowMul);
							if (l.invertLight)
								c.sub(lightColor);
							else
								c.add(lightColor);

							//l.cacheLightColor(x, y, z, lightColor);
						}
					}


				}
			}
		}

		c.a = 1f;

		return c;
	}

	public Color attenuateAreaLightColor(float x, float y, float z, float x2, float y2, float z2, float range, Color lcolor, Color toReturn) {
		Color c = toReturn;
		c.a = 0;

		float xd = (float)Math.pow(x - x2, 2);
		float yd = (float)Math.pow(y - y2, 2);
		float zd = (float)Math.pow(z - z2, 2);
		float dist = GlRenderer.FastSqrt(xd + yd + zd * 0.02f);

		if(dist < range)
		{
			short lum = (short)(255 - (dist / range) * 255);
			float lmod = lum / 255.0f;
			if(lmod > 1) lmod = 1;

			// light falloff (n^2)
			lum *= lmod;

			if(lum > 255) lum = 255;

			float b = lum / 255.0f;
			c.set(b * lcolor.r, b * lcolor.g, b * lcolor.b, b * lcolor.a);
		}

		return c;
	}

	public static Color t_attenuateLightCalcColor = new Color();
	public Color attenuateLightColor(float x, float y, float z, float x2, float y2, float z2, float range, Color lcolor) {
		Color c = t_attenuateLightCalcColor.set(0, 0, 0, 0);

		float xd = (float)Math.pow(x - x2, 2);
		float yd = (float)Math.pow(y - y2, 2);
		float zd = (float)Math.pow(z - z2, 2);
		float dist = GlRenderer.FastSqrt(xd + yd + zd);

		//dist /= 1.3f;

		if(dist < range)
		{
			short lum = (short)(255 - (dist / range) * 255);
			float lmod = lum / 255.0f;
			if(lmod > 1) lmod = 1;

			// light falloff (n^2)
			lum *= lmod;
			lum *= 2;	// brighten things up

			if(lum > 255) lum = 255;

			float b = lum / 255.0f;
			c.set(b * lcolor.r, b * lcolor.g, b * lcolor.b, b * lcolor.a);
		}

		return c;
	}

	public boolean isFree(float x, float y, float z, Vector3 collision, float stepheight, boolean floating, Collision hitLoc)
	{
		//MetricsCore.count("levelCollisionCheck");

		stepheight -= 0.5;

		// Check each of the four corners (-1 -1, -1 1, 1 -1, 1 1)
		for(int x_corner = -1; x_corner <= 1; x_corner += 2) {
			for(int y_corner = -1; y_corner <= 1; y_corner += 2) {
				float xx = (x_corner * collision.x) + x;
				float yy = (y_corner * collision.y) + y;

				int xFloored = (int)Math.floor(xx);
				int yFloored = (int)Math.floor(yy);

				Tile t = getTile(xFloored, yFloored);
				if(t.blockMotion) // tile is completely solid
				{
					if(hitLoc != null) hitLoc.set(xFloored, yFloored, CollisionType.solidTile);
					return false;
				}
				else if(t.getFloorHeight(xx,yy) - z > stepheight) // can't step up to next height
				{
					if(hitLoc != null) hitLoc.set(xx, yy, CollisionType.floor);
					return false;
				}
				else if(t.getCeilHeight(xx,yy) + 0.5f < z + collision.z) // touching ceiling
				{
					if(hitLoc != null) hitLoc.set(xx, yy, CollisionType.ceiling);
					return false;
				}
				else if(t.getCeilHeight(xx,yy) - t.getFloorHeight(xx,yy) <= collision.z) // not enough room for entity to fit
				{
					if(hitLoc != null) hitLoc.set(xFloored, yFloored, CollisionType.solidTile);
					return false;
				}
				else if(t.collidesWithAngles(x - 0.5f, y - 0.5f, xx, yy, collision, xFloored, yFloored, hitLoc)) // touching an angled portion of the tile
				{
					if(hitLoc != null) hitLoc.set(xx, yy, CollisionType.angledWall);
					return false;
				}
			}
		}

		return true;
	}

	public Entity checkStandingRoomWithEntities(float x, float y, float z, Vector3 collision, Entity checking) {
		// Check each of the four corners (-1 -1, -1 1, 1 -1, 1 1)
		for(int x_corner = -1; x_corner <= 1; x_corner += 2) {
			for(int y_corner = -1; y_corner <= 1; y_corner += 2) {
				float xx = (x_corner * collision.x) + x;
				float yy = (y_corner * collision.y) + y;

				int xFloored = (int)Math.floor(xx);
				int yFloored = (int)Math.floor(yy);

				Tile t = getTile(xFloored, yFloored);
				float floorHeight = t.getFloorHeight(xx,yy) + 0.5f;

				if(floorHeight > z) {
					getEntitiesColliding(x, y, floorHeight, collision, checking);
					Entity colliding = checkEntityCollision(x, y, floorHeight, collision, checking);
					if (colliding != null) return colliding;
				}
			}
		}

		return null;
	}

	public boolean collidesWithAngles(float x, float y, Vector3 collision, Entity e)
	{
		float xx0 = (float) (x - collision.x);
		float xx1 = (float) (x + collision.x);
		float yy0 = (float) (y - collision.y);
		float yy1 = (float) (y + collision.y);

		int x0 = (int)Math.floor(xx0);
		int x1 = (int)Math.floor(xx1);
		int y0 = (int)Math.floor(yy0);
		int y1 = (int)Math.floor(yy1);

		Tile t0 = getTile(x0, y0);
		Tile t1 = getTile(x1, y0);
		Tile t2 = getTile(x0, y1);
		Tile t3 = getTile(x1, y1);

		boolean didHit = false;
		didHit = didHit || t0.checkAngledWallCollision(x, y, xx0, yy0, x0, y0, e);
		didHit = didHit || t1.checkAngledWallCollision(x, y, xx1, yy0, x1, y0, e);
		didHit = didHit || t2.checkAngledWallCollision(x, y, xx0, yy1, x0, y1, e);
		didHit = didHit || t3.checkAngledWallCollision(x, y, xx1, yy1, x1, y1, e);

		return didHit;
	}

	public float maxFloorHeight(float x, float y, float z, float width)
	{

		float xx0 = (x - width);
		float xx1 = (x + width);
		float yy0 = (y - width);
		float yy1 = (y + width);

		int x0 = (int)(Math.floor(xx0));
		int x1 = (int)(Math.floor(xx1));
		int y0 = (int)(Math.floor(yy0));
		int y1 = (int)(Math.floor(yy1));

		float height = getTile(x0, y0).getFloorHeight(xx0,yy0);
		height = Math.max(getTile(x1, y0).getFloorHeight(xx1,yy0), height);
		height = Math.max(getTile(x0, y1).getFloorHeight(xx0,yy1), height);
		height = Math.max(getTile(x1, y1).getFloorHeight(xx1,yy1), height);

		return height;
	}

	public Vector3 getSlope(float x, float y, float z, float width) {
		float xx0 = (x - width);
		float xx1 = (x + width);
		float yy0 = (y - width);
		float yy1 = (y + width);

		int x0 = (int)(Math.floor(xx0));
		int x1 = (int)(Math.floor(xx1));
		int y0 = (int)(Math.floor(yy0));
		int y1 = (int)(Math.floor(yy1));

		float height1 = getTile(x0, y0).getFloorHeight(xx0,yy0);
		float height2 = getTile(x1, y0).getFloorHeight(xx1,yy0);
		float height3 = getTile(x0, y1).getFloorHeight(xx0,yy1);
		float height4 = getTile(x1, y1).getFloorHeight(xx1,yy1);

		float maxHeight = height1;
		maxHeight = Math.max(height2, maxHeight);
		maxHeight = Math.max(height3, maxHeight);
		maxHeight = Math.max(height4, maxHeight);

		Vector3 ret = CachePools.getVector3();
		if(maxHeight == height1) {
			getTile(x0,y0).getFloorNormal(xx0, yy0, ret);
		}
		else if(maxHeight == height2) {
			getTile(x1,y0).getFloorNormal(xx1, yy0, ret);
		}
		else if(maxHeight == height3) {
			getTile(x0,y1).getFloorNormal(xx0, yy1, ret);
		}
		else {
			getTile(x1,y1).getFloorNormal(xx1, yy1, ret);
		}

		return ret;
	}

	public float minCeilHeight(float x, float y, float z, float width)
	{
		int x0 = (int)(Math.floor(x - width));
		int x1 = (int)(Math.floor(x + width));
		int y0 = (int)(Math.floor(y - width));
		int y1 = (int)(Math.floor(y + width));

		Tile t0 = getTile(x0, y0);
		Tile t1 = getTile(x1, y0);
		Tile t2 = getTile(x0, y1);
		Tile t3 = getTile(x1, y1);

		float height = t0.getCeilingHeight();
		height = Math.min(t1.getCeilingHeight(), height);
		height = Math.min(t2.getCeilingHeight(), height);
		height = Math.min(t3.getCeilingHeight(), height);

		return height;
	}

	public Entity checkEntityCollision(float x, float y, float z, float width)
	{
		return checkEntityCollision(x,y,z,width,width,100,null);
	}

	public Entity checkEntityCollision(float x, float y, float z, float width, float height)
	{
		return checkEntityCollision(x,y,z,width,width,height,null);
	}

	public Entity checkEntityCollision(float x, float y, float z, Vector3 collision, Entity checking) {
		return checkEntityCollision(x, y, z, collision.x, collision.y, collision.z, checking);
	}

	public Entity checkEntityCollision(float x, float y, float z, Vector3 collision, Entity checking, Entity ignore) {
		return checkEntityCollision(x, y, z, collision.x, collision.y, collision.z, checking, ignore);
	}

	public Entity getHighestEntityCollision(float x, float y, float z, Vector3 collision, Entity checking) {
		Array<Entity> colliding = getEntitiesColliding(x,y,z,checking);

		Entity highest = null;
		for(int i = 0; i < colliding.size; i++) {
			if(highest == null) highest = colliding.get(i);
			else {
				Entity cur = colliding.get(i);
				if(highest.z < cur.z) highest = cur;
			}
		}

		return highest;
	}

	public Array<Entity> getEntitiesEncroaching(Entity checking) {
		collisionCache.clear();
		if(checking == null) return collisionCache;

		//GameManager.renderer.visualizeCollisionCheck(checking.x,checking.y,checking.z, new Vector3(checking.collision.x, checking.collision.y, checking.collision.z));

		Array<Entity> toCheck = spatialhash.getEntitiesAt(checking.x, checking.y, Math.max(checking.collision.x, checking.collision.y));
		toCheck.addAll(staticSpatialhash.getEntitiesAt(checking.x, checking.y, Math.max(checking.collision.x, checking.collision.y)));

		for(int i = 0; i < toCheck.size; i++) {
			Entity e = toCheck.get(i);
			if(e != checking)
			{
				//MetricsCore.count("entityCollisionCheck");

				// simple AABB test
				if(checking.x > e.x - e.collision.x - checking.collision.x) {
					if(checking.x < e.x + e.collision.x + checking.collision.x) {
						if(checking.y > e.y - e.collision.y - checking.collision.y) {
							if(checking.y < e.y + e.collision.y + checking.collision.y) {
								if(checking.z > e.z - checking.collision.z && checking.z < e.z + e.collision.z) {
									collisionCache.add(e);
								}
							}
						}
					}
				}
			}
		}

		return collisionCache;
	}

	public Array<Entity> getEntitiesEncroaching2d(float x, float y, float collisionX, float collisionY, Entity checking) {
		collisionCache.clear();
		if(checking == null) return collisionCache;

		Array<Entity> toCheck = spatialhash.getEntitiesAt(x, y, Math.max(collisionX, collisionY));
		toCheck.addAll(staticSpatialhash.getEntitiesAt(x, y, Math.max(collisionX, collisionY)));

		for(int i = 0; i < toCheck.size; i++) {
			Entity e = toCheck.get(i);
			if(e != checking)
			{
				// simple AABB test
				if(x > e.x - e.collision.x - collisionX) {
					if(x < e.x + e.collision.x + collisionX) {
						if(y > e.y - e.collision.y - collisionY) {
							if(y < e.y + e.collision.y + collisionY) {
								collisionCache.add(e);
							}
						}
					}
				}
			}
		}

		return collisionCache;
	}

	// check if two entities are touching
	public boolean entitiesAreEncroaching(Entity checking, Entity e) {
		if (e != checking) {
			//MetricsCore.count("entityCollisionCheck");

			// simple AABB test
			if (checking.x > e.x - e.collision.x - checking.collision.x) {
				if (checking.x < e.x + e.collision.x + checking.collision.x) {
					if (checking.y > e.y - e.collision.y - checking.collision.y) {
						if (checking.y < e.y + e.collision.y + checking.collision.y) {
							if (checking.z > e.z - checking.collision.z && checking.z < e.z + e.collision.z) {
								return true;
							}
						}
					}
				}
			}
		}

		return false;
	}

	// return a list of all the entities colliding with the given one
	public Array<Entity> getEntitiesColliding(Entity checking) {
		collisionCache.clear();
		if(checking == null) return collisionCache;

		//GameManager.renderer.visualizeCollisionCheck(checking.x,checking.y,checking.z, new Vector3(checking.collision.x, checking.collision.y, checking.collision.z));

		Array<Entity> toCheck = spatialhash.getEntitiesAt(checking.x, checking.y, Math.max(checking.collision.x, checking.collision.y));
		toCheck.addAll(staticSpatialhash.getEntitiesAt(checking.x, checking.y, Math.max(checking.collision.x, checking.collision.y)));

		for(int i = 0; i < toCheck.size; i++) {
			Entity e = toCheck.get(i);
			if(e.isSolid && e != checking && e.isActive)
			{
				if(checking.ignorePlayerCollision && Game.instance.player != null && e == Game.instance.player) continue;
				if(e.ignorePlayerCollision && Game.instance.player != null && checking == Game.instance.player) continue;
				if(e.isDynamic && checking.collidesWith == CollidesWith.staticOnly) continue;

				//MetricsCore.count("entityCollisionCheck");

				// simple AABB test
				if(checking.x > e.x - e.collision.x - checking.collision.x) {
					if(checking.x < e.x + e.collision.x + checking.collision.x) {
						if(checking.y > e.y - e.collision.y - checking.collision.y) {
							if(checking.y < e.y + e.collision.y + checking.collision.y) {
								if(checking.z > e.z - checking.collision.z && checking.z < e.z + e.collision.z) {
									collisionCache.add(e);
								}
							}
						}
					}
				}
			}
		}

		return collisionCache;
	}

	// is this space free of the world and entities?
	public boolean collidesWorldOrEntities(float x, float y, float z, Vector3 collision, Entity checking) {
		boolean isFreeSoFar = isFree(x, y, z, collision, checking.stepHeight, checking.floating, null);
		if(isFreeSoFar) {
			isFreeSoFar = checkEntityCollision(x, y, z, collision, checking) == null;
		}
		return isFreeSoFar;
	}

	// return a list of all the entities colliding with the given one
	public Array<Entity> getEntitiesColliding(float x, float y, float z, Entity checking) {
		collisionCache.clear();
		if(checking == null) return collisionCache;

		//GameManager.renderer.visualizeCollisionCheck(checking.x,checking.y,checking.z, new Vector3(checking.collision.x, checking.collision.y, checking.collision.z));

		Array<Entity> toCheck = spatialhash.getEntitiesAt(x, y, Math.max(checking.collision.x, checking.collision.y));
		toCheck.addAll(staticSpatialhash.getEntitiesAt(x, y, Math.max(checking.collision.x, checking.collision.y)));

		for(int i = 0; i < toCheck.size; i++) {
			Entity e = toCheck.get(i);
			if(e.isSolid && e != checking && e.isActive)
			{
				if(checking.ignorePlayerCollision && Game.instance.player != null && e == Game.instance.player) continue;
				else if(e.ignorePlayerCollision && Game.instance.player != null && checking == Game.instance.player) continue;
				else if(e.isDynamic && checking.collidesWith == CollidesWith.staticOnly) continue;
				else if(e.collidesWith == CollidesWith.staticOnly && checking.isDynamic) continue;
				else if(e.collidesWith == CollidesWith.nonActors && checking instanceof Actor) continue;
				else if(e.collidesWith == CollidesWith.actorsOnly && !(checking instanceof Actor)) continue;
				else if(checking.collidesWith == CollidesWith.nonActors && e instanceof Actor) continue;
				else if(checking.collidesWith == CollidesWith.actorsOnly && !(e instanceof Actor)) continue;

				// simple AABB test
				if(x > e.x - e.collision.x - checking.collision.x) {
					if(x < e.x + e.collision.x + checking.collision.x) {
						if(y > e.y - e.collision.y - checking.collision.y) {
							if(y < e.y + e.collision.y + checking.collision.y) {
								if(z > e.z - checking.collision.z && z < e.z + e.collision.z) {
									collisionCache.add(e);
								}
							}
						}
					}
				}
			}
		}

		return collisionCache;
	}

    // return a list of all the entities colliding with the given one
    public Array<Entity> getEntitiesColliding(float x, float y, float z, Vector3 collision, Entity checking) {
        collisionCache.clear();
        if(checking == null) return collisionCache;

        //GameManager.renderer.visualizeCollisionCheck(checking.x,checking.y,checking.z, new Vector3(checking.collision.x, checking.collision.y, checking.collision.z));

        Array<Entity> toCheck = spatialhash.getEntitiesAt(x, y, Math.max(collision.x, collision.y));
        toCheck.addAll(staticSpatialhash.getEntitiesAt(x, y, Math.max(collision.x, collision.y)));

        for(int i = 0; i < toCheck.size; i++) {
            Entity e = toCheck.get(i);
            if(e.isSolid && e != checking && e.isActive)
            {
                if(checking.ignorePlayerCollision && Game.instance.player != null && e == Game.instance.player) continue;
                else if(e.ignorePlayerCollision && Game.instance.player != null && checking == Game.instance.player) continue;
                else if(e.isDynamic && checking.collidesWith == CollidesWith.staticOnly) continue;
                else if(e.collidesWith == CollidesWith.staticOnly && checking.isDynamic) continue;
                else if(e.collidesWith == CollidesWith.nonActors && checking instanceof Actor) continue;
                else if(e.collidesWith == CollidesWith.actorsOnly && !(checking instanceof Actor)) continue;
                else if(checking.collidesWith == CollidesWith.nonActors && e instanceof Actor) continue;
                else if(checking.collidesWith == CollidesWith.actorsOnly && !(e instanceof Actor)) continue;

                //MetricsCore.count("entityCollisionCheck");

                // simple AABB test
                if(x > e.x - e.collision.x - collision.x) {
                    if(x < e.x + e.collision.x + collision.x) {
                        if(y > e.y - e.collision.y - collision.y) {
                            if(y < e.y + e.collision.y + collision.y) {
                                if(z > e.z - collision.z && z < e.z + e.collision.z) {
                                    collisionCache.add(e);
                                }
                            }
                        }
                    }
                }
            }
        }

        return collisionCache;
    }

	// returns the first entity found colliding
	public Entity checkEntityCollision(float x, float y, float z, float widthX, float widthY, float height, Entity checking)
	{
		Array<Entity> toCheck = spatialhash.getEntitiesAt(x, y, Math.max(widthX, widthY));
		toCheck.addAll(staticSpatialhash.getEntitiesAt(x, y, Math.max(widthX, widthY)));

		//GameManager.renderer.visualizeCollisionCheck(x,y,z, new Vector3(widthX, widthY, height));

		for(int i = 0; i < toCheck.size; i++)
		{
			Entity e = toCheck.get(i);
			if(e.isSolid && e != checking && e.isActive)
			{
				if(checking != null && checking.ignorePlayerCollision && Game.instance.player != null && e == Game.instance.player) continue;
				else if(checking != null && e.ignorePlayerCollision && Game.instance.player != null && checking == Game.instance.player) continue;
				else if(checking != null && e.isDynamic && checking.collidesWith == CollidesWith.staticOnly) continue;
				else if(checking != null && e.collidesWith == CollidesWith.staticOnly && checking.isDynamic) continue;
				else if(checking != null && e.collidesWith == CollidesWith.nonActors && checking instanceof Actor) continue;
				else if(checking != null && e.collidesWith == CollidesWith.actorsOnly && !(checking instanceof Actor)) continue;
				else if(checking != null && checking.collidesWith == CollidesWith.nonActors && e instanceof Actor) continue;
				else if(checking != null && checking.collidesWith == CollidesWith.actorsOnly && !(e instanceof Actor)) continue;

				//MetricsCore.count("entityCollisionCheck");

				// simple AABB test
				if(x > e.x - e.collision.x - widthX) {
					if(x < e.x + e.collision.x + widthX) {
						if(y > e.y - e.collision.y - widthY) {
							if(y < e.y + e.collision.y + widthY) {
								if(z > e.z - height && z < e.z + e.collision.z) {
									return e;
								}
							}
						}
					}
				}
			}
		}

		return null;
	}

	public Entity checkEntityCollision(float x, float y, float z, float widthX, float widthY, float height, Entity checking, Entity ignore)
	{
		Array<Entity> toCheck = spatialhash.getEntitiesAt(x, y, Math.max(widthX, widthY));
		toCheck.addAll(staticSpatialhash.getEntitiesAt(x, y, Math.max(widthX, widthY)));

		//GameManager.renderer.visualizeCollisionCheck(x,y,z, new Vector3(widthX, widthY, height));

		for(int i = 0; i < toCheck.size; i++)
		{
			Entity e = toCheck.get(i);
			if(e.isSolid && e != checking && e.isActive && e != ignore)
			{
				if(checking != null && checking.ignorePlayerCollision && Game.instance.player != null && e == Game.instance.player) continue;
				else if(checking != null && e.ignorePlayerCollision && Game.instance.player != null && checking == Game.instance.player) continue;
				else if(checking != null && e.isDynamic && checking.collidesWith == CollidesWith.staticOnly) continue;
				else if(checking != null && e.collidesWith == CollidesWith.staticOnly && checking.isDynamic) continue;
				else if(checking != null && e.collidesWith == CollidesWith.nonActors && checking instanceof Actor) continue;
				else if(checking != null && e.collidesWith == CollidesWith.actorsOnly && !(checking instanceof Actor)) continue;
				else if(checking != null && checking.collidesWith == CollidesWith.nonActors && e instanceof Actor) continue;
				else if(checking != null && checking.collidesWith == CollidesWith.actorsOnly && !(e instanceof Actor)) continue;

				//MetricsCore.count("entityCollisionCheck");

				// simple AABB test
				if(x > e.x - e.collision.x - widthX) {
					if(x < e.x + e.collision.x + widthX) {
						if(y > e.y - e.collision.y - widthY) {
							if(y < e.y + e.collision.y + widthY) {
								if(z > e.z - height && z < e.z + e.collision.z) {
									return e;
								}
							}
						}
					}
				}
			}
		}

		return null;
	}

	public Entity checkItemCollision(float x, float y, float width)
	{
		for(int i = 0; i < entities.size; i++)
		{
			Entity e = entities.get(i);
			if(e.type == EntityType.item || e instanceof Stairs)
			{
				float xx1 = e.x - e.collision.x - 0.5f - width;
				float xx2 = e.x + e.collision.x  - 0.5f + width;
				float yy1 = e.y - e.collision.y  - 0.5f - width;
				float yy2 = e.y + e.collision.y  - 0.5f + width;

				// simple AABB test
				if(x > xx1 && x < xx2 && y > yy1 && y < yy2) return e;
			}
		}

		return null;
	}

	public boolean checkPlayerCollision(float x, float y, float z, float width, float height)
	{
		Player player = GameManager.getGame().player;
		float xx1 = player.x - player.collision.x - width;
		float xx2 = player.x + player.collision.x + width;
		float yy1 = player.y - player.collision.y - width;
		float yy2 = player.y + player.collision.y + width;

		// simple AABB test
		if(x > xx1 && x < xx2 && y > yy1 && y < yy2)
		{
			if(Math.abs(z - player.z) < height)
				return true;
		}
		return false;
	}

	public void setPlayer(Player player)
	{
		player.levelName = levelName;
	}

	public boolean canSee(float x, float y, float x2, float y2) {
	    float dx = Math.abs(x - x2);
	    float dy = Math.abs(y - y2);
	    float s = 0.99f/(dx>dy?dx:dy);

	    float t = 0.01f;

	    while(t < 1f) {
	        dx = (((1.0f - t)*x + t*x2));
	        dy = (((1.0f - t)*y + t*y2));
	        Tile c = getTile((int)dx, (int)dy);
	        if (c == null || c.renderSolid || c.hide || c.tileSpaceType == TileSpaceType.SOLID || c.ceilHeight <= c.floorHeight) return false;

	        if(c.tileSpaceType != TileSpaceType.EMPTY) {
	        	if(c.pointBehindAngle(dx, dy)) return false;
	        	t += s * 0.25f;
	        }
	        else
	        	t += s * 0.5f;
	    }

	    return true;
	}

	public float canSeeHowMuch(float x, float y, float x2, float y2) {
		float dx = Math.abs(x - x2);
		float dy = Math.abs(y - y2);
		float s = 0.99f/(dx>dy?dx:dy);

		float t = 0.01f;

		while(t < 1f) {
			dx = (((1.0f - t)*x + t*x2));
			dy = (((1.0f - t)*y + t*y2));
			Tile c = getTile((int)dx, (int)dy);
			if (c == null || c.renderSolid || c.hide || c.tileSpaceType == TileSpaceType.SOLID || c.ceilHeight <= c.floorHeight) return t;

			if(c.tileSpaceType != TileSpaceType.EMPTY) {
				if(c.pointBehindAngle(dx, dy)) return t;
				t += s * 0.1f;
			}
			else
				t += s * 0.1f;
		}

		return 1f;
	}

	public boolean canSafelySee(float x, float y, float x2, float y2) {
	    float dx = Math.abs(x - x2);
	    float dy = Math.abs(y - y2);
	    float s = 0.99f/(dx>dy?dx:dy);

	    float t = 0.01f;

        Float lastTileHeight = null;

	    while(t < 1f) {
	        dx = (((1.0f - t)*x + t*x2));
	        dy = (((1.0f - t)*y + t*y2));
	        Tile c = getTile((int)dx, (int)dy);

            // avoid tiles that hurt
	        if (c == null || c.renderSolid || c.hide || c.data.hurts > 0) return false;

            // avoid long falls
            if(lastTileHeight != null) {
                if(Math.abs(c.floorHeight - lastTileHeight) > 3f) {
                	return false;
				}
            }
            lastTileHeight = c.floorHeight;

	        if(c.tileSpaceType != TileSpaceType.EMPTY) {
	        	if(c.pointBehindAngle(dx, dy)) return false;
	        	t += s * 0.25f;
	        }
	        else
	        	t += s * 0.5f;
	    }

	    return true;
	}

	public Array<Entity> getEntitiesAlongLine(float x, float y, float x2, float y2) {
		float dx = Math.abs(x - x2);
	    float dy = Math.abs(y - y2);
	    float s = 0.99f/(dx>dy?dx:dy);

	    float t = 0.0f;

	    Array<Entity> foundEntities = collisionCache;
	    foundEntities.clear();

	    while(t < 1f) {
	        dx = (((1.0f - t)*x + t*x2));
	        dy = (((1.0f - t)*y + t*y2));

	        // check the spatial hash for entities
	        Array<Entity> foundHere = spatialhash.getEntitiesAt(dx, dy, 1.5f);

	        for(Entity e : foundHere) {
	        	if(!foundEntities.contains(e, true)) foundEntities.add(e);
	        }

	        t += s;
	    }

	    return foundEntities;
	}

	private transient Vector3 t_canSeeStart = new Vector3();
	private transient Vector3 t_canSeeEnd = new Vector3();
	private transient Vector3 t_canSeeIntersection = new Vector3();
	public boolean canSeeIncludingDoors(float x, float y, float x2, float y2, float maxDistance) {

		if(Math.abs(x - x2) > maxDistance || Math.abs(y - y2) > maxDistance) return false;

	    boolean canSeeThroughLevel = canSee(x, y, x2, y2);
	    if(!canSeeThroughLevel) return false;

		t_canSeeStart.set(x,0.5f,y);
		t_canSeeEnd.set(x2,0.5f,y2);
		t_canSeeEnd.sub(t_canSeeStart);
		t_canSeeEnd.nor();

	    Ray r = calcRay.set(t_canSeeStart, t_canSeeEnd);
	    Array<Entity> possibles = getEntitiesAlongLine(x, y, x2, y2);

	    // find all the doors or movers, should block vision
	    for(Entity e : possibles) {
	    	if(e instanceof Door) {
	    		Door d = (Door)e;
	    		if(d.doorState == DoorState.CLOSED || d.doorState == DoorState.STUCK) {
					BoundingBox b = getAABB(t_intersectCheck1, e);
					b.min.y = 0;
					b.max.y = 1;

					if(Intersector.intersectRayBounds(r, b, t_canSeeIntersection)) {
						return false;
					}
	    		}
	    	} else if (e instanceof Mover) {
				Mover m = (Mover)e;
				if(m.isSolid) {
					BoundingBox b = getAABB(t_intersectCheck1, e);
					if(Intersector.intersectRayBounds(r, b, t_canSeeIntersection)) {
						return false;
					}
				}
			}
	    }

	    return true;
	}

	public boolean canSee3D(float x, float y, float z, float x2, float y2, float z2) {
	    float dx = Math.abs(x - x2);
	    float dy = Math.abs(y - y2);
	    float dz = Math.abs(z - z2);
	    float s = 0.99f/(dx>dy?dx:dy);

	    float t = 0.01f;

	    while(t < 1f) {
	        dx = (int)(((1.0f - t)*x + t*x2));
	        dy = (int)(((1.0f - t)*y + t*y2));
	        dz = (int)(((1.0f - t)*z + t*z2));
	        Tile c = getTile((int)dx, (int)dy);
	        if (c == null || c.renderSolid || c.hide) return false;
	        else if(dz < c.floorHeight || dz > c.ceilHeight) return false;
	        //return true;
	        t += s * 0.2f;
	    }

	    return true;
	}

	private Color lightmapTempColor = new Color();
	public Color GetLightmapAt(float posx, float posy, float posz)
	{
		Color t = getLightColorAt(posx, posz, posy, null, lightmapTempColor);
		if(t == null) return Color.BLACK;
		else return t;
	}

	public void spawnMonster()
	{
		Player player = Game.instance.player;

		Random r = new Random();
		int xPos = r.nextInt(width);
		int yPos = r.nextInt(height);

		Tile t = getTile(xPos,yPos);
		if(t != null && t.CanSpawnHere() && t.hasRoomFor(0.65f)) {

			// don't generate if the player is too close, or another entity is already nearby
			if(!spawnMonsters) return;
			if(!player.isHoldingOrb && canSee(xPos + 0.5f, yPos + 0.5f, player.x, player.y)) return;
			if(!player.isHoldingOrb && Math.abs(xPos - player.x) <= 4 && Math.abs(yPos - player.y) <= 4) return;
			else if(player.isHoldingOrb && Math.abs(xPos - player.x) <= 1 && Math.abs(yPos - player.y) <= 1) return;

			if(checkEntityCollision(xPos + 0.5f, yPos + 0.5f, 0, 0.5f) == null)
			{
				Monster m;
				int level = Game.instance.player.level;
				if(!player.isHoldingOrb) {
					m = Game.GetMonsterManager().GetRandomMonster(theme);
				}
				else {
					m = Game.GetMonsterManager().GetRandomMonster(DungeonTheme.UNDEAD.toString());
					level = 20;
				}

				if(m != null)
				{
					m.x = xPos + 0.5f;
					m.y = yPos + 0.5f;
					m.z = t.getFloorHeight() + 0.5f;
					m.Init(this, level);
					entities.add(m);
					monsterSpawnTimer = 0;

					if(player.isHoldingOrb) m.alerted = true;
				}
			}
		}
	}

	public int getMonsterCount() {
		int ct = 0;
		for(int i = 0; i < entities.size; i++)
		{
			if(entities.get(i) instanceof Monster) ct++;
		}
		return ct;
	}

	public void editorTick(Player p, float delta) {
		spatialhash.Flush();

		for(int i = 0; i < entities.size; i++)
		{
			Entity e = entities.get(i);
			if(e.isActive) spatialhash.AddEntity(e);
		}

		tickEntityList(delta, entities, true);
		tickEntityList(delta, non_collidable_entities, true);
		tickEntityList(delta, static_entities, true);
	}

	public void updateSpatialHash(Player player) {
		spatialhash.Flush();

		if(player != null)
			spatialhash.AddEntity(player);

		for(int i = 0; i < entities.size; i++)
		{
			Entity e = entities.get(i);
			if(e.isActive && e.isSolid) spatialhash.AddEntity(e);
			else if (e instanceof Item || e instanceof Door || e instanceof Stairs) spatialhash.AddEntity(e);
		}
	}

	public void addEntityToSpatialHash(Entity e) {
		if(e.isActive && e.isSolid) spatialhash.AddEntity(e);
		else if (e instanceof Item || e instanceof Door || e instanceof Stairs) spatialhash.AddEntity(e);
	}

	public void updateStaticSpatialHash() {
		staticSpatialhash.Flush();
		for(int i = 0; i < static_entities.size; i++)
		{
			Entity e = static_entities.get(i);
			if(e.isActive && e.isSolid) staticSpatialhash.AddEntity(e);
		}
	}

	public void addEntityToStaticSpatialHash(Entity e) {
		if(e.isActive && e.isSolid) staticSpatialhash.AddEntity(e);
	}

	public void tick(float delta) {
		Player player = Game.instance.player;

		if(mapIsDirty || lastPlayerTileX != (int)player.x || lastPlayerTileY != (int)player.y) {
			updateSeenTiles(player);
		}

		/* --- Update the spatial hash --- */
		updateSpatialHash(player);

		tickEntityList(delta, entities, false);
		tickEntityList(delta, non_collidable_entities, false);

		spawnMonstersIfNeeded(player, delta);

		if(!needsSaving) needsSaving = true;

		// some things only need to update when the player's tile changes
		lastPlayerTileX = (int)player.x;
		lastPlayerTileY = (int)player.y;
	}

	public void tickEntityList(float delta, Array<Entity> list, boolean inEditor) {

		// update everyone in the list
		Entity e = null;
		for(entity_index = 0; entity_index < list.size; entity_index++)
		{
			e = list.get(entity_index);

			if(!inEditor) {
				if(e.skipTick) {
					e.skipTick = false;
					continue;
				}

				e.tick(this, delta);
			}
			else {
				e.editorTick(this, delta);
			}

			if(!e.isActive) toDelete.add(e);
		}

		// remove the newly inactive items from the list
		for(entity_index = 0 ; entity_index < toDelete.size; entity_index++) {
			e = toDelete.get(entity_index);

			// clear this guy from some caches if needed
			if(e instanceof Particle) CachePools.freeParticle((Particle)e);
			e.onDispose();
			list.removeValue(e,true);
		}

		// clear the list for next time
		toDelete.clear();
	}

	private void spawnMonstersIfNeeded(Player player, float delta) {
		if(player.isHoldingOrb || spawnMonsters) {

			// monster spawn rate increases after the orb is picked up
			monsterSpawnTimer += 1 * delta;
			int monsterSpawnTime = 600;
			int monsterCount = 3;

			if(player.isHoldingOrb)	{
				monsterSpawnTime = 60;
				monsterCount = 15;
			}

			if(monsterSpawnTimer > monsterSpawnTime && getMonsterCount() < monsterCount)
				spawnMonster();
		}
	}

	protected void updateSeenTiles(Player player) {
		int startX = (int)player.x - 5;
		int startY = (int)player.y - 5;
		int endX = startX + 10;
		int endY = startY + 10;

		for(int x = startX; x < endX; x++) {
			for(int y = startY; y < endY; y++) {
				Tile t = getTile(x,y);

				if(t != null && t.IsFree() && !t.seen && t != Tile.solidWall) {
					t.seen = canSee(player.x, player.y, x + 0.5f, y + 0.5f);
					if(t.seen) {

						for(int xxx = -1; xxx <= 1; xxx++) {
							for(int yyy = -1; yyy <= 1; yyy++) {
								Tile near = getTile(xxx + x, yyy + y);
								if(near != null && !near.seen && (near.IsSolid() || near.floorAndCeilingAreSameHeight()))
                                    near.seen = true;
							}
						}

						dirtyMapTiles.add(new Vector2(x, y));
					}
				}
			}
		}
	}

	public void SpawnEntity(Entity e) {
		if(e.spawnChance < 1.0) {
			if(Game.rand.nextFloat() > e.spawnChance)
				return;
		}

		entities.add(e);
		addEntityToSpatialHash(e);
        e.updateDrawable();
        e.init(this, Source.SPAWNED);
	}

	public void SpawnNonCollidingEntity(Entity e) {
		non_collidable_entities.add(e);
	}

	public void SpawnStaticEntity(Entity e) {
		static_entities.add(e);
		addEntityToStaticSpatialHash(e);
	}

	public void addEntity(Entity e) {
		e.init(this, Source.LEVEL_START);

		if (!e.isActive) {
			return;
		}

		if(!e.isDynamic) {
			static_entities.add(e);
			addEntityToStaticSpatialHash(e);
		}
		else if(!e.isSolid) {
			non_collidable_entities.add(e);
		}
		else {
			entities.add(e);
			addEntityToSpatialHash(e);
		}
	}

	public void rotate90() {
		Tile[] tempArray = new Tile[width * height];
		for (int i = 0; i < width; ++i) {
			for (int j = 0; j < height; ++j) {
				Tile t = getTileOrNull(i,j);
				tempArray[(height - 1 - j) + i * width] = t;
				if(t != null) t.rotate90();
			}
		}

		tiles = tempArray;

		// 1.57079633 is 90 degrees in radians
		float s = (float)Math.sin(1.57079633f);
		float c = (float)Math.cos(1.57079633f);

		float midX = width / 2f;
		float midY = height / 2f;

		for(Entity e : entities) {
			if(e != null) {
				e.x -= midX;
				e.y -= midY;

				float xnew = e.x * c - e.y * s;
				float ynew = e.x * s + e.y * c;

				e.x = xnew + midX;
				e.y = ynew + midY;

				e.rotate90();
			}
		}

		for(EditorMarker m : editorMarkers) {
			if(m != null) {

				float origX = m.x + 0.5f - midX;
				float origY = m.y + 0.5f - midY;

				float xnew = origX * c - origY * s;
				float ynew = origX * s + origY * c;

				m.x = (int) (xnew + midX);
				m.y = (int) (ynew + midY);
				m.rot += 90;
			}
		}
	}

	public void paste(Level clip, int offsetx, int offsety) {
		for(int x = 0; x < clip.width; x++) {
			for(int y = 0; y < clip.height; y++) {
				setTile(x + offsetx,y + offsety, clip.getTileOrNull(x, y));
			}
		}

		for(Entity e : clip.entities) {
			if(e != null) {
				e.x += offsetx;
				e.y += offsety;
			}
		}
		entities.addAll(clip.entities);

		for(EditorMarker m : clip.editorMarkers) {
			if(m != null) {
				m.x += offsetx;
				m.y += offsety;
			}
		}
		editorMarkers.addAll(clip.editorMarkers);
	}

	// add a prefix to all entity IDs and trigger IDs
	public void makeEntityIdsUnique(String idPrefix) {
		for(int i = 0; i < entities.size; i++) {
			makeUniqueEntityId(idPrefix, entities.get(i));
		}
	}

	public static void makeUniqueEntityId(String idPrefix, Entity e) {
		e.makeEntityIdUnique(idPrefix);
	}

	public byte[] getTileBytes() {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutput out = null;

		byte[] bytes = null;
		try {
			try {
				out = new ObjectOutputStream(bos);
				out.writeObject(tiles);
			} catch (IOException e) { }

			bytes = bos.toByteArray();
		} finally {
			try {
				out.close();
				bos.close();
			} catch (IOException e) {}
		}

		return bytes;
	}

	public void crop(int selX, int selY, int selWidth, int selHeight) {
		Tile[] tempArray = new Tile[selWidth * selHeight];
		for (int x = 0; x < selWidth; x++) {
			for (int y = 0; y < selHeight; y++) {
				tempArray[x + y * selWidth] = getTile(x + selX,y + selY);
			}
		}

		Array<Entity> tempEntities = new Array<Entity>();
		for(Entity e : entities) {
			if(e != null) {
				if(e.x >= selX && e.x <= selX + selWidth && e.y >= selY && e.y <= selY + selHeight) {
					e.x -= selX;
					e.y -= selY;
					tempEntities.add(e);
				}
			}
		}

		Array<EditorMarker> tempMarkers = new Array<EditorMarker>();
		for(EditorMarker e : editorMarkers) {
			if(e != null) {
				if(e.x >= selX && e.x <= selX + selWidth && e.y >= selY && e.y <= selY + selHeight) {
					e.x -= selX;
					e.y -= selY;
					tempMarkers.add(e);
				}
			}
		}

		tiles = tempArray;
		width = selWidth;
		height = selHeight;

		entities = tempEntities;
		editorMarkers = tempMarkers;
	}

	public Vector3 getCornerLocation(int x, int y, int corner) {
		Tile t = getTileOrNull(x,y);

		Vector3 ret = null;
		if(t != null) {
			ret = CachePools.getVector3();
			if(corner == 0 || corner == 3 || corner == 4 || corner == 7) ret.x = x + 1;
			else ret.x = x;

			if(corner == 0 || corner == 1 || corner == 4 || corner == 5) ret.y = y;
			else ret.y = y + 1;

			if(corner == 0)
				ret.z = t.slopeNW;
			else if (corner == 1)
				ret.z = t.slopeNE;
			else if (corner == 2)
				ret.z = t.slopeSE;
			else if (corner == 3)
				ret.z = t.slopeSW;
			else if(corner == 4)
				ret.z = t.ceilSlopeNW;
			else if (corner == 5)
				ret.z = t.ceilSlopeNE;
			else if (corner == 6)
				ret.z = t.ceilSlopeSE;
			else if (corner == 7)
				ret.z = t.ceilSlopeSW;
		}

		return ret;
	}

	public Stairs spawnStairs(StairDirection direction, float xLoc, float yLoc, float zLoc) {

		Stairs stairs = null;
		if(genTheme != null && direction == StairDirection.down && genTheme.exitDown != null) {
			stairs = (Stairs)KryoSerializer.copyObject(genTheme.exitDown);
		}
		if(genTheme != null && direction == StairDirection.up && genTheme.exitUp != null) {
			stairs = (Stairs)KryoSerializer.copyObject(genTheme.exitUp);
		}
		if(stairs == null) {
			stairs = new Stairs(0, 0, 7, direction);
		}

		// set position
		stairs.direction = direction;
		stairs.x = xLoc + 0.5f;
		stairs.y = yLoc + 0.5f;
		stairs.z = zLoc;

		entities.add(stairs);

		// change tile texture!
		Tile atTile = this.getTileOrNull((int)xLoc, (int)yLoc);
		if(atTile != null && stairs.tileMaterial != null) {
			if(direction == StairDirection.down) {
				atTile.floorTex = stairs.tileMaterial.tex;
				atTile.floorTexAtlas = stairs.tileMaterial.texAtlas;
			}
			else {
				atTile.ceilTex = stairs.tileMaterial.tex;
				atTile.ceilTexAtlas = stairs.tileMaterial.texAtlas;
			}
		}

		return stairs;
	}

	// trigger an entity by id
	public void trigger(Entity instigator, String triggersId, String triggerValue) {
		if(triggersId == null || triggersId.equals("")) return;

		try {
			Array<Entity> matches = getEntitiesById(triggersId);
			for(Entity e : matches) {
				if(e != instigator) e.onTrigger(instigator, triggerValue);
			}
			matches.clear();
		}
		catch(Exception ex) {
			Gdx.app.log("DelverGame", "Error triggering: " + triggersId);
		}
	}

	// find entities by their ID
	public Array<Entity> getEntitiesById(String id) {
		Array<Entity> entitiesToFind = triggerCache;
		entitiesToFind.clear();
		if(id == null || id.equals("")) return entitiesToFind;

        String[] ids = id.split(",");
        for(int i = 0; i < ids.length; i++) {
            String lookForId = ids[i];
            for (Entity e : entities) {
                if (e.id != null && e.isActive && e.id.equals(lookForId)) entitiesToFind.add(e);
            }

            for (Entity e : static_entities) {
                if (e.id != null && e.isActive && e.id.equals(lookForId)) entitiesToFind.add(e);
            }

            for (Entity e : non_collidable_entities) {
                if (e.id != null && e.isActive && e.id.equals(lookForId)) entitiesToFind.add(e);
            }
        }

		return entitiesToFind;
	}

	// find entities with an id that ends with the search id
	public Array<Entity> getEntitiesLikeId(String id) {
		Array<Entity> entitiesToFind = triggerCache;
		entitiesToFind.clear();
		if(id == null || id.equals("")) return entitiesToFind;

		String[] ids = id.split(",");
		for(int i = 0; i < ids.length; i++) {
			String lookForId = ids[i];
			for (Entity e : entities) {
				if (e.id != null && e.isActive && e.id.endsWith(lookForId)) entitiesToFind.add(e);
			}

			for (Entity e : static_entities) {
				if (e.id != null && e.isActive && e.id.endsWith(lookForId)) entitiesToFind.add(e);
			}

			for (Entity e : non_collidable_entities) {
				if (e.id != null && e.isActive && e.id.endsWith(lookForId)) entitiesToFind.add(e);
			}
		}

		return entitiesToFind;
	}

	public Array<Entity> findEntities(Class typeOf, Vector2 position, float range, boolean dynamicEntities, boolean staticEntities, boolean nonCollidingEntities) {
		Array<Entity> toReturn = new Array<Entity>();

		if(dynamicEntities) {
			for(int i = 0; i < entities.size; i++) {
				Entity e = entities.get(i);
				if(e.getClass().equals(typeOf) && Math.abs(e.x - position.x) < range && Math.abs(e.y - position.y) < range) toReturn.add(e);
			}
		}

		if(staticEntities) {
			for(int i = 0; i < static_entities.size; i++) {
				Entity e = static_entities.get(i);
				if(e.getClass().equals(typeOf) && Math.abs(e.x - position.x) < range && Math.abs(e.y - position.y) < range) toReturn.add(e);
			}
		}

		if(nonCollidingEntities) {
			for(int i = 0; i < non_collidable_entities.size; i++) {
				Entity e = non_collidable_entities.get(i);
				if(e.getClass().equals(typeOf) && Math.abs(e.x - position.x) < range && Math.abs(e.y - position.y) < range) toReturn.add(e);
			}
		}

		return toReturn;
	}

	public Array<Entity> findEntities(Vector2 position, float range, boolean dynamicEntities, boolean staticEntities, boolean nonCollidingEntities) {
		Array<Entity> toReturn = new Array<Entity>();

		if(dynamicEntities) {
			for(int i = 0; i < entities.size; i++) {
				Entity e = entities.get(i);
				if(Math.abs(e.x - position.x) < range && Math.abs(e.y - position.y) < range) toReturn.add(e);
			}
		}

		if(staticEntities) {
			for(int i = 0; i < static_entities.size; i++) {
				Entity e = static_entities.get(i);
				if(Math.abs(e.x - position.x) < range && Math.abs(e.y - position.y) < range) toReturn.add(e);
			}
		}

		if(nonCollidingEntities) {
			for(int i = 0; i < non_collidable_entities.size; i++) {
				Entity e = non_collidable_entities.get(i);
				if(Math.abs(e.x - position.x) < range && Math.abs(e.y - position.y) < range) toReturn.add(e);
			}
		}

		return toReturn;
	}

	private void preSaveCleanup(Array<Entity> entities) {
		// Remove any entities that we should not save
		Array<Entity> toDelete = new Array<Entity>();
		if(entities != null) {
			for(Entity e : entities) {
				e.onDispose();
				if(!e.persists || !e.isActive) toDelete.add(e);
			}
		}
		for(int i = 0; i < toDelete.size; i++) {
			entities.removeValue(toDelete.get(i), true);
		}

		// Make sure tile materials persist
		for(int i = 0; i < tiles.length; i++) {
			if(tiles[i] == null)
				tileMaterials[i] = null;
			else
				tileMaterials[i] = tiles[i].materials;
		}
	}

	public void preSaveCleanup() {
		if(entities != null) preSaveCleanup(entities);
		if(non_collidable_entities != null) preSaveCleanup(non_collidable_entities);
		if(static_entities != null) preSaveCleanup(static_entities);
		if(entities != null) { for(Entity e : entities) { e.editorStopPreview(this); } }
	}

    public static boolean checkIfTextureAtlasesMatch(String atlasOne, String atlasTwo) {
        if(atlasOne == null) atlasOne = TextureAtlas.cachedRepeatingAtlases.firstKey();
        if(atlasTwo == null) atlasTwo = TextureAtlas.cachedRepeatingAtlases.firstKey();
        return atlasOne.equals(atlasTwo);
    }

	public void lockTilesAround(Vector2 location, int range) {
		for(int x = -range; x <= range; x++) {
			for(int y = -range; y <= range; y++) {
				Tile tile = getTileOrNull((int)location.x + x, (int)location.y + y);
				if(tile != null) {
					tile.lockTile();
				}
				else {
					// have to make a tile to lock it.
					Tile t = Tile.NewSolidTile();
					t.isLocked = true;

					setTile((int)location.x + x, (int)location.y + y, t);
				}
			}
		}
	}

	private void spawnChaseEncounter() {
		if(!spawnEncounterDuringChase)
			return;

		if(!spawnMonsters)
			return;

		Player p = Game.instance.player;
		if(p == null)
			return;

		if(!p.isHoldingOrb)
			return;

		float pickedX;
		float pickedY;

		if(up != null) {
			pickedX = up.x;
			pickedY = up.y;
		}
		else if(playerStartX != null && playerStartY != null) {
			pickedX = playerStartX;
			pickedY = playerStartY;
		}
		else {
			return;
		}

		Tile t = getTile((int)pickedX, (int)pickedY);

		// Make an encounter here!
		Entity m = EntityManager.instance.getEntity("Escape", "StairsUpEncounter");
		if(m != null) {
			m.x = pickedX;
			m.y = pickedY;
			m.z = t.getFloorHeight() + 0.5f;
			SpawnEntity(m);
		}

		spawnEncounterDuringChase = false;
	}

	public void initAudio() {

		Audio.playMusic(music, true);

		if(ambientSound != null && !Game.isMobile)
			Audio.playAmbientSound(ambientSound, Game.instance.level.ambientSoundVolume, 0.1f);
	}

	public void clear() {
		if (editorMarkers != null) editorMarkers.clear();
		if (entities != null) entities.clear();
		if (static_entities != null) static_entities.clear();
		if (non_collidable_entities != null) non_collidable_entities.clear();
	}
}
