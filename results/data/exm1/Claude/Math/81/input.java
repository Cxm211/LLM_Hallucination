// buggy code
    private void computeGershgorinCircles() {

        final int m     = main.length;
        final int lowerStart = 4 * m;
        final int upperStart = 5 * m;
        lowerSpectra = Double.POSITIVE_INFINITY;
        upperSpectra = Double.NEGATIVE_INFINITY;
        double eMax = 0;

        double eCurrent = 0;
        for (int i = 0; i < m - 1; ++i) {

            final double dCurrent = main[i];
            final double ePrevious = eCurrent;
            eCurrent = Math.abs(secondary[i]);
            eMax = Math.max(eMax, eCurrent);
            final double radius = ePrevious + eCurrent;

            final double lower = dCurrent - radius;
            work[lowerStart + i] = lower;
            lowerSpectra = Math.min(lowerSpectra, lower);

            final double upper = dCurrent + radius;
            work[upperStart + i] = upper;
            upperSpectra = Math.max(upperSpectra, upper);

        }

        final double dCurrent = main[m - 1];
        final double lower = dCurrent - eCurrent;
        work[lowerStart + m - 1] = lower;
        lowerSpectra = Math.min(lowerSpectra, lower);
        final double upper = dCurrent + eCurrent;
        work[upperStart + m - 1] = upper;
        minPivot = MathUtils.SAFE_MIN * Math.max(1.0, eMax * eMax);

    }

    private void processGeneralBlock(final int n)
        throws InvalidMatrixException {

        // check decomposed matrix data range
        double sumOffDiag = 0;
        for (int i = 0; i < n - 1; ++i) {
            final int fourI = 4 * i;
            final double ei = work[fourI + 2];
            sumOffDiag += ei;
        }

        if (sumOffDiag == 0) {
            // matrix is already diagonal
            return;
        }

        // initial checks for splits (see Parlett & Marques section 3.3)
        flipIfWarranted(n, 2);

        // two iterations with Li's test for initial splits
        initialSplits(n);

        // initialize parameters used by goodStep
        tType = 0;
        dMin1 = 0;
        dMin2 = 0;
        dN    = 0;
        dN1   = 0;
        dN2   = 0;
        tau   = 0;

        // process split segments
        int i0 = 0;
        int n0 = n;
        while (n0 > 0) {

            // retrieve shift that was temporarily stored as a negative off-diagonal element
            sigma    = (n0 == n) ? 0 : -work[4 * n0 - 2];
            sigmaLow = 0;

            // find start of a new split segment to process
            double offDiagMin = (i0 == n0) ? 0 : work[4 * n0 - 6];
            double offDiagMax = 0;
            double diagMax    = work[4 * n0 - 4];
            double diagMin    = diagMax;
            i0 = 0;
            for (int i = 4 * (n0 - 2); i >= 0; i -= 4) {
                if (work[i + 2] <= 0) {
                    i0 = 1 + i / 4;
                    break;
                }
                if (diagMin >= 4 * offDiagMax) {
                    diagMin    = Math.min(diagMin, work[i + 4]);
                    offDiagMax = Math.max(offDiagMax, work[i + 2]);
                }
                diagMax    = Math.max(diagMax, work[i] + work[i + 2]);
                offDiagMin = Math.min(offDiagMin, work[i + 2]);
            }
            work[4 * n0 - 2] = offDiagMin;

            // lower bound of Gershgorin disk
            dMin = -Math.max(0, diagMin - 2 * Math.sqrt(diagMin * offDiagMax));

            pingPong = 0;
            int maxIter = 30 * (n0 - i0);
            for (int k = 0; i0 < n0; ++k) {
                if (k >= maxIter) {
                    throw new InvalidMatrixException(new MaxIterationsExceededException(maxIter));
                }

                // perform one step
                n0 = goodStep(i0, n0);
                pingPong = 1 - pingPong;

                // check for new splits after "ping" steps
                // when the last elements of qd array are very small
                if ((pingPong == 0) && (n0 - i0 > 3) &&
                    (work[4 * n0 - 1] <= TOLERANCE_2 * diagMax) &&
                    (work[4 * n0 - 2] <= TOLERANCE_2 * sigma)) {
                    int split  = i0 - 1;
                    diagMax    = work[4 * i0];
                    offDiagMin = work[4 * i0 + 2];
                    double previousEMin = work[4 * i0 + 3];
                    for (int i = 4 * i0; i < 4 * n0 - 11; i += 4) {
                        if ((work[i + 3] <= TOLERANCE_2 * work[i]) &&
                            (work[i + 2] <= TOLERANCE_2 * sigma)) {
                            // insert a split
                            work[i + 2]  = -sigma;
                            split        = i / 4;
                            diagMax      = 0;
                            offDiagMin   = work[i + 6];
                            previousEMin = work[i + 7];
                        } else {
                            diagMax      = Math.max(diagMax, work[i + 4]);
                            offDiagMin   = Math.min(offDiagMin, work[i + 2]);
                            previousEMin = Math.min(previousEMin, work[i + 3]);
                        }
                    }
                    work[4 * n0 - 2] = offDiagMin;
                    work[4 * n0 - 1] = previousEMin;
                    i0 = split + 1;
                }
            }

        }

    }

    private void computeShiftIncrement(final int start, final int end, final int deflated) {

        final double cnst1 = 0.563;
        final double cnst2 = 1.010;
        final double cnst3 = 1.05;

        // a negative dMin forces the shift to take that absolute value
        // tType records the type of shift.
        if (dMin <= 0.0) {
            tau = -dMin;
            tType = -1;
            return;
        }

        int nn = 4 * end + pingPong - 1;
        switch (deflated) {

        case 0 : // no realEigenvalues deflated.
            if (dMin == dN || dMin == dN1) {

                double b1 = Math.sqrt(work[nn - 3]) * Math.sqrt(work[nn - 5]);
                double b2 = Math.sqrt(work[nn - 7]) * Math.sqrt(work[nn - 9]);
                double a2 = work[nn - 7] + work[nn - 5];

                if (dMin == dN && dMin1 == dN1) {
                    // cases 2 and 3.
                    final double gap2 = dMin2 - a2 - dMin2 * 0.25;
                    final double gap1 = a2 - dN - ((gap2 > 0.0 && gap2 > b2) ? (b2 / gap2) * b2 : (b1 + b2));
                    if (gap1 > 0.0 && gap1 > b1) {
                        tau   = Math.max(dN - (b1 / gap1) * b1, 0.5 * dMin);
                        tType = -2;
                    } else {
                        double s = 0.0;
                        if (dN > b1) {
                            s = dN - b1;
                        }
                        if (a2 > (b1 + b2)) {
                            s = Math.min(s, a2 - (b1 + b2));
                        }
                        tau   = Math.max(s, 0.333 * dMin);
                        tType = -3;
                    }
                } else {
                    // case 4.
                    tType = -4;
                    double s = 0.25 * dMin;
                    double gam;
                    int np;
                    if (dMin == dN) {
                        gam = dN;
                        a2 = 0.0;
                        if (work[nn - 5]  >  work[nn - 7]) {
                            return;
                        }
                        b2 = work[nn - 5] / work[nn - 7];
                        np = nn - 9;
                    } else {
                        np = nn - 2 * pingPong;
                        b2 = work[np - 2];
                        gam = dN1;
                        if (work[np - 4]  >  work[np - 2]) {
                            return;
                        }
                        a2 = work[np - 4] / work[np - 2];
                        if (work[nn - 9]  >  work[nn - 11]) {
                            return;
                        }
                        b2 = work[nn - 9] / work[nn - 11];
                        np = nn - 13;
                    }

                    // approximate contribution to norm squared from i < nn-1.
                    a2 = a2 + b2;
                    for (int i4 = np; i4 >= 4 * start + 2 + pingPong; i4 -= 4) {
                        if(b2 == 0.0) {
                            break;
                        }
                        b1 = b2;
                        if (work[i4]  >  work[i4 - 2]) {
                            return;
                        }
                        b2 = b2 * (work[i4] / work[i4 - 2]);
                        a2 = a2 + b2;
                        if (100 * Math.max(b2, b1) < a2 || cnst1 < a2) {
                            break;
                        }
                    }
                    a2 = cnst3 * a2;

                    // rayleigh quotient residual bound.
                    if (a2 < cnst1) {
                        s = gam * (1 - Math.sqrt(a2)) / (1 + a2);
                    }
                    tau = s;

                }
            } else if (dMin == dN2) {

                // case 5.
                tType = -5;
                double s = 0.25 * dMin;

                // compute contribution to norm squared from i > nn-2.
                final int np = nn - 2 * pingPong;
                double b1 = work[np - 2];
                double b2 = work[np - 6];
                final double gam = dN2;
                if (work[np - 8] > b2 || work[np - 4] > b1) {
                    return;
                }
                double a2 = (work[np - 8] / b2) * (1 + work[np - 4] / b1);

                // approximate contribution to norm squared from i < nn-2.
                if (end - start > 2) {
                    b2 = work[nn - 13] / work[nn - 15];
                    a2 = a2 + b2;
                    for (int i4 = nn - 17; i4 >= 4 * start + 2 + pingPong; i4 -= 4) {
                        if (b2 == 0.0) {
                            break;
                        }
                        b1 = b2;
                        if (work[i4]  >  work[i4 - 2]) {
                            return;
                        }
                        b2 = b2 * (work[i4] / work[i4 - 2]);
                        a2 = a2 + b2;
                        if (100 * Math.max(b2, b1) < a2 || cnst1 < a2)  {
                            break;
                        }
                    }
                    a2 = cnst3 * a2;
                }

                if (a2 < cnst1) {
                    tau = gam * (1 - Math.sqrt(a2)) / (1 + a2);
                } else {
                    tau = s;
                }

            } else {

                // case 6, no information to guide us.
                if (tType == -6) {
                    g += 0.333 * (1 - g);
                } else if (tType == -18) {
                    g = 0.25 * 0.333;
                } else {
                    g = 0.25;
                }
                tau   = g * dMin;
                tType = -6;

            }
            break;

        case 1 : // one eigenvalue just deflated. use dMin1, dN1 for dMin and dN.
            if (dMin1 == dN1 && dMin2 == dN2) {

                // cases 7 and 8.
                tType = -7;
                double s = 0.333 * dMin1;
                if (work[nn - 5] > work[nn - 7]) {
                    return;
                }
                double b1 = work[nn - 5] / work[nn - 7];
                double b2 = b1;
                if (b2 != 0.0) {
                    for (int i4 = 4 * end - 10 + pingPong; i4 >= 4 * start + 2 + pingPong; i4 -= 4) {
                        final double oldB1 = b1;
                        if (work[i4] > work[i4 - 2]) {
                            return;
                        }
                        b1 = b1 * (work[i4] / work[i4 - 2]);
                        b2 = b2 + b1;
                        if (100 * Math.max(b1, oldB1) < b2) {
                            break;
                        }
                    }
                }
                b2 = Math.sqrt(cnst3 * b2);
                final double a2 = dMin1 / (1 + b2 * b2);
                final double gap2 = 0.5 * dMin2 - a2;
                if (gap2 > 0.0 && gap2 > b2 * a2) {
                    tau = Math.max(s, a2 * (1 - cnst2 * a2 * (b2 / gap2) * b2));
                } else {
                    tau = Math.max(s, a2 * (1 - cnst2 * b2));
                    tType = -8;
                }
            } else {

                // case 9.
                tau = 0.25 * dMin1;
                if (dMin1 == dN1) {
                    tau = 0.5 * dMin1;
                }
                tType = -9;
            }
            break;

        case 2 : // two realEigenvalues deflated. use dMin2, dN2 for dMin and dN.

            // cases 10 and 11.
            if (dMin2 == dN2 && 2 * work[nn - 5] < work[nn - 7]) {
                tType = -10;
                final double s = 0.333 * dMin2;
                if (work[nn - 5] > work[nn - 7]) {
                    return;
                }
                double b1 = work[nn - 5] / work[nn - 7];
                double b2 = b1;
                if (b2 != 0.0){
                    for (int i4 = 4 * end - 9 + pingPong; i4 >= 4 * start + 2 + pingPong; i4 -= 4) {
                        if (work[i4] > work[i4 - 2]) {
                            return;
                        }
                        b1 *= work[i4] / work[i4 - 2];
                        b2 += b1;
                        if (100 * b1 < b2) {
                            break;
                        }
                    }
                }
                b2 = Math.sqrt(cnst3 * b2);
                final double a2 = dMin2 / (1 + b2 * b2);
                final double gap2 = work[nn - 7] + work[nn - 9] -
                Math.sqrt(work[nn - 11]) * Math.sqrt(work[nn - 9]) - a2;
                if (gap2 > 0.0 && gap2 > b2 * a2) {
                    tau = Math.max(s, a2 * (1 - cnst2 * a2 * (b2 / gap2) * b2));
                } else {
                    tau = Math.max(s, a2 * (1 - cnst2 * b2));
                }
            } else {
                tau   = 0.25 * dMin2;
                tType = -11;
            }
            break;

        default : // case 12, more than two realEigenvalues deflated. no information.
            tau   = 0.0;
            tType = -12;
        }

    }

