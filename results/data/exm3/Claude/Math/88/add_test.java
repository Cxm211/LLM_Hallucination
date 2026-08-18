// org/apache/commons/math/optimization/linear/SimplexSolverTest.java
@Test
public void testMath272AdditionalCase1() throws OptimizationException {
    LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1, 1, 1, 1 }, 0);
    Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
    constraints.add(new LinearConstraint(new double[] { 1, 1, 0, 0 }, Relationship.GEQ, 1));
    constraints.add(new LinearConstraint(new double[] { 0, 1, 1, 0 }, Relationship.GEQ, 1));
    constraints.add(new LinearConstraint(new double[] { 0, 0, 1, 1 }, Relationship.GEQ, 1));

    SimplexSolver solver = new SimplexSolver();
    RealPointValuePair solution = solver.optimize(f, constraints, GoalType.MINIMIZE, true);
    
    assertTrue(solution.getPoint()[0] >= 0);
    assertTrue(solution.getPoint()[1] >= 0);
    assertTrue(solution.getPoint()[2] >= 0);
    assertTrue(solution.getPoint()[3] >= 0);
    assertTrue(solution.getPoint()[0] + solution.getPoint()[1] >= 0.9999);
    assertTrue(solution.getPoint()[1] + solution.getPoint()[2] >= 0.9999);
    assertTrue(solution.getPoint()[2] + solution.getPoint()[3] >= 0.9999);
    assertEquals(2.0, solution.getValue(), 0.0001);
}