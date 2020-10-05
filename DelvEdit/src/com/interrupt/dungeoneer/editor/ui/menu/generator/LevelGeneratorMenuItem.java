package com.interrupt.dungeoneer.editor.ui.menu.generator;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import com.interrupt.dungeoneer.editor.Editor;
import com.interrupt.dungeoneer.editor.ui.menu.DynamicMenuItem;
import com.interrupt.dungeoneer.editor.ui.menu.DynamicMenuItemAction;
import com.interrupt.dungeoneer.editor.ui.menu.MenuAccelerator;
import com.interrupt.dungeoneer.editor.ui.menu.MenuItem;
import com.interrupt.dungeoneer.game.Level;

public class LevelGeneratorMenuItem extends DynamicMenuItem {
    public LevelGeneratorMenuItem(Skin skin) {
        super("Generate Level", skin, new DynamicMenuItemAction() {
            private boolean needsRefresh = false;

            @Override
            public boolean isDirty() {
                return needsRefresh;
            }

            @Override
            public void updateMenuItem(MenuItem menuItem) {
                needsRefresh = false;

                if (menuItem.subMenu != null) {
                    menuItem.subMenu.items.clear();
                }

                Array<LevelGeneratorInfo> levelGenerators = Editor.app.generatorInfo.getLevelGenerators();

                if (levelGenerators.size > 0) {
                    for (LevelGeneratorInfo generator : levelGenerators) {
                        MenuItem parent = new MenuItem(generator.name.toUpperCase(), skin);

                        if (generator.templates.size > 0) {
                            for (LevelTemplateInfo template : generator.templates) {
                                String label = template.roomGeneratorType + " (" + template.theme + ")";
                                parent.addItem(new MenuItem(label, skin,
                                        makeLevelGeneratorAction(template.theme, template.roomGeneratorType)));
                            }
                        } else {
                            MenuItem item = new MenuItem("No templates found", skin);
                            item.getLabel().setColor(0.5f, 0.5f, 0.5f, 1);
                            parent.addItem(item);
                        }

                        menuItem.addItem(parent);
                    }

                    menuItem.addSeparator();
                    String label = "Re-Generate Level" + (Editor.app.generatorInfo.lastGeneratedLevelType != null
                            ? " (" + Editor.app.generatorInfo.lastGeneratedLevelType + ", " + Editor.app.generatorInfo.lastGeneratedLevelRoomType + ")"
                            : "");
                    MenuItem item = new MenuItem(label, skin, makeAnotherLevelGeneratorAction())
                            .setAccelerator(new MenuAccelerator(Keys.L, false, true));

                    if (Editor.app.generatorInfo.lastGeneratedLevelType == null) {
                        item.getLabel().setColor(0.5f, 0.5f, 0.5f, 1);
                        // TODO: Change color of accelerator label.
                    }

                    menuItem.addItem(item);
                } else {
                    MenuItem item = new MenuItem("No levels found", skin);
                    item.getLabel().setColor(0.5f, 0.5f, 0.5f, 1);
                    menuItem.addItem(item);
                }
            }

            private ActionListener makeLevelGeneratorAction(String theme, String roomGenerator) {
                return new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (theme != null || roomGenerator != null) {
                            Level level = Editor.app.getLevel();

                            if (level != null) {
                                level.editorMarkers.clear();
                                level.entities.clear();
                                level.theme = theme;
                                level.generated = true;
                                level.dungeonLevel = 0;
                                // TODO: This is configurable?!
                                level.crop(0, 0, 17 * 5, 17 * 5);
                                level.roomGeneratorChance = 0.4f;
                                level.roomGeneratorType = roomGenerator;
                                level.generate(Level.Source.EDITOR);
                                Editor.app.refresh();

                                if (Editor.app.generatorInfo.lastGeneratedLevelType != theme || Editor.app.generatorInfo.lastGeneratedLevelRoomType != roomGenerator) {
                                    needsRefresh = true;
                                }
        
                                Editor.app.generatorInfo.lastGeneratedLevelType = theme;
                                Editor.app.generatorInfo.lastGeneratedLevelRoomType = roomGenerator;
                            }
                        }
                    }
                };
            }

            private ActionListener makeAnotherLevelGeneratorAction() {
                return new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        makeLevelGeneratorAction(Editor.app.generatorInfo.lastGeneratedLevelType,
                                Editor.app.generatorInfo.lastGeneratedLevelRoomType).actionPerformed(e);
                    }
                };
            }
        });
    }
}
