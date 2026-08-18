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
// org.apache.commons.math3.optimization.direct.CMAESOptimizerTest::testTwoAxes
    public void testTwoAxes() {
        double[] startPoint = point(DIM,1.0);
        double[] insigma = point(DIM,0.1);
        double[][] boundaries = null;
        PointValuePair expected =
            new PointValuePair(point(DIM,0.0),0.0);
        doTest(new TwoAxes(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, 2*LAMBDA, true, 0, 1e-13,
                1e-13, 1e-6, 200000, expected);
        doTest(new TwoAxes(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, 2*LAMBDA, false, 0, 1e-13,
                1e-8, 1e-3, 200000, expected);
    }

// org.apache.commons.math3.optimization.direct.CMAESOptimizerTest::testCigTab
    public void testCigTab() {}

// org.apache.commons.math3.optimization.direct.CMAESOptimizerTest::testSphere
    public void testSphere() {
        double[] startPoint = point(DIM,1.0);
        double[] insigma = point(DIM,0.1);
        double[][] boundaries = null;
        PointValuePair expected =
            new PointValuePair(point(DIM,0.0),0.0);
        doTest(new Sphere(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, LAMBDA, true, 0, 1e-13,
                1e-13, 1e-6, 100000, expected);
        doTest(new Sphere(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, LAMBDA, false, 0, 1e-13,
                1e-13, 1e-6, 100000, expected);
    }

// org.apache.commons.math3.optimization.direct.CMAESOptimizerTest::testTablet
    public void testTablet() {
        double[] startPoint = point(DIM,1.0);
        double[] insigma = point(DIM,0.1);
        double[][] boundaries = null;
        PointValuePair expected =
            new PointValuePair(point(DIM,0.0),0.0);
        doTest(new Tablet(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, LAMBDA, true, 0, 1e-13,
                1e-13, 1e-6, 100000, expected);
        doTest(new Tablet(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, LAMBDA, false, 0, 1e-13,
                1e-13, 1e-6, 100000, expected);
    }

// org.apache.commons.math3.optimization.direct.CMAESOptimizerTest::testDiffPow
    public void testDiffPow() {
        double[] startPoint = point(DIM,1.0);
        double[] insigma = point(DIM,0.1);
        double[][] boundaries = null;
        PointValuePair expected =
            new PointValuePair(point(DIM,0.0),0.0);
        doTest(new DiffPow(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, 10, true, 0, 1e-13,
                1e-8, 1e-1, 100000, expected);
        doTest(new DiffPow(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, 10, false, 0, 1e-13,
                1e-8, 2e-1, 100000, expected);
    }

// org.apache.commons.math3.optimization.direct.CMAESOptimizerTest::testSsDiffPow
    public void testSsDiffPow() {
        double[] startPoint = point(DIM,1.0);
        double[] insigma = point(DIM,0.1);
        double[][] boundaries = null;
        PointValuePair expected =
            new PointValuePair(point(DIM,0.0),0.0);
        doTest(new SsDiffPow(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, 10, true, 0, 1e-13,
                1e-4, 1e-1, 200000, expected);
        doTest(new SsDiffPow(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, 10, false, 0, 1e-13,
                1e-4, 1e-1, 200000, expected);
    }

// org.apache.commons.math3.optimization.direct.CMAESOptimizerTest::testAckley
    public void testAckley() {
        double[] startPoint = point(DIM,1.0);
        double[] insigma = point(DIM,1.0);
        double[][] boundaries = null;
        PointValuePair expected =
            new PointValuePair(point(DIM,0.0),0.0);
        doTest(new Ackley(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, 2*LAMBDA, true, 0, 1e-13,
                1e-9, 1e-5, 100000, expected);
        doTest(new Ackley(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, 2*LAMBDA, false, 0, 1e-13,
                1e-9, 1e-5, 100000, expected);
    }

// org.apache.commons.math3.optimization.direct.CMAESOptimizerTest::testRastrigin
    public void testRastrigin() {
        double[] startPoint = point(DIM,0.1);
        double[] insigma = point(DIM,0.1);
        double[][] boundaries = null;
        PointValuePair expected =
            new PointValuePair(point(DIM,0.0),0.0);
        doTest(new Rastrigin(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, (int)(200*Math.sqrt(DIM)), true, 0, 1e-13,
                1e-13, 1e-6, 200000, expected);
        doTest(new Rastrigin(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, (int)(200*Math.sqrt(DIM)), false, 0, 1e-13,
                1e-13, 1e-6, 200000, expected);
    }

// org.apache.commons.math3.optimization.direct.CMAESOptimizerTest::testConstrainedRosen
    public void testConstrainedRosen() {
        double[] startPoint = point(DIM, 0.1);
        double[] insigma = point(DIM, 0.1);
        double[][] boundaries = boundaries(DIM, -1, 2);
        PointValuePair expected =
            new PointValuePair(point(DIM,1.0),0.0);
        doTest(new Rosen(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, 2*LAMBDA, true, 0, 1e-13,
                1e-13, 1e-6, 100000, expected);
        doTest(new Rosen(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, 2*LAMBDA, false, 0, 1e-13,
                1e-13, 1e-6, 100000, expected);
    }

// org.apache.commons.math3.optimization.direct.CMAESOptimizerTest::testDiagonalRosen
    public void testDiagonalRosen() {}

// org.apache.commons.math3.optimization.direct.CMAESOptimizerTest::testMath864
    public void testMath864() {
        final CMAESOptimizer optimizer = new CMAESOptimizer();
        final MultivariateFunction fitnessFunction = new MultivariateFunction() {
                public double value(double[] parameters) {
                    final double target = 1;
                    final double error = target - parameters[0];
                    return error * error;
                }
            };

        final double[] start = { 0 };
        final double[] lower = { -1e6 };
        final double[] upper = { 1.5 };
        final double[] result = optimizer.optimize(10000, fitnessFunction, GoalType.MINIMIZE,
                                                   start, lower, upper).getPoint();
        Assert.assertTrue("Out of bounds (" + result[0] + " > " + upper[0] + ")",
                          result[0] <= upper[0]);
    }

// org.apache.commons.math3.optimization.direct.CMAESOptimizerTest::testFitAccuracyDependsOnBoundary
    public void testFitAccuracyDependsOnBoundary() {
        final CMAESOptimizer optimizer = new CMAESOptimizer();
        final MultivariateFunction fitnessFunction = new MultivariateFunction() {
                public double value(double[] parameters) {
                    final double target = 11.1;
                    final double error = target - parameters[0];
                    return error * error;
                }
            };

        final double[] start = { 1 };
 
        
        PointValuePair result = optimizer.optimize(100000, fitnessFunction, GoalType.MINIMIZE,
                                                   start);
        final double resNoBound = result.getPoint()[0];

        
        final double[] lower = { -20 };
        final double[] upper = { 5e16 };
        result = optimizer.optimize(100000, fitnessFunction, GoalType.MINIMIZE,
                                    start, lower, upper);
        final double resNearLo = result.getPoint()[0];

        
        lower[0] = -5e16;
        upper[0] = 20;
        result = optimizer.optimize(100000, fitnessFunction, GoalType.MINIMIZE,
                                    start, lower, upper);
        final double resNearHi = result.getPoint()[0];

        
        
        

        
        
        Assert.assertEquals(resNoBound, resNearLo, 1e-3);
        Assert.assertEquals(resNoBound, resNearHi, 1e-3);
    }

// org.apache.commons.math3.optimization.direct.MultivariateFunctionMappingAdapterTest::testStartSimplexInsideRange
    public void testStartSimplexInsideRange() {

        final BiQuadratic biQuadratic = new BiQuadratic(2.0, 2.5, 1.0, 3.0, 2.0, 3.0);
        final MultivariateFunctionMappingAdapter wrapped =
                new MultivariateFunctionMappingAdapter(biQuadratic,
                                                           biQuadratic.getLower(),
                                                           biQuadratic.getUpper());

        SimplexOptimizer optimizer = new SimplexOptimizer(1e-10, 1e-30);
        optimizer.setSimplex(new NelderMeadSimplex(new double[][] {
            wrapped.boundedToUnbounded(new double[] { 1.5, 2.75 }),
            wrapped.boundedToUnbounded(new double[] { 1.5, 2.95 }),
            wrapped.boundedToUnbounded(new double[] { 1.7, 2.90 })
        }));

        final PointValuePair optimum
            = optimizer.optimize(300, wrapped, GoalType.MINIMIZE,
                                 wrapped.boundedToUnbounded(new double[] { 1.5, 2.25 }));
        final double[] bounded = wrapped.unboundedToBounded(optimum.getPoint());

        Assert.assertEquals(biQuadratic.getBoundedXOptimum(), bounded[0], 2e-7);
        Assert.assertEquals(biQuadratic.getBoundedYOptimum(), bounded[1], 2e-7);

    }

// org.apache.commons.math3.optimization.direct.MultivariateFunctionMappingAdapterTest::testOptimumOutsideRange
    public void testOptimumOutsideRange() {

        final BiQuadratic biQuadratic = new BiQuadratic(4.0, 0.0, 1.0, 3.0, 2.0, 3.0);
        final MultivariateFunctionMappingAdapter wrapped =
                new MultivariateFunctionMappingAdapter(biQuadratic,
                                                           biQuadratic.getLower(),
                                                           biQuadratic.getUpper());

        SimplexOptimizer optimizer = new SimplexOptimizer(1e-10, 1e-30);
        optimizer.setSimplex(new NelderMeadSimplex(new double[][] {
            wrapped.boundedToUnbounded(new double[] { 1.5, 2.75 }),
            wrapped.boundedToUnbounded(new double[] { 1.5, 2.95 }),
            wrapped.boundedToUnbounded(new double[] { 1.7, 2.90 })
        }));

        final PointValuePair optimum
            = optimizer.optimize(100, wrapped, GoalType.MINIMIZE,
                                 wrapped.boundedToUnbounded(new double[] { 1.5, 2.25 }));
        final double[] bounded = wrapped.unboundedToBounded(optimum.getPoint());

        Assert.assertEquals(biQuadratic.getBoundedXOptimum(), bounded[0], 2e-7);
        Assert.assertEquals(biQuadratic.getBoundedYOptimum(), bounded[1], 2e-7);

    }

// org.apache.commons.math3.optimization.direct.MultivariateFunctionMappingAdapterTest::testUnbounded
    public void testUnbounded() {

        final BiQuadratic biQuadratic = new BiQuadratic(4.0, 0.0,
                                                        Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY,
                                                        Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
        final MultivariateFunctionMappingAdapter wrapped =
                new MultivariateFunctionMappingAdapter(biQuadratic,
                                                           biQuadratic.getLower(),
                                                           biQuadratic.getUpper());

        SimplexOptimizer optimizer = new SimplexOptimizer(1e-10, 1e-30);
        optimizer.setSimplex(new NelderMeadSimplex(new double[][] {
            wrapped.boundedToUnbounded(new double[] { 1.5, 2.75 }),
            wrapped.boundedToUnbounded(new double[] { 1.5, 2.95 }),
            wrapped.boundedToUnbounded(new double[] { 1.7, 2.90 })
        }));

        final PointValuePair optimum
            = optimizer.optimize(300, wrapped, GoalType.MINIMIZE,
                                 wrapped.boundedToUnbounded(new double[] { 1.5, 2.25 }));
        final double[] bounded = wrapped.unboundedToBounded(optimum.getPoint());

        Assert.assertEquals(biQuadratic.getBoundedXOptimum(), bounded[0], 2e-7);
        Assert.assertEquals(biQuadratic.getBoundedYOptimum(), bounded[1], 2e-7);

    }

// org.apache.commons.math3.optimization.direct.MultivariateFunctionMappingAdapterTest::testHalfBounded
    public void testHalfBounded() {

        final BiQuadratic biQuadratic = new BiQuadratic(4.0, 4.0,
                                                        1.0, Double.POSITIVE_INFINITY,
                                                        Double.NEGATIVE_INFINITY, 3.0);
        final MultivariateFunctionMappingAdapter wrapped =
                new MultivariateFunctionMappingAdapter(biQuadratic,
                                                           biQuadratic.getLower(),
                                                           biQuadratic.getUpper());

        SimplexOptimizer optimizer = new SimplexOptimizer(1e-13, 1e-30);
        optimizer.setSimplex(new NelderMeadSimplex(new double[][] {
            wrapped.boundedToUnbounded(new double[] { 1.5, 2.75 }),
            wrapped.boundedToUnbounded(new double[] { 1.5, 2.95 }),
            wrapped.boundedToUnbounded(new double[] { 1.7, 2.90 })
        }));

        final PointValuePair optimum
            = optimizer.optimize(200, wrapped, GoalType.MINIMIZE,
                                 wrapped.boundedToUnbounded(new double[] { 1.5, 2.25 }));
        final double[] bounded = wrapped.unboundedToBounded(optimum.getPoint());

        Assert.assertEquals(biQuadratic.getBoundedXOptimum(), bounded[0], 1e-7);
        Assert.assertEquals(biQuadratic.getBoundedYOptimum(), bounded[1], 1e-7);

    }

// org.apache.commons.math3.optimization.direct.MultivariateFunctionPenaltyAdapterTest::testStartSimplexInsideRange
    public void testStartSimplexInsideRange() {

        final BiQuadratic biQuadratic = new BiQuadratic(2.0, 2.5, 1.0, 3.0, 2.0, 3.0);
        final MultivariateFunctionPenaltyAdapter wrapped =
                new MultivariateFunctionPenaltyAdapter(biQuadratic,
                                                           biQuadratic.getLower(),
                                                           biQuadratic.getUpper(),
                                                           1000.0, new double[] { 100.0, 100.0 });

        SimplexOptimizer optimizer = new SimplexOptimizer(1e-10, 1e-30);
        optimizer.setSimplex(new NelderMeadSimplex(new double[] { 1.0, 0.5 }));

        final PointValuePair optimum
            = optimizer.optimize(300, wrapped, GoalType.MINIMIZE, new double[] { 1.5, 2.25 });

        Assert.assertEquals(biQuadratic.getBoundedXOptimum(), optimum.getPoint()[0], 2e-7);
        Assert.assertEquals(biQuadratic.getBoundedYOptimum(), optimum.getPoint()[1], 2e-7);

    }

// org.apache.commons.math3.optimization.direct.MultivariateFunctionPenaltyAdapterTest::testStartSimplexOutsideRange
    public void testStartSimplexOutsideRange() {

        final BiQuadratic biQuadratic = new BiQuadratic(2.0, 2.5, 1.0, 3.0, 2.0, 3.0);
        final MultivariateFunctionPenaltyAdapter wrapped =
                new MultivariateFunctionPenaltyAdapter(biQuadratic,
                                                           biQuadratic.getLower(),
                                                           biQuadratic.getUpper(),
                                                           1000.0, new double[] { 100.0, 100.0 });

        SimplexOptimizer optimizer = new SimplexOptimizer(1e-10, 1e-30);
        optimizer.setSimplex(new NelderMeadSimplex(new double[] { 1.0, 0.5 }));

        final PointValuePair optimum
            = optimizer.optimize(300, wrapped, GoalType.MINIMIZE, new double[] { -1.5, 4.0 });

        Assert.assertEquals(biQuadratic.getBoundedXOptimum(), optimum.getPoint()[0], 2e-7);
        Assert.assertEquals(biQuadratic.getBoundedYOptimum(), optimum.getPoint()[1], 2e-7);

    }

// org.apache.commons.math3.optimization.direct.MultivariateFunctionPenaltyAdapterTest::testOptimumOutsideRange
    public void testOptimumOutsideRange() {

        final BiQuadratic biQuadratic = new BiQuadratic(4.0, 0.0, 1.0, 3.0, 2.0, 3.0);
        final MultivariateFunctionPenaltyAdapter wrapped =
                new MultivariateFunctionPenaltyAdapter(biQuadratic,
                                                           biQuadratic.getLower(),
                                                           biQuadratic.getUpper(),
                                                           1000.0, new double[] { 100.0, 100.0 });

        SimplexOptimizer optimizer = new SimplexOptimizer(new SimplePointChecker<PointValuePair>(1.0e-11, 1.0e-20));
        optimizer.setSimplex(new NelderMeadSimplex(new double[] { 1.0, 0.5 }));

        final PointValuePair optimum
            = optimizer.optimize(600, wrapped, GoalType.MINIMIZE, new double[] { -1.5, 4.0 });

        Assert.assertEquals(biQuadratic.getBoundedXOptimum(), optimum.getPoint()[0], 2e-7);
        Assert.assertEquals(biQuadratic.getBoundedYOptimum(), optimum.getPoint()[1], 2e-7);

    }

// org.apache.commons.math3.optimization.direct.MultivariateFunctionPenaltyAdapterTest::testUnbounded
    public void testUnbounded() {

        final BiQuadratic biQuadratic = new BiQuadratic(4.0, 0.0,
                                                        Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY,
                                                        Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
        final MultivariateFunctionPenaltyAdapter wrapped =
                new MultivariateFunctionPenaltyAdapter(biQuadratic,
                                                           biQuadratic.getLower(),
                                                           biQuadratic.getUpper(),
                                                           1000.0, new double[] { 100.0, 100.0 });

        SimplexOptimizer optimizer = new SimplexOptimizer(1e-10, 1e-30);
        optimizer.setSimplex(new NelderMeadSimplex(new double[] { 1.0, 0.5 }));

        final PointValuePair optimum
            = optimizer.optimize(300, wrapped, GoalType.MINIMIZE, new double[] { -1.5, 4.0 });

        Assert.assertEquals(biQuadratic.getBoundedXOptimum(), optimum.getPoint()[0], 2e-7);
        Assert.assertEquals(biQuadratic.getBoundedYOptimum(), optimum.getPoint()[1], 2e-7);

    }

// org.apache.commons.math3.optimization.direct.MultivariateFunctionPenaltyAdapterTest::testHalfBounded
    public void testHalfBounded() {

        final BiQuadratic biQuadratic = new BiQuadratic(4.0, 4.0,
                                                        1.0, Double.POSITIVE_INFINITY,
                                                        Double.NEGATIVE_INFINITY, 3.0);
        final MultivariateFunctionPenaltyAdapter wrapped =
                new MultivariateFunctionPenaltyAdapter(biQuadratic,
                                                           biQuadratic.getLower(),
                                                           biQuadratic.getUpper(),
                                                           1000.0, new double[] { 100.0, 100.0 });

        SimplexOptimizer optimizer = new SimplexOptimizer(new SimplePointChecker<PointValuePair>(1.0e-10, 1.0e-20));
        optimizer.setSimplex(new NelderMeadSimplex(new double[] { 1.0, 0.5 }));

        final PointValuePair optimum
            = optimizer.optimize(400, wrapped, GoalType.MINIMIZE, new double[] { -1.5, 4.0 });

        Assert.assertEquals(biQuadratic.getBoundedXOptimum(), optimum.getPoint()[0], 2e-7);
        Assert.assertEquals(biQuadratic.getBoundedYOptimum(), optimum.getPoint()[1], 2e-7);

    }

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
        doTest(func, minPoint, init, GoalType.MINIMIZE, 1e-9, 1e-9);

        
        for (int i = 0; i < dim; i++) {
            init[i] = minPoint[i] + 3;
        }
        doTest(func, minPoint, init, GoalType.MINIMIZE, 1e-9, 1e-5);
        
        
        doTest(func, minPoint, init, GoalType.MINIMIZE, 1e-9, 1e-9, 1e-7);
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

// org.apache.commons.math3.optimization.direct.SimplexOptimizerMultiDirectionalTest::testMinimize1
    public void testMinimize1() {
        SimplexOptimizer optimizer = new SimplexOptimizer(1e-11, 1e-30);
        optimizer.setSimplex(new MultiDirectionalSimplex(new double[] { 0.2, 0.2 }));
        final FourExtrema fourExtrema = new FourExtrema();

        final PointValuePair optimum
            = optimizer.optimize(200, fourExtrema, GoalType.MINIMIZE, new double[] { -3, 0 });
        Assert.assertEquals(fourExtrema.xM, optimum.getPoint()[0], 4e-6);
        Assert.assertEquals(fourExtrema.yP, optimum.getPoint()[1], 3e-6);
        Assert.assertEquals(fourExtrema.valueXmYp, optimum.getValue(), 8e-13);
        Assert.assertTrue(optimizer.getEvaluations() > 120);
        Assert.assertTrue(optimizer.getEvaluations() < 150);
    }

// org.apache.commons.math3.optimization.direct.SimplexOptimizerMultiDirectionalTest::testMinimize2
    public void testMinimize2() {
        SimplexOptimizer optimizer = new SimplexOptimizer(1e-11, 1e-30);
        optimizer.setSimplex(new MultiDirectionalSimplex(new double[] { 0.2, 0.2 }));
        final FourExtrema fourExtrema = new FourExtrema();

        final PointValuePair optimum
            =  optimizer.optimize(200, fourExtrema, GoalType.MINIMIZE, new double[] { 1, 0 });
        Assert.assertEquals(fourExtrema.xP, optimum.getPoint()[0], 2e-8);
        Assert.assertEquals(fourExtrema.yM, optimum.getPoint()[1], 3e-6);
        Assert.assertEquals(fourExtrema.valueXpYm, optimum.getValue(), 2e-12);
        Assert.assertTrue(optimizer.getEvaluations() > 120);
        Assert.assertTrue(optimizer.getEvaluations() < 150);
    }

// org.apache.commons.math3.optimization.direct.SimplexOptimizerMultiDirectionalTest::testMaximize1
    public void testMaximize1() {
        SimplexOptimizer optimizer = new SimplexOptimizer(1e-11, 1e-30);
        optimizer.setSimplex(new MultiDirectionalSimplex(new double[] { 0.2, 0.2 }));
        final FourExtrema fourExtrema = new FourExtrema();

        final PointValuePair optimum
            = optimizer.optimize(200, fourExtrema, GoalType.MAXIMIZE, new double[] { -3.0, 0.0 });
        Assert.assertEquals(fourExtrema.xM, optimum.getPoint()[0], 7e-7);
        Assert.assertEquals(fourExtrema.yM, optimum.getPoint()[1], 3e-7);
        Assert.assertEquals(fourExtrema.valueXmYm, optimum.getValue(), 2e-14);
        Assert.assertTrue(optimizer.getEvaluations() > 120);
        Assert.assertTrue(optimizer.getEvaluations() < 150);
    }

// org.apache.commons.math3.optimization.direct.SimplexOptimizerMultiDirectionalTest::testMaximize2
    public void testMaximize2() {
        SimplexOptimizer optimizer = new SimplexOptimizer(new SimpleValueChecker(1e-15, 1e-30));
        optimizer.setSimplex(new MultiDirectionalSimplex(new double[] { 0.2, 0.2 }));
        final FourExtrema fourExtrema = new FourExtrema();

        final PointValuePair optimum
            = optimizer.optimize(200, fourExtrema, GoalType.MAXIMIZE, new double[] { 1, 0 });
        Assert.assertEquals(fourExtrema.xP, optimum.getPoint()[0], 2e-8);
        Assert.assertEquals(fourExtrema.yP, optimum.getPoint()[1], 3e-6);
        Assert.assertEquals(fourExtrema.valueXpYp, optimum.getValue(), 2e-12);
        Assert.assertTrue(optimizer.getEvaluations() > 180);
        Assert.assertTrue(optimizer.getEvaluations() < 220);
    }

// org.apache.commons.math3.optimization.direct.SimplexOptimizerMultiDirectionalTest::testRosenbrock
    public void testRosenbrock() {
        MultivariateFunction rosenbrock =
            new MultivariateFunction() {
                public double value(double[] x) {
                    ++count;
                    double a = x[1] - x[0] * x[0];
                    double b = 1.0 - x[0];
                    return 100 * a * a + b * b;
                }
            };

        count = 0;
        SimplexOptimizer optimizer = new SimplexOptimizer(-1, 1e-3);
        optimizer.setSimplex(new MultiDirectionalSimplex(new double[][] {
                    { -1.2,  1.0 }, { 0.9, 1.2 } , {  3.5, -2.3 }
                }));
        PointValuePair optimum =
            optimizer.optimize(100, rosenbrock, GoalType.MINIMIZE, new double[] { -1.2, 1 });

        Assert.assertEquals(count, optimizer.getEvaluations());
        Assert.assertTrue(optimizer.getEvaluations() > 50);
        Assert.assertTrue(optimizer.getEvaluations() < 100);
        Assert.assertTrue(optimum.getValue() > 1e-2);
    }

// org.apache.commons.math3.optimization.direct.SimplexOptimizerMultiDirectionalTest::testPowell
    public void testPowell() {
        MultivariateFunction powell =
            new MultivariateFunction() {
                public double value(double[] x) {
                    ++count;
                    double a = x[0] + 10 * x[1];
                    double b = x[2] - x[3];
                    double c = x[1] - 2 * x[2];
                    double d = x[0] - x[3];
                    return a * a + 5 * b * b + c * c * c * c + 10 * d * d * d * d;
                }
            };

        count = 0;
        SimplexOptimizer optimizer = new SimplexOptimizer(-1, 1e-3);
        optimizer.setSimplex(new MultiDirectionalSimplex(4));
        PointValuePair optimum =
            optimizer.optimize(1000, powell, GoalType.MINIMIZE, new double[] { 3, -1, 0, 1 });
        Assert.assertEquals(count, optimizer.getEvaluations());
        Assert.assertTrue(optimizer.getEvaluations() > 800);
        Assert.assertTrue(optimizer.getEvaluations() < 900);
        Assert.assertTrue(optimum.getValue() > 1e-2);
    }

// org.apache.commons.math3.optimization.direct.SimplexOptimizerMultiDirectionalTest::testMath283
    public void testMath283() {
        
        
        SimplexOptimizer optimizer = new SimplexOptimizer(1e-14, 1e-14);
        optimizer.setSimplex(new MultiDirectionalSimplex(2));
        final Gaussian2D function = new Gaussian2D(0, 0, 1);
        PointValuePair estimate = optimizer.optimize(1000, function,
                                                         GoalType.MAXIMIZE, function.getMaximumPosition());
        final double EPSILON = 1e-5;
        final double expectedMaximum = function.getMaximum();
        final double actualMaximum = estimate.getValue();
        Assert.assertEquals(expectedMaximum, actualMaximum, EPSILON);

        final double[] expectedPosition = function.getMaximumPosition();
        final double[] actualPosition = estimate.getPoint();
        Assert.assertEquals(expectedPosition[0], actualPosition[0], EPSILON );
        Assert.assertEquals(expectedPosition[1], actualPosition[1], EPSILON );
    }

// org.apache.commons.math3.optimization.direct.SimplexOptimizerNelderMeadTest::testMinimize1
    public void testMinimize1() {
        SimplexOptimizer optimizer = new SimplexOptimizer(1e-10, 1e-30);
        optimizer.setSimplex(new NelderMeadSimplex(new double[] { 0.2, 0.2 }));
        final FourExtrema fourExtrema = new FourExtrema();

        final PointValuePair optimum
            = optimizer.optimize(100, fourExtrema, GoalType.MINIMIZE, new double[] { -3, 0 });
        Assert.assertEquals(fourExtrema.xM, optimum.getPoint()[0], 2e-7);
        Assert.assertEquals(fourExtrema.yP, optimum.getPoint()[1], 2e-5);
        Assert.assertEquals(fourExtrema.valueXmYp, optimum.getValue(), 6e-12);
        Assert.assertTrue(optimizer.getEvaluations() > 60);
        Assert.assertTrue(optimizer.getEvaluations() < 90);
    }

// org.apache.commons.math3.optimization.direct.SimplexOptimizerNelderMeadTest::testMinimize2
    public void testMinimize2() {
        SimplexOptimizer optimizer = new SimplexOptimizer(1e-10, 1e-30);
        optimizer.setSimplex(new NelderMeadSimplex(new double[] { 0.2, 0.2 }));
        final FourExtrema fourExtrema = new FourExtrema();

        final PointValuePair optimum
            = optimizer.optimize(100, fourExtrema, GoalType.MINIMIZE, new double[] { 1, 0 });
        Assert.assertEquals(fourExtrema.xP, optimum.getPoint()[0], 5e-6);
        Assert.assertEquals(fourExtrema.yM, optimum.getPoint()[1], 6e-6);
        Assert.assertEquals(fourExtrema.valueXpYm, optimum.getValue(), 1e-11);
        Assert.assertTrue(optimizer.getEvaluations() > 60);
        Assert.assertTrue(optimizer.getEvaluations() < 90);
    }

// org.apache.commons.math3.optimization.direct.SimplexOptimizerNelderMeadTest::testMaximize1
    public void testMaximize1() {
        SimplexOptimizer optimizer = new SimplexOptimizer(1e-10, 1e-30);
        optimizer.setSimplex(new NelderMeadSimplex(new double[] { 0.2, 0.2 }));
        final FourExtrema fourExtrema = new FourExtrema();

        final PointValuePair optimum
            = optimizer.optimize(100, fourExtrema, GoalType.MAXIMIZE, new double[] { -3, 0 });
        Assert.assertEquals(fourExtrema.xM, optimum.getPoint()[0], 1e-5);
        Assert.assertEquals(fourExtrema.yM, optimum.getPoint()[1], 3e-6);
        Assert.assertEquals(fourExtrema.valueXmYm, optimum.getValue(), 3e-12);
        Assert.assertTrue(optimizer.getEvaluations() > 60);
        Assert.assertTrue(optimizer.getEvaluations() < 90);
    }

// org.apache.commons.math3.optimization.direct.SimplexOptimizerNelderMeadTest::testMaximize2
    public void testMaximize2() {
        SimplexOptimizer optimizer = new SimplexOptimizer(1e-10, 1e-30);
        optimizer.setSimplex(new NelderMeadSimplex(new double[] { 0.2, 0.2 }));
        final FourExtrema fourExtrema = new FourExtrema();

        final PointValuePair optimum
            = optimizer.optimize(100, fourExtrema, GoalType.MAXIMIZE, new double[] { 1, 0 });
        Assert.assertEquals(fourExtrema.xP, optimum.getPoint()[0], 4e-6);
        Assert.assertEquals(fourExtrema.yP, optimum.getPoint()[1], 5e-6);
        Assert.assertEquals(fourExtrema.valueXpYp, optimum.getValue(), 7e-12);
        Assert.assertTrue(optimizer.getEvaluations() > 60);
        Assert.assertTrue(optimizer.getEvaluations() < 90);
    }

// org.apache.commons.math3.optimization.direct.SimplexOptimizerNelderMeadTest::testRosenbrock
    public void testRosenbrock() {

        Rosenbrock rosenbrock = new Rosenbrock();
        SimplexOptimizer optimizer = new SimplexOptimizer(-1, 1e-3);
        optimizer.setSimplex(new NelderMeadSimplex(new double[][] {
                    { -1.2,  1 }, { 0.9, 1.2 } , {  3.5, -2.3 }
                }));
        PointValuePair optimum =
            optimizer.optimize(100, rosenbrock, GoalType.MINIMIZE, new double[] { -1.2, 1 });

        Assert.assertEquals(rosenbrock.getCount(), optimizer.getEvaluations());
        Assert.assertTrue(optimizer.getEvaluations() > 40);
        Assert.assertTrue(optimizer.getEvaluations() < 50);
        Assert.assertTrue(optimum.getValue() < 8e-4);
    }

// org.apache.commons.math3.optimization.direct.SimplexOptimizerNelderMeadTest::testPowell
    public void testPowell() {

        Powell powell = new Powell();
        SimplexOptimizer optimizer = new SimplexOptimizer(-1, 1e-3);
        optimizer.setSimplex(new NelderMeadSimplex(4));
        PointValuePair optimum =
            optimizer.optimize(200, powell, GoalType.MINIMIZE, new double[] { 3, -1, 0, 1 });
        Assert.assertEquals(powell.getCount(), optimizer.getEvaluations());
        Assert.assertTrue(optimizer.getEvaluations() > 110);
        Assert.assertTrue(optimizer.getEvaluations() < 130);
        Assert.assertTrue(optimum.getValue() < 2e-3);
    }

// org.apache.commons.math3.optimization.direct.SimplexOptimizerNelderMeadTest::testLeastSquares1
    public void testLeastSquares1() {

        final RealMatrix factors =
            new Array2DRowRealMatrix(new double[][] {
                    { 1, 0 },
                    { 0, 1 }
                }, false);
        LeastSquaresConverter ls = new LeastSquaresConverter(new MultivariateVectorFunction() {
                public double[] value(double[] variables) {
                    return factors.operate(variables);
                }
            }, new double[] { 2.0, -3.0 });
        SimplexOptimizer optimizer = new SimplexOptimizer(-1, 1e-6);
        optimizer.setSimplex(new NelderMeadSimplex(2));
        PointValuePair optimum =
            optimizer.optimize(200, ls, GoalType.MINIMIZE, new double[] { 10, 10 });
        Assert.assertEquals( 2, optimum.getPointRef()[0], 3e-5);
        Assert.assertEquals(-3, optimum.getPointRef()[1], 4e-4);
        Assert.assertTrue(optimizer.getEvaluations() > 60);
        Assert.assertTrue(optimizer.getEvaluations() < 80);
        Assert.assertTrue(optimum.getValue() < 1.0e-6);
    }

// org.apache.commons.math3.optimization.direct.SimplexOptimizerNelderMeadTest::testLeastSquares2
    public void testLeastSquares2() {

        final RealMatrix factors =
            new Array2DRowRealMatrix(new double[][] {
                    { 1, 0 },
                    { 0, 1 }
                }, false);
        LeastSquaresConverter ls = new LeastSquaresConverter(new MultivariateVectorFunction() {
                public double[] value(double[] variables) {
                    return factors.operate(variables);
                }
            }, new double[] { 2, -3 }, new double[] { 10, 0.1 });
        SimplexOptimizer optimizer = new SimplexOptimizer(-1, 1e-6);
        optimizer.setSimplex(new NelderMeadSimplex(2));
        PointValuePair optimum =
            optimizer.optimize(200, ls, GoalType.MINIMIZE, new double[] { 10, 10 });
        Assert.assertEquals( 2, optimum.getPointRef()[0], 5e-5);
        Assert.assertEquals(-3, optimum.getPointRef()[1], 8e-4);
        Assert.assertTrue(optimizer.getEvaluations() > 60);
        Assert.assertTrue(optimizer.getEvaluations() < 80);
        Assert.assertTrue(optimum.getValue() < 1e-6);
    }

// org.apache.commons.math3.optimization.direct.SimplexOptimizerNelderMeadTest::testLeastSquares3
    public void testLeastSquares3() {

        final RealMatrix factors =
            new Array2DRowRealMatrix(new double[][] {
                    { 1, 0 },
                    { 0, 1 }
                }, false);
        LeastSquaresConverter ls = new LeastSquaresConverter(new MultivariateVectorFunction() {
                public double[] value(double[] variables) {
                    return factors.operate(variables);
                }
            }, new double[] { 2, -3 }, new Array2DRowRealMatrix(new double [][] {
                    { 1, 1.2 }, { 1.2, 2 }
                }));
        SimplexOptimizer optimizer = new SimplexOptimizer(-1, 1e-6);
        optimizer.setSimplex(new NelderMeadSimplex(2));
        PointValuePair optimum =
            optimizer.optimize(200, ls, GoalType.MINIMIZE, new double[] { 10, 10 });
        Assert.assertEquals( 2, optimum.getPointRef()[0], 2e-3);
        Assert.assertEquals(-3, optimum.getPointRef()[1], 8e-4);
        Assert.assertTrue(optimizer.getEvaluations() > 60);
        Assert.assertTrue(optimizer.getEvaluations() < 80);
        Assert.assertTrue(optimum.getValue() < 1e-6);
    }

// org.apache.commons.math3.optimization.direct.SimplexOptimizerNelderMeadTest::testMaxIterations
    public void testMaxIterations() {
        Powell powell = new Powell();
        SimplexOptimizer optimizer = new SimplexOptimizer(-1, 1e-3);
        optimizer.setSimplex(new NelderMeadSimplex(4));
        optimizer.optimize(20, powell, GoalType.MINIMIZE, new double[] { 3, -1, 0, 1 });
    }

// org.apache.commons.math3.optimization.fitting.CurveFitterTest::testMath303
    public void testMath303() {

        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        CurveFitter<ParametricUnivariateFunction> fitter = new CurveFitter<ParametricUnivariateFunction>(optimizer);
        fitter.addObservedPoint(2.805d, 0.6934785852953367d);
        fitter.addObservedPoint(2.74333333333333d, 0.6306772025518496d);
        fitter.addObservedPoint(1.655d, 0.9474675497289684);
        fitter.addObservedPoint(1.725d, 0.9013594835804194d);

        ParametricUnivariateFunction sif = new SimpleInverseFunction();

        double[] initialguess1 = new double[1];
        initialguess1[0] = 1.0d;
        Assert.assertEquals(1, fitter.fit(sif, initialguess1).length);

        double[] initialguess2 = new double[2];
        initialguess2[0] = 1.0d;
        initialguess2[1] = .5d;
        Assert.assertEquals(2, fitter.fit(sif, initialguess2).length);

    }

// org.apache.commons.math3.optimization.fitting.CurveFitterTest::testMath304
    public void testMath304() {

        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        CurveFitter<ParametricUnivariateFunction> fitter = new CurveFitter<ParametricUnivariateFunction>(optimizer);
        fitter.addObservedPoint(2.805d, 0.6934785852953367d);
        fitter.addObservedPoint(2.74333333333333d, 0.6306772025518496d);
        fitter.addObservedPoint(1.655d, 0.9474675497289684);
        fitter.addObservedPoint(1.725d, 0.9013594835804194d);

        ParametricUnivariateFunction sif = new SimpleInverseFunction();

        double[] initialguess1 = new double[1];
        initialguess1[0] = 1.0d;
        Assert.assertEquals(1.6357215104109237, fitter.fit(sif, initialguess1)[0], 1.0e-14);

        double[] initialguess2 = new double[1];
        initialguess2[0] = 10.0d;
        Assert.assertEquals(1.6357215104109237, fitter.fit(sif, initialguess1)[0], 1.0e-14);

    }

// org.apache.commons.math3.optimization.fitting.CurveFitterTest::testMath372
    public void testMath372() {
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        CurveFitter<ParametricUnivariateFunction> curveFitter = new CurveFitter<ParametricUnivariateFunction>(optimizer);

        curveFitter.addObservedPoint( 15,  4443);
        curveFitter.addObservedPoint( 31,  8493);
        curveFitter.addObservedPoint( 62, 17586);
        curveFitter.addObservedPoint(125, 30582);
        curveFitter.addObservedPoint(250, 45087);
        curveFitter.addObservedPoint(500, 50683);

        ParametricUnivariateFunction f = new ParametricUnivariateFunction() {

            public double value(double x, double ... parameters) {

                double a = parameters[0];
                double b = parameters[1];
                double c = parameters[2];
                double d = parameters[3];

                return d + ((a - d) / (1 + FastMath.pow(x / c, b)));
            }

            public double[] gradient(double x, double ... parameters) {

                double a = parameters[0];
                double b = parameters[1];
                double c = parameters[2];
                double d = parameters[3];

                double[] gradients = new double[4];
                double den = 1 + FastMath.pow(x / c, b);

                
                gradients[0] = 1 / den;

                
                
                gradients[1] = -((a - d) * FastMath.pow(x / c, b) * FastMath.log(x / c)) / (den * den);

                
                gradients[2] = (b * FastMath.pow(x / c, b - 1) * (x / (c * c)) * (a - d)) / (den * den);

                
                gradients[3] = 1 - (1 / den);

                return gradients;

            }
        };

        double[] initialGuess = new double[] { 1500, 0.95, 65, 35000 };
        double[] estimatedParameters = curveFitter.fit(f, initialGuess);

        Assert.assertEquals( 2411.00, estimatedParameters[0], 500.00);
        Assert.assertEquals(    1.62, estimatedParameters[1],   0.04);
        Assert.assertEquals(  111.22, estimatedParameters[2],   0.30);
        Assert.assertEquals(55347.47, estimatedParameters[3], 300.00);
        Assert.assertTrue(optimizer.getRMS() < 600.0);

    }

// org.apache.commons.math3.optimization.fitting.GaussianFitterTest::testFit01
    public void testFit01() {
        GaussianFitter fitter = new GaussianFitter(new LevenbergMarquardtOptimizer());
        addDatasetToGaussianFitter(DATASET1, fitter);
        double[] parameters = fitter.fit();

        Assert.assertEquals(3496978.1837704973, parameters[0], 1e-4);
        Assert.assertEquals(4.054933085999146, parameters[1], 1e-4);
        Assert.assertEquals(0.015039355620304326, parameters[2], 1e-4);
    }

// org.apache.commons.math3.optimization.fitting.GaussianFitterTest::testFit02
    public void testFit02() {
        GaussianFitter fitter = new GaussianFitter(new LevenbergMarquardtOptimizer());
        fitter.fit();
    }

// org.apache.commons.math3.optimization.fitting.GaussianFitterTest::testFit03
    public void testFit03() {
        GaussianFitter fitter = new GaussianFitter(new LevenbergMarquardtOptimizer());
        addDatasetToGaussianFitter(new double[][] {
            {4.0254623,  531026.0},
            {4.02804905, 664002.0}},
            fitter);
        fitter.fit();
    }

// org.apache.commons.math3.optimization.fitting.GaussianFitterTest::testFit04
    public void testFit04() {
        GaussianFitter fitter = new GaussianFitter(new LevenbergMarquardtOptimizer());
        addDatasetToGaussianFitter(DATASET2, fitter);
        double[] parameters = fitter.fit();

        Assert.assertEquals(233003.2967252038, parameters[0], 1e-4);
        Assert.assertEquals(-10.654887521095983, parameters[1], 1e-4);
        Assert.assertEquals(4.335937353196641, parameters[2], 1e-4);
    }

// org.apache.commons.math3.optimization.fitting.GaussianFitterTest::testFit05
    public void testFit05() {
        GaussianFitter fitter = new GaussianFitter(new LevenbergMarquardtOptimizer());
        addDatasetToGaussianFitter(DATASET3, fitter);
        double[] parameters = fitter.fit();

        Assert.assertEquals(283863.81929180305, parameters[0], 1e-4);
        Assert.assertEquals(-13.29641995105174, parameters[1], 1e-4);
        Assert.assertEquals(1.7297330293549908, parameters[2], 1e-4);
    }

// org.apache.commons.math3.optimization.fitting.GaussianFitterTest::testFit06
    public void testFit06() {
        GaussianFitter fitter = new GaussianFitter(new LevenbergMarquardtOptimizer());
        addDatasetToGaussianFitter(DATASET4, fitter);
        double[] parameters = fitter.fit();

        Assert.assertEquals(285250.66754309234, parameters[0], 1e-4);
        Assert.assertEquals(-13.528375695228455, parameters[1], 1e-4);
        Assert.assertEquals(1.5204344894331614, parameters[2], 1e-4);
    }

// org.apache.commons.math3.optimization.fitting.GaussianFitterTest::testFit07
    public void testFit07() {
        GaussianFitter fitter = new GaussianFitter(new LevenbergMarquardtOptimizer());
        addDatasetToGaussianFitter(DATASET5, fitter);
        double[] parameters = fitter.fit();

        Assert.assertEquals(3514384.729342235, parameters[0], 1e-4);
        Assert.assertEquals(4.054970307455625, parameters[1], 1e-4);
        Assert.assertEquals(0.015029412832160017, parameters[2], 1e-4);
    }

// org.apache.commons.math3.optimization.fitting.GaussianFitterTest::testMath519
    public void testMath519() {
        
        

        final double[] data = { 
            1.1143831578403364E-29,
            4.95281403484594E-28,
            1.1171347211930288E-26,
            1.7044813962636277E-25,
            1.9784716574832164E-24,
            1.8630236407866774E-23,
            1.4820532905097742E-22,
            1.0241963854632831E-21,
            6.275077366673128E-21,
            3.461808994532493E-20,
            1.7407124684715706E-19,
            8.056687953553974E-19,
            3.460193945992071E-18,
            1.3883326374011525E-17,
            5.233894983671116E-17,
            1.8630791465263745E-16,
            6.288759227922111E-16,
            2.0204433920597856E-15,
            6.198768938576155E-15,
            1.821419346860626E-14,
            5.139176445538471E-14,
            1.3956427429045787E-13,
            3.655705706448139E-13,
            9.253753324779779E-13,
            2.267636001476696E-12,
            5.3880460095836855E-12,
            1.2431632654852931E-11
        };

        GaussianFitter fitter = new GaussianFitter(new LevenbergMarquardtOptimizer());
        for (int i = 0; i < data.length; i++) {
            fitter.addObservedPoint(i, data[i]);
        }
        final double[] p = fitter.fit();

        Assert.assertEquals(53.1572792, p[1], 1e-7);
        Assert.assertEquals(5.75214622, p[2], 1e-8);
    }

// org.apache.commons.math3.optimization.fitting.GaussianFitterTest::testMath798
    public void testMath798() {
        final GaussianFitter fitter = new GaussianFitter(new LevenbergMarquardtOptimizer());

        
        
        
        

        fitter.addObservedPoint(0.23, 395.0);
        
        fitter.addObservedPoint(1.14, 376.0);
        
        fitter.addObservedPoint(2.05, 163.0);
        
        fitter.addObservedPoint(2.95, 49.0);
        
        fitter.addObservedPoint(3.86, 16.0);
        
        fitter.addObservedPoint(4.77, 1.0);

        final double[] p = fitter.fit();

        
        Assert.assertEquals(420.8397296167364, p[0], 1e-12);
        Assert.assertEquals(0.603770729862231, p[1], 1e-15);
        Assert.assertEquals(1.0786447936766612, p[2], 1e-14);
    }

// org.apache.commons.math3.optimization.fitting.HarmonicFitterTest::testPreconditions1
    public void testPreconditions1() {
        HarmonicFitter fitter =
            new HarmonicFitter(new LevenbergMarquardtOptimizer());

        fitter.fit();
    }

// org.apache.commons.math3.optimization.fitting.HarmonicFitterTest::testNoError
    public void testNoError() {
        final double a = 0.2;
        final double w = 3.4;
        final double p = 4.1;
        HarmonicOscillator f = new HarmonicOscillator(a, w, p);

        HarmonicFitter fitter =
            new HarmonicFitter(new LevenbergMarquardtOptimizer());
        for (double x = 0.0; x < 1.3; x += 0.01) {
            fitter.addObservedPoint(1, x, f.value(x));
        }

        final double[] fitted = fitter.fit();
        Assert.assertEquals(a, fitted[0], 1.0e-13);
        Assert.assertEquals(w, fitted[1], 1.0e-13);
        Assert.assertEquals(p, MathUtils.normalizeAngle(fitted[2], p), 1e-13);

        HarmonicOscillator ff = new HarmonicOscillator(fitted[0], fitted[1], fitted[2]);

        for (double x = -1.0; x < 1.0; x += 0.01) {
            Assert.assertTrue(FastMath.abs(f.value(x) - ff.value(x)) < 1e-13);
        }
    }

// org.apache.commons.math3.optimization.fitting.HarmonicFitterTest::test1PercentError
    public void test1PercentError() {
        Random randomizer = new Random(64925784252l);
        final double a = 0.2;
        final double w = 3.4;
        final double p = 4.1;
        HarmonicOscillator f = new HarmonicOscillator(a, w, p);

        HarmonicFitter fitter =
            new HarmonicFitter(new LevenbergMarquardtOptimizer());
        for (double x = 0.0; x < 10.0; x += 0.1) {
            fitter.addObservedPoint(1, x,
                                    f.value(x) + 0.01 * randomizer.nextGaussian());
        }

        final double[] fitted = fitter.fit();
        Assert.assertEquals(a, fitted[0], 7.6e-4);
        Assert.assertEquals(w, fitted[1], 2.7e-3);
        Assert.assertEquals(p, MathUtils.normalizeAngle(fitted[2], p), 1.3e-2);
    }

// org.apache.commons.math3.optimization.fitting.HarmonicFitterTest::testTinyVariationsData
    public void testTinyVariationsData() {
        Random randomizer = new Random(64925784252l);

        HarmonicFitter fitter =
            new HarmonicFitter(new LevenbergMarquardtOptimizer());
        for (double x = 0.0; x < 10.0; x += 0.1) {
            fitter.addObservedPoint(1, x, 1e-7 * randomizer.nextGaussian());
        }

        fitter.fit();
        
        
    }

// org.apache.commons.math3.optimization.fitting.HarmonicFitterTest::testInitialGuess
    public void testInitialGuess() {
        Random randomizer = new Random(45314242l);
        final double a = 0.2;
        final double w = 3.4;
        final double p = 4.1;
        HarmonicOscillator f = new HarmonicOscillator(a, w, p);

        HarmonicFitter fitter =
            new HarmonicFitter(new LevenbergMarquardtOptimizer());
        for (double x = 0.0; x < 10.0; x += 0.1) {
            fitter.addObservedPoint(1, x,
                                    f.value(x) + 0.01 * randomizer.nextGaussian());
        }

        final double[] fitted = fitter.fit(new double[] { 0.15, 3.6, 4.5 });
        Assert.assertEquals(a, fitted[0], 1.2e-3);
        Assert.assertEquals(w, fitted[1], 3.3e-3);
        Assert.assertEquals(p, MathUtils.normalizeAngle(fitted[2], p), 1.7e-2);
    }

// org.apache.commons.math3.optimization.fitting.HarmonicFitterTest::testUnsorted
    public void testUnsorted() {
        Random randomizer = new Random(64925784252l);
        final double a = 0.2;
        final double w = 3.4;
        final double p = 4.1;
        HarmonicOscillator f = new HarmonicOscillator(a, w, p);

        HarmonicFitter fitter =
            new HarmonicFitter(new LevenbergMarquardtOptimizer());

        
        int size = 100;
        double[] xTab = new double[size];
        double[] yTab = new double[size];
        for (int i = 0; i < size; ++i) {
            xTab[i] = 0.1 * i;
            yTab[i] = f.value(xTab[i]) + 0.01 * randomizer.nextGaussian();
        }

        
        for (int i = 0; i < size; ++i) {
            int i1 = randomizer.nextInt(size);
            int i2 = randomizer.nextInt(size);
            double xTmp = xTab[i1];
            double yTmp = yTab[i1];
            xTab[i1] = xTab[i2];
            yTab[i1] = yTab[i2];
            xTab[i2] = xTmp;
            yTab[i2] = yTmp;
        }

        
        for (int i = 0; i < size; ++i) {
            fitter.addObservedPoint(1, xTab[i], yTab[i]);
        }

        final double[] fitted = fitter.fit();
        Assert.assertEquals(a, fitted[0], 7.6e-4);
        Assert.assertEquals(w, fitted[1], 3.5e-3);
        Assert.assertEquals(p, MathUtils.normalizeAngle(fitted[2], p), 1.5e-2);
    }

// org.apache.commons.math3.optimization.fitting.HarmonicFitterTest::testMath844
    public void testMath844() {
        final double[] y = { 0, 1, 2, 3, 2, 1,
                             0, -1, -2, -3, -2, -1,
                             0, 1, 2, 3, 2, 1,
                             0, -1, -2, -3, -2, -1,
                             0, 1, 2, 3, 2, 1, 0 };
        final int len = y.length;
        final WeightedObservedPoint[] points = new WeightedObservedPoint[len];
        for (int i = 0; i < len; i++) {
            points[i] = new WeightedObservedPoint(1, i, y[i]);
        }

        
        
        
        
        
        final HarmonicFitter.ParameterGuesser guesser
            = new HarmonicFitter.ParameterGuesser(points);
    }

// org.apache.commons.math3.optimization.fitting.PolynomialFitterTest::testFit
    public void testFit() {
        final RealDistribution rng = new UniformRealDistribution(-100, 100);
        rng.reseedRandomGenerator(64925784252L);

        final LevenbergMarquardtOptimizer optim = new LevenbergMarquardtOptimizer();
        final PolynomialFitter fitter = new PolynomialFitter(optim);
        final double[] coeff = { 12.9, -3.4, 2.1 }; 
        final PolynomialFunction f = new PolynomialFunction(coeff);

        
        for (int i = 0; i < 100; i++) {
            final double x = rng.sample();
            fitter.addObservedPoint(x, f.value(x));
        }

        
        final double[] best = fitter.fit(new double[] { -1e-20, 3e15, -5e25 });

        TestUtils.assertEquals("best != coeff", coeff, best, 1e-12);
    }

// org.apache.commons.math3.optimization.fitting.PolynomialFitterTest::testNoError
    public void testNoError() {
        Random randomizer = new Random(64925784252l);
        for (int degree = 1; degree < 10; ++degree) {
            PolynomialFunction p = buildRandomPolynomial(degree, randomizer);

            PolynomialFitter fitter = new PolynomialFitter(new LevenbergMarquardtOptimizer());
            for (int i = 0; i <= degree; ++i) {
                fitter.addObservedPoint(1.0, i, p.value(i));
            }

            final double[] init = new double[degree + 1];
            PolynomialFunction fitted = new PolynomialFunction(fitter.fit(init));

            for (double x = -1.0; x < 1.0; x += 0.01) {
                double error = FastMath.abs(p.value(x) - fitted.value(x)) /
                               (1.0 + FastMath.abs(p.value(x)));
                Assert.assertEquals(0.0, error, 1.0e-6);
            }
        }
    }

// org.apache.commons.math3.optimization.fitting.PolynomialFitterTest::testSmallError
    public void testSmallError() {
        Random randomizer = new Random(53882150042l);
        double maxError = 0;
        for (int degree = 0; degree < 10; ++degree) {
            PolynomialFunction p = buildRandomPolynomial(degree, randomizer);

            PolynomialFitter fitter = new PolynomialFitter(new LevenbergMarquardtOptimizer());
            for (double x = -1.0; x < 1.0; x += 0.01) {
                fitter.addObservedPoint(1.0, x,
                                        p.value(x) + 0.1 * randomizer.nextGaussian());
            }

            final double[] init = new double[degree + 1];
            PolynomialFunction fitted = new PolynomialFunction(fitter.fit(init));

            for (double x = -1.0; x < 1.0; x += 0.01) {
                double error = FastMath.abs(p.value(x) - fitted.value(x)) /
                              (1.0 + FastMath.abs(p.value(x)));
                maxError = FastMath.max(maxError, error);
                Assert.assertTrue(FastMath.abs(error) < 0.1);
            }
        }
        Assert.assertTrue(maxError > 0.01);
    }

// org.apache.commons.math3.optimization.fitting.PolynomialFitterTest::testMath798
    public void testMath798() {
        final double tol = 1e-14;
        final SimpleVectorValueChecker checker = new SimpleVectorValueChecker(tol, tol);
        final double[] init = new double[] { 0, 0 };
        final int maxEval = 3;

        final double[] lm = doMath798(new LevenbergMarquardtOptimizer(checker), maxEval, init);
        final double[] gn = doMath798(new GaussNewtonOptimizer(checker), maxEval, init);

        for (int i = 0; i <= 1; i++) {
            Assert.assertEquals(lm[i], gn[i], tol);
        }
    }

// org.apache.commons.math3.optimization.fitting.PolynomialFitterTest::testMath798WithToleranceTooLow
    public void testMath798WithToleranceTooLow() {
        final double tol = 1e-100;
        final SimpleVectorValueChecker checker = new SimpleVectorValueChecker(tol, tol);
        final double[] init = new double[] { 0, 0 };
        final int maxEval = 10000; 

        final double[] gn = doMath798(new GaussNewtonOptimizer(checker), maxEval, init);
    }

// org.apache.commons.math3.optimization.fitting.PolynomialFitterTest::testMath798WithToleranceTooLowButNoException
    public void testMath798WithToleranceTooLowButNoException() {
        final double tol = 1e-100;
        final double[] init = new double[] { 0, 0 };
        final int maxEval = 10000; 
        final SimpleVectorValueChecker checker = new SimpleVectorValueChecker(tol, tol, maxEval);

        final double[] lm = doMath798(new LevenbergMarquardtOptimizer(checker), maxEval, init);
        final double[] gn = doMath798(new GaussNewtonOptimizer(checker), maxEval, init);

        for (int i = 0; i <= 1; i++) {
            Assert.assertEquals(lm[i], gn[i], 1e-15);
        }
    }

// org.apache.commons.math3.optimization.fitting.PolynomialFitterTest::testRedundantSolvable
    public void testRedundantSolvable() {
        
        checkUnsolvableProblem(new LevenbergMarquardtOptimizer(), true);
    }

// org.apache.commons.math3.optimization.fitting.PolynomialFitterTest::testRedundantUnsolvable
    public void testRedundantUnsolvable() {
        
        checkUnsolvableProblem(new GaussNewtonOptimizer(true, new SimpleVectorValueChecker(1e-15, 1e-15)), false);
    }

// org.apache.commons.math3.optimization.general.GaussNewtonOptimizerTest::testMoreEstimatedParametersSimple
    public void testMoreEstimatedParametersSimple() {
        
        super.testMoreEstimatedParametersSimple();
    }

// org.apache.commons.math3.optimization.general.GaussNewtonOptimizerTest::testMoreEstimatedParametersUnsorted
    public void testMoreEstimatedParametersUnsorted() {
        
        super.testMoreEstimatedParametersUnsorted();
    }

// org.apache.commons.math3.optimization.general.GaussNewtonOptimizerTest::testMaxEvaluations
    public void testMaxEvaluations() throws Exception {
        CircleVectorial circle = new CircleVectorial();
        circle.addPoint( 30.0,  68.0);
        circle.addPoint( 50.0,  -6.0);
        circle.addPoint(110.0, -20.0);
        circle.addPoint( 35.0,  15.0);
        circle.addPoint( 45.0,  97.0);

        GaussNewtonOptimizer optimizer
            = new GaussNewtonOptimizer(new SimpleVectorValueChecker(1.0e-30, 1.0e-30));

        optimizer.optimize(100, circle, new double[] { 0, 0, 0, 0, 0 },
                           new double[] { 1, 1, 1, 1, 1 },
                           new double[] { 98.680, 47.345 });
    }

// org.apache.commons.math3.optimization.general.GaussNewtonOptimizerTest::testCircleFittingBadInit
    public void testCircleFittingBadInit() {
        
        super.testCircleFittingBadInit();
    }

// org.apache.commons.math3.optimization.general.GaussNewtonOptimizerTest::testHahn1
    public void testHahn1()
        throws IOException {
        
        super.testHahn1();
    }

// org.apache.commons.math3.optimization.general.LevenbergMarquardtOptimizerTest::testNonInvertible
    public void testNonInvertible() {
        
        LinearProblem problem = new LinearProblem(new double[][] {
                {  1, 2, -3 },
                {  2, 1,  3 },
                { -3, 0, -9 }
        }, new double[] { 1, 1, 1 });

        AbstractLeastSquaresOptimizer optimizer = createOptimizer();
        PointVectorValuePair optimum = optimizer.optimize(100, problem, problem.target, new double[] { 1, 1, 1 }, new double[] { 0, 0, 0 });
        Assert.assertTrue(FastMath.sqrt(problem.target.length) * optimizer.getRMS() > 0.6);

        optimizer.computeCovariances(optimum.getPoint(), 1.5e-14);
    }

// org.apache.commons.math3.optimization.general.LevenbergMarquardtOptimizerTest::testControlParameters
    public void testControlParameters() {
        CircleVectorial circle = new CircleVectorial();
        circle.addPoint( 30.0,  68.0);
        circle.addPoint( 50.0,  -6.0);
        circle.addPoint(110.0, -20.0);
        circle.addPoint( 35.0,  15.0);
        circle.addPoint( 45.0,  97.0);
        checkEstimate(circle, 0.1, 10, 1.0e-14, 1.0e-16, 1.0e-10, false);
        checkEstimate(circle, 0.1, 10, 1.0e-15, 1.0e-17, 1.0e-10, true);
        checkEstimate(circle, 0.1,  5, 1.0e-15, 1.0e-16, 1.0e-10, true);
        circle.addPoint(300, -300);
        checkEstimate(circle, 0.1, 20, 1.0e-18, 1.0e-16, 1.0e-10, true);
    }

// org.apache.commons.math3.optimization.general.LevenbergMarquardtOptimizerTest::testMath199
    public void testMath199() {
        try {
            QuadraticProblem problem = new QuadraticProblem();
            problem.addPoint (0, -3.182591015485607);
            problem.addPoint (1, -2.5581184967730577);
            problem.addPoint (2, -2.1488478161387325);
            problem.addPoint (3, -1.9122489313410047);
            problem.addPoint (4, 1.7785661310051026);
            LevenbergMarquardtOptimizer optimizer
                = new LevenbergMarquardtOptimizer(100, 1e-10, 1e-10, 1e-10, 0);
            optimizer.optimize(100, problem,
                               new double[] { 0, 0, 0, 0, 0 },
                               new double[] { 0.0, 4.4e-323, 1.0, 4.4e-323, 0.0 },
                               new double[] { 0, 0, 0 });
            Assert.fail("an exception should have been thrown");
        } catch (ConvergenceException ee) {
            
        }
    }

// org.apache.commons.math3.optimization.general.LevenbergMarquardtOptimizerTest::testBevington
    public void testBevington() {
        final double[][] dataPoints = {
            
            { 15, 30, 45, 60, 75, 90, 105, 120, 135, 150,
              165, 180, 195, 210, 225, 240, 255, 270, 285, 300,
              315, 330, 345, 360, 375, 390, 405, 420, 435, 450,
              465, 480, 495, 510, 525, 540, 555, 570, 585, 600,
              615, 630, 645, 660, 675, 690, 705, 720, 735, 750,
              765, 780, 795, 810, 825, 840, 855, 870, 885, },
            
            { 775, 479, 380, 302, 185, 157, 137, 119, 110, 89,
              74, 61, 66, 68, 48, 54, 51, 46, 55, 29,
              28, 37, 49, 26, 35, 29, 31, 24, 25, 35,
              24, 30, 26, 28, 21, 18, 20, 27, 17, 17,
              14, 17, 24, 11, 22, 17, 12, 10, 13, 16,
              9, 9, 14, 21, 17, 13, 12, 18, 10, },
        };

        final BevingtonProblem problem = new BevingtonProblem();

        final int len = dataPoints[0].length;
        final double[] weights = new double[len];
        for (int i = 0; i < len; i++) {
            problem.addPoint(dataPoints[0][i],
                             dataPoints[1][i]);

            weights[i] = 1 / dataPoints[1][i];
        }

        final LevenbergMarquardtOptimizer optimizer
            = new LevenbergMarquardtOptimizer();

        final PointVectorValuePair optimum
            = optimizer.optimize(100, problem, dataPoints[1], weights,
                               new double[] { 10, 900, 80, 27, 225 });

        final double[] solution = optimum.getPoint();
        final double[] expectedSolution = { 10.4, 958.3, 131.4, 33.9, 205.0 };

        final double[][] covarMatrix = optimizer.computeCovariances(solution, 1e-14);
        final double[][] expectedCovarMatrix = {
            { 3.38, -3.69, 27.98, -2.34, -49.24 },
            { -3.69, 2492.26, 81.89, -69.21, -8.9 },
            { 27.98, 81.89, 468.99, -44.22, -615.44 },
            { -2.34, -69.21, -44.22, 6.39, 53.80 },
            { -49.24, -8.9, -615.44, 53.8, 929.45 }
        };

        final int numParams = expectedSolution.length;

        
        for (int i = 0; i < numParams; i++) {
            final double error = FastMath.sqrt(expectedCovarMatrix[i][i]);
            Assert.assertEquals("Parameter " + i, expectedSolution[i], solution[i], error);
        }

        
        
        for (int i = 0; i < numParams; i++) {
            for (int j = 0; j < numParams; j++) {
                Assert.assertEquals("Covariance matrix [" + i + "][" + j + "]",
                                    expectedCovarMatrix[i][j],
                                    covarMatrix[i][j],
                                    FastMath.abs(0.1 * expectedCovarMatrix[i][j]));
            }
        }
    }

// org.apache.commons.math3.optimization.general.LevenbergMarquardtOptimizerTest::testCircleFitting2
    public void testCircleFitting2() {
        final double xCenter = 123.456;
        final double yCenter = 654.321;
        final double xSigma = 10;
        final double ySigma = 15;
        final double radius = 111.111;
        
        final long seed = 59421061L;
        final RandomCirclePointGenerator factory
            = new RandomCirclePointGenerator(xCenter, yCenter, radius,
                                             xSigma, ySigma,
                                             seed);
        final CircleProblem circle = new CircleProblem(xSigma, ySigma);

        final int numPoints = 10;
        for (Vector2D p : factory.generate(numPoints)) {
            circle.addPoint(p);
            
        }

        
        final double[] init = { 90, 659, 115 };

        final LevenbergMarquardtOptimizer optimizer
            = new LevenbergMarquardtOptimizer();
        final PointVectorValuePair optimum = optimizer.optimize(100, circle,
                                                                circle.target(), circle.weight(),
                                                                init);

        final double[] paramFound = optimum.getPoint();

        
        final double[][] covMatrix = optimizer.computeCovariances(paramFound, 1e-14);
        final double[] asymptoticStandardErrorFound = optimizer.guessParametersErrors();
        final double[] sigmaFound = new double[covMatrix.length];
        for (int i = 0; i < covMatrix.length; i++) {
            sigmaFound[i] = FastMath.sqrt(covMatrix[i][i]);

        }

        

        
        Assert.assertEquals(xCenter, paramFound[0], asymptoticStandardErrorFound[0]);
        Assert.assertEquals(yCenter, paramFound[1], asymptoticStandardErrorFound[1]);
        Assert.assertEquals(radius, paramFound[2], asymptoticStandardErrorFound[2]);
    }

// org.apache.commons.math3.optimization.general.MinpackTest::testMinpackLinearFullRank
  public void testMinpackLinearFullRank() {
    minpackTest(new LinearFullRankFunction(10, 5, 1.0,
                                           5.0, 2.23606797749979), false);
    minpackTest(new LinearFullRankFunction(50, 5, 1.0,
                                           8.06225774829855, 6.70820393249937), false);
  }

// org.apache.commons.math3.optimization.general.MinpackTest::testMinpackLinearRank1
  public void testMinpackLinearRank1() {
    minpackTest(new LinearRank1Function(10, 5, 1.0,
                                        291.521868819476, 1.4638501094228), false);
    minpackTest(new LinearRank1Function(50, 5, 1.0,
                                        3101.60039334535, 3.48263016573496), false);
  }

// org.apache.commons.math3.optimization.general.MinpackTest::testMinpackLinearRank1ZeroColsAndRows
  public void testMinpackLinearRank1ZeroColsAndRows() {
    minpackTest(new LinearRank1ZeroColsAndRowsFunction(10, 5, 1.0), false);
    minpackTest(new LinearRank1ZeroColsAndRowsFunction(50, 5, 1.0), false);
  }

// org.apache.commons.math3.optimization.general.MinpackTest::testMinpackRosenbrok
  public void testMinpackRosenbrok() {
    minpackTest(new RosenbrockFunction(new double[] { -1.2, 1.0 },
                                       FastMath.sqrt(24.2)), false);
    minpackTest(new RosenbrockFunction(new double[] { -12.0, 10.0 },
                                       FastMath.sqrt(1795769.0)), false);
    minpackTest(new RosenbrockFunction(new double[] { -120.0, 100.0 },
                                       11.0 * FastMath.sqrt(169000121.0)), false);
  }

// org.apache.commons.math3.optimization.general.MinpackTest::testMinpackHelicalValley
  public void testMinpackHelicalValley() {
    minpackTest(new HelicalValleyFunction(new double[] { -1.0, 0.0, 0.0 },
                                          50.0), false);
    minpackTest(new HelicalValleyFunction(new double[] { -10.0, 0.0, 0.0 },
                                          102.95630140987), false);
    minpackTest(new HelicalValleyFunction(new double[] { -100.0, 0.0, 0.0},
                                          991.261822123701), false);
  }

// org.apache.commons.math3.optimization.general.MinpackTest::testMinpackPowellSingular
  public void testMinpackPowellSingular() {
    minpackTest(new PowellSingularFunction(new double[] { 3.0, -1.0, 0.0, 1.0 },
                                           14.6628782986152), false);
    minpackTest(new PowellSingularFunction(new double[] { 30.0, -10.0, 0.0, 10.0 },
                                           1270.9838708654), false);
    minpackTest(new PowellSingularFunction(new double[] { 300.0, -100.0, 0.0, 100.0 },
                                           126887.903284750), false);
  }

// org.apache.commons.math3.optimization.general.MinpackTest::testMinpackFreudensteinRoth
  public void testMinpackFreudensteinRoth() {
    minpackTest(new FreudensteinRothFunction(new double[] { 0.5, -2.0 },
                                             20.0124960961895, 6.99887517584575,
                                             new double[] {
                                               11.4124844654993,
                                               -0.896827913731509
                                             }), false);
    minpackTest(new FreudensteinRothFunction(new double[] { 5.0, -20.0 },
                                             12432.833948863, 6.9988751744895,
                                             new double[] {
                                                11.41300466147456,
                                                -0.896796038685959
                                             }), false);
    minpackTest(new FreudensteinRothFunction(new double[] { 50.0, -200.0 },
                                             11426454.595762, 6.99887517242903,
                                             new double[] {
                                                 11.412781785788564,
                                                 -0.8968051074920405
                                             }), false);
  }

// org.apache.commons.math3.optimization.general.MinpackTest::testMinpackBard
  public void testMinpackBard() {
    minpackTest(new BardFunction(1.0, 6.45613629515967, 0.0906359603390466,
                                 new double[] {
                                   0.0824105765758334,
                                   1.1330366534715,
                                   2.34369463894115
                                 }), false);
    minpackTest(new BardFunction(10.0, 36.1418531596785, 4.17476870138539,
                                 new double[] {
                                   0.840666673818329,
                                   -158848033.259565,
                                   -164378671.653535
                                 }), false);
    minpackTest(new BardFunction(100.0, 384.114678637399, 4.17476870135969,
                                 new double[] {
                                   0.840666673867645,
                                   -158946167.205518,
                                   -164464906.857771
                                 }), false);
  }

// org.apache.commons.math3.optimization.general.MinpackTest::testMinpackKowalikOsborne
  public void testMinpackKowalikOsborne() {
    minpackTest(new KowalikOsborneFunction(new double[] { 0.25, 0.39, 0.415, 0.39 },
                                           0.0728915102882945,
                                           0.017535837721129,
                                           new double[] {
                                             0.192807810476249,
                                             0.191262653354071,
                                             0.123052801046931,
                                             0.136053221150517
                                           }), false);
    minpackTest(new KowalikOsborneFunction(new double[] { 2.5, 3.9, 4.15, 3.9 },
                                           2.97937007555202,
                                           0.032052192917937,
                                           new double[] {
                                             728675.473768287,
                                             -14.0758803129393,
                                             -32977797.7841797,
                                             -20571594.1977912
                                           }), false);
    minpackTest(new KowalikOsborneFunction(new double[] { 25.0, 39.0, 41.5, 39.0 },
                                           29.9590617016037,
                                           0.0175364017658228,
                                           new double[] {
                                             0.192948328597594,
                                             0.188053165007911,
                                             0.122430604321144,
                                             0.134575665392506
                                           }), false);
  }

// org.apache.commons.math3.optimization.general.MinpackTest::testMinpackMeyer
  public void testMinpackMeyer() {
    minpackTest(new MeyerFunction(new double[] { 0.02, 4000.0, 250.0 },
                                  41153.4665543031, 9.37794514651874,
                                  new double[] {
                                    0.00560963647102661,
                                    6181.34634628659,
                                    345.223634624144
                                  }), false);
    minpackTest(new MeyerFunction(new double[] { 0.2, 40000.0, 2500.0 },
                                  4168216.89130846, 792.917871779501,
                                  new double[] {
                                    1.42367074157994e-11,
                                    33695.7133432541,
                                    901.268527953801
                                  }), true);
  }

// org.apache.commons.math3.optimization.general.MinpackTest::testMinpackWatson
  public void testMinpackWatson() {

    minpackTest(new WatsonFunction(6, 0.0,
                                   5.47722557505166, 0.0478295939097601,
                                   new double[] {
                                     -0.0157249615083782, 1.01243488232965,
                                     -0.232991722387673,  1.26043101102818,
                                     -1.51373031394421,   0.99299727291842
                                   }), false);
    minpackTest(new WatsonFunction(6, 10.0,
                                   6433.12578950026, 0.0478295939096951,
                                   new double[] {
                                     -0.0157251901386677, 1.01243485860105,
                                     -0.232991545843829,  1.26042932089163,
                                     -1.51372776706575,   0.99299573426328
                                   }), false);
    minpackTest(new WatsonFunction(6, 100.0,
                                   674256.040605213, 0.047829593911544,
                                   new double[] {
                                    -0.0157247019712586, 1.01243490925658,
                                    -0.232991922761641,  1.26043292929555,
                                    -1.51373320452707,   0.99299901922322
                                   }), false);

    minpackTest(new WatsonFunction(9, 0.0,
                                   5.47722557505166, 0.00118311459212420,
                                   new double[] {
                                    -0.153070644166722e-4, 0.999789703934597,
                                     0.0147639634910978,   0.146342330145992,
                                     1.00082109454817,    -2.61773112070507,
                                     4.10440313943354,    -3.14361226236241,
                                     1.05262640378759
                                   }), false);
    minpackTest(new WatsonFunction(9, 10.0,
                                   12088.127069307, 0.00118311459212513,
                                   new double[] {
                                   -0.153071334849279e-4, 0.999789703941234,
                                    0.0147639629786217,   0.146342334818836,
                                    1.00082107321386,    -2.61773107084722,
                                    4.10440307655564,    -3.14361222178686,
                                    1.05262639322589
                                   }), false);
    minpackTest(new WatsonFunction(9, 100.0,
                                   1269109.29043834, 0.00118311459212384,
                                   new double[] {
                                    -0.153069523352176e-4, 0.999789703958371,
                                     0.0147639625185392,   0.146342341096326,
                                     1.00082104729164,    -2.61773101573645,
                                     4.10440301427286,    -3.14361218602503,
                                     1.05262638516774
                                   }), false);

    minpackTest(new WatsonFunction(12, 0.0,
                                   5.47722557505166, 0.217310402535861e-4,
                                   new double[] {
                                    -0.660266001396382e-8, 1.00000164411833,
                                    -0.000563932146980154, 0.347820540050756,
                                    -0.156731500244233,    1.05281515825593,
                                    -3.24727109519451,     7.2884347837505,
                                   -10.271848098614,       9.07411353715783,
                                    -4.54137541918194,     1.01201187975044
                                   }), false);
    minpackTest(new WatsonFunction(12, 10.0,
                                   19220.7589790951, 0.217310402518509e-4,
                                   new double[] {
                                    -0.663710223017410e-8, 1.00000164411787,
                                    -0.000563932208347327, 0.347820540486998,
                                    -0.156731503955652,    1.05281517654573,
                                    -3.2472711515214,      7.28843489430665,
                                   -10.2718482369638,      9.07411364383733,
                                    -4.54137546533666,     1.01201188830857
                                   }), false);
    minpackTest(new WatsonFunction(12, 100.0,
                                   2018918.04462367, 0.217310402539845e-4,
                                   new double[] {
                                    -0.663806046485249e-8, 1.00000164411786,
                                    -0.000563932210324959, 0.347820540503588,
                                    -0.156731504091375,    1.05281517718031,
                                    -3.24727115337025,     7.28843489775302,
                                   -10.2718482410813,      9.07411364688464,
                                    -4.54137546660822,     1.0120118885369
                                   }), false);

  }

// org.apache.commons.math3.optimization.general.MinpackTest::testMinpackBox3Dimensional
  public void testMinpackBox3Dimensional() {
    minpackTest(new Box3DimensionalFunction(10, new double[] { 0.0, 10.0, 20.0 },
                                            32.1115837449572), false);
  }

// org.apache.commons.math3.optimization.general.MinpackTest::testMinpackJennrichSampson
  public void testMinpackJennrichSampson() {
    minpackTest(new JennrichSampsonFunction(10, new double[] { 0.3, 0.4 },
                                            64.5856498144943, 11.1517793413499,
                                            new double[] {
 
                                               0.2578199266368004, 0.25782997676455244
                                            }), false);
  }

// org.apache.commons.math3.optimization.general.MinpackTest::testMinpackBrownDennis
  public void testMinpackBrownDennis() {
    minpackTest(new BrownDennisFunction(20,
                                        new double[] { 25.0, 5.0, -5.0, -1.0 },
                                        2815.43839161816, 292.954288244866,
                                        new double[] {
                                         -11.59125141003, 13.2024883984741,
                                         -0.403574643314272, 0.236736269844604
                                        }), false);
    minpackTest(new BrownDennisFunction(20,
                                        new double[] { 250.0, 50.0, -50.0, -10.0 },
                                        555073.354173069, 292.954270581415,
                                        new double[] {
                                         -11.5959274272203, 13.2041866926242,
                                         -0.403417362841545, 0.236771143410386
                                       }), false);
    minpackTest(new BrownDennisFunction(20,
                                        new double[] { 2500.0, 500.0, -500.0, -100.0 },
                                        61211252.2338581, 292.954306151134,
                                        new double[] {
                                         -11.5902596937374, 13.2020628854665,
                                         -0.403688070279258, 0.236665033746463
                                        }), false);
  }

// org.apache.commons.math3.optimization.general.MinpackTest::testMinpackChebyquad
  public void testMinpackChebyquad() {
    minpackTest(new ChebyquadFunction(1, 8, 1.0,
                                      1.88623796907732, 1.88623796907732,
                                      new double[] { 0.5 }), false);
    minpackTest(new ChebyquadFunction(1, 8, 10.0,
                                      5383344372.34005, 1.88424820499951,
                                      new double[] { 0.9817314924684 }), false);
    minpackTest(new ChebyquadFunction(1, 8, 100.0,
                                      0.118088726698392e19, 1.88424820499347,
                                      new double[] { 0.9817314852934 }), false);
    minpackTest(new ChebyquadFunction(8, 8, 1.0,
                                      0.196513862833975, 0.0593032355046727,
                                      new double[] {
                                        0.0431536648587336, 0.193091637843267,
                                        0.266328593812698,  0.499999334628884,
                                        0.500000665371116,  0.733671406187302,
                                        0.806908362156733,  0.956846335141266
                                      }), false);
    minpackTest(new ChebyquadFunction(9, 9, 1.0,
                                      0.16994993465202, 0.0,
                                      new double[] {
                                        0.0442053461357828, 0.199490672309881,
                                        0.23561910847106,   0.416046907892598,
                                        0.5,                0.583953092107402,
                                        0.764380891528940,  0.800509327690119,
                                        0.955794653864217
                                      }), false);
    minpackTest(new ChebyquadFunction(10, 10, 1.0,
                                      0.183747831178711, 0.0806471004038253,
                                      new double[] {
                                        0.0596202671753563, 0.166708783805937,
                                        0.239171018813509,  0.398885290346268,
                                        0.398883667870681,  0.601116332129320,
                                        0.60111470965373,   0.760828981186491,
                                        0.833291216194063,  0.940379732824644
                                      }), false);
  }

// org.apache.commons.math3.optimization.general.MinpackTest::testMinpackBrownAlmostLinear
  public void testMinpackBrownAlmostLinear() {
    minpackTest(new BrownAlmostLinearFunction(10, 0.5,
                                              16.5302162063499, 0.0,
                                              new double[] {
                                                0.979430303349862, 0.979430303349862,
                                                0.979430303349862, 0.979430303349862,
                                                0.979430303349862, 0.979430303349862,
                                                0.979430303349862, 0.979430303349862,
                                                0.979430303349862, 1.20569696650138
                                              }), false);
    minpackTest(new BrownAlmostLinearFunction(10, 5.0,
                                              9765624.00089211, 0.0,
                                              new double[] {
                                               0.979430303349865, 0.979430303349865,
                                               0.979430303349865, 0.979430303349865,
                                               0.979430303349865, 0.979430303349865,
                                               0.979430303349865, 0.979430303349865,
                                               0.979430303349865, 1.20569696650135
                                              }), false);
    minpackTest(new BrownAlmostLinearFunction(10, 50.0,
                                              0.9765625e17, 0.0,
                                              new double[] {
                                                1.0, 1.0, 1.0, 1.0, 1.0,
                                                1.0, 1.0, 1.0, 1.0, 1.0
                                              }), false);
    minpackTest(new BrownAlmostLinearFunction(30, 0.5,
                                              83.476044467848, 0.0,
                                              new double[] {
                                                0.997754216442807, 0.997754216442807,
                                                0.997754216442807, 0.997754216442807,
                                                0.997754216442807, 0.997754216442807,
                                                0.997754216442807, 0.997754216442807,
                                                0.997754216442807, 0.997754216442807,
                                                0.997754216442807, 0.997754216442807,
                                                0.997754216442807, 0.997754216442807,
                                                0.997754216442807, 0.997754216442807,
                                                0.997754216442807, 0.997754216442807,
                                                0.997754216442807, 0.997754216442807,
                                                0.997754216442807, 0.997754216442807,
                                                0.997754216442807, 0.997754216442807,
                                                0.997754216442807, 0.997754216442807,
                                                0.997754216442807, 0.997754216442807,
                                                0.997754216442807, 1.06737350671578
                                              }), false);
    minpackTest(new BrownAlmostLinearFunction(40, 0.5,
                                              128.026364472323, 0.0,
                                              new double[] {
                                                1.00000000000002, 1.00000000000002,
                                                1.00000000000002, 1.00000000000002,
                                                1.00000000000002, 1.00000000000002,
                                                1.00000000000002, 1.00000000000002,
                                                1.00000000000002, 1.00000000000002,
                                                1.00000000000002, 1.00000000000002,
                                                1.00000000000002, 1.00000000000002,
                                                1.00000000000002, 1.00000000000002,
                                                1.00000000000002, 1.00000000000002,
                                                1.00000000000002, 1.00000000000002,
                                                1.00000000000002, 1.00000000000002,
                                                1.00000000000002, 1.00000000000002,
                                                1.00000000000002, 1.00000000000002,
                                                1.00000000000002, 1.00000000000002,
                                                1.00000000000002, 1.00000000000002,
                                                1.00000000000002, 1.00000000000002,
                                                1.00000000000002, 1.00000000000002,
                                                0.999999999999121
                                              }), false);
    }

// org.apache.commons.math3.optimization.general.MinpackTest::testMinpackOsborne1
  public void testMinpackOsborne1() {
      minpackTest(new Osborne1Function(new double[] { 0.5, 1.5, -1.0, 0.01, 0.02, },
                                       0.937564021037838, 0.00739249260904843,
                                       new double[] {
                                         0.375410049244025, 1.93584654543108,
                                        -1.46468676748716, 0.0128675339110439,
                                         0.0221227011813076
                                       }), false);
    }

// org.apache.commons.math3.optimization.general.MinpackTest::testMinpackOsborne2
  public void testMinpackOsborne2() {

    minpackTest(new Osborne2Function(new double[] {
                                       1.3, 0.65, 0.65, 0.7, 0.6,
                                       3.0, 5.0, 7.0, 2.0, 4.5, 5.5
                                     },
                                     1.44686540984712, 0.20034404483314,
                                     new double[] {
                                       1.30997663810096,  0.43155248076,
                                       0.633661261602859, 0.599428560991695,
                                       0.754179768272449, 0.904300082378518,
                                       1.36579949521007, 4.82373199748107,
                                       2.39868475104871, 4.56887554791452,
                                       5.67534206273052
                                     }), false);
  }

// org.apache.commons.math3.optimization.general.NonLinearConjugateGradientOptimizerTest::testTrivial
    public void testTrivial() {
        LinearProblem problem =
            new LinearProblem(new double[][] { { 2 } }, new double[] { 3 });
        NonLinearConjugateGradientOptimizer optimizer =
            new NonLinearConjugateGradientOptimizer(ConjugateGradientFormula.POLAK_RIBIERE,
                                                    new SimpleValueChecker(1e-6, 1e-6));
        PointValuePair optimum =
            optimizer.optimize(100, problem, GoalType.MINIMIZE, new double[] { 0 });
        Assert.assertEquals(1.5, optimum.getPoint()[0], 1.0e-10);
        Assert.assertEquals(0.0, optimum.getValue(), 1.0e-10);
    }

// org.apache.commons.math3.optimization.general.NonLinearConjugateGradientOptimizerTest::testColumnsPermutation
    public void testColumnsPermutation() {
        LinearProblem problem =
            new LinearProblem(new double[][] { { 1.0, -1.0 }, { 0.0, 2.0 }, { 1.0, -2.0 } },
                              new double[] { 4.0, 6.0, 1.0 });

        NonLinearConjugateGradientOptimizer optimizer =
            new NonLinearConjugateGradientOptimizer(ConjugateGradientFormula.POLAK_RIBIERE,
                                                    new SimpleValueChecker(1e-6, 1e-6));
        PointValuePair optimum =
            optimizer.optimize(100, problem, GoalType.MINIMIZE, new double[] { 0, 0 });
        Assert.assertEquals(7.0, optimum.getPoint()[0], 1.0e-10);
        Assert.assertEquals(3.0, optimum.getPoint()[1], 1.0e-10);
        Assert.assertEquals(0.0, optimum.getValue(), 1.0e-10);

    }

// org.apache.commons.math3.optimization.general.NonLinearConjugateGradientOptimizerTest::testNoDependency
    public void testNoDependency() {
        LinearProblem problem = new LinearProblem(new double[][] {
                { 2, 0, 0, 0, 0, 0 },
                { 0, 2, 0, 0, 0, 0 },
                { 0, 0, 2, 0, 0, 0 },
                { 0, 0, 0, 2, 0, 0 },
                { 0, 0, 0, 0, 2, 0 },
                { 0, 0, 0, 0, 0, 2 }
        }, new double[] { 0.0, 1.1, 2.2, 3.3, 4.4, 5.5 });
        NonLinearConjugateGradientOptimizer optimizer =
            new NonLinearConjugateGradientOptimizer(ConjugateGradientFormula.POLAK_RIBIERE,
                                                    new SimpleValueChecker(1e-6, 1e-6));
        PointValuePair optimum =
            optimizer.optimize(100, problem, GoalType.MINIMIZE, new double[] { 0, 0, 0, 0, 0, 0 });
        for (int i = 0; i < problem.target.length; ++i) {
            Assert.assertEquals(0.55 * i, optimum.getPoint()[i], 1.0e-10);
        }
    }

// org.apache.commons.math3.optimization.general.NonLinearConjugateGradientOptimizerTest::testOneSet
    public void testOneSet() {
        LinearProblem problem = new LinearProblem(new double[][] {
                {  1,  0, 0 },
                { -1,  1, 0 },
                {  0, -1, 1 }
        }, new double[] { 1, 1, 1});
        NonLinearConjugateGradientOptimizer optimizer =
            new NonLinearConjugateGradientOptimizer(ConjugateGradientFormula.POLAK_RIBIERE,
                                                    new SimpleValueChecker(1e-6, 1e-6));
        PointValuePair optimum =
            optimizer.optimize(100, problem, GoalType.MINIMIZE, new double[] { 0, 0, 0 });
        Assert.assertEquals(1.0, optimum.getPoint()[0], 1.0e-10);
        Assert.assertEquals(2.0, optimum.getPoint()[1], 1.0e-10);
        Assert.assertEquals(3.0, optimum.getPoint()[2], 1.0e-10);

    }

// org.apache.commons.math3.optimization.general.NonLinearConjugateGradientOptimizerTest::testTwoSets
    public void testTwoSets() {
        final double epsilon = 1.0e-7;
        LinearProblem problem = new LinearProblem(new double[][] {
                {  2,  1,   0,  4,       0, 0 },
                { -4, -2,   3, -7,       0, 0 },
                {  4,  1,  -2,  8,       0, 0 },
                {  0, -3, -12, -1,       0, 0 },
                {  0,  0,   0,  0, epsilon, 1 },
                {  0,  0,   0,  0,       1, 1 }
        }, new double[] { 2, -9, 2, 2, 1 + epsilon * epsilon, 2});

        final Preconditioner preconditioner
            = new Preconditioner() {
                    public double[] precondition(double[] point, double[] r) {
                        double[] d = r.clone();
                        d[0] /=  72.0;
                        d[1] /=  30.0;
                        d[2] /= 314.0;
                        d[3] /= 260.0;
                        d[4] /= 2 * (1 + epsilon * epsilon);
                        d[5] /= 4.0;
                        return d;
                    }
                };

        NonLinearConjugateGradientOptimizer optimizer =
            new NonLinearConjugateGradientOptimizer(ConjugateGradientFormula.POLAK_RIBIERE,
                                                    new SimpleValueChecker(1e-13, 1e-13),
                                                    new BrentSolver(),
                                                    preconditioner);
                                                    
        PointValuePair optimum =
            optimizer.optimize(100, problem, GoalType.MINIMIZE, new double[] { 0, 0, 0, 0, 0, 0 });
        Assert.assertEquals( 3.0, optimum.getPoint()[0], 1.0e-10);
        Assert.assertEquals( 4.0, optimum.getPoint()[1], 1.0e-10);
        Assert.assertEquals(-1.0, optimum.getPoint()[2], 1.0e-10);
        Assert.assertEquals(-2.0, optimum.getPoint()[3], 1.0e-10);
        Assert.assertEquals( 1.0 + epsilon, optimum.getPoint()[4], 1.0e-10);
        Assert.assertEquals( 1.0 - epsilon, optimum.getPoint()[5], 1.0e-10);

    }

// org.apache.commons.math3.optimization.general.NonLinearConjugateGradientOptimizerTest::testNonInversible
    public void testNonInversible() {
        LinearProblem problem = new LinearProblem(new double[][] {
                {  1, 2, -3 },
                {  2, 1,  3 },
                { -3, 0, -9 }
        }, new double[] { 1, 1, 1 });
        NonLinearConjugateGradientOptimizer optimizer =
            new NonLinearConjugateGradientOptimizer(ConjugateGradientFormula.POLAK_RIBIERE,
                                                    new SimpleValueChecker(1e-6, 1e-6));
        PointValuePair optimum =
                optimizer.optimize(100, problem, GoalType.MINIMIZE, new double[] { 0, 0, 0 });
        Assert.assertTrue(optimum.getValue() > 0.5);
    }

// org.apache.commons.math3.optimization.general.NonLinearConjugateGradientOptimizerTest::testIllConditioned
    public void testIllConditioned() {
        LinearProblem problem1 = new LinearProblem(new double[][] {
                { 10.0, 7.0,  8.0,  7.0 },
                {  7.0, 5.0,  6.0,  5.0 },
                {  8.0, 6.0, 10.0,  9.0 },
                {  7.0, 5.0,  9.0, 10.0 }
        }, new double[] { 32, 23, 33, 31 });
        NonLinearConjugateGradientOptimizer optimizer =
            new NonLinearConjugateGradientOptimizer(ConjugateGradientFormula.POLAK_RIBIERE,
                                                    new SimpleValueChecker(1e-13, 1e-13),
                                                    new BrentSolver(1e-15, 1e-15));
        PointValuePair optimum1 =
            optimizer.optimize(200, problem1, GoalType.MINIMIZE, new double[] { 0, 1, 2, 3 });
        Assert.assertEquals(1.0, optimum1.getPoint()[0], 1.0e-4);
        Assert.assertEquals(1.0, optimum1.getPoint()[1], 1.0e-4);
        Assert.assertEquals(1.0, optimum1.getPoint()[2], 1.0e-4);
        Assert.assertEquals(1.0, optimum1.getPoint()[3], 1.0e-4);

        LinearProblem problem2 = new LinearProblem(new double[][] {
                { 10.00, 7.00, 8.10, 7.20 },
                {  7.08, 5.04, 6.00, 5.00 },
                {  8.00, 5.98, 9.89, 9.00 },
                {  6.99, 4.99, 9.00, 9.98 }
        }, new double[] { 32, 23, 33, 31 });
        PointValuePair optimum2 =
            optimizer.optimize(200, problem2, GoalType.MINIMIZE, new double[] { 0, 1, 2, 3 });
        Assert.assertEquals(-81.0, optimum2.getPoint()[0], 1.0e-1);
        Assert.assertEquals(137.0, optimum2.getPoint()[1], 1.0e-1);
        Assert.assertEquals(-34.0, optimum2.getPoint()[2], 1.0e-1);
        Assert.assertEquals( 22.0, optimum2.getPoint()[3], 1.0e-1);

    }

// org.apache.commons.math3.optimization.general.NonLinearConjugateGradientOptimizerTest::testMoreEstimatedParametersSimple
    public void testMoreEstimatedParametersSimple() {
        LinearProblem problem = new LinearProblem(new double[][] {
                { 3.0, 2.0,  0.0, 0.0 },
                { 0.0, 1.0, -1.0, 1.0 },
                { 2.0, 0.0,  1.0, 0.0 }
        }, new double[] { 7.0, 3.0, 5.0 });

        NonLinearConjugateGradientOptimizer optimizer =
            new NonLinearConjugateGradientOptimizer(ConjugateGradientFormula.POLAK_RIBIERE,
                                                    new SimpleValueChecker(1e-6, 1e-6));
        PointValuePair optimum =
            optimizer.optimize(100, problem, GoalType.MINIMIZE, new double[] { 7, 6, 5, 4 });
        Assert.assertEquals(0, optimum.getValue(), 1.0e-10);

    }

// org.apache.commons.math3.optimization.general.NonLinearConjugateGradientOptimizerTest::testMoreEstimatedParametersUnsorted
    public void testMoreEstimatedParametersUnsorted() {
        LinearProblem problem = new LinearProblem(new double[][] {
                 { 1.0, 1.0,  0.0,  0.0, 0.0,  0.0 },
                 { 0.0, 0.0,  1.0,  1.0, 1.0,  0.0 },
                 { 0.0, 0.0,  0.0,  0.0, 1.0, -1.0 },
                 { 0.0, 0.0, -1.0,  1.0, 0.0,  1.0 },
                 { 0.0, 0.0,  0.0, -1.0, 1.0,  0.0 }
        }, new double[] { 3.0, 12.0, -1.0, 7.0, 1.0 });
        NonLinearConjugateGradientOptimizer optimizer =
            new NonLinearConjugateGradientOptimizer(ConjugateGradientFormula.POLAK_RIBIERE,
                                                    new SimpleValueChecker(1e-6, 1e-6));
        PointValuePair optimum =
            optimizer.optimize(100, problem, GoalType.MINIMIZE, new double[] { 2, 2, 2, 2, 2, 2 });
        Assert.assertEquals(0, optimum.getValue(), 1.0e-10);
    }

// org.apache.commons.math3.optimization.general.NonLinearConjugateGradientOptimizerTest::testRedundantEquations
    public void testRedundantEquations() {
        LinearProblem problem = new LinearProblem(new double[][] {
                { 1.0,  1.0 },
                { 1.0, -1.0 },
                { 1.0,  3.0 }
        }, new double[] { 3.0, 1.0, 5.0 });

        NonLinearConjugateGradientOptimizer optimizer =
            new NonLinearConjugateGradientOptimizer(ConjugateGradientFormula.POLAK_RIBIERE,
                                                    new SimpleValueChecker(1e-6, 1e-6));
        PointValuePair optimum =
            optimizer.optimize(100, problem, GoalType.MINIMIZE, new double[] { 1, 1 });
        Assert.assertEquals(2.0, optimum.getPoint()[0], 1.0e-8);
        Assert.assertEquals(1.0, optimum.getPoint()[1], 1.0e-8);

    }

// org.apache.commons.math3.optimization.general.NonLinearConjugateGradientOptimizerTest::testInconsistentEquations
    public void testInconsistentEquations() {
        LinearProblem problem = new LinearProblem(new double[][] {
                { 1.0,  1.0 },
                { 1.0, -1.0 },
                { 1.0,  3.0 }
        }, new double[] { 3.0, 1.0, 4.0 });

        NonLinearConjugateGradientOptimizer optimizer =
            new NonLinearConjugateGradientOptimizer(ConjugateGradientFormula.POLAK_RIBIERE,
                                                    new SimpleValueChecker(1e-6, 1e-6));
        PointValuePair optimum =
            optimizer.optimize(100, problem, GoalType.MINIMIZE, new double[] { 1, 1 });
        Assert.assertTrue(optimum.getValue() > 0.1);

    }

// org.apache.commons.math3.optimization.general.NonLinearConjugateGradientOptimizerTest::testCircleFitting
    public void testCircleFitting() {
        CircleScalar circle = new CircleScalar();
        circle.addPoint( 30.0,  68.0);
        circle.addPoint( 50.0,  -6.0);
        circle.addPoint(110.0, -20.0);
        circle.addPoint( 35.0,  15.0);
        circle.addPoint( 45.0,  97.0);
        NonLinearConjugateGradientOptimizer optimizer =
            new NonLinearConjugateGradientOptimizer(ConjugateGradientFormula.POLAK_RIBIERE,
                                                    new SimpleValueChecker(1e-30, 1e-30),
                                                    new BrentSolver(1e-15, 1e-13));
        PointValuePair optimum =
            optimizer.optimize(100, circle, GoalType.MINIMIZE, new double[] { 98.680, 47.345 });
        Vector2D center = new Vector2D(optimum.getPointRef()[0], optimum.getPointRef()[1]);
        Assert.assertEquals(69.960161753, circle.getRadius(center), 1.0e-8);
        Assert.assertEquals(96.075902096, center.getX(), 1.0e-8);
        Assert.assertEquals(48.135167894, center.getY(), 1.0e-8);
    }

// org.apache.commons.math3.optimization.linear.SimplexSolverTest::testMath828
    public void testMath828() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
                new double[] { 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0}, 0.0);
        
        ArrayList <LinearConstraint>constraints = new ArrayList<LinearConstraint>();

        constraints.add(new LinearConstraint(new double[] {0.0, 39.0, 23.0, 96.0, 15.0, 48.0, 9.0, 21.0, 48.0, 36.0, 76.0, 19.0, 88.0, 17.0, 16.0, 36.0,}, Relationship.GEQ, 15.0));
        constraints.add(new LinearConstraint(new double[] {0.0, 59.0, 93.0, 12.0, 29.0, 78.0, 73.0, 87.0, 32.0, 70.0, 68.0, 24.0, 11.0, 26.0, 65.0, 25.0,}, Relationship.GEQ, 29.0));
        constraints.add(new LinearConstraint(new double[] {0.0, 74.0, 5.0, 82.0, 6.0, 97.0, 55.0, 44.0, 52.0, 54.0, 5.0, 93.0, 91.0, 8.0, 20.0, 97.0,}, Relationship.GEQ, 6.0));
        constraints.add(new LinearConstraint(new double[] {8.0, -3.0, -28.0, -72.0, -8.0, -31.0, -31.0, -74.0, -47.0, -59.0, -24.0, -57.0, -56.0, -16.0, -92.0, -59.0,}, Relationship.GEQ, 0.0));
        constraints.add(new LinearConstraint(new double[] {25.0, -7.0, -99.0, -78.0, -25.0, -14.0, -16.0, -89.0, -39.0, -56.0, -53.0, -9.0, -18.0, -26.0, -11.0, -61.0,}, Relationship.GEQ, 0.0));
        constraints.add(new LinearConstraint(new double[] {33.0, -95.0, -15.0, -4.0, -33.0, -3.0, -20.0, -96.0, -27.0, -13.0, -80.0, -24.0, -3.0, -13.0, -57.0, -76.0,}, Relationship.GEQ, 0.0));
        constraints.add(new LinearConstraint(new double[] {7.0, -95.0, -39.0, -93.0, -7.0, -94.0, -94.0, -62.0, -76.0, -26.0, -53.0, -57.0, -31.0, -76.0, -53.0, -52.0,}, Relationship.GEQ, 0.0));
        
        double epsilon = 1e-6;
        PointValuePair solution = new SimplexSolver().optimize(f, constraints, GoalType.MINIMIZE, true);
        Assert.assertEquals(1.0d, solution.getValue(), epsilon);
        Assert.assertTrue(validSolution(solution, constraints, epsilon));
    }

// org.apache.commons.math3.optimization.linear.SimplexSolverTest::testMath828Cycle
    public void testMath828Cycle() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
                new double[] { 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0}, 0.0);
        
        ArrayList <LinearConstraint>constraints = new ArrayList<LinearConstraint>();

        constraints.add(new LinearConstraint(new double[] {0.0, 16.0, 14.0, 69.0, 1.0, 85.0, 52.0, 43.0, 64.0, 97.0, 14.0, 74.0, 89.0, 28.0, 94.0, 58.0, 13.0, 22.0, 21.0, 17.0, 30.0, 25.0, 1.0, 59.0, 91.0, 78.0, 12.0, 74.0, 56.0, 3.0, 88.0,}, Relationship.GEQ, 91.0));
        constraints.add(new LinearConstraint(new double[] {0.0, 60.0, 40.0, 81.0, 71.0, 72.0, 46.0, 45.0, 38.0, 48.0, 40.0, 17.0, 33.0, 85.0, 64.0, 32.0, 84.0, 3.0, 54.0, 44.0, 71.0, 67.0, 90.0, 95.0, 54.0, 99.0, 99.0, 29.0, 52.0, 98.0, 9.0,}, Relationship.GEQ, 54.0));
        constraints.add(new LinearConstraint(new double[] {0.0, 41.0, 12.0, 86.0, 90.0, 61.0, 31.0, 41.0, 23.0, 89.0, 17.0, 74.0, 44.0, 27.0, 16.0, 47.0, 80.0, 32.0, 11.0, 56.0, 68.0, 82.0, 11.0, 62.0, 62.0, 53.0, 39.0, 16.0, 48.0, 1.0, 63.0,}, Relationship.GEQ, 62.0));
        constraints.add(new LinearConstraint(new double[] {83.0, -76.0, -94.0, -19.0, -15.0, -70.0, -72.0, -57.0, -63.0, -65.0, -22.0, -94.0, -22.0, -88.0, -86.0, -89.0, -72.0, -16.0, -80.0, -49.0, -70.0, -93.0, -95.0, -17.0, -83.0, -97.0, -31.0, -47.0, -31.0, -13.0, -23.0,}, Relationship.GEQ, 0.0));
        constraints.add(new LinearConstraint(new double[] {41.0, -96.0, -41.0, -48.0, -70.0, -43.0, -43.0, -43.0, -97.0, -37.0, -85.0, -70.0, -45.0, -67.0, -87.0, -69.0, -94.0, -54.0, -54.0, -92.0, -79.0, -10.0, -35.0, -20.0, -41.0, -41.0, -65.0, -25.0, -12.0, -8.0, -46.0,}, Relationship.GEQ, 0.0));
        constraints.add(new LinearConstraint(new double[] {27.0, -42.0, -65.0, -49.0, -53.0, -42.0, -17.0, -2.0, -61.0, -31.0, -76.0, -47.0, -8.0, -93.0, -86.0, -62.0, -65.0, -63.0, -22.0, -43.0, -27.0, -23.0, -32.0, -74.0, -27.0, -63.0, -47.0, -78.0, -29.0, -95.0, -73.0,}, Relationship.GEQ, 0.0));
        constraints.add(new LinearConstraint(new double[] {15.0, -46.0, -41.0, -83.0, -98.0, -99.0, -21.0, -35.0, -7.0, -14.0, -80.0, -63.0, -18.0, -42.0, -5.0, -34.0, -56.0, -70.0, -16.0, -18.0, -74.0, -61.0, -47.0, -41.0, -15.0, -79.0, -18.0, -47.0, -88.0, -68.0, -55.0,}, Relationship.GEQ, 0.0));
        
        double epsilon = 1e-6;
        PointValuePair solution = new SimplexSolver().optimize(f, constraints, GoalType.MINIMIZE, true);
        Assert.assertEquals(1.0d, solution.getValue(), epsilon);
        Assert.assertTrue(validSolution(solution, constraints, epsilon));        
    }

// org.apache.commons.math3.optimization.linear.SimplexSolverTest::testMath781
    public void testMath781() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 2, 6, 7 }, 0);

        ArrayList<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 2, 1 }, Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { -1, 1, 1 }, Relationship.LEQ, -1));
        constraints.add(new LinearConstraint(new double[] { 2, -3, 1 }, Relationship.LEQ, -1));

        double epsilon = 1e-6;
        SimplexSolver solver = new SimplexSolver();
        PointValuePair solution = solver.optimize(f, constraints, GoalType.MAXIMIZE, false);

        Assert.assertTrue(Precision.compareTo(solution.getPoint()[0], 0.0d, epsilon) > 0);
        Assert.assertTrue(Precision.compareTo(solution.getPoint()[1], 0.0d, epsilon) > 0);
        Assert.assertTrue(Precision.compareTo(solution.getPoint()[2], 0.0d, epsilon) < 0);
        Assert.assertEquals(2.0d, solution.getValue(), epsilon);
    }

// org.apache.commons.math3.optimization.linear.SimplexSolverTest::testMath713NegativeVariable
    public void testMath713NegativeVariable() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] {1.0, 1.0}, 0.0d);
        ArrayList<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] {1, 0}, Relationship.EQ, 1));

        double epsilon = 1e-6;
        SimplexSolver solver = new SimplexSolver();
        PointValuePair solution = solver.optimize(f, constraints, GoalType.MINIMIZE, true);

        Assert.assertTrue(Precision.compareTo(solution.getPoint()[0], 0.0d, epsilon) >= 0);
        Assert.assertTrue(Precision.compareTo(solution.getPoint()[1], 0.0d, epsilon) >= 0);
    }

// org.apache.commons.math3.optimization.linear.SimplexSolverTest::testMath434NegativeVariable
    public void testMath434NegativeVariable() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] {0.0, 0.0, 1.0}, 0.0d);
        ArrayList<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] {1, 1, 0}, Relationship.EQ, 5));
        constraints.add(new LinearConstraint(new double[] {0, 0, 1}, Relationship.GEQ, -10));

        double epsilon = 1e-6;
        SimplexSolver solver = new SimplexSolver();
        PointValuePair solution = solver.optimize(f, constraints, GoalType.MINIMIZE, false);

        Assert.assertEquals(5.0, solution.getPoint()[0] + solution.getPoint()[1], epsilon);
        Assert.assertEquals(-10.0, solution.getPoint()[2], epsilon);
        Assert.assertEquals(-10.0, solution.getValue(), epsilon);

    }

