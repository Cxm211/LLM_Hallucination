protected RealPointValuePair getSolution() {
      double[] coefficients = new double[getOriginalNumDecisionVariables()];
      Integer negativeVarBasicRow = getBasicRow(getNegativeDecisionVariableOffset());
      double mostNegative = negativeVarBasicRow == null ? 0 : getEntry(negativeVarBasicRow, getRhsOffset());
      Set<Integer> basicRows = new HashSet<Integer>();
      for (int i = 0; i < coefficients.length; i++) {
          Integer basicRow = getBasicRow(getNumObjectiveFunctions() + i);
          if (basicRow == null) {
              coefficients[i] = 0 - (restrictToNonNegative ? 0 : mostNegative);
          } else {
              if (basicRows.contains(basicRow)) {
                  coefficients[i] = 0;
              } else {
                  basicRows.add(basicRow);
                  coefficients[i] = getEntry(basicRow, getRhsOffset()) - (restrictToNonNegative ? 0 : mostNegative);
              }
          }
      }
        return new RealPointValuePair(coefficients, f.getValue(coefficients));
    }