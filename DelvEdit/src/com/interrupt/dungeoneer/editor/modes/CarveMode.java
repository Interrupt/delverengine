package com.interrupt.dungeoneer.editor.modes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Array;
import com.interrupt.dungeoneer.collision.Collidor;
import com.interrupt.dungeoneer.editor.ControlPoint;
import com.interrupt.dungeoneer.editor.Editor;
import com.interrupt.dungeoneer.editor.EditorApplication;
import com.interrupt.dungeoneer.editor.gfx.SurfacePickerDecal;
import com.interrupt.dungeoneer.editor.selection.AdjacentTileSelectionInfo;
import com.interrupt.dungeoneer.editor.selection.TileSelection;
import com.interrupt.dungeoneer.editor.selection.TileSelectionInfo;
import com.interrupt.dungeoneer.game.Level;
import com.interrupt.dungeoneer.gfx.GlRenderer;
import com.interrupt.dungeoneer.tiles.Tile;
import com.interrupt.helpers.FloatTuple;

public class CarveMode extends EditorMode {
    public CarveMode() {
        super(EditorModes.CARVE);
    }

    public CarveMode(EditorModes mode) {
        super(mode);
    }

    // Some controls that subclasses can override as needed
    protected boolean canCarve = true;
    protected boolean canExtrude = true;
    protected boolean carveAutomatically = true;
    protected TileSelection tileSelectionSettings = new TileSelection();

    Vector3 selectionStart = new Vector3();

    Plane intersectPlane = new Plane();
    Vector3 intersectPoint = new Vector3();
    Vector3 intersectNormal = new Vector3();
    Vector3 intersectNormalPicked = new Vector3();

    private TileSelection hoverSelection;
    private Array<TileSelection> pickedTileSelections = new Array<>();

    boolean didPickSurface = false;
    EditorApplication.TileSurface extrudeFromSurface;
    FloatTuple pickedSurfaceCeilingPoints = new FloatTuple();
    FloatTuple pickedSurfaceFloorPoints = new FloatTuple();

    ControlPoint pickedControlPoint;

    protected enum CarveModeState { START, DRAGGING_SELECTION, SELECTED_TILES, SELECTED_CONTROL_POINT }
    CarveModeState state = CarveModeState.START;

    @Override
    public void tick() {
        if(state == CarveModeState.START) {
            tickStateStart();
        } else if(state == CarveModeState.DRAGGING_SELECTION) {
            tickStateDraggingSelection();
        } else if(state == CarveModeState.SELECTED_TILES) {
            tickStateSelectedTiles();
        } else if(state == CarveModeState.SELECTED_CONTROL_POINT) {
            tickStateSelectedControlPoint();
        }
    }

