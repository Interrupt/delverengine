package com.interrupt.dungeoneer.generator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.game.Level;
import com.interrupt.dungeoneer.generator.rooms.themes.RoomGeneratorTheme;
import com.interrupt.dungeoneer.generator.stairs.StairGenerator;
import com.interrupt.dungeoneer.generator.stairs.StairPattern;
import com.interrupt.dungeoneer.tiles.Tile;

public class LakeGenerator {

    RoomGeneratorTheme.LakeType type = RoomGeneratorTheme.LakeType.WATER;

    Tile existingLakeTile = null;

    float advanceChanceReduction = 0.2f;

    public LakeGenerator() { }

    public boolean goodLake = true;

    RoomGeneratorTheme theme = null;

    public LakeGenerator(RoomGeneratorTheme.LakeType type, RoomGeneratorTheme theme, float advanceChanceReduction) {
        this.advanceChanceReduction = advanceChanceReduction;
        this.type = type;
        this.theme = theme;
    }

    public LakeGenerator(RoomGeneratorTheme.LakeType[] types, RoomGeneratorTheme theme) {
        if(types != null && types.length > 0) {
            type = types[Game.rand.nextInt(types.length)];
        }
        this.theme = theme;
    }

    public LakeGenerator(RoomGeneratorTheme.LakeType[] types, RoomGeneratorTheme theme, float advanceChanceReduction) {
        if(types != null && types.length > 0) {
            type = types[Game.rand.nextInt(types.length)];
        }
        this.advanceChanceReduction = advanceChanceReduction;
        this.theme = theme;
    }

    public void makeLakes(Level generated, int num) {
        if(num == 0) return;
        
        Vector2 picked = new Vector2();
        for(int i = 0; i < num; i++) {
            picked.x = Game.rand.nextInt(generated.width);
            picked.y = Game.rand.nextInt(generated.height);
            goodLake = true;

            Tile startTile = generated.getTileOrNull((int)picked.x, (int)picked.y);
            if(startTile == null) {
                Array<Vector2> lakeTiles = new Array<Vector2>();
                Array<Vector2> groundTiles = new Array<Vector2>();
                advanceLake((int)picked.x, (int)picked.y, generated, lakeTiles, groundTiles, 1.0f);

                // something bad happened making this lake!
                if(!goodLake)
                    continue;

                if(groundTiles.size > 3 && lakeTiles.size > 6) {
                    // find min height
                    float minHeight = 100f;
                    float maxHeight = -100f;
                    Array<Vector2> outline = getOutlineTiles(lakeTiles);
                    for(int oi = 0; oi < outline.size; oi++) {
                        Vector2 outlineTile = outline.get(oi);
                        Tile existing = generated.getTileOrNull((int)outlineTile.x, (int)outlineTile.y);
                        if(existing != null && !existing.blockMotion) {
                            minHeight = Math.min(minHeight, existing.getMinFloorHeight() + (existing.floorTex != 37 ? 0f : 10f));
                            maxHeight = Math.max(maxHeight, existing.getCeilingHeight());
                            existing.init(Level.Source.LEVEL_LOAD);

                            if(type != RoomGeneratorTheme.LakeType.PIT) {
                                // maybe change the lake type to match an existing feature
                                if(existing.data.isWater) {
                                    type = RoomGeneratorTheme.LakeType.WATER;
                                    existingLakeTile = existing;
                                }
                            }
                        }
                    }

                    if(minHeight == 100f || maxHeight == 100f) {
                        // not a good lake :(
                        goodLake = false;
                        continue;
                    }

                    if(type != RoomGeneratorTheme.LakeType.PIT) {
                        StairGenerator stairs = makeStairsUp(lakeTiles, outline, generated, minHeight);
                        if (stairs != null) {
                            minHeight += stairs.getHeightOffset() ;
                        }
                    }

                    // make inner tiles
                    for(int li = 0; li < lakeTiles.size; li++) {
                        Vector2 lakeTile = lakeTiles.get(li);
                        paintLakeTile(generated, lakeTile, minHeight, maxHeight);
                    }

                    // paint outline
                    for(int oi = 0; oi < outline.size; oi++) {
                        Vector2 outlineTile = outline.get(oi);
                        Tile existing = generated.getTileOrNull((int)outlineTile.x, (int)outlineTile.y);
                        if(existing != null && !existing.blockMotion) {
                            paintBrinkTile(existing);
                        }
                        else if(existing == null || existing.blockMotion) {
                            paintOutlineTile(generated, outlineTile);
                        }
                    }
                }
            }
        }
    }

    private void paintBrinkTile(Tile existing) {
        if(!existing.isLocked) {
            if (type == RoomGeneratorTheme.LakeType.LAVA) {
                existing.wallBottomTex = 4;
                existing.wallBottomTexAtlas = "t1";
            }
        }

        if(type == RoomGeneratorTheme.LakeType.PIT) {
            existing.init(Level.Source.LEVEL_LOAD);
            if(existing.data.isWater) {
                existing.floorHeight -= 0.3f;
            }
        }
    }

