package com.interrupt.dungeoneer.game;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.interrupt.dungeoneer.Audio;
import com.interrupt.dungeoneer.GameApplication;
import com.interrupt.dungeoneer.GameInput;
import com.interrupt.dungeoneer.GameManager;
import com.interrupt.dungeoneer.entities.Entity;
import com.interrupt.dungeoneer.entities.Item;
import com.interrupt.dungeoneer.entities.Player;
import com.interrupt.dungeoneer.entities.Stairs;
import com.interrupt.dungeoneer.entities.Stairs.StairDirection;
import com.interrupt.dungeoneer.entities.items.QuestItem;
import com.interrupt.dungeoneer.entities.items.Weapon;
import com.interrupt.dungeoneer.entities.triggers.TriggeredWarp;
import com.interrupt.dungeoneer.game.Level.Source;
import com.interrupt.dungeoneer.generator.SectionDefinition;
import com.interrupt.dungeoneer.gfx.DecalManager;
import com.interrupt.dungeoneer.gfx.animation.lerp3d.LerpedAnimationManager;
import com.interrupt.dungeoneer.input.ControllerState;
import com.interrupt.dungeoneer.input.GamepadManager;
import com.interrupt.dungeoneer.screens.GameScreen;
import com.interrupt.dungeoneer.serializers.KryoSerializer;
import com.interrupt.dungeoneer.ui.*;
import com.interrupt.dungeoneer.ui.Hud.DragAndDropResult;
import com.interrupt.managers.EntityManager;
import com.interrupt.managers.HUDManager;
import com.interrupt.managers.ItemManager;
import com.interrupt.managers.MonsterManager;
import com.interrupt.managers.StringManager;
import com.interrupt.utils.JsonUtil;
import com.interrupt.utils.Logger;
import com.interrupt.utils.OSUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Comparator;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Game {
	/** Engine version */
	public static String VERSION = "v1.4.0";

    /** The save game version, gets saved in the player */
    public static int SAVE_VERSION = 1;

	public float time = 0;
	public Player player;
	public GameInput input;
	public static GamepadManager gamepadManager;

	public boolean gameOver = false;

	public Level level;
	protected int levelNum;

	public static float messageTimer = 0;
	public static float messageScale = 1;
	public static Array<String> message = new Array<String>();
	public static Array<String> useMessage = new Array<String>();
	public static Color useMessageColor = Color.WHITE;

	public static Color flashColor = new Color(1f,0f,0f,1f);
	public static float flashTimer = 0;
	public static float flashLength = 0;

	public static Game instance;
	public static GameData gameData = null;
    public static ModManager modManager;
	public ItemManager itemManager;
	public MonsterManager monsterManager;
	public EntityManager entityManager;
    public static HUDManager hudManager;

	public static boolean isMobile = false;
	public static boolean isDebugMode = false;
	public static boolean drawDebugBoxes = false;
	public static boolean ignoreEscape = false;
	public static boolean inEditor = false;

	// UI stuff
	public static Tooltip tooltip = new Tooltip();
    public static Stage ui;

    public static Hud hud = null;

    public static CharacterScreen characterScreen = null;
    public static InventoryScreen inventoryScreen = null;

    private static float uiSize = 1;
    public static Item dragging = null;

    public enum MenuMode { Character, Inventory, Hidden }
    public MenuMode menuMode = MenuMode.Hidden;
    private boolean interactMode = false;
	public static boolean ignoreTouch = false;

    public static PerspectiveCamera camera;

    public static final Random rand = new Random();

    protected int saveLoc = 0;

    protected float gameTimeScale = 1.0f;

    public Progression progression = null;

    public static LerpedAnimationManager animationManager = new LerpedAnimationManager();

    public static ExecutorService threadPool = Executors.newFixedThreadPool(4);

    public static Pathfinding pathfinding = new Pathfinding();

	public Game(int saveLoc) {
		instance = this;
		Start(saveLoc);
	}

	public void loadManagers() {
		modManager = Game.getModManager();

		// Load item data
		ItemManager im = modManager.loadItemManager(gameData.itemDataFiles);
		if(im != null) itemManager = im;
		else ShowMessage(MessageFormat.format(StringManager.get("game.Game.errorLoadingDataText"), "ITEMS.DAT"), 2, 1f);

		if(itemManager == null) {
			itemManager = new ItemManager();
		}

		// Load enemy data
		MonsterManager mm = modManager.loadMonsterManager(gameData.monsterDataFiles);
		if(mm != null) monsterManager = mm;
		else ShowMessage(MessageFormat.format(StringManager.get("game.Game.errorLoadingDataText"), "MONSTERS.DAT"), 2, 1f);

		if(monsterManager == null) {
			monsterManager = new MonsterManager();
		}
		MonsterManager.setSingleton(monsterManager);

		// Load animation data
		LerpedAnimationManager lam = modManager.loadAnimationManager();
		if(lam != null) animationManager = lam;
		else ShowMessage(MessageFormat.format(StringManager.get("game.Game.errorLoadingDataText"), "ANIMATIONS.DAT"), 2, 1f);

		if(animationManager == null) {
			animationManager = new LerpedAnimationManager();
		}

		// load the entity templates
		EntityManager em = modManager.loadEntityManager(gameData.entityDataFiles);
		if(em != null) entityManager = em;
		else ShowMessage(MessageFormat.format(StringManager.get("game.Game.errorLoadingDataText"), "ENTITIES.DAT"), 2, 1f);

		if(entityManager == null) {
			entityManager = new EntityManager();
		}
		EntityManager.setSingleton(entityManager);

        loadHUDManager(modManager);
	}

	/** Create game for editor usage. */
	public Game(Level levelToStart) {
		instance = this;
		level = levelToStart;

		// we're in the editor
		inEditor = true;

		Game.flashTimer = 0;
		message.clear();
		useMessage.clear();
		useMessageColor = Color.WHITE;
		messageScale = 1;
		this.saveLoc = 3;

		DecalManager.setQuality(Options.instance.gfxQuality);

		// Load the base game data
		if(gameData == null) {
			gameData = modManager.loadGameData();
		}

		// load the game progress
		progression = loadProgression(saveLoc);

		isMobile = false;
		Gdx.input.setCursorCatched(true);
		hud = new Hud();

		loadManagers();

        hudManager.backpack.visible = false;

		Gdx.app.log("DelverLifeCycle", "READY EDITOR ONE");

		// try loading the player template
		try {
			Player playerTemplate = JsonUtil.fromJson(Player.class, Game.findInternalFileInMods("data/" + gameData.playerDataFile));
			if(playerTemplate != null) player = playerTemplate;
		}
		catch(Exception ex) {
			Gdx.app.error("Delver", ex.getMessage());
			player = new Player(this);
		}

        // Save version is always up to date in the editor
		player.saveVersion = SAVE_VERSION;

		levelNum = 0;
		level.loadFromEditor();

		player.randomSeed = rand.nextInt();
		player.gold = 1000;

		progression.inventoryUpgrades = 0;
		progression.hotbarUpgrades = 0;

		try {
			player.init();
			level.setPlayer(player);
		}
		catch(Exception ex) { Gdx.app.log("DelverLifeCycle", ex.getMessage()); }

		if(message.size > 0) messageTimer = 400;

		if(!isMobile)
			GameApplication.instance.input.caughtCursor = true;

		GameScreen.resetDelta = true;
	}

	// build the list of levels using a predefined dungeon layout
	private static Array<Level> loadDataLevels() {
		FileHandle dungeonFile = Game.findInternalFileInMods("data/dungeons.dat");
		if(!dungeonFile.exists()) return null;

		Array<Level> dataLevels = new Array<Level>();
		{
			dataLevels = JsonUtil.fromJson(Array.class, dungeonFile);
		}

		return dataLevels;
	}

	private static String[] getPackagedFiles() {
		// find packaged files
		Gdx.app.debug("Delver", "Looking in packaged_files");
		FileHandle allAssets = getInternal("packaged_files.txt");
		if(allAssets != null)  return allAssets.readString().split("\\r?\\n");
		else return new String[] { };
	}

	public static FileHandle[] listDirectory(FileHandle directory) {

	    Gdx.app.debug("Delver", "Listing Directory " + directory.path());

	    // make the search path match the output of `find .`
	    String path = directory.path();
	    if(!path.startsWith("./")) path = "./" + path;
	    if(path.equals("./assets")) path = "";
	    if(path.startsWith("./assets")) path = "./" + path.substring(9);
	    if(!path.endsWith("/")) path += "/";

        Array<FileHandle> files = new Array<FileHandle>();

        // add external files first
        files.addAll(directory.list());

        // then look for internal files
	    if(Gdx.app.getType() == ApplicationType.Desktop &&
                (directory.type() == Files.FileType.Classpath) || (directory.type() == Files.FileType.Internal)) {

	    	Gdx.app.log("Delver", "Looking internally");

			String[] packagedFiles = getPackagedFiles();

            for (int i = 0; i < packagedFiles.length; i++) {
                String s = packagedFiles[i];
                if (s.startsWith(path)) {
                    String sub = s.substring(path.length());
                    if(!sub.contains("/")) {
                        Gdx.app.debug("Delver", "Found in packaged_files: " + i);
                        FileHandle f = getInternal(s);
                        if (f.exists() && !files.contains(f, false)) {
                            files.add(f);
                        }
                    }
                }
            }
        }

        // return as a bare array, like the directory list function
        files.shrink();
        FileHandle[] r = new FileHandle[files.size];
        for(int i = 0; i < files.size; i++) {
            r[i] = files.get(i);
        }
        return r;
    }

    public static Array<FileHandle> findPackagedFiles(String filename) {
		String[] packagedFiles = getPackagedFiles();
		Array<FileHandle> files = new Array<FileHandle>();
		for (int i = 0; i < packagedFiles.length; i++) {
			String s = packagedFiles[i];
			if (s.endsWith(filename)) {
				FileHandle f = getInternal(s);
				if (f.exists() && !files.contains(f, false)) {
					files.add(f);
				}
			}
		}
		return files;
	}

	// build the list of levels
	public static Array<Level> buildLevelLayout() {

	    Gdx.app.log("Delver", "Building Dungeon Layout");

	    FileHandle dungeonsFile = Game.modManager.findFile("data/dungeons.dat");
	    if(dungeonsFile != null && dungeonsFile.exists()) {
            return loadDataLevels();
        }

        // No predefined dungeon layout, build one by searching for sections
	    // find all the dungeon sections we can
        ArrayMap<String, SectionDefinition> sections = new ArrayMap<String, SectionDefinition>();

        for(String folder : Game.modManager.modsFound) {
            Gdx.app.debug("Delver", "Looking in " + folder);

            FileHandle generatorFolder = getInternal(folder + "/generator");

            Gdx.app.debug("Delver", "Looking for files in " + generatorFolder.path());
            for(FileHandle g : listDirectory(generatorFolder)) {
                if(g.exists()) {
                    FileHandle sectionFile = g.child("section.dat");
                    if(sectionFile.exists()) {
                        Gdx.app.debug("Delver", "Found section file: " + sectionFile.path());
                        SectionDefinition d = JsonUtil.fromJson(SectionDefinition.class, sectionFile);
                        sections.put(g.name(), d);
                    }
                }
            }
        }

        // Build the final array
		Array<SectionDefinition> sectionsFound = new Array<SectionDefinition>();
        for(SectionDefinition s : sections.values()) {
			sectionsFound.add(s);
		}

        sectionsFound.sort(new Comparator<SectionDefinition>() {
            @Override
            public int compare(SectionDefinition o1, SectionDefinition o2) {
                if(o1.sortOrder > o2.sortOrder) return 1;
                if(o1.sortOrder < o2.sortOrder) return -1;
                return 0;
            }
        });

        Array<Level> levels = new Array<Level>();
        for(SectionDefinition s : sectionsFound) {
            Gdx.app.log("Delver", " Adding section " + s.name);
            levels.addAll(s.buildLevels());
        }

        return levels;
    }

	public void Start(int saveLoc)
	{
		loadManagers();

		Game.flashTimer = 0;
        Game.flashColor.set(Color.BLACK);

		message.clear();
		useMessage.clear();
		useMessageColor = Color.WHITE;
		messageScale = 1;
		this.saveLoc = saveLoc;

		DecalManager.setQuality(Options.instance.gfxQuality);

		hudManager.backpack.visible = false;

		// Load the levels data file, keep the levels array null for now (try loading from save first)
		Array<Level> dataLevels = buildLevelLayout();

		// load the game progress
		progression = loadProgression(saveLoc);
		progression.trackMods();

		boolean didLoad = load();

		isMobile = Gdx.app.getType() == ApplicationType.Android || Gdx.app.getType() == ApplicationType.iOS;
		//isMobile = true;

		if(!isMobile) {
			Gdx.input.setCursorCatched(true);
			hud = new Hud();
		} else {
			hud = new MobileHud();
		}

		if(!didLoad) {
			Game.flashTimer = 100;

			// Game Start!
			Gdx.app.log("DelverLifeCycle", "READY PLAYER ONE");

			// try loading the player template
			try {
				Player playerTemplate = JsonUtil.fromJson(Player.class, Game.findInternalFileInMods("data/" + gameData.playerDataFile));
				if (playerTemplate != null) player = playerTemplate;
			} catch (Exception ex) {
				Gdx.app.error("Delver", ex.getMessage());
				player = new Player(this);
			}

			// Set progression
			player.randomSeed = rand.nextInt();
			player.gold = progression.gold;
			player.inventorySize += (progression.inventoryUpgrades + progression.hotbarUpgrades);
			player.hotbarSize += progression.hotbarUpgrades;
			player.startPlaytime();

			// maybe make the game harder after the player has won before?
			progression.won = false;
			progression.newRunStarted();

			// load the level
			levelNum = 0;

			if (progression.sawTutorial || gameData.tutorialLevel == null) {
				level = dataLevels.get(levelNum);
			} else {
				levelNum = -1;
				player = new Player(this);
				player.level = 2;
				player.gold = progression.gold;
				player.maxHp = 12;
				player.hp = player.maxHp;
				player.randomSeed = rand.nextInt();
				level = gameData.tutorialLevel;
				progression.sawTutorial = true;
			}

			// Keep track of what engine version we're playing on
			player.saveVersion = SAVE_VERSION;

			// Now we can load the level
			level.load();

			if(level.playerStartX != null && level.playerStartY != null) {
				player.x = level.playerStartX + 0.5f;
				player.y = level.playerStartY + 0.5f;
			}
			else {
				player.x = 0;
				player.y = 0;
			}

			player.z = level.getTile((int)player.x, (int)player.y).getFloorHeight() + 0.5f;
			if(level.playerStartRot != null) player.rot = (float)Math.toRadians(-(level.playerStartRot + 180f));

			level.setPlayer(player);
			player.levelNum = levelNum;
		}

		try {
			saveProgression(progression, saveLoc);
		}
		catch(Exception ex) { Gdx.app.log("DelverLifeCycle", ex.getMessage()); }

		try {
			player.init();
		}
		catch(Exception ex) { Gdx.app.log("DelverLifeCycle", ex.getMessage()); }

		if(message.size > 0) messageTimer = 400;

		if(!isMobile)
			GameApplication.instance.input.caughtCursor = true;

		GameScreen.resetDelta = true;
	}

	public void setInputHandler(GameInput input)
	{
		this.input = input;
	}

	public static void initGamepadManager() {
		try {
			if(gamepadManager == null) {
				gamepadManager = new GamepadManager(new ControllerState());
				gamepadManager.init();
				Controllers.addListener(gamepadManager);
			}
		}
		catch(Exception ex) { Gdx.app.log("Delver", ex.getMessage()); }
	}

	public void tick(float delta) {
	    // The speed of time can be changed, but we still want to know the original delta
	    float timeModifiedDelta = delta * gameTimeScale;
		time += timeModifiedDelta;

		if(messageTimer > 0) messageTimer -= delta;
		if(flashTimer > 0) flashTimer -= delta;

		Game.useMessage.clear();

        // Game over logic!
        if(player.hp <= 0 && !player.isDead) {
            player.die();
        }
        else {
            if (!gameOver && player.isDead && (player.dyingAnimation == null || !player.dyingAnimation.playing))
                OnGameOver();
        }

        if (gameOver) return;

        // Entities should update using the time modified delta
		level.tick(timeModifiedDelta);
		player.tick(level, delta * player.actorTimeScale, input);

		input.tick();
        Audio.tick(delta, player, level);
		Game.pathfinding.tick(delta);

        if(!gamepadManager.menuMode) {
			gamepadManager.tick(delta);
		}
		else {
			gamepadManager.menuMode = false;
		}

		if(ui != null)
			ui.act(delta);

        hudManager.quickSlots.tickUI(input);
		hudManager.backpack.tickUI(input);
		hud.tick(input);

		// keep the cache clean
		CachePools.clearOnTick();
	}

	public void changeLevel(Stairs stair)
	{

		if(stair.direction == StairDirection.up && player.getCurrentTravelKey() != null) {
			doLevelExit(null);
		}
		else if(stair.direction == StairDirection.up && levelNum == 1) {
			boolean hasOrb = false;
			for(int i = 0; i < player.inventory.size; i++) {
				if(player.inventory.get(i) instanceof QuestItem) {
					hasOrb = true;
				}
			}

			if(!hasOrb) {
				Game.ShowMessage(StringManager.get("game.Game.cannotLeaveText"), 4, 1f);
			}
			else
				GameApplication.ShowGameOverScreen(true);
		}
		else {
			Gdx.app.log("DelverLifeCycle", "Showing Level Change Screen");
			GameApplication.ShowLevelChangeScreen(stair);
		}
	}

	public void warpToLevel(String newTravelPathId, TriggeredWarp warp) {
		Gdx.app.log("DelverLifeCycle", "Warping to: " + warp.levelToLoad);

		// save game on change
		if(!Game.inEditor) {
			save(levelNum, player.getCurrentTravelKey());
		}

		// keep track of new area
		TravelInfo travelInfo = new TravelInfo(warp.travelPath, new Vector3(player.x, player.y, player.z), levelNum);
		player.addTravelPath(travelInfo);

		// renderer doesn't need to hold onto this anymore
		if(warp.unloadParentLevel) GameManager.renderer.freeLoadedLevel();
		else travelInfo.level = level;

		level = null;

		if(warp.fadetoBlack) {
			Game.flash(Color.BLACK, 50);
		}

		boolean loadedLevel = false;
		if(levelFileExists(levelNum, travelInfo.locationUuid)) {
			try {
				loadedLevel = loadLevel(levelNum, travelInfo.locationUuid);
			}
			catch(Exception ex) {
				Gdx.app.error("DelverLifeCycle", ex.getMessage());
			}
		}

		if(!loadedLevel) {
			level = new Level();
			level.generated = warp.generated;
			level.levelFileName = warp.levelToLoad;
			level.theme = warp.levelTheme;
			level.fogColor.set(warp.fogColor);
			level.ambientTileLighting = warp.ambientLightColor;
			level.fogEnd = warp.fogEnd;
			level.fogStart = warp.fogStart;
			level.skyLightColor = new Color(warp.skyLightColor);
			level.viewDistance = level.fogEnd + 2;
			level.isDirty = true;
			level.levelName = warp.levelName;
			level.makeStairsDown = false;
			level.spawnMonsters = warp.spawnMonsters;
			level.objectivePrefab = warp.objectivePrefabToSpawn;
			level.music = warp.music;
			level.ambientSound = warp.ambientSound;

			level.load();
		}

		level.setPlayer(player);

		player.ignoreStairs = true;

		if(level.up != null) {
				player.x = level.up.x;
				player.y = level.up.y + 0.05f;
				player.z = level.getTile((int)level.up.x, (int)level.up.y).floorHeight + 0.5f;
				player.xa = 0;
				player.ya = 0;
		}
		else if(level.playerStartX != null && level.playerStartY != null) {
			player.x = level.playerStartX + 0.5f;
			player.y = level.playerStartY + 0.5f;
			player.z = level.getTile((int) player.x, (int) player.y).floorHeight + 0.5f;
		}

		if(warp != null && warp.toWarpMarkerId != null) {
			putPlayerAtWarpMarker(warp.toWarpMarkerId);
		}

		player.xa = 0;
		player.ya = 0;

		GameManager.renderer.setLevelToRender(level);

		level.rendererDirty = true;

		GameScreen.resetDelta = true;

		level.initAudio();

		Gdx.app.log("DelverLifeCycle", "Level Changed");
	}

	public void putPlayerAtWarpMarker(String warpMarkerId) {
		if(warpMarkerId != null) {
			Array<Entity> found = level.getEntitiesById(warpMarkerId);

			// try a more fuzzy search, as a fallback
			if(found.size == 0) {
				found = level.getEntitiesLikeId(warpMarkerId);
			}

			if(found.size > 0) {
				Entity warpTo = found.first();
				player.x = warpTo.x;
				player.y = warpTo.y;
				player.z = warpTo.z;
				player.rot = (float)Math.toRadians(warpTo.getRotation().z + 90f);
			}
		}
	}

	public void doLevelExit(TriggeredWarp warp) {
		Gdx.app.log("DelverLifeCycle", "Exiting level");

		if(!Game.inEditor)
			save(levelNum, player.getCurrentTravelKey());

		// renderer doesn't need to hold onto this anymore
		if(warp != null) {
			if (warp.unloadThisLevelOnExit) {
				GameManager.renderer.freeLoadedLevel();
			}

			if (warp.fadetoBlack) {
				Game.flash(Color.BLACK, 50);
			}
		}

		if(player.getCurrentTravelKey() == null) {
			// don't let the editor crash!
			if(Game.inEditor) {
				GameApplication.editorRunning = false;
				GameManager.getGame().level.preSaveCleanup();
				return;
			}
		}

		TravelInfo info = player.popTravelPath();
		levelNum = info.fromLevelNum;

		if(info.level != null) {
			level = info.level;
			level.isDirty = true;
			level.init(Source.LEVEL_LOAD);
		}
		else {
			// don't try to load from a save in the editor
			if(Game.inEditor) {
				GameApplication.editorRunning = false;
				GameManager.getGame().level.preSaveCleanup();
				return;
			}

			// not in memory anymore, load it
			try {
				loadLevel(levelNum, player.getCurrentTravelKey());
			} catch (Exception ex) {
				Gdx.app.log("Delver", ex.getMessage());
			}
		}

		level.setPlayer(player);

		player.x = info.fromLocation.x;
		player.y = info.fromLocation.y;
		player.z = info.fromLocation.z;

		if(warp != null && warp.toWarpMarkerId != null) {
			putPlayerAtWarpMarker(warp.toWarpMarkerId);
		}

		player.xa = 0;
		player.ya = 0;

		GameManager.renderer.setLevelToRender(level);
		level.rendererDirty = true;

		GameScreen.resetDelta = true;

		level.initAudio();

		Gdx.app.log("DelverLifeCycle", "Level Changed");
	}

	public void doLevelChange(Stairs stair) {
		if(stair == null) return;

		Gdx.app.log("DelverLifeCycle", "Changing level: " + stair.direction);

		// save game on change
		save(levelNum, player.getCurrentTravelKey());

		// clear memory of old level
		level = null;

		// renderer doesn't need to hold onto this anymore
		GameManager.renderer.freeLoadedLevel();

		if(stair.direction == StairDirection.down)
			levelNum++;
		else
			levelNum--;

		Array<Level> levels = buildLevelLayout();

		if(levelNum < 0) levelNum = 0;
		if(levelNum > levels.size - 1) levelNum = levels.size - 1;

		// load if not already
		try {
			loadLevel(levelNum, player.getCurrentTravelKey());
		} catch(Exception ex) {
			Gdx.app.log("Delver", ex.getMessage());
		}
		level.setPlayer(player);

		// TODO: Generate levels!
		if(!level.isLoaded)
			level.load();
		else
			level.init(Source.LEVEL_LOAD);

		player.spawnX = player.x;
		player.spawnY = player.y;

		player.levelNum = levelNum;

		if(stair.direction == StairDirection.down)
			stair = level.up;
		else
			stair = level.down;

		if(stair != null) {
			player.x = stair.x;
			player.y = stair.y + 0.05f;
			player.z = level.getTile((int)stair.x, (int)stair.y).floorHeight + 0.5f;
			player.xa = 0;
			player.ya = 0;

			if(stair.direction == StairDirection.up)
				player.rot = (float)Math.toRadians((stair.exitRotation + 180));
			else
				player.rot = (float)Math.toRadians(-(stair.exitRotation) + 180);
		}
		else {
			player.x = level.playerStartX + 0.5f;
			player.y = level.playerStartY + 0.5f;
			player.z = level.getTile(level.playerStartX, level.playerStartY).floorHeight + 0.5f;
			player.xa = 0;
			player.ya = 0;

			if(level.playerStartRot != null)
				player.rot = (float)Math.toRadians(-(level.playerStartRot + 90f));
		}

		player.ignoreStairs = true;

		level.rendererDirty = true;
		GameScreen.resetDelta = true;

		Gdx.app.log("DelverLifeCycle", "Level Changed");
	}

	public static void ShowMessage(String newMessage, float seconds)
	{
		useMessageColor = Color.WHITE;
		message.clear();
		if(newMessage.equals("")) return;

		message.addAll(newMessage.split("\n"));

		messageTimer = 60 * seconds;
		messageScale = 1;
	}

	public static void ShowMessage(String newMessage, float seconds, float scale)
	{
		ShowMessage(newMessage,seconds);
		messageScale = scale;
	}

	public static void ShowUseMessage(String msg, Color firstLineColor) {
		ShowUseMessage(msg);
		useMessageColor = firstLineColor;
	}

	public static void ShowUseMessage(String msg) {
		useMessageColor = Color.WHITE;
		useMessage.clear();
		if(useMessage.equals("")) return;

		useMessage.addAll(msg.split("\n"));
	}

	public static void flash(Color color, int milliseconds)
	{
		flashColor.set(color);
		flashTimer = milliseconds;
		flashLength = milliseconds;
	}

	public void OnGameOver()
	{
		gameOver = true;

		if(Audio.steps != null)
			Audio.steps.stop();

		if(Audio.torch != null)
			Audio.torch.stop();

		// goodbye saves!
		Gdx.app.log("DelverLifeCycle", "Game over!");
		GameApplication.ShowGameOverScreen(false);
	}

	// Save all the levels!
	public void save()
	{
		String saveDir = getSaveDir();
		String levelDir = saveDir + "/levels/";

		if(gameOver)
		{
			FileHandle dir = getFile(saveDir);
			if(dir.exists()) {
				Gdx.app.log("DelverLifeCycle", "Sorry man, deleting saves");
				dir.deleteDirectory();
			}

            // Unload the current level
            if(level != null) level.preSaveCleanup();

			return;
		}

		String travelPathKey = player.getCurrentTravelKey();
		if(travelPathKey != null) {
			levelDir += travelPathKey + "/";
		}

		Gdx.app.log("DelverLifeCycle", "saving game!");

		FileHandle dir = getFile(levelDir);
		if(!dir.exists()) dir.mkdirs();

		try {
			Gdx.app.log("DelverLifeCycle", "Saving options");
			Options.saveOptions();
		}
		catch(Exception ex) {
		    Gdx.app.log("DelverLifeCycle", "Error saving options");
		}

		if(level.needsSaving) {
			Gdx.app.log("DelverLifeCycle", "Saving Level");

			FileHandle file = getFile(levelDir + levelNum + ".bin");
			if(file.exists()) file.delete();

			FileHandle lvlfile = getFile(levelDir + levelNum + ".lvl");
			if(lvlfile.exists()) lvlfile.delete();

			FileHandle jsonfile = getFile(levelDir + levelNum + ".dat");
			if(jsonfile.exists()) jsonfile.delete();

			Gdx.app.log("DelverLifeCycle", "Saving to " + file.path());

			// can't save some entity data
			level.preSaveCleanup();

			KryoSerializer.saveLevel(file, level);
		}

		// save the player
		FileHandle file = getFile(saveDir + "player.dat");
		JsonUtil.toJson(player, file);

		// save progress!
		saveProgression(progression, Game.instance.getSaveSlot());
	}

	// Save a level
	public void save(int i, String travelPathKey)
	{
		String saveDir = getSaveDir();
		String levelDir = saveDir + "/levels/";

		if(travelPathKey != null) {
			levelDir += travelPathKey + "/";
		}

		FileHandle dir = getFile(levelDir);
		if(!dir.exists()) dir.mkdirs();

		if(level != null && level.needsSaving)
		{
			Gdx.app.log("DelverLifeCycle", "Saving Level " + levelNum);

			FileHandle file = getFile(levelDir + levelNum + ".bin");
			if(file.exists()) file.delete();

			Gdx.app.log("DelverLifeCycle", "Saving to " + file.path());

			level.preSaveCleanup();
			KryoSerializer.saveLevel(file, level);
		}
		else Gdx.app.log("DelverLifeCycle", "Level " + levelNum + " did not need saving");

		// save the player
		FileHandle file = getFile(saveDir + "player.dat");
		JsonUtil.toJson(player, file);
	}

	public boolean levelFileExists(int levelnum, String travelPathKey) {
		String saveDir = getSaveDir();
		String levelDir = saveDir + "levels/";

		if(travelPathKey != null) {
			levelDir += travelPathKey + "/";
		}

		FileHandle dir = getFile(levelDir);
		if(!dir.exists() && dir.length() == 0) return false;

		FileHandle file = getFile(levelDir + levelnum + ".dat");
		FileHandle kryofile = getFile(levelDir + levelnum + ".bin");

		if(kryofile.exists()) return true;
		return file.exists();
	}

	public boolean loadLevel(int levelNumber, String travelPathKey) throws FileNotFoundException, IOException, ClassNotFoundException {
		String saveDir = getSaveDir();
		String levelDir = saveDir + "levels/";

		if(travelPathKey != null) {
			levelDir += travelPathKey + "/";
		}

		FileHandle dir = getFile(levelDir);
		if(!dir.exists() && dir.length() == 0) return false;

		FileHandle file = getFile(levelDir + levelNumber + ".dat");
		FileHandle kryofile = getFile(levelDir + levelNumber + ".bin");

		Array<Level> dataLevels = buildLevelLayout();

		if(kryofile.exists()) {
			if(travelPathKey == null && levelNumber >= 0 && dataLevels.get(levelNumber) instanceof OverworldLevel) {
				level = KryoSerializer.loadOverworldLevel(kryofile);
				level.init(Source.LEVEL_LOAD);
				Gdx.app.log("DelverLifeCycle", "Loading level " + levelNumber + " from " + file.path());
			}
			else {
				level = KryoSerializer.loadLevel(kryofile);
				level.init(Source.LEVEL_LOAD);
				Gdx.app.log("DelverLifeCycle", "Loading level " + levelNumber + " from " + file.path());
			}
		}
		else if(file.exists()) {
			level = JsonUtil.fromJson(SavedLevelContainer.class, file).level;
			level.init(Source.LEVEL_LOAD);

			Gdx.app.log("DelverLifeCycle", "Loading level " + levelNumber + " from " + file.path());
		}
		else {
			if(levelNumber < 0) levelNumber = 0;
			level = dataLevels.get(levelNumber);
			level.load();
		}

		return true;
	}

	@SuppressWarnings("unchecked")
	public boolean load()
	{
		try
		{
			String saveDir = getSaveDir();
			String levelDir = saveDir + "levels/";

			FileHandle dir = getFile(levelDir);
			if(!dir.exists() && dir.length() == 0) return false;

			Gdx.app.log("DelverLifeCycle", "Loading game from " + dir.path());
			for(FileHandle f : dir.list()) {
				Gdx.app.log("DelverLifeCycle", "Found " + f.path());
			}

			// load the player
			FileHandle file = getFile(saveDir + "player.dat");
			if(file.exists())
			{
				Gdx.app.log("DelverLifeCycle", "Loading player from " + file.path());

				player = JsonUtil.fromJson(Player.class, file);
			}
			else return false;

			levelNum = player.levelNum;

			// Might need to do something if breaking changes were made between save versions
			if(player.saveVersion != SAVE_VERSION) {
				handleVersionMismatch(player);
			}

			// load the level the player is on
			loadLevel(levelNum, player.getCurrentTravelKey());

			// wtf yo
			if(level == null) return false;

			Gdx.app.log("DelverLifeCycle", "Game loaded successfully");

			return true;
		}
		catch(Exception e)
		{
			Logger.logExceptionToFile(e);
			message.add(StringManager.get("game.Game.loadErrorText"));
			return false;
		}
	}

	private void handleVersionMismatch(Player player) {
        // Handle the player offset bug that was present in pre-opensource days
		if(player.saveVersion < 1) {
			player.x += 0.5f;
			player.y += 0.5f;
		}

		player.saveVersion = SAVE_VERSION;
	}

    public static FileHandle getFile(String file) {
        if (OSUtils.isMac()) {
            // OSX needs this user.dir property to figure out where it is running from, and probably linux
            String userDir = System.getProperty("user.dir");
            if (userDir != null) {
                return new FileHandle(userDir + "/" + file);
            }
        }

        if (OSUtils.isMobile() && Gdx.files != null) {
            if (Gdx.files.isExternalStorageAvailable()) {
                return Gdx.files.external("Delver/" + file);
            }

            return Gdx.files.local("Delver/" + file);
        }

        return new FileHandle(file);
    }

	public static Level GetLevel()
	{
		return instance.level;
	}

	public static ItemManager GetItemManager()
	{
		return instance.itemManager;
	}

	public static EntityManager GetEntityManager() {
		return instance.entityManager;
	}

	public static MonsterManager GetMonsterManager()
	{
		return instance.monsterManager;
	}

	public static float GetUiSize()
	{
		if(Game.instance == null) {
			uiSize = 80f * Game.getDynamicUiScale();
		}

		if(Options.instance == null) Options.instance = new Options();
		return uiSize * Options.instance.uiSize;
	}

	public static void RefreshUI() {
		hudManager.quickSlots.refresh();
		hudManager.backpack.refresh();
		hud.refreshEquipLocations();
	}

	public static DragAndDropResult DragAndDropInventoryItem( Item dragging, Integer invLoc, String equipLoc ) {

		Item swap = null;
		Integer mouseOverSlot = null;
		Game.dragging = null;

		if(hudManager.quickSlots.getMouseOverSlot() != null) {
			mouseOverSlot = hudManager.quickSlots.getMouseOverSlot() + hudManager.quickSlots.invOffset;
		} else if(hudManager.backpack.visible && hudManager.backpack.getMouseOverSlot() != null) {
			mouseOverSlot = hudManager.backpack.getMouseOverSlot() + hudManager.backpack.invOffset;
		}

		String equipOverSlot = null;
		for(EquipLoc loc : hud.equipLocations.values())
		{
			equipOverSlot = loc.getMouseOverSlot();
			if(equipOverSlot != null) break;
		}

		if(equipOverSlot != null) {

			if(!dragging.GetEquipLoc().equals(equipOverSlot)) return DragAndDropResult.invalid;

			swap = Game.instance.player.equippedItems.get(equipOverSlot);

			if(invLoc != null) {
				Game.instance.player.inventory.set(invLoc, null);
			}

			if(equipLoc != null) {
				Game.instance.player.equippedItems.put(equipLoc, null);
			}

			Audio.playSound(dragging.equipSound, 0.5f, Game.rand.nextFloat() * 0.1f + 0.95f);
			Game.instance.player.equippedItems.put(equipOverSlot, dragging);

			if(swap != null)
			{
				if(invLoc != null)
					Game.instance.player.inventory.set(invLoc, swap);
				else if(equipLoc != null)
					Game.instance.player.equippedItems.put(equipLoc, swap);
				else {
					swap.x = dragging.x;
					swap.y = dragging.y;
					swap.z = dragging.z;
					swap.isActive = true;
					Game.instance.level.entities.add(swap);
				}
			}

			Audio.playSound("inventory/grab_item.mp3", 0.7f, Game.rand.nextFloat() * 0.1f + 0.95f);

			return DragAndDropResult.equip;
		}
		else if(mouseOverSlot != null) {

			swap = Game.instance.player.inventory.get(mouseOverSlot);

			if(equipLoc != null && swap != null && !equipLoc.equals(swap.equipLoc)) return DragAndDropResult.invalid;

			Boolean dropOnGround = invLoc == null;
			Boolean isSwapEquipped = Game.instance.player.isHeld(swap);
			Boolean isDraggingEquipped = Game.instance.player.isHeld(dragging);

			if(invLoc != null)
				Game.instance.player.inventory.set(invLoc, null);

			if(equipLoc != null)
				Game.instance.player.equippedItems.put(equipLoc, null);

			Game.instance.player.inventory.set(mouseOverSlot, dragging);

			if(swap != null)
			{
				if(invLoc != null)
					Game.instance.player.inventory.set(invLoc, swap);
				else if(equipLoc != null)
					Game.instance.player.equippedItems.put(equipLoc, swap);
				else {
					swap.x = dragging.x;
					swap.y = dragging.y;
					swap.z = dragging.z;
					swap.physicsSleeping = false;
					swap.isActive = true;
					Game.instance.level.entities.add(swap);
				}

			}

			if(isSwapEquipped && !dropOnGround)
				Game.instance.player.equip(swap, false);

			if(isDraggingEquipped)
				Game.instance.player.equip(dragging, false);

			// Dragging from the ground can equip items
			if(invLoc == null) {
				if(isDraggingEquipped)
					Game.instance.player.equip(dragging, false);
				if (dragging instanceof Weapon && Game.instance.player.GetHeldItem() == null)
					Game.instance.player.equip(dragging);
				else if (dragging instanceof QuestItem)
				{
					((QuestItem)dragging).doQuestThing();
				}
			}

			Audio.playSound("inventory/grab_item.mp3", 0.7f, Game.rand.nextFloat() * 0.1f + 0.95f);

			return DragAndDropResult.equip;
		}

		return DragAndDropResult.drop;
	}

	private String getSaveDir() {
		return "save/" + saveLoc + "/";
	}

	public static Progression loadProgression(Integer saveSlot) {
		try {
			//FileHandle modFile = Game.getInternal(path + "/data/items.dat");
			FileHandle progressionFile = getFile(Options.getOptionsDir() + "game_" + saveSlot + ".dat");
			return JsonUtil.fromJson(Progression.class, progressionFile);
		} catch (Exception e) {
			Gdx.app.error("Delver", e.getMessage());
		}

		return new Progression();
	}

	public static void saveProgression(Progression progression, Integer saveSlot) {
		try {
			if(progression != null) {

				// Ensure that the directory exists first
				String optionsDirString = Options.getOptionsDir();
				FileHandle optionsDir = getFile(optionsDirString);

				if(!optionsDir.exists())
					optionsDir.mkdirs();

				FileHandle progressionFile = getFile(Options.getOptionsDir() + "game_" + saveSlot + ".dat");
				JsonUtil.toJson(progression, progressionFile);
			}
		} catch (Exception e) {
			Gdx.app.error("Delver", e.getMessage());
		}
	}

	public void refreshMenu() {
		setMenuMode(Game.instance.getMenuMode());
	}

	public void setMenuMode(MenuMode menuMode) {

		recalculateUiScale();

		if(this.menuMode != menuMode) {
			this.menuMode = menuMode;
			Audio.playSound("inventory/open_inventory.mp3", 0.5f, 1f);
		}

		if(characterScreen == null) characterScreen = new CharacterScreen(player, UiSkin.getSkin());
		if(inventoryScreen == null) inventoryScreen = new InventoryScreen(player, UiSkin.getSkin());

		if(menuMode == MenuMode.Hidden) {
			characterScreen.setVisible(false);
			inventoryScreen.setVisible(false);
		}
		else if(menuMode == MenuMode.Inventory) {
			inventoryScreen.setVisible(true);
			characterScreen.setVisible(false);
		}
		else if(menuMode == MenuMode.Character) {
			characterScreen.setVisible(true);
			inventoryScreen.setVisible(false);
		}

		// Show the proper bag slots
		Game.hudManager.backpack.visible = menuMode == MenuMode.Inventory;
		for(EquipLoc loc : hud.equipLocations.values())
		{
			loc.visible = menuMode != MenuMode.Hidden;
		}

		// Refresh the UI
		Game.RefreshUI();
		if(input != null) input.setCursorCatched(menuMode == MenuMode.Hidden);

		// Hide the map!
		if(Game.instance.getShowingMenu()) {
			GameManager.renderer.showMap = false;
		}
	}

	public void toggleInventory() {
		if(menuMode != MenuMode.Inventory) {
			setMenuMode(MenuMode.Inventory);
		}
		else {
			setMenuMode(MenuMode.Hidden);
		}
	}

	public void toggleCharacterScreen() {
		if(menuMode != MenuMode.Character) {
			setMenuMode(MenuMode.Character);
		}
		else {
			setMenuMode(MenuMode.Hidden);
		}
	}

	public void toggleInteractMode() {
		if(menuMode == MenuMode.Inventory) toggleInventory();
		interactMode = !interactMode;

		input.caughtCursor = !interactMode;
		input.setCursorCatched(input.caughtCursor);
	}

	public void hideMenu() {
		menuMode = MenuMode.Hidden;
		if(inventoryScreen != null && inventoryScreen.isVisible()) inventoryScreen.hide();
		if(characterScreen != null && characterScreen.isVisible()) characterScreen.hide();
	}

	public MenuMode getMenuMode() {
		return menuMode;
	}

	public boolean getShowingMenu() {
		return menuMode != MenuMode.Hidden;
	}

	public boolean getInteractMode() {
		return interactMode;
	}

	// Grabs from an external assets dir if available, or gets internal
	static public FileHandle getInternal(String filename)
	{
	    Gdx.app.debug("Delver", "Looking for " + filename);

		if(filename.startsWith("./")) filename = filename.substring(2);
		FileHandle h = getFile("assets/" + filename);
		if(h.exists()) return h;

        Gdx.app.debug("Delver", " Not found, looking internally");

        h = Gdx.files.internal(filename);

        if(!h.exists()) Gdx.app.debug("Delver", "  Still not found!");

		return h;
	}

    static public FileHandle findInternalFileInMods(String filename) {
		if(filename == null) return null;
        FileHandle h = Game.modManager.findFile(filename);
        if(h != null && h.exists()) return h;
        return Gdx.files.internal(filename);
    }

	public void clearMemory() {
		level = null;
	}

	public void updateMouseInput() {
		if(player != null) player.updateMouseInput(input);
	}

	public String getLevelName(int num) {
		Array<Level> dungeonData = buildLevelLayout();
		Level l = dungeonData.get(num);
		if(l != null) return l.levelName;

		return MessageFormat.format(StringManager.get("game.Game.levelNameText"), num);
	}

	public int getSaveSlot() {
		return saveLoc;
	}

	public static void init() {
		initGamepadManager();

		// Load the base game data
		if(gameData == null) {
			gameData = modManager.loadGameData();
		}
	}

	public static void quitEditorPreview() {
		// TODO Auto-generated method stub
	}

    public static ModManager getModManager() {
        if(modManager == null) {
        	modManager = new ModManager(true);
		}

        return modManager;
    }

    public static boolean isGameRunning() {
		return instance != null && !instance.gameOver;
	}

	public static int getNumberOfWins() {
		if(instance != null && instance.progression != null) {
			return instance.progression.wins;
		}
		return 0;
	}

	public void SetGameTimeScale(float worldSpeedModifier) {
        gameTimeScale = worldSpeedModifier;
    }

    public float GetGameTimeScale() {
	    return gameTimeScale;
    }

	public void recalculateUiScale() {
		uiSize = 80f * Game.getDynamicUiScale();

		if(Game.characterScreen != null) {
			if(Game.characterScreen.isVisible())
				Game.characterScreen.resize();
		}
		if(Game.inventoryScreen != null) {
			if(Game.inventoryScreen.isVisible())
				Game.inventoryScreen.resize();
		}
	}

	public static float getDynamicUiScale() {
		// shrink scale if we start to clip outside the default bounds
		float scaleX = Gdx.graphics.getWidth() / 1800f;
		float scaleY = Gdx.graphics.getHeight() / 1020f;
		float min = Math.min(scaleX, scaleY);
		min = (int) (min * 10f);
		min = (min / 10f);

		// Responsive design! :D
		if(Gdx.graphics.getWidth() <= 720) {
			min *= 2f;
		}
		else if(Gdx.graphics.getWidth() <= 1024) {
			min *= 1.25f;
		}
		else {
			min *= 1f;
		}

		return min;
	}

    private static void loadHUDManager(ModManager modManager) {
        hudManager = modManager.loadHUDManager();

        if (null == hudManager) {
            hudManager = new HUDManager();
            ShowMessage(MessageFormat.format(StringManager.get("game.Game.errorLoadingDataText"), "HUD.DAT"), 2, 1f);
        }
    }
}
