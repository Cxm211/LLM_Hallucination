// org/apache/commons/math/optimization/linear/SimplexSolverTest.java::testSingleVariableAndConstraint
@Test
public void testSingleVariableAndConstraintWithCoefficient() throws OptimizationException {
    LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 3 }, 0);
    Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
    // Constraint: 2x <= 10 -> optimal x = 5
    constraints.add(new LinearConstraint(new double[] { 2 }, Relationship.LEQ, 10));

    SimplexSolver solver = new SimplexSolver();
    RealPointValuePair solution = solver.optimize(f, constraints, GoalType.MAXIMIZE, false);
    assertEquals(5.0, solution.getPoint()[0], 0.0);
    assertEquals(15.0, solution.getValue(), 0.0);
}