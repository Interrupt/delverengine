package com.interrupt.dungeoneer.generator.rooms;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.interrupt.dungeoneer.entities.Prefab;
import com.interrupt.dungeoneer.generator.RoomGenerator;
import com.interrupt.dungeoneer.generator.rooms.themes.RoomTheme;
import com.interrupt.dungeoneer.gfx.Material;
import com.interrupt.dungeoneer.tiles.Tile;

import java.util.Random;

public class TempleRoom extends Room {

    private Room lavaRoom = null;

    private Material columnMaterial = new Material("temple", (byte)5);

    public TempleRoom(int x, int y, int width, int height, Random random, Room parent, RoomTheme theme) {
        super(x, y, width / (random.nextBoolean() ? 2 : 1), height / (random.nextBoolean() ? 2 : 1), random, parent);
        this.theme = theme;
        canContainRooms = true;

        // Lava room info!
        lavaRoom = new HazardRoom(0, 0, 0, 0, random, this);
        RoomTheme lavaRoomTheme = new RoomTheme();

        Material lavaRoomCeilingMaterial = new Material();
        lavaRoomCeilingMaterial.texAtlas = "temple";
        lavaRoomCeilingMaterial.tex = 12;
        lavaRoomCeilingMaterial.heightNoiseAmount = 1.5f;

        Material lavaRoomFloorMaterial = new Material();
        lavaRoomFloorMaterial.tex = 24;
        
        Material lavaRoomWallMaterial = new Material();
        lavaRoomWallMaterial.texAtlas = "temple";
        lavaRoomWallMaterial.tex = 12;

        lavaRoomTheme.ceilings.add(lavaRoomCeilingMaterial);
        lavaRoomTheme.floors.add(lavaRoomFloorMaterial);
        lavaRoomTheme.walls.add(lavaRoomWallMaterial);

        lavaRoom.ceilTex = lavaRoomCeilingMaterial;
        lavaRoom.floorTex = lavaRoomFloorMaterial;
        lavaRoom.wallTex = theme.walls.first();

        lavaRoom.theme = lavaRoomTheme;
        lavaRoom.floorHeight = -2.5f + random.nextFloat() * 1.5f;
        lavaRoom.ceilingHeight = 3f - random.nextFloat() * 2f;

        if(random.nextFloat() < 0.1f) {
            lavaRoom.floorHeight -= random.nextInt(13) + 2;
        }
    }

