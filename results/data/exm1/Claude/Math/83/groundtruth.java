// ===== FIXED org.apache.commons.math.optimization.linear.SimplexTableau :: getBasicRow(int, boolean) [lines 290-301] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Math/Math-83-fixed/src/main/java/org/apache/commons/math/optimization/linear/SimplexTableau.java =====
    private Integer getBasicRow(final int col, boolean ignoreObjectiveRows) {
        Integer row = null;
        int start = ignoreObjectiveRows ? getNumObjectiveFunctions() : 0;
        for (int i = start; i < getHeight(); i++) {
            if (MathUtils.equals(getEntry(i, col), 1.0, epsilon) && (row == null)) {
                row = i;
            } else if (!MathUtils.equals(getEntry(i, col), 0.0, epsilon)) {
                return null;
            }
        }
        return row;
    }

// ===== FIXED org.apache.commons.math.optimization.linear.SimplexTableau :: getSolution() [lines 339-358] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Math/Math-83-fixed/src/main/java/org/apache/commons/math/optimization/linear/SimplexTableau.java =====
    protected RealPointValuePair getSolution() {
      double[] coefficients = new double[getOriginalNumDecisionVariables()];
      Integer negativeVarBasicRow = getBasicRowForSolution(getNegativeDecisionVariableOffset());
      double mostNegative = negativeVarBasicRow == null ? 0 : getEntry(negativeVarBasicRow, getRhsOffset());
      Set<Integer> basicRows = new HashSet<Integer>();
      for (int i = 0; i < coefficients.length; i++) {
          Integer basicRow = getBasicRowForSolution(getNumObjectiveFunctions() + i);
          if (basicRows.contains(basicRow)) {
              // if multiple variables can take a given value 
              // then we choose the first and set the rest equal to 0
              coefficients[i] = 0;
          } else {
              basicRows.add(basicRow);
              coefficients[i] =
                  (basicRow == null ? 0 : getEntry(basicRow, getRhsOffset())) -
                  (restrictToNonNegative ? 0 : mostNegative);
          }
      }
        return new RealPointValuePair(coefficients, f.getValue(coefficients));
    }