// relevant test
// org.apache.commons.math.linear.EigenDecompositionImplTest::testDimension1
    public void testDimension1() {
        RealMatrix matrix =
            MatrixUtils.createRealMatrix(new double[][] { { 1.5 } });
        EigenDecomposition ed = new EigenDecompositionImpl(matrix, MathUtils.SAFE_MIN);
        assertEquals(1.5, ed.getRealEigenvalue(0), 1.0e-15);
    }

// org.apache.commons.math.linear.EigenDecompositionImplTest::testDimension2
    public void testDimension2() {
        RealMatrix matrix =
            MatrixUtils.createRealMatrix(new double[][] {
                    { 59.0, 12.0 },
                    { 12.0, 66.0 }
            });
        EigenDecomposition ed = new EigenDecompositionImpl(matrix, MathUtils.SAFE_MIN);
        assertEquals(75.0, ed.getRealEigenvalue(0), 1.0e-15);
        assertEquals(50.0, ed.getRealEigenvalue(1), 1.0e-15);
    }

// org.apache.commons.math.linear.EigenDecompositionImplTest::testDimension3
    public void testDimension3() {
        RealMatrix matrix =
            MatrixUtils.createRealMatrix(new double[][] {
                                   {  39632.0, -4824.0, -16560.0 },
                                   {  -4824.0,  8693.0,   7920.0 },
                                   { -16560.0,  7920.0,  17300.0 }
                               });
        EigenDecomposition ed = new EigenDecompositionImpl(matrix, MathUtils.SAFE_MIN);
        assertEquals(50000.0, ed.getRealEigenvalue(0), 3.0e-11);
        assertEquals(12500.0, ed.getRealEigenvalue(1), 3.0e-11);
        assertEquals( 3125.0, ed.getRealEigenvalue(2), 3.0e-11);
    }

