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
        // a and b are equal (could be 0.0f and -0.0f). Return +0.0f in case of signed zero.
        if (a == 0.0f && b == 0.0f) {
            return 0.0f;
        }
        return a;
    }