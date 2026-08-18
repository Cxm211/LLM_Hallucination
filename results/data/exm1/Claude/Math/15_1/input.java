// buggy code
    public static double pow(double x, double y) {
        final double lns[] = new double[2];

        if (y == 0.0) {
            return 1.0;
        }

        if (x != x) { // X is NaN
            return x;
        }


        if (x == 0) {
            long bits = Double.doubleToLongBits(x);
            if ((bits & 0x8000000000000000L) != 0) {
                // -zero
                long yi = (long) y;

                if (y < 0 && y == yi && (yi & 1) == 1) {
                    return Double.NEGATIVE_INFINITY;
                }

                if (y > 0 && y == yi && (yi & 1) == 1) {
                    return -0.0;
                }
            }

            if (y < 0) {
                return Double.POSITIVE_INFINITY;
            }
            if (y > 0) {
                return 0.0;
            }

            return Double.NaN;
        }

        if (x == Double.POSITIVE_INFINITY) {
            if (y != y) { // y is NaN
                return y;
            }
            if (y < 0.0) {
                return 0.0;
            } else {
                return Double.POSITIVE_INFINITY;
            }
        }

        if (y == Double.POSITIVE_INFINITY) {
            if (x * x == 1.0) {
                return Double.NaN;
            }

            if (x * x > 1.0) {
                return Double.POSITIVE_INFINITY;
            } else {
                return 0.0;
            }
        }

        if (x == Double.NEGATIVE_INFINITY) {
            if (y != y) { // y is NaN
                return y;
            }

            if (y < 0) {
                long yi = (long) y;
                if (y == yi && (yi & 1) == 1) {
                    return -0.0;
                }

                return 0.0;
            }

            if (y > 0)  {
                long yi = (long) y;
                if (y == yi && (yi & 1) == 1) {
                    return Double.NEGATIVE_INFINITY;
                }

                return Double.POSITIVE_INFINITY;
            }
        }

        if (y == Double.NEGATIVE_INFINITY) {

            if (x * x == 1.0) {
                return Double.NaN;
            }

            if (x * x < 1.0) {
                return Double.POSITIVE_INFINITY;
            } else {
                return 0.0;
            }
        }

        /* Handle special case x<0 */
        if (x < 0) {
            // y is an even integer in this case
            if (y >= TWO_POWER_52 || y <= -TWO_POWER_52) {
                return pow(-x, y);
            }

            if (y == (long) y) {
                // If y is an integer
                return ((long)y & 1) == 0 ? pow(-x, y) : -pow(-x, y);
            } else {
                return Double.NaN;
            }
        }

        /* Split y into ya and yb such that y = ya+yb */
        double ya;
        double yb;
        if (y < 8e298 && y > -8e298) {
            double tmp1 = y * HEX_40000000;
            ya = y + tmp1 - tmp1;
            yb = y - ya;
        } else {
            double tmp1 = y * 9.31322574615478515625E-10;
            double tmp2 = tmp1 * 9.31322574615478515625E-10;
            ya = (tmp1 + tmp2 - tmp1) * HEX_40000000 * HEX_40000000;
            yb = y - ya;
        }

        /* Compute ln(x) */
        final double lores = log(x, lns);
        if (Double.isInfinite(lores)){ // don't allow this to be converted to NaN
            return lores;
        }

        double lna = lns[0];
        double lnb = lns[1];

        /* resplit lns */
        double tmp1 = lna * HEX_40000000;
        double tmp2 = lna + tmp1 - tmp1;
        lnb += lna - tmp2;
        lna = tmp2;

        // y*ln(x) = (aa+ab)
        final double aa = lna * ya;
        final double ab = lna * yb + lnb * ya + lnb * yb;

        lna = aa+ab;
        lnb = -(lna - aa - ab);

        double z = 1.0 / 120.0;
        z = z * lnb + (1.0 / 24.0);
        z = z * lnb + (1.0 / 6.0);
        z = z * lnb + 0.5;
        z = z * lnb + 1.0;
        z = z * lnb;

        final double result = exp(lna, z, null);
        //result = result + result * z;
        return result;
    }

// relevant test
// org.apache.commons.math3.analysis.polynomials.PolynomialsUtilsTest::testJacobiEvaluationAt1
    public void testJacobiEvaluationAt1() {
        for (int v = 0; v < 10; ++v) {
            for (int w = 0; w < 10; ++w) {
                for (int i = 0; i < 10; ++i) {
                    PolynomialFunction jacobi = PolynomialsUtils.createJacobiPolynomial(i, v, w);
                    double binomial = ArithmeticUtils.binomialCoefficient(v + i, i);
                    Assert.assertTrue(Precision.equals(binomial, jacobi.value(1.0), 1));
                }
            }
        }
    }

// org.apache.commons.math3.analysis.polynomials.PolynomialsUtilsTest::testJacobiOrthogonality
    public void testJacobiOrthogonality() {
        for (int v = 0; v < 5; ++v) {
            for (int w = v; w < 5; ++w) {
                final int vv = v;
                final int ww = w;
                UnivariateFunction weight = new UnivariateFunction() {
                    public double value(double x) {
                        return FastMath.pow(1 - x, vv) * FastMath.pow(1 + x, ww);
                    }
                };
                for (int i = 0; i < 10; ++i) {
                    PolynomialFunction pi = PolynomialsUtils.createJacobiPolynomial(i, v, w);
                    for (int j = 0; j <= i; ++j) {
                        PolynomialFunction pj = PolynomialsUtils.createJacobiPolynomial(j, v, w);
                        checkOrthogonality(pi, pj, weight, -1, 1, 0.1, 1.0e-12);
                    }
                }
            }
        }
    }

// org.apache.commons.math3.analysis.polynomials.PolynomialsUtilsTest::testShift
    public void testShift() {
        
        PolynomialFunction f1x = new PolynomialFunction(new double[] { 1, 1, 2 });

        PolynomialFunction f1x1
            = new PolynomialFunction(PolynomialsUtils.shift(f1x.getCoefficients(), 1));
        checkPolynomial(f1x1, "4 + 5 x + 2 x^2");

        PolynomialFunction f1xM1
            = new PolynomialFunction(PolynomialsUtils.shift(f1x.getCoefficients(), -1));
        checkPolynomial(f1xM1, "2 - 3 x + 2 x^2");
        
        PolynomialFunction f1x3
            = new PolynomialFunction(PolynomialsUtils.shift(f1x.getCoefficients(), 3));
        checkPolynomial(f1x3, "22 + 13 x + 2 x^2");

        
        PolynomialFunction f2x = new PolynomialFunction(new double[]{2, 0, 3, 8, 0, 121});

        PolynomialFunction f2x1
            = new PolynomialFunction(PolynomialsUtils.shift(f2x.getCoefficients(), 1));
        checkPolynomial(f2x1, "134 + 635 x + 1237 x^2 + 1218 x^3 + 605 x^4 + 121 x^5");

        PolynomialFunction f2x3
            = new PolynomialFunction(PolynomialsUtils.shift(f2x.getCoefficients(), 3));
        checkPolynomial(f2x3, "29648 + 49239 x + 32745 x^2 + 10898 x^3 + 1815 x^4 + 121 x^5");
    }

// org.apache.commons.math3.analysis.solvers.BisectionSolverTest::testSinZero
    public void testSinZero() {
        UnivariateFunction f = new Sin();
        double result;

        BisectionSolver solver = new BisectionSolver();
        result = solver.solve(100, f, 3, 4);
        Assert.assertEquals(result, FastMath.PI, solver.getAbsoluteAccuracy());

        result = solver.solve(100, f, 1, 4);
        Assert.assertEquals(result, FastMath.PI, solver.getAbsoluteAccuracy());
    }

// org.apache.commons.math3.analysis.solvers.BisectionSolverTest::testQuinticZero
    public void testQuinticZero() {
        UnivariateFunction f = new QuinticFunction();
        double result;

        BisectionSolver solver = new BisectionSolver();
        result = solver.solve(100, f, -0.2, 0.2);
        Assert.assertEquals(result, 0, solver.getAbsoluteAccuracy());

        result = solver.solve(100, f, -0.1, 0.3);
        Assert.assertEquals(result, 0, solver.getAbsoluteAccuracy());

        result = solver.solve(100, f, -0.3, 0.45);
        Assert.assertEquals(result, 0, solver.getAbsoluteAccuracy());

        result = solver.solve(100, f, 0.3, 0.7);
        Assert.assertEquals(result, 0.5, solver.getAbsoluteAccuracy());

        result = solver.solve(100, f, 0.2, 0.6);
        Assert.assertEquals(result, 0.5, solver.getAbsoluteAccuracy());

        result = solver.solve(100, f, 0.05, 0.95);
        Assert.assertEquals(result, 0.5, solver.getAbsoluteAccuracy());

        result = solver.solve(100, f, 0.85, 1.25);
        Assert.assertEquals(result, 1.0, solver.getAbsoluteAccuracy());

        result = solver.solve(100, f, 0.8, 1.2);
        Assert.assertEquals(result, 1.0, solver.getAbsoluteAccuracy());

        result = solver.solve(100, f, 0.85, 1.75);
        Assert.assertEquals(result, 1.0, solver.getAbsoluteAccuracy());

        result = solver.solve(100, f, 0.55, 1.45);
        Assert.assertEquals(result, 1.0, solver.getAbsoluteAccuracy());

        result = solver.solve(100, f, 0.85, 5);
        Assert.assertEquals(result, 1.0, solver.getAbsoluteAccuracy());

        Assert.assertTrue(solver.getEvaluations() > 0);
    }

// org.apache.commons.math3.analysis.solvers.BisectionSolverTest::testMath369
    public void testMath369() {
        UnivariateFunction f = new Sin();
        BisectionSolver solver = new BisectionSolver();
        Assert.assertEquals(FastMath.PI, solver.solve(100, f, 3.0, 3.2, 3.1), solver.getAbsoluteAccuracy());
    }

// org.apache.commons.math3.analysis.solvers.BracketingNthOrderBrentSolverTest::testInsufficientOrder1
    public void testInsufficientOrder1() {
        new BracketingNthOrderBrentSolver(1.0e-10, 1);
    }

// org.apache.commons.math3.analysis.solvers.BracketingNthOrderBrentSolverTest::testInsufficientOrder2
    public void testInsufficientOrder2() {
        new BracketingNthOrderBrentSolver(1.0e-10, 1.0e-10, 1);
    }

// org.apache.commons.math3.analysis.solvers.BracketingNthOrderBrentSolverTest::testInsufficientOrder3
    public void testInsufficientOrder3() {
        new BracketingNthOrderBrentSolver(1.0e-10, 1.0e-10, 1.0e-10, 1);
    }

// org.apache.commons.math3.analysis.solvers.BracketingNthOrderBrentSolverTest::testConstructorsOK
    public void testConstructorsOK() {
        Assert.assertEquals(2, new BracketingNthOrderBrentSolver(1.0e-10, 2).getMaximalOrder());
        Assert.assertEquals(2, new BracketingNthOrderBrentSolver(1.0e-10, 1.0e-10, 2).getMaximalOrder());
        Assert.assertEquals(2, new BracketingNthOrderBrentSolver(1.0e-10, 1.0e-10, 1.0e-10, 2).getMaximalOrder());
    }

// org.apache.commons.math3.analysis.solvers.BracketingNthOrderBrentSolverTest::testConvergenceOnFunctionAccuracy
    public void testConvergenceOnFunctionAccuracy() {
        BracketingNthOrderBrentSolver solver =
                new BracketingNthOrderBrentSolver(1.0e-12, 1.0e-10, 0.001, 3);
        QuinticFunction f = new QuinticFunction();
        double result = solver.solve(20, f, 0.2, 0.9, 0.4, AllowedSolution.BELOW_SIDE);
        Assert.assertEquals(0, f.value(result), solver.getFunctionValueAccuracy());
        Assert.assertTrue(f.value(result) <= 0);
        Assert.assertTrue(result - 0.5 > solver.getAbsoluteAccuracy());
        result = solver.solve(20, f, -0.9, -0.2,  -0.4, AllowedSolution.ABOVE_SIDE);
        Assert.assertEquals(0, f.value(result), solver.getFunctionValueAccuracy());
        Assert.assertTrue(f.value(result) >= 0);
        Assert.assertTrue(result + 0.5 < -solver.getAbsoluteAccuracy());
    }

// org.apache.commons.math3.analysis.solvers.BracketingNthOrderBrentSolverTest::testIssue716
    public void testIssue716() {
        BracketingNthOrderBrentSolver solver =
                new BracketingNthOrderBrentSolver(1.0e-12, 1.0e-10, 1.0e-22, 5);
        UnivariateFunction sharpTurn = new UnivariateFunction() {
            public double value(double x) {
                return (2 * x + 1) / (1.0e9 * (x + 1));
            }
        };
        double result = solver.solve(100, sharpTurn, -0.9999999, 30, 15, AllowedSolution.RIGHT_SIDE);
        Assert.assertEquals(0, sharpTurn.value(result), solver.getFunctionValueAccuracy());
        Assert.assertTrue(sharpTurn.value(result) >= 0);
        Assert.assertEquals(-0.5, result, 1.0e-10);
    }

// org.apache.commons.math3.analysis.solvers.BracketingNthOrderBrentSolverTest::testFasterThanNewton
    public void testFasterThanNewton() {
        
        
        
        
        
        
        compare(new TestFunction(0.0, -2, 2) {
            @Override
            public DerivativeStructure value(DerivativeStructure x) {
                return x.sin().subtract(x.multiply(0.5));
            }
        });
        compare(new TestFunction(6.3087771299726890947, -5, 10) {
            @Override
            public DerivativeStructure value(DerivativeStructure x) {
                return x.pow(5).add(x).subtract(10000);
            }
        });
        compare(new TestFunction(9.6335955628326951924, 0.001, 10) {
            @Override
            public DerivativeStructure value(DerivativeStructure x) {
                return x.sqrt().subtract(x.reciprocal()).subtract(3);
            }
        });
        compare(new TestFunction(2.8424389537844470678, -5, 5) {
            @Override
            public DerivativeStructure value(DerivativeStructure x) {
                return x.exp().add(x).subtract(20);
            }
        });
        compare(new TestFunction(8.3094326942315717953, 0.001, 10) {
            @Override
            public DerivativeStructure value(DerivativeStructure x) {
                return x.log().add(x.sqrt()).subtract(5);
            }
        });
        compare(new TestFunction(1.4655712318767680266, -0.5, 1.5) {
            @Override
            public DerivativeStructure value(DerivativeStructure x) {
                return x.subtract(1).multiply(x).multiply(x).subtract(1);
            }
        });

    }

// org.apache.commons.math3.analysis.solvers.BrentSolverTest::testSinZero
    public void testSinZero() {
        
        
        
        UnivariateFunction f = new Sin();
        double result;
        UnivariateSolver solver = new BrentSolver();
        
        result = solver.solve(100, f, 3, 4);
        
        
        Assert.assertEquals(result, FastMath.PI, solver.getAbsoluteAccuracy());
        Assert.assertTrue(solver.getEvaluations() <= 7);
        
        result = solver.solve(100, f, 1, 4);
        
        
        Assert.assertEquals(result, FastMath.PI, solver.getAbsoluteAccuracy());
        Assert.assertTrue(solver.getEvaluations() <= 8);
    }

// org.apache.commons.math3.analysis.solvers.BrentSolverTest::testQuinticZero
    public void testQuinticZero() {
        
        
        
        
        
        
        
        UnivariateFunction f = new QuinticFunction();
        double result;
        
        UnivariateSolver solver = new BrentSolver();
        
        
        result = solver.solve(100, f, -0.2, 0.2);
        
        
        Assert.assertEquals(result, 0, solver.getAbsoluteAccuracy());
        Assert.assertTrue(solver.getEvaluations() <= 3);
        
        
        result = solver.solve(100, f, -0.1, 0.3);
        
        
        Assert.assertEquals(result, 0, solver.getAbsoluteAccuracy());
        
        Assert.assertTrue(solver.getEvaluations() <= 7);
        
        result = solver.solve(100, f, -0.3, 0.45);
        
        
        Assert.assertEquals(result, 0, solver.getAbsoluteAccuracy());
        
        Assert.assertTrue(solver.getEvaluations() <= 8);
        
        result = solver.solve(100, f, 0.3, 0.7);
        
        
        Assert.assertEquals(result, 0.5, solver.getAbsoluteAccuracy());
        
        Assert.assertTrue(solver.getEvaluations() <= 9);
        
        result = solver.solve(100, f, 0.2, 0.6);
        
        
        Assert.assertEquals(result, 0.5, solver.getAbsoluteAccuracy());
        Assert.assertTrue(solver.getEvaluations() <= 10);
        
        result = solver.solve(100, f, 0.05, 0.95);
        
        
        Assert.assertEquals(result, 0.5, solver.getAbsoluteAccuracy());
        Assert.assertTrue(solver.getEvaluations() <= 11);
        
        
        result = solver.solve(100, f, 0.85, 1.25);
        
        
        Assert.assertEquals(result, 1.0, solver.getAbsoluteAccuracy());
        Assert.assertTrue(solver.getEvaluations() <= 11);
        
        result = solver.solve(100, f, 0.8, 1.2);
        
        
        Assert.assertEquals(result, 1.0, solver.getAbsoluteAccuracy());
        Assert.assertTrue(solver.getEvaluations() <= 11);
        
        result = solver.solve(100, f, 0.85, 1.75);
        
        
        Assert.assertEquals(result, 1.0, solver.getAbsoluteAccuracy());
        Assert.assertTrue(solver.getEvaluations() <= 13);
        
        result = solver.solve(100, f, 0.55, 1.45);
        
        
        Assert.assertEquals(result, 1.0, solver.getAbsoluteAccuracy());
        Assert.assertTrue(solver.getEvaluations() <= 10);
        
        result = solver.solve(100, f, 0.85, 5);
        
       
        Assert.assertEquals(result, 1.0, solver.getAbsoluteAccuracy());
        Assert.assertTrue(solver.getEvaluations() <= 15);

        try {
            result = solver.solve(5, f, 0.85, 5);
            Assert.fail("Expected TooManyEvaluationsException");
        } catch (TooManyEvaluationsException e) {
            
        }
    }

// org.apache.commons.math3.analysis.solvers.BrentSolverTest::testRootEndpoints
    public void testRootEndpoints() {
        UnivariateFunction f = new Sin();
        BrentSolver solver = new BrentSolver();

        
        double result = solver.solve(100, f, FastMath.PI, 4);
        Assert.assertEquals(FastMath.PI, result, solver.getAbsoluteAccuracy());

        result = solver.solve(100, f, 3, FastMath.PI);
        Assert.assertEquals(FastMath.PI, result, solver.getAbsoluteAccuracy());

        result = solver.solve(100, f, FastMath.PI, 4, 3.5);
        Assert.assertEquals(FastMath.PI, result, solver.getAbsoluteAccuracy());

        result = solver.solve(100, f, 3, FastMath.PI, 3.07);
        Assert.assertEquals(FastMath.PI, result, solver.getAbsoluteAccuracy());
    }

// org.apache.commons.math3.analysis.solvers.BrentSolverTest::testBadEndpoints
    public void testBadEndpoints() {
        UnivariateFunction f = new Sin();
        BrentSolver solver = new BrentSolver();
        try {  
            solver.solve(100, f, 1, -1);
            Assert.fail("Expecting NumberIsTooLargeException - bad interval");
        } catch (NumberIsTooLargeException ex) {
            
        }
        try {  
            solver.solve(100, f, 1, 1.5);
            Assert.fail("Expecting NoBracketingException - non-bracketing");
        } catch (NoBracketingException ex) {
            
        }
        try {  
            solver.solve(100, f, 1, 1.5, 1.2);
            Assert.fail("Expecting NoBracketingException - non-bracketing");
        } catch (NoBracketingException ex) {
            
        }
    }

// org.apache.commons.math3.analysis.solvers.BrentSolverTest::testInitialGuess
    public void testInitialGuess() {
        MonitoredFunction f = new MonitoredFunction(new QuinticFunction());
        BrentSolver solver = new BrentSolver();
        double result;

        
        result = solver.solve(100, f, 0.6, 7.0);
        Assert.assertEquals(result, 1.0, solver.getAbsoluteAccuracy());
        int referenceCallsCount = f.getCallsCount();
        Assert.assertTrue(referenceCallsCount >= 13);

        
        try {
          result = solver.solve(100, f, 0.6, 7.0, 0.0);
          Assert.fail("a NumberIsTooLargeException was expected");
        } catch (NumberIsTooLargeException iae) {
            
        }

        
        f.setCallsCount(0);
        result = solver.solve(100, f, 0.6, 7.0, 0.61);
        Assert.assertEquals(result, 1.0, solver.getAbsoluteAccuracy());
        Assert.assertTrue(f.getCallsCount() > referenceCallsCount);

        
        f.setCallsCount(0);
        result = solver.solve(100, f, 0.6, 7.0, 0.999999);
        Assert.assertEquals(result, 1.0, solver.getAbsoluteAccuracy());
        Assert.assertTrue(f.getCallsCount() < referenceCallsCount);

        
        f.setCallsCount(0);
        result = solver.solve(100, f, 0.6, 7.0, 1.0);
        Assert.assertEquals(result, 1.0, solver.getAbsoluteAccuracy());
        Assert.assertEquals(1, solver.getEvaluations());
        Assert.assertEquals(1, f.getCallsCount());
    }

// org.apache.commons.math3.analysis.solvers.BrentSolverTest::testMath832
    public void testMath832() {
        final UnivariateFunction f = new UnivariateFunction() {
                private final UnivariateDifferentiableFunction sqrt = new Sqrt();
                private final UnivariateDifferentiableFunction inv = new Inverse();
                private final UnivariateDifferentiableFunction func
                    = FunctionUtils.add(FunctionUtils.multiply(new Constant(1e2), sqrt),
                                        FunctionUtils.multiply(new Constant(1e6), inv),
                                        FunctionUtils.multiply(new Constant(1e4),
                                                               FunctionUtils.compose(inv, sqrt)));

                public double value(double x) {
                    return func.value(new DerivativeStructure(1, 1, 0, x)).getPartialDerivative(1);
                }

            };

        BrentSolver solver = new BrentSolver();
        final double result = solver.solve(99, f, 1, 1e30, 1 + 1e-10);
        Assert.assertEquals(804.93558250, result, 1e-8);
    }

// org.apache.commons.math3.analysis.solvers.LaguerreSolverTest::testLinearFunction
    public void testLinearFunction() {
        double min, max, expected, result, tolerance;

        
        double coefficients[] = { -1.0, 4.0 };
        PolynomialFunction f = new PolynomialFunction(coefficients);
        LaguerreSolver solver = new LaguerreSolver();

        min = 0.0; max = 1.0; expected = 0.25;
        tolerance = FastMath.max(solver.getAbsoluteAccuracy(),
                    FastMath.abs(expected * solver.getRelativeAccuracy()));
        result = solver.solve(100, f, min, max);
        Assert.assertEquals(expected, result, tolerance);
    }

// org.apache.commons.math3.analysis.solvers.LaguerreSolverTest::testQuadraticFunction
    public void testQuadraticFunction() {
        double min, max, expected, result, tolerance;

        
        double coefficients[] = { -3.0, 5.0, 2.0 };
        PolynomialFunction f = new PolynomialFunction(coefficients);
        LaguerreSolver solver = new LaguerreSolver();

        min = 0.0; max = 2.0; expected = 0.5;
        tolerance = FastMath.max(solver.getAbsoluteAccuracy(),
                    FastMath.abs(expected * solver.getRelativeAccuracy()));
        result = solver.solve(100, f, min, max);
        Assert.assertEquals(expected, result, tolerance);

        min = -4.0; max = -1.0; expected = -3.0;
        tolerance = FastMath.max(solver.getAbsoluteAccuracy(),
                    FastMath.abs(expected * solver.getRelativeAccuracy()));
        result = solver.solve(100, f, min, max);
        Assert.assertEquals(expected, result, tolerance);
    }

// org.apache.commons.math3.analysis.solvers.LaguerreSolverTest::testQuinticFunction
    public void testQuinticFunction() {
        double min, max, expected, result, tolerance;

        
        double coefficients[] = { -12.0, -1.0, 1.0, -12.0, -1.0, 1.0 };
        PolynomialFunction f = new PolynomialFunction(coefficients);
        LaguerreSolver solver = new LaguerreSolver();

        min = -2.0; max = 2.0; expected = -1.0;
        tolerance = FastMath.max(solver.getAbsoluteAccuracy(),
                    FastMath.abs(expected * solver.getRelativeAccuracy()));
        result = solver.solve(100, f, min, max);
        Assert.assertEquals(expected, result, tolerance);

        min = -5.0; max = -2.5; expected = -3.0;
        tolerance = FastMath.max(solver.getAbsoluteAccuracy(),
                    FastMath.abs(expected * solver.getRelativeAccuracy()));
        result = solver.solve(100, f, min, max);
        Assert.assertEquals(expected, result, tolerance);

        min = 3.0; max = 6.0; expected = 4.0;
        tolerance = FastMath.max(solver.getAbsoluteAccuracy(),
                    FastMath.abs(expected * solver.getRelativeAccuracy()));
        result = solver.solve(100, f, min, max);
        Assert.assertEquals(expected, result, tolerance);
    }

// org.apache.commons.math3.analysis.solvers.LaguerreSolverTest::testQuinticFunction2
    public void testQuinticFunction2() {
        
        final double[] coefficients = { 4.0, 0.0, 1.0, 4.0, 0.0, 1.0 };
        final LaguerreSolver solver = new LaguerreSolver();
        final Complex[] result = solver.solveAllComplex(coefficients, 0);

        for (Complex expected : new Complex[] { new Complex(0, -2),
                                                new Complex(0, 2),
                                                new Complex(0.5, 0.5 * FastMath.sqrt(3)),
                                                new Complex(-1, 0),
                                                new Complex(0.5, -0.5 * FastMath.sqrt(3.0)) }) {
            final double tolerance = FastMath.max(solver.getAbsoluteAccuracy(),
                                                  FastMath.abs(expected.abs() * solver.getRelativeAccuracy()));
            TestUtils.assertContains(result, expected, tolerance);
        }
    }

// org.apache.commons.math3.analysis.solvers.LaguerreSolverTest::testParameters
    public void testParameters() {
        double coefficients[] = { -3.0, 5.0, 2.0 };
        PolynomialFunction f = new PolynomialFunction(coefficients);
        LaguerreSolver solver = new LaguerreSolver();

        try {
            
            solver.solve(100, f, 1, -1);
            Assert.fail("Expecting NumberIsTooLargeException - bad interval");
        } catch (NumberIsTooLargeException ex) {
            
        }
        try {
            
            solver.solve(100, f, 2, 3);
            Assert.fail("Expecting NoBracketingException - no bracketing");
        } catch (NoBracketingException ex) {
            
        }
    }

