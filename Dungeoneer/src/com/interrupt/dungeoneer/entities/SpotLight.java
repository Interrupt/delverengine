package com.interrupt.dungeoneer.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.LongMap;
import com.interrupt.dungeoneer.GameManager;
import com.interrupt.dungeoneer.annotations.EditorProperty;
import com.interrupt.dungeoneer.game.Level;
import com.interrupt.dungeoneer.gfx.GlRenderer;
import com.interrupt.dungeoneer.gfx.Material;
import com.interrupt.dungeoneer.interfaces.Directional;

public class SpotLight extends Light implements Directional {
	/** Field of view of the SpotLight, in degrees. */
	@EditorProperty
	public float spotLightWidth = 45.0f;

    /** How much of the spotlight should be the hotspot */
    @EditorProperty
    public float hotSpotWidthFactor = 0.5f;

    @EditorProperty
    public float hotSpotIntensity = 0.5f;

    @EditorProperty
    public float hotSpotRangeFactor = 0.75f;

    // Directional interface. Keep track of both the rotation and direction to avoid recalcs
    protected Vector3 rotation = new Vector3(0f,90f,0f);
    private transient boolean rotationIsDirty = true;
    private transient Vector3 direction = new Vector3(1, 0, 0);
    private transient Vector3 dirWork = new Vector3(1, 0, 0);

	public SpotLight() { super(); range = 9.0f; }

	@Override
	public void tick(Level level, float delta)
	{
	}

    @Override
    public void rotate90() {
        super.rotate90();
        rotation.z -= 90f;
        rotationIsDirty = true;
    }

    @Override
    public void rotate90Reversed() {
        super.rotate90Reversed();
        rotation.z += 90f;
        rotationIsDirty = true;
    }

    @Override
    public void setRotation(float rotX, float rotY, float rotZ) {
        rotation.x = rotX;
        rotation.y = rotY;
        rotation.z = rotZ;
        rotationIsDirty = true;
    }

    @Override
    public void rotate(float rotX, float rotY, float rotZ) {
        rotation.x += rotX;
        rotation.y += rotY;
        rotation.z += rotZ;
        rotationIsDirty = true;
    }

    @Override
    public Vector3 getRotation() {
        return rotation;
    }

    public Vector3 getDirection() {
        // Something is modifying the direction after it is asked for, guard against that
        dirWork.set(direction);

        // Only calculate the direction when needed since it will be asked for a lot
        if(!rotationIsDirty)
            return dirWork;

        direction.set(1, 0, 0);
        direction.rotate(Vector3.Y, -rotation.y);
        direction.rotate(Vector3.X, -rotation.x);
        direction.rotate(Vector3.Z, -rotation.z);

        rotationIsDirty = false;
        dirWork.set(direction);
        return dirWork;
    }

    // Handle tile shadowing, and the caching of the results
    @Override
    public float canSeeHowMuch(Level level, float x, float y, float z) {
        if(!shadowTiles)
            return 1f;

        long key = getLightVoxelKey(x, y);

        if (canSeeHowMuchCache.containsKey(key)) {
            return canSeeHowMuchCache.get(key);
        }

        // Default to shadowed
        float canSeeHowMuch = 0.0f;
        final float angleFromSpotlight = angleFromSpotlight(x, y, z);

        // Only do the check in the level if this point is actually in the spotlight
        // This should cut down drastically on the number of checks required
        final float maxSpotLightWidth = Math.max(spotLightWidth, spotLightWidth * hotSpotWidthFactor);
        if(angleFromSpotlight < maxSpotLightWidth) {
            canSeeHowMuch = level.canSeeHowMuch(this.x, this.y, x, y);

            boolean canSee = canSeeHowMuch >= 1f;
            canSeeCache.put(key, canSee);
            canSeeHowMuchCache.put(key, canSeeHowMuch);
        }

        return canSeeHowMuch;
    }

    protected static Vector3 t_angleCalculator = new Vector3();
    protected float angleFromSpotlight(float x2, float y2, float z2) {
        t_angleCalculator.set(x2, y2, z2).sub(x, y, z).nor();
        final float dot = getDirection().dot(t_angleCalculator);
        return (float)Math.toDegrees(Math.acos(dot));
    }

    protected static Color t_attenuateSpotLightCalcColor = new Color();
    protected static Color t_attenuateHotSpotCalcColor = new Color();
    public Color attenuateLightColor(float x2, float y2, float z2) {
        // Spotlights have an inner hot spot, and a wider full range
        Color baseLighting = t_attenuateSpotLightCalcColor.set(0, 0, 0, 0);
        Color hotSpotLighting = t_attenuateHotSpotCalcColor.set(0, 0, 0, 0);

        // Start with the base color
        attenuateLightColor(baseLighting, x2, y2, z2, spotLightWidth);

        // Now add in the hot spot
        if(hotSpotWidthFactor > 0 && hotSpotIntensity > 0 && hotSpotRangeFactor > 0) {
            // Fake out attenuateLightColor by changing the range out from underneath it
            float savedRange = range;
            range *= hotSpotRangeFactor;

            // Find the light values for the hotspot
            attenuateLightColor(hotSpotLighting, x2, y2, z2, spotLightWidth * hotSpotWidthFactor);
            hotSpotLighting.mul(hotSpotIntensity);

            // Switch the range back to what it was
            range = savedRange;
        }

        // Mix the base spot light with the hot spot
        t_attenuateLightCalcColor.set(baseLighting).add(hotSpotLighting);
        return t_attenuateLightCalcColor;
    }

    private void attenuateLightColor(Color outColor, float x2, float y2, float z2, float lightFov) {
        // An FOV > 360.0f is really just an area light, so early out
        if(lightFov >= 360.f) {
            outColor.set(super.attenuateLightColor(x2, y2, z2));
            return;
        }

        final float lightRayAngle = angleFromSpotlight(x2, y2, z2);

        // Easy case, not in the spot light at all
        if(lightRayAngle > lightFov)
            return;

        // Color the light based on distance first
        outColor.set(super.attenuateLightColor(x2, y2, z2));

        // Now multiply based on spot light angle
        float diff = lightFov - lightRayAngle;
        diff /= lightFov;
        outColor.mul(diff);

        // Don't darken the light more after attenuating, but do brighten it
        if(lightIntensity > 1.0f);
            outColor.mul(lightIntensity);
    }
}
