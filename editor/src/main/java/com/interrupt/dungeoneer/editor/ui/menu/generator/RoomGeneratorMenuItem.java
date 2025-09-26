package com.interrupt.dungeoneer.editor.ui.menu.generator;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import com.interrupt.dungeoneer.editor.Editor;
import com.interrupt.dungeoneer.editor.ui.WarningDialog;
import com.interrupt.dungeoneer.editor.ui.menu.DynamicMenuItem;
import com.interrupt.dungeoneer.editor.ui.menu.DynamicMenuItemAction;
import com.interrupt.dungeoneer.editor.ui.menu.MenuAccelerator;
import com.interrupt.dungeoneer.editor.ui.menu.MenuItem;
import com.interrupt.dungeoneer.game.Level;
import com.interrupt.dungeoneer.generator.RoomGenerator;
import com.interrupt.dungeoneer.generator.SectionDefinition;

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

                Array<SectionDefinition> sectionDefinitions = Editor.app.generatorInfo.getSectionDefinitions();

                if (sectionDefinitions.size > 0) {
                    for (SectionDefinition sectionDefinition : sectionDefinitions) {
                        MenuItem parent = new MenuItem(sectionDefinition.name.toUpperCase(), skin);

                        if (sectionDefinition.levelTemplates.size > 0) {
                            for (Level template : sectionDefinition.levelTemplates) {
                                if (template.generated) {
                                    String label = template.roomGeneratorType + " (" + template.theme + ")";
                                    parent.addItem(new MenuItem(label, skin, makeRoomGeneratorAction(template)));
                                } else {
                                    MenuItem item = new MenuItem("No templates found", skin);
                                    item.getLabel().setColor(0.5f, 0.5f, 0.5f, 1);
                                    parent.addItem(item);
                                }
                            }
                        } else {
                            MenuItem item = new MenuItem("No templates found", skin);
                            item.getLabel().setColor(0.5f, 0.5f, 0.5f, 1);
                            parent.addItem(item);
                        }

                        menuItem.addItem(parent);
                    }

                    menuItem.addSeparator();
                    String label = "Re-Generate Room"
                            + (Editor.app.generatorInfo.lastGeneratedRoomTemplate != null
                                    ? " (" + Editor.app.generatorInfo.lastGeneratedRoomTemplate.roomGeneratorType + ", "
                                            + Editor.app.generatorInfo.lastGeneratedRoomTemplate.theme + ")"
                                    : "");
                    MenuItem item = new MenuItem(label, skin, makeRoomGeneratorAction())
                            .setAccelerator(new MenuAccelerator(Keys.R, false, true));

                    if (Editor.app.generatorInfo.lastGeneratedRoomTemplate == null) {
                        item.getLabel().setColor(0.5f, 0.5f, 0.5f, 1);
                    }

                    menuItem.addItem(item);
                } else {
                    MenuItem item = new MenuItem("No rooms found", skin);
                    item.getLabel().setColor(0.5f, 0.5f, 0.5f, 1);
                    menuItem.addItem(item);
                }
            }

            private ActionListener makeRoomGeneratorAction(Level template) {
                return new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent actionEvent) {
                        if (template != null) {
                            if (!Editor.app.generatorInfo.isLevelTemplateValid(template)) {
                                WarningDialog warningDialog = new WarningDialog(skin, "We were not able to find the level template used for this room generator. Make sure it exists.");
                                warningDialog.show(Editor.app.ui.getStage());
                            }
                            else {
                                Editor.app.generateRoomFromTemplate(template);
    
                                if (!Editor.app.generatorInfo.isLastGeneratedRoomTemplateSelected(template)) {
                                    Editor.app.generatorInfo.lastGeneratedRoomTemplate = template;
                                    needsRefresh = true;
                                }
                            }
                        }
                    }
                };
            }

            private ActionListener makeRoomGeneratorAction() {
                return new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent actionEvent) {
                        makeRoomGeneratorAction(Editor.app.generatorInfo.lastGeneratedRoomTemplate)
                                .actionPerformed(actionEvent);
                    }
                };
            }
        });
    }
}