// org.apache.commons.math.linear.EigenDecompositionImplTest::testDimension4WithSplit
    public void testDimension4WithSplit() {
        RealMatrix matrix =
            MatrixUtils.createRealMatrix(new double[][] {
                                   {  0.784, -0.288,  0.000,  0.000 },
                                   { -0.288,  0.616,  0.000,  0.000 },
                                   {  0.000,  0.000,  0.164, -0.048 },
                                   {  0.000,  0.000, -0.048,  0.136 }
                               });
        EigenDecomposition ed = new EigenDecompositionImpl(matrix, MathUtils.SAFE_MIN);
        assertEquals(1.0, ed.getRealEigenvalue(0), 1.0e-15);
        assertEquals(0.4, ed.getRealEigenvalue(1), 1.0e-15);
        assertEquals(0.2, ed.getRealEigenvalue(2), 1.0e-15);
        assertEquals(0.1, ed.getRealEigenvalue(3), 1.0e-15);
    }

// org.apache.commons.math.linear.EigenDecompositionImplTest::testDimension4WithoutSplit
    public void testDimension4WithoutSplit() {
        RealMatrix matrix =
            MatrixUtils.createRealMatrix(new double[][] {
                                   {  0.5608, -0.2016,  0.1152, -0.2976 },
                                   { -0.2016,  0.4432, -0.2304,  0.1152 },
                                   {  0.1152, -0.2304,  0.3088, -0.1344 },
                                   { -0.2976,  0.1152, -0.1344,  0.3872 }
                               });
        EigenDecomposition ed = new EigenDecompositionImpl(matrix, MathUtils.SAFE_MIN);
        assertEquals(1.0, ed.getRealEigenvalue(0), 1.0e-15);
        assertEquals(0.4, ed.getRealEigenvalue(1), 1.0e-15);
        assertEquals(0.2, ed.getRealEigenvalue(2), 1.0e-15);
        assertEquals(0.1, ed.getRealEigenvalue(3), 1.0e-15);
    }

