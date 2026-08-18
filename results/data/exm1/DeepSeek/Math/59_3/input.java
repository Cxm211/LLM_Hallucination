// buggy code
    public static float max(final float a, final float b) {
        return (a <= b) ? b : (Float.isNaN(a + b) ? Float.NaN : b);
    }

// relevant test
// org.apache.commons.math.optimization.MultiStartMultivariateRealOptimizerTest::testRosenbrock
    public void testRosenbrock() throws MathUserException {
        Rosenbrock rosenbrock = new Rosenbrock();
        SimplexOptimizer underlying = new SimplexOptimizer();
        NelderMeadSimplex simplex = new NelderMeadSimplex(new double[][] {
                { -1.2,  1.0 }, { 0.9, 1.2 } , {  3.5, -2.3 }
            });
        underlying.setSimplex(simplex);
        JDKRandomGenerator g = new JDKRandomGenerator();
        g.setSeed(16069223052l);
        RandomVectorGenerator generator =
            new UncorrelatedRandomVectorGenerator(2, new GaussianRandomGenerator(g));
        MultiStartMultivariateRealOptimizer optimizer =
            new MultiStartMultivariateRealOptimizer(underlying, 10, generator);
        optimizer.setConvergenceChecker(new SimpleScalarValueChecker(-1, 1.0e-3));
        RealPointValuePair optimum =
            optimizer.optimize(1100, rosenbrock, GoalType.MINIMIZE, new double[] { -1.2, 1.0 });

        assertEquals(rosenbrock.getCount(), optimizer.getEvaluations());
        assertTrue(optimizer.getEvaluations() > 900);
        assertTrue(optimizer.getEvaluations() < 1200);
        assertTrue(optimum.getValue() < 8.0e-4);
    }

// org.apache.commons.math.optimization.direct.PowellOptimizerTest::testSumSinc
    public void testSumSinc() {
        final MultivariateRealFunction func = new SumSincFunction(-1);

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
        doTest(func, minPoint, init,  GoalType.MINIMIZE, 1e-9, 1e-7);
    }

