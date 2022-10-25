package com.interrupt.dungeoneer.editor.modes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.interrupt.dungeoneer.editor.ControlPoint;
import com.interrupt.dungeoneer.editor.ControlPointVertex;
import com.interrupt.dungeoneer.editor.Editor;
import com.interrupt.dungeoneer.editor.selection.TileSelection;
import com.interrupt.dungeoneer.editor.selection.TileSelectionInfo;
import com.interrupt.dungeoneer.gfx.GlRenderer;
import com.interrupt.dungeoneer.gfx.SpriteGroupStrategy;
import com.interrupt.dungeoneer.tiles.Tile;

public class VertexMode extends CarveMode {
    Array<ControlPoint> controlPoints = new Array<>();
    boolean madeControlPoints = false;
    boolean movingControlPoint = false;

    protected Vector3 controlPointIntersection = new Vector3();
    protected DecalBatch pointBatch;

    protected ControlPoint hoveredControlPoint = null;
    protected Array<ControlPoint> pickedControlPointVertices = new Array<>();
    protected static Color controlPointColor = new Color(1f, 0.4f, 0f, 1f);

    public VertexMode() {
        super(EditorModes.VERTEX);
        canCarve = false;
        canExtrude = false;
        usePlanePicking = false;
        useCollisionTrianglePicking = false;
        tileSelectionSettings.boundsUseTileHeights = true;
    }

    @Override
    public void start() {
        if(pointBatch != null) pointBatch.dispose();
        pointBatch = new DecalBatch(new SpriteGroupStrategy(Editor.app.camera, null, GlRenderer.worldShaderInfo, 1));

        pickedControlPointVertices.clear();
        controlPoints.clear();
    }

    @Override
    public void onNewTileSelectionPicked() {
        refreshControlPoints();
    }

    @Override
    public void tick() {
        super.tick();

        if(controlPoints.size == 0)
            return;

        hoveredControlPoint = null;
        Ray ray = Editor.app.camera.getPickRay(Gdx.input.getX(), Gdx.input.getY());
        for(ControlPoint point : controlPoints) {
            if(!Editor.app.ui.isShowingContextMenu()) {
                if (Intersector.intersectRaySphere(ray, point.point, 0.12f, controlPointIntersection)) {
                    hoveredControlPoint = point;
                }
            }
        }
    }

    @Override
    public void onSwitchFrom(EditorMode oldMode) {
        refreshControlPoints();
    }

