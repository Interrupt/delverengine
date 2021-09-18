package com.interrupt.math;

import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;

import java.io.Serializable;

public class OrientedBoundingBox implements Serializable {
    private static final long serialVersionUID = -5060522561261348048L;

    private final Vector3 min = new Vector3();
    private final Vector3 max = new Vector3();
    private final Vector3 origin = new Vector3();
    private final Quaternion rotation = new Quaternion();

    private final transient Vector3 tmp = new Vector3();

    private final Vector3 xAxis = new Vector3();
    private final Vector3 yAxis = new Vector3();
    private final Vector3 zAxis = new Vector3();

    public OrientedBoundingBox() {}

    public OrientedBoundingBox(Vector3 min, Vector3 max) {
        this(min, max, 0, 0, 0);
    }

    public OrientedBoundingBox(Vector3 min, Vector3 max, float yaw, float pitch, float roll) {
        set(min, max, yaw, pitch, roll);
    }

    public void set(Vector3 min, Vector3 max, float yaw, float pitch, float roll) {
        this.min.set(min);
        this.max.set(max);
        setEulerAngles(yaw, pitch, roll);
    }

    public Vector3 getCorner000(final Vector3 out) {
        return rotation.transform(out.set(min.x, min.y, min.z)).add(origin);
    }

    public Vector3 getCorner001(final Vector3 out) {
        return rotation.transform(out.set(min.x, min.y, max.z)).add(origin);
    }

    public Vector3 getCorner010(final Vector3 out) {
        return rotation.transform(out.set(min.x, max.y, min.z)).add(origin);
    }

    public Vector3 getCorner011(final Vector3 out) {
        return rotation.transform(out.set(min.x, max.y, max.z)).add(origin);
    }

    public Vector3 getCorner100(final Vector3 out) {
        return rotation.transform(out.set(max.x, min.y, min.z)).add(origin);
    }

    public Vector3 getCorner101(final Vector3 out) {
        return rotation.transform(out.set(max.x, min.y, max.z)).add(origin);
    }

    public Vector3 getCorner110(final Vector3 out) {
        return rotation.transform(out.set(max.x, max.y, min.z)).add(origin);
    }

    public Vector3 getCorner111(final Vector3 out) {
        return rotation.transform(out.set(max.x, max.y, max.z)).add(origin);
    }

    public Vector3 getOrigin(final Vector3 out) {
        return out.set(origin);
    }

    public void setOrigin(final Vector3 origin) {
        this.origin.set(origin);
    }

    public void setOrigin(float x, float y, float z) {
        this.origin.set(x, y ,z);
    }

    public void setEulerAngles(float yaw, float pitch, float roll) {
        rotation.setEulerAngles(yaw, pitch, roll);

        xAxis.set(rotation.transform(tmp.set(1, 0, 0)));
        yAxis.set(rotation.transform(tmp.set(0, 1, 0)));
        zAxis.set(rotation.transform(tmp.set(0, 0, 1)));
    }

