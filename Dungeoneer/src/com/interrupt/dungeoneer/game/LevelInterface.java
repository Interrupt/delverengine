package com.interrupt.dungeoneer.game;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.interrupt.dungeoneer.collision.Collision;
import com.interrupt.dungeoneer.entities.Entity;
import com.interrupt.dungeoneer.entities.Light;
import com.interrupt.dungeoneer.entities.Player;
import com.interrupt.dungeoneer.entities.Stairs;
import com.interrupt.dungeoneer.entities.triggers.TriggeredWarp;
import com.interrupt.dungeoneer.tiles.Tile;

import java.util.logging.FileHandler;

public interface LevelInterface {
    void load();
    void load(Level.Source source);
    void loadFromEditor();
    void loadForSplash(String levelFileName);
    void generate(Level.Source source);

    void decorateLevel();

    void initPrefabs(Level.Source source);
    void init(Level.Source source);
    void initEntities(Array<Entity> entityList, Level.Source source);

    void cleanupLightCache();
    void updateLights(Level.Source source);

    Tile getTile(int x, int y);
    void setTile(int x, int y, Tile t);
    void setTileIfUnlocked(int x, int y, Tile t);
    Tile getTileOrNull(int x, int y);
    Tile findWaterTile(float x, float y, float z, Vector3 collision);

    boolean inBounds(int x, int y);

    Color calculateLightColorAt(float x, float y, float z, Color c);
    Color getAmbientTileLighting(float x, float y, float z);
    Color getLightColorAt(float x, float y, float z, Vector3 normal, Color c);
    Color attenuateAreaLightColor(float x, float y, float z, float x2, float y2, float z2, float range, Color lcolor, Color toReturn);
    Color attenuateLightColor(float x, float y, float z, float x2, float y2, float z2, float range, Color lcolor);

    boolean isFree(float x, float y, float z, Vector3 collision, float stepheight, boolean floating, Collision hitLoc);
    Entity checkStandingRoomWithEntities(float x, float y, float z, Vector3 collision, Entity checking);

    float maxFloorHeight(float x, float y, float z, float width);
    Vector3 getSlope(float x, float y, float z, float width);
    float minCeilHeight(float x, float y, float z, float width);

    Entity checkEntityCollision(float x, float y, float z, float width);
    Entity checkEntityCollision(float x, float y, float z, float width, float height);
    Entity checkEntityCollision(float x, float y, float z, Vector3 collision, Entity checking);
    Entity checkEntityCollision(float x, float y, float z, Vector3 collision, Entity checking, Entity ignore);
    Entity getHighestEntityCollision(float x, float y, float z, Vector3 collision, Entity checking);
    Array<Entity> getEntitiesEncroaching(Entity checking);
    Array<Entity> getEntitiesEncroaching2d(float x, float y, float collisionX, float collisionY, Entity checking);
    boolean entitiesAreEncroaching(Entity checking, Entity e);
    Array<Entity> getEntitiesColliding(Entity checking);
    boolean collidesWorldOrEntities(float x, float y, float z, Vector3 collision, Entity checking);
    Array<Entity> getEntitiesColliding(float x, float y, float z, Entity checking);
    Array<Entity> getEntitiesColliding(float x, float y, float z, Vector3 collision, Entity checking);
    Entity checkEntityCollision(float x, float y, float z, float widthX, float widthY, float height, Entity checking);
    Entity checkEntityCollision(float x, float y, float z, float widthX, float widthY, float height, Entity checking, Entity ignore);
    Entity checkItemCollision(float x, float y, float width);

    void setPlayer(Player player);

    boolean canSee(float x, float y, float x2, float y2);
    float canSeeHowMuch(float x, float y, float x2, float y2);
    boolean canSafelySee(float x, float y, float x2, float y2);
    boolean canSeeIncludingDoors(float x, float y, float x2, float y2, float maxDistance);

    Array<Entity> getEntitiesAt(float x, float y, float colSize);
    Array<Entity> getEntitiesAlongLine(float x, float y, float x2, float y2);

    Color GetLightmapAt(float posx, float posy, float posz);

    void spawnMonster();

    void tick(float delta);
    void editorTick(Player p, float delta);

    float maxLowerWallHeight(int x, int y);

    void updateSpatialHash(Player player);
    void updateStaticSpatialHash();
    void addEntityToSpatialHash(Entity e);
    void addEntityToStaticSpatialHash(Entity e);

    void SpawnEntity(Entity e);
    void SpawnNonCollidingEntity(Entity e);
    void addEntity(Entity e);

    void rotate90();
    void paste(Level clip, int offsetx, int offsety);

    void makeEntityIdsUnique(String idPrefix);

    void crop(int selX, int selY, int selWidth, int selHeight);

    Stairs spawnStairs(Stairs.StairDirection direction, float xLoc, float yLoc, float zLoc);
    void trigger(Entity instigator, String triggersId, String triggerValue);

    Array<Entity> getEntitiesById(String id);
    Array<Entity> getEntitiesLikeId(String id);
    Array<Entity> findEntities(Class typeOf, Vector2 position, float range, boolean dynamicEntities, boolean staticEntities, boolean nonCollidingEntities);

    void preSaveCleanup();

    void initAudio();
    void clear();

    // New
    Vector3 getPlayerStartLocation();
    float getPlayerStartRotation();

    void makeLevelFromWarp(TriggeredWarp warp);
    void markRendererDirty();
    void markDirty();

    boolean isLoaded();

    Stairs getStairs(Stairs.StairDirection stairDirection);
    boolean needsSaving();

    void save(FileHandle file);

    Array<Entity> getEntities();
    Array<Entity> getStaticEntities();
    Array<Entity> getNonCollidableEntities();

    String getLevelName();
    String getLoadingScreenBackgroundFilename();

    int getWidth();
    int getHeight();

    int getDifficulty();

    String getTheme();

    void AddLight(Light e);
}
