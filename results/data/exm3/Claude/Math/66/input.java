// buggy function
    public BrentOptimizer() {
        setMaxEvaluations(Integer.MAX_VALUE);
        setMaximalIterationCount(100);
        setAbsoluteAccuracy(1E-10);
        setRelativeAccuracy(1.0e-14);
    }

    protected double doOptimize()
        throws MaxIterationsExceededException, FunctionEvaluationException {
        throw new UnsupportedOperationException();
    }

    public double optimize(final UnivariateRealFunction f, final GoalType goalType, final double min, final double max, final double startValue) throws MaxIterationsExceededException, FunctionEvaluationException {
        clearResult();
        return localMin(getGoalType() == GoalType.MINIMIZE,
                        f, goalType, min, startValue, max,
                        getRelativeAccuracy(), getAbsoluteAccuracy());
    }

    public double optimize(final UnivariateRealFunction f, final GoalType goalType, final double min, final double max) throws MaxIterationsExceededException, FunctionEvaluationException {
        return optimize(f, goalType, min, max, min + GOLDEN_SECTION * (max - min));
    }

    private double localMin(boolean isMinim,
                            UnivariateRealFunction f,
                            GoalType goalType,
                            double lo, double mid, double hi,
                            double eps, double t)
        throws MaxIterationsExceededException, FunctionEvaluationException {
        if (eps <= 0) {
            throw new NotStrictlyPositiveException(eps);
        }
        if (t <= 0) {
            throw new NotStrictlyPositiveException(t);
        }
        double a, b;
        if (lo < hi) {
            a = lo;
            b = hi;
        } else {
            a = hi;
            b = lo;
        }

        double x = mid;
        double v = x;
        double w = x;
        double d = 0;
        double e = 0;
        double fx = computeObjectiveValue(f, x);
        if (goalType == GoalType.MAXIMIZE) {
            fx = -fx;
        }
        double fv = fx;
        double fw = fx;

        int count = 0;
        while (count < maximalIterationCount) {
            double m = 0.5 * (a + b);
            final double tol1 = eps * Math.abs(x) + t;
            final double tol2 = 2 * tol1;

            // Check stopping criterion.
            if (Math.abs(x - m) > tol2 - 0.5 * (b - a)) {
                double p = 0;
                double q = 0;
                double r = 0;
                double u = 0;

                if (Math.abs(e) > tol1) { // Fit parabola.
                    r = (x - w) * (fx - fv);
                    q = (x - v) * (fx - fw);
                    p = (x - v) * q - (x - w) * r;
                    q = 2 * (q - r);

                    if (q > 0) {
                        p = -p;
                    } else {
                        q = -q;
                    }

                    r = e;
                    e = d;

                    if (p > q * (a - x)
                        && p < q * (b - x)
                        && Math.abs(p) < Math.abs(0.5 * q * r)) {
                        // Parabolic interpolation step.
                        d = p / q;
                        u = x + d;

                        // f must not be evaluated too close to a or b.
                        if (u - a < tol2
                            || b - u < tol2) {
                            if (x <= m) {
                                d = tol1;
                            } else {
                                d = -tol1;
                            }
                        }
                    } else {
                        // Golden section step.
                        if (x < m) {
                            e = b - x;
                        } else {
                            e = a - x;
                        }
                        d = GOLDEN_SECTION * e;
                    }
                } else {
                    // Golden section step.
                    if (x < m) {
                        e = b - x;
                    } else {
                        e = a - x;
                    }
                    d = GOLDEN_SECTION * e;
                }

                // Update by at least "tol1".
                if (Math.abs(d) < tol1) {
                    if (d >= 0) {
                        u = x + tol1;
                    } else {
                        u = x - tol1;
                    }
                } else {
                    u = x + d;
                }

                double fu = computeObjectiveValue(f, u);
                if (goalType == GoalType.MAXIMIZE) {
                    fu = -fu;
                }

                // Update a, b, v, w and x.
                if (fu <= fx) {
                    if (u < x) {
                        b = x;
                    } else {
                        a = x;
                    }
                    v = w;
                    fv = fw;
                    w = x;
                    fw = fx;
                    x = u;
                    fx = fu;
                } else {
                    if (u < x) {
                        a = u;
                    } else {
                        b = u;
                    }
                    if (fu <= fw
                        || w == x) {
                        v = w;
                        fv = fw;
                        w = u;
                        fw = fu;
                    } else if (fu <= fv
                               || v == x
                               || v == w) {
                        v = u;
                        fv = fu;
                    }
                }
            } else { // termination
                setResult(x, (goalType == GoalType.MAXIMIZE) ? -fx : fx, count);
                return x;
            }
            ++count;
        }
        throw new MaxIterationsExceededException(maximalIterationCount);
    }

