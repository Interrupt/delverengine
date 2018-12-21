package com.interrupt.dungeoneer.editor;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.OrderedMap;
import com.interrupt.dungeoneer.editor.ui.menu.MenuItem;
import com.interrupt.dungeoneer.editor.ui.menu.Scene2dMenu;
import com.interrupt.dungeoneer.entities.*;
import com.interrupt.dungeoneer.entities.Entity.ArtType;
import com.interrupt.dungeoneer.entities.areas.Area;
import com.interrupt.dungeoneer.entities.items.*;
import com.interrupt.dungeoneer.entities.projectiles.MagicMissileProjectile;
import com.interrupt.dungeoneer.entities.projectiles.Missile;
import com.interrupt.dungeoneer.entities.triggers.*;
import com.interrupt.dungeoneer.game.Level;
import com.interrupt.dungeoneer.game.ModManager;
import com.interrupt.dungeoneer.generator.GenInfo.Markers;
import com.interrupt.helpers.TileEdges;
import javafx.collections.transformation.SortedList;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map.Entry;

public class EditorRightClickEntitiesMenu extends Scene2dMenu {
    
    public EditorRightClickEntitiesMenu(Skin skin, final float xPos, final float yPos, final float zPos, final EditorFrame editor, final Level level){
        super(skin);
    	
    	if(editor.entityManager != null) {
    		
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
						placeEntity(basic, xPos, yPos, zPos, editor.pickedSurface, editor);
					}
    			});
    			baseEntityMenu.addItem(menuItem);
    		}
    		
    		for(final Entry<String, OrderedMap<String, Entity>> categoryEntry : editor.entityManager.entities.entrySet()) {
                MenuItem categoryMenu = new MenuItem(categoryEntry.getKey(), skin);
                MenuItem prefabCategoryMenu = new MenuItem(categoryEntry.getKey(), skin);
    			
    			OrderedMap<String,Entity> entities = categoryEntry.getValue();
    			
    			for(final com.badlogic.gdx.utils.ObjectMap.Entry<String, Entity> entry : entities.entries()) {
                    MenuItem menuItem = new MenuItem(entry.key, skin);
    		    	categoryMenu.addItem(menuItem);
    		    	
    		    	final Entity entity = editor.entityManager.Copy(entry.value);
    		    	
    		    	menuItem.addActionListener(new ActionListener() {
    					public void actionPerformed (ActionEvent event) {
    						if(entity != null) {
								placeEntity(entity, xPos, yPos, zPos, editor.pickedSurface, editor);
    						}
    					}
    				});
    		    	
    		    	final MenuItem prefabMenuItem = new MenuItem(entry.key, skin);
    		    	prefabCategoryMenu.addItem(prefabMenuItem);
    		    	
    		    	prefabMenuItem.addActionListener(new ActionListener() {
    		    		public void actionPerformed (ActionEvent event) {
    		    			Prefab prefab = new Prefab(categoryEntry.getKey(), prefabMenuItem.getText().toString());
							placeEntity(prefab, xPos, yPos, zPos, editor.pickedSurface, editor);
    		    		}
    		    	});
        		}

        		categoryMenu.sortItems();
    			prefabCategoryMenu.sortItems();
    			
    			entityMenu.addItem(categoryMenu);
    			prefabMenu.addItem(prefabCategoryMenu);
    		}
    		
    		// markers!
    		for(final Markers marker : Markers.values()) {
    			if(marker != Markers.none) {
	    			MenuItem markerItem = new MenuItem(marker.name(), skin);
	    			markersMenu.addItem(markerItem);
	    			
	    			markerItem.addActionListener(new ActionListener() {
	    				public void actionPerformed(ActionEvent event) {
	    					editor.addEntityMarker(marker);
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
    		
    		if(editor.selectionHasEntityMarker()) {
    			MenuItem clearMarkers = new MenuItem("Remove Marker", skin);
    			addItem(clearMarkers);
    			
    			clearMarkers.addActionListener(new ActionListener() {
    				public void actionPerformed(ActionEvent event) {
    					editor.clearSelectedMarkers();
                        editor.history.saveState(level);
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
					editor.doCarve();
				}
    		});
    		
    		paintTiles.addActionListener(new ActionListener() {
				public void actionPerformed (ActionEvent event) {
					editor.doPaint();
				}
    		});
    		
    		pickTiles.addActionListener(new ActionListener() {
				public void actionPerformed (ActionEvent event) {
					editor.doPick();
				}
    		});
    		
    		deleteTiles.addActionListener(new ActionListener() {
				public void actionPerformed (ActionEvent event) {
					editor.doDelete();
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
					editor.paintSurfaceAtCursor();
				}
			});

			surfaceGrabTexture.addActionListener(new ActionListener() {
				public void actionPerformed (ActionEvent event) {
					editor.pickTextureAtSurface();
				}
			});

			surfaceChangeTexture.addActionListener(new ActionListener() {
				public void actionPerformed (ActionEvent event) {
					editor.pickNewSurfaceTexture();
				}
			});

			floodPaintTexture.addActionListener(new ActionListener() {
				public void actionPerformed (ActionEvent event) {
					editor.fillSurfaceTexture();
				}
			});

			surfaceMenu.addItem(surfacePaint);
			surfaceMenu.addItem(floodPaintTexture);
			surfaceMenu.addItem(surfaceGrabTexture);
			surfaceMenu.addItem(surfaceChangeTexture);

			addItem(surfaceMenu);
    	}
    }

    public void placeEntity(Entity entity, float x, float y, float z, EditorFrame.PickedSurface surface, EditorFrame editor) {
		entity.x = x;
		entity.y = y;
		entity.z = z + 0.5f;

		if(entity instanceof Prefab) {
			entity.updateDrawable();
			((Prefab) entity).updateCollision();
		}

		if(surface.tileSurface == EditorFrame.TileSurface.Ceiling) {
			entity.z -= entity.collision.z;
		}
		else if(surface.tileSurface == EditorFrame.TileSurface.UpperWall || surface.tileSurface == EditorFrame.TileSurface.LowerWall) {
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

		editor.level.entities.add(entity);
		editor.refreshLights();
		editor.history.saveState(editor.level);
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