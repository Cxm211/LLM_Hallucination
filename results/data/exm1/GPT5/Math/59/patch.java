public static float max(final float a, final float b) {
        // If either is NaN, return NaN (match Math.max behavior)
        if (Float.isNaN(a)) {
            return a;
        }
        if (Float.isNaN(b)) {
            return b;
        }
        // Regular comparisons
        if (a > b) {
            return a;
        }
        if (a < b) {
            return b;
        }
        // a and b are equal. Handle signed zero per Math.max spec: +0.0 if either is +0.0
        if (a == 0.0f && b == 0.0f) {
            return (Float.floatToRawIntBits(a) == 0 || Float.floatToRawIntBits(b) == 0) ? 0.0f : -0.0f;
        }
        // Equal non-zero (or infinities): return either
        return a;
    }