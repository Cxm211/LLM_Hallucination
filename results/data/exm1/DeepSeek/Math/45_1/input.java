// buggy code
    public OpenMapRealMatrix(int rowDimension, int columnDimension) {
        super(rowDimension, columnDimension);
        this.rows = rowDimension;
        this.columns = columnDimension;
        this.entries = new OpenIntToDoubleHashMap(0.0);
    }

// relevant test
// org.apache.commons.math.optimization.linear.SimplexSolverTest::testMath434UnfeasibleSolution
    public void testMath434UnfeasibleSolution() {
        double epsilon = 1e-6;

        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] {1.0, 0.0}, 0.0);
        ArrayList<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] {epsilon/2, 0.5}, Relationship.EQ, 0));
        constraints.add(new LinearConstraint(new double[] {1e-3, 0.1}, Relationship.EQ, 10));

        SimplexSolver solver = new SimplexSolver();
        
        solver.optimize(f, constraints, GoalType.MINIMIZE, true);
    }

// org.apache.commons.math.optimization.linear.SimplexSolverTest::testMath434PivotRowSelection
    public void testMath434PivotRowSelection() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] {1.0}, 0.0);

        double epsilon = 1e-6;
        ArrayList<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] {200}, Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] {100}, Relationship.GEQ, 0.499900001));

        SimplexSolver solver = new SimplexSolver();
        RealPointValuePair solution = solver.optimize(f, constraints, GoalType.MINIMIZE, false);
        
        Assert.assertTrue(MathUtils.compareTo(solution.getPoint()[0] * 200.d, 1.d, epsilon) >= 0);
        Assert.assertEquals(0.0050, solution.getValue(), epsilon);
    }

// org.apache.commons.math.optimization.linear.SimplexSolverTest::testMath434PivotRowSelection2
    public void testMath434PivotRowSelection2() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] {0.0d, 1.0d, 1.0d, 0.0d, 0.0d, 0.0d, 0.0d}, 0.0d);

        ArrayList<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] {1.0d, -0.1d, 0.0d, 0.0d, 0.0d, 0.0d, 0.0d}, Relationship.EQ, -0.1d));
        constraints.add(new LinearConstraint(new double[] {1.0d, 0.0d, 0.0d, 0.0d, 0.0d, 0.0d, 0.0d}, Relationship.GEQ, -1e-18d));
        constraints.add(new LinearConstraint(new double[] {0.0d, 1.0d, 0.0d, 0.0d, 0.0d, 0.0d, 0.0d}, Relationship.GEQ, 0.0d));
        constraints.add(new LinearConstraint(new double[] {0.0d, 0.0d, 0.0d, 1.0d, 0.0d, -0.0128588d, 1e-5d}, Relationship.EQ, 0.0d));
        constraints.add(new LinearConstraint(new double[] {0.0d, 0.0d, 0.0d, 0.0d, 1.0d, 1e-5d, -0.0128586d}, Relationship.EQ, 1e-10d));
        constraints.add(new LinearConstraint(new double[] {0.0d, 0.0d, 1.0d, -1.0d, 0.0d, 0.0d, 0.0d}, Relationship.GEQ, 0.0d));
        constraints.add(new LinearConstraint(new double[] {0.0d, 0.0d, 1.0d, 1.0d, 0.0d, 0.0d, 0.0d}, Relationship.GEQ, 0.0d));
        constraints.add(new LinearConstraint(new double[] {0.0d, 0.0d, 1.0d, 0.0d, -1.0d, 0.0d, 0.0d}, Relationship.GEQ, 0.0d));
        constraints.add(new LinearConstraint(new double[] {0.0d, 0.0d, 1.0d, 0.0d, 1.0d, 0.0d, 0.0d}, Relationship.GEQ, 0.0d));

        double epsilon = 1e-7;
        SimplexSolver simplex = new SimplexSolver();
        RealPointValuePair solution = simplex.optimize(f, constraints, GoalType.MINIMIZE, false);
        
        Assert.assertTrue(MathUtils.compareTo(solution.getPoint()[0], -1e-18d, epsilon) >= 0);
        Assert.assertEquals(1.0d, solution.getPoint()[1], epsilon);        
        Assert.assertEquals(0.0d, solution.getPoint()[2], epsilon);
        Assert.assertEquals(1.0d, solution.getValue(), epsilon);
    }

// org.apache.commons.math.optimization.linear.SimplexSolverTest::testMath272
    public void testMath272() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 2, 2, 1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 1, 0 }, Relationship.GEQ,  1));
        constraints.add(new LinearConstraint(new double[] { 1, 0, 1 }, Relationship.GEQ,  1));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 }, Relationship.GEQ,  1));

        SimplexSolver solver = new SimplexSolver();
        RealPointValuePair solution = solver.optimize(f, constraints, GoalType.MINIMIZE, true);

        Assert.assertEquals(0.0, solution.getPoint()[0], .0000001);
        Assert.assertEquals(1.0, solution.getPoint()[1], .0000001);
        Assert.assertEquals(1.0, solution.getPoint()[2], .0000001);
        Assert.assertEquals(3.0, solution.getValue(), .0000001);
    }

// org.apache.commons.math.optimization.linear.SimplexSolverTest::testMath286
    public void testMath286() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 0.8, 0.2, 0.7, 0.3, 0.6, 0.4 }, 0 );
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 1, 0, 1, 0 }, Relationship.EQ, 23.0));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0, 1, 0, 1 }, Relationship.EQ, 23.0));
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0, 0, 0, 0 }, Relationship.GEQ, 10.0));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1, 0, 0, 0 }, Relationship.GEQ, 8.0));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 0, 0, 1, 0 }, Relationship.GEQ, 5.0));

        SimplexSolver solver = new SimplexSolver();
        RealPointValuePair solution = solver.optimize(f, constraints, GoalType.MAXIMIZE, true);

        Assert.assertEquals(25.8, solution.getValue(), .0000001);
        Assert.assertEquals(23.0, solution.getPoint()[0] + solution.getPoint()[2] + solution.getPoint()[4], 0.0000001);
        Assert.assertEquals(23.0, solution.getPoint()[1] + solution.getPoint()[3] + solution.getPoint()[5], 0.0000001);
        Assert.assertTrue(solution.getPoint()[0] >= 10.0 - 0.0000001);
        Assert.assertTrue(solution.getPoint()[2] >= 8.0 - 0.0000001);
        Assert.assertTrue(solution.getPoint()[4] >= 5.0 - 0.0000001);
    }

// org.apache.commons.math.optimization.linear.SimplexSolverTest::testDegeneracy
    public void testDegeneracy() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 0.8, 0.7 }, 0 );
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 1 }, Relationship.LEQ, 18.0));
        constraints.add(new LinearConstraint(new double[] { 1, 0 }, Relationship.GEQ, 10.0));
        constraints.add(new LinearConstraint(new double[] { 0, 1 }, Relationship.GEQ, 8.0));

        SimplexSolver solver = new SimplexSolver();
        RealPointValuePair solution = solver.optimize(f, constraints, GoalType.MAXIMIZE, true);
        Assert.assertEquals(13.6, solution.getValue(), .0000001);
    }

// org.apache.commons.math.optimization.linear.SimplexSolverTest::testMath288
    public void testMath288() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 7, 3, 0, 0 }, 0 );
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 3, 0, -5, 0 }, Relationship.LEQ, 0.0));
        constraints.add(new LinearConstraint(new double[] { 2, 0, 0, -5 }, Relationship.LEQ, 0.0));
        constraints.add(new LinearConstraint(new double[] { 0, 3, 0, -5 }, Relationship.LEQ, 0.0));
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0, 0 }, Relationship.LEQ, 1.0));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0, 0 }, Relationship.LEQ, 1.0));

        SimplexSolver solver = new SimplexSolver();
        RealPointValuePair solution = solver.optimize(f, constraints, GoalType.MAXIMIZE, true);
        Assert.assertEquals(10.0, solution.getValue(), .0000001);
    }

// org.apache.commons.math.optimization.linear.SimplexSolverTest::testMath290GEQ
    public void testMath290GEQ() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1, 5 }, 0 );
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 2, 0 }, Relationship.GEQ, -1.0));
        SimplexSolver solver = new SimplexSolver();
        RealPointValuePair solution = solver.optimize(f, constraints, GoalType.MINIMIZE, true);
        Assert.assertEquals(0, solution.getValue(), .0000001);
        Assert.assertEquals(0, solution.getPoint()[0], .0000001);
        Assert.assertEquals(0, solution.getPoint()[1], .0000001);
    }

// org.apache.commons.math.optimization.linear.SimplexSolverTest::testMath290LEQ
    public void testMath290LEQ() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1, 5 }, 0 );
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 2, 0 }, Relationship.LEQ, -1.0));
        SimplexSolver solver = new SimplexSolver();
        solver.optimize(f, constraints, GoalType.MINIMIZE, true);
    }

// org.apache.commons.math.optimization.linear.SimplexSolverTest::testMath293
    public void testMath293() {
      LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 0.8, 0.2, 0.7, 0.3, 0.4, 0.6}, 0 );
      Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
      constraints.add(new LinearConstraint(new double[] { 1, 0, 1, 0, 1, 0 }, Relationship.EQ, 30.0));
      constraints.add(new LinearConstraint(new double[] { 0, 1, 0, 1, 0, 1 }, Relationship.EQ, 30.0));
      constraints.add(new LinearConstraint(new double[] { 0.8, 0.2, 0.0, 0.0, 0.0, 0.0 }, Relationship.GEQ, 10.0));
      constraints.add(new LinearConstraint(new double[] { 0.0, 0.0, 0.7, 0.3, 0.0, 0.0 }, Relationship.GEQ, 10.0));
      constraints.add(new LinearConstraint(new double[] { 0.0, 0.0, 0.0, 0.0, 0.4, 0.6 }, Relationship.GEQ, 10.0));

      SimplexSolver solver = new SimplexSolver();
      RealPointValuePair solution1 = solver.optimize(f, constraints, GoalType.MAXIMIZE, true);

      Assert.assertEquals(15.7143, solution1.getPoint()[0], .0001);
      Assert.assertEquals(0.0, solution1.getPoint()[1], .0001);
      Assert.assertEquals(14.2857, solution1.getPoint()[2], .0001);
      Assert.assertEquals(0.0, solution1.getPoint()[3], .0001);
      Assert.assertEquals(0.0, solution1.getPoint()[4], .0001);
      Assert.assertEquals(30.0, solution1.getPoint()[5], .0001);
      Assert.assertEquals(40.57143, solution1.getValue(), .0001);

      double valA = 0.8 * solution1.getPoint()[0] + 0.2 * solution1.getPoint()[1];
      double valB = 0.7 * solution1.getPoint()[2] + 0.3 * solution1.getPoint()[3];
      double valC = 0.4 * solution1.getPoint()[4] + 0.6 * solution1.getPoint()[5];

      f = new LinearObjectiveFunction(new double[] { 0.8, 0.2, 0.7, 0.3, 0.4, 0.6}, 0 );
      constraints = new ArrayList<LinearConstraint>();
      constraints.add(new LinearConstraint(new double[] { 1, 0, 1, 0, 1, 0 }, Relationship.EQ, 30.0));
      constraints.add(new LinearConstraint(new double[] { 0, 1, 0, 1, 0, 1 }, Relationship.EQ, 30.0));
      constraints.add(new LinearConstraint(new double[] { 0.8, 0.2, 0.0, 0.0, 0.0, 0.0 }, Relationship.GEQ, valA));
      constraints.add(new LinearConstraint(new double[] { 0.0, 0.0, 0.7, 0.3, 0.0, 0.0 }, Relationship.GEQ, valB));
      constraints.add(new LinearConstraint(new double[] { 0.0, 0.0, 0.0, 0.0, 0.4, 0.6 }, Relationship.GEQ, valC));

      RealPointValuePair solution2 = solver.optimize(f, constraints, GoalType.MAXIMIZE, true);
      Assert.assertEquals(40.57143, solution2.getValue(), .0001);
    }

// org.apache.commons.math.optimization.linear.SimplexSolverTest::testSimplexSolver
    public void testSimplexSolver() {
        LinearObjectiveFunction f =
            new LinearObjectiveFunction(new double[] { 15, 10 }, 7);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0 }, Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1 }, Relationship.LEQ, 3));
        constraints.add(new LinearConstraint(new double[] { 1, 1 }, Relationship.EQ, 4));

        SimplexSolver solver = new SimplexSolver();
        RealPointValuePair solution = solver.optimize(f, constraints, GoalType.MAXIMIZE, false);
        Assert.assertEquals(2.0, solution.getPoint()[0], 0.0);
        Assert.assertEquals(2.0, solution.getPoint()[1], 0.0);
        Assert.assertEquals(57.0, solution.getValue(), 0.0);
    }

// org.apache.commons.math.optimization.linear.SimplexSolverTest::testSingleVariableAndConstraint
    public void testSingleVariableAndConstraint() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 3 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1 }, Relationship.LEQ, 10));

        SimplexSolver solver = new SimplexSolver();
        RealPointValuePair solution = solver.optimize(f, constraints, GoalType.MAXIMIZE, false);
        Assert.assertEquals(10.0, solution.getPoint()[0], 0.0);
        Assert.assertEquals(30.0, solution.getValue(), 0.0);
    }

