package com.interrupt.dungeoneer.generator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.interrupt.dungeoneer.editor.EditorMarker;
import com.interrupt.dungeoneer.entities.Door;
import com.interrupt.dungeoneer.entities.Door.DoorDirection;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.game.Level;
import com.interrupt.dungeoneer.game.Progression;
import com.interrupt.dungeoneer.generator.GenTile.TileTypes;
import com.interrupt.dungeoneer.serializers.KryoSerializer;
import com.interrupt.dungeoneer.tiles.Tile;
import com.interrupt.utils.JsonUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class DungeonGenerator {
	private int MAPSIZE = 4;
	private int TILESIZE = 17;
	
	public Integer width = null;
	public Integer height = null;
	
	private GenTile[] genTiles = null;
	private Boolean[] visited = null;
	
	private Random r;
	private int maxComplexity = 1;
	private int curComplexity = 0;
	
	private int dungeonLevel;
	
	private HashMap<String, Level> tileCache = new HashMap<String, Level>();
	
	public DungeonGenerator(Random r, int dungeonLevel) {
		this.dungeonLevel = dungeonLevel;
		this.r = r;
	}
	
	public static String GetThemeDir(String type) {
		char[] typeDirChars = type.toLowerCase().toCharArray();
		typeDirChars[0] = Character.toUpperCase(typeDirChars[0]);
		String theme = String.valueOf(typeDirChars);
		
		String themeDir = "generator/" + theme + "/";
		
		Gdx.app.log("DelverGenerator", "Using theme dir " + themeDir);
		
		return themeDir;
	}
	
	public static GenTheme GetGenData(String type) {
		String tilesDir = GetThemeDir(type);
		
		try {
			GenTheme generatorTheme = Game.modManager.loadTheme(tilesDir + "info.dat");
			return generatorTheme;
		}
		catch( Exception ex ) {
			return new GenTheme();
		}
	}
	
	public Level MakeDungeon(String type) { return MakeDungeon(type, null, 0.7f, null); }
	
	public Level MakeDungeon(String type, String roomGeneratorType, float roomGeneratorChance, Progression progression) {
		String tilesDir = GetThemeDir(type);
		String cornersDir = tilesDir + "Corners/";
		String hallsDir = tilesDir + "Halls/";
		String intersectionsDir = tilesDir + "Intersections/";
		String triIntersectionsDir = tilesDir + "TriIntersections/";
		String beginningsDir = tilesDir + "Beginnings/";
		String startsDir = tilesDir + "Starts/";
		String endsDir = tilesDir + "Ends/";
		
		HashMap<TileTypes,ArrayMap<String, FileHandle>> tiles = new HashMap<TileTypes,ArrayMap<String, FileHandle>>();
		tiles.put(TileTypes.beginning, getLevelFilesInDir(beginningsDir));
		tiles.put(TileTypes.start, getLevelFilesInDir(startsDir));
		tiles.put(TileTypes.corner, getLevelFilesInDir(cornersDir));
		tiles.put(TileTypes.hall, getLevelFilesInDir(hallsDir));
		tiles.put(TileTypes.intersection, getLevelFilesInDir(intersectionsDir));
		tiles.put(TileTypes.tri_intersection, getLevelFilesInDir(triIntersectionsDir));
		tiles.put(TileTypes.end, getLevelFilesInDir(endsDir));
		tiles.put(TileTypes.finish, getLevelFilesInDir(endsDir));

		GenTheme theme = GetGenData(type);
		TILESIZE = theme.getChunkTileSize();
		MAPSIZE = theme.getMapChunks();

		maxComplexity = theme.getMapComplexity();
		curComplexity = 0;

		// initialize the generator with these settings
		width = MAPSIZE * TILESIZE;
		height = MAPSIZE * TILESIZE;

		genTiles = new GenTile[MAPSIZE * MAPSIZE];
		visited = new Boolean[MAPSIZE * MAPSIZE];

		Level generated = new Level(TILESIZE * MAPSIZE, TILESIZE * MAPSIZE);
		generated.genTheme = theme;
		generated.roomGeneratorType = roomGeneratorType;

		// generate the map!
		makeGenTiles(tiles, progression, type);
		
		for(int x = 0; x < MAPSIZE; x++) {
			for(int y = 0; y < MAPSIZE; y++) {
				GenTile tile = genTiles[x + y * MAPSIZE];
				if(tile != null) {
                    ArrayMap<String, FileHandle> tile_entries = tiles.get(tile.type);
                    Level level_tile = null;

                    // generate a tile, or load one
					boolean generatedRoom = false;
                    if(roomGeneratorType != null && r.nextFloat() < roomGeneratorChance &&
                            (tile.type == TileTypes.corner ||
                                    tile.type == TileTypes.hall ||
                                    tile.type == TileTypes.intersection ||
                                    tile.type == TileTypes.tri_intersection ||
									tile.type == TileTypes.end)) {
                        level_tile = new Level(TILESIZE, TILESIZE);

                        boolean madeGoodRoom = false;

                        // try a few times to make a good room
                        for(int i = 0; i < 10 && !madeGoodRoom; i++) {
							RoomGenerator g = new RoomGenerator(level_tile, roomGeneratorType);
							madeGoodRoom = g.generate(tile.exitTop, tile.exitBottom, tile.exitRight, tile.exitLeft);
						}

						if(!madeGoodRoom) {
                        	Gdx.app.error("Delver", "Could not generate a good room!");
						}
						else {
							Gdx.app.log("DelverGenerator", "Generated tile");
						}

						generatedRoom = madeGoodRoom;
                    }

					if(!generatedRoom && tile_entries != null) {
						FileHandle level_tile_entry = null;
						
						// go pick a tile
						int pick_tries = 0;
						while(level_tile_entry == null) {
							pick_tries++;
							
							// grab a random one
							level_tile_entry = tile_entries.getValueAt(r.nextInt(tile_entries.size));
						
							// check if this tile is unique
							if(progression != null && pick_tries < 10) {
								if(level_tile_entry.name().contains("_unique")) {
									if(progression.uniqueTilesSeen.contains(level_tile_entry.name(), false)) {
										// already used, skip this one
										level_tile_entry = null;
										continue;
									}
									else {
										// hasn't been used yet
										progression.uniqueTilesSeen.add(level_tile_entry.name());
									}
								}
							}
						}

						// load the level!
						if(level_tile_entry.name().endsWith(".dat") || level_tile_entry.name().endsWith(".json")) {
							String data = level_tile_entry.readString();
							level_tile = JsonUtil.fromJson(Level.class, data);
						}
						else if(level_tile_entry.name().endsWith(".bin")) {
                            level_tile = KryoSerializer.loadLevel(level_tile_entry.readBytes());
						}
						
						if(level_tile.width != TILESIZE || level_tile.height != TILESIZE) {
							Gdx.app.log("Delver", "Invalid tile size: " + level_tile_entry + " - was expecting a size of " + TILESIZE + "x" + TILESIZE + " but got " + level_tile.width + "x" + level_tile.height);
						}
						
						// rotate
						for(int i = 0; i < tile.rot; i++) {
							level_tile.rotate90();
						}
						
						Gdx.app.log("DelverGenerator", "Added tile " + level_tile_entry.path());
					}

                    if(level_tile != null) {
                        level_tile.makeEntityIdsUnique(x + "_" + y);
                        generated.paste(level_tile, x * TILESIZE, y * TILESIZE);

                        // add doors
                        if(generated.genTheme.doors.size > 0 && (tile.exitLeft || tile.exitTop)) {
                            if(tile.exitLeft) {
                                addDoorAt(generated,
                                        level_tile,
                                        x * TILESIZE,
                                        (TILESIZE / 2f) + y * TILESIZE,
                                        Game.rand.nextBoolean() ? DoorDirection.NORTH : DoorDirection.SOUTH);
                            }
                            else if(tile.exitTop) {
                                addDoorAt(generated,
                                        level_tile,
                                        (TILESIZE / 2f) + x * TILESIZE,
                                        y * TILESIZE,
                                        Game.rand.nextBoolean() ? DoorDirection.WEST : DoorDirection.EAST);
                            }
                        }
                    }
				}
			}
		}

		// lock the tiles around the player start
		for(int i = 0; i < generated.editorMarkers.size; i++) {
			EditorMarker marker = generated.editorMarkers.get(i);
			generated.lockTilesAround(new Vector2(marker.x, marker.y), 8);
		}
		
		tileCache.clear();
		
		return generated;
	}

	private void makeGenTiles(HashMap<TileTypes,ArrayMap<String, FileHandle>> tiles, Progression progression, String theme) {
		int tileCount = 0;
		curComplexity = 0;

		// try to make an interesting map!
		while(tileCount <= 3 || curComplexity <= 2) {
			Gdx.app.log("Delver", "Trying to make map layout.");
			for(int i = 0; i < genTiles.length; i++) {
				genTiles[i] = null;
				visited[i] = null;
			}

			// generate a map layout!
			genTiles(tiles, progression, theme);

			tileCount = 0;
			for(int i = 0; i < genTiles.length; i++) {
				if(genTiles[i] != null) tileCount++;
			}
		}
	}

	private Door addDoorAt(Level generating, Level generatorTile, float doorLocX, float doorLocY, DoorDirection direction) {
		
		Door doorDefinition = generating.genTheme.doors.get(Game.rand.nextInt(generating.genTheme.doors.size));
		
		if(doorDefinition != null) {
			Door door = new Door(doorDefinition);
			
			Tile t = generatorTile.getTileOrNull(0, 8);
			if(door != null && t != null) {
				door.x = doorLocX;
				door.y = doorLocY;
				door.z = 0;
				door.doorDirection = direction;
				door.isDynamic = true;
				
				if(direction == DoorDirection.EAST || direction == DoorDirection.WEST) {
					float tempColX = door.collision.x;
					door.collision.x = door.collision.y;
					door.collision.y = tempColX;
				}
				
				// Add this new door if there are no doors already there
				if(generating.findEntities(Door.class, new Vector2(doorLocX, doorLocY), 1f, true, false, false).size == 0) {
					generating.entities.add(door);
					return door;
				}
			}
		}
		
		return null;
	}
	
	private void genTiles(HashMap<TileTypes,ArrayMap<String, FileHandle>> tiles, Progression progression, String theme) {
		int genRound = 0;

		// only use beginnings once for a dungeon area
		boolean canUseBeginnings = tiles.get(TileTypes.beginning).size > 0 && !progression.hasSeenDungeonArea(theme);

		if(canUseBeginnings)
			genTileAt(TileTypes.beginning, Game.rand.nextInt(3), 1, Game.rand.nextInt(MAPSIZE), Game.rand.nextInt(MAPSIZE));
		else
			genTileAt(TileTypes.start, Game.rand.nextInt(3), 1, Game.rand.nextInt(MAPSIZE), Game.rand.nextInt(MAPSIZE));
		
		while(doGenRound(genRound++)) { }

		pickFinishTile();
	}

	private void pickFinishTile() {
		// Make one of the ends a finish tile
		Array<GenTile> ends = new Array<GenTile>();
		for(int x = 0; x < MAPSIZE; x++) {
			for(int y = 0; y < MAPSIZE; y++) {
				GenTile tile = getTileAt(x, y);
				if(tile != null && tile.type == TileTypes.end)
					ends.add(tile);
			}
		}

		if(ends.size > 0) {
			ends.shuffle();
			GenTile choice = ends.first();
			choice.type = TileTypes.finish;
		}
	}
	
	private Boolean doGenRound(int genRound) {
		for(int x = 0; x < MAPSIZE; x++) {
			for(int y = 0; y < MAPSIZE; y++) {
				Boolean vis = visited[x + y * MAPSIZE];
				if(vis != null && vis == false) {
					return findTileFor(x, y, genRound);
				}
			}
		}
		
		return false;
	}
	
	public Boolean findTileFor(int x, int y, int genRound) {
		
		List<GenTile> canPlaceList = new ArrayList<GenTile>();
		
		List<TileTypes> availableTiles = getAvailableTiles(genRound);
		for(TileTypes type : availableTiles) {
			for(int i = 0; i < 4; i++) {
				GenTile tryTile = new GenTile(type, i, 2);
				boolean canPlace = true;
				
				GenTile up = getTileAt(x, y - 1);
				GenTile down = getTileAt(x, y + 1);
				GenTile left = getTileAt(x - 1, y);
				GenTile right = getTileAt(x + 1, y);

				if(up != null && up.exitBottom != tryTile.exitTop) {
					canPlace = false;
				}
				if(down != null && down.exitTop != tryTile.exitBottom) {
					canPlace = false;
				}
				if(left != null && left.exitRight != tryTile.exitLeft) {
					canPlace = false;
				}
				if(right != null && right.exitLeft != tryTile.exitRight) {
					canPlace = false;
				}
				
				if(tryTile.exitTop && isOutOfBounds(x, y - 1)) canPlace = false;
				else if(tryTile.exitBottom && isOutOfBounds(x, y + 1)) canPlace = false;
				else if(tryTile.exitLeft && isOutOfBounds(x - 1, y)) canPlace = false;
				else if(tryTile.exitRight && isOutOfBounds(x + 1, y)) canPlace = false;
				
				if(canPlace)
					canPlaceList.add(tryTile);
			}
		}
		
		if(canPlaceList.size() > 0) {
			GenTile gen = canPlaceList.get(r.nextInt( canPlaceList.size() ));
			genTileAt(gen.type, gen.rot, 2, x, y);
			
			if(gen.type == TileTypes.intersection)
				curComplexity += 2;
			if(gen.type == TileTypes.tri_intersection)
				curComplexity += 1;
			
			return true;
		}
		
		return false;
	}
	
	private Boolean isOutOfBounds(int x, int y) {
		if(x < 0 || x >= MAPSIZE || y < 0 || y >= MAPSIZE) return true;
		return false;
	}
	
	private Boolean doConnect(int rot1, int exits1, int rot2, int exits2) {
		return false;
	}
	
	private void genTileAt(TileTypes type, int rot, int exits, int x, int y) {
		GenTile t = new GenTile(type, rot, exits);
		genTiles[x + y * MAPSIZE] = t;
		visited[x + y * MAPSIZE] = true;
		
		if(t.exitLeft && !isOutOfBounds(x - 1, y) && getVisited(x - 1, y) != true)
			setVisited(false, x - 1, y);
		if(t.exitTop && !isOutOfBounds(x, y - 1) && getVisited(x, y - 1) != true)
			setVisited(false, x, y - 1);
		if(t.exitRight && !isOutOfBounds(x + 1, y) && getVisited(x + 1, y) != true)
			setVisited(false, x + 1, y);
		if(t.exitBottom && !isOutOfBounds(x, y + 1) && getVisited(x, y + 1) != true)
			setVisited(false, x, y + 1);
	}
	
	private GenTile getTileAt(int x, int y) {
		if(x >= 0 && x < MAPSIZE && y >= 0 && y < MAPSIZE)
			return genTiles[x + y * MAPSIZE];
		
		return null;
	}

	private void replaceTile(int x, int y, GenTile tile)  {
		genTiles[x + y * MAPSIZE] = tile;
	}
	
	private void setVisited(Boolean val, int x, int y) {
		if(x >= 0 && x < MAPSIZE && y >= 0 && y < MAPSIZE)
			visited[x + y * MAPSIZE] = val;
	}
	
	private Boolean getVisited(int x, int y) {
		if (visited[x + y * MAPSIZE] == null) return false;
		return visited[x + y * MAPSIZE];
	}
	
	private List<TileTypes> getAvailableTiles(int genRound) {
		List<TileTypes> availableTiles = new ArrayList<TileTypes>();
		
		availableTiles.add(TileTypes.corner);
		availableTiles.add(TileTypes.hall);
		
		if(maxComplexity > 0 && curComplexity < maxComplexity) {
			availableTiles.add(TileTypes.intersection);
			availableTiles.add(TileTypes.tri_intersection);
		}
		
		if(genRound > 2)
			availableTiles.add(TileTypes.end);
		
		return availableTiles;
	}

	private static ArrayMap<String, FileHandle> getLevelFilesInDir(String dir) {
        ArrayMap<String, FileHandle> files = new ArrayMap<String, FileHandle>();

        for(String folder : Game.modManager.modsFound) {
            String genFolder = folder + "/" + dir;

            Gdx.app.debug("Delver", "Looking for generator pieces in: " + genFolder);

            FileHandle gf = Game.getInternal(genFolder);
            if(gf.exists()) {
                for(FileHandle entry : Game.listDirectory(gf)) {
                    if ((entry.name().endsWith(".dat") || entry.name().endsWith(".bin") || entry.name().endsWith(".json"))) {
                        files.put(entry.name(), entry);
                    }
                }
            }
        }

        return files;
    }
}