    Vector2 tileToCopyPos = new Vector2();
    public void tickStateStart() {
        Ray ray = Editor.app.camera.getPickRay(Gdx.input.getX(), Gdx.input.getY());
        intersectPlane.set(0, 1, 0, 0.5f);
        intersectNormal.set(0, 1, 0);

        // Start with a simple plane intersection
        Intersector.intersectRayPlane(ray, intersectPlane, intersectPoint);

        // But try to get a world intersection
        if (Collidor.intersectRayForwardFacingTriangles(ray, Editor.app.camera, GlRenderer.triangleSpatialHash.getAllTriangles(), intersectPoint, intersectNormal)) {
            // Got an intersection!
        }

        if(hoverSelection == null)
            hoverSelection = tileSelectionSettings.copy();

        // Tile selection indicator
        hoverSelection.width = 1;
        hoverSelection.height = 1;
        if(canExtrude) {
            hoverSelection.x = (int) (intersectPoint.x - intersectNormal.x * 0.5f);
            hoverSelection.y = (int) (intersectPoint.z - intersectNormal.z * 0.5f);
        } else {
            hoverSelection.x = (int) (intersectPoint.x + intersectNormal.x * 0.5f);
            hoverSelection.y = (int) (intersectPoint.z + intersectNormal.z * 0.5f);
        }
        hoverSelection.startX = hoverSelection.x;
        hoverSelection.startY = hoverSelection.y;

        // The actual copy we want to copy is the one on the other side of a wall collision, keep track of that
        tileToCopyPos.set(intersectPoint.x + intersectNormal.x * 0.1f, intersectPoint.z + intersectNormal.z * 0.1f);

        // Recalculate tile selection bounds!
        hoverSelection.getBounds();

        // Keep track of the hovered Pick Surface
        didPickSurface = false;
        if(canExtrude && Editor.app.pickedSurface != null && Editor.app.pickedSurface.isPicked) {
            didPickSurface = Editor.app.pickedSurface.tileSurface == EditorApplication.TileSurface.UpperWall ||
                Editor.app.pickedSurface.tileSurface == EditorApplication.TileSurface.LowerWall;

            pickedSurfaceFloorPoints.set(Editor.app.pickedSurface.floorPoints);
            pickedSurfaceCeilingPoints.set(Editor.app.pickedSurface.ceilingPoints);
        }

        if(!Editor.app.editorInput.isButtonPressed(Input.Buttons.LEFT))
            return;

        // Start over with a new set of tile selections unless Shift is pressed
        if(!Editor.app.editorInput.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
            pickedTileSelections.clear();
        }

        // Switch to the dragging state
        state = CarveModeState.DRAGGING_SELECTION;

        // Keep track of the initial click location
        selectionStart.set(intersectPoint.x, intersectPoint.y, intersectPoint.z);
        intersectNormalPicked.set(intersectNormal);

        // Save the surface being extruded, or default to the floor
        if(canExtrude && Editor.app.pickedSurface != null && Editor.app.pickedSurface.isPicked) {
            extrudeFromSurface = Editor.app.pickedSurface.tileSurface;
        }
    }

    public void tickStateDraggingSelection() {
        Ray ray = Editor.app.camera.getPickRay(Gdx.input.getX(), Gdx.input.getY());
        intersectPlane.set(0, 1, 0, -selectionStart.y);

        // Expand out the selection that was hovering before
        TileSelection tileSelection = hoverSelection;

        // Recalculate tile selection bounds!
        tileSelection.getBounds();

        // Get the intersection position
        Intersector.intersectRayPlane(ray, intersectPlane, intersectPoint);

        // Find how far we have dragged on the plane since the first selection
        float dragDistanceX = intersectPoint.x - selectionStart.x;
        float dragDistanceY = intersectPoint.z - selectionStart.z;

        tileSelection.fixup(dragDistanceX, dragDistanceY);

        // Quit here if we are still dragging
        if(Editor.app.editorInput.isButtonPressed(Input.Buttons.LEFT))
            return;

        // Switch to the selected state when done dragging
        state = CarveModeState.SELECTED_TILES;

        // A new tile selection was created
        pickedTileSelections.add(tileSelection.copy());

        // Carve automatically, unless shift is being held
        if(Editor.app.editorInput.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
            return;
        }

        // Simple case, carve automatically
        if(carveAutomatically)
            doCarve();
    }

    public void tickStateSelectedTiles() {
        // We have selected tiles! Can do a bunch of stuff now
        if(Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            doCarve();
        } else if(Gdx.input.isKeyJustPressed(Input.Keys.BACKSPACE)) {
            doErase();
        } else if(Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            for(TileSelection selection : pickedTileSelections) {
                tryPickingControlPoint(selection);
            }

            // Try starting over and selecting something new if no control point was just set
            if(state != CarveModeState.SELECTED_CONTROL_POINT)
                tickStateStart();
        }
    }

