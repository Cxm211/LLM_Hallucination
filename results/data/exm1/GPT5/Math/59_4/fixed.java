// ===== FIXED org.apache.commons.math.util.FastMath :: max(float, float) [lines 3481-3483] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Math/Math-59-fixed/src/main/java/org/apache/commons/math/util/FastMath.java =====
    public static float max(final float a, final float b) {
        return (a <= b) ? b : (Float.isNaN(a + b) ? Float.NaN : a);
    }
