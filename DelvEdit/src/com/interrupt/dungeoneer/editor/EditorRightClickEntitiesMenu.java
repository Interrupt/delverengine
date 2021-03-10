package com.interrupt.dungeoneer.editor;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.OrderedMap;
import com.interrupt.dungeoneer.editor.ui.menu.MenuItem;
import com.interrupt.dungeoneer.editor.ui.menu.Scene2dMenu;
import com.interrupt.dungeoneer.entities.*;
import com.interrupt.dungeoneer.entities.Entity.ArtType;
import com.interrupt.dungeoneer.entities.items.*;
import com.interrupt.dungeoneer.entities.projectiles.Missile;
import com.interrupt.dungeoneer.entities.triggers.*;
import com.interrupt.dungeoneer.game.Level;
import com.interrupt.dungeoneer.generator.GenInfo.Markers;
import com.interrupt.helpers.TileEdges;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map.Entry;

public class EditorRightClickEntitiesMenu extends Scene2dMenu {

    public EditorRightClickEntitiesMenu(Skin skin, final float xPos, final float yPos, final float zPos, final Level level){
        super(skin);

    	if(Editor.app.entityManager != null) {

    		MenuItem baseEntityMenu = new MenuItem("Basic", skin);
            MenuItem entityMenu = new MenuItem("Place Entity", skin);
            MenuItem prefabMenu = new MenuItem("Place Prefab", skin);
            MenuItem markersMenu = new MenuItem("Place Marker", skin);

            MenuItem tilesMenu = new MenuItem("Tiles", skin);
			MenuItem surfaceMenu = new MenuItem("Surface", skin);

    		Array<Entity> baseEntities = baseEntities();
    		for(final Entity basic : baseEntities) {
                MenuItem menuItem = new MenuItem(basic.getClass().getSimpleName(), skin);
    			menuItem.addActionListener(new ActionListener() {
					public void actionPerformed (ActionEvent event) {
						placeEntity(basic, xPos, yPos, zPos, Editor.app.pickedSurface);
					}
    			});
    			baseEntityMenu.addItem(menuItem);
    		}

			// Build out hierarchical menu for entities/prefabs.
			HashMap<String, MenuItem> prefabCategoryMap = new HashMap<String, MenuItem>();
			HashMap<String, MenuItem> entityCategoryMap = new HashMap<String, MenuItem>();

			for(final Entry<String, OrderedMap<String, Entity>> categoryEntry : Editor.app.entityManager.entities.entrySet()) {
				MenuItem prefabCategoryMenu = prefabCategoryMap.get(categoryEntry.getKey());
				MenuItem categoryMenu = entityCategoryMap.get(categoryEntry.getKey());

				if (prefabCategoryMenu == null) {
					// Normalize path.
					String path = categoryEntry.getKey().replace("\\", "/");
					String[] pathParts = path.split("/");
					StringBuilder currentPath = new StringBuilder();

					MenuItem parentPrefabMenu = prefabMenu;
					MenuItem currentPrefabMenu = null;
					MenuItem parentEntityMenu = entityMenu;
					MenuItem currentEntityMenu = null;

					for (String part: pathParts) {
						// Build up path.
						if (currentPath.length() == 0) {
							currentPath.append(part);
						}
						else {
							currentPath.append("/").append(part);
						}

						// Grab cached version if they exist.
						currentPrefabMenu = prefabCategoryMap.get(currentPath.toString());
						currentEntityMenu = entityCategoryMap.get(currentPath.toString());

						// Create if they dont.
						if (currentPrefabMenu == null) {
							// New prefab MenuItem
							currentPrefabMenu = new MenuItem(part, skin);
							prefabCategoryMap.put(currentPath.toString(), currentPrefabMenu);
							parentPrefabMenu.addItem(currentPrefabMenu);

							// New entity MenuItem
							currentEntityMenu = new MenuItem(part, skin);
							entityCategoryMap.put(currentPath.toString(), currentEntityMenu);
							parentEntityMenu.addItem(currentEntityMenu);
						}

						// Update current parent.
						parentPrefabMenu = currentPrefabMenu;
						parentEntityMenu = currentEntityMenu;
					}

					// Assign before exiting conditional
					prefabCategoryMenu = currentPrefabMenu;
					categoryMenu = currentEntityMenu;
				}

    			OrderedMap<String,Entity> entities = categoryEntry.getValue();

    			for(final com.badlogic.gdx.utils.ObjectMap.Entry<String, Entity> entry : entities.entries()) {
                    MenuItem menuItem = new MenuItem(entry.key, skin);
    		    	categoryMenu.addItem(menuItem);

    		    	final Entity entity = Editor.app.entityManager.Copy(entry.value);

    		    	menuItem.addActionListener(new ActionListener() {
    					public void actionPerformed (ActionEvent event) {
    						if(entity != null) {
								placeEntity(entity, xPos, yPos, zPos, Editor.app.pickedSurface);
    						}
    					}
    				});

    		    	final MenuItem prefabMenuItem = new MenuItem(entry.key, skin);
    		    	prefabCategoryMenu.addItem(prefabMenuItem);

    		    	prefabMenuItem.addActionListener(new ActionListener() {
    		    		public void actionPerformed (ActionEvent event) {
    		    			Prefab prefab = new Prefab(categoryEntry.getKey(), prefabMenuItem.getText().toString());
							placeEntity(prefab, xPos, yPos, zPos, Editor.app.pickedSurface);
    		    		}
    		    	});
        		}

        		categoryMenu.sortItems();
    			prefabCategoryMenu.sortItems();
    		}

			for(final Entry<String, Array<Monster>> categoryEntry : Editor.app.monsterManager.monsters.entrySet()) {
				MenuItem prefabCategoryMenu = prefabCategoryMap.get(categoryEntry.getKey());
				MenuItem categoryMenu = entityCategoryMap.get(categoryEntry.getKey());

				if (prefabCategoryMenu == null) {
					// Normalize path.
					String path = categoryEntry.getKey().replace("\\", "/");
					path = "Monsters/" + path;

					String[] pathParts = path.split("/");
					StringBuilder currentPath = new StringBuilder();

					MenuItem parentPrefabMenu = prefabMenu;
					MenuItem currentPrefabMenu = null;
					MenuItem parentEntityMenu = entityMenu;
					MenuItem currentEntityMenu = null;

					for (String part: pathParts) {
						// Build up path.
						if (currentPath.length() == 0) {
							currentPath.append(part);
						}
						else {
							currentPath.append("/").append(part);
						}

						// Grab cached version if they exist.
						currentPrefabMenu = prefabCategoryMap.get(currentPath.toString());
						currentEntityMenu = entityCategoryMap.get(currentPath.toString());

						// Create if they dont.
						if (currentPrefabMenu == null) {
							// New prefab MenuItem
							currentPrefabMenu = new MenuItem(part, skin);
							prefabCategoryMap.put(currentPath.toString(), currentPrefabMenu);
							parentPrefabMenu.addItem(currentPrefabMenu);

							// New entity MenuItem
							currentEntityMenu = new MenuItem(part, skin);
							entityCategoryMap.put(currentPath.toString(), currentEntityMenu);
							parentEntityMenu.addItem(currentEntityMenu);
						}

						// Update current parent.
						parentPrefabMenu = currentPrefabMenu;
						parentEntityMenu = currentEntityMenu;
					}

					// Assign before exiting conditional
					prefabCategoryMenu = currentPrefabMenu;
					categoryMenu = currentEntityMenu;
				}

				Array<Monster> monsters = categoryEntry.getValue();

				for(final Monster entry : monsters) {
					MenuItem menuItem = new MenuItem(entry.name, skin);
					categoryMenu.addItem(menuItem);

					final Entity entity = Editor.app.entityManager.Copy(entry);

					menuItem.addActionListener(new ActionListener() {
						public void actionPerformed (ActionEvent event) {
							if(entity != null) {
								placeEntity(entity, xPos, yPos, zPos, Editor.app.pickedSurface);
							}
						}
					});

					final MenuItem prefabMenuItem = new MenuItem(entry.name, skin);
					prefabCategoryMenu.addItem(prefabMenuItem);

					prefabMenuItem.addActionListener(new ActionListener() {
						public void actionPerformed (ActionEvent event) {
							Prefab prefab = new MonsterPrefab(categoryEntry.getKey(), prefabMenuItem.getText().toString());
							placeEntity(prefab, xPos, yPos, zPos, Editor.app.pickedSurface);
						}
					});
				}

				categoryMenu.sortItems();
				prefabCategoryMenu.sortItems();
			}

    		// markers!
    		for(final Markers marker : Markers.values()) {
    			if(marker != Markers.none) {
	    			MenuItem markerItem = new MenuItem(marker.name(), skin);
	    			markersMenu.addItem(markerItem);

	    			markerItem.addActionListener(new ActionListener() {
	    				public void actionPerformed(ActionEvent event) {
							Editor.app.addEntityMarker(marker);
	    				}
	    			});
    			}
    		}

    		entityMenu.sortItems();
    		prefabMenu.sortItems();
    		markersMenu.sortItems();

    		baseEntityMenu.sortItems();
			entityMenu.addItemAt(baseEntityMenu, 0);

    		addItem(entityMenu);
    		addItem(prefabMenu);
    		addItem(markersMenu);

    		if(Editor.app.selectionHasEntityMarker()) {
    			MenuItem clearMarkers = new MenuItem("Remove Marker", skin);
    			addItem(clearMarkers);

    			clearMarkers.addActionListener(new ActionListener() {
    				public void actionPerformed(ActionEvent event) {
						Editor.app.clearSelectedMarkers();
						Editor.app.history.saveState(level);
    				}
    			});
    		}

    		// tile editing stuff
    		MenuItem carveTiles = new MenuItem("Carve Area", skin);
    		MenuItem paintTiles = new MenuItem("Paint Area", skin);
    		MenuItem pickTiles = new MenuItem("Pick Tile", skin);
    		MenuItem deleteTiles = new MenuItem("Delete", skin);

    		carveTiles.addActionListener(new ActionListener() {
				public void actionPerformed (ActionEvent event) {
					Editor.app.doCarve();
				}
    		});

    		paintTiles.addActionListener(new ActionListener() {
				public void actionPerformed (ActionEvent event) {
					Editor.app.doPaint();
				}
    		});

    		pickTiles.addActionListener(new ActionListener() {
				public void actionPerformed (ActionEvent event) {
					Editor.app.doPick();
				}
    		});

    		deleteTiles.addActionListener(new ActionListener() {
				public void actionPerformed (ActionEvent event) {
					Editor.app.doDelete();
				}
    		});

    		tilesMenu.addItem(carveTiles);
    		tilesMenu.addItem(paintTiles);
    		tilesMenu.addItem(pickTiles);
    		tilesMenu.addItem(deleteTiles);

			addItem(tilesMenu);

			// surface editing stuff
			MenuItem surfacePaint = new MenuItem("Paint Surface", skin);
			MenuItem floodPaintTexture = new MenuItem("Flood Fill Surface", skin);
			MenuItem surfaceGrabTexture = new MenuItem("Grab Texture", skin);
			MenuItem surfaceChangeTexture = new MenuItem("Change Texture", skin);

			surfacePaint.addActionListener(new ActionListener() {
				public void actionPerformed (ActionEvent event) {
					Editor.app.paintSurfaceAtCursor();
				}
			});

			surfaceGrabTexture.addActionListener(new ActionListener() {
				public void actionPerformed (ActionEvent event) {
					Editor.app.pickTextureAtSurface();
				}
			});

			surfaceChangeTexture.addActionListener(new ActionListener() {
				public void actionPerformed (ActionEvent event) {
					Editor.app.pickNewSurfaceTexture();
				}
			});

			floodPaintTexture.addActionListener(new ActionListener() {
				public void actionPerformed (ActionEvent event) {
					Editor.app.fillSurfaceTexture();
				}
			});

			surfaceMenu.addItem(surfacePaint);
			surfaceMenu.addItem(floodPaintTexture);
			surfaceMenu.addItem(surfaceGrabTexture);
			surfaceMenu.addItem(surfaceChangeTexture);

			addItem(surfaceMenu);
    	}
    }

