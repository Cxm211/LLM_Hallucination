    protected RealPointValuePair getSolution() {
      double[] coefficients = new double[getOriginalNumDecisionVariables()];
      Integer negativeVarBasicRow = getBasicRow(getNegativeDecisionVariableOffset());
      double mostNegative = negativeVarBasicRow == null ? 0 : getEntry(negativeVarBasicRow, getRhsOffset());
      // Use row-based approach to find basic variables
      for (int i = getNumObjectiveFunctions(); i < getHeight(); i++) {
          // Find the column where this row has a 1 (i.e., the basic variable for this row)
          int basicCol = -1;
          for (int j = 0; j < getWidth(); j++) {
              if (j == getRhsOffset()) {
                  continue;
              }
              if (MathUtils.equals(getEntry(i, j), 1.0, epsilon) && basicCol == -1) {
                  basicCol = j;
              } else if (!MathUtils.equals(getEntry(i, j), 0.0, epsilon)) {
                  basicCol = -1;
                  break;
              }
          }
          if (basicCol != -1) {
              // Determine if this column corresponds to a decision variable
              int decisionVarIndex = basicCol - getNumObjectiveFunctions();
              if (decisionVarIndex >= 0 && decisionVarIndex < coefficients.length) {
                  // Avoid setting the same decision variable from multiple rows (should not happen)
                  coefficients[decisionVarIndex] = getEntry(i, getRhsOffset());
              }
          }
      }
      // Apply shift due to negative variable (if variables are unrestricted)
      if (!restrictToNonNegative) {
          for (int i = 0; i < coefficients.length; i++) {
              coefficients[i] -= mostNegative;
          }
      }
      return new RealPointValuePair(coefficients, f.getValue(coefficients));
    }