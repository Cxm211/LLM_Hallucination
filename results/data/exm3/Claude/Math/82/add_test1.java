// org/apache/commons/math/optimization/linear/SimplexSolverTest.java
@Test
public void testPivotRowWithNegativeRHS() throws OptimizationException {
    LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1, 0, 0 }, 0);
    Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
    constraints.add(new LinearConstraint(new double[] { -1, 1, 0 }, Relationship.LEQ, 1.0));
    constraints.add(new LinearConstraint(new double[] { 1, 0, 1 }, Relationship.EQ, 2.0));
    
    SimplexSolver solver = new SimplexSolver();
    RealPointValuePair solution = solver.optimize(f, constraints, GoalType.MAXIMIZE, false);
    assertEquals(2.0, solution.getValue(), 0.0000001);
}