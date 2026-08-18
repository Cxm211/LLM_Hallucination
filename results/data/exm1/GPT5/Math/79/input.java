// buggy code
    public static double distance(int[] p1, int[] p2) {
      int sum = 0;
      for (int i = 0; i < p1.length; i++) {
          final int dp = p1[i] - p2[i];
          sum += dp * dp;
      }
      return Math.sqrt(sum);
    }

// relevant test
// org.apache.commons.math.analysis.interpolation.MicrosphereInterpolatorTest::testLinearFunction2D
    public void testLinearFunction2D() throws MathException {
        MultivariateRealFunction f = new MultivariateRealFunction() {
                public double value(double[] x) {
                    if (x.length != 2) {
                        throw new IllegalArgumentException();
                    }
                    return 2 * x[0] - 3 * x[1] + 5;
                }
            };

        MultivariateRealInterpolator interpolator = new MicrosphereInterpolator();

        
        final int n = 9;
        final int dim = 2;
        double[][] x = new double[n][dim];
        double[] y = new double[n];
        int index = 0;
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                x[index][0] = i;
                x[index][1] = j;
                y[index] = f.value(x[index]);
                ++index;
            }
        }

        MultivariateRealFunction p = interpolator.interpolate(x, y);

        double[] c = new double[dim];
        double expected, result;

        c[0] = 0;
        c[1] = 0;
        expected = f.value(c);
        result = p.value(c);
        Assert.assertEquals("On sample point", expected, result, Math.ulp(1d));

        c[0] = 0 + 1e-5;
        c[1] = 1 - 1e-5;
        expected = f.value(c);
        result = p.value(c);
        Assert.assertEquals("1e-5 away from sample point", expected, result, 1e-4);
    }

// org.apache.commons.math.analysis.interpolation.MicrosphereInterpolatorTest::testParaboloid2D
    public void testParaboloid2D() throws MathException {
        MultivariateRealFunction f = new MultivariateRealFunction() {
                public double value(double[] x) {
                    if (x.length != 2) {
                        throw new IllegalArgumentException();
                    }
                    return 2 * x[0] * x[0] - 3 * x[1] * x[1] + 4 * x[0] * x[1] - 5;
                }
            };

        MultivariateRealInterpolator interpolator = new MicrosphereInterpolator();

        
        final int n = 121;
        final int dim = 2;
        double[][] x = new double[n][dim];
        double[] y = new double[n];
        int index = 0;
        for (int i = -10; i <= 10; i += 2) {
            for (int j = -10; j <= 10; j += 2) {
                x[index][0] = i;
                x[index][1] = j;
                y[index] = f.value(x[index]);
                ++index;
            }
        }

        MultivariateRealFunction p = interpolator.interpolate(x, y);

        double[] c = new double[dim];
        double expected, result;

        c[0] = 0;
        c[1] = 0;
        expected = f.value(c);
        result = p.value(c);
        Assert.assertEquals("On sample point", expected, result, Math.ulp(1d));

        c[0] = 2 + 1e-5;
        c[1] = 2 - 1e-5;
        expected = f.value(c);
        result = p.value(c);
        Assert.assertEquals("1e-5 away from sample point", expected, result, 1e-3);
    }

// org.apache.commons.math.analysis.solvers.LaguerreSolverTest::testDeprecated
    public void testDeprecated() throws MathException {
        double min, max, expected, result, tolerance;

        
        double coefficients[] = { -1.0, 4.0 };
        PolynomialFunction f = new PolynomialFunction(coefficients);
        UnivariateRealSolver solver = new LaguerreSolver(f);

        min = 0.0; max = 1.0; expected = 0.25;
        tolerance = Math.max(solver.getAbsoluteAccuracy(),
                    Math.abs(expected * solver.getRelativeAccuracy()));
        result = solver.solve(min, max);
        assertEquals(expected, result, tolerance);
    }

// org.apache.commons.math.analysis.solvers.LaguerreSolverTest::testLinearFunction
    public void testLinearFunction() throws MathException {
        double min, max, expected, result, tolerance;

        
        double coefficients[] = { -1.0, 4.0 };
        PolynomialFunction f = new PolynomialFunction(coefficients);
        UnivariateRealSolver solver = new LaguerreSolver();

        min = 0.0; max = 1.0; expected = 0.25;
        tolerance = Math.max(solver.getAbsoluteAccuracy(),
                    Math.abs(expected * solver.getRelativeAccuracy()));
        result = solver.solve(f, min, max);
        assertEquals(expected, result, tolerance);
    }

// org.apache.commons.math.analysis.solvers.LaguerreSolverTest::testQuadraticFunction
    public void testQuadraticFunction() throws MathException {
        double min, max, expected, result, tolerance;

        
        double coefficients[] = { -3.0, 5.0, 2.0 };
        PolynomialFunction f = new PolynomialFunction(coefficients);
        UnivariateRealSolver solver = new LaguerreSolver();

        min = 0.0; max = 2.0; expected = 0.5;
        tolerance = Math.max(solver.getAbsoluteAccuracy(),
                    Math.abs(expected * solver.getRelativeAccuracy()));
        result = solver.solve(f, min, max);
        assertEquals(expected, result, tolerance);

        min = -4.0; max = -1.0; expected = -3.0;
        tolerance = Math.max(solver.getAbsoluteAccuracy(),
                    Math.abs(expected * solver.getRelativeAccuracy()));
        result = solver.solve(f, min, max);
        assertEquals(expected, result, tolerance);
    }

// org.apache.commons.math.analysis.solvers.LaguerreSolverTest::testQuinticFunction
    public void testQuinticFunction() throws MathException {
        double min, max, expected, result, tolerance;

        
        double coefficients[] = { -12.0, -1.0, 1.0, -12.0, -1.0, 1.0 };
        PolynomialFunction f = new PolynomialFunction(coefficients);
        UnivariateRealSolver solver = new LaguerreSolver();

        min = -2.0; max = 2.0; expected = -1.0;
        tolerance = Math.max(solver.getAbsoluteAccuracy(),
                    Math.abs(expected * solver.getRelativeAccuracy()));
        result = solver.solve(f, min, max);
        assertEquals(expected, result, tolerance);

        min = -5.0; max = -2.5; expected = -3.0;
        tolerance = Math.max(solver.getAbsoluteAccuracy(),
                    Math.abs(expected * solver.getRelativeAccuracy()));
        result = solver.solve(f, min, max);
        assertEquals(expected, result, tolerance);

        min = 3.0; max = 6.0; expected = 4.0;
        tolerance = Math.max(solver.getAbsoluteAccuracy(),
                    Math.abs(expected * solver.getRelativeAccuracy()));
        result = solver.solve(f, min, max);
        assertEquals(expected, result, tolerance);
    }

// org.apache.commons.math.analysis.solvers.LaguerreSolverTest::testQuinticFunction2
    public void testQuinticFunction2() throws MathException {
        double initial = 0.0, tolerance;
        Complex expected, result[];

        
        double coefficients[] = { 4.0, 0.0, 1.0, 4.0, 0.0, 1.0 };
        LaguerreSolver solver = new LaguerreSolver();
        result = solver.solveAll(coefficients, initial);

        expected = new Complex(0.0, -2.0);
        tolerance = Math.max(solver.getAbsoluteAccuracy(),
                    Math.abs(expected.abs() * solver.getRelativeAccuracy()));
        TestUtils.assertContains(result, expected, tolerance);

        expected = new Complex(0.0, 2.0);
        tolerance = Math.max(solver.getAbsoluteAccuracy(),
                    Math.abs(expected.abs() * solver.getRelativeAccuracy()));
        TestUtils.assertContains(result, expected, tolerance);

        expected = new Complex(0.5, 0.5 * Math.sqrt(3.0));
        tolerance = Math.max(solver.getAbsoluteAccuracy(),
                    Math.abs(expected.abs() * solver.getRelativeAccuracy()));
        TestUtils.assertContains(result, expected, tolerance);

        expected = new Complex(-1.0, 0.0);
        tolerance = Math.max(solver.getAbsoluteAccuracy(),
                    Math.abs(expected.abs() * solver.getRelativeAccuracy()));
        TestUtils.assertContains(result, expected, tolerance);

        expected = new Complex(0.5, -0.5 * Math.sqrt(3.0));
        tolerance = Math.max(solver.getAbsoluteAccuracy(),
                    Math.abs(expected.abs() * solver.getRelativeAccuracy()));
        TestUtils.assertContains(result, expected, tolerance);
    }

