// buggy code
    private void prelim(double[] lowerBound,
                        double[] upperBound) {
        printMethod(); // XXX

        final int n = currentBest.getDimension();
        final int npt = numberOfInterpolationPoints;
        final int ndim = bMatrix.getRowDimension();

        final double rhosq = initialTrustRegionRadius * initialTrustRegionRadius;
        final double recip = 1d / rhosq;
        final int np = n + 1;

        // Set XBASE to the initial vector of variables, and set the initial
        // elements of XPT, BMAT, HQ, PQ and ZMAT to zero.

        for (int j = 0; j < n; j++) {
            originShift.setEntry(j, currentBest.getEntry(j));
            for (int k = 0; k < npt; k++) {
                interpolationPoints.setEntry(k, j, ZERO);
            }
            for (int i = 0; i < ndim; i++) {
                bMatrix.setEntry(i, j, ZERO);
            }
        }
        for (int i = 0, max = n * np / 2; i < max; i++) {
            modelSecondDerivativesValues.setEntry(i, ZERO);
        }
        for (int k = 0; k < npt; k++) {
            modelSecondDerivativesParameters.setEntry(k, ZERO);
            for (int j = 0, max = npt - np; j < max; j++) {
                zMatrix.setEntry(k, j, ZERO);
            }
        }

        // Begin the initialization procedure. NF becomes one more than the number
        // of function values so far. The coordinates of the displacement of the
        // next initial interpolation point from XBASE are set in XPT(NF+1,.).

        int ipt = 0;
        int jpt = 0;
        double fbeg = Double.NaN;
        do {
            final int nfm = getEvaluations();
            final int nfx = nfm - n;
            final int nfmm = nfm - 1;
            final int nfxm = nfx - 1;
            double stepa = 0;
            double stepb = 0;
            if (nfm <= 2 * n) {
                if (nfm >= 1 &&
                    nfm <= n) {
                    stepa = initialTrustRegionRadius;
                    if (upperDifference.getEntry(nfmm) == ZERO) {
                        stepa = -stepa;
                        throw new PathIsExploredException(); // XXX
                    }
                    interpolationPoints.setEntry(nfm, nfmm, stepa);
                } else if (nfm > n) {
                    stepa = interpolationPoints.getEntry(nfx, nfxm);
                    stepb = -initialTrustRegionRadius;
                    if (lowerDifference.getEntry(nfxm) == ZERO) {
                        stepb = Math.min(TWO * initialTrustRegionRadius, upperDifference.getEntry(nfxm));
                        throw new PathIsExploredException(); // XXX
                    }
                    if (upperDifference.getEntry(nfxm) == ZERO) {
                        stepb = Math.max(-TWO * initialTrustRegionRadius, lowerDifference.getEntry(nfxm));
                        throw new PathIsExploredException(); // XXX
                    }
                    interpolationPoints.setEntry(nfm, nfxm, stepb);
                }
            } else {
                final int tmp1 = (nfm - np) / n;
                jpt = nfm - tmp1 * n - n;
                ipt = jpt + tmp1;
                if (ipt > n) {
                    final int tmp2 = jpt;
                    jpt = ipt - n;
                    ipt = tmp2;
                    throw new PathIsExploredException(); // XXX
                }
                final int iptMinus1 = ipt;
                final int jptMinus1 = jpt;
                interpolationPoints.setEntry(nfm, iptMinus1, interpolationPoints.getEntry(ipt, iptMinus1));
                interpolationPoints.setEntry(nfm, jptMinus1, interpolationPoints.getEntry(jpt, jptMinus1));
            }

            // Calculate the next value of F. The least function value so far and
            // its index are required.

            for (int j = 0; j < n; j++) {
                currentBest.setEntry(j, Math.min(Math.max(lowerBound[j],
                                                          originShift.getEntry(j) + interpolationPoints.getEntry(nfm, j)),
                                                 upperBound[j]));
                if (interpolationPoints.getEntry(nfm, j) == lowerDifference.getEntry(j)) {
                    currentBest.setEntry(j, lowerBound[j]);
                }
                if (interpolationPoints.getEntry(nfm, j) == upperDifference.getEntry(j)) {
                    currentBest.setEntry(j, upperBound[j]);
                }
            }

            final double objectiveValue = computeObjectiveValue(currentBest.toArray());
            final double f = isMinimize ? objectiveValue : -objectiveValue;
            final int numEval = getEvaluations(); // nfm + 1
            fAtInterpolationPoints.setEntry(nfm, f);

            if (numEval == 1) {
                fbeg = f;
                trustRegionCenterInterpolationPointIndex = 0;
            } else if (f < fAtInterpolationPoints.getEntry(trustRegionCenterInterpolationPointIndex)) {
                trustRegionCenterInterpolationPointIndex = nfm;
            }

            // Set the nonzero initial elements of BMAT and the quadratic model in the
            // cases when NF is at most 2*N+1. If NF exceeds N+1, then the positions
            // of the NF-th and (NF-N)-th interpolation points may be switched, in
            // order that the function value at the first of them contributes to the
            // off-diagonal second derivative terms of the initial quadratic model.

            if (numEval <= 2 * n + 1) {
                if (numEval >= 2 &&
                    numEval <= n + 1) {
                    gradientAtTrustRegionCenter.setEntry(nfmm, (f - fbeg) / stepa);
                    if (npt < numEval + n) {
                        final double oneOverStepA = ONE / stepa;
                        bMatrix.setEntry(0, nfmm, -oneOverStepA);
                        bMatrix.setEntry(nfm, nfmm, oneOverStepA);
                        bMatrix.setEntry(npt + nfmm, nfmm, -HALF * rhosq);
                        throw new PathIsExploredException(); // XXX
                    }
                } else if (numEval >= n + 2) {
                    final int ih = nfx * (nfx + 1) / 2 - 1;
                    final double tmp = (f - fbeg) / stepb;
                    final double diff = stepb - stepa;
                    modelSecondDerivativesValues.setEntry(ih, TWO * (tmp - gradientAtTrustRegionCenter.getEntry(nfxm)) / diff);
                    gradientAtTrustRegionCenter.setEntry(nfxm, (gradientAtTrustRegionCenter.getEntry(nfxm) * stepb - tmp * stepa) / diff);
                    if (stepa * stepb < ZERO) {
                        if (f < fAtInterpolationPoints.getEntry(nfm - n)) {
                            fAtInterpolationPoints.setEntry(nfm, fAtInterpolationPoints.getEntry(nfm - n));
                            fAtInterpolationPoints.setEntry(nfm - n, f);
                            if (trustRegionCenterInterpolationPointIndex == nfm) {
                                trustRegionCenterInterpolationPointIndex = nfm - n;
                            }
                            interpolationPoints.setEntry(nfm - n, nfxm, stepb);
                            interpolationPoints.setEntry(nfm, nfxm, stepa);
                        }
                    }
                    bMatrix.setEntry(0, nfxm, -(stepa + stepb) / (stepa * stepb));
                    bMatrix.setEntry(nfm, nfxm, -HALF / interpolationPoints.getEntry(nfm - n, nfxm));
                    bMatrix.setEntry(nfm - n, nfxm,
                                  -bMatrix.getEntry(0, nfxm) - bMatrix.getEntry(nfm, nfxm));
                    zMatrix.setEntry(0, nfxm, Math.sqrt(TWO) / (stepa * stepb));
                    zMatrix.setEntry(nfm, nfxm, Math.sqrt(HALF) / rhosq);
                    // zMatrix.setEntry(nfm, nfxm, Math.sqrt(HALF) * recip); // XXX "testAckley" and "testDiffPow" fail.
                    zMatrix.setEntry(nfm - n, nfxm,
                                  -zMatrix.getEntry(0, nfxm) - zMatrix.getEntry(nfm, nfxm));
                }

                // Set the off-diagonal second derivatives of the Lagrange functions and
                // the initial quadratic model.

            } else {
                zMatrix.setEntry(0, nfxm, recip);
                zMatrix.setEntry(nfm, nfxm, recip);
                zMatrix.setEntry(ipt, nfxm, -recip);
                zMatrix.setEntry(jpt, nfxm, -recip);

                final int ih = ipt * (ipt - 1) / 2 + jpt - 1;
                final double tmp = interpolationPoints.getEntry(nfm, ipt - 1) * interpolationPoints.getEntry(nfm, jpt - 1);
                modelSecondDerivativesValues.setEntry(ih, (fbeg - fAtInterpolationPoints.getEntry(ipt) - fAtInterpolationPoints.getEntry(jpt) + f) / tmp);
                throw new PathIsExploredException(); // XXX
            }
        } while (getEvaluations() < npt);
    } // prelim

