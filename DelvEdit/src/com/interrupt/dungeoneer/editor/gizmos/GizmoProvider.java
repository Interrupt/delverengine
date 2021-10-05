package com.interrupt.dungeoneer.editor.gizmos;

import com.badlogic.gdx.Gdx;
import com.interrupt.dungeoneer.entities.Entity;
import org.reflections.Reflections;

import java.util.HashMap;
import java.util.Map;

/**
 * This class provides Gizmo objects for rendering Entity visualizations in editor.
 */
public class GizmoProvider {
    private static final Map<Class<?>, Class<?>> registeredClasses = new HashMap<>();
    static {
        // Find and register all available Gizmos.
        Reflections ref = new Reflections("com.interrupt.dungeoneer");
        for (Class<?> gizmoClass : ref.getTypesAnnotatedWith(GizmoFor.class)) {
            GizmoFor annotation = gizmoClass.getAnnotation(GizmoFor.class);
            Class<?> entityClass = annotation.target();
            registeredClasses.put(entityClass, gizmoClass);
        }
    }

    private static final HashMap<Entity, Gizmo> cachedGizmos = new HashMap<>();

    /**
     * Gets a Gizmo object for drawing Entity visualization.
     * @param entity Entity.
     * @return A Gizmo object.
     */
    public static Gizmo get(Entity entity) {
        try {
            Gizmo gizmo = cachedGizmos.get(entity);
            if (gizmo != null) return gizmo;
        }
        catch (Exception ignored) {}

        Class<?> entityClass = entity.getClass();
        Class<?> originalClass = entityClass;

        try {
            Class<?> gizmoClass = registeredClasses.get(entityClass);

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

            // Walk up inheritance looking for a Gizmo.
            Gizmo gizmo = null;
            while (entityClass != null) {
                try {
                    gizmo = (Gizmo) gizmoClass.getDeclaredConstructor(entityClass).newInstance(entity);
                    cachedGizmos.put(entity, gizmo);
                    break;
                }
                catch (Exception ignored) {
                    entityClass = entityClass.getSuperclass();
                }
            }

            return gizmo;
        }
        catch (Exception ex) {
            Gdx.app.log("EXCEPTION", ex.getMessage());
        }

        return null;
    }

    public static Gizmo get(int index) {
        for (Gizmo gizmo : cachedGizmos.values()) {
            if (gizmo.getId() == index) {
                return gizmo;
            }
        }

        return null;
    }
}
