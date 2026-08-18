// org/apache/commons/math/optimization/linear/SimplexSolverTest.java
@Test
public void testMath272AdditionalCase2() throws OptimizationException {
    LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 3, 1, 2 }, 0);
    Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
    constraints.add(new LinearConstraint(new double[] { 1, 0, 1 }, Relationship.GEQ, 2));
    constraints.add(new LinearConstraint(new double[] { 0, 1, 1 }, Relationship.GEQ, 2));

    SimplexSolver solver = new SimplexSolver();
    RealPointValuePair solution = solver.optimize(f, constraints, GoalType.MINIMIZE, true);
    
    assertTrue(solution.getPoint()[0] >= 0);
    assertTrue(solution.getPoint()[1] >= 0);
    assertTrue(solution.getPoint()[2] >= 0);
    assertTrue(solution.getPoint()[0] + solution.getPoint()[2] >= 1.9999);
    assertTrue(solution.getPoint()[1] + solution.getPoint()[2] >= 1.9999);
    assertEquals(6.0, solution.getValue(), 0.0001);
}