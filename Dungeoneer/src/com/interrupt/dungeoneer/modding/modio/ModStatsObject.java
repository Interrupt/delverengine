package com.interrupt.dungeoneer.modding.modio;

public class ModStatsObject {
    /** Unique mod id. */
    public int mod_id;

    /** Current rank of the mod. */
    public int popularity_rank_position;

    /** Number of ranking spots the current rank is measured against. */
    public int popularity_rank_total_mods;

    /** Number of total mod downloads. Count resets around 11:00 UTC+11 daily. */
    public int downloads_today;

    /** Number of total mod downloads. */
    public int downloads_total;

    /** Number of total users who have subscribed to the mod. */
    public int subscribers_total;

    /** Number of times this mod has been rated. */
    public int ratings_total;

    /** Number of positive ratings. */
    public int ratings_positive;

    /** Number of negative ratings. */
    public int ratings_negative;

    /** Number of positive ratings, divided by the total ratings to determine it’s percentage score. */
    public int ratings_percentage_positive;

    /** Overall rating of this item calculated using the Wilson score confidence interval.
     * This column is good to sort on, as it will order items based on number of ratings
     * and will place items with many positive ratings above those with a higher score
     * but fewer ratings. */
    public float ratings_weighted_aggregate;

    /** Textual representation of the rating in format:
     * - Overwhelmingly Positive
     * - Very Positive
     * - Positive
     * - Mostly Positive
     * - Mixed
     * - Negative
     * - Mostly Negative
     * - Very Negative
     * - Overwhelmingly Negative
     * - Unrated */
    public String ratings_display_text;

    /** Unix timestamp until this mods's statistics are considered stale. */
    public int date_expires;
}
