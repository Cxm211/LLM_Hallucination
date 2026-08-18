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
// org.apache.commons.math3.stat.descriptive.MixedListUnivariateImplTest::testStats
    public void testStats() {
        List<Object> externalList = new ArrayList<Object>();

        DescriptiveStatistics u = new ListUnivariateImpl(externalList,transformers);

        Assert.assertEquals("total count", 0, u.getN(), tolerance);
        u.addValue(one);
        u.addValue(two);
        u.addValue(two);
        u.addValue(three);
        Assert.assertEquals("N", n, u.getN(), tolerance);
        Assert.assertEquals("sum", sum, u.getSum(), tolerance);
        Assert.assertEquals("sumsq", sumSq, u.getSumsq(), tolerance);
        Assert.assertEquals("var", var, u.getVariance(), tolerance);
        Assert.assertEquals("std", std, u.getStandardDeviation(), tolerance);
        Assert.assertEquals("mean", mean, u.getMean(), tolerance);
        Assert.assertEquals("min", min, u.getMin(), tolerance);
        Assert.assertEquals("max", max, u.getMax(), tolerance);
        u.clear();
        Assert.assertEquals("total count", 0, u.getN(), tolerance);
    }

// org.apache.commons.math3.stat.descriptive.MixedListUnivariateImplTest::testN0andN1Conditions
    public void testN0andN1Conditions() {
        DescriptiveStatistics u = new ListUnivariateImpl(new ArrayList<Object>(),transformers);

        Assert.assertTrue(
            "Mean of n = 0 set should be NaN",
            Double.isNaN(u.getMean()));
        Assert.assertTrue(
            "Standard Deviation of n = 0 set should be NaN",
            Double.isNaN(u.getStandardDeviation()));
        Assert.assertTrue(
            "Variance of n = 0 set should be NaN",
            Double.isNaN(u.getVariance()));

        u.addValue(one);

        Assert.assertTrue(
            "Mean of n = 1 set should be value of single item n1, instead it is " + u.getMean() ,
            u.getMean() == one);

        Assert.assertTrue(
            "StdDev of n = 1 set should be zero, instead it is: "
                + u.getStandardDeviation(),
            u.getStandardDeviation() == 0);
        Assert.assertTrue(
            "Variance of n = 1 set should be zero",
            u.getVariance() == 0);
    }

// org.apache.commons.math3.stat.descriptive.MixedListUnivariateImplTest::testSkewAndKurtosis
    public void testSkewAndKurtosis() {
        ListUnivariateImpl u =
            new ListUnivariateImpl(new ArrayList<Object>(), transformers);

        u.addObject("12.5");
        u.addObject(Integer.valueOf(12));
        u.addObject("11.8");
        u.addObject("14.2");
        u.addObject(new Foo());
        u.addObject("14.5");
        u.addObject(Long.valueOf(21));
        u.addObject("8.2");
        u.addObject("10.3");
        u.addObject("11.3");
        u.addObject(Float.valueOf(14.1f));
        u.addObject("9.9");
        u.addObject("12.2");
        u.addObject(new Bar());
        u.addObject("12.1");
        u.addObject("11");
        u.addObject(Double.valueOf(19.8));
        u.addObject("11");
        u.addObject("10");
        u.addObject("8.8");
        u.addObject("9");
        u.addObject("12.3");

        Assert.assertEquals("mean", 12.40455, u.getMean(), 0.0001);
        Assert.assertEquals("variance", 10.00236, u.getVariance(), 0.0001);
        Assert.assertEquals("skewness", 1.437424, u.getSkewness(), 0.0001);
        Assert.assertEquals("kurtosis", 2.37719, u.getKurtosis(), 0.0001);
    }

// org.apache.commons.math3.stat.descriptive.MixedListUnivariateImplTest::testProductAndGeometricMean
    public void testProductAndGeometricMean() {
        ListUnivariateImpl u = new ListUnivariateImpl(new ArrayList<Object>(),transformers);
        u.setWindowSize(10);

        u.addValue(1.0);
        u.addValue(2.0);
        u.addValue(3.0);
        u.addValue(4.0);

        Assert.assertEquals(
            "Geometric mean not expected",
            2.213364,
            u.getGeometricMean(),
            0.00001);

        
        
        for (int i = 0; i < 10; i++) {
            u.addValue(i + 2);
        }
        
        Assert.assertEquals(
            "Geometric mean not expected",
            5.755931,
            u.getGeometricMean(),
            0.00001);

    }

// org.apache.commons.math3.stat.descriptive.MultivariateSummaryStatisticsTest::testSetterInjection
    public void testSetterInjection() {
        MultivariateSummaryStatistics u = createMultivariateSummaryStatistics(2, true);
        u.setMeanImpl(new StorelessUnivariateStatistic[] {
                        new sumMean(), new sumMean()
                      });
        u.addValue(new double[] { 1, 2 });
        u.addValue(new double[] { 3, 4 });
        Assert.assertEquals(4, u.getMean()[0], 1E-14);
        Assert.assertEquals(6, u.getMean()[1], 1E-14);
        u.clear();
        u.addValue(new double[] { 1, 2 });
        u.addValue(new double[] { 3, 4 });
        Assert.assertEquals(4, u.getMean()[0], 1E-14);
        Assert.assertEquals(6, u.getMean()[1], 1E-14);
        u.clear();
        u.setMeanImpl(new StorelessUnivariateStatistic[] {
                        new Mean(), new Mean()
                      }); 
        u.addValue(new double[] { 1, 2 });
        u.addValue(new double[] { 3, 4 });
        Assert.assertEquals(2, u.getMean()[0], 1E-14);
        Assert.assertEquals(3, u.getMean()[1], 1E-14);
        Assert.assertEquals(2, u.getDimension());
    }

// org.apache.commons.math3.stat.descriptive.MultivariateSummaryStatisticsTest::testSetterIllegalState
    public void testSetterIllegalState() {
        MultivariateSummaryStatistics u = createMultivariateSummaryStatistics(2, true);
        u.addValue(new double[] { 1, 2 });
        u.addValue(new double[] { 3, 4 });
        try {
            u.setMeanImpl(new StorelessUnivariateStatistic[] {
                            new sumMean(), new sumMean()
                          });
            Assert.fail("Expecting IllegalStateException");
        } catch (IllegalStateException ex) {
            
        }
    }

// org.apache.commons.math3.stat.descriptive.MultivariateSummaryStatisticsTest::testToString
    public void testToString() {
        MultivariateSummaryStatistics stats = createMultivariateSummaryStatistics(2, true);
        stats.addValue(new double[] {1, 3});
        stats.addValue(new double[] {2, 2});
        stats.addValue(new double[] {3, 1});
        Locale d = Locale.getDefault();
        Locale.setDefault(Locale.US);
        final String suffix = System.getProperty("line.separator");
        Assert.assertEquals("MultivariateSummaryStatistics:" + suffix+
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

// org.apache.commons.math3.stat.descriptive.MultivariateSummaryStatisticsTest::testShuffledStatistics
    public void testShuffledStatistics() {
        
        
        
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

// org.apache.commons.math3.stat.descriptive.MultivariateSummaryStatisticsTest::testDimension
    public void testDimension() {
        try {
            createMultivariateSummaryStatistics(2, true).addValue(new double[3]);
            Assert.fail("Expecting DimensionMismatchException");
        } catch (DimensionMismatchException dme) {
            
        }
    }

// org.apache.commons.math3.stat.descriptive.MultivariateSummaryStatisticsTest::testStats
    public void testStats() {
        MultivariateSummaryStatistics u = createMultivariateSummaryStatistics(2, true);
        Assert.assertEquals(0, u.getN());
        u.addValue(new double[] { 1, 2 });
        u.addValue(new double[] { 2, 3 });
        u.addValue(new double[] { 2, 3 });
        u.addValue(new double[] { 3, 4 });
        Assert.assertEquals( 4, u.getN());
        Assert.assertEquals( 8, u.getSum()[0], 1.0e-10);
        Assert.assertEquals(12, u.getSum()[1], 1.0e-10);
        Assert.assertEquals(18, u.getSumSq()[0], 1.0e-10);
        Assert.assertEquals(38, u.getSumSq()[1], 1.0e-10);
        Assert.assertEquals( 1, u.getMin()[0], 1.0e-10);
        Assert.assertEquals( 2, u.getMin()[1], 1.0e-10);
        Assert.assertEquals( 3, u.getMax()[0], 1.0e-10);
        Assert.assertEquals( 4, u.getMax()[1], 1.0e-10);
        Assert.assertEquals(2.4849066497880003102, u.getSumLog()[0], 1.0e-10);
        Assert.assertEquals( 4.276666119016055311, u.getSumLog()[1], 1.0e-10);
        Assert.assertEquals( 1.8612097182041991979, u.getGeometricMean()[0], 1.0e-10);
        Assert.assertEquals( 2.9129506302439405217, u.getGeometricMean()[1], 1.0e-10);
        Assert.assertEquals( 2, u.getMean()[0], 1.0e-10);
        Assert.assertEquals( 3, u.getMean()[1], 1.0e-10);
        Assert.assertEquals(FastMath.sqrt(2.0 / 3.0), u.getStandardDeviation()[0], 1.0e-10);
        Assert.assertEquals(FastMath.sqrt(2.0 / 3.0), u.getStandardDeviation()[1], 1.0e-10);
        Assert.assertEquals(2.0 / 3.0, u.getCovariance().getEntry(0, 0), 1.0e-10);
        Assert.assertEquals(2.0 / 3.0, u.getCovariance().getEntry(0, 1), 1.0e-10);
        Assert.assertEquals(2.0 / 3.0, u.getCovariance().getEntry(1, 0), 1.0e-10);
        Assert.assertEquals(2.0 / 3.0, u.getCovariance().getEntry(1, 1), 1.0e-10);
        u.clear();
        Assert.assertEquals(0, u.getN());
    }

// org.apache.commons.math3.stat.descriptive.MultivariateSummaryStatisticsTest::testN0andN1Conditions
    public void testN0andN1Conditions() {
        MultivariateSummaryStatistics u = createMultivariateSummaryStatistics(1, true);
        Assert.assertTrue(Double.isNaN(u.getMean()[0]));
        Assert.assertTrue(Double.isNaN(u.getStandardDeviation()[0]));

        
        u.addValue(new double[] { 1 });
        Assert.assertEquals(1.0, u.getMean()[0], 1.0e-10);
        Assert.assertEquals(1.0, u.getGeometricMean()[0], 1.0e-10);
        Assert.assertEquals(0.0, u.getStandardDeviation()[0], 1.0e-10);

        
        u.addValue(new double[] { 2 });
        Assert.assertTrue(u.getStandardDeviation()[0] > 0);

    }

// org.apache.commons.math3.stat.descriptive.MultivariateSummaryStatisticsTest::testNaNContracts
    public void testNaNContracts() {
        MultivariateSummaryStatistics u = createMultivariateSummaryStatistics(1, true);
        Assert.assertTrue(Double.isNaN(u.getMean()[0]));
        Assert.assertTrue(Double.isNaN(u.getMin()[0]));
        Assert.assertTrue(Double.isNaN(u.getStandardDeviation()[0]));
        Assert.assertTrue(Double.isNaN(u.getGeometricMean()[0]));

        u.addValue(new double[] { 1.0 });
        Assert.assertFalse(Double.isNaN(u.getMean()[0]));
        Assert.assertFalse(Double.isNaN(u.getMin()[0]));
        Assert.assertFalse(Double.isNaN(u.getStandardDeviation()[0]));
        Assert.assertFalse(Double.isNaN(u.getGeometricMean()[0]));

    }

// org.apache.commons.math3.stat.descriptive.MultivariateSummaryStatisticsTest::testSerialization
    public void testSerialization() {
        MultivariateSummaryStatistics u = createMultivariateSummaryStatistics(2, true);
        
        TestUtils.checkSerializedEquality(u);
        MultivariateSummaryStatistics s = (MultivariateSummaryStatistics) TestUtils.serializeAndRecover(u);
        Assert.assertEquals(u, s);

        
        u.addValue(new double[] { 2d, 1d });
        u.addValue(new double[] { 1d, 1d });
        u.addValue(new double[] { 3d, 1d });
        u.addValue(new double[] { 4d, 1d });
        u.addValue(new double[] { 5d, 1d });

        
        TestUtils.checkSerializedEquality(u);
        s = (MultivariateSummaryStatistics) TestUtils.serializeAndRecover(u);
        Assert.assertEquals(u, s);

    }

// org.apache.commons.math3.stat.descriptive.MultivariateSummaryStatisticsTest::testEqualsAndHashCode
    public void testEqualsAndHashCode() {
        MultivariateSummaryStatistics u = createMultivariateSummaryStatistics(2, true);
        MultivariateSummaryStatistics t = null;
        int emptyHash = u.hashCode();
        Assert.assertTrue(u.equals(u));
        Assert.assertFalse(u.equals(t));
        Assert.assertFalse(u.equals(Double.valueOf(0)));
        t = createMultivariateSummaryStatistics(2, true);
        Assert.assertTrue(t.equals(u));
        Assert.assertTrue(u.equals(t));
        Assert.assertEquals(emptyHash, t.hashCode());

        
        u.addValue(new double[] { 2d, 1d });
        u.addValue(new double[] { 1d, 1d });
        u.addValue(new double[] { 3d, 1d });
        u.addValue(new double[] { 4d, 1d });
        u.addValue(new double[] { 5d, 1d });
        Assert.assertFalse(t.equals(u));
        Assert.assertFalse(u.equals(t));
        Assert.assertTrue(u.hashCode() != t.hashCode());

        
        t.addValue(new double[] { 2d, 1d });
        t.addValue(new double[] { 1d, 1d });
        t.addValue(new double[] { 3d, 1d });
        t.addValue(new double[] { 4d, 1d });
        t.addValue(new double[] { 5d, 1d });
        Assert.assertTrue(t.equals(u));
        Assert.assertTrue(u.equals(t));
        Assert.assertEquals(u.hashCode(), t.hashCode());

        
        u.clear();
        t.clear();
        Assert.assertTrue(t.equals(u));
        Assert.assertTrue(u.equals(t));
        Assert.assertEquals(emptyHash, t.hashCode());
        Assert.assertEquals(emptyHash, u.hashCode());
    }

// org.apache.commons.math3.stat.descriptive.StatisticalSummaryValuesTest::testSerialization
    public void testSerialization() {
        StatisticalSummaryValues u = new StatisticalSummaryValues(1, 2, 3, 4, 5, 6);
        TestUtils.checkSerializedEquality(u);
        StatisticalSummaryValues t = (StatisticalSummaryValues) TestUtils.serializeAndRecover(u);
        verifyEquality(u, t);
    }

// org.apache.commons.math3.stat.descriptive.StatisticalSummaryValuesTest::testEqualsAndHashCode
    public void testEqualsAndHashCode() {
        StatisticalSummaryValues u  = new StatisticalSummaryValues(1, 2, 3, 4, 5, 6);
        StatisticalSummaryValues t = null;
        Assert.assertTrue("reflexive", u.equals(u));
        Assert.assertFalse("non-null compared to null", u.equals(t));
        Assert.assertFalse("wrong type", u.equals(Double.valueOf(0)));
        t = new StatisticalSummaryValues(1, 2, 3, 4, 5, 6);
        Assert.assertTrue("instances with same data should be equal", t.equals(u));
        Assert.assertEquals("hash code", u.hashCode(), t.hashCode());

        u = new StatisticalSummaryValues(Double.NaN, 2, 3, 4, 5, 6);
        t = new StatisticalSummaryValues(1, Double.NaN, 3, 4, 5, 6);
        Assert.assertFalse("instances based on different data should be different",
                (u.equals(t) ||t.equals(u)));
    }

// org.apache.commons.math3.stat.descriptive.StatisticalSummaryValuesTest::testToString
    public void testToString() {
        StatisticalSummaryValues u  = new StatisticalSummaryValues(4.5, 16, 10, 5, 4, 45);
        Locale d = Locale.getDefault();
        Locale.setDefault(Locale.US);
        Assert.assertEquals("StatisticalSummaryValues:\n" +
                     "n: 10\n" +
                     "min: 4.0\n" +
                     "max: 5.0\n" +
                     "mean: 4.5\n" +
                     "std dev: 4.0\n" +
                     "variance: 16.0\n" +
                     "sum: 45.0\n",  u.toString());
        Locale.setDefault(d);
    }

// org.apache.commons.math3.stat.descriptive.SummaryStatisticsTest::testStats
    public void testStats() {
        SummaryStatistics u = createSummaryStatistics();
        Assert.assertEquals("total count",0,u.getN(),tolerance);
        u.addValue(one);
        u.addValue(twoF);
        u.addValue(twoL);
        u.addValue(three);
        Assert.assertEquals("N",n,u.getN(),tolerance);
        Assert.assertEquals("sum",sum,u.getSum(),tolerance);
        Assert.assertEquals("sumsq",sumSq,u.getSumsq(),tolerance);
        Assert.assertEquals("var",var,u.getVariance(),tolerance);
        Assert.assertEquals("population var",popVar,u.getPopulationVariance(),tolerance);
        Assert.assertEquals("std",std,u.getStandardDeviation(),tolerance);
        Assert.assertEquals("mean",mean,u.getMean(),tolerance);
        Assert.assertEquals("min",min,u.getMin(),tolerance);
        Assert.assertEquals("max",max,u.getMax(),tolerance);
        u.clear();
        Assert.assertEquals("total count",0,u.getN(),tolerance);
    }

// org.apache.commons.math3.stat.descriptive.SummaryStatisticsTest::testN0andN1Conditions
    public void testN0andN1Conditions() {
        SummaryStatistics u = createSummaryStatistics();
        Assert.assertTrue("Mean of n = 0 set should be NaN",
                Double.isNaN( u.getMean() ) );
        Assert.assertTrue("Standard Deviation of n = 0 set should be NaN",
                Double.isNaN( u.getStandardDeviation() ) );
        Assert.assertTrue("Variance of n = 0 set should be NaN",
                Double.isNaN(u.getVariance() ) );

        
        u.addValue(one);
        Assert.assertTrue("mean should be one (n = 1)",
                u.getMean() == one);
        Assert.assertTrue("geometric should be one (n = 1) instead it is " + u.getGeometricMean(),
                u.getGeometricMean() == one);
        Assert.assertTrue("Std should be zero (n = 1)",
                u.getStandardDeviation() == 0.0);
        Assert.assertTrue("variance should be zero (n = 1)",
                u.getVariance() == 0.0);

        
        u.addValue(twoF);
        Assert.assertTrue("Std should not be zero (n = 2)",
                u.getStandardDeviation() != 0.0);
        Assert.assertTrue("variance should not be zero (n = 2)",
                u.getVariance() != 0.0);

    }

// org.apache.commons.math3.stat.descriptive.SummaryStatisticsTest::testProductAndGeometricMean
    public void testProductAndGeometricMean() {
        SummaryStatistics u = createSummaryStatistics();
        u.addValue( 1.0 );
        u.addValue( 2.0 );
        u.addValue( 3.0 );
        u.addValue( 4.0 );

        Assert.assertEquals( "Geometric mean not expected", 2.213364,
                u.getGeometricMean(), 0.00001 );
    }

// org.apache.commons.math3.stat.descriptive.SummaryStatisticsTest::testNaNContracts
    public void testNaNContracts() {
        SummaryStatistics u = createSummaryStatistics();
        Assert.assertTrue("mean not NaN",Double.isNaN(u.getMean()));
        Assert.assertTrue("min not NaN",Double.isNaN(u.getMin()));
        Assert.assertTrue("std dev not NaN",Double.isNaN(u.getStandardDeviation()));
        Assert.assertTrue("var not NaN",Double.isNaN(u.getVariance()));
        Assert.assertTrue("geom mean not NaN",Double.isNaN(u.getGeometricMean()));

        u.addValue(1.0);

        Assert.assertEquals( "mean not expected", 1.0,
                u.getMean(), Double.MIN_VALUE);
        Assert.assertEquals( "variance not expected", 0.0,
                u.getVariance(), Double.MIN_VALUE);
        Assert.assertEquals( "geometric mean not expected", 1.0,
                u.getGeometricMean(), Double.MIN_VALUE);

        u.addValue(-1.0);

        Assert.assertTrue("geom mean not NaN",Double.isNaN(u.getGeometricMean()));

        u.addValue(0.0);

        Assert.assertTrue("geom mean not NaN",Double.isNaN(u.getGeometricMean()));

        
    }

// org.apache.commons.math3.stat.descriptive.SummaryStatisticsTest::testGetSummary
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

// org.apache.commons.math3.stat.descriptive.SummaryStatisticsTest::testSerialization
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

// org.apache.commons.math3.stat.descriptive.SummaryStatisticsTest::testEqualsAndHashCode
    public void testEqualsAndHashCode() {
        SummaryStatistics u = createSummaryStatistics();
        SummaryStatistics t = null;
        int emptyHash = u.hashCode();
        Assert.assertTrue("reflexive", u.equals(u));
        Assert.assertFalse("non-null compared to null", u.equals(t));
        Assert.assertFalse("wrong type", u.equals(Double.valueOf(0)));
        t = createSummaryStatistics();
        Assert.assertTrue("empty instances should be equal", t.equals(u));
        Assert.assertTrue("empty instances should be equal", u.equals(t));
        Assert.assertEquals("empty hash code", emptyHash, t.hashCode());

        
        u.addValue(2d);
        u.addValue(1d);
        u.addValue(3d);
        u.addValue(4d);
        Assert.assertFalse("different n's should make instances not equal", t.equals(u));
        Assert.assertFalse("different n's should make instances not equal", u.equals(t));
        Assert.assertTrue("different n's should make hashcodes different",
                u.hashCode() != t.hashCode());

        
        t.addValue(2d);
        t.addValue(1d);
        t.addValue(3d);
        t.addValue(4d);
        Assert.assertTrue("summaries based on same data should be equal", t.equals(u));
        Assert.assertTrue("summaries based on same data should be equal", u.equals(t));
        Assert.assertEquals("summaries based on same data should have same hashcodes",
                u.hashCode(), t.hashCode());

        
        u.clear();
        t.clear();
        Assert.assertTrue("empty instances should be equal", t.equals(u));
        Assert.assertTrue("empty instances should be equal", u.equals(t));
        Assert.assertEquals("empty hash code", emptyHash, t.hashCode());
        Assert.assertEquals("empty hash code", emptyHash, u.hashCode());
    }

// org.apache.commons.math3.stat.descriptive.SummaryStatisticsTest::testCopy
    public void testCopy() {
        SummaryStatistics u = createSummaryStatistics();
        u.addValue(2d);
        u.addValue(1d);
        u.addValue(3d);
        u.addValue(4d);
        SummaryStatistics v = new SummaryStatistics(u);
        Assert.assertEquals(u, v);
        Assert.assertEquals(v, u);

        
        u.addValue(7d);
        u.addValue(9d);
        u.addValue(11d);
        u.addValue(23d);
        v.addValue(7d);
        v.addValue(9d);
        v.addValue(11d);
        v.addValue(23d);
        Assert.assertEquals(u, v);
        Assert.assertEquals(v, u);

        
        u.clear();
        u.setSumImpl(new Sum());
        SummaryStatistics.copy(u,v);
        Assert.assertEquals(u.getSumImpl(), v.getSumImpl());

    }

// org.apache.commons.math3.stat.descriptive.SummaryStatisticsTest::testSetterInjection
    public void testSetterInjection() {
        SummaryStatistics u = createSummaryStatistics();
        u.setMeanImpl(new Sum());
        u.setSumLogImpl(new Sum());
        u.addValue(1);
        u.addValue(3);
        Assert.assertEquals(4, u.getMean(), 1E-14);
        Assert.assertEquals(4, u.getSumOfLogs(), 1E-14);
        Assert.assertEquals(FastMath.exp(2), u.getGeometricMean(), 1E-14);
        u.clear();
        u.addValue(1);
        u.addValue(2);
        Assert.assertEquals(3, u.getMean(), 1E-14);
        u.clear();
        u.setMeanImpl(new Mean()); 
    }

// org.apache.commons.math3.stat.descriptive.SummaryStatisticsTest::testSetterIllegalState
    public void testSetterIllegalState() {
        SummaryStatistics u = createSummaryStatistics();
        u.addValue(1);
        u.addValue(3);
        try {
            u.setMeanImpl(new Sum());
            Assert.fail("Expecting IllegalStateException");
        } catch (IllegalStateException ex) {
            
        }
    }

// org.apache.commons.math3.stat.descriptive.SummaryStatisticsTest::testOverrideVarianceWithMathClass
    public void testOverrideVarianceWithMathClass() {
        double[] scores = {1, 2, 3, 4};
        SummaryStatistics stats = new SummaryStatistics();
        stats.setVarianceImpl(new Variance(false)); 
        for(double i : scores) {
          stats.addValue(i);
        }
        Assert.assertEquals((new Variance(false)).evaluate(scores),stats.getVariance(), 0); 
    }

// org.apache.commons.math3.stat.descriptive.SummaryStatisticsTest::testOverrideMeanWithMathClass
    public void testOverrideMeanWithMathClass() {
        double[] scores = {1, 2, 3, 4};
        SummaryStatistics stats = new SummaryStatistics();
        stats.setMeanImpl(new Mean()); 
        for(double i : scores) {
          stats.addValue(i);
        }
        Assert.assertEquals((new Mean()).evaluate(scores),stats.getMean(), 0); 
    }

// org.apache.commons.math3.stat.descriptive.SummaryStatisticsTest::testOverrideGeoMeanWithMathClass
    public void testOverrideGeoMeanWithMathClass() {
        double[] scores = {1, 2, 3, 4};
        SummaryStatistics stats = new SummaryStatistics();
        stats.setGeoMeanImpl(new GeometricMean()); 
        for(double i : scores) {
          stats.addValue(i);
        }
        Assert.assertEquals((new GeometricMean()).evaluate(scores),stats.getGeometricMean(), 0); 
    }

// org.apache.commons.math3.stat.descriptive.moment.GeometricMeanTest::testSpecialValues
    public void testSpecialValues() {
        GeometricMean mean = new GeometricMean();
        
        Assert.assertTrue(Double.isNaN(mean.getResult()));

        
        mean.increment(1d);
        Assert.assertFalse(Double.isNaN(mean.getResult()));

        
        mean.increment(0d);
        Assert.assertEquals(0d, mean.getResult(), 0);

        
        mean.increment(Double.POSITIVE_INFINITY);
        Assert.assertTrue(Double.isNaN(mean.getResult()));

        
        mean.clear();
        Assert.assertTrue(Double.isNaN(mean.getResult()));

        
        mean.increment(Double.POSITIVE_INFINITY);
        Assert.assertEquals(Double.POSITIVE_INFINITY, mean.getResult(), 0);

        
        mean.increment(-2d);
        Assert.assertTrue(Double.isNaN(mean.getResult()));
    }

// org.apache.commons.math3.stat.descriptive.moment.InteractionTest::testInteraction
    public void testInteraction() {

        FourthMoment m4 = new FourthMoment();
        Mean m = new Mean(m4);
        Variance v = new Variance(m4);
        Skewness s= new Skewness(m4);
        Kurtosis k = new Kurtosis(m4);

        for (int i = 0; i < testArray.length; i++){
            m4.increment(testArray[i]);
        }

        Assert.assertEquals(mean,m.getResult(),tolerance);
        Assert.assertEquals(var,v.getResult(),tolerance);
        Assert.assertEquals(skew ,s.getResult(),tolerance);
        Assert.assertEquals(kurt,k.getResult(),tolerance);

    }

// org.apache.commons.math3.stat.descriptive.moment.KurtosisTest::testNaN
    public void testNaN() {
        Kurtosis kurt = new Kurtosis();
        Assert.assertTrue(Double.isNaN(kurt.getResult()));
        kurt.increment(1d);
        Assert.assertTrue(Double.isNaN(kurt.getResult()));
        kurt.increment(1d);
        Assert.assertTrue(Double.isNaN(kurt.getResult()));
        kurt.increment(1d);
        Assert.assertTrue(Double.isNaN(kurt.getResult()));
        kurt.increment(1d);
        Assert.assertFalse(Double.isNaN(kurt.getResult()));
    }

// org.apache.commons.math3.stat.descriptive.moment.MeanTest::testSmallSamples
    public void testSmallSamples() {
        Mean mean = new Mean();
        Assert.assertTrue(Double.isNaN(mean.getResult()));
        mean.increment(1d);
        Assert.assertEquals(1d, mean.getResult(), 0);
    }

// org.apache.commons.math3.stat.descriptive.moment.MeanTest::testWeightedMean
    public void testWeightedMean() {
        Mean mean = new Mean();
        Assert.assertEquals(expectedWeightedValue(), mean.evaluate(testArray, testWeightsArray, 0, testArray.length), getTolerance());
        Assert.assertEquals(expectedValue(), mean.evaluate(testArray, identicalWeightsArray, 0, testArray.length), getTolerance());
    }

// org.apache.commons.math3.stat.descriptive.moment.SkewnessTest::testNaN
    public void testNaN() {
        Skewness skew = new Skewness();
        Assert.assertTrue(Double.isNaN(skew.getResult()));
        skew.increment(1d);
        Assert.assertTrue(Double.isNaN(skew.getResult()));
        skew.increment(1d);
        Assert.assertTrue(Double.isNaN(skew.getResult()));
        skew.increment(1d);
        Assert.assertFalse(Double.isNaN(skew.getResult()));
    }

// org.apache.commons.math3.stat.descriptive.moment.StandardDeviationTest::testNaN
    public void testNaN() {
        StandardDeviation std = new StandardDeviation();
        Assert.assertTrue(Double.isNaN(std.getResult()));
        std.increment(1d);
        Assert.assertEquals(0d, std.getResult(), 0);
    }

// org.apache.commons.math3.stat.descriptive.moment.StandardDeviationTest::testPopulation
    public void testPopulation() {
        double[] values = {-1.0d, 3.1d, 4.0d, -2.1d, 22d, 11.7d, 3d, 14d};
        double sigma = populationStandardDeviation(values);
        SecondMoment m = new SecondMoment();
        m.evaluate(values);  
        StandardDeviation s1 = new StandardDeviation();
        s1.setBiasCorrected(false);
        Assert.assertEquals(sigma, s1.evaluate(values), 1E-14);
        s1.incrementAll(values);
        Assert.assertEquals(sigma, s1.getResult(), 1E-14);
        s1 = new StandardDeviation(false, m);
        Assert.assertEquals(sigma, s1.getResult(), 1E-14);
        s1 = new StandardDeviation(false);
        Assert.assertEquals(sigma, s1.evaluate(values), 1E-14);
        s1.incrementAll(values);
        Assert.assertEquals(sigma, s1.getResult(), 1E-14);
    }

// org.apache.commons.math3.stat.descriptive.moment.VarianceTest::testNaN
    public void testNaN() {
        StandardDeviation std = new StandardDeviation();
        Assert.assertTrue(Double.isNaN(std.getResult()));
        std.increment(1d);
        Assert.assertEquals(0d, std.getResult(), 0);
    }

// org.apache.commons.math3.stat.descriptive.moment.VarianceTest::testPopulation
    public void testPopulation() {
        double[] values = {-1.0d, 3.1d, 4.0d, -2.1d, 22d, 11.7d, 3d, 14d};
        SecondMoment m = new SecondMoment();
        m.evaluate(values);  
        Variance v1 = new Variance();
        v1.setBiasCorrected(false);
        Assert.assertEquals(populationVariance(values), v1.evaluate(values), 1E-14);
        v1.incrementAll(values);
        Assert.assertEquals(populationVariance(values), v1.getResult(), 1E-14);
        v1 = new Variance(false, m);
        Assert.assertEquals(populationVariance(values), v1.getResult(), 1E-14);
        v1 = new Variance(false);
        Assert.assertEquals(populationVariance(values), v1.evaluate(values), 1E-14);
        v1.incrementAll(values);
        Assert.assertEquals(populationVariance(values), v1.getResult(), 1E-14);
    }

// org.apache.commons.math3.stat.descriptive.moment.VarianceTest::testWeightedVariance
    public void testWeightedVariance() {
        Variance variance = new Variance();
        Assert.assertEquals(expectedWeightedValue(),
                variance.evaluate(testArray, testWeightsArray, 0, testArray.length), getTolerance());

        
        Assert.assertEquals(expectedValue(),
                variance.evaluate(testArray, unitWeightsArray, 0, testArray.length), getTolerance());

        
        
        Assert.assertEquals(expectedValue(),
                variance.evaluate(testArray, MathArrays.normalizeArray(identicalWeightsArray, testArray.length),
                        0, testArray.length), getTolerance());

    }

// org.apache.commons.math3.stat.descriptive.moment.VectorialMeanTest::testMismatch
    public void testMismatch() {
        try {
            new VectorialMean(8).increment(new double[5]);
            Assert.fail("an exception should have been thrown");
        } catch (DimensionMismatchException dme) {
            Assert.assertEquals(5, dme.getArgument());
            Assert.assertEquals(8, dme.getDimension());
        }
    }

// org.apache.commons.math3.stat.descriptive.moment.VectorialMeanTest::testSimplistic
    public void testSimplistic() {
        VectorialMean stat = new VectorialMean(2);
        stat.increment(new double[] {-1.0,  1.0});
        stat.increment(new double[] { 1.0, -1.0});
        double[] mean = stat.getResult();
        Assert.assertEquals(0.0, mean[0], 1.0e-12);
        Assert.assertEquals(0.0, mean[1], 1.0e-12);
    }

// org.apache.commons.math3.stat.descriptive.moment.VectorialMeanTest::testBasicStats
    public void testBasicStats() {

        VectorialMean stat = new VectorialMean(points[0].length);
        for (int i = 0; i < points.length; ++i) {
            stat.increment(points[i]);
        }

        Assert.assertEquals(points.length, stat.getN());

        double[] mean = stat.getResult();
        double[]   refMean = new double[] { 1.78, 1.62,  3.12};

        for (int i = 0; i < mean.length; ++i) {
            Assert.assertEquals(refMean[i], mean[i], 1.0e-12);
        }

    }

// org.apache.commons.math3.stat.descriptive.moment.VectorialMeanTest::testSerial
    public void testSerial() {
        VectorialMean stat = new VectorialMean(points[0].length);
        for (int i = 0; i < points.length; ++i) {
            stat.increment(points[i]);
        }
        Assert.assertEquals(stat, TestUtils.serializeAndRecover(stat));
    }

// org.apache.commons.math3.stat.descriptive.rank.MaxTest::testSpecialValues
    public void testSpecialValues() {
        double[] testArray = {0d, Double.NaN, Double.NEGATIVE_INFINITY,
                Double.POSITIVE_INFINITY};
        Max max = new Max();
        Assert.assertTrue(Double.isNaN(max.getResult()));
        max.increment(testArray[0]);
        Assert.assertEquals(0d, max.getResult(), 0);
        max.increment(testArray[1]);
        Assert.assertEquals(0d, max.getResult(), 0);
        max.increment(testArray[2]);
        Assert.assertEquals(0d, max.getResult(), 0);
        max.increment(testArray[3]);
        Assert.assertEquals(Double.POSITIVE_INFINITY, max.getResult(), 0);
        Assert.assertEquals(Double.POSITIVE_INFINITY, max.evaluate(testArray), 0);
    }

// org.apache.commons.math3.stat.descriptive.rank.MaxTest::testNaNs
    public void testNaNs() {
        Max max = new Max();
        double nan = Double.NaN;
        Assert.assertEquals(3d, max.evaluate(new double[]{nan, 2d, 3d}), 0);
        Assert.assertEquals(3d, max.evaluate(new double[]{1d, nan, 3d}), 0);
        Assert.assertEquals(2d, max.evaluate(new double[]{1d, 2d, nan}), 0);
        Assert.assertTrue(Double.isNaN(max.evaluate(new double[]{nan, nan, nan})));
    }

// org.apache.commons.math3.stat.descriptive.rank.MinTest::testSpecialValues
    public void testSpecialValues() {
        double[] testArray = {0d, Double.NaN, Double.POSITIVE_INFINITY,
                Double.NEGATIVE_INFINITY};
        Min min = new Min();
        Assert.assertTrue(Double.isNaN(min.getResult()));
        min.increment(testArray[0]);
        Assert.assertEquals(0d, min.getResult(), 0);
        min.increment(testArray[1]);
        Assert.assertEquals(0d, min.getResult(), 0);
        min.increment(testArray[2]);
        Assert.assertEquals(0d, min.getResult(), 0);
        min.increment(testArray[3]);
        Assert.assertEquals(Double.NEGATIVE_INFINITY, min.getResult(), 0);
        Assert.assertEquals(Double.NEGATIVE_INFINITY, min.evaluate(testArray), 0);
    }

// org.apache.commons.math3.stat.descriptive.rank.MinTest::testNaNs
    public void testNaNs() {
        Min min = new Min();
        double nan = Double.NaN;
        Assert.assertEquals(2d, min.evaluate(new double[]{nan, 2d, 3d}), 0);
        Assert.assertEquals(1d, min.evaluate(new double[]{1d, nan, 3d}), 0);
        Assert.assertEquals(1d, min.evaluate(new double[]{1d, 2d, nan}), 0);
        Assert.assertTrue(Double.isNaN(min.evaluate(new double[]{nan, nan, nan})));
    }

// org.apache.commons.math3.stat.descriptive.rank.PercentileTest::testHighPercentile
    public void testHighPercentile(){
        double[] d = new double[]{1, 2, 3};
        Percentile p = new Percentile(75);
        Assert.assertEquals(3.0, p.evaluate(d), 1.0e-5);
    }

// org.apache.commons.math3.stat.descriptive.rank.PercentileTest::testLowPercentile
    public void testLowPercentile() {
        double[] d = new double[] {0, 1};
        Percentile p = new Percentile(25);
        Assert.assertEquals(0d, p.evaluate(d), Double.MIN_VALUE);
    }

// org.apache.commons.math3.stat.descriptive.rank.PercentileTest::testPercentile
    public void testPercentile() {
        double[] d = new double[] {1, 3, 2, 4};
        Percentile p = new Percentile(30);
        Assert.assertEquals(1.5, p.evaluate(d), 1.0e-5);
        p.setQuantile(25);
        Assert.assertEquals(1.25, p.evaluate(d), 1.0e-5);
        p.setQuantile(75);
        Assert.assertEquals(3.75, p.evaluate(d), 1.0e-5);
        p.setQuantile(50);
        Assert.assertEquals(2.5, p.evaluate(d), 1.0e-5);

        
        try {
            p.evaluate(d, 0, d.length, -1.0);
            Assert.fail();
        } catch (MathIllegalArgumentException ex) {
            
        }
        try {
            p.evaluate(d, 0, d.length, 101.0);
            Assert.fail();
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math3.stat.descriptive.rank.PercentileTest::testNISTExample
    public void testNISTExample() {
        double[] d = new double[] {95.1772, 95.1567, 95.1937, 95.1959,
                95.1442, 95.0610,  95.1591, 95.1195, 95.1772, 95.0925, 95.1990, 95.1682
        };
        Percentile p = new Percentile(90);
        Assert.assertEquals(95.1981, p.evaluate(d), 1.0e-4);
        Assert.assertEquals(95.1990, p.evaluate(d,0,d.length, 100d), 0);
    }

// org.apache.commons.math3.stat.descriptive.rank.PercentileTest::test5
    public void test5() {
        Percentile percentile = new Percentile(5);
        Assert.assertEquals(this.percentile5, percentile.evaluate(testArray), getTolerance());
    }

// org.apache.commons.math3.stat.descriptive.rank.PercentileTest::testNullEmpty
    public void testNullEmpty() {
        Percentile percentile = new Percentile(50);
        double[] nullArray = null;
        double[] emptyArray = new double[] {};
        try {
            percentile.evaluate(nullArray);
            Assert.fail("Expecting MathIllegalArgumentException for null array");
        } catch (MathIllegalArgumentException ex) {
            
        }
        Assert.assertTrue(Double.isNaN(percentile.evaluate(emptyArray)));
    }

// org.apache.commons.math3.stat.descriptive.rank.PercentileTest::testSingleton
    public void testSingleton() {
        Percentile percentile = new Percentile(50);
        double[] singletonArray = new double[] {1d};
        Assert.assertEquals(1d, percentile.evaluate(singletonArray), 0);
        Assert.assertEquals(1d, percentile.evaluate(singletonArray, 0, 1), 0);
        Assert.assertEquals(1d, percentile.evaluate(singletonArray, 0, 1, 5), 0);
        Assert.assertEquals(1d, percentile.evaluate(singletonArray, 0, 1, 100), 0);
        Assert.assertTrue(Double.isNaN(percentile.evaluate(singletonArray, 0, 0)));
    }

// org.apache.commons.math3.stat.descriptive.rank.PercentileTest::testSpecialValues
    public void testSpecialValues() {
        Percentile percentile = new Percentile(50);
        double[] specialValues = new double[] {0d, 1d, 2d, 3d, 4d,  Double.NaN};
        Assert.assertEquals(2.5d, percentile.evaluate(specialValues), 0);
        specialValues =  new double[] {Double.NEGATIVE_INFINITY, 1d, 2d, 3d,
                Double.NaN, Double.POSITIVE_INFINITY};
        Assert.assertEquals(2.5d, percentile.evaluate(specialValues), 0);
        specialValues = new double[] {1d, 1d, Double.POSITIVE_INFINITY,
                Double.POSITIVE_INFINITY};
        Assert.assertTrue(Double.isInfinite(percentile.evaluate(specialValues)));
        specialValues = new double[] {1d, 1d, Double.NaN,
                Double.NaN};
        Assert.assertTrue(Double.isNaN(percentile.evaluate(specialValues)));
        specialValues = new double[] {1d, 1d, Double.NEGATIVE_INFINITY,
                Double.NEGATIVE_INFINITY};
        
        Assert.assertTrue(Double.isNaN(percentile.evaluate(specialValues)));
    }

// org.apache.commons.math3.stat.descriptive.rank.PercentileTest::testSetQuantile
    public void testSetQuantile() {
        Percentile percentile = new Percentile(10);
        percentile.setQuantile(100); 
        Assert.assertEquals(100, percentile.getQuantile(), 0);
        try {
            percentile.setQuantile(0);
            Assert.fail("Expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
        try {
            new Percentile(0);
            Assert.fail("Expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math3.stat.descriptive.summary.ProductTest::testSpecialValues
    public void testSpecialValues() {
        Product product = new Product();
        Assert.assertEquals(1, product.getResult(), 0);
        product.increment(1);
        Assert.assertEquals(1, product.getResult(), 0);
        product.increment(Double.POSITIVE_INFINITY);
        Assert.assertEquals(Double.POSITIVE_INFINITY, product.getResult(), 0);
        product.increment(Double.NEGATIVE_INFINITY);
        Assert.assertEquals(Double.NEGATIVE_INFINITY, product.getResult(), 0);
        product.increment(Double.NaN);
        Assert.assertTrue(Double.isNaN(product.getResult()));
        product.increment(1);
        Assert.assertTrue(Double.isNaN(product.getResult()));
    }

// org.apache.commons.math3.stat.descriptive.summary.ProductTest::testWeightedProduct
    public void testWeightedProduct() {
        Product product = new Product();
        Assert.assertEquals(expectedWeightedValue(), product.evaluate(testArray, testWeightsArray, 0, testArray.length),getTolerance());
        Assert.assertEquals(expectedValue(), product.evaluate(testArray, unitWeightsArray, 0, testArray.length), getTolerance());
    }

// org.apache.commons.math3.stat.descriptive.summary.SumLogTest::testSpecialValues
    public void testSpecialValues() {
        SumOfLogs sum = new SumOfLogs();
        
        Assert.assertEquals(0, sum.getResult(), 0);

        
        sum.increment(1d);
        Assert.assertFalse(Double.isNaN(sum.getResult()));

        
        sum.increment(0d);
        Assert.assertEquals(Double.NEGATIVE_INFINITY, sum.getResult(), 0);

        
        sum.increment(Double.POSITIVE_INFINITY);
        Assert.assertTrue(Double.isNaN(sum.getResult()));

        
        sum.clear();
        Assert.assertEquals(0, sum.getResult(), 0);

        
        sum.increment(Double.POSITIVE_INFINITY);
        Assert.assertEquals(Double.POSITIVE_INFINITY, sum.getResult(), 0);

        
        sum.increment(-2d);
        Assert.assertTrue(Double.isNaN(sum.getResult()));
    }

// org.apache.commons.math3.stat.descriptive.summary.SumSqTest::testSpecialValues
    public void testSpecialValues() {
        SumOfSquares sumSq = new SumOfSquares();
        Assert.assertEquals(0, sumSq.getResult(), 0);
        sumSq.increment(2d);
        Assert.assertEquals(4d, sumSq.getResult(), 0);
        sumSq.increment(Double.POSITIVE_INFINITY);
        Assert.assertEquals(Double.POSITIVE_INFINITY, sumSq.getResult(), 0);
        sumSq.increment(Double.NEGATIVE_INFINITY);
        Assert.assertEquals(Double.POSITIVE_INFINITY, sumSq.getResult(), 0);
        sumSq.increment(Double.NaN);
        Assert.assertTrue(Double.isNaN(sumSq.getResult()));
        sumSq.increment(1);
        Assert.assertTrue(Double.isNaN(sumSq.getResult()));
    }

// org.apache.commons.math3.stat.descriptive.summary.SumTest::testSpecialValues
    public void testSpecialValues() {
        Sum sum = new Sum();
        Assert.assertEquals(0, sum.getResult(), 0);
        sum.increment(1);
        Assert.assertEquals(1, sum.getResult(), 0);
        sum.increment(Double.POSITIVE_INFINITY);
        Assert.assertEquals(Double.POSITIVE_INFINITY, sum.getResult(), 0);
        sum.increment(Double.NEGATIVE_INFINITY);
        Assert.assertTrue(Double.isNaN(sum.getResult()));
        sum.increment(1);
        Assert.assertTrue(Double.isNaN(sum.getResult()));
    }

// org.apache.commons.math3.stat.descriptive.summary.SumTest::testWeightedSum
    public void testWeightedSum() {
        Sum sum = new Sum();
        Assert.assertEquals(expectedWeightedValue(), sum.evaluate(testArray, testWeightsArray, 0, testArray.length), getTolerance());
        Assert.assertEquals(expectedValue(), sum.evaluate(testArray, unitWeightsArray, 0, testArray.length), getTolerance());
    }

// org.apache.commons.math3.stat.descriptive.summary.SumTest::testWeightedConsistency
    public void testWeightedConsistency() {}

// org.apache.commons.math3.stat.inference.ChiSquareTestTest::testChiSquare
    public void testChiSquare() {

        
        
        
        

        long[] observed = {10, 9, 11};
        double[] expected = {10, 10, 10};
        Assert.assertEquals("chi-square statistic", 0.2,  testStatistic.chiSquare(expected, observed), 10E-12);
        Assert.assertEquals("chi-square p-value", 0.904837418036, testStatistic.chiSquareTest(expected, observed), 1E-10);

        long[] observed1 = { 500, 623, 72, 70, 31 };
        double[] expected1 = { 485, 541, 82, 61, 37 };
        Assert.assertEquals( "chi-square test statistic", 9.023307936427388, testStatistic.chiSquare(expected1, observed1), 1E-10);
        Assert.assertEquals("chi-square p-value", 0.06051952647453607, testStatistic.chiSquareTest(expected1, observed1), 1E-9);
        Assert.assertTrue("chi-square test reject", testStatistic.chiSquareTest(expected1, observed1, 0.08));
        Assert.assertTrue("chi-square test accept", !testStatistic.chiSquareTest(expected1, observed1, 0.05));

        try {
            testStatistic.chiSquareTest(expected1, observed1, 95);
            Assert.fail("alpha out of range, OutOfRangeException expected");
        } catch (OutOfRangeException ex) {
            
        }

        long[] tooShortObs = { 0 };
        double[] tooShortEx = { 1 };
        try {
            testStatistic.chiSquare(tooShortEx, tooShortObs);
            Assert.fail("arguments too short, DimensionMismatchException expected");
        } catch (DimensionMismatchException ex) {
            
        }

        
        long[] unMatchedObs = { 0, 1, 2, 3 };
        double[] unMatchedEx = { 1, 1, 2 };
        try {
            testStatistic.chiSquare(unMatchedEx, unMatchedObs);
            Assert.fail("arrays have different lengths, DimensionMismatchException expected");
        } catch (DimensionMismatchException ex) {
            
        }

        
        expected[0] = 0;
        try {
            testStatistic.chiSquareTest(expected, observed, .01);
            Assert.fail("bad expected count, NotStrictlyPositiveException expected");
        } catch (NotStrictlyPositiveException ex) {
            
        }

        
        expected[0] = 1;
        observed[0] = -1;
        try {
            testStatistic.chiSquareTest(expected, observed, .01);
            Assert.fail("bad expected count, NotPositiveException expected");
        } catch (NotPositiveException ex) {
            
        }

    }

// org.apache.commons.math3.stat.inference.ChiSquareTestTest::testChiSquareIndependence
    public void testChiSquareIndependence() {

        

        long[][] counts = { {40, 22, 43}, {91, 21, 28}, {60, 10, 22}};
        Assert.assertEquals( "chi-square test statistic", 22.709027688, testStatistic.chiSquare(counts), 1E-9);
        Assert.assertEquals("chi-square p-value", 0.000144751460134, testStatistic.chiSquareTest(counts), 1E-9);
        Assert.assertTrue("chi-square test reject", testStatistic.chiSquareTest(counts, 0.0002));
        Assert.assertTrue("chi-square test accept", !testStatistic.chiSquareTest(counts, 0.0001));

        long[][] counts2 = {{10, 15}, {30, 40}, {60, 90} };
        Assert.assertEquals( "chi-square test statistic", 0.168965517241, testStatistic.chiSquare(counts2), 1E-9);
        Assert.assertEquals("chi-square p-value",0.918987499852, testStatistic.chiSquareTest(counts2), 1E-9);
        Assert.assertTrue("chi-square test accept", !testStatistic.chiSquareTest(counts2, 0.1));

        
        long[][] counts3 = { {40, 22, 43}, {91, 21, 28}, {60, 10}};
        try {
            testStatistic.chiSquare(counts3);
            Assert.fail("Expecting DimensionMismatchException");
        } catch (DimensionMismatchException ex) {
            
        }

        
        long[][] counts4 = {{40, 22, 43}};
        try {
            testStatistic.chiSquare(counts4);
            Assert.fail("Expecting DimensionMismatchException");
        } catch (DimensionMismatchException ex) {
            
        }
        long[][] counts5 = {{40}, {40}, {30}, {10}};
        try {
            testStatistic.chiSquare(counts5);
            Assert.fail("Expecting DimensionMismatchException");
        } catch (DimensionMismatchException ex) {
            
        }

        
        long[][] counts6 = {{10, -2}, {30, 40}, {60, 90} };
        try {
            testStatistic.chiSquare(counts6);
            Assert.fail("Expecting NotPositiveException");
        } catch (NotPositiveException ex) {
            
        }

        
        try {
            testStatistic.chiSquareTest(counts, 0);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
    }

// org.apache.commons.math3.stat.inference.ChiSquareTestTest::testChiSquareLargeTestStatistic
    public void testChiSquareLargeTestStatistic() {
        double[] exp = new double[] {
            3389119.5, 649136.6, 285745.4, 25357364.76, 11291189.78, 543628.0,
            232921.0, 437665.75
        };

        long[] obs = new long[] {
            2372383, 584222, 257170, 17750155, 7903832, 489265, 209628, 393899
        };
        org.apache.commons.math3.stat.inference.ChiSquareTest csti =
            new org.apache.commons.math3.stat.inference.ChiSquareTest();
        double cst = csti.chiSquareTest(exp, obs);
        Assert.assertEquals("chi-square p-value", 0.0, cst, 1E-3);
        Assert.assertEquals( "chi-square test statistic",
                114875.90421929007, testStatistic.chiSquare(exp, obs), 1E-9);
    }

// org.apache.commons.math3.stat.inference.ChiSquareTestTest::testChiSquareZeroCount
    public void testChiSquareZeroCount() {
        
        long[][] counts = { {40, 0, 4}, {91, 1, 2}, {60, 2, 0}};
        Assert.assertEquals( "chi-square test statistic", 9.67444662263,
                testStatistic.chiSquare(counts), 1E-9);
        Assert.assertEquals("chi-square p-value", 0.0462835770603,
                testStatistic.chiSquareTest(counts), 1E-9);
    }

// org.apache.commons.math3.stat.inference.ChiSquareTestTest::testChiSquareDataSetsComparisonEqualCounts
    public void testChiSquareDataSetsComparisonEqualCounts()
        {
        long[] observed1 = {10, 12, 12, 10};
        long[] observed2 = {5, 15, 14, 10};
        Assert.assertEquals("chi-square p value", 0.541096,
                testStatistic.chiSquareTestDataSetsComparison(
                observed1, observed2), 1E-6);
        Assert.assertEquals("chi-square test statistic", 2.153846,
                testStatistic.chiSquareDataSetsComparison(
                observed1, observed2), 1E-6);
        Assert.assertFalse("chi-square test result",
                testStatistic.chiSquareTestDataSetsComparison(
                observed1, observed2, 0.4));
    }

// org.apache.commons.math3.stat.inference.ChiSquareTestTest::testChiSquareDataSetsComparisonUnEqualCounts
    public void testChiSquareDataSetsComparisonUnEqualCounts()
        {
        long[] observed1 = {10, 12, 12, 10, 15};
        long[] observed2 = {15, 10, 10, 15, 5};
        Assert.assertEquals("chi-square p value", 0.124115,
                testStatistic.chiSquareTestDataSetsComparison(
                observed1, observed2), 1E-6);
        Assert.assertEquals("chi-square test statistic", 7.232189,
                testStatistic.chiSquareDataSetsComparison(
                observed1, observed2), 1E-6);
        Assert.assertTrue("chi-square test result",
                testStatistic.chiSquareTestDataSetsComparison(
                observed1, observed2, 0.13));
        Assert.assertFalse("chi-square test result",
                testStatistic.chiSquareTestDataSetsComparison(
                observed1, observed2, 0.12));
    }

// org.apache.commons.math3.stat.inference.ChiSquareTestTest::testChiSquareDataSetsComparisonBadCounts
    public void testChiSquareDataSetsComparisonBadCounts()
        {
        long[] observed1 = {10, -1, 12, 10, 15};
        long[] observed2 = {15, 10, 10, 15, 5};
        try {
            testStatistic.chiSquareTestDataSetsComparison(
                    observed1, observed2);
            Assert.fail("Expecting NotPositiveException - negative count");
        } catch (NotPositiveException ex) {
            
        }
        long[] observed3 = {10, 0, 12, 10, 15};
        long[] observed4 = {15, 0, 10, 15, 5};
        try {
            testStatistic.chiSquareTestDataSetsComparison(
                    observed3, observed4);
            Assert.fail("Expecting ZeroException - double 0's");
        } catch (ZeroException ex) {
            
        }
        long[] observed5 = {10, 10, 12, 10, 15};
        long[] observed6 = {0, 0, 0, 0, 0};
        try {
            testStatistic.chiSquareTestDataSetsComparison(
                    observed5, observed6);
            Assert.fail("Expecting ZeroException - vanishing counts");
        } catch (ZeroException ex) {
            
        }
    }

// org.apache.commons.math3.stat.inference.GTestTest::testGTestGoodnesOfFit1
    public void testGTestGoodnesOfFit1() throws Exception {
        final double[] exp = new double[]{
            3d, 1d
        };

        final long[] obs = new long[]{
            423, 133
        };

        Assert.assertEquals("G test statistic",
                0.348721, testStatistic.g(exp, obs), 1E-6);
        final double p_gtgf = testStatistic.gTest(exp, obs);
        Assert.assertEquals("g-Test p-value", 0.55483, p_gtgf, 1E-5);

        Assert.assertFalse(testStatistic.gTest(exp, obs, 0.05));
    }

// org.apache.commons.math3.stat.inference.GTestTest::testGTestGoodnesOfFit2
    public void testGTestGoodnesOfFit2() throws Exception {
        final double[] exp = new double[]{
            0.54d, 0.40d, 0.05d, 0.01d
        };

        final long[] obs = new long[]{
            70, 79, 3, 4
        };
        Assert.assertEquals("G test statistic",
                13.144799, testStatistic.g(exp, obs), 1E-6);
        final double p_gtgf = testStatistic.gTest(exp, obs);
        Assert.assertEquals("g-Test p-value", 0.004333, p_gtgf, 1E-5);

        Assert.assertTrue(testStatistic.gTest(exp, obs, 0.05));
    }

// org.apache.commons.math3.stat.inference.GTestTest::testGTestGoodnesOfFit3
    public void testGTestGoodnesOfFit3() throws Exception {
        final double[] exp = new double[]{
            0.167d, 0.483d, 0.350d
        };

        final long[] obs = new long[]{
            14, 21, 25
        };

        Assert.assertEquals("G test statistic",
                4.5554, testStatistic.g(exp, obs), 1E-4);
        
        final double p_gtgf = testStatistic.gTestIntrinsic(exp, obs);
        Assert.assertEquals("g-Test p-value", 0.0328, p_gtgf, 1E-4);

        Assert.assertFalse(testStatistic.gTest(exp, obs, 0.05));
    }

// org.apache.commons.math3.stat.inference.GTestTest::testGTestIndependance1
    public void testGTestIndependance1() throws Exception {
        final long[] obs1 = new long[]{
            268, 199, 42
        };

        final long[] obs2 = new long[]{
            807, 759, 184
        };

        final double g = testStatistic.gDataSetsComparison(obs1, obs2);

        Assert.assertEquals("G test statistic",
                7.3008170, g, 1E-6);
        final double p_gti = testStatistic.gTestDataSetsComparison(obs1, obs2);

        Assert.assertEquals("g-Test p-value", 0.0259805, p_gti, 1E-6);
        Assert.assertTrue(testStatistic.gTestDataSetsComparison(obs1, obs2, 0.05));
    }

// org.apache.commons.math3.stat.inference.GTestTest::testGTestIndependance2
    public void testGTestIndependance2() throws Exception {
        final long[] obs1 = new long[]{
            127, 99, 264
        };

        final long[] obs2 = new long[]{
            116, 67, 161
        };

        final double g = testStatistic.gDataSetsComparison(obs1, obs2);

        Assert.assertEquals("G test statistic",
                6.227288, g, 1E-6);
        final double p_gti = testStatistic.gTestDataSetsComparison(obs1, obs2);

        Assert.assertEquals("g-Test p-value", 0.04443, p_gti, 1E-5);
        Assert.assertTrue(testStatistic.gTestDataSetsComparison(obs1, obs2, 0.05));
    }

// org.apache.commons.math3.stat.inference.GTestTest::testGTestIndependance3
    public void testGTestIndependance3() throws Exception {
        final long[] obs1 = new long[]{
            190, 149
        };

        final long[] obs2 = new long[]{
            42, 49
        };

        final double g = testStatistic.gDataSetsComparison(obs1, obs2);
        Assert.assertEquals("G test statistic",
                2.8187, g, 1E-4);
        final double p_gti = testStatistic.gTestDataSetsComparison(obs1, obs2);
        Assert.assertEquals("g-Test p-value", 0.09317325, p_gti, 1E-6);

        Assert.assertFalse(testStatistic.gTestDataSetsComparison(obs1, obs2, 0.05));
    }

// org.apache.commons.math3.stat.inference.GTestTest::testGTestSetsComparisonBadCounts
    public void testGTestSetsComparisonBadCounts() {
        long[] observed1 = {10, -1, 12, 10, 15};
        long[] observed2 = {15, 10, 10, 15, 5};
        try {
            testStatistic.gTestDataSetsComparison(
                    observed1, observed2);
            Assert.fail("Expecting NotPositiveException - negative count");
        } catch (NotPositiveException ex) {
            
        }
        long[] observed3 = {10, 0, 12, 10, 15};
        long[] observed4 = {15, 0, 10, 15, 5};
        try {
            testStatistic.gTestDataSetsComparison(
                    observed3, observed4);
            Assert.fail("Expecting ZeroException - double 0's");
        } catch (ZeroException ex) {
            
        }
        long[] observed5 = {10, 10, 12, 10, 15};
        long[] observed6 = {0, 0, 0, 0, 0};
        try {
            testStatistic.gTestDataSetsComparison(
                    observed5, observed6);
            Assert.fail("Expecting ZeroException - vanishing counts");
        } catch (ZeroException ex) {
            
        }
    }

// org.apache.commons.math3.stat.inference.GTestTest::testUnmatchedArrays
    public void testUnmatchedArrays() {
        final long[] observed = { 0, 1, 2, 3 };
        final double[] expected = { 1, 1, 2 };
        final long[] observed2 = {3, 4};
        try {
            testStatistic.gTest(expected, observed);
            Assert.fail("arrays have different lengths, DimensionMismatchException expected");
        } catch (DimensionMismatchException ex) {
            
        }
        try {
            testStatistic.gTestDataSetsComparison(observed, observed2);
            Assert.fail("arrays have different lengths, DimensionMismatchException expected");
        } catch (DimensionMismatchException ex) {
            
        }
    }

// org.apache.commons.math3.stat.inference.GTestTest::testNegativeObservedCounts
    public void testNegativeObservedCounts() {
        final long[] observed = { 0, 1, 2, -3 };
        final double[] expected = { 1, 1, 2, 3};
        final long[] observed2 = {3, 4, 5, 0};
        try {
            testStatistic.gTest(expected, observed);
            Assert.fail("negative observed count, NotPositiveException expected");
        } catch (NotPositiveException ex) {
            
        }
        try {
            testStatistic.gTestDataSetsComparison(observed, observed2);
            Assert.fail("negative observed count, NotPositiveException expected");
        } catch (NotPositiveException ex) {
            
        } 
    }

// org.apache.commons.math3.stat.inference.GTestTest::testZeroExpectedCounts
    public void testZeroExpectedCounts() {
        final long[] observed = { 0, 1, 2, -3 };
        final double[] expected = { 1, 0, 2, 3};
        try {
            testStatistic.gTest(expected, observed);
            Assert.fail("zero expected count, NotStrictlyPositiveException expected");
        } catch (NotStrictlyPositiveException ex) {
            
        }
    }

// org.apache.commons.math3.stat.inference.GTestTest::testBadAlpha
    public void testBadAlpha() {
        final long[] observed = { 0, 1, 2, 3 };
        final double[] expected = { 1, 2, 2, 3};
        final long[] observed2 = { 0, 2, 2, 3 };
        try {
            testStatistic.gTest(expected, observed, 0.8);
            Assert.fail("zero expected count, NotStrictlyPositiveException expected");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            testStatistic.gTestDataSetsComparison(observed, observed2, -0.5);
            Assert.fail("zero expected count, NotStrictlyPositiveException expected");
        } catch (OutOfRangeException ex) {
            
        }  
    }

// org.apache.commons.math3.stat.inference.GTestTest::testScaling
    public void testScaling() {
      final long[] observed = {9, 11, 10, 8, 12};
      final double[] expected1 = {10, 10, 10, 10, 10};
      final double[] expected2 = {1000, 1000, 1000, 1000, 1000};
      final double[] expected3 = {1, 1, 1, 1, 1};
      final double tol = 1E-15;
      Assert.assertEquals(
              testStatistic.gTest(expected1, observed),
              testStatistic.gTest(expected2, observed),
              tol);
      Assert.assertEquals(
              testStatistic.gTest(expected1, observed),
              testStatistic.gTest(expected3, observed),
              tol);
    }

// org.apache.commons.math3.stat.inference.GTestTest::testRootLogLikelihood
    public void testRootLogLikelihood() {
        
        Assert.assertTrue(testStatistic.rootLogLikelihoodRatio(904, 21060, 1144, 283012) > 0.0);

        
        Assert.assertTrue(testStatistic.rootLogLikelihoodRatio(36, 21928, 60280, 623876) < 0.0);

        Assert.assertEquals(Math.sqrt(2.772589), testStatistic.rootLogLikelihoodRatio(1, 0, 0, 1), 0.000001);
        Assert.assertEquals(-Math.sqrt(2.772589), testStatistic.rootLogLikelihoodRatio(0, 1, 1, 0), 0.000001);
        Assert.assertEquals(Math.sqrt(27.72589), testStatistic.rootLogLikelihoodRatio(10, 0, 0, 10), 0.00001);

        Assert.assertEquals(Math.sqrt(39.33052), testStatistic.rootLogLikelihoodRatio(5, 1995, 0, 100000), 0.00001);
        Assert.assertEquals(-Math.sqrt(39.33052), testStatistic.rootLogLikelihoodRatio(0, 100000, 5, 1995), 0.00001);

        Assert.assertEquals(Math.sqrt(4730.737), testStatistic.rootLogLikelihoodRatio(1000, 1995, 1000, 100000), 0.001);
        Assert.assertEquals(-Math.sqrt(4730.737), testStatistic.rootLogLikelihoodRatio(1000, 100000, 1000, 1995), 0.001);

        Assert.assertEquals(Math.sqrt(5734.343), testStatistic.rootLogLikelihoodRatio(1000, 1000, 1000, 100000), 0.001);
        Assert.assertEquals(Math.sqrt(5714.932), testStatistic.rootLogLikelihoodRatio(1000, 1000, 1000, 99000), 0.001);
    }

// org.apache.commons.math3.stat.inference.MannWhitneyUTestTest::testMannWhitneyUSimple
    public void testMannWhitneyUSimple() {
        
        final double x[] = {19, 22, 16, 29, 24};
        final double y[] = {20, 11, 17, 12};
        
        Assert.assertEquals(17, testStatistic.mannWhitneyU(x, y), 1e-10);
        Assert.assertEquals(0.08641, testStatistic.mannWhitneyUTest(x, y), 1e-5);
    }

// org.apache.commons.math3.stat.inference.MannWhitneyUTestTest::testMannWhitneyUInputValidation
    public void testMannWhitneyUInputValidation() {
        
        try {
            testStatistic.mannWhitneyUTest(new double[] { }, new double[] { 1.0 });
            Assert.fail("x does not contain samples (exact), NoDataException expected");
        } catch (NoDataException ex) {
            
        }

        try {
            testStatistic.mannWhitneyUTest(new double[] { 1.0 }, new double[] { });
            Assert.fail("y does not contain samples (exact), NoDataException expected");
        } catch (NoDataException ex) {
            
        }

        
        try {
            testStatistic.mannWhitneyUTest(null, null);
            Assert.fail("x and y is null (exact), NullArgumentException expected");
        } catch (NullArgumentException ex) {
            
        }
        
        try {
            testStatistic.mannWhitneyUTest(null, null);
            Assert.fail("x and y is null (asymptotic), NullArgumentException expected");
        } catch (NullArgumentException ex) {
            
        }
        
        
        try {
            testStatistic.mannWhitneyUTest(null, new double[] { 1.0 });
            Assert.fail("x is null (exact), NullArgumentException expected");
        } catch (NullArgumentException ex) {
            
        }
        
        try {
            testStatistic.mannWhitneyUTest(new double[] { 1.0 }, null);
            Assert.fail("y is null (exact), NullArgumentException expected");
        } catch (NullArgumentException ex) {
            
        }
    }

// org.apache.commons.math3.stat.inference.MannWhitneyUTestTest::testBigDataSet
    public void testBigDataSet() {
        double[] d1 = new double[1500];
        double[] d2 = new double[1500];
        for (int i = 0; i < 1500; i++) {
            d1[i] = 2 * i;
            d2[i] = 2 * i + 1;
        }
        double result = testStatistic.mannWhitneyUTest(d1, d2);
        Assert.assertTrue(result > 0.1);
    }

// org.apache.commons.math3.stat.inference.OneWayAnovaTest::testAnovaFValue
    public void testAnovaFValue() {
        
        List<double[]> threeClasses = new ArrayList<double[]>();
        threeClasses.add(classA);
        threeClasses.add(classB);
        threeClasses.add(classC);

        Assert.assertEquals("ANOVA F-value",  24.67361709460624,
                 testStatistic.anovaFValue(threeClasses), 1E-12);

        List<double[]> twoClasses = new ArrayList<double[]>();
        twoClasses.add(classA);
        twoClasses.add(classB);

        Assert.assertEquals("ANOVA F-value",  0.0150579150579,
                 testStatistic.anovaFValue(twoClasses), 1E-12);

        List<double[]> emptyContents = new ArrayList<double[]>();
        emptyContents.add(emptyArray);
        emptyContents.add(classC);
        try {
            testStatistic.anovaFValue(emptyContents);
            Assert.fail("empty array for key classX, MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }

        List<double[]> tooFew = new ArrayList<double[]>();
        tooFew.add(classA);
        try {
            testStatistic.anovaFValue(tooFew);
            Assert.fail("less than two classes, MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math3.stat.inference.OneWayAnovaTest::testAnovaPValue
    public void testAnovaPValue() {
        
        List<double[]> threeClasses = new ArrayList<double[]>();
        threeClasses.add(classA);
        threeClasses.add(classB);
        threeClasses.add(classC);

        Assert.assertEquals("ANOVA P-value", 6.959446E-06,
                 testStatistic.anovaPValue(threeClasses), 1E-12);

        List<double[]> twoClasses = new ArrayList<double[]>();
        twoClasses.add(classA);
        twoClasses.add(classB);

        Assert.assertEquals("ANOVA P-value",  0.904212960464,
                 testStatistic.anovaPValue(twoClasses), 1E-12);

    }

// org.apache.commons.math3.stat.inference.OneWayAnovaTest::testAnovaTest
    public void testAnovaTest() {
        
        List<double[]> threeClasses = new ArrayList<double[]>();
        threeClasses.add(classA);
        threeClasses.add(classB);
        threeClasses.add(classC);

        Assert.assertTrue("ANOVA Test P<0.01", testStatistic.anovaTest(threeClasses, 0.01));

        List<double[]> twoClasses = new ArrayList<double[]>();
        twoClasses.add(classA);
        twoClasses.add(classB);

        Assert.assertFalse("ANOVA Test P>0.01", testStatistic.anovaTest(twoClasses, 0.01));
    }

// org.apache.commons.math3.stat.inference.TTestTest::testOneSampleT
    public void testOneSampleT() {
        double[] observed =
            {93.0, 103.0, 95.0, 101.0, 91.0, 105.0, 96.0, 94.0, 101.0,  88.0, 98.0, 94.0, 101.0, 92.0, 95.0 };
        double mu = 100.0;
        SummaryStatistics sampleStats = null;
        sampleStats = new SummaryStatistics();
        for (int i = 0; i < observed.length; i++) {
            sampleStats.addValue(observed[i]);
        }

        
        Assert.assertEquals("t statistic",  -2.81976445346,
                testStatistic.t(mu, observed), 10E-10);
        Assert.assertEquals("t statistic",  -2.81976445346,
                testStatistic.t(mu, sampleStats), 10E-10);
        Assert.assertEquals("p value", 0.0136390585873,
                testStatistic.tTest(mu, observed), 10E-10);
        Assert.assertEquals("p value", 0.0136390585873,
                testStatistic.tTest(mu, sampleStats), 10E-10);

        try {
            testStatistic.t(mu, (double[]) null);
            Assert.fail("arguments too short, NullArgumentException expected");
        } catch (NullArgumentException ex) {
            
        }

        try {
            testStatistic.t(mu, (SummaryStatistics) null);
            Assert.fail("arguments too short, NullArgumentException expected");
        } catch (NullArgumentException ex) {
            
        }

        try {
            testStatistic.t(mu, emptyObs);
            Assert.fail("arguments too short, NumberIsTooSmallException expected");
        } catch (NumberIsTooSmallException ex) {
            
        }

        try {
            testStatistic.t(mu, emptyStats);
            Assert.fail("arguments too short, NumberIsTooSmallException expected");
        } catch (NumberIsTooSmallException ex) {
            
        }

        try {
            testStatistic.t(mu, tooShortObs);
            Assert.fail("insufficient data to compute t statistic, NumberIsTooSmallException expected");
        } catch (NumberIsTooSmallException ex) {
            
        }
        try {
            testStatistic.tTest(mu, tooShortObs);
            Assert.fail("insufficient data to perform t test, NumberIsTooSmallException expected");
        } catch (NumberIsTooSmallException ex) {
           
        }

        try {
            testStatistic.t(mu, tooShortStats);
            Assert.fail("insufficient data to compute t statistic, NumberIsTooSmallException expected");
        } catch (NumberIsTooSmallException ex) {
            
        }
        try {
            testStatistic.tTest(mu, tooShortStats);
            Assert.fail("insufficient data to perform t test, NumberIsTooSmallException expected");
        } catch (NumberIsTooSmallException ex) {
            
        }
    }

// org.apache.commons.math3.stat.inference.TTestTest::testOneSampleTTest
    public void testOneSampleTTest() {
        double[] oneSidedP =
            {2d, 0d, 6d, 6d, 3d, 3d, 2d, 3d, -6d, 6d, 6d, 6d, 3d, 0d, 1d, 1d, 0d, 2d, 3d, 3d };
        SummaryStatistics oneSidedPStats = new SummaryStatistics();
        for (int i = 0; i < oneSidedP.length; i++) {
            oneSidedPStats.addValue(oneSidedP[i]);
        }
        
        Assert.assertEquals("one sample t stat", 3.86485535541,
                testStatistic.t(0d, oneSidedP), 10E-10);
        Assert.assertEquals("one sample t stat", 3.86485535541,
                testStatistic.t(0d, oneSidedPStats),1E-10);
        Assert.assertEquals("one sample p value", 0.000521637019637,
                testStatistic.tTest(0d, oneSidedP) / 2d, 10E-10);
        Assert.assertEquals("one sample p value", 0.000521637019637,
                testStatistic.tTest(0d, oneSidedPStats) / 2d, 10E-5);
        Assert.assertTrue("one sample t-test reject", testStatistic.tTest(0d, oneSidedP, 0.01));
        Assert.assertTrue("one sample t-test reject", testStatistic.tTest(0d, oneSidedPStats, 0.01));
        Assert.assertTrue("one sample t-test accept", !testStatistic.tTest(0d, oneSidedP, 0.0001));
        Assert.assertTrue("one sample t-test accept", !testStatistic.tTest(0d, oneSidedPStats, 0.0001));

        try {
            testStatistic.tTest(0d, oneSidedP, 95);
            Assert.fail("alpha out of range, OutOfRangeException expected");
        } catch (OutOfRangeException ex) {
            
        }

        try {
            testStatistic.tTest(0d, oneSidedPStats, 95);
            Assert.fail("alpha out of range, OutOfRangeException expected");
        } catch (OutOfRangeException ex) {
            
        }

    }

// org.apache.commons.math3.stat.inference.TTestTest::testTwoSampleTHeterscedastic
    public void testTwoSampleTHeterscedastic() {
        double[] sample1 = { 7d, -4d, 18d, 17d, -3d, -5d, 1d, 10d, 11d, -2d };
        double[] sample2 = { -1d, 12d, -1d, -3d, 3d, -5d, 5d, 2d, -11d, -1d, -3d };
        SummaryStatistics sampleStats1 = new SummaryStatistics();
        for (int i = 0; i < sample1.length; i++) {
            sampleStats1.addValue(sample1[i]);
        }
        SummaryStatistics sampleStats2 = new SummaryStatistics();
        for (int i = 0; i < sample2.length; i++) {
            sampleStats2.addValue(sample2[i]);
        }

        
        Assert.assertEquals("two sample heteroscedastic t stat", 1.60371728768,
                testStatistic.t(sample1, sample2), 1E-10);
        Assert.assertEquals("two sample heteroscedastic t stat", 1.60371728768,
                testStatistic.t(sampleStats1, sampleStats2), 1E-10);
        Assert.assertEquals("two sample heteroscedastic p value", 0.128839369622,
                testStatistic.tTest(sample1, sample2), 1E-10);
        Assert.assertEquals("two sample heteroscedastic p value", 0.128839369622,
                testStatistic.tTest(sampleStats1, sampleStats2), 1E-10);
        Assert.assertTrue("two sample heteroscedastic t-test reject",
                testStatistic.tTest(sample1, sample2, 0.2));
        Assert.assertTrue("two sample heteroscedastic t-test reject",
                testStatistic.tTest(sampleStats1, sampleStats2, 0.2));
        Assert.assertTrue("two sample heteroscedastic t-test accept",
                !testStatistic.tTest(sample1, sample2, 0.1));
        Assert.assertTrue("two sample heteroscedastic t-test accept",
                !testStatistic.tTest(sampleStats1, sampleStats2, 0.1));

        try {
            testStatistic.tTest(sample1, sample2, .95);
            Assert.fail("alpha out of range, OutOfRangeException expected");
        } catch (OutOfRangeException ex) {
            
        }

        try {
            testStatistic.tTest(sampleStats1, sampleStats2, .95);
            Assert.fail("alpha out of range, OutOfRangeException expected");
        } catch (OutOfRangeException ex) {
            
        }

        try {
            testStatistic.tTest(sample1, tooShortObs, .01);
            Assert.fail("insufficient data, NumberIsTooSmallException expected");
        } catch (NumberIsTooSmallException ex) {
            
        }

        try {
            testStatistic.tTest(sampleStats1, tooShortStats, .01);
            Assert.fail("insufficient data, NumberIsTooSmallException expected");
        } catch (NumberIsTooSmallException ex) {
            
        }

        try {
            testStatistic.tTest(sample1, tooShortObs);
            Assert.fail("insufficient data, NumberIsTooSmallException expected");
        } catch (NumberIsTooSmallException ex) {
           
        }

        try {
            testStatistic.tTest(sampleStats1, tooShortStats);
            Assert.fail("insufficient data, NumberIsTooSmallException expected");
        } catch (NumberIsTooSmallException ex) {
            
        }

        try {
            testStatistic.t(sample1, tooShortObs);
            Assert.fail("insufficient data, NumberIsTooSmallException expected");
        } catch (NumberIsTooSmallException ex) {
            
        }

        try {
            testStatistic.t(sampleStats1, tooShortStats);
            Assert.fail("insufficient data, NumberIsTooSmallException expected");
        } catch (NumberIsTooSmallException ex) {
           
        }
    }

// org.apache.commons.math3.stat.inference.TTestTest::testTwoSampleTHomoscedastic
    public void testTwoSampleTHomoscedastic() {
        double[] sample1 ={2, 4, 6, 8, 10, 97};
        double[] sample2 = {4, 6, 8, 10, 16};
        SummaryStatistics sampleStats1 = new SummaryStatistics();
        for (int i = 0; i < sample1.length; i++) {
            sampleStats1.addValue(sample1[i]);
        }
        SummaryStatistics sampleStats2 = new SummaryStatistics();
        for (int i = 0; i < sample2.length; i++) {
            sampleStats2.addValue(sample2[i]);
        }

        
        Assert.assertEquals("two sample homoscedastic t stat", 0.73096310086,
              testStatistic.homoscedasticT(sample1, sample2), 10E-11);
        Assert.assertEquals("two sample homoscedastic p value", 0.4833963785,
                testStatistic.homoscedasticTTest(sampleStats1, sampleStats2), 1E-10);
        Assert.assertTrue("two sample homoscedastic t-test reject",
                testStatistic.homoscedasticTTest(sample1, sample2, 0.49));
        Assert.assertTrue("two sample homoscedastic t-test accept",
                !testStatistic.homoscedasticTTest(sample1, sample2, 0.48));
    }

// org.apache.commons.math3.stat.inference.TTestTest::testSmallSamples
    public void testSmallSamples() {
        double[] sample1 = {1d, 3d};
        double[] sample2 = {4d, 5d};

        
        Assert.assertEquals(-2.2360679775, testStatistic.t(sample1, sample2),
                1E-10);
        Assert.assertEquals(0.198727388935, testStatistic.tTest(sample1, sample2),
                1E-10);
    }

// org.apache.commons.math3.stat.inference.TTestTest::testPaired
    public void testPaired() {
        double[] sample1 = {1d, 3d, 5d, 7d};
        double[] sample2 = {0d, 6d, 11d, 2d};
        double[] sample3 = {5d, 7d, 8d, 10d};

        
        Assert.assertEquals(-0.3133, testStatistic.pairedT(sample1, sample2), 1E-4);
        Assert.assertEquals(0.774544295819, testStatistic.pairedTTest(sample1, sample2), 1E-10);
        Assert.assertEquals(0.001208, testStatistic.pairedTTest(sample1, sample3), 1E-6);
        Assert.assertFalse(testStatistic.pairedTTest(sample1, sample3, .001));
        Assert.assertTrue(testStatistic.pairedTTest(sample1, sample3, .002));
    }

// org.apache.commons.math3.stat.inference.TestUtilsTest::testChiSquare
    public void testChiSquare() {

        
        
        
        

        long[] observed = {10, 9, 11};
        double[] expected = {10, 10, 10};
        Assert.assertEquals("chi-square statistic", 0.2,  TestUtils.chiSquare(expected, observed), 10E-12);
        Assert.assertEquals("chi-square p-value", 0.904837418036, TestUtils.chiSquareTest(expected, observed), 1E-10);

        long[] observed1 = { 500, 623, 72, 70, 31 };
        double[] expected1 = { 485, 541, 82, 61, 37 };
        Assert.assertEquals( "chi-square test statistic", 9.023307936427388, TestUtils.chiSquare(expected1, observed1), 1E-10);
        Assert.assertEquals("chi-square p-value", 0.06051952647453607, TestUtils.chiSquareTest(expected1, observed1), 1E-9);
        Assert.assertTrue("chi-square test reject", TestUtils.chiSquareTest(expected1, observed1, 0.07));
        Assert.assertTrue("chi-square test accept", !TestUtils.chiSquareTest(expected1, observed1, 0.05));

        try {
            TestUtils.chiSquareTest(expected1, observed1, 95);
            Assert.fail("alpha out of range, OutOfRangeException expected");
        } catch (OutOfRangeException ex) {
            
        }

        long[] tooShortObs = { 0 };
        double[] tooShortEx = { 1 };
        try {
            TestUtils.chiSquare(tooShortEx, tooShortObs);
            Assert.fail("arguments too short, DimensionMismatchException expected");
        } catch (DimensionMismatchException ex) {
            
        }

        
        long[] unMatchedObs = { 0, 1, 2, 3 };
        double[] unMatchedEx = { 1, 1, 2 };
        try {
            TestUtils.chiSquare(unMatchedEx, unMatchedObs);
            Assert.fail("arrays have different lengths, DimensionMismatchException expected");
        } catch (DimensionMismatchException ex) {
            
        }

        
        expected[0] = 0;
        try {
            TestUtils.chiSquareTest(expected, observed, .01);
            Assert.fail("bad expected count, NotStrictlyPositiveException expected");
        } catch (NotStrictlyPositiveException ex) {
            
        }

        
        expected[0] = 1;
        observed[0] = -1;
        try {
            TestUtils.chiSquareTest(expected, observed, .01);
            Assert.fail("bad expected count, NotPositiveException expected");
        } catch (NotPositiveException ex) {
            
        }

    }

// org.apache.commons.math3.stat.inference.TestUtilsTest::testChiSquareIndependence
    public void testChiSquareIndependence() {

        

        long[][] counts = { {40, 22, 43}, {91, 21, 28}, {60, 10, 22}};
        Assert.assertEquals( "chi-square test statistic", 22.709027688, TestUtils.chiSquare(counts), 1E-9);
        Assert.assertEquals("chi-square p-value", 0.000144751460134, TestUtils.chiSquareTest(counts), 1E-9);
        Assert.assertTrue("chi-square test reject", TestUtils.chiSquareTest(counts, 0.0002));
        Assert.assertTrue("chi-square test accept", !TestUtils.chiSquareTest(counts, 0.0001));

        long[][] counts2 = {{10, 15}, {30, 40}, {60, 90} };
        Assert.assertEquals( "chi-square test statistic", 0.168965517241, TestUtils.chiSquare(counts2), 1E-9);
        Assert.assertEquals("chi-square p-value",0.918987499852, TestUtils.chiSquareTest(counts2), 1E-9);
        Assert.assertTrue("chi-square test accept", !TestUtils.chiSquareTest(counts2, 0.1));

        
        long[][] counts3 = { {40, 22, 43}, {91, 21, 28}, {60, 10}};
        try {
            TestUtils.chiSquare(counts3);
            Assert.fail("Expecting DimensionMismatchException");
        } catch (DimensionMismatchException ex) {
            
        }

        
        long[][] counts4 = {{40, 22, 43}};
        try {
            TestUtils.chiSquare(counts4);
            Assert.fail("Expecting DimensionMismatchException");
        } catch (DimensionMismatchException ex) {
            
        }
        long[][] counts5 = {{40}, {40}, {30}, {10}};
        try {
            TestUtils.chiSquare(counts5);
            Assert.fail("Expecting DimensionMismatchException");
        } catch (DimensionMismatchException ex) {
            
        }

        
        long[][] counts6 = {{10, -2}, {30, 40}, {60, 90} };
        try {
            TestUtils.chiSquare(counts6);
            Assert.fail("Expecting NotPositiveException");
        } catch (NotPositiveException ex) {
            
        }

        
        try {
            TestUtils.chiSquareTest(counts, 0);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
    }

// org.apache.commons.math3.stat.inference.TestUtilsTest::testChiSquareLargeTestStatistic
    public void testChiSquareLargeTestStatistic() {
        double[] exp = new double[] {
                3389119.5, 649136.6, 285745.4, 25357364.76, 11291189.78, 543628.0,
                232921.0, 437665.75
        };

        long[] obs = new long[] {
                2372383, 584222, 257170, 17750155, 7903832, 489265, 209628, 393899
        };
        org.apache.commons.math3.stat.inference.ChiSquareTest csti =
            new org.apache.commons.math3.stat.inference.ChiSquareTest();
        double cst = csti.chiSquareTest(exp, obs);
        Assert.assertEquals("chi-square p-value", 0.0, cst, 1E-3);
        Assert.assertEquals( "chi-square test statistic",
                114875.90421929007, TestUtils.chiSquare(exp, obs), 1E-9);
    }

// org.apache.commons.math3.stat.inference.TestUtilsTest::testChiSquareZeroCount
    public void testChiSquareZeroCount() {
        
        long[][] counts = { {40, 0, 4}, {91, 1, 2}, {60, 2, 0}};
        Assert.assertEquals( "chi-square test statistic", 9.67444662263,
                TestUtils.chiSquare(counts), 1E-9);
        Assert.assertEquals("chi-square p-value", 0.0462835770603,
                TestUtils.chiSquareTest(counts), 1E-9);
    }

// org.apache.commons.math3.stat.inference.TestUtilsTest::testOneSampleT
    public void testOneSampleT() {
        double[] observed =
            {93.0, 103.0, 95.0, 101.0, 91.0, 105.0, 96.0, 94.0, 101.0,  88.0, 98.0, 94.0, 101.0, 92.0, 95.0 };
        double mu = 100.0;
        SummaryStatistics sampleStats = null;
        sampleStats = new SummaryStatistics();
        for (int i = 0; i < observed.length; i++) {
            sampleStats.addValue(observed[i]);
        }

        
        Assert.assertEquals("t statistic",  -2.81976445346,
                TestUtils.t(mu, observed), 10E-10);
        Assert.assertEquals("t statistic",  -2.81976445346,
                TestUtils.t(mu, sampleStats), 10E-10);
        Assert.assertEquals("p value", 0.0136390585873,
                TestUtils.tTest(mu, observed), 10E-10);
        Assert.assertEquals("p value", 0.0136390585873,
                TestUtils.tTest(mu, sampleStats), 10E-10);

        try {
            TestUtils.t(mu, (double[]) null);
            Assert.fail("arguments too short, NullArgumentException expected");
        } catch (NullArgumentException ex) {
            
        }

        try {
            TestUtils.t(mu, (SummaryStatistics) null);
            Assert.fail("arguments too short, NullArgumentException expected");
        } catch (NullArgumentException ex) {
            
        }

        try {
            TestUtils.t(mu, emptyObs);
            Assert.fail("arguments too short, NumberIsTooSmallException expected");
        } catch (NumberIsTooSmallException ex) {
            
        }

        try {
            TestUtils.t(mu, emptyStats);
            Assert.fail("arguments too short, NumberIsTooSmallException expected");
        } catch (NumberIsTooSmallException ex) {
            
        }

        try {
            TestUtils.t(mu, tooShortObs);
            Assert.fail("insufficient data to compute t statistic, NumberIsTooSmallException expected");
        } catch (NumberIsTooSmallException ex) {
            
        }
        try {
            TestUtils.tTest(mu, tooShortObs);
            Assert.fail("insufficient data to perform t test, NumberIsTooSmallException expected");
        } catch (NumberIsTooSmallException ex) {
            
        }

        try {
            TestUtils.t(mu, (SummaryStatistics) null);
            Assert.fail("insufficient data to compute t statistic, NullArgumentException expected");
        } catch (NullArgumentException ex) {
            
        }
        try {
            TestUtils.tTest(mu, (SummaryStatistics) null);
            Assert.fail("insufficient data to perform t test, NullArgumentException expected");
        } catch (NullArgumentException ex) {
            
        }
    }

// org.apache.commons.math3.stat.inference.TestUtilsTest::testOneSampleTTest
    public void testOneSampleTTest() {
        double[] oneSidedP =
            {2d, 0d, 6d, 6d, 3d, 3d, 2d, 3d, -6d, 6d, 6d, 6d, 3d, 0d, 1d, 1d, 0d, 2d, 3d, 3d };
        SummaryStatistics oneSidedPStats = new SummaryStatistics();
        for (int i = 0; i < oneSidedP.length; i++) {
            oneSidedPStats.addValue(oneSidedP[i]);
        }
        
        Assert.assertEquals("one sample t stat", 3.86485535541,
                TestUtils.t(0d, oneSidedP), 10E-10);
        Assert.assertEquals("one sample t stat", 3.86485535541,
                TestUtils.t(0d, oneSidedPStats),1E-10);
        Assert.assertEquals("one sample p value", 0.000521637019637,
                TestUtils.tTest(0d, oneSidedP) / 2d, 10E-10);
        Assert.assertEquals("one sample p value", 0.000521637019637,
                TestUtils.tTest(0d, oneSidedPStats) / 2d, 10E-5);
        Assert.assertTrue("one sample t-test reject", TestUtils.tTest(0d, oneSidedP, 0.01));
        Assert.assertTrue("one sample t-test reject", TestUtils.tTest(0d, oneSidedPStats, 0.01));
        Assert.assertTrue("one sample t-test accept", !TestUtils.tTest(0d, oneSidedP, 0.0001));
        Assert.assertTrue("one sample t-test accept", !TestUtils.tTest(0d, oneSidedPStats, 0.0001));

        try {
            TestUtils.tTest(0d, oneSidedP, 95);
            Assert.fail("alpha out of range, OutOfRangeException expected");
        } catch (OutOfRangeException ex) {
            
        }

        try {
            TestUtils.tTest(0d, oneSidedPStats, 95);
            Assert.fail("alpha out of range, OutOfRangeException expected");
        } catch (OutOfRangeException ex) {
            
        }

    }

// org.apache.commons.math3.stat.inference.TestUtilsTest::testTwoSampleTHeterscedastic
    public void testTwoSampleTHeterscedastic() {
        double[] sample1 = { 7d, -4d, 18d, 17d, -3d, -5d, 1d, 10d, 11d, -2d };
        double[] sample2 = { -1d, 12d, -1d, -3d, 3d, -5d, 5d, 2d, -11d, -1d, -3d };
        SummaryStatistics sampleStats1 = new SummaryStatistics();
        for (int i = 0; i < sample1.length; i++) {
            sampleStats1.addValue(sample1[i]);
        }
        SummaryStatistics sampleStats2 = new SummaryStatistics();
        for (int i = 0; i < sample2.length; i++) {
            sampleStats2.addValue(sample2[i]);
        }

        
        Assert.assertEquals("two sample heteroscedastic t stat", 1.60371728768,
                TestUtils.t(sample1, sample2), 1E-10);
        Assert.assertEquals("two sample heteroscedastic t stat", 1.60371728768,
                TestUtils.t(sampleStats1, sampleStats2), 1E-10);
        Assert.assertEquals("two sample heteroscedastic p value", 0.128839369622,
                TestUtils.tTest(sample1, sample2), 1E-10);
        Assert.assertEquals("two sample heteroscedastic p value", 0.128839369622,
                TestUtils.tTest(sampleStats1, sampleStats2), 1E-10);
        Assert.assertTrue("two sample heteroscedastic t-test reject",
                TestUtils.tTest(sample1, sample2, 0.2));
        Assert.assertTrue("two sample heteroscedastic t-test reject",
                TestUtils.tTest(sampleStats1, sampleStats2, 0.2));
        Assert.assertTrue("two sample heteroscedastic t-test accept",
                !TestUtils.tTest(sample1, sample2, 0.1));
        Assert.assertTrue("two sample heteroscedastic t-test accept",
                !TestUtils.tTest(sampleStats1, sampleStats2, 0.1));

        try {
            TestUtils.tTest(sample1, sample2, .95);
            Assert.fail("alpha out of range, OutOfRangeException expected");
        } catch (OutOfRangeException ex) {
            
        }

        try {
            TestUtils.tTest(sampleStats1, sampleStats2, .95);
            Assert.fail("alpha out of range, OutOfRangeException expected");
        } catch (OutOfRangeException ex) {
            
        }

        try {
            TestUtils.tTest(sample1, tooShortObs, .01);
            Assert.fail("insufficient data, NumberIsTooSmallException expected");
        } catch (NumberIsTooSmallException ex) {
            
        }

        try {
            TestUtils.tTest(sampleStats1, (SummaryStatistics) null, .01);
            Assert.fail("insufficient data, NullArgumentException expected");
        } catch (NullArgumentException ex) {
            
        }

        try {
            TestUtils.tTest(sample1, tooShortObs);
            Assert.fail("insufficient data, NumberIsTooSmallException expected");
        } catch (NumberIsTooSmallException ex) {
            
        }

        try {
            TestUtils.tTest(sampleStats1, (SummaryStatistics) null);
            Assert.fail("insufficient data, NullArgumentException expected");
        } catch (NullArgumentException ex) {
            
        }

        try {
            TestUtils.t(sample1, tooShortObs);
            Assert.fail("insufficient data, NumberIsTooSmallException expected");
        } catch (NumberIsTooSmallException ex) {
            
        }

        try {
            TestUtils.t(sampleStats1, (SummaryStatistics) null);
            Assert.fail("insufficient data, NullArgumentException expected");
        } catch (NullArgumentException ex) {
            
        }
    }

// org.apache.commons.math3.stat.inference.TestUtilsTest::testTwoSampleTHomoscedastic
    public void testTwoSampleTHomoscedastic() {
        double[] sample1 ={2, 4, 6, 8, 10, 97};
        double[] sample2 = {4, 6, 8, 10, 16};
        SummaryStatistics sampleStats1 = new SummaryStatistics();
        for (int i = 0; i < sample1.length; i++) {
            sampleStats1.addValue(sample1[i]);
        }
        SummaryStatistics sampleStats2 = new SummaryStatistics();
        for (int i = 0; i < sample2.length; i++) {
            sampleStats2.addValue(sample2[i]);
        }

        
        Assert.assertEquals("two sample homoscedastic t stat", 0.73096310086,
                TestUtils.homoscedasticT(sample1, sample2), 10E-11);
        Assert.assertEquals("two sample homoscedastic p value", 0.4833963785,
                TestUtils.homoscedasticTTest(sampleStats1, sampleStats2), 1E-10);
        Assert.assertTrue("two sample homoscedastic t-test reject",
                TestUtils.homoscedasticTTest(sample1, sample2, 0.49));
        Assert.assertTrue("two sample homoscedastic t-test accept",
                !TestUtils.homoscedasticTTest(sample1, sample2, 0.48));
    }

// org.apache.commons.math3.stat.inference.TestUtilsTest::testSmallSamples
    public void testSmallSamples() {
        double[] sample1 = {1d, 3d};
        double[] sample2 = {4d, 5d};

        
        Assert.assertEquals(-2.2360679775, TestUtils.t(sample1, sample2),
                1E-10);
        Assert.assertEquals(0.198727388935, TestUtils.tTest(sample1, sample2),
                1E-10);
    }

// org.apache.commons.math3.stat.inference.TestUtilsTest::testPaired
    public void testPaired() {
        double[] sample1 = {1d, 3d, 5d, 7d};
        double[] sample2 = {0d, 6d, 11d, 2d};
        double[] sample3 = {5d, 7d, 8d, 10d};

        
        Assert.assertEquals(-0.3133, TestUtils.pairedT(sample1, sample2), 1E-4);
        Assert.assertEquals(0.774544295819, TestUtils.pairedTTest(sample1, sample2), 1E-10);
        Assert.assertEquals(0.001208, TestUtils.pairedTTest(sample1, sample3), 1E-6);
        Assert.assertFalse(TestUtils.pairedTTest(sample1, sample3, .001));
        Assert.assertTrue(TestUtils.pairedTTest(sample1, sample3, .002));
    }

// org.apache.commons.math3.stat.inference.TestUtilsTest::testOneWayAnovaUtils
    public void testOneWayAnovaUtils() {
        classes.add(classA);
        classes.add(classB);
        classes.add(classC);
        Assert.assertEquals(oneWayAnova.anovaFValue(classes),
                TestUtils.oneWayAnovaFValue(classes), 10E-12);
        Assert.assertEquals(oneWayAnova.anovaPValue(classes),
                TestUtils.oneWayAnovaPValue(classes), 10E-12);
        Assert.assertEquals(oneWayAnova.anovaTest(classes, 0.01),
                TestUtils.oneWayAnovaTest(classes, 0.01));
    }

// org.apache.commons.math3.stat.inference.TestUtilsTest::testGTestGoodnesOfFit
    public void testGTestGoodnesOfFit() throws Exception {
        double[] exp = new double[]{
            0.54d, 0.40d, 0.05d, 0.01d
        };

        long[] obs = new long[]{
            70, 79, 3, 4
        };
        Assert.assertEquals("G test statistic",
                13.144799, TestUtils.g(exp, obs), 1E-5);
        double p_gtgf = TestUtils.gTest(exp, obs);
        Assert.assertEquals("g-Test p-value", 0.004333, p_gtgf, 1E-5);

        Assert.assertTrue(TestUtils.gTest(exp, obs, 0.05));
}

// org.apache.commons.math3.stat.inference.TestUtilsTest::testGTestIndependance
    public void testGTestIndependance() throws Exception {
        long[] obs1 = new long[]{
            268, 199, 42
        };

        long[] obs2 = new long[]{
            807, 759, 184
        };

        double g = TestUtils.gDataSetsComparison(obs1, obs2);

        Assert.assertEquals("G test statistic",
                7.3008170, g, 1E-4);
        double p_gti = TestUtils.gTestDataSetsComparison(obs1, obs2);

        Assert.assertEquals("g-Test p-value", 0.0259805, p_gti, 1E-4);
        Assert.assertTrue(TestUtils.gTestDataSetsComparison(obs1, obs2, 0.05));
    }

// org.apache.commons.math3.stat.inference.TestUtilsTest::testRootLogLikelihood
    public void testRootLogLikelihood() {
        
        Assert.assertTrue(TestUtils.rootLogLikelihoodRatio(904, 21060, 1144, 283012) > 0.0);

        
        Assert.assertTrue(TestUtils.rootLogLikelihoodRatio(36, 21928, 60280, 623876) < 0.0);

        Assert.assertEquals(Math.sqrt(2.772589), TestUtils.rootLogLikelihoodRatio(1, 0, 0, 1), 0.000001);
        Assert.assertEquals(-Math.sqrt(2.772589), TestUtils.rootLogLikelihoodRatio(0, 1, 1, 0), 0.000001);
        Assert.assertEquals(Math.sqrt(27.72589), TestUtils.rootLogLikelihoodRatio(10, 0, 0, 10), 0.00001);

        Assert.assertEquals(Math.sqrt(39.33052), TestUtils.rootLogLikelihoodRatio(5, 1995, 0, 100000), 0.00001);
        Assert.assertEquals(-Math.sqrt(39.33052), TestUtils.rootLogLikelihoodRatio(0, 100000, 5, 1995), 0.00001);

        Assert.assertEquals(Math.sqrt(4730.737), TestUtils.rootLogLikelihoodRatio(1000, 1995, 1000, 100000), 0.001);
        Assert.assertEquals(-Math.sqrt(4730.737), TestUtils.rootLogLikelihoodRatio(1000, 100000, 1000, 1995), 0.001);

        Assert.assertEquals(Math.sqrt(5734.343), TestUtils.rootLogLikelihoodRatio(1000, 1000, 1000, 100000), 0.001);
        Assert.assertEquals(Math.sqrt(5714.932), TestUtils.rootLogLikelihoodRatio(1000, 1000, 1000, 99000), 0.001);
    }

// org.apache.commons.math3.stat.inference.WilcoxonSignedRankTestTest::testWilcoxonSignedRankSimple
    public void testWilcoxonSignedRankSimple() {
        
        final double x[] = {1.83, 0.50, 1.62, 2.48, 1.68, 1.88, 1.55, 3.06, 1.30};
        final double y[] = {0.878, 0.647, 0.598, 2.05, 1.06, 1.29, 1.06, 3.14, 1.29};
        
        
        Assert.assertEquals(40, testStatistic.wilcoxonSignedRank(x, y), 1e-10);
        Assert.assertEquals(0.03906, testStatistic.wilcoxonSignedRankTest(x, y, true), 1e-5);        
        
        
        Assert.assertEquals(40, testStatistic.wilcoxonSignedRank(x, y), 1e-10);
        Assert.assertEquals(0.0329693812, testStatistic.wilcoxonSignedRankTest(x, y, false), 1e-10);
    }

// org.apache.commons.math3.stat.inference.WilcoxonSignedRankTestTest::testWilcoxonSignedRankInputValidation
    public void testWilcoxonSignedRankInputValidation() {
        
        final double[] x1 = new double[30];
        final double[] x2 = new double[31];
        final double[] y1 = new double[30];
        final double[] y2 = new double[31];
        for (int i = 0; i < 30; ++i) {
            x1[i] = x2[i] = y1[i] = y2[i] = i;            
        }
        
        
        
        
        try {
            testStatistic.wilcoxonSignedRankTest(x2, y2, true);
            Assert.fail("More than 30 samples and exact chosen, NumberIsTooLargeException expected");
        } catch (NumberIsTooLargeException ex) {
            
        }
        
        
        try {
            testStatistic.wilcoxonSignedRankTest(new double[] { }, new double[] { 1.0 }, true);
            Assert.fail("x does not contain samples (exact), NoDataException expected");
        } catch (NoDataException ex) {
            
        }

        try {
            testStatistic.wilcoxonSignedRankTest(new double[] { }, new double[] { 1.0 }, false);
            Assert.fail("x does not contain samples (asymptotic), NoDataException expected");
        } catch (NoDataException ex) {
            
        }

        try {
            testStatistic.wilcoxonSignedRankTest(new double[] { 1.0 }, new double[] { }, true);
            Assert.fail("y does not contain samples (exact), NoDataException expected");
        } catch (NoDataException ex) {
            
        }

        try {
            testStatistic.wilcoxonSignedRankTest(new double[] { 1.0 }, new double[] { }, false);
            Assert.fail("y does not contain samples (asymptotic), NoDataException expected");
        } catch (NoDataException ex) {
            
        }

        
        try {
            testStatistic.wilcoxonSignedRankTest(new double[] { 1.0, 2.0 }, new double[] { 3.0 }, true);
            Assert.fail("x and y not same size (exact), DimensionMismatchException expected");
        } catch (DimensionMismatchException ex) {
            
        }

        try {
            testStatistic.wilcoxonSignedRankTest(new double[] { 1.0, 2.0 }, new double[] { 3.0 }, false);
            Assert.fail("x and y not same size (asymptotic), DimensionMismatchException expected");
        } catch (DimensionMismatchException ex) {
            
        }
        
        
        try {
            testStatistic.wilcoxonSignedRankTest(null, null, true);
            Assert.fail("x and y is null (exact), NullArgumentException expected");
        } catch (NullArgumentException ex) {
            
        }
        
        try {
            testStatistic.wilcoxonSignedRankTest(null, null, false);
            Assert.fail("x and y is null (asymptotic), NullArgumentException expected");
        } catch (NullArgumentException ex) {
            
        }
        
        
        try {
            testStatistic.wilcoxonSignedRankTest(null, new double[] { 1.0 }, true);
            Assert.fail("x is null (exact), NullArgumentException expected");
        } catch (NullArgumentException ex) {
            
        }
        
        try {
            testStatistic.wilcoxonSignedRankTest(null, new double[] { 1.0 }, false);
            Assert.fail("x is null (asymptotic), NullArgumentException expected");
        } catch (NullArgumentException ex) {
            
        }
        
        try {
            testStatistic.wilcoxonSignedRankTest(new double[] { 1.0 }, null, true);
            Assert.fail("y is null (exact), NullArgumentException expected");
        } catch (NullArgumentException ex) {
            
        }
        
        try {
            testStatistic.wilcoxonSignedRankTest(new double[] { 1.0 }, null, false);
            Assert.fail("y is null (asymptotic), NullArgumentException expected");
        } catch (NullArgumentException ex) {
            
        }
    }

// org.apache.commons.math3.stat.ranking.NaturalRankingTest::testDefault
    public void testDefault() { 
        NaturalRanking ranking = new NaturalRanking();
        double[] ranks;
        
        try {
            ranks = ranking.rank(exampleData);
            Assert.fail("expected NotANumberException due to NaNStrategy.FAILED");
        } catch (NotANumberException e) {
            
        }
        
        ranks = ranking.rank(tiesFirst);
        double[] correctRanks = new double[] { 1.5, 1.5, 4, 3, 5 };
        TestUtils.assertEquals(correctRanks, ranks, 0d);
        ranks = ranking.rank(tiesLast);
        correctRanks = new double[] { 3.5, 3.5, 2, 1 };
        TestUtils.assertEquals(correctRanks, ranks, 0d);
        
        try {
            ranks = ranking.rank(multipleNaNs);
            Assert.fail("expected NotANumberException due to NaNStrategy.FAILED");
        } catch (NotANumberException e) {
            
        }
        
        ranks = ranking.rank(multipleTies);
        correctRanks = new double[] { 3, 2, 4.5, 4.5, 6.5, 6.5, 1 };
        TestUtils.assertEquals(correctRanks, ranks, 0d);
        ranks = ranking.rank(allSame);
        correctRanks = new double[] { 2.5, 2.5, 2.5, 2.5 };
        TestUtils.assertEquals(correctRanks, ranks, 0d);
    }

// org.apache.commons.math3.stat.ranking.NaturalRankingTest::testNaNsMaximalTiesMinimum
    public void testNaNsMaximalTiesMinimum() {
        NaturalRanking ranking = new NaturalRanking(NaNStrategy.MAXIMAL, TiesStrategy.MINIMUM);
        double[] ranks = ranking.rank(exampleData);
        double[] correctRanks = { 5, 2, 6, 7, 2, 8, 9, 1, 2 };
        TestUtils.assertEquals(correctRanks, ranks, 0d);
        ranks = ranking.rank(tiesFirst);
        correctRanks = new double[] { 1, 1, 4, 3, 5 };
        TestUtils.assertEquals(correctRanks, ranks, 0d);
        ranks = ranking.rank(tiesLast);
        correctRanks = new double[] { 3, 3, 2, 1 };
        TestUtils.assertEquals(correctRanks, ranks, 0d);
        ranks = ranking.rank(multipleNaNs);
        correctRanks = new double[] { 1, 2, 3, 3 };
        TestUtils.assertEquals(correctRanks, ranks, 0d);
        ranks = ranking.rank(multipleTies);
        correctRanks = new double[] { 3, 2, 4, 4, 6, 6, 1 };
        TestUtils.assertEquals(correctRanks, ranks, 0d);
        ranks = ranking.rank(allSame);
        correctRanks = new double[] { 1, 1, 1, 1 };
        TestUtils.assertEquals(correctRanks, ranks, 0d);
    }

// org.apache.commons.math3.stat.ranking.NaturalRankingTest::testNaNsRemovedTiesSequential
    public void testNaNsRemovedTiesSequential() {
        NaturalRanking ranking = new NaturalRanking(NaNStrategy.REMOVED,
                TiesStrategy.SEQUENTIAL);
        double[] ranks = ranking.rank(exampleData);
        double[] correctRanks = { 5, 2, 6, 7, 3, 8, 1, 4 };
        TestUtils.assertEquals(correctRanks, ranks, 0d);
        ranks = ranking.rank(tiesFirst);
        correctRanks = new double[] { 1, 2, 4, 3, 5 };
        TestUtils.assertEquals(correctRanks, ranks, 0d);
        ranks = ranking.rank(tiesLast);
        correctRanks = new double[] { 3, 4, 2, 1 };
        TestUtils.assertEquals(correctRanks, ranks, 0d);
        ranks = ranking.rank(multipleNaNs);
        correctRanks = new double[] { 1, 2 };
        TestUtils.assertEquals(correctRanks, ranks, 0d);
        ranks = ranking.rank(multipleTies);
        correctRanks = new double[] { 3, 2, 4, 5, 6, 7, 1 };
        TestUtils.assertEquals(correctRanks, ranks, 0d);
        ranks = ranking.rank(allSame);
        correctRanks = new double[] { 1, 2, 3, 4 };
        TestUtils.assertEquals(correctRanks, ranks, 0d);
    }

// org.apache.commons.math3.stat.ranking.NaturalRankingTest::testNaNsMinimalTiesMaximum
    public void testNaNsMinimalTiesMaximum() {
        NaturalRanking ranking = new NaturalRanking(NaNStrategy.MINIMAL,
                TiesStrategy.MAXIMUM);
        double[] ranks = ranking.rank(exampleData);
        double[] correctRanks = { 6, 5, 7, 8, 5, 9, 2, 2, 5 };
        TestUtils.assertEquals(correctRanks, ranks, 0d);
        ranks = ranking.rank(tiesFirst);
        correctRanks = new double[] { 2, 2, 4, 3, 5 };
        TestUtils.assertEquals(correctRanks, ranks, 0d);
        ranks = ranking.rank(tiesLast);
        correctRanks = new double[] { 4, 4, 2, 1 };
        TestUtils.assertEquals(correctRanks, ranks, 0d);
        ranks = ranking.rank(multipleNaNs);
        correctRanks = new double[] { 3, 4, 2, 2 };
        TestUtils.assertEquals(correctRanks, ranks, 0d);
        ranks = ranking.rank(multipleTies);
        correctRanks = new double[] { 3, 2, 5, 5, 7, 7, 1 };
        TestUtils.assertEquals(correctRanks, ranks, 0d);
        ranks = ranking.rank(allSame);
        correctRanks = new double[] { 4, 4, 4, 4 };
        TestUtils.assertEquals(correctRanks, ranks, 0d);
    }

// org.apache.commons.math3.stat.ranking.NaturalRankingTest::testNaNsMinimalTiesAverage
    public void testNaNsMinimalTiesAverage() {
        NaturalRanking ranking = new NaturalRanking(NaNStrategy.MINIMAL);
        double[] ranks = ranking.rank(exampleData);
        double[] correctRanks = { 6, 4, 7, 8, 4, 9, 1.5, 1.5, 4 };
        TestUtils.assertEquals(correctRanks, ranks, 0d);
        ranks = ranking.rank(tiesFirst);
        correctRanks = new double[] { 1.5, 1.5, 4, 3, 5 };
        TestUtils.assertEquals(correctRanks, ranks, 0d);
        ranks = ranking.rank(tiesLast);
        correctRanks = new double[] { 3.5, 3.5, 2, 1 };
        TestUtils.assertEquals(correctRanks, ranks, 0d);
        ranks = ranking.rank(multipleNaNs);
        correctRanks = new double[] { 3, 4, 1.5, 1.5 };
        TestUtils.assertEquals(correctRanks, ranks, 0d);
        ranks = ranking.rank(multipleTies);
        correctRanks = new double[] { 3, 2, 4.5, 4.5, 6.5, 6.5, 1 };
        TestUtils.assertEquals(correctRanks, ranks, 0d);
        ranks = ranking.rank(allSame);
        correctRanks = new double[] { 2.5, 2.5, 2.5, 2.5 };
        TestUtils.assertEquals(correctRanks, ranks, 0d);
    }

// org.apache.commons.math3.stat.ranking.NaturalRankingTest::testNaNsFixedTiesRandom
    public void testNaNsFixedTiesRandom() {
        RandomGenerator randomGenerator = new JDKRandomGenerator();
        randomGenerator.setSeed(1000);
        NaturalRanking ranking = new NaturalRanking(NaNStrategy.FIXED,
                randomGenerator);
        double[] ranks = ranking.rank(exampleData);
        double[] correctRanks = { 5, 4, 6, 7, 3, 8, Double.NaN, 1, 4 };
        TestUtils.assertEquals(correctRanks, ranks, 0d);
        ranks = ranking.rank(tiesFirst);
        correctRanks = new double[] { 1, 1, 4, 3, 5 };
        TestUtils.assertEquals(correctRanks, ranks, 0d);
        ranks = ranking.rank(tiesLast);
        correctRanks = new double[] { 3, 4, 2, 1 };
        TestUtils.assertEquals(correctRanks, ranks, 0d);
        ranks = ranking.rank(multipleNaNs);
        correctRanks = new double[] { 1, 2, Double.NaN, Double.NaN };
        TestUtils.assertEquals(correctRanks, ranks, 0d);
        ranks = ranking.rank(multipleTies);
        correctRanks = new double[] { 3, 2, 5, 5, 7, 6, 1 };
        TestUtils.assertEquals(correctRanks, ranks, 0d);
        ranks = ranking.rank(allSame);
        correctRanks = new double[] { 1, 3, 4, 4 };
        TestUtils.assertEquals(correctRanks, ranks, 0d);
    }

// org.apache.commons.math3.stat.ranking.NaturalRankingTest::testNaNsAndInfs
    public void testNaNsAndInfs() {
        double[] data = { 0, Double.POSITIVE_INFINITY, Double.NaN,
                Double.NEGATIVE_INFINITY };
        NaturalRanking ranking = new NaturalRanking(NaNStrategy.MAXIMAL);
        double[] ranks = ranking.rank(data);
        double[] correctRanks = new double[] { 2, 3.5, 3.5, 1 };
        TestUtils.assertEquals(correctRanks, ranks, 0d);
        ranking = new NaturalRanking(NaNStrategy.MINIMAL);
        ranks = ranking.rank(data);
        correctRanks = new double[] { 3, 4, 1.5, 1.5 };
        TestUtils.assertEquals(correctRanks, ranks, 0d);
    }

// org.apache.commons.math3.stat.ranking.NaturalRankingTest::testNaNsFailed
    public void testNaNsFailed() {
        double[] data = { 0, Double.POSITIVE_INFINITY, Double.NaN, Double.NEGATIVE_INFINITY };
        NaturalRanking ranking = new NaturalRanking(NaNStrategy.FAILED);
        ranking.rank(data);
    }

// org.apache.commons.math3.stat.ranking.NaturalRankingTest::testNoNaNsFailed
    public void testNoNaNsFailed() {
        double[] data = { 1, 2, 3, 4 };
        NaturalRanking ranking = new NaturalRanking(NaNStrategy.FAILED);
        double[] ranks = ranking.rank(data);
        TestUtils.assertEquals(data, ranks, 0d);
    }

// org.apache.commons.math3.stat.regression.GLSMultipleLinearRegressionTest::cannotAddXSampleData
    public void cannotAddXSampleData() {
        createRegression().newSampleData(new double[]{}, null, null);
    }

// org.apache.commons.math3.stat.regression.GLSMultipleLinearRegressionTest::cannotAddNullYSampleData
    public void cannotAddNullYSampleData() {
        createRegression().newSampleData(null, new double[][]{}, null);
    }

// org.apache.commons.math3.stat.regression.GLSMultipleLinearRegressionTest::cannotAddSampleDataWithSizeMismatch
    public void cannotAddSampleDataWithSizeMismatch() {
        double[] y = new double[]{1.0, 2.0};
        double[][] x = new double[1][];
        x[0] = new double[]{1.0, 0};
        createRegression().newSampleData(y, x, null);
    }

// org.apache.commons.math3.stat.regression.GLSMultipleLinearRegressionTest::cannotAddNullCovarianceData
    public void cannotAddNullCovarianceData() {
        createRegression().newSampleData(new double[]{}, new double[][]{}, null);
    }

// org.apache.commons.math3.stat.regression.GLSMultipleLinearRegressionTest::notEnoughData
    public void notEnoughData() {
        double[]   reducedY = new double[y.length - 1];
        double[][] reducedX = new double[x.length - 1][];
        double[][] reducedO = new double[omega.length - 1][];
        System.arraycopy(y,     0, reducedY, 0, reducedY.length);
        System.arraycopy(x,     0, reducedX, 0, reducedX.length);
        System.arraycopy(omega, 0, reducedO, 0, reducedO.length);
        createRegression().newSampleData(reducedY, reducedX, reducedO);
    }

// org.apache.commons.math3.stat.regression.GLSMultipleLinearRegressionTest::cannotAddCovarianceDataWithSampleSizeMismatch
    public void cannotAddCovarianceDataWithSampleSizeMismatch() {
        double[] y = new double[]{1.0, 2.0};
        double[][] x = new double[2][];
        x[0] = new double[]{1.0, 0};
        x[1] = new double[]{0, 1.0};
        double[][] omega = new double[1][];
        omega[0] = new double[]{1.0, 0};
        createRegression().newSampleData(y, x, omega);
    }

// org.apache.commons.math3.stat.regression.GLSMultipleLinearRegressionTest::cannotAddCovarianceDataThatIsNotSquare
    public void cannotAddCovarianceDataThatIsNotSquare() {
        double[] y = new double[]{1.0, 2.0};
        double[][] x = new double[2][];
        x[0] = new double[]{1.0, 0};
        x[1] = new double[]{0, 1.0};
        double[][] omega = new double[3][];
        omega[0] = new double[]{1.0, 0};
        omega[1] = new double[]{0, 1.0};
        omega[2] = new double[]{0, 2.0};
        createRegression().newSampleData(y, x, omega);
    }

// org.apache.commons.math3.stat.regression.GLSMultipleLinearRegressionTest::testYVariance
    public void testYVariance() {

        

        GLSMultipleLinearRegression model = new GLSMultipleLinearRegression();
        model.newSampleData(y, x, omega);
        TestUtils.assertEquals(model.calculateYVariance(), 3.5, 0);
    }

// org.apache.commons.math3.stat.regression.GLSMultipleLinearRegressionTest::testNewSample2
    public void testNewSample2() {
        double[] y = new double[] {1, 2, 3, 4}; 
        double[][] x = new double[][] {
          {19, 22, 33},
          {20, 30, 40},
          {25, 35, 45},
          {27, 37, 47}   
        };
        double[][] covariance = MatrixUtils.createRealIdentityMatrix(4).scalarMultiply(2).getData();
        GLSMultipleLinearRegression regression = new GLSMultipleLinearRegression();
        regression.newSampleData(y, x, covariance);
        RealMatrix combinedX = regression.getX().copy();
        RealVector combinedY = regression.getY().copy();
        RealMatrix combinedCovInv = regression.getOmegaInverse();
        regression.newXSampleData(x);
        regression.newYSampleData(y);
        Assert.assertEquals(combinedX, regression.getX());
        Assert.assertEquals(combinedY, regression.getY());
        Assert.assertEquals(combinedCovInv, regression.getOmegaInverse());
    }

// org.apache.commons.math3.stat.regression.GLSMultipleLinearRegressionTest::testGLSOLSConsistency
    public void testGLSOLSConsistency() {      
        RealMatrix identityCov = MatrixUtils.createRealIdentityMatrix(16);
        GLSMultipleLinearRegression glsModel = new GLSMultipleLinearRegression();
        OLSMultipleLinearRegression olsModel = new OLSMultipleLinearRegression();
        glsModel.newSampleData(longley, 16, 6);
        olsModel.newSampleData(longley, 16, 6);
        glsModel.newCovarianceData(identityCov.getData());
        double[] olsBeta = olsModel.calculateBeta().toArray();
        double[] glsBeta = glsModel.calculateBeta().toArray();
        
        
        for (int i = 0; i < olsBeta.length; i++) {
            TestUtils.assertRelativelyEquals(olsBeta[i], glsBeta[i], 10E-7);
        }
    }

// org.apache.commons.math3.stat.regression.GLSMultipleLinearRegressionTest::testGLSEfficiency
    public void testGLSEfficiency() {
        RandomGenerator rg = new JDKRandomGenerator();
        rg.setSeed(200);  
        
        
        
        final int nObs = 16;
        double[] sigma = new double[nObs];
        for (int i = 0; i < nObs; i++) {
            sigma[i] = 10 * rg.nextDouble();
        }
        
        
        
        final int numSeeds = 1000;
        RealMatrix errorSeeds = MatrixUtils.createRealMatrix(numSeeds, nObs);
        for (int i = 0; i < numSeeds; i++) {
            for (int j = 0; j < nObs; j++) {
                errorSeeds.setEntry(i, j, rg.nextGaussian() * sigma[j]);
            }
        }
        
        
        RealMatrix cov = (new Covariance(errorSeeds)).getCovarianceMatrix();
          
        
        GaussianRandomGenerator rawGenerator = new GaussianRandomGenerator(rg);
        double[] errorMeans = new double[nObs];  
        CorrelatedRandomVectorGenerator gen = new CorrelatedRandomVectorGenerator(errorMeans, cov,
         1.0e-12 * cov.getNorm(), rawGenerator);
        
        
        
        
        
        OLSMultipleLinearRegression ols = new OLSMultipleLinearRegression();
        ols.newSampleData(longley, nObs, 6);
        final RealVector b = ols.calculateBeta().copy();
        final RealMatrix x = ols.getX().copy();
        
        
        GLSMultipleLinearRegression gls = new GLSMultipleLinearRegression();
        gls.newSampleData(longley, nObs, 6);
        gls.newCovarianceData(cov.getData());
        
        
        DescriptiveStatistics olsBetaStats = new DescriptiveStatistics();
        DescriptiveStatistics glsBetaStats = new DescriptiveStatistics();
        
        
        
        final int nModels = 10000;
        for (int i = 0; i < nModels; i++) {
            
            
            RealVector u = MatrixUtils.createRealVector(gen.nextVector());
            double[] y = u.add(x.operate(b)).toArray();
            
            
            ols.newYSampleData(y);
            RealVector olsBeta = ols.calculateBeta();
            
            
            gls.newYSampleData(y);
            RealVector glsBeta = gls.calculateBeta();
            
            
            double dist = olsBeta.getDistance(b);
            olsBetaStats.addValue(dist * dist);
            dist = glsBeta.getDistance(b);
            glsBetaStats.addValue(dist * dist);
            
        }
        
        
        assert(olsBetaStats.getMean() > 1.5 * glsBetaStats.getMean());
        assert(olsBetaStats.getStandardDeviation() > glsBetaStats.getStandardDeviation());  
    }

// org.apache.commons.math3.stat.regression.MillerUpdatingRegressionTest::testHasIntercept
    public void testHasIntercept() {
        MillerUpdatingRegression instance = new MillerUpdatingRegression(10, false);
        if (instance.hasIntercept()) {
            Assert.fail("Should not have intercept");
        }
        instance = new MillerUpdatingRegression(10, true);
        if (!instance.hasIntercept()) {
            Assert.fail("Should have intercept");
        }
    }

// org.apache.commons.math3.stat.regression.MillerUpdatingRegressionTest::testAddObsGetNClear
    public void testAddObsGetNClear() {
        MillerUpdatingRegression instance = new MillerUpdatingRegression(3, true);
        double[][] xAll = new double[airdata[0].length][];
        double[] y = new double[airdata[0].length];
        for (int i = 0; i < airdata[0].length; i++) {
            xAll[i] = new double[3];
            xAll[i][0] = Math.log(airdata[3][i]);
            xAll[i][1] = Math.log(airdata[4][i]);
            xAll[i][2] = airdata[5][i];
            y[i] = Math.log(airdata[2][i]);
        }
        instance.addObservations(xAll, y);
        if (instance.getN() != xAll.length) {
            Assert.fail("Number of observations not correct in bulk addition");
        }
        instance.clear();
        for (int i = 0; i < xAll.length; i++) {
            instance.addObservation(xAll[i], y[i]);
        }
        if (instance.getN() != xAll.length) {
            Assert.fail("Number of observations not correct in drip addition");
        }
        return;
    }

// org.apache.commons.math3.stat.regression.MillerUpdatingRegressionTest::testNegativeTestAddObs
    public void testNegativeTestAddObs() {
        MillerUpdatingRegression instance = new MillerUpdatingRegression(3, true);
        try {
            instance.addObservation(new double[]{1.0}, 0.0);
            Assert.fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException iae) {
        } catch (Exception e) {
            Assert.fail("Should throw IllegalArgumentException");
        }
        try {
            instance.addObservation(new double[]{1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0}, 0.0);
            Assert.fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException iae) {
        } catch (Exception e) {
            Assert.fail("Should throw IllegalArgumentException");
        }
        try {
            instance.addObservation(new double[]{1.0, 1.0, 1.0}, 0.0);
        } catch (Exception e) {
            Assert.fail("Should throw IllegalArgumentException");
        }

        
        instance = new MillerUpdatingRegression(3, false);
        try {
            instance.addObservation(new double[]{1.0}, 0.0);
            Assert.fail("Should throw IllegalArgumentException [NOINTERCEPT]");
        } catch (IllegalArgumentException iae) {
        } catch (Exception e) {
            Assert.fail("Should throw IllegalArgumentException [NOINTERCEPT]");
        }
        try {
            instance.addObservation(new double[]{1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0}, 0.0);
            Assert.fail("Should throw IllegalArgumentException [NOINTERCEPT]");
        } catch (IllegalArgumentException iae) {
        } catch (Exception e) {
            Assert.fail("Should throw IllegalArgumentException [NOINTERCEPT]");
        }
        try {
            instance.addObservation(new double[]{1.0, 1.0, 1.0}, 0.0);
        } catch (Exception e) {
            Assert.fail("Should throw IllegalArgumentException [NOINTERCEPT]");
        }
    }

// org.apache.commons.math3.stat.regression.MillerUpdatingRegressionTest::testNegativeTestAddMultipleObs
    public void testNegativeTestAddMultipleObs() {
        MillerUpdatingRegression instance = new MillerUpdatingRegression(3, true);
        try {
            double[][] tst = {{1.0, 1.0, 1.0}, {1.20, 2.0, 2.1}};
            double[] y = {1.0};
            instance.addObservations(tst, y);

            Assert.fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException iae) {
        } catch (Exception e) {
            Assert.fail("Should throw IllegalArgumentException");
        }

        try {
            double[][] tst = {{1.0, 1.0, 1.0}, {1.20, 2.0, 2.1}};
            double[] y = {1.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0};
            instance.addObservations(tst, y);

            Assert.fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException iae) {
        } catch (Exception e) {
            Assert.fail("Should throw IllegalArgumentException");
        }
    }

// org.apache.commons.math3.stat.regression.MillerUpdatingRegressionTest::testRegressAirlineConstantExternal
    public void testRegressAirlineConstantExternal() {
        MillerUpdatingRegression instance = new MillerUpdatingRegression(4, false);
        double[][] x = new double[airdata[0].length][];
        double[] y = new double[airdata[0].length];
        for (int i = 0; i < airdata[0].length; i++) {
            x[i] = new double[4];
            x[i][0] = 1.0;
            x[i][1] = Math.log(airdata[3][i]);
            x[i][2] = Math.log(airdata[4][i]);
            x[i][3] = airdata[5][i];
            y[i] = Math.log(airdata[2][i]);
        }

        instance.addObservations(x, y);
        try {
            RegressionResults result = instance.regress();
            Assert.assertNotNull("The test case is a prototype.", result);
            TestUtils.assertEquals(
                    new double[]{9.5169, 0.8827, 0.4540, -1.6275},
                    result.getParameterEstimates(), 1e-4);

            TestUtils.assertEquals(
                    new double[]{.2292445, .0132545, .0203042, .345302},
                    result.getStdErrorOfEstimates(), 1.0e-4);

            TestUtils.assertEquals(0.01552839, result.getMeanSquareError(), 1.0e-8);
        } catch (Exception e) {
            Assert.fail("Should not throw exception but does");
        }
    }

// org.apache.commons.math3.stat.regression.MillerUpdatingRegressionTest::testRegressAirlineConstantInternal
    public void testRegressAirlineConstantInternal() {
        MillerUpdatingRegression instance = new MillerUpdatingRegression(3, true);
        double[][] x = new double[airdata[0].length][];
        double[] y = new double[airdata[0].length];
        for (int i = 0; i < airdata[0].length; i++) {
            x[i] = new double[3];
            x[i][0] = Math.log(airdata[3][i]);
            x[i][1] = Math.log(airdata[4][i]);
            x[i][2] = airdata[5][i];
            y[i] = Math.log(airdata[2][i]);
        }

        instance.addObservations(x, y);
        try {
            RegressionResults result = instance.regress();
            Assert.assertNotNull("The test case is a prototype.", result);
            TestUtils.assertEquals(
                    new double[]{9.5169, 0.8827, 0.4540, -1.6275},
                    result.getParameterEstimates(), 1e-4);

            TestUtils.assertEquals(
                    new double[]{.2292445, .0132545, .0203042, .345302},
                    result.getStdErrorOfEstimates(), 1.0e-4);

            TestUtils.assertEquals(0.9883, result.getRSquared(), 1.0e-4);
            TestUtils.assertEquals(0.01552839, result.getMeanSquareError(), 1.0e-8);
        } catch (Exception e) {
            Assert.fail("Should not throw exception but does");
        }
    }

// org.apache.commons.math3.stat.regression.MillerUpdatingRegressionTest::testFilippelli
    public void testFilippelli() {
        double[] data = new double[]{
            0.8116, -6.860120914,
            0.9072, -4.324130045,
            0.9052, -4.358625055,
            0.9039, -4.358426747,
            0.8053, -6.955852379,
            0.8377, -6.661145254,
            0.8667, -6.355462942,
            0.8809, -6.118102026,
            0.7975, -7.115148017,
            0.8162, -6.815308569,
            0.8515, -6.519993057,
            0.8766, -6.204119983,
            0.8885, -5.853871964,
            0.8859, -6.109523091,
            0.8959, -5.79832982,
            0.8913, -5.482672118,
            0.8959, -5.171791386,
            0.8971, -4.851705903,
            0.9021, -4.517126416,
            0.909, -4.143573228,
            0.9139, -3.709075441,
            0.9199, -3.499489089,
            0.8692, -6.300769497,
            0.8872, -5.953504836,
            0.89, -5.642065153,
            0.891, -5.031376979,
            0.8977, -4.680685696,
            0.9035, -4.329846955,
            0.9078, -3.928486195,
            0.7675, -8.56735134,
            0.7705, -8.363211311,
            0.7713, -8.107682739,
            0.7736, -7.823908741,
            0.7775, -7.522878745,
            0.7841, -7.218819279,
            0.7971, -6.920818754,
            0.8329, -6.628932138,
            0.8641, -6.323946875,
            0.8804, -5.991399828,
            0.7668, -8.781464495,
            0.7633, -8.663140179,
            0.7678, -8.473531488,
            0.7697, -8.247337057,
            0.77, -7.971428747,
            0.7749, -7.676129393,
            0.7796, -7.352812702,
            0.7897, -7.072065318,
            0.8131, -6.774174009,
            0.8498, -6.478861916,
            0.8741, -6.159517513,
            0.8061, -6.835647144,
            0.846, -6.53165267,
            0.8751, -6.224098421,
            0.8856, -5.910094889,
            0.8919, -5.598599459,
            0.8934, -5.290645224,
            0.894, -4.974284616,
            0.8957, -4.64454848,
            0.9047, -4.290560426,
            0.9129, -3.885055584,
            0.9209, -3.408378962,
            0.9219, -3.13200249,
            0.7739, -8.726767166,
            0.7681, -8.66695597,
            0.7665, -8.511026475,
            0.7703, -8.165388579,
            0.7702, -7.886056648,
            0.7761, -7.588043762,
            0.7809, -7.283412422,
            0.7961, -6.995678626,
            0.8253, -6.691862621,
            0.8602, -6.392544977,
            0.8809, -6.067374056,
            0.8301, -6.684029655,
            0.8664, -6.378719832,
            0.8834, -6.065855188,
            0.8898, -5.752272167,
            0.8964, -5.132414673,
            0.8963, -4.811352704,
            0.9074, -4.098269308,
            0.9119, -3.66174277,
            0.9228, -3.2644011
        };
        MillerUpdatingRegression model = new MillerUpdatingRegression(10, true);
        int off = 0;
        double[] tmp = new double[10];
        int nobs = 82;
        for (int i = 0; i < nobs; i++) {
            tmp[0] = data[off + 1];

            tmp[1] = tmp[0] * tmp[0];
            tmp[2] = tmp[0] * tmp[1];
            tmp[3] = tmp[0] * tmp[2];
            tmp[4] = tmp[0] * tmp[3];
            tmp[5] = tmp[0] * tmp[4];
            tmp[6] = tmp[0] * tmp[5];
            tmp[7] = tmp[0] * tmp[6];
            tmp[8] = tmp[0] * tmp[7];
            tmp[9] = tmp[0] * tmp[8];
            model.addObservation(tmp, data[off]);
            off += 2;
        }
        RegressionResults result = model.regress();
        double[] betaHat = result.getParameterEstimates();
        TestUtils.assertEquals(betaHat,
                new double[]{
                    -1467.48961422980,
                    -2772.17959193342,
                    -2316.37108160893,
                    -1127.97394098372,
                    -354.478233703349,
                    -75.1242017393757,
                    -10.8753180355343,
                    -1.06221498588947,
                    -0.670191154593408E-01,
                    -0.246781078275479E-02,
                    -0.402962525080404E-04
                }, 1E-5); 

        double[] se = result.getStdErrorOfEstimates();
        TestUtils.assertEquals(se,
                new double[]{
                    298.084530995537,
                    559.779865474950,
                    466.477572127796,
                    227.204274477751,
                    71.6478660875927,
                    15.2897178747400,
                    2.23691159816033,
                    0.221624321934227,
                    0.142363763154724E-01,
                    0.535617408889821E-03,
                    0.896632837373868E-05
                }, 1E-5); 

        TestUtils.assertEquals(0.996727416185620, result.getRSquared(), 1.0e-8);
        TestUtils.assertEquals(0.112091743968020E-04, result.getMeanSquareError(), 1.0e-10);
        TestUtils.assertEquals(0.795851382172941E-03, result.getErrorSumSquares(), 1.0e-10);

    }

// org.apache.commons.math3.stat.regression.MillerUpdatingRegressionTest::testWampler1
    public void testWampler1() {
        double[] data = new double[]{
            1, 0,
            6, 1,
            63, 2,
            364, 3,
            1365, 4,
            3906, 5,
            9331, 6,
            19608, 7,
            37449, 8,
            66430, 9,
            111111, 10,
            177156, 11,
            271453, 12,
            402234, 13,
            579195, 14,
            813616, 15,
            1118481, 16,
            1508598, 17,
            2000719, 18,
            2613660, 19,
            3368421, 20};

        MillerUpdatingRegression model = new MillerUpdatingRegression(5, true);
        int off = 0;
        double[] tmp = new double[5];
        int nobs = 21;
        for (int i = 0; i < nobs; i++) {
            tmp[0] = data[off + 1];
            tmp[1] = tmp[0] * tmp[0];
            tmp[2] = tmp[0] * tmp[1];
            tmp[3] = tmp[0] * tmp[2];
            tmp[4] = tmp[0] * tmp[3];
            model.addObservation(tmp, data[off]);
            off += 2;
        }
        RegressionResults result = model.regress();
        double[] betaHat = result.getParameterEstimates();
        TestUtils.assertEquals(betaHat,
                new double[]{1.0,
                    1.0, 1.0,
                    1.0, 1.0,
                    1.0}, 1E-8); 

        double[] se = result.getStdErrorOfEstimates();
        TestUtils.assertEquals(se,
                new double[]{0.0,
                    0.0, 0.0,
                    0.0, 0.0,
                    0.0}, 1E-8); 

        TestUtils.assertEquals(1.0, result.getRSquared(), 1.0e-10);
        TestUtils.assertEquals(0, result.getMeanSquareError(), 1.0e-7);
        TestUtils.assertEquals(0.00, result.getErrorSumSquares(), 1.0e-6);

        return;
    }

// org.apache.commons.math3.stat.regression.MillerUpdatingRegressionTest::testWampler2
    public void testWampler2() {
        double[] data = new double[]{
            1.00000, 0,
            1.11111, 1,
            1.24992, 2,
            1.42753, 3,
            1.65984, 4,
            1.96875, 5,
            2.38336, 6,
            2.94117, 7,
            3.68928, 8,
            4.68559, 9,
            6.00000, 10,
            7.71561, 11,
            9.92992, 12,
            12.75603, 13,
            16.32384, 14,
            20.78125, 15,
            26.29536, 16,
            33.05367, 17,
            41.26528, 18,
            51.16209, 19,
            63.00000, 20};

        MillerUpdatingRegression model = new MillerUpdatingRegression(5, true);
        int off = 0;
        double[] tmp = new double[5];
        int nobs = 21;
        for (int i = 0; i < nobs; i++) {
            tmp[0] = data[off + 1];
            tmp[1] = tmp[0] * tmp[0];
            tmp[2] = tmp[0] * tmp[1];
            tmp[3] = tmp[0] * tmp[2];
            tmp[4] = tmp[0] * tmp[3];
            model.addObservation(tmp, data[off]);
            off += 2;
        }
        RegressionResults result = model.regress();
        double[] betaHat = result.getParameterEstimates();
        TestUtils.assertEquals(betaHat,
                new double[]{1.0,
                    1.0e-1, 1.0e-2,
                    1.0e-3, 1.0e-4,
                    1.0e-5}, 1E-8); 

        double[] se = result.getStdErrorOfEstimates();
        TestUtils.assertEquals(se,
                new double[]{0.0,
                    0.0, 0.0,
                    0.0, 0.0,
                    0.0}, 1E-8); 

        TestUtils.assertEquals(1.0, result.getRSquared(), 1.0e-10);
        TestUtils.assertEquals(0, result.getMeanSquareError(), 1.0e-7);
        TestUtils.assertEquals(0.00, result.getErrorSumSquares(), 1.0e-6);
        return;
    }

// org.apache.commons.math3.stat.regression.MillerUpdatingRegressionTest::testWampler3
    public void testWampler3() {
        double[] data = new double[]{
            760, 0,
            -2042, 1,
            2111, 2,
            -1684, 3,
            3888, 4,
            1858, 5,
            11379, 6,
            17560, 7,
            39287, 8,
            64382, 9,
            113159, 10,
            175108, 11,
            273291, 12,
            400186, 13,
            581243, 14,
            811568, 15,
            1121004, 16,
            1506550, 17,
            2002767, 18,
            2611612, 19,
            3369180, 20};
        MillerUpdatingRegression model = new MillerUpdatingRegression(5, true);
        int off = 0;
        double[] tmp = new double[5];
        int nobs = 21;
        for (int i = 0; i < nobs; i++) {
            tmp[0] = data[off + 1];
            tmp[1] = tmp[0] * tmp[0];
            tmp[2] = tmp[0] * tmp[1];
            tmp[3] = tmp[0] * tmp[2];
            tmp[4] = tmp[0] * tmp[3];
            model.addObservation(tmp, data[off]);
            off += 2;
        }
        RegressionResults result = model.regress();
        double[] betaHat = result.getParameterEstimates();
        TestUtils.assertEquals(betaHat,
                new double[]{1.0,
                    1.0, 1.0,
                    1.0, 1.0,
                    1.0}, 1E-8); 
        double[] se = result.getStdErrorOfEstimates();
        TestUtils.assertEquals(se,
                new double[]{2152.32624678170,
                    2363.55173469681, 779.343524331583,
                    101.475507550350, 5.64566512170752,
                    0.112324854679312}, 1E-8); 

        TestUtils.assertEquals(.999995559025820, result.getRSquared(), 1.0e-10);
        TestUtils.assertEquals(5570284.53333333, result.getMeanSquareError(), 1.0e-7);
        TestUtils.assertEquals(83554268.0000000, result.getErrorSumSquares(), 1.0e-6);
        return;
    }

// org.apache.commons.math3.stat.regression.MillerUpdatingRegressionTest::testWampler4
    public void testWampler4() {
        double[] data = new double[]{
            75901, 0,
            -204794, 1,
            204863, 2,
            -204436, 3,
            253665, 4,
            -200894, 5,
            214131, 6,
            -185192, 7,
            221249, 8,
            -138370, 9,
            315911, 10,
            -27644, 11,
            455253, 12,
            197434, 13,
            783995, 14,
            608816, 15,
            1370781, 16,
            1303798, 17,
            2205519, 18,
            2408860, 19,
            3444321, 20};
        MillerUpdatingRegression model = new MillerUpdatingRegression(5, true);
        int off = 0;
        double[] tmp = new double[5];
        int nobs = 21;
        for (int i = 0; i < nobs; i++) {
            tmp[0] = data[off + 1];
            tmp[1] = tmp[0] * tmp[0];
            tmp[2] = tmp[0] * tmp[1];
            tmp[3] = tmp[0] * tmp[2];
            tmp[4] = tmp[0] * tmp[3];
            model.addObservation(tmp, data[off]);
            off += 2;
        }
        RegressionResults result = model.regress();
        double[] betaHat = result.getParameterEstimates();
        TestUtils.assertEquals(betaHat,
                new double[]{1.0,
                    1.0, 1.0,
                    1.0, 1.0,
                    1.0}, 1E-8); 

        double[] se = result.getStdErrorOfEstimates();
        TestUtils.assertEquals(se,
                new double[]{215232.624678170,
                    236355.173469681, 77934.3524331583,
                    10147.5507550350, 564.566512170752,
                    11.2324854679312}, 1E-8); 

        TestUtils.assertEquals(.957478440825662, result.getRSquared(), 1.0e-10);
        TestUtils.assertEquals(55702845333.3333, result.getMeanSquareError(), 1.0e-4);
        TestUtils.assertEquals(835542680000.000, result.getErrorSumSquares(), 1.0e-3);

        return;
    }

// org.apache.commons.math3.stat.regression.MillerUpdatingRegressionTest::testLongly
    public void testLongly() {
        
        
        double[] design = new double[]{
            60323, 83.0, 234289, 2356, 1590, 107608, 1947,
            61122, 88.5, 259426, 2325, 1456, 108632, 1948,
            60171, 88.2, 258054, 3682, 1616, 109773, 1949,
            61187, 89.5, 284599, 3351, 1650, 110929, 1950,
            63221, 96.2, 328975, 2099, 3099, 112075, 1951,
            63639, 98.1, 346999, 1932, 3594, 113270, 1952,
            64989, 99.0, 365385, 1870, 3547, 115094, 1953,
            63761, 100.0, 363112, 3578, 3350, 116219, 1954,
            66019, 101.2, 397469, 2904, 3048, 117388, 1955,
            67857, 104.6, 419180, 2822, 2857, 118734, 1956,
            68169, 108.4, 442769, 2936, 2798, 120445, 1957,
            66513, 110.8, 444546, 4681, 2637, 121950, 1958,
            68655, 112.6, 482704, 3813, 2552, 123366, 1959,
            69564, 114.2, 502601, 3931, 2514, 125368, 1960,
            69331, 115.7, 518173, 4806, 2572, 127852, 1961,
            70551, 116.9, 554894, 4007, 2827, 130081, 1962
        };

        final int nobs = 16;
        final int nvars = 6;

        
        MillerUpdatingRegression model = new MillerUpdatingRegression(6, true);
        int off = 0;
        double[] tmp = new double[6];
        for (int i = 0; i < nobs; i++) {
            System.arraycopy(design, off + 1, tmp, 0, nvars);
            model.addObservation(tmp, design[off]);
            off += nvars + 1;
        }

        
        RegressionResults result = model.regress();
        double[] betaHat = result.getParameterEstimates();
        TestUtils.assertEquals(betaHat,
                new double[]{-3482258.63459582, 15.0618722713733,
                    -0.358191792925910E-01, -2.02022980381683,
                    -1.03322686717359, -0.511041056535807E-01,
                    1829.15146461355}, 1E-8); 

        
        double[] errors = result.getStdErrorOfEstimates();
        TestUtils.assertEquals(new double[]{890420.383607373,
                    84.9149257747669,
                    0.334910077722432E-01,
                    0.488399681651699,
                    0.214274163161675,
                    0.226073200069370,
                    455.478499142212}, errors, 1E-6);

        
        TestUtils.assertEquals(0.995479004577296, result.getRSquared(), 1E-12);
        TestUtils.assertEquals(0.992465007628826, result.getAdjustedRSquared(), 1E-12);

        model = new MillerUpdatingRegression(6, false);
        off = 0;
        for (int i = 0; i < nobs; i++) {
            System.arraycopy(design, off + 1, tmp, 0, nvars);
            model.addObservation(tmp, design[off]);
            off += nvars + 1;
        }
        
        result = model.regress();
        betaHat = result.getParameterEstimates();
        TestUtils.assertEquals(betaHat,
                new double[]{-52.99357013868291, 0.07107319907358,
                    -0.42346585566399, -0.57256866841929,
                    -0.41420358884978, 48.41786562001326}, 1E-11);

        
        errors = result.getStdErrorOfEstimates();
        TestUtils.assertEquals(new double[]{129.54486693117232, 0.03016640003786,
                    0.41773654056612, 0.27899087467676, 0.32128496193363,
                    17.68948737819961}, errors, 1E-11);

        TestUtils.assertEquals(0.9999670130706, result.getRSquared(), 1E-12);
        TestUtils.assertEquals(0.999947220913, result.getAdjustedRSquared(), 1E-12);

    }

// org.apache.commons.math3.stat.regression.MillerUpdatingRegressionTest::testOneRedundantColumn
    public void testOneRedundantColumn() {
        MillerUpdatingRegression instance = new MillerUpdatingRegression(4, false);
        MillerUpdatingRegression instance2 = new MillerUpdatingRegression(5, false);
        double[][] x = new double[airdata[0].length][];
        double[][] x2 = new double[airdata[0].length][];
        double[] y = new double[airdata[0].length];
        for (int i = 0; i < airdata[0].length; i++) {
            x[i] = new double[4];
            x2[i] = new double[5];
            x[i][0] = 1.0;
            x[i][1] = Math.log(airdata[3][i]);
            x[i][2] = Math.log(airdata[4][i]);
            x[i][3] = airdata[5][i];

            x2[i][0] = x[i][0];
            x2[i][1] = x[i][1];
            x2[i][2] = x[i][2];
            x2[i][3] = x[i][3];
            x2[i][4] = x[i][3];

            y[i] = Math.log(airdata[2][i]);
        }

        instance.addObservations(x, y);
        RegressionResults result = instance.regress();
        Assert.assertNotNull("Could not estimate initial regression", result);

        instance2.addObservations(x2, y);
        RegressionResults resultRedundant = instance2.regress();
        Assert.assertNotNull("Could not estimate redundant regression", resultRedundant);
        double[] beta = result.getParameterEstimates();
        double[] betar = resultRedundant.getParameterEstimates();
        double[] se = result.getStdErrorOfEstimates();
        double[] ser = resultRedundant.getStdErrorOfEstimates();

        for (int i = 0; i < beta.length; i++) {
            if (Math.abs(beta[i] - betar[i]) > 1.0e-8) {
                Assert.fail("Parameters not correctly estimated");
            }
            if (Math.abs(se[i] - ser[i]) > 1.0e-8) {
                Assert.fail("Standard errors not correctly estimated");
            }
            for (int j = 0; j < i; j++) {
                if (Math.abs(result.getCovarianceOfParameters(i, j)
                        - resultRedundant.getCovarianceOfParameters(i, j)) > 1.0e-8) {
                    Assert.fail("Variance Covariance not correct");
                }
            }
        }

        TestUtils.assertEquals(result.getAdjustedRSquared(), resultRedundant.getAdjustedRSquared(), 1.0e-8);
        TestUtils.assertEquals(result.getErrorSumSquares(), resultRedundant.getErrorSumSquares(), 1.0e-8);
        TestUtils.assertEquals(result.getMeanSquareError(), resultRedundant.getMeanSquareError(), 1.0e-8);
        TestUtils.assertEquals(result.getRSquared(), resultRedundant.getRSquared(), 1.0e-8);
        return;
    }

// org.apache.commons.math3.stat.regression.MillerUpdatingRegressionTest::testThreeRedundantColumn
    public void testThreeRedundantColumn() {

        MillerUpdatingRegression instance = new MillerUpdatingRegression(4, false);
        MillerUpdatingRegression instance2 = new MillerUpdatingRegression(7, false);
        double[][] x = new double[airdata[0].length][];
        double[][] x2 = new double[airdata[0].length][];
        double[] y = new double[airdata[0].length];
        for (int i = 0; i < airdata[0].length; i++) {
            x[i] = new double[4];
            x2[i] = new double[7];
            x[i][0] = 1.0;
            x[i][1] = Math.log(airdata[3][i]);
            x[i][2] = Math.log(airdata[4][i]);
            x[i][3] = airdata[5][i];

            x2[i][0] = x[i][0];
            x2[i][1] = x[i][0];
            x2[i][2] = x[i][1];
            x2[i][3] = x[i][2];
            x2[i][4] = x[i][1];
            x2[i][5] = x[i][3];
            x2[i][6] = x[i][2];

            y[i] = Math.log(airdata[2][i]);
        }

        instance.addObservations(x, y);
        RegressionResults result = instance.regress();
        Assert.assertNotNull("Could not estimate initial regression", result);

        instance2.addObservations(x2, y);
        RegressionResults resultRedundant = instance2.regress();
        Assert.assertNotNull("Could not estimate redundant regression", resultRedundant);
        double[] beta = result.getParameterEstimates();
        double[] betar = resultRedundant.getParameterEstimates();
        double[] se = result.getStdErrorOfEstimates();
        double[] ser = resultRedundant.getStdErrorOfEstimates();

        if (Math.abs(beta[0] - betar[0]) > 1.0e-8) {
            Assert.fail("Parameters not correct after reorder (0,3)");
        }
        if (Math.abs(beta[1] - betar[2]) > 1.0e-8) {
            Assert.fail("Parameters not correct after reorder (1,2)");
        }
        if (Math.abs(beta[2] - betar[3]) > 1.0e-8) {
            Assert.fail("Parameters not correct after reorder (2,1)");
        }
        if (Math.abs(beta[3] - betar[5]) > 1.0e-8) {
            Assert.fail("Parameters not correct after reorder (3,0)");
        }

        if (Math.abs(se[0] - ser[0]) > 1.0e-8) {
            Assert.fail("Se not correct after reorder (0,3)");
        }
        if (Math.abs(se[1] - ser[2]) > 1.0e-8) {
            Assert.fail("Se not correct after reorder (1,2)");
        }
        if (Math.abs(se[2] - ser[3]) > 1.0e-8) {
            Assert.fail("Se not correct after reorder (2,1)");
        }
        if (Math.abs(se[3] - ser[5]) > 1.0e-8) {
            Assert.fail("Se not correct after reorder (3,0)");
        }

        if (Math.abs(result.getCovarianceOfParameters(0, 0)
                - resultRedundant.getCovarianceOfParameters(0, 0)) > 1.0e-8) {
            Assert.fail("VCV not correct after reorder (0,0)");
        }
        if (Math.abs(result.getCovarianceOfParameters(0, 1)
                - resultRedundant.getCovarianceOfParameters(0, 2)) > 1.0e-8) {
            Assert.fail("VCV not correct after reorder (0,1)<->(0,2)");
        }
        if (Math.abs(result.getCovarianceOfParameters(0, 2)
                - resultRedundant.getCovarianceOfParameters(0, 3)) > 1.0e-8) {
            Assert.fail("VCV not correct after reorder (0,2)<->(0,1)");
        }
        if (Math.abs(result.getCovarianceOfParameters(0, 3)
                - resultRedundant.getCovarianceOfParameters(0, 5)) > 1.0e-8) {
            Assert.fail("VCV not correct after reorder (0,3)<->(0,3)");
        }
        if (Math.abs(result.getCovarianceOfParameters(1, 0)
                - resultRedundant.getCovarianceOfParameters(2, 0)) > 1.0e-8) {
            Assert.fail("VCV not correct after reorder (1,0)<->(2,0)");
        }
        if (Math.abs(result.getCovarianceOfParameters(1, 1)
                - resultRedundant.getCovarianceOfParameters(2, 2)) > 1.0e-8) {
            Assert.fail("VCV not correct  (1,1)<->(2,1)");
        }
        if (Math.abs(result.getCovarianceOfParameters(1, 2)
                - resultRedundant.getCovarianceOfParameters(2, 3)) > 1.0e-8) {
            Assert.fail("VCV not correct  (1,2)<->(2,2)");
        }

        if (Math.abs(result.getCovarianceOfParameters(2, 0)
                - resultRedundant.getCovarianceOfParameters(3, 0)) > 1.0e-8) {
            Assert.fail("VCV not correct  (2,0)<->(1,0)");
        }
        if (Math.abs(result.getCovarianceOfParameters(2, 1)
                - resultRedundant.getCovarianceOfParameters(3, 2)) > 1.0e-8) {
            Assert.fail("VCV not correct  (2,1)<->(1,2)");
        }

        if (Math.abs(result.getCovarianceOfParameters(3, 3)
                - resultRedundant.getCovarianceOfParameters(5, 5)) > 1.0e-8) {
            Assert.fail("VCV not correct  (3,3)<->(3,2)");
        }

        TestUtils.assertEquals(result.getAdjustedRSquared(), resultRedundant.getAdjustedRSquared(), 1.0e-8);
        TestUtils.assertEquals(result.getErrorSumSquares(), resultRedundant.getErrorSumSquares(), 1.0e-8);
        TestUtils.assertEquals(result.getMeanSquareError(), resultRedundant.getMeanSquareError(), 1.0e-8);
        TestUtils.assertEquals(result.getRSquared(), resultRedundant.getRSquared(), 1.0e-8);
        return;
    }

// org.apache.commons.math3.stat.regression.MillerUpdatingRegressionTest::testPCorr
    public void testPCorr() {
        MillerUpdatingRegression instance = new MillerUpdatingRegression(4, false);
        double[][] x = new double[airdata[0].length][];
        double[] y = new double[airdata[0].length];
        double[] cp = new double[10];
        double[] yxcorr = new double[4];
        double[] diag = new double[4];
        double sumysq = 0.0;
        int off = 0;
        for (int i = 0; i < airdata[0].length; i++) {
            x[i] = new double[4];
            x[i][0] = 1.0;
            x[i][1] = Math.log(airdata[3][i]);
            x[i][2] = Math.log(airdata[4][i]);
            x[i][3] = airdata[5][i];
            y[i] = Math.log(airdata[2][i]);
            off = 0;
            for (int j = 0; j < 4; j++) {
                double tmp = x[i][j];
                for (int k = 0; k <= j; k++, off++) {
                    cp[off] += tmp * x[i][k];
                }
                yxcorr[j] += tmp * y[i];
            }
            sumysq += y[i] * y[i];
        }
        PearsonsCorrelation pearson = new PearsonsCorrelation(x);
        RealMatrix corr = pearson.getCorrelationMatrix();
        off = 0;
        for (int i = 0; i < 4; i++, off += (i + 1)) {
            diag[i] = FastMath.sqrt(cp[off]);
        }

        instance.addObservations(x, y);
        double[] pc = instance.getPartialCorrelations(0);
        int idx = 0;
        off = 0;
        int off2 = 6;
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < i; j++) {
                if (Math.abs(pc[idx] - cp[off] / (diag[i] * diag[j])) > 1.0e-8) {
                    Assert.fail("Failed cross products... i = " + i + " j = " + j);
                }
                ++idx;
                ++off;
            }
            ++off;
            if (Math.abs(pc[i+off2] - yxcorr[ i] / (FastMath.sqrt(sumysq) * diag[i])) > 1.0e-8) {
                Assert.fail("Assert.failed cross product i = " + i + " y");
            }
        }
        double[] pc2 = instance.getPartialCorrelations(1);

        idx = 0;

        for (int i = 1; i < 4; i++) {
            for (int j = 1; j < i; j++) {
                if (Math.abs(pc2[idx] - corr.getEntry(j, i)) > 1.0e-8) {
                    Assert.fail("Failed cross products... i = " + i + " j = " + j);
                }
                ++idx;
            }
        }
        double[] pc3 = instance.getPartialCorrelations(2);
        if (pc3 == null) {
            Assert.fail("Should not be null");
        }
        return;
    }

// org.apache.commons.math3.stat.regression.MillerUpdatingRegressionTest::testHdiag
    public void testHdiag() {
        MillerUpdatingRegression instance = new MillerUpdatingRegression(4, false);
        double[][] x = new double[airdata[0].length][];
        double[] y = new double[airdata[0].length];
        for (int i = 0; i < airdata[0].length; i++) {
            x[i] = new double[4];
            x[i][0] = 1.0;
            x[i][1] = Math.log(airdata[3][i]);
            x[i][2] = Math.log(airdata[4][i]);
            x[i][3] = airdata[5][i];
            y[i] = Math.log(airdata[2][i]);
        }
        instance.addObservations(x, y);
        OLSMultipleLinearRegression ols = new OLSMultipleLinearRegression();
        ols.setNoIntercept(true);
        ols.newSampleData(y, x);

        RealMatrix rm = ols.calculateHat();
        for (int i = 0; i < x.length; i++) {
            TestUtils.assertEquals(instance.getDiagonalOfHatMatrix(x[i]), rm.getEntry(i, i), 1.0e-8);
        }
        return;
    }

// org.apache.commons.math3.stat.regression.MillerUpdatingRegressionTest::testHdiagConstant
    public void testHdiagConstant() {
        MillerUpdatingRegression instance = new MillerUpdatingRegression(3, true);
        double[][] x = new double[airdata[0].length][];
        double[] y = new double[airdata[0].length];
        for (int i = 0; i < airdata[0].length; i++) {
            x[i] = new double[3];
            x[i][0] = Math.log(airdata[3][i]);
            x[i][1] = Math.log(airdata[4][i]);
            x[i][2] = airdata[5][i];
            y[i] = Math.log(airdata[2][i]);
        }
        instance.addObservations(x, y);
        OLSMultipleLinearRegression ols = new OLSMultipleLinearRegression();
        ols.setNoIntercept(false);
        ols.newSampleData(y, x);

        RealMatrix rm = ols.calculateHat();
        for (int i = 0; i < x.length; i++) {
            TestUtils.assertEquals(instance.getDiagonalOfHatMatrix(x[i]), rm.getEntry(i, i), 1.0e-8);
        }
        return;
    }

// org.apache.commons.math3.stat.regression.MillerUpdatingRegressionTest::testSubsetRegression
    public void testSubsetRegression() {
        
        MillerUpdatingRegression instance = new MillerUpdatingRegression(3, true);
        MillerUpdatingRegression redRegression = new MillerUpdatingRegression(2, true);
        double[][] x = new double[airdata[0].length][];
        double[][] xReduced = new double[airdata[0].length][];
        double[] y = new double[airdata[0].length];
        for (int i = 0; i < airdata[0].length; i++) {
            x[i] = new double[3];
            x[i][0] = Math.log(airdata[3][i]);
            x[i][1] = Math.log(airdata[4][i]);
            x[i][2] = airdata[5][i];
            
            xReduced[i] = new double[2];
            xReduced[i][0] = Math.log(airdata[3][i]);
            xReduced[i][1] = Math.log(airdata[4][i]);
            
            y[i] = Math.log(airdata[2][i]);
        }

        instance.addObservations(x, y);
        redRegression.addObservations(xReduced, y);
        
        RegressionResults resultsInstance = instance.regress( new int[]{0,1,2} );
        RegressionResults resultsReduced = redRegression.regress();
        
        TestUtils.assertEquals(resultsInstance.getParameterEstimates(), resultsReduced.getParameterEstimates(), 1.0e-12);
        TestUtils.assertEquals(resultsInstance.getStdErrorOfEstimates(), resultsReduced.getStdErrorOfEstimates(), 1.0e-12);
    }

// org.apache.commons.math3.stat.regression.OLSMultipleLinearRegressionTest::cannotAddSampleDataWithSizeMismatch
    public void cannotAddSampleDataWithSizeMismatch() {
        double[] y = new double[]{1.0, 2.0};
        double[][] x = new double[1][];
        x[0] = new double[]{1.0, 0};
        createRegression().newSampleData(y, x);
    }

// org.apache.commons.math3.stat.regression.OLSMultipleLinearRegressionTest::testPerfectFit
    public void testPerfectFit() {
        double[] betaHat = regression.estimateRegressionParameters();
        TestUtils.assertEquals(betaHat,
                               new double[]{ 11.0, 1.0 / 2.0, 2.0 / 3.0, 3.0 / 4.0, 4.0 / 5.0, 5.0 / 6.0 },
                               1e-14);
        double[] residuals = regression.estimateResiduals();
        TestUtils.assertEquals(residuals, new double[]{0d,0d,0d,0d,0d,0d},
                               1e-14);
        RealMatrix errors =
            new Array2DRowRealMatrix(regression.estimateRegressionParametersVariance(), false);
        final double[] s = { 1.0, -1.0 /  2.0, -1.0 /  3.0, -1.0 /  4.0, -1.0 /  5.0, -1.0 /  6.0 };
        RealMatrix referenceVariance = new Array2DRowRealMatrix(s.length, s.length);
        referenceVariance.walkInOptimizedOrder(new DefaultRealMatrixChangingVisitor() {
            @Override
            public double visit(int row, int column, double value) {
                if (row == 0) {
                    return s[column];
                }
                double x = s[row] * s[column];
                return (row == column) ? 2 * x : x;
            }
        });
       Assert.assertEquals(0.0,
                     errors.subtract(referenceVariance).getNorm(),
                     5.0e-16 * referenceVariance.getNorm());
       Assert.assertEquals(1, ((OLSMultipleLinearRegression) regression).calculateRSquared(), 1E-12);
    }

// org.apache.commons.math3.stat.regression.OLSMultipleLinearRegressionTest::testLongly
    public void testLongly() {
        
        
        double[] design = new double[] {
            60323,83.0,234289,2356,1590,107608,1947,
            61122,88.5,259426,2325,1456,108632,1948,
            60171,88.2,258054,3682,1616,109773,1949,
            61187,89.5,284599,3351,1650,110929,1950,
            63221,96.2,328975,2099,3099,112075,1951,
            63639,98.1,346999,1932,3594,113270,1952,
            64989,99.0,365385,1870,3547,115094,1953,
            63761,100.0,363112,3578,3350,116219,1954,
            66019,101.2,397469,2904,3048,117388,1955,
            67857,104.6,419180,2822,2857,118734,1956,
            68169,108.4,442769,2936,2798,120445,1957,
            66513,110.8,444546,4681,2637,121950,1958,
            68655,112.6,482704,3813,2552,123366,1959,
            69564,114.2,502601,3931,2514,125368,1960,
            69331,115.7,518173,4806,2572,127852,1961,
            70551,116.9,554894,4007,2827,130081,1962
        };

        final int nobs = 16;
        final int nvars = 6;

        
        OLSMultipleLinearRegression model = new OLSMultipleLinearRegression();
        model.newSampleData(design, nobs, nvars);

        
        double[] betaHat = model.estimateRegressionParameters();
        TestUtils.assertEquals(betaHat,
          new double[]{-3482258.63459582, 15.0618722713733,
                -0.358191792925910E-01,-2.02022980381683,
                -1.03322686717359,-0.511041056535807E-01,
                 1829.15146461355}, 2E-8); 

        
        double[] residuals = model.estimateResiduals();
        TestUtils.assertEquals(residuals, new double[]{
                267.340029759711,-94.0139423988359,46.28716775752924,
                -410.114621930906,309.7145907602313,-249.3112153297231,
                -164.0489563956039,-13.18035686637081,14.30477260005235,
                 455.394094551857,-17.26892711483297,-39.0550425226967,
                -155.5499735953195,-85.6713080421283,341.9315139607727,
                -206.7578251937366},
                      1E-8);

        
        double[] errors = model.estimateRegressionParametersStandardErrors();
        TestUtils.assertEquals(new double[] {890420.383607373,
                       84.9149257747669,
                       0.334910077722432E-01,
                       0.488399681651699,
                       0.214274163161675,
                       0.226073200069370,
                       455.478499142212}, errors, 1E-6);
        
        
        Assert.assertEquals(304.8540735619638, model.estimateRegressionStandardError(), 1E-10);
        
        
        Assert.assertEquals(0.995479004577296, model.calculateRSquared(), 1E-12);
        Assert.assertEquals(0.992465007628826, model.calculateAdjustedRSquared(), 1E-12);
        
        checkVarianceConsistency(model);
        
        
        model.setNoIntercept(true);
        model.newSampleData(design, nobs, nvars);
        
        
        betaHat = model.estimateRegressionParameters();
        TestUtils.assertEquals(betaHat,
          new double[]{-52.99357013868291, 0.07107319907358,
                -0.42346585566399,-0.57256866841929,
                -0.41420358884978, 48.41786562001326}, 1E-11); 
        
        
        errors = model.estimateRegressionParametersStandardErrors();
        TestUtils.assertEquals(new double[] {129.54486693117232, 0.03016640003786,
                0.41773654056612, 0.27899087467676, 0.32128496193363,
                17.68948737819961}, errors, 1E-11);
        
        
        residuals = model.estimateResiduals();
        TestUtils.assertEquals(residuals, new double[]{
                279.90274927293092, -130.32465380836874, 90.73228661967445, -401.31252201634948,
                -440.46768772620027, -543.54512853774793, 201.32111639536299, 215.90889365977932,
                73.09368242049943, 913.21694494481869, 424.82484953610174, -8.56475876776709,
                -361.32974610842876, 27.34560497213464, 151.28955976355002, -492.49937355336846},
                      1E-10);
        
        
        Assert.assertEquals(475.1655079819517, model.estimateRegressionStandardError(), 1E-10);
        
        
        Assert.assertEquals(0.9999670130706, model.calculateRSquared(), 1E-12);
        Assert.assertEquals(0.999947220913, model.calculateAdjustedRSquared(), 1E-12);
         
    }

// org.apache.commons.math3.stat.regression.OLSMultipleLinearRegressionTest::testSwissFertility
    public void testSwissFertility() {
        double[] design = new double[] {
            80.2,17.0,15,12,9.96,
            83.1,45.1,6,9,84.84,
            92.5,39.7,5,5,93.40,
            85.8,36.5,12,7,33.77,
            76.9,43.5,17,15,5.16,
            76.1,35.3,9,7,90.57,
            83.8,70.2,16,7,92.85,
            92.4,67.8,14,8,97.16,
            82.4,53.3,12,7,97.67,
            82.9,45.2,16,13,91.38,
            87.1,64.5,14,6,98.61,
            64.1,62.0,21,12,8.52,
            66.9,67.5,14,7,2.27,
            68.9,60.7,19,12,4.43,
            61.7,69.3,22,5,2.82,
            68.3,72.6,18,2,24.20,
            71.7,34.0,17,8,3.30,
            55.7,19.4,26,28,12.11,
            54.3,15.2,31,20,2.15,
            65.1,73.0,19,9,2.84,
            65.5,59.8,22,10,5.23,
            65.0,55.1,14,3,4.52,
            56.6,50.9,22,12,15.14,
            57.4,54.1,20,6,4.20,
            72.5,71.2,12,1,2.40,
            74.2,58.1,14,8,5.23,
            72.0,63.5,6,3,2.56,
            60.5,60.8,16,10,7.72,
            58.3,26.8,25,19,18.46,
            65.4,49.5,15,8,6.10,
            75.5,85.9,3,2,99.71,
            69.3,84.9,7,6,99.68,
            77.3,89.7,5,2,100.00,
            70.5,78.2,12,6,98.96,
            79.4,64.9,7,3,98.22,
            65.0,75.9,9,9,99.06,
            92.2,84.6,3,3,99.46,
            79.3,63.1,13,13,96.83,
            70.4,38.4,26,12,5.62,
            65.7,7.7,29,11,13.79,
            72.7,16.7,22,13,11.22,
            64.4,17.6,35,32,16.92,
            77.6,37.6,15,7,4.97,
            67.6,18.7,25,7,8.65,
            35.0,1.2,37,53,42.34,
            44.7,46.6,16,29,50.43,
            42.8,27.7,22,29,58.33
        };
        
        final int nobs = 47;
        final int nvars = 4;

        
        OLSMultipleLinearRegression model = new OLSMultipleLinearRegression();
        model.newSampleData(design, nobs, nvars);

        
        double[] betaHat = model.estimateRegressionParameters();
        TestUtils.assertEquals(betaHat,
                new double[]{91.05542390271397,
                -0.22064551045715,
                -0.26058239824328,
                -0.96161238456030,
                 0.12441843147162}, 1E-12);

        
        double[] residuals = model.estimateResiduals();
        TestUtils.assertEquals(residuals, new double[]{
                7.1044267859730512,1.6580347433531366,
                4.6944952770029644,8.4548022690166160,13.6547432343186212,
               -9.3586864458500774,7.5822446330520386,15.5568995563859289,
                0.8113090736598980,7.1186762732484308,7.4251378771228724,
                2.6761316873234109,0.8351584810309354,7.1769991119615177,
               -3.8746753206299553,-3.1337779476387251,-0.1412575244091504,
                1.1186809170469780,-6.3588097346816594,3.4039270429434074,
                2.3374058329820175,-7.9272368576900503,-7.8361010968497959,
               -11.2597369269357070,0.9445333697827101,6.6544245101380328,
               -0.9146136301118665,-4.3152449403848570,-4.3536932047009183,
               -3.8907885169304661,-6.3027643926302188,-7.8308982189289091,
               -3.1792280015332750,-6.7167298771158226,-4.8469946718041754,
               -10.6335664353633685,11.1031134362036958,6.0084032641811733,
                5.4326230830188482,-7.2375578629692230,2.1671550814448222,
                15.0147574652763112,4.8625103516321015,-7.1597256413907706,
                -0.4515205619767598,-10.2916870903837587,-15.7812984571900063},
                1E-12);

        
        double[] errors = model.estimateRegressionParametersStandardErrors();
        TestUtils.assertEquals(new double[] {6.94881329475087,
                0.07360008972340,
                0.27410957467466,
                0.19454551679325,
                0.03726654773803}, errors, 1E-10);
        
        
        Assert.assertEquals(7.73642194433223, model.estimateRegressionStandardError(), 1E-12);
        
        
        Assert.assertEquals(0.649789742860228, model.calculateRSquared(), 1E-12);
        Assert.assertEquals(0.6164363850373927, model.calculateAdjustedRSquared(), 1E-12);
        
        checkVarianceConsistency(model);
        
        
        model = new OLSMultipleLinearRegression();
        model.setNoIntercept(true);
        model.newSampleData(design, nobs, nvars);

        
        betaHat = model.estimateRegressionParameters();
        TestUtils.assertEquals(betaHat,
                new double[]{0.52191832900513,
                  2.36588087917963,
                  -0.94770353802795, 
                  0.30851985863609}, 1E-12);

        
        residuals = model.estimateResiduals();
        TestUtils.assertEquals(residuals, new double[]{
                44.138759883538249, 27.720705122356215, 35.873200836126799, 
                34.574619581211977, 26.600168342080213, 15.074636243026923, -12.704904871199814,
                1.497443824078134, 2.691972687079431, 5.582798774291231, -4.422986561283165, 
                -9.198581600334345, 4.481765170730647, 2.273520207553216, -22.649827853221336,
                -17.747900013943308, 20.298314638496436, 6.861405135329779, -8.684712790954924,
                -10.298639278062371, -9.896618896845819, 4.568568616351242, -15.313570491727944,
                -13.762961360873966, 7.156100301980509, 16.722282219843990, 26.716200609071898,
                -1.991466398777079, -2.523342564719335, 9.776486693095093, -5.297535127628603,
                -16.639070567471094, -10.302057295211819, -23.549487860816846, 1.506624392156384,
                -17.939174438345930, 13.105792202765040, -1.943329906928462, -1.516005841666695,
                -0.759066561832886, 20.793137744128977, -2.485236153005426, 27.588238710486976,
                2.658333257106881, -15.998337823623046, -5.550742066720694, -14.219077806826615},
                1E-12);

        
        errors = model.estimateRegressionParametersStandardErrors();
        TestUtils.assertEquals(new double[] {0.10470063765677, 0.41684100584290,
                0.43370143099691, 0.07694953606522}, errors, 1E-10);
        
        
        Assert.assertEquals(17.24710630547, model.estimateRegressionStandardError(), 1E-10);
        
        
        Assert.assertEquals(0.946350722085, model.calculateRSquared(), 1E-12);
        Assert.assertEquals(0.9413600915813, model.calculateAdjustedRSquared(), 1E-12);
    }

// org.apache.commons.math3.stat.regression.OLSMultipleLinearRegressionTest::testHat
    public void testHat() {

        
        double[] design = new double[] {
                11.14, .499, 11.1,
                12.74, .558, 8.9,
                13.13, .604, 8.8,
                11.51, .441, 8.9,
                12.38, .550, 8.8,
                12.60, .528, 9.9,
                11.13, .418, 10.7,
                11.7, .480, 10.5,
                11.02, .406, 10.5,
                11.41, .467, 10.7
        };

        int nobs = 10;
        int nvars = 2;

        
        OLSMultipleLinearRegression model = new OLSMultipleLinearRegression();
        model.newSampleData(design, nobs, nvars);

        RealMatrix hat = model.calculateHat();

        
        double[] referenceData = new double[] {
                .418, -.002,  .079, -.274, -.046,  .181,  .128,  .222,  .050,  .242,
                       .242,  .292,  .136,  .243,  .128, -.041,  .033, -.035,  .004,
                              .417, -.019,  .273,  .187, -.126,  .044, -.153,  .004,
                                     .604,  .197, -.038,  .168, -.022,  .275, -.028,
                                            .252,  .111, -.030,  .019, -.010, -.010,
                                                   .148,  .042,  .117,  .012,  .111,
                                                          .262,  .145,  .277,  .174,
                                                                 .154,  .120,  .168,
                                                                        .315,  .148,
                                                                               .187
        };

        
        int k = 0;
        for (int i = 0; i < 10; i++) {
            for (int j = i; j < 10; j++) {
                Assert.assertEquals(referenceData[k], hat.getEntry(i, j), 10e-3);
                Assert.assertEquals(hat.getEntry(i, j), hat.getEntry(j, i), 10e-12);
                k++;
            }
        }

        
        double[] residuals = model.estimateResiduals();
        RealMatrix I = MatrixUtils.createRealIdentityMatrix(10);
        double[] hatResiduals = I.subtract(hat).operate(model.getY()).toArray();
        TestUtils.assertEquals(residuals, hatResiduals, 10e-12);
    }

// org.apache.commons.math3.stat.regression.OLSMultipleLinearRegressionTest::testYVariance
    public void testYVariance() {

        

        OLSMultipleLinearRegression model = new OLSMultipleLinearRegression();
        model.newSampleData(y, x);
        TestUtils.assertEquals(model.calculateYVariance(), 3.5, 0);
    }

// org.apache.commons.math3.stat.regression.OLSMultipleLinearRegressionTest::testNewSample2
    public void testNewSample2() {
        double[] y = new double[] {1, 2, 3, 4}; 
        double[][] x = new double[][] {
          {19, 22, 33},
          {20, 30, 40},
          {25, 35, 45},
          {27, 37, 47}   
        };
        OLSMultipleLinearRegression regression = new OLSMultipleLinearRegression();
        regression.newSampleData(y, x);
        RealMatrix combinedX = regression.getX().copy();
        RealVector combinedY = regression.getY().copy();
        regression.newXSampleData(x);
        regression.newYSampleData(y);
        Assert.assertEquals(combinedX, regression.getX());
        Assert.assertEquals(combinedY, regression.getY());
        
        
        regression.setNoIntercept(true);
        regression.newSampleData(y, x);
        combinedX = regression.getX().copy();
        combinedY = regression.getY().copy();
        regression.newXSampleData(x);
        regression.newYSampleData(y);
        Assert.assertEquals(combinedX, regression.getX());
        Assert.assertEquals(combinedY, regression.getY());
    }

// org.apache.commons.math3.stat.regression.OLSMultipleLinearRegressionTest::testNewSampleDataYNull
    public void testNewSampleDataYNull() {
        createRegression().newSampleData(null, new double[][] {});
    }

// org.apache.commons.math3.stat.regression.OLSMultipleLinearRegressionTest::testNewSampleDataXNull
    public void testNewSampleDataXNull() {
        createRegression().newSampleData(new double[] {}, null);
    }

// org.apache.commons.math3.stat.regression.OLSMultipleLinearRegressionTest::testWampler1
    public void testWampler1() {
        double[] data = new double[]{
            1, 0,
            6, 1,
            63, 2,
            364, 3,
            1365, 4,
            3906, 5,
            9331, 6,
            19608, 7,
            37449, 8,
            66430, 9,
            111111, 10,
            177156, 11,
            271453, 12,
            402234, 13,
            579195, 14,
            813616, 15,
            1118481, 16,
            1508598, 17,
            2000719, 18,
            2613660, 19,
            3368421, 20};
        OLSMultipleLinearRegression model = new OLSMultipleLinearRegression();

        final int nvars = 5;
        final int nobs = 21;
        double[] tmp = new double[(nvars + 1) * nobs];
        int off = 0;
        int off2 = 0;
        for (int i = 0; i < nobs; i++) {
            tmp[off2] = data[off];
            tmp[off2 + 1] = data[off + 1];
            tmp[off2 + 2] = tmp[off2 + 1] * tmp[off2 + 1];
            tmp[off2 + 3] = tmp[off2 + 1] * tmp[off2 + 2];
            tmp[off2 + 4] = tmp[off2 + 1] * tmp[off2 + 3];
            tmp[off2 + 5] = tmp[off2 + 1] * tmp[off2 + 4];
            off2 += (nvars + 1);
            off += 2;
        }
        model.newSampleData(tmp, nobs, nvars);
        double[] betaHat = model.estimateRegressionParameters();
        TestUtils.assertEquals(betaHat,
                new double[]{1.0,
                    1.0, 1.0,
                    1.0, 1.0,
                    1.0}, 1E-8);

        double[] se = model.estimateRegressionParametersStandardErrors();
        TestUtils.assertEquals(se,
                new double[]{0.0,
                    0.0, 0.0,
                    0.0, 0.0,
                    0.0}, 1E-8); 

        TestUtils.assertEquals(1.0, model.calculateRSquared(), 1.0e-10);
        TestUtils.assertEquals(0, model.estimateErrorVariance(), 1.0e-7);
        TestUtils.assertEquals(0.00, model.calculateResidualSumOfSquares(), 1.0e-6);

        return;
    }

// org.apache.commons.math3.stat.regression.OLSMultipleLinearRegressionTest::testWampler2
    public void testWampler2() {
        double[] data = new double[]{
            1.00000, 0,
            1.11111, 1,
            1.24992, 2,
            1.42753, 3,
            1.65984, 4,
            1.96875, 5,
            2.38336, 6,
            2.94117, 7,
            3.68928, 8,
            4.68559, 9,
            6.00000, 10,
            7.71561, 11,
            9.92992, 12,
            12.75603, 13,
            16.32384, 14,
            20.78125, 15,
            26.29536, 16,
            33.05367, 17,
            41.26528, 18,
            51.16209, 19,
            63.00000, 20};
        OLSMultipleLinearRegression model = new OLSMultipleLinearRegression();

        final int nvars = 5;
        final int nobs = 21;
        double[] tmp = new double[(nvars + 1) * nobs];
        int off = 0;
        int off2 = 0;
        for (int i = 0; i < nobs; i++) {
            tmp[off2] = data[off];
            tmp[off2 + 1] = data[off + 1];
            tmp[off2 + 2] = tmp[off2 + 1] * tmp[off2 + 1];
            tmp[off2 + 3] = tmp[off2 + 1] * tmp[off2 + 2];
            tmp[off2 + 4] = tmp[off2 + 1] * tmp[off2 + 3];
            tmp[off2 + 5] = tmp[off2 + 1] * tmp[off2 + 4];
            off2 += (nvars + 1);
            off += 2;
        }
        model.newSampleData(tmp, nobs, nvars);
        double[] betaHat = model.estimateRegressionParameters();
        TestUtils.assertEquals(betaHat,
                new double[]{
                    1.0,
                    1.0e-1,
                    1.0e-2,
                    1.0e-3, 1.0e-4,
                    1.0e-5}, 1E-8);

        double[] se = model.estimateRegressionParametersStandardErrors();
        TestUtils.assertEquals(se,
                new double[]{0.0,
                    0.0, 0.0,
                    0.0, 0.0,
                    0.0}, 1E-8); 
        TestUtils.assertEquals(1.0, model.calculateRSquared(), 1.0e-10);
        TestUtils.assertEquals(0, model.estimateErrorVariance(), 1.0e-7);
        TestUtils.assertEquals(0.00, model.calculateResidualSumOfSquares(), 1.0e-6);
        return;
    }

// org.apache.commons.math3.stat.regression.OLSMultipleLinearRegressionTest::testWampler3
    public void testWampler3() {
        double[] data = new double[]{
            760, 0,
            -2042, 1,
            2111, 2,
            -1684, 3,
            3888, 4,
            1858, 5,
            11379, 6,
            17560, 7,
            39287, 8,
            64382, 9,
            113159, 10,
            175108, 11,
            273291, 12,
            400186, 13,
            581243, 14,
            811568, 15,
            1121004, 16,
            1506550, 17,
            2002767, 18,
            2611612, 19,
            3369180, 20};

        OLSMultipleLinearRegression model = new OLSMultipleLinearRegression();
        final int nvars = 5;
        final int nobs = 21;
        double[] tmp = new double[(nvars + 1) * nobs];
        int off = 0;
        int off2 = 0;
        for (int i = 0; i < nobs; i++) {
            tmp[off2] = data[off];
            tmp[off2 + 1] = data[off + 1];
            tmp[off2 + 2] = tmp[off2 + 1] * tmp[off2 + 1];
            tmp[off2 + 3] = tmp[off2 + 1] * tmp[off2 + 2];
            tmp[off2 + 4] = tmp[off2 + 1] * tmp[off2 + 3];
            tmp[off2 + 5] = tmp[off2 + 1] * tmp[off2 + 4];
            off2 += (nvars + 1);
            off += 2;
        }
        model.newSampleData(tmp, nobs, nvars);
        double[] betaHat = model.estimateRegressionParameters();
        TestUtils.assertEquals(betaHat,
                new double[]{
                    1.0,
                    1.0,
                    1.0,
                    1.0,
                    1.0,
                    1.0}, 1E-8); 

        double[] se = model.estimateRegressionParametersStandardErrors();
        TestUtils.assertEquals(se,
                new double[]{2152.32624678170,
                    2363.55173469681, 779.343524331583,
                    101.475507550350, 5.64566512170752,
                    0.112324854679312}, 1E-8); 

        TestUtils.assertEquals(.999995559025820, model.calculateRSquared(), 1.0e-10);
        TestUtils.assertEquals(5570284.53333333, model.estimateErrorVariance(), 1.0e-6);
        TestUtils.assertEquals(83554268.0000000, model.calculateResidualSumOfSquares(), 1.0e-5);
        return;
    }

// org.apache.commons.math3.stat.regression.OLSMultipleLinearRegressionTest::testWampler4
    public void testWampler4() {
        double[] data = new double[]{
            75901, 0,
            -204794, 1,
            204863, 2,
            -204436, 3,
            253665, 4,
            -200894, 5,
            214131, 6,
            -185192, 7,
            221249, 8,
            -138370, 9,
            315911, 10,
            -27644, 11,
            455253, 12,
            197434, 13,
            783995, 14,
            608816, 15,
            1370781, 16,
            1303798, 17,
            2205519, 18,
            2408860, 19,
            3444321, 20};

        OLSMultipleLinearRegression model = new OLSMultipleLinearRegression();
        final int nvars = 5;
        final int nobs = 21;
        double[] tmp = new double[(nvars + 1) * nobs];
        int off = 0;
        int off2 = 0;
        for (int i = 0; i < nobs; i++) {
            tmp[off2] = data[off];
            tmp[off2 + 1] = data[off + 1];
            tmp[off2 + 2] = tmp[off2 + 1] * tmp[off2 + 1];
            tmp[off2 + 3] = tmp[off2 + 1] * tmp[off2 + 2];
            tmp[off2 + 4] = tmp[off2 + 1] * tmp[off2 + 3];
            tmp[off2 + 5] = tmp[off2 + 1] * tmp[off2 + 4];
            off2 += (nvars + 1);
            off += 2;
        }
        model.newSampleData(tmp, nobs, nvars);
        double[] betaHat = model.estimateRegressionParameters();
        TestUtils.assertEquals(betaHat,
                new double[]{
                    1.0,
                    1.0,
                    1.0,
                    1.0,
                    1.0,
                    1.0}, 1E-6); 

        double[] se = model.estimateRegressionParametersStandardErrors();
        TestUtils.assertEquals(se,
                new double[]{215232.624678170,
                    236355.173469681, 77934.3524331583,
                    10147.5507550350, 564.566512170752,
                    11.2324854679312}, 1E-8); 

        TestUtils.assertEquals(.957478440825662, model.calculateRSquared(), 1.0e-10);
        TestUtils.assertEquals(55702845333.3333, model.estimateErrorVariance(), 1.0e-4);
        TestUtils.assertEquals(835542680000.000, model.calculateResidualSumOfSquares(), 1.0e-3);
        return;
    }

// org.apache.commons.math3.stat.regression.SimpleRegressionTest::testRegressIfaceMethod
    public void testRegressIfaceMethod(){
        final SimpleRegression regression = new SimpleRegression(true);
        final UpdatingMultipleLinearRegression iface = regression;
        final SimpleRegression regressionNoint = new SimpleRegression( false );
        final SimpleRegression regressionIntOnly= new SimpleRegression( false );
        for (int i = 0; i < data.length; i++) {
            iface.addObservation( new double[]{data[i][1]}, data[i][0]);
            regressionNoint.addData(data[i][1], data[i][0]);
            regressionIntOnly.addData(1.0, data[i][0]);
        }

        
        final RegressionResults fullReg = iface.regress( );
        Assert.assertNotNull(fullReg);
        Assert.assertEquals("intercept", regression.getIntercept(), fullReg.getParameterEstimate(0), 1.0e-16);
        Assert.assertEquals("intercept std err",regression.getInterceptStdErr(), fullReg.getStdErrorOfEstimate(0),1.0E-16);
        Assert.assertEquals("slope", regression.getSlope(), fullReg.getParameterEstimate(1), 1.0e-16);
        Assert.assertEquals("slope std err",regression.getSlopeStdErr(), fullReg.getStdErrorOfEstimate(1),1.0E-16);
        Assert.assertEquals("number of observations",regression.getN(), fullReg.getN());
        Assert.assertEquals("r-square",regression.getRSquare(), fullReg.getRSquared(), 1.0E-16);
        Assert.assertEquals("SSR", regression.getRegressionSumSquares(), fullReg.getRegressionSumSquares() ,1.0E-16);
        Assert.assertEquals("MSE", regression.getMeanSquareError(), fullReg.getMeanSquareError() ,1.0E-16);
        Assert.assertEquals("SSE", regression.getSumSquaredErrors(), fullReg.getErrorSumSquares() ,1.0E-16);

        final RegressionResults noInt   = iface.regress( new int[]{1} );
        Assert.assertNotNull(noInt);
        Assert.assertEquals("slope", regressionNoint.getSlope(), noInt.getParameterEstimate(0), 1.0e-12);
        Assert.assertEquals("slope std err",regressionNoint.getSlopeStdErr(), noInt.getStdErrorOfEstimate(0),1.0E-16);
        Assert.assertEquals("number of observations",regressionNoint.getN(), noInt.getN());
        Assert.assertEquals("r-square",regressionNoint.getRSquare(), noInt.getRSquared(), 1.0E-16);
        Assert.assertEquals("SSR", regressionNoint.getRegressionSumSquares(), noInt.getRegressionSumSquares() ,1.0E-8);
        Assert.assertEquals("MSE", regressionNoint.getMeanSquareError(), noInt.getMeanSquareError() ,1.0E-16);
        Assert.assertEquals("SSE", regressionNoint.getSumSquaredErrors(), noInt.getErrorSumSquares() ,1.0E-16);

        final RegressionResults onlyInt = iface.regress( new int[]{0} );
        Assert.assertNotNull(onlyInt);
        Assert.assertEquals("slope", regressionIntOnly.getSlope(), onlyInt.getParameterEstimate(0), 1.0e-12);
        Assert.assertEquals("slope std err",regressionIntOnly.getSlopeStdErr(), onlyInt.getStdErrorOfEstimate(0),1.0E-12);
        Assert.assertEquals("number of observations",regressionIntOnly.getN(), onlyInt.getN());
        Assert.assertEquals("r-square",regressionIntOnly.getRSquare(), onlyInt.getRSquared(), 1.0E-14);
        Assert.assertEquals("SSE", regressionIntOnly.getSumSquaredErrors(), onlyInt.getErrorSumSquares() ,1.0E-8);
        Assert.assertEquals("SSR", regressionIntOnly.getRegressionSumSquares(), onlyInt.getRegressionSumSquares() ,1.0E-8);
        Assert.assertEquals("MSE", regressionIntOnly.getMeanSquareError(), onlyInt.getMeanSquareError() ,1.0E-8);

    }

// org.apache.commons.math3.stat.regression.SimpleRegressionTest::testRegressExceptions
    public void testRegressExceptions() {
        
        final SimpleRegression noIntRegression = new SimpleRegression(false);
        noIntRegression.addData(noint2[0][1], noint2[0][0]);
        noIntRegression.addData(noint2[1][1], noint2[1][0]);
        noIntRegression.addData(noint2[2][1], noint2[2][0]);
        try { 
            noIntRegression.regress(null);
            Assert.fail("Expecting MathIllegalArgumentException for null array");
        } catch (MathIllegalArgumentException ex) {
            
        }
        try { 
            noIntRegression.regress(new int[] {});
            Assert.fail("Expecting MathIllegalArgumentException for empty array");
        } catch (MathIllegalArgumentException ex) {
            
        }
        try { 
            noIntRegression.regress(new int[] {0, 1});
            Assert.fail("Expecting ModelSpecificationException - too many regressors");
        } catch (ModelSpecificationException ex) {
            
        }
        try { 
            noIntRegression.regress(new int[] {1});
            Assert.fail("Expecting OutOfRangeException - invalid regression");
        } catch (OutOfRangeException ex) {
            
        }
        
        
        final SimpleRegression regression = new SimpleRegression(true);
        regression.addData(noint2[0][1], noint2[0][0]);
        regression.addData(noint2[1][1], noint2[1][0]);
        regression.addData(noint2[2][1], noint2[2][0]);
        try { 
            regression.regress(null);
            Assert.fail("Expecting MathIllegalArgumentException for null array");
        } catch (MathIllegalArgumentException ex) {
            
        }
        try { 
            regression.regress(new int[] {});
            Assert.fail("Expecting MathIllegalArgumentException for empty array");
        } catch (MathIllegalArgumentException ex) {
            
        }
        try { 
            regression.regress(new int[] {0, 1, 2});
            Assert.fail("Expecting ModelSpecificationException - too many regressors");
        } catch (ModelSpecificationException ex) {
            
        }
        try { 
            regression.regress(new int[] {1,0});
            Assert.fail("Expecting ModelSpecificationException - invalid regression");
        } catch (ModelSpecificationException ex) {
            
        }
        try { 
            regression.regress(new int[] {3,4});
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try { 
            regression.regress(new int[] {0,2});
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try { 
            regression.regress(new int[] {2});
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
    }

// org.apache.commons.math3.stat.regression.SimpleRegressionTest::testNoInterceot_noint2
    public void testNoInterceot_noint2(){
         SimpleRegression regression = new SimpleRegression(false);
         regression.addData(noint2[0][1], noint2[0][0]);
         regression.addData(noint2[1][1], noint2[1][0]);
         regression.addData(noint2[2][1], noint2[2][0]);
         Assert.assertEquals("intercept", 0, regression.getIntercept(), 0);
         Assert.assertEquals("slope", 0.727272727272727,
                 regression.getSlope(), 10E-12);
         Assert.assertEquals("slope std err", 0.420827318078432E-01,
                regression.getSlopeStdErr(),10E-12);
        Assert.assertEquals("number of observations", 3, regression.getN());
        Assert.assertEquals("r-square", 0.993348115299335,
            regression.getRSquare(), 10E-12);
        Assert.assertEquals("SSR", 40.7272727272727,
            regression.getRegressionSumSquares(), 10E-9);
        Assert.assertEquals("MSE", 0.136363636363636,
            regression.getMeanSquareError(), 10E-10);
        Assert.assertEquals("SSE", 0.272727272727273,
            regression.getSumSquaredErrors(),10E-9);
    }

// org.apache.commons.math3.stat.regression.SimpleRegressionTest::testNoIntercept_noint1
    public void testNoIntercept_noint1(){
        SimpleRegression regression = new SimpleRegression(false);
        for (int i = 0; i < noint1.length; i++) {
            regression.addData(noint1[i][1], noint1[i][0]);
        }
        Assert.assertEquals("intercept", 0, regression.getIntercept(), 0);
        Assert.assertEquals("slope", 2.07438016528926, regression.getSlope(), 10E-12);
        Assert.assertEquals("slope std err", 0.165289256198347E-01,
                regression.getSlopeStdErr(),10E-12);
        Assert.assertEquals("number of observations", 11, regression.getN());
        Assert.assertEquals("r-square", 0.999365492298663,
            regression.getRSquare(), 10E-12);
        Assert.assertEquals("SSR", 200457.727272727,
            regression.getRegressionSumSquares(), 10E-9);
        Assert.assertEquals("MSE", 12.7272727272727,
            regression.getMeanSquareError(), 10E-10);
        Assert.assertEquals("SSE", 127.272727272727,
            regression.getSumSquaredErrors(),10E-9);

    }

// org.apache.commons.math3.stat.regression.SimpleRegressionTest::testNorris
    public void testNorris() {
        SimpleRegression regression = new SimpleRegression();
        for (int i = 0; i < data.length; i++) {
            regression.addData(data[i][1], data[i][0]);
        }
        
        
        Assert.assertEquals("slope", 1.00211681802045, regression.getSlope(), 10E-12);
        Assert.assertEquals("slope std err", 0.429796848199937E-03,
                regression.getSlopeStdErr(),10E-12);
        Assert.assertEquals("number of observations", 36, regression.getN());
        Assert.assertEquals( "intercept", -0.262323073774029,
            regression.getIntercept(),10E-12);
        Assert.assertEquals("std err intercept", 0.232818234301152,
            regression.getInterceptStdErr(),10E-12);
        Assert.assertEquals("r-square", 0.999993745883712,
            regression.getRSquare(), 10E-12);
        Assert.assertEquals("SSR", 4255954.13232369,
            regression.getRegressionSumSquares(), 10E-9);
        Assert.assertEquals("MSE", 0.782864662630069,
            regression.getMeanSquareError(), 10E-10);
        Assert.assertEquals("SSE", 26.6173985294224,
            regression.getSumSquaredErrors(),10E-9);
        

        Assert.assertEquals( "predict(0)",  -0.262323073774029,
            regression.predict(0), 10E-12);
        Assert.assertEquals("predict(1)", 1.00211681802045 - 0.262323073774029,
            regression.predict(1), 10E-12);
    }

// org.apache.commons.math3.stat.regression.SimpleRegressionTest::testCorr
    public void testCorr() {
        SimpleRegression regression = new SimpleRegression();
        regression.addData(corrData);
        Assert.assertEquals("number of observations", 17, regression.getN());
        Assert.assertEquals("r-square", .896123, regression.getRSquare(), 10E-6);
        Assert.assertEquals("r", -0.94663767742, regression.getR(), 1E-10);
    }

// org.apache.commons.math3.stat.regression.SimpleRegressionTest::testNaNs
    public void testNaNs() {
        SimpleRegression regression = new SimpleRegression();
        Assert.assertTrue("intercept not NaN", Double.isNaN(regression.getIntercept()));
        Assert.assertTrue("slope not NaN", Double.isNaN(regression.getSlope()));
        Assert.assertTrue("slope std err not NaN", Double.isNaN(regression.getSlopeStdErr()));
        Assert.assertTrue("intercept std err not NaN", Double.isNaN(regression.getInterceptStdErr()));
        Assert.assertTrue("MSE not NaN", Double.isNaN(regression.getMeanSquareError()));
        Assert.assertTrue("e not NaN", Double.isNaN(regression.getR()));
        Assert.assertTrue("r-square not NaN", Double.isNaN(regression.getRSquare()));
        Assert.assertTrue( "RSS not NaN", Double.isNaN(regression.getRegressionSumSquares()));
        Assert.assertTrue("SSE not NaN",Double.isNaN(regression.getSumSquaredErrors()));
        Assert.assertTrue("SSTO not NaN", Double.isNaN(regression.getTotalSumSquares()));
        Assert.assertTrue("predict not NaN", Double.isNaN(regression.predict(0)));

        regression.addData(1, 2);
        regression.addData(1, 3);

        
        Assert.assertTrue("intercept not NaN", Double.isNaN(regression.getIntercept()));
        Assert.assertTrue("slope not NaN", Double.isNaN(regression.getSlope()));
        Assert.assertTrue("slope std err not NaN", Double.isNaN(regression.getSlopeStdErr()));
        Assert.assertTrue("intercept std err not NaN", Double.isNaN(regression.getInterceptStdErr()));
        Assert.assertTrue("MSE not NaN", Double.isNaN(regression.getMeanSquareError()));
        Assert.assertTrue("e not NaN", Double.isNaN(regression.getR()));
        Assert.assertTrue("r-square not NaN", Double.isNaN(regression.getRSquare()));
        Assert.assertTrue("RSS not NaN", Double.isNaN(regression.getRegressionSumSquares()));
        Assert.assertTrue("SSE not NaN", Double.isNaN(regression.getSumSquaredErrors()));
        Assert.assertTrue("predict not NaN", Double.isNaN(regression.predict(0)));

        
        Assert.assertTrue("SSTO NaN", !Double.isNaN(regression.getTotalSumSquares()));

        regression = new SimpleRegression();

        regression.addData(1, 2);
        regression.addData(3, 3);

        
        Assert.assertTrue("interceptNaN", !Double.isNaN(regression.getIntercept()));
        Assert.assertTrue("slope NaN", !Double.isNaN(regression.getSlope()));
        Assert.assertTrue("slope std err not NaN", Double.isNaN(regression.getSlopeStdErr()));
        Assert.assertTrue("intercept std err not NaN", Double.isNaN(regression.getInterceptStdErr()));
        Assert.assertTrue("MSE not NaN", Double.isNaN(regression.getMeanSquareError()));
        Assert.assertTrue("r NaN", !Double.isNaN(regression.getR()));
        Assert.assertTrue("r-square NaN", !Double.isNaN(regression.getRSquare()));
        Assert.assertTrue("RSS NaN", !Double.isNaN(regression.getRegressionSumSquares()));
        Assert.assertTrue("SSE NaN", !Double.isNaN(regression.getSumSquaredErrors()));
        Assert.assertTrue("SSTO NaN", !Double.isNaN(regression.getTotalSumSquares()));
        Assert.assertTrue("predict NaN", !Double.isNaN(regression.predict(0)));

        regression.addData(1, 4);

        
        Assert.assertTrue("MSE NaN", !Double.isNaN(regression.getMeanSquareError()));
        Assert.assertTrue("slope std err NaN", !Double.isNaN(regression.getSlopeStdErr()));
        Assert.assertTrue("intercept std err NaN", !Double.isNaN(regression.getInterceptStdErr()));
    }

// org.apache.commons.math3.stat.regression.SimpleRegressionTest::testClear
    public void testClear() {
        SimpleRegression regression = new SimpleRegression();
        regression.addData(corrData);
        Assert.assertEquals("number of observations", 17, regression.getN());
        regression.clear();
        Assert.assertEquals("number of observations", 0, regression.getN());
        regression.addData(corrData);
        Assert.assertEquals("r-square", .896123, regression.getRSquare(), 10E-6);
        regression.addData(data);
        Assert.assertEquals("number of observations", 53, regression.getN());
    }

// org.apache.commons.math3.stat.regression.SimpleRegressionTest::testInference
    public void testInference() {
        
        
        SimpleRegression regression = new SimpleRegression();
        regression.addData(infData);
        Assert.assertEquals("slope std err", 0.011448491,
                regression.getSlopeStdErr(), 1E-10);
        Assert.assertEquals("std err intercept", 0.286036932,
                regression.getInterceptStdErr(),1E-8);
        Assert.assertEquals("significance", 4.596e-07,
                regression.getSignificance(),1E-8);
        Assert.assertEquals("slope conf interval half-width", 0.0270713794287,
                regression.getSlopeConfidenceInterval(),1E-8);
        
        regression = new SimpleRegression();
        regression.addData(infData2);
        Assert.assertEquals("slope std err", 1.07260253,
                regression.getSlopeStdErr(), 1E-8);
        Assert.assertEquals("std err intercept",4.17718672,
                regression.getInterceptStdErr(),1E-8);
        Assert.assertEquals("significance", 0.261829133982,
                regression.getSignificance(),1E-11);
        Assert.assertEquals("slope conf interval half-width", 2.97802204827,
                regression.getSlopeConfidenceInterval(),1E-8);
        

        
        Assert.assertTrue("tighter means wider",
                regression.getSlopeConfidenceInterval() < regression.getSlopeConfidenceInterval(0.01));

        try {
            regression.getSlopeConfidenceInterval(1);
            Assert.fail("expecting MathIllegalArgumentException for alpha = 1");
        } catch (MathIllegalArgumentException ex) {
            
        }

    }

// org.apache.commons.math3.stat.regression.SimpleRegressionTest::testPerfect
    public void testPerfect() {
        SimpleRegression regression = new SimpleRegression();
        int n = 100;
        for (int i = 0; i < n; i++) {
            regression.addData(((double) i) / (n - 1), i);
        }
        Assert.assertEquals(0.0, regression.getSignificance(), 1.0e-5);
        Assert.assertTrue(regression.getSlope() > 0.0);
        Assert.assertTrue(regression.getSumSquaredErrors() >= 0.0);
    }

// org.apache.commons.math3.stat.regression.SimpleRegressionTest::testPerfectNegative
    public void testPerfectNegative() {
        SimpleRegression regression = new SimpleRegression();
        int n = 100;
        for (int i = 0; i < n; i++) {
            regression.addData(- ((double) i) / (n - 1), i);
        }

        Assert.assertEquals(0.0, regression.getSignificance(), 1.0e-5);
        Assert.assertTrue(regression.getSlope() < 0.0);
    }

// org.apache.commons.math3.stat.regression.SimpleRegressionTest::testRandom
    public void testRandom() {
        SimpleRegression regression = new SimpleRegression();
        Random random = new Random(1);
        int n = 100;
        for (int i = 0; i < n; i++) {
            regression.addData(((double) i) / (n - 1), random.nextDouble());
        }

        Assert.assertTrue( 0.0 < regression.getSignificance()
                    && regression.getSignificance() < 1.0);
    }

// org.apache.commons.math3.stat.regression.SimpleRegressionTest::testSSENonNegative
    public void testSSENonNegative() {
        double[] y = { 8915.102, 8919.302, 8923.502 };
        double[] x = { 1.107178495E2, 1.107264895E2, 1.107351295E2 };
        SimpleRegression reg = new SimpleRegression();
        for (int i = 0; i < x.length; i++) {
            reg.addData(x[i], y[i]);
        }
        Assert.assertTrue(reg.getSumSquaredErrors() >= 0.0);
    }

// org.apache.commons.math3.stat.regression.SimpleRegressionTest::testRemoveXY
    public void testRemoveXY() {
        
        SimpleRegression regression = new SimpleRegression();
        regression.addData(infData);
        regression.removeData(removeX, removeY);
        regression.addData(removeX, removeY);
        
        Assert.assertEquals("slope std err", 0.011448491,
                regression.getSlopeStdErr(), 1E-10);
        Assert.assertEquals("std err intercept", 0.286036932,
                regression.getInterceptStdErr(),1E-8);
        Assert.assertEquals("significance", 4.596e-07,
                regression.getSignificance(),1E-8);
        Assert.assertEquals("slope conf interval half-width", 0.0270713794287,
                regression.getSlopeConfidenceInterval(),1E-8);
     }

// org.apache.commons.math3.stat.regression.SimpleRegressionTest::testRemoveSingle
    public void testRemoveSingle() {
        
        SimpleRegression regression = new SimpleRegression();
        regression.addData(infData);
        regression.removeData(removeSingle);
        regression.addData(removeSingle);
        
        Assert.assertEquals("slope std err", 0.011448491,
                regression.getSlopeStdErr(), 1E-10);
        Assert.assertEquals("std err intercept", 0.286036932,
                regression.getInterceptStdErr(),1E-8);
        Assert.assertEquals("significance", 4.596e-07,
                regression.getSignificance(),1E-8);
        Assert.assertEquals("slope conf interval half-width", 0.0270713794287,
                regression.getSlopeConfidenceInterval(),1E-8);
     }

// org.apache.commons.math3.stat.regression.SimpleRegressionTest::testRemoveMultiple
    public void testRemoveMultiple() {
        
        SimpleRegression regression = new SimpleRegression();
        regression.addData(infData);
        regression.removeData(removeMultiple);
        regression.addData(removeMultiple);
        
        Assert.assertEquals("slope std err", 0.011448491,
                regression.getSlopeStdErr(), 1E-10);
        Assert.assertEquals("std err intercept", 0.286036932,
                regression.getInterceptStdErr(),1E-8);
        Assert.assertEquals("significance", 4.596e-07,
                regression.getSignificance(),1E-8);
        Assert.assertEquals("slope conf interval half-width", 0.0270713794287,
                regression.getSlopeConfidenceInterval(),1E-8);
     }

// org.apache.commons.math3.stat.regression.SimpleRegressionTest::testRemoveObsFromEmpty
    public void testRemoveObsFromEmpty() {
        SimpleRegression regression = new SimpleRegression();
        regression.removeData(removeX, removeY);
        Assert.assertEquals(regression.getN(), 0);
    }

// org.apache.commons.math3.stat.regression.SimpleRegressionTest::testRemoveObsFromSingle
    public void testRemoveObsFromSingle() {
        SimpleRegression regression = new SimpleRegression();
        regression.addData(removeX, removeY);
        regression.removeData(removeX, removeY);
        Assert.assertEquals(regression.getN(), 0);
    }

// org.apache.commons.math3.stat.regression.SimpleRegressionTest::testRemoveMultipleToEmpty
    public void testRemoveMultipleToEmpty() {
        SimpleRegression regression = new SimpleRegression();
        regression.addData(removeMultiple);
        regression.removeData(removeMultiple);
        Assert.assertEquals(regression.getN(), 0);
    }

// org.apache.commons.math3.stat.regression.SimpleRegressionTest::testRemoveMultiplePastEmpty
    public void testRemoveMultiplePastEmpty() {
        SimpleRegression regression = new SimpleRegression();
        regression.addData(removeX, removeY);
        regression.removeData(removeMultiple);
        Assert.assertEquals(regression.getN(), 0);
    }

// org.apache.commons.math3.transform.FastCosineTransformerTest::testAdHocData
    public void testAdHocData() {
        FastCosineTransformer transformer;
        transformer = new FastCosineTransformer(DctNormalization.STANDARD_DCT_I);
        double result[], tolerance = 1E-12;

        double x[] = {
            0.0, 1.0, 4.0, 9.0, 16.0, 25.0, 36.0, 49.0, 64.0
        };
        double y[] =
            {
                172.0, -105.096569476353, 27.3137084989848, -12.9593152353742,
                8.0, -5.78585076868676, 4.68629150101524, -4.15826451958632,
                4.0
            };

        result = transformer.transform(x, TransformType.FORWARD);
        for (int i = 0; i < result.length; i++) {
            Assert.assertEquals(y[i], result[i], tolerance);
        }

        result = transformer.transform(y, TransformType.INVERSE);
        for (int i = 0; i < result.length; i++) {
            Assert.assertEquals(x[i], result[i], tolerance);
        }

        TransformUtils.scaleArray(x, FastMath.sqrt(0.5 * (x.length - 1)));

        transformer = new FastCosineTransformer(DctNormalization.ORTHOGONAL_DCT_I);
        result = transformer.transform(y, TransformType.FORWARD);
        for (int i = 0; i < result.length; i++) {
            Assert.assertEquals(x[i], result[i], tolerance);
        }

        result = transformer.transform(x, TransformType.INVERSE);
        for (int i = 0; i < result.length; i++) {
            Assert.assertEquals(y[i], result[i], tolerance);
        }
    }

// org.apache.commons.math3.transform.FastCosineTransformerTest::testParameters
    public void testParameters()
        throws Exception {
        UnivariateFunction f = new Sin();
        FastCosineTransformer transformer;
        transformer = new FastCosineTransformer(DctNormalization.STANDARD_DCT_I);

        try {
            
            transformer.transform(f, 1, -1, 65, TransformType.FORWARD);
            Assert.fail("Expecting IllegalArgumentException - bad interval");
        } catch (IllegalArgumentException ex) {
            
        }
        try {
            
            transformer.transform(f, -1, 1, 1, TransformType.FORWARD);
            Assert
                .fail("Expecting IllegalArgumentException - bad samples number");
        } catch (IllegalArgumentException ex) {
            
        }
        try {
            
            transformer.transform(f, -1, 1, 64, TransformType.FORWARD);
            Assert
                .fail("Expecting IllegalArgumentException - bad samples number");
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math3.transform.FastCosineTransformerTest::testSinFunction
    public void testSinFunction() {
        UnivariateFunction f = new Sin();
        FastCosineTransformer transformer;
        transformer = new FastCosineTransformer(DctNormalization.STANDARD_DCT_I);
        double min, max, result[], tolerance = 1E-12;
        int N = 9;

        double expected[] =
            {
                0.0, 3.26197262739567, 0.0, -2.17958042710327, 0.0,
                -0.648846697642915, 0.0, -0.433545502649478, 0.0
            };
        min = 0.0;
        max = 2.0 * FastMath.PI * N / (N - 1);
        result = transformer.transform(f, min, max, N, TransformType.FORWARD);
        for (int i = 0; i < N; i++) {
            Assert.assertEquals(expected[i], result[i], tolerance);
        }

        min = -FastMath.PI;
        max = FastMath.PI * (N + 1) / (N - 1);
        result = transformer.transform(f, min, max, N, TransformType.FORWARD);
        for (int i = 0; i < N; i++) {
            Assert.assertEquals(-expected[i], result[i], tolerance);
        }
    }

// org.apache.commons.math3.transform.FastFourierTransformerTest::testTransformComplexSizeNotAPowerOfTwo
    public void testTransformComplexSizeNotAPowerOfTwo() {
        final int n = 127;
        final Complex[] x = createComplexData(n);
        final DftNormalization[] norm;
        norm = DftNormalization.values();
        final TransformType[] type;
        type = TransformType.values();
        for (int i = 0; i < norm.length; i++) {
            for (int j = 0; j < type.length; j++) {
                final FastFourierTransformer fft;
                fft = new FastFourierTransformer(norm[i]);
                try {
                    fft.transform(x, type[j]);
                    Assert.fail(norm[i] + ", " + type[j] +
                        ": MathIllegalArgumentException was expected");
                } catch (MathIllegalArgumentException e) {
                    
                }
            }
        }
    }

// org.apache.commons.math3.transform.FastFourierTransformerTest::testTransformRealSizeNotAPowerOfTwo
    public void testTransformRealSizeNotAPowerOfTwo() {
        final int n = 127;
        final double[] x = createRealData(n);
        final DftNormalization[] norm;
        norm = DftNormalization.values();
        final TransformType[] type;
        type = TransformType.values();
        for (int i = 0; i < norm.length; i++) {
            for (int j = 0; j < type.length; j++) {
                final FastFourierTransformer fft;
                fft = new FastFourierTransformer(norm[i]);
                try {
                    fft.transform(x, type[j]);
                    Assert.fail(norm[i] + ", " + type[j] +
                        ": MathIllegalArgumentException was expected");
                } catch (MathIllegalArgumentException e) {
                    
                }
            }
        }
    }

// org.apache.commons.math3.transform.FastFourierTransformerTest::testTransformFunctionSizeNotAPowerOfTwo
    public void testTransformFunctionSizeNotAPowerOfTwo() {
        final int n = 127;
        final UnivariateFunction f = new Sin();
        final DftNormalization[] norm;
        norm = DftNormalization.values();
        final TransformType[] type;
        type = TransformType.values();
        for (int i = 0; i < norm.length; i++) {
            for (int j = 0; j < type.length; j++) {
                final FastFourierTransformer fft;
                fft = new FastFourierTransformer(norm[i]);
                try {
                    fft.transform(f, 0.0, Math.PI, n, type[j]);
                    Assert.fail(norm[i] + ", " + type[j] +
                        ": MathIllegalArgumentException was expected");
                } catch (MathIllegalArgumentException e) {
                    
                }
            }
        }
    }

// org.apache.commons.math3.transform.FastFourierTransformerTest::testTransformFunctionNotStrictlyPositiveNumberOfSamples
    public void testTransformFunctionNotStrictlyPositiveNumberOfSamples() {
        final int n = -128;
        final UnivariateFunction f = new Sin();
        final DftNormalization[] norm;
        norm = DftNormalization.values();
        final TransformType[] type;
        type = TransformType.values();
        for (int i = 0; i < norm.length; i++) {
            for (int j = 0; j < type.length; j++) {
                final FastFourierTransformer fft;
                fft = new FastFourierTransformer(norm[i]);
                try {
                    fft.transform(f, 0.0, Math.PI, n, type[j]);
                    fft.transform(f, 0.0, Math.PI, n, type[j]);
                    Assert.fail(norm[i] + ", " + type[j] +
                        ": NotStrictlyPositiveException was expected");
                } catch (NotStrictlyPositiveException e) {
                    
                }
            }
        }
    }

// org.apache.commons.math3.transform.FastFourierTransformerTest::testTransformFunctionInvalidBounds
    public void testTransformFunctionInvalidBounds() {
        final int n = 128;
        final UnivariateFunction f = new Sin();
        final DftNormalization[] norm;
        norm = DftNormalization.values();
        final TransformType[] type;
        type = TransformType.values();
        for (int i = 0; i < norm.length; i++) {
            for (int j = 0; j < type.length; j++) {
                final FastFourierTransformer fft;
                fft = new FastFourierTransformer(norm[i]);
                try {
                    fft.transform(f, Math.PI, 0.0, n, type[j]);
                    Assert.fail(norm[i] + ", " + type[j] +
                        ": NumberIsTooLargeException was expected");
                } catch (NumberIsTooLargeException e) {
                    
                }
            }
        }
    }

// org.apache.commons.math3.transform.FastFourierTransformerTest::testTransformComplex
    public void testTransformComplex() {
        final DftNormalization[] norm;
        norm = DftNormalization.values();
        final TransformType[] type;
        type = TransformType.values();
        for (int i = 0; i < norm.length; i++) {
            for (int j = 0; j < type.length; j++) {
                doTestTransformComplex(2, 1.0E-15, norm[i], type[j]);
                doTestTransformComplex(4, 1.0E-14, norm[i], type[j]);
                doTestTransformComplex(8, 1.0E-14, norm[i], type[j]);
                doTestTransformComplex(16, 1.0E-13, norm[i], type[j]);
                doTestTransformComplex(32, 1.0E-13, norm[i], type[j]);
                doTestTransformComplex(64, 1.0E-12, norm[i], type[j]);
                doTestTransformComplex(128, 1.0E-12, norm[i], type[j]);
            }
        }
    }

// org.apache.commons.math3.transform.FastFourierTransformerTest::testStandardTransformReal
    public void testStandardTransformReal() {
        final DftNormalization[] norm;
        norm = DftNormalization.values();
        final TransformType[] type;
        type = TransformType.values();
        for (int i = 0; i < norm.length; i++) {
            for (int j = 0; j < type.length; j++) {
                doTestTransformReal(2, 1.0E-15, norm[i], type[j]);
                doTestTransformReal(4, 1.0E-14, norm[i], type[j]);
                doTestTransformReal(8, 1.0E-14, norm[i], type[j]);
                doTestTransformReal(16, 1.0E-13, norm[i], type[j]);
                doTestTransformReal(32, 1.0E-13, norm[i], type[j]);
                doTestTransformReal(64, 1.0E-13, norm[i], type[j]);
                doTestTransformReal(128, 1.0E-11, norm[i], type[j]);
            }
        }
    }

// org.apache.commons.math3.transform.FastFourierTransformerTest::testStandardTransformFunction
    public void testStandardTransformFunction() {
        final UnivariateFunction f = new Sinc();
        final double min = -FastMath.PI;
        final double max = FastMath.PI;
        final DftNormalization[] norm;
        norm = DftNormalization.values();
        final TransformType[] type;
        type = TransformType.values();
        for (int i = 0; i < norm.length; i++) {
            for (int j = 0; j < type.length; j++) {
                doTestTransformFunction(f, min, max, 2, 1.0E-15, norm[i], type[j]);
                doTestTransformFunction(f, min, max, 4, 1.0E-14, norm[i], type[j]);
                doTestTransformFunction(f, min, max, 8, 1.0E-14, norm[i], type[j]);
                doTestTransformFunction(f, min, max, 16, 1.0E-13, norm[i], type[j]);
                doTestTransformFunction(f, min, max, 32, 1.0E-13, norm[i], type[j]);
                doTestTransformFunction(f, min, max, 64, 1.0E-12, norm[i], type[j]);
                doTestTransformFunction(f, min, max, 128, 1.0E-11, norm[i], type[j]);
            }
        }
    }

// org.apache.commons.math3.transform.FastFourierTransformerTest::testAdHocData
    public void testAdHocData() {
        FastFourierTransformer transformer;
        transformer = new FastFourierTransformer(DftNormalization.STANDARD);
        Complex result[]; double tolerance = 1E-12;

        double x[] = {1.3, 2.4, 1.7, 4.1, 2.9, 1.7, 5.1, 2.7};
        Complex y[] = {
            new Complex(21.9, 0.0),
            new Complex(-2.09497474683058, 1.91507575950825),
            new Complex(-2.6, 2.7),
            new Complex(-1.10502525316942, -4.88492424049175),
            new Complex(0.1, 0.0),
            new Complex(-1.10502525316942, 4.88492424049175),
            new Complex(-2.6, -2.7),
            new Complex(-2.09497474683058, -1.91507575950825)};

        result = transformer.transform(x, TransformType.FORWARD);
        for (int i = 0; i < result.length; i++) {
            Assert.assertEquals(y[i].getReal(), result[i].getReal(), tolerance);
            Assert.assertEquals(y[i].getImaginary(), result[i].getImaginary(), tolerance);
        }

        result = transformer.transform(y, TransformType.INVERSE);
        for (int i = 0; i < result.length; i++) {
            Assert.assertEquals(x[i], result[i].getReal(), tolerance);
            Assert.assertEquals(0.0, result[i].getImaginary(), tolerance);
        }

        double x2[] = {10.4, 21.6, 40.8, 13.6, 23.2, 32.8, 13.6, 19.2};
        TransformUtils.scaleArray(x2, 1.0 / FastMath.sqrt(x2.length));
        Complex y2[] = y;

        transformer = new FastFourierTransformer(DftNormalization.UNITARY);
        result = transformer.transform(y2, TransformType.FORWARD);
        for (int i = 0; i < result.length; i++) {
            Assert.assertEquals(x2[i], result[i].getReal(), tolerance);
            Assert.assertEquals(0.0, result[i].getImaginary(), tolerance);
        }

        result = transformer.transform(x2, TransformType.INVERSE);
        for (int i = 0; i < result.length; i++) {
            Assert.assertEquals(y2[i].getReal(), result[i].getReal(), tolerance);
            Assert.assertEquals(y2[i].getImaginary(), result[i].getImaginary(), tolerance);
        }
    }

// org.apache.commons.math3.transform.FastFourierTransformerTest::testSinFunction
    public void testSinFunction() {
        UnivariateFunction f = new Sin();
        FastFourierTransformer transformer;
        transformer = new FastFourierTransformer(DftNormalization.STANDARD);
        Complex result[]; int N = 1 << 8;
        double min, max, tolerance = 1E-12;

        min = 0.0; max = 2.0 * FastMath.PI;
        result = transformer.transform(f, min, max, N, TransformType.FORWARD);
        Assert.assertEquals(0.0, result[1].getReal(), tolerance);
        Assert.assertEquals(-(N >> 1), result[1].getImaginary(), tolerance);
        Assert.assertEquals(0.0, result[N-1].getReal(), tolerance);
        Assert.assertEquals(N >> 1, result[N-1].getImaginary(), tolerance);
        for (int i = 0; i < N-1; i += (i == 0 ? 2 : 1)) {
            Assert.assertEquals(0.0, result[i].getReal(), tolerance);
            Assert.assertEquals(0.0, result[i].getImaginary(), tolerance);
        }

        min = -FastMath.PI; max = FastMath.PI;
        result = transformer.transform(f, min, max, N, TransformType.INVERSE);
        Assert.assertEquals(0.0, result[1].getReal(), tolerance);
        Assert.assertEquals(-0.5, result[1].getImaginary(), tolerance);
        Assert.assertEquals(0.0, result[N-1].getReal(), tolerance);
        Assert.assertEquals(0.5, result[N-1].getImaginary(), tolerance);
        for (int i = 0; i < N-1; i += (i == 0 ? 2 : 1)) {
            Assert.assertEquals(0.0, result[i].getReal(), tolerance);
            Assert.assertEquals(0.0, result[i].getImaginary(), tolerance);
        }
    }

// org.apache.commons.math3.transform.FastFourierTransformerTest::test2DData
    public void test2DData() {
        FastFourierTransformer transformer;
        transformer = new FastFourierTransformer(DftNormalization.STANDARD);

        double tolerance = 1E-12;
        Complex[][] input = new Complex[][] {new Complex[] {new Complex(1, 0),
                                                            new Complex(2, 0)},
                                             new Complex[] {new Complex(3, 1),
                                                            new Complex(4, 2)}};
        Complex[][] goodOutput = new Complex[][] {new Complex[] {new Complex(5,
                1.5), new Complex(-1, -.5)}, new Complex[] {new Complex(-2,
                -1.5), new Complex(0, .5)}};
        for (int i = 0; i < goodOutput.length; i++) {
            TransformUtils.scaleArray(
                goodOutput[i],
                FastMath.sqrt(goodOutput[i].length) *
                    FastMath.sqrt(goodOutput.length));
        }
        Complex[][] output = (Complex[][])transformer.mdfft(input, TransformType.FORWARD);
        Complex[][] output2 = (Complex[][])transformer.mdfft(output, TransformType.INVERSE);

        Assert.assertEquals(input.length, output.length);
        Assert.assertEquals(input.length, output2.length);
        Assert.assertEquals(input[0].length, output[0].length);
        Assert.assertEquals(input[0].length, output2[0].length);
        Assert.assertEquals(input[1].length, output[1].length);
        Assert.assertEquals(input[1].length, output2[1].length);

        for (int i = 0; i < input.length; i++) {
            for (int j = 0; j < input[0].length; j++) {
                Assert.assertEquals(input[i][j].getImaginary(), output2[i][j].getImaginary(),
                             tolerance);
                Assert.assertEquals(input[i][j].getReal(), output2[i][j].getReal(), tolerance);
                Assert.assertEquals(goodOutput[i][j].getImaginary(), output[i][j].getImaginary(),
                             tolerance);
                Assert.assertEquals(goodOutput[i][j].getReal(), output[i][j].getReal(), tolerance);
            }
        }
    }

// org.apache.commons.math3.transform.FastFourierTransformerTest::test2DDataUnitary
    public void test2DDataUnitary() {
        FastFourierTransformer transformer;
        transformer = new FastFourierTransformer(DftNormalization.UNITARY);
        double tolerance = 1E-12;
        Complex[][] input = new Complex[][] {new Complex[] {new Complex(1, 0),
                                                            new Complex(2, 0)},
                                             new Complex[] {new Complex(3, 1),
                                                            new Complex(4, 2)}};
        Complex[][] goodOutput = new Complex[][] {new Complex[] {new Complex(5,
                1.5), new Complex(-1, -.5)}, new Complex[] {new Complex(-2,
                -1.5), new Complex(0, .5)}};
        Complex[][] output = (Complex[][])transformer.mdfft(input, TransformType.FORWARD);
        Complex[][] output2 = (Complex[][])transformer.mdfft(output, TransformType.INVERSE);

        Assert.assertEquals(input.length, output.length);
        Assert.assertEquals(input.length, output2.length);
        Assert.assertEquals(input[0].length, output[0].length);
        Assert.assertEquals(input[0].length, output2[0].length);
        Assert.assertEquals(input[1].length, output[1].length);
        Assert.assertEquals(input[1].length, output2[1].length);

        for (int i = 0; i < input.length; i++) {
            for (int j = 0; j < input[0].length; j++) {
                Assert.assertEquals(input[i][j].getImaginary(), output2[i][j].getImaginary(),
                             tolerance);
                Assert.assertEquals(input[i][j].getReal(), output2[i][j].getReal(), tolerance);
                Assert.assertEquals(goodOutput[i][j].getImaginary(), output[i][j].getImaginary(),
                             tolerance);
                Assert.assertEquals(goodOutput[i][j].getReal(), output[i][j].getReal(), tolerance);
            }
        }
    }

// org.apache.commons.math3.transform.FastHadamardTransformerTest::test8Points
    public void test8Points() {
        checkAllTransforms(new int[] { 1, 4, -2, 3, 0, 1, 4, -1 },
                       new int[] { 10, -4, 2, -4, 2, -12, 6, 8 });
    }

// org.apache.commons.math3.transform.FastHadamardTransformerTest::test4Points
    public void test4Points() {
        checkAllTransforms(new int[] { 1, 2, 3, 4 },
                           new int[] { 10, -2, -4, 0 });
    }

// org.apache.commons.math3.transform.FastHadamardTransformerTest::testNoIntInverse
    public void testNoIntInverse() {
        FastHadamardTransformer transformer = new FastHadamardTransformer();
        double[] x = transformer.transform(new double[] { 0, 1, 0, 1}, TransformType.INVERSE);
        Assert.assertEquals( 0.5, x[0], 0);
        Assert.assertEquals(-0.5, x[1], 0);
        Assert.assertEquals( 0.0, x[2], 0);
        Assert.assertEquals( 0.0, x[3], 0);
    }

// org.apache.commons.math3.transform.FastHadamardTransformerTest::test3Points
    public void test3Points() {
        try {
            new FastHadamardTransformer().transform(new double[3], TransformType.FORWARD);
            Assert.fail("an exception should have been thrown");
        } catch (IllegalArgumentException iae) {
            
        }
    }

// org.apache.commons.math3.transform.FastSineTransformerTest::testTransformRealFirstElementNotZero
    public void testTransformRealFirstElementNotZero() {
        final TransformType[] type = TransformType.values();
        final double[] data = new double[] {
            1.0, 1.0, 1.0, 1.0
        };
        final RealTransformer transformer = createRealTransformer();
        for (int j = 0; j < type.length; j++) {
            try {
                transformer.transform(data, type[j]);
                Assert.fail(type[j].toString());
            } catch (MathIllegalArgumentException e) {
                
            }
        }
    }

// org.apache.commons.math3.transform.FastSineTransformerTest::testAdHocData
    public void testAdHocData() {
        FastSineTransformer transformer;
        transformer = new FastSineTransformer(DstNormalization.STANDARD_DST_I);
        double result[], tolerance = 1E-12;

        double x[] = { 0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0 };
        double y[] = { 0.0, 20.1093579685034, -9.65685424949238,
                       5.98642305066196, -4.0, 2.67271455167720,
                      -1.65685424949238, 0.795649469518633 };

        result = transformer.transform(x, TransformType.FORWARD);
        for (int i = 0; i < result.length; i++) {
            Assert.assertEquals(y[i], result[i], tolerance);
        }

        result = transformer.transform(y, TransformType.INVERSE);
        for (int i = 0; i < result.length; i++) {
            Assert.assertEquals(x[i], result[i], tolerance);
        }

        TransformUtils.scaleArray(x, FastMath.sqrt(x.length / 2.0));
        transformer = new FastSineTransformer(DstNormalization.ORTHOGONAL_DST_I);

        result = transformer.transform(y, TransformType.FORWARD);
        for (int i = 0; i < result.length; i++) {
            Assert.assertEquals(x[i], result[i], tolerance);
        }

        result = transformer.transform(x, TransformType.INVERSE);
        for (int i = 0; i < result.length; i++) {
            Assert.assertEquals(y[i], result[i], tolerance);
        }
    }

// org.apache.commons.math3.transform.FastSineTransformerTest::testSinFunction
    public void testSinFunction() {
        UnivariateFunction f = new Sin();
        FastSineTransformer transformer;
        transformer = new FastSineTransformer(DstNormalization.STANDARD_DST_I);
        double min, max, result[], tolerance = 1E-12; int N = 1 << 8;

        min = 0.0; max = 2.0 * FastMath.PI;
        result = transformer.transform(f, min, max, N, TransformType.FORWARD);
        Assert.assertEquals(N >> 1, result[2], tolerance);
        for (int i = 0; i < N; i += (i == 1 ? 2 : 1)) {
            Assert.assertEquals(0.0, result[i], tolerance);
        }

        min = -FastMath.PI; max = FastMath.PI;
        result = transformer.transform(f, min, max, N, TransformType.FORWARD);
        Assert.assertEquals(-(N >> 1), result[2], tolerance);
        for (int i = 0; i < N; i += (i == 1 ? 2 : 1)) {
            Assert.assertEquals(0.0, result[i], tolerance);
        }
    }

// org.apache.commons.math3.transform.FastSineTransformerTest::testParameters
    public void testParameters() throws Exception {
        UnivariateFunction f = new Sin();
        FastSineTransformer transformer;
        transformer = new FastSineTransformer(DstNormalization.STANDARD_DST_I);

        try {
            
            transformer.transform(f, 1, -1, 64, TransformType.FORWARD);
            Assert.fail("Expecting IllegalArgumentException - bad interval");
        } catch (IllegalArgumentException ex) {
            
        }
        try {
            
            transformer.transform(f, -1, 1, 0, TransformType.FORWARD);
            Assert.fail("Expecting IllegalArgumentException - bad samples number");
        } catch (IllegalArgumentException ex) {
            
        }
        try {
            
            transformer.transform(f, -1, 1, 100, TransformType.FORWARD);
            Assert.fail("Expecting IllegalArgumentException - bad samples number");
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math3.util.ArithmeticUtilsTest::test0Choose0
    public void test0Choose0() {
        Assert.assertEquals(ArithmeticUtils.binomialCoefficientDouble(0, 0), 1d, 0);
        Assert.assertEquals(ArithmeticUtils.binomialCoefficientLog(0, 0), 0d, 0);
        Assert.assertEquals(ArithmeticUtils.binomialCoefficient(0, 0), 1);
    }

// org.apache.commons.math3.util.ArithmeticUtilsTest::testAddAndCheck
    public void testAddAndCheck() {
        int big = Integer.MAX_VALUE;
        int bigNeg = Integer.MIN_VALUE;
        Assert.assertEquals(big, ArithmeticUtils.addAndCheck(big, 0));
        try {
            ArithmeticUtils.addAndCheck(big, 1);
            Assert.fail("Expecting MathArithmeticException");
        } catch (MathArithmeticException ex) {
        }
        try {
            ArithmeticUtils.addAndCheck(bigNeg, -1);
            Assert.fail("Expecting MathArithmeticException");
        } catch (MathArithmeticException ex) {
        }
    }

// org.apache.commons.math3.util.ArithmeticUtilsTest::testAddAndCheckLong
    public void testAddAndCheckLong() {
        long max = Long.MAX_VALUE;
        long min = Long.MIN_VALUE;
        Assert.assertEquals(max, ArithmeticUtils.addAndCheck(max, 0L));
        Assert.assertEquals(min, ArithmeticUtils.addAndCheck(min, 0L));
        Assert.assertEquals(max, ArithmeticUtils.addAndCheck(0L, max));
        Assert.assertEquals(min, ArithmeticUtils.addAndCheck(0L, min));
        Assert.assertEquals(1, ArithmeticUtils.addAndCheck(-1L, 2L));
        Assert.assertEquals(1, ArithmeticUtils.addAndCheck(2L, -1L));
        Assert.assertEquals(-3, ArithmeticUtils.addAndCheck(-2L, -1L));
        Assert.assertEquals(min, ArithmeticUtils.addAndCheck(min + 1, -1L));
        testAddAndCheckLongFailure(max, 1L);
        testAddAndCheckLongFailure(min, -1L);
        testAddAndCheckLongFailure(1L, max);
        testAddAndCheckLongFailure(-1L, min);
    }

// org.apache.commons.math3.util.ArithmeticUtilsTest::testBinomialCoefficient
    public void testBinomialCoefficient() {
        long[] bcoef5 = {
            1,
            5,
            10,
            10,
            5,
            1 };
        long[] bcoef6 = {
            1,
            6,
            15,
            20,
            15,
            6,
            1 };
        for (int i = 0; i < 6; i++) {
            Assert.assertEquals("5 choose " + i, bcoef5[i], ArithmeticUtils.binomialCoefficient(5, i));
        }
        for (int i = 0; i < 7; i++) {
            Assert.assertEquals("6 choose " + i, bcoef6[i], ArithmeticUtils.binomialCoefficient(6, i));
        }

        for (int n = 1; n < 10; n++) {
            for (int k = 0; k <= n; k++) {
                Assert.assertEquals(n + " choose " + k, binomialCoefficient(n, k), ArithmeticUtils.binomialCoefficient(n, k));
                Assert.assertEquals(n + " choose " + k, binomialCoefficient(n, k), ArithmeticUtils.binomialCoefficientDouble(n, k), Double.MIN_VALUE);
                Assert.assertEquals(n + " choose " + k, FastMath.log(binomialCoefficient(n, k)), ArithmeticUtils.binomialCoefficientLog(n, k), 10E-12);
            }
        }

        int[] n = { 34, 66, 100, 1500, 1500 };
        int[] k = { 17, 33, 10, 1500 - 4, 4 };
        for (int i = 0; i < n.length; i++) {
            long expected = binomialCoefficient(n[i], k[i]);
            Assert.assertEquals(n[i] + " choose " + k[i], expected,
                ArithmeticUtils.binomialCoefficient(n[i], k[i]));
            Assert.assertEquals(n[i] + " choose " + k[i], expected,
                ArithmeticUtils.binomialCoefficientDouble(n[i], k[i]), 0.0);
            Assert.assertEquals("log(" + n[i] + " choose " + k[i] + ")", FastMath.log(expected),
                ArithmeticUtils.binomialCoefficientLog(n[i], k[i]), 0.0);
        }
    }

// org.apache.commons.math3.util.ArithmeticUtilsTest::testBinomialCoefficientFail
    public void testBinomialCoefficientFail() {
        try {
            ArithmeticUtils.binomialCoefficient(4, 5);
            Assert.fail("expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }

        try {
            ArithmeticUtils.binomialCoefficientDouble(4, 5);
            Assert.fail("expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }

        try {
            ArithmeticUtils.binomialCoefficientLog(4, 5);
            Assert.fail("expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }

        try {
            ArithmeticUtils.binomialCoefficient(-1, -2);
            Assert.fail("expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
        try {
            ArithmeticUtils.binomialCoefficientDouble(-1, -2);
            Assert.fail("expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
        try {
            ArithmeticUtils.binomialCoefficientLog(-1, -2);
            Assert.fail("expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }

        try {
            ArithmeticUtils.binomialCoefficient(67, 30);
            Assert.fail("expecting MathArithmeticException");
        } catch (MathArithmeticException ex) {
            
        }
        try {
            ArithmeticUtils.binomialCoefficient(67, 34);
            Assert.fail("expecting MathArithmeticException");
        } catch (MathArithmeticException ex) {
            
        }
        double x = ArithmeticUtils.binomialCoefficientDouble(1030, 515);
        Assert.assertTrue("expecting infinite binomial coefficient", Double
            .isInfinite(x));
    }

// org.apache.commons.math3.util.ArithmeticUtilsTest::testBinomialCoefficientLarge
    public void testBinomialCoefficientLarge() throws Exception {
        
        for (int n = 0; n <= 200; n++) {
            for (int k = 0; k <= n; k++) {
                long ourResult = -1;
                long exactResult = -1;
                boolean shouldThrow = false;
                boolean didThrow = false;
                try {
                    ourResult = ArithmeticUtils.binomialCoefficient(n, k);
                } catch (MathArithmeticException ex) {
                    didThrow = true;
                }
                try {
                    exactResult = binomialCoefficient(n, k);
                } catch (MathArithmeticException ex) {
                    shouldThrow = true;
                }
                Assert.assertEquals(n + " choose " + k, exactResult, ourResult);
                Assert.assertEquals(n + " choose " + k, shouldThrow, didThrow);
                Assert.assertTrue(n + " choose " + k, (n > 66 || !didThrow));

                if (!shouldThrow && exactResult > 1) {
                    Assert.assertEquals(n + " choose " + k, 1.,
                        ArithmeticUtils.binomialCoefficientDouble(n, k) / exactResult, 1e-10);
                    Assert.assertEquals(n + " choose " + k, 1,
                        ArithmeticUtils.binomialCoefficientLog(n, k) / FastMath.log(exactResult), 1e-10);
                }
            }
        }

        long ourResult = ArithmeticUtils.binomialCoefficient(300, 3);
        long exactResult = binomialCoefficient(300, 3);
        Assert.assertEquals(exactResult, ourResult);

        ourResult = ArithmeticUtils.binomialCoefficient(700, 697);
        exactResult = binomialCoefficient(700, 697);
        Assert.assertEquals(exactResult, ourResult);

        
        try {
            ArithmeticUtils.binomialCoefficient(700, 300);
            Assert.fail("Expecting MathArithmeticException");
        } catch (MathArithmeticException ex) {
            
        }

        int n = 10000;
        ourResult = ArithmeticUtils.binomialCoefficient(n, 3);
        exactResult = binomialCoefficient(n, 3);
        Assert.assertEquals(exactResult, ourResult);
        Assert.assertEquals(1, ArithmeticUtils.binomialCoefficientDouble(n, 3) / exactResult, 1e-10);
        Assert.assertEquals(1, ArithmeticUtils.binomialCoefficientLog(n, 3) / FastMath.log(exactResult), 1e-10);

    }

// org.apache.commons.math3.util.ArithmeticUtilsTest::testFactorial
    public void testFactorial() {
        for (int i = 1; i < 21; i++) {
            Assert.assertEquals(i + "! ", factorial(i), ArithmeticUtils.factorial(i));
            Assert.assertEquals(i + "! ", factorial(i), ArithmeticUtils.factorialDouble(i), Double.MIN_VALUE);
            Assert.assertEquals(i + "! ", FastMath.log(factorial(i)), ArithmeticUtils.factorialLog(i), 10E-12);
        }

        Assert.assertEquals("0", 1, ArithmeticUtils.factorial(0));
        Assert.assertEquals("0", 1.0d, ArithmeticUtils.factorialDouble(0), 1E-14);
        Assert.assertEquals("0", 0.0d, ArithmeticUtils.factorialLog(0), 1E-14);
    }

// org.apache.commons.math3.util.ArithmeticUtilsTest::testFactorialFail
    public void testFactorialFail() {
        try {
            ArithmeticUtils.factorial(-1);
            Assert.fail("expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
        try {
            ArithmeticUtils.factorialDouble(-1);
            Assert.fail("expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
        try {
            ArithmeticUtils.factorialLog(-1);
            Assert.fail("expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
        try {
            ArithmeticUtils.factorial(21);
            Assert.fail("expecting MathArithmeticException");
        } catch (MathArithmeticException ex) {
            
        }
        Assert.assertTrue("expecting infinite factorial value", Double.isInfinite(ArithmeticUtils.factorialDouble(171)));
    }

// org.apache.commons.math3.util.ArithmeticUtilsTest::testGcd
    public void testGcd() {
        int a = 30;
        int b = 50;
        int c = 77;

        Assert.assertEquals(0, ArithmeticUtils.gcd(0, 0));

        Assert.assertEquals(b, ArithmeticUtils.gcd(0, b));
        Assert.assertEquals(a, ArithmeticUtils.gcd(a, 0));
        Assert.assertEquals(b, ArithmeticUtils.gcd(0, -b));
        Assert.assertEquals(a, ArithmeticUtils.gcd(-a, 0));

        Assert.assertEquals(10, ArithmeticUtils.gcd(a, b));
        Assert.assertEquals(10, ArithmeticUtils.gcd(-a, b));
        Assert.assertEquals(10, ArithmeticUtils.gcd(a, -b));
        Assert.assertEquals(10, ArithmeticUtils.gcd(-a, -b));

        Assert.assertEquals(1, ArithmeticUtils.gcd(a, c));
        Assert.assertEquals(1, ArithmeticUtils.gcd(-a, c));
        Assert.assertEquals(1, ArithmeticUtils.gcd(a, -c));
        Assert.assertEquals(1, ArithmeticUtils.gcd(-a, -c));

        Assert.assertEquals(3 * (1<<15), ArithmeticUtils.gcd(3 * (1<<20), 9 * (1<<15)));

        Assert.assertEquals(Integer.MAX_VALUE, ArithmeticUtils.gcd(Integer.MAX_VALUE, 0));
        Assert.assertEquals(Integer.MAX_VALUE, ArithmeticUtils.gcd(-Integer.MAX_VALUE, 0));
        Assert.assertEquals(1<<30, ArithmeticUtils.gcd(1<<30, -Integer.MIN_VALUE));
        try {
            
            ArithmeticUtils.gcd(Integer.MIN_VALUE, 0);
            Assert.fail("expecting MathArithmeticException");
        } catch (MathArithmeticException expected) {
            
        }
        try {
            
            ArithmeticUtils.gcd(0, Integer.MIN_VALUE);
            Assert.fail("expecting MathArithmeticException");
        } catch (MathArithmeticException expected) {
            
        }
        try {
            
            ArithmeticUtils.gcd(Integer.MIN_VALUE, Integer.MIN_VALUE);
            Assert.fail("expecting MathArithmeticException");
        } catch (MathArithmeticException expected) {
            
        }
    }

// org.apache.commons.math3.util.ArithmeticUtilsTest::testGcdConsistency
    public void testGcdConsistency() {
        int[] primeList = {19, 23, 53, 67, 73, 79, 101, 103, 111, 131};
        ArrayList<Integer> primes = new ArrayList<Integer>();
        for (int i = 0; i < primeList.length; i++) {
            primes.add(Integer.valueOf(primeList[i]));
        }
        RandomDataImpl randomData = new RandomDataImpl();
        for (int i = 0; i < 20; i++) {
            Object[] sample = randomData.nextSample(primes, 4);
            int p1 = ((Integer) sample[0]).intValue();
            int p2 = ((Integer) sample[1]).intValue();
            int p3 = ((Integer) sample[2]).intValue();
            int p4 = ((Integer) sample[3]).intValue();
            int i1 = p1 * p2 * p3;
            int i2 = p1 * p2 * p4;
            int gcd = p1 * p2;
            Assert.assertEquals(gcd, ArithmeticUtils.gcd(i1, i2));
            long l1 = i1;
            long l2 = i2;
            Assert.assertEquals(gcd, ArithmeticUtils.gcd(l1, l2));
        }
    }

// org.apache.commons.math3.util.ArithmeticUtilsTest::testGcdLong
    public void  testGcdLong(){
        long a = 30;
        long b = 50;
        long c = 77;

        Assert.assertEquals(0, ArithmeticUtils.gcd(0L, 0));

        Assert.assertEquals(b, ArithmeticUtils.gcd(0, b));
        Assert.assertEquals(a, ArithmeticUtils.gcd(a, 0));
        Assert.assertEquals(b, ArithmeticUtils.gcd(0, -b));
        Assert.assertEquals(a, ArithmeticUtils.gcd(-a, 0));

        Assert.assertEquals(10, ArithmeticUtils.gcd(a, b));
        Assert.assertEquals(10, ArithmeticUtils.gcd(-a, b));
        Assert.assertEquals(10, ArithmeticUtils.gcd(a, -b));
        Assert.assertEquals(10, ArithmeticUtils.gcd(-a, -b));

        Assert.assertEquals(1, ArithmeticUtils.gcd(a, c));
        Assert.assertEquals(1, ArithmeticUtils.gcd(-a, c));
        Assert.assertEquals(1, ArithmeticUtils.gcd(a, -c));
        Assert.assertEquals(1, ArithmeticUtils.gcd(-a, -c));

        Assert.assertEquals(3L * (1L<<45), ArithmeticUtils.gcd(3L * (1L<<50), 9L * (1L<<45)));

        Assert.assertEquals(1L<<45, ArithmeticUtils.gcd(1L<<45, Long.MIN_VALUE));

        Assert.assertEquals(Long.MAX_VALUE, ArithmeticUtils.gcd(Long.MAX_VALUE, 0L));
        Assert.assertEquals(Long.MAX_VALUE, ArithmeticUtils.gcd(-Long.MAX_VALUE, 0L));
        Assert.assertEquals(1, ArithmeticUtils.gcd(60247241209L, 153092023L));
        try {
            
            ArithmeticUtils.gcd(Long.MIN_VALUE, 0);
            Assert.fail("expecting MathArithmeticException");
        } catch (MathArithmeticException expected) {
            
        }
        try {
            
            ArithmeticUtils.gcd(0, Long.MIN_VALUE);
            Assert.fail("expecting MathArithmeticException");
        } catch (MathArithmeticException expected) {
            
        }
        try {
            
            ArithmeticUtils.gcd(Long.MIN_VALUE, Long.MIN_VALUE);
            Assert.fail("expecting MathArithmeticException");
        } catch (MathArithmeticException expected) {
            
        }
    }

// org.apache.commons.math3.util.ArithmeticUtilsTest::testLcm
    public void testLcm() {
        int a = 30;
        int b = 50;
        int c = 77;

        Assert.assertEquals(0, ArithmeticUtils.lcm(0, b));
        Assert.assertEquals(0, ArithmeticUtils.lcm(a, 0));
        Assert.assertEquals(b, ArithmeticUtils.lcm(1, b));
        Assert.assertEquals(a, ArithmeticUtils.lcm(a, 1));
        Assert.assertEquals(150, ArithmeticUtils.lcm(a, b));
        Assert.assertEquals(150, ArithmeticUtils.lcm(-a, b));
        Assert.assertEquals(150, ArithmeticUtils.lcm(a, -b));
        Assert.assertEquals(150, ArithmeticUtils.lcm(-a, -b));
        Assert.assertEquals(2310, ArithmeticUtils.lcm(a, c));

        
        
        Assert.assertEquals((1<<20)*15, ArithmeticUtils.lcm((1<<20)*3, (1<<20)*5));

        
        Assert.assertEquals(0, ArithmeticUtils.lcm(0, 0));

        try {
            
            ArithmeticUtils.lcm(Integer.MIN_VALUE, 1);
            Assert.fail("Expecting MathArithmeticException");
        } catch (MathArithmeticException expected) {
            
        }

        try {
            
            ArithmeticUtils.lcm(Integer.MIN_VALUE, 1<<20);
            Assert.fail("Expecting MathArithmeticException");
        } catch (MathArithmeticException expected) {
            
        }

        try {
            ArithmeticUtils.lcm(Integer.MAX_VALUE, Integer.MAX_VALUE - 1);
            Assert.fail("Expecting MathArithmeticException");
        } catch (MathArithmeticException expected) {
            
        }
    }

// org.apache.commons.math3.util.ArithmeticUtilsTest::testLcmLong
    public void testLcmLong() {
        long a = 30;
        long b = 50;
        long c = 77;

        Assert.assertEquals(0, ArithmeticUtils.lcm(0, b));
        Assert.assertEquals(0, ArithmeticUtils.lcm(a, 0));
        Assert.assertEquals(b, ArithmeticUtils.lcm(1, b));
        Assert.assertEquals(a, ArithmeticUtils.lcm(a, 1));
        Assert.assertEquals(150, ArithmeticUtils.lcm(a, b));
        Assert.assertEquals(150, ArithmeticUtils.lcm(-a, b));
        Assert.assertEquals(150, ArithmeticUtils.lcm(a, -b));
        Assert.assertEquals(150, ArithmeticUtils.lcm(-a, -b));
        Assert.assertEquals(2310, ArithmeticUtils.lcm(a, c));

        Assert.assertEquals(Long.MAX_VALUE, ArithmeticUtils.lcm(60247241209L, 153092023L));

        
        
        Assert.assertEquals((1L<<50)*15, ArithmeticUtils.lcm((1L<<45)*3, (1L<<50)*5));

        
        Assert.assertEquals(0L, ArithmeticUtils.lcm(0L, 0L));

        try {
            
            ArithmeticUtils.lcm(Long.MIN_VALUE, 1);
            Assert.fail("Expecting MathArithmeticException");
        } catch (MathArithmeticException expected) {
            
        }

        try {
            
            ArithmeticUtils.lcm(Long.MIN_VALUE, 1<<20);
            Assert.fail("Expecting MathArithmeticException");
        } catch (MathArithmeticException expected) {
            
        }

        Assert.assertEquals((long) Integer.MAX_VALUE * (Integer.MAX_VALUE - 1),
            ArithmeticUtils.lcm((long)Integer.MAX_VALUE, Integer.MAX_VALUE - 1));
        try {
            ArithmeticUtils.lcm(Long.MAX_VALUE, Long.MAX_VALUE - 1);
            Assert.fail("Expecting MathArithmeticException");
        } catch (MathArithmeticException expected) {
            
        }
    }

// org.apache.commons.math3.util.ArithmeticUtilsTest::testMulAndCheck
    public void testMulAndCheck() {
        int big = Integer.MAX_VALUE;
        int bigNeg = Integer.MIN_VALUE;
        Assert.assertEquals(big, ArithmeticUtils.mulAndCheck(big, 1));
        try {
            ArithmeticUtils.mulAndCheck(big, 2);
            Assert.fail("Expecting MathArithmeticException");
        } catch (MathArithmeticException ex) {
        }
        try {
            ArithmeticUtils.mulAndCheck(bigNeg, 2);
            Assert.fail("Expecting MathArithmeticException");
        } catch (MathArithmeticException ex) {
        }
    }

// org.apache.commons.math3.util.ArithmeticUtilsTest::testMulAndCheckLong
    public void testMulAndCheckLong() {
        long max = Long.MAX_VALUE;
        long min = Long.MIN_VALUE;
        Assert.assertEquals(max, ArithmeticUtils.mulAndCheck(max, 1L));
        Assert.assertEquals(min, ArithmeticUtils.mulAndCheck(min, 1L));
        Assert.assertEquals(0L, ArithmeticUtils.mulAndCheck(max, 0L));
        Assert.assertEquals(0L, ArithmeticUtils.mulAndCheck(min, 0L));
        Assert.assertEquals(max, ArithmeticUtils.mulAndCheck(1L, max));
        Assert.assertEquals(min, ArithmeticUtils.mulAndCheck(1L, min));
        Assert.assertEquals(0L, ArithmeticUtils.mulAndCheck(0L, max));
        Assert.assertEquals(0L, ArithmeticUtils.mulAndCheck(0L, min));
        Assert.assertEquals(1L, ArithmeticUtils.mulAndCheck(-1L, -1L));
        Assert.assertEquals(min, ArithmeticUtils.mulAndCheck(min / 2, 2));
        testMulAndCheckLongFailure(max, 2L);
        testMulAndCheckLongFailure(2L, max);
        testMulAndCheckLongFailure(min, 2L);
        testMulAndCheckLongFailure(2L, min);
        testMulAndCheckLongFailure(min, -1L);
        testMulAndCheckLongFailure(-1L, min);
    }

// org.apache.commons.math3.util.ArithmeticUtilsTest::testSubAndCheck
    public void testSubAndCheck() {
        int big = Integer.MAX_VALUE;
        int bigNeg = Integer.MIN_VALUE;
        Assert.assertEquals(big, ArithmeticUtils.subAndCheck(big, 0));
        Assert.assertEquals(bigNeg + 1, ArithmeticUtils.subAndCheck(bigNeg, -1));
        Assert.assertEquals(-1, ArithmeticUtils.subAndCheck(bigNeg, -big));
        try {
            ArithmeticUtils.subAndCheck(big, -1);
            Assert.fail("Expecting MathArithmeticException");
        } catch (MathArithmeticException ex) {
        }
        try {
            ArithmeticUtils.subAndCheck(bigNeg, 1);
            Assert.fail("Expecting MathArithmeticException");
        } catch (MathArithmeticException ex) {
        }
    }

// org.apache.commons.math3.util.ArithmeticUtilsTest::testSubAndCheckErrorMessage
    public void testSubAndCheckErrorMessage() {
        int big = Integer.MAX_VALUE;
        try {
            ArithmeticUtils.subAndCheck(big, -1);
            Assert.fail("Expecting MathArithmeticException");
        } catch (MathArithmeticException ex) {
            Assert.assertTrue(ex.getMessage().length() > 1);
        }
    }

// org.apache.commons.math3.util.ArithmeticUtilsTest::testSubAndCheckLong
    public void testSubAndCheckLong() {
        long max = Long.MAX_VALUE;
        long min = Long.MIN_VALUE;
        Assert.assertEquals(max, ArithmeticUtils.subAndCheck(max, 0));
        Assert.assertEquals(min, ArithmeticUtils.subAndCheck(min, 0));
        Assert.assertEquals(-max, ArithmeticUtils.subAndCheck(0, max));
        Assert.assertEquals(min + 1, ArithmeticUtils.subAndCheck(min, -1));
        
        Assert.assertEquals(-1, ArithmeticUtils.subAndCheck(-max - 1, -max));
        Assert.assertEquals(max, ArithmeticUtils.subAndCheck(-1, -1 - max));
        testSubAndCheckLongFailure(0L, min);
        testSubAndCheckLongFailure(max, -1L);
        testSubAndCheckLongFailure(min, 1L);
    }

// org.apache.commons.math3.util.ArithmeticUtilsTest::testPow
    public void testPow() {

        Assert.assertEquals(1801088541, ArithmeticUtils.pow(21, 7));
        Assert.assertEquals(1, ArithmeticUtils.pow(21, 0));
        try {
            ArithmeticUtils.pow(21, -7);
            Assert.fail("Expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException e) {
            
        }

        Assert.assertEquals(1801088541, ArithmeticUtils.pow(21, 7l));
        Assert.assertEquals(1, ArithmeticUtils.pow(21, 0l));
        try {
            ArithmeticUtils.pow(21, -7l);
            Assert.fail("Expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException e) {
            
        }

        Assert.assertEquals(1801088541l, ArithmeticUtils.pow(21l, 7));
        Assert.assertEquals(1l, ArithmeticUtils.pow(21l, 0));
        try {
            ArithmeticUtils.pow(21l, -7);
            Assert.fail("Expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException e) {
            
        }

        Assert.assertEquals(1801088541l, ArithmeticUtils.pow(21l, 7l));
        Assert.assertEquals(1l, ArithmeticUtils.pow(21l, 0l));
        try {
            ArithmeticUtils.pow(21l, -7l);
            Assert.fail("Expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException e) {
            
        }

        BigInteger twentyOne = BigInteger.valueOf(21l);
        Assert.assertEquals(BigInteger.valueOf(1801088541l), ArithmeticUtils.pow(twentyOne, 7));
        Assert.assertEquals(BigInteger.ONE, ArithmeticUtils.pow(twentyOne, 0));
        try {
            ArithmeticUtils.pow(twentyOne, -7);
            Assert.fail("Expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException e) {
            
        }

        Assert.assertEquals(BigInteger.valueOf(1801088541l), ArithmeticUtils.pow(twentyOne, 7l));
        Assert.assertEquals(BigInteger.ONE, ArithmeticUtils.pow(twentyOne, 0l));
        try {
            ArithmeticUtils.pow(twentyOne, -7l);
            Assert.fail("Expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException e) {
            
        }

        Assert.assertEquals(BigInteger.valueOf(1801088541l), ArithmeticUtils.pow(twentyOne, BigInteger.valueOf(7l)));
        Assert.assertEquals(BigInteger.ONE, ArithmeticUtils.pow(twentyOne, BigInteger.ZERO));
        try {
            ArithmeticUtils.pow(twentyOne, BigInteger.valueOf(-7l));
            Assert.fail("Expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException e) {
            
        }

        BigInteger bigOne =
            new BigInteger("1543786922199448028351389769265814882661837148" +
                           "4763915343722775611762713982220306372888519211" +
                           "560905579993523402015636025177602059044911261");
        Assert.assertEquals(bigOne, ArithmeticUtils.pow(twentyOne, 103));
        Assert.assertEquals(bigOne, ArithmeticUtils.pow(twentyOne, 103l));
        Assert.assertEquals(bigOne, ArithmeticUtils.pow(twentyOne, BigInteger.valueOf(103l)));

    }

// org.apache.commons.math3.util.ArithmeticUtilsTest::testIsPowerOfTwo
    public void testIsPowerOfTwo() {
        final int n = 1025;
        final boolean[] expected = new boolean[n];
        Arrays.fill(expected, false);
        for (int i = 1; i < expected.length; i *= 2) {
            expected[i] = true;
        }
        for (int i = 0; i < expected.length; i++) {
            final boolean actual = ArithmeticUtils.isPowerOfTwo(i);
            Assert.assertTrue(Integer.toString(i), actual == expected[i]);
        }
    }

// org.apache.commons.math3.util.ArithmeticUtilsTest::testStirlingS2
    public void testStirlingS2() {

        Assert.assertEquals(1, ArithmeticUtils.stirlingS2(0, 0));

        for (int n = 1; n < 30; ++n) {
            Assert.assertEquals(0, ArithmeticUtils.stirlingS2(n, 0));
            Assert.assertEquals(1, ArithmeticUtils.stirlingS2(n, 1));
            if (n > 2) {
                Assert.assertEquals((1l << (n - 1)) - 1l, ArithmeticUtils.stirlingS2(n, 2));
                Assert.assertEquals(ArithmeticUtils.binomialCoefficient(n, 2),
                                    ArithmeticUtils.stirlingS2(n, n - 1));
            }
            Assert.assertEquals(1, ArithmeticUtils.stirlingS2(n, n));
        }
        Assert.assertEquals(536870911l, ArithmeticUtils.stirlingS2(30, 2));
        Assert.assertEquals(576460752303423487l, ArithmeticUtils.stirlingS2(60, 2));

        Assert.assertEquals(   25, ArithmeticUtils.stirlingS2( 5, 3));
        Assert.assertEquals(   90, ArithmeticUtils.stirlingS2( 6, 3));
        Assert.assertEquals(   65, ArithmeticUtils.stirlingS2( 6, 4));
        Assert.assertEquals(  301, ArithmeticUtils.stirlingS2( 7, 3));
        Assert.assertEquals(  350, ArithmeticUtils.stirlingS2( 7, 4));
        Assert.assertEquals(  140, ArithmeticUtils.stirlingS2( 7, 5));
        Assert.assertEquals(  966, ArithmeticUtils.stirlingS2( 8, 3));
        Assert.assertEquals( 1701, ArithmeticUtils.stirlingS2( 8, 4));
        Assert.assertEquals( 1050, ArithmeticUtils.stirlingS2( 8, 5));
        Assert.assertEquals(  266, ArithmeticUtils.stirlingS2( 8, 6));
        Assert.assertEquals( 3025, ArithmeticUtils.stirlingS2( 9, 3));
        Assert.assertEquals( 7770, ArithmeticUtils.stirlingS2( 9, 4));
        Assert.assertEquals( 6951, ArithmeticUtils.stirlingS2( 9, 5));
        Assert.assertEquals( 2646, ArithmeticUtils.stirlingS2( 9, 6));
        Assert.assertEquals(  462, ArithmeticUtils.stirlingS2( 9, 7));
        Assert.assertEquals( 9330, ArithmeticUtils.stirlingS2(10, 3));
        Assert.assertEquals(34105, ArithmeticUtils.stirlingS2(10, 4));
        Assert.assertEquals(42525, ArithmeticUtils.stirlingS2(10, 5));
        Assert.assertEquals(22827, ArithmeticUtils.stirlingS2(10, 6));
        Assert.assertEquals( 5880, ArithmeticUtils.stirlingS2(10, 7));
        Assert.assertEquals(  750, ArithmeticUtils.stirlingS2(10, 8));

    }

// org.apache.commons.math3.util.ArithmeticUtilsTest::testStirlingS2NegativeN
    public void testStirlingS2NegativeN() {
        ArithmeticUtils.stirlingS2(3, -1);
    }

// org.apache.commons.math3.util.ArithmeticUtilsTest::testStirlingS2LargeK
    public void testStirlingS2LargeK() {
        ArithmeticUtils.stirlingS2(3, 4);
    }

// org.apache.commons.math3.util.ArithmeticUtilsTest::testStirlingS2Overflow
    public void testStirlingS2Overflow() {
        ArithmeticUtils.stirlingS2(26, 9);
    }

// org.apache.commons.math3.util.BigRealTest::testConstructor
    public void testConstructor() {
        Assert.assertEquals(1.625,
                            new BigReal(new BigDecimal("1.625")).doubleValue(),
                            1.0e-15);
        Assert.assertEquals(-5.0,
                            new BigReal(new BigInteger("-5")).doubleValue(),
                            1.0e-15);
        Assert.assertEquals(-5.0, new BigReal(new BigInteger("-5"),
                                              MathContext.DECIMAL64)
            .doubleValue(), 1.0e-15);
        Assert
            .assertEquals(0.125,
                          new BigReal(new BigInteger("125"), 3).doubleValue(),
                          1.0e-15);
        Assert.assertEquals(0.125, new BigReal(new BigInteger("125"), 3,
                                               MathContext.DECIMAL64)
            .doubleValue(), 1.0e-15);
        Assert.assertEquals(1.625, new BigReal(new char[] {
            '1', '.', '6', '2', '5'
        }).doubleValue(), 1.0e-15);
        Assert.assertEquals(1.625, new BigReal(new char[] {
            'A', 'A', '1', '.', '6', '2', '5', '9'
        }, 2, 5).doubleValue(), 1.0e-15);
        Assert.assertEquals(1.625, new BigReal(new char[] {
            'A', 'A', '1', '.', '6', '2', '5', '9'
        }, 2, 5, MathContext.DECIMAL64).doubleValue(), 1.0e-15);
        Assert.assertEquals(1.625, new BigReal(new char[] {
            '1', '.', '6', '2', '5'
        }, MathContext.DECIMAL64).doubleValue(), 1.0e-15);
        Assert.assertEquals(1.625, new BigReal(1.625).doubleValue(), 1.0e-15);
        Assert.assertEquals(1.625, new BigReal(1.625, MathContext.DECIMAL64)
            .doubleValue(), 1.0e-15);
        Assert.assertEquals(-5.0, new BigReal(-5).doubleValue(), 1.0e-15);
        Assert.assertEquals(-5.0, new BigReal(-5, MathContext.DECIMAL64)
            .doubleValue(), 1.0e-15);
        Assert.assertEquals(-5.0, new BigReal(-5l).doubleValue(), 1.0e-15);
        Assert.assertEquals(-5.0, new BigReal(-5l, MathContext.DECIMAL64)
            .doubleValue(), 1.0e-15);
        Assert.assertEquals(1.625, new BigReal("1.625").doubleValue(), 1.0e-15);
        Assert.assertEquals(1.625, new BigReal("1.625", MathContext.DECIMAL64)
            .doubleValue(), 1.0e-15);
    }

// org.apache.commons.math3.util.BigRealTest::testCompareTo
    public void testCompareTo() {
        BigReal first = new BigReal(1.0 / 2.0);
        BigReal second = new BigReal(1.0 / 3.0);
        BigReal third = new BigReal(1.0 / 2.0);

        Assert.assertEquals(0, first.compareTo(first));
        Assert.assertEquals(0, first.compareTo(third));
        Assert.assertEquals(1, first.compareTo(second));
        Assert.assertEquals(-1, second.compareTo(first));

    }

// org.apache.commons.math3.util.BigRealTest::testAdd
    public void testAdd() {
        BigReal a = new BigReal("1.2345678");
        BigReal b = new BigReal("8.7654321");
        Assert.assertEquals(9.9999999, a.add(b).doubleValue(), 1.0e-15);
    }

// org.apache.commons.math3.util.BigRealTest::testSubtract
    public void testSubtract() {
        BigReal a = new BigReal("1.2345678");
        BigReal b = new BigReal("8.7654321");
        Assert.assertEquals(-7.5308643, a.subtract(b).doubleValue(), 1.0e-15);
    }

// org.apache.commons.math3.util.BigRealTest::testNegate
    public void testNegate() {
        BigReal a = new BigReal("1.2345678");
        BigReal zero = new BigReal("0.0000000");
        Assert.assertEquals(a.negate().add(a), zero);
        Assert.assertEquals(a.add(a.negate()), zero);
        Assert.assertEquals(zero, zero.negate());
    }

// org.apache.commons.math3.util.BigRealTest::testDivide
    public void testDivide() {
        BigReal a = new BigReal("1.0000000000");
        BigReal b = new BigReal("0.0009765625");
        Assert.assertEquals(1024.0, a.divide(b).doubleValue(), 1.0e-15);
    }

// org.apache.commons.math3.util.BigRealTest::testDivisionByZero
    public void testDivisionByZero() {
        final BigReal a = BigReal.ONE;
        final BigReal b = BigReal.ZERO;
        a.divide(b);
    }

// org.apache.commons.math3.util.BigRealTest::testReciprocal
    public void testReciprocal() {
        BigReal a = new BigReal("1.2345678");
        double eps = FastMath.pow(10., -a.getScale());
        BigReal one = new BigReal("1.0000000");
        BigReal b = a.reciprocal();
        BigReal r = one.subtract(a.multiply(b));
        Assert.assertTrue(FastMath.abs(r.doubleValue()) <= eps);
        r = one.subtract(b.multiply(a));
        Assert.assertTrue(FastMath.abs(r.doubleValue()) <= eps);
    }

// org.apache.commons.math3.util.BigRealTest::testReciprocalOfZero
    public void testReciprocalOfZero() {
        BigReal.ZERO.reciprocal();
    }

// org.apache.commons.math3.util.BigRealTest::testMultiply
    public void testMultiply() {
        BigReal a = new BigReal("1024.0");
        BigReal b = new BigReal("0.0009765625");
        Assert.assertEquals(1.0, a.multiply(b).doubleValue(), 1.0e-15);
        int n = 1024;
        Assert.assertEquals(1.0, b.multiply(n).doubleValue(), 1.0e-15);
    }

// org.apache.commons.math3.util.BigRealTest::testDoubleValue
    public void testDoubleValue() {
        Assert.assertEquals(0.5, new BigReal(0.5).doubleValue(), 1.0e-15);
    }

// org.apache.commons.math3.util.BigRealTest::testBigDecimalValue
    public void testBigDecimalValue() {
        BigDecimal pi = new BigDecimal(
                                       "3.1415926535897932384626433832795028841971693993751");
        Assert.assertEquals(pi, new BigReal(pi).bigDecimalValue());
        Assert.assertEquals(new BigDecimal(0.5),
                            new BigReal(1.0 / 2.0).bigDecimalValue());
    }

// org.apache.commons.math3.util.BigRealTest::testEqualsAndHashCode
    public void testEqualsAndHashCode() {
        BigReal zero = new BigReal(0.0);
        BigReal nullReal = null;
        Assert.assertTrue(zero.equals(zero));
        Assert.assertFalse(zero.equals(nullReal));
        Assert.assertFalse(zero.equals(Double.valueOf(0)));
        BigReal zero2 = new BigReal(0.0);
        Assert.assertTrue(zero.equals(zero2));
        Assert.assertEquals(zero.hashCode(), zero2.hashCode());
        BigReal one = new BigReal(1.0);
        Assert.assertFalse((one.equals(zero) || zero.equals(one)));
        Assert.assertTrue(one.equals(BigReal.ONE));
    }

// org.apache.commons.math3.util.BigRealTest::testSerial
    public void testSerial() {
        BigReal[] Reals = {
            new BigReal(3.0), BigReal.ONE, BigReal.ZERO, new BigReal(17),
            new BigReal(FastMath.PI), new BigReal(-2.5)
        };
        for (BigReal Real : Reals) {
            Assert.assertEquals(Real, TestUtils.serializeAndRecover(Real));
        }
    }

// org.apache.commons.math3.util.ContinuedFractionTest::testGoldenRatio
    public void testGoldenRatio() throws Exception {
        ContinuedFraction cf = new ContinuedFraction() {

            @Override
            public double getA(int n, double x) {
                return 1.0;
            }

            @Override
            public double getB(int n, double x) {
                return 1.0;
            }
        };

        double gr = cf.evaluate(0.0, 10e-9);
        Assert.assertEquals(1.61803399, gr, 10e-9);
    }

// org.apache.commons.math3.util.FastMathStrictComparisonTest::test1
    public void test1() throws Exception{
        setupMethodCall(mathMethod, fastMethod, types, valueArrays);
    }

// org.apache.commons.math3.util.FastMathTest::testMinMaxDouble
    public void testMinMaxDouble() {
        double[][] pairs = {
            { -50.0, 50.0 },
            {  Double.POSITIVE_INFINITY, 1.0 },
            {  Double.NEGATIVE_INFINITY, 1.0 },
            {  Double.NaN, 1.0 },
            {  Double.POSITIVE_INFINITY, 0.0 },
            {  Double.NEGATIVE_INFINITY, 0.0 },
            {  Double.NaN, 0.0 },
            {  Double.NaN, Double.NEGATIVE_INFINITY },
            {  Double.NaN, Double.POSITIVE_INFINITY },
            { Precision.SAFE_MIN, Precision.EPSILON }
        };
        for (double[] pair : pairs) {
            Assert.assertEquals("min(" + pair[0] + ", " + pair[1] + ")",
                                Math.min(pair[0], pair[1]),
                                FastMath.min(pair[0], pair[1]),
                                Precision.EPSILON);
            Assert.assertEquals("min(" + pair[1] + ", " + pair[0] + ")",
                                Math.min(pair[1], pair[0]),
                                FastMath.min(pair[1], pair[0]),
                                Precision.EPSILON);
            Assert.assertEquals("max(" + pair[0] + ", " + pair[1] + ")",
                                Math.max(pair[0], pair[1]),
                                FastMath.max(pair[0], pair[1]),
                                Precision.EPSILON);
            Assert.assertEquals("max(" + pair[1] + ", " + pair[0] + ")",
                                Math.max(pair[1], pair[0]),
                                FastMath.max(pair[1], pair[0]),
                                Precision.EPSILON);
        }
    }

// org.apache.commons.math3.util.FastMathTest::testMinMaxFloat
    public void testMinMaxFloat() {
        float[][] pairs = {
            { -50.0f, 50.0f },
            {  Float.POSITIVE_INFINITY, 1.0f },
            {  Float.NEGATIVE_INFINITY, 1.0f },
            {  Float.NaN, 1.0f },
            {  Float.POSITIVE_INFINITY, 0.0f },
            {  Float.NEGATIVE_INFINITY, 0.0f },
            {  Float.NaN, 0.0f },
            {  Float.NaN, Float.NEGATIVE_INFINITY },
            {  Float.NaN, Float.POSITIVE_INFINITY }
        };
        for (float[] pair : pairs) {
            Assert.assertEquals("min(" + pair[0] + ", " + pair[1] + ")",
                                Math.min(pair[0], pair[1]),
                                FastMath.min(pair[0], pair[1]),
                                Precision.EPSILON);
            Assert.assertEquals("min(" + pair[1] + ", " + pair[0] + ")",
                                Math.min(pair[1], pair[0]),
                                FastMath.min(pair[1], pair[0]),
                                Precision.EPSILON);
            Assert.assertEquals("max(" + pair[0] + ", " + pair[1] + ")",
                                Math.max(pair[0], pair[1]),
                                FastMath.max(pair[0], pair[1]),
                                Precision.EPSILON);
            Assert.assertEquals("max(" + pair[1] + ", " + pair[0] + ")",
                                Math.max(pair[1], pair[0]),
                                FastMath.max(pair[1], pair[0]),
                                Precision.EPSILON);
        }
    }

// org.apache.commons.math3.util.FastMathTest::testConstants
    public void testConstants() {
        Assert.assertEquals(Math.PI, FastMath.PI, 1.0e-20);
        Assert.assertEquals(Math.E, FastMath.E, 1.0e-20);
    }

// org.apache.commons.math3.util.FastMathTest::testAtan2
    public void testAtan2() {
        double y1 = 1.2713504628280707e10;
        double x1 = -5.674940885228782e-10;
        Assert.assertEquals(Math.atan2(y1, x1), FastMath.atan2(y1, x1), 2 * Precision.EPSILON);
        double y2 = 0.0;
        double x2 = Double.POSITIVE_INFINITY;
        Assert.assertEquals(Math.atan2(y2, x2), FastMath.atan2(y2, x2), Precision.SAFE_MIN);
    }

// org.apache.commons.math3.util.FastMathTest::testHyperbolic
    public void testHyperbolic() {
        double maxErr = 0;
        for (double x = -30; x < 30; x += 0.001) {
            double tst = FastMath.sinh(x);
            double ref = Math.sinh(x);
            maxErr = FastMath.max(maxErr, FastMath.abs(ref - tst) / FastMath.ulp(ref));
        }
        Assert.assertEquals(0, maxErr, 2);

        maxErr = 0;
        for (double x = -30; x < 30; x += 0.001) {
            double tst = FastMath.cosh(x);
            double ref = Math.cosh(x);
            maxErr = FastMath.max(maxErr, FastMath.abs(ref - tst) / FastMath.ulp(ref));
        }
        Assert.assertEquals(0, maxErr, 2);

        maxErr = 0;
        for (double x = -0.5; x < 0.5; x += 0.001) {
            double tst = FastMath.tanh(x);
            double ref = Math.tanh(x);
            maxErr = FastMath.max(maxErr, FastMath.abs(ref - tst) / FastMath.ulp(ref));
        }
        Assert.assertEquals(0, maxErr, 4);

    }

// org.apache.commons.math3.util.FastMathTest::testMath905LargePositive
    public void testMath905LargePositive() {
        final double start = StrictMath.log(Double.MAX_VALUE);
        final double endT = StrictMath.sqrt(2) * StrictMath.sqrt(Double.MAX_VALUE);
        final double end = 2 * StrictMath.log(endT);

        double maxErr = 0;
        for (double x = start; x < end; x += 1e-3) {
            final double tst = FastMath.cosh(x);
            final double ref = Math.cosh(x);
            maxErr = FastMath.max(maxErr, FastMath.abs(ref - tst) / FastMath.ulp(ref));            
        }
        Assert.assertEquals(0, maxErr, 3);

        for (double x = start; x < end; x += 1e-3) {
            final double tst = FastMath.sinh(x);
            final double ref = Math.sinh(x);
            maxErr = FastMath.max(maxErr, FastMath.abs(ref - tst) / FastMath.ulp(ref));            
        }
        Assert.assertEquals(0, maxErr, 3);
    }

// org.apache.commons.math3.util.FastMathTest::testMath905LargeNegative
    public void testMath905LargeNegative() {
        final double start = -StrictMath.log(Double.MAX_VALUE);
        final double endT = StrictMath.sqrt(2) * StrictMath.sqrt(Double.MAX_VALUE);
        final double end = -2 * StrictMath.log(endT);

        double maxErr = 0;
        for (double x = start; x > end; x -= 1e-3) {
            final double tst = FastMath.cosh(x);
            final double ref = Math.cosh(x);
            maxErr = FastMath.max(maxErr, FastMath.abs(ref - tst) / FastMath.ulp(ref));            
        }
        Assert.assertEquals(0, maxErr, 3);

        for (double x = start; x > end; x -= 1e-3) {
            final double tst = FastMath.sinh(x);
            final double ref = Math.sinh(x);
            maxErr = FastMath.max(maxErr, FastMath.abs(ref - tst) / FastMath.ulp(ref));            
        }
        Assert.assertEquals(0, maxErr, 3);
    }

// org.apache.commons.math3.util.FastMathTest::testHyperbolicInverses
    public void testHyperbolicInverses() {
        double maxErr = 0;
        for (double x = -30; x < 30; x += 0.01) {
            maxErr = FastMath.max(maxErr, FastMath.abs(x - FastMath.sinh(FastMath.asinh(x))) / (2 * FastMath.ulp(x)));
        }
        Assert.assertEquals(0, maxErr, 3);

        maxErr = 0;
        for (double x = 1; x < 30; x += 0.01) {
            maxErr = FastMath.max(maxErr, FastMath.abs(x - FastMath.cosh(FastMath.acosh(x))) / (2 * FastMath.ulp(x)));
        }
        Assert.assertEquals(0, maxErr, 2);

        maxErr = 0;
        for (double x = -1 + Precision.EPSILON; x < 1 - Precision.EPSILON; x += 0.0001) {
            maxErr = FastMath.max(maxErr, FastMath.abs(x - FastMath.tanh(FastMath.atanh(x))) / (2 * FastMath.ulp(x)));
        }
        Assert.assertEquals(0, maxErr, 2);

    }

// org.apache.commons.math3.util.FastMathTest::testLogAccuracy
    public void testLogAccuracy() {
        double maxerrulp = 0.0;

        for (int i = 0; i < NUMBER_OF_TRIALS; i++) {
            double x = Math.exp(generator.nextDouble() * 1416.0 - 708.0) * generator.nextDouble();
            
            double tst = FastMath.log(x);
            double ref = DfpMath.log(field.newDfp(x)).toDouble();
            double err = (tst - ref) / ref;

            if (err != 0.0) {
                double ulp = Math.abs(ref -
                                      Double.longBitsToDouble((Double
                                          .doubleToLongBits(ref) ^ 1)));
                double errulp = field.newDfp(tst).subtract(DfpMath.log(field.newDfp(x))).divide(field.newDfp(ulp)).toDouble();

                maxerrulp = Math.max(maxerrulp, Math.abs(errulp));
            }
        }

        Assert.assertTrue("log() had errors in excess of " + MAX_ERROR_ULP + " ULP", maxerrulp < MAX_ERROR_ULP);
    }

// org.apache.commons.math3.util.FastMathTest::testLog10Accuracy
    public void testLog10Accuracy() {
        double maxerrulp = 0.0;

        for (int i = 0; i < NUMBER_OF_TRIALS; i++) {
            double x = Math.exp(generator.nextDouble() * 1416.0 - 708.0) * generator.nextDouble();
            
            double tst = FastMath.log10(x);
            double ref = DfpMath.log(field.newDfp(x)).divide(DfpMath.log(field.newDfp("10"))).toDouble();
            double err = (tst - ref) / ref;

            if (err != 0.0) {
                double ulp = Math.abs(ref -
                                      Double.longBitsToDouble((Double.doubleToLongBits(ref) ^ 1)));
                double errulp = field.newDfp(tst).subtract(DfpMath.log(field.newDfp(x)).divide(DfpMath.log(field.newDfp("10")))).divide(field.newDfp(ulp)).toDouble();

                maxerrulp = Math.max(maxerrulp, Math.abs(errulp));
            }
        }

        Assert.assertTrue("log10() had errors in excess of " + MAX_ERROR_ULP + " ULP", maxerrulp < MAX_ERROR_ULP);
    }

// org.apache.commons.math3.util.FastMathTest::testLog1pAccuracy
    public void testLog1pAccuracy() {
        double maxerrulp = 0.0;

        for (int i = 0; i < NUMBER_OF_TRIALS; i++) {
            double x = Math.exp(generator.nextDouble() * 10.0 - 5.0) * generator.nextDouble();
            
            double tst = FastMath.log1p(x);
            double ref = DfpMath.log(field.newDfp(x).add(field.getOne())).toDouble();
            double err = (tst - ref) / ref;

            if (err != 0.0) {
                double ulp = Math.abs(ref -
                                      Double.longBitsToDouble((Double.doubleToLongBits(ref) ^ 1)));
                double errulp = field.newDfp(tst).subtract(DfpMath.log(field.newDfp(x).add(field.getOne()))).divide(field.newDfp(ulp)).toDouble();

                maxerrulp = Math.max(maxerrulp, Math.abs(errulp));
            }
        }

        Assert.assertTrue("log1p() had errors in excess of " + MAX_ERROR_ULP + " ULP", maxerrulp < MAX_ERROR_ULP);
    }

// org.apache.commons.math3.util.FastMathTest::testLog1pSpecialCases
    public void testLog1pSpecialCases() {

        Assert.assertTrue("Logp of -1.0 should be -Inf", Double.isInfinite(FastMath.log1p(-1.0)));

    }

// org.apache.commons.math3.util.FastMathTest::testLogSpecialCases
    public void testLogSpecialCases() {

        Assert.assertTrue("Log of zero should be -Inf", Double.isInfinite(FastMath.log(0.0)));

        Assert.assertTrue("Log of -zero should be -Inf", Double.isInfinite(FastMath.log(-0.0)));

        Assert.assertTrue("Log of NaN should be NaN", Double.isNaN(FastMath.log(Double.NaN)));

        Assert.assertTrue("Log of negative number should be NaN", Double.isNaN(FastMath.log(-1.0)));

        Assert.assertEquals("Log of Double.MIN_VALUE should be -744.4400719213812", -744.4400719213812, FastMath.log(Double.MIN_VALUE), Precision.EPSILON);

        Assert.assertTrue("Log of infinity should be infinity", Double.isInfinite(FastMath.log(Double.POSITIVE_INFINITY)));
    }

// org.apache.commons.math3.util.FastMathTest::testExpSpecialCases
    public void testExpSpecialCases() {

        
        Assert.assertEquals(Double.MIN_VALUE, FastMath.exp(-745.1332191019411), Precision.EPSILON);

        Assert.assertEquals("exp(-745.1332191019412) should be 0.0", 0.0, FastMath.exp(-745.1332191019412), Precision.EPSILON);

        Assert.assertTrue("exp of NaN should be NaN", Double.isNaN(FastMath.exp(Double.NaN)));

        Assert.assertTrue("exp of infinity should be infinity", Double.isInfinite(FastMath.exp(Double.POSITIVE_INFINITY)));

        Assert.assertEquals("exp of -infinity should be 0.0", 0.0, FastMath.exp(Double.NEGATIVE_INFINITY), Precision.EPSILON);

        Assert.assertEquals("exp(1) should be Math.E", Math.E, FastMath.exp(1.0), Precision.EPSILON);
    }

// org.apache.commons.math3.util.FastMathTest::testPowSpecialCases
    public void testPowSpecialCases() {

        Assert.assertEquals("pow(-1, 0) should be 1.0", 1.0, FastMath.pow(-1.0, 0.0), Precision.EPSILON);

        Assert.assertEquals("pow(-1, -0) should be 1.0", 1.0, FastMath.pow(-1.0, -0.0), Precision.EPSILON);

        Assert.assertEquals("pow(PI, 1.0) should be PI", FastMath.PI, FastMath.pow(FastMath.PI, 1.0), Precision.EPSILON);

        Assert.assertEquals("pow(-PI, 1.0) should be -PI", -FastMath.PI, FastMath.pow(-FastMath.PI, 1.0), Precision.EPSILON);

        Assert.assertTrue("pow(PI, NaN) should be NaN", Double.isNaN(FastMath.pow(Math.PI, Double.NaN)));

        Assert.assertTrue("pow(NaN, PI) should be NaN", Double.isNaN(FastMath.pow(Double.NaN, Math.PI)));

        Assert.assertTrue("pow(2.0, Infinity) should be Infinity", Double.isInfinite(FastMath.pow(2.0, Double.POSITIVE_INFINITY)));

        Assert.assertTrue("pow(0.5, -Infinity) should be Infinity", Double.isInfinite(FastMath.pow(0.5, Double.NEGATIVE_INFINITY)));

        Assert.assertEquals("pow(0.5, Infinity) should be 0.0", 0.0, FastMath.pow(0.5, Double.POSITIVE_INFINITY), Precision.EPSILON);

        Assert.assertEquals("pow(2.0, -Infinity) should be 0.0", 0.0, FastMath.pow(2.0, Double.NEGATIVE_INFINITY), Precision.EPSILON);

        Assert.assertEquals("pow(0.0, 0.5) should be 0.0", 0.0, FastMath.pow(0.0, 0.5), Precision.EPSILON);

        Assert.assertEquals("pow(Infinity, -0.5) should be 0.0", 0.0, FastMath.pow(Double.POSITIVE_INFINITY, -0.5), Precision.EPSILON);

        Assert.assertTrue("pow(0.0, -0.5) should be Inf", Double.isInfinite(FastMath.pow(0.0, -0.5)));

        Assert.assertTrue("pow(Inf, 0.5) should be Inf", Double.isInfinite(FastMath.pow(Double.POSITIVE_INFINITY, 0.5)));

        Assert.assertTrue("pow(-0.0, -3.0) should be -Inf", Double.isInfinite(FastMath.pow(-0.0, -3.0)));

        Assert.assertTrue("pow(-Inf, -3.0) should be -Inf", Double.isInfinite(FastMath.pow(Double.NEGATIVE_INFINITY, 3.0)));

        Assert.assertTrue("pow(-0.0, -3.5) should be Inf", Double.isInfinite(FastMath.pow(-0.0, -3.5)));

        Assert.assertTrue("pow(Inf, 3.5) should be Inf", Double.isInfinite(FastMath.pow(Double.POSITIVE_INFINITY, 3.5)));

        Assert.assertEquals("pow(-2.0, 3.0) should be -8.0", -8.0, FastMath.pow(-2.0, 3.0), Precision.EPSILON);

        Assert.assertTrue("pow(-2.0, 3.5) should be NaN", Double.isNaN(FastMath.pow(-2.0, 3.5)));

        

        Assert.assertTrue("pow(+Inf, NaN) should be NaN", Double.isNaN(FastMath.pow(Double.POSITIVE_INFINITY, Double.NaN)));

        Assert.assertTrue("pow(1.0, +Inf) should be NaN", Double.isNaN(FastMath.pow(1.0, Double.POSITIVE_INFINITY)));

        Assert.assertTrue("pow(-Inf, NaN) should be NaN", Double.isNaN(FastMath.pow(Double.NEGATIVE_INFINITY, Double.NaN)));

        Assert.assertEquals("pow(-Inf, -1.0) should be 0.0", 0.0, FastMath.pow(Double.NEGATIVE_INFINITY, -1.0), Precision.EPSILON);

        Assert.assertEquals("pow(-Inf, -2.0) should be 0.0", 0.0, FastMath.pow(Double.NEGATIVE_INFINITY, -2.0), Precision.EPSILON);

        Assert.assertTrue("pow(-Inf, 1.0) should be -Inf", Double.isInfinite(FastMath.pow(Double.NEGATIVE_INFINITY, 1.0)));

        Assert.assertTrue("pow(-Inf, 2.0) should be +Inf", Double.isInfinite(FastMath.pow(Double.NEGATIVE_INFINITY, 2.0)));

        Assert.assertTrue("pow(1.0, -Inf) should be NaN", Double.isNaN(FastMath.pow(1.0, Double.NEGATIVE_INFINITY)));

    }

// org.apache.commons.math3.util.FastMathTest::testAtan2SpecialCases
    public void testAtan2SpecialCases() {

        Assert.assertTrue("atan2(NaN, 0.0) should be NaN", Double.isNaN(FastMath.atan2(Double.NaN, 0.0)));

        Assert.assertTrue("atan2(0.0, NaN) should be NaN", Double.isNaN(FastMath.atan2(0.0, Double.NaN)));

        Assert.assertEquals("atan2(0.0, 0.0) should be 0.0", 0.0, FastMath.atan2(0.0, 0.0), Precision.EPSILON);

        Assert.assertEquals("atan2(0.0, 0.001) should be 0.0", 0.0, FastMath.atan2(0.0, 0.001), Precision.EPSILON);

        Assert.assertEquals("atan2(0.1, +Inf) should be 0.0", 0.0, FastMath.atan2(0.1, Double.POSITIVE_INFINITY), Precision.EPSILON);

        Assert.assertEquals("atan2(-0.0, 0.0) should be -0.0", -0.0, FastMath.atan2(-0.0, 0.0), Precision.EPSILON);

        Assert.assertEquals("atan2(-0.0, 0.001) should be -0.0", -0.0, FastMath.atan2(-0.0, 0.001), Precision.EPSILON);

        Assert.assertEquals("atan2(-0.0, +Inf) should be -0.0", -0.0, FastMath.atan2(-0.1, Double.POSITIVE_INFINITY), Precision.EPSILON);

        Assert.assertEquals("atan2(0.0, -0.0) should be PI", FastMath.PI, FastMath.atan2(0.0, -0.0), Precision.EPSILON);

        Assert.assertEquals("atan2(0.1, -Inf) should be PI", FastMath.PI, FastMath.atan2(0.1, Double.NEGATIVE_INFINITY), Precision.EPSILON);

        Assert.assertEquals("atan2(-0.0, -0.0) should be -PI", -FastMath.PI, FastMath.atan2(-0.0, -0.0), Precision.EPSILON);

        Assert.assertEquals("atan2(0.1, -Inf) should be -PI", -FastMath.PI, FastMath.atan2(-0.1, Double.NEGATIVE_INFINITY), Precision.EPSILON);

        Assert.assertEquals("atan2(0.1, 0.0) should be PI/2", FastMath.PI / 2.0, FastMath.atan2(0.1, 0.0), Precision.EPSILON);

        Assert.assertEquals("atan2(0.1, -0.0) should be PI/2", FastMath.PI / 2.0, FastMath.atan2(0.1, -0.0), Precision.EPSILON);

        Assert.assertEquals("atan2(Inf, 0.1) should be PI/2", FastMath.PI / 2.0, FastMath.atan2(Double.POSITIVE_INFINITY, 0.1), Precision.EPSILON);

        Assert.assertEquals("atan2(Inf, -0.1) should be PI/2", FastMath.PI / 2.0, FastMath.atan2(Double.POSITIVE_INFINITY, -0.1), Precision.EPSILON);

        Assert.assertEquals("atan2(-0.1, 0.0) should be -PI/2", -FastMath.PI / 2.0, FastMath.atan2(-0.1, 0.0), Precision.EPSILON);

        Assert.assertEquals("atan2(-0.1, -0.0) should be -PI/2", -FastMath.PI / 2.0, FastMath.atan2(-0.1, -0.0), Precision.EPSILON);

        Assert.assertEquals("atan2(-Inf, 0.1) should be -PI/2", -FastMath.PI / 2.0, FastMath.atan2(Double.NEGATIVE_INFINITY, 0.1), Precision.EPSILON);

        Assert.assertEquals("atan2(-Inf, -0.1) should be -PI/2", -FastMath.PI / 2.0, FastMath.atan2(Double.NEGATIVE_INFINITY, -0.1), Precision.EPSILON);

        Assert.assertEquals("atan2(Inf, Inf) should be PI/4", FastMath.PI / 4.0, FastMath.atan2(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY),
                Precision.EPSILON);

        Assert.assertEquals("atan2(Inf, -Inf) should be PI * 3/4", FastMath.PI * 3.0 / 4.0,
                FastMath.atan2(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY), Precision.EPSILON);

        Assert.assertEquals("atan2(-Inf, Inf) should be -PI/4", -FastMath.PI / 4.0, FastMath.atan2(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),
                Precision.EPSILON);

        Assert.assertEquals("atan2(-Inf, -Inf) should be -PI * 3/4", - FastMath.PI * 3.0 / 4.0,
                FastMath.atan2(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY), Precision.EPSILON);
    }

// org.apache.commons.math3.util.FastMathTest::testPowAccuracy
    public void testPowAccuracy() {
        double maxerrulp = 0.0;

        for (int i = 0; i < NUMBER_OF_TRIALS; i++) {
            double x = (generator.nextDouble() * 2.0 + 0.25);
            double y = (generator.nextDouble() * 1200.0 - 600.0) * generator.nextDouble();
            

            
            double tst = FastMath.pow(x, y);
            double ref = DfpMath.pow(field.newDfp(x), field.newDfp(y)).toDouble();
            double err = (tst - ref) / ref;

            if (err != 0) {
                double ulp = Math.abs(ref -
                                      Double.longBitsToDouble((Double
                                          .doubleToLongBits(ref) ^ 1)));
                double errulp = field.newDfp(tst).subtract(DfpMath.pow(field.newDfp(x), field.newDfp(y))).divide(field.newDfp(ulp)).toDouble();

                maxerrulp = Math.max(maxerrulp, Math.abs(errulp));
            }
        }

        Assert.assertTrue("pow() had errors in excess of " + MAX_ERROR_ULP + " ULP", maxerrulp < MAX_ERROR_ULP);
    }

// org.apache.commons.math3.util.FastMathTest::testExpAccuracy
    public void testExpAccuracy() {
        double maxerrulp = 0.0;

        for (int i = 0; i < NUMBER_OF_TRIALS; i++) {
            
            double x = ((generator.nextDouble() * 1416.0) - 708.0) * generator.nextDouble();
            
            
            
            double tst = FastMath.exp(x);
            double ref = DfpMath.exp(field.newDfp(x)).toDouble();
            double err = (tst - ref) / ref;

            if (err != 0) {
                double ulp = Math.abs(ref -
                                      Double.longBitsToDouble((Double.doubleToLongBits(ref) ^ 1)));
                double errulp = field.newDfp(tst).subtract(DfpMath.exp(field.newDfp(x))).divide(field.newDfp(ulp)).toDouble();

                maxerrulp = Math.max(maxerrulp, Math.abs(errulp));
            }
        }

        Assert.assertTrue("exp() had errors in excess of " + MAX_ERROR_ULP + " ULP", maxerrulp < MAX_ERROR_ULP);
    }

// org.apache.commons.math3.util.FastMathTest::testSinAccuracy
    public void testSinAccuracy() {
        double maxerrulp = 0.0;

        for (int i = 0; i < NUMBER_OF_TRIALS; i++) {
            
            
            double x = ((generator.nextDouble() * Math.PI) - Math.PI / 2.0) *
                       Math.pow(2, 21) * generator.nextDouble();
            
            
            
            double tst = FastMath.sin(x);
            double ref = DfpMath.sin(field.newDfp(x)).toDouble();
            double err = (tst - ref) / ref;

            if (err != 0) {
                double ulp = Math.abs(ref -
                                      Double.longBitsToDouble((Double.doubleToLongBits(ref) ^ 1)));
                double errulp = field.newDfp(tst).subtract(DfpMath.sin(field.newDfp(x))).divide(field.newDfp(ulp)).toDouble();

                maxerrulp = Math.max(maxerrulp, Math.abs(errulp));
            }
        }

        Assert.assertTrue("sin() had errors in excess of " + MAX_ERROR_ULP + " ULP", maxerrulp < MAX_ERROR_ULP);
    }

// org.apache.commons.math3.util.FastMathTest::testCosAccuracy
    public void testCosAccuracy() {
        double maxerrulp = 0.0;

        for (int i = 0; i < NUMBER_OF_TRIALS; i++) {
            
            
            double x = ((generator.nextDouble() * Math.PI) - Math.PI / 2.0) *
                       Math.pow(2, 21) * generator.nextDouble();
            
            
            
            double tst = FastMath.cos(x);
            double ref = DfpMath.cos(field.newDfp(x)).toDouble();
            double err = (tst - ref) / ref;

            if (err != 0) {
                double ulp = Math.abs(ref -
                                      Double.longBitsToDouble((Double.doubleToLongBits(ref) ^ 1)));
                double errulp = field.newDfp(tst).subtract(DfpMath.cos(field.newDfp(x))).divide(field.newDfp(ulp)).toDouble();

                maxerrulp = Math.max(maxerrulp, Math.abs(errulp));
            }
        }

        Assert.assertTrue("cos() had errors in excess of " + MAX_ERROR_ULP + " ULP", maxerrulp < MAX_ERROR_ULP);
    }

// org.apache.commons.math3.util.FastMathTest::testTanAccuracy
    public void testTanAccuracy() {
        double maxerrulp = 0.0;

        for (int i = 0; i < NUMBER_OF_TRIALS; i++) {
            
            
            double x = ((generator.nextDouble() * Math.PI) - Math.PI / 2.0) *
                       Math.pow(2, 12) * generator.nextDouble();
            
            
            
            double tst = FastMath.tan(x);
            double ref = DfpMath.tan(field.newDfp(x)).toDouble();
            double err = (tst - ref) / ref;

            if (err != 0) {
                double ulp = Math.abs(ref -
                                      Double.longBitsToDouble((Double.doubleToLongBits(ref) ^ 1)));
                double errulp = field.newDfp(tst).subtract(DfpMath.tan(field.newDfp(x))).divide(field.newDfp(ulp)).toDouble();

                maxerrulp = Math.max(maxerrulp, Math.abs(errulp));
            }
        }

        Assert.assertTrue("tan() had errors in excess of " + MAX_ERROR_ULP + " ULP", maxerrulp < MAX_ERROR_ULP);
    }

// org.apache.commons.math3.util.FastMathTest::testAtanAccuracy
    public void testAtanAccuracy() {
        double maxerrulp = 0.0;

        for (int i = 0; i < NUMBER_OF_TRIALS; i++) {
            
            
            
            
            double x = ((generator.nextDouble() * 16.0) - 8.0) * generator.nextDouble();

            
            
            
            double tst = FastMath.atan(x);
            double ref = DfpMath.atan(field.newDfp(x)).toDouble();
            double err = (tst - ref) / ref;

            if (err != 0) {
                double ulp = Math.abs(ref -
                                      Double.longBitsToDouble((Double.doubleToLongBits(ref) ^ 1)));
                double errulp = field.newDfp(tst).subtract(DfpMath.atan(field.newDfp(x))).divide(field.newDfp(ulp)).toDouble();

                maxerrulp = Math.max(maxerrulp, Math.abs(errulp));
            }
        }

        Assert.assertTrue("atan() had errors in excess of " + MAX_ERROR_ULP + " ULP", maxerrulp < MAX_ERROR_ULP);
    }

// org.apache.commons.math3.util.FastMathTest::testAtan2Accuracy
    public void testAtan2Accuracy() {
        double maxerrulp = 0.0;

        for (int i = 0; i < NUMBER_OF_TRIALS; i++) {
            
            
            double x = generator.nextDouble() - 0.5;
            double y = generator.nextDouble() - 0.5;
            
            
            
            double tst = FastMath.atan2(y, x);
            Dfp refdfp = DfpMath.atan(field.newDfp(y)
                .divide(field.newDfp(x)));
            
            if (x < 0.0) {
                if (y > 0.0)
                    refdfp = field.getPi().add(refdfp);
                else
                    refdfp = refdfp.subtract(field.getPi());
            }

            double ref = refdfp.toDouble();
            double err = (tst - ref) / ref;

            if (err != 0) {
                double ulp = Math.abs(ref -
                                      Double.longBitsToDouble((Double
                                          .doubleToLongBits(ref) ^ 1)));
                double errulp = field.newDfp(tst).subtract(refdfp).divide(field.newDfp(ulp)).toDouble();

                maxerrulp = Math.max(maxerrulp, Math.abs(errulp));
            }
        }

        Assert.assertTrue("atan2() had errors in excess of " + MAX_ERROR_ULP + " ULP", maxerrulp < MAX_ERROR_ULP);
    }

// org.apache.commons.math3.util.FastMathTest::testExpm1Accuracy
    public void testExpm1Accuracy() {
        double maxerrulp = 0.0;

        for (int i = 0; i < NUMBER_OF_TRIALS; i++) {
            
            
            double x = ((generator.nextDouble() * 16.0) - 8.0) * generator.nextDouble();
            
            double tst = FastMath.expm1(x);
            double ref = DfpMath.exp(field.newDfp(x)).subtract(field.getOne()).toDouble();
            double err = (tst - ref) / ref;

            if (err != 0) {
                double ulp = Math.abs(ref -
                                      Double.longBitsToDouble((Double
                                          .doubleToLongBits(ref) ^ 1)));
                double errulp = field.newDfp(tst).subtract(DfpMath.exp(field.newDfp(x)).subtract(field.getOne())).divide(field.newDfp(ulp)).toDouble();

                maxerrulp = Math.max(maxerrulp, Math.abs(errulp));
            }
        }

        Assert.assertTrue("expm1() had errors in excess of " + MAX_ERROR_ULP + " ULP", maxerrulp < MAX_ERROR_ULP);
    }

// org.apache.commons.math3.util.FastMathTest::testAsinAccuracy
    public void testAsinAccuracy() {
        double maxerrulp = 0.0;

        for (int i=0; i<10000; i++) {
            double x = ((generator.nextDouble() * 2.0) - 1.0) * generator.nextDouble();

            double tst = FastMath.asin(x);
            double ref = DfpMath.asin(field.newDfp(x)).toDouble();
            double err = (tst - ref) / ref;

            if (err != 0) {
                double ulp = Math.abs(ref - Double.longBitsToDouble((Double.doubleToLongBits(ref) ^ 1)));
                double errulp = field.newDfp(tst).subtract(DfpMath.asin(field.newDfp(x))).divide(field.newDfp(ulp)).toDouble();
                

                maxerrulp = Math.max(maxerrulp, Math.abs(errulp));
            }
        }

        Assert.assertTrue("asin() had errors in excess of " + MAX_ERROR_ULP + " ULP", maxerrulp < MAX_ERROR_ULP);
    }

// org.apache.commons.math3.util.FastMathTest::testAcosAccuracy
    public void testAcosAccuracy() {
        double maxerrulp = 0.0;

        for (int i=0; i<10000; i++) {
            double x = ((generator.nextDouble() * 2.0) - 1.0) * generator.nextDouble();

            double tst = FastMath.acos(x);
            double ref = DfpMath.acos(field.newDfp(x)).toDouble();
            double err = (tst - ref) / ref;

            if (err != 0) {
                double ulp = Math.abs(ref - Double.longBitsToDouble((Double.doubleToLongBits(ref) ^ 1)));
                double errulp = field.newDfp(tst).subtract(DfpMath.acos(field.newDfp(x))).divide(field.newDfp(ulp)).toDouble();
                

                maxerrulp = Math.max(maxerrulp, Math.abs(errulp));
            }
        }

        Assert.assertTrue("acos() had errors in excess of " + MAX_ERROR_ULP + " ULP", maxerrulp < MAX_ERROR_ULP);
    }

// org.apache.commons.math3.util.FastMathTest::testAcosSpecialCases
    public void testAcosSpecialCases() {
        
        Assert.assertTrue("acos(NaN) should be NaN", Double.isNaN(FastMath.acos(Double.NaN)));
        
        Assert.assertTrue("acos(-1.1) should be NaN", Double.isNaN(FastMath.acos(-1.1)));

        Assert.assertTrue("acos(-1.1) should be NaN", Double.isNaN(FastMath.acos(1.1)));
        
        Assert.assertEquals("acos(-1.0) should be PI", FastMath.acos(-1.0), FastMath.PI, Precision.EPSILON);

        Assert.assertEquals("acos(1.0) should be 0.0", FastMath.acos(1.0), 0.0, Precision.EPSILON);

        Assert.assertEquals("acos(0.0) should be PI/2", FastMath.acos(0.0), FastMath.PI / 2.0, Precision.EPSILON);
    }

// org.apache.commons.math3.util.FastMathTest::testAsinSpecialCases
    public void testAsinSpecialCases() {
   
        Assert.assertTrue("asin(NaN) should be NaN", Double.isNaN(FastMath.asin(Double.NaN)));
        
        Assert.assertTrue("asin(1.1) should be NaN", Double.isNaN(FastMath.asin(1.1)));
        
        Assert.assertTrue("asin(-1.1) should be NaN", Double.isNaN(FastMath.asin(-1.1)));
        
        Assert.assertEquals("asin(1.0) should be PI/2", FastMath.asin(1.0), FastMath.PI / 2.0, Precision.EPSILON);

        Assert.assertEquals("asin(-1.0) should be -PI/2", FastMath.asin(-1.0), -FastMath.PI / 2.0, Precision.EPSILON);

        Assert.assertEquals("asin(0.0) should be 0.0", FastMath.asin(0.0), 0.0, Precision.EPSILON);
    }

// org.apache.commons.math3.util.FastMathTest::testSinhAccuracy
    public void testSinhAccuracy() {
        double maxerrulp = 0.0;

        for (int i=0; i<10000; i++) {
            double x = ((generator.nextDouble() * 16.0) - 8.0) * generator.nextDouble();

            double tst = FastMath.sinh(x);
            double ref = sinh(field.newDfp(x)).toDouble();
            double err = (tst - ref) / ref;

            if (err != 0) {
                double ulp = Math.abs(ref - Double.longBitsToDouble((Double.doubleToLongBits(ref) ^ 1)));
                double errulp = field.newDfp(tst).subtract(sinh(field.newDfp(x))).divide(field.newDfp(ulp)).toDouble();
                
                maxerrulp = Math.max(maxerrulp, Math.abs(errulp));
            }
        }

        Assert.assertTrue("sinh() had errors in excess of " + MAX_ERROR_ULP + " ULP", maxerrulp < MAX_ERROR_ULP);
    }

// org.apache.commons.math3.util.FastMathTest::testCoshAccuracy
    public void testCoshAccuracy() {
        double maxerrulp = 0.0;

        for (int i=0; i<10000; i++) {
            double x = ((generator.nextDouble() * 16.0) - 8.0) * generator.nextDouble();

            double tst = FastMath.cosh(x);
            double ref = cosh(field.newDfp(x)).toDouble();
            double err = (tst - ref) / ref;

            if (err != 0) {
                double ulp = Math.abs(ref - Double.longBitsToDouble((Double.doubleToLongBits(ref) ^ 1)));
                double errulp = field.newDfp(tst).subtract(cosh(field.newDfp(x))).divide(field.newDfp(ulp)).toDouble();
                
                maxerrulp = Math.max(maxerrulp, Math.abs(errulp));
            }
        }

        Assert.assertTrue("cosh() had errors in excess of " + MAX_ERROR_ULP + " ULP", maxerrulp < MAX_ERROR_ULP);
    }

// org.apache.commons.math3.util.FastMathTest::testTanhAccuracy
    public void testTanhAccuracy() {
        double maxerrulp = 0.0;

        for (int i=0; i<10000; i++) {
            double x = ((generator.nextDouble() * 16.0) - 8.0) * generator.nextDouble();

            double tst = FastMath.tanh(x);
            double ref = tanh(field.newDfp(x)).toDouble();
            double err = (tst - ref) / ref;

            if (err != 0) {
                double ulp = Math.abs(ref - Double.longBitsToDouble((Double.doubleToLongBits(ref) ^ 1)));
                double errulp = field.newDfp(tst).subtract(tanh(field.newDfp(x))).divide(field.newDfp(ulp)).toDouble();
                
                maxerrulp = Math.max(maxerrulp, Math.abs(errulp));
            }
        }

        Assert.assertTrue("tanh() had errors in excess of " + MAX_ERROR_ULP + " ULP", maxerrulp < MAX_ERROR_ULP);
    }

// org.apache.commons.math3.util.FastMathTest::testCbrtAccuracy
    public void testCbrtAccuracy() {
        double maxerrulp = 0.0;

        for (int i=0; i<10000; i++) {
            double x = ((generator.nextDouble() * 200.0) - 100.0) * generator.nextDouble();

            double tst = FastMath.cbrt(x);
            double ref = cbrt(field.newDfp(x)).toDouble();
            double err = (tst - ref) / ref;

            if (err != 0) {
                double ulp = Math.abs(ref - Double.longBitsToDouble((Double.doubleToLongBits(ref) ^ 1)));
                double errulp = field.newDfp(tst).subtract(cbrt(field.newDfp(x))).divide(field.newDfp(ulp)).toDouble();
                
                maxerrulp = Math.max(maxerrulp, Math.abs(errulp));
            }
        }

        Assert.assertTrue("cbrt() had errors in excess of " + MAX_ERROR_ULP + " ULP", maxerrulp < MAX_ERROR_ULP);
    }

// org.apache.commons.math3.util.FastMathTest::testToDegrees
    public void testToDegrees() {
        double maxerrulp = 0.0;
        for (int i = 0; i < NUMBER_OF_TRIALS; i++) {
            double x = generator.nextDouble();
            double tst = field.newDfp(x).multiply(180).divide(field.getPi()).toDouble();
            double ref = FastMath.toDegrees(x);
            double err = (tst - ref) / ref;

            if (err != 0) {
                double ulp = Math.abs(ref -
                                      Double.longBitsToDouble((Double.doubleToLongBits(ref) ^ 1)));
                double errulp = field.newDfp(tst).subtract(DfpMath.exp(field.newDfp(x)).subtract(field.getOne())).divide(field.newDfp(ulp)).toDouble();

                maxerrulp = Math.max(maxerrulp, Math.abs(errulp));
            }
        }
        Assert.assertTrue("toDegrees() had errors in excess of " + MAX_ERROR_ULP + " ULP", maxerrulp < MAX_ERROR_ULP);

    }

// org.apache.commons.math3.util.FastMathTest::testToRadians
    public void testToRadians() {
        double maxerrulp = 0.0;
        for (int i = 0; i < NUMBER_OF_TRIALS; i++) {
            double x = generator.nextDouble();
            double tst = field.newDfp(x).multiply(field.getPi()).divide(180).toDouble();
            double ref = FastMath.toRadians(x);
            double err = (tst - ref) / ref;

            if (err != 0) {
                double ulp = Math.abs(ref -
                                      Double.longBitsToDouble((Double
                                          .doubleToLongBits(ref) ^ 1)));
                double errulp = field.newDfp(tst).subtract(DfpMath.exp(field.newDfp(x)).subtract(field.getOne())).divide(field.newDfp(ulp)).toDouble();

                maxerrulp = Math.max(maxerrulp, Math.abs(errulp));
            }
        }
        Assert.assertTrue("toRadians() had errors in excess of " + MAX_ERROR_ULP + " ULP", maxerrulp < MAX_ERROR_ULP);

    }

// org.apache.commons.math3.util.FastMathTest::testNextAfter
    public void testNextAfter() {
        
        Assert.assertEquals(16.0, FastMath.nextAfter(15.999999999999998, 34.27555555555555), 0.0);

        
        Assert.assertEquals(-15.999999999999996, FastMath.nextAfter(-15.999999999999998, 34.27555555555555), 0.0);

        
        Assert.assertEquals(15.999999999999996, FastMath.nextAfter(15.999999999999998, 2.142222222222222), 0.0);

        
        Assert.assertEquals(-15.999999999999996, FastMath.nextAfter(-15.999999999999998, 2.142222222222222), 0.0);

        
        Assert.assertEquals(8.000000000000002, FastMath.nextAfter(8.0, 34.27555555555555), 0.0);

        
        Assert.assertEquals(-7.999999999999999, FastMath.nextAfter(-8.0, 34.27555555555555), 0.0);

        
        Assert.assertEquals(7.999999999999999, FastMath.nextAfter(8.0, 2.142222222222222), 0.0);

        
        Assert.assertEquals(-7.999999999999999, FastMath.nextAfter(-8.0, 2.142222222222222), 0.0);

        
        Assert.assertEquals(2.308922399667661E-4, FastMath.nextAfter(2.3089223996676606E-4, 2.308922399667661E-4), 0.0);

        
        Assert.assertEquals(2.3089223996676606E-4, FastMath.nextAfter(2.3089223996676606E-4, 2.3089223996676606E-4), 0.0);

        
        Assert.assertEquals(2.3089223996676603E-4, FastMath.nextAfter(2.3089223996676606E-4, 2.3089223996676603E-4), 0.0);

        
        Assert.assertEquals(2.3089223996676603E-4, FastMath.nextAfter(2.3089223996676606E-4, -2.308922399667661E-4), 0.0);

        
        Assert.assertEquals(2.3089223996676603E-4, FastMath.nextAfter(2.3089223996676606E-4, -2.3089223996676606E-4), 0.0);

        
        Assert.assertEquals(2.3089223996676603E-4, FastMath.nextAfter(2.3089223996676606E-4, -2.3089223996676603E-4), 0.0);

        
        Assert.assertEquals(-2.3089223996676603E-4, FastMath.nextAfter(-2.3089223996676606E-4, 2.308922399667661E-4), 0.0);

        
        Assert.assertEquals(-2.3089223996676603E-4, FastMath.nextAfter(-2.3089223996676606E-4, 2.3089223996676606E-4), 0.0);

        
        Assert.assertEquals(-2.3089223996676603E-4, FastMath.nextAfter(-2.3089223996676606E-4, 2.3089223996676603E-4), 0.0);

        
        Assert.assertEquals(-2.308922399667661E-4, FastMath.nextAfter(-2.3089223996676606E-4, -2.308922399667661E-4), 0.0);

        
        Assert.assertEquals(-2.3089223996676606E-4, FastMath.nextAfter(-2.3089223996676606E-4, -2.3089223996676606E-4), 0.0);

        
        Assert.assertEquals(-2.3089223996676603E-4, FastMath.nextAfter(-2.3089223996676606E-4, -2.3089223996676603E-4), 0.0);

    }

// org.apache.commons.math3.util.FastMathTest::testDoubleNextAfterSpecialCases
    public void testDoubleNextAfterSpecialCases() {
        Assert.assertEquals(-Double.MAX_VALUE,FastMath.nextAfter(Double.NEGATIVE_INFINITY, 0D), 0D);
        Assert.assertEquals(Double.MAX_VALUE,FastMath.nextAfter(Double.POSITIVE_INFINITY, 0D), 0D);
        Assert.assertEquals(Double.NaN,FastMath.nextAfter(Double.NaN, 0D), 0D);
        Assert.assertEquals(Double.POSITIVE_INFINITY,FastMath.nextAfter(Double.MAX_VALUE, Double.POSITIVE_INFINITY), 0D);
        Assert.assertEquals(Double.NEGATIVE_INFINITY,FastMath.nextAfter(-Double.MAX_VALUE, Double.NEGATIVE_INFINITY), 0D);
        Assert.assertEquals(Double.MIN_VALUE, FastMath.nextAfter(0D, 1D), 0D);
        Assert.assertEquals(-Double.MIN_VALUE, FastMath.nextAfter(0D, -1D), 0D);
        Assert.assertEquals(0D, FastMath.nextAfter(Double.MIN_VALUE, -1), 0D);
        Assert.assertEquals(0D, FastMath.nextAfter(-Double.MIN_VALUE, 1), 0D);
    }

// org.apache.commons.math3.util.FastMathTest::testFloatNextAfterSpecialCases
    public void testFloatNextAfterSpecialCases() {
        Assert.assertEquals(-Float.MAX_VALUE,FastMath.nextAfter(Float.NEGATIVE_INFINITY, 0F), 0F);
        Assert.assertEquals(Float.MAX_VALUE,FastMath.nextAfter(Float.POSITIVE_INFINITY, 0F), 0F);
        Assert.assertEquals(Float.NaN,FastMath.nextAfter(Float.NaN, 0F), 0F);
        Assert.assertEquals(Float.POSITIVE_INFINITY,FastMath.nextAfter(Float.MAX_VALUE, Float.POSITIVE_INFINITY), 0F);
        Assert.assertEquals(Float.NEGATIVE_INFINITY,FastMath.nextAfter(-Float.MAX_VALUE, Float.NEGATIVE_INFINITY), 0F);
        Assert.assertEquals(Float.MIN_VALUE, FastMath.nextAfter(0F, 1F), 0F);
        Assert.assertEquals(-Float.MIN_VALUE, FastMath.nextAfter(0F, -1F), 0F);
        Assert.assertEquals(0F, FastMath.nextAfter(Float.MIN_VALUE, -1F), 0F);
        Assert.assertEquals(0F, FastMath.nextAfter(-Float.MIN_VALUE, 1F), 0F);
    }

// org.apache.commons.math3.util.FastMathTest::testDoubleScalbSpecialCases
    public void testDoubleScalbSpecialCases() {
        Assert.assertEquals(2.5269841324701218E-175,  FastMath.scalb(2.2250738585072014E-308, 442), 0D);
        Assert.assertEquals(1.307993905256674E297,    FastMath.scalb(1.1102230246251565E-16, 1040), 0D);
        Assert.assertEquals(7.2520887996488946E-217,  FastMath.scalb(Double.MIN_VALUE,        356), 0D);
        Assert.assertEquals(8.98846567431158E307,     FastMath.scalb(Double.MIN_VALUE,       2097), 0D);
        Assert.assertEquals(Double.POSITIVE_INFINITY, FastMath.scalb(Double.MIN_VALUE,       2098), 0D);
        Assert.assertEquals(1.1125369292536007E-308,  FastMath.scalb(2.225073858507201E-308,   -1), 0D);
        Assert.assertEquals(1.0E-323,                 FastMath.scalb(Double.MAX_VALUE,      -2097), 0D);
        Assert.assertEquals(Double.MIN_VALUE,         FastMath.scalb(Double.MAX_VALUE,      -2098), 0D);
        Assert.assertEquals(0,                        FastMath.scalb(Double.MAX_VALUE,      -2099), 0D);
        Assert.assertEquals(Double.POSITIVE_INFINITY, FastMath.scalb(Double.POSITIVE_INFINITY, -1000000), 0D);
        Assert.assertEquals(Double.NEGATIVE_INFINITY, FastMath.scalb(-1.1102230246251565E-16, 1078), 0D);
        Assert.assertEquals(Double.NEGATIVE_INFINITY, FastMath.scalb(-1.1102230246251565E-16,  1079), 0D);
        Assert.assertEquals(Double.NEGATIVE_INFINITY, FastMath.scalb(-2.2250738585072014E-308, 2047), 0D);
        Assert.assertEquals(Double.NEGATIVE_INFINITY, FastMath.scalb(-2.2250738585072014E-308, 2048), 0D);
        Assert.assertEquals(Double.NEGATIVE_INFINITY, FastMath.scalb(-1.7976931348623157E308,  2147483647), 0D);
        Assert.assertEquals(Double.POSITIVE_INFINITY, FastMath.scalb( 1.7976931348623157E308,  2147483647), 0D);
        Assert.assertEquals(Double.NEGATIVE_INFINITY, FastMath.scalb(-1.1102230246251565E-16,  2147483647), 0D);
        Assert.assertEquals(Double.POSITIVE_INFINITY, FastMath.scalb( 1.1102230246251565E-16,  2147483647), 0D);
        Assert.assertEquals(Double.NEGATIVE_INFINITY, FastMath.scalb(-2.2250738585072014E-308, 2147483647), 0D);
        Assert.assertEquals(Double.POSITIVE_INFINITY, FastMath.scalb( 2.2250738585072014E-308, 2147483647), 0D);
    }

// org.apache.commons.math3.util.FastMathTest::testFloatScalbSpecialCases
    public void testFloatScalbSpecialCases() {
        Assert.assertEquals(0f,                       FastMath.scalb(Float.MIN_VALUE,  -30), 0F);
        Assert.assertEquals(2 * Float.MIN_VALUE,      FastMath.scalb(Float.MIN_VALUE,    1), 0F);
        Assert.assertEquals(7.555786e22f,             FastMath.scalb(Float.MAX_VALUE,  -52), 0F);
        Assert.assertEquals(1.7014118e38f,            FastMath.scalb(Float.MIN_VALUE,  276), 0F);
        Assert.assertEquals(Float.POSITIVE_INFINITY,  FastMath.scalb(Float.MIN_VALUE,  277), 0F);
        Assert.assertEquals(5.8774718e-39f,           FastMath.scalb(1.1754944e-38f,    -1), 0F);
        Assert.assertEquals(2 * Float.MIN_VALUE,      FastMath.scalb(Float.MAX_VALUE, -276), 0F);
        Assert.assertEquals(Float.MIN_VALUE,          FastMath.scalb(Float.MAX_VALUE, -277), 0F);
        Assert.assertEquals(0,                        FastMath.scalb(Float.MAX_VALUE, -278), 0F);
        Assert.assertEquals(Float.POSITIVE_INFINITY,  FastMath.scalb(Float.POSITIVE_INFINITY, -1000000), 0F);
        Assert.assertEquals(-3.13994498e38f,          FastMath.scalb(-1.1e-7f,         151), 0F);
        Assert.assertEquals(Float.NEGATIVE_INFINITY,  FastMath.scalb(-1.1e-7f,         152), 0F);
        Assert.assertEquals(Float.POSITIVE_INFINITY,  FastMath.scalb(3.4028235E38f,  2147483647), 0F);
        Assert.assertEquals(Float.NEGATIVE_INFINITY,  FastMath.scalb(-3.4028235E38f, 2147483647), 0F);
    }

// org.apache.commons.math3.util.FastMathTest::checkMissingFastMathClasses
    public void checkMissingFastMathClasses() {}

// org.apache.commons.math3.util.FastMathTest::checkExtraFastMathClasses
    public void checkExtraFastMathClasses() {
        compareClassMethods( FastMath.class, StrictMath.class);
    }

// org.apache.commons.math3.util.FastMathTest::testSignumDouble
    public void testSignumDouble() {
        final double delta = 0.0;
        Assert.assertEquals(1.0, FastMath.signum(2.0), delta);
        Assert.assertEquals(0.0, FastMath.signum(0.0), delta);
        Assert.assertEquals(-1.0, FastMath.signum(-2.0), delta);
        TestUtils.assertSame(-0. / 0., FastMath.signum(Double.NaN));
    }

// org.apache.commons.math3.util.FastMathTest::testSignumFloat
    public void testSignumFloat() {
        final float delta = 0.0F;
        Assert.assertEquals(1.0F, FastMath.signum(2.0F), delta);
        Assert.assertEquals(0.0F, FastMath.signum(0.0F), delta);
        Assert.assertEquals(-1.0F, FastMath.signum(-2.0F), delta);
        TestUtils.assertSame(Float.NaN, FastMath.signum(Float.NaN));
    }

// org.apache.commons.math3.util.FastMathTest::testLogWithBase
    public void testLogWithBase() {
        Assert.assertEquals(2.0, FastMath.log(2, 4), 0);
        Assert.assertEquals(3.0, FastMath.log(2, 8), 0);
        Assert.assertTrue(Double.isNaN(FastMath.log(-1, 1)));
        Assert.assertTrue(Double.isNaN(FastMath.log(1, -1)));
        Assert.assertTrue(Double.isNaN(FastMath.log(0, 0)));
        Assert.assertEquals(0, FastMath.log(0, 10), 0);
        Assert.assertEquals(Double.NEGATIVE_INFINITY, FastMath.log(10, 0), 0);
    }

// org.apache.commons.math3.util.FastMathTest::testIndicatorDouble
    public void testIndicatorDouble() {
        double delta = 0.0;
        Assert.assertEquals(1.0, FastMath.copySign(1d, 2.0), delta);
        Assert.assertEquals(1.0, FastMath.copySign(1d, 0.0), delta);
        Assert.assertEquals(-1.0, FastMath.copySign(1d, -2.0), delta);
    }

// org.apache.commons.math3.util.FastMathTest::testIndicatorFloat
    public void testIndicatorFloat() {
        float delta = 0.0F;
        Assert.assertEquals(1.0F, FastMath.copySign(1d, 2.0F), delta);
        Assert.assertEquals(1.0F, FastMath.copySign(1d, 0.0F), delta);
        Assert.assertEquals(-1.0F, FastMath.copySign(1d, -2.0F), delta);
    }

// org.apache.commons.math3.util.FastMathTest::testIntPow
    public void testIntPow() {
        final int maxExp = 300;
        DfpField field = new DfpField(40);
        final double base = 1.23456789;
        Dfp baseDfp = field.newDfp(base);
        Dfp dfpPower = field.getOne();
        for (int i = 0; i < maxExp; i++) {
            Assert.assertEquals("exp=" + i, dfpPower.toDouble(), FastMath.pow(base, i),
                                0.6 * FastMath.ulp(dfpPower.toDouble()));
            dfpPower = dfpPower.multiply(baseDfp);
        }
    }

// org.apache.commons.math3.util.MathArraysTest::testL1DistanceDouble
    public void testL1DistanceDouble() {
        double[] p1 = { 2.5,  0.0 };
        double[] p2 = { -0.5, 4.0 };
        Assert.assertTrue(Precision.equals(7.0, MathArrays.distance1(p1, p2), 1));
    }

// org.apache.commons.math3.util.MathArraysTest::testL1DistanceInt
    public void testL1DistanceInt() {
        int[] p1 = { 3, 0 };
        int[] p2 = { 0, 4 };
        Assert.assertEquals(7, MathArrays.distance1(p1, p2));
    }

// org.apache.commons.math3.util.MathArraysTest::testL2DistanceDouble
    public void testL2DistanceDouble() {
        double[] p1 = { 2.5,  0.0 };
        double[] p2 = { -0.5, 4.0 };
        Assert.assertTrue(Precision.equals(5.0, MathArrays.distance(p1, p2), 1));
    }

// org.apache.commons.math3.util.MathArraysTest::testL2DistanceInt
    public void testL2DistanceInt() {
        int[] p1 = { 3, 0 };
        int[] p2 = { 0, 4 };
        Assert.assertTrue(Precision.equals(5, MathArrays.distance(p1, p2), 1));
    }

// org.apache.commons.math3.util.MathArraysTest::testLInfDistanceDouble
    public void testLInfDistanceDouble() {
        double[] p1 = { 2.5,  0.0 };
        double[] p2 = { -0.5, 4.0 };
        Assert.assertTrue(Precision.equals(4.0, MathArrays.distanceInf(p1, p2), 1));
    }

// org.apache.commons.math3.util.MathArraysTest::testLInfDistanceInt
    public void testLInfDistanceInt() {
        int[] p1 = { 3, 0 };
        int[] p2 = { 0, 4 };
        Assert.assertEquals(4, MathArrays.distanceInf(p1, p2));
    }
