package com.interrupt.dungeoneer.editor.gizmos;

import org.reflections.Reflections;

import java.util.HashMap;
import java.util.Map;

/**
 * This class provides Gizmo objects for rendering Entity visualizations in editor.
 */
public class GizmoProvider {
    private static final Map<Class<?>, Class<?>> registeredClasses = new HashMap<Class<?>, Class<?>>();
    static {
        // Find and register all available Gizmos.
        Reflections ref = new Reflections("com.interrupt.dungeoneer");
        for (Class<?> gizmoClass : ref.getTypesAnnotatedWith(GizmoFor.class)) {
            GizmoFor annotation = gizmoClass.getAnnotation(GizmoFor.class);
            Class<?> entityClass = annotation.target();
            registeredClasses.put(entityClass, gizmoClass);
        }
    }

    /**
     * Gets a Gizmo object for drawing Entity visualization.
     * @param entityClass Class of Entity.
     * @return A Gizmo object.
     */
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

        return new EntityGizmo();
    }
}
