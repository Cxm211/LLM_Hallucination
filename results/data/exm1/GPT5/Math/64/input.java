// buggy code
    protected VectorialPointValuePair doOptimize()
        throws FunctionEvaluationException, OptimizationException, IllegalArgumentException {

        // arrays shared with the other private methods
        solvedCols  = Math.min(rows, cols);
        diagR       = new double[cols];
        jacNorm     = new double[cols];
        beta        = new double[cols];
        permutation = new int[cols];
        lmDir       = new double[cols];

        // local point
        double   delta   = 0;
        double   xNorm   = 0;
        double[] diag    = new double[cols];
        double[] oldX    = new double[cols];
        double[] oldRes  = new double[rows];
        double[] work1   = new double[cols];
        double[] work2   = new double[cols];
        double[] work3   = new double[cols];

        // evaluate the function at the starting point and calculate its norm
        updateResidualsAndCost();

        // outer loop
        lmPar = 0;
        boolean firstIteration = true;
        VectorialPointValuePair current = new VectorialPointValuePair(point, objective);
        while (true) {
            incrementIterationsCounter();

            // compute the Q.R. decomposition of the jacobian matrix
            VectorialPointValuePair previous = current;
            updateJacobian();
            qrDecomposition();

            // compute Qt.res
            qTy(residuals);
            // now we don't need Q anymore,
            // so let jacobian contain the R matrix with its diagonal elements
            for (int k = 0; k < solvedCols; ++k) {
                int pk = permutation[k];
                jacobian[k][pk] = diagR[pk];
            }

            if (firstIteration) {

                // scale the point according to the norms of the columns
                // of the initial jacobian
                xNorm = 0;
                for (int k = 0; k < cols; ++k) {
                    double dk = jacNorm[k];
                    if (dk == 0) {
                        dk = 1.0;
                    }
                    double xk = dk * point[k];
                    xNorm  += xk * xk;
                    diag[k] = dk;
                }
                xNorm = Math.sqrt(xNorm);

                // initialize the step bound delta
                delta = (xNorm == 0) ? initialStepBoundFactor : (initialStepBoundFactor * xNorm);

            }

            // check orthogonality between function vector and jacobian columns
            double maxCosine = 0;
            if (cost != 0) {
                for (int j = 0; j < solvedCols; ++j) {
                    int    pj = permutation[j];
                    double s  = jacNorm[pj];
                    if (s != 0) {
                        double sum = 0;
                        for (int i = 0; i <= j; ++i) {
                            sum += jacobian[i][pj] * residuals[i];
                        }
                        maxCosine = Math.max(maxCosine, Math.abs(sum) / (s * cost));
                    }
                }
            }
            if (maxCosine <= orthoTolerance) {
                // convergence has been reached
                return current;
            }

            // rescale if necessary
            for (int j = 0; j < cols; ++j) {
                diag[j] = Math.max(diag[j], jacNorm[j]);
            }

            // inner loop
            for (double ratio = 0; ratio < 1.0e-4;) {

                // save the state
                for (int j = 0; j < solvedCols; ++j) {
                    int pj = permutation[j];
                    oldX[pj] = point[pj];
                }
                double previousCost = cost;
                double[] tmpVec = residuals;
                residuals = oldRes;
                oldRes    = tmpVec;

                // determine the Levenberg-Marquardt parameter
                determineLMParameter(oldRes, delta, diag, work1, work2, work3);

                // compute the new point and the norm of the evolution direction
                double lmNorm = 0;
                for (int j = 0; j < solvedCols; ++j) {
                    int pj = permutation[j];
                    lmDir[pj] = -lmDir[pj];
                    point[pj] = oldX[pj] + lmDir[pj];
                    double s = diag[pj] * lmDir[pj];
                    lmNorm  += s * s;
                }
                lmNorm = Math.sqrt(lmNorm);
                // on the first iteration, adjust the initial step bound.
                if (firstIteration) {
                    delta = Math.min(delta, lmNorm);
                }

                // evaluate the function at x + p and calculate its norm
                updateResidualsAndCost();
                current = new VectorialPointValuePair(point, objective);

                // compute the scaled actual reduction
                double actRed = -1.0;
                if (0.1 * cost < previousCost) {
                    double r = cost / previousCost;
                    actRed = 1.0 - r * r;
                }

                // compute the scaled predicted reduction
                // and the scaled directional derivative
                for (int j = 0; j < solvedCols; ++j) {
                    int pj = permutation[j];
                    double dirJ = lmDir[pj];
                    work1[j] = 0;
                    for (int i = 0; i <= j; ++i) {
                        work1[i] += jacobian[i][pj] * dirJ;
                    }
                }
                double coeff1 = 0;
                for (int j = 0; j < solvedCols; ++j) {
                    coeff1 += work1[j] * work1[j];
                }
                double pc2 = previousCost * previousCost;
                coeff1 = coeff1 / pc2;
                double coeff2 = lmPar * lmNorm * lmNorm / pc2;
                double preRed = coeff1 + 2 * coeff2;
                double dirDer = -(coeff1 + coeff2);

                // ratio of the actual to the predicted reduction
                ratio = (preRed == 0) ? 0 : (actRed / preRed);

                // update the step bound
                if (ratio <= 0.25) {
                    double tmp =
                        (actRed < 0) ? (0.5 * dirDer / (dirDer + 0.5 * actRed)) : 0.5;
                        if ((0.1 * cost >= previousCost) || (tmp < 0.1)) {
                            tmp = 0.1;
                        }
                        delta = tmp * Math.min(delta, 10.0 * lmNorm);
                        lmPar /= tmp;
                } else if ((lmPar == 0) || (ratio >= 0.75)) {
                    delta = 2 * lmNorm;
                    lmPar *= 0.5;
                }

                // test for successful iteration.
                if (ratio >= 1.0e-4) {
                    // successful iteration, update the norm
                    firstIteration = false;
                    xNorm = 0;
                    for (int k = 0; k < cols; ++k) {
                        double xK = diag[k] * point[k];
                        xNorm    += xK * xK;
                    }
                    xNorm = Math.sqrt(xNorm);

                    // tests for convergence.
                    // we use the vectorial convergence checker
                } else {
                    // failed iteration, reset the previous values
                    cost = previousCost;
                    for (int j = 0; j < solvedCols; ++j) {
                        int pj = permutation[j];
                        point[pj] = oldX[pj];
                    }
                    tmpVec    = residuals;
                    residuals = oldRes;
                    oldRes    = tmpVec;
                }
                if (checker==null) {
                	if (((Math.abs(actRed) <= costRelativeTolerance) &&
                        (preRed <= costRelativeTolerance) &&
                        (ratio <= 2.0)) ||
                       (delta <= parRelativeTolerance * xNorm)) {
                       return current;
                   }
                } else {
                    if (checker.converged(getIterations(), previous, current)) {
                        return current;
                    }
                }
                // tests for termination and stringent tolerances
                // (2.2204e-16 is the machine epsilon for IEEE754)
                if ((Math.abs(actRed) <= 2.2204e-16) && (preRed <= 2.2204e-16) && (ratio <= 2.0)) {
                    throw new OptimizationException(LocalizedFormats.TOO_SMALL_COST_RELATIVE_TOLERANCE,
                            costRelativeTolerance);
                } else if (delta <= 2.2204e-16 * xNorm) {
                    throw new OptimizationException(LocalizedFormats.TOO_SMALL_PARAMETERS_RELATIVE_TOLERANCE,
                            parRelativeTolerance);
                } else if (maxCosine <= 2.2204e-16)  {
                    throw new OptimizationException(LocalizedFormats.TOO_SMALL_ORTHOGONALITY_TOLERANCE,
                            orthoTolerance);
                }

            }

        }

    }