    public void tickStateSelectedControlPoint() {
        if(pickedControlPoint != null)
            dragControlPoint(pickedControlPoint);

        // Keep dragging the control point while the mouse is pressed
        if(Gdx.input.isButtonPressed(Input.Buttons.LEFT))
            return;

        state = CarveModeState.SELECTED_TILES;
        Editor.app.history.saveState(Editor.app.level);
    }

    protected boolean getPointerOverCeilingPlane(TileSelection selection) {
        // Try picking the ceiling
        intersectPlane.set(0, 1, 0, -selection.getBounds(false).max.z);
        intersectNormal.set(0, 1, 0);

        Ray ray = Editor.app.camera.getPickRay(Gdx.input.getX(), Gdx.input.getY());
        boolean hitPlane = Intersector.intersectRayPlane(ray, intersectPlane, intersectPoint);

        if (!hitPlane)
            return false;

        // Now check if the intersection is in the bounds
        if(intersectPoint.x < selection.x)
            return false;
        if(intersectPoint.x > selection.x + selection.width)
            return false;
        if(intersectPoint.z < selection.y)
            return false;
        if(intersectPoint.z > selection.y + selection.height)
            return false;

        return true;
    }

    protected boolean getPointerOverFloorPlane(TileSelection selection) {
        // Try picking the ceiling
        intersectPlane.set(0, 1, 0, -selection.getBounds(false).min.z);
        intersectNormal.set(0, 1, 0);

        Ray ray = Editor.app.camera.getPickRay(Gdx.input.getX(), Gdx.input.getY());
        boolean hitPlane = Intersector.intersectRayPlane(ray, intersectPlane, intersectPoint);

        if (!hitPlane)
            return false;

        // Now check if the intersection is in the bounds
        if(intersectPoint.x < selection.x)
            return false;
        if(intersectPoint.x > selection.x + selection.width)
            return false;
        if(intersectPoint.z < selection.y)
            return false;
        if(intersectPoint.z > selection.y + selection.height)
            return false;

        return true;
    }

    protected void tryPickingControlPoint(TileSelection selection) {
        if(getPointerOverCeilingPlane(selection)) {
            pickedControlPoint = new ControlPoint(new Vector3(intersectPoint), ControlPoint.ControlPointType.ceiling);
            pickedSurfaceCeilingPoints.set(intersectPoint.y, intersectPoint.y);
        } else if(getPointerOverFloorPlane(selection)) {
            pickedControlPoint = new ControlPoint(new Vector3(intersectPoint), ControlPoint.ControlPointType.floor);
            pickedSurfaceFloorPoints.set(intersectPoint.y, intersectPoint.y);
        } else {
            return;
        }

        // Switch to the moving control point state
        didStartDrag = false;
        didPickSurface = false;
        state = CarveModeState.SELECTED_CONTROL_POINT;
    }