// org.apache.commons.math.optimization.linear.SimplexSolverTest::testModelWithNoArtificialVars
    public void testModelWithNoArtificialVars() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 15, 10 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0 }, Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1 }, Relationship.LEQ, 3));
        constraints.add(new LinearConstraint(new double[] { 1, 1 }, Relationship.LEQ, 4));

        SimplexSolver solver = new SimplexSolver();
        RealPointValuePair solution = solver.optimize(f, constraints, GoalType.MAXIMIZE, false);
        Assert.assertEquals(2.0, solution.getPoint()[0], 0.0);
        Assert.assertEquals(2.0, solution.getPoint()[1], 0.0);
        Assert.assertEquals(50.0, solution.getValue(), 0.0);
    }

// org.apache.commons.math.optimization.linear.SimplexSolverTest::testMinimization
    public void testMinimization() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { -2, 1 }, -5);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 2 }, Relationship.LEQ, 6));
        constraints.add(new LinearConstraint(new double[] { 3, 2 }, Relationship.LEQ, 12));
        constraints.add(new LinearConstraint(new double[] { 0, 1 }, Relationship.GEQ, 0));

        SimplexSolver solver = new SimplexSolver();
        RealPointValuePair solution = solver.optimize(f, constraints, GoalType.MINIMIZE, false);
        Assert.assertEquals(4.0, solution.getPoint()[0], 0.0);
        Assert.assertEquals(0.0, solution.getPoint()[1], 0.0);
        Assert.assertEquals(-13.0, solution.getValue(), 0.0);
    }

// org.apache.commons.math.optimization.linear.SimplexSolverTest::testSolutionWithNegativeDecisionVariable
    public void testSolutionWithNegativeDecisionVariable() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { -2, 1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 1 }, Relationship.GEQ, 6));
        constraints.add(new LinearConstraint(new double[] { 1, 2 }, Relationship.LEQ, 14));

        SimplexSolver solver = new SimplexSolver();
        RealPointValuePair solution = solver.optimize(f, constraints, GoalType.MAXIMIZE, false);
        Assert.assertEquals(-2.0, solution.getPoint()[0], 0.0);
        Assert.assertEquals(8.0, solution.getPoint()[1], 0.0);
        Assert.assertEquals(12.0, solution.getValue(), 0.0);
    }

// org.apache.commons.math.optimization.linear.SimplexSolverTest::testInfeasibleSolution
    public void testInfeasibleSolution() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 15 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1 }, Relationship.LEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 1 }, Relationship.GEQ, 3));

        SimplexSolver solver = new SimplexSolver();
        solver.optimize(f, constraints, GoalType.MAXIMIZE, false);
    }

// org.apache.commons.math.optimization.linear.SimplexSolverTest::testUnboundedSolution
    public void testUnboundedSolution() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 15, 10 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0 }, Relationship.EQ, 2));

        SimplexSolver solver = new SimplexSolver();
        solver.optimize(f, constraints, GoalType.MAXIMIZE, false);
    }

// org.apache.commons.math.optimization.linear.SimplexSolverTest::testRestrictVariablesToNonNegative
    public void testRestrictVariablesToNonNegative() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 409, 523, 70, 204, 339 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] {    43,   56, 345,  56,    5 }, Relationship.LEQ,  4567456));
        constraints.add(new LinearConstraint(new double[] {    12,   45,   7,  56,   23 }, Relationship.LEQ,    56454));
        constraints.add(new LinearConstraint(new double[] {     8,  768,   0,  34, 7456 }, Relationship.LEQ,  1923421));
        constraints.add(new LinearConstraint(new double[] { 12342, 2342,  34, 678, 2342 }, Relationship.GEQ,     4356));
        constraints.add(new LinearConstraint(new double[] {    45,  678,  76,  52,   23 }, Relationship.EQ,    456356));

        SimplexSolver solver = new SimplexSolver();
        RealPointValuePair solution = solver.optimize(f, constraints, GoalType.MAXIMIZE, true);
        Assert.assertEquals(2902.92783505155, solution.getPoint()[0], .0000001);
        Assert.assertEquals(480.419243986254, solution.getPoint()[1], .0000001);
        Assert.assertEquals(0.0, solution.getPoint()[2], .0000001);
        Assert.assertEquals(0.0, solution.getPoint()[3], .0000001);
        Assert.assertEquals(0.0, solution.getPoint()[4], .0000001);
        Assert.assertEquals(1438556.7491409, solution.getValue(), .0000001);
    }

// org.apache.commons.math.optimization.linear.SimplexSolverTest::testEpsilon
    public void testEpsilon() {
      LinearObjectiveFunction f =
          new LinearObjectiveFunction(new double[] { 10, 5, 1 }, 0);
      Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
      constraints.add(new LinearConstraint(new double[] {  9, 8, 0 }, Relationship.EQ,  17));
      constraints.add(new LinearConstraint(new double[] {  0, 7, 8 }, Relationship.LEQ,  7));
      constraints.add(new LinearConstraint(new double[] { 10, 0, 2 }, Relationship.LEQ, 10));

      SimplexSolver solver = new SimplexSolver();
      RealPointValuePair solution = solver.optimize(f, constraints, GoalType.MAXIMIZE, false);
      Assert.assertEquals(1.0, solution.getPoint()[0], 0.0);
      Assert.assertEquals(1.0, solution.getPoint()[1], 0.0);
      Assert.assertEquals(0.0, solution.getPoint()[2], 0.0);
      Assert.assertEquals(15.0, solution.getValue(), 0.0);
  }

// org.apache.commons.math.optimization.linear.SimplexSolverTest::testTrivialModel
    public void testTrivialModel() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1, 1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 1 }, Relationship.EQ,  0));

        SimplexSolver solver = new SimplexSolver();
        RealPointValuePair solution = solver.optimize(f, constraints, GoalType.MAXIMIZE, true);
        Assert.assertEquals(0, solution.getValue(), .0000001);
    }

// org.apache.commons.math.optimization.linear.SimplexSolverTest::testLargeModel
    public void testLargeModel() {
        double[] objective = new double[] {
                                           1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                                           1, 1, 12, 1, 1, 1, 1, 1, 1, 1,
                                           1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                                           1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                                           12, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                                           1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                                           1, 1, 1, 1, 1, 1, 1, 1, 12, 1,
                                           1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                                           1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                                           1, 1, 1, 1, 1, 1, 12, 1, 1, 1,
                                           1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                                           1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                                           1, 1, 1, 1, 12, 1, 1, 1, 1, 1,
                                           1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                                           1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                                           1, 1, 12, 1, 1, 1, 1, 1, 1, 1,
                                           1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                                           1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                                           1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                                           1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                                           1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                                           1, 1, 1, 1, 1, 1};

        LinearObjectiveFunction f = new LinearObjectiveFunction(objective, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(equationFromString(objective.length, "x0 + x1 + x2 + x3 - x12 = 0"));
        constraints.add(equationFromString(objective.length, "x4 + x5 + x6 + x7 + x8 + x9 + x10 + x11 - x13 = 0"));
        constraints.add(equationFromString(objective.length, "x4 + x5 + x6 + x7 + x8 + x9 + x10 + x11 >= 49"));
        constraints.add(equationFromString(objective.length, "x0 + x1 + x2 + x3 >= 42"));
        constraints.add(equationFromString(objective.length, "x14 + x15 + x16 + x17 - x26 = 0"));
        constraints.add(equationFromString(objective.length, "x18 + x19 + x20 + x21 + x22 + x23 + x24 + x25 - x27 = 0"));
        constraints.add(equationFromString(objective.length, "x14 + x15 + x16 + x17 - x12 = 0"));
        constraints.add(equationFromString(objective.length, "x18 + x19 + x20 + x21 + x22 + x23 + x24 + x25 - x13 = 0"));
        constraints.add(equationFromString(objective.length, "x28 + x29 + x30 + x31 - x40 = 0"));
        constraints.add(equationFromString(objective.length, "x32 + x33 + x34 + x35 + x36 + x37 + x38 + x39 - x41 = 0"));
        constraints.add(equationFromString(objective.length, "x32 + x33 + x34 + x35 + x36 + x37 + x38 + x39 >= 49"));
        constraints.add(equationFromString(objective.length, "x28 + x29 + x30 + x31 >= 42"));
        constraints.add(equationFromString(objective.length, "x42 + x43 + x44 + x45 - x54 = 0"));
        constraints.add(equationFromString(objective.length, "x46 + x47 + x48 + x49 + x50 + x51 + x52 + x53 - x55 = 0"));
        constraints.add(equationFromString(objective.length, "x42 + x43 + x44 + x45 - x40 = 0"));
        constraints.add(equationFromString(objective.length, "x46 + x47 + x48 + x49 + x50 + x51 + x52 + x53 - x41 = 0"));
        constraints.add(equationFromString(objective.length, "x56 + x57 + x58 + x59 - x68 = 0"));
        constraints.add(equationFromString(objective.length, "x60 + x61 + x62 + x63 + x64 + x65 + x66 + x67 - x69 = 0"));
        constraints.add(equationFromString(objective.length, "x60 + x61 + x62 + x63 + x64 + x65 + x66 + x67 >= 51"));
        constraints.add(equationFromString(objective.length, "x56 + x57 + x58 + x59 >= 44"));
        constraints.add(equationFromString(objective.length, "x70 + x71 + x72 + x73 - x82 = 0"));
        constraints.add(equationFromString(objective.length, "x74 + x75 + x76 + x77 + x78 + x79 + x80 + x81 - x83 = 0"));
        constraints.add(equationFromString(objective.length, "x70 + x71 + x72 + x73 - x68 = 0"));
        constraints.add(equationFromString(objective.length, "x74 + x75 + x76 + x77 + x78 + x79 + x80 + x81 - x69 = 0"));
        constraints.add(equationFromString(objective.length, "x84 + x85 + x86 + x87 - x96 = 0"));
        constraints.add(equationFromString(objective.length, "x88 + x89 + x90 + x91 + x92 + x93 + x94 + x95 - x97 = 0"));
        constraints.add(equationFromString(objective.length, "x88 + x89 + x90 + x91 + x92 + x93 + x94 + x95 >= 51"));
        constraints.add(equationFromString(objective.length, "x84 + x85 + x86 + x87 >= 44"));
        constraints.add(equationFromString(objective.length, "x98 + x99 + x100 + x101 - x110 = 0"));
        constraints.add(equationFromString(objective.length, "x102 + x103 + x104 + x105 + x106 + x107 + x108 + x109 - x111 = 0"));
        constraints.add(equationFromString(objective.length, "x98 + x99 + x100 + x101 - x96 = 0"));
        constraints.add(equationFromString(objective.length, "x102 + x103 + x104 + x105 + x106 + x107 + x108 + x109 - x97 = 0"));
        constraints.add(equationFromString(objective.length, "x112 + x113 + x114 + x115 - x124 = 0"));
        constraints.add(equationFromString(objective.length, "x116 + x117 + x118 + x119 + x120 + x121 + x122 + x123 - x125 = 0"));
        constraints.add(equationFromString(objective.length, "x116 + x117 + x118 + x119 + x120 + x121 + x122 + x123 >= 49"));
        constraints.add(equationFromString(objective.length, "x112 + x113 + x114 + x115 >= 42"));
        constraints.add(equationFromString(objective.length, "x126 + x127 + x128 + x129 - x138 = 0"));
        constraints.add(equationFromString(objective.length, "x130 + x131 + x132 + x133 + x134 + x135 + x136 + x137 - x139 = 0"));
        constraints.add(equationFromString(objective.length, "x126 + x127 + x128 + x129 - x124 = 0"));
        constraints.add(equationFromString(objective.length, "x130 + x131 + x132 + x133 + x134 + x135 + x136 + x137 - x125 = 0"));
        constraints.add(equationFromString(objective.length, "x140 + x141 + x142 + x143 - x152 = 0"));
        constraints.add(equationFromString(objective.length, "x144 + x145 + x146 + x147 + x148 + x149 + x150 + x151 - x153 = 0"));
        constraints.add(equationFromString(objective.length, "x144 + x145 + x146 + x147 + x148 + x149 + x150 + x151 >= 59"));
        constraints.add(equationFromString(objective.length, "x140 + x141 + x142 + x143 >= 42"));
        constraints.add(equationFromString(objective.length, "x154 + x155 + x156 + x157 - x166 = 0"));
        constraints.add(equationFromString(objective.length, "x158 + x159 + x160 + x161 + x162 + x163 + x164 + x165 - x167 = 0"));
        constraints.add(equationFromString(objective.length, "x154 + x155 + x156 + x157 - x152 = 0"));
        constraints.add(equationFromString(objective.length, "x158 + x159 + x160 + x161 + x162 + x163 + x164 + x165 - x153 = 0"));
        constraints.add(equationFromString(objective.length, "x83 + x82 - x168 = 0"));
        constraints.add(equationFromString(objective.length, "x111 + x110 - x169 = 0"));
        constraints.add(equationFromString(objective.length, "x170 - x182 = 0"));
        constraints.add(equationFromString(objective.length, "x171 - x183 = 0"));
        constraints.add(equationFromString(objective.length, "x172 - x184 = 0"));
        constraints.add(equationFromString(objective.length, "x173 - x185 = 0"));
        constraints.add(equationFromString(objective.length, "x174 - x186 = 0"));
        constraints.add(equationFromString(objective.length, "x175 + x176 - x187 = 0"));
        constraints.add(equationFromString(objective.length, "x177 - x188 = 0"));
        constraints.add(equationFromString(objective.length, "x178 - x189 = 0"));
        constraints.add(equationFromString(objective.length, "x179 - x190 = 0"));
        constraints.add(equationFromString(objective.length, "x180 - x191 = 0"));
        constraints.add(equationFromString(objective.length, "x181 - x192 = 0"));
        constraints.add(equationFromString(objective.length, "x170 - x26 = 0"));
        constraints.add(equationFromString(objective.length, "x171 - x27 = 0"));
        constraints.add(equationFromString(objective.length, "x172 - x54 = 0"));
        constraints.add(equationFromString(objective.length, "x173 - x55 = 0"));
        constraints.add(equationFromString(objective.length, "x174 - x168 = 0"));
        constraints.add(equationFromString(objective.length, "x177 - x169 = 0"));
        constraints.add(equationFromString(objective.length, "x178 - x138 = 0"));
        constraints.add(equationFromString(objective.length, "x179 - x139 = 0"));
        constraints.add(equationFromString(objective.length, "x180 - x166 = 0"));
        constraints.add(equationFromString(objective.length, "x181 - x167 = 0"));
        constraints.add(equationFromString(objective.length, "x193 - x205 = 0"));
        constraints.add(equationFromString(objective.length, "x194 - x206 = 0"));
        constraints.add(equationFromString(objective.length, "x195 - x207 = 0"));
        constraints.add(equationFromString(objective.length, "x196 - x208 = 0"));
        constraints.add(equationFromString(objective.length, "x197 - x209 = 0"));
        constraints.add(equationFromString(objective.length, "x198 + x199 - x210 = 0"));
        constraints.add(equationFromString(objective.length, "x200 - x211 = 0"));
        constraints.add(equationFromString(objective.length, "x201 - x212 = 0"));
        constraints.add(equationFromString(objective.length, "x202 - x213 = 0"));
        constraints.add(equationFromString(objective.length, "x203 - x214 = 0"));
        constraints.add(equationFromString(objective.length, "x204 - x215 = 0"));
        constraints.add(equationFromString(objective.length, "x193 - x182 = 0"));
        constraints.add(equationFromString(objective.length, "x194 - x183 = 0"));
        constraints.add(equationFromString(objective.length, "x195 - x184 = 0"));
        constraints.add(equationFromString(objective.length, "x196 - x185 = 0"));
        constraints.add(equationFromString(objective.length, "x197 - x186 = 0"));
        constraints.add(equationFromString(objective.length, "x198 + x199 - x187 = 0"));
        constraints.add(equationFromString(objective.length, "x200 - x188 = 0"));
        constraints.add(equationFromString(objective.length, "x201 - x189 = 0"));
        constraints.add(equationFromString(objective.length, "x202 - x190 = 0"));
        constraints.add(equationFromString(objective.length, "x203 - x191 = 0"));
        constraints.add(equationFromString(objective.length, "x204 - x192 = 0"));

        SimplexSolver solver = new SimplexSolver();
        RealPointValuePair solution = solver.optimize(f, constraints, GoalType.MINIMIZE, true);
        Assert.assertEquals(7518.0, solution.getValue(), .0000001);
    }

// org.apache.commons.math.optimization.linear.SimplexTableauTest::testInitialization
    public void testInitialization() {
        LinearObjectiveFunction f = createFunction();
        Collection<LinearConstraint> constraints = createConstraints();
        SimplexTableau tableau =
            new SimplexTableau(f, constraints, GoalType.MAXIMIZE, false, 1.0e-6);
        double[][] expectedInitialTableau = {
                                             {-1, 0,  -1,  -1,  2, 0, 0, 0, -4},
                                             { 0, 1, -15, -10, 25, 0, 0, 0,  0},
                                             { 0, 0,   1,   0, -1, 1, 0, 0,  2},
                                             { 0, 0,   0,   1, -1, 0, 1, 0,  3},
                                             { 0, 0,   1,   1, -2, 0, 0, 1,  4}
        };
        assertMatrixEquals(expectedInitialTableau, tableau.getData());
    }

// org.apache.commons.math.optimization.linear.SimplexTableauTest::testDropPhase1Objective
    public void testDropPhase1Objective() {
        LinearObjectiveFunction f = createFunction();
        Collection<LinearConstraint> constraints = createConstraints();
        SimplexTableau tableau =
            new SimplexTableau(f, constraints, GoalType.MAXIMIZE, false, 1.0e-6);
        double[][] expectedTableau = {
                                      { 1, -15, -10, 0, 0, 0, 0},
                                      { 0,   1,   0, 1, 0, 0, 2},
                                      { 0,   0,   1, 0, 1, 0, 3},
                                      { 0,   1,   1, 0, 0, 1, 4}
        };
        tableau.dropPhase1Objective();
        assertMatrixEquals(expectedTableau, tableau.getData());
    }

// org.apache.commons.math.optimization.linear.SimplexTableauTest::testTableauWithNoArtificialVars
    public void testTableauWithNoArtificialVars() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] {15, 10}, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] {1, 0}, Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] {0, 1}, Relationship.LEQ, 3));
        constraints.add(new LinearConstraint(new double[] {1, 1}, Relationship.LEQ, 4));
        SimplexTableau tableau =
            new SimplexTableau(f, constraints, GoalType.MAXIMIZE, false, 1.0e-6);
        double[][] initialTableau = {
                                     {1, -15, -10, 25, 0, 0, 0, 0},
                                     {0,   1,   0, -1, 1, 0, 0, 2},
                                     {0,   0,   1, -1, 0, 1, 0, 3},
                                     {0,   1,   1, -2, 0, 0, 1, 4}
        };
        assertMatrixEquals(initialTableau, tableau.getData());
    }

