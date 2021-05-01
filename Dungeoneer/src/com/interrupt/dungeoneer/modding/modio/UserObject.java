package com.interrupt.dungeoneer.modding.modio;

public class UserObject {
    /** Unique id of the user. */
    public int id;

    /** Path for the user on mod.io. For example: https://mod.io/members/name-id-here */
    public String name_id;

    /** Username of the user. */
    public String username;

    /** Unix timestamp of date the user was last online. */
    public int date_online;

    /** Contains avatar data. */
    public AvatarObject avatar;

    /** This field is no longer used and will return an empty string. */
    public String timezone;

    /** This field is no longer used and will return an empty string. To localize the API response we recommend you set the Accept-Language header. */
    public String language;

    /** URL to the user's mod.io profile. */
    public String profile_url;
}
