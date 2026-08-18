// ===== FIXED org.apache.commons.math.optimization.linear.SimplexTableau :: getBasicRow(int) [lines 272-282] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Math/Math-87-fixed/src/java/org/apache/commons/math/optimization/linear/SimplexTableau.java =====
    private Integer getBasicRow(final int col) {
        Integer row = null;
        for (int i = getNumObjectiveFunctions(); i < getHeight(); i++) {
            if (MathUtils.equals(getEntry(i, col), 1.0, epsilon) && (row == null)) {
                row = i;
            } else if (!MathUtils.equals(getEntry(i, col), 0.0, epsilon)) {
                return null;
            }
        }
        return row;
    }
