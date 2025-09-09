package com.interrupt.dungeoneer.generator.rooms;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;
import com.interrupt.dungeoneer.entities.Entity;
import com.interrupt.dungeoneer.entities.ParticleEmitter;
import com.interrupt.dungeoneer.generator.RoomGenerator;
import com.interrupt.dungeoneer.generator.rooms.themes.RoomTheme;
import com.interrupt.dungeoneer.gfx.Material;
import com.interrupt.dungeoneer.tiles.Tile;
import com.interrupt.helpers.Tuple;
import com.interrupt.managers.EntityManager;

import java.util.Random;

public class SewerRoom extends Room {

    public SewerRoom(int x, int y, int width, int height, Random random, Room parent, RoomTheme theme) {
        super(x, y, width, height, random, parent);
        this.theme = theme;
        this.canContainRooms = random.nextBoolean();
    }

    public static float WATER_HEIGHT = 0.25f;
    public static final Material WATER_MATERIAL = new Material("sewer", (byte)6);

    private float waterHeightOffset = 0f;

    @Override
    public void fill(RoomGenerator generator) {
        ceilingHeight = floorHeight + theme.pickRoomHeight(generator);

        wallTex = theme.pickWallMaterial(generator);
        lowerWallTex = theme.pickLowerWallMaterial(generator);
        floorTex = theme.pickFloorMaterial(generator);
        ceilTex = theme.pickCeilingMaterial(generator);

        // paint outside tiles
        for(int cx = x - 1; cx < x + width + 1; cx++) {
            generator.paintTile(cx, y - 1, this);
            generator.paintTile(cx, y + height, this);
        }

        for(int cy = y; cy < y + height; cy++) {
            generator.paintTile(x - 1, cy, this);
            generator.paintTile(x + width, cy, this);
        }

        int randHeight = random.nextInt(3);
        ceilingHeight += randHeight * 0.2f;

        // randomly adjust floor height?
        floorHeight -= random.nextInt(3);

        //waterHeightOffset = random.nextInt(3);

        // carve space
        for(int cx = x; cx < x + width; cx++) {
            for(int cy = y; cy < y + height; cy++) {
                Tile c = generator.carveTile(cx, cy, this);
                c.floorHeight = -0.5f;
                c.ceilHeight = c.floorHeight + 1f + randHeight * 0.2f;
            }
        }
    }

    @Override
    public void finalize(RoomGenerator generator) {
        Array<Vector2> tilesToCarve = new Array<Vector2>();

        // fill!
        for(int i = 0; i < tilesToCarve.size; i++) {
            Vector2 v = tilesToCarve.get(i);
            generator.deleteTile((int)v.x, (int)v.y);
        }
        tilesToCarve.clear();

        // fill in center
        for(int cx = x; cx < x + width; cx++) {
            for(int cy = y; cy < y + height; cy++) {
                int fillDistance = generator.random.nextInt(2) + 4;
                int distance = distanceToWall(cx, cy, generator);
                if(distance >= fillDistance) {
                    tilesToCarve.add(new Vector2(cx, cy));
                }
            }
        }

        // fill!
        for(int i = 0; i < tilesToCarve.size; i++) {
            Vector2 v = tilesToCarve.get(i);
            generator.deleteTile((int)v.x, (int)v.y);
        }

        // make water!
        for(int cx = x; cx < x + width; cx++) {
            for(int cy = y; cy < y + height; cy++) {
                int distance = distanceToWall(cx, cy, generator);
                if(distance >= 2) {
                    Tile t = generator.getTile(cx, cy);
                    if(t != null) {
                        t.floorTexAtlas = WATER_MATERIAL.texAtlas;
                        t.floorTex = WATER_MATERIAL.tex;
                        t.floorHeight = floorHeight;
                        t.floorHeight -= WATER_HEIGHT + waterHeightOffset;
                        t.ceilHeight = ceilingHeight;
                    }
                }
            }
        }

        if(floorHeight < -0.5f) {
            makeBridges(generator, generator.random.nextInt(4) + 1);
        }
    }

