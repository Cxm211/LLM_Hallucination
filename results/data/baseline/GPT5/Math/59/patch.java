public static float max(final float a, final float b) {
        if (Float.isNaN(a) || Float.isNaN(b)) {
            return Float.NaN;
        }
        if (a == 0.0f && b == 0.0f) {
            return 0.0f;
        }
        return (a >= b) ? a : b;
    }