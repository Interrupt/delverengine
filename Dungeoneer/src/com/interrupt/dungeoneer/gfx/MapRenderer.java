package com.interrupt.dungeoneer.gfx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.interrupt.dungeoneer.GameManager;
import com.interrupt.dungeoneer.entities.Door;
import com.interrupt.dungeoneer.entities.Entity;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.game.Level;
import com.interrupt.dungeoneer.game.Options;
import com.interrupt.dungeoneer.tiles.Tile;
import com.interrupt.managers.TileManager;

import java.util.Random;

import static com.interrupt.dungeoneer.gfx.GlRenderer.uiShader;

/** Class that renders the current level to a texture. */
public class MapRenderer {
    public Level loadedLevel;
    public TextureRegion mapTextureRegion;
    public TextureRegion miniMap;
    public boolean showMap = false;
    public SpriteBatch uiBatch;
    public OrthographicCamera camera2D;
    protected Pixmap map;
    protected Pixmap drawnMap;
    protected Texture mapTexture;

    protected Color tempColor = new Color();
    protected Color tempColor2 = new Color();

    public void init() {
        uiBatch = new SpriteBatch();
        uiBatch.setShader(uiShader);
    }

    public void makeMapTextureForLevel(Level level) {
        Color c = tempColor;

        // draw the base map once
        if (map == null || level.mapIsDirty) {
            map = new Pixmap(level.width * 4, level.height * 4, Pixmap.Format.RGBA8888);
            drawnMap = new Pixmap(level.width * 4, level.height * 4, Pixmap.Format.RGBA8888);

            map.setColor(new Color(0f, 0f, 0f, 0f));
            map.fill();

            drawnMap.setColor(new Color(0f, 0f, 0f, 0f));
            drawnMap.fill();

            for (int xx = 0; xx < level.width; xx++) {
                for (int yy = 0; yy < level.height; yy++) {
                    drawMapTileBackground(xx, yy, level, drawnMap, c);
                }
            }

            for (int xx = 0; xx < level.width; xx++) {
                for (int yy = 0; yy < level.height; yy++) {
                    drawMapTileLines(xx, yy, level, drawnMap, c);

                    // make sure the initial seen tiles get drawn next
                    Tile t = level.getTile(xx, yy);
                    if (t != null && t != Tile.solidWall && !t.IsSolid() && t.seen) {
                        level.dirtyMapTiles.add(new Vector2(xx, yy));
                    }
                }
            }

            drawMapDoors(level, drawnMap, c);

            if (mapTexture != null) {
                mapTexture.dispose();
            }

            mapTexture = new Texture(GetNextPowerOf2(level.width * 4), GetNextPowerOf2(level.height * 4), map.getFormat());
            mapTexture.setWrap(Texture.TextureWrap.ClampToEdge, Texture.TextureWrap.ClampToEdge);
            mapTextureRegion = new TextureRegion(mapTexture, 0, 0, level.width * 4, level.height * 4);
            miniMap = new TextureRegion(mapTexture, 0, 0, 19, 19);
        }

        // draw seen tiles
        map.setBlending(Pixmap.Blending.SourceOver);
        for (Vector2 tileLoc : level.dirtyMapTiles) {
            for (int xx = (int) tileLoc.x - 1; xx <= (int) tileLoc.x + 1; xx++) {
                for (int yy = (int) tileLoc.y - 1; yy <= (int) tileLoc.y + 1; yy++) {
                    updateMapTileVisability(xx, yy, level, map, drawnMap, c);
                }
            }
        }

        map.setColor(Color.GREEN);
        if (level.up != null) {
            int xLoc = (int) level.up.x;
            int yLoc = (int) level.up.y;
            if (level.getTile(xLoc, yLoc).seen)
                map.drawRectangle(xLoc * 4, yLoc * 4, 4, 4);
        }
        if (level.down != null) {
            int xLoc = (int) level.down.x;
            int yLoc = (int) level.down.y;
            if (level.getTile(xLoc, yLoc).seen)
                map.drawRectangle(xLoc * 4, yLoc * 4, 4, 4);
        }

        if (mapTextureRegion != null)
            mapTextureRegion.setRegion(0, 0, level.width * 4, level.height * 4);

        if (mapTexture != null)
            mapTexture.draw(map, 0, 0);

        level.dirtyMapTiles.clear();
        level.mapIsDirty = false;
    }

