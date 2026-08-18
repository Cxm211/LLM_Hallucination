// org/apache/commons/math/optimization/linear/SimplexSolverTest.java
@Test
    public void testUnrestrictedVariable() throws OptimizationException {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1 }, Relationship.GEQ, 1));
        SimplexSolver solver = new SimplexSolver();
        RealPointValuePair solution = solver.optimize(f, constraints, GoalType.MINIMIZE, false);
        assertEquals(1.0, solution.getPoint()[0], 1e-6);
        assertEquals(1.0, solution.getValue(), 1e-6);
    }
