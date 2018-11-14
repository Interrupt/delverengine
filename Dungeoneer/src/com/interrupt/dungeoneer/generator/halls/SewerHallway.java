package com.interrupt.dungeoneer.generator.halls;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.interrupt.dungeoneer.editor.EditorMarker;
import com.interrupt.dungeoneer.entities.Entity;
import com.interrupt.dungeoneer.entities.Ladder;
import com.interrupt.dungeoneer.game.Level;
import com.interrupt.dungeoneer.generator.GenInfo;
import com.interrupt.dungeoneer.generator.RoomGenerator;
import com.interrupt.dungeoneer.generator.rooms.SewerRoom;
import com.interrupt.dungeoneer.gfx.Material;
import com.interrupt.dungeoneer.tiles.Tile;
import com.interrupt.managers.EntityManager;

public class SewerHallway extends Hallway {

    public SewerHallway(boolean vertical, Vector2 start, Vector2 end, Vector2 heights) {
        super(vertical, start, end, heights);
    }

    Array<Vector2> possibleLadderPositions = new Array<Vector2>();

    public static final Material WATER_MATERIAL = new Material("sewer", (byte)6);

    boolean putLadderInMiddle = false;

    public void carve(RoomGenerator generator) {

        float startHeight = heights.x;
        float endHeight = heights.y;

        if(end.x == 8f && end.y == 16f) {
            hallPositions.reverse();
        }
        if(end.x == 16f && end.y == 8f) {
            hallPositions.reverse();
        }

        // Won't work because of the directions being wonky
        if(generator.random.nextFloat() > 0.7f) {
            endHeight = generator.getRoom().floorHeight;
            putLadderInMiddle = true;
        }

        float hallCeilHeight = Math.max(startHeight + 1f, endHeight + 1f);
        float stepHeight = (endHeight - startHeight) / (float)(hallPositions.size > 2 ? hallPositions.size - 1 : hallPositions.size);

        int i = 0;
        for (Vector2 p : hallPositions) {
            Tile t = generator.carveHallway((int) p.x, (int) p.y);
            t.wallBottomTexAtlas = t.wallTexAtlas;
            t.wallBottomTex = t.wallTex;

            if(i >= hallPositions.size / 2) {
                t.floorHeight = endHeight;
            }
            else {
                t.floorHeight = startHeight;
            }

            // find a place to make a ladder, if needed
            if(generator.getRoom().floorHeight < 0f && t.floorHeight == generator.getRoom().floorHeight) {
                possibleLadderPositions.add(p);
            }

            /*if (t != null) {
                if(hallPositions.size == 1) {
                    t.floorHeight = (endHeight - startHeight / 2f) + startHeight;
                }
                else {
                    t.floorHeight = i * stepHeight + startHeight;
                }

                if (t.ceilHeight < hallCeilHeight && t.ceilHeight - t.floorHeight < 1.5f) {
                    t.ceilHeight = hallCeilHeight;
                }
            }*/

            i++;
        }

        Array<Vector2> carvedSpots = new Array<Vector2>();

        // carve out some extra space
        for (int ii = 0; ii < hallPositions.size - 1; ii++) {
            Vector2 cur = hallPositions.get(ii);

            for (int xx = -1; xx <= 1; xx += 2) {
                for (int yy = -1; yy <= 1; yy += 2) {
                    if ((int) cur.x + xx >= 0 && (int) cur.x + xx < generator.level.width && (int) cur.y + yy >= 0 && (int) cur.y + yy < generator.level.height) {
                        Tile t = generator.getTile(xx + (int) cur.x, yy + (int) cur.y);
                        if(t == null || (!generator.tileIsLiquid(t) && !WATER_MATERIAL.equals(t.floorTexAtlas, t.floorTex))) {
                            Tile carved = generator.carveHallway(xx + (int) cur.x, yy + (int) cur.y);
                            carved.wallBottomTexAtlas = carved.wallTexAtlas;
                            carved.wallBottomTex = carved.wallTex;
                            carved.ceilHeight = hallCeilHeight;

                            generator.paintTile(xx + (int) cur.x, yy + (int) cur.y, generator.getRoom());

                            if (carved != null && generator.theme.defaultFloorMaterial != null) {
                                carved.floorTex = generator.theme.defaultFloorMaterial.tex;
                                carved.floorTexAtlas = generator.theme.defaultFloorMaterial.texAtlas;

                                carvedSpots.add(new Vector2(xx + (int) cur.x, yy + (int) cur.y));
                            }
                        }
                    }
                }
            }
        }

        // make middle water
        for (int ii = 0; ii < hallPositions.size; ii++) {
            Vector2 cur = hallPositions.get(ii);
            Tile curTile = generator.getTile((int) cur.x, (int) cur.y);
            if(curTile != null) {
                curTile.floorTexAtlas = WATER_MATERIAL.texAtlas;
                curTile.floorTex = WATER_MATERIAL.tex;
                curTile.floorHeight -= SewerRoom.WATER_HEIGHT;
                curTile.ceilHeight += 0.3;
                curTile.init(Level.Source.EDITOR);
            }
        }

        carvedSpots.shuffle();

        for(int ci = 0; ci < 2 && ci < carvedSpots.size; ci++) {
            Vector2 picked = carvedSpots.get(ci);
            Tile t = generator.level.getTile((int)picked.x, (int)picked.y);
            if(t != null && !WATER_MATERIAL.equals(t.floorTexAtlas, t.floorTex)) {
                Array<Vector2> around = generator.getCardinalLocationsAround((int)picked.x, (int)picked.y);

                boolean goodSpot = false;
                for(int ii = 0; ii < around.size; ii++) {
                    Vector2 p = around.get(ii);
                    if(generator.tileIsSolid(generator.getTile((int)p.x, (int)p.y))) goodSpot = true;
                }

                if(goodSpot)
                    generator.level.editorMarkers.add(new EditorMarker(GenInfo.Markers.torch, (int) picked.x, (int) picked.y));
            }
        }
    }