    public void drawMapTileBackground(int xx, int yy, Level level, Pixmap map, Color c) {
        Tile t = level.getTile(xx, yy);

        Random r = new Random();
        if (t != null && t != Tile.solidWall && !t.IsSolid() && !t.floorAndCeilingAreSameHeight()) {
            if (t.IsFree()) c.set(Color.GRAY);
            if (t.IsFree() && t.getMinOpenHeight() < 0.6f) c.set(Color.GRAY).mul(0.55f);
            if (t.IsFree() && t.data.isWater) c.set(0, 0.5f, 1.0f, 1);

            Color mapColor = TileManager.instance.getMapColor(t);
            if (t.IsFree() && mapColor != null) c.set(mapColor);

            for (int yyy = 0; yyy <= 3; yyy++) {
                int xStart, xStop;
                switch (t.tileSpaceType) {
                    case OPEN_SW:
                        xStart = 0;
                        xStop = yyy;
                        break;
                    case OPEN_NW:
                        xStart = 0;
                        xStop = 3 - yyy;
                        break;
                    case OPEN_NE:
                        xStart = yyy;
                        xStop = 3;
                        break;
                    case OPEN_SE:
                        xStart = 4 - yyy;
                        xStop = 3;
                        break;
                    default:
                        xStop = 3;
                        xStart = 0;
                }

                // draw floor
                for (int xxx = xStart; xxx <= xStop; xxx++) {
                    float randomColorOffset = r.nextFloat() * 0.08f;
                    float rColor = c.r - randomColorOffset;
                    float gColor = c.g - randomColorOffset;
                    float bColor = c.b - randomColorOffset;
                    if (rColor < 0) rColor = 0;
                    if (gColor < 0) gColor = 0;
                    if (bColor < 0) bColor = 0;

                    map.drawPixel(xx * 4 + xxx, yy * 4 + yyy, Color.rgba8888(rColor, gColor, bColor, 1f));
                }
            }
        }
    }

