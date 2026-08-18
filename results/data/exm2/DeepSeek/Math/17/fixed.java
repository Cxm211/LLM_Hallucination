// ===== FIXED org.apache.commons.math3.dfp.Dfp :: multiply(int) [lines 1602-1608] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Math/Math-17-fixed/src/main/java/org/apache/commons/math3/dfp/Dfp.java =====
    public Dfp multiply(final int x) {
        if (x >= 0 && x < RADIX) {
            return multiplyFast(x);
        } else {
            return multiply(newInstance(x));
        }
    }
