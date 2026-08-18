// buggy code
    public static double linearCombination(final double[] a, final double[] b)
        throws DimensionMismatchException {
        final int len = a.length;
        if (len != b.length) {
            throw new DimensionMismatchException(len, b.length);
        }

            // Revert to scalar multiplication.

        final double[] prodHigh = new double[len];
        double prodLowSum = 0;

        for (int i = 0; i < len; i++) {
            final double ai = a[i];
            final double ca = SPLIT_FACTOR * ai;
            final double aHigh = ca - (ca - ai);
            final double aLow = ai - aHigh;

            final double bi = b[i];
            final double cb = SPLIT_FACTOR * bi;
            final double bHigh = cb - (cb - bi);
            final double bLow = bi - bHigh;
            prodHigh[i] = ai * bi;
            final double prodLow = aLow * bLow - (((prodHigh[i] -
                                                    aHigh * bHigh) -
                                                   aLow * bHigh) -
                                                  aHigh * bLow);
            prodLowSum += prodLow;
        }


        final double prodHighCur = prodHigh[0];
        double prodHighNext = prodHigh[1];
        double sHighPrev = prodHighCur + prodHighNext;
        double sPrime = sHighPrev - prodHighNext;
        double sLowSum = (prodHighNext - (sHighPrev - sPrime)) + (prodHighCur - sPrime);

        final int lenMinusOne = len - 1;
        for (int i = 1; i < lenMinusOne; i++) {
            prodHighNext = prodHigh[i + 1];
            final double sHighCur = sHighPrev + prodHighNext;
            sPrime = sHighCur - prodHighNext;
            sLowSum += (prodHighNext - (sHighCur - sPrime)) + (sHighPrev - sPrime);
            sHighPrev = sHighCur;
        }

        double result = sHighPrev + (prodLowSum + sLowSum);

        if (Double.isNaN(result)) {
            // either we have split infinite numbers or some coefficients were NaNs,
            // just rely on the naive implementation and let IEEE754 handle this
            result = 0;
            for (int i = 0; i < len; ++i) {
                result += a[i] * b[i];
            }
        }

        return result;
    }

// relevant test
// org.apache.commons.math3.util.MathArraysTest::testLinearCombinationWithSingleElementArray
    public void testLinearCombinationWithSingleElementArray() {
        final double[] a = { 1.23456789 };
        final double[] b = { 98765432.1 };

        Assert.assertEquals(a[0] * b[0], MathArrays.linearCombination(a, b), 0d);
    }

// org.apache.commons.math3.util.MathArraysTest::testLinearCombination1
    public void testLinearCombination1() {
        final double[] a = new double[] {
            -1321008684645961.0 / 268435456.0,
            -5774608829631843.0 / 268435456.0,
            -7645843051051357.0 / 8589934592.0
        };
        final double[] b = new double[] {
            -5712344449280879.0 / 2097152.0,
            -4550117129121957.0 / 2097152.0,
            8846951984510141.0 / 131072.0
        };

        final double abSumInline = MathArrays.linearCombination(a[0], b[0],
                                                                a[1], b[1],
                                                                a[2], b[2]);
        final double abSumArray = MathArrays.linearCombination(a, b);

        Assert.assertEquals(abSumInline, abSumArray, 0);
        Assert.assertEquals(-1.8551294182586248737720779899, abSumInline, 1.0e-15);

        final double naive = a[0] * b[0] + a[1] * b[1] + a[2] * b[2];
        Assert.assertTrue(FastMath.abs(naive - abSumInline) > 1.5);

    }

// org.apache.commons.math3.util.MathArraysTest::testLinearCombination2
    public void testLinearCombination2() {
        
        
        Well1024a random = new Well1024a(553267312521321234l);

        for (int i = 0; i < 10000; ++i) {
            final double ux = 1e17 * random.nextDouble();
            final double uy = 1e17 * random.nextDouble();
            final double uz = 1e17 * random.nextDouble();
            final double vx = 1e17 * random.nextDouble();
            final double vy = 1e17 * random.nextDouble();
            final double vz = 1e17 * random.nextDouble();
            final double sInline = MathArrays.linearCombination(ux, vx,
                                                                uy, vy,
                                                                uz, vz);
            final double sArray = MathArrays.linearCombination(new double[] {ux, uy, uz},
                                                               new double[] {vx, vy, vz});
            Assert.assertEquals(sInline, sArray, 0);
        }
    }