// org.apache.commons.math.linear.EigenDecompositionImplTest::testMath308
    public void testMath308() {

        double[] mainTridiagonal = {
            22.330154644539597, 46.65485522478641, 17.393672330044705, 54.46687435351116, 80.17800767709437
        };
        double[] secondaryTridiagonal = {
            13.04450406501361, -5.977590941539671, 2.9040909856707517, 7.1570352792841225
        };

        
        
        double[] refEigenValues = {
            82.044413207204002, 53.456697699894512, 52.536278520113882, 18.847969733754262, 14.138204224043099
        };
        RealVector[] refEigenVectors = {
            new ArrayRealVector(new double[] { -0.000462690386766, -0.002118073109055,  0.011530080757413,  0.252322434584915,  0.967572088232592 }),
            new ArrayRealVector(new double[] {  0.314647769490148,  0.750806415553905, -0.167700312025760, -0.537092972407375,  0.143854968127780 }),
            new ArrayRealVector(new double[] {  0.222368839324646,  0.514921891363332, -0.021377019336614,  0.801196801016305, -0.207446991247740 }),
            new ArrayRealVector(new double[] {  0.713933751051495, -0.190582113553930,  0.671410443368332, -0.056056055955050,  0.006541576993581 }),
            new ArrayRealVector(new double[] {  0.584677060845929, -0.367177264979103, -0.721453187784497,  0.052971054621812, -0.005740715188257 })
        };

        EigenDecomposition decomposition =
            new EigenDecompositionImpl(mainTridiagonal, secondaryTridiagonal, MathUtils.SAFE_MIN);

        double[] eigenValues = decomposition.getRealEigenvalues();
        for (int i = 0; i < refEigenValues.length; ++i) {
            assertEquals(refEigenValues[i], eigenValues[i], 1.0e-5);
            assertEquals(0, refEigenVectors[i].subtract(decomposition.getEigenvector(i)).getNorm(), 2.0e-7);
        }

    }

// org.apache.commons.math.linear.EigenDecompositionImplTest::testTridiagonal
    public void testTridiagonal() {
        Random r = new Random(4366663527842l);
        double[] ref = new double[30];
        for (int i = 0; i < ref.length; ++i) {
            if (i < 5) {
                ref[i] = 2 * r.nextDouble() - 1;
            } else {
                ref[i] = 0.0001 * r.nextDouble() + 6;
            }
        }
        Arrays.sort(ref);
        TriDiagonalTransformer t =
            new TriDiagonalTransformer(createTestMatrix(r, ref));
        EigenDecomposition ed =
            new EigenDecompositionImpl(t.getMainDiagonalRef(),
                                       t.getSecondaryDiagonalRef(),
                                       MathUtils.SAFE_MIN);
        double[] eigenValues = ed.getRealEigenvalues();
        assertEquals(ref.length, eigenValues.length);
        for (int i = 0; i < ref.length; ++i) {
            assertEquals(ref[ref.length - i - 1], eigenValues[i], 2.0e-14);
        }

    }

// org.apache.commons.math.linear.EigenDecompositionImplTest::testDimensions
    public void testDimensions() {
        final int m = matrix.getRowDimension();
        EigenDecomposition ed = new EigenDecompositionImpl(matrix, MathUtils.SAFE_MIN);
        assertEquals(m, ed.getV().getRowDimension());
        assertEquals(m, ed.getV().getColumnDimension());
        assertEquals(m, ed.getD().getColumnDimension());
        assertEquals(m, ed.getD().getColumnDimension());
        assertEquals(m, ed.getVT().getRowDimension());
        assertEquals(m, ed.getVT().getColumnDimension());
    }

// org.apache.commons.math.linear.EigenDecompositionImplTest::testEigenvalues
    public void testEigenvalues() {
        EigenDecomposition ed = new EigenDecompositionImpl(matrix, MathUtils.SAFE_MIN);
        double[] eigenValues = ed.getRealEigenvalues();
        assertEquals(refValues.length, eigenValues.length);
        for (int i = 0; i < refValues.length; ++i) {
            assertEquals(refValues[i], eigenValues[i], 3.0e-15);
        }
    }

// org.apache.commons.math.linear.EigenDecompositionImplTest::testBigMatrix
    public void testBigMatrix() {
        Random r = new Random(17748333525117l);
        double[] bigValues = new double[200];
        for (int i = 0; i < bigValues.length; ++i) {
            bigValues[i] = 2 * r.nextDouble() - 1;
        }
        Arrays.sort(bigValues);
        EigenDecomposition ed =
            new EigenDecompositionImpl(createTestMatrix(r, bigValues), MathUtils.SAFE_MIN);
        double[] eigenValues = ed.getRealEigenvalues();
        assertEquals(bigValues.length, eigenValues.length);
        for (int i = 0; i < bigValues.length; ++i) {
            assertEquals(bigValues[bigValues.length - i - 1], eigenValues[i], 2.0e-14);
        }
    }

// org.apache.commons.math.linear.EigenDecompositionImplTest::testEigenvectors
    public void testEigenvectors() {
        EigenDecomposition ed = new EigenDecompositionImpl(matrix, MathUtils.SAFE_MIN);
        for (int i = 0; i < matrix.getRowDimension(); ++i) {
            double lambda = ed.getRealEigenvalue(i);
            RealVector v  = ed.getEigenvector(i);
            RealVector mV = matrix.operate(v);
            assertEquals(0, mV.subtract(v.mapMultiplyToSelf(lambda)).getNorm(), 1.0e-13);
        }
    }

