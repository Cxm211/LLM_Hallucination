// org/apache/commons/math3/optimization/linear/SimplexSolverTest.java
@Test
    public void testArtificialVariableTie() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] {1.0}, 0.0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] {1.0}, Relationship.GEQ, 1.0));
        constraints.add(new LinearConstraint(new double[] {2.0}, Relationship.GEQ, 2.0));
        double epsilon = 1e-6;
        PointValuePair solution = new SimplexSolver().optimize(f, constraints, GoalType.MINIMIZE, true);
        Assert.assertEquals(1.0, solution.getValue(), epsilon);
        Assert.assertTrue(validSolution(solution, constraints, epsilon));
    }
