package com.interrupt.dungeoneer.editor.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FillViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.interrupt.api.steam.SteamApi;
import com.interrupt.dungeoneer.editor.Editor;
import com.interrupt.dungeoneer.editor.EditorFrame;
import com.interrupt.dungeoneer.editor.EditorRightClickEntitiesMenu;
import com.interrupt.dungeoneer.editor.EditorRightClickMenu;
import com.interrupt.dungeoneer.editor.ui.menu.MenuAccelerator;
import com.interrupt.dungeoneer.editor.ui.menu.MenuItem;
import com.interrupt.dungeoneer.editor.ui.menu.Scene2dMenu;
import com.interrupt.dungeoneer.editor.ui.menu.Scene2dMenuBar;
import com.interrupt.dungeoneer.entities.Entity;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.game.Level;
import com.interrupt.dungeoneer.generator.RoomGenerator;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;

public class EditorUi {
    Stage stage;
    Table mainTable;

    public static Skin defaultSkin;
    public static Skin mediumSkin;
    public static Skin smallSkin;

    private ScrollPane entityPropertiesPane = null;
    private PropertiesMenu propertiesMenu = null;
    private Table sidebarTable = null;
    private Cell propertiesCell = null;

    Scene2dMenu rightClickMenu;
    Scene2dMenuBar menuBar;

    public Actor showingModal;

    ActionListener resizeWindowAction;
    ActionListener newWindowAction;
    ActionListener pickAction;
    ActionListener uploadModAction;
    ActionListener setThemeAction;
    ActionListener setFogSettingsAction;

    private Vector2 propertiesSize = new Vector2();

    private float rightClickTime;

    private EditorFrame editorFrame;

    Viewport viewport;