    private void makeBridges(RoomGenerator generator, int numBridges) {
        // Mark islands!
        for(int xx = 0; xx < width; xx++) {
            for(int yy = 0; yy < height; yy++) {
                markIslands(xx + x, yy + y, generator, null);
            }
        }

        for(int bi = 0; bi < islands.size - 1; bi++) {
            Array<Vector2> startIsland = islands.get(bi);
            Array<Vector2> endIsland = islands.get(bi + 1);

            //Vector2 closestStart = null;
            //Vector2 closestEnd = null;
            int bestDistance = 100;

            // keep track of a number of possible bridge locations
            Array<Tuple<Vector2>> bridgeLocations = new Array<Tuple<Vector2>>();
            for(int i = 0; i < numBridges; i++) {
                bridgeLocations.add(new Tuple<Vector2>());
            }

            for(int si = 0; si < startIsland.size; si++) {
                Vector2 start = startIsland.get(si);
                for(int ei = 0; ei < endIsland.size; ei++) {
                    Vector2 end = endIsland.get(ei);
                    if(start.x == end.x || start.y == end.y) {
                        int dist = (int)Math.abs(start.x - end.x) + (int)Math.abs(start.y - end.y);
                        if(dist <= bestDistance) {
                            if(bestDistance == 100 || generator.random.nextFloat() > 0.8f) {
                                bestDistance = dist;
                                //closestStart = start;
                                //closestEnd = end;

                                Tuple t = bridgeLocations.get(random.nextInt(numBridges));
                                t.val1 = start;
                                t.val2 = end;
                            }
                        }
                    }
                }
            }

            for(int si = 0; si < bridgeLocations.size; si++) {
                Vector2 closestStart = bridgeLocations.get(si).val1;
                Vector2 closestEnd = bridgeLocations.get(si).val2;

                if(closestStart == null || closestEnd == null) continue;

                int startX = (int)Math.min(closestStart.x, closestEnd.x);
                int endX = (int)Math.max(closestStart.x, closestEnd.x);
                int startY = (int)Math.min(closestStart.y, closestEnd.y);
                int endY = (int)Math.max(closestStart.y, closestEnd.y);

                for(int sx = startX; sx <= endX; sx++) {
                    for(int sy = startY; sy <= endY; sy++) {
                        Tile t = generator.getTile(sx, sy);
                        if(t != null && WATER_MATERIAL.equals(t.floorTexAtlas, t.floorTex)) {
                            Entity e = EntityManager.instance.getEntity("Misc Meshes", "Wood Bridge");
                            e.x = sx + 0.5f;
                            e.y = sy + 0.5f;
                            e.z = -0.125f;
                            generator.level.entities.add(e);
                        }
                        generator.lockTile(sx, sy);
                    }
                }
            }
        }
    }

    @Override
    public void decorate(RoomGenerator generator) {
        Array<Vector2> waterfallLocations = new Array<Vector2>();

        // find some waterfall locations along the edge
        for(int cx = x; cx < x + width; cx++) {
            for(int cy = y; cy < y + height; cy++) {
                int distance = distanceToWall(cx, cy, generator);
                if(distance == 1 && generator.random.nextFloat() > 0.85) {
                    waterfallLocations.add(new Vector2(cx, cy));
                }
            }
        }

        Array<Entity> addedBars = new Array<Entity>();

        // make waterfalls
        for(int i = 0; i < waterfallLocations.size; i++) {
            Vector2 loc = waterfallLocations.get(i);
            Tile t = generator.getTile((int)loc.x, (int)loc.y);
            if(t != null) {
                Array<Vector2> around = generator.getCardinalLocationsAround((int) loc.x, (int) loc.y);
                Array<Vector2> solidTiles = new Array<Vector2>();

                for(int v = 0; v < around.size; v++) {
                    Vector2 l = around.get(v);

                    if(generator.tileIsSolid(generator.getTile((int)l.x, (int)l.y))) {
                        solidTiles.add(l);
                    }
                }

                if(solidTiles.size > 0) {
                    solidTiles.shuffle();
                    Vector2 picked = solidTiles.first();

                    int wx = (int)loc.x;
                    int wy = (int)loc.y;

                    Tile opposite = generator.getTile(wx + wx - (int)picked.x, wy + wy - (int)picked.y);
                    if(opposite != null && WATER_MATERIAL.equals(opposite.floorTexAtlas, opposite.floorTex) && !generator.isTileLocked((int) picked.x, (int) picked.y)) {
                        Tile waterfall = generator.carveTile((int) picked.x, (int) picked.y, this);
                        if (waterfall != null) {

                            float waterfallHeight = ceilingHeight - (ceilingHeight - floorHeight) * generator.random.nextFloat() * 0.5f;

                            if(generator.random.nextBoolean()) {
                                // Make a waterfall
                                waterfall.floorTexAtlas = WATER_MATERIAL.texAtlas;
                                waterfall.floorTex = WATER_MATERIAL.tex;
                                waterfall.ceilHeight = waterfallHeight;
                                waterfall.floorHeight = waterfallHeight - WATER_HEIGHT;

                                t.floorTexAtlas = WATER_MATERIAL.texAtlas;
                                t.floorTex = WATER_MATERIAL.tex;
                                t.floorHeight -= WATER_HEIGHT;
                                t.ceilHeight = ceilingHeight;
                            }
                            else {
                                // Make an alcove
                                t.floorTexAtlas = WATER_MATERIAL.texAtlas;
                                t.floorTex = WATER_MATERIAL.tex;
                                t.floorHeight -= WATER_HEIGHT;
                                t.ceilHeight = ceilingHeight;

                                waterfall.floorTexAtlas = WATER_MATERIAL.texAtlas;
                                waterfall.floorTex = WATER_MATERIAL.tex;
                                waterfall.floorHeight = t.floorHeight;
                                waterfall.ceilHeight = waterfall.floorHeight + WATER_HEIGHT;

                                float xDistance = (wx - (int)picked.x) * 0.4f;
                                float yDistance = (wy - (int)picked.y) * 0.4f;

                                Entity e = EntityManager.instance.getEntity("Misc Meshes", "Bars - Half");
                                if(e != null) {
                                    e.x = picked.x + 0.5f + xDistance;
                                    e.y = picked.y + 0.5f + yDistance;
                                    e.z = waterfall.ceilHeight - e.collision.z + 0.5f;

                                    if(Math.abs(xDistance) > Math.abs(yDistance)) {
                                        e.rotate90();
                                    }

                                    addedBars.add(e);
                                }
                            }

                            makeWaterfallEffects(picked, new Vector2(picked).add((wx - (int)picked.x), (wy - (int)picked.y)), generator);
                            makeWaterfallEffects(new Vector2(picked).add((wx - (int)picked.x), (wy - (int)picked.y)), new Vector2(picked).add((wx - (int)picked.x) * 2, (wy - (int)picked.y) * 2), generator);
                        }
                    }
                }
            }
        }

        // Fix to make sure bars still make sense now
        for(int i = 0; i < addedBars.size; i++) {
            Entity bars = addedBars.get(i);
            Tile t = generator.getTile((int)bars.x, (int)bars.y);
            if(t.ceilHeight + 0.48f > bars.z + bars.collision.z) {
                bars.isActive = false;
            }
            else {
                generator.level.SpawnEntity(bars);
            }
        }
    }

