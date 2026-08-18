// org/apache/commons/math3/optimization/linear/SimplexSolverTest.java
@Test
public void testMath828CycleVariant2() {
    LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0}, 0.0);
    
    ArrayList <LinearConstraint>constraints = new ArrayList<LinearConstraint>();

    constraints.add(new LinearConstraint(new double[] {0.0, 5.0, 10.0, 15.0, 20.0, 25.0, 30.0, 35.0, 40.0, 45.0,}, Relationship.GEQ, 25.0));
    constraints.add(new LinearConstraint(new double[] {0.0, 6.0, 12.0, 18.0, 24.0, 30.0, 36.0, 42.0, 48.0, 54.0,}, Relationship.GEQ, 25.0));
    constraints.add(new LinearConstraint(new double[] {0.0, 7.0, 14.0, 21.0, 28.0, 35.0, 42.0, 49.0, 56.0, 63.0,}, Relationship.GEQ, 25.0));
    constraints.add(new LinearConstraint(new double[] {0.0, 8.0, 16.0, 24.0, 32.0, 40.0, 48.0, 56.0, 64.0, 72.0,}, Relationship.GEQ, 25.0));
    constraints.add(new LinearConstraint(new double[] {20.0, -5.0, -10.0, -15.0, -20.0, -25.0, -30.0, -35.0, -40.0, -45.0,}, Relationship.GEQ, 0.0));
    constraints.add(new LinearConstraint(new double[] {15.0, -6.0, -12.0, -18.0, -24.0, -30.0, -36.0, -42.0, -48.0, -54.0,}, Relationship.GEQ, 0.0));
    constraints.add(new LinearConstraint(new double[] {10.0, -7.0, -14.0, -21.0, -28.0, -35.0, -42.0, -49.0, -56.0, -63.0,}, Relationship.GEQ, 0.0));
    
    double epsilon = 1e-6;
    PointValuePair solution = new SimplexSolver().optimize(f, constraints, GoalType.MINIMIZE, true);
    Assert.assertEquals(1.0d, solution.getValue(), epsilon);
    Assert.assertTrue(validSolution(solution, constraints, epsilon));        
}