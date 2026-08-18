// org/apache/commons/math/optimization/linear/SimplexSolverTest.java
@Test
public void testMath286_variantMaxSum() throws OptimizationException {
  LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1.0, 1.0 }, 0 );
  Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
  constraints.add(new LinearConstraint(new double[] { 1, 1 }, Relationship.EQ, 23.0));

  RealPointValuePair solution = new SimplexSolver().optimize(f, constraints, GoalType.MAXIMIZE, true);
  assertEquals(23.0, solution.getValue(), 1e-7);
}
