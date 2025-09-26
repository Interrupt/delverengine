package com.interrupt.dungeoneer.editor.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.interrupt.dungeoneer.gfx.TextureAtlas;

public class EditorColorPicker extends Table {

    final private Color selectedColor = new Color();

    private final Texture colorTexture;
    private final Pixmap colorPixmap;
    private int colorWidth = 10;

    private final ColorHueImage colorHueImage;

    int pixmapWidth;
    int pixmapHeight;

    public EditorColorPicker(final int pixmapWidth, final int pixmapHeight, Color currentColor){
        if(currentColor != null) selectedColor.set(currentColor);

        this.pixmapWidth = pixmapWidth - colorWidth;
        this.pixmapHeight = pixmapHeight;

        colorHueImage = new ColorHueImage(pixmapWidth, pixmapHeight) {
            @Override
            public void onResult(Color picked) {
                selectedColor.set(picked.r, picked.g, picked.b, 1f);
                refreshColorTexture();
                onSetValue(picked);
            }
        };

        colorPixmap = new Pixmap(colorWidth, pixmapHeight, Format.RGBA8888);
        colorTexture = new Texture(colorPixmap);
        refreshColorTexture();

        final Image pColorPixmap = new Image(colorTexture);

        add(pColorPixmap).padRight(4f).minWidth(colorWidth).maxWidth(colorWidth);
        add(colorHueImage).minWidth(pixmapWidth - colorWidth).maxWidth(pixmapWidth - colorWidth).padBottom(6f).padTop(6f);

        pColorPixmap.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {

                ColorPickerDialog pickerDialog = new ColorPickerDialog(EditorUi.smallSkin, selectedColor) {

                    @Override
                    protected void result(Object object) {
                        if(object != null && object instanceof Color) {
                            selectedColor.set((Color)object);
                            refreshColorTexture();
                            onSetValue(selectedColor);
                        }
                    }
                };

                getStage().addActor(pickerDialog);
                pickerDialog.show(getStage());
            }
        });
    }

    public void refreshColorTexture() {
        colorPixmap.setColor(getValue());
        colorPixmap.fill();
        colorTexture.draw(colorPixmap, 0, 0);
    }

    public void onSetValue(Color pNewValue) {

    }

    public Color getValue(){
        return selectedColor;
    }
}