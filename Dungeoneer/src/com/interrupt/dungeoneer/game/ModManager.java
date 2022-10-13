package com.interrupt.dungeoneer.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.interrupt.api.steam.SteamApi;
import com.interrupt.api.steam.workshop.WorkshopModData;
import com.interrupt.dungeoneer.entities.Door;
import com.interrupt.dungeoneer.entities.Entity;
import com.interrupt.dungeoneer.generator.GenInfo;
import com.interrupt.dungeoneer.generator.GenTheme;
import com.interrupt.dungeoneer.gfx.TextureAtlas;
import com.interrupt.dungeoneer.gfx.animation.lerp3d.LerpedAnimationManager;
import com.interrupt.dungeoneer.gfx.shaders.ShaderData;
import com.interrupt.dungeoneer.scripting.ScriptingApi;
import com.interrupt.managers.*;
import com.interrupt.utils.JsonUtil;
import com.interrupt.utils.Logger;

import java.util.HashMap;

public class ModManager {
    private static final String DATA_HUD_DAT = "/data/hud.dat";

    private transient Array<String> allMods = new Array<String>();

    public transient Array<String> modsFound = new Array<String>();

    private HashMap<String, Boolean> modsEnabled = new HashMap<String, Boolean>();

    private transient Array<String> excludeFiles = new Array<String>();

    // Disabling custom scripting for now. Additional info can be found:
    // https://github.com/Interrupt/delverengine/issues/267
    private static ScriptingApi scriptingApi = null;

    public ModManager() { }

    public ModManager(boolean init) {
        if(init) {
            init();
        }
    }

    public void init() {
        loadModsEnabledList();
        refresh();

        if(scriptingApi != null) {
            // If we have a scripting manager set, try to load any scripts
            scriptingApi.loadScripts(this);
        }
    }

    public static void setScriptingApi(ScriptingApi newScriptingApi) {
        scriptingApi = newScriptingApi;
    }

    private void loadModsEnabledList() {
        try {
            FileHandle progressionFile = Game.getFile(Options.getOptionsDir() + "modslist.dat");
            if(progressionFile.exists()) {
                ModManager loaded = JsonUtil.fromJson(ModManager.class, progressionFile);
                modsEnabled = loaded.modsEnabled;
            }
        } catch (Exception e) {
            Gdx.app.error("DelverMods", e.getMessage());
        }
    }

    public void saveModsEnabledList() {
        try {
            FileHandle progressionFile = Game.getFile(Options.getOptionsDir() + "modslist.dat");
            JsonUtil.toJson(this, progressionFile);
        } catch (Exception e) {
            Gdx.app.error("DelverMods", e.getMessage());
        }
    }

    public void refresh() {
        findMods();
        filterMods();
        loadExcludesList();
    }

    private void findMods() {
        // reset
        allMods.clear();

        // add the default search paths
        allMods.add(".");

        FileHandle fh = Game.getInternal("mods");
        for(FileHandle h : fh.list()) {
            if(h.isDirectory()) allMods.add("mods/" + h.name());
        }

        // add any mods subscribed in Steam Workshop
        allMods.addAll(SteamApi.api.getWorkshopFolders());
    }

    private void filterMods() {
        // reset
        modsFound.clear();

        // add all enabled mods
        for(String mod : allMods) {
            boolean enabled = checkIfModIsEnabled(mod);
            if(enabled) {
                modsFound.add(mod);
            }
        }
    }

    private void loadExcludesList() {
        for(String path : modsFound) {
            try {
                FileHandle modFile = Game.getInternal(path + "/data/excludes.dat");
                if (modFile.exists()) {
                    Array<String> excludes = JsonUtil.fromJson(Array.class, modFile);
                    if(excludes != null) {
                        excludeFiles.addAll(excludes);
                    }
                }
            }
            catch(Exception ex) {
                Gdx.app.error("Delver", "Error loading mod file " + path + "/data/excludes.dat");
            }
        }
    }

    public EntityManager loadEntityManager(String[] filenames) {
        EntityManager entityManager = null;
        for(String filename : filenames) {
            for (String path : modsFound) {
                String filePath = path + "/data/" + filename;
                try {
                    FileHandle modFile = Game.getInternal(filePath);
                    if (modFile.exists() && !pathIsExcluded(filePath)) {
                        EntityManager thisModManager = JsonUtil.fromJson(EntityManager.class, modFile);
                        if (entityManager == null) {
                            entityManager = thisModManager;
                        } else if (thisModManager != null)
                            entityManager.merge(thisModManager);
                    }
                } catch (Exception ex) {
                    Gdx.app.error("Delver", "Error loading mod file: " + filePath);
                    Logger.logExceptionToFile(ex);
                }
            }
        }
        return entityManager;
    }