    @Override
    public void fill(RoomGenerator generator) {
        ceilingHeight = floorHeight + theme.pickRoomHeight(generator);

        wallTex = theme.pickWallMaterial(generator);
        lowerWallTex = theme.pickLowerWallMaterial(generator);
        floorTex = theme.pickFloorMaterial(generator);
        ceilTex = theme.pickCeilingMaterial(generator);

        int randHeight = random.nextInt(3);
        ceilingHeight += randHeight * 0.2f;

        // carve space
        for(int cx = x; cx < x + width; cx++) {
            for(int cy = y; cy < y + height; cy++) {
                if(cx == x || cx == x + width -1 || cy == y || cy == y + height -1) {
                    generator.carveTile(cx, cy, this);
                }
                else {
                    generator.carveTile(cx, cy, this);
                }
            }
        }

        // Make lava on the edges!
        Array<Vector2> lavaTiles = new Array<Vector2>();
        Array<Vector2> lavaFalls = new Array<Vector2>();
        ArrayMap<String, Integer> floorDistances = new ArrayMap<String, Integer>();

        int lavaDistance = generator.random.nextInt(5) + 1;

        for(int xx = -lavaDistance; xx <= width + lavaDistance; xx++) {
            for (int yy = -lavaDistance; yy <= height + lavaDistance; yy++) {
                int floorDist = distanceToFloor(xx, yy, generator);
                if(floorDist > 0 && floorDist <= lavaDistance) {
                    if(floorDist <= lavaDistance / 2) {
                        lavaTiles.add(new Vector2(xx, yy));
                    }
                    else if(floorDist <= 3) {
                        if(generator.random.nextBoolean()) {
                            lavaTiles.add(new Vector2(xx, yy));
                        }
                    }
                    else {
                        if(generator.random.nextBoolean()) {
                            lavaTiles.add(new Vector2(xx, yy));
                        }
                        else if(generator.random.nextBoolean()) {
                            lavaTiles.add(new Vector2(xx, yy));
                            lavaFalls.add(new Vector2(xx, yy));
                        }
                    }

                    floorDistances.put(xx + "," + yy, floorDist);
                }
            }
        }

        boolean raiseLava = generator.random.nextFloat() < 0.3f;

        for(int i = 0; i < lavaTiles.size; i++) {
            Vector2 pos = lavaTiles.get(i);
            if(generator.getTile(x + (int)pos.x, y + (int)pos.y) == null) {
                Tile t = Tile.EmptyTile();

                Material wallMaterial = theme.pickWallMaterial(generator);
                if(wallMaterial != null) {
                    t.wallTex = wallMaterial.tex;
                    t.wallTexAtlas = wallMaterial.texAtlas;
                }

                t.floorTex = 24;
                t.floorHeight = floorHeight - 2;
                t.ceilHeight = ceilingHeight + 2;

                generator.carveTile(x + (int) pos.x, y + (int) pos.y, lavaRoom);

                /*if(raiseLava) {
                    Integer floorDist = floorDistances.get(((int) pos.x) + "," + ((int) pos.y));
                    if (floorDist != null) {
                        t = generator.level.getTile(x + (int) pos.x, y + (int) pos.y);
                        t.floorHeight += floorDist - 1;

                        if (t.ceilHeight - t.floorHeight > 1f) {
                            t.ceilHeight -= floorDist * 0.25f;
                        }
                    }
                }*/

                //generator.level.setTile(x + (int) pos.x, y + (int) pos.y, t);
            }
        }

        for(int i = 0; i < lavaFalls.size; i++) {
            Vector2 pos = lavaFalls.get(i);
            Tile t = generator.getTile(x + (int) pos.x, y + (int) pos.y);
            if(t != null) {
                t.floorHeight = t.ceilHeight - 0.3f;
            }
        }

        Array<Vector2> columnsToMake = new Array<Vector2>();
        Array<Vector2> tilesToFill = new Array<Vector2>();

        if(width > 3 && height > 3 && generator.random.nextBoolean()) {
            // Make a zigguratt thing!

            // TODO: Make lava in middle of lowered ziggurats?

            Array<Vector2> tilesToRaise = new Array<Vector2>();
            Array<Integer> wallDistance = new Array<Integer>();

            for (int xx = -1; xx <= width + 1; xx++) {
                for (int yy = -1; yy <= height + 1; yy++) {
                    int wallDist = distanceToWall(xx, yy, generator) - 1;
                    if (wallDist < 3) {
                        tilesToRaise.add(new Vector2(x + xx, y + yy));
                        wallDistance.add(wallDist);
                    } else {
                        tilesToFill.add(new Vector2(x + xx, y + yy));
                    }
                }
            }

            int ceilMod = generator.random.nextBoolean() ? 1 : -1;
            int floorMod = generator.random.nextBoolean() ? 1 : -1;

            for(int ii = 0; ii < Math.min(tilesToRaise.size, wallDistance.size); ii++) {
                Vector2 pos = tilesToRaise.get(ii);
                Tile t = generator.getTile((int)pos.x, (int)pos.y);
                if(t != null) {
                    int wallDist = wallDistance.get(ii);
                    t.floorHeight += wallDist * 0.2575f * floorMod;
                    t.ceilHeight -= wallDist * 0.2575f * ceilMod;

                    if(floorMod < 0 && wallDist >= 2) {
                        t.floorTex = lavaRoom.floorTex.tex;
                        t.floorTexAtlas = lavaRoom.floorTex.texAtlas;
                        t.floorHeight = lavaRoom.floorHeight;
                    }
                }
            }

            // fill in some columns
            columnsToMake.add(new Vector2(x, y));
            columnsToMake.add(new Vector2(x + width - 1, y));
            columnsToMake.add(new Vector2(x, y + height - 1));
            columnsToMake.add(new Vector2(x + width - 1, y + height - 1));
        }
        else if(width <= 3 || height <= 3) {
            if(width > height) {
                int numColumns = width / 3;
                if(numColumns > 0) {
                    for (int ii = 0; ii <= numColumns; ii++) {
                        int distance = width / numColumns;
                        columnsToMake.add(new Vector2(x + distance * ii, y + height / 2));
                    }
                }
            }
            else {
                int numColumns = height / 3;
                if(numColumns > 0) {
                    for (int ii = 0; ii <= numColumns; ii++) {
                        int distance = height / numColumns;
                        columnsToMake.add(new Vector2(x + width / 2, y + distance * ii));
                    }
                }
            }
        }
        else {
            // Fill in the center!
            int checkDist = 1;
            if (generator.random.nextBoolean()) checkDist = 0;

            for (int xx = -1; xx <= width + 1; xx++) {
                for (int yy = -1; yy <= height + 1; yy++) {
                    int wallDist = distanceToWall(xx, yy, generator) - 1;

                    if (wallDist > checkDist) {

                        if(wallDist > 3) {
                            // skip!
                            Tile t = generator.level.getTile(xx + x, yy + y);
                            if(t != null) t.ceilHeight = ceilingHeight + 0.75f;
                        }
                        else if(width - checkDist > 2 && xx == width / 2) {
                            // skip!
                            Tile t = generator.level.getTile(xx + x, yy + y);
                            if(t != null) t.ceilHeight = ceilingHeight - 0.75f;
                        }
                        else if(height - checkDist > 2 && yy == height / 2) {
                            // skip!
                            Tile t = generator.level.getTile(xx + x, yy + y);
                            if(t != null) t.ceilHeight = ceilingHeight - 0.75f;
                        }
                        else {
                            tilesToFill.add(new Vector2(x + xx, y + yy));
                        }
                    }
                }
            }

            // make columns!
            if(checkDist == 1) {
                columnsToMake.add(new Vector2(x, y));
                columnsToMake.add(new Vector2(x + width - 1, y));
                columnsToMake.add(new Vector2(x, y + height - 1));
                columnsToMake.add(new Vector2(x + width - 1, y + height - 1));
            }
        }

        tilesToFill.addAll(columnsToMake);

        for (int i = 0; i < tilesToFill.size; i++) {
            Vector2 pos = tilesToFill.get(i);
            Tile t = Tile.copy(Tile.emptyWall);
            t.renderSolid = false;

            float mid = (ceilingHeight - floorHeight) / 2f;

            t.floorHeight = floorHeight + mid;
            t.ceilHeight = t.floorHeight;

            Material wallMaterial = theme.pickWallMaterial(generator);
            if (wallMaterial != null) {
                t.wallTex = wallMaterial.tex;
                t.wallTexAtlas = wallMaterial.texAtlas;
                t.wallBottomTex = t.wallTex;
            }

            generator.level.setTile((int) pos.x, (int) pos.y, t);
        }

        for(int i = 0; i < columnsToMake.size; i++) {
            Vector2 pos = columnsToMake.get(i);
            Tile t = generator.level.getTile((int) pos.x, (int) pos.y);
            if(t != null) {
                t.wallTex = columnMaterial.tex;
                t.wallTexAtlas = columnMaterial.texAtlas;
                t.wallBottomTex = columnMaterial.tex;
                t.wallBottomTexAtlas = columnMaterial.texAtlas;
            }
        }
    }

