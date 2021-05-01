package com.interrupt.dungeoneer.net;

public class DownloadProgress {
    /** Number of bytes received. */
    public long bytes;

    /** Total number of bytes for download. */
    public long totalBytes;

    public DownloadProgress(long bytes, long totalBytes) {
        this.bytes = bytes;
        this.totalBytes = totalBytes;
    }
}
