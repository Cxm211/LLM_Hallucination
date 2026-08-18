// org/apache/commons/math/optimization/linear/SimplexSolverTest.java
@Test
    public void testUnrestrictedVariable() throws OptimizationException {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1.0 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1 }, Relationship.EQ, 5.0));
        RealPointValuePair solution = new SimplexSolver().optimize(f, constraints, GoalType.MAXIMIZE, false);
        assertEquals(5.0, solution.getValue(), 1e-6);
        assertEquals(5.0, solution.getPoint()[0], 1e-6);
    }
