// buggy code
    public double solve(final UnivariateRealFunction f, double min, double max, double initial)
        throws MaxIterationsExceededException, FunctionEvaluationException {
        return solve(min, max);
    }

// relevant test
// org.apache.commons.math.analysis.solvers.BisectionSolverTest::testDeprecated
    public void testDeprecated() throws MathException {
        UnivariateRealFunction f = new SinFunction();
        double result;

        UnivariateRealSolver solver = new BisectionSolver(f);
        result = solver.solve(3, 4);
        assertEquals(result, Math.PI, solver.getAbsoluteAccuracy());

        result = solver.solve(1, 4);
        assertEquals(result, Math.PI, solver.getAbsoluteAccuracy());
    }

// org.apache.commons.math.analysis.solvers.BisectionSolverTest::testSinZero
    public void testSinZero() throws MathException {
        UnivariateRealFunction f = new SinFunction();
        double result;

        UnivariateRealSolver solver = new BisectionSolver();
        result = solver.solve(f, 3, 4);
        assertEquals(result, Math.PI, solver.getAbsoluteAccuracy());

        result = solver.solve(f, 1, 4);
        assertEquals(result, Math.PI, solver.getAbsoluteAccuracy());
    }

// org.apache.commons.math.analysis.solvers.BisectionSolverTest::testQuinticZero
   public void testQuinticZero() throws MathException {
        UnivariateRealFunction f = new QuinticFunction();
        double result;

        UnivariateRealSolver solver = new BisectionSolver();
        result = solver.solve(f, -0.2, 0.2);
        assertEquals(result, 0, solver.getAbsoluteAccuracy());

        result = solver.solve(f, -0.1, 0.3);
        assertEquals(result, 0, solver.getAbsoluteAccuracy());

        result = solver.solve(f, -0.3, 0.45);
        assertEquals(result, 0, solver.getAbsoluteAccuracy());

        result = solver.solve(f, 0.3, 0.7);
        assertEquals(result, 0.5, solver.getAbsoluteAccuracy());

        result = solver.solve(f, 0.2, 0.6);
        assertEquals(result, 0.5, solver.getAbsoluteAccuracy());

        result = solver.solve(f, 0.05, 0.95);
        assertEquals(result, 0.5, solver.getAbsoluteAccuracy());

        result = solver.solve(f, 0.85, 1.25);
        assertEquals(result, 1.0, solver.getAbsoluteAccuracy());

        result = solver.solve(f, 0.8, 1.2);
        assertEquals(result, 1.0, solver.getAbsoluteAccuracy());

        result = solver.solve(f, 0.85, 1.75);
        assertEquals(result, 1.0, solver.getAbsoluteAccuracy());

        result = solver.solve(f, 0.55, 1.45);
        assertEquals(result, 1.0, solver.getAbsoluteAccuracy());

        result = solver.solve(f, 0.85, 5);
        assertEquals(result, 1.0, solver.getAbsoluteAccuracy());

        assertEquals(result, solver.getResult(), 0);
        assertTrue(solver.getIterationCount() > 0);
    }

// org.apache.commons.math.analysis.solvers.BisectionSolverTest::testMath369
    public void testMath369() throws Exception {
        UnivariateRealFunction f = new SinFunction();
        UnivariateRealSolver solver = new BisectionSolver();
        assertEquals(Math.PI, solver.solve(f, 3.0, 3.2, 3.1), solver.getAbsoluteAccuracy());
    }

// org.apache.commons.math.analysis.solvers.BisectionSolverTest::testSetFunctionValueAccuracy
    public void testSetFunctionValueAccuracy(){
        double expected = 1.0e-2;
        UnivariateRealSolver solver = new BisectionSolver();
        solver.setFunctionValueAccuracy(expected);
        assertEquals(expected, solver.getFunctionValueAccuracy(), 1.0e-2);
    }

// org.apache.commons.math.analysis.solvers.BisectionSolverTest::testResetFunctionValueAccuracy
    public void testResetFunctionValueAccuracy(){
        double newValue = 1.0e-2;
        UnivariateRealSolver solver = new BisectionSolver();
        double oldValue = solver.getFunctionValueAccuracy();
        solver.setFunctionValueAccuracy(newValue);
        solver.resetFunctionValueAccuracy();
        assertEquals(oldValue, solver.getFunctionValueAccuracy(), 1.0e-2);
    }

