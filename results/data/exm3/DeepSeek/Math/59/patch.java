    public static float max(final float a, final float b) {
        if (Float.isNaN(a) || Float.isNaN(b)) {
            return Float.NaN;
        }
        if (a > b) {
            return a;
        }
        if (a < b) {
            return b;
        }
        // a and b are numerically equal
        // handle signed zeros
        return (Float.compare(a, b) >= 0) ? a : b;
    }