// org.apache.commons.math3.util.MathArraysTest::testLinearCombinationInfinite
    public void testLinearCombinationInfinite() {
        final double[][] a = new double[][] {
            { 1, 2, 3, 4},
            { 1, Double.POSITIVE_INFINITY, 3, 4},
            { 1, 2, Double.POSITIVE_INFINITY, 4},
            { 1, Double.POSITIVE_INFINITY, 3, Double.NEGATIVE_INFINITY},
            { 1, 2, 3, 4},
            { 1, 2, 3, 4},
            { 1, 2, 3, 4},
            { 1, 2, 3, 4}
        };
        final double[][] b = new double[][] {
            { 1, -2, 3, 4},
            { 1, -2, 3, 4},
            { 1, -2, 3, 4},
            { 1, -2, 3, 4},
            { 1, Double.POSITIVE_INFINITY, 3, 4},
            { 1, -2, Double.POSITIVE_INFINITY, 4},
            { 1, Double.POSITIVE_INFINITY, 3, Double.NEGATIVE_INFINITY},
            { Double.NaN, -2, 3, 4}
        };

        Assert.assertEquals(-3,
                            MathArrays.linearCombination(a[0][0], b[0][0],
                                                         a[0][1], b[0][1]),
                            1.0e-10);
        Assert.assertEquals(6,
                            MathArrays.linearCombination(a[0][0], b[0][0],
                                                         a[0][1], b[0][1],
                                                         a[0][2], b[0][2]),
                            1.0e-10);
        Assert.assertEquals(22,
                            MathArrays.linearCombination(a[0][0], b[0][0],
                                                         a[0][1], b[0][1],
                                                         a[0][2], b[0][2],
                                                         a[0][3], b[0][3]),
                            1.0e-10);
        Assert.assertEquals(22, MathArrays.linearCombination(a[0], b[0]), 1.0e-10);

        Assert.assertEquals(Double.NEGATIVE_INFINITY,
                            MathArrays.linearCombination(a[1][0], b[1][0],
                                                         a[1][1], b[1][1]),
                            1.0e-10);
        Assert.assertEquals(Double.NEGATIVE_INFINITY,
                            MathArrays.linearCombination(a[1][0], b[1][0],
                                                         a[1][1], b[1][1],
                                                         a[1][2], b[1][2]),
                            1.0e-10);
        Assert.assertEquals(Double.NEGATIVE_INFINITY,
                            MathArrays.linearCombination(a[1][0], b[1][0],
                                                         a[1][1], b[1][1],
                                                         a[1][2], b[1][2],
                                                         a[1][3], b[1][3]),
                            1.0e-10);
        Assert.assertEquals(Double.NEGATIVE_INFINITY, MathArrays.linearCombination(a[1], b[1]), 1.0e-10);

        Assert.assertEquals(-3,
                            MathArrays.linearCombination(a[2][0], b[2][0],
                                                         a[2][1], b[2][1]),
                            1.0e-10);
        Assert.assertEquals(Double.POSITIVE_INFINITY,
                            MathArrays.linearCombination(a[2][0], b[2][0],
                                                         a[2][1], b[2][1],
                                                         a[2][2], b[2][2]),
                            1.0e-10);
        Assert.assertEquals(Double.POSITIVE_INFINITY,
                            MathArrays.linearCombination(a[2][0], b[2][0],
                                                         a[2][1], b[2][1],
                                                         a[2][2], b[2][2],
                                                         a[2][3], b[2][3]),
                            1.0e-10);
        Assert.assertEquals(Double.POSITIVE_INFINITY, MathArrays.linearCombination(a[2], b[2]), 1.0e-10);

        Assert.assertEquals(Double.NEGATIVE_INFINITY,
                            MathArrays.linearCombination(a[3][0], b[3][0],
                                                         a[3][1], b[3][1]),
                            1.0e-10);
        Assert.assertEquals(Double.NEGATIVE_INFINITY,
                            MathArrays.linearCombination(a[3][0], b[3][0],
                                                         a[3][1], b[3][1],
                                                         a[3][2], b[3][2]),
                            1.0e-10);
        Assert.assertEquals(Double.NEGATIVE_INFINITY,
                            MathArrays.linearCombination(a[3][0], b[3][0],
                                                         a[3][1], b[3][1],
                                                         a[3][2], b[3][2],
                                                         a[3][3], b[3][3]),
                            1.0e-10);
        Assert.assertEquals(Double.NEGATIVE_INFINITY, MathArrays.linearCombination(a[3], b[3]), 1.0e-10);

        Assert.assertEquals(Double.POSITIVE_INFINITY,
                            MathArrays.linearCombination(a[4][0], b[4][0],
                                                         a[4][1], b[4][1]),
                            1.0e-10);
        Assert.assertEquals(Double.POSITIVE_INFINITY,
                            MathArrays.linearCombination(a[4][0], b[4][0],
                                                         a[4][1], b[4][1],
                                                         a[4][2], b[4][2]),
                            1.0e-10);
        Assert.assertEquals(Double.POSITIVE_INFINITY,
                            MathArrays.linearCombination(a[4][0], b[4][0],
                                                         a[4][1], b[4][1],
                                                         a[4][2], b[4][2],
                                                         a[4][3], b[4][3]),
                            1.0e-10);
        Assert.assertEquals(Double.POSITIVE_INFINITY, MathArrays.linearCombination(a[4], b[4]), 1.0e-10);

        Assert.assertEquals(-3,
                            MathArrays.linearCombination(a[5][0], b[5][0],
                                                         a[5][1], b[5][1]),
                            1.0e-10);
        Assert.assertEquals(Double.POSITIVE_INFINITY,
                            MathArrays.linearCombination(a[5][0], b[5][0],
                                                         a[5][1], b[5][1],
                                                         a[5][2], b[5][2]),
                            1.0e-10);
        Assert.assertEquals(Double.POSITIVE_INFINITY,
                            MathArrays.linearCombination(a[5][0], b[5][0],
                                                         a[5][1], b[5][1],
                                                         a[5][2], b[5][2],
                                                         a[5][3], b[5][3]),
                            1.0e-10);
        Assert.assertEquals(Double.POSITIVE_INFINITY, MathArrays.linearCombination(a[5], b[5]), 1.0e-10);

        Assert.assertEquals(Double.POSITIVE_INFINITY,
                            MathArrays.linearCombination(a[6][0], b[6][0],
                                                         a[6][1], b[6][1]),
                            1.0e-10);
        Assert.assertEquals(Double.POSITIVE_INFINITY,
                            MathArrays.linearCombination(a[6][0], b[6][0],
                                                         a[6][1], b[6][1],
                                                         a[6][2], b[6][2]),
                            1.0e-10);
        Assert.assertTrue(Double.isNaN(MathArrays.linearCombination(a[6][0], b[6][0],
                                                                    a[6][1], b[6][1],
                                                                    a[6][2], b[6][2],
                                                                    a[6][3], b[6][3])));
        Assert.assertTrue(Double.isNaN(MathArrays.linearCombination(a[6], b[6])));

        Assert.assertTrue(Double.isNaN(MathArrays.linearCombination(a[7][0], b[7][0],
                                                                    a[7][1], b[7][1])));
        Assert.assertTrue(Double.isNaN(MathArrays.linearCombination(a[7][0], b[7][0],
                                                                    a[7][1], b[7][1],
                                                                    a[7][2], b[7][2])));
        Assert.assertTrue(Double.isNaN(MathArrays.linearCombination(a[7][0], b[7][0],
                                                                    a[7][1], b[7][1],
                                                                    a[7][2], b[7][2],
                                                                    a[7][3], b[7][3])));
        Assert.assertTrue(Double.isNaN(MathArrays.linearCombination(a[7], b[7])));
    }

