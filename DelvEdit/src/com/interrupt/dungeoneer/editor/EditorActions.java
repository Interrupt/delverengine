package com.interrupt.dungeoneer.editor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.interrupt.dungeoneer.editor.ui.EditorUi;
import com.interrupt.dungeoneer.editor.ui.FilePicker;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileFilter;

public class EditorActions {
    public ActionListener saveAction;
    public ActionListener saveAsAction;
    public ActionListener openAction;
    public ActionListener exitAction;
    public ActionListener rotateLeftAction;
    public ActionListener rotateRightAction;
    public ActionListener playAction;
    public ActionListener carveAction;
    public ActionListener paintAction;
    public ActionListener deleteAction;
    public ActionListener planeHeightAction;
    public ActionListener vertexHeightAction;
    public ActionListener vertexToggleAction;
    public ActionListener undoAction;
    public ActionListener redoAction;
    public ActionListener toggleGizmosAction;
    public ActionListener toggleLightsAction;
    public ActionListener escapeAction;
    public ActionListener rotateCeilTexAction;
    public ActionListener rotateFloorTexAction;
    public ActionListener rotateWallAngle;
    public ActionListener copyAction;
    public ActionListener pasteAction;
    public ActionListener moveTileNorthAction;
    public ActionListener moveTileSouthAction;
    public ActionListener moveTileEastAction;
    public ActionListener moveTileWestAction;
    public ActionListener moveTileUpAction;
    public ActionListener moveTileDownAction;
    public ActionListener raiseFloorAction;
    public ActionListener lowerFloorAction;
    public ActionListener raiseCeilingAction;
    public ActionListener lowerCeilingAction;
    public ActionListener paintWallAction;
    public ActionListener pickWallAction;
    public ActionListener pickNewWallTexAction;
    public ActionListener fillTextureAction;
    public ActionListener xDragMode;
    public ActionListener yDragMode;
    public ActionListener zDragMode;
    public ActionListener rotateMode;
    public ActionListener flattenFloor;
    public ActionListener flattenCeiling;
    public ActionListener toggleSimulation;
    public ActionListener viewSelectedAction;

    public EditorActions() {
        initActions();
    }

