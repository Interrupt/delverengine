package com.interrupt.dungeoneer.editor;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

public class ControlPoint {
    public Vector3 point;
    public ControlPointType controlPointType;

    public enum ControlPointType { floor, ceiling, northCeil, northFloor, eastCeil, eastFloor, southCeil, southFloor, westCeil, westFloor, vertex };

    public Array<ControlPointVertex> vertices = new Array<>();

    public ControlPoint(Vector3 point, ControlPointType type) {
        this.point = point;
        controlPointType = type;
    }

    public ControlPoint(Vector3 point, ControlPointVertex vertex) {
        this.point = point;
        controlPointType = ControlPointType.vertex;
        vertices.add(vertex);
    }

    public boolean isCeiling() {
        return controlPointType == ControlPointType.northCeil || controlPointType == ControlPointType.eastCeil || controlPointType == ControlPointType.southCeil || controlPointType == ControlPointType.westCeil;
    }

    public boolean isFloor() {
        return controlPointType == ControlPointType.northFloor || controlPointType == ControlPointType.eastFloor || controlPointType == ControlPointType.southFloor || controlPointType == ControlPointType.westFloor;
    }

    public boolean isNorthCeiling() {
        return controlPointType == ControlPointType.northCeil || controlPointType == ControlPointType.ceiling;
    }

    public boolean isSouthCeiling() {
        return controlPointType == ControlPointType.southCeil || controlPointType == ControlPointType.ceiling;
    }

    public boolean isEastCeiling() {
        return controlPointType == ControlPointType.eastCeil || controlPointType == ControlPointType.ceiling;
    }

    public boolean isWestCeiling() {
        return controlPointType == ControlPointType.westCeil || controlPointType == ControlPointType.ceiling;
    }

    public boolean isNorthFloor() {
        return controlPointType == ControlPointType.northFloor || controlPointType == ControlPointType.floor;
    }

    public boolean isSouthFloor() {
        return controlPointType == ControlPointType.southFloor || controlPointType == ControlPointType.floor;
    }

    public boolean isEastFloor() {
        return controlPointType == ControlPointType.eastFloor || controlPointType == ControlPointType.floor;
    }

    public boolean isWestFloor() {
        return controlPointType == ControlPointType.westFloor || controlPointType == ControlPointType.floor;
    }
}
