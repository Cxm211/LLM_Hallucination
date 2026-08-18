// buggy code
    public static boolean equals(double x, double y) {
        return (Double.isNaN(x) && Double.isNaN(y)) || x == y;
    }

// relevant test
// org.apache.commons.math.linear.BlockFieldMatrixTest::testOperate
    public void testOperate() {
        FieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(id);
        TestUtils.assertEquals(testVector, m.operate(testVector));
        TestUtils.assertEquals(testVector, m.operate(new ArrayFieldVector<Fraction>(testVector)).getData());
        m = new BlockFieldMatrix<Fraction>(bigSingular);
        try {
            m.operate(testVector);
            fail("Expecting illegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockFieldMatrixTest::testOperateLarge
    public void testOperateLarge() {
        int p = (11 * BlockFieldMatrix.BLOCK_SIZE) / 10;
        int q = (11 * BlockFieldMatrix.BLOCK_SIZE) / 10;
        int r =  BlockFieldMatrix.BLOCK_SIZE / 2;
        Random random = new Random(111007463902334l);
        FieldMatrix<Fraction> m1 = createRandomMatrix(random, p, q);
        FieldMatrix<Fraction> m2 = createRandomMatrix(random, q, r);
        FieldMatrix<Fraction> m1m2 = m1.multiply(m2);
        for (int i = 0; i < r; ++i) {
            TestUtils.assertEquals(m1m2.getColumn(i), m1.operate(m2.getColumn(i)));
        }
    }

// org.apache.commons.math.linear.BlockFieldMatrixTest::testOperatePremultiplyLarge
    public void testOperatePremultiplyLarge() {
        int p = (11 * BlockFieldMatrix.BLOCK_SIZE) / 10;
        int q = (11 * BlockFieldMatrix.BLOCK_SIZE) / 10;
        int r =  BlockFieldMatrix.BLOCK_SIZE / 2;
        Random random = new Random(111007463902334l);
        FieldMatrix<Fraction> m1 = createRandomMatrix(random, p, q);
        FieldMatrix<Fraction> m2 = createRandomMatrix(random, q, r);
        FieldMatrix<Fraction> m1m2 = m1.multiply(m2);
        for (int i = 0; i < p; ++i) {
            TestUtils.assertEquals(m1m2.getRow(i), m2.preMultiply(m1.getRow(i)));
        }
    }

// org.apache.commons.math.linear.BlockFieldMatrixTest::testMath209
    public void testMath209() {
        FieldMatrix<Fraction> a = new BlockFieldMatrix<Fraction>(new Fraction[][] {
                { new Fraction(1), new Fraction(2) },
                { new Fraction(3), new Fraction(4) },
                { new Fraction(5), new Fraction(6) }
        });
        Fraction[] b = a.operate(new Fraction[] { new Fraction(1), new Fraction(1) });
        assertEquals(a.getRowDimension(), b.length);
        assertEquals( new Fraction(3), b[0]);
        assertEquals( new Fraction(7), b[1]);
        assertEquals(new Fraction(11), b[2]);
    }

// org.apache.commons.math.linear.BlockFieldMatrixTest::testTranspose
    public void testTranspose() {
        FieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(testData);
        FieldMatrix<Fraction> mIT = new FieldLUDecompositionImpl<Fraction>(m).getSolver().getInverse().transpose();
        FieldMatrix<Fraction> mTI = new FieldLUDecompositionImpl<Fraction>(m.transpose()).getSolver().getInverse();
        TestUtils.assertEquals(mIT, mTI);
        m = new BlockFieldMatrix<Fraction>(testData2);
        FieldMatrix<Fraction> mt = new BlockFieldMatrix<Fraction>(testData2T);
        TestUtils.assertEquals(mt, m.transpose());
    }

// org.apache.commons.math.linear.BlockFieldMatrixTest::testPremultiplyVector
    public void testPremultiplyVector() {
        FieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(testData);
        TestUtils.assertEquals(m.preMultiply(testVector), preMultTest);
        TestUtils.assertEquals(m.preMultiply(new ArrayFieldVector<Fraction>(testVector).getData()),
                               preMultTest);
        m = new BlockFieldMatrix<Fraction>(bigSingular);
        try {
            m.preMultiply(testVector);
            fail("expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockFieldMatrixTest::testPremultiply
    public void testPremultiply() {
        FieldMatrix<Fraction> m3 = new BlockFieldMatrix<Fraction>(d3);
        FieldMatrix<Fraction> m4 = new BlockFieldMatrix<Fraction>(d4);
        FieldMatrix<Fraction> m5 = new BlockFieldMatrix<Fraction>(d5);
        TestUtils.assertEquals(m4.preMultiply(m3), m5);

        BlockFieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(testData);
        BlockFieldMatrix<Fraction> mInv = new BlockFieldMatrix<Fraction>(testDataInv);
        BlockFieldMatrix<Fraction> identity = new BlockFieldMatrix<Fraction>(id);
        TestUtils.assertEquals(m.preMultiply(mInv), identity);
        TestUtils.assertEquals(mInv.preMultiply(m), identity);
        TestUtils.assertEquals(m.preMultiply(identity), m);
        TestUtils.assertEquals(identity.preMultiply(mInv), mInv);
        try {
            m.preMultiply(new BlockFieldMatrix<Fraction>(bigSingular));
            fail("Expecting illegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockFieldMatrixTest::testGetVectors
    public void testGetVectors() {
        FieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(testData);
        TestUtils.assertEquals(m.getRow(0), testDataRow1);
        TestUtils.assertEquals(m.getColumn(2), testDataCol3);
        try {
            m.getRow(10);
            fail("expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.getColumn(-1);
            fail("expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockFieldMatrixTest::testGetEntry
    public void testGetEntry() {
        FieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(testData);
        assertEquals(m.getEntry(0,1),new Fraction(2));
        try {
            m.getEntry(10, 4);
            fail ("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockFieldMatrixTest::testExamples
    public void testExamples() {
        
        Fraction[][] matrixData = {
                {new Fraction(1),new Fraction(2),new Fraction(3)},
                {new Fraction(2),new Fraction(5),new Fraction(3)}
        };
        FieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(matrixData);
        
        Fraction[][] matrixData2 = {
                {new Fraction(1),new Fraction(2)},
                {new Fraction(2),new Fraction(5)},
                {new Fraction(1), new Fraction(7)}
        };
        FieldMatrix<Fraction> n = new BlockFieldMatrix<Fraction>(matrixData2);
        
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
        FieldMatrix<Fraction> coefficients = new BlockFieldMatrix<Fraction>(coefficientsData);
        Fraction[] constants = {new Fraction(1), new Fraction(-2), new Fraction(1)};
        Fraction[] solution = new FieldLUDecompositionImpl<Fraction>(coefficients).getSolver().solve(constants);
        assertEquals(new Fraction(2).multiply(solution[0]).
                     add(new Fraction(3).multiply(solution[1])).
                     subtract(new Fraction(2).multiply(solution[2])),
                     constants[0]);
        assertEquals(new Fraction(-1).multiply(solution[0]).
                     add(new Fraction(7).multiply(solution[1])).
                     add(new Fraction(6).multiply(solution[2])),
                     constants[1]);
        assertEquals(new Fraction(4).multiply(solution[0]).
                     subtract(new Fraction(3).multiply(solution[1])).
                     subtract(new Fraction(5).multiply(solution[2])),
                     constants[2]);

    }

// org.apache.commons.math.linear.BlockFieldMatrixTest::testGetSubMatrix
    public void testGetSubMatrix() {
        FieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(subTestData);
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

// org.apache.commons.math.linear.BlockFieldMatrixTest::testGetSetMatrixLarge
    public void testGetSetMatrixLarge() {
        int n = 3 * BlockFieldMatrix.BLOCK_SIZE;
        FieldMatrix<Fraction> m =
            new BlockFieldMatrix<Fraction>(FractionField.getInstance(), n, n);
        FieldMatrix<Fraction> sub =
            new BlockFieldMatrix<Fraction>(FractionField.getInstance(), n - 4, n - 4).scalarAdd(new Fraction(1));

        m.setSubMatrix(sub.getData(), 2, 2);
        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < n; ++j) {
                if ((i < 2) || (i > n - 3) || (j < 2) || (j > n - 3)) {
                    assertEquals(new Fraction(0), m.getEntry(i, j));
                } else {
                    assertEquals(new Fraction(1), m.getEntry(i, j));
                }
            }
        }
        assertEquals(sub, m.getSubMatrix(2, n - 3, 2, n - 3));

    }

// org.apache.commons.math.linear.BlockFieldMatrixTest::testCopySubMatrix
    public void testCopySubMatrix() {
        FieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(subTestData);
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

// org.apache.commons.math.linear.BlockFieldMatrixTest::testGetRowMatrix
    public void testGetRowMatrix() {
        FieldMatrix<Fraction> m     = new BlockFieldMatrix<Fraction>(subTestData);
        FieldMatrix<Fraction> mRow0 = new BlockFieldMatrix<Fraction>(subRow0);
        FieldMatrix<Fraction> mRow3 = new BlockFieldMatrix<Fraction>(subRow3);
        assertEquals("Row0", mRow0, m.getRowMatrix(0));
        assertEquals("Row3", mRow3, m.getRowMatrix(3));
        try {
            m.getRowMatrix(-1);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.getRowMatrix(4);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockFieldMatrixTest::testSetRowMatrix
    public void testSetRowMatrix() {
        FieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(subTestData);
        FieldMatrix<Fraction> mRow3 = new BlockFieldMatrix<Fraction>(subRow3);
        assertNotSame(mRow3, m.getRowMatrix(0));
        m.setRowMatrix(0, mRow3);
        assertEquals(mRow3, m.getRowMatrix(0));
        try {
            m.setRowMatrix(-1, mRow3);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.setRowMatrix(0, m);
            fail("Expecting InvalidMatrixException");
        } catch (InvalidMatrixException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockFieldMatrixTest::testGetSetRowMatrixLarge
    public void testGetSetRowMatrixLarge() {
        int n = 3 * BlockFieldMatrix.BLOCK_SIZE;
        FieldMatrix<Fraction> m =
            new BlockFieldMatrix<Fraction>(FractionField.getInstance(), n, n);
        FieldMatrix<Fraction> sub =
            new BlockFieldMatrix<Fraction>(FractionField.getInstance(), 1, n).scalarAdd(new Fraction(1));

        m.setRowMatrix(2, sub);
        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < n; ++j) {
                if (i != 2) {
                    assertEquals(new Fraction(0), m.getEntry(i, j));
                } else {
                    assertEquals(new Fraction(1), m.getEntry(i, j));
                }
            }
        }
        assertEquals(sub, m.getRowMatrix(2));

    }

// org.apache.commons.math.linear.BlockFieldMatrixTest::testGetColumnMatrix
    public void testGetColumnMatrix() {
        FieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(subTestData);
        FieldMatrix<Fraction> mColumn1 = new BlockFieldMatrix<Fraction>(subColumn1);
        FieldMatrix<Fraction> mColumn3 = new BlockFieldMatrix<Fraction>(subColumn3);
        assertEquals(mColumn1, m.getColumnMatrix(1));
        assertEquals(mColumn3, m.getColumnMatrix(3));
        try {
            m.getColumnMatrix(-1);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.getColumnMatrix(4);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockFieldMatrixTest::testSetColumnMatrix
    public void testSetColumnMatrix() {
        FieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(subTestData);
        FieldMatrix<Fraction> mColumn3 = new BlockFieldMatrix<Fraction>(subColumn3);
        assertNotSame(mColumn3, m.getColumnMatrix(1));
        m.setColumnMatrix(1, mColumn3);
        assertEquals(mColumn3, m.getColumnMatrix(1));
        try {
            m.setColumnMatrix(-1, mColumn3);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.setColumnMatrix(0, m);
            fail("Expecting InvalidMatrixException");
        } catch (InvalidMatrixException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockFieldMatrixTest::testGetSetColumnMatrixLarge
    public void testGetSetColumnMatrixLarge() {
        int n = 3 * BlockFieldMatrix.BLOCK_SIZE;
        FieldMatrix<Fraction> m =
            new BlockFieldMatrix<Fraction>(FractionField.getInstance(), n, n);
        FieldMatrix<Fraction> sub =
            new BlockFieldMatrix<Fraction>(FractionField.getInstance(), n, 1).scalarAdd(new Fraction(1));

        m.setColumnMatrix(2, sub);
        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < n; ++j) {
                if (j != 2) {
                    assertEquals(new Fraction(0), m.getEntry(i, j));
                } else {
                    assertEquals(new Fraction(1), m.getEntry(i, j));
                }
            }
        }
        assertEquals(sub, m.getColumnMatrix(2));

    }

// org.apache.commons.math.linear.BlockFieldMatrixTest::testGetRowVector
    public void testGetRowVector() {
        FieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(subTestData);
        FieldVector<Fraction> mRow0 = new ArrayFieldVector<Fraction>(subRow0[0]);
        FieldVector<Fraction> mRow3 = new ArrayFieldVector<Fraction>(subRow3[0]);
        assertEquals(mRow0, m.getRowVector(0));
        assertEquals(mRow3, m.getRowVector(3));
        try {
            m.getRowVector(-1);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.getRowVector(4);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockFieldMatrixTest::testSetRowVector
    public void testSetRowVector() {
        FieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(subTestData);
        FieldVector<Fraction> mRow3 = new ArrayFieldVector<Fraction>(subRow3[0]);
        assertNotSame(mRow3, m.getRowMatrix(0));
        m.setRowVector(0, mRow3);
        assertEquals(mRow3, m.getRowVector(0));
        try {
            m.setRowVector(-1, mRow3);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.setRowVector(0, new ArrayFieldVector<Fraction>(FractionField.getInstance(), 5));
            fail("Expecting InvalidMatrixException");
        } catch (InvalidMatrixException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockFieldMatrixTest::testGetSetRowVectorLarge
    public void testGetSetRowVectorLarge() {
        int n = 3 * BlockFieldMatrix.BLOCK_SIZE;
        FieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(FractionField.getInstance(), n, n);
        FieldVector<Fraction> sub = new ArrayFieldVector<Fraction>(n, new Fraction(1));

        m.setRowVector(2, sub);
        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < n; ++j) {
                if (i != 2) {
                    assertEquals(new Fraction(0), m.getEntry(i, j));
                } else {
                    assertEquals(new Fraction(1), m.getEntry(i, j));
                }
            }
        }
        assertEquals(sub, m.getRowVector(2));

    }

// org.apache.commons.math.linear.BlockFieldMatrixTest::testGetColumnVector
    public void testGetColumnVector() {
        FieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(subTestData);
        FieldVector<Fraction> mColumn1 = columnToVector(subColumn1);
        FieldVector<Fraction> mColumn3 = columnToVector(subColumn3);
        assertEquals(mColumn1, m.getColumnVector(1));
        assertEquals(mColumn3, m.getColumnVector(3));
        try {
            m.getColumnVector(-1);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.getColumnVector(4);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockFieldMatrixTest::testSetColumnVector
    public void testSetColumnVector() {
        FieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(subTestData);
        FieldVector<Fraction> mColumn3 = columnToVector(subColumn3);
        assertNotSame(mColumn3, m.getColumnVector(1));
        m.setColumnVector(1, mColumn3);
        assertEquals(mColumn3, m.getColumnVector(1));
        try {
            m.setColumnVector(-1, mColumn3);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.setColumnVector(0, new ArrayFieldVector<Fraction>(FractionField.getInstance(), 5));
            fail("Expecting InvalidMatrixException");
        } catch (InvalidMatrixException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockFieldMatrixTest::testGetSetColumnVectorLarge
    public void testGetSetColumnVectorLarge() {
        int n = 3 * BlockFieldMatrix.BLOCK_SIZE;
        FieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(FractionField.getInstance(), n, n);
        FieldVector<Fraction> sub = new ArrayFieldVector<Fraction>(n, new Fraction(1));

        m.setColumnVector(2, sub);
        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < n; ++j) {
                if (j != 2) {
                    assertEquals(new Fraction(0), m.getEntry(i, j));
                } else {
                    assertEquals(new Fraction(1), m.getEntry(i, j));
                }
            }
        }
        assertEquals(sub, m.getColumnVector(2));

    }

// org.apache.commons.math.linear.BlockFieldMatrixTest::testGetRow
    public void testGetRow() {
        FieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(subTestData);
        checkArrays(subRow0[0], m.getRow(0));
        checkArrays(subRow3[0], m.getRow(3));
        try {
            m.getRow(-1);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.getRow(4);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockFieldMatrixTest::testSetRow
    public void testSetRow() {
        FieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(subTestData);
        assertTrue(subRow3[0][0] != m.getRow(0)[0]);
        m.setRow(0, subRow3[0]);
        checkArrays(subRow3[0], m.getRow(0));
        try {
            m.setRow(-1, subRow3[0]);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.setRow(0, new Fraction[5]);
            fail("Expecting InvalidMatrixException");
        } catch (InvalidMatrixException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockFieldMatrixTest::testGetSetRowLarge
    public void testGetSetRowLarge() {
        int n = 3 * BlockFieldMatrix.BLOCK_SIZE;
        FieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(FractionField.getInstance(), n, n);
        Fraction[] sub = new Fraction[n];
        Arrays.fill(sub, new Fraction(1));

        m.setRow(2, sub);
        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < n; ++j) {
                if (i != 2) {
                    assertEquals(new Fraction(0), m.getEntry(i, j));
                } else {
                    assertEquals(new Fraction(1), m.getEntry(i, j));
                }
            }
        }
        checkArrays(sub, m.getRow(2));

    }

// org.apache.commons.math.linear.BlockFieldMatrixTest::testGetColumn
    public void testGetColumn() {
        FieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(subTestData);
        Fraction[] mColumn1 = columnToArray(subColumn1);
        Fraction[] mColumn3 = columnToArray(subColumn3);
        checkArrays(mColumn1, m.getColumn(1));
        checkArrays(mColumn3, m.getColumn(3));
        try {
            m.getColumn(-1);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.getColumn(4);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockFieldMatrixTest::testSetColumn
    public void testSetColumn() {
        FieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(subTestData);
        Fraction[] mColumn3 = columnToArray(subColumn3);
        assertTrue(mColumn3[0] != m.getColumn(1)[0]);
        m.setColumn(1, mColumn3);
        checkArrays(mColumn3, m.getColumn(1));
        try {
            m.setColumn(-1, mColumn3);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.setColumn(0, new Fraction[5]);
            fail("Expecting InvalidMatrixException");
        } catch (InvalidMatrixException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockFieldMatrixTest::testGetSetColumnLarge
    public void testGetSetColumnLarge() {
        int n = 3 * BlockFieldMatrix.BLOCK_SIZE;
        FieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(FractionField.getInstance(), n, n);
        Fraction[] sub = new Fraction[n];
        Arrays.fill(sub, new Fraction(1));

        m.setColumn(2, sub);
        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < n; ++j) {
                if (j != 2) {
                    assertEquals(new Fraction(0), m.getEntry(i, j));
                } else {
                    assertEquals(new Fraction(1), m.getEntry(i, j));
                }
            }
        }
        checkArrays(sub, m.getColumn(2));

    }

// org.apache.commons.math.linear.BlockFieldMatrixTest::testEqualsAndHashCode
    public void testEqualsAndHashCode() {
        BlockFieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(testData);
        BlockFieldMatrix<Fraction> m1 = (BlockFieldMatrix<Fraction>) m.copy();
        BlockFieldMatrix<Fraction> mt = (BlockFieldMatrix<Fraction>) m.transpose();
        assertTrue(m.hashCode() != mt.hashCode());
        assertEquals(m.hashCode(), m1.hashCode());
        assertEquals(m, m);
        assertEquals(m, m1);
        assertFalse(m.equals(null));
        assertFalse(m.equals(mt));
        assertFalse(m.equals(new BlockFieldMatrix<Fraction>(bigSingular)));
    }

// org.apache.commons.math.linear.BlockFieldMatrixTest::testToString
    public void testToString() {
        BlockFieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(testData);
        assertEquals("BlockFieldMatrix{{1,2,3},{2,5,3},{1,0,8}}", m.toString());
    }

// org.apache.commons.math.linear.BlockFieldMatrixTest::testSetSubMatrix
    public void testSetSubMatrix() throws Exception {
        BlockFieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(testData);
        m.setSubMatrix(detData2,1,1);
        FieldMatrix<Fraction> expected = new BlockFieldMatrix<Fraction>
            (new Fraction[][] {{new Fraction(1),new Fraction(2),new Fraction(3)},{new Fraction(2),new Fraction(1),new Fraction(3)},{new Fraction(1),new Fraction(2),new Fraction(4)}});
        assertEquals(expected, m);

        m.setSubMatrix(detData2,0,0);
        expected = new BlockFieldMatrix<Fraction>
            (new Fraction[][] {{new Fraction(1),new Fraction(3),new Fraction(3)},{new Fraction(2),new Fraction(4),new Fraction(3)},{new Fraction(1),new Fraction(2),new Fraction(4)}});
        assertEquals(expected, m);

        m.setSubMatrix(testDataPlus2,0,0);
        expected = new BlockFieldMatrix<Fraction>
            (new Fraction[][] {{new Fraction(3),new Fraction(4),new Fraction(5)},{new Fraction(4),new Fraction(7),new Fraction(5)},{new Fraction(3),new Fraction(2),new Fraction(10)}});
        assertEquals(expected, m);

        
        BlockFieldMatrix<Fraction> matrix =
            new BlockFieldMatrix<Fraction>(new Fraction[][] {
                    {new Fraction(1), new Fraction(2), new Fraction(3), new Fraction(4)},
                    {new Fraction(5), new Fraction(6), new Fraction(7), new Fraction(8)},
                    {new Fraction(9), new Fraction(0), new Fraction(1) , new Fraction(2)}
            });
        matrix.setSubMatrix(new Fraction[][] {
                {new Fraction(3), new Fraction(4)},
                {new Fraction(5), new Fraction(6)}
        }, 1, 1);
        expected =
            new BlockFieldMatrix<Fraction>(new Fraction[][] {
                    {new Fraction(1), new Fraction(2), new Fraction(3),new Fraction(4)},
                    {new Fraction(5), new Fraction(3), new Fraction(4), new Fraction(8)},
                    {new Fraction(9), new Fraction(5) ,new Fraction(6), new Fraction(2)}
            });
        assertEquals(expected, matrix);

        
        try {
            m.setSubMatrix(testData,1,1);
            fail("expecting MatrixIndexException");
        } catch (MatrixIndexException e) {
            
        }
        
        try {
            m.setSubMatrix(testData,-1,1);
            fail("expecting MatrixIndexException");
        } catch (MatrixIndexException e) {
            
        }
        try {
            m.setSubMatrix(testData,1,-1);
            fail("expecting MatrixIndexException");
        } catch (MatrixIndexException e) {
            
        }

        
        try {
            m.setSubMatrix(null,1,1);
            fail("expecting NullPointerException");
        } catch (NullPointerException e) {
            
        }

        
        try {
            m.setSubMatrix(new Fraction[][] {{new Fraction(1)}, {new Fraction(2), new Fraction(3)}}, 0, 0);
            fail("expecting IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            
        }

        
        try {
            m.setSubMatrix(new Fraction[][] {{}}, 0, 0);
            fail("expecting IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            
        }

    }

// org.apache.commons.math.linear.BlockFieldMatrixTest::testWalk
    public void testWalk() {
        int rows    = 150;
        int columns = 75;

        FieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(FractionField.getInstance(), rows, columns);
        m.walkInRowOrder(new SetVisitor());
        GetVisitor getVisitor = new GetVisitor();
        m.walkInOptimizedOrder(getVisitor);
        assertEquals(rows * columns, getVisitor.getCount());

        m = new BlockFieldMatrix<Fraction>(FractionField.getInstance(), rows, columns);
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

        m = new BlockFieldMatrix<Fraction>(FractionField.getInstance(), rows, columns);
        m.walkInColumnOrder(new SetVisitor());
        getVisitor = new GetVisitor();
        m.walkInOptimizedOrder(getVisitor);
        assertEquals(rows * columns, getVisitor.getCount());

        m = new BlockFieldMatrix<Fraction>(FractionField.getInstance(), rows, columns);
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

        m = new BlockFieldMatrix<Fraction>(FractionField.getInstance(), rows, columns);
        m.walkInOptimizedOrder(new SetVisitor());
        getVisitor = new GetVisitor();
        m.walkInRowOrder(getVisitor);
        assertEquals(rows * columns, getVisitor.getCount());

        m = new BlockFieldMatrix<Fraction>(FractionField.getInstance(), rows, columns);
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

        m = new BlockFieldMatrix<Fraction>(FractionField.getInstance(), rows, columns);
        m.walkInOptimizedOrder(new SetVisitor());
        getVisitor = new GetVisitor();
        m.walkInColumnOrder(getVisitor);
        assertEquals(rows * columns, getVisitor.getCount());

        m = new BlockFieldMatrix<Fraction>(FractionField.getInstance(), rows, columns);
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

// org.apache.commons.math.linear.BlockFieldMatrixTest::testSerial
    public void testSerial()  {
        BlockFieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(testData);
        assertEquals(m,TestUtils.serializeAndRecover(m));
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testDimensions
    public void testDimensions() {
        BlockRealMatrix m = new BlockRealMatrix(testData);
        BlockRealMatrix m2 = new BlockRealMatrix(testData2);
        assertEquals("testData row dimension",3,m.getRowDimension());
        assertEquals("testData column dimension",3,m.getColumnDimension());
        assertTrue("testData is square",m.isSquare());
        assertEquals("testData2 row dimension",m2.getRowDimension(),2);
        assertEquals("testData2 column dimension",m2.getColumnDimension(),3);
        assertTrue("testData2 is not square",!m2.isSquare());
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testCopyFunctions
    public void testCopyFunctions() {
        Random r = new Random(66636328996002l);
        BlockRealMatrix m1 = createRandomMatrix(r, 47, 83);
        BlockRealMatrix m2 = new BlockRealMatrix(m1.getData());
        assertEquals(m1, m2);
        BlockRealMatrix m3 = new BlockRealMatrix(testData);
        BlockRealMatrix m4 = new BlockRealMatrix(m3.getData());
        assertEquals(m3, m4);
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testAdd
    public void testAdd() {
        BlockRealMatrix m = new BlockRealMatrix(testData);
        BlockRealMatrix mInv = new BlockRealMatrix(testDataInv);
        RealMatrix mPlusMInv = m.add(mInv);
        double[][] sumEntries = mPlusMInv.getData();
        for (int row = 0; row < m.getRowDimension(); row++) {
            for (int col = 0; col < m.getColumnDimension(); col++) {
                assertEquals("sum entry entry",
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
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testNorm
    public void testNorm() {
        BlockRealMatrix m = new BlockRealMatrix(testData);
        BlockRealMatrix m2 = new BlockRealMatrix(testData2);
        assertEquals("testData norm",14d,m.getNorm(),entryTolerance);
        assertEquals("testData2 norm",7d,m2.getNorm(),entryTolerance);
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testFrobeniusNorm
    public void testFrobeniusNorm() {
        BlockRealMatrix m = new BlockRealMatrix(testData);
        BlockRealMatrix m2 = new BlockRealMatrix(testData2);
        assertEquals("testData Frobenius norm", FastMath.sqrt(117.0), m.getFrobeniusNorm(), entryTolerance);
        assertEquals("testData2 Frobenius norm", FastMath.sqrt(52.0), m2.getFrobeniusNorm(), entryTolerance);
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testPlusMinus
    public void testPlusMinus() {
        BlockRealMatrix m = new BlockRealMatrix(testData);
        BlockRealMatrix m2 = new BlockRealMatrix(testDataInv);
        assertClose(m.subtract(m2), m2.scalarMultiply(-1d).add(m), entryTolerance);
        try {
            m.subtract(new BlockRealMatrix(testData2));
            fail("Expecting illegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
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
            fail("Expecting illegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
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
        assertEquals(m.getRowDimension(), mT.getColumnDimension());
        assertEquals(m.getColumnDimension(), mT.getRowDimension());
        for (int i = 0; i < mT.getRowDimension(); ++i) {
            for (int j = 0; j < mT.getColumnDimension(); ++j) {
                assertEquals(m.getEntry(j, i), mT.getEntry(i, j), 0);
            }
        }

        RealMatrix mPm = m.add(m);
        for (int i = 0; i < mPm.getRowDimension(); ++i) {
            for (int j = 0; j < mPm.getColumnDimension(); ++j) {
                assertEquals(2 * m.getEntry(i, j), mPm.getEntry(i, j), 0);
            }
        }

        RealMatrix mPmMm = mPm.subtract(m);
        for (int i = 0; i < mPmMm.getRowDimension(); ++i) {
            for (int j = 0; j < mPmMm.getColumnDimension(); ++j) {
                assertEquals(m.getEntry(i, j), mPmMm.getEntry(i, j), 0);
            }
        }

        RealMatrix mTm = mT.multiply(m);
        for (int i = 0; i < mTm.getRowDimension(); ++i) {
            for (int j = 0; j < mTm.getColumnDimension(); ++j) {
                double sum = 0;
                for (int k = 0; k < mT.getColumnDimension(); ++k) {
                    sum += (k + i / 1024.0) * (k + j / 1024.0);
                }
                assertEquals(sum, mTm.getEntry(i, j), 0);
            }
        }

        RealMatrix mmT = m.multiply(mT);
        for (int i = 0; i < mmT.getRowDimension(); ++i) {
            for (int j = 0; j < mmT.getColumnDimension(); ++j) {
                double sum = 0;
                for (int k = 0; k < m.getColumnDimension(); ++k) {
                    sum += (i + k / 1024.0) * (j + k / 1024.0);
                }
                assertEquals(sum, mmT.getEntry(i, j), 0);
            }
        }

        RealMatrix sub1 = m.getSubMatrix(2, 9, 5, 20);
        for (int i = 0; i < sub1.getRowDimension(); ++i) {
            for (int j = 0; j < sub1.getColumnDimension(); ++j) {
                assertEquals((i + 2) + (j + 5) / 1024.0, sub1.getEntry(i, j), 0);
            }
        }

        RealMatrix sub2 = m.getSubMatrix(10, 12, 3, 70);
        for (int i = 0; i < sub2.getRowDimension(); ++i) {
            for (int j = 0; j < sub2.getColumnDimension(); ++j) {
                assertEquals((i + 10) + (j + 3) / 1024.0, sub2.getEntry(i, j), 0);
            }
        }

        RealMatrix sub3 = m.getSubMatrix(30, 34, 0, 5);
        for (int i = 0; i < sub3.getRowDimension(); ++i) {
            for (int j = 0; j < sub3.getColumnDimension(); ++j) {
                assertEquals((i + 30) + (j + 0) / 1024.0, sub3.getEntry(i, j), 0);
            }
        }

        RealMatrix sub4 = m.getSubMatrix(30, 32, 62, 65);
        for (int i = 0; i < sub4.getRowDimension(); ++i) {
            for (int j = 0; j < sub4.getColumnDimension(); ++j) {
                assertEquals((i + 30) + (j + 62) / 1024.0, sub4.getEntry(i, j), 0);
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
        assertEquals("identity trace",3d,m.getTrace(),entryTolerance);
        m = new BlockRealMatrix(testData2);
        try {
            m.getTrace();
            fail("Expecting NonSquareMatrixException");
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
        assertClose(testVector, m.operate(new ArrayRealVector(testVector)).getData(), entryTolerance);
        m = new BlockRealMatrix(bigSingular);
        try {
            m.operate(testVector);
            fail("Expecting illegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
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
        assertEquals(a.getRowDimension(), b.length);
        assertEquals( 3.0, b[0], 1.0e-12);
        assertEquals( 7.0, b[1], 1.0e-12);
        assertEquals(11.0, b[2], 1.0e-12);
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testTranspose
    public void testTranspose() {
        RealMatrix m = new BlockRealMatrix(testData);
        RealMatrix mIT = new LUDecompositionImpl(m).getSolver().getInverse().transpose();
        RealMatrix mTI = new LUDecompositionImpl(m.transpose()).getSolver().getInverse();
        assertClose(mIT, mTI, normTolerance);
        m = new BlockRealMatrix(testData2);
        RealMatrix mt = new BlockRealMatrix(testData2T);
        assertClose(mt, m.transpose(), normTolerance);
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testPremultiplyVector
    public void testPremultiplyVector() {
        RealMatrix m = new BlockRealMatrix(testData);
        assertClose(m.preMultiply(testVector), preMultTest, normTolerance);
        assertClose(m.preMultiply(new ArrayRealVector(testVector).getData()),
                    preMultTest, normTolerance);
        m = new BlockRealMatrix(bigSingular);
        try {
            m.preMultiply(testVector);
            fail("expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
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
            fail("Expecting illegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testGetVectors
    public void testGetVectors() {
        RealMatrix m = new BlockRealMatrix(testData);
        assertClose(m.getRow(0), testDataRow1, entryTolerance);
        assertClose(m.getColumn(2), testDataCol3, entryTolerance);
        try {
            m.getRow(10);
            fail("expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.getColumn(-1);
            fail("expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testGetEntry
    public void testGetEntry() {
        RealMatrix m = new BlockRealMatrix(testData);
        assertEquals("get entry",m.getEntry(0,1),2d,entryTolerance);
        try {
            m.getEntry(10, 4);
            fail ("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testExamples
    public void testExamples() {
        
        double[][] matrixData = { {1d,2d,3d}, {2d,5d,3d}};
        RealMatrix m = new BlockRealMatrix(matrixData);
        
        double[][] matrixData2 = { {1d,2d}, {2d,5d}, {1d, 7d}};
        RealMatrix n = new BlockRealMatrix(matrixData2);
        
        RealMatrix p = m.multiply(n);
        assertEquals(2, p.getRowDimension());
        assertEquals(2, p.getColumnDimension());
        
        RealMatrix pInverse = new LUDecompositionImpl(p).getSolver().getInverse();
        assertEquals(2, pInverse.getRowDimension());
        assertEquals(2, pInverse.getColumnDimension());

        
        double[][] coefficientsData = {{2, 3, -2}, {-1, 7, 6}, {4, -3, -5}};
        RealMatrix coefficients = new BlockRealMatrix(coefficientsData);
        double[] constants = {1, -2, 1};
        double[] solution = new LUDecompositionImpl(coefficients).getSolver().solve(constants);
        assertEquals(2 * solution[0] + 3 * solution[1] -2 * solution[2], constants[0], 1E-12);
        assertEquals(-1 * solution[0] + 7 * solution[1] + 6 * solution[2], constants[1], 1E-12);
        assertEquals(4 * solution[0] - 3 * solution[1] -5 * solution[2], constants[2], 1E-12);

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
                    assertEquals(0.0, m.getEntry(i, j), 0.0);
                } else {
                    assertEquals(1.0, m.getEntry(i, j), 0.0);
                }
            }
        }
        assertEquals(sub, m.getSubMatrix(2, n - 3, 2, n - 3));

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
        assertEquals("Row0", mRow0, m.getRowMatrix(0));
        assertEquals("Row3", mRow3, m.getRowMatrix(3));
        try {
            m.getRowMatrix(-1);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.getRowMatrix(4);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testSetRowMatrix
    public void testSetRowMatrix() {
        RealMatrix m = new BlockRealMatrix(subTestData);
        RealMatrix mRow3 = new BlockRealMatrix(subRow3);
        assertNotSame(mRow3, m.getRowMatrix(0));
        m.setRowMatrix(0, mRow3);
        assertEquals(mRow3, m.getRowMatrix(0));
        try {
            m.setRowMatrix(-1, mRow3);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.setRowMatrix(0, m);
            fail("Expecting InvalidMatrixException");
        } catch (InvalidMatrixException ex) {
            
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
                    assertEquals(0.0, m.getEntry(i, j), 0.0);
                } else {
                    assertEquals(1.0, m.getEntry(i, j), 0.0);
                }
            }
        }
        assertEquals(sub, m.getRowMatrix(2));

    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testGetColumnMatrix
    public void testGetColumnMatrix() {
        RealMatrix m = new BlockRealMatrix(subTestData);
        RealMatrix mColumn1 = new BlockRealMatrix(subColumn1);
        RealMatrix mColumn3 = new BlockRealMatrix(subColumn3);
        assertEquals(mColumn1, m.getColumnMatrix(1));
        assertEquals(mColumn3, m.getColumnMatrix(3));
        try {
            m.getColumnMatrix(-1);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.getColumnMatrix(4);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testSetColumnMatrix
    public void testSetColumnMatrix() {
        RealMatrix m = new BlockRealMatrix(subTestData);
        RealMatrix mColumn3 = new BlockRealMatrix(subColumn3);
        assertNotSame(mColumn3, m.getColumnMatrix(1));
        m.setColumnMatrix(1, mColumn3);
        assertEquals(mColumn3, m.getColumnMatrix(1));
        try {
            m.setColumnMatrix(-1, mColumn3);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.setColumnMatrix(0, m);
            fail("Expecting InvalidMatrixException");
        } catch (InvalidMatrixException ex) {
            
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
                    assertEquals(0.0, m.getEntry(i, j), 0.0);
                } else {
                    assertEquals(1.0, m.getEntry(i, j), 0.0);
                }
            }
        }
        assertEquals(sub, m.getColumnMatrix(2));

    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testGetRowVector
    public void testGetRowVector() {
        RealMatrix m = new BlockRealMatrix(subTestData);
        RealVector mRow0 = new ArrayRealVector(subRow0[0]);
        RealVector mRow3 = new ArrayRealVector(subRow3[0]);
        assertEquals(mRow0, m.getRowVector(0));
        assertEquals(mRow3, m.getRowVector(3));
        try {
            m.getRowVector(-1);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.getRowVector(4);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testSetRowVector
    public void testSetRowVector() {
        RealMatrix m = new BlockRealMatrix(subTestData);
        RealVector mRow3 = new ArrayRealVector(subRow3[0]);
        assertNotSame(mRow3, m.getRowMatrix(0));
        m.setRowVector(0, mRow3);
        assertEquals(mRow3, m.getRowVector(0));
        try {
            m.setRowVector(-1, mRow3);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.setRowVector(0, new ArrayRealVector(5));
            fail("Expecting InvalidMatrixException");
        } catch (InvalidMatrixException ex) {
            
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
                    assertEquals(0.0, m.getEntry(i, j), 0.0);
                } else {
                    assertEquals(1.0, m.getEntry(i, j), 0.0);
                }
            }
        }
        assertEquals(sub, m.getRowVector(2));

    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testGetColumnVector
    public void testGetColumnVector() {
        RealMatrix m = new BlockRealMatrix(subTestData);
        RealVector mColumn1 = columnToVector(subColumn1);
        RealVector mColumn3 = columnToVector(subColumn3);
        assertEquals(mColumn1, m.getColumnVector(1));
        assertEquals(mColumn3, m.getColumnVector(3));
        try {
            m.getColumnVector(-1);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.getColumnVector(4);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testSetColumnVector
    public void testSetColumnVector() {
        RealMatrix m = new BlockRealMatrix(subTestData);
        RealVector mColumn3 = columnToVector(subColumn3);
        assertNotSame(mColumn3, m.getColumnVector(1));
        m.setColumnVector(1, mColumn3);
        assertEquals(mColumn3, m.getColumnVector(1));
        try {
            m.setColumnVector(-1, mColumn3);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.setColumnVector(0, new ArrayRealVector(5));
            fail("Expecting InvalidMatrixException");
        } catch (InvalidMatrixException ex) {
            
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
                    assertEquals(0.0, m.getEntry(i, j), 0.0);
                } else {
                    assertEquals(1.0, m.getEntry(i, j), 0.0);
                }
            }
        }
        assertEquals(sub, m.getColumnVector(2));

    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testGetRow
    public void testGetRow() {
        RealMatrix m = new BlockRealMatrix(subTestData);
        checkArrays(subRow0[0], m.getRow(0));
        checkArrays(subRow3[0], m.getRow(3));
        try {
            m.getRow(-1);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.getRow(4);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testSetRow
    public void testSetRow() {
        RealMatrix m = new BlockRealMatrix(subTestData);
        assertTrue(subRow3[0][0] != m.getRow(0)[0]);
        m.setRow(0, subRow3[0]);
        checkArrays(subRow3[0], m.getRow(0));
        try {
            m.setRow(-1, subRow3[0]);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.setRow(0, new double[5]);
            fail("Expecting InvalidMatrixException");
        } catch (InvalidMatrixException ex) {
            
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
                    assertEquals(0.0, m.getEntry(i, j), 0.0);
                } else {
                    assertEquals(1.0, m.getEntry(i, j), 0.0);
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
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.getColumn(4);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testSetColumn
    public void testSetColumn() {
        RealMatrix m = new BlockRealMatrix(subTestData);
        double[] mColumn3 = columnToArray(subColumn3);
        assertTrue(mColumn3[0] != m.getColumn(1)[0]);
        m.setColumn(1, mColumn3);
        checkArrays(mColumn3, m.getColumn(1));
        try {
            m.setColumn(-1, mColumn3);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.setColumn(0, new double[5]);
            fail("Expecting InvalidMatrixException");
        } catch (InvalidMatrixException ex) {
            
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
                    assertEquals(0.0, m.getEntry(i, j), 0.0);
                } else {
                    assertEquals(1.0, m.getEntry(i, j), 0.0);
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
        assertTrue(m.hashCode() != mt.hashCode());
        assertEquals(m.hashCode(), m1.hashCode());
        assertEquals(m, m);
        assertEquals(m, m1);
        assertFalse(m.equals(null));
        assertFalse(m.equals(mt));
        assertFalse(m.equals(new BlockRealMatrix(bigSingular)));
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testToString
    public void testToString() {
        BlockRealMatrix m = new BlockRealMatrix(testData);
        assertEquals("BlockRealMatrix{{1.0,2.0,3.0},{2.0,5.0,3.0},{1.0,0.0,8.0}}",
                m.toString());
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testSetSubMatrix
    public void testSetSubMatrix() throws Exception {
        BlockRealMatrix m = new BlockRealMatrix(testData);
        m.setSubMatrix(detData2,1,1);
        RealMatrix expected = new BlockRealMatrix
            (new double[][] {{1.0,2.0,3.0},{2.0,1.0,3.0},{1.0,2.0,4.0}});
        assertEquals(expected, m);

        m.setSubMatrix(detData2,0,0);
        expected = new BlockRealMatrix
            (new double[][] {{1.0,3.0,3.0},{2.0,4.0,3.0},{1.0,2.0,4.0}});
        assertEquals(expected, m);

        m.setSubMatrix(testDataPlus2,0,0);
        expected = new BlockRealMatrix
            (new double[][] {{3.0,4.0,5.0},{4.0,7.0,5.0},{3.0,2.0,10.0}});
        assertEquals(expected, m);

        
        BlockRealMatrix matrix = new BlockRealMatrix
            (new double[][] {{1, 2, 3, 4}, {5, 6, 7, 8}, {9, 0, 1 , 2}});
        matrix.setSubMatrix(new double[][] {{3, 4}, {5, 6}}, 1, 1);
        expected = new BlockRealMatrix
            (new double[][] {{1, 2, 3, 4}, {5, 3, 4, 8}, {9, 5 ,6, 2}});
        assertEquals(expected, matrix);

        
        try {
            m.setSubMatrix(testData,1,1);
            fail("expecting MatrixIndexException");
        } catch (MatrixIndexException e) {
            
        }
        
        try {
            m.setSubMatrix(testData,-1,1);
            fail("expecting MatrixIndexException");
        } catch (MatrixIndexException e) {
            
        }
        try {
            m.setSubMatrix(testData,1,-1);
            fail("expecting MatrixIndexException");
        } catch (MatrixIndexException e) {
            
        }

        
        try {
            m.setSubMatrix(null,1,1);
            fail("expecting NullPointerException");
        } catch (NullPointerException e) {
            
        }

        
        try {
            m.setSubMatrix(new double[][] {{1}, {2, 3}}, 0, 0);
            fail("expecting IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            
        }

        
        try {
            m.setSubMatrix(new double[][] {{}}, 0, 0);
            fail("expecting IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            
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
        assertEquals(rows * columns, getVisitor.getCount());

        m = new BlockRealMatrix(rows, columns);
        m.walkInRowOrder(new SetVisitor(), 1, rows - 2, 1, columns - 2);
        getVisitor = new GetVisitor();
        m.walkInOptimizedOrder(getVisitor, 1, rows - 2, 1, columns - 2);
        assertEquals((rows - 2) * (columns - 2), getVisitor.getCount());
        for (int i = 0; i < rows; ++i) {
            assertEquals(0.0, m.getEntry(i, 0), 0);
            assertEquals(0.0, m.getEntry(i, columns - 1), 0);
        }
        for (int j = 0; j < columns; ++j) {
            assertEquals(0.0, m.getEntry(0, j), 0);
            assertEquals(0.0, m.getEntry(rows - 1, j), 0);
        }

        m = new BlockRealMatrix(rows, columns);
        m.walkInColumnOrder(new SetVisitor());
        getVisitor = new GetVisitor();
        m.walkInOptimizedOrder(getVisitor);
        assertEquals(rows * columns, getVisitor.getCount());

        m = new BlockRealMatrix(rows, columns);
        m.walkInColumnOrder(new SetVisitor(), 1, rows - 2, 1, columns - 2);
        getVisitor = new GetVisitor();
        m.walkInOptimizedOrder(getVisitor, 1, rows - 2, 1, columns - 2);
        assertEquals((rows - 2) * (columns - 2), getVisitor.getCount());
        for (int i = 0; i < rows; ++i) {
            assertEquals(0.0, m.getEntry(i, 0), 0);
            assertEquals(0.0, m.getEntry(i, columns - 1), 0);
        }
        for (int j = 0; j < columns; ++j) {
            assertEquals(0.0, m.getEntry(0, j), 0);
            assertEquals(0.0, m.getEntry(rows - 1, j), 0);
        }

        m = new BlockRealMatrix(rows, columns);
        m.walkInOptimizedOrder(new SetVisitor());
        getVisitor = new GetVisitor();
        m.walkInRowOrder(getVisitor);
        assertEquals(rows * columns, getVisitor.getCount());

        m = new BlockRealMatrix(rows, columns);
        m.walkInOptimizedOrder(new SetVisitor(), 1, rows - 2, 1, columns - 2);
        getVisitor = new GetVisitor();
        m.walkInRowOrder(getVisitor, 1, rows - 2, 1, columns - 2);
        assertEquals((rows - 2) * (columns - 2), getVisitor.getCount());
        for (int i = 0; i < rows; ++i) {
            assertEquals(0.0, m.getEntry(i, 0), 0);
            assertEquals(0.0, m.getEntry(i, columns - 1), 0);
        }
        for (int j = 0; j < columns; ++j) {
            assertEquals(0.0, m.getEntry(0, j), 0);
            assertEquals(0.0, m.getEntry(rows - 1, j), 0);
        }

        m = new BlockRealMatrix(rows, columns);
        m.walkInOptimizedOrder(new SetVisitor());
        getVisitor = new GetVisitor();
        m.walkInColumnOrder(getVisitor);
        assertEquals(rows * columns, getVisitor.getCount());

        m = new BlockRealMatrix(rows, columns);
        m.walkInOptimizedOrder(new SetVisitor(), 1, rows - 2, 1, columns - 2);
        getVisitor = new GetVisitor();
        m.walkInColumnOrder(getVisitor, 1, rows - 2, 1, columns - 2);
        assertEquals((rows - 2) * (columns - 2), getVisitor.getCount());
        for (int i = 0; i < rows; ++i) {
            assertEquals(0.0, m.getEntry(i, 0), 0);
            assertEquals(0.0, m.getEntry(i, columns - 1), 0);
        }
        for (int j = 0; j < columns; ++j) {
            assertEquals(0.0, m.getEntry(0, j), 0);
            assertEquals(0.0, m.getEntry(rows - 1, j), 0);
        }

    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testSerial
    public void testSerial()  {
        BlockRealMatrix m = new BlockRealMatrix(testData);
        assertEquals(m,TestUtils.serializeAndRecover(m));
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
        } catch (InvalidMatrixException ime) {
            
        } catch (Exception e) {
            fail("wrong exception caught");
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
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ex) {
            
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
        } catch (IllegalArgumentException ex) {
            
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
        } catch (IllegalArgumentException ex) {
            
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
        } catch (IllegalArgumentException ex) {
            
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
            fail("expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
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
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.linear.FieldMatrixImplTest::testGetVectors
    public void testGetVectors() {
        FieldMatrix<Fraction> m = new Array2DRowFieldMatrix<Fraction>(testData);
        TestUtils.assertEquals(m.getRow(0), testDataRow1);
        TestUtils.assertEquals(m.getColumn(2), testDataCol3);
        try {
            m.getRow(10);
            fail("expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.getColumn(-1);
            fail("expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
    }

// org.apache.commons.math.linear.FieldMatrixImplTest::testGetEntry
    public void testGetEntry() {
        FieldMatrix<Fraction> m = new Array2DRowFieldMatrix<Fraction>(testData);
        assertEquals("get entry",m.getEntry(0,1),new Fraction(2));
        try {
            m.getEntry(10, 4);
            fail ("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
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
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.getRowMatrix(4);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
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
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.setRowMatrix(0, m);
            fail("Expecting InvalidMatrixException");
        } catch (InvalidMatrixException ex) {
            
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
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.getColumnMatrix(4);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
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
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.setColumnMatrix(0, m);
            fail("Expecting InvalidMatrixException");
        } catch (InvalidMatrixException ex) {
            
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
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.getRowVector(4);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
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
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.setRowVector(0, new ArrayFieldVector<Fraction>(FractionField.getInstance(), 5));
            fail("Expecting InvalidMatrixException");
        } catch (InvalidMatrixException ex) {
            
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
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.getColumnVector(4);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
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
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.setColumnVector(0, new ArrayFieldVector<Fraction>(FractionField.getInstance(), 5));
            fail("Expecting InvalidMatrixException");
        } catch (InvalidMatrixException ex) {
            
        }
    }

// org.apache.commons.math.linear.FieldMatrixImplTest::testGetRow
    public void testGetRow() {
        FieldMatrix<Fraction> m = new Array2DRowFieldMatrix<Fraction>(subTestData);
        checkArrays(subRow0[0], m.getRow(0));
        checkArrays(subRow3[0], m.getRow(3));
        try {
            m.getRow(-1);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.getRow(4);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
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
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.setRow(0, new Fraction[5]);
            fail("Expecting InvalidMatrixException");
        } catch (InvalidMatrixException ex) {
            
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
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.getColumn(4);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
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
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.setColumn(0, new Fraction[5]);
            fail("Expecting InvalidMatrixException");
        } catch (InvalidMatrixException ex) {
            
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
            fail("expecting MatrixIndexException");
        } catch (MatrixIndexException e) {
            
        }
        
        try {
            m.setSubMatrix(testData,-1,1);
            fail("expecting MatrixIndexException");
        } catch (MatrixIndexException e) {
            
        }
        try {
            m.setSubMatrix(testData,1,-1);
            fail("expecting MatrixIndexException");
        } catch (MatrixIndexException e) {
            
        }

        
        try {
            m.setSubMatrix(null,1,1);
            fail("expecting NullPointerException");
        } catch (NullPointerException e) {
            
        }
        Array2DRowFieldMatrix<Fraction> m2 = new Array2DRowFieldMatrix<Fraction>(FractionField.getInstance());
        try {
            m2.setSubMatrix(testData,0,1);
            fail("expecting IllegalStateException");
        } catch (IllegalStateException e) {
            
        }
        try {
            m2.setSubMatrix(testData,1,0);
            fail("expecting IllegalStateException");
        } catch (IllegalStateException e) {
            
        }

        
        try {
            m.setSubMatrix(new Fraction[][] {{new Fraction(1)}, {new Fraction(2), new Fraction(3)}}, 0, 0);
            fail("expecting IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            
        }

        
        try {
            m.setSubMatrix(new Fraction[][] {{}}, 0, 0);
            fail("expecting IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            
        }

    }

// org.apache.commons.math.linear.FieldMatrixImplTest::testWalk
    public void testWalk() {
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

// org.apache.commons.math.linear.MatrixUtilsTest::testCreateRealMatrix
    public void testCreateRealMatrix() {
        assertEquals(new BlockRealMatrix(testData),
                MatrixUtils.createRealMatrix(testData));
        try {
            MatrixUtils.createRealMatrix(new double[][] {{1}, {1,2}});  
            fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }
        try {
            MatrixUtils.createRealMatrix(new double[][] {{}, {}});  
            fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }
        try {
            MatrixUtils.createRealMatrix(null);  
            fail("Expecting NullPointerException");
        } catch (NullPointerException ex) {
            
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
            fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }
        try {
            MatrixUtils.createFieldMatrix(asFraction(new double[][] {{}, {}}));  
            fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }
        try {
            MatrixUtils.createFieldMatrix((Fraction[][])null);  
            fail("Expecting NullPointerException");
        } catch (NullPointerException ex) {
            
        }
    }

// org.apache.commons.math.linear.MatrixUtilsTest::testCreateBigMatrix
    public void testCreateBigMatrix() {
        assertEquals(new BigMatrixImpl(testData),
                MatrixUtils.createBigMatrix(testData));
        assertEquals(new BigMatrixImpl(BigMatrixImplTest.asBigDecimal(testData), true),
                MatrixUtils.createBigMatrix(BigMatrixImplTest.asBigDecimal(testData), false));
        assertEquals(new BigMatrixImpl(BigMatrixImplTest.asBigDecimal(testData), false),
                MatrixUtils.createBigMatrix(BigMatrixImplTest.asBigDecimal(testData), true));
        assertEquals(new BigMatrixImpl(bigColMatrix),
                MatrixUtils.createBigMatrix(bigColMatrix));
        assertEquals(new BigMatrixImpl(stringColMatrix),
                MatrixUtils.createBigMatrix(stringColMatrix));
        try {
            MatrixUtils.createBigMatrix(new double[][] {{1}, {1,2}});  
            fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }
        try {
            MatrixUtils.createBigMatrix(new double[][] {{}, {}});  
            fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }
        try {
            MatrixUtils.createBigMatrix(nullMatrix);  
            fail("Expecting NullPointerException");
        } catch (NullPointerException ex) {
            
        }
    }

// org.apache.commons.math.linear.MatrixUtilsTest::testCreateRowRealMatrix
    public void testCreateRowRealMatrix() {
        assertEquals(MatrixUtils.createRowRealMatrix(row),
                     new BlockRealMatrix(rowMatrix));
        try {
            MatrixUtils.createRowRealMatrix(new double[] {});  
            fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }
        try {
            MatrixUtils.createRowRealMatrix(null);  
            fail("Expecting NullPointerException");
        } catch (NullPointerException ex) {
            
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
            fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }
        try {
            MatrixUtils.createRowFieldMatrix((Fraction[]) null);  
            fail("Expecting NullPointerException");
        } catch (NullPointerException ex) {
            
        }
    }

// org.apache.commons.math.linear.MatrixUtilsTest::testCreateRowBigMatrix
    public void testCreateRowBigMatrix() {
        assertEquals(MatrixUtils.createRowBigMatrix(row),
                new BigMatrixImpl(rowMatrix));
        assertEquals(MatrixUtils.createRowBigMatrix(bigRow),
                new BigMatrixImpl(bigRowMatrix));
        assertEquals(MatrixUtils.createRowBigMatrix(stringRow),
                new BigMatrixImpl(stringRowMatrix));
        try {
            MatrixUtils.createRowBigMatrix(new double[] {});  
            fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }
        try {
            MatrixUtils.createRowBigMatrix(nullDoubleArray);  
            fail("Expecting NullPointerException");
        } catch (NullPointerException ex) {
            
        }
    }

// org.apache.commons.math.linear.MatrixUtilsTest::testCreateColumnRealMatrix
    public void testCreateColumnRealMatrix() {
        assertEquals(MatrixUtils.createColumnRealMatrix(col),
                     new BlockRealMatrix(colMatrix));
        try {
            MatrixUtils.createColumnRealMatrix(new double[] {});  
            fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }
        try {
            MatrixUtils.createColumnRealMatrix(null);  
            fail("Expecting NullPointerException");
        } catch (NullPointerException ex) {
            
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
            fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }
        try {
            MatrixUtils.createColumnFieldMatrix((Fraction[]) null);  
            fail("Expecting NullPointerException");
        } catch (NullPointerException ex) {
            
        }
    }

// org.apache.commons.math.linear.MatrixUtilsTest::testCreateColumnBigMatrix
    public void testCreateColumnBigMatrix() {
        assertEquals(MatrixUtils.createColumnBigMatrix(col),
                new BigMatrixImpl(colMatrix));
        assertEquals(MatrixUtils.createColumnBigMatrix(bigCol),
                new BigMatrixImpl(bigColMatrix));
        assertEquals(MatrixUtils.createColumnBigMatrix(stringCol),
                new BigMatrixImpl(stringColMatrix));

        try {
            MatrixUtils.createColumnBigMatrix(new double[] {});  
            fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }
        try {
            MatrixUtils.createColumnBigMatrix(nullDoubleArray);  
            fail("Expecting NullPointerException");
        } catch (NullPointerException ex) {
            
        }
    }

// org.apache.commons.math.linear.MatrixUtilsTest::testCreateIdentityMatrix
    public void testCreateIdentityMatrix() {
        checkIdentityMatrix(MatrixUtils.createRealIdentityMatrix(3));
        checkIdentityMatrix(MatrixUtils.createRealIdentityMatrix(2));
        checkIdentityMatrix(MatrixUtils.createRealIdentityMatrix(1));
        try {
            MatrixUtils.createRealIdentityMatrix(0);
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.linear.MatrixUtilsTest::testcreateFieldIdentityMatrix
    public void testcreateFieldIdentityMatrix() {
        checkIdentityFieldMatrix(MatrixUtils.createFieldIdentityMatrix(FractionField.getInstance(), 3));
        checkIdentityFieldMatrix(MatrixUtils.createFieldIdentityMatrix(FractionField.getInstance(), 2));
        checkIdentityFieldMatrix(MatrixUtils.createFieldIdentityMatrix(FractionField.getInstance(), 1));
        try {
            MatrixUtils.createRealIdentityMatrix(0);
        } catch (IllegalArgumentException ex) {
            
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

// org.apache.commons.math.linear.MatrixUtilsTest::testCreateBigIdentityMatrix
    public void testCreateBigIdentityMatrix() {
        checkIdentityBigMatrix(MatrixUtils.createBigIdentityMatrix(3));
        checkIdentityBigMatrix(MatrixUtils.createBigIdentityMatrix(2));
        checkIdentityBigMatrix(MatrixUtils.createBigIdentityMatrix(1));
        try {
            MatrixUtils.createRealIdentityMatrix(0);
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.linear.RealMatrixImplTest::testDimensions
    public void testDimensions() {
        RealMatrixImpl m = new RealMatrixImpl(testData);
        RealMatrixImpl m2 = new RealMatrixImpl(testData2);
        assertEquals("testData row dimension",3,m.getRowDimension());
        assertEquals("testData column dimension",3,m.getColumnDimension());
        assertTrue("testData is square",m.isSquare());
        assertEquals("testData2 row dimension",m2.getRowDimension(),2);
        assertEquals("testData2 column dimension",m2.getColumnDimension(),3);
        assertTrue("testData2 is not square",!m2.isSquare());
    }

// org.apache.commons.math.linear.RealMatrixImplTest::testCopyFunctions
    public void testCopyFunctions() {
        RealMatrixImpl m1 = new RealMatrixImpl(testData);
        RealMatrixImpl m2 = new RealMatrixImpl(m1.getData());
        assertEquals(m2,m1);
        RealMatrixImpl m3 = new RealMatrixImpl(testData);
        RealMatrixImpl m4 = new RealMatrixImpl(m3.getData(), false);
        assertEquals(m4,m3);
    }

// org.apache.commons.math.linear.RealMatrixImplTest::testAdd
    public void testAdd() {
        RealMatrixImpl m = new RealMatrixImpl(testData);
        RealMatrixImpl mInv = new RealMatrixImpl(testDataInv);
        RealMatrix mPlusMInv = m.add(mInv);
        double[][] sumEntries = mPlusMInv.getData();
        for (int row = 0; row < m.getRowDimension(); row++) {
            for (int col = 0; col < m.getColumnDimension(); col++) {
                assertEquals("sum entry entry",
                    testDataPlusInv[row][col],sumEntries[row][col],
                        entryTolerance);
            }
        }
    }

// org.apache.commons.math.linear.RealMatrixImplTest::testAddFail
    public void testAddFail() {
        RealMatrixImpl m = new RealMatrixImpl(testData);
        RealMatrixImpl m2 = new RealMatrixImpl(testData2);
        try {
            m.add(m2);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.linear.RealMatrixImplTest::testNorm
    public void testNorm() {
        RealMatrixImpl m = new RealMatrixImpl(testData);
        RealMatrixImpl m2 = new RealMatrixImpl(testData2);
        assertEquals("testData norm",14d,m.getNorm(),entryTolerance);
        assertEquals("testData2 norm",7d,m2.getNorm(),entryTolerance);
    }

// org.apache.commons.math.linear.RealMatrixImplTest::testFrobeniusNorm
    public void testFrobeniusNorm() {
        RealMatrixImpl m = new RealMatrixImpl(testData);
        RealMatrixImpl m2 = new RealMatrixImpl(testData2);
        assertEquals("testData Frobenius norm", FastMath.sqrt(117.0), m.getFrobeniusNorm(), entryTolerance);
        assertEquals("testData2 Frobenius norm", FastMath.sqrt(52.0), m2.getFrobeniusNorm(), entryTolerance);
    }

// org.apache.commons.math.linear.RealMatrixImplTest::testPlusMinus
    public void testPlusMinus() {
        RealMatrixImpl m = new RealMatrixImpl(testData);
        RealMatrixImpl m2 = new RealMatrixImpl(testDataInv);
        TestUtils.assertEquals("m-n = m + -n",m.subtract(m2),
            m2.scalarMultiply(-1d).add(m),entryTolerance);
        try {
            m.subtract(new RealMatrixImpl(testData2));
            fail("Expecting illegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.linear.RealMatrixImplTest::testMultiply
     public void testMultiply() {
        RealMatrixImpl m = new RealMatrixImpl(testData);
        RealMatrixImpl mInv = new RealMatrixImpl(testDataInv);
        RealMatrixImpl identity = new RealMatrixImpl(id);
        RealMatrixImpl m2 = new RealMatrixImpl(testData2);
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
            m.multiply(new RealMatrixImpl(bigSingular));
            fail("Expecting illegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.linear.RealMatrixImplTest::testMultiply2
    public void testMultiply2() {
       RealMatrix m3 = new RealMatrixImpl(d3);
       RealMatrix m4 = new RealMatrixImpl(d4);
       RealMatrix m5 = new RealMatrixImpl(d5);
       TestUtils.assertEquals("m3*m4=m5", m3.multiply(m4), m5, entryTolerance);
   }

// org.apache.commons.math.linear.RealMatrixImplTest::testTrace
    public void testTrace() {
        RealMatrix m = new RealMatrixImpl(id);
        assertEquals("identity trace",3d,m.getTrace(),entryTolerance);
        m = new RealMatrixImpl(testData2);
        try {
            m.getTrace();
            fail("Expecting NonSquareMatrixException");
        } catch (NonSquareMatrixException ex) {
            
        }
    }

// org.apache.commons.math.linear.RealMatrixImplTest::testScalarAdd
    public void testScalarAdd() {
        RealMatrix m = new RealMatrixImpl(testData);
        TestUtils.assertEquals("scalar add",new RealMatrixImpl(testDataPlus2),
            m.scalarAdd(2d),entryTolerance);
    }

// org.apache.commons.math.linear.RealMatrixImplTest::testOperate
    public void testOperate() {
        RealMatrix m = new RealMatrixImpl(id);
        TestUtils.assertEquals("identity operate", testVector,
                    m.operate(testVector), entryTolerance);
        TestUtils.assertEquals("identity operate", testVector,
                    m.operate(new ArrayRealVector(testVector)).getData(), entryTolerance);
        m = new RealMatrixImpl(bigSingular);
        try {
            m.operate(testVector);
            fail("Expecting illegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.linear.RealMatrixImplTest::testMath209
    public void testMath209() {
        RealMatrix a = new RealMatrixImpl(new double[][] {
                { 1, 2 }, { 3, 4 }, { 5, 6 }
        }, false);
        double[] b = a.operate(new double[] { 1, 1 });
        assertEquals(a.getRowDimension(), b.length);
        assertEquals( 3.0, b[0], 1.0e-12);
        assertEquals( 7.0, b[1], 1.0e-12);
        assertEquals(11.0, b[2], 1.0e-12);
    }

// org.apache.commons.math.linear.RealMatrixImplTest::testTranspose
    public void testTranspose() {
        RealMatrix m = new RealMatrixImpl(testData);
        RealMatrix mIT = new LUDecompositionImpl(m).getSolver().getInverse().transpose();
        RealMatrix mTI = new LUDecompositionImpl(m.transpose()).getSolver().getInverse();
        TestUtils.assertEquals("inverse-transpose", mIT, mTI, normTolerance);
        m = new RealMatrixImpl(testData2);
        RealMatrix mt = new RealMatrixImpl(testData2T);
        TestUtils.assertEquals("transpose",mt,m.transpose(),normTolerance);
    }

// org.apache.commons.math.linear.RealMatrixImplTest::testPremultiplyVector
    public void testPremultiplyVector() {
        RealMatrix m = new RealMatrixImpl(testData);
        TestUtils.assertEquals("premultiply", m.preMultiply(testVector),
                    preMultTest, normTolerance);
        TestUtils.assertEquals("premultiply", m.preMultiply(new ArrayRealVector(testVector).getData()),
                    preMultTest, normTolerance);
        m = new RealMatrixImpl(bigSingular);
        try {
            m.preMultiply(testVector);
            fail("expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.linear.RealMatrixImplTest::testPremultiply
    public void testPremultiply() {
        RealMatrix m3 = new RealMatrixImpl(d3);
        RealMatrix m4 = new RealMatrixImpl(d4);
        RealMatrix m5 = new RealMatrixImpl(d5);
        TestUtils.assertEquals("m3*m4=m5", m4.preMultiply(m3), m5, entryTolerance);

        RealMatrixImpl m = new RealMatrixImpl(testData);
        RealMatrixImpl mInv = new RealMatrixImpl(testDataInv);
        RealMatrixImpl identity = new RealMatrixImpl(id);
        TestUtils.assertEquals("inverse multiply",m.preMultiply(mInv),
                identity,entryTolerance);
        TestUtils.assertEquals("inverse multiply",mInv.preMultiply(m),
                identity,entryTolerance);
        TestUtils.assertEquals("identity multiply",m.preMultiply(identity),
                m,entryTolerance);
        TestUtils.assertEquals("identity multiply",identity.preMultiply(mInv),
                mInv,entryTolerance);
        try {
            m.preMultiply(new RealMatrixImpl(bigSingular));
            fail("Expecting illegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.linear.RealMatrixImplTest::testGetVectors
    public void testGetVectors() {
        RealMatrix m = new RealMatrixImpl(testData);
        TestUtils.assertEquals("get row",m.getRow(0),testDataRow1,entryTolerance);
        TestUtils.assertEquals("get col",m.getColumn(2),testDataCol3,entryTolerance);
        try {
            m.getRow(10);
            fail("expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.getColumn(-1);
            fail("expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
    }

// org.apache.commons.math.linear.RealMatrixImplTest::testGetEntry
    public void testGetEntry() {
        RealMatrix m = new RealMatrixImpl(testData);
        assertEquals("get entry",m.getEntry(0,1),2d,entryTolerance);
        try {
            m.getEntry(10, 4);
            fail ("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
    }

// org.apache.commons.math.linear.RealMatrixImplTest::testExamples
    public void testExamples() {
        
        double[][] matrixData = { {1d,2d,3d}, {2d,5d,3d}};
        RealMatrix m = new RealMatrixImpl(matrixData);
        
        double[][] matrixData2 = { {1d,2d}, {2d,5d}, {1d, 7d}};
        RealMatrix n = new RealMatrixImpl(matrixData2);
        
        RealMatrix p = m.multiply(n);
        assertEquals(2, p.getRowDimension());
        assertEquals(2, p.getColumnDimension());
        
        RealMatrix pInverse = new LUDecompositionImpl(p).getSolver().getInverse();
        assertEquals(2, pInverse.getRowDimension());
        assertEquals(2, pInverse.getColumnDimension());

        
        double[][] coefficientsData = {{2, 3, -2}, {-1, 7, 6}, {4, -3, -5}};
        RealMatrix coefficients = new RealMatrixImpl(coefficientsData);
        double[] constants = {1, -2, 1};
        double[] solution = new LUDecompositionImpl(coefficients).getSolver().solve(constants);
        assertEquals(2 * solution[0] + 3 * solution[1] -2 * solution[2], constants[0], 1E-12);
        assertEquals(-1 * solution[0] + 7 * solution[1] + 6 * solution[2], constants[1], 1E-12);
        assertEquals(4 * solution[0] - 3 * solution[1] -5 * solution[2], constants[2], 1E-12);

    }

// org.apache.commons.math.linear.RealMatrixImplTest::testGetSubMatrix
    public void testGetSubMatrix() {
        RealMatrix m = new RealMatrixImpl(subTestData);
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

// org.apache.commons.math.linear.RealMatrixImplTest::testCopySubMatrix
    public void testCopySubMatrix() {
        RealMatrix m = new RealMatrixImpl(subTestData);
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

// org.apache.commons.math.linear.RealMatrixImplTest::testGetRowMatrix
    public void testGetRowMatrix() {
        RealMatrix m = new RealMatrixImpl(subTestData);
        RealMatrix mRow0 = new RealMatrixImpl(subRow0);
        RealMatrix mRow3 = new RealMatrixImpl(subRow3);
        assertEquals("Row0", mRow0,
                m.getRowMatrix(0));
        assertEquals("Row3", mRow3,
                m.getRowMatrix(3));
        try {
            m.getRowMatrix(-1);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.getRowMatrix(4);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
    }

// org.apache.commons.math.linear.RealMatrixImplTest::testSetRowMatrix
    public void testSetRowMatrix() {
        RealMatrix m = new RealMatrixImpl(subTestData);
        RealMatrix mRow3 = new RealMatrixImpl(subRow3);
        assertNotSame(mRow3, m.getRowMatrix(0));
        m.setRowMatrix(0, mRow3);
        assertEquals(mRow3, m.getRowMatrix(0));
        try {
            m.setRowMatrix(-1, mRow3);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.setRowMatrix(0, m);
            fail("Expecting InvalidMatrixException");
        } catch (InvalidMatrixException ex) {
            
        }
    }

// org.apache.commons.math.linear.RealMatrixImplTest::testGetColumnMatrix
    public void testGetColumnMatrix() {
        RealMatrix m = new RealMatrixImpl(subTestData);
        RealMatrix mColumn1 = new RealMatrixImpl(subColumn1);
        RealMatrix mColumn3 = new RealMatrixImpl(subColumn3);
        assertEquals("Column1", mColumn1,
                m.getColumnMatrix(1));
        assertEquals("Column3", mColumn3,
                m.getColumnMatrix(3));
        try {
            m.getColumnMatrix(-1);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.getColumnMatrix(4);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
    }

// org.apache.commons.math.linear.RealMatrixImplTest::testSetColumnMatrix
    public void testSetColumnMatrix() {
        RealMatrix m = new RealMatrixImpl(subTestData);
        RealMatrix mColumn3 = new RealMatrixImpl(subColumn3);
        assertNotSame(mColumn3, m.getColumnMatrix(1));
        m.setColumnMatrix(1, mColumn3);
        assertEquals(mColumn3, m.getColumnMatrix(1));
        try {
            m.setColumnMatrix(-1, mColumn3);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.setColumnMatrix(0, m);
            fail("Expecting InvalidMatrixException");
        } catch (InvalidMatrixException ex) {
            
        }
    }

// org.apache.commons.math.linear.RealMatrixImplTest::testGetRowVector
    public void testGetRowVector() {
        RealMatrix m = new RealMatrixImpl(subTestData);
        RealVector mRow0 = new ArrayRealVector(subRow0[0]);
        RealVector mRow3 = new ArrayRealVector(subRow3[0]);
        assertEquals("Row0", mRow0, m.getRowVector(0));
        assertEquals("Row3", mRow3, m.getRowVector(3));
        try {
            m.getRowVector(-1);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.getRowVector(4);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
    }

// org.apache.commons.math.linear.RealMatrixImplTest::testSetRowVector
    public void testSetRowVector() {
        RealMatrix m = new RealMatrixImpl(subTestData);
        RealVector mRow3 = new ArrayRealVector(subRow3[0]);
        assertNotSame(mRow3, m.getRowMatrix(0));
        m.setRowVector(0, mRow3);
        assertEquals(mRow3, m.getRowVector(0));
        try {
            m.setRowVector(-1, mRow3);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.setRowVector(0, new ArrayRealVector(5));
            fail("Expecting InvalidMatrixException");
        } catch (InvalidMatrixException ex) {
            
        }
    }

// org.apache.commons.math.linear.RealMatrixImplTest::testGetColumnVector
    public void testGetColumnVector() {
        RealMatrix m = new RealMatrixImpl(subTestData);
        RealVector mColumn1 = columnToVector(subColumn1);
        RealVector mColumn3 = columnToVector(subColumn3);
        assertEquals("Column1", mColumn1, m.getColumnVector(1));
        assertEquals("Column3", mColumn3, m.getColumnVector(3));
        try {
            m.getColumnVector(-1);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.getColumnVector(4);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
    }

// org.apache.commons.math.linear.RealMatrixImplTest::testSetColumnVector
    public void testSetColumnVector() {
        RealMatrix m = new RealMatrixImpl(subTestData);
        RealVector mColumn3 = columnToVector(subColumn3);
        assertNotSame(mColumn3, m.getColumnVector(1));
        m.setColumnVector(1, mColumn3);
        assertEquals(mColumn3, m.getColumnVector(1));
        try {
            m.setColumnVector(-1, mColumn3);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.setColumnVector(0, new ArrayRealVector(5));
            fail("Expecting InvalidMatrixException");
        } catch (InvalidMatrixException ex) {
            
        }
    }

// org.apache.commons.math.linear.RealMatrixImplTest::testGetRow
    public void testGetRow() {
        RealMatrix m = new RealMatrixImpl(subTestData);
        checkArrays(subRow0[0], m.getRow(0));
        checkArrays(subRow3[0], m.getRow(3));
        try {
            m.getRow(-1);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.getRow(4);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
    }

// org.apache.commons.math.linear.RealMatrixImplTest::testSetRow
    public void testSetRow() {
        RealMatrix m = new RealMatrixImpl(subTestData);
        assertTrue(subRow3[0][0] != m.getRow(0)[0]);
        m.setRow(0, subRow3[0]);
        checkArrays(subRow3[0], m.getRow(0));
        try {
            m.setRow(-1, subRow3[0]);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.setRow(0, new double[5]);
            fail("Expecting InvalidMatrixException");
        } catch (InvalidMatrixException ex) {
            
        }
    }

// org.apache.commons.math.linear.RealMatrixImplTest::testGetColumn
    public void testGetColumn() {
        RealMatrix m = new RealMatrixImpl(subTestData);
        double[] mColumn1 = columnToArray(subColumn1);
        double[] mColumn3 = columnToArray(subColumn3);
        checkArrays(mColumn1, m.getColumn(1));
        checkArrays(mColumn3, m.getColumn(3));
        try {
            m.getColumn(-1);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.getColumn(4);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
    }

// org.apache.commons.math.linear.RealMatrixImplTest::testSetColumn
    public void testSetColumn() {
        RealMatrix m = new RealMatrixImpl(subTestData);
        double[] mColumn3 = columnToArray(subColumn3);
        assertTrue(mColumn3[0] != m.getColumn(1)[0]);
        m.setColumn(1, mColumn3);
        checkArrays(mColumn3, m.getColumn(1));
        try {
            m.setColumn(-1, mColumn3);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.setColumn(0, new double[5]);
            fail("Expecting InvalidMatrixException");
        } catch (InvalidMatrixException ex) {
            
        }
    }

// org.apache.commons.math.linear.RealMatrixImplTest::testEqualsAndHashCode
    public void testEqualsAndHashCode() {
        RealMatrixImpl m = new RealMatrixImpl(testData);
        RealMatrixImpl m1 = (RealMatrixImpl) m.copy();
        RealMatrixImpl mt = (RealMatrixImpl) m.transpose();
        assertTrue(m.hashCode() != mt.hashCode());
        assertEquals(m.hashCode(), m1.hashCode());
        assertEquals(m, m);
        assertEquals(m, m1);
        assertFalse(m.equals(null));
        assertFalse(m.equals(mt));
        assertFalse(m.equals(new RealMatrixImpl(bigSingular)));
    }

// org.apache.commons.math.linear.RealMatrixImplTest::testToString
    public void testToString() {
        RealMatrixImpl m = new RealMatrixImpl(testData);
        assertEquals("RealMatrixImpl{{1.0,2.0,3.0},{2.0,5.0,3.0},{1.0,0.0,8.0}}",
                m.toString());
        m = new RealMatrixImpl();
        assertEquals("RealMatrixImpl{}",
                m.toString());
    }

// org.apache.commons.math.linear.RealMatrixImplTest::testSetSubMatrix
    public void testSetSubMatrix() throws Exception {
        RealMatrixImpl m = new RealMatrixImpl(testData);
        m.setSubMatrix(detData2,1,1);
        RealMatrix expected = MatrixUtils.createRealMatrix
            (new double[][] {{1.0,2.0,3.0},{2.0,1.0,3.0},{1.0,2.0,4.0}});
        assertEquals(expected, m);

        m.setSubMatrix(detData2,0,0);
        expected = MatrixUtils.createRealMatrix
            (new double[][] {{1.0,3.0,3.0},{2.0,4.0,3.0},{1.0,2.0,4.0}});
        assertEquals(expected, m);

        m.setSubMatrix(testDataPlus2,0,0);
        expected = MatrixUtils.createRealMatrix
            (new double[][] {{3.0,4.0,5.0},{4.0,7.0,5.0},{3.0,2.0,10.0}});
        assertEquals(expected, m);

        
        try {
            m.setSubMatrix(testData,1,1);
            fail("expecting MatrixIndexException");
        } catch (MatrixIndexException e) {
            
        }
        
        try {
            m.setSubMatrix(testData,-1,1);
            fail("expecting MatrixIndexException");
        } catch (MatrixIndexException e) {
            
        }
        try {
            m.setSubMatrix(testData,1,-1);
            fail("expecting MatrixIndexException");
        } catch (MatrixIndexException e) {
            
        }

        
        try {
            m.setSubMatrix(null,1,1);
            fail("expecting NullPointerException");
        } catch (NullPointerException e) {
            
        }
        RealMatrixImpl m2 = new RealMatrixImpl();
        try {
            m2.setSubMatrix(testData,0,1);
            fail("expecting IllegalStateException");
        } catch (IllegalStateException e) {
            
        }
        try {
            m2.setSubMatrix(testData,1,0);
            fail("expecting IllegalStateException");
        } catch (IllegalStateException e) {
            
        }

        
        try {
            m.setSubMatrix(new double[][] {{1}, {2, 3}}, 0, 0);
            fail("expecting IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            
        }

        
        try {
            m.setSubMatrix(new double[][] {{}}, 0, 0);
            fail("expecting IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            
        }

    }

// org.apache.commons.math.linear.RealMatrixImplTest::testWalk
    public void testWalk() {
        int rows    = 150;
        int columns = 75;

        RealMatrix m = new RealMatrixImpl(rows, columns);
        m.walkInRowOrder(new SetVisitor());
        GetVisitor getVisitor = new GetVisitor();
        m.walkInOptimizedOrder(getVisitor);
        assertEquals(rows * columns, getVisitor.getCount());

        m = new RealMatrixImpl(rows, columns);
        m.walkInRowOrder(new SetVisitor(), 1, rows - 2, 1, columns - 2);
        getVisitor = new GetVisitor();
        m.walkInOptimizedOrder(getVisitor, 1, rows - 2, 1, columns - 2);
        assertEquals((rows - 2) * (columns - 2), getVisitor.getCount());
        for (int i = 0; i < rows; ++i) {
            assertEquals(0.0, m.getEntry(i, 0), 0);
            assertEquals(0.0, m.getEntry(i, columns - 1), 0);
        }
        for (int j = 0; j < columns; ++j) {
            assertEquals(0.0, m.getEntry(0, j), 0);
            assertEquals(0.0, m.getEntry(rows - 1, j), 0);
        }

        m = new RealMatrixImpl(rows, columns);
        m.walkInColumnOrder(new SetVisitor());
        getVisitor = new GetVisitor();
        m.walkInOptimizedOrder(getVisitor);
        assertEquals(rows * columns, getVisitor.getCount());

        m = new RealMatrixImpl(rows, columns);
        m.walkInColumnOrder(new SetVisitor(), 1, rows - 2, 1, columns - 2);
        getVisitor = new GetVisitor();
        m.walkInOptimizedOrder(getVisitor, 1, rows - 2, 1, columns - 2);
        assertEquals((rows - 2) * (columns - 2), getVisitor.getCount());
        for (int i = 0; i < rows; ++i) {
            assertEquals(0.0, m.getEntry(i, 0), 0);
            assertEquals(0.0, m.getEntry(i, columns - 1), 0);
        }
        for (int j = 0; j < columns; ++j) {
            assertEquals(0.0, m.getEntry(0, j), 0);
            assertEquals(0.0, m.getEntry(rows - 1, j), 0);
        }

        m = new RealMatrixImpl(rows, columns);
        m.walkInOptimizedOrder(new SetVisitor());
        getVisitor = new GetVisitor();
        m.walkInRowOrder(getVisitor);
        assertEquals(rows * columns, getVisitor.getCount());

        m = new RealMatrixImpl(rows, columns);
        m.walkInOptimizedOrder(new SetVisitor(), 1, rows - 2, 1, columns - 2);
        getVisitor = new GetVisitor();
        m.walkInRowOrder(getVisitor, 1, rows - 2, 1, columns - 2);
        assertEquals((rows - 2) * (columns - 2), getVisitor.getCount());
        for (int i = 0; i < rows; ++i) {
            assertEquals(0.0, m.getEntry(i, 0), 0);
            assertEquals(0.0, m.getEntry(i, columns - 1), 0);
        }
        for (int j = 0; j < columns; ++j) {
            assertEquals(0.0, m.getEntry(0, j), 0);
            assertEquals(0.0, m.getEntry(rows - 1, j), 0);
        }

        m = new RealMatrixImpl(rows, columns);
        m.walkInOptimizedOrder(new SetVisitor());
        getVisitor = new GetVisitor();
        m.walkInColumnOrder(getVisitor);
        assertEquals(rows * columns, getVisitor.getCount());

        m = new RealMatrixImpl(rows, columns);
        m.walkInOptimizedOrder(new SetVisitor(), 1, rows - 2, 1, columns - 2);
        getVisitor = new GetVisitor();
        m.walkInColumnOrder(getVisitor, 1, rows - 2, 1, columns - 2);
        assertEquals((rows - 2) * (columns - 2), getVisitor.getCount());
        for (int i = 0; i < rows; ++i) {
            assertEquals(0.0, m.getEntry(i, 0), 0);
            assertEquals(0.0, m.getEntry(i, columns - 1), 0);
        }
        for (int j = 0; j < columns; ++j) {
            assertEquals(0.0, m.getEntry(0, j), 0);
            assertEquals(0.0, m.getEntry(rows - 1, j), 0);
        }

    }

// org.apache.commons.math.linear.RealMatrixImplTest::testSerial
    public void testSerial()  {
        RealMatrixImpl m = new RealMatrixImpl(testData);
        assertEquals(m,TestUtils.serializeAndRecover(m));
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
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ex) {
            
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
        } catch (IllegalArgumentException ex) {
            
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
        } catch (IllegalArgumentException ex) {
            
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
        } catch (IllegalArgumentException ex) {
            
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
            fail("expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
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
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.linear.SparseFieldMatrixTest::testGetVectors
    public void testGetVectors() {
        FieldMatrix<Fraction> m = createSparseMatrix(testData);
        assertClose("get row", m.getRow(0), testDataRow1, entryTolerance);
        assertClose("get col", m.getColumn(2), testDataCol3, entryTolerance);
        try {
            m.getRow(10);
            fail("expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.getColumn(-1);
            fail("expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
    }

// org.apache.commons.math.linear.SparseFieldMatrixTest::testGetEntry
    public void testGetEntry() {
        FieldMatrix<Fraction> m = createSparseMatrix(testData);
        assertEquals("get entry", m.getEntry(0, 1).doubleValue(), 2d, entryTolerance);
        try {
            m.getEntry(10, 4);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
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
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.getSubMatrix(-1, 1, 2, 2);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.getSubMatrix(1, 0, 2, 2);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.getSubMatrix(1, 0, 2, 4);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.getSubMatrix(new int[] {}, new int[] { 0 });
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.getSubMatrix(new int[] { 0 }, new int[] { 4 });
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
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
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.getRowMatrix(4);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
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
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.getColumnMatrix(4);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
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
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.getRowVector(4);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
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
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.getColumnVector(4);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
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
            fail("expecting MatrixIndexException");
        } catch (MatrixIndexException e) {
            
        }
        
        try {
            m.setSubMatrix(testData, -1, 1);
            fail("expecting MatrixIndexException");
        } catch (MatrixIndexException e) {
            
        }
        try {
            m.setSubMatrix(testData, 1, -1);
            fail("expecting MatrixIndexException");
        } catch (MatrixIndexException e) {
            
        }

        
        try {
            m.setSubMatrix(null, 1, 1);
            fail("expecting NullPointerException");
        } catch (NullPointerException e) {
            
        }
        try {
            new SparseFieldMatrix<Fraction>(field, 0, 0);
            fail("expecting IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            
        }

        
        try {
            m.setSubMatrix(new Fraction[][] { { new Fraction(1) }, { new Fraction(2), new Fraction(3) } }, 0, 0);
            fail("expecting IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            
        }

        
        try {
            m.setSubMatrix(new Fraction[][] { {} }, 0, 0);
            fail("expecting IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            
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
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ex) {
            
        } catch (Exception e) {
            fail("wrong exception caught");
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
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ex) {
            
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
        } catch (IllegalArgumentException ex) {
            
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
        } catch (IllegalArgumentException ex) {
            
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
        } catch (IllegalArgumentException ex) {
            
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
            fail("expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
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
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.linear.SparseRealMatrixTest::testGetVectors
    public void testGetVectors() {
        RealMatrix m = createSparseMatrix(testData);
        assertClose("get row", m.getRow(0), testDataRow1, entryTolerance);
        assertClose("get col", m.getColumn(2), testDataCol3, entryTolerance);
        try {
            m.getRow(10);
            fail("expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.getColumn(-1);
            fail("expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
    }

// org.apache.commons.math.linear.SparseRealMatrixTest::testGetEntry
    public void testGetEntry() {
        RealMatrix m = createSparseMatrix(testData);
        assertEquals("get entry", m.getEntry(0, 1), 2d, entryTolerance);
        try {
            m.getEntry(10, 4);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
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
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.getSubMatrix(-1, 1, 2, 2);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.getSubMatrix(1, 0, 2, 2);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.getSubMatrix(1, 0, 2, 4);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.getSubMatrix(new int[] {}, new int[] { 0 });
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.getSubMatrix(new int[] { 0 }, new int[] { 4 });
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
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
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.getRowMatrix(4);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
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
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.getColumnMatrix(4);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
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
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.getRowVector(4);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
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
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.getColumnVector(4);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
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
            fail("expecting MatrixIndexException");
        } catch (MatrixIndexException e) {
            
        }
        
        try {
            m.setSubMatrix(testData, -1, 1);
            fail("expecting MatrixIndexException");
        } catch (MatrixIndexException e) {
            
        }
        try {
            m.setSubMatrix(testData, 1, -1);
            fail("expecting MatrixIndexException");
        } catch (MatrixIndexException e) {
            
        }

        
        try {
            m.setSubMatrix(null, 1, 1);
            fail("expecting NullPointerException");
        } catch (NullPointerException e) {
            
        }
        try {
            new OpenMapRealMatrix(0, 0);
            fail("expecting IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            
        }

        
        try {
            m.setSubMatrix(new double[][] { { 1 }, { 2, 3 } }, 0, 0);
            fail("expecting IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            
        }

        
        try {
            m.setSubMatrix(new double[][] { {} }, 0, 0);
            fail("expecting IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            
        }

    }

// org.apache.commons.math.linear.SparseRealMatrixTest::testSerial
    public void testSerial()  {
        OpenMapRealMatrix m = createSparseMatrix(testData);
        assertEquals(m,TestUtils.serializeAndRecover(m));
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
            
        } catch (Exception e) {
            fail("wrong exception caught: " + e.getMessage());
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
        } catch (DimensionMismatchException dme) {
            
        } catch (Exception e) {
            fail("wrong exception caught");
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