// org.apache.commons.math.optimization.direct.PowellOptimizerTest::testQuadratic
    public void testQuadratic() {
        final MultivariateRealFunction func = new MultivariateRealFunction() {
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

// org.apache.commons.math.optimization.direct.PowellOptimizerTest::testMaximizeQuadratic
    public void testMaximizeQuadratic() {
        final MultivariateRealFunction func = new MultivariateRealFunction() {
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

// org.apache.commons.math.optimization.direct.SimplexOptimizerMultiDirectionalTest::testMinimizeMaximize
    public void testMinimizeMaximize() {
        
        final double xM        = -3.841947088256863675365;
        final double yM        = -1.391745200270734924416;
        final double xP        =  0.2286682237349059125691;
        final double yP        = -yM;
        final double valueXmYm =  0.2373295333134216789769; 
        final double valueXmYp = -valueXmYm;                
        final double valueXpYm = -0.7290400707055187115322; 
        final double valueXpYp = -valueXpYm;                
        MultivariateRealFunction fourExtrema = new MultivariateRealFunction() {
                private static final long serialVersionUID = -7039124064449091152L;
                public double value(double[] variables) {
                    final double x = variables[0];
                    final double y = variables[1];
                    return ((x == 0) || (y == 0)) ? 0 :
                        (FastMath.atan(x) * FastMath.atan(x + 2) * FastMath.atan(y) * FastMath.atan(y) / (x * y));
                }
            };

        SimplexOptimizer optimizer = new SimplexOptimizer(1e-11, 1e-30);
        optimizer.setSimplex(new MultiDirectionalSimplex(new double[] { 0.2, 0.2 }));
        RealPointValuePair optimum;

        
        optimum = optimizer.optimize(200, fourExtrema, GoalType.MINIMIZE, new double[] { -3, 0 });
        Assert.assertEquals(xM,        optimum.getPoint()[0], 4e-6);
        Assert.assertEquals(yP,        optimum.getPoint()[1], 3e-6);
        Assert.assertEquals(valueXmYp, optimum.getValue(),    8e-13);
        Assert.assertTrue(optimizer.getEvaluations() > 120);
        Assert.assertTrue(optimizer.getEvaluations() < 150);

        optimum = optimizer.optimize(200, fourExtrema, GoalType.MINIMIZE, new double[] { 1, 0 });
        Assert.assertEquals(xP,        optimum.getPoint()[0], 2e-8);
        Assert.assertEquals(yM,        optimum.getPoint()[1], 3e-6);
        Assert.assertEquals(valueXpYm, optimum.getValue(),    2e-12);
        Assert.assertTrue(optimizer.getEvaluations() > 120);
        Assert.assertTrue(optimizer.getEvaluations() < 150);

        
        optimum = optimizer.optimize(200, fourExtrema, GoalType.MAXIMIZE, new double[] { -3.0, 0.0 });
        Assert.assertEquals(xM,        optimum.getPoint()[0], 7e-7);
        Assert.assertEquals(yM,        optimum.getPoint()[1], 3e-7);
        Assert.assertEquals(valueXmYm, optimum.getValue(),    2e-14);
        Assert.assertTrue(optimizer.getEvaluations() > 120);
        Assert.assertTrue(optimizer.getEvaluations() < 150);

        optimizer.setConvergenceChecker(new SimpleScalarValueChecker(1e-15, 1e-30));
        optimum = optimizer.optimize(200, fourExtrema, GoalType.MAXIMIZE, new double[] { 1, 0 });
        Assert.assertEquals(xP,        optimum.getPoint()[0], 2e-8);
        Assert.assertEquals(yP,        optimum.getPoint()[1], 3e-6);
        Assert.assertEquals(valueXpYp, optimum.getValue(),    2e-12);
        Assert.assertTrue(optimizer.getEvaluations() > 180);
        Assert.assertTrue(optimizer.getEvaluations() < 220);
    }

// org.apache.commons.math.optimization.direct.SimplexOptimizerMultiDirectionalTest::testRosenbrock
    public void testRosenbrock() {
        MultivariateRealFunction rosenbrock =
            new MultivariateRealFunction() {
                private static final long serialVersionUID = -9044950469615237490L;
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
        RealPointValuePair optimum =
            optimizer.optimize(100, rosenbrock, GoalType.MINIMIZE, new double[] { -1.2, 1 });

        Assert.assertEquals(count, optimizer.getEvaluations());
        Assert.assertTrue(optimizer.getEvaluations() > 50);
        Assert.assertTrue(optimizer.getEvaluations() < 100);
        Assert.assertTrue(optimum.getValue() > 1e-2);
    }

// org.apache.commons.math.optimization.direct.SimplexOptimizerMultiDirectionalTest::testPowell
    public void testPowell() {
        MultivariateRealFunction powell =
            new MultivariateRealFunction() {
                private static final long serialVersionUID = -832162886102041840L;
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
        RealPointValuePair optimum =
            optimizer.optimize(1000, powell, GoalType.MINIMIZE, new double[] { 3, -1, 0, 1 });
        Assert.assertEquals(count, optimizer.getEvaluations());
        Assert.assertTrue(optimizer.getEvaluations() > 800);
        Assert.assertTrue(optimizer.getEvaluations() < 900);
        Assert.assertTrue(optimum.getValue() > 1e-2);
    }

// org.apache.commons.math.optimization.direct.SimplexOptimizerMultiDirectionalTest::testMath283
    public void testMath283() {
        
        
        SimplexOptimizer optimizer = new SimplexOptimizer();
        optimizer.setSimplex(new MultiDirectionalSimplex(2));
        final Gaussian2D function = new Gaussian2D(0, 0, 1);
        RealPointValuePair estimate = optimizer.optimize(1000, function,
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

// org.apache.commons.math.optimization.direct.SimplexOptimizerNelderMeadTest::testMinimizeMaximize
    public void testMinimizeMaximize() {

        
        final double xM        = -3.841947088256863675365;
        final double yM        = -1.391745200270734924416;
        final double xP        =  0.2286682237349059125691;
        final double yP        = -yM;
        final double valueXmYm =  0.2373295333134216789769; 
        final double valueXmYp = -valueXmYm;                
        final double valueXpYm = -0.7290400707055187115322; 
        final double valueXpYp = -valueXpYm;                
        MultivariateRealFunction fourExtrema = new MultivariateRealFunction() {
                private static final long serialVersionUID = -7039124064449091152L;
                public double value(double[] variables) {
                    final double x = variables[0];
                    final double y = variables[1];
                    return (x == 0 || y == 0) ? 0 :
                        (Math.atan(x) * Math.atan(x + 2) * Math.atan(y) * Math.atan(y) / (x * y));
                }
            };

        SimplexOptimizer optimizer = new SimplexOptimizer(1e-10, 1e-30);
        optimizer.setSimplex(new NelderMeadSimplex(new double[] { 0.2, 0.2 }));
        RealPointValuePair optimum;

        
        optimum = optimizer.optimize(100, fourExtrema, GoalType.MINIMIZE, new double[] { -3, 0 });
        assertEquals(xM,        optimum.getPoint()[0], 2e-7);
        assertEquals(yP,        optimum.getPoint()[1], 2e-5);
        assertEquals(valueXmYp, optimum.getValue(),    6e-12);
        assertTrue(optimizer.getEvaluations() > 60);
        assertTrue(optimizer.getEvaluations() < 90);

        optimum = optimizer.optimize(100, fourExtrema, GoalType.MINIMIZE, new double[] { 1, 0 });
        assertEquals(xP,        optimum.getPoint()[0], 5e-6);
        assertEquals(yM,        optimum.getPoint()[1], 6e-6);
        assertEquals(valueXpYm, optimum.getValue(),    1e-11);
        assertTrue(optimizer.getEvaluations() > 60);
        assertTrue(optimizer.getEvaluations() < 90);

        
        optimum = optimizer.optimize(100, fourExtrema, GoalType.MAXIMIZE, new double[] { -3, 0 });
        assertEquals(xM,        optimum.getPoint()[0], 1e-5);
        assertEquals(yM,        optimum.getPoint()[1], 3e-6);
        assertEquals(valueXmYm, optimum.getValue(),    3e-12);
        assertTrue(optimizer.getEvaluations() > 60);
        assertTrue(optimizer.getEvaluations() < 90);

        optimum = optimizer.optimize(100, fourExtrema, GoalType.MAXIMIZE, new double[] { 1, 0 });
        assertEquals(xP,        optimum.getPoint()[0], 4e-6);
        assertEquals(yP,        optimum.getPoint()[1], 5e-6);
        assertEquals(valueXpYp, optimum.getValue(),    7e-12);
        assertTrue(optimizer.getEvaluations() > 60);
        assertTrue(optimizer.getEvaluations() < 90);
    }

// org.apache.commons.math.optimization.direct.SimplexOptimizerNelderMeadTest::testRosenbrock
    public void testRosenbrock() {

        Rosenbrock rosenbrock = new Rosenbrock();
        SimplexOptimizer optimizer = new SimplexOptimizer(-1, 1e-3);
        optimizer.setSimplex(new NelderMeadSimplex(new double[][] {
                    { -1.2,  1 }, { 0.9, 1.2 } , {  3.5, -2.3 }
                }));
        RealPointValuePair optimum =
            optimizer.optimize(100, rosenbrock, GoalType.MINIMIZE, new double[] { -1.2, 1 });

        assertEquals(rosenbrock.getCount(), optimizer.getEvaluations());
        assertTrue(optimizer.getEvaluations() > 40);
        assertTrue(optimizer.getEvaluations() < 50);
        assertTrue(optimum.getValue() < 8e-4);
    }

// org.apache.commons.math.optimization.direct.SimplexOptimizerNelderMeadTest::testPowell
    public void testPowell() {

        Powell powell = new Powell();
        SimplexOptimizer optimizer = new SimplexOptimizer(-1, 1e-3);
        optimizer.setSimplex(new NelderMeadSimplex(4));
        RealPointValuePair optimum =
            optimizer.optimize(200, powell, GoalType.MINIMIZE, new double[] { 3, -1, 0, 1 });
        assertEquals(powell.getCount(), optimizer.getEvaluations());
        assertTrue(optimizer.getEvaluations() > 110);
        assertTrue(optimizer.getEvaluations() < 130);
        assertTrue(optimum.getValue() < 2e-3);
    }

// org.apache.commons.math.optimization.direct.SimplexOptimizerNelderMeadTest::testLeastSquares1
    public void testLeastSquares1() {

        final RealMatrix factors =
            new Array2DRowRealMatrix(new double[][] {
                    { 1, 0 },
                    { 0, 1 }
                }, false);
        LeastSquaresConverter ls = new LeastSquaresConverter(new MultivariateVectorialFunction() {
                public double[] value(double[] variables) {
                    return factors.operate(variables);
                }
            }, new double[] { 2.0, -3.0 });
        SimplexOptimizer optimizer = new SimplexOptimizer(-1, 1e-6);
        optimizer.setSimplex(new NelderMeadSimplex(2));
        RealPointValuePair optimum =
            optimizer.optimize(200, ls, GoalType.MINIMIZE, new double[] { 10, 10 });
        assertEquals( 2, optimum.getPointRef()[0], 3e-5);
        assertEquals(-3, optimum.getPointRef()[1], 4e-4);
        assertTrue(optimizer.getEvaluations() > 60);
        assertTrue(optimizer.getEvaluations() < 80);
        assertTrue(optimum.getValue() < 1.0e-6);
    }

// org.apache.commons.math.optimization.direct.SimplexOptimizerNelderMeadTest::testLeastSquares2
    public void testLeastSquares2() {

        final RealMatrix factors =
            new Array2DRowRealMatrix(new double[][] {
                    { 1, 0 },
                    { 0, 1 }
                }, false);
        LeastSquaresConverter ls = new LeastSquaresConverter(new MultivariateVectorialFunction() {
                public double[] value(double[] variables) {
                    return factors.operate(variables);
                }
            }, new double[] { 2, -3 }, new double[] { 10, 0.1 });
        SimplexOptimizer optimizer = new SimplexOptimizer(-1, 1e-6);
        optimizer.setSimplex(new NelderMeadSimplex(2));
        RealPointValuePair optimum =
            optimizer.optimize(200, ls, GoalType.MINIMIZE, new double[] { 10, 10 });
        assertEquals( 2, optimum.getPointRef()[0], 5e-5);
        assertEquals(-3, optimum.getPointRef()[1], 8e-4);
        assertTrue(optimizer.getEvaluations() > 60);
        assertTrue(optimizer.getEvaluations() < 80);
        assertTrue(optimum.getValue() < 1e-6);
    }

// org.apache.commons.math.optimization.direct.SimplexOptimizerNelderMeadTest::testLeastSquares3
    public void testLeastSquares3() {

        final RealMatrix factors =
            new Array2DRowRealMatrix(new double[][] {
                    { 1, 0 },
                    { 0, 1 }
                }, false);
        LeastSquaresConverter ls = new LeastSquaresConverter(new MultivariateVectorialFunction() {
                public double[] value(double[] variables) {
                    return factors.operate(variables);
                }
            }, new double[] { 2, -3 }, new Array2DRowRealMatrix(new double [][] {
                    { 1, 1.2 }, { 1.2, 2 }
                }));
        SimplexOptimizer optimizer = new SimplexOptimizer(-1, 1e-6);
        optimizer.setSimplex(new NelderMeadSimplex(2));
        RealPointValuePair optimum =
            optimizer.optimize(200, ls, GoalType.MINIMIZE, new double[] { 10, 10 });
        assertEquals( 2, optimum.getPointRef()[0], 2e-3);
        assertEquals(-3, optimum.getPointRef()[1], 8e-4);
        assertTrue(optimizer.getEvaluations() > 60);
        assertTrue(optimizer.getEvaluations() < 80);
        assertTrue(optimum.getValue() < 1e-6);
    }

// org.apache.commons.math.optimization.direct.SimplexOptimizerNelderMeadTest::testMaxIterations
    public void testMaxIterations() {
        Powell powell = new Powell();
        SimplexOptimizer optimizer = new SimplexOptimizer(-1, 1e-3);
        optimizer.setSimplex(new NelderMeadSimplex(4));
        optimizer.optimize(20, powell, GoalType.MINIMIZE, new double[] { 3, -1, 0, 1 });
    }

// org.apache.commons.math.optimization.fitting.CurveFitterTest::testMath303
    public void testMath303()
        throws MathUserException {

        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        CurveFitter fitter = new CurveFitter(optimizer);
        fitter.addObservedPoint(2.805d, 0.6934785852953367d);
        fitter.addObservedPoint(2.74333333333333d, 0.6306772025518496d);
        fitter.addObservedPoint(1.655d, 0.9474675497289684);
        fitter.addObservedPoint(1.725d, 0.9013594835804194d);

        ParametricRealFunction sif = new SimpleInverseFunction();

        double[] initialguess1 = new double[1];
        initialguess1[0] = 1.0d;
        Assert.assertEquals(1, fitter.fit(sif, initialguess1).length);

        double[] initialguess2 = new double[2];
        initialguess2[0] = 1.0d;
        initialguess2[1] = .5d;
        Assert.assertEquals(2, fitter.fit(sif, initialguess2).length);

    }

// org.apache.commons.math.optimization.fitting.CurveFitterTest::testMath304
    public void testMath304()
        throws MathUserException {

        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        CurveFitter fitter = new CurveFitter(optimizer);
        fitter.addObservedPoint(2.805d, 0.6934785852953367d);
        fitter.addObservedPoint(2.74333333333333d, 0.6306772025518496d);
        fitter.addObservedPoint(1.655d, 0.9474675497289684);
        fitter.addObservedPoint(1.725d, 0.9013594835804194d);

        ParametricRealFunction sif = new SimpleInverseFunction();

        double[] initialguess1 = new double[1];
        initialguess1[0] = 1.0d;
        Assert.assertEquals(1.6357215104109237, fitter.fit(sif, initialguess1)[0], 1.0e-14);

        double[] initialguess2 = new double[1];
        initialguess2[0] = 10.0d;
        Assert.assertEquals(1.6357215104109237, fitter.fit(sif, initialguess1)[0], 1.0e-14);

    }

// org.apache.commons.math.optimization.fitting.CurveFitterTest::testMath372
    public void testMath372()
    throws MathUserException {
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        CurveFitter curveFitter = new CurveFitter(optimizer);

        curveFitter.addObservedPoint( 15,  4443);
        curveFitter.addObservedPoint( 31,  8493);
        curveFitter.addObservedPoint( 62, 17586);
        curveFitter.addObservedPoint(125, 30582);
        curveFitter.addObservedPoint(250, 45087);
        curveFitter.addObservedPoint(500, 50683);

        ParametricRealFunction f = new ParametricRealFunction() {

            public double value(double x, double[] parameters) {

                double a = parameters[0];
                double b = parameters[1];
                double c = parameters[2];
                double d = parameters[3];

                return d + ((a - d) / (1 + FastMath.pow(x / c, b)));
            }

            public double[] gradient(double x, double[] parameters) {

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

// org.apache.commons.math.optimization.fitting.GaussianFitterTest::testFit01
    public void testFit01()
    throws OptimizationException {
        GaussianFitter fitter = new GaussianFitter(new LevenbergMarquardtOptimizer());
        addDatasetToGaussianFitter(DATASET1, fitter);
        GaussianFunction fitFunction = fitter.fit();
        assertEquals(99200.86969833552, fitFunction.getA(), 1e-4);
        assertEquals(3410515.285208688, fitFunction.getB(), 1e-4);
        assertEquals(4.054928275302832, fitFunction.getC(), 1e-4);
        assertEquals(0.014609868872574, fitFunction.getD(), 1e-4);
    }

// org.apache.commons.math.optimization.fitting.GaussianFitterTest::testFit02
    public void testFit02()
    throws OptimizationException {
        GaussianFitter fitter = new GaussianFitter(new LevenbergMarquardtOptimizer());
        fitter.fit();
    }

// org.apache.commons.math.optimization.fitting.GaussianFitterTest::testFit03
    public void testFit03()
    throws OptimizationException {
        GaussianFitter fitter = new GaussianFitter(new LevenbergMarquardtOptimizer());
        addDatasetToGaussianFitter(new double[][] {
            {4.0254623,  531026.0},
            {4.02804905, 664002.0}},
            fitter);
        fitter.fit();
    }

// org.apache.commons.math.optimization.fitting.GaussianFitterTest::testFit04
    public void testFit04()
    throws OptimizationException {
        GaussianFitter fitter = new GaussianFitter(new LevenbergMarquardtOptimizer());
        addDatasetToGaussianFitter(DATASET2, fitter);
        GaussianFunction fitFunction = fitter.fit();
        assertEquals(-256534.689445631, fitFunction.getA(), 1e-4);
        assertEquals(481328.2181530679, fitFunction.getB(), 1e-4);
        assertEquals(-10.5217226891099, fitFunction.getC(), 1e-4);
        assertEquals(-7.64248239366800, fitFunction.getD(), 1e-4);
    }

// org.apache.commons.math.optimization.fitting.GaussianFitterTest::testFit05
    public void testFit05()
    throws OptimizationException {
        GaussianFitter fitter = new GaussianFitter(new LevenbergMarquardtOptimizer());
        addDatasetToGaussianFitter(DATASET3, fitter);
        GaussianFunction fitFunction = fitter.fit();
        assertEquals(491.6310079258938, fitFunction.getA(), 1e-4);
        assertEquals(283508.6800413632, fitFunction.getB(), 1e-4);
        assertEquals(-13.2966857238057, fitFunction.getC(), 1e-4);
        assertEquals(1.725590356962981, fitFunction.getD(), 1e-4);
    }

// org.apache.commons.math.optimization.fitting.GaussianFitterTest::testFit06
    public void testFit06()
    throws OptimizationException {
        GaussianFitter fitter = new GaussianFitter(new LevenbergMarquardtOptimizer());
        addDatasetToGaussianFitter(DATASET4, fitter);
        GaussianFunction fitFunction = fitter.fit();
        assertEquals(530.3649792355617, fitFunction.getA(), 1e-4);
        assertEquals(284517.0835567514, fitFunction.getB(), 1e-4);
        assertEquals(-13.5355534565105, fitFunction.getC(), 1e-4);
        assertEquals(1.512353018625465, fitFunction.getD(), 1e-4);
    }

// org.apache.commons.math.optimization.fitting.GaussianFitterTest::testFit07
    public void testFit07()
    throws OptimizationException {
        GaussianFitter fitter = new GaussianFitter(new LevenbergMarquardtOptimizer());
        addDatasetToGaussianFitter(DATASET5, fitter);
        GaussianFunction fitFunction = fitter.fit();
        assertEquals(176748.1400947575, fitFunction.getA(), 1e-4);
        assertEquals(3361537.018813906, fitFunction.getB(), 1e-4);
        assertEquals(4.054949992747176, fitFunction.getC(), 1e-4);
        assertEquals(0.014192380137002, fitFunction.getD(), 1e-4);
    }

// org.apache.commons.math.optimization.fitting.HarmonicFitterTest::testNoError
    public void testNoError() throws OptimizationException {
        HarmonicFunction f = new HarmonicFunction(0.2, 3.4, 4.1);

        HarmonicFitter fitter =
            new HarmonicFitter(new LevenbergMarquardtOptimizer());
        for (double x = 0.0; x < 1.3; x += 0.01) {
            fitter.addObservedPoint(1.0, x, f.value(x));
        }

        HarmonicFunction fitted = fitter.fit();
        assertEquals(f.getAmplitude(), fitted.getAmplitude(), 1.0e-13);
        assertEquals(f.getPulsation(), fitted.getPulsation(), 1.0e-13);
        assertEquals(f.getPhase(),     MathUtils.normalizeAngle(fitted.getPhase(), f.getPhase()), 1.0e-13);

        for (double x = -1.0; x < 1.0; x += 0.01) {
            assertTrue(FastMath.abs(f.value(x) - fitted.value(x)) < 1.0e-13);
        }

    }

// org.apache.commons.math.optimization.fitting.HarmonicFitterTest::test1PercentError
    public void test1PercentError() throws OptimizationException {
        Random randomizer = new Random(64925784252l);
        HarmonicFunction f = new HarmonicFunction(0.2, 3.4, 4.1);

        HarmonicFitter fitter =
            new HarmonicFitter(new LevenbergMarquardtOptimizer());
        for (double x = 0.0; x < 10.0; x += 0.1) {
            fitter.addObservedPoint(1.0, x,
                                   f.value(x) + 0.01 * randomizer.nextGaussian());
        }

        HarmonicFunction fitted = fitter.fit();
        assertEquals(f.getAmplitude(), fitted.getAmplitude(), 7.6e-4);
        assertEquals(f.getPulsation(), fitted.getPulsation(), 2.7e-3);
        assertEquals(f.getPhase(),     MathUtils.normalizeAngle(fitted.getPhase(), f.getPhase()), 1.3e-2);

    }

// org.apache.commons.math.optimization.fitting.HarmonicFitterTest::testInitialGuess
    public void testInitialGuess() throws OptimizationException {
        Random randomizer = new Random(45314242l);
        HarmonicFunction f = new HarmonicFunction(0.2, 3.4, 4.1);

        HarmonicFitter fitter =
            new HarmonicFitter(new LevenbergMarquardtOptimizer(), new double[] { 0.15, 3.6, 4.5 });
        for (double x = 0.0; x < 10.0; x += 0.1) {
            fitter.addObservedPoint(1.0, x,
                                   f.value(x) + 0.01 * randomizer.nextGaussian());
        }

        HarmonicFunction fitted = fitter.fit();
        assertEquals(f.getAmplitude(), fitted.getAmplitude(), 1.2e-3);
        assertEquals(f.getPulsation(), fitted.getPulsation(), 3.3e-3);
        assertEquals(f.getPhase(),     MathUtils.normalizeAngle(fitted.getPhase(), f.getPhase()), 1.7e-2);

    }

// org.apache.commons.math.optimization.fitting.HarmonicFitterTest::testUnsorted
    public void testUnsorted() throws OptimizationException {
        Random randomizer = new Random(64925784252l);
        HarmonicFunction f = new HarmonicFunction(0.2, 3.4, 4.1);

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
            fitter.addObservedPoint(1.0, xTab[i], yTab[i]);
        }

        HarmonicFunction fitted = fitter.fit();
        assertEquals(f.getAmplitude(), fitted.getAmplitude(), 7.6e-4);
        assertEquals(f.getPulsation(), fitted.getPulsation(), 3.5e-3);
        assertEquals(f.getPhase(),     MathUtils.normalizeAngle(fitted.getPhase(), f.getPhase()), 1.5e-2);

    }

// org.apache.commons.math.optimization.fitting.ParametricGaussianFunctionTest::testFit01
    public void testFit01()
    throws OptimizationException, MathUserException {
        CurveFitter fitter = new CurveFitter(new LevenbergMarquardtOptimizer());
        addDatasetToCurveFitter(DATASET1, fitter);
        double[] parameters = fitter.fit(new ParametricGaussianFunction(),
                                         new double[] {8.64753e3, 3.483323e6, 4.06322, 1.946857e-2});
        assertEquals(99200.94715858076, parameters[0], 1e-4);
        assertEquals(3410515.221897707, parameters[1], 1e-4);
        assertEquals(4.054928275257894, parameters[2], 1e-4);
        assertEquals(0.014609868499860, parameters[3], 1e-4);
    }

// org.apache.commons.math.optimization.fitting.ParametricGaussianFunctionTest::testFit02
    public void testFit02()
    throws OptimizationException, MathUserException {
        CurveFitter fitter = new CurveFitter(new LevenbergMarquardtOptimizer());
        addDatasetToCurveFitter(DATASET1, fitter);
        double[] parameters = fitter.fit(new ParametricGaussianFunction(),
                                         new double[] {500000.0, 3500000.0, 4.055, 0.025479654});
        assertEquals(99200.81836264656, parameters[0], 1e-4);
        assertEquals(3410515.327151986, parameters[1], 1e-4);
        assertEquals(4.054928275377392, parameters[2], 1e-4);
        assertEquals(0.014609869119806, parameters[3], 1e-4);
    }

// org.apache.commons.math.optimization.fitting.ParametricGaussianFunctionTest::testValue01
    public void testValue01() throws MathUserException {
        ParametricGaussianFunction f = new ParametricGaussianFunction();
        f.value(0.0, null);
    }

// org.apache.commons.math.optimization.fitting.ParametricGaussianFunctionTest::testValue02
    public void testValue02() throws MathUserException {
        ParametricGaussianFunction f = new ParametricGaussianFunction();
        f.value(0.0, new double[] {0.0, 1.0});
    }

// org.apache.commons.math.optimization.fitting.ParametricGaussianFunctionTest::testValue03
    public void testValue03() throws MathUserException {
        ParametricGaussianFunction f = new ParametricGaussianFunction();
        f.value(0.0, new double[] {0.0, 1.0, 1.0, 0.0});
    }

// org.apache.commons.math.optimization.fitting.PolynomialFitterTest::testNoError
    public void testNoError() {
        Random randomizer = new Random(64925784252l);
        for (int degree = 1; degree < 10; ++degree) {
            PolynomialFunction p = buildRandomPolynomial(degree, randomizer);

            PolynomialFitter fitter =
                new PolynomialFitter(degree, new LevenbergMarquardtOptimizer());
            for (int i = 0; i <= degree; ++i) {
                fitter.addObservedPoint(1.0, i, p.value(i));
            }

            PolynomialFunction fitted = fitter.fit();

            for (double x = -1.0; x < 1.0; x += 0.01) {
                double error = FastMath.abs(p.value(x) - fitted.value(x)) /
                               (1.0 + FastMath.abs(p.value(x)));
                assertEquals(0.0, error, 1.0e-6);
            }

        }

    }

// org.apache.commons.math.optimization.fitting.PolynomialFitterTest::testSmallError
    public void testSmallError() {
        Random randomizer = new Random(53882150042l);
        double maxError = 0;
        for (int degree = 0; degree < 10; ++degree) {
            PolynomialFunction p = buildRandomPolynomial(degree, randomizer);

            PolynomialFitter fitter =
                new PolynomialFitter(degree, new LevenbergMarquardtOptimizer());
            for (double x = -1.0; x < 1.0; x += 0.01) {
                fitter.addObservedPoint(1.0, x,
                                        p.value(x) + 0.1 * randomizer.nextGaussian());
            }

            PolynomialFunction fitted = fitter.fit();

            for (double x = -1.0; x < 1.0; x += 0.01) {
                double error = FastMath.abs(p.value(x) - fitted.value(x)) /
                              (1.0 + FastMath.abs(p.value(x)));
                maxError = FastMath.max(maxError, error);
                assertTrue(FastMath.abs(error) < 0.1);
            }
        }
        assertTrue(maxError > 0.01);

    }

// org.apache.commons.math.optimization.fitting.PolynomialFitterTest::testRedundantSolvable
    public void testRedundantSolvable() {
        
        checkUnsolvableProblem(new LevenbergMarquardtOptimizer(), true);
    }

// org.apache.commons.math.optimization.fitting.PolynomialFitterTest::testRedundantUnsolvable
    public void testRedundantUnsolvable() {
        
        DifferentiableMultivariateVectorialOptimizer optimizer =
            new GaussNewtonOptimizer(true);
        checkUnsolvableProblem(optimizer, false);
    }

// org.apache.commons.math.optimization.general.GaussNewtonOptimizerTest::testTrivial
    public void testTrivial() throws MathUserException {
        LinearProblem problem =
            new LinearProblem(new double[][] { { 2 } }, new double[] { 3 });
        GaussNewtonOptimizer optimizer = new GaussNewtonOptimizer(true);
        optimizer.setConvergenceChecker(new SimpleVectorialValueChecker(1.0e-6, 1.0e-6));
        VectorialPointValuePair optimum =
            optimizer.optimize(100, problem, problem.target, new double[] { 1 }, new double[] { 0 });
        assertEquals(0, optimizer.getRMS(), 1.0e-10);
        assertEquals(1.5, optimum.getPoint()[0], 1.0e-10);
        assertEquals(3.0, optimum.getValue()[0], 1.0e-10);
    }

// org.apache.commons.math.optimization.general.GaussNewtonOptimizerTest::testColumnsPermutation
    public void testColumnsPermutation() throws MathUserException {

        LinearProblem problem =
            new LinearProblem(new double[][] { { 1.0, -1.0 }, { 0.0, 2.0 }, { 1.0, -2.0 } },
                              new double[] { 4.0, 6.0, 1.0 });

        GaussNewtonOptimizer optimizer = new GaussNewtonOptimizer(true);
        optimizer.setConvergenceChecker(new SimpleVectorialValueChecker(1.0e-6, 1.0e-6));
        VectorialPointValuePair optimum =
            optimizer.optimize(100, problem, problem.target, new double[] { 1, 1, 1 }, new double[] { 0, 0 });
        assertEquals(0, optimizer.getRMS(), 1.0e-10);
        assertEquals(7.0, optimum.getPoint()[0], 1.0e-10);
        assertEquals(3.0, optimum.getPoint()[1], 1.0e-10);
        assertEquals(4.0, optimum.getValue()[0], 1.0e-10);
        assertEquals(6.0, optimum.getValue()[1], 1.0e-10);
        assertEquals(1.0, optimum.getValue()[2], 1.0e-10);

    }

// org.apache.commons.math.optimization.general.GaussNewtonOptimizerTest::testNoDependency
    public void testNoDependency() throws MathUserException {
        LinearProblem problem = new LinearProblem(new double[][] {
                { 2, 0, 0, 0, 0, 0 },
                { 0, 2, 0, 0, 0, 0 },
                { 0, 0, 2, 0, 0, 0 },
                { 0, 0, 0, 2, 0, 0 },
                { 0, 0, 0, 0, 2, 0 },
                { 0, 0, 0, 0, 0, 2 }
        }, new double[] { 0.0, 1.1, 2.2, 3.3, 4.4, 5.5 });
        GaussNewtonOptimizer optimizer = new GaussNewtonOptimizer(true);
        optimizer.setConvergenceChecker(new SimpleVectorialValueChecker(1.0e-6, 1.0e-6));
        VectorialPointValuePair optimum =
            optimizer.optimize(100, problem, problem.target, new double[] { 1, 1, 1, 1, 1, 1 },
                               new double[] { 0, 0, 0, 0, 0, 0 });
        assertEquals(0, optimizer.getRMS(), 1.0e-10);
        for (int i = 0; i < problem.target.length; ++i) {
            assertEquals(0.55 * i, optimum.getPoint()[i], 1.0e-10);
        }
    }

// org.apache.commons.math.optimization.general.GaussNewtonOptimizerTest::testOneSet
    public void testOneSet() throws MathUserException {

        LinearProblem problem = new LinearProblem(new double[][] {
                {  1,  0, 0 },
                { -1,  1, 0 },
                {  0, -1, 1 }
        }, new double[] { 1, 1, 1});
        GaussNewtonOptimizer optimizer = new GaussNewtonOptimizer(true);
        optimizer.setConvergenceChecker(new SimpleVectorialValueChecker(1.0e-6, 1.0e-6));
        VectorialPointValuePair optimum =
            optimizer.optimize(100, problem, problem.target, new double[] { 1, 1, 1 }, new double[] { 0, 0, 0 });
        assertEquals(0, optimizer.getRMS(), 1.0e-10);
        assertEquals(1.0, optimum.getPoint()[0], 1.0e-10);
        assertEquals(2.0, optimum.getPoint()[1], 1.0e-10);
        assertEquals(3.0, optimum.getPoint()[2], 1.0e-10);

    }

// org.apache.commons.math.optimization.general.GaussNewtonOptimizerTest::testTwoSets
    public void testTwoSets() throws MathUserException {
        double epsilon = 1.0e-7;
        LinearProblem problem = new LinearProblem(new double[][] {
                {  2,  1,   0,  4,       0, 0 },
                { -4, -2,   3, -7,       0, 0 },
                {  4,  1,  -2,  8,       0, 0 },
                {  0, -3, -12, -1,       0, 0 },
                {  0,  0,   0,  0, epsilon, 1 },
                {  0,  0,   0,  0,       1, 1 }
        }, new double[] { 2, -9, 2, 2, 1 + epsilon * epsilon, 2});

        GaussNewtonOptimizer optimizer = new GaussNewtonOptimizer(true);
        optimizer.setConvergenceChecker(new SimpleVectorialValueChecker(1.0e-6, 1.0e-6));
        VectorialPointValuePair optimum =
            optimizer.optimize(100, problem, problem.target, new double[] { 1, 1, 1, 1, 1, 1 },
                               new double[] { 0, 0, 0, 0, 0, 0 });
        assertEquals(0, optimizer.getRMS(), 1.0e-10);
        assertEquals( 3.0, optimum.getPoint()[0], 1.0e-10);
        assertEquals( 4.0, optimum.getPoint()[1], 1.0e-10);
        assertEquals(-1.0, optimum.getPoint()[2], 1.0e-10);
        assertEquals(-2.0, optimum.getPoint()[3], 1.0e-10);
        assertEquals( 1.0 + epsilon, optimum.getPoint()[4], 1.0e-10);
        assertEquals( 1.0 - epsilon, optimum.getPoint()[5], 1.0e-10);

    }

// org.apache.commons.math.optimization.general.GaussNewtonOptimizerTest::testNonInversible
    public void testNonInversible() throws Exception {

        LinearProblem problem = new LinearProblem(new double[][] {
                {  1, 2, -3 },
                {  2, 1,  3 },
                { -3, 0, -9 }
        }, new double[] { 1, 1, 1 });
        GaussNewtonOptimizer optimizer = new GaussNewtonOptimizer(true);
        optimizer.setConvergenceChecker(new SimpleVectorialValueChecker(1.0e-6, 1.0e-6));
        try {
            optimizer.optimize(100, problem, problem.target, new double[] { 1, 1, 1 }, new double[] { 0, 0, 0 });
            fail("an exception should have been caught");
        } catch (ConvergenceException ee) {
            
        }
    }

// org.apache.commons.math.optimization.general.GaussNewtonOptimizerTest::testIllConditioned
    public void testIllConditioned() throws MathUserException {
        LinearProblem problem1 = new LinearProblem(new double[][] {
                { 10.0, 7.0,  8.0,  7.0 },
                {  7.0, 5.0,  6.0,  5.0 },
                {  8.0, 6.0, 10.0,  9.0 },
                {  7.0, 5.0,  9.0, 10.0 }
        }, new double[] { 32, 23, 33, 31 });
        GaussNewtonOptimizer optimizer = new GaussNewtonOptimizer(true);
        optimizer.setConvergenceChecker(new SimpleVectorialValueChecker(1.0e-6, 1.0e-6));
        VectorialPointValuePair optimum1 =
            optimizer.optimize(100, problem1, problem1.target, new double[] { 1, 1, 1, 1 },
                               new double[] { 0, 1, 2, 3 });
        assertEquals(0, optimizer.getRMS(), 1.0e-10);
        assertEquals(1.0, optimum1.getPoint()[0], 1.0e-10);
        assertEquals(1.0, optimum1.getPoint()[1], 1.0e-10);
        assertEquals(1.0, optimum1.getPoint()[2], 1.0e-10);
        assertEquals(1.0, optimum1.getPoint()[3], 1.0e-10);

        LinearProblem problem2 = new LinearProblem(new double[][] {
                { 10.00, 7.00, 8.10, 7.20 },
                {  7.08, 5.04, 6.00, 5.00 },
                {  8.00, 5.98, 9.89, 9.00 },
                {  6.99, 4.99, 9.00, 9.98 }
        }, new double[] { 32, 23, 33, 31 });
        VectorialPointValuePair optimum2 =
            optimizer.optimize(100, problem2, problem2.target, new double[] { 1, 1, 1, 1 },
                               new double[] { 0, 1, 2, 3 });
        assertEquals(0, optimizer.getRMS(), 1.0e-10);
        assertEquals(-81.0, optimum2.getPoint()[0], 1.0e-8);
        assertEquals(137.0, optimum2.getPoint()[1], 1.0e-8);
        assertEquals(-34.0, optimum2.getPoint()[2], 1.0e-8);
        assertEquals( 22.0, optimum2.getPoint()[3], 1.0e-8);

    }

// org.apache.commons.math.optimization.general.GaussNewtonOptimizerTest::testMoreEstimatedParametersSimple
    public void testMoreEstimatedParametersSimple() throws Exception {

        LinearProblem problem = new LinearProblem(new double[][] {
                { 3.0, 2.0,  0.0, 0.0 },
                { 0.0, 1.0, -1.0, 1.0 },
                { 2.0, 0.0,  1.0, 0.0 }
        }, new double[] { 7.0, 3.0, 5.0 });

        GaussNewtonOptimizer optimizer = new GaussNewtonOptimizer(true);
        optimizer.setConvergenceChecker(new SimpleVectorialValueChecker(1.0e-6, 1.0e-6));
        try {
            optimizer.optimize(100, problem, problem.target, new double[] { 1, 1, 1 },
                               new double[] { 7, 6, 5, 4 });
            fail("an exception should have been caught");
        } catch (ConvergenceException ee) {
            
        }
    }

// org.apache.commons.math.optimization.general.GaussNewtonOptimizerTest::testMoreEstimatedParametersUnsorted
    public void testMoreEstimatedParametersUnsorted() throws Exception {
        LinearProblem problem = new LinearProblem(new double[][] {
                 { 1.0, 1.0,  0.0,  0.0, 0.0,  0.0 },
                 { 0.0, 0.0,  1.0,  1.0, 1.0,  0.0 },
                 { 0.0, 0.0,  0.0,  0.0, 1.0, -1.0 },
                 { 0.0, 0.0, -1.0,  1.0, 0.0,  1.0 },
                 { 0.0, 0.0,  0.0, -1.0, 1.0,  0.0 }
        }, new double[] { 3.0, 12.0, -1.0, 7.0, 1.0 });
        GaussNewtonOptimizer optimizer = new GaussNewtonOptimizer(true);
        optimizer.setConvergenceChecker(new SimpleVectorialValueChecker(1.0e-6, 1.0e-6));
        try {
            optimizer.optimize(100, problem, problem.target, new double[] { 1, 1, 1, 1, 1 },
                               new double[] { 2, 2, 2, 2, 2, 2 });
            fail("an exception should have been caught");
        } catch (ConvergenceException ee) {
            
        }
    }

// org.apache.commons.math.optimization.general.GaussNewtonOptimizerTest::testRedundantEquations
    public void testRedundantEquations() throws MathUserException {
        LinearProblem problem = new LinearProblem(new double[][] {
                { 1.0,  1.0 },
                { 1.0, -1.0 },
                { 1.0,  3.0 }
        }, new double[] { 3.0, 1.0, 5.0 });

        GaussNewtonOptimizer optimizer = new GaussNewtonOptimizer(true);
        optimizer.setConvergenceChecker(new SimpleVectorialValueChecker(1.0e-6, 1.0e-6));
        VectorialPointValuePair optimum =
            optimizer.optimize(100, problem, problem.target, new double[] { 1, 1, 1 },
                               new double[] { 1, 1 });
        assertEquals(0, optimizer.getRMS(), 1.0e-10);
        assertEquals(2.0, optimum.getPoint()[0], 1.0e-8);
        assertEquals(1.0, optimum.getPoint()[1], 1.0e-8);

    }

// org.apache.commons.math.optimization.general.GaussNewtonOptimizerTest::testInconsistentEquations
    public void testInconsistentEquations() throws MathUserException {
        LinearProblem problem = new LinearProblem(new double[][] {
                { 1.0,  1.0 },
                { 1.0, -1.0 },
                { 1.0,  3.0 }
        }, new double[] { 3.0, 1.0, 4.0 });

        GaussNewtonOptimizer optimizer = new GaussNewtonOptimizer(true);
        optimizer.setConvergenceChecker(new SimpleVectorialValueChecker(1.0e-6, 1.0e-6));
        optimizer.optimize(100, problem, problem.target, new double[] { 1, 1, 1 }, new double[] { 1, 1 });
        assertTrue(optimizer.getRMS() > 0.1);

    }

// org.apache.commons.math.optimization.general.GaussNewtonOptimizerTest::testInconsistentSizes
    public void testInconsistentSizes() throws MathUserException {
        LinearProblem problem =
            new LinearProblem(new double[][] { { 1, 0 }, { 0, 1 } }, new double[] { -1, 1 });
        GaussNewtonOptimizer optimizer = new GaussNewtonOptimizer(true);
        optimizer.setConvergenceChecker(new SimpleVectorialValueChecker(1.0e-6, 1.0e-6));

        VectorialPointValuePair optimum =
            optimizer.optimize(100, problem, problem.target, new double[] { 1, 1 }, new double[] { 0, 0 });
        assertEquals(0, optimizer.getRMS(), 1.0e-10);
        assertEquals(-1, optimum.getPoint()[0], 1.0e-10);
        assertEquals(+1, optimum.getPoint()[1], 1.0e-10);

        try {
            optimizer.optimize(100, problem, problem.target,
                               new double[] { 1 },
                               new double[] { 0, 0 });
            fail("an exception should have been thrown");
        } catch (DimensionMismatchException oe) {
            
        }

        try {
            optimizer.optimize(100, problem, new double[] { 1 },
                               new double[] { 1 },
                               new double[] { 0, 0 });
            fail("an exception should have been thrown");
        } catch (DimensionMismatchException oe) {
            
        }

    }

// org.apache.commons.math.optimization.general.GaussNewtonOptimizerTest::testMaxEvaluations
    public void testMaxEvaluations() throws Exception {
        CircleVectorial circle = new CircleVectorial();
        circle.addPoint( 30.0,  68.0);
        circle.addPoint( 50.0,  -6.0);
        circle.addPoint(110.0, -20.0);
        circle.addPoint( 35.0,  15.0);
        circle.addPoint( 45.0,  97.0);
        GaussNewtonOptimizer optimizer = new GaussNewtonOptimizer(true);
        optimizer.setConvergenceChecker(new SimpleVectorialPointChecker(1.0e-30, 1.0e-30));
        try {
            optimizer.optimize(100, circle, new double[] { 0, 0, 0, 0, 0 },
                               new double[] { 1, 1, 1, 1, 1 },
                               new double[] { 98.680, 47.345 });
            fail("an exception should have been caught");
        } catch (TooManyEvaluationsException ee) {
            
        }
    }

// org.apache.commons.math.optimization.general.GaussNewtonOptimizerTest::testCircleFitting
    public void testCircleFitting() throws MathUserException {
        CircleVectorial circle = new CircleVectorial();
        circle.addPoint( 30.0,  68.0);
        circle.addPoint( 50.0,  -6.0);
        circle.addPoint(110.0, -20.0);
        circle.addPoint( 35.0,  15.0);
        circle.addPoint( 45.0,  97.0);
        GaussNewtonOptimizer optimizer = new GaussNewtonOptimizer(true);
        optimizer.setConvergenceChecker(new SimpleVectorialValueChecker(1.0e-13, 1.0e-13));
        VectorialPointValuePair optimum =
            optimizer.optimize(100, circle, new double[] { 0, 0, 0, 0, 0 },
                               new double[] { 1, 1, 1, 1, 1 },
                               new double[] { 98.680, 47.345 });
        assertEquals(1.768262623567235,  FastMath.sqrt(circle.getN()) * optimizer.getRMS(),  1.0e-10);
        Point2D.Double center = new Point2D.Double(optimum.getPointRef()[0], optimum.getPointRef()[1]);
        assertEquals(69.96016175359975, circle.getRadius(center), 1.0e-10);
        assertEquals(96.07590209601095, center.x, 1.0e-10);
        assertEquals(48.135167894714,   center.y, 1.0e-10);
    }

// org.apache.commons.math.optimization.general.GaussNewtonOptimizerTest::testCircleFittingBadInit
    public void testCircleFittingBadInit() throws MathUserException {
        CircleVectorial circle = new CircleVectorial();
        double[][] points = new double[][] {
                {-0.312967,  0.072366}, {-0.339248,  0.132965}, {-0.379780,  0.202724},
                {-0.390426,  0.260487}, {-0.361212,  0.328325}, {-0.346039,  0.392619},
                {-0.280579,  0.444306}, {-0.216035,  0.470009}, {-0.149127,  0.493832},
                {-0.075133,  0.483271}, {-0.007759,  0.452680}, { 0.060071,  0.410235},
                { 0.103037,  0.341076}, { 0.118438,  0.273884}, { 0.131293,  0.192201},
                { 0.115869,  0.129797}, { 0.072223,  0.058396}, { 0.022884,  0.000718},
                {-0.053355, -0.020405}, {-0.123584, -0.032451}, {-0.216248, -0.032862},
                {-0.278592, -0.005008}, {-0.337655,  0.056658}, {-0.385899,  0.112526},
                {-0.405517,  0.186957}, {-0.415374,  0.262071}, {-0.387482,  0.343398},
                {-0.347322,  0.397943}, {-0.287623,  0.458425}, {-0.223502,  0.475513},
                {-0.135352,  0.478186}, {-0.061221,  0.483371}, { 0.003711,  0.422737},
                { 0.065054,  0.375830}, { 0.108108,  0.297099}, { 0.123882,  0.222850},
                { 0.117729,  0.134382}, { 0.085195,  0.056820}, { 0.029800, -0.019138},
                {-0.027520, -0.072374}, {-0.102268, -0.091555}, {-0.200299, -0.106578},
                {-0.292731, -0.091473}, {-0.356288, -0.051108}, {-0.420561,  0.014926},
                {-0.471036,  0.074716}, {-0.488638,  0.182508}, {-0.485990,  0.254068},
                {-0.463943,  0.338438}, {-0.406453,  0.404704}, {-0.334287,  0.466119},
                {-0.254244,  0.503188}, {-0.161548,  0.495769}, {-0.075733,  0.495560},
                { 0.001375,  0.434937}, { 0.082787,  0.385806}, { 0.115490,  0.323807},
                { 0.141089,  0.223450}, { 0.138693,  0.131703}, { 0.126415,  0.049174},
                { 0.066518, -0.010217}, {-0.005184, -0.070647}, {-0.080985, -0.103635},
                {-0.177377, -0.116887}, {-0.260628, -0.100258}, {-0.335756, -0.056251},
                {-0.405195, -0.000895}, {-0.444937,  0.085456}, {-0.484357,  0.175597},
                {-0.472453,  0.248681}, {-0.438580,  0.347463}, {-0.402304,  0.422428},
                {-0.326777,  0.479438}, {-0.247797,  0.505581}, {-0.152676,  0.519380},
                {-0.071754,  0.516264}, { 0.015942,  0.472802}, { 0.076608,  0.419077},
                { 0.127673,  0.330264}, { 0.159951,  0.262150}, { 0.153530,  0.172681},
                { 0.140653,  0.089229}, { 0.078666,  0.024981}, { 0.023807, -0.037022},
                {-0.048837, -0.077056}, {-0.127729, -0.075338}, {-0.221271, -0.067526}
        };
        double[] target = new double[points.length];
        Arrays.fill(target, 0.0);
        double[] weights = new double[points.length];
        Arrays.fill(weights, 2.0);
        for (int i = 0; i < points.length; ++i) {
            circle.addPoint(points[i][0], points[i][1]);
        }
        GaussNewtonOptimizer optimizer = new GaussNewtonOptimizer(true);
        optimizer.setConvergenceChecker(new SimpleVectorialValueChecker(1.0e-6, 1.0e-6));
        try {
            optimizer.optimize(100, circle, target, weights, new double[] { -12, -12 });
            fail("an exception should have been caught");
        } catch (ConvergenceException ee) {
            
        }

        VectorialPointValuePair optimum =
            optimizer.optimize(100, circle, target, weights, new double[] { 0, 0 });
        assertEquals(-0.1517383071957963, optimum.getPointRef()[0], 1.0e-6);
        assertEquals(0.2074999736353867,  optimum.getPointRef()[1], 1.0e-6);
        assertEquals(0.04268731682389561, optimizer.getRMS(),       1.0e-8);

    }

// org.apache.commons.math.optimization.general.LevenbergMarquardtOptimizerTest::testTrivial
    public void testTrivial() {
        LinearProblem problem =
            new LinearProblem(new double[][] { { 2 } }, new double[] { 3 });
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        VectorialPointValuePair optimum =
            optimizer.optimize(100, problem, problem.target, new double[] { 1 }, new double[] { 0 });
        assertEquals(0, optimizer.getRMS(), 1.0e-10);
        try {
            optimizer.guessParametersErrors();
            fail("an exception should have been thrown");
        } catch (NumberIsTooSmallException ee) {
            
        }
        assertEquals(1.5, optimum.getPoint()[0], 1.0e-10);
        assertEquals(3.0, optimum.getValue()[0], 1.0e-10);
    }

// org.apache.commons.math.optimization.general.LevenbergMarquardtOptimizerTest::testQRColumnsPermutation
    public void testQRColumnsPermutation() {

        LinearProblem problem =
            new LinearProblem(new double[][] { { 1.0, -1.0 }, { 0.0, 2.0 }, { 1.0, -2.0 } },
                              new double[] { 4.0, 6.0, 1.0 });

        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        VectorialPointValuePair optimum =
            optimizer.optimize(100, problem, problem.target, new double[] { 1, 1, 1 }, new double[] { 0, 0 });
        assertEquals(0, optimizer.getRMS(), 1.0e-10);
        assertEquals(7.0, optimum.getPoint()[0], 1.0e-10);
        assertEquals(3.0, optimum.getPoint()[1], 1.0e-10);
        assertEquals(4.0, optimum.getValue()[0], 1.0e-10);
        assertEquals(6.0, optimum.getValue()[1], 1.0e-10);
        assertEquals(1.0, optimum.getValue()[2], 1.0e-10);
    }

// org.apache.commons.math.optimization.general.LevenbergMarquardtOptimizerTest::testNoDependency
    public void testNoDependency() {
        LinearProblem problem = new LinearProblem(new double[][] {
                { 2, 0, 0, 0, 0, 0 },
                { 0, 2, 0, 0, 0, 0 },
                { 0, 0, 2, 0, 0, 0 },
                { 0, 0, 0, 2, 0, 0 },
                { 0, 0, 0, 0, 2, 0 },
                { 0, 0, 0, 0, 0, 2 }
        }, new double[] { 0.0, 1.1, 2.2, 3.3, 4.4, 5.5 });
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        VectorialPointValuePair optimum =
            optimizer.optimize(100, problem, problem.target, new double[] { 1, 1, 1, 1, 1, 1 },
                               new double[] { 0, 0, 0, 0, 0, 0 });
        assertEquals(0, optimizer.getRMS(), 1.0e-10);
        for (int i = 0; i < problem.target.length; ++i) {
            assertEquals(0.55 * i, optimum.getPoint()[i], 1.0e-10);
        }
    }

// org.apache.commons.math.optimization.general.LevenbergMarquardtOptimizerTest::testOneSet
    public void testOneSet() {

        LinearProblem problem = new LinearProblem(new double[][] {
                {  1,  0, 0 },
                { -1,  1, 0 },
                {  0, -1, 1 }
        }, new double[] { 1, 1, 1});
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        VectorialPointValuePair optimum =
            optimizer.optimize(100, problem, problem.target, new double[] { 1, 1, 1 }, new double[] { 0, 0, 0 });
        assertEquals(0, optimizer.getRMS(), 1.0e-10);
        assertEquals(1.0, optimum.getPoint()[0], 1.0e-10);
        assertEquals(2.0, optimum.getPoint()[1], 1.0e-10);
        assertEquals(3.0, optimum.getPoint()[2], 1.0e-10);
    }

// org.apache.commons.math.optimization.general.LevenbergMarquardtOptimizerTest::testTwoSets
    public void testTwoSets() {
        double epsilon = 1.0e-7;
        LinearProblem problem = new LinearProblem(new double[][] {
                {  2,  1,   0,  4,       0, 0 },
                { -4, -2,   3, -7,       0, 0 },
                {  4,  1,  -2,  8,       0, 0 },
                {  0, -3, -12, -1,       0, 0 },
                {  0,  0,   0,  0, epsilon, 1 },
                {  0,  0,   0,  0,       1, 1 }
        }, new double[] { 2, -9, 2, 2, 1 + epsilon * epsilon, 2});

        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        VectorialPointValuePair optimum =
            optimizer.optimize(100, problem, problem.target, new double[] { 1, 1, 1, 1, 1, 1 },
                               new double[] { 0, 0, 0, 0, 0, 0 });
        assertEquals(0, optimizer.getRMS(), 1.0e-10);
        assertEquals( 3.0, optimum.getPoint()[0], 1.0e-10);
        assertEquals( 4.0, optimum.getPoint()[1], 1.0e-10);
        assertEquals(-1.0, optimum.getPoint()[2], 1.0e-10);
        assertEquals(-2.0, optimum.getPoint()[3], 1.0e-10);
        assertEquals( 1.0 + epsilon, optimum.getPoint()[4], 1.0e-10);
        assertEquals( 1.0 - epsilon, optimum.getPoint()[5], 1.0e-10);
    }

// org.apache.commons.math.optimization.general.LevenbergMarquardtOptimizerTest::testNonInvertible
    public void testNonInvertible() {

        LinearProblem problem = new LinearProblem(new double[][] {
                {  1, 2, -3 },
                {  2, 1,  3 },
                { -3, 0, -9 }
        }, new double[] { 1, 1, 1 });

        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        optimizer.optimize(100, problem, problem.target, new double[] { 1, 1, 1 }, new double[] { 0, 0, 0 });
        assertTrue(FastMath.sqrt(problem.target.length) * optimizer.getRMS() > 0.6);
        try {
            optimizer.getCovariances();
            fail("an exception should have been thrown");
        } catch (SingularMatrixException ee) {
            
        }
    }

// org.apache.commons.math.optimization.general.LevenbergMarquardtOptimizerTest::testIllConditioned
    public void testIllConditioned() {
        LinearProblem problem1 = new LinearProblem(new double[][] {
                { 10.0, 7.0,  8.0,  7.0 },
                {  7.0, 5.0,  6.0,  5.0 },
                {  8.0, 6.0, 10.0,  9.0 },
                {  7.0, 5.0,  9.0, 10.0 }
        }, new double[] { 32, 23, 33, 31 });
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        VectorialPointValuePair optimum1 =
            optimizer.optimize(100, problem1, problem1.target, new double[] { 1, 1, 1, 1 },
                               new double[] { 0, 1, 2, 3 });
        assertEquals(0, optimizer.getRMS(), 1.0e-10);
        assertEquals(1.0, optimum1.getPoint()[0], 1.0e-10);
        assertEquals(1.0, optimum1.getPoint()[1], 1.0e-10);
        assertEquals(1.0, optimum1.getPoint()[2], 1.0e-10);
        assertEquals(1.0, optimum1.getPoint()[3], 1.0e-10);

        LinearProblem problem2 = new LinearProblem(new double[][] {
                { 10.00, 7.00, 8.10, 7.20 },
                {  7.08, 5.04, 6.00, 5.00 },
                {  8.00, 5.98, 9.89, 9.00 },
                {  6.99, 4.99, 9.00, 9.98 }
        }, new double[] { 32, 23, 33, 31 });
        VectorialPointValuePair optimum2 =
            optimizer.optimize(100, problem2, problem2.target, new double[] { 1, 1, 1, 1 },
                               new double[] { 0, 1, 2, 3 });
        assertEquals(0, optimizer.getRMS(), 1.0e-10);
        assertEquals(-81.0, optimum2.getPoint()[0], 1.0e-8);
        assertEquals(137.0, optimum2.getPoint()[1], 1.0e-8);
        assertEquals(-34.0, optimum2.getPoint()[2], 1.0e-8);
        assertEquals( 22.0, optimum2.getPoint()[3], 1.0e-8);
    }

// org.apache.commons.math.optimization.general.LevenbergMarquardtOptimizerTest::testMoreEstimatedParametersSimple
    public void testMoreEstimatedParametersSimple() {

        LinearProblem problem = new LinearProblem(new double[][] {
                { 3.0, 2.0,  0.0, 0.0 },
                { 0.0, 1.0, -1.0, 1.0 },
                { 2.0, 0.0,  1.0, 0.0 }
        }, new double[] { 7.0, 3.0, 5.0 });

        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        optimizer.optimize(100, problem, problem.target, new double[] { 1, 1, 1 },
                new double[] { 7, 6, 5, 4 });
        assertEquals(0, optimizer.getRMS(), 1.0e-10);
    }

// org.apache.commons.math.optimization.general.LevenbergMarquardtOptimizerTest::testMoreEstimatedParametersUnsorted
    public void testMoreEstimatedParametersUnsorted() {
        LinearProblem problem = new LinearProblem(new double[][] {
                { 1.0, 1.0,  0.0,  0.0, 0.0,  0.0 },
                { 0.0, 0.0,  1.0,  1.0, 1.0,  0.0 },
                { 0.0, 0.0,  0.0,  0.0, 1.0, -1.0 },
                { 0.0, 0.0, -1.0,  1.0, 0.0,  1.0 },
                { 0.0, 0.0,  0.0, -1.0, 1.0,  0.0 }
       }, new double[] { 3.0, 12.0, -1.0, 7.0, 1.0 });

        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        VectorialPointValuePair optimum =
            optimizer.optimize(100, problem, problem.target, new double[] { 1, 1, 1, 1, 1 },
                               new double[] { 2, 2, 2, 2, 2, 2 });
        assertEquals(0, optimizer.getRMS(), 1.0e-10);
        assertEquals(3.0, optimum.getPointRef()[2], 1.0e-10);
        assertEquals(4.0, optimum.getPointRef()[3], 1.0e-10);
        assertEquals(5.0, optimum.getPointRef()[4], 1.0e-10);
        assertEquals(6.0, optimum.getPointRef()[5], 1.0e-10);
    }

// org.apache.commons.math.optimization.general.LevenbergMarquardtOptimizerTest::testRedundantEquations
    public void testRedundantEquations() {
        LinearProblem problem = new LinearProblem(new double[][] {
                { 1.0,  1.0 },
                { 1.0, -1.0 },
                { 1.0,  3.0 }
        }, new double[] { 3.0, 1.0, 5.0 });

        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        VectorialPointValuePair optimum =
            optimizer.optimize(100, problem, problem.target, new double[] { 1, 1, 1 },
                               new double[] { 1, 1 });
        assertEquals(0, optimizer.getRMS(), 1.0e-10);
        assertEquals(2.0, optimum.getPointRef()[0], 1.0e-10);
        assertEquals(1.0, optimum.getPointRef()[1], 1.0e-10);
    }

// org.apache.commons.math.optimization.general.LevenbergMarquardtOptimizerTest::testInconsistentEquations
    public void testInconsistentEquations() {
        LinearProblem problem = new LinearProblem(new double[][] {
                { 1.0,  1.0 },
                { 1.0, -1.0 },
                { 1.0,  3.0 }
        }, new double[] { 3.0, 1.0, 4.0 });

        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        optimizer.optimize(100, problem, problem.target, new double[] { 1, 1, 1 }, new double[] { 1, 1 });
        assertTrue(optimizer.getRMS() > 0.1);
    }

// org.apache.commons.math.optimization.general.LevenbergMarquardtOptimizerTest::testInconsistentSizes
    public void testInconsistentSizes() {
        LinearProblem problem =
            new LinearProblem(new double[][] { { 1, 0 }, { 0, 1 } }, new double[] { -1, 1 });
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();

        VectorialPointValuePair optimum =
            optimizer.optimize(100, problem, problem.target, new double[] { 1, 1 }, new double[] { 0, 0 });
        assertEquals(0, optimizer.getRMS(), 1.0e-10);
        assertEquals(-1, optimum.getPoint()[0], 1.0e-10);
        assertEquals(+1, optimum.getPoint()[1], 1.0e-10);

        try {
            optimizer.optimize(100, problem, problem.target,
                               new double[] { 1 },
                               new double[] { 0, 0 });
            fail("an exception should have been thrown");
        } catch (DimensionMismatchException oe) {
            
        }

        try {
            optimizer.optimize(100, problem, new double[] { 1 },
                               new double[] { 1 },
                               new double[] { 0, 0 });
            fail("an exception should have been thrown");
        } catch (DimensionMismatchException oe) {
            
        }
    }

// org.apache.commons.math.optimization.general.LevenbergMarquardtOptimizerTest::testControlParameters
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

// org.apache.commons.math.optimization.general.LevenbergMarquardtOptimizerTest::testCircleFitting
    public void testCircleFitting() {
        CircleVectorial circle = new CircleVectorial();
        circle.addPoint( 30.0,  68.0);
        circle.addPoint( 50.0,  -6.0);
        circle.addPoint(110.0, -20.0);
        circle.addPoint( 35.0,  15.0);
        circle.addPoint( 45.0,  97.0);
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        VectorialPointValuePair optimum =
            optimizer.optimize(100, circle, new double[] { 0, 0, 0, 0, 0 }, new double[] { 1, 1, 1, 1, 1 },
                               new double[] { 98.680, 47.345 });
        assertTrue(optimizer.getEvaluations() < 10);
        assertTrue(optimizer.getJacobianEvaluations() < 10);
        double rms = optimizer.getRMS();
        assertEquals(1.768262623567235,  FastMath.sqrt(circle.getN()) * rms,  1.0e-10);
        Point2D.Double center = new Point2D.Double(optimum.getPointRef()[0], optimum.getPointRef()[1]);
        assertEquals(69.96016176931406, circle.getRadius(center), 1.0e-10);
        assertEquals(96.07590211815305, center.x,      1.0e-10);
        assertEquals(48.13516790438953, center.y,      1.0e-10);
        double[][] cov = optimizer.getCovariances();
        assertEquals(1.839, cov[0][0], 0.001);
        assertEquals(0.731, cov[0][1], 0.001);
        assertEquals(cov[0][1], cov[1][0], 1.0e-14);
        assertEquals(0.786, cov[1][1], 0.001);
        double[] errors = optimizer.guessParametersErrors();
        assertEquals(1.384, errors[0], 0.001);
        assertEquals(0.905, errors[1], 0.001);

        
        double  r = circle.getRadius(center);
        for (double d= 0; d < 2 * FastMath.PI; d += 0.01) {
            circle.addPoint(center.x + r * FastMath.cos(d), center.y + r * FastMath.sin(d));
        }
        double[] target = new double[circle.getN()];
        Arrays.fill(target, 0.0);
        double[] weights = new double[circle.getN()];
        Arrays.fill(weights, 2.0);
        optimizer.optimize(100, circle, target, weights, new double[] { 98.680, 47.345 });
        cov = optimizer.getCovariances();
        assertEquals(0.0016, cov[0][0], 0.001);
        assertEquals(3.2e-7, cov[0][1], 1.0e-9);
        assertEquals(cov[0][1], cov[1][0], 1.0e-14);
        assertEquals(0.0016, cov[1][1], 0.001);
        errors = optimizer.guessParametersErrors();
        assertEquals(0.004, errors[0], 0.001);
        assertEquals(0.004, errors[1], 0.001);
    }

// org.apache.commons.math.optimization.general.LevenbergMarquardtOptimizerTest::testCircleFittingBadInit
    public void testCircleFittingBadInit() {
        CircleVectorial circle = new CircleVectorial();
        double[][] points = new double[][] {
                {-0.312967,  0.072366}, {-0.339248,  0.132965}, {-0.379780,  0.202724},
                {-0.390426,  0.260487}, {-0.361212,  0.328325}, {-0.346039,  0.392619},
                {-0.280579,  0.444306}, {-0.216035,  0.470009}, {-0.149127,  0.493832},
                {-0.075133,  0.483271}, {-0.007759,  0.452680}, { 0.060071,  0.410235},
                { 0.103037,  0.341076}, { 0.118438,  0.273884}, { 0.131293,  0.192201},
                { 0.115869,  0.129797}, { 0.072223,  0.058396}, { 0.022884,  0.000718},
                {-0.053355, -0.020405}, {-0.123584, -0.032451}, {-0.216248, -0.032862},
                {-0.278592, -0.005008}, {-0.337655,  0.056658}, {-0.385899,  0.112526},
                {-0.405517,  0.186957}, {-0.415374,  0.262071}, {-0.387482,  0.343398},
                {-0.347322,  0.397943}, {-0.287623,  0.458425}, {-0.223502,  0.475513},
                {-0.135352,  0.478186}, {-0.061221,  0.483371}, { 0.003711,  0.422737},
                { 0.065054,  0.375830}, { 0.108108,  0.297099}, { 0.123882,  0.222850},
                { 0.117729,  0.134382}, { 0.085195,  0.056820}, { 0.029800, -0.019138},
                {-0.027520, -0.072374}, {-0.102268, -0.091555}, {-0.200299, -0.106578},
                {-0.292731, -0.091473}, {-0.356288, -0.051108}, {-0.420561,  0.014926},
                {-0.471036,  0.074716}, {-0.488638,  0.182508}, {-0.485990,  0.254068},
                {-0.463943,  0.338438}, {-0.406453,  0.404704}, {-0.334287,  0.466119},
                {-0.254244,  0.503188}, {-0.161548,  0.495769}, {-0.075733,  0.495560},
                { 0.001375,  0.434937}, { 0.082787,  0.385806}, { 0.115490,  0.323807},
                { 0.141089,  0.223450}, { 0.138693,  0.131703}, { 0.126415,  0.049174},
                { 0.066518, -0.010217}, {-0.005184, -0.070647}, {-0.080985, -0.103635},
                {-0.177377, -0.116887}, {-0.260628, -0.100258}, {-0.335756, -0.056251},
                {-0.405195, -0.000895}, {-0.444937,  0.085456}, {-0.484357,  0.175597},
                {-0.472453,  0.248681}, {-0.438580,  0.347463}, {-0.402304,  0.422428},
                {-0.326777,  0.479438}, {-0.247797,  0.505581}, {-0.152676,  0.519380},
                {-0.071754,  0.516264}, { 0.015942,  0.472802}, { 0.076608,  0.419077},
                { 0.127673,  0.330264}, { 0.159951,  0.262150}, { 0.153530,  0.172681},
                { 0.140653,  0.089229}, { 0.078666,  0.024981}, { 0.023807, -0.037022},
                {-0.048837, -0.077056}, {-0.127729, -0.075338}, {-0.221271, -0.067526}
        };
        double[] target = new double[points.length];
        Arrays.fill(target, 0.0);
        double[] weights = new double[points.length];
        Arrays.fill(weights, 2.0);
        for (int i = 0; i < points.length; ++i) {
            circle.addPoint(points[i][0], points[i][1]);
        }
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        optimizer.setConvergenceChecker(new SimpleVectorialValueChecker(1.0e-8, 1.0e-8));
        VectorialPointValuePair optimum =
            optimizer.optimize(100, circle, target, weights, new double[] { -12, -12 });
        Point2D.Double center = new Point2D.Double(optimum.getPointRef()[0], optimum.getPointRef()[1]);
        assertTrue(optimizer.getEvaluations() < 25);
        assertTrue(optimizer.getJacobianEvaluations() < 20);
        assertEquals( 0.043, optimizer.getRMS(), 1.0e-3);
        assertEquals( 0.292235,  circle.getRadius(center), 1.0e-6);
        assertEquals(-0.151738,  center.x,      1.0e-6);
        assertEquals( 0.2075001, center.y,      1.0e-6);
    }

// org.apache.commons.math.optimization.general.LevenbergMarquardtOptimizerTest::testMath199
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
            fail("an exception should have been thrown");
        } catch (ConvergenceException ee) {
            
        }
    }

// org.apache.commons.math.optimization.general.MinpackTest::testMinpackLinearFullRank
  public void testMinpackLinearFullRank() {
    minpackTest(new LinearFullRankFunction(10, 5, 1.0,
                                           5.0, 2.23606797749979), false);
    minpackTest(new LinearFullRankFunction(50, 5, 1.0,
                                           8.06225774829855, 6.70820393249937), false);
  }

// org.apache.commons.math.optimization.general.MinpackTest::testMinpackLinearRank1
  public void testMinpackLinearRank1() {
    minpackTest(new LinearRank1Function(10, 5, 1.0,
                                        291.521868819476, 1.4638501094228), false);
    minpackTest(new LinearRank1Function(50, 5, 1.0,
                                        3101.60039334535, 3.48263016573496), false);
  }

// org.apache.commons.math.optimization.general.MinpackTest::testMinpackLinearRank1ZeroColsAndRows
  public void testMinpackLinearRank1ZeroColsAndRows() {
    minpackTest(new LinearRank1ZeroColsAndRowsFunction(10, 5, 1.0), false);
    minpackTest(new LinearRank1ZeroColsAndRowsFunction(50, 5, 1.0), false);
  }

// org.apache.commons.math.optimization.general.MinpackTest::testMinpackRosenbrok
  public void testMinpackRosenbrok() {
    minpackTest(new RosenbrockFunction(new double[] { -1.2, 1.0 },
                                       FastMath.sqrt(24.2)), false);
    minpackTest(new RosenbrockFunction(new double[] { -12.0, 10.0 },
                                       FastMath.sqrt(1795769.0)), false);
    minpackTest(new RosenbrockFunction(new double[] { -120.0, 100.0 },
                                       11.0 * FastMath.sqrt(169000121.0)), false);
  }

// org.apache.commons.math.optimization.general.MinpackTest::testMinpackHelicalValley
  public void testMinpackHelicalValley() {
    minpackTest(new HelicalValleyFunction(new double[] { -1.0, 0.0, 0.0 },
                                          50.0), false);
    minpackTest(new HelicalValleyFunction(new double[] { -10.0, 0.0, 0.0 },
                                          102.95630140987), false);
    minpackTest(new HelicalValleyFunction(new double[] { -100.0, 0.0, 0.0},
                                          991.261822123701), false);
  }

// org.apache.commons.math.optimization.general.MinpackTest::testMinpackPowellSingular
  public void testMinpackPowellSingular() {
    minpackTest(new PowellSingularFunction(new double[] { 3.0, -1.0, 0.0, 1.0 },
                                           14.6628782986152), false);
    minpackTest(new PowellSingularFunction(new double[] { 30.0, -10.0, 0.0, 10.0 },
                                           1270.9838708654), false);
    minpackTest(new PowellSingularFunction(new double[] { 300.0, -100.0, 0.0, 100.0 },
                                           126887.903284750), false);
  }

// org.apache.commons.math.optimization.general.MinpackTest::testMinpackFreudensteinRoth
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

// org.apache.commons.math.optimization.general.MinpackTest::testMinpackBard
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

// org.apache.commons.math.optimization.general.MinpackTest::testMinpackKowalikOsborne
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

// org.apache.commons.math.optimization.general.MinpackTest::testMinpackMeyer
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

// org.apache.commons.math.optimization.general.MinpackTest::testMinpackWatson
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

// org.apache.commons.math.optimization.general.MinpackTest::testMinpackBox3Dimensional
  public void testMinpackBox3Dimensional() {
    minpackTest(new Box3DimensionalFunction(10, new double[] { 0.0, 10.0, 20.0 },
                                            32.1115837449572), false);
  }

// org.apache.commons.math.optimization.general.MinpackTest::testMinpackJennrichSampson
  public void testMinpackJennrichSampson() {
    minpackTest(new JennrichSampsonFunction(10, new double[] { 0.3, 0.4 },
                                            64.5856498144943, 11.1517793413499,
                                            new double[] {
 
                                               0.2578199266368004, 0.25782997676455244
                                            }), false);
  }

// org.apache.commons.math.optimization.general.MinpackTest::testMinpackBrownDennis
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

// org.apache.commons.math.optimization.general.MinpackTest::testMinpackChebyquad
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

// org.apache.commons.math.optimization.general.MinpackTest::testMinpackBrownAlmostLinear
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

// org.apache.commons.math.optimization.general.MinpackTest::testMinpackOsborne1
  public void testMinpackOsborne1() {
      minpackTest(new Osborne1Function(new double[] { 0.5, 1.5, -1.0, 0.01, 0.02, },
                                       0.937564021037838, 0.00739249260904843,
                                       new double[] {
                                         0.375410049244025, 1.93584654543108,
                                        -1.46468676748716, 0.0128675339110439,
                                         0.0221227011813076
                                       }), false);
    }

// org.apache.commons.math.optimization.general.MinpackTest::testMinpackOsborne2
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

// org.apache.commons.math.optimization.general.NonLinearConjugateGradientOptimizerTest::testTrivial
    public void testTrivial() {
        LinearProblem problem =
            new LinearProblem(new double[][] { { 2 } }, new double[] { 3 });
        NonLinearConjugateGradientOptimizer optimizer =
            new NonLinearConjugateGradientOptimizer(ConjugateGradientFormula.POLAK_RIBIERE);
        optimizer.setConvergenceChecker(new SimpleScalarValueChecker(1.0e-6, 1.0e-6));
        RealPointValuePair optimum =
            optimizer.optimize(100, problem, GoalType.MINIMIZE, new double[] { 0 });
        Assert.assertEquals(1.5, optimum.getPoint()[0], 1.0e-10);
        Assert.assertEquals(0.0, optimum.getValue(), 1.0e-10);
    }

// org.apache.commons.math.optimization.general.NonLinearConjugateGradientOptimizerTest::testColumnsPermutation
    public void testColumnsPermutation() {
        LinearProblem problem =
            new LinearProblem(new double[][] { { 1.0, -1.0 }, { 0.0, 2.0 }, { 1.0, -2.0 } },
                              new double[] { 4.0, 6.0, 1.0 });

        NonLinearConjugateGradientOptimizer optimizer =
            new NonLinearConjugateGradientOptimizer(ConjugateGradientFormula.POLAK_RIBIERE);
        optimizer.setConvergenceChecker(new SimpleScalarValueChecker(1.0e-6, 1.0e-6));
        RealPointValuePair optimum =
            optimizer.optimize(100, problem, GoalType.MINIMIZE, new double[] { 0, 0 });
        Assert.assertEquals(7.0, optimum.getPoint()[0], 1.0e-10);
        Assert.assertEquals(3.0, optimum.getPoint()[1], 1.0e-10);
        Assert.assertEquals(0.0, optimum.getValue(), 1.0e-10);

    }

// org.apache.commons.math.optimization.general.NonLinearConjugateGradientOptimizerTest::testNoDependency
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
            new NonLinearConjugateGradientOptimizer(ConjugateGradientFormula.POLAK_RIBIERE);
        optimizer.setConvergenceChecker(new SimpleScalarValueChecker(1.0e-6, 1.0e-6));
        RealPointValuePair optimum =
            optimizer.optimize(100, problem, GoalType.MINIMIZE, new double[] { 0, 0, 0, 0, 0, 0 });
        for (int i = 0; i < problem.target.length; ++i) {
            Assert.assertEquals(0.55 * i, optimum.getPoint()[i], 1.0e-10);
        }
    }

// org.apache.commons.math.optimization.general.NonLinearConjugateGradientOptimizerTest::testOneSet
    public void testOneSet() {
        LinearProblem problem = new LinearProblem(new double[][] {
                {  1,  0, 0 },
                { -1,  1, 0 },
                {  0, -1, 1 }
        }, new double[] { 1, 1, 1});
        NonLinearConjugateGradientOptimizer optimizer =
            new NonLinearConjugateGradientOptimizer(ConjugateGradientFormula.POLAK_RIBIERE);
        optimizer.setConvergenceChecker(new SimpleScalarValueChecker(1.0e-6, 1.0e-6));
        RealPointValuePair optimum =
            optimizer.optimize(100, problem, GoalType.MINIMIZE, new double[] { 0, 0, 0 });
        Assert.assertEquals(1.0, optimum.getPoint()[0], 1.0e-10);
        Assert.assertEquals(2.0, optimum.getPoint()[1], 1.0e-10);
        Assert.assertEquals(3.0, optimum.getPoint()[2], 1.0e-10);

    }

// org.apache.commons.math.optimization.general.NonLinearConjugateGradientOptimizerTest::testTwoSets
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

        NonLinearConjugateGradientOptimizer optimizer =
            new NonLinearConjugateGradientOptimizer(ConjugateGradientFormula.POLAK_RIBIERE);
        optimizer.setPreconditioner(new Preconditioner() {
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
        });
        optimizer.setConvergenceChecker(new SimpleScalarValueChecker(1.0e-13, 1.0e-13));

        RealPointValuePair optimum =
            optimizer.optimize(100, problem, GoalType.MINIMIZE, new double[] { 0, 0, 0, 0, 0, 0 });
        Assert.assertEquals( 3.0, optimum.getPoint()[0], 1.0e-10);
        Assert.assertEquals( 4.0, optimum.getPoint()[1], 1.0e-10);
        Assert.assertEquals(-1.0, optimum.getPoint()[2], 1.0e-10);
        Assert.assertEquals(-2.0, optimum.getPoint()[3], 1.0e-10);
        Assert.assertEquals( 1.0 + epsilon, optimum.getPoint()[4], 1.0e-10);
        Assert.assertEquals( 1.0 - epsilon, optimum.getPoint()[5], 1.0e-10);

    }

// org.apache.commons.math.optimization.general.NonLinearConjugateGradientOptimizerTest::testNonInversible
    public void testNonInversible() {
        LinearProblem problem = new LinearProblem(new double[][] {
                {  1, 2, -3 },
                {  2, 1,  3 },
                { -3, 0, -9 }
        }, new double[] { 1, 1, 1 });
        NonLinearConjugateGradientOptimizer optimizer =
            new NonLinearConjugateGradientOptimizer(ConjugateGradientFormula.POLAK_RIBIERE);
        optimizer.setConvergenceChecker(new SimpleScalarValueChecker(1.0e-6, 1.0e-6));
        RealPointValuePair optimum =
                optimizer.optimize(100, problem, GoalType.MINIMIZE, new double[] { 0, 0, 0 });
        Assert.assertTrue(optimum.getValue() > 0.5);
    }

// org.apache.commons.math.optimization.general.NonLinearConjugateGradientOptimizerTest::testIllConditioned
    public void testIllConditioned() {
        LinearProblem problem1 = new LinearProblem(new double[][] {
                { 10.0, 7.0,  8.0,  7.0 },
                {  7.0, 5.0,  6.0,  5.0 },
                {  8.0, 6.0, 10.0,  9.0 },
                {  7.0, 5.0,  9.0, 10.0 }
        }, new double[] { 32, 23, 33, 31 });
        NonLinearConjugateGradientOptimizer optimizer =
            new NonLinearConjugateGradientOptimizer(ConjugateGradientFormula.POLAK_RIBIERE);
        optimizer.setConvergenceChecker(new SimpleScalarValueChecker(1.0e-13, 1.0e-13));
        BrentSolver solver = new BrentSolver(1e-15, 1e-15);
        optimizer.setLineSearchSolver(solver);
        RealPointValuePair optimum1 =
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
        RealPointValuePair optimum2 =
            optimizer.optimize(200, problem2, GoalType.MINIMIZE, new double[] { 0, 1, 2, 3 });
        Assert.assertEquals(-81.0, optimum2.getPoint()[0], 1.0e-1);
        Assert.assertEquals(137.0, optimum2.getPoint()[1], 1.0e-1);
        Assert.assertEquals(-34.0, optimum2.getPoint()[2], 1.0e-1);
        Assert.assertEquals( 22.0, optimum2.getPoint()[3], 1.0e-1);

    }

// org.apache.commons.math.optimization.general.NonLinearConjugateGradientOptimizerTest::testMoreEstimatedParametersSimple
    public void testMoreEstimatedParametersSimple() {
        LinearProblem problem = new LinearProblem(new double[][] {
                { 3.0, 2.0,  0.0, 0.0 },
                { 0.0, 1.0, -1.0, 1.0 },
                { 2.0, 0.0,  1.0, 0.0 }
        }, new double[] { 7.0, 3.0, 5.0 });

        NonLinearConjugateGradientOptimizer optimizer =
            new NonLinearConjugateGradientOptimizer(ConjugateGradientFormula.POLAK_RIBIERE);
        optimizer.setConvergenceChecker(new SimpleScalarValueChecker(1.0e-6, 1.0e-6));
        RealPointValuePair optimum =
            optimizer.optimize(100, problem, GoalType.MINIMIZE, new double[] { 7, 6, 5, 4 });
        Assert.assertEquals(0, optimum.getValue(), 1.0e-10);

    }

// org.apache.commons.math.optimization.general.NonLinearConjugateGradientOptimizerTest::testMoreEstimatedParametersUnsorted
    public void testMoreEstimatedParametersUnsorted() {
        LinearProblem problem = new LinearProblem(new double[][] {
                 { 1.0, 1.0,  0.0,  0.0, 0.0,  0.0 },
                 { 0.0, 0.0,  1.0,  1.0, 1.0,  0.0 },
                 { 0.0, 0.0,  0.0,  0.0, 1.0, -1.0 },
                 { 0.0, 0.0, -1.0,  1.0, 0.0,  1.0 },
                 { 0.0, 0.0,  0.0, -1.0, 1.0,  0.0 }
        }, new double[] { 3.0, 12.0, -1.0, 7.0, 1.0 });
        NonLinearConjugateGradientOptimizer optimizer =
            new NonLinearConjugateGradientOptimizer(ConjugateGradientFormula.POLAK_RIBIERE);
        optimizer.setConvergenceChecker(new SimpleScalarValueChecker(1.0e-6, 1.0e-6));
        RealPointValuePair optimum =
            optimizer.optimize(100, problem, GoalType.MINIMIZE, new double[] { 2, 2, 2, 2, 2, 2 });
        Assert.assertEquals(0, optimum.getValue(), 1.0e-10);
    }

// org.apache.commons.math.optimization.general.NonLinearConjugateGradientOptimizerTest::testRedundantEquations
    public void testRedundantEquations() {
        LinearProblem problem = new LinearProblem(new double[][] {
                { 1.0,  1.0 },
                { 1.0, -1.0 },
                { 1.0,  3.0 }
        }, new double[] { 3.0, 1.0, 5.0 });

        NonLinearConjugateGradientOptimizer optimizer =
            new NonLinearConjugateGradientOptimizer(ConjugateGradientFormula.POLAK_RIBIERE);
        optimizer.setConvergenceChecker(new SimpleScalarValueChecker(1.0e-6, 1.0e-6));
        RealPointValuePair optimum =
            optimizer.optimize(100, problem, GoalType.MINIMIZE, new double[] { 1, 1 });
        Assert.assertEquals(2.0, optimum.getPoint()[0], 1.0e-8);
        Assert.assertEquals(1.0, optimum.getPoint()[1], 1.0e-8);

    }

// org.apache.commons.math.optimization.general.NonLinearConjugateGradientOptimizerTest::testInconsistentEquations
    public void testInconsistentEquations() {
        LinearProblem problem = new LinearProblem(new double[][] {
                { 1.0,  1.0 },
                { 1.0, -1.0 },
                { 1.0,  3.0 }
        }, new double[] { 3.0, 1.0, 4.0 });

        NonLinearConjugateGradientOptimizer optimizer =
            new NonLinearConjugateGradientOptimizer(ConjugateGradientFormula.POLAK_RIBIERE);
        optimizer.setConvergenceChecker(new SimpleScalarValueChecker(1.0e-6, 1.0e-6));
        RealPointValuePair optimum =
            optimizer.optimize(100, problem, GoalType.MINIMIZE, new double[] { 1, 1 });
        Assert.assertTrue(optimum.getValue() > 0.1);

    }

// org.apache.commons.math.optimization.general.NonLinearConjugateGradientOptimizerTest::testCircleFitting
    public void testCircleFitting() {
        CircleScalar circle = new CircleScalar();
        circle.addPoint( 30.0,  68.0);
        circle.addPoint( 50.0,  -6.0);
        circle.addPoint(110.0, -20.0);
        circle.addPoint( 35.0,  15.0);
        circle.addPoint( 45.0,  97.0);
        NonLinearConjugateGradientOptimizer optimizer =
            new NonLinearConjugateGradientOptimizer(ConjugateGradientFormula.POLAK_RIBIERE);
        optimizer.setConvergenceChecker(new SimpleScalarValueChecker(1.0e-30, 1.0e-30));
        UnivariateRealSolver solver = new BrentSolver(1e-15, 1e-13);
        optimizer.setLineSearchSolver(solver);
        RealPointValuePair optimum =
            optimizer.optimize(100, circle, GoalType.MINIMIZE, new double[] { 98.680, 47.345 });
        Point2D.Double center = new Point2D.Double(optimum.getPointRef()[0], optimum.getPointRef()[1]);
        Assert.assertEquals(69.960161753, circle.getRadius(center), 1.0e-8);
        Assert.assertEquals(96.075902096, center.x, 1.0e-8);
        Assert.assertEquals(48.135167894, center.y, 1.0e-8);
    }

// org.apache.commons.math.optimization.linear.SimplexSolverTest::testMath272
    public void testMath272() throws OptimizationException {
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
    public void testMath286() throws OptimizationException {
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
    public void testDegeneracy() throws OptimizationException {
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
    public void testMath288() throws OptimizationException {
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
    public void testMath290GEQ() throws OptimizationException {
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
    public void testMath290LEQ() throws OptimizationException {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1, 5 }, 0 );
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 2, 0 }, Relationship.LEQ, -1.0));
        SimplexSolver solver = new SimplexSolver();
        solver.optimize(f, constraints, GoalType.MINIMIZE, true);
    }

// org.apache.commons.math.optimization.linear.SimplexSolverTest::testMath293
    public void testMath293() throws OptimizationException {
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
    public void testSimplexSolver() throws OptimizationException {
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
    public void testSingleVariableAndConstraint() throws OptimizationException {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 3 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1 }, Relationship.LEQ, 10));

        SimplexSolver solver = new SimplexSolver();
        RealPointValuePair solution = solver.optimize(f, constraints, GoalType.MAXIMIZE, false);
        Assert.assertEquals(10.0, solution.getPoint()[0], 0.0);
        Assert.assertEquals(30.0, solution.getValue(), 0.0);
    }

// org.apache.commons.math.optimization.linear.SimplexSolverTest::testModelWithNoArtificialVars
    public void testModelWithNoArtificialVars() throws OptimizationException {
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
    public void testMinimization() throws OptimizationException {
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
    public void testSolutionWithNegativeDecisionVariable() throws OptimizationException {
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
    public void testInfeasibleSolution() throws OptimizationException {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 15 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1 }, Relationship.LEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 1 }, Relationship.GEQ, 3));

        SimplexSolver solver = new SimplexSolver();
        solver.optimize(f, constraints, GoalType.MAXIMIZE, false);
    }

// org.apache.commons.math.optimization.linear.SimplexSolverTest::testUnboundedSolution
    public void testUnboundedSolution() throws OptimizationException {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 15, 10 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0 }, Relationship.EQ, 2));

        SimplexSolver solver = new SimplexSolver();
        solver.optimize(f, constraints, GoalType.MAXIMIZE, false);
    }

// org.apache.commons.math.optimization.linear.SimplexSolverTest::testRestrictVariablesToNonNegative
    public void testRestrictVariablesToNonNegative() throws OptimizationException {
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
    public void testEpsilon() throws OptimizationException {
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
    public void testTrivialModel() throws OptimizationException {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1, 1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 1 }, Relationship.EQ,  0));

        SimplexSolver solver = new SimplexSolver();
        RealPointValuePair solution = solver.optimize(f, constraints, GoalType.MAXIMIZE, true);
        Assert.assertEquals(0, solution.getValue(), .0000001);
    }

