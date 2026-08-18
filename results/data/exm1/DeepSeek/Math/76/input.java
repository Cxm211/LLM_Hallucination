// buggy code
    public RealMatrix getU()
        throws InvalidMatrixException {

        if (cachedU == null) {

            final int p = singularValues.length;
            if (m >= n) {
                // the tridiagonal matrix is Bt.B, where B is upper bidiagonal
                final RealMatrix e =
                    eigenDecomposition.getV().getSubMatrix(0, p - 1, 0, p - 1);
                final double[][] eData = e.getData();
                final double[][] wData = new double[m][p];
                double[] ei1 = eData[0];
                for (int i = 0; i < p - 1; ++i) {
                    // compute W = B.E.S^(-1) where E is the eigenvectors matrix
                    final double mi = mainBidiagonal[i];
                    final double[] ei0 = ei1;
                    final double[] wi  = wData[i];
                        ei1 = eData[i + 1];
                        final double si = secondaryBidiagonal[i];
                        for (int j = 0; j < p; ++j) {
                            wi[j] = (mi * ei0[j] + si * ei1[j]) / singularValues[j];
                        }
                }
                        for (int j = 0; j < p; ++j) {
                            wData[p - 1][j] = ei1[j] * mainBidiagonal[p - 1] / singularValues[j];
                        }

                for (int i = p; i < m; ++i) {
                    wData[i] = new double[p];
                }
                cachedU =
                    transformer.getU().multiply(MatrixUtils.createRealMatrix(wData));
            } else {
                // the tridiagonal matrix is B.Bt, where B is lower bidiagonal
                final RealMatrix e =
                    eigenDecomposition.getV().getSubMatrix(0, m - 1, 0, p - 1);
                cachedU = transformer.getU().multiply(e);
            }

        }

        // return the cached matrix
        return cachedU;

    }

    public RealMatrix getV()
        throws InvalidMatrixException {

        if (cachedV == null) {

            final int p = singularValues.length;
            if (m >= n) {
                // the tridiagonal matrix is Bt.B, where B is upper bidiagonal
                final RealMatrix e =
                    eigenDecomposition.getV().getSubMatrix(0, n - 1, 0, p - 1);
                cachedV = transformer.getV().multiply(e);
            } else {
                // the tridiagonal matrix is B.Bt, where B is lower bidiagonal
                // compute W = Bt.E.S^(-1) where E is the eigenvectors matrix
                final RealMatrix e =
                    eigenDecomposition.getV().getSubMatrix(0, p - 1, 0, p - 1);
                final double[][] eData = e.getData();
                final double[][] wData = new double[n][p];
                double[] ei1 = eData[0];
                for (int i = 0; i < p - 1; ++i) {
                    final double mi = mainBidiagonal[i];
                    final double[] ei0 = ei1;
                    final double[] wi  = wData[i];
                        ei1 = eData[i + 1];
                        final double si = secondaryBidiagonal[i];
                        for (int j = 0; j < p; ++j) {
                            wi[j] = (mi * ei0[j] + si * ei1[j]) / singularValues[j];
                        }
                }
                        for (int j = 0; j < p; ++j) {
                            wData[p - 1][j] = ei1[j] * mainBidiagonal[p - 1] / singularValues[j];
                        }
                for (int i = p; i < n; ++i) {
                    wData[i] = new double[p];
                }
                cachedV =
                    transformer.getV().multiply(MatrixUtils.createRealMatrix(wData));
            }

        }

        // return the cached matrix
        return cachedV;

    }

// relevant test
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
            Assert.fail("an exception should have been thrown");
        } catch (IllegalArgumentException iae) {
            
        } catch (Exception e) {
            Assert.fail("wrong exception caught");
        }
        try {
            solver.solve(b.getColumn(0));
            Assert.fail("an exception should have been thrown");
        } catch (IllegalArgumentException iae) {
            
        } catch (Exception e) {
            Assert.fail("wrong exception caught");
        }
        try {
            solver.solve(new ArrayRealVectorTest.RealVectorTestImpl(b.getColumn(0)));
            Assert.fail("an exception should have been thrown");
        } catch (IllegalArgumentException iae) {
            
        } catch (Exception e) {
            Assert.fail("wrong exception caught");
        }
    }

