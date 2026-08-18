// org/apache/commons/math3/optimization/linear/SimplexSolverTest.java
@Test
    public void testMath781Additional2() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 3, 2 }, 0);

        ArrayList<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 1 }, Relationship.LEQ, 4));
        constraints.add(new LinearConstraint(new double[] { 2, 1 }, Relationship.LEQ, 5));

        double epsilon = 1e-6;
        SimplexSolver solver = new SimplexSolver();
        PointValuePair solution = solver.optimize(f, constraints, GoalType.MAXIMIZE, false);

        Assert.assertTrue(solution.getPoint()[0] >= 0.0d - epsilon);
        Assert.assertTrue(solution.getPoint()[1] >= 0.0d - epsilon);
        Assert.assertEquals(11.0d, solution.getValue(), epsilon);
    }