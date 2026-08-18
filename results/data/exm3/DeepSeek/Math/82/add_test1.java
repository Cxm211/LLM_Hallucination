// org/apache/commons/math/optimization/linear/SimplexSolverTest.java
@Test(expected = OptimizationException.class)
    public void testUnbounded() throws OptimizationException {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[]{1,1}, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[]{1,-1}, Relationship.LEQ, 1.0));
        SimplexSolver solver = new SimplexSolver();
        solver.optimize(f, constraints, GoalType.MAXIMIZE, true);
    }
