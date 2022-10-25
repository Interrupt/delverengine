package com.interrupt.dungeoneer.game.pathfinding;

import com.interrupt.dungeoneer.tiles.Tile;

public class FoundTile {
    public Tile tile;
    public float floorHeight;

    public void set(Tile tile, float floorHeight) {
        this.tile = tile;
        this.floorHeight = floorHeight;
    }
}
