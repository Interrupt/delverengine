package com.interrupt.dungeoneer.generator.halls;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.interrupt.dungeoneer.editor.EditorMarker;
import com.interrupt.dungeoneer.entities.Sprite;
import com.interrupt.dungeoneer.generator.GenInfo;
import com.interrupt.dungeoneer.generator.RoomGenerator;
import com.interrupt.dungeoneer.gfx.Material;
import com.interrupt.dungeoneer.tiles.Tile;

public class TempleHallway extends Hallway {

    ArrayMap<String, Vector2> carvedPositions = new ArrayMap<String, Vector2>();

    public TempleHallway(boolean vertical, Vector2 start, Vector2 end, Vector2 heights) {
        super(vertical, start, end, heights);
    }

    public void carve(RoomGenerator generator) {
        doCarve(generator, heights.x, heights.y, 0, 0);

        if(vertical) {
            doCarve(generator, heights.x, heights.y, 1, 0);
        }
        else {
            doCarve(generator, heights.x, heights.y, 0, 1);
        }
    }

    public void doCarve(RoomGenerator generator, float startHeight, float endHeight, int xOffset, int yOffset) {
        float hallCeilHeight = Math.max(startHeight + 2f, endHeight + 2f);
        float stepHeight = (endHeight - startHeight) / (float)(hallPositions.size > 2 ? hallPositions.size - 1 : hallPositions.size);

        int i = 0;
        for (Vector2 p : hallPositions) {
            Tile t = generator.carveHallway((int) p.x + xOffset, (int) p.y + yOffset);

            if (t != null) {
                if(hallPositions.size == 1) {
                    t.floorHeight = (endHeight - startHeight / 2f) + startHeight;
                }
                else {
                    t.floorHeight = i * stepHeight + startHeight;
                }

                //if (t.ceilHeight < hallCeilHeight && t.ceilHeight - t.floorHeight < 1.5f) {
                    t.ceilHeight = hallCeilHeight;
                //}

                if(generator.theme.defaultFloorMaterial != null) {
                    t.floorTex = generator.theme.defaultFloorMaterial.tex;
                    t.floorTexAtlas = generator.theme.defaultFloorMaterial.texAtlas;
                }

                if(generator.getRoom() != null) {
                    Material lower = generator.getRoom().theme.pickLowerWallMaterial(generator);
                    if(lower != null) {
                        t.wallBottomTex = lower.tex;
                        t.wallBottomTexAtlas = lower.texAtlas;
                    }
                }

                carvedPositions.put(Integer.toString((int)p.x + xOffset + (int)p.y + yOffset), new Vector2(p.x + xOffset, p.y + yOffset));

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

                Tile curTile = generator.getTile((int) cur.x + xOffset, (int) cur.y + yOffset);
                Tile nextTile = generator.getTile((int) next.x + xOffset, (int) next.y + yOffset);
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

                lastWasSameX = sameX;
                lastWasSameY = sameY;
            }
        }
    }

    // override this to take actions after all hallways have been generated
    public void finalize(RoomGenerator generator) {

        Array<Vector2> possibleTorchPositions = new Array<Vector2>();

        for(int i = 0; i < carvedPositions.size; i++) {
            Vector2 pos = carvedPositions.getValueAt(i);
            if(pos == null) continue;

            int xOffset = 0;
            int yOffset = 0;

            // Offset x OR y but not both!
            if(generator.random.nextBoolean()) {
                xOffset = generator.random.nextBoolean() ? -1 : 1;
            }
            else {
                yOffset = generator.random.nextBoolean() ? -1 : 1;
            }

            int xPos = (int)pos.x + xOffset;
            int yPos = (int)pos.y + yOffset;

            if(!generator.level.inBounds(xPos, yPos)) continue;

            Tile found = generator.getTile(xPos, yPos);
            if(generator.tileIsSolid(found)) {

                if(generator.random.nextBoolean()) {
                    Tile column = Tile.EmptyTile();
                    column.wallTex = (byte) 17;
                    column.floorHeight = 0.5f;
                    column.ceilHeight = 0.5f;

                    generator.level.setTile(xPos, yPos, column);

                    possibleTorchPositions.add(pos);
                }
                else {
                    Tile carved = generator.getTile((int)pos.x, (int)pos.y);
                    if(carved != null) {
                        Tile column = Tile.EmptyTile();
                        column.wallTex = (byte) 4;
                        column.wallBottomTex = (byte) 17;
                        column.ceilTex = (byte) 4;
                        column.floorTex = (byte) 22;
                        column.floorHeight = carved.floorHeight + 0.75f;
                        column.ceilHeight = column.floorHeight + 0.4f;

                        generator.level.setTile(xPos, yPos, column);

                        Sprite s = new Sprite();
                        s.isDynamic = false;
                        s.tex = 34;
                        s.spriteAtlas = "entities_undead";
                        s.x = xPos + 0.5f;
                        s.y = yPos + 0.5f;
                        s.z = column.floorHeight + 0.5f;
                        generator.level.SpawnEntity(s);
                    }
                }
            }
        }

        int torchesToMake = possibleTorchPositions.size / 2;
        if(torchesToMake == 0 && possibleTorchPositions.size > 0) torchesToMake = 1;
        for(int i = 0; i < torchesToMake; i++) {
            Vector2 pos = possibleTorchPositions.get(generator.random.nextInt(possibleTorchPositions.size));
            possibleTorchPositions.removeValue(pos, true);
            generator.level.editorMarkers.add(new EditorMarker(GenInfo.Markers.torch, (int)pos.x, (int)pos.y));
        }
    }
}
