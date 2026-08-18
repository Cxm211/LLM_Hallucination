// org/apache/commons/math/optimization/linear/SimplexSolverTest.java
@Test
public void testTwoVariablesWithIdentitySlack() throws OptimizationException {
    LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 4, 5 }, 0);
    Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
    constraints.add(new LinearConstraint(new double[] { 1, 0 }, Relationship.LEQ, 12));
    constraints.add(new LinearConstraint(new double[] { 0, 1 }, Relationship.LEQ, 15));

    SimplexSolver solver = new SimplexSolver();
    RealPointValuePair solution = solver.optimize(f, constraints, GoalType.MAXIMIZE, false);
    assertEquals(12.0, solution.getPoint()[0], 0.0);
    assertEquals(15.0, solution.getPoint()[1], 0.0);
    assertEquals(123.0, solution.getValue(), 0.0);
}