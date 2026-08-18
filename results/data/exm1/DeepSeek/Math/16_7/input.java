// buggy code
    public static double cosh(double x) {
      if (x != x) {
          return x;
      }

      // cosh[z] = (exp(z) + exp(-z))/2

      // for numbers with magnitude 20 or so,
      // exp(-z) can be ignored in comparison with exp(z)

      if (x > 20) {
              // Avoid overflow (MATH-905).
              return 0.5 * exp(x);
          }
      if (x < -20) {
              // Avoid overflow (MATH-905).
              return 0.5 * exp(-x);
      }

      final double hiPrec[] = new double[2];
      if (x < 0.0) {
          x = -x;
      }
      exp(x, 0.0, hiPrec);

      double ya = hiPrec[0] + hiPrec[1];
      double yb = -(ya - hiPrec[0] - hiPrec[1]);

      double temp = ya * HEX_40000000;
      double yaa = ya + temp - temp;
      double yab = ya - yaa;

      // recip = 1/y
      double recip = 1.0/ya;
      temp = recip * HEX_40000000;
      double recipa = recip + temp - temp;
      double recipb = recip - recipa;

      // Correct for rounding in division
      recipb += (1.0 - yaa*recipa - yaa*recipb - yab*recipa - yab*recipb) * recip;
      // Account for yb
      recipb += -yb * recip * recip;

      // y = y + 1/y
      temp = ya + recipa;
      yb += -(temp - ya - recipa);
      ya = temp;
      temp = ya + recipb;
      yb += -(temp - ya - recipb);
      ya = temp;

      double result = ya + yb;
      result *= 0.5;
      return result;
    }

    public static double sinh(double x) {
      boolean negate = false;
      if (x != x) {
          return x;
      }

      // sinh[z] = (exp(z) - exp(-z) / 2

      // for values of z larger than about 20,
      // exp(-z) can be ignored in comparison with exp(z)

      if (x > 20) {
              // Avoid overflow (MATH-905).
              return 0.5 * exp(x);
          }
      if (x < -20) {
              // Avoid overflow (MATH-905).
              return -0.5 * exp(-x);
      }

      if (x == 0) {
          return x;
      }

      if (x < 0.0) {
          x = -x;
          negate = true;
      }

      double result;

      if (x > 0.25) {
          double hiPrec[] = new double[2];
          exp(x, 0.0, hiPrec);

          double ya = hiPrec[0] + hiPrec[1];
          double yb = -(ya - hiPrec[0] - hiPrec[1]);

          double temp = ya * HEX_40000000;
          double yaa = ya + temp - temp;
          double yab = ya - yaa;

          // recip = 1/y
          double recip = 1.0/ya;
          temp = recip * HEX_40000000;
          double recipa = recip + temp - temp;
          double recipb = recip - recipa;

          // Correct for rounding in division
          recipb += (1.0 - yaa*recipa - yaa*recipb - yab*recipa - yab*recipb) * recip;
          // Account for yb
          recipb += -yb * recip * recip;

          recipa = -recipa;
          recipb = -recipb;

          // y = y + 1/y
          temp = ya + recipa;
          yb += -(temp - ya - recipa);
          ya = temp;
          temp = ya + recipb;
          yb += -(temp - ya - recipb);
          ya = temp;

          result = ya + yb;
          result *= 0.5;
      }
      else {
          double hiPrec[] = new double[2];
          expm1(x, hiPrec);

          double ya = hiPrec[0] + hiPrec[1];
          double yb = -(ya - hiPrec[0] - hiPrec[1]);

          /* Compute expm1(-x) = -expm1(x) / (expm1(x) + 1) */
          double denom = 1.0 + ya;
          double denomr = 1.0 / denom;
          double denomb = -(denom - 1.0 - ya) + yb;
          double ratio = ya * denomr;
          double temp = ratio * HEX_40000000;
          double ra = ratio + temp - temp;
          double rb = ratio - ra;

          temp = denom * HEX_40000000;
          double za = denom + temp - temp;
          double zb = denom - za;

          rb += (ya - za*ra - za*rb - zb*ra - zb*rb) * denomr;

          // Adjust for yb
          rb += yb*denomr;                        // numerator
          rb += -ya * denomb * denomr * denomr;   // denominator

          // y = y - 1/y
          temp = ya + ra;
          yb += -(temp - ya - ra);
          ya = temp;
          temp = ya + rb;
          yb += -(temp - ya - rb);
          ya = temp;

          result = ya + yb;
          result *= 0.5;
      }

      if (negate) {
          result = -result;
      }

      return result;
    }

// relevant test
// org.apache.commons.math3.util.MathArraysTest::testCheckOrder
    public void testCheckOrder() {
        MathArrays.checkOrder(new double[] {-15, -5.5, -1, 2, 15},
                             MathArrays.OrderDirection.INCREASING, true);
        MathArrays.checkOrder(new double[] {-15, -5.5, -1, 2, 2},
                             MathArrays.OrderDirection.INCREASING, false);
        MathArrays.checkOrder(new double[] {3, -5.5, -11, -27.5},
                             MathArrays.OrderDirection.DECREASING, true);
        MathArrays.checkOrder(new double[] {3, 0, 0, -5.5, -11, -27.5},
                             MathArrays.OrderDirection.DECREASING, false);

        try {
            MathArrays.checkOrder(new double[] {-15, -5.5, -1, -1, 2, 15},
                                 MathArrays.OrderDirection.INCREASING, true);
            Assert.fail("an exception should have been thrown");
        } catch (NonMonotonicSequenceException e) {
            
        }
        try {
            MathArrays.checkOrder(new double[] {-15, -5.5, -1, -2, 2},
                                 MathArrays.OrderDirection.INCREASING, false);
            Assert.fail("an exception should have been thrown");
        } catch (NonMonotonicSequenceException e) {
            
        }
        try {
            MathArrays.checkOrder(new double[] {3, 3, -5.5, -11, -27.5},
                                 MathArrays.OrderDirection.DECREASING, true);
            Assert.fail("an exception should have been thrown");
        } catch (NonMonotonicSequenceException e) {
            
        }
        try {
            MathArrays.checkOrder(new double[] {3, -1, 0, -5.5, -11, -27.5},
                                 MathArrays.OrderDirection.DECREASING, false);
            Assert.fail("an exception should have been thrown");
        } catch (NonMonotonicSequenceException e) {
            
        }
        try {
            MathArrays.checkOrder(new double[] {3, 0, -5.5, -11, -10},
                                 MathArrays.OrderDirection.DECREASING, false);
            Assert.fail("an exception should have been thrown");
        } catch (NonMonotonicSequenceException e) {
            
        }
    }

// org.apache.commons.math3.util.MathArraysTest::testIsMonotonic
    public void testIsMonotonic() {
        Assert.assertFalse(MathArrays.isMonotonic(new double[] { -15, -5.5, -1, -1, 2, 15 },
                                                  MathArrays.OrderDirection.INCREASING, true));
        Assert.assertTrue(MathArrays.isMonotonic(new double[] { -15, -5.5, -1, 0, 2, 15 },
                                                 MathArrays.OrderDirection.INCREASING, true));
        Assert.assertFalse(MathArrays.isMonotonic(new double[] { -15, -5.5, -1, -2, 2 },
                                                  MathArrays.OrderDirection.INCREASING, false));
        Assert.assertTrue(MathArrays.isMonotonic(new double[] { -15, -5.5, -1, -1, 2 },
                                                 MathArrays.OrderDirection.INCREASING, false));
        Assert.assertFalse(MathArrays.isMonotonic(new double[] { 3, 3, -5.5, -11, -27.5 },
                                                  MathArrays.OrderDirection.DECREASING, true));
        Assert.assertTrue(MathArrays.isMonotonic(new double[] { 3, 2, -5.5, -11, -27.5 },
                                                 MathArrays.OrderDirection.DECREASING, true));
        Assert.assertFalse(MathArrays.isMonotonic(new double[] { 3, -1, 0, -5.5, -11, -27.5 },
                                                  MathArrays.OrderDirection.DECREASING, false));
        Assert.assertTrue(MathArrays.isMonotonic(new double[] { 3, 0, 0, -5.5, -11, -27.5 },
                                                 MathArrays.OrderDirection.DECREASING, false));
    }

// org.apache.commons.math3.util.MathArraysTest::testIsMonotonicComparable
    public void testIsMonotonicComparable() {
        Assert.assertFalse(MathArrays.isMonotonic(new Double[] { new Double(-15),
                                                                 new Double(-5.5),
                                                                 new Double(-1),
                                                                 new Double(-1),
                                                                 new Double(2),
                                                                 new Double(15) },
                MathArrays.OrderDirection.INCREASING, true));
        Assert.assertTrue(MathArrays.isMonotonic(new Double[] { new Double(-15),
                                                                new Double(-5.5),
                                                                new Double(-1),
                                                                new Double(0),
                                                                new Double(2),
                                                                new Double(15) },
                MathArrays.OrderDirection.INCREASING, true));
        Assert.assertFalse(MathArrays.isMonotonic(new Double[] { new Double(-15),
                                                                 new Double(-5.5),
                                                                 new Double(-1),
                                                                 new Double(-2),
                                                                 new Double(2) },
                MathArrays.OrderDirection.INCREASING, false));
        Assert.assertTrue(MathArrays.isMonotonic(new Double[] { new Double(-15),
                                                                new Double(-5.5),
                                                                new Double(-1),
                                                                new Double(-1),
                                                                new Double(2) },
                MathArrays.OrderDirection.INCREASING, false));
        Assert.assertFalse(MathArrays.isMonotonic(new Double[] { new Double(3),
                                                                 new Double(3),
                                                                 new Double(-5.5),
                                                                 new Double(-11),
                                                                 new Double(-27.5) },
                MathArrays.OrderDirection.DECREASING, true));
        Assert.assertTrue(MathArrays.isMonotonic(new Double[] { new Double(3),
                                                                new Double(2),
                                                                new Double(-5.5),
                                                                new Double(-11),
                                                                new Double(-27.5) },
                MathArrays.OrderDirection.DECREASING, true));
        Assert.assertFalse(MathArrays.isMonotonic(new Double[] { new Double(3),
                                                                 new Double(-1),
                                                                 new Double(0),
                                                                 new Double(-5.5),
                                                                 new Double(-11),
                                                                 new Double(-27.5) },
                MathArrays.OrderDirection.DECREASING, false));
        Assert.assertTrue(MathArrays.isMonotonic(new Double[] { new Double(3),
                                                                new Double(0),
                                                                new Double(0),
                                                                new Double(-5.5),
                                                                new Double(-11),
                                                                new Double(-27.5) },
                MathArrays.OrderDirection.DECREASING, false));
    }

