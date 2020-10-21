package com.interrupt.dungeoneer.editor;

import com.badlogic.gdx.Gdx;
import com.interrupt.dungeoneer.editor.file.SaveAdapter;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class EditorActions {
    public ActionListener newAction;
    public ActionListener saveAction;
    public ActionListener saveAsAction;
    public ActionListener openAction;
    public ActionListener exitAction;
    public ActionListener rotateLeftAction;
    public ActionListener rotateRightAction;
    public ActionListener playFromCameraAction;
    public ActionListener playFromStartAction;
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
    public ActionListener turnLeftAction;
    public ActionListener turnRightAction;
    public ActionListener flattenFloor;
    public ActionListener flattenCeiling;
    public ActionListener toggleSimulation;
    public ActionListener viewSelectedAction;

    public EditorActions() {
        initActions();
    }

    private void initActions() {
        newAction = new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                Editor.app.file.create();
            }
        };

        saveAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                Editor.app.file.save();
            }
        };

        saveAsAction = new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                Editor.app.file.saveAs(new SaveAdapter());
            }
        };

        openAction = new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                Editor.app.file.open();
            }
        };

        exitAction = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Gdx.app.exit();
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

        playFromCameraAction = new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                Editor.app.testLevel(true);
            }
        };

        playFromStartAction = new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                Editor.app.testLevel(false);
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
                EditorClipboard.copy();
            }
        };

        pasteAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                EditorClipboard.paste();
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
            public void actionPerformed(ActionEvent actionEvent) {
                Editor.app.setDragMode(EditorApplication.DragMode.X);
            }
        };
        yDragMode = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                Editor.app.setDragMode(EditorApplication.DragMode.Y);
            }
        };
        zDragMode = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                Editor.app.setDragMode(EditorApplication.DragMode.Z);
            }
        };

        rotateMode = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                Editor.app.setMoveMode(EditorApplication.MoveMode.ROTATE);
            }
        };

        turnLeftAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                Editor.app.turnPickedEntityLeft();
                Editor.app.refresh();
            }
        };

        turnRightAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                Editor.app.turnPickedEntityRight();
                Editor.app.refresh();
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