    public ItemManager loadItemManager(String[] filenames) {
        ItemManager itemManager = null;
        for(String filename : filenames) {
            for (String path : modsFound) {
                String filePath = path + "/data/" + filename;
                try {
                    FileHandle modFile = Game.getInternal(filePath);
                    if (modFile.exists() && !pathIsExcluded(filePath)) {
                        ItemManager thisModManager = JsonUtil.fromJson(ItemManager.class, modFile);
                        if (itemManager == null) {
                            itemManager = thisModManager;
                        } else if (thisModManager != null)
                            itemManager.merge(thisModManager);
                    }
                } catch (Exception ex) {
                    Gdx.app.error("Delver", "Error loading mod file: " + filePath);
                    Logger.logExceptionToFile(ex);
                }
            }
        }
        return itemManager;
    }

    public MonsterManager loadMonsterManager(String[] filenames) {
        MonsterManager monsterManager = null;
        for(String filename : filenames) {
            for (String path : modsFound) {
                String filePath = path + "/data/" + filename;
                try {
                    FileHandle modFile = Game.getInternal(filePath);
                    if (modFile.exists() && !pathIsExcluded(filePath)) {
                        MonsterManager thisModManager = JsonUtil.fromJson(MonsterManager.class, modFile);
                        if (monsterManager == null) {
                            monsterManager = thisModManager;
                        } else if (thisModManager != null)
                            monsterManager.merge(thisModManager);
                    }
                } catch (Exception ex) {
                    Gdx.app.error("Delver", "Error loading mod file: " + filePath);
                    Logger.logExceptionToFile(ex);
                }
            }
        }
        return monsterManager;
    }

    public TextureAtlas[] getTextureAtlases(String filename) {
        ArrayMap<String, TextureAtlas> combinedAtlases = new ArrayMap<String, TextureAtlas>();

        for(String path : modsFound) {
            try {
                FileHandle modFile = Game.getInternal(path + "/data/" + filename);
                if(modFile.exists() && !pathIsExcluded(path + "/data/" + filename)) {
                    TextureAtlas[] atlases = JsonUtil.fromJson(TextureAtlas[].class, modFile);

                    for (int i = 0; i < atlases.length; i++) {
                        combinedAtlases.put(atlases[i].name, atlases[i]);
                    }
                }
            }
            catch(Exception ex) {
                Gdx.app.error("Delver", "Error loading mod file " + path + "/" + filename);
                Logger.logExceptionToFile(ex);
            }
        }

        TextureAtlas[] atlasArray = new TextureAtlas[combinedAtlases.size];
        for(int i = 0; i < combinedAtlases.size; i++) {
            atlasArray[i] = combinedAtlases.getValueAt(i);
        }

        return atlasArray;
    }

    public TileManager loadTileManager() {
        TileManager combinedTileManager = new TileManager();

        for(String path : modsFound) {
            try {
                FileHandle modFile = Game.getInternal(path + "/data/tiles.dat");
                if(modFile.exists() && !pathIsExcluded(path + "/data/tiles.dat")) {
                    TileManager tileManager = JsonUtil.fromJson(TileManager.class, modFile);
                    if(tileManager.tileData != null) combinedTileManager.tileData.putAll(tileManager.tileData);
                    if(combinedTileManager.tiles != null) combinedTileManager.tiles.putAll(tileManager.tiles);
                }
            }
            catch(Exception ex) {
                Gdx.app.error("Delver", "Error loading mod file " + path + "/data/tiles.dat");
                Logger.logExceptionToFile(ex);
            }
        }

        return combinedTileManager;
    }

    public ShaderManager loadShaderManager() {
        ShaderManager combinedShaders = new ShaderManager();
        ShaderManager.loaded = true;

        for(String path: modsFound) {
            try {
                FileHandle modFile = Game.getInternal(path + "/data/shaders.dat");
                if(modFile.exists() && !pathIsExcluded(path + "/data/shaders.dat")) {
                    ShaderData[] shaders = JsonUtil.fromJson(ShaderData[].class, modFile);
                    for(int i = 0; i < shaders.length; i++) {
                        ShaderData sd = shaders[i];
                        combinedShaders.shaders.put(sd.name, sd);
                    }
                }
            }
            catch(Exception ex) {
                Gdx.app.error("Delver", "Error loading mod file " + path + "/data/shaders.dat: " + ex.getMessage());
                Logger.logExceptionToFile(ex);
            }
        }

        return combinedShaders;
    }