// org.apache.commons.math.optimization.linear.SimplexSolverTest::testLargeModel
    public void testLargeModel() throws OptimizationException {
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

// org.apache.commons.math.optimization.univariate.BrentOptimizerTest::testSinMin
    public void testSinMin() {
        UnivariateRealFunction f = new SinFunction();
        UnivariateRealOptimizer optimizer = new BrentOptimizer(1e-10, 1e-14);
        assertEquals(3 * Math.PI / 2, optimizer.optimize(200, f, GoalType.MINIMIZE, 4, 5).getPoint(),1e-8);
        assertTrue(optimizer.getEvaluations() <= 50);
        assertEquals(200, optimizer.getMaxEvaluations());
        assertEquals(3 * Math.PI / 2, optimizer.optimize(200, f, GoalType.MINIMIZE, 1, 5).getPoint(), 1e-8);
        assertTrue(optimizer.getEvaluations() <= 100);
        assertTrue(optimizer.getEvaluations() >= 15);
        try {
            optimizer.optimize(10, f, GoalType.MINIMIZE, 4, 5);
            fail("an exception should have been thrown");
        } catch (TooManyEvaluationsException fee) {
            
        }
    }

// org.apache.commons.math.optimization.univariate.BrentOptimizerTest::testQuinticMin
    public void testQuinticMin() {
        
        UnivariateRealFunction f = new QuinticFunction();
        UnivariateRealOptimizer optimizer = new BrentOptimizer(1e-10, 1e-14);
        assertEquals(-0.27195613, optimizer.optimize(200, f, GoalType.MINIMIZE, -0.3, -0.2).getPoint(), 1.0e-8);
        assertEquals( 0.82221643, optimizer.optimize(200, f, GoalType.MINIMIZE,  0.3,  0.9).getPoint(), 1.0e-8);
        assertTrue(optimizer.getEvaluations() <= 50);

        
        assertEquals(-0.27195613, optimizer.optimize(200, f, GoalType.MINIMIZE, -1.0, 0.2).getPoint(), 1.0e-8);
        assertTrue(optimizer.getEvaluations() <= 50);
    }

// org.apache.commons.math.optimization.univariate.BrentOptimizerTest::testQuinticMinStatistics
    public void testQuinticMinStatistics() {
        
        UnivariateRealFunction f = new QuinticFunction();
        UnivariateRealOptimizer optimizer = new BrentOptimizer(1e-11, 1e-14);

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
        assertTrue(meanOptValue > -0.2719561281);
        assertTrue(meanOptValue < -0.2719561280);
        assertEquals(23, (int) medianEval);
    }

// org.apache.commons.math.optimization.univariate.BrentOptimizerTest::testQuinticMax
    public void testQuinticMax() {
        
        
        UnivariateRealFunction f = new QuinticFunction();
        UnivariateRealOptimizer optimizer = new BrentOptimizer(1e-12, 1e-14);
        assertEquals(0.27195613, optimizer.optimize(100, f, GoalType.MAXIMIZE, 0.2, 0.3).getPoint(), 1e-8);
        try {
            optimizer.optimize(5, f, GoalType.MAXIMIZE, 0.2, 0.3);
            fail("an exception should have been thrown");
        } catch (TooManyEvaluationsException miee) {
            
        }
    }

// org.apache.commons.math.optimization.univariate.BrentOptimizerTest::testMinEndpoints
    public void testMinEndpoints() {
        UnivariateRealFunction f = new SinFunction();
        UnivariateRealOptimizer optimizer = new BrentOptimizer(1e-8, 1e-14);

        
        double result = optimizer.optimize(50, f, GoalType.MINIMIZE, 3 * Math.PI / 2, 5).getPoint();
        assertEquals(3 * Math.PI / 2, result, 1e-6);

        result = optimizer.optimize(50, f, GoalType.MINIMIZE, 4, 3 * Math.PI / 2).getPoint();
        assertEquals(3 * Math.PI / 2, result, 1e-6);
    }

// org.apache.commons.math.optimization.univariate.MultiStartUnivariateRealOptimizerTest::testSinMin
    public void testSinMin() {
        UnivariateRealFunction f = new SinFunction();
        UnivariateRealOptimizer underlying = new BrentOptimizer(1e-10, 1e-14);
        JDKRandomGenerator g = new JDKRandomGenerator();
        g.setSeed(44428400075l);
        MultiStartUnivariateRealOptimizer<UnivariateRealFunction> optimizer =
            new MultiStartUnivariateRealOptimizer<UnivariateRealFunction>(underlying, 10, g);
        optimizer.optimize(300, f, GoalType.MINIMIZE, -100.0, 100.0);
        UnivariateRealPointValuePair[] optima = optimizer.getOptima();
        for (int i = 1; i < optima.length; ++i) {
            double d = (optima[i].getPoint() - optima[i-1].getPoint()) / (2 * FastMath.PI);
            assertTrue (FastMath.abs(d - FastMath.rint(d)) < 1.0e-8);
            assertEquals(-1.0, f.value(optima[i].getPoint()), 1.0e-10);
            assertEquals(f.value(optima[i].getPoint()), optima[i].getValue(), 1.0e-10);
        }
        assertTrue(optimizer.getEvaluations() > 200);
        assertTrue(optimizer.getEvaluations() < 300);
    }

// org.apache.commons.math.optimization.univariate.MultiStartUnivariateRealOptimizerTest::testQuinticMin
    public void testQuinticMin() {
        
        
        UnivariateRealFunction f = new QuinticFunction();
        UnivariateRealOptimizer underlying = new BrentOptimizer(1e-9, 1e-14);
        JDKRandomGenerator g = new JDKRandomGenerator();
        g.setSeed(4312000053L);
        MultiStartUnivariateRealOptimizer<UnivariateRealFunction> optimizer =
            new MultiStartUnivariateRealOptimizer<UnivariateRealFunction>(underlying, 5, g);

        UnivariateRealPointValuePair optimum
            = optimizer.optimize(300, f, GoalType.MINIMIZE, -0.3, -0.2);
        assertEquals(-0.2719561293, optimum.getPoint(), 1e-9);
        assertEquals(-0.0443342695, optimum.getValue(), 1e-9);

        UnivariateRealPointValuePair[] optima = optimizer.getOptima();
        for (int i = 0; i < optima.length; ++i) {
            assertEquals(f.value(optima[i].getPoint()), optima[i].getValue(), 1e-9);
        }
        assertTrue(optimizer.getEvaluations() >= 50);
        assertTrue(optimizer.getEvaluations() <= 100);
    }

// org.apache.commons.math.optimization.univariate.MultiStartUnivariateRealOptimizerTest::testBadFunction
    public void testBadFunction() {
        UnivariateRealFunction f = new UnivariateRealFunction() {
                public double value(double x) {
                    if (x < 0) {
                        throw new MathUserException();
                    }
                    return 0;
                }
            };
        UnivariateRealOptimizer underlying = new BrentOptimizer(1e-9, 1e-14);
        JDKRandomGenerator g = new JDKRandomGenerator();
        g.setSeed(4312000053L);
        MultiStartUnivariateRealOptimizer<UnivariateRealFunction> optimizer =
            new MultiStartUnivariateRealOptimizer<UnivariateRealFunction>(underlying, 5, g);
 
        try {
            UnivariateRealPointValuePair optimum
                = optimizer.optimize(300, f, GoalType.MINIMIZE, -0.3, -0.2);
            Assert.fail();
        } catch (MathUserException e) {
            
        }

        
        Assert.assertTrue(optimizer.getOptima()[0] == null);
    }

// org.apache.commons.math.special.BetaTest::testRegularizedBetaNanPositivePositive
    public void testRegularizedBetaNanPositivePositive() {
        testRegularizedBeta(Double.NaN, Double.NaN, 1.0, 1.0);
    }

// org.apache.commons.math.special.BetaTest::testRegularizedBetaPositiveNanPositive
    public void testRegularizedBetaPositiveNanPositive() {
        testRegularizedBeta(Double.NaN, 0.5, Double.NaN, 1.0);
    }

// org.apache.commons.math.special.BetaTest::testRegularizedBetaPositivePositiveNan
    public void testRegularizedBetaPositivePositiveNan() {
        testRegularizedBeta(Double.NaN, 0.5, 1.0, Double.NaN);
    }

// org.apache.commons.math.special.BetaTest::testRegularizedBetaNegativePositivePositive
    public void testRegularizedBetaNegativePositivePositive() {
        testRegularizedBeta(Double.NaN, -0.5, 1.0, 2.0);
    }

// org.apache.commons.math.special.BetaTest::testRegularizedBetaPositiveNegativePositive
    public void testRegularizedBetaPositiveNegativePositive() {
        testRegularizedBeta(Double.NaN, 0.5, -1.0, 2.0);
    }

// org.apache.commons.math.special.BetaTest::testRegularizedBetaPositivePositiveNegative
    public void testRegularizedBetaPositivePositiveNegative() {
        testRegularizedBeta(Double.NaN, 0.5, 1.0, -2.0);
    }

// org.apache.commons.math.special.BetaTest::testRegularizedBetaZeroPositivePositive
    public void testRegularizedBetaZeroPositivePositive() {
        testRegularizedBeta(0.0, 0.0, 1.0, 2.0);
    }

// org.apache.commons.math.special.BetaTest::testRegularizedBetaPositiveZeroPositive
    public void testRegularizedBetaPositiveZeroPositive() {
        testRegularizedBeta(Double.NaN, 0.5, 0.0, 2.0);
    }

// org.apache.commons.math.special.BetaTest::testRegularizedBetaPositivePositiveZero
    public void testRegularizedBetaPositivePositiveZero() {
        testRegularizedBeta(Double.NaN, 0.5, 1.0, 0.0);
    }

// org.apache.commons.math.special.BetaTest::testRegularizedBetaPositivePositivePositive
    public void testRegularizedBetaPositivePositivePositive() {
        testRegularizedBeta(0.75, 0.5, 1.0, 2.0);
    }

// org.apache.commons.math.special.BetaTest::testLogBetaNanPositive
    public void testLogBetaNanPositive() {
        testLogBeta(Double.NaN, Double.NaN, 2.0);
    }

// org.apache.commons.math.special.BetaTest::testLogBetaPositiveNan
    public void testLogBetaPositiveNan() {
        testLogBeta(Double.NaN, 1.0, Double.NaN);
    }

// org.apache.commons.math.special.BetaTest::testLogBetaNegativePositive
    public void testLogBetaNegativePositive() {
        testLogBeta(Double.NaN, -1.0, 2.0);
    }

// org.apache.commons.math.special.BetaTest::testLogBetaPositiveNegative
    public void testLogBetaPositiveNegative() {
        testLogBeta(Double.NaN, 1.0, -2.0);
    }

// org.apache.commons.math.special.BetaTest::testLogBetaZeroPositive
    public void testLogBetaZeroPositive() {
        testLogBeta(Double.NaN, 0.0, 2.0);
    }

// org.apache.commons.math.special.BetaTest::testLogBetaPositiveZero
    public void testLogBetaPositiveZero() {
        testLogBeta(Double.NaN, 1.0, 0.0);
    }

// org.apache.commons.math.special.BetaTest::testLogBetaPositivePositive
    public void testLogBetaPositivePositive() {
        testLogBeta(-0.693147180559945, 1.0, 2.0);
    }

// org.apache.commons.math.special.ErfTest::testErf0
    public void testErf0() throws MathException {
        double actual = Erf.erf(0.0);
        double expected = 0.0;
        assertEquals(expected, actual, 1.0e-15);
        assertEquals(1 - expected, Erf.erfc(0.0), 1.0e-15);
    }

// org.apache.commons.math.special.ErfTest::testErf1960
    public void testErf1960() throws MathException {
        double x = 1.960 / FastMath.sqrt(2.0);
        double actual = Erf.erf(x);
        double expected = 0.95;
        assertEquals(expected, actual, 1.0e-5);
        assertEquals(1 - actual, Erf.erfc(x), 1.0e-15);

        actual = Erf.erf(-x);
        expected = -expected;
        assertEquals(expected, actual, 1.0e-5);
        assertEquals(1 - actual, Erf.erfc(-x), 1.0e-15);
    }

// org.apache.commons.math.special.ErfTest::testErf2576
    public void testErf2576() throws MathException {
        double x = 2.576 / FastMath.sqrt(2.0);
        double actual = Erf.erf(x);
        double expected = 0.99;
        assertEquals(expected, actual, 1.0e-5);
        assertEquals(1 - actual, Erf.erfc(x), 1e-15);

        actual = Erf.erf(-x);
        expected = -expected;
        assertEquals(expected, actual, 1.0e-5);
        assertEquals(1 - actual, Erf.erfc(-x), 1.0e-15);
    }

// org.apache.commons.math.special.ErfTest::testErf2807
    public void testErf2807() throws MathException {
        double x = 2.807 / FastMath.sqrt(2.0);
        double actual = Erf.erf(x);
        double expected = 0.995;
        assertEquals(expected, actual, 1.0e-5);
        assertEquals(1 - actual, Erf.erfc(x), 1.0e-15);

        actual = Erf.erf(-x);
        expected = -expected;
        assertEquals(expected, actual, 1.0e-5);
        assertEquals(1 - actual, Erf.erfc(-x), 1.0e-15);
    }

// org.apache.commons.math.special.ErfTest::testErf3291
    public void testErf3291() throws MathException {
        double x = 3.291 / FastMath.sqrt(2.0);
        double actual = Erf.erf(x);
        double expected = 0.999;
        assertEquals(expected, actual, 1.0e-5);
        assertEquals(1 - expected, Erf.erfc(x), 1.0e-5);

        actual = Erf.erf(-x);
        expected = -expected;
        assertEquals(expected, actual, 1.0e-5);
        assertEquals(1 - expected, Erf.erfc(-x), 1.0e-5);
    }

// org.apache.commons.math.special.ErfTest::testLargeValues
    public void testLargeValues() throws Exception {
        for (int i = 1; i < 200; i*=10) {
            double result = Erf.erf(i);
            assertFalse(Double.isNaN(result));
            assertTrue(result > 0 && result <= 1);
            result = Erf.erf(-i);
            assertFalse(Double.isNaN(result));
            assertTrue(result >= -1 && result < 0);
            result = Erf.erfc(i);
            assertFalse(Double.isNaN(result));
            assertTrue(result >= 0 && result < 1);
            result = Erf.erfc(-i);
            assertFalse(Double.isNaN(result));
            assertTrue(result >= 1 && result <= 2);    
        }
        assertEquals(-1, Erf.erf(Double.NEGATIVE_INFINITY), 0);
        assertEquals(1, Erf.erf(Double.POSITIVE_INFINITY), 0);
        assertEquals(2, Erf.erfc(Double.NEGATIVE_INFINITY), 0);
        assertEquals(0, Erf.erfc(Double.POSITIVE_INFINITY), 0);
    }

// org.apache.commons.math.special.ErfTest::testErfGnu
    public void testErfGnu() throws Exception {
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
            assertEquals(gnuValues[i], Erf.erf(x), tol);
            x += 0.5d;
        }
    }

