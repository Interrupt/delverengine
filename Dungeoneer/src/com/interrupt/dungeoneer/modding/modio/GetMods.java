package com.interrupt.dungeoneer.modding.modio;

import com.badlogic.gdx.utils.Array;

public class GetMods {
    /** Array containing mod objects. */
    public Array<ModObject> data;

    /** Number of results returned in this request. */
    public int result_count;

    /** Number of results skipped over. Defaults to 0 unless overridden by _offset filter. */
    public int result_offset;

    /** Maximum number of results returned in the request. Defaults to 100 (max) unless overridden by _limit filter. */
    public int result_limit;

    /** Total number of results found. */
    public int result_total;
}
