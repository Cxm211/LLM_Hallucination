// org/apache/commons/math/optimization/linear/SimplexSolverTest.java
@Test
    public void testMath713NegativeVariableWithNonRestrictive() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] {1.0, 1.0}, 0.0d);
        ArrayList<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] {1, 0}, Relationship.EQ, 1));

        double epsilon = 1e-6;
        SimplexSolver solver = new SimplexSolver();
        RealPointValuePair solution = solver.optimize(f, constraints, GoalType.MINIMIZE, false);

        Assert.assertEquals(1.0d, solution.getPoint()[0], epsilon);
    }