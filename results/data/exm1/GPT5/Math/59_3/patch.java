public static float max(final float a, final float b) {
        // If either is NaN, return NaN
        if (Float.isNaN(a) || Float.isNaN(b)) {
            return Float.NaN;
        }
        // Standard comparison first
        if (a > b) {
            return a;
        }
        if (a < b) {
            return b;
        }
        // a and b are numerically equal. Handle signed zeros to match Math.max behavior:
        // return +0.0f if one is +0.0f and the other is -0.0f; otherwise return the value (including -0.0f).
        if (a == 0.0f) {
            int ia = Float.floatToRawIntBits(a);
            int ib = Float.floatToRawIntBits(b);
            // If bits equal, return a (covers both +0.0 and -0.0 same sign)
            if (ia == ib) {
                return a;
            }
            // Different signed zeros: return +0.0f
            return 0.0f;
        }
        return a; // equal non-zero values
    }