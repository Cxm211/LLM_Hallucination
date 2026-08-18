// buggy code
    public static float max(final float a, final float b) {
        return (a <= b) ? b : (Float.isNaN(a + b) ? Float.NaN : b);
    }

// relevant test
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
        } catch (SingularMatrixException ime) {
            
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
        } catch (MathIllegalArgumentException iae) {
            
        }
        try {
            es.solve(b.getColumn(0));
            fail("an exception should have been thrown");
        } catch (MathIllegalArgumentException iae) {
            
        }
        try {
            es.solve(new ArrayRealVectorTest.RealVectorTestImpl(b.getColumn(0)));
            fail("an exception should have been thrown");
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

        
        RealMatrix solution=es.solve(b);
        assertEquals(0, solution.subtract(xRef).getNorm(), 2.5e-12);

        
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

// org.apache.commons.math.linear.FieldLUDecompositionImplTest::testDimensions
    public void testDimensions() {
        FieldMatrix<Fraction> matrix = new Array2DRowFieldMatrix<Fraction>(testData);
        FieldLUDecomposition<Fraction> LU = new FieldLUDecompositionImpl<Fraction>(matrix);
        assertEquals(testData.length, LU.getL().getRowDimension());
        assertEquals(testData.length, LU.getL().getColumnDimension());
        assertEquals(testData.length, LU.getU().getRowDimension());
        assertEquals(testData.length, LU.getU().getColumnDimension());
        assertEquals(testData.length, LU.getP().getRowDimension());
        assertEquals(testData.length, LU.getP().getColumnDimension());

    }

// org.apache.commons.math.linear.FieldLUDecompositionImplTest::testNonSquare
    public void testNonSquare() {
        try {
            new FieldLUDecompositionImpl<Fraction>(new Array2DRowFieldMatrix<Fraction>(new Fraction[][] {
                    { Fraction.ZERO, Fraction.ZERO },
                    { Fraction.ZERO, Fraction.ZERO },
                    { Fraction.ZERO, Fraction.ZERO }
            }));
        } catch (NonSquareMatrixException ime) {
            
        }
    }

// org.apache.commons.math.linear.FieldLUDecompositionImplTest::testPAEqualLU
    public void testPAEqualLU() {
        FieldMatrix<Fraction> matrix = new Array2DRowFieldMatrix<Fraction>(testData);
        FieldLUDecomposition<Fraction> lu = new FieldLUDecompositionImpl<Fraction>(matrix);
        FieldMatrix<Fraction> l = lu.getL();
        FieldMatrix<Fraction> u = lu.getU();
        FieldMatrix<Fraction> p = lu.getP();
        TestUtils.assertEquals(p.multiply(matrix), l.multiply(u));

        matrix = new Array2DRowFieldMatrix<Fraction>(testDataMinus);
        lu = new FieldLUDecompositionImpl<Fraction>(matrix);
        l = lu.getL();
        u = lu.getU();
        p = lu.getP();
        TestUtils.assertEquals(p.multiply(matrix), l.multiply(u));

        matrix = new Array2DRowFieldMatrix<Fraction>(FractionField.getInstance(), 17, 17);
        for (int i = 0; i < matrix.getRowDimension(); ++i) {
            matrix.setEntry(i, i, Fraction.ONE);
        }
        lu = new FieldLUDecompositionImpl<Fraction>(matrix);
        l = lu.getL();
        u = lu.getU();
        p = lu.getP();
        TestUtils.assertEquals(p.multiply(matrix), l.multiply(u));

        matrix = new Array2DRowFieldMatrix<Fraction>(singular);
        lu = new FieldLUDecompositionImpl<Fraction>(matrix);
        assertFalse(lu.getSolver().isNonSingular());
        assertNull(lu.getL());
        assertNull(lu.getU());
        assertNull(lu.getP());

        matrix = new Array2DRowFieldMatrix<Fraction>(bigSingular);
        lu = new FieldLUDecompositionImpl<Fraction>(matrix);
        assertFalse(lu.getSolver().isNonSingular());
        assertNull(lu.getL());
        assertNull(lu.getU());
        assertNull(lu.getP());

    }

// org.apache.commons.math.linear.FieldLUDecompositionImplTest::testLLowerTriangular
    public void testLLowerTriangular() {
        FieldMatrix<Fraction> matrix = new Array2DRowFieldMatrix<Fraction>(testData);
        FieldMatrix<Fraction> l = new FieldLUDecompositionImpl<Fraction>(matrix).getL();
        for (int i = 0; i < l.getRowDimension(); i++) {
            assertEquals(Fraction.ONE, l.getEntry(i, i));
            for (int j = i + 1; j < l.getColumnDimension(); j++) {
                assertEquals(Fraction.ZERO, l.getEntry(i, j));
            }
        }
    }

// org.apache.commons.math.linear.FieldLUDecompositionImplTest::testUUpperTriangular
    public void testUUpperTriangular() {
        FieldMatrix<Fraction> matrix = new Array2DRowFieldMatrix<Fraction>(testData);
        FieldMatrix<Fraction> u = new FieldLUDecompositionImpl<Fraction>(matrix).getU();
        for (int i = 0; i < u.getRowDimension(); i++) {
            for (int j = 0; j < i; j++) {
                assertEquals(Fraction.ZERO, u.getEntry(i, j));
            }
        }
    }

// org.apache.commons.math.linear.FieldLUDecompositionImplTest::testPPermutation
    public void testPPermutation() {
        FieldMatrix<Fraction> matrix = new Array2DRowFieldMatrix<Fraction>(testData);
        FieldMatrix<Fraction> p   = new FieldLUDecompositionImpl<Fraction>(matrix).getP();

        FieldMatrix<Fraction> ppT = p.multiply(p.transpose());
        FieldMatrix<Fraction> id  =
            new Array2DRowFieldMatrix<Fraction>(FractionField.getInstance(),
                                          p.getRowDimension(), p.getRowDimension());
        for (int i = 0; i < id.getRowDimension(); ++i) {
            id.setEntry(i, i, Fraction.ONE);
        }
        TestUtils.assertEquals(id, ppT);

        for (int i = 0; i < p.getRowDimension(); i++) {
            int zeroCount  = 0;
            int oneCount   = 0;
            int otherCount = 0;
            for (int j = 0; j < p.getColumnDimension(); j++) {
                final Fraction e = p.getEntry(i, j);
                if (e.equals(Fraction.ZERO)) {
                    ++zeroCount;
                } else if (e.equals(Fraction.ONE)) {
                    ++oneCount;
                } else {
                    ++otherCount;
                }
            }
            assertEquals(p.getColumnDimension() - 1, zeroCount);
            assertEquals(1, oneCount);
            assertEquals(0, otherCount);
        }

        for (int j = 0; j < p.getColumnDimension(); j++) {
            int zeroCount  = 0;
            int oneCount   = 0;
            int otherCount = 0;
            for (int i = 0; i < p.getRowDimension(); i++) {
                final Fraction e = p.getEntry(i, j);
                if (e.equals(Fraction.ZERO)) {
                    ++zeroCount;
                } else if (e.equals(Fraction.ONE)) {
                    ++oneCount;
                } else {
                    ++otherCount;
                }
            }
            assertEquals(p.getRowDimension() - 1, zeroCount);
            assertEquals(1, oneCount);
            assertEquals(0, otherCount);
        }

    }

// org.apache.commons.math.linear.FieldLUDecompositionImplTest::testSingular
    public void testSingular() {
        FieldLUDecomposition<Fraction> lu =
            new FieldLUDecompositionImpl<Fraction>(new Array2DRowFieldMatrix<Fraction>(testData));
        assertTrue(lu.getSolver().isNonSingular());
        lu = new FieldLUDecompositionImpl<Fraction>(new Array2DRowFieldMatrix<Fraction>(singular));
        assertFalse(lu.getSolver().isNonSingular());
        lu = new FieldLUDecompositionImpl<Fraction>(new Array2DRowFieldMatrix<Fraction>(bigSingular));
        assertFalse(lu.getSolver().isNonSingular());
    }

// org.apache.commons.math.linear.FieldLUDecompositionImplTest::testMatricesValues1
    public void testMatricesValues1() {
       FieldLUDecomposition<Fraction> lu =
            new FieldLUDecompositionImpl<Fraction>(new Array2DRowFieldMatrix<Fraction>(testData));
        FieldMatrix<Fraction> lRef = new Array2DRowFieldMatrix<Fraction>(new Fraction[][] {
                { new Fraction(1), new Fraction(0), new Fraction(0) },
                { new Fraction(2), new Fraction(1), new Fraction(0) },
                { new Fraction(1), new Fraction(-2), new Fraction(1) }
        });
        FieldMatrix<Fraction> uRef = new Array2DRowFieldMatrix<Fraction>(new Fraction[][] {
                { new Fraction(1),  new Fraction(2), new Fraction(3) },
                { new Fraction(0), new Fraction(1), new Fraction(-3) },
                { new Fraction(0),  new Fraction(0), new Fraction(-1) }
        });
        FieldMatrix<Fraction> pRef = new Array2DRowFieldMatrix<Fraction>(new Fraction[][] {
                { new Fraction(1), new Fraction(0), new Fraction(0) },
                { new Fraction(0), new Fraction(1), new Fraction(0) },
                { new Fraction(0), new Fraction(0), new Fraction(1) }
        });
        int[] pivotRef = { 0, 1, 2 };

        
        FieldMatrix<Fraction> l = lu.getL();
        TestUtils.assertEquals(lRef, l);
        FieldMatrix<Fraction> u = lu.getU();
        TestUtils.assertEquals(uRef, u);
        FieldMatrix<Fraction> p = lu.getP();
        TestUtils.assertEquals(pRef, p);
        int[] pivot = lu.getPivot();
        for (int i = 0; i < pivotRef.length; ++i) {
            assertEquals(pivotRef[i], pivot[i]);
        }

        
        assertTrue(l == lu.getL());
        assertTrue(u == lu.getU());
        assertTrue(p == lu.getP());

    }

// org.apache.commons.math.linear.FieldLUDecompositionImplTest::testMatricesValues2
    public void testMatricesValues2() {
       FieldLUDecomposition<Fraction> lu =
            new FieldLUDecompositionImpl<Fraction>(new Array2DRowFieldMatrix<Fraction>(luData));
        FieldMatrix<Fraction> lRef = new Array2DRowFieldMatrix<Fraction>(new Fraction[][] {
                { new Fraction(1), new Fraction(0), new Fraction(0) },
                { new Fraction(3), new Fraction(1), new Fraction(0) },
                { new Fraction(1), new Fraction(0), new Fraction(1) }
        });
        FieldMatrix<Fraction> uRef = new Array2DRowFieldMatrix<Fraction>(new Fraction[][] {
                { new Fraction(2), new Fraction(3), new Fraction(3)    },
                { new Fraction(0), new Fraction(-3), new Fraction(-1)  },
                { new Fraction(0), new Fraction(0), new Fraction(4) }
        });
        FieldMatrix<Fraction> pRef = new Array2DRowFieldMatrix<Fraction>(new Fraction[][] {
                { new Fraction(1), new Fraction(0), new Fraction(0) },
                { new Fraction(0), new Fraction(0), new Fraction(1) },
                { new Fraction(0), new Fraction(1), new Fraction(0) }
        });
        int[] pivotRef = { 0, 2, 1 };

        
        FieldMatrix<Fraction> l = lu.getL();
        TestUtils.assertEquals(lRef, l);
        FieldMatrix<Fraction> u = lu.getU();
        TestUtils.assertEquals(uRef, u);
        FieldMatrix<Fraction> p = lu.getP();
        TestUtils.assertEquals(pRef, p);
        int[] pivot = lu.getPivot();
        for (int i = 0; i < pivotRef.length; ++i) {
            assertEquals(pivotRef[i], pivot[i]);
        }

        
        assertTrue(l == lu.getL());
        assertTrue(u == lu.getU());
        assertTrue(p == lu.getP());

    }

// org.apache.commons.math.linear.FieldMatrixImplTest::testDimensions
    public void testDimensions() {
        Array2DRowFieldMatrix<Fraction> m = new Array2DRowFieldMatrix<Fraction>(testData);
        Array2DRowFieldMatrix<Fraction> m2 = new Array2DRowFieldMatrix<Fraction>(testData2);
        assertEquals("testData row dimension",3,m.getRowDimension());
        assertEquals("testData column dimension",3,m.getColumnDimension());
        assertTrue("testData is square",m.isSquare());
        assertEquals("testData2 row dimension",m2.getRowDimension(),2);
        assertEquals("testData2 column dimension",m2.getColumnDimension(),3);
        assertTrue("testData2 is not square",!m2.isSquare());
    }

// org.apache.commons.math.linear.FieldMatrixImplTest::testCopyFunctions
    public void testCopyFunctions() {
        Array2DRowFieldMatrix<Fraction> m1 = new Array2DRowFieldMatrix<Fraction>(testData);
        Array2DRowFieldMatrix<Fraction> m2 = new Array2DRowFieldMatrix<Fraction>(m1.getData());
        assertEquals(m2,m1);
        Array2DRowFieldMatrix<Fraction> m3 = new Array2DRowFieldMatrix<Fraction>(testData);
        Array2DRowFieldMatrix<Fraction> m4 = new Array2DRowFieldMatrix<Fraction>(m3.getData(), false);
        assertEquals(m4,m3);
    }

// org.apache.commons.math.linear.FieldMatrixImplTest::testAdd
    public void testAdd() {
        Array2DRowFieldMatrix<Fraction> m = new Array2DRowFieldMatrix<Fraction>(testData);
        Array2DRowFieldMatrix<Fraction> mInv = new Array2DRowFieldMatrix<Fraction>(testDataInv);
        FieldMatrix<Fraction> mPlusMInv = m.add(mInv);
        Fraction[][] sumEntries = mPlusMInv.getData();
        for (int row = 0; row < m.getRowDimension(); row++) {
            for (int col = 0; col < m.getColumnDimension(); col++) {
                assertEquals(testDataPlusInv[row][col],sumEntries[row][col]);
            }
        }
    }

// org.apache.commons.math.linear.FieldMatrixImplTest::testAddFail
    public void testAddFail() {
        Array2DRowFieldMatrix<Fraction> m = new Array2DRowFieldMatrix<Fraction>(testData);
        Array2DRowFieldMatrix<Fraction> m2 = new Array2DRowFieldMatrix<Fraction>(testData2);
        try {
            m.add(m2);
            fail("MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.linear.FieldMatrixImplTest::testPlusMinus
    public void testPlusMinus() {
        Array2DRowFieldMatrix<Fraction> m = new Array2DRowFieldMatrix<Fraction>(testData);
        Array2DRowFieldMatrix<Fraction> m2 = new Array2DRowFieldMatrix<Fraction>(testDataInv);
        TestUtils.assertEquals(m.subtract(m2),m2.scalarMultiply(new Fraction(-1)).add(m));
        try {
            m.subtract(new Array2DRowFieldMatrix<Fraction>(testData2));
            fail("Expecting illegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.linear.FieldMatrixImplTest::testMultiply
     public void testMultiply() {
        Array2DRowFieldMatrix<Fraction> m = new Array2DRowFieldMatrix<Fraction>(testData);
        Array2DRowFieldMatrix<Fraction> mInv = new Array2DRowFieldMatrix<Fraction>(testDataInv);
        Array2DRowFieldMatrix<Fraction> identity = new Array2DRowFieldMatrix<Fraction>(id);
        Array2DRowFieldMatrix<Fraction> m2 = new Array2DRowFieldMatrix<Fraction>(testData2);
        TestUtils.assertEquals(m.multiply(mInv), identity);
        TestUtils.assertEquals(mInv.multiply(m), identity);
        TestUtils.assertEquals(m.multiply(identity), m);
        TestUtils.assertEquals(identity.multiply(mInv), mInv);
        TestUtils.assertEquals(m2.multiply(identity), m2);
        try {
            m.multiply(new Array2DRowFieldMatrix<Fraction>(bigSingular));
            fail("Expecting illegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.linear.FieldMatrixImplTest::testMultiply2
    public void testMultiply2() {
       FieldMatrix<Fraction> m3 = new Array2DRowFieldMatrix<Fraction>(d3);
       FieldMatrix<Fraction> m4 = new Array2DRowFieldMatrix<Fraction>(d4);
       FieldMatrix<Fraction> m5 = new Array2DRowFieldMatrix<Fraction>(d5);
       TestUtils.assertEquals(m3.multiply(m4), m5);
   }

// org.apache.commons.math.linear.FieldMatrixImplTest::testTrace
    public void testTrace() {
        FieldMatrix<Fraction> m = new Array2DRowFieldMatrix<Fraction>(id);
        assertEquals("identity trace",new Fraction(3),m.getTrace());
        m = new Array2DRowFieldMatrix<Fraction>(testData2);
        try {
            m.getTrace();
            fail("Expecting NonSquareMatrixException");
        } catch (NonSquareMatrixException ex) {
            
        }
    }

// org.apache.commons.math.linear.FieldMatrixImplTest::testScalarAdd
    public void testScalarAdd() {
        FieldMatrix<Fraction> m = new Array2DRowFieldMatrix<Fraction>(testData);
        TestUtils.assertEquals(new Array2DRowFieldMatrix<Fraction>(testDataPlus2), m.scalarAdd(new Fraction(2)));
    }

// org.apache.commons.math.linear.FieldMatrixImplTest::testOperate
    public void testOperate() {
        FieldMatrix<Fraction> m = new Array2DRowFieldMatrix<Fraction>(id);
        TestUtils.assertEquals(testVector, m.operate(testVector));
        TestUtils.assertEquals(testVector, m.operate(new ArrayFieldVector<Fraction>(testVector)).getData());
        m = new Array2DRowFieldMatrix<Fraction>(bigSingular);
        try {
            m.operate(testVector);
            fail("Expecting illegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.linear.FieldMatrixImplTest::testMath209
    public void testMath209() {
        FieldMatrix<Fraction> a = new Array2DRowFieldMatrix<Fraction>(new Fraction[][] {
                { new Fraction(1), new Fraction(2) }, { new Fraction(3), new Fraction(4) }, { new Fraction(5), new Fraction(6) }
        }, false);
        Fraction[] b = a.operate(new Fraction[] { new Fraction(1), new Fraction(1) });
        assertEquals(a.getRowDimension(), b.length);
        assertEquals( new Fraction(3), b[0]);
        assertEquals( new Fraction(7), b[1]);
        assertEquals(new Fraction(11), b[2]);
    }

// org.apache.commons.math.linear.FieldMatrixImplTest::testTranspose
    public void testTranspose() {
        FieldMatrix<Fraction> m = new Array2DRowFieldMatrix<Fraction>(testData);
        FieldMatrix<Fraction> mIT = new FieldLUDecompositionImpl<Fraction>(m).getSolver().getInverse().transpose();
        FieldMatrix<Fraction> mTI = new FieldLUDecompositionImpl<Fraction>(m.transpose()).getSolver().getInverse();
        TestUtils.assertEquals(mIT, mTI);
        m = new Array2DRowFieldMatrix<Fraction>(testData2);
        FieldMatrix<Fraction> mt = new Array2DRowFieldMatrix<Fraction>(testData2T);
        TestUtils.assertEquals(mt, m.transpose());
    }

// org.apache.commons.math.linear.FieldMatrixImplTest::testPremultiplyVector
    public void testPremultiplyVector() {
        FieldMatrix<Fraction> m = new Array2DRowFieldMatrix<Fraction>(testData);
        TestUtils.assertEquals(m.preMultiply(testVector), preMultTest);
        TestUtils.assertEquals(m.preMultiply(new ArrayFieldVector<Fraction>(testVector).getData()),
                               preMultTest);
        m = new Array2DRowFieldMatrix<Fraction>(bigSingular);
        try {
            m.preMultiply(testVector);
            fail("expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.linear.FieldMatrixImplTest::testPremultiply
    public void testPremultiply() {
        FieldMatrix<Fraction> m3 = new Array2DRowFieldMatrix<Fraction>(d3);
        FieldMatrix<Fraction> m4 = new Array2DRowFieldMatrix<Fraction>(d4);
        FieldMatrix<Fraction> m5 = new Array2DRowFieldMatrix<Fraction>(d5);
        TestUtils.assertEquals(m4.preMultiply(m3), m5);

        Array2DRowFieldMatrix<Fraction> m = new Array2DRowFieldMatrix<Fraction>(testData);
        Array2DRowFieldMatrix<Fraction> mInv = new Array2DRowFieldMatrix<Fraction>(testDataInv);
        Array2DRowFieldMatrix<Fraction> identity = new Array2DRowFieldMatrix<Fraction>(id);
        TestUtils.assertEquals(m.preMultiply(mInv), identity);
        TestUtils.assertEquals(mInv.preMultiply(m), identity);
        TestUtils.assertEquals(m.preMultiply(identity), m);
        TestUtils.assertEquals(identity.preMultiply(mInv), mInv);
        try {
            m.preMultiply(new Array2DRowFieldMatrix<Fraction>(bigSingular));
            fail("Expecting illegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.linear.FieldMatrixImplTest::testGetVectors
    public void testGetVectors() {
        FieldMatrix<Fraction> m = new Array2DRowFieldMatrix<Fraction>(testData);
        TestUtils.assertEquals(m.getRow(0), testDataRow1);
        TestUtils.assertEquals(m.getColumn(2), testDataCol3);
        try {
            m.getRow(10);
            fail("expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.getColumn(-1);
            fail("expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
    }

// org.apache.commons.math.linear.FieldMatrixImplTest::testGetEntry
    public void testGetEntry() {
        FieldMatrix<Fraction> m = new Array2DRowFieldMatrix<Fraction>(testData);
        assertEquals("get entry", m.getEntry(0,1), new Fraction(2));
        try {
            m.getEntry(10, 4);
            fail ("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
    }

// org.apache.commons.math.linear.FieldMatrixImplTest::testExamples
    public void testExamples() {
        
        Fraction[][] matrixData = {
                {new Fraction(1),new Fraction(2),new Fraction(3)},
                {new Fraction(2),new Fraction(5),new Fraction(3)}
        };
        FieldMatrix<Fraction> m = new Array2DRowFieldMatrix<Fraction>(matrixData);
        
        Fraction[][] matrixData2 = {
                {new Fraction(1),new Fraction(2)},
                {new Fraction(2),new Fraction(5)},
                {new Fraction(1), new Fraction(7)}
        };
        FieldMatrix<Fraction> n = new Array2DRowFieldMatrix<Fraction>(matrixData2);
        
        FieldMatrix<Fraction> p = m.multiply(n);
        assertEquals(2, p.getRowDimension());
        assertEquals(2, p.getColumnDimension());
        
        FieldMatrix<Fraction> pInverse = new FieldLUDecompositionImpl<Fraction>(p).getSolver().getInverse();
        assertEquals(2, pInverse.getRowDimension());
        assertEquals(2, pInverse.getColumnDimension());

        
        Fraction[][] coefficientsData = {
                {new Fraction(2), new Fraction(3), new Fraction(-2)},
                {new Fraction(-1), new Fraction(7), new Fraction(6)},
                {new Fraction(4), new Fraction(-3), new Fraction(-5)}
        };
        FieldMatrix<Fraction> coefficients = new Array2DRowFieldMatrix<Fraction>(coefficientsData);
        Fraction[] constants = {new Fraction(1), new Fraction(-2), new Fraction(1)};
        Fraction[] solution = new FieldLUDecompositionImpl<Fraction>(coefficients).getSolver().solve(constants);
        assertEquals(new Fraction(2).multiply(solution[0]).
                     add(new Fraction(3).multiply(solution[1])).
                     subtract(new Fraction(2).multiply(solution[2])), constants[0]);
        assertEquals(new Fraction(-1).multiply(solution[0]).
                     add(new Fraction(7).multiply(solution[1])).
                     add(new Fraction(6).multiply(solution[2])), constants[1]);
        assertEquals(new Fraction(4).multiply(solution[0]).
                     subtract(new Fraction(3).multiply(solution[1])).
                     subtract(new Fraction(5).multiply(solution[2])), constants[2]);

    }

// org.apache.commons.math.linear.FieldMatrixImplTest::testGetSubMatrix
    public void testGetSubMatrix() {
        FieldMatrix<Fraction> m = new Array2DRowFieldMatrix<Fraction>(subTestData);
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

// org.apache.commons.math.linear.FieldMatrixImplTest::testCopySubMatrix
    public void testCopySubMatrix() {
        FieldMatrix<Fraction> m = new Array2DRowFieldMatrix<Fraction>(subTestData);
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

// org.apache.commons.math.linear.FieldMatrixImplTest::testGetRowMatrix
    public void testGetRowMatrix() {
        FieldMatrix<Fraction> m = new Array2DRowFieldMatrix<Fraction>(subTestData);
        FieldMatrix<Fraction> mRow0 = new Array2DRowFieldMatrix<Fraction>(subRow0);
        FieldMatrix<Fraction> mRow3 = new Array2DRowFieldMatrix<Fraction>(subRow3);
        assertEquals("Row0", mRow0,
                m.getRowMatrix(0));
        assertEquals("Row3", mRow3,
                m.getRowMatrix(3));
        try {
            m.getRowMatrix(-1);
            fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.getRowMatrix(4);
            fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
    }

// org.apache.commons.math.linear.FieldMatrixImplTest::testSetRowMatrix
    public void testSetRowMatrix() {
        FieldMatrix<Fraction> m = new Array2DRowFieldMatrix<Fraction>(subTestData);
        FieldMatrix<Fraction> mRow3 = new Array2DRowFieldMatrix<Fraction>(subRow3);
        assertNotSame(mRow3, m.getRowMatrix(0));
        m.setRowMatrix(0, mRow3);
        assertEquals(mRow3, m.getRowMatrix(0));
        try {
            m.setRowMatrix(-1, mRow3);
            fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.setRowMatrix(0, m);
            fail("Expecting MatrixDimensionMismatchException");
        } catch (MatrixDimensionMismatchException ex) {
            
        }
    }

// org.apache.commons.math.linear.FieldMatrixImplTest::testGetColumnMatrix
    public void testGetColumnMatrix() {
        FieldMatrix<Fraction> m = new Array2DRowFieldMatrix<Fraction>(subTestData);
        FieldMatrix<Fraction> mColumn1 = new Array2DRowFieldMatrix<Fraction>(subColumn1);
        FieldMatrix<Fraction> mColumn3 = new Array2DRowFieldMatrix<Fraction>(subColumn3);
        assertEquals("Column1", mColumn1,
                m.getColumnMatrix(1));
        assertEquals("Column3", mColumn3,
                m.getColumnMatrix(3));
        try {
            m.getColumnMatrix(-1);
            fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.getColumnMatrix(4);
            fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
    }

// org.apache.commons.math.linear.FieldMatrixImplTest::testSetColumnMatrix
    public void testSetColumnMatrix() {
        FieldMatrix<Fraction> m = new Array2DRowFieldMatrix<Fraction>(subTestData);
        FieldMatrix<Fraction> mColumn3 = new Array2DRowFieldMatrix<Fraction>(subColumn3);
        assertNotSame(mColumn3, m.getColumnMatrix(1));
        m.setColumnMatrix(1, mColumn3);
        assertEquals(mColumn3, m.getColumnMatrix(1));
        try {
            m.setColumnMatrix(-1, mColumn3);
            fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.setColumnMatrix(0, m);
            fail("Expecting MatrixDimensionMismatchException");
        } catch (MatrixDimensionMismatchException ex) {
            
        }
    }

// org.apache.commons.math.linear.FieldMatrixImplTest::testGetRowVector
    public void testGetRowVector() {
        FieldMatrix<Fraction> m = new Array2DRowFieldMatrix<Fraction>(subTestData);
        FieldVector<Fraction> mRow0 = new ArrayFieldVector<Fraction>(subRow0[0]);
        FieldVector<Fraction> mRow3 = new ArrayFieldVector<Fraction>(subRow3[0]);
        assertEquals("Row0", mRow0, m.getRowVector(0));
        assertEquals("Row3", mRow3, m.getRowVector(3));
        try {
            m.getRowVector(-1);
            fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.getRowVector(4);
            fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
    }

// org.apache.commons.math.linear.FieldMatrixImplTest::testSetRowVector
    public void testSetRowVector() {
        FieldMatrix<Fraction> m = new Array2DRowFieldMatrix<Fraction>(subTestData);
        FieldVector<Fraction> mRow3 = new ArrayFieldVector<Fraction>(subRow3[0]);
        assertNotSame(mRow3, m.getRowMatrix(0));
        m.setRowVector(0, mRow3);
        assertEquals(mRow3, m.getRowVector(0));
        try {
            m.setRowVector(-1, mRow3);
            fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.setRowVector(0, new ArrayFieldVector<Fraction>(FractionField.getInstance(), 5));
            fail("Expecting MatrixDimensionMismatchException");
        } catch (MatrixDimensionMismatchException ex) {
            
        }
    }

// org.apache.commons.math.linear.FieldMatrixImplTest::testGetColumnVector
    public void testGetColumnVector() {
        FieldMatrix<Fraction> m = new Array2DRowFieldMatrix<Fraction>(subTestData);
        FieldVector<Fraction> mColumn1 = columnToVector(subColumn1);
        FieldVector<Fraction> mColumn3 = columnToVector(subColumn3);
        assertEquals("Column1", mColumn1, m.getColumnVector(1));
        assertEquals("Column3", mColumn3, m.getColumnVector(3));
        try {
            m.getColumnVector(-1);
            fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.getColumnVector(4);
            fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
    }

// org.apache.commons.math.linear.FieldMatrixImplTest::testSetColumnVector
    public void testSetColumnVector() {
        FieldMatrix<Fraction> m = new Array2DRowFieldMatrix<Fraction>(subTestData);
        FieldVector<Fraction> mColumn3 = columnToVector(subColumn3);
        assertNotSame(mColumn3, m.getColumnVector(1));
        m.setColumnVector(1, mColumn3);
        assertEquals(mColumn3, m.getColumnVector(1));
        try {
            m.setColumnVector(-1, mColumn3);
            fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.setColumnVector(0, new ArrayFieldVector<Fraction>(FractionField.getInstance(), 5));
            fail("Expecting MatrixDimensionMismatchException");
        } catch (MatrixDimensionMismatchException ex) {
            
        }
    }

// org.apache.commons.math.linear.FieldMatrixImplTest::testGetRow
    public void testGetRow() {
        FieldMatrix<Fraction> m = new Array2DRowFieldMatrix<Fraction>(subTestData);
        checkArrays(subRow0[0], m.getRow(0));
        checkArrays(subRow3[0], m.getRow(3));
        try {
            m.getRow(-1);
            fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.getRow(4);
            fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
    }

// org.apache.commons.math.linear.FieldMatrixImplTest::testSetRow
    public void testSetRow() {
        FieldMatrix<Fraction> m = new Array2DRowFieldMatrix<Fraction>(subTestData);
        assertTrue(subRow3[0][0] != m.getRow(0)[0]);
        m.setRow(0, subRow3[0]);
        checkArrays(subRow3[0], m.getRow(0));
        try {
            m.setRow(-1, subRow3[0]);
            fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.setRow(0, new Fraction[5]);
            fail("Expecting MatrixDimensionMismatchException");
        } catch (MatrixDimensionMismatchException ex) {
            
        }
    }

// org.apache.commons.math.linear.FieldMatrixImplTest::testGetColumn
    public void testGetColumn() {
        FieldMatrix<Fraction> m = new Array2DRowFieldMatrix<Fraction>(subTestData);
        Fraction[] mColumn1 = columnToArray(subColumn1);
        Fraction[] mColumn3 = columnToArray(subColumn3);
        checkArrays(mColumn1, m.getColumn(1));
        checkArrays(mColumn3, m.getColumn(3));
        try {
            m.getColumn(-1);
            fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.getColumn(4);
            fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
    }

// org.apache.commons.math.linear.FieldMatrixImplTest::testSetColumn
    public void testSetColumn() {
        FieldMatrix<Fraction> m = new Array2DRowFieldMatrix<Fraction>(subTestData);
        Fraction[] mColumn3 = columnToArray(subColumn3);
        assertTrue(mColumn3[0] != m.getColumn(1)[0]);
        m.setColumn(1, mColumn3);
        checkArrays(mColumn3, m.getColumn(1));
        try {
            m.setColumn(-1, mColumn3);
            fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.setColumn(0, new Fraction[5]);
            fail("Expecting MatrixDimensionMismatchException");
        } catch (MatrixDimensionMismatchException ex) {
            
        }
    }

// org.apache.commons.math.linear.FieldMatrixImplTest::testEqualsAndHashCode
    public void testEqualsAndHashCode() {
        Array2DRowFieldMatrix<Fraction> m = new Array2DRowFieldMatrix<Fraction>(testData);
        Array2DRowFieldMatrix<Fraction> m1 = (Array2DRowFieldMatrix<Fraction>) m.copy();
        Array2DRowFieldMatrix<Fraction> mt = (Array2DRowFieldMatrix<Fraction>) m.transpose();
        assertTrue(m.hashCode() != mt.hashCode());
        assertEquals(m.hashCode(), m1.hashCode());
        assertEquals(m, m);
        assertEquals(m, m1);
        assertFalse(m.equals(null));
        assertFalse(m.equals(mt));
        assertFalse(m.equals(new Array2DRowFieldMatrix<Fraction>(bigSingular)));
    }

// org.apache.commons.math.linear.FieldMatrixImplTest::testToString
    public void testToString() {
        Array2DRowFieldMatrix<Fraction> m = new Array2DRowFieldMatrix<Fraction>(testData);
        assertEquals("Array2DRowFieldMatrix{{1,2,3},{2,5,3},{1,0,8}}", m.toString());
        m = new Array2DRowFieldMatrix<Fraction>(FractionField.getInstance());
        assertEquals("Array2DRowFieldMatrix{}", m.toString());
    }

// org.apache.commons.math.linear.FieldMatrixImplTest::testSetSubMatrix
    public void testSetSubMatrix() throws Exception {
        Array2DRowFieldMatrix<Fraction> m = new Array2DRowFieldMatrix<Fraction>(testData);
        m.setSubMatrix(detData2,1,1);
        FieldMatrix<Fraction> expected = new Array2DRowFieldMatrix<Fraction>
            (new Fraction[][] {
                    {new Fraction(1),new Fraction(2),new Fraction(3)},
                    {new Fraction(2),new Fraction(1),new Fraction(3)},
                    {new Fraction(1),new Fraction(2),new Fraction(4)}
             });
        assertEquals(expected, m);

        m.setSubMatrix(detData2,0,0);
        expected = new Array2DRowFieldMatrix<Fraction>
            (new Fraction[][] {
                    {new Fraction(1),new Fraction(3),new Fraction(3)},
                    {new Fraction(2),new Fraction(4),new Fraction(3)},
                    {new Fraction(1),new Fraction(2),new Fraction(4)}
             });
        assertEquals(expected, m);

        m.setSubMatrix(testDataPlus2,0,0);
        expected = new Array2DRowFieldMatrix<Fraction>
            (new Fraction[][] {
                    {new Fraction(3),new Fraction(4),new Fraction(5)},
                    {new Fraction(4),new Fraction(7),new Fraction(5)},
                    {new Fraction(3),new Fraction(2),new Fraction(10)}
             });
        assertEquals(expected, m);

        
        try {
            m.setSubMatrix(testData,1,1);
            fail("expecting OutOfRangeException");
        } catch (OutOfRangeException e) {
            
        }
        
        try {
            m.setSubMatrix(testData,-1,1);
            fail("expecting OutOfRangeException");
        } catch (OutOfRangeException e) {
            
        }
        try {
            m.setSubMatrix(testData,1,-1);
            fail("expecting OutOfRangeException");
        } catch (OutOfRangeException e) {
            
        }

        
        try {
            m.setSubMatrix(null, 1, 1);
            fail("expecting NullArgumentException");
        } catch (NullArgumentException e) {
            
        }
        Array2DRowFieldMatrix<Fraction> m2 = new Array2DRowFieldMatrix<Fraction>(FractionField.getInstance());
        try {
            m2.setSubMatrix(testData,0,1);
            fail("expecting MathIllegalStateException");
        } catch (MathIllegalStateException e) {
            
        }
        try {
            m2.setSubMatrix(testData,1,0);
            fail("expecting MathIllegalStateException");
        } catch (MathIllegalStateException e) {
            
        }

        
        try {
            m.setSubMatrix(new Fraction[][] {{new Fraction(1)}, {new Fraction(2), new Fraction(3)}}, 0, 0);
            fail("expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException e) {
            
        }

        
        try {
            m.setSubMatrix(new Fraction[][] {{}}, 0, 0);
            fail("expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException e) {
            
        }

    }

// org.apache.commons.math.linear.FieldMatrixImplTest::testWalk
    public void testWalk() throws MathUserException {
        int rows    = 150;
        int columns = 75;

        FieldMatrix<Fraction> m =
            new Array2DRowFieldMatrix<Fraction>(FractionField.getInstance(), rows, columns);
        m.walkInRowOrder(new SetVisitor());
        GetVisitor getVisitor = new GetVisitor();
        m.walkInOptimizedOrder(getVisitor);
        assertEquals(rows * columns, getVisitor.getCount());

        m = new Array2DRowFieldMatrix<Fraction>(FractionField.getInstance(), rows, columns);
        m.walkInRowOrder(new SetVisitor(), 1, rows - 2, 1, columns - 2);
        getVisitor = new GetVisitor();
        m.walkInOptimizedOrder(getVisitor, 1, rows - 2, 1, columns - 2);
        assertEquals((rows - 2) * (columns - 2), getVisitor.getCount());
        for (int i = 0; i < rows; ++i) {
            assertEquals(new Fraction(0), m.getEntry(i, 0));
            assertEquals(new Fraction(0), m.getEntry(i, columns - 1));
        }
        for (int j = 0; j < columns; ++j) {
            assertEquals(new Fraction(0), m.getEntry(0, j));
            assertEquals(new Fraction(0), m.getEntry(rows - 1, j));
        }

        m = new Array2DRowFieldMatrix<Fraction>(FractionField.getInstance(), rows, columns);
        m.walkInColumnOrder(new SetVisitor());
        getVisitor = new GetVisitor();
        m.walkInOptimizedOrder(getVisitor);
        assertEquals(rows * columns, getVisitor.getCount());

        m = new Array2DRowFieldMatrix<Fraction>(FractionField.getInstance(), rows, columns);
        m.walkInColumnOrder(new SetVisitor(), 1, rows - 2, 1, columns - 2);
        getVisitor = new GetVisitor();
        m.walkInOptimizedOrder(getVisitor, 1, rows - 2, 1, columns - 2);
        assertEquals((rows - 2) * (columns - 2), getVisitor.getCount());
        for (int i = 0; i < rows; ++i) {
            assertEquals(new Fraction(0), m.getEntry(i, 0));
            assertEquals(new Fraction(0), m.getEntry(i, columns - 1));
        }
        for (int j = 0; j < columns; ++j) {
            assertEquals(new Fraction(0), m.getEntry(0, j));
            assertEquals(new Fraction(0), m.getEntry(rows - 1, j));
        }

        m = new Array2DRowFieldMatrix<Fraction>(FractionField.getInstance(), rows, columns);
        m.walkInOptimizedOrder(new SetVisitor());
        getVisitor = new GetVisitor();
        m.walkInRowOrder(getVisitor);
        assertEquals(rows * columns, getVisitor.getCount());

        m = new Array2DRowFieldMatrix<Fraction>(FractionField.getInstance(), rows, columns);
        m.walkInOptimizedOrder(new SetVisitor(), 1, rows - 2, 1, columns - 2);
        getVisitor = new GetVisitor();
        m.walkInRowOrder(getVisitor, 1, rows - 2, 1, columns - 2);
        assertEquals((rows - 2) * (columns - 2), getVisitor.getCount());
        for (int i = 0; i < rows; ++i) {
            assertEquals(new Fraction(0), m.getEntry(i, 0));
            assertEquals(new Fraction(0), m.getEntry(i, columns - 1));
        }
        for (int j = 0; j < columns; ++j) {
            assertEquals(new Fraction(0), m.getEntry(0, j));
            assertEquals(new Fraction(0), m.getEntry(rows - 1, j));
        }

        m = new Array2DRowFieldMatrix<Fraction>(FractionField.getInstance(), rows, columns);
        m.walkInOptimizedOrder(new SetVisitor());
        getVisitor = new GetVisitor();
        m.walkInColumnOrder(getVisitor);
        assertEquals(rows * columns, getVisitor.getCount());

        m = new Array2DRowFieldMatrix<Fraction>(FractionField.getInstance(), rows, columns);
        m.walkInOptimizedOrder(new SetVisitor(), 1, rows - 2, 1, columns - 2);
        getVisitor = new GetVisitor();
        m.walkInColumnOrder(getVisitor, 1, rows - 2, 1, columns - 2);
        assertEquals((rows - 2) * (columns - 2), getVisitor.getCount());
        for (int i = 0; i < rows; ++i) {
            assertEquals(new Fraction(0), m.getEntry(i, 0));
            assertEquals(new Fraction(0), m.getEntry(i, columns - 1));
        }
        for (int j = 0; j < columns; ++j) {
            assertEquals(new Fraction(0), m.getEntry(0, j));
            assertEquals(new Fraction(0), m.getEntry(rows - 1, j));
        }

    }

// org.apache.commons.math.linear.FieldMatrixImplTest::testSerial
    public void testSerial()  {
        Array2DRowFieldMatrix<Fraction> m = new Array2DRowFieldMatrix<Fraction>(testData);
        assertEquals(m,TestUtils.serializeAndRecover(m));
    }

// org.apache.commons.math.linear.LUDecompositionImplTest::testDimensions
    public void testDimensions() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(testData);
        LUDecomposition LU = new LUDecompositionImpl(matrix);
        assertEquals(testData.length, LU.getL().getRowDimension());
        assertEquals(testData.length, LU.getL().getColumnDimension());
        assertEquals(testData.length, LU.getU().getRowDimension());
        assertEquals(testData.length, LU.getU().getColumnDimension());
        assertEquals(testData.length, LU.getP().getRowDimension());
        assertEquals(testData.length, LU.getP().getColumnDimension());

    }

// org.apache.commons.math.linear.LUDecompositionImplTest::testNonSquare
    public void testNonSquare() {
        try {
            new LUDecompositionImpl(MatrixUtils.createRealMatrix(new double[3][2]));
            fail("Expecting NonSquareMatrixException");
        } catch (NonSquareMatrixException ime) {
            
        }
    }

// org.apache.commons.math.linear.LUDecompositionImplTest::testPAEqualLU
    public void testPAEqualLU() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(testData);
        LUDecomposition lu = new LUDecompositionImpl(matrix);
        RealMatrix l = lu.getL();
        RealMatrix u = lu.getU();
        RealMatrix p = lu.getP();
        double norm = l.multiply(u).subtract(p.multiply(matrix)).getNorm();
        assertEquals(0, norm, normTolerance);

        matrix = MatrixUtils.createRealMatrix(testDataMinus);
        lu = new LUDecompositionImpl(matrix);
        l = lu.getL();
        u = lu.getU();
        p = lu.getP();
        norm = l.multiply(u).subtract(p.multiply(matrix)).getNorm();
        assertEquals(0, norm, normTolerance);

        matrix = MatrixUtils.createRealIdentityMatrix(17);
        lu = new LUDecompositionImpl(matrix);
        l = lu.getL();
        u = lu.getU();
        p = lu.getP();
        norm = l.multiply(u).subtract(p.multiply(matrix)).getNorm();
        assertEquals(0, norm, normTolerance);

        matrix = MatrixUtils.createRealMatrix(singular);
        lu = new LUDecompositionImpl(matrix);
        assertFalse(lu.getSolver().isNonSingular());
        assertNull(lu.getL());
        assertNull(lu.getU());
        assertNull(lu.getP());

        matrix = MatrixUtils.createRealMatrix(bigSingular);
        lu = new LUDecompositionImpl(matrix);
        assertFalse(lu.getSolver().isNonSingular());
        assertNull(lu.getL());
        assertNull(lu.getU());
        assertNull(lu.getP());

    }

// org.apache.commons.math.linear.LUDecompositionImplTest::testLLowerTriangular
    public void testLLowerTriangular() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(testData);
        RealMatrix l = new LUDecompositionImpl(matrix).getL();
        for (int i = 0; i < l.getRowDimension(); i++) {
            assertEquals(l.getEntry(i, i), 1, entryTolerance);
            for (int j = i + 1; j < l.getColumnDimension(); j++) {
                assertEquals(l.getEntry(i, j), 0, entryTolerance);
            }
        }
    }

// org.apache.commons.math.linear.LUDecompositionImplTest::testUUpperTriangular
    public void testUUpperTriangular() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(testData);
        RealMatrix u = new LUDecompositionImpl(matrix).getU();
        for (int i = 0; i < u.getRowDimension(); i++) {
            for (int j = 0; j < i; j++) {
                assertEquals(u.getEntry(i, j), 0, entryTolerance);
            }
        }
    }

// org.apache.commons.math.linear.LUDecompositionImplTest::testPPermutation
    public void testPPermutation() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(testData);
        RealMatrix p   = new LUDecompositionImpl(matrix).getP();

        RealMatrix ppT = p.multiply(p.transpose());
        RealMatrix id  = MatrixUtils.createRealIdentityMatrix(p.getRowDimension());
        assertEquals(0, ppT.subtract(id).getNorm(), normTolerance);

        for (int i = 0; i < p.getRowDimension(); i++) {
            int zeroCount  = 0;
            int oneCount   = 0;
            int otherCount = 0;
            for (int j = 0; j < p.getColumnDimension(); j++) {
                final double e = p.getEntry(i, j);
                if (e == 0) {
                    ++zeroCount;
                } else if (e == 1) {
                    ++oneCount;
                } else {
                    ++otherCount;
                }
            }
            assertEquals(p.getColumnDimension() - 1, zeroCount);
            assertEquals(1, oneCount);
            assertEquals(0, otherCount);
        }

        for (int j = 0; j < p.getColumnDimension(); j++) {
            int zeroCount  = 0;
            int oneCount   = 0;
            int otherCount = 0;
            for (int i = 0; i < p.getRowDimension(); i++) {
                final double e = p.getEntry(i, j);
                if (e == 0) {
                    ++zeroCount;
                } else if (e == 1) {
                    ++oneCount;
                } else {
                    ++otherCount;
                }
            }
            assertEquals(p.getRowDimension() - 1, zeroCount);
            assertEquals(1, oneCount);
            assertEquals(0, otherCount);
        }

    }

// org.apache.commons.math.linear.LUDecompositionImplTest::testSingular
    public void testSingular() {
        LUDecomposition lu =
            new LUDecompositionImpl(MatrixUtils.createRealMatrix(testData));
        assertTrue(lu.getSolver().isNonSingular());
        lu = new LUDecompositionImpl(MatrixUtils.createRealMatrix(singular));
        assertFalse(lu.getSolver().isNonSingular());
        lu = new LUDecompositionImpl(MatrixUtils.createRealMatrix(bigSingular));
        assertFalse(lu.getSolver().isNonSingular());
    }

// org.apache.commons.math.linear.LUDecompositionImplTest::testMatricesValues1
    public void testMatricesValues1() {
       LUDecomposition lu =
            new LUDecompositionImpl(MatrixUtils.createRealMatrix(testData));
        RealMatrix lRef = MatrixUtils.createRealMatrix(new double[][] {
                { 1.0, 0.0, 0.0 },
                { 0.5, 1.0, 0.0 },
                { 0.5, 0.2, 1.0 }
        });
        RealMatrix uRef = MatrixUtils.createRealMatrix(new double[][] {
                { 2.0,  5.0, 3.0 },
                { 0.0, -2.5, 6.5 },
                { 0.0,  0.0, 0.2 }
        });
        RealMatrix pRef = MatrixUtils.createRealMatrix(new double[][] {
                { 0.0, 1.0, 0.0 },
                { 0.0, 0.0, 1.0 },
                { 1.0, 0.0, 0.0 }
        });
        int[] pivotRef = { 1, 2, 0 };

        
        RealMatrix l = lu.getL();
        assertEquals(0, l.subtract(lRef).getNorm(), 1.0e-13);
        RealMatrix u = lu.getU();
        assertEquals(0, u.subtract(uRef).getNorm(), 1.0e-13);
        RealMatrix p = lu.getP();
        assertEquals(0, p.subtract(pRef).getNorm(), 1.0e-13);
        int[] pivot = lu.getPivot();
        for (int i = 0; i < pivotRef.length; ++i) {
            assertEquals(pivotRef[i], pivot[i]);
        }

        
        assertTrue(l == lu.getL());
        assertTrue(u == lu.getU());
        assertTrue(p == lu.getP());

    }

// org.apache.commons.math.linear.LUDecompositionImplTest::testMatricesValues2
    public void testMatricesValues2() {
       LUDecomposition lu =
            new LUDecompositionImpl(MatrixUtils.createRealMatrix(luData));
        RealMatrix lRef = MatrixUtils.createRealMatrix(new double[][] {
                {    1.0,    0.0, 0.0 },
                {    0.0,    1.0, 0.0 },
                { 1.0 / 3.0, 0.0, 1.0 }
        });
        RealMatrix uRef = MatrixUtils.createRealMatrix(new double[][] {
                { 6.0, 9.0,    8.0    },
                { 0.0, 5.0,    7.0    },
                { 0.0, 0.0, 1.0 / 3.0 }
        });
        RealMatrix pRef = MatrixUtils.createRealMatrix(new double[][] {
                { 0.0, 0.0, 1.0 },
                { 0.0, 1.0, 0.0 },
                { 1.0, 0.0, 0.0 }
        });
        int[] pivotRef = { 2, 1, 0 };

        
        RealMatrix l = lu.getL();
        assertEquals(0, l.subtract(lRef).getNorm(), 1.0e-13);
        RealMatrix u = lu.getU();
        assertEquals(0, u.subtract(uRef).getNorm(), 1.0e-13);
        RealMatrix p = lu.getP();
        assertEquals(0, p.subtract(pRef).getNorm(), 1.0e-13);
        int[] pivot = lu.getPivot();
        for (int i = 0; i < pivotRef.length; ++i) {
            assertEquals(pivotRef[i], pivot[i]);
        }

        
        assertTrue(l == lu.getL());
        assertTrue(u == lu.getU());
        assertTrue(p == lu.getP());

    }

// org.apache.commons.math.linear.LUSolverTest::testThreshold
    public void testThreshold() {
        final RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
                                                       { 1.0, 2.0, 3.0},
                                                       { 2.0, 5.0, 3.0},
                                                       { 4.000001, 9.0, 9.0}
                                                     });
        assertFalse(new LUDecompositionImpl(matrix, 1.0e-5).getSolver().isNonSingular());
        assertTrue(new LUDecompositionImpl(matrix, 1.0e-10).getSolver().isNonSingular());
    }

// org.apache.commons.math.linear.LUSolverTest::testSingular
    public void testSingular() {
        DecompositionSolver solver =
            new LUDecompositionImpl(MatrixUtils.createRealMatrix(testData)).getSolver();
        assertTrue(solver.isNonSingular());
        solver = new LUDecompositionImpl(MatrixUtils.createRealMatrix(singular)).getSolver();
        assertFalse(solver.isNonSingular());
        solver = new LUDecompositionImpl(MatrixUtils.createRealMatrix(bigSingular)).getSolver();
        assertFalse(solver.isNonSingular());
    }

// org.apache.commons.math.linear.LUSolverTest::testSolveDimensionErrors
    public void testSolveDimensionErrors() {
        DecompositionSolver solver =
            new LUDecompositionImpl(MatrixUtils.createRealMatrix(testData)).getSolver();
        RealMatrix b = MatrixUtils.createRealMatrix(new double[2][2]);
        try {
            solver.solve(b);
            fail("an exception should have been thrown");
        } catch (MathIllegalArgumentException iae) {
            
        }
        try {
            solver.solve(b.getColumn(0));
            fail("an exception should have been thrown");
        } catch (MathIllegalArgumentException iae) {
            
        }
        try {
            solver.solve(new ArrayRealVectorTest.RealVectorTestImpl(b.getColumn(0)));
            fail("an exception should have been thrown");
        } catch (MathIllegalArgumentException iae) {
            
        }
    }

// org.apache.commons.math.linear.LUSolverTest::testSolveSingularityErrors
    public void testSolveSingularityErrors() {
        DecompositionSolver solver =
            new LUDecompositionImpl(MatrixUtils.createRealMatrix(singular)).getSolver();
        RealMatrix b = MatrixUtils.createRealMatrix(new double[2][2]);
        try {
            solver.solve(b);
            fail("an exception should have been thrown");
        } catch (SingularMatrixException ime) {
            
        }
        try {
            solver.solve(b.getColumn(0));
            fail("an exception should have been thrown");
        } catch (SingularMatrixException ime) {
            
        }
        try {
            solver.solve(b.getColumnVector(0));
            fail("an exception should have been thrown");
        } catch (SingularMatrixException ime) {
            
        }
        try {
            solver.solve(new ArrayRealVectorTest.RealVectorTestImpl(b.getColumn(0)));
            fail("an exception should have been thrown");
        } catch (SingularMatrixException ime) {
            
        }
    }

// org.apache.commons.math.linear.LUSolverTest::testSolve
    public void testSolve() {
        DecompositionSolver solver =
            new LUDecompositionImpl(MatrixUtils.createRealMatrix(testData)).getSolver();
        RealMatrix b = MatrixUtils.createRealMatrix(new double[][] {
                { 1, 0 }, { 2, -5 }, { 3, 1 }
        });
        RealMatrix xRef = MatrixUtils.createRealMatrix(new double[][] {
                { 19, -71 }, { -6, 22 }, { -2, 9 }
        });

        
        assertEquals(0, solver.solve(b).subtract(xRef).getNorm(), 1.0e-13);

        
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

// org.apache.commons.math.linear.LUSolverTest::testDeterminant
    public void testDeterminant() {
        assertEquals( -1, getDeterminant(MatrixUtils.createRealMatrix(testData)), 1.0e-15);
        assertEquals(-10, getDeterminant(MatrixUtils.createRealMatrix(luData)), 1.0e-14);
        assertEquals(  0, getDeterminant(MatrixUtils.createRealMatrix(singular)), 1.0e-17);
        assertEquals(  0, getDeterminant(MatrixUtils.createRealMatrix(bigSingular)), 1.0e-10);
    }

// org.apache.commons.math.linear.MatrixUtilsTest::testCreateRealMatrix
    public void testCreateRealMatrix() {
        assertEquals(new BlockRealMatrix(testData),
                MatrixUtils.createRealMatrix(testData));
        try {
            MatrixUtils.createRealMatrix(new double[][] {{1}, {1,2}});  
            fail("Expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
        try {
            MatrixUtils.createRealMatrix(new double[][] {{}, {}});  
            fail("Expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
        try {
            MatrixUtils.createRealMatrix(null);  
            fail("Expecting NullArgumentException");
        } catch (NullArgumentException ex) {
            
        }
    }

// org.apache.commons.math.linear.MatrixUtilsTest::testcreateFieldMatrix
    public void testcreateFieldMatrix() {
        assertEquals(new Array2DRowFieldMatrix<Fraction>(asFraction(testData)),
                     MatrixUtils.createFieldMatrix(asFraction(testData)));
        assertEquals(new Array2DRowFieldMatrix<Fraction>(fractionColMatrix),
                     MatrixUtils.createFieldMatrix(fractionColMatrix));
        try {
            MatrixUtils.createFieldMatrix(asFraction(new double[][] {{1}, {1,2}}));  
            fail("Expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
        try {
            MatrixUtils.createFieldMatrix(asFraction(new double[][] {{}, {}}));  
            fail("Expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
        try {
            MatrixUtils.createFieldMatrix((Fraction[][])null);  
            fail("Expecting NullArgumentException");
        } catch (NullArgumentException ex) {
            
        }
    }

// org.apache.commons.math.linear.MatrixUtilsTest::testCreateRowRealMatrix
    public void testCreateRowRealMatrix() {
        assertEquals(MatrixUtils.createRowRealMatrix(row),
                     new BlockRealMatrix(rowMatrix));
        try {
            MatrixUtils.createRowRealMatrix(new double[] {});  
            fail("Expecting NotStrictlyPositiveException");
        } catch (NotStrictlyPositiveException ex) {
            
        }
        try {
            MatrixUtils.createRowRealMatrix(null);  
            fail("Expecting NullArgumentException");
        } catch (NullArgumentException ex) {
            
        }
    }

// org.apache.commons.math.linear.MatrixUtilsTest::testCreateRowFieldMatrix
    public void testCreateRowFieldMatrix() {
        assertEquals(MatrixUtils.createRowFieldMatrix(asFraction(row)),
                     new Array2DRowFieldMatrix<Fraction>(asFraction(rowMatrix)));
        assertEquals(MatrixUtils.createRowFieldMatrix(fractionRow),
                     new Array2DRowFieldMatrix<Fraction>(fractionRowMatrix));
        try {
            MatrixUtils.createRowFieldMatrix(new Fraction[] {});  
            fail("Expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
        try {
            MatrixUtils.createRowFieldMatrix((Fraction[]) null);  
            fail("Expecting NullArgumentException");
        } catch (NullArgumentException ex) {
            
        }
    }

// org.apache.commons.math.linear.MatrixUtilsTest::testCreateColumnRealMatrix
    public void testCreateColumnRealMatrix() {
        assertEquals(MatrixUtils.createColumnRealMatrix(col),
                     new BlockRealMatrix(colMatrix));
        try {
            MatrixUtils.createColumnRealMatrix(new double[] {});  
            fail("Expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
        try {
            MatrixUtils.createColumnRealMatrix(null);  
            fail("Expecting NullArgumentException");
        } catch (NullArgumentException ex) {
            
        }
    }

// org.apache.commons.math.linear.MatrixUtilsTest::testCreateColumnFieldMatrix
    public void testCreateColumnFieldMatrix() {
        assertEquals(MatrixUtils.createColumnFieldMatrix(asFraction(col)),
                     new Array2DRowFieldMatrix<Fraction>(asFraction(colMatrix)));
        assertEquals(MatrixUtils.createColumnFieldMatrix(fractionCol),
                     new Array2DRowFieldMatrix<Fraction>(fractionColMatrix));

        try {
            MatrixUtils.createColumnFieldMatrix(new Fraction[] {});  
            fail("Expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
        try {
            MatrixUtils.createColumnFieldMatrix((Fraction[]) null);  
            fail("Expecting NullArgumentException");
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
            fail("Expecting MathIllegalArgumentException");
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
            fail("Expecting MathIllegalArgumentException");
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
        assertEquals(0.0, converted.subtract(reference).getNorm(), 0.0);
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
        assertEquals(0.0, converted.subtract(reference).getNorm(), 0.0);
    }

// org.apache.commons.math.linear.QRDecompositionImplTest::testDimensions
    public void testDimensions() throws MathUserException {
        checkDimension(MatrixUtils.createRealMatrix(testData3x3NonSingular));

        checkDimension(MatrixUtils.createRealMatrix(testData4x3));

        checkDimension(MatrixUtils.createRealMatrix(testData3x4));

        Random r = new Random(643895747384642l);
        int    p = (5 * BlockRealMatrix.BLOCK_SIZE) / 4;
        int    q = (7 * BlockRealMatrix.BLOCK_SIZE) / 4;
        checkDimension(createTestMatrix(r, p, q));
        checkDimension(createTestMatrix(r, q, p));

    }

// org.apache.commons.math.linear.QRDecompositionImplTest::testAEqualQR
    public void testAEqualQR() throws MathUserException {
        checkAEqualQR(MatrixUtils.createRealMatrix(testData3x3NonSingular));

        checkAEqualQR(MatrixUtils.createRealMatrix(testData3x3Singular));

        checkAEqualQR(MatrixUtils.createRealMatrix(testData3x4));

        checkAEqualQR(MatrixUtils.createRealMatrix(testData4x3));

        Random r = new Random(643895747384642l);
        int    p = (5 * BlockRealMatrix.BLOCK_SIZE) / 4;
        int    q = (7 * BlockRealMatrix.BLOCK_SIZE) / 4;
        checkAEqualQR(createTestMatrix(r, p, q));

        checkAEqualQR(createTestMatrix(r, q, p));

    }

// org.apache.commons.math.linear.QRDecompositionImplTest::testQOrthogonal
    public void testQOrthogonal() throws MathUserException {
        checkQOrthogonal(MatrixUtils.createRealMatrix(testData3x3NonSingular));

        checkQOrthogonal(MatrixUtils.createRealMatrix(testData3x3Singular));

        checkQOrthogonal(MatrixUtils.createRealMatrix(testData3x4));

        checkQOrthogonal(MatrixUtils.createRealMatrix(testData4x3));

        Random r = new Random(643895747384642l);
        int    p = (5 * BlockRealMatrix.BLOCK_SIZE) / 4;
        int    q = (7 * BlockRealMatrix.BLOCK_SIZE) / 4;
        checkQOrthogonal(createTestMatrix(r, p, q));

        checkQOrthogonal(createTestMatrix(r, q, p));

    }

// org.apache.commons.math.linear.QRDecompositionImplTest::testRUpperTriangular
    public void testRUpperTriangular() throws MathUserException {
        RealMatrix matrix = MatrixUtils.createRealMatrix(testData3x3NonSingular);
        checkUpperTriangular(new QRDecompositionImpl(matrix).getR());

        matrix = MatrixUtils.createRealMatrix(testData3x3Singular);
        checkUpperTriangular(new QRDecompositionImpl(matrix).getR());

        matrix = MatrixUtils.createRealMatrix(testData3x4);
        checkUpperTriangular(new QRDecompositionImpl(matrix).getR());

        matrix = MatrixUtils.createRealMatrix(testData4x3);
        checkUpperTriangular(new QRDecompositionImpl(matrix).getR());

        Random r = new Random(643895747384642l);
        int    p = (5 * BlockRealMatrix.BLOCK_SIZE) / 4;
        int    q = (7 * BlockRealMatrix.BLOCK_SIZE) / 4;
        matrix = createTestMatrix(r, p, q);
        checkUpperTriangular(new QRDecompositionImpl(matrix).getR());

        matrix = createTestMatrix(r, p, q);
        checkUpperTriangular(new QRDecompositionImpl(matrix).getR());

    }

// org.apache.commons.math.linear.QRDecompositionImplTest::testHTrapezoidal
    public void testHTrapezoidal() throws MathUserException {
        RealMatrix matrix = MatrixUtils.createRealMatrix(testData3x3NonSingular);
        checkTrapezoidal(new QRDecompositionImpl(matrix).getH());

        matrix = MatrixUtils.createRealMatrix(testData3x3Singular);
        checkTrapezoidal(new QRDecompositionImpl(matrix).getH());

        matrix = MatrixUtils.createRealMatrix(testData3x4);
        checkTrapezoidal(new QRDecompositionImpl(matrix).getH());

        matrix = MatrixUtils.createRealMatrix(testData4x3);
        checkTrapezoidal(new QRDecompositionImpl(matrix).getH());

        Random r = new Random(643895747384642l);
        int    p = (5 * BlockRealMatrix.BLOCK_SIZE) / 4;
        int    q = (7 * BlockRealMatrix.BLOCK_SIZE) / 4;
        matrix = createTestMatrix(r, p, q);
        checkTrapezoidal(new QRDecompositionImpl(matrix).getH());

        matrix = createTestMatrix(r, p, q);
        checkTrapezoidal(new QRDecompositionImpl(matrix).getH());

    }

// org.apache.commons.math.linear.QRDecompositionImplTest::testMatricesValues
    public void testMatricesValues() {
        QRDecomposition qr =
            new QRDecompositionImpl(MatrixUtils.createRealMatrix(testData3x3NonSingular));
        RealMatrix qRef = MatrixUtils.createRealMatrix(new double[][] {
                { -12.0 / 14.0,   69.0 / 175.0,  -58.0 / 175.0 },
                {  -6.0 / 14.0, -158.0 / 175.0,    6.0 / 175.0 },
                {   4.0 / 14.0,  -30.0 / 175.0, -165.0 / 175.0 }
        });
        RealMatrix rRef = MatrixUtils.createRealMatrix(new double[][] {
                { -14.0,  -21.0, 14.0 },
                {   0.0, -175.0, 70.0 },
                {   0.0,    0.0, 35.0 }
        });
        RealMatrix hRef = MatrixUtils.createRealMatrix(new double[][] {
                { 26.0 / 14.0, 0.0, 0.0 },
                {  6.0 / 14.0, 648.0 / 325.0, 0.0 },
                { -4.0 / 14.0,  36.0 / 325.0, 2.0 }
        });

        
        RealMatrix q = qr.getQ();
        assertEquals(0, q.subtract(qRef).getNorm(), 1.0e-13);
        RealMatrix qT = qr.getQT();
        assertEquals(0, qT.subtract(qRef.transpose()).getNorm(), 1.0e-13);
        RealMatrix r = qr.getR();
        assertEquals(0, r.subtract(rRef).getNorm(), 1.0e-13);
        RealMatrix h = qr.getH();
        assertEquals(0, h.subtract(hRef).getNorm(), 1.0e-13);

        
        assertTrue(q == qr.getQ());
        assertTrue(r == qr.getR());
        assertTrue(h == qr.getH());

    }

// org.apache.commons.math.linear.QRSolverTest::testRank
    public void testRank() {
        DecompositionSolver solver =
            new QRDecompositionImpl(MatrixUtils.createRealMatrix(testData3x3NonSingular)).getSolver();
        assertTrue(solver.isNonSingular());

        solver = new QRDecompositionImpl(MatrixUtils.createRealMatrix(testData3x3Singular)).getSolver();
        assertFalse(solver.isNonSingular());

        solver = new QRDecompositionImpl(MatrixUtils.createRealMatrix(testData3x4)).getSolver();
        assertTrue(solver.isNonSingular());

        solver = new QRDecompositionImpl(MatrixUtils.createRealMatrix(testData4x3)).getSolver();
        assertTrue(solver.isNonSingular());

    }

// org.apache.commons.math.linear.QRSolverTest::testSolveDimensionErrors
    public void testSolveDimensionErrors() {
        DecompositionSolver solver =
            new QRDecompositionImpl(MatrixUtils.createRealMatrix(testData3x3NonSingular)).getSolver();
        RealMatrix b = MatrixUtils.createRealMatrix(new double[2][2]);
        try {
            solver.solve(b);
            fail("an exception should have been thrown");
        } catch (MathIllegalArgumentException iae) {
            
        }
        try {
            solver.solve(b.getColumn(0));
            fail("an exception should have been thrown");
        } catch (MathIllegalArgumentException iae) {
            
        }
        try {
            solver.solve(b.getColumnVector(0));
            fail("an exception should have been thrown");
        } catch (MathIllegalArgumentException iae) {
            
        }
    }

// org.apache.commons.math.linear.QRSolverTest::testSolveRankErrors
    public void testSolveRankErrors() {
        DecompositionSolver solver =
            new QRDecompositionImpl(MatrixUtils.createRealMatrix(testData3x3Singular)).getSolver();
        RealMatrix b = MatrixUtils.createRealMatrix(new double[3][2]);
        try {
            solver.solve(b);
            fail("an exception should have been thrown");
        } catch (SingularMatrixException iae) {
            
        }
        try {
            solver.solve(b.getColumn(0));
            fail("an exception should have been thrown");
        } catch (SingularMatrixException iae) {
            
        }
        try {
            solver.solve(b.getColumnVector(0));
            fail("an exception should have been thrown");
        } catch (SingularMatrixException iae) {
            
        }
    }

// org.apache.commons.math.linear.QRSolverTest::testSolve
    public void testSolve() {
        QRDecomposition decomposition =
            new QRDecompositionImpl(MatrixUtils.createRealMatrix(testData3x3NonSingular));
        DecompositionSolver solver = decomposition.getSolver();
        RealMatrix b = MatrixUtils.createRealMatrix(new double[][] {
                { -102, 12250 }, { 544, 24500 }, { 167, -36750 }
        });
        RealMatrix xRef = MatrixUtils.createRealMatrix(new double[][] {
                { 1, 2515 }, { 2, 422 }, { -3, 898 }
        });

        
        assertEquals(0, solver.solve(b).subtract(xRef).getNorm(), 2.0e-16 * xRef.getNorm());

        
        for (int i = 0; i < b.getColumnDimension(); ++i) {
            final double[] x = solver.solve(b.getColumn(i));
            final double error = new ArrayRealVector(x).subtract(xRef.getColumnVector(i)).getNorm();
            assertEquals(0, error, 3.0e-16 * xRef.getColumnVector(i).getNorm());
        }

        
        for (int i = 0; i < b.getColumnDimension(); ++i) {
            final RealVector x = solver.solve(b.getColumnVector(i));
            final double error = x.subtract(xRef.getColumnVector(i)).getNorm();
            assertEquals(0, error, 3.0e-16 * xRef.getColumnVector(i).getNorm());
        }

        
        for (int i = 0; i < b.getColumnDimension(); ++i) {
            ArrayRealVectorTest.RealVectorTestImpl v =
                new ArrayRealVectorTest.RealVectorTestImpl(b.getColumn(i));
            final RealVector x = solver.solve(v);
            final double error = x.subtract(xRef.getColumnVector(i)).getNorm();
            assertEquals(0, error, 3.0e-16 * xRef.getColumnVector(i).getNorm());
        }

    }

// org.apache.commons.math.linear.QRSolverTest::testOverdetermined
    public void testOverdetermined() throws MathUserException {
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

        
        RealMatrix x = new QRDecompositionImpl(a).getSolver().solve(b);
        assertEquals(0, x.subtract(xRef).getNorm(), 0.01 * noise * p * q);

    }

// org.apache.commons.math.linear.QRSolverTest::testUnderdetermined
    public void testUnderdetermined() throws MathUserException {
        final Random r    = new Random(42185006424567123l);
        int          p    = (5 * BlockRealMatrix.BLOCK_SIZE) / 4;
        int          q    = (7 * BlockRealMatrix.BLOCK_SIZE) / 4;
        RealMatrix   a    = createTestMatrix(r, p, q);
        RealMatrix   xRef = createTestMatrix(r, q, BlockRealMatrix.BLOCK_SIZE + 3);
        RealMatrix   b    = a.multiply(xRef);
        RealMatrix   x = new QRDecompositionImpl(a).getSolver().solve(b);

        
        assertTrue(x.subtract(xRef).getNorm() / (p * q) > 0.01);

        
        assertEquals(0.0, x.getSubMatrix(p, q - 1, 0, x.getColumnDimension() - 1).getNorm());

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

// org.apache.commons.math.linear.SingularValueDecompositionImplTest::testConditionNumber
    public void testConditionNumber() {
        SingularValueDecompositionImpl svd =
            new SingularValueDecompositionImpl(MatrixUtils.createRealMatrix(testSquare));
        
        assertEquals(3.0, svd.getConditionNumber(), 1.5e-15);
    }

// org.apache.commons.math.linear.SingularValueSolverTest::testSolveDimensionErrors
    public void testSolveDimensionErrors() {
        DecompositionSolver solver =
            new SingularValueDecompositionImpl(MatrixUtils.createRealMatrix(testSquare)).getSolver();
        RealMatrix b = MatrixUtils.createRealMatrix(new double[3][2]);
        try {
            solver.solve(b);
            Assert.fail("an exception should have been thrown");
        } catch (MathIllegalArgumentException iae) {
            
        }
        try {
            solver.solve(b.getColumn(0));
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
        
        Assert.assertEquals(3.0, svd.getConditionNumber(), 1.5e-15);
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

// org.apache.commons.math.linear.SparseFieldMatrixTest::testDimensions
    public void testDimensions() {
        SparseFieldMatrix<Fraction> m = createSparseMatrix(testData);
        SparseFieldMatrix<Fraction> m2 = createSparseMatrix(testData2);
        assertEquals("testData row dimension", 3, m.getRowDimension());
        assertEquals("testData column dimension", 3, m.getColumnDimension());
        assertTrue("testData is square", m.isSquare());
        assertEquals("testData2 row dimension", m2.getRowDimension(), 2);
        assertEquals("testData2 column dimension", m2.getColumnDimension(), 3);
        assertTrue("testData2 is not square", !m2.isSquare());
    }

// org.apache.commons.math.linear.SparseFieldMatrixTest::testCopyFunctions
    public void testCopyFunctions() {
        SparseFieldMatrix<Fraction> m1 = createSparseMatrix(testData);
        FieldMatrix<Fraction> m2 = m1.copy();
        assertEquals(m1.getClass(), m2.getClass());
        assertEquals((m2), m1);
        SparseFieldMatrix<Fraction> m3 = createSparseMatrix(testData);
        FieldMatrix<Fraction> m4 = m3.copy();
        assertEquals(m3.getClass(), m4.getClass());
        assertEquals((m4), m3);
    }

// org.apache.commons.math.linear.SparseFieldMatrixTest::testAdd
    public void testAdd() {
        SparseFieldMatrix<Fraction> m = createSparseMatrix(testData);
        SparseFieldMatrix<Fraction> mInv = createSparseMatrix(testDataInv);
        SparseFieldMatrix<Fraction> mDataPlusInv = createSparseMatrix(testDataPlusInv);
        FieldMatrix<Fraction> mPlusMInv = m.add(mInv);
        for (int row = 0; row < m.getRowDimension(); row++) {
            for (int col = 0; col < m.getColumnDimension(); col++) {
                assertEquals("sum entry entry",
                    mDataPlusInv.getEntry(row, col).doubleValue(), mPlusMInv.getEntry(row, col).doubleValue(),
                    entryTolerance);
            }
        }
    }

// org.apache.commons.math.linear.SparseFieldMatrixTest::testAddFail
    public void testAddFail() {
        SparseFieldMatrix<Fraction> m = createSparseMatrix(testData);
        SparseFieldMatrix<Fraction> m2 = createSparseMatrix(testData2);
        try {
            m.add(m2);
            fail("MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.linear.SparseFieldMatrixTest::testPlusMinus
    public void testPlusMinus() {
        SparseFieldMatrix<Fraction> m = createSparseMatrix(testData);
        SparseFieldMatrix<Fraction> n = createSparseMatrix(testDataInv);
        assertClose("m-n = m + -n", m.subtract(n),
            n.scalarMultiply(new Fraction(-1)).add(m), entryTolerance);
        try {
            m.subtract(createSparseMatrix(testData2));
            fail("Expecting illegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.linear.SparseFieldMatrixTest::testMultiply
    public void testMultiply() {
        SparseFieldMatrix<Fraction> m = createSparseMatrix(testData);
        SparseFieldMatrix<Fraction> mInv = createSparseMatrix(testDataInv);
        SparseFieldMatrix<Fraction> identity = createSparseMatrix(id);
        SparseFieldMatrix<Fraction> m2 = createSparseMatrix(testData2);
        assertClose("inverse multiply", m.multiply(mInv), identity,
                entryTolerance);
        assertClose("inverse multiply", m.multiply(new Array2DRowFieldMatrix<Fraction>(testDataInv)), identity,
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
            fail("Expecting illegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.linear.SparseFieldMatrixTest::testMultiply2
    public void testMultiply2() {
        FieldMatrix<Fraction> m3 = createSparseMatrix(d3);
        FieldMatrix<Fraction> m4 = createSparseMatrix(d4);
        FieldMatrix<Fraction> m5 = createSparseMatrix(d5);
        assertClose("m3*m4=m5", m3.multiply(m4), m5, entryTolerance);
    }

// org.apache.commons.math.linear.SparseFieldMatrixTest::testTrace
    public void testTrace() {
        FieldMatrix<Fraction> m = createSparseMatrix(id);
        assertEquals("identity trace", 3d, m.getTrace().doubleValue(), entryTolerance);
        m = createSparseMatrix(testData2);
        try {
            m.getTrace();
            fail("Expecting NonSquareMatrixException");
        } catch (NonSquareMatrixException ex) {
            
        }
    }

// org.apache.commons.math.linear.SparseFieldMatrixTest::testScalarAdd
    public void testScalarAdd() {
        FieldMatrix<Fraction> m = createSparseMatrix(testData);
        assertClose("scalar add", createSparseMatrix(testDataPlus2),
            m.scalarAdd(new Fraction(2)), entryTolerance);
    }

// org.apache.commons.math.linear.SparseFieldMatrixTest::testOperate
    public void testOperate() {
        FieldMatrix<Fraction> m = createSparseMatrix(id);
        assertClose("identity operate", testVector, m.operate(testVector),
                entryTolerance);
        assertClose("identity operate", testVector, m.operate(
                new ArrayFieldVector<Fraction>(testVector)).getData(), entryTolerance);
        m = createSparseMatrix(bigSingular);
        try {
            m.operate(testVector);
            fail("Expecting illegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.linear.SparseFieldMatrixTest::testMath209
    public void testMath209() {
        FieldMatrix<Fraction> a = createSparseMatrix(new Fraction[][] {
                { new Fraction(1), new Fraction(2) }, { new Fraction(3), new Fraction(4) }, { new Fraction(5), new Fraction(6) } });
        Fraction[] b = a.operate(new Fraction[] { new Fraction(1), new Fraction(1) });
        assertEquals(a.getRowDimension(), b.length);
        assertEquals(3.0, b[0].doubleValue(), 1.0e-12);
        assertEquals(7.0, b[1].doubleValue(), 1.0e-12);
        assertEquals(11.0, b[2].doubleValue(), 1.0e-12);
    }

// org.apache.commons.math.linear.SparseFieldMatrixTest::testTranspose
    public void testTranspose() {

        FieldMatrix<Fraction> m = createSparseMatrix(testData);
        FieldMatrix<Fraction> mIT = new FieldLUDecompositionImpl<Fraction>(m).getSolver().getInverse().transpose();
        FieldMatrix<Fraction> mTI = new FieldLUDecompositionImpl<Fraction>(m.transpose()).getSolver().getInverse();
        assertClose("inverse-transpose", mIT, mTI, normTolerance);
        m = createSparseMatrix(testData2);
        FieldMatrix<Fraction> mt = createSparseMatrix(testData2T);
        assertClose("transpose",mt,m.transpose(),normTolerance);
    }

// org.apache.commons.math.linear.SparseFieldMatrixTest::testPremultiplyVector
    public void testPremultiplyVector() {
        FieldMatrix<Fraction> m = createSparseMatrix(testData);
        assertClose("premultiply", m.preMultiply(testVector), preMultTest,
            normTolerance);
        assertClose("premultiply", m.preMultiply(
            new ArrayFieldVector<Fraction>(testVector).getData()), preMultTest, normTolerance);
        m = createSparseMatrix(bigSingular);
        try {
            m.preMultiply(testVector);
            fail("expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.linear.SparseFieldMatrixTest::testPremultiply
    public void testPremultiply() {
        FieldMatrix<Fraction> m3 = createSparseMatrix(d3);
        FieldMatrix<Fraction> m4 = createSparseMatrix(d4);
        FieldMatrix<Fraction> m5 = createSparseMatrix(d5);
        assertClose("m3*m4=m5", m4.preMultiply(m3), m5, entryTolerance);

        SparseFieldMatrix<Fraction> m = createSparseMatrix(testData);
        SparseFieldMatrix<Fraction> mInv = createSparseMatrix(testDataInv);
        SparseFieldMatrix<Fraction> identity = createSparseMatrix(id);
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
            fail("Expecting illegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.linear.SparseFieldMatrixTest::testGetVectors
    public void testGetVectors() {
        FieldMatrix<Fraction> m = createSparseMatrix(testData);
        assertClose("get row", m.getRow(0), testDataRow1, entryTolerance);
        assertClose("get col", m.getColumn(2), testDataCol3, entryTolerance);
        try {
            m.getRow(10);
            fail("expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.getColumn(-1);
            fail("expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
    }

// org.apache.commons.math.linear.SparseFieldMatrixTest::testGetEntry
    public void testGetEntry() {
        FieldMatrix<Fraction> m = createSparseMatrix(testData);
        assertEquals("get entry", m.getEntry(0, 1).doubleValue(), 2d, entryTolerance);
        try {
            m.getEntry(10, 4);
            fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
    }

// org.apache.commons.math.linear.SparseFieldMatrixTest::testExamples
    public void testExamples() {
        
        Fraction[][] matrixData = { { new Fraction(1), new Fraction(2), new Fraction(3) }, { new Fraction(2), new Fraction(5), new Fraction(3) } };
        FieldMatrix<Fraction> m = createSparseMatrix(matrixData);
        
        Fraction[][] matrixData2 = { { new Fraction(1), new Fraction(2) }, { new Fraction(2), new Fraction(5) }, { new Fraction(1), new Fraction(7) } };
        FieldMatrix<Fraction> n = createSparseMatrix(matrixData2);
        
        FieldMatrix<Fraction> p = m.multiply(n);
        assertEquals(2, p.getRowDimension());
        assertEquals(2, p.getColumnDimension());
        
        FieldMatrix<Fraction> pInverse = new FieldLUDecompositionImpl<Fraction>(p).getSolver().getInverse();
        assertEquals(2, pInverse.getRowDimension());
        assertEquals(2, pInverse.getColumnDimension());

        
        Fraction[][] coefficientsData = { { new Fraction(2), new Fraction(3), new Fraction(-2) }, { new Fraction(-1), new Fraction(7), new Fraction(6) },
                { new Fraction(4), new Fraction(-3), new Fraction(-5) } };
        FieldMatrix<Fraction> coefficients = createSparseMatrix(coefficientsData);
        Fraction[] constants = { new Fraction(1), new Fraction(-2), new Fraction(1) };
        Fraction[] solution = new FieldLUDecompositionImpl<Fraction>(coefficients).getSolver().solve(constants);
        assertEquals((new Fraction(2).multiply((solution[0])).add(new Fraction(3).multiply(solution[1])).subtract(new Fraction(2).multiply(solution[2]))).doubleValue(),
                constants[0].doubleValue(), 1E-12);
        assertEquals(((new Fraction(-1).multiply(solution[0])).add(new Fraction(7).multiply(solution[1])).add(new Fraction(6).multiply(solution[2]))).doubleValue(),
                constants[1].doubleValue(), 1E-12);
        assertEquals(((new Fraction(4).multiply(solution[0])).subtract(new Fraction(3).multiply( solution[1])).subtract(new Fraction(5).multiply(solution[2]))).doubleValue(),
                constants[2].doubleValue(), 1E-12);

    }

// org.apache.commons.math.linear.SparseFieldMatrixTest::testSubMatrix
    public void testSubMatrix() {
        FieldMatrix<Fraction> m = createSparseMatrix(subTestData);
        FieldMatrix<Fraction> mRows23Cols00 = createSparseMatrix(subRows23Cols00);
        FieldMatrix<Fraction> mRows00Cols33 = createSparseMatrix(subRows00Cols33);
        FieldMatrix<Fraction> mRows01Cols23 = createSparseMatrix(subRows01Cols23);
        FieldMatrix<Fraction> mRows02Cols13 = createSparseMatrix(subRows02Cols13);
        FieldMatrix<Fraction> mRows03Cols12 = createSparseMatrix(subRows03Cols12);
        FieldMatrix<Fraction> mRows03Cols123 = createSparseMatrix(subRows03Cols123);
        FieldMatrix<Fraction> mRows20Cols123 = createSparseMatrix(subRows20Cols123);
        FieldMatrix<Fraction> mRows31Cols31 = createSparseMatrix(subRows31Cols31);
        assertEquals("Rows23Cols00", mRows23Cols00, m.getSubMatrix(2, 3, 0, 0));
        assertEquals("Rows00Cols33", mRows00Cols33, m.getSubMatrix(0, 0, 3, 3));
        assertEquals("Rows01Cols23", mRows01Cols23, m.getSubMatrix(0, 1, 2, 3));
        assertEquals("Rows02Cols13", mRows02Cols13,
            m.getSubMatrix(new int[] { 0, 2 }, new int[] { 1, 3 }));
        assertEquals("Rows03Cols12", mRows03Cols12,
            m.getSubMatrix(new int[] { 0, 3 }, new int[] { 1, 2 }));
        assertEquals("Rows03Cols123", mRows03Cols123,
            m.getSubMatrix(new int[] { 0, 3 }, new int[] { 1, 2, 3 }));
        assertEquals("Rows20Cols123", mRows20Cols123,
            m.getSubMatrix(new int[] { 2, 0 }, new int[] { 1, 2, 3 }));
        assertEquals("Rows31Cols31", mRows31Cols31,
            m.getSubMatrix(new int[] { 3, 1 }, new int[] { 3, 1 }));
        assertEquals("Rows31Cols31", mRows31Cols31,
            m.getSubMatrix(new int[] { 3, 1 }, new int[] { 3, 1 }));

        try {
            m.getSubMatrix(1, 0, 2, 4);
            fail("Expecting NumberIsTooSmallException");
        } catch (NumberIsTooSmallException ex) {
            
        }
        try {
            m.getSubMatrix(-1, 1, 2, 2);
            fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.getSubMatrix(1, 0, 2, 2);
            fail("Expecting NumberIsTooSmallException");
        } catch (NumberIsTooSmallException ex) {
            
        }
        try {
            m.getSubMatrix(1, 0, 2, 4);
            fail("Expecting NumberIsTooSmallException");
        } catch (NumberIsTooSmallException ex) {
            
        }
        try {
            m.getSubMatrix(new int[] {}, new int[] { 0 });
            fail("Expecting NoDataException");
        } catch (NoDataException ex) {
            
        }
        try {
            m.getSubMatrix(new int[] { 0 }, new int[] { 4 });
            fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
    }

// org.apache.commons.math.linear.SparseFieldMatrixTest::testGetRowMatrix
    public void testGetRowMatrix() {
        FieldMatrix<Fraction> m = createSparseMatrix(subTestData);
        FieldMatrix<Fraction> mRow0 = createSparseMatrix(subRow0);
        FieldMatrix<Fraction> mRow3 = createSparseMatrix(subRow3);
        assertEquals("Row0", mRow0, m.getRowMatrix(0));
        assertEquals("Row3", mRow3, m.getRowMatrix(3));
        try {
            m.getRowMatrix(-1);
            fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.getRowMatrix(4);
            fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
    }

// org.apache.commons.math.linear.SparseFieldMatrixTest::testGetColumnMatrix
    public void testGetColumnMatrix() {
        FieldMatrix<Fraction> m = createSparseMatrix(subTestData);
        FieldMatrix<Fraction> mColumn1 = createSparseMatrix(subColumn1);
        FieldMatrix<Fraction> mColumn3 = createSparseMatrix(subColumn3);
        assertEquals("Column1", mColumn1, m.getColumnMatrix(1));
        assertEquals("Column3", mColumn3, m.getColumnMatrix(3));
        try {
            m.getColumnMatrix(-1);
            fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.getColumnMatrix(4);
            fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
    }

// org.apache.commons.math.linear.SparseFieldMatrixTest::testGetRowVector
    public void testGetRowVector() {
        FieldMatrix<Fraction> m = createSparseMatrix(subTestData);
        FieldVector<Fraction> mRow0 = new ArrayFieldVector<Fraction>(subRow0[0]);
        FieldVector<Fraction> mRow3 = new ArrayFieldVector<Fraction>(subRow3[0]);
        assertEquals("Row0", mRow0, m.getRowVector(0));
        assertEquals("Row3", mRow3, m.getRowVector(3));
        try {
            m.getRowVector(-1);
            fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.getRowVector(4);
            fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
    }

// org.apache.commons.math.linear.SparseFieldMatrixTest::testGetColumnVector
    public void testGetColumnVector() {
        FieldMatrix<Fraction> m = createSparseMatrix(subTestData);
        FieldVector<Fraction> mColumn1 = columnToVector(subColumn1);
        FieldVector<Fraction> mColumn3 = columnToVector(subColumn3);
        assertEquals("Column1", mColumn1, m.getColumnVector(1));
        assertEquals("Column3", mColumn3, m.getColumnVector(3));
        try {
            m.getColumnVector(-1);
            fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.getColumnVector(4);
            fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
    }

// org.apache.commons.math.linear.SparseFieldMatrixTest::testEqualsAndHashCode
    public void testEqualsAndHashCode() {
        SparseFieldMatrix<Fraction> m = createSparseMatrix(testData);
        SparseFieldMatrix<Fraction> m1 = (SparseFieldMatrix<Fraction>) m.copy();
        SparseFieldMatrix<Fraction> mt = (SparseFieldMatrix<Fraction>) m.transpose();
        assertTrue(m.hashCode() != mt.hashCode());
        assertEquals(m.hashCode(), m1.hashCode());
        assertEquals(m, m);
        assertEquals(m, m1);
        assertFalse(m.equals(null));
        assertFalse(m.equals(mt));
        assertFalse(m.equals(createSparseMatrix(bigSingular)));
    }

// org.apache.commons.math.linear.SparseFieldMatrixTest::testSetSubMatrix
    public void testSetSubMatrix() throws Exception {
        SparseFieldMatrix<Fraction> m = createSparseMatrix(testData);
        m.setSubMatrix(detData2, 1, 1);
        FieldMatrix<Fraction> expected = createSparseMatrix(new Fraction[][] {
                { new Fraction(1), new Fraction(2), new Fraction(3) }, { new Fraction(2), new Fraction(1), new Fraction(3) }, { new Fraction(1), new Fraction(2), new Fraction(4) } });
        assertEquals(expected, m);

        m.setSubMatrix(detData2, 0, 0);
        expected = createSparseMatrix(new Fraction[][] {
                { new Fraction(1), new Fraction(3), new Fraction(3) }, { new Fraction(2), new Fraction(4), new Fraction(3) }, { new Fraction(1), new Fraction(2), new Fraction(4) } });
        assertEquals(expected, m);

        m.setSubMatrix(testDataPlus2, 0, 0);
        expected = createSparseMatrix(new Fraction[][] {
                { new Fraction(3), new Fraction(4), new Fraction(5) }, { new Fraction(4), new Fraction(7), new Fraction(5) }, { new Fraction(3), new Fraction(2), new Fraction(10) } });
        assertEquals(expected, m);

        
        SparseFieldMatrix<Fraction> matrix =
            createSparseMatrix(new Fraction[][] {
        { new Fraction(1), new Fraction(2), new Fraction(3), new Fraction(4) }, { new Fraction(5), new Fraction(6), new Fraction(7), new Fraction(8) }, { new Fraction(9), new Fraction(0), new Fraction(1), new Fraction(2) } });
        matrix.setSubMatrix(new Fraction[][] { { new Fraction(3), new Fraction(4) }, { new Fraction(5), new Fraction(6) } }, 1, 1);
        expected = createSparseMatrix(new Fraction[][] {
                { new Fraction(1), new Fraction(2), new Fraction(3), new Fraction(4) }, { new Fraction(5), new Fraction(3), new Fraction(4), new Fraction(8) }, { new Fraction(9), new Fraction(5), new Fraction(6), new Fraction(2) } });
        assertEquals(expected, matrix);

        
        try {
            m.setSubMatrix(testData, 1, 1);
            fail("expecting OutOfRangeException");
        } catch (OutOfRangeException e) {
            
        }
        
        try {
            m.setSubMatrix(testData, -1, 1);
            fail("expecting OutOfRangeException");
        } catch (OutOfRangeException e) {
            
        }
        try {
            m.setSubMatrix(testData, 1, -1);
            fail("expecting OutOfRangeException");
        } catch (OutOfRangeException e) {
            
        }

        
        try {
            m.setSubMatrix(null, 1, 1);
            fail("expecting NullArgumentException");
        } catch (NullArgumentException e) {
            
        }
        try {
            new SparseFieldMatrix<Fraction>(field, 0, 0);
            fail("expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException e) {
            
        }

        
        try {
            m.setSubMatrix(new Fraction[][] { { new Fraction(1) }, { new Fraction(2), new Fraction(3) } }, 0, 0);
            fail("expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException e) {
            
        }

        
        try {
            m.setSubMatrix(new Fraction[][] { {} }, 0, 0);
            fail("expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException e) {
            
        }

    }

// org.apache.commons.math.linear.SparseFieldVectorTest::testMapFunctions
    public void testMapFunctions() throws FractionConversionException {
        SparseFieldVector<Fraction> v1 = new SparseFieldVector<Fraction>(field,vec1);

        
        FieldVector<Fraction> v_mapAdd = v1.mapAdd(new Fraction(2));
        Fraction[] result_mapAdd = {new Fraction(3), new Fraction(4), new Fraction(5)};
        assertEquals("compare vectors" ,result_mapAdd,v_mapAdd.getData());

        
        FieldVector<Fraction> v_mapAddToSelf = v1.copy();
        v_mapAddToSelf.mapAddToSelf(new Fraction(2));
        Fraction[] result_mapAddToSelf = {new Fraction(3), new Fraction(4), new Fraction(5)};
        assertEquals("compare vectors" ,result_mapAddToSelf,v_mapAddToSelf.getData());

        
        FieldVector<Fraction> v_mapSubtract = v1.mapSubtract(new Fraction(2));
        Fraction[] result_mapSubtract = {new Fraction(-1), new Fraction(0), new Fraction(1)};
        assertEquals("compare vectors" ,result_mapSubtract,v_mapSubtract.getData());

        
        FieldVector<Fraction> v_mapSubtractToSelf = v1.copy();
        v_mapSubtractToSelf.mapSubtractToSelf(new Fraction(2));
        Fraction[] result_mapSubtractToSelf = {new Fraction(-1), new Fraction(0), new Fraction(1)};
        assertEquals("compare vectors" ,result_mapSubtractToSelf,v_mapSubtractToSelf.getData());

        
        FieldVector<Fraction> v_mapMultiply = v1.mapMultiply(new Fraction(2));
        Fraction[] result_mapMultiply = {new Fraction(2), new Fraction(4), new Fraction(6)};
        assertEquals("compare vectors" ,result_mapMultiply,v_mapMultiply.getData());

        
        FieldVector<Fraction> v_mapMultiplyToSelf = v1.copy();
        v_mapMultiplyToSelf.mapMultiplyToSelf(new Fraction(2));
        Fraction[] result_mapMultiplyToSelf = {new Fraction(2), new Fraction(4), new Fraction(6)};
        assertEquals("compare vectors" ,result_mapMultiplyToSelf,v_mapMultiplyToSelf.getData());

        
        FieldVector<Fraction> v_mapDivide = v1.mapDivide(new Fraction(2));
        Fraction[] result_mapDivide = {new Fraction(.5d), new Fraction(1), new Fraction(1.5d)};
        assertEquals("compare vectors" ,result_mapDivide,v_mapDivide.getData());

        
        FieldVector<Fraction> v_mapDivideToSelf = v1.copy();
        v_mapDivideToSelf.mapDivideToSelf(new Fraction(2));
        Fraction[] result_mapDivideToSelf = {new Fraction(.5d), new Fraction(1), new Fraction(1.5d)};
        assertEquals("compare vectors" ,result_mapDivideToSelf,v_mapDivideToSelf.getData());

        
        FieldVector<Fraction> v_mapInv = v1.mapInv();
        Fraction[] result_mapInv = {new Fraction(1),new Fraction(0.5d),new Fraction(3.333333333333333e-01d)};
        assertEquals("compare vectors" ,result_mapInv,v_mapInv.getData());

        
        FieldVector<Fraction> v_mapInvToSelf = v1.copy();
        v_mapInvToSelf.mapInvToSelf();
        Fraction[] result_mapInvToSelf = {new Fraction(1),new Fraction(0.5d),new Fraction(3.333333333333333e-01d)};
        assertEquals("compare vectors" ,result_mapInvToSelf,v_mapInvToSelf.getData());

    }

// org.apache.commons.math.linear.SparseFieldVectorTest::testBasicFunctions
    public void testBasicFunctions() throws FractionConversionException {
        SparseFieldVector<Fraction> v1 = new SparseFieldVector<Fraction>(field,vec1);
        SparseFieldVector<Fraction> v2 = new SparseFieldVector<Fraction>(field,vec2);

        SparseFieldVector<Fraction> v2_t = new SparseFieldVector<Fraction>(field,vec2);

        
        FieldVector<Fraction> v_add = v1.add(v2);
        Fraction[] result_add = {new Fraction(5), new Fraction(7), new Fraction(9)};
        assertEquals("compare vect" ,v_add.getData(),result_add);

        SparseFieldVector<Fraction> vt2 = new SparseFieldVector<Fraction>(field,vec2);
        FieldVector<Fraction> v_add_i = v1.add(vt2);
        Fraction[] result_add_i = {new Fraction(5), new Fraction(7), new Fraction(9)};
        assertEquals("compare vect" ,v_add_i.getData(),result_add_i);

        
        SparseFieldVector<Fraction> v_subtract = v1.subtract(v2);
        Fraction[] result_subtract = {new Fraction(-3), new Fraction(-3), new Fraction(-3)};
        assertClose("compare vect" ,v_subtract.getData(),result_subtract,normTolerance);

        FieldVector<Fraction> v_subtract_i = v1.subtract(vt2);
        Fraction[] result_subtract_i = {new Fraction(-3), new Fraction(-3), new Fraction(-3)};
        assertClose("compare vect" ,v_subtract_i.getData(),result_subtract_i,normTolerance);

        
        FieldVector<Fraction>  v_ebeMultiply = v1.ebeMultiply(v2);
        Fraction[] result_ebeMultiply = {new Fraction(4), new Fraction(10), new Fraction(18)};
        assertClose("compare vect" ,v_ebeMultiply.getData(),result_ebeMultiply,normTolerance);

        FieldVector<Fraction>  v_ebeMultiply_2 = v1.ebeMultiply(v2_t);
        Fraction[] result_ebeMultiply_2 = {new Fraction(4), new Fraction(10), new Fraction(18)};
        assertClose("compare vect" ,v_ebeMultiply_2.getData(),result_ebeMultiply_2,normTolerance);

        
        FieldVector<Fraction>  v_ebeDivide = v1.ebeDivide(v2);
        Fraction[] result_ebeDivide = {new Fraction(0.25d), new Fraction(0.4d), new Fraction(0.5d)};
        assertClose("compare vect" ,v_ebeDivide.getData(),result_ebeDivide,normTolerance);

        FieldVector<Fraction>  v_ebeDivide_2 = v1.ebeDivide(v2_t);
        Fraction[] result_ebeDivide_2 = {new Fraction(0.25d), new Fraction(0.4d), new Fraction(0.5d)};
        assertClose("compare vect" ,v_ebeDivide_2.getData(),result_ebeDivide_2,normTolerance);

        
        Fraction dot =  v1.dotProduct(v2);
        assertEquals("compare val ",new Fraction(32), dot);

        
        Fraction dot_2 =  v1.dotProduct(v2_t);
        assertEquals("compare val ",new Fraction(32), dot_2);

        FieldMatrix<Fraction> m_outerProduct = v1.outerProduct(v2);
        assertEquals("compare val ",new Fraction(4), m_outerProduct.getEntry(0,0));

        FieldMatrix<Fraction> m_outerProduct_2 = v1.outerProduct(v2_t);
        assertEquals("compare val ",new Fraction(4), m_outerProduct_2.getEntry(0,0));

    }

// org.apache.commons.math.linear.SparseFieldVectorTest::testMisc
    public void testMisc() {
        SparseFieldVector<Fraction> v1 = new SparseFieldVector<Fraction>(field,vec1);

        String out1 = v1.toString();
        assertTrue("some output ",  out1.length()!=0);
        try {
            v1.checkVectorDimensions(2);
            fail("MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }

    }

// org.apache.commons.math.linear.SparseFieldVectorTest::testPredicates
    public void testPredicates() {

        SparseFieldVector<Fraction> v = new SparseFieldVector<Fraction>(field, new Fraction[] { new Fraction(0), new Fraction(1), new Fraction(2) });

        v.setEntry(0, field.getZero());
        assertEquals(v, new SparseFieldVector<Fraction>(field, new Fraction[] { new Fraction(0), new Fraction(1), new Fraction(2) }));
        assertNotSame(v, new SparseFieldVector<Fraction>(field, new Fraction[] { new Fraction(0), new Fraction(1), new Fraction(2), new Fraction(3) }));

    }

// org.apache.commons.math.linear.SparseRealMatrixTest::testDimensions
    public void testDimensions() {
        OpenMapRealMatrix m = createSparseMatrix(testData);
        OpenMapRealMatrix m2 = createSparseMatrix(testData2);
        assertEquals("testData row dimension", 3, m.getRowDimension());
        assertEquals("testData column dimension", 3, m.getColumnDimension());
        assertTrue("testData is square", m.isSquare());
        assertEquals("testData2 row dimension", m2.getRowDimension(), 2);
        assertEquals("testData2 column dimension", m2.getColumnDimension(), 3);
        assertTrue("testData2 is not square", !m2.isSquare());
    }

// org.apache.commons.math.linear.SparseRealMatrixTest::testCopyFunctions
    public void testCopyFunctions() {
        OpenMapRealMatrix m1 = createSparseMatrix(testData);
        RealMatrix m2 = m1.copy();
        assertEquals(m1.getClass(), m2.getClass());
        assertEquals((m2), m1);
        OpenMapRealMatrix m3 = createSparseMatrix(testData);
        RealMatrix m4 = m3.copy();
        assertEquals(m3.getClass(), m4.getClass());
        assertEquals((m4), m3);
    }

// org.apache.commons.math.linear.SparseRealMatrixTest::testAdd
    public void testAdd() {
        OpenMapRealMatrix m = createSparseMatrix(testData);
        OpenMapRealMatrix mInv = createSparseMatrix(testDataInv);
        OpenMapRealMatrix mDataPlusInv = createSparseMatrix(testDataPlusInv);
        RealMatrix mPlusMInv = m.add(mInv);
        for (int row = 0; row < m.getRowDimension(); row++) {
            for (int col = 0; col < m.getColumnDimension(); col++) {
                assertEquals("sum entry entry",
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
            fail("MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.linear.SparseRealMatrixTest::testNorm
    public void testNorm() {
        OpenMapRealMatrix m = createSparseMatrix(testData);
        OpenMapRealMatrix m2 = createSparseMatrix(testData2);
        assertEquals("testData norm", 14d, m.getNorm(), entryTolerance);
        assertEquals("testData2 norm", 7d, m2.getNorm(), entryTolerance);
    }

// org.apache.commons.math.linear.SparseRealMatrixTest::testPlusMinus
    public void testPlusMinus() {
        OpenMapRealMatrix m = createSparseMatrix(testData);
        OpenMapRealMatrix n = createSparseMatrix(testDataInv);
        assertClose("m-n = m + -n", m.subtract(n),
            n.scalarMultiply(-1d).add(m), entryTolerance);
        try {
            m.subtract(createSparseMatrix(testData2));
            fail("Expecting illegalArgumentException");
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
            fail("Expecting illegalArgumentException");
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
        assertEquals("identity trace", 3d, m.getTrace(), entryTolerance);
        m = createSparseMatrix(testData2);
        try {
            m.getTrace();
            fail("Expecting NonSquareMatrixException");
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
                new ArrayRealVector(testVector)).getData(), entryTolerance);
        m = createSparseMatrix(bigSingular);
        try {
            m.operate(testVector);
            fail("Expecting illegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.linear.SparseRealMatrixTest::testMath209
    public void testMath209() {
        RealMatrix a = createSparseMatrix(new double[][] {
                { 1, 2 }, { 3, 4 }, { 5, 6 } });
        double[] b = a.operate(new double[] { 1, 1 });
        assertEquals(a.getRowDimension(), b.length);
        assertEquals(3.0, b[0], 1.0e-12);
        assertEquals(7.0, b[1], 1.0e-12);
        assertEquals(11.0, b[2], 1.0e-12);
    }

// org.apache.commons.math.linear.SparseRealMatrixTest::testTranspose
    public void testTranspose() {

        RealMatrix m = createSparseMatrix(testData);
        RealMatrix mIT = new LUDecompositionImpl(m).getSolver().getInverse().transpose();
        RealMatrix mTI = new LUDecompositionImpl(m.transpose()).getSolver().getInverse();
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
            new ArrayRealVector(testVector).getData()), preMultTest, normTolerance);
        m = createSparseMatrix(bigSingular);
        try {
            m.preMultiply(testVector);
            fail("expecting MathIllegalArgumentException");
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
            fail("Expecting illegalArgumentException");
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
            fail("expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.getColumn(-1);
            fail("expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
    }

// org.apache.commons.math.linear.SparseRealMatrixTest::testGetEntry
    public void testGetEntry() {
        RealMatrix m = createSparseMatrix(testData);
        assertEquals("get entry", m.getEntry(0, 1), 2d, entryTolerance);
        try {
            m.getEntry(10, 4);
            fail("Expecting OutOfRangeException");
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
        assertEquals(2, p.getRowDimension());
        assertEquals(2, p.getColumnDimension());
        
        RealMatrix pInverse = new LUDecompositionImpl(p).getSolver().getInverse();
        assertEquals(2, pInverse.getRowDimension());
        assertEquals(2, pInverse.getColumnDimension());

        
        double[][] coefficientsData = { { 2, 3, -2 }, { -1, 7, 6 },
                { 4, -3, -5 } };
        RealMatrix coefficients = createSparseMatrix(coefficientsData);
        double[] constants = { 1, -2, 1 };
        double[] solution = new LUDecompositionImpl(coefficients).getSolver().solve(constants);
        assertEquals(2 * solution[0] + 3 * solution[1] - 2 * solution[2],
                constants[0], 1E-12);
        assertEquals(-1 * solution[0] + 7 * solution[1] + 6 * solution[2],
                constants[1], 1E-12);
        assertEquals(4 * solution[0] - 3 * solution[1] - 5 * solution[2],
                constants[2], 1E-12);

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
        assertEquals("Rows23Cols00", mRows23Cols00, m.getSubMatrix(2, 3, 0, 0));
        assertEquals("Rows00Cols33", mRows00Cols33, m.getSubMatrix(0, 0, 3, 3));
        assertEquals("Rows01Cols23", mRows01Cols23, m.getSubMatrix(0, 1, 2, 3));
        assertEquals("Rows02Cols13", mRows02Cols13,
            m.getSubMatrix(new int[] { 0, 2 }, new int[] { 1, 3 }));
        assertEquals("Rows03Cols12", mRows03Cols12,
            m.getSubMatrix(new int[] { 0, 3 }, new int[] { 1, 2 }));
        assertEquals("Rows03Cols123", mRows03Cols123,
            m.getSubMatrix(new int[] { 0, 3 }, new int[] { 1, 2, 3 }));
        assertEquals("Rows20Cols123", mRows20Cols123,
            m.getSubMatrix(new int[] { 2, 0 }, new int[] { 1, 2, 3 }));
        assertEquals("Rows31Cols31", mRows31Cols31,
            m.getSubMatrix(new int[] { 3, 1 }, new int[] { 3, 1 }));
        assertEquals("Rows31Cols31", mRows31Cols31,
            m.getSubMatrix(new int[] { 3, 1 }, new int[] { 3, 1 }));

        try {
            m.getSubMatrix(1, 0, 2, 4);
            fail("Expecting NumberIsTooSmallException");
        } catch (NumberIsTooSmallException ex) {
            
        }
        try {
            m.getSubMatrix(-1, 1, 2, 2);
            fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.getSubMatrix(1, 0, 2, 2);
            fail("Expecting NumberIsTooSmallException");
        } catch (NumberIsTooSmallException ex) {
            
        }
        try {
            m.getSubMatrix(1, 0, 2, 4);
            fail("Expecting NumberIsTooSmallException");
        } catch (NumberIsTooSmallException ex) {
            
        }
        try {
            m.getSubMatrix(new int[] {}, new int[] { 0 });
            fail("Expecting NoDataException");
        } catch (NoDataException ex) {
            
        }
        try {
            m.getSubMatrix(new int[] { 0 }, new int[] { 4 });
            fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
    }

// org.apache.commons.math.linear.SparseRealMatrixTest::testGetRowMatrix
    public void testGetRowMatrix() {
        RealMatrix m = createSparseMatrix(subTestData);
        RealMatrix mRow0 = createSparseMatrix(subRow0);
        RealMatrix mRow3 = createSparseMatrix(subRow3);
        assertEquals("Row0", mRow0, m.getRowMatrix(0));
        assertEquals("Row3", mRow3, m.getRowMatrix(3));
        try {
            m.getRowMatrix(-1);
            fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.getRowMatrix(4);
            fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
    }

// org.apache.commons.math.linear.SparseRealMatrixTest::testGetColumnMatrix
    public void testGetColumnMatrix() {
        RealMatrix m = createSparseMatrix(subTestData);
        RealMatrix mColumn1 = createSparseMatrix(subColumn1);
        RealMatrix mColumn3 = createSparseMatrix(subColumn3);
        assertEquals("Column1", mColumn1, m.getColumnMatrix(1));
        assertEquals("Column3", mColumn3, m.getColumnMatrix(3));
        try {
            m.getColumnMatrix(-1);
            fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.getColumnMatrix(4);
            fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
    }

// org.apache.commons.math.linear.SparseRealMatrixTest::testGetRowVector
    public void testGetRowVector() {
        RealMatrix m = createSparseMatrix(subTestData);
        RealVector mRow0 = new ArrayRealVector(subRow0[0]);
        RealVector mRow3 = new ArrayRealVector(subRow3[0]);
        assertEquals("Row0", mRow0, m.getRowVector(0));
        assertEquals("Row3", mRow3, m.getRowVector(3));
        try {
            m.getRowVector(-1);
            fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.getRowVector(4);
            fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
    }

// org.apache.commons.math.linear.SparseRealMatrixTest::testGetColumnVector
    public void testGetColumnVector() {
        RealMatrix m = createSparseMatrix(subTestData);
        RealVector mColumn1 = columnToVector(subColumn1);
        RealVector mColumn3 = columnToVector(subColumn3);
        assertEquals("Column1", mColumn1, m.getColumnVector(1));
        assertEquals("Column3", mColumn3, m.getColumnVector(3));
        try {
            m.getColumnVector(-1);
            fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.getColumnVector(4);
            fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
    }

// org.apache.commons.math.linear.SparseRealMatrixTest::testEqualsAndHashCode
    public void testEqualsAndHashCode() {
        OpenMapRealMatrix m = createSparseMatrix(testData);
        OpenMapRealMatrix m1 = m.copy();
        OpenMapRealMatrix mt = (OpenMapRealMatrix) m.transpose();
        assertTrue(m.hashCode() != mt.hashCode());
        assertEquals(m.hashCode(), m1.hashCode());
        assertEquals(m, m);
        assertEquals(m, m1);
        assertFalse(m.equals(null));
        assertFalse(m.equals(mt));
        assertFalse(m.equals(createSparseMatrix(bigSingular)));
    }

// org.apache.commons.math.linear.SparseRealMatrixTest::testToString
    public void testToString() {
        OpenMapRealMatrix m = createSparseMatrix(testData);
        assertEquals("OpenMapRealMatrix{{1.0,2.0,3.0},{2.0,5.0,3.0},{1.0,0.0,8.0}}",
            m.toString());
        m = new OpenMapRealMatrix(1, 1);
        assertEquals("OpenMapRealMatrix{{0.0}}", m.toString());
    }

// org.apache.commons.math.linear.SparseRealMatrixTest::testSetSubMatrix
    public void testSetSubMatrix() throws Exception {
        OpenMapRealMatrix m = createSparseMatrix(testData);
        m.setSubMatrix(detData2, 1, 1);
        RealMatrix expected = createSparseMatrix(new double[][] {
                { 1.0, 2.0, 3.0 }, { 2.0, 1.0, 3.0 }, { 1.0, 2.0, 4.0 } });
        assertEquals(expected, m);

        m.setSubMatrix(detData2, 0, 0);
        expected = createSparseMatrix(new double[][] {
                { 1.0, 3.0, 3.0 }, { 2.0, 4.0, 3.0 }, { 1.0, 2.0, 4.0 } });
        assertEquals(expected, m);

        m.setSubMatrix(testDataPlus2, 0, 0);
        expected = createSparseMatrix(new double[][] {
                { 3.0, 4.0, 5.0 }, { 4.0, 7.0, 5.0 }, { 3.0, 2.0, 10.0 } });
        assertEquals(expected, m);

        
        OpenMapRealMatrix matrix =
            createSparseMatrix(new double[][] {
        { 1, 2, 3, 4 }, { 5, 6, 7, 8 }, { 9, 0, 1, 2 } });
        matrix.setSubMatrix(new double[][] { { 3, 4 }, { 5, 6 } }, 1, 1);
        expected = createSparseMatrix(new double[][] {
                { 1, 2, 3, 4 }, { 5, 3, 4, 8 }, { 9, 5, 6, 2 } });
        assertEquals(expected, matrix);

        
        try {
            m.setSubMatrix(testData, 1, 1);
            fail("expecting OutOfRangeException");
        } catch (OutOfRangeException e) {
            
        }
        
        try {
            m.setSubMatrix(testData, -1, 1);
            fail("expecting OutOfRangeException");
        } catch (OutOfRangeException e) {
            
        }
        try {
            m.setSubMatrix(testData, 1, -1);
            fail("expecting OutOfRangeException");
        } catch (OutOfRangeException e) {
            
        }

        
        try {
            m.setSubMatrix(null, 1, 1);
            fail("expecting NullPointerException");
        } catch (NullPointerException e) {
            
        }
        try {
            new OpenMapRealMatrix(0, 0);
            fail("expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException e) {
            
        }

        
        try {
            m.setSubMatrix(new double[][] { { 1 }, { 2, 3 } }, 0, 0);
            fail("expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException e) {
            
        }

        
        try {
            m.setSubMatrix(new double[][] { {} }, 0, 0);
            fail("expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException e) {
            
        }

    }

// org.apache.commons.math.linear.SparseRealMatrixTest::testSerial
    public void testSerial()  {
        OpenMapRealMatrix m = createSparseMatrix(testData);
        assertEquals(m,TestUtils.serializeAndRecover(m));
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

        RealVector v_append_3 = v1.append(vec2);
        Assert.assertEquals("testData len", 6, v_append_3.getDimension());
        Assert.assertEquals("testData is  ", 4.0, v_append_3.getEntry(3), 0);

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
        assertClose("compare vectors" ,result_mapAdd,v_mapAdd.getData(),normTolerance);

        
        RealVector v_mapAddToSelf = v1.copy();
        v_mapAddToSelf.mapAddToSelf(2.0d);
        double[] result_mapAddToSelf = {3d, 4d, 5d};
        assertClose("compare vectors" ,result_mapAddToSelf,v_mapAddToSelf.getData(),normTolerance);

        
        RealVector v_mapSubtract = v1.mapSubtract(2.0d);
        double[] result_mapSubtract = {-1d, 0d, 1d};
        assertClose("compare vectors" ,result_mapSubtract,v_mapSubtract.getData(),normTolerance);

        
        RealVector v_mapSubtractToSelf = v1.copy();
        v_mapSubtractToSelf.mapSubtractToSelf(2.0d);
        double[] result_mapSubtractToSelf = {-1d, 0d, 1d};
        assertClose("compare vectors" ,result_mapSubtractToSelf,v_mapSubtractToSelf.getData(),normTolerance);

        
        RealVector v_mapMultiply = v1.mapMultiply(2.0d);
        double[] result_mapMultiply = {2d, 4d, 6d};
        assertClose("compare vectors" ,result_mapMultiply,v_mapMultiply.getData(),normTolerance);

        
        RealVector v_mapMultiplyToSelf = v1.copy();
        v_mapMultiplyToSelf.mapMultiplyToSelf(2.0d);
        double[] result_mapMultiplyToSelf = {2d, 4d, 6d};
        assertClose("compare vectors" ,result_mapMultiplyToSelf,v_mapMultiplyToSelf.getData(),normTolerance);

        
        RealVector v_mapDivide = v1.mapDivide(2.0d);
        double[] result_mapDivide = {.5d, 1d, 1.5d};
        assertClose("compare vectors" ,result_mapDivide,v_mapDivide.getData(),normTolerance);

        
        RealVector v_mapDivideToSelf = v1.copy();
        v_mapDivideToSelf.mapDivideToSelf(2.0d);
        double[] result_mapDivideToSelf = {.5d, 1d, 1.5d};
        assertClose("compare vectors" ,result_mapDivideToSelf,v_mapDivideToSelf.getData(),normTolerance);

        
        RealVector v_mapPow = v1.map(new Power(2));
        double[] result_mapPow = {1d, 4d, 9d};
        assertClose("compare vectors" ,result_mapPow,v_mapPow.getData(),normTolerance);

        
        RealVector v_mapPowToSelf = v1.copy();
        v_mapPowToSelf.mapToSelf(new Power(2));
        double[] result_mapPowToSelf = {1d, 4d, 9d};
        assertClose("compare vectors" ,result_mapPowToSelf,v_mapPowToSelf.getData(),normTolerance);

        
        RealVector v_mapExp = v1.map(new Exp());
        double[] result_mapExp = {2.718281828459045e+00d,7.389056098930650e+00d, 2.008553692318767e+01d};
        assertClose("compare vectors" ,result_mapExp,v_mapExp.getData(),normTolerance);

        
        RealVector v_mapExpToSelf = v1.copy();
        v_mapExpToSelf.mapToSelf(new Exp());
        double[] result_mapExpToSelf = {2.718281828459045e+00d,7.389056098930650e+00d, 2.008553692318767e+01d};
        assertClose("compare vectors" ,result_mapExpToSelf,v_mapExpToSelf.getData(),normTolerance);

        
        RealVector v_mapExpm1 = v1.map(new Expm1());
        double[] result_mapExpm1 = {1.718281828459045d,6.38905609893065d, 19.085536923187668d};
        assertClose("compare vectors" ,result_mapExpm1,v_mapExpm1.getData(),normTolerance);

        
        RealVector v_mapExpm1ToSelf = v1.copy();
        v_mapExpm1ToSelf.mapToSelf(new Expm1());
        double[] result_mapExpm1ToSelf = {1.718281828459045d,6.38905609893065d, 19.085536923187668d};
        assertClose("compare vectors" ,result_mapExpm1ToSelf,v_mapExpm1ToSelf.getData(),normTolerance);

        
        RealVector v_mapLog = v1.map(new Log());
        double[] result_mapLog = {0d,6.931471805599453e-01d, 1.098612288668110e+00d};
        assertClose("compare vectors" ,result_mapLog,v_mapLog.getData(),normTolerance);

        
        RealVector v_mapLogToSelf = v1.copy();
        v_mapLogToSelf.mapToSelf(new Log());
        double[] result_mapLogToSelf = {0d,6.931471805599453e-01d, 1.098612288668110e+00d};
        assertClose("compare vectors" ,result_mapLogToSelf,v_mapLogToSelf.getData(),normTolerance);

        
        RealVector v_mapLog10 = v1.map(new Log10());
        double[] result_mapLog10 = {0d,3.010299956639812e-01d, 4.771212547196624e-01d};
        assertClose("compare vectors" ,result_mapLog10,v_mapLog10.getData(),normTolerance);

        
        RealVector v_mapLog10ToSelf = v1.copy();
        v_mapLog10ToSelf.mapToSelf(new Log10());
        double[] result_mapLog10ToSelf = {0d,3.010299956639812e-01d, 4.771212547196624e-01d};
        assertClose("compare vectors" ,result_mapLog10ToSelf,v_mapLog10ToSelf.getData(),normTolerance);

        
        RealVector v_mapLog1p = v1.map(new Log1p());
        double[] result_mapLog1p = {0.6931471805599453d,1.0986122886681096d,1.3862943611198906d};
        assertClose("compare vectors" ,result_mapLog1p,v_mapLog1p.getData(),normTolerance);

        
        RealVector v_mapLog1pToSelf = v1.copy();
        v_mapLog1pToSelf.mapToSelf(new Log1p());
        double[] result_mapLog1pToSelf = {0.6931471805599453d,1.0986122886681096d,1.3862943611198906d};
        assertClose("compare vectors" ,result_mapLog1pToSelf,v_mapLog1pToSelf.getData(),normTolerance);

        
        RealVector v_mapCosh = v1.map(new Cosh());
        double[] result_mapCosh = {1.543080634815244e+00d,3.762195691083631e+00d, 1.006766199577777e+01d};
        assertClose("compare vectors" ,result_mapCosh,v_mapCosh.getData(),normTolerance);

        
        RealVector v_mapCoshToSelf = v1.copy();
        v_mapCoshToSelf.mapToSelf(new Cosh());
        double[] result_mapCoshToSelf = {1.543080634815244e+00d,3.762195691083631e+00d, 1.006766199577777e+01d};
        assertClose("compare vectors" ,result_mapCoshToSelf,v_mapCoshToSelf.getData(),normTolerance);

        
        RealVector v_mapSinh = v1.map(new Sinh());
        double[] result_mapSinh = {1.175201193643801e+00d,3.626860407847019e+00d, 1.001787492740990e+01d};
        assertClose("compare vectors" ,result_mapSinh,v_mapSinh.getData(),normTolerance);

        
        RealVector v_mapSinhToSelf = v1.copy();
        v_mapSinhToSelf.mapToSelf(new Sinh());
        double[] result_mapSinhToSelf = {1.175201193643801e+00d,3.626860407847019e+00d, 1.001787492740990e+01d};
        assertClose("compare vectors" ,result_mapSinhToSelf,v_mapSinhToSelf.getData(),normTolerance);

        
        RealVector v_mapTanh = v1.map(new Tanh());
        double[] result_mapTanh = {7.615941559557649e-01d,9.640275800758169e-01d,9.950547536867305e-01d};
        assertClose("compare vectors" ,result_mapTanh,v_mapTanh.getData(),normTolerance);

        
        RealVector v_mapTanhToSelf = v1.copy();
        v_mapTanhToSelf.mapToSelf(new Tanh());
        double[] result_mapTanhToSelf = {7.615941559557649e-01d,9.640275800758169e-01d,9.950547536867305e-01d};
        assertClose("compare vectors" ,result_mapTanhToSelf,v_mapTanhToSelf.getData(),normTolerance);

        
        RealVector v_mapCos = v1.map(new Cos());
        double[] result_mapCos = {5.403023058681398e-01d,-4.161468365471424e-01d, -9.899924966004454e-01d};
        assertClose("compare vectors" ,result_mapCos,v_mapCos.getData(),normTolerance);

        
        RealVector v_mapCosToSelf = v1.copy();
        v_mapCosToSelf.mapToSelf(new Cos());
        double[] result_mapCosToSelf = {5.403023058681398e-01d,-4.161468365471424e-01d, -9.899924966004454e-01d};
        assertClose("compare vectors" ,result_mapCosToSelf,v_mapCosToSelf.getData(),normTolerance);

        
        RealVector v_mapSin = v1.map(new Sin());
        double[] result_mapSin = {8.414709848078965e-01d,9.092974268256817e-01d,1.411200080598672e-01d};
        assertClose("compare vectors" ,result_mapSin,v_mapSin.getData(),normTolerance);

        
        RealVector v_mapSinToSelf = v1.copy();
        v_mapSinToSelf.mapToSelf(new Sin());
        double[] result_mapSinToSelf = {8.414709848078965e-01d,9.092974268256817e-01d,1.411200080598672e-01d};
        assertClose("compare vectors" ,result_mapSinToSelf,v_mapSinToSelf.getData(),normTolerance);

        
        RealVector v_mapTan = v1.map(new Tan());
        double[] result_mapTan = {1.557407724654902e+00d,-2.185039863261519e+00d,-1.425465430742778e-01d};
        assertClose("compare vectors" ,result_mapTan,v_mapTan.getData(),normTolerance);

        
        RealVector v_mapTanToSelf = v1.copy();
        v_mapTanToSelf.mapToSelf(new Tan());
        double[] result_mapTanToSelf = {1.557407724654902e+00d,-2.185039863261519e+00d,-1.425465430742778e-01d};
        assertClose("compare vectors" ,result_mapTanToSelf,v_mapTanToSelf.getData(),normTolerance);

        double[] vat_a = {0d, 0.5d, 1.0d};
        OpenMapRealVector vat = new OpenMapRealVector(vat_a);

        
        RealVector v_mapAcos = vat.map(new Acos());
        double[] result_mapAcos = {1.570796326794897e+00d,1.047197551196598e+00d, 0.0d};
        assertClose("compare vectors" ,result_mapAcos,v_mapAcos.getData(),normTolerance);

        
        RealVector v_mapAcosToSelf = vat.copy();
        v_mapAcosToSelf.mapToSelf(new Acos());
        double[] result_mapAcosToSelf = {1.570796326794897e+00d,1.047197551196598e+00d, 0.0d};
        assertClose("compare vectors" ,result_mapAcosToSelf,v_mapAcosToSelf.getData(),normTolerance);

        
        RealVector v_mapAsin = vat.map(new Asin());
        double[] result_mapAsin = {0.0d,5.235987755982989e-01d,1.570796326794897e+00d};
        assertClose("compare vectors" ,result_mapAsin,v_mapAsin.getData(),normTolerance);

        
        RealVector v_mapAsinToSelf = vat.copy();
        v_mapAsinToSelf.mapToSelf(new Asin());
        double[] result_mapAsinToSelf = {0.0d,5.235987755982989e-01d,1.570796326794897e+00d};
        assertClose("compare vectors" ,result_mapAsinToSelf,v_mapAsinToSelf.getData(),normTolerance);

        
        RealVector v_mapAtan = vat.map(new Atan());
        double[] result_mapAtan = {0.0d,4.636476090008061e-01d,7.853981633974483e-01d};
        assertClose("compare vectors" ,result_mapAtan,v_mapAtan.getData(),normTolerance);

        
        RealVector v_mapAtanToSelf = vat.copy();
        v_mapAtanToSelf.mapToSelf(new Atan());
        double[] result_mapAtanToSelf = {0.0d,4.636476090008061e-01d,7.853981633974483e-01d};
        assertClose("compare vectors" ,result_mapAtanToSelf,v_mapAtanToSelf.getData(),normTolerance);

        
        RealVector v_mapInv = v1.map(new Inverse());
        double[] result_mapInv = {1d,0.5d,3.333333333333333e-01d};
        assertClose("compare vectors" ,result_mapInv,v_mapInv.getData(),normTolerance);

        
        RealVector v_mapInvToSelf = v1.copy();
        v_mapInvToSelf.mapToSelf(new Inverse());
        double[] result_mapInvToSelf = {1d,0.5d,3.333333333333333e-01d};
        assertClose("compare vectors" ,result_mapInvToSelf,v_mapInvToSelf.getData(),normTolerance);

        double[] abs_a = {-1.0d, 0.0d, 1.0d};
        OpenMapRealVector abs_v = new OpenMapRealVector(abs_a);

        
        RealVector v_mapAbs = abs_v.map(new Abs());
        double[] result_mapAbs = {1d,0d,1d};
        assertClose("compare vectors" ,result_mapAbs,v_mapAbs.getData(),normTolerance);

        
        RealVector v_mapAbsToSelf = abs_v.copy();
        v_mapAbsToSelf.mapToSelf(new Abs());
        double[] result_mapAbsToSelf = {1d,0d,1d};
        assertClose("compare vectors" ,result_mapAbsToSelf,v_mapAbsToSelf.getData(),normTolerance);

        
        RealVector v_mapSqrt = v1.map(new Sqrt());
        double[] result_mapSqrt = {1d,1.414213562373095e+00d,1.732050807568877e+00d};
        assertClose("compare vectors" ,result_mapSqrt,v_mapSqrt.getData(),normTolerance);

        
        RealVector v_mapSqrtToSelf = v1.copy();
        v_mapSqrtToSelf.mapToSelf(new Sqrt());
        double[] result_mapSqrtToSelf = {1d,1.414213562373095e+00d,1.732050807568877e+00d};
        assertClose("compare vectors" ,result_mapSqrtToSelf,v_mapSqrtToSelf.getData(),normTolerance);

        double[] cbrt_a = {-2.0d, 0.0d, 2.0d};
        OpenMapRealVector cbrt_v = new OpenMapRealVector(cbrt_a);

        
        RealVector v_mapCbrt = cbrt_v.map(new Cbrt());
        double[] result_mapCbrt = {-1.2599210498948732d,0d,1.2599210498948732d};
        assertClose("compare vectors" ,result_mapCbrt,v_mapCbrt.getData(),normTolerance);

        
        RealVector v_mapCbrtToSelf = cbrt_v.copy();
        v_mapCbrtToSelf.mapToSelf(new Cbrt());
        double[] result_mapCbrtToSelf =  {-1.2599210498948732d,0d,1.2599210498948732d};
        assertClose("compare vectors" ,result_mapCbrtToSelf,v_mapCbrtToSelf.getData(),normTolerance);

        double[] ceil_a = {-1.1d, 0.9d, 1.1d};
        OpenMapRealVector ceil_v = new OpenMapRealVector(ceil_a);

        
        RealVector v_mapCeil = ceil_v.map(new Ceil());
        double[] result_mapCeil = {-1d,1d,2d};
        assertClose("compare vectors" ,result_mapCeil,v_mapCeil.getData(),normTolerance);

        
        RealVector v_mapCeilToSelf = ceil_v.copy();
        v_mapCeilToSelf.mapToSelf(new Ceil());
        double[] result_mapCeilToSelf =  {-1d,1d,2d};
        assertClose("compare vectors" ,result_mapCeilToSelf,v_mapCeilToSelf.getData(),normTolerance);

        
        RealVector v_mapFloor = ceil_v.map(new Floor());
        double[] result_mapFloor = {-2d,0d,1d};
        assertClose("compare vectors" ,result_mapFloor,v_mapFloor.getData(),normTolerance);

        
        RealVector v_mapFloorToSelf = ceil_v.copy();
        v_mapFloorToSelf.mapToSelf(new Floor());
        double[] result_mapFloorToSelf =  {-2d,0d,1d};
        assertClose("compare vectors" ,result_mapFloorToSelf,v_mapFloorToSelf.getData(),normTolerance);

        
        RealVector v_mapRint = ceil_v.map(new Rint());
        double[] result_mapRint = {-1d,1d,1d};
        assertClose("compare vectors" ,result_mapRint,v_mapRint.getData(),normTolerance);

        
        RealVector v_mapRintToSelf = ceil_v.copy();
        v_mapRintToSelf.mapToSelf(new Rint());
        double[] result_mapRintToSelf =  {-1d,1d,1d};
        assertClose("compare vectors" ,result_mapRintToSelf,v_mapRintToSelf.getData(),normTolerance);

        
        RealVector v_mapSignum = ceil_v.map(new Signum());
        double[] result_mapSignum = {-1d,1d,1d};
        assertClose("compare vectors" ,result_mapSignum,v_mapSignum.getData(),normTolerance);

        
        RealVector v_mapSignumToSelf = ceil_v.copy();
        v_mapSignumToSelf.mapToSelf(new Signum());
        double[] result_mapSignumToSelf =  {-1d,1d,1d};
        assertClose("compare vectors" ,result_mapSignumToSelf,v_mapSignumToSelf.getData(),normTolerance);

        
        
        RealVector v_mapUlp = ceil_v.map(new Ulp());
        double[] result_mapUlp = {2.220446049250313E-16d,1.1102230246251565E-16d,2.220446049250313E-16d};
        assertClose("compare vectors" ,result_mapUlp,v_mapUlp.getData(),normTolerance);

        
        RealVector v_mapUlpToSelf = ceil_v.copy();
        v_mapUlpToSelf.mapToSelf(new Ulp());
        double[] result_mapUlpToSelf = {2.220446049250313E-16d,1.1102230246251565E-16d,2.220446049250313E-16d};
        assertClose("compare vectors" ,result_mapUlpToSelf,v_mapUlpToSelf.getData(),normTolerance);
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
        assertClose("compare vect" ,v_add.getData(),result_add,normTolerance);

        SparseRealVectorTestImpl vt2 = new SparseRealVectorTestImpl(vec2);
        RealVector v_add_i = v1.add(vt2);
        double[] result_add_i = {5d, 7d, 9d};
        assertClose("compare vect" ,v_add_i.getData(),result_add_i,normTolerance);

        
        OpenMapRealVector v_subtract = v1.subtract(v2);
        double[] result_subtract = {-3d, -3d, -3d};
        assertClose("compare vect" ,v_subtract.getData(),result_subtract,normTolerance);

        RealVector v_subtract_i = v1.subtract(vt2);
        double[] result_subtract_i = {-3d, -3d, -3d};
        assertClose("compare vect" ,v_subtract_i.getData(),result_subtract_i,normTolerance);

        
        RealVector  v_ebeMultiply = v1.ebeMultiply(v2);
        double[] result_ebeMultiply = {4d, 10d, 18d};
        assertClose("compare vect" ,v_ebeMultiply.getData(),result_ebeMultiply,normTolerance);

        RealVector  v_ebeMultiply_2 = v1.ebeMultiply(v2_t);
        double[] result_ebeMultiply_2 = {4d, 10d, 18d};
        assertClose("compare vect" ,v_ebeMultiply_2.getData(),result_ebeMultiply_2,normTolerance);

        
        RealVector  v_ebeDivide = v1.ebeDivide(v2);
        double[] result_ebeDivide = {0.25d, 0.4d, 0.5d};
        assertClose("compare vect" ,v_ebeDivide.getData(),result_ebeDivide,normTolerance);

        RealVector  v_ebeDivide_2 = v1.ebeDivide(v2_t);
        double[] result_ebeDivide_2 = {0.25d, 0.4d, 0.5d};
        assertClose("compare vect" ,v_ebeDivide_2.getData(),result_ebeDivide_2,normTolerance);

        
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
        assertClose("compare vect" ,v_unitVector.getData(),v_unitVector_2.getData(),normTolerance);

        try {
            v_null.unitVector();
            Assert.fail("Expecting MathArithmeticException");
        } catch (MathArithmeticException ex) {
            
        }

        OpenMapRealVector v_unitize = v1.copy();
        v_unitize.unitize();
        assertClose("compare vect" ,v_unitVector_2.getData(),v_unitize.getData(),normTolerance);
        try {
            v_null.unitize();
            Assert.fail("Expecting MathArithmeticException");
        } catch (MathArithmeticException ex) {
            
        }

        RealVector v_projection = v1.projection(v2);
        double[] result_projection = {1.662337662337662, 2.0779220779220777, 2.493506493506493};
        assertClose("compare vect", v_projection.getData(), result_projection, normTolerance);

        RealVector v_projection_2 = v1.projection(v2_t);
        double[] result_projection_2 = {1.662337662337662, 2.0779220779220777, 2.493506493506493};
        assertClose("compare vect", v_projection_2.getData(), result_projection_2, normTolerance);

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

// org.apache.commons.math.linear.TriDiagonalTransformerTest::testNonSquare
    public void testNonSquare() {
        try {
            new TriDiagonalTransformer(MatrixUtils.createRealMatrix(new double[3][2]));
            fail("an exception should have been thrown");
        } catch (NonSquareMatrixException ime) {
            
        }
    }

// org.apache.commons.math.linear.TriDiagonalTransformerTest::testAEqualQTQt
    public void testAEqualQTQt() {
        checkAEqualQTQt(MatrixUtils.createRealMatrix(testSquare5));
        checkAEqualQTQt(MatrixUtils.createRealMatrix(testSquare3));
    }

// org.apache.commons.math.linear.TriDiagonalTransformerTest::testNoAccessBelowDiagonal
    public void testNoAccessBelowDiagonal() {
        checkNoAccessBelowDiagonal(testSquare5);
        checkNoAccessBelowDiagonal(testSquare3);
    }

// org.apache.commons.math.linear.TriDiagonalTransformerTest::testQOrthogonal
    public void testQOrthogonal() {
        checkOrthogonal(new TriDiagonalTransformer(MatrixUtils.createRealMatrix(testSquare5)).getQ());
        checkOrthogonal(new TriDiagonalTransformer(MatrixUtils.createRealMatrix(testSquare3)).getQ());
    }

// org.apache.commons.math.linear.TriDiagonalTransformerTest::testQTOrthogonal
    public void testQTOrthogonal() {
        checkOrthogonal(new TriDiagonalTransformer(MatrixUtils.createRealMatrix(testSquare5)).getQT());
        checkOrthogonal(new TriDiagonalTransformer(MatrixUtils.createRealMatrix(testSquare3)).getQT());
    }

// org.apache.commons.math.linear.TriDiagonalTransformerTest::testTTriDiagonal
    public void testTTriDiagonal() {
        checkTriDiagonal(new TriDiagonalTransformer(MatrixUtils.createRealMatrix(testSquare5)).getT());
        checkTriDiagonal(new TriDiagonalTransformer(MatrixUtils.createRealMatrix(testSquare3)).getT());
    }

// org.apache.commons.math.linear.TriDiagonalTransformerTest::testMatricesValues5
    public void testMatricesValues5() {
        checkMatricesValues(testSquare5,
                            new double[][] {
                                { 1.0,  0.0,                 0.0,                  0.0,                   0.0 },
                                { 0.0, -0.5163977794943222,  0.016748280772542083, 0.839800693771262,     0.16669620021405473 },
                                { 0.0, -0.7745966692414833, -0.4354553000860955,  -0.44989322880603355,  -0.08930153582895772 },
                                { 0.0, -0.2581988897471611,  0.6364346693566014,  -0.30263204032131164,   0.6608313651342882 },
                                { 0.0, -0.2581988897471611,  0.6364346693566009,  -0.027289660803112598, -0.7263191580755246 }
                            },
                            new double[] { 1, 4.4, 1.433099579242636, -0.89537362758743, 2.062274048344794 },
                            new double[] { -FastMath.sqrt(15), -3.0832882879592476, 0.6082710842351517, 1.1786086405912128 });
    }

// org.apache.commons.math.linear.TriDiagonalTransformerTest::testMatricesValues3
    public void testMatricesValues3() {
        checkMatricesValues(testSquare3,
                            new double[][] {
                                {  1.0,  0.0,  0.0 },
                                {  0.0, -0.6,  0.8 },
                                {  0.0, -0.8, -0.6 },
                            },
                            new double[] { 1, 2.64, -0.64 },
                            new double[] { -5, -1.52 });
    }

// org.apache.commons.math.ode.ContinuousOutputModelTest::testBoundaries
  public void testBoundaries()
    throws MathUserException, IntegratorException {
    integ.addStepHandler(new ContinuousOutputModel());
    integ.integrate(pb,
                    pb.getInitialTime(), pb.getInitialState(),
                    pb.getFinalTime(), new double[pb.getDimension()]);
    ContinuousOutputModel cm = (ContinuousOutputModel) integ.getStepHandlers().iterator().next();
    cm.setInterpolatedTime(2.0 * pb.getInitialTime() - pb.getFinalTime());
    cm.setInterpolatedTime(2.0 * pb.getFinalTime() - pb.getInitialTime());
    cm.setInterpolatedTime(0.5 * (pb.getFinalTime() + pb.getInitialTime()));
  }

// org.apache.commons.math.ode.ContinuousOutputModelTest::testRandomAccess
  public void testRandomAccess()
    throws MathUserException, IntegratorException {

    ContinuousOutputModel cm = new ContinuousOutputModel();
    integ.addStepHandler(cm);
    integ.integrate(pb,
                    pb.getInitialTime(), pb.getInitialState(),
                    pb.getFinalTime(), new double[pb.getDimension()]);

    Random random = new Random(347588535632l);
    double maxError = 0.0;
    for (int i = 0; i < 1000; ++i) {
      double r = random.nextDouble();
      double time = r * pb.getInitialTime() + (1.0 - r) * pb.getFinalTime();
      cm.setInterpolatedTime(time);
      double[] interpolatedY = cm.getInterpolatedState ();
      double[] theoreticalY  = pb.computeTheoreticalState(time);
      double dx = interpolatedY[0] - theoreticalY[0];
      double dy = interpolatedY[1] - theoreticalY[1];
      double error = dx * dx + dy * dy;
      if (error > maxError) {
        maxError = error;
      }
    }

    assertTrue(maxError < 1.0e-9);

  }

// org.apache.commons.math.ode.ContinuousOutputModelTest::testModelsMerging
  public void testModelsMerging()
    throws MathUserException, IntegratorException {

      
      FirstOrderDifferentialEquations problem =
          new FirstOrderDifferentialEquations() {
              private static final long serialVersionUID = 2472449657345878299L;
              public void computeDerivatives(double t, double[] y, double[] dot)
                  throws MathUserException {
                  dot[0] = -y[1];
                  dot[1] =  y[0];
              }
              public int getDimension() {
                  return 2;
              }
          };

      
      ContinuousOutputModel cm1 = new ContinuousOutputModel();
      FirstOrderIntegrator integ1 =
          new DormandPrince853Integrator(0, 1.0, 1.0e-8, 1.0e-8);
      integ1.addStepHandler(cm1);
      integ1.integrate(problem, FastMath.PI, new double[] { -1.0, 0.0 },
                       0, new double[2]);

      
      ContinuousOutputModel cm2 = new ContinuousOutputModel();
      FirstOrderIntegrator integ2 =
          new DormandPrince853Integrator(0, 0.1, 1.0e-12, 1.0e-12);
      integ2.addStepHandler(cm2);
      integ2.integrate(problem, 2.0 * FastMath.PI, new double[] { 1.0, 0.0 },
                       FastMath.PI, new double[2]);

      
      ContinuousOutputModel cm = new ContinuousOutputModel();
      cm.append(cm2);
      cm.append(new ContinuousOutputModel());
      cm.append(cm1);

      
      assertEquals(2.0 * FastMath.PI, cm.getInitialTime(), 1.0e-12);
      assertEquals(0, cm.getFinalTime(), 1.0e-12);
      assertEquals(cm.getFinalTime(), cm.getInterpolatedTime(), 1.0e-12);
      for (double t = 0; t < 2.0 * FastMath.PI; t += 0.1) {
          cm.setInterpolatedTime(t);
          double[] y = cm.getInterpolatedState();
          assertEquals(FastMath.cos(t), y[0], 1.0e-7);
          assertEquals(FastMath.sin(t), y[1], 1.0e-7);
      }

  }

// org.apache.commons.math.ode.ContinuousOutputModelTest::testErrorConditions
  public void testErrorConditions()
    throws MathUserException {

      ContinuousOutputModel cm = new ContinuousOutputModel();
      cm.handleStep(buildInterpolator(0, new double[] { 0.0, 1.0, -2.0 }, 1), true);

      
      assertTrue(checkAppendError(cm, 1.0, new double[] { 0.0, 1.0 }, 2.0));

      
      assertTrue(checkAppendError(cm, 10.0, new double[] { 0.0, 1.0, -2.0 }, 20.0));

      
      assertTrue(checkAppendError(cm, 1.0, new double[] { 0.0, 1.0, -2.0 }, 0.0));

      
      assertFalse(checkAppendError(cm, 1.0, new double[] { 0.0, 1.0, -2.0 }, 2.0));

  }

// org.apache.commons.math.ode.FirstOrderConverterTest::testDoubleDimension
  public void testDoubleDimension() {
    for (int i = 1; i < 10; ++i) {
      SecondOrderDifferentialEquations eqn2 = new Equations(i, 0.2);
      FirstOrderConverter eqn1 = new FirstOrderConverter(eqn2);
      assertTrue(eqn1.getDimension() == (2 * eqn2.getDimension()));
    }
  }

// org.apache.commons.math.ode.FirstOrderConverterTest::testDecreasingSteps
  public void testDecreasingSteps()
    throws MathUserException, IntegratorException {

    double previousError = Double.NaN;
    for (int i = 0; i < 10; ++i) {

      double step  = FastMath.pow(2.0, -(i + 1));
      double error = integrateWithSpecifiedStep(4.0, 0.0, 1.0, step)
                   - FastMath.sin(4.0);
      if (i > 0) {
        assertTrue(FastMath.abs(error) < FastMath.abs(previousError));
      }
      previousError = error;

    }
  }

// org.apache.commons.math.ode.FirstOrderConverterTest::testSmallStep
  public void testSmallStep()
    throws MathUserException, IntegratorException {
    double error = integrateWithSpecifiedStep(4.0, 0.0, 1.0, 1.0e-4)
                   - FastMath.sin(4.0);
    assertTrue(FastMath.abs(error) < 1.0e-10);
  }

// org.apache.commons.math.ode.FirstOrderConverterTest::testBigStep
  public void testBigStep()
    throws MathUserException, IntegratorException {
    double error = integrateWithSpecifiedStep(4.0, 0.0, 1.0, 0.5)
                   - FastMath.sin(4.0);
    assertTrue(FastMath.abs(error) > 0.1);
  }

// org.apache.commons.math.ode.events.EventStateTest::closeEvents
    public void closeEvents()
        throws EventException, ConvergenceException, MathUserException {

        final double r1  = 90.0;
        final double r2  = 135.0;
        final double gap = r2 - r1;
        EventHandler closeEventsGenerator = new EventHandler() {
            public void resetState(double t, double[] y) {
            }
            public double g(double t, double[] y) {
                return (t - r1) * (r2 - t);
            }
            public int eventOccurred(double t, double[] y, boolean increasing) {
                return CONTINUE;
            }
        };

        final double tolerance = 0.1;
        EventState es = new EventState(closeEventsGenerator, 1.5 * gap, tolerance, 10);

        AbstractStepInterpolator interpolator =
            new DummyStepInterpolator(new double[0], new double[0], true);
        interpolator.storeTime(r1 - 2.5 * gap);
        interpolator.shift();
        interpolator.storeTime(r1 - 1.5 * gap);
        es.reinitializeBegin(interpolator);

        interpolator.shift();
        interpolator.storeTime(r1 - 0.5 * gap);
        Assert.assertFalse(es.evaluateStep(interpolator));

        interpolator.shift();
        interpolator.storeTime(0.5 * (r1 + r2));
        Assert.assertTrue(es.evaluateStep(interpolator));
        Assert.assertEquals(r1, es.getEventTime(), tolerance);
        es.stepAccepted(es.getEventTime(), new double[0]);

        interpolator.shift();
        interpolator.storeTime(r2 + 0.4 * gap);
        Assert.assertTrue(es.evaluateStep(interpolator));
        Assert.assertEquals(r2, es.getEventTime(), tolerance);

    }

// org.apache.commons.math.ode.nonstiff.AdamsBashforthIntegratorTest::dimensionCheck
    public void dimensionCheck() throws MathUserException, IntegratorException {
        TestProblem1 pb = new TestProblem1();
        FirstOrderIntegrator integ =
            new AdamsBashforthIntegrator(2, 0.0, 1.0, 1.0e-10, 1.0e-10);
        integ.integrate(pb,
                        0.0, new double[pb.getDimension()+10],
                        1.0, new double[pb.getDimension()+10]);
    }

// org.apache.commons.math.ode.nonstiff.AdamsBashforthIntegratorTest::testMinStep
    public void testMinStep() throws MathUserException, IntegratorException {

          TestProblem1 pb = new TestProblem1();
          double minStep = 0.1 * (pb.getFinalTime() - pb.getInitialTime());
          double maxStep = pb.getFinalTime() - pb.getInitialTime();
          double[] vecAbsoluteTolerance = { 1.0e-15, 1.0e-16 };
          double[] vecRelativeTolerance = { 1.0e-15, 1.0e-16 };

          FirstOrderIntegrator integ = new AdamsBashforthIntegrator(4, minStep, maxStep,
                                                                    vecAbsoluteTolerance,
                                                                    vecRelativeTolerance);
          TestProblemHandler handler = new TestProblemHandler(pb, integ);
          integ.addStepHandler(handler);
          integ.integrate(pb,
                          pb.getInitialTime(), pb.getInitialState(),
                          pb.getFinalTime(), new double[pb.getDimension()]);

    }

// org.apache.commons.math.ode.nonstiff.AdamsBashforthIntegratorTest::testIncreasingTolerance
    public void testIncreasingTolerance()
        throws MathUserException, IntegratorException {

        int previousCalls = Integer.MAX_VALUE;
        for (int i = -12; i < -5; ++i) {
            TestProblem1 pb = new TestProblem1();
            double minStep = 0;
            double maxStep = pb.getFinalTime() - pb.getInitialTime();
            double scalAbsoluteTolerance = FastMath.pow(10.0, i);
            double scalRelativeTolerance = 0.01 * scalAbsoluteTolerance;

            FirstOrderIntegrator integ = new AdamsBashforthIntegrator(4, minStep, maxStep,
                                                                      scalAbsoluteTolerance,
                                                                      scalRelativeTolerance);
            TestProblemHandler handler = new TestProblemHandler(pb, integ);
            integ.addStepHandler(handler);
            integ.integrate(pb,
                            pb.getInitialTime(), pb.getInitialState(),
                            pb.getFinalTime(), new double[pb.getDimension()]);

            
            
            
            assertTrue(handler.getMaximalValueError() > (31.0 * scalAbsoluteTolerance));
            assertTrue(handler.getMaximalValueError() < (36.0 * scalAbsoluteTolerance));
            assertEquals(0, handler.getMaximalTimeError(), 1.0e-16);

            int calls = pb.getCalls();
            assertEquals(integ.getEvaluations(), calls);
            assertTrue(calls <= previousCalls);
            previousCalls = calls;

        }

    }

// org.apache.commons.math.ode.nonstiff.AdamsBashforthIntegratorTest::exceedMaxEvaluations
    public void exceedMaxEvaluations() throws MathUserException, IntegratorException {

        TestProblem1 pb  = new TestProblem1();
        double range = pb.getFinalTime() - pb.getInitialTime();

        AdamsBashforthIntegrator integ = new AdamsBashforthIntegrator(2, 0, range, 1.0e-12, 1.0e-12);
        TestProblemHandler handler = new TestProblemHandler(pb, integ);
        integ.addStepHandler(handler);
        integ.setMaxEvaluations(650);
        integ.integrate(pb,
                        pb.getInitialTime(), pb.getInitialState(),
                        pb.getFinalTime(), new double[pb.getDimension()]);

    }

// org.apache.commons.math.ode.nonstiff.AdamsBashforthIntegratorTest::backward
    public void backward() throws MathUserException, IntegratorException {

        TestProblem5 pb = new TestProblem5();
        double range = FastMath.abs(pb.getFinalTime() - pb.getInitialTime());

        FirstOrderIntegrator integ = new AdamsBashforthIntegrator(4, 0, range, 1.0e-12, 1.0e-12);
        TestProblemHandler handler = new TestProblemHandler(pb, integ);
        integ.addStepHandler(handler);
        integ.integrate(pb, pb.getInitialTime(), pb.getInitialState(),
                        pb.getFinalTime(), new double[pb.getDimension()]);

        assertTrue(handler.getLastError() < 1.0e-8);
        assertTrue(handler.getMaximalValueError() < 1.0e-8);
        assertEquals(0, handler.getMaximalTimeError(), 1.0e-16);
        assertEquals("Adams-Bashforth", integ.getName());
    }

// org.apache.commons.math.ode.nonstiff.AdamsBashforthIntegratorTest::polynomial
    public void polynomial() throws MathUserException, IntegratorException {
        TestProblem6 pb = new TestProblem6();
        double range = FastMath.abs(pb.getFinalTime() - pb.getInitialTime());

        for (int nSteps = 1; nSteps < 8; ++nSteps) {
            AdamsBashforthIntegrator integ =
                new AdamsBashforthIntegrator(nSteps, 1.0e-6 * range, 0.1 * range, 1.0e-10, 1.0e-10);
            TestProblemHandler handler = new TestProblemHandler(pb, integ);
            integ.addStepHandler(handler);
            integ.integrate(pb, pb.getInitialTime(), pb.getInitialState(),
                            pb.getFinalTime(), new double[pb.getDimension()]);
            if (nSteps < 4) {
                assertTrue(integ.getEvaluations() > 160);
            } else {
                assertTrue(integ.getEvaluations() < 80);
            }
        }

    }

// org.apache.commons.math.ode.nonstiff.AdamsMoultonIntegratorTest::dimensionCheck
    public void dimensionCheck() throws MathUserException, IntegratorException {
        TestProblem1 pb = new TestProblem1();
        FirstOrderIntegrator integ =
            new AdamsMoultonIntegrator(2, 0.0, 1.0, 1.0e-10, 1.0e-10);
        integ.integrate(pb,
                        0.0, new double[pb.getDimension()+10],
                        1.0, new double[pb.getDimension()+10]);
    }

// org.apache.commons.math.ode.nonstiff.AdamsMoultonIntegratorTest::testMinStep
    public void testMinStep() throws MathUserException, IntegratorException {

          TestProblem1 pb = new TestProblem1();
          double minStep = 0.1 * (pb.getFinalTime() - pb.getInitialTime());
          double maxStep = pb.getFinalTime() - pb.getInitialTime();
          double[] vecAbsoluteTolerance = { 1.0e-15, 1.0e-16 };
          double[] vecRelativeTolerance = { 1.0e-15, 1.0e-16 };

          FirstOrderIntegrator integ = new AdamsMoultonIntegrator(4, minStep, maxStep,
                                                                  vecAbsoluteTolerance,
                                                                  vecRelativeTolerance);
          TestProblemHandler handler = new TestProblemHandler(pb, integ);
          integ.addStepHandler(handler);
          integ.integrate(pb,
                          pb.getInitialTime(), pb.getInitialState(),
                          pb.getFinalTime(), new double[pb.getDimension()]);

    }

// org.apache.commons.math.ode.nonstiff.AdamsMoultonIntegratorTest::testIncreasingTolerance
    public void testIncreasingTolerance()
        throws MathUserException, IntegratorException {

        int previousCalls = Integer.MAX_VALUE;
        for (int i = -12; i < -2; ++i) {
            TestProblem1 pb = new TestProblem1();
            double minStep = 0;
            double maxStep = pb.getFinalTime() - pb.getInitialTime();
            double scalAbsoluteTolerance = FastMath.pow(10.0, i);
            double scalRelativeTolerance = 0.01 * scalAbsoluteTolerance;

            FirstOrderIntegrator integ = new AdamsMoultonIntegrator(4, minStep, maxStep,
                                                                    scalAbsoluteTolerance,
                                                                    scalRelativeTolerance);
            TestProblemHandler handler = new TestProblemHandler(pb, integ);
            integ.addStepHandler(handler);
            integ.integrate(pb,
                            pb.getInitialTime(), pb.getInitialState(),
                            pb.getFinalTime(), new double[pb.getDimension()]);

            
            
            
            assertTrue(handler.getMaximalValueError() > (0.15 * scalAbsoluteTolerance));
            assertTrue(handler.getMaximalValueError() < (3.0 * scalAbsoluteTolerance));
            assertEquals(0, handler.getMaximalTimeError(), 1.0e-16);

            int calls = pb.getCalls();
            assertEquals(integ.getEvaluations(), calls);
            assertTrue(calls <= previousCalls);
            previousCalls = calls;

        }

    }

// org.apache.commons.math.ode.nonstiff.AdamsMoultonIntegratorTest::exceedMaxEvaluations
    public void exceedMaxEvaluations() throws MathUserException, IntegratorException {

        TestProblem1 pb  = new TestProblem1();
        double range = pb.getFinalTime() - pb.getInitialTime();

        AdamsMoultonIntegrator integ = new AdamsMoultonIntegrator(2, 0, range, 1.0e-12, 1.0e-12);
        TestProblemHandler handler = new TestProblemHandler(pb, integ);
        integ.addStepHandler(handler);
        integ.setMaxEvaluations(650);
        integ.integrate(pb,
                        pb.getInitialTime(), pb.getInitialState(),
                        pb.getFinalTime(), new double[pb.getDimension()]);

    }

// org.apache.commons.math.ode.nonstiff.AdamsMoultonIntegratorTest::backward
    public void backward() throws MathUserException, IntegratorException {

        TestProblem5 pb = new TestProblem5();
        double range = FastMath.abs(pb.getFinalTime() - pb.getInitialTime());

        FirstOrderIntegrator integ = new AdamsMoultonIntegrator(4, 0, range, 1.0e-12, 1.0e-12);
        TestProblemHandler handler = new TestProblemHandler(pb, integ);
        integ.addStepHandler(handler);
        integ.integrate(pb, pb.getInitialTime(), pb.getInitialState(),
                        pb.getFinalTime(), new double[pb.getDimension()]);

        assertTrue(handler.getLastError() < 1.0e-9);
        assertTrue(handler.getMaximalValueError() < 1.0e-9);
        assertEquals(0, handler.getMaximalTimeError(), 1.0e-16);
        assertEquals("Adams-Moulton", integ.getName());
    }

// org.apache.commons.math.ode.nonstiff.AdamsMoultonIntegratorTest::polynomial
    public void polynomial() throws MathUserException, IntegratorException {
        TestProblem6 pb = new TestProblem6();
        double range = FastMath.abs(pb.getFinalTime() - pb.getInitialTime());

        for (int nSteps = 1; nSteps < 7; ++nSteps) {
            AdamsMoultonIntegrator integ =
                new AdamsMoultonIntegrator(nSteps, 1.0e-6 * range, 0.1 * range, 1.0e-9, 1.0e-9);
            TestProblemHandler handler = new TestProblemHandler(pb, integ);
            integ.addStepHandler(handler);
            integ.integrate(pb, pb.getInitialTime(), pb.getInitialState(),
                            pb.getFinalTime(), new double[pb.getDimension()]);
            if (nSteps < 4) {
                assertTrue(integ.getEvaluations() > 140);
            } else {
                assertTrue(integ.getEvaluations() < 90);
            }
        }

    }

// org.apache.commons.math.ode.nonstiff.ClassicalRungeKuttaIntegratorTest::testMissedEndEvent
  public void testMissedEndEvent() throws IntegratorException, MathUserException {
      final double   t0     = 1878250320.0000029;
      final double   tEvent = 1878250379.9999986;
      final double[] k      = { 1.0e-4, 1.0e-5, 1.0e-6 };
      FirstOrderDifferentialEquations ode = new FirstOrderDifferentialEquations() {

          public int getDimension() {
              return k.length;
          }

          public void computeDerivatives(double t, double[] y, double[] yDot) {
              for (int i = 0; i < y.length; ++i) {
                  yDot[i] = k[i] * y[i];
              }
          }
      };

      ClassicalRungeKuttaIntegrator integrator = new ClassicalRungeKuttaIntegrator(60.0);

      double[] y0   = new double[k.length];
      for (int i = 0; i < y0.length; ++i) {
          y0[i] = i + 1;
      }
      double[] y    = new double[k.length];

      double finalT = integrator.integrate(ode, t0, y0, tEvent, y);
      Assert.assertEquals(tEvent, finalT, 5.0e-6);
      for (int i = 0; i < y.length; ++i) {
          Assert.assertEquals(y0[i] * FastMath.exp(k[i] * (finalT - t0)), y[i], 1.0e-9);
      }

      integrator.addEventHandler(new EventHandler() {

          public void resetState(double t, double[] y) {
          }

          public double g(double t, double[] y) {
              return t - tEvent;
          }

          public int eventOccurred(double t, double[] y, boolean increasing) {
              Assert.assertEquals(tEvent, t, 5.0e-6);
              return CONTINUE;
          }
      }, Double.POSITIVE_INFINITY, 1.0e-20, 100);
      finalT = integrator.integrate(ode, t0, y0, tEvent + 120, y);
      Assert.assertEquals(tEvent + 120, finalT, 5.0e-6);
      for (int i = 0; i < y.length; ++i) {
          Assert.assertEquals(y0[i] * FastMath.exp(k[i] * (finalT - t0)), y[i], 1.0e-9);
      }

  }

// org.apache.commons.math.ode.nonstiff.ClassicalRungeKuttaIntegratorTest::testSanityChecks
  public void testSanityChecks() {
    try  {
      TestProblem1 pb = new TestProblem1();
      new ClassicalRungeKuttaIntegrator(0.01).integrate(pb,
                                                        0.0, new double[pb.getDimension()+10],
                                                        1.0, new double[pb.getDimension()]);
        fail("an exception should have been thrown");
    } catch(MathUserException de) {
      fail("wrong exception caught");
    } catch(IntegratorException ie) {
    }
    try  {
        TestProblem1 pb = new TestProblem1();
        new ClassicalRungeKuttaIntegrator(0.01).integrate(pb,
                                                          0.0, new double[pb.getDimension()],
                                                          1.0, new double[pb.getDimension()+10]);
          fail("an exception should have been thrown");
      } catch(MathUserException de) {
        fail("wrong exception caught");
      } catch(IntegratorException ie) {
      }
    try  {
      TestProblem1 pb = new TestProblem1();
      new ClassicalRungeKuttaIntegrator(0.01).integrate(pb,
                                                        0.0, new double[pb.getDimension()],
                                                        0.0, new double[pb.getDimension()]);
        fail("an exception should have been thrown");
    } catch(MathUserException de) {
      fail("wrong exception caught");
    } catch(IntegratorException ie) {
    }
  }

// org.apache.commons.math.ode.nonstiff.ClassicalRungeKuttaIntegratorTest::testDecreasingSteps
  public void testDecreasingSteps()
    throws MathUserException, IntegratorException  {

    TestProblemAbstract[] problems = TestProblemFactory.getProblems();
    for (int k = 0; k < problems.length; ++k) {

      double previousError = Double.NaN;
      for (int i = 4; i < 10; ++i) {

        TestProblemAbstract pb = problems[k].copy();
        double step = (pb.getFinalTime() - pb.getInitialTime()) * FastMath.pow(2.0, -i);

        FirstOrderIntegrator integ = new ClassicalRungeKuttaIntegrator(step);
        TestProblemHandler handler = new TestProblemHandler(pb, integ);
        integ.addStepHandler(handler);
        EventHandler[] functions = pb.getEventsHandlers();
        for (int l = 0; l < functions.length; ++l) {
          integ.addEventHandler(functions[l],
                                     Double.POSITIVE_INFINITY, 1.0e-6 * step, 1000);
        }
        assertEquals(functions.length, integ.getEventHandlers().size());
        double stopTime = integ.integrate(pb, pb.getInitialTime(), pb.getInitialState(),
                                          pb.getFinalTime(), new double[pb.getDimension()]);
        if (functions.length == 0) {
            assertEquals(pb.getFinalTime(), stopTime, 1.0e-10);
        }

        double error = handler.getMaximalValueError();
        if (i > 4) {
          assertTrue(error < FastMath.abs(previousError));
        }
        previousError = error;
        assertEquals(0, handler.getMaximalTimeError(), 1.0e-12);
        integ.clearEventHandlers();
        assertEquals(0, integ.getEventHandlers().size());
      }

    }

  }

// org.apache.commons.math.ode.nonstiff.ClassicalRungeKuttaIntegratorTest::testSmallStep
  public void testSmallStep()
    throws MathUserException, IntegratorException {

    TestProblem1 pb = new TestProblem1();
    double step = (pb.getFinalTime() - pb.getInitialTime()) * 0.001;

    FirstOrderIntegrator integ = new ClassicalRungeKuttaIntegrator(step);
    TestProblemHandler handler = new TestProblemHandler(pb, integ);
    integ.addStepHandler(handler);
    integ.integrate(pb, pb.getInitialTime(), pb.getInitialState(),
                    pb.getFinalTime(), new double[pb.getDimension()]);

    assertTrue(handler.getLastError() < 2.0e-13);
    assertTrue(handler.getMaximalValueError() < 4.0e-12);
    assertEquals(0, handler.getMaximalTimeError(), 1.0e-12);
    assertEquals("classical Runge-Kutta", integ.getName());
  }

// org.apache.commons.math.ode.nonstiff.ClassicalRungeKuttaIntegratorTest::testBigStep
  public void testBigStep()
    throws MathUserException, IntegratorException {

    TestProblem1 pb = new TestProblem1();
    double step = (pb.getFinalTime() - pb.getInitialTime()) * 0.2;

    FirstOrderIntegrator integ = new ClassicalRungeKuttaIntegrator(step);
    TestProblemHandler handler = new TestProblemHandler(pb, integ);
    integ.addStepHandler(handler);
    integ.integrate(pb, pb.getInitialTime(), pb.getInitialState(),
                    pb.getFinalTime(), new double[pb.getDimension()]);

    assertTrue(handler.getLastError() > 0.0004);
    assertTrue(handler.getMaximalValueError() > 0.005);
    assertEquals(0, handler.getMaximalTimeError(), 1.0e-12);

  }

// org.apache.commons.math.ode.nonstiff.ClassicalRungeKuttaIntegratorTest::testBackward
  public void testBackward()
    throws MathUserException, IntegratorException {

    TestProblem5 pb = new TestProblem5();
    double step = FastMath.abs(pb.getFinalTime() - pb.getInitialTime()) * 0.001;

    FirstOrderIntegrator integ = new ClassicalRungeKuttaIntegrator(step);
    TestProblemHandler handler = new TestProblemHandler(pb, integ);
    integ.addStepHandler(handler);
    integ.integrate(pb, pb.getInitialTime(), pb.getInitialState(),
                    pb.getFinalTime(), new double[pb.getDimension()]);

    assertTrue(handler.getLastError() < 5.0e-10);
    assertTrue(handler.getMaximalValueError() < 7.0e-10);
    assertEquals(0, handler.getMaximalTimeError(), 1.0e-12);
    assertEquals("classical Runge-Kutta", integ.getName());
  }

// org.apache.commons.math.ode.nonstiff.ClassicalRungeKuttaIntegratorTest::testKepler
  public void testKepler()
    throws MathUserException, IntegratorException {

    final TestProblem3 pb  = new TestProblem3(0.9);
    double step = (pb.getFinalTime() - pb.getInitialTime()) * 0.0003;

    FirstOrderIntegrator integ = new ClassicalRungeKuttaIntegrator(step);
    integ.addStepHandler(new KeplerHandler(pb));
    integ.integrate(pb,
                    pb.getInitialTime(), pb.getInitialState(),
                    pb.getFinalTime(), new double[pb.getDimension()]);
  }

// org.apache.commons.math.ode.nonstiff.ClassicalRungeKuttaIntegratorTest::testStepSize
  public void testStepSize()
    throws MathUserException, IntegratorException {
      final double step = 1.23456;
      FirstOrderIntegrator integ = new ClassicalRungeKuttaIntegrator(step);
      integ.addStepHandler(new StepHandler() {
          public void handleStep(StepInterpolator interpolator, boolean isLast) {
              if (! isLast) {
                  assertEquals(step,
                               interpolator.getCurrentTime() - interpolator.getPreviousTime(),
                               1.0e-12);
              }
          }
          public boolean requiresDenseOutput() {
              return false;
          }
          public void reset() {
          }
      });
      integ.integrate(new FirstOrderDifferentialEquations() {
          private static final long serialVersionUID = 0L;
          public void computeDerivatives(double t, double[] y, double[] dot) {
              dot[0] = 1.0;
          }
          public int getDimension() {
              return 1;
          }
      }, 0.0, new double[] { 0.0 }, 5.0, new double[1]);
  }

// org.apache.commons.math.ode.nonstiff.ClassicalRungeKuttaStepInterpolatorTest::derivativesConsistency
  public void derivativesConsistency()
  throws MathUserException, IntegratorException {
    TestProblem3 pb = new TestProblem3();
    double step = (pb.getFinalTime() - pb.getInitialTime()) * 0.001;
    ClassicalRungeKuttaIntegrator integ = new ClassicalRungeKuttaIntegrator(step);
    StepInterpolatorTestUtils.checkDerivativesConsistency(integ, pb, 1.0e-10);
  }

// org.apache.commons.math.ode.nonstiff.ClassicalRungeKuttaStepInterpolatorTest::serialization
  public void serialization()
    throws MathUserException, IntegratorException,
           IOException, ClassNotFoundException {

    TestProblem3 pb = new TestProblem3(0.9);
    double step = (pb.getFinalTime() - pb.getInitialTime()) * 0.0003;
    ClassicalRungeKuttaIntegrator integ = new ClassicalRungeKuttaIntegrator(step);
    integ.addStepHandler(new ContinuousOutputModel());
    integ.integrate(pb,
                    pb.getInitialTime(), pb.getInitialState(),
                    pb.getFinalTime(), new double[pb.getDimension()]);

    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    ObjectOutputStream    oos = new ObjectOutputStream(bos);
    for (StepHandler handler : integ.getStepHandlers()) {
        oos.writeObject(handler);
    }

    assertTrue(bos.size () > 700000);
    assertTrue(bos.size () < 701000);

    ByteArrayInputStream  bis = new ByteArrayInputStream(bos.toByteArray());
    ObjectInputStream     ois = new ObjectInputStream(bis);
    ContinuousOutputModel cm  = (ContinuousOutputModel) ois.readObject();

    Random random = new Random(347588535632l);
    double maxError = 0.0;
    for (int i = 0; i < 1000; ++i) {
      double r = random.nextDouble();
      double time = r * pb.getInitialTime() + (1.0 - r) * pb.getFinalTime();
      cm.setInterpolatedTime(time);
      double[] interpolatedY = cm.getInterpolatedState ();
      double[] theoreticalY  = pb.computeTheoreticalState(time);
      double dx = interpolatedY[0] - theoreticalY[0];
      double dy = interpolatedY[1] - theoreticalY[1];
      double error = dx * dx + dy * dy;
      if (error > maxError) {
        maxError = error;
      }
    }

    assertTrue(maxError > 0.005);

  }

// org.apache.commons.math.ode.nonstiff.DormandPrince54IntegratorTest::testDimensionCheck
  public void testDimensionCheck() {
    try  {
      TestProblem1 pb = new TestProblem1();
      DormandPrince54Integrator integrator = new DormandPrince54Integrator(0.0, 1.0,
                                                                           1.0e-10, 1.0e-10);
      integrator.integrate(pb,
                           0.0, new double[pb.getDimension()+10],
                           1.0, new double[pb.getDimension()+10]);
      fail("an exception should have been thrown");
    } catch(MathUserException de) {
      fail("wrong exception caught");
    } catch(IntegratorException ie) {
    }
  }

// org.apache.commons.math.ode.nonstiff.DormandPrince54IntegratorTest::testMinStep
  public void testMinStep() {

    try {
      TestProblem1 pb = new TestProblem1();
      double minStep = 0.1 * (pb.getFinalTime() - pb.getInitialTime());
      double maxStep = pb.getFinalTime() - pb.getInitialTime();
      double[] vecAbsoluteTolerance = { 1.0e-15, 1.0e-16 };
      double[] vecRelativeTolerance = { 1.0e-15, 1.0e-16 };

      FirstOrderIntegrator integ = new DormandPrince54Integrator(minStep, maxStep,
                                                                 vecAbsoluteTolerance,
                                                                 vecRelativeTolerance);
      TestProblemHandler handler = new TestProblemHandler(pb, integ);
      integ.addStepHandler(handler);
      integ.integrate(pb,
                      pb.getInitialTime(), pb.getInitialState(),
                      pb.getFinalTime(), new double[pb.getDimension()]);
      fail("an exception should have been thrown");
    } catch(MathUserException de) {
      fail("wrong exception caught");
    } catch(IntegratorException ie) {
    }

  }

// org.apache.commons.math.ode.nonstiff.DormandPrince54IntegratorTest::testSmallLastStep
  public void testSmallLastStep()
    throws MathUserException, IntegratorException {

    TestProblemAbstract pb = new TestProblem5();
    double minStep = 1.25;
    double maxStep = FastMath.abs(pb.getFinalTime() - pb.getInitialTime());
    double scalAbsoluteTolerance = 6.0e-4;
    double scalRelativeTolerance = 6.0e-4;

    AdaptiveStepsizeIntegrator integ =
      new DormandPrince54Integrator(minStep, maxStep,
                                    scalAbsoluteTolerance,
                                    scalRelativeTolerance);

    DP54SmallLastHandler handler = new DP54SmallLastHandler(minStep);
    integ.addStepHandler(handler);
    integ.setInitialStepSize(1.7);
    integ.integrate(pb,
                    pb.getInitialTime(), pb.getInitialState(),
                    pb.getFinalTime(), new double[pb.getDimension()]);
    assertTrue(handler.wasLastSeen());
    assertEquals("Dormand-Prince 5(4)", integ.getName());

  }

// org.apache.commons.math.ode.nonstiff.DormandPrince54IntegratorTest::testBackward
  public void testBackward()
      throws MathUserException, IntegratorException {

      TestProblem5 pb = new TestProblem5();
      double minStep = 0;
      double maxStep = pb.getFinalTime() - pb.getInitialTime();
      double scalAbsoluteTolerance = 1.0e-8;
      double scalRelativeTolerance = 0.01 * scalAbsoluteTolerance;

      FirstOrderIntegrator integ = new DormandPrince54Integrator(minStep, maxStep,
                                                                 scalAbsoluteTolerance,
                                                                 scalRelativeTolerance);
      TestProblemHandler handler = new TestProblemHandler(pb, integ);
      integ.addStepHandler(handler);
      integ.integrate(pb, pb.getInitialTime(), pb.getInitialState(),
                      pb.getFinalTime(), new double[pb.getDimension()]);

      assertTrue(handler.getLastError() < 2.0e-7);
      assertTrue(handler.getMaximalValueError() < 2.0e-7);
      assertEquals(0, handler.getMaximalTimeError(), 1.0e-12);
      assertEquals("Dormand-Prince 5(4)", integ.getName());
  }

// org.apache.commons.math.ode.nonstiff.DormandPrince54IntegratorTest::testIncreasingTolerance
  public void testIncreasingTolerance()
    throws MathUserException, IntegratorException {

    int previousCalls = Integer.MAX_VALUE;
    for (int i = -12; i < -2; ++i) {
      TestProblem1 pb = new TestProblem1();
      double minStep = 0;
      double maxStep = pb.getFinalTime() - pb.getInitialTime();
      double scalAbsoluteTolerance = FastMath.pow(10.0, i);
      double scalRelativeTolerance = 0.01 * scalAbsoluteTolerance;

      EmbeddedRungeKuttaIntegrator integ =
          new DormandPrince54Integrator(minStep, maxStep,
                                        scalAbsoluteTolerance, scalRelativeTolerance);
      TestProblemHandler handler = new TestProblemHandler(pb, integ);
      integ.setSafety(0.8);
      integ.setMaxGrowth(5.0);
      integ.setMinReduction(0.3);
      integ.addStepHandler(handler);
      integ.integrate(pb,
                      pb.getInitialTime(), pb.getInitialState(),
                      pb.getFinalTime(), new double[pb.getDimension()]);
      assertEquals(0.8, integ.getSafety(), 1.0e-12);
      assertEquals(5.0, integ.getMaxGrowth(), 1.0e-12);
      assertEquals(0.3, integ.getMinReduction(), 1.0e-12);

      
      
      
      assertTrue(handler.getMaximalValueError() < (0.7 * scalAbsoluteTolerance));
      assertEquals(0, handler.getMaximalTimeError(), 1.0e-12);

      int calls = pb.getCalls();
      assertEquals(integ.getEvaluations(), calls);
      assertTrue(calls <= previousCalls);
      previousCalls = calls;

    }

  }

// org.apache.commons.math.ode.nonstiff.DormandPrince54IntegratorTest::testEvents
  public void testEvents()
    throws MathUserException, IntegratorException {

    TestProblem4 pb = new TestProblem4();
    double minStep = 0;
    double maxStep = pb.getFinalTime() - pb.getInitialTime();
    double scalAbsoluteTolerance = 1.0e-8;
    double scalRelativeTolerance = 0.01 * scalAbsoluteTolerance;

    FirstOrderIntegrator integ = new DormandPrince54Integrator(minStep, maxStep,
                                                               scalAbsoluteTolerance,
                                                               scalRelativeTolerance);
    TestProblemHandler handler = new TestProblemHandler(pb, integ);
    integ.addStepHandler(handler);
    EventHandler[] functions = pb.getEventsHandlers();
    for (int l = 0; l < functions.length; ++l) {
      integ.addEventHandler(functions[l],
                                 Double.POSITIVE_INFINITY, 1.0e-8 * maxStep, 1000);
    }
    assertEquals(functions.length, integ.getEventHandlers().size());
    integ.integrate(pb,
                    pb.getInitialTime(), pb.getInitialState(),
                    pb.getFinalTime(), new double[pb.getDimension()]);

    assertTrue(handler.getMaximalValueError() < 5.0e-6);
    assertEquals(0, handler.getMaximalTimeError(), 1.0e-12);
    assertEquals(12.0, handler.getLastTime(), 1.0e-8 * maxStep);
    integ.clearEventHandlers();
    assertEquals(0, integ.getEventHandlers().size());

  }

// org.apache.commons.math.ode.nonstiff.DormandPrince54IntegratorTest::testKepler
  public void testKepler()
    throws MathUserException, IntegratorException {

    final TestProblem3 pb  = new TestProblem3(0.9);
    double minStep = 0;
    double maxStep = pb.getFinalTime() - pb.getInitialTime();
    double scalAbsoluteTolerance = 1.0e-8;
    double scalRelativeTolerance = scalAbsoluteTolerance;

    FirstOrderIntegrator integ = new DormandPrince54Integrator(minStep, maxStep,
                                                               scalAbsoluteTolerance,
                                                               scalRelativeTolerance);
    integ.addStepHandler(new KeplerHandler(pb));
    integ.integrate(pb,
                    pb.getInitialTime(), pb.getInitialState(),
                    pb.getFinalTime(), new double[pb.getDimension()]);

    assertEquals(integ.getEvaluations(), pb.getCalls());
    assertTrue(pb.getCalls() < 2800);

  }

// org.apache.commons.math.ode.nonstiff.DormandPrince54IntegratorTest::testVariableSteps
  public void testVariableSteps()
    throws MathUserException, IntegratorException {

    final TestProblem3 pb  = new TestProblem3(0.9);
    double minStep = 0;
    double maxStep = pb.getFinalTime() - pb.getInitialTime();
    double scalAbsoluteTolerance = 1.0e-8;
    double scalRelativeTolerance = scalAbsoluteTolerance;

    FirstOrderIntegrator integ = new DormandPrince54Integrator(minStep, maxStep,
                                                               scalAbsoluteTolerance,
                                                               scalRelativeTolerance);
    integ.addStepHandler(new VariableHandler());
    double stopTime = integ.integrate(pb, pb.getInitialTime(), pb.getInitialState(),
                                      pb.getFinalTime(), new double[pb.getDimension()]);
    assertEquals(pb.getFinalTime(), stopTime, 1.0e-10);
  }

// org.apache.commons.math.ode.nonstiff.DormandPrince54StepInterpolatorTest::derivativesConsistency
  public void derivativesConsistency()
  throws MathUserException, IntegratorException {
    TestProblem3 pb = new TestProblem3(0.1);
    double minStep = 0;
    double maxStep = pb.getFinalTime() - pb.getInitialTime();
    double scalAbsoluteTolerance = 1.0e-8;
    double scalRelativeTolerance = scalAbsoluteTolerance;
    DormandPrince54Integrator integ = new DormandPrince54Integrator(minStep, maxStep,
                                                                    scalAbsoluteTolerance,
                                                                    scalRelativeTolerance);
    StepInterpolatorTestUtils.checkDerivativesConsistency(integ, pb, 1.0e-10);
  }

// org.apache.commons.math.ode.nonstiff.DormandPrince54StepInterpolatorTest::serialization
  public void serialization()
    throws MathUserException, IntegratorException,
           IOException, ClassNotFoundException {

    TestProblem3 pb = new TestProblem3(0.9);
    double minStep = 0;
    double maxStep = pb.getFinalTime() - pb.getInitialTime();
    double scalAbsoluteTolerance = 1.0e-8;
    double scalRelativeTolerance = scalAbsoluteTolerance;
    DormandPrince54Integrator integ = new DormandPrince54Integrator(minStep, maxStep,
                                                                    scalAbsoluteTolerance,
                                                                    scalRelativeTolerance);
    integ.addStepHandler(new ContinuousOutputModel());
    integ.integrate(pb,
                    pb.getInitialTime(), pb.getInitialState(),
                    pb.getFinalTime(), new double[pb.getDimension()]);

    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    ObjectOutputStream    oos = new ObjectOutputStream(bos);
    for (StepHandler handler : integ.getStepHandlers()) {
        oos.writeObject(handler);
    }

    assertTrue(bos.size () > 119500);
    assertTrue(bos.size () < 120500);

    ByteArrayInputStream  bis = new ByteArrayInputStream(bos.toByteArray());
    ObjectInputStream     ois = new ObjectInputStream(bis);
    ContinuousOutputModel cm  = (ContinuousOutputModel) ois.readObject();

    Random random = new Random(347588535632l);
    double maxError = 0.0;
    for (int i = 0; i < 1000; ++i) {
      double r = random.nextDouble();
      double time = r * pb.getInitialTime() + (1.0 - r) * pb.getFinalTime();
      cm.setInterpolatedTime(time);
      double[] interpolatedY = cm.getInterpolatedState ();
      double[] theoreticalY  = pb.computeTheoreticalState(time);
      double dx = interpolatedY[0] - theoreticalY[0];
      double dy = interpolatedY[1] - theoreticalY[1];
      double error = dx * dx + dy * dy;
      if (error > maxError) {
        maxError = error;
      }
    }

    assertTrue(maxError < 7.0e-10);

  }

// org.apache.commons.math.ode.nonstiff.DormandPrince54StepInterpolatorTest::checkClone
  public void checkClone()
    throws MathUserException, IntegratorException {
      TestProblem3 pb = new TestProblem3(0.9);
      double minStep = 0;
      double maxStep = pb.getFinalTime() - pb.getInitialTime();
      double scalAbsoluteTolerance = 1.0e-8;
      double scalRelativeTolerance = scalAbsoluteTolerance;
      DormandPrince54Integrator integ = new DormandPrince54Integrator(minStep, maxStep,
                                                                      scalAbsoluteTolerance,
                                                                      scalRelativeTolerance);
      integ.addStepHandler(new StepHandler() {
        public void handleStep(StepInterpolator interpolator, boolean isLast)
          throws MathUserException {
              StepInterpolator cloned = interpolator.copy();
              double tA = cloned.getPreviousTime();
              double tB = cloned.getCurrentTime();
              double halfStep = FastMath.abs(tB - tA) / 2;
              assertEquals(interpolator.getPreviousTime(), tA, 1.0e-12);
              assertEquals(interpolator.getCurrentTime(), tB, 1.0e-12);
              for (int i = 0; i < 10; ++i) {
                  double t = (i * tB + (9 - i) * tA) / 9;
                  interpolator.setInterpolatedTime(t);
                  assertTrue(FastMath.abs(cloned.getInterpolatedTime() - t) > (halfStep / 10));
                  cloned.setInterpolatedTime(t);
                  assertEquals(t, cloned.getInterpolatedTime(), 1.0e-12);
                  double[] referenceState = interpolator.getInterpolatedState();
                  double[] cloneState     = cloned.getInterpolatedState();
                  for (int j = 0; j < referenceState.length; ++j) {
                      assertEquals(referenceState[j], cloneState[j], 1.0e-12);
                  }
              }
          }
          public boolean requiresDenseOutput() {
              return true;
          }
          public void reset() {
          }
      });
      integ.integrate(pb,
              pb.getInitialTime(), pb.getInitialState(),
              pb.getFinalTime(), new double[pb.getDimension()]);

  }

// org.apache.commons.math.ode.nonstiff.DormandPrince853IntegratorTest::testMissedEndEvent
  public void testMissedEndEvent() throws IntegratorException, MathUserException {
      final double   t0     = 1878250320.0000029;
      final double   tEvent = 1878250379.9999986;
      final double[] k  = { 1.0e-4, 1.0e-5, 1.0e-6 };
      FirstOrderDifferentialEquations ode = new FirstOrderDifferentialEquations() {

          public int getDimension() {
              return k.length;
          }

          public void computeDerivatives(double t, double[] y, double[] yDot) {
              for (int i = 0; i < y.length; ++i) {
                  yDot[i] = k[i] * y[i];
              }
          }
      };

      DormandPrince853Integrator integrator = new DormandPrince853Integrator(0.0, 100.0,
                                                                             1.0e-10, 1.0e-10);

      double[] y0   = new double[k.length];
      for (int i = 0; i < y0.length; ++i) {
          y0[i] = i + 1;
      }
      double[] y    = new double[k.length];

      integrator.setInitialStepSize(60.0);
      double finalT = integrator.integrate(ode, t0, y0, tEvent, y);
      Assert.assertEquals(tEvent, finalT, 5.0e-6);
      for (int i = 0; i < y.length; ++i) {
          Assert.assertEquals(y0[i] * FastMath.exp(k[i] * (finalT - t0)), y[i], 1.0e-9);
      }

      integrator.setInitialStepSize(60.0);
      integrator.addEventHandler(new EventHandler() {

          public void resetState(double t, double[] y) {
          }

          public double g(double t, double[] y) {
              return t - tEvent;
          }

          public int eventOccurred(double t, double[] y, boolean increasing) {
              Assert.assertEquals(tEvent, t, 5.0e-6);
              return CONTINUE;
          }
      }, Double.POSITIVE_INFINITY, 1.0e-20, 100);
      finalT = integrator.integrate(ode, t0, y0, tEvent + 120, y);
      Assert.assertEquals(tEvent + 120, finalT, 5.0e-6);
      for (int i = 0; i < y.length; ++i) {
          Assert.assertEquals(y0[i] * FastMath.exp(k[i] * (finalT - t0)), y[i], 1.0e-9);
      }

  }

// org.apache.commons.math.ode.nonstiff.DormandPrince853IntegratorTest::testDimensionCheck
  public void testDimensionCheck() {
    try  {
      TestProblem1 pb = new TestProblem1();
      DormandPrince853Integrator integrator = new DormandPrince853Integrator(0.0, 1.0,
                                                                             1.0e-10, 1.0e-10);
      integrator.integrate(pb,
                           0.0, new double[pb.getDimension()+10],
                           1.0, new double[pb.getDimension()+10]);
      fail("an exception should have been thrown");
    } catch(MathUserException de) {
      fail("wrong exception caught");
    } catch(IntegratorException ie) {
    }
  }

// org.apache.commons.math.ode.nonstiff.DormandPrince853IntegratorTest::testNullIntervalCheck
  public void testNullIntervalCheck() {
    try  {
      TestProblem1 pb = new TestProblem1();
      DormandPrince853Integrator integrator = new DormandPrince853Integrator(0.0, 1.0,
                                                                             1.0e-10, 1.0e-10);
      integrator.integrate(pb,
                           0.0, new double[pb.getDimension()],
                           0.0, new double[pb.getDimension()]);
      fail("an exception should have been thrown");
    } catch(MathUserException de) {
      fail("wrong exception caught");
    } catch(IntegratorException ie) {
    }
  }

// org.apache.commons.math.ode.nonstiff.DormandPrince853IntegratorTest::testMinStep
  public void testMinStep() {

    try {
      TestProblem1 pb = new TestProblem1();
      double minStep = 0.1 * (pb.getFinalTime() - pb.getInitialTime());
      double maxStep = pb.getFinalTime() - pb.getInitialTime();
      double[] vecAbsoluteTolerance = { 1.0e-15, 1.0e-16 };
      double[] vecRelativeTolerance = { 1.0e-15, 1.0e-16 };

      FirstOrderIntegrator integ = new DormandPrince853Integrator(minStep, maxStep,
                                                                  vecAbsoluteTolerance,
                                                                  vecRelativeTolerance);
      TestProblemHandler handler = new TestProblemHandler(pb, integ);
      integ.addStepHandler(handler);
      integ.integrate(pb,
                      pb.getInitialTime(), pb.getInitialState(),
                      pb.getFinalTime(), new double[pb.getDimension()]);
      fail("an exception should have been thrown");
    } catch(MathUserException de) {
      fail("wrong exception caught");
    } catch(IntegratorException ie) {
    }

  }

// org.apache.commons.math.ode.nonstiff.DormandPrince853IntegratorTest::testIncreasingTolerance
  public void testIncreasingTolerance()
    throws MathUserException, IntegratorException {

    int previousCalls = Integer.MAX_VALUE;
    for (int i = -12; i < -2; ++i) {
      TestProblem1 pb = new TestProblem1();
      double minStep = 0;
      double maxStep = pb.getFinalTime() - pb.getInitialTime();
      double scalAbsoluteTolerance = FastMath.pow(10.0, i);
      double scalRelativeTolerance = 0.01 * scalAbsoluteTolerance;

      FirstOrderIntegrator integ = new DormandPrince853Integrator(minStep, maxStep,
                                                                  scalAbsoluteTolerance,
                                                                  scalRelativeTolerance);
      TestProblemHandler handler = new TestProblemHandler(pb, integ);
      integ.addStepHandler(handler);
      integ.integrate(pb,
                      pb.getInitialTime(), pb.getInitialState(),
                      pb.getFinalTime(), new double[pb.getDimension()]);

      
      
      
      assertTrue(handler.getMaximalValueError() < (1.3 * scalAbsoluteTolerance));
      assertEquals(0, handler.getMaximalTimeError(), 1.0e-12);

      int calls = pb.getCalls();
      assertEquals(integ.getEvaluations(), calls);
      assertTrue(calls <= previousCalls);
      previousCalls = calls;

    }

  }

// org.apache.commons.math.ode.nonstiff.DormandPrince853IntegratorTest::testBackward
  public void testBackward()
      throws MathUserException, IntegratorException {

      TestProblem5 pb = new TestProblem5();
      double minStep = 0;
      double maxStep = pb.getFinalTime() - pb.getInitialTime();
      double scalAbsoluteTolerance = 1.0e-8;
      double scalRelativeTolerance = 0.01 * scalAbsoluteTolerance;

      FirstOrderIntegrator integ = new DormandPrince853Integrator(minStep, maxStep,
                                                                  scalAbsoluteTolerance,
                                                                  scalRelativeTolerance);
      TestProblemHandler handler = new TestProblemHandler(pb, integ);
      integ.addStepHandler(handler);
      integ.integrate(pb, pb.getInitialTime(), pb.getInitialState(),
                      pb.getFinalTime(), new double[pb.getDimension()]);

      assertTrue(handler.getLastError() < 8.1e-8);
      assertTrue(handler.getMaximalValueError() < 1.1e-7);
      assertEquals(0, handler.getMaximalTimeError(), 1.0e-12);
      assertEquals("Dormand-Prince 8 (5, 3)", integ.getName());
  }

// org.apache.commons.math.ode.nonstiff.DormandPrince853IntegratorTest::testEvents
  public void testEvents()
    throws MathUserException, IntegratorException {

    TestProblem4 pb = new TestProblem4();
    double minStep = 0;
    double maxStep = pb.getFinalTime() - pb.getInitialTime();
    double scalAbsoluteTolerance = 1.0e-9;
    double scalRelativeTolerance = 0.01 * scalAbsoluteTolerance;

    FirstOrderIntegrator integ = new DormandPrince853Integrator(minStep, maxStep,
                                                                scalAbsoluteTolerance,
                                                                scalRelativeTolerance);
    TestProblemHandler handler = new TestProblemHandler(pb, integ);
    integ.addStepHandler(handler);
    EventHandler[] functions = pb.getEventsHandlers();
    for (int l = 0; l < functions.length; ++l) {
      integ.addEventHandler(functions[l],
                                 Double.POSITIVE_INFINITY, 1.0e-8 * maxStep, 1000);
    }
    assertEquals(functions.length, integ.getEventHandlers().size());
    integ.integrate(pb,
                    pb.getInitialTime(), pb.getInitialState(),
                    pb.getFinalTime(), new double[pb.getDimension()]);

    assertEquals(0, handler.getMaximalValueError(), 1.1e-7);
    assertEquals(0, handler.getMaximalTimeError(), 1.0e-12);
    assertEquals(12.0, handler.getLastTime(), 1.0e-8 * maxStep);
    integ.clearEventHandlers();
    assertEquals(0, integ.getEventHandlers().size());

  }

// org.apache.commons.math.ode.nonstiff.DormandPrince853IntegratorTest::testKepler
  public void testKepler()
    throws MathUserException, IntegratorException {

    final TestProblem3 pb  = new TestProblem3(0.9);
    double minStep = 0;
    double maxStep = pb.getFinalTime() - pb.getInitialTime();
    double scalAbsoluteTolerance = 1.0e-8;
    double scalRelativeTolerance = scalAbsoluteTolerance;

    FirstOrderIntegrator integ = new DormandPrince853Integrator(minStep, maxStep,
                                                                scalAbsoluteTolerance,
                                                                scalRelativeTolerance);
    integ.addStepHandler(new KeplerHandler(pb));
    integ.integrate(pb,
                    pb.getInitialTime(), pb.getInitialState(),
                    pb.getFinalTime(), new double[pb.getDimension()]);

    assertEquals(integ.getEvaluations(), pb.getCalls());
    assertTrue(pb.getCalls() < 3300);

  }

// org.apache.commons.math.ode.nonstiff.DormandPrince853IntegratorTest::testVariableSteps
  public void testVariableSteps()
    throws MathUserException, IntegratorException {

    final TestProblem3 pb  = new TestProblem3(0.9);
    double minStep = 0;
    double maxStep = pb.getFinalTime() - pb.getInitialTime();
    double scalAbsoluteTolerance = 1.0e-8;
    double scalRelativeTolerance = scalAbsoluteTolerance;

    FirstOrderIntegrator integ = new DormandPrince853Integrator(minStep, maxStep,
                                                               scalAbsoluteTolerance,
                                                               scalRelativeTolerance);
    integ.addStepHandler(new VariableHandler());
    double stopTime = integ.integrate(pb,
                                      pb.getInitialTime(), pb.getInitialState(),
                                      pb.getFinalTime(), new double[pb.getDimension()]);
    assertEquals(pb.getFinalTime(), stopTime, 1.0e-10);
    assertEquals("Dormand-Prince 8 (5, 3)", integ.getName());
  }

// org.apache.commons.math.ode.nonstiff.DormandPrince853IntegratorTest::testNoDenseOutput
  public void testNoDenseOutput()
    throws MathUserException, IntegratorException {
    TestProblem1 pb1 = new TestProblem1();
    TestProblem1 pb2 = pb1.copy();
    double minStep = 0.1 * (pb1.getFinalTime() - pb1.getInitialTime());
    double maxStep = pb1.getFinalTime() - pb1.getInitialTime();
    double scalAbsoluteTolerance = 1.0e-4;
    double scalRelativeTolerance = 1.0e-4;

    FirstOrderIntegrator integ = new DormandPrince853Integrator(minStep, maxStep,
                                                                scalAbsoluteTolerance,
                                                                scalRelativeTolerance);
    integ.addStepHandler(DummyStepHandler.getInstance());
    integ.integrate(pb1,
                    pb1.getInitialTime(), pb1.getInitialState(),
                    pb1.getFinalTime(), new double[pb1.getDimension()]);
    int callsWithoutDenseOutput = pb1.getCalls();
    assertEquals(integ.getEvaluations(), callsWithoutDenseOutput);

    integ.addStepHandler(new InterpolatingStepHandler());
    integ.integrate(pb2,
                    pb2.getInitialTime(), pb2.getInitialState(),
                    pb2.getFinalTime(), new double[pb2.getDimension()]);
    int callsWithDenseOutput = pb2.getCalls();
    assertEquals(integ.getEvaluations(), callsWithDenseOutput);

    assertTrue(callsWithDenseOutput > callsWithoutDenseOutput);

  }

// org.apache.commons.math.ode.nonstiff.DormandPrince853IntegratorTest::testUnstableDerivative
  public void testUnstableDerivative()
  throws MathUserException, IntegratorException {
    final StepProblem stepProblem = new StepProblem(0.0, 1.0, 2.0);
    FirstOrderIntegrator integ =
      new DormandPrince853Integrator(0.1, 10, 1.0e-12, 0.0);
    integ.addEventHandler(stepProblem, 1.0, 1.0e-12, 1000);
    double[] y = { Double.NaN };
    integ.integrate(stepProblem, 0.0, new double[] { 0.0 }, 10.0, y);
    assertEquals(8.0, y[0], 1.0e-12);
  }

// org.apache.commons.math.ode.nonstiff.DormandPrince853StepInterpolatorTest::derivativesConsistency
  public void derivativesConsistency()
  throws MathUserException, IntegratorException {
    TestProblem3 pb = new TestProblem3(0.1);
    double minStep = 0;
    double maxStep = pb.getFinalTime() - pb.getInitialTime();
    double scalAbsoluteTolerance = 1.0e-8;
    double scalRelativeTolerance = scalAbsoluteTolerance;
    DormandPrince853Integrator integ = new DormandPrince853Integrator(minStep, maxStep,
                                                                      scalAbsoluteTolerance,
                                                                      scalRelativeTolerance);
    StepInterpolatorTestUtils.checkDerivativesConsistency(integ, pb, 1.0e-10);
  }

// org.apache.commons.math.ode.nonstiff.DormandPrince853StepInterpolatorTest::serialization
  public void serialization()
    throws MathUserException, IntegratorException,
           IOException, ClassNotFoundException {

    TestProblem3 pb = new TestProblem3(0.9);
    double minStep = 0;
    double maxStep = pb.getFinalTime() - pb.getInitialTime();
    double scalAbsoluteTolerance = 1.0e-8;
    double scalRelativeTolerance = scalAbsoluteTolerance;
    DormandPrince853Integrator integ = new DormandPrince853Integrator(minStep, maxStep,
                                                                      scalAbsoluteTolerance,
                                                                      scalRelativeTolerance);
    integ.addStepHandler(new ContinuousOutputModel());
    integ.integrate(pb,
                    pb.getInitialTime(), pb.getInitialState(),
                    pb.getFinalTime(), new double[pb.getDimension()]);

    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    ObjectOutputStream    oos = new ObjectOutputStream(bos);
    for (StepHandler handler : integ.getStepHandlers()) {
        oos.writeObject(handler);
    }

    assertTrue(bos.size () > 86000);
    assertTrue(bos.size () < 87000);

    ByteArrayInputStream  bis = new ByteArrayInputStream(bos.toByteArray());
    ObjectInputStream     ois = new ObjectInputStream(bis);
    ContinuousOutputModel cm  = (ContinuousOutputModel) ois.readObject();

    Random random = new Random(347588535632l);
    double maxError = 0.0;
    for (int i = 0; i < 1000; ++i) {
      double r = random.nextDouble();
      double time = r * pb.getInitialTime() + (1.0 - r) * pb.getFinalTime();
      cm.setInterpolatedTime(time);
      double[] interpolatedY = cm.getInterpolatedState ();
      double[] theoreticalY  = pb.computeTheoreticalState(time);
      double dx = interpolatedY[0] - theoreticalY[0];
      double dy = interpolatedY[1] - theoreticalY[1];
      double error = dx * dx + dy * dy;
      if (error > maxError) {
        maxError = error;
      }
    }

    assertTrue(maxError < 2.4e-10);

  }

// org.apache.commons.math.ode.nonstiff.DormandPrince853StepInterpolatorTest::checklone
  public void checklone()
  throws MathUserException, IntegratorException {
    TestProblem3 pb = new TestProblem3(0.9);
    double minStep = 0;
    double maxStep = pb.getFinalTime() - pb.getInitialTime();
    double scalAbsoluteTolerance = 1.0e-8;
    double scalRelativeTolerance = scalAbsoluteTolerance;
    DormandPrince853Integrator integ = new DormandPrince853Integrator(minStep, maxStep,
                                                                      scalAbsoluteTolerance,
                                                                      scalRelativeTolerance);
    integ.addStepHandler(new StepHandler() {
        public void handleStep(StepInterpolator interpolator, boolean isLast)
        throws MathUserException {
            StepInterpolator cloned = interpolator.copy();
            double tA = cloned.getPreviousTime();
            double tB = cloned.getCurrentTime();
            double halfStep = FastMath.abs(tB - tA) / 2;
            assertEquals(interpolator.getPreviousTime(), tA, 1.0e-12);
            assertEquals(interpolator.getCurrentTime(), tB, 1.0e-12);
            for (int i = 0; i < 10; ++i) {
                double t = (i * tB + (9 - i) * tA) / 9;
                interpolator.setInterpolatedTime(t);
                assertTrue(FastMath.abs(cloned.getInterpolatedTime() - t) > (halfStep / 10));
                cloned.setInterpolatedTime(t);
                assertEquals(t, cloned.getInterpolatedTime(), 1.0e-12);
                double[] referenceState = interpolator.getInterpolatedState();
                double[] cloneState     = cloned.getInterpolatedState();
                for (int j = 0; j < referenceState.length; ++j) {
                    assertEquals(referenceState[j], cloneState[j], 1.0e-12);
                }
            }
        }
        public boolean requiresDenseOutput() {
            return true;
        }
        public void reset() {
        }
    });
    integ.integrate(pb,
            pb.getInitialTime(), pb.getInitialState(),
            pb.getFinalTime(), new double[pb.getDimension()]);

  }

// org.apache.commons.math.ode.nonstiff.EulerIntegratorTest::testDimensionCheck
  public void testDimensionCheck() {
    try  {
      TestProblem1 pb = new TestProblem1();
      new EulerIntegrator(0.01).integrate(pb,
                                          0.0, new double[pb.getDimension()+10],
                                          1.0, new double[pb.getDimension()+10]);
        fail("an exception should have been thrown");
    } catch(MathUserException de) {
      fail("wrong exception caught");
    } catch(IntegratorException ie) {
    }
  }

// org.apache.commons.math.ode.nonstiff.EulerIntegratorTest::testDecreasingSteps
  public void testDecreasingSteps()
    throws MathUserException, IntegratorException {

    TestProblemAbstract[] problems = TestProblemFactory.getProblems();
    for (int k = 0; k < problems.length; ++k) {

      double previousError = Double.NaN;
      for (int i = 4; i < 10; ++i) {

        TestProblemAbstract pb  = problems[k].copy();
        double step = (pb.getFinalTime() - pb.getInitialTime())
          * FastMath.pow(2.0, -i);

        FirstOrderIntegrator integ = new EulerIntegrator(step);
        TestProblemHandler handler = new TestProblemHandler(pb, integ);
        integ.addStepHandler(handler);
        EventHandler[] functions = pb.getEventsHandlers();
        for (int l = 0; l < functions.length; ++l) {
          integ.addEventHandler(functions[l],
                                     Double.POSITIVE_INFINITY, 1.0e-6 * step, 1000);
        }
        double stopTime = integ.integrate(pb, pb.getInitialTime(), pb.getInitialState(),
                                          pb.getFinalTime(), new double[pb.getDimension()]);
        if (functions.length == 0) {
            assertEquals(pb.getFinalTime(), stopTime, 1.0e-10);
        }

        double error = handler.getMaximalValueError();
        if (i > 4) {
          assertTrue(error < FastMath.abs(previousError));
        }
        previousError = error;
        assertEquals(0, handler.getMaximalTimeError(), 1.0e-12);

      }

    }

  }

// org.apache.commons.math.ode.nonstiff.EulerIntegratorTest::testSmallStep
  public void testSmallStep()
    throws MathUserException, IntegratorException {

    TestProblem1 pb  = new TestProblem1();
    double step = (pb.getFinalTime() - pb.getInitialTime()) * 0.001;

    FirstOrderIntegrator integ = new EulerIntegrator(step);
    TestProblemHandler handler = new TestProblemHandler(pb, integ);
    integ.addStepHandler(handler);
    integ.integrate(pb,
                    pb.getInitialTime(), pb.getInitialState(),
                    pb.getFinalTime(), new double[pb.getDimension()]);

   assertTrue(handler.getLastError() < 2.0e-4);
   assertTrue(handler.getMaximalValueError() < 1.0e-3);
   assertEquals(0, handler.getMaximalTimeError(), 1.0e-12);
   assertEquals("Euler", integ.getName());

  }

// org.apache.commons.math.ode.nonstiff.EulerIntegratorTest::testBigStep
  public void testBigStep()
    throws MathUserException, IntegratorException {

    TestProblem1 pb  = new TestProblem1();
    double step = (pb.getFinalTime() - pb.getInitialTime()) * 0.2;

    FirstOrderIntegrator integ = new EulerIntegrator(step);
    TestProblemHandler handler = new TestProblemHandler(pb, integ);
    integ.addStepHandler(handler);
    integ.integrate(pb,
                    pb.getInitialTime(), pb.getInitialState(),
                    pb.getFinalTime(), new double[pb.getDimension()]);

    assertTrue(handler.getLastError() > 0.01);
    assertTrue(handler.getMaximalValueError() > 0.2);
    assertEquals(0, handler.getMaximalTimeError(), 1.0e-12);

  }

// org.apache.commons.math.ode.nonstiff.EulerIntegratorTest::testBackward
  public void testBackward()
      throws MathUserException, IntegratorException {

      TestProblem5 pb = new TestProblem5();
      double step = FastMath.abs(pb.getFinalTime() - pb.getInitialTime()) * 0.001;

      FirstOrderIntegrator integ = new EulerIntegrator(step);
      TestProblemHandler handler = new TestProblemHandler(pb, integ);
      integ.addStepHandler(handler);
      integ.integrate(pb, pb.getInitialTime(), pb.getInitialState(),
                      pb.getFinalTime(), new double[pb.getDimension()]);

      assertTrue(handler.getLastError() < 0.45);
      assertTrue(handler.getMaximalValueError() < 0.45);
      assertEquals(0, handler.getMaximalTimeError(), 1.0e-12);
      assertEquals("Euler", integ.getName());
  }

// org.apache.commons.math.ode.nonstiff.EulerIntegratorTest::testStepSize
  public void testStepSize()
    throws MathUserException, IntegratorException {
      final double step = 1.23456;
      FirstOrderIntegrator integ = new EulerIntegrator(step);
      integ.addStepHandler(new StepHandler() {
        public void handleStep(StepInterpolator interpolator, boolean isLast) {
            if (! isLast) {
                assertEquals(step,
                             interpolator.getCurrentTime() - interpolator.getPreviousTime(),
                             1.0e-12);
            }
        }
        public boolean requiresDenseOutput() {
            return false;
        }
        public void reset() {
        }
      });
      integ.integrate(new FirstOrderDifferentialEquations() {
                          private static final long serialVersionUID = 0L;
                          public void computeDerivatives(double t, double[] y, double[] dot) {
                              dot[0] = 1.0;
                          }
                          public int getDimension() {
                              return 1;
                          }
                      }, 0.0, new double[] { 0.0 }, 5.0, new double[1]);
  }

// org.apache.commons.math.ode.nonstiff.EulerStepInterpolatorTest::noReset
  public void noReset() throws MathUserException {

    double[]   y    =   { 0.0, 1.0, -2.0 };
    double[][] yDot = { { 1.0, 2.0, -2.0 } };
    EulerStepInterpolator interpolator = new EulerStepInterpolator();
    interpolator.reinitialize(new DummyIntegrator(interpolator), y, yDot, true);
    interpolator.storeTime(0);
    interpolator.shift();
    interpolator.storeTime(1);

    double[] result = interpolator.getInterpolatedState();
    for (int i = 0; i < result.length; ++i) {
      assertTrue(FastMath.abs(result[i] - y[i]) < 1.0e-10);
    }

  }

// org.apache.commons.math.ode.nonstiff.EulerStepInterpolatorTest::interpolationAtBounds
  public void interpolationAtBounds()
    throws MathUserException {

    double   t0 = 0;
    double[] y0 = {0.0, 1.0, -2.0};

    double[] y = y0.clone();
    double[][] yDot = { new double[y0.length] };
    EulerStepInterpolator interpolator = new EulerStepInterpolator();
    interpolator.reinitialize(new DummyIntegrator(interpolator), y, yDot, true);
    interpolator.storeTime(t0);

    double dt = 1.0;
    y[0] =  1.0;
    y[1] =  3.0;
    y[2] = -4.0;
    yDot[0][0] = (y[0] - y0[0]) / dt;
    yDot[0][1] = (y[1] - y0[1]) / dt;
    yDot[0][2] = (y[2] - y0[2]) / dt;
    interpolator.shift();
    interpolator.storeTime(t0 + dt);

    interpolator.setInterpolatedTime(interpolator.getPreviousTime());
    double[] result = interpolator.getInterpolatedState();
    for (int i = 0; i < result.length; ++i) {
      assertTrue(FastMath.abs(result[i] - y0[i]) < 1.0e-10);
    }

    interpolator.setInterpolatedTime(interpolator.getCurrentTime());
    result = interpolator.getInterpolatedState();
    for (int i = 0; i < result.length; ++i) {
      assertTrue(FastMath.abs(result[i] - y[i]) < 1.0e-10);
    }

  }

// org.apache.commons.math.ode.nonstiff.EulerStepInterpolatorTest::interpolationInside
  public void interpolationInside()
  throws MathUserException {

    double[]   y    =   { 1.0, 3.0, -4.0 };
    double[][] yDot = { { 1.0, 2.0, -2.0 } };
    EulerStepInterpolator interpolator = new EulerStepInterpolator();
    interpolator.reinitialize(new DummyIntegrator(interpolator), y, yDot, true);
    interpolator.storeTime(0);
    interpolator.shift();
    interpolator.storeTime(1);

    interpolator.setInterpolatedTime(0.1);
    double[] result = interpolator.getInterpolatedState();
    assertTrue(FastMath.abs(result[0] - 0.1) < 1.0e-10);
    assertTrue(FastMath.abs(result[1] - 1.2) < 1.0e-10);
    assertTrue(FastMath.abs(result[2] + 2.2) < 1.0e-10);

    interpolator.setInterpolatedTime(0.5);
    result = interpolator.getInterpolatedState();
    assertTrue(FastMath.abs(result[0] - 0.5) < 1.0e-10);
    assertTrue(FastMath.abs(result[1] - 2.0) < 1.0e-10);
    assertTrue(FastMath.abs(result[2] + 3.0) < 1.0e-10);

  }

// org.apache.commons.math.ode.nonstiff.EulerStepInterpolatorTest::derivativesConsistency
  public void derivativesConsistency()
  throws MathUserException, IntegratorException {
    TestProblem3 pb = new TestProblem3();
    double step = (pb.getFinalTime() - pb.getInitialTime()) * 0.001;
    EulerIntegrator integ = new EulerIntegrator(step);
    StepInterpolatorTestUtils.checkDerivativesConsistency(integ, pb, 1.0e-10);
  }

// org.apache.commons.math.ode.nonstiff.EulerStepInterpolatorTest::serialization
  public void serialization()
    throws MathUserException, IntegratorException,
           IOException, ClassNotFoundException {

    TestProblem1 pb = new TestProblem1();
    double step = (pb.getFinalTime() - pb.getInitialTime()) * 0.001;
    EulerIntegrator integ = new EulerIntegrator(step);
    integ.addStepHandler(new ContinuousOutputModel());
    integ.integrate(pb,
                    pb.getInitialTime(), pb.getInitialState(),
                    pb.getFinalTime(), new double[pb.getDimension()]);

    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    ObjectOutputStream    oos = new ObjectOutputStream(bos);
    for (StepHandler handler : integ.getStepHandlers()) {
        oos.writeObject(handler);
    }

    ByteArrayInputStream  bis = new ByteArrayInputStream(bos.toByteArray());
    ObjectInputStream     ois = new ObjectInputStream(bis);
    ContinuousOutputModel cm  = (ContinuousOutputModel) ois.readObject();

    Random random = new Random(347588535632l);
    double maxError = 0.0;
    for (int i = 0; i < 1000; ++i) {
      double r = random.nextDouble();
      double time = r * pb.getInitialTime() + (1.0 - r) * pb.getFinalTime();
      cm.setInterpolatedTime(time);
      double[] interpolatedY = cm.getInterpolatedState ();
      double[] theoreticalY  = pb.computeTheoreticalState(time);
      double dx = interpolatedY[0] - theoreticalY[0];
      double dy = interpolatedY[1] - theoreticalY[1];
      double error = dx * dx + dy * dy;
      if (error > maxError) {
        maxError = error;
      }
    }
    assertTrue(maxError < 0.001);

  }

// org.apache.commons.math.ode.nonstiff.GillIntegratorTest::testDimensionCheck
  public void testDimensionCheck() {
    try  {
      TestProblem1 pb = new TestProblem1();
      new GillIntegrator(0.01).integrate(pb,
                                         0.0, new double[pb.getDimension()+10],
                                         1.0, new double[pb.getDimension()+10]);
        fail("an exception should have been thrown");
    } catch(MathUserException de) {
      fail("wrong exception caught");
    } catch(IntegratorException ie) {
    }
  }

// org.apache.commons.math.ode.nonstiff.GillIntegratorTest::testDecreasingSteps
  public void testDecreasingSteps()
    throws MathUserException, IntegratorException  {

    TestProblemAbstract[] problems = TestProblemFactory.getProblems();
    for (int k = 0; k < problems.length; ++k) {

      double previousError = Double.NaN;
      for (int i = 5; i < 10; ++i) {

        TestProblemAbstract pb = problems[k].copy();
        double step = (pb.getFinalTime() - pb.getInitialTime())
          * FastMath.pow(2.0, -i);

        FirstOrderIntegrator integ = new GillIntegrator(step);
        TestProblemHandler handler = new TestProblemHandler(pb, integ);
        integ.addStepHandler(handler);
        EventHandler[] functions = pb.getEventsHandlers();
        for (int l = 0; l < functions.length; ++l) {
          integ.addEventHandler(functions[l],
                                     Double.POSITIVE_INFINITY, 1.0e-6 * step, 1000);
        }
        double stopTime = integ.integrate(pb, pb.getInitialTime(), pb.getInitialState(),
                                          pb.getFinalTime(), new double[pb.getDimension()]);
        if (functions.length == 0) {
            assertEquals(pb.getFinalTime(), stopTime, 1.0e-10);
        }

        double error = handler.getMaximalValueError();
        if (i > 5) {
          assertTrue(error < FastMath.abs(previousError));
        }
        previousError = error;
        assertEquals(0, handler.getMaximalTimeError(), 1.0e-12);

      }

    }

  }

// org.apache.commons.math.ode.nonstiff.GillIntegratorTest::testSmallStep
  public void testSmallStep()
    throws MathUserException, IntegratorException {

    TestProblem1 pb = new TestProblem1();
    double step = (pb.getFinalTime() - pb.getInitialTime()) * 0.001;

    FirstOrderIntegrator integ = new GillIntegrator(step);
    TestProblemHandler handler = new TestProblemHandler(pb, integ);
    integ.addStepHandler(handler);
    integ.integrate(pb, pb.getInitialTime(), pb.getInitialState(),
                    pb.getFinalTime(), new double[pb.getDimension()]);

    assertTrue(handler.getLastError() < 2.0e-13);
    assertTrue(handler.getMaximalValueError() < 4.0e-12);
    assertEquals(0, handler.getMaximalTimeError(), 1.0e-12);
    assertEquals("Gill", integ.getName());

  }

// org.apache.commons.math.ode.nonstiff.GillIntegratorTest::testBigStep
  public void testBigStep()
    throws MathUserException, IntegratorException {

    TestProblem1 pb = new TestProblem1();
    double step = (pb.getFinalTime() - pb.getInitialTime()) * 0.2;

    FirstOrderIntegrator integ = new GillIntegrator(step);
    TestProblemHandler handler = new TestProblemHandler(pb, integ);
    integ.addStepHandler(handler);
    integ.integrate(pb, pb.getInitialTime(), pb.getInitialState(),
                    pb.getFinalTime(), new double[pb.getDimension()]);

    assertTrue(handler.getLastError() > 0.0004);
    assertTrue(handler.getMaximalValueError() > 0.005);
    assertEquals(0, handler.getMaximalTimeError(), 1.0e-12);

  }

// org.apache.commons.math.ode.nonstiff.GillIntegratorTest::testBackward
  public void testBackward()
      throws MathUserException, IntegratorException {

      TestProblem5 pb = new TestProblem5();
      double step = FastMath.abs(pb.getFinalTime() - pb.getInitialTime()) * 0.001;

      FirstOrderIntegrator integ = new GillIntegrator(step);
      TestProblemHandler handler = new TestProblemHandler(pb, integ);
      integ.addStepHandler(handler);
      integ.integrate(pb, pb.getInitialTime(), pb.getInitialState(),
                      pb.getFinalTime(), new double[pb.getDimension()]);

      assertTrue(handler.getLastError() < 5.0e-10);
      assertTrue(handler.getMaximalValueError() < 7.0e-10);
      assertEquals(0, handler.getMaximalTimeError(), 1.0e-12);
      assertEquals("Gill", integ.getName());
  }

// org.apache.commons.math.ode.nonstiff.GillIntegratorTest::testKepler
  public void testKepler()
    throws MathUserException, IntegratorException {

    final TestProblem3 pb  = new TestProblem3(0.9);
    double step = (pb.getFinalTime() - pb.getInitialTime()) * 0.0003;

    FirstOrderIntegrator integ = new GillIntegrator(step);
    integ.addStepHandler(new KeplerStepHandler(pb));
    integ.integrate(pb,
                    pb.getInitialTime(), pb.getInitialState(),
                    pb.getFinalTime(), new double[pb.getDimension()]);
  }

// org.apache.commons.math.ode.nonstiff.GillIntegratorTest::testUnstableDerivative
  public void testUnstableDerivative()
  throws MathUserException, IntegratorException {
    final StepProblem stepProblem = new StepProblem(0.0, 1.0, 2.0);
    FirstOrderIntegrator integ = new GillIntegrator(0.3);
    integ.addEventHandler(stepProblem, 1.0, 1.0e-12, 1000);
    double[] y = { Double.NaN };
    integ.integrate(stepProblem, 0.0, new double[] { 0.0 }, 10.0, y);
    assertEquals(8.0, y[0], 1.0e-12);
  }

// org.apache.commons.math.ode.nonstiff.GillIntegratorTest::testStepSize
  public void testStepSize()
    throws MathUserException, IntegratorException {
      final double step = 1.23456;
      FirstOrderIntegrator integ = new GillIntegrator(step);
      integ.addStepHandler(new StepHandler() {
          public void handleStep(StepInterpolator interpolator, boolean isLast) {
              if (! isLast) {
                  assertEquals(step,
                               interpolator.getCurrentTime() - interpolator.getPreviousTime(),
                               1.0e-12);
              }
          }
          public boolean requiresDenseOutput() {
              return false;
          }
          public void reset() {
          }
      });
      integ.integrate(new FirstOrderDifferentialEquations() {
          private static final long serialVersionUID = 0L;
          public void computeDerivatives(double t, double[] y, double[] dot) {
              dot[0] = 1.0;
          }
          public int getDimension() {
              return 1;
          }
      }, 0.0, new double[] { 0.0 }, 5.0, new double[1]);
  }

// org.apache.commons.math.ode.nonstiff.GillStepInterpolatorTest::testDerivativesConsistency
  public void testDerivativesConsistency()
  throws MathUserException, IntegratorException {
    TestProblem3 pb = new TestProblem3();
    double step = (pb.getFinalTime() - pb.getInitialTime()) * 0.001;
    GillIntegrator integ = new GillIntegrator(step);
    StepInterpolatorTestUtils.checkDerivativesConsistency(integ, pb, 1.0e-10);
  }

// org.apache.commons.math.ode.nonstiff.GillStepInterpolatorTest::serialization
  public void serialization()
    throws MathUserException, IntegratorException,
           IOException, ClassNotFoundException {

    TestProblem3 pb = new TestProblem3(0.9);
    double step = (pb.getFinalTime() - pb.getInitialTime()) * 0.0003;
    GillIntegrator integ = new GillIntegrator(step);
    integ.addStepHandler(new ContinuousOutputModel());
    integ.integrate(pb,
                    pb.getInitialTime(), pb.getInitialState(),
                    pb.getFinalTime(), new double[pb.getDimension()]);

    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    ObjectOutputStream    oos = new ObjectOutputStream(bos);
    for (StepHandler handler : integ.getStepHandlers()) {
        oos.writeObject(handler);
    }

    assertTrue(bos.size () > 700000);
    assertTrue(bos.size () < 701000);

    ByteArrayInputStream  bis = new ByteArrayInputStream(bos.toByteArray());
    ObjectInputStream     ois = new ObjectInputStream(bis);
    ContinuousOutputModel cm  = (ContinuousOutputModel) ois.readObject();

    Random random = new Random(347588535632l);
    double maxError = 0.0;
    for (int i = 0; i < 1000; ++i) {
      double r = random.nextDouble();
      double time = r * pb.getInitialTime() + (1.0 - r) * pb.getFinalTime();
      cm.setInterpolatedTime(time);
      double[] interpolatedY = cm.getInterpolatedState ();
      double[] theoreticalY  = pb.computeTheoreticalState(time);
      double dx = interpolatedY[0] - theoreticalY[0];
      double dy = interpolatedY[1] - theoreticalY[1];
      double error = dx * dx + dy * dy;
      if (error > maxError) {
        maxError = error;
      }
    }

    assertTrue(maxError < 0.003);

  }

// org.apache.commons.math.ode.nonstiff.GraggBulirschStoerIntegratorTest::testDimensionCheck
  public void testDimensionCheck() {
    try  {
      TestProblem1 pb = new TestProblem1();
      AdaptiveStepsizeIntegrator integrator =
        new GraggBulirschStoerIntegrator(0.0, 1.0, 1.0e-10, 1.0e-10);
      integrator.integrate(pb,
                           0.0, new double[pb.getDimension()+10],
                           1.0, new double[pb.getDimension()+10]);
      fail("an exception should have been thrown");
    } catch(MathUserException de) {
      fail("wrong exception caught");
    } catch(IntegratorException ie) {
    }
  }

// org.apache.commons.math.ode.nonstiff.GraggBulirschStoerIntegratorTest::testNullIntervalCheck
  public void testNullIntervalCheck() {
    try  {
      TestProblem1 pb = new TestProblem1();
      GraggBulirschStoerIntegrator integrator =
        new GraggBulirschStoerIntegrator(0.0, 1.0, 1.0e-10, 1.0e-10);
      integrator.integrate(pb,
                           0.0, new double[pb.getDimension()],
                           0.0, new double[pb.getDimension()]);
      fail("an exception should have been thrown");
    } catch(MathUserException de) {
      fail("wrong exception caught");
    } catch(IntegratorException ie) {
    }
  }

// org.apache.commons.math.ode.nonstiff.GraggBulirschStoerIntegratorTest::testMinStep
  public void testMinStep() {

    try {
      TestProblem5 pb  = new TestProblem5();
      double minStep   = 0.1 * FastMath.abs(pb.getFinalTime() - pb.getInitialTime());
      double maxStep   = FastMath.abs(pb.getFinalTime() - pb.getInitialTime());
      double[] vecAbsoluteTolerance = { 1.0e-20, 1.0e-21 };
      double[] vecRelativeTolerance = { 1.0e-20, 1.0e-21 };

      FirstOrderIntegrator integ =
        new GraggBulirschStoerIntegrator(minStep, maxStep,
                                         vecAbsoluteTolerance, vecRelativeTolerance);
      TestProblemHandler handler = new TestProblemHandler(pb, integ);
      integ.addStepHandler(handler);
      integ.integrate(pb,
                      pb.getInitialTime(), pb.getInitialState(),
                      pb.getFinalTime(), new double[pb.getDimension()]);
      fail("an exception should have been thrown");
    } catch(MathUserException de) {
      fail("wrong exception caught");
    } catch(IntegratorException ie) {
    }

  }

// org.apache.commons.math.ode.nonstiff.GraggBulirschStoerIntegratorTest::testBackward
  public void testBackward()
      throws MathUserException, IntegratorException {

      TestProblem5 pb = new TestProblem5();
      double minStep = 0;
      double maxStep = pb.getFinalTime() - pb.getInitialTime();
      double scalAbsoluteTolerance = 1.0e-8;
      double scalRelativeTolerance = 0.01 * scalAbsoluteTolerance;

      FirstOrderIntegrator integ = new GraggBulirschStoerIntegrator(minStep, maxStep,
                                                                    scalAbsoluteTolerance,
                                                                    scalRelativeTolerance);
      TestProblemHandler handler = new TestProblemHandler(pb, integ);
      integ.addStepHandler(handler);
      integ.integrate(pb, pb.getInitialTime(), pb.getInitialState(),
                      pb.getFinalTime(), new double[pb.getDimension()]);

      assertTrue(handler.getLastError() < 9.0e-10);
      assertTrue(handler.getMaximalValueError() < 9.0e-10);
      assertEquals(0, handler.getMaximalTimeError(), 1.0e-12);
      assertEquals("Gragg-Bulirsch-Stoer", integ.getName());
  }

// org.apache.commons.math.ode.nonstiff.GraggBulirschStoerIntegratorTest::testIncreasingTolerance
  public void testIncreasingTolerance()
    throws MathUserException, IntegratorException {

    int previousCalls = Integer.MAX_VALUE;
    for (int i = -12; i < -4; ++i) {
      TestProblem1 pb     = new TestProblem1();
      double minStep      = 0;
      double maxStep      = pb.getFinalTime() - pb.getInitialTime();
      double absTolerance = FastMath.pow(10.0, i);
      double relTolerance = absTolerance;

      FirstOrderIntegrator integ =
        new GraggBulirschStoerIntegrator(minStep, maxStep,
                                         absTolerance, relTolerance);
      TestProblemHandler handler = new TestProblemHandler(pb, integ);
      integ.addStepHandler(handler);
      integ.integrate(pb,
                      pb.getInitialTime(), pb.getInitialState(),
                      pb.getFinalTime(), new double[pb.getDimension()]);

      
      
      
      double ratio =  handler.getMaximalValueError() / absTolerance;
      assertTrue(ratio < 2.4);
      assertTrue(ratio > 0.02);
      assertEquals(0, handler.getMaximalTimeError(), 1.0e-12);

      int calls = pb.getCalls();
      assertEquals(integ.getEvaluations(), calls);
      assertTrue(calls <= previousCalls);
      previousCalls = calls;

    }

  }

// org.apache.commons.math.ode.nonstiff.GraggBulirschStoerIntegratorTest::testIntegratorControls
  public void testIntegratorControls() {}

// org.apache.commons.math.ode.nonstiff.GraggBulirschStoerIntegratorTest::testEvents
  public void testEvents()
    throws MathUserException, IntegratorException {

    TestProblem4 pb = new TestProblem4();
    double minStep = 0;
    double maxStep = pb.getFinalTime() - pb.getInitialTime();
    double scalAbsoluteTolerance = 1.0e-10;
    double scalRelativeTolerance = 0.01 * scalAbsoluteTolerance;

    FirstOrderIntegrator integ = new GraggBulirschStoerIntegrator(minStep, maxStep,
                                                                  scalAbsoluteTolerance,
                                                                  scalRelativeTolerance);
    TestProblemHandler handler = new TestProblemHandler(pb, integ);
    integ.addStepHandler(handler);
    EventHandler[] functions = pb.getEventsHandlers();
    for (int l = 0; l < functions.length; ++l) {
      integ.addEventHandler(functions[l],
                                 Double.POSITIVE_INFINITY, 1.0e-8 * maxStep, 1000);
    }
    assertEquals(functions.length, integ.getEventHandlers().size());
    integ.integrate(pb,
                    pb.getInitialTime(), pb.getInitialState(),
                    pb.getFinalTime(), new double[pb.getDimension()]);

    assertTrue(handler.getMaximalValueError() < 5.0e-8);
    assertEquals(0, handler.getMaximalTimeError(), 1.0e-12);
    assertEquals(12.0, handler.getLastTime(), 1.0e-8 * maxStep);
    integ.clearEventHandlers();
    assertEquals(0, integ.getEventHandlers().size());

  }

// org.apache.commons.math.ode.nonstiff.GraggBulirschStoerIntegratorTest::testKepler
  public void testKepler()
    throws MathUserException, IntegratorException {

    final TestProblem3 pb = new TestProblem3(0.9);
    double minStep        = 0;
    double maxStep        = pb.getFinalTime() - pb.getInitialTime();
    double absTolerance   = 1.0e-6;
    double relTolerance   = 1.0e-6;

    FirstOrderIntegrator integ =
      new GraggBulirschStoerIntegrator(minStep, maxStep,
                                       absTolerance, relTolerance);
    integ.addStepHandler(new KeplerStepHandler(pb));
    integ.integrate(pb,
                    pb.getInitialTime(), pb.getInitialState(),
                    pb.getFinalTime(), new double[pb.getDimension()]);

    assertEquals(integ.getEvaluations(), pb.getCalls());
    assertTrue(pb.getCalls() < 2150);

  }

// org.apache.commons.math.ode.nonstiff.GraggBulirschStoerIntegratorTest::testVariableSteps
  public void testVariableSteps()
    throws MathUserException, IntegratorException {

    final TestProblem3 pb = new TestProblem3(0.9);
    double minStep        = 0;
    double maxStep        = pb.getFinalTime() - pb.getInitialTime();
    double absTolerance   = 1.0e-8;
    double relTolerance   = 1.0e-8;
    FirstOrderIntegrator integ =
      new GraggBulirschStoerIntegrator(minStep, maxStep,
                                       absTolerance, relTolerance);
    integ.addStepHandler(new VariableStepHandler());
    double stopTime = integ.integrate(pb,
                                      pb.getInitialTime(), pb.getInitialState(),
                                      pb.getFinalTime(), new double[pb.getDimension()]);
    assertEquals(pb.getFinalTime(), stopTime, 1.0e-10);
    assertEquals("Gragg-Bulirsch-Stoer", integ.getName());
  }

// org.apache.commons.math.ode.nonstiff.GraggBulirschStoerIntegratorTest::testUnstableDerivative
  public void testUnstableDerivative()
    throws MathUserException, IntegratorException {
    final StepProblem stepProblem = new StepProblem(0.0, 1.0, 2.0);
    FirstOrderIntegrator integ =
      new GraggBulirschStoerIntegrator(0.1, 10, 1.0e-12, 0.0);
    integ.addEventHandler(stepProblem, 1.0, 1.0e-12, 1000);
    double[] y = { Double.NaN };
    integ.integrate(stepProblem, 0.0, new double[] { 0.0 }, 10.0, y);
    assertEquals(8.0, y[0], 1.0e-12);
  }

// org.apache.commons.math.ode.nonstiff.GraggBulirschStoerStepInterpolatorTest::derivativesConsistency
  public void derivativesConsistency()
  throws MathUserException, IntegratorException {
    TestProblem3 pb = new TestProblem3(0.9);
    double minStep   = 0;
    double maxStep   = pb.getFinalTime() - pb.getInitialTime();
    double absTolerance = 1.0e-8;
    double relTolerance = 1.0e-8;

    GraggBulirschStoerIntegrator integ =
      new GraggBulirschStoerIntegrator(minStep, maxStep,
                                       absTolerance, relTolerance);
    StepInterpolatorTestUtils.checkDerivativesConsistency(integ, pb, 1.0e-8);
  }

// org.apache.commons.math.ode.nonstiff.GraggBulirschStoerStepInterpolatorTest::serialization
  public void serialization()
    throws MathUserException, IntegratorException,
           IOException, ClassNotFoundException {

    TestProblem3 pb  = new TestProblem3(0.9);
    double minStep   = 0;
    double maxStep   = pb.getFinalTime() - pb.getInitialTime();
    double absTolerance = 1.0e-8;
    double relTolerance = 1.0e-8;

    GraggBulirschStoerIntegrator integ =
      new GraggBulirschStoerIntegrator(minStep, maxStep,
                                       absTolerance, relTolerance);
    integ.addStepHandler(new ContinuousOutputModel());
    integ.integrate(pb,
                    pb.getInitialTime(), pb.getInitialState(),
                    pb.getFinalTime(), new double[pb.getDimension()]);

    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    ObjectOutputStream    oos = new ObjectOutputStream(bos);
    for (StepHandler handler : integ.getStepHandlers()) {
        oos.writeObject(handler);
    }

    assertTrue(bos.size () > 33000);
    assertTrue(bos.size () < 34000);

    ByteArrayInputStream  bis = new ByteArrayInputStream(bos.toByteArray());
    ObjectInputStream     ois = new ObjectInputStream(bis);
    ContinuousOutputModel cm  = (ContinuousOutputModel) ois.readObject();

    Random random = new Random(347588535632l);
    double maxError = 0.0;
    for (int i = 0; i < 1000; ++i) {
      double r = random.nextDouble();
      double time = r * pb.getInitialTime() + (1.0 - r) * pb.getFinalTime();
      cm.setInterpolatedTime(time);
      double[] interpolatedY = cm.getInterpolatedState ();
      double[] theoreticalY  = pb.computeTheoreticalState(time);
      double dx = interpolatedY[0] - theoreticalY[0];
      double dy = interpolatedY[1] - theoreticalY[1];
      double error = dx * dx + dy * dy;
      if (error > maxError) {
        maxError = error;
      }
    }

    assertTrue(maxError < 5.0e-10);

  }

// org.apache.commons.math.ode.nonstiff.GraggBulirschStoerStepInterpolatorTest::checklone
  public void checklone()
  throws MathUserException, IntegratorException {
    TestProblem3 pb = new TestProblem3(0.9);
    double minStep = 0;
    double maxStep = pb.getFinalTime() - pb.getInitialTime();
    double scalAbsoluteTolerance = 1.0e-8;
    double scalRelativeTolerance = scalAbsoluteTolerance;
    GraggBulirschStoerIntegrator integ = new GraggBulirschStoerIntegrator(minStep, maxStep,
                                                                          scalAbsoluteTolerance,
                                                                          scalRelativeTolerance);
    integ.addStepHandler(new StepHandler() {
        public void handleStep(StepInterpolator interpolator, boolean isLast)
        throws MathUserException {
            StepInterpolator cloned = interpolator.copy();
            double tA = cloned.getPreviousTime();
            double tB = cloned.getCurrentTime();
            double halfStep = FastMath.abs(tB - tA) / 2;
            assertEquals(interpolator.getPreviousTime(), tA, 1.0e-12);
            assertEquals(interpolator.getCurrentTime(), tB, 1.0e-12);
            for (int i = 0; i < 10; ++i) {
                double t = (i * tB + (9 - i) * tA) / 9;
                interpolator.setInterpolatedTime(t);
                assertTrue(FastMath.abs(cloned.getInterpolatedTime() - t) > (halfStep / 10));
                cloned.setInterpolatedTime(t);
                assertEquals(t, cloned.getInterpolatedTime(), 1.0e-12);
                double[] referenceState = interpolator.getInterpolatedState();
                double[] cloneState     = cloned.getInterpolatedState();
                for (int j = 0; j < referenceState.length; ++j) {
                    assertEquals(referenceState[j], cloneState[j], 1.0e-12);
                }
            }
        }
        public boolean requiresDenseOutput() {
            return true;
        }
        public void reset() {
        }
    });
    integ.integrate(pb,
            pb.getInitialTime(), pb.getInitialState(),
            pb.getFinalTime(), new double[pb.getDimension()]);

  }

// org.apache.commons.math.ode.nonstiff.HighamHall54IntegratorTest::testWrongDerivative
  public void testWrongDerivative() throws Exception {
      HighamHall54Integrator integrator =
          new HighamHall54Integrator(0.0, 1.0, 1.0e-10, 1.0e-10);
      FirstOrderDifferentialEquations equations =
          new FirstOrderDifferentialEquations() {
            private static final long serialVersionUID = -1157081786301178032L;
            public void computeDerivatives(double t, double[] y, double[] dot)
            throws MathUserException {
            if (t < -0.5) {
                throw new MathUserException(LocalizedFormats.SIMPLE_MESSAGE, "oops");
            } else {
                throw new MathUserException(new RuntimeException("oops"));
           }
          }
          public int getDimension() {
              return 1;
          }
      };

      try  {
        integrator.integrate(equations, -1.0, new double[1], 0.0, new double[1]);
        fail("an exception should have been thrown");
      } catch(MathUserException de) {
        
      }

      try  {
        integrator.integrate(equations, 0.0, new double[1], 1.0, new double[1]);
        fail("an exception should have been thrown");
      } catch(MathUserException de) {
        
      }

  }

// org.apache.commons.math.ode.nonstiff.HighamHall54IntegratorTest::testMinStep
  public void testMinStep() {

    try {
      TestProblem1 pb = new TestProblem1();
      double minStep = 0.1 * (pb.getFinalTime() - pb.getInitialTime());
      double maxStep = pb.getFinalTime() - pb.getInitialTime();
      double[] vecAbsoluteTolerance = { 1.0e-15, 1.0e-16 };
      double[] vecRelativeTolerance = { 1.0e-15, 1.0e-16 };

      FirstOrderIntegrator integ = new HighamHall54Integrator(minStep, maxStep,
                                                              vecAbsoluteTolerance,
                                                              vecRelativeTolerance);
      TestProblemHandler handler = new TestProblemHandler(pb, integ);
      integ.addStepHandler(handler);
      integ.integrate(pb,
                      pb.getInitialTime(), pb.getInitialState(),
                      pb.getFinalTime(), new double[pb.getDimension()]);
      fail("an exception should have been thrown");
    } catch(MathUserException de) {
      fail("wrong exception caught");
    } catch(IntegratorException ie) {
    }

  }

// org.apache.commons.math.ode.nonstiff.HighamHall54IntegratorTest::testIncreasingTolerance
  public void testIncreasingTolerance()
    throws MathUserException, IntegratorException {

    int previousCalls = Integer.MAX_VALUE;
    for (int i = -12; i < -2; ++i) {
      TestProblem1 pb = new TestProblem1();
      double minStep = 0;
      double maxStep = pb.getFinalTime() - pb.getInitialTime();
      double scalAbsoluteTolerance = FastMath.pow(10.0, i);
      double scalRelativeTolerance = 0.01 * scalAbsoluteTolerance;

      FirstOrderIntegrator integ = new HighamHall54Integrator(minStep, maxStep,
                                                              scalAbsoluteTolerance,
                                                              scalRelativeTolerance);
      TestProblemHandler handler = new TestProblemHandler(pb, integ);
      integ.addStepHandler(handler);
      integ.integrate(pb,
                      pb.getInitialTime(), pb.getInitialState(),
                      pb.getFinalTime(), new double[pb.getDimension()]);

      
      
      
      assertTrue(handler.getMaximalValueError() < (1.3 * scalAbsoluteTolerance));
      assertEquals(0, handler.getMaximalTimeError(), 1.0e-12);

      int calls = pb.getCalls();
      assertEquals(integ.getEvaluations(), calls);
      assertTrue(calls <= previousCalls);
      previousCalls = calls;

    }

  }

// org.apache.commons.math.ode.nonstiff.HighamHall54IntegratorTest::testBackward
  public void testBackward()
      throws MathUserException, IntegratorException {

      TestProblem5 pb = new TestProblem5();
      double minStep = 0;
      double maxStep = pb.getFinalTime() - pb.getInitialTime();
      double scalAbsoluteTolerance = 1.0e-8;
      double scalRelativeTolerance = 0.01 * scalAbsoluteTolerance;

      FirstOrderIntegrator integ = new HighamHall54Integrator(minStep, maxStep,
                                                              scalAbsoluteTolerance,
                                                              scalRelativeTolerance);
      TestProblemHandler handler = new TestProblemHandler(pb, integ);
      integ.addStepHandler(handler);
      integ.integrate(pb, pb.getInitialTime(), pb.getInitialState(),
                      pb.getFinalTime(), new double[pb.getDimension()]);

      assertTrue(handler.getLastError() < 5.0e-7);
      assertTrue(handler.getMaximalValueError() < 5.0e-7);
      assertEquals(0, handler.getMaximalTimeError(), 1.0e-12);
      assertEquals("Higham-Hall 5(4)", integ.getName());
  }

// org.apache.commons.math.ode.nonstiff.HighamHall54IntegratorTest::testEvents
  public void testEvents()
    throws MathUserException, IntegratorException {

    TestProblem4 pb = new TestProblem4();
    double minStep = 0;
    double maxStep = pb.getFinalTime() - pb.getInitialTime();
    double scalAbsoluteTolerance = 1.0e-8;
    double scalRelativeTolerance = 0.01 * scalAbsoluteTolerance;

    FirstOrderIntegrator integ = new HighamHall54Integrator(minStep, maxStep,
                                                            scalAbsoluteTolerance,
                                                            scalRelativeTolerance);
    TestProblemHandler handler = new TestProblemHandler(pb, integ);
    integ.addStepHandler(handler);
    EventHandler[] functions = pb.getEventsHandlers();
    for (int l = 0; l < functions.length; ++l) {
      integ.addEventHandler(functions[l],
                                 Double.POSITIVE_INFINITY, 1.0e-8 * maxStep, 1000);
    }
    assertEquals(functions.length, integ.getEventHandlers().size());
    integ.integrate(pb,
                    pb.getInitialTime(), pb.getInitialState(),
                    pb.getFinalTime(), new double[pb.getDimension()]);

    assertTrue(handler.getMaximalValueError() < 1.0e-7);
    assertEquals(0, handler.getMaximalTimeError(), 1.0e-12);
    assertEquals(12.0, handler.getLastTime(), 1.0e-8 * maxStep);
    integ.clearEventHandlers();
    assertEquals(0, integ.getEventHandlers().size());

  }

// org.apache.commons.math.ode.nonstiff.HighamHall54IntegratorTest::testEventsErrors
  public void testEventsErrors() throws Exception {

      final TestProblem1 pb = new TestProblem1();
      double minStep = 0;
      double maxStep = pb.getFinalTime() - pb.getInitialTime();
      double scalAbsoluteTolerance = 1.0e-8;
      double scalRelativeTolerance = 0.01 * scalAbsoluteTolerance;

      FirstOrderIntegrator integ =
          new HighamHall54Integrator(minStep, maxStep,
                                     scalAbsoluteTolerance, scalRelativeTolerance);
      TestProblemHandler handler = new TestProblemHandler(pb, integ);
      integ.addStepHandler(handler);

      integ.addEventHandler(new EventHandler() {
        public int eventOccurred(double t, double[] y, boolean increasing) {
          return EventHandler.CONTINUE;
        }
        public double g(double t, double[] y) throws EventException {
          double middle = (pb.getInitialTime() + pb.getFinalTime()) / 2;
          double offset = t - middle;
          if (offset > 0) {
            throw new EventException(LocalizedFormats.EVALUATION_FAILED, t);
          }
          return offset;
        }
        public void resetState(double t, double[] y) {
        }
        private static final long serialVersionUID = 935652725339916361L;
      }, Double.POSITIVE_INFINITY, 1.0e-8 * maxStep, 1000);

      try {
        integ.integrate(pb,
                        pb.getInitialTime(), pb.getInitialState(),
                        pb.getFinalTime(), new double[pb.getDimension()]);
        fail("an exception should have been thrown");
      } catch (IntegratorException ie) {
        
      }

  }

// org.apache.commons.math.ode.nonstiff.HighamHall54IntegratorTest::testEventsNoConvergence
  public void testEventsNoConvergence() throws Exception {

    final TestProblem1 pb = new TestProblem1();
    double minStep = 0;
    double maxStep = pb.getFinalTime() - pb.getInitialTime();
    double scalAbsoluteTolerance = 1.0e-8;
    double scalRelativeTolerance = 0.01 * scalAbsoluteTolerance;

    FirstOrderIntegrator integ =
        new HighamHall54Integrator(minStep, maxStep,
                                   scalAbsoluteTolerance, scalRelativeTolerance);
    TestProblemHandler handler = new TestProblemHandler(pb, integ);
    integ.addStepHandler(handler);

    integ.addEventHandler(new EventHandler() {
      public int eventOccurred(double t, double[] y, boolean increasing) {
        return EventHandler.CONTINUE;
      }
      public double g(double t, double[] y) {
        double middle = (pb.getInitialTime() + pb.getFinalTime()) / 2;
        double offset = t - middle;
        return (offset > 0) ? (offset + 0.5) : (offset - 0.5);
      }
      public void resetState(double t, double[] y) {
      }
      private static final long serialVersionUID = 935652725339916361L;
    }, Double.POSITIVE_INFINITY, 1.0e-8 * maxStep, 3);

    try {
      integ.integrate(pb,
                      pb.getInitialTime(), pb.getInitialState(),
                      pb.getFinalTime(), new double[pb.getDimension()]);
      fail("an exception should have been thrown");
    } catch (TooManyEvaluationsException tmee) {
        
    }

}

// org.apache.commons.math.ode.nonstiff.HighamHall54IntegratorTest::testSanityChecks
  public void testSanityChecks() throws Exception {
      final TestProblem3 pb  = new TestProblem3(0.9);
      double minStep = 0;
      double maxStep = pb.getFinalTime() - pb.getInitialTime();

      try {
        FirstOrderIntegrator integ =
            new HighamHall54Integrator(minStep, maxStep, new double[4], new double[4]);
        integ.integrate(pb, pb.getInitialTime(), new double[6],
                        pb.getFinalTime(), new double[pb.getDimension()]);
        fail("an exception should have been thrown");
      } catch (IntegratorException ie) {
        
      }

      try {
        FirstOrderIntegrator integ =
            new HighamHall54Integrator(minStep, maxStep, new double[4], new double[4]);
        integ.integrate(pb, pb.getInitialTime(), pb.getInitialState(),
                        pb.getFinalTime(), new double[6]);
        fail("an exception should have been thrown");
      } catch (IntegratorException ie) {
        
      }

      try {
        FirstOrderIntegrator integ =
            new HighamHall54Integrator(minStep, maxStep, new double[2], new double[4]);
        integ.integrate(pb, pb.getInitialTime(), pb.getInitialState(),
                        pb.getFinalTime(), new double[pb.getDimension()]);
        fail("an exception should have been thrown");
      } catch (IntegratorException ie) {
        
      }

      try {
        FirstOrderIntegrator integ =
            new HighamHall54Integrator(minStep, maxStep, new double[4], new double[2]);
        integ.integrate(pb, pb.getInitialTime(), pb.getInitialState(),
                        pb.getFinalTime(), new double[pb.getDimension()]);
        fail("an exception should have been thrown");
      } catch (IntegratorException ie) {
        
      }

      try {
        FirstOrderIntegrator integ =
            new HighamHall54Integrator(minStep, maxStep, new double[4], new double[4]);
        integ.integrate(pb, pb.getInitialTime(), pb.getInitialState(),
                        pb.getInitialTime(), new double[pb.getDimension()]);
        fail("an exception should have been thrown");
      } catch (IntegratorException ie) {
        
      }

  }

// org.apache.commons.math.ode.nonstiff.HighamHall54IntegratorTest::testKepler
  public void testKepler()
    throws MathUserException, IntegratorException {

    final TestProblem3 pb  = new TestProblem3(0.9);
    double minStep = 0;
    double maxStep = pb.getFinalTime() - pb.getInitialTime();
    double[] vecAbsoluteTolerance = { 1.0e-8, 1.0e-8, 1.0e-10, 1.0e-10 };
    double[] vecRelativeTolerance = { 1.0e-10, 1.0e-10, 1.0e-8, 1.0e-8 };

    FirstOrderIntegrator integ = new HighamHall54Integrator(minStep, maxStep,
                                                            vecAbsoluteTolerance,
                                                            vecRelativeTolerance);
    integ.addStepHandler(new KeplerHandler(pb));
    integ.integrate(pb,
                    pb.getInitialTime(), pb.getInitialState(),
                    pb.getFinalTime(), new double[pb.getDimension()]);
    assertEquals("Higham-Hall 5(4)", integ.getName());
  }

// org.apache.commons.math.ode.nonstiff.HighamHall54StepInterpolatorTest::derivativesConsistency
  public void derivativesConsistency()
  throws MathUserException, IntegratorException {
    TestProblem3 pb = new TestProblem3(0.1);
    double minStep = 0;
    double maxStep = pb.getFinalTime() - pb.getInitialTime();
    double scalAbsoluteTolerance = 1.0e-8;
    double scalRelativeTolerance = scalAbsoluteTolerance;
    HighamHall54Integrator integ = new HighamHall54Integrator(minStep, maxStep,
                                                              scalAbsoluteTolerance,
                                                              scalRelativeTolerance);
    StepInterpolatorTestUtils.checkDerivativesConsistency(integ, pb, 1.1e-10);
  }

// org.apache.commons.math.ode.nonstiff.HighamHall54StepInterpolatorTest::serialization
  public void serialization()
    throws MathUserException, IntegratorException,
           IOException, ClassNotFoundException {

    TestProblem3 pb = new TestProblem3(0.9);
    double minStep = 0;
    double maxStep = pb.getFinalTime() - pb.getInitialTime();
    double scalAbsoluteTolerance = 1.0e-8;
    double scalRelativeTolerance = scalAbsoluteTolerance;
    HighamHall54Integrator integ = new HighamHall54Integrator(minStep, maxStep,
                                                              scalAbsoluteTolerance,
                                                              scalRelativeTolerance);
    integ.addStepHandler(new ContinuousOutputModel());
    integ.integrate(pb,
                    pb.getInitialTime(), pb.getInitialState(),
                    pb.getFinalTime(), new double[pb.getDimension()]);

    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    ObjectOutputStream    oos = new ObjectOutputStream(bos);
    for (StepHandler handler : integ.getStepHandlers()) {
        oos.writeObject(handler);
    }

    assertTrue(bos.size () > 158000);
    assertTrue(bos.size () < 159000);

    ByteArrayInputStream  bis = new ByteArrayInputStream(bos.toByteArray());
    ObjectInputStream     ois = new ObjectInputStream(bis);
    ContinuousOutputModel cm  = (ContinuousOutputModel) ois.readObject();

    Random random = new Random(347588535632l);
    double maxError = 0.0;
    for (int i = 0; i < 1000; ++i) {
      double r = random.nextDouble();
      double time = r * pb.getInitialTime() + (1.0 - r) * pb.getFinalTime();
      cm.setInterpolatedTime(time);
      double[] interpolatedY = cm.getInterpolatedState ();
      double[] theoreticalY  = pb.computeTheoreticalState(time);
      double dx = interpolatedY[0] - theoreticalY[0];
      double dy = interpolatedY[1] - theoreticalY[1];
      double error = dx * dx + dy * dy;
      if (error > maxError) {
        maxError = error;
      }
    }

    assertTrue(maxError < 1.6e-10);

  }

// org.apache.commons.math.ode.nonstiff.HighamHall54StepInterpolatorTest::checkClone
  public void checkClone()
  throws MathUserException, IntegratorException {
    TestProblem3 pb = new TestProblem3(0.9);
    double minStep = 0;
    double maxStep = pb.getFinalTime() - pb.getInitialTime();
    double scalAbsoluteTolerance = 1.0e-8;
    double scalRelativeTolerance = scalAbsoluteTolerance;
    HighamHall54Integrator integ = new HighamHall54Integrator(minStep, maxStep,
                                                              scalAbsoluteTolerance,
                                                              scalRelativeTolerance);
    integ.addStepHandler(new StepHandler() {
        public void handleStep(StepInterpolator interpolator, boolean isLast)
        throws MathUserException {
            StepInterpolator cloned = interpolator.copy();
            double tA = cloned.getPreviousTime();
            double tB = cloned.getCurrentTime();
            double halfStep = FastMath.abs(tB - tA) / 2;
            assertEquals(interpolator.getPreviousTime(), tA, 1.0e-12);
            assertEquals(interpolator.getCurrentTime(), tB, 1.0e-12);
            for (int i = 0; i < 10; ++i) {
                double t = (i * tB + (9 - i) * tA) / 9;
                interpolator.setInterpolatedTime(t);
                assertTrue(FastMath.abs(cloned.getInterpolatedTime() - t) > (halfStep / 10));
                cloned.setInterpolatedTime(t);
                assertEquals(t, cloned.getInterpolatedTime(), 1.0e-12);
                double[] referenceState = interpolator.getInterpolatedState();
                double[] cloneState     = cloned.getInterpolatedState();
                for (int j = 0; j < referenceState.length; ++j) {
                    assertEquals(referenceState[j], cloneState[j], 1.0e-12);
                }
            }
        }
        public boolean requiresDenseOutput() {
            return true;
        }
        public void reset() {
        }
    });
    integ.integrate(pb,
            pb.getInitialTime(), pb.getInitialState(),
            pb.getFinalTime(), new double[pb.getDimension()]);

  }

// org.apache.commons.math.ode.nonstiff.MidpointIntegratorTest::testDimensionCheck
  public void testDimensionCheck() {
    try  {
      TestProblem1 pb = new TestProblem1();
      new MidpointIntegrator(0.01).integrate(pb,
                                             0.0, new double[pb.getDimension()+10],
                                             1.0, new double[pb.getDimension()+10]);
        fail("an exception should have been thrown");
    } catch(MathUserException de) {
      fail("wrong exception caught");
    } catch(IntegratorException ie) {
    }
  }

// org.apache.commons.math.ode.nonstiff.MidpointIntegratorTest::testDecreasingSteps
  public void testDecreasingSteps()
    throws MathUserException, IntegratorException  {

    TestProblemAbstract[] problems = TestProblemFactory.getProblems();
    for (int k = 0; k < problems.length; ++k) {

      double previousError = Double.NaN;
      for (int i = 4; i < 10; ++i) {

        TestProblemAbstract pb = problems[k].copy();
        double step = (pb.getFinalTime() - pb.getInitialTime())
          * FastMath.pow(2.0, -i);
        FirstOrderIntegrator integ = new MidpointIntegrator(step);
        TestProblemHandler handler = new TestProblemHandler(pb, integ);
        integ.addStepHandler(handler);
        EventHandler[] functions = pb.getEventsHandlers();
        for (int l = 0; l < functions.length; ++l) {
          integ.addEventHandler(functions[l],
                                     Double.POSITIVE_INFINITY, 1.0e-6 * step, 1000);
        }
        double stopTime = integ.integrate(pb,
                                          pb.getInitialTime(), pb.getInitialState(),
                                          pb.getFinalTime(), new double[pb.getDimension()]);
        if (functions.length == 0) {
            assertEquals(pb.getFinalTime(), stopTime, 1.0e-10);
        }

        double error = handler.getMaximalValueError();
        if (i > 4) {
          assertTrue(error < FastMath.abs(previousError));
        }
        previousError = error;
        assertEquals(0, handler.getMaximalTimeError(), 1.0e-12);

      }

    }

  }

// org.apache.commons.math.ode.nonstiff.MidpointIntegratorTest::testSmallStep
  public void testSmallStep()
    throws MathUserException, IntegratorException {

    TestProblem1 pb  = new TestProblem1();
    double step = (pb.getFinalTime() - pb.getInitialTime()) * 0.001;

    FirstOrderIntegrator integ = new MidpointIntegrator(step);
    TestProblemHandler handler = new TestProblemHandler(pb, integ);
    integ.addStepHandler(handler);
    integ.integrate(pb,
                    pb.getInitialTime(), pb.getInitialState(),
                    pb.getFinalTime(), new double[pb.getDimension()]);

    assertTrue(handler.getLastError() < 2.0e-7);
    assertTrue(handler.getMaximalValueError() < 1.0e-6);
    assertEquals(0, handler.getMaximalTimeError(), 1.0e-12);
    assertEquals("midpoint", integ.getName());

  }

// org.apache.commons.math.ode.nonstiff.MidpointIntegratorTest::testBigStep
  public void testBigStep()
    throws MathUserException, IntegratorException {

    TestProblem1 pb  = new TestProblem1();
    double step = (pb.getFinalTime() - pb.getInitialTime()) * 0.2;

    FirstOrderIntegrator integ = new MidpointIntegrator(step);
    TestProblemHandler handler = new TestProblemHandler(pb, integ);
    integ.addStepHandler(handler);
    integ.integrate(pb,
                    pb.getInitialTime(), pb.getInitialState(),
                    pb.getFinalTime(), new double[pb.getDimension()]);

    assertTrue(handler.getLastError() > 0.01);
    assertTrue(handler.getMaximalValueError() > 0.05);
    assertEquals(0, handler.getMaximalTimeError(), 1.0e-12);

  }

// org.apache.commons.math.ode.nonstiff.MidpointIntegratorTest::testBackward
  public void testBackward()
      throws MathUserException, IntegratorException {

      TestProblem5 pb = new TestProblem5();
      double step = FastMath.abs(pb.getFinalTime() - pb.getInitialTime()) * 0.001;

      FirstOrderIntegrator integ = new MidpointIntegrator(step);
      TestProblemHandler handler = new TestProblemHandler(pb, integ);
      integ.addStepHandler(handler);
      integ.integrate(pb, pb.getInitialTime(), pb.getInitialState(),
                      pb.getFinalTime(), new double[pb.getDimension()]);

      assertTrue(handler.getLastError() < 6.0e-4);
      assertTrue(handler.getMaximalValueError() < 6.0e-4);
      assertEquals(0, handler.getMaximalTimeError(), 1.0e-12);
      assertEquals("midpoint", integ.getName());
  }

// org.apache.commons.math.ode.nonstiff.MidpointIntegratorTest::testStepSize
  public void testStepSize()
    throws MathUserException, IntegratorException {
      final double step = 1.23456;
      FirstOrderIntegrator integ = new MidpointIntegrator(step);
      integ.addStepHandler(new StepHandler() {
          public void handleStep(StepInterpolator interpolator, boolean isLast) {
              if (! isLast) {
                  assertEquals(step,
                               interpolator.getCurrentTime() - interpolator.getPreviousTime(),
                               1.0e-12);
              }
          }
          public boolean requiresDenseOutput() {
              return false;
          }
          public void reset() {
          }
      });
      integ.integrate(new FirstOrderDifferentialEquations() {
          private static final long serialVersionUID = 0L;
          public void computeDerivatives(double t, double[] y, double[] dot) {
              dot[0] = 1.0;
          }
          public int getDimension() {
              return 1;
          }
      }, 0.0, new double[] { 0.0 }, 5.0, new double[1]);
  }

// org.apache.commons.math.ode.nonstiff.MidpointStepInterpolatorTest::testDerivativesConsistency
  public void testDerivativesConsistency()
  throws MathUserException, IntegratorException {
    TestProblem3 pb = new TestProblem3();
    double step = (pb.getFinalTime() - pb.getInitialTime()) * 0.001;
    MidpointIntegrator integ = new MidpointIntegrator(step);
    StepInterpolatorTestUtils.checkDerivativesConsistency(integ, pb, 1.0e-10);
  }

// org.apache.commons.math.ode.nonstiff.MidpointStepInterpolatorTest::serialization
  public void serialization()
    throws MathUserException, IntegratorException,
           IOException, ClassNotFoundException {

    TestProblem1 pb = new TestProblem1();
    double step = (pb.getFinalTime() - pb.getInitialTime()) * 0.001;
    MidpointIntegrator integ = new MidpointIntegrator(step);
    integ.addStepHandler(new ContinuousOutputModel());
    integ.integrate(pb,
                    pb.getInitialTime(), pb.getInitialState(),
                    pb.getFinalTime(), new double[pb.getDimension()]);

    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    ObjectOutputStream    oos = new ObjectOutputStream(bos);
    for (StepHandler handler : integ.getStepHandlers()) {
        oos.writeObject(handler);
    }

    assertTrue(bos.size () > 98000);
    assertTrue(bos.size () < 99000);

    ByteArrayInputStream  bis = new ByteArrayInputStream(bos.toByteArray());
    ObjectInputStream     ois = new ObjectInputStream(bis);
    ContinuousOutputModel cm  = (ContinuousOutputModel) ois.readObject();

    Random random = new Random(347588535632l);
    double maxError = 0.0;
    for (int i = 0; i < 1000; ++i) {
      double r = random.nextDouble();
      double time = r * pb.getInitialTime() + (1.0 - r) * pb.getFinalTime();
      cm.setInterpolatedTime(time);
      double[] interpolatedY = cm.getInterpolatedState ();
      double[] theoreticalY  = pb.computeTheoreticalState(time);
      double dx = interpolatedY[0] - theoreticalY[0];
      double dy = interpolatedY[1] - theoreticalY[1];
      double error = dx * dx + dy * dy;
      if (error > maxError) {
        maxError = error;
      }
    }

    assertTrue(maxError < 1.0e-6);

  }

// org.apache.commons.math.ode.nonstiff.ThreeEighthesIntegratorTest::testDimensionCheck
  public void testDimensionCheck() {
    try  {
      TestProblem1 pb = new TestProblem1();
      new ThreeEighthesIntegrator(0.01).integrate(pb,
                                                  0.0, new double[pb.getDimension()+10],
                                                  1.0, new double[pb.getDimension()+10]);
        fail("an exception should have been thrown");
    } catch(MathUserException de) {
      fail("wrong exception caught");
    } catch(IntegratorException ie) {
    }
  }

// org.apache.commons.math.ode.nonstiff.ThreeEighthesIntegratorTest::testDecreasingSteps
  public void testDecreasingSteps()
    throws MathUserException, IntegratorException  {

    TestProblemAbstract[] problems = TestProblemFactory.getProblems();
    for (int k = 0; k < problems.length; ++k) {

      double previousError = Double.NaN;
      for (int i = 4; i < 10; ++i) {

        TestProblemAbstract pb = problems[k].copy();
        double step = (pb.getFinalTime() - pb.getInitialTime())
          * FastMath.pow(2.0, -i);

        FirstOrderIntegrator integ = new ThreeEighthesIntegrator(step);
        TestProblemHandler handler = new TestProblemHandler(pb, integ);
        integ.addStepHandler(handler);
        EventHandler[] functions = pb.getEventsHandlers();
        for (int l = 0; l < functions.length; ++l) {
          integ.addEventHandler(functions[l],
                                     Double.POSITIVE_INFINITY, 1.0e-6 * step, 1000);
        }
        double stopTime = integ.integrate(pb, pb.getInitialTime(), pb.getInitialState(),
                                          pb.getFinalTime(), new double[pb.getDimension()]);
        if (functions.length == 0) {
            assertEquals(pb.getFinalTime(), stopTime, 1.0e-10);
        }

        double error = handler.getMaximalValueError();
        if (i > 4) {
          assertTrue(error < FastMath.abs(previousError));
        }
        previousError = error;
        assertEquals(0, handler.getMaximalTimeError(), 1.0e-12);

      }

    }

  }

// org.apache.commons.math.ode.nonstiff.ThreeEighthesIntegratorTest::testSmallStep
 public void testSmallStep()
    throws MathUserException, IntegratorException {

    TestProblem1 pb = new TestProblem1();
    double step = (pb.getFinalTime() - pb.getInitialTime()) * 0.001;

    FirstOrderIntegrator integ = new ThreeEighthesIntegrator(step);
    TestProblemHandler handler = new TestProblemHandler(pb, integ);
    integ.addStepHandler(handler);
    integ.integrate(pb, pb.getInitialTime(), pb.getInitialState(),
                    pb.getFinalTime(), new double[pb.getDimension()]);

    assertTrue(handler.getLastError() < 2.0e-13);
    assertTrue(handler.getMaximalValueError() < 4.0e-12);
    assertEquals(0, handler.getMaximalTimeError(), 1.0e-12);
    assertEquals("3/8", integ.getName());

  }

// org.apache.commons.math.ode.nonstiff.ThreeEighthesIntegratorTest::testBigStep
  public void testBigStep()
    throws MathUserException, IntegratorException {

    TestProblem1 pb = new TestProblem1();
    double step = (pb.getFinalTime() - pb.getInitialTime()) * 0.2;

    FirstOrderIntegrator integ = new ThreeEighthesIntegrator(step);
    TestProblemHandler handler = new TestProblemHandler(pb, integ);
    integ.addStepHandler(handler);
    integ.integrate(pb, pb.getInitialTime(), pb.getInitialState(),
                    pb.getFinalTime(), new double[pb.getDimension()]);

    assertTrue(handler.getLastError() > 0.0004);
    assertTrue(handler.getMaximalValueError() > 0.005);
    assertEquals(0, handler.getMaximalTimeError(), 1.0e-12);

  }

// org.apache.commons.math.ode.nonstiff.ThreeEighthesIntegratorTest::testBackward
  public void testBackward()
      throws MathUserException, IntegratorException {

      TestProblem5 pb = new TestProblem5();
      double step = FastMath.abs(pb.getFinalTime() - pb.getInitialTime()) * 0.001;

      FirstOrderIntegrator integ = new ThreeEighthesIntegrator(step);
      TestProblemHandler handler = new TestProblemHandler(pb, integ);
      integ.addStepHandler(handler);
      integ.integrate(pb, pb.getInitialTime(), pb.getInitialState(),
                      pb.getFinalTime(), new double[pb.getDimension()]);

      assertTrue(handler.getLastError() < 5.0e-10);
      assertTrue(handler.getMaximalValueError() < 7.0e-10);
      assertEquals(0, handler.getMaximalTimeError(), 1.0e-12);
      assertEquals("3/8", integ.getName());
  }

// org.apache.commons.math.ode.nonstiff.ThreeEighthesIntegratorTest::testKepler
  public void testKepler()
    throws MathUserException, IntegratorException {

    final TestProblem3 pb  = new TestProblem3(0.9);
    double step = (pb.getFinalTime() - pb.getInitialTime()) * 0.0003;

    FirstOrderIntegrator integ = new ThreeEighthesIntegrator(step);
    integ.addStepHandler(new KeplerHandler(pb));
    integ.integrate(pb,
                    pb.getInitialTime(), pb.getInitialState(),
                    pb.getFinalTime(), new double[pb.getDimension()]);
  }

// org.apache.commons.math.ode.nonstiff.ThreeEighthesIntegratorTest::testStepSize
  public void testStepSize()
    throws MathUserException, IntegratorException {
      final double step = 1.23456;
      FirstOrderIntegrator integ = new ThreeEighthesIntegrator(step);
      integ.addStepHandler(new StepHandler() {
          public void handleStep(StepInterpolator interpolator, boolean isLast) {
              if (! isLast) {
                  assertEquals(step,
                               interpolator.getCurrentTime() - interpolator.getPreviousTime(),
                               1.0e-12);
              }
          }
          public boolean requiresDenseOutput() {
              return false;
          }
          public void reset() {
          }
      });
      integ.integrate(new FirstOrderDifferentialEquations() {
          public void computeDerivatives(double t, double[] y, double[] dot) {
              dot[0] = 1.0;
          }
          public int getDimension() {
              return 1;
          }
      }, 0.0, new double[] { 0.0 }, 5.0, new double[1]);
  }

// org.apache.commons.math.ode.nonstiff.ThreeEighthesStepInterpolatorTest::derivativesConsistency
  public void derivativesConsistency()
  throws MathUserException, IntegratorException {
    TestProblem3 pb = new TestProblem3();
    double step = (pb.getFinalTime() - pb.getInitialTime()) * 0.001;
    ThreeEighthesIntegrator integ = new ThreeEighthesIntegrator(step);
    StepInterpolatorTestUtils.checkDerivativesConsistency(integ, pb, 1.0e-10);
  }

// org.apache.commons.math.ode.nonstiff.ThreeEighthesStepInterpolatorTest::serialization
  public void serialization()
    throws MathUserException, IntegratorException,
           IOException, ClassNotFoundException {

    TestProblem3 pb = new TestProblem3(0.9);
    double step = (pb.getFinalTime() - pb.getInitialTime()) * 0.0003;
    ThreeEighthesIntegrator integ = new ThreeEighthesIntegrator(step);
    integ.addStepHandler(new ContinuousOutputModel());
    integ.integrate(pb,
                    pb.getInitialTime(), pb.getInitialState(),
                    pb.getFinalTime(), new double[pb.getDimension()]);

    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    ObjectOutputStream    oos = new ObjectOutputStream(bos);
    for (StepHandler handler : integ.getStepHandlers()) {
        oos.writeObject(handler);
    }

    assertTrue(bos.size () > 700000);
    assertTrue(bos.size () < 701000);

    ByteArrayInputStream  bis = new ByteArrayInputStream(bos.toByteArray());
    ObjectInputStream     ois = new ObjectInputStream(bis);
    ContinuousOutputModel cm  = (ContinuousOutputModel) ois.readObject();

    Random random = new Random(347588535632l);
    double maxError = 0.0;
    for (int i = 0; i < 1000; ++i) {
      double r = random.nextDouble();
      double time = r * pb.getInitialTime() + (1.0 - r) * pb.getFinalTime();
      cm.setInterpolatedTime(time);
      double[] interpolatedY = cm.getInterpolatedState ();
      double[] theoreticalY  = pb.computeTheoreticalState(time);
      double dx = interpolatedY[0] - theoreticalY[0];
      double dy = interpolatedY[1] - theoreticalY[1];
      double error = dx * dx + dy * dy;
      if (error > maxError) {
        maxError = error;
      }
    }

    assertTrue(maxError > 0.005);

  }

// org.apache.commons.math.ode.sampling.DummyStepInterpolatorTest::testNoReset
  public void testNoReset() throws MathUserException {

    double[]   y    =   { 0.0, 1.0, -2.0 };
    DummyStepInterpolator interpolator = new DummyStepInterpolator(y, new double[y.length], true);
    interpolator.storeTime(0);
    interpolator.shift();
    interpolator.storeTime(1);

    double[] result = interpolator.getInterpolatedState();
    for (int i = 0; i < result.length; ++i) {
      assertTrue(FastMath.abs(result[i] - y[i]) < 1.0e-10);
    }

  }

// org.apache.commons.math.ode.sampling.DummyStepInterpolatorTest::testFixedState
  public void testFixedState()
    throws MathUserException {

    double[]   y    =   { 1.0, 3.0, -4.0 };
    DummyStepInterpolator interpolator = new DummyStepInterpolator(y, new double[y.length], true);
    interpolator.storeTime(0);
    interpolator.shift();
    interpolator.storeTime(1);

    interpolator.setInterpolatedTime(0.1);
    double[] result = interpolator.getInterpolatedState();
    for (int i = 0; i < result.length; ++i) {
        assertTrue(FastMath.abs(result[i] - y[i]) < 1.0e-10);
    }

    interpolator.setInterpolatedTime(0.5);
    result = interpolator.getInterpolatedState();
    for (int i = 0; i < result.length; ++i) {
        assertTrue(FastMath.abs(result[i] - y[i]) < 1.0e-10);
    }

  }

// org.apache.commons.math.ode.sampling.DummyStepInterpolatorTest::testSerialization
  public void testSerialization()
  throws MathUserException, IOException, ClassNotFoundException {

    double[]   y    =   { 0.0, 1.0, -2.0 };
    DummyStepInterpolator interpolator = new DummyStepInterpolator(y, new double[y.length], true);
    interpolator.storeTime(0);
    interpolator.shift();
    interpolator.storeTime(1);

    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    ObjectOutputStream    oos = new ObjectOutputStream(bos);
    oos.writeObject(interpolator);

    assertTrue(bos.size () > 150);
    assertTrue(bos.size () < 250);

    ByteArrayInputStream  bis = new ByteArrayInputStream(bos.toByteArray());
    ObjectInputStream     ois = new ObjectInputStream(bis);
    DummyStepInterpolator dsi = (DummyStepInterpolator) ois.readObject();

    dsi.setInterpolatedTime(0.5);
    double[] result = dsi.getInterpolatedState();
    for (int i = 0; i < result.length; ++i) {
        assertTrue(FastMath.abs(result[i] - y[i]) < 1.0e-10);
    }

  }

// org.apache.commons.math.ode.sampling.DummyStepInterpolatorTest::testImpossibleSerialization
  public void testImpossibleSerialization()
  throws IOException {

    double[] y = { 0.0, 1.0, -2.0 };
    AbstractStepInterpolator interpolator = new BadStepInterpolator(y, true);
    interpolator.storeTime(0);
    interpolator.shift();
    interpolator.storeTime(1);

    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    ObjectOutputStream    oos = new ObjectOutputStream(bos);
    try {
        oos.writeObject(interpolator);
        fail("an exception should have been thrown");
    } catch (IOException ioe) {
        
        assertEquals(0, ioe.getMessage().length());
    }

  }

// org.apache.commons.math.ode.sampling.NordsieckStepInterpolatorTest::derivativesConsistency
    public void derivativesConsistency()
    throws MathUserException, IntegratorException {
        TestProblem3 pb = new TestProblem3();
        AdamsBashforthIntegrator integ = new AdamsBashforthIntegrator(4, 0.0, 1.0, 1.0e-10, 1.0e-10);
        StepInterpolatorTestUtils.checkDerivativesConsistency(integ, pb, 7e-10);
    }

// org.apache.commons.math.ode.sampling.NordsieckStepInterpolatorTest::serialization
    public void serialization()
    throws MathUserException, IntegratorException,
    IOException, ClassNotFoundException {

        TestProblem1 pb = new TestProblem1();
        AdamsBashforthIntegrator integ = new AdamsBashforthIntegrator(4, 0.0, 1.0, 1.0e-10, 1.0e-10);
        integ.addStepHandler(new ContinuousOutputModel());
        integ.integrate(pb,
                        pb.getInitialTime(), pb.getInitialState(),
                        pb.getFinalTime(), new double[pb.getDimension()]);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream    oos = new ObjectOutputStream(bos);
        for (StepHandler handler : integ.getStepHandlers()) {
            oos.writeObject(handler);
        }

        assertTrue(bos.size () >  20000);
        assertTrue(bos.size () <  25000);

        ByteArrayInputStream  bis = new ByteArrayInputStream(bos.toByteArray());
        ObjectInputStream     ois = new ObjectInputStream(bis);
        ContinuousOutputModel cm  = (ContinuousOutputModel) ois.readObject();

        Random random = new Random(347588535632l);
        double maxError = 0.0;
        for (int i = 0; i < 1000; ++i) {
            double r = random.nextDouble();
            double time = r * pb.getInitialTime() + (1.0 - r) * pb.getFinalTime();
            cm.setInterpolatedTime(time);
            double[] interpolatedY = cm.getInterpolatedState ();
            double[] theoreticalY  = pb.computeTheoreticalState(time);
            double dx = interpolatedY[0] - theoreticalY[0];
            double dy = interpolatedY[1] - theoreticalY[1];
            double error = dx * dx + dy * dy;
            if (error > maxError) {
                maxError = error;
            }
        }

        assertTrue(maxError < 1.0e-6);

    }

// org.apache.commons.math.ode.sampling.StepNormalizerTest::testBoundaries
  public void testBoundaries()
    throws MathUserException, IntegratorException {
    double range = pb.getFinalTime() - pb.getInitialTime();
    setLastSeen(false);
    integ.addStepHandler(new StepNormalizer(range / 10.0,
                                       new FixedStepHandler() {
                                        private static final long serialVersionUID = 1650337364641626444L;
                                        private boolean firstCall = true;
                                         public void handleStep(double t,
                                                                double[] y,
                                                                double[] yDot,
                                                                boolean isLast) {
                                           if (firstCall) {
                                             checkValue(t, pb.getInitialTime());
                                             firstCall = false;
                                           }
                                           if (isLast) {
                                             setLastSeen(true);
                                             checkValue(t, pb.getFinalTime());
                                           }
                                         }
                                       }));
    integ.integrate(pb,
                    pb.getInitialTime(), pb.getInitialState(),
                    pb.getFinalTime(), new double[pb.getDimension()]);
    assertTrue(lastSeen);
  }

// org.apache.commons.math.ode.sampling.StepNormalizerTest::testBeforeEnd
  public void testBeforeEnd()
    throws MathUserException, IntegratorException {
    final double range = pb.getFinalTime() - pb.getInitialTime();
    setLastSeen(false);
    integ.addStepHandler(new StepNormalizer(range / 10.5,
                                       new FixedStepHandler() {
                                        private static final long serialVersionUID = 2228457391561277298L;
                                        public void handleStep(double t,
                                                                double[] y,
                                                                double[] yDot,
                                                                boolean isLast) {
                                           if (isLast) {
                                             setLastSeen(true);
                                             checkValue(t,
                                                        pb.getFinalTime() - range / 21.0);
                                           }
                                         }
                                       }));
    integ.integrate(pb,
                    pb.getInitialTime(), pb.getInitialState(),
                    pb.getFinalTime(), new double[pb.getDimension()]);
    assertTrue(lastSeen);
  }

// org.apache.commons.math.optimization.MultiStartDifferentiableMultivariateRealOptimizerTest::testCircleFitting
    public void testCircleFitting() {
        Circle circle = new Circle();
        circle.addPoint( 30.0,  68.0);
        circle.addPoint( 50.0,  -6.0);
        circle.addPoint(110.0, -20.0);
        circle.addPoint( 35.0,  15.0);
        circle.addPoint( 45.0,  97.0);
        NonLinearConjugateGradientOptimizer underlying =
            new NonLinearConjugateGradientOptimizer(ConjugateGradientFormula.POLAK_RIBIERE);
        JDKRandomGenerator g = new JDKRandomGenerator();
        g.setSeed(753289573253l);
        RandomVectorGenerator generator =
            new UncorrelatedRandomVectorGenerator(new double[] { 50.0, 50.0 }, new double[] { 10.0, 10.0 },
                                                  new GaussianRandomGenerator(g));
        MultiStartDifferentiableMultivariateRealOptimizer optimizer =
            new MultiStartDifferentiableMultivariateRealOptimizer(underlying, 10, generator);
        optimizer.setConvergenceChecker(new SimpleScalarValueChecker(1.0e-10, 1.0e-10));
        BrentSolver solver = new BrentSolver();
        RealPointValuePair optimum =
            optimizer.optimize(200, circle, GoalType.MINIMIZE, new double[] { 98.680, 47.345 });
        assertEquals(200, optimizer.getMaxEvaluations());
        RealPointValuePair[] optima = optimizer.getOptima();
        for (RealPointValuePair o : optima) {
            Point2D.Double center = new Point2D.Double(o.getPointRef()[0], o.getPointRef()[1]);
            assertEquals(69.960161753, circle.getRadius(center), 1.0e-8);
            assertEquals(96.075902096, center.x, 1.0e-8);
            assertEquals(48.135167894, center.y, 1.0e-8);
        }
        assertTrue(optimizer.getEvaluations() > 70);
        assertTrue(optimizer.getEvaluations() < 90);
        assertEquals(3.1267527, optimum.getValue(), 1.0e-8);
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
            fail("an exception should have been thrown");
        } catch (MathIllegalStateException ise) {
            
        }
        VectorialPointValuePair optimum =
            optimizer.optimize(100, problem, problem.target, new double[] { 1 }, new double[] { 0 });
        assertEquals(1.5, optimum.getPoint()[0], 1.0e-10);
        assertEquals(3.0, optimum.getValue()[0], 1.0e-10);
        VectorialPointValuePair[] optima = optimizer.getOptima();
        assertEquals(10, optima.length);
        for (int i = 0; i < optima.length; ++i) {
            assertEquals(1.5, optima[i].getPoint()[0], 1.0e-10);
            assertEquals(3.0, optima[i].getValue()[0], 1.0e-10);
        }
        assertTrue(optimizer.getEvaluations() > 20);
        assertTrue(optimizer.getEvaluations() < 50);
        assertEquals(100, optimizer.getMaxEvaluations());
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
                    throw new MathUserException();
                }
            }, new double[] { 2 }, new double[] { 1 }, new double[] { 0 });
    }
