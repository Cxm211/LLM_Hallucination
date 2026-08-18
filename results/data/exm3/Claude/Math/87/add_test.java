// org/apache/commons/math/optimization/linear/SimplexSolverTest.java
@Test
public void testMultipleVariablesWithBasicColumn() throws OptimizationException {
    LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 2, 3, 1 }, 0);
    Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
    constraints.add(new LinearConstraint(new double[] { 1, 0, 0 }, Relationship.LEQ, 5));
    constraints.add(new LinearConstraint(new double[] { 0, 1, 0 }, Relationship.LEQ, 8));
    constraints.add(new LinearConstraint(new double[] { 0, 0, 1 }, Relationship.LEQ, 3));

    SimplexSolver solver = new SimplexSolver();
    RealPointValuePair solution = solver.optimize(f, constraints, GoalType.MAXIMIZE, false);
    assertEquals(5.0, solution.getPoint()[0], 0.0);
    assertEquals(8.0, solution.getPoint()[1], 0.0);
    assertEquals(3.0, solution.getPoint()[2], 0.0);
    assertEquals(37.0, solution.getValue(), 0.0);
}