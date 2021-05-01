package com.interrupt.dungeoneer.modding.modio;

public class ModfileObject {
    /** Unique modfile id. */
    public int id;

    /** Unique mod id. */
    public int mod_id;

    /** Unix timestamp of date file was added. */
    public int date_added;

    /** Unix timestamp of date file was virus scanned. */
    public int date_scanned;

    /** Current virus scan status of the file. For newly added files that have yet to be scanned this
     * field will change frequently until a scan is complete:
     * 0 = Not scanned
     * 1 = Scan complete
     * 2 = In progress
     * 3 = Too large to scan
     * 4 = File not found
     * 5 = Error Scanning
     */
    public int virus_status;

    /** Was a virus detected:
     * 0 = No threats detected
     * 1 = Flagged as malicious
     */
    public int virus_positive;

    /** VirusTotal proprietary hash to view the scan results. */
    public String virustotal_hash;

    /** Size of the file in bytes. */
    public int filesize;

    /** Contains filehash data. */
    public FilehashObject filehash;

    /** Filename including extension. */
    public String filename;

    /** Release version this file represents. */
    public String version;

    /** Changelog for the file. */
    public String changelog;

    /** Metadata stored by the game developer for this file. */
    public String metadata_blob;

    /** Contains download data. */
    public DownloadObject download;
}