// org.apache.commons.math.linear.EigenDecompositionImplTest::testAEqualVDVt
    public void testAEqualVDVt() {
        EigenDecomposition ed = new EigenDecompositionImpl(matrix, MathUtils.SAFE_MIN);
        RealMatrix v  = ed.getV();
        RealMatrix d  = ed.getD();
        RealMatrix vT = ed.getVT();
        double norm = v.multiply(d).multiply(vT).subtract(matrix).getNorm();
        assertEquals(0, norm, 6.0e-13);
    }

// org.apache.commons.math.linear.EigenDecompositionImplTest::testVOrthogonal
    public void testVOrthogonal() {
        RealMatrix v = new EigenDecompositionImpl(matrix, MathUtils.SAFE_MIN).getV();
        RealMatrix vTv = v.transpose().multiply(v);
        RealMatrix id  = MatrixUtils.createRealIdentityMatrix(vTv.getRowDimension());
        assertEquals(0, vTv.subtract(id).getNorm(), 2.0e-13);
    }

// org.apache.commons.math.linear.EigenDecompositionImplTest::testDiagonal
    public void testDiagonal() {
        double[] diagonal = new double[] { -3.0, -2.0, 2.0, 5.0 };
        RealMatrix m = createDiagonalMatrix(diagonal, diagonal.length, diagonal.length);
        EigenDecomposition ed = new EigenDecompositionImpl(m, MathUtils.SAFE_MIN);
        assertEquals(diagonal[0], ed.getRealEigenvalue(3), 2.0e-15);
        assertEquals(diagonal[1], ed.getRealEigenvalue(2), 2.0e-15);
        assertEquals(diagonal[2], ed.getRealEigenvalue(1), 2.0e-15);
        assertEquals(diagonal[3], ed.getRealEigenvalue(0), 2.0e-15);
    }

// org.apache.commons.math.linear.EigenDecompositionImplTest::testRepeatedEigenvalue
    public void testRepeatedEigenvalue() {
        RealMatrix repeated = MatrixUtils.createRealMatrix(new double[][] {
                {3,  2,  4},
                {2,  0,  2},
                {4,  2,  3}
        });
        EigenDecomposition ed = new EigenDecompositionImpl(repeated, MathUtils.SAFE_MIN);
        checkEigenValues((new double[] {8, -1, -1}), ed, 1E-12);
        checkEigenVector((new double[] {2, 1, 2}), ed, 1E-12);
    }

// org.apache.commons.math.linear.EigenDecompositionImplTest::testDistinctEigenvalues
    public void testDistinctEigenvalues() {
        RealMatrix distinct = MatrixUtils.createRealMatrix(new double[][] {
                {3, 1, -4},
                {1, 3, -4},
                {-4, -4, 8}
        });
        EigenDecomposition ed = new EigenDecompositionImpl(distinct, MathUtils.SAFE_MIN);
        checkEigenValues((new double[] {2, 0, 12}), ed, 1E-12);
        checkEigenVector((new double[] {1, -1, 0}), ed, 1E-12);
        checkEigenVector((new double[] {1, 1, 1}), ed, 1E-12);
        checkEigenVector((new double[] {-1, -1, 2}), ed, 1E-12);
    }

// org.apache.commons.math.linear.EigenDecompositionImplTest::testZeroDivide
    public void testZeroDivide() {
        RealMatrix indefinite = MatrixUtils.createRealMatrix(new double [][] {
                { 0.0, 1.0, -1.0 }, 
                { 1.0, 1.0, 0.0 }, 
                { -1.0,0.0, 1.0 }        
        });
        EigenDecomposition ed = new EigenDecompositionImpl(indefinite, MathUtils.SAFE_MIN);
        checkEigenValues((new double[] {2, 1, -1}), ed, 1E-12);
        double isqrt3 = 1/Math.sqrt(3.0);
        checkEigenVector((new double[] {isqrt3,isqrt3,-isqrt3}), ed, 1E-12);
        double isqrt2 = 1/Math.sqrt(2.0);
        checkEigenVector((new double[] {0.0,-isqrt2,-isqrt2}), ed, 1E-12);
        double isqrt6 = 1/Math.sqrt(6.0);
        checkEigenVector((new double[] {2*isqrt6,-isqrt6,isqrt6}), ed, 1E-12);
    }

// org.apache.commons.math.linear.EigenSolverTest::testNonInvertible
    public void testNonInvertible() {
        Random r = new Random(9994100315209l);
        RealMatrix m =
            EigenDecompositionImplTest.createTestMatrix(r, new double[] { 1.0, 0.0, -1.0, -2.0, -3.0 });
        DecompositionSolver es = new EigenDecompositionImpl(m, MathUtils.SAFE_MIN).getSolver();
        assertFalse(es.isNonSingular());
        try {
            es.getInverse();
            fail("an exception should have been thrown");
        } catch (InvalidMatrixException ime) {
            
        } catch (Exception e) {
            fail("wrong exception caught");
        }
    }

// org.apache.commons.math.linear.EigenSolverTest::testInvertible
    public void testInvertible() {
        Random r = new Random(9994100315209l);
        RealMatrix m =
            EigenDecompositionImplTest.createTestMatrix(r, new double[] { 1.0, 0.5, -1.0, -2.0, -3.0 });
        DecompositionSolver es = new EigenDecompositionImpl(m, MathUtils.SAFE_MIN).getSolver();
        assertTrue(es.isNonSingular());
        RealMatrix inverse = es.getInverse();
        RealMatrix error =
            m.multiply(inverse).subtract(MatrixUtils.createRealIdentityMatrix(m.getRowDimension()));
        assertEquals(0, error.getNorm(), 4.0e-15);
    }