// relevant test
// org.apache.commons.math.optimization.direct.BOBYQAOptimizerTest::testInitOutOfBounds
    public void testInitOutOfBounds() {
        double[] startPoint = point(DIM, 3);
        double[][] boundaries = boundaries(DIM, -1, 2);
        doTest(new Rosen(), startPoint, boundaries,
                GoalType.MINIMIZE, 
                1e-13, 1e-6, 2000, null);
    }

// org.apache.commons.math.optimization.direct.BOBYQAOptimizerTest::testBoundariesDimensionMismatch
    public void testBoundariesDimensionMismatch() {
        double[] startPoint = point(DIM, 0.5);
        double[][] boundaries = boundaries(DIM + 1, -1, 2);
        doTest(new Rosen(), startPoint, boundaries,
               GoalType.MINIMIZE, 
               1e-13, 1e-6, 2000, null);
    }

// org.apache.commons.math.optimization.direct.BOBYQAOptimizerTest::testProblemDimensionTooSmall
    public void testProblemDimensionTooSmall() {
        double[] startPoint = point(1, 0.5);
        doTest(new Rosen(), startPoint, null,
               GoalType.MINIMIZE,
               1e-13, 1e-6, 2000, null);
    }

// org.apache.commons.math.optimization.direct.BOBYQAOptimizerTest::testMaxEvaluations
    public void testMaxEvaluations() {
        final int lowMaxEval = 2;
        double[] startPoint = point(DIM, 0.1);
        double[][] boundaries = null;
        doTest(new Rosen(), startPoint, boundaries,
               GoalType.MINIMIZE, 
               1e-13, 1e-6, lowMaxEval, null);
     }

