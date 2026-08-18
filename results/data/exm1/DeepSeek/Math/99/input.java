// buggy code
    public static int gcd(final int p, final int q) {
        int u = p;
        int v = q;
        if ((u == 0) || (v == 0)) {
            return (Math.abs(u) + Math.abs(v));
        }
        // keep u and v negative, as negative integers range down to
        // -2^31, while positive numbers can only be as large as 2^31-1
        // (i.e. we can't necessarily negate a negative number without
        // overflow)
        /* assert u!=0 && v!=0; */
        if (u > 0) {
            u = -u;
        } // make u negative
        if (v > 0) {
            v = -v;
        } // make v negative
        // B1. [Find power of 2]
        int k = 0;
        while ((u & 1) == 0 && (v & 1) == 0 && k < 31) { // while u and v are
                                                            // both even...
            u /= 2;
            v /= 2;
            k++; // cast out twos.
        }
        if (k == 31) {
            throw MathRuntimeException.createArithmeticException(
                    "overflow: gcd({0}, {1}) is 2^31",
                    new Object[] { p, q });
        }
        // B2. Initialize: u and v have been divided by 2^k and at least
        // one is odd.
        int t = ((u & 1) == 1) ? v : -(u / 2)/* B3 */;
        // t negative: u was odd, v may be even (t replaces v)
        // t positive: u was even, v is odd (t replaces u)
        do {
            /* assert u<0 && v<0; */
            // B4/B3: cast out twos from t.
            while ((t & 1) == 0) { // while t is even..
                t /= 2; // cast out twos
            }
            // B5 [reset max(u,v)]
            if (t > 0) {
                u = -t;
            } else {
                v = t;
            }
            // B6/B3. at this point both u and v should be odd.
            t = (v - u) / 2;
            // |u| larger: t positive (replace u)
            // |v| larger: t negative (replace v)
        } while (t != 0);
        return -u * (1 << k); // gcd is u*2^k
    }

    public static int lcm(int a, int b) {
        if (a==0 || b==0){
            return 0;
        }
        int lcm = Math.abs(mulAndCheck(a / gcd(a, b), b));
        return lcm;
    }

// relevant test
// org.apache.commons.math.analysis.polynomials.PolynomialsUtilsTest::testFirstChebyshevPolynomials
    public void testFirstChebyshevPolynomials() {

        checkPolynomial(PolynomialsUtils.createChebyshevPolynomial(3), "-3.0 x + 4.0 x^3");
        checkPolynomial(PolynomialsUtils.createChebyshevPolynomial(2), "-1.0 + 2.0 x^2");
        checkPolynomial(PolynomialsUtils.createChebyshevPolynomial(1), "x");
        checkPolynomial(PolynomialsUtils.createChebyshevPolynomial(0), "1.0");

        checkPolynomial(PolynomialsUtils.createChebyshevPolynomial(7), "-7.0 x + 56.0 x^3 - 112.0 x^5 + 64.0 x^7");
        checkPolynomial(PolynomialsUtils.createChebyshevPolynomial(6), "-1.0 + 18.0 x^2 - 48.0 x^4 + 32.0 x^6");
        checkPolynomial(PolynomialsUtils.createChebyshevPolynomial(5), "5.0 x - 20.0 x^3 + 16.0 x^5");
        checkPolynomial(PolynomialsUtils.createChebyshevPolynomial(4), "1.0 - 8.0 x^2 + 8.0 x^4");

    }

// org.apache.commons.math.analysis.polynomials.PolynomialsUtilsTest::testChebyshevBounds
    public void testChebyshevBounds() {
        for (int k = 0; k < 12; ++k) {
            PolynomialFunction Tk = PolynomialsUtils.createChebyshevPolynomial(k);
            for (double x = -1.0; x <= 1.0; x += 0.02) {
                assertTrue(k + " " + Tk.value(x), Math.abs(Tk.value(x)) < (1.0 + 1.0e-12));
            }
        }
    }

// org.apache.commons.math.analysis.polynomials.PolynomialsUtilsTest::testChebyshevDifferentials
    public void testChebyshevDifferentials() {
        for (int k = 0; k < 12; ++k) {

            PolynomialFunction Tk0 = PolynomialsUtils.createChebyshevPolynomial(k);
            PolynomialFunction Tk1 = Tk0.polynomialDerivative();
            PolynomialFunction Tk2 = Tk1.polynomialDerivative();

            PolynomialFunction g0 = new PolynomialFunction(new double[] { k * k });
            PolynomialFunction g1 = new PolynomialFunction(new double[] { 0, -1});
            PolynomialFunction g2 = new PolynomialFunction(new double[] { 1, 0, -1 });

            PolynomialFunction Tk0g0 = Tk0.multiply(g0);
            PolynomialFunction Tk1g1 = Tk1.multiply(g1);
            PolynomialFunction Tk2g2 = Tk2.multiply(g2);

            checkNullPolynomial(Tk0g0.add(Tk1g1.add(Tk2g2)));

        }
    }

// org.apache.commons.math.analysis.polynomials.PolynomialsUtilsTest::testFirstHermitePolynomials
    public void testFirstHermitePolynomials() {

        checkPolynomial(PolynomialsUtils.createHermitePolynomial(3), "-12.0 x + 8.0 x^3");
        checkPolynomial(PolynomialsUtils.createHermitePolynomial(2), "-2.0 + 4.0 x^2");
        checkPolynomial(PolynomialsUtils.createHermitePolynomial(1), "2.0 x");
        checkPolynomial(PolynomialsUtils.createHermitePolynomial(0), "1.0");

        checkPolynomial(PolynomialsUtils.createHermitePolynomial(7), "-1680.0 x + 3360.0 x^3 - 1344.0 x^5 + 128.0 x^7");
        checkPolynomial(PolynomialsUtils.createHermitePolynomial(6), "-120.0 + 720.0 x^2 - 480.0 x^4 + 64.0 x^6");
        checkPolynomial(PolynomialsUtils.createHermitePolynomial(5), "120.0 x - 160.0 x^3 + 32.0 x^5");
        checkPolynomial(PolynomialsUtils.createHermitePolynomial(4), "12.0 - 48.0 x^2 + 16.0 x^4");

    }

// org.apache.commons.math.analysis.polynomials.PolynomialsUtilsTest::testHermiteDifferentials
    public void testHermiteDifferentials() {
        for (int k = 0; k < 12; ++k) {

            PolynomialFunction Hk0 = PolynomialsUtils.createHermitePolynomial(k);
            PolynomialFunction Hk1 = Hk0.polynomialDerivative();
            PolynomialFunction Hk2 = Hk1.polynomialDerivative();

            PolynomialFunction g0 = new PolynomialFunction(new double[] { 2 * k });
            PolynomialFunction g1 = new PolynomialFunction(new double[] { 0, -2 });
            PolynomialFunction g2 = new PolynomialFunction(new double[] { 1 });

            PolynomialFunction Hk0g0 = Hk0.multiply(g0);
            PolynomialFunction Hk1g1 = Hk1.multiply(g1);
            PolynomialFunction Hk2g2 = Hk2.multiply(g2);

            checkNullPolynomial(Hk0g0.add(Hk1g1.add(Hk2g2)));

        }
    }

// org.apache.commons.math.analysis.polynomials.PolynomialsUtilsTest::testFirstLaguerrePolynomials
    public void testFirstLaguerrePolynomials() {

        checkPolynomial(PolynomialsUtils.createLaguerrePolynomial(3), 6l, "6.0 - 18.0 x + 9.0 x^2 - x^3");
        checkPolynomial(PolynomialsUtils.createLaguerrePolynomial(2), 2l, "2.0 - 4.0 x + x^2");
        checkPolynomial(PolynomialsUtils.createLaguerrePolynomial(1), 1l, "1.0 - x");
        checkPolynomial(PolynomialsUtils.createLaguerrePolynomial(0), 1l, "1.0");

        checkPolynomial(PolynomialsUtils.createLaguerrePolynomial(7), 5040l,
                "5040.0 - 35280.0 x + 52920.0 x^2 - 29400.0 x^3"
                + " + 7350.0 x^4 - 882.0 x^5 + 49.0 x^6 - x^7");
        checkPolynomial(PolynomialsUtils.createLaguerrePolynomial(6),  720l,
                "720.0 - 4320.0 x + 5400.0 x^2 - 2400.0 x^3 + 450.0 x^4"
                + " - 36.0 x^5 + x^6");
        checkPolynomial(PolynomialsUtils.createLaguerrePolynomial(5),  120l,
        "120.0 - 600.0 x + 600.0 x^2 - 200.0 x^3 + 25.0 x^4 - x^5");
        checkPolynomial(PolynomialsUtils.createLaguerrePolynomial(4),   24l,
        "24.0 - 96.0 x + 72.0 x^2 - 16.0 x^3 + x^4");

    }

// org.apache.commons.math.analysis.polynomials.PolynomialsUtilsTest::testLaguerreDifferentials
    public void testLaguerreDifferentials() {
        for (int k = 0; k < 12; ++k) {

            PolynomialFunction Lk0 = PolynomialsUtils.createLaguerrePolynomial(k);
            PolynomialFunction Lk1 = Lk0.polynomialDerivative();
            PolynomialFunction Lk2 = Lk1.polynomialDerivative();

            PolynomialFunction g0 = new PolynomialFunction(new double[] { k });
            PolynomialFunction g1 = new PolynomialFunction(new double[] { 1, -1 });
            PolynomialFunction g2 = new PolynomialFunction(new double[] { 0, 1 });

            PolynomialFunction Lk0g0 = Lk0.multiply(g0);
            PolynomialFunction Lk1g1 = Lk1.multiply(g1);
            PolynomialFunction Lk2g2 = Lk2.multiply(g2);

            checkNullPolynomial(Lk0g0.add(Lk1g1.add(Lk2g2)));

        }
    }

// org.apache.commons.math.analysis.polynomials.PolynomialsUtilsTest::testFirstLegendrePolynomials
    public void testFirstLegendrePolynomials() {

        checkPolynomial(PolynomialsUtils.createLegendrePolynomial(3),  2l, "-3.0 x + 5.0 x^3");
        checkPolynomial(PolynomialsUtils.createLegendrePolynomial(2),  2l, "-1.0 + 3.0 x^2");
        checkPolynomial(PolynomialsUtils.createLegendrePolynomial(1),  1l, "x");
        checkPolynomial(PolynomialsUtils.createLegendrePolynomial(0),  1l, "1.0");

        checkPolynomial(PolynomialsUtils.createLegendrePolynomial(7), 16l, "-35.0 x + 315.0 x^3 - 693.0 x^5 + 429.0 x^7");
        checkPolynomial(PolynomialsUtils.createLegendrePolynomial(6), 16l, "-5.0 + 105.0 x^2 - 315.0 x^4 + 231.0 x^6");
        checkPolynomial(PolynomialsUtils.createLegendrePolynomial(5),  8l, "15.0 x - 70.0 x^3 + 63.0 x^5");
        checkPolynomial(PolynomialsUtils.createLegendrePolynomial(4),  8l, "3.0 - 30.0 x^2 + 35.0 x^4");

    }

