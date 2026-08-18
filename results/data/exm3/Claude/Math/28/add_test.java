// org/apache/commons/math3/optimization/linear/SimplexSolverTest.java
@Test
public void testMath828CycleVariant1() {
    LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0}, 0.0);
    
    ArrayList <LinearConstraint>constraints = new ArrayList<LinearConstraint>();

    constraints.add(new LinearConstraint(new double[] {0.0, 10.0, 20.0, 30.0, 40.0, 50.0, 60.0, 70.0, 80.0, 90.0, 100.0, 110.0, 120.0, 130.0, 140.0,}, Relationship.GEQ, 50.0));
    constraints.add(new LinearConstraint(new double[] {0.0, 15.0, 25.0, 35.0, 45.0, 55.0, 65.0, 75.0, 85.0, 95.0, 105.0, 115.0, 125.0, 135.0, 145.0,}, Relationship.GEQ, 50.0));
    constraints.add(new LinearConstraint(new double[] {0.0, 12.0, 22.0, 32.0, 42.0, 52.0, 62.0, 72.0, 82.0, 92.0, 102.0, 112.0, 122.0, 132.0, 142.0,}, Relationship.GEQ, 50.0));
    constraints.add(new LinearConstraint(new double[] {50.0, -10.0, -20.0, -30.0, -40.0, -50.0, -60.0, -70.0, -80.0, -90.0, -100.0, -110.0, -120.0, -130.0, -140.0,}, Relationship.GEQ, 0.0));
    constraints.add(new LinearConstraint(new double[] {30.0, -15.0, -25.0, -35.0, -45.0, -55.0, -65.0, -75.0, -85.0, -95.0, -105.0, -115.0, -125.0, -135.0, -145.0,}, Relationship.GEQ, 0.0));
    
    double epsilon = 1e-6;
    PointValuePair solution = new SimplexSolver().optimize(f, constraints, GoalType.MINIMIZE, true);
    Assert.assertEquals(1.0d, solution.getValue(), epsilon);
    Assert.assertTrue(validSolution(solution, constraints, epsilon));        
}