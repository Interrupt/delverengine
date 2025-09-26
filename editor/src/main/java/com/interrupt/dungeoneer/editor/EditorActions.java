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
        newAction = () -> Editor.app.file.create();

        saveAction = () -> Editor.app.file.save();

        saveAsAction = () -> Editor.app.file.saveAs(new SaveAdapter());

        openAction = () -> Editor.app.file.open();

        exitAction = () -> Gdx.app.exit();

        rotateLeftAction = () -> {
            Editor.app.level.rotate90();
            Editor.app.refresh();
        };

        rotateRightAction = () -> {
            Editor.app.level.rotate90();
            Editor.app.level.rotate90();
            Editor.app.level.rotate90();
            Editor.app.refresh();
        };

        playFromCameraAction = () -> Editor.app.testLevel(true);

        playFromStartAction = () -> Editor.app.testLevel(false);

        carveAction = () -> Editor.app.doCarve();

        paintAction = () -> Editor.app.doPaint();

        deleteAction = () -> Editor.app.doDelete();

        planeHeightAction = () -> Editor.app.setPlaneHeightMode();

        vertexHeightAction = () -> Editor.app.setVertexHeightMode();

        vertexToggleAction = () -> Editor.app.toggleVertexHeightMode();

        undoAction = () -> Editor.app.undo();

        redoAction = () -> Editor.app.redo();

        toggleGizmosAction = () -> Editor.app.toggleGizmos();

        toggleLightsAction = () -> Editor.app.toggleLights();

        escapeAction = () -> Editor.app.clearSelection();

        rotateCeilTexAction = () -> Editor.app.rotateCeilTex(1);

        rotateFloorTexAction = () -> Editor.app.rotateFloorTex(1);

        raiseFloorAction = () -> Editor.app.doFloorMoveUp();

        lowerFloorAction = () -> Editor.app.doFloorMoveDown();

        raiseCeilingAction = () -> Editor.app.doCeilMoveUp();

        lowerCeilingAction = () -> Editor.app.doCeilMoveDown();

        paintWallAction = () -> Editor.app.paintSurfaceAtCursor();

        pickWallAction = () -> Editor.app.pickTextureAtSurface();

        pickNewWallTexAction = () -> Editor.app.pickNewSurfaceTexture();

        fillTextureAction = () -> Editor.app.fillSurfaceTexture();

        rotateWallAngle = () -> Editor.app.rotateAngle();

        copyAction = () -> EditorClipboard.copy();

        pasteAction = () -> EditorClipboard.paste();

        moveTileNorthAction = () -> Editor.app.moveTiles(0, 1, 0);

        moveTileSouthAction = () -> Editor.app.moveTiles(0, -1, 0);

        moveTileEastAction = () -> Editor.app.moveTiles(1, 0, 0);

        moveTileWestAction = () -> Editor.app.moveTiles(-1, 0, 0);

        moveTileUpAction = () -> Editor.app.moveTiles(0, 0, 0.5f);

        moveTileDownAction = () -> Editor.app.moveTiles(0, 0, -0.5f);

        toggleSimulation = () -> Editor.app.toggleSimulation();

        viewSelectedAction = () -> Editor.app.viewSelected();

        xDragMode = () -> Editor.app.setDragMode(EditorApplication.DragMode.X);
        yDragMode = () -> Editor.app.setDragMode(EditorApplication.DragMode.Y);
        zDragMode = () -> Editor.app.setDragMode(EditorApplication.DragMode.Z);

        rotateMode = () -> Editor.app.setMoveMode(EditorApplication.MoveMode.ROTATE);

        turnLeftAction = () -> {
            Editor.app.turnPickedEntityLeft();
            Editor.app.refresh();
        };

        turnRightAction = () -> {
            Editor.app.turnPickedEntityRight();
            Editor.app.refresh();
        };

        flattenFloor = () -> Editor.app.flattenFloor();

        flattenCeiling = () -> Editor.app.flattenCeiling();
    }
}