    public Vector2 getGoodLadderSpot(Array<Vector2> possiblePositions, RoomGenerator generator) {
        possibleLadderPositions.shuffle();

        for(int ix = 0; ix < possibleLadderPositions.size; ix++) {
            Vector2 picked = possibleLadderPositions.get(ix);

            Array<Vector2> around = generator.getCardinalLocationsAround((int)picked.x, (int)picked.y);
            boolean goodSpot = false;
            for(int ii = 0; ii < around.size; ii++) {
                Vector2 p = around.get(ii);
                if(!generator.tileIsLiquid(generator.getTile((int)p.x, (int)p.y))) goodSpot = true;
            }

            if(goodSpot) {
                // lock this spot from changing
                generator.lockTile((int)picked.x, (int)picked.y);
                for(int li = 0; li < around.size; li++) {
                    Vector2 p = around.get(li);
                    generator.lockTile((int)p.x, (int)p.y);
                }

                return picked;
            }
        }

        return null;
    }

    public void finalize(RoomGenerator generator) {

        int ladderHeight = -(int)generator.getRoom().floorHeight;
        if(ladderHeight <= 0) return;

        // Make a ladder out
        Vector2 picked = putLadderInMiddle ? hallPositions.get(hallPositions.size / 2) : hallPositions.get(hallPositions.size - 1);

        Tile curTile = generator.getTile((int)picked.x, (int)picked.y);
        curTile.floorHeight = generator.getRoom().floorHeight - SewerRoom.WATER_HEIGHT;
        curTile.lockTile();

        possibleLadderPositions.removeValue(picked, true);

        Ladder l = new Ladder();
        l.x = picked.x + 0.5f;
        l.y = picked.y + 0.5f;
        generator.level.SpawnEntity(l);
        generator.level.decorateWallWith(l, true, false);
        l.z = -1f;

        for (int ii = 1; ii <= Math.abs(ladderHeight); ii++) {
            Ladder ln = new Ladder();
            ln.x = l.x;
            ln.y = l.y;

            if(ladderHeight > 0)
                ln.z = -1f - ii;
            else
                ln.z = -1f + ii;

            ln.collision.set(l.collision);
            ln.rotation.set(l.rotation);
            generator.level.SpawnEntity(ln);
        }

        // Make a bridge somewhere, maybe
        if(generator.random.nextFloat() > 0.6f) {
            picked = getGoodLadderSpot(possibleLadderPositions, generator);
            if (picked != null) {
                possibleLadderPositions.removeValue(picked, true);
                Entity e = EntityManager.instance.getEntity("Misc Meshes", "Wood Bridge");
                e.x = picked.x + 0.5f;
                e.y = picked.y + 0.5f;
                e.z = -0.125f;
                generator.level.entities.add(e);
            }
        }

        try {
            int middleIndex = (hallPositions.size / 2) - 1;
            Vector2 waterfallStart = hallPositions.get(middleIndex);
            Vector2 waterfallEnd = hallPositions.get(middleIndex + 1);

            // add waterfall effects
            SewerRoom.makeWaterfallEffects(waterfallStart, waterfallEnd, generator);
        }
        catch(Exception ex) {
            // whoops. couldn't make the waterfall effect there
        }
    }
}
