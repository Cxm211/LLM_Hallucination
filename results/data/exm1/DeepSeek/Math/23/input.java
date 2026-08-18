// buggy code
    protected UnivariatePointValuePair doOptimize() {
        final boolean isMinim = getGoalType() == GoalType.MINIMIZE;
        final double lo = getMin();
        final double mid = getStartValue();
        final double hi = getMax();

        // Optional additional convergence criteria.
        final ConvergenceChecker<UnivariatePointValuePair> checker
            = getConvergenceChecker();

        double a;
        double b;
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
        double fx = computeObjectiveValue(x);
        if (!isMinim) {
            fx = -fx;
        }
        double fv = fx;
        double fw = fx;

        UnivariatePointValuePair previous = null;
        UnivariatePointValuePair current
            = new UnivariatePointValuePair(x, isMinim ? fx : -fx);
        // Best point encountered so far (which is the initial guess).

        int iter = 0;
        while (true) {
            final double m = 0.5 * (a + b);
            final double tol1 = relativeThreshold * FastMath.abs(x) + absoluteThreshold;
            final double tol2 = 2 * tol1;

            // Default stopping criterion.
            final boolean stop = FastMath.abs(x - m) <= tol2 - 0.5 * (b - a);
            if (!stop) {
                double p = 0;
                double q = 0;
                double r = 0;
                double u = 0;

                if (FastMath.abs(e) > tol1) { // Fit parabola.
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

                    if (p > q * (a - x) &&
                        p < q * (b - x) &&
                        FastMath.abs(p) < FastMath.abs(0.5 * q * r)) {
                        // Parabolic interpolation step.
                        d = p / q;
                        u = x + d;

                        // f must not be evaluated too close to a or b.
                        if (u - a < tol2 || b - u < tol2) {
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
                if (FastMath.abs(d) < tol1) {
                    if (d >= 0) {
                        u = x + tol1;
                    } else {
                        u = x - tol1;
                    }
                } else {
                    u = x + d;
                }

                double fu = computeObjectiveValue(u);
                if (!isMinim) {
                    fu = -fu;
                }

                // User-defined convergence checker.
                previous = current;
                current = new UnivariatePointValuePair(u, isMinim ? fu : -fu);

                if (checker != null) {
                    if (checker.converged(iter, previous, current)) {
                        return best(current, previous, isMinim);
                    }
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
                    if (fu <= fw ||
                        Precision.equals(w, x)) {
                        v = w;
                        fv = fw;
                        w = u;
                        fw = fu;
                    } else if (fu <= fv ||
                               Precision.equals(v, x) ||
                               Precision.equals(v, w)) {
                        v = u;
                        fv = fu;
                    }
                }
            } else { // Default termination (Brent's criterion).
                return
                            best(current,
                                 previous,
                            isMinim);
            }
            ++iter;
        }
    }

// relevant test
// org.apache.commons.math3.optimization.direct.PowellOptimizerTest::testSumSinc
    public void testSumSinc() {
        final MultivariateFunction func = new SumSincFunction(-1);

        int dim = 2;
        final double[] minPoint = new double[dim];
        for (int i = 0; i < dim; i++) {
            minPoint[i] = 0;
        }

        double[] init = new double[dim];

        
        for (int i = 0; i < dim; i++) {
            init[i] = minPoint[i];
        }
        doTest(func, minPoint, init,  GoalType.MINIMIZE, 1e-9, 1e-7);

        
        for (int i = 0; i < dim; i++) {
            init[i] = minPoint[i] + 3;
        }
        doTest(func, minPoint, init,  GoalType.MINIMIZE, 1e-9, 1e-5);
    }

// org.apache.commons.math3.optimization.direct.PowellOptimizerTest::testQuadratic
    public void testQuadratic() {
        final MultivariateFunction func = new MultivariateFunction() {
                public double value(double[] x) {
                    final double a = x[0] - 1;
                    final double b = x[1] - 1;
                    return a * a + b * b + 1;
                }
            };

        int dim = 2;
        final double[] minPoint = new double[dim];
        for (int i = 0; i < dim; i++) {
            minPoint[i] = 1;
        }

        double[] init = new double[dim];

        
        for (int i = 0; i < dim; i++) {
            init[i] = minPoint[i];
        }
        doTest(func, minPoint, init, GoalType.MINIMIZE, 1e-9, 1e-8);

        
        for (int i = 0; i < dim; i++) {
            init[i] = minPoint[i] - 20;
        }
        doTest(func, minPoint, init, GoalType.MINIMIZE, 1e-9, 1e-8);
    }

// org.apache.commons.math3.optimization.direct.PowellOptimizerTest::testMaximizeQuadratic
    public void testMaximizeQuadratic() {
        final MultivariateFunction func = new MultivariateFunction() {
                public double value(double[] x) {
                    final double a = x[0] - 1;
                    final double b = x[1] - 1;
                    return -a * a - b * b + 1;
                }
            };

        int dim = 2;
        final double[] maxPoint = new double[dim];
        for (int i = 0; i < dim; i++) {
            maxPoint[i] = 1;
        }

        double[] init = new double[dim];

        
        for (int i = 0; i < dim; i++) {
            init[i] = maxPoint[i];
        }
        doTest(func, maxPoint, init,  GoalType.MAXIMIZE, 1e-9, 1e-8);

        
        for (int i = 0; i < dim; i++) {
            init[i] = maxPoint[i] - 20;
        }
        doTest(func, maxPoint, init, GoalType.MAXIMIZE, 1e-9, 1e-8);
    }

// org.apache.commons.math3.optimization.direct.PowellOptimizerTest::testRelativeToleranceOnScaledValues
    public void testRelativeToleranceOnScaledValues() {
        final MultivariateFunction func = new MultivariateFunction() {
                public double value(double[] x) {
                    final double a = x[0] - 1;
                    final double b = x[1] - 1;
                    return a * a * FastMath.sqrt(FastMath.abs(a)) + b * b + 1;
                }
            };

        int dim = 2;
        final double[] minPoint = new double[dim];
        for (int i = 0; i < dim; i++) {
            minPoint[i] = 1;
        }

        double[] init = new double[dim];
        
        for (int i = 0; i < dim; i++) {
            init[i] = minPoint[i] - 20;
        }

        final double relTol = 1e-10;

        final int maxEval = 1000;
        
        
        final MultivariateOptimizer optim = new PowellOptimizer(relTol, 1e-100);

        final PointValuePair funcResult = optim.optimize(maxEval, func, GoalType.MINIMIZE, init);
        final double funcValue = func.value(funcResult.getPoint());
        final int funcEvaluations = optim.getEvaluations();

        final double scale = 1e10;
        final MultivariateFunction funcScaled = new MultivariateFunction() {
                public double value(double[] x) {
                    return scale * func.value(x);
                }
            };

        final PointValuePair funcScaledResult = optim.optimize(maxEval, funcScaled, GoalType.MINIMIZE, init);
        final double funcScaledValue = funcScaled.value(funcScaledResult.getPoint());
        final int funcScaledEvaluations = optim.getEvaluations();

        
        
        Assert.assertEquals(1, funcScaledValue / (scale * funcValue), relTol);

        
        Assert.assertEquals(funcEvaluations, funcScaledEvaluations);
    }

// org.apache.commons.math3.optimization.univariate.BrentOptimizerTest::testSinMin
    public void testSinMin() {
        UnivariateFunction f = new Sin();
        UnivariateOptimizer optimizer = new BrentOptimizer(1e-10, 1e-14);
        Assert.assertEquals(3 * Math.PI / 2, optimizer.optimize(200, f, GoalType.MINIMIZE, 4, 5).getPoint(), 1e-8);
        Assert.assertTrue(optimizer.getEvaluations() <= 50);
        Assert.assertEquals(200, optimizer.getMaxEvaluations());
        Assert.assertEquals(3 * Math.PI / 2, optimizer.optimize(200, f, GoalType.MINIMIZE, 1, 5).getPoint(), 1e-8);
        Assert.assertTrue(optimizer.getEvaluations() <= 100);
        Assert.assertTrue(optimizer.getEvaluations() >= 15);
        try {
            optimizer.optimize(10, f, GoalType.MINIMIZE, 4, 5);
            Assert.fail("an exception should have been thrown");
        } catch (TooManyEvaluationsException fee) {
            
        }
    }

// org.apache.commons.math3.optimization.univariate.BrentOptimizerTest::testSinMinWithValueChecker
    public void testSinMinWithValueChecker() {
        final UnivariateFunction f = new Sin();
        final ConvergenceChecker<UnivariatePointValuePair> checker = new SimpleUnivariateValueChecker(1e-5, 1e-14);
        
        
        
        final UnivariateOptimizer optimizer = new BrentOptimizer(1e-10, 1e-14, checker);
        final UnivariatePointValuePair result = optimizer.optimize(200, f, GoalType.MINIMIZE, 4, 5);
        Assert.assertEquals(3 * Math.PI / 2, result.getPoint(), 1e-3);
    }

// org.apache.commons.math3.optimization.univariate.BrentOptimizerTest::testBoundaries
    public void testBoundaries() {
        final double lower = -1.0;
        final double upper = +1.0;
        UnivariateFunction f = new UnivariateFunction() {            
            public double value(double x) {
                if (x < lower) {
                    throw new NumberIsTooSmallException(x, lower, true);
                } else if (x > upper) {
                    throw new NumberIsTooLargeException(x, upper, true);
                } else {
                    return x;
                }
            }
        };
        UnivariateOptimizer optimizer = new BrentOptimizer(1e-10, 1e-14);
        Assert.assertEquals(lower,
                            optimizer.optimize(100, f, GoalType.MINIMIZE, lower, upper).getPoint(),
                            1.0e-8);
        Assert.assertEquals(upper,
                            optimizer.optimize(100, f, GoalType.MAXIMIZE, lower, upper).getPoint(),
                            1.0e-8);
    }

// org.apache.commons.math3.optimization.univariate.BrentOptimizerTest::testQuinticMin
    public void testQuinticMin() {
        
        UnivariateFunction f = new QuinticFunction();
        UnivariateOptimizer optimizer = new BrentOptimizer(1e-10, 1e-14);
        Assert.assertEquals(-0.27195613, optimizer.optimize(200, f, GoalType.MINIMIZE, -0.3, -0.2).getPoint(), 1.0e-8);
        Assert.assertEquals( 0.82221643, optimizer.optimize(200, f, GoalType.MINIMIZE,  0.3,  0.9).getPoint(), 1.0e-8);
        Assert.assertTrue(optimizer.getEvaluations() <= 50);

        
        Assert.assertEquals(-0.27195613, optimizer.optimize(200, f, GoalType.MINIMIZE, -1.0, 0.2).getPoint(), 1.0e-8);
        Assert.assertTrue(optimizer.getEvaluations() <= 50);
    }

// org.apache.commons.math3.optimization.univariate.BrentOptimizerTest::testQuinticMinStatistics
    public void testQuinticMinStatistics() {
        
        UnivariateFunction f = new QuinticFunction();
        UnivariateOptimizer optimizer = new BrentOptimizer(1e-11, 1e-14);

        final DescriptiveStatistics[] stat = new DescriptiveStatistics[2];
        for (int i = 0; i < stat.length; i++) {
            stat[i] = new DescriptiveStatistics();
        }

        final double min = -0.75;
        final double max = 0.25;
        final int nSamples = 200;
        final double delta = (max - min) / nSamples;
        for (int i = 0; i < nSamples; i++) {
            final double start = min + i * delta;
            stat[0].addValue(optimizer.optimize(40, f, GoalType.MINIMIZE, min, max, start).getPoint());
            stat[1].addValue(optimizer.getEvaluations());
        }

        final double meanOptValue = stat[0].getMean();
        final double medianEval = stat[1].getPercentile(50);
        Assert.assertTrue(meanOptValue > -0.2719561281);
        Assert.assertTrue(meanOptValue < -0.2719561280);
        Assert.assertEquals(23, (int) medianEval);
    }

// org.apache.commons.math3.optimization.univariate.BrentOptimizerTest::testQuinticMax
    public void testQuinticMax() {
        
        
        UnivariateFunction f = new QuinticFunction();
        UnivariateOptimizer optimizer = new BrentOptimizer(1e-12, 1e-14);
        Assert.assertEquals(0.27195613, optimizer.optimize(100, f, GoalType.MAXIMIZE, 0.2, 0.3).getPoint(), 1e-8);
        try {
            optimizer.optimize(5, f, GoalType.MAXIMIZE, 0.2, 0.3);
            Assert.fail("an exception should have been thrown");
        } catch (TooManyEvaluationsException miee) {
            
        }
    }

// org.apache.commons.math3.optimization.univariate.BrentOptimizerTest::testMinEndpoints
    public void testMinEndpoints() {
        UnivariateFunction f = new Sin();
        UnivariateOptimizer optimizer = new BrentOptimizer(1e-8, 1e-14);

        
        double result = optimizer.optimize(50, f, GoalType.MINIMIZE, 3 * Math.PI / 2, 5).getPoint();
        Assert.assertEquals(3 * Math.PI / 2, result, 1e-6);

        result = optimizer.optimize(50, f, GoalType.MINIMIZE, 4, 3 * Math.PI / 2).getPoint();
        Assert.assertEquals(3 * Math.PI / 2, result, 1e-6);
    }

// org.apache.commons.math3.optimization.univariate.BrentOptimizerTest::testMath832
    public void testMath832() {
        final UnivariateFunction f = new UnivariateFunction() {
                public double value(double x) {
                    final double sqrtX = FastMath.sqrt(x);
                    final double a = 1e2 * sqrtX;
                    final double b = 1e6 / x;
                    final double c = 1e4 / sqrtX;

                    return a + b + c;
                }
            };

        UnivariateOptimizer optimizer = new BrentOptimizer(1e-10, 1e-8);
        final double result = optimizer.optimize(1483,
                                                 f,
                                                 GoalType.MINIMIZE,
                                                 Double.MIN_VALUE,
                                                 Double.MAX_VALUE).getPoint();

        Assert.assertEquals(804.9355825, result, 1e-6);
    }

// org.apache.commons.math3.optimization.univariate.BrentOptimizerTest::testKeepInitIfBest
    public void testKeepInitIfBest() {
        final double minSin = 3 * Math.PI / 2;
        final double offset = 1e-8;
        final double delta = 1e-7;
        final UnivariateFunction f1 = new Sin();
        final UnivariateFunction f2 = new StepFunction(new double[] { minSin, minSin + offset, minSin + 2 * offset},
                                                       new double[] { 0, -1, 0 });
        final UnivariateFunction f = FunctionUtils.add(f1, f2);
        
        
        final double relTol = 1e-8;
        final UnivariateOptimizer optimizer = new BrentOptimizer(relTol, 1e-100);
        final double init = minSin + 1.5 * offset;
        final UnivariatePointValuePair result
            = optimizer.optimize(200, f, GoalType.MINIMIZE,
                                 minSin - 6.789 * delta,
                                 minSin + 9.876 * delta,
                                 init);
        final int numEval = optimizer.getEvaluations();

        final double sol = result.getPoint();
        final double expected = init;

        Assert.assertTrue("Best point not reported", f.value(sol) <= f.value(expected));
    }

// org.apache.commons.math3.optimization.univariate.BrentOptimizerTest::testMath855
    public void testMath855() {
        final double minSin = 3 * Math.PI / 2;
        final double offset = 1e-8;
        final double delta = 1e-7;
        final UnivariateFunction f1 = new Sin();
        final UnivariateFunction f2 = new StepFunction(new double[] { minSin, minSin + offset, minSin + 5 * offset },
                                                       new double[] { 0, -1, 0 });
        final UnivariateFunction f = FunctionUtils.add(f1, f2);
        final UnivariateOptimizer optimizer = new BrentOptimizer(1e-8, 1e-100);
        final UnivariatePointValuePair result
            = optimizer.optimize(200, f, GoalType.MINIMIZE,
                                 minSin - 6.789 * delta,
                                 minSin + 9.876 * delta);
        final int numEval = optimizer.getEvaluations();

        final double sol = result.getPoint();
        final double expected = 4.712389027602411;

        
        
        

        Assert.assertTrue("Best point not reported", f.value(sol) <= f.value(expected));
    }

// org.apache.commons.math3.optimization.univariate.UnivariateMultiStartOptimizerTest::testSinMin
    public void testSinMin() {
        UnivariateFunction f = new Sin();
        UnivariateOptimizer underlying = new BrentOptimizer(1e-10, 1e-14);
        JDKRandomGenerator g = new JDKRandomGenerator();
        g.setSeed(44428400075l);
        UnivariateMultiStartOptimizer<UnivariateFunction> optimizer =
            new UnivariateMultiStartOptimizer<UnivariateFunction>(underlying, 10, g);
        optimizer.optimize(300, f, GoalType.MINIMIZE, -100.0, 100.0);
        UnivariatePointValuePair[] optima = optimizer.getOptima();
        for (int i = 1; i < optima.length; ++i) {
            double d = (optima[i].getPoint() - optima[i-1].getPoint()) / (2 * FastMath.PI);
            Assert.assertTrue(FastMath.abs(d - FastMath.rint(d)) < 1.0e-8);
            Assert.assertEquals(-1.0, f.value(optima[i].getPoint()), 1.0e-10);
            Assert.assertEquals(f.value(optima[i].getPoint()), optima[i].getValue(), 1.0e-10);
        }
        Assert.assertTrue(optimizer.getEvaluations() > 200);
        Assert.assertTrue(optimizer.getEvaluations() < 300);
    }

// org.apache.commons.math3.optimization.univariate.UnivariateMultiStartOptimizerTest::testQuinticMin
    public void testQuinticMin() {
        
        
        UnivariateFunction f = new QuinticFunction();
        UnivariateOptimizer underlying = new BrentOptimizer(1e-9, 1e-14);
        JDKRandomGenerator g = new JDKRandomGenerator();
        g.setSeed(4312000053L);
        UnivariateMultiStartOptimizer<UnivariateFunction> optimizer =
            new UnivariateMultiStartOptimizer<UnivariateFunction>(underlying, 5, g);

        UnivariatePointValuePair optimum
            = optimizer.optimize(300, f, GoalType.MINIMIZE, -0.3, -0.2);
        Assert.assertEquals(-0.2719561293, optimum.getPoint(), 1e-9);
        Assert.assertEquals(-0.0443342695, optimum.getValue(), 1e-9);

        UnivariatePointValuePair[] optima = optimizer.getOptima();
        for (int i = 0; i < optima.length; ++i) {
            Assert.assertEquals(f.value(optima[i].getPoint()), optima[i].getValue(), 1e-9);
        }
        Assert.assertTrue(optimizer.getEvaluations() >= 50);
        Assert.assertTrue(optimizer.getEvaluations() <= 100);
    }

// org.apache.commons.math3.optimization.univariate.UnivariateMultiStartOptimizerTest::testBadFunction
    public void testBadFunction() {
        UnivariateFunction f = new UnivariateFunction() {
                public double value(double x) {
                    if (x < 0) {
                        throw new LocalException();
                    }
                    return 0;
                }
            };
        UnivariateOptimizer underlying = new BrentOptimizer(1e-9, 1e-14);
        JDKRandomGenerator g = new JDKRandomGenerator();
        g.setSeed(4312000053L);
        UnivariateMultiStartOptimizer<UnivariateFunction> optimizer =
            new UnivariateMultiStartOptimizer<UnivariateFunction>(underlying, 5, g);
 
        try {
            optimizer.optimize(300, f, GoalType.MINIMIZE, -0.3, -0.2);
            Assert.fail();
        } catch (LocalException e) {
            
        }

        
        Assert.assertTrue(optimizer.getOptima()[0] == null);
    }
