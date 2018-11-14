package com.interrupt.dungeoneer.overlays;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.BaseDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.interrupt.api.steam.SteamApi;
import com.interrupt.dungeoneer.Audio;
import com.interrupt.dungeoneer.entities.Item;
import com.interrupt.dungeoneer.entities.Player;
import com.interrupt.dungeoneer.game.Colors;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.gfx.TextureAtlas;
import com.interrupt.dungeoneer.ui.UiSkin;
import com.interrupt.helpers.ShopItem;
import com.interrupt.managers.ItemManager;
import com.interrupt.managers.StringManager;

public class ShopOverlay extends WindowOverlay {
	
	private final Player player;
	private String titleText = StringManager.get("overlays.ShopPause.titleText");
	private String descriptionText = StringManager.get("overlays.ShopPause.descriptionText");
	private String itemPrefix = "";
	private Array<ShopItem> items;
	private Array<ShopItem> selected = new Array<ShopItem>();
	private Label lblTotalAmount;
	private Label lblGoldAmount;
	
	public ShopOverlay(Player player) { this.player = player; }
	public ShopOverlay(Player player, String prefix, String title, String description, Array<ShopItem> items) {
		this.player = player;
		this.titleText = title;
		this.itemPrefix = prefix;
		this.descriptionText = description;
		this.items = items;
	}
	
	public ShopOverlay(Player player, String title, String description, Array<ShopItem> items) {
		this.player = player;
		this.titleText = title;
		this.descriptionText = description;
		this.items = items;
	}
	
	@Override
	public void onShow() {
		super.onShow();
		
		final Overlay thisOverlay = this;
		InputListener listener = new com.badlogic.gdx.scenes.scene2d.InputListener() {
			@Override
			public boolean keyDown(InputEvent event, int keycode) {
				if(keycode == Keys.ESCAPE || keycode == Keys.BACK) {
					OverlayManager.instance.remove(thisOverlay);
				}
				return false;
			}
		};
		ui.addListener(listener);
		
		Audio.playSound("/ui/ui_dialogue_open.mp3", 0.35f);
	}
	
	@Override
	public void onHide() {
		Audio.playSound("/ui/ui_dialogue_close.mp3", 0.35f);
	}

