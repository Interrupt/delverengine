package com.interrupt.dungeoneer.editor.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.*;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.utils.StringBuilder;
import com.interrupt.dungeoneer.annotations.EditorProperty;
import com.interrupt.dungeoneer.editor.Editor;
import com.interrupt.dungeoneer.editor.EditorArt;
import com.interrupt.dungeoneer.entities.*;
import com.interrupt.dungeoneer.gfx.TextureAtlas;
import com.interrupt.dungeoneer.gfx.Material;
import org.lwjgl.LWJGLUtil;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.DecimalFormat;
import java.util.HashMap;

public class PropertiesMenu extends Table {
    public ArrayMap<String, Array<Field>> arrayMap = new ArrayMap<String, Array<Field>>();
    private HashMap<Field, Actor> fieldMap = new HashMap<Field, Actor>();

    private final Array<Entity> selectedEntities;
    private final Array<Class> classes;

    public PropertiesMenu(Skin skin, final Array<Entity> entities) {
        super(skin);
        final Entity entity = entities.get(0);
        selectedEntities = entities;

        // add all of the classes from the first entity
        classes = getClassesForEntity(entity);

        // remove the non-common classes
        Array<Class> nonCommon = new Array<Class>();
        for(Entity e : entities) {
            Array<Class> checkClasses = getClassesForEntity(e);
            for(Class existing : classes) {
                if(!checkClasses.contains(existing, true)) nonCommon.add(existing);
            }
        }
        classes.removeAll(nonCommon, true);

        try {
            // Show entity name as pane header.
            String[] nameParts = entity.getClass().getName().split("\\.");

            String entityName = "Unknown";
            if (nameParts.length > 0) {
                entityName = nameParts[nameParts.length - 1];
            }

            add(new Label(entityName, EditorUi.mediumSkin))
                    .align(Align.left)
                    .padLeft(-12f);
            row();

            // gather all of the fields into groups
            for (Class oClass : classes) {
                Field[] fields = oClass.getDeclaredFields();
                for (int i = 0; i < fields.length; i++) {
                    Field field = fields[i];
                    field.setAccessible(true);

                    String groupName = editorPropertyGroup(field, entity);
                    if (!field.getType().isArray() && !Modifier.isTransient(field.getModifiers()) && isEditorProperty(field)) {
                        Array<Field> groupItems = arrayMap.get(groupName);
                        if (groupItems == null) {
                            groupItems = new Array<Field>();
                            arrayMap.put(groupName, groupItems);
                        }

                        groupItems.add(field);
                    }
                }
            }

            // loop through the groups
            for (ObjectMap.Entry<String, Array<Field>> item : arrayMap.entries()) {
                Array<Field> fields = item.value;
                if (fields == null) continue;

                // don't display some items for a prefab or group
                if (entity instanceof Prefab) {
                    if (!item.key.equals("Prefab") && !item.key.equals("MonsterPrefab")) continue;
                } else if (entity instanceof Group) {
                    if (!item.key.equals("Group") && !item.key.equals("General")) continue;
                }

                if(fields.size == 0) continue;
                add(item.key).colspan(2).align(Align.left).padLeft(-8f);
                row();

                // loop through the fields in the group
                for(final Field field : fields) {
                    Object value = getCommonValue(field, entities);

                    Label label = new Label(field.getName(), skin);
                    label.setColor(1f, 1f, 1f, 0.75f);

                    String v = "";
                    if(value != null) v = value.toString();

                    if(field.getType() == String.class && editorPropertyValidStrings(field, entity) != null) {
                        SelectBox sb = new SelectBox(skin);
                        sb.setItems(editorPropertyValidStrings(field, entity));
                        setSelectedIn(sb, v);
                        sb.addListener(getSelectBoxListener(field));

                        add(label).align(Align.left);
                        add(sb).align(Align.left).fill();

                        fieldMap.put(field, sb);
                    }
                    else if(field.getType() == String.class && isFilePicker(field)) {
                        final String filePickerType = getFilePickerType(field);
                        final Skin finalSkin = skin;
                        final TextButton button = new TextButton(v, skin);
                        final FileHandle folder = new FileHandle(filePickerType);

                        button.addListener(new ClickListener() {
                            @Override
                            public void clicked(InputEvent event, float x, float y) {
                                AssetPicker picker = new AssetPicker("Select File", finalSkin, button.getText().toString(), folder, getFilePickerIncludeBase(field)) {
                                    @Override
                                    protected void result(Object object) {
                                        Boolean result = (Boolean)object;
                                        if(result) {
                                            String picked = getResult();
                                            button.setText(picked);
                                            applyChanges(field);
                                        }
                                    }
                                }.setFileNameEnabled(false);

                                Editor.app.ui.getStage().addActor(picker);
                                picker.show(getStage());
                            }
                        });

                        add(label).align(Align.left);
                        add(button).height(58f).align(Align.left).fill();

                        fieldMap.put(field, button);
                    }
                    else if(field.getType() == String.class) {
                        TextField tf = new TextField(v, skin);
                        tf.setTextFieldListener(getTextFieldListener(field));

                        add(label).align(Align.left);
                        add(tf).align(Align.left).fill();

                        fieldMap.put(field, tf);
                    }
                    else if(field.getType() == Material.class) {
                        Material val = (Material)value;

                        final TextureAtlas atlas = val == null ? null : TextureAtlas.getCachedRegion(val.texAtlas);
                        ImageButton button = null;

                        try {
                            ImageButton.ImageButtonStyle buttonStyle =
                                    new ImageButton.ImageButtonStyle(skin.get(ImageButton.ImageButtonStyle.class));

                            int texVal = 0;

                            if(val != null) {
                                texVal = val.tex;
                                if (texVal < 0) texVal = 0;
                                if (texVal > atlas.getSpriteRegions().length) texVal = atlas.getSpriteRegions().length;

                                Drawable sprite = new TextureRegionDrawable(atlas.getClippedSprite(val.tex));
                                buttonStyle.imageUp = sprite;
                                buttonStyle.imageOver = sprite;
                                buttonStyle.imageDown = sprite;
                            }

                            button = new ImageButton(buttonStyle);
                            button.getImage().setScaling(Scaling.stretch);

                            float scaleMod = 1f / atlas.getClippedSizeMod()[texVal].y;
                            button.getImageCell().height(40f).width(40f * atlas.getClippedSizeMod()[texVal].x * scaleMod);
                        }
                        catch(Exception ex) {
                            ImageButton.ImageButtonStyle buttonStyle =
                                    new ImageButton.ImageButtonStyle(skin.get(ImageButton.ImageButtonStyle.class));

                            buttonStyle.imageUp = null;
                            buttonStyle.imageOver = null;
                            buttonStyle.imageDown = null;

                            button = new ImageButton(buttonStyle);
                        }

                        button.addListener(new ClickListener() {
                            @Override
                            public void clicked(InputEvent event, float x, float y) {
                                TextureRegionPicker picker = new TextureRegionPicker("Pick Sprite", EditorUi.smallSkin, atlas != null ? atlas.name : null, TextureAtlas.getAllSpriteAtlases()) {
                                    @Override
                                    public void result(Integer value, String atlas) {
                                        if(value != null) {
                                            int v = value;
                                            applyChanges(field, new Material(atlas, (byte)v));
                                        }

                                        Editor.app.ui.showEntityPropertiesMenu(false);
                                    }
                                };
                                Editor.app.ui.getStage().addActor(picker);
                                picker.show(Editor.app.ui.getStage());
                            }
                        });

                        add(label).align(Align.left);
                        add(button).height(58f).align(Align.left).fill();

                        fieldMap.put(field, button);
                    }
                    else if((field.getType() == int.class) && isAtlasRegionPicker(field)) {
                        Integer val = (Integer)value;
                        if(val == null) val = 0;

                        final TextureAtlas atlas = TextureAtlas.getCachedRegion(entity.spriteAtlas);
                        ImageButton button = null;

                        try {
                            if(val < 0) val = 0;
                            if (val > atlas.getSpriteRegions().length) val = atlas.getSpriteRegions().length;

                            ImageButton.ImageButtonStyle buttonStyle =
                                    new ImageButton.ImageButtonStyle(skin.get(ImageButton.ImageButtonStyle.class));

                            Drawable sprite = new TextureRegionDrawable(atlas.getClippedSprite(val));
                            buttonStyle.imageUp = sprite;
                            buttonStyle.imageOver = sprite;
                            buttonStyle.imageDown = sprite;

                            button = new ImageButton(buttonStyle);
                            button.getImage().setScaling(Scaling.stretch);

                            float scaleMod = 1f / atlas.getClippedSizeMod()[val].y;
                            button.getImageCell().height(40f).width(40f * atlas.getClippedSizeMod()[val].x * scaleMod);
                        }
                        catch(Exception ex) {
                            ImageButton.ImageButtonStyle buttonStyle =
                                    new ImageButton.ImageButtonStyle(skin.get(ImageButton.ImageButtonStyle.class));

                            buttonStyle.imageUp = null;
                            buttonStyle.imageOver = null;
                            buttonStyle.imageDown = null;

                            button = new ImageButton(buttonStyle);
                        }

                        button.addListener(new ClickListener() {
                           @Override
                           public void clicked(InputEvent event, float x, float y) {
                               TextureRegionPicker picker = new TextureRegionPicker("Pick Sprite", EditorUi.smallSkin, atlas != null ? atlas.name : null, TextureAtlas.getAllSpriteAtlases()) {
                                    @Override
                                    public void result(Integer value, String atlas) {

                                        for(int i = 0; i < entities.size; i++) {
                                            entities.get(i).spriteAtlas = atlas;
                                            if (entities.get(i).drawable != null) entities.get(i).drawable.refresh();
                                        }

                                        applyChanges(field, value.toString());
                                        Editor.app.ui.showEntityPropertiesMenu(false);
                                    }
                               };
                               Editor.app.ui.getStage().addActor(picker);
                               picker.show(Editor.app.ui.getStage());
                           }
                       });

                        add(label).align(Align.left);
                        add(button).height(58f).align(Align.left).fill();

                        fieldMap.put(field, button);
                    }
                    else if(field.getType() == int.class || field.getType() == Integer.class) {
                        final TextField tf = new TextField(v, skin);
                        tf.setTextFieldFilter(new IntegerFilter());
                        tf.setTextFieldListener(getTextFieldListener(field));
                        tf.addListener(new InputListener() {
                            @Override
                            public boolean keyDown(InputEvent event, int keycode) {
                                if(keycode == Input.Keys.UP) {
                                    int val = 0;
                                    if(!tf.getText().equals("")) val = Integer.parseInt(tf.getText());
                                    tf.setTextFieldFilter(null);
                                    tf.setText(Integer.toString(val + 1));
                                    tf.setTextFieldFilter(new IntegerFilter());
                                }
                                else if(keycode == Input.Keys.DOWN) {
                                    int val = 0;
                                    if(!tf.getText().equals("")) val = Integer.parseInt(tf.getText());
                                    tf.setTextFieldFilter(null);
                                    tf.setText(Integer.toString(val - 1));
                                    tf.setTextFieldFilter(new IntegerFilter());
                                }
                                return super.keyDown(event, keycode);
                            }
                        });

                        add(label).align(Align.left);
                        add(tf).align(Align.left).fill();

                        fieldMap.put(field, tf);
                    }
                    else if(field.getType() == float.class || field.getType() == double.class) {
                        final TextField tf = new TextField(v, skin);
                        tf.setTextFieldFilter(new DecimalsFilter());
                        tf.setTextFieldListener(getTextFieldListener(field));
                        tf.addListener(new InputListener() {
                            @Override
                            public boolean keyDown(InputEvent event, int keycode) {
                                DecimalFormat format = new DecimalFormat("##.##");
                                if(keycode == Input.Keys.UP) {
                                    double dval = 0;
                                    if(!tf.getText().equals("")) dval = Double.parseDouble(tf.getText());
                                    dval += 0.1;
                                    tf.setTextFieldFilter(null);
                                    tf.setText(format.format(dval));
                                    tf.setTextFieldFilter(new DecimalsFilter());
                                }
                                else if(keycode == Input.Keys.DOWN) {
                                    double dval = 0;
                                    if(!tf.getText().equals("")) dval = Double.parseDouble(tf.getText());
                                    dval -= 0.1;
                                    tf.setTextFieldFilter(null);
                                    tf.setText(format.format(dval));
                                    tf.setTextFieldFilter(new DecimalsFilter());
                                }
                                return false;
                            }
                        });

                        // Allow drag on label to scrub value.
                        label.addListener(new InputListener() {
                            private final DecimalFormat format = new DecimalFormat("##.##");
                            private float firstX;
                            private float firstY;
                            private float lastX;

                            @Override
                            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                                firstX = Gdx.input.getX();
                                firstY = Gdx.input.getY();
                                lastX = x;

                                if (!Gdx.input.isCursorCatched()) {
                                    Gdx.input.setCursorCatched(true);
                                }

                                return true;
                            }

                            @Override
                            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                                if (Gdx.input.isCursorCatched()) {
                                    Gdx.input.setCursorCatched(false);
                                }

                                Gdx.input.setCursorPosition((int) firstX, (int) firstY);
                                if (LWJGLUtil.getPlatform() == LWJGLUtil.PLATFORM_MACOSX) {
                                    Gdx.input.setCursorPosition((int) firstX, Gdx.graphics.getHeight() - 1 - (int) firstY);
                                }
                            }

                            @Override
                            public void touchDragged(InputEvent event, float x, float y, int pointer) {
                                float dx = x - lastX;

                                double dval = 0;
                                if(!tf.getText().equals("")) dval = Double.parseDouble(tf.getText());
                                dval += 0.1 * dx / 10;
                                tf.setTextFieldFilter(null);
                                tf.setText(format.format(dval));
                                tf.setTextFieldFilter(new DecimalsFilter());

                                applyChanges(field);

                                lastX = x;
                            }
                        });

                        add(label).align(Align.left);
                        add(tf).align(Align.left).fill();

                        fieldMap.put(field, tf);
                    }
                    else if(field.getType().isEnum()) {
                        SelectBox sb = new SelectBox(skin);
                        sb.setItems(field.getType().getEnumConstants());
                        setSelectedIn(sb, v);
                        sb.addListener(getSelectBoxListener(field));

                        add(label).align(Align.left);
                        add(sb).align(Align.left).fill();

                        fieldMap.put(field, sb);
                    }
                    else if(field.getType() == Vector3.class) {
                        Vector3 vecval = (Vector3)value;

                        TextField xField = new TextField(vecval != null ? Float.toString(vecval.x) : "", skin);
                        xField.setTextFieldFilter(new DecimalsFilter());
                        xField.setTextFieldListener(getTextFieldListener(field));

                        TextField yField = new TextField(vecval != null ? Float.toString(vecval.y) : "", skin);
                        yField.setTextFieldFilter(new DecimalsFilter());
                        yField.setTextFieldListener(getTextFieldListener(field));

                        TextField zField = new TextField(vecval != null ? Float.toString(vecval.z) : "", skin);
                        zField.setTextFieldFilter(new DecimalsFilter());
                        zField.setTextFieldListener(getTextFieldListener(field));

                        Table vecTable = new Table(skin);
                        vecTable.add(xField).width(62f);
                        vecTable.add(yField).width(62f);
                        vecTable.add(zField).width(62f);
                        vecTable.pack();

                        add(label).align(Align.left);
                        add(vecTable).align(Align.left);

                        fieldMap.put(field, vecTable);
                    }
                    else if(field.getType() == Color.class) {
                        Color colorval = (Color)value;
                        if(colorval == null) colorval = new Color();

                        EditorColorPicker picker = new EditorColorPicker(200, 48, colorval) {
                            @Override
                            public void onSetValue(Color color) {
                                applyChanges(field);
                            }
                        };

                        add(label).align(Align.left);
                        add(picker).align(Align.left);

                        fieldMap.put(field, picker);
                    }
                    else if(field.getType() == boolean.class) {
                        Boolean[] values = new Boolean[2];
                        values[0] = true;
                        values[1] = false;

                        SelectBox sb = new SelectBox(skin);
                        sb.setItems(values);
                        setSelectedIn(sb, v);
                        sb.addListener(getSelectBoxListener(field));

                        add(label).align(Align.left);
                        add(sb).align(Align.left).fill();

                        fieldMap.put(field, sb);
                    }

                    row();
                }
            }