// org.apache.commons.math3.util.MathArraysTest::testCheckRectangular
    public void testCheckRectangular() {
        final long[][] rect = new long[][] {{0, 1}, {2, 3}};
        final long[][] ragged = new long[][] {{0, 1}, {2}};
        final long[][] nullArray = null;
        final long[][] empty = new long[][] {};
        MathArrays.checkRectangular(rect);
        MathArrays.checkRectangular(empty);
        try {
            MathArrays.checkRectangular(ragged);
            Assert.fail("Expecting DimensionMismatchException");
        } catch (DimensionMismatchException ex) {
            
        }
        try {
            MathArrays.checkRectangular(nullArray);
            Assert.fail("Expecting NullArgumentException");
        } catch (NullArgumentException ex) {
            
        } 
    }

// org.apache.commons.math3.util.MathArraysTest::testCheckPositive
    public void testCheckPositive() {
        final double[] positive = new double[] {1, 2, 3};
        final double[] nonNegative = new double[] {0, 1, 2};
        final double[] nullArray = null;
        final double[] empty = new double[] {};
        MathArrays.checkPositive(positive);
        MathArrays.checkPositive(empty);
        try {
            MathArrays.checkPositive(nullArray);
            Assert.fail("Expecting NullPointerException");
        } catch (NullPointerException ex) {
            
        }
        try {
            MathArrays.checkPositive(nonNegative);
            Assert.fail("Expecting NotStrictlyPositiveException");
        } catch (NotStrictlyPositiveException ex) {
            
        }
    }

// org.apache.commons.math3.util.MathArraysTest::testCheckNonNegative
    public void testCheckNonNegative() {
        final long[] nonNegative = new long[] {0, 1};
        final long[] hasNegative = new long[] {-1};
        final long[] nullArray = null;
        final long[] empty = new long[] {};
        MathArrays.checkNonNegative(nonNegative);
        MathArrays.checkNonNegative(empty);
        try {
            MathArrays.checkNonNegative(nullArray);
            Assert.fail("Expecting NullPointerException");
        } catch (NullPointerException ex) {
            
        }
        try {
            MathArrays.checkNonNegative(hasNegative);
            Assert.fail("Expecting NotPositiveException");
        } catch (NotPositiveException ex) {
            
        }
    }

// org.apache.commons.math3.util.MathArraysTest::testCheckNonNegative2D
    public void testCheckNonNegative2D() {
        final long[][] nonNegative = new long[][] {{0, 1}, {1, 0}};
        final long[][] hasNegative = new long[][] {{-1}, {0}};
        final long[][] nullArray = null;
        final long[][] empty = new long[][] {};
        MathArrays.checkNonNegative(nonNegative);
        MathArrays.checkNonNegative(empty);
        try {
            MathArrays.checkNonNegative(nullArray);
            Assert.fail("Expecting NullPointerException");
        } catch (NullPointerException ex) {
            
        }
        try {
            MathArrays.checkNonNegative(hasNegative);
            Assert.fail("Expecting NotPositiveException");
        } catch (NotPositiveException ex) {
            
        }
    }

// org.apache.commons.math3.util.MathArraysTest::testSortInPlace
    public void testSortInPlace() {
        final double[] x1 = {2,   5,  -3, 1,  4};
        final double[] x2 = {4,  25,   9, 1, 16};
        final double[] x3 = {8, 125, -27, 1, 64};

        MathArrays.sortInPlace(x1, x2, x3);

        Assert.assertEquals(-3,  x1[0], Math.ulp(1d));
        Assert.assertEquals(9,   x2[0], Math.ulp(1d));
        Assert.assertEquals(-27, x3[0], Math.ulp(1d));

        Assert.assertEquals(1, x1[1], Math.ulp(1d));
        Assert.assertEquals(1, x2[1], Math.ulp(1d));
        Assert.assertEquals(1, x3[1], Math.ulp(1d));

        Assert.assertEquals(2, x1[2], Math.ulp(1d));
        Assert.assertEquals(4, x2[2], Math.ulp(1d));
        Assert.assertEquals(8, x3[2], Math.ulp(1d));

        Assert.assertEquals(4,  x1[3], Math.ulp(1d));
        Assert.assertEquals(16, x2[3], Math.ulp(1d));
        Assert.assertEquals(64, x3[3], Math.ulp(1d));

        Assert.assertEquals(5,   x1[4], Math.ulp(1d));
        Assert.assertEquals(25,  x2[4], Math.ulp(1d));
        Assert.assertEquals(125, x3[4], Math.ulp(1d));
    }

// org.apache.commons.math3.util.MathArraysTest::testSortInPlaceExample
    public void testSortInPlaceExample() {
        final double[] x = {3, 1, 2};
        final double[] y = {1, 2, 3};
        final double[] z = {0, 5, 7};
        MathArrays.sortInPlace(x, y, z);
        final double[] sx = {1, 2, 3};
        final double[] sy = {2, 3, 1};
        final double[] sz = {5, 7, 0};
        Assert.assertTrue(Arrays.equals(sx, x));
        Assert.assertTrue(Arrays.equals(sy, y));
        Assert.assertTrue(Arrays.equals(sz, z));
    }

// org.apache.commons.math3.util.MathArraysTest::testSortInPlaceFailures
    public void testSortInPlaceFailures() {
        final double[] nullArray = null;
        final double[] one = {1};
        final double[] two = {1, 2};
        final double[] onep = {2};
        try {
            MathArrays.sortInPlace(one, two);
            Assert.fail("Expecting DimensionMismatchException");
        } catch (DimensionMismatchException ex) {
            
        }
        try {
            MathArrays.sortInPlace(one, nullArray);
            Assert.fail("Expecting NullArgumentException");
        } catch (NullArgumentException ex) {
            
        }
        try {
            MathArrays.sortInPlace(one, onep, nullArray);
            Assert.fail("Expecting NullArgumentException");
        } catch (NullArgumentException ex) {
            
        }
    }

// org.apache.commons.math3.util.MathArraysTest::testCopyOfInt
    public void testCopyOfInt() {
        final int[] source = { Integer.MIN_VALUE,
                               -1, 0, 1, 3, 113, 4769,
                               Integer.MAX_VALUE };
        final int[] dest = MathArrays.copyOf(source);

        Assert.assertEquals(dest.length, source.length);
        for (int i = 0; i < source.length; i++) {
            Assert.assertEquals(source[i], dest[i]);
        }
    }

// org.apache.commons.math3.util.MathArraysTest::testCopyOfInt2
    public void testCopyOfInt2() {
        final int[] source = { Integer.MIN_VALUE,
                               -1, 0, 1, 3, 113, 4769,
                               Integer.MAX_VALUE };
        final int offset = 3;
        final int[] dest = MathArrays.copyOf(source, source.length - offset);

        Assert.assertEquals(dest.length, source.length - offset);
        for (int i = 0; i < source.length - offset; i++) {
            Assert.assertEquals(source[i], dest[i]);
        }
    }

// org.apache.commons.math3.util.MathArraysTest::testCopyOfInt3
    public void testCopyOfInt3() {
        final int[] source = { Integer.MIN_VALUE,
                               -1, 0, 1, 3, 113, 4769,
                               Integer.MAX_VALUE };
        final int offset = 3;
        final int[] dest = MathArrays.copyOf(source, source.length + offset);

        Assert.assertEquals(dest.length, source.length + offset);
        for (int i = 0; i < source.length; i++) {
            Assert.assertEquals(source[i], dest[i]);
        }
        for (int i = source.length; i < source.length + offset; i++) {
            Assert.assertEquals(0, dest[i], 0);
        }
    }

// org.apache.commons.math3.util.MathArraysTest::testCopyOfDouble
    public void testCopyOfDouble() {
        final double[] source = { Double.NEGATIVE_INFINITY,
                                  -Double.MAX_VALUE,
                                  -1, 0,
                                  Double.MIN_VALUE,
                                  Math.ulp(1d),
                                  1, 3, 113, 4769,
                                  Double.MAX_VALUE,
                                  Double.POSITIVE_INFINITY };
        final double[] dest = MathArrays.copyOf(source);

        Assert.assertEquals(dest.length, source.length);
        for (int i = 0; i < source.length; i++) {
            Assert.assertEquals(source[i], dest[i], 0);
        }
    }

// org.apache.commons.math3.util.MathArraysTest::testCopyOfDouble2
    public void testCopyOfDouble2() {
        final double[] source = { Double.NEGATIVE_INFINITY,
                                  -Double.MAX_VALUE,
                                  -1, 0,
                                  Double.MIN_VALUE,
                                  Math.ulp(1d),
                                  1, 3, 113, 4769,
                                  Double.MAX_VALUE,
                                  Double.POSITIVE_INFINITY };
        final int offset = 3;
        final double[] dest = MathArrays.copyOf(source, source.length - offset);

        Assert.assertEquals(dest.length, source.length - offset);
        for (int i = 0; i < source.length - offset; i++) {
            Assert.assertEquals(source[i], dest[i], 0);
        }
    }

