public static float max(final float a, final float b) {
        // Match Math.max semantics for floats, including NaN and signed zeros
        if (a != a) {
            return a; // NaN
        }
        if (b != b) {
            return b; // NaN
        }
        if (a == 0.0f && b == 0.0f) {
            // If both are zeros, return +0.0f unless both are -0.0f
            int sa = Float.floatToRawIntBits(a) & 0x80000000;
            int sb = Float.floatToRawIntBits(b) & 0x80000000;
            if (sa != 0 && sb != 0) {
                return a; // both -0.0f, return -0.0f
            }
            return 0.0f; // at least one +0.0f, return +0.0f
        }
        return (a > b) ? a : b;
    }