    public void drawMapTileLines(int xx, int yy, Level level, Pixmap map, Color c) {
        Tile t = level.getTile(xx, yy);
        Color cc = tempColor2;

        if (t != null && t != Tile.solidWall && !t.IsSolid() && !t.floorAndCeilingAreSameHeight()) {

            map.setBlending(Pixmap.Blending.SourceOver);

            if (t.tileSpaceType != Tile.TileSpaceType.EMPTY && t.tileSpaceType != Tile.TileSpaceType.SOLID) {
                int lineStartX = 0;
                int lineStartY = 0;
                int lineEndX = 3;
                int lineEndY = 3;

                switch (t.tileSpaceType) {
                    case OPEN_SE:
                        lineStartY = 3;
                        lineEndY = 0;
                        break;
                    case OPEN_SW:
                        lineStartX = 3;
                        lineEndX = 0;
                        lineStartY = 3;
                        lineEndY = 0;
                        break;
                    case OPEN_NW:
                        lineStartX = 3;
                        lineEndX = 0;
                        lineStartY = 0;
                        lineEndY = 3;
                        break;
                }

                map.setColor(Color.BLACK);
                map.drawLine(lineStartX + xx * 4, lineStartY + yy * 4, lineEndX + xx * 4, lineEndY + yy * 4);
            }

            Tile westTile = level.getTileOrNull(xx - 1, yy);
            Tile eastTile = level.getTileOrNull(xx + 1, yy);
            Tile northTile = level.getTileOrNull(xx, yy - 1);
            Tile southTile = level.getTileOrNull(xx, yy + 1);

            if (!t.isWestSolid()) {
                float heightDifference = westTile == null ? 0 : westTile.getMaxFloorHeight() - t.getMaxFloorHeight();
                if (westTile == null || westTile.isEastSolid() || heightDifference >= 0.16f) {
                    if (westTile == null || westTile.isEastSolid()) cc.set(Color.BLACK);
                    else if (heightDifference > 0.5f) cc.set(0, 0, 0, 0.5f);
                    else if (heightDifference > 0.18f) cc.set(0, 0, 0, 0.25f);
                    else cc.set(0, 0, 0, 0.1f);

                    map.setColor(cc);

                    for (int yyy = 0; yyy < 4; yyy++) {
                        map.drawPixel(xx * 4 - 1, yy * 4 + yyy);
                    }
                }
            }
            if (!t.isEastSolid()) {
                float heightDifference = eastTile == null ? 0 : eastTile.getMaxFloorHeight() - t.getMaxFloorHeight();
                if (eastTile == null || eastTile.isWestSolid() || heightDifference >= 0.16f) {
                    if (eastTile == null || eastTile.isWestSolid()) cc.set(Color.BLACK);
                    else if (heightDifference > 0.5f) cc.set(0, 0, 0, 0.5f);
                    else if (heightDifference > 0.18f) cc.set(0, 0, 0, 0.25f);
                    else cc.set(0, 0, 0, 0.1f);

                    map.setColor(cc);

                    for (int yyy = 0; yyy < 4; yyy++) {
                        map.drawPixel(xx * 4 + 3 + 1, yy * 4 + yyy);
                    }
                }
            }
            if (!t.isNorthSolid()) {
                float heightDifference = northTile == null ? 0 : northTile.getMaxFloorHeight() - t.getMaxFloorHeight();
                if (northTile == null || northTile.isSouthSolid() || heightDifference >= 0.16f) {
                    if (northTile == null || northTile.isSouthSolid()) cc.set(Color.BLACK);
                    else if (heightDifference > 0.5f) cc.set(0, 0, 0, 0.5f);
                    else if (heightDifference > 0.18f) cc.set(0, 0, 0, 0.25f);
                    else cc.set(0, 0, 0, 0.1f);

                    map.setColor(cc);

                    for (int xxx = 0; xxx < 4; xxx++) {
                        map.drawPixel(xx * 4 + xxx, yy * 4 - 1);
                    }
                }
            }
            if (!t.isSouthSolid()) {
                float heightDifference = southTile == null ? 0f : southTile.getMaxFloorHeight() - t.getMaxFloorHeight();
                if (southTile == null || southTile.isNorthSolid() || heightDifference >= 0.16f) {
                    if (southTile == null || southTile.isNorthSolid()) cc.set(Color.BLACK);
                    else if (heightDifference > 0.5f) cc.set(0, 0, 0, 0.5f);
                    else if (heightDifference > 0.18f) cc.set(0, 0, 0, 0.25f);
                    else cc.set(0, 0, 0, 0.1f);

                    map.setColor(cc);

                    for (int xxx = 0; xxx < 4; xxx++) {
                        map.drawPixel(xx * 4 + xxx, yy * 4 + 3 + 1);
                    }
                }
            }
        }
    }

    public void drawMapDoors(Level level, Pixmap map, Color c) {
        for(Entity e : level.entities) {
            if(e instanceof Door) {
                Door d = (Door)e;
                if(d.doorDirection == Door.DoorDirection.NORTH || d.doorDirection == Door.DoorDirection.SOUTH) {
                    int offset = 1;
                    if(d.doorDirection == Door.DoorDirection.NORTH) offset = 0;
                    map.drawLine((int)((d.startLoc.x) * 4) - offset, (int)((d.startLoc.y - d.collision.y) * 4), (int)((d.startLoc.x) * 4) - offset, (int)((d.startLoc.y + d.collision.y) * 4));
                }
                else {
                    int offset = 1;
                    if(d.doorDirection == Door.DoorDirection.EAST) offset = 0;
                    map.drawLine((int)((d.startLoc.x - d.collision.x) * 4), (int)((d.startLoc.y) * 4) - offset, (int)((d.startLoc.x + d.collision.x) * 4), (int)((d.startLoc.y) * 4) - offset);
                }
            }
        }
    }