// org.apache.commons.math.optimization.direct.BOBYQAOptimizerTest::testRosen
    public void testRosen() {
        double[] startPoint = point(DIM,0.1);
        double[][] boundaries = null;
        RealPointValuePair expected = new RealPointValuePair(point(DIM,1.0),0.0);
        doTest(new Rosen(), startPoint, boundaries,
                GoalType.MINIMIZE, 
                1e-13, 1e-6, 2000, expected);
     }

// org.apache.commons.math.optimization.direct.BOBYQAOptimizerTest::testMaximize
    public void testMaximize() {
        double[] startPoint = point(DIM,1.0);
        double[][] boundaries = null;
        RealPointValuePair expected = new RealPointValuePair(point(DIM,0.0),1.0);
        doTest(new MinusElli(), startPoint, boundaries,
                GoalType.MAXIMIZE, 
                2e-10, 5e-6, 1000, expected);
        boundaries = boundaries(DIM,-0.3,0.3); 
        startPoint = point(DIM,0.1);
        doTest(new MinusElli(), startPoint, boundaries,
                GoalType.MAXIMIZE, 
                2e-10, 5e-6, 1000, expected);
    }

// org.apache.commons.math.optimization.direct.BOBYQAOptimizerTest::testEllipse
    public void testEllipse() {
        double[] startPoint = point(DIM,1.0);
        double[][] boundaries = null;
        RealPointValuePair expected =
            new RealPointValuePair(point(DIM,0.0),0.0);
        doTest(new Elli(), startPoint, boundaries,
                GoalType.MINIMIZE, 
                1e-13, 1e-6, 1000, expected);
     }

// org.apache.commons.math.optimization.direct.BOBYQAOptimizerTest::testElliRotated
    public void testElliRotated() {
        double[] startPoint = point(DIM,1.0);
        double[][] boundaries = null;
        RealPointValuePair expected =
            new RealPointValuePair(point(DIM,0.0),0.0);
        doTest(new ElliRotated(), startPoint, boundaries,
                GoalType.MINIMIZE, 
                1e-12, 1e-6, 10000, expected);
    }

// org.apache.commons.math.optimization.direct.BOBYQAOptimizerTest::testCigar
    public void testCigar() {
        double[] startPoint = point(DIM,1.0);
        double[][] boundaries = null;
        RealPointValuePair expected =
            new RealPointValuePair(point(DIM,0.0),0.0);
        doTest(new Cigar(), startPoint, boundaries,
                GoalType.MINIMIZE, 
                1e-13, 1e-6, 100, expected);
    }