    public void placeEntity(Entity entity, float x, float y, float z, EditorApplication.PickedSurface surface) {
		entity.x = x;
		entity.y = y;
		entity.z = z + 0.5f;

		if(entity instanceof Prefab) {
			entity.updateDrawable();
			((Prefab) entity).updateCollision();
		}

		if(surface.tileSurface == EditorApplication.TileSurface.Ceiling) {
			entity.z -= entity.collision.z;
		}
		else if(surface.tileSurface == EditorApplication.TileSurface.UpperWall || surface.tileSurface == EditorApplication.TileSurface.LowerWall) {
			if(surface.edge == TileEdges.East) {
				entity.x -= entity.collision.x;
			}
			else if(surface.edge == TileEdges.West) {
				entity.x += entity.collision.x;
			}
			else if(surface.edge == TileEdges.North) {
				entity.y -= entity.collision.y;
			}
			else if(surface.edge == TileEdges.South) {
				entity.y += entity.collision.y;
			}
		}

		Editor.app.addEntity(entity);

		Editor.app.refreshLights();
		Editor.app.history.saveState(Editor.app.level);
	}

    public Array<Entity> baseEntities() {

    	Array<Entity> baseEntity = new Array<Entity>();

    	// Basic entities
		Light light = new Light();
		light.lightColor = com.badlogic.gdx.graphics.Color.WHITE;
		light.range = 3;
		baseEntity.add(light);
		
		Monster monster = new Monster();
		monster.artType = ArtType.entity;
		monster.tex = 0;
		baseEntity.add(monster);
		
		Sprite s = new Sprite();
		s.isDynamic = false;
		s.artType = ArtType.sprite;
		baseEntity.add(s);
		
		AnimatedSprite as = new AnimatedSprite();
		s.isDynamic = false;
		s.artType = ArtType.sprite;
		baseEntity.add(as);

		SpriteBeam beam = new SpriteBeam();
		beam.tex = 15;
		baseEntity.add(beam);
		
		Door d = new Door(0,0,0);
		baseEntity.add(d);

		Key k = new Key();
		k.name = "Key";
		baseEntity.add(k);
		
		Model m = new Model("meshes/chair.obj");
		m.isDynamic = false;
		baseEntity.add(m);
		
		Breakable b = new Breakable("meshes/crate.obj", "meshes.png");
		b.collision.set(0.25f, 0.25f, 0.5f);
		baseEntity.add(b);
		
		SpriteDecal decal = new SpriteDecal(0,0,0);
		baseEntity.add(decal);
		
		baseEntity.add(new ProjectedDecal());
		baseEntity.add(new Trigger());
		baseEntity.add(new ProgressionTrigger());
		baseEntity.add(new ParticleEmitter());
		baseEntity.add(new DynamicLight());
		baseEntity.add(new AmbientSound());
		baseEntity.add(new Note());
		baseEntity.add(new Decoration());
		baseEntity.add(new Armor());
		baseEntity.add(new Sword());
		baseEntity.add(new Wand());
		baseEntity.add(new Bow());
		baseEntity.add(new Missile());
		baseEntity.add(new Stairs());
		baseEntity.add(new Mover());
		baseEntity.add(new FogSprite());
        baseEntity.add(new Spikes());
        baseEntity.add(new Ladder());
        baseEntity.add(new Critter());
		baseEntity.add(new Fire());
		baseEntity.add(new Text());
		
		return baseEntity;
    }
}