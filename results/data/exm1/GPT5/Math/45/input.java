// buggy code
    public OpenMapRealMatrix(int rowDimension, int columnDimension) {
        super(rowDimension, columnDimension);
        this.rows = rowDimension;
        this.columns = columnDimension;
        this.entries = new OpenIntToDoubleHashMap(0.0);
    }

// relevant test
// org.apache.commons.math.analysis.interpolation.MicrosphereInterpolatorTest::testLinearFunction2D
    public void testLinearFunction2D() {
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
        Assert.assertEquals("On sample point", expected, result, FastMath.ulp(1d));

        c[0] = 0 + 1e-5;
        c[1] = 1 - 1e-5;
        expected = f.value(c);
        result = p.value(c);
        Assert.assertEquals("1e-5 away from sample point", expected, result, 1e-4);
    }

// org.apache.commons.math.analysis.interpolation.MicrosphereInterpolatorTest::testParaboloid2D
    public void testParaboloid2D() {
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
        Assert.assertEquals("On sample point", expected, result, FastMath.ulp(1d));

        c[0] = 2 + 1e-5;
        c[1] = 2 - 1e-5;
        expected = f.value(c);
        result = p.value(c);
        Assert.assertEquals("1e-5 away from sample point", expected, result, 1e-3);
    }

// org.apache.commons.math.analysis.interpolation.SmoothingPolynomialBicubicSplineInterpolatorTest::testPreconditions
    public void testPreconditions() {
        double[] xval = new double[] {3, 4, 5, 6.5};
        double[] yval = new double[] {-4, -3, -1, 2.5};
        double[][] zval = new double[xval.length][yval.length];

        BivariateRealGridInterpolator interpolator = new SmoothingPolynomialBicubicSplineInterpolator(0);
        
        @SuppressWarnings("unused")
        BivariateRealFunction p = interpolator.interpolate(xval, yval, zval);
        
        double[] wxval = new double[] {3, 2, 5, 6.5};
        try {
            p = interpolator.interpolate(wxval, yval, zval);
            Assert.fail("an exception should have been thrown");
        } catch (MathIllegalArgumentException e) {
            
        }

        double[] wyval = new double[] {-4, -3, -1, -1};
        try {
            p = interpolator.interpolate(xval, wyval, zval);
            Assert.fail("an exception should have been thrown");
        } catch (MathIllegalArgumentException e) {
            
        }

        double[][] wzval = new double[xval.length][yval.length + 1];
        try {
            p = interpolator.interpolate(xval, yval, wzval);
            Assert.fail("an exception should have been thrown");
        } catch (DimensionMismatchException e) {
            
        }
        wzval = new double[xval.length - 1][yval.length];
        try {
            p = interpolator.interpolate(xval, yval, wzval);
            Assert.fail("an exception should have been thrown");
        } catch (DimensionMismatchException e) {
            
        }
        wzval = new double[xval.length][yval.length - 1];
        try {
            p = interpolator.interpolate(xval, yval, wzval);
            Assert.fail("an exception should have been thrown");
        } catch (DimensionMismatchException e) {
            
        }
    }

// org.apache.commons.math.analysis.interpolation.SmoothingPolynomialBicubicSplineInterpolatorTest::testPlane
    public void testPlane() {
        BivariateRealFunction f = new BivariateRealFunction() {
                public double value(double x, double y) {
                    return 2 * x - 3 * y + 5
                        + ((int) (FastMath.abs(5 * x + 3 * y)) % 2 == 0 ? 1 : -1);
                }
            };

        BivariateRealGridInterpolator interpolator = new SmoothingPolynomialBicubicSplineInterpolator(1);

        double[] xval = new double[] {3, 4, 5, 6.5};
        double[] yval = new double[] {-4, -3, -1, 2, 2.5};
        double[][] zval = new double[xval.length][yval.length];
        for (int i = 0; i < xval.length; i++) {
            for (int j = 0; j < yval.length; j++) {
                zval[i][j] = f.value(xval[i], yval[j]);
            }
        }

        BivariateRealFunction p = interpolator.interpolate(xval, yval, zval);
        double x, y;
        double expected, result;
        
        x = 4;
        y = -3;
        expected = f.value(x, y);
        result = p.value(x, y);
        Assert.assertEquals("On sample point", expected, result, 2);

        x = 4.5;
        y = -1.5;
        expected = f.value(x, y);
        result = p.value(x, y);
        Assert.assertEquals("half-way between sample points (middle of the patch)", expected, result, 2);

        x = 3.5;
        y = -3.5;
        expected = f.value(x, y);
        result = p.value(x, y);
        Assert.assertEquals("half-way between sample points (border of the patch)", expected, result, 2);
    }

// org.apache.commons.math.analysis.interpolation.SmoothingPolynomialBicubicSplineInterpolatorTest::testParaboloid
    public void testParaboloid() {
        BivariateRealFunction f = new BivariateRealFunction() {
                public double value(double x, double y) {
                    return 2 * x * x - 3 * y * y + 4 * x * y - 5
                        + ((int) (FastMath.abs(5 * x + 3 * y)) % 2 == 0 ? 1 : -1);
                }
            };

        BivariateRealGridInterpolator interpolator = new SmoothingPolynomialBicubicSplineInterpolator(4);

        double[] xval = new double[] {3, 4, 5, 6.5};
        double[] yval = new double[] {-4, -3, -2, -1, 0.5, 2.5};
        double[][] zval = new double[xval.length][yval.length];
        for (int i = 0; i < xval.length; i++) {
            for (int j = 0; j < yval.length; j++) {
                zval[i][j] = f.value(xval[i], yval[j]);
            }
        }

        BivariateRealFunction p = interpolator.interpolate(xval, yval, zval);
        double x, y;
        double expected, result;

        x = 5;
        y = 0.5;
        expected = f.value(x, y);
        result = p.value(x, y);
        Assert.assertEquals("On sample point", expected, result, 2);

        x = 4.5;
        y = -1.5;
        expected = f.value(x, y);
        result = p.value(x, y);
        Assert.assertEquals("half-way between sample points (middle of the patch)", expected, result, 2);

        x = 3.5;
        y = -3.5;
        expected = f.value(x, y);
        result = p.value(x, y);
        Assert.assertEquals("half-way between sample points (border of the patch)", expected, result, 2);
    }