// org.apache.commons.math3.optimization.linear.SimplexSolverTest::testMath434UnfeasibleSolution
    public void testMath434UnfeasibleSolution() {
        double epsilon = 1e-6;

        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] {1.0, 0.0}, 0.0);
        ArrayList<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] {epsilon/2, 0.5}, Relationship.EQ, 0));
        constraints.add(new LinearConstraint(new double[] {1e-3, 0.1}, Relationship.EQ, 10));

        SimplexSolver solver = new SimplexSolver();
        
        solver.optimize(f, constraints, GoalType.MINIMIZE, true);
    }

// org.apache.commons.math3.optimization.linear.SimplexSolverTest::testMath434PivotRowSelection
    public void testMath434PivotRowSelection() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] {1.0}, 0.0);

        double epsilon = 1e-6;
        ArrayList<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] {200}, Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] {100}, Relationship.GEQ, 0.499900001));

        SimplexSolver solver = new SimplexSolver();
        PointValuePair solution = solver.optimize(f, constraints, GoalType.MINIMIZE, false);
        
        Assert.assertTrue(Precision.compareTo(solution.getPoint()[0] * 200.d, 1.d, epsilon) >= 0);
        Assert.assertEquals(0.0050, solution.getValue(), epsilon);
    }

// org.apache.commons.math3.optimization.linear.SimplexSolverTest::testMath434PivotRowSelection2
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
        PointValuePair solution = simplex.optimize(f, constraints, GoalType.MINIMIZE, false);
        
        Assert.assertTrue(Precision.compareTo(solution.getPoint()[0], -1e-18d, epsilon) >= 0);
        Assert.assertEquals(1.0d, solution.getPoint()[1], epsilon);        
        Assert.assertEquals(0.0d, solution.getPoint()[2], epsilon);
        Assert.assertEquals(1.0d, solution.getValue(), epsilon);
    }