    private ActionListener makeLevelGeneratorAction(final String theme, final String roomGenerator, final EditorFrame editorFrame) {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                editorFrame.getLevel().editorMarkers.clear();
                editorFrame.getLevel().entities.clear();
                editorFrame.getLevel().theme = theme;
                editorFrame.getLevel().generated = true;
                editorFrame.getLevel().dungeonLevel = 0;
                editorFrame.getLevel().crop(0, 0, 17 * 5, 17 * 5);
                editorFrame.getLevel().roomGeneratorChance = 0.4f;
                editorFrame.getLevel().roomGeneratorType = roomGenerator;
                editorFrame.getLevel().generate(Level.Source.EDITOR);
                editorFrame.refresh();

                lastGeneratedLevelType = theme;
                lastGeneratedLevelRoomType = roomGenerator;
            }
        };
    }

    private static String lastGeneratedLevelType = "DUNGEON";
    private static String lastGeneratedLevelRoomType = "DUNGEON_ROOMS";
    private ActionListener makeAnotherLevelGeneratorAction(final EditorFrame editorFrame) {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                makeLevelGeneratorAction(lastGeneratedLevelType, lastGeneratedLevelRoomType, editorFrame).actionPerformed(actionEvent);
            }
        };
    }

    private ActionListener makeRoomGeneratorAction(final String generatorType, final EditorFrame editorFrame) {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                editorFrame.getLevel().editorMarkers.clear();
                editorFrame.getLevel().entities.clear();

                Level generatedLevel = new Level(17,17);
                generatedLevel.roomGeneratorType = generatorType;

                RoomGenerator g = new RoomGenerator(generatedLevel, generatorType);
                g.generate(true, true, true, true);

                editorFrame.getLevel().crop(0, 0, generatedLevel.width, generatedLevel.height);
                editorFrame.getLevel().paste(generatedLevel, 0, 0);

                editorFrame.refresh();

                lastGeneratedRoomType = generatorType;
            }
        };
    }

    private static String lastGeneratedRoomType = "DUNGEON_ROOMS";
    private ActionListener makeAnotherRoomGeneratorAction(final EditorFrame editorFrame) {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
            makeRoomGeneratorAction(lastGeneratedRoomType, editorFrame).actionPerformed(actionEvent);
            }
        };
    }

    public EditorUi(final Editor editor, final EditorFrame editorFrame) {
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

        this.editorFrame = editorFrame;

        // action listener for the new level dialog
        newWindowAction = new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                NewLevelDialog newLevelDialog = new NewLevelDialog(smallSkin) {
                    @Override
                    protected void result(Object object) {
                        if((Boolean)object == true) {
                            editorFrame.createNewLevel(getLevelWidth(), getLevelHeight());
                            editor.createdNewLevel();
                        }
                    }
                };

                newLevelDialog.show(stage);
            }
        };

        resizeWindowAction = new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                NewLevelDialog newLevelDialog = new NewLevelDialog(smallSkin) {
                    @Override
                    protected void result(Object object) {
                        if((Boolean)object == true)
                            editorFrame.resizeLevel(getLevelWidth(),getLevelHeight());
                    }
                };

                newLevelDialog.show(stage);
            }
        };

        setFogSettingsAction = new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                final SetFogDialog fogDialog = new SetFogDialog(smallSkin, editorFrame.getLevel()) {
                    @Override
                    protected void result(Object object) {
                    }
                };

                fogDialog.show(stage);
            }
        };

        setThemeAction = new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                SetThemeDialog themeDialog = new SetThemeDialog(smallSkin, editorFrame.getLevel()) {
                    @Override
                    protected void result(Object object) {
                        editorFrame.getLevel().theme = getSelectedTheme();
                    }
                };

                themeDialog.show(stage);
            }
        };

        // action listener for the editor pick action
        pickAction = new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                editorFrame.doPick();
            }
        };

        // make the menu bar
        menuBar = new Scene2dMenuBar(smallSkin);
        menuBar.addItem(new MenuItem("File", smallSkin)
            .addItem(new MenuItem("Save", smallSkin, editor.saveAction).setAccelerator(new MenuAccelerator(Keys.S, true, false)))
            .addItem(new MenuItem("Save As...", smallSkin, editor.saveAsAction).setAccelerator(new MenuAccelerator(Keys.S, true, true)))
            .addItem(new MenuItem("New", smallSkin, newWindowAction).setAccelerator(new MenuAccelerator(Keys.N, true, false)))
            .addItem(new MenuItem("Open", smallSkin, editor.openAction).setAccelerator(new MenuAccelerator(Keys.O, true, false)))
        );

        menuBar.addItem(new MenuItem("Edit", smallSkin)
                        .addItem(new MenuItem("Undo", smallSkin, editor.undoAction).setAccelerator(new MenuAccelerator(Keys.Z, true, false)))
                        .addItem(new MenuItem("Redo", smallSkin, editor.redoAction).setAccelerator(new MenuAccelerator(Keys.Y, true, false)))
                        .addItem(new MenuItem("Copy", smallSkin, editor.copyAction).setAccelerator(new MenuAccelerator(Keys.C, true, false)))
                        .addItem(new MenuItem("Paste", smallSkin, editor.pasteAction).setAccelerator(new MenuAccelerator(Keys.V, true, false)))
                        .addSeparator()
                        .addItem(new MenuItem("Carve Tiles", smallSkin, editor.carveAction).setAccelerator(new MenuAccelerator(Keys.ENTER, false, false)))
                        .addItem(new MenuItem("Paint Tiles", smallSkin, editor.paintAction).setAccelerator(new MenuAccelerator(Keys.ENTER, false, true)))
                        .addItem(new MenuItem("Delete", smallSkin, editor.deleteAction).setAccelerator(new MenuAccelerator(Keys.DEL, false, false)))
                        .addItem(new MenuItem("Pick Textures", smallSkin, pickAction).setAccelerator(new MenuAccelerator(Keys.G, false, false)))
                        .addItem(new MenuItem("Reset Selection", smallSkin, editor.escapeAction).setAccelerator(new MenuAccelerator(Keys.ESCAPE, false, false)))
                        .addSeparator()
                        .addItem(new MenuItem("Height Edit Mode", smallSkin)
                                        .addItem(new MenuItem("Plane", smallSkin, editor.planeHeightAction))
                                        .addItem(new MenuItem("Vertex", smallSkin, editor.vertexHeightAction))
                                        .addItem(new MenuItem("Toggle", smallSkin, editor.vertexToggleAction).setAccelerator(new MenuAccelerator(Keys.V, false, false)))
                        )
                        .addItem(new MenuItem("Rotate Texture", smallSkin)
                                        .addItem(new MenuItem("Floor", smallSkin, editor.rotateFloorTexAction).setAccelerator(new MenuAccelerator(Keys.T, false, false)))
                                        .addItem(new MenuItem("Ceiling", smallSkin, editor.rotateCeilTexAction).setAccelerator(new MenuAccelerator(Keys.T, false, true)))
                        )
                        .addItem(new MenuItem("Surface", smallSkin)
                                .addItem(new MenuItem("Paint Surface Texture", smallSkin, editor.paintWallAction).setAccelerator(new MenuAccelerator(Keys.NUM_1, false, false)))
                                .addItem(new MenuItem("Grab Surface Texture", smallSkin, editor.pickWallAction).setAccelerator(new MenuAccelerator(Keys.NUM_2, false, false)))
                                .addItem(new MenuItem("Pick Surface Texture", smallSkin, editor.pickNewWallTexAction).setAccelerator(new MenuAccelerator(Keys.NUM_2, false, true)))
                                .addItem(new MenuItem("Flood Fill Surface Texture", smallSkin, editor.fillTextureAction).setAccelerator(new MenuAccelerator(Keys.NUM_1, false, true)))
                        )
                        .addItem(new MenuItem("Tiles", smallSkin)
                                .addItem(new MenuItem("Raise Floor", smallSkin, editor.raiseFloorAction).setAccelerator(new MenuAccelerator(Keys.NUM_3, false, false)))
                                .addItem(new MenuItem("Lower Floor", smallSkin, editor.lowerFloorAction).setAccelerator(new MenuAccelerator(Keys.NUM_3, false, true)))
                                .addItem(new MenuItem("Raise Ceiling", smallSkin, editor.raiseCeilingAction).setAccelerator(new MenuAccelerator(Keys.NUM_4, false, false)))
                                .addItem(new MenuItem("Lower Ceiling", smallSkin, editor.lowerCeilingAction).setAccelerator(new MenuAccelerator(Keys.NUM_4, false, true)))
                                .addSeparator()
                                .addItem(new MenuItem("Move North", smallSkin, editor.moveTileNorthAction).setAccelerator(new MenuAccelerator(Keys.UP, false, true)))
                                .addItem(new MenuItem("Move South", smallSkin, editor.moveTileSouthAction).setAccelerator(new MenuAccelerator(Keys.DOWN, false, true)))
                                .addItem(new MenuItem("Move East", smallSkin, editor.moveTileEastAction).setAccelerator(new MenuAccelerator(Keys.LEFT, false, true)))
                                .addItem(new MenuItem("Move West", smallSkin, editor.moveTileWestAction).setAccelerator(new MenuAccelerator(Keys.RIGHT, false, true)))
                                .addItem(new MenuItem("Move Up", smallSkin, editor.moveTileUpAction).setAccelerator(new MenuAccelerator(Keys.E, false, true)))
                                .addItem(new MenuItem("Move Down", smallSkin, editor.moveTileDownAction).setAccelerator(new MenuAccelerator(Keys.Q, false, true)))
                        )
                        .addItem(new MenuItem("Rotate Wall Angle", smallSkin, editor.rotateWallAngle).setAccelerator(new MenuAccelerator(Keys.U, false, false)))
                        .addItem(new MenuItem("Flatten", smallSkin)
                                .addItem(new MenuItem("Floor", smallSkin, editor.flattenFloor).setAccelerator(new MenuAccelerator(Keys.F, false, false)))
                                .addItem(new MenuItem("Ceiling", smallSkin, editor.flattenCeiling).setAccelerator(new MenuAccelerator(Keys.F, false, true))))
                        .addSeparator()
                        .addItem(new MenuItem("Rotate Level", smallSkin)
                                        .addItem(new MenuItem("Clockwise", smallSkin, editor.rotateLeftAction))
                                        .addItem(new MenuItem("Counter-Clockwise", smallSkin, editor.rotateRightAction)))
                        .addItem(new MenuItem("Move Mode", smallSkin)
                                        .addItem(new MenuItem("Clamp X", smallSkin, editor.xDragMode).setAccelerator(new MenuAccelerator(Keys.X, false, false)))
                                        .addItem(new MenuItem("Clamp Y", smallSkin, editor.yDragMode).setAccelerator(new MenuAccelerator(Keys.Y, false, false)))
                                        .addItem(new MenuItem("Clamp Z", smallSkin, editor.zDragMode).setAccelerator(new MenuAccelerator(Keys.Z, false, false)))
                                        .addItem(new MenuItem("Rotate", smallSkin, editor.rotateMode).setAccelerator(new MenuAccelerator(Keys.R, false, false)))
                        )
                        .addItem(new MenuItem("Generate Room", smallSkin, makeAnotherRoomGeneratorAction(editorFrame)).setAccelerator(new MenuAccelerator(Keys.G, false, true))
                                        .addItem(new MenuItem("Dungeon Room", smallSkin, makeRoomGeneratorAction("DUNGEON_ROOMS", editorFrame)))
                                        .addItem(new MenuItem("Cave Room", smallSkin, makeRoomGeneratorAction("CAVE_ROOMS", editorFrame)))
                                        .addItem(new MenuItem("Sewer Room", smallSkin, makeRoomGeneratorAction("SEWER_ROOMS", editorFrame)))
                                        .addItem(new MenuItem("Temple Room", smallSkin, makeRoomGeneratorAction("TEMPLE_ROOMS", editorFrame)))
                        )
                        .addItem(new MenuItem("Generate Level", smallSkin, makeAnotherLevelGeneratorAction(editorFrame))
                            .addItem(new MenuItem("Dungeon", smallSkin, makeLevelGeneratorAction("DUNGEON", "DUNGEON_ROOMS", editorFrame)))
                            .addItem(new MenuItem("Cave", smallSkin, makeLevelGeneratorAction("CAVE", "CAVE_ROOMS", editorFrame)))
                            .addItem(new MenuItem("Sewer", smallSkin, makeLevelGeneratorAction("SEWER", "SEWER_ROOMS", editorFrame)))
                            .addItem(new MenuItem("Temple", smallSkin, makeLevelGeneratorAction("UNDEAD", "TEMPLE_ROOMS", editorFrame)))
                )
        );

        menuBar.addItem(
            new MenuItem("View", smallSkin)
                .addItem(new MenuItem("Toggle Simulation", smallSkin, editor.toggleSimulation).setAccelerator(new MenuAccelerator(Keys.B, false, false)))
                .addItem(new MenuItem("Toggle Collision Boxes", smallSkin, editor.toggleCollisionBoxesAction))
                .addItem(new MenuItem("Toggle Lights", smallSkin, editor.toggleLightsAction).setAccelerator(new MenuAccelerator(Keys.L, false, false)))
        );

        menuBar.addItem(
                new MenuItem("Level", smallSkin)
                        .addItem(new MenuItem("Test Level", smallSkin, editor.playAction).setAccelerator(new MenuAccelerator(Keys.P, false, false)))
                        .addSeparator()
                        .addItem(new MenuItem("Resize Level", smallSkin, resizeWindowAction))
                        .addItem(new MenuItem("Set Theme", smallSkin, setThemeAction))
                        .addItem(new MenuItem("Set Fog Settings", smallSkin, setFogSettingsAction))
        );

        if(SteamApi.api.isAvailable()) {
            menuBar.addItem(
                new MenuItem("Mods", smallSkin)
                    .addItem(new MenuItem("Upload Mod to Workshop", smallSkin, uploadModAction))
            );
        }

        menuBar.pack();

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
                    if(entityPropertiesPane != null && editorFrame.getHoveredEntity() == null) {
                        sidebarTable.setVisible(false);
                    }
                }

                if (button == Input.Buttons.LEFT && (touched == null || (rightClickMenu != null && !touched.isDescendantOf(rightClickMenu)))) {
                    hideContextMenu();
                } else if (button == Input.Buttons.RIGHT) {
                    rightClickTime = editorFrame.time;
                }

                // only let things through that touch the main area
                if(touched != mainTable) return true;

                return false;
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

    public void showEntityPropertiesMenu(EditorFrame editorFrame) {
        showEntityPropertiesMenu(editorFrame, true);
    }

    public void showEntityPropertiesMenu(EditorFrame editorFrame, boolean resetScroll) {
        if(editorFrame.getPickedEntity() != null) {
            Array<Entity> selected = new Array<Entity>();
            selected.add(editorFrame.getPickedEntity());
            selected.addAll(editorFrame.getAdditionalSelectedEntities());

            propertiesMenu = new PropertiesMenu(smallSkin, editorFrame, selected);

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
            }
            else {
                entityPropertiesPane.setWidget(propertiesMenu);
            }

            if(resetScroll) entityPropertiesPane.setScrollY(0f);

            propertiesSize.set(propertiesMenu.getWidth(), propertiesMenu.getHeight());
            resize(stage.getWidth(), stage.getHeight());

            sidebarTable.setVisible(true);
            stage.setScrollFocus(entityPropertiesPane);
        }
        else if(entityPropertiesPane != null) {
            sidebarTable.setVisible(false);
            entityPropertiesPane.setScrollY(0f);
        }
    }

    public void resize(float width, float height) {
        viewport.setWorldSize(width, height);
        viewport.update((int)width, (int)height, true);

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

        editorFrame.editorInput.resetKeys();
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
        editorFrame.editorInput.resetKeys();
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

            float currentTime = editorFrame.time;
            if (currentTime - rightClickTime > 0.5) return;

            if (editorFrame.getPickedEntity() != null || editorFrame.getHoveredEntity() != null) {
                Entity sel = editorFrame.getPickedEntity();
                if (sel == null) sel = editorFrame.getHoveredEntity();

                if (editorFrame.getMoveMode() == EditorFrame.MoveMode.ROTATE) {
                    editorFrame.clearEntitySelection();
                } else if (editorFrame.getAdditionalSelectedEntities().size == 0) {
                    EditorRightClickMenu menu = new EditorRightClickMenu(sel, editorFrame, null, editorFrame.getLevel());
                    showContextMenu(x, y, menu);
                } else {
                    EditorRightClickMenu menu = new EditorRightClickMenu(sel, editorFrame.getAdditionalSelectedEntities(), editorFrame, null, editorFrame.getLevel());
                    showContextMenu(x, y, menu);
                }
            } else {
                editorFrame.setSelected(true);
                showContextMenu(x, y, new EditorRightClickEntitiesMenu(smallSkin,
                        editorFrame.getIntersection().x,
                        editorFrame.getIntersection().z,
                        editorFrame.getIntersection().y,
                        editorFrame, editorFrame.getLevel()));
            }
        }
    }
}