// org.apache.commons.math.optimization.linear.SimplexTableauTest::testSerial
    public void testSerial() {
        LinearObjectiveFunction f = createFunction();
        Collection<LinearConstraint> constraints = createConstraints();
        SimplexTableau tableau =
            new SimplexTableau(f, constraints, GoalType.MAXIMIZE, false, 1.0e-6);
        Assert.assertEquals(tableau, TestUtils.serializeAndRecover(tableau));
    }

// org.apache.commons.math.stat.regression.GLSMultipleLinearRegressionTest::cannotAddXSampleData
    public void cannotAddXSampleData() {
        createRegression().newSampleData(new double[]{}, null, null);
    }

// org.apache.commons.math.stat.regression.GLSMultipleLinearRegressionTest::cannotAddNullYSampleData
    public void cannotAddNullYSampleData() {
        createRegression().newSampleData(null, new double[][]{}, null);
    }

// org.apache.commons.math.stat.regression.GLSMultipleLinearRegressionTest::cannotAddSampleDataWithSizeMismatch
    public void cannotAddSampleDataWithSizeMismatch() {
        double[] y = new double[]{1.0, 2.0};
        double[][] x = new double[1][];
        x[0] = new double[]{1.0, 0};
        createRegression().newSampleData(y, x, null);
    }

// org.apache.commons.math.stat.regression.GLSMultipleLinearRegressionTest::cannotAddNullCovarianceData
    public void cannotAddNullCovarianceData() {
        createRegression().newSampleData(new double[]{}, new double[][]{}, null);
    }

// org.apache.commons.math.stat.regression.GLSMultipleLinearRegressionTest::notEnoughData
    public void notEnoughData() {
        double[]   reducedY = new double[y.length - 1];
        double[][] reducedX = new double[x.length - 1][];
        double[][] reducedO = new double[omega.length - 1][];
        System.arraycopy(y,     0, reducedY, 0, reducedY.length);
        System.arraycopy(x,     0, reducedX, 0, reducedX.length);
        System.arraycopy(omega, 0, reducedO, 0, reducedO.length);
        createRegression().newSampleData(reducedY, reducedX, reducedO);
    }

// org.apache.commons.math.stat.regression.GLSMultipleLinearRegressionTest::cannotAddCovarianceDataWithSampleSizeMismatch
    public void cannotAddCovarianceDataWithSampleSizeMismatch() {
        double[] y = new double[]{1.0, 2.0};
        double[][] x = new double[2][];
        x[0] = new double[]{1.0, 0};
        x[1] = new double[]{0, 1.0};
        double[][] omega = new double[1][];
        omega[0] = new double[]{1.0, 0};
        createRegression().newSampleData(y, x, omega);
    }

// org.apache.commons.math.stat.regression.GLSMultipleLinearRegressionTest::cannotAddCovarianceDataThatIsNotSquare
    public void cannotAddCovarianceDataThatIsNotSquare() {
        double[] y = new double[]{1.0, 2.0};
        double[][] x = new double[2][];
        x[0] = new double[]{1.0, 0};
        x[1] = new double[]{0, 1.0};
        double[][] omega = new double[3][];
        omega[0] = new double[]{1.0, 0};
        omega[1] = new double[]{0, 1.0};
        omega[2] = new double[]{0, 2.0};
        createRegression().newSampleData(y, x, omega);
    }

// org.apache.commons.math.stat.regression.GLSMultipleLinearRegressionTest::testYVariance
    public void testYVariance() {

        

        GLSMultipleLinearRegression model = new GLSMultipleLinearRegression();
        model.newSampleData(y, x, omega);
        TestUtils.assertEquals(model.calculateYVariance(), 3.5, 0);
    }

// org.apache.commons.math.stat.regression.GLSMultipleLinearRegressionTest::testNewSample2
    public void testNewSample2() throws Exception {
        double[] y = new double[] {1, 2, 3, 4}; 
        double[][] x = new double[][] {
          {19, 22, 33},
          {20, 30, 40},
          {25, 35, 45},
          {27, 37, 47}   
        };
        double[][] covariance = MatrixUtils.createRealIdentityMatrix(4).scalarMultiply(2).getData();
        GLSMultipleLinearRegression regression = new GLSMultipleLinearRegression();
        regression.newSampleData(y, x, covariance);
        RealMatrix combinedX = regression.X.copy();
        RealVector combinedY = regression.Y.copy();
        RealMatrix combinedCovInv = regression.getOmegaInverse();
        regression.newXSampleData(x);
        regression.newYSampleData(y);
        Assert.assertEquals(combinedX, regression.X);
        Assert.assertEquals(combinedY, regression.Y);
        Assert.assertEquals(combinedCovInv, regression.getOmegaInverse());
    }

// org.apache.commons.math.stat.regression.GLSMultipleLinearRegressionTest::testGLSOLSConsistency
    public void testGLSOLSConsistency() throws Exception {      
        RealMatrix identityCov = MatrixUtils.createRealIdentityMatrix(16);
        GLSMultipleLinearRegression glsModel = new GLSMultipleLinearRegression();
        OLSMultipleLinearRegression olsModel = new OLSMultipleLinearRegression();
        glsModel.newSampleData(longley, 16, 6);
        olsModel.newSampleData(longley, 16, 6);
        glsModel.newCovarianceData(identityCov.getData());
        double[] olsBeta = olsModel.calculateBeta().toArray();
        double[] glsBeta = glsModel.calculateBeta().toArray();
        
        
        for (int i = 0; i < olsBeta.length; i++) {
            TestUtils.assertRelativelyEquals(olsBeta[i], glsBeta[i], 10E-7);
        }
    }

