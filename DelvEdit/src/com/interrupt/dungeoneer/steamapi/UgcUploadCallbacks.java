package com.interrupt.dungeoneer.steamapi;

import com.badlogic.gdx.Gdx;
import com.codedisaster.steamworks.*;
import com.interrupt.api.steam.SteamApi;
import com.interrupt.dungeoneer.editor.ui.UploadModDialog;

public class UgcUploadCallbacks implements SteamUGCCallback {

    String modImagePath;
    String modTitle;
    String modDescription;
    String modFolderPath;
    int appId;

    // callback related properties
    SteamUGCUpdateHandle updateHandle = null;

    // update a progress bar?
    UploadModDialog uploadDialog = null;

    public UgcUploadCallbacks(String modImagePath, String modTitle, String modDescription, String modFolderPath, int appId, UploadModDialog uploadDialog) {
        this.modImagePath = modImagePath;
        this.modTitle = modTitle;
        this.modDescription = modDescription;
        this.modFolderPath = modFolderPath;
        this.appId = appId;
        this.uploadDialog = uploadDialog;
    }

    @Override
    public void onUGCQueryCompleted(SteamUGCQuery query, int numResultsReturned, int totalMatchingResults, boolean isCachedData, SteamResult result) {
        Gdx.app.log("SteamAPI", "On UGC Query Completed");
    }

    @Override
    public void onSubscribeItem(SteamPublishedFileID publishedFileID, SteamResult result) {

    }

    @Override
    public void onUnsubscribeItem(SteamPublishedFileID publishedFileID, SteamResult result) {

    }

    @Override
    public void onRequestUGCDetails(SteamUGCDetails details, SteamResult result) {
        Gdx.app.log("SteamAPI", "On Request UGC Details");
    }

    @Override
    public void onCreateItem(SteamPublishedFileID publishedFileID, boolean needsToAcceptWLA, SteamResult result) {
        uploadDialog.setUploadStatus("Creating New Workshop Item... " + result);
        Gdx.app.log("SteamAPI", "Created Workshop Item: " + result);

        uploadDialog.setWorkshopId(publishedFileID.getNativeHandle());

        if(!needsToAcceptWLA) {
            SteamEditorApi api = (SteamEditorApi)SteamApi.api;
            api.doModUpload(publishedFileID, modTitle, modDescription, modImagePath, modFolderPath, "Initial Mod Upload");
        }
        else {
            uploadDialog.setUploadStatus("User needs to accept the Steam Workshop License Agreement");
            Gdx.app.log("SteamAPI", "User needs to accept the Steam Workshop License Agreement before uploading!");
        }
    }

    @Override
    public void onSubmitItemUpdate(boolean needsToAcceptWLA, SteamResult result) {
        Gdx.app.log("DelvEdit", "Workshop upload finished! " + result);

        if(result == SteamResult.OK) {
            uploadDialog.setUploadStatus("Upload Finished! Check in the Workshop in a few minutes.");
            uploadDialog.makeDoneContent();
        }
        else {
            uploadDialog.setUploadStatus("Something bad happened! Result:" + result);
        }
    }

    @Override
    public void onDownloadItemResult(int appID, SteamPublishedFileID publishedFileID, SteamResult result) {

    }

    @Override
    public void onUserFavoriteItemsListChanged(SteamPublishedFileID publishedFileID, boolean wasAddRequest, SteamResult result) {

    }

    @Override
    public void onSetUserItemVote(SteamPublishedFileID publishedFileID, boolean voteUp, SteamResult result) {

    }

    @Override
    public void onGetUserItemVote(SteamPublishedFileID publishedFileID, boolean votedUp, boolean votedDown, boolean voteSkipped, SteamResult result) {

    }

    @Override
    public void onStartPlaytimeTracking(SteamResult result) {

    }

    @Override
    public void onStopPlaytimeTracking(SteamResult result) {

    }

    @Override
    public void onStopPlaytimeTrackingForAllItems(SteamResult result) {

    }
}