// org.apache.commons.math.analysis.solvers.BisectionSolverTest::testSetAbsoluteAccuracy
    public void testSetAbsoluteAccuracy(){
        double expected = 1.0e-2;
        UnivariateRealSolver solver = new BisectionSolver();
        solver.setAbsoluteAccuracy(expected);
        assertEquals(expected, solver.getAbsoluteAccuracy(), 1.0e-2);
    }

// org.apache.commons.math.analysis.solvers.BisectionSolverTest::testResetAbsoluteAccuracy
    public void testResetAbsoluteAccuracy(){
        double newValue = 1.0e-2;
        UnivariateRealSolver solver = new BisectionSolver();
        double oldValue = solver.getAbsoluteAccuracy();
        solver.setAbsoluteAccuracy(newValue);
        solver.resetAbsoluteAccuracy();
        assertEquals(oldValue, solver.getAbsoluteAccuracy(), 1.0e-2);
    }

// org.apache.commons.math.analysis.solvers.BisectionSolverTest::testSetMaximalIterationCount
    public void testSetMaximalIterationCount(){
        int expected = 100;
        UnivariateRealSolver solver = new BisectionSolver();
        solver.setMaximalIterationCount(expected);
        assertEquals(expected, solver.getMaximalIterationCount());
    }

// org.apache.commons.math.analysis.solvers.BisectionSolverTest::testResetMaximalIterationCount
    public void testResetMaximalIterationCount(){
        int newValue = 10000;
        UnivariateRealSolver solver = new BisectionSolver();
        int oldValue = solver.getMaximalIterationCount();
        solver.setMaximalIterationCount(newValue);
        solver.resetMaximalIterationCount();
        assertEquals(oldValue, solver.getMaximalIterationCount());
    }

// org.apache.commons.math.analysis.solvers.BisectionSolverTest::testSetRelativeAccuracy
    public void testSetRelativeAccuracy(){
        double expected = 1.0e-2;
        UnivariateRealSolver solver = new BisectionSolver();
        solver.setRelativeAccuracy(expected);
        assertEquals(expected, solver.getRelativeAccuracy(), 1.0e-2);
    }

// org.apache.commons.math.analysis.solvers.BisectionSolverTest::testResetRelativeAccuracy
    public void testResetRelativeAccuracy(){
        double newValue = 1.0e-2;
        UnivariateRealSolver solver = new BisectionSolver();
        double oldValue = solver.getRelativeAccuracy();
        solver.setRelativeAccuracy(newValue);
        solver.resetRelativeAccuracy();
        assertEquals(oldValue, solver.getRelativeAccuracy(), 1.0e-2);
    }

// org.apache.commons.math.analysis.solvers.UnivariateRealSolverFactoryImplTest::testNewBisectionSolverValid
    public void testNewBisectionSolverValid() {
        UnivariateRealSolver solver = factory.newBisectionSolver();
        assertNotNull(solver);
        assertTrue(solver instanceof BisectionSolver);
    }

// org.apache.commons.math.analysis.solvers.UnivariateRealSolverFactoryImplTest::testNewNewtonSolverValid
    public void testNewNewtonSolverValid() {
        UnivariateRealSolver solver = factory.newNewtonSolver();
        assertNotNull(solver);
        assertTrue(solver instanceof NewtonSolver);
    }

// org.apache.commons.math.analysis.solvers.UnivariateRealSolverFactoryImplTest::testNewBrentSolverValid
    public void testNewBrentSolverValid() {
        UnivariateRealSolver solver = factory.newBrentSolver();
        assertNotNull(solver);
        assertTrue(solver instanceof BrentSolver);
    }

// org.apache.commons.math.analysis.solvers.UnivariateRealSolverFactoryImplTest::testNewSecantSolverValid
    public void testNewSecantSolverValid() {
        UnivariateRealSolver solver = factory.newSecantSolver();
        assertNotNull(solver);
        assertTrue(solver instanceof SecantSolver);
    }
