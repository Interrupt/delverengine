package com.badlogic.gdx.vr;

import com.badlogic.gdx.graphics.PerspectiveCamera;
import org.lwjgl.openvr.HmdMatrix34;
import org.lwjgl.openvr.HmdMatrix44;
import org.lwjgl.openvr.VRSystem;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.vr.VRContext.Eye;
import com.badlogic.gdx.vr.VRContext.Space;
import com.badlogic.gdx.vr.VRContext.VRDevice;
import com.badlogic.gdx.vr.VRContext.VRDeviceType;

/**
 * A {@link Camera} implementation for one {@link Eye}
 * of a {@link VRContext}. All properties except {@link Camera#near},
 * {@link Camera#far} and {@link #offset} will be overwritten
 * on every call to {@link #update()} based on the tracked values
 * from the head mounted display. The {@link #offset}
 * vector allows you to position the camera in world space.
 * @author badlogic
 *
 */
public class VRCamera extends PerspectiveCamera {
    public final VRContext context;
    public final Eye eye;
    public final Matrix4 eyeSpace = new Matrix4();
    public final Matrix4 invEyeSpace = new Matrix4();

    public VRCamera(VRContext context, Eye eye) {
        this.context = context;
        this.eye = eye;
    }

    @Override
    public void update() {
        update(true);
    }

    HmdMatrix44 projectionMat = HmdMatrix44.create();
    HmdMatrix34 eyeMat = HmdMatrix34.create();
    Vector3 tmp = new Vector3();

    @Override
    public void update(boolean updateFrustum) {
        // get the projection matrix from the HDM
        VRSystem.VRSystem_GetProjectionMatrix(eye.index, near * VRContext.WORLD_SCALE, far * VRContext.WORLD_SCALE, projectionMat);
        VRContext.hmdMat4toMatrix4(projectionMat, projection);

        // get the eye space matrix from the HDM
        VRSystem.VRSystem_GetEyeToHeadTransform(eye.index, eyeMat);
        VRContext.hmdMat34ToMatrix4(eyeMat, eyeSpace);

        invEyeSpace.set(eyeSpace).inv();

        // get the pose matrix from the HDM
        VRDevice hmd = context.getDeviceByType(VRDeviceType.HeadMountedDisplay);
        Vector3 y = hmd.getUp(Space.World);
        Vector3 z = hmd.getDirection(Space.World);
        Vector3 p = hmd.getPosition(Space.World);

        view.idt();
        view.setToLookAt(p, tmp.set(p).add(z), y);

        position.set(p);
        direction.set(z);
        up.set(y);

        combined.set(projection);
        Matrix4.mul(combined.val, invEyeSpace.val);
        Matrix4.mul(combined.val, view.val);

        // Up the world scale if needed
        combined.scale(VRContext.WORLD_SCALE, VRContext.WORLD_SCALE, VRContext.WORLD_SCALE);

        if (updateFrustum) {
            invProjectionView.set(combined);
            Matrix4.inv(invProjectionView.val);
            frustum.update(invProjectionView);
        }
    }
}