// org.apache.commons.math3.analysis.solvers.MullerSolver2Test::testSinFunction
    public void testSinFunction() {
        UnivariateFunction f = new Sin();
        UnivariateSolver solver = new MullerSolver2();
        double min, max, expected, result, tolerance;

        min = 3.0; max = 4.0; expected = FastMath.PI;
        tolerance = FastMath.max(solver.getAbsoluteAccuracy(),
                    FastMath.abs(expected * solver.getRelativeAccuracy()));
        result = solver.solve(100, f, min, max);
        Assert.assertEquals(expected, result, tolerance);

        min = -1.0; max = 1.5; expected = 0.0;
        tolerance = FastMath.max(solver.getAbsoluteAccuracy(),
                    FastMath.abs(expected * solver.getRelativeAccuracy()));
        result = solver.solve(100, f, min, max);
        Assert.assertEquals(expected, result, tolerance);
    }

// org.apache.commons.math3.analysis.solvers.MullerSolver2Test::testQuinticFunction
    public void testQuinticFunction() {
        UnivariateFunction f = new QuinticFunction();
        UnivariateSolver solver = new MullerSolver2();
        double min, max, expected, result, tolerance;

        min = -0.4; max = 0.2; expected = 0.0;
        tolerance = FastMath.max(solver.getAbsoluteAccuracy(),
                    FastMath.abs(expected * solver.getRelativeAccuracy()));
        result = solver.solve(100, f, min, max);
        Assert.assertEquals(expected, result, tolerance);

        min = 0.75; max = 1.5; expected = 1.0;
        tolerance = FastMath.max(solver.getAbsoluteAccuracy(),
                    FastMath.abs(expected * solver.getRelativeAccuracy()));
        result = solver.solve(100, f, min, max);
        Assert.assertEquals(expected, result, tolerance);

        min = -0.9; max = -0.2; expected = -0.5;
        tolerance = FastMath.max(solver.getAbsoluteAccuracy(),
                    FastMath.abs(expected * solver.getRelativeAccuracy()));
        result = solver.solve(100, f, min, max);
        Assert.assertEquals(expected, result, tolerance);
    }

// org.apache.commons.math3.analysis.solvers.MullerSolver2Test::testExpm1Function
    public void testExpm1Function() {
        UnivariateFunction f = new Expm1();
        UnivariateSolver solver = new MullerSolver2();
        double min, max, expected, result, tolerance;

        min = -1.0; max = 2.0; expected = 0.0;
        tolerance = FastMath.max(solver.getAbsoluteAccuracy(),
                    FastMath.abs(expected * solver.getRelativeAccuracy()));
        result = solver.solve(100, f, min, max);
        Assert.assertEquals(expected, result, tolerance);

        min = -20.0; max = 10.0; expected = 0.0;
        tolerance = FastMath.max(solver.getAbsoluteAccuracy(),
                    FastMath.abs(expected * solver.getRelativeAccuracy()));
        result = solver.solve(100, f, min, max);
        Assert.assertEquals(expected, result, tolerance);

        min = -50.0; max = 100.0; expected = 0.0;
        tolerance = FastMath.max(solver.getAbsoluteAccuracy(),
                    FastMath.abs(expected * solver.getRelativeAccuracy()));
        result = solver.solve(100, f, min, max);
        Assert.assertEquals(expected, result, tolerance);
    }

// org.apache.commons.math3.analysis.solvers.MullerSolver2Test::testParameters
    public void testParameters() {
        UnivariateFunction f = new Sin();
        UnivariateSolver solver = new MullerSolver2();

        try {
            
            solver.solve(100, f, 1, -1);
            Assert.fail("Expecting NumberIsTooLargeException - bad interval");
        } catch (NumberIsTooLargeException ex) {
            
        }
        try {
            
            solver.solve(100, f, 2, 3);
            Assert.fail("Expecting NoBracketingException - no bracketing");
        } catch (NoBracketingException ex) {
            
        }
    }

// org.apache.commons.math3.analysis.solvers.MullerSolverTest::testSinFunction
    public void testSinFunction() {
        UnivariateFunction f = new Sin();
        UnivariateSolver solver = new MullerSolver();
        double min, max, expected, result, tolerance;

        min = 3.0; max = 4.0; expected = FastMath.PI;
        tolerance = FastMath.max(solver.getAbsoluteAccuracy(),
                    FastMath.abs(expected * solver.getRelativeAccuracy()));
        result = solver.solve(100, f, min, max);
        Assert.assertEquals(expected, result, tolerance);

        min = -1.0; max = 1.5; expected = 0.0;
        tolerance = FastMath.max(solver.getAbsoluteAccuracy(),
                    FastMath.abs(expected * solver.getRelativeAccuracy()));
        result = solver.solve(100, f, min, max);
        Assert.assertEquals(expected, result, tolerance);
    }

// org.apache.commons.math3.analysis.solvers.MullerSolverTest::testQuinticFunction
    public void testQuinticFunction() {
        UnivariateFunction f = new QuinticFunction();
        UnivariateSolver solver = new MullerSolver();
        double min, max, expected, result, tolerance;

        min = -0.4; max = 0.2; expected = 0.0;
        tolerance = FastMath.max(solver.getAbsoluteAccuracy(),
                    FastMath.abs(expected * solver.getRelativeAccuracy()));
        result = solver.solve(100, f, min, max);
        Assert.assertEquals(expected, result, tolerance);

        min = 0.75; max = 1.5; expected = 1.0;
        tolerance = FastMath.max(solver.getAbsoluteAccuracy(),
                    FastMath.abs(expected * solver.getRelativeAccuracy()));
        result = solver.solve(100, f, min, max);
        Assert.assertEquals(expected, result, tolerance);

        min = -0.9; max = -0.2; expected = -0.5;
        tolerance = FastMath.max(solver.getAbsoluteAccuracy(),
                    FastMath.abs(expected * solver.getRelativeAccuracy()));
        result = solver.solve(100, f, min, max);
        Assert.assertEquals(expected, result, tolerance);
    }

// org.apache.commons.math3.analysis.solvers.MullerSolverTest::testExpm1Function
    public void testExpm1Function() {
        UnivariateFunction f = new Expm1();
        UnivariateSolver solver = new MullerSolver();
        double min, max, expected, result, tolerance;

        min = -1.0; max = 2.0; expected = 0.0;
        tolerance = FastMath.max(solver.getAbsoluteAccuracy(),
                    FastMath.abs(expected * solver.getRelativeAccuracy()));
        result = solver.solve(100, f, min, max);
        Assert.assertEquals(expected, result, tolerance);

        min = -20.0; max = 10.0; expected = 0.0;
        tolerance = FastMath.max(solver.getAbsoluteAccuracy(),
                    FastMath.abs(expected * solver.getRelativeAccuracy()));
        result = solver.solve(100, f, min, max);
        Assert.assertEquals(expected, result, tolerance);

        min = -50.0; max = 100.0; expected = 0.0;
        tolerance = FastMath.max(solver.getAbsoluteAccuracy(),
                    FastMath.abs(expected * solver.getRelativeAccuracy()));
        result = solver.solve(100, f, min, max);
        Assert.assertEquals(expected, result, tolerance);
    }

// org.apache.commons.math3.analysis.solvers.MullerSolverTest::testParameters
    public void testParameters() {
        UnivariateFunction f = new Sin();
        UnivariateSolver solver = new MullerSolver();

        try {
            
            double root = solver.solve(100, f, 1, -1);
            System.out.println("root=" + root);
            Assert.fail("Expecting NumberIsTooLargeException - bad interval");
        } catch (NumberIsTooLargeException ex) {
            
        }
        try {
            
            solver.solve(100, f, 2, 3);
            Assert.fail("Expecting NoBracketingException - no bracketing");
        } catch (NoBracketingException ex) {
            
        }
    }

// org.apache.commons.math3.analysis.solvers.NewtonRaphsonSolverTest::testSinZero
    public void testSinZero() {
        UnivariateDifferentiableFunction f = new Sin();
        double result;

        NewtonRaphsonSolver solver = new NewtonRaphsonSolver();
        result = solver.solve(100, f, 3, 4);
        Assert.assertEquals(result, FastMath.PI, solver.getAbsoluteAccuracy());

        result = solver.solve(100, f, 1, 4);
        Assert.assertEquals(result, FastMath.PI, solver.getAbsoluteAccuracy());

        Assert.assertTrue(solver.getEvaluations() > 0);
    }

// org.apache.commons.math3.analysis.solvers.NewtonRaphsonSolverTest::testQuinticZero
    public void testQuinticZero() {
        final UnivariateDifferentiableFunction f = new QuinticFunction();
        double result;

        NewtonRaphsonSolver solver = new NewtonRaphsonSolver();
        result = solver.solve(100, f, -0.2, 0.2);
        Assert.assertEquals(result, 0, solver.getAbsoluteAccuracy());

        result = solver.solve(100, f, -0.1, 0.3);
        Assert.assertEquals(result, 0, solver.getAbsoluteAccuracy());

        result = solver.solve(100, f, -0.3, 0.45);
        Assert.assertEquals(result, 0, solver.getAbsoluteAccuracy());

        result = solver.solve(100, f, 0.3, 0.7);
        Assert.assertEquals(result, 0.5, solver.getAbsoluteAccuracy());

        result = solver.solve(100, f, 0.2, 0.6);
        Assert.assertEquals(result, 0.5, solver.getAbsoluteAccuracy());

        result = solver.solve(100, f, 0.05, 0.95);
        Assert.assertEquals(result, 0.5, solver.getAbsoluteAccuracy());

        result = solver.solve(100, f, 0.85, 1.25);
        Assert.assertEquals(result, 1.0, solver.getAbsoluteAccuracy());

        result = solver.solve(100, f, 0.8, 1.2);
        Assert.assertEquals(result, 1.0, solver.getAbsoluteAccuracy());

        result = solver.solve(100, f, 0.85, 1.75);
        Assert.assertEquals(result, 1.0, solver.getAbsoluteAccuracy());

        result = solver.solve(100, f, 0.55, 1.45);
        Assert.assertEquals(result, 1.0, solver.getAbsoluteAccuracy());

        result = solver.solve(100, f, 0.85, 5);
        Assert.assertEquals(result, 1.0, solver.getAbsoluteAccuracy());
    }

// org.apache.commons.math3.analysis.solvers.NewtonSolverTest::testSinZero
    public void testSinZero() {
        DifferentiableUnivariateFunction f = new Sin();
        double result;

        NewtonSolver solver = new NewtonSolver();
        result = solver.solve(100, f, 3, 4);
        Assert.assertEquals(result, FastMath.PI, solver.getAbsoluteAccuracy());

        result = solver.solve(100, f, 1, 4);
        Assert.assertEquals(result, FastMath.PI, solver.getAbsoluteAccuracy());

        Assert.assertTrue(solver.getEvaluations() > 0);
    }

// org.apache.commons.math3.analysis.solvers.NewtonSolverTest::testQuinticZero
    public void testQuinticZero() {
        final UnivariateDifferentiableFunction q = new QuinticFunction();
        DifferentiableUnivariateFunction f = new DifferentiableUnivariateFunction() {

            public double value(double x) {
                return q.value(x);
            }

            public UnivariateFunction derivative() {
                return new UnivariateFunction() {
                    public double value(double x) {
                        return q.value(new DerivativeStructure(1, 1, 0, x)).getPartialDerivative(1);
                    }
                };
            }

        };
        double result;

        NewtonSolver solver = new NewtonSolver();
        result = solver.solve(100, f, -0.2, 0.2);
        Assert.assertEquals(result, 0, solver.getAbsoluteAccuracy());

        result = solver.solve(100, f, -0.1, 0.3);
        Assert.assertEquals(result, 0, solver.getAbsoluteAccuracy());

        result = solver.solve(100, f, -0.3, 0.45);
        Assert.assertEquals(result, 0, solver.getAbsoluteAccuracy());

        result = solver.solve(100, f, 0.3, 0.7);
        Assert.assertEquals(result, 0.5, solver.getAbsoluteAccuracy());

        result = solver.solve(100, f, 0.2, 0.6);
        Assert.assertEquals(result, 0.5, solver.getAbsoluteAccuracy());

        result = solver.solve(100, f, 0.05, 0.95);
        Assert.assertEquals(result, 0.5, solver.getAbsoluteAccuracy());

        result = solver.solve(100, f, 0.85, 1.25);
        Assert.assertEquals(result, 1.0, solver.getAbsoluteAccuracy());

        result = solver.solve(100, f, 0.8, 1.2);
        Assert.assertEquals(result, 1.0, solver.getAbsoluteAccuracy());

        result = solver.solve(100, f, 0.85, 1.75);
        Assert.assertEquals(result, 1.0, solver.getAbsoluteAccuracy());

        result = solver.solve(100, f, 0.55, 1.45);
        Assert.assertEquals(result, 1.0, solver.getAbsoluteAccuracy());

        result = solver.solve(100, f, 0.85, 5);
        Assert.assertEquals(result, 1.0, solver.getAbsoluteAccuracy());
    }

// org.apache.commons.math3.analysis.solvers.RegulaFalsiSolverTest::testIssue631
    public void testIssue631() {
        final UnivariateFunction f = new UnivariateFunction() {
                
                public double value(double x) {
                    return Math.exp(x) - Math.pow(Math.PI, 3.0);
                }
            };

        final UnivariateSolver solver = new RegulaFalsiSolver();
        final double root = solver.solve(3624, f, 1, 10);
        Assert.assertEquals(3.4341896575482003, root, 1e-15);
    }

// org.apache.commons.math3.analysis.solvers.RiddersSolverTest::testSinFunction
    public void testSinFunction() {
        UnivariateFunction f = new Sin();
        UnivariateSolver solver = new RiddersSolver();
        double min, max, expected, result, tolerance;

        min = 3.0; max = 4.0; expected = FastMath.PI;
        tolerance = FastMath.max(solver.getAbsoluteAccuracy(),
                    FastMath.abs(expected * solver.getRelativeAccuracy()));
        result = solver.solve(100, f, min, max);
        Assert.assertEquals(expected, result, tolerance);

        min = -1.0; max = 1.5; expected = 0.0;
        tolerance = FastMath.max(solver.getAbsoluteAccuracy(),
                    FastMath.abs(expected * solver.getRelativeAccuracy()));
        result = solver.solve(100, f, min, max);
        Assert.assertEquals(expected, result, tolerance);
    }

// org.apache.commons.math3.analysis.solvers.RiddersSolverTest::testQuinticFunction
    public void testQuinticFunction() {
        UnivariateFunction f = new QuinticFunction();
        UnivariateSolver solver = new RiddersSolver();
        double min, max, expected, result, tolerance;

        min = -0.4; max = 0.2; expected = 0.0;
        tolerance = FastMath.max(solver.getAbsoluteAccuracy(),
                    FastMath.abs(expected * solver.getRelativeAccuracy()));
        result = solver.solve(100, f, min, max);
        Assert.assertEquals(expected, result, tolerance);

        min = 0.75; max = 1.5; expected = 1.0;
        tolerance = FastMath.max(solver.getAbsoluteAccuracy(),
                    FastMath.abs(expected * solver.getRelativeAccuracy()));
        result = solver.solve(100, f, min, max);
        Assert.assertEquals(expected, result, tolerance);

        min = -0.9; max = -0.2; expected = -0.5;
        tolerance = FastMath.max(solver.getAbsoluteAccuracy(),
                    FastMath.abs(expected * solver.getRelativeAccuracy()));
        result = solver.solve(100, f, min, max);
        Assert.assertEquals(expected, result, tolerance);
    }

// org.apache.commons.math3.analysis.solvers.RiddersSolverTest::testExpm1Function
    public void testExpm1Function() {
        UnivariateFunction f = new Expm1();
        UnivariateSolver solver = new RiddersSolver();
        double min, max, expected, result, tolerance;

        min = -1.0; max = 2.0; expected = 0.0;
        tolerance = FastMath.max(solver.getAbsoluteAccuracy(),
                    FastMath.abs(expected * solver.getRelativeAccuracy()));
        result = solver.solve(100, f, min, max);
        Assert.assertEquals(expected, result, tolerance);

        min = -20.0; max = 10.0; expected = 0.0;
        tolerance = FastMath.max(solver.getAbsoluteAccuracy(),
                    FastMath.abs(expected * solver.getRelativeAccuracy()));
        result = solver.solve(100, f, min, max);
        Assert.assertEquals(expected, result, tolerance);

        min = -50.0; max = 100.0; expected = 0.0;
        tolerance = FastMath.max(solver.getAbsoluteAccuracy(),
                    FastMath.abs(expected * solver.getRelativeAccuracy()));
        result = solver.solve(100, f, min, max);
        Assert.assertEquals(expected, result, tolerance);
    }

// org.apache.commons.math3.analysis.solvers.RiddersSolverTest::testParameters
    public void testParameters() {
        UnivariateFunction f = new Sin();
        UnivariateSolver solver = new RiddersSolver();

        try {
            
            solver.solve(100, f, 1, -1);
            Assert.fail("Expecting NumberIsTooLargeException - bad interval");
        } catch (NumberIsTooLargeException ex) {
            
        }
        try {
            
            solver.solve(100, f, 2, 3);
            Assert.fail("Expecting NoBracketingException - no bracketing");
        } catch (NoBracketingException ex) {
            
        }
    }

// org.apache.commons.math3.analysis.solvers.UnivariateSolverUtilsTest::testSolveNull
    public void testSolveNull() {
        UnivariateSolverUtils.solve(null, 0.0, 4.0);
    }

// org.apache.commons.math3.analysis.solvers.UnivariateSolverUtilsTest::testSolveBadEndpoints
    public void testSolveBadEndpoints() {
        double root = UnivariateSolverUtils.solve(sin, 4.0, -0.1, 1e-6);
        System.out.println("root=" + root);
    }