// org.apache.commons.math.optimization.direct.BOBYQAOptimizerTest::testTwoAxes
    public void testTwoAxes() {
        double[] startPoint = point(DIM,1.0);
        double[][] boundaries = null;
        RealPointValuePair expected =
            new RealPointValuePair(point(DIM,0.0),0.0);
        doTest(new TwoAxes(), startPoint, boundaries,
                GoalType.MINIMIZE, 2*
                1e-13, 1e-6, 100, expected);
     }

// org.apache.commons.math.optimization.direct.BOBYQAOptimizerTest::testCigTab
    public void testCigTab() {
        double[] startPoint = point(DIM,1.0);
        double[][] boundaries = null;
        RealPointValuePair expected =
            new RealPointValuePair(point(DIM,0.0),0.0);
        doTest(new CigTab(), startPoint, boundaries,
                GoalType.MINIMIZE, 
                1e-13, 5e-5, 100, expected);
     }

// org.apache.commons.math.optimization.direct.BOBYQAOptimizerTest::testSphere
    public void testSphere() {
        double[] startPoint = point(DIM,1.0);
        double[][] boundaries = null;
        RealPointValuePair expected =
            new RealPointValuePair(point(DIM,0.0),0.0);
        doTest(new Sphere(), startPoint, boundaries,
                GoalType.MINIMIZE, 
                1e-13, 1e-6, 100, expected);
    }

// org.apache.commons.math.optimization.direct.BOBYQAOptimizerTest::testTablet
    public void testTablet() {
        double[] startPoint = point(DIM,1.0); 
        double[][] boundaries = null;
        RealPointValuePair expected =
            new RealPointValuePair(point(DIM,0.0),0.0);
        doTest(new Tablet(), startPoint, boundaries,
                GoalType.MINIMIZE, 
                1e-13, 1e-6, 100, expected);
    }

// org.apache.commons.math.optimization.direct.BOBYQAOptimizerTest::testDiffPow
    public void testDiffPow() {}

// org.apache.commons.math.optimization.direct.BOBYQAOptimizerTest::testSsDiffPow
    public void testSsDiffPow() {
        double[] startPoint = point(DIM/2,1.0);
        double[][] boundaries = null;
        RealPointValuePair expected =
            new RealPointValuePair(point(DIM/2,0.0),0.0);
        doTest(new SsDiffPow(), startPoint, boundaries,
                GoalType.MINIMIZE, 
                1e-2, 1.3e-1, 50000, expected);
    }

// org.apache.commons.math.optimization.direct.BOBYQAOptimizerTest::testAckley
    public void testAckley() {}

// org.apache.commons.math.optimization.direct.BOBYQAOptimizerTest::testRastrigin
    public void testRastrigin() {
        double[] startPoint = point(DIM,1.0);

        double[][] boundaries = null;
        RealPointValuePair expected =
            new RealPointValuePair(point(DIM,0.0),0.0);
        doTest(new Rastrigin(), startPoint, boundaries,
                GoalType.MINIMIZE, 
                1e-13, 1e-6, 1000, expected);
    }

// org.apache.commons.math.optimization.direct.BOBYQAOptimizerTest::testConstrainedRosen
    public void testConstrainedRosen() {
        double[] startPoint = point(DIM,0.1);

        double[][] boundaries = boundaries(DIM,-1,2);
        RealPointValuePair expected =
            new RealPointValuePair(point(DIM,1.0),0.0);
        doTest(new Rosen(), startPoint, boundaries,
                GoalType.MINIMIZE,
                1e-13, 1e-6, 2000, expected);
    }

// org.apache.commons.math.optimization.direct.BOBYQAOptimizerTest::testConstrainedRosenWithMoreInterpolationPoints
    public void testConstrainedRosenWithMoreInterpolationPoints() {
        final double[] startPoint = point(DIM, 0.1);
        final double[][] boundaries = boundaries(DIM, -1, 2);
        final RealPointValuePair expected = new RealPointValuePair(point(DIM, 1.0), 0.0);

        
        
        
        
        
        final int maxAdditionalPoints = 47;

        for (int num = 1; num <= maxAdditionalPoints; num++) {
            doTest(new Rosen(), startPoint, boundaries,
                   GoalType.MINIMIZE,
                   1e-12, 1e-6, 2000,
                   num,
                   expected,
                   "num=" + num);
        }
    }