// org.apache.commons.math3.util.MathArraysTest::testCopyOfDouble3
    public void testCopyOfDouble3() {
        final double[] source = { Double.NEGATIVE_INFINITY,
                                  -Double.MAX_VALUE,
                                  -1, 0,
                                  Double.MIN_VALUE,
                                  Math.ulp(1d),
                                  1, 3, 113, 4769,
                                  Double.MAX_VALUE,
                                  Double.POSITIVE_INFINITY };
        final int offset = 3;
        final double[] dest = MathArrays.copyOf(source, source.length + offset);

        Assert.assertEquals(dest.length, source.length + offset);
        for (int i = 0; i < source.length; i++) {
            Assert.assertEquals(source[i], dest[i], 0);
        }
        for (int i = source.length; i < source.length + offset; i++) {
            Assert.assertEquals(0, dest[i], 0);
        }
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

// org.apache.commons.math3.util.MathUtilsTest::testHash
    public void testHash() {
        double[] testArray = {
            Double.NaN,
            Double.POSITIVE_INFINITY,
            Double.NEGATIVE_INFINITY,
            1d,
            0d,
            1E-14,
            (1 + 1E-14),
            Double.MIN_VALUE,
            Double.MAX_VALUE };
        for (int i = 0; i < testArray.length; i++) {
            for (int j = 0; j < testArray.length; j++) {
                if (i == j) {
                    Assert.assertEquals(MathUtils.hash(testArray[i]), MathUtils.hash(testArray[j]));
                    Assert.assertEquals(MathUtils.hash(testArray[j]), MathUtils.hash(testArray[i]));
                } else {
                    Assert.assertTrue(MathUtils.hash(testArray[i]) != MathUtils.hash(testArray[j]));
                    Assert.assertTrue(MathUtils.hash(testArray[j]) != MathUtils.hash(testArray[i]));
                }
            }
        }
    }

// org.apache.commons.math3.util.MathUtilsTest::testArrayHash
    public void testArrayHash() {
        Assert.assertEquals(0, MathUtils.hash((double[]) null));
        Assert.assertEquals(MathUtils.hash(new double[] {
                                      Double.NaN, Double.POSITIVE_INFINITY,
                                      Double.NEGATIVE_INFINITY, 1d, 0d
                                    }),
                     MathUtils.hash(new double[] {
                                      Double.NaN, Double.POSITIVE_INFINITY,
                                      Double.NEGATIVE_INFINITY, 1d, 0d
                                    }));
        Assert.assertFalse(MathUtils.hash(new double[] { 1d }) ==
                    MathUtils.hash(new double[] { FastMath.nextAfter(1d, 2d) }));
        Assert.assertFalse(MathUtils.hash(new double[] { 1d }) ==
                    MathUtils.hash(new double[] { 1d, 1d }));
    }

// org.apache.commons.math3.util.MathUtilsTest::testPermutedArrayHash
    public void testPermutedArrayHash() {
        double[] original = new double[10];
        double[] permuted = new double[10];
        RandomDataImpl random = new RandomDataImpl();

        
        for (int i = 0; i < 10; i++) {
            final RealDistribution u = new UniformRealDistribution(i + 0.5, i + 0.75);
            original[i] = u.sample();
        }

        
        boolean isIdentity = true;
        do {
            int[] permutation = random.nextPermutation(10, 10);
            for (int i = 0; i < 10; i++) {
                if (i != permutation[i]) {
                    isIdentity = false;
                }
                permuted[i] = original[permutation[i]];
            }
        } while (isIdentity);

        
        Assert.assertFalse(MathUtils.hash(original) == MathUtils.hash(permuted));
    }

// org.apache.commons.math3.util.MathUtilsTest::testIndicatorByte
    public void testIndicatorByte() {
        Assert.assertEquals((byte)1, MathUtils.copySign((byte)1, (byte)2));
        Assert.assertEquals((byte)1, MathUtils.copySign((byte)1, (byte)0));
        Assert.assertEquals((byte)(-1), MathUtils.copySign((byte)1, (byte)(-2)));
    }

// org.apache.commons.math3.util.MathUtilsTest::testIndicatorInt
    public void testIndicatorInt() {
        Assert.assertEquals(1, MathUtils.copySign(1, 2));
        Assert.assertEquals(1, MathUtils.copySign(1, 0));
        Assert.assertEquals((-1), MathUtils.copySign(1, -2));
    }

// org.apache.commons.math3.util.MathUtilsTest::testIndicatorLong
    public void testIndicatorLong() {
        Assert.assertEquals(1L, MathUtils.copySign(1L, 2L));
        Assert.assertEquals(1L, MathUtils.copySign(1L, 0L));
        Assert.assertEquals(-1L, MathUtils.copySign(1L, -2L));
    }

// org.apache.commons.math3.util.MathUtilsTest::testIndicatorShort
    public void testIndicatorShort() {
        Assert.assertEquals((short)1, MathUtils.copySign((short)1, (short)2));
        Assert.assertEquals((short)1, MathUtils.copySign((short)1, (short)0));
        Assert.assertEquals((short)(-1), MathUtils.copySign((short)1, (short)(-2)));
    }

// org.apache.commons.math3.util.MathUtilsTest::testNormalizeAngle
    public void testNormalizeAngle() {
        for (double a = -15.0; a <= 15.0; a += 0.1) {
            for (double b = -15.0; b <= 15.0; b += 0.2) {
                double c = MathUtils.normalizeAngle(a, b);
                Assert.assertTrue((b - FastMath.PI) <= c);
                Assert.assertTrue(c <= (b + FastMath.PI));
                double twoK = FastMath.rint((a - c) / FastMath.PI);
                Assert.assertEquals(c, a - twoK * FastMath.PI, 1.0e-14);
            }
        }
    }

// org.apache.commons.math3.util.MathUtilsTest::testReduce
    public void testReduce() {
        final double period = -12.222;
        final double offset = 13;

        final double delta = 1.5;

        double orig = offset + 122456789 * period + delta;
        double expected = delta;
        Assert.assertEquals(expected,
                            MathUtils.reduce(orig, period, offset),
                            1e-7);
        Assert.assertEquals(expected,
                            MathUtils.reduce(orig, -period, offset),
                            1e-7);

        orig = offset - 123356789 * period - delta;
        expected = Math.abs(period) - delta;
        Assert.assertEquals(expected,
                            MathUtils.reduce(orig, period, offset),
                            1e-6);
        Assert.assertEquals(expected,
                            MathUtils.reduce(orig, -period, offset),
                            1e-6);

        orig = offset - 123446789 * period + delta;
        expected = delta;
        Assert.assertEquals(expected,
                            MathUtils.reduce(orig, period, offset),
                            1e-6);
        Assert.assertEquals(expected,
                            MathUtils.reduce(orig, -period, offset),
                            1e-6);

        Assert.assertTrue(Double.isNaN(MathUtils.reduce(orig, Double.NaN, offset)));
        Assert.assertTrue(Double.isNaN(MathUtils.reduce(Double.NaN, period, offset)));
        Assert.assertTrue(Double.isNaN(MathUtils.reduce(orig, period, Double.NaN)));
        Assert.assertTrue(Double.isNaN(MathUtils.reduce(orig, period,
                Double.POSITIVE_INFINITY)));
        Assert.assertTrue(Double.isNaN(MathUtils.reduce(Double.POSITIVE_INFINITY,
                period, offset)));
        Assert.assertTrue(Double.isNaN(MathUtils.reduce(orig,
                Double.POSITIVE_INFINITY, offset)));
        Assert.assertTrue(Double.isNaN(MathUtils.reduce(orig,
                Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)));
        Assert.assertTrue(Double.isNaN(MathUtils.reduce(Double.POSITIVE_INFINITY,
                period, Double.POSITIVE_INFINITY)));
        Assert.assertTrue(Double.isNaN(MathUtils.reduce(Double.POSITIVE_INFINITY,
                Double.POSITIVE_INFINITY, offset)));
        Assert.assertTrue(Double.isNaN(MathUtils.reduce(Double.POSITIVE_INFINITY,
                Double.POSITIVE_INFINITY,  Double.POSITIVE_INFINITY)));
    }

// org.apache.commons.math3.util.MathUtilsTest::testReduceComparedWithNormalizeAngle
    public void testReduceComparedWithNormalizeAngle() {
        final double tol = Math.ulp(1d);
        final double period = 2 * Math.PI;
        for (double a = -15; a <= 15; a += 0.5) {
            for (double center = -15; center <= 15; center += 1) {
                final double nA = MathUtils.normalizeAngle(a, center);
                final double offset = center - Math.PI;
                final double r = MathUtils.reduce(a, period, offset);
                Assert.assertEquals(nA, r + offset, tol);
            }
        }
    }

// org.apache.commons.math3.util.MathUtilsTest::testSignByte
    public void testSignByte() {
        final byte one = (byte) 1;
        Assert.assertEquals((byte) 1, MathUtils.copySign(one, (byte) 2));
        Assert.assertEquals((byte) (-1), MathUtils.copySign(one, (byte) (-2)));
    }

// org.apache.commons.math3.util.MathUtilsTest::testSignInt
    public void testSignInt() {
        final int one = 1;
        Assert.assertEquals(1, MathUtils.copySign(one, 2));
        Assert.assertEquals((-1), MathUtils.copySign(one, -2));
    }

// org.apache.commons.math3.util.MathUtilsTest::testSignLong
    public void testSignLong() {
        final long one = 1L;
        Assert.assertEquals(1L, MathUtils.copySign(one, 2L));
        Assert.assertEquals(-1L, MathUtils.copySign(one, -2L));
    }

// org.apache.commons.math3.util.MathUtilsTest::testSignShort
    public void testSignShort() {
        final short one = (short) 1;
        Assert.assertEquals((short) 1, MathUtils.copySign(one, (short) 2));
        Assert.assertEquals((short) (-1), MathUtils.copySign(one, (short) (-2)));
    }

// org.apache.commons.math3.util.MathUtilsTest::testCheckFinite
    public void testCheckFinite() {
        try {
            MathUtils.checkFinite(Double.POSITIVE_INFINITY);
            Assert.fail("an exception should have been thrown");
        } catch (NotFiniteNumberException e) {
            
        }
        try {
            MathUtils.checkFinite(Double.NEGATIVE_INFINITY);
            Assert.fail("an exception should have been thrown");
        } catch (NotFiniteNumberException e) {
            
        }
        try {
            MathUtils.checkFinite(Double.NaN);
            Assert.fail("an exception should have been thrown");
        } catch (NotFiniteNumberException e) {
            
        }

        try {
            MathUtils.checkFinite(new double[] {0, -1, Double.POSITIVE_INFINITY, -2, 3});
            Assert.fail("an exception should have been thrown");
        } catch (NotFiniteNumberException e) {
            
        }
        try {
            MathUtils.checkFinite(new double[] {1, Double.NEGATIVE_INFINITY, -2, 3});
            Assert.fail("an exception should have been thrown");
        } catch (NotFiniteNumberException e) {
            
        }
        try {
            MathUtils.checkFinite(new double[] {4, 3, -1, Double.NaN, -2, 1});
            Assert.fail("an exception should have been thrown");
        } catch (NotFiniteNumberException e) {
            
        }
    }

// org.apache.commons.math3.util.MathUtilsTest::testCheckNotNull1
    public void testCheckNotNull1() {
        try {
            Object obj = null;
            MathUtils.checkNotNull(obj);
        } catch (NullArgumentException e) {
            
        }
    }

// org.apache.commons.math3.util.MathUtilsTest::testCheckNotNull2
    public void testCheckNotNull2() {
        try {
            double[] array = null;
            MathUtils.checkNotNull(array, LocalizedFormats.INPUT_ARRAY);
        } catch (NullArgumentException e) {
            
        }
    }

// org.apache.commons.math3.util.MathUtilsTest::testCopySignByte
    public void testCopySignByte() {
        byte a = MathUtils.copySign(Byte.MIN_VALUE, (byte) -1);
        Assert.assertEquals(Byte.MIN_VALUE, a);

        final byte minValuePlusOne = Byte.MIN_VALUE + (byte) 1;
        a = MathUtils.copySign(minValuePlusOne, (byte) 1);
        Assert.assertEquals(Byte.MAX_VALUE, a);

        a = MathUtils.copySign(Byte.MAX_VALUE, (byte) -1);
        Assert.assertEquals(minValuePlusOne, a);

        final byte one = 1;
        byte val = -2;
        a = MathUtils.copySign(val, one);
        Assert.assertEquals(-val, a);

        final byte minusOne = -one;
        val = 2;
        a = MathUtils.copySign(val, minusOne);
        Assert.assertEquals(-val, a);

        val = 0;
        a = MathUtils.copySign(val, minusOne);
        Assert.assertEquals(val, a);

        val = 0;
        a = MathUtils.copySign(val, one);
        Assert.assertEquals(val, a);
    }

// org.apache.commons.math3.util.MathUtilsTest::testCopySignByte2
    public void testCopySignByte2() {
        MathUtils.copySign(Byte.MIN_VALUE, (byte) 1);
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

// org.apache.commons.math3.util.OpenIntToDoubleHashMapTest::testPutAndGetWith0ExpectedSize
    public void testPutAndGetWith0ExpectedSize() {
        OpenIntToDoubleHashMap map = new OpenIntToDoubleHashMap(0);
        assertPutAndGet(map);
    }

// org.apache.commons.math3.util.OpenIntToDoubleHashMapTest::testPutAndGetWithExpectedSize
    public void testPutAndGetWithExpectedSize() {
        OpenIntToDoubleHashMap map = new OpenIntToDoubleHashMap(500);
        assertPutAndGet(map);
    }

// org.apache.commons.math3.util.OpenIntToDoubleHashMapTest::testPutAndGet
    public void testPutAndGet() {
        OpenIntToDoubleHashMap map = new OpenIntToDoubleHashMap();
        assertPutAndGet(map);
    }

// org.apache.commons.math3.util.OpenIntToDoubleHashMapTest::testPutAbsentOnExisting
    public void testPutAbsentOnExisting() {
        OpenIntToDoubleHashMap map = createFromJavaMap();
        int size = javaMap.size();
        for (Map.Entry<Integer, Double> mapEntry : generateAbsent().entrySet()) {
            map.put(mapEntry.getKey(), mapEntry.getValue());
            Assert.assertEquals(++size, map.size());
            Assert.assertTrue(Precision.equals(mapEntry.getValue(), map.get(mapEntry.getKey()), 1));
        }
    }

// org.apache.commons.math3.util.OpenIntToDoubleHashMapTest::testPutOnExisting
    public void testPutOnExisting() {
        OpenIntToDoubleHashMap map = createFromJavaMap();
        for (Map.Entry<Integer, Double> mapEntry : javaMap.entrySet()) {
            map.put(mapEntry.getKey(), mapEntry.getValue());
            Assert.assertEquals(javaMap.size(), map.size());
            Assert.assertTrue(Precision.equals(mapEntry.getValue(), map.get(mapEntry.getKey()), 1));
        }
    }

// org.apache.commons.math3.util.OpenIntToDoubleHashMapTest::testGetAbsent
    public void testGetAbsent() {
        Map<Integer, Double> generated = generateAbsent();
        OpenIntToDoubleHashMap map = createFromJavaMap();

        for (Map.Entry<Integer, Double> mapEntry : generated.entrySet())
            Assert.assertTrue(Double.isNaN(map.get(mapEntry.getKey())));
    }

// org.apache.commons.math3.util.OpenIntToDoubleHashMapTest::testGetFromEmpty
    public void testGetFromEmpty() {
        OpenIntToDoubleHashMap map = new OpenIntToDoubleHashMap();
        Assert.assertTrue(Double.isNaN(map.get(5)));
        Assert.assertTrue(Double.isNaN(map.get(0)));
        Assert.assertTrue(Double.isNaN(map.get(50)));
    }

// org.apache.commons.math3.util.OpenIntToDoubleHashMapTest::testRemove
    public void testRemove() {
        OpenIntToDoubleHashMap map = createFromJavaMap();
        int mapSize = javaMap.size();
        Assert.assertEquals(mapSize, map.size());
        for (Map.Entry<Integer, Double> mapEntry : javaMap.entrySet()) {
            map.remove(mapEntry.getKey());
            Assert.assertEquals(--mapSize, map.size());
            Assert.assertTrue(Double.isNaN(map.get(mapEntry.getKey())));
        }

        
        assertPutAndGet(map);
    }

// org.apache.commons.math3.util.OpenIntToDoubleHashMapTest::testRemove2
    public void testRemove2() {
        OpenIntToDoubleHashMap map = createFromJavaMap();
        int mapSize = javaMap.size();
        int count = 0;
        Set<Integer> keysInMap = new HashSet<Integer>(javaMap.keySet());
        for (Map.Entry<Integer, Double> mapEntry : javaMap.entrySet()) {
            keysInMap.remove(mapEntry.getKey());
            map.remove(mapEntry.getKey());
            Assert.assertEquals(--mapSize, map.size());
            Assert.assertTrue(Double.isNaN(map.get(mapEntry.getKey())));
            if (count++ > 5)
                break;
        }

        
        assertPutAndGet(map, mapSize, keysInMap);
    }

// org.apache.commons.math3.util.OpenIntToDoubleHashMapTest::testRemoveFromEmpty
    public void testRemoveFromEmpty() {
        OpenIntToDoubleHashMap map = new OpenIntToDoubleHashMap();
        Assert.assertTrue(Double.isNaN(map.remove(50)));
    }

// org.apache.commons.math3.util.OpenIntToDoubleHashMapTest::testRemoveAbsent
    public void testRemoveAbsent() {
        Map<Integer, Double> generated = generateAbsent();

        OpenIntToDoubleHashMap map = createFromJavaMap();
        int mapSize = map.size();

        for (Map.Entry<Integer, Double> mapEntry : generated.entrySet()) {
            map.remove(mapEntry.getKey());
            Assert.assertEquals(mapSize, map.size());
            Assert.assertTrue(Double.isNaN(map.get(mapEntry.getKey())));
        }
    }

// org.apache.commons.math3.util.OpenIntToDoubleHashMapTest::testCopy
    public void testCopy() {
        OpenIntToDoubleHashMap copy =
            new OpenIntToDoubleHashMap(createFromJavaMap());
        Assert.assertEquals(javaMap.size(), copy.size());

        for (Map.Entry<Integer, Double> mapEntry : javaMap.entrySet())
            Assert.assertTrue(Precision.equals(mapEntry.getValue(), copy.get(mapEntry.getKey()), 1));
    }

// org.apache.commons.math3.util.OpenIntToDoubleHashMapTest::testContainsKey
    public void testContainsKey() {
        OpenIntToDoubleHashMap map = createFromJavaMap();
        for (Map.Entry<Integer, Double> mapEntry : javaMap.entrySet()) {
            Assert.assertTrue(map.containsKey(mapEntry.getKey()));
        }
        for (Map.Entry<Integer, Double> mapEntry : generateAbsent().entrySet()) {
            Assert.assertFalse(map.containsKey(mapEntry.getKey()));
        }
        for (Map.Entry<Integer, Double> mapEntry : javaMap.entrySet()) {
            int key = mapEntry.getKey();
            Assert.assertTrue(map.containsKey(key));
            map.remove(key);
            Assert.assertFalse(map.containsKey(key));
        }
    }

// org.apache.commons.math3.util.OpenIntToDoubleHashMapTest::testIterator
    public void testIterator() {
        OpenIntToDoubleHashMap map = createFromJavaMap();
        OpenIntToDoubleHashMap.Iterator iterator = map.iterator();
        for (int i = 0; i < map.size(); ++i) {
            Assert.assertTrue(iterator.hasNext());
            iterator.advance();
            int key = iterator.key();
            Assert.assertTrue(map.containsKey(key));
            Assert.assertEquals(javaMap.get(key), map.get(key), 0);
            Assert.assertEquals(javaMap.get(key), iterator.value(), 0);
            Assert.assertTrue(javaMap.containsKey(key));
        }
        Assert.assertFalse(iterator.hasNext());
        try {
            iterator.advance();
            Assert.fail("an exception should have been thrown");
        } catch (NoSuchElementException nsee) {
            
        }
    }

// org.apache.commons.math3.util.OpenIntToDoubleHashMapTest::testConcurrentModification
    public void testConcurrentModification() {
        OpenIntToDoubleHashMap map = createFromJavaMap();
        OpenIntToDoubleHashMap.Iterator iterator = map.iterator();
        map.put(3, 3);
        try {
            iterator.advance();
            Assert.fail("an exception should have been thrown");
        } catch (ConcurrentModificationException cme) {
            
        }
    }

// org.apache.commons.math3.util.OpenIntToDoubleHashMapTest::testPutKeysWithCollisions
    public void testPutKeysWithCollisions() {
        OpenIntToDoubleHashMap map = new OpenIntToDoubleHashMap();
        int key1 = -1996012590;
        double value1 = 1.0;
        map.put(key1, value1);
        int key2 = 835099822;
        map.put(key2, value1);
        int key3 = 1008859686;
        map.put(key3, value1);
        Assert.assertTrue(Precision.equals(value1, map.get(key3), 1));
        Assert.assertEquals(3, map.size());

        map.remove(key2);
        double value2 = 2.0;
        map.put(key3, value2);
        Assert.assertTrue(Precision.equals(value2, map.get(key3), 1));
        Assert.assertEquals(2, map.size());
    }

// org.apache.commons.math3.util.OpenIntToDoubleHashMapTest::testPutKeysWithCollision2
    public void testPutKeysWithCollision2() {
        OpenIntToDoubleHashMap map = new OpenIntToDoubleHashMap();
        int key1 = 837989881;
        double value1 = 1.0;
        map.put(key1, value1);
        int key2 = 476463321;
        map.put(key2, value1);
        Assert.assertEquals(2, map.size());
        Assert.assertTrue(Precision.equals(value1, map.get(key2), 1));

        map.remove(key1);
        double value2 = 2.0;
        map.put(key2, value2);
        Assert.assertEquals(1, map.size());
        Assert.assertTrue(Precision.equals(value2, map.get(key2), 1));
    }

// org.apache.commons.math3.util.OpenIntToFieldTest::testPutAndGetWith0ExpectedSize
    public void testPutAndGetWith0ExpectedSize() {
        OpenIntToFieldHashMap<Fraction> map = new OpenIntToFieldHashMap<Fraction>(field,0);
        assertPutAndGet(map);
    }

// org.apache.commons.math3.util.OpenIntToFieldTest::testPutAndGetWithExpectedSize
    public void testPutAndGetWithExpectedSize() {
        OpenIntToFieldHashMap<Fraction> map = new OpenIntToFieldHashMap<Fraction>(field,500);
        assertPutAndGet(map);
    }

// org.apache.commons.math3.util.OpenIntToFieldTest::testPutAndGet
    public void testPutAndGet() {
        OpenIntToFieldHashMap<Fraction> map = new OpenIntToFieldHashMap<Fraction>(field);
        assertPutAndGet(map);
    }

// org.apache.commons.math3.util.OpenIntToFieldTest::testPutAbsentOnExisting
    public void testPutAbsentOnExisting() {
        OpenIntToFieldHashMap<Fraction> map = createFromJavaMap(field);
        int size = javaMap.size();
        for (Map.Entry<Integer, Fraction> mapEntry : generateAbsent().entrySet()) {
            map.put(mapEntry.getKey(), mapEntry.getValue());
            Assert.assertEquals(++size, map.size());
            Assert.assertEquals(mapEntry.getValue(), map.get(mapEntry.getKey()));
        }
    }

// org.apache.commons.math3.util.OpenIntToFieldTest::testPutOnExisting
    public void testPutOnExisting() {
        OpenIntToFieldHashMap<Fraction> map = createFromJavaMap(field);
        for (Map.Entry<Integer, Fraction> mapEntry : javaMap.entrySet()) {
            map.put(mapEntry.getKey(), mapEntry.getValue());
            Assert.assertEquals(javaMap.size(), map.size());
            Assert.assertEquals(mapEntry.getValue(), map.get(mapEntry.getKey()));
        }
    }

// org.apache.commons.math3.util.OpenIntToFieldTest::testGetAbsent
    public void testGetAbsent() {
        Map<Integer, Fraction> generated = generateAbsent();
        OpenIntToFieldHashMap<Fraction> map = createFromJavaMap(field);

        for (Map.Entry<Integer, Fraction> mapEntry : generated.entrySet())
            Assert.assertTrue(field.getZero().equals(map.get(mapEntry.getKey())));
    }

// org.apache.commons.math3.util.OpenIntToFieldTest::testGetFromEmpty
    public void testGetFromEmpty() {
        OpenIntToFieldHashMap<Fraction> map = new OpenIntToFieldHashMap<Fraction>(field);
        Assert.assertTrue(field.getZero().equals(map.get(5)));
        Assert.assertTrue(field.getZero().equals(map.get(0)));
        Assert.assertTrue(field.getZero().equals(map.get(50)));
    }

// org.apache.commons.math3.util.OpenIntToFieldTest::testRemove
    public void testRemove() {
        OpenIntToFieldHashMap<Fraction> map = createFromJavaMap(field);
        int mapSize = javaMap.size();
        Assert.assertEquals(mapSize, map.size());
        for (Map.Entry<Integer, Fraction> mapEntry : javaMap.entrySet()) {
            map.remove(mapEntry.getKey());
            Assert.assertEquals(--mapSize, map.size());
            Assert.assertTrue(field.getZero().equals(map.get(mapEntry.getKey())));
        }

        
        assertPutAndGet(map);
    }

// org.apache.commons.math3.util.OpenIntToFieldTest::testRemove2
    public void testRemove2() {
        OpenIntToFieldHashMap<Fraction> map = createFromJavaMap(field);
        int mapSize = javaMap.size();
        int count = 0;
        Set<Integer> keysInMap = new HashSet<Integer>(javaMap.keySet());
        for (Map.Entry<Integer, Fraction> mapEntry : javaMap.entrySet()) {
            keysInMap.remove(mapEntry.getKey());
            map.remove(mapEntry.getKey());
            Assert.assertEquals(--mapSize, map.size());
            Assert.assertTrue(field.getZero().equals(map.get(mapEntry.getKey())));
            if (count++ > 5)
                break;
        }

        
        assertPutAndGet(map, mapSize, keysInMap);
    }

// org.apache.commons.math3.util.OpenIntToFieldTest::testRemoveFromEmpty
    public void testRemoveFromEmpty() {
        OpenIntToFieldHashMap<Fraction> map = new OpenIntToFieldHashMap<Fraction>(field);
        Assert.assertTrue(field.getZero().equals(map.remove(50)));
    }

// org.apache.commons.math3.util.OpenIntToFieldTest::testRemoveAbsent
    public void testRemoveAbsent() {
        Map<Integer, Fraction> generated = generateAbsent();

        OpenIntToFieldHashMap<Fraction> map = createFromJavaMap(field);
        int mapSize = map.size();

        for (Map.Entry<Integer, Fraction> mapEntry : generated.entrySet()) {
            map.remove(mapEntry.getKey());
            Assert.assertEquals(mapSize, map.size());
            Assert.assertTrue(field.getZero().equals(map.get(mapEntry.getKey())));
        }
    }

// org.apache.commons.math3.util.OpenIntToFieldTest::testCopy
    public void testCopy() {
        OpenIntToFieldHashMap<Fraction> copy =
            new OpenIntToFieldHashMap<Fraction>(createFromJavaMap(field));
        Assert.assertEquals(javaMap.size(), copy.size());

        for (Map.Entry<Integer, Fraction> mapEntry : javaMap.entrySet())
            Assert.assertEquals(mapEntry.getValue(), copy.get(mapEntry.getKey()));
    }

// org.apache.commons.math3.util.OpenIntToFieldTest::testContainsKey
    public void testContainsKey() {
        OpenIntToFieldHashMap<Fraction> map = createFromJavaMap(field);
        for (Entry<Integer, Fraction> mapEntry : javaMap.entrySet()) {
            Assert.assertTrue(map.containsKey(mapEntry.getKey()));
        }
        for (Map.Entry<Integer, Fraction> mapEntry : generateAbsent().entrySet()) {
            Assert.assertFalse(map.containsKey(mapEntry.getKey()));
        }
        for (Entry<Integer, Fraction> mapEntry : javaMap.entrySet()) {
            int key = mapEntry.getKey();
            Assert.assertTrue(map.containsKey(key));
            map.remove(key);
            Assert.assertFalse(map.containsKey(key));
        }
    }

// org.apache.commons.math3.util.OpenIntToFieldTest::testIterator
    public void testIterator() {
        OpenIntToFieldHashMap<Fraction> map = createFromJavaMap(field);
        OpenIntToFieldHashMap<Fraction>.Iterator iterator = map.iterator();
        for (int i = 0; i < map.size(); ++i) {
            Assert.assertTrue(iterator.hasNext());
            iterator.advance();
            int key = iterator.key();
            Assert.assertTrue(map.containsKey(key));
            Assert.assertEquals(javaMap.get(key), map.get(key));
            Assert.assertEquals(javaMap.get(key), iterator.value());
            Assert.assertTrue(javaMap.containsKey(key));
        }
        Assert.assertFalse(iterator.hasNext());
        try {
            iterator.advance();
            Assert.fail("an exception should have been thrown");
        } catch (NoSuchElementException nsee) {
            
        }
    }

// org.apache.commons.math3.util.OpenIntToFieldTest::testConcurrentModification
    public void testConcurrentModification() {
        OpenIntToFieldHashMap<Fraction> map = createFromJavaMap(field);
        OpenIntToFieldHashMap<Fraction>.Iterator iterator = map.iterator();
        map.put(3, new Fraction(3));
        try {
            iterator.advance();
            Assert.fail("an exception should have been thrown");
        } catch (ConcurrentModificationException cme) {
            
        }
    }

// org.apache.commons.math3.util.OpenIntToFieldTest::testPutKeysWithCollisions
    public void testPutKeysWithCollisions() {
        OpenIntToFieldHashMap<Fraction> map = new OpenIntToFieldHashMap<Fraction>(field);
        int key1 = -1996012590;
        Fraction value1 = new Fraction(1);
        map.put(key1, value1);
        int key2 = 835099822;
        map.put(key2, value1);
        int key3 = 1008859686;
        map.put(key3, value1);
        Assert.assertEquals(value1, map.get(key3));
        Assert.assertEquals(3, map.size());

        map.remove(key2);
        Fraction value2 = new Fraction(2);
        map.put(key3, value2);
        Assert.assertEquals(value2, map.get(key3));
        Assert.assertEquals(2, map.size());
    }

// org.apache.commons.math3.util.OpenIntToFieldTest::testPutKeysWithCollision2
    public void testPutKeysWithCollision2() {
        OpenIntToFieldHashMap<Fraction>map = new OpenIntToFieldHashMap<Fraction>(field);
        int key1 = 837989881;
        Fraction value1 = new Fraction(1);
        map.put(key1, value1);
        int key2 = 476463321;
        map.put(key2, value1);
        Assert.assertEquals(2, map.size());
        Assert.assertEquals(value1, map.get(key2));

        map.remove(key1);
        Fraction value2 = new Fraction(2);
        map.put(key2, value2);
        Assert.assertEquals(1, map.size());
        Assert.assertEquals(value2, map.get(key2));
    }

// org.apache.commons.math3.util.PrecisionTest::testEqualsWithRelativeTolerance
    public void testEqualsWithRelativeTolerance() {
        Assert.assertTrue(Precision.equalsWithRelativeTolerance(0d, 0d, 0d));
        Assert.assertTrue(Precision.equalsWithRelativeTolerance(0d, 1 / Double.NEGATIVE_INFINITY, 0d));

        final double eps = 1e-14;
        Assert.assertFalse(Precision.equalsWithRelativeTolerance(1.987654687654968, 1.987654687654988, eps));
        Assert.assertTrue(Precision.equalsWithRelativeTolerance(1.987654687654968, 1.987654687654987, eps));
        Assert.assertFalse(Precision.equalsWithRelativeTolerance(1.987654687654968, 1.987654687654948, eps));
        Assert.assertTrue(Precision.equalsWithRelativeTolerance(1.987654687654968, 1.987654687654949, eps));

        Assert.assertFalse(Precision.equalsWithRelativeTolerance(Precision.SAFE_MIN, 0.0, eps));

        Assert.assertFalse(Precision.equalsWithRelativeTolerance(1.0000000000001e-300, 1e-300, eps));
        Assert.assertTrue(Precision.equalsWithRelativeTolerance(1.00000000000001e-300, 1e-300, eps));

        Assert.assertFalse(Precision.equalsWithRelativeTolerance(Double.NEGATIVE_INFINITY, 1.23, eps));
        Assert.assertFalse(Precision.equalsWithRelativeTolerance(Double.POSITIVE_INFINITY, 1.23, eps));

        Assert.assertTrue(Precision.equalsWithRelativeTolerance(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, eps));
        Assert.assertTrue(Precision.equalsWithRelativeTolerance(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, eps));
        Assert.assertFalse(Precision.equalsWithRelativeTolerance(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, eps));

        Assert.assertFalse(Precision.equalsWithRelativeTolerance(Double.NaN, 1.23, eps));
        Assert.assertFalse(Precision.equalsWithRelativeTolerance(Double.NaN, Double.NaN, eps));
    }

// org.apache.commons.math3.util.PrecisionTest::testEqualsIncludingNaN
    public void testEqualsIncludingNaN() {
        double[] testArray = {
            Double.NaN,
            Double.POSITIVE_INFINITY,
            Double.NEGATIVE_INFINITY,
            1d,
            0d };
        for (int i = 0; i < testArray.length; i++) {
            for (int j = 0; j < testArray.length; j++) {
                if (i == j) {
                    Assert.assertTrue(Precision.equalsIncludingNaN(testArray[i], testArray[j]));
                    Assert.assertTrue(Precision.equalsIncludingNaN(testArray[j], testArray[i]));
                } else {
                    Assert.assertTrue(!Precision.equalsIncludingNaN(testArray[i], testArray[j]));
                    Assert.assertTrue(!Precision.equalsIncludingNaN(testArray[j], testArray[i]));
                }
            }
        }
    }

// org.apache.commons.math3.util.PrecisionTest::testEqualsWithAllowedDelta
    public void testEqualsWithAllowedDelta() {
        Assert.assertTrue(Precision.equals(153.0000, 153.0000, .0625));
        Assert.assertTrue(Precision.equals(153.0000, 153.0625, .0625));
        Assert.assertTrue(Precision.equals(152.9375, 153.0000, .0625));
        Assert.assertFalse(Precision.equals(153.0000, 153.0625, .0624));
        Assert.assertFalse(Precision.equals(152.9374, 153.0000, .0625));
        Assert.assertFalse(Precision.equals(Double.NaN, Double.NaN, 1.0));
        Assert.assertTrue(Precision.equals(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, 1.0));
        Assert.assertTrue(Precision.equals(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, 1.0));
        Assert.assertFalse(Precision.equals(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 1.0));
    }

// org.apache.commons.math3.util.PrecisionTest::testMath475
    public void testMath475() {
        final double a = 1.7976931348623182E16;
        final double b = FastMath.nextUp(a);

        double diff = FastMath.abs(a - b);
        
        
        
        Assert.assertTrue(Precision.equals(a, b, 0.5 * diff));

        final double c = FastMath.nextUp(b);
        diff = FastMath.abs(a - c);
        
        
        Assert.assertTrue(Precision.equals(a, c, diff));
        Assert.assertFalse(Precision.equals(a, c, (1 - 1e-16) * diff));
    }

// org.apache.commons.math3.util.PrecisionTest::testEqualsIncludingNaNWithAllowedDelta
    public void testEqualsIncludingNaNWithAllowedDelta() {
        Assert.assertTrue(Precision.equalsIncludingNaN(153.0000, 153.0000, .0625));
        Assert.assertTrue(Precision.equalsIncludingNaN(153.0000, 153.0625, .0625));
        Assert.assertTrue(Precision.equalsIncludingNaN(152.9375, 153.0000, .0625));
        Assert.assertTrue(Precision.equalsIncludingNaN(Double.NaN, Double.NaN, 1.0));
        Assert.assertTrue(Precision.equalsIncludingNaN(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, 1.0));
        Assert.assertTrue(Precision.equalsIncludingNaN(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, 1.0));
        Assert.assertFalse(Precision.equalsIncludingNaN(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 1.0));
        Assert.assertFalse(Precision.equalsIncludingNaN(153.0000, 153.0625, .0624));
        Assert.assertFalse(Precision.equalsIncludingNaN(152.9374, 153.0000, .0625));
    }

// org.apache.commons.math3.util.PrecisionTest::testFloatEqualsWithAllowedUlps
    public void testFloatEqualsWithAllowedUlps() {
        Assert.assertTrue("+0.0f == -0.0f",Precision.equals(0.0f, -0.0f));
        Assert.assertTrue("+0.0f == -0.0f (1 ulp)",Precision.equals(0.0f, -0.0f, 1));
        float oneFloat = 1.0f;
        Assert.assertTrue("1.0f == 1.0f + 1 ulp",Precision.equals(oneFloat, Float.intBitsToFloat(1 + Float.floatToIntBits(oneFloat))));
        Assert.assertTrue("1.0f == 1.0f + 1 ulp (1 ulp)",Precision.equals(oneFloat, Float.intBitsToFloat(1 + Float.floatToIntBits(oneFloat)), 1));
        Assert.assertFalse("1.0f != 1.0f + 2 ulp (1 ulp)",Precision.equals(oneFloat, Float.intBitsToFloat(2 + Float.floatToIntBits(oneFloat)), 1));

        Assert.assertTrue(Precision.equals(153.0f, 153.0f, 1));

        

        Assert.assertTrue(Precision.equals(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, 1));
        Assert.assertTrue(Precision.equals(Double.MAX_VALUE, Float.POSITIVE_INFINITY, 1));

        Assert.assertTrue(Precision.equals(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY, 1));
        Assert.assertTrue(Precision.equals(-Float.MAX_VALUE, Float.NEGATIVE_INFINITY, 1));

        Assert.assertFalse(Precision.equals(Float.NaN, Float.NaN, 1));
        Assert.assertFalse(Precision.equals(Float.NaN, Float.NaN, 0));
        Assert.assertFalse(Precision.equals(Float.NaN, 0, 0));
        Assert.assertFalse(Precision.equals(Float.NaN, Float.POSITIVE_INFINITY, 0));
        Assert.assertFalse(Precision.equals(Float.NaN, Float.NEGATIVE_INFINITY, 0));

        Assert.assertFalse(Precision.equals(Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY, 100000));
    }

// org.apache.commons.math3.util.PrecisionTest::testEqualsWithAllowedUlps
    public void testEqualsWithAllowedUlps() {
        Assert.assertTrue(Precision.equals(0.0, -0.0, 1));

        Assert.assertTrue(Precision.equals(1.0, 1 + FastMath.ulp(1d), 1));
        Assert.assertFalse(Precision.equals(1.0, 1 + 2 * FastMath.ulp(1d), 1));

        final double nUp1 = FastMath.nextAfter(1d, Double.POSITIVE_INFINITY);
        final double nnUp1 = FastMath.nextAfter(nUp1, Double.POSITIVE_INFINITY);
        Assert.assertTrue(Precision.equals(1.0, nUp1, 1));
        Assert.assertTrue(Precision.equals(nUp1, nnUp1, 1));
        Assert.assertFalse(Precision.equals(1.0, nnUp1, 1));

        Assert.assertTrue(Precision.equals(0.0, FastMath.ulp(0d), 1));
        Assert.assertTrue(Precision.equals(0.0, -FastMath.ulp(0d), 1));

        Assert.assertTrue(Precision.equals(153.0, 153.0, 1));

        Assert.assertTrue(Precision.equals(153.0, 153.00000000000003, 1));
        Assert.assertFalse(Precision.equals(153.0, 153.00000000000006, 1));
        Assert.assertTrue(Precision.equals(153.0, 152.99999999999997, 1));
        Assert.assertFalse(Precision.equals(153, 152.99999999999994, 1));

        Assert.assertTrue(Precision.equals(-128.0, -127.99999999999999, 1));
        Assert.assertFalse(Precision.equals(-128.0, -127.99999999999997, 1));
        Assert.assertTrue(Precision.equals(-128.0, -128.00000000000003, 1));
        Assert.assertFalse(Precision.equals(-128.0, -128.00000000000006, 1));

        Assert.assertTrue(Precision.equals(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, 1));
        Assert.assertTrue(Precision.equals(Double.MAX_VALUE, Double.POSITIVE_INFINITY, 1));

        Assert.assertTrue(Precision.equals(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, 1));
        Assert.assertTrue(Precision.equals(-Double.MAX_VALUE, Double.NEGATIVE_INFINITY, 1));

        Assert.assertFalse(Precision.equals(Double.NaN, Double.NaN, 1));
        Assert.assertFalse(Precision.equals(Double.NaN, Double.NaN, 0));
        Assert.assertFalse(Precision.equals(Double.NaN, 0, 0));
        Assert.assertFalse(Precision.equals(Double.NaN, Double.POSITIVE_INFINITY, 0));
        Assert.assertFalse(Precision.equals(Double.NaN, Double.NEGATIVE_INFINITY, 0));

        Assert.assertFalse(Precision.equals(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 100000));
    }

// org.apache.commons.math3.util.PrecisionTest::testEqualsIncludingNaNWithAllowedUlps
    public void testEqualsIncludingNaNWithAllowedUlps() {
        Assert.assertTrue(Precision.equalsIncludingNaN(0.0, -0.0, 1));

        Assert.assertTrue(Precision.equalsIncludingNaN(1.0, 1 + FastMath.ulp(1d), 1));
        Assert.assertFalse(Precision.equalsIncludingNaN(1.0, 1 + 2 * FastMath.ulp(1d), 1));

        final double nUp1 = FastMath.nextAfter(1d, Double.POSITIVE_INFINITY);
        final double nnUp1 = FastMath.nextAfter(nUp1, Double.POSITIVE_INFINITY);
        Assert.assertTrue(Precision.equalsIncludingNaN(1.0, nUp1, 1));
        Assert.assertTrue(Precision.equalsIncludingNaN(nUp1, nnUp1, 1));
        Assert.assertFalse(Precision.equalsIncludingNaN(1.0, nnUp1, 1));

        Assert.assertTrue(Precision.equalsIncludingNaN(0.0, FastMath.ulp(0d), 1));
        Assert.assertTrue(Precision.equalsIncludingNaN(0.0, -FastMath.ulp(0d), 1));

        Assert.assertTrue(Precision.equalsIncludingNaN(153.0, 153.0, 1));

        Assert.assertTrue(Precision.equalsIncludingNaN(153.0, 153.00000000000003, 1));
        Assert.assertFalse(Precision.equalsIncludingNaN(153.0, 153.00000000000006, 1));
        Assert.assertTrue(Precision.equalsIncludingNaN(153.0, 152.99999999999997, 1));
        Assert.assertFalse(Precision.equalsIncludingNaN(153, 152.99999999999994, 1));

        Assert.assertTrue(Precision.equalsIncludingNaN(-128.0, -127.99999999999999, 1));
        Assert.assertFalse(Precision.equalsIncludingNaN(-128.0, -127.99999999999997, 1));
        Assert.assertTrue(Precision.equalsIncludingNaN(-128.0, -128.00000000000003, 1));
        Assert.assertFalse(Precision.equalsIncludingNaN(-128.0, -128.00000000000006, 1));

        Assert.assertTrue(Precision.equalsIncludingNaN(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, 1));
        Assert.assertTrue(Precision.equalsIncludingNaN(Double.MAX_VALUE, Double.POSITIVE_INFINITY, 1));

        Assert.assertTrue(Precision.equalsIncludingNaN(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, 1));
        Assert.assertTrue(Precision.equalsIncludingNaN(-Double.MAX_VALUE, Double.NEGATIVE_INFINITY, 1));

        Assert.assertTrue(Precision.equalsIncludingNaN(Double.NaN, Double.NaN, 1));

        Assert.assertFalse(Precision.equalsIncludingNaN(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 100000));
    }

// org.apache.commons.math3.util.PrecisionTest::testCompareToEpsilon
    public void testCompareToEpsilon() {
        Assert.assertEquals(0, Precision.compareTo(152.33, 152.32, .011));
        Assert.assertTrue(Precision.compareTo(152.308, 152.32, .011) < 0);
        Assert.assertTrue(Precision.compareTo(152.33, 152.318, .011) > 0);
        Assert.assertEquals(0, Precision.compareTo(Double.MIN_VALUE, +0.0, Double.MIN_VALUE));
        Assert.assertEquals(0, Precision.compareTo(Double.MIN_VALUE, -0.0, Double.MIN_VALUE));
    }

// org.apache.commons.math3.util.PrecisionTest::testCompareToMaxUlps
    public void testCompareToMaxUlps() {
        double a     = 152.32;
        double delta = FastMath.ulp(a);
        for (int i = 0; i <= 10; ++i) {
            if (i <= 5) {
                Assert.assertEquals( 0, Precision.compareTo(a, a + i * delta, 5));
                Assert.assertEquals( 0, Precision.compareTo(a, a - i * delta, 5));
            } else {
                Assert.assertEquals(-1, Precision.compareTo(a, a + i * delta, 5));
                Assert.assertEquals(+1, Precision.compareTo(a, a - i * delta, 5));
            }
        }

        Assert.assertEquals( 0, Precision.compareTo(-0.0, 0.0, 0));

        Assert.assertEquals(-1, Precision.compareTo(-Double.MIN_VALUE, -0.0, 0));
        Assert.assertEquals( 0, Precision.compareTo(-Double.MIN_VALUE, -0.0, 1));
        Assert.assertEquals(-1, Precision.compareTo(-Double.MIN_VALUE, +0.0, 0));
        Assert.assertEquals( 0, Precision.compareTo(-Double.MIN_VALUE, +0.0, 1));

        Assert.assertEquals(+1, Precision.compareTo( Double.MIN_VALUE, -0.0, 0));
        Assert.assertEquals( 0, Precision.compareTo( Double.MIN_VALUE, -0.0, 1));
        Assert.assertEquals(+1, Precision.compareTo( Double.MIN_VALUE, +0.0, 0));
        Assert.assertEquals( 0, Precision.compareTo( Double.MIN_VALUE, +0.0, 1));

        Assert.assertEquals(-1, Precision.compareTo(-Double.MIN_VALUE, Double.MIN_VALUE, 0));
        Assert.assertEquals(-1, Precision.compareTo(-Double.MIN_VALUE, Double.MIN_VALUE, 1));
        Assert.assertEquals( 0, Precision.compareTo(-Double.MIN_VALUE, Double.MIN_VALUE, 2));

        Assert.assertEquals( 0, Precision.compareTo(Double.MAX_VALUE, Double.POSITIVE_INFINITY, 1));
        Assert.assertEquals(-1, Precision.compareTo(Double.MAX_VALUE, Double.POSITIVE_INFINITY, 0));

        Assert.assertEquals(+1, Precision.compareTo(Double.MAX_VALUE, Double.NaN, Integer.MAX_VALUE));
        Assert.assertEquals(+1, Precision.compareTo(Double.NaN, Double.MAX_VALUE, Integer.MAX_VALUE));
    }

// org.apache.commons.math3.util.PrecisionTest::testRoundDouble
    public void testRoundDouble() {
        double x = 1.234567890;
        Assert.assertEquals(1.23, Precision.round(x, 2), 0.0);
        Assert.assertEquals(1.235, Precision.round(x, 3), 0.0);
        Assert.assertEquals(1.2346, Precision.round(x, 4), 0.0);

        
        Assert.assertEquals(39.25, Precision.round(39.245, 2), 0.0);
        Assert.assertEquals(39.24, Precision.round(39.245, 2, BigDecimal.ROUND_DOWN), 0.0);
        double xx = 39.0;
        xx = xx + 245d / 1000d;
        Assert.assertEquals(39.25, Precision.round(xx, 2), 0.0);

        
        Assert.assertEquals(30.1d, Precision.round(30.095d, 2), 0.0d);
        Assert.assertEquals(30.1d, Precision.round(30.095d, 1), 0.0d);
        Assert.assertEquals(33.1d, Precision.round(33.095d, 1), 0.0d);
        Assert.assertEquals(33.1d, Precision.round(33.095d, 2), 0.0d);
        Assert.assertEquals(50.09d, Precision.round(50.085d, 2), 0.0d);
        Assert.assertEquals(50.19d, Precision.round(50.185d, 2), 0.0d);
        Assert.assertEquals(50.01d, Precision.round(50.005d, 2), 0.0d);
        Assert.assertEquals(30.01d, Precision.round(30.005d, 2), 0.0d);
        Assert.assertEquals(30.65d, Precision.round(30.645d, 2), 0.0d);

        Assert.assertEquals(1.24, Precision.round(x, 2, BigDecimal.ROUND_CEILING), 0.0);
        Assert.assertEquals(1.235, Precision.round(x, 3, BigDecimal.ROUND_CEILING), 0.0);
        Assert.assertEquals(1.2346, Precision.round(x, 4, BigDecimal.ROUND_CEILING), 0.0);
        Assert.assertEquals(-1.23, Precision.round(-x, 2, BigDecimal.ROUND_CEILING), 0.0);
        Assert.assertEquals(-1.234, Precision.round(-x, 3, BigDecimal.ROUND_CEILING), 0.0);
        Assert.assertEquals(-1.2345, Precision.round(-x, 4, BigDecimal.ROUND_CEILING), 0.0);

        Assert.assertEquals(1.23, Precision.round(x, 2, BigDecimal.ROUND_DOWN), 0.0);
        Assert.assertEquals(1.234, Precision.round(x, 3, BigDecimal.ROUND_DOWN), 0.0);
        Assert.assertEquals(1.2345, Precision.round(x, 4, BigDecimal.ROUND_DOWN), 0.0);
        Assert.assertEquals(-1.23, Precision.round(-x, 2, BigDecimal.ROUND_DOWN), 0.0);
        Assert.assertEquals(-1.234, Precision.round(-x, 3, BigDecimal.ROUND_DOWN), 0.0);
        Assert.assertEquals(-1.2345, Precision.round(-x, 4, BigDecimal.ROUND_DOWN), 0.0);

        Assert.assertEquals(1.23, Precision.round(x, 2, BigDecimal.ROUND_FLOOR), 0.0);
        Assert.assertEquals(1.234, Precision.round(x, 3, BigDecimal.ROUND_FLOOR), 0.0);
        Assert.assertEquals(1.2345, Precision.round(x, 4, BigDecimal.ROUND_FLOOR), 0.0);
        Assert.assertEquals(-1.24, Precision.round(-x, 2, BigDecimal.ROUND_FLOOR), 0.0);
        Assert.assertEquals(-1.235, Precision.round(-x, 3, BigDecimal.ROUND_FLOOR), 0.0);
        Assert.assertEquals(-1.2346, Precision.round(-x, 4, BigDecimal.ROUND_FLOOR), 0.0);

        Assert.assertEquals(1.23, Precision.round(x, 2, BigDecimal.ROUND_HALF_DOWN), 0.0);
        Assert.assertEquals(1.235, Precision.round(x, 3, BigDecimal.ROUND_HALF_DOWN), 0.0);
        Assert.assertEquals(1.2346, Precision.round(x, 4, BigDecimal.ROUND_HALF_DOWN), 0.0);
        Assert.assertEquals(-1.23, Precision.round(-x, 2, BigDecimal.ROUND_HALF_DOWN), 0.0);
        Assert.assertEquals(-1.235, Precision.round(-x, 3, BigDecimal.ROUND_HALF_DOWN), 0.0);
        Assert.assertEquals(-1.2346, Precision.round(-x, 4, BigDecimal.ROUND_HALF_DOWN), 0.0);
        Assert.assertEquals(1.234, Precision.round(1.2345, 3, BigDecimal.ROUND_HALF_DOWN), 0.0);
        Assert.assertEquals(-1.234, Precision.round(-1.2345, 3, BigDecimal.ROUND_HALF_DOWN), 0.0);

        Assert.assertEquals(1.23, Precision.round(x, 2, BigDecimal.ROUND_HALF_EVEN), 0.0);
        Assert.assertEquals(1.235, Precision.round(x, 3, BigDecimal.ROUND_HALF_EVEN), 0.0);
        Assert.assertEquals(1.2346, Precision.round(x, 4, BigDecimal.ROUND_HALF_EVEN), 0.0);
        Assert.assertEquals(-1.23, Precision.round(-x, 2, BigDecimal.ROUND_HALF_EVEN), 0.0);
        Assert.assertEquals(-1.235, Precision.round(-x, 3, BigDecimal.ROUND_HALF_EVEN), 0.0);
        Assert.assertEquals(-1.2346, Precision.round(-x, 4, BigDecimal.ROUND_HALF_EVEN), 0.0);
        Assert.assertEquals(1.234, Precision.round(1.2345, 3, BigDecimal.ROUND_HALF_EVEN), 0.0);
        Assert.assertEquals(-1.234, Precision.round(-1.2345, 3, BigDecimal.ROUND_HALF_EVEN), 0.0);
        Assert.assertEquals(1.236, Precision.round(1.2355, 3, BigDecimal.ROUND_HALF_EVEN), 0.0);
        Assert.assertEquals(-1.236, Precision.round(-1.2355, 3, BigDecimal.ROUND_HALF_EVEN), 0.0);

        Assert.assertEquals(1.23, Precision.round(x, 2, BigDecimal.ROUND_HALF_UP), 0.0);
        Assert.assertEquals(1.235, Precision.round(x, 3, BigDecimal.ROUND_HALF_UP), 0.0);
        Assert.assertEquals(1.2346, Precision.round(x, 4, BigDecimal.ROUND_HALF_UP), 0.0);
        Assert.assertEquals(-1.23, Precision.round(-x, 2, BigDecimal.ROUND_HALF_UP), 0.0);
        Assert.assertEquals(-1.235, Precision.round(-x, 3, BigDecimal.ROUND_HALF_UP), 0.0);
        Assert.assertEquals(-1.2346, Precision.round(-x, 4, BigDecimal.ROUND_HALF_UP), 0.0);
        Assert.assertEquals(1.235, Precision.round(1.2345, 3, BigDecimal.ROUND_HALF_UP), 0.0);
        Assert.assertEquals(-1.235, Precision.round(-1.2345, 3, BigDecimal.ROUND_HALF_UP), 0.0);

        Assert.assertEquals(-1.23, Precision.round(-1.23, 2, BigDecimal.ROUND_UNNECESSARY), 0.0);
        Assert.assertEquals(1.23, Precision.round(1.23, 2, BigDecimal.ROUND_UNNECESSARY), 0.0);

        try {
            Precision.round(1.234, 2, BigDecimal.ROUND_UNNECESSARY);
            Assert.fail();
        } catch (ArithmeticException ex) {
            
        }

        Assert.assertEquals(1.24, Precision.round(x, 2, BigDecimal.ROUND_UP), 0.0);
        Assert.assertEquals(1.235, Precision.round(x, 3, BigDecimal.ROUND_UP), 0.0);
        Assert.assertEquals(1.2346, Precision.round(x, 4, BigDecimal.ROUND_UP), 0.0);
        Assert.assertEquals(-1.24, Precision.round(-x, 2, BigDecimal.ROUND_UP), 0.0);
        Assert.assertEquals(-1.235, Precision.round(-x, 3, BigDecimal.ROUND_UP), 0.0);
        Assert.assertEquals(-1.2346, Precision.round(-x, 4, BigDecimal.ROUND_UP), 0.0);

        try {
            Precision.round(1.234, 2, 1923);
            Assert.fail();
        } catch (IllegalArgumentException ex) {
            
        }

        
        Assert.assertEquals(39.25, Precision.round(39.245, 2, BigDecimal.ROUND_HALF_UP), 0.0);

        
        TestUtils.assertEquals(Double.NaN, Precision.round(Double.NaN, 2), 0.0);
        Assert.assertEquals(0.0, Precision.round(0.0, 2), 0.0);
        Assert.assertEquals(Double.POSITIVE_INFINITY, Precision.round(Double.POSITIVE_INFINITY, 2), 0.0);
        Assert.assertEquals(Double.NEGATIVE_INFINITY, Precision.round(Double.NEGATIVE_INFINITY, 2), 0.0);
    }

// org.apache.commons.math3.util.PrecisionTest::testRoundFloat
    public void testRoundFloat() {
        float x = 1.234567890f;
        Assert.assertEquals(1.23f, Precision.round(x, 2), 0.0);
        Assert.assertEquals(1.235f, Precision.round(x, 3), 0.0);
        Assert.assertEquals(1.2346f, Precision.round(x, 4), 0.0);

        
        Assert.assertEquals(30.1f, Precision.round(30.095f, 2), 0.0f);
        Assert.assertEquals(30.1f, Precision.round(30.095f, 1), 0.0f);
        Assert.assertEquals(50.09f, Precision.round(50.085f, 2), 0.0f);
        Assert.assertEquals(50.19f, Precision.round(50.185f, 2), 0.0f);
        Assert.assertEquals(50.01f, Precision.round(50.005f, 2), 0.0f);
        Assert.assertEquals(30.01f, Precision.round(30.005f, 2), 0.0f);
        Assert.assertEquals(30.65f, Precision.round(30.645f, 2), 0.0f);

        Assert.assertEquals(1.24f, Precision.round(x, 2, BigDecimal.ROUND_CEILING), 0.0);
        Assert.assertEquals(1.235f, Precision.round(x, 3, BigDecimal.ROUND_CEILING), 0.0);
        Assert.assertEquals(1.2346f, Precision.round(x, 4, BigDecimal.ROUND_CEILING), 0.0);
        Assert.assertEquals(-1.23f, Precision.round(-x, 2, BigDecimal.ROUND_CEILING), 0.0);
        Assert.assertEquals(-1.234f, Precision.round(-x, 3, BigDecimal.ROUND_CEILING), 0.0);
        Assert.assertEquals(-1.2345f, Precision.round(-x, 4, BigDecimal.ROUND_CEILING), 0.0);

        Assert.assertEquals(1.23f, Precision.round(x, 2, BigDecimal.ROUND_DOWN), 0.0);
        Assert.assertEquals(1.234f, Precision.round(x, 3, BigDecimal.ROUND_DOWN), 0.0);
        Assert.assertEquals(1.2345f, Precision.round(x, 4, BigDecimal.ROUND_DOWN), 0.0);
        Assert.assertEquals(-1.23f, Precision.round(-x, 2, BigDecimal.ROUND_DOWN), 0.0);
        Assert.assertEquals(-1.234f, Precision.round(-x, 3, BigDecimal.ROUND_DOWN), 0.0);
        Assert.assertEquals(-1.2345f, Precision.round(-x, 4, BigDecimal.ROUND_DOWN), 0.0);

        Assert.assertEquals(1.23f, Precision.round(x, 2, BigDecimal.ROUND_FLOOR), 0.0);
        Assert.assertEquals(1.234f, Precision.round(x, 3, BigDecimal.ROUND_FLOOR), 0.0);
        Assert.assertEquals(1.2345f, Precision.round(x, 4, BigDecimal.ROUND_FLOOR), 0.0);
        Assert.assertEquals(-1.24f, Precision.round(-x, 2, BigDecimal.ROUND_FLOOR), 0.0);
        Assert.assertEquals(-1.235f, Precision.round(-x, 3, BigDecimal.ROUND_FLOOR), 0.0);
        Assert.assertEquals(-1.2346f, Precision.round(-x, 4, BigDecimal.ROUND_FLOOR), 0.0);

        Assert.assertEquals(1.23f, Precision.round(x, 2, BigDecimal.ROUND_HALF_DOWN), 0.0);
        Assert.assertEquals(1.235f, Precision.round(x, 3, BigDecimal.ROUND_HALF_DOWN), 0.0);
        Assert.assertEquals(1.2346f, Precision.round(x, 4, BigDecimal.ROUND_HALF_DOWN), 0.0);
        Assert.assertEquals(-1.23f, Precision.round(-x, 2, BigDecimal.ROUND_HALF_DOWN), 0.0);
        Assert.assertEquals(-1.235f, Precision.round(-x, 3, BigDecimal.ROUND_HALF_DOWN), 0.0);
        Assert.assertEquals(-1.2346f, Precision.round(-x, 4, BigDecimal.ROUND_HALF_DOWN), 0.0);
        Assert.assertEquals(1.234f, Precision.round(1.2345f, 3, BigDecimal.ROUND_HALF_DOWN), 0.0);
        Assert.assertEquals(-1.234f, Precision.round(-1.2345f, 3, BigDecimal.ROUND_HALF_DOWN), 0.0);

        Assert.assertEquals(1.23f, Precision.round(x, 2, BigDecimal.ROUND_HALF_EVEN), 0.0);
        Assert.assertEquals(1.235f, Precision.round(x, 3, BigDecimal.ROUND_HALF_EVEN), 0.0);
        Assert.assertEquals(1.2346f, Precision.round(x, 4, BigDecimal.ROUND_HALF_EVEN), 0.0);
        Assert.assertEquals(-1.23f, Precision.round(-x, 2, BigDecimal.ROUND_HALF_EVEN), 0.0);
        Assert.assertEquals(-1.235f, Precision.round(-x, 3, BigDecimal.ROUND_HALF_EVEN), 0.0);
        Assert.assertEquals(-1.2346f, Precision.round(-x, 4, BigDecimal.ROUND_HALF_EVEN), 0.0);
        Assert.assertEquals(1.234f, Precision.round(1.2345f, 3, BigDecimal.ROUND_HALF_EVEN), 0.0);
        Assert.assertEquals(-1.234f, Precision.round(-1.2345f, 3, BigDecimal.ROUND_HALF_EVEN), 0.0);
        Assert.assertEquals(1.236f, Precision.round(1.2355f, 3, BigDecimal.ROUND_HALF_EVEN), 0.0);
        Assert.assertEquals(-1.236f, Precision.round(-1.2355f, 3, BigDecimal.ROUND_HALF_EVEN), 0.0);

        Assert.assertEquals(1.23f, Precision.round(x, 2, BigDecimal.ROUND_HALF_UP), 0.0);
        Assert.assertEquals(1.235f, Precision.round(x, 3, BigDecimal.ROUND_HALF_UP), 0.0);
        Assert.assertEquals(1.2346f, Precision.round(x, 4, BigDecimal.ROUND_HALF_UP), 0.0);
        Assert.assertEquals(-1.23f, Precision.round(-x, 2, BigDecimal.ROUND_HALF_UP), 0.0);
        Assert.assertEquals(-1.235f, Precision.round(-x, 3, BigDecimal.ROUND_HALF_UP), 0.0);
        Assert.assertEquals(-1.2346f, Precision.round(-x, 4, BigDecimal.ROUND_HALF_UP), 0.0);
        Assert.assertEquals(1.235f, Precision.round(1.2345f, 3, BigDecimal.ROUND_HALF_UP), 0.0);
        Assert.assertEquals(-1.235f, Precision.round(-1.2345f, 3, BigDecimal.ROUND_HALF_UP), 0.0);

        Assert.assertEquals(-1.23f, Precision.round(-1.23f, 2, BigDecimal.ROUND_UNNECESSARY), 0.0);
        Assert.assertEquals(1.23f, Precision.round(1.23f, 2, BigDecimal.ROUND_UNNECESSARY), 0.0);

        try {
            Precision.round(1.234f, 2, BigDecimal.ROUND_UNNECESSARY);
            Assert.fail();
        } catch (MathArithmeticException ex) {
            
        }

        Assert.assertEquals(1.24f, Precision.round(x, 2, BigDecimal.ROUND_UP), 0.0);
        Assert.assertEquals(1.235f, Precision.round(x, 3, BigDecimal.ROUND_UP), 0.0);
        Assert.assertEquals(1.2346f, Precision.round(x, 4, BigDecimal.ROUND_UP), 0.0);
        Assert.assertEquals(-1.24f, Precision.round(-x, 2, BigDecimal.ROUND_UP), 0.0);
        Assert.assertEquals(-1.235f, Precision.round(-x, 3, BigDecimal.ROUND_UP), 0.0);
        Assert.assertEquals(-1.2346f, Precision.round(-x, 4, BigDecimal.ROUND_UP), 0.0);

        try {
            Precision.round(1.234f, 2, 1923);
            Assert.fail();
        } catch (MathIllegalArgumentException ex) {
            
        }

        
        TestUtils.assertEquals(Float.NaN, Precision.round(Float.NaN, 2), 0.0f);
        Assert.assertEquals(0.0f, Precision.round(0.0f, 2), 0.0f);
        Assert.assertEquals(Float.POSITIVE_INFINITY, Precision.round(Float.POSITIVE_INFINITY, 2), 0.0f);
        Assert.assertEquals(Float.NEGATIVE_INFINITY, Precision.round(Float.NEGATIVE_INFINITY, 2), 0.0f);
    }

// org.apache.commons.math3.util.PrecisionTest::testIssue721
    public void testIssue721() {
        Assert.assertEquals(-53,   FastMath.getExponent(Precision.EPSILON));
        Assert.assertEquals(-1022, FastMath.getExponent(Precision.SAFE_MIN));
    }

// org.apache.commons.math3.util.PrecisionTest::testRepresentableDelta
    public void testRepresentableDelta() {
        int nonRepresentableCount = 0;
        final double x = 100;
        final int numTrials = 10000;
        for (int i = 0; i < numTrials; i++) {
            final double originalDelta = Math.random();
            final double delta = Precision.representableDelta(x, originalDelta);
            if (delta != originalDelta) {
                ++nonRepresentableCount;
            }
        }

        Assert.assertTrue(nonRepresentableCount / (double) numTrials > 0.9);
    }

// org.apache.commons.math3.util.PrecisionTest::testMath843
    public void testMath843() {
        final double afterEpsilon = FastMath.nextAfter(Precision.EPSILON,
                                                       Double.POSITIVE_INFINITY);

        
        Assert.assertTrue(1 + Precision.EPSILON == 1);

        
        Assert.assertFalse(1 + afterEpsilon == 1);
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
