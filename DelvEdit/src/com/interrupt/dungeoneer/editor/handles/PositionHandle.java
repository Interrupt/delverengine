package com.interrupt.dungeoneer.editor.handles;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.interrupt.dungeoneer.editor.EditorColors;
import com.interrupt.dungeoneer.editor.gfx.Draw;

public class PositionHandle extends CompositeHandle {
    public PositionHandle(Vector3 position) {
        super(position);

        Quaternion axisRotation = new Quaternion();
        PositionHandle self = this;

        Handle xAxisHandle = new ArrowHandle(position, axisRotation.setEulerAngles(0, 0, -90)) {
            @Override
            public void change() {
                self.setPosition(getPosition());
                self.change();
            }
        };
        xAxisHandle.setColor(EditorColors.X_AXIS);
        xAxisHandle.setHighlightColor(EditorColors.X_AXIS_BRIGHT);

        Handle yAxisHandle = new ArrowHandle(position, axisRotation.setEulerAngles(0, 90, 0)) {
            @Override
            public void change() {
                self.setPosition(getPosition());
                self.change();
            }
        };
        yAxisHandle.setColor(EditorColors.Y_AXIS);
        yAxisHandle.setHighlightColor(EditorColors.Y_AXIS_BRIGHT);

        Handle zAxisHandle = new ArrowHandle(position, axisRotation.setEulerAngles(90, 0, 0)) {
            @Override
            public void change() {
                self.setPosition(getPosition());
                self.change();
            }
        };
        zAxisHandle.setColor(EditorColors.Z_AXIS);
        zAxisHandle.setHighlightColor(EditorColors.Z_AXIS_BRIGHT);

        Handle zPlaneHandle = new Handle(position) {
            private final Vector3 scale = new Vector3();

            @Override
            public void draw() {
                super.draw();

                Draw.color(getDrawColor());
                Draw.quad(getPosition(), getRotation(), scale.set(0.25f, 0.25f, 0.25f));
                Draw.color(Color.WHITE);
            }

            @Override
            public void change() {
                self.setPosition(getPosition());
                self.change();
            }
        };
        zPlaneHandle.setColor(EditorColors.Z_AXIS);
        zPlaneHandle.setHighlightColor(EditorColors.Z_AXIS_BRIGHT);

        children.addAll(
            xAxisHandle,
            yAxisHandle,
            zAxisHandle,
            zPlaneHandle
        );
    }
}
