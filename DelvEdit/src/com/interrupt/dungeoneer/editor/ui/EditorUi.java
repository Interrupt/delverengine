package com.interrupt.dungeoneer.editor.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FillViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.interrupt.api.steam.SteamApi;
import com.interrupt.dungeoneer.editor.*;
import com.interrupt.dungeoneer.editor.ui.menu.*;
import com.interrupt.dungeoneer.editor.ui.menu.generator.LevelGeneratorMenuItem;
import com.interrupt.dungeoneer.editor.ui.menu.generator.RoomGeneratorMenuItem;
import com.interrupt.dungeoneer.entities.Entity;
import com.interrupt.dungeoneer.game.Game;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class EditorUi {
    Stage stage;
    Table mainTable;
    final EditorActions actions = new EditorActions();

    public static Skin defaultSkin;
    public static Skin mediumSkin;
    public static Skin smallSkin;

    private ScrollPane entityPropertiesPane = null;
    private PropertiesMenu propertiesMenu = null;
    private Table sidebarTable = null;
    private Cell<?> propertiesCell = null;

    Scene2dMenu rightClickMenu;
    Scene2dMenuBar menuBar;

    public Actor showingModal;

    ActionListener resizeWindowAction;
    ActionListener pickAction;
    ActionListener uploadModAction;
    ActionListener setThemeAction;

    private final Vector2 propertiesSize = new Vector2();

    private float rightClickTime;

    Viewport viewport;

    public EditorUi() {
        defaultSkin = new Skin(Game.getInternal("ui/editor/HoloSkin/Holo-dark-hdpi.json"),
                new TextureAtlas(Game.getInternal("ui/editor/HoloSkin/Holo-dark-hdpi.atlas")));

        mediumSkin = new Skin(Game.getInternal("ui/editor/HoloSkin/Holo-dark-mdpi.json"),
                new TextureAtlas(Game.getInternal("ui/editor/HoloSkin/Holo-dark-mdpi.atlas")));

        smallSkin = new Skin(Game.getInternal("ui/editor/HoloSkin/Holo-dark-ldpi.json"),
                new TextureAtlas(Game.getInternal("ui/editor/HoloSkin/Holo-dark-ldpi.atlas")));

        viewport = new FillViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        stage = new Stage(viewport);

        mainTable = new Table();
        mainTable.setFillParent(true);
        mainTable.align(Align.left | Align.top);

        resizeWindowAction = new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                NewLevelDialog newLevelDialog = new NewLevelDialog(smallSkin) {
                    @Override
                    protected void result(Object object) {
                        if((Boolean)object)
                            Editor.app.resizeLevel(getLevelWidth(),getLevelHeight());
                    }
                };

                newLevelDialog.show(stage);
            }
        };

        setThemeAction = new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                SetThemeDialog themeDialog = new SetThemeDialog(smallSkin, Editor.app.getLevel()) {
                    @Override
                    protected void result(Object object) {
                        Editor.app.getLevel().theme = getSelectedTheme();
                    }
                };

                themeDialog.show(stage);
            }
        };

        // action listener for the editor pick action
        pickAction = new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                Editor.app.doPick();
            }
        };

        MenuItem openRecent = new DynamicMenuItem("Open Recent", smallSkin, new DynamicMenuItemAction() {
            private String mostRecentFile = null;

            @Override
            public boolean isDirty() {
                String first = "";

                if (Editor.options.recentlyOpenedFiles.size > 0) {
                    first = Editor.options.recentlyOpenedFiles.first();
                }

                return !first.equals(mostRecentFile);
            }

            @Override
            public void updateMenuItem(MenuItem item) {
                // Update most recent file. Used for isDirty check.
                if (Editor.options.recentlyOpenedFiles.size > 0) {
                    mostRecentFile = Editor.options.recentlyOpenedFiles.first();
                }
                else {
                    mostRecentFile = "";
                }

                // Clear existing items for a clean rebuild.
                if (item.subMenu != null) {
                    item.subMenu.items.clear();
                }

                // Build sequence of recent level menu items.
                int recentFilesAdded = 0;
                for (final String recentFile : Editor.options.recentlyOpenedFiles) {
                    item.addItem(
                        new MenuItem(recentFile, smallSkin, new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                FileHandle fh = Gdx.files.absolute(recentFile);
                                Editor.app.file.open(fh);
                            }
                        })
                    );

                    recentFilesAdded++;

                    int maximumFilesToShow = 10;
                    if (recentFilesAdded >= maximumFilesToShow) {
                        break;
                    }
                }

                if (recentFilesAdded == 0) {
                    MenuItem mi = new MenuItem("No Recent Files", smallSkin);
                    mi.getLabel().setColor(0.5f, 0.5f, 0.5f, 1);
                    item.addItem(mi);
                }

                item.addSeparator();
                item.addItem(new MenuItem("Clear Recently Opened", smallSkin, new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        Editor.options.recentlyOpenedFiles.clear();
                        Editor.options.save();
                    }
                }));
            }
        });

        // make the menu bar
        menuBar = new Scene2dMenuBar(smallSkin);
        menuBar.addItem(new MenuItem("File", smallSkin)
            .addItem(new MenuItem("New", smallSkin, actions.newAction).setAccelerator(new MenuAccelerator(Keys.N, true, false)))
            .addItem(new MenuItem("Open", smallSkin, actions.openAction).setAccelerator(new MenuAccelerator(Keys.O, true, false)))
            .addItem(openRecent)
            .addSeparator()
            .addItem(new MenuItem("Save", smallSkin, actions.saveAction).setAccelerator(new MenuAccelerator(Keys.S, true, false)))
            .addItem(new MenuItem("Save As...", smallSkin, actions.saveAsAction).setAccelerator(new MenuAccelerator(Keys.S, true, true)))
            .addSeparator()
            .addItem(new MenuItem("Exit", smallSkin, actions.exitAction))
        );

        MenuItem.acceleratorItems.add(new MenuItem("Delete", smallSkin, actions.deleteAction).setAccelerator(new MenuAccelerator(Keys.FORWARD_DEL, false, false)));

        menuBar.addItem(new MenuItem("Edit", smallSkin)
            .addItem(new MenuItem("Undo", smallSkin, actions.undoAction).setAccelerator(new MenuAccelerator(Keys.Z, true, false)))
            .addItem(new MenuItem("Redo", smallSkin, actions.redoAction).setAccelerator(new MenuAccelerator(Keys.Y, true, false)))
            .addSeparator()
            .addItem(new MenuItem("Copy", smallSkin, actions.copyAction).setAccelerator(new MenuAccelerator(Keys.C, true, false)))
            .addItem(new MenuItem("Paste", smallSkin, actions.pasteAction).setAccelerator(new MenuAccelerator(Keys.V, true, false)))
        );

        menuBar.addItem(
            new MenuItem("Tile", smallSkin)
                .addItem(new MenuItem("Carve", smallSkin, actions.carveAction).setAccelerator(new MenuAccelerator(Keys.ENTER, false, false)))
                .addItem(new MenuItem("Paint", smallSkin, actions.paintAction).setAccelerator(new MenuAccelerator(Keys.ENTER, false, true)))
                .addItem(new MenuItem("Delete", smallSkin, actions.deleteAction).setAccelerator(new MenuAccelerator(Keys.DEL, false, false)))
                .addItem(new MenuItem("Deselect", smallSkin, actions.escapeAction).setAccelerator(new MenuAccelerator(Keys.ESCAPE, false, false)))
                .addSeparator()
                .addItem(new MenuItem("Height Edit Mode", smallSkin)
                    .addItem(new MenuItem("Plane", smallSkin, actions.planeHeightAction))
                    .addItem(new MenuItem("Vertex", smallSkin, actions.vertexHeightAction))
                    .addItem(new MenuItem("Toggle", smallSkin, actions.vertexToggleAction).setAccelerator(new MenuAccelerator(Keys.V, false, false)))
                )
                .addItem(new MenuItem("Raise/Lower", smallSkin)
                    .addItem(new MenuItem("Raise Floor", smallSkin, actions.raiseFloorAction).setAccelerator(new MenuAccelerator(Keys.NUM_3, false, false)))
                    .addItem(new MenuItem("Lower Floor", smallSkin, actions.lowerFloorAction).setAccelerator(new MenuAccelerator(Keys.NUM_3, false, true)))
                    .addItem(new MenuItem("Raise Ceiling", smallSkin, actions.raiseCeilingAction).setAccelerator(new MenuAccelerator(Keys.NUM_4, false, false)))
                    .addItem(new MenuItem("Lower Ceiling", smallSkin, actions.lowerCeilingAction).setAccelerator(new MenuAccelerator(Keys.NUM_4, false, true)))
                )
                .addItem(new MenuItem("Move", smallSkin)
                    .addItem(new MenuItem("Move North", smallSkin, actions.moveTileNorthAction).setAccelerator(new MenuAccelerator(Keys.UP, false, true)))
                    .addItem(new MenuItem("Move South", smallSkin, actions.moveTileSouthAction).setAccelerator(new MenuAccelerator(Keys.DOWN, false, true)))
                    .addItem(new MenuItem("Move East", smallSkin, actions.moveTileEastAction).setAccelerator(new MenuAccelerator(Keys.LEFT, false, true)))
                    .addItem(new MenuItem("Move West", smallSkin, actions.moveTileWestAction).setAccelerator(new MenuAccelerator(Keys.RIGHT, false, true)))
                    .addSeparator()
                    .addItem(new MenuItem("Move Up", smallSkin, actions.moveTileUpAction).setAccelerator(new MenuAccelerator(Keys.E, false, true)))
                    .addItem(new MenuItem("Move Down", smallSkin, actions.moveTileDownAction).setAccelerator(new MenuAccelerator(Keys.Q, false, true)))
                )
                .addItem(new MenuItem("Rotate Wall Angle", smallSkin, actions.rotateWallAngle).setAccelerator(new MenuAccelerator(Keys.U, false, false)))
                .addItem(new MenuItem("Flatten", smallSkin)
                    .addItem(new MenuItem("Floor", smallSkin, actions.flattenFloor).setAccelerator(new MenuAccelerator(Keys.F, false, false)))
                    .addItem(new MenuItem("Ceiling", smallSkin, actions.flattenCeiling).setAccelerator(new MenuAccelerator(Keys.F, false, true)))
                )
                .addSeparator()
                .addItem(new MenuItem("Pick Textures", smallSkin, pickAction).setAccelerator(new MenuAccelerator(Keys.G, false, false)))
                .addItem(new MenuItem("Rotate Texture", smallSkin)
                    .addItem(new MenuItem("Floor", smallSkin, actions.rotateFloorTexAction).setAccelerator(new MenuAccelerator(Keys.T, false, false)))
                    .addItem(new MenuItem("Ceiling", smallSkin, actions.rotateCeilTexAction).setAccelerator(new MenuAccelerator(Keys.T, false, true)))
                )
                .addItem(new MenuItem("Surface", smallSkin)
                    .addItem(new MenuItem("Paint Surface Texture", smallSkin, actions.paintWallAction).setAccelerator(new MenuAccelerator(Keys.NUM_1, false, false)))
                    .addItem(new MenuItem("Grab Surface Texture", smallSkin, actions.pickWallAction).setAccelerator(new MenuAccelerator(Keys.NUM_2, false, false)))
                    .addItem(new MenuItem("Pick Surface Texture", smallSkin, actions.pickNewWallTexAction).setAccelerator(new MenuAccelerator(Keys.NUM_2, false, true)))
                    .addItem(new MenuItem("Flood Fill Surface Texture", smallSkin, actions.fillTextureAction).setAccelerator(new MenuAccelerator(Keys.NUM_1, false, true)))
                )
        );

        menuBar.addItem(
            new MenuItem("Entity", smallSkin)
                .addItem(new MenuItem("Delete", smallSkin, actions.deleteAction).setAccelerator(new MenuAccelerator(Keys.DEL, false, false)))
                .addItem(new MenuItem("Deselect", smallSkin, actions.escapeAction).setAccelerator(new MenuAccelerator(Keys.ESCAPE, false, false)))
                .addSeparator()
                .addItem(new MenuItem("Move", smallSkin)
                    .addItem(new MenuItem("Constrain to X-axis", smallSkin, actions.xDragMode).setAccelerator(new MenuAccelerator(Keys.X, false, false)))
                    .addItem(new MenuItem("Constrain to Y-axis", smallSkin, actions.yDragMode).setAccelerator(new MenuAccelerator(Keys.Y, false, false)))
                    .addItem(new MenuItem("Constrain to Z-axis", smallSkin, actions.zDragMode).setAccelerator(new MenuAccelerator(Keys.Z, false, false)))
                )
                .addItem(new MenuItem("Rotate", smallSkin, actions.rotateMode).setAccelerator(new MenuAccelerator(Keys.R, false, false)))
                .addItem(new MenuItem("Turn", smallSkin)
                        .addItem(new MenuItem("Clockwise", smallSkin, actions.turnLeftAction).setAccelerator(new MenuAccelerator(Keys.LEFT, true, false)))
                        .addItem(new MenuItem("Counter-clockwise", smallSkin, actions.turnRightAction).setAccelerator(new MenuAccelerator(Keys.RIGHT, true, false))))
        );

        menuBar.addItem(
            new MenuItem("View", smallSkin)
                .addItem(new MenuItem("Toggle Simulation", smallSkin, actions.toggleSimulation).setAccelerator(new MenuAccelerator(Keys.B, false, false)))
                .addItem(new MenuItem("Toggle Gizmos", smallSkin, actions.toggleGizmosAction))
                .addItem(new MenuItem("Toggle Lights", smallSkin, actions.toggleLightsAction).setAccelerator(new MenuAccelerator(Keys.L, false, false)))
                .addSeparator()
                .addItem(new MenuItem("View Selected", smallSkin, actions.viewSelectedAction).setAccelerator(new MenuAccelerator(Keys.SPACE, false, false)))
        );

        menuBar.addItem(
            new MenuItem("Level", smallSkin)
                        .addItem(new MenuItem("Play From Camera", smallSkin, actions.playFromCameraAction)
                                .setAccelerator(new MenuAccelerator(Keys.P, false, false)))
                        .addItem(new MenuItem("Play From Start", smallSkin, actions.playFromStartAction)
                                .setAccelerator(new MenuAccelerator(Keys.P, false, true)))
                .addSeparator()
                .addItem(new MenuItem("Rotate Level", smallSkin)
                    .addItem(new MenuItem("Clockwise", smallSkin, actions.rotateLeftAction))
                    .addItem(new MenuItem("Counter-clockwise", smallSkin, actions.rotateRightAction)))
                .addItem(new MenuItem("Resize Level", smallSkin, resizeWindowAction))
                .addSeparator()
                .addItem(new MenuItem("Set Theme", smallSkin, setThemeAction))
                .addSeparator()
                .addItem(new RoomGeneratorMenuItem(smallSkin))
                .addItem(new LevelGeneratorMenuItem(smallSkin))
        );

        if(SteamApi.api.isAvailable()) {
            menuBar.addItem(
                new MenuItem("Mods", smallSkin)
                    .addItem(new MenuItem("Upload Mod to Workshop", smallSkin, uploadModAction))
            );
        }

        menuBar.pack();

        Scene2dMenuBar.playButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (event.getType().name().equals("touchUp")) {
                    Editor.app.testLevel(false);
                }
            }
        });

        mainTable.setZIndex(1000);
        mainTable.add(menuBar);

        stage.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {

                Actor touched = stage.hit(x, y, false);

                if (!(event.getTarget() instanceof TextField)) stage.setKeyboardFocus(null);

                if((touched == null || !touched.isDescendantOf(menuBar))) {
                    menuBar.close();
                }

                // must have touched the stage, should we show an entity properties menu?
                if(button == Input.Buttons.LEFT && touched == mainTable) {
                    if(entityPropertiesPane != null && Editor.selection.hovered == null) {
                        sidebarTable.setVisible(false);
                    }
                }

                if (button == Input.Buttons.LEFT && (touched == null || (rightClickMenu != null && !touched.isDescendantOf(rightClickMenu)))) {
                    hideContextMenu();
                } else if (button == Input.Buttons.RIGHT) {
                    rightClickTime = Editor.app.time;
                }

                // only let things through that touch the main area
                return touched != mainTable;
            }

            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if(keycode == Keys.ESCAPE) {
                    for(Actor actor : stage.getActors()) {
                        if(actor instanceof TextureRegionPicker) {
                            TextureRegionPicker picker = (TextureRegionPicker)actor;
                            picker.hide();
                            return true;
                        }
                        else if(actor instanceof Dialog) {
                            ((Dialog)actor).hide();
                            return true;
                        }
                    }
                }

                return false;
            }
        });
    }

    public void showEntityPropertiesMenu(boolean resetScroll) {
        if(Editor.selection.picked != null) {
            Array<Entity> selected = new Array<Entity>();
            selected.add(Editor.selection.picked);
            selected.addAll(Editor.selection.selected);

            propertiesMenu = new PropertiesMenu(smallSkin, selected);

            if(entityPropertiesPane == null) {

                sidebarTable = new Table(smallSkin);
                sidebarTable.setBackground("scroll_opaque_gray");

                entityPropertiesPane = new ScrollPane(propertiesMenu, smallSkin, "opaque-gray");
                entityPropertiesPane.setFadeScrollBars(false);
                entityPropertiesPane.setFlickScroll(false);
                entityPropertiesPane.setClamp(true);

                sidebarTable.align(Align.top);
                propertiesCell = sidebarTable.add(entityPropertiesPane);

                stage.addActor(sidebarTable);

                // Only listen to events when mouse is hovering over ScrollPane.
                entityPropertiesPane.addListener(new EventListener() {
                    @Override
                    public boolean handle(Event event) {
                        if(event instanceof InputEvent) {
                            if (((InputEvent) event).getType() == InputEvent.Type.enter) {
                                event.getStage().setScrollFocus(entityPropertiesPane);
                            }
                        }
                        return false;
                    }
                });

                // Stop listening to events when mouse leaves ScrollPane.
                entityPropertiesPane.addListener(new EventListener() {
                    @Override
                    public boolean handle(Event event) {
                        if(event instanceof InputEvent) {
                            if (((InputEvent) event).getType() == InputEvent.Type.exit) {
                                event.getStage().setScrollFocus(null);
                            }
                        }
                        return false;
                    }
                });
            }
            else {
                entityPropertiesPane.setActor(propertiesMenu);
            }

            if(resetScroll) entityPropertiesPane.setScrollY(0f);

            propertiesSize.set(propertiesMenu.getWidth(), propertiesMenu.getHeight());
            resize(stage.getWidth(), stage.getHeight());

            sidebarTable.setVisible(true);
        }
        else if(entityPropertiesPane != null) {
            sidebarTable.setVisible(false);
            entityPropertiesPane.setScrollY(0f);
        }
    }

    public void resize(float width, float height) {
        viewport.setWorldSize(width, height);
        viewport.update((int)width, (int)height, true);

        menuBar.refresh();

        mainTable.pack();

        if(entityPropertiesPane != null && propertiesMenu != null) {
            boolean fillsStage = propertiesSize.y > stage.getHeight() - menuBar.getHeight();

            entityPropertiesPane.setSize(propertiesSize.x + (fillsStage ? 60f : 30f), propertiesSize.y);

            sidebarTable.setSize(entityPropertiesPane.getWidth(), stage.getHeight() - menuBar.getHeight());
            sidebarTable.setX(stage.getWidth() - sidebarTable.getWidth());
            sidebarTable.setY(0f);

            propertiesCell.width(entityPropertiesPane.getWidth());
        }
    }

    public void initUi() {
        stage.addActor(mainTable);
    }

    public void showModal(Actor modal) {
        showingModal = modal;
        stage.addActor(modal);

        if(modal instanceof TextureRegionPicker) {
            ((TextureRegionPicker) modal).show(stage);
        }

        Editor.app.editorInput.resetKeys();
    }

    public boolean isShowingModal() {
        if(showingModal == null || !showingModal.isVisible())
            return false;

        return stage.getActors().contains(showingModal, true);
    }

    public void showContextMenu(float x, float y) {
        stage.addActor(rightClickMenu);
        rightClickMenu.setPosition(x, y);
    }

    public void showContextMenu(float x, float y, Scene2dMenu contextMenu) {
        this.rightClickMenu = contextMenu;
        stage.addActor(rightClickMenu);
        rightClickMenu.setPosition(x, y);
        Editor.app.editorInput.resetKeys();
    }

    public void hideContextMenu() {
        stage.getRoot().removeActor(rightClickMenu);
    }

    public boolean isShowingContextMenu() {
        if(rightClickMenu == null || !rightClickMenu.isVisible())
            return false;

        return stage.getActors().contains(rightClickMenu, true);
    }

    public boolean isShowingMenuOrModal() {
        return isShowingModal() || isShowingContextMenu();
    }

    public Stage getStage() { return stage; }
    public static Skin getSkin() { return defaultSkin; }
    public static Skin getMediumSkin() { return mediumSkin; }
    public static Skin getSmallSkin() { return smallSkin; }

    public void touchUp(int x, int y, int pointer, int button) {
        if(button == Input.Buttons.RIGHT) {
            hideContextMenu();

            y = (int)stage.getHeight() - y;

            float currentTime = Editor.app.time;
            if (currentTime - rightClickTime > 0.5) return;

            if (Editor.selection.picked != null || Editor.selection.hovered != null) {
                Entity sel = Editor.selection.picked;
                if (sel == null) sel = Editor.selection.hovered;

                if (Editor.app.getMoveMode() == EditorApplication.MoveMode.ROTATE) {
                    Editor.app.clearEntitySelection();
                } else if (Editor.selection.selected.size == 0) {
                    EditorRightClickMenu menu = new EditorRightClickMenu(sel, Editor.app.getLevel());
                    showContextMenu(x, y, menu);
                } else {
                    EditorRightClickMenu menu = new EditorRightClickMenu(sel, Editor.selection.selected, Editor.app.getLevel());
                    showContextMenu(x, y, menu);
                }
            } else {
                Editor.app.setSelected(true);
                showContextMenu(x, y, new EditorRightClickEntitiesMenu(smallSkin,
                        Editor.app.getIntersection().x,
                        Editor.app.getIntersection().z,
                        Editor.app.getIntersection().y,
                        Editor.app.getLevel()));
            }
        }
    }

    public void touchDown(int x, int y, int pointer, int button) {
        if (button == Input.Buttons.RIGHT) {
            hideContextMenu();
        }
    }
}
