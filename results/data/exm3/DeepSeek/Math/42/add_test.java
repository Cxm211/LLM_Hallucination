// org/apache/commons/math/optimization/linear/SimplexSolverTest.java
@Test
    public void testNonBasicVariableWithNegativeAllowed() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] {1.0, 1.0, 0.0}, 0.0d);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] {1, 0, 0}, Relationship.EQ, 1));
        constraints.add(new LinearConstraint(new double[] {0, 1, 0}, Relationship.GEQ, -1));
        SimplexSolver solver = new SimplexSolver();
        RealPointValuePair solution = solver.optimize(f, constraints, GoalType.MINIMIZE, false);
        Assert.assertEquals(1.0, solution.getPoint()[0], 1e-6);
        Assert.assertEquals(-1.0, solution.getPoint()[1], 1e-6);
        Assert.assertEquals(0.0, solution.getPoint()[2], 1e-6);
    }