// org.apache.commons.math.special.ErfTest::testErfcGnu
    public void testErfcGnu() throws Exception {
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
            assertEquals(gnuValues[i], Erf.erfc(x), tol);
            x += 0.5d;
        }
    }

// org.apache.commons.math.special.ErfTest::testErfcMaple
    public void testErfcMaple() throws Exception {
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
            assertEquals(ref[i][1], result, 1E-15);
            TestUtils.assertRelativelyEquals(ref[i][1], result, 1E-13);
        }
    }

// org.apache.commons.math.special.GammaTest::testRegularizedGammaNanPositive
    public void testRegularizedGammaNanPositive() {
        testRegularizedGamma(Double.NaN, Double.NaN, 1.0);
    }

// org.apache.commons.math.special.GammaTest::testRegularizedGammaPositiveNan
    public void testRegularizedGammaPositiveNan() {
        testRegularizedGamma(Double.NaN, 1.0, Double.NaN);
    }

// org.apache.commons.math.special.GammaTest::testRegularizedGammaNegativePositive
    public void testRegularizedGammaNegativePositive() {
        testRegularizedGamma(Double.NaN, -1.5, 1.0);
    }

// org.apache.commons.math.special.GammaTest::testRegularizedGammaPositiveNegative
    public void testRegularizedGammaPositiveNegative() {
        testRegularizedGamma(Double.NaN, 1.0, -1.0);
    }