    public void updateMapTileVisability(int xx, int yy, Level level, Pixmap map, Pixmap drawnMap, Color c) {
        Tile t = level.getTile(xx, yy);

        // update the initial tile viz
        if (t != null && t != Tile.solidWall && t.seen) {
            // now make sure nearby walls get drawn too
            Tile westTile = level.getTileOrNull(xx - 1, yy);
            Tile eastTile = level.getTileOrNull(xx + 1, yy);
            Tile northTile = level.getTileOrNull(xx, yy - 1);
            Tile southTile = level.getTileOrNull(xx, yy + 1);

            boolean westSeen = westTile == null || westTile.seen;
            boolean eastSeen = eastTile == null || eastTile.seen;
            boolean northSeen = northTile == null || northTile.seen;
            boolean southSeen = southTile == null || southTile.seen;

            for (int xxx = 0; xxx < 4; xxx++) {
                for (int yyy = 0; yyy < 4; yyy++) {
                    c.set(drawnMap.getPixel(xxx + xx * 4, yyy + yy * 4));

                    if (!t.IsSolid() && !t.floorAndCeilingAreSameHeight()) {
                        if (!westSeen) c.a *= (1 - ((4 - xxx) / 4f)) * 0.75f + 0.1f;
                        if (!eastSeen) c.a *= (((4 - xxx) / 4f)) * 0.75f + 0.1f;
                        if (!northSeen) c.a *= (1 - ((4 - yyy) / 4f)) * 0.75f + 0.1f;
                        if (!southSeen) c.a *= (((4 - yyy) / 4f)) * 0.75f + 0.1f;
                    }

                    map.setBlending(Pixmap.Blending.None);
                    map.setColor(c);
                    map.drawPixel(xxx + xx * 4, yyy + yy * 4);
                }
            }
        }
    }

    public int GetNextPowerOf2(int v) {
        v--;
        v |= v >> 1;
        v |= v >> 2;
        v |= v >> 4;
        v |= v >> 8;
        v |= v >> 16;
        v++;

        return v;
    }

    public void draw() {
        Game game = Game.instance;
        if (game != null) {
            loadedLevel = game.level;
        }

        Camera camera2D = GameManager.renderer.camera2D;

        Gdx.gl.glDisable(GL20.GL_CULL_FACE);
        uiBatch.setProjectionMatrix(camera2D.combined);

        if (loadedLevel != null && (loadedLevel.mapIsDirty || loadedLevel.dirtyMapTiles.size > 0))
            makeMapTextureForLevel(loadedLevel);

        if (miniMap != null && mapTexture != null && !Options.instance.hideUI) {
            miniMap.setRegion(game.player.x - 10, game.player.y - 10, 20, 20);

            float startX = game.player.x - 10 + 0.5f;
            float startY = game.player.y - 10 + 0.5f;
            float endX = startX + 20;
            float endY = startY + 20;

            int mapWidth = mapTexture.getWidth() / 4;
            int mapHeight = mapTexture.getHeight() / 4;

            miniMap.setRegion(startX / mapWidth, startY / mapHeight, endX / mapWidth, endY / mapHeight);

            float mapSize = 175f * Options.instance.uiSize * Game.getDynamicUiScale();

            uiBatch.begin();

            uiBatch.setColor(1f, 0.8f, 0.6f, 0.75f);
            uiBatch.draw(miniMap, camera2D.viewportWidth / 2f - mapSize * 1.05f, camera2D.viewportHeight / 2f - mapSize - mapSize * 0.05f, mapSize, mapSize);

            uiBatch.setColor(1f, 1f, 1f, 0.4f);

            uiBatch.end();
        }
    }
}
