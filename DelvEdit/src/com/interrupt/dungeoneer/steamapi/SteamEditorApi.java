package com.interrupt.dungeoneer.steamapi;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.codedisaster.steamworks.*;
import com.interrupt.api.steam.SteamApiInterface;
import com.interrupt.api.steam.workshop.WorkshopModData;
import com.interrupt.dungeoneer.editor.ui.UploadModDialog;

import java.util.Timer;
import java.util.TimerTask;

public class SteamEditorApi implements SteamApiInterface {

    public static UploadModDialog uploadModDialog = null;
    boolean started = false;

    private UgcUploadCallbacks uploadModCallbacks = null;

    final public int STEAM_APP_ID = 249630;

    protected SteamUGCCallback nullUgcCallback = new SteamUGCCallback() {
        @Override
        public void onUGCQueryCompleted(SteamUGCQuery query, int numResultsReturned, int totalMatchingResults, boolean isCachedData, SteamResult result) { }

        @Override
        public void onSubscribeItem(SteamPublishedFileID publishedFileID, SteamResult result) { }

        @Override
        public void onUnsubscribeItem(SteamPublishedFileID publishedFileID, SteamResult result) { }

        @Override
        public void onRequestUGCDetails(SteamUGCDetails details, SteamResult result) { }

        @Override
        public void onCreateItem(SteamPublishedFileID publishedFileID, boolean needsToAcceptWLA, SteamResult result) { }

        @Override
        public void onSubmitItemUpdate(boolean needsToAcceptWLA, SteamResult result) { }

        @Override
        public void onDownloadItemResult(int appID, SteamPublishedFileID publishedFileID, SteamResult result) { }

        @Override
        public void onUserFavoriteItemsListChanged(SteamPublishedFileID publishedFileID, boolean wasAddRequest, SteamResult result) { }

        @Override
        public void onSetUserItemVote(SteamPublishedFileID publishedFileID, boolean voteUp, SteamResult result) { }

        @Override
        public void onGetUserItemVote(SteamPublishedFileID publishedFileID, boolean votedUp, boolean votedDown, boolean voteSkipped, SteamResult result) { }

        @Override
        public void onStartPlaytimeTracking(SteamResult result) { }

        @Override
        public void onStopPlaytimeTracking(SteamResult result) { }

        @Override
        public void onStopPlaytimeTrackingForAllItems(SteamResult result) { }
    };

    protected SteamUserStatsCallback userStatsCallback = new SteamUserStatsCallback() {
        @Override
        public void onUserStatsReceived(long gameId, SteamID steamIDUser, SteamResult result) { }

        @Override
        public void onUserStatsStored(long gameId, SteamResult result) { }

        @Override
        public void onUserStatsUnloaded(SteamID steamIDUser) { }

        @Override
        public void onUserAchievementStored(long gameId, boolean isGroupAchievement, String achievementName, int curProgress, int maxProgress) { }

        @Override
        public void onLeaderboardFindResult(SteamLeaderboardHandle leaderboard, boolean found) { }

        @Override
        public void onLeaderboardScoresDownloaded(SteamLeaderboardHandle leaderboard, SteamLeaderboardEntriesHandle entries, int numEntries) { }

        @Override
        public void onLeaderboardScoreUploaded(boolean success, SteamLeaderboardHandle leaderboard, int score, boolean scoreChanged, int globalRankNew, int globalRankPrevious) { }

        @Override
        public void onGlobalStatsReceived(long gameId, SteamResult result) { }
    };

    protected SteamUserStats stats = null;
    protected SteamUGC ugc;

    @Override
    public boolean init() {
        try {
            Gdx.app.error("SteamApi", "Starting Steam API");
            started = SteamAPI.init();

            if(started) {
                stats = new SteamUserStats(userStatsCallback);
                stats.requestCurrentStats();
            }

            if(!started) {
                Gdx.app.error("SteamApi", "Could not start Steam API");
            }

            return started;
        } catch (SteamException e) {
            // Error extracting or loading native libraries
            Gdx.app.error("SteamApi", "Error starting Steam API", e);
        }
        return false;
    }

    @Override
    public Array<String> getWorkshopFolders() {
        return new Array<String>();
    }

    @Override
    public void runCallbacks() {
        if(SteamAPI.isSteamRunning()) {
            SteamAPI.runCallbacks();
        }
    }

