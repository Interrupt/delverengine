package com.interrupt.dungeoneer.ui;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.interrupt.dungeoneer.Art;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.game.Options;

public class UiSkin {

	private static Skin skin;
    private static BitmapFont font;

    public static Skin getSkin() {
    	if(skin == null) loadSkin();
    	//font.setScale(1f);
    	return skin;
    }

    public static BitmapFont getFont() {
    	if(font == null) loadSkin();
    	//font.setScale(1f);
    	return font;
    }

    public static void loadSkin() {
    	Texture t = Art.loadTexture("ui/skin.png");
		TextureAtlas atlas = new TextureAtlas();
		TextureAtlas.AtlasRegion upRegion = atlas.addRegion("up", t, 0, 0, 48, 16);
		TextureAtlas.AtlasRegion downRegion = atlas.addRegion("down", t, 0, 16, 48, 16);
		TextureAtlas.AtlasRegion sliderRegion = atlas.addRegion("slider", t, 16, 32, 16, 16);
		TextureAtlas.AtlasRegion knobBeforeRegion = atlas.addRegion("knobBefore", t, 32, 32, 16, 16);

		atlas.addRegion("check-on", t, 0, 32, 16, 16);
		atlas.addRegion("check-off", t, 0, 48, 16, 16);
		atlas.addRegion("knob", t, 16, 48, 16, 16);
		atlas.addRegion("knobDown", t, 32, 48, 16, 16);

        Texture windowTexture = Art.loadTexture("ui/window.png");
        Texture noteTexture = Art.loadTexture("ui/note.png");
        Texture mapTexture = Art.loadTexture("ui/map-bg.png");
        Texture saveSelectTexture = Art.loadTexture("ui/save-select.png");
        Texture inventoryWindowTexture = Art.loadTexture("ui/inventory-window.png");
        Texture inventoryButtons = Art.loadTexture("ui/inventory-buttons.png");
        Texture tooltipTexture = Art.loadTexture("ui/tooltip.png");
		Texture tableHover = Art.loadTexture("ui/table-hover.png");
		Texture tableNoHover = Art.loadTexture("ui/table-no-hover.png");

		TextureAtlas.AtlasRegion windowRegion = atlas.addRegion("window", windowTexture, 0, 0, windowTexture.getWidth(), windowTexture.getHeight());
		TextureAtlas.AtlasRegion tooltipRegion = atlas.addRegion("tooltip-window", tooltipTexture, 0, 0, tooltipTexture.getWidth(), tooltipTexture.getHeight());
		atlas.addRegion("note-window", noteTexture, 0, 0, noteTexture.getWidth(), noteTexture.getHeight());
        atlas.addRegion("map-window", mapTexture, 0, 0, mapTexture.getWidth(), mapTexture.getHeight());
        atlas.addRegion("save-select", saveSelectTexture, 0, 0, saveSelectTexture.getWidth(), saveSelectTexture.getHeight());
		TextureAtlas.AtlasRegion tableHoverAtlas = atlas.addRegion("table-hover", tableHover, 0, 0, tableHover.getWidth(), tableHover.getHeight());
		TextureAtlas.AtlasRegion tableNoHoverAtlas = atlas.addRegion("table-no-hover", tableNoHover, 0, 0, tableNoHover.getWidth(), tableNoHover.getHeight());

        // inventory
		atlas.addRegion("inventory-window", inventoryWindowTexture, 0, 0, inventoryWindowTexture.getWidth(), inventoryWindowTexture.getHeight());
		atlas.addRegion("menu-inv-btn-active", inventoryButtons, 0, 0, 30, 20);
		atlas.addRegion("menu-char-btn-active", inventoryButtons, 0, 22, 30, 20);
		atlas.addRegion("menu-inv-btn-inactive", inventoryButtons, 0, 44, 30, 20);
		atlas.addRegion("menu-char-btn-inactive", inventoryButtons, 0, 66, 30, 20);

		upRegion.splits = new int[] {6, 6, 5, 9};
		upRegion.pads = new int[] {0, 0, 4, 8};

		downRegion.splits = new int[] {6, 6, 7, 7};
		downRegion.pads = new int[] {0, 0, 4, 8};

		sliderRegion.splits = new int[] {4, 4, 8, 7};
		sliderRegion.pads = new int[] {0, 0, 0, 0};

		knobBeforeRegion.splits = new int[] {4, 4, 8, 7};
		knobBeforeRegion.pads = new int[] {0, 0, 0, 0};

		tooltipRegion.splits = new int[]{10, 10, 10, 10};
		tooltipRegion.pads = new int[]{8, 8, 8, 8};

		tableHoverAtlas.splits = new int[]{1, 1, 1, 1};
		tableHoverAtlas.pads = new int[]{4, 1, 4, 2};

		tableNoHoverAtlas.splits = new int[]{1, 1, 1, 1};
		tableNoHoverAtlas.pads = new int[]{4, 1, 4, 2};

		windowRegion.splits = new int[] {8, 8, 8, 8};
		windowRegion.pads = new int[] {0, 0, 0, 0};

		skin = new Skin(Game.getInternal("ui/skin.json"), atlas);

		t.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);

		// Support for modded fonts
		font = new BitmapFont(Game.findInternalFileInMods("ui/pixel.fnt"), Game.findInternalFileInMods("ui/pixel.png"), false, true);
		font.getData().markupEnabled = true;
		font.getData().padBottom = 0f;
		font.getData().padTop    = 0f;
		font.getData().padLeft   = 0f;
		font.getData().padRight   = 0f;
		skin.add("default-font", font);

		TextButtonStyle textButtonStyle = skin.get(TextButtonStyle.class);
		textButtonStyle.font = font;
		skin.add("default", textButtonStyle);

		TextButtonStyle textButtonGamepadStyle = skin.get("gamepad-selected", TextButtonStyle.class);
		if(textButtonGamepadStyle != null) {
			textButtonGamepadStyle.font = font;
			skin.add("gamepad-selected", textButtonGamepadStyle);
		}

		LabelStyle labelStyle = skin.get(LabelStyle.class);
		labelStyle.font = font;
		skin.add("default", labelStyle);

		Texture fontTex = Art.loadTexture("ui/pixel.png");
		float fontScale = fontTex.getWidth() / 128;

		for(int ii = 0; ii < font.getData().glyphs.length; ii++) {
			for(int i = 0; font.getData().glyphs[ii] != null && i < font.getData().glyphs[ii].length; i++) {
				if(font.getData().glyphs[ii][i] != null) {
					font.getData().glyphs[ii][i].u *= fontScale;
					font.getData().glyphs[ii][i].v *= fontScale;
					font.getData().glyphs[ii][i].u2 *= fontScale;
					font.getData().glyphs[ii][i].v2 *= fontScale;

					// offset the fonts a bit to not bleed into the next

					float fontOffsetOffset = 0.000025f * Options.instance.antiAliasingSamples;
					float fontOffset = fontScale * 0.0005f + fontOffsetOffset;
					font.getData().glyphs[ii][i].u += fontOffset;
					font.getData().glyphs[ii][i].v -= fontOffset;
					font.getData().glyphs[ii][i].u2 -= fontOffset;
					font.getData().glyphs[ii][i].v2 += fontOffset;
				}
			}
		}
    }

    public static void clearCache() {
    	if(skin != null) {
    		//skin.dispose();
    		skin = null;
    	}

    	if(font != null) {
    		//font.dispose();
    		font = null;
    	}

		loadSkin();
    }
}
