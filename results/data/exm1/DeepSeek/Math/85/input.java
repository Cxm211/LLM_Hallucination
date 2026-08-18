// buggy code
    public static double[] bracket(UnivariateRealFunction function,
            double initial, double lowerBound, double upperBound, 
            int maximumIterations) throws ConvergenceException, 
            FunctionEvaluationException {
        
        if (function == null) {
            throw MathRuntimeException.createIllegalArgumentException("function is null");
        }
        if (maximumIterations <= 0)  {
            throw MathRuntimeException.createIllegalArgumentException(
                  "bad value for maximum iterations number: {0}", maximumIterations);
        }
        if (initial < lowerBound || initial > upperBound || lowerBound >= upperBound) {
            throw MathRuntimeException.createIllegalArgumentException(
                  "invalid bracketing parameters:  lower bound={0},  initial={1}, upper bound={2}",
                  lowerBound, initial, upperBound);
        }
        double a = initial;
        double b = initial;
        double fa;
        double fb;
        int numIterations = 0 ;
    
        do {
            a = Math.max(a - 1.0, lowerBound);
            b = Math.min(b + 1.0, upperBound);
            fa = function.value(a);
            
            fb = function.value(b);
            numIterations++ ;
        } while ((fa * fb > 0.0) && (numIterations < maximumIterations) && 
                ((a > lowerBound) || (b < upperBound)));
   
        if (fa * fb >= 0.0 ) {
            throw new ConvergenceException(
                      "number of iterations={0}, maximum iterations={1}, " +
                      "initial={2}, lower bound={3}, upper bound={4}, final a value={5}, " +
                      "final b value={6}, f(a)={7}, f(b)={8}",
                      numIterations, maximumIterations, initial,
                      lowerBound, upperBound, a, b, fa, fb);
        }
        
        return new double[]{a, b};
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

// org.apache.commons.math.analysis.solvers.BrentSolverTest::testDeprecated
    public void testDeprecated() throws MathException {
        
        
        
        UnivariateRealFunction f = new SinFunction();
        double result;
        UnivariateRealSolver solver = new BrentSolver(f);
        
        result = solver.solve(3, 4);
        
        
        assertEquals(result, Math.PI, solver.getAbsoluteAccuracy());
        
        assertTrue(solver.getIterationCount() <= 5);
        
        result = solver.solve(1, 4);
        
        
        assertEquals(result, Math.PI, solver.getAbsoluteAccuracy());
        
        assertTrue(solver.getIterationCount() <= 6);
        solver = new SecantSolver(f);
        result = solver.solve(3, 4);
        
        
        assertEquals(result, Math.PI, solver.getAbsoluteAccuracy());
        
        assertTrue(solver.getIterationCount() <= 5);
        result = solver.solve(1, 4);
        
        
        assertEquals(result, Math.PI, solver.getAbsoluteAccuracy());
        
        assertTrue(solver.getIterationCount() <= 6);
        assertEquals(result, solver.getResult(), 0);
    }

// org.apache.commons.math.analysis.solvers.BrentSolverTest::testSinZero
    public void testSinZero() throws MathException {
        
        
        
        UnivariateRealFunction f = new SinFunction();
        double result;
        UnivariateRealSolver solver = new BrentSolver();
        
        result = solver.solve(f, 3, 4);
        
        
        assertEquals(result, Math.PI, solver.getAbsoluteAccuracy());
        
        assertTrue(solver.getIterationCount() <= 5);
        
        result = solver.solve(f, 1, 4);
        
        
        assertEquals(result, Math.PI, solver.getAbsoluteAccuracy());
        
        assertTrue(solver.getIterationCount() <= 6);
        solver = new SecantSolver();
        result = solver.solve(f, 3, 4);
        
        
        assertEquals(result, Math.PI, solver.getAbsoluteAccuracy());
        
        assertTrue(solver.getIterationCount() <= 5);
        result = solver.solve(f, 1, 4);
        
        
        assertEquals(result, Math.PI, solver.getAbsoluteAccuracy());
        
        assertTrue(solver.getIterationCount() <= 6);
        assertEquals(result, solver.getResult(), 0);
    }

// org.apache.commons.math.analysis.solvers.BrentSolverTest::testQuinticZero
   public void testQuinticZero() throws MathException {
        
        
        
        
        
        
        
        UnivariateRealFunction f = new QuinticFunction();
        double result;
        
        UnivariateRealSolver solver = new BrentSolver();
        
        
        result = solver.solve(f, -0.2, 0.2);
        
        
        assertEquals(result, 0, solver.getAbsoluteAccuracy());
        assertTrue(solver.getIterationCount() <= 2);
        
        
        result = solver.solve(f, -0.1, 0.3);
        
        
        assertEquals(result, 0, solver.getAbsoluteAccuracy());
        
        assertTrue(solver.getIterationCount() <= 6);
        
        result = solver.solve(f, -0.3, 0.45);
        
        
        assertEquals(result, 0, solver.getAbsoluteAccuracy());
        
        assertTrue(solver.getIterationCount() <= 7);
        
        result = solver.solve(f, 0.3, 0.7);
        
        
        assertEquals(result, 0.5, solver.getAbsoluteAccuracy());
        
        assertTrue(solver.getIterationCount() <= 7);
        
        result = solver.solve(f, 0.2, 0.6);
        
        
        assertEquals(result, 0.5, solver.getAbsoluteAccuracy());
        
        assertTrue(solver.getIterationCount() <= 7);
        
        result = solver.solve(f, 0.05, 0.95);
        
        
        assertEquals(result, 0.5, solver.getAbsoluteAccuracy());
        
        assertTrue(solver.getIterationCount() <= 9);
        
        
        result = solver.solve(f, 0.85, 1.25);
        
        
        assertEquals(result, 1.0, solver.getAbsoluteAccuracy());
        
        assertTrue(solver.getIterationCount() <= 9);
        
        result = solver.solve(f, 0.8, 1.2);
        
        
        assertEquals(result, 1.0, solver.getAbsoluteAccuracy());
        
        assertTrue(solver.getIterationCount() <= 9);
        
        result = solver.solve(f, 0.85, 1.75);
        
        
        assertEquals(result, 1.0, solver.getAbsoluteAccuracy());
        
        assertTrue(solver.getIterationCount() <= 11);
        
        result = solver.solve(f, 0.55, 1.45);
        
        
        assertEquals(result, 1.0, solver.getAbsoluteAccuracy());
        
        assertTrue(solver.getIterationCount() <= 8);
        
        result = solver.solve(f, 0.85, 5);
        
       
        assertEquals(result, 1.0, solver.getAbsoluteAccuracy());
        
        assertTrue(solver.getIterationCount() <= 13);
        
        solver = new SecantSolver();
        result = solver.solve(f, -0.2, 0.2);
        
        
        assertEquals(result, 0, solver.getAbsoluteAccuracy());
        
        assertTrue(solver.getIterationCount() <= 2);
        result = solver.solve(f, -0.1, 0.3);
        
        
        assertEquals(result, 0, solver.getAbsoluteAccuracy());
        
        assertTrue(solver.getIterationCount() <= 6);
        result = solver.solve(f, -0.3, 0.45);
        
        
        assertEquals(result, 0, solver.getAbsoluteAccuracy());
        
        assertTrue(solver.getIterationCount() <= 7);
        result = solver.solve(f, 0.3, 0.7);
        
        
        assertEquals(result, 0.5, solver.getAbsoluteAccuracy());
        
        assertTrue(solver.getIterationCount() <= 8);
        result = solver.solve(f, 0.2, 0.6);
        
        
        assertEquals(result, 0.5, solver.getAbsoluteAccuracy());
        
        assertTrue(solver.getIterationCount() <= 7);
        result = solver.solve(f, 0.05, 0.95);
        
        
        assertEquals(result, 0.5, solver.getAbsoluteAccuracy());
        
        assertTrue(solver.getIterationCount() <= 9);
        result = solver.solve(f, 0.85, 1.25);
        
        
        assertEquals(result, 1.0, solver.getAbsoluteAccuracy());
        
        assertTrue(solver.getIterationCount() <= 11);
        result = solver.solve(f, 0.8, 1.2);
        
        
        assertEquals(result, 1.0, solver.getAbsoluteAccuracy());
        
        assertTrue(solver.getIterationCount() <= 9);
        result = solver.solve(f, 0.85, 1.75);
        
        
        assertEquals(result, 1.0, solver.getAbsoluteAccuracy());
        
        assertTrue(solver.getIterationCount() <= 15);
        
        
        result = solver.solve(f, 0.55, 1.45);
        
        
        assertEquals(result, 1.0, solver.getAbsoluteAccuracy());
        
        assertTrue(solver.getIterationCount() <= 8);
        result = solver.solve(f, 0.85, 5);
        
        
        assertEquals(result, 1.0, solver.getAbsoluteAccuracy());
        
        assertTrue(solver.getIterationCount() <= 15);
        
        result = UnivariateRealSolverUtils.solve(f, -0.2, 0.2);
        assertEquals(result, 0, solver.getAbsoluteAccuracy());
        result = UnivariateRealSolverUtils.solve(f, -0.1, 0.3);
        assertEquals(result, 0, 1E-8);
        result = UnivariateRealSolverUtils.solve(f, -0.3, 0.45);
        assertEquals(result, 0, 1E-6);
        result = UnivariateRealSolverUtils.solve(f, 0.3, 0.7);
        assertEquals(result, 0.5, 1E-6);
        result = UnivariateRealSolverUtils.solve(f, 0.2, 0.6);
        assertEquals(result, 0.5, 1E-6);
        result = UnivariateRealSolverUtils.solve(f, 0.05, 0.95);
        assertEquals(result, 0.5, 1E-6);
        result = UnivariateRealSolverUtils.solve(f, 0.85, 1.25);
        assertEquals(result, 1.0, 1E-6);
        result = UnivariateRealSolverUtils.solve(f, 0.8, 1.2);
        assertEquals(result, 1.0, 1E-6);
        result = UnivariateRealSolverUtils.solve(f, 0.85, 1.75);
        assertEquals(result, 1.0, 1E-6);
        result = UnivariateRealSolverUtils.solve(f, 0.55, 1.45);
        assertEquals(result, 1.0, 1E-6);
        result = UnivariateRealSolverUtils.solve(f, 0.85, 5);
        assertEquals(result, 1.0, 1E-6);
    }

// org.apache.commons.math.analysis.solvers.BrentSolverTest::testRootEndpoints
    public void testRootEndpoints() throws Exception {
        UnivariateRealFunction f = new SinFunction();
        UnivariateRealSolver solver = new BrentSolver();
        
        
        double result = solver.solve(f, Math.PI, 4);
        assertEquals(result, Math.PI, solver.getAbsoluteAccuracy());

        result = solver.solve(f, 3, Math.PI);
        assertEquals(result, Math.PI, solver.getAbsoluteAccuracy());
    }

// org.apache.commons.math.analysis.solvers.BrentSolverTest::testBadEndpoints
    public void testBadEndpoints() throws Exception {
        UnivariateRealFunction f = new SinFunction();
        UnivariateRealSolver solver = new BrentSolver();
        try {  
            solver.solve(f, 1, -1);
            fail("Expecting IllegalArgumentException - bad interval");
        } catch (IllegalArgumentException ex) {
            
        }
        try {  
            solver.solve(f, 1, 1.5);
            fail("Expecting IllegalArgumentException - non-bracketing");
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.analysis.solvers.BrentSolverTest::testInitialGuess
    public void testInitialGuess() throws MathException {

        MonitoredFunction f = new MonitoredFunction(new QuinticFunction());
        UnivariateRealSolver solver = new BrentSolver();
        double result;

        
        result = solver.solve(f, 0.6, 7.0);
        assertEquals(result, 1.0, solver.getAbsoluteAccuracy());
        int referenceCallsCount = f.getCallsCount();
        assertTrue(referenceCallsCount >= 13);
 
        
        try {
          result = solver.solve(f, 0.6, 7.0, 0.0);
          fail("an IllegalArgumentException was expected");
        } catch (IllegalArgumentException iae) {
            
        } catch (Exception e) {
            fail("wrong exception caught: " + e.getMessage());
        }
 
        
        f.setCallsCount(0);
        result = solver.solve(f, 0.6, 7.0, 0.61);
        assertEquals(result, 1.0, solver.getAbsoluteAccuracy());
        assertTrue(f.getCallsCount() > referenceCallsCount);
 
        
        f.setCallsCount(0);
        result = solver.solve(f, 0.6, 7.0, 0.999999);
        assertEquals(result, 1.0, solver.getAbsoluteAccuracy());
        assertTrue(f.getCallsCount() < referenceCallsCount);

        
        f.setCallsCount(0);
        result = solver.solve(f, 0.6, 7.0, 1.0);
        assertEquals(result, 1.0, solver.getAbsoluteAccuracy());
        assertEquals(0, solver.getIterationCount());
        assertEquals(1, f.getCallsCount());
 
    }

// org.apache.commons.math.analysis.solvers.NewtonSolverTest::testDeprecated
    public void testDeprecated() throws MathException {
        DifferentiableUnivariateRealFunction f = new SinFunction();
        double result;
        
        UnivariateRealSolver solver = new NewtonSolver(f);
        result = solver.solve(3, 4);
        assertEquals(result, Math.PI, solver.getAbsoluteAccuracy());

        result = solver.solve(1, 4);
        assertEquals(result, Math.PI, solver.getAbsoluteAccuracy());
        
        
        assertEquals(result, solver.getResult(), 0);
        assertTrue(solver.getIterationCount() > 0);
    }

// org.apache.commons.math.analysis.solvers.NewtonSolverTest::testSinZero
   public void testSinZero() throws MathException {
       DifferentiableUnivariateRealFunction f = new SinFunction();
       double result;
       
       UnivariateRealSolver solver = new NewtonSolver();
       result = solver.solve(f, 3, 4);
       assertEquals(result, Math.PI, solver.getAbsoluteAccuracy());

       result = solver.solve(f, 1, 4);
       assertEquals(result, Math.PI, solver.getAbsoluteAccuracy());
       
       
       assertEquals(result, solver.getResult(), 0);
       assertTrue(solver.getIterationCount() > 0);
   }

// org.apache.commons.math.analysis.solvers.NewtonSolverTest::testQuinticZero
    public void testQuinticZero() throws MathException {
        DifferentiableUnivariateRealFunction f = new QuinticFunction();
        double result;

        UnivariateRealSolver solver = new NewtonSolver();
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
    }

// org.apache.commons.math.analysis.solvers.UnivariateRealSolverUtilsTest::testSolveNull
    public void testSolveNull() throws MathException {
        try {
            UnivariateRealSolverUtils.solve(null, 0.0, 4.0);
            fail();
        } catch(IllegalArgumentException ex){
            
        }
    }

// org.apache.commons.math.analysis.solvers.UnivariateRealSolverUtilsTest::testSolveBadParameters
    public void testSolveBadParameters() throws MathException {
        try { 
            UnivariateRealSolverUtils.solve(sin,0.0, 4.0, 4.0); 
        } catch (IllegalArgumentException ex) {
            
        }    
        try { 
            UnivariateRealSolverUtils.solve(sin, 0.0, 4.0, 0.0); 
        } catch (IllegalArgumentException ex) {
            
        }        
    }

// org.apache.commons.math.analysis.solvers.UnivariateRealSolverUtilsTest::testSolveSin
    public void testSolveSin() throws MathException {     
        double x = UnivariateRealSolverUtils.solve(sin, 1.0, 4.0);
        assertEquals(Math.PI, x, 1.0e-4);
    }

// org.apache.commons.math.analysis.solvers.UnivariateRealSolverUtilsTest::testSolveAccuracyNull
    public void testSolveAccuracyNull()  throws MathException {
        try {
            double accuracy = 1.0e-6;
            UnivariateRealSolverUtils.solve(null, 0.0, 4.0, accuracy);
            fail();
        } catch(IllegalArgumentException ex){
            
        }
    }

// org.apache.commons.math.analysis.solvers.UnivariateRealSolverUtilsTest::testSolveAccuracySin
    public void testSolveAccuracySin() throws MathException {
        double accuracy = 1.0e-6;
        double x = UnivariateRealSolverUtils.solve(sin, 1.0,
                4.0, accuracy);
        assertEquals(Math.PI, x, accuracy);
    }

// org.apache.commons.math.analysis.solvers.UnivariateRealSolverUtilsTest::testSolveNoRoot
    public void testSolveNoRoot() throws MathException {
        try {
            UnivariateRealSolverUtils.solve(sin, 1.0, 1.5);  
            fail("Expecting IllegalArgumentException ");  
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.analysis.solvers.UnivariateRealSolverUtilsTest::testBracketSin
    public void testBracketSin() throws MathException {
        double[] result = UnivariateRealSolverUtils.bracket(sin, 
                0.0, -2.0, 2.0);
        assertTrue(sin.value(result[0]) < 0);
        assertTrue(sin.value(result[1]) > 0);
    }

// org.apache.commons.math.analysis.solvers.UnivariateRealSolverUtilsTest::testBadParameters
    public void testBadParameters() throws MathException {
        try { 
            UnivariateRealSolverUtils.bracket(null, 1.5, 0, 2.0);
            fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }
        try { 
            UnivariateRealSolverUtils.bracket(sin, 2.5, 0, 2.0);
            fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }
        try { 
            UnivariateRealSolverUtils.bracket(sin, 1.5, 2.0, 1.0);
            fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }
        try { 
            UnivariateRealSolverUtils.bracket(sin, 1.5, 0, 2.0, 0);
            fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }        
    }

// org.apache.commons.math.distribution.BetaDistributionTest::testCumulative
    public void testCumulative() throws MathException {
        double[] x = new double[]{-0.1, 0.0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0, 1.1};
        
        checkCumulative(0.1, 0.1,
                x, new double[]{
                0.0000000000, 0.0000000000, 0.4063850939, 0.4397091902, 0.4628041861,
                0.4821200456, 0.5000000000, 0.5178799544, 0.5371958139, 0.5602908098,
                0.5936149061, 1.0000000000, 1.0000000000});
        checkCumulative(0.1, 0.5,
                x, new double[]{
                0.0000000000, 0.0000000000, 0.7048336221, 0.7593042194, 0.7951765304,
                0.8234948385, 0.8480017124, 0.8706034370, 0.8926585878, 0.9156406404,
                0.9423662883, 1.0000000000, 1.0000000000});
        checkCumulative(0.1, 1.0,
                x, new double[]{
                0.0000000000, 0.0000000000, 0.7943282347, 0.8513399225, 0.8865681506,
                0.9124435366, 0.9330329915, 0.9502002165, 0.9649610951, 0.9779327685,
                0.9895192582, 1.0000000000, 1.0000000000});
        checkCumulative(0.1, 2.0,
                x, new double[]{
                0.0000000000, 0.0000000000, 0.8658177758, 0.9194471163, 0.9486279211,
                0.9671901487, 0.9796846411, 0.9882082252, 0.9939099280, 0.9974914239,
                0.9994144508, 1.0000000000, 1.0000000000});
        checkCumulative(0.1, 4.0,
                x, new double[]{
                0.0000000000, 0.0000000000, 0.9234991121, 0.9661958941, 0.9842285085,
                0.9928444112, 0.9970040660, 0.9989112804, 0.9996895625, 0.9999440793,
                0.9999967829, 1.0000000000, 1.0000000000});
        checkCumulative(0.5, 0.1,
                x, new double[]{
                0.00000000000, 0.00000000000, 0.05763371168, 0.08435935962,
                0.10734141216, 0.12939656302, 0.15199828760, 0.17650516146,
                0.20482346963, 0.24069578055, 0.29516637795, 1.00000000000, 1.00000000000});

        checkCumulative(0.5, 0.5,
                x, new double[]{
                0.0000000000, 0.0000000000, 0.2048327647, 0.2951672353, 0.3690101196,
                0.4359057832, 0.5000000000, 0.5640942168, 0.6309898804, 0.7048327647,
                0.7951672353, 1.0000000000, 1.0000000000});
        checkCumulative(0.5, 1.0,
                x, new double[]{
                0.0000000000, 0.0000000000, 0.3162277660, 0.4472135955, 0.5477225575,
                0.6324555320, 0.7071067812, 0.7745966692, 0.8366600265, 0.8944271910,
                0.9486832981, 1.0000000000, 1.0000000000});
        checkCumulative(0.5, 2.0,
                x, new double[]{
                0.0000000000, 0.0000000000, 0.4585302607, 0.6260990337, 0.7394254526,
                0.8221921916, 0.8838834765, 0.9295160031, 0.9621590305, 0.9838699101,
                0.9961174630, 1.0000000000, 1.0000000000});
        checkCumulative(0.5, 4.0,
                x, new double[]{
                0.0000000000, 0.0000000000, 0.6266250826, 0.8049844719, 0.8987784842,
                0.9502644369, 0.9777960959, 0.9914837366, 0.9974556254, 0.9995223859,
                0.9999714889, 1.0000000000, 1.0000000000});
        checkCumulative(1.0, 0.1,
                x, new double[]{
                0.00000000000, 0.00000000000, 0.01048074179, 0.02206723146,
                0.03503890488, 0.04979978349, 0.06696700846, 0.08755646344,
                0.11343184943, 0.14866007748, 0.20567176528, 1.00000000000, 1.00000000000});
        checkCumulative(1.0, 0.5,
                x, new double[]{
                0.00000000000, 0.00000000000, 0.05131670195, 0.10557280900,
                0.16333997347, 0.22540333076, 0.29289321881, 0.36754446797,
                0.45227744249, 0.55278640450, 0.68377223398, 1.00000000000, 1.00000000000});
        checkCumulative(1, 1,
                x, new double[]{
                0.0, 0.0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0, 1.0});
        checkCumulative(1, 2,
                x, new double[]{
                0.00, 0.00, 0.19, 0.36, 0.51, 0.64, 0.75, 0.84, 0.91, 0.96, 0.99, 1.00, 1.00});
        checkCumulative(1, 4,
                x, new double[]{
                0.0000, 0.0000, 0.3439, 0.5904, 0.7599, 0.8704, 0.9375, 0.9744, 0.9919,
                0.9984, 0.9999, 1.0000, 1.0000});
        checkCumulative(2.0, 0.1,
                x, new double[]{
                0.0000000000000, 0.0000000000000, 0.0005855492117, 0.0025085760862,
                0.0060900720266, 0.0117917748341, 0.0203153588864, 0.0328098512512,
                0.0513720788952, 0.0805528836776, 0.1341822241505, 1.0000000000000, 1.0000000000000});
        checkCumulative(2, 1,
                x, new double[]{
                0.00, 0.00, 0.01, 0.04, 0.09, 0.16, 0.25, 0.36, 0.49, 0.64, 0.81, 1.00, 1.00});
        checkCumulative(2.0, 0.5,
                x, new double[]{
                0.000000000000, 0.000000000000, 0.003882537047, 0.016130089900,
                0.037840969486, 0.070483996910, 0.116116523517, 0.177807808356,
                0.260574547368, 0.373900966300, 0.541469739276, 1.000000000000, 1.000000000000});
        checkCumulative(2, 2,
                x, new double[]{
                0.000, 0.000, 0.028, 0.104, 0.216, 0.352, 0.500, 0.648, 0.784, 0.896, 0.972, 1.000, 1.000});
        checkCumulative(2, 4,
                x, new double[]{
                0.00000, 0.00000, 0.08146, 0.26272, 0.47178, 0.66304, 0.81250, 0.91296,
                0.96922, 0.99328, 0.99954, 1.00000, 1.00000});
        checkCumulative(4.0, 0.1,
                x, new double[]{
                0.000000000e+00, 0.000000000e+00, 3.217128269e-06, 5.592070271e-05,
                3.104375474e-04, 1.088719595e-03, 2.995933981e-03, 7.155588777e-03,
                1.577149153e-02, 3.380410585e-02, 7.650088789e-02, 1.000000000e+00, 1.000000000e+00});
        checkCumulative(4.0, 0.5,
                x, new double[]{
                0.000000000e+00, 0.000000000e+00, 2.851114863e-05, 4.776140576e-04,
                2.544374616e-03, 8.516263371e-03, 2.220390414e-02, 4.973556312e-02,
                1.012215158e-01, 1.950155281e-01, 3.733749174e-01, 1.000000000e+00, 1.000000000e+00});
        checkCumulative(4, 1,
                x, new double[]{
                0.0000, 0.0000, 0.0001, 0.0016, 0.0081, 0.0256, 0.0625, 0.1296, 0.2401,
                0.4096, 0.6561, 1.0000, 1.0000});
        checkCumulative(4, 2,
                x, new double[]{
                0.00000, 0.00000, 0.00046, 0.00672, 0.03078, 0.08704, 0.18750, 0.33696,
                0.52822, 0.73728, 0.91854, 1.00000, 1.00000});
        checkCumulative(4, 4,
                x, new double[]{
                0.000000, 0.000000, 0.002728, 0.033344, 0.126036, 0.289792, 0.500000,
                0.710208, 0.873964, 0.966656, 0.997272, 1.000000, 1.000000});

    }

// org.apache.commons.math.distribution.BetaDistributionTest::testDensity
    public void testDensity() throws MathException {
        double[] x = new double[]{1e-6, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9};
        checkDensity(0.1, 0.1,
                x, new double[]{
                12741.2357380649, 0.4429889586665234, 2.639378715e-01, 2.066393611e-01,
                1.832401831e-01, 1.766302780e-01, 1.832404579e-01, 2.066400696e-01,
                2.639396531e-01, 4.429925026e-01});
        checkDensity(0.1, 0.5,
                x, new double[]{
                2.218377102e+04, 7.394524202e-01, 4.203020268e-01, 3.119435533e-01,
                2.600787829e-01, 2.330648626e-01, 2.211408259e-01, 2.222728708e-01,
                2.414013907e-01, 3.070567405e-01});
        checkDensity(0.1, 1.0,
                x, new double[]{
                2.511886432e+04, 7.943210858e-01, 4.256680458e-01, 2.955218303e-01,
                2.281103709e-01, 1.866062624e-01, 1.583664652e-01, 1.378514078e-01,
                1.222414585e-01, 1.099464743e-01});
        checkDensity(0.1, 2.0,
                x, new double[]{
                2.763072312e+04, 7.863770012e-01, 3.745874120e-01, 2.275514842e-01,
                1.505525939e-01, 1.026332391e-01, 6.968107049e-02, 4.549081293e-02,
                2.689298641e-02, 1.209399123e-02});
        checkDensity(0.1, 4.0,
                x, new double[]{
                2.997927462e+04, 6.911058917e-01, 2.601128486e-01, 1.209774010e-01,
                5.880564714e-02, 2.783915474e-02, 1.209657335e-02, 4.442148268e-03,
                1.167143939e-03, 1.312171805e-04});
        checkDensity(0.5, 0.1,
                x, new double[]{
                88.3152184726, 0.3070542841, 0.2414007269, 0.2222727015,
                0.2211409364, 0.2330652355, 0.2600795198, 0.3119449793,
                0.4203052841, 0.7394649088});
        checkDensity(0.5, 0.5,
                x, new double[]{
                318.3100453389, 1.0610282383, 0.7957732234, 0.6946084565,
                0.6497470636, 0.6366197724, 0.6497476051, 0.6946097796,
                0.7957762075, 1.0610376697});
        checkDensity(0.5, 1.0,
                x, new double[]{
                500.0000000000, 1.5811309244, 1.1180311937, 0.9128694077,
                0.7905684268, 0.7071060741, 0.6454966865, 0.5976138778,
                0.5590166450, 0.5270459839});
        checkDensity(0.5, 2.0,
                x, new double[]{
                749.99925000000, 2.134537420613655, 1.34163575536, 0.95851150881,
                0.71151039830, 0.53032849490, 0.38729704363, 0.26892534859,
                0.16770415497, 0.07905610701});
        checkDensity(0.5, 4.0,
                x, new double[]{
                1.093746719e+03, 2.52142232809988, 1.252190241e+00, 6.849343920e-01,
                3.735417140e-01, 1.933481570e-01, 9.036885833e-02, 3.529621669e-02,
                9.782644546e-03, 1.152878503e-03});
        checkDensity(1.0, 0.1,
                x, new double[]{
                0.1000000900, 0.1099466942, 0.1222417336, 0.1378517623, 0.1583669403,
                0.1866069342, 0.2281113974, 0.2955236034, 0.4256718768,
                0.7943353837});
        checkDensity(1.0, 0.5,
                x, new double[]{
                0.5000002500, 0.5270465695, 0.5590173438, 0.5976147315, 0.6454977623,
                0.7071074883, 0.7905704033, 0.9128724506,
                1.1180367838, 1.5811467358});
        checkDensity(1, 1,
                x, new double[]{
                1, 1, 1,
                1, 1, 1, 1,
                1, 1, 1});
        checkDensity(1, 2,
                x, new double[]{
                1.999998, 1.799998, 1.599998, 1.399998, 1.199998, 0.999998, 0.799998,
                0.599998, 0.399998,
                0.199998});
        checkDensity(1, 4,
                x, new double[]{
                3.999988000012, 2.915990280011, 2.047992320010, 1.371994120008,
                0.863995680007, 0.499997000006, 0.255998080005, 0.107998920004,
                0.031999520002, 0.003999880001});
        checkDensity(2.0, 0.1,
                x, new double[]{
                1.100000990e-07, 1.209425730e-02, 2.689331586e-02, 4.549123318e-02,
                6.968162794e-02, 1.026340191e-01, 1.505537732e-01, 2.275534997e-01,
                3.745917198e-01, 7.863929037e-01});
        checkDensity(2.0, 0.5,
                x, new double[]{
                7.500003750e-07, 7.905777599e-02, 1.677060417e-01, 2.689275256e-01,
                3.872996256e-01, 5.303316769e-01, 7.115145488e-01, 9.585174425e-01,
                1.341645818e+00, 2.134537420613655});
        checkDensity(2, 1,
                x, new double[]{
                0.000002, 0.200002, 0.400002, 0.600002, 0.800002, 1.000002, 1.200002,
                1.400002, 1.600002,
                1.800002});
        checkDensity(2, 2,
                x, new double[]{
                5.9999940e-06, 5.4000480e-01, 9.6000360e-01, 1.2600024e+00,
                1.4400012e+00, 1.5000000e+00, 1.4399988e+00, 1.2599976e+00,
                9.5999640e-01, 5.3999520e-01});
        checkDensity(2, 4,
                x, new double[]{
                0.00001999994, 1.45800971996, 2.04800255997, 2.05799803998,
                1.72799567999, 1.24999500000, 0.76799552000, 0.37799676001,
                0.12799824001, 0.01799948000});
        checkDensity(4.0, 0.1,
                x, new double[]{
                1.193501074e-19, 1.312253162e-04, 1.167181580e-03, 4.442248535e-03,
                1.209679109e-02, 2.783958903e-02, 5.880649983e-02, 1.209791638e-01,
                2.601171405e-01, 6.911229392e-01});
        checkDensity(4.0, 0.5,
                x, new double[]{
                1.093750547e-18, 1.152948959e-03, 9.782950259e-03, 3.529697305e-02,
                9.037036449e-02, 1.933508639e-01, 3.735463833e-01, 6.849425461e-01,
                1.252205894e+00, 2.52142232809988});
        checkDensity(4, 1,
                x, new double[]{
                4.000000000e-18, 4.000120001e-03, 3.200048000e-02, 1.080010800e-01,
                2.560019200e-01, 5.000030000e-01, 8.640043200e-01, 1.372005880e+00,
                2.048007680e+00, 2.916009720e+00});
        checkDensity(4, 2,
                x, new double[]{
                1.999998000e-17, 1.800052000e-02, 1.280017600e-01, 3.780032400e-01,
                7.680044800e-01, 1.250005000e+00, 1.728004320e+00, 2.058001960e+00,
                2.047997440e+00, 1.457990280e+00});
        checkDensity(4, 4,
                x, new double[]{
                1.399995800e-16, 1.020627216e-01, 5.734464512e-01, 1.296547409e+00,
                1.935364838e+00, 2.187500000e+00, 1.935355162e+00, 1.296532591e+00,
                5.734335488e-01, 1.020572784e-01});

    }

// org.apache.commons.math.distribution.ChiSquareDistributionTest::testSmallDf
    public void testSmallDf() throws Exception {
        setDistribution(new ChiSquaredDistributionImpl(0.1d));
        setTolerance(1E-4);
        
        setCumulativeTestPoints(new double[] {1.168926E-60, 1.168926E-40, 1.063132E-32, 
                1.144775E-26, 1.168926E-20, 5.472917, 2.175255, 1.13438, 
                0.5318646, 0.1526342});
        setInverseCumulativeTestValues(getCumulativeTestPoints());
        setInverseCumulativeTestPoints(getCumulativeTestValues());
        verifyCumulativeProbabilities();
        verifyInverseCumulativeProbabilities();
    }

// org.apache.commons.math.distribution.ChiSquareDistributionTest::testDfAccessors
    public void testDfAccessors() {
        ChiSquaredDistribution distribution = (ChiSquaredDistribution) getDistribution();
        assertEquals(5d, distribution.getDegreesOfFreedom(), Double.MIN_VALUE);
        distribution.setDegreesOfFreedom(4d);
        assertEquals(4d, distribution.getDegreesOfFreedom(), Double.MIN_VALUE);
        try {
            distribution.setDegreesOfFreedom(0d);
            fail("Expecting IllegalArgumentException for df = 0");
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.distribution.ChiSquareDistributionTest::testDensity
    public void testDensity() {
        double[] x = new double[]{-0.1, 1e-6, 0.5, 1, 2, 5};
        
        checkDensity(1, x, new double[]{0.00000000000, 398.94208093034, 0.43939128947, 0.24197072452, 0.10377687436, 0.01464498256});
        
        checkDensity(0.1, x, new double[]{0.000000000e+00, 2.486453997e+04, 7.464238732e-02, 3.009077718e-02, 9.447299159e-03, 8.827199396e-04});
        
        checkDensity(2, x, new double[]{0.00000000000, 0.49999975000, 0.38940039154, 0.30326532986, 0.18393972059, 0.04104249931});
        
        checkDensity(10, x, new double[]{0.000000000e+00, 1.302082682e-27, 6.337896998e-05, 7.897534632e-04, 7.664155024e-03, 6.680094289e-02});
    }

// org.apache.commons.math.distribution.ChiSquareDistributionTest::testSampling
    public void testSampling() {}

// org.apache.commons.math.distribution.FDistributionTest::testCumulativeProbabilityExtremes
    public void testCumulativeProbabilityExtremes() throws Exception {
        setCumulativeTestPoints(new double[] {-2, 0});
        setCumulativeTestValues(new double[] {0, 0});
        verifyCumulativeProbabilities();
    }

// org.apache.commons.math.distribution.FDistributionTest::testInverseCumulativeProbabilityExtremes
    public void testInverseCumulativeProbabilityExtremes() throws Exception {
        setInverseCumulativeTestPoints(new double[] {0, 1});
        setInverseCumulativeTestValues(new double[] {0, Double.POSITIVE_INFINITY});
        verifyInverseCumulativeProbabilities();
    }

// org.apache.commons.math.distribution.FDistributionTest::testDfAccessors
    public void testDfAccessors() {
        FDistribution distribution = (FDistribution) getDistribution();
        assertEquals(5d, distribution.getNumeratorDegreesOfFreedom(), Double.MIN_VALUE);
        distribution.setNumeratorDegreesOfFreedom(4d);
        assertEquals(4d, distribution.getNumeratorDegreesOfFreedom(), Double.MIN_VALUE);
        assertEquals(6d, distribution.getDenominatorDegreesOfFreedom(), Double.MIN_VALUE);
        distribution.setDenominatorDegreesOfFreedom(4d);
        assertEquals(4d, distribution.getDenominatorDegreesOfFreedom(), Double.MIN_VALUE);
        try {
            distribution.setNumeratorDegreesOfFreedom(0d);
            fail("Expecting IllegalArgumentException for df = 0");
        } catch (IllegalArgumentException ex) {
            
        }
        try {
            distribution.setDenominatorDegreesOfFreedom(0d);
            fail("Expecting IllegalArgumentException for df = 0");
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.distribution.FDistributionTest::testLargeDegreesOfFreedom
    public void testLargeDegreesOfFreedom() throws Exception {
        org.apache.commons.math.distribution.FDistributionImpl fd =
            new org.apache.commons.math.distribution.FDistributionImpl(
                100000., 100000.);
        double p = fd.cumulativeProbability(.999);
        double x = fd.inverseCumulativeProbability(p);
        assertEquals(.999, x, 1.0e-5);
    }

// org.apache.commons.math.distribution.FDistributionTest::testSmallDegreesOfFreedom
    public void testSmallDegreesOfFreedom() throws Exception {
        org.apache.commons.math.distribution.FDistributionImpl fd =
            new org.apache.commons.math.distribution.FDistributionImpl(
                1.0, 1.0);
        double p = fd.cumulativeProbability(0.975);
        double x = fd.inverseCumulativeProbability(p);
        assertEquals(0.975, x, 1.0e-5);

        fd.setDenominatorDegreesOfFreedom(2.0);
        p = fd.cumulativeProbability(0.975);
        x = fd.inverseCumulativeProbability(p);
        assertEquals(0.975, x, 1.0e-5);
    }

// org.apache.commons.math.distribution.GammaDistributionTest::testParameterAccessors
    public void testParameterAccessors() {
        GammaDistribution distribution = (GammaDistribution) getDistribution();
        assertEquals(4d, distribution.getAlpha(), 0);
        distribution.setAlpha(3d);
        assertEquals(3d, distribution.getAlpha(), 0);
        assertEquals(2d, distribution.getBeta(), 0);
        distribution.setBeta(4d);
        assertEquals(4d, distribution.getBeta(), 0);
        try {
            distribution.setAlpha(0d);
            fail("Expecting IllegalArgumentException for alpha = 0");
        } catch (IllegalArgumentException ex) {
            
        }
        try {
            distribution.setBeta(0d);
            fail("Expecting IllegalArgumentException for beta = 0");
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.distribution.GammaDistributionTest::testProbabilities
    public void testProbabilities() throws Exception {
        testProbability(-1.000, 4.0, 2.0, .0000);
        testProbability(15.501, 4.0, 2.0, .9499);
        testProbability(0.504, 4.0, 1.0, .0018);
        testProbability(10.011, 1.0, 2.0, .9933);
        testProbability(5.000, 2.0, 2.0, .7127);
    }

// org.apache.commons.math.distribution.GammaDistributionTest::testValues
    public void testValues() throws Exception {
        testValue(15.501, 4.0, 2.0, .9499);
        testValue(0.504, 4.0, 1.0, .0018);
        testValue(10.011, 1.0, 2.0, .9933);
        testValue(5.000, 2.0, 2.0, .7127);
    }

// org.apache.commons.math.distribution.GammaDistributionTest::testDensity
    public void testDensity() {
        double[] x = new double[]{-0.1, 1e-6, 0.5, 1, 2, 5};
        
        checkDensity(1, 1, x, new double[]{0.000000000000, 0.999999000001, 0.606530659713, 0.367879441171, 0.135335283237, 0.006737946999});
        
        checkDensity(2, 1, x, new double[]{0.000000000000, 0.000000999999, 0.303265329856, 0.367879441171, 0.270670566473, 0.033689734995});
        
        checkDensity(4, 1, x, new double[]{0.000000000e+00, 1.666665000e-19, 1.263605541e-02, 6.131324020e-02, 1.804470443e-01, 1.403738958e-01});
        
        checkDensity(4, 10, x, new double[]{0.000000000e+00, 1.666650000e-15, 1.403738958e+00, 7.566654960e-02, 2.748204830e-05, 4.018228850e-17});
        
        checkDensity(0.1, 10, x, new double[]{0.000000000e+00, 3.323953832e+04, 1.663849010e-03, 6.007786726e-06, 1.461647647e-10, 5.996008322e-24});
        
        checkDensity(0.1, 20, x, new double[]{0.000000000e+00, 3.562489883e+04, 1.201557345e-05, 2.923295295e-10, 3.228910843e-19, 1.239484589e-45});
        
        checkDensity(0.1, 4, x, new double[]{0.000000000e+00, 3.032938388e+04, 3.049322494e-02, 2.211502311e-03, 2.170613371e-05, 5.846590589e-11});
        
        checkDensity(0.1, 1, x, new double[]{0.000000000e+00, 2.640334143e+04, 1.189704437e-01, 3.866916944e-02, 7.623306235e-03, 1.663849010e-04});
    }

// org.apache.commons.math.distribution.GammaDistributionTest::testInverseCumulativeProbabilityExtremes
    public void testInverseCumulativeProbabilityExtremes() throws Exception {
        setInverseCumulativeTestPoints(new double[] {0, 1});
        setInverseCumulativeTestValues(new double[] {0, Double.POSITIVE_INFINITY});
        verifyInverseCumulativeProbabilities();
    }

// org.apache.commons.math.distribution.NormalDistributionTest::testQuantiles
    public void testQuantiles() throws Exception {
        verifyQuantiles();
        setDistribution(new NormalDistributionImpl(0, 1));
        verifyQuantiles();
        setDistribution(new NormalDistributionImpl(0, 0.1));
        verifyQuantiles();
    }

// org.apache.commons.math.distribution.NormalDistributionTest::testInverseCumulativeProbabilityExtremes
    public void testInverseCumulativeProbabilityExtremes() throws Exception {
        setInverseCumulativeTestPoints(new double[] {0, 1});
        setInverseCumulativeTestValues(
                new double[] {Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY});
        verifyInverseCumulativeProbabilities();
    }

// org.apache.commons.math.distribution.NormalDistributionTest::testGetMean
    public void testGetMean() {
        NormalDistribution distribution = (NormalDistribution) getDistribution();
        assertEquals(2.1, distribution.getMean(), 0);
    }

// org.apache.commons.math.distribution.NormalDistributionTest::testSetMean
    public void testSetMean() throws Exception {
        double mu = Math.random();
        NormalDistribution distribution = (NormalDistribution) getDistribution();
        distribution.setMean(mu);
        verifyQuantiles();
    }

// org.apache.commons.math.distribution.NormalDistributionTest::testGetStandardDeviation
    public void testGetStandardDeviation() {
        NormalDistribution distribution = (NormalDistribution) getDistribution();
        assertEquals(1.4, distribution.getStandardDeviation(), 0);  
    }

// org.apache.commons.math.distribution.NormalDistributionTest::testSetStandardDeviation
    public void testSetStandardDeviation() throws Exception {
        double sigma = 0.1d + Math.random();
        NormalDistribution distribution = (NormalDistribution) getDistribution();
        distribution.setStandardDeviation(sigma);
        assertEquals(sigma, distribution.getStandardDeviation(), 0);
        verifyQuantiles();
        try {
            distribution.setStandardDeviation(0);
            fail("Expecting IllegalArgumentException for sd = 0");
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.distribution.NormalDistributionTest::testDensity
    public void testDensity() {
        double [] x = new double[]{-2, -1, 0, 1, 2};
        
        checkDensity(0, 1, x, new double[]{0.05399096651, 0.24197072452, 0.39894228040, 0.24197072452, 0.05399096651});
        
        checkDensity(1.1, 1, x, new double[]{0.003266819056,0.043983595980,0.217852177033,0.396952547477,0.266085249899});
    }

// org.apache.commons.math.distribution.NormalDistributionTest::testExtremeValues
    public void testExtremeValues() throws Exception {
        NormalDistribution distribution = (NormalDistribution) getDistribution();
        distribution.setMean(0);
        distribution.setStandardDeviation(1);
        for (int i = 0; i < 100; i+=5) { 
            double lowerTail = distribution.cumulativeProbability(-i);
            double upperTail = distribution.cumulativeProbability(i);
            if (i < 10) { 
                assertTrue(lowerTail > 0.0d);
                assertTrue(upperTail < 1.0d);
            }
            else { 
                assertTrue(lowerTail < 0.00001);
                assertTrue(upperTail > 0.99999);
            }
        } 
   }

// org.apache.commons.math.distribution.NormalDistributionTest::testMath280
    public void testMath280() throws MathException {
        NormalDistribution normal = new NormalDistributionImpl(0,1);
        double result = normal.inverseCumulativeProbability(0.9772498680518209);
        assertEquals(2.0, result, 1.0e-12);
    }

// org.apache.commons.math.distribution.TDistributionTest::testCumulativeProbabilityAgaintStackOverflow
    public void testCumulativeProbabilityAgaintStackOverflow() throws Exception {
        TDistributionImpl td = new TDistributionImpl(5.);
        td.cumulativeProbability(.1);
        td.cumulativeProbability(.01);
    }

// org.apache.commons.math.distribution.TDistributionTest::testSmallDf
    public void testSmallDf() throws Exception {
        setDistribution(new TDistributionImpl(1d));
        setTolerance(1E-4);
        
        setCumulativeTestPoints(new double[] {-318.3088, -31.82052, -12.70620, -6.313752,
            -3.077684, 0.0, 318.3088, 31.82052, 12.70620,
            6.313752, 3.077684});
        setInverseCumulativeTestValues(getCumulativeTestPoints());
        verifyCumulativeProbabilities();
        verifyInverseCumulativeProbabilities();
    }

// org.apache.commons.math.distribution.TDistributionTest::testInverseCumulativeProbabilityExtremes
    public void testInverseCumulativeProbabilityExtremes() throws Exception {
        setInverseCumulativeTestPoints(new double[] {0, 1});
        setInverseCumulativeTestValues(
                new double[] {Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY});
        verifyInverseCumulativeProbabilities();
    }

// org.apache.commons.math.distribution.TDistributionTest::testDfAccessors
    public void testDfAccessors() {
        TDistribution distribution = (TDistribution) getDistribution();
        assertEquals(5d, distribution.getDegreesOfFreedom(), Double.MIN_VALUE);
        distribution.setDegreesOfFreedom(4d);
        assertEquals(4d, distribution.getDegreesOfFreedom(), Double.MIN_VALUE);
        try {
            distribution.setDegreesOfFreedom(0d);
            fail("Expecting IllegalArgumentException for df = 0");
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.stat.regression.SimpleRegressionTest::testNorris
    public void testNorris() {
        SimpleRegression regression = new SimpleRegression();
        for (int i = 0; i < data.length; i++) {
            regression.addData(data[i][1], data[i][0]);
        }
        
        
        assertEquals("slope", 1.00211681802045, regression.getSlope(), 10E-12);
        assertEquals("slope std err", 0.429796848199937E-03,
                regression.getSlopeStdErr(),10E-12);
        assertEquals("number of observations", 36, regression.getN());
        assertEquals( "intercept", -0.262323073774029,
            regression.getIntercept(),10E-12);
        assertEquals("std err intercept", 0.232818234301152,
            regression.getInterceptStdErr(),10E-12);
        assertEquals("r-square", 0.999993745883712,
            regression.getRSquare(), 10E-12);
        assertEquals("SSR", 4255954.13232369,
            regression.getRegressionSumSquares(), 10E-9);
        assertEquals("MSE", 0.782864662630069,
            regression.getMeanSquareError(), 10E-10);
        assertEquals("SSE", 26.6173985294224,
            regression.getSumSquaredErrors(),10E-9);
        
          
        assertEquals( "predict(0)",  -0.262323073774029,
            regression.predict(0), 10E-12);
        assertEquals("predict(1)", 1.00211681802045 - 0.262323073774029,
            regression.predict(1), 10E-12);
    }

// org.apache.commons.math.stat.regression.SimpleRegressionTest::testCorr
    public void testCorr() {
        SimpleRegression regression = new SimpleRegression();
        regression.addData(corrData);
        assertEquals("number of observations", 17, regression.getN());
        assertEquals("r-square", .896123, regression.getRSquare(), 10E-6);
        assertEquals("r", -0.94663767742, regression.getR(), 1E-10);
    }

// org.apache.commons.math.stat.regression.SimpleRegressionTest::testNaNs
    public void testNaNs() {
        SimpleRegression regression = new SimpleRegression();
        assertTrue("intercept not NaN", Double.isNaN(regression.getIntercept()));
        assertTrue("slope not NaN", Double.isNaN(regression.getSlope()));
        assertTrue("slope std err not NaN", Double.isNaN(regression.getSlopeStdErr()));
        assertTrue("intercept std err not NaN", Double.isNaN(regression.getInterceptStdErr()));
        assertTrue("MSE not NaN", Double.isNaN(regression.getMeanSquareError()));
        assertTrue("e not NaN", Double.isNaN(regression.getR()));
        assertTrue("r-square not NaN", Double.isNaN(regression.getRSquare()));
        assertTrue( "RSS not NaN", Double.isNaN(regression.getRegressionSumSquares()));
        assertTrue("SSE not NaN",Double.isNaN(regression.getSumSquaredErrors()));
        assertTrue("SSTO not NaN", Double.isNaN(regression.getTotalSumSquares()));
        assertTrue("predict not NaN", Double.isNaN(regression.predict(0)));

        regression.addData(1, 2);
        regression.addData(1, 3);

        
        assertTrue("intercept not NaN", Double.isNaN(regression.getIntercept()));
        assertTrue("slope not NaN", Double.isNaN(regression.getSlope()));
        assertTrue("slope std err not NaN", Double.isNaN(regression.getSlopeStdErr()));
        assertTrue("intercept std err not NaN", Double.isNaN(regression.getInterceptStdErr()));
        assertTrue("MSE not NaN", Double.isNaN(regression.getMeanSquareError()));
        assertTrue("e not NaN", Double.isNaN(regression.getR()));
        assertTrue("r-square not NaN", Double.isNaN(regression.getRSquare()));
        assertTrue("RSS not NaN", Double.isNaN(regression.getRegressionSumSquares()));
        assertTrue("SSE not NaN", Double.isNaN(regression.getSumSquaredErrors()));
        assertTrue("predict not NaN", Double.isNaN(regression.predict(0)));

        
        assertTrue("SSTO NaN", !Double.isNaN(regression.getTotalSumSquares()));

        regression = new SimpleRegression();

        regression.addData(1, 2);
        regression.addData(3, 3);

        
        assertTrue("interceptNaN", !Double.isNaN(regression.getIntercept()));
        assertTrue("slope NaN", !Double.isNaN(regression.getSlope()));
        assertTrue ("slope std err not NaN", Double.isNaN(regression.getSlopeStdErr()));
        assertTrue("intercept std err not NaN", Double.isNaN(regression.getInterceptStdErr()));
        assertTrue("MSE not NaN", Double.isNaN(regression.getMeanSquareError()));
        assertTrue("r NaN", !Double.isNaN(regression.getR()));
        assertTrue("r-square NaN", !Double.isNaN(regression.getRSquare()));
        assertTrue("RSS NaN", !Double.isNaN(regression.getRegressionSumSquares()));
        assertTrue("SSE NaN", !Double.isNaN(regression.getSumSquaredErrors()));
        assertTrue("SSTO NaN", !Double.isNaN(regression.getTotalSumSquares()));
        assertTrue("predict NaN", !Double.isNaN(regression.predict(0)));

        regression.addData(1, 4);

        
        assertTrue("MSE NaN", !Double.isNaN(regression.getMeanSquareError()));
        assertTrue("slope std err NaN", !Double.isNaN(regression.getSlopeStdErr()));
        assertTrue("intercept std err NaN", !Double.isNaN(regression.getInterceptStdErr()));
    }

// org.apache.commons.math.stat.regression.SimpleRegressionTest::testClear
    public void testClear() {
        SimpleRegression regression = new SimpleRegression();
        regression.addData(corrData);
        assertEquals("number of observations", 17, regression.getN());
        regression.clear();
        assertEquals("number of observations", 0, regression.getN());
        regression.addData(corrData);
        assertEquals("r-square", .896123, regression.getRSquare(), 10E-6);
        regression.addData(data);
        assertEquals("number of observations", 53, regression.getN());
    }

// org.apache.commons.math.stat.regression.SimpleRegressionTest::testInference
    public void testInference() throws Exception {
        
        
        SimpleRegression regression = new SimpleRegression();
        regression.addData(infData);
        assertEquals("slope std err", 0.011448491,
                regression.getSlopeStdErr(), 1E-10);
        assertEquals("std err intercept", 0.286036932,
                regression.getInterceptStdErr(),1E-8);
        assertEquals("significance", 4.596e-07,
                regression.getSignificance(),1E-8);    
        assertEquals("slope conf interval half-width", 0.0270713794287, 
                regression.getSlopeConfidenceInterval(),1E-8);
        
        regression = new SimpleRegression();
        regression.addData(infData2);
        assertEquals("slope std err", 1.07260253,
                regression.getSlopeStdErr(), 1E-8);
        assertEquals("std err intercept",4.17718672,
                regression.getInterceptStdErr(),1E-8);
        assertEquals("significance", 0.261829133982,
                regression.getSignificance(),1E-11);    
        assertEquals("slope conf interval half-width", 2.97802204827, 
                regression.getSlopeConfidenceInterval(),1E-8);
        
        
        
        assertTrue("tighter means wider",
                regression.getSlopeConfidenceInterval() < regression.getSlopeConfidenceInterval(0.01));
     
        try {
            regression.getSlopeConfidenceInterval(1);
            fail("expecting IllegalArgumentException for alpha = 1");
        } catch (IllegalArgumentException ex) {
            
        }  

    }

// org.apache.commons.math.stat.regression.SimpleRegressionTest::testPerfect
    public void testPerfect() throws Exception {
        SimpleRegression regression = new SimpleRegression();
        int n = 100;
        for (int i = 0; i < n; i++) {
            regression.addData(((double) i) / (n - 1), i);
        }
        assertEquals(0.0, regression.getSignificance(), 1.0e-5);
        assertTrue(regression.getSlope() > 0.0);
        assertTrue(regression.getSumSquaredErrors() >= 0.0);
    }

// org.apache.commons.math.stat.regression.SimpleRegressionTest::testPerfectNegative
    public void testPerfectNegative() throws Exception {
        SimpleRegression regression = new SimpleRegression();
        int n = 100;
        for (int i = 0; i < n; i++) {
            regression.addData(- ((double) i) / (n - 1), i);
        }
   
        assertEquals(0.0, regression.getSignificance(), 1.0e-5);
        assertTrue(regression.getSlope() < 0.0);   
    }

// org.apache.commons.math.stat.regression.SimpleRegressionTest::testRandom
    public void testRandom() throws Exception {
        SimpleRegression regression = new SimpleRegression();
        Random random = new Random(1);
        int n = 100;
        for (int i = 0; i < n; i++) {
            regression.addData(((double) i) / (n - 1), random.nextDouble());
        }

        assertTrue( 0.0 < regression.getSignificance()
                    && regression.getSignificance() < 1.0);       
    }

// org.apache.commons.math.stat.regression.SimpleRegressionTest::testSSENonNegative
    public void testSSENonNegative() {
        double[] y = { 8915.102, 8919.302, 8923.502 };
        double[] x = { 1.107178495E2, 1.107264895E2, 1.107351295E2 };
        SimpleRegression reg = new SimpleRegression();
        for (int i = 0; i < x.length; i++) {
            reg.addData(x[i], y[i]);
        }
        assertTrue(reg.getSumSquaredErrors() >= 0.0);
    }

// org.apache.commons.math.stat.regression.SimpleRegressionTest::testRemoveXY
    public void testRemoveXY() throws Exception {
        
        SimpleRegression regression = new SimpleRegression();
        regression.addData(infData);
        regression.removeData(removeX, removeY);
        regression.addData(removeX, removeY);
        
        assertEquals("slope std err", 0.011448491,
                regression.getSlopeStdErr(), 1E-10);
        assertEquals("std err intercept", 0.286036932,
                regression.getInterceptStdErr(),1E-8);
        assertEquals("significance", 4.596e-07,
                regression.getSignificance(),1E-8);    
        assertEquals("slope conf interval half-width", 0.0270713794287, 
                regression.getSlopeConfidenceInterval(),1E-8);
     }

// org.apache.commons.math.stat.regression.SimpleRegressionTest::testRemoveSingle
    public void testRemoveSingle() throws Exception {
        
        SimpleRegression regression = new SimpleRegression();
        regression.addData(infData);
        regression.removeData(removeSingle);
        regression.addData(removeSingle);
        
        assertEquals("slope std err", 0.011448491,
                regression.getSlopeStdErr(), 1E-10);
        assertEquals("std err intercept", 0.286036932,
                regression.getInterceptStdErr(),1E-8);
        assertEquals("significance", 4.596e-07,
                regression.getSignificance(),1E-8);    
        assertEquals("slope conf interval half-width", 0.0270713794287, 
                regression.getSlopeConfidenceInterval(),1E-8);
     }

// org.apache.commons.math.stat.regression.SimpleRegressionTest::testRemoveMultiple
    public void testRemoveMultiple() throws Exception {
        
        SimpleRegression regression = new SimpleRegression();
        regression.addData(infData);
        regression.removeData(removeMultiple);
        regression.addData(removeMultiple);
        
        assertEquals("slope std err", 0.011448491,
                regression.getSlopeStdErr(), 1E-10);
        assertEquals("std err intercept", 0.286036932,
                regression.getInterceptStdErr(),1E-8);
        assertEquals("significance", 4.596e-07,
                regression.getSignificance(),1E-8);    
        assertEquals("slope conf interval half-width", 0.0270713794287, 
                regression.getSlopeConfidenceInterval(),1E-8);
     }

// org.apache.commons.math.stat.regression.SimpleRegressionTest::testRemoveObsFromEmpty
    public void testRemoveObsFromEmpty() {
        SimpleRegression regression = new SimpleRegression();
        regression.removeData(removeX, removeY);
        assertEquals(regression.getN(), 0);
    }

// org.apache.commons.math.stat.regression.SimpleRegressionTest::testRemoveObsFromSingle
    public void testRemoveObsFromSingle() {
        SimpleRegression regression = new SimpleRegression();
        regression.addData(removeX, removeY);
        regression.removeData(removeX, removeY);
        assertEquals(regression.getN(), 0);
    }

// org.apache.commons.math.stat.regression.SimpleRegressionTest::testRemoveMultipleToEmpty
    public void testRemoveMultipleToEmpty() {
        SimpleRegression regression = new SimpleRegression();
        regression.addData(removeMultiple);
        regression.removeData(removeMultiple);
        assertEquals(regression.getN(), 0);
    }

// org.apache.commons.math.stat.regression.SimpleRegressionTest::testRemoveMultiplePastEmpty
    public void testRemoveMultiplePastEmpty() {
        SimpleRegression regression = new SimpleRegression();
        regression.addData(removeX, removeY);
        regression.removeData(removeMultiple);
        assertEquals(regression.getN(), 0);
    }
