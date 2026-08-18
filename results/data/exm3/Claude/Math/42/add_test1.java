// org/apache/commons/math/optimization/linear/SimplexSolverTest.java
@Test
    public void testMath713MultipleVariablesNegative() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] {1.0, 1.0, 1.0}, 0.0d);
        ArrayList<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] {1, 1, 0}, Relationship.EQ, 2));
        constraints.add(new LinearConstraint(new double[] {0, 0, 1}, Relationship.EQ, 1));

        double epsilon = 1e-6;
        SimplexSolver solver = new SimplexSolver();
        RealPointValuePair solution = solver.optimize(f, constraints, GoalType.MINIMIZE, true);

        Assert.assertTrue(Precision.compareTo(solution.getPoint()[0], 0.0d, epsilon) >= 0);
        Assert.assertTrue(Precision.compareTo(solution.getPoint()[1], 0.0d, epsilon) >= 0);
        Assert.assertTrue(Precision.compareTo(solution.getPoint()[2], 0.0d, epsilon) >= 0);
    }