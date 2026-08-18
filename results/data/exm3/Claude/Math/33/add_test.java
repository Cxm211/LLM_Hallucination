// org/apache/commons/math3/optimization/linear/SimplexSolverTest.java
@Test
    public void testMath781Additional1() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1, 1, 1 }, 0);

        ArrayList<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 }, Relationship.LEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 }, Relationship.LEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 }, Relationship.LEQ, 1));

        double epsilon = 1e-6;
        SimplexSolver solver = new SimplexSolver();
        PointValuePair solution = solver.optimize(f, constraints, GoalType.MAXIMIZE, false);

        Assert.assertEquals(1.0d, solution.getPoint()[0], epsilon);
        Assert.assertEquals(1.0d, solution.getPoint()[1], epsilon);
        Assert.assertEquals(1.0d, solution.getPoint()[2], epsilon);
        Assert.assertEquals(3.0d, solution.getValue(), epsilon);
    }