// org.apache.commons.math.filter.KalmanFilterTest::testConstant
    public void testConstant() {
        double constantValue = 10d;
        double measurementNoise = 0.1d;
        double processNoise = 1e-5d;

        
        RealMatrix A = new Array2DRowRealMatrix(new double[] { 1d });
        
        RealMatrix B = null;
        
        RealMatrix H = new Array2DRowRealMatrix(new double[] { 1d });
        
        RealVector x = new ArrayRealVector(new double[] { constantValue });
        
        RealMatrix Q = new Array2DRowRealMatrix(new double[] { processNoise });
        
        RealMatrix R = new Array2DRowRealMatrix(new double[] { measurementNoise });

        ProcessModel pm
            = new DefaultProcessModel(A, B, Q,
                                      new ArrayRealVector(new double[] { constantValue }), null);
        MeasurementModel mm = new DefaultMeasurementModel(H, R);
        KalmanFilter filter = new KalmanFilter(pm, mm);

        Assert.assertEquals(1, filter.getMeasurementDimension());
        Assert.assertEquals(1, filter.getStateDimension());

        assertMatrixEquals(Q.getData(), filter.getErrorCovariance());

        
        double[] expectedInitialState = new double[] { constantValue };
        assertVectorEquals(expectedInitialState, filter.getStateEstimation());

        RealVector pNoise = new ArrayRealVector(1);
        RealVector mNoise = new ArrayRealVector(1);

        RandomGenerator rand = new JDKRandomGenerator();
        
        for (int i = 0; i < 60; i++) {
            filter.predict();

            
            pNoise.setEntry(0, processNoise * rand.nextGaussian());

            
            x = A.operate(x).add(pNoise);

            
            mNoise.setEntry(0, measurementNoise * rand.nextGaussian());

            
            RealVector z = H.operate(x).add(mNoise);

            filter.correct(z);

            
            double diff = Math.abs(constantValue - filter.getStateEstimation()[0]);
            
            Assert.assertTrue(MathUtils.compareTo(diff, measurementNoise, 1e-6) < 0);
        }

        
        Assert.assertTrue(MathUtils.compareTo(filter.getErrorCovariance()[0][0],
                                              0.02d, 1e-6) < 0);
    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testDimensions
    public void testDimensions() {
        Array2DRowRealMatrix m = new Array2DRowRealMatrix(testData);
        Array2DRowRealMatrix m2 = new Array2DRowRealMatrix(testData2);
        Assert.assertEquals("testData row dimension",3,m.getRowDimension());
        Assert.assertEquals("testData column dimension",3,m.getColumnDimension());
        Assert.assertTrue("testData is square",m.isSquare());
        Assert.assertEquals("testData2 row dimension",m2.getRowDimension(),2);
        Assert.assertEquals("testData2 column dimension",m2.getColumnDimension(),3);
        Assert.assertTrue("testData2 is not square",!m2.isSquare());
    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testCopyFunctions
    public void testCopyFunctions() {
        Array2DRowRealMatrix m1 = new Array2DRowRealMatrix(testData);
        Array2DRowRealMatrix m2 = new Array2DRowRealMatrix(m1.getData());
        Assert.assertEquals(m2,m1);
        Array2DRowRealMatrix m3 = new Array2DRowRealMatrix(testData);
        Array2DRowRealMatrix m4 = new Array2DRowRealMatrix(m3.getData(), false);
        Assert.assertEquals(m4,m3);
    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testAdd
    public void testAdd() {
        Array2DRowRealMatrix m = new Array2DRowRealMatrix(testData);
        Array2DRowRealMatrix mInv = new Array2DRowRealMatrix(testDataInv);
        RealMatrix mPlusMInv = m.add(mInv);
        double[][] sumEntries = mPlusMInv.getData();
        for (int row = 0; row < m.getRowDimension(); row++) {
            for (int col = 0; col < m.getColumnDimension(); col++) {
                Assert.assertEquals("sum entry entry",
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
            Assert.fail("MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testNorm
    public void testNorm() {
        Array2DRowRealMatrix m = new Array2DRowRealMatrix(testData);
        Array2DRowRealMatrix m2 = new Array2DRowRealMatrix(testData2);
        Assert.assertEquals("testData norm",14d,m.getNorm(),entryTolerance);
        Assert.assertEquals("testData2 norm",7d,m2.getNorm(),entryTolerance);
    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testFrobeniusNorm
    public void testFrobeniusNorm() {
        Array2DRowRealMatrix m = new Array2DRowRealMatrix(testData);
        Array2DRowRealMatrix m2 = new Array2DRowRealMatrix(testData2);
        Assert.assertEquals("testData Frobenius norm", FastMath.sqrt(117.0), m.getFrobeniusNorm(), entryTolerance);
        Assert.assertEquals("testData2 Frobenius norm", FastMath.sqrt(52.0), m2.getFrobeniusNorm(), entryTolerance);
    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testPlusMinus
    public void testPlusMinus() {
        Array2DRowRealMatrix m = new Array2DRowRealMatrix(testData);
        Array2DRowRealMatrix m2 = new Array2DRowRealMatrix(testDataInv);
        TestUtils.assertEquals("m-n = m + -n",m.subtract(m2),
            m2.scalarMultiply(-1d).add(m),entryTolerance);
        try {
            m.subtract(new Array2DRowRealMatrix(testData2));
            Assert.fail("Expecting illegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
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
            Assert.fail("Expecting illegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testMultiply2
    public void testMultiply2() {
       RealMatrix m3 = new Array2DRowRealMatrix(d3);
       RealMatrix m4 = new Array2DRowRealMatrix(d4);
       RealMatrix m5 = new Array2DRowRealMatrix(d5);
       TestUtils.assertEquals("m3*m4=m5", m3.multiply(m4), m5, entryTolerance);
   }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testPower
    public void testPower() {
        Array2DRowRealMatrix m = new Array2DRowRealMatrix(testData);
        Array2DRowRealMatrix mInv = new Array2DRowRealMatrix(testDataInv);
        Array2DRowRealMatrix mPlusInv = new Array2DRowRealMatrix(testDataPlusInv);
        Array2DRowRealMatrix identity = new Array2DRowRealMatrix(id);

        TestUtils.assertEquals("m^0", m.power(0),
            identity, entryTolerance);
        TestUtils.assertEquals("mInv^0", mInv.power(0),
                identity, entryTolerance);
        TestUtils.assertEquals("mPlusInv^0", mPlusInv.power(0),
                identity, entryTolerance);

        TestUtils.assertEquals("m^1", m.power(1),
                m, entryTolerance);
        TestUtils.assertEquals("mInv^1", mInv.power(1),
                mInv, entryTolerance);
        TestUtils.assertEquals("mPlusInv^1", mPlusInv.power(1),
                mPlusInv, entryTolerance);

        RealMatrix C1 = m.copy();
        RealMatrix C2 = mInv.copy();
        RealMatrix C3 = mPlusInv.copy();

        for (int i = 2; i <= 10; ++i) {
            C1 = C1.multiply(m);
            C2 = C2.multiply(mInv);
            C3 = C3.multiply(mPlusInv);

            TestUtils.assertEquals("m^" + i, m.power(i),
                    C1, entryTolerance);
            TestUtils.assertEquals("mInv^" + i, mInv.power(i),
                    C2, entryTolerance);
            TestUtils.assertEquals("mPlusInv^" + i, mPlusInv.power(i),
                    C3, entryTolerance);
        }

        try {
            Array2DRowRealMatrix mNotSquare = new Array2DRowRealMatrix(testData2T);
            mNotSquare.power(2);
            Assert.fail("Expecting NonSquareMatrixException");
        } catch (NonSquareMatrixException ex) {
            
        }

        try {
            m.power(-1);
            Assert.fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testTrace
    public void testTrace() {
        RealMatrix m = new Array2DRowRealMatrix(id);
        Assert.assertEquals("identity trace",3d,m.getTrace(),entryTolerance);
        m = new Array2DRowRealMatrix(testData2);
        try {
            m.getTrace();
            Assert.fail("Expecting NonSquareMatrixException");
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
                    m.operate(new ArrayRealVector(testVector)).toArray(), entryTolerance);
        m = new Array2DRowRealMatrix(bigSingular);
        try {
            m.operate(testVector);
            Assert.fail("Expecting illegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testMath209
    public void testMath209() {
        RealMatrix a = new Array2DRowRealMatrix(new double[][] {
                { 1, 2 }, { 3, 4 }, { 5, 6 }
        }, false);
        double[] b = a.operate(new double[] { 1, 1 });
        Assert.assertEquals(a.getRowDimension(), b.length);
        Assert.assertEquals( 3.0, b[0], 1.0e-12);
        Assert.assertEquals( 7.0, b[1], 1.0e-12);
        Assert.assertEquals(11.0, b[2], 1.0e-12);
    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testTranspose
    public void testTranspose() {
        RealMatrix m = new Array2DRowRealMatrix(testData);
        RealMatrix mIT = new LUDecomposition(m).getSolver().getInverse().transpose();
        RealMatrix mTI = new LUDecomposition(m.transpose()).getSolver().getInverse();
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
        TestUtils.assertEquals("premultiply", m.preMultiply(new ArrayRealVector(testVector).toArray()),
                    preMultTest, normTolerance);
        m = new Array2DRowRealMatrix(bigSingular);
        try {
            m.preMultiply(testVector);
            Assert.fail("expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
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
            Assert.fail("Expecting illegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testGetVectors
    public void testGetVectors() {
        RealMatrix m = new Array2DRowRealMatrix(testData);
        TestUtils.assertEquals("get row",m.getRow(0),testDataRow1,entryTolerance);
        TestUtils.assertEquals("get col",m.getColumn(2),testDataCol3,entryTolerance);
        try {
            m.getRow(10);
            Assert.fail("expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.getColumn(-1);
            Assert.fail("expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testGetEntry
    public void testGetEntry() {
        RealMatrix m = new Array2DRowRealMatrix(testData);
        Assert.assertEquals("get entry",m.getEntry(0,1),2d,entryTolerance);
        try {
            m.getEntry(10, 4);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testExamples
    public void testExamples() {
        
        double[][] matrixData = { {1d,2d,3d}, {2d,5d,3d}};
        RealMatrix m = new Array2DRowRealMatrix(matrixData);
        
        double[][] matrixData2 = { {1d,2d}, {2d,5d}, {1d, 7d}};
        RealMatrix n = new Array2DRowRealMatrix(matrixData2);
        
        RealMatrix p = m.multiply(n);
        Assert.assertEquals(2, p.getRowDimension());
        Assert.assertEquals(2, p.getColumnDimension());
        
        RealMatrix pInverse = new LUDecomposition(p).getSolver().getInverse();
        Assert.assertEquals(2, pInverse.getRowDimension());
        Assert.assertEquals(2, pInverse.getColumnDimension());

        
        double[][] coefficientsData = {{2, 3, -2}, {-1, 7, 6}, {4, -3, -5}};
        RealMatrix coefficients = new Array2DRowRealMatrix(coefficientsData);
        RealVector constants = new ArrayRealVector(new double[]{1, -2, 1}, false);
        RealVector solution = new LUDecomposition(coefficients).getSolver().solve(constants);
        final double cst0 = constants.getEntry(0);
        final double cst1 = constants.getEntry(1);
        final double cst2 = constants.getEntry(2);
        final double sol0 = solution.getEntry(0);
        final double sol1 = solution.getEntry(1);
        final double sol2 = solution.getEntry(2);
        Assert.assertEquals(2 * sol0 + 3 * sol1 -2 * sol2, cst0, 1E-12);
        Assert.assertEquals(-1 * sol0 + 7 * sol1 + 6 * sol2, cst1, 1E-12);
        Assert.assertEquals(4 * sol0 - 3 * sol1 -5 * sol2, cst2, 1E-12);
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
        Assert.assertEquals("Row0", mRow0,
                m.getRowMatrix(0));
        Assert.assertEquals("Row3", mRow3,
                m.getRowMatrix(3));
        try {
            m.getRowMatrix(-1);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.getRowMatrix(4);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testSetRowMatrix
    public void testSetRowMatrix() {
        RealMatrix m = new Array2DRowRealMatrix(subTestData);
        RealMatrix mRow3 = new Array2DRowRealMatrix(subRow3);
        Assert.assertNotSame(mRow3, m.getRowMatrix(0));
        m.setRowMatrix(0, mRow3);
        Assert.assertEquals(mRow3, m.getRowMatrix(0));
        try {
            m.setRowMatrix(-1, mRow3);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.setRowMatrix(0, m);
            Assert.fail("Expecting MatrixDimensionMismatchException");
        } catch (MatrixDimensionMismatchException ex) {
            
        }
    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testGetColumnMatrix
    public void testGetColumnMatrix() {
        RealMatrix m = new Array2DRowRealMatrix(subTestData);
        RealMatrix mColumn1 = new Array2DRowRealMatrix(subColumn1);
        RealMatrix mColumn3 = new Array2DRowRealMatrix(subColumn3);
        Assert.assertEquals("Column1", mColumn1,
                m.getColumnMatrix(1));
        Assert.assertEquals("Column3", mColumn3,
                m.getColumnMatrix(3));
        try {
            m.getColumnMatrix(-1);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.getColumnMatrix(4);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testSetColumnMatrix
    public void testSetColumnMatrix() {
        RealMatrix m = new Array2DRowRealMatrix(subTestData);
        RealMatrix mColumn3 = new Array2DRowRealMatrix(subColumn3);
        Assert.assertNotSame(mColumn3, m.getColumnMatrix(1));
        m.setColumnMatrix(1, mColumn3);
        Assert.assertEquals(mColumn3, m.getColumnMatrix(1));
        try {
            m.setColumnMatrix(-1, mColumn3);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.setColumnMatrix(0, m);
            Assert.fail("Expecting MatrixDimensionMismatchException");
        } catch (MatrixDimensionMismatchException ex) {
            
        }
    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testGetRowVector
    public void testGetRowVector() {
        RealMatrix m = new Array2DRowRealMatrix(subTestData);
        RealVector mRow0 = new ArrayRealVector(subRow0[0]);
        RealVector mRow3 = new ArrayRealVector(subRow3[0]);
        Assert.assertEquals("Row0", mRow0, m.getRowVector(0));
        Assert.assertEquals("Row3", mRow3, m.getRowVector(3));
        try {
            m.getRowVector(-1);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.getRowVector(4);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testSetRowVector
    public void testSetRowVector() {
        RealMatrix m = new Array2DRowRealMatrix(subTestData);
        RealVector mRow3 = new ArrayRealVector(subRow3[0]);
        Assert.assertNotSame(mRow3, m.getRowMatrix(0));
        m.setRowVector(0, mRow3);
        Assert.assertEquals(mRow3, m.getRowVector(0));
        try {
            m.setRowVector(-1, mRow3);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.setRowVector(0, new ArrayRealVector(5));
            Assert.fail("Expecting MatrixDimensionMismatchException");
        } catch (MatrixDimensionMismatchException ex) {
            
        }
    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testGetColumnVector
    public void testGetColumnVector() {
        RealMatrix m = new Array2DRowRealMatrix(subTestData);
        RealVector mColumn1 = columnToVector(subColumn1);
        RealVector mColumn3 = columnToVector(subColumn3);
        Assert.assertEquals("Column1", mColumn1, m.getColumnVector(1));
        Assert.assertEquals("Column3", mColumn3, m.getColumnVector(3));
        try {
            m.getColumnVector(-1);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.getColumnVector(4);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testSetColumnVector
    public void testSetColumnVector() {
        RealMatrix m = new Array2DRowRealMatrix(subTestData);
        RealVector mColumn3 = columnToVector(subColumn3);
        Assert.assertNotSame(mColumn3, m.getColumnVector(1));
        m.setColumnVector(1, mColumn3);
        Assert.assertEquals(mColumn3, m.getColumnVector(1));
        try {
            m.setColumnVector(-1, mColumn3);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.setColumnVector(0, new ArrayRealVector(5));
            Assert.fail("Expecting MatrixDimensionMismatchException");
        } catch (MatrixDimensionMismatchException ex) {
            
        }
    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testGetRow
    public void testGetRow() {
        RealMatrix m = new Array2DRowRealMatrix(subTestData);
        checkArrays(subRow0[0], m.getRow(0));
        checkArrays(subRow3[0], m.getRow(3));
        try {
            m.getRow(-1);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.getRow(4);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testSetRow
    public void testSetRow() {
        RealMatrix m = new Array2DRowRealMatrix(subTestData);
        Assert.assertTrue(subRow3[0][0] != m.getRow(0)[0]);
        m.setRow(0, subRow3[0]);
        checkArrays(subRow3[0], m.getRow(0));
        try {
            m.setRow(-1, subRow3[0]);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.setRow(0, new double[5]);
            Assert.fail("Expecting MatrixDimensionMismatchException");
        } catch (MatrixDimensionMismatchException ex) {
            
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
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.getColumn(4);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testSetColumn
    public void testSetColumn() {
        RealMatrix m = new Array2DRowRealMatrix(subTestData);
        double[] mColumn3 = columnToArray(subColumn3);
        Assert.assertTrue(mColumn3[0] != m.getColumn(1)[0]);
        m.setColumn(1, mColumn3);
        checkArrays(mColumn3, m.getColumn(1));
        try {
            m.setColumn(-1, mColumn3);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.setColumn(0, new double[5]);
            Assert.fail("Expecting MatrixDimensionMismatchException");
        } catch (MatrixDimensionMismatchException ex) {
            
        }
    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testEqualsAndHashCode
    public void testEqualsAndHashCode() {
        Array2DRowRealMatrix m = new Array2DRowRealMatrix(testData);
        Array2DRowRealMatrix m1 = (Array2DRowRealMatrix) m.copy();
        Array2DRowRealMatrix mt = (Array2DRowRealMatrix) m.transpose();
        Assert.assertTrue(m.hashCode() != mt.hashCode());
        Assert.assertEquals(m.hashCode(), m1.hashCode());
        Assert.assertEquals(m, m);
        Assert.assertEquals(m, m1);
        Assert.assertFalse(m.equals(null));
        Assert.assertFalse(m.equals(mt));
        Assert.assertFalse(m.equals(new Array2DRowRealMatrix(bigSingular)));
    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testToString
    public void testToString() {
        Array2DRowRealMatrix m = new Array2DRowRealMatrix(testData);
        Assert.assertEquals("Array2DRowRealMatrix{{1.0,2.0,3.0},{2.0,5.0,3.0},{1.0,0.0,8.0}}",
                m.toString());
        m = new Array2DRowRealMatrix();
        Assert.assertEquals("Array2DRowRealMatrix{}",
                m.toString());
    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testSetSubMatrix
    public void testSetSubMatrix() throws Exception {
        Array2DRowRealMatrix m = new Array2DRowRealMatrix(testData);
        m.setSubMatrix(detData2,1,1);
        RealMatrix expected = MatrixUtils.createRealMatrix
            (new double[][] {{1.0,2.0,3.0},{2.0,1.0,3.0},{1.0,2.0,4.0}});
        Assert.assertEquals(expected, m);

        m.setSubMatrix(detData2,0,0);
        expected = MatrixUtils.createRealMatrix
            (new double[][] {{1.0,3.0,3.0},{2.0,4.0,3.0},{1.0,2.0,4.0}});
        Assert.assertEquals(expected, m);

        m.setSubMatrix(testDataPlus2,0,0);
        expected = MatrixUtils.createRealMatrix
            (new double[][] {{3.0,4.0,5.0},{4.0,7.0,5.0},{3.0,2.0,10.0}});
        Assert.assertEquals(expected, m);

        
        try {
            m.setSubMatrix(testData,1,1);
            Assert.fail("expecting OutOfRangeException");
        } catch (OutOfRangeException e) {
            
        }
        
        try {
            m.setSubMatrix(testData,-1,1);
            Assert.fail("expecting OutOfRangeException");
        } catch (OutOfRangeException e) {
            
        }
        try {
            m.setSubMatrix(testData,1,-1);
            Assert.fail("expecting OutOfRangeException");
        } catch (OutOfRangeException e) {
            
        }

        
        try {
            m.setSubMatrix(null,1,1);
            Assert.fail("expecting NullArgumentException");
        } catch (NullArgumentException e) {
            
        }
        Array2DRowRealMatrix m2 = new Array2DRowRealMatrix();
        try {
            m2.setSubMatrix(testData,0,1);
            Assert.fail("expecting MathIllegalStateException");
        } catch (MathIllegalStateException e) {
            
        }
        try {
            m2.setSubMatrix(testData,1,0);
            Assert.fail("expecting MathIllegalStateException");
        } catch (MathIllegalStateException e) {
            
        }

        
        try {
            m.setSubMatrix(new double[][] {{1}, {2, 3}}, 0, 0);
            Assert.fail("expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException e) {
            
        }

        
        try {
            m.setSubMatrix(new double[][] {{}}, 0, 0);
            Assert.fail("expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException e) {
            
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
        Assert.assertEquals(rows * columns, getVisitor.getCount());

        m = new Array2DRowRealMatrix(rows, columns);
        m.walkInRowOrder(new SetVisitor(), 1, rows - 2, 1, columns - 2);
        getVisitor = new GetVisitor();
        m.walkInOptimizedOrder(getVisitor, 1, rows - 2, 1, columns - 2);
        Assert.assertEquals((rows - 2) * (columns - 2), getVisitor.getCount());
        for (int i = 0; i < rows; ++i) {
            Assert.assertEquals(0.0, m.getEntry(i, 0), 0);
            Assert.assertEquals(0.0, m.getEntry(i, columns - 1), 0);
        }
        for (int j = 0; j < columns; ++j) {
            Assert.assertEquals(0.0, m.getEntry(0, j), 0);
            Assert.assertEquals(0.0, m.getEntry(rows - 1, j), 0);
        }

        m = new Array2DRowRealMatrix(rows, columns);
        m.walkInColumnOrder(new SetVisitor());
        getVisitor = new GetVisitor();
        m.walkInOptimizedOrder(getVisitor);
        Assert.assertEquals(rows * columns, getVisitor.getCount());

        m = new Array2DRowRealMatrix(rows, columns);
        m.walkInColumnOrder(new SetVisitor(), 1, rows - 2, 1, columns - 2);
        getVisitor = new GetVisitor();
        m.walkInOptimizedOrder(getVisitor, 1, rows - 2, 1, columns - 2);
        Assert.assertEquals((rows - 2) * (columns - 2), getVisitor.getCount());
        for (int i = 0; i < rows; ++i) {
            Assert.assertEquals(0.0, m.getEntry(i, 0), 0);
            Assert.assertEquals(0.0, m.getEntry(i, columns - 1), 0);
        }
        for (int j = 0; j < columns; ++j) {
            Assert.assertEquals(0.0, m.getEntry(0, j), 0);
            Assert.assertEquals(0.0, m.getEntry(rows - 1, j), 0);
        }

        m = new Array2DRowRealMatrix(rows, columns);
        m.walkInOptimizedOrder(new SetVisitor());
        getVisitor = new GetVisitor();
        m.walkInRowOrder(getVisitor);
        Assert.assertEquals(rows * columns, getVisitor.getCount());

        m = new Array2DRowRealMatrix(rows, columns);
        m.walkInOptimizedOrder(new SetVisitor(), 1, rows - 2, 1, columns - 2);
        getVisitor = new GetVisitor();
        m.walkInRowOrder(getVisitor, 1, rows - 2, 1, columns - 2);
        Assert.assertEquals((rows - 2) * (columns - 2), getVisitor.getCount());
        for (int i = 0; i < rows; ++i) {
            Assert.assertEquals(0.0, m.getEntry(i, 0), 0);
            Assert.assertEquals(0.0, m.getEntry(i, columns - 1), 0);
        }
        for (int j = 0; j < columns; ++j) {
            Assert.assertEquals(0.0, m.getEntry(0, j), 0);
            Assert.assertEquals(0.0, m.getEntry(rows - 1, j), 0);
        }

        m = new Array2DRowRealMatrix(rows, columns);
        m.walkInOptimizedOrder(new SetVisitor());
        getVisitor = new GetVisitor();
        m.walkInColumnOrder(getVisitor);
        Assert.assertEquals(rows * columns, getVisitor.getCount());

        m = new Array2DRowRealMatrix(rows, columns);
        m.walkInOptimizedOrder(new SetVisitor(), 1, rows - 2, 1, columns - 2);
        getVisitor = new GetVisitor();
        m.walkInColumnOrder(getVisitor, 1, rows - 2, 1, columns - 2);
        Assert.assertEquals((rows - 2) * (columns - 2), getVisitor.getCount());
        for (int i = 0; i < rows; ++i) {
            Assert.assertEquals(0.0, m.getEntry(i, 0), 0);
            Assert.assertEquals(0.0, m.getEntry(i, columns - 1), 0);
        }
        for (int j = 0; j < columns; ++j) {
            Assert.assertEquals(0.0, m.getEntry(0, j), 0);
            Assert.assertEquals(0.0, m.getEntry(rows - 1, j), 0);
        }
    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testSerial
    public void testSerial()  {
        Array2DRowRealMatrix m = new Array2DRowRealMatrix(testData);
        Assert.assertEquals(m,TestUtils.serializeAndRecover(m));
    }

// org.apache.commons.math.linear.ArrayRealVectorTest::testConstructors
    public void testConstructors() {

        ArrayRealVector v0 = new ArrayRealVector();
        Assert.assertEquals("testData len", 0, v0.getDimension());

        ArrayRealVector v1 = new ArrayRealVector(7);
        Assert.assertEquals("testData len", 7, v1.getDimension());
        Assert.assertEquals("testData is 0.0 ", 0.0, v1.getEntry(6), 0);

        ArrayRealVector v2 = new ArrayRealVector(5, 1.23);
        Assert.assertEquals("testData len", 5, v2.getDimension());
        Assert.assertEquals("testData is 1.23 ", 1.23, v2.getEntry(4), 0);

        ArrayRealVector v3 = new ArrayRealVector(vec1);
        Assert.assertEquals("testData len", 3, v3.getDimension());
        Assert.assertEquals("testData is 2.0 ", 2.0, v3.getEntry(1), 0);

        ArrayRealVector v3_bis = new ArrayRealVector(vec1, true);
        Assert.assertEquals("testData len", 3, v3_bis.getDimension());
        Assert.assertEquals("testData is 2.0 ", 2.0, v3_bis.getEntry(1), 0);
        Assert.assertNotSame(v3_bis.getDataRef(), vec1);
        Assert.assertNotSame(v3_bis.toArray(), vec1);

        ArrayRealVector v3_ter = new ArrayRealVector(vec1, false);
        Assert.assertEquals("testData len", 3, v3_ter.getDimension());
        Assert.assertEquals("testData is 2.0 ", 2.0, v3_ter.getEntry(1), 0);
        Assert.assertSame(v3_ter.getDataRef(), vec1);
        Assert.assertNotSame(v3_ter.toArray(), vec1);

        ArrayRealVector v4 = new ArrayRealVector(vec4, 3, 2);
        Assert.assertEquals("testData len", 2, v4.getDimension());
        Assert.assertEquals("testData is 4.0 ", 4.0, v4.getEntry(0), 0);
        try {
            new ArrayRealVector(vec4, 8, 3);
            Assert.fail("MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }

        RealVector v5_i = new ArrayRealVector(dvec1);
        Assert.assertEquals("testData len", 9, v5_i.getDimension());
        Assert.assertEquals("testData is 9.0 ", 9.0, v5_i.getEntry(8), 0);

        ArrayRealVector v5 = new ArrayRealVector(dvec1);
        Assert.assertEquals("testData len", 9, v5.getDimension());
        Assert.assertEquals("testData is 9.0 ", 9.0, v5.getEntry(8), 0);

        ArrayRealVector v6 = new ArrayRealVector(dvec1, 3, 2);
        Assert.assertEquals("testData len", 2, v6.getDimension());
        Assert.assertEquals("testData is 4.0 ", 4.0, v6.getEntry(0), 0);
        try {
            new ArrayRealVector(dvec1, 8, 3);
            Assert.fail("MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }

        ArrayRealVector v7 = new ArrayRealVector(v1);
        Assert.assertEquals("testData len", 7, v7.getDimension());
        Assert.assertEquals("testData is 0.0 ", 0.0, v7.getEntry(6), 0);

        RealVectorTestImpl v7_i = new RealVectorTestImpl(vec1);

        ArrayRealVector v7_2 = new ArrayRealVector(v7_i);
        Assert.assertEquals("testData len", 3, v7_2.getDimension());
        Assert.assertEquals("testData is 0.0 ", 2.0d, v7_2.getEntry(1), 0);

        ArrayRealVector v8 = new ArrayRealVector(v1, true);
        Assert.assertEquals("testData len", 7, v8.getDimension());
        Assert.assertEquals("testData is 0.0 ", 0.0, v8.getEntry(6), 0);
        Assert.assertNotSame("testData not same object ", v1.data, v8.data);

        ArrayRealVector v8_2 = new ArrayRealVector(v1, false);
        Assert.assertEquals("testData len", 7, v8_2.getDimension());
        Assert.assertEquals("testData is 0.0 ", 0.0, v8_2.getEntry(6), 0);
        Assert.assertEquals("testData same object ", v1.data, v8_2.data);

        ArrayRealVector v9 = new ArrayRealVector(v1, v3);
        Assert.assertEquals("testData len", 10, v9.getDimension());
        Assert.assertEquals("testData is 1.0 ", 1.0, v9.getEntry(7), 0);

        ArrayRealVector v10 = new ArrayRealVector(v2, new RealVectorTestImpl(vec3));
        Assert.assertEquals("testData len", 8, v10.getDimension());
        Assert.assertEquals("testData is 1.23 ", 1.23, v10.getEntry(4), 0);
        Assert.assertEquals("testData is 7.0 ", 7.0, v10.getEntry(5), 0);

        ArrayRealVector v11 = new ArrayRealVector(new RealVectorTestImpl(vec3), v2);
        Assert.assertEquals("testData len", 8, v11.getDimension());
        Assert.assertEquals("testData is 9.0 ", 9.0, v11.getEntry(2), 0);
        Assert.assertEquals("testData is 1.23 ", 1.23, v11.getEntry(3), 0);

        ArrayRealVector v12 = new ArrayRealVector(v2, vec3);
        Assert.assertEquals("testData len", 8, v12.getDimension());
        Assert.assertEquals("testData is 1.23 ", 1.23, v12.getEntry(4), 0);
        Assert.assertEquals("testData is 7.0 ", 7.0, v12.getEntry(5), 0);

        ArrayRealVector v13 = new ArrayRealVector(vec3, v2);
        Assert.assertEquals("testData len", 8, v13.getDimension());
        Assert.assertEquals("testData is 9.0 ", 9.0, v13.getEntry(2), 0);
        Assert.assertEquals("testData is 1.23 ", 1.23, v13.getEntry(3), 0);

        ArrayRealVector v14 = new ArrayRealVector(vec3, vec4);
        Assert.assertEquals("testData len", 12, v14.getDimension());
        Assert.assertEquals("testData is 9.0 ", 9.0, v14.getEntry(2), 0);
        Assert.assertEquals("testData is 1.0 ", 1.0, v14.getEntry(3), 0);

    }

// org.apache.commons.math.linear.ArrayRealVectorTest::testDataInOut
    public void testDataInOut() {

        ArrayRealVector v1 = new ArrayRealVector(vec1);
        ArrayRealVector v2 = new ArrayRealVector(vec2);
        ArrayRealVector v4 = new ArrayRealVector(vec4);
        RealVectorTestImpl v2_t = new RealVectorTestImpl(vec2);

        RealVector v_append_1 = v1.append(v2);
        Assert.assertEquals("testData len", 6, v_append_1.getDimension());
        Assert.assertEquals("testData is 4.0 ", 4.0, v_append_1.getEntry(3), 0);

        RealVector v_append_2 = v1.append(2.0);
        Assert.assertEquals("testData len", 4, v_append_2.getDimension());
        Assert.assertEquals("testData is 2.0 ", 2.0, v_append_2.getEntry(3), 0);

        RealVector v_append_4 = v1.append(v2_t);
        Assert.assertEquals("testData len", 6, v_append_4.getDimension());
        Assert.assertEquals("testData is 4.0 ", 4.0, v_append_4.getEntry(3), 0);

        RealVector v_append_5 = v1.append((RealVector) v2);
        Assert.assertEquals("testData len", 6, v_append_5.getDimension());
        Assert.assertEquals("testData is 4.0 ", 4.0, v_append_5.getEntry(3), 0);

        RealVector v_copy = v1.copy();
        Assert.assertEquals("testData len", 3, v_copy.getDimension());
        Assert.assertNotSame("testData not same object ", v1.data, v_copy.toArray());

        double[] a_double = v1.toArray();
        Assert.assertEquals("testData len", 3, a_double.length);
        Assert.assertNotSame("testData not same object ", v1.data, a_double);

        RealVector vout5 = v4.getSubVector(3, 3);
        Assert.assertEquals("testData len", 3, vout5.getDimension());
        Assert.assertEquals("testData is 4.0 ", 5.0, vout5.getEntry(1), 0);
        try {
            v4.getSubVector(3, 7);
            Assert.fail("OutOfRangeException expected");
        } catch (OutOfRangeException ex) {
            
        }

        ArrayRealVector v_set1 = v1.copy();
        v_set1.setEntry(1, 11.0);
        Assert.assertEquals("testData is 11.0 ", 11.0, v_set1.getEntry(1), 0);
        try {
            v_set1.setEntry(3, 11.0);
            Assert.fail("OutOfRangeException expected");
        } catch (OutOfRangeException ex) {
            
        }

        ArrayRealVector v_set3 = v1.copy();
        v_set3.set(13.0);
        Assert.assertEquals("testData is 13.0 ", 13.0, v_set3.getEntry(2), 0);

        try {
            v_set3.getEntry(23);
            Assert.fail("ArrayIndexOutOfBoundsException expected");
        } catch (ArrayIndexOutOfBoundsException ex) {
            
        }

        ArrayRealVector v_set4 = v4.copy();
        v_set4.setSubVector(3, v2_t);
        Assert.assertEquals("testData is 1.0 ", 4.0, v_set4.getEntry(3), 0);
        Assert.assertEquals("testData is 7.0 ", 7.0, v_set4.getEntry(6), 0);
        try {
            v_set4.setSubVector(7, v2_t);
            Assert.fail("OutOfRangeException expected");
        } catch (OutOfRangeException ex) {
            
        }

        ArrayRealVector vout10 = v1.copy();
        ArrayRealVector vout10_2 = v1.copy();
        Assert.assertEquals(vout10, vout10_2);
        vout10_2.setEntry(0, 1.1);
        Assert.assertNotSame(vout10, vout10_2);

    }

// org.apache.commons.math.linear.ArrayRealVectorTest::testMapFunctions
    public void testMapFunctions() {
        ArrayRealVector v1 = new ArrayRealVector(vec1);

        
        RealVector v_mapAdd = v1.mapAdd(2.0d);
        double[] result_mapAdd = {3d, 4d, 5d};
        assertClose("compare vectors" ,result_mapAdd,v_mapAdd.toArray(),normTolerance);

        
        RealVector v_mapAddToSelf = v1.copy();
        v_mapAddToSelf.mapAddToSelf(2.0d);
        double[] result_mapAddToSelf = {3d, 4d, 5d};
        assertClose("compare vectors" ,result_mapAddToSelf,v_mapAddToSelf.toArray(),normTolerance);

        
        RealVector v_mapSubtract = v1.mapSubtract(2.0d);
        double[] result_mapSubtract = {-1d, 0d, 1d};
        assertClose("compare vectors" ,result_mapSubtract,v_mapSubtract.toArray(),normTolerance);

        
        RealVector v_mapSubtractToSelf = v1.copy();
        v_mapSubtractToSelf.mapSubtractToSelf(2.0d);
        double[] result_mapSubtractToSelf = {-1d, 0d, 1d};
        assertClose("compare vectors" ,result_mapSubtractToSelf,v_mapSubtractToSelf.toArray(),normTolerance);

        
        RealVector v_mapMultiply = v1.mapMultiply(2.0d);
        double[] result_mapMultiply = {2d, 4d, 6d};
        assertClose("compare vectors" ,result_mapMultiply,v_mapMultiply.toArray(),normTolerance);

        
        RealVector v_mapMultiplyToSelf = v1.copy();
        v_mapMultiplyToSelf.mapMultiplyToSelf(2.0d);
        double[] result_mapMultiplyToSelf = {2d, 4d, 6d};
        assertClose("compare vectors" ,result_mapMultiplyToSelf,v_mapMultiplyToSelf.toArray(),normTolerance);

        
        RealVector v_mapDivide = v1.mapDivide(2.0d);
        double[] result_mapDivide = {.5d, 1d, 1.5d};
        assertClose("compare vectors" ,result_mapDivide,v_mapDivide.toArray(),normTolerance);

        
        RealVector v_mapDivideToSelf = v1.copy();
        v_mapDivideToSelf.mapDivideToSelf(2.0d);
        double[] result_mapDivideToSelf = {.5d, 1d, 1.5d};
        assertClose("compare vectors" ,result_mapDivideToSelf,v_mapDivideToSelf.toArray(),normTolerance);

        
        RealVector v_mapPow = v1.map(new Power(2));
        double[] result_mapPow = {1d, 4d, 9d};
        assertClose("compare vectors" ,result_mapPow,v_mapPow.toArray(),normTolerance);

        
        RealVector v_mapPowToSelf = v1.copy();
        v_mapPowToSelf.mapToSelf(new Power(2));
        double[] result_mapPowToSelf = {1d, 4d, 9d};
        assertClose("compare vectors" ,result_mapPowToSelf,v_mapPowToSelf.toArray(),normTolerance);

        
        RealVector v_mapExp = v1.map(new Exp());
        double[] result_mapExp = {2.718281828459045e+00d,7.389056098930650e+00d, 2.008553692318767e+01d};
        assertClose("compare vectors" ,result_mapExp,v_mapExp.toArray(),normTolerance);

        
        RealVector v_mapExpToSelf = v1.copy();
        v_mapExpToSelf.mapToSelf(new Exp());
        double[] result_mapExpToSelf = {2.718281828459045e+00d,7.389056098930650e+00d, 2.008553692318767e+01d};
        assertClose("compare vectors" ,result_mapExpToSelf,v_mapExpToSelf.toArray(),normTolerance);

        
        RealVector v_mapExpm1 = v1.map(new Expm1());
        double[] result_mapExpm1 = {1.718281828459045d,6.38905609893065d, 19.085536923187668d};
        assertClose("compare vectors" ,result_mapExpm1,v_mapExpm1.toArray(),normTolerance);

        
        RealVector v_mapExpm1ToSelf = v1.copy();
        v_mapExpm1ToSelf.mapToSelf(new Expm1());
        double[] result_mapExpm1ToSelf = {1.718281828459045d,6.38905609893065d, 19.085536923187668d};
        assertClose("compare vectors" ,result_mapExpm1ToSelf,v_mapExpm1ToSelf.toArray(),normTolerance);

        
        RealVector v_mapLog = v1.map(new Log());
        double[] result_mapLog = {0d,6.931471805599453e-01d, 1.098612288668110e+00d};
        assertClose("compare vectors" ,result_mapLog,v_mapLog.toArray(),normTolerance);

        
        RealVector v_mapLogToSelf = v1.copy();
        v_mapLogToSelf.mapToSelf(new Log());
        double[] result_mapLogToSelf = {0d,6.931471805599453e-01d, 1.098612288668110e+00d};
        assertClose("compare vectors" ,result_mapLogToSelf,v_mapLogToSelf.toArray(),normTolerance);

        
        RealVector v_mapLog10 = v1.map(new Log10());
        double[] result_mapLog10 = {0d,3.010299956639812e-01d, 4.771212547196624e-01d};
        assertClose("compare vectors" ,result_mapLog10,v_mapLog10.toArray(),normTolerance);

        
        RealVector v_mapLog10ToSelf = v1.copy();
        v_mapLog10ToSelf.mapToSelf(new Log10());
        double[] result_mapLog10ToSelf = {0d,3.010299956639812e-01d, 4.771212547196624e-01d};
        assertClose("compare vectors" ,result_mapLog10ToSelf,v_mapLog10ToSelf.toArray(),normTolerance);

        
        RealVector v_mapLog1p = v1.map(new Log1p());
        double[] result_mapLog1p = {0.6931471805599453d,1.0986122886681096d,1.3862943611198906d};
        assertClose("compare vectors" ,result_mapLog1p,v_mapLog1p.toArray(),normTolerance);

        
        RealVector v_mapLog1pToSelf = v1.copy();
        v_mapLog1pToSelf.mapToSelf(new Log1p());
        double[] result_mapLog1pToSelf = {0.6931471805599453d,1.0986122886681096d,1.3862943611198906d};
        assertClose("compare vectors" ,result_mapLog1pToSelf,v_mapLog1pToSelf.toArray(),normTolerance);

        
        RealVector v_mapCosh = v1.map(new Cosh());
        double[] result_mapCosh = {1.543080634815244e+00d,3.762195691083631e+00d, 1.006766199577777e+01d};
        assertClose("compare vectors" ,result_mapCosh,v_mapCosh.toArray(),normTolerance);

        
        RealVector v_mapCoshToSelf = v1.copy();
        v_mapCoshToSelf.mapToSelf(new Cosh());
        double[] result_mapCoshToSelf = {1.543080634815244e+00d,3.762195691083631e+00d, 1.006766199577777e+01d};
        assertClose("compare vectors" ,result_mapCoshToSelf,v_mapCoshToSelf.toArray(),normTolerance);

        
        RealVector v_mapSinh = v1.map(new Sinh());
        double[] result_mapSinh = {1.175201193643801e+00d,3.626860407847019e+00d, 1.001787492740990e+01d};
        assertClose("compare vectors" ,result_mapSinh,v_mapSinh.toArray(),normTolerance);

        
        RealVector v_mapSinhToSelf = v1.copy();
        v_mapSinhToSelf.mapToSelf(new Sinh());
        double[] result_mapSinhToSelf = {1.175201193643801e+00d,3.626860407847019e+00d, 1.001787492740990e+01d};
        assertClose("compare vectors" ,result_mapSinhToSelf,v_mapSinhToSelf.toArray(),normTolerance);

        
        RealVector v_mapTanh = v1.map(new Tanh());
        double[] result_mapTanh = {7.615941559557649e-01d,9.640275800758169e-01d,9.950547536867305e-01d};
        assertClose("compare vectors" ,result_mapTanh,v_mapTanh.toArray(),normTolerance);

        
        RealVector v_mapTanhToSelf = v1.copy();
        v_mapTanhToSelf.mapToSelf(new Tanh());
        double[] result_mapTanhToSelf = {7.615941559557649e-01d,9.640275800758169e-01d,9.950547536867305e-01d};
        assertClose("compare vectors" ,result_mapTanhToSelf,v_mapTanhToSelf.toArray(),normTolerance);

        
        RealVector v_mapCos = v1.map(new Cos());
        double[] result_mapCos = {5.403023058681398e-01d,-4.161468365471424e-01d, -9.899924966004454e-01d};
        assertClose("compare vectors" ,result_mapCos,v_mapCos.toArray(),normTolerance);

        
        RealVector v_mapCosToSelf = v1.copy();
        v_mapCosToSelf.mapToSelf(new Cos());
        double[] result_mapCosToSelf = {5.403023058681398e-01d,-4.161468365471424e-01d, -9.899924966004454e-01d};
        assertClose("compare vectors" ,result_mapCosToSelf,v_mapCosToSelf.toArray(),normTolerance);

        
        RealVector v_mapSin = v1.map(new Sin());
        double[] result_mapSin = {8.414709848078965e-01d,9.092974268256817e-01d,1.411200080598672e-01d};
        assertClose("compare vectors" ,result_mapSin,v_mapSin.toArray(),normTolerance);

        
        RealVector v_mapSinToSelf = v1.copy();
        v_mapSinToSelf.mapToSelf(new Sin());
        double[] result_mapSinToSelf = {8.414709848078965e-01d,9.092974268256817e-01d,1.411200080598672e-01d};
        assertClose("compare vectors" ,result_mapSinToSelf,v_mapSinToSelf.toArray(),normTolerance);

        
        RealVector v_mapTan = v1.map(new Tan());
        double[] result_mapTan = {1.557407724654902e+00d,-2.185039863261519e+00d,-1.425465430742778e-01d};
        assertClose("compare vectors" ,result_mapTan,v_mapTan.toArray(),normTolerance);

        
        RealVector v_mapTanToSelf = v1.copy();
        v_mapTanToSelf.mapToSelf(new Tan());
        double[] result_mapTanToSelf = {1.557407724654902e+00d,-2.185039863261519e+00d,-1.425465430742778e-01d};
        assertClose("compare vectors" ,result_mapTanToSelf,v_mapTanToSelf.toArray(),normTolerance);

        double[] vat_a = {0d, 0.5d, 1.0d};
        ArrayRealVector vat = new ArrayRealVector(vat_a);

        
        RealVector v_mapAcos = vat.map(new Acos());
        double[] result_mapAcos = {1.570796326794897e+00d,1.047197551196598e+00d, 0.0d};
        assertClose("compare vectors" ,result_mapAcos,v_mapAcos.toArray(),normTolerance);

        
        RealVector v_mapAcosToSelf = vat.copy();
        v_mapAcosToSelf.mapToSelf(new Acos());
        double[] result_mapAcosToSelf = {1.570796326794897e+00d,1.047197551196598e+00d, 0.0d};
        assertClose("compare vectors" ,result_mapAcosToSelf,v_mapAcosToSelf.toArray(),normTolerance);

        
        RealVector v_mapAsin = vat.map(new Asin());
        double[] result_mapAsin = {0.0d,5.235987755982989e-01d,1.570796326794897e+00d};
        assertClose("compare vectors" ,result_mapAsin,v_mapAsin.toArray(),normTolerance);

        
        RealVector v_mapAsinToSelf = vat.copy();
        v_mapAsinToSelf.mapToSelf(new Asin());
        double[] result_mapAsinToSelf = {0.0d,5.235987755982989e-01d,1.570796326794897e+00d};
        assertClose("compare vectors" ,result_mapAsinToSelf,v_mapAsinToSelf.toArray(),normTolerance);

        
        RealVector v_mapAtan = vat.map(new Atan());
        double[] result_mapAtan = {0.0d,4.636476090008061e-01d,7.853981633974483e-01d};
        assertClose("compare vectors" ,result_mapAtan,v_mapAtan.toArray(),normTolerance);

        
        RealVector v_mapAtanToSelf = vat.copy();
        v_mapAtanToSelf.mapToSelf(new Atan());
        double[] result_mapAtanToSelf = {0.0d,4.636476090008061e-01d,7.853981633974483e-01d};
        assertClose("compare vectors" ,result_mapAtanToSelf,v_mapAtanToSelf.toArray(),normTolerance);

        
        RealVector v_mapInv = v1.map(new Inverse());
        double[] result_mapInv = {1d,0.5d,3.333333333333333e-01d};
        assertClose("compare vectors" ,result_mapInv,v_mapInv.toArray(),normTolerance);

        
        RealVector v_mapInvToSelf = v1.copy();
        v_mapInvToSelf.mapToSelf(new Inverse());
        double[] result_mapInvToSelf = {1d,0.5d,3.333333333333333e-01d};
        assertClose("compare vectors" ,result_mapInvToSelf,v_mapInvToSelf.toArray(),normTolerance);

        double[] abs_a = {-1.0d, 0.0d, 1.0d};
        ArrayRealVector abs_v = new ArrayRealVector(abs_a);

        
        RealVector v_mapAbs = abs_v.map(new Abs());
        double[] result_mapAbs = {1d,0d,1d};
        assertClose("compare vectors" ,result_mapAbs,v_mapAbs.toArray(),normTolerance);

        
        RealVector v_mapAbsToSelf = abs_v.copy();
        v_mapAbsToSelf.mapToSelf(new Abs());
        double[] result_mapAbsToSelf = {1d,0d,1d};
        assertClose("compare vectors" ,result_mapAbsToSelf,v_mapAbsToSelf.toArray(),normTolerance);

        
        RealVector v_mapSqrt = v1.map(new Sqrt());
        double[] result_mapSqrt = {1d,1.414213562373095e+00d,1.732050807568877e+00d};
        assertClose("compare vectors" ,result_mapSqrt,v_mapSqrt.toArray(),normTolerance);

        
        RealVector v_mapSqrtToSelf = v1.copy();
        v_mapSqrtToSelf.mapToSelf(new Sqrt());
        double[] result_mapSqrtToSelf = {1d,1.414213562373095e+00d,1.732050807568877e+00d};
        assertClose("compare vectors" ,result_mapSqrtToSelf,v_mapSqrtToSelf.toArray(),normTolerance);

        double[] cbrt_a = {-2.0d, 0.0d, 2.0d};
        ArrayRealVector cbrt_v = new ArrayRealVector(cbrt_a);

        
        RealVector v_mapCbrt = cbrt_v.map(new Cbrt());
        double[] result_mapCbrt = {-1.2599210498948732d,0d,1.2599210498948732d};
        assertClose("compare vectors" ,result_mapCbrt,v_mapCbrt.toArray(),normTolerance);

        
        RealVector v_mapCbrtToSelf = cbrt_v.copy();
        v_mapCbrtToSelf.mapToSelf(new Cbrt());
        double[] result_mapCbrtToSelf =  {-1.2599210498948732d,0d,1.2599210498948732d};
        assertClose("compare vectors" ,result_mapCbrtToSelf,v_mapCbrtToSelf.toArray(),normTolerance);

        double[] ceil_a = {-1.1d, 0.9d, 1.1d};
        ArrayRealVector ceil_v = new ArrayRealVector(ceil_a);

        
        RealVector v_mapCeil = ceil_v.map(new Ceil());
        double[] result_mapCeil = {-1d,1d,2d};
        assertClose("compare vectors" ,result_mapCeil,v_mapCeil.toArray(),normTolerance);

        
        RealVector v_mapCeilToSelf = ceil_v.copy();
        v_mapCeilToSelf.mapToSelf(new Ceil());
        double[] result_mapCeilToSelf =  {-1d,1d,2d};
        assertClose("compare vectors" ,result_mapCeilToSelf,v_mapCeilToSelf.toArray(),normTolerance);

        
        RealVector v_mapFloor = ceil_v.map(new Floor());
        double[] result_mapFloor = {-2d,0d,1d};
        assertClose("compare vectors" ,result_mapFloor,v_mapFloor.toArray(),normTolerance);

        
        RealVector v_mapFloorToSelf = ceil_v.copy();
        v_mapFloorToSelf.mapToSelf(new Floor());
        double[] result_mapFloorToSelf =  {-2d,0d,1d};
        assertClose("compare vectors" ,result_mapFloorToSelf,v_mapFloorToSelf.toArray(),normTolerance);

        
        RealVector v_mapRint = ceil_v.map(new Rint());
        double[] result_mapRint = {-1d,1d,1d};
        assertClose("compare vectors" ,result_mapRint,v_mapRint.toArray(),normTolerance);

        
        RealVector v_mapRintToSelf = ceil_v.copy();
        v_mapRintToSelf.mapToSelf(new Rint());
        double[] result_mapRintToSelf =  {-1d,1d,1d};
        assertClose("compare vectors" ,result_mapRintToSelf,v_mapRintToSelf.toArray(),normTolerance);

        
        RealVector v_mapSignum = ceil_v.map(new Signum());
        double[] result_mapSignum = {-1d,1d,1d};
        assertClose("compare vectors" ,result_mapSignum,v_mapSignum.toArray(),normTolerance);

        
        RealVector v_mapSignumToSelf = ceil_v.copy();
        v_mapSignumToSelf.mapToSelf(new Signum());
        double[] result_mapSignumToSelf =  {-1d,1d,1d};
        assertClose("compare vectors" ,result_mapSignumToSelf,v_mapSignumToSelf.toArray(),normTolerance);

        
        
        RealVector v_mapUlp = ceil_v.map(new Ulp());
        double[] result_mapUlp = {2.220446049250313E-16d,1.1102230246251565E-16d,2.220446049250313E-16d};
        assertClose("compare vectors" ,result_mapUlp,v_mapUlp.toArray(),normTolerance);

        
        RealVector v_mapUlpToSelf = ceil_v.copy();
        v_mapUlpToSelf.mapToSelf(new Ulp());
        double[] result_mapUlpToSelf = {2.220446049250313E-16d,1.1102230246251565E-16d,2.220446049250313E-16d};
        assertClose("compare vectors" ,result_mapUlpToSelf,v_mapUlpToSelf.toArray(),normTolerance);
    }

// org.apache.commons.math.linear.ArrayRealVectorTest::testBasicFunctions
    public void testBasicFunctions() {
        ArrayRealVector v1 = new ArrayRealVector(vec1);
        ArrayRealVector v2 = new ArrayRealVector(vec2);
        ArrayRealVector v5 = new ArrayRealVector(vec5);
        ArrayRealVector v_null = new ArrayRealVector(vec_null);

        RealVectorTestImpl v2_t = new RealVectorTestImpl(vec2);

        
        double d_getNorm = v5.getNorm();
        Assert.assertEquals("compare values  ", 8.4261497731763586307, d_getNorm, normTolerance);

        
        double d_getL1Norm = v5.getL1Norm();
        Assert.assertEquals("compare values  ", 17.0, d_getL1Norm, normTolerance);

        
        double d_getLInfNorm = v5.getLInfNorm();
        Assert.assertEquals("compare values  ", 6.0, d_getLInfNorm, normTolerance);

        
        double dist = v1.getDistance(v2);
        Assert.assertEquals("compare values  ",v1.subtract(v2).getNorm(), dist, normTolerance);

        
        double dist_2 = v1.getDistance(v2_t);
        Assert.assertEquals("compare values  ", v1.subtract(v2).getNorm(),dist_2, normTolerance);

        
        double dist_3 = v1.getDistance((RealVector) v2);
        Assert.assertEquals("compare values  ", v1.subtract(v2).getNorm(),dist_3, normTolerance);

        
        double d_getL1Distance = v1. getL1Distance(v2);
        Assert.assertEquals("compare values  ", 9d, d_getL1Distance, normTolerance);

        double d_getL1Distance_2 = v1.getL1Distance(v2_t);
        Assert.assertEquals("compare values  ", 9d, d_getL1Distance_2, normTolerance);

        double d_getL1Distance_3 = v1.getL1Distance((RealVector) v2);
        Assert.assertEquals("compare values  ", 9d, d_getL1Distance_3, normTolerance);

        
        double d_getLInfDistance = v1.getLInfDistance(v2);
        Assert.assertEquals("compare values  ", 3d, d_getLInfDistance, normTolerance);

        double d_getLInfDistance_2 = v1. getLInfDistance(v2_t);
        Assert.assertEquals("compare values  ", 3d, d_getLInfDistance_2, normTolerance);

        double d_getLInfDistance_3 = v1. getLInfDistance((RealVector) v2);
        Assert.assertEquals("compare values  ", 3d, d_getLInfDistance_3, normTolerance);

        
        ArrayRealVector v_add = v1.add(v2);
        double[] result_add = {5d, 7d, 9d};
        assertClose("compare vect" ,v_add.toArray(), result_add, normTolerance);

        RealVectorTestImpl vt2 = new RealVectorTestImpl(vec2);
        RealVector v_add_i = v1.add(vt2);
        double[] result_add_i = {5d, 7d, 9d};
        assertClose("compare vect" ,v_add_i.toArray(),result_add_i,normTolerance);

        
        ArrayRealVector v_subtract = v1.subtract(v2);
        double[] result_subtract = {-3d, -3d, -3d};
        assertClose("compare vect" ,v_subtract.toArray(),result_subtract,normTolerance);

        RealVector v_subtract_i = v1.subtract(vt2);
        double[] result_subtract_i = {-3d, -3d, -3d};
        assertClose("compare vect" ,v_subtract_i.toArray(),result_subtract_i,normTolerance);

        
        ArrayRealVector  v_ebeMultiply = v1.ebeMultiply(v2);
        double[] result_ebeMultiply = {4d, 10d, 18d};
        assertClose("compare vect" ,v_ebeMultiply.toArray(),result_ebeMultiply,normTolerance);

        RealVector  v_ebeMultiply_2 = v1.ebeMultiply(v2_t);
        double[] result_ebeMultiply_2 = {4d, 10d, 18d};
        assertClose("compare vect" ,v_ebeMultiply_2.toArray(),result_ebeMultiply_2,normTolerance);

        RealVector  v_ebeMultiply_3 = v1.ebeMultiply((RealVector) v2);
        double[] result_ebeMultiply_3 = {4d, 10d, 18d};
        assertClose("compare vect" ,v_ebeMultiply_3.toArray(),result_ebeMultiply_3,normTolerance);

        
        ArrayRealVector  v_ebeDivide = v1.ebeDivide(v2);
        double[] result_ebeDivide = {0.25d, 0.4d, 0.5d};
        assertClose("compare vect" ,v_ebeDivide.toArray(),result_ebeDivide,normTolerance);

        RealVector  v_ebeDivide_2 = v1.ebeDivide(v2_t);
        double[] result_ebeDivide_2 = {0.25d, 0.4d, 0.5d};
        assertClose("compare vect" ,v_ebeDivide_2.toArray(),result_ebeDivide_2,normTolerance);

        RealVector  v_ebeDivide_3 = v1.ebeDivide((RealVector) v2);
        double[] result_ebeDivide_3 = {0.25d, 0.4d, 0.5d};
        assertClose("compare vect" ,v_ebeDivide_3.toArray(),result_ebeDivide_3,normTolerance);

        
        double dot =  v1.dotProduct(v2);
        Assert.assertEquals("compare val ",32d, dot, normTolerance);

        
        double dot_2 =  v1.dotProduct(v2_t);
        Assert.assertEquals("compare val ",32d, dot_2, normTolerance);

        RealMatrix m_outerProduct = v1.outerProduct(v2);
        Assert.assertEquals("compare val ",4d, m_outerProduct.getEntry(0,0), normTolerance);

        RealMatrix m_outerProduct_2 = v1.outerProduct(v2_t);
        Assert.assertEquals("compare val ",4d, m_outerProduct_2.getEntry(0,0), normTolerance);

        RealMatrix m_outerProduct_3 = v1.outerProduct((RealVector) v2);
        Assert.assertEquals("compare val ",4d, m_outerProduct_3.getEntry(0,0), normTolerance);

        RealVector v_unitVector = v1.unitVector();
        RealVector v_unitVector_2 = v1.mapDivide(v1.getNorm());
        assertClose("compare vect" ,v_unitVector.toArray(),v_unitVector_2.toArray(),normTolerance);

        try {
            v_null.unitVector();
            Assert.fail("Expecting MathArithmeticException");
        } catch (MathArithmeticException ex) {
            
        }

        ArrayRealVector v_unitize = v1.copy();
        v_unitize.unitize();
        assertClose("compare vect" ,v_unitVector_2.toArray(),v_unitize.toArray(),normTolerance);
        try {
            v_null.unitize();
            Assert.fail("Expecting MathArithmeticException");
        } catch (MathArithmeticException ex) {
            
        }

        RealVector v_projection = v1.projection(v2);
        double[] result_projection = {1.662337662337662, 2.0779220779220777, 2.493506493506493};
        assertClose("compare vect", v_projection.toArray(), result_projection, normTolerance);

        RealVector v_projection_2 = v1.projection(v2_t);
        double[] result_projection_2 = {1.662337662337662, 2.0779220779220777, 2.493506493506493};
        assertClose("compare vect", v_projection_2.toArray(), result_projection_2, normTolerance);
    }

// org.apache.commons.math.linear.ArrayRealVectorTest::testMisc
    public void testMisc() {
        ArrayRealVector v1 = new ArrayRealVector(vec1);
        ArrayRealVector v4 = new ArrayRealVector(vec4);
        RealVector v4_2 = new ArrayRealVector(vec4);

        String out1 = v1.toString();
        Assert.assertTrue("some output ",  out1.length()!=0);
        
        try {
            v1.checkVectorDimensions(2);
            Assert.fail("MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }

       try {
            v1.checkVectorDimensions(v4);
            Assert.fail("MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }

        try {
            v1.checkVectorDimensions(v4_2);
            Assert.fail("MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }

    }

// org.apache.commons.math.linear.ArrayRealVectorTest::testPredicates
    public void testPredicates() {

        ArrayRealVector v = new ArrayRealVector(new double[] { 0, 1, 2 });

        Assert.assertFalse(v.isNaN());
        v.setEntry(1, Double.NaN);
        Assert.assertTrue(v.isNaN());

        Assert.assertFalse(v.isInfinite());
        v.setEntry(0, Double.POSITIVE_INFINITY);
        Assert.assertFalse(v.isInfinite());
        v.setEntry(1, 1);
        Assert.assertTrue(v.isInfinite());
        v.setEntry(0, 1);
        Assert.assertFalse(v.isInfinite());

        v.setEntry(0, 0);
        Assert.assertEquals(v, new ArrayRealVector(new double[] { 0, 1, 2 }));
        Assert.assertNotSame(v, new ArrayRealVector(new double[] { 0, 1, 2 + FastMath.ulp(2)}));
        Assert.assertNotSame(v, new ArrayRealVector(new double[] { 0, 1, 2, 3 }));

        Assert.assertEquals(new ArrayRealVector(new double[] { Double.NaN, 1, 2 }).hashCode(),
                     new ArrayRealVector(new double[] { 0, Double.NaN, 2 }).hashCode());

        Assert.assertTrue(new ArrayRealVector(new double[] { Double.NaN, 1, 2 }).hashCode() !=
                   new ArrayRealVector(new double[] { 0, 1, 2 }).hashCode());

        Assert.assertTrue(v.equals(v));
        Assert.assertTrue(v.equals(v.copy()));
        Assert.assertFalse(v.equals(null));
        Assert.assertFalse(v.equals(v.getDataRef()));
        Assert.assertFalse(v.equals(v.getSubVector(0, v.getDimension() - 1)));
        Assert.assertTrue(v.equals(v.getSubVector(0, v.getDimension())));

    }

// org.apache.commons.math.linear.ArrayRealVectorTest::testSerial
    public void testSerial()  {
        ArrayRealVector v = new ArrayRealVector(new double[] { 0, 1, 2 });
        Assert.assertEquals(v,TestUtils.serializeAndRecover(v));
    }

// org.apache.commons.math.linear.ArrayRealVectorTest::testZeroVectors
    public void testZeroVectors() {
        Assert.assertEquals(0, new ArrayRealVector(new double[0]).getDimension());
        Assert.assertEquals(0, new ArrayRealVector(new double[0], true).getDimension());
        Assert.assertEquals(0, new ArrayRealVector(new double[0], false).getDimension());
    }

// org.apache.commons.math.linear.ArrayRealVectorTest::testMinMax
    public void testMinMax()  {
        ArrayRealVector v1 = new ArrayRealVector(new double[] { 0, -6, 4, 12, 7 });
        Assert.assertEquals(1,  v1.getMinIndex());
        Assert.assertEquals(-6, v1.getMinValue(), 1.0e-12);
        Assert.assertEquals(3,  v1.getMaxIndex());
        Assert.assertEquals(12, v1.getMaxValue(), 1.0e-12);
        ArrayRealVector v2 = new ArrayRealVector(new double[] { Double.NaN, 3, Double.NaN, -2 });
        Assert.assertEquals(3,  v2.getMinIndex());
        Assert.assertEquals(-2, v2.getMinValue(), 1.0e-12);
        Assert.assertEquals(1,  v2.getMaxIndex());
        Assert.assertEquals(3, v2.getMaxValue(), 1.0e-12);
        ArrayRealVector v3 = new ArrayRealVector(new double[] { Double.NaN, Double.NaN });
        Assert.assertEquals(-1,  v3.getMinIndex());
        Assert.assertTrue(Double.isNaN(v3.getMinValue()));
        Assert.assertEquals(-1,  v3.getMaxIndex());
        Assert.assertTrue(Double.isNaN(v3.getMaxValue()));
        ArrayRealVector v4 = new ArrayRealVector(new double[0]);
        Assert.assertEquals(-1,  v4.getMinIndex());
        Assert.assertTrue(Double.isNaN(v4.getMinValue()));
        Assert.assertEquals(-1,  v4.getMaxIndex());
        Assert.assertTrue(Double.isNaN(v4.getMaxValue()));
    }

// org.apache.commons.math.linear.ArrayRealVectorTest::testCosine
    public void testCosine() {
        final ArrayRealVector v = new ArrayRealVector(new double[] {1, 0, 0});

        double[] wData = new double[] {1, 1, 0};
        RealVector w = new ArrayRealVector(wData);
        Assert.assertEquals(FastMath.sqrt(2) / 2, v.cosine(w), normTolerance);

        wData = new double[] {1, 0, 0};
        w = new ArrayRealVector(wData);
        Assert.assertEquals(1, v.cosine(w), normTolerance);

        wData = new double[] {0, 1, 0};
        w = new ArrayRealVector(wData);
        Assert.assertEquals(0, v.cosine(w), 0);

        wData = new double[] {-1, 0, 0};
        w = new ArrayRealVector(wData);
        Assert.assertEquals(-1, v.cosine(w), normTolerance);
    }

// org.apache.commons.math.linear.ArrayRealVectorTest::testCosinePrecondition1
    public void testCosinePrecondition1() {
        final ArrayRealVector v = new ArrayRealVector(new double[] {0, 0, 0});
        final ArrayRealVector w = new ArrayRealVector(new double[] {1, 0, 0});
        v.cosine(w);
    }

// org.apache.commons.math.linear.ArrayRealVectorTest::testCosinePrecondition2
    public void testCosinePrecondition2() {
        final ArrayRealVector v = new ArrayRealVector(new double[] {0, 0, 0});
        final ArrayRealVector w = new ArrayRealVector(new double[] {1, 0, 0});
        w.cosine(v);
    }

// org.apache.commons.math.linear.ArrayRealVectorTest::testCosinePrecondition3
    public void testCosinePrecondition3() {
        final ArrayRealVector v = new ArrayRealVector(new double[] {1, 2, 3});
        final ArrayRealVector w = new ArrayRealVector(new double[] {1, 2, 3, 4});
        v.cosine(w);
    }

// org.apache.commons.math.linear.ArrayRealVectorTest::testOuterProduct
    public void testOuterProduct() {
        final ArrayRealVector u = new ArrayRealVector(new double[] {1, 2, -3});
        final ArrayRealVector v = new ArrayRealVector(new double[] {4, -2});

        final RealMatrix uv = u.outerProduct(v);

        final double tol = Math.ulp(1d);
        Assert.assertEquals(4, uv.getEntry(0, 0), tol);
        Assert.assertEquals(-2, uv.getEntry(0, 1), tol);
        Assert.assertEquals(8, uv.getEntry(1, 0), tol);
        Assert.assertEquals(-4, uv.getEntry(1, 1), tol);
        Assert.assertEquals(-12, uv.getEntry(2, 0), tol);
        Assert.assertEquals(6, uv.getEntry(2, 1), tol);
    }

// org.apache.commons.math.linear.ArrayRealVectorTest::testCombinePreconditionSameType
    public void testCombinePreconditionSameType() {
        final double a = 1d;
        final double b = 2d;
        double[] aux = new double[] { 3d, 4d, 5d };
        final RealVector x = new ArrayRealVector(aux, false);
        aux = new double[] { 6d, 7d };
        final RealVector y = new ArrayRealVector(aux, false);
        x.combine(a, b, y);
    }

// org.apache.commons.math.linear.ArrayRealVectorTest::testCombineSameType
    public void testCombineSameType() {
        final Random random = new Random(20110726);
        final int dim = 10;
        final double a = (2 * random.nextDouble() - 1);
        final double b = (2 * random.nextDouble() - 1);
        final RealVector x = new ArrayRealVector(dim);
        final RealVector y = new ArrayRealVector(dim);
        final double[] expected = new double[dim];
        for (int i = 0; i < dim; i++) {
            final double xi = 2 * random.nextDouble() - 1;
            final double yi = 2 * random.nextDouble() - 1;
            x.setEntry(i, xi);
            y.setEntry(i, yi);
            expected[i] = a * xi + b * yi;
        }
        final double[] actual = x.combine(a, b, y).toArray();
        for (int i = 0; i < dim; i++) {
            final double delta;
            if (expected[i] == 0d) {
                delta = Math.ulp(1d);
            } else {
                delta = Math.ulp(expected[i]);
            }
            Assert.assertEquals("elements [" + i + "] differ",
                                expected[i],
                                actual[i],
                                delta);
        }
    }

// org.apache.commons.math.linear.ArrayRealVectorTest::testCombinePreconditionMixedType
    public void testCombinePreconditionMixedType() {
        final double a = 1d;
        final double b = 2d;
        double[] aux = new double[] { 3d, 4d, 5d };
        final RealVector x = new ArrayRealVector(aux, false);
        aux = new double[] { 6d, 7d };
        final RealVector y = new OpenMapRealVector(aux);
        x.combine(a, b, y);
    }

// org.apache.commons.math.linear.ArrayRealVectorTest::testCombineMixedTypes
    public void testCombineMixedTypes() {
        final Random random = new Random(20110726);
        final int dim = 10;
        final double a = (2 * random.nextDouble() - 1);
        final double b = (2 * random.nextDouble() - 1);
        final RealVector x = new ArrayRealVector(dim);
        final RealVector y = new OpenMapRealVector(dim, 0d);
        final double[] expected = new double[dim];
        for (int i = 0; i < dim; i++) {
            final double xi = 2 * random.nextDouble() - 1;
            final double yi = 2 * random.nextDouble() - 1;
            x.setEntry(i, xi);
            y.setEntry(i, yi);
            expected[i] = a * xi + b * yi;
        }
        final double[] actual = x.combine(a, b, y).toArray();
        for (int i = 0; i < dim; i++) {
            final double delta;
            if (expected[i] == 0d) {
                delta = Math.ulp(1d);
            } else {
                delta = Math.ulp(expected[i]);
            }
            Assert.assertEquals("elements [" + i + "] differ",
                                expected[i],
                                actual[i],
                                delta);
        }
    }

// org.apache.commons.math.linear.ArrayRealVectorTest::testCombineToSelfPreconditionSameType
    public void testCombineToSelfPreconditionSameType() {
        final double a = 1d;
        final double b = 2d;
        double[] aux = new double[] { 3d, 4d, 5d };
        final RealVector x = new ArrayRealVector(aux, false);
        aux = new double[] { 6d, 7d };
        final RealVector y = new ArrayRealVector(aux, false);
        x.combineToSelf(a, b, y);
    }

// org.apache.commons.math.linear.ArrayRealVectorTest::testCombineToSelfSameType
    public void testCombineToSelfSameType() {
        final Random random = new Random(20110726);
        final int dim = 10;
        final double a = (2 * random.nextDouble() - 1);
        final double b = (2 * random.nextDouble() - 1);
        final RealVector x = new ArrayRealVector(dim);
        final RealVector y = new ArrayRealVector(dim);
        final double[] expected = new double[dim];
        for (int i = 0; i < dim; i++) {
            final double xi = 2 * random.nextDouble() - 1;
            final double yi = 2 * random.nextDouble() - 1;
            x.setEntry(i, xi);
            y.setEntry(i, yi);
            expected[i] = a * xi + b * yi;
        }
        Assert.assertSame(x, x.combineToSelf(a, b, y));
        final double[] actual = x.toArray();
        for (int i = 0; i < dim; i++) {
            final double delta;
            if (expected[i] == 0d) {
                delta = Math.ulp(1d);
            } else {
                delta = Math.ulp(expected[i]);
            }
            Assert.assertEquals("elements [" + i + "] differ",
                                expected[i],
                                actual[i],
                                delta);
        }
    }

// org.apache.commons.math.linear.ArrayRealVectorTest::testCombineToSelfPreconditionMixedType
    public void testCombineToSelfPreconditionMixedType() {
        final double a = 1d;
        final double b = 2d;
        double[] aux = new double[] { 3d, 4d, 5d };
        final RealVector x = new ArrayRealVector(aux, false);
        aux = new double[] { 6d, 7d };
        final RealVector y = new OpenMapRealVector(aux);
        x.combineToSelf(a, b, y);
    }

// org.apache.commons.math.linear.ArrayRealVectorTest::testCombineToSelfMixedTypes
    public void testCombineToSelfMixedTypes() {
        final Random random = new Random(20110726);
        final int dim = 10;
        final double a = (2 * random.nextDouble() - 1);
        final double b = (2 * random.nextDouble() - 1);
        final RealVector x = new ArrayRealVector(dim);
        final RealVector y = new OpenMapRealVector(dim, 0d);
        final double[] expected = new double[dim];
        for (int i = 0; i < dim; i++) {
            final double xi = 2 * random.nextDouble() - 1;
            final double yi = 2 * random.nextDouble() - 1;
            x.setEntry(i, xi);
            y.setEntry(i, yi);
            expected[i] = a * xi + b * yi;
        }
        Assert.assertSame(x, x.combineToSelf(a, b, y));
        final double[] actual = x.toArray();
        for (int i = 0; i < dim; i++) {
            final double delta;
            if (expected[i] == 0d) {
                delta = Math.ulp(1d);
            } else {
                delta = Math.ulp(expected[i]);
            }
            Assert.assertEquals("elements [" + i + "] differ",
                                expected[i],
                                actual[i],
                                delta);
        }
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testDimensions
    public void testDimensions() {
        BlockRealMatrix m = new BlockRealMatrix(testData);
        BlockRealMatrix m2 = new BlockRealMatrix(testData2);
        Assert.assertEquals("testData row dimension",3,m.getRowDimension());
        Assert.assertEquals("testData column dimension",3,m.getColumnDimension());
        Assert.assertTrue("testData is square",m.isSquare());
        Assert.assertEquals("testData2 row dimension",m2.getRowDimension(),2);
        Assert.assertEquals("testData2 column dimension",m2.getColumnDimension(),3);
        Assert.assertTrue("testData2 is not square",!m2.isSquare());
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testCopyFunctions
    public void testCopyFunctions() {
        Random r = new Random(66636328996002l);
        BlockRealMatrix m1 = createRandomMatrix(r, 47, 83);
        BlockRealMatrix m2 = new BlockRealMatrix(m1.getData());
        Assert.assertEquals(m1, m2);
        BlockRealMatrix m3 = new BlockRealMatrix(testData);
        BlockRealMatrix m4 = new BlockRealMatrix(m3.getData());
        Assert.assertEquals(m3, m4);
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testAdd
    public void testAdd() {
        BlockRealMatrix m = new BlockRealMatrix(testData);
        BlockRealMatrix mInv = new BlockRealMatrix(testDataInv);
        RealMatrix mPlusMInv = m.add(mInv);
        double[][] sumEntries = mPlusMInv.getData();
        for (int row = 0; row < m.getRowDimension(); row++) {
            for (int col = 0; col < m.getColumnDimension(); col++) {
                Assert.assertEquals("sum entry entry",
                    testDataPlusInv[row][col],sumEntries[row][col],
                        entryTolerance);
            }
        }
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testAddFail
    public void testAddFail() {
        BlockRealMatrix m = new BlockRealMatrix(testData);
        BlockRealMatrix m2 = new BlockRealMatrix(testData2);
        try {
            m.add(m2);
            Assert.fail("MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testNorm
    public void testNorm() {
        BlockRealMatrix m = new BlockRealMatrix(testData);
        BlockRealMatrix m2 = new BlockRealMatrix(testData2);
        Assert.assertEquals("testData norm",14d,m.getNorm(),entryTolerance);
        Assert.assertEquals("testData2 norm",7d,m2.getNorm(),entryTolerance);
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testFrobeniusNorm
    public void testFrobeniusNorm() {
        BlockRealMatrix m = new BlockRealMatrix(testData);
        BlockRealMatrix m2 = new BlockRealMatrix(testData2);
        Assert.assertEquals("testData Frobenius norm", FastMath.sqrt(117.0), m.getFrobeniusNorm(), entryTolerance);
        Assert.assertEquals("testData2 Frobenius norm", FastMath.sqrt(52.0), m2.getFrobeniusNorm(), entryTolerance);
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testPlusMinus
    public void testPlusMinus() {
        BlockRealMatrix m = new BlockRealMatrix(testData);
        BlockRealMatrix m2 = new BlockRealMatrix(testDataInv);
        assertClose(m.subtract(m2), m2.scalarMultiply(-1d).add(m), entryTolerance);
        try {
            m.subtract(new BlockRealMatrix(testData2));
            Assert.fail("Expecting illegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testMultiply
    public void testMultiply() {
        BlockRealMatrix m = new BlockRealMatrix(testData);
        BlockRealMatrix mInv = new BlockRealMatrix(testDataInv);
        BlockRealMatrix identity = new BlockRealMatrix(id);
        BlockRealMatrix m2 = new BlockRealMatrix(testData2);
        assertClose(m.multiply(mInv), identity, entryTolerance);
        assertClose(mInv.multiply(m), identity, entryTolerance);
        assertClose(m.multiply(identity), m, entryTolerance);
        assertClose(identity.multiply(mInv), mInv, entryTolerance);
        assertClose(m2.multiply(identity), m2, entryTolerance);
        try {
            m.multiply(new BlockRealMatrix(bigSingular));
            Assert.fail("Expecting illegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testSeveralBlocks
    public void testSeveralBlocks() {
        RealMatrix m = new BlockRealMatrix(35, 71);
        for (int i = 0; i < m.getRowDimension(); ++i) {
            for (int j = 0; j < m.getColumnDimension(); ++j) {
                m.setEntry(i, j, i + j / 1024.0);
            }
        }

        RealMatrix mT = m.transpose();
        Assert.assertEquals(m.getRowDimension(), mT.getColumnDimension());
        Assert.assertEquals(m.getColumnDimension(), mT.getRowDimension());
        for (int i = 0; i < mT.getRowDimension(); ++i) {
            for (int j = 0; j < mT.getColumnDimension(); ++j) {
                Assert.assertEquals(m.getEntry(j, i), mT.getEntry(i, j), 0);
            }
        }

        RealMatrix mPm = m.add(m);
        for (int i = 0; i < mPm.getRowDimension(); ++i) {
            for (int j = 0; j < mPm.getColumnDimension(); ++j) {
                Assert.assertEquals(2 * m.getEntry(i, j), mPm.getEntry(i, j), 0);
            }
        }

        RealMatrix mPmMm = mPm.subtract(m);
        for (int i = 0; i < mPmMm.getRowDimension(); ++i) {
            for (int j = 0; j < mPmMm.getColumnDimension(); ++j) {
                Assert.assertEquals(m.getEntry(i, j), mPmMm.getEntry(i, j), 0);
            }
        }

        RealMatrix mTm = mT.multiply(m);
        for (int i = 0; i < mTm.getRowDimension(); ++i) {
            for (int j = 0; j < mTm.getColumnDimension(); ++j) {
                double sum = 0;
                for (int k = 0; k < mT.getColumnDimension(); ++k) {
                    sum += (k + i / 1024.0) * (k + j / 1024.0);
                }
                Assert.assertEquals(sum, mTm.getEntry(i, j), 0);
            }
        }

        RealMatrix mmT = m.multiply(mT);
        for (int i = 0; i < mmT.getRowDimension(); ++i) {
            for (int j = 0; j < mmT.getColumnDimension(); ++j) {
                double sum = 0;
                for (int k = 0; k < m.getColumnDimension(); ++k) {
                    sum += (i + k / 1024.0) * (j + k / 1024.0);
                }
                Assert.assertEquals(sum, mmT.getEntry(i, j), 0);
            }
        }

        RealMatrix sub1 = m.getSubMatrix(2, 9, 5, 20);
        for (int i = 0; i < sub1.getRowDimension(); ++i) {
            for (int j = 0; j < sub1.getColumnDimension(); ++j) {
                Assert.assertEquals((i + 2) + (j + 5) / 1024.0, sub1.getEntry(i, j), 0);
            }
        }

        RealMatrix sub2 = m.getSubMatrix(10, 12, 3, 70);
        for (int i = 0; i < sub2.getRowDimension(); ++i) {
            for (int j = 0; j < sub2.getColumnDimension(); ++j) {
                Assert.assertEquals((i + 10) + (j + 3) / 1024.0, sub2.getEntry(i, j), 0);
            }
        }

        RealMatrix sub3 = m.getSubMatrix(30, 34, 0, 5);
        for (int i = 0; i < sub3.getRowDimension(); ++i) {
            for (int j = 0; j < sub3.getColumnDimension(); ++j) {
                Assert.assertEquals((i + 30) + (j + 0) / 1024.0, sub3.getEntry(i, j), 0);
            }
        }

        RealMatrix sub4 = m.getSubMatrix(30, 32, 62, 65);
        for (int i = 0; i < sub4.getRowDimension(); ++i) {
            for (int j = 0; j < sub4.getColumnDimension(); ++j) {
                Assert.assertEquals((i + 30) + (j + 62) / 1024.0, sub4.getEntry(i, j), 0);
            }
        }

    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testMultiply2
    public void testMultiply2() {
        RealMatrix m3 = new BlockRealMatrix(d3);
        RealMatrix m4 = new BlockRealMatrix(d4);
        RealMatrix m5 = new BlockRealMatrix(d5);
        assertClose(m3.multiply(m4), m5, entryTolerance);
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testTrace
    public void testTrace() {
        RealMatrix m = new BlockRealMatrix(id);
        Assert.assertEquals("identity trace",3d,m.getTrace(),entryTolerance);
        m = new BlockRealMatrix(testData2);
        try {
            m.getTrace();
            Assert.fail("Expecting NonSquareMatrixException");
        } catch (NonSquareMatrixException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testScalarAdd
    public void testScalarAdd() {
        RealMatrix m = new BlockRealMatrix(testData);
        assertClose(new BlockRealMatrix(testDataPlus2), m.scalarAdd(2d), entryTolerance);
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testOperate
    public void testOperate() {
        RealMatrix m = new BlockRealMatrix(id);
        assertClose(testVector, m.operate(testVector), entryTolerance);
        assertClose(testVector, m.operate(new ArrayRealVector(testVector)).toArray(), entryTolerance);
        m = new BlockRealMatrix(bigSingular);
        try {
            m.operate(testVector);
            Assert.fail("Expecting illegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testOperateLarge
    public void testOperateLarge() {
        int p = (7 * BlockRealMatrix.BLOCK_SIZE) / 2;
        int q = (5 * BlockRealMatrix.BLOCK_SIZE) / 2;
        int r =  3 * BlockRealMatrix.BLOCK_SIZE;
        Random random = new Random(111007463902334l);
        RealMatrix m1 = createRandomMatrix(random, p, q);
        RealMatrix m2 = createRandomMatrix(random, q, r);
        RealMatrix m1m2 = m1.multiply(m2);
        for (int i = 0; i < r; ++i) {
            checkArrays(m1m2.getColumn(i), m1.operate(m2.getColumn(i)));
        }
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testOperatePremultiplyLarge
    public void testOperatePremultiplyLarge() {
        int p = (7 * BlockRealMatrix.BLOCK_SIZE) / 2;
        int q = (5 * BlockRealMatrix.BLOCK_SIZE) / 2;
        int r =  3 * BlockRealMatrix.BLOCK_SIZE;
        Random random = new Random(111007463902334l);
        RealMatrix m1 = createRandomMatrix(random, p, q);
        RealMatrix m2 = createRandomMatrix(random, q, r);
        RealMatrix m1m2 = m1.multiply(m2);
        for (int i = 0; i < p; ++i) {
            checkArrays(m1m2.getRow(i), m2.preMultiply(m1.getRow(i)));
        }
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testMath209
    public void testMath209() {
        RealMatrix a = new BlockRealMatrix(new double[][] {
                { 1, 2 }, { 3, 4 }, { 5, 6 }
        });
        double[] b = a.operate(new double[] { 1, 1 });
        Assert.assertEquals(a.getRowDimension(), b.length);
        Assert.assertEquals( 3.0, b[0], 1.0e-12);
        Assert.assertEquals( 7.0, b[1], 1.0e-12);
        Assert.assertEquals(11.0, b[2], 1.0e-12);
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testTranspose
    public void testTranspose() {
        RealMatrix m = new BlockRealMatrix(testData);
        RealMatrix mIT = new LUDecomposition(m).getSolver().getInverse().transpose();
        RealMatrix mTI = new LUDecomposition(m.transpose()).getSolver().getInverse();
        assertClose(mIT, mTI, normTolerance);
        m = new BlockRealMatrix(testData2);
        RealMatrix mt = new BlockRealMatrix(testData2T);
        assertClose(mt, m.transpose(), normTolerance);
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testPremultiplyVector
    public void testPremultiplyVector() {
        RealMatrix m = new BlockRealMatrix(testData);
        assertClose(m.preMultiply(testVector), preMultTest, normTolerance);
        assertClose(m.preMultiply(new ArrayRealVector(testVector).toArray()),
                    preMultTest, normTolerance);
        m = new BlockRealMatrix(bigSingular);
        try {
            m.preMultiply(testVector);
            Assert.fail("expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testPremultiply
    public void testPremultiply() {
        RealMatrix m3 = new BlockRealMatrix(d3);
        RealMatrix m4 = new BlockRealMatrix(d4);
        RealMatrix m5 = new BlockRealMatrix(d5);
        assertClose(m4.preMultiply(m3), m5, entryTolerance);

        BlockRealMatrix m = new BlockRealMatrix(testData);
        BlockRealMatrix mInv = new BlockRealMatrix(testDataInv);
        BlockRealMatrix identity = new BlockRealMatrix(id);
        assertClose(m.preMultiply(mInv), identity, entryTolerance);
        assertClose(mInv.preMultiply(m), identity, entryTolerance);
        assertClose(m.preMultiply(identity), m, entryTolerance);
        assertClose(identity.preMultiply(mInv), mInv, entryTolerance);
        try {
            m.preMultiply(new BlockRealMatrix(bigSingular));
            Assert.fail("Expecting illegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testGetVectors
    public void testGetVectors() {
        RealMatrix m = new BlockRealMatrix(testData);
        assertClose(m.getRow(0), testDataRow1, entryTolerance);
        assertClose(m.getColumn(2), testDataCol3, entryTolerance);
        try {
            m.getRow(10);
            Assert.fail("expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.getColumn(-1);
            Assert.fail("expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testGetEntry
    public void testGetEntry() {
        RealMatrix m = new BlockRealMatrix(testData);
        Assert.assertEquals("get entry",m.getEntry(0,1),2d,entryTolerance);
        try {
            m.getEntry(10, 4);
            Assert.fail ("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testExamples
    public void testExamples() {
        
        double[][] matrixData = { {1d,2d,3d}, {2d,5d,3d}};
        RealMatrix m = new BlockRealMatrix(matrixData);
        
        double[][] matrixData2 = { {1d,2d}, {2d,5d}, {1d, 7d}};
        RealMatrix n = new BlockRealMatrix(matrixData2);
        
        RealMatrix p = m.multiply(n);
        Assert.assertEquals(2, p.getRowDimension());
        Assert.assertEquals(2, p.getColumnDimension());
        
        RealMatrix pInverse = new LUDecomposition(p).getSolver().getInverse();
        Assert.assertEquals(2, pInverse.getRowDimension());
        Assert.assertEquals(2, pInverse.getColumnDimension());

        
        double[][] coefficientsData = {{2, 3, -2}, {-1, 7, 6}, {4, -3, -5}};
        RealMatrix coefficients = new BlockRealMatrix(coefficientsData);
        RealVector constants = new ArrayRealVector(new double[]{1, -2, 1}, false);
        RealVector solution = new LUDecomposition(coefficients).getSolver().solve(constants);
        final double cst0 = constants.getEntry(0);
        final double cst1 = constants.getEntry(1);
        final double cst2 = constants.getEntry(2);
        final double sol0 = solution.getEntry(0);
        final double sol1 = solution.getEntry(1);
        final double sol2 = solution.getEntry(2);
        Assert.assertEquals(2 * sol0 + 3 * sol1 -2 * sol2, cst0, 1E-12);
        Assert.assertEquals(-1 * sol0 + 7 * sol1 + 6 * sol2, cst1, 1E-12);
        Assert.assertEquals(4 * sol0 - 3 * sol1 -5 * sol2, cst2, 1E-12);
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testGetSubMatrix
    public void testGetSubMatrix() {
        RealMatrix m = new BlockRealMatrix(subTestData);
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

// org.apache.commons.math.linear.BlockRealMatrixTest::testGetSetMatrixLarge
    public void testGetSetMatrixLarge() {
        int n = 3 * BlockRealMatrix.BLOCK_SIZE;
        RealMatrix m = new BlockRealMatrix(n, n);
        RealMatrix sub = new BlockRealMatrix(n - 4, n - 4).scalarAdd(1);

        m.setSubMatrix(sub.getData(), 2, 2);
        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < n; ++j) {
                if ((i < 2) || (i > n - 3) || (j < 2) || (j > n - 3)) {
                    Assert.assertEquals(0.0, m.getEntry(i, j), 0.0);
                } else {
                    Assert.assertEquals(1.0, m.getEntry(i, j), 0.0);
                }
            }
        }
        Assert.assertEquals(sub, m.getSubMatrix(2, n - 3, 2, n - 3));

    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testCopySubMatrix
    public void testCopySubMatrix() {
        RealMatrix m = new BlockRealMatrix(subTestData);
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

// org.apache.commons.math.linear.BlockRealMatrixTest::testGetRowMatrix
    public void testGetRowMatrix() {
        RealMatrix m     = new BlockRealMatrix(subTestData);
        RealMatrix mRow0 = new BlockRealMatrix(subRow0);
        RealMatrix mRow3 = new BlockRealMatrix(subRow3);
        Assert.assertEquals("Row0", mRow0, m.getRowMatrix(0));
        Assert.assertEquals("Row3", mRow3, m.getRowMatrix(3));
        try {
            m.getRowMatrix(-1);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.getRowMatrix(4);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testSetRowMatrix
    public void testSetRowMatrix() {
        RealMatrix m = new BlockRealMatrix(subTestData);
        RealMatrix mRow3 = new BlockRealMatrix(subRow3);
        Assert.assertNotSame(mRow3, m.getRowMatrix(0));
        m.setRowMatrix(0, mRow3);
        Assert.assertEquals(mRow3, m.getRowMatrix(0));
        try {
            m.setRowMatrix(-1, mRow3);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.setRowMatrix(0, m);
            Assert.fail("Expecting MatrixDimensionMismatchException");
        } catch (MatrixDimensionMismatchException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testGetSetRowMatrixLarge
    public void testGetSetRowMatrixLarge() {
        int n = 3 * BlockRealMatrix.BLOCK_SIZE;
        RealMatrix m = new BlockRealMatrix(n, n);
        RealMatrix sub = new BlockRealMatrix(1, n).scalarAdd(1);

        m.setRowMatrix(2, sub);
        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < n; ++j) {
                if (i != 2) {
                    Assert.assertEquals(0.0, m.getEntry(i, j), 0.0);
                } else {
                    Assert.assertEquals(1.0, m.getEntry(i, j), 0.0);
                }
            }
        }
        Assert.assertEquals(sub, m.getRowMatrix(2));
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testGetColumnMatrix
    public void testGetColumnMatrix() {
        RealMatrix m = new BlockRealMatrix(subTestData);
        RealMatrix mColumn1 = new BlockRealMatrix(subColumn1);
        RealMatrix mColumn3 = new BlockRealMatrix(subColumn3);
        Assert.assertEquals(mColumn1, m.getColumnMatrix(1));
        Assert.assertEquals(mColumn3, m.getColumnMatrix(3));
        try {
            m.getColumnMatrix(-1);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.getColumnMatrix(4);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testSetColumnMatrix
    public void testSetColumnMatrix() {
        RealMatrix m = new BlockRealMatrix(subTestData);
        RealMatrix mColumn3 = new BlockRealMatrix(subColumn3);
        Assert.assertNotSame(mColumn3, m.getColumnMatrix(1));
        m.setColumnMatrix(1, mColumn3);
        Assert.assertEquals(mColumn3, m.getColumnMatrix(1));
        try {
            m.setColumnMatrix(-1, mColumn3);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.setColumnMatrix(0, m);
            Assert.fail("Expecting MatrixDimensionMismatchException");
        } catch (MatrixDimensionMismatchException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testGetSetColumnMatrixLarge
    public void testGetSetColumnMatrixLarge() {
        int n = 3 * BlockRealMatrix.BLOCK_SIZE;
        RealMatrix m = new BlockRealMatrix(n, n);
        RealMatrix sub = new BlockRealMatrix(n, 1).scalarAdd(1);

        m.setColumnMatrix(2, sub);
        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < n; ++j) {
                if (j != 2) {
                    Assert.assertEquals(0.0, m.getEntry(i, j), 0.0);
                } else {
                    Assert.assertEquals(1.0, m.getEntry(i, j), 0.0);
                }
            }
        }
        Assert.assertEquals(sub, m.getColumnMatrix(2));

    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testGetRowVector
    public void testGetRowVector() {
        RealMatrix m = new BlockRealMatrix(subTestData);
        RealVector mRow0 = new ArrayRealVector(subRow0[0]);
        RealVector mRow3 = new ArrayRealVector(subRow3[0]);
        Assert.assertEquals(mRow0, m.getRowVector(0));
        Assert.assertEquals(mRow3, m.getRowVector(3));
        try {
            m.getRowVector(-1);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.getRowVector(4);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testSetRowVector
    public void testSetRowVector() {
        RealMatrix m = new BlockRealMatrix(subTestData);
        RealVector mRow3 = new ArrayRealVector(subRow3[0]);
        Assert.assertNotSame(mRow3, m.getRowMatrix(0));
        m.setRowVector(0, mRow3);
        Assert.assertEquals(mRow3, m.getRowVector(0));
        try {
            m.setRowVector(-1, mRow3);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.setRowVector(0, new ArrayRealVector(5));
            Assert.fail("Expecting MatrixDimensionMismatchException");
        } catch (MatrixDimensionMismatchException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testGetSetRowVectorLarge
    public void testGetSetRowVectorLarge() {
        int n = 3 * BlockRealMatrix.BLOCK_SIZE;
        RealMatrix m = new BlockRealMatrix(n, n);
        RealVector sub = new ArrayRealVector(n, 1.0);

        m.setRowVector(2, sub);
        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < n; ++j) {
                if (i != 2) {
                    Assert.assertEquals(0.0, m.getEntry(i, j), 0.0);
                } else {
                    Assert.assertEquals(1.0, m.getEntry(i, j), 0.0);
                }
            }
        }
        Assert.assertEquals(sub, m.getRowVector(2));
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testGetColumnVector
    public void testGetColumnVector() {
        RealMatrix m = new BlockRealMatrix(subTestData);
        RealVector mColumn1 = columnToVector(subColumn1);
        RealVector mColumn3 = columnToVector(subColumn3);
        Assert.assertEquals(mColumn1, m.getColumnVector(1));
        Assert.assertEquals(mColumn3, m.getColumnVector(3));
        try {
            m.getColumnVector(-1);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.getColumnVector(4);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testSetColumnVector
    public void testSetColumnVector() {
        RealMatrix m = new BlockRealMatrix(subTestData);
        RealVector mColumn3 = columnToVector(subColumn3);
        Assert.assertNotSame(mColumn3, m.getColumnVector(1));
        m.setColumnVector(1, mColumn3);
        Assert.assertEquals(mColumn3, m.getColumnVector(1));
        try {
            m.setColumnVector(-1, mColumn3);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.setColumnVector(0, new ArrayRealVector(5));
            Assert.fail("Expecting MatrixDimensionMismatchException");
        } catch (MatrixDimensionMismatchException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testGetSetColumnVectorLarge
    public void testGetSetColumnVectorLarge() {
        int n = 3 * BlockRealMatrix.BLOCK_SIZE;
        RealMatrix m = new BlockRealMatrix(n, n);
        RealVector sub = new ArrayRealVector(n, 1.0);

        m.setColumnVector(2, sub);
        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < n; ++j) {
                if (j != 2) {
                    Assert.assertEquals(0.0, m.getEntry(i, j), 0.0);
                } else {
                    Assert.assertEquals(1.0, m.getEntry(i, j), 0.0);
                }
            }
        }
        Assert.assertEquals(sub, m.getColumnVector(2));
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testGetRow
    public void testGetRow() {
        RealMatrix m = new BlockRealMatrix(subTestData);
        checkArrays(subRow0[0], m.getRow(0));
        checkArrays(subRow3[0], m.getRow(3));
        try {
            m.getRow(-1);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.getRow(4);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testSetRow
    public void testSetRow() {
        RealMatrix m = new BlockRealMatrix(subTestData);
        Assert.assertTrue(subRow3[0][0] != m.getRow(0)[0]);
        m.setRow(0, subRow3[0]);
        checkArrays(subRow3[0], m.getRow(0));
        try {
            m.setRow(-1, subRow3[0]);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.setRow(0, new double[5]);
            Assert.fail("Expecting MatrixDimensionMismatchException");
        } catch (MatrixDimensionMismatchException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testGetSetRowLarge
    public void testGetSetRowLarge() {
        int n = 3 * BlockRealMatrix.BLOCK_SIZE;
        RealMatrix m = new BlockRealMatrix(n, n);
        double[] sub = new double[n];
        Arrays.fill(sub, 1.0);

        m.setRow(2, sub);
        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < n; ++j) {
                if (i != 2) {
                    Assert.assertEquals(0.0, m.getEntry(i, j), 0.0);
                } else {
                    Assert.assertEquals(1.0, m.getEntry(i, j), 0.0);
                }
            }
        }
        checkArrays(sub, m.getRow(2));
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testGetColumn
    public void testGetColumn() {
        RealMatrix m = new BlockRealMatrix(subTestData);
        double[] mColumn1 = columnToArray(subColumn1);
        double[] mColumn3 = columnToArray(subColumn3);
        checkArrays(mColumn1, m.getColumn(1));
        checkArrays(mColumn3, m.getColumn(3));
        try {
            m.getColumn(-1);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.getColumn(4);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testSetColumn
    public void testSetColumn() {
        RealMatrix m = new BlockRealMatrix(subTestData);
        double[] mColumn3 = columnToArray(subColumn3);
        Assert.assertTrue(mColumn3[0] != m.getColumn(1)[0]);
        m.setColumn(1, mColumn3);
        checkArrays(mColumn3, m.getColumn(1));
        try {
            m.setColumn(-1, mColumn3);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.setColumn(0, new double[5]);
            Assert.fail("Expecting MatrixDimensionMismatchException");
        } catch (MatrixDimensionMismatchException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testGetSetColumnLarge
    public void testGetSetColumnLarge() {
        int n = 3 * BlockRealMatrix.BLOCK_SIZE;
        RealMatrix m = new BlockRealMatrix(n, n);
        double[] sub = new double[n];
        Arrays.fill(sub, 1.0);

        m.setColumn(2, sub);
        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < n; ++j) {
                if (j != 2) {
                    Assert.assertEquals(0.0, m.getEntry(i, j), 0.0);
                } else {
                    Assert.assertEquals(1.0, m.getEntry(i, j), 0.0);
                }
            }
        }
        checkArrays(sub, m.getColumn(2));
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testEqualsAndHashCode
    public void testEqualsAndHashCode() {
        BlockRealMatrix m = new BlockRealMatrix(testData);
        BlockRealMatrix m1 = m.copy();
        BlockRealMatrix mt = m.transpose();
        Assert.assertTrue(m.hashCode() != mt.hashCode());
        Assert.assertEquals(m.hashCode(), m1.hashCode());
        Assert.assertEquals(m, m);
        Assert.assertEquals(m, m1);
        Assert.assertFalse(m.equals(null));
        Assert.assertFalse(m.equals(mt));
        Assert.assertFalse(m.equals(new BlockRealMatrix(bigSingular)));
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testToString
    public void testToString() {
        BlockRealMatrix m = new BlockRealMatrix(testData);
        Assert.assertEquals("BlockRealMatrix{{1.0,2.0,3.0},{2.0,5.0,3.0},{1.0,0.0,8.0}}",
                m.toString());
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testSetSubMatrix
    public void testSetSubMatrix() throws Exception {
        BlockRealMatrix m = new BlockRealMatrix(testData);
        m.setSubMatrix(detData2,1,1);
        RealMatrix expected = new BlockRealMatrix
            (new double[][] {{1.0,2.0,3.0},{2.0,1.0,3.0},{1.0,2.0,4.0}});
        Assert.assertEquals(expected, m);

        m.setSubMatrix(detData2,0,0);
        expected = new BlockRealMatrix
            (new double[][] {{1.0,3.0,3.0},{2.0,4.0,3.0},{1.0,2.0,4.0}});
        Assert.assertEquals(expected, m);

        m.setSubMatrix(testDataPlus2,0,0);
        expected = new BlockRealMatrix
            (new double[][] {{3.0,4.0,5.0},{4.0,7.0,5.0},{3.0,2.0,10.0}});
        Assert.assertEquals(expected, m);

        
        BlockRealMatrix matrix = new BlockRealMatrix
            (new double[][] {{1, 2, 3, 4}, {5, 6, 7, 8}, {9, 0, 1 , 2}});
        matrix.setSubMatrix(new double[][] {{3, 4}, {5, 6}}, 1, 1);
        expected = new BlockRealMatrix
            (new double[][] {{1, 2, 3, 4}, {5, 3, 4, 8}, {9, 5 ,6, 2}});
        Assert.assertEquals(expected, matrix);

        
        try {
            m.setSubMatrix(testData,1,1);
            Assert.fail("expecting OutOfRangeException");
        } catch (OutOfRangeException e) {
            
        }
        
        try {
            m.setSubMatrix(testData,-1,1);
            Assert.fail("expecting OutOfRangeException");
        } catch (OutOfRangeException e) {
            
        }
        try {
            m.setSubMatrix(testData,1,-1);
            Assert.fail("expecting OutOfRangeException");
        } catch (OutOfRangeException e) {
            
        }

        
        try {
            m.setSubMatrix(null,1,1);
            Assert.fail("expecting NullArgumentException");
        } catch (NullArgumentException e) {
            
        }

        
        try {
            m.setSubMatrix(new double[][] {{1}, {2, 3}}, 0, 0);
            Assert.fail("expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException e) {
            
        }

        
        try {
            m.setSubMatrix(new double[][] {{}}, 0, 0);
            Assert.fail("expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException e) {
            
        }
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testWalk
    public void testWalk() {
        int rows    = 150;
        int columns = 75;

        RealMatrix m = new BlockRealMatrix(rows, columns);
        m.walkInRowOrder(new SetVisitor());
        GetVisitor getVisitor = new GetVisitor();
        m.walkInOptimizedOrder(getVisitor);
        Assert.assertEquals(rows * columns, getVisitor.getCount());

        m = new BlockRealMatrix(rows, columns);
        m.walkInRowOrder(new SetVisitor(), 1, rows - 2, 1, columns - 2);
        getVisitor = new GetVisitor();
        m.walkInOptimizedOrder(getVisitor, 1, rows - 2, 1, columns - 2);
        Assert.assertEquals((rows - 2) * (columns - 2), getVisitor.getCount());
        for (int i = 0; i < rows; ++i) {
            Assert.assertEquals(0.0, m.getEntry(i, 0), 0);
            Assert.assertEquals(0.0, m.getEntry(i, columns - 1), 0);
        }
        for (int j = 0; j < columns; ++j) {
            Assert.assertEquals(0.0, m.getEntry(0, j), 0);
            Assert.assertEquals(0.0, m.getEntry(rows - 1, j), 0);
        }

        m = new BlockRealMatrix(rows, columns);
        m.walkInColumnOrder(new SetVisitor());
        getVisitor = new GetVisitor();
        m.walkInOptimizedOrder(getVisitor);
        Assert.assertEquals(rows * columns, getVisitor.getCount());

        m = new BlockRealMatrix(rows, columns);
        m.walkInColumnOrder(new SetVisitor(), 1, rows - 2, 1, columns - 2);
        getVisitor = new GetVisitor();
        m.walkInOptimizedOrder(getVisitor, 1, rows - 2, 1, columns - 2);
        Assert.assertEquals((rows - 2) * (columns - 2), getVisitor.getCount());
        for (int i = 0; i < rows; ++i) {
            Assert.assertEquals(0.0, m.getEntry(i, 0), 0);
            Assert.assertEquals(0.0, m.getEntry(i, columns - 1), 0);
        }
        for (int j = 0; j < columns; ++j) {
            Assert.assertEquals(0.0, m.getEntry(0, j), 0);
            Assert.assertEquals(0.0, m.getEntry(rows - 1, j), 0);
        }

        m = new BlockRealMatrix(rows, columns);
        m.walkInOptimizedOrder(new SetVisitor());
        getVisitor = new GetVisitor();
        m.walkInRowOrder(getVisitor);
        Assert.assertEquals(rows * columns, getVisitor.getCount());

        m = new BlockRealMatrix(rows, columns);
        m.walkInOptimizedOrder(new SetVisitor(), 1, rows - 2, 1, columns - 2);
        getVisitor = new GetVisitor();
        m.walkInRowOrder(getVisitor, 1, rows - 2, 1, columns - 2);
        Assert.assertEquals((rows - 2) * (columns - 2), getVisitor.getCount());
        for (int i = 0; i < rows; ++i) {
            Assert.assertEquals(0.0, m.getEntry(i, 0), 0);
            Assert.assertEquals(0.0, m.getEntry(i, columns - 1), 0);
        }
        for (int j = 0; j < columns; ++j) {
            Assert.assertEquals(0.0, m.getEntry(0, j), 0);
            Assert.assertEquals(0.0, m.getEntry(rows - 1, j), 0);
        }

        m = new BlockRealMatrix(rows, columns);
        m.walkInOptimizedOrder(new SetVisitor());
        getVisitor = new GetVisitor();
        m.walkInColumnOrder(getVisitor);
        Assert.assertEquals(rows * columns, getVisitor.getCount());

        m = new BlockRealMatrix(rows, columns);
        m.walkInOptimizedOrder(new SetVisitor(), 1, rows - 2, 1, columns - 2);
        getVisitor = new GetVisitor();
        m.walkInColumnOrder(getVisitor, 1, rows - 2, 1, columns - 2);
        Assert.assertEquals((rows - 2) * (columns - 2), getVisitor.getCount());
        for (int i = 0; i < rows; ++i) {
            Assert.assertEquals(0.0, m.getEntry(i, 0), 0);
            Assert.assertEquals(0.0, m.getEntry(i, columns - 1), 0);
        }
        for (int j = 0; j < columns; ++j) {
            Assert.assertEquals(0.0, m.getEntry(0, j), 0);
            Assert.assertEquals(0.0, m.getEntry(rows - 1, j), 0);
        }

    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testSerial
    public void testSerial()  {
        BlockRealMatrix m = new BlockRealMatrix(testData);
        Assert.assertEquals(m,TestUtils.serializeAndRecover(m));
    }

// org.apache.commons.math.linear.CholeskySolverTest::testSolveDimensionErrors
    public void testSolveDimensionErrors() {
        DecompositionSolver solver =
            new CholeskyDecomposition(MatrixUtils.createRealMatrix(testData)).getSolver();
        RealMatrix b = MatrixUtils.createRealMatrix(new double[2][2]);
        try {
            solver.solve(b);
            Assert.fail("an exception should have been thrown");
        } catch (MathIllegalArgumentException iae) {
            
        }
        try {
            solver.solve(b.getColumnVector(0));
            Assert.fail("an exception should have been thrown");
        } catch (MathIllegalArgumentException iae) {
            
        }
        try {
            solver.solve(new ArrayRealVectorTest.RealVectorTestImpl(b.getColumn(0)));
            Assert.fail("an exception should have been thrown");
        } catch (MathIllegalArgumentException iae) {
            
        }
    }

// org.apache.commons.math.linear.CholeskySolverTest::testSolve
    public void testSolve() {
        DecompositionSolver solver =
            new CholeskyDecomposition(MatrixUtils.createRealMatrix(testData)).getSolver();
        RealMatrix b = MatrixUtils.createRealMatrix(new double[][] {
                {   78,  -13,    1 },
                {  414,  -62,   -1 },
                { 1312, -202,  -37 },
                { 2989, -542,  145 },
                { 5510, -1465, 201 }
        });
        RealMatrix xRef = MatrixUtils.createRealMatrix(new double[][] {
                { 1,  0,  1 },
                { 0,  1,  1 },
                { 2,  1, -4 },
                { 2,  2,  2 },
                { 5, -3,  0 }
        });

        
        Assert.assertEquals(0, solver.solve(b).subtract(xRef).getNorm(), 1.0e-13);

        
        for (int i = 0; i < b.getColumnDimension(); ++i) {
            Assert.assertEquals(0,
                         solver.solve(b.getColumnVector(i)).subtract(xRef.getColumnVector(i)).getNorm(),
                         1.0e-13);
        }

        
        for (int i = 0; i < b.getColumnDimension(); ++i) {
            ArrayRealVectorTest.RealVectorTestImpl v =
                new ArrayRealVectorTest.RealVectorTestImpl(b.getColumn(i));
            Assert.assertEquals(0,
                         solver.solve(v).subtract(xRef.getColumnVector(i)).getNorm(),
                         1.0e-13);
        }

    }

// org.apache.commons.math.linear.CholeskySolverTest::testDeterminant
    public void testDeterminant() {
        Assert.assertEquals(7290000.0, getDeterminant(MatrixUtils.createRealMatrix(testData)), 1.0e-15);
    }

// org.apache.commons.math.linear.ConjugateGradientTest::testNonSquareOperator
    public void testNonSquareOperator() {
        final Array2DRowRealMatrix a = new Array2DRowRealMatrix(2, 3);
        final IterativeLinearSolver solver;
        solver = new ConjugateGradient(10, 0., false);
        final ArrayRealVector b = new ArrayRealVector(a.getRowDimension());
        final ArrayRealVector x = new ArrayRealVector(a.getColumnDimension());
        solver.solve(a, b, x);
    }

// org.apache.commons.math.linear.ConjugateGradientTest::testDimensionMismatchRightHandSide
    public void testDimensionMismatchRightHandSide() {
        final Array2DRowRealMatrix a = new Array2DRowRealMatrix(3, 3);
        final IterativeLinearSolver solver;
        solver = new ConjugateGradient(10, 0., false);
        final ArrayRealVector b = new ArrayRealVector(2);
        final ArrayRealVector x = new ArrayRealVector(3);
        solver.solve(a, b, x);
    }

// org.apache.commons.math.linear.ConjugateGradientTest::testDimensionMismatchSolution
    public void testDimensionMismatchSolution() {
        final Array2DRowRealMatrix a = new Array2DRowRealMatrix(3, 3);
        final IterativeLinearSolver solver;
        solver = new ConjugateGradient(10, 0., false);
        final ArrayRealVector b = new ArrayRealVector(3);
        final ArrayRealVector x = new ArrayRealVector(2);
        solver.solve(a, b, x);
    }

// org.apache.commons.math.linear.ConjugateGradientTest::testNonPositiveDefiniteLinearOperator
    public void testNonPositiveDefiniteLinearOperator() {
        final Array2DRowRealMatrix a = new Array2DRowRealMatrix(2, 2);
        a.setEntry(0, 0, -1.);
        a.setEntry(0, 1, 2.);
        a.setEntry(1, 0, 3.);
        a.setEntry(1, 1, 4.);
        final IterativeLinearSolver solver;
        solver = new ConjugateGradient(10, 0., true);
        final ArrayRealVector b = new ArrayRealVector(2);
        b.setEntry(0, -1.);
        b.setEntry(1, -1.);
        final ArrayRealVector x = new ArrayRealVector(2);
        solver.solve(a, b, x);
    }

// org.apache.commons.math.linear.ConjugateGradientTest::testUnpreconditionedSolution
    public void testUnpreconditionedSolution() {
        final int n = 5;
        final int maxIterations = 100;
        final RealLinearOperator a = new HilbertMatrix(n);
        final InverseHilbertMatrix ainv = new InverseHilbertMatrix(n);
        final IterativeLinearSolver solver;
        solver = new ConjugateGradient(maxIterations, 1E-10, true);
        final RealVector b = new ArrayRealVector(n);
        for (int j = 0; j < n; j++) {
            b.set(0.);
            b.setEntry(j, 1.);
            final RealVector x = solver.solve(a, b);
            for (int i = 0; i < n; i++) {
                final double actual = x.getEntry(i);
                final double expected = ainv.getEntry(i, j);
                final double delta = 1E-10 * Math.abs(expected);
                final String msg = String.format("entry[%d][%d]", i, j);
                Assert.assertEquals(msg, expected, actual, delta);
            }
        }
    }

// org.apache.commons.math.linear.ConjugateGradientTest::testUnpreconditionedInPlaceSolutionWithInitialGuess
    public void testUnpreconditionedInPlaceSolutionWithInitialGuess() {
        final int n = 5;
        final int maxIterations = 100;
        final RealLinearOperator a = new HilbertMatrix(n);
        final InverseHilbertMatrix ainv = new InverseHilbertMatrix(n);
        final IterativeLinearSolver solver;
        solver = new ConjugateGradient(maxIterations, 1E-10, true);
        final RealVector b = new ArrayRealVector(n);
        for (int j = 0; j < n; j++) {
            b.set(0.);
            b.setEntry(j, 1.);
            final RealVector x0 = new ArrayRealVector(n);
            x0.set(1.);
            final RealVector x = solver.solveInPlace(a, b, x0);
            Assert.assertSame("x should be a reference to x0", x0, x);
            for (int i = 0; i < n; i++) {
                final double actual = x.getEntry(i);
                final double expected = ainv.getEntry(i, j);
                final double delta = 1E-10 * Math.abs(expected);
                final String msg = String.format("entry[%d][%d)", i, j);
                Assert.assertEquals(msg, expected, actual, delta);
            }
        }
    }

// org.apache.commons.math.linear.ConjugateGradientTest::testUnpreconditionedSolutionWithInitialGuess
    public void testUnpreconditionedSolutionWithInitialGuess() {
        final int n = 5;
        final int maxIterations = 100;
        final RealLinearOperator a = new HilbertMatrix(n);
        final InverseHilbertMatrix ainv = new InverseHilbertMatrix(n);
        final IterativeLinearSolver solver;
        solver = new ConjugateGradient(maxIterations, 1E-10, true);
        final RealVector b = new ArrayRealVector(n);
        for (int j = 0; j < n; j++) {
            b.set(0.);
            b.setEntry(j, 1.);
            final RealVector x0 = new ArrayRealVector(n);
            x0.set(1.);
            final RealVector x = solver.solve(a, b, x0);
            Assert.assertNotSame("x should not be a reference to x0", x0, x);
            for (int i = 0; i < n; i++) {
                final double actual = x.getEntry(i);
                final double expected = ainv.getEntry(i, j);
                final double delta = 1E-10 * Math.abs(expected);
                final String msg = String.format("entry[%d][%d]", i, j);
                Assert.assertEquals(msg, expected, actual, delta);
                Assert.assertEquals(msg, x0.getEntry(i), 1., Math.ulp(1.));
            }
        }
    }

// org.apache.commons.math.linear.ConjugateGradientTest::testUnpreconditionedResidual
    public void testUnpreconditionedResidual() {
        final int n = 10;
        final int maxIterations = n;
        final RealLinearOperator a = new HilbertMatrix(n);
        final ConjugateGradient solver;
        solver = new ConjugateGradient(maxIterations, 1E-15, true);
        final RealVector r = new ArrayRealVector(n);
        final RealVector x = new ArrayRealVector(n);
        final IterationListener listener = new IterationListener() {

            public void terminationPerformed(final IterationEvent e) {
                
            }

            public void iterationStarted(final IterationEvent e) {
                
            }

            public void iterationPerformed(final IterationEvent e) {
                RealVector v = ((ProvidesResidual)e).getResidual();
                r.setSubVector(0, v);
                v = ((IterativeLinearSolverEvent) e).getSolution();
                x.setSubVector(0, v);
            }

            public void initializationPerformed(final IterationEvent e) {
                
            }
        };
        solver.getIterationManager().addIterationListener(listener);
        final RealVector b = new ArrayRealVector(n);
        for (int j = 0; j < n; j++) {
            b.set(0.);
            b.setEntry(j, 1.);

            boolean caught = false;
            try {
                solver.solve(a, b);
            } catch (MaxCountExceededException e) {
                caught = true;
                final RealVector y = a.operate(x);
                for (int i = 0; i < n; i++) {
                    final double actual = b.getEntry(i) - y.getEntry(i);
                    final double expected = r.getEntry(i);
                    final double delta = 1E-6 * Math.abs(expected);
                    final String msg = String
                        .format("column %d, residual %d", i, j);
                    Assert.assertEquals(msg, expected, actual, delta);
                }
            }
            Assert
                .assertTrue("MaxCountExceededException should have been caught",
                            caught);
        }
    }

// org.apache.commons.math.linear.ConjugateGradientTest::testNonSquarePreconditioner
    public void testNonSquarePreconditioner() {
        final Array2DRowRealMatrix a = new Array2DRowRealMatrix(2, 2);
        final InvertibleRealLinearOperator m;
        m = new InvertibleRealLinearOperator() {

            @Override
            public RealVector operate(final RealVector x) {
                throw new UnsupportedOperationException();
            }

            @Override
            public int getRowDimension() {
                return 2;
            }

            @Override
            public int getColumnDimension() {
                return 3;
            }

            @Override
            public RealVector solve(final RealVector b) {
                throw new UnsupportedOperationException();
            }
        };
        final PreconditionedIterativeLinearSolver solver;
        solver = new ConjugateGradient(10, 0d, false);
        final ArrayRealVector b = new ArrayRealVector(a.getRowDimension());
        solver.solve(a, m, b);
    }

// org.apache.commons.math.linear.ConjugateGradientTest::testMismatchedOperatorDimensions
    public void testMismatchedOperatorDimensions() {
        final Array2DRowRealMatrix a = new Array2DRowRealMatrix(2, 2);
        final InvertibleRealLinearOperator m;
        m = new InvertibleRealLinearOperator() {

            @Override
            public RealVector operate(final RealVector x) {
                throw new UnsupportedOperationException();
            }

            @Override
            public int getRowDimension() {
                return 3;
            }

            @Override
            public int getColumnDimension() {
                return 3;
            }

            @Override
            public RealVector solve(final RealVector b) {
                throw new UnsupportedOperationException();
            }
        };
        final PreconditionedIterativeLinearSolver solver;
        solver = new ConjugateGradient(10, 0d, false);
        final ArrayRealVector b = new ArrayRealVector(a.getRowDimension());
        solver.solve(a, m, b);
    }

// org.apache.commons.math.linear.ConjugateGradientTest::testNonPositiveDefinitePreconditioner
    public void testNonPositiveDefinitePreconditioner() {
        final Array2DRowRealMatrix a = new Array2DRowRealMatrix(2, 2);
        a.setEntry(0, 0, 1d);
        a.setEntry(0, 1, 2d);
        a.setEntry(1, 0, 3d);
        a.setEntry(1, 1, 4d);
        final InvertibleRealLinearOperator m;
        m = new InvertibleRealLinearOperator() {

            @Override
            public RealVector operate(final RealVector x) {
                final ArrayRealVector y = new ArrayRealVector(2);
                y.setEntry(0, -x.getEntry(0));
                y.setEntry(1, x.getEntry(1));
                return y;
            }

            @Override
            public int getRowDimension() {
                return 2;
            }

            @Override
            public int getColumnDimension() {
                return 2;
            }

            @Override
            public RealVector solve(final RealVector b) {
                final ArrayRealVector x = new ArrayRealVector(2);
                x.setEntry(0, -b.getEntry(0));
                x.setEntry(1, b.getEntry(1));
                return x;
            }
        };
        final PreconditionedIterativeLinearSolver solver;
        solver = new ConjugateGradient(10, 0d, true);
        final ArrayRealVector b = new ArrayRealVector(2);
        b.setEntry(0, -1d);
        b.setEntry(1, -1d);
        solver.solve(a, m, b);
    }

// org.apache.commons.math.linear.ConjugateGradientTest::testPreconditionedSolution
    public void testPreconditionedSolution() {
        final int n = 8;
        final int maxIterations = 100;
        final RealLinearOperator a = new HilbertMatrix(n);
        final InverseHilbertMatrix ainv = new InverseHilbertMatrix(n);
        final InvertibleRealLinearOperator m = JacobiPreconditioner.create(a);
        final PreconditionedIterativeLinearSolver solver;
        solver = new ConjugateGradient(maxIterations, 1E-15, true);
        final RealVector b = new ArrayRealVector(n);
        for (int j = 0; j < n; j++) {
            b.set(0.);
            b.setEntry(j, 1.);
            final RealVector x = solver.solve(a, m, b);
            for (int i = 0; i < n; i++) {
                final double actual = x.getEntry(i);
                final double expected = ainv.getEntry(i, j);
                final double delta = 1E-6 * Math.abs(expected);
                final String msg = String.format("coefficient (%d, %d)", i, j);
                Assert.assertEquals(msg, expected, actual, delta);
            }
        }
    }

// org.apache.commons.math.linear.ConjugateGradientTest::testPreconditionedResidual
    public void testPreconditionedResidual() {
        final int n = 10;
        final int maxIterations = n;
        final RealLinearOperator a = new HilbertMatrix(n);
        final InvertibleRealLinearOperator m = JacobiPreconditioner.create(a);
        final ConjugateGradient solver;
        solver = new ConjugateGradient(maxIterations, 1E-15, true);
        final RealVector r = new ArrayRealVector(n);
        final RealVector x = new ArrayRealVector(n);
        final IterationListener listener = new IterationListener() {

            public void terminationPerformed(final IterationEvent e) {
                
            }

            public void iterationStarted(final IterationEvent e) {
                
            }

            public void iterationPerformed(final IterationEvent e) {
                RealVector v = ((ProvidesResidual)e).getResidual();
                r.setSubVector(0, v);
                v = ((IterativeLinearSolverEvent) e).getSolution();
                x.setSubVector(0, v);
            }

            public void initializationPerformed(final IterationEvent e) {
                
            }
        };
        solver.getIterationManager().addIterationListener(listener);
        final RealVector b = new ArrayRealVector(n);

        for (int j = 0; j < n; j++) {
            b.set(0.);
            b.setEntry(j, 1.);

            boolean caught = false;
            try {
                solver.solve(a, m, b);
            } catch (MaxCountExceededException e) {
                caught = true;
                final RealVector y = a.operate(x);
                for (int i = 0; i < n; i++) {
                    final double actual = b.getEntry(i) - y.getEntry(i);
                    final double expected = r.getEntry(i);
                    final double delta = 1E-6 * Math.abs(expected);
                    final String msg = String
                        .format("column %d, residual %d", i, j);
                    Assert.assertEquals(msg, expected, actual, delta);
                }
            }
            Assert
                .assertTrue("MaxCountExceededException should have been caught",
                            caught);
        }
    }

// org.apache.commons.math.linear.ConjugateGradientTest::testPreconditionedSolution2
    public void testPreconditionedSolution2() {
        final int n = 100;
        final int maxIterations = 100000;
        final Array2DRowRealMatrix a = new Array2DRowRealMatrix(n, n);
        double daux = 1.;
        for (int i = 0; i < n; i++) {
            a.setEntry(i, i, daux);
            daux *= 1.2;
            for (int j = i + 1; j < n; j++) {
                if (i == j) {
                } else {
                    final double value = 1.0;
                    a.setEntry(i, j, value);
                    a.setEntry(j, i, value);
                }
            }
        }
        final InvertibleRealLinearOperator m = JacobiPreconditioner.create(a);
        final PreconditionedIterativeLinearSolver pcg;
        final IterativeLinearSolver cg;
        pcg = new ConjugateGradient(maxIterations, 1E-6, true);
        cg = new ConjugateGradient(maxIterations, 1E-6, true);
        final RealVector b = new ArrayRealVector(n);
        final String pattern = "preconditioned gradient (%d iterations) should"
                               + " have been faster than unpreconditioned (%d iterations)";
        String msg;
        for (int j = 0; j < 1; j++) {
            b.set(0.);
            b.setEntry(j, 1.);
            final RealVector px = pcg.solve(a, m, b);
            final RealVector x = cg.solve(a, b);
            final int npcg = pcg.getIterationManager().getIterations();
            final int ncg = cg.getIterationManager().getIterations();
            msg = String.format(pattern, npcg, ncg);
            Assert.assertTrue(msg, npcg < ncg);
            for (int i = 0; i < n; i++) {
                msg = String.format("row %d, column %d", i, j);
                final double expected = x.getEntry(i);
                final double actual = px.getEntry(i);
                final double delta = 1E-6 * Math.abs(expected);
                Assert.assertEquals(msg, expected, actual, delta);
            }
        }
    }

// org.apache.commons.math.linear.ConjugateGradientTest::testEventManagement
    public void testEventManagement() {
        final int n = 5;
        final int maxIterations = 100;
        final RealLinearOperator a = new HilbertMatrix(n);
        final IterativeLinearSolver solver;
        final int[] count = new int[] {
            0, 0, 0, 0
        };
        final IterationListener listener = new IterationListener() {

            public void initializationPerformed(final IterationEvent e) {
                count[0] = 1;
                count[1] = 0;
                count[2] = 0;
                count[3] = 0;

            }

            public void iterationPerformed(final IterationEvent e) {
                ++count[2];
            }

            public void iterationStarted(IterationEvent e) {
                ++count[1];

            }

            public void terminationPerformed(final IterationEvent e) {
                ++count[3];
            }
        };
        solver = new ConjugateGradient(maxIterations, 1E-10, true);
        solver.getIterationManager().addIterationListener(listener);
        final RealVector b = new ArrayRealVector(n);
        for (int j = 0; j < n; j++) {
            b.set(0.);
            b.setEntry(j, 1.);
            solver.solve(a, b);
            String msg = String.format("column %d (initialization)", j);
            Assert.assertEquals(msg, 1, count[0]);
            msg = String.format("column %d (iterations started)", j);
            Assert.assertEquals(msg, solver.getIterationManager()
                .getIterations() - 1, count[1]);
            msg = String.format("column %d (iterations performed)", j);
            Assert.assertEquals(msg, solver.getIterationManager()
                .getIterations() - 1, count[2]);
            msg = String.format("column %d (finalization)", j);
            Assert.assertEquals(msg, 1, count[3]);
        }
    }

// org.apache.commons.math.linear.EigenDecompositionTest::testDimension1
    public void testDimension1() {
        RealMatrix matrix =
            MatrixUtils.createRealMatrix(new double[][] { { 1.5 } });
        EigenDecomposition ed;
        ed = new EigenDecomposition(matrix, MathUtils.SAFE_MIN);
        Assert.assertEquals(1.5, ed.getRealEigenvalue(0), 1.0e-15);
    }

// org.apache.commons.math.linear.EigenDecompositionTest::testDimension2
    public void testDimension2() {
        RealMatrix matrix =
            MatrixUtils.createRealMatrix(new double[][] {
                    { 59.0, 12.0 },
                    { 12.0, 66.0 }
            });
        EigenDecomposition ed;
        ed = new EigenDecomposition(matrix, MathUtils.SAFE_MIN);
        Assert.assertEquals(75.0, ed.getRealEigenvalue(0), 1.0e-15);
        Assert.assertEquals(50.0, ed.getRealEigenvalue(1), 1.0e-15);
    }

// org.apache.commons.math.linear.EigenDecompositionTest::testDimension3
    public void testDimension3() {
        RealMatrix matrix =
            MatrixUtils.createRealMatrix(new double[][] {
                                   {  39632.0, -4824.0, -16560.0 },
                                   {  -4824.0,  8693.0,   7920.0 },
                                   { -16560.0,  7920.0,  17300.0 }
                               });
        EigenDecomposition ed;
        ed = new EigenDecomposition(matrix, MathUtils.SAFE_MIN);
        Assert.assertEquals(50000.0, ed.getRealEigenvalue(0), 3.0e-11);
        Assert.assertEquals(12500.0, ed.getRealEigenvalue(1), 3.0e-11);
        Assert.assertEquals( 3125.0, ed.getRealEigenvalue(2), 3.0e-11);
    }

// org.apache.commons.math.linear.EigenDecompositionTest::testDimension3MultipleRoot
    public void testDimension3MultipleRoot() {
        RealMatrix matrix =
            MatrixUtils.createRealMatrix(new double[][] {
                    {  5,   10,   15 },
                    { 10,   20,   30 },
                    { 15,   30,   45 }
            });
        EigenDecomposition ed;
        ed = new EigenDecomposition(matrix, MathUtils.SAFE_MIN);
        Assert.assertEquals(70.0, ed.getRealEigenvalue(0), 3.0e-11);
        Assert.assertEquals(0.0,  ed.getRealEigenvalue(1), 3.0e-11);
        Assert.assertEquals(0.0,  ed.getRealEigenvalue(2), 3.0e-11);
    }

// org.apache.commons.math.linear.EigenDecompositionTest::testDimension4WithSplit
    public void testDimension4WithSplit() {
        RealMatrix matrix =
            MatrixUtils.createRealMatrix(new double[][] {
                                   {  0.784, -0.288,  0.000,  0.000 },
                                   { -0.288,  0.616,  0.000,  0.000 },
                                   {  0.000,  0.000,  0.164, -0.048 },
                                   {  0.000,  0.000, -0.048,  0.136 }
                               });
        EigenDecomposition ed;
        ed = new EigenDecomposition(matrix, MathUtils.SAFE_MIN);
        Assert.assertEquals(1.0, ed.getRealEigenvalue(0), 1.0e-15);
        Assert.assertEquals(0.4, ed.getRealEigenvalue(1), 1.0e-15);
        Assert.assertEquals(0.2, ed.getRealEigenvalue(2), 1.0e-15);
        Assert.assertEquals(0.1, ed.getRealEigenvalue(3), 1.0e-15);
    }

// org.apache.commons.math.linear.EigenDecompositionTest::testDimension4WithoutSplit
    public void testDimension4WithoutSplit() {
        RealMatrix matrix =
            MatrixUtils.createRealMatrix(new double[][] {
                                   {  0.5608, -0.2016,  0.1152, -0.2976 },
                                   { -0.2016,  0.4432, -0.2304,  0.1152 },
                                   {  0.1152, -0.2304,  0.3088, -0.1344 },
                                   { -0.2976,  0.1152, -0.1344,  0.3872 }
                               });
        EigenDecomposition ed;
        ed = new EigenDecomposition(matrix, MathUtils.SAFE_MIN);
        Assert.assertEquals(1.0, ed.getRealEigenvalue(0), 1.0e-15);
        Assert.assertEquals(0.4, ed.getRealEigenvalue(1), 1.0e-15);
        Assert.assertEquals(0.2, ed.getRealEigenvalue(2), 1.0e-15);
        Assert.assertEquals(0.1, ed.getRealEigenvalue(3), 1.0e-15);
    }

// org.apache.commons.math.linear.EigenDecompositionTest::testMath308
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
            new ArrayRealVector(new double[] { -0.713933751051495,  0.190582113553930, -0.671410443368332,  0.056056055955050, -0.006541576993581 }),
            new ArrayRealVector(new double[] { -0.584677060845929,  0.367177264979103,  0.721453187784497, -0.052971054621812,  0.005740715188257 })
        };

        EigenDecomposition decomposition;
        decomposition = new EigenDecomposition(mainTridiagonal,
                                                   secondaryTridiagonal,
                                                   MathUtils.SAFE_MIN);

        double[] eigenValues = decomposition.getRealEigenvalues();
        for (int i = 0; i < refEigenValues.length; ++i) {
            Assert.assertEquals(refEigenValues[i], eigenValues[i], 1.0e-5);
            Assert.assertEquals(0, refEigenVectors[i].subtract(decomposition.getEigenvector(i)).getNorm(), 2.0e-7);
        }

    }

// org.apache.commons.math.linear.EigenDecompositionTest::testMathpbx02
    public void testMathpbx02() {

        double[] mainTridiagonal = {
              7484.860960227216, 18405.28129035345, 13855.225609560746,
             10016.708722343366, 559.8117399576674, 6750.190788301587,
                71.21428769782159
        };
        double[] secondaryTridiagonal = {
             -4175.088570476366,1975.7955858241994,5193.178422374075,
              1995.286659169179,75.34535882933804,-234.0808002076056
        };

        
        
        double[] refEigenValues = {
                20654.744890306974412,16828.208208485466457,
                6893.155912634994820,6757.083016675340332,
                5887.799885688558788,64.309089923240379,
                57.992628792736340
        };
        RealVector[] refEigenVectors = {
                new ArrayRealVector(new double[] {-0.270356342026904, 0.852811091326997, 0.399639490702077, 0.198794657813990, 0.019739323307666, 0.000106983022327, -0.000001216636321}),
                new ArrayRealVector(new double[] {0.179995273578326,-0.402807848153042,0.701870993525734,0.555058211014888,0.068079148898236,0.000509139115227,-0.000007112235617}),
                new ArrayRealVector(new double[] {-0.399582721284727,-0.056629954519333,-0.514406488522827,0.711168164518580,0.225548081276367,0.125943999652923,-0.004321507456014}),
                new ArrayRealVector(new double[] {0.058515721572821,0.010200130057739,0.063516274916536,-0.090696087449378,-0.017148420432597,0.991318870265707,-0.034707338554096}),
                new ArrayRealVector(new double[] {0.855205995537564,0.327134656629775,-0.265382397060548,0.282690729026706,0.105736068025572,-0.009138126622039,0.000367751821196}),
                new ArrayRealVector(new double[] {-0.002913069901144,-0.005177515777101,0.041906334478672,-0.109315918416258,0.436192305456741,0.026307315639535,0.891797507436344}),
                new ArrayRealVector(new double[] {-0.005738311176435,-0.010207611670378,0.082662420517928,-0.215733886094368,0.861606487840411,-0.025478530652759,-0.451080697503958})
        };

        
        EigenDecomposition decomposition;
        decomposition = new EigenDecomposition(mainTridiagonal,
                                                   secondaryTridiagonal,
                                                   MathUtils.SAFE_MIN);

        double[] eigenValues = decomposition.getRealEigenvalues();
        for (int i = 0; i < refEigenValues.length; ++i) {
            Assert.assertEquals(refEigenValues[i], eigenValues[i], 1.0e-3);
            if (refEigenVectors[i].dotProduct(decomposition.getEigenvector(i)) < 0) {
                Assert.assertEquals(0, refEigenVectors[i].add(decomposition.getEigenvector(i)).getNorm(), 1.0e-5);
            } else {
                Assert.assertEquals(0, refEigenVectors[i].subtract(decomposition.getEigenvector(i)).getNorm(), 1.0e-5);
            }
        }

    }

// org.apache.commons.math.linear.EigenDecompositionTest::testMathpbx03
    public void testMathpbx03() {

        double[] mainTridiagonal = {
            1809.0978259647177,3395.4763425956166,1832.1894584712693,3804.364873592377,
            806.0482458637571,2403.656427234185,28.48691431556015
        };
        double[] secondaryTridiagonal = {
            -656.8932064545833,-469.30804108920734,-1021.7714889369421,
            -1152.540497328983,-939.9765163817368,-12.885877015422391
        };

        
        
        double[] refEigenValues = {
            4603.121913685183245,3691.195818048970978,2743.442955402465032,1657.596442107321764,
            1336.797819095331306,30.129865209677519,17.035352085224986
        };

        RealVector[] refEigenVectors = {
            new ArrayRealVector(new double[] {-0.036249830202337,0.154184732411519,-0.346016328392363,0.867540105133093,-0.294483395433451,0.125854235969548,-0.000354507444044}),
            new ArrayRealVector(new double[] {-0.318654191697157,0.912992309960507,-0.129270874079777,-0.184150038178035,0.096521712579439,-0.070468788536461,0.000247918177736}),
            new ArrayRealVector(new double[] {-0.051394668681147,0.073102235876933,0.173502042943743,-0.188311980310942,-0.327158794289386,0.905206581432676,-0.004296342252659}),
            new ArrayRealVector(new double[] {0.838150199198361,0.193305209055716,-0.457341242126146,-0.166933875895419,0.094512811358535,0.119062381338757,-0.000941755685226}),
            new ArrayRealVector(new double[] {0.438071395458547,0.314969169786246,0.768480630802146,0.227919171600705,-0.193317045298647,-0.170305467485594,0.001677380536009}),
            new ArrayRealVector(new double[] {-0.003726503878741,-0.010091946369146,-0.067152015137611,-0.113798146542187,-0.313123000097908,-0.118940107954918,0.932862311396062}),
            new ArrayRealVector(new double[] {0.009373003194332,0.025570377559400,0.170955836081348,0.291954519805750,0.807824267665706,0.320108347088646,0.360202112392266}),
        };

        
        EigenDecomposition decomposition;
        decomposition = new EigenDecomposition(mainTridiagonal,
                                                   secondaryTridiagonal,
                                                   MathUtils.SAFE_MIN);

        double[] eigenValues = decomposition.getRealEigenvalues();
        for (int i = 0; i < refEigenValues.length; ++i) {
            Assert.assertEquals(refEigenValues[i], eigenValues[i], 1.0e-4);
            if (refEigenVectors[i].dotProduct(decomposition.getEigenvector(i)) < 0) {
                Assert.assertEquals(0, refEigenVectors[i].add(decomposition.getEigenvector(i)).getNorm(), 1.0e-5);
            } else {
                Assert.assertEquals(0, refEigenVectors[i].subtract(decomposition.getEigenvector(i)).getNorm(), 1.0e-5);
            }
        }

    }

// org.apache.commons.math.linear.EigenDecompositionTest::testTridiagonal
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
        EigenDecomposition ed;
        ed = new EigenDecomposition(t.getMainDiagonalRef(),
                                        t.getSecondaryDiagonalRef(),
                                        MathUtils.SAFE_MIN);
        double[] eigenValues = ed.getRealEigenvalues();
        Assert.assertEquals(ref.length, eigenValues.length);
        for (int i = 0; i < ref.length; ++i) {
            Assert.assertEquals(ref[ref.length - i - 1], eigenValues[i], 2.0e-14);
        }

    }

// org.apache.commons.math.linear.EigenDecompositionTest::testDimensions
    public void testDimensions() {
        final int m = matrix.getRowDimension();
        EigenDecomposition ed;
        ed = new EigenDecomposition(matrix, MathUtils.SAFE_MIN);
        Assert.assertEquals(m, ed.getV().getRowDimension());
        Assert.assertEquals(m, ed.getV().getColumnDimension());
        Assert.assertEquals(m, ed.getD().getColumnDimension());
        Assert.assertEquals(m, ed.getD().getColumnDimension());
        Assert.assertEquals(m, ed.getVT().getRowDimension());
        Assert.assertEquals(m, ed.getVT().getColumnDimension());
    }

// org.apache.commons.math.linear.EigenDecompositionTest::testEigenvalues
    public void testEigenvalues() {
        EigenDecomposition ed;
        ed = new EigenDecomposition(matrix, MathUtils.SAFE_MIN);
        double[] eigenValues = ed.getRealEigenvalues();
        Assert.assertEquals(refValues.length, eigenValues.length);
        for (int i = 0; i < refValues.length; ++i) {
            Assert.assertEquals(refValues[i], eigenValues[i], 3.0e-15);
        }
    }

// org.apache.commons.math.linear.EigenDecompositionTest::testBigMatrix
    public void testBigMatrix() {
        Random r = new Random(17748333525117l);
        double[] bigValues = new double[200];
        for (int i = 0; i < bigValues.length; ++i) {
            bigValues[i] = 2 * r.nextDouble() - 1;
        }
        Arrays.sort(bigValues);
        EigenDecomposition ed;
        ed = new EigenDecomposition(createTestMatrix(r, bigValues),
                                        MathUtils.SAFE_MIN);
        double[] eigenValues = ed.getRealEigenvalues();
        Assert.assertEquals(bigValues.length, eigenValues.length);
        for (int i = 0; i < bigValues.length; ++i) {
            Assert.assertEquals(bigValues[bigValues.length - i - 1], eigenValues[i], 2.0e-14);
        }
    }

// org.apache.commons.math.linear.EigenDecompositionTest::testEigenvectors
    public void testEigenvectors() {
        EigenDecomposition ed;
        ed = new EigenDecomposition(matrix, MathUtils.SAFE_MIN);
        for (int i = 0; i < matrix.getRowDimension(); ++i) {
            double lambda = ed.getRealEigenvalue(i);
            RealVector v  = ed.getEigenvector(i);
            RealVector mV = matrix.operate(v);
            Assert.assertEquals(0, mV.subtract(v.mapMultiplyToSelf(lambda)).getNorm(), 1.0e-13);
        }
    }

// org.apache.commons.math.linear.EigenDecompositionTest::testAEqualVDVt
    public void testAEqualVDVt() {
        EigenDecomposition ed;
        ed = new EigenDecomposition(matrix, MathUtils.SAFE_MIN);
        RealMatrix v  = ed.getV();
        RealMatrix d  = ed.getD();
        RealMatrix vT = ed.getVT();
        double norm = v.multiply(d).multiply(vT).subtract(matrix).getNorm();
        Assert.assertEquals(0, norm, 6.0e-13);
    }

// org.apache.commons.math.linear.EigenDecompositionTest::testVOrthogonal
    public void testVOrthogonal() {
        RealMatrix v = new EigenDecomposition(matrix, MathUtils.SAFE_MIN).getV();
        RealMatrix vTv = v.transpose().multiply(v);
        RealMatrix id  = MatrixUtils.createRealIdentityMatrix(vTv.getRowDimension());
        Assert.assertEquals(0, vTv.subtract(id).getNorm(), 2.0e-13);
    }

// org.apache.commons.math.linear.EigenDecompositionTest::testDiagonal
    public void testDiagonal() {
        double[] diagonal = new double[] { -3.0, -2.0, 2.0, 5.0 };
        RealMatrix m = createDiagonalMatrix(diagonal, diagonal.length, diagonal.length);
        EigenDecomposition ed;
        ed = new EigenDecomposition(m, MathUtils.SAFE_MIN);
        Assert.assertEquals(diagonal[0], ed.getRealEigenvalue(3), 2.0e-15);
        Assert.assertEquals(diagonal[1], ed.getRealEigenvalue(2), 2.0e-15);
        Assert.assertEquals(diagonal[2], ed.getRealEigenvalue(1), 2.0e-15);
        Assert.assertEquals(diagonal[3], ed.getRealEigenvalue(0), 2.0e-15);
    }

// org.apache.commons.math.linear.EigenDecompositionTest::testRepeatedEigenvalue
    public void testRepeatedEigenvalue() {
        RealMatrix repeated = MatrixUtils.createRealMatrix(new double[][] {
                {3,  2,  4},
                {2,  0,  2},
                {4,  2,  3}
        });
        EigenDecomposition ed;
        ed = new EigenDecomposition(repeated, MathUtils.SAFE_MIN);
        checkEigenValues((new double[] {8, -1, -1}), ed, 1E-12);
        checkEigenVector((new double[] {2, 1, 2}), ed, 1E-12);
    }

// org.apache.commons.math.linear.EigenDecompositionTest::testDistinctEigenvalues
    public void testDistinctEigenvalues() {
        RealMatrix distinct = MatrixUtils.createRealMatrix(new double[][] {
                {3, 1, -4},
                {1, 3, -4},
                {-4, -4, 8}
        });
        EigenDecomposition ed;
        ed = new EigenDecomposition(distinct, MathUtils.SAFE_MIN);
        checkEigenValues((new double[] {2, 0, 12}), ed, 1E-12);
        checkEigenVector((new double[] {1, -1, 0}), ed, 1E-12);
        checkEigenVector((new double[] {1, 1, 1}), ed, 1E-12);
        checkEigenVector((new double[] {-1, -1, 2}), ed, 1E-12);
    }

// org.apache.commons.math.linear.EigenDecompositionTest::testZeroDivide
    public void testZeroDivide() {
        RealMatrix indefinite = MatrixUtils.createRealMatrix(new double [][] {
                { 0.0, 1.0, -1.0 },
                { 1.0, 1.0, 0.0 },
                { -1.0,0.0, 1.0 }
        });
        EigenDecomposition ed;
        ed = new EigenDecomposition(indefinite, MathUtils.SAFE_MIN);
        checkEigenValues((new double[] {2, 1, -1}), ed, 1E-12);
        double isqrt3 = 1/FastMath.sqrt(3.0);
        checkEigenVector((new double[] {isqrt3,isqrt3,-isqrt3}), ed, 1E-12);
        double isqrt2 = 1/FastMath.sqrt(2.0);
        checkEigenVector((new double[] {0.0,-isqrt2,-isqrt2}), ed, 1E-12);
        double isqrt6 = 1/FastMath.sqrt(6.0);
        checkEigenVector((new double[] {2*isqrt6,-isqrt6,isqrt6}), ed, 1E-12);
    }

// org.apache.commons.math.linear.EigenSolverTest::testNonInvertible
    public void testNonInvertible() {
        Random r = new Random(9994100315209l);
        RealMatrix m =
            EigenDecompositionTest.createTestMatrix(r, new double[] { 1.0, 0.0, -1.0, -2.0, -3.0 });
        DecompositionSolver es = new EigenDecomposition(m, MathUtils.SAFE_MIN).getSolver();
        Assert.assertFalse(es.isNonSingular());
        try {
            es.getInverse();
            Assert.fail("an exception should have been thrown");
        } catch (SingularMatrixException ime) {
            
        }
    }

// org.apache.commons.math.linear.EigenSolverTest::testInvertible
    public void testInvertible() {
        Random r = new Random(9994100315209l);
        RealMatrix m =
            EigenDecompositionTest.createTestMatrix(r, new double[] { 1.0, 0.5, -1.0, -2.0, -3.0 });
        DecompositionSolver es = new EigenDecomposition(m, MathUtils.SAFE_MIN).getSolver();
        Assert.assertTrue(es.isNonSingular());
        RealMatrix inverse = es.getInverse();
        RealMatrix error =
            m.multiply(inverse).subtract(MatrixUtils.createRealIdentityMatrix(m.getRowDimension()));
        Assert.assertEquals(0, error.getNorm(), 4.0e-15);
    }

// org.apache.commons.math.linear.EigenSolverTest::testSolveDimensionErrors
    public void testSolveDimensionErrors() {
        final double[] refValues = new double[] {
            2.003, 2.002, 2.001, 1.001, 1.000, 0.001
        };
        final RealMatrix matrix = EigenDecompositionTest.createTestMatrix(new Random(35992629946426l), refValues);

        DecompositionSolver es = new EigenDecomposition(matrix, MathUtils.SAFE_MIN).getSolver();
        RealMatrix b = MatrixUtils.createRealMatrix(new double[2][2]);
        try {
            es.solve(b);
            Assert.fail("an exception should have been thrown");
        } catch (MathIllegalArgumentException iae) {
            
        }
        try {
            es.solve(b.getColumnVector(0));
            Assert.fail("an exception should have been thrown");
        } catch (MathIllegalArgumentException iae) {
            
        }
        try {
            es.solve(new ArrayRealVectorTest.RealVectorTestImpl(b.getColumn(0)));
            Assert.fail("an exception should have been thrown");
        } catch (MathIllegalArgumentException iae) {
            
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
        DecompositionSolver es = new EigenDecomposition(m, MathUtils.SAFE_MIN).getSolver();
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

        
        RealMatrix solution=es.solve(b);
        Assert.assertEquals(0, solution.subtract(xRef).getNorm(), 2.5e-12);

        
        for (int i = 0; i < b.getColumnDimension(); ++i) {
            Assert.assertEquals(0,
                         es.solve(b.getColumnVector(i)).subtract(xRef.getColumnVector(i)).getNorm(),
                         2.0e-11);
        }

        
        for (int i = 0; i < b.getColumnDimension(); ++i) {
            ArrayRealVectorTest.RealVectorTestImpl v =
                new ArrayRealVectorTest.RealVectorTestImpl(b.getColumn(i));
            Assert.assertEquals(0,
                         es.solve(v).subtract(xRef.getColumnVector(i)).getNorm(),
                         2.0e-11);
        }
    }

// org.apache.commons.math.linear.LUSolverTest::testThreshold
    public void testThreshold() {
        final RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
                                                       { 1.0, 2.0, 3.0},
                                                       { 2.0, 5.0, 3.0},
                                                       { 4.000001, 9.0, 9.0}
                                                     });
        Assert.assertFalse(new LUDecomposition(matrix, 1.0e-5).getSolver().isNonSingular());
        Assert.assertTrue(new LUDecomposition(matrix, 1.0e-10).getSolver().isNonSingular());
    }

// org.apache.commons.math.linear.LUSolverTest::testSingular
    public void testSingular() {
        DecompositionSolver solver =
            new LUDecomposition(MatrixUtils.createRealMatrix(testData)).getSolver();
        Assert.assertTrue(solver.isNonSingular());
        solver = new LUDecomposition(MatrixUtils.createRealMatrix(singular)).getSolver();
        Assert.assertFalse(solver.isNonSingular());
        solver = new LUDecomposition(MatrixUtils.createRealMatrix(bigSingular)).getSolver();
        Assert.assertFalse(solver.isNonSingular());
    }

// org.apache.commons.math.linear.LUSolverTest::testSolveDimensionErrors
    public void testSolveDimensionErrors() {
        DecompositionSolver solver =
            new LUDecomposition(MatrixUtils.createRealMatrix(testData)).getSolver();
        RealMatrix b = MatrixUtils.createRealMatrix(new double[2][2]);
        try {
            solver.solve(b);
            Assert.fail("an exception should have been thrown");
        } catch (MathIllegalArgumentException iae) {
            
        }
        try {
            solver.solve(b.getColumnVector(0));
            Assert.fail("an exception should have been thrown");
        } catch (MathIllegalArgumentException iae) {
            
        }
        try {
            solver.solve(new ArrayRealVectorTest.RealVectorTestImpl(b.getColumn(0)));
            Assert.fail("an exception should have been thrown");
        } catch (MathIllegalArgumentException iae) {
            
        }
    }

// org.apache.commons.math.linear.LUSolverTest::testSolveSingularityErrors
    public void testSolveSingularityErrors() {
        DecompositionSolver solver =
            new LUDecomposition(MatrixUtils.createRealMatrix(singular)).getSolver();
        RealMatrix b = MatrixUtils.createRealMatrix(new double[2][2]);
        try {
            solver.solve(b);
            Assert.fail("an exception should have been thrown");
        } catch (SingularMatrixException ime) {
            
        }
        try {
            solver.solve(b.getColumnVector(0));
            Assert.fail("an exception should have been thrown");
        } catch (SingularMatrixException ime) {
            
        }
        try {
            solver.solve(new ArrayRealVectorTest.RealVectorTestImpl(b.getColumn(0)));
            Assert.fail("an exception should have been thrown");
        } catch (SingularMatrixException ime) {
            
        }
    }

// org.apache.commons.math.linear.LUSolverTest::testSolve
    public void testSolve() {
        DecompositionSolver solver =
            new LUDecomposition(MatrixUtils.createRealMatrix(testData)).getSolver();
        RealMatrix b = MatrixUtils.createRealMatrix(new double[][] {
                { 1, 0 }, { 2, -5 }, { 3, 1 }
        });
        RealMatrix xRef = MatrixUtils.createRealMatrix(new double[][] {
                { 19, -71 }, { -6, 22 }, { -2, 9 }
        });

        
        Assert.assertEquals(0, solver.solve(b).subtract(xRef).getNorm(), 1.0e-13);

        
        for (int i = 0; i < b.getColumnDimension(); ++i) {
            Assert.assertEquals(0,
                         solver.solve(b.getColumnVector(i)).subtract(xRef.getColumnVector(i)).getNorm(),
                         1.0e-13);
        }

        
        for (int i = 0; i < b.getColumnDimension(); ++i) {
            ArrayRealVectorTest.RealVectorTestImpl v =
                new ArrayRealVectorTest.RealVectorTestImpl(b.getColumn(i));
            Assert.assertEquals(0,
                         solver.solve(v).subtract(xRef.getColumnVector(i)).getNorm(),
                         1.0e-13);
        }
    }

// org.apache.commons.math.linear.LUSolverTest::testDeterminant
    public void testDeterminant() {
        Assert.assertEquals( -1, getDeterminant(MatrixUtils.createRealMatrix(testData)), 1.0e-15);
        Assert.assertEquals(-10, getDeterminant(MatrixUtils.createRealMatrix(luData)), 1.0e-14);
        Assert.assertEquals(  0, getDeterminant(MatrixUtils.createRealMatrix(singular)), 1.0e-17);
        Assert.assertEquals(  0, getDeterminant(MatrixUtils.createRealMatrix(bigSingular)), 1.0e-10);
    }

// org.apache.commons.math.linear.MatrixUtilsTest::testCreateRealMatrix
    public void testCreateRealMatrix() {
        Assert.assertEquals(new BlockRealMatrix(testData),
                MatrixUtils.createRealMatrix(testData));
        try {
            MatrixUtils.createRealMatrix(new double[][] {{1}, {1,2}});  
            Assert.fail("Expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
        try {
            MatrixUtils.createRealMatrix(new double[][] {{}, {}});  
            Assert.fail("Expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
        try {
            MatrixUtils.createRealMatrix(null);  
            Assert.fail("Expecting NullArgumentException");
        } catch (NullArgumentException ex) {
            
        }
    }

// org.apache.commons.math.linear.MatrixUtilsTest::testcreateFieldMatrix
    public void testcreateFieldMatrix() {
        Assert.assertEquals(new Array2DRowFieldMatrix<Fraction>(asFraction(testData)),
                     MatrixUtils.createFieldMatrix(asFraction(testData)));
        Assert.assertEquals(new Array2DRowFieldMatrix<Fraction>(FractionField.getInstance(), fractionColMatrix),
                     MatrixUtils.createFieldMatrix(fractionColMatrix));
        try {
            MatrixUtils.createFieldMatrix(asFraction(new double[][] {{1}, {1,2}}));  
            Assert.fail("Expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
        try {
            MatrixUtils.createFieldMatrix(asFraction(new double[][] {{}, {}}));  
            Assert.fail("Expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
        try {
            MatrixUtils.createFieldMatrix((Fraction[][])null);  
            Assert.fail("Expecting NullArgumentException");
        } catch (NullArgumentException ex) {
            
        }
    }

// org.apache.commons.math.linear.MatrixUtilsTest::testCreateRowRealMatrix
    public void testCreateRowRealMatrix() {
        Assert.assertEquals(MatrixUtils.createRowRealMatrix(row),
                     new BlockRealMatrix(rowMatrix));
        try {
            MatrixUtils.createRowRealMatrix(new double[] {});  
            Assert.fail("Expecting NotStrictlyPositiveException");
        } catch (NotStrictlyPositiveException ex) {
            
        }
        try {
            MatrixUtils.createRowRealMatrix(null);  
            Assert.fail("Expecting NullArgumentException");
        } catch (NullArgumentException ex) {
            
        }
    }

// org.apache.commons.math.linear.MatrixUtilsTest::testCreateRowFieldMatrix
    public void testCreateRowFieldMatrix() {
        Assert.assertEquals(MatrixUtils.createRowFieldMatrix(asFraction(row)),
                     new Array2DRowFieldMatrix<Fraction>(asFraction(rowMatrix)));
        Assert.assertEquals(MatrixUtils.createRowFieldMatrix(fractionRow),
                     new Array2DRowFieldMatrix<Fraction>(fractionRowMatrix));
        try {
            MatrixUtils.createRowFieldMatrix(new Fraction[] {});  
            Assert.fail("Expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
        try {
            MatrixUtils.createRowFieldMatrix((Fraction[]) null);  
            Assert.fail("Expecting NullArgumentException");
        } catch (NullArgumentException ex) {
            
        }
    }

// org.apache.commons.math.linear.MatrixUtilsTest::testCreateColumnRealMatrix
    public void testCreateColumnRealMatrix() {
        Assert.assertEquals(MatrixUtils.createColumnRealMatrix(col),
                     new BlockRealMatrix(colMatrix));
        try {
            MatrixUtils.createColumnRealMatrix(new double[] {});  
            Assert.fail("Expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
        try {
            MatrixUtils.createColumnRealMatrix(null);  
            Assert.fail("Expecting NullArgumentException");
        } catch (NullArgumentException ex) {
            
        }
    }

// org.apache.commons.math.linear.MatrixUtilsTest::testCreateColumnFieldMatrix
    public void testCreateColumnFieldMatrix() {
        Assert.assertEquals(MatrixUtils.createColumnFieldMatrix(asFraction(col)),
                     new Array2DRowFieldMatrix<Fraction>(asFraction(colMatrix)));
        Assert.assertEquals(MatrixUtils.createColumnFieldMatrix(fractionCol),
                     new Array2DRowFieldMatrix<Fraction>(fractionColMatrix));

        try {
            MatrixUtils.createColumnFieldMatrix(new Fraction[] {});  
            Assert.fail("Expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
        try {
            MatrixUtils.createColumnFieldMatrix((Fraction[]) null);  
            Assert.fail("Expecting NullArgumentException");
        } catch (NullArgumentException ex) {
            
        }
    }

// org.apache.commons.math.linear.MatrixUtilsTest::testCreateIdentityMatrix
    public void testCreateIdentityMatrix() {
        checkIdentityMatrix(MatrixUtils.createRealIdentityMatrix(3));
        checkIdentityMatrix(MatrixUtils.createRealIdentityMatrix(2));
        checkIdentityMatrix(MatrixUtils.createRealIdentityMatrix(1));
        try {
            MatrixUtils.createRealIdentityMatrix(0);
            Assert.fail("Expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.linear.MatrixUtilsTest::testcreateFieldIdentityMatrix
    public void testcreateFieldIdentityMatrix() {
        checkIdentityFieldMatrix(MatrixUtils.createFieldIdentityMatrix(FractionField.getInstance(), 3));
        checkIdentityFieldMatrix(MatrixUtils.createFieldIdentityMatrix(FractionField.getInstance(), 2));
        checkIdentityFieldMatrix(MatrixUtils.createFieldIdentityMatrix(FractionField.getInstance(), 1));
        try {
            MatrixUtils.createRealIdentityMatrix(0);
            Assert.fail("Expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.linear.MatrixUtilsTest::testBigFractionConverter
    public void testBigFractionConverter() {
        BigFraction[][] bfData = {
                { new BigFraction(1), new BigFraction(2), new BigFraction(3) },
                { new BigFraction(2), new BigFraction(5), new BigFraction(3) },
                { new BigFraction(1), new BigFraction(0), new BigFraction(8) }
        };
        FieldMatrix<BigFraction> m = new Array2DRowFieldMatrix<BigFraction>(bfData, false);
        RealMatrix converted = MatrixUtils.bigFractionMatrixToRealMatrix(m);
        RealMatrix reference = new Array2DRowRealMatrix(testData, false);
        Assert.assertEquals(0.0, converted.subtract(reference).getNorm(), 0.0);
    }

// org.apache.commons.math.linear.MatrixUtilsTest::testFractionConverter
    public void testFractionConverter() {
        Fraction[][] fData = {
                { new Fraction(1), new Fraction(2), new Fraction(3) },
                { new Fraction(2), new Fraction(5), new Fraction(3) },
                { new Fraction(1), new Fraction(0), new Fraction(8) }
        };
        FieldMatrix<Fraction> m = new Array2DRowFieldMatrix<Fraction>(fData, false);
        RealMatrix converted = MatrixUtils.fractionMatrixToRealMatrix(m);
        RealMatrix reference = new Array2DRowRealMatrix(testData, false);
        Assert.assertEquals(0.0, converted.subtract(reference).getNorm(), 0.0);
    }

// org.apache.commons.math.linear.MatrixUtilsTest::testSolveLowerTriangularSystem
    public void testSolveLowerTriangularSystem(){
        RealMatrix rm = new Array2DRowRealMatrix(
                new double[][] { {2,0,0,0 }, { 1,1,0,0 }, { 3,3,3,0 }, { 3,3,3,4 } },
                       false);
        RealVector b = new ArrayRealVector(new double[] { 2,3,4,8 }, false);
        MatrixUtils.solveLowerTriangularSystem(rm, b);
        TestUtils.assertEquals( new double[]{1,2,-1.66666666666667, 1.0}  , b.toArray() , 1.0e-12);
    }

// org.apache.commons.math.linear.MatrixUtilsTest::testSolveUpperTriangularSystem
    public void testSolveUpperTriangularSystem(){
        RealMatrix rm = new Array2DRowRealMatrix(
                new double[][] { {1,2,3 }, { 0,1,1 }, { 0,0,2 } },
                       false);
        RealVector b = new ArrayRealVector(new double[] { 8,4,2 }, false);
        MatrixUtils.solveUpperTriangularSystem(rm, b);
        TestUtils.assertEquals( new double[]{-1,3,1}  , b.toArray() , 1.0e-12);
    }

// org.apache.commons.math.linear.OpenMapRealMatrixTest::testMath679
    public void testMath679() {
        new OpenMapRealMatrix(3, Integer.MAX_VALUE);
    }

// org.apache.commons.math.linear.PivotingQRSolverTest::testRank
    public void testRank() throws ConvergenceException {
        DecompositionSolver solver =
            new PivotingQRDecomposition(MatrixUtils.createRealMatrix(testData3x3NonSingular)).getSolver();
        Assert.assertTrue(solver.isNonSingular());

        solver = new PivotingQRDecomposition(MatrixUtils.createRealMatrix(testData3x3Singular)).getSolver();
        Assert.assertFalse(solver.isNonSingular());

        solver = new PivotingQRDecomposition(MatrixUtils.createRealMatrix(testData3x4)).getSolver();
        Assert.assertTrue(solver.isNonSingular());

        solver = new PivotingQRDecomposition(MatrixUtils.createRealMatrix(testData4x3)).getSolver();
        Assert.assertTrue(solver.isNonSingular());

    }

// org.apache.commons.math.linear.PivotingQRSolverTest::testSolveDimensionErrors
    public void testSolveDimensionErrors() throws ConvergenceException {
        DecompositionSolver solver =
            new PivotingQRDecomposition(MatrixUtils.createRealMatrix(testData3x3NonSingular)).getSolver();
        RealMatrix b = MatrixUtils.createRealMatrix(new double[2][2]);
        try {
            solver.solve(b);
            Assert.fail("an exception should have been thrown");
        } catch (MathIllegalArgumentException iae) {
            
        }
        try {
            solver.solve(b.getColumnVector(0));
            Assert.fail("an exception should have been thrown");
        } catch (MathIllegalArgumentException iae) {
            
        }
    }

// org.apache.commons.math.linear.PivotingQRSolverTest::testSolveRankErrors
    public void testSolveRankErrors() throws ConvergenceException {
        DecompositionSolver solver =
            new PivotingQRDecomposition(MatrixUtils.createRealMatrix(testData3x3Singular)).getSolver();
        RealMatrix b = MatrixUtils.createRealMatrix(new double[3][2]);
        try {
            solver.solve(b);
            Assert.fail("an exception should have been thrown");
        } catch (SingularMatrixException iae) {
            
        }
        try {
            solver.solve(b.getColumnVector(0));
            Assert.fail("an exception should have been thrown");
        } catch (SingularMatrixException iae) {
            
        }
    }

// org.apache.commons.math.linear.PivotingQRSolverTest::testSolve
    public void testSolve() throws ConvergenceException {
        PivotingQRDecomposition decomposition =
            new PivotingQRDecomposition(MatrixUtils.createRealMatrix(testData3x3NonSingular));
        DecompositionSolver solver = decomposition.getSolver();
        RealMatrix b = MatrixUtils.createRealMatrix(new double[][] {
                { -102, 12250 }, { 544, 24500 }, { 167, -36750 }
        });

        RealMatrix xRef = MatrixUtils.createRealMatrix(new double[][] {
                { 1, 2515 }, { 2, 422 }, { -3, 898 }
        });

        
        Assert.assertEquals(0, solver.solve(b).subtract(xRef).getNorm(), 2.0e-14 * xRef.getNorm());

        
        for (int i = 0; i < b.getColumnDimension(); ++i) {
            final RealVector x = solver.solve(b.getColumnVector(i));
            final double error = x.subtract(xRef.getColumnVector(i)).getNorm();
            Assert.assertEquals(0, error, 3.0e-14 * xRef.getColumnVector(i).getNorm());
        }

        
        for (int i = 0; i < b.getColumnDimension(); ++i) {
            ArrayRealVectorTest.RealVectorTestImpl v =
                new ArrayRealVectorTest.RealVectorTestImpl(b.getColumn(i));
            final RealVector x = solver.solve(v);
            final double error = x.subtract(xRef.getColumnVector(i)).getNorm();
            Assert.assertEquals(0, error, 3.0e-14 * xRef.getColumnVector(i).getNorm());
        }

    }

// org.apache.commons.math.linear.PivotingQRSolverTest::testOverdetermined
    public void testOverdetermined() throws ConvergenceException {
        final Random r    = new Random(5559252868205245l);
        int          p    = (7 * BlockRealMatrix.BLOCK_SIZE) / 4;
        int          q    = (5 * BlockRealMatrix.BLOCK_SIZE) / 4;
        RealMatrix   a    = createTestMatrix(r, p, q);
        RealMatrix   xRef = createTestMatrix(r, q, BlockRealMatrix.BLOCK_SIZE + 3);

        
        RealMatrix b = a.multiply(xRef);
        final double noise = 0.001;
        b.walkInOptimizedOrder(new DefaultRealMatrixChangingVisitor() {
            @Override
            public double visit(int row, int column, double value) {
                return value * (1.0 + noise * (2 * r.nextDouble() - 1));
            }
        });

        
        RealMatrix x = new PivotingQRDecomposition(a).getSolver().solve(b);
        Assert.assertEquals(0, x.subtract(xRef).getNorm(), 0.01 * noise * p * q);

    }

// org.apache.commons.math.linear.PivotingQRSolverTest::testUnderdetermined
    public void testUnderdetermined() throws ConvergenceException {
        final Random r    = new Random(42185006424567123l);
        int          p    = (5 * BlockRealMatrix.BLOCK_SIZE) / 4;
        int          q    = (7 * BlockRealMatrix.BLOCK_SIZE) / 4;
        RealMatrix   a    = createTestMatrix(r, p, q);
        RealMatrix   xRef = createTestMatrix(r, q, BlockRealMatrix.BLOCK_SIZE + 3);
        RealMatrix   b    = a.multiply(xRef);
        PivotingQRDecomposition pqr = new PivotingQRDecomposition(a);
        RealMatrix   x = pqr.getSolver().solve(b);
        Assert.assertTrue(x.subtract(xRef).getNorm() / (p * q) > 0.01);
        int count=0;
        for( int i = 0 ; i < q; i++){
            if(  x.getRowVector(i).getNorm() == 0.0 ){
                ++count;
            }
        }
        Assert.assertEquals("Zeroed rows", q-p, count);
    }

// org.apache.commons.math.linear.QRSolverTest::testRank
    public void testRank() {
        DecompositionSolver solver =
            new QRDecomposition(MatrixUtils.createRealMatrix(testData3x3NonSingular)).getSolver();
        Assert.assertTrue(solver.isNonSingular());

        solver = new QRDecomposition(MatrixUtils.createRealMatrix(testData3x3Singular)).getSolver();
        Assert.assertFalse(solver.isNonSingular());

        solver = new QRDecomposition(MatrixUtils.createRealMatrix(testData3x4)).getSolver();
        Assert.assertTrue(solver.isNonSingular());

        solver = new QRDecomposition(MatrixUtils.createRealMatrix(testData4x3)).getSolver();
        Assert.assertTrue(solver.isNonSingular());

    }

// org.apache.commons.math.linear.QRSolverTest::testSolveDimensionErrors
    public void testSolveDimensionErrors() {
        DecompositionSolver solver =
            new QRDecomposition(MatrixUtils.createRealMatrix(testData3x3NonSingular)).getSolver();
        RealMatrix b = MatrixUtils.createRealMatrix(new double[2][2]);
        try {
            solver.solve(b);
            Assert.fail("an exception should have been thrown");
        } catch (MathIllegalArgumentException iae) {
            
        }
        try {
            solver.solve(b.getColumnVector(0));
            Assert.fail("an exception should have been thrown");
        } catch (MathIllegalArgumentException iae) {
            
        }
    }

// org.apache.commons.math.linear.QRSolverTest::testSolveRankErrors
    public void testSolveRankErrors() {
        DecompositionSolver solver =
            new QRDecomposition(MatrixUtils.createRealMatrix(testData3x3Singular)).getSolver();
        RealMatrix b = MatrixUtils.createRealMatrix(new double[3][2]);
        try {
            solver.solve(b);
            Assert.fail("an exception should have been thrown");
        } catch (SingularMatrixException iae) {
            
        }
        try {
            solver.solve(b.getColumnVector(0));
            Assert.fail("an exception should have been thrown");
        } catch (SingularMatrixException iae) {
            
        }
    }

// org.apache.commons.math.linear.QRSolverTest::testSolve
    public void testSolve() {
        QRDecomposition decomposition =
            new QRDecomposition(MatrixUtils.createRealMatrix(testData3x3NonSingular));
        DecompositionSolver solver = decomposition.getSolver();
        RealMatrix b = MatrixUtils.createRealMatrix(new double[][] {
                { -102, 12250 }, { 544, 24500 }, { 167, -36750 }
        });
        RealMatrix xRef = MatrixUtils.createRealMatrix(new double[][] {
                { 1, 2515 }, { 2, 422 }, { -3, 898 }
        });

        
        Assert.assertEquals(0, solver.solve(b).subtract(xRef).getNorm(), 2.0e-16 * xRef.getNorm());

        
        for (int i = 0; i < b.getColumnDimension(); ++i) {
            final RealVector x = solver.solve(b.getColumnVector(i));
            final double error = x.subtract(xRef.getColumnVector(i)).getNorm();
            Assert.assertEquals(0, error, 3.0e-16 * xRef.getColumnVector(i).getNorm());
        }

        
        for (int i = 0; i < b.getColumnDimension(); ++i) {
            ArrayRealVectorTest.RealVectorTestImpl v =
                new ArrayRealVectorTest.RealVectorTestImpl(b.getColumn(i));
            final RealVector x = solver.solve(v);
            final double error = x.subtract(xRef.getColumnVector(i)).getNorm();
            Assert.assertEquals(0, error, 3.0e-16 * xRef.getColumnVector(i).getNorm());
        }

    }

// org.apache.commons.math.linear.QRSolverTest::testOverdetermined
    public void testOverdetermined() {
        final Random r    = new Random(5559252868205245l);
        int          p    = (7 * BlockRealMatrix.BLOCK_SIZE) / 4;
        int          q    = (5 * BlockRealMatrix.BLOCK_SIZE) / 4;
        RealMatrix   a    = createTestMatrix(r, p, q);
        RealMatrix   xRef = createTestMatrix(r, q, BlockRealMatrix.BLOCK_SIZE + 3);

        
        RealMatrix b = a.multiply(xRef);
        final double noise = 0.001;
        b.walkInOptimizedOrder(new DefaultRealMatrixChangingVisitor() {
            @Override
            public double visit(int row, int column, double value) {
                return value * (1.0 + noise * (2 * r.nextDouble() - 1));
            }
        });

        
        RealMatrix x = new QRDecomposition(a).getSolver().solve(b);
        Assert.assertEquals(0, x.subtract(xRef).getNorm(), 0.01 * noise * p * q);

    }

// org.apache.commons.math.linear.QRSolverTest::testUnderdetermined
    public void testUnderdetermined() {
        final Random r    = new Random(42185006424567123l);
        int          p    = (5 * BlockRealMatrix.BLOCK_SIZE) / 4;
        int          q    = (7 * BlockRealMatrix.BLOCK_SIZE) / 4;
        RealMatrix   a    = createTestMatrix(r, p, q);
        RealMatrix   xRef = createTestMatrix(r, q, BlockRealMatrix.BLOCK_SIZE + 3);
        RealMatrix   b    = a.multiply(xRef);
        RealMatrix   x = new QRDecomposition(a).getSolver().solve(b);

        
        Assert.assertTrue(x.subtract(xRef).getNorm() / (p * q) > 0.01);

        
        Assert.assertEquals(0.0, x.getSubMatrix(p, q - 1, 0, x.getColumnDimension() - 1).getNorm(), 0);
    }

// org.apache.commons.math.linear.RealVectorTest::testMap
    public void testMap() throws Exception {
        double[] vec1Squared = { 1d, 4d, 9d, 16d, 25d };
        RealVector v = new TestVectorImpl(vec1.clone());
        RealVector w = v.map(new UnivariateRealFunction() { public double value(double x) { return x * x; } });
        double[] d2 = w.toArray();
        Assert.assertEquals(vec1Squared.length, d2.length);
        for(int i=0; i<vec1Squared.length; i++) {
            Assert.assertEquals(vec1Squared[i], d2[i], 0);
        }
    }

// org.apache.commons.math.linear.RealVectorTest::testIterator
    public void testIterator() throws Exception {
        RealVector v = new TestVectorImpl(vec2.clone());
        Entry e;
        int i = 0;
        for(Iterator<Entry> it = v.iterator(); it.hasNext() && (e = it.next()) != null; i++) {
            Assert.assertEquals(vec2[i], e.getValue(), 0);
        }
    }

// org.apache.commons.math.linear.RealVectorTest::testSparseIterator
    public void testSparseIterator() throws Exception {
        RealVector v = new TestVectorImpl(vec2.clone());
        Entry e;
        int i = 0;
        double[] nonDefaultV2 = { -3d, 2d, 1d };
        for(Iterator<Entry> it = v.sparseIterator(); it.hasNext() && (e = it.next()) != null; i++) {
            Assert.assertEquals(nonDefaultV2[i], e.getValue(), 0);
        }
        double [] onlyOne = {0d, 1.0, 0d};
        v = new TestVectorImpl(onlyOne);
        for(Iterator<Entry> it = v.sparseIterator(); it.hasNext() && (e = it.next()) != null; ) {
            Assert.assertEquals(onlyOne[1], e.getValue(), 0);
        }

    }

// org.apache.commons.math.linear.RealVectorTest::testClone
    public void testClone() throws Exception {
        double[] d = new double[1000000];
        Random r = new Random(1234);
        for(int i=0;i<d.length; i++) d[i] = r.nextDouble();
        Assert.assertTrue(new ArrayRealVector(d).getNorm() > 0);
        double[] c = d.clone();
        c[0] = 1;
        Assert.assertNotSame(c[0], d[0]);
        d[0] = 1;
        Assert.assertEquals(new ArrayRealVector(d).getNorm(), new ArrayRealVector(c).getNorm(), 0);
    }

// org.apache.commons.math.linear.RealVectorTest::testCombinePrecondition
    public void testCombinePrecondition() {
        final double a = 1d;
        final double b = 2d;
        double[] aux = new double[] { 3d, 4d, 5d };
        final TestVectorImpl x = new TestVectorImpl(aux);
        aux = new double[] { 6d, 7d };
        final TestVectorImpl y = new TestVectorImpl(aux);
        x.combine(a, b, y);
    }

// org.apache.commons.math.linear.RealVectorTest::testCombine
    public void testCombine() {
        final Random random = new Random(20110726);
        final int dim = 10;
        final double a = (2 * random.nextDouble() - 1);
        final double b = (2 * random.nextDouble() - 1);
        final RealVector x = new TestVectorImpl(new double[dim]);
        final RealVector y = new TestVectorImpl(new double[dim]);
        final double[] expected = new double[dim];
        for (int i = 0; i < dim; i++) {
            final double xi = 2 * random.nextDouble() - 1;
            final double yi = 2 * random.nextDouble() - 1;
            x.setEntry(i, xi);
            y.setEntry(i, yi);
            expected[i] = a * xi + b * yi;
        }
        final RealVector z = x.combine(a, b, y);
        Assert.assertTrue(z != x);
        final double[] actual = z.toArray();
        for (int i = 0; i < dim; i++) {
            final double delta;
            if (expected[i] == 0d) {
                delta = Math.ulp(1d);
            } else {
                delta = Math.ulp(expected[i]);
            }
            Assert.assertEquals("elements [" + i + "] differ", expected[i],
                                actual[i], delta);
        }
    }

// org.apache.commons.math.linear.RealVectorTest::testCombineToSelfPrecondition
    public void testCombineToSelfPrecondition() {
        final double a = 1d;
        final double b = 2d;
        double[] aux = new double[] { 3d, 4d, 5d };
        final TestVectorImpl x = new TestVectorImpl(aux);
        aux = new double[] { 6d, 7d };
        final TestVectorImpl y = new TestVectorImpl(aux);
        x.combineToSelf(a, b, y);
    }

// org.apache.commons.math.linear.RealVectorTest::testCombineToSelf
    public void testCombineToSelf() {
        final Random random = new Random(20110726);
        final int dim = 10;
        final double a = (2 * random.nextDouble() - 1);
        final double b = (2 * random.nextDouble() - 1);
        final RealVector x = new TestVectorImpl(new double[dim]);
        final RealVector y = new TestVectorImpl(new double[dim]);
        final double[] expected = new double[dim];
        for (int i = 0; i < dim; i++) {
            final double xi = 2 * random.nextDouble() - 1;
            final double yi = 2 * random.nextDouble() - 1;
            x.setEntry(i, xi);
            y.setEntry(i, yi);
            expected[i] = a * xi + b * yi;
        }
        Assert.assertSame(x, x.combineToSelf(a, b, y));
        final double[] actual = x.toArray();
        for (int i = 0; i < dim; i++) {
            final double delta;
            if (expected[i] == 0d) {
                delta = Math.ulp(1d);
            } else {
                delta = Math.ulp(expected[i]);
            }
            Assert.assertEquals("elements [" + i + "] differ", expected[i],
                                actual[i], delta);
        }
    }

// org.apache.commons.math.linear.SingularValueSolverTest::testSolveDimensionErrors
    public void testSolveDimensionErrors() {
        DecompositionSolver solver =
            new SingularValueDecomposition(MatrixUtils.createRealMatrix(testSquare)).getSolver();
        RealMatrix b = MatrixUtils.createRealMatrix(new double[3][2]);
        try {
            solver.solve(b);
            Assert.fail("an exception should have been thrown");
        } catch (MathIllegalArgumentException iae) {
            
        }
        try {
            solver.solve(b.getColumnVector(0));
            Assert.fail("an exception should have been thrown");
        } catch (MathIllegalArgumentException iae) {
            
        }
        try {
            solver.solve(new ArrayRealVectorTest.RealVectorTestImpl(b.getColumn(0)));
            Assert.fail("an exception should have been thrown");
        } catch (MathIllegalArgumentException iae) {
            
        }
    }

// org.apache.commons.math.linear.SingularValueSolverTest::testLeastSquareSolve
    public void testLeastSquareSolve() {
        RealMatrix m =
            MatrixUtils.createRealMatrix(new double[][] {
                                   { 1.0, 0.0 },
                                   { 0.0, 0.0 }
                               });
        DecompositionSolver solver = new SingularValueDecomposition(m).getSolver();
        RealMatrix b = MatrixUtils.createRealMatrix(new double[][] {
            { 11, 12 }, { 21, 22 }
        });
        RealMatrix xMatrix = solver.solve(b);
        Assert.assertEquals(11, xMatrix.getEntry(0, 0), 1.0e-15);
        Assert.assertEquals(12, xMatrix.getEntry(0, 1), 1.0e-15);
        Assert.assertEquals(0, xMatrix.getEntry(1, 0), 1.0e-15);
        Assert.assertEquals(0, xMatrix.getEntry(1, 1), 1.0e-15);
        RealVector xColVec = solver.solve(b.getColumnVector(0));
        Assert.assertEquals(11, xColVec.getEntry(0), 1.0e-15);
        Assert.assertEquals(0, xColVec.getEntry(1), 1.0e-15);
        RealVector xColOtherVec = solver.solve(new ArrayRealVectorTest.RealVectorTestImpl(b.getColumn(0)));
        Assert.assertEquals(11, xColOtherVec.getEntry(0), 1.0e-15);
        Assert.assertEquals(0, xColOtherVec.getEntry(1), 1.0e-15);
    }

// org.apache.commons.math.linear.SingularValueSolverTest::testSolve
    public void testSolve() {
        DecompositionSolver solver =
            new SingularValueDecomposition(MatrixUtils.createRealMatrix(testSquare)).getSolver();
        RealMatrix b = MatrixUtils.createRealMatrix(new double[][] {
                { 1, 2, 3 }, { 0, -5, 1 }
        });
        RealMatrix xRef = MatrixUtils.createRealMatrix(new double[][] {
                { -8.0 / 25.0, -263.0 / 75.0, -29.0 / 75.0 },
                { 19.0 / 25.0,   78.0 / 25.0,  49.0 / 25.0 }
        });

        
        Assert.assertEquals(0, solver.solve(b).subtract(xRef).getNorm(), normTolerance);

        
        for (int i = 0; i < b.getColumnDimension(); ++i) {
            Assert.assertEquals(0,
                         solver.solve(b.getColumnVector(i)).subtract(xRef.getColumnVector(i)).getNorm(),
                         1.0e-13);
        }

        
        for (int i = 0; i < b.getColumnDimension(); ++i) {
            ArrayRealVectorTest.RealVectorTestImpl v =
                new ArrayRealVectorTest.RealVectorTestImpl(b.getColumn(i));
            Assert.assertEquals(0,
                         solver.solve(v).subtract(xRef.getColumnVector(i)).getNorm(),
                         1.0e-13);
        }

    }

// org.apache.commons.math.linear.SingularValueSolverTest::testConditionNumber
    public void testConditionNumber() {
        SingularValueDecomposition svd =
            new SingularValueDecomposition(MatrixUtils.createRealMatrix(testSquare));
        
        Assert.assertEquals(3.0, svd.getConditionNumber(), 1.5e-15);
    }

// org.apache.commons.math.linear.SingularValueSolverTest::testMath320B
    public void testMath320B() {
        RealMatrix rm = new Array2DRowRealMatrix(new double[][] {
            { 1.0, 2.0 }, { 1.0, 2.0 }
        });
        SingularValueDecomposition svd =
            new SingularValueDecomposition(rm);
        RealMatrix recomposed = svd.getU().multiply(svd.getS()).multiply(svd.getVT());
        Assert.assertEquals(0.0, recomposed.subtract(rm).getNorm(), 2.0e-15);
    }

// org.apache.commons.math.linear.SparseRealMatrixTest::testDimensions
    public void testDimensions() {
        OpenMapRealMatrix m = createSparseMatrix(testData);
        OpenMapRealMatrix m2 = createSparseMatrix(testData2);
        Assert.assertEquals("testData row dimension", 3, m.getRowDimension());
        Assert.assertEquals("testData column dimension", 3, m.getColumnDimension());
        Assert.assertTrue("testData is square", m.isSquare());
        Assert.assertEquals("testData2 row dimension", m2.getRowDimension(), 2);
        Assert.assertEquals("testData2 column dimension", m2.getColumnDimension(), 3);
        Assert.assertTrue("testData2 is not square", !m2.isSquare());
    }

// org.apache.commons.math.linear.SparseRealMatrixTest::testCopyFunctions
    public void testCopyFunctions() {
        OpenMapRealMatrix m1 = createSparseMatrix(testData);
        RealMatrix m2 = m1.copy();
        Assert.assertEquals(m1.getClass(), m2.getClass());
        Assert.assertEquals((m2), m1);
        OpenMapRealMatrix m3 = createSparseMatrix(testData);
        RealMatrix m4 = m3.copy();
        Assert.assertEquals(m3.getClass(), m4.getClass());
        Assert.assertEquals((m4), m3);
    }

// org.apache.commons.math.linear.SparseRealMatrixTest::testAdd
    public void testAdd() {
        OpenMapRealMatrix m = createSparseMatrix(testData);
        OpenMapRealMatrix mInv = createSparseMatrix(testDataInv);
        OpenMapRealMatrix mDataPlusInv = createSparseMatrix(testDataPlusInv);
        RealMatrix mPlusMInv = m.add(mInv);
        for (int row = 0; row < m.getRowDimension(); row++) {
            for (int col = 0; col < m.getColumnDimension(); col++) {
                Assert.assertEquals("sum entry entry",
                    mDataPlusInv.getEntry(row, col), mPlusMInv.getEntry(row, col),
                    entryTolerance);
            }
        }
    }

// org.apache.commons.math.linear.SparseRealMatrixTest::testAddFail
    public void testAddFail() {
        OpenMapRealMatrix m = createSparseMatrix(testData);
        OpenMapRealMatrix m2 = createSparseMatrix(testData2);
        try {
            m.add(m2);
            Assert.fail("MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.linear.SparseRealMatrixTest::testNorm
    public void testNorm() {
        OpenMapRealMatrix m = createSparseMatrix(testData);
        OpenMapRealMatrix m2 = createSparseMatrix(testData2);
        Assert.assertEquals("testData norm", 14d, m.getNorm(), entryTolerance);
        Assert.assertEquals("testData2 norm", 7d, m2.getNorm(), entryTolerance);
    }

// org.apache.commons.math.linear.SparseRealMatrixTest::testPlusMinus
    public void testPlusMinus() {
        OpenMapRealMatrix m = createSparseMatrix(testData);
        OpenMapRealMatrix n = createSparseMatrix(testDataInv);
        assertClose("m-n = m + -n", m.subtract(n),
            n.scalarMultiply(-1d).add(m), entryTolerance);
        try {
            m.subtract(createSparseMatrix(testData2));
            Assert.fail("Expecting illegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.linear.SparseRealMatrixTest::testMultiply
    public void testMultiply() {
        OpenMapRealMatrix m = createSparseMatrix(testData);
        OpenMapRealMatrix mInv = createSparseMatrix(testDataInv);
        OpenMapRealMatrix identity = createSparseMatrix(id);
        OpenMapRealMatrix m2 = createSparseMatrix(testData2);
        assertClose("inverse multiply", m.multiply(mInv), identity,
                entryTolerance);
        assertClose("inverse multiply", m.multiply(new BlockRealMatrix(testDataInv)), identity,
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
            Assert.fail("Expecting illegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
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
        Assert.assertEquals("identity trace", 3d, m.getTrace(), entryTolerance);
        m = createSparseMatrix(testData2);
        try {
            m.getTrace();
            Assert.fail("Expecting NonSquareMatrixException");
        } catch (NonSquareMatrixException ex) {
            
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
                new ArrayRealVector(testVector)).toArray(), entryTolerance);
        m = createSparseMatrix(bigSingular);
        try {
            m.operate(testVector);
            Assert.fail("Expecting illegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.linear.SparseRealMatrixTest::testMath209
    public void testMath209() {
        RealMatrix a = createSparseMatrix(new double[][] {
                { 1, 2 }, { 3, 4 }, { 5, 6 } });
        double[] b = a.operate(new double[] { 1, 1 });
        Assert.assertEquals(a.getRowDimension(), b.length);
        Assert.assertEquals(3.0, b[0], 1.0e-12);
        Assert.assertEquals(7.0, b[1], 1.0e-12);
        Assert.assertEquals(11.0, b[2], 1.0e-12);
    }

// org.apache.commons.math.linear.SparseRealMatrixTest::testTranspose
    public void testTranspose() {
        RealMatrix m = createSparseMatrix(testData);
        RealMatrix mIT = new LUDecomposition(m).getSolver().getInverse().transpose();
        RealMatrix mTI = new LUDecomposition(m.transpose()).getSolver().getInverse();
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
            new ArrayRealVector(testVector).toArray()), preMultTest, normTolerance);
        m = createSparseMatrix(bigSingular);
        try {
            m.preMultiply(testVector);
            Assert.fail("expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.linear.SparseRealMatrixTest::testPremultiply
    public void testPremultiply() {
        RealMatrix m3 = createSparseMatrix(d3);
        RealMatrix m4 = createSparseMatrix(d4);
        RealMatrix m5 = createSparseMatrix(d5);
        assertClose("m3*m4=m5", m4.preMultiply(m3), m5, entryTolerance);

        OpenMapRealMatrix m = createSparseMatrix(testData);
        OpenMapRealMatrix mInv = createSparseMatrix(testDataInv);
        OpenMapRealMatrix identity = createSparseMatrix(id);
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
            Assert.fail("Expecting illegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.linear.SparseRealMatrixTest::testGetVectors
    public void testGetVectors() {
        RealMatrix m = createSparseMatrix(testData);
        assertClose("get row", m.getRow(0), testDataRow1, entryTolerance);
        assertClose("get col", m.getColumn(2), testDataCol3, entryTolerance);
        try {
            m.getRow(10);
            Assert.fail("expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.getColumn(-1);
            Assert.fail("expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
    }

// org.apache.commons.math.linear.SparseRealMatrixTest::testGetEntry
    public void testGetEntry() {
        RealMatrix m = createSparseMatrix(testData);
        Assert.assertEquals("get entry", m.getEntry(0, 1), 2d, entryTolerance);
        try {
            m.getEntry(10, 4);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
    }

// org.apache.commons.math.linear.SparseRealMatrixTest::testExamples
    public void testExamples() {
        
        double[][] matrixData = { { 1d, 2d, 3d }, { 2d, 5d, 3d } };
        RealMatrix m = createSparseMatrix(matrixData);
        
        double[][] matrixData2 = { { 1d, 2d }, { 2d, 5d }, { 1d, 7d } };
        RealMatrix n = createSparseMatrix(matrixData2);
        
        RealMatrix p = m.multiply(n);
        Assert.assertEquals(2, p.getRowDimension());
        Assert.assertEquals(2, p.getColumnDimension());
        
        RealMatrix pInverse = new LUDecomposition(p).getSolver().getInverse();
        Assert.assertEquals(2, pInverse.getRowDimension());
        Assert.assertEquals(2, pInverse.getColumnDimension());

        
        double[][] coefficientsData = { { 2, 3, -2 }, { -1, 7, 6 },
                { 4, -3, -5 } };
        RealMatrix coefficients = createSparseMatrix(coefficientsData);
        RealVector constants = new ArrayRealVector(new double[]{ 1, -2, 1 }, false);
        RealVector solution = new LUDecomposition(coefficients).getSolver().solve(constants);
        final double cst0 = constants.getEntry(0);
        final double cst1 = constants.getEntry(1);
        final double cst2 = constants.getEntry(2);
        final double sol0 = solution.getEntry(0);
        final double sol1 = solution.getEntry(1);
        final double sol2 = solution.getEntry(2);
        Assert.assertEquals(2 * sol0 + 3 * sol1 - 2 * sol2, cst0, 1E-12);
        Assert.assertEquals(-1 * sol0 + 7 * sol1 + 6 * sol2, cst1, 1E-12);
        Assert.assertEquals(4 * sol0 - 3 * sol1 - 5 * sol2, cst2, 1E-12);

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
        Assert.assertEquals("Rows23Cols00", mRows23Cols00, m.getSubMatrix(2, 3, 0, 0));
        Assert.assertEquals("Rows00Cols33", mRows00Cols33, m.getSubMatrix(0, 0, 3, 3));
        Assert.assertEquals("Rows01Cols23", mRows01Cols23, m.getSubMatrix(0, 1, 2, 3));
        Assert.assertEquals("Rows02Cols13", mRows02Cols13,
            m.getSubMatrix(new int[] { 0, 2 }, new int[] { 1, 3 }));
        Assert.assertEquals("Rows03Cols12", mRows03Cols12,
            m.getSubMatrix(new int[] { 0, 3 }, new int[] { 1, 2 }));
        Assert.assertEquals("Rows03Cols123", mRows03Cols123,
            m.getSubMatrix(new int[] { 0, 3 }, new int[] { 1, 2, 3 }));
        Assert.assertEquals("Rows20Cols123", mRows20Cols123,
            m.getSubMatrix(new int[] { 2, 0 }, new int[] { 1, 2, 3 }));
        Assert.assertEquals("Rows31Cols31", mRows31Cols31,
            m.getSubMatrix(new int[] { 3, 1 }, new int[] { 3, 1 }));
        Assert.assertEquals("Rows31Cols31", mRows31Cols31,
            m.getSubMatrix(new int[] { 3, 1 }, new int[] { 3, 1 }));

        try {
            m.getSubMatrix(1, 0, 2, 4);
            Assert.fail("Expecting NumberIsTooSmallException");
        } catch (NumberIsTooSmallException ex) {
            
        }
        try {
            m.getSubMatrix(-1, 1, 2, 2);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.getSubMatrix(1, 0, 2, 2);
            Assert.fail("Expecting NumberIsTooSmallException");
        } catch (NumberIsTooSmallException ex) {
            
        }
        try {
            m.getSubMatrix(1, 0, 2, 4);
            Assert.fail("Expecting NumberIsTooSmallException");
        } catch (NumberIsTooSmallException ex) {
            
        }
        try {
            m.getSubMatrix(new int[] {}, new int[] { 0 });
            Assert.fail("Expecting NoDataException");
        } catch (NoDataException ex) {
            
        }
        try {
            m.getSubMatrix(new int[] { 0 }, new int[] { 4 });
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
    }

// org.apache.commons.math.linear.SparseRealMatrixTest::testGetRowMatrix
    public void testGetRowMatrix() {
        RealMatrix m = createSparseMatrix(subTestData);
        RealMatrix mRow0 = createSparseMatrix(subRow0);
        RealMatrix mRow3 = createSparseMatrix(subRow3);
        Assert.assertEquals("Row0", mRow0, m.getRowMatrix(0));
        Assert.assertEquals("Row3", mRow3, m.getRowMatrix(3));
        try {
            m.getRowMatrix(-1);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.getRowMatrix(4);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
    }

// org.apache.commons.math.linear.SparseRealMatrixTest::testGetColumnMatrix
    public void testGetColumnMatrix() {
        RealMatrix m = createSparseMatrix(subTestData);
        RealMatrix mColumn1 = createSparseMatrix(subColumn1);
        RealMatrix mColumn3 = createSparseMatrix(subColumn3);
        Assert.assertEquals("Column1", mColumn1, m.getColumnMatrix(1));
        Assert.assertEquals("Column3", mColumn3, m.getColumnMatrix(3));
        try {
            m.getColumnMatrix(-1);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.getColumnMatrix(4);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
    }

// org.apache.commons.math.linear.SparseRealMatrixTest::testGetRowVector
    public void testGetRowVector() {
        RealMatrix m = createSparseMatrix(subTestData);
        RealVector mRow0 = new ArrayRealVector(subRow0[0]);
        RealVector mRow3 = new ArrayRealVector(subRow3[0]);
        Assert.assertEquals("Row0", mRow0, m.getRowVector(0));
        Assert.assertEquals("Row3", mRow3, m.getRowVector(3));
        try {
            m.getRowVector(-1);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.getRowVector(4);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
    }

// org.apache.commons.math.linear.SparseRealMatrixTest::testGetColumnVector
    public void testGetColumnVector() {
        RealMatrix m = createSparseMatrix(subTestData);
        RealVector mColumn1 = columnToVector(subColumn1);
        RealVector mColumn3 = columnToVector(subColumn3);
        Assert.assertEquals("Column1", mColumn1, m.getColumnVector(1));
        Assert.assertEquals("Column3", mColumn3, m.getColumnVector(3));
        try {
            m.getColumnVector(-1);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.getColumnVector(4);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
    }

// org.apache.commons.math.linear.SparseRealMatrixTest::testEqualsAndHashCode
    public void testEqualsAndHashCode() {
        OpenMapRealMatrix m = createSparseMatrix(testData);
        OpenMapRealMatrix m1 = m.copy();
        OpenMapRealMatrix mt = (OpenMapRealMatrix) m.transpose();
        Assert.assertTrue(m.hashCode() != mt.hashCode());
        Assert.assertEquals(m.hashCode(), m1.hashCode());
        Assert.assertEquals(m, m);
        Assert.assertEquals(m, m1);
        Assert.assertFalse(m.equals(null));
        Assert.assertFalse(m.equals(mt));
        Assert.assertFalse(m.equals(createSparseMatrix(bigSingular)));
    }

// org.apache.commons.math.linear.SparseRealMatrixTest::testToString
    public void testToString() {
        OpenMapRealMatrix m = createSparseMatrix(testData);
        Assert.assertEquals("OpenMapRealMatrix{{1.0,2.0,3.0},{2.0,5.0,3.0},{1.0,0.0,8.0}}",
            m.toString());
        m = new OpenMapRealMatrix(1, 1);
        Assert.assertEquals("OpenMapRealMatrix{{0.0}}", m.toString());
    }

// org.apache.commons.math.linear.SparseRealMatrixTest::testSetSubMatrix
    public void testSetSubMatrix() {
        OpenMapRealMatrix m = createSparseMatrix(testData);
        m.setSubMatrix(detData2, 1, 1);
        RealMatrix expected = createSparseMatrix(new double[][] {
                { 1.0, 2.0, 3.0 }, { 2.0, 1.0, 3.0 }, { 1.0, 2.0, 4.0 } });
        Assert.assertEquals(expected, m);

        m.setSubMatrix(detData2, 0, 0);
        expected = createSparseMatrix(new double[][] {
                { 1.0, 3.0, 3.0 }, { 2.0, 4.0, 3.0 }, { 1.0, 2.0, 4.0 } });
        Assert.assertEquals(expected, m);

        m.setSubMatrix(testDataPlus2, 0, 0);
        expected = createSparseMatrix(new double[][] {
                { 3.0, 4.0, 5.0 }, { 4.0, 7.0, 5.0 }, { 3.0, 2.0, 10.0 } });
        Assert.assertEquals(expected, m);

        
        OpenMapRealMatrix matrix =
            createSparseMatrix(new double[][] {
        { 1, 2, 3, 4 }, { 5, 6, 7, 8 }, { 9, 0, 1, 2 } });
        matrix.setSubMatrix(new double[][] { { 3, 4 }, { 5, 6 } }, 1, 1);
        expected = createSparseMatrix(new double[][] {
                { 1, 2, 3, 4 }, { 5, 3, 4, 8 }, { 9, 5, 6, 2 } });
        Assert.assertEquals(expected, matrix);

        
        try {
            m.setSubMatrix(testData, 1, 1);
            Assert.fail("expecting OutOfRangeException");
        } catch (OutOfRangeException e) {
            
        }
        
        try {
            m.setSubMatrix(testData, -1, 1);
            Assert.fail("expecting OutOfRangeException");
        } catch (OutOfRangeException e) {
            
        }
        try {
            m.setSubMatrix(testData, 1, -1);
            Assert.fail("expecting OutOfRangeException");
        } catch (OutOfRangeException e) {
            
        }

        
        try {
            m.setSubMatrix(null, 1, 1);
            Assert.fail("expecting NullArgumentException");
        } catch (NullArgumentException e) {
            
        }
        try {
            new OpenMapRealMatrix(0, 0);
            Assert.fail("expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException e) {
            
        }

        
        try {
            m.setSubMatrix(new double[][] { { 1 }, { 2, 3 } }, 0, 0);
            Assert.fail("expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException e) {
            
        }

        
        try {
            m.setSubMatrix(new double[][] { {} }, 0, 0);
            Assert.fail("expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException e) {
            
        }

    }

// org.apache.commons.math.linear.SparseRealMatrixTest::testSerial
    public void testSerial()  {
        OpenMapRealMatrix m = createSparseMatrix(testData);
        Assert.assertEquals(m,TestUtils.serializeAndRecover(m));
    }

// org.apache.commons.math.linear.SparseRealVectorTest::testConstructors
    public void testConstructors() {

        OpenMapRealVector v0 = new OpenMapRealVector();
        Assert.assertEquals("testData len", 0, v0.getDimension());

        OpenMapRealVector v1 = new OpenMapRealVector(7);
        Assert.assertEquals("testData len", 7, v1.getDimension());
        Assert.assertEquals("testData is 0.0 ", 0.0, v1.getEntry(6), 0);

        OpenMapRealVector v3 = new OpenMapRealVector(vec1);
        Assert.assertEquals("testData len", 3, v3.getDimension());
        Assert.assertEquals("testData is 2.0 ", 2.0, v3.getEntry(1), 0);

        
        
        
        
        
        
        
            
        

        RealVector v5_i = new OpenMapRealVector(dvec1);
        Assert.assertEquals("testData len", 9, v5_i.getDimension());
        Assert.assertEquals("testData is 9.0 ", 9.0, v5_i.getEntry(8), 0);

        OpenMapRealVector v5 = new OpenMapRealVector(dvec1);
        Assert.assertEquals("testData len", 9, v5.getDimension());
        Assert.assertEquals("testData is 9.0 ", 9.0, v5.getEntry(8), 0);

        OpenMapRealVector v7 = new OpenMapRealVector(v1);
        Assert.assertEquals("testData len", 7, v7.getDimension());
        Assert.assertEquals("testData is 0.0 ", 0.0, v7.getEntry(6), 0);

        SparseRealVectorTestImpl v7_i = new SparseRealVectorTestImpl(vec1);

        OpenMapRealVector v7_2 = new OpenMapRealVector(v7_i);
        Assert.assertEquals("testData len", 3, v7_2.getDimension());
        Assert.assertEquals("testData is 0.0 ", 2.0d, v7_2.getEntry(1), 0);

        OpenMapRealVector v8 = new OpenMapRealVector(v1);
        Assert.assertEquals("testData len", 7, v8.getDimension());
        Assert.assertEquals("testData is 0.0 ", 0.0, v8.getEntry(6), 0);

    }

// org.apache.commons.math.linear.SparseRealVectorTest::testDataInOut
    public void testDataInOut() {

        OpenMapRealVector v1 = new OpenMapRealVector(vec1);
        OpenMapRealVector v2 = new OpenMapRealVector(vec2);
        OpenMapRealVector v4 = new OpenMapRealVector(vec4);
        SparseRealVectorTestImpl v2_t = new SparseRealVectorTestImpl(vec2);

        RealVector v_append_1 = v1.append(v2);
        Assert.assertEquals("testData len", 6, v_append_1.getDimension());
        Assert.assertEquals("testData is 4.0 ", 4.0, v_append_1.getEntry(3), 0);

        RealVector v_append_2 = v1.append(2.0);
        Assert.assertEquals("testData len", 4, v_append_2.getDimension());
        Assert.assertEquals("testData is 2.0 ", 2.0, v_append_2.getEntry(3), 0);

        RealVector v_append_4 = v1.append(v2_t);
        Assert.assertEquals("testData len", 6, v_append_4.getDimension());
        Assert.assertEquals("testData is 4.0 ", 4.0, v_append_4.getEntry(3), 0);

        RealVector vout5 = v4.getSubVector(3, 3);
        Assert.assertEquals("testData len", 3, vout5.getDimension());
        Assert.assertEquals("testData is 4.0 ", 5.0, vout5.getEntry(1), 0);
        try {
            v4.getSubVector(3, 7);
            Assert.fail("OutOfRangeException expected");
        } catch (OutOfRangeException ex) {
            
        }

        OpenMapRealVector v_set1 = v1.copy();
        v_set1.setEntry(1, 11.0);
        Assert.assertEquals("testData is 11.0 ", 11.0, v_set1.getEntry(1), 0);
        try {
            v_set1.setEntry(3, 11.0);
            Assert.fail("OutOfRangeException expected");
        } catch (OutOfRangeException ex) {
            
        }

        OpenMapRealVector v_set2 = v4.copy();
        v_set2.setSubVector(3, v1);
        Assert.assertEquals("testData is 1.0 ", 1.0, v_set2.getEntry(3), 0);
        Assert.assertEquals("testData is 7.0 ", 7.0, v_set2.getEntry(6), 0);
        try {
            v_set2.setSubVector(7, v1);
            Assert.fail("OutOfRangeException expected");
        } catch (OutOfRangeException ex) {
            
        }

        OpenMapRealVector v_set3 = v1.copy();
        v_set3.set(13.0);
        Assert.assertEquals("testData is 13.0 ", 13.0, v_set3.getEntry(2), 0);

        try {
            v_set3.getEntry(23);
            Assert.fail("OutOfRangeException expected");
        } catch (OutOfRangeException ex) {
            
        }

        OpenMapRealVector v_set4 = v4.copy();
        v_set4.setSubVector(3, v2_t);
        Assert.assertEquals("testData is 1.0 ", 4.0, v_set4.getEntry(3), 0);
        Assert.assertEquals("testData is 7.0 ", 7.0, v_set4.getEntry(6), 0);
        try {
            v_set4.setSubVector(7, v2_t);
            Assert.fail("OutOfRangeException expected");
        } catch (OutOfRangeException ex) {
            
        }

    }

// org.apache.commons.math.linear.SparseRealVectorTest::testMapFunctions
    public void testMapFunctions() {
        OpenMapRealVector v1 = new OpenMapRealVector(vec1);

        
        RealVector v_mapAdd = v1.mapAdd(2.0d);
        double[] result_mapAdd = {3d, 4d, 5d};
        assertClose("compare vectors" ,result_mapAdd,v_mapAdd.toArray(),normTolerance);

        
        RealVector v_mapAddToSelf = v1.copy();
        v_mapAddToSelf.mapAddToSelf(2.0d);
        double[] result_mapAddToSelf = {3d, 4d, 5d};
        assertClose("compare vectors" ,result_mapAddToSelf,v_mapAddToSelf.toArray(),normTolerance);

        
        RealVector v_mapSubtract = v1.mapSubtract(2.0d);
        double[] result_mapSubtract = {-1d, 0d, 1d};
        assertClose("compare vectors" ,result_mapSubtract,v_mapSubtract.toArray(),normTolerance);

        
        RealVector v_mapSubtractToSelf = v1.copy();
        v_mapSubtractToSelf.mapSubtractToSelf(2.0d);
        double[] result_mapSubtractToSelf = {-1d, 0d, 1d};
        assertClose("compare vectors" ,result_mapSubtractToSelf,v_mapSubtractToSelf.toArray(),normTolerance);

        
        RealVector v_mapMultiply = v1.mapMultiply(2.0d);
        double[] result_mapMultiply = {2d, 4d, 6d};
        assertClose("compare vectors" ,result_mapMultiply,v_mapMultiply.toArray(),normTolerance);

        
        RealVector v_mapMultiplyToSelf = v1.copy();
        v_mapMultiplyToSelf.mapMultiplyToSelf(2.0d);
        double[] result_mapMultiplyToSelf = {2d, 4d, 6d};
        assertClose("compare vectors" ,result_mapMultiplyToSelf,v_mapMultiplyToSelf.toArray(),normTolerance);

        
        RealVector v_mapDivide = v1.mapDivide(2.0d);
        double[] result_mapDivide = {.5d, 1d, 1.5d};
        assertClose("compare vectors" ,result_mapDivide,v_mapDivide.toArray(),normTolerance);

        
        RealVector v_mapDivideToSelf = v1.copy();
        v_mapDivideToSelf.mapDivideToSelf(2.0d);
        double[] result_mapDivideToSelf = {.5d, 1d, 1.5d};
        assertClose("compare vectors" ,result_mapDivideToSelf,v_mapDivideToSelf.toArray(),normTolerance);

        
        RealVector v_mapPow = v1.map(new Power(2));
        double[] result_mapPow = {1d, 4d, 9d};
        assertClose("compare vectors" ,result_mapPow,v_mapPow.toArray(),normTolerance);

        
        RealVector v_mapPowToSelf = v1.copy();
        v_mapPowToSelf.mapToSelf(new Power(2));
        double[] result_mapPowToSelf = {1d, 4d, 9d};
        assertClose("compare vectors" ,result_mapPowToSelf,v_mapPowToSelf.toArray(),normTolerance);

        
        RealVector v_mapExp = v1.map(new Exp());
        double[] result_mapExp = {2.718281828459045e+00d,7.389056098930650e+00d, 2.008553692318767e+01d};
        assertClose("compare vectors" ,result_mapExp,v_mapExp.toArray(),normTolerance);

        
        RealVector v_mapExpToSelf = v1.copy();
        v_mapExpToSelf.mapToSelf(new Exp());
        double[] result_mapExpToSelf = {2.718281828459045e+00d,7.389056098930650e+00d, 2.008553692318767e+01d};
        assertClose("compare vectors" ,result_mapExpToSelf,v_mapExpToSelf.toArray(),normTolerance);

        
        RealVector v_mapExpm1 = v1.map(new Expm1());
        double[] result_mapExpm1 = {1.718281828459045d,6.38905609893065d, 19.085536923187668d};
        assertClose("compare vectors" ,result_mapExpm1,v_mapExpm1.toArray(),normTolerance);

        
        RealVector v_mapExpm1ToSelf = v1.copy();
        v_mapExpm1ToSelf.mapToSelf(new Expm1());
        double[] result_mapExpm1ToSelf = {1.718281828459045d,6.38905609893065d, 19.085536923187668d};
        assertClose("compare vectors" ,result_mapExpm1ToSelf,v_mapExpm1ToSelf.toArray(),normTolerance);

        
        RealVector v_mapLog = v1.map(new Log());
        double[] result_mapLog = {0d,6.931471805599453e-01d, 1.098612288668110e+00d};
        assertClose("compare vectors" ,result_mapLog,v_mapLog.toArray(),normTolerance);

        
        RealVector v_mapLogToSelf = v1.copy();
        v_mapLogToSelf.mapToSelf(new Log());
        double[] result_mapLogToSelf = {0d,6.931471805599453e-01d, 1.098612288668110e+00d};
        assertClose("compare vectors" ,result_mapLogToSelf,v_mapLogToSelf.toArray(),normTolerance);

        
        RealVector v_mapLog10 = v1.map(new Log10());
        double[] result_mapLog10 = {0d,3.010299956639812e-01d, 4.771212547196624e-01d};
        assertClose("compare vectors" ,result_mapLog10,v_mapLog10.toArray(),normTolerance);

        
        RealVector v_mapLog10ToSelf = v1.copy();
        v_mapLog10ToSelf.mapToSelf(new Log10());
        double[] result_mapLog10ToSelf = {0d,3.010299956639812e-01d, 4.771212547196624e-01d};
        assertClose("compare vectors" ,result_mapLog10ToSelf,v_mapLog10ToSelf.toArray(),normTolerance);

        
        RealVector v_mapLog1p = v1.map(new Log1p());
        double[] result_mapLog1p = {0.6931471805599453d,1.0986122886681096d,1.3862943611198906d};
        assertClose("compare vectors" ,result_mapLog1p,v_mapLog1p.toArray(),normTolerance);

        
        RealVector v_mapLog1pToSelf = v1.copy();
        v_mapLog1pToSelf.mapToSelf(new Log1p());
        double[] result_mapLog1pToSelf = {0.6931471805599453d,1.0986122886681096d,1.3862943611198906d};
        assertClose("compare vectors" ,result_mapLog1pToSelf,v_mapLog1pToSelf.toArray(),normTolerance);

        
        RealVector v_mapCosh = v1.map(new Cosh());
        double[] result_mapCosh = {1.543080634815244e+00d,3.762195691083631e+00d, 1.006766199577777e+01d};
        assertClose("compare vectors" ,result_mapCosh,v_mapCosh.toArray(),normTolerance);

        
        RealVector v_mapCoshToSelf = v1.copy();
        v_mapCoshToSelf.mapToSelf(new Cosh());
        double[] result_mapCoshToSelf = {1.543080634815244e+00d,3.762195691083631e+00d, 1.006766199577777e+01d};
        assertClose("compare vectors" ,result_mapCoshToSelf,v_mapCoshToSelf.toArray(),normTolerance);

        
        RealVector v_mapSinh = v1.map(new Sinh());
        double[] result_mapSinh = {1.175201193643801e+00d,3.626860407847019e+00d, 1.001787492740990e+01d};
        assertClose("compare vectors" ,result_mapSinh,v_mapSinh.toArray(),normTolerance);

        
        RealVector v_mapSinhToSelf = v1.copy();
        v_mapSinhToSelf.mapToSelf(new Sinh());
        double[] result_mapSinhToSelf = {1.175201193643801e+00d,3.626860407847019e+00d, 1.001787492740990e+01d};
        assertClose("compare vectors" ,result_mapSinhToSelf,v_mapSinhToSelf.toArray(),normTolerance);

        
        RealVector v_mapTanh = v1.map(new Tanh());
        double[] result_mapTanh = {7.615941559557649e-01d,9.640275800758169e-01d,9.950547536867305e-01d};
        assertClose("compare vectors" ,result_mapTanh,v_mapTanh.toArray(),normTolerance);

        
        RealVector v_mapTanhToSelf = v1.copy();
        v_mapTanhToSelf.mapToSelf(new Tanh());
        double[] result_mapTanhToSelf = {7.615941559557649e-01d,9.640275800758169e-01d,9.950547536867305e-01d};
        assertClose("compare vectors" ,result_mapTanhToSelf,v_mapTanhToSelf.toArray(),normTolerance);

        
        RealVector v_mapCos = v1.map(new Cos());
        double[] result_mapCos = {5.403023058681398e-01d,-4.161468365471424e-01d, -9.899924966004454e-01d};
        assertClose("compare vectors" ,result_mapCos,v_mapCos.toArray(),normTolerance);

        
        RealVector v_mapCosToSelf = v1.copy();
        v_mapCosToSelf.mapToSelf(new Cos());
        double[] result_mapCosToSelf = {5.403023058681398e-01d,-4.161468365471424e-01d, -9.899924966004454e-01d};
        assertClose("compare vectors" ,result_mapCosToSelf,v_mapCosToSelf.toArray(),normTolerance);

        
        RealVector v_mapSin = v1.map(new Sin());
        double[] result_mapSin = {8.414709848078965e-01d,9.092974268256817e-01d,1.411200080598672e-01d};
        assertClose("compare vectors" ,result_mapSin,v_mapSin.toArray(),normTolerance);

        
        RealVector v_mapSinToSelf = v1.copy();
        v_mapSinToSelf.mapToSelf(new Sin());
        double[] result_mapSinToSelf = {8.414709848078965e-01d,9.092974268256817e-01d,1.411200080598672e-01d};
        assertClose("compare vectors" ,result_mapSinToSelf,v_mapSinToSelf.toArray(),normTolerance);

        
        RealVector v_mapTan = v1.map(new Tan());
        double[] result_mapTan = {1.557407724654902e+00d,-2.185039863261519e+00d,-1.425465430742778e-01d};
        assertClose("compare vectors" ,result_mapTan,v_mapTan.toArray(),normTolerance);

        
        RealVector v_mapTanToSelf = v1.copy();
        v_mapTanToSelf.mapToSelf(new Tan());
        double[] result_mapTanToSelf = {1.557407724654902e+00d,-2.185039863261519e+00d,-1.425465430742778e-01d};
        assertClose("compare vectors" ,result_mapTanToSelf,v_mapTanToSelf.toArray(),normTolerance);

        double[] vat_a = {0d, 0.5d, 1.0d};
        OpenMapRealVector vat = new OpenMapRealVector(vat_a);

        
        RealVector v_mapAcos = vat.map(new Acos());
        double[] result_mapAcos = {1.570796326794897e+00d,1.047197551196598e+00d, 0.0d};
        assertClose("compare vectors" ,result_mapAcos,v_mapAcos.toArray(),normTolerance);

        
        RealVector v_mapAcosToSelf = vat.copy();
        v_mapAcosToSelf.mapToSelf(new Acos());
        double[] result_mapAcosToSelf = {1.570796326794897e+00d,1.047197551196598e+00d, 0.0d};
        assertClose("compare vectors" ,result_mapAcosToSelf,v_mapAcosToSelf.toArray(),normTolerance);

        
        RealVector v_mapAsin = vat.map(new Asin());
        double[] result_mapAsin = {0.0d,5.235987755982989e-01d,1.570796326794897e+00d};
        assertClose("compare vectors" ,result_mapAsin,v_mapAsin.toArray(),normTolerance);

        
        RealVector v_mapAsinToSelf = vat.copy();
        v_mapAsinToSelf.mapToSelf(new Asin());
        double[] result_mapAsinToSelf = {0.0d,5.235987755982989e-01d,1.570796326794897e+00d};
        assertClose("compare vectors" ,result_mapAsinToSelf,v_mapAsinToSelf.toArray(),normTolerance);

        
        RealVector v_mapAtan = vat.map(new Atan());
        double[] result_mapAtan = {0.0d,4.636476090008061e-01d,7.853981633974483e-01d};
        assertClose("compare vectors" ,result_mapAtan,v_mapAtan.toArray(),normTolerance);

        
        RealVector v_mapAtanToSelf = vat.copy();
        v_mapAtanToSelf.mapToSelf(new Atan());
        double[] result_mapAtanToSelf = {0.0d,4.636476090008061e-01d,7.853981633974483e-01d};
        assertClose("compare vectors" ,result_mapAtanToSelf,v_mapAtanToSelf.toArray(),normTolerance);

        
        RealVector v_mapInv = v1.map(new Inverse());
        double[] result_mapInv = {1d,0.5d,3.333333333333333e-01d};
        assertClose("compare vectors" ,result_mapInv,v_mapInv.toArray(),normTolerance);

        
        RealVector v_mapInvToSelf = v1.copy();
        v_mapInvToSelf.mapToSelf(new Inverse());
        double[] result_mapInvToSelf = {1d,0.5d,3.333333333333333e-01d};
        assertClose("compare vectors" ,result_mapInvToSelf,v_mapInvToSelf.toArray(),normTolerance);

        double[] abs_a = {-1.0d, 0.0d, 1.0d};
        OpenMapRealVector abs_v = new OpenMapRealVector(abs_a);

        
        RealVector v_mapAbs = abs_v.map(new Abs());
        double[] result_mapAbs = {1d,0d,1d};
        assertClose("compare vectors" ,result_mapAbs,v_mapAbs.toArray(),normTolerance);

        
        RealVector v_mapAbsToSelf = abs_v.copy();
        v_mapAbsToSelf.mapToSelf(new Abs());
        double[] result_mapAbsToSelf = {1d,0d,1d};
        assertClose("compare vectors" ,result_mapAbsToSelf,v_mapAbsToSelf.toArray(),normTolerance);

        
        RealVector v_mapSqrt = v1.map(new Sqrt());
        double[] result_mapSqrt = {1d,1.414213562373095e+00d,1.732050807568877e+00d};
        assertClose("compare vectors" ,result_mapSqrt,v_mapSqrt.toArray(),normTolerance);

        
        RealVector v_mapSqrtToSelf = v1.copy();
        v_mapSqrtToSelf.mapToSelf(new Sqrt());
        double[] result_mapSqrtToSelf = {1d,1.414213562373095e+00d,1.732050807568877e+00d};
        assertClose("compare vectors" ,result_mapSqrtToSelf,v_mapSqrtToSelf.toArray(),normTolerance);

        double[] cbrt_a = {-2.0d, 0.0d, 2.0d};
        OpenMapRealVector cbrt_v = new OpenMapRealVector(cbrt_a);

        
        RealVector v_mapCbrt = cbrt_v.map(new Cbrt());
        double[] result_mapCbrt = {-1.2599210498948732d,0d,1.2599210498948732d};
        assertClose("compare vectors" ,result_mapCbrt,v_mapCbrt.toArray(),normTolerance);

        
        RealVector v_mapCbrtToSelf = cbrt_v.copy();
        v_mapCbrtToSelf.mapToSelf(new Cbrt());
        double[] result_mapCbrtToSelf =  {-1.2599210498948732d,0d,1.2599210498948732d};
        assertClose("compare vectors" ,result_mapCbrtToSelf,v_mapCbrtToSelf.toArray(),normTolerance);

        double[] ceil_a = {-1.1d, 0.9d, 1.1d};
        OpenMapRealVector ceil_v = new OpenMapRealVector(ceil_a);

        
        RealVector v_mapCeil = ceil_v.map(new Ceil());
        double[] result_mapCeil = {-1d,1d,2d};
        assertClose("compare vectors" ,result_mapCeil,v_mapCeil.toArray(),normTolerance);

        
        RealVector v_mapCeilToSelf = ceil_v.copy();
        v_mapCeilToSelf.mapToSelf(new Ceil());
        double[] result_mapCeilToSelf =  {-1d,1d,2d};
        assertClose("compare vectors" ,result_mapCeilToSelf,v_mapCeilToSelf.toArray(),normTolerance);

        
        RealVector v_mapFloor = ceil_v.map(new Floor());
        double[] result_mapFloor = {-2d,0d,1d};
        assertClose("compare vectors" ,result_mapFloor,v_mapFloor.toArray(),normTolerance);

        
        RealVector v_mapFloorToSelf = ceil_v.copy();
        v_mapFloorToSelf.mapToSelf(new Floor());
        double[] result_mapFloorToSelf =  {-2d,0d,1d};
        assertClose("compare vectors" ,result_mapFloorToSelf,v_mapFloorToSelf.toArray(),normTolerance);

        
        RealVector v_mapRint = ceil_v.map(new Rint());
        double[] result_mapRint = {-1d,1d,1d};
        assertClose("compare vectors" ,result_mapRint,v_mapRint.toArray(),normTolerance);

        
        RealVector v_mapRintToSelf = ceil_v.copy();
        v_mapRintToSelf.mapToSelf(new Rint());
        double[] result_mapRintToSelf =  {-1d,1d,1d};
        assertClose("compare vectors" ,result_mapRintToSelf,v_mapRintToSelf.toArray(),normTolerance);

        
        RealVector v_mapSignum = ceil_v.map(new Signum());
        double[] result_mapSignum = {-1d,1d,1d};
        assertClose("compare vectors" ,result_mapSignum,v_mapSignum.toArray(),normTolerance);

        
        RealVector v_mapSignumToSelf = ceil_v.copy();
        v_mapSignumToSelf.mapToSelf(new Signum());
        double[] result_mapSignumToSelf =  {-1d,1d,1d};
        assertClose("compare vectors" ,result_mapSignumToSelf,v_mapSignumToSelf.toArray(),normTolerance);

        
        
        RealVector v_mapUlp = ceil_v.map(new Ulp());
        double[] result_mapUlp = {2.220446049250313E-16d,1.1102230246251565E-16d,2.220446049250313E-16d};
        assertClose("compare vectors" ,result_mapUlp,v_mapUlp.toArray(),normTolerance);

        
        RealVector v_mapUlpToSelf = ceil_v.copy();
        v_mapUlpToSelf.mapToSelf(new Ulp());
        double[] result_mapUlpToSelf = {2.220446049250313E-16d,1.1102230246251565E-16d,2.220446049250313E-16d};
        assertClose("compare vectors" ,result_mapUlpToSelf,v_mapUlpToSelf.toArray(),normTolerance);
    }

// org.apache.commons.math.linear.SparseRealVectorTest::testBasicFunctions
    public void testBasicFunctions() {
        OpenMapRealVector v1 = new OpenMapRealVector(vec1);
        OpenMapRealVector v2 = new OpenMapRealVector(vec2);
        OpenMapRealVector v5 = new OpenMapRealVector(vec5);
        OpenMapRealVector v_null = new OpenMapRealVector(vec_null);

        SparseRealVectorTestImpl v2_t = new SparseRealVectorTestImpl(vec2);

        
        double d_getNorm = v5.getNorm();
        Assert.assertEquals("compare values  ", 8.4261497731763586307, d_getNorm, normTolerance);

        
        double d_getL1Norm = v5.getL1Norm();
        Assert.assertEquals("compare values  ", 17.0, d_getL1Norm, normTolerance);

        
        double d_getLInfNorm = v5.getLInfNorm();
        Assert.assertEquals("compare values  ", 6.0, d_getLInfNorm, normTolerance);

        
        double dist = v1.getDistance(v2);
        Assert.assertEquals("compare values  ",v1.subtract(v2).getNorm(), dist, normTolerance);

        
        double dist_2 = v1.getDistance(v2_t);
        Assert.assertEquals("compare values  ", v1.subtract(v2).getNorm(),dist_2, normTolerance);

        
        double d_getL1Distance = v1. getL1Distance(v2);
        Assert.assertEquals("compare values  ", 9d, d_getL1Distance, normTolerance);

        double d_getL1Distance_2 = v1. getL1Distance(v2_t);
        Assert.assertEquals("compare values  ", 9d, d_getL1Distance_2, normTolerance);

        
        double d_getLInfDistance = v1. getLInfDistance(v2);
        Assert.assertEquals("compare values  ", 3d, d_getLInfDistance, normTolerance);

        double d_getLInfDistance_2 = v1. getLInfDistance(v2_t);
        Assert.assertEquals("compare values  ", 3d, d_getLInfDistance_2, normTolerance);

        
        OpenMapRealVector v_add = v1.add(v2);
        double[] result_add = {5d, 7d, 9d};
        assertClose("compare vect" ,v_add.toArray(),result_add,normTolerance);

        SparseRealVectorTestImpl vt2 = new SparseRealVectorTestImpl(vec2);
        RealVector v_add_i = v1.add(vt2);
        double[] result_add_i = {5d, 7d, 9d};
        assertClose("compare vect" ,v_add_i.toArray(),result_add_i,normTolerance);

        
        OpenMapRealVector v_subtract = v1.subtract(v2);
        double[] result_subtract = {-3d, -3d, -3d};
        assertClose("compare vect" ,v_subtract.toArray(),result_subtract,normTolerance);

        RealVector v_subtract_i = v1.subtract(vt2);
        double[] result_subtract_i = {-3d, -3d, -3d};
        assertClose("compare vect" ,v_subtract_i.toArray(),result_subtract_i,normTolerance);

        
        RealVector  v_ebeMultiply = v1.ebeMultiply(v2);
        double[] result_ebeMultiply = {4d, 10d, 18d};
        assertClose("compare vect" ,v_ebeMultiply.toArray(),result_ebeMultiply,normTolerance);

        RealVector  v_ebeMultiply_2 = v1.ebeMultiply(v2_t);
        double[] result_ebeMultiply_2 = {4d, 10d, 18d};
        assertClose("compare vect" ,v_ebeMultiply_2.toArray(),result_ebeMultiply_2,normTolerance);

        
        RealVector  v_ebeDivide = v1.ebeDivide(v2);
        double[] result_ebeDivide = {0.25d, 0.4d, 0.5d};
        assertClose("compare vect" ,v_ebeDivide.toArray(),result_ebeDivide,normTolerance);

        RealVector  v_ebeDivide_2 = v1.ebeDivide(v2_t);
        double[] result_ebeDivide_2 = {0.25d, 0.4d, 0.5d};
        assertClose("compare vect" ,v_ebeDivide_2.toArray(),result_ebeDivide_2,normTolerance);

        
        double dot =  v1.dotProduct(v2);
        Assert.assertEquals("compare val ",32d, dot, normTolerance);

        
        double dot_2 =  v1.dotProduct(v2_t);
        Assert.assertEquals("compare val ",32d, dot_2, normTolerance);

        RealMatrix m_outerProduct = v1.outerProduct(v2);
        Assert.assertEquals("compare val ",4d, m_outerProduct.getEntry(0,0), normTolerance);

        RealMatrix m_outerProduct_2 = v1.outerProduct(v2_t);
        Assert.assertEquals("compare val ",4d, m_outerProduct_2.getEntry(0,0), normTolerance);

        RealVector v_unitVector = v1.unitVector();
        RealVector v_unitVector_2 = v1.mapDivide(v1.getNorm());
        assertClose("compare vect" ,v_unitVector.toArray(),v_unitVector_2.toArray(),normTolerance);

        try {
            v_null.unitVector();
            Assert.fail("Expecting MathArithmeticException");
        } catch (MathArithmeticException ex) {
            
        }

        OpenMapRealVector v_unitize = v1.copy();
        v_unitize.unitize();
        assertClose("compare vect" ,v_unitVector_2.toArray(),v_unitize.toArray(),normTolerance);
        try {
            v_null.unitize();
            Assert.fail("Expecting MathArithmeticException");
        } catch (MathArithmeticException ex) {
            
        }

        RealVector v_projection = v1.projection(v2);
        double[] result_projection = {1.662337662337662, 2.0779220779220777, 2.493506493506493};
        assertClose("compare vect", v_projection.toArray(), result_projection, normTolerance);

        RealVector v_projection_2 = v1.projection(v2_t);
        double[] result_projection_2 = {1.662337662337662, 2.0779220779220777, 2.493506493506493};
        assertClose("compare vect", v_projection_2.toArray(), result_projection_2, normTolerance);

    }

// org.apache.commons.math.linear.SparseRealVectorTest::testOuterProduct
    public void testOuterProduct() {
        final OpenMapRealVector u = new OpenMapRealVector(new double[] {1, 2, -3});
        final OpenMapRealVector v = new OpenMapRealVector(new double[] {4, -2});

        final RealMatrix uv = u.outerProduct(v);

        final double tol = Math.ulp(1d);
        Assert.assertEquals(4, uv.getEntry(0, 0), tol);
        Assert.assertEquals(-2, uv.getEntry(0, 1), tol);
        Assert.assertEquals(8, uv.getEntry(1, 0), tol);
        Assert.assertEquals(-4, uv.getEntry(1, 1), tol);
        Assert.assertEquals(-12, uv.getEntry(2, 0), tol);
        Assert.assertEquals(6, uv.getEntry(2, 1), tol);
    }

// org.apache.commons.math.linear.SparseRealVectorTest::testMisc
    public void testMisc() {
        OpenMapRealVector v1 = new OpenMapRealVector(vec1);

        String out1 = v1.toString();
        Assert.assertTrue("some output ",  out1.length()!=0);
        try {
            v1.checkVectorDimensions(2);
            Assert.fail("MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }

    }

// org.apache.commons.math.linear.SparseRealVectorTest::testPredicates
    public void testPredicates() {

        OpenMapRealVector v = new OpenMapRealVector(new double[] { 0, 1, 2 });

        Assert.assertFalse(v.isNaN());
        v.setEntry(1, Double.NaN);
        Assert.assertTrue(v.isNaN());

        Assert.assertFalse(v.isInfinite());
        v.setEntry(0, Double.POSITIVE_INFINITY);
        Assert.assertFalse(v.isInfinite()); 
        v.setEntry(1, 1);
        Assert.assertTrue(v.isInfinite());

        v.setEntry(0, 0);
        Assert.assertEquals(v, new OpenMapRealVector(new double[] { 0, 1, 2 }));
        Assert.assertNotSame(v, new OpenMapRealVector(new double[] { 0, 1, 2 + FastMath.ulp(2)}));
        Assert.assertNotSame(v, new OpenMapRealVector(new double[] { 0, 1, 2, 3 }));

    }

// org.apache.commons.math.linear.SparseRealVectorTest::testSerial
    public void testSerial()  {
        OpenMapRealVector v = new OpenMapRealVector(new double[] { 0, 1, 2 });
        Assert.assertEquals(v,TestUtils.serializeAndRecover(v));
    }

// org.apache.commons.math.linear.SparseRealVectorTest::testConcurrentModification
    public void testConcurrentModification() {
        final RealVector u = new OpenMapRealVector(3, 1e-6);
        u.setEntry(0, 1);
        u.setEntry(1, 0);
        u.setEntry(2, 2);

        final RealVector v1 = new OpenMapRealVector(3, 1e-6);
        v1.setEntry(0, 0);
        v1.setEntry(1, 3);
        v1.setEntry(2, 0);

        RealVector w;

        w = u.ebeMultiply(v1);
        w = u.ebeDivide(v1);
    }

// org.apache.commons.math.optimization.MultiStartDifferentiableMultivariateVectorialOptimizerTest::testTrivial
    public void testTrivial() {
        LinearProblem problem =
            new LinearProblem(new double[][] { { 2 } }, new double[] { 3 });
        DifferentiableMultivariateVectorialOptimizer underlyingOptimizer =
            new GaussNewtonOptimizer(true);
        JDKRandomGenerator g = new JDKRandomGenerator();
        g.setSeed(16069223052l);
        RandomVectorGenerator generator =
            new UncorrelatedRandomVectorGenerator(1, new GaussianRandomGenerator(g));
        MultiStartDifferentiableMultivariateVectorialOptimizer optimizer =
            new MultiStartDifferentiableMultivariateVectorialOptimizer(underlyingOptimizer,
                                                                       10, generator);
        optimizer.setConvergenceChecker(new SimpleVectorialValueChecker(1.0e-6, 1.0e-6));

        
        try {
            optimizer.getOptima();
            Assert.fail("an exception should have been thrown");
        } catch (MathIllegalStateException ise) {
            
        }
        VectorialPointValuePair optimum =
            optimizer.optimize(100, problem, problem.target, new double[] { 1 }, new double[] { 0 });
        Assert.assertEquals(1.5, optimum.getPoint()[0], 1.0e-10);
        Assert.assertEquals(3.0, optimum.getValue()[0], 1.0e-10);
        VectorialPointValuePair[] optima = optimizer.getOptima();
        Assert.assertEquals(10, optima.length);
        for (int i = 0; i < optima.length; ++i) {
            Assert.assertEquals(1.5, optima[i].getPoint()[0], 1.0e-10);
            Assert.assertEquals(3.0, optima[i].getValue()[0], 1.0e-10);
        }
        Assert.assertTrue(optimizer.getEvaluations() > 20);
        Assert.assertTrue(optimizer.getEvaluations() < 50);
        Assert.assertEquals(100, optimizer.getMaxEvaluations());
    }

// org.apache.commons.math.optimization.MultiStartDifferentiableMultivariateVectorialOptimizerTest::testNoOptimum
    public void testNoOptimum() {
        DifferentiableMultivariateVectorialOptimizer underlyingOptimizer =
            new GaussNewtonOptimizer(true);
        JDKRandomGenerator g = new JDKRandomGenerator();
        g.setSeed(12373523445l);
        RandomVectorGenerator generator =
            new UncorrelatedRandomVectorGenerator(1, new GaussianRandomGenerator(g));
        MultiStartDifferentiableMultivariateVectorialOptimizer optimizer =
            new MultiStartDifferentiableMultivariateVectorialOptimizer(underlyingOptimizer,
                                                                       10, generator);
        optimizer.setConvergenceChecker(new SimpleVectorialValueChecker(1.0e-6, 1.0e-6));
        optimizer.optimize(100, new DifferentiableMultivariateVectorialFunction() {
                public MultivariateMatrixFunction jacobian() {
                    return null;
                }
                public double[] value(double[] point) {
                    throw new TestException();
                }
            }, new double[] { 2 }, new double[] { 1 }, new double[] { 0 });
    }

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
        double[][] boundaries = null;
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

// org.apache.commons.math.optimization.direct.CMAESOptimizerTest::testInitOutofbounds
    public void testInitOutofbounds() {
        double[] startPoint = point(DIM,3);
        double[] insigma = null;
        double[][] boundaries = boundaries(DIM,-1,2);
        RealPointValuePair expected =
            new RealPointValuePair(point(DIM,1.0),0.0);
        doTest(new Rosen(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, LAMBDA, true, 0, 1e-13,
                1e-13, 1e-6, 100000, expected);
    }

// org.apache.commons.math.optimization.direct.CMAESOptimizerTest::testBoundariesDimensionMismatch
    public void testBoundariesDimensionMismatch() {
        double[] startPoint = point(DIM,0.5);
        double[] insigma = null;
        double[][] boundaries = boundaries(DIM+1,-1,2);
        RealPointValuePair expected =
            new RealPointValuePair(point(DIM,1.0),0.0);
        doTest(new Rosen(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, LAMBDA, true, 0, 1e-13,
                1e-13, 1e-6, 100000, expected);
    }

// org.apache.commons.math.optimization.direct.CMAESOptimizerTest::testBoundariesNoData
    public void testBoundariesNoData() {
        double[] startPoint = point(DIM,0.5);
        double[] insigma = null;
        double[][] boundaries = boundaries(DIM,-1,2);
        boundaries[1] = null;
        RealPointValuePair expected =
            new RealPointValuePair(point(DIM,1.0),0.0);
        doTest(new Rosen(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, LAMBDA, true, 0, 1e-13,
                1e-13, 1e-6, 100000, expected);
    }

// org.apache.commons.math.optimization.direct.CMAESOptimizerTest::testInputSigmaNegative
    public void testInputSigmaNegative() {
        double[] startPoint = point(DIM,0.5);
        double[] insigma = point(DIM,-0.5);
        double[][] boundaries = null;
        RealPointValuePair expected =
            new RealPointValuePair(point(DIM,1.0),0.0);
        doTest(new Rosen(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, LAMBDA, true, 0, 1e-13,
                1e-13, 1e-6, 100000, expected);
    }

// org.apache.commons.math.optimization.direct.CMAESOptimizerTest::testInputSigmaOutOfRange
    public void testInputSigmaOutOfRange() {
        double[] startPoint = point(DIM,0.5);
        double[] insigma = point(DIM, 1.1);
        double[][] boundaries = boundaries(DIM,-1,2);
        RealPointValuePair expected =
            new RealPointValuePair(point(DIM,1.0),0.0);
        doTest(new Rosen(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, LAMBDA, true, 0, 1e-13,
                1e-13, 1e-6, 100000, expected);
    }

// org.apache.commons.math.optimization.direct.CMAESOptimizerTest::testInputSigmaDimensionMismatch
    public void testInputSigmaDimensionMismatch() {
        double[] startPoint = point(DIM,0.5);
        double[] insigma = point(DIM+1,-0.5);
        double[][] boundaries = null;
        RealPointValuePair expected =
            new RealPointValuePair(point(DIM,1.0),0.0);
        doTest(new Rosen(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, LAMBDA, true, 0, 1e-13,
                1e-13, 1e-6, 100000, expected);
    }

// org.apache.commons.math.optimization.direct.CMAESOptimizerTest::testRosen
    public void testRosen() {
        double[] startPoint = point(DIM,0.1);
        double[] insigma = point(DIM,0.1);
        double[][] boundaries = null;
        RealPointValuePair expected =
            new RealPointValuePair(point(DIM,1.0),0.0);
        doTest(new Rosen(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, LAMBDA, true, 0, 1e-13,
                1e-13, 1e-6, 100000, expected);
        doTest(new Rosen(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, LAMBDA, false, 0, 1e-13,
                1e-13, 1e-6, 100000, expected);
    }

// org.apache.commons.math.optimization.direct.CMAESOptimizerTest::testMaximize
    public void testMaximize() {}

// org.apache.commons.math.optimization.direct.CMAESOptimizerTest::testEllipse
    public void testEllipse() {
        double[] startPoint = point(DIM,1.0);
        double[] insigma = point(DIM,0.1);
        double[][] boundaries = null;
        RealPointValuePair expected =
            new RealPointValuePair(point(DIM,0.0),0.0);
        doTest(new Elli(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, LAMBDA, true, 0, 1e-13,
                1e-13, 1e-6, 100000, expected);
        doTest(new Elli(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, LAMBDA, false, 0, 1e-13,
                1e-13, 1e-6, 100000, expected);
    }

// org.apache.commons.math.optimization.direct.CMAESOptimizerTest::testElliRotated
    public void testElliRotated() {
        double[] startPoint = point(DIM,1.0);
        double[] insigma = point(DIM,0.1);
        double[][] boundaries = null;
        RealPointValuePair expected =
            new RealPointValuePair(point(DIM,0.0),0.0);
        doTest(new ElliRotated(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, LAMBDA, true, 0, 1e-13,
                1e-13, 1e-6, 100000, expected);
        doTest(new ElliRotated(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, LAMBDA, false, 0, 1e-13,
                1e-13, 1e-6, 100000, expected);
    }

// org.apache.commons.math.optimization.direct.CMAESOptimizerTest::testCigar
    public void testCigar() {
        double[] startPoint = point(DIM,1.0);
        double[] insigma = point(DIM,0.1);
        double[][] boundaries = null;
        RealPointValuePair expected =
            new RealPointValuePair(point(DIM,0.0),0.0);
        doTest(new Cigar(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, LAMBDA, true, 0, 1e-13,
                1e-13, 1e-6, 200000, expected);
        doTest(new Cigar(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, LAMBDA, false, 0, 1e-13,
                1e-13, 1e-6, 100000, expected);
    }

// org.apache.commons.math.optimization.direct.CMAESOptimizerTest::testTwoAxes
    public void testTwoAxes() {
        double[] startPoint = point(DIM,1.0);
        double[] insigma = point(DIM,0.1);
        double[][] boundaries = null;
        RealPointValuePair expected =
            new RealPointValuePair(point(DIM,0.0),0.0);
        doTest(new TwoAxes(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, 2*LAMBDA, true, 0, 1e-13,
                1e-13, 1e-6, 200000, expected);
        doTest(new TwoAxes(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, 2*LAMBDA, false, 0, 1e-13,
                1e-8, 1e-3, 200000, expected);
    }

// org.apache.commons.math.optimization.direct.CMAESOptimizerTest::testCigTab
    public void testCigTab() {}

// org.apache.commons.math.optimization.direct.CMAESOptimizerTest::testSphere
    public void testSphere() {
        double[] startPoint = point(DIM,1.0);
        double[] insigma = point(DIM,0.1);
        double[][] boundaries = null;
        RealPointValuePair expected =
            new RealPointValuePair(point(DIM,0.0),0.0);
        doTest(new Sphere(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, LAMBDA, true, 0, 1e-13,
                1e-13, 1e-6, 100000, expected);
        doTest(new Sphere(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, LAMBDA, false, 0, 1e-13,
                1e-13, 1e-6, 100000, expected);
    }

// org.apache.commons.math.optimization.direct.CMAESOptimizerTest::testTablet
    public void testTablet() {
        double[] startPoint = point(DIM,1.0);
        double[] insigma = point(DIM,0.1);
        double[][] boundaries = null;
        RealPointValuePair expected =
            new RealPointValuePair(point(DIM,0.0),0.0);
        doTest(new Tablet(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, LAMBDA, true, 0, 1e-13,
                1e-13, 1e-6, 100000, expected);
        doTest(new Tablet(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, LAMBDA, false, 0, 1e-13,
                1e-13, 1e-6, 100000, expected);
    }

// org.apache.commons.math.optimization.direct.CMAESOptimizerTest::testDiffPow
    public void testDiffPow() {
        double[] startPoint = point(DIM,1.0);
        double[] insigma = point(DIM,0.1);
        double[][] boundaries = null;
        RealPointValuePair expected =
            new RealPointValuePair(point(DIM,0.0),0.0);
        doTest(new DiffPow(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, 10, true, 0, 1e-13,
                1e-8, 1e-1, 100000, expected);
        doTest(new DiffPow(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, 10, false, 0, 1e-13,
                1e-8, 2e-1, 100000, expected);
    }

// org.apache.commons.math.optimization.direct.CMAESOptimizerTest::testSsDiffPow
    public void testSsDiffPow() {
        double[] startPoint = point(DIM,1.0);
        double[] insigma = point(DIM,0.1);
        double[][] boundaries = null;
        RealPointValuePair expected =
            new RealPointValuePair(point(DIM,0.0),0.0);
        doTest(new SsDiffPow(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, 10, true, 0, 1e-13,
                1e-4, 1e-1, 200000, expected);
        doTest(new SsDiffPow(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, 10, false, 0, 1e-13,
                1e-4, 1e-1, 200000, expected);
    }

// org.apache.commons.math.optimization.direct.CMAESOptimizerTest::testAckley
    public void testAckley() {
        double[] startPoint = point(DIM,1.0);
        double[] insigma = point(DIM,1.0);
        double[][] boundaries = null;
        RealPointValuePair expected =
            new RealPointValuePair(point(DIM,0.0),0.0);
        doTest(new Ackley(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, 2*LAMBDA, true, 0, 1e-13,
                1e-9, 1e-5, 100000, expected);
        doTest(new Ackley(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, 2*LAMBDA, false, 0, 1e-13,
                1e-9, 1e-5, 100000, expected);
    }

// org.apache.commons.math.optimization.direct.CMAESOptimizerTest::testRastrigin
    public void testRastrigin() {
        double[] startPoint = point(DIM,0.1);
        double[] insigma = point(DIM,0.1);
        double[][] boundaries = null;
        RealPointValuePair expected =
            new RealPointValuePair(point(DIM,0.0),0.0);
        doTest(new Rastrigin(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, (int)(200*Math.sqrt(DIM)), true, 0, 1e-13,
                1e-13, 1e-6, 200000, expected);
        doTest(new Rastrigin(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, (int)(200*Math.sqrt(DIM)), false, 0, 1e-13,
                1e-13, 1e-6, 200000, expected);
    }

// org.apache.commons.math.optimization.direct.CMAESOptimizerTest::testConstrainedRosen
    public void testConstrainedRosen() {
        double[] startPoint = point(DIM,0.1);
        double[] insigma = point(DIM,0.1);
        double[][] boundaries = boundaries(DIM,-1,2);
        RealPointValuePair expected =
            new RealPointValuePair(point(DIM,1.0),0.0);
        doTest(new Rosen(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, 2*LAMBDA, true, 0, 1e-13,
                1e-13, 1e-6, 100000, expected);
        doTest(new Rosen(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, 2*LAMBDA, false, 0, 1e-13,
                1e-13, 1e-6, 100000, expected);
    }

// org.apache.commons.math.optimization.direct.CMAESOptimizerTest::testDiagonalRosen
    public void testDiagonalRosen() {}

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

            PolynomialFunction fitted = new PolynomialFunction(fitter.fit());

            for (double x = -1.0; x < 1.0; x += 0.01) {
                double error = FastMath.abs(p.value(x) - fitted.value(x)) /
                               (1.0 + FastMath.abs(p.value(x)));
                Assert.assertEquals(0.0, error, 1.0e-6);
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

            PolynomialFunction fitted = new PolynomialFunction(fitter.fit());

            for (double x = -1.0; x < 1.0; x += 0.01) {
                double error = FastMath.abs(p.value(x) - fitted.value(x)) /
                              (1.0 + FastMath.abs(p.value(x)));
                maxError = FastMath.max(maxError, error);
                Assert.assertTrue(FastMath.abs(error) < 0.1);
            }
        }
        Assert.assertTrue(maxError > 0.01);

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
    public void testTrivial() {
        LinearProblem problem =
            new LinearProblem(new double[][] { { 2 } }, new double[] { 3 });

        GaussNewtonOptimizer optimizer
            = new GaussNewtonOptimizer(new SimpleVectorialValueChecker(1.0e-6, 1.0e-6));

        VectorialPointValuePair optimum =
            optimizer.optimize(100, problem, problem.target, new double[] { 1 }, new double[] { 0 });
        Assert.assertEquals(0, optimizer.getRMS(), 1.0e-10);
        Assert.assertEquals(1.5, optimum.getPoint()[0], 1.0e-10);
        Assert.assertEquals(3.0, optimum.getValue()[0], 1.0e-10);
    }

// org.apache.commons.math.optimization.general.GaussNewtonOptimizerTest::testColumnsPermutation
    public void testColumnsPermutation() {

        LinearProblem problem =
            new LinearProblem(new double[][] { { 1.0, -1.0 }, { 0.0, 2.0 }, { 1.0, -2.0 } },
                              new double[] { 4.0, 6.0, 1.0 });

        GaussNewtonOptimizer optimizer
            = new GaussNewtonOptimizer(new SimpleVectorialValueChecker(1.0e-6, 1.0e-6));

        VectorialPointValuePair optimum =
            optimizer.optimize(100, problem, problem.target, new double[] { 1, 1, 1 }, new double[] { 0, 0 });
        Assert.assertEquals(0, optimizer.getRMS(), 1.0e-10);
        Assert.assertEquals(7.0, optimum.getPoint()[0], 1.0e-10);
        Assert.assertEquals(3.0, optimum.getPoint()[1], 1.0e-10);
        Assert.assertEquals(4.0, optimum.getValue()[0], 1.0e-10);
        Assert.assertEquals(6.0, optimum.getValue()[1], 1.0e-10);
        Assert.assertEquals(1.0, optimum.getValue()[2], 1.0e-10);

    }

// org.apache.commons.math.optimization.general.GaussNewtonOptimizerTest::testNoDependency
    public void testNoDependency() {
        LinearProblem problem = new LinearProblem(new double[][] {
                { 2, 0, 0, 0, 0, 0 },
                { 0, 2, 0, 0, 0, 0 },
                { 0, 0, 2, 0, 0, 0 },
                { 0, 0, 0, 2, 0, 0 },
                { 0, 0, 0, 0, 2, 0 },
                { 0, 0, 0, 0, 0, 2 }
        }, new double[] { 0.0, 1.1, 2.2, 3.3, 4.4, 5.5 });

        GaussNewtonOptimizer optimizer
            = new GaussNewtonOptimizer(new SimpleVectorialValueChecker(1.0e-6, 1.0e-6));

        VectorialPointValuePair optimum =
            optimizer.optimize(100, problem, problem.target, new double[] { 1, 1, 1, 1, 1, 1 },
                               new double[] { 0, 0, 0, 0, 0, 0 });
        Assert.assertEquals(0, optimizer.getRMS(), 1.0e-10);
        for (int i = 0; i < problem.target.length; ++i) {
            Assert.assertEquals(0.55 * i, optimum.getPoint()[i], 1.0e-10);
        }
    }

// org.apache.commons.math.optimization.general.GaussNewtonOptimizerTest::testOneSet
    public void testOneSet() {

        LinearProblem problem = new LinearProblem(new double[][] {
                {  1,  0, 0 },
                { -1,  1, 0 },
                {  0, -1, 1 }
        }, new double[] { 1, 1, 1});

        GaussNewtonOptimizer optimizer
            = new GaussNewtonOptimizer(new SimpleVectorialValueChecker(1.0e-6, 1.0e-6));

        VectorialPointValuePair optimum =
            optimizer.optimize(100, problem, problem.target, new double[] { 1, 1, 1 }, new double[] { 0, 0, 0 });
        Assert.assertEquals(0, optimizer.getRMS(), 1.0e-10);
        Assert.assertEquals(1.0, optimum.getPoint()[0], 1.0e-10);
        Assert.assertEquals(2.0, optimum.getPoint()[1], 1.0e-10);
        Assert.assertEquals(3.0, optimum.getPoint()[2], 1.0e-10);

    }

// org.apache.commons.math.optimization.general.GaussNewtonOptimizerTest::testTwoSets
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

        GaussNewtonOptimizer optimizer
            = new GaussNewtonOptimizer(new SimpleVectorialValueChecker(1.0e-6, 1.0e-6));

        VectorialPointValuePair optimum =
            optimizer.optimize(100, problem, problem.target, new double[] { 1, 1, 1, 1, 1, 1 },
                               new double[] { 0, 0, 0, 0, 0, 0 });
        Assert.assertEquals(0, optimizer.getRMS(), 1.0e-10);
        Assert.assertEquals( 3.0, optimum.getPoint()[0], 1.0e-10);
        Assert.assertEquals( 4.0, optimum.getPoint()[1], 1.0e-10);
        Assert.assertEquals(-1.0, optimum.getPoint()[2], 1.0e-10);
        Assert.assertEquals(-2.0, optimum.getPoint()[3], 1.0e-10);
        Assert.assertEquals( 1.0 + epsilon, optimum.getPoint()[4], 1.0e-10);
        Assert.assertEquals( 1.0 - epsilon, optimum.getPoint()[5], 1.0e-10);

    }

// org.apache.commons.math.optimization.general.GaussNewtonOptimizerTest::testNonInversible
    public void testNonInversible() throws Exception {

        LinearProblem problem = new LinearProblem(new double[][] {
                {  1, 2, -3 },
                {  2, 1,  3 },
                { -3, 0, -9 }
        }, new double[] { 1, 1, 1 });

        GaussNewtonOptimizer optimizer
            = new GaussNewtonOptimizer(new SimpleVectorialValueChecker(1.0e-6, 1.0e-6));

        optimizer.optimize(100, problem, problem.target, new double[] { 1, 1, 1 }, new double[] { 0, 0, 0 });
    }

// org.apache.commons.math.optimization.general.GaussNewtonOptimizerTest::testIllConditioned
    public void testIllConditioned() {
        LinearProblem problem1 = new LinearProblem(new double[][] {
                { 10.0, 7.0,  8.0,  7.0 },
                {  7.0, 5.0,  6.0,  5.0 },
                {  8.0, 6.0, 10.0,  9.0 },
                {  7.0, 5.0,  9.0, 10.0 }
        }, new double[] { 32, 23, 33, 31 });

        GaussNewtonOptimizer optimizer
            = new GaussNewtonOptimizer(new SimpleVectorialValueChecker(1.0e-6, 1.0e-6));

        VectorialPointValuePair optimum1 =
            optimizer.optimize(100, problem1, problem1.target, new double[] { 1, 1, 1, 1 },
                               new double[] { 0, 1, 2, 3 });
        Assert.assertEquals(0, optimizer.getRMS(), 1.0e-10);
        Assert.assertEquals(1.0, optimum1.getPoint()[0], 1.0e-10);
        Assert.assertEquals(1.0, optimum1.getPoint()[1], 1.0e-10);
        Assert.assertEquals(1.0, optimum1.getPoint()[2], 1.0e-10);
        Assert.assertEquals(1.0, optimum1.getPoint()[3], 1.0e-10);

        LinearProblem problem2 = new LinearProblem(new double[][] {
                { 10.00, 7.00, 8.10, 7.20 },
                {  7.08, 5.04, 6.00, 5.00 },
                {  8.00, 5.98, 9.89, 9.00 },
                {  6.99, 4.99, 9.00, 9.98 }
        }, new double[] { 32, 23, 33, 31 });
        VectorialPointValuePair optimum2 =
            optimizer.optimize(100, problem2, problem2.target, new double[] { 1, 1, 1, 1 },
                               new double[] { 0, 1, 2, 3 });
        Assert.assertEquals(0, optimizer.getRMS(), 1.0e-10);
        Assert.assertEquals(-81.0, optimum2.getPoint()[0], 1.0e-8);
        Assert.assertEquals(137.0, optimum2.getPoint()[1], 1.0e-8);
        Assert.assertEquals(-34.0, optimum2.getPoint()[2], 1.0e-8);
        Assert.assertEquals( 22.0, optimum2.getPoint()[3], 1.0e-8);

    }

// org.apache.commons.math.optimization.general.GaussNewtonOptimizerTest::testMoreEstimatedParametersSimple
    public void testMoreEstimatedParametersSimple() throws Exception {

        LinearProblem problem = new LinearProblem(new double[][] {
                { 3.0, 2.0,  0.0, 0.0 },
                { 0.0, 1.0, -1.0, 1.0 },
                { 2.0, 0.0,  1.0, 0.0 }
        }, new double[] { 7.0, 3.0, 5.0 });

        GaussNewtonOptimizer optimizer
            = new GaussNewtonOptimizer(new SimpleVectorialValueChecker(1.0e-6, 1.0e-6));

        optimizer.optimize(100, problem, problem.target, new double[] { 1, 1, 1 },
                           new double[] { 7, 6, 5, 4 });
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

        GaussNewtonOptimizer optimizer
            = new GaussNewtonOptimizer(new SimpleVectorialValueChecker(1.0e-6, 1.0e-6));

        optimizer.optimize(100, problem, problem.target, new double[] { 1, 1, 1, 1, 1 },
                           new double[] { 2, 2, 2, 2, 2, 2 });
    }

// org.apache.commons.math.optimization.general.GaussNewtonOptimizerTest::testRedundantEquations
    public void testRedundantEquations() {
        LinearProblem problem = new LinearProblem(new double[][] {
                { 1.0,  1.0 },
                { 1.0, -1.0 },
                { 1.0,  3.0 }
        }, new double[] { 3.0, 1.0, 5.0 });

        GaussNewtonOptimizer optimizer
            = new GaussNewtonOptimizer(new SimpleVectorialValueChecker(1.0e-6, 1.0e-6));

        VectorialPointValuePair optimum =
            optimizer.optimize(100, problem, problem.target, new double[] { 1, 1, 1 },
                               new double[] { 1, 1 });
        Assert.assertEquals(0, optimizer.getRMS(), 1.0e-10);
        Assert.assertEquals(2.0, optimum.getPoint()[0], 1.0e-8);
        Assert.assertEquals(1.0, optimum.getPoint()[1], 1.0e-8);
    }

// org.apache.commons.math.optimization.general.GaussNewtonOptimizerTest::testInconsistentEquations
    public void testInconsistentEquations() {
        LinearProblem problem = new LinearProblem(new double[][] {
                { 1.0,  1.0 },
                { 1.0, -1.0 },
                { 1.0,  3.0 }
        }, new double[] { 3.0, 1.0, 4.0 });

        GaussNewtonOptimizer optimizer
            = new GaussNewtonOptimizer(new SimpleVectorialValueChecker(1.0e-6, 1.0e-6));

        optimizer.optimize(100, problem, problem.target, new double[] { 1, 1, 1 }, new double[] { 1, 1 });
        Assert.assertTrue(optimizer.getRMS() > 0.1);

    }

// org.apache.commons.math.optimization.general.GaussNewtonOptimizerTest::testInconsistentSizes1
    public void testInconsistentSizes1() {
        LinearProblem problem =
            new LinearProblem(new double[][] { { 1, 0 }, { 0, 1 } }, new double[] { -1, 1 });

        GaussNewtonOptimizer optimizer
            = new GaussNewtonOptimizer(new SimpleVectorialValueChecker(1.0e-6, 1.0e-6));

        VectorialPointValuePair optimum =
            optimizer.optimize(100, problem, problem.target, new double[] { 1, 1 }, new double[] { 0, 0 });
        Assert.assertEquals(0, optimizer.getRMS(), 1.0e-10);
        Assert.assertEquals(-1, optimum.getPoint()[0], 1.0e-10);
        Assert.assertEquals(+1, optimum.getPoint()[1], 1.0e-10);

        optimizer.optimize(100, problem, problem.target,
                           new double[] { 1 },
                           new double[] { 0, 0 });
    }

// org.apache.commons.math.optimization.general.GaussNewtonOptimizerTest::testInconsistentSizes2
    public void testInconsistentSizes2() {
        LinearProblem problem =
            new LinearProblem(new double[][] { { 1, 0 }, { 0, 1 } }, new double[] { -1, 1 });

        GaussNewtonOptimizer optimizer
            = new GaussNewtonOptimizer(new SimpleVectorialValueChecker(1.0e-6, 1.0e-6));

        VectorialPointValuePair optimum =
            optimizer.optimize(100, problem, problem.target, new double[] { 1, 1 }, new double[] { 0, 0 });
        Assert.assertEquals(0, optimizer.getRMS(), 1.0e-10);
        Assert.assertEquals(-1, optimum.getPoint()[0], 1.0e-10);
        Assert.assertEquals(+1, optimum.getPoint()[1], 1.0e-10);

        optimizer.optimize(100, problem, new double[] { 1 },
                           new double[] { 1 },
                           new double[] { 0, 0 });
    }

// org.apache.commons.math.optimization.general.GaussNewtonOptimizerTest::testMaxEvaluations
    public void testMaxEvaluations() throws Exception {
        CircleVectorial circle = new CircleVectorial();
        circle.addPoint( 30.0,  68.0);
        circle.addPoint( 50.0,  -6.0);
        circle.addPoint(110.0, -20.0);
        circle.addPoint( 35.0,  15.0);
        circle.addPoint( 45.0,  97.0);

        GaussNewtonOptimizer optimizer
            = new GaussNewtonOptimizer(new SimpleVectorialValueChecker(1.0e-30, 1.0e-30));

        optimizer.optimize(100, circle, new double[] { 0, 0, 0, 0, 0 },
                           new double[] { 1, 1, 1, 1, 1 },
                           new double[] { 98.680, 47.345 });
    }

// org.apache.commons.math.optimization.general.GaussNewtonOptimizerTest::testCircleFitting
    public void testCircleFitting() {
        CircleVectorial circle = new CircleVectorial();
        circle.addPoint( 30.0,  68.0);
        circle.addPoint( 50.0,  -6.0);
        circle.addPoint(110.0, -20.0);
        circle.addPoint( 35.0,  15.0);
        circle.addPoint( 45.0,  97.0);

        GaussNewtonOptimizer optimizer
            = new GaussNewtonOptimizer(new SimpleVectorialValueChecker(1.0e-13, 1.0e-13));

        VectorialPointValuePair optimum =
            optimizer.optimize(100, circle, new double[] { 0, 0, 0, 0, 0 },
                               new double[] { 1, 1, 1, 1, 1 },
                               new double[] { 98.680, 47.345 });
        Assert.assertEquals(1.768262623567235,  FastMath.sqrt(circle.getN()) * optimizer.getRMS(),  1.0e-10);
        Point2D.Double center = new Point2D.Double(optimum.getPointRef()[0], optimum.getPointRef()[1]);
        Assert.assertEquals(69.96016175359975, circle.getRadius(center), 1.0e-10);
        Assert.assertEquals(96.07590209601095, center.x, 1.0e-10);
        Assert.assertEquals(48.135167894714,   center.y, 1.0e-10);
    }

// org.apache.commons.math.optimization.general.GaussNewtonOptimizerTest::testCircleFittingBadInit
    public void testCircleFittingBadInit() {
        CircleVectorial circle = new CircleVectorial();
        double[][] points = circlePoints;
        double[] target = new double[points.length];
        Arrays.fill(target, 0.0);
        double[] weights = new double[points.length];
        Arrays.fill(weights, 2.0);
        for (int i = 0; i < points.length; ++i) {
            circle.addPoint(points[i][0], points[i][1]);
        }

        GaussNewtonOptimizer optimizer
            = new GaussNewtonOptimizer(new SimpleVectorialValueChecker(1.0e-6, 1.0e-6));

        optimizer.optimize(100, circle, target, weights, new double[] { -12, -12 });
    }

// org.apache.commons.math.optimization.general.GaussNewtonOptimizerTest::testCircleFittingGoodInit
    public void testCircleFittingGoodInit() {
        CircleVectorial circle = new CircleVectorial();
        double[][] points = circlePoints;
        double[] target = new double[points.length];
        Arrays.fill(target, 0.0);
        double[] weights = new double[points.length];
        Arrays.fill(weights, 2.0);
        for (int i = 0; i < points.length; ++i) {
            circle.addPoint(points[i][0], points[i][1]);
        }

        GaussNewtonOptimizer optimizer
            = new GaussNewtonOptimizer(new SimpleVectorialValueChecker(1.0e-6, 1.0e-6));

        VectorialPointValuePair optimum =
            optimizer.optimize(100, circle, target, weights, new double[] { 0, 0 });
        Assert.assertEquals(-0.1517383071957963, optimum.getPointRef()[0], 1.0e-6);
        Assert.assertEquals(0.2074999736353867,  optimum.getPointRef()[1], 1.0e-6);
        Assert.assertEquals(0.04268731682389561, optimizer.getRMS(),       1.0e-8);
    }

// org.apache.commons.math.optimization.linear.SimplexSolverTest::testMath434NegativeVariable
    public void testMath434NegativeVariable() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] {0.0, 0.0, 1.0}, 0.0d);
        ArrayList<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] {1, 1, 0}, Relationship.EQ, 5));
        constraints.add(new LinearConstraint(new double[] {0, 0, 1}, Relationship.GEQ, -10));

        double epsilon = 1e-6;
        SimplexSolver solver = new SimplexSolver();
        RealPointValuePair solution = solver.optimize(f, constraints, GoalType.MINIMIZE, false);

        Assert.assertEquals(5.0, solution.getPoint()[0] + solution.getPoint()[1], epsilon);
        Assert.assertEquals(-10.0, solution.getPoint()[2], epsilon);
        Assert.assertEquals(-10.0, solution.getValue(), epsilon);

    }
