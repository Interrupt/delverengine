package com.interrupt.dungeoneer.editor.handles;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.interrupt.dungeoneer.editor.EditorColors;
import com.interrupt.dungeoneer.gfx.Meshes;

public class PositionHandle extends CompositeHandle {
    private static final Mesh mesh;

    static {
        // Construct an offset quad mesh
        Matrix4 matrix = new Matrix4()
            .translate(-0.5f, 0, 0.5f)
            .scale(0.25f, 0.25f, 0.25f);

        mesh = Meshes.quad();
        mesh.transform(matrix);
    }

    public PositionHandle(Vector3 position) {
        super(position);

        Quaternion axisRotation = new Quaternion();
        PositionHandle self = this;

        Handle xAxisHandle = new ArrowHandle(Vector3.Zero, new Vector3(1, 0, 0)) {
            @Override
            public void change() {
                Vector3 delta = transform.getPosition();
                self.setPosition(delta);
                transform.setLocalPosition(Vector3.Zero);

                self.change();
            }
        };
        xAxisHandle.setColor(EditorColors.X_AXIS);
        xAxisHandle.setHighlightColor(EditorColors.X_AXIS_BRIGHT);

        Handle yAxisHandle = new ArrowHandle(Vector3.Zero, new Vector3(0, 0, 1)) {
            @Override
            public void change() {
                Vector3 delta = transform.getPosition();
                self.setPosition(delta);
                transform.setLocalPosition(Vector3.Zero);

                self.change();
            }
        };
        yAxisHandle.setColor(EditorColors.Y_AXIS);
        yAxisHandle.setHighlightColor(EditorColors.Y_AXIS_BRIGHT);

        Handle zAxisHandle = new ArrowHandle(Vector3.Zero, new Vector3(0, 1, 0)) {
            @Override
            public void change() {
                Vector3 delta = transform.getPosition();
                self.setPosition(delta);
                transform.setLocalPosition(Vector3.Zero);

                self.change();
            }
        };
        zAxisHandle.setColor(EditorColors.Z_AXIS);
        zAxisHandle.setHighlightColor(EditorColors.Z_AXIS_BRIGHT);

        Handle xPlaneHandle = new PlaneAlignedHandle(mesh, Vector3.Zero, axisRotation.setEulerAngles(0, 0, -90)) {
            @Override
            public void change() {
                Vector3 delta = transform.getPosition();
                self.setPosition(delta);
                transform.setLocalPosition(Vector3.Zero);

                self.change();
            }
        };
        xPlaneHandle.setColor(EditorColors.X_AXIS);
        xPlaneHandle.setHighlightColor(EditorColors.X_AXIS_BRIGHT);

        Handle yPlaneHandle = new PlaneAlignedHandle(mesh, Vector3.Zero, axisRotation.setEulerAngles(180, -90, 0)) {
            @Override
            public void change() {
                Vector3 delta = transform.getPosition();
                self.setPosition(delta);
                transform.setLocalPosition(Vector3.Zero);

                self.change();
            }
        };
        yPlaneHandle.setColor(EditorColors.Y_AXIS);
        yPlaneHandle.setHighlightColor(EditorColors.Y_AXIS_BRIGHT);

        Handle zPlaneHandle = new PlaneAlignedHandle(mesh, Vector3.Zero, axisRotation.setEulerAngles(90, 0, 0)) {
            @Override
            public void change() {
                Vector3 delta = transform.getPosition();
                self.setPosition(delta);
                transform.setLocalPosition(Vector3.Zero);

                self.change();
            }
        };
        zPlaneHandle.setColor(EditorColors.Z_AXIS);
        zPlaneHandle.setHighlightColor(EditorColors.Z_AXIS_BRIGHT);

        add(xAxisHandle);
        add(yAxisHandle);
        add(zAxisHandle);
        add(xPlaneHandle);
        add(yPlaneHandle);
        add(zPlaneHandle);
    }

    @Override
    public void setPosition(Vector3 position) {
        super.setPosition(position);
    }

    @Override
    public void draw() {
        /*// Maintain constant screen size
        Camera camera = Editor.app.camera;
        Vector3 position = getPosition();
        float distance = Vector3.dst(
            camera.position.x,
            camera.position.z,
            camera.position.y,
            position.x,
            position.y,
            position.z
        );
        float scale = distance * 0.1f;
        setScale(scale);*/
        super.draw();
    }
}
