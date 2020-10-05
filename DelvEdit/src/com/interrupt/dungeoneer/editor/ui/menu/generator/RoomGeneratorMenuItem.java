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
import com.interrupt.dungeoneer.generator.RoomGenerator;

public class RoomGeneratorMenuItem extends DynamicMenuItem {
    public RoomGeneratorMenuItem(Skin skin) {
        super("Generate Room", skin, new DynamicMenuItemAction() {
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

                Array<String> roomGenerators = Editor.app.generatorInfo.getRoomGenerators();

                if (roomGenerators.size > 0) {
                    for (String generator : roomGenerators) {
                        menuItem.addItem(new MenuItem(generator, skin, makeRoomGeneratorAction(generator)));
                    }

                    menuItem.addSeparator();
                    String label = "Re-Generate Room" + (Editor.app.generatorInfo.lastGeneratedRoomType != null
                            ? " (" + Editor.app.generatorInfo.lastGeneratedRoomType + ")"
                            : "");
                    MenuItem item = new MenuItem(label, skin, makeRoomGeneratorAction())
                            .setAccelerator(new MenuAccelerator(Keys.G, false, true));

                    if (Editor.app.generatorInfo.lastGeneratedRoomType == null) {
                        item.getLabel().setColor(0.5f, 0.5f, 0.5f, 1);
                        // TODO: Change color of accelerator label.
                    }

                    menuItem.addItem(item);
                } else {
                    MenuItem item = new MenuItem("No generators found", skin);
                    item.getLabel().setColor(0.5f, 0.5f, 0.5f, 1);
                    menuItem.addItem(item);
                }
            }

            private ActionListener makeRoomGeneratorAction(String generatorType) {
                return new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent actionEvent) {
                        Editor.app.getLevel().editorMarkers.clear();
                        Editor.app.getLevel().entities.clear();

                        Level generatedLevel = new Level(17, 17);
                        generatedLevel.roomGeneratorType = generatorType;

                        RoomGenerator generator = new RoomGenerator(generatedLevel, generatorType);
                        generator.generate(true, true, true, true);

                        Editor.app.getLevel().crop(0, 0, generatedLevel.width, generatedLevel.height);
                        Editor.app.getLevel().paste(generatedLevel, 0, 0);

                        Editor.app.refresh();

                        if (Editor.app.generatorInfo.lastGeneratedRoomType != generatorType) {
                            needsRefresh = true;
                        }

                        Editor.app.generatorInfo.lastGeneratedRoomType = generatorType;
                    }
                };
            }

            private ActionListener makeRoomGeneratorAction() {
                return new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent actionEvent) {
                        if (Editor.app.generatorInfo.lastGeneratedRoomType != null) {
                            makeRoomGeneratorAction(Editor.app.generatorInfo.lastGeneratedRoomType)
                                    .actionPerformed(actionEvent);
                        }
                    }
                };
            }
        });
    }
}