    /*public void fill(RoomGenerator generator) {

        caveCarver = new DrunkWalkCursor(width, height, generator);

        ceilingHeight = floorHeight + theme.pickRoomHeight(generator);

        wallTex = theme.pickWallMaterial(generator);
        lowerWallTex = theme.pickLowerWallMaterial(generator);
        floorTex = theme.pickFloorMaterial(generator);
        ceilTex = theme.pickCeilingMaterial(generator);

        int walkAmount = width * height * 2;

        // Carve the cave!
        for(int i = 0; i < walkAmount; i++) {
            Vector2 cursorPos = caveCarver.step();
            Tile t = generator.getTile(x + (int)cursorPos.x, y + (int)cursorPos.y);
            if(t == null) {
                generator.carveTile(x + (int) cursorPos.x, y + (int) cursorPos.y, this);
            }
        }

        Array<Vector2> tilesToFill = new Array<Vector2>();
        for(int xx = -1; xx <= width + 1; xx++) {
            for (int yy = -1; yy <= height + 1; yy++) {

                int wallDist = distanceToWall(xx, yy, generator) - 1;
                if(wallDist > 1) {
                    tilesToFill.add(new Vector2(x + xx, y + yy));
                    //t = Tile.NewSolidTile();
                    //generator.level.setTile(x + xx, y + yy, t);
                }
            }
        }

        for(int i = 0; i < tilesToFill.size; i++) {
            Vector2 pos = tilesToFill.get(i);
            Tile t = Tile.NewSolidTile();

            Material wallMaterial = theme.pickWallMaterial(generator);
            if(wallMaterial != null) {
                t.wallTex = wallMaterial.tex;
                t.wallTexAtlas = wallMaterial.texAtlas;
            }

            generator.level.setTile((int)pos.x, (int)pos.y, t);
        }
    }*/