// org.apache.commons.math.stat.regression.GLSMultipleLinearRegressionTest::testGLSEfficiency
    public void testGLSEfficiency() throws Exception {
        RandomGenerator rg = new JDKRandomGenerator();
        rg.setSeed(200);  
        
        
        
        final int nObs = 16;
        double[] sigma = new double[nObs];
        for (int i = 0; i < nObs; i++) {
            sigma[i] = 10 * rg.nextDouble();
        }
        
        
        
        final int numSeeds = 1000;
        RealMatrix errorSeeds = MatrixUtils.createRealMatrix(numSeeds, nObs);
        for (int i = 0; i < numSeeds; i++) {
            for (int j = 0; j < nObs; j++) {
                errorSeeds.setEntry(i, j, rg.nextGaussian() * sigma[j]);
            }
        }
        
        
        RealMatrix cov = (new Covariance(errorSeeds)).getCovarianceMatrix();
          
        
        GaussianRandomGenerator rawGenerator = new GaussianRandomGenerator(rg);
        double[] errorMeans = new double[nObs];  
        CorrelatedRandomVectorGenerator gen = new CorrelatedRandomVectorGenerator(errorMeans, cov,
         1.0e-12 * cov.getNorm(), rawGenerator);
        
        
        
        
        
        OLSMultipleLinearRegression ols = new OLSMultipleLinearRegression();
        ols.newSampleData(longley, nObs, 6);
        final RealVector b = ols.calculateBeta().copy();
        final RealMatrix x = ols.X.copy();
        
        
        GLSMultipleLinearRegression gls = new GLSMultipleLinearRegression();
        gls.newSampleData(longley, nObs, 6);
        gls.newCovarianceData(cov.getData());
        
        
        DescriptiveStatistics olsBetaStats = new DescriptiveStatistics();
        DescriptiveStatistics glsBetaStats = new DescriptiveStatistics();
        
        
        
        final int nModels = 10000;
        for (int i = 0; i < nModels; i++) {
            
            
            RealVector u = MatrixUtils.createRealVector(gen.nextVector());
            double[] y = u.add(x.operate(b)).toArray();
            
            
            ols.newYSampleData(y);
            RealVector olsBeta = ols.calculateBeta();
            
            
            gls.newYSampleData(y);
            RealVector glsBeta = gls.calculateBeta();
            
            
            double dist = olsBeta.getDistance(b);
            olsBetaStats.addValue(dist * dist);
            dist = glsBeta.getDistance(b);
            glsBetaStats.addValue(dist * dist);
            
        }
        
        
        assert(olsBetaStats.getMean() > 1.5 * glsBetaStats.getMean());
        assert(olsBetaStats.getStandardDeviation() > glsBetaStats.getStandardDeviation());  
    }

// org.apache.commons.math.stat.regression.MillerUpdatingRegressionTest::testHasIntercept
    public void testHasIntercept() {
        MillerUpdatingRegression instance = new MillerUpdatingRegression(10, false);
        if (instance.hasIntercept()) {
            fail("Should not have intercept");
        }
        instance = new MillerUpdatingRegression(10, true);
        if (!instance.hasIntercept()) {
            fail("Should have intercept");
        }
    }

// org.apache.commons.math.stat.regression.MillerUpdatingRegressionTest::testAddObsGetNClear
    public void testAddObsGetNClear() {
        MillerUpdatingRegression instance = new MillerUpdatingRegression(3, true);
        double[][] xAll = new double[airdata[0].length][];
        double[] y = new double[airdata[0].length];
        for (int i = 0; i < airdata[0].length; i++) {
            xAll[i] = new double[3];
            xAll[i][0] = Math.log(airdata[3][i]);
            xAll[i][1] = Math.log(airdata[4][i]);
            xAll[i][2] = airdata[5][i];
            y[i] = Math.log(airdata[2][i]);
        }
        instance.addObservations(xAll, y);
        if (instance.getN() != xAll.length) {
            fail("Number of observations not correct in bulk addition");
        }
        instance.clear();
        for (int i = 0; i < xAll.length; i++) {
            instance.addObservation(xAll[i], y[i]);
        }
        if (instance.getN() != xAll.length) {
            fail("Number of observations not correct in drip addition");
        }
        return;
    }

// org.apache.commons.math.stat.regression.MillerUpdatingRegressionTest::testNegativeTestAddObs
    public void testNegativeTestAddObs() {
        MillerUpdatingRegression instance = new MillerUpdatingRegression(3, true);
        try {
            instance.addObservation(new double[]{1.0}, 0.0);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException iae) {
        } catch (Exception e) {
            fail("Should throw IllegalArgumentException");
        }
        try {
            instance.addObservation(new double[]{1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0}, 0.0);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException iae) {
        } catch (Exception e) {
            fail("Should throw IllegalArgumentException");
        }
        try {
            instance.addObservation(new double[]{1.0, 1.0, 1.0}, 0.0);
        } catch (Exception e) {
            fail("Should throw IllegalArgumentException");
        }

        
        instance = new MillerUpdatingRegression(3, false);
        try {
            instance.addObservation(new double[]{1.0}, 0.0);
            fail("Should throw IllegalArgumentException [NOINTERCEPT]");
        } catch (IllegalArgumentException iae) {
        } catch (Exception e) {
            fail("Should throw IllegalArgumentException [NOINTERCEPT]");
        }
        try {
            instance.addObservation(new double[]{1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0}, 0.0);
            fail("Should throw IllegalArgumentException [NOINTERCEPT]");
        } catch (IllegalArgumentException iae) {
        } catch (Exception e) {
            fail("Should throw IllegalArgumentException [NOINTERCEPT]");
        }
        try {
            instance.addObservation(new double[]{1.0, 1.0, 1.0}, 0.0);
        } catch (Exception e) {
            fail("Should throw IllegalArgumentException [NOINTERCEPT]");
        }
    }

// org.apache.commons.math.stat.regression.MillerUpdatingRegressionTest::testNegativeTestAddMultipleObs
    public void testNegativeTestAddMultipleObs() {
        MillerUpdatingRegression instance = new MillerUpdatingRegression(3, true);
        try {
            double[][] tst = {{1.0, 1.0, 1.0}, {1.20, 2.0, 2.1}};
            double[] y = {1.0};
            instance.addObservations(tst, y);

            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException iae) {
        } catch (Exception e) {
            fail("Should throw IllegalArgumentException");
        }

        try {
            double[][] tst = {{1.0, 1.0, 1.0}, {1.20, 2.0, 2.1}};
            double[] y = {1.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0};
            instance.addObservations(tst, y);

            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException iae) {
        } catch (Exception e) {
            fail("Should throw IllegalArgumentException");
        }
    }

// org.apache.commons.math.stat.regression.MillerUpdatingRegressionTest::testRegressAirlineConstantExternal
    public void testRegressAirlineConstantExternal() {
        MillerUpdatingRegression instance = new MillerUpdatingRegression(4, false);
        double[][] x = new double[airdata[0].length][];
        double[] y = new double[airdata[0].length];
        for (int i = 0; i < airdata[0].length; i++) {
            x[i] = new double[4];
            x[i][0] = 1.0;
            x[i][1] = Math.log(airdata[3][i]);
            x[i][2] = Math.log(airdata[4][i]);
            x[i][3] = airdata[5][i];
            y[i] = Math.log(airdata[2][i]);
        }

        instance.addObservations(x, y);
        try {
            RegressionResults result = instance.regress();
            if (result == null) {
                fail("The test case is a prototype.");
            }
            TestUtils.assertEquals(
                    new double[]{9.5169, 0.8827, 0.4540, -1.6275},
                    result.getParameterEstimates(), 1e-4);

            TestUtils.assertEquals(
                    new double[]{.2292445, .0132545, .0203042, .345302},
                    result.getStdErrorOfEstimates(), 1.0e-4);

            TestUtils.assertEquals(0.01552839, result.getMeanSquareError(), 1.0e-8);
        } catch (Exception e) {
            fail("Should not throw exception but does");
        }
    }

// org.apache.commons.math.stat.regression.MillerUpdatingRegressionTest::testRegressAirlineConstantInternal
    public void testRegressAirlineConstantInternal() {
        MillerUpdatingRegression instance = new MillerUpdatingRegression(3, true);
        double[][] x = new double[airdata[0].length][];
        double[] y = new double[airdata[0].length];
        for (int i = 0; i < airdata[0].length; i++) {
            x[i] = new double[3];
            x[i][0] = Math.log(airdata[3][i]);
            x[i][1] = Math.log(airdata[4][i]);
            x[i][2] = airdata[5][i];
            y[i] = Math.log(airdata[2][i]);
        }

        instance.addObservations(x, y);
        try {
            RegressionResults result = instance.regress();
            if (result == null) {
                fail("The test case is a prototype.");
            }
            TestUtils.assertEquals(
                    new double[]{9.5169, 0.8827, 0.4540, -1.6275},
                    result.getParameterEstimates(), 1e-4);

            TestUtils.assertEquals(
                    new double[]{.2292445, .0132545, .0203042, .345302},
                    result.getStdErrorOfEstimates(), 1.0e-4);

            TestUtils.assertEquals(0.9883, result.getRSquared(), 1.0e-4);
            TestUtils.assertEquals(0.01552839, result.getMeanSquareError(), 1.0e-8);
        } catch (Exception e) {
            fail("Should not throw exception but does");
        }
    }

// org.apache.commons.math.stat.regression.MillerUpdatingRegressionTest::testFilippelli
    public void testFilippelli() throws MathException {
        double[] data = new double[]{
            0.8116, -6.860120914,
            0.9072, -4.324130045,
            0.9052, -4.358625055,
            0.9039, -4.358426747,
            0.8053, -6.955852379,
            0.8377, -6.661145254,
            0.8667, -6.355462942,
            0.8809, -6.118102026,
            0.7975, -7.115148017,
            0.8162, -6.815308569,
            0.8515, -6.519993057,
            0.8766, -6.204119983,
            0.8885, -5.853871964,
            0.8859, -6.109523091,
            0.8959, -5.79832982,
            0.8913, -5.482672118,
            0.8959, -5.171791386,
            0.8971, -4.851705903,
            0.9021, -4.517126416,
            0.909, -4.143573228,
            0.9139, -3.709075441,
            0.9199, -3.499489089,
            0.8692, -6.300769497,
            0.8872, -5.953504836,
            0.89, -5.642065153,
            0.891, -5.031376979,
            0.8977, -4.680685696,
            0.9035, -4.329846955,
            0.9078, -3.928486195,
            0.7675, -8.56735134,
            0.7705, -8.363211311,
            0.7713, -8.107682739,
            0.7736, -7.823908741,
            0.7775, -7.522878745,
            0.7841, -7.218819279,
            0.7971, -6.920818754,
            0.8329, -6.628932138,
            0.8641, -6.323946875,
            0.8804, -5.991399828,
            0.7668, -8.781464495,
            0.7633, -8.663140179,
            0.7678, -8.473531488,
            0.7697, -8.247337057,
            0.77, -7.971428747,
            0.7749, -7.676129393,
            0.7796, -7.352812702,
            0.7897, -7.072065318,
            0.8131, -6.774174009,
            0.8498, -6.478861916,
            0.8741, -6.159517513,
            0.8061, -6.835647144,
            0.846, -6.53165267,
            0.8751, -6.224098421,
            0.8856, -5.910094889,
            0.8919, -5.598599459,
            0.8934, -5.290645224,
            0.894, -4.974284616,
            0.8957, -4.64454848,
            0.9047, -4.290560426,
            0.9129, -3.885055584,
            0.9209, -3.408378962,
            0.9219, -3.13200249,
            0.7739, -8.726767166,
            0.7681, -8.66695597,
            0.7665, -8.511026475,
            0.7703, -8.165388579,
            0.7702, -7.886056648,
            0.7761, -7.588043762,
            0.7809, -7.283412422,
            0.7961, -6.995678626,
            0.8253, -6.691862621,
            0.8602, -6.392544977,
            0.8809, -6.067374056,
            0.8301, -6.684029655,
            0.8664, -6.378719832,
            0.8834, -6.065855188,
            0.8898, -5.752272167,
            0.8964, -5.132414673,
            0.8963, -4.811352704,
            0.9074, -4.098269308,
            0.9119, -3.66174277,
            0.9228, -3.2644011
        };
        MillerUpdatingRegression model = new MillerUpdatingRegression(10, true);
        int off = 0;
        double[] tmp = new double[10];
        int nobs = 82;
        for (int i = 0; i < nobs; i++) {
            tmp[0] = data[off + 1];

            tmp[1] = tmp[0] * tmp[0];
            tmp[2] = tmp[0] * tmp[1];
            tmp[3] = tmp[0] * tmp[2];
            tmp[4] = tmp[0] * tmp[3];
            tmp[5] = tmp[0] * tmp[4];
            tmp[6] = tmp[0] * tmp[5];
            tmp[7] = tmp[0] * tmp[6];
            tmp[8] = tmp[0] * tmp[7];
            tmp[9] = tmp[0] * tmp[8];
            model.addObservation(tmp, data[off]);
            off += 2;
        }
        RegressionResults result = model.regress();
        double[] betaHat = result.getParameterEstimates();
        TestUtils.assertEquals(betaHat,
                new double[]{
                    -1467.48961422980,
                    -2772.17959193342,
                    -2316.37108160893,
                    -1127.97394098372,
                    -354.478233703349,
                    -75.1242017393757,
                    -10.8753180355343,
                    -1.06221498588947,
                    -0.670191154593408E-01,
                    -0.246781078275479E-02,
                    -0.402962525080404E-04
                }, 1E-5); 

        double[] se = result.getStdErrorOfEstimates();
        TestUtils.assertEquals(se,
                new double[]{
                    298.084530995537,
                    559.779865474950,
                    466.477572127796,
                    227.204274477751,
                    71.6478660875927,
                    15.2897178747400,
                    2.23691159816033,
                    0.221624321934227,
                    0.142363763154724E-01,
                    0.535617408889821E-03,
                    0.896632837373868E-05
                }, 1E-5); 

        TestUtils.assertEquals(0.996727416185620, result.getRSquared(), 1.0e-8);
        TestUtils.assertEquals(0.112091743968020E-04, result.getMeanSquareError(), 1.0e-10);
        TestUtils.assertEquals(0.795851382172941E-03, result.getErrorSumSquares(), 1.0e-10);

    }

