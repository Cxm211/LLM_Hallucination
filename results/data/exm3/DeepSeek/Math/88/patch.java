    protected RealPointValuePair getSolution() {
        double[] coefficients = new double[getOriginalNumDecisionVariables()];
        double mostNegative = 0;
        if (!restrictToNonNegative) {
            int artificialVarOffset = getArtificialVariableOffset();
            for (int i = 0; i < getNumArtificialVariables(); i++) {
                Integer basicRow = getBasicRow(artificialVarOffset + i);
                if (basicRow != null) {
                    double entry = getEntry(basicRow, getRhsOffset());
                    if (entry < mostNegative) {
                        mostNegative = entry;
                    }
                }
            }
        }
        for (int i = 0; i < coefficients.length; i++) {
            Integer basicRow = getBasicRow(getNumObjectiveFunctions() + i);
                // if multiple variables can take a given value 
                // then we choose the first and set the rest equal to 0
                coefficients[i] =
                    (basicRow == null ? 0 : getEntry(basicRow, getRhsOffset())) -
                    (restrictToNonNegative ? 0 : mostNegative);
            if (basicRow != null) {
                for (int j = getNumObjectiveFunctions(); j < getNumObjectiveFunctions() + i; j++) {
                    if (tableau.getEntry(basicRow, j) == 1) {
                         coefficients[i] = 0;
                    }
                }
            }
        }
        return new RealPointValuePair(coefficients, f.getValue(coefficients));
    }