            pack();
        }
        catch(Exception ex) {
            Gdx.app.error("DelvEdit", ex.getMessage());
        }
    }

    public Array<Class> getClassesForEntity(Entity entity) {
        Array<Class> classes = new Array<Class>();
        {
            Class c = entity.getClass();
            while (c != null) {
                classes.add(c);
                c = c.getSuperclass();
            }
        }
        return classes;
    }

    public static boolean isEditorProperty(Field field) {

        Annotation annotation = field.getAnnotation(EditorProperty.class);
        if(annotation != null) return true;

        return false;
    }

    public static String editorPropertyGroup(Field field, Entity entity) {
        EditorProperty annotation = field.getAnnotation(EditorProperty.class);
        if(annotation != null && !annotation.group().equals("")) return annotation.group();
        return entity.getClass().getSimpleName();
    }

    public static String[] editorPropertyValidStrings(Field field, Entity entity) {
        EditorProperty annotation = field.getAnnotation(EditorProperty.class);
        if(annotation != null && annotation.valid().length > 0) return annotation.valid();

        if(annotation != null && annotation.type().equals("SPRITE_ATLAS_LIST")) {
            return EditorArt.getAtlasList();
        }

        return null;
    }

    public static boolean isAtlasRegionPicker(Field field) {
        EditorProperty annotation = field.getAnnotation(EditorProperty.class);
        if(annotation != null && annotation.type().equals("SPRITE_ATLAS_NUM")) return true;
        return false;
    }

    public static boolean isFilePicker(Field field) {
        EditorProperty annotation = field.getAnnotation(EditorProperty.class);
        if(annotation != null && annotation.type().equals("FILE_PICKER")) return true;
        return false;
    }

    public static String getFilePickerType(Field field) {
        EditorProperty annotation = field.getAnnotation(EditorProperty.class);
        if(annotation != null && annotation.type().equals("FILE_PICKER")) return annotation.params();
        return null;
    }

    public static boolean getFilePickerIncludeBase(Field field) {
        EditorProperty annotation = field.getAnnotation(EditorProperty.class);
        if(annotation != null && annotation.type().equals("FILE_PICKER")) return annotation.include_base();
        return false;
    }

    private Object getCommonValue(Field field, Array<Entity> entities) throws IllegalAccessException {
        Object commonVal = null;
        for(Entity entity : entities) {
            Object val = field.get(entity);
            if(commonVal == null && val != null) commonVal = val;
            else if(commonVal != null && !commonVal.equals(val)) return null; // no common value, return null
        }

        // found a common value!
        return commonVal;
    }

    public TextField.TextFieldListener getTextFieldListener(final Field currentField) {
        return new TextField.TextFieldListener() {
            public void keyTyped (TextField textField, char key) {
                applyChanges(currentField);
            }
        };
    }

    public ChangeListener getSelectBoxListener(final Field currentField) {
        return new ChangeListener() {
            public void changed(ChangeEvent changeEvent, Actor actor) {
                applyChanges(currentField);
                Editor.app.ui.showEntityPropertiesMenu(false);
            }
        };
    }

    public void applyChanges(final Field currentField, String val) {
        try {
            if (currentField.getType() == int.class) {
                if (val.equals("")) val = "0";
                for (Entity entity : selectedEntities) {
                    currentField.set(entity, Integer.parseInt(val));
                }
            }
        }
        catch(Exception ex) {
            Gdx.app.error("DelvEdit", ex.getMessage());
        }
    }

    public void applyChanges(final Field currentField, Material val) {
        try {
            if (currentField.getType() == Material.class && val != null) {
                for (Entity entity : selectedEntities) {
                    currentField.set(entity, new Material(val.texAtlas, val.tex));
                }
            }
        }
        catch(Exception ex) {
            Gdx.app.error("DelvEdit", ex.getMessage());
        }
    }

    public void applyChanges(final Field currentField) {
        try {
            Gdx.app.debug("DelvEdit", "Applying change for " + currentField.getName());

            if(selectedEntities != null && fieldMap.containsKey(currentField)) {
                Actor actor = fieldMap.get(currentField);

                // apply based on the type of input
                if(actor instanceof TextField) {
                    String val = ((TextField)actor).getText();

                    if(currentField.getType() == int.class) {
                        if(val.equals("")) val = "0";
                        for(Entity entity : selectedEntities) {
                            currentField.set(entity, Integer.parseInt(val));
                        }
                    }
                    else if(currentField.getType() == Integer.class) {
                        if(val.equals("")) val = null;
                        for(Entity entity : selectedEntities) {
                            currentField.set(entity, Integer.parseInt(val));
                        }
                    }
                    else if(currentField.getType() == float.class) {
                        if(val.equals("")) val = "0";
                        for(Entity entity : selectedEntities) {
                            currentField.set(entity, Float.parseFloat(val));
                        }
                    }
                    else if(currentField.getType() == double.class) {
                        if(val.equals("")) val = "0";
                        for(Entity entity : selectedEntities) {
                            currentField.set(entity, Double.parseDouble(val));
                        }
                    }
                    else if(currentField.getType() == String.class) {
                        for(Entity entity : selectedEntities) {
                            currentField.set(entity, val);
                        }
                    }
                }
                else if(actor instanceof TextButton) {
                    String val = ((TextButton)actor).getText().toString();
                    if(currentField.getType() == String.class) {
                        for(Entity entity : selectedEntities) {
                            currentField.set(entity, val);
                        }
                    }
                }
                else if(actor instanceof SelectBox) {
                    String val = ((SelectBox)actor).getSelected().toString();
                    if(currentField.getType() == boolean.class) {
                        for(Entity entity : selectedEntities) {
                            currentField.set(entity, Boolean.parseBoolean(val));
                        }
                    }
                    else if(currentField.getType().isEnum()) {
                        for(Entity entity : selectedEntities) {
                            currentField.set(entity, Enum.valueOf((Class<Enum>) currentField.getType(), val));
                        }
                    }
                    else if(currentField.getType() == String.class) {
                        for(Entity entity : selectedEntities) {
                            currentField.set(entity, val);
                        }
                    }
                }
                else if(actor instanceof EditorColorPicker) {
                    EditorColorPicker picker = (EditorColorPicker)actor;

                    for(Entity entity : selectedEntities) {
                        currentField.set(entity, new Color(picker.getValue()));
                    }
                }
                else if(actor instanceof Table) {
                    Table t = (Table)actor;

                    if(currentField.getType() == Vector3.class) {
                        String x = ((TextField)t.getCells().get(0).getActor()).getText();
                        String y = ((TextField)t.getCells().get(1).getActor()).getText();
                        String z = ((TextField)t.getCells().get(2).getActor()).getText();

                        if(x.equals("")) x = "0";
                        if(y.equals("")) y = "0";
                        if(z.equals("")) z = "0";

                        for(Entity entity : selectedEntities) {
                            currentField.set(entity, new Vector3(Float.parseFloat(x), Float.parseFloat(y), Float.parseFloat(z)));
                        }
                    }
                    else if(currentField.getType() == Color.class) {
                        String r = ((TextField)t.getCells().get(0).getActor()).getText();
                        String g = ((TextField)t.getCells().get(1).getActor()).getText();
                        String b = ((TextField)t.getCells().get(2).getActor()).getText();

                        if(r.equals("")) r = "0";
                        if(g.equals("")) g = "0";
                        if(b.equals("")) b = "0";

                        for(Entity entity : selectedEntities) {
                            currentField.set(entity, new Color(Float.parseFloat(r) / 255f, Float.parseFloat(g) / 255f, Float.parseFloat(b) / 255f, 1f));
                        }
                    }
                }
            }

            Editor.app.refreshLights();

            for(Entity entity : selectedEntities) {
                if (entity.drawable != null) entity.drawable.refresh();

                if (entity instanceof ProjectedDecal) {
                    ((ProjectedDecal) entity).refresh();
                }
            }
        }
        catch(Exception ex) {
            Gdx.app.error("DelvEdit", ex.getMessage());
        }
    }

    static public class DecimalsFilter implements TextField.TextFieldFilter {
        @Override
        public boolean acceptChar (TextField textField, char c) {
            try {
                String newValue = new StringBuilder(textField.getText()).insert(textField.getCursorPosition(), c).toString();
                if(newValue.equals("-")) return true;

                Double.parseDouble(newValue);
                return true;
            }
            catch(Exception ex) { }
            return false;
        }
    }

    static public class IntegerFilter implements TextField.TextFieldFilter {
        private int max = Integer.MAX_VALUE;
        private int min = Integer.MIN_VALUE;

        public IntegerFilter() { }

        public IntegerFilter(int min, int max) {
            this.max = max;
            this.min = min;
        }

        @Override
        public boolean acceptChar (TextField textField, char c) {
            try {
                String newValue = new StringBuilder(textField.getText()).insert(textField.getCursorPosition(), c).toString();
                if(newValue.equals("-")) return true;

                Integer parsed = Integer.parseInt(newValue);
                return parsed <= max && parsed >= min;
            }
            catch(Exception ex) { }
            return false;
        }
    }

    public void setSelectedIn(SelectBox select, String value) {
        Array items = select.getItems();
        for(int i = 0; i < items.size; i++) {
            if(items.get(i).toString().equals(value)) {
                select.setSelectedIndex(i);
                return;
            }
        }
    }
}