// org.apache.commons.math.stat.regression.MillerUpdatingRegressionTest::testWampler1
    public void testWampler1() throws MathException {
        double[] data = new double[]{
            1, 0,
            6, 1,
            63, 2,
            364, 3,
            1365, 4,
            3906, 5,
            9331, 6,
            19608, 7,
            37449, 8,
            66430, 9,
            111111, 10,
            177156, 11,
            271453, 12,
            402234, 13,
            579195, 14,
            813616, 15,
            1118481, 16,
            1508598, 17,
            2000719, 18,
            2613660, 19,
            3368421, 20};

        MillerUpdatingRegression model = new MillerUpdatingRegression(5, true);
        int off = 0;
        double[] tmp = new double[5];
        int nobs = 21;
        for (int i = 0; i < nobs; i++) {
            tmp[0] = data[off + 1];
            tmp[1] = tmp[0] * tmp[0];
            tmp[2] = tmp[0] * tmp[1];
            tmp[3] = tmp[0] * tmp[2];
            tmp[4] = tmp[0] * tmp[3];
            model.addObservation(tmp, data[off]);
            off += 2;
        }
        RegressionResults result = model.regress();
        double[] betaHat = result.getParameterEstimates();
        TestUtils.assertEquals(betaHat,
                new double[]{1.0,
                    1.0, 1.0,
                    1.0, 1.0,
                    1.0}, 1E-8); 

        double[] se = result.getStdErrorOfEstimates();
        TestUtils.assertEquals(se,
                new double[]{0.0,
                    0.0, 0.0,
                    0.0, 0.0,
                    0.0}, 1E-8); 

        TestUtils.assertEquals(1.0, result.getRSquared(), 1.0e-10);
        TestUtils.assertEquals(0, result.getMeanSquareError(), 1.0e-7);
        TestUtils.assertEquals(0.00, result.getErrorSumSquares(), 1.0e-6);

        return;
    }

// org.apache.commons.math.stat.regression.MillerUpdatingRegressionTest::testWampler2
    public void testWampler2() throws MathException {
        double[] data = new double[]{
            1.00000, 0,
            1.11111, 1,
            1.24992, 2,
            1.42753, 3,
            1.65984, 4,
            1.96875, 5,
            2.38336, 6,
            2.94117, 7,
            3.68928, 8,
            4.68559, 9,
            6.00000, 10,
            7.71561, 11,
            9.92992, 12,
            12.75603, 13,
            16.32384, 14,
            20.78125, 15,
            26.29536, 16,
            33.05367, 17,
            41.26528, 18,
            51.16209, 19,
            63.00000, 20};

        MillerUpdatingRegression model = new MillerUpdatingRegression(5, true);
        int off = 0;
        double[] tmp = new double[5];
        int nobs = 21;
        for (int i = 0; i < nobs; i++) {
            tmp[0] = data[off + 1];
            tmp[1] = tmp[0] * tmp[0];
            tmp[2] = tmp[0] * tmp[1];
            tmp[3] = tmp[0] * tmp[2];
            tmp[4] = tmp[0] * tmp[3];
            model.addObservation(tmp, data[off]);
            off += 2;
        }
        RegressionResults result = model.regress();
        double[] betaHat = result.getParameterEstimates();
        TestUtils.assertEquals(betaHat,
                new double[]{1.0,
                    1.0e-1, 1.0e-2,
                    1.0e-3, 1.0e-4,
                    1.0e-5}, 1E-8); 

        double[] se = result.getStdErrorOfEstimates();
        TestUtils.assertEquals(se,
                new double[]{0.0,
                    0.0, 0.0,
                    0.0, 0.0,
                    0.0}, 1E-8); 

        TestUtils.assertEquals(1.0, result.getRSquared(), 1.0e-10);
        TestUtils.assertEquals(0, result.getMeanSquareError(), 1.0e-7);
        TestUtils.assertEquals(0.00, result.getErrorSumSquares(), 1.0e-6);
        return;
    }

// org.apache.commons.math.stat.regression.MillerUpdatingRegressionTest::testWampler3
    public void testWampler3() throws MathException {
        double[] data = new double[]{
            760, 0,
            -2042, 1,
            2111, 2,
            -1684, 3,
            3888, 4,
            1858, 5,
            11379, 6,
            17560, 7,
            39287, 8,
            64382, 9,
            113159, 10,
            175108, 11,
            273291, 12,
            400186, 13,
            581243, 14,
            811568, 15,
            1121004, 16,
            1506550, 17,
            2002767, 18,
            2611612, 19,
            3369180, 20};
        MillerUpdatingRegression model = new MillerUpdatingRegression(5, true);
        int off = 0;
        double[] tmp = new double[5];
        int nobs = 21;
        for (int i = 0; i < nobs; i++) {
            tmp[0] = data[off + 1];
            tmp[1] = tmp[0] * tmp[0];
            tmp[2] = tmp[0] * tmp[1];
            tmp[3] = tmp[0] * tmp[2];
            tmp[4] = tmp[0] * tmp[3];
            model.addObservation(tmp, data[off]);
            off += 2;
        }
        RegressionResults result = model.regress();
        double[] betaHat = result.getParameterEstimates();
        TestUtils.assertEquals(betaHat,
                new double[]{1.0,
                    1.0, 1.0,
                    1.0, 1.0,
                    1.0}, 1E-8); 
        double[] se = result.getStdErrorOfEstimates();
        TestUtils.assertEquals(se,
                new double[]{2152.32624678170,
                    2363.55173469681, 779.343524331583,
                    101.475507550350, 5.64566512170752,
                    0.112324854679312}, 1E-8); 

        TestUtils.assertEquals(.999995559025820, result.getRSquared(), 1.0e-10);
        TestUtils.assertEquals(5570284.53333333, result.getMeanSquareError(), 1.0e-7);
        TestUtils.assertEquals(83554268.0000000, result.getErrorSumSquares(), 1.0e-6);
        return;
    }

// org.apache.commons.math.stat.regression.MillerUpdatingRegressionTest::testWampler4
    public void testWampler4() throws MathException {
        double[] data = new double[]{
            75901, 0,
            -204794, 1,
            204863, 2,
            -204436, 3,
            253665, 4,
            -200894, 5,
            214131, 6,
            -185192, 7,
            221249, 8,
            -138370, 9,
            315911, 10,
            -27644, 11,
            455253, 12,
            197434, 13,
            783995, 14,
            608816, 15,
            1370781, 16,
            1303798, 17,
            2205519, 18,
            2408860, 19,
            3444321, 20};
        MillerUpdatingRegression model = new MillerUpdatingRegression(5, true);
        int off = 0;
        double[] tmp = new double[5];
        int nobs = 21;
        for (int i = 0; i < nobs; i++) {
            tmp[0] = data[off + 1];
            tmp[1] = tmp[0] * tmp[0];
            tmp[2] = tmp[0] * tmp[1];
            tmp[3] = tmp[0] * tmp[2];
            tmp[4] = tmp[0] * tmp[3];
            model.addObservation(tmp, data[off]);
            off += 2;
        }
        RegressionResults result = model.regress();
        double[] betaHat = result.getParameterEstimates();
        TestUtils.assertEquals(betaHat,
                new double[]{1.0,
                    1.0, 1.0,
                    1.0, 1.0,
                    1.0}, 1E-8); 

        double[] se = result.getStdErrorOfEstimates();
        TestUtils.assertEquals(se,
                new double[]{215232.624678170,
                    236355.173469681, 77934.3524331583,
                    10147.5507550350, 564.566512170752,
                    11.2324854679312}, 1E-8); 

        TestUtils.assertEquals(.957478440825662, result.getRSquared(), 1.0e-10);
        TestUtils.assertEquals(55702845333.3333, result.getMeanSquareError(), 1.0e-4);
        TestUtils.assertEquals(835542680000.000, result.getErrorSumSquares(), 1.0e-3);

        return;
    }

// org.apache.commons.math.stat.regression.MillerUpdatingRegressionTest::testLongly
    public void testLongly() throws Exception {
        
        
        double[] design = new double[]{
            60323, 83.0, 234289, 2356, 1590, 107608, 1947,
            61122, 88.5, 259426, 2325, 1456, 108632, 1948,
            60171, 88.2, 258054, 3682, 1616, 109773, 1949,
            61187, 89.5, 284599, 3351, 1650, 110929, 1950,
            63221, 96.2, 328975, 2099, 3099, 112075, 1951,
            63639, 98.1, 346999, 1932, 3594, 113270, 1952,
            64989, 99.0, 365385, 1870, 3547, 115094, 1953,
            63761, 100.0, 363112, 3578, 3350, 116219, 1954,
            66019, 101.2, 397469, 2904, 3048, 117388, 1955,
            67857, 104.6, 419180, 2822, 2857, 118734, 1956,
            68169, 108.4, 442769, 2936, 2798, 120445, 1957,
            66513, 110.8, 444546, 4681, 2637, 121950, 1958,
            68655, 112.6, 482704, 3813, 2552, 123366, 1959,
            69564, 114.2, 502601, 3931, 2514, 125368, 1960,
            69331, 115.7, 518173, 4806, 2572, 127852, 1961,
            70551, 116.9, 554894, 4007, 2827, 130081, 1962
        };

        final int nobs = 16;
        final int nvars = 6;

        
        MillerUpdatingRegression model = new MillerUpdatingRegression(6, true);
        int off = 0;
        double[] tmp = new double[6];
        for (int i = 0; i < nobs; i++) {
            System.arraycopy(design, off + 1, tmp, 0, nvars);
            model.addObservation(tmp, design[off]);
            off += nvars + 1;
        }

        
        RegressionResults result = model.regress();
        double[] betaHat = result.getParameterEstimates();
        TestUtils.assertEquals(betaHat,
                new double[]{-3482258.63459582, 15.0618722713733,
                    -0.358191792925910E-01, -2.02022980381683,
                    -1.03322686717359, -0.511041056535807E-01,
                    1829.15146461355}, 1E-8); 

        
        double[] errors = result.getStdErrorOfEstimates();
        TestUtils.assertEquals(new double[]{890420.383607373,
                    84.9149257747669,
                    0.334910077722432E-01,
                    0.488399681651699,
                    0.214274163161675,
                    0.226073200069370,
                    455.478499142212}, errors, 1E-6);

        
        TestUtils.assertEquals(0.995479004577296, result.getRSquared(), 1E-12);
        TestUtils.assertEquals(0.992465007628826, result.getAdjustedRSquared(), 1E-12);

        model = new MillerUpdatingRegression(6, false);
        off = 0;
        for (int i = 0; i < nobs; i++) {
            System.arraycopy(design, off + 1, tmp, 0, nvars);
            model.addObservation(tmp, design[off]);
            off += nvars + 1;
        }
        
        result = model.regress();
        betaHat = result.getParameterEstimates();
        TestUtils.assertEquals(betaHat,
                new double[]{-52.99357013868291, 0.07107319907358,
                    -0.42346585566399, -0.57256866841929,
                    -0.41420358884978, 48.41786562001326}, 1E-11);

        
        errors = result.getStdErrorOfEstimates();
        TestUtils.assertEquals(new double[]{129.54486693117232, 0.03016640003786,
                    0.41773654056612, 0.27899087467676, 0.32128496193363,
                    17.68948737819961}, errors, 1E-11);

        TestUtils.assertEquals(0.9999670130706, result.getRSquared(), 1E-12);
        TestUtils.assertEquals(0.999947220913, result.getAdjustedRSquared(), 1E-12);

    }

// org.apache.commons.math.stat.regression.MillerUpdatingRegressionTest::testOneRedundantColumn
    public void testOneRedundantColumn() throws MathException {
        MillerUpdatingRegression instance = new MillerUpdatingRegression(4, false);
        MillerUpdatingRegression instance2 = new MillerUpdatingRegression(5, false);
        double[][] x = new double[airdata[0].length][];
        double[][] x2 = new double[airdata[0].length][];
        double[] y = new double[airdata[0].length];
        for (int i = 0; i < airdata[0].length; i++) {
            x[i] = new double[4];
            x2[i] = new double[5];
            x[i][0] = 1.0;
            x[i][1] = Math.log(airdata[3][i]);
            x[i][2] = Math.log(airdata[4][i]);
            x[i][3] = airdata[5][i];

            x2[i][0] = x[i][0];
            x2[i][1] = x[i][1];
            x2[i][2] = x[i][2];
            x2[i][3] = x[i][3];
            x2[i][4] = x[i][3];

            y[i] = Math.log(airdata[2][i]);
        }

        instance.addObservations(x, y);
        RegressionResults result = instance.regress();
        if (result == null) {
            fail("Could not estimate initial regression");
        }

        instance2.addObservations(x2, y);
        RegressionResults resultRedundant = instance2.regress();
        if (resultRedundant == null) {
            fail("Could not estimate redundant regression");
        }
        double[] beta = result.getParameterEstimates();
        double[] betar = resultRedundant.getParameterEstimates();
        double[] se = result.getStdErrorOfEstimates();
        double[] ser = resultRedundant.getStdErrorOfEstimates();

        for (int i = 0; i < beta.length; i++) {
            if (Math.abs(beta[i] - betar[i]) > 1.0e-8) {
                fail("Parameters not correctly estimated");
            }
            if (Math.abs(se[i] - ser[i]) > 1.0e-8) {
                fail("Standard errors not correctly estimated");
            }
            for (int j = 0; j < i; j++) {
                if (Math.abs(result.getCovarianceOfParameters(i, j)
                        - resultRedundant.getCovarianceOfParameters(i, j)) > 1.0e-8) {
                    fail("Variance Covariance not correct");
                }
            }
        }

        TestUtils.assertEquals(result.getAdjustedRSquared(), resultRedundant.getAdjustedRSquared(), 1.0e-8);
        TestUtils.assertEquals(result.getErrorSumSquares(), resultRedundant.getErrorSumSquares(), 1.0e-8);
        TestUtils.assertEquals(result.getMeanSquareError(), resultRedundant.getMeanSquareError(), 1.0e-8);
        TestUtils.assertEquals(result.getRSquared(), resultRedundant.getRSquared(), 1.0e-8);
        return;
    }

