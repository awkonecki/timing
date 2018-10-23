package com.nebo.timing.util;

import com.github.mikephil.charting.utils.ColorTemplate;

public class ActivityTimerUtils {
    public static int[] getColors(int count) {
        // have as many colors as stack-values per entry
        int[] colors = new int[count];

        for (int i = 0; i < colors.length && i < ColorTemplate.MATERIAL_COLORS.length; i++) {
            colors[i] = ColorTemplate.MATERIAL_COLORS[i];
        }

        return colors;
    }
}
