package com.interrupt.dungeoneer.generator.rooms;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.interrupt.dungeoneer.entities.Entity;
import com.interrupt.dungeoneer.game.Level;
import com.interrupt.dungeoneer.generator.RoomGenerator;
import com.interrupt.dungeoneer.gfx.Material;
import com.interrupt.dungeoneer.tiles.Tile;
import com.interrupt.managers.EntityManager;
import com.noise.PerlinNoise;

import java.util.Random;

public class HazardRoom extends Room {

    public HazardRoom(int x, int y, int width, int height, Random random, Room parent) {
        super(x, y, width, height, random, parent);
        this.canContainRooms = false;
        this.canDecorate = false;
        this.makeExits = false;
        this.floorHeight = -0.75f;
    }

    byte floorTex = 3;

    @Override
    public void fill(RoomGenerator generator) {

        ceilTex = new Material("t1", (byte)4);
        ceilTex.heightNoiseAmount = 1f;
        ceilTex.tileNoiseAmount = 0.5f;

        wallTex = ceilTex;

        boolean isLava = generator.random.nextFloat() < 0.2f;
        if (isLava) {
            floorTex = 24;
        }
        else {
            floorTex = 3;
        }

        if(parent != null) {
            floorHeight = parent.floorHeight - 0.25f;
            ceilingHeight = parent.ceilingHeight;
        }

        // lava floor height can be lower
        if(isLava) floorHeight -= generator.random.nextInt(8) * 0.2f;

        ceilingHeight += random.nextInt(8) * 0.2f;

        for(int cx = x; cx < x + width; cx++) {
            for(int cy = y; cy < y + height; cy++) {
                generator.carveTile(cx, cy, this);
                Tile t = generator.getTile(cx, cy);
                t.floorTexAtlas = "t1";
                t.floorTex = floorTex;
                t.init(Level.Source.LEVEL_START);
            }
        }

        /*if(!northDoor) {
            Tile t = generator.getTile(x + 1, y);
            t.floorHeight = t.ceilHeight - 0.01f;
        }
        if(!southDoor) {
            Tile t = generator.getTile(x + 1, y + height - 1);
            t.floorHeight = t.ceilHeight - 0.01f;
        }*/
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

    @Override
    public void decorate(RoomGenerator generator) {
        Array<Vector2> waterfallLocations = new Array<Vector2>();

        Array<Vector2> corners = new Array<Vector2>();
        corners.add(new Vector2(x, y));
        corners.add(new Vector2(x + width, y));
        corners.add(new Vector2(x, y + height));
        corners.add(new Vector2(x + width, y + height));

        // find some waterfall locations along the edge
        for(int cx = x; cx < x + width; cx++) {
            for(int cy = y; cy < y + height; cy++) {
                int distance = distanceToWall(cx, cy, generator);
                if (distance == 1 && generator.random.nextFloat() > 0.75f) {
                    Vector2 loc = new Vector2(cx, cy);
                    if(!corners.contains(loc, false)) waterfallLocations.add(loc);
                }
            }
        }

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

                    Tile opposite = generator.getTile(wx + (int)picked.x - wx, wy + (int)picked.y - wy);

                    if((opposite == null || opposite.blockMotion) && !generator.isTileLocked((int)picked.x, (int)picked.y)) {
                        Tile waterfall = generator.carveTile((int) picked.x, (int) picked.y, this);
                        if (waterfall != null) {
                            waterfall.floorTexAtlas = "t1";
                            waterfall.floorTex = floorTex;
                            waterfall.floorHeight = ceilingHeight - 0.25f;
                            waterfall.init(Level.Source.LEVEL_START);
                        }
                    }
                }
            }
        }
    }
}
