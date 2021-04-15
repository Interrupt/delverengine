package com.interrupt.dungeoneer.modding.modio;

import com.badlogic.gdx.utils.Array;

public class ModObject {
    /** Unique mod id. */
    public int id;

    /** unique game id. */
    public int game_id;

    /** Status of the mod (see status and visibility for details):
     * 0 = Not Accepted
     * 1 = Accepted
     * 3 = Deleted */
    public int status;

    /** Visibility of the mod (see status and visibility for details):
     * 0 = Hidden
     * 1 = Public */
    public int visible;

    /** Contains user data. */
    public UserObject submitted_by;

    /** Unix timestamp of date mod was registered. */
    public int date_added;

    /** Unix timestamp of date mod was updated. */
    public int date_updated;

    /** Unix timestamp of date mod was set live. */
    public int date_live;

    /** Maturity options flagged by the mod developer, this is only relevant if the parent game allows mods to be labelled as mature.
     * 0 = None set (default)
     * 1 = Alcohol
     * 2 = Drugs
     * 4 = Violence
     * 8 = Explicit
     * ? = Add the options you want together, to enable multiple filters (see BITWISE fields) */
    public int maturity_option;

    /** Contains logo data. */
    public LogoObject logo;

    /** Official homepage of the mod. */
    public String homepage_url;

    /** Name of the mod. */
    public String name;

    /** Path for the mod on mod.io. For example: https://rogue-knight.mod.io/mod-name-id-here */
    public String name_id;

    /** Summary of the mod. */
    public String summary;

    /** Detailed description of the mod which allows HTML. */
    public String description;

    /** description field converted into plaintext. */
    public String description_plaintext;

    /** Metadata stored by the game developer. Metadata can also be stored as searchable key value pairs, and to individual mod files. */
    public String metadata_blob;

    /** URL to the mod's mod.io profile. */
    public String profile_url;

    /** Contains mod media data. */
    public ModMediaObject media;

    /** Contains modfile data. */
    public ModfileObject modfile;

    /** Contains stats data. */
    public ModStatsObject stats;

    /** Contains key-value metadata. */
    public Array<MetadataKVPObject> metadata_kvp;

    /** Contains mod tag data. */
    public Array<ModTagObject> tags;
}
