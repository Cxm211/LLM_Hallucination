// buggy code
    private boolean flipIfWarranted(final int n, final int step) {
        if (1.5 * work[pingPong] < work[4 * (n - 1) + pingPong]) {
            // flip array
            int j = 4 * n - 1;
            for (int i = 0; i < j; i += 4) {
                for (int k = 0; k < 4; k += step) {
                    final double tmp = work[i + k];
                    work[i + k] = work[j - k];
                    work[j - k] = tmp;
                }
                j -= 4;
            }
            return true;
        }
        return false;
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

// org.apache.commons.math.linear.EigenDecompositionImplTest::testMathpbx02
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

        
        EigenDecomposition decomposition =
            new EigenDecompositionImpl(mainTridiagonal, secondaryTridiagonal, MathUtils.SAFE_MIN);

        double[] eigenValues = decomposition.getRealEigenvalues();
        for (int i = 0; i < refEigenValues.length; ++i) {
            assertEquals(refEigenValues[i], eigenValues[i], 1.0e-3);
            if (refEigenVectors[i].dotProduct(decomposition.getEigenvector(i)) < 0) {
                assertEquals(0, refEigenVectors[i].add(decomposition.getEigenvector(i)).getNorm(), 1.0e-5);
            } else {
                assertEquals(0, refEigenVectors[i].subtract(decomposition.getEigenvector(i)).getNorm(), 1.0e-5);
            }
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
