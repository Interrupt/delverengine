package com.interrupt.dungeoneer.editor.ui.menu.generator;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import com.interrupt.dungeoneer.editor.Editor;
import com.interrupt.dungeoneer.editor.ui.menu.DynamicMenuItem;
import com.interrupt.dungeoneer.editor.ui.menu.DynamicMenuItemAction;
import com.interrupt.dungeoneer.editor.ui.menu.MenuItem;

public class LevelGeneratorMenuItem extends DynamicMenuItem {
    public LevelGeneratorMenuItem(Skin skin) {
        super("Generate Level", skin, new DynamicMenuItemAction() {
            private boolean needsRefresh = false;

            @Override
            public boolean isDirty() {
                return false;
            }

            @Override
            public void initMenuItem(MenuItem item) {}

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
                                parent.addItem(new MenuItem(label, skin));
                            }
                        } else {
                            MenuItem item = new MenuItem("No templates found", skin);
                            item.getLabel().setColor(0.5f, 0.5f, 0.5f, 1);
                            parent.addItem(item);
                        }

                        menuItem.addItem(parent);
                    }
                } else {
                    MenuItem item = new MenuItem("No levels found", skin);
                    item.getLabel().setColor(0.5f, 0.5f, 0.5f, 1);
                    menuItem.addItem(item);
                }
            }
        });
    }
}