// org.apache.commons.math3.optimization.linear.SimplexSolverTest::testMath272
    public void testMath272() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 2, 2, 1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 1, 0 }, Relationship.GEQ,  1));
        constraints.add(new LinearConstraint(new double[] { 1, 0, 1 }, Relationship.GEQ,  1));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 }, Relationship.GEQ,  1));

        SimplexSolver solver = new SimplexSolver();
        PointValuePair solution = solver.optimize(f, constraints, GoalType.MINIMIZE, true);

        Assert.assertEquals(0.0, solution.getPoint()[0], .0000001);
        Assert.assertEquals(1.0, solution.getPoint()[1], .0000001);
        Assert.assertEquals(1.0, solution.getPoint()[2], .0000001);
        Assert.assertEquals(3.0, solution.getValue(), .0000001);
    }

// org.apache.commons.math3.optimization.linear.SimplexSolverTest::testMath286
    public void testMath286() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 0.8, 0.2, 0.7, 0.3, 0.6, 0.4 }, 0 );
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 1, 0, 1, 0 }, Relationship.EQ, 23.0));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0, 1, 0, 1 }, Relationship.EQ, 23.0));
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0, 0, 0, 0 }, Relationship.GEQ, 10.0));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1, 0, 0, 0 }, Relationship.GEQ, 8.0));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 0, 0, 1, 0 }, Relationship.GEQ, 5.0));

        SimplexSolver solver = new SimplexSolver();
        PointValuePair solution = solver.optimize(f, constraints, GoalType.MAXIMIZE, true);

        Assert.assertEquals(25.8, solution.getValue(), .0000001);
        Assert.assertEquals(23.0, solution.getPoint()[0] + solution.getPoint()[2] + solution.getPoint()[4], 0.0000001);
        Assert.assertEquals(23.0, solution.getPoint()[1] + solution.getPoint()[3] + solution.getPoint()[5], 0.0000001);
        Assert.assertTrue(solution.getPoint()[0] >= 10.0 - 0.0000001);
        Assert.assertTrue(solution.getPoint()[2] >= 8.0 - 0.0000001);
        Assert.assertTrue(solution.getPoint()[4] >= 5.0 - 0.0000001);
    }