// org.apache.commons.math.stat.regression.MillerUpdatingRegressionTest::testThreeRedundantColumn
    public void testThreeRedundantColumn() throws MathException {

        MillerUpdatingRegression instance = new MillerUpdatingRegression(4, false);
        MillerUpdatingRegression instance2 = new MillerUpdatingRegression(7, false);
        double[][] x = new double[airdata[0].length][];
        double[][] x2 = new double[airdata[0].length][];
        double[] y = new double[airdata[0].length];
        for (int i = 0; i < airdata[0].length; i++) {
            x[i] = new double[4];
            x2[i] = new double[7];
            x[i][0] = 1.0;
            x[i][1] = Math.log(airdata[3][i]);
            x[i][2] = Math.log(airdata[4][i]);
            x[i][3] = airdata[5][i];

            x2[i][0] = x[i][0];
            x2[i][1] = x[i][0];
            x2[i][2] = x[i][1];
            x2[i][3] = x[i][2];
            x2[i][4] = x[i][1];
            x2[i][5] = x[i][3];
            x2[i][6] = x[i][2];

            y[i] = Math.log(airdata[2][i]);
        }

        instance.addObservations(x, y);
        RegressionResults result = instance.regress();
        if (result == null) {
            fail("Could not estimate initial regression");
        }

        instance2.addObservations(x2, y);
        RegressionResults resultRedundant = instance2.regress();
        if (resultRedundant == null) {
            fail("Could not estimate redundant regression");
        }
        double[] beta = result.getParameterEstimates();
        double[] betar = resultRedundant.getParameterEstimates();
        double[] se = result.getStdErrorOfEstimates();
        double[] ser = resultRedundant.getStdErrorOfEstimates();

        if (Math.abs(beta[0] - betar[0]) > 1.0e-8) {
            fail("Parameters not correct after reorder (0,3)");
        }
        if (Math.abs(beta[1] - betar[2]) > 1.0e-8) {
            fail("Parameters not correct after reorder (1,2)");
        }
        if (Math.abs(beta[2] - betar[3]) > 1.0e-8) {
            fail("Parameters not correct after reorder (2,1)");
        }
        if (Math.abs(beta[3] - betar[5]) > 1.0e-8) {
            fail("Parameters not correct after reorder (3,0)");
        }

        if (Math.abs(se[0] - ser[0]) > 1.0e-8) {
            fail("Se not correct after reorder (0,3)");
        }
        if (Math.abs(se[1] - ser[2]) > 1.0e-8) {
            fail("Se not correct after reorder (1,2)");
        }
        if (Math.abs(se[2] - ser[3]) > 1.0e-8) {
            fail("Se not correct after reorder (2,1)");
        }
        if (Math.abs(se[3] - ser[5]) > 1.0e-8) {
            fail("Se not correct after reorder (3,0)");
        }

        if (Math.abs(result.getCovarianceOfParameters(0, 0)
                - resultRedundant.getCovarianceOfParameters(0, 0)) > 1.0e-8) {
            fail("VCV not correct after reorder (0,0)");
        }
        if (Math.abs(result.getCovarianceOfParameters(0, 1)
                - resultRedundant.getCovarianceOfParameters(0, 2)) > 1.0e-8) {
            fail("VCV not correct after reorder (0,1)<->(0,2)");
        }
        if (Math.abs(result.getCovarianceOfParameters(0, 2)
                - resultRedundant.getCovarianceOfParameters(0, 3)) > 1.0e-8) {
            fail("VCV not correct after reorder (0,2)<->(0,1)");
        }
        if (Math.abs(result.getCovarianceOfParameters(0, 3)
                - resultRedundant.getCovarianceOfParameters(0, 5)) > 1.0e-8) {
            fail("VCV not correct after reorder (0,3)<->(0,3)");
        }
        if (Math.abs(result.getCovarianceOfParameters(1, 0)
                - resultRedundant.getCovarianceOfParameters(2, 0)) > 1.0e-8) {
            fail("VCV not correct after reorder (1,0)<->(2,0)");
        }
        if (Math.abs(result.getCovarianceOfParameters(1, 1)
                - resultRedundant.getCovarianceOfParameters(2, 2)) > 1.0e-8) {
            fail("VCV not correct  (1,1)<->(2,1)");
        }
        if (Math.abs(result.getCovarianceOfParameters(1, 2)
                - resultRedundant.getCovarianceOfParameters(2, 3)) > 1.0e-8) {
            fail("VCV not correct  (1,2)<->(2,2)");
        }

        if (Math.abs(result.getCovarianceOfParameters(2, 0)
                - resultRedundant.getCovarianceOfParameters(3, 0)) > 1.0e-8) {
            fail("VCV not correct  (2,0)<->(1,0)");
        }
        if (Math.abs(result.getCovarianceOfParameters(2, 1)
                - resultRedundant.getCovarianceOfParameters(3, 2)) > 1.0e-8) {
            fail("VCV not correct  (2,1)<->(1,2)");
        }

        if (Math.abs(result.getCovarianceOfParameters(3, 3)
                - resultRedundant.getCovarianceOfParameters(5, 5)) > 1.0e-8) {
            fail("VCV not correct  (3,3)<->(3,2)");
        }

        TestUtils.assertEquals(result.getAdjustedRSquared(), resultRedundant.getAdjustedRSquared(), 1.0e-8);
        TestUtils.assertEquals(result.getErrorSumSquares(), resultRedundant.getErrorSumSquares(), 1.0e-8);
        TestUtils.assertEquals(result.getMeanSquareError(), resultRedundant.getMeanSquareError(), 1.0e-8);
        TestUtils.assertEquals(result.getRSquared(), resultRedundant.getRSquared(), 1.0e-8);
        return;
    }

// org.apache.commons.math.stat.regression.MillerUpdatingRegressionTest::testPCorr
    public void testPCorr() {
        MillerUpdatingRegression instance = new MillerUpdatingRegression(4, false);
        double[][] x = new double[airdata[0].length][];
        double[] y = new double[airdata[0].length];
        double[] cp = new double[10];
        double[] yxcorr = new double[4];
        double[] diag = new double[4];
        double sumysq = 0.0;
        int off = 0;
        for (int i = 0; i < airdata[0].length; i++) {
            x[i] = new double[4];
            x[i][0] = 1.0;
            x[i][1] = Math.log(airdata[3][i]);
            x[i][2] = Math.log(airdata[4][i]);
            x[i][3] = airdata[5][i];
            y[i] = Math.log(airdata[2][i]);
            off = 0;
            for (int j = 0; j < 4; j++) {
                double tmp = x[i][j];
                for (int k = 0; k <= j; k++, off++) {
                    cp[off] += tmp * x[i][k];
                }
                yxcorr[j] += tmp * y[i];
            }
            sumysq += y[i] * y[i];
        }
        PearsonsCorrelation pearson = new PearsonsCorrelation(x);
        RealMatrix corr = pearson.getCorrelationMatrix();
        off = 0;
        for (int i = 0; i < 4; i++, off += (i + 1)) {
            diag[i] = FastMath.sqrt(cp[off]);
        }

        instance.addObservations(x, y);
        double[] pc = instance.getPartialCorrelations(0);
        int idx = 0;
        off = 0;
        int off2 = 6;
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < i; j++) {
                if (Math.abs(pc[idx] - cp[off] / (diag[i] * diag[j])) > 1.0e-8) {
                    fail("Failed cross products... i = " + i + " j = " + j);
                }
                ++idx;
                ++off;
            }
            ++off;
            if (Math.abs(pc[i+off2] - yxcorr[ i] / (FastMath.sqrt(sumysq) * diag[i])) > 1.0e-8) {
                fail("failed cross product i = " + i + " y");
            }
        }
        double[] pc2 = instance.getPartialCorrelations(1);

        idx = 0;

        for (int i = 1; i < 4; i++) {
            for (int j = 1; j < i; j++) {
                if (Math.abs(pc2[idx] - corr.getEntry(j, i)) > 1.0e-8) {
                    fail("Failed cross products... i = " + i + " j = " + j);
                }
                ++idx;
            }
        }
        double[] pc3 = instance.getPartialCorrelations(2);
        if (pc3 == null) {
            fail("Should not be null");
        }
        return;
    }

// org.apache.commons.math.stat.regression.MillerUpdatingRegressionTest::testHdiag
    public void testHdiag() {
        MillerUpdatingRegression instance = new MillerUpdatingRegression(4, false);
        double[][] x = new double[airdata[0].length][];
        double[] y = new double[airdata[0].length];
        for (int i = 0; i < airdata[0].length; i++) {
            x[i] = new double[4];
            x[i][0] = 1.0;
            x[i][1] = Math.log(airdata[3][i]);
            x[i][2] = Math.log(airdata[4][i]);
            x[i][3] = airdata[5][i];
            y[i] = Math.log(airdata[2][i]);
        }
        instance.addObservations(x, y);
        OLSMultipleLinearRegression ols = new OLSMultipleLinearRegression();
        ols.setNoIntercept(true);
        ols.newSampleData(y, x);

        RealMatrix rm = ols.calculateHat();
        for (int i = 0; i < x.length; i++) {
            TestUtils.assertEquals(instance.getDiagonalOfHatMatrix(x[i]), rm.getEntry(i, i), 1.0e-8);
        }
        return;
    }

// org.apache.commons.math.stat.regression.MillerUpdatingRegressionTest::testHdiagConstant
    public void testHdiagConstant() {
        MillerUpdatingRegression instance = new MillerUpdatingRegression(3, true);
        double[][] x = new double[airdata[0].length][];
        double[] y = new double[airdata[0].length];
        for (int i = 0; i < airdata[0].length; i++) {
            x[i] = new double[3];
            x[i][0] = Math.log(airdata[3][i]);
            x[i][1] = Math.log(airdata[4][i]);
            x[i][2] = airdata[5][i];
            y[i] = Math.log(airdata[2][i]);
        }
        instance.addObservations(x, y);
        OLSMultipleLinearRegression ols = new OLSMultipleLinearRegression();
        ols.setNoIntercept(false);
        ols.newSampleData(y, x);

        RealMatrix rm = ols.calculateHat();
        for (int i = 0; i < x.length; i++) {
            TestUtils.assertEquals(instance.getDiagonalOfHatMatrix(x[i]), rm.getEntry(i, i), 1.0e-8);
        }
        return;
    }

// org.apache.commons.math.stat.regression.MillerUpdatingRegressionTest::testSubsetRegression
    public void testSubsetRegression() throws MathException {
        
        MillerUpdatingRegression instance = new MillerUpdatingRegression(3, true);
        MillerUpdatingRegression redRegression = new MillerUpdatingRegression(2, true);
        double[][] x = new double[airdata[0].length][];
        double[][] xReduced = new double[airdata[0].length][];
        double[] y = new double[airdata[0].length];
        for (int i = 0; i < airdata[0].length; i++) {
            x[i] = new double[3];
            x[i][0] = Math.log(airdata[3][i]);
            x[i][1] = Math.log(airdata[4][i]);
            x[i][2] = airdata[5][i];
            
            xReduced[i] = new double[2];
            xReduced[i][0] = Math.log(airdata[3][i]);
            xReduced[i][1] = Math.log(airdata[4][i]);
            
            y[i] = Math.log(airdata[2][i]);
        }

        instance.addObservations(x, y);
        redRegression.addObservations(xReduced, y);
        
        RegressionResults resultsInstance = instance.regress( new int[]{0,1,2} );
        RegressionResults resultsReduced = redRegression.regress();
        
        TestUtils.assertEquals(resultsInstance.getParameterEstimates(), resultsReduced.getParameterEstimates(), 1.0e-12);
        TestUtils.assertEquals(resultsInstance.getStdErrorOfEstimates(), resultsReduced.getStdErrorOfEstimates(), 1.0e-12);
    }

