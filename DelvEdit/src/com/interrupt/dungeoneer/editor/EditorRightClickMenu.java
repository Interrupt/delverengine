package com.interrupt.dungeoneer.editor;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JDialog;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import com.interrupt.dungeoneer.editor.ui.EditorUi;
import com.interrupt.dungeoneer.editor.ui.menu.MenuItem;
import com.interrupt.dungeoneer.editor.ui.menu.Scene2dMenu;
import com.interrupt.dungeoneer.entities.Entity;
import com.interrupt.dungeoneer.entities.Group;
import com.interrupt.dungeoneer.entities.Prefab;
import com.interrupt.dungeoneer.game.Level;

public class EditorRightClickMenu extends Scene2dMenu {
    
    public EditorRightClickMenu(Entity e, Level level) {
        super(EditorUi.getSmallSkin());
        Skin skin = EditorUi.getSmallSkin();
    	
    	final Entity entity = e;
        final Level lvl = level;

    	MenuItem remove = new MenuItem("Remove Entity", skin);
    	MenuItem toJson = new MenuItem("To JSON", skin);
		MenuItem onFloor = new MenuItem("Move to Floor", skin);
		MenuItem onCeiling = new MenuItem("Move to Ceiling", skin);
		MenuItem center = new MenuItem("Center in Tile", skin);
    	
    	if(e instanceof Group && !(e instanceof Prefab)) {
    		MenuItem unGroup = new MenuItem("Ungroup", skin);
    		addItem(unGroup);
    		
    		unGroup.addActionListener(new ActionListener() {
    			public void actionPerformed (ActionEvent event) {
    				Group g = (Group)entity;
    				
    				for(Entity grouped : g.entities) {
    					if(grouped .drawable != null) {
    						grouped.x += grouped.drawable.drawOffset.x;
        					grouped.y += grouped.drawable.drawOffset.y;
        					grouped.z += grouped.drawable.drawOffset.z;
        					
        					grouped.drawable.drawOffset.set(0,0,0);
    					}
    					
    					lvl.entities.add(grouped);
    				}
    				
    				lvl.entities.removeValue(g, true);
    			}
    		});
    	}
    	
    	addItem(remove);
        
        remove.addActionListener(new ActionListener() {
			public void actionPerformed (ActionEvent event) {
				lvl.entities.removeValue(entity, true);
				Editor.app.refreshLights();
			}
		});
        
        addItem(toJson);
        toJson.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent event) {
    			JDialog dialog = new JDialog(Editor.app.frame, Dialog.ModalityType.MODELESS);
				
				JsonViewer jsonViewer = new JsonViewer(entity);
				dialog.setTitle("JSON for Entity");
			    dialog.add(jsonViewer);
			    dialog.pack();
			    dialog.setVisible(true);
        	}
        });

        addItem(onFloor);
		onFloor.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				float floorHeight = lvl.getTile((int)entity.x, (int)entity.y).getFloorHeight(entity.x, entity.y);
				entity.z = floorHeight + 0.5f;
				Editor.app.refreshEntity(entity);
			}
		});

	addItem(onCeiling);
		onCeiling.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				float ceilHeight = lvl.getTile((int)entity.x, (int)entity.y).getCeilHeight(entity.x, entity.y);
				entity.z = ceilHeight - entity.collision.z + 0.5f;
				Editor.app.refreshEntity(entity);
			}
		});

        addItem(center);
		center.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				entity.x = (int)entity.x + 0.5f;
				entity.y = (int)entity.y + 0.5f;
				Editor.app.refreshEntity(entity);
			}
		});		
    }
    
    public EditorRightClickMenu(final Entity main, final Array<Entity> additionalSelected, final Level level) {

        super(EditorUi.getSmallSkin());
        Skin skin = EditorUi.getSmallSkin();

    	MenuItem group = new MenuItem("Group Together", skin);
    	MenuItem remove = new MenuItem("Remove Entities", skin);
    	
    	group.addActionListener(new ActionListener() {
			public void actionPerformed (ActionEvent event) {
				Group newGroup = new Group();
				newGroup.x = main.x;
				newGroup.y = main.y;
				newGroup.z = main.z;
				
				for(Entity selected : additionalSelected) {
					selected.x = selected.x - main.x;
					selected.y = selected.y - main.y;
					selected.z = selected.z - main.z;
					newGroup.entities.add(selected);

					Editor.app.markWorldAsDirty((int)selected.x, (int)selected.y, 4);
					
					level.entities.removeValue(selected, true);
				}
				
				main.x = 0;
				main.y = 0;
				main.z = 0;
				newGroup.entities.add(main);
				
				level.entities.removeValue(main, true);
				Editor.app.addEntity(newGroup);
			}
    	});
    	
    	remove.addActionListener(new ActionListener() {
			public void actionPerformed (ActionEvent event) {
				level.entities.removeValue(main, true);
				Editor.app.markWorldAsDirty((int)main.x, (int)main.y, 4);
				
				for(Entity selected : additionalSelected) {
					level.entities.removeValue(selected, true);
					Editor.app.markWorldAsDirty((int)selected.x, (int)selected.y, 4);
				}

				Editor.app.refreshLights();
			}
		});

    	addItem(group);
    	addItem(remove);

		MenuItem onFloor = new MenuItem("Move to Floor", skin);
		MenuItem onCeiling = new MenuItem("Move to Ceiling", skin);
		MenuItem center = new MenuItem("Center in Tile", skin);

		addItem(onFloor);
		onFloor.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				Array<Entity> allSelected = new Array<Entity>();
				allSelected.add(main);
				allSelected.addAll(additionalSelected);

				for(Entity entity : allSelected) {
					float floorHeight = level.getTile((int) entity.x, (int) entity.y).getFloorHeight(entity.x, entity.y);
					entity.z = floorHeight + 0.5f;
					Editor.app.refreshEntity(entity);
				}
			}
		});

		addItem(onCeiling);
		onCeiling.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				Array<Entity> allSelected = new Array<Entity>();
				allSelected.add(main);
				allSelected.addAll(additionalSelected);

				for(Entity entity : allSelected) {
					float ceilHeight = level.getTile((int) entity.x, (int) entity.y).getCeilHeight(entity.x, entity.y);
					entity.z = ceilHeight - entity.collision.z + 0.5f;
					Editor.app.refreshEntity(entity);
				}
			}
		});

		addItem(center);
		center.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				Array<Entity> allSelected = new Array<Entity>();
				allSelected.add(main);
				allSelected.addAll(additionalSelected);

				for(Entity entity : allSelected) {
					entity.x = (int)entity.x + 0.5f;
					entity.y = (int)entity.y + 0.5f;
					Editor.app.refreshEntity(entity);
				}
			}
		});
    }
}
