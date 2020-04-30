package com.interrupt.dungeoneer.editor.selection;

import com.interrupt.dungeoneer.tiles.Tile;

public class AdjacentTileSelectionInfo {
    public enum AdjacencyDirection {
        NORTH,
        SOUTH,
        EAST,
        WEST
    }

    public int x;
    public int y;
    public Tile tile;
    public AdjacencyDirection dir;

    public AdjacentTileSelectionInfo() {}

    public AdjacentTileSelectionInfo(int x, int y, Tile tile, AdjacencyDirection direction) {
        this.x = x;
        this.y = y;
        this.tile = tile;
        this.dir = direction;
    }
}