    private final Range projectedRange = new Range(0);
    private final Range axisRange = new Range(0);
    public boolean intersects(OrientedBoundingBox other) {
        float axisOffset;
        Vector3 axis;
        OrientedBoundingBox a;
        OrientedBoundingBox b;

        // A vs B
        a = this;
        b = other;
        axis = a.xAxis;
        projectedRange.set(b.getCorner000(tmp).dot(axis));
        projectedRange.extend(b.getCorner001(tmp).dot(axis));
        projectedRange.extend(b.getCorner010(tmp).dot(axis));
        projectedRange.extend(b.getCorner011(tmp).dot(axis));
        projectedRange.extend(b.getCorner100(tmp).dot(axis));
        projectedRange.extend(b.getCorner101(tmp).dot(axis));
        projectedRange.extend(b.getCorner110(tmp).dot(axis));
        projectedRange.extend(b.getCorner111(tmp).dot(axis));

        axisOffset = tmp.set(a.origin).dot(axis);
        axisRange.set(a.min.x + axisOffset, a.max.x + axisOffset);

        if (!projectedRange.intersects(axisRange)) return false;

        axis = a.yAxis;
        projectedRange.set(b.getCorner000(tmp).dot(axis));
        projectedRange.extend(b.getCorner001(tmp).dot(axis));
        projectedRange.extend(b.getCorner010(tmp).dot(axis));
        projectedRange.extend(b.getCorner011(tmp).dot(axis));
        projectedRange.extend(b.getCorner100(tmp).dot(axis));
        projectedRange.extend(b.getCorner101(tmp).dot(axis));
        projectedRange.extend(b.getCorner110(tmp).dot(axis));
        projectedRange.extend(b.getCorner111(tmp).dot(axis));

        axisOffset = tmp.set(a.origin).dot(axis);
        axisRange.set(a.min.x + axisOffset, a.max.x + axisOffset);

        if (!projectedRange.intersects(axisRange)) return false;

        axis = a.zAxis;
        projectedRange.set(b.getCorner000(tmp).dot(axis));
        projectedRange.extend(b.getCorner001(tmp).dot(axis));
        projectedRange.extend(b.getCorner010(tmp).dot(axis));
        projectedRange.extend(b.getCorner011(tmp).dot(axis));
        projectedRange.extend(b.getCorner100(tmp).dot(axis));
        projectedRange.extend(b.getCorner101(tmp).dot(axis));
        projectedRange.extend(b.getCorner110(tmp).dot(axis));
        projectedRange.extend(b.getCorner111(tmp).dot(axis));

        axisOffset = tmp.set(a.origin).dot(axis);
        axisRange.set(a.min.x + axisOffset, a.max.x + axisOffset);

        if (!projectedRange.intersects(axisRange)) return false;

        // B vs A
        a = other;
        b = this;
        axis = a.xAxis;
        projectedRange.set(b.getCorner000(tmp).dot(axis));
        projectedRange.extend(b.getCorner001(tmp).dot(axis));
        projectedRange.extend(b.getCorner010(tmp).dot(axis));
        projectedRange.extend(b.getCorner011(tmp).dot(axis));
        projectedRange.extend(b.getCorner100(tmp).dot(axis));
        projectedRange.extend(b.getCorner101(tmp).dot(axis));
        projectedRange.extend(b.getCorner110(tmp).dot(axis));
        projectedRange.extend(b.getCorner111(tmp).dot(axis));

        axisOffset = tmp.set(a.origin).dot(axis);
        axisRange.set(a.min.x + axisOffset, a.max.x + axisOffset);

        if (!projectedRange.intersects(axisRange)) return false;

        axis = a.yAxis;
        projectedRange.set(b.getCorner000(tmp).dot(axis));
        projectedRange.extend(b.getCorner001(tmp).dot(axis));
        projectedRange.extend(b.getCorner010(tmp).dot(axis));
        projectedRange.extend(b.getCorner011(tmp).dot(axis));
        projectedRange.extend(b.getCorner100(tmp).dot(axis));
        projectedRange.extend(b.getCorner101(tmp).dot(axis));
        projectedRange.extend(b.getCorner110(tmp).dot(axis));
        projectedRange.extend(b.getCorner111(tmp).dot(axis));

        axisOffset = tmp.set(a.origin).dot(axis);
        axisRange.set(a.min.x + axisOffset, a.max.x + axisOffset);

        if (!projectedRange.intersects(axisRange)) return false;

        axis = a.zAxis;
        projectedRange.set(b.getCorner000(tmp).dot(axis));
        projectedRange.extend(b.getCorner001(tmp).dot(axis));
        projectedRange.extend(b.getCorner010(tmp).dot(axis));
        projectedRange.extend(b.getCorner011(tmp).dot(axis));
        projectedRange.extend(b.getCorner100(tmp).dot(axis));
        projectedRange.extend(b.getCorner101(tmp).dot(axis));
        projectedRange.extend(b.getCorner110(tmp).dot(axis));
        projectedRange.extend(b.getCorner111(tmp).dot(axis));

        axisOffset = tmp.set(a.origin).dot(axis);
        axisRange.set(a.min.x + axisOffset, a.max.x + axisOffset);

        if (!projectedRange.intersects(axisRange)) return false;

        return true;
    }