// org.apache.commons.math3.optimization.linear.SimplexSolverTest::testDegeneracy
    public void testDegeneracy() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 0.8, 0.7 }, 0 );
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 1 }, Relationship.LEQ, 18.0));
        constraints.add(new LinearConstraint(new double[] { 1, 0 }, Relationship.GEQ, 10.0));
        constraints.add(new LinearConstraint(new double[] { 0, 1 }, Relationship.GEQ, 8.0));

        SimplexSolver solver = new SimplexSolver();
        PointValuePair solution = solver.optimize(f, constraints, GoalType.MAXIMIZE, true);
        Assert.assertEquals(13.6, solution.getValue(), .0000001);
    }

// org.apache.commons.math3.optimization.linear.SimplexSolverTest::testMath288
    public void testMath288() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 7, 3, 0, 0 }, 0 );
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 3, 0, -5, 0 }, Relationship.LEQ, 0.0));
        constraints.add(new LinearConstraint(new double[] { 2, 0, 0, -5 }, Relationship.LEQ, 0.0));
        constraints.add(new LinearConstraint(new double[] { 0, 3, 0, -5 }, Relationship.LEQ, 0.0));
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0, 0 }, Relationship.LEQ, 1.0));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0, 0 }, Relationship.LEQ, 1.0));

        SimplexSolver solver = new SimplexSolver();
        PointValuePair solution = solver.optimize(f, constraints, GoalType.MAXIMIZE, true);
        Assert.assertEquals(10.0, solution.getValue(), .0000001);
    }

// org.apache.commons.math3.optimization.linear.SimplexSolverTest::testMath290GEQ
    public void testMath290GEQ() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1, 5 }, 0 );
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 2, 0 }, Relationship.GEQ, -1.0));
        SimplexSolver solver = new SimplexSolver();
        PointValuePair solution = solver.optimize(f, constraints, GoalType.MINIMIZE, true);
        Assert.assertEquals(0, solution.getValue(), .0000001);
        Assert.assertEquals(0, solution.getPoint()[0], .0000001);
        Assert.assertEquals(0, solution.getPoint()[1], .0000001);
    }

// org.apache.commons.math3.optimization.linear.SimplexSolverTest::testMath290LEQ
    public void testMath290LEQ() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1, 5 }, 0 );
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 2, 0 }, Relationship.LEQ, -1.0));
        SimplexSolver solver = new SimplexSolver();
        solver.optimize(f, constraints, GoalType.MINIMIZE, true);
    }

// org.apache.commons.math3.optimization.linear.SimplexSolverTest::testMath293
    public void testMath293() {
      LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 0.8, 0.2, 0.7, 0.3, 0.4, 0.6}, 0 );
      Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
      constraints.add(new LinearConstraint(new double[] { 1, 0, 1, 0, 1, 0 }, Relationship.EQ, 30.0));
      constraints.add(new LinearConstraint(new double[] { 0, 1, 0, 1, 0, 1 }, Relationship.EQ, 30.0));
      constraints.add(new LinearConstraint(new double[] { 0.8, 0.2, 0.0, 0.0, 0.0, 0.0 }, Relationship.GEQ, 10.0));
      constraints.add(new LinearConstraint(new double[] { 0.0, 0.0, 0.7, 0.3, 0.0, 0.0 }, Relationship.GEQ, 10.0));
      constraints.add(new LinearConstraint(new double[] { 0.0, 0.0, 0.0, 0.0, 0.4, 0.6 }, Relationship.GEQ, 10.0));

      SimplexSolver solver = new SimplexSolver();
      PointValuePair solution1 = solver.optimize(f, constraints, GoalType.MAXIMIZE, true);

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

      PointValuePair solution2 = solver.optimize(f, constraints, GoalType.MAXIMIZE, true);
      Assert.assertEquals(40.57143, solution2.getValue(), .0001);
    }

// org.apache.commons.math3.optimization.linear.SimplexSolverTest::testSimplexSolver
    public void testSimplexSolver() {
        LinearObjectiveFunction f =
            new LinearObjectiveFunction(new double[] { 15, 10 }, 7);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0 }, Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1 }, Relationship.LEQ, 3));
        constraints.add(new LinearConstraint(new double[] { 1, 1 }, Relationship.EQ, 4));

        SimplexSolver solver = new SimplexSolver();
        PointValuePair solution = solver.optimize(f, constraints, GoalType.MAXIMIZE, false);
        Assert.assertEquals(2.0, solution.getPoint()[0], 0.0);
        Assert.assertEquals(2.0, solution.getPoint()[1], 0.0);
        Assert.assertEquals(57.0, solution.getValue(), 0.0);
    }

// org.apache.commons.math3.optimization.linear.SimplexSolverTest::testSingleVariableAndConstraint
    public void testSingleVariableAndConstraint() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 3 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1 }, Relationship.LEQ, 10));

        SimplexSolver solver = new SimplexSolver();
        PointValuePair solution = solver.optimize(f, constraints, GoalType.MAXIMIZE, false);
        Assert.assertEquals(10.0, solution.getPoint()[0], 0.0);
        Assert.assertEquals(30.0, solution.getValue(), 0.0);
    }

// org.apache.commons.math3.optimization.linear.SimplexSolverTest::testModelWithNoArtificialVars
    public void testModelWithNoArtificialVars() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 15, 10 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0 }, Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1 }, Relationship.LEQ, 3));
        constraints.add(new LinearConstraint(new double[] { 1, 1 }, Relationship.LEQ, 4));

        SimplexSolver solver = new SimplexSolver();
        PointValuePair solution = solver.optimize(f, constraints, GoalType.MAXIMIZE, false);
        Assert.assertEquals(2.0, solution.getPoint()[0], 0.0);
        Assert.assertEquals(2.0, solution.getPoint()[1], 0.0);
        Assert.assertEquals(50.0, solution.getValue(), 0.0);
    }