    @Override
    public void draw() {
        // Draw the tile selections as bounding boxes
        Editor.app.boxRenderer.setColor(0.75f, 0.75f, 0.75f, 0.5f);

        if(state.ordinal() >= CarveModeState.SELECTED_TILES.ordinal())
            Editor.app.boxRenderer.setColor(1.0f, 0.25f, 0.25f, 0.75f);

        Editor.app.boxRenderer.begin(ShapeRenderer.ShapeType.Line);

        // Don't draw the hover indicator box when not hovering or dragging
        if(state.ordinal() <= CarveModeState.DRAGGING_SELECTION.ordinal()) {
            BoundingBox hoverBounds = didPickSurface ? hoverSelection.getBounds(pickedSurfaceFloorPoints, pickedSurfaceCeilingPoints) :
                hoverSelection.getBounds(false);

            Editor.app.boxRenderer.box(
                hoverBounds.min.x,
                hoverBounds.min.z,
                hoverBounds.min.y,
                hoverBounds.getWidth(),
                hoverBounds.getDepth(),
                -hoverBounds.getHeight()
            );
        }

        // Then draw all the picked selections
        for(int i = 0; i < pickedTileSelections.size; i++) {
            TileSelection selection = pickedTileSelections.get(i);
            BoundingBox bounds = selection.getBounds();

            Editor.app.boxRenderer.box(
                bounds.min.x,
                bounds.min.z,
                bounds.min.y,
                bounds.getWidth(),
                bounds.getDepth(),
                -bounds.getHeight()
            );
        }

        Editor.app.boxRenderer.end();

        // Can quit here unless we are in ceiling or floor move modes
        if(state.ordinal() < CarveModeState.SELECTED_TILES.ordinal())
            return;

        boolean isOverACeilingPlane = false;
        boolean isOverAFloorPlane = false;

        // Check if the mouse is over a selection floor or ceiling
        for(TileSelection selection : pickedTileSelections) {
            isOverACeilingPlane |= getPointerOverCeilingPlane(selection);
            isOverAFloorPlane |= getPointerOverFloorPlane(selection);

            if(isOverACeilingPlane || isOverAFloorPlane)
                break;
        }

        // Check if the mouse is over a selection floor or ceiling
        for(TileSelection selection : pickedTileSelections) {
            if(isOverACeilingPlane)
                renderSurfaceControlPoint(selection, true);
            else if(isOverAFloorPlane)
                renderSurfaceControlPoint(selection, false);
        }
    }

    @Override
    public void start() {
        state = CarveModeState.START;
        pickedControlPoint = null;
        didStartDrag = false;
        didPickSurface = false;
        extrudeFromSurface = EditorApplication.TileSurface.Floor;

        intersectPlane.set(0, 1, 0, 0.5f);
    }

    @Override
    public void reset() {
        state = CarveModeState.START;
        pickedControlPoint = null;
        didStartDrag = false;
        didPickSurface = false;
        extrudeFromSurface = EditorApplication.TileSurface.Floor;
        pickedTileSelections.clear();
    }

    protected void doCarve() {
        if(!canCarve)
            return;

        // Set the default tile to use when carving
        Tile t = new Tile();
        t.wallTex = (byte)Editor.app.pickedWallTexture;
        t.wallTexAtlas = Editor.app.pickedWallTextureAtlas;
        t.floorTex = (byte)Editor.app.pickedFloorTexture;
        t.floorTexAtlas = Editor.app.pickedFloorTextureAtlas;
        t.ceilTex = (byte)Editor.app.pickedCeilingTexture;
        t.ceilTexAtlas = Editor.app.pickedCeilingTextureAtlas;
        t.wallBottomTex = (byte)Editor.app.pickedWallBottomTexture;
        t.wallBottomTexAtlas = Editor.app.pickedWallBottomTextureAtlas;
        t.blockMotion = false;
        t.tileSpaceType = Tile.TileSpaceType.EMPTY;
        t.renderSolid = t.blockMotion;

        // Now go set the tiles
        for(TileSelection selection : pickedTileSelections) {
            setTiles(t, selection);
        }

        // Reset the extrude surface
        extrudeFromSurface = EditorApplication.TileSurface.Floor;

        // Save the history for undo
        Editor.app.history.saveState(Editor.app.level);
    }

    protected void eraseSelection(TileSelection selection) {
        Level level = Editor.app.level;
        for (TileSelectionInfo info : selection) {
            if(!canCarveTile(selection, info))
                continue;

            Tile n = level.getTile(info.x, info.y - 1);
            Tile s = level.getTile(info.x, info.y + 1);
            Tile e = level.getTile(info.x - 1, info.y);
            Tile w = level.getTile(info.x + 1, info.y);

            if(n.blockMotion && s.blockMotion && e.blockMotion && w.blockMotion) {
                level.setTile(info.x, info.y, null);
            }
            else {
                Tile t = Tile.NewSolidTile();
                t.wallTex = (byte)Editor.app.pickedWallTexture;
                t.wallTexAtlas = Editor.app.pickedWallTextureAtlas;
                level.setTile(info.x, info.y, t);
            }
        }

        // Now mark everything as dirty
        // FIXME: Just do this once, not per tile!
        for (TileSelectionInfo info : selection) {
            Editor.app.markWorldAsDirty(info.x, info.y, 1);
        }
    }

