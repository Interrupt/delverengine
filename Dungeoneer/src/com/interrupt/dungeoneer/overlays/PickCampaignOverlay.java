package com.interrupt.dungeoneer.overlays;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.interrupt.dungeoneer.GameApplication;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.game.GameCampaign;
import com.interrupt.dungeoneer.screens.OverlayWrapperScreen;
import com.interrupt.managers.StringManager;

public class PickCampaignOverlay extends WindowOverlay {

	public PickCampaignOverlay() { }

	@Override
	public  void tick(float delta) {
		super.tick(delta);
	}

	@Override
	public void onShow() {
		super.onShow();

		final Overlay thisOverlay = this;
		InputListener listener = new InputListener() {
			@Override
			public boolean keyDown(InputEvent event, int keycode) {
				if(keycode == Keys.ESCAPE || keycode == Keys.BACK) {
					OverlayManager.instance.remove(thisOverlay);
				}
				return false;
			}
		};

		ui.addListener(listener);
	}

	@Override
	public void onHide() {
		super.onHide();
	}

	@Override
	public Table makeContent() {

		final Overlay thisOverlay = this;

		TextButton backBtn = new TextButton(" " + StringManager.get("overlays.ModsOverlay.backButton") + " ", skin.get(TextButtonStyle.class));
		backBtn.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				OverlayManager.instance.remove(thisOverlay);
			}
		});

		Table contentTable = new Table();

	    Label headerText = new Label("Pick a Campaign",skin.get(LabelStyle.class));
	    headerText.setAlignment(Align.center);
	    contentTable.add(headerText).align(Align.center).width(200).padBottom(4).expand();
	    contentTable.row();

		Table innerTable = new Table();
		innerTable.align(Align.top);

		ScrollPane scrollPane = new ScrollPane(innerTable);

		if(Game.modManager != null) {
			Array<GameCampaign> allCampaigns = Game.modManager.getFoundCampaigns();
            ArrayMap<String, String> campaignNamesById = new ArrayMap<>();

            // Remove duplicate campaign IDs
            ArrayMap<String, GameCampaign> filteredCampaigns = new ArrayMap<>();
            for(GameCampaign campaign : allCampaigns) {
                filteredCampaigns.put(campaign.campaignId, campaign);

                // Also keep track of the campaign name for this campaign, if any was given
                if(campaign.displayName != null && !campaign.displayName.isEmpty())
                    campaignNamesById.put(campaign.campaignId, campaign.displayName);
            }

			for(GameCampaign campaign : filteredCampaigns.values()) {

				String campaignName = campaignNamesById.get(campaign.campaignId, campaign.campaignId);

				int maxNameLength = 40;
				if(campaignName.length() > maxNameLength)
					campaignName = campaignName.substring(0, maxNameLength - 3) + "...";

                final Label campaignBtn = new Label(campaignName, skin.get("inputover", LabelStyle.class));
                campaignBtn.setWidth(200);
                campaignBtn.setHeight(50);

                campaignBtn.addListener(new ClickListener() {
					@Override
					public void clicked(InputEvent event, float x, float y) {
                        close();
                        GameApplication.SetScreen(new OverlayWrapperScreen(new SelectSaveSlotOverlay()));
					}
				});

				innerTable.add(campaignBtn).align(Align.top).align(Align.left).fillX().expand();

				buttonOrder.add(campaignBtn);
				innerTable.row();
			}
		}

		contentTable.add(scrollPane).fillX().expand().maxHeight(135).row();

	    contentTable.add(backBtn).align(Align.left).expand().padTop(8);

	    ui.setScrollFocus(scrollPane);

	    buttonOrder.clear();
	    buttonOrder.add(backBtn);

		return contentTable;
	}
}
