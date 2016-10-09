package com.monsoonblessing.moments.enums;

/**
 * Created by Kevin on 2016-06-19.
 */
public enum SortingOptions {
    ORDER_ADDED("Order added"),
    RECENT_TO_OLD_DATE("Recent to old"),
    OLD_TO_RECENT_DATE("Old to recent");

    private final String description;


    SortingOptions(String s) {
        description = s;
    }


    //when user chooses a sort choice from dropdown filter menu, we want to grab the enum value
    public static SortingOptions getEnum(String enumString) {
        switch (enumString) {
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
        return description;
    }

}