// trigger testcase
// org/apache/commons/math/optimization/MultiStartUnivariateRealOptimizerTest.java::testQuinticMin
@Test
    public void testQuinticMin() throws MathException {
        // The quintic function has zeros at 0, +-0.5 and +-1.
        // The function has extrema (first derivative is zero) at 0.27195613 and 0.82221643,
        UnivariateRealFunction f = new QuinticFunction();
        UnivariateRealOptimizer underlying = new BrentOptimizer();
        underlying.setRelativeAccuracy(1e-15);
        JDKRandomGenerator g = new JDKRandomGenerator();
        g.setSeed(4312000053L);
        MultiStartUnivariateRealOptimizer minimizer =
            new MultiStartUnivariateRealOptimizer(underlying, 5, g);
        minimizer.setAbsoluteAccuracy(10 * minimizer.getAbsoluteAccuracy());
        minimizer.setRelativeAccuracy(10 * minimizer.getRelativeAccuracy());

        try {
            minimizer.getOptima();
            fail("an exception should have been thrown");
        } catch (IllegalStateException ise) {
            // expected
        } catch (Exception e) {
            fail("wrong exception caught");
        }
        try {
            minimizer.getOptimaValues();
            fail("an exception should have been thrown");
        } catch (IllegalStateException ise) {
            // expected
        } catch (Exception e) {
            fail("wrong exception caught");
        }

        double result = minimizer.optimize(f, GoalType.MINIMIZE, -0.3, -0.2);
        assertEquals(-0.2719561270319131, result, 1.0e-13);
        assertEquals(-0.2719561270319131, minimizer.getResult(), 1.0e-13);
        assertEquals(-0.04433426954946637, minimizer.getFunctionValue(), 1.0e-13);

        double[] optima = minimizer.getOptima();
        double[] optimaValues = minimizer.getOptimaValues();
        for (int i = 0; i < optima.length; ++i) {
            assertEquals(f.value(optima[i]), optimaValues[i], 1.0e-10);
        }
        assertTrue(minimizer.getEvaluations()    >= 120);
        assertTrue(minimizer.getEvaluations()    <= 170);
        assertTrue(minimizer.getIterationCount() >= 120);
        assertTrue(minimizer.getIterationCount() <= 170);
    }

// org/apache/commons/math/optimization/MultiStartUnivariateRealOptimizerTest.java::testSinMin
@Test
    public void testSinMin() throws MathException {
        UnivariateRealFunction f = new SinFunction();
        UnivariateRealOptimizer underlying = new BrentOptimizer();
        JDKRandomGenerator g = new JDKRandomGenerator();
        g.setSeed(44428400075l);
        MultiStartUnivariateRealOptimizer minimizer =
            new MultiStartUnivariateRealOptimizer(underlying, 10, g);
        minimizer.optimize(f, GoalType.MINIMIZE, -100.0, 100.0);
        double[] optima = minimizer.getOptima();
        double[] optimaValues = minimizer.getOptimaValues();
        for (int i = 1; i < optima.length; ++i) {
            double d = (optima[i] - optima[i-1]) / (2 * Math.PI);
            assertTrue (Math.abs(d - Math.rint(d)) < 1.0e-8);
            assertEquals(-1.0, f.value(optima[i]), 1.0e-10);
            assertEquals(f.value(optima[i]), optimaValues[i], 1.0e-10);
        }
        assertTrue(minimizer.getEvaluations() > 150);
        assertTrue(minimizer.getEvaluations() < 250);
    }

// org/apache/commons/math/optimization/univariate/BrentOptimizerTest.java::testQuinticMinStatistics
@Test
    public void testQuinticMinStatistics() throws MathException {
        // The function has local minima at -0.27195613 and 0.82221643.
        UnivariateRealFunction f = new QuinticFunction();
        UnivariateRealOptimizer minimizer = new BrentOptimizer();
        minimizer.setRelativeAccuracy(1e-10);
        minimizer.setAbsoluteAccuracy(1e-11);

        final DescriptiveStatistics[] stat = new DescriptiveStatistics[3];
        for (int i = 0; i < stat.length; i++) {
            stat[i] = new DescriptiveStatistics();
        }

        final double min = -0.75;
        final double max = 0.25;
        final int nSamples = 200;
        final double delta = (max - min) / nSamples;
        for (int i = 0; i < nSamples; i++) {
            final double start = min + i * delta;
            stat[0].addValue(minimizer.optimize(f, GoalType.MINIMIZE, min, max, start));
            stat[1].addValue(minimizer.getIterationCount());
            stat[2].addValue(minimizer.getEvaluations());
        }

        final double meanOptValue = stat[0].getMean();
        final double medianIter = stat[1].getPercentile(50);
        final double medianEval = stat[2].getPercentile(50);
        assertTrue(meanOptValue > -0.27195612812 && meanOptValue < -0.27195612811);
        assertEquals(medianIter, 17, Math.ulp(1d));
        assertEquals(medianEval, 18, Math.ulp(1d));
    }

// org/apache/commons/math/optimization/univariate/BrentOptimizerTest.java::testSinMin
@Test
    public void testSinMin() throws MathException {
        UnivariateRealFunction f = new SinFunction();
        UnivariateRealOptimizer minimizer = new BrentOptimizer();
        minimizer.setMaxEvaluations(200);
        assertEquals(200, minimizer.getMaxEvaluations());
        try {
            minimizer.getResult();
            fail("an exception should have been thrown");
        } catch (NoDataException ise) {
            // expected
        } catch (Exception e) {
            fail("wrong exception caught");
        }
        assertEquals(3 * Math.PI / 2, minimizer.optimize(f, GoalType.MINIMIZE, 4, 5), 10 * minimizer.getRelativeAccuracy());
        assertTrue(minimizer.getIterationCount() <= 50);
        assertEquals(3 * Math.PI / 2, minimizer.optimize(f, GoalType.MINIMIZE, 1, 5), 10 * minimizer.getRelativeAccuracy());
        assertTrue(minimizer.getIterationCount() <= 50);
        assertTrue(minimizer.getEvaluations()    <= 100);
        assertTrue(minimizer.getEvaluations()    >=  15);
        minimizer.setMaxEvaluations(10);
        try {
            minimizer.optimize(f, GoalType.MINIMIZE, 4, 5);
            fail("an exception should have been thrown");
        } catch (FunctionEvaluationException fee) {
            // expected
        } catch (Exception e) {
            fail("wrong exception caught");
        }
    }
