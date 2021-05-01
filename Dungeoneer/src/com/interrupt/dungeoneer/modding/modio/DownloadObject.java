package com.interrupt.dungeoneer.modding.modio;

public class DownloadObject {
    /** URL to download the file from the mod.io CDN.
     * NOTE: If the game requires mod downloads to be initiated via the API, the binary_url
     * returned will contain a verification hash. This hash must be supplied to get the
     * modfile, and will expire after a certain period of time. Saving and reusing the
     * binary_url won't work in this situation given it's dynamic nature.
     */
    public String binary_url;

    /** Unix timestamp of when the binary_url will expire. */
    public int date_expires;
}