    public LerpedAnimationManager loadAnimationManager() {
        LerpedAnimationManager animationManager = new LerpedAnimationManager();

        for(String path : modsFound) {
            try {
                FileHandle modFile = Game.getInternal(path + "/data/animations.dat");
                if(modFile.exists() && !pathIsExcluded(path + "/data/animations.dat")) {
                    LerpedAnimationManager modManager = JsonUtil.fromJson(LerpedAnimationManager.class, modFile);
                    animationManager.animations.putAll(modManager.animations);
                }
            }
            catch(Exception ex) {
                Gdx.app.error("Delver", "Error loading mod file " + path + "/data/animations.dat");
                Logger.logExceptionToFile(ex);
            }
        }

        animationManager.decorationCharge = animationManager.getAnimation("decorationCharge");

        return animationManager;
    }

    public HashMap<String, LocalizedString> loadLocalizedStrings() {
        HashMap<String, LocalizedString> combinedLocalizedStrings = new HashMap<String, LocalizedString>();

        for(String path : modsFound) {
            try {
                FileHandle modFile = Game.getInternal(path + "/data/strings.dat");
                if(modFile.exists() && !pathIsExcluded(path + "/data/strings.dat")) {
                    HashMap<String, LocalizedString> localizedStrings = JsonUtil.fromJson(HashMap.class, modFile);
                    if (!localizedStrings.isEmpty()) {
                        combinedLocalizedStrings.putAll(localizedStrings);
                    }
                }
            }
            catch(Exception ex) {
                Gdx.app.error("Delver", "Error loading mod file " + path + "/data/strings.dat");
                Logger.logExceptionToFile(ex);
            }
        }

        return combinedLocalizedStrings;
    }

    public GameData loadGameData() {
        GameData gameData = new GameData();

        for(String path : modsFound) {
            try {
                FileHandle modFile = Game.getInternal(path + "/data/game.dat");
                if(modFile.exists() && !pathIsExcluded(path + "/data/game.dat")) {
                    GameData modData = JsonUtil.fromJson(GameData.class, modFile);
                    gameData.merge(modData);
                }
            }
            catch(Exception ex) {
                Gdx.app.error("Delver", "Error loading mod file " + path + "/data/game.dat");
                Logger.logExceptionToFile(ex);
            }
        }

        return gameData;
    }

    public HUDManager loadHUDManager() {
        HUDManager hudManager = new HUDManager();

        for (String path : modsFound) {
            try {
                FileHandle modFile = Game.getInternal(path + DATA_HUD_DAT);
                if (modFile.exists() && !pathIsExcluded(path + DATA_HUD_DAT)) {
                    HUDManager modData = JsonUtil.fromJson(HUDManager.class, modFile);
                    hudManager.merge(modData);
                }
            } catch (Exception ex) {
                Gdx.app.error("Delver", "Error loading mod file " + path + DATA_HUD_DAT);
                Logger.logExceptionToFile(ex);
            }
        }

        return hudManager;
    }

    public GenTheme loadTheme(String filename) {
        GenTheme combinedTheme = new GenTheme();
        for (String path: modsFound) {
            FileHandle modFile = Game.getInternal(path + "/" + filename);
            if (modFile.exists() && !pathIsExcluded(path + "/" + filename)) {
                GenTheme theme = JsonUtil.fromJson(GenTheme.class, modFile);

                if (theme.genInfos != null) {
                    if (combinedTheme.genInfos == null) {
                        combinedTheme.genInfos = new Array<GenInfo>();
                    }
                    combinedTheme.genInfos.addAll(theme.genInfos);
                }

                if (theme.doors != null) {
                    if (combinedTheme.doors == null) {
                        combinedTheme.doors = new Array<Door>();
                    }
                    combinedTheme.doors.addAll(theme.doors);
                }

                if (theme.spawnLights != null) {
                    if (combinedTheme.spawnLights == null) {
                        combinedTheme.spawnLights = new Array<Entity>();
                    }
                    combinedTheme.spawnLights.addAll(theme.spawnLights);
                }

                if (theme.exitUp != null) {
                    combinedTheme.exitUp = theme.exitUp;
                }

                if (theme.exitDown != null) {
                    combinedTheme.exitDown = theme.exitDown;
                }

                if (theme.decorations != null) {
                    if (combinedTheme.decorations == null) {
                        combinedTheme.decorations = new Array<Entity>();
                    }
                    combinedTheme.decorations.addAll(theme.decorations);
                }

                if (theme.surprises != null) {
                    if (combinedTheme.surprises == null) {
                        combinedTheme.surprises = new Array<Entity>();
                    }
                    combinedTheme.surprises.addAll(theme.surprises);
                }

                if (theme.defaultTextureAtlas != null) {
                    combinedTheme.defaultTextureAtlas = theme.defaultTextureAtlas;
                }

                if (theme.painter != null) {
                    combinedTheme.painter = theme.painter;
                }

                if (theme.texturePainters != null) {
                    combinedTheme.texturePainters = theme.texturePainters;
                }

                if (theme.chunkTiles != null) {
                    combinedTheme.chunkTiles = theme.chunkTiles;
                }

                if (theme.mapChunks != null) {
                    combinedTheme.mapChunks = theme.mapChunks;
                }

                if (theme.mapComplexity != null) {
                    combinedTheme.mapComplexity = theme.mapComplexity;
                }

                if (theme.lakes != null) {
                    combinedTheme.lakes = theme.lakes;
                }
            }
        }

        return combinedTheme;
    }

