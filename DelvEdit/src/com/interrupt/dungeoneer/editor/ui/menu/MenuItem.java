package com.interrupt.dungeoneer.editor.ui.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Scaling;
import com.interrupt.dungeoneer.editor.ui.EditorUi;

import java.awt.event.ActionListener;

public class MenuItem extends TextButton implements Comparable {
    public Scene2dMenu subMenu;
    public Skin skin;
    public Scene2dMenu parent;
    public ActionListener actionListener;
    public ClickListener clickListener;

    private Label acceleratorLabel = null;
    private MenuAccelerator menuAccelerator = null;
    public boolean showExpandArrow = true;

    public static Array<MenuItem> acceleratorItems = new Array<MenuItem>();

    public MenuItem(CharSequence text) {
        this(text, EditorUi.defaultSkin);
    }

    private Vector2 tempVec2 = new Vector2();

    private float leftPadding = 45f;

    public MenuItem(CharSequence text, Skin skin) {
        super(text.toString(), skin);
        this.skin = skin;

        getLabel().setAlignment(Align.left);
        setStyle(skin.get("menu", TextButtonStyle.class));
        setSkin(skin);

        clear();
        add(getLabel()).expand().fill();

        final MenuItem thisItem = this;
        clickListener = new ClickListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if(getParentMenu() != null) getParentMenu().setExpanded(thisItem);
            }

            @Override
            public void clicked(InputEvent event, float x, float y) {
                Scene2dMenu p = getParentMenu();
                while(p.parentMenuItem != null && p.parentMenuItem.parent != null) {
                    p = p.parentMenuItem.parent;
                }

                if(actionListener != null && p != null) p.close();
                if(actionListener != null) actionListener.actionPerformed(null);
            }
        };

        refresh();
        setWidth(400f);
    }

    public MenuItem(CharSequence text, Skin skin, ActionListener actionListener) {
        this(text, skin);
        this.actionListener = actionListener;
    }

    public MenuItem addItem(MenuItem item) {
        if(subMenu == null) {
            subMenu = new Scene2dMenu(skin);
            subMenu.parentMenuItem = this;
            subMenu.setVisible(false);

            refresh();
        }

        subMenu.addItem(item);
        return this;
    }

    public MenuItem addItemAt(MenuItem item, int index) {
        if(subMenu == null) {
            subMenu = new Scene2dMenu(skin);
            subMenu.parentMenuItem = this;
            subMenu.setVisible(false);

            refresh();
        }

        subMenu.addItemAt(item, index);
        return this;
    }

    public MenuItem addSeparator() {
        if(subMenu != null)
            subMenu.addSeparator();
        return this;
    }

    public void sortItems() {
        if(subMenu != null) {
            subMenu.items.sort();
        }
    }

    @Override
    public void act(float delta) {
        if(subMenu != null && subMenu.menuTable != null) {
            float yLocation = getY() - subMenu.menuTable.getHeight() + getHeight();
            subMenu.setY(yLocation);

            // where on the stage is the menu being drawn?
            Vector2 stageLocation = localToStageCoordinates(tempVec2.set(0,yLocation));

            // might need to push it back up some to keep it on the screen
            if(stageLocation.y < getHeight() + 60f) {
                //subMenu.setY(subMenu.getY() - (stageLocation.y - getY()));
            }

            if(isVisible() && parent.isVisible() && subMenu.isVisible()) {
                float yPos = getYLocation();

                if(yPos - subMenu.menuTable.getHeight() < 0) {
                    subMenu.setY(subMenu.getY() - (yPos - subMenu.menuTable.getHeight()));
                }
            }

            if(parent != null && parent instanceof Scene2dMenuBar) {
                subMenu.setY(-subMenu.menuTable.getHeight());
            }
        }

        if(parent == null) {
            Actor pw = this;
            while(pw != null) {
                pw = pw.getParent();
                if(pw instanceof Scene2dMenu) {
                    parent = (Scene2dMenu)pw;
                    break;
                }
            }
        }
    }

    public void updateStyle(boolean selected) {
        if(!selected) {
            setStyle(skin.get("menu", TextButton.TextButtonStyle.class));
        }
        else {
            setStyle(skin.get("menu-selected", TextButton.TextButtonStyle.class));
        }
    }

    public void addActionListener(ActionListener actionListener) {
        this.actionListener = actionListener;
    }

    public void setParentMenu(Scene2dMenu parent) {
        this.parent = parent;
    }

    public Scene2dMenu getParentMenu() {
        return parent;
    }

    public void refresh() {
        clear();
        addListener(clickListener);
        add(getLabel()).expand().fill();

        boolean isMacOS = System.getProperty("os.name").toUpperCase().contains("MAC");

        String text = "";

        if (acceleratorLabel != null) {
            text = acceleratorLabel.getText().toString();
        }

        boolean madePadding = false;
        if(menuAccelerator != null) {
            if(menuAccelerator.getShiftRequired()) {
                if (isMacOS) {
                    Image commandIcon = new Image(skin, "menu-shift-icon");
                    commandIcon.setScaling(Scaling.none);
                    add(commandIcon).align(Align.right).fill().padLeft(leftPadding);
                    madePadding = true;
                }
                else {
                    text = "Shift+" + text;
                }
            }
            if(menuAccelerator.getControlRequired()) {
                if (isMacOS) {
                    Image commandIcon = new Image(skin, "menu-command-icon");
                    commandIcon.setScaling(Scaling.none);
                    add(commandIcon).align(Align.right).fill().padLeft(madePadding ? 3f : leftPadding);
                    madePadding = true;
                }
                else {
                    text = "Ctrl+" + text;
                }
            }
        }

        if(acceleratorLabel != null) {
            add(new Label(text, acceleratorLabel.getStyle())).align(Align.right).fill().padLeft(madePadding ? 3f : leftPadding);
        }
        else if(subMenu != null && showExpandArrow) {
            Image arrowImage = new Image(skin, "menu-arrow");
            arrowImage.setScaling(Scaling.none);
            add(arrowImage).align(Align.right).fill().padLeft(leftPadding);
        }

        pack();
    }

    public MenuItem setAccelerator(MenuAccelerator accelerator) {
        this.menuAccelerator = accelerator;
        String text = accelerator.toString();
        text = text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
        acceleratorLabel = new Label(text, getLabel().getStyle());
        refresh();

        if(!acceleratorItems.contains(this, true))
            acceleratorItems.add(this);

        return this;
    }

    public MenuAccelerator getMenuAccelerator() {
        return menuAccelerator;
    }

    public float getYLocation() {
        float yPos = getY();

        if(parent != null) {
            if(parent.parentMenuItem != null) {
                yPos += parent.parentMenuItem.getYLocation() - 66;
            }
            yPos += parent.getY();
        }

        return yPos;
    }

    @Override
    public int compareTo(Object o) {
        if(o instanceof MenuItem) {
            MenuItem other = (MenuItem)o;
            if(other.getText().length() > 0 && getText().length() > 0)
                return getText().charAt(0) - other.getText().charAt(0);
        }

        return 0;
    }
}