// org.apache.commons.math.stat.regression.OLSMultipleLinearRegressionTest::cannotAddSampleDataWithSizeMismatch
    public void cannotAddSampleDataWithSizeMismatch() {
        double[] y = new double[]{1.0, 2.0};
        double[][] x = new double[1][];
        x[0] = new double[]{1.0, 0};
        createRegression().newSampleData(y, x);
    }

// org.apache.commons.math.stat.regression.OLSMultipleLinearRegressionTest::testPerfectFit
    public void testPerfectFit() throws Exception {
        double[] betaHat = regression.estimateRegressionParameters();
        TestUtils.assertEquals(betaHat,
                               new double[]{ 11.0, 1.0 / 2.0, 2.0 / 3.0, 3.0 / 4.0, 4.0 / 5.0, 5.0 / 6.0 },
                               1e-14);
        double[] residuals = regression.estimateResiduals();
        TestUtils.assertEquals(residuals, new double[]{0d,0d,0d,0d,0d,0d},
                               1e-14);
        RealMatrix errors =
            new Array2DRowRealMatrix(regression.estimateRegressionParametersVariance(), false);
        final double[] s = { 1.0, -1.0 /  2.0, -1.0 /  3.0, -1.0 /  4.0, -1.0 /  5.0, -1.0 /  6.0 };
        RealMatrix referenceVariance = new Array2DRowRealMatrix(s.length, s.length);
        referenceVariance.walkInOptimizedOrder(new DefaultRealMatrixChangingVisitor() {
            @Override
            public double visit(int row, int column, double value) {
                if (row == 0) {
                    return s[column];
                }
                double x = s[row] * s[column];
                return (row == column) ? 2 * x : x;
            }
        });
       Assert.assertEquals(0.0,
                     errors.subtract(referenceVariance).getNorm(),
                     5.0e-16 * referenceVariance.getNorm());
       Assert.assertEquals(1, ((OLSMultipleLinearRegression) regression).calculateRSquared(), 1E-12);
    }

// org.apache.commons.math.stat.regression.OLSMultipleLinearRegressionTest::testLongly
    public void testLongly() throws Exception {
        
        
        double[] design = new double[] {
            60323,83.0,234289,2356,1590,107608,1947,
            61122,88.5,259426,2325,1456,108632,1948,
            60171,88.2,258054,3682,1616,109773,1949,
            61187,89.5,284599,3351,1650,110929,1950,
            63221,96.2,328975,2099,3099,112075,1951,
            63639,98.1,346999,1932,3594,113270,1952,
            64989,99.0,365385,1870,3547,115094,1953,
            63761,100.0,363112,3578,3350,116219,1954,
            66019,101.2,397469,2904,3048,117388,1955,
            67857,104.6,419180,2822,2857,118734,1956,
            68169,108.4,442769,2936,2798,120445,1957,
            66513,110.8,444546,4681,2637,121950,1958,
            68655,112.6,482704,3813,2552,123366,1959,
            69564,114.2,502601,3931,2514,125368,1960,
            69331,115.7,518173,4806,2572,127852,1961,
            70551,116.9,554894,4007,2827,130081,1962
        };

        final int nobs = 16;
        final int nvars = 6;

        
        OLSMultipleLinearRegression model = new OLSMultipleLinearRegression();
        model.newSampleData(design, nobs, nvars);

        
        double[] betaHat = model.estimateRegressionParameters();
        TestUtils.assertEquals(betaHat,
          new double[]{-3482258.63459582, 15.0618722713733,
                -0.358191792925910E-01,-2.02022980381683,
                -1.03322686717359,-0.511041056535807E-01,
                 1829.15146461355}, 2E-8); 

        
        double[] residuals = model.estimateResiduals();
        TestUtils.assertEquals(residuals, new double[]{
                267.340029759711,-94.0139423988359,46.28716775752924,
                -410.114621930906,309.7145907602313,-249.3112153297231,
                -164.0489563956039,-13.18035686637081,14.30477260005235,
                 455.394094551857,-17.26892711483297,-39.0550425226967,
                -155.5499735953195,-85.6713080421283,341.9315139607727,
                -206.7578251937366},
                      1E-8);

        
        double[] errors = model.estimateRegressionParametersStandardErrors();
        TestUtils.assertEquals(new double[] {890420.383607373,
                       84.9149257747669,
                       0.334910077722432E-01,
                       0.488399681651699,
                       0.214274163161675,
                       0.226073200069370,
                       455.478499142212}, errors, 1E-6);
        
        
        Assert.assertEquals(304.8540735619638, model.estimateRegressionStandardError(), 1E-10);
        
        
        Assert.assertEquals(0.995479004577296, model.calculateRSquared(), 1E-12);
        Assert.assertEquals(0.992465007628826, model.calculateAdjustedRSquared(), 1E-12);
        
        checkVarianceConsistency(model);
        
        
        model.setNoIntercept(true);
        model.newSampleData(design, nobs, nvars);
        
        
        betaHat = model.estimateRegressionParameters();
        TestUtils.assertEquals(betaHat,
          new double[]{-52.99357013868291, 0.07107319907358,
                -0.42346585566399,-0.57256866841929,
                -0.41420358884978, 48.41786562001326}, 1E-11); 
        
        
        errors = model.estimateRegressionParametersStandardErrors();
        TestUtils.assertEquals(new double[] {129.54486693117232, 0.03016640003786,
                0.41773654056612, 0.27899087467676, 0.32128496193363,
                17.68948737819961}, errors, 1E-11);
        
        
        residuals = model.estimateResiduals();
        TestUtils.assertEquals(residuals, new double[]{
                279.90274927293092, -130.32465380836874, 90.73228661967445, -401.31252201634948,
                -440.46768772620027, -543.54512853774793, 201.32111639536299, 215.90889365977932,
                73.09368242049943, 913.21694494481869, 424.82484953610174, -8.56475876776709,
                -361.32974610842876, 27.34560497213464, 151.28955976355002, -492.49937355336846},
                      1E-10);
        
        
        Assert.assertEquals(475.1655079819517, model.estimateRegressionStandardError(), 1E-10);
        
        
        Assert.assertEquals(0.9999670130706, model.calculateRSquared(), 1E-12);
        Assert.assertEquals(0.999947220913, model.calculateAdjustedRSquared(), 1E-12);
         
    }

// org.apache.commons.math.stat.regression.OLSMultipleLinearRegressionTest::testSwissFertility
    public void testSwissFertility() throws Exception {
        double[] design = new double[] {
            80.2,17.0,15,12,9.96,
            83.1,45.1,6,9,84.84,
            92.5,39.7,5,5,93.40,
            85.8,36.5,12,7,33.77,
            76.9,43.5,17,15,5.16,
            76.1,35.3,9,7,90.57,
            83.8,70.2,16,7,92.85,
            92.4,67.8,14,8,97.16,
            82.4,53.3,12,7,97.67,
            82.9,45.2,16,13,91.38,
            87.1,64.5,14,6,98.61,
            64.1,62.0,21,12,8.52,
            66.9,67.5,14,7,2.27,
            68.9,60.7,19,12,4.43,
            61.7,69.3,22,5,2.82,
            68.3,72.6,18,2,24.20,
            71.7,34.0,17,8,3.30,
            55.7,19.4,26,28,12.11,
            54.3,15.2,31,20,2.15,
            65.1,73.0,19,9,2.84,
            65.5,59.8,22,10,5.23,
            65.0,55.1,14,3,4.52,
            56.6,50.9,22,12,15.14,
            57.4,54.1,20,6,4.20,
            72.5,71.2,12,1,2.40,
            74.2,58.1,14,8,5.23,
            72.0,63.5,6,3,2.56,
            60.5,60.8,16,10,7.72,
            58.3,26.8,25,19,18.46,
            65.4,49.5,15,8,6.10,
            75.5,85.9,3,2,99.71,
            69.3,84.9,7,6,99.68,
            77.3,89.7,5,2,100.00,
            70.5,78.2,12,6,98.96,
            79.4,64.9,7,3,98.22,
            65.0,75.9,9,9,99.06,
            92.2,84.6,3,3,99.46,
            79.3,63.1,13,13,96.83,
            70.4,38.4,26,12,5.62,
            65.7,7.7,29,11,13.79,
            72.7,16.7,22,13,11.22,
            64.4,17.6,35,32,16.92,
            77.6,37.6,15,7,4.97,
            67.6,18.7,25,7,8.65,
            35.0,1.2,37,53,42.34,
            44.7,46.6,16,29,50.43,
            42.8,27.7,22,29,58.33
        };
        
        final int nobs = 47;
        final int nvars = 4;

        
        OLSMultipleLinearRegression model = new OLSMultipleLinearRegression();
        model.newSampleData(design, nobs, nvars);

        
        double[] betaHat = model.estimateRegressionParameters();
        TestUtils.assertEquals(betaHat,
                new double[]{91.05542390271397,
                -0.22064551045715,
                -0.26058239824328,
                -0.96161238456030,
                 0.12441843147162}, 1E-12);

        
        double[] residuals = model.estimateResiduals();
        TestUtils.assertEquals(residuals, new double[]{
                7.1044267859730512,1.6580347433531366,
                4.6944952770029644,8.4548022690166160,13.6547432343186212,
               -9.3586864458500774,7.5822446330520386,15.5568995563859289,
                0.8113090736598980,7.1186762732484308,7.4251378771228724,
                2.6761316873234109,0.8351584810309354,7.1769991119615177,
               -3.8746753206299553,-3.1337779476387251,-0.1412575244091504,
                1.1186809170469780,-6.3588097346816594,3.4039270429434074,
                2.3374058329820175,-7.9272368576900503,-7.8361010968497959,
               -11.2597369269357070,0.9445333697827101,6.6544245101380328,
               -0.9146136301118665,-4.3152449403848570,-4.3536932047009183,
               -3.8907885169304661,-6.3027643926302188,-7.8308982189289091,
               -3.1792280015332750,-6.7167298771158226,-4.8469946718041754,
               -10.6335664353633685,11.1031134362036958,6.0084032641811733,
                5.4326230830188482,-7.2375578629692230,2.1671550814448222,
                15.0147574652763112,4.8625103516321015,-7.1597256413907706,
                -0.4515205619767598,-10.2916870903837587,-15.7812984571900063},
                1E-12);

        
        double[] errors = model.estimateRegressionParametersStandardErrors();
        TestUtils.assertEquals(new double[] {6.94881329475087,
                0.07360008972340,
                0.27410957467466,
                0.19454551679325,
                0.03726654773803}, errors, 1E-10);
        
        
        Assert.assertEquals(7.73642194433223, model.estimateRegressionStandardError(), 1E-12);
        
        
        Assert.assertEquals(0.649789742860228, model.calculateRSquared(), 1E-12);
        Assert.assertEquals(0.6164363850373927, model.calculateAdjustedRSquared(), 1E-12);
        
        checkVarianceConsistency(model);
        
        
        model = new OLSMultipleLinearRegression();
        model.setNoIntercept(true);
        model.newSampleData(design, nobs, nvars);

        
        betaHat = model.estimateRegressionParameters();
        TestUtils.assertEquals(betaHat,
                new double[]{0.52191832900513,
                  2.36588087917963,
                  -0.94770353802795, 
                  0.30851985863609}, 1E-12);

        
        residuals = model.estimateResiduals();
        TestUtils.assertEquals(residuals, new double[]{
                44.138759883538249, 27.720705122356215, 35.873200836126799, 
                34.574619581211977, 26.600168342080213, 15.074636243026923, -12.704904871199814,
                1.497443824078134, 2.691972687079431, 5.582798774291231, -4.422986561283165, 
                -9.198581600334345, 4.481765170730647, 2.273520207553216, -22.649827853221336,
                -17.747900013943308, 20.298314638496436, 6.861405135329779, -8.684712790954924,
                -10.298639278062371, -9.896618896845819, 4.568568616351242, -15.313570491727944,
                -13.762961360873966, 7.156100301980509, 16.722282219843990, 26.716200609071898,
                -1.991466398777079, -2.523342564719335, 9.776486693095093, -5.297535127628603,
                -16.639070567471094, -10.302057295211819, -23.549487860816846, 1.506624392156384,
                -17.939174438345930, 13.105792202765040, -1.943329906928462, -1.516005841666695,
                -0.759066561832886, 20.793137744128977, -2.485236153005426, 27.588238710486976,
                2.658333257106881, -15.998337823623046, -5.550742066720694, -14.219077806826615},
                1E-12);

        
        errors = model.estimateRegressionParametersStandardErrors();
        TestUtils.assertEquals(new double[] {0.10470063765677, 0.41684100584290,
                0.43370143099691, 0.07694953606522}, errors, 1E-10);
        
        
        Assert.assertEquals(17.24710630547, model.estimateRegressionStandardError(), 1E-10);
        
        
        Assert.assertEquals(0.946350722085, model.calculateRSquared(), 1E-12);
        Assert.assertEquals(0.9413600915813, model.calculateAdjustedRSquared(), 1E-12);
    }