// org.apache.commons.math3.optimization.linear.SimplexSolverTest::testMinimization
    public void testMinimization() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { -2, 1 }, -5);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 2 }, Relationship.LEQ, 6));
        constraints.add(new LinearConstraint(new double[] { 3, 2 }, Relationship.LEQ, 12));
        constraints.add(new LinearConstraint(new double[] { 0, 1 }, Relationship.GEQ, 0));

        SimplexSolver solver = new SimplexSolver();
        PointValuePair solution = solver.optimize(f, constraints, GoalType.MINIMIZE, false);
        Assert.assertEquals(4.0, solution.getPoint()[0], 0.0);
        Assert.assertEquals(0.0, solution.getPoint()[1], 0.0);
        Assert.assertEquals(-13.0, solution.getValue(), 0.0);
    }

// org.apache.commons.math3.optimization.linear.SimplexSolverTest::testSolutionWithNegativeDecisionVariable
    public void testSolutionWithNegativeDecisionVariable() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { -2, 1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 1 }, Relationship.GEQ, 6));
        constraints.add(new LinearConstraint(new double[] { 1, 2 }, Relationship.LEQ, 14));

        SimplexSolver solver = new SimplexSolver();
        PointValuePair solution = solver.optimize(f, constraints, GoalType.MAXIMIZE, false);
        Assert.assertEquals(-2.0, solution.getPoint()[0], 0.0);
        Assert.assertEquals(8.0, solution.getPoint()[1], 0.0);
        Assert.assertEquals(12.0, solution.getValue(), 0.0);
    }

// org.apache.commons.math3.optimization.linear.SimplexSolverTest::testInfeasibleSolution
    public void testInfeasibleSolution() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 15 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1 }, Relationship.LEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 1 }, Relationship.GEQ, 3));

        SimplexSolver solver = new SimplexSolver();
        solver.optimize(f, constraints, GoalType.MAXIMIZE, false);
    }

// org.apache.commons.math3.optimization.linear.SimplexSolverTest::testUnboundedSolution
    public void testUnboundedSolution() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 15, 10 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0 }, Relationship.EQ, 2));

        SimplexSolver solver = new SimplexSolver();
        solver.optimize(f, constraints, GoalType.MAXIMIZE, false);
    }

// org.apache.commons.math3.optimization.linear.SimplexSolverTest::testRestrictVariablesToNonNegative
    public void testRestrictVariablesToNonNegative() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 409, 523, 70, 204, 339 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] {    43,   56, 345,  56,    5 }, Relationship.LEQ,  4567456));
        constraints.add(new LinearConstraint(new double[] {    12,   45,   7,  56,   23 }, Relationship.LEQ,    56454));
        constraints.add(new LinearConstraint(new double[] {     8,  768,   0,  34, 7456 }, Relationship.LEQ,  1923421));
        constraints.add(new LinearConstraint(new double[] { 12342, 2342,  34, 678, 2342 }, Relationship.GEQ,     4356));
        constraints.add(new LinearConstraint(new double[] {    45,  678,  76,  52,   23 }, Relationship.EQ,    456356));

        SimplexSolver solver = new SimplexSolver();
        PointValuePair solution = solver.optimize(f, constraints, GoalType.MAXIMIZE, true);
        Assert.assertEquals(2902.92783505155, solution.getPoint()[0], .0000001);
        Assert.assertEquals(480.419243986254, solution.getPoint()[1], .0000001);
        Assert.assertEquals(0.0, solution.getPoint()[2], .0000001);
        Assert.assertEquals(0.0, solution.getPoint()[3], .0000001);
        Assert.assertEquals(0.0, solution.getPoint()[4], .0000001);
        Assert.assertEquals(1438556.7491409, solution.getValue(), .0000001);
    }

// org.apache.commons.math3.optimization.linear.SimplexSolverTest::testEpsilon
    public void testEpsilon() {
      LinearObjectiveFunction f =
          new LinearObjectiveFunction(new double[] { 10, 5, 1 }, 0);
      Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
      constraints.add(new LinearConstraint(new double[] {  9, 8, 0 }, Relationship.EQ,  17));
      constraints.add(new LinearConstraint(new double[] {  0, 7, 8 }, Relationship.LEQ,  7));
      constraints.add(new LinearConstraint(new double[] { 10, 0, 2 }, Relationship.LEQ, 10));

      SimplexSolver solver = new SimplexSolver();
      PointValuePair solution = solver.optimize(f, constraints, GoalType.MAXIMIZE, false);
      Assert.assertEquals(1.0, solution.getPoint()[0], 0.0);
      Assert.assertEquals(1.0, solution.getPoint()[1], 0.0);
      Assert.assertEquals(0.0, solution.getPoint()[2], 0.0);
      Assert.assertEquals(15.0, solution.getValue(), 0.0);
  }

// org.apache.commons.math3.optimization.linear.SimplexSolverTest::testTrivialModel
    public void testTrivialModel() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1, 1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 1 }, Relationship.EQ,  0));

        SimplexSolver solver = new SimplexSolver();
        PointValuePair solution = solver.optimize(f, constraints, GoalType.MAXIMIZE, true);
        Assert.assertEquals(0, solution.getValue(), .0000001);
    }

// org.apache.commons.math3.optimization.linear.SimplexSolverTest::testLargeModel
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
        PointValuePair solution = solver.optimize(f, constraints, GoalType.MINIMIZE, true);
        Assert.assertEquals(7518.0, solution.getValue(), .0000001);
    }

// org.apache.commons.math3.optimization.linear.SimplexTableauTest::testInitialization
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

// org.apache.commons.math3.optimization.linear.SimplexTableauTest::testDropPhase1Objective
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

// org.apache.commons.math3.optimization.linear.SimplexTableauTest::testTableauWithNoArtificialVars
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