// org.apache.commons.math3.util.MathArraysTest::testArrayEquals
    public void testArrayEquals() {
        Assert.assertFalse(MathArrays.equals(new double[] { 1d }, null));
        Assert.assertFalse(MathArrays.equals(null, new double[] { 1d }));
        Assert.assertTrue(MathArrays.equals((double[]) null, (double[]) null));

        Assert.assertFalse(MathArrays.equals(new double[] { 1d }, new double[0]));
        Assert.assertTrue(MathArrays.equals(new double[] { 1d }, new double[] { 1d }));
        Assert.assertTrue(MathArrays.equals(new double[] { Double.POSITIVE_INFINITY,
                                                           Double.NEGATIVE_INFINITY, 1d, 0d },
                                            new double[] { Double.POSITIVE_INFINITY,
                                                           Double.NEGATIVE_INFINITY, 1d, 0d }));
        Assert.assertFalse(MathArrays.equals(new double[] { Double.NaN },
                                             new double[] { Double.NaN }));
        Assert.assertFalse(MathArrays.equals(new double[] { Double.POSITIVE_INFINITY },
                                             new double[] { Double.NEGATIVE_INFINITY }));
        Assert.assertFalse(MathArrays.equals(new double[] { 1d },
                                             new double[] { FastMath.nextAfter(FastMath.nextAfter(1d, 2d), 2d) }));

    }

// org.apache.commons.math3.util.MathArraysTest::testArrayEqualsIncludingNaN
    public void testArrayEqualsIncludingNaN() {
        Assert.assertFalse(MathArrays.equalsIncludingNaN(new double[] { 1d }, null));
        Assert.assertFalse(MathArrays.equalsIncludingNaN(null, new double[] { 1d }));
        Assert.assertTrue(MathArrays.equalsIncludingNaN((double[]) null, (double[]) null));

        Assert.assertFalse(MathArrays.equalsIncludingNaN(new double[] { 1d }, new double[0]));
        Assert.assertTrue(MathArrays.equalsIncludingNaN(new double[] { 1d }, new double[] { 1d }));
        Assert.assertTrue(MathArrays.equalsIncludingNaN(new double[] { Double.NaN, Double.POSITIVE_INFINITY,
                                                                       Double.NEGATIVE_INFINITY, 1d, 0d },
                                                        new double[] { Double.NaN, Double.POSITIVE_INFINITY,
                                                                       Double.NEGATIVE_INFINITY, 1d, 0d }));
        Assert.assertFalse(MathArrays.equalsIncludingNaN(new double[] { Double.POSITIVE_INFINITY },
                                                         new double[] { Double.NEGATIVE_INFINITY }));
        Assert.assertFalse(MathArrays.equalsIncludingNaN(new double[] { 1d },
                                                         new double[] { FastMath.nextAfter(FastMath.nextAfter(1d, 2d), 2d) }));
    }

