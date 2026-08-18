public static float max(final float a, final float b) {
        if (a > b) {
            return a;
        }
        if (a < b) {
            return b;
        }
        if (a != b) { // one is NaN
            return Float.NaN;
        }
        // a and b are numerically equal (handles signed zeros)
        return a == 0.0f ? 0.0f : a;
    }