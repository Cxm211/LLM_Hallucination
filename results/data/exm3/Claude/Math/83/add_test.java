// org/apache/commons/math/optimization/linear/SimplexSolverTest.java
@Test
public void testMath286Additional1() throws OptimizationException {
  LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1.0, 2.0 }, 0 );
  Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
  constraints.add(new LinearConstraint(new double[] { 1, 0 }, Relationship.EQ, 10.0));
  constraints.add(new LinearConstraint(new double[] { 0, 1 }, Relationship.EQ, 5.0));

  RealPointValuePair solution = new SimplexSolver().optimize(f, constraints, GoalType.MAXIMIZE, false);
  assertEquals(20.0, solution.getValue(), .0000001);
  assertEquals(10.0, solution.getPoint()[0], .0000001);
  assertEquals(5.0, solution.getPoint()[1], .0000001);
}