// org.apache.commons.math.linear.EigenSolverTest::testSolveDimensionErrors
    public void testSolveDimensionErrors() {
        DecompositionSolver es = new EigenDecompositionImpl(matrix, MathUtils.SAFE_MIN).getSolver();
        RealMatrix b = MatrixUtils.createRealMatrix(new double[2][2]);
        try {
            es.solve(b);
            fail("an exception should have been thrown");
        } catch (IllegalArgumentException iae) {
            
        } catch (Exception e) {
            fail("wrong exception caught");
        }
        try {
            es.solve(b.getColumn(0));
            fail("an exception should have been thrown");
        } catch (IllegalArgumentException iae) {
            
        } catch (Exception e) {
            fail("wrong exception caught");
        }
        try {
            es.solve(new ArrayRealVectorTest.RealVectorTestImpl(b.getColumn(0)));
            fail("an exception should have been thrown");
        } catch (IllegalArgumentException iae) {
            
        } catch (Exception e) {
            fail("wrong exception caught");
        }
    }

// org.apache.commons.math.linear.EigenSolverTest::testSolve
    public void testSolve() {
        RealMatrix m = MatrixUtils.createRealMatrix(new double[][] {
                { 91,  5, 29, 32, 40, 14 },
                {  5, 34, -1,  0,  2, -1 },
                { 29, -1, 12,  9, 21,  8 },
                { 32,  0,  9, 14,  9,  0 },
                { 40,  2, 21,  9, 51, 19 },
                { 14, -1,  8,  0, 19, 14 }
        });
        DecompositionSolver es = new EigenDecompositionImpl(m, MathUtils.SAFE_MIN).getSolver();
        RealMatrix b = MatrixUtils.createRealMatrix(new double[][] {
                { 1561, 269, 188 },
                {   69, -21,  70 },
                {  739, 108,  63 },
                {  324,  86,  59 },
                { 1624, 194, 107 },
                {  796,  69,  36 }
        });
        RealMatrix xRef = MatrixUtils.createRealMatrix(new double[][] {
                { 1,   2, 1 },
                { 2,  -1, 2 },
                { 4,   2, 3 },
                { 8,  -1, 0 },
                { 16,  2, 0 },
                { 32, -1, 0 }
        });

        
        assertEquals(0, es.solve(b).subtract(xRef).getNorm(), 2.0e-12);

        
        for (int i = 0; i < b.getColumnDimension(); ++i) {
            assertEquals(0,
                         new ArrayRealVector(es.solve(b.getColumn(i))).subtract(xRef.getColumnVector(i)).getNorm(),
                         2.0e-11);
        }

        
        for (int i = 0; i < b.getColumnDimension(); ++i) {
            assertEquals(0,
                         es.solve(b.getColumnVector(i)).subtract(xRef.getColumnVector(i)).getNorm(),
                         2.0e-11);
        }

        
        for (int i = 0; i < b.getColumnDimension(); ++i) {
            ArrayRealVectorTest.RealVectorTestImpl v =
                new ArrayRealVectorTest.RealVectorTestImpl(b.getColumn(i));
            assertEquals(0,
                         es.solve(v).subtract(xRef.getColumnVector(i)).getNorm(),
                         2.0e-11);
        }

    }

// org.apache.commons.math.linear.SingularValueDecompositionImplTest::testMoreRows
    public void testMoreRows() {
        final double[] singularValues = { 123.456, 2.3, 1.001, 0.999 };
        final int rows    = singularValues.length + 2;
        final int columns = singularValues.length;
        Random r = new Random(15338437322523l);
        SingularValueDecomposition svd =
            new SingularValueDecompositionImpl(createTestMatrix(r, rows, columns, singularValues));
        double[] computedSV = svd.getSingularValues();
        assertEquals(singularValues.length, computedSV.length);
        for (int i = 0; i < singularValues.length; ++i) {
            assertEquals(singularValues[i], computedSV[i], 1.0e-10);
        }
    }

// org.apache.commons.math.linear.SingularValueDecompositionImplTest::testMoreColumns
    public void testMoreColumns() {
        final double[] singularValues = { 123.456, 2.3, 1.001, 0.999 };
        final int rows    = singularValues.length;
        final int columns = singularValues.length + 2;
        Random r = new Random(732763225836210l);
        SingularValueDecomposition svd =
            new SingularValueDecompositionImpl(createTestMatrix(r, rows, columns, singularValues));
        double[] computedSV = svd.getSingularValues();
        assertEquals(singularValues.length, computedSV.length);
        for (int i = 0; i < singularValues.length; ++i) {
            assertEquals(singularValues[i], computedSV[i], 1.0e-10);
        }
    }

// org.apache.commons.math.linear.SingularValueDecompositionImplTest::testDimensions
    public void testDimensions() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(testSquare);
        final int m = matrix.getRowDimension();
        final int n = matrix.getColumnDimension();
        SingularValueDecomposition svd = new SingularValueDecompositionImpl(matrix);
        assertEquals(m, svd.getU().getRowDimension());
        assertEquals(m, svd.getU().getColumnDimension());
        assertEquals(m, svd.getS().getColumnDimension());
        assertEquals(n, svd.getS().getColumnDimension());
        assertEquals(n, svd.getV().getRowDimension());
        assertEquals(n, svd.getV().getColumnDimension());

    }

