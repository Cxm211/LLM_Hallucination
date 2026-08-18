protected RealPointValuePair getSolution() {
      double[] coefficients = new double[getOriginalNumDecisionVariables()];
      Integer negativeVarBasicRow = getBasicRow(getNegativeDecisionVariableOffset(), true);
      double mostNegative = negativeVarBasicRow == null ? 0 : getEntry(negativeVarBasicRow, getRhsOffset());
      Set<Integer> basicRows = new HashSet<Integer>();
      for (int i = 0; i < coefficients.length; i++) {
          Integer basicRow = getBasicRow(getNumObjectiveFunctions() + i, true);
          if (basicRow != null && basicRows.contains(basicRow)) {
              coefficients[i] = 0;
          } else {
              if (basicRow != null) {
                  basicRows.add(basicRow);
              }
              coefficients[i] =
                  (basicRow == null ? 0 : getEntry(basicRow, getRhsOffset())) -
                  (restrictToNonNegative ? 0 : mostNegative);
          }
      }
        return new RealPointValuePair(coefficients, f.getValue(coefficients));
    }