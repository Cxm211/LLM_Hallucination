// buggy code
    public CholeskyDecompositionImpl(final RealMatrix matrix,
                                     final double relativeSymmetryThreshold,
                                     final double absolutePositivityThreshold)
        throws NonSquareMatrixException,
               NotSymmetricMatrixException, NotPositiveDefiniteMatrixException {

        if (!matrix.isSquare()) {
            throw new NonSquareMatrixException(matrix.getRowDimension(),
                                               matrix.getColumnDimension());
        }

        final int order = matrix.getRowDimension();
        lTData   = matrix.getData();
        cachedL  = null;
        cachedLT = null;

        // check the matrix before transformation
        for (int i = 0; i < order; ++i) {

            final double[] lI = lTData[i];

            if (lTData[i][i] < absolutePositivityThreshold) {
                throw new NotPositiveDefiniteMatrixException();
            }
            // check off-diagonal elements (and reset them to 0)
            for (int j = i + 1; j < order; ++j) {
                final double[] lJ = lTData[j];
                final double lIJ = lI[j];
                final double lJI = lJ[i];
                final double maxDelta =
                    relativeSymmetryThreshold * Math.max(Math.abs(lIJ), Math.abs(lJI));
                if (Math.abs(lIJ - lJI) > maxDelta) {
                    throw new NotSymmetricMatrixException();
                }
                lJ[i] = 0;
           }
        }

        // transform the matrix
        for (int i = 0; i < order; ++i) {

            final double[] ltI = lTData[i];

            // check diagonal element

            ltI[i] = Math.sqrt(ltI[i]);
            final double inverse = 1.0 / ltI[i];

            for (int q = order - 1; q > i; --q) {
                ltI[q] *= inverse;
                final double[] ltQ = lTData[q];
                for (int p = q; p < order; ++p) {
                    ltQ[p] -= ltI[q] * ltI[p];
                }
            }

        }

    }

// relevant test
// org.apache.commons.math.linear.CholeskyDecompositionImplTest::testDimensions
    public void testDimensions() throws MathException {
        CholeskyDecomposition llt =
            new CholeskyDecompositionImpl(MatrixUtils.createRealMatrix(testData));
        assertEquals(testData.length, llt.getL().getRowDimension());
        assertEquals(testData.length, llt.getL().getColumnDimension());
        assertEquals(testData.length, llt.getLT().getRowDimension());
        assertEquals(testData.length, llt.getLT().getColumnDimension());
    }

// org.apache.commons.math.linear.CholeskyDecompositionImplTest::testNonSquare
    public void testNonSquare() throws MathException {
        new CholeskyDecompositionImpl(MatrixUtils.createRealMatrix(new double[3][2]));
    }

// org.apache.commons.math.linear.CholeskyDecompositionImplTest::testNotSymmetricMatrixException
    public void testNotSymmetricMatrixException() throws MathException {
        double[][] changed = testData.clone();
        changed[0][changed[0].length - 1] += 1.0e-5;
        new CholeskyDecompositionImpl(MatrixUtils.createRealMatrix(changed));
    }

// org.apache.commons.math.linear.CholeskyDecompositionImplTest::testNotPositiveDefinite
    public void testNotPositiveDefinite() throws MathException {
        CholeskyDecomposition cd = new CholeskyDecompositionImpl(MatrixUtils.createRealMatrix(new double[][] {
                { 14, 11, 13, 15, 24 },
                { 11, 34, 13, 8,  25 },
                { 13, 13, 14, 15, 21 },
                { 15, 8,  15, 18, 23 },
                { 24, 25, 21, 23, 45 }
        }));
        System.out.println(cd.getL().multiply(cd.getLT()));
    }

// org.apache.commons.math.linear.CholeskyDecompositionImplTest::testMath274
    public void testMath274() throws MathException {
        new CholeskyDecompositionImpl(MatrixUtils.createRealMatrix(new double[][] {
                { 0.40434286, -0.09376327, 0.30328980, 0.04909388 },
                {-0.09376327,  0.10400408, 0.07137959, 0.04762857 },
                { 0.30328980,  0.07137959, 0.30458776, 0.04882449 },
                { 0.04909388,  0.04762857, 0.04882449, 0.07543265 }
            
        }));
    }

