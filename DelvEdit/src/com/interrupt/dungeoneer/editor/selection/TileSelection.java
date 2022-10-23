package com.interrupt.dungeoneer.editor.selection;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.interrupt.dungeoneer.collision.CollisionTriangle;
import com.interrupt.dungeoneer.editor.ControlPoint;
import com.interrupt.dungeoneer.editor.ControlPointVertex;
import com.interrupt.dungeoneer.editor.Editor;
import com.interrupt.dungeoneer.editor.EditorApplication;
import com.interrupt.dungeoneer.game.Level;
import com.interrupt.dungeoneer.gfx.WorldChunk;
import com.interrupt.dungeoneer.partitioning.TriangleSpatialHash;
import com.interrupt.dungeoneer.tiles.Tile;
import com.interrupt.helpers.FloatTuple;
import com.interrupt.helpers.TileEdges;
import com.interrupt.managers.ShaderManager;

import java.util.Iterator;

/** Class representing a collection of Tile objects. */
public class TileSelection implements Iterable<TileSelectionInfo>{
    public int x;
    public int y;
    public int width;
    public int height;

    public int startX;
    public int startY;

    public boolean boundsUseTileHeights = false;

    // Used for drawing the floors and ceilings of this selection
    public WorldChunk floorWorldChunk;
    public WorldChunk ceilWorldChunk;

    public TriangleSpatialHash floorCollisionTriangles = new TriangleSpatialHash(1);
    public TriangleSpatialHash ceilCollisionTriangles = new TriangleSpatialHash(1);

    /** A collection of tiles that are adjacent to the selection. */
    public Iterable<AdjacentTileSelectionInfo> adjacent;

    public TileSelection() {
        clear();

        final TileSelection self = this;
        adjacent = new Iterable<AdjacentTileSelectionInfo>() {
            @Override
            public Iterator<AdjacentTileSelectionInfo> iterator() {
                Level level = Editor.app.getLevel();

                Array<AdjacentTileSelectionInfo> result = new Array<AdjacentTileSelectionInfo>();

                // South
                if (self.y - 1 >= 0) {
                    int j = self.y - 1;
                    for (int i = self.x; i < self.x + self.width; i++) {
                        Tile t = level.getTileOrNull(i, j);
                        result.add(new AdjacentTileSelectionInfo(i, j, t, AdjacentTileSelectionInfo.AdjacencyDirection.SOUTH));
                    }
                }

                // North
                if (self.y + self.height < level.height) {
                    int j = self.y + self.height;
                    for (int i = self.x; i < self.x + self.width; i++) {
                        Tile t = level.getTileOrNull(i, j);
                        result.add(new AdjacentTileSelectionInfo(i, j, t, AdjacentTileSelectionInfo.AdjacencyDirection.NORTH));
                    }
                }

                // West
                if (self.x - 1 >= 0) {
                    int i = self.x - 1;
                    for (int j = self.y; j < self.y + self.height; j++) {
                        Tile t = level.getTileOrNull(i, j);
                        result.add(new AdjacentTileSelectionInfo(i, j, t, AdjacentTileSelectionInfo.AdjacencyDirection.WEST));
                    }
                }

                // East
                if (self.x + self.width < level.width) {
                    int i = self.x + self.width;
                    for (int j = self.y; j < self.y + self.height; j++) {
                        Tile t = level.getTileOrNull(i, j);
                        result.add(new AdjacentTileSelectionInfo(i, j, t, AdjacentTileSelectionInfo.AdjacencyDirection.EAST));
                    }
                }

                return result.iterator();
            }
        };
    }

    public Array<Vector3> getVertexLocations() {
        Array<Vector3> result = new Array<>();
        for(int ix = x; ix < x + width + 1; ix++) {
            for(int iy = y; iy < y + height + 1; iy++) {
                result.add(new Vector3(ix, iy, 0));
            }
        }
        return result;
    }

    public static TileSelection Rect(int x, int y, int width, int height) {
        TileSelection tileSelection = new TileSelection();

        tileSelection.x = x;
        tileSelection.y = y;
        tileSelection.startX = x;
        tileSelection.startY = y;
        tileSelection.width = width;
        tileSelection.height = height;

        return tileSelection;
    }

    public void clear() {
        x = 0;
        y = 0;
        startX = 0;
        startY = 0;
        width = 1;
        height = 1;
    }

    /** Initial tile of selection. */
    public Tile first() {
        return Editor.app.getLevel().getTile(startX, startY);
    }

    public void setStartTile(int tileStartX, int tileStartY) {
        startX = tileStartX;
        startY = tileStartY;
    }

    public boolean contains(int x, int y) {
         return x >= this.x && x < this.x + width && y >= this.y && y < this.y + height;
    }