// org.apache.commons.math.linear.SingularValueDecompositionImplTest::testHadamard
    public void testHadamard() {
        RealMatrix matrix = new Array2DRowRealMatrix(new double[][] {
                {15.0 / 2.0,  5.0 / 2.0,  9.0 / 2.0,  3.0 / 2.0 },
                { 5.0 / 2.0, 15.0 / 2.0,  3.0 / 2.0,  9.0 / 2.0 },
                { 9.0 / 2.0,  3.0 / 2.0, 15.0 / 2.0,  5.0 / 2.0 },
                { 3.0 / 2.0,  9.0 / 2.0,  5.0 / 2.0, 15.0 / 2.0 }
        }, false);
        SingularValueDecomposition svd = new SingularValueDecompositionImpl(matrix);
        assertEquals(16.0, svd.getSingularValues()[0], 1.0e-14);
        assertEquals( 8.0, svd.getSingularValues()[1], 1.0e-14);
        assertEquals( 4.0, svd.getSingularValues()[2], 1.0e-14);
        assertEquals( 2.0, svd.getSingularValues()[3], 1.0e-14);

        RealMatrix fullCovariance = new Array2DRowRealMatrix(new double[][] {
                {  85.0 / 1024, -51.0 / 1024, -75.0 / 1024,  45.0 / 1024 },
                { -51.0 / 1024,  85.0 / 1024,  45.0 / 1024, -75.0 / 1024 },
                { -75.0 / 1024,  45.0 / 1024,  85.0 / 1024, -51.0 / 1024 },
                {  45.0 / 1024, -75.0 / 1024, -51.0 / 1024,  85.0 / 1024 }
        }, false);
        assertEquals(0.0,
                     fullCovariance.subtract(svd.getCovariance(0.0)).getNorm(),
                     1.0e-14);

        RealMatrix halfCovariance = new Array2DRowRealMatrix(new double[][] {
                {   5.0 / 1024,  -3.0 / 1024,   5.0 / 1024,  -3.0 / 1024 },
                {  -3.0 / 1024,   5.0 / 1024,  -3.0 / 1024,   5.0 / 1024 },
                {   5.0 / 1024,  -3.0 / 1024,   5.0 / 1024,  -3.0 / 1024 },
                {  -3.0 / 1024,   5.0 / 1024,  -3.0 / 1024,   5.0 / 1024 }
        }, false);
        assertEquals(0.0,
                     halfCovariance.subtract(svd.getCovariance(6.0)).getNorm(),
                     1.0e-14);

    }

// org.apache.commons.math.linear.SingularValueDecompositionImplTest::testAEqualUSVt
    public void testAEqualUSVt() {
        checkAEqualUSVt(MatrixUtils.createRealMatrix(testSquare));
        checkAEqualUSVt(MatrixUtils.createRealMatrix(testNonSquare));
        checkAEqualUSVt(MatrixUtils.createRealMatrix(testNonSquare).transpose());
    }

// org.apache.commons.math.linear.SingularValueDecompositionImplTest::testUOrthogonal
    public void testUOrthogonal() {
        checkOrthogonal(new SingularValueDecompositionImpl(MatrixUtils.createRealMatrix(testSquare)).getU());
        checkOrthogonal(new SingularValueDecompositionImpl(MatrixUtils.createRealMatrix(testNonSquare)).getU());
        checkOrthogonal(new SingularValueDecompositionImpl(MatrixUtils.createRealMatrix(testNonSquare).transpose()).getU());
    }

// org.apache.commons.math.linear.SingularValueDecompositionImplTest::testVOrthogonal
    public void testVOrthogonal() {
        checkOrthogonal(new SingularValueDecompositionImpl(MatrixUtils.createRealMatrix(testSquare)).getV());
        checkOrthogonal(new SingularValueDecompositionImpl(MatrixUtils.createRealMatrix(testNonSquare)).getV());
        checkOrthogonal(new SingularValueDecompositionImpl(MatrixUtils.createRealMatrix(testNonSquare).transpose()).getV());
    }

// org.apache.commons.math.linear.SingularValueDecompositionImplTest::testMatricesValues1
    public void testMatricesValues1() {
       SingularValueDecomposition svd =
            new SingularValueDecompositionImpl(MatrixUtils.createRealMatrix(testSquare));
        RealMatrix uRef = MatrixUtils.createRealMatrix(new double[][] {
                { 3.0 / 5.0, -4.0 / 5.0 },
                { 4.0 / 5.0,  3.0 / 5.0 }
        });
        RealMatrix sRef = MatrixUtils.createRealMatrix(new double[][] {
                { 3.0, 0.0 },
                { 0.0, 1.0 }
        });
        RealMatrix vRef = MatrixUtils.createRealMatrix(new double[][] {
                { 4.0 / 5.0,  3.0 / 5.0 },
                { 3.0 / 5.0, -4.0 / 5.0 }
        });

        
        RealMatrix u = svd.getU();
        assertEquals(0, u.subtract(uRef).getNorm(), normTolerance);
        RealMatrix s = svd.getS();
        assertEquals(0, s.subtract(sRef).getNorm(), normTolerance);
        RealMatrix v = svd.getV();
        assertEquals(0, v.subtract(vRef).getNorm(), normTolerance);

        
        assertTrue(u == svd.getU());
        assertTrue(s == svd.getS());
        assertTrue(v == svd.getV());

    }

// org.apache.commons.math.linear.SingularValueDecompositionImplTest::testMatricesValues2
    public void testMatricesValues2() {

        RealMatrix uRef = MatrixUtils.createRealMatrix(new double[][] {
            {  0.0 / 5.0,  3.0 / 5.0,  0.0 / 5.0 },
            { -4.0 / 5.0,  0.0 / 5.0, -3.0 / 5.0 },
            {  0.0 / 5.0,  4.0 / 5.0,  0.0 / 5.0 },
            { -3.0 / 5.0,  0.0 / 5.0,  4.0 / 5.0 }
        });
        RealMatrix sRef = MatrixUtils.createRealMatrix(new double[][] {
            { 4.0, 0.0, 0.0 },
            { 0.0, 3.0, 0.0 },
            { 0.0, 0.0, 2.0 }
        });
        RealMatrix vRef = MatrixUtils.createRealMatrix(new double[][] {
            {  80.0 / 125.0,  -60.0 / 125.0, 75.0 / 125.0 },
            {  24.0 / 125.0,  107.0 / 125.0, 60.0 / 125.0 },
            { -93.0 / 125.0,  -24.0 / 125.0, 80.0 / 125.0 }
        });

        
        SingularValueDecomposition svd =
            new SingularValueDecompositionImpl(MatrixUtils.createRealMatrix(testNonSquare));
        RealMatrix u = svd.getU();
        assertEquals(0, u.subtract(uRef).getNorm(), normTolerance);
        RealMatrix s = svd.getS();
        assertEquals(0, s.subtract(sRef).getNorm(), normTolerance);
        RealMatrix v = svd.getV();
        assertEquals(0, v.subtract(vRef).getNorm(), normTolerance);

        
        assertTrue(u == svd.getU());
        assertTrue(s == svd.getS());
        assertTrue(v == svd.getV());

    }

