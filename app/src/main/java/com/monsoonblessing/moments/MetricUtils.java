package com.monsoonblessing.moments;

import android.content.res.Resources;

/**
 * Created by Kevin Faust on 12/8/2016.
 */

public class MetricUtils {

    public static int dpToPx(int dp)
    {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

}
