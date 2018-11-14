package com.interrupt.dungeoneer.generator.stairs;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.game.Level;
import com.interrupt.dungeoneer.tiles.Tile;

public class StairPattern {
    private static StairPattern[] allStairs = new StairPattern[] {
            new StairPattern()
    };

    public static StairPattern getRandomStairPattern() {
        return allStairs[Game.rand.nextInt(allStairs.length)];
    }

    protected Vector2 offset = new Vector2(0, 1);

    protected short[][] pattern = {
            {  -1, 0, 0 },
            {   1, 1, 0 },
            {  -1, 2, 0 }
    };

    protected Array<short[][]> rotatedPatterns = null;

    public StairPattern() { }

    public StairPattern(Vector2 offset, short[][] pattern) {
        this.offset = offset;
        this.pattern = pattern;
    }

    public float getHeightOffset() {
        float maxHeightOffset = 0f;
        for(int y = 0; y < pattern.length; y++) {
            for (int x = 0; x < pattern[y].length; x++) {
                short matchType = pattern[y][x];
                maxHeightOffset = Math.max(matchType, maxHeightOffset);
            }
        }
        return maxHeightOffset * -0.25f;
    }

    private Vector2 t_matchVector = new Vector2();
    public boolean match(Vector2 pos, Array<Vector2> lakeTiles, Level level) {
        boolean didMatch = true;

        for (int y = 0; y < pattern.length && didMatch; y++) {
            for (int x = 0; x < pattern[y].length && didMatch; x++) {
                short matchType = pattern[y][x];
                Tile existingTile = level.getTile((int)(pos.x + x + offset.x), (int)(pos.y + y + offset.y));
                if (matchType == 1 && existingTile != null) {
                    if(existingTile.blockMotion) {
                        didMatch = false;
                    }
                }
                else if(matchType != -1) {
                    if (!lakeTiles.contains(t_matchVector.set(pos.x + x, pos.y + y).add(offset), false)) {
                        didMatch = false;
                    }
                }
            }
        }

        return didMatch;
    }

    public void applyPattern(Vector2 pos, Array<Vector2> lakeTiles, Level level) {
        Tile edgeTile = level.getTileOrNull((int) pos.x, (int) pos.y);
        if(edgeTile == null) return;

        for(int y = 0; y < pattern.length; y++) {
            for (int x = 0; x < pattern[y].length; x++) {
                short matchType = pattern[y][x];
                if(matchType > 0) {
                    lakeTiles.removeValue(t_matchVector.set((int) pos.x + x, (int) pos.y + y).add(offset), false);

                    Tile t = new Tile();
                    t.floorHeight = edgeTile.floorHeight - matchType * 0.25f;
                    t.ceilHeight = edgeTile.ceilHeight;
                    t.floorTex = edgeTile.floorTex;
                    t.floorTexAtlas = edgeTile.floorTexAtlas;
                    t.ceilTex = edgeTile.ceilTex;
                    t.ceilTexAtlas = edgeTile.ceilTexAtlas;
                    t.lockTile();
                    level.setTile((int) pos.x + x + (int)offset.x, (int) pos.y + y + (int)offset.y, t);
                }
            }
        }
    }
}