// relevant test
// org.apache.commons.math.optimization.fitting.CurveFitterTest::testMath303
    public void testMath303()
        throws OptimizationException, FunctionEvaluationException {

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
        throws OptimizationException, FunctionEvaluationException {

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
    throws OptimizationException, FunctionEvaluationException {
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

                return d + ((a - d) / (1 + Math.pow(x / c, b)));
            }

            public double[] gradient(double x, double[] parameters) {

                double a = parameters[0];
                double b = parameters[1];
                double c = parameters[2];
                double d = parameters[3];

                double[] gradients = new double[4];
                double den = 1 + Math.pow(x / c, b);

                
                gradients[0] = 1 / den;

                
                
                gradients[1] = -((a - d) * Math.pow(x / c, b) * Math.log(x / c)) / (den * den);

                
                gradients[2] = (b * Math.pow(x / c, b - 1) * (x / (c * c)) * (a - d)) / (den * den);

                
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
    throws OptimizationException, FunctionEvaluationException {
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
    throws OptimizationException, FunctionEvaluationException {
        GaussianFitter fitter = new GaussianFitter(new LevenbergMarquardtOptimizer());
        fitter.fit();
    }

// org.apache.commons.math.optimization.fitting.GaussianFitterTest::testFit03
    public void testFit03()
    throws OptimizationException, FunctionEvaluationException {
        GaussianFitter fitter = new GaussianFitter(new LevenbergMarquardtOptimizer());
        addDatasetToGaussianFitter(new double[][] {
            {4.0254623,  531026.0},
            {4.02804905, 664002.0}},
            fitter);
        fitter.fit();
    }

// org.apache.commons.math.optimization.fitting.GaussianFitterTest::testFit04
    public void testFit04()
    throws OptimizationException, FunctionEvaluationException {
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
    throws OptimizationException, FunctionEvaluationException {
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
    throws OptimizationException, FunctionEvaluationException {
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
    throws OptimizationException, FunctionEvaluationException {
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
            assertTrue(Math.abs(f.value(x) - fitted.value(x)) < 1.0e-13);
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
    throws OptimizationException, FunctionEvaluationException {
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
    throws OptimizationException, FunctionEvaluationException {
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
    public void testValue01() throws FunctionEvaluationException {
        ParametricGaussianFunction f = new ParametricGaussianFunction();
        f.value(0.0, null);
    }

// org.apache.commons.math.optimization.fitting.ParametricGaussianFunctionTest::testValue02
    public void testValue02() throws FunctionEvaluationException {
        ParametricGaussianFunction f = new ParametricGaussianFunction();
        f.value(0.0, new double[] {0.0, 1.0});
    }

// org.apache.commons.math.optimization.fitting.ParametricGaussianFunctionTest::testValue03
    public void testValue03() throws FunctionEvaluationException {
        ParametricGaussianFunction f = new ParametricGaussianFunction();
        f.value(0.0, new double[] {0.0, 1.0, 1.0, 0.0});
    }

// org.apache.commons.math.optimization.fitting.PolynomialFitterTest::testNoError
    public void testNoError() throws OptimizationException {
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
                double error = Math.abs(p.value(x) - fitted.value(x)) /
                               (1.0 + Math.abs(p.value(x)));
                assertEquals(0.0, error, 1.0e-6);
            }

        }

    }

// org.apache.commons.math.optimization.fitting.PolynomialFitterTest::testSmallError
    public void testSmallError() throws OptimizationException {
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
                double error = Math.abs(p.value(x) - fitted.value(x)) /
                              (1.0 + Math.abs(p.value(x)));
                maxError = Math.max(maxError, error);
                assertTrue(Math.abs(error) < 0.1);
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

// org.apache.commons.math.optimization.general.LevenbergMarquardtOptimizerTest::testTrivial
    public void testTrivial() throws FunctionEvaluationException, OptimizationException {
        LinearProblem problem =
            new LinearProblem(new double[][] { { 2 } }, new double[] { 3 });
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        VectorialPointValuePair optimum =
            optimizer.optimize(problem, problem.target, new double[] { 1 }, new double[] { 0 });
        assertEquals(0, optimizer.getRMS(), 1.0e-10);
        try {
            optimizer.guessParametersErrors();
            fail("an exception should have been thrown");
        } catch (OptimizationException ee) {
            
        } catch (Exception e) {
            fail("wrong exception caught");
        }
        assertEquals(1.5, optimum.getPoint()[0], 1.0e-10);
        assertEquals(3.0, optimum.getValue()[0], 1.0e-10);
    }

// org.apache.commons.math.optimization.general.LevenbergMarquardtOptimizerTest::testQRColumnsPermutation
    public void testQRColumnsPermutation() throws FunctionEvaluationException, OptimizationException {

        LinearProblem problem =
            new LinearProblem(new double[][] { { 1.0, -1.0 }, { 0.0, 2.0 }, { 1.0, -2.0 } },
                              new double[] { 4.0, 6.0, 1.0 });

        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        VectorialPointValuePair optimum =
            optimizer.optimize(problem, problem.target, new double[] { 1, 1, 1 }, new double[] { 0, 0 });
        assertEquals(0, optimizer.getRMS(), 1.0e-10);
        assertEquals(7.0, optimum.getPoint()[0], 1.0e-10);
        assertEquals(3.0, optimum.getPoint()[1], 1.0e-10);
        assertEquals(4.0, optimum.getValue()[0], 1.0e-10);
        assertEquals(6.0, optimum.getValue()[1], 1.0e-10);
        assertEquals(1.0, optimum.getValue()[2], 1.0e-10);

    }

// org.apache.commons.math.optimization.general.LevenbergMarquardtOptimizerTest::testNoDependency
    public void testNoDependency() throws FunctionEvaluationException, OptimizationException {
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
            optimizer.optimize(problem, problem.target, new double[] { 1, 1, 1, 1, 1, 1 },
                               new double[] { 0, 0, 0, 0, 0, 0 });
        assertEquals(0, optimizer.getRMS(), 1.0e-10);
        for (int i = 0; i < problem.target.length; ++i) {
            assertEquals(0.55 * i, optimum.getPoint()[i], 1.0e-10);
        }
    }

// org.apache.commons.math.optimization.general.LevenbergMarquardtOptimizerTest::testOneSet
    public void testOneSet() throws FunctionEvaluationException, OptimizationException {

        LinearProblem problem = new LinearProblem(new double[][] {
                {  1,  0, 0 },
                { -1,  1, 0 },
                {  0, -1, 1 }
        }, new double[] { 1, 1, 1});
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        VectorialPointValuePair optimum =
            optimizer.optimize(problem, problem.target, new double[] { 1, 1, 1 }, new double[] { 0, 0, 0 });
        assertEquals(0, optimizer.getRMS(), 1.0e-10);
        assertEquals(1.0, optimum.getPoint()[0], 1.0e-10);
        assertEquals(2.0, optimum.getPoint()[1], 1.0e-10);
        assertEquals(3.0, optimum.getPoint()[2], 1.0e-10);

    }

// org.apache.commons.math.optimization.general.LevenbergMarquardtOptimizerTest::testTwoSets
    public void testTwoSets() throws FunctionEvaluationException, OptimizationException {
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
            optimizer.optimize(problem, problem.target, new double[] { 1, 1, 1, 1, 1, 1 },
                               new double[] { 0, 0, 0, 0, 0, 0 });
        assertEquals(0, optimizer.getRMS(), 1.0e-10);
        assertEquals( 3.0, optimum.getPoint()[0], 1.0e-10);
        assertEquals( 4.0, optimum.getPoint()[1], 1.0e-10);
        assertEquals(-1.0, optimum.getPoint()[2], 1.0e-10);
        assertEquals(-2.0, optimum.getPoint()[3], 1.0e-10);
        assertEquals( 1.0 + epsilon, optimum.getPoint()[4], 1.0e-10);
        assertEquals( 1.0 - epsilon, optimum.getPoint()[5], 1.0e-10);

    }

// org.apache.commons.math.optimization.general.LevenbergMarquardtOptimizerTest::testNonInversible
    public void testNonInversible() throws FunctionEvaluationException, OptimizationException {

        LinearProblem problem = new LinearProblem(new double[][] {
                {  1, 2, -3 },
                {  2, 1,  3 },
                { -3, 0, -9 }
        }, new double[] { 1, 1, 1 });

        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        optimizer.optimize(problem, problem.target, new double[] { 1, 1, 1 }, new double[] { 0, 0, 0 });
        assertTrue(Math.sqrt(problem.target.length) * optimizer.getRMS() > 0.6);
        try {
            optimizer.getCovariances();
            fail("an exception should have been thrown");
        } catch (OptimizationException ee) {
            
        } catch (Exception e) {
            fail("wrong exception caught");
        }

    }

// org.apache.commons.math.optimization.general.LevenbergMarquardtOptimizerTest::testIllConditioned
    public void testIllConditioned() throws FunctionEvaluationException, OptimizationException {
        LinearProblem problem1 = new LinearProblem(new double[][] {
                { 10.0, 7.0,  8.0,  7.0 },
                {  7.0, 5.0,  6.0,  5.0 },
                {  8.0, 6.0, 10.0,  9.0 },
                {  7.0, 5.0,  9.0, 10.0 }
        }, new double[] { 32, 23, 33, 31 });
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        VectorialPointValuePair optimum1 =
            optimizer.optimize(problem1, problem1.target, new double[] { 1, 1, 1, 1 },
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
            optimizer.optimize(problem2, problem2.target, new double[] { 1, 1, 1, 1 },
                               new double[] { 0, 1, 2, 3 });
        assertEquals(0, optimizer.getRMS(), 1.0e-10);
        assertEquals(-81.0, optimum2.getPoint()[0], 1.0e-8);
        assertEquals(137.0, optimum2.getPoint()[1], 1.0e-8);
        assertEquals(-34.0, optimum2.getPoint()[2], 1.0e-8);
        assertEquals( 22.0, optimum2.getPoint()[3], 1.0e-8);

    }

// org.apache.commons.math.optimization.general.LevenbergMarquardtOptimizerTest::testMoreEstimatedParametersSimple
    public void testMoreEstimatedParametersSimple() throws FunctionEvaluationException, OptimizationException {

        LinearProblem problem = new LinearProblem(new double[][] {
                { 3.0, 2.0,  0.0, 0.0 },
                { 0.0, 1.0, -1.0, 1.0 },
                { 2.0, 0.0,  1.0, 0.0 }
        }, new double[] { 7.0, 3.0, 5.0 });

        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        optimizer.optimize(problem, problem.target, new double[] { 1, 1, 1 },
                new double[] { 7, 6, 5, 4 });
        assertEquals(0, optimizer.getRMS(), 1.0e-10);

    }

// org.apache.commons.math.optimization.general.LevenbergMarquardtOptimizerTest::testMoreEstimatedParametersUnsorted
    public void testMoreEstimatedParametersUnsorted() throws FunctionEvaluationException, OptimizationException {
        LinearProblem problem = new LinearProblem(new double[][] {
                { 1.0, 1.0,  0.0,  0.0, 0.0,  0.0 },
                { 0.0, 0.0,  1.0,  1.0, 1.0,  0.0 },
                { 0.0, 0.0,  0.0,  0.0, 1.0, -1.0 },
                { 0.0, 0.0, -1.0,  1.0, 0.0,  1.0 },
                { 0.0, 0.0,  0.0, -1.0, 1.0,  0.0 }
       }, new double[] { 3.0, 12.0, -1.0, 7.0, 1.0 });

        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        VectorialPointValuePair optimum =
            optimizer.optimize(problem, problem.target, new double[] { 1, 1, 1, 1, 1 },
                               new double[] { 2, 2, 2, 2, 2, 2 });
        assertEquals(0, optimizer.getRMS(), 1.0e-10);
        assertEquals(3.0, optimum.getPointRef()[2], 1.0e-10);
        assertEquals(4.0, optimum.getPointRef()[3], 1.0e-10);
        assertEquals(5.0, optimum.getPointRef()[4], 1.0e-10);
        assertEquals(6.0, optimum.getPointRef()[5], 1.0e-10);

    }

// org.apache.commons.math.optimization.general.LevenbergMarquardtOptimizerTest::testRedundantEquations
    public void testRedundantEquations() throws FunctionEvaluationException, OptimizationException {
        LinearProblem problem = new LinearProblem(new double[][] {
                { 1.0,  1.0 },
                { 1.0, -1.0 },
                { 1.0,  3.0 }
        }, new double[] { 3.0, 1.0, 5.0 });

        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        VectorialPointValuePair optimum =
            optimizer.optimize(problem, problem.target, new double[] { 1, 1, 1 },
                               new double[] { 1, 1 });
        assertEquals(0, optimizer.getRMS(), 1.0e-10);
        assertEquals(2.0, optimum.getPointRef()[0], 1.0e-10);
        assertEquals(1.0, optimum.getPointRef()[1], 1.0e-10);

    }

// org.apache.commons.math.optimization.general.LevenbergMarquardtOptimizerTest::testInconsistentEquations
    public void testInconsistentEquations() throws FunctionEvaluationException, OptimizationException {
        LinearProblem problem = new LinearProblem(new double[][] {
                { 1.0,  1.0 },
                { 1.0, -1.0 },
                { 1.0,  3.0 }
        }, new double[] { 3.0, 1.0, 4.0 });

        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        optimizer.optimize(problem, problem.target, new double[] { 1, 1, 1 }, new double[] { 1, 1 });
        assertTrue(optimizer.getRMS() > 0.1);

    }

// org.apache.commons.math.optimization.general.LevenbergMarquardtOptimizerTest::testInconsistentSizes
    public void testInconsistentSizes() throws FunctionEvaluationException, OptimizationException {
        LinearProblem problem =
            new LinearProblem(new double[][] { { 1, 0 }, { 0, 1 } }, new double[] { -1, 1 });
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();

        VectorialPointValuePair optimum =
            optimizer.optimize(problem, problem.target, new double[] { 1, 1 }, new double[] { 0, 0 });
        assertEquals(0, optimizer.getRMS(), 1.0e-10);
        assertEquals(-1, optimum.getPoint()[0], 1.0e-10);
        assertEquals(+1, optimum.getPoint()[1], 1.0e-10);

        try {
            optimizer.optimize(problem, problem.target,
                               new double[] { 1 },
                               new double[] { 0, 0 });
            fail("an exception should have been thrown");
        } catch (OptimizationException oe) {
            
        } catch (Exception e) {
            fail("wrong exception caught");
        }

        try {
            optimizer.optimize(problem, new double[] { 1 },
                               new double[] { 1 },
                               new double[] { 0, 0 });
            fail("an exception should have been thrown");
        } catch (FunctionEvaluationException oe) {
            
        } catch (Exception e) {
            fail("wrong exception caught");
        }

    }

// org.apache.commons.math.optimization.general.LevenbergMarquardtOptimizerTest::testControlParameters
    public void testControlParameters() {
        Circle circle = new Circle();
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
    public void testCircleFitting() throws FunctionEvaluationException, OptimizationException {
        Circle circle = new Circle();
        circle.addPoint( 30.0,  68.0);
        circle.addPoint( 50.0,  -6.0);
        circle.addPoint(110.0, -20.0);
        circle.addPoint( 35.0,  15.0);
        circle.addPoint( 45.0,  97.0);
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        VectorialPointValuePair optimum =
            optimizer.optimize(circle, new double[] { 0, 0, 0, 0, 0 }, new double[] { 1, 1, 1, 1, 1 },
                               new double[] { 98.680, 47.345 });
        assertTrue(optimizer.getEvaluations() < 10);
        assertTrue(optimizer.getJacobianEvaluations() < 10);
        double rms = optimizer.getRMS();
        assertEquals(1.768262623567235,  Math.sqrt(circle.getN()) * rms,  1.0e-10);
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
        for (double d= 0; d < 2 * Math.PI; d += 0.01) {
            circle.addPoint(center.x + r * Math.cos(d), center.y + r * Math.sin(d));
        }
        double[] target = new double[circle.getN()];
        Arrays.fill(target, 0.0);
        double[] weights = new double[circle.getN()];
        Arrays.fill(weights, 2.0);
        optimizer.optimize(circle, target, weights, new double[] { 98.680, 47.345 });
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
    public void testCircleFittingBadInit() throws FunctionEvaluationException, OptimizationException {
        Circle circle = new Circle();
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
            optimizer.optimize(circle, target, weights, new double[] { -12, -12 });
        Point2D.Double center = new Point2D.Double(optimum.getPointRef()[0], optimum.getPointRef()[1]);
        assertTrue(optimizer.getEvaluations() < 25);
        assertTrue(optimizer.getJacobianEvaluations() < 20);
        assertEquals( 0.043, optimizer.getRMS(), 1.0e-3);
        assertEquals( 0.292235,  circle.getRadius(center), 1.0e-6);
        assertEquals(-0.151738,  center.x,      1.0e-6);
        assertEquals( 0.2075001, center.y,      1.0e-6);
    }

// org.apache.commons.math.optimization.general.LevenbergMarquardtOptimizerTest::testMath199
    public void testMath199() throws FunctionEvaluationException {
        try {
            QuadraticProblem problem = new QuadraticProblem();
            problem.addPoint (0, -3.182591015485607);
            problem.addPoint (1, -2.5581184967730577);
            problem.addPoint (2, -2.1488478161387325);
            problem.addPoint (3, -1.9122489313410047);
            problem.addPoint (4, 1.7785661310051026);
            LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
            optimizer.setQRRankingThreshold(0);
            optimizer.optimize(problem,
                               new double[] { 0, 0, 0, 0, 0 },
                               new double[] { 0.0, 4.4e-323, 1.0, 4.4e-323, 0.0 },
                               new double[] { 0, 0, 0 });
            fail("an exception should have been thrown");
        } catch (OptimizationException ee) {
            
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
                                       Math.sqrt(24.2)), false);
    minpackTest(new RosenbrockFunction(new double[] { -12.0, 10.0 },
                                       Math.sqrt(1795769.0)), false);
    minpackTest(new RosenbrockFunction(new double[] { -120.0, 100.0 },
                                       11.0 * Math.sqrt(169000121.0)), false);
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