// org.apache.commons.math3.analysis.solvers.UnivariateSolverUtilsTest::testSolveBadAccuracy
    public void testSolveBadAccuracy() {
        try { 
            UnivariateSolverUtils.solve(sin, 0.0, 4.0, 0.0);

        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math3.analysis.solvers.UnivariateSolverUtilsTest::testSolveSin
    public void testSolveSin() {
        double x = UnivariateSolverUtils.solve(sin, 1.0, 4.0);
        Assert.assertEquals(FastMath.PI, x, 1.0e-4);
    }

// org.apache.commons.math3.analysis.solvers.UnivariateSolverUtilsTest::testSolveAccuracyNull
    public void testSolveAccuracyNull()  {
        double accuracy = 1.0e-6;
        UnivariateSolverUtils.solve(null, 0.0, 4.0, accuracy);
    }

// org.apache.commons.math3.analysis.solvers.UnivariateSolverUtilsTest::testSolveAccuracySin
    public void testSolveAccuracySin() {
        double accuracy = 1.0e-6;
        double x = UnivariateSolverUtils.solve(sin, 1.0,
                4.0, accuracy);
        Assert.assertEquals(FastMath.PI, x, accuracy);
    }

// org.apache.commons.math3.analysis.solvers.UnivariateSolverUtilsTest::testSolveNoRoot
    public void testSolveNoRoot() {
        UnivariateSolverUtils.solve(sin, 1.0, 1.5);
    }

// org.apache.commons.math3.analysis.solvers.UnivariateSolverUtilsTest::testBracketSin
    public void testBracketSin() {
        double[] result = UnivariateSolverUtils.bracket(sin,
                0.0, -2.0, 2.0);
        Assert.assertTrue(sin.value(result[0]) < 0);
        Assert.assertTrue(sin.value(result[1]) > 0);
    }

// org.apache.commons.math3.analysis.solvers.UnivariateSolverUtilsTest::testBracketEndpointRoot
    public void testBracketEndpointRoot() {
        double[] result = UnivariateSolverUtils.bracket(sin, 1.5, 0, 2.0);
        Assert.assertEquals(0.0, sin.value(result[0]), 1.0e-15);
        Assert.assertTrue(sin.value(result[1]) > 0);
    }

// org.apache.commons.math3.analysis.solvers.UnivariateSolverUtilsTest::testNullFunction
    public void testNullFunction() {
        UnivariateSolverUtils.bracket(null, 1.5, 0, 2.0);
    }

// org.apache.commons.math3.analysis.solvers.UnivariateSolverUtilsTest::testBadInitial
    public void testBadInitial() {
        UnivariateSolverUtils.bracket(sin, 2.5, 0, 2.0);
    }

// org.apache.commons.math3.analysis.solvers.UnivariateSolverUtilsTest::testBadEndpoints
    public void testBadEndpoints() {
        
        UnivariateSolverUtils.bracket(sin, 1.5, 2.0, 1.0);
    }

// org.apache.commons.math3.analysis.solvers.UnivariateSolverUtilsTest::testBadMaximumIterations
    public void testBadMaximumIterations() {
        
        UnivariateSolverUtils.bracket(sin, 1.5, 0, 2.0, 0);
    }

// org.apache.commons.math3.analysis.solvers.UnivariateSolverUtilsTest::testMisc
    public void testMisc() {
        UnivariateFunction f = new QuinticFunction();
        double result;
        
        result = UnivariateSolverUtils.solve(f, -0.2, 0.2);
        Assert.assertEquals(result, 0, 1E-8);
        result = UnivariateSolverUtils.solve(f, -0.1, 0.3);
        Assert.assertEquals(result, 0, 1E-8);
        result = UnivariateSolverUtils.solve(f, -0.3, 0.45);
        Assert.assertEquals(result, 0, 1E-6);
        result = UnivariateSolverUtils.solve(f, 0.3, 0.7);
        Assert.assertEquals(result, 0.5, 1E-6);
        result = UnivariateSolverUtils.solve(f, 0.2, 0.6);
        Assert.assertEquals(result, 0.5, 1E-6);
        result = UnivariateSolverUtils.solve(f, 0.05, 0.95);
        Assert.assertEquals(result, 0.5, 1E-6);
        result = UnivariateSolverUtils.solve(f, 0.85, 1.25);
        Assert.assertEquals(result, 1.0, 1E-6);
        result = UnivariateSolverUtils.solve(f, 0.8, 1.2);
        Assert.assertEquals(result, 1.0, 1E-6);
        result = UnivariateSolverUtils.solve(f, 0.85, 1.75);
        Assert.assertEquals(result, 1.0, 1E-6);
        result = UnivariateSolverUtils.solve(f, 0.55, 1.45);
        Assert.assertEquals(result, 1.0, 1E-6);
        result = UnivariateSolverUtils.solve(f, 0.85, 5);
        Assert.assertEquals(result, 1.0, 1E-6);
    }

// org.apache.commons.math3.complex.ComplexTest::testConstructor
    public void testConstructor() {
        Complex z = new Complex(3.0, 4.0);
        Assert.assertEquals(3.0, z.getReal(), 1.0e-5);
        Assert.assertEquals(4.0, z.getImaginary(), 1.0e-5);
    }

// org.apache.commons.math3.complex.ComplexTest::testConstructorNaN
    public void testConstructorNaN() {
        Complex z = new Complex(3.0, Double.NaN);
        Assert.assertTrue(z.isNaN());

        z = new Complex(nan, 4.0);
        Assert.assertTrue(z.isNaN());

        z = new Complex(3.0, 4.0);
        Assert.assertFalse(z.isNaN());
    }

// org.apache.commons.math3.complex.ComplexTest::testAbs
    public void testAbs() {
        Complex z = new Complex(3.0, 4.0);
        Assert.assertEquals(5.0, z.abs(), 1.0e-5);
    }

// org.apache.commons.math3.complex.ComplexTest::testAbsNaN
    public void testAbsNaN() {
        Assert.assertTrue(Double.isNaN(Complex.NaN.abs()));
        Complex z = new Complex(inf, nan);
        Assert.assertTrue(Double.isNaN(z.abs()));
    }

// org.apache.commons.math3.complex.ComplexTest::testAbsInfinite
    public void testAbsInfinite() {
        Complex z = new Complex(inf, 0);
        Assert.assertEquals(inf, z.abs(), 0);
        z = new Complex(0, neginf);
        Assert.assertEquals(inf, z.abs(), 0);
        z = new Complex(inf, neginf);
        Assert.assertEquals(inf, z.abs(), 0);
    }

// org.apache.commons.math3.complex.ComplexTest::testAdd
    public void testAdd() {
        Complex x = new Complex(3.0, 4.0);
        Complex y = new Complex(5.0, 6.0);
        Complex z = x.add(y);
        Assert.assertEquals(8.0, z.getReal(), 1.0e-5);
        Assert.assertEquals(10.0, z.getImaginary(), 1.0e-5);
    }

// org.apache.commons.math3.complex.ComplexTest::testAddNaN
    public void testAddNaN() {
        Complex x = new Complex(3.0, 4.0);
        Complex z = x.add(Complex.NaN);
        Assert.assertSame(Complex.NaN, z);
        z = new Complex(1, nan);
        Complex w = x.add(z);
        Assert.assertSame(Complex.NaN, w);
    }

// org.apache.commons.math3.complex.ComplexTest::testAddInf
    public void testAddInf() {
        Complex x = new Complex(1, 1);
        Complex z = new Complex(inf, 0);
        Complex w = x.add(z);
        Assert.assertEquals(w.getImaginary(), 1, 0);
        Assert.assertEquals(inf, w.getReal(), 0);

        x = new Complex(neginf, 0);
        Assert.assertTrue(Double.isNaN(x.add(z).getReal()));
    }

// org.apache.commons.math3.complex.ComplexTest::testScalarAdd
    public void testScalarAdd() {
        Complex x = new Complex(3.0, 4.0);
        double yDouble = 2.0;
        Complex yComplex = new Complex(yDouble);
        Assert.assertEquals(x.add(yComplex), x.add(yDouble));
    }

// org.apache.commons.math3.complex.ComplexTest::testScalarAddNaN
    public void testScalarAddNaN() {
        Complex x = new Complex(3.0, 4.0);
        double yDouble = Double.NaN;
        Complex yComplex = new Complex(yDouble);
        Assert.assertEquals(x.add(yComplex), x.add(yDouble));
    }

// org.apache.commons.math3.complex.ComplexTest::testScalarAddInf
    public void testScalarAddInf() {
        Complex x = new Complex(1, 1);
        double yDouble = Double.POSITIVE_INFINITY;

        Complex yComplex = new Complex(yDouble);
        Assert.assertEquals(x.add(yComplex), x.add(yDouble));

        x = new Complex(neginf, 0);
        Assert.assertEquals(x.add(yComplex), x.add(yDouble));
    }

// org.apache.commons.math3.complex.ComplexTest::testConjugate
    public void testConjugate() {
        Complex x = new Complex(3.0, 4.0);
        Complex z = x.conjugate();
        Assert.assertEquals(3.0, z.getReal(), 1.0e-5);
        Assert.assertEquals(-4.0, z.getImaginary(), 1.0e-5);
    }

// org.apache.commons.math3.complex.ComplexTest::testConjugateNaN
    public void testConjugateNaN() {
        Complex z = Complex.NaN.conjugate();
        Assert.assertTrue(z.isNaN());
    }

// org.apache.commons.math3.complex.ComplexTest::testConjugateInfiinite
    public void testConjugateInfiinite() {
        Complex z = new Complex(0, inf);
        Assert.assertEquals(neginf, z.conjugate().getImaginary(), 0);
        z = new Complex(0, neginf);
        Assert.assertEquals(inf, z.conjugate().getImaginary(), 0);
    }

// org.apache.commons.math3.complex.ComplexTest::testDivide
    public void testDivide() {
        Complex x = new Complex(3.0, 4.0);
        Complex y = new Complex(5.0, 6.0);
        Complex z = x.divide(y);
        Assert.assertEquals(39.0 / 61.0, z.getReal(), 1.0e-5);
        Assert.assertEquals(2.0 / 61.0, z.getImaginary(), 1.0e-5);
    }

// org.apache.commons.math3.complex.ComplexTest::testDivideReal
    public void testDivideReal() {
        Complex x = new Complex(2d, 3d);
        Complex y = new Complex(2d, 0d);
        Assert.assertEquals(new Complex(1d, 1.5), x.divide(y));

    }

// org.apache.commons.math3.complex.ComplexTest::testDivideImaginary
    public void testDivideImaginary() {
        Complex x = new Complex(2d, 3d);
        Complex y = new Complex(0d, 2d);
        Assert.assertEquals(new Complex(1.5d, -1d), x.divide(y));
    }

// org.apache.commons.math3.complex.ComplexTest::testDivideInf
    public void testDivideInf() {
        Complex x = new Complex(3, 4);
        Complex w = new Complex(neginf, inf);
        Assert.assertTrue(x.divide(w).equals(Complex.ZERO));

        Complex z = w.divide(x);
        Assert.assertTrue(Double.isNaN(z.getReal()));
        Assert.assertEquals(inf, z.getImaginary(), 0);

        w = new Complex(inf, inf);
        z = w.divide(x);
        Assert.assertTrue(Double.isNaN(z.getImaginary()));
        Assert.assertEquals(inf, z.getReal(), 0);

        w = new Complex(1, inf);
        z = w.divide(w);
        Assert.assertTrue(Double.isNaN(z.getReal()));
        Assert.assertTrue(Double.isNaN(z.getImaginary()));
    }

// org.apache.commons.math3.complex.ComplexTest::testDivideZero
    public void testDivideZero() {
        Complex x = new Complex(3.0, 4.0);
        Complex z = x.divide(Complex.ZERO);
        
        Assert.assertEquals(z, Complex.NaN);
    }

// org.apache.commons.math3.complex.ComplexTest::testDivideZeroZero
    public void testDivideZeroZero() {
        Complex x = new Complex(0.0, 0.0);
        Complex z = x.divide(Complex.ZERO);
        Assert.assertEquals(z, Complex.NaN);
    }

// org.apache.commons.math3.complex.ComplexTest::testDivideNaN
    public void testDivideNaN() {
        Complex x = new Complex(3.0, 4.0);
        Complex z = x.divide(Complex.NaN);
        Assert.assertTrue(z.isNaN());
    }

// org.apache.commons.math3.complex.ComplexTest::testDivideNaNInf
    public void testDivideNaNInf() {
       Complex z = oneInf.divide(Complex.ONE);
       Assert.assertTrue(Double.isNaN(z.getReal()));
       Assert.assertEquals(inf, z.getImaginary(), 0);

       z = negInfNegInf.divide(oneNaN);
       Assert.assertTrue(Double.isNaN(z.getReal()));
       Assert.assertTrue(Double.isNaN(z.getImaginary()));

       z = negInfInf.divide(Complex.ONE);
       Assert.assertTrue(Double.isNaN(z.getReal()));
       Assert.assertTrue(Double.isNaN(z.getImaginary()));
    }

// org.apache.commons.math3.complex.ComplexTest::testScalarDivide
    public void testScalarDivide() {
        Complex x = new Complex(3.0, 4.0);
        double yDouble = 2.0;
        Complex yComplex = new Complex(yDouble);
        Assert.assertEquals(x.divide(yComplex), x.divide(yDouble));
    }

// org.apache.commons.math3.complex.ComplexTest::testScalarDivideNaN
    public void testScalarDivideNaN() {
        Complex x = new Complex(3.0, 4.0);
        double yDouble = Double.NaN;
        Complex yComplex = new Complex(yDouble);
        Assert.assertEquals(x.divide(yComplex), x.divide(yDouble));
    }

// org.apache.commons.math3.complex.ComplexTest::testScalarDivideInf
    public void testScalarDivideInf() {
        Complex x = new Complex(1,1);
        double yDouble = Double.POSITIVE_INFINITY;
        Complex yComplex = new Complex(yDouble);
        TestUtils.assertEquals(x.divide(yComplex), x.divide(yDouble), 0);

        yDouble = Double.NEGATIVE_INFINITY;
        yComplex = new Complex(yDouble);
        TestUtils.assertEquals(x.divide(yComplex), x.divide(yDouble), 0);

        x = new Complex(1, Double.NEGATIVE_INFINITY);
        TestUtils.assertEquals(x.divide(yComplex), x.divide(yDouble), 0);
    }

// org.apache.commons.math3.complex.ComplexTest::testScalarDivideZero
    public void testScalarDivideZero() {
        Complex x = new Complex(1,1);
        TestUtils.assertEquals(x.divide(Complex.ZERO), x.divide(0), 0);
    }

// org.apache.commons.math3.complex.ComplexTest::testReciprocal
    public void testReciprocal() {
        Complex z = new Complex(5.0, 6.0);
        Complex act = z.reciprocal();
        double expRe = 5.0 / 61.0;
        double expIm = -6.0 / 61.0;
        Assert.assertEquals(expRe, act.getReal(), FastMath.ulp(expRe));
        Assert.assertEquals(expIm, act.getImaginary(), FastMath.ulp(expIm));
    }

// org.apache.commons.math3.complex.ComplexTest::testReciprocalReal
    public void testReciprocalReal() {
        Complex z = new Complex(-2.0, 0.0);
        Assert.assertEquals(new Complex(-0.5, 0.0), z.reciprocal());
    }

// org.apache.commons.math3.complex.ComplexTest::testReciprocalImaginary
    public void testReciprocalImaginary() {
        Complex z = new Complex(0.0, -2.0);
        Assert.assertEquals(new Complex(0.0, 0.5), z.reciprocal());
    }

// org.apache.commons.math3.complex.ComplexTest::testReciprocalInf
    public void testReciprocalInf() {
        Complex z = new Complex(neginf, inf);
        Assert.assertTrue(z.reciprocal().equals(Complex.ZERO));

        z = new Complex(1, inf).reciprocal();
        Assert.assertEquals(z, Complex.ZERO);
    }

// org.apache.commons.math3.complex.ComplexTest::testReciprocalZero
    public void testReciprocalZero() {
        Assert.assertEquals(Complex.ZERO.reciprocal(), Complex.NaN);
    }

// org.apache.commons.math3.complex.ComplexTest::testReciprocalNaN
    public void testReciprocalNaN() {
        Assert.assertTrue(Complex.NaN.reciprocal().isNaN());
    }

// org.apache.commons.math3.complex.ComplexTest::testMultiply
    public void testMultiply() {
        Complex x = new Complex(3.0, 4.0);
        Complex y = new Complex(5.0, 6.0);
        Complex z = x.multiply(y);
        Assert.assertEquals(-9.0, z.getReal(), 1.0e-5);
        Assert.assertEquals(38.0, z.getImaginary(), 1.0e-5);
    }

// org.apache.commons.math3.complex.ComplexTest::testMultiplyNaN
    public void testMultiplyNaN() {
        Complex x = new Complex(3.0, 4.0);
        Complex z = x.multiply(Complex.NaN);
        Assert.assertSame(Complex.NaN, z);
        z = Complex.NaN.multiply(5);
        Assert.assertSame(Complex.NaN, z);
    }

// org.apache.commons.math3.complex.ComplexTest::testMultiplyInfInf
    public void testMultiplyInfInf() {
        
        Assert.assertTrue(infInf.multiply(infInf).isInfinite());
    }

// org.apache.commons.math3.complex.ComplexTest::testMultiplyNaNInf
    public void testMultiplyNaNInf() {
        Complex z = new Complex(1,1);
        Complex w = z.multiply(infOne);
        Assert.assertEquals(w.getReal(), inf, 0);
        Assert.assertEquals(w.getImaginary(), inf, 0);

        
        Assert.assertTrue(new Complex( 1,0).multiply(infInf).equals(Complex.INF));
        Assert.assertTrue(new Complex(-1,0).multiply(infInf).equals(Complex.INF));
        Assert.assertTrue(new Complex( 1,0).multiply(negInfZero).equals(Complex.INF));

        w = oneInf.multiply(oneNegInf);
        Assert.assertEquals(w.getReal(), inf, 0);
        Assert.assertEquals(w.getImaginary(), inf, 0);

        w = negInfNegInf.multiply(oneNaN);
        Assert.assertTrue(Double.isNaN(w.getReal()));
        Assert.assertTrue(Double.isNaN(w.getImaginary()));

        z = new Complex(1, neginf);
        Assert.assertSame(Complex.INF, z.multiply(z));
    }

// org.apache.commons.math3.complex.ComplexTest::testScalarMultiply
    public void testScalarMultiply() {
        Complex x = new Complex(3.0, 4.0);
        double yDouble = 2.0;
        Complex yComplex = new Complex(yDouble);
        Assert.assertEquals(x.multiply(yComplex), x.multiply(yDouble));
        int zInt = -5;
        Complex zComplex = new Complex(zInt);
        Assert.assertEquals(x.multiply(zComplex), x.multiply(zInt));
    }

// org.apache.commons.math3.complex.ComplexTest::testScalarMultiplyNaN
    public void testScalarMultiplyNaN() {
        Complex x = new Complex(3.0, 4.0);
        double yDouble = Double.NaN;
        Complex yComplex = new Complex(yDouble);
        Assert.assertEquals(x.multiply(yComplex), x.multiply(yDouble));
    }

// org.apache.commons.math3.complex.ComplexTest::testScalarMultiplyInf
    public void testScalarMultiplyInf() {
        Complex x = new Complex(1, 1);
        double yDouble = Double.POSITIVE_INFINITY;
        Complex yComplex = new Complex(yDouble);
        Assert.assertEquals(x.multiply(yComplex), x.multiply(yDouble));

        yDouble = Double.NEGATIVE_INFINITY;
        yComplex = new Complex(yDouble);
        Assert.assertEquals(x.multiply(yComplex), x.multiply(yDouble));
    }

// org.apache.commons.math3.complex.ComplexTest::testNegate
    public void testNegate() {
        Complex x = new Complex(3.0, 4.0);
        Complex z = x.negate();
        Assert.assertEquals(-3.0, z.getReal(), 1.0e-5);
        Assert.assertEquals(-4.0, z.getImaginary(), 1.0e-5);
    }

// org.apache.commons.math3.complex.ComplexTest::testNegateNaN
    public void testNegateNaN() {
        Complex z = Complex.NaN.negate();
        Assert.assertTrue(z.isNaN());
    }

// org.apache.commons.math3.complex.ComplexTest::testSubtract
    public void testSubtract() {
        Complex x = new Complex(3.0, 4.0);
        Complex y = new Complex(5.0, 6.0);
        Complex z = x.subtract(y);
        Assert.assertEquals(-2.0, z.getReal(), 1.0e-5);
        Assert.assertEquals(-2.0, z.getImaginary(), 1.0e-5);
    }

// org.apache.commons.math3.complex.ComplexTest::testSubtractNaN
    public void testSubtractNaN() {
        Complex x = new Complex(3.0, 4.0);
        Complex z = x.subtract(Complex.NaN);
        Assert.assertSame(Complex.NaN, z);
        z = new Complex(1, nan);
        Complex w = x.subtract(z);
        Assert.assertSame(Complex.NaN, w);
    }

// org.apache.commons.math3.complex.ComplexTest::testSubtractInf
    public void testSubtractInf() {
        Complex x = new Complex(1, 1);
        Complex z = new Complex(neginf, 0);
        Complex w = x.subtract(z);
        Assert.assertEquals(w.getImaginary(), 1, 0);
        Assert.assertEquals(inf, w.getReal(), 0);

        x = new Complex(neginf, 0);
        Assert.assertTrue(Double.isNaN(x.subtract(z).getReal()));
    }

// org.apache.commons.math3.complex.ComplexTest::testScalarSubtract
    public void testScalarSubtract() {
        Complex x = new Complex(3.0, 4.0);
        double yDouble = 2.0;
        Complex yComplex = new Complex(yDouble);
        Assert.assertEquals(x.subtract(yComplex), x.subtract(yDouble));
    }

// org.apache.commons.math3.complex.ComplexTest::testScalarSubtractNaN
    public void testScalarSubtractNaN() {
        Complex x = new Complex(3.0, 4.0);
        double yDouble = Double.NaN;
        Complex yComplex = new Complex(yDouble);
        Assert.assertEquals(x.subtract(yComplex), x.subtract(yDouble));
    }

// org.apache.commons.math3.complex.ComplexTest::testScalarSubtractInf
    public void testScalarSubtractInf() {
        Complex x = new Complex(1, 1);
        double yDouble = Double.POSITIVE_INFINITY;
        Complex yComplex = new Complex(yDouble);
        Assert.assertEquals(x.subtract(yComplex), x.subtract(yDouble));

        x = new Complex(neginf, 0);
        Assert.assertEquals(x.subtract(yComplex), x.subtract(yDouble));
    }

// org.apache.commons.math3.complex.ComplexTest::testEqualsNull
    public void testEqualsNull() {
        Complex x = new Complex(3.0, 4.0);
        Assert.assertFalse(x.equals(null));
    }

// org.apache.commons.math3.complex.ComplexTest::testEqualsClass
    public void testEqualsClass() {
        Complex x = new Complex(3.0, 4.0);
        Assert.assertFalse(x.equals(this));
    }

// org.apache.commons.math3.complex.ComplexTest::testEqualsSame
    public void testEqualsSame() {
        Complex x = new Complex(3.0, 4.0);
        Assert.assertTrue(x.equals(x));
    }

// org.apache.commons.math3.complex.ComplexTest::testEqualsTrue
    public void testEqualsTrue() {
        Complex x = new Complex(3.0, 4.0);
        Complex y = new Complex(3.0, 4.0);
        Assert.assertTrue(x.equals(y));
    }

// org.apache.commons.math3.complex.ComplexTest::testEqualsRealDifference
    public void testEqualsRealDifference() {
        Complex x = new Complex(0.0, 0.0);
        Complex y = new Complex(0.0 + Double.MIN_VALUE, 0.0);
        Assert.assertFalse(x.equals(y));
    }

// org.apache.commons.math3.complex.ComplexTest::testEqualsImaginaryDifference
    public void testEqualsImaginaryDifference() {
        Complex x = new Complex(0.0, 0.0);
        Complex y = new Complex(0.0, 0.0 + Double.MIN_VALUE);
        Assert.assertFalse(x.equals(y));
    }

// org.apache.commons.math3.complex.ComplexTest::testEqualsNaN
    public void testEqualsNaN() {
        Complex realNaN = new Complex(Double.NaN, 0.0);
        Complex imaginaryNaN = new Complex(0.0, Double.NaN);
        Complex complexNaN = Complex.NaN;
        Assert.assertTrue(realNaN.equals(imaginaryNaN));
        Assert.assertTrue(imaginaryNaN.equals(complexNaN));
        Assert.assertTrue(realNaN.equals(complexNaN));
    }

// org.apache.commons.math3.complex.ComplexTest::testHashCode
    public void testHashCode() {
        Complex x = new Complex(0.0, 0.0);
        Complex y = new Complex(0.0, 0.0 + Double.MIN_VALUE);
        Assert.assertFalse(x.hashCode()==y.hashCode());
        y = new Complex(0.0 + Double.MIN_VALUE, 0.0);
        Assert.assertFalse(x.hashCode()==y.hashCode());
        Complex realNaN = new Complex(Double.NaN, 0.0);
        Complex imaginaryNaN = new Complex(0.0, Double.NaN);
        Assert.assertEquals(realNaN.hashCode(), imaginaryNaN.hashCode());
        Assert.assertEquals(imaginaryNaN.hashCode(), Complex.NaN.hashCode());
    }

// org.apache.commons.math3.complex.ComplexTest::testAcos
    public void testAcos() {
        Complex z = new Complex(3, 4);
        Complex expected = new Complex(0.936812, -2.30551);
        TestUtils.assertEquals(expected, z.acos(), 1.0e-5);
        TestUtils.assertEquals(new Complex(FastMath.acos(0), 0),
                Complex.ZERO.acos(), 1.0e-12);
    }

// org.apache.commons.math3.complex.ComplexTest::testAcosInf
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

// org.apache.commons.math3.complex.ComplexTest::testAcosNaN
    public void testAcosNaN() {
        Assert.assertTrue(Complex.NaN.acos().isNaN());
    }

// org.apache.commons.math3.complex.ComplexTest::testAsin
    public void testAsin() {
        Complex z = new Complex(3, 4);
        Complex expected = new Complex(0.633984, 2.30551);
        TestUtils.assertEquals(expected, z.asin(), 1.0e-5);
    }

// org.apache.commons.math3.complex.ComplexTest::testAsinNaN
    public void testAsinNaN() {
        Assert.assertTrue(Complex.NaN.asin().isNaN());
    }

// org.apache.commons.math3.complex.ComplexTest::testAsinInf
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

// org.apache.commons.math3.complex.ComplexTest::testAtan
    public void testAtan() {
        Complex z = new Complex(3, 4);
        Complex expected = new Complex(1.44831, 0.158997);
        TestUtils.assertEquals(expected, z.atan(), 1.0e-5);
    }

// org.apache.commons.math3.complex.ComplexTest::testAtanInf
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

// org.apache.commons.math3.complex.ComplexTest::testAtanI
    public void testAtanI() {
        Assert.assertTrue(Complex.I.atan().isNaN());
    }

// org.apache.commons.math3.complex.ComplexTest::testAtanNaN
    public void testAtanNaN() {
        Assert.assertTrue(Complex.NaN.atan().isNaN());
    }

// org.apache.commons.math3.complex.ComplexTest::testCos
    public void testCos() {
        Complex z = new Complex(3, 4);
        Complex expected = new Complex(-27.03495, -3.851153);
        TestUtils.assertEquals(expected, z.cos(), 1.0e-5);
    }

// org.apache.commons.math3.complex.ComplexTest::testCosNaN
    public void testCosNaN() {
        Assert.assertTrue(Complex.NaN.cos().isNaN());
    }

// org.apache.commons.math3.complex.ComplexTest::testCosInf
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

// org.apache.commons.math3.complex.ComplexTest::testCosh
    public void testCosh() {
        Complex z = new Complex(3, 4);
        Complex expected = new Complex(-6.58066, -7.58155);
        TestUtils.assertEquals(expected, z.cosh(), 1.0e-5);
    }

// org.apache.commons.math3.complex.ComplexTest::testCoshNaN
    public void testCoshNaN() {
        Assert.assertTrue(Complex.NaN.cosh().isNaN());
    }

// org.apache.commons.math3.complex.ComplexTest::testCoshInf
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

// org.apache.commons.math3.complex.ComplexTest::testExp
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

// org.apache.commons.math3.complex.ComplexTest::testExpNaN
    public void testExpNaN() {
        Assert.assertTrue(Complex.NaN.exp().isNaN());
    }

// org.apache.commons.math3.complex.ComplexTest::testExpInf
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

// org.apache.commons.math3.complex.ComplexTest::testLog
    public void testLog() {
        Complex z = new Complex(3, 4);
        Complex expected = new Complex(1.60944, 0.927295);
        TestUtils.assertEquals(expected, z.log(), 1.0e-5);
    }

// org.apache.commons.math3.complex.ComplexTest::testLogNaN
    public void testLogNaN() {
        Assert.assertTrue(Complex.NaN.log().isNaN());
    }

// org.apache.commons.math3.complex.ComplexTest::testLogInf
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

// org.apache.commons.math3.complex.ComplexTest::testLogZero
    public void testLogZero() {
        TestUtils.assertSame(negInfZero, Complex.ZERO.log());
    }

// org.apache.commons.math3.complex.ComplexTest::testPow
    public void testPow() {
        Complex x = new Complex(3, 4);
        Complex y = new Complex(5, 6);
        Complex expected = new Complex(-1.860893, 11.83677);
        TestUtils.assertEquals(expected, x.pow(y), 1.0e-5);
    }

// org.apache.commons.math3.complex.ComplexTest::testPowNaNBase
    public void testPowNaNBase() {
        Complex x = new Complex(3, 4);
        Assert.assertTrue(Complex.NaN.pow(x).isNaN());
    }

// org.apache.commons.math3.complex.ComplexTest::testPowNaNExponent
    public void testPowNaNExponent() {
        Complex x = new Complex(3, 4);
        Assert.assertTrue(x.pow(Complex.NaN).isNaN());
    }

// org.apache.commons.math3.complex.ComplexTest::testPowInf
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

// org.apache.commons.math3.complex.ComplexTest::testPowZero
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

// org.apache.commons.math3.complex.ComplexTest::testScalarPow
    public void testScalarPow() {
        Complex x = new Complex(3, 4);
        double yDouble = 5.0;
        Complex yComplex = new Complex(yDouble);
        Assert.assertEquals(x.pow(yComplex), x.pow(yDouble));
    }

// org.apache.commons.math3.complex.ComplexTest::testScalarPowNaNBase
    public void testScalarPowNaNBase() {
        Complex x = Complex.NaN;
        double yDouble = 5.0;
        Complex yComplex = new Complex(yDouble);
        Assert.assertEquals(x.pow(yComplex), x.pow(yDouble));
    }

// org.apache.commons.math3.complex.ComplexTest::testScalarPowNaNExponent
    public void testScalarPowNaNExponent() {
        Complex x = new Complex(3, 4);
        double yDouble = Double.NaN;
        Complex yComplex = new Complex(yDouble);
        Assert.assertEquals(x.pow(yComplex), x.pow(yDouble));
    }

// org.apache.commons.math3.complex.ComplexTest::testScalarPowInf
   public void testScalarPowInf() {
       TestUtils.assertSame(Complex.NaN,Complex.ONE.pow(Double.POSITIVE_INFINITY));
       TestUtils.assertSame(Complex.NaN,Complex.ONE.pow(Double.NEGATIVE_INFINITY));
       TestUtils.assertSame(Complex.NaN,infOne.pow(1.0));
       TestUtils.assertSame(Complex.NaN,negInfOne.pow(1.0));
       TestUtils.assertSame(Complex.NaN,infInf.pow(1.0));
       TestUtils.assertSame(Complex.NaN,infNegInf.pow(1.0));
       TestUtils.assertSame(Complex.NaN,negInfInf.pow(10));
       TestUtils.assertSame(Complex.NaN,negInfNegInf.pow(1.0));
       TestUtils.assertSame(Complex.NaN,negInfNegInf.pow(Double.POSITIVE_INFINITY));
       TestUtils.assertSame(Complex.NaN,negInfNegInf.pow(Double.POSITIVE_INFINITY));
       TestUtils.assertSame(Complex.NaN,infInf.pow(Double.POSITIVE_INFINITY));
       TestUtils.assertSame(Complex.NaN,infInf.pow(Double.NEGATIVE_INFINITY));
       TestUtils.assertSame(Complex.NaN,infNegInf.pow(Double.NEGATIVE_INFINITY));
       TestUtils.assertSame(Complex.NaN,infNegInf.pow(Double.POSITIVE_INFINITY));
   }

// org.apache.commons.math3.complex.ComplexTest::testScalarPowZero
   public void testScalarPowZero() {
       TestUtils.assertSame(Complex.NaN, Complex.ZERO.pow(1.0));
       TestUtils.assertSame(Complex.NaN, Complex.ZERO.pow(0.0));
       TestUtils.assertEquals(Complex.ONE, Complex.ONE.pow(0.0), 10e-12);
       TestUtils.assertEquals(Complex.ONE, Complex.I.pow(0.0), 10e-12);
       TestUtils.assertEquals(Complex.ONE, new Complex(-1, 3).pow(0.0), 10e-12);
   }

// org.apache.commons.math3.complex.ComplexTest::testpowNull
    public void testpowNull() {
        Complex.ONE.pow(null);
    }

// org.apache.commons.math3.complex.ComplexTest::testSin
    public void testSin() {
        Complex z = new Complex(3, 4);
        Complex expected = new Complex(3.853738, -27.01681);
        TestUtils.assertEquals(expected, z.sin(), 1.0e-5);
    }

// org.apache.commons.math3.complex.ComplexTest::testSinInf
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

// org.apache.commons.math3.complex.ComplexTest::testSinNaN
    public void testSinNaN() {
        Assert.assertTrue(Complex.NaN.sin().isNaN());
    }

// org.apache.commons.math3.complex.ComplexTest::testSinh
    public void testSinh() {
        Complex z = new Complex(3, 4);
        Complex expected = new Complex(-6.54812, -7.61923);
        TestUtils.assertEquals(expected, z.sinh(), 1.0e-5);
    }

// org.apache.commons.math3.complex.ComplexTest::testSinhNaN
    public void testSinhNaN() {
        Assert.assertTrue(Complex.NaN.sinh().isNaN());
    }

// org.apache.commons.math3.complex.ComplexTest::testSinhInf
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

// org.apache.commons.math3.complex.ComplexTest::testSqrtRealPositive
    public void testSqrtRealPositive() {
        Complex z = new Complex(3, 4);
        Complex expected = new Complex(2, 1);
        TestUtils.assertEquals(expected, z.sqrt(), 1.0e-5);
    }

// org.apache.commons.math3.complex.ComplexTest::testSqrtRealZero
    public void testSqrtRealZero() {
        Complex z = new Complex(0.0, 4);
        Complex expected = new Complex(1.41421, 1.41421);
        TestUtils.assertEquals(expected, z.sqrt(), 1.0e-5);
    }

// org.apache.commons.math3.complex.ComplexTest::testSqrtRealNegative
    public void testSqrtRealNegative() {
        Complex z = new Complex(-3.0, 4);
        Complex expected = new Complex(1, 2);
        TestUtils.assertEquals(expected, z.sqrt(), 1.0e-5);
    }

// org.apache.commons.math3.complex.ComplexTest::testSqrtImaginaryZero
    public void testSqrtImaginaryZero() {
        Complex z = new Complex(-3.0, 0.0);
        Complex expected = new Complex(0.0, 1.73205);
        TestUtils.assertEquals(expected, z.sqrt(), 1.0e-5);
    }

// org.apache.commons.math3.complex.ComplexTest::testSqrtImaginaryNegative
    public void testSqrtImaginaryNegative() {
        Complex z = new Complex(-3.0, -4.0);
        Complex expected = new Complex(1.0, -2.0);
        TestUtils.assertEquals(expected, z.sqrt(), 1.0e-5);
    }

// org.apache.commons.math3.complex.ComplexTest::testSqrtPolar
    public void testSqrtPolar() {
        double r = 1;
        for (int i = 0; i < 5; i++) {
            r += i;
            double theta = 0;
            for (int j =0; j < 11; j++) {
                theta += pi /12;
                Complex z = ComplexUtils.polar2Complex(r, theta);
                Complex sqrtz = ComplexUtils.polar2Complex(FastMath.sqrt(r), theta / 2);
                TestUtils.assertEquals(sqrtz, z.sqrt(), 10e-12);
            }
        }
    }

// org.apache.commons.math3.complex.ComplexTest::testSqrtNaN
    public void testSqrtNaN() {
        Assert.assertTrue(Complex.NaN.sqrt().isNaN());
    }

// org.apache.commons.math3.complex.ComplexTest::testSqrtInf
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

// org.apache.commons.math3.complex.ComplexTest::testSqrt1z
    public void testSqrt1z() {
        Complex z = new Complex(3, 4);
        Complex expected = new Complex(4.08033, -2.94094);
        TestUtils.assertEquals(expected, z.sqrt1z(), 1.0e-5);
    }

// org.apache.commons.math3.complex.ComplexTest::testSqrt1zNaN
    public void testSqrt1zNaN() {
        Assert.assertTrue(Complex.NaN.sqrt1z().isNaN());
    }

// org.apache.commons.math3.complex.ComplexTest::testTan
    public void testTan() {
        Complex z = new Complex(3, 4);
        Complex expected = new Complex(-0.000187346, 0.999356);
        TestUtils.assertEquals(expected, z.tan(), 1.0e-5);
        
        Complex actual = new Complex(3.0, 1E10).tan();
        expected = new Complex(0, 1);
        TestUtils.assertEquals(expected, actual, 1.0e-5);
        actual = new Complex(3.0, -1E10).tan();
        expected = new Complex(0, -1);
        TestUtils.assertEquals(expected, actual, 1.0e-5);
    }

// org.apache.commons.math3.complex.ComplexTest::testTanNaN
    public void testTanNaN() {
        Assert.assertTrue(Complex.NaN.tan().isNaN());
    }

// org.apache.commons.math3.complex.ComplexTest::testTanInf
    public void testTanInf() {
        TestUtils.assertSame(Complex.valueOf(0.0, 1.0), oneInf.tan());
        TestUtils.assertSame(Complex.valueOf(0.0, -1.0), oneNegInf.tan());
        TestUtils.assertSame(Complex.NaN, infOne.tan());
        TestUtils.assertSame(Complex.NaN, negInfOne.tan());
        TestUtils.assertSame(Complex.NaN, infInf.tan());
        TestUtils.assertSame(Complex.NaN, infNegInf.tan());
        TestUtils.assertSame(Complex.NaN, negInfInf.tan());
        TestUtils.assertSame(Complex.NaN, negInfNegInf.tan());
    }

// org.apache.commons.math3.complex.ComplexTest::testTanCritical
   public void testTanCritical() {
        TestUtils.assertSame(infNaN, new Complex(pi/2, 0).tan());
        TestUtils.assertSame(negInfNaN, new Complex(-pi/2, 0).tan());
    }

// org.apache.commons.math3.complex.ComplexTest::testTanh
    public void testTanh() {
        Complex z = new Complex(3, 4);
        Complex expected = new Complex(1.00071, 0.00490826);
        TestUtils.assertEquals(expected, z.tanh(), 1.0e-5);
        
        Complex actual = new Complex(1E10, 3.0).tanh();
        expected = new Complex(1, 0);
        TestUtils.assertEquals(expected, actual, 1.0e-5);
        actual = new Complex(-1E10, 3.0).tanh();
        expected = new Complex(-1, 0);
        TestUtils.assertEquals(expected, actual, 1.0e-5);
    }

// org.apache.commons.math3.complex.ComplexTest::testTanhNaN
    public void testTanhNaN() {
        Assert.assertTrue(Complex.NaN.tanh().isNaN());
    }

// org.apache.commons.math3.complex.ComplexTest::testTanhInf
    public void testTanhInf() {
        TestUtils.assertSame(Complex.NaN, oneInf.tanh());
        TestUtils.assertSame(Complex.NaN, oneNegInf.tanh());
        TestUtils.assertSame(Complex.valueOf(1.0, 0.0), infOne.tanh());
        TestUtils.assertSame(Complex.valueOf(-1.0, 0.0), negInfOne.tanh());
        TestUtils.assertSame(Complex.NaN, infInf.tanh());
        TestUtils.assertSame(Complex.NaN, infNegInf.tanh());
        TestUtils.assertSame(Complex.NaN, negInfInf.tanh());
        TestUtils.assertSame(Complex.NaN, negInfNegInf.tanh());
    }

// org.apache.commons.math3.complex.ComplexTest::testTanhCritical
    public void testTanhCritical() {
        TestUtils.assertSame(nanInf, new Complex(0, pi/2).tanh());
    }

// org.apache.commons.math3.complex.ComplexTest::testMath221
    public void testMath221() {
        Assert.assertEquals(new Complex(0,-1), new Complex(0,1).multiply(new Complex(-1,0)));
    }

// org.apache.commons.math3.complex.ComplexTest::testNthRoot_normal_thirdRoot
    public void testNthRoot_normal_thirdRoot() {
        
        Complex z = new Complex(-2,2);
        
        Complex[] thirdRootsOfZ = z.nthRoot(3).toArray(new Complex[0]);
        
        Assert.assertEquals(3, thirdRootsOfZ.length);
        
        Assert.assertEquals(1.0,                  thirdRootsOfZ[0].getReal(),      1.0e-5);
        Assert.assertEquals(1.0,                  thirdRootsOfZ[0].getImaginary(), 1.0e-5);
        
        Assert.assertEquals(-1.3660254037844386,  thirdRootsOfZ[1].getReal(),      1.0e-5);
        Assert.assertEquals(0.36602540378443843,  thirdRootsOfZ[1].getImaginary(), 1.0e-5);
        
        Assert.assertEquals(0.366025403784439,    thirdRootsOfZ[2].getReal(),      1.0e-5);
        Assert.assertEquals(-1.3660254037844384,  thirdRootsOfZ[2].getImaginary(), 1.0e-5);
    }

// org.apache.commons.math3.complex.ComplexTest::testNthRoot_normal_fourthRoot
    public void testNthRoot_normal_fourthRoot() {
        
        Complex z = new Complex(5,-2);
        
        Complex[] fourthRootsOfZ = z.nthRoot(4).toArray(new Complex[0]);
        
        Assert.assertEquals(4, fourthRootsOfZ.length);
        
        Assert.assertEquals(1.5164629308487783,     fourthRootsOfZ[0].getReal(),      1.0e-5);
        Assert.assertEquals(-0.14469266210702247,   fourthRootsOfZ[0].getImaginary(), 1.0e-5);
        
        Assert.assertEquals(0.14469266210702256,    fourthRootsOfZ[1].getReal(),      1.0e-5);
        Assert.assertEquals(1.5164629308487783,     fourthRootsOfZ[1].getImaginary(), 1.0e-5);
        
        Assert.assertEquals(-1.5164629308487783,    fourthRootsOfZ[2].getReal(),      1.0e-5);
        Assert.assertEquals(0.14469266210702267,    fourthRootsOfZ[2].getImaginary(), 1.0e-5);
        
        Assert.assertEquals(-0.14469266210702275,   fourthRootsOfZ[3].getReal(),      1.0e-5);
        Assert.assertEquals(-1.5164629308487783,    fourthRootsOfZ[3].getImaginary(), 1.0e-5);
    }

// org.apache.commons.math3.complex.ComplexTest::testNthRoot_cornercase_thirdRoot_imaginaryPartEmpty
    public void testNthRoot_cornercase_thirdRoot_imaginaryPartEmpty() {
        
        
        Complex z = new Complex(8,0);
        
        Complex[] thirdRootsOfZ = z.nthRoot(3).toArray(new Complex[0]);
        
        Assert.assertEquals(3, thirdRootsOfZ.length);
        
        Assert.assertEquals(2.0,                thirdRootsOfZ[0].getReal(),      1.0e-5);
        Assert.assertEquals(0.0,                thirdRootsOfZ[0].getImaginary(), 1.0e-5);
        
        Assert.assertEquals(-1.0,               thirdRootsOfZ[1].getReal(),      1.0e-5);
        Assert.assertEquals(1.7320508075688774, thirdRootsOfZ[1].getImaginary(), 1.0e-5);
        
        Assert.assertEquals(-1.0,               thirdRootsOfZ[2].getReal(),      1.0e-5);
        Assert.assertEquals(-1.732050807568877, thirdRootsOfZ[2].getImaginary(), 1.0e-5);
    }

// org.apache.commons.math3.complex.ComplexTest::testNthRoot_cornercase_thirdRoot_realPartZero
    public void testNthRoot_cornercase_thirdRoot_realPartZero() {
        
        Complex z = new Complex(0,2);
        
        Complex[] thirdRootsOfZ = z.nthRoot(3).toArray(new Complex[0]);
        
        Assert.assertEquals(3, thirdRootsOfZ.length);
        
        Assert.assertEquals(1.0911236359717216,      thirdRootsOfZ[0].getReal(),      1.0e-5);
        Assert.assertEquals(0.6299605249474365,      thirdRootsOfZ[0].getImaginary(), 1.0e-5);
        
        Assert.assertEquals(-1.0911236359717216,     thirdRootsOfZ[1].getReal(),      1.0e-5);
        Assert.assertEquals(0.6299605249474365,      thirdRootsOfZ[1].getImaginary(), 1.0e-5);
        
        Assert.assertEquals(-2.3144374213981936E-16, thirdRootsOfZ[2].getReal(),      1.0e-5);
        Assert.assertEquals(-1.2599210498948732,     thirdRootsOfZ[2].getImaginary(), 1.0e-5);
    }

// org.apache.commons.math3.complex.ComplexTest::testNthRoot_cornercase_NAN_Inf
    public void testNthRoot_cornercase_NAN_Inf() {
        
        List<Complex> roots = oneNaN.nthRoot(3);
        Assert.assertEquals(1,roots.size());
        Assert.assertEquals(Complex.NaN, roots.get(0));

        roots = nanZero.nthRoot(3);
        Assert.assertEquals(1,roots.size());
        Assert.assertEquals(Complex.NaN, roots.get(0));

        
        roots = nanInf.nthRoot(3);
        Assert.assertEquals(1,roots.size());
        Assert.assertEquals(Complex.NaN, roots.get(0));

        
        roots = oneInf.nthRoot(3);
        Assert.assertEquals(1,roots.size());
        Assert.assertEquals(Complex.INF, roots.get(0));

        
        roots = negInfInf.nthRoot(3);
        Assert.assertEquals(1,roots.size());
        Assert.assertEquals(Complex.INF, roots.get(0));
    }

// org.apache.commons.math3.complex.ComplexTest::testGetArgument
    public void testGetArgument() {
        Complex z = new Complex(1, 0);
        Assert.assertEquals(0.0, z.getArgument(), 1.0e-12);

        z = new Complex(1, 1);
        Assert.assertEquals(FastMath.PI/4, z.getArgument(), 1.0e-12);

        z = new Complex(0, 1);
        Assert.assertEquals(FastMath.PI/2, z.getArgument(), 1.0e-12);

        z = new Complex(-1, 1);
        Assert.assertEquals(3 * FastMath.PI/4, z.getArgument(), 1.0e-12);

        z = new Complex(-1, 0);
        Assert.assertEquals(FastMath.PI, z.getArgument(), 1.0e-12);

        z = new Complex(-1, -1);
        Assert.assertEquals(-3 * FastMath.PI/4, z.getArgument(), 1.0e-12);

        z = new Complex(0, -1);
        Assert.assertEquals(-FastMath.PI/2, z.getArgument(), 1.0e-12);

        z = new Complex(1, -1);
        Assert.assertEquals(-FastMath.PI/4, z.getArgument(), 1.0e-12);

    }

// org.apache.commons.math3.complex.ComplexTest::testGetArgumentInf
    public void testGetArgumentInf() {
        Assert.assertEquals(FastMath.PI/4, infInf.getArgument(), 1.0e-12);
        Assert.assertEquals(FastMath.PI/2, oneInf.getArgument(), 1.0e-12);
        Assert.assertEquals(0.0, infOne.getArgument(), 1.0e-12);
        Assert.assertEquals(FastMath.PI/2, zeroInf.getArgument(), 1.0e-12);
        Assert.assertEquals(0.0, infZero.getArgument(), 1.0e-12);
        Assert.assertEquals(FastMath.PI, negInfOne.getArgument(), 1.0e-12);
        Assert.assertEquals(-3.0*FastMath.PI/4, negInfNegInf.getArgument(), 1.0e-12);
        Assert.assertEquals(-FastMath.PI/2, oneNegInf.getArgument(), 1.0e-12);
    }

// org.apache.commons.math3.complex.ComplexTest::testGetArgumentNaN
    public void testGetArgumentNaN() {
        Assert.assertTrue(Double.isNaN(nanZero.getArgument()));
        Assert.assertTrue(Double.isNaN(zeroNaN.getArgument()));
        Assert.assertTrue(Double.isNaN(Complex.NaN.getArgument()));
    }

// org.apache.commons.math3.complex.ComplexTest::testSerial
    public void testSerial() {
        Complex z = new Complex(3.0, 4.0);
        Assert.assertEquals(z, TestUtils.serializeAndRecover(z));
        Complex ncmplx = (Complex)TestUtils.serializeAndRecover(oneNaN);
        Assert.assertEquals(nanZero, ncmplx);
        Assert.assertTrue(ncmplx.isNaN());
        Complex infcmplx = (Complex)TestUtils.serializeAndRecover(infInf);
        Assert.assertEquals(infInf, infcmplx);
        Assert.assertTrue(infcmplx.isInfinite());
        TestComplex tz = new TestComplex(3.0, 4.0);
        Assert.assertEquals(tz, TestUtils.serializeAndRecover(tz));
        TestComplex ntcmplx = (TestComplex)TestUtils.serializeAndRecover(new TestComplex(oneNaN));
        Assert.assertEquals(nanZero, ntcmplx);
        Assert.assertTrue(ntcmplx.isNaN());
        TestComplex inftcmplx = (TestComplex)TestUtils.serializeAndRecover(new TestComplex(infInf));
        Assert.assertEquals(infInf, inftcmplx);
        Assert.assertTrue(inftcmplx.isInfinite());
    }

// org.apache.commons.math3.complex.ComplexUtilsTest::testPolar2Complex
    public void testPolar2Complex() {
        TestUtils.assertEquals(Complex.ONE,
                ComplexUtils.polar2Complex(1, 0), 10e-12);
        TestUtils.assertEquals(Complex.ZERO,
                ComplexUtils.polar2Complex(0, 1), 10e-12);
        TestUtils.assertEquals(Complex.ZERO,
                ComplexUtils.polar2Complex(0, -1), 10e-12);
        TestUtils.assertEquals(Complex.I,
                ComplexUtils.polar2Complex(1, pi/2), 10e-12);
        TestUtils.assertEquals(Complex.I.negate(),
                ComplexUtils.polar2Complex(1, -pi/2), 10e-12);
        double r = 0;
        for (int i = 0; i < 5; i++) {
          r += i;
          double theta = 0;
          for (int j =0; j < 20; j++) {
              theta += pi / 6;
              TestUtils.assertEquals(altPolar(r, theta),
                      ComplexUtils.polar2Complex(r, theta), 10e-12);
          }
          theta = -2 * pi;
          for (int j =0; j < 20; j++) {
              theta -= pi / 6;
              TestUtils.assertEquals(altPolar(r, theta),
                      ComplexUtils.polar2Complex(r, theta), 10e-12);
          }
        }
    }

// org.apache.commons.math3.complex.ComplexUtilsTest::testPolar2ComplexIllegalModulus
    public void testPolar2ComplexIllegalModulus() {
        ComplexUtils.polar2Complex(-1, 0);
    }

// org.apache.commons.math3.complex.ComplexUtilsTest::testPolar2ComplexNaN
    public void testPolar2ComplexNaN() {
        TestUtils.assertSame(Complex.NaN, ComplexUtils.polar2Complex(nan, 1));
        TestUtils.assertSame(Complex.NaN, ComplexUtils.polar2Complex(1, nan));
        TestUtils.assertSame(Complex.NaN,
                ComplexUtils.polar2Complex(nan, nan));
    }

// org.apache.commons.math3.complex.ComplexUtilsTest::testPolar2ComplexInf
    public void testPolar2ComplexInf() {
        TestUtils.assertSame(Complex.NaN, ComplexUtils.polar2Complex(1, inf));
        TestUtils.assertSame(Complex.NaN,
                ComplexUtils.polar2Complex(1, negInf));
        TestUtils.assertSame(Complex.NaN, ComplexUtils.polar2Complex(inf, inf));
        TestUtils.assertSame(Complex.NaN,
                ComplexUtils.polar2Complex(inf, negInf));
        TestUtils.assertSame(infInf, ComplexUtils.polar2Complex(inf, pi/4));
        TestUtils.assertSame(infNaN, ComplexUtils.polar2Complex(inf, 0));
        TestUtils.assertSame(infNegInf, ComplexUtils.polar2Complex(inf, -pi/4));
        TestUtils.assertSame(negInfInf, ComplexUtils.polar2Complex(inf, 3*pi/4));
        TestUtils.assertSame(negInfNegInf, ComplexUtils.polar2Complex(inf, 5*pi/4));
    }

// org.apache.commons.math3.complex.ComplexUtilsTest::testConvertToComplex
    public void testConvertToComplex() {
        final double[] real = new double[] { negInf, -123.45, 0, 1, 234.56, pi, inf };
        final Complex[] complex = ComplexUtils.convertToComplex(real);

        for (int i = 0; i < real.length; i++) {
            Assert.assertEquals(real[i], complex[i].getReal(), 0d);
        }
    }

// org.apache.commons.math3.complex.QuaternionTest::testWrongDimension
    public void testWrongDimension() {
        new Quaternion(new double[] { 1, 2 });
    }

// org.apache.commons.math3.complex.RootsOfUnityTest::testMathIllegalState1
    public void testMathIllegalState1() {
        final RootsOfUnity roots = new RootsOfUnity();
        roots.getReal(0);
    }

// org.apache.commons.math3.complex.RootsOfUnityTest::testMathIllegalState2
    public void testMathIllegalState2() {
        final RootsOfUnity roots = new RootsOfUnity();
        roots.getImaginary(0);
    }

// org.apache.commons.math3.complex.RootsOfUnityTest::testMathIllegalState3
    public void testMathIllegalState3() {
        final RootsOfUnity roots = new RootsOfUnity();
        roots.isCounterClockWise();
    }

// org.apache.commons.math3.complex.RootsOfUnityTest::testZeroNumberOfRoots
    public void testZeroNumberOfRoots() {
        final RootsOfUnity roots = new RootsOfUnity();
        roots.computeRoots(0);
    }

// org.apache.commons.math3.complex.RootsOfUnityTest::testGetNumberOfRoots
    public void testGetNumberOfRoots() {
        final RootsOfUnity roots = new RootsOfUnity();
        Assert.assertEquals("", 0, roots.getNumberOfRoots());
        roots.computeRoots(5);
        Assert.assertEquals("", 5, roots.getNumberOfRoots());
        
        roots.computeRoots(-5);
        Assert.assertEquals("", 5, roots.getNumberOfRoots());
        roots.computeRoots(6);
        Assert.assertEquals("", 6, roots.getNumberOfRoots());
    }

// org.apache.commons.math3.complex.RootsOfUnityTest::testComputeRoots
    public void testComputeRoots() {
        final RootsOfUnity roots = new RootsOfUnity();
        for (int n = -10; n < 11; n++) {
            
            if (n != 0) {
                roots.computeRoots(n);
                doTestComputeRoots(roots);
                roots.computeRoots(-n);
                doTestComputeRoots(roots);
            }
        }
    }

// org.apache.commons.math3.dfp.DfpTest::testByteConstructor
    public void testByteConstructor() {
        Assert.assertEquals("0.", new Dfp(field, (byte) 0).toString());
        Assert.assertEquals("1.", new Dfp(field, (byte) 1).toString());
        Assert.assertEquals("-1.", new Dfp(field, (byte) -1).toString());
        Assert.assertEquals("-128.", new Dfp(field, Byte.MIN_VALUE).toString());
        Assert.assertEquals("127.", new Dfp(field, Byte.MAX_VALUE).toString());
    }

// org.apache.commons.math3.dfp.DfpTest::testIntConstructor
    public void testIntConstructor() {
        Assert.assertEquals("0.", new Dfp(field, 0).toString());
        Assert.assertEquals("1.", new Dfp(field, 1).toString());
        Assert.assertEquals("-1.", new Dfp(field, -1).toString());
        Assert.assertEquals("1234567890.", new Dfp(field, 1234567890).toString());
        Assert.assertEquals("-1234567890.", new Dfp(field, -1234567890).toString());
        Assert.assertEquals("-2147483648.", new Dfp(field, Integer.MIN_VALUE).toString());
        Assert.assertEquals("2147483647.", new Dfp(field, Integer.MAX_VALUE).toString());
    }

// org.apache.commons.math3.dfp.DfpTest::testLongConstructor
    public void testLongConstructor() {
        Assert.assertEquals("0.", new Dfp(field, 0l).toString());
        Assert.assertEquals("1.", new Dfp(field, 1l).toString());
        Assert.assertEquals("-1.", new Dfp(field, -1l).toString());
        Assert.assertEquals("1234567890.", new Dfp(field, 1234567890l).toString());
        Assert.assertEquals("-1234567890.", new Dfp(field, -1234567890l).toString());
        Assert.assertEquals("-9223372036854775808.", new Dfp(field, Long.MIN_VALUE).toString());
        Assert.assertEquals("9223372036854775807.", new Dfp(field, Long.MAX_VALUE).toString());
    }

// org.apache.commons.math3.dfp.DfpTest::testAdd
    public void testAdd()
    {
        test(field.newDfp("1").add(field.newDfp("1")),      
             field.newDfp("2"),
             0, "Add #1");

        test(field.newDfp("1").add(field.newDfp("-1")),     
             field.newDfp("0"),
             0, "Add #2");

        test(field.newDfp("-1").add(field.newDfp("1")),     
             field.newDfp("0"),
             0, "Add #3");

        test(field.newDfp("-1").add(field.newDfp("-1")),     
             field.newDfp("-2"),
             0, "Add #4");

        

        test(field.newDfp("1").add(field.newDfp("1e-16")),     
             field.newDfp("1.0000000000000001"),
             0, "Add #5");

        test(field.newDfp("1").add(field.newDfp("1e-17")),     
             field.newDfp("1"),
             DfpField.FLAG_INEXACT, "Add #6");

        test(field.newDfp("0.90999999999999999999").add(field.newDfp("0.1")),     
             field.newDfp("1.01"),
             DfpField.FLAG_INEXACT, "Add #7");

        test(field.newDfp(".10000000000000005000").add(field.newDfp(".9")),     
             field.newDfp("1."),
             DfpField.FLAG_INEXACT, "Add #8");

        test(field.newDfp(".10000000000000015000").add(field.newDfp(".9")),     
             field.newDfp("1.0000000000000002"),
             DfpField.FLAG_INEXACT, "Add #9");

        test(field.newDfp(".10000000000000014999").add(field.newDfp(".9")),     
             field.newDfp("1.0000000000000001"),
             DfpField.FLAG_INEXACT, "Add #10");

        test(field.newDfp(".10000000000000015001").add(field.newDfp(".9")),     
             field.newDfp("1.0000000000000002"),
             DfpField.FLAG_INEXACT, "Add #11");

        test(field.newDfp(".11111111111111111111").add(field.newDfp("11.1111111111111111")), 
             field.newDfp("11.22222222222222222222"),
             DfpField.FLAG_INEXACT, "Add #12");

        test(field.newDfp(".11111111111111111111").add(field.newDfp("1111111111111111.1111")), 
             field.newDfp("1111111111111111.2222"),
             DfpField.FLAG_INEXACT, "Add #13");

        test(field.newDfp(".11111111111111111111").add(field.newDfp("11111111111111111111")), 
             field.newDfp("11111111111111111111"),
             DfpField.FLAG_INEXACT, "Add #14");

        test(field.newDfp("9.9999999999999999999e131071").add(field.newDfp("-1e131052")), 
             field.newDfp("9.9999999999999999998e131071"),
             0, "Add #15");

        test(field.newDfp("9.9999999999999999999e131071").add(field.newDfp("1e131052")), 
             pinf,
             DfpField.FLAG_OVERFLOW, "Add #16");

        test(field.newDfp("-9.9999999999999999999e131071").add(field.newDfp("-1e131052")), 
             ninf,
             DfpField.FLAG_OVERFLOW, "Add #17");

        test(field.newDfp("-9.9999999999999999999e131071").add(field.newDfp("1e131052")), 
             field.newDfp("-9.9999999999999999998e131071"),
             0, "Add #18");

        test(field.newDfp("1e-131072").add(field.newDfp("1e-131072")), 
             field.newDfp("2e-131072"),
             0, "Add #19");

        test(field.newDfp("1.0000000000000001e-131057").add(field.newDfp("-1e-131057")), 
             field.newDfp("1e-131073"),
             DfpField.FLAG_UNDERFLOW, "Add #20");

        test(field.newDfp("1.1e-131072").add(field.newDfp("-1e-131072")), 
             field.newDfp("1e-131073"),
             DfpField.FLAG_UNDERFLOW, "Add #21");

        test(field.newDfp("1.0000000000000001e-131072").add(field.newDfp("-1e-131072")), 
             field.newDfp("1e-131088"),
             DfpField.FLAG_UNDERFLOW, "Add #22");

        test(field.newDfp("1.0000000000000001e-131078").add(field.newDfp("-1e-131078")), 
             field.newDfp("0"),
             DfpField.FLAG_UNDERFLOW, "Add #23");

        test(field.newDfp("1.0").add(field.newDfp("-1e-20")), 
             field.newDfp("0.99999999999999999999"),
             0, "Add #23.1");

        test(field.newDfp("-0.99999999999999999999").add(field.newDfp("1")), 
             field.newDfp("0.00000000000000000001"),
             0, "Add #23.2");

        test(field.newDfp("1").add(field.newDfp("0")), 
             field.newDfp("1"),
             0, "Add #24");

        test(field.newDfp("0").add(field.newDfp("0")), 
             field.newDfp("0"),
             0, "Add #25");

        test(field.newDfp("-0").add(field.newDfp("0")), 
             field.newDfp("0"),
             0, "Add #26");

        test(field.newDfp("0").add(field.newDfp("-0")), 
             field.newDfp("0"),
             0, "Add #27");

        test(field.newDfp("-0").add(field.newDfp("-0")), 
             field.newDfp("-0"),
             0, "Add #28");

        test(field.newDfp("1e-20").add(field.newDfp("0")), 
             field.newDfp("1e-20"),
             0, "Add #29");

        test(field.newDfp("1e-40").add(field.newDfp("0")), 
             field.newDfp("1e-40"),
             0, "Add #30");

        test(pinf.add(ninf), 
             nan,
             DfpField.FLAG_INVALID, "Add #31");

        test(ninf.add(pinf), 
             nan,
             DfpField.FLAG_INVALID, "Add #32");

        test(ninf.add(ninf), 
             ninf,
             0, "Add #33");

        test(pinf.add(pinf), 
             pinf,
             0, "Add #34");

        test(pinf.add(field.newDfp("0")), 
             pinf,
             0, "Add #35");

        test(pinf.add(field.newDfp("-1e131071")), 
             pinf,
             0, "Add #36");

        test(pinf.add(field.newDfp("1e131071")), 
             pinf,
             0, "Add #37");

        test(field.newDfp("0").add(pinf), 
             pinf,
             0, "Add #38");

        test(field.newDfp("-1e131071").add(pinf), 
             pinf,
             0, "Add #39");

        test(field.newDfp("1e131071").add(pinf), 
             pinf,
             0, "Add #40");

        test(ninf.add(field.newDfp("0")), 
             ninf,
             0, "Add #41");

        test(ninf.add(field.newDfp("-1e131071")), 
             ninf,
             0, "Add #42");

        test(ninf.add(field.newDfp("1e131071")), 
             ninf,
             0, "Add #43");

        test(field.newDfp("0").add(ninf), 
             ninf,
             0, "Add #44");

        test(field.newDfp("-1e131071").add(ninf), 
             ninf,
             0, "Add #45");

        test(field.newDfp("1e131071").add(ninf), 
             ninf,
             0, "Add #46");

        test(field.newDfp("9.9999999999999999999e131071").add(field.newDfp("5e131051")),  
             pinf,
             DfpField.FLAG_OVERFLOW, "Add #47");

        test(field.newDfp("9.9999999999999999999e131071").add(field.newDfp("4.9999999999999999999e131051")),  
             field.newDfp("9.9999999999999999999e131071"),
             DfpField.FLAG_INEXACT, "Add #48");

        test(nan.add(field.newDfp("1")),
             nan,
             0, "Add #49");

        test(field.newDfp("1").add(nan),
             nan,
             0, "Add #50");

        test(field.newDfp("12345678123456781234").add(field.newDfp("0.12345678123456781234")),
             field.newDfp("12345678123456781234"),
             DfpField.FLAG_INEXACT, "Add #51");

        test(field.newDfp("12345678123456781234").add(field.newDfp("123.45678123456781234")),
             field.newDfp("12345678123456781357"),
             DfpField.FLAG_INEXACT, "Add #52");

        test(field.newDfp("123.45678123456781234").add(field.newDfp("12345678123456781234")),
             field.newDfp("12345678123456781357"),
             DfpField.FLAG_INEXACT, "Add #53");

        test(field.newDfp("12345678123456781234").add(field.newDfp(".00001234567812345678")),
             field.newDfp("12345678123456781234"),
             DfpField.FLAG_INEXACT, "Add #54");

        test(field.newDfp("12345678123456781234").add(field.newDfp(".00000000123456781234")),
             field.newDfp("12345678123456781234"),
             DfpField.FLAG_INEXACT, "Add #55");

        test(field.newDfp("-0").add(field.newDfp("-0")),
             field.newDfp("-0"),
             0, "Add #56");

        test(field.newDfp("0").add(field.newDfp("-0")),
             field.newDfp("0"),
             0, "Add #57");

        test(field.newDfp("-0").add(field.newDfp("0")),
             field.newDfp("0"),
             0, "Add #58");

        test(field.newDfp("0").add(field.newDfp("0")),
             field.newDfp("0"),
             0, "Add #59");
    }

// org.apache.commons.math3.dfp.DfpTest::testCompare
    public void  testCompare()
    {
        
        
        field.clearIEEEFlags();

        cmptst(field.newDfp("0"), field.newDfp("0"), "equal", true, 1);         
        cmptst(field.newDfp("0"), field.newDfp("-0"), "equal", true, 2);        
        cmptst(field.newDfp("-0"), field.newDfp("-0"), "equal", true, 3);       
        cmptst(field.newDfp("-0"), field.newDfp("0"), "equal", true, 4);        

        

        cmptst(field.newDfp("0"), field.newDfp("1"), "equal", false, 5);         
        cmptst(field.newDfp("1"), field.newDfp("0"), "equal", false, 6);         
        cmptst(field.newDfp("-1"), field.newDfp("0"), "equal", false, 7);        
        cmptst(field.newDfp("0"), field.newDfp("-1"), "equal", false, 8);        
        cmptst(field.newDfp("0"), field.newDfp("1e-131072"), "equal", false, 9); 
        
        if (field.getIEEEFlags() != 0)
            Assert.fail("assersion failed.  compare flags = "+field.getIEEEFlags());

        cmptst(field.newDfp("0"), field.newDfp("1e-131078"), "equal", false, 10); 

        
        if (field.getIEEEFlags() != DfpField.FLAG_UNDERFLOW)
            Assert.fail("assersion failed.  compare flags = "+field.getIEEEFlags());

        field.clearIEEEFlags();

        cmptst(field.newDfp("0"), field.newDfp("1e+131071"), "equal", false, 11); 

        

        cmptst(field.newDfp("0"), pinf, "equal", false, 12);    
        cmptst(field.newDfp("0"), ninf, "equal", false, 13);    
        cmptst(field.newDfp("-0"), pinf, "equal", false, 14);   
        cmptst(field.newDfp("-0"), ninf, "equal", false, 15);   
        cmptst(pinf, field.newDfp("0"), "equal", false, 16);    
        cmptst(ninf, field.newDfp("0"), "equal", false, 17);    
        cmptst(pinf, field.newDfp("-0"), "equal", false, 18);   
        cmptst(ninf, field.newDfp("-0"), "equal", false, 19);   
        cmptst(ninf, pinf, "equal", false, 19.10);     
        cmptst(pinf, ninf, "equal", false, 19.11);     
        cmptst(pinf, pinf, "equal", true, 19.12);     
        cmptst(ninf, ninf, "equal", true, 19.13);     

        
        cmptst(field.newDfp("1"), field.newDfp("1"), "equal", true, 20);   
        cmptst(field.newDfp("1"), field.newDfp("-1"), "equal", false, 21);   
        cmptst(field.newDfp("-1"), field.newDfp("-1"), "equal", true, 22);   
        cmptst(field.newDfp("1"), field.newDfp("1.0000000000000001"), "equal", false, 23);   

        
        
        cmptst(field.newDfp("1e20"), field.newDfp("1.0000000000000001"), "equal", false, 24);
        if (field.getIEEEFlags() != 0)
            Assert.fail("assersion failed.  compare flags = "+field.getIEEEFlags());

        cmptst(field.newDfp("0.000001"), field.newDfp("1e-6"), "equal", true, 25);

        

        cmptst(snan, snan, "equal", false, 27);
        cmptst(qnan, qnan, "equal", false, 28);
        cmptst(snan, qnan, "equal", false, 29);
        cmptst(qnan, snan, "equal", false, 30);
        cmptst(qnan, field.newDfp("0"), "equal", false, 31);
        cmptst(snan, field.newDfp("0"), "equal", false, 32);
        cmptst(field.newDfp("0"), snan, "equal", false, 33);
        cmptst(field.newDfp("0"), qnan, "equal", false, 34);
        cmptst(qnan, pinf, "equal", false, 35);
        cmptst(snan, pinf, "equal", false, 36);
        cmptst(pinf, snan, "equal", false, 37);
        cmptst(pinf, qnan, "equal", false, 38);
        cmptst(qnan, ninf, "equal", false, 39);
        cmptst(snan, ninf, "equal", false, 40);
        cmptst(ninf, snan, "equal", false, 41);
        cmptst(ninf, qnan, "equal", false, 42);
        cmptst(qnan, field.newDfp("-1"), "equal", false, 43);
        cmptst(snan, field.newDfp("-1"), "equal", false, 44);
        cmptst(field.newDfp("-1"), snan, "equal", false, 45);
        cmptst(field.newDfp("-1"), qnan, "equal", false, 46);
        cmptst(qnan, field.newDfp("1"), "equal", false, 47);
        cmptst(snan, field.newDfp("1"), "equal", false, 48);
        cmptst(field.newDfp("1"), snan, "equal", false, 49);
        cmptst(field.newDfp("1"), qnan, "equal", false, 50);
        cmptst(snan.negate(), snan, "equal", false, 51);
        cmptst(qnan.negate(), qnan, "equal", false, 52);

        
        
        

        cmptst(field.newDfp("0"), field.newDfp("0"), "unequal", false, 1);         
        cmptst(field.newDfp("0"), field.newDfp("-0"), "unequal", false, 2);        
        cmptst(field.newDfp("-0"), field.newDfp("-0"), "unequal", false, 3);       
        cmptst(field.newDfp("-0"), field.newDfp("0"), "unequal", false, 4);        

        

        cmptst(field.newDfp("0"), field.newDfp("1"), "unequal", true, 5);         
        cmptst(field.newDfp("1"), field.newDfp("0"), "unequal", true, 6);         
        cmptst(field.newDfp("-1"), field.newDfp("0"), "unequal", true, 7);        
        cmptst(field.newDfp("0"), field.newDfp("-1"), "unequal", true, 8);        
        cmptst(field.newDfp("0"), field.newDfp("1e-131072"), "unequal", true, 9); 
        
        if (field.getIEEEFlags() != 0)
            Assert.fail("assersion failed.  compare flags = "+field.getIEEEFlags());

        cmptst(field.newDfp("0"), field.newDfp("1e-131078"), "unequal", true, 10); 

        
        if (field.getIEEEFlags() != DfpField.FLAG_UNDERFLOW)
            Assert.fail("assersion failed.  compare flags = "+field.getIEEEFlags());

        field.clearIEEEFlags();

        cmptst(field.newDfp("0"), field.newDfp("1e+131071"), "unequal", true, 11); 

        

        cmptst(field.newDfp("0"), pinf, "unequal", true, 12);    
        cmptst(field.newDfp("0"), ninf, "unequal", true, 13);    
        cmptst(field.newDfp("-0"), pinf, "unequal", true, 14);   
        cmptst(field.newDfp("-0"), ninf, "unequal", true, 15);   
        cmptst(pinf, field.newDfp("0"), "unequal", true, 16);    
        cmptst(ninf, field.newDfp("0"), "unequal", true, 17);    
        cmptst(pinf, field.newDfp("-0"), "unequal", true, 18);   
        cmptst(ninf, field.newDfp("-0"), "unequal", true, 19);   
        cmptst(ninf, pinf, "unequal", true, 19.10);     
        cmptst(pinf, ninf, "unequal", true, 19.11);     
        cmptst(pinf, pinf, "unequal", false, 19.12);     
        cmptst(ninf, ninf, "unequal", false, 19.13);     

        
        cmptst(field.newDfp("1"), field.newDfp("1"), "unequal", false, 20);   
        cmptst(field.newDfp("1"), field.newDfp("-1"), "unequal", true, 21);   
        cmptst(field.newDfp("-1"), field.newDfp("-1"), "unequal", false, 22);   
        cmptst(field.newDfp("1"), field.newDfp("1.0000000000000001"), "unequal", true, 23);   

        
        
        cmptst(field.newDfp("1e20"), field.newDfp("1.0000000000000001"), "unequal", true, 24);
        if (field.getIEEEFlags() != 0)
            Assert.fail("assersion failed.  compare flags = "+field.getIEEEFlags());

        cmptst(field.newDfp("0.000001"), field.newDfp("1e-6"), "unequal", false, 25);

        

        cmptst(snan, snan, "unequal", false, 27);
        cmptst(qnan, qnan, "unequal", false, 28);
        cmptst(snan, qnan, "unequal", false, 29);
        cmptst(qnan, snan, "unequal", false, 30);
        cmptst(qnan, field.newDfp("0"), "unequal", false, 31);
        cmptst(snan, field.newDfp("0"), "unequal", false, 32);
        cmptst(field.newDfp("0"), snan, "unequal", false, 33);
        cmptst(field.newDfp("0"), qnan, "unequal", false, 34);
        cmptst(qnan, pinf, "unequal", false, 35);
        cmptst(snan, pinf, "unequal", false, 36);
        cmptst(pinf, snan, "unequal", false, 37);
        cmptst(pinf, qnan, "unequal", false, 38);
        cmptst(qnan, ninf, "unequal", false, 39);
        cmptst(snan, ninf, "unequal", false, 40);
        cmptst(ninf, snan, "unequal", false, 41);
        cmptst(ninf, qnan, "unequal", false, 42);
        cmptst(qnan, field.newDfp("-1"), "unequal", false, 43);
        cmptst(snan, field.newDfp("-1"), "unequal", false, 44);
        cmptst(field.newDfp("-1"), snan, "unequal", false, 45);
        cmptst(field.newDfp("-1"), qnan, "unequal", false, 46);
        cmptst(qnan, field.newDfp("1"), "unequal", false, 47);
        cmptst(snan, field.newDfp("1"), "unequal", false, 48);
        cmptst(field.newDfp("1"), snan, "unequal", false, 49);
        cmptst(field.newDfp("1"), qnan, "unequal", false, 50);
        cmptst(snan.negate(), snan, "unequal", false, 51);
        cmptst(qnan.negate(), qnan, "unequal", false, 52);

        if (field.getIEEEFlags() != 0)
            Assert.fail("assersion failed.  compare unequal flags = "+field.getIEEEFlags());

        
        
        

        cmptst(field.newDfp("0"), field.newDfp("0"), "lessThan", false, 1);         
        cmptst(field.newDfp("0"), field.newDfp("-0"), "lessThan", false, 2);        
        cmptst(field.newDfp("-0"), field.newDfp("-0"), "lessThan", false, 3);       
        cmptst(field.newDfp("-0"), field.newDfp("0"), "lessThan", false, 4);        

        

        cmptst(field.newDfp("0"), field.newDfp("1"), "lessThan", true, 5);         
        cmptst(field.newDfp("1"), field.newDfp("0"), "lessThan", false, 6);         
        cmptst(field.newDfp("-1"), field.newDfp("0"), "lessThan", true, 7);        
        cmptst(field.newDfp("0"), field.newDfp("-1"), "lessThan", false, 8);        
        cmptst(field.newDfp("0"), field.newDfp("1e-131072"), "lessThan", true, 9); 
        
        if (field.getIEEEFlags() != 0)
            Assert.fail("assersion failed.  compare flags = "+field.getIEEEFlags());

        cmptst(field.newDfp("0"), field.newDfp("1e-131078"), "lessThan", true, 10); 

        
        if (field.getIEEEFlags() != DfpField.FLAG_UNDERFLOW)
            Assert.fail("assersion failed.  compare flags = "+field.getIEEEFlags());
        field.clearIEEEFlags();

        cmptst(field.newDfp("0"), field.newDfp("1e+131071"), "lessThan", true, 11); 

        

        cmptst(field.newDfp("0"), pinf, "lessThan", true, 12);    
        cmptst(field.newDfp("0"), ninf, "lessThan", false, 13);    
        cmptst(field.newDfp("-0"), pinf, "lessThan", true, 14);   
        cmptst(field.newDfp("-0"), ninf, "lessThan", false, 15);   
        cmptst(pinf, field.newDfp("0"), "lessThan", false, 16);    
        cmptst(ninf, field.newDfp("0"), "lessThan", true, 17);    
        cmptst(pinf, field.newDfp("-0"), "lessThan", false, 18);   
        cmptst(ninf, field.newDfp("-0"), "lessThan", true, 19);   
        cmptst(ninf, pinf, "lessThan", true, 19.10);     
        cmptst(pinf, ninf, "lessThan", false, 19.11);     
        cmptst(pinf, pinf, "lessThan", false, 19.12);     
        cmptst(ninf, ninf, "lessThan", false, 19.13);     

        
        cmptst(field.newDfp("1"), field.newDfp("1"), "lessThan", false, 20);   
        cmptst(field.newDfp("1"), field.newDfp("-1"), "lessThan", false, 21);   
        cmptst(field.newDfp("-1"), field.newDfp("-1"), "lessThan", false, 22);   
        cmptst(field.newDfp("1"), field.newDfp("1.0000000000000001"), "lessThan", true, 23);   

        
        
        cmptst(field.newDfp("1e20"), field.newDfp("1.0000000000000001"), "lessThan", false, 24);
        if (field.getIEEEFlags() != 0)
            Assert.fail("assersion failed.  compare flags = "+field.getIEEEFlags());

        cmptst(field.newDfp("0.000001"), field.newDfp("1e-6"), "lessThan", false, 25);

        
        cmptst(snan, snan, "lessThan", false, 27);
        cmptst(qnan, qnan, "lessThan", false, 28);
        cmptst(snan, qnan, "lessThan", false, 29);
        cmptst(qnan, snan, "lessThan", false, 30);
        cmptst(qnan, field.newDfp("0"), "lessThan", false, 31);
        cmptst(snan, field.newDfp("0"), "lessThan", false, 32);
        cmptst(field.newDfp("0"), snan, "lessThan", false, 33);
        cmptst(field.newDfp("0"), qnan, "lessThan", false, 34);
        cmptst(qnan, pinf, "lessThan", false, 35);
        cmptst(snan, pinf, "lessThan", false, 36);
        cmptst(pinf, snan, "lessThan", false, 37);
        cmptst(pinf, qnan, "lessThan", false, 38);
        cmptst(qnan, ninf, "lessThan", false, 39);
        cmptst(snan, ninf, "lessThan", false, 40);
        cmptst(ninf, snan, "lessThan", false, 41);
        cmptst(ninf, qnan, "lessThan", false, 42);
        cmptst(qnan, field.newDfp("-1"), "lessThan", false, 43);
        cmptst(snan, field.newDfp("-1"), "lessThan", false, 44);
        cmptst(field.newDfp("-1"), snan, "lessThan", false, 45);
        cmptst(field.newDfp("-1"), qnan, "lessThan", false, 46);
        cmptst(qnan, field.newDfp("1"), "lessThan", false, 47);
        cmptst(snan, field.newDfp("1"), "lessThan", false, 48);
        cmptst(field.newDfp("1"), snan, "lessThan", false, 49);
        cmptst(field.newDfp("1"), qnan, "lessThan", false, 50);
        cmptst(snan.negate(), snan, "lessThan", false, 51);
        cmptst(qnan.negate(), qnan, "lessThan", false, 52);

        
        if (field.getIEEEFlags() != DfpField.FLAG_INVALID)
            Assert.fail("assersion failed.  compare lessThan flags = "+field.getIEEEFlags());
        field.clearIEEEFlags();

        
        
        

        cmptst(field.newDfp("0"), field.newDfp("0"), "greaterThan", false, 1);         
        cmptst(field.newDfp("0"), field.newDfp("-0"), "greaterThan", false, 2);        
        cmptst(field.newDfp("-0"), field.newDfp("-0"), "greaterThan", false, 3);       
        cmptst(field.newDfp("-0"), field.newDfp("0"), "greaterThan", false, 4);        

        

        cmptst(field.newDfp("0"), field.newDfp("1"), "greaterThan", false, 5);         
        cmptst(field.newDfp("1"), field.newDfp("0"), "greaterThan", true, 6);         
        cmptst(field.newDfp("-1"), field.newDfp("0"), "greaterThan", false, 7);        
        cmptst(field.newDfp("0"), field.newDfp("-1"), "greaterThan", true, 8);        
        cmptst(field.newDfp("0"), field.newDfp("1e-131072"), "greaterThan", false, 9); 
        
        if (field.getIEEEFlags() != 0)
            Assert.fail("assersion failed.  compare flags = "+field.getIEEEFlags());

        cmptst(field.newDfp("0"), field.newDfp("1e-131078"), "greaterThan", false, 10); 

        
        if (field.getIEEEFlags() != DfpField.FLAG_UNDERFLOW)
            Assert.fail("assersion failed.  compare flags = "+field.getIEEEFlags());
        field.clearIEEEFlags();

        cmptst(field.newDfp("0"), field.newDfp("1e+131071"), "greaterThan", false, 11); 

        

        cmptst(field.newDfp("0"), pinf, "greaterThan", false, 12);    
        cmptst(field.newDfp("0"), ninf, "greaterThan", true, 13);    
        cmptst(field.newDfp("-0"), pinf, "greaterThan", false, 14);   
        cmptst(field.newDfp("-0"), ninf, "greaterThan", true, 15);   
        cmptst(pinf, field.newDfp("0"), "greaterThan", true, 16);    
        cmptst(ninf, field.newDfp("0"), "greaterThan", false, 17);    
        cmptst(pinf, field.newDfp("-0"), "greaterThan", true, 18);   
        cmptst(ninf, field.newDfp("-0"), "greaterThan", false, 19);   
        cmptst(ninf, pinf, "greaterThan", false, 19.10);     
        cmptst(pinf, ninf, "greaterThan", true, 19.11);     
        cmptst(pinf, pinf, "greaterThan", false, 19.12);     
        cmptst(ninf, ninf, "greaterThan", false, 19.13);     

        
        cmptst(field.newDfp("1"), field.newDfp("1"), "greaterThan", false, 20);   
        cmptst(field.newDfp("1"), field.newDfp("-1"), "greaterThan", true, 21);   
        cmptst(field.newDfp("-1"), field.newDfp("-1"), "greaterThan", false, 22);   
        cmptst(field.newDfp("1"), field.newDfp("1.0000000000000001"), "greaterThan", false, 23);   

        
        
        cmptst(field.newDfp("1e20"), field.newDfp("1.0000000000000001"), "greaterThan", true, 24);
        if (field.getIEEEFlags() != 0)
            Assert.fail("assersion failed.  compare flags = "+field.getIEEEFlags());

        cmptst(field.newDfp("0.000001"), field.newDfp("1e-6"), "greaterThan", false, 25);

        
        cmptst(snan, snan, "greaterThan", false, 27);
        cmptst(qnan, qnan, "greaterThan", false, 28);
        cmptst(snan, qnan, "greaterThan", false, 29);
        cmptst(qnan, snan, "greaterThan", false, 30);
        cmptst(qnan, field.newDfp("0"), "greaterThan", false, 31);
        cmptst(snan, field.newDfp("0"), "greaterThan", false, 32);
        cmptst(field.newDfp("0"), snan, "greaterThan", false, 33);
        cmptst(field.newDfp("0"), qnan, "greaterThan", false, 34);
        cmptst(qnan, pinf, "greaterThan", false, 35);
        cmptst(snan, pinf, "greaterThan", false, 36);
        cmptst(pinf, snan, "greaterThan", false, 37);
        cmptst(pinf, qnan, "greaterThan", false, 38);
        cmptst(qnan, ninf, "greaterThan", false, 39);
        cmptst(snan, ninf, "greaterThan", false, 40);
        cmptst(ninf, snan, "greaterThan", false, 41);
        cmptst(ninf, qnan, "greaterThan", false, 42);
        cmptst(qnan, field.newDfp("-1"), "greaterThan", false, 43);
        cmptst(snan, field.newDfp("-1"), "greaterThan", false, 44);
        cmptst(field.newDfp("-1"), snan, "greaterThan", false, 45);
        cmptst(field.newDfp("-1"), qnan, "greaterThan", false, 46);
        cmptst(qnan, field.newDfp("1"), "greaterThan", false, 47);
        cmptst(snan, field.newDfp("1"), "greaterThan", false, 48);
        cmptst(field.newDfp("1"), snan, "greaterThan", false, 49);
        cmptst(field.newDfp("1"), qnan, "greaterThan", false, 50);
        cmptst(snan.negate(), snan, "greaterThan", false, 51);
        cmptst(qnan.negate(), qnan, "greaterThan", false, 52);

        
        if (field.getIEEEFlags() != DfpField.FLAG_INVALID)
            Assert.fail("assersion failed.  compare greaterThan flags = "+field.getIEEEFlags());
        field.clearIEEEFlags();
    }

// org.apache.commons.math3.dfp.DfpTest::testMultiply
    public void testMultiply()
    {
        test(field.newDfp("1").multiply(field.newDfp("1")),      
             field.newDfp("1"),
             0, "Multiply #1");

        test(field.newDfp("1").multiply(1),             
             field.newDfp("1"),
             0, "Multiply #2");

        test(field.newDfp("-1").multiply(field.newDfp("1")),     
             field.newDfp("-1"),
             0, "Multiply #3");

        test(field.newDfp("-1").multiply(1),            
             field.newDfp("-1"),
             0, "Multiply #4");

        
        test(field.newDfp("2").multiply(field.newDfp("3")),
             field.newDfp("6"),
             0, "Multiply #5");

        test(field.newDfp("2").multiply(3),
             field.newDfp("6"),
             0, "Multiply #6");

        test(field.newDfp("-2").multiply(field.newDfp("3")),
             field.newDfp("-6"),
             0, "Multiply #7");

        test(field.newDfp("-2").multiply(3),
             field.newDfp("-6"),
             0, "Multiply #8");

        test(field.newDfp("2").multiply(field.newDfp("-3")),
             field.newDfp("-6"),
             0, "Multiply #9");

        test(field.newDfp("-2").multiply(field.newDfp("-3")),
             field.newDfp("6"),
             0, "Multiply #10");

        

        test(field.newDfp("-2").multiply(field.newDfp("0")),
             field.newDfp("-0"),
             0, "Multiply #11");

        test(field.newDfp("-2").multiply(0),
             field.newDfp("-0"),
             0, "Multiply #12");

        test(field.newDfp("2").multiply(field.newDfp("0")),
             field.newDfp("0"),
             0, "Multiply #13");

        test(field.newDfp("2").multiply(0),
             field.newDfp("0"),
             0, "Multiply #14");

        test(field.newDfp("2").multiply(pinf),
             pinf,
             0, "Multiply #15");

        test(field.newDfp("2").multiply(ninf),
             ninf,
             0, "Multiply #16");

        test(field.newDfp("-2").multiply(pinf),
             ninf,
             0, "Multiply #17");

        test(field.newDfp("-2").multiply(ninf),
             pinf,
             0, "Multiply #18");

        test(ninf.multiply(field.newDfp("-2")),
             pinf,
             0, "Multiply #18.1");

        test(field.newDfp("5e131071").multiply(2),
             pinf,
             DfpField.FLAG_OVERFLOW, "Multiply #19");

        test(field.newDfp("5e131071").multiply(field.newDfp("1.999999999999999")),
             field.newDfp("9.9999999999999950000e131071"),
             0, "Multiply #20");

        test(field.newDfp("-5e131071").multiply(2),
             ninf,
             DfpField.FLAG_OVERFLOW, "Multiply #22");

        test(field.newDfp("-5e131071").multiply(field.newDfp("1.999999999999999")),
             field.newDfp("-9.9999999999999950000e131071"),
             0, "Multiply #23");

        test(field.newDfp("1e-65539").multiply(field.newDfp("1e-65539")),
             field.newDfp("1e-131078"),
             DfpField.FLAG_UNDERFLOW, "Multiply #24");

        test(field.newDfp("1").multiply(nan),
             nan,
             0, "Multiply #25");

        test(nan.multiply(field.newDfp("1")),
             nan,
             0, "Multiply #26");

        test(nan.multiply(pinf),
             nan,
             0, "Multiply #27");

        test(pinf.multiply(nan),
             nan,
             0, "Multiply #27");

        test(pinf.multiply(field.newDfp("0")),
             nan,
             DfpField.FLAG_INVALID, "Multiply #28");

        test(field.newDfp("0").multiply(pinf),
             nan,
             DfpField.FLAG_INVALID, "Multiply #29");

        test(pinf.multiply(pinf),
             pinf,
             0, "Multiply #30");

        test(ninf.multiply(pinf),
             ninf,
             0, "Multiply #31");

        test(pinf.multiply(ninf),
             ninf,
             0, "Multiply #32");

        test(ninf.multiply(ninf),
             pinf,
             0, "Multiply #33");

        test(pinf.multiply(1),
             pinf,
             0, "Multiply #34");

        test(pinf.multiply(0),
             nan,
             DfpField.FLAG_INVALID, "Multiply #35");

        test(nan.multiply(1),
             nan,
             0, "Multiply #36");

        test(field.newDfp("1").multiply(10000),
             field.newDfp("10000"),
             0, "Multiply #37");

        test(field.newDfp("2").multiply(1000000),
             field.newDfp("2000000"),
             0, "Multiply #38");

        test(field.newDfp("1").multiply(-1),
             field.newDfp("-1"),
             0, "Multiply #39");
    }

// org.apache.commons.math3.dfp.DfpTest::testDivide
    public void testDivide()
    {
        test(field.newDfp("1").divide(nan),      
             nan,
             0, "Divide #1");

        test(nan.divide(field.newDfp("1")),      
             nan,
             0, "Divide #2");

        test(pinf.divide(field.newDfp("1")),
             pinf,
             0, "Divide #3");

        test(pinf.divide(field.newDfp("-1")),
             ninf,
             0, "Divide #4");

        test(pinf.divide(pinf),
             nan,
             DfpField.FLAG_INVALID, "Divide #5");

        test(ninf.divide(pinf),
             nan,
             DfpField.FLAG_INVALID, "Divide #6");

        test(pinf.divide(ninf),
             nan,
             DfpField.FLAG_INVALID, "Divide #7");

        test(ninf.divide(ninf),
             nan,
             DfpField.FLAG_INVALID, "Divide #8");

        test(field.newDfp("0").divide(field.newDfp("0")),
             nan,
             DfpField.FLAG_DIV_ZERO, "Divide #9");

        test(field.newDfp("1").divide(field.newDfp("0")),
             pinf,
             DfpField.FLAG_DIV_ZERO, "Divide #10");

        test(field.newDfp("1").divide(field.newDfp("-0")),
             ninf,
             DfpField.FLAG_DIV_ZERO, "Divide #11");

        test(field.newDfp("-1").divide(field.newDfp("0")),
             ninf,
             DfpField.FLAG_DIV_ZERO, "Divide #12");

        test(field.newDfp("-1").divide(field.newDfp("-0")),
             pinf,
             DfpField.FLAG_DIV_ZERO, "Divide #13");

        test(field.newDfp("1").divide(field.newDfp("3")),
             field.newDfp("0.33333333333333333333"),
             DfpField.FLAG_INEXACT, "Divide #14");

        test(field.newDfp("1").divide(field.newDfp("6")),
             field.newDfp("0.16666666666666666667"),
             DfpField.FLAG_INEXACT, "Divide #15");

        test(field.newDfp("10").divide(field.newDfp("6")),
             field.newDfp("1.6666666666666667"),
             DfpField.FLAG_INEXACT, "Divide #16");

        test(field.newDfp("100").divide(field.newDfp("6")),
             field.newDfp("16.6666666666666667"),
             DfpField.FLAG_INEXACT, "Divide #17");

        test(field.newDfp("1000").divide(field.newDfp("6")),
             field.newDfp("166.6666666666666667"),
             DfpField.FLAG_INEXACT, "Divide #18");

        test(field.newDfp("10000").divide(field.newDfp("6")),
             field.newDfp("1666.6666666666666667"),
             DfpField.FLAG_INEXACT, "Divide #19");

        test(field.newDfp("1").divide(field.newDfp("1")),
             field.newDfp("1"),
             0, "Divide #20");

        test(field.newDfp("1").divide(field.newDfp("-1")),
             field.newDfp("-1"),
             0, "Divide #21");

        test(field.newDfp("-1").divide(field.newDfp("1")),
             field.newDfp("-1"),
             0, "Divide #22");

        test(field.newDfp("-1").divide(field.newDfp("-1")),
             field.newDfp("1"),
             0, "Divide #23");

        test(field.newDfp("1e-65539").divide(field.newDfp("1e65539")),
             field.newDfp("1e-131078"),
             DfpField.FLAG_UNDERFLOW, "Divide #24");

        test(field.newDfp("1e65539").divide(field.newDfp("1e-65539")),
             pinf,
             DfpField.FLAG_OVERFLOW, "Divide #24");

        test(field.newDfp("2").divide(field.newDfp("1.5")),     
             field.newDfp("1.3333333333333333"),
             DfpField.FLAG_INEXACT, "Divide #25");

        test(field.newDfp("2").divide(pinf),
             field.newDfp("0"),
             0, "Divide #26");

        test(field.newDfp("2").divide(ninf),
             field.newDfp("-0"),
             0, "Divide #27");

        test(field.newDfp("0").divide(field.newDfp("1")),
             field.newDfp("0"),
             0, "Divide #28");
    }

// org.apache.commons.math3.dfp.DfpTest::testReciprocal
    public void testReciprocal()
    {
        test(nan.reciprocal(),
             nan,
             0, "Reciprocal #1");

        test(field.newDfp("0").reciprocal(),
             pinf,
             DfpField.FLAG_DIV_ZERO, "Reciprocal #2");

        test(field.newDfp("-0").reciprocal(),
             ninf,
             DfpField.FLAG_DIV_ZERO, "Reciprocal #3");

        test(field.newDfp("3").reciprocal(),
             field.newDfp("0.33333333333333333333"),
             DfpField.FLAG_INEXACT, "Reciprocal #4");

        test(field.newDfp("6").reciprocal(),
             field.newDfp("0.16666666666666666667"),
             DfpField.FLAG_INEXACT, "Reciprocal #5");

        test(field.newDfp("1").reciprocal(),
             field.newDfp("1"),
             0, "Reciprocal #6");

        test(field.newDfp("-1").reciprocal(),
             field.newDfp("-1"),
             0, "Reciprocal #7");

        test(pinf.reciprocal(),
             field.newDfp("0"),
             0, "Reciprocal #8");

        test(ninf.reciprocal(),
             field.newDfp("-0"),
             0, "Reciprocal #9");
    }

// org.apache.commons.math3.dfp.DfpTest::testDivideInt
    public void testDivideInt()
    {
        test(nan.divide(1),      
             nan,
             0, "DivideInt #1");

        test(pinf.divide(1),
             pinf,
             0, "DivideInt #2");

        test(field.newDfp("0").divide(0),
             nan,
             DfpField.FLAG_DIV_ZERO, "DivideInt #3");

        test(field.newDfp("1").divide(0),
             pinf,
             DfpField.FLAG_DIV_ZERO, "DivideInt #4");

        test(field.newDfp("-1").divide(0),
             ninf,
             DfpField.FLAG_DIV_ZERO, "DivideInt #5");

        test(field.newDfp("1").divide(3),
             field.newDfp("0.33333333333333333333"),
             DfpField.FLAG_INEXACT, "DivideInt #6");

        test(field.newDfp("1").divide(6),
             field.newDfp("0.16666666666666666667"),
             DfpField.FLAG_INEXACT, "DivideInt #7");

        test(field.newDfp("10").divide(6),
             field.newDfp("1.6666666666666667"),
             DfpField.FLAG_INEXACT, "DivideInt #8");

        test(field.newDfp("100").divide(6),
             field.newDfp("16.6666666666666667"),
             DfpField.FLAG_INEXACT, "DivideInt #9");

        test(field.newDfp("1000").divide(6),
             field.newDfp("166.6666666666666667"),
             DfpField.FLAG_INEXACT, "DivideInt #10");

        test(field.newDfp("10000").divide(6),
             field.newDfp("1666.6666666666666667"),
             DfpField.FLAG_INEXACT, "DivideInt #20");

        test(field.newDfp("1").divide(1),
             field.newDfp("1"),
             0, "DivideInt #21");

        test(field.newDfp("1e-131077").divide(10),
             field.newDfp("1e-131078"),
             DfpField.FLAG_UNDERFLOW, "DivideInt #22");

        test(field.newDfp("0").divide(1),
             field.newDfp("0"),
             0, "DivideInt #23");

        test(field.newDfp("1").divide(10000),
             nan,
             DfpField.FLAG_INVALID, "DivideInt #24");

        test(field.newDfp("1").divide(-1),
             nan,
             DfpField.FLAG_INVALID, "DivideInt #25");
    }

// org.apache.commons.math3.dfp.DfpTest::testNextAfter
    public void testNextAfter()
    {
        test(field.newDfp("1").nextAfter(pinf),
             field.newDfp("1.0000000000000001"),
             0, "NextAfter #1");

        test(field.newDfp("1.0000000000000001").nextAfter(ninf),
             field.newDfp("1"),
             0, "NextAfter #1.5");

        test(field.newDfp("1").nextAfter(ninf),
             field.newDfp("0.99999999999999999999"),
             0, "NextAfter #2");

        test(field.newDfp("0.99999999999999999999").nextAfter(field.newDfp("2")),
             field.newDfp("1"),
             0, "NextAfter #3");

        test(field.newDfp("-1").nextAfter(ninf),
             field.newDfp("-1.0000000000000001"),
             0, "NextAfter #4");

        test(field.newDfp("-1").nextAfter(pinf),
             field.newDfp("-0.99999999999999999999"),
             0, "NextAfter #5");

        test(field.newDfp("-0.99999999999999999999").nextAfter(field.newDfp("-2")),
             field.newDfp("-1"),
             0, "NextAfter #6");

        test(field.newDfp("2").nextAfter(field.newDfp("2")),
             field.newDfp("2"),
             0, "NextAfter #7");

        test(field.newDfp("0").nextAfter(field.newDfp("0")),
             field.newDfp("0"),
             0, "NextAfter #8");

        test(field.newDfp("-2").nextAfter(field.newDfp("-2")),
             field.newDfp("-2"),
             0, "NextAfter #9");

        test(field.newDfp("0").nextAfter(field.newDfp("1")),
             field.newDfp("1e-131092"),
             DfpField.FLAG_UNDERFLOW, "NextAfter #10");

        test(field.newDfp("0").nextAfter(field.newDfp("-1")),
             field.newDfp("-1e-131092"),
             DfpField.FLAG_UNDERFLOW, "NextAfter #11");

        test(field.newDfp("-1e-131092").nextAfter(pinf),
             field.newDfp("-0"),
             DfpField.FLAG_UNDERFLOW|DfpField.FLAG_INEXACT, "Next After #12");

        test(field.newDfp("1e-131092").nextAfter(ninf),
             field.newDfp("0"),
             DfpField.FLAG_UNDERFLOW|DfpField.FLAG_INEXACT, "Next After #13");

        test(field.newDfp("9.9999999999999999999e131078").nextAfter(pinf),
             pinf,
             DfpField.FLAG_OVERFLOW|DfpField.FLAG_INEXACT, "Next After #14");
    }

// org.apache.commons.math3.dfp.DfpTest::testToString
    public void testToString()
    {
        Assert.assertEquals("toString #1", "Infinity", pinf.toString());
        Assert.assertEquals("toString #2", "-Infinity", ninf.toString());
        Assert.assertEquals("toString #3", "NaN", nan.toString());
        Assert.assertEquals("toString #4", "NaN", field.newDfp((byte) 1, Dfp.QNAN).toString());
        Assert.assertEquals("toString #5", "NaN", field.newDfp((byte) 1, Dfp.SNAN).toString());
        Assert.assertEquals("toString #6", "1.2300000000000000e100", field.newDfp("1.23e100").toString());
        Assert.assertEquals("toString #7", "-1.2300000000000000e100", field.newDfp("-1.23e100").toString());
        Assert.assertEquals("toString #8", "12345678.1234", field.newDfp("12345678.1234").toString());
        Assert.assertEquals("toString #9", "0.00001234", field.newDfp("0.00001234").toString());
    }

// org.apache.commons.math3.dfp.DfpTest::testRound
    public void testRound()
    {
        field.setRoundingMode(DfpField.RoundingMode.ROUND_DOWN);

        
        test(field.newDfp("12345678901234567890").add(field.newDfp("0.9")),
             field.newDfp("12345678901234567890"),
             DfpField.FLAG_INEXACT, "Round #1");

        test(field.newDfp("12345678901234567890").add(field.newDfp("0.99999999")),
             field.newDfp("12345678901234567890"),
             DfpField.FLAG_INEXACT, "Round #2");

        test(field.newDfp("-12345678901234567890").add(field.newDfp("-0.99999999")),
             field.newDfp("-12345678901234567890"),
             DfpField.FLAG_INEXACT, "Round #3");

        field.setRoundingMode(DfpField.RoundingMode.ROUND_UP);

        
        test(field.newDfp("12345678901234567890").add(field.newDfp("0.1")),
             field.newDfp("12345678901234567891"),
             DfpField.FLAG_INEXACT, "Round #4");

        test(field.newDfp("12345678901234567890").add(field.newDfp("0.0001")),
             field.newDfp("12345678901234567891"),
             DfpField.FLAG_INEXACT, "Round #5");

        test(field.newDfp("-12345678901234567890").add(field.newDfp("-0.1")),
             field.newDfp("-12345678901234567891"),
             DfpField.FLAG_INEXACT, "Round #6");

        test(field.newDfp("-12345678901234567890").add(field.newDfp("-0.0001")),
             field.newDfp("-12345678901234567891"),
             DfpField.FLAG_INEXACT, "Round #7");

        field.setRoundingMode(DfpField.RoundingMode.ROUND_HALF_UP);

        
        test(field.newDfp("12345678901234567890").add(field.newDfp("0.4999")),
             field.newDfp("12345678901234567890"),
             DfpField.FLAG_INEXACT, "Round #8");

        test(field.newDfp("12345678901234567890").add(field.newDfp("0.5000")),
             field.newDfp("12345678901234567891"),
             DfpField.FLAG_INEXACT, "Round #9");

        test(field.newDfp("-12345678901234567890").add(field.newDfp("-0.4999")),
             field.newDfp("-12345678901234567890"),
             DfpField.FLAG_INEXACT, "Round #10");

        test(field.newDfp("-12345678901234567890").add(field.newDfp("-0.5000")),
             field.newDfp("-12345678901234567891"),
             DfpField.FLAG_INEXACT, "Round #11");

        field.setRoundingMode(DfpField.RoundingMode.ROUND_HALF_DOWN);

        
        test(field.newDfp("12345678901234567890").add(field.newDfp("0.5001")),
             field.newDfp("12345678901234567891"),
             DfpField.FLAG_INEXACT, "Round #12");

        test(field.newDfp("12345678901234567890").add(field.newDfp("0.5000")),
             field.newDfp("12345678901234567890"),
             DfpField.FLAG_INEXACT, "Round #13");

        test(field.newDfp("-12345678901234567890").add(field.newDfp("-0.5001")),
             field.newDfp("-12345678901234567891"),
             DfpField.FLAG_INEXACT, "Round #14");

        test(field.newDfp("-12345678901234567890").add(field.newDfp("-0.5000")),
             field.newDfp("-12345678901234567890"),
             DfpField.FLAG_INEXACT, "Round #15");

        field.setRoundingMode(DfpField.RoundingMode.ROUND_HALF_ODD);

        
        test(field.newDfp("12345678901234567890").add(field.newDfp("0.5000")),
             field.newDfp("12345678901234567891"),
             DfpField.FLAG_INEXACT, "Round #16");

        test(field.newDfp("12345678901234567891").add(field.newDfp("0.5000")),
             field.newDfp("12345678901234567891"),
             DfpField.FLAG_INEXACT, "Round #17");

        test(field.newDfp("-12345678901234567890").add(field.newDfp("-0.5000")),
             field.newDfp("-12345678901234567891"),
             DfpField.FLAG_INEXACT, "Round #18");

        test(field.newDfp("-12345678901234567891").add(field.newDfp("-0.5000")),
             field.newDfp("-12345678901234567891"),
             DfpField.FLAG_INEXACT, "Round #19");

        field.setRoundingMode(DfpField.RoundingMode.ROUND_CEIL);

        
        test(field.newDfp("12345678901234567890").add(field.newDfp("0.0001")),
             field.newDfp("12345678901234567891"),
             DfpField.FLAG_INEXACT, "Round #20");

        test(field.newDfp("-12345678901234567890").add(field.newDfp("-0.9999")),
             field.newDfp("-12345678901234567890"),
             DfpField.FLAG_INEXACT, "Round #21");

        field.setRoundingMode(DfpField.RoundingMode.ROUND_FLOOR);

        
        test(field.newDfp("12345678901234567890").add(field.newDfp("0.9999")),
             field.newDfp("12345678901234567890"),
             DfpField.FLAG_INEXACT, "Round #22");

        test(field.newDfp("-12345678901234567890").add(field.newDfp("-0.0001")),
             field.newDfp("-12345678901234567891"),
             DfpField.FLAG_INEXACT, "Round #23");

        field.setRoundingMode(DfpField.RoundingMode.ROUND_HALF_EVEN);  
    }

// org.apache.commons.math3.dfp.DfpTest::testCeil
    public void testCeil()
    {
        test(field.newDfp("1234.0000000000000001").ceil(),
             field.newDfp("1235"),
             DfpField.FLAG_INEXACT, "Ceil #1");
    }

// org.apache.commons.math3.dfp.DfpTest::testFloor
    public void testFloor()
    {
        test(field.newDfp("1234.9999999999999999").floor(),
             field.newDfp("1234"),
             DfpField.FLAG_INEXACT, "Floor #1");
    }

// org.apache.commons.math3.dfp.DfpTest::testRint
    public void testRint()
    {
        test(field.newDfp("1234.50000000001").rint(),
             field.newDfp("1235"),
             DfpField.FLAG_INEXACT, "Rint #1");

        test(field.newDfp("1234.5000").rint(),
             field.newDfp("1234"),
             DfpField.FLAG_INEXACT, "Rint #2");

        test(field.newDfp("1235.5000").rint(),
             field.newDfp("1236"),
             DfpField.FLAG_INEXACT, "Rint #3");
    }

// org.apache.commons.math3.dfp.DfpTest::testCopySign
    public void testCopySign()
    {
        test(Dfp.copysign(field.newDfp("1234."), field.newDfp("-1")),
             field.newDfp("-1234"),
             0, "CopySign #1");

        test(Dfp.copysign(field.newDfp("-1234."), field.newDfp("-1")),
             field.newDfp("-1234"),
             0, "CopySign #2");

        test(Dfp.copysign(field.newDfp("-1234."), field.newDfp("1")),
             field.newDfp("1234"),
             0, "CopySign #3");

        test(Dfp.copysign(field.newDfp("1234."), field.newDfp("1")),
             field.newDfp("1234"),
             0, "CopySign #4");
    }

// org.apache.commons.math3.dfp.DfpTest::testIntValue
    public void testIntValue()
    {
        Assert.assertEquals("intValue #1", 1234, field.newDfp("1234").intValue());
        Assert.assertEquals("intValue #2", -1234, field.newDfp("-1234").intValue());
        Assert.assertEquals("intValue #3", 1234, field.newDfp("1234.5").intValue());
        Assert.assertEquals("intValue #4", 1235, field.newDfp("1234.500001").intValue());
        Assert.assertEquals("intValue #5", 2147483647, field.newDfp("1e1000").intValue());
        Assert.assertEquals("intValue #6", -2147483648, field.newDfp("-1e1000").intValue());
    }

// org.apache.commons.math3.dfp.DfpTest::testLog10K
    public void testLog10K()
    {
        Assert.assertEquals("log10K #1", 1, field.newDfp("123456").log10K());
        Assert.assertEquals("log10K #2", 2, field.newDfp("123456789").log10K());
        Assert.assertEquals("log10K #3", 0, field.newDfp("2").log10K());
        Assert.assertEquals("log10K #3", 0, field.newDfp("1").log10K());
        Assert.assertEquals("log10K #4", -1, field.newDfp("0.1").log10K());
    }

// org.apache.commons.math3.dfp.DfpTest::testPower10K
    public void testPower10K()
    {
        Dfp d = field.newDfp();

        test(d.power10K(0), field.newDfp("1"), 0, "Power10 #1");
        test(d.power10K(1), field.newDfp("10000"), 0, "Power10 #2");
        test(d.power10K(2), field.newDfp("100000000"), 0, "Power10 #3");

        test(d.power10K(-1), field.newDfp("0.0001"), 0, "Power10 #4");
        test(d.power10K(-2), field.newDfp("0.00000001"), 0, "Power10 #5");
        test(d.power10K(-3), field.newDfp("0.000000000001"), 0, "Power10 #6");
    }

// org.apache.commons.math3.dfp.DfpTest::testLog10
    public void testLog10()
    {

        Assert.assertEquals("log10 #1", 1, field.newDfp("12").log10());
        Assert.assertEquals("log10 #2", 2, field.newDfp("123").log10());
        Assert.assertEquals("log10 #3", 3, field.newDfp("1234").log10());
        Assert.assertEquals("log10 #4", 4, field.newDfp("12345").log10());
        Assert.assertEquals("log10 #5", 5, field.newDfp("123456").log10());
        Assert.assertEquals("log10 #6", 6, field.newDfp("1234567").log10());
        Assert.assertEquals("log10 #6", 7, field.newDfp("12345678").log10());
        Assert.assertEquals("log10 #7", 8, field.newDfp("123456789").log10());
        Assert.assertEquals("log10 #8", 9, field.newDfp("1234567890").log10());
        Assert.assertEquals("log10 #9", 10, field.newDfp("12345678901").log10());
        Assert.assertEquals("log10 #10", 11, field.newDfp("123456789012").log10());
        Assert.assertEquals("log10 #11", 12, field.newDfp("1234567890123").log10());

        Assert.assertEquals("log10 #12", 0, field.newDfp("2").log10());
        Assert.assertEquals("log10 #13", 0, field.newDfp("1").log10());
        Assert.assertEquals("log10 #14", -1, field.newDfp("0.12").log10());
        Assert.assertEquals("log10 #15", -2, field.newDfp("0.012").log10());
    }

// org.apache.commons.math3.dfp.DfpTest::testPower10
    public void testPower10()
    {
        Dfp d = field.newDfp();

        test(d.power10(0), field.newDfp("1"), 0, "Power10 #1");
        test(d.power10(1), field.newDfp("10"), 0, "Power10 #2");
        test(d.power10(2), field.newDfp("100"), 0, "Power10 #3");
        test(d.power10(3), field.newDfp("1000"), 0, "Power10 #4");
        test(d.power10(4), field.newDfp("10000"), 0, "Power10 #5");
        test(d.power10(5), field.newDfp("100000"), 0, "Power10 #6");
        test(d.power10(6), field.newDfp("1000000"), 0, "Power10 #7");
        test(d.power10(7), field.newDfp("10000000"), 0, "Power10 #8");
        test(d.power10(8), field.newDfp("100000000"), 0, "Power10 #9");
        test(d.power10(9), field.newDfp("1000000000"), 0, "Power10 #10");

        test(d.power10(-1), field.newDfp(".1"), 0, "Power10 #11");
        test(d.power10(-2), field.newDfp(".01"), 0, "Power10 #12");
        test(d.power10(-3), field.newDfp(".001"), 0, "Power10 #13");
        test(d.power10(-4), field.newDfp(".0001"), 0, "Power10 #14");
        test(d.power10(-5), field.newDfp(".00001"), 0, "Power10 #15");
        test(d.power10(-6), field.newDfp(".000001"), 0, "Power10 #16");
        test(d.power10(-7), field.newDfp(".0000001"), 0, "Power10 #17");
        test(d.power10(-8), field.newDfp(".00000001"), 0, "Power10 #18");
        test(d.power10(-9), field.newDfp(".000000001"), 0, "Power10 #19");
        test(d.power10(-10), field.newDfp(".0000000001"), 0, "Power10 #20");
    }

// org.apache.commons.math3.dfp.DfpTest::testRemainder
    public void testRemainder()
    {
        test(field.newDfp("10").remainder(field.newDfp("3")),
             field.newDfp("1"),
             DfpField.FLAG_INEXACT, "Remainder #1");

        test(field.newDfp("9").remainder(field.newDfp("3")),
             field.newDfp("0"),
             0, "Remainder #2");

        test(field.newDfp("-9").remainder(field.newDfp("3")),
             field.newDfp("-0"),
             0, "Remainder #3");
    }

// org.apache.commons.math3.dfp.DfpTest::testSqrt
    public void testSqrt()
    {
        test(field.newDfp("0").sqrt(),
             field.newDfp("0"),
             0, "Sqrt #1");

        test(field.newDfp("-0").sqrt(),
             field.newDfp("-0"),
             0, "Sqrt #2");

        test(field.newDfp("1").sqrt(),
             field.newDfp("1"),
             0, "Sqrt #3");

        test(field.newDfp("2").sqrt(),
             field.newDfp("1.4142135623730950"),
             DfpField.FLAG_INEXACT, "Sqrt #4");

        test(field.newDfp("3").sqrt(),
             field.newDfp("1.7320508075688773"),
             DfpField.FLAG_INEXACT, "Sqrt #5");

        test(field.newDfp("5").sqrt(),
             field.newDfp("2.2360679774997897"),
             DfpField.FLAG_INEXACT, "Sqrt #6");

        test(field.newDfp("500").sqrt(),
             field.newDfp("22.3606797749978970"),
             DfpField.FLAG_INEXACT, "Sqrt #6.2");

        test(field.newDfp("50000").sqrt(),
             field.newDfp("223.6067977499789696"),
             DfpField.FLAG_INEXACT, "Sqrt #6.3");

        test(field.newDfp("-1").sqrt(),
             nan,
             DfpField.FLAG_INVALID, "Sqrt #7");

        test(pinf.sqrt(),
             pinf,
             0, "Sqrt #8");

        test(field.newDfp((byte) 1, Dfp.QNAN).sqrt(),
             nan,
             0, "Sqrt #9");

        test(field.newDfp((byte) 1, Dfp.SNAN).sqrt(),
             nan,
             DfpField.FLAG_INVALID, "Sqrt #9");
    }

// org.apache.commons.math3.dfp.DfpTest::testIssue567
    public void testIssue567() {
        DfpField field = new DfpField(100);
        Assert.assertEquals(0.0, field.getZero().toDouble(), Precision.SAFE_MIN);
        Assert.assertEquals(0.0, field.newDfp(0.0).toDouble(), Precision.SAFE_MIN);
        Assert.assertEquals(-1, FastMath.copySign(1, field.newDfp(-0.0).toDouble()), Precision.EPSILON);
        Assert.assertEquals(+1, FastMath.copySign(1, field.newDfp(+0.0).toDouble()), Precision.EPSILON);
    }

// org.apache.commons.math3.dfp.DfpTest::testIsZero
    public void testIsZero() {
        Assert.assertTrue(field.getZero().isZero());
        Assert.assertTrue(field.getZero().negate().isZero());
        Assert.assertTrue(field.newDfp(+0.0).isZero());
        Assert.assertTrue(field.newDfp(-0.0).isZero());
        Assert.assertFalse(field.newDfp(1.0e-90).isZero());
        Assert.assertFalse(nan.isZero());
        Assert.assertFalse(nan.negate().isZero());
        Assert.assertFalse(pinf.isZero());
        Assert.assertFalse(pinf.negate().isZero());
        Assert.assertFalse(ninf.isZero());
        Assert.assertFalse(ninf.negate().isZero());
    }

// org.apache.commons.math3.dfp.DfpTest::testSignPredicates
    public void testSignPredicates() {

        Assert.assertTrue(field.getZero().negativeOrNull());
        Assert.assertTrue(field.getZero().positiveOrNull());
        Assert.assertFalse(field.getZero().strictlyNegative());
        Assert.assertFalse(field.getZero().strictlyPositive());

        Assert.assertTrue(field.getZero().negate().negativeOrNull());
        Assert.assertTrue(field.getZero().negate().positiveOrNull());
        Assert.assertFalse(field.getZero().negate().strictlyNegative());
        Assert.assertFalse(field.getZero().negate().strictlyPositive());

        Assert.assertFalse(field.getOne().negativeOrNull());
        Assert.assertTrue(field.getOne().positiveOrNull());
        Assert.assertFalse(field.getOne().strictlyNegative());
        Assert.assertTrue(field.getOne().strictlyPositive());

        Assert.assertTrue(field.getOne().negate().negativeOrNull());
        Assert.assertFalse(field.getOne().negate().positiveOrNull());
        Assert.assertTrue(field.getOne().negate().strictlyNegative());
        Assert.assertFalse(field.getOne().negate().strictlyPositive());

        Assert.assertFalse(nan.negativeOrNull());
        Assert.assertFalse(nan.positiveOrNull());
        Assert.assertFalse(nan.strictlyNegative());
        Assert.assertFalse(nan.strictlyPositive());

        Assert.assertFalse(nan.negate().negativeOrNull());
        Assert.assertFalse(nan.negate().positiveOrNull());
        Assert.assertFalse(nan.negate().strictlyNegative());
        Assert.assertFalse(nan.negate().strictlyPositive());

        Assert.assertFalse(pinf.negativeOrNull());
        Assert.assertTrue(pinf.positiveOrNull());
        Assert.assertFalse(pinf.strictlyNegative());
        Assert.assertTrue(pinf.strictlyPositive());

        Assert.assertTrue(pinf.negate().negativeOrNull());
        Assert.assertFalse(pinf.negate().positiveOrNull());
        Assert.assertTrue(pinf.negate().strictlyNegative());
        Assert.assertFalse(pinf.negate().strictlyPositive());

        Assert.assertTrue(ninf.negativeOrNull());
        Assert.assertFalse(ninf.positiveOrNull());
        Assert.assertTrue(ninf.strictlyNegative());
        Assert.assertFalse(ninf.strictlyPositive());

        Assert.assertFalse(ninf.negate().negativeOrNull());
        Assert.assertTrue(ninf.negate().positiveOrNull());
        Assert.assertFalse(ninf.negate().strictlyNegative());
        Assert.assertTrue(ninf.negate().strictlyPositive());

    }

// org.apache.commons.math3.distribution.BetaDistributionTest::testCumulative
    public void testCumulative() {
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

// org.apache.commons.math3.distribution.BetaDistributionTest::testDensity
    public void testDensity() {
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

// org.apache.commons.math3.distribution.BetaDistributionTest::testMoments
    public void testMoments() {
        final double tol = 1e-9;
        BetaDistribution dist;

        dist = new BetaDistribution(1, 1);
        Assert.assertEquals(dist.getNumericalMean(), 0.5, tol);
        Assert.assertEquals(dist.getNumericalVariance(), 1.0 / 12.0, tol);

        dist = new BetaDistribution(2, 5);
        Assert.assertEquals(dist.getNumericalMean(), 2.0 / 7.0, tol);
        Assert.assertEquals(dist.getNumericalVariance(), 10.0 / (49.0 * 8.0), tol);
    }

// org.apache.commons.math3.distribution.BinomialDistributionTest::testDegenerate0
    public void testDegenerate0() {
        BinomialDistribution dist = new BinomialDistribution(5, 0.0d);
        setDistribution(dist);
        setCumulativeTestPoints(new int[] { -1, 0, 1, 5, 10 });
        setCumulativeTestValues(new double[] { 0d, 1d, 1d, 1d, 1d });
        setDensityTestPoints(new int[] { -1, 0, 1, 10, 11 });
        setDensityTestValues(new double[] { 0d, 1d, 0d, 0d, 0d });
        setInverseCumulativeTestPoints(new double[] { 0.1d, 0.5d });
        setInverseCumulativeTestValues(new int[] { 0, 0 });
        verifyDensities();
        verifyCumulativeProbabilities();
        verifyInverseCumulativeProbabilities();
        Assert.assertEquals(dist.getSupportLowerBound(), 0);
        Assert.assertEquals(dist.getSupportUpperBound(), 0);
    }

// org.apache.commons.math3.distribution.BinomialDistributionTest::testDegenerate1
    public void testDegenerate1() {
        BinomialDistribution dist = new BinomialDistribution(5, 1.0d);
        setDistribution(dist);
        setCumulativeTestPoints(new int[] { -1, 0, 1, 2, 5, 10 });
        setCumulativeTestValues(new double[] { 0d, 0d, 0d, 0d, 1d, 1d });
        setDensityTestPoints(new int[] { -1, 0, 1, 2, 5, 10 });
        setDensityTestValues(new double[] { 0d, 0d, 0d, 0d, 1d, 0d });
        setInverseCumulativeTestPoints(new double[] { 0.1d, 0.5d });
        setInverseCumulativeTestValues(new int[] { 5, 5 });
        verifyDensities();
        verifyCumulativeProbabilities();
        verifyInverseCumulativeProbabilities();
        Assert.assertEquals(dist.getSupportLowerBound(), 5);
        Assert.assertEquals(dist.getSupportUpperBound(), 5);
    }

// org.apache.commons.math3.distribution.BinomialDistributionTest::testMoments
    public void testMoments() {
        final double tol = 1e-9;
        BinomialDistribution dist;

        dist = new BinomialDistribution(10, 0.5);
        Assert.assertEquals(dist.getNumericalMean(), 10d * 0.5d, tol);
        Assert.assertEquals(dist.getNumericalVariance(), 10d * 0.5d * 0.5d, tol);

        dist = new BinomialDistribution(30, 0.3);
        Assert.assertEquals(dist.getNumericalMean(), 30d * 0.3d, tol);
        Assert.assertEquals(dist.getNumericalVariance(), 30d * 0.3d * (1d - 0.3d), tol);
    }

// org.apache.commons.math3.distribution.BinomialDistributionTest::testMath718
    public void testMath718() {
        
        
        

        for (int trials = 500000; trials < 20000000; trials += 100000) {
            BinomialDistribution dist = new BinomialDistribution(trials, 0.5);
            int p = dist.inverseCumulativeProbability(0.5);
            Assert.assertEquals(trials / 2, p);
        }
    }

// org.apache.commons.math3.distribution.CauchyDistributionTest::testInverseCumulativeProbabilityExtremes
    public void testInverseCumulativeProbabilityExtremes() {
        setInverseCumulativeTestPoints(new double[] {0.0, 1.0});
        setInverseCumulativeTestValues(
                new double[] {Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY});
        verifyInverseCumulativeProbabilities();
    }

// org.apache.commons.math3.distribution.CauchyDistributionTest::testMedian
    public void testMedian() {
        CauchyDistribution distribution = (CauchyDistribution) getDistribution();
        Assert.assertEquals(1.2, distribution.getMedian(), 0.0);
    }

// org.apache.commons.math3.distribution.CauchyDistributionTest::testScale
    public void testScale() {
        CauchyDistribution distribution = (CauchyDistribution) getDistribution();
        Assert.assertEquals(2.1, distribution.getScale(), 0.0);
    }

// org.apache.commons.math3.distribution.CauchyDistributionTest::testPreconditions
    public void testPreconditions() {
        try {
            new CauchyDistribution(0, 0);
            Assert.fail("Cannot have zero scale");
        } catch (NotStrictlyPositiveException ex) {
            
        }
        try {
            new CauchyDistribution(0, -1);
            Assert.fail("Cannot have negative scale");
        } catch (NotStrictlyPositiveException ex) {
            
        }
    }

// org.apache.commons.math3.distribution.CauchyDistributionTest::testMoments
    public void testMoments() {
        CauchyDistribution dist;

        dist = new CauchyDistribution(10.2, 0.15);
        Assert.assertTrue(Double.isNaN(dist.getNumericalMean()));
        Assert.assertTrue(Double.isNaN(dist.getNumericalVariance()));

        dist = new CauchyDistribution(23.12, 2.12);
        Assert.assertTrue(Double.isNaN(dist.getNumericalMean()));
        Assert.assertTrue(Double.isNaN(dist.getNumericalVariance()));
    }

// org.apache.commons.math3.distribution.CauchyDistributionTest::testSampling
    public void testSampling() {}

// org.apache.commons.math3.distribution.ChiSquaredDistributionTest::testSmallDf
    public void testSmallDf() {
        setDistribution(new ChiSquaredDistribution(0.1d));
        setTolerance(1E-4);
        
        setCumulativeTestPoints(new double[] {1.168926E-60, 1.168926E-40, 1.063132E-32,
                1.144775E-26, 1.168926E-20, 5.472917, 2.175255, 1.13438,
                0.5318646, 0.1526342});
        setInverseCumulativeTestValues(getCumulativeTestPoints());
        setInverseCumulativeTestPoints(getCumulativeTestValues());
        verifyCumulativeProbabilities();
        verifyInverseCumulativeProbabilities();
    }

// org.apache.commons.math3.distribution.ChiSquaredDistributionTest::testDfAccessors
    public void testDfAccessors() {
        ChiSquaredDistribution distribution = (ChiSquaredDistribution) getDistribution();
        Assert.assertEquals(5d, distribution.getDegreesOfFreedom(), Double.MIN_VALUE);
    }

// org.apache.commons.math3.distribution.ChiSquaredDistributionTest::testDensity
    public void testDensity() {
        double[] x = new double[]{-0.1, 1e-6, 0.5, 1, 2, 5};
        
        checkDensity(1, x, new double[]{0.00000000000, 398.94208093034, 0.43939128947, 0.24197072452, 0.10377687436, 0.01464498256});
        
        checkDensity(0.1, x, new double[]{0.000000000e+00, 2.486453997e+04, 7.464238732e-02, 3.009077718e-02, 9.447299159e-03, 8.827199396e-04});
        
        checkDensity(2, x, new double[]{0.00000000000, 0.49999975000, 0.38940039154, 0.30326532986, 0.18393972059, 0.04104249931});
        
        checkDensity(10, x, new double[]{0.000000000e+00, 1.302082682e-27, 6.337896998e-05, 7.897534632e-04, 7.664155024e-03, 6.680094289e-02});
    }

// org.apache.commons.math3.distribution.ChiSquaredDistributionTest::testMoments
    public void testMoments() {
        final double tol = 1e-9;
        ChiSquaredDistribution dist;

        dist = new ChiSquaredDistribution(1500);
        Assert.assertEquals(dist.getNumericalMean(), 1500, tol);
        Assert.assertEquals(dist.getNumericalVariance(), 3000, tol);

        dist = new ChiSquaredDistribution(1.12);
        Assert.assertEquals(dist.getNumericalMean(), 1.12, tol);
        Assert.assertEquals(dist.getNumericalVariance(), 2.24, tol);
    }

// org.apache.commons.math3.distribution.ExponentialDistributionTest::testCumulativeProbabilityExtremes
    public void testCumulativeProbabilityExtremes() {
        setCumulativeTestPoints(new double[] {-2, 0});
        setCumulativeTestValues(new double[] {0, 0});
        verifyCumulativeProbabilities();
    }

// org.apache.commons.math3.distribution.ExponentialDistributionTest::testInverseCumulativeProbabilityExtremes
    public void testInverseCumulativeProbabilityExtremes() {
         setInverseCumulativeTestPoints(new double[] {0, 1});
         setInverseCumulativeTestValues(new double[] {0, Double.POSITIVE_INFINITY});
         verifyInverseCumulativeProbabilities();
    }

// org.apache.commons.math3.distribution.ExponentialDistributionTest::testCumulativeProbability2
    public void testCumulativeProbability2() {
        double actual = getDistribution().cumulativeProbability(0.25, 0.75);
        Assert.assertEquals(0.0905214, actual, 10e-4);
    }

// org.apache.commons.math3.distribution.ExponentialDistributionTest::testDensity
    public void testDensity() {
        ExponentialDistribution d1 = new ExponentialDistribution(1);
        Assert.assertTrue(Precision.equals(0.0, d1.density(-1e-9), 1));
        Assert.assertTrue(Precision.equals(1.0, d1.density(0.0), 1));
        Assert.assertTrue(Precision.equals(0.0, d1.density(1000.0), 1));
        Assert.assertTrue(Precision.equals(FastMath.exp(-1), d1.density(1.0), 1));
        Assert.assertTrue(Precision.equals(FastMath.exp(-2), d1.density(2.0), 1));

        ExponentialDistribution d2 = new ExponentialDistribution(3);
        Assert.assertTrue(Precision.equals(1/3.0, d2.density(0.0), 1));
        
        Assert.assertEquals(0.2388437702, d2.density(1.0), 1e-8);

        
        Assert.assertEquals(0.1711390397, d2.density(2.0), 1e-8);
    }

// org.apache.commons.math3.distribution.ExponentialDistributionTest::testMeanAccessors
    public void testMeanAccessors() {
        ExponentialDistribution distribution = (ExponentialDistribution) getDistribution();
        Assert.assertEquals(5d, distribution.getMean(), Double.MIN_VALUE);
    }

// org.apache.commons.math3.distribution.ExponentialDistributionTest::testPreconditions
    public void testPreconditions() {
        new ExponentialDistribution(0);
    }

// org.apache.commons.math3.distribution.ExponentialDistributionTest::testMoments
    public void testMoments() {
        final double tol = 1e-9;
        ExponentialDistribution dist;

        dist = new ExponentialDistribution(11d);
        Assert.assertEquals(dist.getNumericalMean(), 11d, tol);
        Assert.assertEquals(dist.getNumericalVariance(), 11d * 11d, tol);

        dist = new ExponentialDistribution(10.5d);
        Assert.assertEquals(dist.getNumericalMean(), 10.5d, tol);
        Assert.assertEquals(dist.getNumericalVariance(), 10.5d * 10.5d, tol);
    }

// org.apache.commons.math3.distribution.FDistributionTest::testCumulativeProbabilityExtremes
    public void testCumulativeProbabilityExtremes() {
        setCumulativeTestPoints(new double[] {-2, 0});
        setCumulativeTestValues(new double[] {0, 0});
        verifyCumulativeProbabilities();
    }

// org.apache.commons.math3.distribution.FDistributionTest::testInverseCumulativeProbabilityExtremes
    public void testInverseCumulativeProbabilityExtremes() {
        setInverseCumulativeTestPoints(new double[] {0, 1});
        setInverseCumulativeTestValues(new double[] {0, Double.POSITIVE_INFINITY});
        verifyInverseCumulativeProbabilities();
    }

// org.apache.commons.math3.distribution.FDistributionTest::testDfAccessors
    public void testDfAccessors() {
        FDistribution dist = (FDistribution) getDistribution();
        Assert.assertEquals(5d, dist.getNumeratorDegreesOfFreedom(), Double.MIN_VALUE);
        Assert.assertEquals(6d, dist.getDenominatorDegreesOfFreedom(), Double.MIN_VALUE);
    }

// org.apache.commons.math3.distribution.FDistributionTest::testPreconditions
    public void testPreconditions() {
        try {
            new FDistribution(0, 1);
            Assert.fail("Expecting NotStrictlyPositiveException for df = 0");
        } catch (NotStrictlyPositiveException ex) {
            
        }
        try {
            new FDistribution(1, 0);
            Assert.fail("Expecting NotStrictlyPositiveException for df = 0");
        } catch (NotStrictlyPositiveException ex) {
            
        }
    }

// org.apache.commons.math3.distribution.FDistributionTest::testLargeDegreesOfFreedom
    public void testLargeDegreesOfFreedom() {
        FDistribution fd = new FDistribution(100000, 100000);
        double p = fd.cumulativeProbability(.999);
        double x = fd.inverseCumulativeProbability(p);
        Assert.assertEquals(.999, x, 1.0e-5);
    }

// org.apache.commons.math3.distribution.FDistributionTest::testSmallDegreesOfFreedom
    public void testSmallDegreesOfFreedom() {
        FDistribution fd = new FDistribution(1, 1);
        double p = fd.cumulativeProbability(0.975);
        double x = fd.inverseCumulativeProbability(p);
        Assert.assertEquals(0.975, x, 1.0e-5);

        fd = new FDistribution(1, 2);
        p = fd.cumulativeProbability(0.975);
        x = fd.inverseCumulativeProbability(p);
        Assert.assertEquals(0.975, x, 1.0e-5);
    }

// org.apache.commons.math3.distribution.FDistributionTest::testMoments
    public void testMoments() {
        final double tol = 1e-9;
        FDistribution dist;

        dist = new FDistribution(1, 2);
        Assert.assertTrue(Double.isNaN(dist.getNumericalMean()));
        Assert.assertTrue(Double.isNaN(dist.getNumericalVariance()));

        dist = new FDistribution(1, 3);
        Assert.assertEquals(dist.getNumericalMean(), 3d / (3d - 2d), tol);
        Assert.assertTrue(Double.isNaN(dist.getNumericalVariance()));

        dist = new FDistribution(1, 5);
        Assert.assertEquals(dist.getNumericalMean(), 5d / (5d - 2d), tol);
        Assert.assertEquals(dist.getNumericalVariance(), (2d * 5d * 5d * 4d) / 9d, tol);
    }

// org.apache.commons.math3.distribution.FDistributionTest::testMath785
    public void testMath785() {
        

        try {
            double prob = 0.01;
            FDistribution f = new FDistribution(200000, 200000);
            double result = f.inverseCumulativeProbability(prob);
            Assert.assertTrue(result < 1.0);
        } catch (Exception e) {
            Assert.fail("Failing to calculate inverse cumulative probability");
        }
    }

// org.apache.commons.math3.distribution.GammaDistributionTest::testParameterAccessors
    public void testParameterAccessors() {
        GammaDistribution distribution = (GammaDistribution) getDistribution();
        Assert.assertEquals(4d, distribution.getAlpha(), 0);
        Assert.assertEquals(2d, distribution.getBeta(), 0);
    }

// org.apache.commons.math3.distribution.GammaDistributionTest::testPreconditions
    public void testPreconditions() {
        try {
            new GammaDistribution(0, 1);
            Assert.fail("Expecting NotStrictlyPositiveException for alpha = 0");
        } catch (NotStrictlyPositiveException ex) {
            
        }
        try {
            new GammaDistribution(1, 0);
            Assert.fail("Expecting NotStrictlyPositiveException for alpha = 0");
        } catch (NotStrictlyPositiveException ex) {
            
        }
    }

// org.apache.commons.math3.distribution.GammaDistributionTest::testProbabilities
    public void testProbabilities() {
        testProbability(-1.000, 4.0, 2.0, .0000);
        testProbability(15.501, 4.0, 2.0, .9499);
        testProbability(0.504, 4.0, 1.0, .0018);
        testProbability(10.011, 1.0, 2.0, .9933);
        testProbability(5.000, 2.0, 2.0, .7127);
    }

// org.apache.commons.math3.distribution.GammaDistributionTest::testValues
    public void testValues() {
        testValue(15.501, 4.0, 2.0, .9499);
        testValue(0.504, 4.0, 1.0, .0018);
        testValue(10.011, 1.0, 2.0, .9933);
        testValue(5.000, 2.0, 2.0, .7127);
    }

// org.apache.commons.math3.distribution.GammaDistributionTest::testDensity
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

// org.apache.commons.math3.distribution.GammaDistributionTest::testInverseCumulativeProbabilityExtremes
    public void testInverseCumulativeProbabilityExtremes() {
        setInverseCumulativeTestPoints(new double[] {0, 1});
        setInverseCumulativeTestValues(new double[] {0, Double.POSITIVE_INFINITY});
        verifyInverseCumulativeProbabilities();
    }

// org.apache.commons.math3.distribution.GammaDistributionTest::testMoments
    public void testMoments() {
        final double tol = 1e-9;
        GammaDistribution dist;

        dist = new GammaDistribution(1, 2);
        Assert.assertEquals(dist.getNumericalMean(), 2, tol);
        Assert.assertEquals(dist.getNumericalVariance(), 4, tol);

        dist = new GammaDistribution(1.1, 4.2);
        Assert.assertEquals(dist.getNumericalMean(), 1.1d * 4.2d, tol);
        Assert.assertEquals(dist.getNumericalVariance(), 1.1d * 4.2d * 4.2d, tol);
    }

// org.apache.commons.math3.distribution.GammaDistributionTest::testMath753Shape1
    public void testMath753Shape1() throws IOException {
        doTestMath753(1.0, 1.5, 0.5, 0.0, 0.0, "gamma-distribution-shape-1.csv");
    }

// org.apache.commons.math3.distribution.GammaDistributionTest::testMath753Shape8
    public void testMath753Shape8() throws IOException {
        doTestMath753(8.0, 1.5, 1.0, 0.0, 0.0, "gamma-distribution-shape-8.csv");
    }

// org.apache.commons.math3.distribution.GammaDistributionTest::testMath753Shape10
    public void testMath753Shape10() throws IOException {
        doTestMath753(10.0, 1.0, 1.0, 0.0, 0.0, "gamma-distribution-shape-10.csv");
    }

// org.apache.commons.math3.distribution.GammaDistributionTest::testMath753Shape100
    public void testMath753Shape100() throws IOException {
        doTestMath753(100.0, 1.5, 1.0, 0.0, 0.0, "gamma-distribution-shape-100.csv");
    }

// org.apache.commons.math3.distribution.GammaDistributionTest::testMath753Shape142
    public void testMath753Shape142() throws IOException {
        doTestMath753(142.0, 0.5, 1.5, 40.0, 40.0, "gamma-distribution-shape-142.csv");
    }

// org.apache.commons.math3.distribution.GammaDistributionTest::testMath753Shape1000
    public void testMath753Shape1000() throws IOException {
        doTestMath753(1000.0, 1.0, 1.0, 160.0, 220.0, "gamma-distribution-shape-1000.csv");
    }

// org.apache.commons.math3.distribution.HypergeometricDistributionTest::testDegenerateNoFailures
    public void testDegenerateNoFailures() {
        HypergeometricDistribution dist = new HypergeometricDistribution(5,5,3);
        setDistribution(dist);
        setCumulativeTestPoints(new int[] {-1, 0, 1, 3, 10 });
        setCumulativeTestValues(new double[] {0d, 0d, 0d, 1d, 1d});
        setDensityTestPoints(new int[] {-1, 0, 1, 3, 10});
        setDensityTestValues(new double[] {0d, 0d, 0d, 1d, 0d});
        setInverseCumulativeTestPoints(new double[] {0.1d, 0.5d});
        setInverseCumulativeTestValues(new int[] {3, 3});
        verifyDensities();
        verifyCumulativeProbabilities();
        verifyInverseCumulativeProbabilities();
        Assert.assertEquals(dist.getSupportLowerBound(), 3);
        Assert.assertEquals(dist.getSupportUpperBound(), 3);
    }

// org.apache.commons.math3.distribution.HypergeometricDistributionTest::testDegenerateNoSuccesses
    public void testDegenerateNoSuccesses() {
        HypergeometricDistribution dist = new HypergeometricDistribution(5,0,3);
        setDistribution(dist);
        setCumulativeTestPoints(new int[] {-1, 0, 1, 3, 10 });
        setCumulativeTestValues(new double[] {0d, 1d, 1d, 1d, 1d});
        setDensityTestPoints(new int[] {-1, 0, 1, 3, 10});
        setDensityTestValues(new double[] {0d, 1d, 0d, 0d, 0d});
        setInverseCumulativeTestPoints(new double[] {0.1d, 0.5d});
        setInverseCumulativeTestValues(new int[] {0, 0});
        verifyDensities();
        verifyCumulativeProbabilities();
        verifyInverseCumulativeProbabilities();
        Assert.assertEquals(dist.getSupportLowerBound(), 0);
        Assert.assertEquals(dist.getSupportUpperBound(), 0);
    }

// org.apache.commons.math3.distribution.HypergeometricDistributionTest::testDegenerateFullSample
    public void testDegenerateFullSample() {
        HypergeometricDistribution dist = new HypergeometricDistribution(5,3,5);
        setDistribution(dist);
        setCumulativeTestPoints(new int[] {-1, 0, 1, 3, 10 });
        setCumulativeTestValues(new double[] {0d, 0d, 0d, 1d, 1d});
        setDensityTestPoints(new int[] {-1, 0, 1, 3, 10});
        setDensityTestValues(new double[] {0d, 0d, 0d, 1d, 0d});
        setInverseCumulativeTestPoints(new double[] {0.1d, 0.5d});
        setInverseCumulativeTestValues(new int[] {3, 3});
        verifyDensities();
        verifyCumulativeProbabilities();
        verifyInverseCumulativeProbabilities();
        Assert.assertEquals(dist.getSupportLowerBound(), 3);
        Assert.assertEquals(dist.getSupportUpperBound(), 3);
    }

// org.apache.commons.math3.distribution.HypergeometricDistributionTest::testPreconditions
    public void testPreconditions() {
        try {
            new HypergeometricDistribution(0, 3, 5);
            Assert.fail("negative population size. NotStrictlyPositiveException expected");
        } catch(NotStrictlyPositiveException ex) {
            
        }
        try {
            new HypergeometricDistribution(5, -1, 5);
            Assert.fail("negative number of successes. NotPositiveException expected");
        } catch(NotPositiveException ex) {
            
        }
        try {
            new HypergeometricDistribution(5, 3, -1);
            Assert.fail("negative sample size. NotPositiveException expected");
        } catch(NotPositiveException ex) {
            
        }
        try {
            new HypergeometricDistribution(5, 6, 5);
            Assert.fail("numberOfSuccesses > populationSize. NumberIsTooLargeException expected");
        } catch(NumberIsTooLargeException ex) {
            
        }
        try {
            new HypergeometricDistribution(5, 3, 6);
            Assert.fail("sampleSize > populationSize. NumberIsTooLargeException expected");
        } catch(NumberIsTooLargeException ex) {
            
        }
    }

// org.apache.commons.math3.distribution.HypergeometricDistributionTest::testAccessors
    public void testAccessors() {
        HypergeometricDistribution dist = new HypergeometricDistribution(5, 3, 4);
        Assert.assertEquals(5, dist.getPopulationSize());
        Assert.assertEquals(3, dist.getNumberOfSuccesses());
        Assert.assertEquals(4, dist.getSampleSize());
    }

// org.apache.commons.math3.distribution.HypergeometricDistributionTest::testLargeValues
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

// org.apache.commons.math3.distribution.HypergeometricDistributionTest::testMoreLargeValues
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

// org.apache.commons.math3.distribution.HypergeometricDistributionTest::testMoments
    public void testMoments() {
        final double tol = 1e-9;
        HypergeometricDistribution dist;

        dist = new HypergeometricDistribution(1500, 40, 100);
        Assert.assertEquals(dist.getNumericalMean(), 40d * 100d / 1500d, tol);
        Assert.assertEquals(dist.getNumericalVariance(), ( 100d * 40d * (1500d - 100d) * (1500d - 40d) ) / ( (1500d * 1500d * 1499d) ), tol);

        dist = new HypergeometricDistribution(3000, 55, 200);
        Assert.assertEquals(dist.getNumericalMean(), 55d * 200d / 3000d, tol);
        Assert.assertEquals(dist.getNumericalVariance(), ( 200d * 55d * (3000d - 200d) * (3000d - 55d) ) / ( (3000d * 3000d * 2999d) ), tol);
    }

// org.apache.commons.math3.distribution.HypergeometricDistributionTest::testMath644
    public void testMath644() {
        int N = 14761461;  
        int m = 1035;      
        int n = 1841;      

        int k = 0;
        final HypergeometricDistribution dist = new HypergeometricDistribution(N, m, n);
        
        Assert.assertTrue(Precision.compareTo(1.0, dist.upperCumulativeProbability(k), 1) == 0);
        Assert.assertTrue(Precision.compareTo(dist.cumulativeProbability(k), 0.0, 1) > 0);
        
        
        double upper = 1.0 - dist.cumulativeProbability(k) + dist.probability(k);
        Assert.assertTrue(Precision.compareTo(1.0, upper, 1) == 0);
    }

// org.apache.commons.math3.distribution.KolmogorovSmirnovDistributionTest::testCumulativeDensityFunction
    public void testCumulativeDensityFunction() {
        
        KolmogorovSmirnovDistribution dist;
        
        
        
        

        
        dist = new KolmogorovSmirnovDistribution(200);
        Assert.assertEquals(4.907829957616471622388047046469198862537e-86, dist.cdf(0.005, false), TOLERANCE);

        
        dist = new KolmogorovSmirnovDistribution(200);
        Assert.assertEquals(5.151982014280041957199687829849210629618e-06, dist.cdf(0.02, false), TOLERANCE);

        
        dist = new KolmogorovSmirnovDistribution(200);
        Assert.assertEquals(0.01291614648162886340443389343590752105229, dist.cdf(0.031111, false), TOLERANCE);

        
        dist = new KolmogorovSmirnovDistribution(200);
        Assert.assertEquals(0.1067137011362679355208626930107129737735, dist.cdf(0.04, false), TOLERANCE);

        
        dist = new KolmogorovSmirnovDistribution(341);
        Assert.assertEquals(1.914734701559404553985102395145063418825e-53, dist.cdf(0.005, false), TOLERANCE);

        
        dist = new KolmogorovSmirnovDistribution(341);
        Assert.assertEquals(0.001171328985781981343872182321774744195864, dist.cdf(0.02, false), TOLERANCE);

        
        dist = new KolmogorovSmirnovDistribution(341);
        Assert.assertEquals(0.1142955196267499418105728636874118819833, dist.cdf(0.031111, false), TOLERANCE);

        
        dist = new KolmogorovSmirnovDistribution(341);
        Assert.assertEquals(0.3685529520496805266915885113121476024389, dist.cdf(0.04, false), TOLERANCE);

        
        dist = new KolmogorovSmirnovDistribution(389);
        Assert.assertEquals(1.810657144595055888918455512707637574637e-47, dist.cdf(0.005, false), TOLERANCE);

        
        dist = new KolmogorovSmirnovDistribution(389);
        Assert.assertEquals(0.003068542559702356568168690742481885536108, dist.cdf(0.02, false), TOLERANCE);

        
        dist = new KolmogorovSmirnovDistribution(389);
        Assert.assertEquals(0.1658291700122746237244797384846606291831, dist.cdf(0.031111, false), TOLERANCE);

        
        dist = new KolmogorovSmirnovDistribution(389);
        Assert.assertEquals(0.4513143712128902529379104180407011881471, dist.cdf(0.04, false), TOLERANCE);

    }

// org.apache.commons.math3.distribution.LogNormalDistributionTest::testQuantiles
    public void testQuantiles() {
        setCumulativeTestValues(new double[] {0, 0.0396495152787,
                                              0.16601209243, 0.272533253269,
                                              0.357618409638, 0.426488363093,
                                              0.483255136841, 0.530823013877});
        setDensityTestValues(new double[] {0, 0.0873055825147, 0.0847676303432,
                                           0.0677935186237, 0.0544105523058,
                                           0.0444614628804, 0.0369750288945,
                                           0.0312206409653});
        verifyQuantiles();
        verifyDensities();

        setDistribution(new LogNormalDistribution(0, 1));
        setCumulativeTestValues(new double[] {0, 0, 0, 0.5, 0.755891404214,
                                              0.864031392359, 0.917171480998,
                                              0.946239689548});
        setDensityTestValues(new double[] {0, 0, 0, 0.398942280401,
                                           0.156874019279, 0.07272825614,
                                           0.0381534565119, 0.0218507148303});
        verifyQuantiles();
        verifyDensities();

        setDistribution(new LogNormalDistribution(0, 0.1));
        setCumulativeTestValues(new double[] {0, 0, 0, 1.28417563064e-117,
                                              1.39679883412e-58,
                                              1.09839325447e-33,
                                              2.52587961726e-20,
                                              2.0824223487e-12});
        setDensityTestValues(new double[] {0, 0, 0, 2.96247992535e-114,
                                           1.1283370232e-55, 4.43812313223e-31,
                                           5.85346445002e-18,
                                           2.9446618076e-10});
        verifyQuantiles();
        verifyDensities();
    }

// org.apache.commons.math3.distribution.LogNormalDistributionTest::testInverseCumulativeProbabilityExtremes
    public void testInverseCumulativeProbabilityExtremes() {
        setInverseCumulativeTestPoints(new double[] {0, 1});
        setInverseCumulativeTestValues(
                new double[] {0, Double.POSITIVE_INFINITY});
        verifyInverseCumulativeProbabilities();
    }

// org.apache.commons.math3.distribution.LogNormalDistributionTest::testGetScale
    public void testGetScale() {
        LogNormalDistribution distribution = (LogNormalDistribution)getDistribution();
        Assert.assertEquals(2.1, distribution.getScale(), 0);
    }

// org.apache.commons.math3.distribution.LogNormalDistributionTest::testGetShape
    public void testGetShape() {
        LogNormalDistribution distribution = (LogNormalDistribution)getDistribution();
        Assert.assertEquals(1.4, distribution.getShape(), 0);
    }

// org.apache.commons.math3.distribution.LogNormalDistributionTest::testPreconditions
    public void testPreconditions() {
        new LogNormalDistribution(1, 0);
    }

// org.apache.commons.math3.distribution.LogNormalDistributionTest::testDensity
    public void testDensity() {
        double [] x = new double[]{-2, -1, 0, 1, 2};
        
        checkDensity(0, 1, x, new double[] { 0.0000000000, 0.0000000000,
                                             0.0000000000, 0.3989422804,
                                             0.1568740193 });
        
        checkDensity(1.1, 1, x, new double[] { 0.0000000000, 0.0000000000,
                                               0.0000000000, 0.2178521770,
                                               0.1836267118});
    }

// org.apache.commons.math3.distribution.LogNormalDistributionTest::testExtremeValues
    public void testExtremeValues() {
        LogNormalDistribution d = new LogNormalDistribution(0, 1);
        for (int i = 0; i < 1e5; i++) { 
            double upperTail = d.cumulativeProbability(i);
            if (i <= 72) { 
                Assert.assertTrue(upperTail < 1.0d);
            }
            else { 
                Assert.assertTrue(upperTail > 0.99999);
            }
        }

        Assert.assertEquals(d.cumulativeProbability(Double.MAX_VALUE), 1, 0);
        Assert.assertEquals(d.cumulativeProbability(-Double.MAX_VALUE), 0, 0);
        Assert.assertEquals(d.cumulativeProbability(Double.POSITIVE_INFINITY), 1, 0);
        Assert.assertEquals(d.cumulativeProbability(Double.NEGATIVE_INFINITY), 0, 0);
    }

// org.apache.commons.math3.distribution.LogNormalDistributionTest::testMeanVariance
    public void testMeanVariance() {
        final double tol = 1e-9;
        LogNormalDistribution dist;

        dist = new LogNormalDistribution(0, 1);
        Assert.assertEquals(dist.getNumericalMean(), 1.6487212707001282, tol);
        Assert.assertEquals(dist.getNumericalVariance(),
                            4.670774270471604, tol);

        dist = new LogNormalDistribution(2.2, 1.4);
        Assert.assertEquals(dist.getNumericalMean(), 24.046753552064498, tol);
        Assert.assertEquals(dist.getNumericalVariance(),
                            3526.913651880464, tol);

        dist = new LogNormalDistribution(-2000.9, 10.4);
        Assert.assertEquals(dist.getNumericalMean(), 0.0, tol);
        Assert.assertEquals(dist.getNumericalVariance(), 0.0, tol);
    }

// org.apache.commons.math3.distribution.MultivariateNormalDistributionTest::testGetMean
    public void testGetMean() {
        final double[] mu = { -1.5, 2 };
        final double[][] sigma = { { 2, -1.1 },
                                   { -1.1, 2 } };
        final MultivariateNormalDistribution d = new MultivariateNormalDistribution(mu, sigma);

        final double[] m = d.getMeans();
        for (int i = 0; i < m.length; i++) {
            Assert.assertEquals(mu[i], m[i], 0);
        }
    }

// org.apache.commons.math3.distribution.MultivariateNormalDistributionTest::testGetCovarianceMatrix
    public void testGetCovarianceMatrix() {
        final double[] mu = { -1.5, 2 };
        final double[][] sigma = { { 2, -1.1 },
                                   { -1.1, 2 } };
        final MultivariateNormalDistribution d = new MultivariateNormalDistribution(mu, sigma);

        final RealMatrix s = d.getCovariances();
        final int dim = d.getDimension();
        for (int i = 0; i < dim; i++) {
            for (int j = 0; j < dim; j++) {
                Assert.assertEquals(sigma[i][j], s.getEntry(i, j), 0);
            }
        }
    }

// org.apache.commons.math3.distribution.MultivariateNormalDistributionTest::testSampling
    public void testSampling() {
        final double[] mu = { -1.5, 2 };
        final double[][] sigma = { { 2, -1.1 },
                                   { -1.1, 2 } };
        final MultivariateNormalDistribution d = new MultivariateNormalDistribution(mu, sigma);
        d.reseedRandomGenerator(50);

        final int n = 500000;

        final double[][] samples = d.sample(n);
        final int dim = d.getDimension();
        final double[] sampleMeans = new double[dim];

        for (int i = 0; i < samples.length; i++) {
            for (int j = 0; j < dim; j++) {
                sampleMeans[j] += samples[i][j];
            }
        }

        final double sampledValueTolerance = 1e-2;
        for (int j = 0; j < dim; j++) {
            sampleMeans[j] /= samples.length;
            Assert.assertEquals(mu[j], sampleMeans[j], sampledValueTolerance);
        }

        final double[][] sampleSigma = new Covariance(samples).getCovarianceMatrix().getData();
        for (int i = 0; i < dim; i++) {
            for (int j = 0; j < dim; j++) {
                Assert.assertEquals(sigma[i][j], sampleSigma[i][j], sampledValueTolerance);
            }
        }
    }

// org.apache.commons.math3.distribution.MultivariateNormalDistributionTest::testDensities
    public void testDensities() {
        final double[] mu = { -1.5, 2 };
        final double[][] sigma = { { 2, -1.1 },
                                   { -1.1, 2 } };
        final MultivariateNormalDistribution d = new MultivariateNormalDistribution(mu, sigma);

        final double[][] testValues = { { -1.5, 2 },
                                        { 4, 4 },
                                        { 1.5, -2 },
                                        { 0, 0 } };
        final double[] densities = new double[testValues.length];
        for (int i = 0; i < densities.length; i++) {
            densities[i] = d.density(testValues[i]);
        }

        
        final double[] correctDensities = { 0.09528357207691344,
                                            5.80932710124009e-09,
                                            0.001387448895173267,
                                            0.03309922090210541 };

        for (int i = 0; i < testValues.length; i++) {
            Assert.assertEquals(correctDensities[i], densities[i], 1e-16);
        }
    }

// org.apache.commons.math3.distribution.MultivariateNormalMixtureModelDistributionTest::testNonUnitWeightSum
    public void testNonUnitWeightSum() {
        final double[] weights = { 1, 2 };
        final double[][] means = { { -1.5, 2.0 },
                                   { 4.0, 8.2 } };
        final double[][][] covariances = { { { 2.0, -1.1 },
                                             { -1.1, 2.0 } },
                                           { { 3.5, 1.5 },
                                             { 1.5, 3.5 } } };
        final MultivariateNormalMixtureModelDistribution d
            = create(weights, means, covariances);

        final List<Pair<Double, MultivariateNormalDistribution>> comp = d.getComponents();

        Assert.assertEquals(1d / 3, comp.get(0).getFirst(), Math.ulp(1d));
        Assert.assertEquals(2d / 3, comp.get(1).getFirst(), Math.ulp(1d));
    }

// org.apache.commons.math3.distribution.MultivariateNormalMixtureModelDistributionTest::testWeightSumOverFlow
    public void testWeightSumOverFlow() {
        final double[] weights = { 0.5 * Double.MAX_VALUE, 0.51 * Double.MAX_VALUE };
        final double[][] means = { { -1.5, 2.0 },
                                   { 4.0, 8.2 } };
        final double[][][] covariances = { { { 2.0, -1.1 },
                                             { -1.1, 2.0 } },
                                           { { 3.5, 1.5 },
                                             { 1.5, 3.5 } } };
        final MultivariateNormalMixtureModelDistribution d
            = create(weights, means, covariances);
    }

// org.apache.commons.math3.distribution.MultivariateNormalMixtureModelDistributionTest::testPreconditionPositiveWeights
    public void testPreconditionPositiveWeights() {
        final double[] negativeWeights = { -0.5, 1.5 };
        final double[][] means = { { -1.5, 2.0 },
                                   { 4.0, 8.2 } };
        final double[][][] covariances = { { { 2.0, -1.1 },
                                             { -1.1, 2.0 } },
                                           { { 3.5, 1.5 },
                                             { 1.5, 3.5 } } };
        final MultivariateNormalMixtureModelDistribution d
            = create(negativeWeights, means, covariances);
    }

// org.apache.commons.math3.distribution.MultivariateNormalMixtureModelDistributionTest::testDensities
    public void testDensities() {
        final double[] weights = { 0.3, 0.7 };
        final double[][] means = { { -1.5, 2.0 },
                                   { 4.0, 8.2 } };
        final double[][][] covariances = { { { 2.0, -1.1 },
                                             { -1.1, 2.0 } },
                                           { { 3.5, 1.5 },
                                             { 1.5, 3.5 } } };
        final MultivariateNormalMixtureModelDistribution d
            = create(weights, means, covariances);

        
        final double[][] testValues = { { -1.5, 2 },
                                        { 4, 8.2 },
                                        { 1.5, -2 },
                                        { 0, 0 } };

        
        
        
        
        
        final double[] correctDensities = { 0.02862037278930575,
                                            0.03523044847314091,
                                            0.000416241365629767,
                                            0.009932042831700297 };

        for (int i = 0; i < testValues.length; i++) {
            Assert.assertEquals(correctDensities[i], d.density(testValues[i]), Math.ulp(1d));
        }
    }

// org.apache.commons.math3.distribution.MultivariateNormalMixtureModelDistributionTest::testSampling
    public void testSampling() {
        final double[] weights = { 0.3, 0.7 };
        final double[][] means = { { -1.5, 2.0 },
                                   { 4.0, 8.2 } };
        final double[][][] covariances = { { { 2.0, -1.1 },
                                             { -1.1, 2.0 } },
                                           { { 3.5, 1.5 },
                                             { 1.5, 3.5 } } };
        final MultivariateNormalMixtureModelDistribution d
            = create(weights, means, covariances);
        d.reseedRandomGenerator(50);

        final double[][] correctSamples = getCorrectSamples();
        final int n = correctSamples.length;
        final double[][] samples = d.sample(n);

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < samples[i].length; j++) {
                Assert.assertEquals(correctSamples[i][j], samples[i][j], 1e-16);
            }
        }
    }

// org.apache.commons.math3.distribution.NormalDistributionTest::testQuantiles
    public void testQuantiles() {
        setDensityTestValues(new double[] {0.0385649760808, 0.172836231799, 0.284958771715, 0.172836231799, 0.0385649760808,
                0.00316560600853, 9.55930184035e-05, 1.06194251052e-06});
        verifyQuantiles();
        verifyDensities();

        setDistribution(new NormalDistribution(0, 1));
        setDensityTestValues(new double[] {0.0539909665132, 0.241970724519, 0.398942280401, 0.241970724519, 0.0539909665132,
                0.00443184841194, 0.000133830225765, 1.48671951473e-06});
        verifyQuantiles();
        verifyDensities();

        setDistribution(new NormalDistribution(0, 0.1));
        setDensityTestValues(new double[] {0.539909665132, 2.41970724519, 3.98942280401, 2.41970724519,
                0.539909665132, 0.0443184841194, 0.00133830225765, 1.48671951473e-05});
        verifyQuantiles();
        verifyDensities();
    }

// org.apache.commons.math3.distribution.NormalDistributionTest::testInverseCumulativeProbabilityExtremes
    public void testInverseCumulativeProbabilityExtremes() {
        setInverseCumulativeTestPoints(new double[] {0, 1});
        setInverseCumulativeTestValues(
                new double[] {Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY});
        verifyInverseCumulativeProbabilities();
    }

// org.apache.commons.math3.distribution.NormalDistributionTest::testGetMean
    public void testGetMean() {
        NormalDistribution distribution = (NormalDistribution) getDistribution();
        Assert.assertEquals(2.1, distribution.getMean(), 0);
    }

// org.apache.commons.math3.distribution.NormalDistributionTest::testGetStandardDeviation
    public void testGetStandardDeviation() {
        NormalDistribution distribution = (NormalDistribution) getDistribution();
        Assert.assertEquals(1.4, distribution.getStandardDeviation(), 0);
    }

// org.apache.commons.math3.distribution.NormalDistributionTest::testPreconditions
    public void testPreconditions() {
        new NormalDistribution(1, 0);
    }

// org.apache.commons.math3.distribution.NormalDistributionTest::testDensity
    public void testDensity() {
        double [] x = new double[]{-2, -1, 0, 1, 2};
        
        checkDensity(0, 1, x, new double[]{0.05399096651, 0.24197072452, 0.39894228040, 0.24197072452, 0.05399096651});
        
        checkDensity(1.1, 1, x, new double[]{0.003266819056,0.043983595980,0.217852177033,0.396952547477,0.266085249899});
    }

// org.apache.commons.math3.distribution.NormalDistributionTest::testExtremeValues
    public void testExtremeValues() {
        NormalDistribution distribution = new NormalDistribution(0, 1);
        for (int i = 0; i < 100; i++) { 
            double lowerTail = distribution.cumulativeProbability(-i);
            double upperTail = distribution.cumulativeProbability(i);
            if (i < 9) { 
                
                
                Assert.assertTrue(lowerTail > 0.0d);
                Assert.assertTrue(upperTail < 1.0d);
            }
            else { 
                Assert.assertTrue(lowerTail < 0.00001);
                Assert.assertTrue(upperTail > 0.99999);
            }
        }

        Assert.assertEquals(distribution.cumulativeProbability(Double.MAX_VALUE), 1, 0);
        Assert.assertEquals(distribution.cumulativeProbability(-Double.MAX_VALUE), 0, 0);
        Assert.assertEquals(distribution.cumulativeProbability(Double.POSITIVE_INFINITY), 1, 0);
        Assert.assertEquals(distribution.cumulativeProbability(Double.NEGATIVE_INFINITY), 0, 0);
    }

// org.apache.commons.math3.distribution.NormalDistributionTest::testMath280
    public void testMath280() {
        NormalDistribution normal = new NormalDistribution(0,1);
        double result = normal.inverseCumulativeProbability(0.9986501019683698);
        Assert.assertEquals(3.0, result, defaultTolerance);
        result = normal.inverseCumulativeProbability(0.841344746068543);
        Assert.assertEquals(1.0, result, defaultTolerance);
        result = normal.inverseCumulativeProbability(0.9999683287581673);
        Assert.assertEquals(4.0, result, defaultTolerance);
        result = normal.inverseCumulativeProbability(0.9772498680518209);
        Assert.assertEquals(2.0, result, defaultTolerance);
    }

// org.apache.commons.math3.distribution.NormalDistributionTest::testMoments
    public void testMoments() {
        final double tol = 1e-9;
        NormalDistribution dist;

        dist = new NormalDistribution(0, 1);
        Assert.assertEquals(dist.getNumericalMean(), 0, tol);
        Assert.assertEquals(dist.getNumericalVariance(), 1, tol);

        dist = new NormalDistribution(2.2, 1.4);
        Assert.assertEquals(dist.getNumericalMean(), 2.2, tol);
        Assert.assertEquals(dist.getNumericalVariance(), 1.4 * 1.4, tol);

        dist = new NormalDistribution(-2000.9, 10.4);
        Assert.assertEquals(dist.getNumericalMean(), -2000.9, tol);
        Assert.assertEquals(dist.getNumericalVariance(), 10.4 * 10.4, tol);
    }

// org.apache.commons.math3.distribution.PascalDistributionTest::testDegenerate0
    public void testDegenerate0() {
        setDistribution(new PascalDistribution(5, 0.0d));
        setCumulativeTestPoints(new int[] {-1, 0, 1, 5, 10 });
        setCumulativeTestValues(new double[] {0d, 0d, 0d, 0d, 0d});
        setDensityTestPoints(new int[] {-1, 0, 1, 10, 11});
        setDensityTestValues(new double[] {0d, 0d, 0d, 0d, 0d});
        setInverseCumulativeTestPoints(new double[] {0.1d, 0.5d});
        setInverseCumulativeTestValues(new int[] {Integer.MAX_VALUE, Integer.MAX_VALUE});
        verifyDensities();
        verifyCumulativeProbabilities();
        verifyInverseCumulativeProbabilities();
    }

// org.apache.commons.math3.distribution.PascalDistributionTest::testDegenerate1
    public void testDegenerate1() {
        setDistribution(new PascalDistribution(5, 1.0d));
        setCumulativeTestPoints(new int[] {-1, 0, 1, 2, 5, 10 });
        setCumulativeTestValues(new double[] {0d, 1d, 1d, 1d, 1d, 1d});
        setDensityTestPoints(new int[] {-1, 0, 1, 2, 5, 10});
        setDensityTestValues(new double[] {0d, 1d, 0d, 0d, 0d, 0d});
        setInverseCumulativeTestPoints(new double[] {0.1d, 0.5d});
        setInverseCumulativeTestValues(new int[] {0, 0});
        verifyDensities();
        verifyCumulativeProbabilities();
        verifyInverseCumulativeProbabilities();
    }

// org.apache.commons.math3.distribution.PascalDistributionTest::testMoments
    public void testMoments() {
        final double tol = 1e-9;
        PascalDistribution dist;

        dist = new PascalDistribution(10, 0.5);
        Assert.assertEquals(dist.getNumericalMean(), ( 10d * 0.5d ) / 0.5d, tol);
        Assert.assertEquals(dist.getNumericalVariance(), ( 10d * 0.5d ) / (0.5d * 0.5d), tol);

        dist = new PascalDistribution(25, 0.7);
        Assert.assertEquals(dist.getNumericalMean(), ( 25d * 0.3d ) / 0.7d, tol);
        Assert.assertEquals(dist.getNumericalVariance(), ( 25d * 0.3d ) / (0.7d * 0.7d), tol);
    }
