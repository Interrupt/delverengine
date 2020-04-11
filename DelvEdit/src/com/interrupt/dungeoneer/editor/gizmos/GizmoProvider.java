package com.interrupt.dungeoneer.editor.gizmos;

import com.interrupt.dungeoneer.entities.AmbientSound;
import com.interrupt.dungeoneer.entities.DynamicLight;
import com.interrupt.dungeoneer.entities.Entity;
import com.interrupt.dungeoneer.entities.Light;
import com.interrupt.dungeoneer.entities.areas.Area;

import java.util.HashMap;
import java.util.Map;

public class GizmoProvider {
    private static final Map<Class<?>, Class<?>> registeredClasses = new HashMap<Class<?>, Class<?>>();
    static {
        registeredClasses.put(Entity.class, EntityGizmo.class);
        registeredClasses.put(Light.class, LightGizmo.class);
        registeredClasses.put(DynamicLight.class, DynamicLightGizmo.class);
        registeredClasses.put(AmbientSound.class, AmbientSoundGizmo.class);
        registeredClasses.put(Area.class, AreaGizmo.class);
    }

    public static Gizmo getGizmo(Class<?> entityClass) {
        try {
            Class<?> gizmoClass = registeredClasses.get(entityClass);
            Class<?> originalClass = entityClass;

            // Walk up inheritance looking for a Gizmo.
            while (gizmoClass == null && entityClass != null) {
                entityClass = entityClass.getSuperclass();
                gizmoClass = registeredClasses.get(entityClass);

                // When we find the appropriate Gizmo, cache it.
                if (gizmoClass != null) {
                    GizmoProvider.registeredClasses.put(originalClass, gizmoClass);
                    break;
                }
            }

            // Fallback to Entity gizmo.
            if (gizmoClass == null) {
                gizmoClass = EntityGizmo.class;
            }

            return (Gizmo) gizmoClass.newInstance();
        }
        catch (Exception ignored) {}

        return null;
    }
}