// org.apache.commons.math3.optimization.linear.SimplexTableauTest::testSerial
    public void testSerial() {
        LinearObjectiveFunction f = createFunction();
        Collection<LinearConstraint> constraints = createConstraints();
        SimplexTableau tableau =
            new SimplexTableau(f, constraints, GoalType.MAXIMIZE, false, 1.0e-6);
        Assert.assertEquals(tableau, TestUtils.serializeAndRecover(tableau));
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

// org.apache.commons.math3.optimization.univariate.SimpleUnivariateValueCheckerTest::testIterationCheckPrecondition
    public void testIterationCheckPrecondition() {
        new SimpleUnivariateValueChecker(1e-1, 1e-2, 0);
    }

// org.apache.commons.math3.optimization.univariate.SimpleUnivariateValueCheckerTest::testIterationCheck
    public void testIterationCheck() {
        final int max = 10;
        final SimpleUnivariateValueChecker checker = new SimpleUnivariateValueChecker(1e-1, 1e-2, max);
        Assert.assertTrue(checker.converged(max, null, null)); 
        Assert.assertTrue(checker.converged(max + 1, null, null));
    }

// org.apache.commons.math3.optimization.univariate.SimpleUnivariateValueCheckerTest::testIterationCheckDisabled
    public void testIterationCheckDisabled() {
        final SimpleUnivariateValueChecker checker = new SimpleUnivariateValueChecker(1e-8, 1e-8);

        final UnivariatePointValuePair a = new UnivariatePointValuePair(1d, 1d);
        final UnivariatePointValuePair b = new UnivariatePointValuePair(10d, 10d);

        Assert.assertFalse(checker.converged(-1, a, b));
        Assert.assertFalse(checker.converged(0, a, b));
        Assert.assertFalse(checker.converged(1000000, a, b));

        Assert.assertTrue(checker.converged(-1, a, a));
        Assert.assertTrue(checker.converged(-1, b, b));
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

// org.apache.commons.math3.special.BetaTest::testRegularizedBetaNanPositivePositive
    public void testRegularizedBetaNanPositivePositive() {
        testRegularizedBeta(Double.NaN, Double.NaN, 1.0, 1.0);
    }

// org.apache.commons.math3.special.BetaTest::testRegularizedBetaPositiveNanPositive
    public void testRegularizedBetaPositiveNanPositive() {
        testRegularizedBeta(Double.NaN, 0.5, Double.NaN, 1.0);
    }

// org.apache.commons.math3.special.BetaTest::testRegularizedBetaPositivePositiveNan
    public void testRegularizedBetaPositivePositiveNan() {
        testRegularizedBeta(Double.NaN, 0.5, 1.0, Double.NaN);
    }

// org.apache.commons.math3.special.BetaTest::testRegularizedBetaNegativePositivePositive
    public void testRegularizedBetaNegativePositivePositive() {
        testRegularizedBeta(Double.NaN, -0.5, 1.0, 2.0);
    }

// org.apache.commons.math3.special.BetaTest::testRegularizedBetaPositiveNegativePositive
    public void testRegularizedBetaPositiveNegativePositive() {
        testRegularizedBeta(Double.NaN, 0.5, -1.0, 2.0);
    }

// org.apache.commons.math3.special.BetaTest::testRegularizedBetaPositivePositiveNegative
    public void testRegularizedBetaPositivePositiveNegative() {
        testRegularizedBeta(Double.NaN, 0.5, 1.0, -2.0);
    }

// org.apache.commons.math3.special.BetaTest::testRegularizedBetaZeroPositivePositive
    public void testRegularizedBetaZeroPositivePositive() {
        testRegularizedBeta(0.0, 0.0, 1.0, 2.0);
    }

// org.apache.commons.math3.special.BetaTest::testRegularizedBetaPositiveZeroPositive
    public void testRegularizedBetaPositiveZeroPositive() {
        testRegularizedBeta(Double.NaN, 0.5, 0.0, 2.0);
    }

// org.apache.commons.math3.special.BetaTest::testRegularizedBetaPositivePositiveZero
    public void testRegularizedBetaPositivePositiveZero() {
        testRegularizedBeta(Double.NaN, 0.5, 1.0, 0.0);
    }

// org.apache.commons.math3.special.BetaTest::testRegularizedBetaPositivePositivePositive
    public void testRegularizedBetaPositivePositivePositive() {
        testRegularizedBeta(0.75, 0.5, 1.0, 2.0);
    }

// org.apache.commons.math3.special.BetaTest::testLogBetaNanPositive
    public void testLogBetaNanPositive() {
        testLogBeta(Double.NaN, Double.NaN, 2.0);
    }

// org.apache.commons.math3.special.BetaTest::testLogBetaPositiveNan
    public void testLogBetaPositiveNan() {
        testLogBeta(Double.NaN, 1.0, Double.NaN);
    }

// org.apache.commons.math3.special.BetaTest::testLogBetaNegativePositive
    public void testLogBetaNegativePositive() {
        testLogBeta(Double.NaN, -1.0, 2.0);
    }

// org.apache.commons.math3.special.BetaTest::testLogBetaPositiveNegative
    public void testLogBetaPositiveNegative() {
        testLogBeta(Double.NaN, 1.0, -2.0);
    }

// org.apache.commons.math3.special.BetaTest::testLogBetaZeroPositive
    public void testLogBetaZeroPositive() {
        testLogBeta(Double.NaN, 0.0, 2.0);
    }

// org.apache.commons.math3.special.BetaTest::testLogBetaPositiveZero
    public void testLogBetaPositiveZero() {
        testLogBeta(Double.NaN, 1.0, 0.0);
    }

// org.apache.commons.math3.special.BetaTest::testLogBetaPositivePositive
    public void testLogBetaPositivePositive() {
        testLogBeta(-0.693147180559945, 1.0, 2.0);
    }

// org.apache.commons.math3.special.BetaTest::testBcorr
    public void testBcorr() {

        final int ulps = 3;
        for (int i = 0; i < BCORR_REF.length; i++) {
            final double[] ref = BCORR_REF[i];
            final double a = ref[0];
            final double b = ref[1];
            final double expected = ref[2];
            final double actual = Beta.bcorr(a, b);
            final double tol = ulps * FastMath.ulp(expected);
            final StringBuilder builder = new StringBuilder();
            builder.append(a).append(", ").append(b);
            Assert.assertEquals(builder.toString(), expected, actual, tol);
        }
    }

// org.apache.commons.math3.special.BetaTest::testBcorrPrecondition1
    public void testBcorrPrecondition1() {

        Beta.bcorr(9.0, 10.0);
    }

// org.apache.commons.math3.special.BetaTest::testBcorrPrecondition2
    public void testBcorrPrecondition2() {

        Beta.bcorr(10.0, 9.0);
    }

// org.apache.commons.math3.special.ErfTest::testErf0
    public void testErf0() {
        double actual = Erf.erf(0.0);
        double expected = 0.0;
        Assert.assertEquals(expected, actual, 1.0e-15);
        Assert.assertEquals(1 - expected, Erf.erfc(0.0), 1.0e-15);
    }

// org.apache.commons.math3.special.ErfTest::testErf1960
    public void testErf1960() {
        double x = 1.960 / FastMath.sqrt(2.0);
        double actual = Erf.erf(x);
        double expected = 0.95;
        Assert.assertEquals(expected, actual, 1.0e-5);
        Assert.assertEquals(1 - actual, Erf.erfc(x), 1.0e-15);

        actual = Erf.erf(-x);
        expected = -expected;
        Assert.assertEquals(expected, actual, 1.0e-5);
        Assert.assertEquals(1 - actual, Erf.erfc(-x), 1.0e-15);
    }

// org.apache.commons.math3.special.ErfTest::testErf2576
    public void testErf2576() {
        double x = 2.576 / FastMath.sqrt(2.0);
        double actual = Erf.erf(x);
        double expected = 0.99;
        Assert.assertEquals(expected, actual, 1.0e-5);
        Assert.assertEquals(1 - actual, Erf.erfc(x), 1e-15);

        actual = Erf.erf(-x);
        expected = -expected;
        Assert.assertEquals(expected, actual, 1.0e-5);
        Assert.assertEquals(1 - actual, Erf.erfc(-x), 1.0e-15);
    }

// org.apache.commons.math3.special.ErfTest::testErf2807
    public void testErf2807() {
        double x = 2.807 / FastMath.sqrt(2.0);
        double actual = Erf.erf(x);
        double expected = 0.995;
        Assert.assertEquals(expected, actual, 1.0e-5);
        Assert.assertEquals(1 - actual, Erf.erfc(x), 1.0e-15);

        actual = Erf.erf(-x);
        expected = -expected;
        Assert.assertEquals(expected, actual, 1.0e-5);
        Assert.assertEquals(1 - actual, Erf.erfc(-x), 1.0e-15);
    }

// org.apache.commons.math3.special.ErfTest::testErf3291
    public void testErf3291() {
        double x = 3.291 / FastMath.sqrt(2.0);
        double actual = Erf.erf(x);
        double expected = 0.999;
        Assert.assertEquals(expected, actual, 1.0e-5);
        Assert.assertEquals(1 - expected, Erf.erfc(x), 1.0e-5);

        actual = Erf.erf(-x);
        expected = -expected;
        Assert.assertEquals(expected, actual, 1.0e-5);
        Assert.assertEquals(1 - expected, Erf.erfc(-x), 1.0e-5);
    }

// org.apache.commons.math3.special.ErfTest::testLargeValues
    public void testLargeValues() {
        for (int i = 1; i < 200; i*=10) {
            double result = Erf.erf(i);
            Assert.assertFalse(Double.isNaN(result));
            Assert.assertTrue(result > 0 && result <= 1);
            result = Erf.erf(-i);
            Assert.assertFalse(Double.isNaN(result));
            Assert.assertTrue(result >= -1 && result < 0);
            result = Erf.erfc(i);
            Assert.assertFalse(Double.isNaN(result));
            Assert.assertTrue(result >= 0 && result < 1);
            result = Erf.erfc(-i);
            Assert.assertFalse(Double.isNaN(result));
            Assert.assertTrue(result >= 1 && result <= 2);    
        }
        Assert.assertEquals(-1, Erf.erf(Double.NEGATIVE_INFINITY), 0);
        Assert.assertEquals(1, Erf.erf(Double.POSITIVE_INFINITY), 0);
        Assert.assertEquals(2, Erf.erfc(Double.NEGATIVE_INFINITY), 0);
        Assert.assertEquals(0, Erf.erfc(Double.POSITIVE_INFINITY), 0);
    }

// org.apache.commons.math3.special.ErfTest::testErfGnu
    public void testErfGnu() {
        final double tol = 1E-15;
        final double[] gnuValues = new double[] {-1, -1, -1, -1, -1, 
        -1, -1, -1, -0.99999999999999997848, 
        -0.99999999999999264217, -0.99999999999846254017, -0.99999999980338395581, -0.99999998458274209971, 
        -0.9999992569016276586, -0.99997790950300141459, -0.99959304798255504108, -0.99532226501895273415, 
        -0.96610514647531072711, -0.84270079294971486948, -0.52049987781304653809,  0, 
         0.52049987781304653809, 0.84270079294971486948, 0.96610514647531072711, 0.99532226501895273415, 
         0.99959304798255504108, 0.99997790950300141459, 0.9999992569016276586, 0.99999998458274209971, 
         0.99999999980338395581, 0.99999999999846254017, 0.99999999999999264217, 0.99999999999999997848, 
         1,  1,  1,  1, 
         1,  1,  1,  1};
        double x = -10d;
        for (int i = 0; i < 41; i++) {
            Assert.assertEquals(gnuValues[i], Erf.erf(x), tol);
            x += 0.5d;
        }
    }

// org.apache.commons.math3.special.ErfTest::testErfcGnu
    public void testErfcGnu() {
        final double tol = 1E-15;
        final double[] gnuValues = new double[] { 2,  2,  2,  2,  2, 
        2,  2,  2, 1.9999999999999999785, 
        1.9999999999999926422, 1.9999999999984625402, 1.9999999998033839558, 1.9999999845827420998, 
        1.9999992569016276586, 1.9999779095030014146, 1.9995930479825550411, 1.9953222650189527342, 
        1.9661051464753107271, 1.8427007929497148695, 1.5204998778130465381,  1, 
        0.47950012218695346194, 0.15729920705028513051, 0.033894853524689272893, 0.0046777349810472658333, 
        0.00040695201744495893941, 2.2090496998585441366E-05, 7.4309837234141274516E-07, 1.5417257900280018858E-08, 
        1.966160441542887477E-10, 1.5374597944280348501E-12, 7.3578479179743980661E-15, 2.1519736712498913103E-17, 
        3.8421483271206474691E-20, 4.1838256077794144006E-23, 2.7766493860305691016E-26, 1.1224297172982927079E-29, 
        2.7623240713337714448E-33, 4.1370317465138102353E-37, 3.7692144856548799402E-41, 2.0884875837625447567E-45};
        double x = -10d;
        for (int i = 0; i < 41; i++) {
            Assert.assertEquals(gnuValues[i], Erf.erfc(x), tol);
            x += 0.5d;
        }
    }

// org.apache.commons.math3.special.ErfTest::testErfcMaple
    public void testErfcMaple() {
        double[][] ref = new double[][]
                        {{0.1, 4.60172162722971e-01},
                         {1.2, 1.15069670221708e-01},
                         {2.3, 1.07241100216758e-02},
                         {3.4, 3.36929265676881e-04},
                         {4.5, 3.39767312473006e-06},
                         {5.6, 1.07175902583109e-08}, 
                         {6.7, 1.04209769879652e-11},
                         {7.8, 3.09535877195870e-15},
                         {8.9, 2.79233437493966e-19},
                         {10.0, 7.61985302416053e-24},
                         {11.1, 6.27219439321703e-29},
                         {12.2, 1.55411978638959e-34}, 
                         {13.3, 1.15734162836904e-40},
                         {14.4, 2.58717592540226e-47},
                         {15.5, 1.73446079179387e-54},
                         {16.6, 3.48454651995041e-62}
        };
        for (int i = 0; i < 15; i++) {
            final double result = 0.5*Erf.erfc(ref[i][0]/Math.sqrt(2));
            Assert.assertEquals(ref[i][1], result, 1E-15);
            TestUtils.assertRelativelyEquals(ref[i][1], result, 1E-13);
        }
    }

// org.apache.commons.math3.special.ErfTest::testTwoArgumentErf
    public void testTwoArgumentErf() {
        double[] xi = new double[]{-2.0, -1.0, -0.9, -0.1, 0.0, 0.1, 0.9, 1.0, 2.0};
        for(double x1 : xi) {
            for(double x2 : xi) {
                double a = Erf.erf(x1, x2);
                double b = Erf.erf(x2) - Erf.erf(x1);
                double c = Erf.erfc(x1) - Erf.erfc(x2);
                Assert.assertEquals(a, b, 1E-15);
                Assert.assertEquals(a, c, 1E-15);
            }
        }
    }

// org.apache.commons.math3.special.GammaTest::testRegularizedGammaNanPositive
    public void testRegularizedGammaNanPositive() {
        testRegularizedGamma(Double.NaN, Double.NaN, 1.0);
    }

// org.apache.commons.math3.special.GammaTest::testRegularizedGammaPositiveNan
    public void testRegularizedGammaPositiveNan() {
        testRegularizedGamma(Double.NaN, 1.0, Double.NaN);
    }

// org.apache.commons.math3.special.GammaTest::testRegularizedGammaNegativePositive
    public void testRegularizedGammaNegativePositive() {
        testRegularizedGamma(Double.NaN, -1.5, 1.0);
    }

// org.apache.commons.math3.special.GammaTest::testRegularizedGammaPositiveNegative
    public void testRegularizedGammaPositiveNegative() {
        testRegularizedGamma(Double.NaN, 1.0, -1.0);
    }

// org.apache.commons.math3.special.GammaTest::testRegularizedGammaZeroPositive
    public void testRegularizedGammaZeroPositive() {
        testRegularizedGamma(Double.NaN, 0.0, 1.0);
    }

// org.apache.commons.math3.special.GammaTest::testRegularizedGammaPositiveZero
    public void testRegularizedGammaPositiveZero() {
        testRegularizedGamma(0.0, 1.0, 0.0);
    }

// org.apache.commons.math3.special.GammaTest::testRegularizedGammaPositivePositive
    public void testRegularizedGammaPositivePositive() {
        testRegularizedGamma(0.632120558828558, 1.0, 1.0);
    }

// org.apache.commons.math3.special.GammaTest::testLogGammaNan
    public void testLogGammaNan() {
        testLogGamma(Double.NaN, Double.NaN);
    }

// org.apache.commons.math3.special.GammaTest::testLogGammaNegative
    public void testLogGammaNegative() {
        testLogGamma(Double.NaN, -1.0);
    }

// org.apache.commons.math3.special.GammaTest::testLogGammaZero
    public void testLogGammaZero() {
        testLogGamma(Double.NaN, 0.0);
    }

// org.apache.commons.math3.special.GammaTest::testLogGammaPositive
    public void testLogGammaPositive() {
        testLogGamma(0.6931471805599457, 3.0);
    }

// org.apache.commons.math3.special.GammaTest::testDigammaLargeArgs
    public void testDigammaLargeArgs() {
        double eps = 1e-8;
        Assert.assertEquals(4.6001618527380874002, Gamma.digamma(100), eps);
        Assert.assertEquals(3.9019896734278921970, Gamma.digamma(50), eps);
        Assert.assertEquals(2.9705239922421490509, Gamma.digamma(20), eps);
        Assert.assertEquals(2.9958363947076465821, Gamma.digamma(20.5), eps);
        Assert.assertEquals(2.2622143570941481605, Gamma.digamma(10.1), eps);
        Assert.assertEquals(2.1168588189004379233, Gamma.digamma(8.8), eps);
        Assert.assertEquals(1.8727843350984671394, Gamma.digamma(7), eps);
        Assert.assertEquals(0.42278433509846713939, Gamma.digamma(2), eps);
        Assert.assertEquals(-100.56088545786867450, Gamma.digamma(0.01), eps);
        Assert.assertEquals(-4.0390398965921882955, Gamma.digamma(-0.8), eps);
        Assert.assertEquals(4.2003210041401844726, Gamma.digamma(-6.3), eps);
    }

// org.apache.commons.math3.special.GammaTest::testDigammaSmallArgs
    public void testDigammaSmallArgs() {
        
        
        double[] expected = {-10.423754940411076795, -100.56088545786867450, -1000.5755719318103005,
                -10000.577051183514335, -100000.57719921568107, -1.0000005772140199687e6, -1.0000000577215500408e7,
                -1.0000000057721564845e8, -1.0000000005772156633e9, -1.0000000000577215665e10, -1.0000000000057721566e11,
                -1.0000000000005772157e12, -1.0000000000000577216e13, -1.0000000000000057722e14, -1.0000000000000005772e15, -1e+16,
                -1e+17, -1e+18, -1e+19, -1e+20, -1e+21, -1e+22, -1e+23, -1e+24, -1e+25, -1e+26,
                -1e+27, -1e+28, -1e+29, -1e+30};
        for (double n = 1; n < 30; n++) {
            checkRelativeError(String.format("Test %.0f: ", n), expected[(int) (n - 1)], Gamma.digamma(FastMath.pow(10.0, -n)), 1e-8);
        }
    }

// org.apache.commons.math3.special.GammaTest::testTrigamma
    public void testTrigamma() {
        double eps = 1e-8;
        
        
        
        double[] data = {
                1e-4, 1.0000000164469368793e8,
                1e-3, 1.0000016425331958690e6,
                1e-2, 10001.621213528313220,
                1e-1, 101.43329915079275882,
                1, 1.6449340668482264365,
                2, 0.64493406684822643647,
                3, 0.39493406684822643647,
                4, 0.28382295573711532536,
                5, 0.22132295573711532536,
                10, 0.10516633568168574612,
                20, 0.051270822935203119832,
                50, 0.020201333226697125806,
                100, 0.010050166663333571395
        };
        for (int i = data.length - 2; i >= 0; i -= 2) {
            Assert.assertEquals(String.format("trigamma %.0f", data[i]), data[i + 1], Gamma.trigamma(data[i]), eps);
        }
    }

// org.apache.commons.math3.special.GammaTest::testLogGamma
    public void testLogGamma() {
        final int ulps = 3;
        for (int i = 0; i < LOG_GAMMA_REF.length; i++) {
            final double[] data = LOG_GAMMA_REF[i];
            final double x = data[0];
            final double expected = data[1];
            final double actual = Gamma.logGamma(x);
            final double tol;
            if (expected == 0.0) {
                tol = 1E-15;
            } else {
                tol = ulps * FastMath.ulp(expected);
            }
            Assert.assertEquals(Double.toString(x), expected, actual, tol);
        }
    }

// org.apache.commons.math3.special.GammaTest::testLogGammaPrecondition1
    public void testLogGammaPrecondition1() {
        Assert.assertTrue(Double.isNaN(Gamma.logGamma(0.0)));
    }

// org.apache.commons.math3.special.GammaTest::testLogGammaPrecondition2
    public void testLogGammaPrecondition2() {
        Assert.assertTrue(Double.isNaN(Gamma.logGamma(-1.0)));
    }

// org.apache.commons.math3.special.GammaTest::testInvGamma1pm1
    public void testInvGamma1pm1() {

        final int ulps = 3;
        for (int i = 0; i < INV_GAMMA1P_M1_REF.length; i++) {
            final double[] ref = INV_GAMMA1P_M1_REF[i];
            final double x = ref[0];
            final double expected = ref[1];
            final double actual = Gamma.invGamma1pm1(x);
            final double tol = ulps * FastMath.ulp(expected);
            Assert.assertEquals(Double.toString(x), expected, actual, tol);
        }
    }

// org.apache.commons.math3.special.GammaTest::testInvGamma1pm1Precondition1
    public void testInvGamma1pm1Precondition1() {

        Gamma.invGamma1pm1(-0.51);
    }

// org.apache.commons.math3.special.GammaTest::testInvGamma1pm1Precondition2
    public void testInvGamma1pm1Precondition2() {

        Gamma.invGamma1pm1(1.51);
    }

// org.apache.commons.math3.special.GammaTest::testLogGamma1p
    public void testLogGamma1p() {

        final int ulps = 3;
        for (int i = 0; i < LOG_GAMMA1P_REF.length; i++) {
            final double[] ref = LOG_GAMMA1P_REF[i];
            final double x = ref[0];
            final double expected = ref[1];
            final double actual = Gamma.logGamma1p(x);
            final double tol = ulps * FastMath.ulp(expected);
            Assert.assertEquals(Double.toString(x), expected, actual, tol);
        }
    }

// org.apache.commons.math3.special.GammaTest::testLogGamma1pPrecondition1
    public void testLogGamma1pPrecondition1() {

        Gamma.logGamma1p(-0.51);
    }

// org.apache.commons.math3.special.GammaTest::testLogGamma1pPrecondition2
    public void testLogGamma1pPrecondition2() {

        Gamma.logGamma1p(1.51);
    }

// org.apache.commons.math3.special.GammaTest::testGamma
    public void testGamma() {

        for (int i = 0; i < GAMMA_REF.length; i++) {
            final double[] ref = GAMMA_REF[i];
            final double x = ref[0];
            final double expected = ref[1];
            final double actual = Gamma.gamma(x);
            final double absX = FastMath.abs(x);
            final int ulps;
            if (absX <= 8.0) {
                ulps = 3;
            } else if (absX <= 20.0) {
                ulps = 5;
            } else if (absX <= 30.0) {
                ulps = 50;
            } else if (absX <= 50.0) {
                ulps = 180;
            } else {
                ulps = 500;
            }
            final double tol = ulps * FastMath.ulp(expected);
            Assert.assertEquals(Double.toString(x), expected, actual, tol);
        }
    }

// org.apache.commons.math3.special.GammaTest::testGammaNegativeInteger
    public void testGammaNegativeInteger() {

        for (int i = -100; i <= 0; i++) {
            Assert.assertTrue(Integer.toString(i), Double.isNaN(Gamma.gamma(i)));
        }
    }

// org.apache.commons.math3.special.GammaTest::testLogGammaSum
    public void testLogGammaSum() {
        final int ulps = 5;
        for (int i = 0; i < LOG_GAMMA_SUM_REF.length; i++) {
            final double[] ref = LOG_GAMMA_SUM_REF[i];
            final double a = ref[0];
            final double b = ref[1];
            final double expected = ref[2];
            final double actual = Gamma.logGammaSum(a, b);
            final double tol = ulps * FastMath.ulp(expected);
            final StringBuilder builder = new StringBuilder();
            builder.append(a).append(", ").append(b);
            Assert.assertEquals(builder.toString(), expected, actual, tol);
        }
    }

// org.apache.commons.math3.special.GammaTest::testLogGammaSumPrecondition1
    public void testLogGammaSumPrecondition1() {

        Gamma.logGammaSum(0.0, 1.0);
    }

// org.apache.commons.math3.special.GammaTest::testLogGammaSumPrecondition2
    public void testLogGammaSumPrecondition2() {

        Gamma.logGammaSum(3.0, 1.0);
    }

// org.apache.commons.math3.special.GammaTest::testLogGammaSumPrecondition3
    public void testLogGammaSumPrecondition3() {

        Gamma.logGammaSum(1.0, 0.0);
    }

// org.apache.commons.math3.special.GammaTest::testLogGammaSumPrecondition4
    public void testLogGammaSumPrecondition4() {

        Gamma.logGammaSum(1.0, 3.0);
    }

// org.apache.commons.math3.special.GammaTest::testLogGammaMinusLogGammaSum
    public void testLogGammaMinusLogGammaSum() {
        final int ulps = 4;
        for (int i = 0; i < LOG_GAMMA_MINUS_LOG_GAMMA_SUM_REF.length; i++) {
            final double[] ref = LOG_GAMMA_MINUS_LOG_GAMMA_SUM_REF[i];
            final double a = ref[0];
            final double b = ref[1];
            final double expected = ref[2];
            final double actual = Gamma.logGammaMinusLogGammaSum(a, b);
            final double tol = ulps * FastMath.ulp(expected);
            final StringBuilder builder = new StringBuilder();
            builder.append(a).append(", ").append(b);
            Assert.assertEquals(builder.toString(), expected, actual, tol);
        }
    }

// org.apache.commons.math3.special.GammaTest::testLogGammaMinusLogGammaSumPrecondition1
    public void testLogGammaMinusLogGammaSumPrecondition1() {
        Gamma.logGammaMinusLogGammaSum(-1.0, 8.0);
    }

// org.apache.commons.math3.special.GammaTest::testLogGammaMinusLogGammaSumPrecondition2
    public void testLogGammaMinusLogGammaSumPrecondition2() {
        Gamma.logGammaMinusLogGammaSum(1.0, 7.0);
    }

// org.apache.commons.math3.stat.CertifiedDataTest::testSummaryStatistics
    public void testSummaryStatistics() throws Exception {
        SummaryStatistics u = new SummaryStatistics();
        loadStats("data/PiDigits.txt", u);
        Assert.assertEquals("PiDigits: std", std, u.getStandardDeviation(), 1E-13);
        Assert.assertEquals("PiDigits: mean", mean, u.getMean(), 1E-13);

        loadStats("data/Mavro.txt", u);
        Assert.assertEquals("Mavro: std", std, u.getStandardDeviation(), 1E-14);
        Assert.assertEquals("Mavro: mean", mean, u.getMean(), 1E-14);

        loadStats("data/Michelso.txt", u);
        Assert.assertEquals("Michelso: std", std, u.getStandardDeviation(), 1E-13);
        Assert.assertEquals("Michelso: mean", mean, u.getMean(), 1E-13);

        loadStats("data/NumAcc1.txt", u);
        Assert.assertEquals("NumAcc1: std", std, u.getStandardDeviation(), 1E-14);
        Assert.assertEquals("NumAcc1: mean", mean, u.getMean(), 1E-14);

        loadStats("data/NumAcc2.txt", u);
        Assert.assertEquals("NumAcc2: std", std, u.getStandardDeviation(), 1E-14);
        Assert.assertEquals("NumAcc2: mean", mean, u.getMean(), 1E-14);
    }

// org.apache.commons.math3.stat.CertifiedDataTest::testDescriptiveStatistics
    public void testDescriptiveStatistics() throws Exception {

        DescriptiveStatistics u = new DescriptiveStatistics();

        loadStats("data/PiDigits.txt", u);
        Assert.assertEquals("PiDigits: std", std, u.getStandardDeviation(), 1E-14);
        Assert.assertEquals("PiDigits: mean", mean, u.getMean(), 1E-14);

        loadStats("data/Mavro.txt", u);
        Assert.assertEquals("Mavro: std", std, u.getStandardDeviation(), 1E-14);
        Assert.assertEquals("Mavro: mean", mean, u.getMean(), 1E-14);

        loadStats("data/Michelso.txt", u);
        Assert.assertEquals("Michelso: std", std, u.getStandardDeviation(), 1E-14);
        Assert.assertEquals("Michelso: mean", mean, u.getMean(), 1E-14);

        loadStats("data/NumAcc1.txt", u);
        Assert.assertEquals("NumAcc1: std", std, u.getStandardDeviation(), 1E-14);
        Assert.assertEquals("NumAcc1: mean", mean, u.getMean(), 1E-14);

        loadStats("data/NumAcc2.txt", u);
        Assert.assertEquals("NumAcc2: std", std, u.getStandardDeviation(), 1E-14);
        Assert.assertEquals("NumAcc2: mean", mean, u.getMean(), 1E-14);
    }

// org.apache.commons.math3.stat.StatUtilsTest::testStats
    public void testStats() {
        double[] values = new double[] { one, two, two, three };
        Assert.assertEquals("sum", sum, StatUtils.sum(values), tolerance);
        Assert.assertEquals("sumsq", sumSq, StatUtils.sumSq(values), tolerance);
        Assert.assertEquals("var", var, StatUtils.variance(values), tolerance);
        Assert.assertEquals("var with mean", var, StatUtils.variance(values, mean), tolerance);
        Assert.assertEquals("mean", mean, StatUtils.mean(values), tolerance);
        Assert.assertEquals("min", min, StatUtils.min(values), tolerance);
        Assert.assertEquals("max", max, StatUtils.max(values), tolerance);
    }

// org.apache.commons.math3.stat.StatUtilsTest::testN0andN1Conditions
    public void testN0andN1Conditions() {
        double[] values = new double[0];

        Assert.assertTrue(
            "Mean of n = 0 set should be NaN",
            Double.isNaN(StatUtils.mean(values)));
        Assert.assertTrue(
            "Variance of n = 0 set should be NaN",
            Double.isNaN(StatUtils.variance(values)));

        values = new double[] { one };

        Assert.assertTrue(
            "Mean of n = 1 set should be value of single item n1",
            StatUtils.mean(values) == one);
        Assert.assertTrue(
            "Variance of n = 1 set should be zero",
            StatUtils.variance(values) == 0);
    }

// org.apache.commons.math3.stat.StatUtilsTest::testArrayIndexConditions
    public void testArrayIndexConditions() {
        double[] values = { 1.0, 2.0, 3.0, 4.0 };

        Assert.assertEquals(
            "Sum not expected",
            5.0,
            StatUtils.sum(values, 1, 2),
            Double.MIN_VALUE);
        Assert.assertEquals(
            "Sum not expected",
            3.0,
            StatUtils.sum(values, 0, 2),
            Double.MIN_VALUE);
        Assert.assertEquals(
            "Sum not expected",
            7.0,
            StatUtils.sum(values, 2, 2),
            Double.MIN_VALUE);

        try {
            StatUtils.sum(values, 2, 3);
            Assert.fail("Expected RuntimeException");
        } catch (RuntimeException e) {
            
        }

        try {
            StatUtils.sum(values, -1, 2);
            Assert.fail("Expected RuntimeException");
        } catch (RuntimeException e) {
            
        }

    }

// org.apache.commons.math3.stat.StatUtilsTest::testSumSq
    public void testSumSq() {
        double[] x = null;

        
        try {
            StatUtils.sumSq(x);
            Assert.fail("null is not a valid data array.");
        } catch (MathIllegalArgumentException ex) {
            
        }

        try {
            StatUtils.sumSq(x, 0, 4);
            Assert.fail("null is not a valid data array.");
        } catch (MathIllegalArgumentException ex) {
            
        }

        
        x = new double[] {};
        TestUtils.assertEquals(0, StatUtils.sumSq(x), tolerance);
        TestUtils.assertEquals(0, StatUtils.sumSq(x, 0, 0), tolerance);

        
        x = new double[] {two};
        TestUtils.assertEquals(4, StatUtils.sumSq(x), tolerance);
        TestUtils.assertEquals(4, StatUtils.sumSq(x, 0, 1), tolerance);

        
        x = new double[] {one, two, two, three};
        TestUtils.assertEquals(18, StatUtils.sumSq(x), tolerance);
        TestUtils.assertEquals(8, StatUtils.sumSq(x, 1, 2), tolerance);
    }

// org.apache.commons.math3.stat.StatUtilsTest::testProduct
    public void testProduct() {
        double[] x = null;

        
        try {
            StatUtils.product(x);
            Assert.fail("null is not a valid data array.");
        } catch (MathIllegalArgumentException ex) {
            
        }

        try {
            StatUtils.product(x, 0, 4);
            Assert.fail("null is not a valid data array.");
        } catch (MathIllegalArgumentException ex) {
            
        }

        
        x = new double[] {};
        TestUtils.assertEquals(1, StatUtils.product(x), tolerance);
        TestUtils.assertEquals(1, StatUtils.product(x, 0, 0), tolerance);

        
        x = new double[] {two};
        TestUtils.assertEquals(two, StatUtils.product(x), tolerance);
        TestUtils.assertEquals(two, StatUtils.product(x, 0, 1), tolerance);

        
        x = new double[] {one, two, two, three};
        TestUtils.assertEquals(12, StatUtils.product(x), tolerance);
        TestUtils.assertEquals(4, StatUtils.product(x, 1, 2), tolerance);
    }

// org.apache.commons.math3.stat.StatUtilsTest::testSumLog
    public void testSumLog() {
        double[] x = null;

        
        try {
            StatUtils.sumLog(x);
            Assert.fail("null is not a valid data array.");
        } catch (MathIllegalArgumentException ex) {
            
        }

        try {
            StatUtils.sumLog(x, 0, 4);
            Assert.fail("null is not a valid data array.");
        } catch (MathIllegalArgumentException ex) {
            
        }

        
        x = new double[] {};
        TestUtils.assertEquals(0, StatUtils.sumLog(x), tolerance);
        TestUtils.assertEquals(0, StatUtils.sumLog(x, 0, 0), tolerance);

        
        x = new double[] {two};
        TestUtils.assertEquals(FastMath.log(two), StatUtils.sumLog(x), tolerance);
        TestUtils.assertEquals(FastMath.log(two), StatUtils.sumLog(x, 0, 1), tolerance);

        
        x = new double[] {one, two, two, three};
        TestUtils.assertEquals(FastMath.log(one) + 2.0 * FastMath.log(two) + FastMath.log(three), StatUtils.sumLog(x), tolerance);
        TestUtils.assertEquals(2.0 * FastMath.log(two), StatUtils.sumLog(x, 1, 2), tolerance);
    }

// org.apache.commons.math3.stat.StatUtilsTest::testMean
    public void testMean() {
        double[] x = null;

        try {
            StatUtils.mean(x, 0, 4);
            Assert.fail("null is not a valid data array.");
        } catch (MathIllegalArgumentException ex) {
            
        }

        
        x = new double[] {};
        TestUtils.assertEquals(Double.NaN, StatUtils.mean(x, 0, 0), tolerance);

        
        x = new double[] {two};
        TestUtils.assertEquals(two, StatUtils.mean(x, 0, 1), tolerance);

        
        x = new double[] {one, two, two, three};
        TestUtils.assertEquals(2.5, StatUtils.mean(x, 2, 2), tolerance);
    }

// org.apache.commons.math3.stat.StatUtilsTest::testVariance
    public void testVariance() {
        double[] x = null;

        try {
            StatUtils.variance(x, 0, 4);
            Assert.fail("null is not a valid data array.");
        } catch (MathIllegalArgumentException ex) {
            
        }

        
        x = new double[] {};
        TestUtils.assertEquals(Double.NaN, StatUtils.variance(x, 0, 0), tolerance);

        
        x = new double[] {two};
        TestUtils.assertEquals(0.0, StatUtils.variance(x, 0, 1), tolerance);

        
        x = new double[] {one, two, two, three};
        TestUtils.assertEquals(0.5, StatUtils.variance(x, 2, 2), tolerance);

        
        x = new double[] {one, two, two, three};
        TestUtils.assertEquals(0.5, StatUtils.variance(x,2.5, 2, 2), tolerance);
    }

// org.apache.commons.math3.stat.StatUtilsTest::testPopulationVariance
    public void testPopulationVariance() {
        double[] x = null;

        try {
            StatUtils.variance(x, 0, 4);
            Assert.fail("null is not a valid data array.");
        } catch (MathIllegalArgumentException ex) {
            
        }

        
        x = new double[] {};
        TestUtils.assertEquals(Double.NaN, StatUtils.populationVariance(x, 0, 0), tolerance);

        
        x = new double[] {two};
        TestUtils.assertEquals(0.0, StatUtils.populationVariance(x, 0, 1), tolerance);

        
        x = new double[] {one, two, two, three};
        TestUtils.assertEquals(0.25, StatUtils.populationVariance(x, 0, 2), tolerance);

        
        x = new double[] {one, two, two, three};
        TestUtils.assertEquals(0.25, StatUtils.populationVariance(x, 2.5, 2, 2), tolerance);
    }

// org.apache.commons.math3.stat.StatUtilsTest::testMax
    public void testMax() {
        double[] x = null;

        try {
            StatUtils.max(x, 0, 4);
            Assert.fail("null is not a valid data array.");
        } catch (MathIllegalArgumentException ex) {
            
        }

        
        x = new double[] {};
        TestUtils.assertEquals(Double.NaN, StatUtils.max(x, 0, 0), tolerance);

        
        x = new double[] {two};
        TestUtils.assertEquals(two, StatUtils.max(x, 0, 1), tolerance);

        
        x = new double[] {one, two, two, three};
        TestUtils.assertEquals(three, StatUtils.max(x, 1, 3), tolerance);

        
        x = new double[] {nan, two, three};
        TestUtils.assertEquals(three, StatUtils.max(x), tolerance);

        
        x = new double[] {one, nan, three};
        TestUtils.assertEquals(three, StatUtils.max(x), tolerance);

        
        x = new double[] {one, two, nan};
        TestUtils.assertEquals(two, StatUtils.max(x), tolerance);

        
        x = new double[] {nan, nan, nan};
        TestUtils.assertEquals(nan, StatUtils.max(x), tolerance);
    }

// org.apache.commons.math3.stat.StatUtilsTest::testMin
    public void testMin() {
        double[] x = null;

        try {
            StatUtils.min(x, 0, 4);
            Assert.fail("null is not a valid data array.");
        } catch (MathIllegalArgumentException ex) {
            
        }

        
        x = new double[] {};
        TestUtils.assertEquals(Double.NaN, StatUtils.min(x, 0, 0), tolerance);

        
        x = new double[] {two};
        TestUtils.assertEquals(two, StatUtils.min(x, 0, 1), tolerance);

        
        x = new double[] {one, two, two, three};
        TestUtils.assertEquals(two, StatUtils.min(x, 1, 3), tolerance);

        
        x = new double[] {nan, two, three};
        TestUtils.assertEquals(two, StatUtils.min(x), tolerance);

        
        x = new double[] {one, nan, three};
        TestUtils.assertEquals(one, StatUtils.min(x), tolerance);

        
        x = new double[] {one, two, nan};
        TestUtils.assertEquals(one, StatUtils.min(x), tolerance);

        
        x = new double[] {nan, nan, nan};
        TestUtils.assertEquals(nan, StatUtils.min(x), tolerance);
    }

// org.apache.commons.math3.stat.StatUtilsTest::testPercentile
    public void testPercentile() {
        double[] x = null;

        
        try {
            StatUtils.percentile(x, .25);
            Assert.fail("null is not a valid data array.");
        } catch (MathIllegalArgumentException ex) {
            
        }

        try {
            StatUtils.percentile(x, 0, 4, 0.25);
            Assert.fail("null is not a valid data array.");
        } catch (MathIllegalArgumentException ex) {
            
        }

        
        x = new double[] {};
        TestUtils.assertEquals(Double.NaN, StatUtils.percentile(x, 25), tolerance);
        TestUtils.assertEquals(Double.NaN, StatUtils.percentile(x, 0, 0, 25), tolerance);

        
        x = new double[] {two};
        TestUtils.assertEquals(two, StatUtils.percentile(x, 25), tolerance);
        TestUtils.assertEquals(two, StatUtils.percentile(x, 0, 1, 25), tolerance);

        
        x = new double[] {one, two, two, three};
        TestUtils.assertEquals(2.5, StatUtils.percentile(x, 70), tolerance);
        TestUtils.assertEquals(2.5, StatUtils.percentile(x, 1, 3, 62.5), tolerance);
    }

// org.apache.commons.math3.stat.StatUtilsTest::testDifferenceStats
    public void testDifferenceStats() {
        double sample1[] = {1d, 2d, 3d, 4d};
        double sample2[] = {1d, 3d, 4d, 2d};
        double diff[] = {0d, -1d, -1d, 2d};
        double small[] = {1d, 4d};
        double meanDifference = StatUtils.meanDifference(sample1, sample2);
        Assert.assertEquals(StatUtils.sumDifference(sample1, sample2), StatUtils.sum(diff), tolerance);
        Assert.assertEquals(meanDifference, StatUtils.mean(diff), tolerance);
        Assert.assertEquals(StatUtils.varianceDifference(sample1, sample2, meanDifference),
                StatUtils.variance(diff), tolerance);
        try {
            StatUtils.meanDifference(sample1, small);
            Assert.fail("Expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
        try {
            StatUtils.varianceDifference(sample1, small, meanDifference);
            Assert.fail("Expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
        try {
            double[] single = {1.0};
            StatUtils.varianceDifference(single, single, meanDifference);
            Assert.fail("Expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math3.stat.StatUtilsTest::testGeometricMean
    public void testGeometricMean() {
        double[] test = null;
        try {
            StatUtils.geometricMean(test);
            Assert.fail("Expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
        test = new double[] {2, 4, 6, 8};
        Assert.assertEquals(FastMath.exp(0.25d * StatUtils.sumLog(test)),
                StatUtils.geometricMean(test), Double.MIN_VALUE);
        Assert.assertEquals(FastMath.exp(0.5 * StatUtils.sumLog(test, 0, 2)),
                StatUtils.geometricMean(test, 0, 2), Double.MIN_VALUE);
    }

// org.apache.commons.math3.stat.StatUtilsTest::testNormalize1
    public void testNormalize1() {
        double sample[] = { 50, 100 };
        double expectedSample[] = { -25 / Math.sqrt(1250), 25 / Math.sqrt(1250) };
        double[] out = StatUtils.normalize(sample);
        for (int i = 0; i < out.length; i++) {
            Assert.assertTrue(Precision.equals(out[i], expectedSample[i], 1));
        }

    }

// org.apache.commons.math3.stat.StatUtilsTest::testNormalize2
    public void testNormalize2() {
        
        int length = 77;
        double sample[] = new double[length];
        for (int i = 0; i < length; i++) {
            sample[i] = Math.random();
        }
        
        double standardizedSample[] = StatUtils.normalize(sample);

        DescriptiveStatistics stats = new DescriptiveStatistics();
        
        for (int i = 0; i < length; i++) {
            stats.addValue(standardizedSample[i]);
        }
        
        double distance = 1E-10;
        
        Assert.assertEquals(0.0, stats.getMean(), distance);
        Assert.assertEquals(1.0, stats.getStandardDeviation(), distance);

    }

// org.apache.commons.math3.stat.clustering.DBSCANClustererTest::testCluster
    public void testCluster() {
        
        final EuclideanDoublePoint[] points = new EuclideanDoublePoint[] {
                new EuclideanDoublePoint(new double[] { 83.08303244924173, 58.83387754182331 }),
                new EuclideanDoublePoint(new double[] { 45.05445510940626, 23.469642649637535 }),
                new EuclideanDoublePoint(new double[] { 14.96417921432294, 69.0264096390456 }),
                new EuclideanDoublePoint(new double[] { 73.53189604333602, 34.896145021310076 }),
                new EuclideanDoublePoint(new double[] { 73.28498173551634, 33.96860806993209 }),
                new EuclideanDoublePoint(new double[] { 73.45828098873608, 33.92584423092194 }),
                new EuclideanDoublePoint(new double[] { 73.9657889183145, 35.73191006924026 }),
                new EuclideanDoublePoint(new double[] { 74.0074097183533, 36.81735596177168 }),
                new EuclideanDoublePoint(new double[] { 73.41247541410848, 34.27314856695011 }),
                new EuclideanDoublePoint(new double[] { 73.9156256353017, 36.83206791547127 }),
                new EuclideanDoublePoint(new double[] { 74.81499205809087, 37.15682749846019 }),
                new EuclideanDoublePoint(new double[] { 74.03144880081527, 37.57399178552441 }),
                new EuclideanDoublePoint(new double[] { 74.51870941207744, 38.674258946906775 }),
                new EuclideanDoublePoint(new double[] { 74.50754595105536, 35.58903978415765 }),
                new EuclideanDoublePoint(new double[] { 74.51322752749547, 36.030572259100154 }),
                new EuclideanDoublePoint(new double[] { 59.27900996617973, 46.41091720294207 }),
                new EuclideanDoublePoint(new double[] { 59.73744793841615, 46.20015558367595 }),
                new EuclideanDoublePoint(new double[] { 58.81134076672606, 45.71150126331486 }),
                new EuclideanDoublePoint(new double[] { 58.52225539437495, 47.416083617601544 }),
                new EuclideanDoublePoint(new double[] { 58.218626647023484, 47.36228902172297 }),
                new EuclideanDoublePoint(new double[] { 60.27139669447206, 46.606106348801404 }),
                new EuclideanDoublePoint(new double[] { 60.894962462363765, 46.976924697402865 }),
                new EuclideanDoublePoint(new double[] { 62.29048673878424, 47.66970563563518 }),
                new EuclideanDoublePoint(new double[] { 61.03857608977705, 46.212924720020965 }),
                new EuclideanDoublePoint(new double[] { 60.16916214139201, 45.18193661351688 }),
                new EuclideanDoublePoint(new double[] { 59.90036905976012, 47.555364347063005 }),
                new EuclideanDoublePoint(new double[] { 62.33003634144552, 47.83941489877179 }),
                new EuclideanDoublePoint(new double[] { 57.86035536718555, 47.31117930193432 }),
                new EuclideanDoublePoint(new double[] { 58.13715479685925, 48.985960494028404 }),
                new EuclideanDoublePoint(new double[] { 56.131923963548616, 46.8508904252667 }),
                new EuclideanDoublePoint(new double[] { 55.976329887053, 47.46384037658572 }),
                new EuclideanDoublePoint(new double[] { 56.23245975235477, 47.940035191131756 }),
                new EuclideanDoublePoint(new double[] { 58.51687048212625, 46.622885352699086 }),
                new EuclideanDoublePoint(new double[] { 57.85411081905477, 45.95394361577928 }),
                new EuclideanDoublePoint(new double[] { 56.445776311447844, 45.162093662656844 }),
                new EuclideanDoublePoint(new double[] { 57.36691949656233, 47.50097194337286 }),
                new EuclideanDoublePoint(new double[] { 58.243626387557015, 46.114052729681134 }),
                new EuclideanDoublePoint(new double[] { 56.27224595635198, 44.799080066150054 }),
                new EuclideanDoublePoint(new double[] { 57.606924816500396, 46.94291057763621 }),
                new EuclideanDoublePoint(new double[] { 30.18714230041951, 13.877149710431695 }),
                new EuclideanDoublePoint(new double[] { 30.449448810657486, 13.490778346545994 }),
                new EuclideanDoublePoint(new double[] { 30.295018390286714, 13.264889000216499 }),
                new EuclideanDoublePoint(new double[] { 30.160201832884923, 11.89278262341395 }),
                new EuclideanDoublePoint(new double[] { 31.341509791789576, 15.282655921997502 }),
                new EuclideanDoublePoint(new double[] { 31.68601630325429, 14.756873246748 }),
                new EuclideanDoublePoint(new double[] { 29.325963742565364, 12.097849250072613 }),
                new EuclideanDoublePoint(new double[] { 29.54820742388256, 13.613295356975868 }),
                new EuclideanDoublePoint(new double[] { 28.79359608888626, 10.36352064087987 }),
                new EuclideanDoublePoint(new double[] { 31.01284597092308, 12.788479208014905 }),
                new EuclideanDoublePoint(new double[] { 27.58509216737002, 11.47570110601373 }),
                new EuclideanDoublePoint(new double[] { 28.593799561727792, 10.780998203903437 }),
                new EuclideanDoublePoint(new double[] { 31.356105766724795, 15.080316198524088 }),
                new EuclideanDoublePoint(new double[] { 31.25948503636755, 13.674329151166603 }),
                new EuclideanDoublePoint(new double[] { 32.31590076372959, 14.95261758659035 }),
                new EuclideanDoublePoint(new double[] { 30.460413702763617, 15.88402809202671 }),
                new EuclideanDoublePoint(new double[] { 32.56178203062154, 14.586076852632686 }),
                new EuclideanDoublePoint(new double[] { 32.76138648530468, 16.239837325178087 }),
                new EuclideanDoublePoint(new double[] { 30.1829453331884, 14.709592407103628 }),
                new EuclideanDoublePoint(new double[] { 29.55088173528202, 15.0651247180067 }),
                new EuclideanDoublePoint(new double[] { 29.004155302187428, 14.089665298582986 }),
                new EuclideanDoublePoint(new double[] { 29.339624439831823, 13.29096065578051 }),
                new EuclideanDoublePoint(new double[] { 30.997460327576846, 14.551914158277214 }),
                new EuclideanDoublePoint(new double[] { 30.66784126125276, 16.269703107886016 })
        };

        final DBSCANClusterer<EuclideanDoublePoint> transformer =
                new DBSCANClusterer<EuclideanDoublePoint>(2.0, 5);
        final List<Cluster<EuclideanDoublePoint>> clusters = transformer.cluster(Arrays.asList(points));

        final List<EuclideanDoublePoint> clusterOne =
                Arrays.asList(points[3], points[4], points[5], points[6], points[7], points[8], points[9], points[10],
                              points[11], points[12], points[13], points[14]);
        final List<EuclideanDoublePoint> clusterTwo =
                Arrays.asList(points[15], points[16], points[17], points[18], points[19], points[20], points[21],
                              points[22], points[23], points[24], points[25], points[26], points[27], points[28],
                              points[29], points[30], points[31], points[32], points[33], points[34], points[35],
                              points[36], points[37], points[38]);
        final List<EuclideanDoublePoint> clusterThree =
                Arrays.asList(points[39], points[40], points[41], points[42], points[43], points[44], points[45],
                              points[46], points[47], points[48], points[49], points[50], points[51], points[52],
                              points[53], points[54], points[55], points[56], points[57], points[58], points[59],
                              points[60], points[61], points[62]);

        boolean cluster1Found = false;
        boolean cluster2Found = false;
        boolean cluster3Found = false;
        Assert.assertEquals(3, clusters.size());
        for (final Cluster<EuclideanDoublePoint> cluster : clusters) {
            if (cluster.getPoints().containsAll(clusterOne)) {
                cluster1Found = true;
            }
            if (cluster.getPoints().containsAll(clusterTwo)) {
                cluster2Found = true;
            }
            if (cluster.getPoints().containsAll(clusterThree)) {
                cluster3Found = true;
            }
        }
        Assert.assertTrue(cluster1Found);
        Assert.assertTrue(cluster2Found);
        Assert.assertTrue(cluster3Found);
    }

// org.apache.commons.math3.stat.clustering.DBSCANClustererTest::testSingleLink
    public void testSingleLink() {
        final EuclideanIntegerPoint[] points = {
                new EuclideanIntegerPoint(new int[] {10, 10}), 
                new EuclideanIntegerPoint(new int[] {12, 9}),
                new EuclideanIntegerPoint(new int[] {10, 8}),
                new EuclideanIntegerPoint(new int[] {8, 8}),
                new EuclideanIntegerPoint(new int[] {8, 6}),
                new EuclideanIntegerPoint(new int[] {7, 7}),
                new EuclideanIntegerPoint(new int[] {5, 6}),  
                new EuclideanIntegerPoint(new int[] {14, 8}), 
                new EuclideanIntegerPoint(new int[] {7, 15}), 
                new EuclideanIntegerPoint(new int[] {17, 8}), 
                
        };
        
        final DBSCANClusterer<EuclideanIntegerPoint> clusterer = new DBSCANClusterer<EuclideanIntegerPoint>(3, 3);
        List<Cluster<EuclideanIntegerPoint>> clusters = clusterer.cluster(Arrays.asList(points));
        
        Assert.assertEquals(1, clusters.size());
        
        final List<EuclideanIntegerPoint> clusterOne =
                Arrays.asList(points[0], points[1], points[2], points[3], points[4], points[5], points[6], points[7]);
        Assert.assertTrue(clusters.get(0).getPoints().containsAll(clusterOne));
    }

// org.apache.commons.math3.stat.clustering.DBSCANClustererTest::testGetEps
    public void testGetEps() {
        final DBSCANClusterer<EuclideanDoublePoint> transformer = new DBSCANClusterer<EuclideanDoublePoint>(2.0, 5);
        Assert.assertEquals(2.0, transformer.getEps(), 0.0);
    }

// org.apache.commons.math3.stat.clustering.DBSCANClustererTest::testGetMinPts
    public void testGetMinPts() {
        final DBSCANClusterer<EuclideanDoublePoint> transformer = new DBSCANClusterer<EuclideanDoublePoint>(2.0, 5);
        Assert.assertEquals(5, transformer.getMinPts());
    }

// org.apache.commons.math3.stat.clustering.DBSCANClustererTest::testNegativeEps
    public void testNegativeEps() {
        new DBSCANClusterer<EuclideanDoublePoint>(-2.0, 5);
    }

// org.apache.commons.math3.stat.clustering.DBSCANClustererTest::testNegativeMinPts
    public void testNegativeMinPts() {
        new DBSCANClusterer<EuclideanDoublePoint>(2.0, -5);
    }

// org.apache.commons.math3.stat.clustering.DBSCANClustererTest::testNullDataset
    public void testNullDataset() {
        DBSCANClusterer<EuclideanDoublePoint> clusterer = new DBSCANClusterer<EuclideanDoublePoint>(2.0, 5);
        clusterer.cluster(null);
    }

// org.apache.commons.math3.stat.clustering.EuclideanDoublePointTest::testArrayIsReference
    public void testArrayIsReference() {
        final double[] array = { -3.0, -2.0, -1.0, 0.0, 1.0 };
        Assert.assertArrayEquals(array, new EuclideanDoublePoint(array).getPoint(), 1.0e-15);
    }

// org.apache.commons.math3.stat.clustering.EuclideanDoublePointTest::testDistance
    public void testDistance() {
        final EuclideanDoublePoint e1 = new EuclideanDoublePoint(new double[] { -3.0, -2.0, -1.0, 0.0, 1.0 });
        final EuclideanDoublePoint e2 = new EuclideanDoublePoint(new double[] { 1.0, 0.0, -1.0, 1.0, 1.0 });
        Assert.assertEquals(FastMath.sqrt(21.0), e1.distanceFrom(e2), 1.0e-15);
        Assert.assertEquals(0.0, e1.distanceFrom(e1), 1.0e-15);
        Assert.assertEquals(0.0, e2.distanceFrom(e2), 1.0e-15);
    }

// org.apache.commons.math3.stat.clustering.EuclideanDoublePointTest::testCentroid
    public void testCentroid() {
        final List<EuclideanDoublePoint> list = new ArrayList<EuclideanDoublePoint>();
        list.add(new EuclideanDoublePoint(new double[] { 1.0, 3.0 }));
        list.add(new EuclideanDoublePoint(new double[] { 2.0, 2.0 }));
        list.add(new EuclideanDoublePoint(new double[] { 3.0, 3.0 }));
        list.add(new EuclideanDoublePoint(new double[] { 2.0, 4.0 }));
        final EuclideanDoublePoint c = list.get(0).centroidOf(list);
        Assert.assertEquals(2.0, c.getPoint()[0], 1.0e-15);
        Assert.assertEquals(3.0, c.getPoint()[1], 1.0e-15);
    }

// org.apache.commons.math3.stat.clustering.EuclideanDoublePointTest::testSerial
    public void testSerial() {
        final EuclideanDoublePoint p = new EuclideanDoublePoint(new double[] { -3.0, -2.0, -1.0, 0.0, 1.0 });
        Assert.assertEquals(p, TestUtils.serializeAndRecover(p));
    }

// org.apache.commons.math3.stat.clustering.EuclideanIntegerPointTest::testArrayIsReference
    public void testArrayIsReference() {
        int[] array = { -3, -2, -1, 0, 1 };
        Assert.assertTrue(array == new EuclideanIntegerPoint(array).getPoint());
    }

// org.apache.commons.math3.stat.clustering.EuclideanIntegerPointTest::testDistance
    public void testDistance() {
        EuclideanIntegerPoint e1 = new EuclideanIntegerPoint(new int[] { -3, -2, -1, 0, 1 });
        EuclideanIntegerPoint e2 = new EuclideanIntegerPoint(new int[] {  1,  0, -1, 1, 1 });
        Assert.assertEquals(FastMath.sqrt(21.0), e1.distanceFrom(e2), 1.0e-15);
        Assert.assertEquals(0.0, e1.distanceFrom(e1), 1.0e-15);
        Assert.assertEquals(0.0, e2.distanceFrom(e2), 1.0e-15);
    }

// org.apache.commons.math3.stat.clustering.EuclideanIntegerPointTest::testCentroid
    public void testCentroid() {
        List<EuclideanIntegerPoint> list = new ArrayList<EuclideanIntegerPoint>();
        list.add(new EuclideanIntegerPoint(new int[] {  1,  3 }));
        list.add(new EuclideanIntegerPoint(new int[] {  2,  2 }));
        list.add(new EuclideanIntegerPoint(new int[] {  3,  3 }));
        list.add(new EuclideanIntegerPoint(new int[] {  2,  4 }));
        EuclideanIntegerPoint c = list.get(0).centroidOf(list);
        Assert.assertEquals(2, c.getPoint()[0]);
        Assert.assertEquals(3, c.getPoint()[1]);
    }

// org.apache.commons.math3.stat.clustering.EuclideanIntegerPointTest::testSerial
    public void testSerial() {
        EuclideanIntegerPoint p = new EuclideanIntegerPoint(new int[] { -3, -2, -1, 0, 1 });
        Assert.assertEquals(p, TestUtils.serializeAndRecover(p));
    }

// org.apache.commons.math3.stat.clustering.KMeansPlusPlusClustererTest::dimension2
    public void dimension2() {
        KMeansPlusPlusClusterer<EuclideanIntegerPoint> transformer =
            new KMeansPlusPlusClusterer<EuclideanIntegerPoint>(new Random(1746432956321l));
        EuclideanIntegerPoint[] points = new EuclideanIntegerPoint[] {

                
                new EuclideanIntegerPoint(new int[] { -15,  3 }),
                new EuclideanIntegerPoint(new int[] { -15,  4 }),
                new EuclideanIntegerPoint(new int[] { -15,  5 }),
                new EuclideanIntegerPoint(new int[] { -14,  3 }),
                new EuclideanIntegerPoint(new int[] { -14,  5 }),
                new EuclideanIntegerPoint(new int[] { -13,  3 }),
                new EuclideanIntegerPoint(new int[] { -13,  4 }),
                new EuclideanIntegerPoint(new int[] { -13,  5 }),

                
                new EuclideanIntegerPoint(new int[] { -1,  0 }),
                new EuclideanIntegerPoint(new int[] { -1, -1 }),
                new EuclideanIntegerPoint(new int[] {  0, -1 }),
                new EuclideanIntegerPoint(new int[] {  1, -1 }),
                new EuclideanIntegerPoint(new int[] {  1, -2 }),

                
                new EuclideanIntegerPoint(new int[] { 13,  3 }),
                new EuclideanIntegerPoint(new int[] { 13,  4 }),
                new EuclideanIntegerPoint(new int[] { 14,  4 }),
                new EuclideanIntegerPoint(new int[] { 14,  7 }),
                new EuclideanIntegerPoint(new int[] { 16,  5 }),
                new EuclideanIntegerPoint(new int[] { 16,  6 }),
                new EuclideanIntegerPoint(new int[] { 17,  4 }),
                new EuclideanIntegerPoint(new int[] { 17,  7 })

        };
        List<Cluster<EuclideanIntegerPoint>> clusters =
            transformer.cluster(Arrays.asList(points), 3, 5, 10);

        Assert.assertEquals(3, clusters.size());
        boolean cluster1Found = false;
        boolean cluster2Found = false;
        boolean cluster3Found = false;
        for (Cluster<EuclideanIntegerPoint> cluster : clusters) {
            int[] center = cluster.getCenter().getPoint();
            if (center[0] < 0) {
                cluster1Found = true;
                Assert.assertEquals(8, cluster.getPoints().size());
                Assert.assertEquals(-14, center[0]);
                Assert.assertEquals( 4, center[1]);
            } else if (center[1] < 0) {
                cluster2Found = true;
                Assert.assertEquals(5, cluster.getPoints().size());
                Assert.assertEquals( 0, center[0]);
                Assert.assertEquals(-1, center[1]);
            } else {
                cluster3Found = true;
                Assert.assertEquals(8, cluster.getPoints().size());
                Assert.assertEquals(15, center[0]);
                Assert.assertEquals(5, center[1]);
            }
        }
        Assert.assertTrue(cluster1Found);
        Assert.assertTrue(cluster2Found);
        Assert.assertTrue(cluster3Found);

    }

// org.apache.commons.math3.stat.clustering.KMeansPlusPlusClustererTest::testPerformClusterAnalysisDegenerate
    public void testPerformClusterAnalysisDegenerate() {
        KMeansPlusPlusClusterer<EuclideanIntegerPoint> transformer = new KMeansPlusPlusClusterer<EuclideanIntegerPoint>(
                new Random(1746432956321l));
        EuclideanIntegerPoint[] points = new EuclideanIntegerPoint[] {
                new EuclideanIntegerPoint(new int[] { 1959, 325100 }),
                new EuclideanIntegerPoint(new int[] { 1960, 373200 }), };
        List<Cluster<EuclideanIntegerPoint>> clusters = transformer.cluster(Arrays.asList(points), 1, 1);
        Assert.assertEquals(1, clusters.size());
        Assert.assertEquals(2, (clusters.get(0).getPoints().size()));
        EuclideanIntegerPoint pt1 = new EuclideanIntegerPoint(new int[] { 1959, 325100 });
        EuclideanIntegerPoint pt2 = new EuclideanIntegerPoint(new int[] { 1960, 373200 });
        Assert.assertTrue(clusters.get(0).getPoints().contains(pt1));
        Assert.assertTrue(clusters.get(0).getPoints().contains(pt2));

    }

// org.apache.commons.math3.stat.clustering.KMeansPlusPlusClustererTest::testCertainSpace
    public void testCertainSpace() {
        KMeansPlusPlusClusterer.EmptyClusterStrategy[] strategies = {
            KMeansPlusPlusClusterer.EmptyClusterStrategy.LARGEST_VARIANCE,
            KMeansPlusPlusClusterer.EmptyClusterStrategy.LARGEST_POINTS_NUMBER,
            KMeansPlusPlusClusterer.EmptyClusterStrategy.FARTHEST_POINT
        };
        for (KMeansPlusPlusClusterer.EmptyClusterStrategy strategy : strategies) {
            KMeansPlusPlusClusterer<EuclideanIntegerPoint> transformer =
                new KMeansPlusPlusClusterer<EuclideanIntegerPoint>(new Random(1746432956321l), strategy);
            int numberOfVariables = 27;
            
            int position1 = 1;
            int position2 = position1 + numberOfVariables;
            int position3 = position2 + numberOfVariables;
            int position4 = position3 + numberOfVariables;
            
            int multiplier = 1000000;

            EuclideanIntegerPoint[] breakingPoints = new EuclideanIntegerPoint[numberOfVariables];
            
            for (int i = 0; i < numberOfVariables; i++) {
                int points[] = { position1, position2, position3, position4 };
                
                for (int j = 0; j < points.length; j++) {
                    points[j] = points[j] * multiplier;
                }
                EuclideanIntegerPoint euclideanIntegerPoint = new EuclideanIntegerPoint(points);
                breakingPoints[i] = euclideanIntegerPoint;
                position1 = position1 + numberOfVariables;
                position2 = position2 + numberOfVariables;
                position3 = position3 + numberOfVariables;
                position4 = position4 + numberOfVariables;
            }

            for (int n = 2; n < 27; ++n) {
                List<Cluster<EuclideanIntegerPoint>> clusters =
                    transformer.cluster(Arrays.asList(breakingPoints), n, 100);
                Assert.assertEquals(n, clusters.size());
                int sum = 0;
                for (Cluster<EuclideanIntegerPoint> cluster : clusters) {
                    sum += cluster.getPoints().size();
                }
                Assert.assertEquals(numberOfVariables, sum);
            }
        }

    }

// org.apache.commons.math3.stat.clustering.KMeansPlusPlusClustererTest::testSmallDistances
    public void testSmallDistances() {
        
        
        int[] repeatedArray = { 0 };
        int[] uniqueArray = { 1 };
        CloseIntegerPoint repeatedPoint =
            new CloseIntegerPoint(new EuclideanIntegerPoint(repeatedArray));
        CloseIntegerPoint uniquePoint =
            new CloseIntegerPoint(new EuclideanIntegerPoint(uniqueArray));

        Collection<CloseIntegerPoint> points = new ArrayList<CloseIntegerPoint>();
        final int NUM_REPEATED_POINTS = 10 * 1000;
        for (int i = 0; i < NUM_REPEATED_POINTS; ++i) {
            points.add(repeatedPoint);
        }
        points.add(uniquePoint);

        
        
        final long RANDOM_SEED = 0;
        final int NUM_CLUSTERS = 2;
        final int NUM_ITERATIONS = 0;
        KMeansPlusPlusClusterer<CloseIntegerPoint> clusterer =
            new KMeansPlusPlusClusterer<CloseIntegerPoint>(new Random(RANDOM_SEED));
        List<Cluster<CloseIntegerPoint>> clusters =
            clusterer.cluster(points, NUM_CLUSTERS, NUM_ITERATIONS);

        
        boolean uniquePointIsCenter = false;
        for (Cluster<CloseIntegerPoint> cluster : clusters) {
            if (cluster.getCenter().equals(uniquePoint)) {
                uniquePointIsCenter = true;
            }
        }
        Assert.assertTrue(uniquePointIsCenter);
    }

// org.apache.commons.math3.stat.clustering.KMeansPlusPlusClustererTest::testPerformClusterAnalysisToManyClusters
    public void testPerformClusterAnalysisToManyClusters() {
        KMeansPlusPlusClusterer<EuclideanIntegerPoint> transformer = 
            new KMeansPlusPlusClusterer<EuclideanIntegerPoint>(
                    new Random(1746432956321l));
        
        EuclideanIntegerPoint[] points = new EuclideanIntegerPoint[] {
            new EuclideanIntegerPoint(new int[] {
                1959, 325100
            }), new EuclideanIntegerPoint(new int[] {
                1960, 373200
            })
        };
        
        transformer.cluster(Arrays.asList(points), 3, 1);

    }

// org.apache.commons.math3.stat.correlation.CovarianceTest::testLongly
    public void testLongly() {
        RealMatrix matrix = createRealMatrix(longleyData, 16, 7);
        RealMatrix covarianceMatrix = new Covariance(matrix).getCovarianceMatrix();
        double[] rData = new double[] {
         12333921.73333333246, 3.679666000000000e+04, 343330206.333333313,
         1649102.666666666744, 1117681.066666666651, 23461965.733333334, 16240.93333333333248,
         36796.66000000000, 1.164576250000000e+02, 1063604.115416667,
         6258.666250000000, 3490.253750000000, 73503.000000000, 50.92333333333334,
         343330206.33333331347, 1.063604115416667e+06, 9879353659.329166412,
         56124369.854166664183, 30880428.345833335072, 685240944.600000024, 470977.90000000002328,
         1649102.66666666674, 6.258666250000000e+03, 56124369.854166664,
         873223.429166666698, -115378.762499999997, 4462741.533333333, 2973.03333333333330,
         1117681.06666666665, 3.490253750000000e+03, 30880428.345833335,
         -115378.762499999997, 484304.095833333326, 1764098.133333333, 1382.43333333333339,
         23461965.73333333433, 7.350300000000000e+04, 685240944.600000024,
         4462741.533333333209, 1764098.133333333302, 48387348.933333330, 32917.40000000000146,
         16240.93333333333, 5.092333333333334e+01, 470977.900000000,
         2973.033333333333, 1382.433333333333, 32917.40000000, 22.66666666666667
        };

        TestUtils.assertEquals("covariance matrix", createRealMatrix(rData, 7, 7), covarianceMatrix, 10E-9);

    }

// org.apache.commons.math3.stat.correlation.CovarianceTest::testSwissFertility
    public void testSwissFertility() {
         RealMatrix matrix = createRealMatrix(swissData, 47, 5);
         RealMatrix covarianceMatrix = new Covariance(matrix).getCovarianceMatrix();
         double[] rData = new double[] {
           156.0424976873265, 100.1691489361702, -64.36692876965772, -79.7295097132285, 241.5632030527289,
           100.169148936170251, 515.7994172062905, -124.39283071230344, -139.6574005550416, 379.9043755781684,
           -64.3669287696577, -124.3928307123034, 63.64662349676226, 53.5758556891767, -190.5606105457909,
           -79.7295097132285, -139.6574005550416, 53.57585568917669, 92.4560592044403, -61.6988297872340,
            241.5632030527289, 379.9043755781684, -190.56061054579092, -61.6988297872340, 1739.2945371877890
         };

         TestUtils.assertEquals("covariance matrix", createRealMatrix(rData, 5, 5), covarianceMatrix, 10E-13);
    }

// org.apache.commons.math3.stat.correlation.CovarianceTest::testConstant
    public void testConstant() {
        double[] noVariance = new double[] {1, 1, 1, 1};
        double[] values = new double[] {1, 2, 3, 4};
        Assert.assertEquals(0d, new Covariance().covariance(noVariance, values, true), Double.MIN_VALUE);
        Assert.assertEquals(0d, new Covariance().covariance(noVariance, noVariance, true), Double.MIN_VALUE);
    }

// org.apache.commons.math3.stat.correlation.CovarianceTest::testInsufficientData
    public void testInsufficientData() {
        double[] one = new double[] {1};
        double[] two = new double[] {2};
        try {
            new Covariance().covariance(one, two, false);
            Assert.fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }
        RealMatrix matrix = new Array2DRowRealMatrix(new double[][] {{0},{1}});
        try {
            new Covariance(matrix);
            Assert.fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math3.stat.correlation.CovarianceTest::testConsistency
    public void testConsistency() {
        final RealMatrix matrix = createRealMatrix(swissData, 47, 5);
        final RealMatrix covarianceMatrix = new Covariance(matrix).getCovarianceMatrix();

        
        Variance variance = new Variance();
        for (int i = 0; i < 5; i++) {
            Assert.assertEquals(variance.evaluate(matrix.getColumn(i)), covarianceMatrix.getEntry(i,i), 10E-14);
        }

        
        Assert.assertEquals(covarianceMatrix.getEntry(2, 3),
                new Covariance().covariance(matrix.getColumn(2), matrix.getColumn(3), true), 10E-14);
        Assert.assertEquals(covarianceMatrix.getEntry(2, 3), covarianceMatrix.getEntry(3, 2), Double.MIN_VALUE);

        
        RealMatrix repeatedColumns = new Array2DRowRealMatrix(47, 3);
        for (int i = 0; i < 3; i++) {
            repeatedColumns.setColumnMatrix(i, matrix.getColumnMatrix(0));
        }
        RealMatrix repeatedCovarianceMatrix = new Covariance(repeatedColumns).getCovarianceMatrix();
        double columnVariance = variance.evaluate(matrix.getColumn(0));
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                Assert.assertEquals(columnVariance, repeatedCovarianceMatrix.getEntry(i, j), 10E-14);
            }
        }

        
        double[][] data = matrix.getData();
        TestUtils.assertEquals("Covariances",
                covarianceMatrix, new Covariance().computeCovarianceMatrix(data),Double.MIN_VALUE);
        TestUtils.assertEquals("Covariances",
                covarianceMatrix, new Covariance().computeCovarianceMatrix(data, true),Double.MIN_VALUE);

        double[] x = data[0];
        double[] y = data[1];
        Assert.assertEquals(new Covariance().covariance(x, y),
                new Covariance().covariance(x, y, true), Double.MIN_VALUE);
    }

// org.apache.commons.math3.stat.correlation.PearsonsCorrelationTest::testLongly
    public void testLongly() {
        RealMatrix matrix = createRealMatrix(longleyData, 16, 7);
        PearsonsCorrelation corrInstance = new PearsonsCorrelation(matrix);
        RealMatrix correlationMatrix = corrInstance.getCorrelationMatrix();
        double[] rData = new double[] {
                1.000000000000000, 0.9708985250610560, 0.9835516111796693, 0.5024980838759942,
                0.4573073999764817, 0.960390571594376, 0.9713294591921188,
                0.970898525061056, 1.0000000000000000, 0.9915891780247822, 0.6206333925590966,
                0.4647441876006747, 0.979163432977498, 0.9911491900672053,
                0.983551611179669, 0.9915891780247822, 1.0000000000000000, 0.6042609398895580,
                0.4464367918926265, 0.991090069458478, 0.9952734837647849,
                0.502498083875994, 0.6206333925590966, 0.6042609398895580, 1.0000000000000000,
                -0.1774206295018783, 0.686551516365312, 0.6682566045621746,
                0.457307399976482, 0.4647441876006747, 0.4464367918926265, -0.1774206295018783,
                1.0000000000000000, 0.364416267189032, 0.4172451498349454,
                0.960390571594376, 0.9791634329774981, 0.9910900694584777, 0.6865515163653120,
                0.3644162671890320, 1.000000000000000, 0.9939528462329257,
                0.971329459192119, 0.9911491900672053, 0.9952734837647849, 0.6682566045621746,
                0.4172451498349454, 0.993952846232926, 1.0000000000000000
        };
        TestUtils.assertEquals("correlation matrix", createRealMatrix(rData, 7, 7), correlationMatrix, 10E-15);

        double[] rPvalues = new double[] {
                4.38904690369668e-10,
                8.36353208910623e-12, 7.8159700933611e-14,
                0.0472894097790304, 0.01030636128354301, 0.01316878049026582,
                0.0749178049642416, 0.06971758330341182, 0.0830166169296545, 0.510948586323452,
                3.693245043123738e-09, 4.327782576751815e-11, 1.167954621905665e-13, 0.00331028281967516, 0.1652293725106684,
                3.95834476307755e-10, 1.114663916723657e-13, 1.332267629550188e-15, 0.00466039138541463, 0.1078477071581498, 7.771561172376096e-15
        };
        RealMatrix rPMatrix = createLowerTriangularRealMatrix(rPvalues, 7);
        fillUpper(rPMatrix, 0d);
        TestUtils.assertEquals("correlation p values", rPMatrix, corrInstance.getCorrelationPValues(), 10E-15);
    }

// org.apache.commons.math3.stat.correlation.PearsonsCorrelationTest::testSwissFertility
    public void testSwissFertility() {
         RealMatrix matrix = createRealMatrix(swissData, 47, 5);
         PearsonsCorrelation corrInstance = new PearsonsCorrelation(matrix);
         RealMatrix correlationMatrix = corrInstance.getCorrelationMatrix();
         double[] rData = new double[] {
               1.0000000000000000, 0.3530791836199747, -0.6458827064572875, -0.6637888570350691,  0.4636847006517939,
                 0.3530791836199747, 1.0000000000000000,-0.6865422086171366, -0.6395225189483201, 0.4010950530487398,
                -0.6458827064572875, -0.6865422086171366, 1.0000000000000000, 0.6984152962884830, -0.5727418060641666,
                -0.6637888570350691, -0.6395225189483201, 0.6984152962884830, 1.0000000000000000, -0.1538589170909148,
                 0.4636847006517939, 0.4010950530487398, -0.5727418060641666, -0.1538589170909148, 1.0000000000000000
         };
         TestUtils.assertEquals("correlation matrix", createRealMatrix(rData, 5, 5), correlationMatrix, 10E-15);

         double[] rPvalues = new double[] {
                 0.01491720061472623,
                 9.45043734069043e-07, 9.95151527133974e-08,
                 3.658616965962355e-07, 1.304590105694471e-06, 4.811397236181847e-08,
                 0.001028523190118147, 0.005204433539191644, 2.588307925380906e-05, 0.301807756132683
         };
         RealMatrix rPMatrix = createLowerTriangularRealMatrix(rPvalues, 5);
         fillUpper(rPMatrix, 0d);
         TestUtils.assertEquals("correlation p values", rPMatrix, corrInstance.getCorrelationPValues(), 10E-15);
    }

// org.apache.commons.math3.stat.correlation.PearsonsCorrelationTest::testPValueNearZero
    public void testPValueNearZero() {
        
        int dimension = 120;
        double[][] data = new double[dimension][2];
        for (int i = 0; i < dimension; i++) {
            data[i][0] = i;
            data[i][1] = i + 1/((double)i + 1);
        }
        PearsonsCorrelation corrInstance = new PearsonsCorrelation(data);
        Assert.assertTrue(corrInstance.getCorrelationPValues().getEntry(0, 1) > 0);
    }

// org.apache.commons.math3.stat.correlation.PearsonsCorrelationTest::testConstant
    public void testConstant() {
        double[] noVariance = new double[] {1, 1, 1, 1};
        double[] values = new double[] {1, 2, 3, 4};
        Assert.assertTrue(Double.isNaN(new PearsonsCorrelation().correlation(noVariance, values)));
    }

// org.apache.commons.math3.stat.correlation.PearsonsCorrelationTest::testInsufficientData
    public void testInsufficientData() {
        double[] one = new double[] {1};
        double[] two = new double[] {2};
        try {
            new PearsonsCorrelation().correlation(one, two);
            Assert.fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }
        RealMatrix matrix = new BlockRealMatrix(new double[][] {{0},{1}});
        try {
            new PearsonsCorrelation(matrix);
            Assert.fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math3.stat.correlation.PearsonsCorrelationTest::testStdErrorConsistency
    public void testStdErrorConsistency() {
        TDistribution tDistribution = new TDistribution(45);
        RealMatrix matrix = createRealMatrix(swissData, 47, 5);
        PearsonsCorrelation corrInstance = new PearsonsCorrelation(matrix);
        RealMatrix rValues = corrInstance.getCorrelationMatrix();
        RealMatrix pValues = corrInstance.getCorrelationPValues();
        RealMatrix stdErrors = corrInstance.getCorrelationStandardErrors();
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < i; j++) {
                double t = FastMath.abs(rValues.getEntry(i, j)) / stdErrors.getEntry(i, j);
                double p = 2 * (1 - tDistribution.cumulativeProbability(t));
                Assert.assertEquals(p, pValues.getEntry(i, j), 10E-15);
            }
        }
    }

// org.apache.commons.math3.stat.correlation.PearsonsCorrelationTest::testCovarianceConsistency
    public void testCovarianceConsistency() {
        RealMatrix matrix = createRealMatrix(longleyData, 16, 7);
        PearsonsCorrelation corrInstance = new PearsonsCorrelation(matrix);
        Covariance covInstance = new Covariance(matrix);
        PearsonsCorrelation corrFromCovInstance = new PearsonsCorrelation(covInstance);
        TestUtils.assertEquals("correlation values", corrInstance.getCorrelationMatrix(),
                corrFromCovInstance.getCorrelationMatrix(), 10E-15);
        TestUtils.assertEquals("p values", corrInstance.getCorrelationPValues(),
                corrFromCovInstance.getCorrelationPValues(), 10E-15);
        TestUtils.assertEquals("standard errors", corrInstance.getCorrelationStandardErrors(),
                corrFromCovInstance.getCorrelationStandardErrors(), 10E-15);

        PearsonsCorrelation corrFromCovInstance2 =
            new PearsonsCorrelation(covInstance.getCovarianceMatrix(), 16);
        TestUtils.assertEquals("correlation values", corrInstance.getCorrelationMatrix(),
                corrFromCovInstance2.getCorrelationMatrix(), 10E-15);
        TestUtils.assertEquals("p values", corrInstance.getCorrelationPValues(),
                corrFromCovInstance2.getCorrelationPValues(), 10E-15);
        TestUtils.assertEquals("standard errors", corrInstance.getCorrelationStandardErrors(),
                corrFromCovInstance2.getCorrelationStandardErrors(), 10E-15);
    }

// org.apache.commons.math3.stat.correlation.PearsonsCorrelationTest::testConsistency
    public void testConsistency() {
        RealMatrix matrix = createRealMatrix(longleyData, 16, 7);
        PearsonsCorrelation corrInstance = new PearsonsCorrelation(matrix);
        double[][] data = matrix.getData();
        double[] x = matrix.getColumn(0);
        double[] y = matrix.getColumn(1);
        Assert.assertEquals(new PearsonsCorrelation().correlation(x, y),
                corrInstance.getCorrelationMatrix().getEntry(0, 1), Double.MIN_VALUE);
        TestUtils.assertEquals("Correlation matrix", corrInstance.getCorrelationMatrix(),
                new PearsonsCorrelation().computeCorrelationMatrix(data), Double.MIN_VALUE);
    }

// org.apache.commons.math3.stat.correlation.SpearmansRankCorrelationTest::testLongly
    public void testLongly() {
        RealMatrix matrix = createRealMatrix(longleyData, 16, 7);
        SpearmansCorrelation corrInstance = new SpearmansCorrelation(matrix);
        RealMatrix correlationMatrix = corrInstance.getCorrelationMatrix();
        double[] rData = new double[] {
                1, 0.982352941176471, 0.985294117647059, 0.564705882352941, 0.2264705882352941, 0.976470588235294,
                0.976470588235294, 0.982352941176471, 1, 0.997058823529412, 0.664705882352941, 0.2205882352941176,
                0.997058823529412, 0.997058823529412, 0.985294117647059, 0.997058823529412, 1, 0.638235294117647,
                0.2235294117647059, 0.9941176470588236, 0.9941176470588236, 0.564705882352941, 0.664705882352941,
                0.638235294117647, 1, -0.3411764705882353, 0.685294117647059, 0.685294117647059, 0.2264705882352941,
                0.2205882352941176, 0.2235294117647059, -0.3411764705882353, 1, 0.2264705882352941, 0.2264705882352941,
                0.976470588235294, 0.997058823529412, 0.9941176470588236, 0.685294117647059, 0.2264705882352941, 1, 1,
                0.976470588235294, 0.997058823529412, 0.9941176470588236, 0.685294117647059, 0.2264705882352941, 1, 1
        };
        TestUtils.assertEquals("Spearman's correlation matrix", createRealMatrix(rData, 7, 7), correlationMatrix, 10E-15);
    }

// org.apache.commons.math3.stat.correlation.SpearmansRankCorrelationTest::testSwiss
    public void testSwiss() {
        RealMatrix matrix = createRealMatrix(swissData, 47, 5);
        SpearmansCorrelation corrInstance = new SpearmansCorrelation(matrix);
        RealMatrix correlationMatrix = corrInstance.getCorrelationMatrix();
        double[] rData = new double[] {
                1, 0.2426642769364176, -0.660902996352354, -0.443257690360988, 0.4136455623012432,
                0.2426642769364176, 1, -0.598859938748963, -0.650463814145816, 0.2886878090882852,
               -0.660902996352354, -0.598859938748963, 1, 0.674603831406147, -0.4750575257171745,
               -0.443257690360988, -0.650463814145816, 0.674603831406147, 1, -0.1444163088302244,
                0.4136455623012432, 0.2886878090882852, -0.4750575257171745, -0.1444163088302244, 1
        };
        TestUtils.assertEquals("Spearman's correlation matrix", createRealMatrix(rData, 5, 5), correlationMatrix, 10E-15);
    }

// org.apache.commons.math3.stat.correlation.SpearmansRankCorrelationTest::testConstant
    public void testConstant() {
        double[] noVariance = new double[] {1, 1, 1, 1};
        double[] values = new double[] {1, 2, 3, 4};
        Assert.assertTrue(Double.isNaN(new SpearmansCorrelation().correlation(noVariance, values)));
    }

// org.apache.commons.math3.stat.correlation.SpearmansRankCorrelationTest::testInsufficientData
    public void testInsufficientData() {
        double[] one = new double[] {1};
        double[] two = new double[] {2};
        try {
            new SpearmansCorrelation().correlation(one, two);
            Assert.fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }
        RealMatrix matrix = new BlockRealMatrix(new double[][] {{0},{1}});
        try {
            new SpearmansCorrelation(matrix);
            Assert.fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math3.stat.correlation.SpearmansRankCorrelationTest::testConsistency
    public void testConsistency() {
        RealMatrix matrix = createRealMatrix(longleyData, 16, 7);
        SpearmansCorrelation corrInstance = new SpearmansCorrelation(matrix);
        double[][] data = matrix.getData();
        double[] x = matrix.getColumn(0);
        double[] y = matrix.getColumn(1);
        Assert.assertEquals(new SpearmansCorrelation().correlation(x, y),
                corrInstance.getCorrelationMatrix().getEntry(0, 1), Double.MIN_VALUE);
        TestUtils.assertEquals("Correlation matrix", corrInstance.getCorrelationMatrix(),
                new SpearmansCorrelation().computeCorrelationMatrix(data), Double.MIN_VALUE);
    }

// org.apache.commons.math3.stat.correlation.SpearmansRankCorrelationTest::testStdErrorConsistency
    public void testStdErrorConsistency() {}

// org.apache.commons.math3.stat.correlation.SpearmansRankCorrelationTest::testCovarianceConsistency
    public void testCovarianceConsistency() {}

// org.apache.commons.math3.stat.correlation.StorelessCovarianceTest::testLonglySimpleVar
    public void testLonglySimpleVar(){
        double rCov = 12333921.73333333246;
        StorelessBivariateCovariance cov = new StorelessBivariateCovariance();
        for(int i=0;i<longleyDataSimple.length;i++){
            cov.increment(longleyDataSimple[i][0],longleyDataSimple[i][0]);
        }
        TestUtils.assertEquals("simple covariance test", rCov, cov.getResult(), 10E-7);
    }

// org.apache.commons.math3.stat.correlation.StorelessCovarianceTest::testLonglySimpleCov
    public void testLonglySimpleCov(){
        double rCov = 36796.660000;
        StorelessBivariateCovariance cov = new StorelessBivariateCovariance();
        for(int i=0;i<longleyDataSimple.length;i++){
            cov.increment(longleyDataSimple[i][0], longleyDataSimple[i][1]);
        }
        TestUtils.assertEquals("simple covariance test", rCov, cov.getResult(), 10E-7);
    }

// org.apache.commons.math3.stat.correlation.StorelessCovarianceTest::testLonglyByRow
    public void testLonglyByRow() {
        RealMatrix matrix = createRealMatrix(longleyData, 16, 7);

        double[] rData = new double[] {
         12333921.73333333246, 3.679666000000000e+04, 343330206.333333313,
         1649102.666666666744, 1117681.066666666651, 23461965.733333334, 16240.93333333333248,
         36796.66000000000, 1.164576250000000e+02, 1063604.115416667,
         6258.666250000000, 3490.253750000000, 73503.000000000, 50.92333333333334,
         343330206.33333331347, 1.063604115416667e+06, 9879353659.329166412,
         56124369.854166664183, 30880428.345833335072, 685240944.600000024, 470977.90000000002328,
         1649102.66666666674, 6.258666250000000e+03, 56124369.854166664,
         873223.429166666698, -115378.762499999997, 4462741.533333333, 2973.03333333333330,
         1117681.06666666665, 3.490253750000000e+03, 30880428.345833335,
         -115378.762499999997, 484304.095833333326, 1764098.133333333, 1382.43333333333339,
         23461965.73333333433, 7.350300000000000e+04, 685240944.600000024,
         4462741.533333333209, 1764098.133333333302, 48387348.933333330, 32917.40000000000146,
         16240.93333333333, 5.092333333333334e+01, 470977.900000000,
         2973.033333333333, 1382.433333333333, 32917.40000000, 22.66666666666667
        };

        StorelessCovariance covMatrix = new StorelessCovariance(7);
        for(int i=0;i<matrix.getRowDimension();i++){
            covMatrix.increment(matrix.getRow(i));
        }

        RealMatrix covarianceMatrix = covMatrix.getCovarianceMatrix();

        TestUtils.assertEquals("covariance matrix", createRealMatrix(rData, 7, 7), covarianceMatrix, 10E-7);

    }

// org.apache.commons.math3.stat.correlation.StorelessCovarianceTest::testSwissFertilityByRow
    public void testSwissFertilityByRow() {
         RealMatrix matrix = createRealMatrix(swissData, 47, 5);

         double[] rData = new double[] {
           156.0424976873265, 100.1691489361702, -64.36692876965772, -79.7295097132285, 241.5632030527289,
           100.169148936170251, 515.7994172062905, -124.39283071230344, -139.6574005550416, 379.9043755781684,
           -64.3669287696577, -124.3928307123034, 63.64662349676226, 53.5758556891767, -190.5606105457909,
           -79.7295097132285, -139.6574005550416, 53.57585568917669, 92.4560592044403, -61.6988297872340,
            241.5632030527289, 379.9043755781684, -190.56061054579092, -61.6988297872340, 1739.2945371877890
         };

        StorelessCovariance covMatrix = new StorelessCovariance(5);
        for(int i=0;i<matrix.getRowDimension();i++){
            covMatrix.increment(matrix.getRow(i));
        }

        RealMatrix covarianceMatrix = covMatrix.getCovarianceMatrix();

        TestUtils.assertEquals("covariance matrix", createRealMatrix(rData, 5, 5), covarianceMatrix, 10E-13);
    }

// org.apache.commons.math3.stat.correlation.StorelessCovarianceTest::testSymmetry
    public void testSymmetry() {
        RealMatrix matrix = createRealMatrix(swissData, 47, 5);

        final int dimension = 5;
        StorelessCovariance storelessCov = new StorelessCovariance(dimension);
        for(int i=0;i<matrix.getRowDimension();i++){
            storelessCov.increment(matrix.getRow(i));
        }

        double[][] covMatrix = storelessCov.getData();
        for (int i = 0; i < dimension; i++) {
            for (int j = i; j < dimension; j++) {
                Assert.assertEquals(covMatrix[i][j], covMatrix[j][i], 10e-9);
            }
        }
    }

// org.apache.commons.math3.stat.descriptive.AggregateSummaryStatisticsTest::testAggregation
    public void testAggregation() {
        AggregateSummaryStatistics aggregate = new AggregateSummaryStatistics();
        SummaryStatistics setOneStats = aggregate.createContributingStatistics();
        SummaryStatistics setTwoStats = aggregate.createContributingStatistics();

        Assert.assertNotNull("The set one contributing stats are null", setOneStats);
        Assert.assertNotNull("The set two contributing stats are null", setTwoStats);
        Assert.assertNotSame("Contributing stats objects are the same", setOneStats, setTwoStats);

        setOneStats.addValue(2);
        setOneStats.addValue(3);
        setOneStats.addValue(5);
        setOneStats.addValue(7);
        setOneStats.addValue(11);
        Assert.assertEquals("Wrong number of set one values", 5, setOneStats.getN());
        Assert.assertTrue("Wrong sum of set one values", Precision.equals(28.0, setOneStats.getSum(), 1));

        setTwoStats.addValue(2);
        setTwoStats.addValue(4);
        setTwoStats.addValue(8);
        Assert.assertEquals("Wrong number of set two values", 3, setTwoStats.getN());
        Assert.assertTrue("Wrong sum of set two values", Precision.equals(14.0, setTwoStats.getSum(), 1));

        Assert.assertEquals("Wrong number of aggregate values", 8, aggregate.getN());
        Assert.assertTrue("Wrong aggregate sum", Precision.equals(42.0, aggregate.getSum(), 1));
    }

// org.apache.commons.math3.stat.descriptive.AggregateSummaryStatisticsTest::testAggregationConsistency
    public void testAggregationConsistency() {

        
        double[] totalSample = generateSample();
        double[][] subSamples = generatePartition(totalSample);
        int nSamples = subSamples.length;

        
        AggregateSummaryStatistics aggregate = new AggregateSummaryStatistics();
        SummaryStatistics totalStats = new SummaryStatistics();

        
        SummaryStatistics componentStats[] = new SummaryStatistics[nSamples];

        for (int i = 0; i < nSamples; i++) {

            
            componentStats[i] = aggregate.createContributingStatistics();

            
            for (int j = 0; j < subSamples[i].length; j++) {
                componentStats[i].addValue(subSamples[i][j]);
            }
        }

        
        for (int i = 0; i < totalSample.length; i++) {
            totalStats.addValue(totalSample[i]);
        }

        
        Assert.assertEquals(totalStats.getSummary(), aggregate.getSummary());

    }

// org.apache.commons.math3.stat.descriptive.AggregateSummaryStatisticsTest::testAggregate
    public void testAggregate() {

        
        double[] totalSample = generateSample();
        double[][] subSamples = generatePartition(totalSample);
        int nSamples = subSamples.length;

        
        SummaryStatistics totalStats = new SummaryStatistics();
        for (int i = 0; i < totalSample.length; i++) {
            totalStats.addValue(totalSample[i]);
        }

        
        SummaryStatistics[] subSampleStats = new SummaryStatistics[nSamples];
        for (int i = 0; i < nSamples; i++) {
            subSampleStats[i] = new SummaryStatistics();
        }
        Collection<SummaryStatistics> aggregate = new ArrayList<SummaryStatistics>();
        for (int i = 0; i < nSamples; i++) {
            for (int j = 0; j < subSamples[i].length; j++) {
                subSampleStats[i].addValue(subSamples[i][j]);
            }
            aggregate.add(subSampleStats[i]);
        }

        
        StatisticalSummary aggregatedStats = AggregateSummaryStatistics.aggregate(aggregate);
        assertEquals(totalStats.getSummary(), aggregatedStats, 10E-12);
    }

// org.apache.commons.math3.stat.descriptive.AggregateSummaryStatisticsTest::testAggregateDegenerate
    public void testAggregateDegenerate() {
        double[] totalSample = {1, 2, 3, 4, 5};
        double[][] subSamples = {{1}, {2}, {3}, {4}, {5}};

        
        SummaryStatistics totalStats = new SummaryStatistics();
        for (int i = 0; i < totalSample.length; i++) {
            totalStats.addValue(totalSample[i]);
        }

        
        SummaryStatistics[] subSampleStats = new SummaryStatistics[5];
        for (int i = 0; i < 5; i++) {
            subSampleStats[i] = new SummaryStatistics();
        }
        Collection<SummaryStatistics> aggregate = new ArrayList<SummaryStatistics>();
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < subSamples[i].length; j++) {
                subSampleStats[i].addValue(subSamples[i][j]);
            }
            aggregate.add(subSampleStats[i]);
        }

        
        StatisticalSummaryValues aggregatedStats = AggregateSummaryStatistics.aggregate(aggregate);
        assertEquals(totalStats.getSummary(), aggregatedStats, 10E-12);
    }

// org.apache.commons.math3.stat.descriptive.AggregateSummaryStatisticsTest::testAggregateSpecialValues
    public void testAggregateSpecialValues() {
        double[] totalSample = {Double.POSITIVE_INFINITY, 2, 3, Double.NaN, 5};
        double[][] subSamples = {{Double.POSITIVE_INFINITY, 2}, {3}, {Double.NaN}, {5}};

        
        SummaryStatistics totalStats = new SummaryStatistics();
        for (int i = 0; i < totalSample.length; i++) {
            totalStats.addValue(totalSample[i]);
        }

        
        SummaryStatistics[] subSampleStats = new SummaryStatistics[5];
        for (int i = 0; i < 4; i++) {
            subSampleStats[i] = new SummaryStatistics();
        }
        Collection<SummaryStatistics> aggregate = new ArrayList<SummaryStatistics>();
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < subSamples[i].length; j++) {
                subSampleStats[i].addValue(subSamples[i][j]);
            }
            aggregate.add(subSampleStats[i]);
        }

        
        StatisticalSummaryValues aggregatedStats = AggregateSummaryStatistics.aggregate(aggregate);
        assertEquals(totalStats.getSummary(), aggregatedStats, 10E-12);

    }

// org.apache.commons.math3.stat.descriptive.DescriptiveStatisticsTest::testSetterInjection
    public void testSetterInjection() {
        DescriptiveStatistics stats = createDescriptiveStatistics();
        stats.addValue(1);
        stats.addValue(3);
        Assert.assertEquals(2, stats.getMean(), 1E-10);
        
        stats.setMeanImpl(new deepMean());
        Assert.assertEquals(42, stats.getMean(), 1E-10);
    }

// org.apache.commons.math3.stat.descriptive.DescriptiveStatisticsTest::testCopy
    public void testCopy() {
        DescriptiveStatistics stats = createDescriptiveStatistics();
        stats.addValue(1);
        stats.addValue(3);
        DescriptiveStatistics copy = new DescriptiveStatistics(stats);
        Assert.assertEquals(2, copy.getMean(), 1E-10);
        
        stats.setMeanImpl(new deepMean());
        copy = stats.copy();
        Assert.assertEquals(42, copy.getMean(), 1E-10);
    }

// org.apache.commons.math3.stat.descriptive.DescriptiveStatisticsTest::testWindowSize
    public void testWindowSize() {
        DescriptiveStatistics stats = createDescriptiveStatistics();
        stats.setWindowSize(300);
        for (int i = 0; i < 100; ++i) {
            stats.addValue(i + 1);
        }
        int refSum = (100 * 101) / 2;
        Assert.assertEquals(refSum / 100.0, stats.getMean(), 1E-10);
        Assert.assertEquals(300, stats.getWindowSize());
        try {
            stats.setWindowSize(-3);
            Assert.fail("an exception should have been thrown");
        } catch (IllegalArgumentException iae) {
            
        }
        Assert.assertEquals(300, stats.getWindowSize());
        stats.setWindowSize(50);
        Assert.assertEquals(50, stats.getWindowSize());
        int refSum2 = refSum - (50 * 51) / 2;
        Assert.assertEquals(refSum2 / 50.0, stats.getMean(), 1E-10);
    }

// org.apache.commons.math3.stat.descriptive.DescriptiveStatisticsTest::testGetValues
    public void testGetValues() {
        DescriptiveStatistics stats = createDescriptiveStatistics();
        for (int i = 100; i > 0; --i) {
            stats.addValue(i);
        }
        int refSum = (100 * 101) / 2;
        Assert.assertEquals(refSum / 100.0, stats.getMean(), 1E-10);
        double[] v = stats.getValues();
        for (int i = 0; i < v.length; ++i) {
            Assert.assertEquals(100.0 - i, v[i], 1.0e-10);
        }
        double[] s = stats.getSortedValues();
        for (int i = 0; i < s.length; ++i) {
            Assert.assertEquals(i + 1.0, s[i], 1.0e-10);
        }
        Assert.assertEquals(12.0, stats.getElement(88), 1.0e-10);
    }

// org.apache.commons.math3.stat.descriptive.DescriptiveStatisticsTest::testToString
    public void testToString() {
        DescriptiveStatistics stats = createDescriptiveStatistics();
        stats.addValue(1);
        stats.addValue(2);
        stats.addValue(3);
        Locale d = Locale.getDefault();
        Locale.setDefault(Locale.US);
        Assert.assertEquals("DescriptiveStatistics:\n" +
                     "n: 3\n" +
                     "min: 1.0\n" +
                     "max: 3.0\n" +
                     "mean: 2.0\n" +
                     "std dev: 1.0\n" +
                     "median: 2.0\n" +
                     "skewness: 0.0\n" +
                     "kurtosis: NaN\n",  stats.toString());
        Locale.setDefault(d);
    }

// org.apache.commons.math3.stat.descriptive.DescriptiveStatisticsTest::testShuffledStatistics
    public void testShuffledStatistics() {
        
        
        
        DescriptiveStatistics reference = createDescriptiveStatistics();
        DescriptiveStatistics shuffled  = createDescriptiveStatistics();

        UnivariateStatistic tmp = shuffled.getGeometricMeanImpl();
        shuffled.setGeometricMeanImpl(shuffled.getMeanImpl());
        shuffled.setMeanImpl(shuffled.getKurtosisImpl());
        shuffled.setKurtosisImpl(shuffled.getSkewnessImpl());
        shuffled.setSkewnessImpl(shuffled.getVarianceImpl());
        shuffled.setVarianceImpl(shuffled.getMaxImpl());
        shuffled.setMaxImpl(shuffled.getMinImpl());
        shuffled.setMinImpl(shuffled.getSumImpl());
        shuffled.setSumImpl(shuffled.getSumsqImpl());
        shuffled.setSumsqImpl(tmp);

        for (int i = 100; i > 0; --i) {
            reference.addValue(i);
            shuffled.addValue(i);
        }

        Assert.assertEquals(reference.getMean(),          shuffled.getGeometricMean(), 1.0e-10);
        Assert.assertEquals(reference.getKurtosis(),      shuffled.getMean(),          1.0e-10);
        Assert.assertEquals(reference.getSkewness(),      shuffled.getKurtosis(), 1.0e-10);
        Assert.assertEquals(reference.getVariance(),      shuffled.getSkewness(), 1.0e-10);
        Assert.assertEquals(reference.getMax(),           shuffled.getVariance(), 1.0e-10);
        Assert.assertEquals(reference.getMin(),           shuffled.getMax(), 1.0e-10);
        Assert.assertEquals(reference.getSum(),           shuffled.getMin(), 1.0e-10);
        Assert.assertEquals(reference.getSumsq(),         shuffled.getSum(), 1.0e-10);
        Assert.assertEquals(reference.getGeometricMean(), shuffled.getSumsq(), 1.0e-10);

    }

// org.apache.commons.math3.stat.descriptive.DescriptiveStatisticsTest::testPercentileSetter
    public void testPercentileSetter() {
        DescriptiveStatistics stats = createDescriptiveStatistics();
        stats.addValue(1);
        stats.addValue(2);
        stats.addValue(3);
        Assert.assertEquals(2, stats.getPercentile(50.0), 1E-10);

        
        stats.setPercentileImpl(new goodPercentile());
        Assert.assertEquals(2, stats.getPercentile(50.0), 1E-10);

        
        stats.setPercentileImpl(new subPercentile());
        Assert.assertEquals(10.0, stats.getPercentile(10.0), 1E-10);

        
        try {
            stats.setPercentileImpl(new badPercentile());
            Assert.fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math3.stat.descriptive.DescriptiveStatisticsTest::test20090720
    public void test20090720() {
        DescriptiveStatistics descriptiveStatistics = new DescriptiveStatistics(100);
        for (int i = 0; i < 161; i++) {
            descriptiveStatistics.addValue(1.2);
        }
        descriptiveStatistics.clear();
        descriptiveStatistics.addValue(1.2);
        Assert.assertEquals(1, descriptiveStatistics.getN());
    }

// org.apache.commons.math3.stat.descriptive.DescriptiveStatisticsTest::testRemoval
    public void testRemoval() {

        final DescriptiveStatistics dstat = createDescriptiveStatistics();

        checkremoval(dstat, 1, 6.0, 0.0, Double.NaN);
        checkremoval(dstat, 3, 5.0, 3.0, 4.5);
        checkremoval(dstat, 6, 3.5, 2.5, 3.0);
        checkremoval(dstat, 9, 3.5, 2.5, 3.0);
        checkremoval(dstat, DescriptiveStatistics.INFINITE_WINDOW, 3.5, 2.5, 3.0);

    }

// org.apache.commons.math3.stat.descriptive.DescriptiveStatisticsTest::testSummaryConsistency
    public void testSummaryConsistency() {
        final DescriptiveStatistics dstats = new DescriptiveStatistics();
        final SummaryStatistics sstats = new SummaryStatistics();
        final int windowSize = 5;
        dstats.setWindowSize(windowSize);
        final double tol = 1E-12;
        for (int i = 0; i < 20; i++) {
            dstats.addValue(i);
            sstats.clear();
            double[] values = dstats.getValues();
            for (int j = 0; j < values.length; j++) {
                sstats.addValue(values[j]);
            }
            TestUtils.assertEquals(dstats.getMean(), sstats.getMean(), tol);
            TestUtils.assertEquals(new Mean().evaluate(values), dstats.getMean(), tol);
            TestUtils.assertEquals(dstats.getMax(), sstats.getMax(), tol);
            TestUtils.assertEquals(new Max().evaluate(values), dstats.getMax(), tol);
            TestUtils.assertEquals(dstats.getGeometricMean(), sstats.getGeometricMean(), tol);
            TestUtils.assertEquals(new GeometricMean().evaluate(values), dstats.getGeometricMean(), tol);
            TestUtils.assertEquals(dstats.getMin(), sstats.getMin(), tol);
            TestUtils.assertEquals(new Min().evaluate(values), dstats.getMin(), tol);
            TestUtils.assertEquals(dstats.getStandardDeviation(), sstats.getStandardDeviation(), tol);
            TestUtils.assertEquals(dstats.getVariance(), sstats.getVariance(), tol);
            TestUtils.assertEquals(new Variance().evaluate(values), dstats.getVariance(), tol);
            TestUtils.assertEquals(dstats.getSum(), sstats.getSum(), tol);
            TestUtils.assertEquals(new Sum().evaluate(values), dstats.getSum(), tol);
            TestUtils.assertEquals(dstats.getSumsq(), sstats.getSumsq(), tol);
            TestUtils.assertEquals(new SumOfSquares().evaluate(values), dstats.getSumsq(), tol);
            TestUtils.assertEquals(dstats.getPopulationVariance(), sstats.getPopulationVariance(), tol);
            TestUtils.assertEquals(new Variance(false).evaluate(values), dstats.getPopulationVariance(), tol);
        }
    }

// org.apache.commons.math3.stat.descriptive.ListUnivariateImplTest::testStats
    public void testStats() {
        List<Object> externalList = new ArrayList<Object>();

        DescriptiveStatistics u = new ListUnivariateImpl( externalList );

        Assert.assertEquals("total count",0,u.getN(),tolerance);
        u.addValue(one);
        u.addValue(two);
        u.addValue(two);
        u.addValue(three);
        Assert.assertEquals("N",n,u.getN(),tolerance);
        Assert.assertEquals("sum",sum,u.getSum(),tolerance);
        Assert.assertEquals("sumsq",sumSq,u.getSumsq(),tolerance);
        Assert.assertEquals("var",var,u.getVariance(),tolerance);
        Assert.assertEquals("std",std,u.getStandardDeviation(),tolerance);
        Assert.assertEquals("mean",mean,u.getMean(),tolerance);
        Assert.assertEquals("min",min,u.getMin(),tolerance);
        Assert.assertEquals("max",max,u.getMax(),tolerance);
        u.clear();
        Assert.assertEquals("total count",0,u.getN(),tolerance);
    }

// org.apache.commons.math3.stat.descriptive.ListUnivariateImplTest::testN0andN1Conditions
    public void testN0andN1Conditions() {
        List<Object> list = new ArrayList<Object>();

        DescriptiveStatistics u = new ListUnivariateImpl( list );

        Assert.assertTrue("Mean of n = 0 set should be NaN", Double.isNaN( u.getMean() ) );
        Assert.assertTrue("Standard Deviation of n = 0 set should be NaN", Double.isNaN( u.getStandardDeviation() ) );
        Assert.assertTrue("Variance of n = 0 set should be NaN", Double.isNaN(u.getVariance() ) );

        list.add( Double.valueOf(one));

        Assert.assertTrue( "Mean of n = 1 set should be value of single item n1", u.getMean() == one);
        Assert.assertTrue( "StdDev of n = 1 set should be zero, instead it is: " + u.getStandardDeviation(), u.getStandardDeviation() == 0);
        Assert.assertTrue( "Variance of n = 1 set should be zero", u.getVariance() == 0);
    }

// org.apache.commons.math3.stat.descriptive.ListUnivariateImplTest::testSkewAndKurtosis
    public void testSkewAndKurtosis() {
        DescriptiveStatistics u = new DescriptiveStatistics();

        double[] testArray = { 12.5, 12, 11.8, 14.2, 14.9, 14.5, 21, 8.2, 10.3, 11.3, 14.1,
                                             9.9, 12.2, 12, 12.1, 11, 19.8, 11, 10, 8.8, 9, 12.3 };
        for( int i = 0; i < testArray.length; i++) {
            u.addValue( testArray[i]);
        }

        Assert.assertEquals("mean", 12.40455, u.getMean(), 0.0001);
        Assert.assertEquals("variance", 10.00236, u.getVariance(), 0.0001);
        Assert.assertEquals("skewness", 1.437424, u.getSkewness(), 0.0001);
        Assert.assertEquals("kurtosis", 2.37719, u.getKurtosis(), 0.0001);
    }

// org.apache.commons.math3.stat.descriptive.ListUnivariateImplTest::testProductAndGeometricMean
    public void testProductAndGeometricMean() {
        ListUnivariateImpl u = new ListUnivariateImpl(new ArrayList<Object>());
        u.setWindowSize(10);

        u.addValue( 1.0 );
        u.addValue( 2.0 );
        u.addValue( 3.0 );
        u.addValue( 4.0 );

        Assert.assertEquals( "Geometric mean not expected", 2.213364, u.getGeometricMean(), 0.00001 );

        
        
        for( int i = 0; i < 10; i++ ) {
            u.addValue( i + 2 );
        }
        

        Assert.assertEquals( "Geometric mean not expected", 5.755931, u.getGeometricMean(), 0.00001 );

    }