// org.apache.commons.math.analysis.polynomials.PolynomialsUtilsTest::testLegendreDifferentials
    public void testLegendreDifferentials() {
        for (int k = 0; k < 12; ++k) {

            PolynomialFunction Pk0 = PolynomialsUtils.createLegendrePolynomial(k);
            PolynomialFunction Pk1 = Pk0.polynomialDerivative();
            PolynomialFunction Pk2 = Pk1.polynomialDerivative();

            PolynomialFunction g0 = new PolynomialFunction(new double[] { k * (k + 1) });
            PolynomialFunction g1 = new PolynomialFunction(new double[] { 0, -2 });
            PolynomialFunction g2 = new PolynomialFunction(new double[] { 1, 0, -1 });

            PolynomialFunction Pk0g0 = Pk0.multiply(g0);
            PolynomialFunction Pk1g1 = Pk1.multiply(g1);
            PolynomialFunction Pk2g2 = Pk2.multiply(g2);

            checkNullPolynomial(Pk0g0.add(Pk1g1.add(Pk2g2)));

        }
    }

// org.apache.commons.math.analysis.polynomials.PolynomialsUtilsTest::testHighDegreeLegendre
    public void testHighDegreeLegendre() {
        try {
            PolynomialsUtils.createLegendrePolynomial(40);
            fail("an exception should have been thrown");
        } catch (ArithmeticException ae) {
            
        }

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

// org.apache.commons.math.distribution.BinomialDistributionTest::testDegenerate0
    public void testDegenerate0() throws Exception {
        setDistribution(new BinomialDistributionImpl(5,0.0d));
        setCumulativeTestPoints(new int[] {-1, 0, 1, 5, 10 });
        setCumulativeTestValues(new double[] {0d, 1d, 1d, 1d, 1d});
        setDensityTestPoints(new int[] {-1, 0, 1, 10, 11});
        setDensityTestValues(new double[] {0d, 1d, 0d, 0d, 0d});
        setInverseCumulativeTestPoints(new double[] {0.1d, 0.5d});
        setInverseCumulativeTestValues(new int[] {-1, -1});
        verifyDensities();
        verifyCumulativeProbabilities();
        verifyInverseCumulativeProbabilities();     
    }

// org.apache.commons.math.distribution.BinomialDistributionTest::testDegenerate1
    public void testDegenerate1() throws Exception {
        setDistribution(new BinomialDistributionImpl(5,1.0d));
        setCumulativeTestPoints(new int[] {-1, 0, 1, 2, 5, 10 });
        setCumulativeTestValues(new double[] {0d, 0d, 0d, 0d, 1d, 1d});
        setDensityTestPoints(new int[] {-1, 0, 1, 2, 5, 10});
        setDensityTestValues(new double[] {0d, 0d, 0d, 0d, 1d, 0d});
        setInverseCumulativeTestPoints(new double[] {0.1d, 0.5d});
        setInverseCumulativeTestValues(new int[] {4, 4});
        verifyDensities();
        verifyCumulativeProbabilities();
        verifyInverseCumulativeProbabilities();     
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

// org.apache.commons.math.distribution.PoissonDistributionTest::testNormalApproximateProbability
    public void testNormalApproximateProbability() throws Exception {
        PoissonDistribution dist = new PoissonDistributionImpl(100);
        double result = dist.normalApproximateProbability(110)
                - dist.normalApproximateProbability(89);
        assertEquals(0.706281887248, result, 1E-10);
        dist.setMean(10000);
        result = dist.normalApproximateProbability(10200)
        - dist.normalApproximateProbability(9899);
        assertEquals(0.820070051552, result, 1E-10);
    }

// org.apache.commons.math.distribution.PoissonDistributionTest::testDegenerateInverseCumulativeProbability
    public void testDegenerateInverseCumulativeProbability() throws Exception {
        PoissonDistribution dist = new PoissonDistributionImpl(DEFAULT_TEST_POISSON_PARAMETER);
        assertEquals(Integer.MAX_VALUE, dist.inverseCumulativeProbability(1.0d));
        assertEquals(-1, dist.inverseCumulativeProbability(0d));
    }

// org.apache.commons.math.distribution.PoissonDistributionTest::testMean
    public void testMean() {
        PoissonDistribution dist = new PoissonDistributionImpl(DEFAULT_TEST_POISSON_PARAMETER);
        try {
            dist.setMean(-1);
            fail("negative mean.  IllegalArgumentException expected");
        } catch(IllegalArgumentException ex) {
        }
        
        dist.setMean(10.0);
        assertEquals(10.0, dist.getMean(), 0.0);
    }

// org.apache.commons.math.distribution.PoissonDistributionTest::testLargeMeanCumulativeProbability
    public void testLargeMeanCumulativeProbability() {
        PoissonDistribution dist = new PoissonDistributionImpl(1.0);
        double mean = 1.0;
        while (mean <= 10000000.0) {
            dist.setMean(mean);
            
            double x = mean * 2.0;
            double dx = x / 10.0;
            while (x >= 0) {
                try {
                    dist.cumulativeProbability(x);
                } catch (MathException ex) {
                    fail("mean of " + mean + " and x of " + x + " caused " + ex.getMessage());
                }
                x -= dx;
            }
            
            mean *= 10.0;
        }
    }

// org.apache.commons.math.distribution.PoissonDistributionTest::testLargeMeanInverseCumulativeProbability
    public void testLargeMeanInverseCumulativeProbability() {
        PoissonDistribution dist = new PoissonDistributionImpl(1.0);
        double mean = 1.0;
        while (mean <= 10000000.0) {
            dist.setMean(mean);
            
            double p = 0.1;
            double dp = p;
            while (p < 1.0) {
                try {
                    dist.inverseCumulativeProbability(p);
                } catch (MathException ex) {
                    fail("mean of " + mean + " and p of " + p + " caused " + ex.getMessage());
                }
                p += dp;
            }
            
            mean *= 10.0;
        }
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
  public void testSingularities()
    throws CardanEulerSingularityException {

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

// org.apache.commons.math.linear.DenseRealMatrixTest::testDimensions
    public void testDimensions() {
        DenseRealMatrix m = new DenseRealMatrix(testData);
        DenseRealMatrix m2 = new DenseRealMatrix(testData2);
        assertEquals("testData row dimension",3,m.getRowDimension());
        assertEquals("testData column dimension",3,m.getColumnDimension());
        assertTrue("testData is square",m.isSquare());
        assertEquals("testData2 row dimension",m2.getRowDimension(),2);
        assertEquals("testData2 column dimension",m2.getColumnDimension(),3);
        assertTrue("testData2 is not square",!m2.isSquare());
    }

// org.apache.commons.math.linear.DenseRealMatrixTest::testCopyFunctions
    public void testCopyFunctions() {
        Random r = new Random(66636328996002l);
        DenseRealMatrix m1 = createRandomMatrix(r, 47, 83);
        DenseRealMatrix m2 = new DenseRealMatrix(m1.getData());
        assertEquals(m1, m2);
        DenseRealMatrix m3 = new DenseRealMatrix(testData);
        DenseRealMatrix m4 = new DenseRealMatrix(m3.getData());
        assertEquals(m3, m4);
    }

// org.apache.commons.math.linear.DenseRealMatrixTest::testAdd
    public void testAdd() {
        DenseRealMatrix m = new DenseRealMatrix(testData);
        DenseRealMatrix mInv = new DenseRealMatrix(testDataInv);
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

// org.apache.commons.math.linear.DenseRealMatrixTest::testAddFail
    public void testAddFail() {
        DenseRealMatrix m = new DenseRealMatrix(testData);
        DenseRealMatrix m2 = new DenseRealMatrix(testData2);
        try {
            m.add(m2);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ex) {
            ;
        }
    }

// org.apache.commons.math.linear.DenseRealMatrixTest::testNorm
    public void testNorm() {
        DenseRealMatrix m = new DenseRealMatrix(testData);
        DenseRealMatrix m2 = new DenseRealMatrix(testData2);
        assertEquals("testData norm",14d,m.getNorm(),entryTolerance);
        assertEquals("testData2 norm",7d,m2.getNorm(),entryTolerance);
    }

// org.apache.commons.math.linear.DenseRealMatrixTest::testFrobeniusNorm
    public void testFrobeniusNorm() {
        DenseRealMatrix m = new DenseRealMatrix(testData);
        DenseRealMatrix m2 = new DenseRealMatrix(testData2);
        assertEquals("testData Frobenius norm", Math.sqrt(117.0), m.getFrobeniusNorm(), entryTolerance);
        assertEquals("testData2 Frobenius norm", Math.sqrt(52.0), m2.getFrobeniusNorm(), entryTolerance);
    }

// org.apache.commons.math.linear.DenseRealMatrixTest::testPlusMinus
    public void testPlusMinus() {
        DenseRealMatrix m = new DenseRealMatrix(testData);
        DenseRealMatrix m2 = new DenseRealMatrix(testDataInv);
        assertClose(m.subtract(m2), m2.scalarMultiply(-1d).add(m), entryTolerance);        
        try {
            m.subtract(new DenseRealMatrix(testData2));
            fail("Expecting illegalArgumentException");
        } catch (IllegalArgumentException ex) {
            ;
        }      
    }

// org.apache.commons.math.linear.DenseRealMatrixTest::testMultiply
     public void testMultiply() {
        DenseRealMatrix m = new DenseRealMatrix(testData);
        DenseRealMatrix mInv = new DenseRealMatrix(testDataInv);
        DenseRealMatrix identity = new DenseRealMatrix(id);
        DenseRealMatrix m2 = new DenseRealMatrix(testData2);
        assertClose(m.multiply(mInv), identity, entryTolerance);
        assertClose(mInv.multiply(m), identity, entryTolerance);
        assertClose(m.multiply(identity), m, entryTolerance);
        assertClose(identity.multiply(mInv), mInv, entryTolerance);
        assertClose(m2.multiply(identity), m2, entryTolerance); 
        try {
            m.multiply(new DenseRealMatrix(bigSingular));
            fail("Expecting illegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }      
    }

// org.apache.commons.math.linear.DenseRealMatrixTest::testSeveralBlocks
    public void testSeveralBlocks() {

        RealMatrix m = new DenseRealMatrix(35, 71);
        for (int i = 0; i < m.getRowDimension(); ++i) {
            for (int j = 0; j < m.getColumnDimension(); ++j) {
                m.setEntry(i, j, i + j / 1024.0);
            }
        }

        RealMatrix mT = m.transpose();
        assertEquals(m.getRowDimension(), mT.getColumnDimension());
        assertEquals(m.getColumnDimension(), mT.getRowDimension());
        for (int i = 0; i < mT.getRowDimension(); ++i) {
            for (int j = 0; j < mT.getColumnDimension(); ++j) {
                assertEquals(m.getEntry(j, i), mT.getEntry(i, j), 0);
            }
        }

        RealMatrix mPm = m.add(m);
        for (int i = 0; i < mPm.getRowDimension(); ++i) {
            for (int j = 0; j < mPm.getColumnDimension(); ++j) {
                assertEquals(2 * m.getEntry(i, j), mPm.getEntry(i, j), 0);
            }
        }

        RealMatrix mPmMm = mPm.subtract(m);
        for (int i = 0; i < mPmMm.getRowDimension(); ++i) {
            for (int j = 0; j < mPmMm.getColumnDimension(); ++j) {
                assertEquals(m.getEntry(i, j), mPmMm.getEntry(i, j), 0);
            }
        }

        RealMatrix mTm = mT.multiply(m);
        for (int i = 0; i < mTm.getRowDimension(); ++i) {
            for (int j = 0; j < mTm.getColumnDimension(); ++j) {
                double sum = 0;
                for (int k = 0; k < mT.getColumnDimension(); ++k) {
                    sum += (k + i / 1024.0) * (k + j / 1024.0);
                }
                assertEquals(sum, mTm.getEntry(i, j), 0);
            }
        }

        RealMatrix mmT = m.multiply(mT);
        for (int i = 0; i < mmT.getRowDimension(); ++i) {
            for (int j = 0; j < mmT.getColumnDimension(); ++j) {
                double sum = 0;
                for (int k = 0; k < m.getColumnDimension(); ++k) {
                    sum += (i + k / 1024.0) * (j + k / 1024.0);
                }
                assertEquals(sum, mmT.getEntry(i, j), 0);
            }
        }

        RealMatrix sub1 = m.getSubMatrix(2, 9, 5, 20);
        for (int i = 0; i < sub1.getRowDimension(); ++i) {
            for (int j = 0; j < sub1.getColumnDimension(); ++j) {
                assertEquals((i + 2) + (j + 5) / 1024.0, sub1.getEntry(i, j), 0);
            }
        }

        RealMatrix sub2 = m.getSubMatrix(10, 12, 3, 70);
        for (int i = 0; i < sub2.getRowDimension(); ++i) {
            for (int j = 0; j < sub2.getColumnDimension(); ++j) {
                assertEquals((i + 10) + (j + 3) / 1024.0, sub2.getEntry(i, j), 0);
            }
        }

        RealMatrix sub3 = m.getSubMatrix(30, 34, 0, 5);
        for (int i = 0; i < sub3.getRowDimension(); ++i) {
            for (int j = 0; j < sub3.getColumnDimension(); ++j) {
                assertEquals((i + 30) + (j + 0) / 1024.0, sub3.getEntry(i, j), 0);
            }
        }

        RealMatrix sub4 = m.getSubMatrix(30, 32, 62, 65);
        for (int i = 0; i < sub4.getRowDimension(); ++i) {
            for (int j = 0; j < sub4.getColumnDimension(); ++j) {
                assertEquals((i + 30) + (j + 62) / 1024.0, sub4.getEntry(i, j), 0);
            }
        }

    }

// org.apache.commons.math.linear.DenseRealMatrixTest::testMultiply2
    public void testMultiply2() { 
       RealMatrix m3 = new DenseRealMatrix(d3);   
       RealMatrix m4 = new DenseRealMatrix(d4);
       RealMatrix m5 = new DenseRealMatrix(d5);
       assertClose(m3.multiply(m4), m5, entryTolerance);
   }

// org.apache.commons.math.linear.DenseRealMatrixTest::testTrace
    public void testTrace() {
        RealMatrix m = new DenseRealMatrix(id);
        assertEquals("identity trace",3d,m.getTrace(),entryTolerance);
        m = new DenseRealMatrix(testData2);
        try {
            m.getTrace();
            fail("Expecting NonSquareMatrixException");
        } catch (NonSquareMatrixException ex) {
            ;
        }      
    }

// org.apache.commons.math.linear.DenseRealMatrixTest::testScalarAdd
    public void testScalarAdd() {
        RealMatrix m = new DenseRealMatrix(testData);
        assertClose(new DenseRealMatrix(testDataPlus2), m.scalarAdd(2d), entryTolerance);
    }

// org.apache.commons.math.linear.DenseRealMatrixTest::testOperate
    public void testOperate() {
        RealMatrix m = new DenseRealMatrix(id);
        assertClose(testVector, m.operate(testVector), entryTolerance);
        assertClose(testVector, m.operate(new RealVectorImpl(testVector)).getData(), entryTolerance);
        m = new DenseRealMatrix(bigSingular);
        try {
            m.operate(testVector);
            fail("Expecting illegalArgumentException");
        } catch (IllegalArgumentException ex) {
            ;
        }      
    }

// org.apache.commons.math.linear.DenseRealMatrixTest::testOperateLarge
    public void testOperateLarge() {
        int p = (7 * DenseRealMatrix.BLOCK_SIZE) / 2;
        int q = (5 * DenseRealMatrix.BLOCK_SIZE) / 2;
        int r =  3 * DenseRealMatrix.BLOCK_SIZE;
        Random random = new Random(111007463902334l);
        RealMatrix m1 = createRandomMatrix(random, p, q);
        RealMatrix m2 = createRandomMatrix(random, q, r);
        RealMatrix m1m2 = m1.multiply(m2);
        for (int i = 0; i < r; ++i) {
            checkArrays(m1m2.getColumn(i), m1.operate(m2.getColumn(i)));
        }
    }

// org.apache.commons.math.linear.DenseRealMatrixTest::testOperatePremultiplyLarge
    public void testOperatePremultiplyLarge() {
        int p = (7 * DenseRealMatrix.BLOCK_SIZE) / 2;
        int q = (5 * DenseRealMatrix.BLOCK_SIZE) / 2;
        int r =  3 * DenseRealMatrix.BLOCK_SIZE;
        Random random = new Random(111007463902334l);
        RealMatrix m1 = createRandomMatrix(random, p, q);
        RealMatrix m2 = createRandomMatrix(random, q, r);
        RealMatrix m1m2 = m1.multiply(m2);
        for (int i = 0; i < p; ++i) {
            checkArrays(m1m2.getRow(i), m2.preMultiply(m1.getRow(i)));
        }
    }

// org.apache.commons.math.linear.DenseRealMatrixTest::testMath209
    public void testMath209() {
        RealMatrix a = new DenseRealMatrix(new double[][] {
                { 1, 2 }, { 3, 4 }, { 5, 6 }
        });
        double[] b = a.operate(new double[] { 1, 1 });
        assertEquals(a.getRowDimension(), b.length);
        assertEquals( 3.0, b[0], 1.0e-12);
        assertEquals( 7.0, b[1], 1.0e-12);
        assertEquals(11.0, b[2], 1.0e-12);
    }

// org.apache.commons.math.linear.DenseRealMatrixTest::testTranspose
    public void testTranspose() {
        RealMatrix m = new DenseRealMatrix(testData); 
        RealMatrix mIT = new LUDecompositionImpl(m).getSolver().getInverse().transpose();
        RealMatrix mTI = new LUDecompositionImpl(m.transpose()).getSolver().getInverse();
        assertClose(mIT, mTI, normTolerance);
        m = new DenseRealMatrix(testData2);
        RealMatrix mt = new DenseRealMatrix(testData2T);
        assertClose(mt, m.transpose(), normTolerance);
    }

// org.apache.commons.math.linear.DenseRealMatrixTest::testPremultiplyVector
    public void testPremultiplyVector() {
        RealMatrix m = new DenseRealMatrix(testData);
        assertClose(m.preMultiply(testVector), preMultTest, normTolerance);
        assertClose(m.preMultiply(new RealVectorImpl(testVector).getData()),
                    preMultTest, normTolerance);
        m = new DenseRealMatrix(bigSingular);
        try {
            m.preMultiply(testVector);
            fail("expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            ;
        }
    }

// org.apache.commons.math.linear.DenseRealMatrixTest::testPremultiply
    public void testPremultiply() {
        RealMatrix m3 = new DenseRealMatrix(d3);   
        RealMatrix m4 = new DenseRealMatrix(d4);
        RealMatrix m5 = new DenseRealMatrix(d5);
        assertClose(m4.preMultiply(m3), m5, entryTolerance);
        
        DenseRealMatrix m = new DenseRealMatrix(testData);
        DenseRealMatrix mInv = new DenseRealMatrix(testDataInv);
        DenseRealMatrix identity = new DenseRealMatrix(id);
        assertClose(m.preMultiply(mInv), identity, entryTolerance);
        assertClose(mInv.preMultiply(m), identity, entryTolerance);
        assertClose(m.preMultiply(identity), m, entryTolerance);
        assertClose(identity.preMultiply(mInv), mInv, entryTolerance);
        try {
            m.preMultiply(new DenseRealMatrix(bigSingular));
            fail("Expecting illegalArgumentException");
        } catch (IllegalArgumentException ex) {
            ;
        }      
    }

// org.apache.commons.math.linear.DenseRealMatrixTest::testGetVectors
    public void testGetVectors() {
        RealMatrix m = new DenseRealMatrix(testData);
        assertClose(m.getRow(0), testDataRow1, entryTolerance);
        assertClose(m.getColumn(2), testDataCol3, entryTolerance);
        try {
            m.getRow(10);
            fail("expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            ;
        }
        try {
            m.getColumn(-1);
            fail("expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            ;
        }
    }

// org.apache.commons.math.linear.DenseRealMatrixTest::testGetEntry
    public void testGetEntry() {
        RealMatrix m = new DenseRealMatrix(testData);
        assertEquals("get entry",m.getEntry(0,1),2d,entryTolerance);
        try {
            m.getEntry(10, 4);
            fail ("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
    }

// org.apache.commons.math.linear.DenseRealMatrixTest::testExamples
    public void testExamples() {
        
        double[][] matrixData = { {1d,2d,3d}, {2d,5d,3d}};
        RealMatrix m = new DenseRealMatrix(matrixData);
        
        double[][] matrixData2 = { {1d,2d}, {2d,5d}, {1d, 7d}};
        RealMatrix n = new DenseRealMatrix(matrixData2);
        
        RealMatrix p = m.multiply(n);
        assertEquals(2, p.getRowDimension());
        assertEquals(2, p.getColumnDimension());
        
        RealMatrix pInverse = new LUDecompositionImpl(p).getSolver().getInverse(); 
        assertEquals(2, pInverse.getRowDimension());
        assertEquals(2, pInverse.getColumnDimension());
        
        
        double[][] coefficientsData = {{2, 3, -2}, {-1, 7, 6}, {4, -3, -5}};
        RealMatrix coefficients = new DenseRealMatrix(coefficientsData);
        double[] constants = {1, -2, 1};
        double[] solution = new LUDecompositionImpl(coefficients).getSolver().solve(constants);
        assertEquals(2 * solution[0] + 3 * solution[1] -2 * solution[2], constants[0], 1E-12);
        assertEquals(-1 * solution[0] + 7 * solution[1] + 6 * solution[2], constants[1], 1E-12);
        assertEquals(4 * solution[0] - 3 * solution[1] -5 * solution[2], constants[2], 1E-12);   
        
    }

// org.apache.commons.math.linear.DenseRealMatrixTest::testGetSubMatrix
    public void testGetSubMatrix() {
        RealMatrix m = new DenseRealMatrix(subTestData);
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

// org.apache.commons.math.linear.DenseRealMatrixTest::testGetSetMatrixLarge
    public void testGetSetMatrixLarge() {
        int n = 3 * DenseRealMatrix.BLOCK_SIZE;
        RealMatrix m = new DenseRealMatrix(n, n);
        RealMatrix sub = new DenseRealMatrix(n - 4, n - 4).scalarAdd(1);

        m.setSubMatrix(sub.getData(), 2, 2);
        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < n; ++j) {
                if ((i < 2) || (i > n - 3) || (j < 2) || (j > n - 3)) {
                    assertEquals(0.0, m.getEntry(i, j), 0.0);
                } else {
                    assertEquals(1.0, m.getEntry(i, j), 0.0);
                }
            }
        }
        assertEquals(sub, m.getSubMatrix(2, n - 3, 2, n - 3));

    }

// org.apache.commons.math.linear.DenseRealMatrixTest::testCopySubMatrix
    public void testCopySubMatrix() {
        RealMatrix m = new DenseRealMatrix(subTestData);
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

// org.apache.commons.math.linear.DenseRealMatrixTest::testGetRowMatrix
    public void testGetRowMatrix() {
        RealMatrix m     = new DenseRealMatrix(subTestData);
        RealMatrix mRow0 = new DenseRealMatrix(subRow0);
        RealMatrix mRow3 = new DenseRealMatrix(subRow3);
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

// org.apache.commons.math.linear.DenseRealMatrixTest::testSetRowMatrix
    public void testSetRowMatrix() {
        RealMatrix m = new DenseRealMatrix(subTestData);
        RealMatrix mRow3 = new DenseRealMatrix(subRow3);
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

// org.apache.commons.math.linear.DenseRealMatrixTest::testGetSetRowMatrixLarge
    public void testGetSetRowMatrixLarge() {
        int n = 3 * DenseRealMatrix.BLOCK_SIZE;
        RealMatrix m = new DenseRealMatrix(n, n);
        RealMatrix sub = new DenseRealMatrix(1, n).scalarAdd(1);

        m.setRowMatrix(2, sub);
        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < n; ++j) {
                if (i != 2) {
                    assertEquals(0.0, m.getEntry(i, j), 0.0);
                } else {
                    assertEquals(1.0, m.getEntry(i, j), 0.0);
                }
            }
        }
        assertEquals(sub, m.getRowMatrix(2));

    }

// org.apache.commons.math.linear.DenseRealMatrixTest::testGetColumnMatrix
    public void testGetColumnMatrix() {
        RealMatrix m = new DenseRealMatrix(subTestData);
        RealMatrix mColumn1 = new DenseRealMatrix(subColumn1);
        RealMatrix mColumn3 = new DenseRealMatrix(subColumn3);
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

// org.apache.commons.math.linear.DenseRealMatrixTest::testSetColumnMatrix
    public void testSetColumnMatrix() {
        RealMatrix m = new DenseRealMatrix(subTestData);
        RealMatrix mColumn3 = new DenseRealMatrix(subColumn3);
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

// org.apache.commons.math.linear.DenseRealMatrixTest::testGetSetColumnMatrixLarge
    public void testGetSetColumnMatrixLarge() {
        int n = 3 * DenseRealMatrix.BLOCK_SIZE;
        RealMatrix m = new DenseRealMatrix(n, n);
        RealMatrix sub = new DenseRealMatrix(n, 1).scalarAdd(1);

        m.setColumnMatrix(2, sub);
        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < n; ++j) {
                if (j != 2) {
                    assertEquals(0.0, m.getEntry(i, j), 0.0);
                } else {
                    assertEquals(1.0, m.getEntry(i, j), 0.0);
                }
            }
        }
        assertEquals(sub, m.getColumnMatrix(2));

    }

// org.apache.commons.math.linear.DenseRealMatrixTest::testGetRowVector
    public void testGetRowVector() {
        RealMatrix m = new DenseRealMatrix(subTestData);
        RealVector mRow0 = new RealVectorImpl(subRow0[0]);
        RealVector mRow3 = new RealVectorImpl(subRow3[0]);
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

// org.apache.commons.math.linear.DenseRealMatrixTest::testSetRowVector
    public void testSetRowVector() {
        RealMatrix m = new DenseRealMatrix(subTestData);
        RealVector mRow3 = new RealVectorImpl(subRow3[0]);
        assertNotSame(mRow3, m.getRowMatrix(0));
        m.setRowVector(0, mRow3);
        assertEquals(mRow3, m.getRowVector(0));
        try {
            m.setRowVector(-1, mRow3);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.setRowVector(0, new RealVectorImpl(5));
            fail("Expecting InvalidMatrixException");
        } catch (InvalidMatrixException ex) {
            
        }
    }

// org.apache.commons.math.linear.DenseRealMatrixTest::testGetSetRowVectorLarge
    public void testGetSetRowVectorLarge() {
        int n = 3 * DenseRealMatrix.BLOCK_SIZE;
        RealMatrix m = new DenseRealMatrix(n, n);
        RealVector sub = new RealVectorImpl(n, 1.0);

        m.setRowVector(2, sub);
        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < n; ++j) {
                if (i != 2) {
                    assertEquals(0.0, m.getEntry(i, j), 0.0);
                } else {
                    assertEquals(1.0, m.getEntry(i, j), 0.0);
                }
            }
        }
        assertEquals(sub, m.getRowVector(2));

    }

// org.apache.commons.math.linear.DenseRealMatrixTest::testGetColumnVector
    public void testGetColumnVector() {
        RealMatrix m = new DenseRealMatrix(subTestData);
        RealVector mColumn1 = columnToVector(subColumn1);
        RealVector mColumn3 = columnToVector(subColumn3);
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

// org.apache.commons.math.linear.DenseRealMatrixTest::testSetColumnVector
    public void testSetColumnVector() {
        RealMatrix m = new DenseRealMatrix(subTestData);
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
            m.setColumnVector(0, new RealVectorImpl(5));
            fail("Expecting InvalidMatrixException");
        } catch (InvalidMatrixException ex) {
            
        }
    }

// org.apache.commons.math.linear.DenseRealMatrixTest::testGetSetColumnVectorLarge
    public void testGetSetColumnVectorLarge() {
        int n = 3 * DenseRealMatrix.BLOCK_SIZE;
        RealMatrix m = new DenseRealMatrix(n, n);
        RealVector sub = new RealVectorImpl(n, 1.0);

        m.setColumnVector(2, sub);
        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < n; ++j) {
                if (j != 2) {
                    assertEquals(0.0, m.getEntry(i, j), 0.0);
                } else {
                    assertEquals(1.0, m.getEntry(i, j), 0.0);
                }
            }
        }
        assertEquals(sub, m.getColumnVector(2));

    }

// org.apache.commons.math.linear.DenseRealMatrixTest::testGetRow
    public void testGetRow() {
        RealMatrix m = new DenseRealMatrix(subTestData);
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

// org.apache.commons.math.linear.DenseRealMatrixTest::testSetRow
    public void testSetRow() {
        RealMatrix m = new DenseRealMatrix(subTestData);
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

// org.apache.commons.math.linear.DenseRealMatrixTest::testGetSetRowLarge
    public void testGetSetRowLarge() {
        int n = 3 * DenseRealMatrix.BLOCK_SIZE;
        RealMatrix m = new DenseRealMatrix(n, n);
        double[] sub = new double[n];
        Arrays.fill(sub, 1.0);

        m.setRow(2, sub);
        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < n; ++j) {
                if (i != 2) {
                    assertEquals(0.0, m.getEntry(i, j), 0.0);
                } else {
                    assertEquals(1.0, m.getEntry(i, j), 0.0);
                }
            }
        }
        checkArrays(sub, m.getRow(2));

    }

// org.apache.commons.math.linear.DenseRealMatrixTest::testGetColumn
    public void testGetColumn() {
        RealMatrix m = new DenseRealMatrix(subTestData);
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

// org.apache.commons.math.linear.DenseRealMatrixTest::testSetColumn
    public void testSetColumn() {
        RealMatrix m = new DenseRealMatrix(subTestData);
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

// org.apache.commons.math.linear.DenseRealMatrixTest::testGetSetColumnLarge
    public void testGetSetColumnLarge() {
        int n = 3 * DenseRealMatrix.BLOCK_SIZE;
        RealMatrix m = new DenseRealMatrix(n, n);
        double[] sub = new double[n];
        Arrays.fill(sub, 1.0);

        m.setColumn(2, sub);
        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < n; ++j) {
                if (j != 2) {
                    assertEquals(0.0, m.getEntry(i, j), 0.0);
                } else {
                    assertEquals(1.0, m.getEntry(i, j), 0.0);
                }
            }
        }
        checkArrays(sub, m.getColumn(2));

    }

// org.apache.commons.math.linear.DenseRealMatrixTest::testEqualsAndHashCode
    public void testEqualsAndHashCode() {
        DenseRealMatrix m = new DenseRealMatrix(testData);
        DenseRealMatrix m1 = (DenseRealMatrix) m.copy();
        DenseRealMatrix mt = (DenseRealMatrix) m.transpose();
        assertTrue(m.hashCode() != mt.hashCode());
        assertEquals(m.hashCode(), m1.hashCode());
        assertEquals(m, m);
        assertEquals(m, m1);
        assertFalse(m.equals(null));
        assertFalse(m.equals(mt));
        assertFalse(m.equals(new DenseRealMatrix(bigSingular))); 
    }

// org.apache.commons.math.linear.DenseRealMatrixTest::testToString
    public void testToString() {
        DenseRealMatrix m = new DenseRealMatrix(testData);
        assertEquals("DenseRealMatrix{{1.0,2.0,3.0},{2.0,5.0,3.0},{1.0,0.0,8.0}}",
                m.toString());
    }

// org.apache.commons.math.linear.DenseRealMatrixTest::testSetSubMatrix
    public void testSetSubMatrix() throws Exception {
        DenseRealMatrix m = new DenseRealMatrix(testData);
        m.setSubMatrix(detData2,1,1);
        RealMatrix expected = new DenseRealMatrix
            (new double[][] {{1.0,2.0,3.0},{2.0,1.0,3.0},{1.0,2.0,4.0}});
        assertEquals(expected, m);  
        
        m.setSubMatrix(detData2,0,0);
        expected = new DenseRealMatrix
            (new double[][] {{1.0,3.0,3.0},{2.0,4.0,3.0},{1.0,2.0,4.0}});
        assertEquals(expected, m);  
        
        m.setSubMatrix(testDataPlus2,0,0);      
        expected = new DenseRealMatrix
            (new double[][] {{3.0,4.0,5.0},{4.0,7.0,5.0},{3.0,2.0,10.0}});
        assertEquals(expected, m);   
        
        
        DenseRealMatrix matrix = new DenseRealMatrix
            (new double[][] {{1, 2, 3, 4}, {5, 6, 7, 8}, {9, 0, 1 , 2}});
        matrix.setSubMatrix(new double[][] {{3, 4}, {5, 6}}, 1, 1);
        expected = new DenseRealMatrix
            (new double[][] {{1, 2, 3, 4}, {5, 3, 4, 8}, {9, 5 ,6, 2}});
        assertEquals(expected, matrix);   

        
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

// org.apache.commons.math.linear.DenseRealMatrixTest::testWalk
    public void testWalk() {
        int rows    = 150;
        int columns = 75;

        RealMatrix m = new DenseRealMatrix(rows, columns);
        m.walkInRowOrder(new SetVisitor());
        GetVisitor getVisitor = new GetVisitor();
        m.walkInOptimizedOrder(getVisitor);
        assertEquals(rows * columns, getVisitor.getCount());

        m = new DenseRealMatrix(rows, columns);
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

        m = new DenseRealMatrix(rows, columns);
        m.walkInColumnOrder(new SetVisitor());
        getVisitor = new GetVisitor();
        m.walkInOptimizedOrder(getVisitor);
        assertEquals(rows * columns, getVisitor.getCount());

        m = new DenseRealMatrix(rows, columns);
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

        m = new DenseRealMatrix(rows, columns);
        m.walkInOptimizedOrder(new SetVisitor());
        getVisitor = new GetVisitor();
        m.walkInRowOrder(getVisitor);
        assertEquals(rows * columns, getVisitor.getCount());

        m = new DenseRealMatrix(rows, columns);
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

        m = new DenseRealMatrix(rows, columns);
        m.walkInOptimizedOrder(new SetVisitor());
        getVisitor = new GetVisitor();
        m.walkInColumnOrder(getVisitor);
        assertEquals(rows * columns, getVisitor.getCount());

        m = new DenseRealMatrix(rows, columns);
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

// org.apache.commons.math.linear.RealMatrixImplTest::testDimensions
    public void testDimensions() {
        RealMatrixImpl m = new RealMatrixImpl(testData);
        RealMatrixImpl m2 = new RealMatrixImpl(testData2);
        assertEquals("testData row dimension",3,m.getRowDimension());
        assertEquals("testData column dimension",3,m.getColumnDimension());
        assertTrue("testData is square",m.isSquare());
        assertEquals("testData2 row dimension",m2.getRowDimension(),2);
        assertEquals("testData2 column dimension",m2.getColumnDimension(),3);
        assertTrue("testData2 is not square",!m2.isSquare());
    }

// org.apache.commons.math.linear.RealMatrixImplTest::testCopyFunctions
    public void testCopyFunctions() {
        RealMatrixImpl m1 = new RealMatrixImpl(testData);
        RealMatrixImpl m2 = new RealMatrixImpl(m1.getData());
        assertEquals(m2,m1);
        RealMatrixImpl m3 = new RealMatrixImpl(testData);
        RealMatrixImpl m4 = new RealMatrixImpl(m3.getData(), false);
        assertEquals(m4,m3);
    }

// org.apache.commons.math.linear.RealMatrixImplTest::testAdd
    public void testAdd() {
        RealMatrixImpl m = new RealMatrixImpl(testData);
        RealMatrixImpl mInv = new RealMatrixImpl(testDataInv);
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

// org.apache.commons.math.linear.RealMatrixImplTest::testAddFail
    public void testAddFail() {
        RealMatrixImpl m = new RealMatrixImpl(testData);
        RealMatrixImpl m2 = new RealMatrixImpl(testData2);
        try {
            m.add(m2);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ex) {
            ;
        }
    }

// org.apache.commons.math.linear.RealMatrixImplTest::testNorm
    public void testNorm() {
        RealMatrixImpl m = new RealMatrixImpl(testData);
        RealMatrixImpl m2 = new RealMatrixImpl(testData2);
        assertEquals("testData norm",14d,m.getNorm(),entryTolerance);
        assertEquals("testData2 norm",7d,m2.getNorm(),entryTolerance);
    }

// org.apache.commons.math.linear.RealMatrixImplTest::testFrobeniusNorm
    public void testFrobeniusNorm() {
        RealMatrixImpl m = new RealMatrixImpl(testData);
        RealMatrixImpl m2 = new RealMatrixImpl(testData2);
        assertEquals("testData Frobenius norm", Math.sqrt(117.0), m.getFrobeniusNorm(), entryTolerance);
        assertEquals("testData2 Frobenius norm", Math.sqrt(52.0), m2.getFrobeniusNorm(), entryTolerance);
    }

// org.apache.commons.math.linear.RealMatrixImplTest::testPlusMinus
    public void testPlusMinus() {
        RealMatrixImpl m = new RealMatrixImpl(testData);
        RealMatrixImpl m2 = new RealMatrixImpl(testDataInv);
        TestUtils.assertEquals("m-n = m + -n",m.subtract(m2),
            m2.scalarMultiply(-1d).add(m),entryTolerance);        
        try {
            m.subtract(new RealMatrixImpl(testData2));
            fail("Expecting illegalArgumentException");
        } catch (IllegalArgumentException ex) {
            ;
        }      
    }

// org.apache.commons.math.linear.RealMatrixImplTest::testMultiply
     public void testMultiply() {
        RealMatrixImpl m = new RealMatrixImpl(testData);
        RealMatrixImpl mInv = new RealMatrixImpl(testDataInv);
        RealMatrixImpl identity = new RealMatrixImpl(id);
        RealMatrixImpl m2 = new RealMatrixImpl(testData2);
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
            m.multiply(new RealMatrixImpl(bigSingular));
            fail("Expecting illegalArgumentException");
        } catch (IllegalArgumentException ex) {
            ;
        }      
    }

// org.apache.commons.math.linear.RealMatrixImplTest::testMultiply2
    public void testMultiply2() { 
       RealMatrix m3 = new RealMatrixImpl(d3);   
       RealMatrix m4 = new RealMatrixImpl(d4);
       RealMatrix m5 = new RealMatrixImpl(d5);
       TestUtils.assertEquals("m3*m4=m5", m3.multiply(m4), m5, entryTolerance);
   }

// org.apache.commons.math.linear.RealMatrixImplTest::testTrace
    public void testTrace() {
        RealMatrix m = new RealMatrixImpl(id);
        assertEquals("identity trace",3d,m.getTrace(),entryTolerance);
        m = new RealMatrixImpl(testData2);
        try {
            m.getTrace();
            fail("Expecting NonSquareMatrixException");
        } catch (NonSquareMatrixException ex) {
            ;
        }      
    }

// org.apache.commons.math.linear.RealMatrixImplTest::testScalarAdd
    public void testScalarAdd() {
        RealMatrix m = new RealMatrixImpl(testData);
        TestUtils.assertEquals("scalar add",new RealMatrixImpl(testDataPlus2),
            m.scalarAdd(2d),entryTolerance);
    }

// org.apache.commons.math.linear.RealMatrixImplTest::testOperate
    public void testOperate() {
        RealMatrix m = new RealMatrixImpl(id);
        TestUtils.assertEquals("identity operate", testVector,
                    m.operate(testVector), entryTolerance);
        TestUtils.assertEquals("identity operate", testVector,
                    m.operate(new RealVectorImpl(testVector)).getData(), entryTolerance);
        m = new RealMatrixImpl(bigSingular);
        try {
            m.operate(testVector);
            fail("Expecting illegalArgumentException");
        } catch (IllegalArgumentException ex) {
            ;
        }      
    }

// org.apache.commons.math.linear.RealMatrixImplTest::testMath209
    public void testMath209() {
        RealMatrix a = new RealMatrixImpl(new double[][] {
                { 1, 2 }, { 3, 4 }, { 5, 6 }
        }, false);
        double[] b = a.operate(new double[] { 1, 1 });
        assertEquals(a.getRowDimension(), b.length);
        assertEquals( 3.0, b[0], 1.0e-12);
        assertEquals( 7.0, b[1], 1.0e-12);
        assertEquals(11.0, b[2], 1.0e-12);
    }

// org.apache.commons.math.linear.RealMatrixImplTest::testTranspose
    public void testTranspose() {
        RealMatrix m = new RealMatrixImpl(testData); 
        RealMatrix mIT = new LUDecompositionImpl(m).getSolver().getInverse().transpose();
        RealMatrix mTI = new LUDecompositionImpl(m.transpose()).getSolver().getInverse();
        TestUtils.assertEquals("inverse-transpose", mIT, mTI, normTolerance);
        m = new RealMatrixImpl(testData2);
        RealMatrix mt = new RealMatrixImpl(testData2T);
        TestUtils.assertEquals("transpose",mt,m.transpose(),normTolerance);
    }

// org.apache.commons.math.linear.RealMatrixImplTest::testPremultiplyVector
    public void testPremultiplyVector() {
        RealMatrix m = new RealMatrixImpl(testData);
        TestUtils.assertEquals("premultiply", m.preMultiply(testVector),
                    preMultTest, normTolerance);
        TestUtils.assertEquals("premultiply", m.preMultiply(new RealVectorImpl(testVector).getData()),
                    preMultTest, normTolerance);
        m = new RealMatrixImpl(bigSingular);
        try {
            m.preMultiply(testVector);
            fail("expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            ;
        }
    }

// org.apache.commons.math.linear.RealMatrixImplTest::testPremultiply
    public void testPremultiply() {
        RealMatrix m3 = new RealMatrixImpl(d3);   
        RealMatrix m4 = new RealMatrixImpl(d4);
        RealMatrix m5 = new RealMatrixImpl(d5);
        TestUtils.assertEquals("m3*m4=m5", m4.preMultiply(m3), m5, entryTolerance);
        
        RealMatrixImpl m = new RealMatrixImpl(testData);
        RealMatrixImpl mInv = new RealMatrixImpl(testDataInv);
        RealMatrixImpl identity = new RealMatrixImpl(id);
        TestUtils.assertEquals("inverse multiply",m.preMultiply(mInv),
                identity,entryTolerance);
        TestUtils.assertEquals("inverse multiply",mInv.preMultiply(m),
                identity,entryTolerance);
        TestUtils.assertEquals("identity multiply",m.preMultiply(identity),
                m,entryTolerance);
        TestUtils.assertEquals("identity multiply",identity.preMultiply(mInv),
                mInv,entryTolerance);
        try {
            m.preMultiply(new RealMatrixImpl(bigSingular));
            fail("Expecting illegalArgumentException");
        } catch (IllegalArgumentException ex) {
            ;
        }      
    }

// org.apache.commons.math.linear.RealMatrixImplTest::testGetVectors
    public void testGetVectors() {
        RealMatrix m = new RealMatrixImpl(testData);
        TestUtils.assertEquals("get row",m.getRow(0),testDataRow1,entryTolerance);
        TestUtils.assertEquals("get col",m.getColumn(2),testDataCol3,entryTolerance);
        try {
            m.getRow(10);
            fail("expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            ;
        }
        try {
            m.getColumn(-1);
            fail("expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            ;
        }
    }

// org.apache.commons.math.linear.RealMatrixImplTest::testGetEntry
    public void testGetEntry() {
        RealMatrix m = new RealMatrixImpl(testData);
        assertEquals("get entry",m.getEntry(0,1),2d,entryTolerance);
        try {
            m.getEntry(10, 4);
            fail ("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
    }

// org.apache.commons.math.linear.RealMatrixImplTest::testExamples
    public void testExamples() {
        
        double[][] matrixData = { {1d,2d,3d}, {2d,5d,3d}};
        RealMatrix m = new RealMatrixImpl(matrixData);
        
        double[][] matrixData2 = { {1d,2d}, {2d,5d}, {1d, 7d}};
        RealMatrix n = new RealMatrixImpl(matrixData2);
        
        RealMatrix p = m.multiply(n);
        assertEquals(2, p.getRowDimension());
        assertEquals(2, p.getColumnDimension());
        
        RealMatrix pInverse = new LUDecompositionImpl(p).getSolver().getInverse(); 
        assertEquals(2, pInverse.getRowDimension());
        assertEquals(2, pInverse.getColumnDimension());
        
        
        double[][] coefficientsData = {{2, 3, -2}, {-1, 7, 6}, {4, -3, -5}};
        RealMatrix coefficients = new RealMatrixImpl(coefficientsData);
        double[] constants = {1, -2, 1};
        double[] solution = new LUDecompositionImpl(coefficients).getSolver().solve(constants);
        assertEquals(2 * solution[0] + 3 * solution[1] -2 * solution[2], constants[0], 1E-12);
        assertEquals(-1 * solution[0] + 7 * solution[1] + 6 * solution[2], constants[1], 1E-12);
        assertEquals(4 * solution[0] - 3 * solution[1] -5 * solution[2], constants[2], 1E-12);   
        
    }

// org.apache.commons.math.linear.RealMatrixImplTest::testGetSubMatrix
    public void testGetSubMatrix() {
        RealMatrix m = new RealMatrixImpl(subTestData);
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

// org.apache.commons.math.linear.RealMatrixImplTest::testCopySubMatrix
    public void testCopySubMatrix() {
        RealMatrix m = new RealMatrixImpl(subTestData);
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

// org.apache.commons.math.linear.RealMatrixImplTest::testGetRowMatrix
    public void testGetRowMatrix() {
        RealMatrix m = new RealMatrixImpl(subTestData);
        RealMatrix mRow0 = new RealMatrixImpl(subRow0);
        RealMatrix mRow3 = new RealMatrixImpl(subRow3);
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

// org.apache.commons.math.linear.RealMatrixImplTest::testSetRowMatrix
    public void testSetRowMatrix() {
        RealMatrix m = new RealMatrixImpl(subTestData);
        RealMatrix mRow3 = new RealMatrixImpl(subRow3);
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

// org.apache.commons.math.linear.RealMatrixImplTest::testGetColumnMatrix
    public void testGetColumnMatrix() {
        RealMatrix m = new RealMatrixImpl(subTestData);
        RealMatrix mColumn1 = new RealMatrixImpl(subColumn1);
        RealMatrix mColumn3 = new RealMatrixImpl(subColumn3);
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

// org.apache.commons.math.linear.RealMatrixImplTest::testSetColumnMatrix
    public void testSetColumnMatrix() {
        RealMatrix m = new RealMatrixImpl(subTestData);
        RealMatrix mColumn3 = new RealMatrixImpl(subColumn3);
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

// org.apache.commons.math.linear.RealMatrixImplTest::testGetRowVector
    public void testGetRowVector() {
        RealMatrix m = new RealMatrixImpl(subTestData);
        RealVector mRow0 = new RealVectorImpl(subRow0[0]);
        RealVector mRow3 = new RealVectorImpl(subRow3[0]);
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

// org.apache.commons.math.linear.RealMatrixImplTest::testSetRowVector
    public void testSetRowVector() {
        RealMatrix m = new RealMatrixImpl(subTestData);
        RealVector mRow3 = new RealVectorImpl(subRow3[0]);
        assertNotSame(mRow3, m.getRowMatrix(0));
        m.setRowVector(0, mRow3);
        assertEquals(mRow3, m.getRowVector(0));
        try {
            m.setRowVector(-1, mRow3);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.setRowVector(0, new RealVectorImpl(5));
            fail("Expecting InvalidMatrixException");
        } catch (InvalidMatrixException ex) {
            
        }
    }

// org.apache.commons.math.linear.RealMatrixImplTest::testGetColumnVector
    public void testGetColumnVector() {
        RealMatrix m = new RealMatrixImpl(subTestData);
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

// org.apache.commons.math.linear.RealMatrixImplTest::testSetColumnVector
    public void testSetColumnVector() {
        RealMatrix m = new RealMatrixImpl(subTestData);
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
            m.setColumnVector(0, new RealVectorImpl(5));
            fail("Expecting InvalidMatrixException");
        } catch (InvalidMatrixException ex) {
            
        }
    }

// org.apache.commons.math.linear.RealMatrixImplTest::testGetRow
    public void testGetRow() {
        RealMatrix m = new RealMatrixImpl(subTestData);
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

// org.apache.commons.math.linear.RealMatrixImplTest::testSetRow
    public void testSetRow() {
        RealMatrix m = new RealMatrixImpl(subTestData);
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

// org.apache.commons.math.linear.RealMatrixImplTest::testGetColumn
    public void testGetColumn() {
        RealMatrix m = new RealMatrixImpl(subTestData);
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

// org.apache.commons.math.linear.RealMatrixImplTest::testSetColumn
    public void testSetColumn() {
        RealMatrix m = new RealMatrixImpl(subTestData);
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

// org.apache.commons.math.linear.RealMatrixImplTest::testEqualsAndHashCode
    public void testEqualsAndHashCode() {
        RealMatrixImpl m = new RealMatrixImpl(testData);
        RealMatrixImpl m1 = (RealMatrixImpl) m.copy();
        RealMatrixImpl mt = (RealMatrixImpl) m.transpose();
        assertTrue(m.hashCode() != mt.hashCode());
        assertEquals(m.hashCode(), m1.hashCode());
        assertEquals(m, m);
        assertEquals(m, m1);
        assertFalse(m.equals(null));
        assertFalse(m.equals(mt));
        assertFalse(m.equals(new RealMatrixImpl(bigSingular))); 
    }

// org.apache.commons.math.linear.RealMatrixImplTest::testToString
    public void testToString() {
        RealMatrixImpl m = new RealMatrixImpl(testData);
        assertEquals("RealMatrixImpl{{1.0,2.0,3.0},{2.0,5.0,3.0},{1.0,0.0,8.0}}",
                m.toString());
        m = new RealMatrixImpl();
        assertEquals("RealMatrixImpl{}",
                m.toString());
    }

// org.apache.commons.math.linear.RealMatrixImplTest::testSetSubMatrix
    public void testSetSubMatrix() throws Exception {
        RealMatrixImpl m = new RealMatrixImpl(testData);
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
        RealMatrixImpl m2 = new RealMatrixImpl();
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

// org.apache.commons.math.linear.RealMatrixImplTest::testWalk
    public void testWalk() {
        int rows    = 150;
        int columns = 75;

        RealMatrix m = new RealMatrixImpl(rows, columns);
        m.walkInRowOrder(new SetVisitor());
        GetVisitor getVisitor = new GetVisitor();
        m.walkInOptimizedOrder(getVisitor);
        assertEquals(rows * columns, getVisitor.getCount());

        m = new RealMatrixImpl(rows, columns);
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

        m = new RealMatrixImpl(rows, columns);
        m.walkInColumnOrder(new SetVisitor());
        getVisitor = new GetVisitor();
        m.walkInOptimizedOrder(getVisitor);
        assertEquals(rows * columns, getVisitor.getCount());

        m = new RealMatrixImpl(rows, columns);
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

        m = new RealMatrixImpl(rows, columns);
        m.walkInOptimizedOrder(new SetVisitor());
        getVisitor = new GetVisitor();
        m.walkInRowOrder(getVisitor);
        assertEquals(rows * columns, getVisitor.getCount());

        m = new RealMatrixImpl(rows, columns);
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

        m = new RealMatrixImpl(rows, columns);
        m.walkInOptimizedOrder(new SetVisitor());
        getVisitor = new GetVisitor();
        m.walkInColumnOrder(getVisitor);
        assertEquals(rows * columns, getVisitor.getCount());

        m = new RealMatrixImpl(rows, columns);
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

// org.apache.commons.math.linear.RealVectorImplTest::testConstructors
    public void testConstructors() {

        RealVectorImpl v0 = new RealVectorImpl();
        assertEquals("testData len", 0, v0.getDimension());

        RealVectorImpl v1 = new RealVectorImpl(7);
        assertEquals("testData len", 7, v1.getDimension());
        assertEquals("testData is 0.0 ", 0.0, v1.getEntry(6));

        RealVectorImpl v2 = new RealVectorImpl(5, 1.23);
        assertEquals("testData len", 5, v2.getDimension());
        assertEquals("testData is 1.23 ", 1.23, v2.getEntry(4));

        RealVectorImpl v3 = new RealVectorImpl(vec1);
        assertEquals("testData len", 3, v3.getDimension());
        assertEquals("testData is 2.0 ", 2.0, v3.getEntry(1));

        RealVectorImpl v4 = new RealVectorImpl(vec4, 3, 2);
        assertEquals("testData len", 2, v4.getDimension());
        assertEquals("testData is 4.0 ", 4.0, v4.getEntry(0));
        try {
            new RealVectorImpl(vec4, 8, 3);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ex) {
            
        } catch (Exception e) {
            fail("wrong exception caught");
        }

        RealVector v5_i = new RealVectorImpl(dvec1);
        assertEquals("testData len", 9, v5_i.getDimension());
        assertEquals("testData is 9.0 ", 9.0, v5_i.getEntry(8));

        RealVectorImpl v5 = new RealVectorImpl(dvec1);
        assertEquals("testData len", 9, v5.getDimension());
        assertEquals("testData is 9.0 ", 9.0, v5.getEntry(8));

        RealVectorImpl v6 = new RealVectorImpl(dvec1, 3, 2);
        assertEquals("testData len", 2, v6.getDimension());
        assertEquals("testData is 4.0 ", 4.0, v6.getEntry(0));
        try {
            new RealVectorImpl(dvec1, 8, 3);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ex) {
            
        } catch (Exception e) {
            fail("wrong exception caught");
        }

        RealVectorImpl v7 = new RealVectorImpl(v1);
        assertEquals("testData len", 7, v7.getDimension());
        assertEquals("testData is 0.0 ", 0.0, v7.getEntry(6));

        RealVectorTestImpl v7_i = new RealVectorTestImpl(vec1);

        RealVectorImpl v7_2 = new RealVectorImpl(v7_i);
        assertEquals("testData len", 3, v7_2.getDimension());
        assertEquals("testData is 0.0 ", 2.0d, v7_2.getEntry(1));

        RealVectorImpl v8 = new RealVectorImpl(v1, true);
        assertEquals("testData len", 7, v8.getDimension());
        assertEquals("testData is 0.0 ", 0.0, v8.getEntry(6));
        assertNotSame("testData not same object ", v1.data, v8.data);

        RealVectorImpl v8_2 = new RealVectorImpl(v1, false);
        assertEquals("testData len", 7, v8_2.getDimension());
        assertEquals("testData is 0.0 ", 0.0, v8_2.getEntry(6));
        assertEquals("testData same object ", v1.data, v8_2.data);

        RealVectorImpl v9 = new RealVectorImpl(v1, v3);
        assertEquals("testData len", 10, v9.getDimension());
        assertEquals("testData is 1.0 ", 1.0, v9.getEntry(7));

    }

// org.apache.commons.math.linear.RealVectorImplTest::testDataInOut
    public void testDataInOut() {

        RealVectorImpl v1 = new RealVectorImpl(vec1);
        RealVectorImpl v2 = new RealVectorImpl(vec2);
        RealVectorImpl v4 = new RealVectorImpl(vec4);
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

        RealVectorImpl v_set1 = (RealVectorImpl) v1.copy();
        v_set1.setEntry(1, 11.0);
        assertEquals("testData is 11.0 ", 11.0, v_set1.getEntry(1));
        try {
            v_set1.setEntry(3, 11.0);
            fail("MatrixIndexException expected");
        } catch (MatrixIndexException ex) {
            
        } catch (Exception e) {
            fail("wrong exception caught");
        }

        RealVectorImpl v_set2 = (RealVectorImpl) v4.copy();
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

        RealVectorImpl v_set3 = (RealVectorImpl) v1.copy();
        v_set3.set(13.0);
        assertEquals("testData is 13.0 ", 13.0, v_set3.getEntry(2));

        try {
            v_set3.getEntry(23);
            fail("ArrayIndexOutOfBoundsException expected");
        } catch (ArrayIndexOutOfBoundsException ex) {
            
        } catch (Exception e) {
            fail("wrong exception caught");
        }

        RealVectorImpl v_set4 = (RealVectorImpl) v4.copy();
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

        RealVectorImpl vout10 = (RealVectorImpl) v1.copy();       
        RealVectorImpl vout10_2 = (RealVectorImpl) v1.copy();
        assertEquals(vout10, vout10_2);
        vout10_2.setEntry(0, 1.1);
        assertNotSame(vout10, vout10_2);

    }

// org.apache.commons.math.linear.RealVectorImplTest::testMapFunctions
    public void testMapFunctions() { 
        RealVectorImpl v1 = new RealVectorImpl(vec1);

        
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
        RealVectorImpl vat = new RealVectorImpl(vat_a);

        
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
        RealVectorImpl abs_v = new RealVectorImpl(abs_a);

        
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
        RealVectorImpl cbrt_v = new RealVectorImpl(cbrt_a);

        
        RealVector v_mapCbrt = cbrt_v.mapCbrt();
        double[] result_mapCbrt = {-1.2599210498948732d,0d,1.2599210498948732d};
        assertClose("compare vectors" ,result_mapCbrt,v_mapCbrt.getData(),normTolerance);

        
        RealVector v_mapCbrtToSelf = cbrt_v.copy();
        v_mapCbrtToSelf.mapCbrtToSelf();
        double[] result_mapCbrtToSelf =  {-1.2599210498948732d,0d,1.2599210498948732d};
        assertClose("compare vectors" ,result_mapCbrtToSelf,v_mapCbrtToSelf.getData(),normTolerance);

        double[] ceil_a = {-1.1d, 0.9d, 1.1d};
        RealVectorImpl ceil_v = new RealVectorImpl(ceil_a);

        
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

// org.apache.commons.math.linear.RealVectorImplTest::testBasicFunctions
    public void testBasicFunctions() { 
        RealVectorImpl v1 = new RealVectorImpl(vec1);
        RealVectorImpl v2 = new RealVectorImpl(vec2);
        RealVectorImpl v_null = new RealVectorImpl(vec_null);

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

        
        RealVectorImpl v_add = v1.add(v2);
        double[] result_add = {5d, 7d, 9d};
        assertClose("compare vect" ,v_add.getData(),result_add,normTolerance);

        RealVectorTestImpl vt2 = new RealVectorTestImpl(vec2);
        RealVector v_add_i = v1.add(vt2);
        double[] result_add_i = {5d, 7d, 9d};
        assertClose("compare vect" ,v_add_i.getData(),result_add_i,normTolerance);

        
        RealVectorImpl v_subtract = v1.subtract(v2);
        double[] result_subtract = {-3d, -3d, -3d};
        assertClose("compare vect" ,v_subtract.getData(),result_subtract,normTolerance);

        RealVector v_subtract_i = v1.subtract(vt2);
        double[] result_subtract_i = {-3d, -3d, -3d};
        assertClose("compare vect" ,v_subtract_i.getData(),result_subtract_i,normTolerance);

        
        RealVectorImpl  v_ebeMultiply = v1.ebeMultiply(v2);
        double[] result_ebeMultiply = {4d, 10d, 18d};
        assertClose("compare vect" ,v_ebeMultiply.getData(),result_ebeMultiply,normTolerance);

        RealVector  v_ebeMultiply_2 = v1.ebeMultiply(v2_t);
        double[] result_ebeMultiply_2 = {4d, 10d, 18d};
        assertClose("compare vect" ,v_ebeMultiply_2.getData(),result_ebeMultiply_2,normTolerance);

        
        RealVectorImpl  v_ebeDivide = v1.ebeDivide(v2);
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

        RealVectorImpl v_unitize = (RealVectorImpl)v1.copy();
        v_unitize.unitize();
        assertClose("compare vect" ,v_unitVector_2.getData(),v_unitize.getData(),normTolerance);
        try {
            v_null.unitize();
            fail("Expecting ArithmeticException");
        } catch (ArithmeticException ex) {
            
        } catch (Exception e) {
            fail("wrong exception caught");
        }

        RealVectorImpl v_projection = v1.projection(v2);
        double[] result_projection = {1.662337662337662, 2.0779220779220777, 2.493506493506493};
        assertClose("compare vect", v_projection.getData(), result_projection, normTolerance);

        RealVector v_projection_2 = v1.projection(v2_t);
        double[] result_projection_2 = {1.662337662337662, 2.0779220779220777, 2.493506493506493};
        assertClose("compare vect", v_projection_2.getData(), result_projection_2, normTolerance);

    }

// org.apache.commons.math.linear.RealVectorImplTest::testMisc
    public void testMisc() { 
        RealVectorImpl v1 = new RealVectorImpl(vec1);
        RealVectorImpl v4 = new RealVectorImpl(vec4);
        RealVector v4_2 = new RealVectorImpl(vec4);

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

// org.apache.commons.math.linear.RealVectorImplTest::testPredicates
    public void testPredicates() {

        RealVectorImpl v = new RealVectorImpl(new double[] { 0, 1, 2 });

        assertFalse(v.isNaN());
        v.setEntry(1, Double.NaN);
        assertTrue(v.isNaN());

        assertFalse(v.isInfinite());
        v.setEntry(0, Double.POSITIVE_INFINITY);
        assertFalse(v.isInfinite());
        v.setEntry(1, 1);
        assertTrue(v.isInfinite());

        v.setEntry(0, 0);
        assertEquals(v, new RealVectorImpl(new double[] { 0, 1, 2 }));
        assertNotSame(v, new RealVectorImpl(new double[] { 0, 1, 2 + Math.ulp(2)}));
        assertNotSame(v, new RealVectorImpl(new double[] { 0, 1, 2, 3 }));

        assertEquals(new RealVectorImpl(new double[] { Double.NaN, 1, 2 }).hashCode(),
                     new RealVectorImpl(new double[] { 0, Double.NaN, 2 }).hashCode());

        assertTrue(new RealVectorImpl(new double[] { Double.NaN, 1, 2 }).hashCode() !=
                   new RealVectorImpl(new double[] { 0, 1, 2 }).hashCode());

    }

// org.apache.commons.math.linear.SparseRealMatrixTest::testDimensions
    public void testDimensions() {
        SparseRealMatrix m = createSparseMatrix(testData);
        SparseRealMatrix m2 = createSparseMatrix(testData2);
        assertEquals("testData row dimension", 3, m.getRowDimension());
        assertEquals("testData column dimension", 3, m.getColumnDimension());
        assertTrue("testData is square", m.isSquare());
        assertEquals("testData2 row dimension", m2.getRowDimension(), 2);
        assertEquals("testData2 column dimension", m2.getColumnDimension(), 3);
        assertTrue("testData2 is not square", !m2.isSquare());
    }

// org.apache.commons.math.linear.SparseRealMatrixTest::testCopyFunctions
    public void testCopyFunctions() {
        SparseRealMatrix m1 = createSparseMatrix(testData);
        RealMatrix m2 = m1.copy();
        assertTrue(m2 instanceof SparseRealMatrix);
        assertEquals(((SparseRealMatrix) m2), m1);
        SparseRealMatrix m3 = createSparseMatrix(testData);
        RealMatrix m4 = m3.copy();
        assertTrue(m4 instanceof SparseRealMatrix);
        assertEquals(((SparseRealMatrix) m4), m3);
    }

// org.apache.commons.math.linear.SparseRealMatrixTest::testAdd
    public void testAdd() {
        SparseRealMatrix m = createSparseMatrix(testData);
        SparseRealMatrix mInv = createSparseMatrix(testDataInv);
        SparseRealMatrix mDataPlusInv = createSparseMatrix(testDataPlusInv);
        RealMatrix mPlusMInv = m.add(mInv);
        for (int row = 0; row < m.getRowDimension(); row++) {
            for (int col = 0; col < m.getColumnDimension(); col++) {
                assertEquals("sum entry entry", 
                    mDataPlusInv.getEntry(row, col), mPlusMInv.getEntry(row, col), 
                    entryTolerance);
            }
        }
    }

// org.apache.commons.math.linear.SparseRealMatrixTest::testAddFail
    public void testAddFail() {
        SparseRealMatrix m = createSparseMatrix(testData);
        SparseRealMatrix m2 = createSparseMatrix(testData2);
        try {
            m.add(m2);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ex) {
            ;
        }
    }

// org.apache.commons.math.linear.SparseRealMatrixTest::testNorm
    public void testNorm() {
        SparseRealMatrix m = createSparseMatrix(testData);
        SparseRealMatrix m2 = createSparseMatrix(testData2);
        assertEquals("testData norm", 14d, m.getNorm(), entryTolerance);
        assertEquals("testData2 norm", 7d, m2.getNorm(), entryTolerance);
    }

// org.apache.commons.math.linear.SparseRealMatrixTest::testPlusMinus
    public void testPlusMinus() {
        SparseRealMatrix m = createSparseMatrix(testData);
        SparseRealMatrix n = createSparseMatrix(testDataInv);
        assertClose("m-n = m + -n", m.subtract(n),
            n.scalarMultiply(-1d).add(m), entryTolerance);
        try {
            m.subtract(createSparseMatrix(testData2));
            fail("Expecting illegalArgumentException");
        } catch (IllegalArgumentException ex) {
            ;
        }
    }

// org.apache.commons.math.linear.SparseRealMatrixTest::testMultiply
    public void testMultiply() {
        SparseRealMatrix m = createSparseMatrix(testData);
        SparseRealMatrix mInv = createSparseMatrix(testDataInv);
        SparseRealMatrix identity = createSparseMatrix(id);
        SparseRealMatrix m2 = createSparseMatrix(testData2);
        assertClose("inverse multiply", m.multiply(mInv), identity,
                entryTolerance);
        assertClose("inverse multiply", mInv.multiply(m), identity,
                entryTolerance);
        assertClose("identity multiply", m.multiply(identity), m,
                entryTolerance);
        assertClose("identity multiply", identity.multiply(mInv), mInv,
                entryTolerance);
        assertClose("identity multiply", m2.multiply(identity), m2,
                entryTolerance);
        try {
            m.multiply(createSparseMatrix(bigSingular));
            fail("Expecting illegalArgumentException");
        } catch (IllegalArgumentException ex) {
            ;
        }
    }

// org.apache.commons.math.linear.SparseRealMatrixTest::testMultiply2
    public void testMultiply2() {
        RealMatrix m3 = createSparseMatrix(d3);
        RealMatrix m4 = createSparseMatrix(d4);
        RealMatrix m5 = createSparseMatrix(d5);
        assertClose("m3*m4=m5", m3.multiply(m4), m5, entryTolerance);
    }

// org.apache.commons.math.linear.SparseRealMatrixTest::testTrace
    public void testTrace() {
        RealMatrix m = createSparseMatrix(id);
        assertEquals("identity trace", 3d, m.getTrace(), entryTolerance);
        m = createSparseMatrix(testData2);
        try {
            m.getTrace();
            fail("Expecting NonSquareMatrixException");
        } catch (NonSquareMatrixException ex) {
            ;
        }
    }

// org.apache.commons.math.linear.SparseRealMatrixTest::testScalarAdd
    public void testScalarAdd() {
        RealMatrix m = createSparseMatrix(testData);
        assertClose("scalar add", createSparseMatrix(testDataPlus2), 
            m.scalarAdd(2d), entryTolerance);
    }

// org.apache.commons.math.linear.SparseRealMatrixTest::testOperate
    public void testOperate() {
        RealMatrix m = createSparseMatrix(id);
        assertClose("identity operate", testVector, m.operate(testVector),
                entryTolerance);
        assertClose("identity operate", testVector, m.operate(
                new RealVectorImpl(testVector)).getData(), entryTolerance);
        m = createSparseMatrix(bigSingular);
        try {
            m.operate(testVector);
            fail("Expecting illegalArgumentException");
        } catch (IllegalArgumentException ex) {
            ;
        }
    }

// org.apache.commons.math.linear.SparseRealMatrixTest::testMath209
    public void testMath209() {
        RealMatrix a = createSparseMatrix(new double[][] {
                { 1, 2 }, { 3, 4 }, { 5, 6 } });
        double[] b = a.operate(new double[] { 1, 1 });
        assertEquals(a.getRowDimension(), b.length);
        assertEquals(3.0, b[0], 1.0e-12);
        assertEquals(7.0, b[1], 1.0e-12);
        assertEquals(11.0, b[2], 1.0e-12);
    }

// org.apache.commons.math.linear.SparseRealMatrixTest::testTranspose
    public void testTranspose() {
        
        RealMatrix m = createSparseMatrix(testData); 
        RealMatrix mIT = new LUDecompositionImpl(m).getSolver().getInverse().transpose();
        RealMatrix mTI = new LUDecompositionImpl(m.transpose()).getSolver().getInverse();
        assertClose("inverse-transpose", mIT, mTI, normTolerance);
        m = createSparseMatrix(testData2);
        RealMatrix mt = createSparseMatrix(testData2T);
        assertClose("transpose",mt,m.transpose(),normTolerance);
    }

// org.apache.commons.math.linear.SparseRealMatrixTest::testPremultiplyVector
    public void testPremultiplyVector() {
        RealMatrix m = createSparseMatrix(testData);
        assertClose("premultiply", m.preMultiply(testVector), preMultTest,
            normTolerance);
        assertClose("premultiply", m.preMultiply(
            new RealVectorImpl(testVector).getData()), preMultTest, normTolerance);
        m = createSparseMatrix(bigSingular);
        try {
            m.preMultiply(testVector);
            fail("expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            ;
        }
    }

// org.apache.commons.math.linear.SparseRealMatrixTest::testPremultiply
    public void testPremultiply() {
        RealMatrix m3 = createSparseMatrix(d3);
        RealMatrix m4 = createSparseMatrix(d4);
        RealMatrix m5 = createSparseMatrix(d5);
        assertClose("m3*m4=m5", m4.preMultiply(m3), m5, entryTolerance);

        SparseRealMatrix m = createSparseMatrix(testData);
        SparseRealMatrix mInv = createSparseMatrix(testDataInv);
        SparseRealMatrix identity = createSparseMatrix(id);
        assertClose("inverse multiply", m.preMultiply(mInv), identity,
                entryTolerance);
        assertClose("inverse multiply", mInv.preMultiply(m), identity,
                entryTolerance);
        assertClose("identity multiply", m.preMultiply(identity), m,
                entryTolerance);
        assertClose("identity multiply", identity.preMultiply(mInv), mInv,
                entryTolerance);
        try {
            m.preMultiply(createSparseMatrix(bigSingular));
            fail("Expecting illegalArgumentException");
        } catch (IllegalArgumentException ex) {
            ;
        }
    }

// org.apache.commons.math.linear.SparseRealMatrixTest::testGetVectors
    public void testGetVectors() {
        RealMatrix m = createSparseMatrix(testData);
        assertClose("get row", m.getRow(0), testDataRow1, entryTolerance);
        assertClose("get col", m.getColumn(2), testDataCol3, entryTolerance);
        try {
            m.getRow(10);
            fail("expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            ;
        }
        try {
            m.getColumn(-1);
            fail("expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            ;
        }
    }

// org.apache.commons.math.linear.SparseRealMatrixTest::testGetEntry
    public void testGetEntry() {
        RealMatrix m = createSparseMatrix(testData);
        assertEquals("get entry", m.getEntry(0, 1), 2d, entryTolerance);
        try {
            m.getEntry(10, 4);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
    }

// org.apache.commons.math.linear.SparseRealMatrixTest::testExamples
    public void testExamples() {
        
        double[][] matrixData = { { 1d, 2d, 3d }, { 2d, 5d, 3d } };
        RealMatrix m = createSparseMatrix(matrixData);
        
        double[][] matrixData2 = { { 1d, 2d }, { 2d, 5d }, { 1d, 7d } };
        RealMatrix n = createSparseMatrix(matrixData2);
        
        RealMatrix p = m.multiply(n);
        assertEquals(2, p.getRowDimension());
        assertEquals(2, p.getColumnDimension());
        
        RealMatrix pInverse = new LUDecompositionImpl(p).getSolver().getInverse(); 
        assertEquals(2, pInverse.getRowDimension());
        assertEquals(2, pInverse.getColumnDimension());

        
        double[][] coefficientsData = { { 2, 3, -2 }, { -1, 7, 6 },
                { 4, -3, -5 } };
        RealMatrix coefficients = createSparseMatrix(coefficientsData);
        double[] constants = { 1, -2, 1 };
        double[] solution = new LUDecompositionImpl(coefficients).getSolver().solve(constants);
        assertEquals(2 * solution[0] + 3 * solution[1] - 2 * solution[2],
                constants[0], 1E-12);
        assertEquals(-1 * solution[0] + 7 * solution[1] + 6 * solution[2],
                constants[1], 1E-12);
        assertEquals(4 * solution[0] - 3 * solution[1] - 5 * solution[2],
                constants[2], 1E-12);

    }

// org.apache.commons.math.linear.SparseRealMatrixTest::testSubMatrix
    public void testSubMatrix() {
        RealMatrix m = createSparseMatrix(subTestData);
        RealMatrix mRows23Cols00 = createSparseMatrix(subRows23Cols00);
        RealMatrix mRows00Cols33 = createSparseMatrix(subRows00Cols33);
        RealMatrix mRows01Cols23 = createSparseMatrix(subRows01Cols23);
        RealMatrix mRows02Cols13 = createSparseMatrix(subRows02Cols13);
        RealMatrix mRows03Cols12 = createSparseMatrix(subRows03Cols12);
        RealMatrix mRows03Cols123 = createSparseMatrix(subRows03Cols123);
        RealMatrix mRows20Cols123 = createSparseMatrix(subRows20Cols123);
        RealMatrix mRows31Cols31 = createSparseMatrix(subRows31Cols31);
        assertEquals("Rows23Cols00", mRows23Cols00, m.getSubMatrix(2, 3, 0, 0));
        assertEquals("Rows00Cols33", mRows00Cols33, m.getSubMatrix(0, 0, 3, 3));
        assertEquals("Rows01Cols23", mRows01Cols23, m.getSubMatrix(0, 1, 2, 3));
        assertEquals("Rows02Cols13", mRows02Cols13, 
            m.getSubMatrix(new int[] { 0, 2 }, new int[] { 1, 3 }));
        assertEquals("Rows03Cols12", mRows03Cols12, 
            m.getSubMatrix(new int[] { 0, 3 }, new int[] { 1, 2 }));
        assertEquals("Rows03Cols123", mRows03Cols123, 
            m.getSubMatrix(new int[] { 0, 3 }, new int[] { 1, 2, 3 }));
        assertEquals("Rows20Cols123", mRows20Cols123, 
            m.getSubMatrix(new int[] { 2, 0 }, new int[] { 1, 2, 3 }));
        assertEquals("Rows31Cols31", mRows31Cols31, 
            m.getSubMatrix(new int[] { 3, 1 }, new int[] { 3, 1 }));
        assertEquals("Rows31Cols31", mRows31Cols31, 
            m.getSubMatrix(new int[] { 3, 1 }, new int[] { 3, 1 }));

        try {
            m.getSubMatrix(1, 0, 2, 4);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.getSubMatrix(-1, 1, 2, 2);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.getSubMatrix(1, 0, 2, 2);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.getSubMatrix(1, 0, 2, 4);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.getSubMatrix(new int[] {}, new int[] { 0 });
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.getSubMatrix(new int[] { 0 }, new int[] { 4 });
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
    }

// org.apache.commons.math.linear.SparseRealMatrixTest::testGetRowMatrix
    public void testGetRowMatrix() {
        RealMatrix m = createSparseMatrix(subTestData);
        RealMatrix mRow0 = createSparseMatrix(subRow0);
        RealMatrix mRow3 = createSparseMatrix(subRow3);
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

// org.apache.commons.math.linear.SparseRealMatrixTest::testGetColumnMatrix
    public void testGetColumnMatrix() {
        RealMatrix m = createSparseMatrix(subTestData);
        RealMatrix mColumn1 = createSparseMatrix(subColumn1);
        RealMatrix mColumn3 = createSparseMatrix(subColumn3);
        assertEquals("Column1", mColumn1, m.getColumnMatrix(1));
        assertEquals("Column3", mColumn3, m.getColumnMatrix(3));
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

// org.apache.commons.math.linear.SparseRealMatrixTest::testGetRowVector
    public void testGetRowVector() {
        RealMatrix m = createSparseMatrix(subTestData);
        RealVector mRow0 = new RealVectorImpl(subRow0[0]);
        RealVector mRow3 = new RealVectorImpl(subRow3[0]);
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

// org.apache.commons.math.linear.SparseRealMatrixTest::testGetColumnVector
    public void testGetColumnVector() {
        RealMatrix m = createSparseMatrix(subTestData);
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
