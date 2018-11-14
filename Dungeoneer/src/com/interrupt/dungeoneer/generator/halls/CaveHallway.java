package com.interrupt.dungeoneer.generator.halls;

import com.badlogic.gdx.math.Vector2;
import com.interrupt.dungeoneer.editor.EditorMarker;
import com.interrupt.dungeoneer.generator.GenInfo;
import com.interrupt.dungeoneer.generator.RoomGenerator;
import com.interrupt.dungeoneer.tiles.Tile;

public class CaveHallway extends Hallway {
    public CaveHallway(boolean vertical, Vector2 start, Vector2 end, Vector2 heights) {
        super(vertical, start, end, heights);
    }

    public void carve(RoomGenerator generator) {
        float startHeight = heights.x;
        float endHeight = heights.y;

        float hallCeilHeight = Math.max(startHeight + 1f, endHeight + 1f);
        float stepHeight = (endHeight - startHeight) / (float)(hallPositions.size > 2 ? hallPositions.size - 1 : hallPositions.size);

        int i = 0;
        for (Vector2 p : hallPositions) {
            Tile t = generator.carveHallway((int) p.x, (int) p.y);
            generator.paintTile((int)p.x ,(int)p.y, generator.getRoom());

            if (t != null) {
                if(hallPositions.size == 1) {
                    t.floorHeight = (endHeight - startHeight / 2f) + startHeight;
                }
                else {
                    t.floorHeight = i * stepHeight + startHeight;
                }

                if (t.ceilHeight < hallCeilHeight && t.ceilHeight - t.floorHeight < 1.5f) {
                    t.ceilHeight = hallCeilHeight;
                }

                t.ceilHeight += generator.random.nextInt(4) * 0.04f;

                i++;
            }
        }

        // make ramps!
        if(stepHeight < 0.15f) {
            boolean lastWasSameX = true;
            boolean lastWasSameY = true;

            for (int ii = 0; ii < hallPositions.size - 1; ii++) {
                Vector2 cur = hallPositions.get(ii);
                Vector2 next = hallPositions.get(ii + 1);

                Tile curTile = generator.getTile((int) cur.x, (int) cur.y);
                Tile nextTile = generator.getTile((int) next.x, (int) next.y);
                if (curTile == null || nextTile == null) continue;

                boolean sameX = cur.x == next.x;
                boolean sameY = cur.y == next.y;

                float heightDifference = Math.abs(nextTile.floorHeight - curTile.floorHeight);
                if (heightDifference <= 0.22f) {
                    if (sameX && lastWasSameX) {
                        if (cur.y > next.y) {
                            curTile.slopeNW = curTile.slopeNE = nextTile.floorHeight - curTile.floorHeight;
                        } else {
                            curTile.slopeSW = curTile.slopeSE = nextTile.floorHeight - curTile.floorHeight;
                        }
                    } else if (sameY && lastWasSameY) {
                        if (cur.x < next.x) {
                            curTile.slopeNW = curTile.slopeSW = nextTile.floorHeight - curTile.floorHeight;
                        } else {
                            curTile.slopeNE = curTile.slopeSE = nextTile.floorHeight - curTile.floorHeight;
                        }
                    } else if (heightDifference <= 0.15f) {
                        nextTile.floorHeight = curTile.floorHeight;
                    }
                }

                generator.paintTile((int)cur.x ,(int)cur.y, generator.getRoom());

                lastWasSameX = sameX;
                lastWasSameY = sameY;
            }
        }

        boolean madeTorch = false;

        // carve out some extra space
        for (int ii = 0; ii < hallPositions.size - 1; ii++) {
            Vector2 cur = hallPositions.get(ii);
            Vector2 next = hallPositions.get(ii + 1);

            Tile curTile = generator.getTile((int)cur.x, (int)cur.y);

            boolean sameX = cur.x == next.x;
            boolean sameY = cur.y == next.y;

            for (int xx = -1; xx <= 1; xx += 2) {
                for (int yy = -1; yy <= 1; yy += 2) {
                    if((int)cur.x + xx >= 0 && (int)cur.x + xx < generator.level.width && (int)cur.y + yy >= 0 && (int)cur.y + yy < generator.level.height) {
                        Tile t = generator.getTile(xx + (int) cur.x, yy + (int) cur.y);
                        if (t == null || generator.tileIsSolid(t) || generator.tileIsLiquid(t) || (generator.tileHurts(t) && generator.random.nextBoolean())) {
                            if (generator.random.nextFloat() > 0.1f) {
                                t = generator.carveHallway(xx + (int) cur.x, yy + (int) cur.y);
                                if (t != null) {
                                    t.floorHeight = curTile.floorHeight + generator.random.nextInt(6) * 0.045f;
                                    t.ceilHeight = curTile.ceilHeight + generator.random.nextInt(6) * 0.04f;
                                    generator.paintTile(xx + (int) cur.x, yy + (int) cur.y, generator.getRoom());

                                    //boolean madeAlcove = t.floorHeight != curTile.floorHeight;
                                    boolean madeAlcove = true;

                                    float slopeMod = 0.175f * generator.random.nextFloat();

                                    if(slopeMod > 0.125f || Math.abs(t.floorHeight - curTile.floorHeight) > 0.1f) {
                                        t.floorTex = t.ceilTex;
                                        t.floorTexAtlas = t.ceilTexAtlas;
                                    }

                                    // slope away from Y
                                    if (sameY) {
                                        if (madeAlcove) {
                                            t.slopeNE += (yy == -1 ? slopeMod : 0f);
                                            t.slopeNW += (yy == -1 ? slopeMod : 0f);
                                            t.slopeSE += (yy == 1 ? slopeMod : 0f);
                                            t.slopeSW += (yy == 1 ? slopeMod : 0f);
                                        }

                                        t.ceilSlopeNE -= (yy == -1 ? slopeMod : 0f);
                                        t.ceilSlopeNW -= (yy == -1 ? slopeMod : 0f);
                                        t.ceilSlopeSE -= (yy == 1 ? slopeMod : 0f);
                                        t.ceilSlopeSW -= (yy == 1 ? slopeMod : 0f);
                                    }

                                    // slope away from X
                                    if (sameX) {
                                        if (madeAlcove) {
                                            t.slopeNE += (xx == -1 ? slopeMod : 0f);
                                            t.slopeNW += (xx == 1 ? slopeMod : 0f);
                                            t.slopeSE += (xx == -1 ? slopeMod : 0f);
                                            t.slopeSW += (xx == 1 ? slopeMod : 0f);
                                        }

                                        t.ceilSlopeNE -= (xx == -1 ? slopeMod : 0f);
                                        t.ceilSlopeNW -= (xx == 1 ? slopeMod : 0f);
                                        t.ceilSlopeSE -= (xx == -1 ? slopeMod : 0f);
                                        t.ceilSlopeSW -= (xx == 1 ? slopeMod : 0f);
                                    }
                                }

                                if (generator.random.nextFloat() > 0.5f && t.hasRoomFor(0.7f) && !madeTorch) {
                                    generator.level.editorMarkers.add(new EditorMarker(GenInfo.Markers.torch, xx + (int) cur.x, yy + (int) cur.y));
                                    madeTorch = true;
                                }
                            }
                        }
                    }
                }
            }

            // make cracks
            for (int xx = -2; xx <= 2; xx += 4) {
                for (int yy = -2; yy <= 2; yy += 4) {
                    if ((int) cur.x + xx >= 0 && (int) cur.x + xx < generator.level.width && (int) cur.y + yy >= 0 && (int) cur.y + yy < generator.level.height) {
                        Tile t = generator.getTile(xx + (int) cur.x, yy + (int) cur.y);
                        if ((t == null || generator.tileIsSolid(t)) && generator.random.nextFloat() > 0.8f) {
                            t = generator.carveHallway(xx + (int) cur.x, yy + (int) cur.y);
                            if (t != null) {
                                t.floorHeight = curTile.floorHeight + 0.5f + generator.random.nextInt(3) * 0.045f;
                                t.ceilHeight = t.floorHeight + 0.2f + generator.random.nextInt(3) * 0.04f;
                                generator.paintTile((int)cur.x ,(int)cur.y, generator.getRoom());
                                t.floorTex = t.ceilTex;
                                t.floorTexAtlas = t.ceilTexAtlas;
                            }
                        }
                    }
                }
            }
        }
    }
}