    @Override
    public void achieve(String achievementName) {
        Gdx.app.log("SteamApi", "Achievement unlocked! " + achievementName);
    }

    @Override
    public void achieve(String achievementName, int numProgress, int maxProgress) {
        if(SteamAPI.isSteamRunning() && stats != null) {
            if(stats.indicateAchievementProgress(achievementName, 1, 1)) {
                if(stats.storeStats())
                    Gdx.app.log("SteamApi", "Updated achievement: " + achievementName);
                else
                    Gdx.app.log("SteamApi", "Could not store Steam stats");
            }
            else {
                Gdx.app.log("SteamApi", "Could not set achievement: " + achievementName);
            }
        }
    }

    @Override
    public void dispose() {
        if(SteamAPI.isSteamRunning()) {
            SteamAPI.shutdown();
        }
    }

    @Override
    public void uploadToWorkshop(Long workshopId, String modImagePath, String modTitle, String modFolderPath) {
        if(SteamAPI.isSteamRunning() && started) {
            uploadModCallbacks = new UgcUploadCallbacks(modImagePath, modTitle, null, modFolderPath, STEAM_APP_ID, uploadModDialog);
            ugc = new SteamUGC(uploadModCallbacks);

            if(workshopId == null) {
                uploadModDialog.setUploadStatus("Creating New Workshop Item... ");
                ugc.createItem(STEAM_APP_ID, SteamRemoteStorage.WorkshopFileType.Community);
            }
            else {
                doModUpload(new SteamPublishedFileID(workshopId), modTitle, null, modImagePath, modFolderPath, "Mod Changed!");
            }
        }
    }

    @Override
    public boolean isAvailable() {
        return started;
    }

    private void packageMod(final long workshopId, final String title, final String path, final String imagePath) {
        // Package the preview image into the mod folder
        FileHandle image = new FileHandle(imagePath);
        FileHandle imageCopy = new FileHandle(path).child("modPreview." + image.extension());

        if(image.exists() && !image.equals(imageCopy)) {
            image.copyTo(imageCopy);
        }

        // Package the metadata into the mod
        WorkshopModData data = new WorkshopModData(workshopId, title, imageCopy.name());

        Json json = new Json();
        json.toJson(data, new FileHandle(path).child("modInfo.json"));
    }

    public void doModUpload(SteamPublishedFileID publishedFileID, String modTitle, String modDescription, String modImagePath, String modFolderPath, String changeNote) {

        // save all the mod data
        packageMod(publishedFileID.getNativeHandle(), modTitle, modFolderPath, modImagePath);

        // Start the update!
        SteamUGCUpdateHandle h = ugc.startItemUpdate(STEAM_APP_ID, publishedFileID);
        ugc.setItemTitle(h, modTitle);
        //ugc.setItemDescription(h, modDescription);
        ugc.setItemPreview(h, modImagePath);
        ugc.setItemContent(h, modFolderPath);
        //ugc.setItemVisibility(h, SteamRemoteStorage.PublishedFileVisibility.Private);
        //ugc.setItemTags(h, new String[]{});
        ugc.submitItemUpdate(h, changeNote);

        Gdx.app.log("SteamAPI", "Starting Workshop Update.");

        if(uploadModDialog != null)
            startCheckingUpdateStatus(ugc, h);
    }

    public void startCheckingUpdateStatus(final SteamUGC ugc, final SteamUGCUpdateHandle uh) {
        final SteamUGC.ItemUpdateInfo info = new SteamUGC.ItemUpdateInfo();
        final Timer updateTimer = new Timer();
        TimerTask checkStatusTask = new TimerTask() {
            public void run() {
                SteamUGC.ItemUpdateStatus status = ugc.getItemUpdateProgress(uh, info);
                String uploadStatusText = "Upload progress: ";

                if(info.getBytesTotal() != 0) {
                    float uploadPercentage = (info.getBytesProcessed() / (float)info.getBytesTotal()) * 100;
                    uploadStatusText += String.format("%.2f", uploadPercentage) + "%";
                }
                else {
                    uploadStatusText += status;
                }

                if(status != SteamUGC.ItemUpdateStatus.Invalid) {
                    uploadModDialog.setUploadStatus(uploadStatusText);
                }
                else {
                    updateTimer.cancel();
                }
            }
        };

        uploadModDialog.setUploadStatus("Starting Upload...");
        updateTimer.scheduleAtFixedRate(checkStatusTask, 400, 20);
    }
}
