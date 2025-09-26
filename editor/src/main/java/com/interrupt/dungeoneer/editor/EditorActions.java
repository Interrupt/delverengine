package com.interrupt.dungeoneer.editor;

import com.badlogic.gdx.Gdx;
import com.interrupt.dungeoneer.editor.file.SaveAdapter;
import com.interrupt.dungeoneer.editor.ui.menu.MenuAction;

public class EditorActions {
    public MenuAction newAction;
    public MenuAction saveAction;
    public MenuAction saveAsAction;
    public MenuAction openAction;
    public MenuAction exitAction;
    public MenuAction rotateLeftAction;
    public MenuAction rotateRightAction;
    public MenuAction playFromCameraAction;
    public MenuAction playFromStartAction;
    public MenuAction carveAction;
    public MenuAction paintAction;
    public MenuAction deleteAction;
    public MenuAction planeHeightAction;
    public MenuAction vertexHeightAction;
    public MenuAction vertexToggleAction;
    public MenuAction undoAction;
    public MenuAction redoAction;
    public MenuAction toggleGizmosAction;
    public MenuAction toggleLightsAction;
    public MenuAction escapeAction;
    public MenuAction rotateCeilTexAction;
    public MenuAction rotateFloorTexAction;
    public MenuAction rotateWallAngle;
    public MenuAction copyAction;
    public MenuAction pasteAction;
    public MenuAction moveTileNorthAction;
    public MenuAction moveTileSouthAction;
    public MenuAction moveTileEastAction;
    public MenuAction moveTileWestAction;
    public MenuAction moveTileUpAction;
    public MenuAction moveTileDownAction;
    public MenuAction raiseFloorAction;
    public MenuAction lowerFloorAction;
    public MenuAction raiseCeilingAction;
    public MenuAction lowerCeilingAction;
    public MenuAction paintWallAction;
    public MenuAction pickWallAction;
    public MenuAction pickNewWallTexAction;
    public MenuAction fillTextureAction;
    public MenuAction xDragMode;
    public MenuAction yDragMode;
    public MenuAction zDragMode;
    public MenuAction rotateMode;
    public MenuAction turnLeftAction;
    public MenuAction turnRightAction;
    public MenuAction flattenFloor;
    public MenuAction flattenCeiling;
    public MenuAction toggleSimulation;
    public MenuAction viewSelectedAction;

    public EditorActions() {
        initActions();
    }