// org.apache.commons.math3.util.MathArraysTest::testNormalizeArray
    public void testNormalizeArray() {
        double[] testValues1 = new double[] {1, 1, 2};
        TestUtils.assertEquals( new double[] {.25, .25, .5},
                                MathArrays.normalizeArray(testValues1, 1),
                                Double.MIN_VALUE);

        double[] testValues2 = new double[] {-1, -1, 1};
        TestUtils.assertEquals( new double[] {1, 1, -1},
                                MathArrays.normalizeArray(testValues2, 1),
                                Double.MIN_VALUE);

        
        double[] testValues3 = new double[] {-1, -1, Double.NaN, 1, Double.NaN};
        TestUtils.assertEquals( new double[] {1, 1,Double.NaN, -1, Double.NaN},
                                MathArrays.normalizeArray(testValues3, 1),
                                Double.MIN_VALUE);

        
        double[] zeroSum = new double[] {-1, 1};
        try {
            MathArrays.normalizeArray(zeroSum, 1);
            Assert.fail("expecting MathArithmeticException");
        } catch (MathArithmeticException ex) {}

        
        double[] hasInf = new double[] {1, 2, 1, Double.NEGATIVE_INFINITY};
        try {
            MathArrays.normalizeArray(hasInf, 1);
            Assert.fail("expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {}

        
        try {
            MathArrays.normalizeArray(testValues1, Double.POSITIVE_INFINITY);
            Assert.fail("expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {}

        
        try {
            MathArrays.normalizeArray(testValues1, Double.NaN);
            Assert.fail("expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {}
    }

// org.apache.commons.math3.util.MathArraysTest::testConvolve
    public void testConvolve() {
        
        double[] x1 = { 1.2, -1.8, 1.4 };
        double[] h1 = { 1, 0.8, 0.5, 0.3 };
        double[] y1 = { 1.2, -0.84, 0.56, 0.58, 0.16, 0.42 };
        double tolerance = 1e-13;

        double[] yActual = MathArrays.convolve(x1, h1);
        Assert.assertArrayEquals(y1, yActual, tolerance);

        double[] x2 = { 1, 2, 3 };
        double[] h2 = { 0, 1, 0.5 };
        double[] y2 = { 0, 1, 2.5, 4, 1.5 };
        
        yActual = MathArrays.convolve(x2, h2);
        Assert.assertArrayEquals(y2, yActual, tolerance);
                
        try {
            MathArrays.convolve(new double[]{1, 2}, null);
            Assert.fail("an exception should have been thrown");
        } catch (NullArgumentException e) {
            
        }

        try {
            MathArrays.convolve(null, new double[]{1, 2});
            Assert.fail("an exception should have been thrown");
        } catch (NullArgumentException e) {
            
        }

        try {
            MathArrays.convolve(new double[]{1, 2}, new double[]{});
            Assert.fail("an exception should have been thrown");
        } catch (NoDataException e) {
            
        }

        try {
            MathArrays.convolve(new double[]{}, new double[]{1, 2});
            Assert.fail("an exception should have been thrown");
        } catch (NoDataException e) {
            
        }

        try {
            MathArrays.convolve(new double[]{}, new double[]{});
            Assert.fail("an exception should have been thrown");
        } catch (NoDataException e) {
            
        }
    }

// org.apache.commons.math3.util.MultidimensionalCounterTest::testPreconditions
    public void testPreconditions() {
        MultidimensionalCounter c;

        try {
            c = new MultidimensionalCounter(0, 1);
            Assert.fail("NotStrictlyPositiveException expected");
        } catch (NotStrictlyPositiveException e) {
            
        }
        try {
            c = new MultidimensionalCounter(2, 0);
            Assert.fail("NotStrictlyPositiveException expected");
        } catch (NotStrictlyPositiveException e) {
            
        }
        try {
            c = new MultidimensionalCounter(-1, 1);
            Assert.fail("NotStrictlyPositiveException expected");
        } catch (NotStrictlyPositiveException e) {
            
        }

        c = new MultidimensionalCounter(2, 3);
        try {
            c.getCount(1, 1, 1);
            Assert.fail("DimensionMismatchException expected");
        } catch (DimensionMismatchException e) {
            
        }
        try {
            c.getCount(3, 1);
            Assert.fail("OutOfRangeException expected");
        } catch (OutOfRangeException e) {
            
        }
        try {
            c.getCount(0, -1);
            Assert.fail("OutOfRangeException expected");
        } catch (OutOfRangeException e) {
            
        }
        try {
            c.getCounts(-1);
            Assert.fail("OutOfRangeException expected");
        } catch (OutOfRangeException e) {
            
        }
        try {
            c.getCounts(6);
            Assert.fail("OutOfRangeException expected");
        } catch (OutOfRangeException e) {
            
        }
    }

// org.apache.commons.math3.util.MultidimensionalCounterTest::testIteratorPreconditions
    public void testIteratorPreconditions() {
        MultidimensionalCounter.Iterator iter = (new MultidimensionalCounter(2, 3)).iterator();
        try {
            iter.getCount(-1);
            Assert.fail("IndexOutOfBoundsException expected");
        } catch (IndexOutOfBoundsException e) {
            
        }
        try {
            iter.getCount(2);
            Assert.fail("IndexOutOfBoundsException expected");
        } catch (IndexOutOfBoundsException e) {
            
        }
    }

// org.apache.commons.math3.util.MultidimensionalCounterTest::testMulti2UniConversion
    public void testMulti2UniConversion() {
        final MultidimensionalCounter c = new MultidimensionalCounter(2, 4, 5);
        Assert.assertEquals(c.getCount(1, 2, 3), 33);
    }

// org.apache.commons.math3.util.MultidimensionalCounterTest::testAccessors
    public void testAccessors() {
        final int[] originalSize = new int[] {2, 6, 5};
        final MultidimensionalCounter c = new MultidimensionalCounter(originalSize);
        final int nDim = c.getDimension();
        Assert.assertEquals(nDim, originalSize.length);

        final int[] size = c.getSizes();
        for (int i = 0; i < nDim; i++) {
            Assert.assertEquals(originalSize[i], size[i]);
        }
    }

// org.apache.commons.math3.util.MultidimensionalCounterTest::testIterationConsistency
    public void testIterationConsistency() {
        final MultidimensionalCounter c = new MultidimensionalCounter(2, 3, 4);
        final int[][] expected = new int[][] {
            { 0, 0, 0 },
            { 0, 0, 1 },
            { 0, 0, 2 },
            { 0, 0, 3 },
            { 0, 1, 0 },
            { 0, 1, 1 },
            { 0, 1, 2 },
            { 0, 1, 3 },
            { 0, 2, 0 },
            { 0, 2, 1 },
            { 0, 2, 2 },
            { 0, 2, 3 },
            { 1, 0, 0 },
            { 1, 0, 1 },
            { 1, 0, 2 },
            { 1, 0, 3 },
            { 1, 1, 0 },
            { 1, 1, 1 },
            { 1, 1, 2 },
            { 1, 1, 3 },
            { 1, 2, 0 },
            { 1, 2, 1 },
            { 1, 2, 2 },
            { 1, 2, 3 }
        };

        final int totalSize = c.getSize();
        final int nDim = c.getDimension();
        final MultidimensionalCounter.Iterator iter = c.iterator();
        for (int i = 0; i < totalSize; i++) {
            if (!iter.hasNext()) {
                Assert.fail("Too short");
            }
            final int uniDimIndex = iter.next();
            Assert.assertEquals("Wrong iteration at " + i, i, uniDimIndex);

            for (int dimIndex = 0; dimIndex < nDim; dimIndex++) {
                Assert.assertEquals("Wrong multidimensional index for [" + i + "][" + dimIndex + "]",
                                    expected[i][dimIndex], iter.getCount(dimIndex));
            }

            Assert.assertEquals("Wrong unidimensional index for [" + i + "]",
                                c.getCount(expected[i]), uniDimIndex);

            final int[] indices = c.getCounts(uniDimIndex);
            for (int dimIndex = 0; dimIndex < nDim; dimIndex++) {
                Assert.assertEquals("Wrong multidimensional index for [" + i + "][" + dimIndex + "]",
                                    expected[i][dimIndex], indices[dimIndex]);
            }
        }

        if (iter.hasNext()) {
            Assert.fail("Too long");
        }
    }

// org.apache.commons.math3.util.ResizableDoubleArrayTest::testConstructors
    public void testConstructors() {
        float defaultExpansionFactor = 2.0f;
        float defaultContractionCriteria = 2.5f;
        int defaultMode = ResizableDoubleArray.MULTIPLICATIVE_MODE;

        ResizableDoubleArray testDa = new ResizableDoubleArray(2);
        Assert.assertEquals(0, testDa.getNumElements());
        Assert.assertEquals(2, testDa.getCapacity());
        Assert.assertEquals(defaultExpansionFactor, testDa.getExpansionFactor(), 0);
        Assert.assertEquals(defaultContractionCriteria, testDa.getContractionCriteria(), 0);
        Assert.assertEquals(defaultMode, testDa.getExpansionMode());
        try {
            da = new ResizableDoubleArray(-1);
            Assert.fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }
        
        testDa = new ResizableDoubleArray((double[]) null);
        Assert.assertEquals(0, testDa.getNumElements());
        
        double[] initialArray = new double[] { 0, 1, 2 };        
        testDa = new ResizableDoubleArray(initialArray);
        Assert.assertEquals(3, testDa.getNumElements());

        testDa = new ResizableDoubleArray(2, 2.0f);
        Assert.assertEquals(0, testDa.getNumElements());
        Assert.assertEquals(2, testDa.getCapacity());
        Assert.assertEquals(defaultExpansionFactor, testDa.getExpansionFactor(), 0);
        Assert.assertEquals(defaultContractionCriteria, testDa.getContractionCriteria(), 0);
        Assert.assertEquals(defaultMode, testDa.getExpansionMode());

        try {
            da = new ResizableDoubleArray(2, 0.5f);
            Assert.fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }

        testDa = new ResizableDoubleArray(2, 3.0f);
        Assert.assertEquals(3.0f, testDa.getExpansionFactor(), 0);
        Assert.assertEquals(3.5f, testDa.getContractionCriteria(), 0);

        testDa = new ResizableDoubleArray(2, 2.0f, 3.0f);
        Assert.assertEquals(0, testDa.getNumElements());
        Assert.assertEquals(2, testDa.getCapacity());
        Assert.assertEquals(defaultExpansionFactor, testDa.getExpansionFactor(), 0);
        Assert.assertEquals(3.0f, testDa.getContractionCriteria(), 0);
        Assert.assertEquals(defaultMode, testDa.getExpansionMode());

        try {
            da = new ResizableDoubleArray(2, 2.0f, 1.5f);
            Assert.fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }

        testDa = new ResizableDoubleArray(2, 2.0f, 3.0f,
                ResizableDoubleArray.ADDITIVE_MODE);
        Assert.assertEquals(0, testDa.getNumElements());
        Assert.assertEquals(2, testDa.getCapacity());
        Assert.assertEquals(defaultExpansionFactor, testDa.getExpansionFactor(), 0);
        Assert.assertEquals(3.0f, testDa.getContractionCriteria(), 0);
        Assert.assertEquals(ResizableDoubleArray.ADDITIVE_MODE,
                testDa.getExpansionMode());

        try {
            da = new ResizableDoubleArray(2, 2.0f, 2.5f, -1);
            Assert.fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }

        
        testDa = new ResizableDoubleArray(2, 2.0f, 3.0f,
                ResizableDoubleArray.ADDITIVE_MODE);
        testDa.addElement(2.0);
        testDa.addElement(3.2);
        ResizableDoubleArray copyDa = new ResizableDoubleArray(testDa);
        Assert.assertEquals(copyDa, testDa);
        Assert.assertEquals(testDa, copyDa);
    }

// org.apache.commons.math3.util.ResizableDoubleArrayTest::testSetElementArbitraryExpansion1
    public void testSetElementArbitraryExpansion1() {

        
        da.addElement(2.0);
        da.addElement(4.0);
        da.addElement(6.0);
        da.setElement(1, 3.0);

        
        da.setElement(1000, 3.4);

        Assert.assertEquals( "The number of elements should now be 1001, it isn't",
                da.getNumElements(), 1001);

        Assert.assertEquals( "Uninitialized Elements are default value of 0.0, index 766 wasn't", 0.0,
                da.getElement( 760 ), Double.MIN_VALUE );

        Assert.assertEquals( "The 1000th index should be 3.4, it isn't", 3.4, da.getElement(1000),
                Double.MIN_VALUE );
        Assert.assertEquals( "The 0th index should be 2.0, it isn't", 2.0, da.getElement(0),
                Double.MIN_VALUE);
    }

// org.apache.commons.math3.util.ResizableDoubleArrayTest::testSetElementArbitraryExpansion2
    public void testSetElementArbitraryExpansion2() {
        
        da.addElement(2.0);
        da.addElement(4.0);
        da.addElement(6.0);
        Assert.assertEquals(16, ((ResizableDoubleArray) da).getCapacity());
        Assert.assertEquals(3, da.getNumElements());
        da.setElement(3, 7.0);
        Assert.assertEquals(16, ((ResizableDoubleArray) da).getCapacity());
        Assert.assertEquals(4, da.getNumElements());
        da.setElement(10, 10.0);
        Assert.assertEquals(16, ((ResizableDoubleArray) da).getCapacity());
        Assert.assertEquals(11, da.getNumElements());
        da.setElement(9, 10.0);
        Assert.assertEquals(16, ((ResizableDoubleArray) da).getCapacity());
        Assert.assertEquals(11, da.getNumElements());

        try {
            da.setElement(-2, 3);
            Assert.fail("Expecting ArrayIndexOutOfBoundsException for negative index");
        } catch (ArrayIndexOutOfBoundsException ex) {
            
        }

        

        ResizableDoubleArray testDa = new ResizableDoubleArray(2, 2.0f, 3.0f,
                ResizableDoubleArray.ADDITIVE_MODE);
        Assert.assertEquals(2, testDa.getCapacity());
        testDa.addElement(1d);
        testDa.addElement(1d);
        Assert.assertEquals(2, testDa.getCapacity());
        testDa.addElement(1d);
        Assert.assertEquals(4, testDa.getCapacity());
    }

// org.apache.commons.math3.util.ResizableDoubleArrayTest::testAdd1000
    public void testAdd1000() {
        super.testAdd1000();
        Assert.assertEquals("Internal Storage length should be 1024 if we started out with initial capacity of " +
                "16 and an expansion factor of 2.0",
                1024, ((ResizableDoubleArray) da).getCapacity());
    }

// org.apache.commons.math3.util.ResizableDoubleArrayTest::testAddElements
    public void testAddElements() {
        ResizableDoubleArray testDa = new ResizableDoubleArray();
        
        
        testDa.addElements(new double[] {4, 5, 6});
        Assert.assertEquals(3, testDa.getNumElements(), 0);
        Assert.assertEquals(4, testDa.getElement(0), 0);
        Assert.assertEquals(5, testDa.getElement(1), 0);
        Assert.assertEquals(6, testDa.getElement(2), 0);
        
        testDa.addElements(new double[] {4, 5, 6});
        Assert.assertEquals(6, testDa.getNumElements());

        
        testDa = new ResizableDoubleArray(2, 2.0f, 2.5f,
                ResizableDoubleArray.ADDITIVE_MODE);        
        Assert.assertEquals(2, testDa.getCapacity());
        testDa.addElements(new double[] { 1d }); 
        testDa.addElements(new double[] { 2d }); 
        testDa.addElements(new double[] { 3d }); 
        Assert.assertEquals(1d, testDa.getElement(0), 0);
        Assert.assertEquals(2d, testDa.getElement(1), 0);
        Assert.assertEquals(3d, testDa.getElement(2), 0);
        Assert.assertEquals(4, testDa.getCapacity());  
        Assert.assertEquals(3, testDa.getNumElements());
    }

// org.apache.commons.math3.util.ResizableDoubleArrayTest::testAddElementRolling
    public void testAddElementRolling() {
        super.testAddElementRolling();

        
        da.clear();
        da.addElement(1);
        da.addElement(2);
        da.addElementRolling(3);
        Assert.assertEquals(3, da.getElement(1), 0);
        da.addElementRolling(4);
        Assert.assertEquals(3, da.getElement(0), 0);
        Assert.assertEquals(4, da.getElement(1), 0);
        da.addElement(5);
        Assert.assertEquals(5, da.getElement(2), 0);
        da.addElementRolling(6);
        Assert.assertEquals(4, da.getElement(0), 0);
        Assert.assertEquals(5, da.getElement(1), 0);
        Assert.assertEquals(6, da.getElement(2), 0);

        
        ResizableDoubleArray testDa = new ResizableDoubleArray(2, 2.0f, 2.5f,
                ResizableDoubleArray.ADDITIVE_MODE);
        Assert.assertEquals(2, testDa.getCapacity());
        testDa.addElement(1d); 
        testDa.addElement(2d); 
        testDa.addElement(3d); 
        Assert.assertEquals(1d, testDa.getElement(0), 0);
        Assert.assertEquals(2d, testDa.getElement(1), 0);
        Assert.assertEquals(3d, testDa.getElement(2), 0);
        Assert.assertEquals(4, testDa.getCapacity());  
        Assert.assertEquals(3, testDa.getNumElements());
        testDa.addElementRolling(4d);
        Assert.assertEquals(2d, testDa.getElement(0), 0);
        Assert.assertEquals(3d, testDa.getElement(1), 0);
        Assert.assertEquals(4d, testDa.getElement(2), 0);
        Assert.assertEquals(4, testDa.getCapacity());  
        Assert.assertEquals(3, testDa.getNumElements());
        testDa.addElementRolling(5d);   
        Assert.assertEquals(3d, testDa.getElement(0), 0);
        Assert.assertEquals(4d, testDa.getElement(1), 0);
        Assert.assertEquals(5d, testDa.getElement(2), 0);
        Assert.assertEquals(4, testDa.getCapacity());  
        Assert.assertEquals(3, testDa.getNumElements());
        try {
            testDa.getElement(4);
            Assert.fail("Expecting ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException ex) {
            
        }
        try {
            testDa.getElement(-1);
            Assert.fail("Expecting ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException ex) {
            
        }
    }

// org.apache.commons.math3.util.ResizableDoubleArrayTest::testSetNumberOfElements
    public void testSetNumberOfElements() {
        da.addElement( 1.0 );
        da.addElement( 1.0 );
        da.addElement( 1.0 );
        da.addElement( 1.0 );
        da.addElement( 1.0 );
        da.addElement( 1.0 );
        Assert.assertEquals( "Number of elements should equal 6", da.getNumElements(), 6);

        ((ResizableDoubleArray) da).setNumElements( 3 );
        Assert.assertEquals( "Number of elements should equal 3", da.getNumElements(), 3);

        try {
            ((ResizableDoubleArray) da).setNumElements( -3 );
            Assert.fail( "Setting number of elements to negative should've thrown an exception");
        } catch( IllegalArgumentException iae ) {
        }

        ((ResizableDoubleArray) da).setNumElements(1024);
        Assert.assertEquals( "Number of elements should now be 1024", da.getNumElements(), 1024);
        Assert.assertEquals( "Element 453 should be a default double", da.getElement( 453 ), 0.0, Double.MIN_VALUE);

    }

// org.apache.commons.math3.util.ResizableDoubleArrayTest::testWithInitialCapacity
    public void testWithInitialCapacity() {

        ResizableDoubleArray eDA2 = new ResizableDoubleArray(2);
        Assert.assertEquals("Initial number of elements should be 0", 0, eDA2.getNumElements());

        final IntegerDistribution randomData = new UniformIntegerDistribution(100, 1000);
        final int iterations = randomData.sample();

        for( int i = 0; i < iterations; i++) {
            eDA2.addElement( i );
        }

        Assert.assertEquals("Number of elements should be equal to " + iterations, iterations, eDA2.getNumElements());

        eDA2.addElement( 2.0 );

        Assert.assertEquals("Number of elements should be equals to " + (iterations +1),
                iterations + 1 , eDA2.getNumElements() );
    }

// org.apache.commons.math3.util.ResizableDoubleArrayTest::testWithInitialCapacityAndExpansionFactor
    public void testWithInitialCapacityAndExpansionFactor() {

        ResizableDoubleArray eDA3 = new ResizableDoubleArray(3, 3.0f, 3.5f);
        Assert.assertEquals("Initial number of elements should be 0", 0, eDA3.getNumElements() );

        final IntegerDistribution randomData = new UniformIntegerDistribution(100, 3000);
        final int iterations = randomData.sample();

        for( int i = 0; i < iterations; i++) {
            eDA3.addElement( i );
        }

        Assert.assertEquals("Number of elements should be equal to " + iterations, iterations,eDA3.getNumElements());

        eDA3.addElement( 2.0 );

        Assert.assertEquals("Number of elements should be equals to " + (iterations +1),
                iterations +1, eDA3.getNumElements() );

        Assert.assertEquals("Expansion factor should equal 3.0", 3.0f, eDA3.getExpansionFactor(), Double.MIN_VALUE);
    }

// org.apache.commons.math3.util.ResizableDoubleArrayTest::testDiscard
    public void testDiscard() {
        da.addElement(2.0);
        da.addElement(2.0);
        da.addElement(2.0);
        da.addElement(2.0);
        da.addElement(2.0);
        da.addElement(2.0);
        da.addElement(2.0);
        da.addElement(2.0);
        da.addElement(2.0);
        da.addElement(2.0);
        da.addElement(2.0);
        Assert.assertEquals( "Number of elements should be 11", 11, da.getNumElements());

        ((ResizableDoubleArray)da).discardFrontElements(5);
        Assert.assertEquals( "Number of elements should be 6", 6, da.getNumElements());

        da.addElement(2.0);
        da.addElement(2.0);
        da.addElement(2.0);
        da.addElement(2.0);
        Assert.assertEquals( "Number of elements should be 10", 10, da.getNumElements());

        ((ResizableDoubleArray)da).discardMostRecentElements(2);
        Assert.assertEquals( "Number of elements should be 8", 8, da.getNumElements());

        try {
            ((ResizableDoubleArray)da).discardFrontElements(-1);
            Assert.fail( "Trying to discard a negative number of element is not allowed");
        } catch( Exception e ){
        }

        try {
            ((ResizableDoubleArray)da).discardMostRecentElements(-1);
            Assert.fail( "Trying to discard a negative number of element is not allowed");
        } catch( Exception e ){
        }

        try {
            ((ResizableDoubleArray)da).discardFrontElements( 10000 );
            Assert.fail( "You can't discard more elements than the array contains");
        } catch( Exception e ){
        }

        try {
            ((ResizableDoubleArray)da).discardMostRecentElements( 10000 );
            Assert.fail( "You can't discard more elements than the array contains");
        } catch( Exception e ){
        }

    }

// org.apache.commons.math3.util.ResizableDoubleArrayTest::testSubstitute
    public void testSubstitute() {

        da.addElement(2.0);
        da.addElement(2.0);
        da.addElement(2.0);
        da.addElement(2.0);
        da.addElement(2.0);
        da.addElement(2.0);
        da.addElement(2.0);
        da.addElement(2.0);
        da.addElement(2.0);
        da.addElement(2.0);
        da.addElement(2.0);
        Assert.assertEquals( "Number of elements should be 11", 11, da.getNumElements());

        ((ResizableDoubleArray)da).substituteMostRecentElement(24);

        Assert.assertEquals( "Number of elements should be 11", 11, da.getNumElements());

        try {
            ((ResizableDoubleArray)da).discardMostRecentElements(10);
        } catch( Exception e ){
            Assert.fail( "Trying to discard a negative number of element is not allowed");
        }

        ((ResizableDoubleArray)da).substituteMostRecentElement(24);

        Assert.assertEquals( "Number of elements should be 1", 1, da.getNumElements());

    }

// org.apache.commons.math3.util.ResizableDoubleArrayTest::testMutators
    public void testMutators() {
        ((ResizableDoubleArray)da).setContractionCriteria(10f);
        Assert.assertEquals(10f, ((ResizableDoubleArray)da).getContractionCriteria(), 0);
        ((ResizableDoubleArray)da).setExpansionFactor(8f);
        Assert.assertEquals(8f, ((ResizableDoubleArray)da).getExpansionFactor(), 0);
        try {
            ((ResizableDoubleArray)da).setExpansionFactor(11f);  
            Assert.fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }
        ((ResizableDoubleArray)da).setExpansionMode(
                ResizableDoubleArray.ADDITIVE_MODE);
        Assert.assertEquals(ResizableDoubleArray.ADDITIVE_MODE,
                ((ResizableDoubleArray)da).getExpansionMode());
        try {
            ((ResizableDoubleArray)da).setExpansionMode(-1);
            Assert.fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math3.util.ResizableDoubleArrayTest::testEqualsAndHashCode
    public void testEqualsAndHashCode() throws Exception {

        
        ResizableDoubleArray first = new ResizableDoubleArray();
        Double other = new Double(2);
        Assert.assertFalse(first.equals(other));

        
        other = null;
        Assert.assertFalse(first.equals(other));

        
        Assert.assertTrue(first.equals(first));

        
        ResizableDoubleArray second = new ResizableDoubleArray();
        verifyEquality(first, second);

        
        ResizableDoubleArray third = new ResizableDoubleArray(3, 2.0f, 2.0f);
        verifyInequality(third, first);
        ResizableDoubleArray fourth = new ResizableDoubleArray(3, 2.0f, 2.0f);
        ResizableDoubleArray fifth = new ResizableDoubleArray(2, 2.0f, 2.0f);
        verifyEquality(third, fourth);
        verifyInequality(third, fifth);
        third.addElement(4.1);
        third.addElement(4.2);
        third.addElement(4.3);
        fourth.addElement(4.1);
        fourth.addElement(4.2);
        fourth.addElement(4.3);
        verifyEquality(third, fourth);

        
        fourth.addElement(4.4);
        verifyInequality(third, fourth);
        third.addElement(4.4);
        verifyEquality(third, fourth);
        fourth.addElement(4.4);
        verifyInequality(third, fourth);
        third.addElement(4.4);
        verifyEquality(third, fourth);
        fourth.addElementRolling(4.5);
        third.addElementRolling(4.5);
        verifyEquality(third, fourth);

        
        third.discardFrontElements(1);
        verifyInequality(third, fourth);
        fourth.discardFrontElements(1);
        verifyEquality(third, fourth);

        
        third.discardMostRecentElements(2);
        fourth.discardMostRecentElements(2);
        verifyEquality(third, fourth);

        
        third.addElement(18);
        fourth.addElement(17);
        third.addElement(17);
        fourth.addElement(18);
        verifyInequality(third, fourth);

        
        ResizableDoubleArray.copy(fourth, fifth);
        verifyEquality(fourth, fifth);

        
        verifyEquality(fourth, new ResizableDoubleArray(fourth));

        
        verifyEquality(fourth, fourth.copy());

    }

// org.apache.commons.math3.util.ResizableDoubleArrayTest::testGetArrayRef
    public void testGetArrayRef() {
        final ResizableDoubleArray a = new ResizableDoubleArray();

        
        final int index = 20;
        final double v1 = 1.2;
        a.setElement(index, v1);

        
        final double v2 = v1 + 3.4;
        final double[] aInternalArray = a.getArrayRef();
        aInternalArray[a.getStartIndex() + index] = v2;

        Assert.assertEquals(v2, a.getElement(index), 0d);
    }

// org.apache.commons.math3.util.ResizableDoubleArrayTest::testCompute
    public void testCompute() {
        final ResizableDoubleArray a = new ResizableDoubleArray();
        final int max = 20;
        for (int i = 1; i <= max; i++) {
            a.setElement(i, i);
        }

        final MathArrays.Function add = new MathArrays.Function() {
                public double evaluate(double[] a, int index, int num) {
                    double sum = 0;
                    final int max = index + num;
                    for (int i = index; i < max; i++) {
                        sum += a[i];
                    }
                    return sum;
                }
                public double evaluate(double[] a) {
                    return evaluate(a, 0, a.length);
                }
            };

        final double sum = a.compute(add);
        Assert.assertEquals(0.5 * max * (max + 1), sum, 0);
    }