	protected void makeBuyModal(final ShopItem item, Image itemIcon) {
		buttonOrder.clear();
		gamepadSelectionIndex = null;
		lastGamepadSelectionIndex = null;

		int width = 160;

		itemIcon.setWidth(14);
		itemIcon.setHeight(14);

		Table contentTable = new Table();
		contentTable.add(itemIcon).align(Align.left).padRight(4);

		Table itemInfoTable = new Table();

		Label title = new Label(item.getName(), skin.get(LabelStyle.class));
		title.setFontScale(0.75f);
		title.setWrap(true);
		itemInfoTable.add(title).width(width).align(Align.left).padBottom(3).row();

		if(item.getDescription().length() > 0) {
			Label description = new Label(item.getDescription(), skin.get(LabelStyle.class));
			description.setFontScale(0.75f);
			description.setWrap(true);
			description.setColor(Colors.PARALYZE);
			itemInfoTable.add(description).width(width).align(Align.left).row();
		}

		contentTable.add(itemInfoTable);
		contentTable.row();

		Label cost = new Label("Buy for " + item.cost + " Gold?", skin.get(LabelStyle.class));
		cost.setFontScale(0.75f);
		contentTable.add();
		contentTable.add(cost).colspan(2).align(Align.left).padTop(8).row();

		Table buttonTable = new Table();

		if(item.cost <= player.gold) {
			TextButton buyButton = new TextButton("Yes", skin.get(TextButtonStyle.class));
			buyButton.addListener(new ClickListener() {
				@Override
				public void clicked(InputEvent event, float x, float y) {

					// Buy the item!
					if(item.item != null) {

						// Can't buy it twice!
						items.removeValue(item, true);

						if(item.useOnBuy) {
							item.item.inventoryUse(player);
							OverlayManager.instance.clear();
						}
						else {
							Item copy = ItemManager.Copy(item.item.getClass(), item.item);
							boolean hadSpace = player.addToInventory(copy);
							if (!hadSpace) {
								// no space in the inventory, drop the item on the ground.
								dropItem(copy);
							}
						}

						// Might have an attached achievement
						if(item.achieveOnBuy != null) {
							SteamApi.api.achieve(item.achieveOnBuy);
						}
					}

					// Might be an upgrade
					if(item.upgrade != null) {
						player.stats.END += item.upgrade.stats.END;
						player.stats.ATK += item.upgrade.stats.ATK;
						player.stats.DEF += item.upgrade.stats.DEF;
						player.stats.DEX += item.upgrade.stats.DEX;
						player.stats.MAG += item.upgrade.stats.MAG;
						player.stats.SPD += item.upgrade.stats.SPD;
					}

					player.gold-=item.cost;
					Audio.playSound("ui/ui_buy.mp3", 0.6f);
					makeLayout();
				}
			});

			buttonOrder.add(buyButton);
			buttonTable.add(buyButton).padRight(4);
		}

		TextButton cancelButton = new TextButton("No", skin.get(TextButtonStyle.class));
		cancelButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				makeLayout();
				Audio.playSound("/ui/ui_button_click.mp3", 0.35f);
			}
		});
		buttonOrder.add(cancelButton);
		buttonTable.add(cancelButton);

		contentTable.add().align(Align.center);
		contentTable.add(buttonTable).align(Align.left).padTop(4);

		// Your Gold
		Label lblGoldLabel = new Label(StringManager.get("overlays.ShopPause.yourGoldText"), skin.get(LabelStyle.class));
		lblGoldLabel.setAlignment(Align.bottom | Align.left);
		lblGoldLabel.setFontScale(0.75f);

		lblGoldAmount = new Label(String.format("%d", player.gold), skin.get(LabelStyle.class));
		lblGoldAmount.setAlignment(Align.right);
		lblGoldAmount.setFontScale(0.75f);

		TextureAtlas t = TextureAtlas.cachedAtlases.get("item");

		Image goldIcon = new Image(new TextureRegionDrawable(t.getSprite(89)));
		goldIcon.setWidth(3);
		goldIcon.setHeight(3);
		goldIcon.setAlign(Align.left);

		Table goldTable = new Table();
		goldTable.add(goldIcon).width(20).height(20);
		goldTable.add(lblGoldAmount);
		goldTable.pack();

		contentTable.add(goldTable);

		makeLayout(contentTable);
	}

	@Override
	public Table makeContent() {

		int windowWidth = 200;
		
		buttonOrder.clear();
		
		final Overlay thisOverlay = this;
		
		Table contentTable = new Table();
		contentTable.columnDefaults(1).align(Align.right);

		if(titleText != null) {
			Label title = new Label(titleText, skin.get(LabelStyle.class));
			title.setColor(0.5f, 0.75f, 1f, 1);
			title.setWrap(true);
			title.setAlignment(Align.center);
			title.setFontScale(0.75f);

			contentTable.add(title).minWidth(windowWidth).colspan(2).padBottom(5f).center();
			contentTable.row();
		}

		if(descriptionText != null) {
			Label description = new Label(descriptionText, skin.get(LabelStyle.class));
			description.setFontScale(0.75f);
			description.setWrap(true);
			description.setAlignment(Align.center);

			contentTable.add(description).minWidth(windowWidth).colspan(2).padBottom(5f).center();
			contentTable.row();
		}

	    for(ShopItem item : items) {
			// Make sure that items we are selling are always identified
			if(item.item != null) item.item.identified = true;

	    	addShopItem(contentTable, itemPrefix, item);
	    }

	    if(items.size == 0) {
			final Label emptyText = new Label(StringManager.getOrDefaultTo("overlays.ShopPause.emptyText", "Sorry! Nothing for sale right now."), skin.get(LabelStyle.class));
			emptyText.setWrap(true);
			emptyText.setAlignment(Align.bottom | Align.left);
			emptyText.setFontScale(0.75f);
			emptyText.setColor(1f, 1f, 1f, 0.6f);

			contentTable.add(emptyText).width(240).colspan(2).padBottom(4);
			contentTable.row();
		}
		
	    Label lblGoldLabel = new Label(StringManager.get("overlays.ShopPause.yourGoldText"), skin.get(LabelStyle.class));
	    lblGoldLabel.setAlignment(Align.bottom | Align.left);
	    lblGoldLabel.setFontScale(0.75f);
	    //lblGoldLabel.setColor(1f, 1f, 0.5f, 0.6f);
	    
	    lblGoldAmount = new Label(String.format("%d", player.gold), skin.get(LabelStyle.class));
	    lblGoldAmount.setAlignment(Align.right);
	    lblGoldAmount.setFontScale(0.75f);
	    //lblGoldAmount.setColor(1f, 1f, 0.5f, 0.6f);

		TextureAtlas t = TextureAtlas.cachedAtlases.get("item");

		Image goldIcon = new Image(new TextureRegionDrawable(t.getSprite(89)));
		goldIcon.setWidth(3);
		goldIcon.setHeight(3);
		goldIcon.setAlign(Align.left);

		Table goldTable = new Table();
		goldTable.add(goldIcon).width(20).height(20);
		goldTable.add(lblGoldAmount);
		goldTable.pack();

		// Done
		TextButton doneBtn = new TextButton(StringManager.get("overlays.ShopPause.doneButton"), skin.get(TextButtonStyle.class));
		contentTable.add(doneBtn).align(Align.left).padTop(4);

		// Gold
		contentTable.add(goldTable).align(Align.right);
	    
	    doneBtn.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				OverlayManager.instance.remove(thisOverlay);
			}
		});
	    
	    buttonOrder.add(doneBtn);
		contentTable.pack();
	    
	    return contentTable;
	}

	// drop items around the player
	float rotOffset = 0;
	protected void dropItem(Item itm) {
		// pick a random direction to drop the item in
		float rot = player.rot + rotOffset;
		float throwPower = 0.01f;

		// next bought item will be offset more
		rotOffset += 1f;

		float projx = ( 0 * (float)Math.cos(rot) + 1 * (float)Math.sin(rot)) * 1;
		float projy = (1 * (float)Math.cos(rot) - 0 * (float)Math.sin(rot)) * 1;

		itm.isActive = true;
		itm.isDynamic = true;
		itm.x = (player.x + projx * 0.2f);
		itm.y = (player.y + projy * 0.2f);
		itm.z = player.z + 0.4f;
		itm.xa = projx * (throwPower * 0.3f);
		itm.ya = projy * (throwPower * 0.3f);
		itm.za = throwPower * 0.05f;
		itm.ignorePlayerCollision = true;

		Game.instance.level.SpawnEntity(itm);

		itm.x = (player.x + projx * 0.5f);
		itm.y = (player.y + projy * 0.5f);
	}
	
	protected void addShopItem(Table table, String prefix, final ShopItem item) {
		
		final Label itemName = new Label(item.getName().replace(prefix, ""), skin.get(LabelStyle.class));
		final Integer cost = item.cost;
		final String costText = cost + "";

		// Icon
		String iconAtlas = item.item.spriteAtlas != null ? item.item.spriteAtlas : "item";

		TextureAtlas t = TextureAtlas.cachedAtlases.get(iconAtlas);

		final Image itemIcon = new Image(new TextureRegionDrawable(t.getSprite(item.item.tex)));
		itemIcon.setWidth(6);
		itemIcon.setHeight(6);

		// Name
		itemName.setWrap(true);
		itemName.setAlignment(Align.bottom | Align.left);
		itemName.setFontScale(0.75f);
		itemName.setColor(1f, 1f, 1f, 0.6f);
		
		final Label value = new Label(costText, skin.get(LabelStyle.class));
		value.setAlignment(Align.right);
		value.setFontScale(0.75f);
		
		if(player.gold >= item.cost)
			value.setColor(0.6f, 1f, 0.6f, 0.6f);
		else
			value.setColor(1f, 0.6f, 0.6f, 0.6f);

		final Table itemTable = new Table();
		
		itemName.addListener(new ClickListener() {
			
			@Override
			public void clicked(InputEvent event, float x, float y) {
				makeBuyModal(item, itemIcon);
				Audio.playSound("/ui/ui_button_click.mp3", 0.35f);
			}
			
			@Override
			public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
				itemName.setColor(UiSkin.getSkin().get("inputover", LabelStyle.class).fontColor);
				value.setColor(value.getColor().r, value.getColor().g, value.getColor().b, 1f);
			}
			
			@Override
			public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
				if(selected.contains(item, true)) {
					itemName.setColor(UiSkin.getSkin().get("input", LabelStyle.class).fontColor);
					value.setColor(value.getColor().r, value.getColor().g, value.getColor().b, 0.9f);
				}
				else {
					itemName.setColor(UiSkin.getSkin().get("input", LabelStyle.class).fontColor);
					value.setColor(value.getColor().r, value.getColor().g, value.getColor().b, 0.6f);
				}
			}
		});

		itemTable.add(itemIcon).width(12).height(12).padRight(4).align(Align.center);
		itemTable.add(itemName).width(180).align(Align.left);
		itemTable.pack();
		
    	table.add(itemTable).align(Align.left);
		table.add(value).align(Align.right).padLeft(1);
		
		table.row();
		
		buttonOrder.add(itemName);
	}
}