    private void paintLakeTile(Level generated, Vector2 lakeTile, float floorHeight, float ceilHeight) {
        Tile t = new Tile();
        t.blockMotion = false;

        if(type == RoomGeneratorTheme.LakeType.WATER) {
            t.floorTex = 3;
            t.wallTex = 4;
            t.ceilTex = 4;
            t.ceilTex = 4;
            t.floorHeight = floorHeight - 0.25f;
            t.ceilHeight = ceilHeight + 1f + Game.rand.nextInt(8) / 8f;
        }
        else if(type == RoomGeneratorTheme.LakeType.LAVA) {
            t.floorTex = 24;
            t.wallTex = 4;
            t.ceilTex = 4;
            t.ceilTex = 4;
            t.floorHeight = floorHeight - 0.25f;
            t.ceilHeight = ceilHeight + 1f + Game.rand.nextInt(8) / 8f;
        }
        else if(type == RoomGeneratorTheme.LakeType.PIT) {
            t.floorTex = 37;
            t.wallTex = 4;
            t.ceilTex = 4;
            t.ceilTex = 4;
            t.floorHeight = floorHeight - 10f;
            t.ceilHeight = ceilHeight + 1f + Game.rand.nextInt(8) / 8f;
        }

        if(existingLakeTile != null) {
            t.floorTex = existingLakeTile.floorTex;
            t.floorTexAtlas = existingLakeTile.floorTexAtlas;

            t.wallTex = existingLakeTile.wallTex;
            t.wallTexAtlas = existingLakeTile.wallTexAtlas;
        }

        generated.setTileIfUnlocked((int)lakeTile.x, (int)lakeTile.y, t);
    }

    private void paintOutlineTile(Level generated, Vector2 outlineTile) {
        if(generated.getTileOrNull((int)outlineTile.x, (int)outlineTile.y) == null) {
            Tile t = new Tile();
            t.blockMotion = true;
            t.renderSolid = true;
            t.wallTex = 4;
            generated.setTileIfUnlocked((int) outlineTile.x, (int) outlineTile.y, t);
        }
    }

    private void advanceLake(int x, int y, Level generated, Array<Vector2> lakeTiles, Array<Vector2> groundTiles, float chance) {
        try {
            if (x < 0 || y < 0 || x > generated.height - 1 || y > generated.width - 1)
                return;

            // short circuit out of making this lake if something bad happened
            if(!goodLake)
                return;

            Tile existing = generated.getTileOrNull(x, y);
            if (existing == null || existing.blockMotion) {
                lakeTiles.add(new Vector2(x, y));
                if (Game.rand.nextFloat() < chance) {
                    advanceLake(x + 1, y, generated, lakeTiles, groundTiles, chance - advanceChanceReduction);
                }
                if (Game.rand.nextFloat() < chance) {
                    advanceLake(x - 1, y, generated, lakeTiles, groundTiles, chance - advanceChanceReduction);
                }
                if (Game.rand.nextFloat() < chance) {
                    advanceLake(x, y + 1, generated, lakeTiles, groundTiles, chance - advanceChanceReduction);
                }
                if (Game.rand.nextFloat() < chance) {
                    advanceLake(x, y - 1, generated, lakeTiles, groundTiles, chance - advanceChanceReduction);
                }
            } else {
                groundTiles.add(new Vector2(x, y));
            }
        }
        catch(Exception ex) {
            Gdx.app.log("Delver", "Error advancing lake!");
            return;
        }
    }

    private Array<Vector2> getOutlineTiles(Array<Vector2> lakeTiles) {
        Vector2[] around = new Vector2[] { new Vector2(0, 1), new Vector2(0, -1), new Vector2(-1, 0), new Vector2(1, 0) };
        Vector2 next = new Vector2();

        Array<Vector2> outline = new Array<Vector2>();
        for(int i = 0; i < lakeTiles.size; i++) {
            Vector2 lakeTile = lakeTiles.get(i);
            next.x = lakeTile.x;
            next.y = lakeTile.y;

            for(int ii = 0; ii < around.length; ii++) {
                next.set(lakeTile);
                next.add(around[ii]);
                if(!lakeTiles.contains(next, false)) {
                    if(!outline.contains(next, false)) {
                        outline.add(new Vector2(next));
                    }
                }
            }
        }
        return outline;
    }

    private StairGenerator makeStairsUp(Array<Vector2> lakeTiles, Array<Vector2> outlineTiles, Level generated, float height) {
        StairGenerator generator = new StairGenerator(theme);
        for(int i = 0; i < outlineTiles.size; i++) {
            Vector2 tile = outlineTiles.get(i);

            Tile startTile = generated.getTileOrNull((int)tile.x, (int)tile.y);
            if(startTile == null || startTile.blockMotion) continue;
            if(startTile.floorHeight > height) continue;

            boolean didMatch = generator.makeStairsAt(tile, lakeTiles, type, generated);
            if(didMatch) {
                generator.applyStairs(tile, lakeTiles, generated);
                return generator;
            }
        }

        return null;
    }
}