// org.apache.commons.math.linear.SingularValueDecompositionImplTest::testConditionNumber
    public void testConditionNumber() {
        SingularValueDecompositionImpl svd =
            new SingularValueDecompositionImpl(MatrixUtils.createRealMatrix(testSquare));
        assertEquals(3.0, svd.getConditionNumber(), 1.0e-15);
    }

// org.apache.commons.math.linear.SingularValueSolverTest::testSolveDimensionErrors
    public void testSolveDimensionErrors() {
        DecompositionSolver solver =
            new SingularValueDecompositionImpl(MatrixUtils.createRealMatrix(testSquare)).getSolver();
        RealMatrix b = MatrixUtils.createRealMatrix(new double[3][2]);
        try {
            solver.solve(b);
            fail("an exception should have been thrown");
        } catch (IllegalArgumentException iae) {
            
        } catch (Exception e) {
            fail("wrong exception caught");
        }
        try {
            solver.solve(b.getColumn(0));
            fail("an exception should have been thrown");
        } catch (IllegalArgumentException iae) {
            
        } catch (Exception e) {
            fail("wrong exception caught");
        }
        try {
            solver.solve(new ArrayRealVectorTest.RealVectorTestImpl(b.getColumn(0)));
            fail("an exception should have been thrown");
        } catch (IllegalArgumentException iae) {
            
        } catch (Exception e) {
            fail("wrong exception caught");
        }
    }

// org.apache.commons.math.linear.SingularValueSolverTest::testSolveSingularityErrors
    public void testSolveSingularityErrors() {
        RealMatrix m =
            MatrixUtils.createRealMatrix(new double[][] {
                                   { 1.0, 0.0 },
                                   { 0.0, 0.0 }
                               });
        DecompositionSolver solver = new SingularValueDecompositionImpl(m).getSolver();
        RealMatrix b = MatrixUtils.createRealMatrix(new double[2][2]);
        try {
            solver.solve(b);
            fail("an exception should have been thrown");
        } catch (InvalidMatrixException ime) {
            
        } catch (Exception e) {
            fail("wrong exception caught");
        }
        try {
            solver.solve(b.getColumn(0));
            fail("an exception should have been thrown");
        } catch (InvalidMatrixException ime) {
            
        } catch (Exception e) {
            fail("wrong exception caught");
        }
        try {
            solver.solve(b.getColumnVector(0));
            fail("an exception should have been thrown");
        } catch (InvalidMatrixException ime) {
            
        } catch (Exception e) {
            fail("wrong exception caught");
        }
        try {
            solver.solve(new ArrayRealVectorTest.RealVectorTestImpl(b.getColumn(0)));
            fail("an exception should have been thrown");
        } catch (InvalidMatrixException ime) {
            
        } catch (Exception e) {
            fail("wrong exception caught");
        }
    }

// org.apache.commons.math.linear.SingularValueSolverTest::testSolve
    public void testSolve() {
        DecompositionSolver solver =
            new SingularValueDecompositionImpl(MatrixUtils.createRealMatrix(testSquare)).getSolver();
        RealMatrix b = MatrixUtils.createRealMatrix(new double[][] {
                { 1, 2, 3 }, { 0, -5, 1 }
        });
        RealMatrix xRef = MatrixUtils.createRealMatrix(new double[][] {
                { -8.0 / 25.0, -263.0 / 75.0, -29.0 / 75.0 },
                { 19.0 / 25.0,   78.0 / 25.0,  49.0 / 25.0 }
        });

        
        assertEquals(0, solver.solve(b).subtract(xRef).getNorm(), normTolerance);

        
        for (int i = 0; i < b.getColumnDimension(); ++i) {
            assertEquals(0,
                         new ArrayRealVector(solver.solve(b.getColumn(i))).subtract(xRef.getColumnVector(i)).getNorm(),
                         1.0e-13);
        }

        
        for (int i = 0; i < b.getColumnDimension(); ++i) {
            assertEquals(0,
                         solver.solve(b.getColumnVector(i)).subtract(xRef.getColumnVector(i)).getNorm(),
                         1.0e-13);
        }

        
        for (int i = 0; i < b.getColumnDimension(); ++i) {
            ArrayRealVectorTest.RealVectorTestImpl v =
                new ArrayRealVectorTest.RealVectorTestImpl(b.getColumn(i));
            assertEquals(0,
                         solver.solve(v).subtract(xRef.getColumnVector(i)).getNorm(),
                         1.0e-13);
        }

    }

// org.apache.commons.math.linear.SingularValueSolverTest::testConditionNumber
    public void testConditionNumber() {
        SingularValueDecompositionImpl svd =
            new SingularValueDecompositionImpl(MatrixUtils.createRealMatrix(testSquare));
        assertEquals(3.0, svd.getConditionNumber(), 1.0e-15);
    }
