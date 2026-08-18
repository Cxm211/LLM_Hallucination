// buggy code
    public static double pow(double x, double y) {
        final double lns[] = new double[2];

        if (y == 0.0) {
            return 1.0;
        }

        if (x != x) { // X is NaN
            return x;
        }


        if (x == 0) {
            long bits = Double.doubleToLongBits(x);
            if ((bits & 0x8000000000000000L) != 0) {
                // -zero
                long yi = (long) y;

                if (y < 0 && y == yi && (yi & 1) == 1) {
                    return Double.NEGATIVE_INFINITY;
                }

                if (y > 0 && y == yi && (yi & 1) == 1) {
                    return -0.0;
                }
            }

            if (y < 0) {
                return Double.POSITIVE_INFINITY;
            }
            if (y > 0) {
                return 0.0;
            }

            return Double.NaN;
        }

        if (x == Double.POSITIVE_INFINITY) {
            if (y != y) { // y is NaN
                return y;
            }
            if (y < 0.0) {
                return 0.0;
            } else {
                return Double.POSITIVE_INFINITY;
            }
        }

        if (y == Double.POSITIVE_INFINITY) {
            if (x * x == 1.0) {
                return Double.NaN;
            }

            if (x * x > 1.0) {
                return Double.POSITIVE_INFINITY;
            } else {
                return 0.0;
            }
        }

        if (x == Double.NEGATIVE_INFINITY) {
            if (y != y) { // y is NaN
                return y;
            }

            if (y < 0) {
                long yi = (long) y;
                if (y == yi && (yi & 1) == 1) {
                    return -0.0;
                }

                return 0.0;
            }

            if (y > 0)  {
                long yi = (long) y;
                if (y == yi && (yi & 1) == 1) {
                    return Double.NEGATIVE_INFINITY;
                }

                return Double.POSITIVE_INFINITY;
            }
        }

        if (y == Double.NEGATIVE_INFINITY) {

            if (x * x == 1.0) {
                return Double.NaN;
            }

            if (x * x < 1.0) {
                return Double.POSITIVE_INFINITY;
            } else {
                return 0.0;
            }
        }

        /* Handle special case x<0 */
        if (x < 0) {
            // y is an even integer in this case
            if (y >= TWO_POWER_52 || y <= -TWO_POWER_52) {
                return pow(-x, y);
            }

            if (y == (long) y) {
                // If y is an integer
                return ((long)y & 1) == 0 ? pow(-x, y) : -pow(-x, y);
            } else {
                return Double.NaN;
            }
        }

        /* Split y into ya and yb such that y = ya+yb */
        double ya;
        double yb;
        if (y < 8e298 && y > -8e298) {
            double tmp1 = y * HEX_40000000;
            ya = y + tmp1 - tmp1;
            yb = y - ya;
        } else {
            double tmp1 = y * 9.31322574615478515625E-10;
            double tmp2 = tmp1 * 9.31322574615478515625E-10;
            ya = (tmp1 + tmp2 - tmp1) * HEX_40000000 * HEX_40000000;
            yb = y - ya;
        }

        /* Compute ln(x) */
        final double lores = log(x, lns);
        if (Double.isInfinite(lores)){ // don't allow this to be converted to NaN
            return lores;
        }

        double lna = lns[0];
        double lnb = lns[1];

        /* resplit lns */
        double tmp1 = lna * HEX_40000000;
        double tmp2 = lna + tmp1 - tmp1;
        lnb += lna - tmp2;
        lna = tmp2;

        // y*ln(x) = (aa+ab)
        final double aa = lna * ya;
        final double ab = lna * yb + lnb * ya + lnb * yb;

        lna = aa+ab;
        lnb = -(lna - aa - ab);

        double z = 1.0 / 120.0;
        z = z * lnb + (1.0 / 24.0);
        z = z * lnb + (1.0 / 6.0);
        z = z * lnb + 0.5;
        z = z * lnb + 1.0;
        z = z * lnb;

        final double result = exp(lna, z, null);
        //result = result + result * z;
        return result;
    }

// relevant test
// org.apache.commons.math3.linear.RectangularCholeskyDecompositionTest::testDecomposition3x3
    public void testDecomposition3x3() {

        RealMatrix m = MatrixUtils.createRealMatrix(new double[][] {
            { 1,   9,   9 },
            { 9, 225, 225 },
            { 9, 225, 625 }
        });

        RectangularCholeskyDecomposition d =
                new RectangularCholeskyDecomposition(m, 1.0e-6);

        
        
        
        Assert.assertEquals(0.8,  d.getRootMatrix().getEntry(0, 2), 1.0e-15);
        Assert.assertEquals(25.0, d.getRootMatrix().getEntry(2, 0), 1.0e-15);
        Assert.assertEquals(0.0,  d.getRootMatrix().getEntry(2, 2), 1.0e-15);

        RealMatrix root = d.getRootMatrix();
        RealMatrix rebuiltM = root.multiply(root.transpose());
        Assert.assertEquals(0.0, m.subtract(rebuiltM).getNorm(), 1.0e-15);

    }

// org.apache.commons.math3.linear.RectangularCholeskyDecompositionTest::testFullRank
    public void testFullRank() {

        RealMatrix base = MatrixUtils.createRealMatrix(new double[][] {
            { 0.1159548705,      0.,           0.,           0.      },
            { 0.0896442724, 0.1223540781,      0.,           0.      },
            { 0.0852155322, 4.558668e-3,  0.1083577299,      0.      },
            { 0.0905486674, 0.0213768077, 0.0128878333, 0.1014155693 }
        });

        RealMatrix m = base.multiply(base.transpose());

        RectangularCholeskyDecomposition d =
                new RectangularCholeskyDecomposition(m, 1.0e-10);

        RealMatrix root = d.getRootMatrix();
        RealMatrix rebuiltM = root.multiply(root.transpose());
        Assert.assertEquals(0.0, m.subtract(rebuiltM).getNorm(), 1.0e-15);

        
        
        Assert.assertTrue(root.subtract(base).getNorm() > 0.3);

    }

// org.apache.commons.math3.linear.RectangularCholeskyDecompositionTest::testMath789
    public void testMath789() {

        final RealMatrix m1 = MatrixUtils.createRealMatrix(new double[][]{
            {0.013445532, 0.010394690, 0.009881156, 0.010499559},
            {0.010394690, 0.023006616, 0.008196856, 0.010732709},
            {0.009881156, 0.008196856, 0.019023866, 0.009210099},
            {0.010499559, 0.010732709, 0.009210099, 0.019107243}
        });
        composeAndTest(m1, 4);

        final RealMatrix m2 = MatrixUtils.createRealMatrix(new double[][]{
            {0.0, 0.0, 0.0, 0.0, 0.0},
            {0.0, 0.013445532, 0.010394690, 0.009881156, 0.010499559},
            {0.0, 0.010394690, 0.023006616, 0.008196856, 0.010732709},
            {0.0, 0.009881156, 0.008196856, 0.019023866, 0.009210099},
            {0.0, 0.010499559, 0.010732709, 0.009210099, 0.019107243}
        });
        composeAndTest(m2, 4);

        final RealMatrix m3 = MatrixUtils.createRealMatrix(new double[][]{
            {0.013445532, 0.010394690, 0.0, 0.009881156, 0.010499559},
            {0.010394690, 0.023006616, 0.0, 0.008196856, 0.010732709},
            {0.0, 0.0, 0.0, 0.0, 0.0},
            {0.009881156, 0.008196856, 0.0, 0.019023866, 0.009210099},
            {0.010499559, 0.010732709, 0.0, 0.009210099, 0.019107243}
        });
        composeAndTest(m3, 4);

    }

// org.apache.commons.math3.linear.SchurTransformerTest::testNonSquare
    public void testNonSquare() {
        try {
            new SchurTransformer(MatrixUtils.createRealMatrix(new double[3][2]));
            Assert.fail("an exception should have been thrown");
        } catch (NonSquareMatrixException ime) {
            
        }
    }

// org.apache.commons.math3.linear.SchurTransformerTest::testAEqualPTPt
    public void testAEqualPTPt() {
        checkAEqualPTPt(MatrixUtils.createRealMatrix(testSquare5));
        checkAEqualPTPt(MatrixUtils.createRealMatrix(testSquare3));
        checkAEqualPTPt(MatrixUtils.createRealMatrix(testRandom));
   }

// org.apache.commons.math3.linear.SchurTransformerTest::testPOrthogonal
    public void testPOrthogonal() {
        checkOrthogonal(new SchurTransformer(MatrixUtils.createRealMatrix(testSquare5)).getP());
        checkOrthogonal(new SchurTransformer(MatrixUtils.createRealMatrix(testSquare3)).getP());
        checkOrthogonal(new SchurTransformer(MatrixUtils.createRealMatrix(testRandom)).getP());        
    }

// org.apache.commons.math3.linear.SchurTransformerTest::testPTOrthogonal
    public void testPTOrthogonal() {
        checkOrthogonal(new SchurTransformer(MatrixUtils.createRealMatrix(testSquare5)).getPT());
        checkOrthogonal(new SchurTransformer(MatrixUtils.createRealMatrix(testSquare3)).getPT());
        checkOrthogonal(new SchurTransformer(MatrixUtils.createRealMatrix(testRandom)).getPT());
    }

// org.apache.commons.math3.linear.SchurTransformerTest::testSchurForm
    public void testSchurForm() {
        checkSchurForm(new SchurTransformer(MatrixUtils.createRealMatrix(testSquare5)).getT());
        checkSchurForm(new SchurTransformer(MatrixUtils.createRealMatrix(testSquare3)).getT());
        checkSchurForm(new SchurTransformer(MatrixUtils.createRealMatrix(testRandom)).getT());
    }

// org.apache.commons.math3.linear.SchurTransformerTest::testRandomData
    public void testRandomData() {
        for (int run = 0; run < 100; run++) {
            Random r = new Random(System.currentTimeMillis());

            
            int size = r.nextInt(20) + 4;

            double[][] data = new double[size][size];
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    data[i][j] = r.nextInt(100);
                }
            }

            RealMatrix m = MatrixUtils.createRealMatrix(data);
            RealMatrix s = checkAEqualPTPt(m);
            checkSchurForm(s);
        }
    }

// org.apache.commons.math3.linear.SchurTransformerTest::testRandomDataNormalDistribution
    public void testRandomDataNormalDistribution() {
        for (int run = 0; run < 100; run++) {
            Random r = new Random(System.currentTimeMillis());
            NormalDistribution dist = new NormalDistribution(0.0, r.nextDouble() * 5);

            
            int size = r.nextInt(20) + 4;

            double[][] data = new double[size][size];
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    data[i][j] = dist.sample();
                }
            }

            RealMatrix m = MatrixUtils.createRealMatrix(data);
            RealMatrix s = checkAEqualPTPt(m);
            checkSchurForm(s);
        }
    }

// org.apache.commons.math3.linear.SchurTransformerTest::testMath848
    public void testMath848() {
        double[][] data = {
                { 0.1849449280, -0.0646971046,  0.0774755812, -0.0969651755, -0.0692648806,  0.3282344352, -0.0177423074,  0.2063136340},
                {-0.0742700134, -0.0289063030, -0.0017269460, -0.0375550146, -0.0487737922, -0.2616837868, -0.0821201295, -0.2530000167},
                { 0.2549910127,  0.0995733692, -0.0009718388,  0.0149282808,  0.1791878897, -0.0823182816,  0.0582629256,  0.3219545182},
                {-0.0694747557, -0.1880649148, -0.2740630911,  0.0720096468, -0.1800836914, -0.3518996425,  0.2486747833,  0.6257938167},
                { 0.0536360918, -0.1339297778,  0.2241579764, -0.0195327484, -0.0054103808,  0.0347564518,  0.5120802482, -0.0329902864},
                {-0.5933332356, -0.2488721082,  0.2357173629,  0.0177285473,  0.0856630593, -0.3567126300, -0.1600668126, -0.1010899621},
                {-0.0514349819, -0.0854319435,  0.1125050061,  0.0063453560, -0.2250000688, -0.2209343090,  0.1964623477, -0.1512329924},
                { 0.0197395947, -0.1997170581, -0.1425959019, -0.2749477910, -0.0969467073,  0.0603688520, -0.2826905192,  0.1794315473}};
        RealMatrix m = MatrixUtils.createRealMatrix(data);
        RealMatrix s = checkAEqualPTPt(m);
        checkSchurForm(s);
    }

// org.apache.commons.math3.linear.SingularValueDecompositionTest::testMoreRows
    public void testMoreRows() {
        final double[] singularValues = { 123.456, 2.3, 1.001, 0.999 };
        final int rows    = singularValues.length + 2;
        final int columns = singularValues.length;
        Random r = new Random(15338437322523l);
        SingularValueDecomposition svd =
            new SingularValueDecomposition(createTestMatrix(r, rows, columns, singularValues));
        double[] computedSV = svd.getSingularValues();
        Assert.assertEquals(singularValues.length, computedSV.length);
        for (int i = 0; i < singularValues.length; ++i) {
            Assert.assertEquals(singularValues[i], computedSV[i], 1.0e-10);
        }
    }

// org.apache.commons.math3.linear.SingularValueDecompositionTest::testMoreColumns
    public void testMoreColumns() {
        final double[] singularValues = { 123.456, 2.3, 1.001, 0.999 };
        final int rows    = singularValues.length;
        final int columns = singularValues.length + 2;
        Random r = new Random(732763225836210l);
        SingularValueDecomposition svd =
            new SingularValueDecomposition(createTestMatrix(r, rows, columns, singularValues));
        double[] computedSV = svd.getSingularValues();
        Assert.assertEquals(singularValues.length, computedSV.length);
        for (int i = 0; i < singularValues.length; ++i) {
            Assert.assertEquals(singularValues[i], computedSV[i], 1.0e-10);
        }
    }

// org.apache.commons.math3.linear.SingularValueDecompositionTest::testDimensions
    public void testDimensions() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(testSquare);
        final int m = matrix.getRowDimension();
        final int n = matrix.getColumnDimension();
        SingularValueDecomposition svd = new SingularValueDecomposition(matrix);
        Assert.assertEquals(m, svd.getU().getRowDimension());
        Assert.assertEquals(m, svd.getU().getColumnDimension());
        Assert.assertEquals(m, svd.getS().getColumnDimension());
        Assert.assertEquals(n, svd.getS().getColumnDimension());
        Assert.assertEquals(n, svd.getV().getRowDimension());
        Assert.assertEquals(n, svd.getV().getColumnDimension());

    }

// org.apache.commons.math3.linear.SingularValueDecompositionTest::testHadamard
    public void testHadamard() {
        RealMatrix matrix = new Array2DRowRealMatrix(new double[][] {
                {15.0 / 2.0,  5.0 / 2.0,  9.0 / 2.0,  3.0 / 2.0 },
                { 5.0 / 2.0, 15.0 / 2.0,  3.0 / 2.0,  9.0 / 2.0 },
                { 9.0 / 2.0,  3.0 / 2.0, 15.0 / 2.0,  5.0 / 2.0 },
                { 3.0 / 2.0,  9.0 / 2.0,  5.0 / 2.0, 15.0 / 2.0 }
        }, false);
        SingularValueDecomposition svd = new SingularValueDecomposition(matrix);
        Assert.assertEquals(16.0, svd.getSingularValues()[0], 1.0e-14);
        Assert.assertEquals( 8.0, svd.getSingularValues()[1], 1.0e-14);
        Assert.assertEquals( 4.0, svd.getSingularValues()[2], 1.0e-14);
        Assert.assertEquals( 2.0, svd.getSingularValues()[3], 1.0e-14);

        RealMatrix fullCovariance = new Array2DRowRealMatrix(new double[][] {
                {  85.0 / 1024, -51.0 / 1024, -75.0 / 1024,  45.0 / 1024 },
                { -51.0 / 1024,  85.0 / 1024,  45.0 / 1024, -75.0 / 1024 },
                { -75.0 / 1024,  45.0 / 1024,  85.0 / 1024, -51.0 / 1024 },
                {  45.0 / 1024, -75.0 / 1024, -51.0 / 1024,  85.0 / 1024 }
        }, false);
        Assert.assertEquals(0.0,
                     fullCovariance.subtract(svd.getCovariance(0.0)).getNorm(),
                     1.0e-14);

        RealMatrix halfCovariance = new Array2DRowRealMatrix(new double[][] {
                {   5.0 / 1024,  -3.0 / 1024,   5.0 / 1024,  -3.0 / 1024 },
                {  -3.0 / 1024,   5.0 / 1024,  -3.0 / 1024,   5.0 / 1024 },
                {   5.0 / 1024,  -3.0 / 1024,   5.0 / 1024,  -3.0 / 1024 },
                {  -3.0 / 1024,   5.0 / 1024,  -3.0 / 1024,   5.0 / 1024 }
        }, false);
        Assert.assertEquals(0.0,
                     halfCovariance.subtract(svd.getCovariance(6.0)).getNorm(),
                     1.0e-14);

    }

// org.apache.commons.math3.linear.SingularValueDecompositionTest::testAEqualUSVt
    public void testAEqualUSVt() {
        checkAEqualUSVt(MatrixUtils.createRealMatrix(testSquare));
        checkAEqualUSVt(MatrixUtils.createRealMatrix(testNonSquare));
        checkAEqualUSVt(MatrixUtils.createRealMatrix(testNonSquare).transpose());
    }

// org.apache.commons.math3.linear.SingularValueDecompositionTest::testUOrthogonal
    public void testUOrthogonal() {
        checkOrthogonal(new SingularValueDecomposition(MatrixUtils.createRealMatrix(testSquare)).getU());
        checkOrthogonal(new SingularValueDecomposition(MatrixUtils.createRealMatrix(testNonSquare)).getU());
        checkOrthogonal(new SingularValueDecomposition(MatrixUtils.createRealMatrix(testNonSquare).transpose()).getU());
    }

// org.apache.commons.math3.linear.SingularValueDecompositionTest::testVOrthogonal
    public void testVOrthogonal() {
        checkOrthogonal(new SingularValueDecomposition(MatrixUtils.createRealMatrix(testSquare)).getV());
        checkOrthogonal(new SingularValueDecomposition(MatrixUtils.createRealMatrix(testNonSquare)).getV());
        checkOrthogonal(new SingularValueDecomposition(MatrixUtils.createRealMatrix(testNonSquare).transpose()).getV());
    }

// org.apache.commons.math3.linear.SingularValueDecompositionTest::testMatricesValues1
    public void testMatricesValues1() {
       SingularValueDecomposition svd =
            new SingularValueDecomposition(MatrixUtils.createRealMatrix(testSquare));
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
        Assert.assertEquals(0, u.subtract(uRef).getNorm(), normTolerance);
        RealMatrix s = svd.getS();
        Assert.assertEquals(0, s.subtract(sRef).getNorm(), normTolerance);
        RealMatrix v = svd.getV();
        Assert.assertEquals(0, v.subtract(vRef).getNorm(), normTolerance);

        
        Assert.assertTrue(u == svd.getU());
        Assert.assertTrue(s == svd.getS());
        Assert.assertTrue(v == svd.getV());

    }

// org.apache.commons.math3.linear.SingularValueDecompositionTest::testRank
    public void testRank() {
        double[][] d = { { 1, 1, 1 }, { 0, 0, 0 }, { 1, 2, 3 } };
        RealMatrix m = new Array2DRowRealMatrix(d);
        SingularValueDecomposition svd = new SingularValueDecomposition(m);
        Assert.assertEquals(2, svd.getRank());
    }

// org.apache.commons.math3.linear.SingularValueDecompositionTest::testStability1
    public void testStability1() {
        RealMatrix m = new Array2DRowRealMatrix(201, 201);
        loadRealMatrix(m,"matrix1.csv");
        try {
            new SingularValueDecomposition(m);
        } catch (Exception e) {
            Assert.fail("Exception whilst constructing SVD");
        }
    }

// org.apache.commons.math3.linear.SingularValueDecompositionTest::testStability2
    public void testStability2() {
        RealMatrix m = new Array2DRowRealMatrix(7, 168);
        loadRealMatrix(m,"matrix2.csv");
        try {
            new SingularValueDecomposition(m);
        } catch (Throwable e) {
            Assert.fail("Exception whilst constructing SVD");
        }
    }

// org.apache.commons.math3.linear.SingularValueDecompositionTest::testConditionNumber
    public void testConditionNumber() {
        SingularValueDecomposition svd =
            new SingularValueDecomposition(MatrixUtils.createRealMatrix(testSquare));
        
        Assert.assertEquals(3.0, svd.getConditionNumber(), 1.5e-15);
    }

// org.apache.commons.math3.linear.SingularValueDecompositionTest::testInverseConditionNumber
    public void testInverseConditionNumber() {
        SingularValueDecomposition svd =
            new SingularValueDecomposition(MatrixUtils.createRealMatrix(testSquare));
        Assert.assertEquals(1.0/3.0, svd.getInverseConditionNumber(), 1.5e-15);
    }

// org.apache.commons.math3.linear.SingularValueSolverTest::testSolveDimensionErrors
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

// org.apache.commons.math3.linear.SingularValueSolverTest::testLeastSquareSolve
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

// org.apache.commons.math3.linear.SingularValueSolverTest::testSolve
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

// org.apache.commons.math3.linear.SingularValueSolverTest::testConditionNumber
    public void testConditionNumber() {
        SingularValueDecomposition svd =
            new SingularValueDecomposition(MatrixUtils.createRealMatrix(testSquare));
        
        Assert.assertEquals(3.0, svd.getConditionNumber(), 1.5e-15);
    }

// org.apache.commons.math3.linear.SingularValueSolverTest::testMath320B
    public void testMath320B() {
        RealMatrix rm = new Array2DRowRealMatrix(new double[][] {
            { 1.0, 2.0 }, { 1.0, 2.0 }
        });
        SingularValueDecomposition svd =
            new SingularValueDecomposition(rm);
        RealMatrix recomposed = svd.getU().multiply(svd.getS()).multiply(svd.getVT());
        Assert.assertEquals(0.0, recomposed.subtract(rm).getNorm(), 2.0e-15);
    }

// org.apache.commons.math3.linear.SparseFieldMatrixTest::testDimensions
    public void testDimensions() {
        SparseFieldMatrix<Fraction> m = createSparseMatrix(testData);
        SparseFieldMatrix<Fraction> m2 = createSparseMatrix(testData2);
        Assert.assertEquals("testData row dimension", 3, m.getRowDimension());
        Assert.assertEquals("testData column dimension", 3, m.getColumnDimension());
        Assert.assertTrue("testData is square", m.isSquare());
        Assert.assertEquals("testData2 row dimension", m2.getRowDimension(), 2);
        Assert.assertEquals("testData2 column dimension", m2.getColumnDimension(), 3);
        Assert.assertTrue("testData2 is not square", !m2.isSquare());
    }

// org.apache.commons.math3.linear.SparseFieldMatrixTest::testCopyFunctions
    public void testCopyFunctions() {
        SparseFieldMatrix<Fraction> m1 = createSparseMatrix(testData);
        FieldMatrix<Fraction> m2 = m1.copy();
        Assert.assertEquals(m1.getClass(), m2.getClass());
        Assert.assertEquals((m2), m1);
        SparseFieldMatrix<Fraction> m3 = createSparseMatrix(testData);
        FieldMatrix<Fraction> m4 = m3.copy();
        Assert.assertEquals(m3.getClass(), m4.getClass());
        Assert.assertEquals((m4), m3);
    }

// org.apache.commons.math3.linear.SparseFieldMatrixTest::testAdd
    public void testAdd() {
        SparseFieldMatrix<Fraction> m = createSparseMatrix(testData);
        SparseFieldMatrix<Fraction> mInv = createSparseMatrix(testDataInv);
        SparseFieldMatrix<Fraction> mDataPlusInv = createSparseMatrix(testDataPlusInv);
        FieldMatrix<Fraction> mPlusMInv = m.add(mInv);
        for (int row = 0; row < m.getRowDimension(); row++) {
            for (int col = 0; col < m.getColumnDimension(); col++) {
                Assert.assertEquals("sum entry entry",
                    mDataPlusInv.getEntry(row, col).doubleValue(), mPlusMInv.getEntry(row, col).doubleValue(),
                    entryTolerance);
            }
        }
    }

