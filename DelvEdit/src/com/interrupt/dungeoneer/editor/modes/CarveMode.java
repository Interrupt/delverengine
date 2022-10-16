package com.interrupt.dungeoneer.editor.modes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import com.interrupt.dungeoneer.collision.Collidor;
import com.interrupt.dungeoneer.editor.ControlPoint;
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
    public CarveMode(EditorApplication inEditor) {
        super(inEditor);
    }

    Vector3 selectionStart = new Vector3();

    Plane intersectPlane = new Plane();
    Vector3 intersectPoint = new Vector3();
    Vector3 intersectNormal = new Vector3();
    Vector3 intersectNormalPicked = new Vector3();

    Vector3 temp1 = new Vector3();

    TileSelection tileSelection = new TileSelection();

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
        Ray ray = editor.camera.getPickRay(Gdx.input.getX(), Gdx.input.getY());
        intersectPlane.set(0, 1, 0, -0.5f);

        // Start with a simple plane intersection
        Intersector.intersectRayPlane(ray, intersectPlane, intersectPoint);

        // But try to get a world intersection
        if (Collidor.intersectRayForwardFacingTriangles(ray, editor.camera, GlRenderer.triangleSpatialHash.getAllTriangles(), intersectPoint, intersectNormal)) {
            temp1.set(intersectPoint).sub(editor.camera.position).nor();
        }

        // Tile selection indicator
        tileSelection.width = 1;
        tileSelection.height = 1;
        tileSelection.x = (int)(intersectPoint.x - intersectNormal.x * 0.5f);
        tileSelection.y = (int)(intersectPoint.z - intersectNormal.z * 0.5f);
        tileSelection.startX = tileSelection.x;
        tileSelection.startY = tileSelection.y;

        // The actual copy we want to copy is the one on the other side of a wall collision, keep track of that
        tileToCopyPos.set(intersectPoint.x + intersectNormal.x * 0.1f, intersectPoint.z + intersectNormal.z * 0.1f);

        // Also keep track of which surface was being extruded, if any
        if(editor.pickedSurface != null && editor.pickedSurface.isPicked) {
            didPickSurface = editor.pickedSurface.tileSurface == EditorApplication.TileSurface.UpperWall ||
                editor.pickedSurface.tileSurface == EditorApplication.TileSurface.LowerWall;

            pickedSurfaceFloorPoints.set(editor.pickedSurface.floorPoints);
            pickedSurfaceCeilingPoints.set(editor.pickedSurface.ceilingPoints);
        }

        if(!editor.editorInput.isButtonPressed(Input.Buttons.LEFT))
            return;

        // Switch to the dragging state
        state = CarveModeState.DRAGGING_SELECTION;

        // Keep track of the initial click location
        selectionStart.set(intersectPoint.x, intersectPoint.y, intersectPoint.z);
        intersectNormalPicked.set(intersectNormal);

        // Save the surface being extruded
        if(editor.pickedSurface != null && editor.pickedSurface.isPicked) {
            extrudeFromSurface = editor.pickedSurface.tileSurface;
        } else {
            extrudeFromSurface = EditorApplication.TileSurface.Floor;
        }
    }

    public void tickStateDraggingSelection() {
        Ray ray = editor.camera.getPickRay(Gdx.input.getX(), Gdx.input.getY());
        intersectPlane.set(0, 1, 0, -selectionStart.y);

        // Get the intersection position
        Intersector.intersectRayPlane(ray, intersectPlane, intersectPoint);

        // Find how far we have dragged on the plane since the first selection
        float dragDistanceX = intersectPoint.x - selectionStart.x;
        float dragDistanceY = intersectPoint.z - selectionStart.z;

        tileSelection.fixup(dragDistanceX, dragDistanceY);

        // Quit here if we are still dragging
        if(editor.editorInput.isButtonPressed(Input.Buttons.LEFT))
            return;

        // Switch to the selected state when done dragging
        state = CarveModeState.SELECTED_TILES;

        // Carve automatically, unless shift is being held
        if(editor.editorInput.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
            return;
        }
        doCarve();
    }

    public void tickStateSelectedTiles() {
        // We have selected tiles! Can do a bunch of stuff now
        if(Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            doCarve();
        } else if(Gdx.input.isKeyJustPressed(Input.Keys.BACKSPACE)) {
            doErase();
        } else if(Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            tryPickingControlPoint();

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
        editor.history.saveState(editor.level);
    }

    private boolean getPointerOverCeilingPlane() {
        // Try picking the ceiling
        intersectPlane.set(0, 1, 0, -tileSelection.getBounds().max.z);
        intersectNormal.set(0, 1, 0);

        Ray ray = editor.camera.getPickRay(Gdx.input.getX(), Gdx.input.getY());
        boolean hitPlane = Intersector.intersectRayPlane(ray, intersectPlane, intersectPoint);

        if (!hitPlane)
            return false;

        // Now check if the intersection is in the bounds
        if(intersectPoint.x < tileSelection.x)
            return false;
        if(intersectPoint.x > tileSelection.x + tileSelection.width)
            return false;
        if(intersectPoint.z < tileSelection.y)
            return false;
        if(intersectPoint.z > tileSelection.y + tileSelection.height)
            return false;

        return true;
    }

    private boolean getPointerOverFloorPlane() {
        // Try picking the ceiling
        intersectPlane.set(0, 1, 0, -tileSelection.getBounds().min.z);
        intersectNormal.set(0, 1, 0);

        Ray ray = editor.camera.getPickRay(Gdx.input.getX(), Gdx.input.getY());
        boolean hitPlane = Intersector.intersectRayPlane(ray, intersectPlane, intersectPoint);

        if (!hitPlane)
            return false;

        // Now check if the intersection is in the bounds
        if(intersectPoint.x < tileSelection.x)
            return false;
        if(intersectPoint.x > tileSelection.x + tileSelection.width)
            return false;
        if(intersectPoint.z < tileSelection.y)
            return false;
        if(intersectPoint.z > tileSelection.y + tileSelection.height)
            return false;

        return true;
    }

    private void tryPickingControlPoint() {
        if(getPointerOverCeilingPlane()) {
            pickedControlPoint = new ControlPoint(new Vector3(intersectPoint), ControlPoint.ControlPointType.ceiling);
        } else if(getPointerOverFloorPlane()) {
            pickedControlPoint = new ControlPoint(new Vector3(intersectPoint), ControlPoint.ControlPointType.floor);
        } else {
            return;
        }

        // Switch to the moving control point state
        didStartDrag = false;
        state = CarveModeState.SELECTED_CONTROL_POINT;
    }

    @Override
    public void draw() {
        // Draw selection
        editor.boxRenderer.setColor(0.75f, 0.75f, 0.75f, 0.5f);
        editor.boxRenderer.begin(ShapeRenderer.ShapeType.Line);

        BoundingBox bounds = didPickSurface ? tileSelection.getBounds(pickedSurfaceFloorPoints, pickedSurfaceCeilingPoints) :
            tileSelection.getBounds();

        editor.boxRenderer.box(
            bounds.min.x,
            bounds.min.z,
            bounds.min.y,
            bounds.getWidth(),
            bounds.getDepth(),
            -bounds.getHeight()
        );
        editor.boxRenderer.end();

        // Can quit here unless we are in ceiling or floor move modes
        if(state.ordinal() < CarveModeState.SELECTED_TILES.ordinal())
            return;

        if(getPointerOverCeilingPlane())
            renderSurfaceControlPoint(tileSelection, true);
        else if(getPointerOverFloorPlane())
            renderSurfaceControlPoint(tileSelection, false);
    }

    @Override
    public void reset() {
        state = CarveModeState.START;
        pickedControlPoint = null;
        didStartDrag = false;
    }

    public void doCarve() {
        // Set the default tile to use when carving
        Tile t = new Tile();
        t.wallTex = (byte)editor.pickedWallTexture;
        t.wallTexAtlas = editor.pickedWallTextureAtlas;
        t.floorTex = (byte)editor.pickedFloorTexture;
        t.floorTexAtlas = editor.pickedFloorTextureAtlas;
        t.ceilTex = (byte)editor.pickedCeilingTexture;
        t.ceilTexAtlas = editor.pickedCeilingTextureAtlas;
        t.wallBottomTex = (byte)editor.pickedWallBottomTexture;
        t.wallBottomTexAtlas = editor.pickedWallBottomTextureAtlas;
        t.blockMotion = false;
        t.tileSpaceType = Tile.TileSpaceType.EMPTY;
        t.renderSolid = t.blockMotion;

        Tile selectedTile = editor.level.getTile((int)tileToCopyPos.x, (int)tileToCopyPos.y);
        t.floorHeight = selectedTile.floorHeight;
        t.ceilHeight = selectedTile.ceilHeight;

        // Now go set the tiles
        setTiles(t);

        // Save the history for undo
        editor.history.saveState(editor.level);
    }

    public void doErase() {
        Level level = editor.level;
        for (TileSelectionInfo info : tileSelection) {
            Tile n = level.getTile(info.x, info.y - 1);
            Tile s = level.getTile(info.x, info.y + 1);
            Tile e = level.getTile(info.x - 1, info.y);
            Tile w = level.getTile(info.x + 1, info.y);

            if(n.blockMotion && s.blockMotion && e.blockMotion && w.blockMotion) {
                level.setTile(info.x, info.y, null);
            }
            else {
                Tile t = Tile.NewSolidTile();
                t.wallTex = (byte)editor.pickedWallTexture;
                t.wallTexAtlas = editor.pickedWallTextureAtlas;
                level.setTile(info.x, info.y, t);
            }
        }

        // Now mark everything as dirty
        // FIXME: Just do this once, not per tile!
        for (TileSelectionInfo info : tileSelection) {
            editor.markWorldAsDirty(info.x, info.y, 1);
        }

        // Save the history for undo
        editor.history.saveState(editor.level);
    }

    protected void setTiles(Tile tocopy) {
        // Carve out selected tiles
        for (TileSelectionInfo info : tileSelection) {
            Tile t = info.tile;
            if (t == null) {
                t = new Tile();
            }

            boolean isExtruding = extrudeFromSurface == EditorApplication.TileSurface.LowerWall ||
                extrudeFromSurface == EditorApplication.TileSurface.UpperWall;

            Tile selectedTile = tileSelection.first();

            Tile existing = editor.level.getTileOrNull(info.x, info.y);
            if(!isExtruding) {
                // Simple case, just copy the whole tile
                Tile.copy(tocopy, t);
                editor.level.setTile(info.x, info.y, t);
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

                editor.level.setTile(info.x, info.y, t);
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

            //t.eastTex = t.westTex = t.northTex = t.southTex = null;
            //t.bottomEastTex = t.bottomWestTex = t.bottomNorthTex = t.bottomSouthTex = null;
        }

        // Now mark everything as dirty
        // FIXME: Just do this once, not per tile!
        for (TileSelectionInfo info : tileSelection) {
            editor.markWorldAsDirty(info.x, info.y, 1);
        }

        if(!editor.paintAdjacent.isChecked()) {
            return;
        }

        // Paint adjacent tiles.
        for (AdjacentTileSelectionInfo info : tileSelection.adjacent) {
            Tile t = info.tile;
            if (t == null) {
                t = new Tile();
                t.blockMotion = true;
                t.renderSolid = true;
                editor.level.setTile(info.x, info.y, t);
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
            surfacePickerDecal = SurfacePickerDecal.newDecal(1f, 1f, editor.editorSprites[17]);
        }

        surfacePickerDecal.setBlending(1, 1);
        surfacePickerDecal.setScale(1f, 1f);
        surfacePickerDecal.setTextureRegion(editor.renderer.flashRegion);
        surfacePickerDecal.setColor(1f, 0f, 0f, 0.25f);

        // Pick a vertical height
        SurfacePickerDecal d = surfacePickerDecal;
        float verticalHeight = tileSelection.getBounds().min.z + 0.0001f;
        if(isCeiling)
            verticalHeight = tileSelection.getBounds().max.z - 0.0001f;

        d.setPosition(selection.x + 0.25f, verticalHeight, selection.y + 0.25f);
        d.setRotation(Vector3.Y, Vector3.Y);

        float drawWidth = (selection.width * 2) - 1f;
        float drawHeight = (selection.height * 2) - 1f;

        d.setTopLeftOffset(0, 0, 0);
        d.setTopRightOffset(drawWidth, 0, 0);
        d.setBottomLeftOffset(0, -drawHeight, 0);
        d.setBottomRightOffset(drawWidth, -drawHeight, 0);

        editor.spriteBatch.add(d);
        editor.spriteBatch.flush();
    }

    boolean didStartDrag = false;
    Vector3 dragStart = new Vector3();
    Vector3 dragPlaneIntersectPos = new Vector3();
    public void dragControlPoint(ControlPoint pickedControlPoint) {
        Plane dragPlane = new Plane();
        Vector3 t_dragVector = new Vector3();

        if(pickedControlPoint == null)
            return;

        // Get a vertical drag plane
        Vector3 vertDir = t_dragVector.set(editor.camera.direction);
        vertDir.y = 0;
        vertDir.nor();

        dragPlane.set(vertDir.x, vertDir.y, vertDir.z, 0);
        float len = dragPlane.distance(pickedControlPoint.point);
        dragPlane.set(vertDir.x, vertDir.y, vertDir.z, -len);

        boolean didIntersect = Intersector.intersectRayPlane(editor.camera.getPickRay(Gdx.input.getX(), Gdx.input.getY()), dragPlane, dragPlaneIntersectPos);
        if(!didIntersect)
            return;

        // Keep track of the initial drag location
        if(!didStartDrag) {
            didStartDrag = true;
            dragStart.set(dragPlaneIntersectPos);
        }

        // Round the intersect position a bit
        dragPlaneIntersectPos.y = (int)(dragPlaneIntersectPos.y * 16) / 16f;
        Vector3 dragOffset = new Vector3(dragStart.x - dragPlaneIntersectPos.x,dragStart.y - dragPlaneIntersectPos.y,dragStart.z - dragPlaneIntersectPos.z);

        for (TileSelectionInfo info : tileSelection) {
            Tile t = info.tile;
            if (t == null) {
                continue;
            }

            if (pickedControlPoint.controlPointType == ControlPoint.ControlPointType.floor) {
                t.floorHeight -= dragOffset.y;
                t.packHeights();

                if (t.getMinOpenHeight() < 0f) {
                    t.compressFloorAndCeiling(true);
                }
            } else if (pickedControlPoint.controlPointType == ControlPoint.ControlPointType.ceiling) {
                t.ceilHeight -= dragOffset.y;
                t.packHeights();

                if (t.getMinOpenHeight() < 0f) {
                    t.compressFloorAndCeiling(false);
                }
            }
        }

        // Now move the control point for next time
        pickedControlPoint.point.y -= dragOffset.y;
        dragStart.set(dragPlaneIntersectPos);

        // Now mark everything as dirty
        // FIXME: Just do this once, not per tile!
        for (TileSelectionInfo info : tileSelection) {
            editor.markWorldAsDirty(info.x, info.y, 1);
        }
    }
}
