package com.interrupt.dungeoneer.generator.rooms;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.interrupt.dungeoneer.game.Level;
import com.interrupt.dungeoneer.generator.RoomGenerator;
import com.interrupt.dungeoneer.generator.rooms.themes.RoomTheme;
import com.interrupt.dungeoneer.tiles.Tile;

import java.util.Random;

public class CaveRoom extends Room {

    private class DrunkWalkCursor {
        private int width;
        private int height;

        public Vector2 cursorPos = new Vector2();
        private WalkDirection lastWalkDir = null;
        private RoomGenerator generator;

        public DrunkWalkCursor(int width, int height, RoomGenerator generator) {
            this.width = width;
            this.height = height;
            this.generator = generator;

            cursorPos.set(generator.random.nextInt(width), generator.random.nextInt(height));
        }

        public Array<WalkDirection> getValidWalkDirections(int x, int y, WalkDirection lastWalkDir) {
            Array<WalkDirection> directions = new Array<WalkDirection>();
            if(y > 0) directions.add(WalkDirection.north);
            if(y < height) directions.add(WalkDirection.south);
            if(x < width) directions.add(WalkDirection.east);
            if(x > 0) directions.add(WalkDirection.west);

            if(lastWalkDir != null) directions.removeValue(lastWalkDir, false);

            return directions;
        }

        public Vector2 step() {
            int curWalkX = (int)cursorPos.x;
            int curWalkY = (int)cursorPos.y;

            Array<WalkDirection> directions = getValidWalkDirections(curWalkX, curWalkY, lastWalkDir);
            if(directions.size > 0) {
                directions.shuffle();
                WalkDirection picked = directions.first();

                lastWalkDir = picked;

                if (picked == WalkDirection.east) {
                    curWalkX++;
                } else if (picked == WalkDirection.west) {
                    curWalkX--;
                } else if (picked == WalkDirection.north) {
                    curWalkY--;
                } else if (picked == WalkDirection.south) {
                    curWalkY++;
                }
            }

            return cursorPos.set(curWalkX, curWalkY);
        }

    }

    private enum WalkDirection { north, south, east, west }

    private DrunkWalkCursor caveCarver = null;
    private DrunkWalkCursor waterCarver = null;

    public CaveRoom(int x, int y, int width, int height, Random random, Room parent, RoomTheme theme) {
        super(x, y, width, height, random, parent);
        this.theme = theme;
    }

