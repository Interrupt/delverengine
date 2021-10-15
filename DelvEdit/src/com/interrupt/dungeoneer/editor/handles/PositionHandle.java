package com.interrupt.dungeoneer.editor.handles;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.interrupt.dungeoneer.editor.Editor;
import com.interrupt.dungeoneer.editor.EditorColors;
import com.interrupt.dungeoneer.editor.gfx.Draw;

public class PositionHandle extends CompositeHandle {
    public PositionHandle(Vector3 position) {
        super(position);

        Quaternion axisRotation = new Quaternion();
        final Vector3 temp = new Vector3();

        PositionHandle self = this;

        Handle xAxisHandle = new ArrowHandle(Vector3.Zero, axisRotation.setEulerAngles(0, 0, -90)) {
            @Override
            public void change() {
                Vector3 delta = transform.getLocalPosition();
                self.setPosition(self.getPosition().add(delta));
                transform.setLocalPosition(Vector3.Zero);

                self.change();
            }
        };
        xAxisHandle.setColor(EditorColors.X_AXIS);
        xAxisHandle.setHighlightColor(EditorColors.X_AXIS_BRIGHT);

        Handle yAxisHandle = new ArrowHandle(Vector3.Zero, axisRotation.setEulerAngles(0, 90, 0)) {
            @Override
            public void change() {
                Vector3 delta = transform.getLocalPosition();
                self.setPosition(self.getPosition().add(delta));
                transform.setLocalPosition(Vector3.Zero);

                self.change();
            }
        };
        yAxisHandle.setColor(EditorColors.Y_AXIS);
        yAxisHandle.setHighlightColor(EditorColors.Y_AXIS_BRIGHT);

        Handle zAxisHandle = new ArrowHandle(Vector3.Zero, axisRotation.setEulerAngles(90, 0, 0)) {
            @Override
            public void change() {
                Vector3 delta = transform.getLocalPosition();
                self.setPosition(self.getPosition().add(delta));
                transform.setLocalPosition(Vector3.Zero);

                self.change();
            }
        };
        zAxisHandle.setColor(EditorColors.Z_AXIS);
        zAxisHandle.setHighlightColor(EditorColors.Z_AXIS_BRIGHT);

        Handle zPlaneHandle = new Handle(position) {
            @Override
            public void draw() {
                super.draw();

                Draw.color(getDrawColor());
                Draw.quad(getPosition(), getRotation(), temp.set(0.25f, 0.25f, 0.25f));
                Draw.color(Color.WHITE);
            }

            @Override
            public void change() {

            }
        };
        zPlaneHandle.setColor(EditorColors.Z_AXIS);
        zPlaneHandle.setHighlightColor(EditorColors.Z_AXIS_BRIGHT);

        add(xAxisHandle);
        add(yAxisHandle);
        add(zAxisHandle);
    }

    @Override
    public void setPosition(Vector3 position) {
        super.setPosition(position);
    }

    @Override
    public void draw() {
        // Maintain constant screen size
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
        setScale(scale);
        super.draw();
    }
}