    public void doErase() {
        Level level = Editor.app.level;

        if(pickedTileSelections.size == 0)
            eraseSelection(hoverSelection);

        for(TileSelection selection : pickedTileSelections) {
            eraseSelection(selection);
        }

        // Save the history for undo
        Editor.app.history.saveState(Editor.app.level);
    }

    protected boolean canCarveTile(TileSelection selection, TileSelectionInfo info) {
        return true;
    }

    protected void setTiles(Tile tocopy, TileSelection selection) {
        // Are we extruding along an upper or lower wall?
        boolean isExtruding = extrudeFromSurface == EditorApplication.TileSurface.LowerWall ||
            extrudeFromSurface == EditorApplication.TileSurface.UpperWall;

        // Set the tile heights from the heights of this selection box
        BoundingBox tileBounds = selection.getBounds(false);
        tocopy.floorHeight = tileBounds.min.z;
        tocopy.ceilHeight = tileBounds.max.z;

        // Carve out selected tiles
        for (TileSelectionInfo info : selection) {
            if(!canCarveTile(selection, info))
                continue;

            Tile t = info.tile;
            if (t == null) {
                t = new Tile();
            }

            Tile existing = Editor.app.level.getTileOrNull(info.x, info.y);
            if(!isExtruding) {
                // Simple case, just copy the whole tile
                Tile.copy(tocopy, t);
                Editor.app.level.setTile(info.x, info.y, t);
            }
            else if(existing == null) {
                // Harder case, need a new tile but different floor/ceil heights
                Tile.copy(tocopy, t);

                // Extruding, so use the surface picker heights for any new tiles
                float ch = pickedSurfaceCeilingPoints.max();
                float fh = pickedSurfaceFloorPoints.min();

                t.ceilHeight = ch;
                t.floorHeight = fh;
                t.slopeNE = t.slopeNW = t.slopeSE = t.slopeSW = 0;
                t.ceilSlopeNE = t.ceilSlopeNW = t.ceilSlopeSE = t.ceilSlopeSW = 0;

                Editor.app.level.setTile(info.x, info.y, t);
            } else {
                // More complicated case, extruding the upper or lower walls
                if(extrudeFromSurface == EditorApplication.TileSurface.LowerWall) {
                    if(existing.renderSolid) {
                        existing.ceilHeight = pickedSurfaceCeilingPoints.max();
                        existing.ceilTex = tocopy.ceilTex;
                        existing.ceilTexAtlas = tocopy.ceilTexAtlas;
                    }

                    existing.floorHeight = pickedSurfaceFloorPoints.min();
                    existing.slopeNE = existing.slopeNW = existing.slopeSE = existing.slopeSW = 0;
                    existing.floorTex = tocopy.floorTex;
                    existing.floorTexAtlas = tocopy.floorTexAtlas;
                    existing.wallBottomTex = tocopy.wallBottomTex;
                    existing.wallBottomTexAtlas = tocopy.wallBottomTexAtlas;
                    existing.renderSolid = false;
                    existing.blockMotion = false;
                }
                else if(extrudeFromSurface == EditorApplication.TileSurface.UpperWall) {
                    if(existing.renderSolid) {
                        existing.floorHeight = pickedSurfaceFloorPoints.min();
                        existing.floorTex = tocopy.floorTex;
                        existing.floorTexAtlas = tocopy.floorTexAtlas;
                    }

                    existing.ceilHeight = pickedSurfaceCeilingPoints.max();
                    existing.ceilSlopeNE = existing.ceilSlopeNW = existing.ceilSlopeSE = existing.ceilSlopeSW = 0;
                    existing.ceilTex = tocopy.ceilTex;
                    existing.ceilTexAtlas = tocopy.ceilTexAtlas;
                    existing.wallTex = tocopy.wallTex;
                    existing.wallTexAtlas = tocopy.wallTexAtlas;
                    existing.renderSolid = false;
                    existing.blockMotion = false;
                }
            }
        }

        // Now mark everything as dirty
        // FIXME: Just do this once, not per tile!
        for (TileSelectionInfo info : selection) {
            Editor.app.markWorldAsDirty(info.x, info.y, 1);
        }

        if(!Editor.app.paintAdjacent.isChecked()) {
            return;
        }

        // Paint adjacent tiles.
        for (AdjacentTileSelectionInfo info : selection.adjacent) {
            Tile t = info.tile;
            if (t == null) {
                t = new Tile();
                t.blockMotion = true;
                t.renderSolid = true;
                Editor.app.level.setTile(info.x, info.y, t);
            }

            switch (info.dir) {
                case NORTH:
                    t.northTex = tocopy.wallTex;
                    t.northTexAtlas = tocopy.wallTexAtlas;
                    t.bottomNorthTex = tocopy.wallBottomTex;
                    t.bottomNorthTexAtlas = tocopy.wallBottomTexAtlas;
                    break;

                case SOUTH:
                    t.southTex = tocopy.wallTex;
                    t.southTexAtlas = tocopy.wallTexAtlas;
                    t.bottomSouthTex = tocopy.wallBottomTex;
                    t.bottomSouthTexAtlas = tocopy.wallBottomTexAtlas;
                    break;

                case EAST:
                    t.eastTex = tocopy.wallTex;
                    t.eastTexAtlas = tocopy.wallTexAtlas;
                    t.bottomEastTex = tocopy.wallBottomTex;
                    t.bottomEastTexAtlas = tocopy.wallBottomTexAtlas;
                    break;

                case WEST:
                    t.westTex = tocopy.wallTex;
                    t.westTexAtlas = tocopy.wallTexAtlas;
                    t.bottomWestTex = tocopy.wallBottomTex;
                    t.bottomWestTexAtlas = tocopy.wallBottomTexAtlas;
                    break;
            }
        }
    }