    public void fill(RoomGenerator generator) {

        caveCarver = new DrunkWalkCursor(width, height, generator);
        waterCarver = new DrunkWalkCursor(width, height, generator);

        ceilingHeight = floorHeight + theme.pickRoomHeight(generator);

        wallTex = theme.pickWallMaterial(generator);
        lowerWallTex = theme.pickLowerWallMaterial(generator);
        floorTex = theme.pickFloorMaterial(generator);
        ceilTex = theme.pickCeilingMaterial(generator);

        ceilTex.heightNoiseAmount = 0.3f;
        floorTex.heightNoiseAmount = 0.05f;
        floorTex.heightNoiseFrequency = 3f;

        int walkAmount = width * height * 2;

        // Carve the cave!
        for(int i = 0; i < walkAmount; i++) {
            Vector2 cursorPos = caveCarver.step();
            generator.carveTile(x + (int)cursorPos.x, y + (int)cursorPos.y, this);
        }

        boolean floodFloor = generator.random.nextFloat() > 0.7f;

        // Adjust floor heights a bit to be more interesting
        boolean moveFloorDown = generator.random.nextBoolean();
        for(int xx = -1; xx <= width + 1; xx++) {
            for(int yy = -1; yy <= height + 1; yy++) {
                Tile t = generator.getTile(x + xx, y + yy);

                if(generator.tileIsEmpty(t)) {
                    int wallDist = distanceToWall(xx, yy, generator) - 1;

                    if(moveFloorDown) {
                        t.floorHeight -= wallDist * 0.1f;
                    }
                    else {
                        t.floorHeight += wallDist * 0.1f;
                    }

                    if(moveFloorDown) {
                        if(generator.random.nextBoolean()) t.ceilHeight += wallDist * 0.2f;
                        else t.ceilHeight -= wallDist * 0.175f;
                    }
                    else {
                        t.ceilHeight += wallDist * 0.2f;
                    }

                    if(wallDist >= 1) {
                        if(generator.random.nextFloat() < 0.1f) {
                            generator.deleteTile(xx + x, yy + y);
                        }
                    }
                }
                else {
                    // Carve out alcoves
                    if(generator.random.nextFloat() > 0.7f && hasAdjacentOpenSpace(xx, yy, generator)) {
                        t = generator.carveTile(xx + x, yy + y, this);
                        t.floorHeight += 0.4f;
                        t.ceilHeight = t.floorHeight + 0.35f;

                        t.floorTexAtlas = t.ceilTexAtlas;
                        t.floorTex = t.ceilTex;
                    }
                }

                if(t != null) {
                    t.floorHeight -= generator.random.nextInt(3) * 0.1f;
                    if(generator.random.nextBoolean()) t.ceilHeight += generator.random.nextInt(6) * 0.1f;
                    else t.ceilHeight -= generator.random.nextInt(3) * 0.1f;

                    if(generator.random.nextFloat() < 0.6f) {
                        t.ceilSlopeNE += generator.random.nextInt(1) * 0.1f;
                        t.ceilSlopeNW += generator.random.nextInt(1) * 0.1f;
                        t.ceilSlopeSE += generator.random.nextInt(1) * 0.1f;
                        t.ceilSlopeSW += generator.random.nextInt(1) * 0.1f;
                    }
                }
            }
        }

        // Place water / lava / pits
        if(generator.random.nextFloat() > 0.4f) {
            boolean isLava = generator.random.nextBoolean();
            int lavaWalkAmount = walkAmount + generator.random.nextInt(Math.max(1, (int)(walkAmount * 0.5f)));
            for (int i = 0; i < lavaWalkAmount; i++) {
                Vector2 cursorPos = waterCarver.step();
                Tile t = generator.getTile(x + (int) cursorPos.x, y + (int) cursorPos.y);
                if (t == null || generator.tileIsSolid(t) && !isEmptySurrounding((int) cursorPos.x, (int) cursorPos.y, generator)) {
                    // make water!
                    t = generator.carveTile(x + (int) cursorPos.x, y + (int) cursorPos.y, this);
                    if(isLava) {
                        t.floorTexAtlas = "t1";
                        t.floorHeight -= 1f;
                        t.floorTex = 24;
                    }
                    else {
                        t.floorTexAtlas = "t1";
                        t.floorHeight -= 4f;
                        t.floorTex = 37;
                    }

                    t.ceilHeight += generator.random.nextInt(14) * 0.1f;
                }
            }
        }
    }

    public int distanceToWall(int wx, int wy, RoomGenerator generator) {
        for(int checkDist = 1; checkDist < Math.max(width, height); checkDist++) {
            for(int xx = wx - checkDist; xx <= wx + checkDist; xx++) {
                for(int yy = wy - checkDist; yy <= wy + checkDist; yy++) {
                    if(!generator.tileIsEmpty(generator.getTile(x + xx, y + yy))) return checkDist;
                }
            }
        }

        return 1;
    }

    public boolean isEmptySurrounding(int wx, int wy, RoomGenerator generator) {
        return generator.tileIsEmpty(generator.getTile(wx + x + 1, wy + y)) &&
        generator.tileIsEmpty(generator.getTile(wx + x - 1, wy + y)) &&
        generator.tileIsEmpty(generator.getTile(wx + x, wy + y + 1)) &&
        generator.tileIsEmpty(generator.getTile(wx + x, wy + y - 1));
    }

    public boolean hasAdjacentOpenSpace(int wx, int wy, RoomGenerator generator) {
        if(generator.tileIsEmpty(generator.getTile(wx + x + 1, wy + y))) return true;
        if(generator.tileIsEmpty(generator.getTile(wx + x - 1, wy + y))) return true;
        if(generator.tileIsEmpty(generator.getTile(wx + x, wy + y + 1))) return true;
        if(generator.tileIsEmpty(generator.getTile(wx + x, wy + y - 1))) return true;
        return false;
    }

    public Array<WalkDirection> getValidWalkDirections(int wx, int wy, WalkDirection lastWalkDir) {
        Array<WalkDirection> directions = new Array<WalkDirection>();
        if(wy > 0) directions.add(WalkDirection.north);
        if(wy < height) directions.add(WalkDirection.south);
        if(wx < width) directions.add(WalkDirection.east);
        if(wx > 0) directions.add(WalkDirection.west);

        if(lastWalkDir != null) directions.removeValue(lastWalkDir, false);

        return directions;
    }
}
