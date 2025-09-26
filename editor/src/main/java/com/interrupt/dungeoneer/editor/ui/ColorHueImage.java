package com.interrupt.dungeoneer.editor.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

public class ColorHueImage extends Group {
    private Pixmap pixmap;
    private Texture texture;

    private Image image;
    private Color tempColor = new Color();

    private float saturationMultiplier = 1f;

    public ColorHueImage(int width, int height) {
        pixmap = makePixmap(height, width);
        texture = new Texture(pixmap);
        image = new Image(texture);

        image.addListener(new ClickListener(){
            private int c;
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                int c = pixmap.getPixel((int)x, pixmap.getHeight() - (int)y);
                Color.rgba8888ToColor(tempColor, c);
                onResult(tempColor);
                return super.touchDown(event, x, y, pointer, button);
            }
            @Override
            public void touchDragged(InputEvent event, float x, float y, int pointer) {
                if(x > pixmap.getWidth() - 1) x = pixmap.getWidth() - 1;
                if(x < 0) x = 0;
                if(y > pixmap.getHeight() - 1) y = pixmap.getHeight() - 1;
                if(y < 0) y = 0;

                c = pixmap.getPixel((int)x, pixmap.getHeight() - (int)y);
                Color.rgba8888ToColor(tempColor, c);
                onResult(tempColor);
                super.touchDragged(event, x, y, pointer);
            }
        });

        setHeight(height);
        setWidth(width);

        addActor(image);
    }

    public void updateSaturation(float saturation) {
        saturationMultiplier = saturation;
        pixmap = makePixmap(pixmap.getWidth(), pixmap.getHeight());
        texture.draw(pixmap, 0, 0);
    }

    public Pixmap makePixmap(int height, int width) {
        Pixmap px = new Pixmap(width, height, Pixmap.Format.RGBA8888);

        for (int x = 0; x < px.getWidth(); x++) {
            for(int y = 0; y < px.getHeight(); y++) {
                int rgb = HSBtoRGBA8888(x / (float)px.getWidth(),
                        Math.min(y / (px.getHeight() * 0.5f), 1f) * saturationMultiplier,
                        Math.max(0f, Math.min(1f, 1f - (y - px.getHeight() / 2) / (px.getHeight() * 0.5f))));
                Color tmp = new Color();
                Color.rgba8888ToColor(tmp, rgb);
                px.setColor(rgb);
                px.drawPixel(x, y);
            }
        }

        return px;
    }

    public void onResult(Color picked) { }

    //taken from https://gist.github.com/mattdesl/5120985

    /** Converts the components of a color, as specified by the HSB model, to an
     * equivalent set of values for the default RGB model.
     * <p>
     * The <code>saturation</code> and <code>brightness</code> components should
     * be floating-point values between zero and one (numbers in the range
     * 0.0-1.0). The <code>hue</code> component can be any floating-point
     * number. The floor of this number is subtracted from it to create a
     * fraction between 0 and 1. This fractional number is then multiplied by
     * 360 to produce the hue angle in the HSB color model.
     * <p>
     * The integer that is returned by <code>HSBtoRGB</code> encodes the value
     * of a color in bits 0-23 of an integer value that is the same format used
     * by the method <code>getRGB</code>. This integer can be
     * supplied as an argument to the <code>Color</code> constructor that takes
     * a single integer argument.
     *
     * @param hue the hue component of the color
     * @param saturation the saturation of the color
     * @param brightness the brightness of the color
     * @return the RGB value of the color with the indicated hue, saturation,
     * and brightness.
     * @see java.awt.Color#getRGB()
     * @see java.awt.Color#Color(int)
     * @see java.awt.image.ColorModel#getRGBdefault()
     * @since JDK1.0 */
    public static int HSBtoRGBA8888(float hue, float saturation, float brightness) {
        int r = 0, g = 0, b = 0;
        if (saturation == 0) {
            r = g = b = (int)(brightness * 255.0f + 0.5f);
        } else {
            float h = (hue - (float)Math.floor(hue)) * 6.0f;
            float f = h - (float)java.lang.Math.floor(h);
            float p = brightness * (1.0f - saturation);
            float q = brightness * (1.0f - saturation * f);
            float t = brightness * (1.0f - (saturation * (1.0f - f)));
            switch ((int)h) {
                case 0:
                    r = (int)(brightness * 255.0f + 0.5f);
                    g = (int)(t * 255.0f + 0.5f);
                    b = (int)(p * 255.0f + 0.5f);
                    break;
                case 1:
                    r = (int)(q * 255.0f + 0.5f);
                    g = (int)(brightness * 255.0f + 0.5f);
                    b = (int)(p * 255.0f + 0.5f);
                    break;
                case 2:
                    r = (int)(p * 255.0f + 0.5f);
                    g = (int)(brightness * 255.0f + 0.5f);
                    b = (int)(t * 255.0f + 0.5f);
                    break;
                case 3:
                    r = (int)(p * 255.0f + 0.5f);
                    g = (int)(q * 255.0f + 0.5f);
                    b = (int)(brightness * 255.0f + 0.5f);
                    break;
                case 4:
                    r = (int)(t * 255.0f + 0.5f);
                    g = (int)(p * 255.0f + 0.5f);
                    b = (int)(brightness * 255.0f + 0.5f);
                    break;
                case 5:
                    r = (int)(brightness * 255.0f + 0.5f);
                    g = (int)(p * 255.0f + 0.5f);
                    b = (int)(q * 255.0f + 0.5f);
                    break;
            }
        }
        return (r << 24) | (g << 16) | (b << 8) | 0x000000ff;
    }
}