    SurfacePickerDecal surfacePickerDecal = null;
    public void renderSurfaceControlPoint(TileSelection selection, boolean isCeiling) {
        // Make the surface picker when needed
        if(surfacePickerDecal == null) {
            surfacePickerDecal = SurfacePickerDecal.newDecal(1f, 1f, Editor.app.editorSprites[17]);
        }

        surfacePickerDecal.setBlending(1, 1);
        surfacePickerDecal.setScale(1f, 1f);
        surfacePickerDecal.setTextureRegion(Editor.app.renderer.flashRegion);

        if(isCeiling)
            surfacePickerDecal.setColor(0f, 1f, 0f, 0.25f);
        else
            surfacePickerDecal.setColor(0f, 0f, 1f, 0.4f);

        // Pick a vertical height
        SurfacePickerDecal d = surfacePickerDecal;
        float verticalHeight = selection.getBounds(false).min.z + 0.0001f;
        if(isCeiling)
            verticalHeight = selection.getBounds(false).max.z - 0.0001f;

        d.setPosition(selection.x + 0.25f, verticalHeight, selection.y + 0.25f);
        d.setRotation(Vector3.Y, Vector3.Y);

        float drawWidth = (selection.width * 2) - 1f;
        float drawHeight = (selection.height * 2) - 1f;

        d.setTopLeftOffset(0, 0, 0);
        d.setTopRightOffset(drawWidth, 0, 0);
        d.setBottomLeftOffset(0, -drawHeight, 0);
        d.setBottomRightOffset(drawWidth, -drawHeight, 0);

        Editor.app.spriteBatch.add(d);
        Editor.app.spriteBatch.flush();
    }