// org.apache.commons.math.analysis.solvers.LaguerreSolverTest::testParameters
    public void testParameters() throws Exception {
        double coefficients[] = { -3.0, 5.0, 2.0 };
        PolynomialFunction f = new PolynomialFunction(coefficients);
        UnivariateRealSolver solver = new LaguerreSolver();

        try {
            
            solver.solve(f, 1, -1);
            fail("Expecting IllegalArgumentException - bad interval");
        } catch (IllegalArgumentException ex) {
            
        }
        try {
            
            solver.solve(f, 2, 3);
            fail("Expecting IllegalArgumentException - no bracketing");
        } catch (IllegalArgumentException ex) {
            
        }
        try {
            
            solver.solve(new SinFunction(), -1, 1);
            fail("Expecting IllegalArgumentException - bad function");
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.analysis.solvers.MullerSolverTest::testDeprecated
    public void testDeprecated() throws MathException {
        UnivariateRealFunction f = new SinFunction();
        UnivariateRealSolver solver = new MullerSolver(f);
        double min, max, expected, result, tolerance;

        min = 3.0; max = 4.0; expected = Math.PI;
        tolerance = Math.max(solver.getAbsoluteAccuracy(),
                    Math.abs(expected * solver.getRelativeAccuracy()));
        result = solver.solve(min, max);
        assertEquals(expected, result, tolerance);

        min = -1.0; max = 1.5; expected = 0.0;
        tolerance = Math.max(solver.getAbsoluteAccuracy(),
                    Math.abs(expected * solver.getRelativeAccuracy()));
        result = solver.solve(min, max);
        assertEquals(expected, result, tolerance);
    }

// org.apache.commons.math.analysis.solvers.MullerSolverTest::testDeprecated2
    public void testDeprecated2() throws MathException {
        UnivariateRealFunction f = new QuinticFunction();
        MullerSolver solver = new MullerSolver(f);
        double min, max, expected, result, tolerance;

        min = -0.4; max = 0.2; expected = 0.0;
        tolerance = Math.max(solver.getAbsoluteAccuracy(),
                    Math.abs(expected * solver.getRelativeAccuracy()));
        result = solver.solve2(min, max);
        assertEquals(expected, result, tolerance);

        min = 0.75; max = 1.5; expected = 1.0;
        tolerance = Math.max(solver.getAbsoluteAccuracy(),
                    Math.abs(expected * solver.getRelativeAccuracy()));
        result = solver.solve2(min, max);
        assertEquals(expected, result, tolerance);

        min = -0.9; max = -0.2; expected = -0.5;
        tolerance = Math.max(solver.getAbsoluteAccuracy(),
                    Math.abs(expected * solver.getRelativeAccuracy()));
        result = solver.solve2(min, max);
        assertEquals(expected, result, tolerance);
    }

// org.apache.commons.math.analysis.solvers.MullerSolverTest::testSinFunction
    public void testSinFunction() throws MathException {
        UnivariateRealFunction f = new SinFunction();
        UnivariateRealSolver solver = new MullerSolver();
        double min, max, expected, result, tolerance;

        min = 3.0; max = 4.0; expected = Math.PI;
        tolerance = Math.max(solver.getAbsoluteAccuracy(),
                    Math.abs(expected * solver.getRelativeAccuracy()));
        result = solver.solve(f, min, max);
        assertEquals(expected, result, tolerance);

        min = -1.0; max = 1.5; expected = 0.0;
        tolerance = Math.max(solver.getAbsoluteAccuracy(),
                    Math.abs(expected * solver.getRelativeAccuracy()));
        result = solver.solve(f, min, max);
        assertEquals(expected, result, tolerance);
    }

// org.apache.commons.math.analysis.solvers.MullerSolverTest::testSinFunction2
    public void testSinFunction2() throws MathException {
        UnivariateRealFunction f = new SinFunction();
        MullerSolver solver = new MullerSolver();
        double min, max, expected, result, tolerance;

        min = 3.0; max = 4.0; expected = Math.PI;
        tolerance = Math.max(solver.getAbsoluteAccuracy(),
                    Math.abs(expected * solver.getRelativeAccuracy()));
        result = solver.solve2(f, min, max);
        assertEquals(expected, result, tolerance);

        min = -1.0; max = 1.5; expected = 0.0;
        tolerance = Math.max(solver.getAbsoluteAccuracy(),
                    Math.abs(expected * solver.getRelativeAccuracy()));
        result = solver.solve2(f, min, max);
        assertEquals(expected, result, tolerance);
    }

// org.apache.commons.math.analysis.solvers.MullerSolverTest::testQuinticFunction
    public void testQuinticFunction() throws MathException {
        UnivariateRealFunction f = new QuinticFunction();
        UnivariateRealSolver solver = new MullerSolver();
        double min, max, expected, result, tolerance;

        min = -0.4; max = 0.2; expected = 0.0;
        tolerance = Math.max(solver.getAbsoluteAccuracy(),
                    Math.abs(expected * solver.getRelativeAccuracy()));
        result = solver.solve(f, min, max);
        assertEquals(expected, result, tolerance);

        min = 0.75; max = 1.5; expected = 1.0;
        tolerance = Math.max(solver.getAbsoluteAccuracy(),
                    Math.abs(expected * solver.getRelativeAccuracy()));
        result = solver.solve(f, min, max);
        assertEquals(expected, result, tolerance);

        min = -0.9; max = -0.2; expected = -0.5;
        tolerance = Math.max(solver.getAbsoluteAccuracy(),
                    Math.abs(expected * solver.getRelativeAccuracy()));
        result = solver.solve(f, min, max);
        assertEquals(expected, result, tolerance);
    }

// org.apache.commons.math.analysis.solvers.MullerSolverTest::testQuinticFunction2
    public void testQuinticFunction2() throws MathException {
        UnivariateRealFunction f = new QuinticFunction();
        MullerSolver solver = new MullerSolver();
        double min, max, expected, result, tolerance;

        min = -0.4; max = 0.2; expected = 0.0;
        tolerance = Math.max(solver.getAbsoluteAccuracy(),
                    Math.abs(expected * solver.getRelativeAccuracy()));
        result = solver.solve2(f, min, max);
        assertEquals(expected, result, tolerance);

        min = 0.75; max = 1.5; expected = 1.0;
        tolerance = Math.max(solver.getAbsoluteAccuracy(),
                    Math.abs(expected * solver.getRelativeAccuracy()));
        result = solver.solve2(f, min, max);
        assertEquals(expected, result, tolerance);

        min = -0.9; max = -0.2; expected = -0.5;
        tolerance = Math.max(solver.getAbsoluteAccuracy(),
                    Math.abs(expected * solver.getRelativeAccuracy()));
        result = solver.solve2(f, min, max);
        assertEquals(expected, result, tolerance);
    }

// org.apache.commons.math.analysis.solvers.MullerSolverTest::testExpm1Function
    public void testExpm1Function() throws MathException {
        UnivariateRealFunction f = new Expm1Function();
        UnivariateRealSolver solver = new MullerSolver();
        double min, max, expected, result, tolerance;

        min = -1.0; max = 2.0; expected = 0.0;
        tolerance = Math.max(solver.getAbsoluteAccuracy(),
                    Math.abs(expected * solver.getRelativeAccuracy()));
        result = solver.solve(f, min, max);
        assertEquals(expected, result, tolerance);

        min = -20.0; max = 10.0; expected = 0.0;
        tolerance = Math.max(solver.getAbsoluteAccuracy(),
                    Math.abs(expected * solver.getRelativeAccuracy()));
        result = solver.solve(f, min, max);
        assertEquals(expected, result, tolerance);

        min = -50.0; max = 100.0; expected = 0.0;
        tolerance = Math.max(solver.getAbsoluteAccuracy(),
                    Math.abs(expected * solver.getRelativeAccuracy()));
        result = solver.solve(f, min, max);
        assertEquals(expected, result, tolerance);
    }

// org.apache.commons.math.analysis.solvers.MullerSolverTest::testExpm1Function2
    public void testExpm1Function2() throws MathException {
        UnivariateRealFunction f = new Expm1Function();
        MullerSolver solver = new MullerSolver();
        double min, max, expected, result, tolerance;

        min = -1.0; max = 2.0; expected = 0.0;
        tolerance = Math.max(solver.getAbsoluteAccuracy(),
                    Math.abs(expected * solver.getRelativeAccuracy()));
        result = solver.solve2(f, min, max);
        assertEquals(expected, result, tolerance);

        min = -20.0; max = 10.0; expected = 0.0;
        tolerance = Math.max(solver.getAbsoluteAccuracy(),
                    Math.abs(expected * solver.getRelativeAccuracy()));
        result = solver.solve2(f, min, max);
        assertEquals(expected, result, tolerance);

        min = -50.0; max = 100.0; expected = 0.0;
        tolerance = Math.max(solver.getAbsoluteAccuracy(),
                    Math.abs(expected * solver.getRelativeAccuracy()));
        result = solver.solve2(f, min, max);
        assertEquals(expected, result, tolerance);
    }

// org.apache.commons.math.analysis.solvers.MullerSolverTest::testParameters
    public void testParameters() throws Exception {
        UnivariateRealFunction f = new SinFunction();
        UnivariateRealSolver solver = new MullerSolver();

        try {
            
            solver.solve(f, 1, -1);
            fail("Expecting IllegalArgumentException - bad interval");
        } catch (IllegalArgumentException ex) {
            
        }
        try {
            
            solver.solve(f, 2, 3);
            fail("Expecting IllegalArgumentException - no bracketing");
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.analysis.solvers.RiddersSolverTest::testDeprecated
    public void testDeprecated() throws MathException {
        UnivariateRealFunction f = new SinFunction();
        UnivariateRealSolver solver = new RiddersSolver(f);
        double min, max, expected, result, tolerance;

        min = 3.0; max = 4.0; expected = Math.PI;
        tolerance = Math.max(solver.getAbsoluteAccuracy(),
                    Math.abs(expected * solver.getRelativeAccuracy()));
        result = solver.solve(min, max);
        assertEquals(expected, result, tolerance);

        min = -1.0; max = 1.5; expected = 0.0;
        tolerance = Math.max(solver.getAbsoluteAccuracy(),
                    Math.abs(expected * solver.getRelativeAccuracy()));
        result = solver.solve(min, max);
        assertEquals(expected, result, tolerance);
    }

// org.apache.commons.math.analysis.solvers.RiddersSolverTest::testSinFunction
    public void testSinFunction() throws MathException {
        UnivariateRealFunction f = new SinFunction();
        UnivariateRealSolver solver = new RiddersSolver();
        double min, max, expected, result, tolerance;

        min = 3.0; max = 4.0; expected = Math.PI;
        tolerance = Math.max(solver.getAbsoluteAccuracy(),
                    Math.abs(expected * solver.getRelativeAccuracy()));
        result = solver.solve(f, min, max);
        assertEquals(expected, result, tolerance);

        min = -1.0; max = 1.5; expected = 0.0;
        tolerance = Math.max(solver.getAbsoluteAccuracy(),
                    Math.abs(expected * solver.getRelativeAccuracy()));
        result = solver.solve(f, min, max);
        assertEquals(expected, result, tolerance);
    }

// org.apache.commons.math.analysis.solvers.RiddersSolverTest::testQuinticFunction
    public void testQuinticFunction() throws MathException {
        UnivariateRealFunction f = new QuinticFunction();
        UnivariateRealSolver solver = new RiddersSolver();
        double min, max, expected, result, tolerance;

        min = -0.4; max = 0.2; expected = 0.0;
        tolerance = Math.max(solver.getAbsoluteAccuracy(),
                    Math.abs(expected * solver.getRelativeAccuracy()));
        result = solver.solve(f, min, max);
        assertEquals(expected, result, tolerance);

        min = 0.75; max = 1.5; expected = 1.0;
        tolerance = Math.max(solver.getAbsoluteAccuracy(),
                    Math.abs(expected * solver.getRelativeAccuracy()));
        result = solver.solve(f, min, max);
        assertEquals(expected, result, tolerance);

        min = -0.9; max = -0.2; expected = -0.5;
        tolerance = Math.max(solver.getAbsoluteAccuracy(),
                    Math.abs(expected * solver.getRelativeAccuracy()));
        result = solver.solve(f, min, max);
        assertEquals(expected, result, tolerance);
    }

// org.apache.commons.math.analysis.solvers.RiddersSolverTest::testExpm1Function
    public void testExpm1Function() throws MathException {
        UnivariateRealFunction f = new Expm1Function();
        UnivariateRealSolver solver = new RiddersSolver();
        double min, max, expected, result, tolerance;

        min = -1.0; max = 2.0; expected = 0.0;
        tolerance = Math.max(solver.getAbsoluteAccuracy(),
                    Math.abs(expected * solver.getRelativeAccuracy()));
        result = solver.solve(f, min, max);
        assertEquals(expected, result, tolerance);

        min = -20.0; max = 10.0; expected = 0.0;
        tolerance = Math.max(solver.getAbsoluteAccuracy(),
                    Math.abs(expected * solver.getRelativeAccuracy()));
        result = solver.solve(f, min, max);
        assertEquals(expected, result, tolerance);

        min = -50.0; max = 100.0; expected = 0.0;
        tolerance = Math.max(solver.getAbsoluteAccuracy(),
                    Math.abs(expected * solver.getRelativeAccuracy()));
        result = solver.solve(f, min, max);
        assertEquals(expected, result, tolerance);
    }

// org.apache.commons.math.analysis.solvers.RiddersSolverTest::testParameters
    public void testParameters() throws Exception {
        UnivariateRealFunction f = new SinFunction();
        UnivariateRealSolver solver = new RiddersSolver();

        try {
            
            solver.solve(f, 1, -1);
            fail("Expecting IllegalArgumentException - bad interval");
        } catch (IllegalArgumentException ex) {
            
        }
        try {
            
            solver.solve(f, 2, 3);
            fail("Expecting IllegalArgumentException - no bracketing");
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.complex.ComplexTest::testConstructor
    public void testConstructor() {
        Complex z = new Complex(3.0, 4.0);
        assertEquals(3.0, z.getReal(), 1.0e-5);
        assertEquals(4.0, z.getImaginary(), 1.0e-5);
    }

// org.apache.commons.math.complex.ComplexTest::testConstructorNaN
    public void testConstructorNaN() {
        Complex z = new Complex(3.0, Double.NaN);
        assertTrue(z.isNaN());

        z = new Complex(nan, 4.0);
        assertTrue(z.isNaN());

        z = new Complex(3.0, 4.0);
        assertFalse(z.isNaN());
    }

// org.apache.commons.math.complex.ComplexTest::testAbs
    public void testAbs() {
        Complex z = new Complex(3.0, 4.0);
        assertEquals(5.0, z.abs(), 1.0e-5);
    }

// org.apache.commons.math.complex.ComplexTest::testAbsNaN
    public void testAbsNaN() {
        assertTrue(Double.isNaN(Complex.NaN.abs()));
        Complex z = new Complex(inf, nan);
        assertTrue(Double.isNaN(z.abs()));
    }

// org.apache.commons.math.complex.ComplexTest::testAbsInfinite
    public void testAbsInfinite() {
        Complex z = new Complex(inf, 0);
        assertEquals(inf, z.abs(), 0);
        z = new Complex(0, neginf);
        assertEquals(inf, z.abs(), 0);
        z = new Complex(inf, neginf);
        assertEquals(inf, z.abs(), 0);
    }

// org.apache.commons.math.complex.ComplexTest::testAdd
    public void testAdd() {
        Complex x = new Complex(3.0, 4.0);
        Complex y = new Complex(5.0, 6.0);
        Complex z = x.add(y);
        assertEquals(8.0, z.getReal(), 1.0e-5);
        assertEquals(10.0, z.getImaginary(), 1.0e-5);
    }

// org.apache.commons.math.complex.ComplexTest::testAddNaN
    public void testAddNaN() {
        Complex x = new Complex(3.0, 4.0);
        Complex z = x.add(Complex.NaN);
        assertTrue(z.isNaN());
        z = new Complex(1, nan);
        Complex w = x.add(z);
        assertEquals(w.getReal(), 4.0, 0);
        assertTrue(Double.isNaN(w.getImaginary()));
    }

// org.apache.commons.math.complex.ComplexTest::testAddInfinite
    public void testAddInfinite() {
        Complex x = new Complex(1, 1);
        Complex z = new Complex(inf, 0);
        Complex w = x.add(z);
        assertEquals(w.getImaginary(), 1, 0);
        assertEquals(inf, w.getReal(), 0);

        x = new Complex(neginf, 0);
        assertTrue(Double.isNaN(x.add(z).getReal()));
    }

// org.apache.commons.math.complex.ComplexTest::testConjugate
    public void testConjugate() {
        Complex x = new Complex(3.0, 4.0);
        Complex z = x.conjugate();
        assertEquals(3.0, z.getReal(), 1.0e-5);
        assertEquals(-4.0, z.getImaginary(), 1.0e-5);
    }

// org.apache.commons.math.complex.ComplexTest::testConjugateNaN
    public void testConjugateNaN() {
        Complex z = Complex.NaN.conjugate();
        assertTrue(z.isNaN());
    }

// org.apache.commons.math.complex.ComplexTest::testConjugateInfiinite
    public void testConjugateInfiinite() {
        Complex z = new Complex(0, inf);
        assertEquals(neginf, z.conjugate().getImaginary(), 0);
        z = new Complex(0, neginf);
        assertEquals(inf, z.conjugate().getImaginary(), 0);
    }

// org.apache.commons.math.complex.ComplexTest::testDivide
    public void testDivide() {
        Complex x = new Complex(3.0, 4.0);
        Complex y = new Complex(5.0, 6.0);
        Complex z = x.divide(y);
        assertEquals(39.0 / 61.0, z.getReal(), 1.0e-5);
        assertEquals(2.0 / 61.0, z.getImaginary(), 1.0e-5);
    }

// org.apache.commons.math.complex.ComplexTest::testDivideReal
    public void testDivideReal() {
        Complex x = new Complex(2d, 3d);
        Complex y = new Complex(2d, 0d);
        assertEquals(new Complex(1d, 1.5), x.divide(y));
        
    }

// org.apache.commons.math.complex.ComplexTest::testDivideImaginary
    public void testDivideImaginary() {
        Complex x = new Complex(2d, 3d);
        Complex y = new Complex(0d, 2d);
        assertEquals(new Complex(1.5d, -1d), x.divide(y));
    }

// org.apache.commons.math.complex.ComplexTest::testDivideInfinite
    public void testDivideInfinite() {
        Complex x = new Complex(3, 4);
        Complex w = new Complex(neginf, inf);
        assertTrue(x.divide(w).equals(Complex.ZERO));

        Complex z = w.divide(x);
        assertTrue(Double.isNaN(z.getReal()));
        assertEquals(inf, z.getImaginary(), 0);

        w = new Complex(inf, inf);
        z = w.divide(x);
        assertTrue(Double.isNaN(z.getImaginary()));
        assertEquals(inf, z.getReal(), 0);

        w = new Complex(1, inf);
        z = w.divide(w);
        assertTrue(Double.isNaN(z.getReal()));
        assertTrue(Double.isNaN(z.getImaginary()));
    }

// org.apache.commons.math.complex.ComplexTest::testDivideZero
    public void testDivideZero() {
        Complex x = new Complex(3.0, 4.0);
        Complex z = x.divide(Complex.ZERO);
        assertEquals(z, Complex.NaN);
    }

// org.apache.commons.math.complex.ComplexTest::testDivideNaN
    public void testDivideNaN() {
        Complex x = new Complex(3.0, 4.0);
        Complex z = x.divide(Complex.NaN);
        assertTrue(z.isNaN());
    }

// org.apache.commons.math.complex.ComplexTest::testDivideNaNInf
    public void testDivideNaNInf() {
       Complex z = oneInf.divide(Complex.ONE);
       assertTrue(Double.isNaN(z.getReal()));
       assertEquals(inf, z.getImaginary(), 0);

       z = negInfNegInf.divide(oneNaN);
       assertTrue(Double.isNaN(z.getReal()));
       assertTrue(Double.isNaN(z.getImaginary()));

       z = negInfInf.divide(Complex.ONE);
       assertTrue(Double.isNaN(z.getReal()));
       assertTrue(Double.isNaN(z.getImaginary()));
    }

// org.apache.commons.math.complex.ComplexTest::testMultiply
    public void testMultiply() {
        Complex x = new Complex(3.0, 4.0);
        Complex y = new Complex(5.0, 6.0);
        Complex z = x.multiply(y);
        assertEquals(-9.0, z.getReal(), 1.0e-5);
        assertEquals(38.0, z.getImaginary(), 1.0e-5);
    }

// org.apache.commons.math.complex.ComplexTest::testMultiplyNaN
    public void testMultiplyNaN() {
        Complex x = new Complex(3.0, 4.0);
        Complex z = x.multiply(Complex.NaN);
        assertTrue(z.isNaN());
    }

// org.apache.commons.math.complex.ComplexTest::testMultiplyNaNInf
    public void testMultiplyNaNInf() {
        Complex z = new Complex(1,1);
        Complex w = z.multiply(infOne);
        assertEquals(w.getReal(), inf, 0);
        assertEquals(w.getImaginary(), inf, 0);

        
        assertTrue(new Complex( 1,0).multiply(infInf).equals(Complex.INF));
        assertTrue(new Complex(-1,0).multiply(infInf).equals(Complex.INF));
        assertTrue(new Complex( 1,0).multiply(negInfZero).equals(Complex.INF));

        w = oneInf.multiply(oneNegInf);
        assertEquals(w.getReal(), inf, 0);
        assertEquals(w.getImaginary(), inf, 0);

        w = negInfNegInf.multiply(oneNaN);
        assertTrue(Double.isNaN(w.getReal()));
        assertTrue(Double.isNaN(w.getImaginary()));
    }

// org.apache.commons.math.complex.ComplexTest::testScalarMultiply
    public void testScalarMultiply() {
        Complex x = new Complex(3.0, 4.0);
        double y = 2.0;
        Complex z = x.multiply(y);
        assertEquals(6.0, z.getReal(), 1.0e-5);
        assertEquals(8.0, z.getImaginary(), 1.0e-5);
    }

// org.apache.commons.math.complex.ComplexTest::testScalarMultiplyNaN
    public void testScalarMultiplyNaN() {
        Complex x = new Complex(3.0, 4.0);
        Complex z = x.multiply(Double.NaN);
        assertTrue(z.isNaN());
    }

// org.apache.commons.math.complex.ComplexTest::testScalarMultiplyInf
    public void testScalarMultiplyInf() {
        Complex z = new Complex(1,1);
        Complex w = z.multiply(Double.POSITIVE_INFINITY);
        assertEquals(w.getReal(), inf, 0);
        assertEquals(w.getImaginary(), inf, 0);

        w = z.multiply(Double.NEGATIVE_INFINITY);
        assertEquals(w.getReal(), inf, 0);
        assertEquals(w.getImaginary(), inf, 0);
    }

// org.apache.commons.math.complex.ComplexTest::testNegate
    public void testNegate() {
        Complex x = new Complex(3.0, 4.0);
        Complex z = x.negate();
        assertEquals(-3.0, z.getReal(), 1.0e-5);
        assertEquals(-4.0, z.getImaginary(), 1.0e-5);
    }

// org.apache.commons.math.complex.ComplexTest::testNegateNaN
    public void testNegateNaN() {
        Complex z = Complex.NaN.negate();
        assertTrue(z.isNaN());
    }

// org.apache.commons.math.complex.ComplexTest::testSubtract
    public void testSubtract() {
        Complex x = new Complex(3.0, 4.0);
        Complex y = new Complex(5.0, 6.0);
        Complex z = x.subtract(y);
        assertEquals(-2.0, z.getReal(), 1.0e-5);
        assertEquals(-2.0, z.getImaginary(), 1.0e-5);
    }

// org.apache.commons.math.complex.ComplexTest::testSubtractNaN
    public void testSubtractNaN() {
        Complex x = new Complex(3.0, 4.0);
        Complex z = x.subtract(Complex.NaN);
        assertTrue(z.isNaN());
    }

// org.apache.commons.math.complex.ComplexTest::testEqualsNull
    public void testEqualsNull() {
        Complex x = new Complex(3.0, 4.0);
        assertFalse(x.equals(null));
    }

// org.apache.commons.math.complex.ComplexTest::testEqualsClass
    public void testEqualsClass() {
        Complex x = new Complex(3.0, 4.0);
        assertFalse(x.equals(this));
    }

// org.apache.commons.math.complex.ComplexTest::testEqualsSame
    public void testEqualsSame() {
        Complex x = new Complex(3.0, 4.0);
        assertTrue(x.equals(x));
    }

// org.apache.commons.math.complex.ComplexTest::testEqualsTrue
    public void testEqualsTrue() {
        Complex x = new Complex(3.0, 4.0);
        Complex y = new Complex(3.0, 4.0);
        assertTrue(x.equals(y));
    }

// org.apache.commons.math.complex.ComplexTest::testEqualsRealDifference
    public void testEqualsRealDifference() {
        Complex x = new Complex(0.0, 0.0);
        Complex y = new Complex(0.0 + Double.MIN_VALUE, 0.0);
        assertFalse(x.equals(y));
    }

// org.apache.commons.math.complex.ComplexTest::testEqualsImaginaryDifference
    public void testEqualsImaginaryDifference() {
        Complex x = new Complex(0.0, 0.0);
        Complex y = new Complex(0.0, 0.0 + Double.MIN_VALUE);
        assertFalse(x.equals(y));
    }

// org.apache.commons.math.complex.ComplexTest::testEqualsNaN
    public void testEqualsNaN() {
        Complex realNaN = new Complex(Double.NaN, 0.0);
        Complex imaginaryNaN = new Complex(0.0, Double.NaN);
        Complex complexNaN = Complex.NaN;
        assertTrue(realNaN.equals(imaginaryNaN));
        assertTrue(imaginaryNaN.equals(complexNaN));
        assertTrue(realNaN.equals(complexNaN));
    }

// org.apache.commons.math.complex.ComplexTest::testHashCode
    public void testHashCode() {
        Complex x = new Complex(0.0, 0.0);
        Complex y = new Complex(0.0, 0.0 + Double.MIN_VALUE);
        assertFalse(x.hashCode()==y.hashCode());
        y = new Complex(0.0 + Double.MIN_VALUE, 0.0);
        assertFalse(x.hashCode()==y.hashCode());
        Complex realNaN = new Complex(Double.NaN, 0.0);
        Complex imaginaryNaN = new Complex(0.0, Double.NaN);
        assertEquals(realNaN.hashCode(), imaginaryNaN.hashCode());
        assertEquals(imaginaryNaN.hashCode(), Complex.NaN.hashCode());
    }

// org.apache.commons.math.complex.ComplexTest::testAcos
    public void testAcos() {
        Complex z = new Complex(3, 4);
        Complex expected = new Complex(0.936812, -2.30551);
        TestUtils.assertEquals(expected, z.acos(), 1.0e-5);
        TestUtils.assertEquals(new Complex(Math.acos(0), 0),
                Complex.ZERO.acos(), 1.0e-12);
    }

// org.apache.commons.math.complex.ComplexTest::testAcosInf
    public void testAcosInf() {
        TestUtils.assertSame(Complex.NaN, oneInf.acos());
        TestUtils.assertSame(Complex.NaN, oneNegInf.acos());
        TestUtils.assertSame(Complex.NaN, infOne.acos());
        TestUtils.assertSame(Complex.NaN, negInfOne.acos());
        TestUtils.assertSame(Complex.NaN, infInf.acos());
        TestUtils.assertSame(Complex.NaN, infNegInf.acos());
        TestUtils.assertSame(Complex.NaN, negInfInf.acos());
        TestUtils.assertSame(Complex.NaN, negInfNegInf.acos());
    }

// org.apache.commons.math.complex.ComplexTest::testAcosNaN
    public void testAcosNaN() {
        assertTrue(Complex.NaN.acos().isNaN());
    }

// org.apache.commons.math.complex.ComplexTest::testAsin
    public void testAsin() {
        Complex z = new Complex(3, 4);
        Complex expected = new Complex(0.633984, 2.30551);
        TestUtils.assertEquals(expected, z.asin(), 1.0e-5);
    }

// org.apache.commons.math.complex.ComplexTest::testAsinNaN
    public void testAsinNaN() {
        assertTrue(Complex.NaN.asin().isNaN());
    }

// org.apache.commons.math.complex.ComplexTest::testAsinInf
    public void testAsinInf() {
        TestUtils.assertSame(Complex.NaN, oneInf.asin());
        TestUtils.assertSame(Complex.NaN, oneNegInf.asin());
        TestUtils.assertSame(Complex.NaN, infOne.asin());
        TestUtils.assertSame(Complex.NaN, negInfOne.asin());
        TestUtils.assertSame(Complex.NaN, infInf.asin());
        TestUtils.assertSame(Complex.NaN, infNegInf.asin());
        TestUtils.assertSame(Complex.NaN, negInfInf.asin());
        TestUtils.assertSame(Complex.NaN, negInfNegInf.asin());
    }

// org.apache.commons.math.complex.ComplexTest::testAtan
    public void testAtan() {
        Complex z = new Complex(3, 4);
        Complex expected = new Complex(1.44831, 0.158997);
        TestUtils.assertEquals(expected, z.atan(), 1.0e-5);
    }

// org.apache.commons.math.complex.ComplexTest::testAtanInf
    public void testAtanInf() {
        TestUtils.assertSame(Complex.NaN, oneInf.atan());
        TestUtils.assertSame(Complex.NaN, oneNegInf.atan());
        TestUtils.assertSame(Complex.NaN, infOne.atan());
        TestUtils.assertSame(Complex.NaN, negInfOne.atan());
        TestUtils.assertSame(Complex.NaN, infInf.atan());
        TestUtils.assertSame(Complex.NaN, infNegInf.atan());
        TestUtils.assertSame(Complex.NaN, negInfInf.atan());
        TestUtils.assertSame(Complex.NaN, negInfNegInf.atan());
    }

// org.apache.commons.math.complex.ComplexTest::testAtanNaN
    public void testAtanNaN() {
        assertTrue(Complex.NaN.atan().isNaN());
        assertTrue(Complex.I.atan().isNaN());
    }

// org.apache.commons.math.complex.ComplexTest::testCos
    public void testCos() {
        Complex z = new Complex(3, 4);
        Complex expected = new Complex(-27.03495, -3.851153);
        TestUtils.assertEquals(expected, z.cos(), 1.0e-5);
    }

// org.apache.commons.math.complex.ComplexTest::testCosNaN
    public void testCosNaN() {
        assertTrue(Complex.NaN.cos().isNaN());
    }

// org.apache.commons.math.complex.ComplexTest::testCosInf
    public void testCosInf() {
        TestUtils.assertSame(infNegInf, oneInf.cos());
        TestUtils.assertSame(infInf, oneNegInf.cos());
        TestUtils.assertSame(Complex.NaN, infOne.cos());
        TestUtils.assertSame(Complex.NaN, negInfOne.cos());
        TestUtils.assertSame(Complex.NaN, infInf.cos());
        TestUtils.assertSame(Complex.NaN, infNegInf.cos());
        TestUtils.assertSame(Complex.NaN, negInfInf.cos());
        TestUtils.assertSame(Complex.NaN, negInfNegInf.cos());
    }

// org.apache.commons.math.complex.ComplexTest::testCosh
    public void testCosh() {
        Complex z = new Complex(3, 4);
        Complex expected = new Complex(-6.58066, -7.58155);
        TestUtils.assertEquals(expected, z.cosh(), 1.0e-5);
    }

// org.apache.commons.math.complex.ComplexTest::testCoshNaN
    public void testCoshNaN() {
        assertTrue(Complex.NaN.cosh().isNaN());
    }

// org.apache.commons.math.complex.ComplexTest::testCoshInf
    public void testCoshInf() {
        TestUtils.assertSame(Complex.NaN, oneInf.cosh());
        TestUtils.assertSame(Complex.NaN, oneNegInf.cosh());
        TestUtils.assertSame(infInf, infOne.cosh());
        TestUtils.assertSame(infNegInf, negInfOne.cosh());
        TestUtils.assertSame(Complex.NaN, infInf.cosh());
        TestUtils.assertSame(Complex.NaN, infNegInf.cosh());
        TestUtils.assertSame(Complex.NaN, negInfInf.cosh());
        TestUtils.assertSame(Complex.NaN, negInfNegInf.cosh());
    }

// org.apache.commons.math.complex.ComplexTest::testExp
    public void testExp() {
        Complex z = new Complex(3, 4);
        Complex expected = new Complex(-13.12878, -15.20078);
        TestUtils.assertEquals(expected, z.exp(), 1.0e-5);
        TestUtils.assertEquals(Complex.ONE,
                Complex.ZERO.exp(), 10e-12);
        Complex iPi = Complex.I.multiply(new Complex(pi,0));
        TestUtils.assertEquals(Complex.ONE.negate(),
                iPi.exp(), 10e-12);
    }

// org.apache.commons.math.complex.ComplexTest::testExpNaN
    public void testExpNaN() {
        assertTrue(Complex.NaN.exp().isNaN());
    }

// org.apache.commons.math.complex.ComplexTest::testExpInf
    public void testExpInf() {
        TestUtils.assertSame(Complex.NaN, oneInf.exp());
        TestUtils.assertSame(Complex.NaN, oneNegInf.exp());
        TestUtils.assertSame(infInf, infOne.exp());
        TestUtils.assertSame(Complex.ZERO, negInfOne.exp());
        TestUtils.assertSame(Complex.NaN, infInf.exp());
        TestUtils.assertSame(Complex.NaN, infNegInf.exp());
        TestUtils.assertSame(Complex.NaN, negInfInf.exp());
        TestUtils.assertSame(Complex.NaN, negInfNegInf.exp());
    }

// org.apache.commons.math.complex.ComplexTest::testLog
    public void testLog() {
        Complex z = new Complex(3, 4);
        Complex expected = new Complex(1.60944, 0.927295);
        TestUtils.assertEquals(expected, z.log(), 1.0e-5);
    }

// org.apache.commons.math.complex.ComplexTest::testLogNaN
    public void testLogNaN() {
        assertTrue(Complex.NaN.log().isNaN());
    }

// org.apache.commons.math.complex.ComplexTest::testLogInf
    public void testLogInf() {
        TestUtils.assertEquals(new Complex(inf, pi / 2),
                oneInf.log(), 10e-12);
        TestUtils.assertEquals(new Complex(inf, -pi / 2),
                oneNegInf.log(), 10e-12);
        TestUtils.assertEquals(infZero, infOne.log(), 10e-12);
        TestUtils.assertEquals(new Complex(inf, pi),
                negInfOne.log(), 10e-12);
        TestUtils.assertEquals(new Complex(inf, pi / 4),
                infInf.log(), 10e-12);
        TestUtils.assertEquals(new Complex(inf, -pi / 4),
                infNegInf.log(), 10e-12);
        TestUtils.assertEquals(new Complex(inf, 3d * pi / 4),
                negInfInf.log(), 10e-12);
        TestUtils.assertEquals(new Complex(inf, - 3d * pi / 4),
                negInfNegInf.log(), 10e-12);
    }

// org.apache.commons.math.complex.ComplexTest::testLogZero
    public void testLogZero() {
        TestUtils.assertSame(negInfZero, Complex.ZERO.log());
    }

// org.apache.commons.math.complex.ComplexTest::testPow
    public void testPow() {
        Complex x = new Complex(3, 4);
        Complex y = new Complex(5, 6);
        Complex expected = new Complex(-1.860893, 11.83677);
        TestUtils.assertEquals(expected, x.pow(y), 1.0e-5);
    }

// org.apache.commons.math.complex.ComplexTest::testPowNaNBase
    public void testPowNaNBase() {
        Complex x = new Complex(3, 4);
        assertTrue(Complex.NaN.pow(x).isNaN());
    }

// org.apache.commons.math.complex.ComplexTest::testPowNaNExponent
    public void testPowNaNExponent() {
        Complex x = new Complex(3, 4);
        assertTrue(x.pow(Complex.NaN).isNaN());
    }

// org.apache.commons.math.complex.ComplexTest::testPowInf
   public void testPowInf() {
       TestUtils.assertSame(Complex.NaN,Complex.ONE.pow(oneInf));
       TestUtils.assertSame(Complex.NaN,Complex.ONE.pow(oneNegInf));
       TestUtils.assertSame(Complex.NaN,Complex.ONE.pow(infOne));
       TestUtils.assertSame(Complex.NaN,Complex.ONE.pow(infInf));
       TestUtils.assertSame(Complex.NaN,Complex.ONE.pow(infNegInf));
       TestUtils.assertSame(Complex.NaN,Complex.ONE.pow(negInfInf));
       TestUtils.assertSame(Complex.NaN,Complex.ONE.pow(negInfNegInf));
       TestUtils.assertSame(Complex.NaN,infOne.pow(Complex.ONE));
       TestUtils.assertSame(Complex.NaN,negInfOne.pow(Complex.ONE));
       TestUtils.assertSame(Complex.NaN,infInf.pow(Complex.ONE));
       TestUtils.assertSame(Complex.NaN,infNegInf.pow(Complex.ONE));
       TestUtils.assertSame(Complex.NaN,negInfInf.pow(Complex.ONE));
       TestUtils.assertSame(Complex.NaN,negInfNegInf.pow(Complex.ONE));
       TestUtils.assertSame(Complex.NaN,negInfNegInf.pow(infNegInf));
       TestUtils.assertSame(Complex.NaN,negInfNegInf.pow(negInfNegInf));
       TestUtils.assertSame(Complex.NaN,negInfNegInf.pow(infInf));
       TestUtils.assertSame(Complex.NaN,infInf.pow(infNegInf));
       TestUtils.assertSame(Complex.NaN,infInf.pow(negInfNegInf));
       TestUtils.assertSame(Complex.NaN,infInf.pow(infInf));
       TestUtils.assertSame(Complex.NaN,infNegInf.pow(infNegInf));
       TestUtils.assertSame(Complex.NaN,infNegInf.pow(negInfNegInf));
       TestUtils.assertSame(Complex.NaN,infNegInf.pow(infInf));
   }

// org.apache.commons.math.complex.ComplexTest::testPowZero
   public void testPowZero() {
       TestUtils.assertSame(Complex.NaN,
               Complex.ZERO.pow(Complex.ONE));
       TestUtils.assertSame(Complex.NaN,
               Complex.ZERO.pow(Complex.ZERO));
       TestUtils.assertSame(Complex.NaN,
               Complex.ZERO.pow(Complex.I));
       TestUtils.assertEquals(Complex.ONE,
               Complex.ONE.pow(Complex.ZERO), 10e-12);
       TestUtils.assertEquals(Complex.ONE,
               Complex.I.pow(Complex.ZERO), 10e-12);
       TestUtils.assertEquals(Complex.ONE,
               new Complex(-1, 3).pow(Complex.ZERO), 10e-12);
   }

// org.apache.commons.math.complex.ComplexTest::testpowNull
    public void testpowNull() {
        try {
            Complex.ONE.pow(null);
            fail("Expecting NullPointerException");
        } catch (NullPointerException ex) {
            
        }
    }

// org.apache.commons.math.complex.ComplexTest::testSin
    public void testSin() {
        Complex z = new Complex(3, 4);
        Complex expected = new Complex(3.853738, -27.01681);
        TestUtils.assertEquals(expected, z.sin(), 1.0e-5);
    }

// org.apache.commons.math.complex.ComplexTest::testSinInf
    public void testSinInf() {
        TestUtils.assertSame(infInf, oneInf.sin());
        TestUtils.assertSame(infNegInf, oneNegInf.sin());
        TestUtils.assertSame(Complex.NaN, infOne.sin());
        TestUtils.assertSame(Complex.NaN, negInfOne.sin());
        TestUtils.assertSame(Complex.NaN, infInf.sin());
        TestUtils.assertSame(Complex.NaN, infNegInf.sin());
        TestUtils.assertSame(Complex.NaN, negInfInf.sin());
        TestUtils.assertSame(Complex.NaN, negInfNegInf.sin());
    }

// org.apache.commons.math.complex.ComplexTest::testSinNaN
    public void testSinNaN() {
        assertTrue(Complex.NaN.sin().isNaN());
    }

// org.apache.commons.math.complex.ComplexTest::testSinh
    public void testSinh() {
        Complex z = new Complex(3, 4);
        Complex expected = new Complex(-6.54812, -7.61923);
        TestUtils.assertEquals(expected, z.sinh(), 1.0e-5);
    }

// org.apache.commons.math.complex.ComplexTest::testSinhNaN
    public void testSinhNaN() {
        assertTrue(Complex.NaN.sinh().isNaN());
    }

// org.apache.commons.math.complex.ComplexTest::testSinhInf
    public void testSinhInf() {
        TestUtils.assertSame(Complex.NaN, oneInf.sinh());
        TestUtils.assertSame(Complex.NaN, oneNegInf.sinh());
        TestUtils.assertSame(infInf, infOne.sinh());
        TestUtils.assertSame(negInfInf, negInfOne.sinh());
        TestUtils.assertSame(Complex.NaN, infInf.sinh());
        TestUtils.assertSame(Complex.NaN, infNegInf.sinh());
        TestUtils.assertSame(Complex.NaN, negInfInf.sinh());
        TestUtils.assertSame(Complex.NaN, negInfNegInf.sinh());
    }

// org.apache.commons.math.complex.ComplexTest::testSqrtRealPositive
    public void testSqrtRealPositive() {
        Complex z = new Complex(3, 4);
        Complex expected = new Complex(2, 1);
        TestUtils.assertEquals(expected, z.sqrt(), 1.0e-5);
    }

// org.apache.commons.math.complex.ComplexTest::testSqrtRealZero
    public void testSqrtRealZero() {
        Complex z = new Complex(0.0, 4);
        Complex expected = new Complex(1.41421, 1.41421);
        TestUtils.assertEquals(expected, z.sqrt(), 1.0e-5);
    }

// org.apache.commons.math.complex.ComplexTest::testSqrtRealNegative
    public void testSqrtRealNegative() {
        Complex z = new Complex(-3.0, 4);
        Complex expected = new Complex(1, 2);
        TestUtils.assertEquals(expected, z.sqrt(), 1.0e-5);
    }

// org.apache.commons.math.complex.ComplexTest::testSqrtImaginaryZero
    public void testSqrtImaginaryZero() {
        Complex z = new Complex(-3.0, 0.0);
        Complex expected = new Complex(0.0, 1.73205);
        TestUtils.assertEquals(expected, z.sqrt(), 1.0e-5);
    }

// org.apache.commons.math.complex.ComplexTest::testSqrtImaginaryNegative
    public void testSqrtImaginaryNegative() {
        Complex z = new Complex(-3.0, -4.0);
        Complex expected = new Complex(1.0, -2.0);
        TestUtils.assertEquals(expected, z.sqrt(), 1.0e-5);
    }

// org.apache.commons.math.complex.ComplexTest::testSqrtPolar
    public void testSqrtPolar() {
        double r = 1;
        for (int i = 0; i < 5; i++) {
            r += i;
            double theta = 0;
            for (int j =0; j < 11; j++) {
                theta += pi /12;
                Complex z = ComplexUtils.polar2Complex(r, theta);
                Complex sqrtz = ComplexUtils.polar2Complex(Math.sqrt(r), theta / 2);
                TestUtils.assertEquals(sqrtz, z.sqrt(), 10e-12);
            }
        }
    }

// org.apache.commons.math.complex.ComplexTest::testSqrtNaN
    public void testSqrtNaN() {
        assertTrue(Complex.NaN.sqrt().isNaN());
    }

// org.apache.commons.math.complex.ComplexTest::testSqrtInf
    public void testSqrtInf() {
        TestUtils.assertSame(infNaN, oneInf.sqrt());
        TestUtils.assertSame(infNaN, oneNegInf.sqrt());
        TestUtils.assertSame(infZero, infOne.sqrt());
        TestUtils.assertSame(zeroInf, negInfOne.sqrt());
        TestUtils.assertSame(infNaN, infInf.sqrt());
        TestUtils.assertSame(infNaN, infNegInf.sqrt());
        TestUtils.assertSame(nanInf, negInfInf.sqrt());
        TestUtils.assertSame(nanNegInf, negInfNegInf.sqrt());
    }

// org.apache.commons.math.complex.ComplexTest::testSqrt1z
    public void testSqrt1z() {
        Complex z = new Complex(3, 4);
        Complex expected = new Complex(4.08033, -2.94094);
        TestUtils.assertEquals(expected, z.sqrt1z(), 1.0e-5);
    }

// org.apache.commons.math.complex.ComplexTest::testSqrt1zNaN
    public void testSqrt1zNaN() {
        assertTrue(Complex.NaN.sqrt1z().isNaN());
    }

// org.apache.commons.math.complex.ComplexTest::testTan
    public void testTan() {
        Complex z = new Complex(3, 4);
        Complex expected = new Complex(-0.000187346, 0.999356);
        TestUtils.assertEquals(expected, z.tan(), 1.0e-5);
    }

// org.apache.commons.math.complex.ComplexTest::testTanNaN
    public void testTanNaN() {
        assertTrue(Complex.NaN.tan().isNaN());
    }

// org.apache.commons.math.complex.ComplexTest::testTanInf
    public void testTanInf() {
        TestUtils.assertSame(zeroNaN, oneInf.tan());
        TestUtils.assertSame(zeroNaN, oneNegInf.tan());
        TestUtils.assertSame(Complex.NaN, infOne.tan());
        TestUtils.assertSame(Complex.NaN, negInfOne.tan());
        TestUtils.assertSame(Complex.NaN, infInf.tan());
        TestUtils.assertSame(Complex.NaN, infNegInf.tan());
        TestUtils.assertSame(Complex.NaN, negInfInf.tan());
        TestUtils.assertSame(Complex.NaN, negInfNegInf.tan());
    }

// org.apache.commons.math.complex.ComplexTest::testTanCritical
   public void testTanCritical() {
        TestUtils.assertSame(infNaN, new Complex(pi/2, 0).tan());
        TestUtils.assertSame(negInfNaN, new Complex(-pi/2, 0).tan());
    }

// org.apache.commons.math.complex.ComplexTest::testTanh
    public void testTanh() {
        Complex z = new Complex(3, 4);
        Complex expected = new Complex(1.00071, 0.00490826);
        TestUtils.assertEquals(expected, z.tanh(), 1.0e-5);
    }

// org.apache.commons.math.complex.ComplexTest::testTanhNaN
    public void testTanhNaN() {
        assertTrue(Complex.NaN.tanh().isNaN());
    }

// org.apache.commons.math.complex.ComplexTest::testTanhInf
    public void testTanhInf() {
        TestUtils.assertSame(Complex.NaN, oneInf.tanh());
        TestUtils.assertSame(Complex.NaN, oneNegInf.tanh());
        TestUtils.assertSame(nanZero, infOne.tanh());
        TestUtils.assertSame(nanZero, negInfOne.tanh());
        TestUtils.assertSame(Complex.NaN, infInf.tanh());
        TestUtils.assertSame(Complex.NaN, infNegInf.tanh());
        TestUtils.assertSame(Complex.NaN, negInfInf.tanh());
        TestUtils.assertSame(Complex.NaN, negInfNegInf.tanh());
    }

// org.apache.commons.math.complex.ComplexTest::testTanhCritical
    public void testTanhCritical() {
        TestUtils.assertSame(nanInf, new Complex(0, pi/2).tanh());
    }

// org.apache.commons.math.complex.ComplexTest::testMath221
    public void testMath221() {
        assertEquals(new Complex(0,-1), new Complex(0,1).multiply(new Complex(-1,0)));
    }

// org.apache.commons.math.complex.ComplexTest::testNthRoot_normal_thirdRoot
    public void testNthRoot_normal_thirdRoot() {
        
        Complex z = new Complex(-2,2);
        
        Complex[] thirdRootsOfZ = z.nthRoot(3).toArray(new Complex[0]);
        
        assertEquals(3, thirdRootsOfZ.length);
        
        assertEquals(1.0,                  thirdRootsOfZ[0].getReal(),      1.0e-5);
        assertEquals(1.0,                  thirdRootsOfZ[0].getImaginary(), 1.0e-5);
        
        assertEquals(-1.3660254037844386,  thirdRootsOfZ[1].getReal(),      1.0e-5);
        assertEquals(0.36602540378443843,  thirdRootsOfZ[1].getImaginary(), 1.0e-5);
        
        assertEquals(0.366025403784439,    thirdRootsOfZ[2].getReal(),      1.0e-5);
        assertEquals(-1.3660254037844384,  thirdRootsOfZ[2].getImaginary(), 1.0e-5);
    }

// org.apache.commons.math.complex.ComplexTest::testNthRoot_normal_fourthRoot
    public void testNthRoot_normal_fourthRoot() {
        
        Complex z = new Complex(5,-2);
        
        Complex[] fourthRootsOfZ = z.nthRoot(4).toArray(new Complex[0]);
        
        assertEquals(4, fourthRootsOfZ.length);
        
        assertEquals(1.5164629308487783,     fourthRootsOfZ[0].getReal(),      1.0e-5);
        assertEquals(-0.14469266210702247,   fourthRootsOfZ[0].getImaginary(), 1.0e-5);
        
        assertEquals(0.14469266210702256,    fourthRootsOfZ[1].getReal(),      1.0e-5);
        assertEquals(1.5164629308487783,     fourthRootsOfZ[1].getImaginary(), 1.0e-5);
        
        assertEquals(-1.5164629308487783,    fourthRootsOfZ[2].getReal(),      1.0e-5);
        assertEquals(0.14469266210702267,    fourthRootsOfZ[2].getImaginary(), 1.0e-5);
        
        assertEquals(-0.14469266210702275,   fourthRootsOfZ[3].getReal(),      1.0e-5);
        assertEquals(-1.5164629308487783,    fourthRootsOfZ[3].getImaginary(), 1.0e-5);
    }

// org.apache.commons.math.complex.ComplexTest::testNthRoot_cornercase_thirdRoot_imaginaryPartEmpty
    public void testNthRoot_cornercase_thirdRoot_imaginaryPartEmpty() {
        
        
        Complex z = new Complex(8,0);
        
        Complex[] thirdRootsOfZ = z.nthRoot(3).toArray(new Complex[0]);
        
        assertEquals(3, thirdRootsOfZ.length);
        
        assertEquals(2.0,                thirdRootsOfZ[0].getReal(),      1.0e-5);
        assertEquals(0.0,                thirdRootsOfZ[0].getImaginary(), 1.0e-5);
        
        assertEquals(-1.0,               thirdRootsOfZ[1].getReal(),      1.0e-5);
        assertEquals(1.7320508075688774, thirdRootsOfZ[1].getImaginary(), 1.0e-5);
        
        assertEquals(-1.0,               thirdRootsOfZ[2].getReal(),      1.0e-5);
        assertEquals(-1.732050807568877, thirdRootsOfZ[2].getImaginary(), 1.0e-5);
    }

// org.apache.commons.math.complex.ComplexTest::testNthRoot_cornercase_thirdRoot_realPartZero
    public void testNthRoot_cornercase_thirdRoot_realPartZero() {
        
        Complex z = new Complex(0,2);
        
        Complex[] thirdRootsOfZ = z.nthRoot(3).toArray(new Complex[0]);
        
        assertEquals(3, thirdRootsOfZ.length);
        
        assertEquals(1.0911236359717216,      thirdRootsOfZ[0].getReal(),      1.0e-5);
        assertEquals(0.6299605249474365,      thirdRootsOfZ[0].getImaginary(), 1.0e-5);
        
        assertEquals(-1.0911236359717216,     thirdRootsOfZ[1].getReal(),      1.0e-5);
        assertEquals(0.6299605249474365,      thirdRootsOfZ[1].getImaginary(), 1.0e-5);
        
        assertEquals(-2.3144374213981936E-16, thirdRootsOfZ[2].getReal(),      1.0e-5);
        assertEquals(-1.2599210498948732,     thirdRootsOfZ[2].getImaginary(), 1.0e-5);
    }

// org.apache.commons.math.complex.ComplexTest::testNthRoot_cornercase_NAN_Inf
    public void testNthRoot_cornercase_NAN_Inf() {
        
        List<Complex> roots = oneNaN.nthRoot(3);
        assertEquals(1,roots.size());
        assertEquals(Complex.NaN, roots.get(0));

        roots = nanZero.nthRoot(3);
        assertEquals(1,roots.size());
        assertEquals(Complex.NaN, roots.get(0));

        
        roots = nanInf.nthRoot(3);
        assertEquals(1,roots.size());
        assertEquals(Complex.NaN, roots.get(0));

        
        roots = oneInf.nthRoot(3);
        assertEquals(1,roots.size());
        assertEquals(Complex.INF, roots.get(0));

        
        roots = negInfInf.nthRoot(3);
        assertEquals(1,roots.size());
        assertEquals(Complex.INF, roots.get(0));
    }

// org.apache.commons.math.complex.ComplexTest::testGetArgument
    public void testGetArgument() {
        Complex z = new Complex(1, 0);
        assertEquals(0.0, z.getArgument(), 1.0e-12);

        z = new Complex(1, 1);
        assertEquals(Math.PI/4, z.getArgument(), 1.0e-12);

        z = new Complex(0, 1);
        assertEquals(Math.PI/2, z.getArgument(), 1.0e-12);

        z = new Complex(-1, 1);
        assertEquals(3 * Math.PI/4, z.getArgument(), 1.0e-12);

        z = new Complex(-1, 0);
        assertEquals(Math.PI, z.getArgument(), 1.0e-12);

        z = new Complex(-1, -1);
        assertEquals(-3 * Math.PI/4, z.getArgument(), 1.0e-12);

        z = new Complex(0, -1);
        assertEquals(-Math.PI/2, z.getArgument(), 1.0e-12);

        z = new Complex(1, -1);
        assertEquals(-Math.PI/4, z.getArgument(), 1.0e-12);

    }

// org.apache.commons.math.complex.ComplexTest::testGetArgumentInf
    public void testGetArgumentInf() {
        assertEquals(Math.PI/4, infInf.getArgument(), 1.0e-12);
        assertEquals(Math.PI/2, oneInf.getArgument(), 1.0e-12);
        assertEquals(0.0, infOne.getArgument(), 1.0e-12);
        assertEquals(Math.PI/2, zeroInf.getArgument(), 1.0e-12);
        assertEquals(0.0, infZero.getArgument(), 1.0e-12);
        assertEquals(Math.PI, negInfOne.getArgument(), 1.0e-12);
        assertEquals(-3.0*Math.PI/4, negInfNegInf.getArgument(), 1.0e-12);
        assertEquals(-Math.PI/2, oneNegInf.getArgument(), 1.0e-12);
    }

// org.apache.commons.math.complex.ComplexTest::testGetArgumentNaN
    public void testGetArgumentNaN() {
        assertEquals(nan, nanZero.getArgument());
        assertEquals(nan, zeroNaN.getArgument());
        assertEquals(nan, Complex.NaN.getArgument());
    }

// org.apache.commons.math.complex.ComplexTest::testSerial
    public void testSerial() {
        Complex z = new Complex(3.0, 4.0);
        assertEquals(z, TestUtils.serializeAndRecover(z));
        Complex ncmplx = (Complex)TestUtils.serializeAndRecover(oneNaN);
        assertEquals(nanZero, ncmplx);
        assertTrue(ncmplx.isNaN());
        Complex infcmplx = (Complex)TestUtils.serializeAndRecover(infInf);
        assertEquals(infInf, infcmplx);
        assertTrue(infcmplx.isInfinite());
        TestComplex tz = new TestComplex(3.0, 4.0);
        assertEquals(tz, TestUtils.serializeAndRecover(tz));
        TestComplex ntcmplx = (TestComplex)TestUtils.serializeAndRecover(new TestComplex(oneNaN));
        assertEquals(nanZero, ntcmplx);
        assertTrue(ntcmplx.isNaN());
        TestComplex inftcmplx = (TestComplex)TestUtils.serializeAndRecover(new TestComplex(infInf));
        assertEquals(infInf, inftcmplx);
        assertTrue(inftcmplx.isInfinite());
    }

// org.apache.commons.math.distribution.HypergeometricDistributionTest::testDegenerateNoFailures
    public void testDegenerateNoFailures() throws Exception {
        setDistribution(new HypergeometricDistributionImpl(5,5,3));
        setCumulativeTestPoints(new int[] {-1, 0, 1, 3, 10 });
        setCumulativeTestValues(new double[] {0d, 0d, 0d, 1d, 1d});
        setDensityTestPoints(new int[] {-1, 0, 1, 3, 10});
        setDensityTestValues(new double[] {0d, 0d, 0d, 1d, 0d});
        setInverseCumulativeTestPoints(new double[] {0.1d, 0.5d});
        setInverseCumulativeTestValues(new int[] {2, 2});
        verifyDensities();
        verifyCumulativeProbabilities();
        verifyInverseCumulativeProbabilities();
    }

// org.apache.commons.math.distribution.HypergeometricDistributionTest::testDegenerateNoSuccesses
    public void testDegenerateNoSuccesses() throws Exception {
        setDistribution(new HypergeometricDistributionImpl(5,0,3));
        setCumulativeTestPoints(new int[] {-1, 0, 1, 3, 10 });
        setCumulativeTestValues(new double[] {0d, 1d, 1d, 1d, 1d});
        setDensityTestPoints(new int[] {-1, 0, 1, 3, 10});
        setDensityTestValues(new double[] {0d, 1d, 0d, 0d, 0d});
        setInverseCumulativeTestPoints(new double[] {0.1d, 0.5d});
        setInverseCumulativeTestValues(new int[] {-1, -1});
        verifyDensities();
        verifyCumulativeProbabilities();
        verifyInverseCumulativeProbabilities();
    }

// org.apache.commons.math.distribution.HypergeometricDistributionTest::testDegenerateFullSample
    public void testDegenerateFullSample() throws Exception {
        setDistribution(new HypergeometricDistributionImpl(5,3,5));
        setCumulativeTestPoints(new int[] {-1, 0, 1, 3, 10 });
        setCumulativeTestValues(new double[] {0d, 0d, 0d, 1d, 1d});
        setDensityTestPoints(new int[] {-1, 0, 1, 3, 10});
        setDensityTestValues(new double[] {0d, 0d, 0d, 1d, 0d});
        setInverseCumulativeTestPoints(new double[] {0.1d, 0.5d});
        setInverseCumulativeTestValues(new int[] {2, 2});
        verifyDensities();
        verifyCumulativeProbabilities();
        verifyInverseCumulativeProbabilities();
    }

// org.apache.commons.math.distribution.HypergeometricDistributionTest::testPopulationSize
    public void testPopulationSize() {
        HypergeometricDistribution dist = new HypergeometricDistributionImpl(5,3,5);
        try {
            dist.setPopulationSize(-1);
            fail("negative population size.  IllegalArgumentException expected");
        } catch(IllegalArgumentException ex) {
        }

        dist.setPopulationSize(10);
        assertEquals(10, dist.getPopulationSize());
    }

// org.apache.commons.math.distribution.HypergeometricDistributionTest::testLargeValues
    public void testLargeValues() {
        int populationSize = 3456;
        int sampleSize = 789;
        int numberOfSucceses = 101;
        double[][] data = {
            {0.0, 2.75646034603961e-12, 2.75646034603961e-12, 1.0},
            {1.0, 8.55705370142386e-11, 8.83269973602783e-11, 0.999999999997244},
            {2.0, 1.31288129219665e-9, 1.40120828955693e-9, 0.999999999911673},
            {3.0, 1.32724172984193e-8, 1.46736255879763e-8, 0.999999998598792},
            {4.0, 9.94501711734089e-8, 1.14123796761385e-7, 0.999999985326375},
            {5.0, 5.89080768883643e-7, 7.03204565645028e-7, 0.999999885876203},
            {20.0, 0.0760051397707708, 0.27349758476299, 0.802507555007781},
            {21.0, 0.087144222047629, 0.360641806810619, 0.72650241523701},
            {22.0, 0.0940378846881819, 0.454679691498801, 0.639358193189381},
            {23.0, 0.0956897500614809, 0.550369441560282, 0.545320308501199},
            {24.0, 0.0919766921922999, 0.642346133752582, 0.449630558439718},
            {25.0, 0.083641637261095, 0.725987771013677, 0.357653866247418},
            {96.0, 5.93849188852098e-57, 1.0, 6.01900244560712e-57},
            {97.0, 7.96593036832547e-59, 1.0, 8.05105570861321e-59},
            {98.0, 8.44582921934367e-61, 1.0, 8.5125340287733e-61},
            {99.0, 6.63604297068222e-63, 1.0, 6.670480942963e-63},
            {100.0, 3.43501099007557e-65, 1.0, 3.4437972280786e-65},
            {101.0, 8.78623800302957e-68, 1.0, 8.78623800302957e-68},
        };

        testHypergeometricDistributionProbabilities(populationSize, sampleSize, numberOfSucceses, data);
    }

// org.apache.commons.math.distribution.HypergeometricDistributionTest::testMoreLargeValues
    public void testMoreLargeValues() {
        int populationSize = 26896;
        int sampleSize = 895;
        int numberOfSucceses = 55;
        double[][] data = {
            {0.0, 0.155168304750504, 0.155168304750504, 1.0},
            {1.0, 0.29437545000746, 0.449543754757964, 0.844831695249496},
            {2.0, 0.273841321577003, 0.723385076334967, 0.550456245242036},
            {3.0, 0.166488572570786, 0.889873648905753, 0.276614923665033},
            {4.0, 0.0743969744713231, 0.964270623377076, 0.110126351094247},
            {5.0, 0.0260542785784855, 0.990324901955562, 0.0357293766229237},
            {20.0, 3.57101101678792e-16, 1.0, 3.78252101622096e-16},
            {21.0, 2.00551638598312e-17, 1.0, 2.11509999433041e-17},
            {22.0, 1.04317070180562e-18, 1.0, 1.09583608347287e-18},
            {23.0, 5.03153504903308e-20, 1.0, 5.266538166725e-20},
            {24.0, 2.2525984149695e-21, 1.0, 2.35003117691919e-21},
            {25.0, 9.3677424515947e-23, 1.0, 9.74327619496943e-23},
            {50.0, 9.83633962945521e-69, 1.0, 9.8677629437617e-69},
            {51.0, 3.13448949497553e-71, 1.0, 3.14233143064882e-71},
            {52.0, 7.82755221928122e-74, 1.0, 7.84193567329055e-74},
            {53.0, 1.43662126065532e-76, 1.0, 1.43834540093295e-76},
            {54.0, 1.72312692517348e-79, 1.0, 1.7241402776278e-79},
            {55.0, 1.01335245432581e-82, 1.0, 1.01335245432581e-82},
        };
        testHypergeometricDistributionProbabilities(populationSize, sampleSize, numberOfSucceses, data);
    }

// org.apache.commons.math.distribution.PascalDistributionTest::testDegenerate0
    public void testDegenerate0() throws Exception {
        setDistribution(new PascalDistributionImpl(5,0.0d));
        setCumulativeTestPoints(new int[] {-1, 0, 1, 5, 10 });
        setCumulativeTestValues(new double[] {0d, 0d, 0d, 0d, 0d});
        setDensityTestPoints(new int[] {-1, 0, 1, 10, 11});
        setDensityTestValues(new double[] {0d, 0d, 0d, 0d, 0d});
        setInverseCumulativeTestPoints(new double[] {0.1d, 0.5d});
        setInverseCumulativeTestValues(new int[] {Integer.MAX_VALUE - 1, Integer.MAX_VALUE - 1});
        verifyDensities();
        verifyCumulativeProbabilities();
        verifyInverseCumulativeProbabilities();
    }

// org.apache.commons.math.distribution.PascalDistributionTest::testDegenerate1
    public void testDegenerate1() throws Exception {
        setDistribution(new PascalDistributionImpl(5,1.0d));
        setCumulativeTestPoints(new int[] {-1, 0, 1, 2, 5, 10 });
        setCumulativeTestValues(new double[] {0d, 1d, 1d, 1d, 1d, 1d});
        setDensityTestPoints(new int[] {-1, 0, 1, 2, 5, 10});
        setDensityTestValues(new double[] {0d, 1d, 0d, 0d, 0d, 0d});
        setInverseCumulativeTestPoints(new double[] {0.1d, 0.5d});
        setInverseCumulativeTestValues(new int[] {-1, -1});
        verifyDensities();
        verifyCumulativeProbabilities();
        verifyInverseCumulativeProbabilities();
    }

// org.apache.commons.math.fraction.BigFractionTest::testConstructor
    public void testConstructor() {
        assertFraction(0, 1, new BigFraction(0, 1));
        assertFraction(0, 1, new BigFraction(0l, 2l));
        assertFraction(0, 1, new BigFraction(0, -1));
        assertFraction(1, 2, new BigFraction(1, 2));
        assertFraction(1, 2, new BigFraction(2, 4));
        assertFraction(-1, 2, new BigFraction(-1, 2));
        assertFraction(-1, 2, new BigFraction(1, -2));
        assertFraction(-1, 2, new BigFraction(-2, 4));
        assertFraction(-1, 2, new BigFraction(2, -4));
        assertFraction(11, 1, new BigFraction(11));
        assertFraction(11, 1, new BigFraction(11l));
        assertFraction(11, 1, new BigFraction(new BigInteger("11")));

        try {
            assertFraction(0, 1, new BigFraction(0.00000000000001, 1.0e-5, 100));
            assertFraction(2, 5, new BigFraction(0.40000000000001, 1.0e-5, 100));
            assertFraction(15, 1, new BigFraction(15.0000000000001, 1.0e-5, 100));
        } catch (ConvergenceException ex) {
            fail(ex.getMessage());
        }
        assertEquals(0.00000000000001, new BigFraction(0.00000000000001).doubleValue(), 0.0);
        assertEquals(0.40000000000001, new BigFraction(0.40000000000001).doubleValue(), 0.0);
        assertEquals(15.0000000000001, new BigFraction(15.0000000000001).doubleValue(), 0.0);
        assertFraction(3602879701896487l, 9007199254740992l, new BigFraction(0.40000000000001));
        assertFraction(1055531162664967l, 70368744177664l, new BigFraction(15.0000000000001));
        try {
            new BigFraction(null, BigInteger.ONE);
        } catch (NullPointerException npe) {
            
        }
        try {
            new BigFraction(BigInteger.ONE, null);
        } catch (NullPointerException npe) {
            
        }
        try {
            new BigFraction(BigInteger.ONE, BigInteger.ZERO);
        } catch (ArithmeticException npe) {
            
        }
        try {
            new BigFraction(2.0 * Integer.MAX_VALUE, 1.0e-5, 100000);
        } catch (FractionConversionException fce) {
            
        }
    }

// org.apache.commons.math.fraction.BigFractionTest::testGoldenRatio
    public void testGoldenRatio() {
        try {
            
            
            new BigFraction((1 + Math.sqrt(5)) / 2, 1.0e-12, 25);
            fail("an exception should have been thrown");
        } catch (ConvergenceException ce) {
            
        } catch (Exception e) {
            fail("wrong exception caught");
        }
    }

// org.apache.commons.math.fraction.BigFractionTest::testDoubleConstructor
    public void testDoubleConstructor() throws ConvergenceException {
        assertFraction(1, 2, new BigFraction((double) 1 / (double) 2, 1.0e-5, 100));
        assertFraction(1, 3, new BigFraction((double) 1 / (double) 3, 1.0e-5, 100));
        assertFraction(2, 3, new BigFraction((double) 2 / (double) 3, 1.0e-5, 100));
        assertFraction(1, 4, new BigFraction((double) 1 / (double) 4, 1.0e-5, 100));
        assertFraction(3, 4, new BigFraction((double) 3 / (double) 4, 1.0e-5, 100));
        assertFraction(1, 5, new BigFraction((double) 1 / (double) 5, 1.0e-5, 100));
        assertFraction(2, 5, new BigFraction((double) 2 / (double) 5, 1.0e-5, 100));
        assertFraction(3, 5, new BigFraction((double) 3 / (double) 5, 1.0e-5, 100));
        assertFraction(4, 5, new BigFraction((double) 4 / (double) 5, 1.0e-5, 100));
        assertFraction(1, 6, new BigFraction((double) 1 / (double) 6, 1.0e-5, 100));
        assertFraction(5, 6, new BigFraction((double) 5 / (double) 6, 1.0e-5, 100));
        assertFraction(1, 7, new BigFraction((double) 1 / (double) 7, 1.0e-5, 100));
        assertFraction(2, 7, new BigFraction((double) 2 / (double) 7, 1.0e-5, 100));
        assertFraction(3, 7, new BigFraction((double) 3 / (double) 7, 1.0e-5, 100));
        assertFraction(4, 7, new BigFraction((double) 4 / (double) 7, 1.0e-5, 100));
        assertFraction(5, 7, new BigFraction((double) 5 / (double) 7, 1.0e-5, 100));
        assertFraction(6, 7, new BigFraction((double) 6 / (double) 7, 1.0e-5, 100));
        assertFraction(1, 8, new BigFraction((double) 1 / (double) 8, 1.0e-5, 100));
        assertFraction(3, 8, new BigFraction((double) 3 / (double) 8, 1.0e-5, 100));
        assertFraction(5, 8, new BigFraction((double) 5 / (double) 8, 1.0e-5, 100));
        assertFraction(7, 8, new BigFraction((double) 7 / (double) 8, 1.0e-5, 100));
        assertFraction(1, 9, new BigFraction((double) 1 / (double) 9, 1.0e-5, 100));
        assertFraction(2, 9, new BigFraction((double) 2 / (double) 9, 1.0e-5, 100));
        assertFraction(4, 9, new BigFraction((double) 4 / (double) 9, 1.0e-5, 100));
        assertFraction(5, 9, new BigFraction((double) 5 / (double) 9, 1.0e-5, 100));
        assertFraction(7, 9, new BigFraction((double) 7 / (double) 9, 1.0e-5, 100));
        assertFraction(8, 9, new BigFraction((double) 8 / (double) 9, 1.0e-5, 100));
        assertFraction(1, 10, new BigFraction((double) 1 / (double) 10, 1.0e-5, 100));
        assertFraction(3, 10, new BigFraction((double) 3 / (double) 10, 1.0e-5, 100));
        assertFraction(7, 10, new BigFraction((double) 7 / (double) 10, 1.0e-5, 100));
        assertFraction(9, 10, new BigFraction((double) 9 / (double) 10, 1.0e-5, 100));
        assertFraction(1, 11, new BigFraction((double) 1 / (double) 11, 1.0e-5, 100));
        assertFraction(2, 11, new BigFraction((double) 2 / (double) 11, 1.0e-5, 100));
        assertFraction(3, 11, new BigFraction((double) 3 / (double) 11, 1.0e-5, 100));
        assertFraction(4, 11, new BigFraction((double) 4 / (double) 11, 1.0e-5, 100));
        assertFraction(5, 11, new BigFraction((double) 5 / (double) 11, 1.0e-5, 100));
        assertFraction(6, 11, new BigFraction((double) 6 / (double) 11, 1.0e-5, 100));
        assertFraction(7, 11, new BigFraction((double) 7 / (double) 11, 1.0e-5, 100));
        assertFraction(8, 11, new BigFraction((double) 8 / (double) 11, 1.0e-5, 100));
        assertFraction(9, 11, new BigFraction((double) 9 / (double) 11, 1.0e-5, 100));
        assertFraction(10, 11, new BigFraction((double) 10 / (double) 11, 1.0e-5, 100));
    }

// org.apache.commons.math.fraction.BigFractionTest::testDigitLimitConstructor
    public void testDigitLimitConstructor() throws ConvergenceException {
        assertFraction(2, 5, new BigFraction(0.4, 9));
        assertFraction(2, 5, new BigFraction(0.4, 99));
        assertFraction(2, 5, new BigFraction(0.4, 999));

        assertFraction(3, 5, new BigFraction(0.6152, 9));
        assertFraction(8, 13, new BigFraction(0.6152, 99));
        assertFraction(510, 829, new BigFraction(0.6152, 999));
        assertFraction(769, 1250, new BigFraction(0.6152, 9999));
    }

// org.apache.commons.math.fraction.BigFractionTest::testEpsilonLimitConstructor
    public void testEpsilonLimitConstructor() throws ConvergenceException {
        assertFraction(2, 5, new BigFraction(0.4, 1.0e-5, 100));

        assertFraction(3, 5, new BigFraction(0.6152, 0.02, 100));
        assertFraction(8, 13, new BigFraction(0.6152, 1.0e-3, 100));
        assertFraction(251, 408, new BigFraction(0.6152, 1.0e-4, 100));
        assertFraction(251, 408, new BigFraction(0.6152, 1.0e-5, 100));
        assertFraction(510, 829, new BigFraction(0.6152, 1.0e-6, 100));
        assertFraction(769, 1250, new BigFraction(0.6152, 1.0e-7, 100));
    }

// org.apache.commons.math.fraction.BigFractionTest::testCompareTo
    public void testCompareTo() {
        BigFraction first = new BigFraction(1, 2);
        BigFraction second = new BigFraction(1, 3);
        BigFraction third = new BigFraction(1, 2);

        assertEquals(0, first.compareTo(first));
        assertEquals(0, first.compareTo(third));
        assertEquals(1, first.compareTo(second));
        assertEquals(-1, second.compareTo(first));

        
        
        
        BigFraction pi1 = new BigFraction(1068966896, 340262731);
        BigFraction pi2 = new BigFraction( 411557987, 131002976);
        assertEquals(-1, pi1.compareTo(pi2));
        assertEquals( 1, pi2.compareTo(pi1));
        assertEquals(0.0, pi1.doubleValue() - pi2.doubleValue(), 1.0e-20);

    }

// org.apache.commons.math.fraction.BigFractionTest::testDoubleValue
    public void testDoubleValue() {
        BigFraction first = new BigFraction(1, 2);
        BigFraction second = new BigFraction(1, 3);

        assertEquals(0.5, first.doubleValue(), 0.0);
        assertEquals(1.0 / 3.0, second.doubleValue(), 0.0);
    }

// org.apache.commons.math.fraction.BigFractionTest::testFloatValue
    public void testFloatValue() {
        BigFraction first = new BigFraction(1, 2);
        BigFraction second = new BigFraction(1, 3);

        assertEquals(0.5f, first.floatValue(), 0.0f);
        assertEquals((float) (1.0 / 3.0), second.floatValue(), 0.0f);
    }

// org.apache.commons.math.fraction.BigFractionTest::testIntValue
    public void testIntValue() {
        BigFraction first = new BigFraction(1, 2);
        BigFraction second = new BigFraction(3, 2);

        assertEquals(0, first.intValue());
        assertEquals(1, second.intValue());
    }

// org.apache.commons.math.fraction.BigFractionTest::testLongValue
    public void testLongValue() {
        BigFraction first = new BigFraction(1, 2);
        BigFraction second = new BigFraction(3, 2);

        assertEquals(0L, first.longValue());
        assertEquals(1L, second.longValue());
    }

// org.apache.commons.math.fraction.BigFractionTest::testConstructorDouble
    public void testConstructorDouble() {
        assertFraction(1, 2, new BigFraction(0.5));
        assertFraction(6004799503160661l, 18014398509481984l, new BigFraction(1.0 / 3.0));
        assertFraction(6124895493223875l, 36028797018963968l, new BigFraction(17.0 / 100.0));
        assertFraction(1784551352345559l, 562949953421312l, new BigFraction(317.0 / 100.0));
        assertFraction(-1, 2, new BigFraction(-0.5));
        assertFraction(-6004799503160661l, 18014398509481984l, new BigFraction(-1.0 / 3.0));
        assertFraction(-6124895493223875l, 36028797018963968l, new BigFraction(17.0 / -100.0));
        assertFraction(-1784551352345559l, 562949953421312l, new BigFraction(-317.0 / 100.0));
        for (double v : new double[] { Double.NaN, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY}) {
            try {
                new BigFraction(v);
                fail("expected exception");
            } catch (IllegalArgumentException iae) {
                
            }
        }
        assertEquals(1l, new BigFraction(Double.MAX_VALUE).getDenominatorAsLong());
        assertEquals(1l, new BigFraction(Double.longBitsToDouble(0x0010000000000000L)).getNumeratorAsLong());
        assertEquals(1l, new BigFraction(Double.MIN_VALUE).getNumeratorAsLong());
    }

// org.apache.commons.math.fraction.BigFractionTest::testAbs
    public void testAbs() {
        BigFraction a = new BigFraction(10, 21);
        BigFraction b = new BigFraction(-10, 21);
        BigFraction c = new BigFraction(10, -21);

        assertFraction(10, 21, a.abs());
        assertFraction(10, 21, b.abs());
        assertFraction(10, 21, c.abs());
    }

// org.apache.commons.math.fraction.BigFractionTest::testReciprocal
    public void testReciprocal() {
        BigFraction f = null;

        f = new BigFraction(50, 75);
        f = f.reciprocal();
        assertEquals(3, f.getNumeratorAsInt());
        assertEquals(2, f.getDenominatorAsInt());

        f = new BigFraction(4, 3);
        f = f.reciprocal();
        assertEquals(3, f.getNumeratorAsInt());
        assertEquals(4, f.getDenominatorAsInt());

        f = new BigFraction(-15, 47);
        f = f.reciprocal();
        assertEquals(-47, f.getNumeratorAsInt());
        assertEquals(15, f.getDenominatorAsInt());

        f = new BigFraction(0, 3);
        try {
            f = f.reciprocal();
            fail("expecting ArithmeticException");
        } catch (ArithmeticException ex) {
        }

        
        f = new BigFraction(Integer.MAX_VALUE, 1);
        f = f.reciprocal();
        assertEquals(1, f.getNumeratorAsInt());
        assertEquals(Integer.MAX_VALUE, f.getDenominatorAsInt());
    }

// org.apache.commons.math.fraction.BigFractionTest::testNegate
    public void testNegate() {
        BigFraction f = null;

        f = new BigFraction(50, 75);
        f = f.negate();
        assertEquals(-2, f.getNumeratorAsInt());
        assertEquals(3, f.getDenominatorAsInt());

        f = new BigFraction(-50, 75);
        f = f.negate();
        assertEquals(2, f.getNumeratorAsInt());
        assertEquals(3, f.getDenominatorAsInt());

        
        f = new BigFraction(Integer.MAX_VALUE - 1, Integer.MAX_VALUE);
        f = f.negate();
        assertEquals(Integer.MIN_VALUE + 2, f.getNumeratorAsInt());
        assertEquals(Integer.MAX_VALUE, f.getDenominatorAsInt());

    }

// org.apache.commons.math.fraction.BigFractionTest::testAdd
    public void testAdd() {
        BigFraction a = new BigFraction(1, 2);
        BigFraction b = new BigFraction(2, 3);

        assertFraction(1, 1, a.add(a));
        assertFraction(7, 6, a.add(b));
        assertFraction(7, 6, b.add(a));
        assertFraction(4, 3, b.add(b));

        BigFraction f1 = new BigFraction(Integer.MAX_VALUE - 1, 1);
        BigFraction f2 = BigFraction.ONE;
        BigFraction f = f1.add(f2);
        assertEquals(Integer.MAX_VALUE, f.getNumeratorAsInt());
        assertEquals(1, f.getDenominatorAsInt());

        f1 = new BigFraction(-1, 13 * 13 * 2 * 2);
        f2 = new BigFraction(-2, 13 * 17 * 2);
        f = f1.add(f2);
        assertEquals(13 * 13 * 17 * 2 * 2, f.getDenominatorAsInt());
        assertEquals(-17 - 2 * 13 * 2, f.getNumeratorAsInt());

        try {
            f.add((BigFraction) null);
            fail("expecting NullPointerException");
        } catch (NullPointerException ex) {
        }

        
        
        f1 = new BigFraction(1, 32768 * 3);
        f2 = new BigFraction(1, 59049);
        f = f1.add(f2);
        assertEquals(52451, f.getNumeratorAsInt());
        assertEquals(1934917632, f.getDenominatorAsInt());

        f1 = new BigFraction(Integer.MIN_VALUE, 3);
        f2 = new BigFraction(1, 3);
        f = f1.add(f2);
        assertEquals(Integer.MIN_VALUE + 1, f.getNumeratorAsInt());
        assertEquals(3, f.getDenominatorAsInt());

        f1 = new BigFraction(Integer.MAX_VALUE - 1, 1);
        f = f1.add(BigInteger.ONE);
        assertEquals(Integer.MAX_VALUE, f.getNumeratorAsInt());
        assertEquals(1, f.getDenominatorAsInt());

        f = f.add(BigInteger.ZERO);
        assertEquals(Integer.MAX_VALUE, f.getNumeratorAsInt());
        assertEquals(1, f.getDenominatorAsInt());

        f1 = new BigFraction(Integer.MAX_VALUE - 1, 1);
        f = f1.add(1);
        assertEquals(Integer.MAX_VALUE, f.getNumeratorAsInt());
        assertEquals(1, f.getDenominatorAsInt());

        f = f.add(0);
        assertEquals(Integer.MAX_VALUE, f.getNumeratorAsInt());
        assertEquals(1, f.getDenominatorAsInt());

        f1 = new BigFraction(Integer.MAX_VALUE - 1, 1);
        f = f1.add(1l);
        assertEquals(Integer.MAX_VALUE, f.getNumeratorAsInt());
        assertEquals(1, f.getDenominatorAsInt());

        f = f.add(0l);
        assertEquals(Integer.MAX_VALUE, f.getNumeratorAsInt());
        assertEquals(1, f.getDenominatorAsInt());

    }

// org.apache.commons.math.fraction.BigFractionTest::testDivide
    public void testDivide() {
        BigFraction a = new BigFraction(1, 2);
        BigFraction b = new BigFraction(2, 3);

        assertFraction(1, 1, a.divide(a));
        assertFraction(3, 4, a.divide(b));
        assertFraction(4, 3, b.divide(a));
        assertFraction(1, 1, b.divide(b));

        BigFraction f1 = new BigFraction(3, 5);
        BigFraction f2 = BigFraction.ZERO;
        try {
            f1.divide(f2);
            fail("expecting ArithmeticException");
        } catch (ArithmeticException ex) {
        }

        f1 = new BigFraction(0, 5);
        f2 = new BigFraction(2, 7);
        BigFraction f = f1.divide(f2);
        assertSame(BigFraction.ZERO, f);

        f1 = new BigFraction(2, 7);
        f2 = BigFraction.ONE;
        f = f1.divide(f2);
        assertEquals(2, f.getNumeratorAsInt());
        assertEquals(7, f.getDenominatorAsInt());

        f1 = new BigFraction(1, Integer.MAX_VALUE);
        f = f1.divide(f1);
        assertEquals(1, f.getNumeratorAsInt());
        assertEquals(1, f.getDenominatorAsInt());

        f1 = new BigFraction(Integer.MIN_VALUE, Integer.MAX_VALUE);
        f2 = new BigFraction(1, Integer.MAX_VALUE);
        f = f1.divide(f2);
        assertEquals(Integer.MIN_VALUE, f.getNumeratorAsInt());
        assertEquals(1, f.getDenominatorAsInt());

        try {
            f.divide((BigFraction) null);
            fail("expecting NullPointerException");
        } catch (NullPointerException ex) {
        }

        f1 = new BigFraction(Integer.MIN_VALUE, Integer.MAX_VALUE);
        f = f1.divide(BigInteger.valueOf(Integer.MIN_VALUE));
        assertEquals(Integer.MAX_VALUE, f.getDenominatorAsInt());
        assertEquals(1, f.getNumeratorAsInt());

        f1 = new BigFraction(Integer.MIN_VALUE, Integer.MAX_VALUE);
        f = f1.divide(Integer.MIN_VALUE);
        assertEquals(Integer.MAX_VALUE, f.getDenominatorAsInt());
        assertEquals(1, f.getNumeratorAsInt());

        f1 = new BigFraction(Integer.MIN_VALUE, Integer.MAX_VALUE);
        f = f1.divide((long) Integer.MIN_VALUE);
        assertEquals(Integer.MAX_VALUE, f.getDenominatorAsInt());
        assertEquals(1, f.getNumeratorAsInt());

    }

// org.apache.commons.math.fraction.BigFractionTest::testMultiply
    public void testMultiply() {
        BigFraction a = new BigFraction(1, 2);
        BigFraction b = new BigFraction(2, 3);

        assertFraction(1, 4, a.multiply(a));
        assertFraction(1, 3, a.multiply(b));
        assertFraction(1, 3, b.multiply(a));
        assertFraction(4, 9, b.multiply(b));

        BigFraction f1 = new BigFraction(Integer.MAX_VALUE, 1);
        BigFraction f2 = new BigFraction(Integer.MIN_VALUE, Integer.MAX_VALUE);
        BigFraction f = f1.multiply(f2);
        assertEquals(Integer.MIN_VALUE, f.getNumeratorAsInt());
        assertEquals(1, f.getDenominatorAsInt());

        f = f2.multiply(Integer.MAX_VALUE);
        assertEquals(Integer.MIN_VALUE, f.getNumeratorAsInt());
        assertEquals(1, f.getDenominatorAsInt());

        f = f2.multiply((long) Integer.MAX_VALUE);
        assertEquals(Integer.MIN_VALUE, f.getNumeratorAsInt());
        assertEquals(1, f.getDenominatorAsInt());

        try {
            f.multiply((BigFraction) null);
            fail("expecting NullPointerException");
        } catch (NullPointerException ex) {
        }

    }

// org.apache.commons.math.fraction.BigFractionTest::testSubtract
    public void testSubtract() {
        BigFraction a = new BigFraction(1, 2);
        BigFraction b = new BigFraction(2, 3);

        assertFraction(0, 1, a.subtract(a));
        assertFraction(-1, 6, a.subtract(b));
        assertFraction(1, 6, b.subtract(a));
        assertFraction(0, 1, b.subtract(b));

        BigFraction f = new BigFraction(1, 1);
        try {
            f.subtract((BigFraction) null);
            fail("expecting NullPointerException");
        } catch (NullPointerException ex) {
        }

        
        
        BigFraction f1 = new BigFraction(1, 32768 * 3);
        BigFraction f2 = new BigFraction(1, 59049);
        f = f1.subtract(f2);
        assertEquals(-13085, f.getNumeratorAsInt());
        assertEquals(1934917632, f.getDenominatorAsInt());

        f1 = new BigFraction(Integer.MIN_VALUE, 3);
        f2 = new BigFraction(1, 3).negate();
        f = f1.subtract(f2);
        assertEquals(Integer.MIN_VALUE + 1, f.getNumeratorAsInt());
        assertEquals(3, f.getDenominatorAsInt());

        f1 = new BigFraction(Integer.MAX_VALUE, 1);
        f2 = BigFraction.ONE;
        f = f1.subtract(f2);
        assertEquals(Integer.MAX_VALUE - 1, f.getNumeratorAsInt());
        assertEquals(1, f.getDenominatorAsInt());

    }

// org.apache.commons.math.fraction.BigFractionTest::testBigDecimalValue
    public void testBigDecimalValue() {
        assertEquals(new BigDecimal(0.5), new BigFraction(1, 2).bigDecimalValue());
        assertEquals(new BigDecimal("0.0003"), new BigFraction(3, 10000).bigDecimalValue());
        assertEquals(new BigDecimal("0"), new BigFraction(1, 3).bigDecimalValue(BigDecimal.ROUND_DOWN));
        assertEquals(new BigDecimal("0.333"), new BigFraction(1, 3).bigDecimalValue(3, BigDecimal.ROUND_DOWN));
    }

// org.apache.commons.math.fraction.BigFractionTest::testEqualsAndHashCode
    public void testEqualsAndHashCode() {
        BigFraction zero = new BigFraction(0, 1);
        BigFraction nullFraction = null;
        assertTrue(zero.equals(zero));
        assertFalse(zero.equals(nullFraction));
        assertFalse(zero.equals(Double.valueOf(0)));
        BigFraction zero2 = new BigFraction(0, 2);
        assertTrue(zero.equals(zero2));
        assertEquals(zero.hashCode(), zero2.hashCode());
        BigFraction one = new BigFraction(1, 1);
        assertFalse((one.equals(zero) || zero.equals(one)));
        assertTrue(one.equals(BigFraction.ONE));
    }

// org.apache.commons.math.fraction.BigFractionTest::testGetReducedFraction
    public void testGetReducedFraction() {
        BigFraction threeFourths = new BigFraction(3, 4);
        assertTrue(threeFourths.equals(BigFraction.getReducedFraction(6, 8)));
        assertTrue(BigFraction.ZERO.equals(BigFraction.getReducedFraction(0, -1)));
        try {
            BigFraction.getReducedFraction(1, 0);
            fail("expecting ArithmeticException");
        } catch (ArithmeticException ex) {
            
        }
        assertEquals(BigFraction.getReducedFraction(2, Integer.MIN_VALUE).getNumeratorAsInt(), -1);
        assertEquals(BigFraction.getReducedFraction(1, -1).getNumeratorAsInt(), -1);
    }

// org.apache.commons.math.fraction.BigFractionTest::testPow
    public void testPow() {
        assertEquals(new BigFraction(8192, 1594323), new BigFraction(2, 3).pow(13));
        assertEquals(new BigFraction(8192, 1594323), new BigFraction(2, 3).pow(13l));
        assertEquals(new BigFraction(8192, 1594323), new BigFraction(2, 3).pow(BigInteger.valueOf(13l)));
        assertEquals(BigFraction.ONE, new BigFraction(2, 3).pow(0));
        assertEquals(BigFraction.ONE, new BigFraction(2, 3).pow(0l));
        assertEquals(BigFraction.ONE, new BigFraction(2, 3).pow(BigInteger.valueOf(0l)));
        assertEquals(new BigFraction(1594323, 8192), new BigFraction(2, 3).pow(-13));
        assertEquals(new BigFraction(1594323, 8192), new BigFraction(2, 3).pow(-13l));
        assertEquals(new BigFraction(1594323, 8192), new BigFraction(2, 3).pow(BigInteger.valueOf(-13l)));
    }

// org.apache.commons.math.fraction.BigFractionTest::testSerial
    public void testSerial() throws FractionConversionException {
        BigFraction[] fractions = {
            new BigFraction(3, 4), BigFraction.ONE, BigFraction.ZERO,
            new BigFraction(17), new BigFraction(Math.PI, 1000),
            new BigFraction(-5, 2)
        };
        for (BigFraction fraction : fractions) {
            assertEquals(fraction, TestUtils.serializeAndRecover(fraction));
        }
    }

// org.apache.commons.math.fraction.FractionFieldTest::testZero
    public void testZero() {
        assertEquals(Fraction.ZERO, FractionField.getInstance().getZero());
    }

// org.apache.commons.math.fraction.FractionFieldTest::testOne
    public void testOne() {
        assertEquals(Fraction.ONE, FractionField.getInstance().getOne());
    }

// org.apache.commons.math.fraction.FractionFieldTest::testSerial
    public void testSerial() {
        
        FractionField field = FractionField.getInstance();
        assertTrue(field == TestUtils.serializeAndRecover(field));
    }

// org.apache.commons.math.fraction.FractionFormatTest::testFormat
    public void testFormat() {
        Fraction c = new Fraction(1, 2);
        String expected = "1 / 2";

        String actual = properFormat.format(c);
        assertEquals(expected, actual);

        actual = improperFormat.format(c);
        assertEquals(expected, actual);
    }

// org.apache.commons.math.fraction.FractionFormatTest::testFormatNegative
    public void testFormatNegative() {
        Fraction c = new Fraction(-1, 2);
        String expected = "-1 / 2";

        String actual = properFormat.format(c);
        assertEquals(expected, actual);

        actual = improperFormat.format(c);
        assertEquals(expected, actual);
    }

// org.apache.commons.math.fraction.FractionFormatTest::testFormatZero
    public void testFormatZero() {
        Fraction c = new Fraction(0, 1);
        String expected = "0 / 1";

        String actual = properFormat.format(c);
        assertEquals(expected, actual);

        actual = improperFormat.format(c);
        assertEquals(expected, actual);
    }

// org.apache.commons.math.fraction.FractionFormatTest::testFormatImproper
    public void testFormatImproper() {
        Fraction c = new Fraction(5, 3);

        String actual = properFormat.format(c);
        assertEquals("1 2 / 3", actual);

        actual = improperFormat.format(c);
        assertEquals("5 / 3", actual);
    }

// org.apache.commons.math.fraction.FractionFormatTest::testFormatImproperNegative
    public void testFormatImproperNegative() {
        Fraction c = new Fraction(-5, 3);

        String actual = properFormat.format(c);
        assertEquals("-1 2 / 3", actual);

        actual = improperFormat.format(c);
        assertEquals("-5 / 3", actual);
    }

// org.apache.commons.math.fraction.FractionFormatTest::testParse
    public void testParse() {
        String source = "1 / 2";

        try {
            Fraction c = properFormat.parse(source);
            assertNotNull(c);
            assertEquals(1, c.getNumerator());
            assertEquals(2, c.getDenominator());

            c = improperFormat.parse(source);
            assertNotNull(c);
            assertEquals(1, c.getNumerator());
            assertEquals(2, c.getDenominator());
        } catch (ParseException ex) {
            fail(ex.getMessage());
        }
    }

// org.apache.commons.math.fraction.FractionFormatTest::testParseInteger
    public void testParseInteger() {
        String source = "10";
        try {
            Fraction c = properFormat.parse(source);
            assertNotNull(c);
            assertEquals(10, c.getNumerator());
            assertEquals(1, c.getDenominator());
        } catch (ParseException ex) {
            fail(ex.getMessage());
        }
        try {
            Fraction c = improperFormat.parse(source);
            assertNotNull(c);
            assertEquals(10, c.getNumerator());
            assertEquals(1, c.getDenominator());
        } catch (ParseException ex) {
            fail(ex.getMessage());
        }
    }

// org.apache.commons.math.fraction.FractionFormatTest::testParseInvalid
    public void testParseInvalid() {
        String source = "a";
        String msg = "should not be able to parse '10 / a'.";
        try {
            properFormat.parse(source);
            fail(msg);
        } catch (ParseException ex) {
            
        }
        try {
            improperFormat.parse(source);
            fail(msg);
        } catch (ParseException ex) {
            
        }
    }

// org.apache.commons.math.fraction.FractionFormatTest::testParseInvalidDenominator
    public void testParseInvalidDenominator() {
        String source = "10 / a";
        String msg = "should not be able to parse '10 / a'.";
        try {
            properFormat.parse(source);
            fail(msg);
        } catch (ParseException ex) {
            
        }
        try {
            improperFormat.parse(source);
            fail(msg);
        } catch (ParseException ex) {
            
        }
    }

// org.apache.commons.math.fraction.FractionFormatTest::testParseNegative
    public void testParseNegative() {

        try {
            String source = "-1 / 2";
            Fraction c = properFormat.parse(source);
            assertNotNull(c);
            assertEquals(-1, c.getNumerator());
            assertEquals(2, c.getDenominator());

            c = improperFormat.parse(source);
            assertNotNull(c);
            assertEquals(-1, c.getNumerator());
            assertEquals(2, c.getDenominator());

            source = "1 / -2";
            c = properFormat.parse(source);
            assertNotNull(c);
            assertEquals(-1, c.getNumerator());
            assertEquals(2, c.getDenominator());

            c = improperFormat.parse(source);
            assertNotNull(c);
            assertEquals(-1, c.getNumerator());
            assertEquals(2, c.getDenominator());
        } catch (ParseException ex) {
            fail(ex.getMessage());
        }
    }

// org.apache.commons.math.fraction.FractionFormatTest::testParseProper
    public void testParseProper() {
        String source = "1 2 / 3";

        try {
            Fraction c = properFormat.parse(source);
            assertNotNull(c);
            assertEquals(5, c.getNumerator());
            assertEquals(3, c.getDenominator());
        } catch (ParseException ex) {
            fail(ex.getMessage());
        }

        try {
            improperFormat.parse(source);
            fail("invalid improper fraction.");
        } catch (ParseException ex) {
            
        }
    }

// org.apache.commons.math.fraction.FractionFormatTest::testParseProperNegative
    public void testParseProperNegative() {
        String source = "-1 2 / 3";
        try {
            Fraction c = properFormat.parse(source);
            assertNotNull(c);
            assertEquals(-5, c.getNumerator());
            assertEquals(3, c.getDenominator());
        } catch (ParseException ex) {
            fail(ex.getMessage());
        }

        try {
            improperFormat.parse(source);
            fail("invalid improper fraction.");
        } catch (ParseException ex) {
            
        }
    }

// org.apache.commons.math.fraction.FractionFormatTest::testParseProperInvalidMinus
    public void testParseProperInvalidMinus() {
        String source = "2 -2 / 3";
        try {
            properFormat.parse(source);
            fail("invalid minus in improper fraction.");
        } catch (ParseException ex) {
            
        }
        source = "2 2 / -3";
        try {
            properFormat.parse(source);
            fail("invalid minus in improper fraction.");
        } catch (ParseException ex) {
            
        }
    }

// org.apache.commons.math.fraction.FractionFormatTest::testNumeratorFormat
    public void testNumeratorFormat() {
        NumberFormat old = properFormat.getNumeratorFormat();
        NumberFormat nf = NumberFormat.getInstance();
        nf.setParseIntegerOnly(true);
        properFormat.setNumeratorFormat(nf);
        assertEquals(nf, properFormat.getNumeratorFormat());
        properFormat.setNumeratorFormat(old);

        old = improperFormat.getNumeratorFormat();
        nf = NumberFormat.getInstance();
        nf.setParseIntegerOnly(true);
        improperFormat.setNumeratorFormat(nf);
        assertEquals(nf, improperFormat.getNumeratorFormat());
        improperFormat.setNumeratorFormat(old);
    }

// org.apache.commons.math.fraction.FractionFormatTest::testDenominatorFormat
    public void testDenominatorFormat() {
        NumberFormat old = properFormat.getDenominatorFormat();
        NumberFormat nf = NumberFormat.getInstance();
        nf.setParseIntegerOnly(true);
        properFormat.setDenominatorFormat(nf);
        assertEquals(nf, properFormat.getDenominatorFormat());
        properFormat.setDenominatorFormat(old);

        old = improperFormat.getDenominatorFormat();
        nf = NumberFormat.getInstance();
        nf.setParseIntegerOnly(true);
        improperFormat.setDenominatorFormat(nf);
        assertEquals(nf, improperFormat.getDenominatorFormat());
        improperFormat.setDenominatorFormat(old);
    }

// org.apache.commons.math.fraction.FractionFormatTest::testWholeFormat
    public void testWholeFormat() {
        ProperFractionFormat format = (ProperFractionFormat)properFormat;

        NumberFormat old = format.getWholeFormat();
        NumberFormat nf = NumberFormat.getInstance();
        nf.setParseIntegerOnly(true);
        format.setWholeFormat(nf);
        assertEquals(nf, format.getWholeFormat());
        format.setWholeFormat(old);
    }

// org.apache.commons.math.fraction.FractionFormatTest::testLongFormat
    public void testLongFormat() {
        assertEquals("10 / 1", improperFormat.format(10l));
    }

// org.apache.commons.math.fraction.FractionFormatTest::testDoubleFormat
    public void testDoubleFormat() {
        assertEquals("355 / 113", improperFormat.format(Math.PI));
    }

// org.apache.commons.math.fraction.FractionTest::testConstructor
    public void testConstructor() {
        assertFraction(0, 1, new Fraction(0, 1));
        assertFraction(0, 1, new Fraction(0, 2));
        assertFraction(0, 1, new Fraction(0, -1));
        assertFraction(1, 2, new Fraction(1, 2));
        assertFraction(1, 2, new Fraction(2, 4));
        assertFraction(-1, 2, new Fraction(-1, 2));
        assertFraction(-1, 2, new Fraction(1, -2));
        assertFraction(-1, 2, new Fraction(-2, 4));
        assertFraction(-1, 2, new Fraction(2, -4));

        
        try {
            new Fraction(Integer.MIN_VALUE, -1);
            fail();
        } catch (ArithmeticException ex) {
            
        }
        try {
            new Fraction(1, Integer.MIN_VALUE);
            fail();
        } catch (ArithmeticException ex) {
            
        }
        try {
            assertFraction(0, 1, new Fraction(0.00000000000001));
            assertFraction(2, 5, new Fraction(0.40000000000001));
            assertFraction(15, 1, new Fraction(15.0000000000001));

        } catch (ConvergenceException ex) {
            fail(ex.getMessage());
        }
    }

// org.apache.commons.math.fraction.FractionTest::testGoldenRatio
    public void testGoldenRatio() {
        try {
            
            new Fraction((1 + Math.sqrt(5)) / 2, 1.0e-12, 25);
            fail("an exception should have been thrown");
        } catch (ConvergenceException ce) {
            
        } catch (Exception e) {
            fail("wrong exception caught");
        }
    }

// org.apache.commons.math.fraction.FractionTest::testDoubleConstructor
    public void testDoubleConstructor() throws ConvergenceException  {
        assertFraction(1, 2, new Fraction((double)1 / (double)2));
        assertFraction(1, 3, new Fraction((double)1 / (double)3));
        assertFraction(2, 3, new Fraction((double)2 / (double)3));
        assertFraction(1, 4, new Fraction((double)1 / (double)4));
        assertFraction(3, 4, new Fraction((double)3 / (double)4));
        assertFraction(1, 5, new Fraction((double)1 / (double)5));
        assertFraction(2, 5, new Fraction((double)2 / (double)5));
        assertFraction(3, 5, new Fraction((double)3 / (double)5));
        assertFraction(4, 5, new Fraction((double)4 / (double)5));
        assertFraction(1, 6, new Fraction((double)1 / (double)6));
        assertFraction(5, 6, new Fraction((double)5 / (double)6));
        assertFraction(1, 7, new Fraction((double)1 / (double)7));
        assertFraction(2, 7, new Fraction((double)2 / (double)7));
        assertFraction(3, 7, new Fraction((double)3 / (double)7));
        assertFraction(4, 7, new Fraction((double)4 / (double)7));
        assertFraction(5, 7, new Fraction((double)5 / (double)7));
        assertFraction(6, 7, new Fraction((double)6 / (double)7));
        assertFraction(1, 8, new Fraction((double)1 / (double)8));
        assertFraction(3, 8, new Fraction((double)3 / (double)8));
        assertFraction(5, 8, new Fraction((double)5 / (double)8));
        assertFraction(7, 8, new Fraction((double)7 / (double)8));
        assertFraction(1, 9, new Fraction((double)1 / (double)9));
        assertFraction(2, 9, new Fraction((double)2 / (double)9));
        assertFraction(4, 9, new Fraction((double)4 / (double)9));
        assertFraction(5, 9, new Fraction((double)5 / (double)9));
        assertFraction(7, 9, new Fraction((double)7 / (double)9));
        assertFraction(8, 9, new Fraction((double)8 / (double)9));
        assertFraction(1, 10, new Fraction((double)1 / (double)10));
        assertFraction(3, 10, new Fraction((double)3 / (double)10));
        assertFraction(7, 10, new Fraction((double)7 / (double)10));
        assertFraction(9, 10, new Fraction((double)9 / (double)10));
        assertFraction(1, 11, new Fraction((double)1 / (double)11));
        assertFraction(2, 11, new Fraction((double)2 / (double)11));
        assertFraction(3, 11, new Fraction((double)3 / (double)11));
        assertFraction(4, 11, new Fraction((double)4 / (double)11));
        assertFraction(5, 11, new Fraction((double)5 / (double)11));
        assertFraction(6, 11, new Fraction((double)6 / (double)11));
        assertFraction(7, 11, new Fraction((double)7 / (double)11));
        assertFraction(8, 11, new Fraction((double)8 / (double)11));
        assertFraction(9, 11, new Fraction((double)9 / (double)11));
        assertFraction(10, 11, new Fraction((double)10 / (double)11));
    }

// org.apache.commons.math.fraction.FractionTest::testDigitLimitConstructor
    public void testDigitLimitConstructor() throws ConvergenceException  {
        assertFraction(2, 5, new Fraction(0.4,   9));
        assertFraction(2, 5, new Fraction(0.4,  99));
        assertFraction(2, 5, new Fraction(0.4, 999));

        assertFraction(3, 5,      new Fraction(0.6152,    9));
        assertFraction(8, 13,     new Fraction(0.6152,   99));
        assertFraction(510, 829,  new Fraction(0.6152,  999));
        assertFraction(769, 1250, new Fraction(0.6152, 9999));
    }

// org.apache.commons.math.fraction.FractionTest::testIntegerOverflow
    public void testIntegerOverflow() {
        checkIntegerOverflow(0.75000000001455192);
        checkIntegerOverflow(1.0e10);
    }

// org.apache.commons.math.fraction.FractionTest::testEpsilonLimitConstructor
    public void testEpsilonLimitConstructor() throws ConvergenceException  {
        assertFraction(2, 5, new Fraction(0.4, 1.0e-5, 100));

        assertFraction(3, 5,      new Fraction(0.6152, 0.02, 100));
        assertFraction(8, 13,     new Fraction(0.6152, 1.0e-3, 100));
        assertFraction(251, 408,  new Fraction(0.6152, 1.0e-4, 100));
        assertFraction(251, 408,  new Fraction(0.6152, 1.0e-5, 100));
        assertFraction(510, 829,  new Fraction(0.6152, 1.0e-6, 100));
        assertFraction(769, 1250, new Fraction(0.6152, 1.0e-7, 100));
    }

// org.apache.commons.math.fraction.FractionTest::testCompareTo
    public void testCompareTo() {
        Fraction first = new Fraction(1, 2);
        Fraction second = new Fraction(1, 3);
        Fraction third = new Fraction(1, 2);

        assertEquals(0, first.compareTo(first));
        assertEquals(0, first.compareTo(third));
        assertEquals(1, first.compareTo(second));
        assertEquals(-1, second.compareTo(first));

        
        
        
        Fraction pi1 = new Fraction(1068966896, 340262731);
        Fraction pi2 = new Fraction( 411557987, 131002976);
        assertEquals(-1, pi1.compareTo(pi2));
        assertEquals( 1, pi2.compareTo(pi1));
        assertEquals(0.0, pi1.doubleValue() - pi2.doubleValue(), 1.0e-20);
    }

// org.apache.commons.math.fraction.FractionTest::testDoubleValue
    public void testDoubleValue() {
        Fraction first = new Fraction(1, 2);
        Fraction second = new Fraction(1, 3);

        assertEquals(0.5, first.doubleValue(), 0.0);
        assertEquals(1.0 / 3.0, second.doubleValue(), 0.0);
    }

// org.apache.commons.math.fraction.FractionTest::testFloatValue
    public void testFloatValue() {
        Fraction first = new Fraction(1, 2);
        Fraction second = new Fraction(1, 3);

        assertEquals(0.5f, first.floatValue(), 0.0f);
        assertEquals((float)(1.0 / 3.0), second.floatValue(), 0.0f);
    }

// org.apache.commons.math.fraction.FractionTest::testIntValue
    public void testIntValue() {
        Fraction first = new Fraction(1, 2);
        Fraction second = new Fraction(3, 2);

        assertEquals(0, first.intValue());
        assertEquals(1, second.intValue());
    }

// org.apache.commons.math.fraction.FractionTest::testLongValue
    public void testLongValue() {
        Fraction first = new Fraction(1, 2);
        Fraction second = new Fraction(3, 2);

        assertEquals(0L, first.longValue());
        assertEquals(1L, second.longValue());
    }

// org.apache.commons.math.fraction.FractionTest::testConstructorDouble
    public void testConstructorDouble() {
        try {
            assertFraction(1, 2, new Fraction(0.5));
            assertFraction(1, 3, new Fraction(1.0 / 3.0));
            assertFraction(17, 100, new Fraction(17.0 / 100.0));
            assertFraction(317, 100, new Fraction(317.0 / 100.0));
            assertFraction(-1, 2, new Fraction(-0.5));
            assertFraction(-1, 3, new Fraction(-1.0 / 3.0));
            assertFraction(-17, 100, new Fraction(17.0 / -100.0));
            assertFraction(-317, 100, new Fraction(-317.0 / 100.0));
        } catch (ConvergenceException ex) {
            fail(ex.getMessage());
        }
    }

// org.apache.commons.math.fraction.FractionTest::testAbs
    public void testAbs() {
        Fraction a = new Fraction(10, 21);
        Fraction b = new Fraction(-10, 21);
        Fraction c = new Fraction(10, -21);

        assertFraction(10, 21, a.abs());
        assertFraction(10, 21, b.abs());
        assertFraction(10, 21, c.abs());
    }

// org.apache.commons.math.fraction.FractionTest::testReciprocal
    public void testReciprocal() {
        Fraction f = null;

        f = new Fraction(50, 75);
        f = f.reciprocal();
        assertEquals(3, f.getNumerator());
        assertEquals(2, f.getDenominator());

        f = new Fraction(4, 3);
        f = f.reciprocal();
        assertEquals(3, f.getNumerator());
        assertEquals(4, f.getDenominator());

        f = new Fraction(-15, 47);
        f = f.reciprocal();
        assertEquals(-47, f.getNumerator());
        assertEquals(15, f.getDenominator());

        f = new Fraction(0, 3);
        try {
            f = f.reciprocal();
            fail("expecting ArithmeticException");
        } catch (ArithmeticException ex) {}

        
        f = new Fraction(Integer.MAX_VALUE, 1);
        f = f.reciprocal();
        assertEquals(1, f.getNumerator());
        assertEquals(Integer.MAX_VALUE, f.getDenominator());
    }

// org.apache.commons.math.fraction.FractionTest::testNegate
    public void testNegate() {
        Fraction f = null;

        f = new Fraction(50, 75);
        f = f.negate();
        assertEquals(-2, f.getNumerator());
        assertEquals(3, f.getDenominator());

        f = new Fraction(-50, 75);
        f = f.negate();
        assertEquals(2, f.getNumerator());
        assertEquals(3, f.getDenominator());

        
        f = new Fraction(Integer.MAX_VALUE-1, Integer.MAX_VALUE);
        f = f.negate();
        assertEquals(Integer.MIN_VALUE+2, f.getNumerator());
        assertEquals(Integer.MAX_VALUE, f.getDenominator());

        f = new Fraction(Integer.MIN_VALUE, 1);
        try {
            f = f.negate();
            fail("expecting ArithmeticException");
        } catch (ArithmeticException ex) {}
    }

// org.apache.commons.math.fraction.FractionTest::testAdd
    public void testAdd() {
        Fraction a = new Fraction(1, 2);
        Fraction b = new Fraction(2, 3);

        assertFraction(1, 1, a.add(a));
        assertFraction(7, 6, a.add(b));
        assertFraction(7, 6, b.add(a));
        assertFraction(4, 3, b.add(b));

        Fraction f1 = new Fraction(Integer.MAX_VALUE - 1, 1);
        Fraction f2 = Fraction.ONE;
        Fraction f = f1.add(f2);
        assertEquals(Integer.MAX_VALUE, f.getNumerator());
        assertEquals(1, f.getDenominator());
        f = f1.add(1);
        assertEquals(Integer.MAX_VALUE, f.getNumerator());
        assertEquals(1, f.getDenominator());

        f1 = new Fraction(-1, 13*13*2*2);
        f2 = new Fraction(-2, 13*17*2);
        f = f1.add(f2);
        assertEquals(13*13*17*2*2, f.getDenominator());
        assertEquals(-17 - 2*13*2, f.getNumerator());

        try {
            f.add(null);
            fail("expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {}

        
        
        f1 = new Fraction(1,32768*3);
        f2 = new Fraction(1,59049);
        f = f1.add(f2);
        assertEquals(52451, f.getNumerator());
        assertEquals(1934917632, f.getDenominator());

        f1 = new Fraction(Integer.MIN_VALUE, 3);
        f2 = new Fraction(1,3);
        f = f1.add(f2);
        assertEquals(Integer.MIN_VALUE+1, f.getNumerator());
        assertEquals(3, f.getDenominator());

        f1 = new Fraction(Integer.MAX_VALUE - 1, 1);
        f2 = Fraction.ONE;
        f = f1.add(f2);
        assertEquals(Integer.MAX_VALUE, f.getNumerator());
        assertEquals(1, f.getDenominator());

        try {
            f = f.add(Fraction.ONE); 
            fail("expecting ArithmeticException but got: " + f.toString());
        } catch (ArithmeticException ex) {}

        
        f1 = new Fraction(Integer.MIN_VALUE, 5);
        f2 = new Fraction(-1,5);
        try {
            f = f1.add(f2); 
            fail("expecting ArithmeticException but got: " + f.toString());
        } catch (ArithmeticException ex) {}

        try {
            f= new Fraction(-Integer.MAX_VALUE, 1);
            f = f.add(f);
            fail("expecting ArithmeticException");
        } catch (ArithmeticException ex) {}

        try {
            f= new Fraction(-Integer.MAX_VALUE, 1);
            f = f.add(f);
            fail("expecting ArithmeticException");
        } catch (ArithmeticException ex) {}

        f1 = new Fraction(3,327680);
        f2 = new Fraction(2,59049);
        try {
            f = f1.add(f2); 
            fail("expecting ArithmeticException but got: " + f.toString());
        } catch (ArithmeticException ex) {}
    }

// org.apache.commons.math.fraction.FractionTest::testDivide
    public void testDivide() {
        Fraction a = new Fraction(1, 2);
        Fraction b = new Fraction(2, 3);

        assertFraction(1, 1, a.divide(a));
        assertFraction(3, 4, a.divide(b));
        assertFraction(4, 3, b.divide(a));
        assertFraction(1, 1, b.divide(b));

        Fraction f1 = new Fraction(3, 5);
        Fraction f2 = Fraction.ZERO;
        try {
            f1.divide(f2);
            fail("expecting ArithmeticException");
        } catch (ArithmeticException ex) {}

        f1 = new Fraction(0, 5);
        f2 = new Fraction(2, 7);
        Fraction f = f1.divide(f2);
        assertSame(Fraction.ZERO, f);

        f1 = new Fraction(2, 7);
        f2 = Fraction.ONE;
        f = f1.divide(f2);
        assertEquals(2, f.getNumerator());
        assertEquals(7, f.getDenominator());

        f1 = new Fraction(1, Integer.MAX_VALUE);
        f = f1.divide(f1);
        assertEquals(1, f.getNumerator());
        assertEquals(1, f.getDenominator());

        f1 = new Fraction(Integer.MIN_VALUE, Integer.MAX_VALUE);
        f2 = new Fraction(1, Integer.MAX_VALUE);
        f = f1.divide(f2);
        assertEquals(Integer.MIN_VALUE, f.getNumerator());
        assertEquals(1, f.getDenominator());

        try {
            f.divide(null);
            fail("IllegalArgumentException");
        } catch (IllegalArgumentException ex) {}

        try {
            f1 = new Fraction(1, Integer.MAX_VALUE);
            f = f1.divide(f1.reciprocal());  
            fail("expecting ArithmeticException");
        } catch (ArithmeticException ex) {}
        try {
            f1 = new Fraction(1, -Integer.MAX_VALUE);
            f = f1.divide(f1.reciprocal());  
            fail("expecting ArithmeticException");
        } catch (ArithmeticException ex) {}

        f1 = new Fraction(6, 35);
        f  = f1.divide(15);
        assertEquals(2, f.getNumerator());
        assertEquals(175, f.getDenominator());

    }

// org.apache.commons.math.fraction.FractionTest::testMultiply
    public void testMultiply() {
        Fraction a = new Fraction(1, 2);
        Fraction b = new Fraction(2, 3);

        assertFraction(1, 4, a.multiply(a));
        assertFraction(1, 3, a.multiply(b));
        assertFraction(1, 3, b.multiply(a));
        assertFraction(4, 9, b.multiply(b));

        Fraction f1 = new Fraction(Integer.MAX_VALUE, 1);
        Fraction f2 = new Fraction(Integer.MIN_VALUE, Integer.MAX_VALUE);
        Fraction f = f1.multiply(f2);
        assertEquals(Integer.MIN_VALUE, f.getNumerator());
        assertEquals(1, f.getDenominator());

        try {
            f.multiply(null);
            fail("expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {}

        f1 = new Fraction(6, 35);
        f  = f1.multiply(15);
        assertEquals(18, f.getNumerator());
        assertEquals(7, f.getDenominator());
    }

// org.apache.commons.math.fraction.FractionTest::testSubtract
    public void testSubtract() {
        Fraction a = new Fraction(1, 2);
        Fraction b = new Fraction(2, 3);

        assertFraction(0, 1, a.subtract(a));
        assertFraction(-1, 6, a.subtract(b));
        assertFraction(1, 6, b.subtract(a));
        assertFraction(0, 1, b.subtract(b));

        Fraction f = new Fraction(1,1);
        try {
            f.subtract(null);
            fail("expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {}

        
        
        Fraction f1 = new Fraction(1,32768*3);
        Fraction f2 = new Fraction(1,59049);
        f = f1.subtract(f2);
        assertEquals(-13085, f.getNumerator());
        assertEquals(1934917632, f.getDenominator());

        f1 = new Fraction(Integer.MIN_VALUE, 3);
        f2 = new Fraction(1,3).negate();
        f = f1.subtract(f2);
        assertEquals(Integer.MIN_VALUE+1, f.getNumerator());
        assertEquals(3, f.getDenominator());

        f1 = new Fraction(Integer.MAX_VALUE, 1);
        f2 = Fraction.ONE;
        f = f1.subtract(f2);
        assertEquals(Integer.MAX_VALUE-1, f.getNumerator());
        assertEquals(1, f.getDenominator());
        f = f1.subtract(1);
        assertEquals(Integer.MAX_VALUE-1, f.getNumerator());
        assertEquals(1, f.getDenominator());

        try {
            f1 = new Fraction(1, Integer.MAX_VALUE);
            f2 = new Fraction(1, Integer.MAX_VALUE - 1);
            f = f1.subtract(f2);
            fail("expecting ArithmeticException");  
        } catch (ArithmeticException ex) {}

        
        f1 = new Fraction(Integer.MIN_VALUE, 5);
        f2 = new Fraction(1,5);
        try {
            f = f1.subtract(f2); 
            fail("expecting ArithmeticException but got: " + f.toString());
        } catch (ArithmeticException ex) {}

        try {
            f= new Fraction(Integer.MIN_VALUE, 1);
            f = f.subtract(Fraction.ONE);
            fail("expecting ArithmeticException");
        } catch (ArithmeticException ex) {}

        try {
            f= new Fraction(Integer.MAX_VALUE, 1);
            f = f.subtract(Fraction.ONE.negate());
            fail("expecting ArithmeticException");
        } catch (ArithmeticException ex) {}

        f1 = new Fraction(3,327680);
        f2 = new Fraction(2,59049);
        try {
            f = f1.subtract(f2); 
            fail("expecting ArithmeticException but got: " + f.toString());
        } catch (ArithmeticException ex) {}
    }

// org.apache.commons.math.fraction.FractionTest::testEqualsAndHashCode
    public void testEqualsAndHashCode() {
        Fraction zero  = new Fraction(0,1);
        Fraction nullFraction = null;
        assertTrue( zero.equals(zero));
        assertFalse(zero.equals(nullFraction));
        assertFalse(zero.equals(Double.valueOf(0)));
        Fraction zero2 = new Fraction(0,2);
        assertTrue(zero.equals(zero2));
        assertEquals(zero.hashCode(), zero2.hashCode());
        Fraction one = new Fraction(1,1);
        assertFalse((one.equals(zero) ||zero.equals(one)));
    }

// org.apache.commons.math.fraction.FractionTest::testGetReducedFraction
    public void testGetReducedFraction() {
        Fraction threeFourths = new Fraction(3, 4);
        assertTrue(threeFourths.equals(Fraction.getReducedFraction(6, 8)));
        assertTrue(Fraction.ZERO.equals(Fraction.getReducedFraction(0, -1)));
        try {
            Fraction.getReducedFraction(1, 0);
            fail("expecting ArithmeticException");
        } catch (ArithmeticException ex) {
            
        }
        assertEquals(Fraction.getReducedFraction
                (2, Integer.MIN_VALUE).getNumerator(),-1);
        assertEquals(Fraction.getReducedFraction
                (1, -1).getNumerator(), -1);
    }

// org.apache.commons.math.fraction.FractionTest::testToString
    public void testToString() {
        assertEquals("0", new Fraction(0, 3).toString());
        assertEquals("3", new Fraction(6, 2).toString());
        assertEquals("2 / 3", new Fraction(18, 27).toString());
    }

// org.apache.commons.math.fraction.FractionTest::testSerial
    public void testSerial() throws FractionConversionException {
        Fraction[] fractions = {
            new Fraction(3, 4), Fraction.ONE, Fraction.ZERO,
            new Fraction(17), new Fraction(Math.PI, 1000),
            new Fraction(-5, 2)
        };
        for (Fraction fraction : fractions) {
            assertEquals(fraction, TestUtils.serializeAndRecover(fraction));
        }
    }

// org.apache.commons.math.geometry.RotationTest::testIdentity
  public void testIdentity() {

    Rotation r = Rotation.IDENTITY;
    checkVector(r.applyTo(Vector3D.PLUS_I), Vector3D.PLUS_I);
    checkVector(r.applyTo(Vector3D.PLUS_J), Vector3D.PLUS_J);
    checkVector(r.applyTo(Vector3D.PLUS_K), Vector3D.PLUS_K);
    checkAngle(r.getAngle(), 0);

    r = new Rotation(-1, 0, 0, 0, false);
    checkVector(r.applyTo(Vector3D.PLUS_I), Vector3D.PLUS_I);
    checkVector(r.applyTo(Vector3D.PLUS_J), Vector3D.PLUS_J);
    checkVector(r.applyTo(Vector3D.PLUS_K), Vector3D.PLUS_K);
    checkAngle(r.getAngle(), 0);

    r = new Rotation(42, 0, 0, 0, true);
    checkVector(r.applyTo(Vector3D.PLUS_I), Vector3D.PLUS_I);
    checkVector(r.applyTo(Vector3D.PLUS_J), Vector3D.PLUS_J);
    checkVector(r.applyTo(Vector3D.PLUS_K), Vector3D.PLUS_K);
    checkAngle(r.getAngle(), 0);

  }

// org.apache.commons.math.geometry.RotationTest::testAxisAngle
  public void testAxisAngle() {

    Rotation r = new Rotation(new Vector3D(10, 10, 10), 2 * Math.PI / 3);
    checkVector(r.applyTo(Vector3D.PLUS_I), Vector3D.PLUS_J);
    checkVector(r.applyTo(Vector3D.PLUS_J), Vector3D.PLUS_K);
    checkVector(r.applyTo(Vector3D.PLUS_K), Vector3D.PLUS_I);
    double s = 1 / Math.sqrt(3);
    checkVector(r.getAxis(), new Vector3D(s, s, s));
    checkAngle(r.getAngle(), 2 * Math.PI / 3);

    try {
      new Rotation(new Vector3D(0, 0, 0), 2 * Math.PI / 3);
      fail("an exception should have been thrown");
    } catch (ArithmeticException e) {
    } catch (Exception e) {
      fail("unexpected exception");
    }

    r = new Rotation(Vector3D.PLUS_K, 1.5 * Math.PI);
    checkVector(r.getAxis(), new Vector3D(0, 0, -1));
    checkAngle(r.getAngle(), 0.5 * Math.PI);

    r = new Rotation(Vector3D.PLUS_J, Math.PI);
    checkVector(r.getAxis(), Vector3D.PLUS_J);
    checkAngle(r.getAngle(), Math.PI);

    checkVector(Rotation.IDENTITY.getAxis(), Vector3D.PLUS_I);

  }

// org.apache.commons.math.geometry.RotationTest::testRevert
  public void testRevert() {
    Rotation r = new Rotation(0.001, 0.36, 0.48, 0.8, true);
    Rotation reverted = r.revert();
    checkRotation(r.applyTo(reverted), 1, 0, 0, 0);
    checkRotation(reverted.applyTo(r), 1, 0, 0, 0);
    assertEquals(r.getAngle(), reverted.getAngle(), 1.0e-12);
    assertEquals(-1, Vector3D.dotProduct(r.getAxis(), reverted.getAxis()), 1.0e-12);
  }

// org.apache.commons.math.geometry.RotationTest::testVectorOnePair
  public void testVectorOnePair() {

    Vector3D u = new Vector3D(3, 2, 1);
    Vector3D v = new Vector3D(-4, 2, 2);
    Rotation r = new Rotation(u, v);
    checkVector(r.applyTo(u.scalarMultiply(v.getNorm())), v.scalarMultiply(u.getNorm()));

    checkAngle(new Rotation(u, u.negate()).getAngle(), Math.PI);

    try {
        new Rotation(u, Vector3D.ZERO);
        fail("an exception should have been thrown");
      } catch (IllegalArgumentException e) {
        
      } catch (Exception e) {
        fail("unexpected exception");
    }

  }

// org.apache.commons.math.geometry.RotationTest::testVectorTwoPairs
  public void testVectorTwoPairs() {

    Vector3D u1 = new Vector3D(3, 0, 0);
    Vector3D u2 = new Vector3D(0, 5, 0);
    Vector3D v1 = new Vector3D(0, 0, 2);
    Vector3D v2 = new Vector3D(-2, 0, 2);
    Rotation r = new Rotation(u1, u2, v1, v2);
    checkVector(r.applyTo(Vector3D.PLUS_I), Vector3D.PLUS_K);
    checkVector(r.applyTo(Vector3D.PLUS_J), Vector3D.MINUS_I);

    r = new Rotation(u1, u2, u1.negate(), u2.negate());
    Vector3D axis = r.getAxis();
    if (Vector3D.dotProduct(axis, Vector3D.PLUS_K) > 0) {
      checkVector(axis, Vector3D.PLUS_K);
    } else {
      checkVector(axis, Vector3D.MINUS_K);
    }
    checkAngle(r.getAngle(), Math.PI);

    double sqrt = Math.sqrt(2) / 2;
    r = new Rotation(Vector3D.PLUS_I,  Vector3D.PLUS_J,
                     new Vector3D(0.5, 0.5,  sqrt),
                     new Vector3D(0.5, 0.5, -sqrt));
    checkRotation(r, sqrt, 0.5, 0.5, 0);

    r = new Rotation(u1, u2, u1, Vector3D.crossProduct(u1, u2));
    checkRotation(r, sqrt, -sqrt, 0, 0);

    checkRotation(new Rotation(u1, u2, u1, u2), 1, 0, 0, 0);

    try {
        new Rotation(u1, u2, Vector3D.ZERO, v2);
        fail("an exception should have been thrown");
    } catch (IllegalArgumentException e) {
      
    } catch (Exception e) {
        fail("unexpected exception");
    }

  }

// org.apache.commons.math.geometry.RotationTest::testMatrix
  public void testMatrix()
    throws NotARotationMatrixException {

    try {
      new Rotation(new double[][] {
                     { 0.0, 1.0, 0.0 },
                     { 1.0, 0.0, 0.0 }
                   }, 1.0e-7);
    } catch (NotARotationMatrixException nrme) {
      
    } catch (Exception e) {
      fail("wrong exception caught: " + e.getMessage());
    }

    try {
      new Rotation(new double[][] {
                     {  0.445888,  0.797184, -0.407040 },
                     {  0.821760, -0.184320,  0.539200 },
                     { -0.354816,  0.574912,  0.737280 }
                   }, 1.0e-7);
    } catch (NotARotationMatrixException nrme) {
      
    } catch (Exception e) {
      fail("wrong exception caught: " + e.getMessage());
    }

    try {
        new Rotation(new double[][] {
                       {  0.4,  0.8, -0.4 },
                       { -0.4,  0.6,  0.7 },
                       {  0.8, -0.2,  0.5 }
                     }, 1.0e-15);
      } catch (NotARotationMatrixException nrme) {
        
      } catch (Exception e) {
        fail("wrong exception caught: " + e.getMessage());
      }

    checkRotation(new Rotation(new double[][] {
                                 {  0.445888,  0.797184, -0.407040 },
                                 { -0.354816,  0.574912,  0.737280 },
                                 {  0.821760, -0.184320,  0.539200 }
                               }, 1.0e-10),
                  0.8, 0.288, 0.384, 0.36);

    checkRotation(new Rotation(new double[][] {
                                 {  0.539200,  0.737280,  0.407040 },
                                 {  0.184320, -0.574912,  0.797184 },
                                 {  0.821760, -0.354816, -0.445888 }
                              }, 1.0e-10),
                  0.36, 0.8, 0.288, 0.384);

    checkRotation(new Rotation(new double[][] {
                                 { -0.445888,  0.797184, -0.407040 },
                                 {  0.354816,  0.574912,  0.737280 },
                                 {  0.821760,  0.184320, -0.539200 }
                               }, 1.0e-10),
                  0.384, 0.36, 0.8, 0.288);

    checkRotation(new Rotation(new double[][] {
                                 { -0.539200,  0.737280,  0.407040 },
                                 { -0.184320, -0.574912,  0.797184 },
                                 {  0.821760,  0.354816,  0.445888 }
                               }, 1.0e-10),
                  0.288, 0.384, 0.36, 0.8);

    double[][] m1 = { { 0.0, 1.0, 0.0 },
                      { 0.0, 0.0, 1.0 },
                      { 1.0, 0.0, 0.0 } };
    Rotation r = new Rotation(m1, 1.0e-7);
    checkVector(r.applyTo(Vector3D.PLUS_I), Vector3D.PLUS_K);
    checkVector(r.applyTo(Vector3D.PLUS_J), Vector3D.PLUS_I);
    checkVector(r.applyTo(Vector3D.PLUS_K), Vector3D.PLUS_J);

    double[][] m2 = { { 0.83203, -0.55012, -0.07139 },
                      { 0.48293,  0.78164, -0.39474 },
                      { 0.27296,  0.29396,  0.91602 } };
    r = new Rotation(m2, 1.0e-12);

    double[][] m3 = r.getMatrix();
    double d00 = m2[0][0] - m3[0][0];
    double d01 = m2[0][1] - m3[0][1];
    double d02 = m2[0][2] - m3[0][2];
    double d10 = m2[1][0] - m3[1][0];
    double d11 = m2[1][1] - m3[1][1];
    double d12 = m2[1][2] - m3[1][2];
    double d20 = m2[2][0] - m3[2][0];
    double d21 = m2[2][1] - m3[2][1];
    double d22 = m2[2][2] - m3[2][2];

    assertTrue(Math.abs(d00) < 6.0e-6);
    assertTrue(Math.abs(d01) < 6.0e-6);
    assertTrue(Math.abs(d02) < 6.0e-6);
    assertTrue(Math.abs(d10) < 6.0e-6);
    assertTrue(Math.abs(d11) < 6.0e-6);
    assertTrue(Math.abs(d12) < 6.0e-6);
    assertTrue(Math.abs(d20) < 6.0e-6);
    assertTrue(Math.abs(d21) < 6.0e-6);
    assertTrue(Math.abs(d22) < 6.0e-6);

    assertTrue(Math.abs(d00) > 4.0e-7);
    assertTrue(Math.abs(d01) > 4.0e-7);
    assertTrue(Math.abs(d02) > 4.0e-7);
    assertTrue(Math.abs(d10) > 4.0e-7);
    assertTrue(Math.abs(d11) > 4.0e-7);
    assertTrue(Math.abs(d12) > 4.0e-7);
    assertTrue(Math.abs(d20) > 4.0e-7);
    assertTrue(Math.abs(d21) > 4.0e-7);
    assertTrue(Math.abs(d22) > 4.0e-7);

    for (int i = 0; i < 3; ++i) {
      for (int j = 0; j < 3; ++j) {
        double m3tm3 = m3[i][0] * m3[j][0]
                     + m3[i][1] * m3[j][1]
                     + m3[i][2] * m3[j][2];
        if (i == j) {
          assertTrue(Math.abs(m3tm3 - 1.0) < 1.0e-10);
        } else {
          assertTrue(Math.abs(m3tm3) < 1.0e-10);
        }
      }
    }

    checkVector(r.applyTo(Vector3D.PLUS_I),
                new Vector3D(m3[0][0], m3[1][0], m3[2][0]));
    checkVector(r.applyTo(Vector3D.PLUS_J),
                new Vector3D(m3[0][1], m3[1][1], m3[2][1]));
    checkVector(r.applyTo(Vector3D.PLUS_K),
                new Vector3D(m3[0][2], m3[1][2], m3[2][2]));

    double[][] m4 = { { 1.0,  0.0,  0.0 },
                      { 0.0, -1.0,  0.0 },
                      { 0.0,  0.0, -1.0 } };
    r = new Rotation(m4, 1.0e-7);
    checkAngle(r.getAngle(), Math.PI);

    try {
      double[][] m5 = { { 0.0, 0.0, 1.0 },
                        { 0.0, 1.0, 0.0 },
                        { 1.0, 0.0, 0.0 } };
      r = new Rotation(m5, 1.0e-7);
      fail("got " + r + ", should have caught an exception");
    } catch (NotARotationMatrixException e) {
      
    } catch (Exception e) {
      fail("wrong exception caught");
    }

  }

// org.apache.commons.math.geometry.RotationTest::testAngles
  public void testAngles()
    throws CardanEulerSingularityException {

    RotationOrder[] CardanOrders = {
      RotationOrder.XYZ, RotationOrder.XZY, RotationOrder.YXZ,
      RotationOrder.YZX, RotationOrder.ZXY, RotationOrder.ZYX
    };

    for (int i = 0; i < CardanOrders.length; ++i) {
      for (double alpha1 = 0.1; alpha1 < 6.2; alpha1 += 0.3) {
        for (double alpha2 = -1.55; alpha2 < 1.55; alpha2 += 0.3) {
          for (double alpha3 = 0.1; alpha3 < 6.2; alpha3 += 0.3) {
            Rotation r = new Rotation(CardanOrders[i], alpha1, alpha2, alpha3);
            double[] angles = r.getAngles(CardanOrders[i]);
            checkAngle(angles[0], alpha1);
            checkAngle(angles[1], alpha2);
            checkAngle(angles[2], alpha3);
          }
        }
      }
    }

    RotationOrder[] EulerOrders = {
            RotationOrder.XYX, RotationOrder.XZX, RotationOrder.YXY,
            RotationOrder.YZY, RotationOrder.ZXZ, RotationOrder.ZYZ
          };

    for (int i = 0; i < EulerOrders.length; ++i) {
      for (double alpha1 = 0.1; alpha1 < 6.2; alpha1 += 0.3) {
        for (double alpha2 = 0.05; alpha2 < 3.1; alpha2 += 0.3) {
          for (double alpha3 = 0.1; alpha3 < 6.2; alpha3 += 0.3) {
            Rotation r = new Rotation(EulerOrders[i],
                                      alpha1, alpha2, alpha3);
            double[] angles = r.getAngles(EulerOrders[i]);
            checkAngle(angles[0], alpha1);
            checkAngle(angles[1], alpha2);
            checkAngle(angles[2], alpha3);
          }
        }
      }
    }

  }

// org.apache.commons.math.geometry.RotationTest::testSingularities
  public void testSingularities() {

    RotationOrder[] CardanOrders = {
      RotationOrder.XYZ, RotationOrder.XZY, RotationOrder.YXZ,
      RotationOrder.YZX, RotationOrder.ZXY, RotationOrder.ZYX
    };

    double[] singularCardanAngle = { Math.PI / 2, -Math.PI / 2 };
    for (int i = 0; i < CardanOrders.length; ++i) {
      for (int j = 0; j < singularCardanAngle.length; ++j) {
        Rotation r = new Rotation(CardanOrders[i], 0.1, singularCardanAngle[j], 0.3);
        try {
          r.getAngles(CardanOrders[i]);
          fail("an exception should have been caught");
        } catch (CardanEulerSingularityException cese) {
          
        } catch (Exception e) {
          fail("wrong exception caught: " + e.getMessage());
        }
      }
    }

    RotationOrder[] EulerOrders = {
            RotationOrder.XYX, RotationOrder.XZX, RotationOrder.YXY,
            RotationOrder.YZY, RotationOrder.ZXZ, RotationOrder.ZYZ
          };

    double[] singularEulerAngle = { 0, Math.PI };
    for (int i = 0; i < EulerOrders.length; ++i) {
      for (int j = 0; j < singularEulerAngle.length; ++j) {
        Rotation r = new Rotation(EulerOrders[i], 0.1, singularEulerAngle[j], 0.3);
        try {
          r.getAngles(EulerOrders[i]);
          fail("an exception should have been caught");
        } catch (CardanEulerSingularityException cese) {
          
        } catch (Exception e) {
          fail("wrong exception caught: " + e.getMessage());
        }
      }
    }

  }

// org.apache.commons.math.geometry.RotationTest::testQuaternion
  public void testQuaternion() {

    Rotation r1 = new Rotation(new Vector3D(2, -3, 5), 1.7);
    double n = 23.5;
    Rotation r2 = new Rotation(n * r1.getQ0(), n * r1.getQ1(),
                               n * r1.getQ2(), n * r1.getQ3(),
                               true);
    for (double x = -0.9; x < 0.9; x += 0.2) {
      for (double y = -0.9; y < 0.9; y += 0.2) {
        for (double z = -0.9; z < 0.9; z += 0.2) {
          Vector3D u = new Vector3D(x, y, z);
          checkVector(r2.applyTo(u), r1.applyTo(u));
        }
      }
    }

    r1 = new Rotation( 0.288,  0.384,  0.36,  0.8, false);
    checkRotation(r1, -r1.getQ0(), -r1.getQ1(), -r1.getQ2(), -r1.getQ3());

  }

// org.apache.commons.math.geometry.RotationTest::testCompose
  public void testCompose() {

    Rotation r1 = new Rotation(new Vector3D(2, -3, 5), 1.7);
    Rotation r2 = new Rotation(new Vector3D(-1, 3, 2), 0.3);
    Rotation r3 = r2.applyTo(r1);

    for (double x = -0.9; x < 0.9; x += 0.2) {
      for (double y = -0.9; y < 0.9; y += 0.2) {
        for (double z = -0.9; z < 0.9; z += 0.2) {
          Vector3D u = new Vector3D(x, y, z);
          checkVector(r2.applyTo(r1.applyTo(u)), r3.applyTo(u));
        }
      }
    }

  }

// org.apache.commons.math.geometry.RotationTest::testComposeInverse
  public void testComposeInverse() {

    Rotation r1 = new Rotation(new Vector3D(2, -3, 5), 1.7);
    Rotation r2 = new Rotation(new Vector3D(-1, 3, 2), 0.3);
    Rotation r3 = r2.applyInverseTo(r1);

    for (double x = -0.9; x < 0.9; x += 0.2) {
      for (double y = -0.9; y < 0.9; y += 0.2) {
        for (double z = -0.9; z < 0.9; z += 0.2) {
          Vector3D u = new Vector3D(x, y, z);
          checkVector(r2.applyInverseTo(r1.applyTo(u)), r3.applyTo(u));
        }
      }
    }

  }

// org.apache.commons.math.geometry.RotationTest::testApplyInverseTo
  public void testApplyInverseTo() {

    Rotation r = new Rotation(new Vector3D(2, -3, 5), 1.7);
    for (double lambda = 0; lambda < 6.2; lambda += 0.2) {
      for (double phi = -1.55; phi < 1.55; phi += 0.2) {
          Vector3D u = new Vector3D(Math.cos(lambda) * Math.cos(phi),
                                    Math.sin(lambda) * Math.cos(phi),
                                    Math.sin(phi));
          r.applyInverseTo(r.applyTo(u));
          checkVector(u, r.applyInverseTo(r.applyTo(u)));
          checkVector(u, r.applyTo(r.applyInverseTo(u)));
      }
    }

    r = Rotation.IDENTITY;
    for (double lambda = 0; lambda < 6.2; lambda += 0.2) {
      for (double phi = -1.55; phi < 1.55; phi += 0.2) {
          Vector3D u = new Vector3D(Math.cos(lambda) * Math.cos(phi),
                                    Math.sin(lambda) * Math.cos(phi),
                                    Math.sin(phi));
          checkVector(u, r.applyInverseTo(r.applyTo(u)));
          checkVector(u, r.applyTo(r.applyInverseTo(u)));
      }
    }

    r = new Rotation(Vector3D.PLUS_K, Math.PI);
    for (double lambda = 0; lambda < 6.2; lambda += 0.2) {
      for (double phi = -1.55; phi < 1.55; phi += 0.2) {
          Vector3D u = new Vector3D(Math.cos(lambda) * Math.cos(phi),
                                    Math.sin(lambda) * Math.cos(phi),
                                    Math.sin(phi));
          checkVector(u, r.applyInverseTo(r.applyTo(u)));
          checkVector(u, r.applyTo(r.applyInverseTo(u)));
      }
    }

  }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testDimensions
    public void testDimensions() {
        Array2DRowRealMatrix m = new Array2DRowRealMatrix(testData);
        Array2DRowRealMatrix m2 = new Array2DRowRealMatrix(testData2);
        assertEquals("testData row dimension",3,m.getRowDimension());
        assertEquals("testData column dimension",3,m.getColumnDimension());
        assertTrue("testData is square",m.isSquare());
        assertEquals("testData2 row dimension",m2.getRowDimension(),2);
        assertEquals("testData2 column dimension",m2.getColumnDimension(),3);
        assertTrue("testData2 is not square",!m2.isSquare());
    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testCopyFunctions
    public void testCopyFunctions() {
        Array2DRowRealMatrix m1 = new Array2DRowRealMatrix(testData);
        Array2DRowRealMatrix m2 = new Array2DRowRealMatrix(m1.getData());
        assertEquals(m2,m1);
        Array2DRowRealMatrix m3 = new Array2DRowRealMatrix(testData);
        Array2DRowRealMatrix m4 = new Array2DRowRealMatrix(m3.getData(), false);
        assertEquals(m4,m3);
    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testAdd
    public void testAdd() {
        Array2DRowRealMatrix m = new Array2DRowRealMatrix(testData);
        Array2DRowRealMatrix mInv = new Array2DRowRealMatrix(testDataInv);
        RealMatrix mPlusMInv = m.add(mInv);
        double[][] sumEntries = mPlusMInv.getData();
        for (int row = 0; row < m.getRowDimension(); row++) {
            for (int col = 0; col < m.getColumnDimension(); col++) {
                assertEquals("sum entry entry",
                    testDataPlusInv[row][col],sumEntries[row][col],
                        entryTolerance);
            }
        }
    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testAddFail
    public void testAddFail() {
        Array2DRowRealMatrix m = new Array2DRowRealMatrix(testData);
        Array2DRowRealMatrix m2 = new Array2DRowRealMatrix(testData2);
        try {
            m.add(m2);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testNorm
    public void testNorm() {
        Array2DRowRealMatrix m = new Array2DRowRealMatrix(testData);
        Array2DRowRealMatrix m2 = new Array2DRowRealMatrix(testData2);
        assertEquals("testData norm",14d,m.getNorm(),entryTolerance);
        assertEquals("testData2 norm",7d,m2.getNorm(),entryTolerance);
    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testFrobeniusNorm
    public void testFrobeniusNorm() {
        Array2DRowRealMatrix m = new Array2DRowRealMatrix(testData);
        Array2DRowRealMatrix m2 = new Array2DRowRealMatrix(testData2);
        assertEquals("testData Frobenius norm", Math.sqrt(117.0), m.getFrobeniusNorm(), entryTolerance);
        assertEquals("testData2 Frobenius norm", Math.sqrt(52.0), m2.getFrobeniusNorm(), entryTolerance);
    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testPlusMinus
    public void testPlusMinus() {
        Array2DRowRealMatrix m = new Array2DRowRealMatrix(testData);
        Array2DRowRealMatrix m2 = new Array2DRowRealMatrix(testDataInv);
        TestUtils.assertEquals("m-n = m + -n",m.subtract(m2),
            m2.scalarMultiply(-1d).add(m),entryTolerance);
        try {
            m.subtract(new Array2DRowRealMatrix(testData2));
            fail("Expecting illegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testMultiply
     public void testMultiply() {
        Array2DRowRealMatrix m = new Array2DRowRealMatrix(testData);
        Array2DRowRealMatrix mInv = new Array2DRowRealMatrix(testDataInv);
        Array2DRowRealMatrix identity = new Array2DRowRealMatrix(id);
        Array2DRowRealMatrix m2 = new Array2DRowRealMatrix(testData2);
        TestUtils.assertEquals("inverse multiply",m.multiply(mInv),
            identity,entryTolerance);
        TestUtils.assertEquals("inverse multiply",mInv.multiply(m),
            identity,entryTolerance);
        TestUtils.assertEquals("identity multiply",m.multiply(identity),
            m,entryTolerance);
        TestUtils.assertEquals("identity multiply",identity.multiply(mInv),
            mInv,entryTolerance);
        TestUtils.assertEquals("identity multiply",m2.multiply(identity),
            m2,entryTolerance);
        try {
            m.multiply(new Array2DRowRealMatrix(bigSingular));
            fail("Expecting illegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testMultiply2
    public void testMultiply2() {
       RealMatrix m3 = new Array2DRowRealMatrix(d3);
       RealMatrix m4 = new Array2DRowRealMatrix(d4);
       RealMatrix m5 = new Array2DRowRealMatrix(d5);
       TestUtils.assertEquals("m3*m4=m5", m3.multiply(m4), m5, entryTolerance);
   }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testTrace
    public void testTrace() {
        RealMatrix m = new Array2DRowRealMatrix(id);
        assertEquals("identity trace",3d,m.getTrace(),entryTolerance);
        m = new Array2DRowRealMatrix(testData2);
        try {
            m.getTrace();
            fail("Expecting NonSquareMatrixException");
        } catch (NonSquareMatrixException ex) {
            
        }
    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testScalarAdd
    public void testScalarAdd() {
        RealMatrix m = new Array2DRowRealMatrix(testData);
        TestUtils.assertEquals("scalar add",new Array2DRowRealMatrix(testDataPlus2),
            m.scalarAdd(2d),entryTolerance);
    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testOperate
    public void testOperate() {
        RealMatrix m = new Array2DRowRealMatrix(id);
        TestUtils.assertEquals("identity operate", testVector,
                    m.operate(testVector), entryTolerance);
        TestUtils.assertEquals("identity operate", testVector,
                    m.operate(new ArrayRealVector(testVector)).getData(), entryTolerance);
        m = new Array2DRowRealMatrix(bigSingular);
        try {
            m.operate(testVector);
            fail("Expecting illegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testMath209
    public void testMath209() {
        RealMatrix a = new Array2DRowRealMatrix(new double[][] {
                { 1, 2 }, { 3, 4 }, { 5, 6 }
        }, false);
        double[] b = a.operate(new double[] { 1, 1 });
        assertEquals(a.getRowDimension(), b.length);
        assertEquals( 3.0, b[0], 1.0e-12);
        assertEquals( 7.0, b[1], 1.0e-12);
        assertEquals(11.0, b[2], 1.0e-12);
    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testTranspose
    public void testTranspose() {
        RealMatrix m = new Array2DRowRealMatrix(testData);
        RealMatrix mIT = new LUDecompositionImpl(m).getSolver().getInverse().transpose();
        RealMatrix mTI = new LUDecompositionImpl(m.transpose()).getSolver().getInverse();
        TestUtils.assertEquals("inverse-transpose", mIT, mTI, normTolerance);
        m = new Array2DRowRealMatrix(testData2);
        RealMatrix mt = new Array2DRowRealMatrix(testData2T);
        TestUtils.assertEquals("transpose",mt,m.transpose(),normTolerance);
    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testPremultiplyVector
    public void testPremultiplyVector() {
        RealMatrix m = new Array2DRowRealMatrix(testData);
        TestUtils.assertEquals("premultiply", m.preMultiply(testVector),
                    preMultTest, normTolerance);
        TestUtils.assertEquals("premultiply", m.preMultiply(new ArrayRealVector(testVector).getData()),
                    preMultTest, normTolerance);
        m = new Array2DRowRealMatrix(bigSingular);
        try {
            m.preMultiply(testVector);
            fail("expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testPremultiply
    public void testPremultiply() {
        RealMatrix m3 = new Array2DRowRealMatrix(d3);
        RealMatrix m4 = new Array2DRowRealMatrix(d4);
        RealMatrix m5 = new Array2DRowRealMatrix(d5);
        TestUtils.assertEquals("m3*m4=m5", m4.preMultiply(m3), m5, entryTolerance);

        Array2DRowRealMatrix m = new Array2DRowRealMatrix(testData);
        Array2DRowRealMatrix mInv = new Array2DRowRealMatrix(testDataInv);
        Array2DRowRealMatrix identity = new Array2DRowRealMatrix(id);
        TestUtils.assertEquals("inverse multiply",m.preMultiply(mInv),
                identity,entryTolerance);
        TestUtils.assertEquals("inverse multiply",mInv.preMultiply(m),
                identity,entryTolerance);
        TestUtils.assertEquals("identity multiply",m.preMultiply(identity),
                m,entryTolerance);
        TestUtils.assertEquals("identity multiply",identity.preMultiply(mInv),
                mInv,entryTolerance);
        try {
            m.preMultiply(new Array2DRowRealMatrix(bigSingular));
            fail("Expecting illegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testGetVectors
    public void testGetVectors() {
        RealMatrix m = new Array2DRowRealMatrix(testData);
        TestUtils.assertEquals("get row",m.getRow(0),testDataRow1,entryTolerance);
        TestUtils.assertEquals("get col",m.getColumn(2),testDataCol3,entryTolerance);
        try {
            m.getRow(10);
            fail("expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.getColumn(-1);
            fail("expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testGetEntry
    public void testGetEntry() {
        RealMatrix m = new Array2DRowRealMatrix(testData);
        assertEquals("get entry",m.getEntry(0,1),2d,entryTolerance);
        try {
            m.getEntry(10, 4);
            fail ("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testExamples
    public void testExamples() {
        
        double[][] matrixData = { {1d,2d,3d}, {2d,5d,3d}};
        RealMatrix m = new Array2DRowRealMatrix(matrixData);
        
        double[][] matrixData2 = { {1d,2d}, {2d,5d}, {1d, 7d}};
        RealMatrix n = new Array2DRowRealMatrix(matrixData2);
        
        RealMatrix p = m.multiply(n);
        assertEquals(2, p.getRowDimension());
        assertEquals(2, p.getColumnDimension());
        
        RealMatrix pInverse = new LUDecompositionImpl(p).getSolver().getInverse();
        assertEquals(2, pInverse.getRowDimension());
        assertEquals(2, pInverse.getColumnDimension());

        
        double[][] coefficientsData = {{2, 3, -2}, {-1, 7, 6}, {4, -3, -5}};
        RealMatrix coefficients = new Array2DRowRealMatrix(coefficientsData);
        double[] constants = {1, -2, 1};
        double[] solution = new LUDecompositionImpl(coefficients).getSolver().solve(constants);
        assertEquals(2 * solution[0] + 3 * solution[1] -2 * solution[2], constants[0], 1E-12);
        assertEquals(-1 * solution[0] + 7 * solution[1] + 6 * solution[2], constants[1], 1E-12);
        assertEquals(4 * solution[0] - 3 * solution[1] -5 * solution[2], constants[2], 1E-12);

    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testGetSubMatrix
    public void testGetSubMatrix() {
        RealMatrix m = new Array2DRowRealMatrix(subTestData);
        checkGetSubMatrix(m, subRows23Cols00,  2 , 3 , 0, 0, false);
        checkGetSubMatrix(m, subRows00Cols33,  0 , 0 , 3, 3, false);
        checkGetSubMatrix(m, subRows01Cols23,  0 , 1 , 2, 3, false);
        checkGetSubMatrix(m, subRows02Cols13,  new int[] { 0, 2 }, new int[] { 1, 3 },    false);
        checkGetSubMatrix(m, subRows03Cols12,  new int[] { 0, 3 }, new int[] { 1, 2 },    false);
        checkGetSubMatrix(m, subRows03Cols123, new int[] { 0, 3 }, new int[] { 1, 2, 3 }, false);
        checkGetSubMatrix(m, subRows20Cols123, new int[] { 2, 0 }, new int[] { 1, 2, 3 }, false);
        checkGetSubMatrix(m, subRows31Cols31,  new int[] { 3, 1 }, new int[] { 3, 1 },    false);
        checkGetSubMatrix(m, subRows31Cols31,  new int[] { 3, 1 }, new int[] { 3, 1 },    false);
        checkGetSubMatrix(m, null,  1, 0, 2, 4, true);
        checkGetSubMatrix(m, null, -1, 1, 2, 2, true);
        checkGetSubMatrix(m, null,  1, 0, 2, 2, true);
        checkGetSubMatrix(m, null,  1, 0, 2, 4, true);
        checkGetSubMatrix(m, null, new int[] {},    new int[] { 0 }, true);
        checkGetSubMatrix(m, null, new int[] { 0 }, new int[] { 4 }, true);
    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testCopySubMatrix
    public void testCopySubMatrix() {
        RealMatrix m = new Array2DRowRealMatrix(subTestData);
        checkCopy(m, subRows23Cols00,  2 , 3 , 0, 0, false);
        checkCopy(m, subRows00Cols33,  0 , 0 , 3, 3, false);
        checkCopy(m, subRows01Cols23,  0 , 1 , 2, 3, false);
        checkCopy(m, subRows02Cols13,  new int[] { 0, 2 }, new int[] { 1, 3 },    false);
        checkCopy(m, subRows03Cols12,  new int[] { 0, 3 }, new int[] { 1, 2 },    false);
        checkCopy(m, subRows03Cols123, new int[] { 0, 3 }, new int[] { 1, 2, 3 }, false);
        checkCopy(m, subRows20Cols123, new int[] { 2, 0 }, new int[] { 1, 2, 3 }, false);
        checkCopy(m, subRows31Cols31,  new int[] { 3, 1 }, new int[] { 3, 1 },    false);
        checkCopy(m, subRows31Cols31,  new int[] { 3, 1 }, new int[] { 3, 1 },    false);

        checkCopy(m, null,  1, 0, 2, 4, true);
        checkCopy(m, null, -1, 1, 2, 2, true);
        checkCopy(m, null,  1, 0, 2, 2, true);
        checkCopy(m, null,  1, 0, 2, 4, true);
        checkCopy(m, null, new int[] {},    new int[] { 0 }, true);
        checkCopy(m, null, new int[] { 0 }, new int[] { 4 }, true);
    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testGetRowMatrix
    public void testGetRowMatrix() {
        RealMatrix m = new Array2DRowRealMatrix(subTestData);
        RealMatrix mRow0 = new Array2DRowRealMatrix(subRow0);
        RealMatrix mRow3 = new Array2DRowRealMatrix(subRow3);
        assertEquals("Row0", mRow0,
                m.getRowMatrix(0));
        assertEquals("Row3", mRow3,
                m.getRowMatrix(3));
        try {
            m.getRowMatrix(-1);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.getRowMatrix(4);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testSetRowMatrix
    public void testSetRowMatrix() {
        RealMatrix m = new Array2DRowRealMatrix(subTestData);
        RealMatrix mRow3 = new Array2DRowRealMatrix(subRow3);
        assertNotSame(mRow3, m.getRowMatrix(0));
        m.setRowMatrix(0, mRow3);
        assertEquals(mRow3, m.getRowMatrix(0));
        try {
            m.setRowMatrix(-1, mRow3);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.setRowMatrix(0, m);
            fail("Expecting InvalidMatrixException");
        } catch (InvalidMatrixException ex) {
            
        }
    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testGetColumnMatrix
    public void testGetColumnMatrix() {
        RealMatrix m = new Array2DRowRealMatrix(subTestData);
        RealMatrix mColumn1 = new Array2DRowRealMatrix(subColumn1);
        RealMatrix mColumn3 = new Array2DRowRealMatrix(subColumn3);
        assertEquals("Column1", mColumn1,
                m.getColumnMatrix(1));
        assertEquals("Column3", mColumn3,
                m.getColumnMatrix(3));
        try {
            m.getColumnMatrix(-1);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.getColumnMatrix(4);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testSetColumnMatrix
    public void testSetColumnMatrix() {
        RealMatrix m = new Array2DRowRealMatrix(subTestData);
        RealMatrix mColumn3 = new Array2DRowRealMatrix(subColumn3);
        assertNotSame(mColumn3, m.getColumnMatrix(1));
        m.setColumnMatrix(1, mColumn3);
        assertEquals(mColumn3, m.getColumnMatrix(1));
        try {
            m.setColumnMatrix(-1, mColumn3);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.setColumnMatrix(0, m);
            fail("Expecting InvalidMatrixException");
        } catch (InvalidMatrixException ex) {
            
        }
    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testGetRowVector
    public void testGetRowVector() {
        RealMatrix m = new Array2DRowRealMatrix(subTestData);
        RealVector mRow0 = new ArrayRealVector(subRow0[0]);
        RealVector mRow3 = new ArrayRealVector(subRow3[0]);
        assertEquals("Row0", mRow0, m.getRowVector(0));
        assertEquals("Row3", mRow3, m.getRowVector(3));
        try {
            m.getRowVector(-1);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.getRowVector(4);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testSetRowVector
    public void testSetRowVector() {
        RealMatrix m = new Array2DRowRealMatrix(subTestData);
        RealVector mRow3 = new ArrayRealVector(subRow3[0]);
        assertNotSame(mRow3, m.getRowMatrix(0));
        m.setRowVector(0, mRow3);
        assertEquals(mRow3, m.getRowVector(0));
        try {
            m.setRowVector(-1, mRow3);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.setRowVector(0, new ArrayRealVector(5));
            fail("Expecting InvalidMatrixException");
        } catch (InvalidMatrixException ex) {
            
        }
    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testGetColumnVector
    public void testGetColumnVector() {
        RealMatrix m = new Array2DRowRealMatrix(subTestData);
        RealVector mColumn1 = columnToVector(subColumn1);
        RealVector mColumn3 = columnToVector(subColumn3);
        assertEquals("Column1", mColumn1, m.getColumnVector(1));
        assertEquals("Column3", mColumn3, m.getColumnVector(3));
        try {
            m.getColumnVector(-1);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.getColumnVector(4);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testSetColumnVector
    public void testSetColumnVector() {
        RealMatrix m = new Array2DRowRealMatrix(subTestData);
        RealVector mColumn3 = columnToVector(subColumn3);
        assertNotSame(mColumn3, m.getColumnVector(1));
        m.setColumnVector(1, mColumn3);
        assertEquals(mColumn3, m.getColumnVector(1));
        try {
            m.setColumnVector(-1, mColumn3);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.setColumnVector(0, new ArrayRealVector(5));
            fail("Expecting InvalidMatrixException");
        } catch (InvalidMatrixException ex) {
            
        }
    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testGetRow
    public void testGetRow() {
        RealMatrix m = new Array2DRowRealMatrix(subTestData);
        checkArrays(subRow0[0], m.getRow(0));
        checkArrays(subRow3[0], m.getRow(3));
        try {
            m.getRow(-1);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.getRow(4);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testSetRow
    public void testSetRow() {
        RealMatrix m = new Array2DRowRealMatrix(subTestData);
        assertTrue(subRow3[0][0] != m.getRow(0)[0]);
        m.setRow(0, subRow3[0]);
        checkArrays(subRow3[0], m.getRow(0));
        try {
            m.setRow(-1, subRow3[0]);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.setRow(0, new double[5]);
            fail("Expecting InvalidMatrixException");
        } catch (InvalidMatrixException ex) {
            
        }
    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testGetColumn
    public void testGetColumn() {
        RealMatrix m = new Array2DRowRealMatrix(subTestData);
        double[] mColumn1 = columnToArray(subColumn1);
        double[] mColumn3 = columnToArray(subColumn3);
        checkArrays(mColumn1, m.getColumn(1));
        checkArrays(mColumn3, m.getColumn(3));
        try {
            m.getColumn(-1);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.getColumn(4);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testSetColumn
    public void testSetColumn() {
        RealMatrix m = new Array2DRowRealMatrix(subTestData);
        double[] mColumn3 = columnToArray(subColumn3);
        assertTrue(mColumn3[0] != m.getColumn(1)[0]);
        m.setColumn(1, mColumn3);
        checkArrays(mColumn3, m.getColumn(1));
        try {
            m.setColumn(-1, mColumn3);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.setColumn(0, new double[5]);
            fail("Expecting InvalidMatrixException");
        } catch (InvalidMatrixException ex) {
            
        }
    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testEqualsAndHashCode
    public void testEqualsAndHashCode() {
        Array2DRowRealMatrix m = new Array2DRowRealMatrix(testData);
        Array2DRowRealMatrix m1 = (Array2DRowRealMatrix) m.copy();
        Array2DRowRealMatrix mt = (Array2DRowRealMatrix) m.transpose();
        assertTrue(m.hashCode() != mt.hashCode());
        assertEquals(m.hashCode(), m1.hashCode());
        assertEquals(m, m);
        assertEquals(m, m1);
        assertFalse(m.equals(null));
        assertFalse(m.equals(mt));
        assertFalse(m.equals(new Array2DRowRealMatrix(bigSingular)));
    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testToString
    public void testToString() {
        Array2DRowRealMatrix m = new Array2DRowRealMatrix(testData);
        assertEquals("Array2DRowRealMatrix{{1.0,2.0,3.0},{2.0,5.0,3.0},{1.0,0.0,8.0}}",
                m.toString());
        m = new Array2DRowRealMatrix();
        assertEquals("Array2DRowRealMatrix{}",
                m.toString());
    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testSetSubMatrix
    public void testSetSubMatrix() throws Exception {
        Array2DRowRealMatrix m = new Array2DRowRealMatrix(testData);
        m.setSubMatrix(detData2,1,1);
        RealMatrix expected = MatrixUtils.createRealMatrix
            (new double[][] {{1.0,2.0,3.0},{2.0,1.0,3.0},{1.0,2.0,4.0}});
        assertEquals(expected, m);

        m.setSubMatrix(detData2,0,0);
        expected = MatrixUtils.createRealMatrix
            (new double[][] {{1.0,3.0,3.0},{2.0,4.0,3.0},{1.0,2.0,4.0}});
        assertEquals(expected, m);

        m.setSubMatrix(testDataPlus2,0,0);
        expected = MatrixUtils.createRealMatrix
            (new double[][] {{3.0,4.0,5.0},{4.0,7.0,5.0},{3.0,2.0,10.0}});
        assertEquals(expected, m);

        
        try {
            m.setSubMatrix(testData,1,1);
            fail("expecting MatrixIndexException");
        } catch (MatrixIndexException e) {
            
        }
        
        try {
            m.setSubMatrix(testData,-1,1);
            fail("expecting MatrixIndexException");
        } catch (MatrixIndexException e) {
            
        }
        try {
            m.setSubMatrix(testData,1,-1);
            fail("expecting MatrixIndexException");
        } catch (MatrixIndexException e) {
            
        }

        
        try {
            m.setSubMatrix(null,1,1);
            fail("expecting NullPointerException");
        } catch (NullPointerException e) {
            
        }
        Array2DRowRealMatrix m2 = new Array2DRowRealMatrix();
        try {
            m2.setSubMatrix(testData,0,1);
            fail("expecting IllegalStateException");
        } catch (IllegalStateException e) {
            
        }
        try {
            m2.setSubMatrix(testData,1,0);
            fail("expecting IllegalStateException");
        } catch (IllegalStateException e) {
            
        }

        
        try {
            m.setSubMatrix(new double[][] {{1}, {2, 3}}, 0, 0);
            fail("expecting IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            
        }

        
        try {
            m.setSubMatrix(new double[][] {{}}, 0, 0);
            fail("expecting IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            
        }

    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testWalk
    public void testWalk() {
        int rows    = 150;
        int columns = 75;

        RealMatrix m = new Array2DRowRealMatrix(rows, columns);
        m.walkInRowOrder(new SetVisitor());
        GetVisitor getVisitor = new GetVisitor();
        m.walkInOptimizedOrder(getVisitor);
        assertEquals(rows * columns, getVisitor.getCount());

        m = new Array2DRowRealMatrix(rows, columns);
        m.walkInRowOrder(new SetVisitor(), 1, rows - 2, 1, columns - 2);
        getVisitor = new GetVisitor();
        m.walkInOptimizedOrder(getVisitor, 1, rows - 2, 1, columns - 2);
        assertEquals((rows - 2) * (columns - 2), getVisitor.getCount());
        for (int i = 0; i < rows; ++i) {
            assertEquals(0.0, m.getEntry(i, 0), 0);
            assertEquals(0.0, m.getEntry(i, columns - 1), 0);
        }
        for (int j = 0; j < columns; ++j) {
            assertEquals(0.0, m.getEntry(0, j), 0);
            assertEquals(0.0, m.getEntry(rows - 1, j), 0);
        }

        m = new Array2DRowRealMatrix(rows, columns);
        m.walkInColumnOrder(new SetVisitor());
        getVisitor = new GetVisitor();
        m.walkInOptimizedOrder(getVisitor);
        assertEquals(rows * columns, getVisitor.getCount());

        m = new Array2DRowRealMatrix(rows, columns);
        m.walkInColumnOrder(new SetVisitor(), 1, rows - 2, 1, columns - 2);
        getVisitor = new GetVisitor();
        m.walkInOptimizedOrder(getVisitor, 1, rows - 2, 1, columns - 2);
        assertEquals((rows - 2) * (columns - 2), getVisitor.getCount());
        for (int i = 0; i < rows; ++i) {
            assertEquals(0.0, m.getEntry(i, 0), 0);
            assertEquals(0.0, m.getEntry(i, columns - 1), 0);
        }
        for (int j = 0; j < columns; ++j) {
            assertEquals(0.0, m.getEntry(0, j), 0);
            assertEquals(0.0, m.getEntry(rows - 1, j), 0);
        }

        m = new Array2DRowRealMatrix(rows, columns);
        m.walkInOptimizedOrder(new SetVisitor());
        getVisitor = new GetVisitor();
        m.walkInRowOrder(getVisitor);
        assertEquals(rows * columns, getVisitor.getCount());

        m = new Array2DRowRealMatrix(rows, columns);
        m.walkInOptimizedOrder(new SetVisitor(), 1, rows - 2, 1, columns - 2);
        getVisitor = new GetVisitor();
        m.walkInRowOrder(getVisitor, 1, rows - 2, 1, columns - 2);
        assertEquals((rows - 2) * (columns - 2), getVisitor.getCount());
        for (int i = 0; i < rows; ++i) {
            assertEquals(0.0, m.getEntry(i, 0), 0);
            assertEquals(0.0, m.getEntry(i, columns - 1), 0);
        }
        for (int j = 0; j < columns; ++j) {
            assertEquals(0.0, m.getEntry(0, j), 0);
            assertEquals(0.0, m.getEntry(rows - 1, j), 0);
        }

        m = new Array2DRowRealMatrix(rows, columns);
        m.walkInOptimizedOrder(new SetVisitor());
        getVisitor = new GetVisitor();
        m.walkInColumnOrder(getVisitor);
        assertEquals(rows * columns, getVisitor.getCount());

        m = new Array2DRowRealMatrix(rows, columns);
        m.walkInOptimizedOrder(new SetVisitor(), 1, rows - 2, 1, columns - 2);
        getVisitor = new GetVisitor();
        m.walkInColumnOrder(getVisitor, 1, rows - 2, 1, columns - 2);
        assertEquals((rows - 2) * (columns - 2), getVisitor.getCount());
        for (int i = 0; i < rows; ++i) {
            assertEquals(0.0, m.getEntry(i, 0), 0);
            assertEquals(0.0, m.getEntry(i, columns - 1), 0);
        }
        for (int j = 0; j < columns; ++j) {
            assertEquals(0.0, m.getEntry(0, j), 0);
            assertEquals(0.0, m.getEntry(rows - 1, j), 0);
        }

    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testSerial
    public void testSerial()  {
        Array2DRowRealMatrix m = new Array2DRowRealMatrix(testData);
        assertEquals(m,TestUtils.serializeAndRecover(m));
    }

// org.apache.commons.math.linear.ArrayFieldVectorTest::testConstructors
    public void testConstructors() {

        ArrayFieldVector<Fraction> v0 = new ArrayFieldVector<Fraction>(FractionField.getInstance());
        assertEquals(0, v0.getDimension());

        ArrayFieldVector<Fraction> v1 = new ArrayFieldVector<Fraction>(FractionField.getInstance(), 7);
        assertEquals(7, v1.getDimension());
        assertEquals(new Fraction(0), v1.getEntry(6));

        ArrayFieldVector<Fraction> v2 = new ArrayFieldVector<Fraction>(5, new Fraction(123, 100));
        assertEquals(5, v2.getDimension());
        assertEquals(new Fraction(123, 100), v2.getEntry(4));

        ArrayFieldVector<Fraction> v3 = new ArrayFieldVector<Fraction>(vec1);
        assertEquals(3, v3.getDimension());
        assertEquals(new Fraction(2), v3.getEntry(1));

        ArrayFieldVector<Fraction> v4 = new ArrayFieldVector<Fraction>(vec4, 3, 2);
        assertEquals(2, v4.getDimension());
        assertEquals(new Fraction(4), v4.getEntry(0));
        try {
            new ArrayFieldVector<Fraction>(vec4, 8, 3);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ex) {
            
        } catch (Exception e) {
            fail("wrong exception caught");
        }

        FieldVector<Fraction> v5_i = new ArrayFieldVector<Fraction>(dvec1);
        assertEquals(9, v5_i.getDimension());
        assertEquals(new Fraction(9), v5_i.getEntry(8));

        ArrayFieldVector<Fraction> v5 = new ArrayFieldVector<Fraction>(dvec1);
        assertEquals(9, v5.getDimension());
        assertEquals(new Fraction(9), v5.getEntry(8));

        ArrayFieldVector<Fraction> v6 = new ArrayFieldVector<Fraction>(dvec1, 3, 2);
        assertEquals(2, v6.getDimension());
        assertEquals(new Fraction(4), v6.getEntry(0));
        try {
            new ArrayFieldVector<Fraction>(dvec1, 8, 3);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ex) {
            
        } catch (Exception e) {
            fail("wrong exception caught");
        }

        ArrayFieldVector<Fraction> v7 = new ArrayFieldVector<Fraction>(v1);
        assertEquals(7, v7.getDimension());
        assertEquals(new Fraction(0), v7.getEntry(6));

        FieldVectorTestImpl<Fraction> v7_i = new FieldVectorTestImpl<Fraction>(vec1);

        ArrayFieldVector<Fraction> v7_2 = new ArrayFieldVector<Fraction>(v7_i);
        assertEquals(3, v7_2.getDimension());
        assertEquals(new Fraction(2), v7_2.getEntry(1));

        ArrayFieldVector<Fraction> v8 = new ArrayFieldVector<Fraction>(v1, true);
        assertEquals(7, v8.getDimension());
        assertEquals(new Fraction(0), v8.getEntry(6));
        assertNotSame("testData not same object ", v1.data, v8.data);

        ArrayFieldVector<Fraction> v8_2 = new ArrayFieldVector<Fraction>(v1, false);
        assertEquals(7, v8_2.getDimension());
        assertEquals(new Fraction(0), v8_2.getEntry(6));
        assertEquals(v1.data, v8_2.data);

        ArrayFieldVector<Fraction> v9 = new ArrayFieldVector<Fraction>(v1, v3);
        assertEquals(10, v9.getDimension());
        assertEquals(new Fraction(1), v9.getEntry(7));

    }

// org.apache.commons.math.linear.ArrayFieldVectorTest::testDataInOut
    public void testDataInOut() {

        ArrayFieldVector<Fraction> v1 = new ArrayFieldVector<Fraction>(vec1);
        ArrayFieldVector<Fraction> v2 = new ArrayFieldVector<Fraction>(vec2);
        ArrayFieldVector<Fraction> v4 = new ArrayFieldVector<Fraction>(vec4);
        FieldVectorTestImpl<Fraction> v2_t = new FieldVectorTestImpl<Fraction>(vec2);

        FieldVector<Fraction> v_append_1 = v1.append(v2);
        assertEquals(6, v_append_1.getDimension());
        assertEquals(new Fraction(4), v_append_1.getEntry(3));

        FieldVector<Fraction> v_append_2 = v1.append(new Fraction(2));
        assertEquals(4, v_append_2.getDimension());
        assertEquals(new Fraction(2), v_append_2.getEntry(3));

        FieldVector<Fraction> v_append_3 = v1.append(vec2);
        assertEquals(6, v_append_3.getDimension());
        assertEquals(new Fraction(4), v_append_3.getEntry(3));

        FieldVector<Fraction> v_append_4 = v1.append(v2_t);
        assertEquals(6, v_append_4.getDimension());
        assertEquals(new Fraction(4), v_append_4.getEntry(3));

        FieldVector<Fraction> v_copy = v1.copy();
        assertEquals(3, v_copy.getDimension());
        assertNotSame("testData not same object ", v1.data, v_copy.getData());

        Fraction[] a_frac = v1.toArray();
        assertEquals(3, a_frac.length);
        assertNotSame("testData not same object ", v1.data, a_frac);

        FieldVector<Fraction> vout5 = v4.getSubVector(3, 3);
        assertEquals(3, vout5.getDimension());
        assertEquals(new Fraction(5), vout5.getEntry(1));
        try {
            v4.getSubVector(3, 7);
            fail("MatrixIndexException expected");
        } catch (MatrixIndexException ex) {
            
        } catch (Exception e) {
            fail("wrong exception caught");
        }

        ArrayFieldVector<Fraction> v_set1 = (ArrayFieldVector<Fraction>) v1.copy();
        v_set1.setEntry(1, new Fraction(11));
        assertEquals(new Fraction(11), v_set1.getEntry(1));
        try {
            v_set1.setEntry(3, new Fraction(11));
            fail("MatrixIndexException expected");
        } catch (MatrixIndexException ex) {
            
        } catch (Exception e) {
            fail("wrong exception caught");
        }

        ArrayFieldVector<Fraction> v_set2 = (ArrayFieldVector<Fraction>) v4.copy();
        v_set2.set(3, v1);
        assertEquals(new Fraction(1), v_set2.getEntry(3));
        assertEquals(new Fraction(7), v_set2.getEntry(6));
        try {
            v_set2.set(7, v1);
            fail("MatrixIndexException expected");
        } catch (MatrixIndexException ex) {
            
        } catch (Exception e) {
            fail("wrong exception caught");
        }

        ArrayFieldVector<Fraction> v_set3 = (ArrayFieldVector<Fraction>) v1.copy();
        v_set3.set(new Fraction(13));
        assertEquals(new Fraction(13), v_set3.getEntry(2));

        try {
            v_set3.getEntry(23);
            fail("ArrayIndexOutOfBoundsException expected");
        } catch (ArrayIndexOutOfBoundsException ex) {
            
        } catch (Exception e) {
            fail("wrong exception caught");
        }

        ArrayFieldVector<Fraction> v_set4 = (ArrayFieldVector<Fraction>) v4.copy();
        v_set4.setSubVector(3, v2_t);
        assertEquals(new Fraction(4), v_set4.getEntry(3));
        assertEquals(new Fraction(7), v_set4.getEntry(6));
        try {
            v_set4.setSubVector(7, v2_t);
            fail("MatrixIndexException expected");
        } catch (MatrixIndexException ex) {
            
        } catch (Exception e) {
            fail("wrong exception caught");
        }

        ArrayFieldVector<Fraction> vout10 = (ArrayFieldVector<Fraction>) v1.copy();
        ArrayFieldVector<Fraction> vout10_2 = (ArrayFieldVector<Fraction>) v1.copy();
        assertEquals(vout10, vout10_2);
        vout10_2.setEntry(0, new Fraction(11, 10));
        assertNotSame(vout10, vout10_2);

    }

// org.apache.commons.math.linear.ArrayFieldVectorTest::testMapFunctions
    public void testMapFunctions() {
        ArrayFieldVector<Fraction> v1 = new ArrayFieldVector<Fraction>(vec1);

        
        FieldVector<Fraction> v_mapAdd = v1.mapAdd(new Fraction(2));
        Fraction[] result_mapAdd = {new Fraction(3), new Fraction(4), new Fraction(5)};
        checkArray("compare vectors" ,result_mapAdd,v_mapAdd.getData());

        
        FieldVector<Fraction> v_mapAddToSelf = v1.copy();
        v_mapAddToSelf.mapAddToSelf(new Fraction(2));
        Fraction[] result_mapAddToSelf = {new Fraction(3), new Fraction(4), new Fraction(5)};
        checkArray("compare vectors" ,result_mapAddToSelf,v_mapAddToSelf.getData());

        
        FieldVector<Fraction> v_mapSubtract = v1.mapSubtract(new Fraction(2));
        Fraction[] result_mapSubtract = {new Fraction(-1), new Fraction(0), new Fraction(1)};
        checkArray("compare vectors" ,result_mapSubtract,v_mapSubtract.getData());

        
        FieldVector<Fraction> v_mapSubtractToSelf = v1.copy();
        v_mapSubtractToSelf.mapSubtractToSelf(new Fraction(2));
        Fraction[] result_mapSubtractToSelf = {new Fraction(-1), new Fraction(0), new Fraction(1)};
        checkArray("compare vectors" ,result_mapSubtractToSelf,v_mapSubtractToSelf.getData());

        
        FieldVector<Fraction> v_mapMultiply = v1.mapMultiply(new Fraction(2));
        Fraction[] result_mapMultiply = {new Fraction(2), new Fraction(4), new Fraction(6)};
        checkArray("compare vectors" ,result_mapMultiply,v_mapMultiply.getData());

        
        FieldVector<Fraction> v_mapMultiplyToSelf = v1.copy();
        v_mapMultiplyToSelf.mapMultiplyToSelf(new Fraction(2));
        Fraction[] result_mapMultiplyToSelf = {new Fraction(2), new Fraction(4), new Fraction(6)};
        checkArray("compare vectors" ,result_mapMultiplyToSelf,v_mapMultiplyToSelf.getData());

        
        FieldVector<Fraction> v_mapDivide = v1.mapDivide(new Fraction(2));
        Fraction[] result_mapDivide = {new Fraction(1, 2), new Fraction(1), new Fraction(3, 2)};
        checkArray("compare vectors" ,result_mapDivide,v_mapDivide.getData());

        
        FieldVector<Fraction> v_mapDivideToSelf = v1.copy();
        v_mapDivideToSelf.mapDivideToSelf(new Fraction(2));
        Fraction[] result_mapDivideToSelf = {new Fraction(1, 2), new Fraction(1), new Fraction(3, 2)};
        checkArray("compare vectors" ,result_mapDivideToSelf,v_mapDivideToSelf.getData());

        
        FieldVector<Fraction> v_mapInv = v1.mapInv();
        Fraction[] result_mapInv = {new Fraction(1),new Fraction(1, 2),new Fraction(1, 3)};
        checkArray("compare vectors" ,result_mapInv,v_mapInv.getData());

        
        FieldVector<Fraction> v_mapInvToSelf = v1.copy();
        v_mapInvToSelf.mapInvToSelf();
        Fraction[] result_mapInvToSelf = {new Fraction(1),new Fraction(1, 2),new Fraction(1, 3)};
        checkArray("compare vectors" ,result_mapInvToSelf,v_mapInvToSelf.getData());

    }

// org.apache.commons.math.linear.ArrayFieldVectorTest::testBasicFunctions
    public void testBasicFunctions() {
        ArrayFieldVector<Fraction> v1 = new ArrayFieldVector<Fraction>(vec1);
        ArrayFieldVector<Fraction> v2 = new ArrayFieldVector<Fraction>(vec2);
        new ArrayFieldVector<Fraction>(vec_null);

        FieldVectorTestImpl<Fraction> v2_t = new FieldVectorTestImpl<Fraction>(vec2);

        
        ArrayFieldVector<Fraction> v_add = v1.add(v2);
        Fraction[] result_add = {new Fraction(5), new Fraction(7), new Fraction(9)};
        checkArray("compare vect" ,v_add.getData(),result_add);

        FieldVectorTestImpl<Fraction> vt2 = new FieldVectorTestImpl<Fraction>(vec2);
        FieldVector<Fraction> v_add_i = v1.add(vt2);
        Fraction[] result_add_i = {new Fraction(5), new Fraction(7), new Fraction(9)};
        checkArray("compare vect" ,v_add_i.getData(),result_add_i);

        
        ArrayFieldVector<Fraction> v_subtract = v1.subtract(v2);
        Fraction[] result_subtract = {new Fraction(-3), new Fraction(-3), new Fraction(-3)};
        checkArray("compare vect" ,v_subtract.getData(),result_subtract);

        FieldVector<Fraction> v_subtract_i = v1.subtract(vt2);
        Fraction[] result_subtract_i = {new Fraction(-3), new Fraction(-3), new Fraction(-3)};
        checkArray("compare vect" ,v_subtract_i.getData(),result_subtract_i);

        
        ArrayFieldVector<Fraction>  v_ebeMultiply = v1.ebeMultiply(v2);
        Fraction[] result_ebeMultiply = {new Fraction(4), new Fraction(10), new Fraction(18)};
        checkArray("compare vect" ,v_ebeMultiply.getData(),result_ebeMultiply);

        FieldVector<Fraction>  v_ebeMultiply_2 = v1.ebeMultiply(v2_t);
        Fraction[] result_ebeMultiply_2 = {new Fraction(4), new Fraction(10), new Fraction(18)};
        checkArray("compare vect" ,v_ebeMultiply_2.getData(),result_ebeMultiply_2);

        
        ArrayFieldVector<Fraction>  v_ebeDivide = v1.ebeDivide(v2);
        Fraction[] result_ebeDivide = {new Fraction(1, 4), new Fraction(2, 5), new Fraction(1, 2)};
        checkArray("compare vect" ,v_ebeDivide.getData(),result_ebeDivide);

        FieldVector<Fraction>  v_ebeDivide_2 = v1.ebeDivide(v2_t);
        Fraction[] result_ebeDivide_2 = {new Fraction(1, 4), new Fraction(2, 5), new Fraction(1, 2)};
        checkArray("compare vect" ,v_ebeDivide_2.getData(),result_ebeDivide_2);

        
        Fraction dot =  v1.dotProduct(v2);
        assertEquals("compare val ",new Fraction(32), dot);

        
        Fraction dot_2 =  v1.dotProduct(v2_t);
        assertEquals("compare val ",new Fraction(32), dot_2);

        FieldMatrix<Fraction> m_outerProduct = v1.outerProduct(v2);
        assertEquals("compare val ",new Fraction(4), m_outerProduct.getEntry(0,0));

        FieldMatrix<Fraction> m_outerProduct_2 = v1.outerProduct(v2_t);
        assertEquals("compare val ",new Fraction(4), m_outerProduct_2.getEntry(0,0));

        ArrayFieldVector<Fraction> v_projection = v1.projection(v2);
        Fraction[] result_projection = {new Fraction(128, 77), new Fraction(160, 77), new Fraction(192, 77)};
        checkArray("compare vect", v_projection.getData(), result_projection);

        FieldVector<Fraction> v_projection_2 = v1.projection(v2_t);
        Fraction[] result_projection_2 = {new Fraction(128, 77), new Fraction(160, 77), new Fraction(192, 77)};
        checkArray("compare vect", v_projection_2.getData(), result_projection_2);

    }

// org.apache.commons.math.linear.ArrayFieldVectorTest::testMisc
    public void testMisc() {
        ArrayFieldVector<Fraction> v1 = new ArrayFieldVector<Fraction>(vec1);
        ArrayFieldVector<Fraction> v4 = new ArrayFieldVector<Fraction>(vec4);
        FieldVector<Fraction> v4_2 = new ArrayFieldVector<Fraction>(vec4);

        String out1 = v1.toString();
        assertTrue("some output ",  out1.length()!=0);
        
        try {
            v1.checkVectorDimensions(2);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ex) {
            
        } catch (Exception e) {
            fail("wrong exception caught");
        }

       try {
            v1.checkVectorDimensions(v4);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ex) {
            
        } catch (Exception e) {
            fail("wrong exception caught");
        }

        try {
            v1.checkVectorDimensions(v4_2);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ex) {
            
        } catch (Exception e) {
            fail("wrong exception caught");
        }

    }

// org.apache.commons.math.linear.ArrayFieldVectorTest::testSerial
    public void testSerial()  {
        ArrayFieldVector<Fraction> v = new ArrayFieldVector<Fraction>(vec1);
        assertEquals(v,TestUtils.serializeAndRecover(v));
    }

// org.apache.commons.math.linear.ArrayRealVectorTest::testConstructors
    public void testConstructors() {

        ArrayRealVector v0 = new ArrayRealVector();
        assertEquals("testData len", 0, v0.getDimension());

        ArrayRealVector v1 = new ArrayRealVector(7);
        assertEquals("testData len", 7, v1.getDimension());
        assertEquals("testData is 0.0 ", 0.0, v1.getEntry(6));

        ArrayRealVector v2 = new ArrayRealVector(5, 1.23);
        assertEquals("testData len", 5, v2.getDimension());
        assertEquals("testData is 1.23 ", 1.23, v2.getEntry(4));

        ArrayRealVector v3 = new ArrayRealVector(vec1);
        assertEquals("testData len", 3, v3.getDimension());
        assertEquals("testData is 2.0 ", 2.0, v3.getEntry(1));

        ArrayRealVector v4 = new ArrayRealVector(vec4, 3, 2);
        assertEquals("testData len", 2, v4.getDimension());
        assertEquals("testData is 4.0 ", 4.0, v4.getEntry(0));
        try {
            new ArrayRealVector(vec4, 8, 3);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ex) {
            
        } catch (Exception e) {
            fail("wrong exception caught");
        }

        RealVector v5_i = new ArrayRealVector(dvec1);
        assertEquals("testData len", 9, v5_i.getDimension());
        assertEquals("testData is 9.0 ", 9.0, v5_i.getEntry(8));

        ArrayRealVector v5 = new ArrayRealVector(dvec1);
        assertEquals("testData len", 9, v5.getDimension());
        assertEquals("testData is 9.0 ", 9.0, v5.getEntry(8));

        ArrayRealVector v6 = new ArrayRealVector(dvec1, 3, 2);
        assertEquals("testData len", 2, v6.getDimension());
        assertEquals("testData is 4.0 ", 4.0, v6.getEntry(0));
        try {
            new ArrayRealVector(dvec1, 8, 3);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ex) {
            
        } catch (Exception e) {
            fail("wrong exception caught");
        }

        ArrayRealVector v7 = new ArrayRealVector(v1);
        assertEquals("testData len", 7, v7.getDimension());
        assertEquals("testData is 0.0 ", 0.0, v7.getEntry(6));

        RealVectorTestImpl v7_i = new RealVectorTestImpl(vec1);

        ArrayRealVector v7_2 = new ArrayRealVector(v7_i);
        assertEquals("testData len", 3, v7_2.getDimension());
        assertEquals("testData is 0.0 ", 2.0d, v7_2.getEntry(1));

        ArrayRealVector v8 = new ArrayRealVector(v1, true);
        assertEquals("testData len", 7, v8.getDimension());
        assertEquals("testData is 0.0 ", 0.0, v8.getEntry(6));
        assertNotSame("testData not same object ", v1.data, v8.data);

        ArrayRealVector v8_2 = new ArrayRealVector(v1, false);
        assertEquals("testData len", 7, v8_2.getDimension());
        assertEquals("testData is 0.0 ", 0.0, v8_2.getEntry(6));
        assertEquals("testData same object ", v1.data, v8_2.data);

        ArrayRealVector v9 = new ArrayRealVector(v1, v3);
        assertEquals("testData len", 10, v9.getDimension());
        assertEquals("testData is 1.0 ", 1.0, v9.getEntry(7));

    }

// org.apache.commons.math.linear.ArrayRealVectorTest::testDataInOut
    public void testDataInOut() {

        ArrayRealVector v1 = new ArrayRealVector(vec1);
        ArrayRealVector v2 = new ArrayRealVector(vec2);
        ArrayRealVector v4 = new ArrayRealVector(vec4);
        RealVectorTestImpl v2_t = new RealVectorTestImpl(vec2);

        RealVector v_append_1 = v1.append(v2);
        assertEquals("testData len", 6, v_append_1.getDimension());
        assertEquals("testData is 4.0 ", 4.0, v_append_1.getEntry(3));

        RealVector v_append_2 = v1.append(2.0);
        assertEquals("testData len", 4, v_append_2.getDimension());
        assertEquals("testData is 2.0 ", 2.0, v_append_2.getEntry(3));

        RealVector v_append_3 = v1.append(vec2);
        assertEquals("testData len", 6, v_append_3.getDimension());
        assertEquals("testData is  ", 4.0, v_append_3.getEntry(3));

        RealVector v_append_4 = v1.append(v2_t);
        assertEquals("testData len", 6, v_append_4.getDimension());
        assertEquals("testData is 4.0 ", 4.0, v_append_4.getEntry(3));

        RealVector v_copy = v1.copy();
        assertEquals("testData len", 3, v_copy.getDimension());
        assertNotSame("testData not same object ", v1.data, v_copy.getData());

        double[] a_double = v1.toArray();
        assertEquals("testData len", 3, a_double.length);
        assertNotSame("testData not same object ", v1.data, a_double);

        RealVector vout5 = v4.getSubVector(3, 3);
        assertEquals("testData len", 3, vout5.getDimension());
        assertEquals("testData is 4.0 ", 5.0, vout5.getEntry(1));
        try {
            v4.getSubVector(3, 7);
            fail("MatrixIndexException expected");
        } catch (MatrixIndexException ex) {
            
        } catch (Exception e) {
            fail("wrong exception caught");
        }

        ArrayRealVector v_set1 = (ArrayRealVector) v1.copy();
        v_set1.setEntry(1, 11.0);
        assertEquals("testData is 11.0 ", 11.0, v_set1.getEntry(1));
        try {
            v_set1.setEntry(3, 11.0);
            fail("MatrixIndexException expected");
        } catch (MatrixIndexException ex) {
            
        } catch (Exception e) {
            fail("wrong exception caught");
        }

        ArrayRealVector v_set2 = (ArrayRealVector) v4.copy();
        v_set2.set(3, v1);
        assertEquals("testData is 1.0 ", 1.0, v_set2.getEntry(3));
        assertEquals("testData is 7.0 ", 7.0, v_set2.getEntry(6));
        try {
            v_set2.set(7, v1);
            fail("MatrixIndexException expected");
        } catch (MatrixIndexException ex) {
            
        } catch (Exception e) {
            fail("wrong exception caught");
        }

        ArrayRealVector v_set3 = (ArrayRealVector) v1.copy();
        v_set3.set(13.0);
        assertEquals("testData is 13.0 ", 13.0, v_set3.getEntry(2));

        try {
            v_set3.getEntry(23);
            fail("ArrayIndexOutOfBoundsException expected");
        } catch (ArrayIndexOutOfBoundsException ex) {
            
        } catch (Exception e) {
            fail("wrong exception caught");
        }

        ArrayRealVector v_set4 = (ArrayRealVector) v4.copy();
        v_set4.setSubVector(3, v2_t);
        assertEquals("testData is 1.0 ", 4.0, v_set4.getEntry(3));
        assertEquals("testData is 7.0 ", 7.0, v_set4.getEntry(6));
        try {
            v_set4.setSubVector(7, v2_t);
            fail("MatrixIndexException expected");
        } catch (MatrixIndexException ex) {
            
        } catch (Exception e) {
            fail("wrong exception caught");
        }

        ArrayRealVector vout10 = (ArrayRealVector) v1.copy();
        ArrayRealVector vout10_2 = (ArrayRealVector) v1.copy();
        assertEquals(vout10, vout10_2);
        vout10_2.setEntry(0, 1.1);
        assertNotSame(vout10, vout10_2);

    }

// org.apache.commons.math.linear.ArrayRealVectorTest::testMapFunctions
    public void testMapFunctions() {
        ArrayRealVector v1 = new ArrayRealVector(vec1);

        
        RealVector v_mapAdd = v1.mapAdd(2.0d);
        double[] result_mapAdd = {3d, 4d, 5d};
        assertClose("compare vectors" ,result_mapAdd,v_mapAdd.getData(),normTolerance);

        
        RealVector v_mapAddToSelf = v1.copy();
        v_mapAddToSelf.mapAddToSelf(2.0d);
        double[] result_mapAddToSelf = {3d, 4d, 5d};
        assertClose("compare vectors" ,result_mapAddToSelf,v_mapAddToSelf.getData(),normTolerance);

        
        RealVector v_mapSubtract = v1.mapSubtract(2.0d);
        double[] result_mapSubtract = {-1d, 0d, 1d};
        assertClose("compare vectors" ,result_mapSubtract,v_mapSubtract.getData(),normTolerance);

        
        RealVector v_mapSubtractToSelf = v1.copy();
        v_mapSubtractToSelf.mapSubtractToSelf(2.0d);
        double[] result_mapSubtractToSelf = {-1d, 0d, 1d};
        assertClose("compare vectors" ,result_mapSubtractToSelf,v_mapSubtractToSelf.getData(),normTolerance);

        
        RealVector v_mapMultiply = v1.mapMultiply(2.0d);
        double[] result_mapMultiply = {2d, 4d, 6d};
        assertClose("compare vectors" ,result_mapMultiply,v_mapMultiply.getData(),normTolerance);

        
        RealVector v_mapMultiplyToSelf = v1.copy();
        v_mapMultiplyToSelf.mapMultiplyToSelf(2.0d);
        double[] result_mapMultiplyToSelf = {2d, 4d, 6d};
        assertClose("compare vectors" ,result_mapMultiplyToSelf,v_mapMultiplyToSelf.getData(),normTolerance);

        
        RealVector v_mapDivide = v1.mapDivide(2.0d);
        double[] result_mapDivide = {.5d, 1d, 1.5d};
        assertClose("compare vectors" ,result_mapDivide,v_mapDivide.getData(),normTolerance);

        
        RealVector v_mapDivideToSelf = v1.copy();
        v_mapDivideToSelf.mapDivideToSelf(2.0d);
        double[] result_mapDivideToSelf = {.5d, 1d, 1.5d};
        assertClose("compare vectors" ,result_mapDivideToSelf,v_mapDivideToSelf.getData(),normTolerance);

        
        RealVector v_mapPow = v1.mapPow(2.0d);
        double[] result_mapPow = {1d, 4d, 9d};
        assertClose("compare vectors" ,result_mapPow,v_mapPow.getData(),normTolerance);

        
        RealVector v_mapPowToSelf = v1.copy();
        v_mapPowToSelf.mapPowToSelf(2.0d);
        double[] result_mapPowToSelf = {1d, 4d, 9d};
        assertClose("compare vectors" ,result_mapPowToSelf,v_mapPowToSelf.getData(),normTolerance);

        
        RealVector v_mapExp = v1.mapExp();
        double[] result_mapExp = {2.718281828459045e+00d,7.389056098930650e+00d, 2.008553692318767e+01d};
        assertClose("compare vectors" ,result_mapExp,v_mapExp.getData(),normTolerance);

        
        RealVector v_mapExpToSelf = v1.copy();
        v_mapExpToSelf.mapExpToSelf();
        double[] result_mapExpToSelf = {2.718281828459045e+00d,7.389056098930650e+00d, 2.008553692318767e+01d};
        assertClose("compare vectors" ,result_mapExpToSelf,v_mapExpToSelf.getData(),normTolerance);

        
        RealVector v_mapExpm1 = v1.mapExpm1();
        double[] result_mapExpm1 = {1.718281828459045d,6.38905609893065d, 19.085536923187668d};
        assertClose("compare vectors" ,result_mapExpm1,v_mapExpm1.getData(),normTolerance);

        
        RealVector v_mapExpm1ToSelf = v1.copy();
        v_mapExpm1ToSelf.mapExpm1ToSelf();
        double[] result_mapExpm1ToSelf = {1.718281828459045d,6.38905609893065d, 19.085536923187668d};
        assertClose("compare vectors" ,result_mapExpm1ToSelf,v_mapExpm1ToSelf.getData(),normTolerance);

        
        RealVector v_mapLog = v1.mapLog();
        double[] result_mapLog = {0d,6.931471805599453e-01d, 1.098612288668110e+00d};
        assertClose("compare vectors" ,result_mapLog,v_mapLog.getData(),normTolerance);

        
        RealVector v_mapLogToSelf = v1.copy();
        v_mapLogToSelf.mapLogToSelf();
        double[] result_mapLogToSelf = {0d,6.931471805599453e-01d, 1.098612288668110e+00d};
        assertClose("compare vectors" ,result_mapLogToSelf,v_mapLogToSelf.getData(),normTolerance);

        
        RealVector v_mapLog10 = v1.mapLog10();
        double[] result_mapLog10 = {0d,3.010299956639812e-01d, 4.771212547196624e-01d};
        assertClose("compare vectors" ,result_mapLog10,v_mapLog10.getData(),normTolerance);

        
        RealVector v_mapLog10ToSelf = v1.copy();
        v_mapLog10ToSelf.mapLog10ToSelf();
        double[] result_mapLog10ToSelf = {0d,3.010299956639812e-01d, 4.771212547196624e-01d};
        assertClose("compare vectors" ,result_mapLog10ToSelf,v_mapLog10ToSelf.getData(),normTolerance);

        
        RealVector v_mapLog1p = v1.mapLog1p();
        double[] result_mapLog1p = {0.6931471805599453d,1.0986122886681096d,1.3862943611198906d};
        assertClose("compare vectors" ,result_mapLog1p,v_mapLog1p.getData(),normTolerance);

        
        RealVector v_mapLog1pToSelf = v1.copy();
        v_mapLog1pToSelf.mapLog1pToSelf();
        double[] result_mapLog1pToSelf = {0.6931471805599453d,1.0986122886681096d,1.3862943611198906d};
        assertClose("compare vectors" ,result_mapLog1pToSelf,v_mapLog1pToSelf.getData(),normTolerance);

        
        RealVector v_mapCosh = v1.mapCosh();
        double[] result_mapCosh = {1.543080634815244e+00d,3.762195691083631e+00d, 1.006766199577777e+01d};
        assertClose("compare vectors" ,result_mapCosh,v_mapCosh.getData(),normTolerance);

        
        RealVector v_mapCoshToSelf = v1.copy();
        v_mapCoshToSelf.mapCoshToSelf();
        double[] result_mapCoshToSelf = {1.543080634815244e+00d,3.762195691083631e+00d, 1.006766199577777e+01d};
        assertClose("compare vectors" ,result_mapCoshToSelf,v_mapCoshToSelf.getData(),normTolerance);

        
        RealVector v_mapSinh = v1.mapSinh();
        double[] result_mapSinh = {1.175201193643801e+00d,3.626860407847019e+00d, 1.001787492740990e+01d};
        assertClose("compare vectors" ,result_mapSinh,v_mapSinh.getData(),normTolerance);

        
        RealVector v_mapSinhToSelf = v1.copy();
        v_mapSinhToSelf.mapSinhToSelf();
        double[] result_mapSinhToSelf = {1.175201193643801e+00d,3.626860407847019e+00d, 1.001787492740990e+01d};
        assertClose("compare vectors" ,result_mapSinhToSelf,v_mapSinhToSelf.getData(),normTolerance);

        
        RealVector v_mapTanh = v1.mapTanh();
        double[] result_mapTanh = {7.615941559557649e-01d,9.640275800758169e-01d,9.950547536867305e-01d};
        assertClose("compare vectors" ,result_mapTanh,v_mapTanh.getData(),normTolerance);

        
        RealVector v_mapTanhToSelf = v1.copy();
        v_mapTanhToSelf.mapTanhToSelf();
        double[] result_mapTanhToSelf = {7.615941559557649e-01d,9.640275800758169e-01d,9.950547536867305e-01d};
        assertClose("compare vectors" ,result_mapTanhToSelf,v_mapTanhToSelf.getData(),normTolerance);

        
        RealVector v_mapCos = v1.mapCos();
        double[] result_mapCos = {5.403023058681398e-01d,-4.161468365471424e-01d, -9.899924966004454e-01d};
        assertClose("compare vectors" ,result_mapCos,v_mapCos.getData(),normTolerance);

        
        RealVector v_mapCosToSelf = v1.copy();
        v_mapCosToSelf.mapCosToSelf();
        double[] result_mapCosToSelf = {5.403023058681398e-01d,-4.161468365471424e-01d, -9.899924966004454e-01d};
        assertClose("compare vectors" ,result_mapCosToSelf,v_mapCosToSelf.getData(),normTolerance);

        
        RealVector v_mapSin = v1.mapSin();
        double[] result_mapSin = {8.414709848078965e-01d,9.092974268256817e-01d,1.411200080598672e-01d};
        assertClose("compare vectors" ,result_mapSin,v_mapSin.getData(),normTolerance);

        
        RealVector v_mapSinToSelf = v1.copy();
        v_mapSinToSelf.mapSinToSelf();
        double[] result_mapSinToSelf = {8.414709848078965e-01d,9.092974268256817e-01d,1.411200080598672e-01d};
        assertClose("compare vectors" ,result_mapSinToSelf,v_mapSinToSelf.getData(),normTolerance);

        
        RealVector v_mapTan = v1.mapTan();
        double[] result_mapTan = {1.557407724654902e+00d,-2.185039863261519e+00d,-1.425465430742778e-01d};
        assertClose("compare vectors" ,result_mapTan,v_mapTan.getData(),normTolerance);

        
        RealVector v_mapTanToSelf = v1.copy();
        v_mapTanToSelf.mapTanToSelf();
        double[] result_mapTanToSelf = {1.557407724654902e+00d,-2.185039863261519e+00d,-1.425465430742778e-01d};
        assertClose("compare vectors" ,result_mapTanToSelf,v_mapTanToSelf.getData(),normTolerance);

        double[] vat_a = {0d, 0.5d, 1.0d};
        ArrayRealVector vat = new ArrayRealVector(vat_a);

        
        RealVector v_mapAcos = vat.mapAcos();
        double[] result_mapAcos = {1.570796326794897e+00d,1.047197551196598e+00d, 0.0d};
        assertClose("compare vectors" ,result_mapAcos,v_mapAcos.getData(),normTolerance);

        
        RealVector v_mapAcosToSelf = vat.copy();
        v_mapAcosToSelf.mapAcosToSelf();
        double[] result_mapAcosToSelf = {1.570796326794897e+00d,1.047197551196598e+00d, 0.0d};
        assertClose("compare vectors" ,result_mapAcosToSelf,v_mapAcosToSelf.getData(),normTolerance);

        
        RealVector v_mapAsin = vat.mapAsin();
        double[] result_mapAsin = {0.0d,5.235987755982989e-01d,1.570796326794897e+00d};
        assertClose("compare vectors" ,result_mapAsin,v_mapAsin.getData(),normTolerance);

        
        RealVector v_mapAsinToSelf = vat.copy();
        v_mapAsinToSelf.mapAsinToSelf();
        double[] result_mapAsinToSelf = {0.0d,5.235987755982989e-01d,1.570796326794897e+00d};
        assertClose("compare vectors" ,result_mapAsinToSelf,v_mapAsinToSelf.getData(),normTolerance);

        
        RealVector v_mapAtan = vat.mapAtan();
        double[] result_mapAtan = {0.0d,4.636476090008061e-01d,7.853981633974483e-01d};
        assertClose("compare vectors" ,result_mapAtan,v_mapAtan.getData(),normTolerance);

        
        RealVector v_mapAtanToSelf = vat.copy();
        v_mapAtanToSelf.mapAtanToSelf();
        double[] result_mapAtanToSelf = {0.0d,4.636476090008061e-01d,7.853981633974483e-01d};
        assertClose("compare vectors" ,result_mapAtanToSelf,v_mapAtanToSelf.getData(),normTolerance);

        
        RealVector v_mapInv = v1.mapInv();
        double[] result_mapInv = {1d,0.5d,3.333333333333333e-01d};
        assertClose("compare vectors" ,result_mapInv,v_mapInv.getData(),normTolerance);

        
        RealVector v_mapInvToSelf = v1.copy();
        v_mapInvToSelf.mapInvToSelf();
        double[] result_mapInvToSelf = {1d,0.5d,3.333333333333333e-01d};
        assertClose("compare vectors" ,result_mapInvToSelf,v_mapInvToSelf.getData(),normTolerance);

        double[] abs_a = {-1.0d, 0.0d, 1.0d};
        ArrayRealVector abs_v = new ArrayRealVector(abs_a);

        
        RealVector v_mapAbs = abs_v.mapAbs();
        double[] result_mapAbs = {1d,0d,1d};
        assertClose("compare vectors" ,result_mapAbs,v_mapAbs.getData(),normTolerance);

        
        RealVector v_mapAbsToSelf = abs_v.copy();
        v_mapAbsToSelf.mapAbsToSelf();
        double[] result_mapAbsToSelf = {1d,0d,1d};
        assertClose("compare vectors" ,result_mapAbsToSelf,v_mapAbsToSelf.getData(),normTolerance);

        
        RealVector v_mapSqrt = v1.mapSqrt();
        double[] result_mapSqrt = {1d,1.414213562373095e+00d,1.732050807568877e+00d};
        assertClose("compare vectors" ,result_mapSqrt,v_mapSqrt.getData(),normTolerance);

        
        RealVector v_mapSqrtToSelf = v1.copy();
        v_mapSqrtToSelf.mapSqrtToSelf();
        double[] result_mapSqrtToSelf = {1d,1.414213562373095e+00d,1.732050807568877e+00d};
        assertClose("compare vectors" ,result_mapSqrtToSelf,v_mapSqrtToSelf.getData(),normTolerance);

        double[] cbrt_a = {-2.0d, 0.0d, 2.0d};
        ArrayRealVector cbrt_v = new ArrayRealVector(cbrt_a);

        
        RealVector v_mapCbrt = cbrt_v.mapCbrt();
        double[] result_mapCbrt = {-1.2599210498948732d,0d,1.2599210498948732d};
        assertClose("compare vectors" ,result_mapCbrt,v_mapCbrt.getData(),normTolerance);

        
        RealVector v_mapCbrtToSelf = cbrt_v.copy();
        v_mapCbrtToSelf.mapCbrtToSelf();
        double[] result_mapCbrtToSelf =  {-1.2599210498948732d,0d,1.2599210498948732d};
        assertClose("compare vectors" ,result_mapCbrtToSelf,v_mapCbrtToSelf.getData(),normTolerance);

        double[] ceil_a = {-1.1d, 0.9d, 1.1d};
        ArrayRealVector ceil_v = new ArrayRealVector(ceil_a);

        
        RealVector v_mapCeil = ceil_v.mapCeil();
        double[] result_mapCeil = {-1d,1d,2d};
        assertClose("compare vectors" ,result_mapCeil,v_mapCeil.getData(),normTolerance);

        
        RealVector v_mapCeilToSelf = ceil_v.copy();
        v_mapCeilToSelf.mapCeilToSelf();
        double[] result_mapCeilToSelf =  {-1d,1d,2d};
        assertClose("compare vectors" ,result_mapCeilToSelf,v_mapCeilToSelf.getData(),normTolerance);

        
        RealVector v_mapFloor = ceil_v.mapFloor();
        double[] result_mapFloor = {-2d,0d,1d};
        assertClose("compare vectors" ,result_mapFloor,v_mapFloor.getData(),normTolerance);

        
        RealVector v_mapFloorToSelf = ceil_v.copy();
        v_mapFloorToSelf.mapFloorToSelf();
        double[] result_mapFloorToSelf =  {-2d,0d,1d};
        assertClose("compare vectors" ,result_mapFloorToSelf,v_mapFloorToSelf.getData(),normTolerance);

        
        RealVector v_mapRint = ceil_v.mapRint();
        double[] result_mapRint = {-1d,1d,1d};
        assertClose("compare vectors" ,result_mapRint,v_mapRint.getData(),normTolerance);

        
        RealVector v_mapRintToSelf = ceil_v.copy();
        v_mapRintToSelf.mapRintToSelf();
        double[] result_mapRintToSelf =  {-1d,1d,1d};
        assertClose("compare vectors" ,result_mapRintToSelf,v_mapRintToSelf.getData(),normTolerance);

        
        RealVector v_mapSignum = ceil_v.mapSignum();
        double[] result_mapSignum = {-1d,1d,1d};
        assertClose("compare vectors" ,result_mapSignum,v_mapSignum.getData(),normTolerance);

        
        RealVector v_mapSignumToSelf = ceil_v.copy();
        v_mapSignumToSelf.mapSignumToSelf();
        double[] result_mapSignumToSelf =  {-1d,1d,1d};
        assertClose("compare vectors" ,result_mapSignumToSelf,v_mapSignumToSelf.getData(),normTolerance);

        
        
        RealVector v_mapUlp = ceil_v.mapUlp();
        double[] result_mapUlp = {2.220446049250313E-16d,1.1102230246251565E-16d,2.220446049250313E-16d};
        assertClose("compare vectors" ,result_mapUlp,v_mapUlp.getData(),normTolerance);

        
        RealVector v_mapUlpToSelf = ceil_v.copy();
        v_mapUlpToSelf.mapUlpToSelf();
        double[] result_mapUlpToSelf = {2.220446049250313E-16d,1.1102230246251565E-16d,2.220446049250313E-16d};
        assertClose("compare vectors" ,result_mapUlpToSelf,v_mapUlpToSelf.getData(),normTolerance);

    }

// org.apache.commons.math.linear.ArrayRealVectorTest::testBasicFunctions
    public void testBasicFunctions() {
        ArrayRealVector v1 = new ArrayRealVector(vec1);
        ArrayRealVector v2 = new ArrayRealVector(vec2);
        ArrayRealVector v_null = new ArrayRealVector(vec_null);

        RealVectorTestImpl v2_t = new RealVectorTestImpl(vec2);

        
        double d_getNorm = v1.getNorm();
        assertEquals("compare values  ", 3.7416573867739413,d_getNorm);

        double d_getL1Norm = v1.getL1Norm();
        assertEquals("compare values  ",6.0, d_getL1Norm);

        double d_getLInfNorm = v1.getLInfNorm();
        assertEquals("compare values  ",6.0, d_getLInfNorm);

        
        double dist = v1.getDistance(v2);
        assertEquals("compare values  ",v1.subtract(v2).getNorm(), dist );

        
        double dist_2 = v1.getDistance(v2_t);
        assertEquals("compare values  ", v1.subtract(v2).getNorm(),dist_2 );

        
        double d_getL1Distance = v1. getL1Distance(v2);
        assertEquals("compare values  ",9d, d_getL1Distance );

        double d_getL1Distance_2 = v1. getL1Distance(v2_t);
        assertEquals("compare values  ",9d, d_getL1Distance_2 );

        
        double d_getLInfDistance = v1. getLInfDistance(v2);
        assertEquals("compare values  ",3d, d_getLInfDistance );

        double d_getLInfDistance_2 = v1. getLInfDistance(v2_t);
        assertEquals("compare values  ",3d, d_getLInfDistance_2 );

        
        ArrayRealVector v_add = v1.add(v2);
        double[] result_add = {5d, 7d, 9d};
        assertClose("compare vect" ,v_add.getData(),result_add,normTolerance);

        RealVectorTestImpl vt2 = new RealVectorTestImpl(vec2);
        RealVector v_add_i = v1.add(vt2);
        double[] result_add_i = {5d, 7d, 9d};
        assertClose("compare vect" ,v_add_i.getData(),result_add_i,normTolerance);

        
        ArrayRealVector v_subtract = v1.subtract(v2);
        double[] result_subtract = {-3d, -3d, -3d};
        assertClose("compare vect" ,v_subtract.getData(),result_subtract,normTolerance);

        RealVector v_subtract_i = v1.subtract(vt2);
        double[] result_subtract_i = {-3d, -3d, -3d};
        assertClose("compare vect" ,v_subtract_i.getData(),result_subtract_i,normTolerance);

        
        ArrayRealVector  v_ebeMultiply = v1.ebeMultiply(v2);
        double[] result_ebeMultiply = {4d, 10d, 18d};
        assertClose("compare vect" ,v_ebeMultiply.getData(),result_ebeMultiply,normTolerance);

        RealVector  v_ebeMultiply_2 = v1.ebeMultiply(v2_t);
        double[] result_ebeMultiply_2 = {4d, 10d, 18d};
        assertClose("compare vect" ,v_ebeMultiply_2.getData(),result_ebeMultiply_2,normTolerance);

        
        ArrayRealVector  v_ebeDivide = v1.ebeDivide(v2);
        double[] result_ebeDivide = {0.25d, 0.4d, 0.5d};
        assertClose("compare vect" ,v_ebeDivide.getData(),result_ebeDivide,normTolerance);

        RealVector  v_ebeDivide_2 = v1.ebeDivide(v2_t);
        double[] result_ebeDivide_2 = {0.25d, 0.4d, 0.5d};
        assertClose("compare vect" ,v_ebeDivide_2.getData(),result_ebeDivide_2,normTolerance);

        
        double dot =  v1.dotProduct(v2);
        assertEquals("compare val ",32d, dot);

        
        double dot_2 =  v1.dotProduct(v2_t);
        assertEquals("compare val ",32d, dot_2);

        RealMatrix m_outerProduct = v1.outerProduct(v2);
        assertEquals("compare val ",4d, m_outerProduct.getEntry(0,0));

        RealMatrix m_outerProduct_2 = v1.outerProduct(v2_t);
        assertEquals("compare val ",4d, m_outerProduct_2.getEntry(0,0));

        RealVector v_unitVector = v1.unitVector();
        RealVector v_unitVector_2 = v1.mapDivide(v1.getNorm());
        assertClose("compare vect" ,v_unitVector.getData(),v_unitVector_2.getData(),normTolerance);

        try {
            v_null.unitVector();
            fail("Expecting ArithmeticException");
        } catch (ArithmeticException ex) {
            
        } catch (Exception e) {
            fail("wrong exception caught");
        }

        ArrayRealVector v_unitize = (ArrayRealVector)v1.copy();
        v_unitize.unitize();
        assertClose("compare vect" ,v_unitVector_2.getData(),v_unitize.getData(),normTolerance);
        try {
            v_null.unitize();
            fail("Expecting ArithmeticException");
        } catch (ArithmeticException ex) {
            
        } catch (Exception e) {
            fail("wrong exception caught");
        }

        ArrayRealVector v_projection = v1.projection(v2);
        double[] result_projection = {1.662337662337662, 2.0779220779220777, 2.493506493506493};
        assertClose("compare vect", v_projection.getData(), result_projection, normTolerance);

        RealVector v_projection_2 = v1.projection(v2_t);
        double[] result_projection_2 = {1.662337662337662, 2.0779220779220777, 2.493506493506493};
        assertClose("compare vect", v_projection_2.getData(), result_projection_2, normTolerance);

    }

// org.apache.commons.math.linear.ArrayRealVectorTest::testMisc
    public void testMisc() {
        ArrayRealVector v1 = new ArrayRealVector(vec1);
        ArrayRealVector v4 = new ArrayRealVector(vec4);
        RealVector v4_2 = new ArrayRealVector(vec4);

        String out1 = v1.toString();
        assertTrue("some output ",  out1.length()!=0);
        
        try {
            v1.checkVectorDimensions(2);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ex) {
            
        } catch (Exception e) {
            fail("wrong exception caught");
        }

       try {
            v1.checkVectorDimensions(v4);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ex) {
            
        } catch (Exception e) {
            fail("wrong exception caught");
        }

        try {
            v1.checkVectorDimensions(v4_2);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ex) {
            
        } catch (Exception e) {
            fail("wrong exception caught");
        }

    }

// org.apache.commons.math.linear.ArrayRealVectorTest::testPredicates
    public void testPredicates() {

        ArrayRealVector v = new ArrayRealVector(new double[] { 0, 1, 2 });

        assertFalse(v.isNaN());
        v.setEntry(1, Double.NaN);
        assertTrue(v.isNaN());

        assertFalse(v.isInfinite());
        v.setEntry(0, Double.POSITIVE_INFINITY);
        assertFalse(v.isInfinite());
        v.setEntry(1, 1);
        assertTrue(v.isInfinite());

        v.setEntry(0, 0);
        assertEquals(v, new ArrayRealVector(new double[] { 0, 1, 2 }));
        assertNotSame(v, new ArrayRealVector(new double[] { 0, 1, 2 + Math.ulp(2)}));
        assertNotSame(v, new ArrayRealVector(new double[] { 0, 1, 2, 3 }));

        assertEquals(new ArrayRealVector(new double[] { Double.NaN, 1, 2 }).hashCode(),
                     new ArrayRealVector(new double[] { 0, Double.NaN, 2 }).hashCode());

        assertTrue(new ArrayRealVector(new double[] { Double.NaN, 1, 2 }).hashCode() !=
                   new ArrayRealVector(new double[] { 0, 1, 2 }).hashCode());

    }

// org.apache.commons.math.linear.ArrayRealVectorTest::testSerial
    public void testSerial()  {
        ArrayRealVector v = new ArrayRealVector(new double[] { 0, 1, 2 });
        assertEquals(v,TestUtils.serializeAndRecover(v));
    }

// org.apache.commons.math.linear.BlockFieldMatrixTest::testDimensions
    public void testDimensions() {
        BlockFieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(testData);
        BlockFieldMatrix<Fraction> m2 = new BlockFieldMatrix<Fraction>(testData2);
        assertEquals("testData row dimension",3,m.getRowDimension());
        assertEquals("testData column dimension",3,m.getColumnDimension());
        assertTrue("testData is square",m.isSquare());
        assertEquals("testData2 row dimension",m2.getRowDimension(),2);
        assertEquals("testData2 column dimension",m2.getColumnDimension(),3);
        assertTrue("testData2 is not square",!m2.isSquare());
    }

// org.apache.commons.math.linear.BlockFieldMatrixTest::testCopyFunctions
    public void testCopyFunctions() {
        Random r = new Random(66636328996002l);
        BlockFieldMatrix<Fraction> m1 = createRandomMatrix(r, 47, 83);
        BlockFieldMatrix<Fraction> m2 = new BlockFieldMatrix<Fraction>(m1.getData());
        assertEquals(m1, m2);
        BlockFieldMatrix<Fraction> m3 = new BlockFieldMatrix<Fraction>(testData);
        BlockFieldMatrix<Fraction> m4 = new BlockFieldMatrix<Fraction>(m3.getData());
        assertEquals(m3, m4);
    }

// org.apache.commons.math.linear.BlockFieldMatrixTest::testAdd
    public void testAdd() {
        BlockFieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(testData);
        BlockFieldMatrix<Fraction> mInv = new BlockFieldMatrix<Fraction>(testDataInv);
        FieldMatrix<Fraction> mPlusMInv = m.add(mInv);
        Fraction[][] sumEntries = mPlusMInv.getData();
        for (int row = 0; row < m.getRowDimension(); row++) {
            for (int col = 0; col < m.getColumnDimension(); col++) {
                assertEquals(testDataPlusInv[row][col],sumEntries[row][col]);
            }
        }
    }

// org.apache.commons.math.linear.BlockFieldMatrixTest::testAddFail
    public void testAddFail() {
        BlockFieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(testData);
        BlockFieldMatrix<Fraction> m2 = new BlockFieldMatrix<Fraction>(testData2);
        try {
            m.add(m2);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockFieldMatrixTest::testPlusMinus
    public void testPlusMinus() {
        BlockFieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(testData);
        BlockFieldMatrix<Fraction> m2 = new BlockFieldMatrix<Fraction>(testDataInv);
        TestUtils.assertEquals(m.subtract(m2), m2.scalarMultiply(new Fraction(-1)).add(m));
        try {
            m.subtract(new BlockFieldMatrix<Fraction>(testData2));
            fail("Expecting illegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockFieldMatrixTest::testMultiply
     public void testMultiply() {
        BlockFieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(testData);
        BlockFieldMatrix<Fraction> mInv = new BlockFieldMatrix<Fraction>(testDataInv);
        BlockFieldMatrix<Fraction> identity = new BlockFieldMatrix<Fraction>(id);
        BlockFieldMatrix<Fraction> m2 = new BlockFieldMatrix<Fraction>(testData2);
        TestUtils.assertEquals(m.multiply(mInv), identity);
        TestUtils.assertEquals(mInv.multiply(m), identity);
        TestUtils.assertEquals(m.multiply(identity), m);
        TestUtils.assertEquals(identity.multiply(mInv), mInv);
        TestUtils.assertEquals(m2.multiply(identity), m2);
        try {
            m.multiply(new BlockFieldMatrix<Fraction>(bigSingular));
            fail("Expecting illegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockFieldMatrixTest::testSeveralBlocks
    public void testSeveralBlocks() {

        FieldMatrix<Fraction> m =
            new BlockFieldMatrix<Fraction>(FractionField.getInstance(), 37, 41);
        for (int i = 0; i < m.getRowDimension(); ++i) {
            for (int j = 0; j < m.getColumnDimension(); ++j) {
                m.setEntry(i, j, new Fraction(i * 11 + j, 11));
            }
        }

        FieldMatrix<Fraction> mT = m.transpose();
        assertEquals(m.getRowDimension(), mT.getColumnDimension());
        assertEquals(m.getColumnDimension(), mT.getRowDimension());
        for (int i = 0; i < mT.getRowDimension(); ++i) {
            for (int j = 0; j < mT.getColumnDimension(); ++j) {
                assertEquals(m.getEntry(j, i), mT.getEntry(i, j));
            }
        }

        FieldMatrix<Fraction> mPm = m.add(m);
        for (int i = 0; i < mPm.getRowDimension(); ++i) {
            for (int j = 0; j < mPm.getColumnDimension(); ++j) {
                assertEquals(m.getEntry(i, j).multiply(new Fraction(2)), mPm.getEntry(i, j));
            }
        }

        FieldMatrix<Fraction> mPmMm = mPm.subtract(m);
        for (int i = 0; i < mPmMm.getRowDimension(); ++i) {
            for (int j = 0; j < mPmMm.getColumnDimension(); ++j) {
                assertEquals(m.getEntry(i, j), mPmMm.getEntry(i, j));
            }
        }

        FieldMatrix<Fraction> mTm = mT.multiply(m);
        for (int i = 0; i < mTm.getRowDimension(); ++i) {
            for (int j = 0; j < mTm.getColumnDimension(); ++j) {
                Fraction sum = Fraction.ZERO;
                for (int k = 0; k < mT.getColumnDimension(); ++k) {
                    sum = sum.add(new Fraction(k * 11 + i, 11).multiply(new Fraction(k * 11 + j, 11)));
                }
                assertEquals(sum, mTm.getEntry(i, j));
            }
        }

        FieldMatrix<Fraction> mmT = m.multiply(mT);
        for (int i = 0; i < mmT.getRowDimension(); ++i) {
            for (int j = 0; j < mmT.getColumnDimension(); ++j) {
                Fraction sum = Fraction.ZERO;
                for (int k = 0; k < m.getColumnDimension(); ++k) {
                    sum = sum.add(new Fraction(i * 11 + k, 11).multiply(new Fraction(j * 11 + k, 11)));
                }
                assertEquals(sum, mmT.getEntry(i, j));
            }
        }

        FieldMatrix<Fraction> sub1 = m.getSubMatrix(2, 9, 5, 20);
        for (int i = 0; i < sub1.getRowDimension(); ++i) {
            for (int j = 0; j < sub1.getColumnDimension(); ++j) {
                assertEquals(new Fraction((i + 2) * 11 + (j + 5), 11), sub1.getEntry(i, j));
            }
        }

        FieldMatrix<Fraction> sub2 = m.getSubMatrix(10, 12, 3, 40);
        for (int i = 0; i < sub2.getRowDimension(); ++i) {
            for (int j = 0; j < sub2.getColumnDimension(); ++j) {
                assertEquals(new Fraction((i + 10) * 11 + (j + 3), 11), sub2.getEntry(i, j));
            }
        }

        FieldMatrix<Fraction> sub3 = m.getSubMatrix(30, 34, 0, 5);
        for (int i = 0; i < sub3.getRowDimension(); ++i) {
            for (int j = 0; j < sub3.getColumnDimension(); ++j) {
                assertEquals(new Fraction((i + 30) * 11 + (j + 0), 11), sub3.getEntry(i, j));
            }
        }

        FieldMatrix<Fraction> sub4 = m.getSubMatrix(30, 32, 32, 35);
        for (int i = 0; i < sub4.getRowDimension(); ++i) {
            for (int j = 0; j < sub4.getColumnDimension(); ++j) {
                assertEquals(new Fraction((i + 30) * 11 + (j + 32), 11), sub4.getEntry(i, j));
            }
        }

    }

// org.apache.commons.math.linear.BlockFieldMatrixTest::testMultiply2
    public void testMultiply2() {
       FieldMatrix<Fraction> m3 = new BlockFieldMatrix<Fraction>(d3);
       FieldMatrix<Fraction> m4 = new BlockFieldMatrix<Fraction>(d4);
       FieldMatrix<Fraction> m5 = new BlockFieldMatrix<Fraction>(d5);
       TestUtils.assertEquals(m3.multiply(m4), m5);
   }

// org.apache.commons.math.linear.BlockFieldMatrixTest::testTrace
    public void testTrace() {
        FieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(id);
        assertEquals(new Fraction(3),m.getTrace());
        m = new BlockFieldMatrix<Fraction>(testData2);
        try {
            m.getTrace();
            fail("Expecting NonSquareMatrixException");
        } catch (NonSquareMatrixException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockFieldMatrixTest::testScalarAdd
    public void testScalarAdd() {
        FieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(testData);
        TestUtils.assertEquals(new BlockFieldMatrix<Fraction>(testDataPlus2),
                               m.scalarAdd(new Fraction(2)));
    }

// org.apache.commons.math.linear.BlockFieldMatrixTest::testOperate
    public void testOperate() {
        FieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(id);
        TestUtils.assertEquals(testVector, m.operate(testVector));
        TestUtils.assertEquals(testVector, m.operate(new ArrayFieldVector<Fraction>(testVector)).getData());
        m = new BlockFieldMatrix<Fraction>(bigSingular);
        try {
            m.operate(testVector);
            fail("Expecting illegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockFieldMatrixTest::testOperateLarge
    public void testOperateLarge() {
        int p = (11 * BlockFieldMatrix.BLOCK_SIZE) / 10;
        int q = (11 * BlockFieldMatrix.BLOCK_SIZE) / 10;
        int r =  BlockFieldMatrix.BLOCK_SIZE / 2;
        Random random = new Random(111007463902334l);
        FieldMatrix<Fraction> m1 = createRandomMatrix(random, p, q);
        FieldMatrix<Fraction> m2 = createRandomMatrix(random, q, r);
        FieldMatrix<Fraction> m1m2 = m1.multiply(m2);
        for (int i = 0; i < r; ++i) {
            TestUtils.assertEquals(m1m2.getColumn(i), m1.operate(m2.getColumn(i)));
        }
    }

// org.apache.commons.math.linear.BlockFieldMatrixTest::testOperatePremultiplyLarge
    public void testOperatePremultiplyLarge() {
        int p = (11 * BlockFieldMatrix.BLOCK_SIZE) / 10;
        int q = (11 * BlockFieldMatrix.BLOCK_SIZE) / 10;
        int r =  BlockFieldMatrix.BLOCK_SIZE / 2;
        Random random = new Random(111007463902334l);
        FieldMatrix<Fraction> m1 = createRandomMatrix(random, p, q);
        FieldMatrix<Fraction> m2 = createRandomMatrix(random, q, r);
        FieldMatrix<Fraction> m1m2 = m1.multiply(m2);
        for (int i = 0; i < p; ++i) {
            TestUtils.assertEquals(m1m2.getRow(i), m2.preMultiply(m1.getRow(i)));
        }
    }

// org.apache.commons.math.linear.BlockFieldMatrixTest::testMath209
    public void testMath209() {
        FieldMatrix<Fraction> a = new BlockFieldMatrix<Fraction>(new Fraction[][] {
                { new Fraction(1), new Fraction(2) },
                { new Fraction(3), new Fraction(4) },
                { new Fraction(5), new Fraction(6) }
        });
        Fraction[] b = a.operate(new Fraction[] { new Fraction(1), new Fraction(1) });
        assertEquals(a.getRowDimension(), b.length);
        assertEquals( new Fraction(3), b[0]);
        assertEquals( new Fraction(7), b[1]);
        assertEquals(new Fraction(11), b[2]);
    }

// org.apache.commons.math.linear.BlockFieldMatrixTest::testTranspose
    public void testTranspose() {
        FieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(testData);
        FieldMatrix<Fraction> mIT = new FieldLUDecompositionImpl<Fraction>(m).getSolver().getInverse().transpose();
        FieldMatrix<Fraction> mTI = new FieldLUDecompositionImpl<Fraction>(m.transpose()).getSolver().getInverse();
        TestUtils.assertEquals(mIT, mTI);
        m = new BlockFieldMatrix<Fraction>(testData2);
        FieldMatrix<Fraction> mt = new BlockFieldMatrix<Fraction>(testData2T);
        TestUtils.assertEquals(mt, m.transpose());
    }

// org.apache.commons.math.linear.BlockFieldMatrixTest::testPremultiplyVector
    public void testPremultiplyVector() {
        FieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(testData);
        TestUtils.assertEquals(m.preMultiply(testVector), preMultTest);
        TestUtils.assertEquals(m.preMultiply(new ArrayFieldVector<Fraction>(testVector).getData()),
                               preMultTest);
        m = new BlockFieldMatrix<Fraction>(bigSingular);
        try {
            m.preMultiply(testVector);
            fail("expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockFieldMatrixTest::testPremultiply
    public void testPremultiply() {
        FieldMatrix<Fraction> m3 = new BlockFieldMatrix<Fraction>(d3);
        FieldMatrix<Fraction> m4 = new BlockFieldMatrix<Fraction>(d4);
        FieldMatrix<Fraction> m5 = new BlockFieldMatrix<Fraction>(d5);
        TestUtils.assertEquals(m4.preMultiply(m3), m5);

        BlockFieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(testData);
        BlockFieldMatrix<Fraction> mInv = new BlockFieldMatrix<Fraction>(testDataInv);
        BlockFieldMatrix<Fraction> identity = new BlockFieldMatrix<Fraction>(id);
        TestUtils.assertEquals(m.preMultiply(mInv), identity);
        TestUtils.assertEquals(mInv.preMultiply(m), identity);
        TestUtils.assertEquals(m.preMultiply(identity), m);
        TestUtils.assertEquals(identity.preMultiply(mInv), mInv);
        try {
            m.preMultiply(new BlockFieldMatrix<Fraction>(bigSingular));
            fail("Expecting illegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockFieldMatrixTest::testGetVectors
    public void testGetVectors() {
        FieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(testData);
        TestUtils.assertEquals(m.getRow(0), testDataRow1);
        TestUtils.assertEquals(m.getColumn(2), testDataCol3);
        try {
            m.getRow(10);
            fail("expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.getColumn(-1);
            fail("expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockFieldMatrixTest::testGetEntry
    public void testGetEntry() {
        FieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(testData);
        assertEquals(m.getEntry(0,1),new Fraction(2));
        try {
            m.getEntry(10, 4);
            fail ("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockFieldMatrixTest::testExamples
    public void testExamples() {
        
        Fraction[][] matrixData = {
                {new Fraction(1),new Fraction(2),new Fraction(3)},
                {new Fraction(2),new Fraction(5),new Fraction(3)}
        };
        FieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(matrixData);
        
        Fraction[][] matrixData2 = {
                {new Fraction(1),new Fraction(2)},
                {new Fraction(2),new Fraction(5)},
                {new Fraction(1), new Fraction(7)}
        };
        FieldMatrix<Fraction> n = new BlockFieldMatrix<Fraction>(matrixData2);
        
        FieldMatrix<Fraction> p = m.multiply(n);
        assertEquals(2, p.getRowDimension());
        assertEquals(2, p.getColumnDimension());
        
        FieldMatrix<Fraction> pInverse = new FieldLUDecompositionImpl<Fraction>(p).getSolver().getInverse();
        assertEquals(2, pInverse.getRowDimension());
        assertEquals(2, pInverse.getColumnDimension());

        
        Fraction[][] coefficientsData = {
                {new Fraction(2), new Fraction(3), new Fraction(-2)},
                {new Fraction(-1), new Fraction(7), new Fraction(6)},
                {new Fraction(4), new Fraction(-3), new Fraction(-5)}
        };
        FieldMatrix<Fraction> coefficients = new BlockFieldMatrix<Fraction>(coefficientsData);
        Fraction[] constants = {new Fraction(1), new Fraction(-2), new Fraction(1)};
        Fraction[] solution = new FieldLUDecompositionImpl<Fraction>(coefficients).getSolver().solve(constants);
        assertEquals(new Fraction(2).multiply(solution[0]).
                     add(new Fraction(3).multiply(solution[1])).
                     subtract(new Fraction(2).multiply(solution[2])),
                     constants[0]);
        assertEquals(new Fraction(-1).multiply(solution[0]).
                     add(new Fraction(7).multiply(solution[1])).
                     add(new Fraction(6).multiply(solution[2])),
                     constants[1]);
        assertEquals(new Fraction(4).multiply(solution[0]).
                     subtract(new Fraction(3).multiply(solution[1])).
                     subtract(new Fraction(5).multiply(solution[2])),
                     constants[2]);

    }

// org.apache.commons.math.linear.BlockFieldMatrixTest::testGetSubMatrix
    public void testGetSubMatrix() {
        FieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(subTestData);
        checkGetSubMatrix(m, subRows23Cols00,  2 , 3 , 0, 0);
        checkGetSubMatrix(m, subRows00Cols33,  0 , 0 , 3, 3);
        checkGetSubMatrix(m, subRows01Cols23,  0 , 1 , 2, 3);
        checkGetSubMatrix(m, subRows02Cols13,  new int[] { 0, 2 }, new int[] { 1, 3 });
        checkGetSubMatrix(m, subRows03Cols12,  new int[] { 0, 3 }, new int[] { 1, 2 });
        checkGetSubMatrix(m, subRows03Cols123, new int[] { 0, 3 }, new int[] { 1, 2, 3 });
        checkGetSubMatrix(m, subRows20Cols123, new int[] { 2, 0 }, new int[] { 1, 2, 3 });
        checkGetSubMatrix(m, subRows31Cols31,  new int[] { 3, 1 }, new int[] { 3, 1 });
        checkGetSubMatrix(m, subRows31Cols31,  new int[] { 3, 1 }, new int[] { 3, 1 });
        checkGetSubMatrix(m, null,  1, 0, 2, 4);
        checkGetSubMatrix(m, null, -1, 1, 2, 2);
        checkGetSubMatrix(m, null,  1, 0, 2, 2);
        checkGetSubMatrix(m, null,  1, 0, 2, 4);
        checkGetSubMatrix(m, null, new int[] {},    new int[] { 0 });
        checkGetSubMatrix(m, null, new int[] { 0 }, new int[] { 4 });
    }

// org.apache.commons.math.linear.BlockFieldMatrixTest::testGetSetMatrixLarge
    public void testGetSetMatrixLarge() {
        int n = 3 * BlockFieldMatrix.BLOCK_SIZE;
        FieldMatrix<Fraction> m =
            new BlockFieldMatrix<Fraction>(FractionField.getInstance(), n, n);
        FieldMatrix<Fraction> sub =
            new BlockFieldMatrix<Fraction>(FractionField.getInstance(), n - 4, n - 4).scalarAdd(new Fraction(1));

        m.setSubMatrix(sub.getData(), 2, 2);
        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < n; ++j) {
                if ((i < 2) || (i > n - 3) || (j < 2) || (j > n - 3)) {
                    assertEquals(new Fraction(0), m.getEntry(i, j));
                } else {
                    assertEquals(new Fraction(1), m.getEntry(i, j));
                }
            }
        }
        assertEquals(sub, m.getSubMatrix(2, n - 3, 2, n - 3));

    }

// org.apache.commons.math.linear.BlockFieldMatrixTest::testCopySubMatrix
    public void testCopySubMatrix() {
        FieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(subTestData);
        checkCopy(m, subRows23Cols00,  2 , 3 , 0, 0);
        checkCopy(m, subRows00Cols33,  0 , 0 , 3, 3);
        checkCopy(m, subRows01Cols23,  0 , 1 , 2, 3);
        checkCopy(m, subRows02Cols13,  new int[] { 0, 2 }, new int[] { 1, 3 });
        checkCopy(m, subRows03Cols12,  new int[] { 0, 3 }, new int[] { 1, 2 });
        checkCopy(m, subRows03Cols123, new int[] { 0, 3 }, new int[] { 1, 2, 3 });
        checkCopy(m, subRows20Cols123, new int[] { 2, 0 }, new int[] { 1, 2, 3 });
        checkCopy(m, subRows31Cols31,  new int[] { 3, 1 }, new int[] { 3, 1 });
        checkCopy(m, subRows31Cols31,  new int[] { 3, 1 }, new int[] { 3, 1 });

        checkCopy(m, null,  1, 0, 2, 4);
        checkCopy(m, null, -1, 1, 2, 2);
        checkCopy(m, null,  1, 0, 2, 2);
        checkCopy(m, null,  1, 0, 2, 4);
        checkCopy(m, null, new int[] {},    new int[] { 0 });
        checkCopy(m, null, new int[] { 0 }, new int[] { 4 });
    }

// org.apache.commons.math.linear.BlockFieldMatrixTest::testGetRowMatrix
    public void testGetRowMatrix() {
        FieldMatrix<Fraction> m     = new BlockFieldMatrix<Fraction>(subTestData);
        FieldMatrix<Fraction> mRow0 = new BlockFieldMatrix<Fraction>(subRow0);
        FieldMatrix<Fraction> mRow3 = new BlockFieldMatrix<Fraction>(subRow3);
        assertEquals("Row0", mRow0, m.getRowMatrix(0));
        assertEquals("Row3", mRow3, m.getRowMatrix(3));
        try {
            m.getRowMatrix(-1);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.getRowMatrix(4);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockFieldMatrixTest::testSetRowMatrix
    public void testSetRowMatrix() {
        FieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(subTestData);
        FieldMatrix<Fraction> mRow3 = new BlockFieldMatrix<Fraction>(subRow3);
        assertNotSame(mRow3, m.getRowMatrix(0));
        m.setRowMatrix(0, mRow3);
        assertEquals(mRow3, m.getRowMatrix(0));
        try {
            m.setRowMatrix(-1, mRow3);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.setRowMatrix(0, m);
            fail("Expecting InvalidMatrixException");
        } catch (InvalidMatrixException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockFieldMatrixTest::testGetSetRowMatrixLarge
    public void testGetSetRowMatrixLarge() {
        int n = 3 * BlockFieldMatrix.BLOCK_SIZE;
        FieldMatrix<Fraction> m =
            new BlockFieldMatrix<Fraction>(FractionField.getInstance(), n, n);
        FieldMatrix<Fraction> sub =
            new BlockFieldMatrix<Fraction>(FractionField.getInstance(), 1, n).scalarAdd(new Fraction(1));

        m.setRowMatrix(2, sub);
        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < n; ++j) {
                if (i != 2) {
                    assertEquals(new Fraction(0), m.getEntry(i, j));
                } else {
                    assertEquals(new Fraction(1), m.getEntry(i, j));
                }
            }
        }
        assertEquals(sub, m.getRowMatrix(2));

    }

// org.apache.commons.math.linear.BlockFieldMatrixTest::testGetColumnMatrix
    public void testGetColumnMatrix() {
        FieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(subTestData);
        FieldMatrix<Fraction> mColumn1 = new BlockFieldMatrix<Fraction>(subColumn1);
        FieldMatrix<Fraction> mColumn3 = new BlockFieldMatrix<Fraction>(subColumn3);
        assertEquals(mColumn1, m.getColumnMatrix(1));
        assertEquals(mColumn3, m.getColumnMatrix(3));
        try {
            m.getColumnMatrix(-1);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.getColumnMatrix(4);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockFieldMatrixTest::testSetColumnMatrix
    public void testSetColumnMatrix() {
        FieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(subTestData);
        FieldMatrix<Fraction> mColumn3 = new BlockFieldMatrix<Fraction>(subColumn3);
        assertNotSame(mColumn3, m.getColumnMatrix(1));
        m.setColumnMatrix(1, mColumn3);
        assertEquals(mColumn3, m.getColumnMatrix(1));
        try {
            m.setColumnMatrix(-1, mColumn3);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.setColumnMatrix(0, m);
            fail("Expecting InvalidMatrixException");
        } catch (InvalidMatrixException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockFieldMatrixTest::testGetSetColumnMatrixLarge
    public void testGetSetColumnMatrixLarge() {
        int n = 3 * BlockFieldMatrix.BLOCK_SIZE;
        FieldMatrix<Fraction> m =
            new BlockFieldMatrix<Fraction>(FractionField.getInstance(), n, n);
        FieldMatrix<Fraction> sub =
            new BlockFieldMatrix<Fraction>(FractionField.getInstance(), n, 1).scalarAdd(new Fraction(1));

        m.setColumnMatrix(2, sub);
        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < n; ++j) {
                if (j != 2) {
                    assertEquals(new Fraction(0), m.getEntry(i, j));
                } else {
                    assertEquals(new Fraction(1), m.getEntry(i, j));
                }
            }
        }
        assertEquals(sub, m.getColumnMatrix(2));

    }

// org.apache.commons.math.linear.BlockFieldMatrixTest::testGetRowVector
    public void testGetRowVector() {
        FieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(subTestData);
        FieldVector<Fraction> mRow0 = new ArrayFieldVector<Fraction>(subRow0[0]);
        FieldVector<Fraction> mRow3 = new ArrayFieldVector<Fraction>(subRow3[0]);
        assertEquals(mRow0, m.getRowVector(0));
        assertEquals(mRow3, m.getRowVector(3));
        try {
            m.getRowVector(-1);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.getRowVector(4);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockFieldMatrixTest::testSetRowVector
    public void testSetRowVector() {
        FieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(subTestData);
        FieldVector<Fraction> mRow3 = new ArrayFieldVector<Fraction>(subRow3[0]);
        assertNotSame(mRow3, m.getRowMatrix(0));
        m.setRowVector(0, mRow3);
        assertEquals(mRow3, m.getRowVector(0));
        try {
            m.setRowVector(-1, mRow3);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.setRowVector(0, new ArrayFieldVector<Fraction>(FractionField.getInstance(), 5));
            fail("Expecting InvalidMatrixException");
        } catch (InvalidMatrixException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockFieldMatrixTest::testGetSetRowVectorLarge
    public void testGetSetRowVectorLarge() {
        int n = 3 * BlockFieldMatrix.BLOCK_SIZE;
        FieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(FractionField.getInstance(), n, n);
        FieldVector<Fraction> sub = new ArrayFieldVector<Fraction>(n, new Fraction(1));

        m.setRowVector(2, sub);
        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < n; ++j) {
                if (i != 2) {
                    assertEquals(new Fraction(0), m.getEntry(i, j));
                } else {
                    assertEquals(new Fraction(1), m.getEntry(i, j));
                }
            }
        }
        assertEquals(sub, m.getRowVector(2));

    }

// org.apache.commons.math.linear.BlockFieldMatrixTest::testGetColumnVector
    public void testGetColumnVector() {
        FieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(subTestData);
        FieldVector<Fraction> mColumn1 = columnToVector(subColumn1);
        FieldVector<Fraction> mColumn3 = columnToVector(subColumn3);
        assertEquals(mColumn1, m.getColumnVector(1));
        assertEquals(mColumn3, m.getColumnVector(3));
        try {
            m.getColumnVector(-1);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.getColumnVector(4);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockFieldMatrixTest::testSetColumnVector
    public void testSetColumnVector() {
        FieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(subTestData);
        FieldVector<Fraction> mColumn3 = columnToVector(subColumn3);
        assertNotSame(mColumn3, m.getColumnVector(1));
        m.setColumnVector(1, mColumn3);
        assertEquals(mColumn3, m.getColumnVector(1));
        try {
            m.setColumnVector(-1, mColumn3);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.setColumnVector(0, new ArrayFieldVector<Fraction>(FractionField.getInstance(), 5));
            fail("Expecting InvalidMatrixException");
        } catch (InvalidMatrixException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockFieldMatrixTest::testGetSetColumnVectorLarge
    public void testGetSetColumnVectorLarge() {
        int n = 3 * BlockFieldMatrix.BLOCK_SIZE;
        FieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(FractionField.getInstance(), n, n);
        FieldVector<Fraction> sub = new ArrayFieldVector<Fraction>(n, new Fraction(1));

        m.setColumnVector(2, sub);
        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < n; ++j) {
                if (j != 2) {
                    assertEquals(new Fraction(0), m.getEntry(i, j));
                } else {
                    assertEquals(new Fraction(1), m.getEntry(i, j));
                }
            }
        }
        assertEquals(sub, m.getColumnVector(2));

    }

// org.apache.commons.math.linear.BlockFieldMatrixTest::testGetRow
    public void testGetRow() {
        FieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(subTestData);
        checkArrays(subRow0[0], m.getRow(0));
        checkArrays(subRow3[0], m.getRow(3));
        try {
            m.getRow(-1);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.getRow(4);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockFieldMatrixTest::testSetRow
    public void testSetRow() {
        FieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(subTestData);
        assertTrue(subRow3[0][0] != m.getRow(0)[0]);
        m.setRow(0, subRow3[0]);
        checkArrays(subRow3[0], m.getRow(0));
        try {
            m.setRow(-1, subRow3[0]);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.setRow(0, new Fraction[5]);
            fail("Expecting InvalidMatrixException");
        } catch (InvalidMatrixException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockFieldMatrixTest::testGetSetRowLarge
    public void testGetSetRowLarge() {
        int n = 3 * BlockFieldMatrix.BLOCK_SIZE;
        FieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(FractionField.getInstance(), n, n);
        Fraction[] sub = new Fraction[n];
        Arrays.fill(sub, new Fraction(1));

        m.setRow(2, sub);
        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < n; ++j) {
                if (i != 2) {
                    assertEquals(new Fraction(0), m.getEntry(i, j));
                } else {
                    assertEquals(new Fraction(1), m.getEntry(i, j));
                }
            }
        }
        checkArrays(sub, m.getRow(2));

    }

// org.apache.commons.math.linear.BlockFieldMatrixTest::testGetColumn
    public void testGetColumn() {
        FieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(subTestData);
        Fraction[] mColumn1 = columnToArray(subColumn1);
        Fraction[] mColumn3 = columnToArray(subColumn3);
        checkArrays(mColumn1, m.getColumn(1));
        checkArrays(mColumn3, m.getColumn(3));
        try {
            m.getColumn(-1);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.getColumn(4);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockFieldMatrixTest::testSetColumn
    public void testSetColumn() {
        FieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(subTestData);
        Fraction[] mColumn3 = columnToArray(subColumn3);
        assertTrue(mColumn3[0] != m.getColumn(1)[0]);
        m.setColumn(1, mColumn3);
        checkArrays(mColumn3, m.getColumn(1));
        try {
            m.setColumn(-1, mColumn3);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.setColumn(0, new Fraction[5]);
            fail("Expecting InvalidMatrixException");
        } catch (InvalidMatrixException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockFieldMatrixTest::testGetSetColumnLarge
    public void testGetSetColumnLarge() {
        int n = 3 * BlockFieldMatrix.BLOCK_SIZE;
        FieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(FractionField.getInstance(), n, n);
        Fraction[] sub = new Fraction[n];
        Arrays.fill(sub, new Fraction(1));

        m.setColumn(2, sub);
        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < n; ++j) {
                if (j != 2) {
                    assertEquals(new Fraction(0), m.getEntry(i, j));
                } else {
                    assertEquals(new Fraction(1), m.getEntry(i, j));
                }
            }
        }
        checkArrays(sub, m.getColumn(2));

    }

// org.apache.commons.math.linear.BlockFieldMatrixTest::testEqualsAndHashCode
    public void testEqualsAndHashCode() {
        BlockFieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(testData);
        BlockFieldMatrix<Fraction> m1 = (BlockFieldMatrix<Fraction>) m.copy();
        BlockFieldMatrix<Fraction> mt = (BlockFieldMatrix<Fraction>) m.transpose();
        assertTrue(m.hashCode() != mt.hashCode());
        assertEquals(m.hashCode(), m1.hashCode());
        assertEquals(m, m);
        assertEquals(m, m1);
        assertFalse(m.equals(null));
        assertFalse(m.equals(mt));
        assertFalse(m.equals(new BlockFieldMatrix<Fraction>(bigSingular)));
    }