// org.apache.commons.math3.linear.SparseFieldMatrixTest::testAddFail
    public void testAddFail() {
        SparseFieldMatrix<Fraction> m = createSparseMatrix(testData);
        SparseFieldMatrix<Fraction> m2 = createSparseMatrix(testData2);
        try {
            m.add(m2);
            Assert.fail("MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math3.linear.SparseFieldMatrixTest::testPlusMinus
    public void testPlusMinus() {
        SparseFieldMatrix<Fraction> m = createSparseMatrix(testData);
        SparseFieldMatrix<Fraction> n = createSparseMatrix(testDataInv);
        assertClose("m-n = m + -n", m.subtract(n),
            n.scalarMultiply(new Fraction(-1)).add(m), entryTolerance);
        try {
            m.subtract(createSparseMatrix(testData2));
            Assert.fail("Expecting illegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math3.linear.SparseFieldMatrixTest::testMultiply
    public void testMultiply() {
        SparseFieldMatrix<Fraction> m = createSparseMatrix(testData);
        SparseFieldMatrix<Fraction> mInv = createSparseMatrix(testDataInv);
        SparseFieldMatrix<Fraction> identity = createSparseMatrix(id);
        SparseFieldMatrix<Fraction> m2 = createSparseMatrix(testData2);
        assertClose("inverse multiply", m.multiply(mInv), identity,
                entryTolerance);
        assertClose("inverse multiply", m.multiply(new Array2DRowFieldMatrix<Fraction>(FractionField.getInstance(), testDataInv)), identity,
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

// org.apache.commons.math3.linear.SparseFieldMatrixTest::testMultiply2
    public void testMultiply2() {
        FieldMatrix<Fraction> m3 = createSparseMatrix(d3);
        FieldMatrix<Fraction> m4 = createSparseMatrix(d4);
        FieldMatrix<Fraction> m5 = createSparseMatrix(d5);
        assertClose("m3*m4=m5", m3.multiply(m4), m5, entryTolerance);
    }

// org.apache.commons.math3.linear.SparseFieldMatrixTest::testTrace
    public void testTrace() {
        FieldMatrix<Fraction> m = createSparseMatrix(id);
        Assert.assertEquals("identity trace", 3d, m.getTrace().doubleValue(), entryTolerance);
        m = createSparseMatrix(testData2);
        try {
            m.getTrace();
            Assert.fail("Expecting NonSquareMatrixException");
        } catch (NonSquareMatrixException ex) {
            
        }
    }

// org.apache.commons.math3.linear.SparseFieldMatrixTest::testScalarAdd
    public void testScalarAdd() {
        FieldMatrix<Fraction> m = createSparseMatrix(testData);
        assertClose("scalar add", createSparseMatrix(testDataPlus2),
            m.scalarAdd(new Fraction(2)), entryTolerance);
    }

// org.apache.commons.math3.linear.SparseFieldMatrixTest::testOperate
    public void testOperate() {
        FieldMatrix<Fraction> m = createSparseMatrix(id);
        assertClose("identity operate", testVector, m.operate(testVector),
                entryTolerance);
        assertClose("identity operate", testVector, m.operate(
                new ArrayFieldVector<Fraction>(testVector)).getData(), entryTolerance);
        m = createSparseMatrix(bigSingular);
        try {
            m.operate(testVector);
            Assert.fail("Expecting illegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math3.linear.SparseFieldMatrixTest::testMath209
    public void testMath209() {
        FieldMatrix<Fraction> a = createSparseMatrix(new Fraction[][] {
                { new Fraction(1), new Fraction(2) }, { new Fraction(3), new Fraction(4) }, { new Fraction(5), new Fraction(6) } });
        Fraction[] b = a.operate(new Fraction[] { new Fraction(1), new Fraction(1) });
        Assert.assertEquals(a.getRowDimension(), b.length);
        Assert.assertEquals(3.0, b[0].doubleValue(), 1.0e-12);
        Assert.assertEquals(7.0, b[1].doubleValue(), 1.0e-12);
        Assert.assertEquals(11.0, b[2].doubleValue(), 1.0e-12);
    }

// org.apache.commons.math3.linear.SparseFieldMatrixTest::testTranspose
    public void testTranspose() {
        FieldMatrix<Fraction> m = createSparseMatrix(testData);
        FieldMatrix<Fraction> mIT = new FieldLUDecomposition<Fraction>(m).getSolver().getInverse().transpose();
        FieldMatrix<Fraction> mTI = new FieldLUDecomposition<Fraction>(m.transpose()).getSolver().getInverse();
        assertClose("inverse-transpose", mIT, mTI, normTolerance);
        m = createSparseMatrix(testData2);
        FieldMatrix<Fraction> mt = createSparseMatrix(testData2T);
        assertClose("transpose",mt,m.transpose(),normTolerance);
    }

// org.apache.commons.math3.linear.SparseFieldMatrixTest::testPremultiplyVector
    public void testPremultiplyVector() {
        FieldMatrix<Fraction> m = createSparseMatrix(testData);
        assertClose("premultiply", m.preMultiply(testVector), preMultTest,
            normTolerance);
        assertClose("premultiply", m.preMultiply(
            new ArrayFieldVector<Fraction>(testVector).getData()), preMultTest, normTolerance);
        m = createSparseMatrix(bigSingular);
        try {
            m.preMultiply(testVector);
            Assert.fail("expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math3.linear.SparseFieldMatrixTest::testPremultiply
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
            Assert.fail("Expecting illegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math3.linear.SparseFieldMatrixTest::testGetVectors
    public void testGetVectors() {
        FieldMatrix<Fraction> m = createSparseMatrix(testData);
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

// org.apache.commons.math3.linear.SparseFieldMatrixTest::testGetEntry
    public void testGetEntry() {
        FieldMatrix<Fraction> m = createSparseMatrix(testData);
        Assert.assertEquals("get entry", m.getEntry(0, 1).doubleValue(), 2d, entryTolerance);
        try {
            m.getEntry(10, 4);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
    }

// org.apache.commons.math3.linear.SparseFieldMatrixTest::testExamples
    public void testExamples() {
        
        Fraction[][] matrixData = { { new Fraction(1), new Fraction(2), new Fraction(3) }, { new Fraction(2), new Fraction(5), new Fraction(3) } };
        FieldMatrix<Fraction> m = createSparseMatrix(matrixData);
        
        Fraction[][] matrixData2 = { { new Fraction(1), new Fraction(2) }, { new Fraction(2), new Fraction(5) }, { new Fraction(1), new Fraction(7) } };
        FieldMatrix<Fraction> n = createSparseMatrix(matrixData2);
        
        FieldMatrix<Fraction> p = m.multiply(n);
        Assert.assertEquals(2, p.getRowDimension());
        Assert.assertEquals(2, p.getColumnDimension());
        
        FieldMatrix<Fraction> pInverse = new FieldLUDecomposition<Fraction>(p).getSolver().getInverse();
        Assert.assertEquals(2, pInverse.getRowDimension());
        Assert.assertEquals(2, pInverse.getColumnDimension());

        
        Fraction[][] coefficientsData = { { new Fraction(2), new Fraction(3), new Fraction(-2) }, { new Fraction(-1), new Fraction(7), new Fraction(6) },
                { new Fraction(4), new Fraction(-3), new Fraction(-5) } };
        FieldMatrix<Fraction> coefficients = createSparseMatrix(coefficientsData);
        Fraction[] constants = { new Fraction(1), new Fraction(-2), new Fraction(1) };
        Fraction[] solution;
        solution = new FieldLUDecomposition<Fraction>(coefficients)
            .getSolver()
            .solve(new ArrayFieldVector<Fraction>(constants, false)).toArray();
        Assert.assertEquals((new Fraction(2).multiply((solution[0])).add(new Fraction(3).multiply(solution[1])).subtract(new Fraction(2).multiply(solution[2]))).doubleValue(),
                constants[0].doubleValue(), 1E-12);
        Assert.assertEquals(((new Fraction(-1).multiply(solution[0])).add(new Fraction(7).multiply(solution[1])).add(new Fraction(6).multiply(solution[2]))).doubleValue(),
                constants[1].doubleValue(), 1E-12);
        Assert.assertEquals(((new Fraction(4).multiply(solution[0])).subtract(new Fraction(3).multiply( solution[1])).subtract(new Fraction(5).multiply(solution[2]))).doubleValue(),
                constants[2].doubleValue(), 1E-12);

    }

// org.apache.commons.math3.linear.SparseFieldMatrixTest::testSubMatrix
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

// org.apache.commons.math3.linear.SparseFieldMatrixTest::testGetRowMatrix
    public void testGetRowMatrix() {
        FieldMatrix<Fraction> m = createSparseMatrix(subTestData);
        FieldMatrix<Fraction> mRow0 = createSparseMatrix(subRow0);
        FieldMatrix<Fraction> mRow3 = createSparseMatrix(subRow3);
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

// org.apache.commons.math3.linear.SparseFieldMatrixTest::testGetColumnMatrix
    public void testGetColumnMatrix() {
        FieldMatrix<Fraction> m = createSparseMatrix(subTestData);
        FieldMatrix<Fraction> mColumn1 = createSparseMatrix(subColumn1);
        FieldMatrix<Fraction> mColumn3 = createSparseMatrix(subColumn3);
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

// org.apache.commons.math3.linear.SparseFieldMatrixTest::testGetRowVector
    public void testGetRowVector() {
        FieldMatrix<Fraction> m = createSparseMatrix(subTestData);
        FieldVector<Fraction> mRow0 = new ArrayFieldVector<Fraction>(subRow0[0]);
        FieldVector<Fraction> mRow3 = new ArrayFieldVector<Fraction>(subRow3[0]);
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

// org.apache.commons.math3.linear.SparseFieldMatrixTest::testGetColumnVector
    public void testGetColumnVector() {
        FieldMatrix<Fraction> m = createSparseMatrix(subTestData);
        FieldVector<Fraction> mColumn1 = columnToVector(subColumn1);
        FieldVector<Fraction> mColumn3 = columnToVector(subColumn3);
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

// org.apache.commons.math3.linear.SparseFieldMatrixTest::testEqualsAndHashCode
    public void testEqualsAndHashCode() {
        SparseFieldMatrix<Fraction> m = createSparseMatrix(testData);
        SparseFieldMatrix<Fraction> m1 = (SparseFieldMatrix<Fraction>) m.copy();
        SparseFieldMatrix<Fraction> mt = (SparseFieldMatrix<Fraction>) m.transpose();
        Assert.assertTrue(m.hashCode() != mt.hashCode());
        Assert.assertEquals(m.hashCode(), m1.hashCode());
        Assert.assertEquals(m, m);
        Assert.assertEquals(m, m1);
        Assert.assertFalse(m.equals(null));
        Assert.assertFalse(m.equals(mt));
        Assert.assertFalse(m.equals(createSparseMatrix(bigSingular)));
    }

// org.apache.commons.math3.linear.SparseFieldMatrixTest::testSetSubMatrix
    public void testSetSubMatrix() {
        SparseFieldMatrix<Fraction> m = createSparseMatrix(testData);
        m.setSubMatrix(detData2, 1, 1);
        FieldMatrix<Fraction> expected = createSparseMatrix(new Fraction[][] {
                { new Fraction(1), new Fraction(2), new Fraction(3) }, { new Fraction(2), new Fraction(1), new Fraction(3) }, { new Fraction(1), new Fraction(2), new Fraction(4) } });
        Assert.assertEquals(expected, m);

        m.setSubMatrix(detData2, 0, 0);
        expected = createSparseMatrix(new Fraction[][] {
                { new Fraction(1), new Fraction(3), new Fraction(3) }, { new Fraction(2), new Fraction(4), new Fraction(3) }, { new Fraction(1), new Fraction(2), new Fraction(4) } });
        Assert.assertEquals(expected, m);

        m.setSubMatrix(testDataPlus2, 0, 0);
        expected = createSparseMatrix(new Fraction[][] {
                { new Fraction(3), new Fraction(4), new Fraction(5) }, { new Fraction(4), new Fraction(7), new Fraction(5) }, { new Fraction(3), new Fraction(2), new Fraction(10) } });
        Assert.assertEquals(expected, m);

        
        SparseFieldMatrix<Fraction> matrix =
            createSparseMatrix(new Fraction[][] {
        { new Fraction(1), new Fraction(2), new Fraction(3), new Fraction(4) }, { new Fraction(5), new Fraction(6), new Fraction(7), new Fraction(8) }, { new Fraction(9), new Fraction(0), new Fraction(1), new Fraction(2) } });
        matrix.setSubMatrix(new Fraction[][] { { new Fraction(3), new Fraction(4) }, { new Fraction(5), new Fraction(6) } }, 1, 1);
        expected = createSparseMatrix(new Fraction[][] {
                { new Fraction(1), new Fraction(2), new Fraction(3), new Fraction(4) }, { new Fraction(5), new Fraction(3), new Fraction(4), new Fraction(8) }, { new Fraction(9), new Fraction(5), new Fraction(6), new Fraction(2) } });
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
            new SparseFieldMatrix<Fraction>(field, 0, 0);
            Assert.fail("expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException e) {
            
        }

        
        try {
            m.setSubMatrix(new Fraction[][] { { new Fraction(1) }, { new Fraction(2), new Fraction(3) } }, 0, 0);
            Assert.fail("expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException e) {
            
        }

        
        try {
            m.setSubMatrix(new Fraction[][] { {} }, 0, 0);
            Assert.fail("expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException e) {
            
        }
    }

// org.apache.commons.math3.linear.SparseFieldVectorTest::testMapFunctions
    public void testMapFunctions() throws FractionConversionException {
        SparseFieldVector<Fraction> v1 = new SparseFieldVector<Fraction>(field,vec1);

        
        FieldVector<Fraction> v_mapAdd = v1.mapAdd(new Fraction(2));
        Fraction[] result_mapAdd = {new Fraction(3), new Fraction(4), new Fraction(5)};
        Assert.assertArrayEquals("compare vectors" ,result_mapAdd,v_mapAdd.getData());

        
        FieldVector<Fraction> v_mapAddToSelf = v1.copy();
        v_mapAddToSelf.mapAddToSelf(new Fraction(2));
        Fraction[] result_mapAddToSelf = {new Fraction(3), new Fraction(4), new Fraction(5)};
        Assert.assertArrayEquals("compare vectors" ,result_mapAddToSelf,v_mapAddToSelf.getData());

        
        FieldVector<Fraction> v_mapSubtract = v1.mapSubtract(new Fraction(2));
        Fraction[] result_mapSubtract = {new Fraction(-1), new Fraction(0), new Fraction(1)};
        Assert.assertArrayEquals("compare vectors" ,result_mapSubtract,v_mapSubtract.getData());

        
        FieldVector<Fraction> v_mapSubtractToSelf = v1.copy();
        v_mapSubtractToSelf.mapSubtractToSelf(new Fraction(2));
        Fraction[] result_mapSubtractToSelf = {new Fraction(-1), new Fraction(0), new Fraction(1)};
        Assert.assertArrayEquals("compare vectors" ,result_mapSubtractToSelf,v_mapSubtractToSelf.getData());

        
        FieldVector<Fraction> v_mapMultiply = v1.mapMultiply(new Fraction(2));
        Fraction[] result_mapMultiply = {new Fraction(2), new Fraction(4), new Fraction(6)};
        Assert.assertArrayEquals("compare vectors" ,result_mapMultiply,v_mapMultiply.getData());

        
        FieldVector<Fraction> v_mapMultiplyToSelf = v1.copy();
        v_mapMultiplyToSelf.mapMultiplyToSelf(new Fraction(2));
        Fraction[] result_mapMultiplyToSelf = {new Fraction(2), new Fraction(4), new Fraction(6)};
        Assert.assertArrayEquals("compare vectors" ,result_mapMultiplyToSelf,v_mapMultiplyToSelf.getData());

        
        FieldVector<Fraction> v_mapDivide = v1.mapDivide(new Fraction(2));
        Fraction[] result_mapDivide = {new Fraction(.5d), new Fraction(1), new Fraction(1.5d)};
        Assert.assertArrayEquals("compare vectors" ,result_mapDivide,v_mapDivide.getData());

        
        FieldVector<Fraction> v_mapDivideToSelf = v1.copy();
        v_mapDivideToSelf.mapDivideToSelf(new Fraction(2));
        Fraction[] result_mapDivideToSelf = {new Fraction(.5d), new Fraction(1), new Fraction(1.5d)};
        Assert.assertArrayEquals("compare vectors" ,result_mapDivideToSelf,v_mapDivideToSelf.getData());

        
        FieldVector<Fraction> v_mapInv = v1.mapInv();
        Fraction[] result_mapInv = {new Fraction(1),new Fraction(0.5d),new Fraction(3.333333333333333e-01d)};
        Assert.assertArrayEquals("compare vectors" ,result_mapInv,v_mapInv.getData());

        
        FieldVector<Fraction> v_mapInvToSelf = v1.copy();
        v_mapInvToSelf.mapInvToSelf();
        Fraction[] result_mapInvToSelf = {new Fraction(1),new Fraction(0.5d),new Fraction(3.333333333333333e-01d)};
        Assert.assertArrayEquals("compare vectors" ,result_mapInvToSelf,v_mapInvToSelf.getData());

    }

// org.apache.commons.math3.linear.SparseFieldVectorTest::testBasicFunctions
    public void testBasicFunctions() throws FractionConversionException {
        SparseFieldVector<Fraction> v1 = new SparseFieldVector<Fraction>(field,vec1);
        SparseFieldVector<Fraction> v2 = new SparseFieldVector<Fraction>(field,vec2);

        FieldVector<Fraction> v2_t = new ArrayFieldVectorTest.FieldVectorTestImpl<Fraction>(vec2);

        
        FieldVector<Fraction> v_add = v1.add(v2);
        Fraction[] result_add = {new Fraction(5), new Fraction(7), new Fraction(9)};
        Assert.assertArrayEquals("compare vect" ,v_add.getData(),result_add);

        FieldVector<Fraction> vt2 = new ArrayFieldVectorTest.FieldVectorTestImpl<Fraction>(vec2);
        FieldVector<Fraction> v_add_i = v1.add(vt2);
        Fraction[] result_add_i = {new Fraction(5), new Fraction(7), new Fraction(9)};
        Assert.assertArrayEquals("compare vect" ,v_add_i.getData(),result_add_i);

        
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
        Assert.assertEquals("compare val ",new Fraction(32), dot);

        
        Fraction dot_2 =  v1.dotProduct(v2_t);
        Assert.assertEquals("compare val ",new Fraction(32), dot_2);

        FieldMatrix<Fraction> m_outerProduct = v1.outerProduct(v2);
        Assert.assertEquals("compare val ",new Fraction(4), m_outerProduct.getEntry(0,0));

        FieldMatrix<Fraction> m_outerProduct_2 = v1.outerProduct(v2_t);
        Assert.assertEquals("compare val ",new Fraction(4), m_outerProduct_2.getEntry(0,0));

    }

// org.apache.commons.math3.linear.SparseFieldVectorTest::testOuterProduct
    public void testOuterProduct() {
        final SparseFieldVector<Fraction> u
            = new SparseFieldVector<Fraction>(FractionField.getInstance(),
                                              new Fraction[] {new Fraction(1),
                                                              new Fraction(2),
                                                              new Fraction(-3)});
        final SparseFieldVector<Fraction> v
            = new SparseFieldVector<Fraction>(FractionField.getInstance(),
                                              new Fraction[] {new Fraction(4),
                                                              new Fraction(-2)});

        final FieldMatrix<Fraction> uv = u.outerProduct(v);

        final double tol = Math.ulp(1d);
        Assert.assertEquals(new Fraction(4).doubleValue(), uv.getEntry(0, 0).doubleValue(), tol);
        Assert.assertEquals(new Fraction(-2).doubleValue(), uv.getEntry(0, 1).doubleValue(), tol);
        Assert.assertEquals(new Fraction(8).doubleValue(), uv.getEntry(1, 0).doubleValue(), tol);
        Assert.assertEquals(new Fraction(-4).doubleValue(), uv.getEntry(1, 1).doubleValue(), tol);
        Assert.assertEquals(new Fraction(-12).doubleValue(), uv.getEntry(2, 0).doubleValue(), tol);
        Assert.assertEquals(new Fraction(6).doubleValue(), uv.getEntry(2, 1).doubleValue(), tol);
    }

// org.apache.commons.math3.linear.SparseFieldVectorTest::testMisc
    public void testMisc() {
        SparseFieldVector<Fraction> v1 = new SparseFieldVector<Fraction>(field,vec1);

        String out1 = v1.toString();
        Assert.assertTrue("some output ",  out1.length()!=0);
        try {
            v1.checkVectorDimensions(2);
            Assert.fail("MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }

    }

// org.apache.commons.math3.linear.SparseFieldVectorTest::testPredicates
    public void testPredicates() {

        SparseFieldVector<Fraction> v = new SparseFieldVector<Fraction>(field, new Fraction[] { new Fraction(0), new Fraction(1), new Fraction(2) });

        v.setEntry(0, field.getZero());
        Assert.assertEquals(v, new SparseFieldVector<Fraction>(field, new Fraction[] { new Fraction(0), new Fraction(1), new Fraction(2) }));
        Assert.assertNotSame(v, new SparseFieldVector<Fraction>(field, new Fraction[] { new Fraction(0), new Fraction(1), new Fraction(2), new Fraction(3) }));

    }

// org.apache.commons.math3.linear.SparseRealMatrixTest::testDimensions
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

// org.apache.commons.math3.linear.SparseRealMatrixTest::testCopyFunctions
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

// org.apache.commons.math3.linear.SparseRealMatrixTest::testAdd
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

// org.apache.commons.math3.linear.SparseRealMatrixTest::testAddFail
    public void testAddFail() {
        OpenMapRealMatrix m = createSparseMatrix(testData);
        OpenMapRealMatrix m2 = createSparseMatrix(testData2);
        try {
            m.add(m2);
            Assert.fail("MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math3.linear.SparseRealMatrixTest::testNorm
    public void testNorm() {
        OpenMapRealMatrix m = createSparseMatrix(testData);
        OpenMapRealMatrix m2 = createSparseMatrix(testData2);
        Assert.assertEquals("testData norm", 14d, m.getNorm(), entryTolerance);
        Assert.assertEquals("testData2 norm", 7d, m2.getNorm(), entryTolerance);
    }

// org.apache.commons.math3.linear.SparseRealMatrixTest::testPlusMinus
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

// org.apache.commons.math3.linear.SparseRealMatrixTest::testMultiply
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

// org.apache.commons.math3.linear.SparseRealMatrixTest::testMultiply2
    public void testMultiply2() {
        RealMatrix m3 = createSparseMatrix(d3);
        RealMatrix m4 = createSparseMatrix(d4);
        RealMatrix m5 = createSparseMatrix(d5);
        assertClose("m3*m4=m5", m3.multiply(m4), m5, entryTolerance);
    }

// org.apache.commons.math3.linear.SparseRealMatrixTest::testTrace
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

// org.apache.commons.math3.linear.SparseRealMatrixTest::testScalarAdd
    public void testScalarAdd() {
        RealMatrix m = createSparseMatrix(testData);
        assertClose("scalar add", createSparseMatrix(testDataPlus2),
            m.scalarAdd(2d), entryTolerance);
    }

// org.apache.commons.math3.linear.SparseRealMatrixTest::testOperate
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

// org.apache.commons.math3.linear.SparseRealMatrixTest::testMath209
    public void testMath209() {
        RealMatrix a = createSparseMatrix(new double[][] {
                { 1, 2 }, { 3, 4 }, { 5, 6 } });
        double[] b = a.operate(new double[] { 1, 1 });
        Assert.assertEquals(a.getRowDimension(), b.length);
        Assert.assertEquals(3.0, b[0], 1.0e-12);
        Assert.assertEquals(7.0, b[1], 1.0e-12);
        Assert.assertEquals(11.0, b[2], 1.0e-12);
    }

// org.apache.commons.math3.linear.SparseRealMatrixTest::testTranspose
    public void testTranspose() {
        RealMatrix m = createSparseMatrix(testData);
        RealMatrix mIT = new LUDecomposition(m).getSolver().getInverse().transpose();
        RealMatrix mTI = new LUDecomposition(m.transpose()).getSolver().getInverse();
        assertClose("inverse-transpose", mIT, mTI, normTolerance);
        m = createSparseMatrix(testData2);
        RealMatrix mt = createSparseMatrix(testData2T);
        assertClose("transpose",mt,m.transpose(),normTolerance);
    }

// org.apache.commons.math3.linear.SparseRealMatrixTest::testPremultiplyVector
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

// org.apache.commons.math3.linear.SparseRealMatrixTest::testPremultiply
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

// org.apache.commons.math3.linear.SparseRealMatrixTest::testGetVectors
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

// org.apache.commons.math3.linear.SparseRealMatrixTest::testGetEntry
    public void testGetEntry() {
        RealMatrix m = createSparseMatrix(testData);
        Assert.assertEquals("get entry", m.getEntry(0, 1), 2d, entryTolerance);
        try {
            m.getEntry(10, 4);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
    }

// org.apache.commons.math3.linear.SparseRealMatrixTest::testExamples
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

// org.apache.commons.math3.linear.SparseRealMatrixTest::testSubMatrix
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

// org.apache.commons.math3.linear.SparseRealMatrixTest::testGetRowMatrix
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

// org.apache.commons.math3.linear.SparseRealMatrixTest::testGetColumnMatrix
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

// org.apache.commons.math3.linear.SparseRealMatrixTest::testGetRowVector
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

// org.apache.commons.math3.linear.SparseRealMatrixTest::testGetColumnVector
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

// org.apache.commons.math3.linear.SparseRealMatrixTest::testEqualsAndHashCode
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

// org.apache.commons.math3.linear.SparseRealMatrixTest::testToString
    public void testToString() {
        OpenMapRealMatrix m = createSparseMatrix(testData);
        Assert.assertEquals("OpenMapRealMatrix{{1.0,2.0,3.0},{2.0,5.0,3.0},{1.0,0.0,8.0}}",
            m.toString());
        m = new OpenMapRealMatrix(1, 1);
        Assert.assertEquals("OpenMapRealMatrix{{0.0}}", m.toString());
    }

// org.apache.commons.math3.linear.SparseRealMatrixTest::testSetSubMatrix
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

// org.apache.commons.math3.linear.SparseRealMatrixTest::testSerial
    public void testSerial()  {
        OpenMapRealMatrix m = createSparseMatrix(testData);
        Assert.assertEquals(m,TestUtils.serializeAndRecover(m));
    }

// org.apache.commons.math3.linear.SparseRealVectorTest::testConstructors
    public void testConstructors() {
        final double[] vec1 = {1d, 2d, 3d};
        final Double[] dvec1 = {1d, 2d, 3d, 4d, 5d, 6d, 7d, 8d, 9d};

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

        RealVectorTestImpl v7_i = new RealVectorTestImpl(vec1);

        OpenMapRealVector v7_2 = new OpenMapRealVector(v7_i);
        Assert.assertEquals("testData len", 3, v7_2.getDimension());
        Assert.assertEquals("testData is 0.0 ", 2.0d, v7_2.getEntry(1), 0);

        OpenMapRealVector v8 = new OpenMapRealVector(v1);
        Assert.assertEquals("testData len", 7, v8.getDimension());
        Assert.assertEquals("testData is 0.0 ", 0.0, v8.getEntry(6), 0);

    }

// org.apache.commons.math3.linear.SparseRealVectorTest::testConcurrentModification
    public void testConcurrentModification() {
        final RealVector u = new OpenMapRealVector(3, 1e-6);
        u.setEntry(0, 1);
        u.setEntry(1, 0);
        u.setEntry(2, 2);

        final RealVector v1 = new OpenMapRealVector(3, 1e-6);
        v1.setEntry(0, 0);
        v1.setEntry(1, 3);
        v1.setEntry(2, 0);

        u.ebeMultiply(v1);
        u.ebeDivide(v1);
    }

// org.apache.commons.math3.linear.SparseRealVectorTest::testMap
    public void testMap() {}

// org.apache.commons.math3.linear.SparseRealVectorTest::testMapToSelf
    public void testMapToSelf() {}

// org.apache.commons.math3.linear.SymmLQTest::testSolveSaunders1
    public void testSolveSaunders1() {
        saundersTest(1, false, false, 0., 0.);
    }

// org.apache.commons.math3.linear.SymmLQTest::testSolveSaunders2
    public void testSolveSaunders2() {
        saundersTest(2, false, false, 0., 0.);
    }

// org.apache.commons.math3.linear.SymmLQTest::testSolveSaunders3
    public void testSolveSaunders3() {
        saundersTest(1, false, true, 0., 0.);
    }

// org.apache.commons.math3.linear.SymmLQTest::testSolveSaunders4
    public void testSolveSaunders4() {
        saundersTest(2, false, true, 0., 0.);
    }

// org.apache.commons.math3.linear.SymmLQTest::testSolveSaunders5
    public void testSolveSaunders5() {
        saundersTest(5, false, true, 0., 0.);
    }

// org.apache.commons.math3.linear.SymmLQTest::testSolveSaunders6
    public void testSolveSaunders6() {
        saundersTest(5, false, true, 0.25, 0.);
    }

// org.apache.commons.math3.linear.SymmLQTest::testSolveSaunders7
    public void testSolveSaunders7() {
        saundersTest(50, false, false, 0., 0.);
    }

// org.apache.commons.math3.linear.SymmLQTest::testSolveSaunders8
    public void testSolveSaunders8() {
        saundersTest(50, false, false, 0.25, 0.);
    }

// org.apache.commons.math3.linear.SymmLQTest::testSolveSaunders9
    public void testSolveSaunders9() {
        saundersTest(50, false, true, 0., 0.10);
    }

// org.apache.commons.math3.linear.SymmLQTest::testSolveSaunders10
    public void testSolveSaunders10() {
        saundersTest(50, false, true, 0.25, 0.10);
    }

// org.apache.commons.math3.linear.SymmLQTest::testSolveSaunders11
    public void testSolveSaunders11() {
        saundersTest(1, true, false, 0., 0.);
    }

// org.apache.commons.math3.linear.SymmLQTest::testSolveSaunders12
    public void testSolveSaunders12() {
        saundersTest(2, true, false, 0., 0.);
    }

// org.apache.commons.math3.linear.SymmLQTest::testSolveSaunders13
    public void testSolveSaunders13() {
        saundersTest(1, true, true, 0., 0.);
    }

// org.apache.commons.math3.linear.SymmLQTest::testSolveSaunders14
    public void testSolveSaunders14() {
        saundersTest(2, true, true, 0., 0.);
    }

// org.apache.commons.math3.linear.SymmLQTest::testSolveSaunders15
    public void testSolveSaunders15() {
        saundersTest(5, true, true, 0., 0.);
    }

// org.apache.commons.math3.linear.SymmLQTest::testSolveSaunders16
    public void testSolveSaunders16() {
        saundersTest(5, true, true, 0.25, 0.);
    }

// org.apache.commons.math3.linear.SymmLQTest::testSolveSaunders17
    public void testSolveSaunders17() {
        saundersTest(50, true, false, 0., 0.);
    }

// org.apache.commons.math3.linear.SymmLQTest::testSolveSaunders18
    public void testSolveSaunders18() {
        saundersTest(50, true, false, 0.25, 0.);
    }

// org.apache.commons.math3.linear.SymmLQTest::testSolveSaunders19
    public void testSolveSaunders19() {
        saundersTest(50, true, true, 0., 0.10);
    }

// org.apache.commons.math3.linear.SymmLQTest::testSolveSaunders20
    public void testSolveSaunders20() {
        saundersTest(50, true, true, 0.25, 0.10);
    }

// org.apache.commons.math3.linear.SymmLQTest::testNonSquareOperator
    public void testNonSquareOperator() {
        final Array2DRowRealMatrix a = new Array2DRowRealMatrix(2, 3);
        final IterativeLinearSolver solver;
        solver = new SymmLQ(10, 0., false);
        final ArrayRealVector b = new ArrayRealVector(a.getRowDimension());
        final ArrayRealVector x = new ArrayRealVector(a.getColumnDimension());
        solver.solve(a, b, x);
    }

// org.apache.commons.math3.linear.SymmLQTest::testDimensionMismatchRightHandSide
    public void testDimensionMismatchRightHandSide() {
        final Array2DRowRealMatrix a = new Array2DRowRealMatrix(3, 3);
        final IterativeLinearSolver solver;
        solver = new SymmLQ(10, 0., false);
        final ArrayRealVector b = new ArrayRealVector(2);
        solver.solve(a, b);
    }

// org.apache.commons.math3.linear.SymmLQTest::testDimensionMismatchSolution
    public void testDimensionMismatchSolution() {
        final Array2DRowRealMatrix a = new Array2DRowRealMatrix(3, 3);
        final IterativeLinearSolver solver;
        solver = new SymmLQ(10, 0., false);
        final ArrayRealVector b = new ArrayRealVector(3);
        final ArrayRealVector x = new ArrayRealVector(2);
        solver.solve(a, b, x);
    }

// org.apache.commons.math3.linear.SymmLQTest::testUnpreconditionedSolution
    public void testUnpreconditionedSolution() {
        final int n = 5;
        final int maxIterations = 100;
        final RealLinearOperator a = new HilbertMatrix(n);
        final InverseHilbertMatrix ainv = new InverseHilbertMatrix(n);
        final IterativeLinearSolver solver;
        solver = new SymmLQ(maxIterations, 1E-10, true);
        final RealVector b = new ArrayRealVector(n);
        for (int j = 0; j < n; j++) {
            b.set(0.);
            b.setEntry(j, 1.);
            final RealVector x = solver.solve(a, b);
            for (int i = 0; i < n; i++) {
                final double actual = x.getEntry(i);
                final double expected = ainv.getEntry(i, j);
                final double delta = 1E-6 * Math.abs(expected);
                final String msg = String.format("entry[%d][%d]", i, j);
                Assert.assertEquals(msg, expected, actual, delta);
            }
        }
    }

// org.apache.commons.math3.linear.SymmLQTest::testUnpreconditionedInPlaceSolutionWithInitialGuess
    public void testUnpreconditionedInPlaceSolutionWithInitialGuess() {
        final int n = 5;
        final int maxIterations = 100;
        final RealLinearOperator a = new HilbertMatrix(n);
        final InverseHilbertMatrix ainv = new InverseHilbertMatrix(n);
        final IterativeLinearSolver solver;
        solver = new SymmLQ(maxIterations, 1E-10, true);
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
                final double delta = 1E-6 * Math.abs(expected);
                final String msg = String.format("entry[%d][%d)", i, j);
                Assert.assertEquals(msg, expected, actual, delta);
            }
        }
    }

// org.apache.commons.math3.linear.SymmLQTest::testUnpreconditionedSolutionWithInitialGuess
    public void testUnpreconditionedSolutionWithInitialGuess() {
        final int n = 5;
        final int maxIterations = 100;
        final RealLinearOperator a = new HilbertMatrix(n);
        final InverseHilbertMatrix ainv = new InverseHilbertMatrix(n);
        final IterativeLinearSolver solver;
        solver = new SymmLQ(maxIterations, 1E-10, true);
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
                final double delta = 1E-6 * Math.abs(expected);
                final String msg = String.format("entry[%d][%d]", i, j);
                Assert.assertEquals(msg, expected, actual, delta);
                Assert.assertEquals(msg, x0.getEntry(i), 1., Math.ulp(1.));
            }
        }
    }

// org.apache.commons.math3.linear.SymmLQTest::testNonSquarePreconditioner
    public void testNonSquarePreconditioner() {
        final Array2DRowRealMatrix a = new Array2DRowRealMatrix(2, 2);
        final RealLinearOperator m = new RealLinearOperator() {

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
        };
        final PreconditionedIterativeLinearSolver solver;
        solver = new SymmLQ(10, 0., false);
        final ArrayRealVector b = new ArrayRealVector(a.getRowDimension());
        solver.solve(a, m, b);
    }

// org.apache.commons.math3.linear.SymmLQTest::testMismatchedOperatorDimensions
    public void testMismatchedOperatorDimensions() {
        final Array2DRowRealMatrix a = new Array2DRowRealMatrix(2, 2);
        final RealLinearOperator m = new RealLinearOperator() {

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
        };
        final PreconditionedIterativeLinearSolver solver;
        solver = new SymmLQ(10, 0d, false);
        final ArrayRealVector b = new ArrayRealVector(a.getRowDimension());
        solver.solve(a, m, b);
    }

// org.apache.commons.math3.linear.SymmLQTest::testNonPositiveDefinitePreconditioner
    public void testNonPositiveDefinitePreconditioner() {
        final Array2DRowRealMatrix a = new Array2DRowRealMatrix(2, 2);
        a.setEntry(0, 0, 1d);
        a.setEntry(0, 1, 2d);
        a.setEntry(1, 0, 3d);
        a.setEntry(1, 1, 4d);
        final RealLinearOperator m = new RealLinearOperator() {

            @Override
            public RealVector operate(final RealVector x) {
                final ArrayRealVector y = new ArrayRealVector(2);
                y.setEntry(0, -x.getEntry(0));
                y.setEntry(1, -x.getEntry(1));
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
        };
        final PreconditionedIterativeLinearSolver solver;
        solver = new SymmLQ(10, 0d, true);
        final ArrayRealVector b = new ArrayRealVector(2);
        b.setEntry(0, -1d);
        b.setEntry(1, -1d);
        solver.solve(a, m, b);
    }

// org.apache.commons.math3.linear.SymmLQTest::testPreconditionedSolution
    public void testPreconditionedSolution() {
        final int n = 8;
        final int maxIterations = 100;
        final RealLinearOperator a = new HilbertMatrix(n);
        final InverseHilbertMatrix ainv = new InverseHilbertMatrix(n);
        final RealLinearOperator m = JacobiPreconditioner.create(a);
        final PreconditionedIterativeLinearSolver solver;
        solver = new SymmLQ(maxIterations, 1E-15, true);
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

// org.apache.commons.math3.linear.SymmLQTest::testPreconditionedSolution2
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
        final RealLinearOperator m = JacobiPreconditioner.create(a);
        final PreconditionedIterativeLinearSolver prec;
        final IterativeLinearSolver unprec;
        prec = new SymmLQ(maxIterations, 1E-15, true);
        unprec = new SymmLQ(maxIterations, 1E-15, true);
        final RealVector b = new ArrayRealVector(n);
        final String pattern = "preconditioned SymmLQ (%d iterations) should"
                               + " have been faster than unpreconditioned (%d iterations)";
        String msg;
        for (int j = 0; j < 1; j++) {
            b.set(0.);
            b.setEntry(j, 1.);
            final RealVector px = prec.solve(a, m, b);
            final RealVector x = unprec.solve(a, b);
            final int np = prec.getIterationManager().getIterations();
            final int nup = unprec.getIterationManager().getIterations();
            msg = String.format(pattern, np, nup);
            for (int i = 0; i < n; i++) {
                msg = String.format("row %d, column %d", i, j);
                final double expected = x.getEntry(i);
                final double actual = px.getEntry(i);
                final double delta = 5E-5 * Math.abs(expected);
                Assert.assertEquals(msg, expected, actual, delta);
            }
        }
    }

// org.apache.commons.math3.linear.SymmLQTest::testEventManagement
    public void testEventManagement() {
        final int n = 5;
        final int maxIterations = 100;
        final RealLinearOperator a = new HilbertMatrix(n);
        final IterativeLinearSolver solver;
        
        final int[] count = new int[] {0, 0, 0, 0};
        final RealVector xFromListener = new ArrayRealVector(n);
        final IterationListener listener = new IterationListener() {

            public void initializationPerformed(final IterationEvent e) {
                ++count[0];
            }

            public void iterationPerformed(final IterationEvent e) {
                ++count[2];
                Assert.assertEquals("iteration performed",
                                    count[2],
                                    e.getIterations() - 1);
            }

            public void iterationStarted(final IterationEvent e) {
                ++count[1];
                Assert.assertEquals("iteration started",
                                    count[1],
                                    e.getIterations() - 1);
            }

            public void terminationPerformed(final IterationEvent e) {
                ++count[3];
                final IterativeLinearSolverEvent ilse;
                ilse = (IterativeLinearSolverEvent) e;
                xFromListener.setSubVector(0, ilse.getSolution());
            }
        };
        solver = new SymmLQ(maxIterations, 1E-10, true);
        solver.getIterationManager().addIterationListener(listener);
        final RealVector b = new ArrayRealVector(n);
        for (int j = 0; j < n; j++) {
            Arrays.fill(count, 0);
            b.set(0.);
            b.setEntry(j, 1.);
            final RealVector xFromSolver = solver.solve(a, b);
            String msg = String.format("column %d (initialization)", j);
            Assert.assertEquals(msg, 1, count[0]);
            msg = String.format("column %d (finalization)", j);
            Assert.assertEquals(msg, 1, count[3]);
            
            for (int i = 0; i < n; i++){
                msg = String.format("row %d, column %d", i, j);
                final double expected = xFromSolver.getEntry(i);
                final double actual = xFromListener.getEntry(i);
                Assert.assertEquals(msg, expected, actual, 0.0);
            }
        }
    }

// org.apache.commons.math3.linear.SymmLQTest::testNonSelfAdjointOperator
    public void testNonSelfAdjointOperator() {
        final RealLinearOperator a;
        a = new Array2DRowRealMatrix(new double[][] {
            {1., 2., 3.},
            {2., 4., 5.},
            {2.999, 5., 6.}
        });
        final RealVector b;
        b = new ArrayRealVector(new double[] {1., 1., 1.});
        new SymmLQ(100, 1., true).solve(a, b);
    }

// org.apache.commons.math3.linear.SymmLQTest::testNonSelfAdjointPreconditioner
    public void testNonSelfAdjointPreconditioner() {
        final RealLinearOperator a = new Array2DRowRealMatrix(new double[][] {
            {1., 2., 3.},
            {2., 4., 5.},
            {3., 5., 6.}
        });
        final Array2DRowRealMatrix mMat;
        mMat = new Array2DRowRealMatrix(new double[][] {
            {1., 0., 1.},
            {0., 1., 0.},
            {0., 0., 1.}
        });
        final DecompositionSolver mSolver;
        mSolver = new LUDecomposition(mMat).getSolver();
        final RealLinearOperator minv = new RealLinearOperator() {

            @Override
            public RealVector operate(final RealVector x) {
                return mSolver.solve(x);
            }

            @Override
            public int getRowDimension() {
                return mMat.getRowDimension();
            }

            @Override
            public int getColumnDimension() {
                return mMat.getColumnDimension();
            }
        };
        final RealVector b = new ArrayRealVector(new double[] {
            1., 1., 1.
        });
        new SymmLQ(100, 1., true).solve(a, minv, b);
    }

// org.apache.commons.math3.linear.SymmLQTest::testUnpreconditionedNormOfResidual
    public void testUnpreconditionedNormOfResidual() {
        final int n = 5;
        final int maxIterations = 100;
        final RealLinearOperator a = new HilbertMatrix(n);
        final IterativeLinearSolver solver;
        final IterationListener listener = new IterationListener() {

            private void doTestNormOfResidual(final IterationEvent e) {
                final IterativeLinearSolverEvent evt;
                evt = (IterativeLinearSolverEvent) e;
                final RealVector x = evt.getSolution();
                final RealVector b = evt.getRightHandSideVector();
                final RealVector r = b.subtract(a.operate(x));
                final double rnorm = r.getNorm();
                Assert.assertEquals("iteration performed (residual)",
                    rnorm, evt.getNormOfResidual(),
                    FastMath.max(1E-5 * rnorm, 1E-10));
            }

            public void initializationPerformed(final IterationEvent e) {
                doTestNormOfResidual(e);
            }

            public void iterationPerformed(final IterationEvent e) {
                doTestNormOfResidual(e);
            }

            public void iterationStarted(final IterationEvent e) {
                doTestNormOfResidual(e);
            }

            public void terminationPerformed(final IterationEvent e) {
                doTestNormOfResidual(e);
            }
        };
        solver = new SymmLQ(maxIterations, 1E-10, true);
        solver.getIterationManager().addIterationListener(listener);
        final RealVector b = new ArrayRealVector(n);
        for (int j = 0; j < n; j++) {
            b.set(0.);
            b.setEntry(j, 1.);
            solver.solve(a, b);
        }
    }

// org.apache.commons.math3.linear.SymmLQTest::testPreconditionedNormOfResidual
    public void testPreconditionedNormOfResidual() {
        final int n = 5;
        final int maxIterations = 100;
        final RealLinearOperator a = new HilbertMatrix(n);
        final JacobiPreconditioner m = JacobiPreconditioner.create(a);
        final RealLinearOperator p = m.sqrt();
        final PreconditionedIterativeLinearSolver solver;
        final IterationListener listener = new IterationListener() {

            private void doTestNormOfResidual(final IterationEvent e) {
                final IterativeLinearSolverEvent evt;
                evt = (IterativeLinearSolverEvent) e;
                final RealVector x = evt.getSolution();
                final RealVector b = evt.getRightHandSideVector();
                final RealVector r = b.subtract(a.operate(x));
                final double rnorm = p.operate(r).getNorm();
                Assert.assertEquals("iteration performed (residual)",
                    rnorm, evt.getNormOfResidual(),
                    FastMath.max(1E-5 * rnorm, 1E-10));
            }

            public void initializationPerformed(final IterationEvent e) {
                doTestNormOfResidual(e);
            }

            public void iterationPerformed(final IterationEvent e) {
                doTestNormOfResidual(e);
            }

            public void iterationStarted(final IterationEvent e) {
                doTestNormOfResidual(e);
            }

            public void terminationPerformed(final IterationEvent e) {
                doTestNormOfResidual(e);
            }
        };
        solver = new SymmLQ(maxIterations, 1E-10, true);
        solver.getIterationManager().addIterationListener(listener);
        final RealVector b = new ArrayRealVector(n);
        for (int j = 0; j < n; j++) {
            b.set(0.);
            b.setEntry(j, 1.);
            solver.solve(a, m, b);
        }
    }

// org.apache.commons.math3.linear.TriDiagonalTransformerTest::testNonSquare
    public void testNonSquare() {
        try {
            new TriDiagonalTransformer(MatrixUtils.createRealMatrix(new double[3][2]));
            Assert.fail("an exception should have been thrown");
        } catch (NonSquareMatrixException ime) {
            
        }
    }

// org.apache.commons.math3.linear.TriDiagonalTransformerTest::testAEqualQTQt
    public void testAEqualQTQt() {
        checkAEqualQTQt(MatrixUtils.createRealMatrix(testSquare5));
        checkAEqualQTQt(MatrixUtils.createRealMatrix(testSquare3));
    }

// org.apache.commons.math3.linear.TriDiagonalTransformerTest::testNoAccessBelowDiagonal
    public void testNoAccessBelowDiagonal() {
        checkNoAccessBelowDiagonal(testSquare5);
        checkNoAccessBelowDiagonal(testSquare3);
    }

// org.apache.commons.math3.linear.TriDiagonalTransformerTest::testQOrthogonal
    public void testQOrthogonal() {
        checkOrthogonal(new TriDiagonalTransformer(MatrixUtils.createRealMatrix(testSquare5)).getQ());
        checkOrthogonal(new TriDiagonalTransformer(MatrixUtils.createRealMatrix(testSquare3)).getQ());
    }

// org.apache.commons.math3.linear.TriDiagonalTransformerTest::testQTOrthogonal
    public void testQTOrthogonal() {
        checkOrthogonal(new TriDiagonalTransformer(MatrixUtils.createRealMatrix(testSquare5)).getQT());
        checkOrthogonal(new TriDiagonalTransformer(MatrixUtils.createRealMatrix(testSquare3)).getQT());
    }

// org.apache.commons.math3.linear.TriDiagonalTransformerTest::testTTriDiagonal
    public void testTTriDiagonal() {
        checkTriDiagonal(new TriDiagonalTransformer(MatrixUtils.createRealMatrix(testSquare5)).getT());
        checkTriDiagonal(new TriDiagonalTransformer(MatrixUtils.createRealMatrix(testSquare3)).getT());
    }

// org.apache.commons.math3.linear.TriDiagonalTransformerTest::testMatricesValues5
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

// org.apache.commons.math3.linear.TriDiagonalTransformerTest::testMatricesValues3
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

// org.apache.commons.math3.ode.ContinuousOutputModelTest::testBoundaries
  public void testBoundaries() throws DimensionMismatchException, NumberIsTooSmallException, MaxCountExceededException, NoBracketingException {
    integ.addStepHandler(new ContinuousOutputModel());
    integ.integrate(pb,
                    pb.getInitialTime(), pb.getInitialState(),
                    pb.getFinalTime(), new double[pb.getDimension()]);
    ContinuousOutputModel cm = (ContinuousOutputModel) integ.getStepHandlers().iterator().next();
    cm.setInterpolatedTime(2.0 * pb.getInitialTime() - pb.getFinalTime());
    cm.setInterpolatedTime(2.0 * pb.getFinalTime() - pb.getInitialTime());
    cm.setInterpolatedTime(0.5 * (pb.getFinalTime() + pb.getInitialTime()));
  }

// org.apache.commons.math3.ode.ContinuousOutputModelTest::testRandomAccess
  public void testRandomAccess() throws DimensionMismatchException, NumberIsTooSmallException, MaxCountExceededException, NoBracketingException {

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

    Assert.assertTrue(maxError < 1.0e-9);

  }

// org.apache.commons.math3.ode.ContinuousOutputModelTest::testModelsMerging
  public void testModelsMerging() throws MaxCountExceededException, MathIllegalArgumentException {

      
      FirstOrderDifferentialEquations problem =
          new FirstOrderDifferentialEquations() {
              public void computeDerivatives(double t, double[] y, double[] dot) {
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

      
      Assert.assertEquals(2.0 * FastMath.PI, cm.getInitialTime(), 1.0e-12);
      Assert.assertEquals(0, cm.getFinalTime(), 1.0e-12);
      Assert.assertEquals(cm.getFinalTime(), cm.getInterpolatedTime(), 1.0e-12);
      for (double t = 0; t < 2.0 * FastMath.PI; t += 0.1) {
          cm.setInterpolatedTime(t);
          double[] y = cm.getInterpolatedState();
          Assert.assertEquals(FastMath.cos(t), y[0], 1.0e-7);
          Assert.assertEquals(FastMath.sin(t), y[1], 1.0e-7);
      }

  }

// org.apache.commons.math3.ode.ContinuousOutputModelTest::testErrorConditions
  public void testErrorConditions() throws MaxCountExceededException, MathIllegalArgumentException {

      ContinuousOutputModel cm = new ContinuousOutputModel();
      cm.handleStep(buildInterpolator(0, new double[] { 0.0, 1.0, -2.0 }, 1), true);

      
      Assert.assertTrue(checkAppendError(cm, 1.0, new double[] { 0.0, 1.0 }, 2.0));

      
      Assert.assertTrue(checkAppendError(cm, 10.0, new double[] { 0.0, 1.0, -2.0 }, 20.0));

      
      Assert.assertTrue(checkAppendError(cm, 1.0, new double[] { 0.0, 1.0, -2.0 }, 0.0));

      
      Assert.assertFalse(checkAppendError(cm, 1.0, new double[] { 0.0, 1.0, -2.0 }, 2.0));

  }

// org.apache.commons.math3.ode.FirstOrderConverterTest::testDoubleDimension
  public void testDoubleDimension() {
    for (int i = 1; i < 10; ++i) {
      SecondOrderDifferentialEquations eqn2 = new Equations(i, 0.2);
      FirstOrderConverter eqn1 = new FirstOrderConverter(eqn2);
      Assert.assertTrue(eqn1.getDimension() == (2 * eqn2.getDimension()));
    }
  }

// org.apache.commons.math3.ode.FirstOrderConverterTest::testDecreasingSteps
  public void testDecreasingSteps()
      throws DimensionMismatchException, NumberIsTooSmallException, MaxCountExceededException, NoBracketingException {

    double previousError = Double.NaN;
    for (int i = 0; i < 10; ++i) {

      double step  = FastMath.pow(2.0, -(i + 1));
      double error = integrateWithSpecifiedStep(4.0, 0.0, 1.0, step)
                   - FastMath.sin(4.0);
      if (i > 0) {
        Assert.assertTrue(FastMath.abs(error) < FastMath.abs(previousError));
      }
      previousError = error;

    }
  }

// org.apache.commons.math3.ode.FirstOrderConverterTest::testSmallStep
  public void testSmallStep()
      throws DimensionMismatchException, NumberIsTooSmallException, MaxCountExceededException, NoBracketingException {
    double error = integrateWithSpecifiedStep(4.0, 0.0, 1.0, 1.0e-4)
                   - FastMath.sin(4.0);
    Assert.assertTrue(FastMath.abs(error) < 1.0e-10);
  }

// org.apache.commons.math3.ode.FirstOrderConverterTest::testBigStep
  public void testBigStep()
      throws DimensionMismatchException, NumberIsTooSmallException, MaxCountExceededException, NoBracketingException {
    double error = integrateWithSpecifiedStep(4.0, 0.0, 1.0, 0.5)
                   - FastMath.sin(4.0);
    Assert.assertTrue(FastMath.abs(error) > 0.1);
  }

// org.apache.commons.math3.ode.JacobianMatricesTest::testLowAccuracyExternalDifferentiation
    public void testLowAccuracyExternalDifferentiation()
        throws NumberIsTooSmallException, DimensionMismatchException,
               MaxCountExceededException, NoBracketingException {
        
        
        
        
        
        
        
        
        
        FirstOrderIntegrator integ =
            new DormandPrince54Integrator(1.0e-8, 100.0, new double[] { 1.0e-4, 1.0e-4 }, new double[] { 1.0e-4, 1.0e-4 });
        double hP = 1.0e-12;
        SummaryStatistics residualsP0 = new SummaryStatistics();
        SummaryStatistics residualsP1 = new SummaryStatistics();
        for (double b = 2.88; b < 3.08; b += 0.001) {
            Brusselator brusselator = new Brusselator(b);
            double[] y = { 1.3, b };
            integ.integrate(brusselator, 0, y, 20.0, y);
            double[] yP = { 1.3, b + hP };
            integ.integrate(brusselator, 0, yP, 20.0, yP);
            residualsP0.addValue((yP[0] - y[0]) / hP - brusselator.dYdP0());
            residualsP1.addValue((yP[1] - y[1]) / hP - brusselator.dYdP1());
        }
        Assert.assertTrue((residualsP0.getMax() - residualsP0.getMin()) > 500);
        Assert.assertTrue(residualsP0.getStandardDeviation() > 30);
        Assert.assertTrue((residualsP1.getMax() - residualsP1.getMin()) > 700);
        Assert.assertTrue(residualsP1.getStandardDeviation() > 40);
    }

// org.apache.commons.math3.ode.JacobianMatricesTest::testHighAccuracyExternalDifferentiation
    public void testHighAccuracyExternalDifferentiation()
        throws NumberIsTooSmallException, DimensionMismatchException,
               MaxCountExceededException, NoBracketingException, UnknownParameterException {
        FirstOrderIntegrator integ =
            new DormandPrince54Integrator(1.0e-8, 100.0, new double[] { 1.0e-10, 1.0e-10 }, new double[] { 1.0e-10, 1.0e-10 });
        double hP = 1.0e-12;
        SummaryStatistics residualsP0 = new SummaryStatistics();
        SummaryStatistics residualsP1 = new SummaryStatistics();
        for (double b = 2.88; b < 3.08; b += 0.001) {
            ParamBrusselator brusselator = new ParamBrusselator(b);
            double[] y = { 1.3, b };
            integ.integrate(brusselator, 0, y, 20.0, y);
            double[] yP = { 1.3, b + hP };
            brusselator.setParameter("b", b + hP);
            integ.integrate(brusselator, 0, yP, 20.0, yP);
            residualsP0.addValue((yP[0] - y[0]) / hP - brusselator.dYdP0());
            residualsP1.addValue((yP[1] - y[1]) / hP - brusselator.dYdP1());
        }
        Assert.assertTrue((residualsP0.getMax() - residualsP0.getMin()) > 0.02);
        Assert.assertTrue((residualsP0.getMax() - residualsP0.getMin()) < 0.03);
        Assert.assertTrue(residualsP0.getStandardDeviation() > 0.003);
        Assert.assertTrue(residualsP0.getStandardDeviation() < 0.004);
        Assert.assertTrue((residualsP1.getMax() - residualsP1.getMin()) > 0.04);
        Assert.assertTrue((residualsP1.getMax() - residualsP1.getMin()) < 0.05);
        Assert.assertTrue(residualsP1.getStandardDeviation() > 0.007);
        Assert.assertTrue(residualsP1.getStandardDeviation() < 0.008);
    }

// org.apache.commons.math3.ode.JacobianMatricesTest::testInternalDifferentiation
    public void testInternalDifferentiation()
        throws NumberIsTooSmallException, DimensionMismatchException,
               MaxCountExceededException, NoBracketingException,
               UnknownParameterException, MismatchedEquations {
        AbstractIntegrator integ =
            new DormandPrince54Integrator(1.0e-8, 100.0, new double[] { 1.0e-4, 1.0e-4 }, new double[] { 1.0e-4, 1.0e-4 });
        double hP = 1.0e-12;
        double hY = 1.0e-12;
        SummaryStatistics residualsP0 = new SummaryStatistics();
        SummaryStatistics residualsP1 = new SummaryStatistics();
        for (double b = 2.88; b < 3.08; b += 0.001) {
            ParamBrusselator brusselator = new ParamBrusselator(b);
            brusselator.setParameter(ParamBrusselator.B, b);
            double[] z = { 1.3, b };
            double[][] dZdZ0 = new double[2][2];
            double[]   dZdP  = new double[2];

            JacobianMatrices jacob = new JacobianMatrices(brusselator, new double[] { hY, hY }, ParamBrusselator.B);
            jacob.setParameterizedODE(brusselator);
            jacob.setParameterStep(ParamBrusselator.B, hP);
            jacob.setInitialParameterJacobian(ParamBrusselator.B, new double[] { 0.0, 1.0 });

            ExpandableStatefulODE efode = new ExpandableStatefulODE(brusselator);
            efode.setTime(0);
            efode.setPrimaryState(z);
            jacob.registerVariationalEquations(efode);

            integ.setMaxEvaluations(5000);
            integ.integrate(efode, 20.0);
            jacob.getCurrentMainSetJacobian(dZdZ0);
            jacob.getCurrentParameterJacobian(ParamBrusselator.B, dZdP);

            residualsP0.addValue(dZdP[0] - brusselator.dYdP0());
            residualsP1.addValue(dZdP[1] - brusselator.dYdP1());
        }
        Assert.assertTrue((residualsP0.getMax() - residualsP0.getMin()) < 0.02);
        Assert.assertTrue(residualsP0.getStandardDeviation() < 0.003);
        Assert.assertTrue((residualsP1.getMax() - residualsP1.getMin()) < 0.05);
        Assert.assertTrue(residualsP1.getStandardDeviation() < 0.01);
    }

// org.apache.commons.math3.ode.JacobianMatricesTest::testAnalyticalDifferentiation
    public void testAnalyticalDifferentiation()
        throws MaxCountExceededException, DimensionMismatchException,
               NumberIsTooSmallException, NoBracketingException,
               UnknownParameterException, MismatchedEquations {
        AbstractIntegrator integ =
            new DormandPrince54Integrator(1.0e-8, 100.0, new double[] { 1.0e-4, 1.0e-4 }, new double[] { 1.0e-4, 1.0e-4 });
        SummaryStatistics residualsP0 = new SummaryStatistics();
        SummaryStatistics residualsP1 = new SummaryStatistics();
        for (double b = 2.88; b < 3.08; b += 0.001) {
            Brusselator brusselator = new Brusselator(b);
            double[] z = { 1.3, b };
            double[][] dZdZ0 = new double[2][2];
            double[]   dZdP  = new double[2];

            JacobianMatrices jacob = new JacobianMatrices(brusselator, Brusselator.B);
            jacob.addParameterJacobianProvider(brusselator);
            jacob.setInitialParameterJacobian(Brusselator.B, new double[] { 0.0, 1.0 });

            ExpandableStatefulODE efode = new ExpandableStatefulODE(brusselator);
            efode.setTime(0);
            efode.setPrimaryState(z);
            jacob.registerVariationalEquations(efode);

            integ.setMaxEvaluations(5000);
            integ.integrate(efode, 20.0);
            jacob.getCurrentMainSetJacobian(dZdZ0);
            jacob.getCurrentParameterJacobian(Brusselator.B, dZdP);

            residualsP0.addValue(dZdP[0] - brusselator.dYdP0());
            residualsP1.addValue(dZdP[1] - brusselator.dYdP1());
        }
        Assert.assertTrue((residualsP0.getMax() - residualsP0.getMin()) < 0.014);
        Assert.assertTrue(residualsP0.getStandardDeviation() < 0.003);
        Assert.assertTrue((residualsP1.getMax() - residualsP1.getMin()) < 0.05);
        Assert.assertTrue(residualsP1.getStandardDeviation() < 0.01);
    }

// org.apache.commons.math3.ode.JacobianMatricesTest::testFinalResult
    public void testFinalResult()
        throws MaxCountExceededException, DimensionMismatchException,
               NumberIsTooSmallException, NoBracketingException,
               UnknownParameterException, MismatchedEquations {

        AbstractIntegrator integ =
            new DormandPrince54Integrator(1.0e-8, 100.0, new double[] { 1.0e-10, 1.0e-10 }, new double[] { 1.0e-10, 1.0e-10 });
        double[] y = new double[] { 0.0, 1.0 };
        Circle circle = new Circle(y, 1.0, 1.0, 0.1);

        JacobianMatrices jacob = new JacobianMatrices(circle, Circle.CX, Circle.CY, Circle.OMEGA);
        jacob.addParameterJacobianProvider(circle);
        jacob.setInitialMainStateJacobian(circle.exactDyDy0(0));
        jacob.setInitialParameterJacobian(Circle.CX, circle.exactDyDcx(0));
        jacob.setInitialParameterJacobian(Circle.CY, circle.exactDyDcy(0));
        jacob.setInitialParameterJacobian(Circle.OMEGA, circle.exactDyDom(0));

        ExpandableStatefulODE efode = new ExpandableStatefulODE(circle);
        efode.setTime(0);
        efode.setPrimaryState(y);
        jacob.registerVariationalEquations(efode);

        integ.setMaxEvaluations(5000);

        double t = 18 * FastMath.PI;
        integ.integrate(efode, t);
        y = efode.getPrimaryState();
        for (int i = 0; i < y.length; ++i) {
            Assert.assertEquals(circle.exactY(t)[i], y[i], 1.0e-9);
        }

        double[][] dydy0 = new double[2][2];
        jacob.getCurrentMainSetJacobian(dydy0);
        for (int i = 0; i < dydy0.length; ++i) {
            for (int j = 0; j < dydy0[i].length; ++j) {
                Assert.assertEquals(circle.exactDyDy0(t)[i][j], dydy0[i][j], 1.0e-9);
            }
        }
        double[] dydcx = new double[2];
        jacob.getCurrentParameterJacobian(Circle.CX, dydcx);
        for (int i = 0; i < dydcx.length; ++i) {
            Assert.assertEquals(circle.exactDyDcx(t)[i], dydcx[i], 1.0e-7);
        }
        double[] dydcy = new double[2];
        jacob.getCurrentParameterJacobian(Circle.CY, dydcy);
        for (int i = 0; i < dydcy.length; ++i) {
            Assert.assertEquals(circle.exactDyDcy(t)[i], dydcy[i], 1.0e-7);
        }
        double[] dydom = new double[2];
        jacob.getCurrentParameterJacobian(Circle.OMEGA, dydom);
        for (int i = 0; i < dydom.length; ++i) {
            Assert.assertEquals(circle.exactDyDom(t)[i], dydom[i], 1.0e-7);
        }
    }

// org.apache.commons.math3.ode.JacobianMatricesTest::testParameterizable
    public void testParameterizable()
        throws MaxCountExceededException, DimensionMismatchException,
               NumberIsTooSmallException, NoBracketingException,
               UnknownParameterException, MismatchedEquations {

        AbstractIntegrator integ =
            new DormandPrince54Integrator(1.0e-8, 100.0, new double[] { 1.0e-10, 1.0e-10 }, new double[] { 1.0e-10, 1.0e-10 });
        double[] y = new double[] { 0.0, 1.0 };
        ParameterizedCircle pcircle = new ParameterizedCircle(y, 1.0, 1.0, 0.1);

        double hP = 1.0e-12;
        double hY = 1.0e-12;

        JacobianMatrices jacob = new JacobianMatrices(pcircle, new double[] { hY, hY },
                                                      ParameterizedCircle.CX, ParameterizedCircle.CY,
                                                      ParameterizedCircle.OMEGA);
        jacob.setParameterizedODE(pcircle);
        jacob.setParameterStep(ParameterizedCircle.CX,    hP);
        jacob.setParameterStep(ParameterizedCircle.CY,    hP);
        jacob.setParameterStep(ParameterizedCircle.OMEGA, hP);
        jacob.setInitialMainStateJacobian(pcircle.exactDyDy0(0));
        jacob.setInitialParameterJacobian(ParameterizedCircle.CX, pcircle.exactDyDcx(0));
        jacob.setInitialParameterJacobian(ParameterizedCircle.CY, pcircle.exactDyDcy(0));
        jacob.setInitialParameterJacobian(ParameterizedCircle.OMEGA, pcircle.exactDyDom(0));

        ExpandableStatefulODE efode = new ExpandableStatefulODE(pcircle);
        efode.setTime(0);
        efode.setPrimaryState(y);
        jacob.registerVariationalEquations(efode);

        integ.setMaxEvaluations(50000);

        double t = 18 * FastMath.PI;
        integ.integrate(efode, t);
        y = efode.getPrimaryState();
        for (int i = 0; i < y.length; ++i) {
            Assert.assertEquals(pcircle.exactY(t)[i], y[i], 1.0e-9);
        }

        double[][] dydy0 = new double[2][2];
        jacob.getCurrentMainSetJacobian(dydy0);
        for (int i = 0; i < dydy0.length; ++i) {
            for (int j = 0; j < dydy0[i].length; ++j) {
                Assert.assertEquals(pcircle.exactDyDy0(t)[i][j], dydy0[i][j], 5.0e-4);
            }
        }

        double[] dydp0 = new double[2];
        jacob.getCurrentParameterJacobian(ParameterizedCircle.CX, dydp0);
        for (int i = 0; i < dydp0.length; ++i) {
            Assert.assertEquals(pcircle.exactDyDcx(t)[i], dydp0[i], 5.0e-4);
        }

        double[] dydp1 = new double[2];
        jacob.getCurrentParameterJacobian(ParameterizedCircle.OMEGA, dydp1);
        for (int i = 0; i < dydp1.length; ++i) {
            Assert.assertEquals(pcircle.exactDyDom(t)[i], dydp1[i], 1.0e-2);
        }
    }

// org.apache.commons.math3.ode.events.EventStateTest::closeEvents
    public void closeEvents() throws MaxCountExceededException, NoBracketingException {

        final double r1  = 90.0;
        final double r2  = 135.0;
        final double gap = r2 - r1;
        EventHandler closeEventsGenerator = new EventHandler() {
            public void init(double t0, double[] y0, double t) {
            }
            public void resetState(double t, double[] y) {
            }
            public double g(double t, double[] y) {
                return (t - r1) * (r2 - t);
            }
            public Action eventOccurred(double t, double[] y, boolean increasing) {
                return Action.CONTINUE;
            }
        };

        final double tolerance = 0.1;
        EventState es = new EventState(closeEventsGenerator, 1.5 * gap,
                                       tolerance, 100,
                                       new BrentSolver(tolerance));

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

// org.apache.commons.math3.ode.events.EventStateTest::testIssue695
    public void testIssue695()
        throws DimensionMismatchException, NumberIsTooSmallException,
               MaxCountExceededException, NoBracketingException {

        FirstOrderDifferentialEquations equation = new FirstOrderDifferentialEquations() {
            
            public int getDimension() {
                return 1;
            }
            
            public void computeDerivatives(double t, double[] y, double[] yDot) {
                yDot[0] = 1.0;
            }
        };

        DormandPrince853Integrator integrator = new DormandPrince853Integrator(0.001, 1000, 1.0e-14, 1.0e-14);
        integrator.addEventHandler(new ResettingEvent(10.99), 0.1, 1.0e-9, 1000);
        integrator.addEventHandler(new ResettingEvent(11.01), 0.1, 1.0e-9, 1000);
        integrator.setInitialStepSize(3.0);

        double target = 30.0;
        double[] y = new double[1];
        double tEnd = integrator.integrate(equation, 0.0, y, target, y);
        Assert.assertEquals(target, tEnd, 1.0e-10);
        Assert.assertEquals(32.0, y[0], 1.0e-10);

    }

// org.apache.commons.math3.ode.events.OverlappingEventsTest::testOverlappingEvents0
    public void testOverlappingEvents0()
        throws DimensionMismatchException, NumberIsTooSmallException,
               MaxCountExceededException, NoBracketingException {
        test(0);
    }

// org.apache.commons.math3.ode.events.OverlappingEventsTest::testOverlappingEvents1
    public void testOverlappingEvents1()
        throws DimensionMismatchException, NumberIsTooSmallException,
               MaxCountExceededException, NoBracketingException {
        test(1);
    }

// org.apache.commons.math3.ode.events.OverlappingEventsTest::test
    public void test(int eventType)
        throws DimensionMismatchException, NumberIsTooSmallException,
               MaxCountExceededException, NoBracketingException {
        double e = 1e-15;
        FirstOrderIntegrator integrator = new DormandPrince853Integrator(e, 100.0, 1e-7, 1e-7);
        BaseSecantSolver rootSolver = new PegasusSolver(e, e);
        EventHandler evt1 = new Event(0, eventType);
        EventHandler evt2 = new Event(1, eventType);
        integrator.addEventHandler(evt1, 0.1, e, 999, rootSolver);
        integrator.addEventHandler(evt2, 0.1, e, 999, rootSolver);
        double t = 0.0;
        double tEnd = 10.0;
        double[] y = {0.0, 0.0};
        List<Double> events1 = new ArrayList<Double>();
        List<Double> events2 = new ArrayList<Double>();
        while (t < tEnd) {
            t = integrator.integrate(this, t, y, tEnd, y);
            

            if (y[0] >= 1.0) {
                y[0] = 0.0;
                events1.add(t);
                
            }
            if (y[1] >= 1.0) {
                y[1] = 0.0;
                events2.add(t);
                
            }
        }
        Assert.assertEquals(EVENT_TIMES1.length, events1.size());
        Assert.assertEquals(EVENT_TIMES2.length, events2.size());
        for(int i = 0; i < EVENT_TIMES1.length; i++) {
            Assert.assertEquals(EVENT_TIMES1[i], events1.get(i), 1e-7);
        }
        for(int i = 0; i < EVENT_TIMES2.length; i++) {
            Assert.assertEquals(EVENT_TIMES2[i], events2.get(i), 1e-7);
        }
        
    }

// org.apache.commons.math3.ode.events.ReappearingEventTest::testDormandPrince
    public void testDormandPrince()
        throws DimensionMismatchException, NumberIsTooSmallException,
               MaxCountExceededException, NoBracketingException {
        double tEnd = test(1);
        assertEquals(10.0, tEnd, 1e-7);
    }

// org.apache.commons.math3.ode.events.ReappearingEventTest::testGragg
    public void testGragg()
        throws DimensionMismatchException, NumberIsTooSmallException,
               MaxCountExceededException, NoBracketingException {
        double tEnd = test(2);
        assertEquals(10.0, tEnd, 1e-7);
    }

// org.apache.commons.math3.ode.events.ReappearingEventTest::test
    public double test(int integratorType)
        throws DimensionMismatchException, NumberIsTooSmallException,
               MaxCountExceededException, NoBracketingException {
        double e = 1e-15;
        FirstOrderIntegrator integrator;
        integrator = (integratorType == 1)
                     ? new DormandPrince853Integrator(e, 100.0, 1e-7, 1e-7)
                     : new GraggBulirschStoerIntegrator(e, 100.0, 1e-7, 1e-7);
        PegasusSolver rootSolver = new PegasusSolver(e, e);
        integrator.addEventHandler(new Event(), 0.1, e, 1000, rootSolver);
        double t0 = 6.0;
        double tEnd = 10.0;
        double[] y = {2.0, 2.0, 2.0, 4.0, 2.0, 7.0, 15.0};
        return integrator.integrate(new Ode(), t0, y, tEnd, y);
    }

// org.apache.commons.math3.ode.nonstiff.AdamsBashforthIntegratorTest::dimensionCheck
    public void dimensionCheck() throws NumberIsTooSmallException, DimensionMismatchException, MaxCountExceededException, NoBracketingException {
        TestProblem1 pb = new TestProblem1();
        FirstOrderIntegrator integ =
            new AdamsBashforthIntegrator(2, 0.0, 1.0, 1.0e-10, 1.0e-10);
        integ.integrate(pb,
                        0.0, new double[pb.getDimension()+10],
                        1.0, new double[pb.getDimension()+10]);
    }

// org.apache.commons.math3.ode.nonstiff.AdamsBashforthIntegratorTest::testMinStep
    public void testMinStep() throws DimensionMismatchException, NumberIsTooSmallException, MaxCountExceededException, NoBracketingException {

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

// org.apache.commons.math3.ode.nonstiff.AdamsBashforthIntegratorTest::testIncreasingTolerance
    public void testIncreasingTolerance() throws DimensionMismatchException, NumberIsTooSmallException, MaxCountExceededException, NoBracketingException
        {

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

            
            
            
            Assert.assertTrue(handler.getMaximalValueError() > (50.0 * scalAbsoluteTolerance));
            Assert.assertTrue(handler.getMaximalValueError() < (300.0 * scalAbsoluteTolerance));
            Assert.assertEquals(0, handler.getMaximalTimeError(), 1.0e-16);

            int calls = pb.getCalls();
            Assert.assertEquals(integ.getEvaluations(), calls);
            Assert.assertTrue(calls <= previousCalls);
            previousCalls = calls;

        }

    }

// org.apache.commons.math3.ode.nonstiff.AdamsBashforthIntegratorTest::exceedMaxEvaluations
    public void exceedMaxEvaluations() throws DimensionMismatchException, NumberIsTooSmallException, MaxCountExceededException, NoBracketingException {

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

// org.apache.commons.math3.ode.nonstiff.AdamsBashforthIntegratorTest::backward
    public void backward() throws DimensionMismatchException, NumberIsTooSmallException, MaxCountExceededException, NoBracketingException {

        TestProblem5 pb = new TestProblem5();
        double range = FastMath.abs(pb.getFinalTime() - pb.getInitialTime());

        FirstOrderIntegrator integ = new AdamsBashforthIntegrator(4, 0, range, 1.0e-12, 1.0e-12);
        TestProblemHandler handler = new TestProblemHandler(pb, integ);
        integ.addStepHandler(handler);
        integ.integrate(pb, pb.getInitialTime(), pb.getInitialState(),
                        pb.getFinalTime(), new double[pb.getDimension()]);

        Assert.assertTrue(handler.getLastError() < 1.5e-8);
        Assert.assertTrue(handler.getMaximalValueError() < 1.5e-8);
        Assert.assertEquals(0, handler.getMaximalTimeError(), 1.0e-16);
        Assert.assertEquals("Adams-Bashforth", integ.getName());
    }

// org.apache.commons.math3.ode.nonstiff.AdamsBashforthIntegratorTest::polynomial
    public void polynomial() throws DimensionMismatchException, NumberIsTooSmallException, MaxCountExceededException, NoBracketingException {
        TestProblem6 pb = new TestProblem6();
        double range = FastMath.abs(pb.getFinalTime() - pb.getInitialTime());

        for (int nSteps = 2; nSteps < 8; ++nSteps) {
            AdamsBashforthIntegrator integ =
                new AdamsBashforthIntegrator(nSteps, 1.0e-6 * range, 0.1 * range, 1.0e-5, 1.0e-5);
            TestProblemHandler handler = new TestProblemHandler(pb, integ);
            integ.addStepHandler(handler);
            integ.integrate(pb, pb.getInitialTime(), pb.getInitialState(),
                            pb.getFinalTime(), new double[pb.getDimension()]);
            if (nSteps < 4) {
                Assert.assertTrue(handler.getMaximalValueError() > 1.0e-03);
            } else {
                Assert.assertTrue(handler.getMaximalValueError() < 4.0e-12);
            }
        }

    }

// org.apache.commons.math3.ode.nonstiff.AdamsMoultonIntegratorTest::dimensionCheck
    public void dimensionCheck()
        throws DimensionMismatchException, NumberIsTooSmallException,
               MaxCountExceededException, NoBracketingException {
        TestProblem1 pb = new TestProblem1();
        FirstOrderIntegrator integ =
            new AdamsMoultonIntegrator(2, 0.0, 1.0, 1.0e-10, 1.0e-10);
        integ.integrate(pb,
                        0.0, new double[pb.getDimension()+10],
                        1.0, new double[pb.getDimension()+10]);
    }

// org.apache.commons.math3.ode.nonstiff.AdamsMoultonIntegratorTest::testMinStep
    public void testMinStep()
            throws DimensionMismatchException, NumberIsTooSmallException,
            MaxCountExceededException, NoBracketingException {

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

// org.apache.commons.math3.ode.nonstiff.AdamsMoultonIntegratorTest::testIncreasingTolerance
    public void testIncreasingTolerance()
            throws DimensionMismatchException, NumberIsTooSmallException,
            MaxCountExceededException, NoBracketingException {

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

            
            
            
            Assert.assertTrue(handler.getMaximalValueError() > ( 0.5 * scalAbsoluteTolerance));
            Assert.assertTrue(handler.getMaximalValueError() < (11.0 * scalAbsoluteTolerance));
            Assert.assertEquals(0, handler.getMaximalTimeError(), 1.0e-16);

            int calls = pb.getCalls();
            Assert.assertEquals(integ.getEvaluations(), calls);
            Assert.assertTrue(calls <= previousCalls);
            previousCalls = calls;

        }

    }

// org.apache.commons.math3.ode.nonstiff.AdamsMoultonIntegratorTest::exceedMaxEvaluations
    public void exceedMaxEvaluations()
            throws DimensionMismatchException, NumberIsTooSmallException,
            MaxCountExceededException, NoBracketingException {

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

// org.apache.commons.math3.ode.nonstiff.AdamsMoultonIntegratorTest::backward
    public void backward()
            throws DimensionMismatchException, NumberIsTooSmallException,
            MaxCountExceededException, NoBracketingException {

        TestProblem5 pb = new TestProblem5();
        double range = FastMath.abs(pb.getFinalTime() - pb.getInitialTime());

        FirstOrderIntegrator integ = new AdamsMoultonIntegrator(4, 0, range, 1.0e-12, 1.0e-12);
        TestProblemHandler handler = new TestProblemHandler(pb, integ);
        integ.addStepHandler(handler);
        integ.integrate(pb, pb.getInitialTime(), pb.getInitialState(),
                        pb.getFinalTime(), new double[pb.getDimension()]);

        Assert.assertTrue(handler.getLastError() < 1.0e-9);
        Assert.assertTrue(handler.getMaximalValueError() < 1.0e-9);
        Assert.assertEquals(0, handler.getMaximalTimeError(), 1.0e-16);
        Assert.assertEquals("Adams-Moulton", integ.getName());
    }

// org.apache.commons.math3.ode.nonstiff.AdamsMoultonIntegratorTest::polynomial
    public void polynomial()
            throws DimensionMismatchException, NumberIsTooSmallException,
            MaxCountExceededException, NoBracketingException {
        TestProblem6 pb = new TestProblem6();
        double range = FastMath.abs(pb.getFinalTime() - pb.getInitialTime());

        for (int nSteps = 2; nSteps < 8; ++nSteps) {
            AdamsMoultonIntegrator integ =
                new AdamsMoultonIntegrator(nSteps, 1.0e-6 * range, 0.1 * range, 1.0e-5, 1.0e-5);
            TestProblemHandler handler = new TestProblemHandler(pb, integ);
            integ.addStepHandler(handler);
            integ.integrate(pb, pb.getInitialTime(), pb.getInitialState(),
                            pb.getFinalTime(), new double[pb.getDimension()]);
            if (nSteps < 4) {
                Assert.assertTrue(handler.getMaximalValueError() > 7.0e-04);
            } else {
                Assert.assertTrue(handler.getMaximalValueError() < 3.0e-13);
            }
        }

    }

// org.apache.commons.math3.ode.nonstiff.ClassicalRungeKuttaIntegratorTest::testMissedEndEvent
  public void testMissedEndEvent()
      throws DimensionMismatchException, NumberIsTooSmallException,
             MaxCountExceededException, NoBracketingException {
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

          public void init(double t0, double[] y0, double t) {
          }

          public void resetState(double t, double[] y) {
          }

          public double g(double t, double[] y) {
              return t - tEvent;
          }

          public Action eventOccurred(double t, double[] y, boolean increasing) {
              Assert.assertEquals(tEvent, t, 5.0e-6);
              return Action.CONTINUE;
          }
      }, Double.POSITIVE_INFINITY, 1.0e-20, 100);
      finalT = integrator.integrate(ode, t0, y0, tEvent + 120, y);
      Assert.assertEquals(tEvent + 120, finalT, 5.0e-6);
      for (int i = 0; i < y.length; ++i) {
          Assert.assertEquals(y0[i] * FastMath.exp(k[i] * (finalT - t0)), y[i], 1.0e-9);
      }

  }

// org.apache.commons.math3.ode.nonstiff.ClassicalRungeKuttaIntegratorTest::testSanityChecks
  public void testSanityChecks()
      throws DimensionMismatchException, NumberIsTooSmallException,
             MaxCountExceededException, NoBracketingException {
    try  {
      TestProblem1 pb = new TestProblem1();
      new ClassicalRungeKuttaIntegrator(0.01).integrate(pb,
                                                        0.0, new double[pb.getDimension()+10],
                                                        1.0, new double[pb.getDimension()]);
        Assert.fail("an exception should have been thrown");
    } catch(DimensionMismatchException ie) {
    }
    try  {
        TestProblem1 pb = new TestProblem1();
        new ClassicalRungeKuttaIntegrator(0.01).integrate(pb,
                                                          0.0, new double[pb.getDimension()],
                                                          1.0, new double[pb.getDimension()+10]);
          Assert.fail("an exception should have been thrown");
      } catch(DimensionMismatchException ie) {
      }
    try  {
      TestProblem1 pb = new TestProblem1();
      new ClassicalRungeKuttaIntegrator(0.01).integrate(pb,
                                                        0.0, new double[pb.getDimension()],
                                                        0.0, new double[pb.getDimension()]);
        Assert.fail("an exception should have been thrown");
    } catch(NumberIsTooSmallException ie) {
    }
  }

// org.apache.commons.math3.ode.nonstiff.ClassicalRungeKuttaIntegratorTest::testDecreasingSteps
  public void testDecreasingSteps()
      throws DimensionMismatchException, NumberIsTooSmallException,
             MaxCountExceededException, NoBracketingException {

    TestProblemAbstract[] problems = TestProblemFactory.getProblems();
    for (int k = 0; k < problems.length; ++k) {

      double previousValueError = Double.NaN;
      double previousTimeError = Double.NaN;
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
        Assert.assertEquals(functions.length, integ.getEventHandlers().size());
        double stopTime = integ.integrate(pb, pb.getInitialTime(), pb.getInitialState(),
                                          pb.getFinalTime(), new double[pb.getDimension()]);
        if (functions.length == 0) {
            Assert.assertEquals(pb.getFinalTime(), stopTime, 1.0e-10);
        }

        double error = handler.getMaximalValueError();
        if (i > 4) {
          Assert.assertTrue(error < 1.01 * FastMath.abs(previousValueError));
        }
        previousValueError = error;

        double timeError = handler.getMaximalTimeError();
        if (i > 4) {
          Assert.assertTrue(timeError <= FastMath.abs(previousTimeError));
        }
        previousTimeError = timeError;

        integ.clearEventHandlers();
        Assert.assertEquals(0, integ.getEventHandlers().size());
      }

    }

  }

// org.apache.commons.math3.ode.nonstiff.ClassicalRungeKuttaIntegratorTest::testSmallStep
  public void testSmallStep()
      throws DimensionMismatchException, NumberIsTooSmallException,
             MaxCountExceededException, NoBracketingException {

    TestProblem1 pb = new TestProblem1();
    double step = (pb.getFinalTime() - pb.getInitialTime()) * 0.001;

    FirstOrderIntegrator integ = new ClassicalRungeKuttaIntegrator(step);
    TestProblemHandler handler = new TestProblemHandler(pb, integ);
    integ.addStepHandler(handler);
    integ.integrate(pb, pb.getInitialTime(), pb.getInitialState(),
                    pb.getFinalTime(), new double[pb.getDimension()]);

    Assert.assertTrue(handler.getLastError() < 2.0e-13);
    Assert.assertTrue(handler.getMaximalValueError() < 4.0e-12);
    Assert.assertEquals(0, handler.getMaximalTimeError(), 1.0e-12);
    Assert.assertEquals("classical Runge-Kutta", integ.getName());
  }

// org.apache.commons.math3.ode.nonstiff.ClassicalRungeKuttaIntegratorTest::testBigStep
  public void testBigStep()
      throws DimensionMismatchException, NumberIsTooSmallException,
             MaxCountExceededException, NoBracketingException {

    TestProblem1 pb = new TestProblem1();
    double step = (pb.getFinalTime() - pb.getInitialTime()) * 0.2;

    FirstOrderIntegrator integ = new ClassicalRungeKuttaIntegrator(step);
    TestProblemHandler handler = new TestProblemHandler(pb, integ);
    integ.addStepHandler(handler);
    integ.integrate(pb, pb.getInitialTime(), pb.getInitialState(),
                    pb.getFinalTime(), new double[pb.getDimension()]);

    Assert.assertTrue(handler.getLastError() > 0.0004);
    Assert.assertTrue(handler.getMaximalValueError() > 0.005);
    Assert.assertEquals(0, handler.getMaximalTimeError(), 1.0e-12);

  }

// org.apache.commons.math3.ode.nonstiff.ClassicalRungeKuttaIntegratorTest::testBackward
  public void testBackward()
      throws DimensionMismatchException, NumberIsTooSmallException,
             MaxCountExceededException, NoBracketingException {

    TestProblem5 pb = new TestProblem5();
    double step = FastMath.abs(pb.getFinalTime() - pb.getInitialTime()) * 0.001;

    FirstOrderIntegrator integ = new ClassicalRungeKuttaIntegrator(step);
    TestProblemHandler handler = new TestProblemHandler(pb, integ);
    integ.addStepHandler(handler);
    integ.integrate(pb, pb.getInitialTime(), pb.getInitialState(),
                    pb.getFinalTime(), new double[pb.getDimension()]);

    Assert.assertTrue(handler.getLastError() < 5.0e-10);
    Assert.assertTrue(handler.getMaximalValueError() < 7.0e-10);
    Assert.assertEquals(0, handler.getMaximalTimeError(), 1.0e-12);
    Assert.assertEquals("classical Runge-Kutta", integ.getName());
  }

// org.apache.commons.math3.ode.nonstiff.ClassicalRungeKuttaIntegratorTest::testKepler
  public void testKepler()
      throws DimensionMismatchException, NumberIsTooSmallException,
             MaxCountExceededException, NoBracketingException {

    final TestProblem3 pb  = new TestProblem3(0.9);
    double step = (pb.getFinalTime() - pb.getInitialTime()) * 0.0003;

    FirstOrderIntegrator integ = new ClassicalRungeKuttaIntegrator(step);
    integ.addStepHandler(new KeplerHandler(pb));
    integ.integrate(pb,
                    pb.getInitialTime(), pb.getInitialState(),
                    pb.getFinalTime(), new double[pb.getDimension()]);
  }

// org.apache.commons.math3.ode.nonstiff.ClassicalRungeKuttaIntegratorTest::testStepSize
  public void testStepSize()
      throws DimensionMismatchException, NumberIsTooSmallException,
             MaxCountExceededException, NoBracketingException {
      final double step = 1.23456;
      FirstOrderIntegrator integ = new ClassicalRungeKuttaIntegrator(step);
      integ.addStepHandler(new StepHandler() {
          public void handleStep(StepInterpolator interpolator, boolean isLast) {
              if (! isLast) {
                  Assert.assertEquals(step,
                               interpolator.getCurrentTime() - interpolator.getPreviousTime(),
                               1.0e-12);
              }
          }
          public void init(double t0, double[] y0, double t) {
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

// org.apache.commons.math3.ode.nonstiff.ClassicalRungeKuttaStepInterpolatorTest::derivativesConsistency
  public void derivativesConsistency()
      throws DimensionMismatchException, NumberIsTooSmallException,
             MaxCountExceededException, NoBracketingException {
    TestProblem3 pb = new TestProblem3();
    double step = (pb.getFinalTime() - pb.getInitialTime()) * 0.001;
    ClassicalRungeKuttaIntegrator integ = new ClassicalRungeKuttaIntegrator(step);
    StepInterpolatorTestUtils.checkDerivativesConsistency(integ, pb, 1.0e-10);
  }

// org.apache.commons.math3.ode.nonstiff.ClassicalRungeKuttaStepInterpolatorTest::serialization
  public void serialization()
    throws IOException, ClassNotFoundException,
           DimensionMismatchException, NumberIsTooSmallException,
           MaxCountExceededException, NoBracketingException  {

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

    Assert.assertTrue(bos.size () > 880000);
    Assert.assertTrue(bos.size () < 900000);

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

    Assert.assertTrue(maxError > 0.005);

  }

// org.apache.commons.math3.ode.nonstiff.DormandPrince54IntegratorTest::testDimensionCheck
  public void testDimensionCheck()
      throws DimensionMismatchException, NumberIsTooSmallException,
             MaxCountExceededException, NoBracketingException {
      TestProblem1 pb = new TestProblem1();
      DormandPrince54Integrator integrator = new DormandPrince54Integrator(0.0, 1.0,
                                                                           1.0e-10, 1.0e-10);
      integrator.integrate(pb,
                           0.0, new double[pb.getDimension()+10],
                           1.0, new double[pb.getDimension()+10]);
  }

// org.apache.commons.math3.ode.nonstiff.DormandPrince54IntegratorTest::testMinStep
  public void testMinStep()
      throws DimensionMismatchException, NumberIsTooSmallException,
             MaxCountExceededException, NoBracketingException {

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
      Assert.fail("an exception should have been thrown");

  }

// org.apache.commons.math3.ode.nonstiff.DormandPrince54IntegratorTest::testSmallLastStep
  public void testSmallLastStep()
      throws DimensionMismatchException, NumberIsTooSmallException,
             MaxCountExceededException, NoBracketingException {

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
    Assert.assertTrue(handler.wasLastSeen());
    Assert.assertEquals("Dormand-Prince 5(4)", integ.getName());

  }

// org.apache.commons.math3.ode.nonstiff.DormandPrince54IntegratorTest::testBackward
  public void testBackward()
      throws DimensionMismatchException, NumberIsTooSmallException,
             MaxCountExceededException, NoBracketingException {

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

      Assert.assertTrue(handler.getLastError() < 2.0e-7);
      Assert.assertTrue(handler.getMaximalValueError() < 2.0e-7);
      Assert.assertEquals(0, handler.getMaximalTimeError(), 1.0e-12);
      Assert.assertEquals("Dormand-Prince 5(4)", integ.getName());
  }

// org.apache.commons.math3.ode.nonstiff.DormandPrince54IntegratorTest::testIncreasingTolerance
  public void testIncreasingTolerance()
      throws DimensionMismatchException, NumberIsTooSmallException,
             MaxCountExceededException, NoBracketingException {

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
      Assert.assertEquals(0.8, integ.getSafety(), 1.0e-12);
      Assert.assertEquals(5.0, integ.getMaxGrowth(), 1.0e-12);
      Assert.assertEquals(0.3, integ.getMinReduction(), 1.0e-12);

      
      
      
      Assert.assertTrue(handler.getMaximalValueError() < (0.7 * scalAbsoluteTolerance));
      Assert.assertEquals(0, handler.getMaximalTimeError(), 1.0e-12);

      int calls = pb.getCalls();
      Assert.assertEquals(integ.getEvaluations(), calls);
      Assert.assertTrue(calls <= previousCalls);
      previousCalls = calls;

    }

  }

// org.apache.commons.math3.ode.nonstiff.DormandPrince54IntegratorTest::testEvents
  public void testEvents()
      throws DimensionMismatchException, NumberIsTooSmallException,
             MaxCountExceededException, NoBracketingException {

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
    double convergence = 1.0e-8 * maxStep;
    for (int l = 0; l < functions.length; ++l) {
      integ.addEventHandler(functions[l],
                                 Double.POSITIVE_INFINITY, convergence, 1000);
    }
    Assert.assertEquals(functions.length, integ.getEventHandlers().size());
    integ.integrate(pb,
                    pb.getInitialTime(), pb.getInitialState(),
                    pb.getFinalTime(), new double[pb.getDimension()]);

    Assert.assertTrue(handler.getMaximalValueError() < 5.0e-6);
    Assert.assertEquals(0, handler.getMaximalTimeError(), convergence);
    Assert.assertEquals(12.0, handler.getLastTime(), convergence);
    integ.clearEventHandlers();
    Assert.assertEquals(0, integ.getEventHandlers().size());

  }

// org.apache.commons.math3.ode.nonstiff.DormandPrince54IntegratorTest::testKepler
  public void testKepler()
      throws DimensionMismatchException, NumberIsTooSmallException,
             MaxCountExceededException, NoBracketingException {

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

    Assert.assertEquals(integ.getEvaluations(), pb.getCalls());
    Assert.assertTrue(pb.getCalls() < 2800);

  }

// org.apache.commons.math3.ode.nonstiff.DormandPrince54IntegratorTest::testVariableSteps
  public void testVariableSteps()
      throws DimensionMismatchException, NumberIsTooSmallException,
             MaxCountExceededException, NoBracketingException {

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
    Assert.assertEquals(pb.getFinalTime(), stopTime, 1.0e-10);
  }

// org.apache.commons.math3.ode.nonstiff.DormandPrince54StepInterpolatorTest::derivativesConsistency
  public void derivativesConsistency()
      throws DimensionMismatchException, NumberIsTooSmallException,
             MaxCountExceededException, NoBracketingException {
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

// org.apache.commons.math3.ode.nonstiff.DormandPrince54StepInterpolatorTest::serialization
  public void serialization()
    throws IOException, ClassNotFoundException,
           DimensionMismatchException, NumberIsTooSmallException,
           MaxCountExceededException, NoBracketingException  {

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

    Assert.assertTrue(bos.size () > 135000);
    Assert.assertTrue(bos.size () < 145000);

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

    Assert.assertTrue(maxError < 7.0e-10);

  }

// org.apache.commons.math3.ode.nonstiff.DormandPrince54StepInterpolatorTest::checkClone
  public void checkClone()
      throws DimensionMismatchException, NumberIsTooSmallException,
             MaxCountExceededException, NoBracketingException {
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
              throws MaxCountExceededException {
              StepInterpolator cloned = interpolator.copy();
              double tA = cloned.getPreviousTime();
              double tB = cloned.getCurrentTime();
              double halfStep = FastMath.abs(tB - tA) / 2;
              Assert.assertEquals(interpolator.getPreviousTime(), tA, 1.0e-12);
              Assert.assertEquals(interpolator.getCurrentTime(), tB, 1.0e-12);
              for (int i = 0; i < 10; ++i) {
                  double t = (i * tB + (9 - i) * tA) / 9;
                  interpolator.setInterpolatedTime(t);
                  Assert.assertTrue(FastMath.abs(cloned.getInterpolatedTime() - t) > (halfStep / 10));
                  cloned.setInterpolatedTime(t);
                  Assert.assertEquals(t, cloned.getInterpolatedTime(), 1.0e-12);
                  double[] referenceState = interpolator.getInterpolatedState();
                  double[] cloneState     = cloned.getInterpolatedState();
                  for (int j = 0; j < referenceState.length; ++j) {
                      Assert.assertEquals(referenceState[j], cloneState[j], 1.0e-12);
                  }
              }
          }
          public void init(double t0, double[] y0, double t) {
          }
      });
      integ.integrate(pb,
              pb.getInitialTime(), pb.getInitialState(),
              pb.getFinalTime(), new double[pb.getDimension()]);

  }

// org.apache.commons.math3.ode.nonstiff.DormandPrince853IntegratorTest::testMissedEndEvent
  public void testMissedEndEvent()
      throws DimensionMismatchException, NumberIsTooSmallException,
             MaxCountExceededException, NoBracketingException {
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

          public void init(double t0, double[] y0, double t) {
          }

          public void resetState(double t, double[] y) {
          }

          public double g(double t, double[] y) {
              return t - tEvent;
          }

          public Action eventOccurred(double t, double[] y, boolean increasing) {
              Assert.assertEquals(tEvent, t, 5.0e-6);
              return Action.CONTINUE;
          }
      }, Double.POSITIVE_INFINITY, 1.0e-20, 100);
      finalT = integrator.integrate(ode, t0, y0, tEvent + 120, y);
      Assert.assertEquals(tEvent + 120, finalT, 5.0e-6);
      for (int i = 0; i < y.length; ++i) {
          Assert.assertEquals(y0[i] * FastMath.exp(k[i] * (finalT - t0)), y[i], 1.0e-9);
      }

  }

// org.apache.commons.math3.ode.nonstiff.DormandPrince853IntegratorTest::testDimensionCheck
  public void testDimensionCheck()
      throws DimensionMismatchException, NumberIsTooSmallException,
             MaxCountExceededException, NoBracketingException {
      TestProblem1 pb = new TestProblem1();
      DormandPrince853Integrator integrator = new DormandPrince853Integrator(0.0, 1.0,
                                                                             1.0e-10, 1.0e-10);
      integrator.integrate(pb,
                           0.0, new double[pb.getDimension()+10],
                           1.0, new double[pb.getDimension()+10]);
      Assert.fail("an exception should have been thrown");
  }

// org.apache.commons.math3.ode.nonstiff.DormandPrince853IntegratorTest::testNullIntervalCheck
  public void testNullIntervalCheck()
      throws DimensionMismatchException, NumberIsTooSmallException,
             MaxCountExceededException, NoBracketingException {
      TestProblem1 pb = new TestProblem1();
      DormandPrince853Integrator integrator = new DormandPrince853Integrator(0.0, 1.0,
                                                                             1.0e-10, 1.0e-10);
      integrator.integrate(pb,
                           0.0, new double[pb.getDimension()],
                           0.0, new double[pb.getDimension()]);
      Assert.fail("an exception should have been thrown");
  }

// org.apache.commons.math3.ode.nonstiff.DormandPrince853IntegratorTest::testMinStep
  public void testMinStep()
      throws DimensionMismatchException, NumberIsTooSmallException,
             MaxCountExceededException, NoBracketingException {

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
      Assert.fail("an exception should have been thrown");

  }

// org.apache.commons.math3.ode.nonstiff.DormandPrince853IntegratorTest::testIncreasingTolerance
  public void testIncreasingTolerance()
      throws DimensionMismatchException, NumberIsTooSmallException,
             MaxCountExceededException, NoBracketingException {

    int previousCalls = Integer.MAX_VALUE;
    AdaptiveStepsizeIntegrator integ =
        new DormandPrince853Integrator(0, Double.POSITIVE_INFINITY,
                                       Double.NaN, Double.NaN);
    for (int i = -12; i < -2; ++i) {
      TestProblem1 pb = new TestProblem1();
      double minStep = 0;
      double maxStep = pb.getFinalTime() - pb.getInitialTime();
      double scalAbsoluteTolerance = FastMath.pow(10.0, i);
      double scalRelativeTolerance = 0.01 * scalAbsoluteTolerance;
      integ.setStepSizeControl(minStep, maxStep, scalAbsoluteTolerance, scalRelativeTolerance);

      TestProblemHandler handler = new TestProblemHandler(pb, integ);
      integ.addStepHandler(handler);
      integ.integrate(pb,
                      pb.getInitialTime(), pb.getInitialState(),
                      pb.getFinalTime(), new double[pb.getDimension()]);

      
      
      
      Assert.assertTrue(handler.getMaximalValueError() < (1.3 * scalAbsoluteTolerance));
      Assert.assertEquals(0, handler.getMaximalTimeError(), 1.0e-12);

      int calls = pb.getCalls();
      Assert.assertEquals(integ.getEvaluations(), calls);
      Assert.assertTrue(calls <= previousCalls);
      previousCalls = calls;

    }

  }

// org.apache.commons.math3.ode.nonstiff.DormandPrince853IntegratorTest::testTooLargeFirstStep
  public void testTooLargeFirstStep()
      throws DimensionMismatchException, NumberIsTooSmallException,
             MaxCountExceededException, NoBracketingException {

      AdaptiveStepsizeIntegrator integ =
              new DormandPrince853Integrator(0, Double.POSITIVE_INFINITY, Double.NaN, Double.NaN);
      final double start = 0.0;
      final double end   = 0.001;
      FirstOrderDifferentialEquations equations = new FirstOrderDifferentialEquations() {

          public int getDimension() {
              return 1;
          }

          public void computeDerivatives(double t, double[] y, double[] yDot) {
              Assert.assertTrue(t >= FastMath.nextAfter(start, Double.NEGATIVE_INFINITY));
              Assert.assertTrue(t <= FastMath.nextAfter(end,   Double.POSITIVE_INFINITY));
              yDot[0] = -100.0 * y[0];
          }

      };

      integ.setStepSizeControl(0, 1.0, 1.0e-6, 1.0e-8);
      integ.integrate(equations, start, new double[] { 1.0 }, end, new double[1]);

  }

// org.apache.commons.math3.ode.nonstiff.DormandPrince853IntegratorTest::testBackward
  public void testBackward()
      throws DimensionMismatchException, NumberIsTooSmallException,
             MaxCountExceededException, NoBracketingException {

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

      Assert.assertTrue(handler.getLastError() < 1.1e-7);
      Assert.assertTrue(handler.getMaximalValueError() < 1.1e-7);
      Assert.assertEquals(0, handler.getMaximalTimeError(), 1.0e-12);
      Assert.assertEquals("Dormand-Prince 8 (5, 3)", integ.getName());
  }

// org.apache.commons.math3.ode.nonstiff.DormandPrince853IntegratorTest::testEvents
  public void testEvents()
      throws DimensionMismatchException, NumberIsTooSmallException,
             MaxCountExceededException, NoBracketingException {

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
    double convergence = 1.0e-8 * maxStep;
    for (int l = 0; l < functions.length; ++l) {
      integ.addEventHandler(functions[l], Double.POSITIVE_INFINITY, convergence, 1000);
    }
    Assert.assertEquals(functions.length, integ.getEventHandlers().size());
    integ.integrate(pb,
                    pb.getInitialTime(), pb.getInitialState(),
                    pb.getFinalTime(), new double[pb.getDimension()]);

    Assert.assertEquals(0, handler.getMaximalValueError(), 2.1e-7);
    Assert.assertEquals(0, handler.getMaximalTimeError(), convergence);
    Assert.assertEquals(12.0, handler.getLastTime(), convergence);
    integ.clearEventHandlers();
    Assert.assertEquals(0, integ.getEventHandlers().size());

  }

// org.apache.commons.math3.ode.nonstiff.DormandPrince853IntegratorTest::testKepler
  public void testKepler()
      throws DimensionMismatchException, NumberIsTooSmallException,
             MaxCountExceededException, NoBracketingException {

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

    Assert.assertEquals(integ.getEvaluations(), pb.getCalls());
    Assert.assertTrue(pb.getCalls() < 3300);

  }

// org.apache.commons.math3.ode.nonstiff.DormandPrince853IntegratorTest::testVariableSteps
  public void testVariableSteps()
      throws DimensionMismatchException, NumberIsTooSmallException,
             MaxCountExceededException, NoBracketingException {

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
    Assert.assertEquals(pb.getFinalTime(), stopTime, 1.0e-10);
    Assert.assertEquals("Dormand-Prince 8 (5, 3)", integ.getName());
  }

// org.apache.commons.math3.ode.nonstiff.DormandPrince853IntegratorTest::testUnstableDerivative
  public void testUnstableDerivative()
      throws DimensionMismatchException, NumberIsTooSmallException,
             MaxCountExceededException, NoBracketingException {
    final StepProblem stepProblem = new StepProblem(0.0, 1.0, 2.0);
    FirstOrderIntegrator integ =
      new DormandPrince853Integrator(0.1, 10, 1.0e-12, 0.0);
    integ.addEventHandler(stepProblem, 1.0, 1.0e-12, 1000);
    double[] y = { Double.NaN };
    integ.integrate(stepProblem, 0.0, new double[] { 0.0 }, 10.0, y);
    Assert.assertEquals(8.0, y[0], 1.0e-12);
  }

// org.apache.commons.math3.ode.nonstiff.DormandPrince853StepInterpolatorTest::derivativesConsistency
  public void derivativesConsistency()
      throws DimensionMismatchException, NumberIsTooSmallException,
             MaxCountExceededException, NoBracketingException {
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

// org.apache.commons.math3.ode.nonstiff.DormandPrince853StepInterpolatorTest::serialization
  public void serialization()
    throws IOException, ClassNotFoundException,
           DimensionMismatchException, NumberIsTooSmallException,
           MaxCountExceededException, NoBracketingException {

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

    Assert.assertTrue(bos.size () > 90000);
    Assert.assertTrue(bos.size () < 100000);

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

    Assert.assertTrue(maxError < 2.4e-10);

  }

// org.apache.commons.math3.ode.nonstiff.DormandPrince853StepInterpolatorTest::checklone
  public void checklone()
      throws DimensionMismatchException, NumberIsTooSmallException,
             MaxCountExceededException, NoBracketingException {
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
            throws MaxCountExceededException {
            StepInterpolator cloned = interpolator.copy();
            double tA = cloned.getPreviousTime();
            double tB = cloned.getCurrentTime();
            double halfStep = FastMath.abs(tB - tA) / 2;
            Assert.assertEquals(interpolator.getPreviousTime(), tA, 1.0e-12);
            Assert.assertEquals(interpolator.getCurrentTime(), tB, 1.0e-12);
            for (int i = 0; i < 10; ++i) {
                double t = (i * tB + (9 - i) * tA) / 9;
                interpolator.setInterpolatedTime(t);
                Assert.assertTrue(FastMath.abs(cloned.getInterpolatedTime() - t) > (halfStep / 10));
                cloned.setInterpolatedTime(t);
                Assert.assertEquals(t, cloned.getInterpolatedTime(), 1.0e-12);
                double[] referenceState = interpolator.getInterpolatedState();
                double[] cloneState     = cloned.getInterpolatedState();
                for (int j = 0; j < referenceState.length; ++j) {
                    Assert.assertEquals(referenceState[j], cloneState[j], 1.0e-12);
                }
            }
        }
        public void init(double t0, double[] y0, double t) {
        }
    });
    integ.integrate(pb,
            pb.getInitialTime(), pb.getInitialState(),
            pb.getFinalTime(), new double[pb.getDimension()]);

  }

// org.apache.commons.math3.ode.nonstiff.EulerIntegratorTest::testDimensionCheck
  public void testDimensionCheck()
      throws DimensionMismatchException, NumberIsTooSmallException,
             MaxCountExceededException, NoBracketingException {
      TestProblem1 pb = new TestProblem1();
      new EulerIntegrator(0.01).integrate(pb,
                                          0.0, new double[pb.getDimension()+10],
                                          1.0, new double[pb.getDimension()+10]);
        Assert.fail("an exception should have been thrown");
  }

// org.apache.commons.math3.ode.nonstiff.EulerIntegratorTest::testDecreasingSteps
  public void testDecreasingSteps()
      throws DimensionMismatchException, NumberIsTooSmallException,
             MaxCountExceededException, NoBracketingException {

    TestProblemAbstract[] problems = TestProblemFactory.getProblems();
    for (int k = 0; k < problems.length; ++k) {

      double previousValueError = Double.NaN;
      double previousTimeError = Double.NaN;
      for (int i = 4; i < 8; ++i) {

        TestProblemAbstract pb  = problems[k].copy();
        double step = (pb.getFinalTime() - pb.getInitialTime()) * FastMath.pow(2.0, -i);

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
            Assert.assertEquals(pb.getFinalTime(), stopTime, 1.0e-10);
        }

        double valueError = handler.getMaximalValueError();
        if (i > 4) {
          Assert.assertTrue(valueError < FastMath.abs(previousValueError));
        }
        previousValueError = valueError;

        double timeError = handler.getMaximalTimeError();
        if (i > 4) {
          Assert.assertTrue(timeError <= FastMath.abs(previousTimeError));
        }
        previousTimeError = timeError;

      }

    }

  }

// org.apache.commons.math3.ode.nonstiff.EulerIntegratorTest::testSmallStep
  public void testSmallStep()
      throws DimensionMismatchException, NumberIsTooSmallException,
             MaxCountExceededException, NoBracketingException {

    TestProblem1 pb  = new TestProblem1();
    double step = (pb.getFinalTime() - pb.getInitialTime()) * 0.001;

    FirstOrderIntegrator integ = new EulerIntegrator(step);
    TestProblemHandler handler = new TestProblemHandler(pb, integ);
    integ.addStepHandler(handler);
    integ.integrate(pb,
                    pb.getInitialTime(), pb.getInitialState(),
                    pb.getFinalTime(), new double[pb.getDimension()]);

   Assert.assertTrue(handler.getLastError() < 2.0e-4);
   Assert.assertTrue(handler.getMaximalValueError() < 1.0e-3);
   Assert.assertEquals(0, handler.getMaximalTimeError(), 1.0e-12);
   Assert.assertEquals("Euler", integ.getName());

  }

// org.apache.commons.math3.ode.nonstiff.EulerIntegratorTest::testBigStep
  public void testBigStep()
      throws DimensionMismatchException, NumberIsTooSmallException,
             MaxCountExceededException, NoBracketingException {

    TestProblem1 pb  = new TestProblem1();
    double step = (pb.getFinalTime() - pb.getInitialTime()) * 0.2;

    FirstOrderIntegrator integ = new EulerIntegrator(step);
    TestProblemHandler handler = new TestProblemHandler(pb, integ);
    integ.addStepHandler(handler);
    integ.integrate(pb,
                    pb.getInitialTime(), pb.getInitialState(),
                    pb.getFinalTime(), new double[pb.getDimension()]);

    Assert.assertTrue(handler.getLastError() > 0.01);
    Assert.assertTrue(handler.getMaximalValueError() > 0.2);
    Assert.assertEquals(0, handler.getMaximalTimeError(), 1.0e-12);

  }

// org.apache.commons.math3.ode.nonstiff.EulerIntegratorTest::testBackward
  public void testBackward()
      throws DimensionMismatchException, NumberIsTooSmallException,
             MaxCountExceededException, NoBracketingException {

      TestProblem5 pb = new TestProblem5();
      double step = FastMath.abs(pb.getFinalTime() - pb.getInitialTime()) * 0.001;

      FirstOrderIntegrator integ = new EulerIntegrator(step);
      TestProblemHandler handler = new TestProblemHandler(pb, integ);
      integ.addStepHandler(handler);
      integ.integrate(pb, pb.getInitialTime(), pb.getInitialState(),
                      pb.getFinalTime(), new double[pb.getDimension()]);

      Assert.assertTrue(handler.getLastError() < 0.45);
      Assert.assertTrue(handler.getMaximalValueError() < 0.45);
      Assert.assertEquals(0, handler.getMaximalTimeError(), 1.0e-12);
      Assert.assertEquals("Euler", integ.getName());
  }

// org.apache.commons.math3.ode.nonstiff.EulerIntegratorTest::testStepSize
  public void testStepSize()
      throws DimensionMismatchException, NumberIsTooSmallException,
             MaxCountExceededException, NoBracketingException {
      final double step = 1.23456;
      FirstOrderIntegrator integ = new EulerIntegrator(step);
      integ.addStepHandler(new StepHandler() {
        public void handleStep(StepInterpolator interpolator, boolean isLast) {
            if (! isLast) {
                Assert.assertEquals(step,
                             interpolator.getCurrentTime() - interpolator.getPreviousTime(),
                             1.0e-12);
            }
        }
        public void init(double t0, double[] y0, double t) {
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

// org.apache.commons.math3.ode.nonstiff.EulerStepInterpolatorTest::noReset
  public void noReset() throws MaxCountExceededException {

    double[]   y    =   { 0.0, 1.0, -2.0 };
    double[][] yDot = { { 1.0, 2.0, -2.0 } };
    EulerStepInterpolator interpolator = new EulerStepInterpolator();
    interpolator.reinitialize(new DummyIntegrator(interpolator), y, yDot, true,
                              new EquationsMapper(0, y.length),
                              new EquationsMapper[0]);
    interpolator.storeTime(0);
    interpolator.shift();
    interpolator.storeTime(1);

    double[] result = interpolator.getInterpolatedState();
    for (int i = 0; i < result.length; ++i) {
      Assert.assertTrue(FastMath.abs(result[i] - y[i]) < 1.0e-10);
    }

  }

// org.apache.commons.math3.ode.nonstiff.EulerStepInterpolatorTest::interpolationAtBounds
  public void interpolationAtBounds() throws MaxCountExceededException {

    double   t0 = 0;
    double[] y0 = {0.0, 1.0, -2.0};

    double[] y = y0.clone();
    double[][] yDot = { new double[y0.length] };
    EulerStepInterpolator interpolator = new EulerStepInterpolator();
    interpolator.reinitialize(new DummyIntegrator(interpolator), y, yDot, true,
                              new EquationsMapper(0, y.length),
                              new EquationsMapper[0]);
    interpolator.storeTime(t0);

    double dt = 1.0;
    interpolator.shift();
    y[0] =  1.0;
    y[1] =  3.0;
    y[2] = -4.0;
    yDot[0][0] = (y[0] - y0[0]) / dt;
    yDot[0][1] = (y[1] - y0[1]) / dt;
    yDot[0][2] = (y[2] - y0[2]) / dt;
    interpolator.storeTime(t0 + dt);

    interpolator.setInterpolatedTime(interpolator.getPreviousTime());
    double[] result = interpolator.getInterpolatedState();
    for (int i = 0; i < result.length; ++i) {
        Assert.assertTrue(FastMath.abs(result[i] - y0[i]) < 1.0e-10);
    }

    interpolator.setInterpolatedTime(interpolator.getCurrentTime());
    result = interpolator.getInterpolatedState();
    for (int i = 0; i < result.length; ++i) {
      Assert.assertTrue(FastMath.abs(result[i] - y[i]) < 1.0e-10);
    }

  }

// org.apache.commons.math3.ode.nonstiff.EulerStepInterpolatorTest::interpolationInside
  public void interpolationInside() throws MaxCountExceededException {

    double[]   y    =   { 0.0, 1.0, -2.0 };
    double[][] yDot = { { 1.0, 2.0, -2.0 } };
    EulerStepInterpolator interpolator = new EulerStepInterpolator();
    interpolator.reinitialize(new DummyIntegrator(interpolator), y, yDot, true,
                              new EquationsMapper(0, y.length),
                              new EquationsMapper[0]);
    interpolator.storeTime(0);
    interpolator.shift();
    y[0] =  1.0;
    y[1] =  3.0;
    y[2] = -4.0;
    interpolator.storeTime(1);

    interpolator.setInterpolatedTime(0.1);
    double[] result = interpolator.getInterpolatedState();
    Assert.assertTrue(FastMath.abs(result[0] - 0.1) < 1.0e-10);
    Assert.assertTrue(FastMath.abs(result[1] - 1.2) < 1.0e-10);
    Assert.assertTrue(FastMath.abs(result[2] + 2.2) < 1.0e-10);

    interpolator.setInterpolatedTime(0.5);
    result = interpolator.getInterpolatedState();
    Assert.assertTrue(FastMath.abs(result[0] - 0.5) < 1.0e-10);
    Assert.assertTrue(FastMath.abs(result[1] - 2.0) < 1.0e-10);
    Assert.assertTrue(FastMath.abs(result[2] + 3.0) < 1.0e-10);

  }

// org.apache.commons.math3.ode.nonstiff.EulerStepInterpolatorTest::derivativesConsistency
  public void derivativesConsistency()
      throws DimensionMismatchException, NumberIsTooSmallException,
             MaxCountExceededException, NoBracketingException {
    TestProblem3 pb = new TestProblem3();
    double step = (pb.getFinalTime() - pb.getInitialTime()) * 0.001;
    EulerIntegrator integ = new EulerIntegrator(step);
    StepInterpolatorTestUtils.checkDerivativesConsistency(integ, pb, 1.0e-10);
  }

// org.apache.commons.math3.ode.nonstiff.EulerStepInterpolatorTest::serialization
  public void serialization()
    throws IOException, ClassNotFoundException,
           DimensionMismatchException, NumberIsTooSmallException,
           MaxCountExceededException, NoBracketingException {

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
    Assert.assertTrue(maxError < 0.001);

  }

// org.apache.commons.math3.ode.nonstiff.GillIntegratorTest::testDimensionCheck
  public void testDimensionCheck()
      throws DimensionMismatchException, NumberIsTooSmallException,
             MaxCountExceededException, NoBracketingException {
      TestProblem1 pb = new TestProblem1();
      new GillIntegrator(0.01).integrate(pb,
                                         0.0, new double[pb.getDimension()+10],
                                         1.0, new double[pb.getDimension()+10]);
        Assert.fail("an exception should have been thrown");
  }

// org.apache.commons.math3.ode.nonstiff.GillIntegratorTest::testDecreasingSteps
  public void testDecreasingSteps()
      throws DimensionMismatchException, NumberIsTooSmallException,
             MaxCountExceededException, NoBracketingException {

    TestProblemAbstract[] problems = TestProblemFactory.getProblems();
    for (int k = 0; k < problems.length; ++k) {

      double previousValueError = Double.NaN;
      double previousTimeError = Double.NaN;
      for (int i = 5; i < 10; ++i) {

        TestProblemAbstract pb = problems[k].copy();
        double step = (pb.getFinalTime() - pb.getInitialTime()) * FastMath.pow(2.0, -i);

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
            Assert.assertEquals(pb.getFinalTime(), stopTime, 1.0e-10);
        }

        double valueError = handler.getMaximalValueError();
        if (i > 5) {
          Assert.assertTrue(valueError < 1.01 * FastMath.abs(previousValueError));
        }
        previousValueError = valueError;

        double timeError = handler.getMaximalTimeError();
        if (i > 5) {
          Assert.assertTrue(timeError <= FastMath.abs(previousTimeError));
        }
        previousTimeError = timeError;

      }

    }

  }

// org.apache.commons.math3.ode.nonstiff.GillIntegratorTest::testSmallStep
  public void testSmallStep()
      throws DimensionMismatchException, NumberIsTooSmallException,
             MaxCountExceededException, NoBracketingException {

    TestProblem1 pb = new TestProblem1();
    double step = (pb.getFinalTime() - pb.getInitialTime()) * 0.001;

    FirstOrderIntegrator integ = new GillIntegrator(step);
    TestProblemHandler handler = new TestProblemHandler(pb, integ);
    integ.addStepHandler(handler);
    integ.integrate(pb, pb.getInitialTime(), pb.getInitialState(),
                    pb.getFinalTime(), new double[pb.getDimension()]);

    Assert.assertTrue(handler.getLastError() < 2.0e-13);
    Assert.assertTrue(handler.getMaximalValueError() < 4.0e-12);
    Assert.assertEquals(0, handler.getMaximalTimeError(), 1.0e-12);
    Assert.assertEquals("Gill", integ.getName());

  }

// org.apache.commons.math3.ode.nonstiff.GillIntegratorTest::testBigStep
  public void testBigStep()
      throws DimensionMismatchException, NumberIsTooSmallException,
             MaxCountExceededException, NoBracketingException {

    TestProblem1 pb = new TestProblem1();
    double step = (pb.getFinalTime() - pb.getInitialTime()) * 0.2;

    FirstOrderIntegrator integ = new GillIntegrator(step);
    TestProblemHandler handler = new TestProblemHandler(pb, integ);
    integ.addStepHandler(handler);
    integ.integrate(pb, pb.getInitialTime(), pb.getInitialState(),
                    pb.getFinalTime(), new double[pb.getDimension()]);

    Assert.assertTrue(handler.getLastError() > 0.0004);
    Assert.assertTrue(handler.getMaximalValueError() > 0.005);
    Assert.assertEquals(0, handler.getMaximalTimeError(), 1.0e-12);

  }

// org.apache.commons.math3.ode.nonstiff.GillIntegratorTest::testBackward
  public void testBackward()
      throws DimensionMismatchException, NumberIsTooSmallException,
             MaxCountExceededException, NoBracketingException {

      TestProblem5 pb = new TestProblem5();
      double step = FastMath.abs(pb.getFinalTime() - pb.getInitialTime()) * 0.001;

      FirstOrderIntegrator integ = new GillIntegrator(step);
      TestProblemHandler handler = new TestProblemHandler(pb, integ);
      integ.addStepHandler(handler);
      integ.integrate(pb, pb.getInitialTime(), pb.getInitialState(),
                      pb.getFinalTime(), new double[pb.getDimension()]);

      Assert.assertTrue(handler.getLastError() < 5.0e-10);
      Assert.assertTrue(handler.getMaximalValueError() < 7.0e-10);
      Assert.assertEquals(0, handler.getMaximalTimeError(), 1.0e-12);
      Assert.assertEquals("Gill", integ.getName());
  }

// org.apache.commons.math3.ode.nonstiff.GillIntegratorTest::testKepler
  public void testKepler()
      throws DimensionMismatchException, NumberIsTooSmallException,
             MaxCountExceededException, NoBracketingException {

    final TestProblem3 pb  = new TestProblem3(0.9);
    double step = (pb.getFinalTime() - pb.getInitialTime()) * 0.0003;

    FirstOrderIntegrator integ = new GillIntegrator(step);
    integ.addStepHandler(new KeplerStepHandler(pb));
    integ.integrate(pb,
                    pb.getInitialTime(), pb.getInitialState(),
                    pb.getFinalTime(), new double[pb.getDimension()]);
  }

// org.apache.commons.math3.ode.nonstiff.GillIntegratorTest::testUnstableDerivative
  public void testUnstableDerivative()
      throws DimensionMismatchException, NumberIsTooSmallException,
             MaxCountExceededException, NoBracketingException {
    final StepProblem stepProblem = new StepProblem(0.0, 1.0, 2.0);
    FirstOrderIntegrator integ = new GillIntegrator(0.3);
    integ.addEventHandler(stepProblem, 1.0, 1.0e-12, 1000);
    double[] y = { Double.NaN };
    integ.integrate(stepProblem, 0.0, new double[] { 0.0 }, 10.0, y);
    Assert.assertEquals(8.0, y[0], 1.0e-12);
  }

// org.apache.commons.math3.ode.nonstiff.GillIntegratorTest::testStepSize
  public void testStepSize()
      throws DimensionMismatchException, NumberIsTooSmallException,
             MaxCountExceededException, NoBracketingException {
      final double step = 1.23456;
      FirstOrderIntegrator integ = new GillIntegrator(step);
      integ.addStepHandler(new StepHandler() {
          public void handleStep(StepInterpolator interpolator, boolean isLast) {
              if (! isLast) {
                  Assert.assertEquals(step,
                               interpolator.getCurrentTime() - interpolator.getPreviousTime(),
                               1.0e-12);
              }
          }
          public void init(double t0, double[] y0, double t) {
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

// org.apache.commons.math3.ode.nonstiff.GillStepInterpolatorTest::testDerivativesConsistency
  public void testDerivativesConsistency()
      throws DimensionMismatchException, NumberIsTooSmallException,
             MaxCountExceededException, NoBracketingException {
    TestProblem3 pb = new TestProblem3();
    double step = (pb.getFinalTime() - pb.getInitialTime()) * 0.001;
    GillIntegrator integ = new GillIntegrator(step);
    StepInterpolatorTestUtils.checkDerivativesConsistency(integ, pb, 1.0e-10);
  }

// org.apache.commons.math3.ode.nonstiff.GillStepInterpolatorTest::serialization
  public void serialization()
    throws IOException, ClassNotFoundException,
           DimensionMismatchException, NumberIsTooSmallException,
           MaxCountExceededException, NoBracketingException {

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

    Assert.assertTrue(bos.size () > 880000);
    Assert.assertTrue(bos.size () < 900000);

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

    Assert.assertTrue(maxError < 0.003);

  }

// org.apache.commons.math3.ode.nonstiff.GraggBulirschStoerIntegratorTest::testDimensionCheck
  public void testDimensionCheck()
      throws DimensionMismatchException, NumberIsTooSmallException,
             MaxCountExceededException, NoBracketingException {
      TestProblem1 pb = new TestProblem1();
      AdaptiveStepsizeIntegrator integrator =
        new GraggBulirschStoerIntegrator(0.0, 1.0, 1.0e-10, 1.0e-10);
      integrator.integrate(pb,
                           0.0, new double[pb.getDimension()+10],
                           1.0, new double[pb.getDimension()+10]);
  }

// org.apache.commons.math3.ode.nonstiff.GraggBulirschStoerIntegratorTest::testNullIntervalCheck
  public void testNullIntervalCheck()
      throws DimensionMismatchException, NumberIsTooSmallException,
             MaxCountExceededException, NoBracketingException {
      TestProblem1 pb = new TestProblem1();
      GraggBulirschStoerIntegrator integrator =
        new GraggBulirschStoerIntegrator(0.0, 1.0, 1.0e-10, 1.0e-10);
      integrator.integrate(pb,
                           0.0, new double[pb.getDimension()],
                           0.0, new double[pb.getDimension()]);
  }

// org.apache.commons.math3.ode.nonstiff.GraggBulirschStoerIntegratorTest::testMinStep
  public void testMinStep()
      throws DimensionMismatchException, NumberIsTooSmallException,
             MaxCountExceededException, NoBracketingException {

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

  }

// org.apache.commons.math3.ode.nonstiff.GraggBulirschStoerIntegratorTest::testBackward
  public void testBackward()
      throws DimensionMismatchException, NumberIsTooSmallException,
             MaxCountExceededException, NoBracketingException {

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

      Assert.assertTrue(handler.getLastError() < 7.5e-9);
      Assert.assertTrue(handler.getMaximalValueError() < 8.1e-9);
      Assert.assertEquals(0, handler.getMaximalTimeError(), 1.0e-12);
      Assert.assertEquals("Gragg-Bulirsch-Stoer", integ.getName());
  }

// org.apache.commons.math3.ode.nonstiff.GraggBulirschStoerIntegratorTest::testIncreasingTolerance
  public void testIncreasingTolerance()
      throws DimensionMismatchException, NumberIsTooSmallException,
             MaxCountExceededException, NoBracketingException {

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
      Assert.assertTrue(ratio < 2.4);
      Assert.assertTrue(ratio > 0.02);
      Assert.assertEquals(0, handler.getMaximalTimeError(), 1.0e-12);

      int calls = pb.getCalls();
      Assert.assertEquals(integ.getEvaluations(), calls);
      Assert.assertTrue(calls <= previousCalls);
      previousCalls = calls;

    }

  }

// org.apache.commons.math3.ode.nonstiff.GraggBulirschStoerIntegratorTest::testIntegratorControls
  public void testIntegratorControls()
      throws DimensionMismatchException, NumberIsTooSmallException,
             MaxCountExceededException, NoBracketingException {

    TestProblem3 pb = new TestProblem3(0.999);
    GraggBulirschStoerIntegrator integ =
        new GraggBulirschStoerIntegrator(0, pb.getFinalTime() - pb.getInitialTime(),
                1.0e-8, 1.0e-10);

    double errorWithDefaultSettings = getMaxError(integ, pb);

    
    integ.setStabilityCheck(true, 2, 1, 0.99);
    Assert.assertTrue(errorWithDefaultSettings < getMaxError(integ, pb));
    integ.setStabilityCheck(true, -1, -1, -1);

    integ.setControlFactors(0.5, 0.99, 0.1, 2.5);
    Assert.assertTrue(errorWithDefaultSettings < getMaxError(integ, pb));
    integ.setControlFactors(-1, -1, -1, -1);

    integ.setOrderControl(10, 0.7, 0.95);
    Assert.assertTrue(errorWithDefaultSettings < getMaxError(integ, pb));
    integ.setOrderControl(-1, -1, -1);

    integ.setInterpolationControl(true, 3);
    Assert.assertTrue(errorWithDefaultSettings < getMaxError(integ, pb));
    integ.setInterpolationControl(true, -1);

  }

// org.apache.commons.math3.ode.nonstiff.GraggBulirschStoerIntegratorTest::testEvents
  public void testEvents()
      throws DimensionMismatchException, NumberIsTooSmallException,
             MaxCountExceededException, NoBracketingException {

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
    double convergence = 1.0e-8 * maxStep;
    for (int l = 0; l < functions.length; ++l) {
      integ.addEventHandler(functions[l], Double.POSITIVE_INFINITY, convergence, 1000);
    }
    Assert.assertEquals(functions.length, integ.getEventHandlers().size());
    integ.integrate(pb,
                    pb.getInitialTime(), pb.getInitialState(),
                    pb.getFinalTime(), new double[pb.getDimension()]);

    Assert.assertTrue(handler.getMaximalValueError() < 4.0e-7);
    Assert.assertEquals(0, handler.getMaximalTimeError(), convergence);
    Assert.assertEquals(12.0, handler.getLastTime(), convergence);
    integ.clearEventHandlers();
    Assert.assertEquals(0, integ.getEventHandlers().size());

  }

// org.apache.commons.math3.ode.nonstiff.GraggBulirschStoerIntegratorTest::testKepler
  public void testKepler()
      throws DimensionMismatchException, NumberIsTooSmallException,
             MaxCountExceededException, NoBracketingException {

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

    Assert.assertEquals(integ.getEvaluations(), pb.getCalls());
    Assert.assertTrue(pb.getCalls() < 2150);

  }

// org.apache.commons.math3.ode.nonstiff.GraggBulirschStoerIntegratorTest::testVariableSteps
  public void testVariableSteps()
      throws DimensionMismatchException, NumberIsTooSmallException,
             MaxCountExceededException, NoBracketingException {

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
    Assert.assertEquals(pb.getFinalTime(), stopTime, 1.0e-10);
    Assert.assertEquals("Gragg-Bulirsch-Stoer", integ.getName());
  }

// org.apache.commons.math3.ode.nonstiff.GraggBulirschStoerIntegratorTest::testTooLargeFirstStep
  public void testTooLargeFirstStep()
      throws DimensionMismatchException, NumberIsTooSmallException,
             MaxCountExceededException, NoBracketingException {

      AdaptiveStepsizeIntegrator integ =
              new GraggBulirschStoerIntegrator(0, Double.POSITIVE_INFINITY, Double.NaN, Double.NaN);
      final double start = 0.0;
      final double end   = 0.001;
      FirstOrderDifferentialEquations equations = new FirstOrderDifferentialEquations() {

          public int getDimension() {
              return 1;
          }

          public void computeDerivatives(double t, double[] y, double[] yDot) {
              Assert.assertTrue(t >= FastMath.nextAfter(start, Double.NEGATIVE_INFINITY));
              Assert.assertTrue(t <= FastMath.nextAfter(end,   Double.POSITIVE_INFINITY));
              yDot[0] = -100.0 * y[0];
          }

      };

      integ.setStepSizeControl(0, 1.0, 1.0e-6, 1.0e-8);
      integ.integrate(equations, start, new double[] { 1.0 }, end, new double[1]);

  }

// org.apache.commons.math3.ode.nonstiff.GraggBulirschStoerIntegratorTest::testUnstableDerivative
  public void testUnstableDerivative()
      throws DimensionMismatchException, NumberIsTooSmallException,
             MaxCountExceededException, NoBracketingException {
    final StepProblem stepProblem = new StepProblem(0.0, 1.0, 2.0);
    FirstOrderIntegrator integ =
      new GraggBulirschStoerIntegrator(0.1, 10, 1.0e-12, 0.0);
    integ.addEventHandler(stepProblem, 1.0, 1.0e-12, 1000);
    double[] y = { Double.NaN };
    integ.integrate(stepProblem, 0.0, new double[] { 0.0 }, 10.0, y);
    Assert.assertEquals(8.0, y[0], 1.0e-12);
  }

// org.apache.commons.math3.ode.nonstiff.GraggBulirschStoerIntegratorTest::testIssue596
  public void testIssue596()
      throws DimensionMismatchException, NumberIsTooSmallException,
             MaxCountExceededException, NoBracketingException {
    FirstOrderIntegrator integ = new GraggBulirschStoerIntegrator(1e-10, 100.0, 1e-7, 1e-7);
      integ.addStepHandler(new StepHandler() {

          public void init(double t0, double[] y0, double t) {
          }

          public void handleStep(StepInterpolator interpolator, boolean isLast)
              throws MaxCountExceededException {
              double t = interpolator.getCurrentTime();
              interpolator.setInterpolatedTime(t);
              double[] y = interpolator.getInterpolatedState();
              double[] yDot = interpolator.getInterpolatedDerivatives();
              Assert.assertEquals(3.0 * t - 5.0, y[0], 1.0e-14);
              Assert.assertEquals(3.0, yDot[0], 1.0e-14);
          }
      });
      double[] y = {4.0};
      double t0 = 3.0;
      double tend = 10.0;
      integ.integrate(new FirstOrderDifferentialEquations() {
          public int getDimension() {
              return 1;
          }

          public void computeDerivatives(double t, double[] y, double[] yDot) {
              yDot[0] = 3.0;
          }
      }, t0, y, tend, y);

  }

// org.apache.commons.math3.ode.nonstiff.GraggBulirschStoerStepInterpolatorTest::derivativesConsistency
  public void derivativesConsistency()
      throws DimensionMismatchException, NumberIsTooSmallException,
             MaxCountExceededException, NoBracketingException {
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

// org.apache.commons.math3.ode.nonstiff.GraggBulirschStoerStepInterpolatorTest::serialization
  public void serialization()
    throws IOException, ClassNotFoundException,
           DimensionMismatchException, NumberIsTooSmallException,
           MaxCountExceededException, NoBracketingException {

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

    Assert.assertTrue(bos.size () > 35000);
    Assert.assertTrue(bos.size () < 36000);

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

    Assert.assertTrue(maxError < 5.0e-10);

  }

// org.apache.commons.math3.ode.nonstiff.GraggBulirschStoerStepInterpolatorTest::checklone
  public void checklone()
      throws DimensionMismatchException, NumberIsTooSmallException,
             MaxCountExceededException, NoBracketingException {
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
            throws MaxCountExceededException {
            StepInterpolator cloned = interpolator.copy();
            double tA = cloned.getPreviousTime();
            double tB = cloned.getCurrentTime();
            double halfStep = FastMath.abs(tB - tA) / 2;
            Assert.assertEquals(interpolator.getPreviousTime(), tA, 1.0e-12);
            Assert.assertEquals(interpolator.getCurrentTime(), tB, 1.0e-12);
            for (int i = 0; i < 10; ++i) {
                double t = (i * tB + (9 - i) * tA) / 9;
                interpolator.setInterpolatedTime(t);
                Assert.assertTrue(FastMath.abs(cloned.getInterpolatedTime() - t) > (halfStep / 10));
                cloned.setInterpolatedTime(t);
                Assert.assertEquals(t, cloned.getInterpolatedTime(), 1.0e-12);
                double[] referenceState = interpolator.getInterpolatedState();
                double[] cloneState     = cloned.getInterpolatedState();
                for (int j = 0; j < referenceState.length; ++j) {
                    Assert.assertEquals(referenceState[j], cloneState[j], 1.0e-12);
                }
            }
        }
        public void init(double t0, double[] y0, double t) {
        }
    });
    integ.integrate(pb,
            pb.getInitialTime(), pb.getInitialState(),
            pb.getFinalTime(), new double[pb.getDimension()]);

  }

// org.apache.commons.math3.ode.nonstiff.HighamHall54IntegratorTest::testWrongDerivative
  public void testWrongDerivative()
      throws DimensionMismatchException, NumberIsTooSmallException,
             MaxCountExceededException, NoBracketingException {
      HighamHall54Integrator integrator =
          new HighamHall54Integrator(0.0, 1.0, 1.0e-10, 1.0e-10);
      FirstOrderDifferentialEquations equations =
          new FirstOrderDifferentialEquations() {
            public void computeDerivatives(double t, double[] y, double[] dot) {
            if (t < -0.5) {
                throw new LocalException();
            } else {
                throw new RuntimeException("oops");
           }
          }
          public int getDimension() {
              return 1;
          }
      };

      try  {
        integrator.integrate(equations, -1.0, new double[1], 0.0, new double[1]);
        Assert.fail("an exception should have been thrown");
      } catch(LocalException de) {
        
      }

      try  {
        integrator.integrate(equations, 0.0, new double[1], 1.0, new double[1]);
        Assert.fail("an exception should have been thrown");
      } catch(RuntimeException de) {
        
      }

  }

// org.apache.commons.math3.ode.nonstiff.HighamHall54IntegratorTest::testMinStep
  public void testMinStep()
      throws DimensionMismatchException, NumberIsTooSmallException,
             MaxCountExceededException, NoBracketingException {

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
      Assert.fail("an exception should have been thrown");

  }

// org.apache.commons.math3.ode.nonstiff.HighamHall54IntegratorTest::testIncreasingTolerance
  public void testIncreasingTolerance()
      throws DimensionMismatchException, NumberIsTooSmallException,
             MaxCountExceededException, NoBracketingException {

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

      
      
      
      Assert.assertTrue(handler.getMaximalValueError() < (1.3 * scalAbsoluteTolerance));
      Assert.assertEquals(0, handler.getMaximalTimeError(), 1.0e-12);

      int calls = pb.getCalls();
      Assert.assertEquals(integ.getEvaluations(), calls);
      Assert.assertTrue(calls <= previousCalls);
      previousCalls = calls;

    }

  }

// org.apache.commons.math3.ode.nonstiff.HighamHall54IntegratorTest::testBackward
  public void testBackward()
      throws DimensionMismatchException, NumberIsTooSmallException,
             MaxCountExceededException, NoBracketingException {

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

      Assert.assertTrue(handler.getLastError() < 5.0e-7);
      Assert.assertTrue(handler.getMaximalValueError() < 5.0e-7);
      Assert.assertEquals(0, handler.getMaximalTimeError(), 1.0e-12);
      Assert.assertEquals("Higham-Hall 5(4)", integ.getName());
  }

// org.apache.commons.math3.ode.nonstiff.HighamHall54IntegratorTest::testEvents
  public void testEvents()
      throws DimensionMismatchException, NumberIsTooSmallException,
             MaxCountExceededException, NoBracketingException {

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
    double convergence = 1.0e-8 * maxStep;
    for (int l = 0; l < functions.length; ++l) {
      integ.addEventHandler(functions[l],
                                 Double.POSITIVE_INFINITY, convergence, 1000);
    }
    Assert.assertEquals(functions.length, integ.getEventHandlers().size());
    integ.integrate(pb,
                    pb.getInitialTime(), pb.getInitialState(),
                    pb.getFinalTime(), new double[pb.getDimension()]);

    Assert.assertTrue(handler.getMaximalValueError() < 1.0e-7);
    Assert.assertEquals(0, handler.getMaximalTimeError(), convergence);
    Assert.assertEquals(12.0, handler.getLastTime(), convergence);
    integ.clearEventHandlers();
    Assert.assertEquals(0, integ.getEventHandlers().size());

  }

// org.apache.commons.math3.ode.nonstiff.HighamHall54IntegratorTest::testEventsErrors
  public void testEventsErrors()
      throws DimensionMismatchException, NumberIsTooSmallException,
             MaxCountExceededException, NoBracketingException {

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
        public void init(double t0, double[] y0, double t) {
        }
        public Action eventOccurred(double t, double[] y, boolean increasing) {
          return Action.CONTINUE;
        }
        public double g(double t, double[] y) {
          double middle = (pb.getInitialTime() + pb.getFinalTime()) / 2;
          double offset = t - middle;
          if (offset > 0) {
            throw new LocalException();
          }
          return offset;
        }
        public void resetState(double t, double[] y) {
        }
      }, Double.POSITIVE_INFINITY, 1.0e-8 * maxStep, 1000);

      integ.integrate(pb,
                      pb.getInitialTime(), pb.getInitialState(),
                      pb.getFinalTime(), new double[pb.getDimension()]);

  }

// org.apache.commons.math3.ode.nonstiff.HighamHall54IntegratorTest::testEventsNoConvergence
  public void testEventsNoConvergence()
      throws DimensionMismatchException, NumberIsTooSmallException,
             MaxCountExceededException, NoBracketingException {

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
      public void init(double t0, double[] y0, double t) {
      }
      public Action eventOccurred(double t, double[] y, boolean increasing) {
        return Action.CONTINUE;
      }
      public double g(double t, double[] y) {
        double middle = (pb.getInitialTime() + pb.getFinalTime()) / 2;
        double offset = t - middle;
        return (offset > 0) ? (offset + 0.5) : (offset - 0.5);
      }
      public void resetState(double t, double[] y) {
      }
    }, Double.POSITIVE_INFINITY, 1.0e-8 * maxStep, 3);

    try {
      integ.integrate(pb,
                      pb.getInitialTime(), pb.getInitialState(),
                      pb.getFinalTime(), new double[pb.getDimension()]);
      Assert.fail("an exception should have been thrown");
    } catch (TooManyEvaluationsException tmee) {
        
    }

}

// org.apache.commons.math3.ode.nonstiff.HighamHall54IntegratorTest::testSanityChecks
  public void testSanityChecks()
      throws DimensionMismatchException, NumberIsTooSmallException,
             MaxCountExceededException, NoBracketingException {
      final TestProblem3 pb  = new TestProblem3(0.9);
      double minStep = 0;
      double maxStep = pb.getFinalTime() - pb.getInitialTime();

      try {
        FirstOrderIntegrator integ =
            new HighamHall54Integrator(minStep, maxStep, new double[4], new double[4]);
        integ.integrate(pb, pb.getInitialTime(), new double[6],
                        pb.getFinalTime(), new double[pb.getDimension()]);
        Assert.fail("an exception should have been thrown");
      } catch (DimensionMismatchException ie) {
        
      }

      try {
        FirstOrderIntegrator integ =
            new HighamHall54Integrator(minStep, maxStep, new double[4], new double[4]);
        integ.integrate(pb, pb.getInitialTime(), pb.getInitialState(),
                        pb.getFinalTime(), new double[6]);
        Assert.fail("an exception should have been thrown");
      } catch (DimensionMismatchException ie) {
        
      }

      try {
        FirstOrderIntegrator integ =
            new HighamHall54Integrator(minStep, maxStep, new double[2], new double[4]);
        integ.integrate(pb, pb.getInitialTime(), pb.getInitialState(),
                        pb.getFinalTime(), new double[pb.getDimension()]);
        Assert.fail("an exception should have been thrown");
      } catch (DimensionMismatchException ie) {
        
      }

      try {
        FirstOrderIntegrator integ =
            new HighamHall54Integrator(minStep, maxStep, new double[4], new double[2]);
        integ.integrate(pb, pb.getInitialTime(), pb.getInitialState(),
                        pb.getFinalTime(), new double[pb.getDimension()]);
        Assert.fail("an exception should have been thrown");
      } catch (DimensionMismatchException ie) {
        
      }

      try {
        FirstOrderIntegrator integ =
            new HighamHall54Integrator(minStep, maxStep, new double[4], new double[4]);
        integ.integrate(pb, pb.getInitialTime(), pb.getInitialState(),
                        pb.getInitialTime(), new double[pb.getDimension()]);
        Assert.fail("an exception should have been thrown");
      } catch (NumberIsTooSmallException ie) {
        
      }

  }

// org.apache.commons.math3.ode.nonstiff.HighamHall54IntegratorTest::testKepler
  public void testKepler()
      throws DimensionMismatchException, NumberIsTooSmallException,
             MaxCountExceededException, NoBracketingException {

    final TestProblem3 pb  = new TestProblem3(0.9);
    double minStep = 0;
    double maxStep = pb.getFinalTime() - pb.getInitialTime();
    double[] vecAbsoluteTolerance = { 1.0e-8, 1.0e-8, 1.0e-10, 1.0e-10 };
    double[] vecRelativeTolerance = { 1.0e-10, 1.0e-10, 1.0e-8, 1.0e-8 };

    FirstOrderIntegrator integ = new HighamHall54Integrator(minStep, maxStep,
                                                            vecAbsoluteTolerance,
                                                            vecRelativeTolerance);
    TestProblemHandler handler = new TestProblemHandler(pb, integ); 
    integ.addStepHandler(handler);
    integ.integrate(pb,
                    pb.getInitialTime(), pb.getInitialState(),
                    pb.getFinalTime(), new double[pb.getDimension()]);
    Assert.assertEquals(0.0, handler.getMaximalValueError(), 1.5e-4);
    Assert.assertEquals("Higham-Hall 5(4)", integ.getName());
  }

// org.apache.commons.math3.ode.nonstiff.HighamHall54StepInterpolatorTest::derivativesConsistency
  public void derivativesConsistency()
      throws DimensionMismatchException, NumberIsTooSmallException,
             MaxCountExceededException, NoBracketingException {
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

// org.apache.commons.math3.ode.nonstiff.HighamHall54StepInterpolatorTest::serialization
  public void serialization()
    throws IOException, ClassNotFoundException,
           DimensionMismatchException, NumberIsTooSmallException,
           MaxCountExceededException, NoBracketingException {

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

    Assert.assertTrue(bos.size () > 185000);
    Assert.assertTrue(bos.size () < 195000);

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

    Assert.assertTrue(maxError < 1.6e-10);

  }

// org.apache.commons.math3.ode.nonstiff.HighamHall54StepInterpolatorTest::checkClone
  public void checkClone()
      throws DimensionMismatchException, NumberIsTooSmallException,
             MaxCountExceededException, NoBracketingException {
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
            throws MaxCountExceededException {
            StepInterpolator cloned = interpolator.copy();
            double tA = cloned.getPreviousTime();
            double tB = cloned.getCurrentTime();
            double halfStep = FastMath.abs(tB - tA) / 2;
            Assert.assertEquals(interpolator.getPreviousTime(), tA, 1.0e-12);
            Assert.assertEquals(interpolator.getCurrentTime(), tB, 1.0e-12);
            for (int i = 0; i < 10; ++i) {
                double t = (i * tB + (9 - i) * tA) / 9;
                interpolator.setInterpolatedTime(t);
                Assert.assertTrue(FastMath.abs(cloned.getInterpolatedTime() - t) > (halfStep / 10));
                cloned.setInterpolatedTime(t);
                Assert.assertEquals(t, cloned.getInterpolatedTime(), 1.0e-12);
                double[] referenceState = interpolator.getInterpolatedState();
                double[] cloneState     = cloned.getInterpolatedState();
                for (int j = 0; j < referenceState.length; ++j) {
                    Assert.assertEquals(referenceState[j], cloneState[j], 1.0e-12);
                }
            }
        }
        public void init(double t0, double[] y0, double t) {
        }
    });
    integ.integrate(pb,
            pb.getInitialTime(), pb.getInitialState(),
            pb.getFinalTime(), new double[pb.getDimension()]);

  }

// org.apache.commons.math3.ode.nonstiff.MidpointIntegratorTest::testDimensionCheck
  public void testDimensionCheck()
      throws DimensionMismatchException, NumberIsTooSmallException,
             MaxCountExceededException, NoBracketingException {
      TestProblem1 pb = new TestProblem1();
      new MidpointIntegrator(0.01).integrate(pb,
                                             0.0, new double[pb.getDimension()+10],
                                             1.0, new double[pb.getDimension()+10]);
        Assert.fail("an exception should have been thrown");
  }

// org.apache.commons.math3.ode.nonstiff.MidpointIntegratorTest::testDecreasingSteps
  public void testDecreasingSteps()
      throws DimensionMismatchException, NumberIsTooSmallException,
             MaxCountExceededException, NoBracketingException {

    TestProblemAbstract[] problems = TestProblemFactory.getProblems();
    for (int k = 0; k < problems.length; ++k) {

      double previousValueError = Double.NaN;
      double previousTimeError = Double.NaN;
      for (int i = 4; i < 10; ++i) {

        TestProblemAbstract pb = problems[k].copy();
        double step = (pb.getFinalTime() - pb.getInitialTime()) * FastMath.pow(2.0, -i);
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
            Assert.assertEquals(pb.getFinalTime(), stopTime, 1.0e-10);
        }

        double valueError = handler.getMaximalValueError();
        if (i > 4) {
          Assert.assertTrue(valueError < FastMath.abs(previousValueError));
        }
        previousValueError = valueError;

        double timeError = handler.getMaximalTimeError();
        if (i > 4) {
          Assert.assertTrue(timeError <= FastMath.abs(previousTimeError));
        }
        previousTimeError = timeError;

      }

    }

  }

// org.apache.commons.math3.ode.nonstiff.MidpointIntegratorTest::testSmallStep
  public void testSmallStep()
      throws DimensionMismatchException, NumberIsTooSmallException,
             MaxCountExceededException, NoBracketingException {

    TestProblem1 pb  = new TestProblem1();
    double step = (pb.getFinalTime() - pb.getInitialTime()) * 0.001;

    FirstOrderIntegrator integ = new MidpointIntegrator(step);
    TestProblemHandler handler = new TestProblemHandler(pb, integ);
    integ.addStepHandler(handler);
    integ.integrate(pb,
                    pb.getInitialTime(), pb.getInitialState(),
                    pb.getFinalTime(), new double[pb.getDimension()]);

    Assert.assertTrue(handler.getLastError() < 2.0e-7);
    Assert.assertTrue(handler.getMaximalValueError() < 1.0e-6);
    Assert.assertEquals(0, handler.getMaximalTimeError(), 1.0e-12);
    Assert.assertEquals("midpoint", integ.getName());

  }

// org.apache.commons.math3.ode.nonstiff.MidpointIntegratorTest::testBigStep
  public void testBigStep()
      throws DimensionMismatchException, NumberIsTooSmallException,
             MaxCountExceededException, NoBracketingException {

    TestProblem1 pb  = new TestProblem1();
    double step = (pb.getFinalTime() - pb.getInitialTime()) * 0.2;

    FirstOrderIntegrator integ = new MidpointIntegrator(step);
    TestProblemHandler handler = new TestProblemHandler(pb, integ);
    integ.addStepHandler(handler);
    integ.integrate(pb,
                    pb.getInitialTime(), pb.getInitialState(),
                    pb.getFinalTime(), new double[pb.getDimension()]);

    Assert.assertTrue(handler.getLastError() > 0.01);
    Assert.assertTrue(handler.getMaximalValueError() > 0.05);
    Assert.assertEquals(0, handler.getMaximalTimeError(), 1.0e-12);

  }

// org.apache.commons.math3.ode.nonstiff.MidpointIntegratorTest::testBackward
  public void testBackward()
      throws DimensionMismatchException, NumberIsTooSmallException,
             MaxCountExceededException, NoBracketingException {

      TestProblem5 pb = new TestProblem5();
      double step = FastMath.abs(pb.getFinalTime() - pb.getInitialTime()) * 0.001;

      FirstOrderIntegrator integ = new MidpointIntegrator(step);
      TestProblemHandler handler = new TestProblemHandler(pb, integ);
      integ.addStepHandler(handler);
      integ.integrate(pb, pb.getInitialTime(), pb.getInitialState(),
                      pb.getFinalTime(), new double[pb.getDimension()]);

      Assert.assertTrue(handler.getLastError() < 6.0e-4);
      Assert.assertTrue(handler.getMaximalValueError() < 6.0e-4);
      Assert.assertEquals(0, handler.getMaximalTimeError(), 1.0e-12);
      Assert.assertEquals("midpoint", integ.getName());
  }

// org.apache.commons.math3.ode.nonstiff.MidpointIntegratorTest::testStepSize
  public void testStepSize()
      throws DimensionMismatchException, NumberIsTooSmallException,
             MaxCountExceededException, NoBracketingException {
      final double step = 1.23456;
      FirstOrderIntegrator integ = new MidpointIntegrator(step);
      integ.addStepHandler(new StepHandler() {
          public void handleStep(StepInterpolator interpolator, boolean isLast) {
              if (! isLast) {
                  Assert.assertEquals(step,
                               interpolator.getCurrentTime() - interpolator.getPreviousTime(),
                               1.0e-12);
              }
          }
          public void init(double t0, double[] y0, double t) {
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

// org.apache.commons.math3.ode.nonstiff.MidpointStepInterpolatorTest::testDerivativesConsistency
  public void testDerivativesConsistency()
      throws DimensionMismatchException, NumberIsTooSmallException,
             MaxCountExceededException, NoBracketingException {
    TestProblem3 pb = new TestProblem3();
    double step = (pb.getFinalTime() - pb.getInitialTime()) * 0.001;
    MidpointIntegrator integ = new MidpointIntegrator(step);
    StepInterpolatorTestUtils.checkDerivativesConsistency(integ, pb, 1.0e-10);
  }

// org.apache.commons.math3.ode.nonstiff.MidpointStepInterpolatorTest::serialization
  public void serialization()
    throws IOException, ClassNotFoundException,
           DimensionMismatchException, NumberIsTooSmallException,
           MaxCountExceededException, NoBracketingException {

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

    Assert.assertTrue(bos.size () > 135000);
    Assert.assertTrue(bos.size () < 145000);

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

    Assert.assertTrue(maxError < 1.0e-6);

  }

// org.apache.commons.math3.ode.nonstiff.ThreeEighthesIntegratorTest::testDimensionCheck
  public void testDimensionCheck()
      throws DimensionMismatchException, NumberIsTooSmallException,
             MaxCountExceededException, NoBracketingException {
      TestProblem1 pb = new TestProblem1();
      new ThreeEighthesIntegrator(0.01).integrate(pb,
                                                  0.0, new double[pb.getDimension()+10],
                                                  1.0, new double[pb.getDimension()+10]);
        Assert.fail("an exception should have been thrown");
  }

// org.apache.commons.math3.ode.nonstiff.ThreeEighthesIntegratorTest::testDecreasingSteps
  public void testDecreasingSteps()
      throws DimensionMismatchException, NumberIsTooSmallException,
             MaxCountExceededException, NoBracketingException {

    TestProblemAbstract[] problems = TestProblemFactory.getProblems();
    for (int k = 0; k < problems.length; ++k) {

      double previousValueError = Double.NaN;
      double previousTimeError = Double.NaN;
      for (int i = 4; i < 10; ++i) {

        TestProblemAbstract pb = problems[k].copy();
        double step = (pb.getFinalTime() - pb.getInitialTime()) * FastMath.pow(2.0, -i);

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
            Assert.assertEquals(pb.getFinalTime(), stopTime, 1.0e-10);
        }

        double error = handler.getMaximalValueError();
        if (i > 4) {
          Assert.assertTrue(error < 1.01 * FastMath.abs(previousValueError));
        }
        previousValueError = error;

        double timeError = handler.getMaximalTimeError();
        if (i > 4) {
          Assert.assertTrue(timeError <= FastMath.abs(previousTimeError));
        }
        previousTimeError = timeError;

      }

    }

  }

// org.apache.commons.math3.ode.nonstiff.ThreeEighthesIntegratorTest::testSmallStep
 public void testSmallStep()
     throws DimensionMismatchException, NumberIsTooSmallException,
            MaxCountExceededException, NoBracketingException {

    TestProblem1 pb = new TestProblem1();
    double step = (pb.getFinalTime() - pb.getInitialTime()) * 0.001;

    FirstOrderIntegrator integ = new ThreeEighthesIntegrator(step);
    TestProblemHandler handler = new TestProblemHandler(pb, integ);
    integ.addStepHandler(handler);
    integ.integrate(pb, pb.getInitialTime(), pb.getInitialState(),
                    pb.getFinalTime(), new double[pb.getDimension()]);

    Assert.assertTrue(handler.getLastError() < 2.0e-13);
    Assert.assertTrue(handler.getMaximalValueError() < 4.0e-12);
    Assert.assertEquals(0, handler.getMaximalTimeError(), 1.0e-12);
    Assert.assertEquals("3/8", integ.getName());

  }

// org.apache.commons.math3.ode.nonstiff.ThreeEighthesIntegratorTest::testBigStep
  public void testBigStep()
      throws DimensionMismatchException, NumberIsTooSmallException,
             MaxCountExceededException, NoBracketingException {

    TestProblem1 pb = new TestProblem1();
    double step = (pb.getFinalTime() - pb.getInitialTime()) * 0.2;

    FirstOrderIntegrator integ = new ThreeEighthesIntegrator(step);
    TestProblemHandler handler = new TestProblemHandler(pb, integ);
    integ.addStepHandler(handler);
    integ.integrate(pb, pb.getInitialTime(), pb.getInitialState(),
                    pb.getFinalTime(), new double[pb.getDimension()]);

    Assert.assertTrue(handler.getLastError() > 0.0004);
    Assert.assertTrue(handler.getMaximalValueError() > 0.005);
    Assert.assertEquals(0, handler.getMaximalTimeError(), 1.0e-12);

  }

// org.apache.commons.math3.ode.nonstiff.ThreeEighthesIntegratorTest::testBackward
  public void testBackward()
      throws DimensionMismatchException, NumberIsTooSmallException,
             MaxCountExceededException, NoBracketingException {

      TestProblem5 pb = new TestProblem5();
      double step = FastMath.abs(pb.getFinalTime() - pb.getInitialTime()) * 0.001;

      FirstOrderIntegrator integ = new ThreeEighthesIntegrator(step);
      TestProblemHandler handler = new TestProblemHandler(pb, integ);
      integ.addStepHandler(handler);
      integ.integrate(pb, pb.getInitialTime(), pb.getInitialState(),
                      pb.getFinalTime(), new double[pb.getDimension()]);

      Assert.assertTrue(handler.getLastError() < 5.0e-10);
      Assert.assertTrue(handler.getMaximalValueError() < 7.0e-10);
      Assert.assertEquals(0, handler.getMaximalTimeError(), 1.0e-12);
      Assert.assertEquals("3/8", integ.getName());
  }

// org.apache.commons.math3.ode.nonstiff.ThreeEighthesIntegratorTest::testKepler
  public void testKepler()
      throws DimensionMismatchException, NumberIsTooSmallException,
             MaxCountExceededException, NoBracketingException {

    final TestProblem3 pb  = new TestProblem3(0.9);
    double step = (pb.getFinalTime() - pb.getInitialTime()) * 0.0003;

    FirstOrderIntegrator integ = new ThreeEighthesIntegrator(step);
    integ.addStepHandler(new KeplerHandler(pb));
    integ.integrate(pb,
                    pb.getInitialTime(), pb.getInitialState(),
                    pb.getFinalTime(), new double[pb.getDimension()]);
  }

// org.apache.commons.math3.ode.nonstiff.ThreeEighthesIntegratorTest::testStepSize
  public void testStepSize()
      throws DimensionMismatchException, NumberIsTooSmallException,
             MaxCountExceededException, NoBracketingException {
      final double step = 1.23456;
      FirstOrderIntegrator integ = new ThreeEighthesIntegrator(step);
      integ.addStepHandler(new StepHandler() {
          public void handleStep(StepInterpolator interpolator, boolean isLast) {
              if (! isLast) {
                  Assert.assertEquals(step,
                               interpolator.getCurrentTime() - interpolator.getPreviousTime(),
                               1.0e-12);
              }
          }
          public void init(double t0, double[] y0, double t) {
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

// org.apache.commons.math3.ode.nonstiff.ThreeEighthesStepInterpolatorTest::derivativesConsistency
  public void derivativesConsistency()
      throws DimensionMismatchException, NumberIsTooSmallException,
             MaxCountExceededException, NoBracketingException {
    TestProblem3 pb = new TestProblem3();
    double step = (pb.getFinalTime() - pb.getInitialTime()) * 0.001;
    ThreeEighthesIntegrator integ = new ThreeEighthesIntegrator(step);
    StepInterpolatorTestUtils.checkDerivativesConsistency(integ, pb, 1.0e-10);
  }

// org.apache.commons.math3.ode.nonstiff.ThreeEighthesStepInterpolatorTest::serialization
  public void serialization()
    throws IOException, ClassNotFoundException,
           DimensionMismatchException, NumberIsTooSmallException,
           MaxCountExceededException, NoBracketingException {

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

    Assert.assertTrue(bos.size () > 880000);
    Assert.assertTrue(bos.size () < 900000);

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

    Assert.assertTrue(maxError > 0.005);

  }

// org.apache.commons.math3.ode.sampling.DummyStepInterpolatorTest::testNoReset
  public void testNoReset() throws MaxCountExceededException {

    double[]   y    =   { 0.0, 1.0, -2.0 };
    DummyStepInterpolator interpolator = new DummyStepInterpolator(y, new double[y.length], true);
    interpolator.storeTime(0);
    interpolator.shift();
    interpolator.storeTime(1);

    double[] result = interpolator.getInterpolatedState();
    for (int i = 0; i < result.length; ++i) {
      Assert.assertTrue(FastMath.abs(result[i] - y[i]) < 1.0e-10);
    }

  }

// org.apache.commons.math3.ode.sampling.DummyStepInterpolatorTest::testFixedState
  public void testFixedState() throws MaxCountExceededException {

    double[]   y    =   { 1.0, 3.0, -4.0 };
    DummyStepInterpolator interpolator = new DummyStepInterpolator(y, new double[y.length], true);
    interpolator.storeTime(0);
    interpolator.shift();
    interpolator.storeTime(1);

    interpolator.setInterpolatedTime(0.1);
    double[] result = interpolator.getInterpolatedState();
    for (int i = 0; i < result.length; ++i) {
        Assert.assertTrue(FastMath.abs(result[i] - y[i]) < 1.0e-10);
    }

    interpolator.setInterpolatedTime(0.5);
    result = interpolator.getInterpolatedState();
    for (int i = 0; i < result.length; ++i) {
        Assert.assertTrue(FastMath.abs(result[i] - y[i]) < 1.0e-10);
    }

  }

// org.apache.commons.math3.ode.sampling.DummyStepInterpolatorTest::testSerialization
  public void testSerialization()
  throws IOException, ClassNotFoundException, MaxCountExceededException {

    double[]   y    =   { 0.0, 1.0, -2.0 };
    DummyStepInterpolator interpolator = new DummyStepInterpolator(y, new double[y.length], true);
    interpolator.storeTime(0);
    interpolator.shift();
    interpolator.storeTime(1);

    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    ObjectOutputStream    oos = new ObjectOutputStream(bos);
    oos.writeObject(interpolator);

    Assert.assertTrue(bos.size () > 300);
    Assert.assertTrue(bos.size () < 500);

    ByteArrayInputStream  bis = new ByteArrayInputStream(bos.toByteArray());
    ObjectInputStream     ois = new ObjectInputStream(bis);
    DummyStepInterpolator dsi = (DummyStepInterpolator) ois.readObject();

    dsi.setInterpolatedTime(0.5);
    double[] result = dsi.getInterpolatedState();
    for (int i = 0; i < result.length; ++i) {
        Assert.assertTrue(FastMath.abs(result[i] - y[i]) < 1.0e-10);
    }

  }

// org.apache.commons.math3.ode.sampling.DummyStepInterpolatorTest::testImpossibleSerialization
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
        Assert.fail("an exception should have been thrown");
    } catch (LocalException le) {
        
    }
  }

// org.apache.commons.math3.ode.sampling.NordsieckStepInterpolatorTest::derivativesConsistency
    public void derivativesConsistency()
        throws NumberIsTooSmallException, DimensionMismatchException,
               MaxCountExceededException, NoBracketingException {
        TestProblem3 pb = new TestProblem3();
        AdamsBashforthIntegrator integ = new AdamsBashforthIntegrator(4, 0.0, 1.0, 1.0e-10, 1.0e-10);
        StepInterpolatorTestUtils.checkDerivativesConsistency(integ, pb, 5e-9);
    }

// org.apache.commons.math3.ode.sampling.NordsieckStepInterpolatorTest::serialization
    public void serialization()
    throws IOException, ClassNotFoundException,
           NumberIsTooSmallException, DimensionMismatchException,
           MaxCountExceededException, NoBracketingException {

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

        Assert.assertTrue(bos.size () >  25500);
        Assert.assertTrue(bos.size () <  26500);

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

        Assert.assertTrue(maxError < 1.0e-6);

    }

// org.apache.commons.math3.ode.sampling.StepNormalizerTest::testBoundaries
  public void testBoundaries()
      throws DimensionMismatchException, NumberIsTooSmallException,
             MaxCountExceededException, NoBracketingException {
    double range = pb.getFinalTime() - pb.getInitialTime();
    setLastSeen(false);
    integ.addStepHandler(new StepNormalizer(range / 10.0,
                                       new FixedStepHandler() {
                                         private boolean firstCall = true;
                                         public void init(double t0, double[] y0, double t) {
                                         }
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
    Assert.assertTrue(lastSeen);
  }

// org.apache.commons.math3.ode.sampling.StepNormalizerTest::testBeforeEnd
  public void testBeforeEnd()
      throws DimensionMismatchException, NumberIsTooSmallException,
             MaxCountExceededException, NoBracketingException {
    final double range = pb.getFinalTime() - pb.getInitialTime();
    setLastSeen(false);
    integ.addStepHandler(new StepNormalizer(range / 10.5,
                                       new FixedStepHandler() {
                                         public void init(double t0, double[] y0, double t) {
                                         }
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
    Assert.assertTrue(lastSeen);
  }

// org.apache.commons.math3.optimization.MultivariateDifferentiableMultiStartOptimizerTest::testCircleFitting
    public void testCircleFitting() {
        CircleScalar circle = new CircleScalar();
        circle.addPoint( 30.0,  68.0);
        circle.addPoint( 50.0,  -6.0);
        circle.addPoint(110.0, -20.0);
        circle.addPoint( 35.0,  15.0);
        circle.addPoint( 45.0,  97.0);
        
        
        
        MultivariateDifferentiableOptimizer underlying =
                new MultivariateDifferentiableOptimizer() {

            private final NonLinearConjugateGradientOptimizer cg =
                    new NonLinearConjugateGradientOptimizer(ConjugateGradientFormula.POLAK_RIBIERE,
                                                            new SimpleValueChecker(1.0e-10, 1.0e-10));
            public PointValuePair optimize(int maxEval,
                                           MultivariateDifferentiableFunction f,
                                           GoalType goalType,
                                           double[] startPoint) {
                return cg.optimize(maxEval, f, goalType, startPoint);
            }

            public int getMaxEvaluations() {
                return cg.getMaxEvaluations();
            }

            public int getEvaluations() {
                return cg.getEvaluations();
            }

            public ConvergenceChecker<PointValuePair> getConvergenceChecker() {
                return cg.getConvergenceChecker();
            }
        };
        JDKRandomGenerator g = new JDKRandomGenerator();
        g.setSeed(753289573253l);
        RandomVectorGenerator generator =
            new UncorrelatedRandomVectorGenerator(new double[] { 50.0, 50.0 }, new double[] { 10.0, 10.0 },
                                                  new GaussianRandomGenerator(g));
        MultivariateDifferentiableMultiStartOptimizer optimizer =
            new MultivariateDifferentiableMultiStartOptimizer(underlying, 10, generator);
        PointValuePair optimum =
            optimizer.optimize(200, circle, GoalType.MINIMIZE, new double[] { 98.680, 47.345 });
        Assert.assertEquals(200, optimizer.getMaxEvaluations());
        PointValuePair[] optima = optimizer.getOptima();
        for (PointValuePair o : optima) {
            Vector2D center = new Vector2D(o.getPointRef()[0], o.getPointRef()[1]);
            Assert.assertEquals(69.960161753, circle.getRadius(center), 1.0e-8);
            Assert.assertEquals(96.075902096, center.getX(), 1.0e-8);
            Assert.assertEquals(48.135167894, center.getY(), 1.0e-8);
        }
        Assert.assertTrue(optimizer.getEvaluations() > 70);
        Assert.assertTrue(optimizer.getEvaluations() < 90);
        Assert.assertEquals(3.1267527, optimum.getValue(), 1.0e-8);
    }

// org.apache.commons.math3.optimization.MultivariateDifferentiableVectorMultiStartOptimizerTest::testTrivial
    public void testTrivial() {
        LinearProblem problem =
            new LinearProblem(new double[][] { { 2 } }, new double[] { 3 });
        
        
        
        MultivariateDifferentiableVectorOptimizer underlyingOptimizer =
                new MultivariateDifferentiableVectorOptimizer() {
            private GaussNewtonOptimizer gn =
                    new GaussNewtonOptimizer(true,
                                             new SimpleVectorValueChecker(1.0e-6, 1.0e-6));

            public PointVectorValuePair optimize(int maxEval,
                                                 MultivariateDifferentiableVectorFunction f,
                                                 double[] target,
                                                 double[] weight,
                                                 double[] startPoint) {
                return gn.optimize(maxEval, f, target, weight, startPoint);
            }

            public int getMaxEvaluations() {
                return gn.getMaxEvaluations();
            }

            public int getEvaluations() {
                return gn.getEvaluations();
            }

            public ConvergenceChecker<PointVectorValuePair> getConvergenceChecker() {
                return gn.getConvergenceChecker();
            }
        };
        JDKRandomGenerator g = new JDKRandomGenerator();
        g.setSeed(16069223052l);
        RandomVectorGenerator generator =
            new UncorrelatedRandomVectorGenerator(1, new GaussianRandomGenerator(g));
        MultivariateDifferentiableVectorMultiStartOptimizer optimizer =
            new MultivariateDifferentiableVectorMultiStartOptimizer(underlyingOptimizer,
                                                                       10, generator);

        
        try {
            optimizer.getOptima();
            Assert.fail("an exception should have been thrown");
        } catch (MathIllegalStateException ise) {
            
        }
        PointVectorValuePair optimum =
            optimizer.optimize(100, problem, problem.target, new double[] { 1 }, new double[] { 0 });
        Assert.assertEquals(1.5, optimum.getPoint()[0], 1.0e-10);
        Assert.assertEquals(3.0, optimum.getValue()[0], 1.0e-10);
        PointVectorValuePair[] optima = optimizer.getOptima();
        Assert.assertEquals(10, optima.length);
        for (int i = 0; i < optima.length; ++i) {
            Assert.assertEquals(1.5, optima[i].getPoint()[0], 1.0e-10);
            Assert.assertEquals(3.0, optima[i].getValue()[0], 1.0e-10);
        }
        Assert.assertTrue(optimizer.getEvaluations() > 20);
        Assert.assertTrue(optimizer.getEvaluations() < 50);
        Assert.assertEquals(100, optimizer.getMaxEvaluations());
    }

// org.apache.commons.math3.optimization.MultivariateDifferentiableVectorMultiStartOptimizerTest::testNoOptimum
    public void testNoOptimum() {

        
        
        
        MultivariateDifferentiableVectorOptimizer underlyingOptimizer =
                new MultivariateDifferentiableVectorOptimizer() {
            private GaussNewtonOptimizer gn =
                    new GaussNewtonOptimizer(true,
                                             new SimpleVectorValueChecker(1.0e-6, 1.0e-6));

            public PointVectorValuePair optimize(int maxEval,
                                                 MultivariateDifferentiableVectorFunction f,
                                                 double[] target,
                                                 double[] weight,
                                                 double[] startPoint) {
                return gn.optimize(maxEval, f, target, weight, startPoint);
            }

            public int getMaxEvaluations() {
                return gn.getMaxEvaluations();
            }

            public int getEvaluations() {
                return gn.getEvaluations();
            }

            public ConvergenceChecker<PointVectorValuePair> getConvergenceChecker() {
                return gn.getConvergenceChecker();
            }
        };
        JDKRandomGenerator g = new JDKRandomGenerator();
        g.setSeed(12373523445l);
        RandomVectorGenerator generator =
            new UncorrelatedRandomVectorGenerator(1, new GaussianRandomGenerator(g));
        MultivariateDifferentiableVectorMultiStartOptimizer optimizer =
            new MultivariateDifferentiableVectorMultiStartOptimizer(underlyingOptimizer,
                                                                       10, generator);
        optimizer.optimize(100, new MultivariateDifferentiableVectorFunction() {
            public double[] value(double[] point) {
                throw new TestException();
            }
            public DerivativeStructure[] value(DerivativeStructure[] point) {
                return point;
            }
            }, new double[] { 2 }, new double[] { 1 }, new double[] { 0 });
    }

// org.apache.commons.math3.optimization.MultivariateMultiStartOptimizerTest::testRosenbrock
    public void testRosenbrock() {
        Rosenbrock rosenbrock = new Rosenbrock();
        SimplexOptimizer underlying
            = new SimplexOptimizer(new SimpleValueChecker(-1, 1.0e-3));
        NelderMeadSimplex simplex = new NelderMeadSimplex(new double[][] {
                { -1.2,  1.0 }, { 0.9, 1.2 } , {  3.5, -2.3 }
            });
        underlying.setSimplex(simplex);
        JDKRandomGenerator g = new JDKRandomGenerator();
        g.setSeed(16069223052l);
        RandomVectorGenerator generator =
            new UncorrelatedRandomVectorGenerator(2, new GaussianRandomGenerator(g));
        MultivariateMultiStartOptimizer optimizer =
            new MultivariateMultiStartOptimizer(underlying, 10, generator);
        PointValuePair optimum =
            optimizer.optimize(1100, rosenbrock, GoalType.MINIMIZE, new double[] { -1.2, 1.0 });

        Assert.assertEquals(rosenbrock.getCount(), optimizer.getEvaluations());
        Assert.assertTrue(optimizer.getEvaluations() > 900);
        Assert.assertTrue(optimizer.getEvaluations() < 1200);
        Assert.assertTrue(optimum.getValue() < 8.0e-4);
    }

// org.apache.commons.math3.optimization.SimplePointCheckerTest::testIterationCheckPrecondition
    public void testIterationCheckPrecondition() {
        new SimplePointChecker<PointValuePair>(1e-1, 1e-2, 0);
    }

// org.apache.commons.math3.optimization.SimplePointCheckerTest::testIterationCheck
    public void testIterationCheck() {
        final int max = 10;
        final SimplePointChecker<PointValuePair> checker
            = new SimplePointChecker<PointValuePair>(1e-1, 1e-2, max);
        Assert.assertTrue(checker.converged(max, null, null)); 
        Assert.assertTrue(checker.converged(max + 1, null, null));
    }

// org.apache.commons.math3.optimization.SimplePointCheckerTest::testIterationCheckDisabled
    public void testIterationCheckDisabled() {
        final SimplePointChecker<PointValuePair> checker
            = new SimplePointChecker<PointValuePair>(1e-8, 1e-8);

        final PointValuePair a = new PointValuePair(new double[] { 1d }, 1d);
        final PointValuePair b = new PointValuePair(new double[] { 10d }, 10d);

        Assert.assertFalse(checker.converged(-1, a, b));
        Assert.assertFalse(checker.converged(0, a, b));
        Assert.assertFalse(checker.converged(1000000, a, b));

        Assert.assertTrue(checker.converged(-1, a, a));
        Assert.assertTrue(checker.converged(-1, b, b));
    }

// org.apache.commons.math3.optimization.SimpleValueCheckerTest::testIterationCheckPrecondition
    public void testIterationCheckPrecondition() {
        new SimpleValueChecker(1e-1, 1e-2, 0);
    }

// org.apache.commons.math3.optimization.SimpleValueCheckerTest::testIterationCheck
    public void testIterationCheck() {
        final int max = 10;
        final SimpleValueChecker checker = new SimpleValueChecker(1e-1, 1e-2, max);
        Assert.assertTrue(checker.converged(max, null, null)); 
        Assert.assertTrue(checker.converged(max + 1, null, null));
    }

// org.apache.commons.math3.optimization.SimpleValueCheckerTest::testIterationCheckDisabled
    public void testIterationCheckDisabled() {
        final SimpleValueChecker checker = new SimpleValueChecker(1e-8, 1e-8);

        final PointValuePair a = new PointValuePair(new double[] { 1d }, 1d);
        final PointValuePair b = new PointValuePair(new double[] { 10d }, 10d);

        Assert.assertFalse(checker.converged(-1, a, b));
        Assert.assertFalse(checker.converged(0, a, b));
        Assert.assertFalse(checker.converged(1000000, a, b));

        Assert.assertTrue(checker.converged(-1, a, a));
        Assert.assertTrue(checker.converged(-1, b, b));
    }

// org.apache.commons.math3.optimization.SimpleVectorValueCheckerTest::testIterationCheckPrecondition
    public void testIterationCheckPrecondition() {
        new SimpleVectorValueChecker(1e-1, 1e-2, 0);
    }

// org.apache.commons.math3.optimization.SimpleVectorValueCheckerTest::testIterationCheck
    public void testIterationCheck() {
        final int max = 10;
        final SimpleVectorValueChecker checker = new SimpleVectorValueChecker(1e-1, 1e-2, max);
        Assert.assertTrue(checker.converged(max, null, null));
        Assert.assertTrue(checker.converged(max + 1, null, null));
    }

// org.apache.commons.math3.optimization.SimpleVectorValueCheckerTest::testIterationCheckDisabled
    public void testIterationCheckDisabled() {
        final SimpleVectorValueChecker checker = new SimpleVectorValueChecker(1e-8, 1e-8);

        final PointVectorValuePair a = new PointVectorValuePair(new double[] { 1d },
                                                                new double[] { 1d });
        final PointVectorValuePair b = new PointVectorValuePair(new double[] { 10d },
                                                                new double[] { 10d });

        Assert.assertFalse(checker.converged(-1, a, b));
        Assert.assertFalse(checker.converged(0, a, b));
        Assert.assertFalse(checker.converged(1000000, a, b));

        Assert.assertTrue(checker.converged(-1, a, a));
        Assert.assertTrue(checker.converged(-1, b, b));
    }

// org.apache.commons.math3.optimization.direct.CMAESOptimizerTest::testInitOutofbounds1
    public void testInitOutofbounds1() {
        double[] startPoint = point(DIM,3);
        double[] insigma = point(DIM, 0.3);
        double[][] boundaries = boundaries(DIM,-1,2);
        PointValuePair expected =
            new PointValuePair(point(DIM,1.0),0.0);
        doTest(new Rosen(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, LAMBDA, true, 0, 1e-13,
                1e-13, 1e-6, 100000, expected);
    }

// org.apache.commons.math3.optimization.direct.CMAESOptimizerTest::testInitOutofbounds2
    public void testInitOutofbounds2() {
        double[] startPoint = point(DIM, -2);
        double[] insigma = point(DIM, 0.3);
        double[][] boundaries = boundaries(DIM,-1,2);
        PointValuePair expected =
            new PointValuePair(point(DIM,1.0),0.0);
        doTest(new Rosen(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, LAMBDA, true, 0, 1e-13,
                1e-13, 1e-6, 100000, expected);
    }

// org.apache.commons.math3.optimization.direct.CMAESOptimizerTest::testBoundariesDimensionMismatch
    public void testBoundariesDimensionMismatch() {
        double[] startPoint = point(DIM,0.5);
        double[] insigma = point(DIM, 0.3);
        double[][] boundaries = boundaries(DIM+1,-1,2);
        PointValuePair expected =
            new PointValuePair(point(DIM,1.0),0.0);
        doTest(new Rosen(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, LAMBDA, true, 0, 1e-13,
                1e-13, 1e-6, 100000, expected);
    }

// org.apache.commons.math3.optimization.direct.CMAESOptimizerTest::testInputSigmaNegative
    public void testInputSigmaNegative() {
        double[] startPoint = point(DIM,0.5);
        double[] insigma = point(DIM,-0.5);
        double[][] boundaries = null;
        PointValuePair expected =
            new PointValuePair(point(DIM,1.0),0.0);
        doTest(new Rosen(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, LAMBDA, true, 0, 1e-13,
                1e-13, 1e-6, 100000, expected);
    }

// org.apache.commons.math3.optimization.direct.CMAESOptimizerTest::testInputSigmaOutOfRange
    public void testInputSigmaOutOfRange() {
        double[] startPoint = point(DIM,0.5);
        double[] insigma = point(DIM, 1.1);
        double[][] boundaries = boundaries(DIM,-0.5,0.5);
        PointValuePair expected =
            new PointValuePair(point(DIM,1.0),0.0);
        doTest(new Rosen(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, LAMBDA, true, 0, 1e-13,
                1e-13, 1e-6, 100000, expected);
    }

// org.apache.commons.math3.optimization.direct.CMAESOptimizerTest::testInputSigmaDimensionMismatch
    public void testInputSigmaDimensionMismatch() {
        double[] startPoint = point(DIM,0.5);
        double[] insigma = point(DIM + 1, 0.5);
        double[][] boundaries = null;
        PointValuePair expected =
            new PointValuePair(point(DIM,1.0),0.0);
        doTest(new Rosen(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, LAMBDA, true, 0, 1e-13,
                1e-13, 1e-6, 100000, expected);
    }

// org.apache.commons.math3.optimization.direct.CMAESOptimizerTest::testRosen
    public void testRosen() {
        double[] startPoint = point(DIM,0.1);
        double[] insigma = point(DIM,0.1);
        double[][] boundaries = null;
        PointValuePair expected =
            new PointValuePair(point(DIM,1.0),0.0);
        doTest(new Rosen(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, LAMBDA, true, 0, 1e-13,
                1e-13, 1e-6, 100000, expected);
        doTest(new Rosen(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, LAMBDA, false, 0, 1e-13,
                1e-13, 1e-6, 100000, expected);
    }

// org.apache.commons.math3.optimization.direct.CMAESOptimizerTest::testMaximize
    public void testMaximize() {}

// org.apache.commons.math3.optimization.direct.CMAESOptimizerTest::testEllipse
    public void testEllipse() {
        double[] startPoint = point(DIM,1.0);
        double[] insigma = point(DIM,0.1);
        double[][] boundaries = null;
        PointValuePair expected =
            new PointValuePair(point(DIM,0.0),0.0);
        doTest(new Elli(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, LAMBDA, true, 0, 1e-13,
                1e-13, 1e-6, 100000, expected);
        doTest(new Elli(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, LAMBDA, false, 0, 1e-13,
                1e-13, 1e-6, 100000, expected);
    }

// org.apache.commons.math3.optimization.direct.CMAESOptimizerTest::testElliRotated
    public void testElliRotated() {
        double[] startPoint = point(DIM,1.0);
        double[] insigma = point(DIM,0.1);
        double[][] boundaries = null;
        PointValuePair expected =
            new PointValuePair(point(DIM,0.0),0.0);
        doTest(new ElliRotated(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, LAMBDA, true, 0, 1e-13,
                1e-13, 1e-6, 100000, expected);
        doTest(new ElliRotated(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, LAMBDA, false, 0, 1e-13,
                1e-13, 1e-6, 100000, expected);
    }

// org.apache.commons.math3.optimization.direct.CMAESOptimizerTest::testCigar
    public void testCigar() {
        double[] startPoint = point(DIM,1.0);
        double[] insigma = point(DIM,0.1);
        double[][] boundaries = null;
        PointValuePair expected =
            new PointValuePair(point(DIM,0.0),0.0);
        doTest(new Cigar(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, LAMBDA, true, 0, 1e-13,
                1e-13, 1e-6, 200000, expected);
        doTest(new Cigar(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, LAMBDA, false, 0, 1e-13,
                1e-13, 1e-6, 100000, expected);
    }

// org.apache.commons.math3.optimization.direct.CMAESOptimizerTest::testCigarWithBoundaries
    public void testCigarWithBoundaries() {
        double[] startPoint = point(DIM,1.0);
        double[] insigma = point(DIM,0.1);
        double[][] boundaries = boundaries(DIM, -1e100, Double.POSITIVE_INFINITY);
        PointValuePair expected =
            new PointValuePair(point(DIM,0.0),0.0);
        doTest(new Cigar(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, LAMBDA, true, 0, 1e-13,
                1e-13, 1e-6, 200000, expected);
        doTest(new Cigar(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, LAMBDA, false, 0, 1e-13,
                1e-13, 1e-6, 100000, expected);
    }
