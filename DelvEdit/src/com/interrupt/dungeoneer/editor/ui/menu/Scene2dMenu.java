package com.interrupt.dungeoneer.editor.ui.menu;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;

public class Scene2dMenu extends Group {

    public Array<Actor> items = new Array<Actor>();
    public MenuItem parentMenuItem = null;

    Table menuTable;
    Skin skin;

    private boolean dirty = true;

    public Scene2dMenu(Skin skin) {
        this.skin = skin;
        setZIndex(10);
    }

    public void addItem(MenuItem item) {
        dirty = true;
        item.skin = skin;
        items.add(item);
    }

    public void addItemAt(MenuItem item, int index) {
        dirty = true;
        item.skin = skin;
        items.insert(index, item);
    }

    public void addSeparator() {
        dirty = true;
        Image image = new Image(skin, "menu-separator");
        items.add(image);
    }

    @Override
    public void act(float delta) {

        if(dirty) {
            refreshDrawables();
            dirty = false;
        }

        super.act(delta);

        Actor parent = getParent();
        boolean hasParent = false;

        if(parent != null && parent instanceof Scene2dMenu) {
            Table theirTable = ((Scene2dMenu)parent).menuTable;
            if(theirTable != null) {
                if(parent instanceof Scene2dMenuBar && parentMenuItem != null)
                    setX(parentMenuItem.getX() - 1f);
                else
                    setX(theirTable.getWidth() - 1f);
            }
            hasParent = true;
        }

        if(!hasParent) {
            if(menuTable != null && getStage() != null) {
                // make sure the menu doesn't go up off of the screen
                if(getY() + menuTable.getHeight() > getStage().getHeight()) setY(getStage().getHeight() - menuTable.getHeight());
            }
        }
    }

    public void setExpanded(MenuItem item) {
        for(Actor a : items) {
            if(a instanceof MenuItem) {
                MenuItem i = (MenuItem)a;
                if (item == i) {
                    if (i.subMenu != null) i.subMenu.open();
                    i.updateStyle(true);
                } else {
                    if (i.subMenu != null) i.subMenu.close();
                    i.updateStyle(false);
                }
            }
        }
    }

    public void close() {
        setVisible(false);
        for(Actor a : items) {
            if(a instanceof MenuItem) {
                MenuItem i = (MenuItem) a;
                i.updateStyle(false);
                if (i.subMenu != null) i.subMenu.close();
            }
        }
    }

    public void open() {
        setVisible(true);
    }

    public void pack() {
        if(menuTable != null) {
            menuTable.pack();
        }
    }

    public void refresh() {
        refreshDrawables();
    }

    protected void refreshDrawables() {
        clearChildren();

        // make the table
        menuTable = new Table();
        menuTable.setZIndex(10);
        menuTable.setOrigin(0f, 50f);
        addActor(menuTable);

        // add the rows
        for(Actor a : items) {
            menuTable.row();
            menuTable.add(a).align(Align.left).fill();
        }
        menuTable.pack();

        // add all of the sub menus to this space
        for(Actor a : items) {
            if(a instanceof MenuItem) {
                MenuItem i = (MenuItem)a;
                i.setParentMenu(this);
                if(i.subMenu != null) {
                    addActor(i.subMenu);
                }
            }
        }
    }
}
