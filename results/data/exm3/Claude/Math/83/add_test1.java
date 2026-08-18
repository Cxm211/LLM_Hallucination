// org/apache/commons/math/optimization/linear/SimplexSolverTest.java
@Test
public void testMath286Additional2() throws OptimizationException {
  LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { -1.0, -1.0 }, 0 );
  Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
  constraints.add(new LinearConstraint(new double[] { 1, 1 }, Relationship.GEQ, 5.0));

  RealPointValuePair solution = new SimplexSolver().optimize(f, constraints, GoalType.MINIMIZE, false);
  assertTrue(solution.getValue() <= -5.0 + .0000001);
}