    public boolean checkIfModIsEnabled(String mod) {
        if(modsEnabled == null)
            return true;

        Boolean enabled = modsEnabled.get(mod);
        return enabled == null || enabled;
    }

    // Call refresh after changing this!
    public void setEnabled(String mod, boolean enabled) {
        modsEnabled.put(mod, enabled);
    }

    // Return the last found version of this file handle in the list of mod folders
    public FileHandle findFile(String filename) {
        FileHandle foundHandle = null;

        for(int i = 0; i < modsFound.size; i++) {
            String path = modsFound.get(i);
            if(!pathIsExcluded(path + "/" + filename)) {
                FileHandle modFile = Game.getInternal(path + "/" + filename);
                if (modFile.exists()) foundHandle = modFile;
            }
        }
        if(foundHandle == null) Gdx.app.error("Delver", "Could not find file in any mods: " + filename);
        return foundHandle;
    }

    public boolean pathIsExcluded(String filename) {
        return excludeFiles.contains(filename, false);
    }

    public boolean hasExtraMods() {
        return allMods.size > 1;
    }

    public Array<String> getAllMods() {
        return allMods;
    }

    public WorkshopModData getDataForMod(String modPath) {
        try {
            WorkshopModData data = JsonUtil.fromJson(WorkshopModData.class, new FileHandle(modPath).child("modInfo.json"));
            return data;
        }
        catch(Exception ex) {
            return null;
        }
    }

    public String getModName(String modFolder) {
        if(Game.modManager != null) {
            WorkshopModData data = getDataForMod(modFolder);
            if(data != null && data.title != null && !data.title.isEmpty())
                return data.title;
        }

        FileHandle path = new FileHandle(modFolder);
        return path.name();
    }

    public ArrayMap<FileHandle, FileHandle[]> getFilesForModsWithSuffix(String folder, String suffix) {
        ArrayMap<FileHandle, FileHandle[]> filesByDirectory = new ArrayMap<FileHandle, FileHandle[]>();

        for (String path: modsFound) {
            FileHandle modFile = Game.getInternal(path + "/" + folder);
            if (modFile.exists() && modFile.isDirectory()) {
                FileHandle[] files = findRecursively(modFile, suffix);
                if(files.length > 0) {
                    filesByDirectory.put(modFile, files);
                }
            }
        }

        return filesByDirectory;
    }

    public Array<FileHandle> getFileInAllMods(String file) {
        Array<FileHandle> files = new Array<FileHandle>();
        for (String path: modsFound) {
            FileHandle modFile = Game.getInternal(path + "/" + file);
            if (modFile.exists()) {
                files.add(modFile);
            }
        }
        return files;
    }

    private FileHandle[] findRecursively(FileHandle parent, String suffix) {
        Array<FileHandle> foundFiles = new Array<FileHandle>();
        foundFiles.addAll(parent.list(suffix));

        FileHandle[] files = parent.list();
        for(int i = 0; i < files.length; i++) {
            if(files[i].exists() && files[i].isDirectory()) {
                foundFiles.addAll(findRecursively(files[i], suffix));
            }
        }

        FileHandle[] shrunk = new FileHandle[foundFiles.size];
        for(int i = 0; i < foundFiles.size; i++) {
            shrunk[i] = foundFiles.get(i);
        }

        return shrunk;
    }
}
