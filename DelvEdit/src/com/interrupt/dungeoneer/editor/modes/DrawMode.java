package com.interrupt.dungeoneer.editor.modes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import com.interrupt.dungeoneer.collision.Collidor;
import com.interrupt.dungeoneer.editor.Editor;
import com.interrupt.dungeoneer.editor.selection.AdjacentTileSelectionInfo;
import com.interrupt.dungeoneer.editor.selection.TileSelection;
import com.interrupt.dungeoneer.editor.selection.TileSelectionInfo;
import com.interrupt.dungeoneer.game.Level;
import com.interrupt.dungeoneer.gfx.GlRenderer;
import com.interrupt.dungeoneer.tiles.Tile;

public class DrawMode extends EditorMode {
    public DrawMode() {
        super(EditorModes.DRAW);
    }

    public DrawMode(EditorModes mode) {
        super(mode);
    }

    boolean lockTiles = false;

    TileSelection tileSelection = new TileSelection();

    // World intersection
    Plane intersectPlane = new Plane();
    Vector3 intersectPoint = new Vector3();
    Vector3 intersectNormal = new Vector3();

    // Ignore the first click if it is partial
    boolean canDoAction = false;
    boolean wasPainting = false;

    @Override
    public void start() {
        canDoAction = false;
        wasPainting = false;
    }

    @Override
    public void onSwitchTo(EditorMode newMode) {
        canDoAction = false;
        wasPainting = false;
    }

    @Override
    public void tick() {
        Ray ray = Editor.app.camera.getPickRay(Gdx.input.getX(), Gdx.input.getY());
        intersectPlane.set(0, 1, 0, 0.5f);
        intersectNormal.set(0, 1, 0);

        // Use the last click intersection height for the click plane after the action has starteds
        if(!lockTiles && canDoAction && Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
            intersectPlane.set(0, 1, 0, -intersectPoint.y);
            intersectNormal.set(0, 1, 0);
        }

        // Start with a simple plane intersection
        Intersector.intersectRayPlane(ray, intersectPlane, intersectPoint);

        // Can also try to get a world intersection, before the click
        if(!canDoAction || lockTiles || !Gdx.input.isButtonPressed(Input.Buttons.LEFT))
            Collidor.intersectRayForwardFacingTriangles(ray, Editor.app.camera, GlRenderer.triangleSpatialHash.getAllTriangles(), intersectPoint, intersectNormal);

        // Select the tile under the pointer
        tileSelection.width = 1;
        tileSelection.height = 1;
        tileSelection.x = (int) (intersectPoint.x + intersectNormal.x * 0.5f);
        tileSelection.y = (int) (intersectPoint.z + intersectNormal.z * 0.5f);
        tileSelection.startX = tileSelection.x;
        tileSelection.startY = tileSelection.y;

        // Only update the height the first click
        if(Gdx.input.isButtonPressed(Input.Buttons.LEFT) && !lockTiles) {
            BoundingBox previousBounds = tileSelection.getBounds(false);
            float previousMinZ = previousBounds.min.z;
            float previousMaxZ = previousBounds.max.z;

            BoundingBox newBounds = tileSelection.getBounds(); // Update bounding box
            newBounds.min.z = previousMinZ;
            newBounds.max.z = previousMaxZ;
        } else {
            tileSelection.getBounds(); // Update bounding box
        }

        // Make sure we only start erasing after the first full click
        if(!Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
            canDoAction = true;
        }

        if(!canDoAction)
            return;

        // We're painting!
        if(Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
            // Mark that we're painting now, so that we can save the state later
            wasPainting = true;
            applyTiles();
            return;
        }

        // Done painting, save the state if needed
        if(wasPainting) {
            wasPainting = false;
            Editor.app.history.save();
        }
    }

    protected Tile getNewTile() {
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

        BoundingBox tileBounds = tileSelection.getBounds(false);
        t.floorHeight = tileBounds.min.z;
        t.ceilHeight = tileBounds.max.z;

        return t;
    }

    protected void applyTiles() {
        Level level = Editor.app.level;
        Tile tocopy = getNewTile();

        // Set a new tile wherever the mouse is
        for (TileSelectionInfo info : tileSelection) {
            Tile existing = level.getTileOrNull(info.x, info.y);

            // Easy case, solid tiles
            if(existing == null || existing.blockMotion) {
                if(!lockTiles)
                    level.setTile(info.x, info.y, getNewTile());
                continue;
            }

            // Harder case, existing non solid tile
            if(lockTiles)
                Tile.copyTextures(tocopy, existing);
            else
                Tile.copy(tocopy, existing);
        }

        // Also paint adjacent tiles when asked
        for (AdjacentTileSelectionInfo info : tileSelection.adjacent) {
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

        // Refresh world
        Editor.app.markWorldAsDirty(tileSelection.x, tileSelection.y, tileSelection.width, tileSelection.height);
    }

    @Override
    public void draw() {
        // Draw selection
        Editor.app.boxRenderer.setColor(0.75f, 0.75f, 0.75f, 0.5f);
        Editor.app.boxRenderer.begin(ShapeRenderer.ShapeType.Line);

        BoundingBox bounds = tileSelection.getBounds(false);

        Editor.app.boxRenderer.box(
            bounds.min.x,
            bounds.min.z,
            bounds.min.y,
            bounds.getWidth(),
            bounds.getDepth(),
            -bounds.getHeight()
        );
        Editor.app.boxRenderer.end();
    }
}