// org.apache.commons.math.special.GammaTest::testRegularizedGammaZeroPositive
    public void testRegularizedGammaZeroPositive() {
        testRegularizedGamma(Double.NaN, 0.0, 1.0);
    }

// org.apache.commons.math.special.GammaTest::testRegularizedGammaPositiveZero
    public void testRegularizedGammaPositiveZero() {
        testRegularizedGamma(0.0, 1.0, 0.0);
    }

// org.apache.commons.math.special.GammaTest::testRegularizedGammaPositivePositive
    public void testRegularizedGammaPositivePositive() {
        testRegularizedGamma(0.632120558828558, 1.0, 1.0);
    }

// org.apache.commons.math.special.GammaTest::testLogGammaNan
    public void testLogGammaNan() {
        testLogGamma(Double.NaN, Double.NaN);
    }

// org.apache.commons.math.special.GammaTest::testLogGammaNegative
    public void testLogGammaNegative() {
        testLogGamma(Double.NaN, -1.0);
    }

// org.apache.commons.math.special.GammaTest::testLogGammaZero
    public void testLogGammaZero() {
        testLogGamma(Double.NaN, 0.0);
    }

// org.apache.commons.math.special.GammaTest::testLogGammaPositive
    public void testLogGammaPositive() {
        testLogGamma(0.6931471805599457, 3.0);
    }

// org.apache.commons.math.special.GammaTest::testDigammaLargeArgs
    public void testDigammaLargeArgs() {
        double eps = 1e-8;
        assertEquals(4.6001618527380874002, Gamma.digamma(100), eps);
        assertEquals(3.9019896734278921970, Gamma.digamma(50), eps);
        assertEquals(2.9705239922421490509, Gamma.digamma(20), eps);
        assertEquals(2.9958363947076465821, Gamma.digamma(20.5), eps);
        assertEquals(2.2622143570941481605, Gamma.digamma(10.1), eps);
        assertEquals(2.1168588189004379233, Gamma.digamma(8.8), eps);
        assertEquals(1.8727843350984671394, Gamma.digamma(7), eps);
        assertEquals(0.42278433509846713939, Gamma.digamma(2), eps);
        assertEquals(-100.56088545786867450, Gamma.digamma(0.01), eps);
        assertEquals(-4.0390398965921882955, Gamma.digamma(-0.8), eps);
        assertEquals(4.2003210041401844726, Gamma.digamma(-6.3), eps);
    }

// org.apache.commons.math.special.GammaTest::testDigammaSmallArgs
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

// org.apache.commons.math.special.GammaTest::testTrigamma
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
            assertEquals(String.format("trigamma %.0f", data[i]), data[i + 1], Gamma.trigamma(data[i]), eps);
        }
    }

// org.apache.commons.math.stat.CertifiedDataTest::testSummaryStatistics
    public void testSummaryStatistics() throws Exception {
        SummaryStatistics u = new SummaryStatistics();
        loadStats("data/PiDigits.txt", u);
        assertEquals("PiDigits: std", std, u.getStandardDeviation(), 1E-13);
        assertEquals("PiDigits: mean", mean, u.getMean(), 1E-13);

        loadStats("data/Mavro.txt", u);
        assertEquals("Mavro: std", std, u.getStandardDeviation(), 1E-14);
        assertEquals("Mavro: mean", mean, u.getMean(), 1E-14);

        loadStats("data/Michelso.txt", u);
        assertEquals("Michelso: std", std, u.getStandardDeviation(), 1E-13);
        assertEquals("Michelso: mean", mean, u.getMean(), 1E-13);

        loadStats("data/NumAcc1.txt", u);
        assertEquals("NumAcc1: std", std, u.getStandardDeviation(), 1E-14);
        assertEquals("NumAcc1: mean", mean, u.getMean(), 1E-14);

        loadStats("data/NumAcc2.txt", u);
        assertEquals("NumAcc2: std", std, u.getStandardDeviation(), 1E-14);
        assertEquals("NumAcc2: mean", mean, u.getMean(), 1E-14);
    }

// org.apache.commons.math.stat.CertifiedDataTest::testDescriptiveStatistics
    public void testDescriptiveStatistics() throws Exception {

        DescriptiveStatistics u = new DescriptiveStatistics();

        loadStats("data/PiDigits.txt", u);
        assertEquals("PiDigits: std", std, u.getStandardDeviation(), 1E-14);
        assertEquals("PiDigits: mean", mean, u.getMean(), 1E-14);

        loadStats("data/Mavro.txt", u);
        assertEquals("Mavro: std", std, u.getStandardDeviation(), 1E-14);
        assertEquals("Mavro: mean", mean, u.getMean(), 1E-14);

        loadStats("data/Michelso.txt", u);
        assertEquals("Michelso: std", std, u.getStandardDeviation(), 1E-14);
        assertEquals("Michelso: mean", mean, u.getMean(), 1E-14);

        loadStats("data/NumAcc1.txt", u);
        assertEquals("NumAcc1: std", std, u.getStandardDeviation(), 1E-14);
        assertEquals("NumAcc1: mean", mean, u.getMean(), 1E-14);

        loadStats("data/NumAcc2.txt", u);
        assertEquals("NumAcc2: std", std, u.getStandardDeviation(), 1E-14);
        assertEquals("NumAcc2: mean", mean, u.getMean(), 1E-14);
    }

// org.apache.commons.math.stat.StatUtilsTest::testStats
    public void testStats() {
        double[] values = new double[] { one, two, two, three };
        assertEquals("sum", sum, StatUtils.sum(values), tolerance);
        assertEquals("sumsq", sumSq, StatUtils.sumSq(values), tolerance);
        assertEquals("var", var, StatUtils.variance(values), tolerance);
        assertEquals("var with mean", var, StatUtils.variance(values, mean), tolerance);
        assertEquals("mean", mean, StatUtils.mean(values), tolerance);
        assertEquals("min", min, StatUtils.min(values), tolerance);
        assertEquals("max", max, StatUtils.max(values), tolerance);
    }

// org.apache.commons.math.stat.StatUtilsTest::testN0andN1Conditions
    public void testN0andN1Conditions() throws Exception {
        double[] values = new double[0];

        assertTrue(
            "Mean of n = 0 set should be NaN",
            Double.isNaN(StatUtils.mean(values)));
        assertTrue(
            "Variance of n = 0 set should be NaN",
            Double.isNaN(StatUtils.variance(values)));

        values = new double[] { one };

        assertTrue(
            "Mean of n = 1 set should be value of single item n1",
            StatUtils.mean(values) == one);
        assertTrue(
            "Variance of n = 1 set should be zero",
            StatUtils.variance(values) == 0);
    }

// org.apache.commons.math.stat.StatUtilsTest::testArrayIndexConditions
    public void testArrayIndexConditions() throws Exception {
        double[] values = { 1.0, 2.0, 3.0, 4.0 };

        assertEquals(
            "Sum not expected",
            5.0,
            StatUtils.sum(values, 1, 2),
            Double.MIN_VALUE);
        assertEquals(
            "Sum not expected",
            3.0,
            StatUtils.sum(values, 0, 2),
            Double.MIN_VALUE);
        assertEquals(
            "Sum not expected",
            7.0,
            StatUtils.sum(values, 2, 2),
            Double.MIN_VALUE);

        try {
            StatUtils.sum(values, 2, 3);
            fail("Expected RuntimeException");
        } catch (RuntimeException e) {
            
        }

        try {
            StatUtils.sum(values, -1, 2);
            fail("Expected RuntimeException");
        } catch (RuntimeException e) {
            
        }

    }