    private void initActions() {
        newAction = new MenuAction() {
            public void invoke() {
                Editor.app.file.create();
            }
        };

        saveAction = new MenuAction() {
            @Override
            public void invoke() {
                Editor.app.file.save();
            }
        };

        saveAsAction = new MenuAction() {
            public void invoke() {
                Editor.app.file.saveAs(new SaveAdapter());
            }
        };

        openAction = new MenuAction() {
            public void invoke() {
                Editor.app.file.open();
            }
        };

        exitAction = new MenuAction() {
            public void invoke() {
                Gdx.app.exit();
            }
        };

        rotateLeftAction = new MenuAction() {
            public void invoke() {
                Editor.app.level.rotate90();
                Editor.app.refresh();
            }
        };

        rotateRightAction = new MenuAction() {
            public void invoke() {
                Editor.app.level.rotate90();
                Editor.app.level.rotate90();
                Editor.app.level.rotate90();
                Editor.app.refresh();
            }
        };

        playFromCameraAction = new MenuAction() {
            public void invoke() {
                Editor.app.testLevel(true);
            }
        };

        playFromStartAction = new MenuAction() {
            public void invoke() {
                Editor.app.testLevel(false);
            }
        };

        carveAction = new MenuAction() {
            @Override
            public void invoke() {
                Editor.app.doCarve();
            }
        };

        paintAction = new MenuAction() {
            @Override
            public void invoke() {
                Editor.app.doPaint();
            }
        };

        deleteAction = new MenuAction() {
            @Override
            public void invoke() {
                Editor.app.doDelete();
            }
        };

        planeHeightAction = new MenuAction() {
            @Override
            public void invoke() {
                Editor.app.setPlaneHeightMode();
            }
        };

        vertexHeightAction = new MenuAction() {
            @Override
            public void invoke() {
                Editor.app.setVertexHeightMode();
            }
        };

        vertexToggleAction = new MenuAction() {
            @Override
            public void invoke() {
                Editor.app.toggleVertexHeightMode();
            }
        };

        undoAction = new MenuAction() {
            @Override
            public void invoke() {
                Editor.app.undo();
            }
        };

        redoAction = new MenuAction() {
            @Override
            public void invoke() {
                Editor.app.redo();
            }
        };

        toggleGizmosAction = new MenuAction() {
            @Override
            public void invoke() {
                Editor.app.toggleGizmos();
            }
        };

        toggleLightsAction = new MenuAction() {
            @Override
            public void invoke() {
                Editor.app.toggleLights();
            }
        };

        escapeAction = new MenuAction() {
            @Override
            public void invoke() {
                Editor.app.clearSelection();
            }
        };

        rotateCeilTexAction = new MenuAction() {
            @Override
            public void invoke() {
                Editor.app.rotateCeilTex(1);
            }
        };

        rotateFloorTexAction = new MenuAction() {
            @Override
            public void invoke() {
                Editor.app.rotateFloorTex(1);
            }
        };

        raiseFloorAction = new MenuAction() {
            @Override
            public void invoke() {
                Editor.app.doFloorMoveUp();
            }
        };

        lowerFloorAction = new MenuAction() {
            @Override
            public void invoke() {
                Editor.app.doFloorMoveDown();
            }
        };

        raiseCeilingAction = new MenuAction() {
            @Override
            public void invoke() {
                Editor.app.doCeilMoveUp();
            }
        };

        lowerCeilingAction = new MenuAction() {
            @Override
            public void invoke() {
                Editor.app.doCeilMoveDown();
            }
        };

        paintWallAction = new MenuAction() {
            @Override
            public void invoke() {
                Editor.app.paintSurfaceAtCursor();
            }
        };

        pickWallAction = new MenuAction() {
            @Override
            public void invoke() {
                Editor.app.pickTextureAtSurface();
            }
        };

        pickNewWallTexAction = new MenuAction() {
            @Override
            public void invoke() {
                Editor.app.pickNewSurfaceTexture();
            }
        };

        fillTextureAction = new MenuAction() {
            @Override
            public void invoke() {
                Editor.app.fillSurfaceTexture();
            }
        };

        rotateWallAngle = new MenuAction() {
            @Override
            public void invoke() {
                Editor.app.rotateAngle();
            }
        };

        copyAction = new MenuAction() {
            @Override
            public void invoke() {
                EditorClipboard.copy();
            }
        };

        pasteAction = new MenuAction() {
            @Override
            public void invoke() {
                EditorClipboard.paste();
            }
        };

        moveTileNorthAction = new MenuAction() {
            @Override
            public void invoke() {
                Editor.app.moveTiles(0, 1, 0);
            }
        };

        moveTileSouthAction = new MenuAction() {
            @Override
            public void invoke() {
                Editor.app.moveTiles(0, -1, 0);
            }
        };

        moveTileEastAction = new MenuAction() {
            @Override
            public void invoke() {
                Editor.app.moveTiles(1, 0, 0);
            }
        };

        moveTileWestAction = new MenuAction() {
            @Override
            public void invoke() {
                Editor.app.moveTiles(-1, 0, 0);
            }
        };

        moveTileUpAction = new MenuAction() {
            @Override
            public void invoke() {
                Editor.app.moveTiles(0, 0, 0.5f);
            }
        };

        moveTileDownAction = new MenuAction() {
            @Override
            public void invoke() {
                Editor.app.moveTiles(0, 0, -0.5f);
            }
        };

        toggleSimulation = new MenuAction() {
            @Override
            public void invoke() {
                Editor.app.toggleSimulation();
            }
        };

        viewSelectedAction = new MenuAction() {
            @Override
            public void invoke() {
                Editor.app.viewSelected();
            }
        };

        xDragMode = new MenuAction() {
            @Override
            public void invoke() {
                Editor.app.setDragMode(EditorApplication.DragMode.X);
            }
        };
        yDragMode = new MenuAction() {
            @Override
            public void invoke() {
                Editor.app.setDragMode(EditorApplication.DragMode.Y);
            }
        };
        zDragMode = new MenuAction() {
            @Override
            public void invoke() {
                Editor.app.setDragMode(EditorApplication.DragMode.Z);
            }
        };

        rotateMode = new MenuAction() {
            @Override
            public void invoke() {
                Editor.app.setMoveMode(EditorApplication.MoveMode.ROTATE);
            }
        };

        turnLeftAction = new MenuAction() {
            @Override
            public void invoke() {
                Editor.app.turnPickedEntityLeft();
                Editor.app.refresh();
            }
        };

        turnRightAction = new MenuAction() {
            @Override
            public void invoke() {
                Editor.app.turnPickedEntityRight();
                Editor.app.refresh();
            }
        };

        flattenFloor = new MenuAction() {
            @Override
            public void invoke() {
                Editor.app.flattenFloor();
            }
        };

        flattenCeiling = new MenuAction() {
            @Override
            public void invoke() {
                Editor.app.flattenCeiling();
            }
        };
    }
}