    public int distanceToWall(int wx, int wy, RoomGenerator generator) {
        for(int checkDist = 1; checkDist < Math.max(width, height); checkDist++) {
            for(int xx = wx - checkDist; xx <= wx + checkDist; xx++) {
                for(int yy = wy - checkDist; yy <= wy + checkDist; yy++) {
                    if(!generator.tileIsEmpty(generator.getTile(xx, yy))) return checkDist;
                }
            }
        }

        return 1;
    }

    IntMap<Integer> islandMarkers = new IntMap<Integer>();
    IntMap<Array<Vector2>> islands = new IntMap<Array<Vector2>>();

    public void markIslands(int sx, int sy, RoomGenerator generator, Integer islandNum) {
        Tile c = generator.getTile(sx, sy);

        int tileKey = sx + sy * generator.level.height;

        // stop if out of bounds
        if(sx < x || sx >= x + width || sy < y || sy >= y + height) return;

        // stop if the tile is null, solid, or water
        if(c == null || WATER_MATERIAL.equals(c.floorTexAtlas, c.floorTex) || c.blockMotion) return;

        if(!islandMarkers.containsKey(tileKey)) {

            // this tile is unmarked! set an island group, or pass on the existing one
            if(islandNum == null) islandNum = islands.size;

            // mark the tile with the current group
            islandMarkers.put(tileKey, islandNum);

            // make the new island group, if it doesn't exist yet
            if(!islands.containsKey(islandNum)) {
                islands.put(islandNum, new Array<Vector2>());
            }

            // add this tile to the corresponding island
            islands.get(islandNum).add(new Vector2(sx, sy));

            // now go mark all the tiles around this one
            markIslands(sx + 1, sy, generator, islandNum);
            markIslands(sx - 1, sy, generator, islandNum);
            markIslands(sx, sy - 1, generator, islandNum);
            markIslands(sx, sy + 1, generator, islandNum);
        }
    }

    public static void makeWaterfallEffects(Vector2 start, Vector2 end, RoomGenerator generator) {
        Tile topTile = generator.level.getTile((int)start.x, (int)start.y);
        Tile bottomTile = generator.level.getTile((int)(end.x), (int)(end.y));

        float distance = topTile.floorHeight - bottomTile.floorHeight;

        float xDistance = start.x - end.x;
        float yDistance = start.y - end.y;

        Tile bot = bottomTile;

        if(topTile.floorHeight < bottomTile.floorHeight) {
            bot = topTile;
            xDistance *= 2;
            yDistance *= 2;
        }

        if(distance == 0) return;

        Entity e = EntityManager.instance.getEntity("Particles", "Waterfall Clouds");
        if(e != null && e instanceof ParticleEmitter) {
            ParticleEmitter emitter = (ParticleEmitter)e;
            emitter.x = end.x + 0.5f + xDistance * 0.35f;
            emitter.y = end.y + 0.5f + yDistance * 0.35f;
            emitter.z = bot.floorHeight + 0.5f;

            if(Math.abs(xDistance) < Math.abs(yDistance)) {
                emitter.rotate90();
            }

            generator.level.SpawnEntity(emitter);
        }
    }
}
