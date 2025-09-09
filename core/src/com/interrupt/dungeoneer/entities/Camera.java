package com.interrupt.dungeoneer.entities;

import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.interrupt.dungeoneer.GameManager;
import com.interrupt.dungeoneer.annotations.EditorProperty;
import com.interrupt.dungeoneer.game.Level;
import com.interrupt.dungeoneer.gfx.GlRenderer;

public class Camera extends DirectionalEntity {
    public Camera() { hidden = true; tex = 13; spriteAtlas = "editor"; }

    @EditorProperty
    public float lookAtDistance = 15f;

    @EditorProperty
    public float animationSpeed = 1.0f;

    public Ray ray = new Ray();
    public Vector3 lookAtLocation = new Vector3();
    public Vector3 swayLookAt = new Vector3();

    public enum CameraAnimation { none, sway, drop, sway_small }

    @EditorProperty
    public boolean enabled = true;

    @EditorProperty
    public CameraAnimation animation = CameraAnimation.sway;

    private transient float tickTime = 0f;

    private Vector3 tVector1 = new Vector3();

    @Override
    public void tick(Level level, float delta) {

        tickTime += delta;

        if(!enabled)
            return;

        GlRenderer renderer = GameManager.renderer;

        if(animation == CameraAnimation.sway) {
            ray.set(tVector1.set(x, z, y), getCameraDirection());
            lookAtLocation = ray.getEndPoint(lookAtLocation, lookAtDistance);
            swayLookAt.set(lookAtLocation);
            swayLookAt.add((float) Math.sin(renderer.time * 0.06f) * 4f * animationSpeed, (float) Math.sin(renderer.time * 0.04f) * 0.5f * animationSpeed, 0f);
        }
        else if(animation == CameraAnimation.sway_small) {
            ray.set(tVector1.set(x, z, y), getCameraDirection());
            lookAtLocation = ray.getEndPoint(lookAtLocation, lookAtDistance);
            swayLookAt.set(lookAtLocation);
            swayLookAt.add((float) Math.sin(renderer.time * 0.06f) * 2.5f * animationSpeed, (float) Math.sin(renderer.time * 0.04f) * 1f * animationSpeed, 0f);
        }

        renderer.cutsceneCamera = this;
        renderer.camera.position.set(x, z, y);
        renderer.camera.up.set(Vector3.Y);
        renderer.camera.far = 30f;
        renderer.camera.direction.set(getCameraDirection());

        if(animation == CameraAnimation.drop) {
            ray.set(tVector1.set(x, z, y), getCameraDirection());
            lookAtLocation = ray.getEndPoint(lookAtLocation, lookAtDistance);
            swayLookAt.set(lookAtLocation);
            swayLookAt.add((float) Math.sin(renderer.time * 0.06f) * 0.1f, (float) Math.sin(renderer.time * 0.04f) * 0.1f, 0f);

            float dropAmount = (1.25f - Interpolation.circleOut.apply(Math.min(tickTime * 0.01f * animationSpeed, 1f)) * 1.25f);
            renderer.camera.position.y += dropAmount;

            renderer.camera.lookAt(swayLookAt);
        }

        if(animation == CameraAnimation.sway || animation == CameraAnimation.sway_small) {
            renderer.camera.lookAt(swayLookAt);
        }

        renderer.camera.update();
    }

    public Vector3 getCameraDirection() {
        Vector3 dir = dirWork.set(1,0,0);
        dir.rotate(Vector3.X, rotation.x);
        dir.rotate(Vector3.Z, rotation.y);
        dir.rotate(Vector3.Y, rotation.z);
        return dir;
    }

    private PerspectiveCamera perspective = new PerspectiveCamera(60f, 800f, 600f);
    public com.badlogic.gdx.graphics.Camera getCamera() {
        perspective.position.set(x, z, y);
        perspective.up.set(Vector3.Y);
        perspective.near = 0.5f;
        perspective.far = 2f;
        perspective.direction.set(getCameraDirection());
        perspective.update(true);
        return perspective;
    }

    public void onTrigger(Entity instigator, String value) {
        enabled = !enabled;

        GlRenderer renderer = GameManager.renderer;
        renderer.cutsceneCamera = enabled ? this : null;
    }
}