    boolean didStartDrag = false;
    Plane dragPlane = new Plane();
    Vector3 dragStart = new Vector3();
    Vector3 dragPlaneIntersectPos = new Vector3();
    Vector3 t_dragOffset = new Vector3();
    Vector3 t_dragVector = new Vector3();
    public void dragControlPoint(ControlPoint pickedControlPoint) {
        if(pickedControlPoint == null)
            return;

        // Get a vertical drag plane
        Vector3 vertDir = t_dragVector.set(Editor.app.camera.direction);
        vertDir.y = 0;
        vertDir.nor();

        dragPlane.set(vertDir.x, vertDir.y, vertDir.z, 0);
        float len = dragPlane.distance(pickedControlPoint.point);
        dragPlane.set(vertDir.x, vertDir.y, vertDir.z, -len);

        boolean didIntersect = Intersector.intersectRayPlane(Editor.app.camera.getPickRay(Gdx.input.getX(), Gdx.input.getY()), dragPlane, dragPlaneIntersectPos);
        if(!didIntersect)
            return;

        // Keep track of the initial drag location
        if(!didStartDrag) {
            didStartDrag = true;
            dragStart.set(dragPlaneIntersectPos);
        }

        // Clamp the dragging to a sub grid. Controls how much to divide a whole tile
        int clampHeightModifier = 16;
        if(Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT))
            clampHeightModifier = 8;
        if(Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT))
            clampHeightModifier = 2;
        if(Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) && Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT))
            clampHeightModifier = 4;

        // Round the intersect position a bit
        dragPlaneIntersectPos.y = (int)(dragPlaneIntersectPos.y * clampHeightModifier) / (float)clampHeightModifier;
        Vector3 dragOffset = t_dragOffset.set(dragStart.x - dragPlaneIntersectPos.x,dragStart.y - dragPlaneIntersectPos.y,dragStart.z - dragPlaneIntersectPos.z);

        for(TileSelection selection : pickedTileSelections) {
            if (pickedControlPoint.controlPointType == ControlPoint.ControlPointType.floor) {
                selection.getBounds(false).min.z -= dragOffset.y;
            } else if (pickedControlPoint.controlPointType == ControlPoint.ControlPointType.ceiling) {
                selection.getBounds(false).max.z -= dragOffset.y;
            }

            adjustTileHeights(selection, dragStart, dragOffset, pickedControlPoint.controlPointType);
        }

        // FIXME: Just do this once for the whole box, not per tile!
        for(TileSelection selection : pickedTileSelections) {
            for(TileSelectionInfo info : selection) {
                Editor.app.markWorldAsDirty(info.x, info.y, 1);
            }
        }

        // Now move the control point for next time
        pickedControlPoint.point.y -= dragOffset.y;
        dragStart.set(dragPlaneIntersectPos);
    }

    // Override this for different behaviors when adjusting the tile ceiling heights
    public void adjustTileHeights(TileSelection selection, Vector3 dragStart, Vector3 dragOffset, ControlPoint.ControlPointType controlPointType) {
        boolean isCeiling = controlPointType == ControlPoint.ControlPointType.ceiling;
        for (TileSelectionInfo info : selection) {
            Tile t = info.tile;
            if (t == null) {
                continue;
            }

            if (isCeiling) {
                t.ceilHeight -= dragOffset.y;
            } else {
                t.floorHeight -= dragOffset.y;
            }

            t.packHeights();
            if (t.getMinOpenHeight() < 0f) {
                t.compressFloorAndCeiling(!isCeiling);
            }
        }
    }

    @Override
    public void onSwitchTo(EditorMode newMode) {
        if(!(newMode instanceof CarveMode))
            return;

        // Copy our picked tile selections into the new mode
        CarveMode newCarveMode = (CarveMode) newMode;
        newCarveMode.pickedTileSelections.addAll(pickedTileSelections);
        newCarveMode.state = CarveModeState.SELECTED_TILES;
    }
}
