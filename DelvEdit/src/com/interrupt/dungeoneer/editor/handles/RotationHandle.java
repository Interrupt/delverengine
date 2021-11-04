package com.interrupt.dungeoneer.editor.handles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.interrupt.dungeoneer.editor.Editor;
import com.interrupt.dungeoneer.editor.EditorColors;
import com.interrupt.dungeoneer.gfx.Meshes;

public class RotationHandle extends CompositeHandle {
    private static final Mesh mesh;

    static {
        mesh = Meshes.ring(1 - (1 / 8f), 1, 3);
    }

    private class AxialRotationHandle extends PlaneAlignedHandle {
        private final Vector3 axis = new Vector3();

        public AxialRotationHandle(Mesh mesh, Vector3 position, Quaternion rotation) {
            super(mesh, position, rotation);
        }

        @Override
        public void select() {
            // Calculate vector along axis
            axis.set(Vector3.Y);
            getRotation().transform(axis);

            // Capture offset of initial selection
            Camera camera = Editor.app.camera;
            Vector3 position = transform.getPosition();
            plane.set(
                position.x,
                position.y,
                position.z,
                axis.x,
                axis.y,
                axis.z
            );
            Intersector.intersectRayPlane(
                camera.getPickRay(
                    Gdx.input.getX(),
                    Gdx.input.getY()
                ),
                plane,
                intersection
            );

            cursorDragOffset.set(intersection).sub(position);
        }

        private final Quaternion dragRotation = new Quaternion();
        @Override
        public boolean touchDragged(int screenX, int screenY, int pointer) {
            if (!getSelected()) return false;

            wasDragged = true;

            // Calculate vector along axis
            axis.set(Vector3.Y);
            Quaternion rotation = getRotation();
            rotation.transform(axis);

            Vector3 position = getPosition();
            Camera camera = Editor.app.camera;
            plane.set(
                position.x,
                position.y,
                position.z,
                axis.x,
                axis.y,
                axis.z
            );
            Intersector.intersectRayPlane(
                camera.getPickRay(
                    screenX,
                    screenY
                ),
                plane,
                intersection
            );

            Vector3 o = new Vector3(cursorDragOffset).nor();
            cursorDragOffset.set(intersection).sub(position);
            Vector3 p = new Vector3(intersection).sub(position).nor();
            dragRotation.setFromCross(o, p);
            dragRotation.mul(rotation);

            setRotation(dragRotation);
            change();

            return false;
        }
    }

    public RotationHandle(Vector3 position) {
        super(position);

        Handle self = this;
        Quaternion q = new Quaternion().setEulerAngles(90, 0, 0);
        Handle zPlaneHandle = new AxialRotationHandle(mesh, Vector3.Zero, q) {
            @Override
            public void change() {
                self.change();
            }
        };
        zPlaneHandle.setColor(EditorColors.Z_AXIS);
        zPlaneHandle.setHighlightColor(EditorColors.Z_AXIS_BRIGHT);

        Handle xPlaneHandle = new AxialRotationHandle(mesh, Vector3.Zero, q.setEulerAngles(0, 0, -90)) {
            @Override
            public void change() {
                Quaternion rotation = getRotation();
                self.setRotation(rotation);
                this.transform.setLocalRotation(q.setEulerAngles(0, 0, -90));
                self.change();
            }
        };
        xPlaneHandle.setColor(EditorColors.X_AXIS);
        xPlaneHandle.setHighlightColor(EditorColors.X_AXIS_BRIGHT);

        //add(xPlaneHandle);
        add(zPlaneHandle);
    }
}
