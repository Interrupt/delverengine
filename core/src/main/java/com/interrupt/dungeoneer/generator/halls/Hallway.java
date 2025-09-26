package com.interrupt.dungeoneer.generator.halls;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.interrupt.dungeoneer.generator.RoomGenerator;
import com.interrupt.dungeoneer.tiles.Tile;

public class Hallway {
    public Vector2 start;
    public Vector2 end;
    public Vector2 heights;
    public boolean vertical;

    public Array<Vector2> hallPositions = new Array<Vector2>();

    public Hallway(boolean vertical, Vector2 start, Vector2 end, Vector2 heights) {
        this.start = start;
        this.end = end;
        this.heights = heights;
        this.vertical = vertical;

        calculatePath();
    }

    public void setHeight(Vector2 heights) {
        this.heights = heights;
    }

    private int calculatePath() {
        hallPositions.clear();

        int startX = (int)start.x;
        int startY = (int)start.y;
        int endX = (int)end.x;
        int endY = (int)end.y;

        if(vertical) {
            int yMid = (endY - startY) / 2 + startY;

            // vertical
            for(int y = startY; y < yMid; y++) {
                int x = startX;
                hallPositions.add(new Vector2(x, y));
            }

            // horizontal
            if(endX > startX) {
                for (int x = startX; x <= endX; x++) {
                    int y = yMid;
                    hallPositions.add(new Vector2(x,y));
                }
            }
            else {
                for (int x = startX; x >= endX; x--) {
                    int y = yMid;
                    hallPositions.add(new Vector2(x, y));
                }
            }

            // vertical
            for(int y = yMid + 1; y <= endY; y++) {
                int x = endX;
                hallPositions.add(new Vector2(x, y));
            }
        }
        else {
            int xMid = (endX - startX) / 2 + startX;

            // horizontal
            for(int x = startX; x < xMid; x++) {
                int y = startY;
                hallPositions.add(new Vector2(x, y));
            }

            // vertical
            if(endY > startY) {
                for (int y = startY; y <= endY; y++) {
                    int x = xMid;
                    hallPositions.add(new Vector2(x, y));
                }
            }
            else {
                for (int y = startY; y >= endY; y--) {
                    int x = xMid;
                    hallPositions.add(new Vector2(x, y));
                }
            }

            // horizontal
            for(int x = xMid + 1; x <= endX; x++) {
                int y = endY;
                hallPositions.add(new Vector2(x, y));
            }
        }

        return hallPositions.size;
    }

    public void carve(RoomGenerator generator) {

        float startHeight = heights.x;
        float endHeight = heights.y;

        float hallCeilHeight = Math.max(startHeight + 1f, endHeight + 1f);
        float stepHeight = (endHeight - startHeight) / (float)(hallPositions.size > 2 ? hallPositions.size - 1 : hallPositions.size);

        int i = 0;
        for (Vector2 p : hallPositions) {
            Tile t = generator.carveHallway((int) p.x, (int) p.y);

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

                lastWasSameX = sameX;
                lastWasSameY = sameY;
            }
        }
    }

    public int getLength() {
        return hallPositions.size;
    }

    // override this to take actions after all hallways have been generated
    public void finalize(RoomGenerator generator) {

    }
}
