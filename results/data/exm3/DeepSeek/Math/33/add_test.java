// org/apache/commons/math3/optimization/linear/SimplexSolverTest.java
@Test
public void testDropPhase1WithBasicArtificial() {
    LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1, 1 }, 0);
    ArrayList<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
    constraints.add(new LinearConstraint(new double[] { 1, 1 }, Relationship.EQ, 1));
    double epsilon = 1e-6;
    SimplexSolver solver = new SimplexSolver();
    PointValuePair solution = solver.optimize(f, constraints, GoalType.MAXIMIZE, false);
    Assert.assertEquals(1.0, solution.getValue(), epsilon);
    Assert.assertEquals(1.0, solution.getPoint()[0] + solution.getPoint()[1], epsilon);
}
