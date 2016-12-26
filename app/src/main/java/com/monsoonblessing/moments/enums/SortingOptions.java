package com.monsoonblessing.moments.enums;

import io.realm.Sort;

/**
 * Created by Kevin on 2016-06-19.
 */
public enum SortingOptions {
    ORDER_ADDED("Order added", "id", Sort.DESCENDING),
    RECENT_TO_OLD_DATE("Recent to old", "date", Sort.DESCENDING),
    OLD_TO_RECENT_DATE("Old to recent", "date", Sort.ASCENDING);

    private final String desc;
    private final String col;
    private final Sort sort;


    SortingOptions(String desc, String col, Sort sort) {
        this.desc = desc;
        this.col = col;
        this.sort = sort;
    }


    //when user chooses a sort choice from dropdown filter menu, we want to grab the enum value
    public static SortingOptions getEnum(String sortDescription) {
        switch (sortDescription) {
            case "Order added":
                return SortingOptions.ORDER_ADDED;
            case "Recent to old":
                return SortingOptions.RECENT_TO_OLD_DATE;
            case "Old to recent":
                return SortingOptions.OLD_TO_RECENT_DATE;
        }
        return null;
    }


    public String getDescription() {
        return this.desc;
    }


    public String getCol() {
        return col;
    }


    public Sort getSort() {
        return sort;
    }
}