// org.apache.commons.math.linear.CholeskyDecompositionImplTest::testAEqualLLT
    public void testAEqualLLT() throws MathException {
        RealMatrix matrix = MatrixUtils.createRealMatrix(testData);
        CholeskyDecomposition llt = new CholeskyDecompositionImpl(matrix);
        RealMatrix l  = llt.getL();
        RealMatrix lt = llt.getLT();
        double norm = l.multiply(lt).subtract(matrix).getNorm();
        assertEquals(0, norm, 1.0e-15);
    }

// org.apache.commons.math.linear.CholeskyDecompositionImplTest::testLLowerTriangular
    public void testLLowerTriangular() throws MathException {
        RealMatrix matrix = MatrixUtils.createRealMatrix(testData);
        RealMatrix l = new CholeskyDecompositionImpl(matrix).getL();
        for (int i = 0; i < l.getRowDimension(); i++) {
            for (int j = i + 1; j < l.getColumnDimension(); j++) {
                assertEquals(0.0, l.getEntry(i, j), 0.0);
            }
        }
    }

// org.apache.commons.math.linear.CholeskyDecompositionImplTest::testLTTransposed
    public void testLTTransposed() throws MathException {
        RealMatrix matrix = MatrixUtils.createRealMatrix(testData);
        CholeskyDecomposition llt = new CholeskyDecompositionImpl(matrix);
        RealMatrix l  = llt.getL();
        RealMatrix lt = llt.getLT();
        double norm = l.subtract(lt.transpose()).getNorm();
        assertEquals(0, norm, 1.0e-15);
    }

// org.apache.commons.math.linear.CholeskyDecompositionImplTest::testMatricesValues
    public void testMatricesValues() throws MathException {
        RealMatrix lRef = MatrixUtils.createRealMatrix(new double[][] {
                {  1,  0,  0,  0,  0 },
                {  2,  3,  0,  0,  0 },
                {  4,  5,  6,  0,  0 },
                {  7,  8,  9, 10,  0 },
                { 11, 12, 13, 14, 15 }
        });
       CholeskyDecomposition llt =
            new CholeskyDecompositionImpl(MatrixUtils.createRealMatrix(testData));

        
        RealMatrix l = llt.getL();
        assertEquals(0, l.subtract(lRef).getNorm(), 1.0e-13);
        RealMatrix lt = llt.getLT();
        assertEquals(0, lt.subtract(lRef.transpose()).getNorm(), 1.0e-13);

        
        assertTrue(l  == llt.getL());
        assertTrue(lt == llt.getLT());
        
    }

// org.apache.commons.math.linear.CholeskySolverTest::testSolveDimensionErrors
    public void testSolveDimensionErrors() throws MathException {
        DecompositionSolver solver =
            new CholeskyDecompositionImpl(MatrixUtils.createRealMatrix(testData)).getSolver();
        RealMatrix b = MatrixUtils.createRealMatrix(new double[2][2]);
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
            solver.solve(new RealVectorImplTest.RealVectorTestImpl(b.getColumn(0)));
            fail("an exception should have been thrown");
        } catch (IllegalArgumentException iae) {
            
        } catch (Exception e) {
            fail("wrong exception caught");
        }
    }

// org.apache.commons.math.linear.CholeskySolverTest::testSolve
    public void testSolve() throws MathException {
        DecompositionSolver solver =
            new CholeskyDecompositionImpl(MatrixUtils.createRealMatrix(testData)).getSolver();
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

        
        assertEquals(0, solver.solve(b).subtract(xRef).getNorm(), 1.0e-13);

        
        for (int i = 0; i < b.getColumnDimension(); ++i) {
            assertEquals(0,
                         new RealVectorImpl(solver.solve(b.getColumn(i))).subtract(xRef.getColumnVector(i)).getNorm(),
                         1.0e-13);
        }

        
        for (int i = 0; i < b.getColumnDimension(); ++i) {
            assertEquals(0,
                         solver.solve(b.getColumnVector(i)).subtract(xRef.getColumnVector(i)).getNorm(),
                         1.0e-13);
        }

        
        for (int i = 0; i < b.getColumnDimension(); ++i) {
            RealVectorImplTest.RealVectorTestImpl v =
                new RealVectorImplTest.RealVectorTestImpl(b.getColumn(i));
            assertEquals(0,
                         solver.solve(v).subtract(xRef.getColumnVector(i)).getNorm(),
                         1.0e-13);
        }

    }

// org.apache.commons.math.linear.CholeskySolverTest::testDeterminant
    public void testDeterminant() throws MathException {
        assertEquals(7290000.0, getDeterminant(MatrixUtils.createRealMatrix(testData)), 1.0e-15);
    }