// org.apache.commons.math.stat.StatUtilsTest::testSumSq
    public void testSumSq() {
        double[] x = null;

        
        try {
            StatUtils.sumSq(x);
            fail("null is not a valid data array.");
        } catch (MathIllegalArgumentException ex) {
            
        }

        try {
            StatUtils.sumSq(x, 0, 4);
            fail("null is not a valid data array.");
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

// org.apache.commons.math.stat.StatUtilsTest::testProduct
    public void testProduct() {
        double[] x = null;

        
        try {
            StatUtils.product(x);
            fail("null is not a valid data array.");
        } catch (MathIllegalArgumentException ex) {
            
        }

        try {
            StatUtils.product(x, 0, 4);
            fail("null is not a valid data array.");
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

// org.apache.commons.math.stat.StatUtilsTest::testSumLog
    public void testSumLog() {
        double[] x = null;

        
        try {
            StatUtils.sumLog(x);
            fail("null is not a valid data array.");
        } catch (MathIllegalArgumentException ex) {
            
        }

        try {
            StatUtils.sumLog(x, 0, 4);
            fail("null is not a valid data array.");
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

// org.apache.commons.math.stat.StatUtilsTest::testMean
    public void testMean() {
        double[] x = null;

        try {
            StatUtils.mean(x, 0, 4);
            fail("null is not a valid data array.");
        } catch (MathIllegalArgumentException ex) {
            
        }

        
        x = new double[] {};
        TestUtils.assertEquals(Double.NaN, StatUtils.mean(x, 0, 0), tolerance);

        
        x = new double[] {two};
        TestUtils.assertEquals(two, StatUtils.mean(x, 0, 1), tolerance);

        
        x = new double[] {one, two, two, three};
        TestUtils.assertEquals(2.5, StatUtils.mean(x, 2, 2), tolerance);
    }

// org.apache.commons.math.stat.StatUtilsTest::testVariance
    public void testVariance() {
        double[] x = null;

        try {
            StatUtils.variance(x, 0, 4);
            fail("null is not a valid data array.");
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

// org.apache.commons.math.stat.StatUtilsTest::testMax
    public void testMax() {
        double[] x = null;

        try {
            StatUtils.max(x, 0, 4);
            fail("null is not a valid data array.");
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

// org.apache.commons.math.stat.StatUtilsTest::testMin
    public void testMin() {
        double[] x = null;

        try {
            StatUtils.min(x, 0, 4);
            fail("null is not a valid data array.");
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

// org.apache.commons.math.stat.StatUtilsTest::testPercentile
    public void testPercentile() {
        double[] x = null;

        
        try {
            StatUtils.percentile(x, .25);
            fail("null is not a valid data array.");
        } catch (MathIllegalArgumentException ex) {
            
        }

        try {
            StatUtils.percentile(x, 0, 4, 0.25);
            fail("null is not a valid data array.");
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

// org.apache.commons.math.stat.StatUtilsTest::testDifferenceStats
    public void testDifferenceStats() throws Exception {
        double sample1[] = {1d, 2d, 3d, 4d};
        double sample2[] = {1d, 3d, 4d, 2d};
        double diff[] = {0d, -1d, -1d, 2d};
        double small[] = {1d, 4d};
        double meanDifference = StatUtils.meanDifference(sample1, sample2);
        assertEquals(StatUtils.sumDifference(sample1, sample2), StatUtils.sum(diff), tolerance);
        assertEquals(meanDifference, StatUtils.mean(diff), tolerance);
        assertEquals(StatUtils.varianceDifference(sample1, sample2, meanDifference),
                StatUtils.variance(diff), tolerance);
        try {
            StatUtils.meanDifference(sample1, small);
            fail("Expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
        try {
            StatUtils.varianceDifference(sample1, small, meanDifference);
            fail("Expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
        try {
            double[] single = {1.0};
            StatUtils.varianceDifference(single, single, meanDifference);
            fail("Expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.stat.StatUtilsTest::testGeometricMean
    public void testGeometricMean() throws Exception {
        double[] test = null;
        try {
            StatUtils.geometricMean(test);
            fail("Expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
        test = new double[] {2, 4, 6, 8};
        assertEquals(FastMath.exp(0.25d * StatUtils.sumLog(test)),
                StatUtils.geometricMean(test), Double.MIN_VALUE);
        assertEquals(FastMath.exp(0.5 * StatUtils.sumLog(test, 0, 2)),
                StatUtils.geometricMean(test, 0, 2), Double.MIN_VALUE);
    }

// org.apache.commons.math.stat.StatUtilsTest::testNormalize1
	public void testNormalize1() {
		double sample[] = { 50, 100 };
		double expectedSample[] = { -25 / Math.sqrt(1250), 25 / Math.sqrt(1250) };
		double[] out = StatUtils.normalize(sample);
		for (int i = 0; i < out.length; i++) {
			assertEquals(out[i], expectedSample[i]);
		}

	}

// org.apache.commons.math.stat.StatUtilsTest::testNormalize2
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
		
		assertEquals(0.0, stats.getMean(), distance);
		assertEquals(1.0, stats.getStandardDeviation(), distance);

	}

// org.apache.commons.math.stat.clustering.EuclideanIntegerPointTest::testArrayIsReference
    public void testArrayIsReference() {
        int[] array = { -3, -2, -1, 0, 1 };
        assertTrue(array == new EuclideanIntegerPoint(array).getPoint());
    }

// org.apache.commons.math.stat.clustering.EuclideanIntegerPointTest::testDistance
    public void testDistance() {
        EuclideanIntegerPoint e1 = new EuclideanIntegerPoint(new int[] { -3, -2, -1, 0, 1 });
        EuclideanIntegerPoint e2 = new EuclideanIntegerPoint(new int[] {  1,  0, -1, 1, 1 });
        assertEquals(FastMath.sqrt(21.0), e1.distanceFrom(e2), 1.0e-15);
        assertEquals(0.0, e1.distanceFrom(e1), 1.0e-15);
        assertEquals(0.0, e2.distanceFrom(e2), 1.0e-15);
    }

// org.apache.commons.math.stat.clustering.EuclideanIntegerPointTest::testCentroid
    public void testCentroid() {
        List<EuclideanIntegerPoint> list = new ArrayList<EuclideanIntegerPoint>();
        list.add(new EuclideanIntegerPoint(new int[] {  1,  3 }));
        list.add(new EuclideanIntegerPoint(new int[] {  2,  2 }));
        list.add(new EuclideanIntegerPoint(new int[] {  3,  3 }));
        list.add(new EuclideanIntegerPoint(new int[] {  2,  4 }));
        EuclideanIntegerPoint c = list.get(0).centroidOf(list);
        assertEquals(2, c.getPoint()[0]);
        assertEquals(3, c.getPoint()[1]);
    }

// org.apache.commons.math.stat.clustering.EuclideanIntegerPointTest::testSerial
    public void testSerial() {
        EuclideanIntegerPoint p = new EuclideanIntegerPoint(new int[] { -3, -2, -1, 0, 1 });
        assertEquals(p, TestUtils.serializeAndRecover(p));
    }

// org.apache.commons.math.stat.clustering.KMeansPlusPlusClustererTest::dimension2
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
            transformer.cluster(Arrays.asList(points), 3, 10);

        assertEquals(3, clusters.size());
        boolean cluster1Found = false;
        boolean cluster2Found = false;
        boolean cluster3Found = false;
        for (Cluster<EuclideanIntegerPoint> cluster : clusters) {
            int[] center = cluster.getCenter().getPoint();
            if (center[0] < 0) {
                cluster1Found = true;
                assertEquals(8, cluster.getPoints().size());
                assertEquals(-14, center[0]);
                assertEquals( 4, center[1]);
            } else if (center[1] < 0) {
                cluster2Found = true;
                assertEquals(5, cluster.getPoints().size());
                assertEquals( 0, center[0]);
                assertEquals(-1, center[1]);
            } else {
                cluster3Found = true;
                assertEquals(8, cluster.getPoints().size());
                assertEquals(15, center[0]);
                assertEquals(5, center[1]);
            }
        }
        assertTrue(cluster1Found);
        assertTrue(cluster2Found);
        assertTrue(cluster3Found);

    }

// org.apache.commons.math.stat.clustering.KMeansPlusPlusClustererTest::testPerformClusterAnalysisDegenerate
    public void testPerformClusterAnalysisDegenerate() {
        KMeansPlusPlusClusterer<EuclideanIntegerPoint> transformer = new KMeansPlusPlusClusterer<EuclideanIntegerPoint>(
                new Random(1746432956321l));
        EuclideanIntegerPoint[] points = new EuclideanIntegerPoint[] {
                new EuclideanIntegerPoint(new int[] { 1959, 325100 }),
                new EuclideanIntegerPoint(new int[] { 1960, 373200 }), };
        List<Cluster<EuclideanIntegerPoint>> clusters = transformer.cluster(Arrays.asList(points), 1, 1);
        assertEquals(1, clusters.size());
        assertEquals(2, (clusters.get(0).getPoints().size()));
        EuclideanIntegerPoint pt1 = new EuclideanIntegerPoint(new int[] { 1959, 325100 });
        EuclideanIntegerPoint pt2 = new EuclideanIntegerPoint(new int[] { 1960, 373200 });
        assertTrue(clusters.get(0).getPoints().contains(pt1));
        assertTrue(clusters.get(0).getPoints().contains(pt2));

    }

// org.apache.commons.math.stat.clustering.KMeansPlusPlusClustererTest::testCertainSpace
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

// org.apache.commons.math.stat.correlation.CovarianceTest::testLongly
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

// org.apache.commons.math.stat.correlation.CovarianceTest::testSwissFertility
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

// org.apache.commons.math.stat.correlation.CovarianceTest::testConstant
    public void testConstant() {
        double[] noVariance = new double[] {1, 1, 1, 1};
        double[] values = new double[] {1, 2, 3, 4};
        assertEquals(0d, new Covariance().covariance(noVariance, values, true), Double.MIN_VALUE);
        assertEquals(0d, new Covariance().covariance(noVariance, noVariance, true), Double.MIN_VALUE);
    }

// org.apache.commons.math.stat.correlation.CovarianceTest::testInsufficientData
    public void testInsufficientData() {
        double[] one = new double[] {1};
        double[] two = new double[] {2};
        try {
            new Covariance().covariance(one, two, false);
            fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }
        RealMatrix matrix = new Array2DRowRealMatrix(new double[][] {{0},{1}});
        try {
            new Covariance(matrix);
            fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.stat.correlation.CovarianceTest::testConsistency
    public void testConsistency() {
        final RealMatrix matrix = createRealMatrix(swissData, 47, 5);
        final RealMatrix covarianceMatrix = new Covariance(matrix).getCovarianceMatrix();

        
        Variance variance = new Variance();
        for (int i = 0; i < 5; i++) {
            assertEquals(variance.evaluate(matrix.getColumn(i)), covarianceMatrix.getEntry(i,i), 10E-14);
        }

        
        assertEquals(covarianceMatrix.getEntry(2, 3),
                new Covariance().covariance(matrix.getColumn(2), matrix.getColumn(3), true), 10E-14);
        assertEquals(covarianceMatrix.getEntry(2, 3), covarianceMatrix.getEntry(3, 2), Double.MIN_VALUE);

        
        RealMatrix repeatedColumns = new Array2DRowRealMatrix(47, 3);
        for (int i = 0; i < 3; i++) {
            repeatedColumns.setColumnMatrix(i, matrix.getColumnMatrix(0));
        }
        RealMatrix repeatedCovarianceMatrix = new Covariance(repeatedColumns).getCovarianceMatrix();
        double columnVariance = variance.evaluate(matrix.getColumn(0));
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                assertEquals(columnVariance, repeatedCovarianceMatrix.getEntry(i, j), 10E-14);
            }
        }

        
        double[][] data = matrix.getData();
        TestUtils.assertEquals("Covariances",
                covarianceMatrix, new Covariance().computeCovarianceMatrix(data),Double.MIN_VALUE);
        TestUtils.assertEquals("Covariances",
                covarianceMatrix, new Covariance().computeCovarianceMatrix(data, true),Double.MIN_VALUE);

        double[] x = data[0];
        double[] y = data[1];
        assertEquals(new Covariance().covariance(x, y),
                new Covariance().covariance(x, y, true), Double.MIN_VALUE);
    }

// org.apache.commons.math.stat.correlation.PearsonsCorrelationTest::testLongly
    public void testLongly() throws Exception {
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

// org.apache.commons.math.stat.correlation.PearsonsCorrelationTest::testSwissFertility
    public void testSwissFertility() throws Exception {
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

// org.apache.commons.math.stat.correlation.PearsonsCorrelationTest::testPValueNearZero
    public void testPValueNearZero() throws Exception {
        
        int dimension = 120; 
        double[][] data = new double[dimension][2];
        for (int i = 0; i < dimension; i++) {
            data[i][0] = i;
            data[i][1] = i + 1/((double)i + 1);
        }
        PearsonsCorrelation corrInstance = new PearsonsCorrelation(data);
        assertTrue(corrInstance.getCorrelationPValues().getEntry(0, 1) > 0);
    }

// org.apache.commons.math.stat.correlation.PearsonsCorrelationTest::testConstant
    public void testConstant() {
        double[] noVariance = new double[] {1, 1, 1, 1};
        double[] values = new double[] {1, 2, 3, 4};
        assertTrue(Double.isNaN(new PearsonsCorrelation().correlation(noVariance, values)));
    }

// org.apache.commons.math.stat.correlation.PearsonsCorrelationTest::testInsufficientData
    public void testInsufficientData() {
        double[] one = new double[] {1};
        double[] two = new double[] {2};
        try {
            new PearsonsCorrelation().correlation(one, two);
            fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }
        RealMatrix matrix = new BlockRealMatrix(new double[][] {{0},{1}});
        try {
            new PearsonsCorrelation(matrix);
            fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.stat.correlation.PearsonsCorrelationTest::testStdErrorConsistency
    public void testStdErrorConsistency() throws Exception {
        TDistribution tDistribution = new TDistributionImpl(45);
        RealMatrix matrix = createRealMatrix(swissData, 47, 5);
        PearsonsCorrelation corrInstance = new PearsonsCorrelation(matrix);
        RealMatrix rValues = corrInstance.getCorrelationMatrix();
        RealMatrix pValues = corrInstance.getCorrelationPValues();
        RealMatrix stdErrors = corrInstance.getCorrelationStandardErrors();
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < i; j++) {
                double t = FastMath.abs(rValues.getEntry(i, j)) / stdErrors.getEntry(i, j);
                double p = 2 * (1 - tDistribution.cumulativeProbability(t));
                assertEquals(p, pValues.getEntry(i, j), 10E-15);
            }
        }
    }

// org.apache.commons.math.stat.correlation.PearsonsCorrelationTest::testCovarianceConsistency
    public void testCovarianceConsistency() throws Exception {
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

// org.apache.commons.math.stat.correlation.PearsonsCorrelationTest::testConsistency
    public void testConsistency() {
        RealMatrix matrix = createRealMatrix(longleyData, 16, 7);
        PearsonsCorrelation corrInstance = new PearsonsCorrelation(matrix);
        double[][] data = matrix.getData();
        double[] x = matrix.getColumn(0);
        double[] y = matrix.getColumn(1);
        assertEquals(new PearsonsCorrelation().correlation(x, y),
                corrInstance.getCorrelationMatrix().getEntry(0, 1), Double.MIN_VALUE);
        TestUtils.assertEquals("Correlation matrix", corrInstance.getCorrelationMatrix(),
                new PearsonsCorrelation().computeCorrelationMatrix(data), Double.MIN_VALUE);
    }

// org.apache.commons.math.stat.correlation.SpearmansRankCorrelationTest::testLongly
    public void testLongly() throws Exception {
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

// org.apache.commons.math.stat.correlation.SpearmansRankCorrelationTest::testSwiss
    public void testSwiss() throws Exception {
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

// org.apache.commons.math.stat.correlation.SpearmansRankCorrelationTest::testConstant
    public void testConstant() {
        double[] noVariance = new double[] {1, 1, 1, 1};
        double[] values = new double[] {1, 2, 3, 4};
        assertTrue(Double.isNaN(new SpearmansCorrelation().correlation(noVariance, values)));
    }

// org.apache.commons.math.stat.correlation.SpearmansRankCorrelationTest::testInsufficientData
    public void testInsufficientData() {
        double[] one = new double[] {1};
        double[] two = new double[] {2};
        try {
            new SpearmansCorrelation().correlation(one, two);
            fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }
        RealMatrix matrix = new BlockRealMatrix(new double[][] {{0},{1}});
        try {
            new SpearmansCorrelation(matrix);
            fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.stat.correlation.SpearmansRankCorrelationTest::testConsistency
    public void testConsistency() {
        RealMatrix matrix = createRealMatrix(longleyData, 16, 7);
        SpearmansCorrelation corrInstance = new SpearmansCorrelation(matrix);
        double[][] data = matrix.getData();
        double[] x = matrix.getColumn(0);
        double[] y = matrix.getColumn(1);
        assertEquals(new SpearmansCorrelation().correlation(x, y),
                corrInstance.getCorrelationMatrix().getEntry(0, 1), Double.MIN_VALUE);
        TestUtils.assertEquals("Correlation matrix", corrInstance.getCorrelationMatrix(),
                new SpearmansCorrelation().computeCorrelationMatrix(data), Double.MIN_VALUE);
    }

// org.apache.commons.math.stat.correlation.SpearmansRankCorrelationTest::testStdErrorConsistency
    public void testStdErrorConsistency() throws Exception {}

// org.apache.commons.math.stat.correlation.SpearmansRankCorrelationTest::testCovarianceConsistency
    public void testCovarianceConsistency() throws Exception {}

// org.apache.commons.math.stat.descriptive.AggregateSummaryStatisticsTest::testAggregation
    public void testAggregation() {
        AggregateSummaryStatistics aggregate = new AggregateSummaryStatistics();
        SummaryStatistics setOneStats = aggregate.createContributingStatistics();
        SummaryStatistics setTwoStats = aggregate.createContributingStatistics();

        assertNotNull("The set one contributing stats are null", setOneStats);
        assertNotNull("The set two contributing stats are null", setTwoStats);
        assertNotSame("Contributing stats objects are the same", setOneStats, setTwoStats);

        setOneStats.addValue(2);
        setOneStats.addValue(3);
        setOneStats.addValue(5);
        setOneStats.addValue(7);
        setOneStats.addValue(11);
        assertEquals("Wrong number of set one values", 5, setOneStats.getN());
        assertEquals("Wrong sum of set one values", 28.0, setOneStats.getSum());

        setTwoStats.addValue(2);
        setTwoStats.addValue(4);
        setTwoStats.addValue(8);
        assertEquals("Wrong number of set two values", 3, setTwoStats.getN());
        assertEquals("Wrong sum of set two values", 14.0, setTwoStats.getSum());

        assertEquals("Wrong number of aggregate values", 8, aggregate.getN());
        assertEquals("Wrong aggregate sum", 42.0, aggregate.getSum());
    }

// org.apache.commons.math.stat.descriptive.AggregateSummaryStatisticsTest::testAggregationConsistency
    public void testAggregationConsistency() throws Exception {

        
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

        
        assertEquals(totalStats.getSummary(), aggregate.getSummary());

    }

// org.apache.commons.math.stat.descriptive.AggregateSummaryStatisticsTest::testAggregate
    public void testAggregate() throws Exception {

        
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

// org.apache.commons.math.stat.descriptive.AggregateSummaryStatisticsTest::testAggregateDegenerate
    public void testAggregateDegenerate() throws Exception {
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

// org.apache.commons.math.stat.descriptive.AggregateSummaryStatisticsTest::testAggregateSpecialValues
    public void testAggregateSpecialValues() throws Exception {
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

// org.apache.commons.math.stat.descriptive.DescriptiveStatisticsTest::testSetterInjection
    public void testSetterInjection() {
        DescriptiveStatistics stats = createDescriptiveStatistics();
        stats.addValue(1);
        stats.addValue(3);
        assertEquals(2, stats.getMean(), 1E-10);
        
        stats.setMeanImpl(new deepMean());
        assertEquals(42, stats.getMean(), 1E-10);
    }

// org.apache.commons.math.stat.descriptive.DescriptiveStatisticsTest::testCopy
    public void testCopy() {
        DescriptiveStatistics stats = createDescriptiveStatistics();
        stats.addValue(1);
        stats.addValue(3);
        DescriptiveStatistics copy = new DescriptiveStatistics(stats);
        assertEquals(2, copy.getMean(), 1E-10);
        
        stats.setMeanImpl(new deepMean());
        copy = stats.copy();
        assertEquals(42, copy.getMean(), 1E-10);
    }

// org.apache.commons.math.stat.descriptive.DescriptiveStatisticsTest::testWindowSize
    public void testWindowSize() {
        DescriptiveStatistics stats = createDescriptiveStatistics();
        stats.setWindowSize(300);
        for (int i = 0; i < 100; ++i) {
            stats.addValue(i + 1);
        }
        int refSum = (100 * 101) / 2;
        assertEquals(refSum / 100.0, stats.getMean(), 1E-10);
        assertEquals(300, stats.getWindowSize());
        try {
            stats.setWindowSize(-3);
            fail("an exception should have been thrown");
        } catch (IllegalArgumentException iae) {
            
        }
        assertEquals(300, stats.getWindowSize());
        stats.setWindowSize(50);
        assertEquals(50, stats.getWindowSize());
        int refSum2 = refSum - (50 * 51) / 2;
        assertEquals(refSum2 / 50.0, stats.getMean(), 1E-10);
    }

// org.apache.commons.math.stat.descriptive.DescriptiveStatisticsTest::testGetValues
    public void testGetValues() {
        DescriptiveStatistics stats = createDescriptiveStatistics();
        for (int i = 100; i > 0; --i) {
            stats.addValue(i);
        }
        int refSum = (100 * 101) / 2;
        assertEquals(refSum / 100.0, stats.getMean(), 1E-10);
        double[] v = stats.getValues();
        for (int i = 0; i < v.length; ++i) {
            assertEquals(100.0 - i, v[i], 1.0e-10);
        }
        double[] s = stats.getSortedValues();
        for (int i = 0; i < s.length; ++i) {
            assertEquals(i + 1.0, s[i], 1.0e-10);
        }
        assertEquals(12.0, stats.getElement(88), 1.0e-10);
    }

// org.apache.commons.math.stat.descriptive.DescriptiveStatisticsTest::testToString
    public void testToString() {
        DescriptiveStatistics stats = createDescriptiveStatistics();
        stats.addValue(1);
        stats.addValue(2);
        stats.addValue(3);
        Locale d = Locale.getDefault();
        Locale.setDefault(Locale.US);
        assertEquals("DescriptiveStatistics:\n" +
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

// org.apache.commons.math.stat.descriptive.DescriptiveStatisticsTest::testShuffledStatistics
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

        assertEquals(reference.getMean(),          shuffled.getGeometricMean(), 1.0e-10);
        assertEquals(reference.getKurtosis(),      shuffled.getMean(),          1.0e-10);
        assertEquals(reference.getSkewness(),      shuffled.getKurtosis(), 1.0e-10);
        assertEquals(reference.getVariance(),      shuffled.getSkewness(), 1.0e-10);
        assertEquals(reference.getMax(),           shuffled.getVariance(), 1.0e-10);
        assertEquals(reference.getMin(),           shuffled.getMax(), 1.0e-10);
        assertEquals(reference.getSum(),           shuffled.getMin(), 1.0e-10);
        assertEquals(reference.getSumsq(),         shuffled.getSum(), 1.0e-10);
        assertEquals(reference.getGeometricMean(), shuffled.getSumsq(), 1.0e-10);

    }

// org.apache.commons.math.stat.descriptive.DescriptiveStatisticsTest::testPercentileSetter
    public void testPercentileSetter() throws Exception {
        DescriptiveStatistics stats = createDescriptiveStatistics();
        stats.addValue(1);
        stats.addValue(2);
        stats.addValue(3);
        assertEquals(2, stats.getPercentile(50.0), 1E-10);

        
        stats.setPercentileImpl(new goodPercentile());
        assertEquals(2, stats.getPercentile(50.0), 1E-10);

        
        stats.setPercentileImpl(new subPercentile());
        assertEquals(10.0, stats.getPercentile(10.0), 1E-10);

        
        try {
            stats.setPercentileImpl(new badPercentile());
            fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.stat.descriptive.DescriptiveStatisticsTest::test20090720
    public void test20090720() {
        DescriptiveStatistics descriptiveStatistics = new DescriptiveStatistics(100);
        for (int i = 0; i < 161; i++) {
            descriptiveStatistics.addValue(1.2);
        }
        descriptiveStatistics.clear();
        descriptiveStatistics.addValue(1.2);
        assertEquals(1, descriptiveStatistics.getN());
    }

// org.apache.commons.math.stat.descriptive.DescriptiveStatisticsTest::testRemoval
    public void testRemoval() {

        final DescriptiveStatistics dstat = createDescriptiveStatistics();

        checkremoval(dstat, 1, 6.0, 0.0, Double.NaN);
        checkremoval(dstat, 3, 5.0, 3.0, 4.5);
        checkremoval(dstat, 6, 3.5, 2.5, 3.0);
        checkremoval(dstat, 9, 3.5, 2.5, 3.0);
        checkremoval(dstat, DescriptiveStatistics.INFINITE_WINDOW, 3.5, 2.5, 3.0);

    }

// org.apache.commons.math.stat.descriptive.InteractionTest::testInteraction
    public void testInteraction() {

        FourthMoment m4 = new FourthMoment();
        Mean m = new Mean(m4);
        Variance v = new Variance(m4);
        Skewness s= new Skewness(m4);
        Kurtosis k = new Kurtosis(m4);

        for (int i = 0; i < testArray.length; i++){
            m4.increment(testArray[i]);
        }

        assertEquals(mean,m.getResult(),tolerance);
        assertEquals(var,v.getResult(),tolerance);
        assertEquals(skew ,s.getResult(),tolerance);
        assertEquals(kurt,k.getResult(),tolerance);

    }

// org.apache.commons.math.stat.descriptive.ListUnivariateImplTest::testStats
    public void testStats() {
        List<Object> externalList = new ArrayList<Object>();

        DescriptiveStatistics u = new ListUnivariateImpl( externalList );

        assertEquals("total count",0,u.getN(),tolerance);
        u.addValue(one);
        u.addValue(two);
        u.addValue(two);
        u.addValue(three);
        assertEquals("N",n,u.getN(),tolerance);
        assertEquals("sum",sum,u.getSum(),tolerance);
        assertEquals("sumsq",sumSq,u.getSumsq(),tolerance);
        assertEquals("var",var,u.getVariance(),tolerance);
        assertEquals("std",std,u.getStandardDeviation(),tolerance);
        assertEquals("mean",mean,u.getMean(),tolerance);
        assertEquals("min",min,u.getMin(),tolerance);
        assertEquals("max",max,u.getMax(),tolerance);
        u.clear();
        assertEquals("total count",0,u.getN(),tolerance);
    }

// org.apache.commons.math.stat.descriptive.ListUnivariateImplTest::testN0andN1Conditions
    public void testN0andN1Conditions() throws Exception {
        List<Object> list = new ArrayList<Object>();

        DescriptiveStatistics u = new ListUnivariateImpl( list );

        assertTrue("Mean of n = 0 set should be NaN", Double.isNaN( u.getMean() ) );
        assertTrue("Standard Deviation of n = 0 set should be NaN", Double.isNaN( u.getStandardDeviation() ) );
        assertTrue("Variance of n = 0 set should be NaN", Double.isNaN(u.getVariance() ) );

        list.add( Double.valueOf(one));

        assertTrue( "Mean of n = 1 set should be value of single item n1", u.getMean() == one);
        assertTrue( "StdDev of n = 1 set should be zero, instead it is: " + u.getStandardDeviation(), u.getStandardDeviation() == 0);
        assertTrue( "Variance of n = 1 set should be zero", u.getVariance() == 0);
    }

// org.apache.commons.math.stat.descriptive.ListUnivariateImplTest::testSkewAndKurtosis
    public void testSkewAndKurtosis() {
        DescriptiveStatistics u = new DescriptiveStatistics();

        double[] testArray = { 12.5, 12, 11.8, 14.2, 14.9, 14.5, 21, 8.2, 10.3, 11.3, 14.1,
                                             9.9, 12.2, 12, 12.1, 11, 19.8, 11, 10, 8.8, 9, 12.3 };
        for( int i = 0; i < testArray.length; i++) {
            u.addValue( testArray[i]);
        }

        assertEquals("mean", 12.40455, u.getMean(), 0.0001);
        assertEquals("variance", 10.00236, u.getVariance(), 0.0001);
        assertEquals("skewness", 1.437424, u.getSkewness(), 0.0001);
        assertEquals("kurtosis", 2.37719, u.getKurtosis(), 0.0001);
    }

// org.apache.commons.math.stat.descriptive.ListUnivariateImplTest::testProductAndGeometricMean
    public void testProductAndGeometricMean() throws Exception {
        ListUnivariateImpl u = new ListUnivariateImpl(new ArrayList<Object>());
        u.setWindowSize(10);

        u.addValue( 1.0 );
        u.addValue( 2.0 );
        u.addValue( 3.0 );
        u.addValue( 4.0 );

        assertEquals( "Geometric mean not expected", 2.213364, u.getGeometricMean(), 0.00001 );

        
        
        for( int i = 0; i < 10; i++ ) {
            u.addValue( i + 2 );
        }
        

        assertEquals( "Geometric mean not expected", 5.755931, u.getGeometricMean(), 0.00001 );

    }

// org.apache.commons.math.stat.descriptive.ListUnivariateImplTest::testSerialization
    public void testSerialization() {

        DescriptiveStatistics u = new ListUnivariateImpl();

        assertEquals("total count",0,u.getN(),tolerance);
        u.addValue(one);
        u.addValue(two);

        DescriptiveStatistics u2 = (DescriptiveStatistics)TestUtils.serializeAndRecover(u);

        u2.addValue(two);
        u2.addValue(three);

        assertEquals("N",n,u2.getN(),tolerance);
        assertEquals("sum",sum,u2.getSum(),tolerance);
        assertEquals("sumsq",sumSq,u2.getSumsq(),tolerance);
        assertEquals("var",var,u2.getVariance(),tolerance);
        assertEquals("std",std,u2.getStandardDeviation(),tolerance);
        assertEquals("mean",mean,u2.getMean(),tolerance);
        assertEquals("min",min,u2.getMin(),tolerance);
        assertEquals("max",max,u2.getMax(),tolerance);

        u2.clear();
        assertEquals("total count",0,u2.getN(),tolerance);
    }

// org.apache.commons.math.stat.descriptive.MixedListUnivariateImplTest::testStats
    public void testStats() {
        List<Object> externalList = new ArrayList<Object>();

        DescriptiveStatistics u = new ListUnivariateImpl(externalList,transformers);

        assertEquals("total count", 0, u.getN(), tolerance);
        u.addValue(one);
        u.addValue(two);
        u.addValue(two);
        u.addValue(three);
        assertEquals("N", n, u.getN(), tolerance);
        assertEquals("sum", sum, u.getSum(), tolerance);
        assertEquals("sumsq", sumSq, u.getSumsq(), tolerance);
        assertEquals("var", var, u.getVariance(), tolerance);
        assertEquals("std", std, u.getStandardDeviation(), tolerance);
        assertEquals("mean", mean, u.getMean(), tolerance);
        assertEquals("min", min, u.getMin(), tolerance);
        assertEquals("max", max, u.getMax(), tolerance);
        u.clear();
        assertEquals("total count", 0, u.getN(), tolerance);
    }

// org.apache.commons.math.stat.descriptive.MixedListUnivariateImplTest::testN0andN1Conditions
    public void testN0andN1Conditions() throws Exception {
        DescriptiveStatistics u = new ListUnivariateImpl(new ArrayList<Object>(),transformers);

        assertTrue(
            "Mean of n = 0 set should be NaN",
            Double.isNaN(u.getMean()));
        assertTrue(
            "Standard Deviation of n = 0 set should be NaN",
            Double.isNaN(u.getStandardDeviation()));
        assertTrue(
            "Variance of n = 0 set should be NaN",
            Double.isNaN(u.getVariance()));

        u.addValue(one);

        assertTrue(
            "Mean of n = 1 set should be value of single item n1, instead it is " + u.getMean() ,
            u.getMean() == one);

        assertTrue(
            "StdDev of n = 1 set should be zero, instead it is: "
                + u.getStandardDeviation(),
            u.getStandardDeviation() == 0);
        assertTrue(
            "Variance of n = 1 set should be zero",
            u.getVariance() == 0);
    }

// org.apache.commons.math.stat.descriptive.MixedListUnivariateImplTest::testSkewAndKurtosis
    public void testSkewAndKurtosis() {
        ListUnivariateImpl u =
            new ListUnivariateImpl(new ArrayList<Object>(), transformers);

        u.addObject("12.5");
        u.addObject(Integer.valueOf(12));
        u.addObject("11.8");
        u.addObject("14.2");
        u.addObject(new Foo());
        u.addObject("14.5");
        u.addObject(Long.valueOf(21));
        u.addObject("8.2");
        u.addObject("10.3");
        u.addObject("11.3");
        u.addObject(Float.valueOf(14.1f));
        u.addObject("9.9");
        u.addObject("12.2");
        u.addObject(new Bar());
        u.addObject("12.1");
        u.addObject("11");
        u.addObject(Double.valueOf(19.8));
        u.addObject("11");
        u.addObject("10");
        u.addObject("8.8");
        u.addObject("9");
        u.addObject("12.3");

        assertEquals("mean", 12.40455, u.getMean(), 0.0001);
        assertEquals("variance", 10.00236, u.getVariance(), 0.0001);
        assertEquals("skewness", 1.437424, u.getSkewness(), 0.0001);
        assertEquals("kurtosis", 2.37719, u.getKurtosis(), 0.0001);
    }

// org.apache.commons.math.stat.descriptive.MixedListUnivariateImplTest::testProductAndGeometricMean
    public void testProductAndGeometricMean() throws Exception {
        ListUnivariateImpl u = new ListUnivariateImpl(new ArrayList<Object>(),transformers);
        u.setWindowSize(10);

        u.addValue(1.0);
        u.addValue(2.0);
        u.addValue(3.0);
        u.addValue(4.0);

        assertEquals(
            "Geometric mean not expected",
            2.213364,
            u.getGeometricMean(),
            0.00001);

        
        
        for (int i = 0; i < 10; i++) {
            u.addValue(i + 2);
        }
        
        assertEquals(
            "Geometric mean not expected",
            5.755931,
            u.getGeometricMean(),
            0.00001);

    }

// org.apache.commons.math.stat.descriptive.MultivariateSummaryStatisticsTest::testSetterInjection
    public void testSetterInjection() throws Exception {
        MultivariateSummaryStatistics u = createMultivariateSummaryStatistics(2, true);
        u.setMeanImpl(new StorelessUnivariateStatistic[] {
                        new sumMean(), new sumMean()
                      });
        u.addValue(new double[] { 1, 2 });
        u.addValue(new double[] { 3, 4 });
        assertEquals(4, u.getMean()[0], 1E-14);
        assertEquals(6, u.getMean()[1], 1E-14);
        u.clear();
        u.addValue(new double[] { 1, 2 });
        u.addValue(new double[] { 3, 4 });
        assertEquals(4, u.getMean()[0], 1E-14);
        assertEquals(6, u.getMean()[1], 1E-14);
        u.clear();
        u.setMeanImpl(new StorelessUnivariateStatistic[] {
                        new Mean(), new Mean()
                      }); 
        u.addValue(new double[] { 1, 2 });
        u.addValue(new double[] { 3, 4 });
        assertEquals(2, u.getMean()[0], 1E-14);
        assertEquals(3, u.getMean()[1], 1E-14);
        assertEquals(2, u.getDimension());
    }

// org.apache.commons.math.stat.descriptive.MultivariateSummaryStatisticsTest::testSetterIllegalState
    public void testSetterIllegalState() throws Exception {
        MultivariateSummaryStatistics u = createMultivariateSummaryStatistics(2, true);
        u.addValue(new double[] { 1, 2 });
        u.addValue(new double[] { 3, 4 });
        try {
            u.setMeanImpl(new StorelessUnivariateStatistic[] {
                            new sumMean(), new sumMean()
                          });
            fail("Expecting IllegalStateException");
        } catch (IllegalStateException ex) {
            
        }
    }

// org.apache.commons.math.stat.descriptive.MultivariateSummaryStatisticsTest::testToString
    public void testToString() throws DimensionMismatchException {
        MultivariateSummaryStatistics stats = createMultivariateSummaryStatistics(2, true);
        stats.addValue(new double[] {1, 3});
        stats.addValue(new double[] {2, 2});
        stats.addValue(new double[] {3, 1});
        Locale d = Locale.getDefault();
        Locale.setDefault(Locale.US);
        final String suffix = System.getProperty("line.separator");
        assertEquals("MultivariateSummaryStatistics:" + suffix+
                     "n: 3" +suffix+
                     "min: 1.0, 1.0" +suffix+
                     "max: 3.0, 3.0" +suffix+
                     "mean: 2.0, 2.0" +suffix+
                     "geometric mean: 1.817..., 1.817..." +suffix+
                     "sum of squares: 14.0, 14.0" +suffix+
                     "sum of logarithms: 1.791..., 1.791..." +suffix+
                     "standard deviation: 1.0, 1.0" +suffix+
                     "covariance: Array2DRowRealMatrix{{1.0,-1.0},{-1.0,1.0}}" +suffix,
                     stats.toString().replaceAll("([0-9]+\\.[0-9][0-9][0-9])[0-9]+", "$1..."));
        Locale.setDefault(d);
    }

// org.apache.commons.math.stat.descriptive.MultivariateSummaryStatisticsTest::testShuffledStatistics
    public void testShuffledStatistics() throws DimensionMismatchException {
        
        
        
        MultivariateSummaryStatistics reference = createMultivariateSummaryStatistics(2, true);
        MultivariateSummaryStatistics shuffled  = createMultivariateSummaryStatistics(2, true);

        StorelessUnivariateStatistic[] tmp = shuffled.getGeoMeanImpl();
        shuffled.setGeoMeanImpl(shuffled.getMeanImpl());
        shuffled.setMeanImpl(shuffled.getMaxImpl());
        shuffled.setMaxImpl(shuffled.getMinImpl());
        shuffled.setMinImpl(shuffled.getSumImpl());
        shuffled.setSumImpl(shuffled.getSumsqImpl());
        shuffled.setSumsqImpl(shuffled.getSumLogImpl());
        shuffled.setSumLogImpl(tmp);

        for (int i = 100; i > 0; --i) {
            reference.addValue(new double[] {i, i});
            shuffled.addValue(new double[] {i, i});
        }

        TestUtils.assertEquals(reference.getMean(),          shuffled.getGeometricMean(), 1.0e-10);
        TestUtils.assertEquals(reference.getMax(),           shuffled.getMean(),          1.0e-10);
        TestUtils.assertEquals(reference.getMin(),           shuffled.getMax(),           1.0e-10);
        TestUtils.assertEquals(reference.getSum(),           shuffled.getMin(),           1.0e-10);
        TestUtils.assertEquals(reference.getSumSq(),         shuffled.getSum(),           1.0e-10);
        TestUtils.assertEquals(reference.getSumLog(),        shuffled.getSumSq(),         1.0e-10);
        TestUtils.assertEquals(reference.getGeometricMean(), shuffled.getSumLog(),        1.0e-10);

    }

// org.apache.commons.math.stat.descriptive.MultivariateSummaryStatisticsTest::testDimension
    public void testDimension() {
        try {
            createMultivariateSummaryStatistics(2, true).addValue(new double[3]);
            fail("Expecting DimensionMismatchException");
        } catch (DimensionMismatchException dme) {
            
        }
    }

// org.apache.commons.math.stat.descriptive.MultivariateSummaryStatisticsTest::testStats
    public void testStats() throws DimensionMismatchException {
        MultivariateSummaryStatistics u = createMultivariateSummaryStatistics(2, true);
        assertEquals(0, u.getN());
        u.addValue(new double[] { 1, 2 });
        u.addValue(new double[] { 2, 3 });
        u.addValue(new double[] { 2, 3 });
        u.addValue(new double[] { 3, 4 });
        assertEquals( 4, u.getN());
        assertEquals( 8, u.getSum()[0], 1.0e-10);
        assertEquals(12, u.getSum()[1], 1.0e-10);
        assertEquals(18, u.getSumSq()[0], 1.0e-10);
        assertEquals(38, u.getSumSq()[1], 1.0e-10);
        assertEquals( 1, u.getMin()[0], 1.0e-10);
        assertEquals( 2, u.getMin()[1], 1.0e-10);
        assertEquals( 3, u.getMax()[0], 1.0e-10);
        assertEquals( 4, u.getMax()[1], 1.0e-10);
        assertEquals(2.4849066497880003102, u.getSumLog()[0], 1.0e-10);
        assertEquals( 4.276666119016055311, u.getSumLog()[1], 1.0e-10);
        assertEquals( 1.8612097182041991979, u.getGeometricMean()[0], 1.0e-10);
        assertEquals( 2.9129506302439405217, u.getGeometricMean()[1], 1.0e-10);
        assertEquals( 2, u.getMean()[0], 1.0e-10);
        assertEquals( 3, u.getMean()[1], 1.0e-10);
        assertEquals(FastMath.sqrt(2.0 / 3.0), u.getStandardDeviation()[0], 1.0e-10);
        assertEquals(FastMath.sqrt(2.0 / 3.0), u.getStandardDeviation()[1], 1.0e-10);
        assertEquals(2.0 / 3.0, u.getCovariance().getEntry(0, 0), 1.0e-10);
        assertEquals(2.0 / 3.0, u.getCovariance().getEntry(0, 1), 1.0e-10);
        assertEquals(2.0 / 3.0, u.getCovariance().getEntry(1, 0), 1.0e-10);
        assertEquals(2.0 / 3.0, u.getCovariance().getEntry(1, 1), 1.0e-10);
        u.clear();
        assertEquals(0, u.getN());
    }

// org.apache.commons.math.stat.descriptive.MultivariateSummaryStatisticsTest::testN0andN1Conditions
    public void testN0andN1Conditions() throws Exception {
        MultivariateSummaryStatistics u = createMultivariateSummaryStatistics(1, true);
        assertTrue(Double.isNaN(u.getMean()[0]));
        assertTrue(Double.isNaN(u.getStandardDeviation()[0]));

        
        u.addValue(new double[] { 1 });
        assertEquals(1.0, u.getMean()[0], 1.0e-10);
        assertEquals(1.0, u.getGeometricMean()[0], 1.0e-10);
        assertEquals(0.0, u.getStandardDeviation()[0], 1.0e-10);

        
        u.addValue(new double[] { 2 });
        assertTrue(u.getStandardDeviation()[0] > 0);

    }

// org.apache.commons.math.stat.descriptive.MultivariateSummaryStatisticsTest::testNaNContracts
    public void testNaNContracts() throws DimensionMismatchException {
        MultivariateSummaryStatistics u = createMultivariateSummaryStatistics(1, true);
        assertTrue(Double.isNaN(u.getMean()[0]));
        assertTrue(Double.isNaN(u.getMin()[0]));
        assertTrue(Double.isNaN(u.getStandardDeviation()[0]));
        assertTrue(Double.isNaN(u.getGeometricMean()[0]));

        u.addValue(new double[] { 1.0 });
        assertFalse(Double.isNaN(u.getMean()[0]));
        assertFalse(Double.isNaN(u.getMin()[0]));
        assertFalse(Double.isNaN(u.getStandardDeviation()[0]));
        assertFalse(Double.isNaN(u.getGeometricMean()[0]));

    }

// org.apache.commons.math.stat.descriptive.MultivariateSummaryStatisticsTest::testSerialization
    public void testSerialization() throws DimensionMismatchException {
        MultivariateSummaryStatistics u = createMultivariateSummaryStatistics(2, true);
        
        TestUtils.checkSerializedEquality(u);
        MultivariateSummaryStatistics s = (MultivariateSummaryStatistics) TestUtils.serializeAndRecover(u);
        assertEquals(u, s);

        
        u.addValue(new double[] { 2d, 1d });
        u.addValue(new double[] { 1d, 1d });
        u.addValue(new double[] { 3d, 1d });
        u.addValue(new double[] { 4d, 1d });
        u.addValue(new double[] { 5d, 1d });

        
        TestUtils.checkSerializedEquality(u);
        s = (MultivariateSummaryStatistics) TestUtils.serializeAndRecover(u);
        assertEquals(u, s);

    }

// org.apache.commons.math.stat.descriptive.MultivariateSummaryStatisticsTest::testEqualsAndHashCode
    public void testEqualsAndHashCode() throws DimensionMismatchException {
        MultivariateSummaryStatistics u = createMultivariateSummaryStatistics(2, true);
        MultivariateSummaryStatistics t = null;
        int emptyHash = u.hashCode();
        assertTrue(u.equals(u));
        assertFalse(u.equals(t));
        assertFalse(u.equals(Double.valueOf(0)));
        t = createMultivariateSummaryStatistics(2, true);
        assertTrue(t.equals(u));
        assertTrue(u.equals(t));
        assertEquals(emptyHash, t.hashCode());

        
        u.addValue(new double[] { 2d, 1d });
        u.addValue(new double[] { 1d, 1d });
        u.addValue(new double[] { 3d, 1d });
        u.addValue(new double[] { 4d, 1d });
        u.addValue(new double[] { 5d, 1d });
        assertFalse(t.equals(u));
        assertFalse(u.equals(t));
        assertTrue(u.hashCode() != t.hashCode());

        
        t.addValue(new double[] { 2d, 1d });
        t.addValue(new double[] { 1d, 1d });
        t.addValue(new double[] { 3d, 1d });
        t.addValue(new double[] { 4d, 1d });
        t.addValue(new double[] { 5d, 1d });
        assertTrue(t.equals(u));
        assertTrue(u.equals(t));
        assertEquals(u.hashCode(), t.hashCode());

        
        u.clear();
        t.clear();
        assertTrue(t.equals(u));
        assertTrue(u.equals(t));
        assertEquals(emptyHash, t.hashCode());
        assertEquals(emptyHash, u.hashCode());
    }

// org.apache.commons.math.stat.descriptive.StatisticalSummaryValuesTest::testSerialization
    public void testSerialization() {
        StatisticalSummaryValues u = new StatisticalSummaryValues(1, 2, 3, 4, 5, 6);
        TestUtils.checkSerializedEquality(u);
        StatisticalSummaryValues t = (StatisticalSummaryValues) TestUtils.serializeAndRecover(u);
        verifyEquality(u, t);
    }

// org.apache.commons.math.stat.descriptive.StatisticalSummaryValuesTest::testEqualsAndHashCode
    public void testEqualsAndHashCode() {
        StatisticalSummaryValues u  = new StatisticalSummaryValues(1, 2, 3, 4, 5, 6);
        StatisticalSummaryValues t = null;
        assertTrue("reflexive", u.equals(u));
        assertFalse("non-null compared to null", u.equals(t));
        assertFalse("wrong type", u.equals(Double.valueOf(0)));
        t = new StatisticalSummaryValues(1, 2, 3, 4, 5, 6);
        assertTrue("instances with same data should be equal", t.equals(u));
        assertEquals("hash code", u.hashCode(), t.hashCode());

        u = new StatisticalSummaryValues(Double.NaN, 2, 3, 4, 5, 6);
        t = new StatisticalSummaryValues(1, Double.NaN, 3, 4, 5, 6);
        assertFalse("instances based on different data should be different",
                (u.equals(t) ||t.equals(u)));
    }

// org.apache.commons.math.stat.descriptive.StatisticalSummaryValuesTest::testToString
    public void testToString() {
        StatisticalSummaryValues u  = new StatisticalSummaryValues(4.5, 16, 10, 5, 4, 45);
        Locale d = Locale.getDefault();
        Locale.setDefault(Locale.US);
        assertEquals("StatisticalSummaryValues:\n" +
                     "n: 10\n" +
                     "min: 4.0\n" +
                     "max: 5.0\n" +
                     "mean: 4.5\n" +
                     "std dev: 4.0\n" +
                     "variance: 16.0\n" +
                     "sum: 45.0\n",  u.toString());
        Locale.setDefault(d);
    }

// org.apache.commons.math.stat.descriptive.SummaryStatisticsTest::testStats
    public void testStats() {
        SummaryStatistics u = createSummaryStatistics();
        assertEquals("total count",0,u.getN(),tolerance);
        u.addValue(one);
        u.addValue(twoF);
        u.addValue(twoL);
        u.addValue(three);
        assertEquals("N",n,u.getN(),tolerance);
        assertEquals("sum",sum,u.getSum(),tolerance);
        assertEquals("sumsq",sumSq,u.getSumsq(),tolerance);
        assertEquals("var",var,u.getVariance(),tolerance);
        assertEquals("std",std,u.getStandardDeviation(),tolerance);
        assertEquals("mean",mean,u.getMean(),tolerance);
        assertEquals("min",min,u.getMin(),tolerance);
        assertEquals("max",max,u.getMax(),tolerance);
        u.clear();
        assertEquals("total count",0,u.getN(),tolerance);
    }

// org.apache.commons.math.stat.descriptive.SummaryStatisticsTest::testN0andN1Conditions
    public void testN0andN1Conditions() throws Exception {
        SummaryStatistics u = createSummaryStatistics();
        assertTrue("Mean of n = 0 set should be NaN",
                Double.isNaN( u.getMean() ) );
        assertTrue("Standard Deviation of n = 0 set should be NaN",
                Double.isNaN( u.getStandardDeviation() ) );
        assertTrue("Variance of n = 0 set should be NaN",
                Double.isNaN(u.getVariance() ) );

        
        u.addValue(one);
        assertTrue("mean should be one (n = 1)",
                u.getMean() == one);
        assertTrue("geometric should be one (n = 1) instead it is " + u.getGeometricMean(),
                u.getGeometricMean() == one);
        assertTrue("Std should be zero (n = 1)",
                u.getStandardDeviation() == 0.0);
        assertTrue("variance should be zero (n = 1)",
                u.getVariance() == 0.0);

        
        u.addValue(twoF);
        assertTrue("Std should not be zero (n = 2)",
                u.getStandardDeviation() != 0.0);
        assertTrue("variance should not be zero (n = 2)",
                u.getVariance() != 0.0);

    }

// org.apache.commons.math.stat.descriptive.SummaryStatisticsTest::testProductAndGeometricMean
    public void testProductAndGeometricMean() throws Exception {
        SummaryStatistics u = createSummaryStatistics();
        u.addValue( 1.0 );
        u.addValue( 2.0 );
        u.addValue( 3.0 );
        u.addValue( 4.0 );

        assertEquals( "Geometric mean not expected", 2.213364,
                u.getGeometricMean(), 0.00001 );
    }

// org.apache.commons.math.stat.descriptive.SummaryStatisticsTest::testNaNContracts
    public void testNaNContracts() {
        SummaryStatistics u = createSummaryStatistics();
        assertTrue("mean not NaN",Double.isNaN(u.getMean()));
        assertTrue("min not NaN",Double.isNaN(u.getMin()));
        assertTrue("std dev not NaN",Double.isNaN(u.getStandardDeviation()));
        assertTrue("var not NaN",Double.isNaN(u.getVariance()));
        assertTrue("geom mean not NaN",Double.isNaN(u.getGeometricMean()));

        u.addValue(1.0);

        assertEquals( "mean not expected", 1.0,
                u.getMean(), Double.MIN_VALUE);
        assertEquals( "variance not expected", 0.0,
                u.getVariance(), Double.MIN_VALUE);
        assertEquals( "geometric mean not expected", 1.0,
                u.getGeometricMean(), Double.MIN_VALUE);

        u.addValue(-1.0);

        assertTrue("geom mean not NaN",Double.isNaN(u.getGeometricMean()));

        u.addValue(0.0);

        assertTrue("geom mean not NaN",Double.isNaN(u.getGeometricMean()));

        
    }

// org.apache.commons.math.stat.descriptive.SummaryStatisticsTest::testGetSummary
    public void testGetSummary() {
        SummaryStatistics u = createSummaryStatistics();
        StatisticalSummary summary = u.getSummary();
        verifySummary(u, summary);
        u.addValue(1d);
        summary = u.getSummary();
        verifySummary(u, summary);
        u.addValue(2d);
        summary = u.getSummary();
        verifySummary(u, summary);
        u.addValue(2d);
        summary = u.getSummary();
        verifySummary(u, summary);
    }

// org.apache.commons.math.stat.descriptive.SummaryStatisticsTest::testSerialization
    public void testSerialization() {
        SummaryStatistics u = createSummaryStatistics();
        
        TestUtils.checkSerializedEquality(u);
        SummaryStatistics s = (SummaryStatistics) TestUtils.serializeAndRecover(u);
        StatisticalSummary summary = s.getSummary();
        verifySummary(u, summary);

        
        u.addValue(2d);
        u.addValue(1d);
        u.addValue(3d);
        u.addValue(4d);
        u.addValue(5d);

        
        TestUtils.checkSerializedEquality(u);
        s = (SummaryStatistics) TestUtils.serializeAndRecover(u);
        summary = s.getSummary();
        verifySummary(u, summary);

    }

// org.apache.commons.math.stat.descriptive.SummaryStatisticsTest::testEqualsAndHashCode
    public void testEqualsAndHashCode() {
        SummaryStatistics u = createSummaryStatistics();
        SummaryStatistics t = null;
        int emptyHash = u.hashCode();
        assertTrue("reflexive", u.equals(u));
        assertFalse("non-null compared to null", u.equals(t));
        assertFalse("wrong type", u.equals(Double.valueOf(0)));
        t = createSummaryStatistics();
        assertTrue("empty instances should be equal", t.equals(u));
        assertTrue("empty instances should be equal", u.equals(t));
        assertEquals("empty hash code", emptyHash, t.hashCode());

        
        u.addValue(2d);
        u.addValue(1d);
        u.addValue(3d);
        u.addValue(4d);
        assertFalse("different n's should make instances not equal", t.equals(u));
        assertFalse("different n's should make instances not equal", u.equals(t));
        assertTrue("different n's should make hashcodes different",
                u.hashCode() != t.hashCode());

        
        t.addValue(2d);
        t.addValue(1d);
        t.addValue(3d);
        t.addValue(4d);
        assertTrue("summaries based on same data should be equal", t.equals(u));
        assertTrue("summaries based on same data should be equal", u.equals(t));
        assertEquals("summaries based on same data should have same hashcodes",
                u.hashCode(), t.hashCode());

        
        u.clear();
        t.clear();
        assertTrue("empty instances should be equal", t.equals(u));
        assertTrue("empty instances should be equal", u.equals(t));
        assertEquals("empty hash code", emptyHash, t.hashCode());
        assertEquals("empty hash code", emptyHash, u.hashCode());
    }

// org.apache.commons.math.stat.descriptive.SummaryStatisticsTest::testCopy
    public void testCopy() throws Exception {
        SummaryStatistics u = createSummaryStatistics();
        u.addValue(2d);
        u.addValue(1d);
        u.addValue(3d);
        u.addValue(4d);
        SummaryStatistics v = new SummaryStatistics(u);
        assertEquals(u, v);
        assertEquals(v, u);
        assertTrue(v.geoMean == v.getGeoMeanImpl());
        assertTrue(v.mean == v.getMeanImpl());
        assertTrue(v.min == v.getMinImpl());
        assertTrue(v.max == v.getMaxImpl());
        assertTrue(v.sum == v.getSumImpl());
        assertTrue(v.sumsq == v.getSumsqImpl());
        assertTrue(v.sumLog == v.getSumLogImpl());
        assertTrue(v.variance == v.getVarianceImpl());

        
        u.addValue(7d);
        u.addValue(9d);
        u.addValue(11d);
        u.addValue(23d);
        v.addValue(7d);
        v.addValue(9d);
        v.addValue(11d);
        v.addValue(23d);
        assertEquals(u, v);
        assertEquals(v, u);

        
        u.clear();
        u.setSumImpl(new Sum());
        SummaryStatistics.copy(u,v);
        assertEquals(u.sum, v.sum);
        assertEquals(u.getSumImpl(), v.getSumImpl());

    }

// org.apache.commons.math.stat.descriptive.SummaryStatisticsTest::testSetterInjection
    public void testSetterInjection() throws Exception {
        SummaryStatistics u = createSummaryStatistics();
        u.setMeanImpl(new Sum());
        u.setSumLogImpl(new Sum());
        u.addValue(1);
        u.addValue(3);
        assertEquals(4, u.getMean(), 1E-14);
        assertEquals(4, u.getSumOfLogs(), 1E-14);
        assertEquals(FastMath.exp(2), u.getGeometricMean(), 1E-14);
        u.clear();
        u.addValue(1);
        u.addValue(2);
        assertEquals(3, u.getMean(), 1E-14);
        u.clear();
        u.setMeanImpl(new Mean()); 
    }

// org.apache.commons.math.stat.descriptive.SummaryStatisticsTest::testSetterIllegalState
    public void testSetterIllegalState() throws Exception {
        SummaryStatistics u = createSummaryStatistics();
        u.addValue(1);
        u.addValue(3);
        try {
            u.setMeanImpl(new Sum());
            fail("Expecting IllegalStateException");
        } catch (IllegalStateException ex) {
            
        }
    }

// org.apache.commons.math.stat.descriptive.moment.GeometricMeanTest::testSpecialValues
    public void testSpecialValues() {
        GeometricMean mean = new GeometricMean();
        
        assertTrue(Double.isNaN(mean.getResult()));

        
        mean.increment(1d);
        assertFalse(Double.isNaN(mean.getResult()));

        
        mean.increment(0d);
        assertEquals(0d, mean.getResult(), 0);

        
        mean.increment(Double.POSITIVE_INFINITY);
        assertTrue(Double.isNaN(mean.getResult()));

        
        mean.clear();
        assertTrue(Double.isNaN(mean.getResult()));

        
        mean.increment(Double.POSITIVE_INFINITY);
        assertEquals(Double.POSITIVE_INFINITY, mean.getResult(), 0);

        
        mean.increment(-2d);
        assertTrue(Double.isNaN(mean.getResult()));
    }

// org.apache.commons.math.stat.descriptive.moment.KurtosisTest::testNaN
    public void testNaN() {
        Kurtosis kurt = new Kurtosis();
        assertTrue(Double.isNaN(kurt.getResult()));
        kurt.increment(1d);
        assertTrue(Double.isNaN(kurt.getResult()));
        kurt.increment(1d);
        assertTrue(Double.isNaN(kurt.getResult()));
        kurt.increment(1d);
        assertTrue(Double.isNaN(kurt.getResult()));
        kurt.increment(1d);
        assertFalse(Double.isNaN(kurt.getResult()));
    }

// org.apache.commons.math.stat.descriptive.moment.MeanTest::testSmallSamples
    public void testSmallSamples() {
        Mean mean = new Mean();
        assertTrue(Double.isNaN(mean.getResult()));
        mean.increment(1d);
        assertEquals(1d, mean.getResult(), 0);
    }

// org.apache.commons.math.stat.descriptive.moment.MeanTest::testWeightedMean
    public void testWeightedMean() {
        Mean mean = new Mean();
        assertEquals(expectedWeightedValue(), mean.evaluate(testArray, testWeightsArray, 0, testArray.length), getTolerance());
        assertEquals(expectedValue(), mean.evaluate(testArray, identicalWeightsArray, 0, testArray.length), getTolerance());
    }

// org.apache.commons.math.stat.descriptive.moment.SkewnessTest::testNaN
    public void testNaN() {
        Skewness skew = new Skewness();
        assertTrue(Double.isNaN(skew.getResult()));
        skew.increment(1d);
        assertTrue(Double.isNaN(skew.getResult()));
        skew.increment(1d);
        assertTrue(Double.isNaN(skew.getResult()));
        skew.increment(1d);
        assertFalse(Double.isNaN(skew.getResult()));
    }

// org.apache.commons.math.stat.descriptive.moment.StandardDeviationTest::testNaN
    public void testNaN() {
        StandardDeviation std = new StandardDeviation();
        assertTrue(Double.isNaN(std.getResult()));
        std.increment(1d);
        assertEquals(0d, std.getResult(), 0);
    }

// org.apache.commons.math.stat.descriptive.moment.StandardDeviationTest::testPopulation
    public void testPopulation() {
        double[] values = {-1.0d, 3.1d, 4.0d, -2.1d, 22d, 11.7d, 3d, 14d};
        double sigma = populationStandardDeviation(values);
        SecondMoment m = new SecondMoment();
        m.evaluate(values);  
        StandardDeviation s1 = new StandardDeviation();
        s1.setBiasCorrected(false);
        assertEquals(sigma, s1.evaluate(values), 1E-14);
        s1.incrementAll(values);
        assertEquals(sigma, s1.getResult(), 1E-14);
        s1 = new StandardDeviation(false, m);
        assertEquals(sigma, s1.getResult(), 1E-14);
        s1 = new StandardDeviation(false);
        assertEquals(sigma, s1.evaluate(values), 1E-14);
        s1.incrementAll(values);
        assertEquals(sigma, s1.getResult(), 1E-14);
    }

// org.apache.commons.math.stat.descriptive.moment.VarianceTest::testNaN
    public void testNaN() {
        StandardDeviation std = new StandardDeviation();
        assertTrue(Double.isNaN(std.getResult()));
        std.increment(1d);
        assertEquals(0d, std.getResult(), 0);
    }

// org.apache.commons.math.stat.descriptive.moment.VarianceTest::testPopulation
    public void testPopulation() {
        double[] values = {-1.0d, 3.1d, 4.0d, -2.1d, 22d, 11.7d, 3d, 14d};
        SecondMoment m = new SecondMoment();
        m.evaluate(values);  
        Variance v1 = new Variance();
        v1.setBiasCorrected(false);
        assertEquals(populationVariance(values), v1.evaluate(values), 1E-14);
        v1.incrementAll(values);
        assertEquals(populationVariance(values), v1.getResult(), 1E-14);
        v1 = new Variance(false, m);
        assertEquals(populationVariance(values), v1.getResult(), 1E-14);
        v1 = new Variance(false);
        assertEquals(populationVariance(values), v1.evaluate(values), 1E-14);
        v1.incrementAll(values);
        assertEquals(populationVariance(values), v1.getResult(), 1E-14);
    }

// org.apache.commons.math.stat.descriptive.moment.VarianceTest::testWeightedVariance
    public void testWeightedVariance() {
        Variance variance = new Variance();
        assertEquals(expectedWeightedValue(),
                variance.evaluate(testArray, testWeightsArray, 0, testArray.length), getTolerance());

        
        assertEquals(expectedValue(),
                variance.evaluate(testArray, unitWeightsArray, 0, testArray.length), getTolerance());

        
        
        assertEquals(expectedValue(),
                variance.evaluate(testArray, MathUtils.normalizeArray(identicalWeightsArray, testArray.length),
                        0, testArray.length), getTolerance());

    }

// org.apache.commons.math.stat.descriptive.moment.VectorialMeanTest::testMismatch
    public void testMismatch() {
        try {
            new VectorialMean(8).increment(new double[5]);
            fail("an exception should have been thrown");
        } catch (DimensionMismatchException dme) {
            assertEquals(5, dme.getDimension1());
            assertEquals(8, dme.getDimension2());
        }
    }

// org.apache.commons.math.stat.descriptive.moment.VectorialMeanTest::testSimplistic
    public void testSimplistic() throws DimensionMismatchException {
        VectorialMean stat = new VectorialMean(2);
        stat.increment(new double[] {-1.0,  1.0});
        stat.increment(new double[] { 1.0, -1.0});
        double[] mean = stat.getResult();
        assertEquals(0.0, mean[0], 1.0e-12);
        assertEquals(0.0, mean[1], 1.0e-12);
    }

// org.apache.commons.math.stat.descriptive.moment.VectorialMeanTest::testBasicStats
    public void testBasicStats() throws DimensionMismatchException {

        VectorialMean stat = new VectorialMean(points[0].length);
        for (int i = 0; i < points.length; ++i) {
            stat.increment(points[i]);
        }

        assertEquals(points.length, stat.getN());

        double[] mean = stat.getResult();
        double[]   refMean = new double[] { 1.78, 1.62,  3.12};

        for (int i = 0; i < mean.length; ++i) {
            assertEquals(refMean[i], mean[i], 1.0e-12);
        }

    }

// org.apache.commons.math.stat.descriptive.moment.VectorialMeanTest::testSerial
    public void testSerial() throws DimensionMismatchException {
        VectorialMean stat = new VectorialMean(points[0].length);
        for (int i = 0; i < points.length; ++i) {
            stat.increment(points[i]);
        }
        assertEquals(stat, TestUtils.serializeAndRecover(stat));
    }

// org.apache.commons.math.stat.descriptive.rank.MaxTest::testSpecialValues
    public void testSpecialValues() {
        double[] testArray = {0d, Double.NaN, Double.NEGATIVE_INFINITY,
                Double.POSITIVE_INFINITY};
        Max max = new Max();
        assertTrue(Double.isNaN(max.getResult()));
        max.increment(testArray[0]);
        assertEquals(0d, max.getResult(), 0);
        max.increment(testArray[1]);
        assertEquals(0d, max.getResult(), 0);
        max.increment(testArray[2]);
        assertEquals(0d, max.getResult(), 0);
        max.increment(testArray[3]);
        assertEquals(Double.POSITIVE_INFINITY, max.getResult(), 0);
        assertEquals(Double.POSITIVE_INFINITY, max.evaluate(testArray), 0);
    }

// org.apache.commons.math.stat.descriptive.rank.MaxTest::testNaNs
    public void testNaNs() {
        Max max = new Max();
        double nan = Double.NaN;
        assertEquals(3d, max.evaluate(new double[]{nan, 2d, 3d}), 0);
        assertEquals(3d, max.evaluate(new double[]{1d, nan, 3d}), 0);
        assertEquals(2d, max.evaluate(new double[]{1d, 2d, nan}), 0);
        assertTrue(Double.isNaN(max.evaluate(new double[]{nan, nan, nan})));
    }

// org.apache.commons.math.stat.descriptive.rank.MinTest::testSpecialValues
    public void testSpecialValues() {
        double[] testArray = {0d, Double.NaN, Double.POSITIVE_INFINITY,
                Double.NEGATIVE_INFINITY};
        Min min = new Min();
        assertTrue(Double.isNaN(min.getResult()));
        min.increment(testArray[0]);
        assertEquals(0d, min.getResult(), 0);
        min.increment(testArray[1]);
        assertEquals(0d, min.getResult(), 0);
        min.increment(testArray[2]);
        assertEquals(0d, min.getResult(), 0);
        min.increment(testArray[3]);
        assertEquals(Double.NEGATIVE_INFINITY, min.getResult(), 0);
        assertEquals(Double.NEGATIVE_INFINITY, min.evaluate(testArray), 0);
    }

// org.apache.commons.math.stat.descriptive.rank.MinTest::testNaNs
    public void testNaNs() {
        Min min = new Min();
        double nan = Double.NaN;
        assertEquals(2d, min.evaluate(new double[]{nan, 2d, 3d}), 0);
        assertEquals(1d, min.evaluate(new double[]{1d, nan, 3d}), 0);
        assertEquals(1d, min.evaluate(new double[]{1d, 2d, nan}), 0);
        assertTrue(Double.isNaN(min.evaluate(new double[]{nan, nan, nan})));
    }

// org.apache.commons.math.stat.descriptive.rank.PercentileTest::testHighPercentile
    public void testHighPercentile(){
        double[] d = new double[]{1, 2, 3};
        Percentile p = new Percentile(75);
        assertEquals(3.0, p.evaluate(d), 1.0e-5);
    }

// org.apache.commons.math.stat.descriptive.rank.PercentileTest::testPercentile
    public void testPercentile() {
        double[] d = new double[] {1, 3, 2, 4};
        Percentile p = new Percentile(30);
        assertEquals(1.5, p.evaluate(d), 1.0e-5);
        p.setQuantile(25);
        assertEquals(1.25, p.evaluate(d), 1.0e-5);
        p.setQuantile(75);
        assertEquals(3.75, p.evaluate(d), 1.0e-5);
        p.setQuantile(50);
        assertEquals(2.5, p.evaluate(d), 1.0e-5);

        
        try {
            p.evaluate(d, 0, d.length, -1.0);
            fail();
        } catch (MathIllegalArgumentException ex) {
            
        }
        try {
            p.evaluate(d, 0, d.length, 101.0);
            fail();
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.stat.descriptive.rank.PercentileTest::testNISTExample
    public void testNISTExample() {
        double[] d = new double[] {95.1772, 95.1567, 95.1937, 95.1959,
                95.1442, 95.0610,  95.1591, 95.1195, 95.1772, 95.0925, 95.1990, 95.1682
        };
        Percentile p = new Percentile(90);
        assertEquals(95.1981, p.evaluate(d), 1.0e-4);
        assertEquals(95.1990, p.evaluate(d,0,d.length, 100d), 0);
    }

// org.apache.commons.math.stat.descriptive.rank.PercentileTest::test5
    public void test5() {
        Percentile percentile = new Percentile(5);
        assertEquals(this.percentile5, percentile.evaluate(testArray), getTolerance());
    }

// org.apache.commons.math.stat.descriptive.rank.PercentileTest::testNullEmpty
    public void testNullEmpty() {
        Percentile percentile = new Percentile(50);
        double[] nullArray = null;
        double[] emptyArray = new double[] {};
        try {
            percentile.evaluate(nullArray);
            fail("Expecting MathIllegalArgumentException for null array");
        } catch (MathIllegalArgumentException ex) {
            
        }
        assertTrue(Double.isNaN(percentile.evaluate(emptyArray)));
    }

// org.apache.commons.math.stat.descriptive.rank.PercentileTest::testSingleton
    public void testSingleton() {
        Percentile percentile = new Percentile(50);
        double[] singletonArray = new double[] {1d};
        assertEquals(1d, percentile.evaluate(singletonArray), 0);
        assertEquals(1d, percentile.evaluate(singletonArray, 0, 1), 0);
        assertEquals(1d, percentile.evaluate(singletonArray, 0, 1, 5), 0);
        assertEquals(1d, percentile.evaluate(singletonArray, 0, 1, 100), 0);
        assertTrue(Double.isNaN(percentile.evaluate(singletonArray, 0, 0)));
    }

// org.apache.commons.math.stat.descriptive.rank.PercentileTest::testSpecialValues
    public void testSpecialValues() {
        Percentile percentile = new Percentile(50);
        double[] specialValues = new double[] {0d, 1d, 2d, 3d, 4d,  Double.NaN};
        assertEquals(2.5d, percentile.evaluate(specialValues), 0);
        specialValues =  new double[] {Double.NEGATIVE_INFINITY, 1d, 2d, 3d,
                Double.NaN, Double.POSITIVE_INFINITY};
        assertEquals(2.5d, percentile.evaluate(specialValues), 0);
        specialValues = new double[] {1d, 1d, Double.POSITIVE_INFINITY,
                Double.POSITIVE_INFINITY};
        assertTrue(Double.isInfinite(percentile.evaluate(specialValues)));
        specialValues = new double[] {1d, 1d, Double.NaN,
                Double.NaN};
        assertTrue(Double.isNaN(percentile.evaluate(specialValues)));
        specialValues = new double[] {1d, 1d, Double.NEGATIVE_INFINITY,
                Double.NEGATIVE_INFINITY};
        
        assertTrue(Double.isNaN(percentile.evaluate(specialValues)));
    }

// org.apache.commons.math.stat.descriptive.rank.PercentileTest::testSetQuantile
    public void testSetQuantile() {
        Percentile percentile = new Percentile(10);
        percentile.setQuantile(100); 
        assertEquals(100, percentile.getQuantile(), 0);
        try {
            percentile.setQuantile(0);
            fail("Expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
        try {
            new Percentile(0);
            fail("Expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.stat.descriptive.summary.ProductTest::testSpecialValues
    public void testSpecialValues() {
        Product product = new Product();
        assertEquals(1, product.getResult(), 0);
        product.increment(1);
        assertEquals(1, product.getResult(), 0);
        product.increment(Double.POSITIVE_INFINITY);
        assertEquals(Double.POSITIVE_INFINITY, product.getResult(), 0);
        product.increment(Double.NEGATIVE_INFINITY);
        assertEquals(Double.NEGATIVE_INFINITY, product.getResult(), 0);
        product.increment(Double.NaN);
        assertTrue(Double.isNaN(product.getResult()));
        product.increment(1);
        assertTrue(Double.isNaN(product.getResult()));
    }

// org.apache.commons.math.stat.descriptive.summary.ProductTest::testWeightedProduct
    public void testWeightedProduct() {
        Product product = new Product();
        assertEquals(expectedWeightedValue(), product.evaluate(testArray, testWeightsArray, 0, testArray.length),getTolerance());
        assertEquals(expectedValue(), product.evaluate(testArray, unitWeightsArray, 0, testArray.length), getTolerance());
    }

// org.apache.commons.math.stat.descriptive.summary.SumLogTest::testSpecialValues
    public void testSpecialValues() {
        SumOfLogs sum = new SumOfLogs();
        
        assertEquals(0, sum.getResult(), 0);

        
        sum.increment(1d);
        assertFalse(Double.isNaN(sum.getResult()));

        
        sum.increment(0d);
        assertEquals(Double.NEGATIVE_INFINITY, sum.getResult(), 0);

        
        sum.increment(Double.POSITIVE_INFINITY);
        assertTrue(Double.isNaN(sum.getResult()));

        
        sum.clear();
        assertEquals(0, sum.getResult(), 0);

        
        sum.increment(Double.POSITIVE_INFINITY);
        assertEquals(Double.POSITIVE_INFINITY, sum.getResult(), 0);

        
        sum.increment(-2d);
        assertTrue(Double.isNaN(sum.getResult()));
    }

// org.apache.commons.math.stat.descriptive.summary.SumSqTest::testSpecialValues
    public void testSpecialValues() {
        SumOfSquares sumSq = new SumOfSquares();
        assertEquals(0, sumSq.getResult(), 0);
        sumSq.increment(2d);
        assertEquals(4d, sumSq.getResult(), 0);
        sumSq.increment(Double.POSITIVE_INFINITY);
        assertEquals(Double.POSITIVE_INFINITY, sumSq.getResult(), 0);
        sumSq.increment(Double.NEGATIVE_INFINITY);
        assertEquals(Double.POSITIVE_INFINITY, sumSq.getResult(), 0);
        sumSq.increment(Double.NaN);
        assertTrue(Double.isNaN(sumSq.getResult()));
        sumSq.increment(1);
        assertTrue(Double.isNaN(sumSq.getResult()));
    }

// org.apache.commons.math.stat.descriptive.summary.SumTest::testSpecialValues
    public void testSpecialValues() {
        Sum sum = new Sum();
        assertEquals(0, sum.getResult(), 0);
        sum.increment(1);
        assertEquals(1, sum.getResult(), 0);
        sum.increment(Double.POSITIVE_INFINITY);
        assertEquals(Double.POSITIVE_INFINITY, sum.getResult(), 0);
        sum.increment(Double.NEGATIVE_INFINITY);
        assertTrue(Double.isNaN(sum.getResult()));
        sum.increment(1);
        assertTrue(Double.isNaN(sum.getResult()));
    }

// org.apache.commons.math.stat.descriptive.summary.SumTest::testWeightedSum
    public void testWeightedSum() {
        Sum sum = new Sum();
        assertEquals(expectedWeightedValue(), sum.evaluate(testArray, testWeightsArray, 0, testArray.length), getTolerance());
        assertEquals(expectedValue(), sum.evaluate(testArray, unitWeightsArray, 0, testArray.length), getTolerance());
    }

// org.apache.commons.math.stat.descriptive.summary.SumTest::testWeightedConsistency
    public void testWeightedConsistency() {}

// org.apache.commons.math.stat.inference.ChiSquareTestTest::testChiSquare
    public void testChiSquare() throws Exception {

        
        
        
        

        long[] observed = {10, 9, 11};
        double[] expected = {10, 10, 10};
        assertEquals("chi-square statistic", 0.2,  testStatistic.chiSquare(expected, observed), 10E-12);
        assertEquals("chi-square p-value", 0.904837418036, testStatistic.chiSquareTest(expected, observed), 1E-10);

        long[] observed1 = { 500, 623, 72, 70, 31 };
        double[] expected1 = { 485, 541, 82, 61, 37 };
        assertEquals( "chi-square test statistic", 9.023307936427388, testStatistic.chiSquare(expected1, observed1), 1E-10);
        assertEquals("chi-square p-value", 0.06051952647453607, testStatistic.chiSquareTest(expected1, observed1), 1E-9);
        assertTrue("chi-square test reject", testStatistic.chiSquareTest(expected1, observed1, 0.08));
        assertTrue("chi-square test accept", !testStatistic.chiSquareTest(expected1, observed1, 0.05));

        try {
            testStatistic.chiSquareTest(expected1, observed1, 95);
            fail("alpha out of range, MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }

        long[] tooShortObs = { 0 };
        double[] tooShortEx = { 1 };
        try {
            testStatistic.chiSquare(tooShortEx, tooShortObs);
            fail("arguments too short, MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }

        
        long[] unMatchedObs = { 0, 1, 2, 3 };
        double[] unMatchedEx = { 1, 1, 2 };
        try {
            testStatistic.chiSquare(unMatchedEx, unMatchedObs);
            fail("arrays have different lengths, MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }

        
        expected[0] = 0;
        try {
            testStatistic.chiSquareTest(expected, observed, .01);
            fail("bad expected count, MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }

        
        expected[0] = 1;
        observed[0] = -1;
        try {
            testStatistic.chiSquareTest(expected, observed, .01);
            fail("bad expected count, MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }

    }

// org.apache.commons.math.stat.inference.ChiSquareTestTest::testChiSquareIndependence
    public void testChiSquareIndependence() throws Exception {

        

        long[][] counts = { {40, 22, 43}, {91, 21, 28}, {60, 10, 22}};
        assertEquals( "chi-square test statistic", 22.709027688, testStatistic.chiSquare(counts), 1E-9);
        assertEquals("chi-square p-value", 0.000144751460134, testStatistic.chiSquareTest(counts), 1E-9);
        assertTrue("chi-square test reject", testStatistic.chiSquareTest(counts, 0.0002));
        assertTrue("chi-square test accept", !testStatistic.chiSquareTest(counts, 0.0001));

        long[][] counts2 = {{10, 15}, {30, 40}, {60, 90} };
        assertEquals( "chi-square test statistic", 0.168965517241, testStatistic.chiSquare(counts2), 1E-9);
        assertEquals("chi-square p-value",0.918987499852, testStatistic.chiSquareTest(counts2), 1E-9);
        assertTrue("chi-square test accept", !testStatistic.chiSquareTest(counts2, 0.1));

        
        long[][] counts3 = { {40, 22, 43}, {91, 21, 28}, {60, 10}};
        try {
            testStatistic.chiSquare(counts3);
            fail("Expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }

        
        long[][] counts4 = {{40, 22, 43}};
        try {
            testStatistic.chiSquare(counts4);
            fail("Expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
        long[][] counts5 = {{40}, {40}, {30}, {10}};
        try {
            testStatistic.chiSquare(counts5);
            fail("Expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }

        
        long[][] counts6 = {{10, -2}, {30, 40}, {60, 90} };
        try {
            testStatistic.chiSquare(counts6);
            fail("Expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }

        
        try {
            testStatistic.chiSquareTest(counts, 0);
            fail("Expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.stat.inference.ChiSquareTestTest::testChiSquareLargeTestStatistic
    public void testChiSquareLargeTestStatistic() throws Exception {
        double[] exp = new double[] {
            3389119.5, 649136.6, 285745.4, 25357364.76, 11291189.78, 543628.0,
            232921.0, 437665.75
        };

        long[] obs = new long[] {
            2372383, 584222, 257170, 17750155, 7903832, 489265, 209628, 393899
        };
        org.apache.commons.math.stat.inference.ChiSquareTestImpl csti =
            new org.apache.commons.math.stat.inference.ChiSquareTestImpl();
        double cst = csti.chiSquareTest(exp, obs);
        assertEquals("chi-square p-value", 0.0, cst, 1E-3);
        assertEquals( "chi-square test statistic",
                114875.90421929007, testStatistic.chiSquare(exp, obs), 1E-9);
    }

// org.apache.commons.math.stat.inference.ChiSquareTestTest::testChiSquareZeroCount
    public void testChiSquareZeroCount() throws Exception {
        
        long[][] counts = { {40, 0, 4}, {91, 1, 2}, {60, 2, 0}};
        assertEquals( "chi-square test statistic", 9.67444662263,
                testStatistic.chiSquare(counts), 1E-9);
        assertEquals("chi-square p-value", 0.0462835770603,
                testStatistic.chiSquareTest(counts), 1E-9);
    }

// org.apache.commons.math.stat.inference.ChiSquareTestTest::testChiSquareDataSetsComparisonEqualCounts
    public void testChiSquareDataSetsComparisonEqualCounts()
    throws Exception {
        long[] observed1 = {10, 12, 12, 10};
        long[] observed2 = {5, 15, 14, 10};
        assertEquals("chi-square p value", 0.541096,
                testStatistic.chiSquareTestDataSetsComparison(
                observed1, observed2), 1E-6);
        assertEquals("chi-square test statistic", 2.153846,
                testStatistic.chiSquareDataSetsComparison(
                observed1, observed2), 1E-6);
        assertFalse("chi-square test result",
                testStatistic.chiSquareTestDataSetsComparison(
                observed1, observed2, 0.4));
    }

// org.apache.commons.math.stat.inference.ChiSquareTestTest::testChiSquareDataSetsComparisonUnEqualCounts
    public void testChiSquareDataSetsComparisonUnEqualCounts()
    throws Exception {
        long[] observed1 = {10, 12, 12, 10, 15};
        long[] observed2 = {15, 10, 10, 15, 5};
        assertEquals("chi-square p value", 0.124115,
                testStatistic.chiSquareTestDataSetsComparison(
                observed1, observed2), 1E-6);
        assertEquals("chi-square test statistic", 7.232189,
                testStatistic.chiSquareDataSetsComparison(
                observed1, observed2), 1E-6);
        assertTrue("chi-square test result",
                testStatistic.chiSquareTestDataSetsComparison(
                observed1, observed2, 0.13));
        assertFalse("chi-square test result",
                testStatistic.chiSquareTestDataSetsComparison(
                observed1, observed2, 0.12));
    }

// org.apache.commons.math.stat.inference.ChiSquareTestTest::testChiSquareDataSetsComparisonBadCounts
    public void testChiSquareDataSetsComparisonBadCounts()
    throws Exception {
        long[] observed1 = {10, -1, 12, 10, 15};
        long[] observed2 = {15, 10, 10, 15, 5};
        try {
            testStatistic.chiSquareTestDataSetsComparison(
                    observed1, observed2);
            fail("Expecting MathIllegalArgumentException - negative count");
        } catch (MathIllegalArgumentException ex) {
            
        }
        long[] observed3 = {10, 0, 12, 10, 15};
        long[] observed4 = {15, 0, 10, 15, 5};
        try {
            testStatistic.chiSquareTestDataSetsComparison(
                    observed3, observed4);
            fail("Expecting MathIllegalArgumentException - double 0's");
        } catch (MathIllegalArgumentException ex) {
            
        }
        long[] observed5 = {10, 10, 12, 10, 15};
        long[] observed6 = {0, 0, 0, 0, 0};
        try {
            testStatistic.chiSquareTestDataSetsComparison(
                    observed5, observed6);
            fail("Expecting MathIllegalArgumentException - vanishing counts");
        } catch (MathIllegalArgumentException ex) {
            
        }
    }
