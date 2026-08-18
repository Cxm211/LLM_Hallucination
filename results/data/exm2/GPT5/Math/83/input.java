    private Integer getBasicRow(final int col, boolean ignoreObjectiveRows) {
        Integer row = null;
        int start = getNumObjectiveFunctions();
        for (int i = start; i < getHeight(); i++) {
            if (MathUtils.equals(getEntry(i, col), 1.0, epsilon) && (row == null)) {
                row = i;
            } else if (!MathUtils.equals(getEntry(i, col), 0.0, epsilon)) {
                return null;
            }
        }
        return row;
    }

    protected RealPointValuePair getSolution() {
      double[] coefficients = new double[getOriginalNumDecisionVariables()];
      Integer negativeVarBasicRow = getBasicRow(getNegativeDecisionVariableOffset());
      double mostNegative = negativeVarBasicRow == null ? 0 : getEntry(negativeVarBasicRow, getRhsOffset());
      Set<Integer> basicRows = new HashSet<Integer>();
      for (int i = 0; i < coefficients.length; i++) {
          Integer basicRow = getBasicRow(getNumObjectiveFunctions() + i);
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

// trigger testcase
@Test
    public void testMath286() throws OptimizationException {
      LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 0.2, 0.3 }, 0 );
      Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
      constraints.add(new LinearConstraint(new double[] { 1, 1 }, Relationship.EQ, 23.0));

      RealPointValuePair solution = new SimplexSolver().optimize(f, constraints, GoalType.MAXIMIZE, true);
      assertEquals(6.9, solution.getValue(), .0000001);
    }
