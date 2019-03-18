package com.syouth.telegram_charts.math;

public class Common {
    public static long[] findMaxMin(long[] vals) {
        long max = Long.MIN_VALUE;
        long min = Long.MAX_VALUE;

        for (long v : vals) {
            if (v < min) {
                min = v;
            }
            if (v > max) {
                max = v;
            }
        }

        return new long[] {max, min};
    }
}