    private void initActions() {
        saveAction = new ActionListener() {
            public void actionPerformed (ActionEvent event) {
                // Invoke the save as dialog if we haven't saved anything yet
                if(Editor.app.currentFileName == null || Editor.app.currentDirectory == null) {
                    saveAsAction.actionPerformed(event);
                }
                else {
                    Editor.app.save(Editor.app.currentDirectory + Editor.app.currentFileName);
                    Editor.app.setTitle(Editor.app.currentFileName);
                }
            }
        };

        saveAsAction = new ActionListener() {
            public void actionPerformed (ActionEvent event) {
                class WSFilter implements FileFilter {
                    @Override
                    public boolean accept(File pathname) {
                        return (pathname.getName().endsWith(".bin") || pathname.getName().endsWith(".dat"));
                    }
                }

                FileFilter wsFilter = new WSFilter();

                if(Editor.app.currentDirectory == null) {
                    Editor.app.currentDirectory = new FileHandle(".").file().getAbsolutePath();
                    Editor.app.currentDirectory = Editor.app.currentDirectory.substring(0, Editor.app.currentDirectory.length() - 2);
                }

                FilePicker picker = FilePicker.createSaveDialog("Save Level", EditorUi.getSmallSkin(), new FileHandle(Editor.app.currentDirectory));
                picker.setFileNameEnabled(true);
                picker.setNewFolderEnabled(false);
                picker.setFilter(wsFilter);

                if(Editor.app.currentFileName == null) {
                    picker.setFileName("level.bin");
                }
                else {
                    picker.setFileName(Editor.app.currentFileName);
                }

                picker.setResultListener(new FilePicker.ResultListener() {
                    @Override
                    public boolean result(boolean success, FileHandle result) {
                        if(success) {
                            try {
                                Editor.app.save(result.file().getAbsolutePath());
                                Editor.app.currentDirectory = result.file().getParent() + "/";
                                Editor.app.currentFileName = result.name();

                                Editor.app.setTitle(Editor.app.currentFileName);
                            }
                            catch(Exception ex) {
                                Gdx.app.error("DelvEdit", ex.getMessage());
                            }
                        }
                        return true;
                    }
                });

                picker.show(Editor.app.ui.getStage());

                Editor.app.ui.showingModal = picker;
                Editor.app.editorInput.resetKeys();
            }
        };

        openAction = new ActionListener() {
            public void actionPerformed (ActionEvent event) {
                class WSFilter implements FileFilter {
                    @Override
                    public boolean accept(File path) {
                        String name = path.getName();
                        return (name.endsWith(".dat") || name.endsWith(".png") || name.endsWith(".bin"));
                    }
                }
                FileFilter wsFilter = new WSFilter();

                if(Editor.app.currentDirectory == null) {
                    Editor.app.currentDirectory = new FileHandle(".").file().getAbsolutePath();
                    Editor.app.currentDirectory = Editor.app.currentDirectory.substring(0, Editor.app.currentDirectory.length() - 2);
                }

                FilePicker picker = FilePicker.createLoadDialog("Open Level", EditorUi.getSmallSkin(), new FileHandle(Editor.app.currentDirectory));
                picker.setFileNameEnabled(true);
                picker.setNewFolderEnabled(false);
                if(Editor.app.currentFileName != null) picker.setFileName(Editor.app.currentFileName);
                picker.setFilter(wsFilter);

                picker.setResultListener(new FilePicker.ResultListener() {
                    @Override
                    public boolean result(boolean success, FileHandle result) {
                        if(success) {
                            Editor.app.open(result);
                        }

                        return true;
                    }
                });

                picker.show(Editor.app.ui.getStage());

                Editor.app.ui.showingModal = picker;
                Editor.app.editorInput.resetKeys();
            }
        };

        exitAction = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Editor.app.frame.dispatchEvent(new WindowEvent(Editor.app.frame, WindowEvent.WINDOW_CLOSING));
            }
        };

        rotateLeftAction = new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                Editor.app.level.rotate90();
                Editor.app.refresh();
            }
        };

        rotateRightAction = new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                Editor.app.level.rotate90();
                Editor.app.level.rotate90();
                Editor.app.level.rotate90();
                Editor.app.refresh();
            }
        };

        playAction = new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                Editor.app.testLevel();
            }
        };

        carveAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                Editor.app.doCarve();
            }
        };

        paintAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                Editor.app.doPaint();
            }
        };

        deleteAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                Editor.app.doDelete();
            }
        };

        planeHeightAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                Editor.app.setPlaneHeightMode();
            }
        };

        vertexHeightAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                Editor.app.setVertexHeightMode();
            }
        };

        vertexToggleAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                Editor.app.toggleVertexHeightMode();
            }
        };

        undoAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                Editor.app.undo();
            }
        };

        redoAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                Editor.app.redo();
            }
        };

        toggleGizmosAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                Editor.app.toggleGizmos();
            }
        };

        toggleLightsAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                Editor.app.toggleLights();
            }
        };

        escapeAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                Editor.app.clearSelection();
            }
        };

        rotateCeilTexAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                Editor.app.rotateCeilTex(1);
            }
        };

        rotateFloorTexAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                Editor.app.rotateFloorTex(1);
            }
        };

        raiseFloorAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                Editor.app.doFloorMoveUp();
            }
        };

        lowerFloorAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                Editor.app.doFloorMoveDown();
            }
        };

        raiseCeilingAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                Editor.app.doCeilMoveUp();
            }
        };

        lowerCeilingAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                Editor.app.doCeilMoveDown();
            }
        };

        paintWallAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                Editor.app.paintSurfaceAtCursor();
            }
        };

        pickWallAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                Editor.app.pickTextureAtSurface();
            }
        };

        pickNewWallTexAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                Editor.app.pickNewSurfaceTexture();
            }
        };

        fillTextureAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                Editor.app.fillSurfaceTexture();
            }
        };

        rotateWallAngle = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                Editor.app.rotateAngle();
            }
        };

        copyAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                Editor.app.copy();
            }
        };

        pasteAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                Editor.app.paste();
            }
        };

        moveTileNorthAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                Editor.app.moveTiles(0, 1, 0);
            }
        };

        moveTileSouthAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                Editor.app.moveTiles(0, -1, 0);
            }
        };

        moveTileEastAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                Editor.app.moveTiles(1, 0, 0);
            }
        };

        moveTileWestAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                Editor.app.moveTiles(-1, 0, 0);
            }
        };

        moveTileUpAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                Editor.app.moveTiles(0, 0, 0.5f);
            }
        };

        moveTileDownAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                Editor.app.moveTiles(0, 0, -0.5f);
            }
        };

        toggleSimulation = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                Editor.app.toggleSimulation();
            }
        };

        viewSelectedAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Editor.app.viewSelected();
            }
        };

        xDragMode = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) { Editor.app.setDragMode(EditorApplication.DragMode.X);
            }
        };
        yDragMode = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) { Editor.app.setDragMode(EditorApplication.DragMode.Y);
            }
        };
        zDragMode = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) { Editor.app.setDragMode(EditorApplication.DragMode.Z);
            }
        };
        rotateMode = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) { Editor.app.setMoveMode(EditorApplication.MoveMode.ROTATE);
            }
        };

        flattenFloor = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                Editor.app.flattenFloor();
            }
        };

        flattenCeiling = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                Editor.app.flattenCeiling();
            }
        };
    }
}
