// org/apache/commons/math/optimization/linear/SimplexSolverTest.java
@Test
    public void testSmallNegativeEntry() throws OptimizationException {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[]{1}, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[]{1}, Relationship.LEQ, 1.0));
        constraints.add(new LinearConstraint(new double[]{-1e-7}, Relationship.LEQ, 0.0));
        SimplexSolver solver = new SimplexSolver();
        RealPointValuePair solution = solver.optimize(f, constraints, GoalType.MAXIMIZE, true);
        assertEquals(1.0, solution.getValue(), 1e-6);
    }
