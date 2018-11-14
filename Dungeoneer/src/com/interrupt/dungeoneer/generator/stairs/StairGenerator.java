package com.interrupt.dungeoneer.generator.stairs;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.game.Level;
import com.interrupt.dungeoneer.gfx.Material;
import com.interrupt.dungeoneer.generator.rooms.themes.RoomGeneratorTheme;
import com.interrupt.dungeoneer.tiles.Tile;

public class StairGenerator {

    Array<Vector2> stairLocations = new Array<Vector2>();

    Array<Vector2> directions = new Array<Vector2>();

    RoomGeneratorTheme theme = null;

    RoomGeneratorTheme.LakeType lakeType = RoomGeneratorTheme.LakeType.LAVA;

    public StairGenerator(RoomGeneratorTheme theme) {
        directions.add(new Vector2(1, 0));
        directions.add(new Vector2(-1, 0));
        directions.add(new Vector2(0, 1));
        directions.add(new Vector2(0, -1));
        this.theme = theme;
    }

    public boolean makeStairsAt(Vector2 edgeTile, Array<Vector2> lakeTiles, RoomGeneratorTheme.LakeType type, Level level) {
        Array<Vector2> stairDirections = new Array<Vector2>();
        this.lakeType = type;

        boolean madeStairs = false;
        for(int i = 0; i < directions.size && !madeStairs; i++) {
            madeStairs = makeStep((int) edgeTile.x, (int) edgeTile.y, directions.get(i), lakeTiles, stairDirections, level);
        }
        return stairLocations.size > 0;
    }

    public boolean makeStep(int posX, int posY, Vector2 dir, Array<Vector2> lakeTiles, Array<Vector2> stairDirections, Level level) {
        // try to make another step
        if (dir == null) {
            dir = directions.get(Game.rand.nextInt(directions.size));

            if (stairDirections.size == 2) {
                // keep going in a direction once picked
                dir = stairDirections.get(1);
            }
        }

        // ensure this new tile is actually a lake tile
        if(isLakeTile(posX + (int)dir.x, posY + (int)dir.y, lakeTiles)) {
            // make sure there are lake tiles next to the stairs to climb off of
            Vector2 freeSpaceDir = stairDirections.size == 0 ? dir : stairDirections.get(0);
            boolean hasFreeSpace = isLakeTile(posX + (int)(dir.x + freeSpaceDir.x), posY + (int)(dir.y + freeSpaceDir.y), lakeTiles);
            if(!hasFreeSpace) return false;

            // we want to keep further stairs from cutting off lake access
            if(stairDirections.size != 0) {
                boolean hasLedge = !isLakeTile(posX + (int) (dir.x - freeSpaceDir.x), posY + (int) (dir.y - freeSpaceDir.y), lakeTiles);
                if (!hasLedge) return false;
            }

            stairLocations.add(new Vector2(posX, posY).add(dir));
            stairDirections.add(dir);
            if(Game.rand.nextFloat() < 0.7f && stairLocations.size < 10) {
                boolean madeAdditional = makeStep(posX + (int)dir.x, posY + (int)dir.y, null, lakeTiles, stairDirections, level);
                if(!madeAdditional) makeStep(posX + (int)dir.x, posY + (int)dir.y, directions.get(0), lakeTiles, stairDirections, level);
            }

            return true;
        }

        return false;
    }

    Vector2 t_lakeTileCheck = new Vector2();
    public boolean isLakeTile(int x, int y, Array<Vector2> lakeTiles) {
        t_lakeTileCheck.set(x, y);
        boolean isStairTile = stairLocations.contains(t_lakeTileCheck, false);
        if(isStairTile) return false;
        return lakeTiles.contains(t_lakeTileCheck, false);
    }

    public float getHeightOffset() {
        return stairLocations.size * -0.25f;
    }

    public void applyStairs(Vector2 edgeTile, Array<Vector2> lakeTiles, Level level) {
        Tile tile = level.getTileOrNull((int)edgeTile.x, (int)edgeTile.y);
        if(tile == null) return;

        Material floorMaterial = null;
        Material wallMaterial = null;

        if (theme != null) {
            if (theme.defaultHallwayFloorMaterial != null) {
                floorMaterial = theme.defaultHallwayFloorMaterial;
            }
            if(theme.defaultHallwayWallMaterial != null) {
                wallMaterial = theme.defaultHallwayWallMaterial;
            }
        }

        // Lava uses the cave texture
        if(lakeType == RoomGeneratorTheme.LakeType.LAVA) {
            floorMaterial = new Material("t1", (byte)4);
            wallMaterial = new Material("t1", (byte)4);
        }

        for(int i = 0; i < stairLocations.size; i++) {
            Vector2 stairTile = stairLocations.get(i);
            lakeTiles.removeValue(stairTile, false);

            Tile t = new Tile();
            t.floorHeight = tile.floorHeight - ((i + 1) * 0.25f);
            t.ceilHeight = tile.ceilHeight;
            t.floorTex = tile.floorTex;
            t.floorTexAtlas = tile.floorTexAtlas;
            t.ceilTex = tile.ceilTex;
            t.ceilTexAtlas = tile.ceilTexAtlas;

            if (floorMaterial != null) {
                t.floorTex = floorMaterial.tex;
                t.floorTexAtlas = floorMaterial.texAtlas;
            }

            if(wallMaterial != null) {
                t.wallBottomTex = wallMaterial.tex;
                t.wallBottomTexAtlas = wallMaterial.texAtlas;
            }

            t.lockTile();
            level.setTile((int)stairTile.x, (int)stairTile.y, t);
        }
    }
}
