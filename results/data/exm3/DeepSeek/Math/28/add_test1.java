// org/apache/commons/math3/optimization/linear/SimplexSolverTest.java
@Test
    public void testBlandsRuleTie() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] {1.0, 1.0}, 0.0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] {1.0, 0.0}, Relationship.LEQ, 1.0));
        constraints.add(new LinearConstraint(new double[] {1.0, 0.0}, Relationship.LEQ, 1.0));
        constraints.add(new LinearConstraint(new double[] {0.0, 1.0}, Relationship.LEQ, 1.0));
        double epsilon = 1e-6;
        PointValuePair solution = new SimplexSolver().optimize(f, constraints, GoalType.MAXIMIZE, true);
        Assert.assertEquals(2.0, solution.getValue(), epsilon);
        Assert.assertTrue(validSolution(solution, constraints, epsilon));
    }
