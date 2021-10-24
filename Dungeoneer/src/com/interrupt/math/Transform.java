package com.interrupt.math;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

public class Transform {
    private Vector3 position;
    private Quaternion rotation;
    private Vector3 scale;

    private Matrix4 transformation;
    private Matrix4 invTransformation;

    private Transform parent;
    private final Array<Transform> children = new Array<>();

    private final static Vector3 temp = new Vector3();

    public Transform() {
        position = new Vector3();
        rotation = new Quaternion();
        scale = new Vector3(1, 1, 1);
        transformation = new Matrix4();
        invTransformation = new Matrix4(transformation).inv();
        parent = null;
        updateTransformation();
    }

    public Transform set(Transform other) {
        position.set(other.position);
        rotation.set(other.rotation);
        scale.set(other.scale);
        parent = null;
        updateTransformation();

        return this;
    }

    public Transform set(Vector3 position, Quaternion rotation, Vector3 scale) {
        this.position.set(position);
        this.rotation.set(rotation);
        this.scale.set(scale);
        parent = null;
        updateTransformation();

        return this;
    }

    private final Vector3 _position = new Vector3();
    public Vector3 getPosition() {
        if (parent != null) {
            _position.set(position);
            return parent.localToWorldPosition(_position);
        }

        _position.set(0, 0, 0);
        localToWorldPosition(_position);
        return _position;
    }

    public void setPosition(float x, float y, float z) {
        setPosition(temp.set(x, y, z));
    }

    public void setPosition(Vector3 position) {
        temp.set(position);

        if (parent != null) {
            parent.worldToLocalPosition(temp);
        }

        this.position.set(temp);
        updateTransformation();
    }

    private final Quaternion _rotation = new Quaternion();
    public Quaternion getRotation() {
        localToWorldRotation(_rotation.set(rotation));
        return _rotation;
    }

    public void setRotation(float yaw, float pitch, float roll) {
        setRotation(_rotation.setEulerAngles(yaw, pitch, roll));
    }

    public void setRotation(Quaternion rotation) {
        worldToLocalRotation(_rotation.set(rotation));
        this.rotation.set(_rotation);
        updateTransformation();
    }

    public Vector3 getLocalPosition() {
        return this.position;
    }

    public void setLocalPosition(Vector3 position) {
        this.position.set(position);
        updateTransformation();
    }

    public Quaternion getLocalRotation() {
        return this.rotation;
    }

    public void setLocalRotation(Quaternion rotation) {
        this.rotation.set(rotation);
        updateTransformation();
    }

    public Vector3 getLocalScale() {
        return this.scale;
    }

    private static final Vector3 _scale = new Vector3();
    public void setLocalScale(float x, float y, float z) {
        _scale.set(x, y, z);
        setLocalScale(_scale);
    }

    public void setLocalScale(Vector3 scale) {
        this.scale.set(scale);
        updateTransformation();
    }

    public Transform getParent() {
        return this.parent;
    }

    public void setParent(Transform parent) {
        this.parent = parent;
        updateTransformation();
    }

    public Matrix4 getTransformation() {
        return transformation;
    }

    protected void updateTransformation() {
        transformation.idt();
        transformation.setToTranslation(position);
        transformation.rotate(rotation);
        transformation.scale(scale.x, scale.y, scale.z);

        if(parent != null) {
            transformation.mul(parent.transformation);
        }

        invTransformation.set(transformation).inv();

        for (Transform child : children) {
            child.updateTransformation();
        }
    }

    public Transform getChild(int i) {
        return children.get(i);
    }

    public void addChild(Transform child) {
        children.add(child);
        child.setParent(this);
    }

    public Vector3 localToWorldPosition(Vector3 position) {
        return position.mul(transformation);
    }

    Matrix4 m = new Matrix4();
    public Vector3 worldToLocalPosition(Vector3 position) {
        return position.mul(invTransformation);
    }

    private static final Quaternion q = new Quaternion();
    public Quaternion localToWorldRotation(Quaternion quaternion) {
        if (parent != null) {
            return quaternion.mulLeft(parent.getRotation());
        }

        return quaternion;
    }

    public Quaternion worldToLocalRotation(Quaternion quaternion) {
        if (parent == null) {
            return quaternion;
        }

        m.set(parent.invTransformation);
        m.rotate(quaternion);

        return m.getRotation(quaternion);
    }
}
