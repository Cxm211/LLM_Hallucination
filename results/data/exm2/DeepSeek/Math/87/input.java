    private Integer getBasicRow(final int col) {
        Integer row = null;
        for (int i = getNumObjectiveFunctions(); i < getHeight(); i++) {
            if (!MathUtils.equals(getEntry(i, col), 0.0, epsilon)) {
                if (row == null) {
                row = i;
                } else {
                return null;
                }
            }
        }
        return row;
    }

// trigger testcase
@Test
    public void testSingleVariableAndConstraint() throws OptimizationException {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 3 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1 }, Relationship.LEQ, 10));

        SimplexSolver solver = new SimplexSolver();
        RealPointValuePair solution = solver.optimize(f, constraints, GoalType.MAXIMIZE, false);
        assertEquals(10.0, solution.getPoint()[0], 0.0);
        assertEquals(30.0, solution.getValue(), 0.0);
    }
