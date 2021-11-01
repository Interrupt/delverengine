package com.interrupt.dungeoneer.ui.values;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import com.badlogic.gdx.graphics.Color;

public abstract class ReflectedValue extends DynamicValue {
    public String field;

    public abstract Object getObject();

    @Override
    public String stringValue() {
        validate();
        return super.stringValue();
    }

    @Override
    public int intValue() {
        validate();
        return super.intValue();
    }

    @Override
    public float floatValue() {
        validate();
        return super.floatValue();
    }

    @Override
    public boolean booleanValue() {
        validate();
        return super.booleanValue();
    }

    @Override
    public Color colorValue() {
        validate();
        return super.colorValue();
    }

    private void validate() {
        Object object = getObject();

        if (object == null || field == null || field.isEmpty()) {
            setValue("");
            return;
        }

        String name = field.replaceAll("\\(\\)", "");
        String[] pieces = name.split("\\.");

        for (int i = 0; i < pieces.length - 1; i++) {
            String part = pieces[i];
            object = eval(object, part);

            if (object == null) {
                setValue("");
                return;
            }
        }

        name = pieces[pieces.length - 1];

        Field f = getField(object, name);
        if (f != null) {
            try {
                Class<?> fieldType = f.getType();

                if ((fieldType).isAssignableFrom(int.class)) {
                    setValue((int) f.get(object));
                    return;
                }
                else if ((fieldType).isAssignableFrom(float.class)) {
                    setValue((float) f.get(object));
                    return;
                }
                else if ((fieldType).isAssignableFrom(boolean.class)) {
                    setValue((boolean) f.get(object));
                    return;
                }
                else if ((fieldType).isAssignableFrom(String.class)) {
                    setValue((String) f.get(object));
                    return;
                }
                else if((fieldType).isAssignableFrom(Color.class)) {
                    setValue((Color) f.get(object));
                    return;
                }
                else {
                    setValue("UNSUPPORTED TYPE");
                    return;
                }
            }
            catch (Exception ignored) {}
        }

        Method method = getMethod(object, name);
        if (method != null) {
            try {
                Class<?> returnType = method.getReturnType();

                if ((returnType).isAssignableFrom(int.class)) {
                    setValue((int)method.invoke(object));
                    return;
                }
                else if ((returnType).isAssignableFrom(float.class)) {
                    setValue((float)method.invoke(object));
                    return;
                }
                else if ((returnType).isAssignableFrom(boolean.class)){
                    setValue((boolean)method.invoke(object));
                    return;
                }
                else if ((returnType).isAssignableFrom(String.class)) {
                    setValue((String)method.invoke(object));
                    return;
                }
                else if((returnType).isAssignableFrom(Color.class)) {
                    setValue((Color)method.invoke(object));
                    return;
                }
                else {
                    setValue("UNSUPPORTED RETURN TYPE");
                    return;
                }
            } catch (Exception ignored) {}
        }

        setValue("UNKNOWN FIELD");
    }

    private static Field getField(Object object, String name) {
        if (object == null || name == null || name.isEmpty()) {
            return null;
        }

        Field field = null;
        Class<?> o = object.getClass();
        while (o != Object.class) {
            try {
                field = o.getDeclaredField(name);
                field.setAccessible(true);
                break;
            }
            catch (Exception ignored) {
                if (o.getSuperclass() == Object.class) {
                    break;
                }

                o = o.getSuperclass();
            }
        }

        return field;
    }

    private static Method getMethod(Object object, String name) {
        if (object == null || name == null || name.isEmpty()) {
            return null;
        }

        Method method = null;
        Class<?> o = object.getClass();
        while (o != Object.class) {
            try {
                method = o.getDeclaredMethod(name);
                method.setAccessible(true);
                break;
            }
            catch (Exception ignored) {
                if (o.getSuperclass() == Object.class) {
                    break;
                }

                o = o.getSuperclass();
            }
        }

        return method;
    }

    /** Get value for given field or method name. */
    private Object eval(Object object, String name) {
        try {
            Field f = getField(object, name);
            return f.get(object);
        }
        catch (Exception ignored) {}

        try {
            Method method = getMethod(object, name);
            return method.invoke(object);
        }
        catch (Exception ignored) {}

        return null;
    }
}