    public boolean intersects(BoundingBox other) {
        float axisOffset;
        Vector3 axis;
        OrientedBoundingBox a;
        BoundingBox b;

        // A vs B
        a = this;
        b = other;
        axis = a.xAxis;
        projectedRange.set(b.getCorner000(tmp).dot(axis));
        projectedRange.extend(b.getCorner001(tmp).dot(axis));
        projectedRange.extend(b.getCorner010(tmp).dot(axis));
        projectedRange.extend(b.getCorner011(tmp).dot(axis));
        projectedRange.extend(b.getCorner100(tmp).dot(axis));
        projectedRange.extend(b.getCorner101(tmp).dot(axis));
        projectedRange.extend(b.getCorner110(tmp).dot(axis));
        projectedRange.extend(b.getCorner111(tmp).dot(axis));

        axisOffset = tmp.set(a.origin).dot(axis);
        axisRange.set(a.min.x + axisOffset, a.max.x + axisOffset);

        if (!projectedRange.intersects(axisRange)) return false;

        axis = a.yAxis;
        projectedRange.set(b.getCorner000(tmp).dot(axis));
        projectedRange.extend(b.getCorner001(tmp).dot(axis));
        projectedRange.extend(b.getCorner010(tmp).dot(axis));
        projectedRange.extend(b.getCorner011(tmp).dot(axis));
        projectedRange.extend(b.getCorner100(tmp).dot(axis));
        projectedRange.extend(b.getCorner101(tmp).dot(axis));
        projectedRange.extend(b.getCorner110(tmp).dot(axis));
        projectedRange.extend(b.getCorner111(tmp).dot(axis));

        axisOffset = tmp.set(a.origin).dot(axis);
        axisRange.set(a.min.x + axisOffset, a.max.x + axisOffset);

        if (!projectedRange.intersects(axisRange)) return false;

        axis = a.zAxis;
        projectedRange.set(b.getCorner000(tmp).dot(axis));
        projectedRange.extend(b.getCorner001(tmp).dot(axis));
        projectedRange.extend(b.getCorner010(tmp).dot(axis));
        projectedRange.extend(b.getCorner011(tmp).dot(axis));
        projectedRange.extend(b.getCorner100(tmp).dot(axis));
        projectedRange.extend(b.getCorner101(tmp).dot(axis));
        projectedRange.extend(b.getCorner110(tmp).dot(axis));
        projectedRange.extend(b.getCorner111(tmp).dot(axis));

        axisOffset = tmp.set(a.origin).dot(axis);
        axisRange.set(a.min.x + axisOffset, a.max.x + axisOffset);

        if (!projectedRange.intersects(axisRange)) return false;

        return true;
    }

    public boolean contains(Vector3 point) {
        float projectedPoint;
        float offset;

        // Check x-axis
        projectedPoint = tmp.set(point).dot(xAxis);
        offset = tmp.set(origin).dot(xAxis);
        axisRange.set(min.x + offset, max.x + offset);
        if (!axisRange.contains(projectedPoint)) return false;

        // Check y-axis
        projectedPoint = tmp.set(point).dot(yAxis);
        offset = tmp.set(origin).dot(yAxis);
        axisRange.set(min.y + offset, max.y + offset);
        if (!axisRange.contains(projectedPoint)) return false;

        // Check z-axis
        projectedPoint = tmp.set(point).dot(zAxis);
        offset = tmp.set(origin).dot(zAxis);
        axisRange.set(min.z + offset, max.z + offset);
        if (!axisRange.contains(projectedPoint)) return false;

        return true;
    }
}