    private final Array<TileSelectionInfo> tileInfos = new Array<TileSelectionInfo>();
    @Override
    public Iterator<TileSelectionInfo> iterator() {
        tileInfos.clear();

        for (int i = x; i < x + width; i++) {
            for (int j = y; j < y + height; j++) {
                Tile t = Editor.app.getLevel().getTileOrNull(i, j);
                tileInfos.add(new TileSelectionInfo(i, j, t));
            }
        }

        return tileInfos.iterator();
    }

    private final BoundingBox bounds = new BoundingBox();
    public BoundingBox getBounds() {
        // Always recalculate by default
        return getBounds(true);
    }

    public BoundingBox getBounds(boolean recalculate) {
        // Only update the bounds when asked
        if(!recalculate) {
            // Reset bounds, to update the internal size.
            bounds.set(bounds.min, bounds.max);
            return bounds;
        }

        TileSelectionInfo first = null;

        for (TileSelectionInfo info : this) {
            if (info.tile == null) {
                TileSelectionInfo i = new TileSelectionInfo(info);
                i.tile = Tile.solidWall;
                info = i;
            }

            BoundingBox b = getBounds(info);
            if (first == null) {
                bounds.set(b);
                first = info;
            }

            bounds.ext(getBounds(info));
        }

        // Clamp tile selection bounds to the floor / ceiling heights of the first tile
        if(!boundsUseTileHeights) {
            Tile firstTile = first();
            bounds.min.z = firstTile.floorHeight;
            bounds.max.z = firstTile.ceilHeight;
        }

        // Reset bounds, to update the internal size.
        bounds.set(bounds.min, bounds.max);

        return bounds;
    }

    public BoundingBox getBounds(FloatTuple floorPoints, FloatTuple ceilPoints) {
        getBounds();

        bounds.max.z = ceilPoints.max();
        bounds.min.z = floorPoints.min();

        // Reset bounds, to update the internal size.
        bounds.set(bounds.min, bounds.max);
        return bounds;
    }

    private final BoundingBox tileBounds = new BoundingBox();
    private BoundingBox getBounds(TileSelectionInfo info) {
        tileBounds.min.set(info.x, info.y, info.tile.floorHeight);
        tileBounds.max.set(info.x + 1, info.y + 1, info.tile.ceilHeight);

        return tileBounds;
    }

    // Always make that the lowest corner is the start so that the selection size is positive
    // this makes it easier to iterate through the list in a for loop
    public void fixup(float dragDistanceX, float dragDistanceY) {
        float selectionWidth = 1.5f + Math.abs(dragDistanceX);
        float selectionHeight = 1.5f + Math.abs(dragDistanceY);

        if(dragDistanceX < -0.5f)
            x = startX - (int)selectionWidth + 1;
        else
            x = startX;

        if(dragDistanceY < -0.5f)
            y = startY - (int)selectionHeight + 1;
        else
            y = startY;

        width = (int)selectionWidth;
        height = (int)selectionHeight;
    }

    public TileSelection copy() {
        TileSelection copy = new TileSelection();
        copy.x = x;
        copy.y = y;
        copy.startX = startX;
        copy.startY = startY;
        copy.width = width;
        copy.height = height;
        copy.boundsUseTileHeights = boundsUseTileHeights;
        copy.bounds.set(bounds);
        return copy;
    }

    public void initWorldChunks() {
        if(floorWorldChunk != null && ceilWorldChunk != null)
            return;

        floorWorldChunk = new WorldChunk(Editor.app.renderer);
        floorWorldChunk.setOffset(x, y);
        floorWorldChunk.setSize(width, height);
        floorWorldChunk.makeWalls = false;
        floorWorldChunk.makeUpperWalls = false;
        floorWorldChunk.makeCeilings = false;

        ceilWorldChunk = new WorldChunk(Editor.app.renderer);
        ceilWorldChunk.setOffset(x, y);
        ceilWorldChunk.setSize(width, height);
        ceilWorldChunk.makeWalls = false;
        ceilWorldChunk.makeLowerWalls = false;
        ceilWorldChunk.makeFloors = false;

        // tesselate for the first time
        refreshWorldChunks();
    }

    public void refreshWorldChunks() {
        floorCollisionTriangles.Flush();
        ceilCollisionTriangles.Flush();

        if(floorWorldChunk != null) {
            floorWorldChunk.refresh();
            floorWorldChunk.Tesselate(Editor.app.level, Editor.app.renderer, false);
            floorWorldChunk.getTesselators().addCollisionTriangles(floorCollisionTriangles);
        }
        if(ceilWorldChunk != null) {
            ceilWorldChunk.refresh();
            ceilWorldChunk.Tesselate(Editor.app.level, Editor.app.renderer, false);
            ceilWorldChunk.getTesselators().addCollisionTriangles(ceilCollisionTriangles);
        }
    }
}