    public int distanceToFloor(int wx, int wy, RoomGenerator generator) {
        for(int checkDist = 1; checkDist < Math.max(width, height); checkDist++) {
            for(int xx = wx - checkDist; xx <= wx + checkDist; xx++) {
                for(int yy = wy - checkDist; yy <= wy + checkDist; yy++) {
                    if(generator.tileIsEmpty(generator.getTile(x + xx, y + yy)) && !generator.tileIsLiquid(generator.getTile(x + xx, y + yy))) return checkDist;
                }
            }
        }

        return 0;
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

    public boolean hasAdjacentOpenSpace(int wx, int wy, RoomGenerator generator) {
        if(generator.tileIsEmpty(generator.getTile(wx + x + 1, wy + y))) return true;
        if(generator.tileIsEmpty(generator.getTile(wx + x - 1, wy + y))) return true;
        if(generator.tileIsEmpty(generator.getTile(wx + x, wy + y + 1))) return true;
        if(generator.tileIsEmpty(generator.getTile(wx + x, wy + y - 1))) return true;
        return false;
    }

    public void done(RoomGenerator generator) {
        int lavaMod = generator.random.nextInt(2) + 1;
        int lavaDistance = generator.random.nextInt(2) + 2;

        for(int ix = 0; ix < generator.level.width; ix++) {
            for(int iy = 0; iy < generator.level.height; iy++) {
                Tile t = generator.getTile(ix, iy);
                if(t != null && t.data != null && t.data.hurts > 0) {
                    int floorDistance = distanceToFloor(ix - x, iy - y, generator);
                    if(floorDistance > lavaDistance) {
                        if(t.ceilHeight - (t.floorHeight + floorDistance - lavaMod) > 1f)
                            t.floorHeight += floorDistance - lavaMod;
                    }
                }
            }
        }
    }

    public void decorate(RoomGenerator generator) {
        super.decorate(generator);

        for(int cx = 0; cx < generator.level.width; cx++) {
            for(int cy = 0; cy < generator.level.height; cy++) {
                Tile t = generator.level.getTile(cx, cy);

                if(t != null && t.floorTex == (byte)24 && generator.random.nextFloat() < 0.09f) {

                    Prefab p = new Prefab("Particles", "Lava Motes");
                    p.x = cx + 0.5f;
                    p.y = cy + 0.5f;
                    p.z = t.floorHeight + 4;
                    generator.level.SpawnEntity(p);

                    Prefab f = new Prefab("Particles", "Lava Fountain");
                    f.x = cx + 0.5f;
                    f.y = cy + 0.5f;
                    f.z = t.floorHeight;
                    generator.level.SpawnEntity(f);
                }
            }
        }
    }
}