// org.apache.commons.math.linear.SingularValueSolverTest::testLeastSquareSolve
    public void testLeastSquareSolve() {
        RealMatrix m =
            MatrixUtils.createRealMatrix(new double[][] {
                                   { 1.0, 0.0 },
                                   { 0.0, 0.0 }
                               });
        DecompositionSolver solver = new SingularValueDecompositionImpl(m).getSolver();
        RealMatrix b = MatrixUtils.createRealMatrix(new double[][] {
            { 11, 12 }, { 21, 22 } 
        });
        RealMatrix xMatrix = solver.solve(b);
        Assert.assertEquals(11, xMatrix.getEntry(0, 0), 1.0e-15);
        Assert.assertEquals(12, xMatrix.getEntry(0, 1), 1.0e-15);
        Assert.assertEquals(0, xMatrix.getEntry(1, 0), 1.0e-15);
        Assert.assertEquals(0, xMatrix.getEntry(1, 1), 1.0e-15);
        double[] xCol = solver.solve(b.getColumn(0));
        Assert.assertEquals(11, xCol[0], 1.0e-15);
        Assert.assertEquals(0, xCol[1], 1.0e-15);
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
            new SingularValueDecompositionImpl(MatrixUtils.createRealMatrix(testSquare)).getSolver();
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
                         new ArrayRealVector(solver.solve(b.getColumn(i))).subtract(xRef.getColumnVector(i)).getNorm(),
                         1.0e-13);
        }

        
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
        SingularValueDecompositionImpl svd =
            new SingularValueDecompositionImpl(MatrixUtils.createRealMatrix(testSquare));
        Assert.assertEquals(3.0, svd.getConditionNumber(), 1.0e-15);
    }

// org.apache.commons.math.linear.SingularValueSolverTest::testTruncated
    public void testTruncated() {

        RealMatrix rm = new Array2DRowRealMatrix(new double[][] {
            { 1.0, 2.0, 3.0 }, { 2.0, 3.0, 4.0 }, { 3.0, 5.0, 7.0 }
        });
        double s439  = Math.sqrt(439.0);
        double[] reference = new double[] {
            Math.sqrt(3.0 * (21.0 + s439))
        };
        SingularValueDecomposition svd =
            new SingularValueDecompositionImpl(rm, 1);

        
        double[] singularValues = svd.getSingularValues();
        Assert.assertEquals(reference.length, singularValues.length);
        for (int i = 0; i < reference.length; ++i) {
            Assert.assertEquals(reference[i], singularValues[i], 4.0e-13);
        }

        
        RealMatrix recomposed = svd.getU().multiply(svd.getS()).multiply(svd.getVT());
        Assert.assertTrue(recomposed.subtract(rm).getNorm() > 1.4);

    }

// org.apache.commons.math.linear.SingularValueSolverTest::testMath320A
    public void testMath320A() {
        RealMatrix rm = new Array2DRowRealMatrix(new double[][] {
            { 1.0, 2.0, 3.0 }, { 2.0, 3.0, 4.0 }, { 3.0, 5.0, 7.0 }
        });
        double s439  = Math.sqrt(439.0);
        double[] reference = new double[] {
            Math.sqrt(3.0 * (21.0 + s439)), Math.sqrt(3.0 * (21.0 - s439))
        };
        SingularValueDecomposition svd =
            new SingularValueDecompositionImpl(rm);

        
        double[] singularValues = svd.getSingularValues();
        Assert.assertEquals(reference.length, singularValues.length);
        for (int i = 0; i < reference.length; ++i) {
            Assert.assertEquals(reference[i], singularValues[i], 4.0e-13);
        }

        
        RealMatrix recomposed = svd.getU().multiply(svd.getS()).multiply(svd.getVT());
        Assert.assertEquals(0.0, recomposed.subtract(rm).getNorm(), 5.0e-13);

        
        double[] b = new double[] { 5.0, 6.0, 7.0 };
        double[] resSVD = svd.getSolver().solve(b);
        Assert.assertEquals(rm.getColumnDimension(), resSVD.length);

        
        double svdMinResidual = residual(rm, resSVD, b);
        double epsilon = 2 * Math.ulp(svdMinResidual);
        double h = 0.1;
        int    k = 3;
        for (double d0 = -k * h; d0 <= k * h; d0 += h) {
            for (double d1 = -k * h ; d1 <= k * h; d1 += h) {
                for (double d2 = -k * h; d2 <= k * h; d2 += h) {
                    double[] x = new double[] { resSVD[0] + d0, resSVD[1] + d1, resSVD[2] + d2 };
                    Assert.assertTrue((residual(rm, x, b) - svdMinResidual) > -epsilon);
                }
            }
        }

    }

// org.apache.commons.math.linear.SingularValueSolverTest::testMath320B
    public void testMath320B() {
        RealMatrix rm = new Array2DRowRealMatrix(new double[][] {
            { 1.0, 2.0 }, { 1.0, 2.0 }
        });
        SingularValueDecomposition svd =
            new SingularValueDecompositionImpl(rm);
        RealMatrix recomposed = svd.getU().multiply(svd.getS()).multiply(svd.getVT());
        Assert.assertEquals(0.0, recomposed.subtract(rm).getNorm(), 2.0e-15);
    }
