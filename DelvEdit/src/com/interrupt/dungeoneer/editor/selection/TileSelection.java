package com.interrupt.dungeoneer.editor.selection;

import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.interrupt.dungeoneer.editor.Editor;
import com.interrupt.dungeoneer.game.Level;
import com.interrupt.dungeoneer.tiles.Tile;

import java.util.Iterator;

/** Class representing a collection of Tile objects. */
public class TileSelection implements Iterable<TileSelectionInfo>{
    public int x;
    public int y;
    public int width;
    public int height;

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

    /** Clear the tile selection and reset the origin to (0, 0). */
    public void clear() {
        clear(0, 0);
    }

    /** Clear the tile selection and set the origin explicitly. */
    public void clear(int x, int y) {
        this.x = x;
        this.y = y;
        width = 1;
        height = 1;
    }

    /** Initial tile of selection. */
    public Tile first() {
        return Editor.app.getLevel().getTile(x, y);
    }

    public boolean contains(int x, int y) {
         return x >= this.x && x < this.x + width && y >= this.y && y < this.y + height;
    }

    /** Returns whether the tile selection contains level out-of-bounds tiles. */
    public boolean outOfLevelBounds(int width, int height) {
        return (x < 0 || x >= width || y < 0 || y >= height) ? true: false;
    }

    /** Crops the selection to only contain level in-bounds tiles. */
    public void cropToLevelBounds(int width, int height) {
        x = Math.min(width, Math.max(0, x));
        y = Math.min(height, Math.max(0, y));
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

        return bounds;
    }

    private final BoundingBox tileBounds = new BoundingBox();
    private BoundingBox getBounds(TileSelectionInfo info) {
        tileBounds.min.set(info.x, info.y, info.tile.floorHeight);
        tileBounds.max.set(info.x + 1, info.y + 1, info.tile.ceilHeight);

        return tileBounds;
    }
}