    @Override
    protected void tryPickingControlPoint(TileSelection selection) {
        pickedControlPoint = hoveredControlPoint;
        if(pickedControlPoint == null)
            return;

        // Switch to the moving control point state
        didStartDrag = false;
        didPickSurface = false;
        state = CarveModeState.SELECTED_CONTROL_POINT;

        // Might not be anything to do
        if(pickedControlPointVertices.contains(pickedControlPoint, true))
            return;

        // Keep adding more vertices to the set if Shift is pressed
        if(!Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT))
            pickedControlPointVertices.clear();
        pickedControlPointVertices.add(pickedControlPoint);
    }

    @Override
    protected void movePickedControlPoint(Vector3 dragOffset) {
        for(ControlPoint controlPoint : pickedControlPointVertices) {
            controlPoint.point.y -= dragOffset.y;
        }
    }

    public void adjustTileHeights(TileSelection selection, Vector3 dragStart, Vector3 dragOffset, ControlPoint.ControlPointType controlPointType) {

        if(pickedControlPointVertices.size == 0)
            return;

        for(ControlPoint controlPoint : pickedControlPointVertices) {
            for (ControlPointVertex v : controlPoint.vertices) {
                Tile t = v.tile;

                if (v.vertex == ControlPointVertex.ControlVertex.ceilNE) {
                    t.ceilSlopeNE = controlPoint.point.y - t.ceilHeight;
                } else if (v.vertex == ControlPointVertex.ControlVertex.ceilSE) {
                    t.ceilSlopeSE = controlPoint.point.y - t.ceilHeight;
                } else if (v.vertex == ControlPointVertex.ControlVertex.ceilNW) {
                    t.ceilSlopeNW = controlPoint.point.y - t.ceilHeight;
                } else if (v.vertex == ControlPointVertex.ControlVertex.ceilSW) {
                    t.ceilSlopeSW = controlPoint.point.y - t.ceilHeight;
                } else if (v.vertex == ControlPointVertex.ControlVertex.slopeNE) {
                    t.slopeNE = controlPoint.point.y - t.floorHeight;
                } else if (v.vertex == ControlPointVertex.ControlVertex.slopeSE) {
                    t.slopeSE = controlPoint.point.y - t.floorHeight;
                } else if (v.vertex == ControlPointVertex.ControlVertex.slopeNW) {
                    t.slopeNW = controlPoint.point.y - t.floorHeight;
                } else if (v.vertex == ControlPointVertex.ControlVertex.slopeSW) {
                    t.slopeSW = controlPoint.point.y - t.floorHeight;
                }
                t.packHeights();
            }
        }
    }

    @Override
    public void draw() {
        super.draw();

        // Only draw control points when there is a selected tile set
        if(state.ordinal() < CarveModeState.SELECTED_TILES.ordinal())
            return;

        for(ControlPoint point : controlPoints) {
            boolean isPicked = pickedControlPointVertices.contains(point, true);
            if(movingControlPoint && !isPicked)
                continue;

            drawPoint(point.point, isPicked || hoveredControlPoint == point ? Color.WHITE : controlPointColor);
        }

        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
        pointBatch.flush();
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
    }

    @Override
    public void reset() {
        super.reset();
    }

    void refreshControlPoints() {
        controlPoints.clear();
        pickedControlPointVertices.clear();

        for(TileSelection selection : pickedTileSelections) {
            for (TileSelectionInfo info : selection) {
                Tile current = info.tile;

                if (current != null && !current.renderSolid) {
                    if (current.tileSpaceType != Tile.TileSpaceType.OPEN_SE) {
                        controlPoints.add(new ControlPoint(new Vector3(info.x, current.ceilHeight + current.ceilSlopeNE, info.y), new ControlPointVertex(current, ControlPointVertex.ControlVertex.ceilNE)));
                        controlPoints.add(new ControlPoint(new Vector3(info.x, current.floorHeight + current.slopeNE, info.y), new ControlPointVertex(current, ControlPointVertex.ControlVertex.slopeNE)));
                    }

                    if (current.tileSpaceType != Tile.TileSpaceType.OPEN_SW) {
                        controlPoints.add(new ControlPoint(new Vector3(info.x + 1, current.ceilHeight + current.ceilSlopeNW, info.y), new ControlPointVertex(current, ControlPointVertex.ControlVertex.ceilNW)));
                        controlPoints.add(new ControlPoint(new Vector3(info.x + 1, current.floorHeight + current.slopeNW, info.y), new ControlPointVertex(current, ControlPointVertex.ControlVertex.slopeNW)));
                    }

                    if (current.tileSpaceType != Tile.TileSpaceType.OPEN_NE) {
                        controlPoints.add(new ControlPoint(new Vector3(info.x, current.ceilHeight + current.ceilSlopeSE, info.y + 1), new ControlPointVertex(current, ControlPointVertex.ControlVertex.ceilSE)));
                        controlPoints.add(new ControlPoint(new Vector3(info.x, current.floorHeight + current.slopeSE, info.y + 1), new ControlPointVertex(current, ControlPointVertex.ControlVertex.slopeSE)));
                    }

                    if (current.tileSpaceType != Tile.TileSpaceType.OPEN_NW) {
                        controlPoints.add(new ControlPoint(new Vector3(info.x + 1, current.ceilHeight + current.ceilSlopeSW, info.y + 1), new ControlPointVertex(current, ControlPointVertex.ControlVertex.ceilSW)));
                        controlPoints.add(new ControlPoint(new Vector3(info.x + 1, current.floorHeight + current.slopeSW, info.y + 1), new ControlPointVertex(current, ControlPointVertex.ControlVertex.slopeSW)));
                    }
                }
            }
        }

        // filter out duplicate vertices
        ArrayMap<String, ControlPoint> reduceMap = new ArrayMap<>();
        for(ControlPoint point : controlPoints) {
            String key = point.point.x + "," + point.point.y + "," + point.point.z;
            ControlPoint found = reduceMap.get(key);

            if(found != null) found.vertices.addAll(point.vertices);
            else reduceMap.put(key, point);
        }

        controlPoints.clear();
        for(ControlPoint point : reduceMap.values()) {
            controlPoints.add(point);
        }

        madeControlPoints = true;
    }


    Vector3 t_Pos1 = new Vector3();
    Vector3 t_Pos2 = new Vector3();
    Vector3 t_Pos3 = new Vector3();
    private void drawPoint(Vector3 point, Color color) {
        PerspectiveCamera camera = Editor.app.camera;

        Vector3 pos1 = t_Pos1.set(point);
        Vector3 pos2 = t_Pos2.set(camera.position);

        pos2.sub(pos1);

        Decal sd = Editor.app.getDecal();
        sd.setRotation(t_Pos3.set(camera.direction.x, camera.direction.y, camera.direction.z).nor().scl(-1f), Vector3.Y);
        sd.setScale((pos2.len() / camera.far) * (pos2.len() * 0.5f) + 1);
        sd.setTextureRegion(Editor.app.editorSprites[17]);
        sd.setPosition(point.x, point.y, point.z);
        sd.setColor(color.r, color.g, color.b, color.a);

        pointBatch.add(sd);
    }
}