// org.apache.commons.math.stat.regression.OLSMultipleLinearRegressionTest::testHat
    public void testHat() throws Exception {

        
        double[] design = new double[] {
                11.14, .499, 11.1,
                12.74, .558, 8.9,
                13.13, .604, 8.8,
                11.51, .441, 8.9,
                12.38, .550, 8.8,
                12.60, .528, 9.9,
                11.13, .418, 10.7,
                11.7, .480, 10.5,
                11.02, .406, 10.5,
                11.41, .467, 10.7
        };

        int nobs = 10;
        int nvars = 2;

        
        OLSMultipleLinearRegression model = new OLSMultipleLinearRegression();
        model.newSampleData(design, nobs, nvars);

        RealMatrix hat = model.calculateHat();

        
        double[] referenceData = new double[] {
                .418, -.002,  .079, -.274, -.046,  .181,  .128,  .222,  .050,  .242,
                       .242,  .292,  .136,  .243,  .128, -.041,  .033, -.035,  .004,
                              .417, -.019,  .273,  .187, -.126,  .044, -.153,  .004,
                                     .604,  .197, -.038,  .168, -.022,  .275, -.028,
                                            .252,  .111, -.030,  .019, -.010, -.010,
                                                   .148,  .042,  .117,  .012,  .111,
                                                          .262,  .145,  .277,  .174,
                                                                 .154,  .120,  .168,
                                                                        .315,  .148,
                                                                               .187
        };

        
        int k = 0;
        for (int i = 0; i < 10; i++) {
            for (int j = i; j < 10; j++) {
                Assert.assertEquals(referenceData[k], hat.getEntry(i, j), 10e-3);
                Assert.assertEquals(hat.getEntry(i, j), hat.getEntry(j, i), 10e-12);
                k++;
            }
        }

        
        double[] residuals = model.estimateResiduals();
        RealMatrix I = MatrixUtils.createRealIdentityMatrix(10);
        double[] hatResiduals = I.subtract(hat).operate(model.Y).toArray();
        TestUtils.assertEquals(residuals, hatResiduals, 10e-12);
    }

// org.apache.commons.math.stat.regression.OLSMultipleLinearRegressionTest::testYVariance
    public void testYVariance() {

        

        OLSMultipleLinearRegression model = new OLSMultipleLinearRegression();
        model.newSampleData(y, x);
        TestUtils.assertEquals(model.calculateYVariance(), 3.5, 0);
    }

// org.apache.commons.math.stat.regression.OLSMultipleLinearRegressionTest::testNewSample2
    public void testNewSample2() throws Exception {
        double[] y = new double[] {1, 2, 3, 4}; 
        double[][] x = new double[][] {
          {19, 22, 33},
          {20, 30, 40},
          {25, 35, 45},
          {27, 37, 47}   
        };
        OLSMultipleLinearRegression regression = new OLSMultipleLinearRegression();
        regression.newSampleData(y, x);
        RealMatrix combinedX = regression.X.copy();
        RealVector combinedY = regression.Y.copy();
        regression.newXSampleData(x);
        regression.newYSampleData(y);
        Assert.assertEquals(combinedX, regression.X);
        Assert.assertEquals(combinedY, regression.Y);
        
        
        regression.setNoIntercept(true);
        regression.newSampleData(y, x);
        combinedX = regression.X.copy();
        combinedY = regression.Y.copy();
        regression.newXSampleData(x);
        regression.newYSampleData(y);
        Assert.assertEquals(combinedX, regression.X);
        Assert.assertEquals(combinedY, regression.Y);
    }

// org.apache.commons.math.stat.regression.OLSMultipleLinearRegressionTest::testNewSampleDataYNull
    public void testNewSampleDataYNull() {
        createRegression().newSampleData(null, new double[][] {});
    }

// org.apache.commons.math.stat.regression.OLSMultipleLinearRegressionTest::testNewSampleDataXNull
    public void testNewSampleDataXNull() {
        createRegression().newSampleData(new double[] {}, null);
    }

// org.apache.commons.math.stat.regression.OLSMultipleLinearRegressionTest::testWampler1
    public void testWampler1() throws Exception {
        double[] data = new double[]{
            1, 0,
            6, 1,
            63, 2,
            364, 3,
            1365, 4,
            3906, 5,
            9331, 6,
            19608, 7,
            37449, 8,
            66430, 9,
            111111, 10,
            177156, 11,
            271453, 12,
            402234, 13,
            579195, 14,
            813616, 15,
            1118481, 16,
            1508598, 17,
            2000719, 18,
            2613660, 19,
            3368421, 20};
        OLSMultipleLinearRegression model = new OLSMultipleLinearRegression();

        final int nvars = 5;
        final int nobs = 21;
        double[] tmp = new double[(nvars + 1) * nobs];
        int off = 0;
        int off2 = 0;
        for (int i = 0; i < nobs; i++) {
            tmp[off2] = data[off];
            tmp[off2 + 1] = data[off + 1];
            tmp[off2 + 2] = tmp[off2 + 1] * tmp[off2 + 1];
            tmp[off2 + 3] = tmp[off2 + 1] * tmp[off2 + 2];
            tmp[off2 + 4] = tmp[off2 + 1] * tmp[off2 + 3];
            tmp[off2 + 5] = tmp[off2 + 1] * tmp[off2 + 4];
            off2 += (nvars + 1);
            off += 2;
        }
        model.newSampleData(tmp, nobs, nvars);
        double[] betaHat = model.estimateRegressionParameters();
        TestUtils.assertEquals(betaHat,
                new double[]{1.0,
                    1.0, 1.0,
                    1.0, 1.0,
                    1.0}, 1E-8);

        double[] se = model.estimateRegressionParametersStandardErrors();
        TestUtils.assertEquals(se,
                new double[]{0.0,
                    0.0, 0.0,
                    0.0, 0.0,
                    0.0}, 1E-8); 

        TestUtils.assertEquals(1.0, model.calculateRSquared(), 1.0e-10);
        TestUtils.assertEquals(0, model.estimateErrorVariance(), 1.0e-7);
        TestUtils.assertEquals(0.00, model.calculateResidualSumOfSquares(), 1.0e-6);

        return;
    }

// org.apache.commons.math.stat.regression.OLSMultipleLinearRegressionTest::testWampler2
    public void testWampler2() throws Exception {
        double[] data = new double[]{
            1.00000, 0,
            1.11111, 1,
            1.24992, 2,
            1.42753, 3,
            1.65984, 4,
            1.96875, 5,
            2.38336, 6,
            2.94117, 7,
            3.68928, 8,
            4.68559, 9,
            6.00000, 10,
            7.71561, 11,
            9.92992, 12,
            12.75603, 13,
            16.32384, 14,
            20.78125, 15,
            26.29536, 16,
            33.05367, 17,
            41.26528, 18,
            51.16209, 19,
            63.00000, 20};
        OLSMultipleLinearRegression model = new OLSMultipleLinearRegression();

        final int nvars = 5;
        final int nobs = 21;
        double[] tmp = new double[(nvars + 1) * nobs];
        int off = 0;
        int off2 = 0;
        for (int i = 0; i < nobs; i++) {
            tmp[off2] = data[off];
            tmp[off2 + 1] = data[off + 1];
            tmp[off2 + 2] = tmp[off2 + 1] * tmp[off2 + 1];
            tmp[off2 + 3] = tmp[off2 + 1] * tmp[off2 + 2];
            tmp[off2 + 4] = tmp[off2 + 1] * tmp[off2 + 3];
            tmp[off2 + 5] = tmp[off2 + 1] * tmp[off2 + 4];
            off2 += (nvars + 1);
            off += 2;
        }
        model.newSampleData(tmp, nobs, nvars);
        double[] betaHat = model.estimateRegressionParameters();
        TestUtils.assertEquals(betaHat,
                new double[]{
                    1.0,
                    1.0e-1,
                    1.0e-2,
                    1.0e-3, 1.0e-4,
                    1.0e-5}, 1E-8);

        double[] se = model.estimateRegressionParametersStandardErrors();
        TestUtils.assertEquals(se,
                new double[]{0.0,
                    0.0, 0.0,
                    0.0, 0.0,
                    0.0}, 1E-8); 
        TestUtils.assertEquals(1.0, model.calculateRSquared(), 1.0e-10);
        TestUtils.assertEquals(0, model.estimateErrorVariance(), 1.0e-7);
        TestUtils.assertEquals(0.00, model.calculateResidualSumOfSquares(), 1.0e-6);
        return;
    }

// org.apache.commons.math.stat.regression.OLSMultipleLinearRegressionTest::testWampler3
    public void testWampler3() throws Exception {
        double[] data = new double[]{
            760, 0,
            -2042, 1,
            2111, 2,
            -1684, 3,
            3888, 4,
            1858, 5,
            11379, 6,
            17560, 7,
            39287, 8,
            64382, 9,
            113159, 10,
            175108, 11,
            273291, 12,
            400186, 13,
            581243, 14,
            811568, 15,
            1121004, 16,
            1506550, 17,
            2002767, 18,
            2611612, 19,
            3369180, 20};

        OLSMultipleLinearRegression model = new OLSMultipleLinearRegression();
        final int nvars = 5;
        final int nobs = 21;
        double[] tmp = new double[(nvars + 1) * nobs];
        int off = 0;
        int off2 = 0;
        for (int i = 0; i < nobs; i++) {
            tmp[off2] = data[off];
            tmp[off2 + 1] = data[off + 1];
            tmp[off2 + 2] = tmp[off2 + 1] * tmp[off2 + 1];
            tmp[off2 + 3] = tmp[off2 + 1] * tmp[off2 + 2];
            tmp[off2 + 4] = tmp[off2 + 1] * tmp[off2 + 3];
            tmp[off2 + 5] = tmp[off2 + 1] * tmp[off2 + 4];
            off2 += (nvars + 1);
            off += 2;
        }
        model.newSampleData(tmp, nobs, nvars);
        double[] betaHat = model.estimateRegressionParameters();
        TestUtils.assertEquals(betaHat,
                new double[]{
                    1.0,
                    1.0,
                    1.0,
                    1.0,
                    1.0,
                    1.0}, 1E-8); 

        double[] se = model.estimateRegressionParametersStandardErrors();
        TestUtils.assertEquals(se,
                new double[]{2152.32624678170,
                    2363.55173469681, 779.343524331583,
                    101.475507550350, 5.64566512170752,
                    0.112324854679312}, 1E-8); 

        TestUtils.assertEquals(.999995559025820, model.calculateRSquared(), 1.0e-10);
        TestUtils.assertEquals(5570284.53333333, model.estimateErrorVariance(), 1.0e-6);
        TestUtils.assertEquals(83554268.0000000, model.calculateResidualSumOfSquares(), 1.0e-5);
        return;
    }

// org.apache.commons.math.stat.regression.OLSMultipleLinearRegressionTest::testWampler4
    public void testWampler4() throws Exception {
        double[] data = new double[]{
            75901, 0,
            -204794, 1,
            204863, 2,
            -204436, 3,
            253665, 4,
            -200894, 5,
            214131, 6,
            -185192, 7,
            221249, 8,
            -138370, 9,
            315911, 10,
            -27644, 11,
            455253, 12,
            197434, 13,
            783995, 14,
            608816, 15,
            1370781, 16,
            1303798, 17,
            2205519, 18,
            2408860, 19,
            3444321, 20};

        OLSMultipleLinearRegression model = new OLSMultipleLinearRegression();
        final int nvars = 5;
        final int nobs = 21;
        double[] tmp = new double[(nvars + 1) * nobs];
        int off = 0;
        int off2 = 0;
        for (int i = 0; i < nobs; i++) {
            tmp[off2] = data[off];
            tmp[off2 + 1] = data[off + 1];
            tmp[off2 + 2] = tmp[off2 + 1] * tmp[off2 + 1];
            tmp[off2 + 3] = tmp[off2 + 1] * tmp[off2 + 2];
            tmp[off2 + 4] = tmp[off2 + 1] * tmp[off2 + 3];
            tmp[off2 + 5] = tmp[off2 + 1] * tmp[off2 + 4];
            off2 += (nvars + 1);
            off += 2;
        }
        model.newSampleData(tmp, nobs, nvars);
        double[] betaHat = model.estimateRegressionParameters();
        TestUtils.assertEquals(betaHat,
                new double[]{
                    1.0,
                    1.0,
                    1.0,
                    1.0,
                    1.0,
                    1.0}, 1E-6); 

        double[] se = model.estimateRegressionParametersStandardErrors();
        TestUtils.assertEquals(se,
                new double[]{215232.624678170,
                    236355.173469681, 77934.3524331583,
                    10147.5507550350, 564.566512170752,
                    11.2324854679312}, 1E-8); 

        TestUtils.assertEquals(.957478440825662, model.calculateRSquared(), 1.0e-10);
        TestUtils.assertEquals(55702845333.3333, model.estimateErrorVariance(), 1.0e-4);
        TestUtils.assertEquals(835542680000.000, model.calculateResidualSumOfSquares(), 1.0e-3);
        return;
    }
