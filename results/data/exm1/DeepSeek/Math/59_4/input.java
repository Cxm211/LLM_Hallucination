// buggy code
    public static float max(final float a, final float b) {
        return (a <= b) ? b : (Float.isNaN(a + b) ? Float.NaN : b);
    }

// relevant test
// org.apache.commons.math.stat.inference.MannWhitneyUTestTest::testMannWhitneyUSimple
    public void testMannWhitneyUSimple() throws Exception {
        
        final double x[] = {19, 22, 16, 29, 24};
        final double y[] = {20, 11, 17, 12};
        
        assertEquals(17, testStatistic.mannWhitneyU(x, y), 1e-10);
        assertEquals(0.08641, testStatistic.mannWhitneyUTest(x, y), 1e-5);
    }

// org.apache.commons.math.stat.inference.MannWhitneyUTestTest::testMannWhitneyUInputValidation
    public void testMannWhitneyUInputValidation() throws Exception {
        
        try {
            testStatistic.mannWhitneyUTest(new double[] { }, new double[] { 1.0 });
            fail("x does not contain samples (exact), IllegalArgumentException expected");
        } catch (IllegalArgumentException ex) {
            
        }

        try {
            testStatistic.mannWhitneyUTest(new double[] { 1.0 }, new double[] { });
            fail("y does not contain samples (exact), IllegalArgumentException expected");
        } catch (IllegalArgumentException ex) {
            
        }

        
        try {
            testStatistic.mannWhitneyUTest(null, null);
            fail("x and y is null (exact), IllegalArgumentException expected");
        } catch (IllegalArgumentException ex) {
            
        }
        
        try {
            testStatistic.mannWhitneyUTest(null, null);
            fail("x and y is null (asymptotic), IllegalArgumentException expected");
        } catch (IllegalArgumentException ex) {
            
        }
        
        
        try {
            testStatistic.mannWhitneyUTest(null, new double[] { 1.0 });
            fail("x is null (exact), IllegalArgumentException expected");
        } catch (IllegalArgumentException ex) {
            
        }
        
        try {
            testStatistic.mannWhitneyUTest(new double[] { 1.0 }, null);
            fail("y is null (exact), IllegalArgumentException expected");
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.stat.inference.OneWayAnovaTest::testAnovaFValue
    public void testAnovaFValue() throws Exception {
        
        List<double[]> threeClasses = new ArrayList<double[]>();
        threeClasses.add(classA);
        threeClasses.add(classB);
        threeClasses.add(classC);

        assertEquals("ANOVA F-value",  24.67361709460624,
                 testStatistic.anovaFValue(threeClasses), 1E-12);

        List<double[]> twoClasses = new ArrayList<double[]>();
        twoClasses.add(classA);
        twoClasses.add(classB);

        assertEquals("ANOVA F-value",  0.0150579150579,
                 testStatistic.anovaFValue(twoClasses), 1E-12);

        List<double[]> emptyContents = new ArrayList<double[]>();
        emptyContents.add(emptyArray);
        emptyContents.add(classC);
        try {
            testStatistic.anovaFValue(emptyContents);
            fail("empty array for key classX, IllegalArgumentException expected");
        } catch (IllegalArgumentException ex) {
            
        }

        List<double[]> tooFew = new ArrayList<double[]>();
        tooFew.add(classA);
        try {
            testStatistic.anovaFValue(tooFew);
            fail("less than two classes, IllegalArgumentException expected");
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.stat.inference.OneWayAnovaTest::testAnovaPValue
    public void testAnovaPValue() throws Exception {
        
        List<double[]> threeClasses = new ArrayList<double[]>();
        threeClasses.add(classA);
        threeClasses.add(classB);
        threeClasses.add(classC);

        assertEquals("ANOVA P-value", 6.959446E-06,
                 testStatistic.anovaPValue(threeClasses), 1E-12);

        List<double[]> twoClasses = new ArrayList<double[]>();
        twoClasses.add(classA);
        twoClasses.add(classB);

        assertEquals("ANOVA P-value",  0.904212960464,
                 testStatistic.anovaPValue(twoClasses), 1E-12);

    }

// org.apache.commons.math.stat.inference.OneWayAnovaTest::testAnovaTest
    public void testAnovaTest() throws Exception {
        
        List<double[]> threeClasses = new ArrayList<double[]>();
        threeClasses.add(classA);
        threeClasses.add(classB);
        threeClasses.add(classC);

        assertTrue("ANOVA Test P<0.01", testStatistic.anovaTest(threeClasses, 0.01));

        List<double[]> twoClasses = new ArrayList<double[]>();
        twoClasses.add(classA);
        twoClasses.add(classB);

        assertFalse("ANOVA Test P>0.01", testStatistic.anovaTest(twoClasses, 0.01));
    }

// org.apache.commons.math.stat.inference.TTestTest::testOneSampleT
    public void testOneSampleT() throws Exception {
        double[] observed =
            {93.0, 103.0, 95.0, 101.0, 91.0, 105.0, 96.0, 94.0, 101.0,  88.0, 98.0, 94.0, 101.0, 92.0, 95.0 };
        double mu = 100.0;
        SummaryStatistics sampleStats = null;
        sampleStats = new SummaryStatistics();
        for (int i = 0; i < observed.length; i++) {
            sampleStats.addValue(observed[i]);
        }

        
        assertEquals("t statistic",  -2.81976445346,
                testStatistic.t(mu, observed), 10E-10);
        assertEquals("t statistic",  -2.81976445346,
                testStatistic.t(mu, sampleStats), 10E-10);
        assertEquals("p value", 0.0136390585873,
                testStatistic.tTest(mu, observed), 10E-10);
        assertEquals("p value", 0.0136390585873,
                testStatistic.tTest(mu, sampleStats), 10E-10);

        try {
            testStatistic.t(mu, (double[]) null);
            fail("arguments too short, MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }

        try {
            testStatistic.t(mu, (SummaryStatistics) null);
            fail("arguments too short, MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }

        try {
            testStatistic.t(mu, emptyObs);
            fail("arguments too short, MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }

        try {
            testStatistic.t(mu, emptyStats);
            fail("arguments too short, MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }

        try {
            testStatistic.t(mu, tooShortObs);
            fail("insufficient data to compute t statistic, MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }
        try {
            testStatistic.tTest(mu, tooShortObs);
            fail("insufficient data to perform t test, MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
           
        }

        try {
            testStatistic.t(mu, tooShortStats);
            fail("insufficient data to compute t statistic, MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }
        try {
            testStatistic.tTest(mu, tooShortStats);
            fail("insufficient data to perform t test, MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.stat.inference.TTestTest::testOneSampleTTest
    public void testOneSampleTTest() throws Exception {
        double[] oneSidedP =
            {2d, 0d, 6d, 6d, 3d, 3d, 2d, 3d, -6d, 6d, 6d, 6d, 3d, 0d, 1d, 1d, 0d, 2d, 3d, 3d };
        SummaryStatistics oneSidedPStats = new SummaryStatistics();
        for (int i = 0; i < oneSidedP.length; i++) {
            oneSidedPStats.addValue(oneSidedP[i]);
        }
        
        assertEquals("one sample t stat", 3.86485535541,
                testStatistic.t(0d, oneSidedP), 10E-10);
        assertEquals("one sample t stat", 3.86485535541,
                testStatistic.t(0d, oneSidedPStats),1E-10);
        assertEquals("one sample p value", 0.000521637019637,
                testStatistic.tTest(0d, oneSidedP) / 2d, 10E-10);
        assertEquals("one sample p value", 0.000521637019637,
                testStatistic.tTest(0d, oneSidedPStats) / 2d, 10E-5);
        assertTrue("one sample t-test reject", testStatistic.tTest(0d, oneSidedP, 0.01));
        assertTrue("one sample t-test reject", testStatistic.tTest(0d, oneSidedPStats, 0.01));
        assertTrue("one sample t-test accept", !testStatistic.tTest(0d, oneSidedP, 0.0001));
        assertTrue("one sample t-test accept", !testStatistic.tTest(0d, oneSidedPStats, 0.0001));

        try {
            testStatistic.tTest(0d, oneSidedP, 95);
            fail("alpha out of range, MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }

        try {
            testStatistic.tTest(0d, oneSidedPStats, 95);
            fail("alpha out of range, MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }

    }

// org.apache.commons.math.stat.inference.TTestTest::testTwoSampleTHeterscedastic
    public void testTwoSampleTHeterscedastic() throws Exception {
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

        
        assertEquals("two sample heteroscedastic t stat", 1.60371728768,
                testStatistic.t(sample1, sample2), 1E-10);
        assertEquals("two sample heteroscedastic t stat", 1.60371728768,
                testStatistic.t(sampleStats1, sampleStats2), 1E-10);
        assertEquals("two sample heteroscedastic p value", 0.128839369622,
                testStatistic.tTest(sample1, sample2), 1E-10);
        assertEquals("two sample heteroscedastic p value", 0.128839369622,
                testStatistic.tTest(sampleStats1, sampleStats2), 1E-10);
        assertTrue("two sample heteroscedastic t-test reject",
                testStatistic.tTest(sample1, sample2, 0.2));
        assertTrue("two sample heteroscedastic t-test reject",
                testStatistic.tTest(sampleStats1, sampleStats2, 0.2));
        assertTrue("two sample heteroscedastic t-test accept",
                !testStatistic.tTest(sample1, sample2, 0.1));
        assertTrue("two sample heteroscedastic t-test accept",
                !testStatistic.tTest(sampleStats1, sampleStats2, 0.1));

        try {
            testStatistic.tTest(sample1, sample2, .95);
            fail("alpha out of range, MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }

        try {
            testStatistic.tTest(sampleStats1, sampleStats2, .95);
            fail("alpha out of range, MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }

        try {
            testStatistic.tTest(sample1, tooShortObs, .01);
            fail("insufficient data, MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }

        try {
            testStatistic.tTest(sampleStats1, tooShortStats, .01);
            fail("insufficient data, MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }

        try {
            testStatistic.tTest(sample1, tooShortObs);
            fail("insufficient data, MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
           
        }

        try {
            testStatistic.tTest(sampleStats1, tooShortStats);
            fail("insufficient data, MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }

        try {
            testStatistic.t(sample1, tooShortObs);
            fail("insufficient data, MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }

        try {
            testStatistic.t(sampleStats1, tooShortStats);
            fail("insufficient data, MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
           
        }
    }

// org.apache.commons.math.stat.inference.TTestTest::testTwoSampleTHomoscedastic
    public void testTwoSampleTHomoscedastic() throws Exception {
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

        
        assertEquals("two sample homoscedastic t stat", 0.73096310086,
              testStatistic.homoscedasticT(sample1, sample2), 10E-11);
        assertEquals("two sample homoscedastic p value", 0.4833963785,
                testStatistic.homoscedasticTTest(sampleStats1, sampleStats2), 1E-10);
        assertTrue("two sample homoscedastic t-test reject",
                testStatistic.homoscedasticTTest(sample1, sample2, 0.49));
        assertTrue("two sample homoscedastic t-test accept",
                !testStatistic.homoscedasticTTest(sample1, sample2, 0.48));
    }

// org.apache.commons.math.stat.inference.TTestTest::testSmallSamples
    public void testSmallSamples() throws Exception {
        double[] sample1 = {1d, 3d};
        double[] sample2 = {4d, 5d};

        
        assertEquals(-2.2360679775, testStatistic.t(sample1, sample2),
                1E-10);
        assertEquals(0.198727388935, testStatistic.tTest(sample1, sample2),
                1E-10);
    }

// org.apache.commons.math.stat.inference.TTestTest::testPaired
    public void testPaired() throws Exception {
        double[] sample1 = {1d, 3d, 5d, 7d};
        double[] sample2 = {0d, 6d, 11d, 2d};
        double[] sample3 = {5d, 7d, 8d, 10d};

        
        assertEquals(-0.3133, testStatistic.pairedT(sample1, sample2), 1E-4);
        assertEquals(0.774544295819, testStatistic.pairedTTest(sample1, sample2), 1E-10);
        assertEquals(0.001208, testStatistic.pairedTTest(sample1, sample3), 1E-6);
        assertFalse(testStatistic.pairedTTest(sample1, sample3, .001));
        assertTrue(testStatistic.pairedTTest(sample1, sample3, .002));
    }

// org.apache.commons.math.stat.inference.TestUtilsTest::testChiSquare
    public void testChiSquare() throws Exception {

        
        
        
        

        long[] observed = {10, 9, 11};
        double[] expected = {10, 10, 10};
        assertEquals("chi-square statistic", 0.2,  TestUtils.chiSquare(expected, observed), 10E-12);
        assertEquals("chi-square p-value", 0.904837418036, TestUtils.chiSquareTest(expected, observed), 1E-10);

        long[] observed1 = { 500, 623, 72, 70, 31 };
        double[] expected1 = { 485, 541, 82, 61, 37 };
        assertEquals( "chi-square test statistic", 9.023307936427388, TestUtils.chiSquare(expected1, observed1), 1E-10);
        assertEquals("chi-square p-value", 0.06051952647453607, TestUtils.chiSquareTest(expected1, observed1), 1E-9);
        assertTrue("chi-square test reject", TestUtils.chiSquareTest(expected1, observed1, 0.07));
        assertTrue("chi-square test accept", !TestUtils.chiSquareTest(expected1, observed1, 0.05));

        try {
            TestUtils.chiSquareTest(expected1, observed1, 95);
            fail("alpha out of range, MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }

        long[] tooShortObs = { 0 };
        double[] tooShortEx = { 1 };
        try {
            TestUtils.chiSquare(tooShortEx, tooShortObs);
            fail("arguments too short, MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }

        
        long[] unMatchedObs = { 0, 1, 2, 3 };
        double[] unMatchedEx = { 1, 1, 2 };
        try {
            TestUtils.chiSquare(unMatchedEx, unMatchedObs);
            fail("arrays have different lengths, MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }

        
        expected[0] = 0;
        try {
            TestUtils.chiSquareTest(expected, observed, .01);
            fail("bad expected count, MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }

        
        expected[0] = 1;
        observed[0] = -1;
        try {
            TestUtils.chiSquareTest(expected, observed, .01);
            fail("bad expected count, MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }

    }

// org.apache.commons.math.stat.inference.TestUtilsTest::testChiSquareIndependence
    public void testChiSquareIndependence() throws Exception {

        

        long[][] counts = { {40, 22, 43}, {91, 21, 28}, {60, 10, 22}};
        assertEquals( "chi-square test statistic", 22.709027688, TestUtils.chiSquare(counts), 1E-9);
        assertEquals("chi-square p-value", 0.000144751460134, TestUtils.chiSquareTest(counts), 1E-9);
        assertTrue("chi-square test reject", TestUtils.chiSquareTest(counts, 0.0002));
        assertTrue("chi-square test accept", !TestUtils.chiSquareTest(counts, 0.0001));

        long[][] counts2 = {{10, 15}, {30, 40}, {60, 90} };
        assertEquals( "chi-square test statistic", 0.168965517241, TestUtils.chiSquare(counts2), 1E-9);
        assertEquals("chi-square p-value",0.918987499852, TestUtils.chiSquareTest(counts2), 1E-9);
        assertTrue("chi-square test accept", !TestUtils.chiSquareTest(counts2, 0.1));

        
        long[][] counts3 = { {40, 22, 43}, {91, 21, 28}, {60, 10}};
        try {
            TestUtils.chiSquare(counts3);
            fail("Expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }

        
        long[][] counts4 = {{40, 22, 43}};
        try {
            TestUtils.chiSquare(counts4);
            fail("Expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
        long[][] counts5 = {{40}, {40}, {30}, {10}};
        try {
            TestUtils.chiSquare(counts5);
            fail("Expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }

        
        long[][] counts6 = {{10, -2}, {30, 40}, {60, 90} };
        try {
            TestUtils.chiSquare(counts6);
            fail("Expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }

        
        try {
            TestUtils.chiSquareTest(counts, 0);
            fail("Expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.stat.inference.TestUtilsTest::testChiSquareLargeTestStatistic
    public void testChiSquareLargeTestStatistic() throws Exception {
        double[] exp = new double[] {
                3389119.5, 649136.6, 285745.4, 25357364.76, 11291189.78, 543628.0,
                232921.0, 437665.75
        };

        long[] obs = new long[] {
                2372383, 584222, 257170, 17750155, 7903832, 489265, 209628, 393899
        };
        org.apache.commons.math.stat.inference.ChiSquareTestImpl csti =
            new org.apache.commons.math.stat.inference.ChiSquareTestImpl();
        double cst = csti.chiSquareTest(exp, obs);
        assertEquals("chi-square p-value", 0.0, cst, 1E-3);
        assertEquals( "chi-square test statistic",
                114875.90421929007, TestUtils.chiSquare(exp, obs), 1E-9);
    }

// org.apache.commons.math.stat.inference.TestUtilsTest::testChiSquareZeroCount
    public void testChiSquareZeroCount() throws Exception {
        
        long[][] counts = { {40, 0, 4}, {91, 1, 2}, {60, 2, 0}};
        assertEquals( "chi-square test statistic", 9.67444662263,
                TestUtils.chiSquare(counts), 1E-9);
        assertEquals("chi-square p-value", 0.0462835770603,
                TestUtils.chiSquareTest(counts), 1E-9);
    }

// org.apache.commons.math.stat.inference.TestUtilsTest::testOneSampleT
    public void testOneSampleT() throws Exception {
        double[] observed =
            {93.0, 103.0, 95.0, 101.0, 91.0, 105.0, 96.0, 94.0, 101.0,  88.0, 98.0, 94.0, 101.0, 92.0, 95.0 };
        double mu = 100.0;
        SummaryStatistics sampleStats = null;
        sampleStats = new SummaryStatistics();
        for (int i = 0; i < observed.length; i++) {
            sampleStats.addValue(observed[i]);
        }

        
        assertEquals("t statistic",  -2.81976445346,
                TestUtils.t(mu, observed), 10E-10);
        assertEquals("t statistic",  -2.81976445346,
                TestUtils.t(mu, sampleStats), 10E-10);
        assertEquals("p value", 0.0136390585873,
                TestUtils.tTest(mu, observed), 10E-10);
        assertEquals("p value", 0.0136390585873,
                TestUtils.tTest(mu, sampleStats), 10E-10);

        try {
            TestUtils.t(mu, (double[]) null);
            fail("arguments too short, MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }

        try {
            TestUtils.t(mu, (SummaryStatistics) null);
            fail("arguments too short, MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }

        try {
            TestUtils.t(mu, emptyObs);
            fail("arguments too short, MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }

        try {
            TestUtils.t(mu, emptyStats);
            fail("arguments too short, MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }

        try {
            TestUtils.t(mu, tooShortObs);
            fail("insufficient data to compute t statistic, MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }
        try {
            TestUtils.tTest(mu, tooShortObs);
            fail("insufficient data to perform t test, MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }

        try {
            TestUtils.t(mu, (SummaryStatistics) null);
            fail("insufficient data to compute t statistic, MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }
        try {
            TestUtils.tTest(mu, (SummaryStatistics) null);
            fail("insufficient data to perform t test, MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.stat.inference.TestUtilsTest::testOneSampleTTest
    public void testOneSampleTTest() throws Exception {
        double[] oneSidedP =
            {2d, 0d, 6d, 6d, 3d, 3d, 2d, 3d, -6d, 6d, 6d, 6d, 3d, 0d, 1d, 1d, 0d, 2d, 3d, 3d };
        SummaryStatistics oneSidedPStats = new SummaryStatistics();
        for (int i = 0; i < oneSidedP.length; i++) {
            oneSidedPStats.addValue(oneSidedP[i]);
        }
        
        assertEquals("one sample t stat", 3.86485535541,
                TestUtils.t(0d, oneSidedP), 10E-10);
        assertEquals("one sample t stat", 3.86485535541,
                TestUtils.t(0d, oneSidedPStats),1E-10);
        assertEquals("one sample p value", 0.000521637019637,
                TestUtils.tTest(0d, oneSidedP) / 2d, 10E-10);
        assertEquals("one sample p value", 0.000521637019637,
                TestUtils.tTest(0d, oneSidedPStats) / 2d, 10E-5);
        assertTrue("one sample t-test reject", TestUtils.tTest(0d, oneSidedP, 0.01));
        assertTrue("one sample t-test reject", TestUtils.tTest(0d, oneSidedPStats, 0.01));
        assertTrue("one sample t-test accept", !TestUtils.tTest(0d, oneSidedP, 0.0001));
        assertTrue("one sample t-test accept", !TestUtils.tTest(0d, oneSidedPStats, 0.0001));

        try {
            TestUtils.tTest(0d, oneSidedP, 95);
            fail("alpha out of range, MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }

        try {
            TestUtils.tTest(0d, oneSidedPStats, 95);
            fail("alpha out of range, MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }

    }

// org.apache.commons.math.stat.inference.TestUtilsTest::testTwoSampleTHeterscedastic
    public void testTwoSampleTHeterscedastic() throws Exception {
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

        
        assertEquals("two sample heteroscedastic t stat", 1.60371728768,
                TestUtils.t(sample1, sample2), 1E-10);
        assertEquals("two sample heteroscedastic t stat", 1.60371728768,
                TestUtils.t(sampleStats1, sampleStats2), 1E-10);
        assertEquals("two sample heteroscedastic p value", 0.128839369622,
                TestUtils.tTest(sample1, sample2), 1E-10);
        assertEquals("two sample heteroscedastic p value", 0.128839369622,
                TestUtils.tTest(sampleStats1, sampleStats2), 1E-10);
        assertTrue("two sample heteroscedastic t-test reject",
                TestUtils.tTest(sample1, sample2, 0.2));
        assertTrue("two sample heteroscedastic t-test reject",
                TestUtils.tTest(sampleStats1, sampleStats2, 0.2));
        assertTrue("two sample heteroscedastic t-test accept",
                !TestUtils.tTest(sample1, sample2, 0.1));
        assertTrue("two sample heteroscedastic t-test accept",
                !TestUtils.tTest(sampleStats1, sampleStats2, 0.1));

        try {
            TestUtils.tTest(sample1, sample2, .95);
            fail("alpha out of range, MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }

        try {
            TestUtils.tTest(sampleStats1, sampleStats2, .95);
            fail("alpha out of range, MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }

        try {
            TestUtils.tTest(sample1, tooShortObs, .01);
            fail("insufficient data, MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }

        try {
            TestUtils.tTest(sampleStats1, (SummaryStatistics) null, .01);
            fail("insufficient data, MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }

        try {
            TestUtils.tTest(sample1, tooShortObs);
            fail("insufficient data, MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }

        try {
            TestUtils.tTest(sampleStats1, (SummaryStatistics) null);
            fail("insufficient data, MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }

        try {
            TestUtils.t(sample1, tooShortObs);
            fail("insufficient data, MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }

        try {
            TestUtils.t(sampleStats1, (SummaryStatistics) null);
            fail("insufficient data, MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.stat.inference.TestUtilsTest::testTwoSampleTHomoscedastic
    public void testTwoSampleTHomoscedastic() throws Exception {
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

        
        assertEquals("two sample homoscedastic t stat", 0.73096310086,
                TestUtils.homoscedasticT(sample1, sample2), 10E-11);
        assertEquals("two sample homoscedastic p value", 0.4833963785,
                TestUtils.homoscedasticTTest(sampleStats1, sampleStats2), 1E-10);
        assertTrue("two sample homoscedastic t-test reject",
                TestUtils.homoscedasticTTest(sample1, sample2, 0.49));
        assertTrue("two sample homoscedastic t-test accept",
                !TestUtils.homoscedasticTTest(sample1, sample2, 0.48));
    }

// org.apache.commons.math.stat.inference.TestUtilsTest::testSmallSamples
    public void testSmallSamples() throws Exception {
        double[] sample1 = {1d, 3d};
        double[] sample2 = {4d, 5d};

        
        assertEquals(-2.2360679775, TestUtils.t(sample1, sample2),
                1E-10);
        assertEquals(0.198727388935, TestUtils.tTest(sample1, sample2),
                1E-10);
    }

// org.apache.commons.math.stat.inference.TestUtilsTest::testPaired
    public void testPaired() throws Exception {
        double[] sample1 = {1d, 3d, 5d, 7d};
        double[] sample2 = {0d, 6d, 11d, 2d};
        double[] sample3 = {5d, 7d, 8d, 10d};

        
        assertEquals(-0.3133, TestUtils.pairedT(sample1, sample2), 1E-4);
        assertEquals(0.774544295819, TestUtils.pairedTTest(sample1, sample2), 1E-10);
        assertEquals(0.001208, TestUtils.pairedTTest(sample1, sample3), 1E-6);
        assertFalse(TestUtils.pairedTTest(sample1, sample3, .001));
        assertTrue(TestUtils.pairedTTest(sample1, sample3, .002));
    }

// org.apache.commons.math.stat.inference.TestUtilsTest::testOneWayAnovaUtils
    public void testOneWayAnovaUtils() throws Exception {
        classes.add(classA);
        classes.add(classB);
        classes.add(classC);
        assertEquals(oneWayAnova.anovaFValue(classes),
                TestUtils.oneWayAnovaFValue(classes), 10E-12);
        assertEquals(oneWayAnova.anovaPValue(classes),
                TestUtils.oneWayAnovaPValue(classes), 10E-12);
        assertEquals(oneWayAnova.anovaTest(classes, 0.01),
                TestUtils.oneWayAnovaTest(classes, 0.01));
    }

// org.apache.commons.math.stat.inference.WilcoxonSignedRankTestTest::testWilcoxonSignedRankSimple
    public void testWilcoxonSignedRankSimple() throws Exception {
        
        final double x[] = {1.83, 0.50, 1.62, 2.48, 1.68, 1.88, 1.55, 3.06, 1.30};
        final double y[] = {0.878, 0.647, 0.598, 2.05, 1.06, 1.29, 1.06, 3.14, 1.29};
        
        
        assertEquals(40, testStatistic.wilcoxonSignedRank(x, y), 1e-10);
        assertEquals(0.03906, testStatistic.wilcoxonSignedRankTest(x, y, true), 1e-5);        
        
        
        assertEquals(40, testStatistic.wilcoxonSignedRank(x, y), 1e-10);
        assertEquals(0.0329693812, testStatistic.wilcoxonSignedRankTest(x, y, false), 1e-10);
    }

// org.apache.commons.math.stat.inference.WilcoxonSignedRankTestTest::testWilcoxonSignedRankInputValidation
    public void testWilcoxonSignedRankInputValidation() throws Exception {
        
        final double[] x1 = new double[30];
        final double[] x2 = new double[31];
        final double[] y1 = new double[30];
        final double[] y2 = new double[31];
        for (int i = 0; i < 30; ++i) {
            x1[i] = x2[i] = y1[i] = y2[i] = i;            
        }
        
        
        
        
        try {
            testStatistic.wilcoxonSignedRankTest(x2, y2, true);
            fail("More than 30 samples and exact chosen, IllegalArgumentException expected");
        } catch (IllegalArgumentException ex) {
            
        }
        
        
        try {
            testStatistic.wilcoxonSignedRankTest(new double[] { }, new double[] { 1.0 }, true);
            fail("x does not contain samples (exact), IllegalArgumentException expected");
        } catch (IllegalArgumentException ex) {
            
        }

        try {
            testStatistic.wilcoxonSignedRankTest(new double[] { }, new double[] { 1.0 }, false);
            fail("x does not contain samples (asymptotic), IllegalArgumentException expected");
        } catch (IllegalArgumentException ex) {
            
        }

        try {
            testStatistic.wilcoxonSignedRankTest(new double[] { 1.0 }, new double[] { }, true);
            fail("y does not contain samples (exact), IllegalArgumentException expected");
        } catch (IllegalArgumentException ex) {
            
        }

        try {
            testStatistic.wilcoxonSignedRankTest(new double[] { 1.0 }, new double[] { }, false);
            fail("y does not contain samples (asymptotic), IllegalArgumentException expected");
        } catch (IllegalArgumentException ex) {
            
        }

        
        try {
            testStatistic.wilcoxonSignedRankTest(new double[] { 1.0, 2.0 }, new double[] { 3.0 }, true);
            fail("x and y not same size (exact), IllegalArgumentException expected");
        } catch (IllegalArgumentException ex) {
            
        }

        try {
            testStatistic.wilcoxonSignedRankTest(new double[] { 1.0, 2.0 }, new double[] { 3.0 }, false);
            fail("x and y not same size (asymptotic), IllegalArgumentException expected");
        } catch (IllegalArgumentException ex) {
            
        }
        
        
        try {
            testStatistic.wilcoxonSignedRankTest(null, null, true);
            fail("x and y is null (exact), IllegalArgumentException expected");
        } catch (IllegalArgumentException ex) {
            
        }
        
        try {
            testStatistic.wilcoxonSignedRankTest(null, null, false);
            fail("x and y is null (asymptotic), IllegalArgumentException expected");
        } catch (IllegalArgumentException ex) {
            
        }
        
        
        try {
            testStatistic.wilcoxonSignedRankTest(null, new double[] { 1.0 }, true);
            fail("x is null (exact), IllegalArgumentException expected");
        } catch (IllegalArgumentException ex) {
            
        }
        
        try {
            testStatistic.wilcoxonSignedRankTest(null, new double[] { 1.0 }, false);
            fail("x is null (asymptotic), IllegalArgumentException expected");
        } catch (IllegalArgumentException ex) {
            
        }
        
        try {
            testStatistic.wilcoxonSignedRankTest(new double[] { 1.0 }, null, true);
            fail("y is null (exact), IllegalArgumentException expected");
        } catch (IllegalArgumentException ex) {
            
        }
        
        try {
            testStatistic.wilcoxonSignedRankTest(new double[] { 1.0 }, null, false);
            fail("y is null (asymptotic), IllegalArgumentException expected");
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.stat.ranking.NaturalRankingTest::testDefault
    public void testDefault() { 
        NaturalRanking ranking = new NaturalRanking();
        double[] ranks = ranking.rank(exampleData);
        double[] correctRanks = { 5, 3, 6, 7, 3, 8, 9, 1, 3 };
        TestUtils.assertEquals(correctRanks, ranks, 0d);
        ranks = ranking.rank(tiesFirst);
        correctRanks = new double[] { 1.5, 1.5, 4, 3, 5 };
        TestUtils.assertEquals(correctRanks, ranks, 0d);
        ranks = ranking.rank(tiesLast);
        correctRanks = new double[] { 3.5, 3.5, 2, 1 };
        TestUtils.assertEquals(correctRanks, ranks, 0d);
        ranks = ranking.rank(multipleNaNs);
        correctRanks = new double[] { 1, 2, 3.5, 3.5 };
        TestUtils.assertEquals(correctRanks, ranks, 0d);
        ranks = ranking.rank(multipleTies);
        correctRanks = new double[] { 3, 2, 4.5, 4.5, 6.5, 6.5, 1 };
        TestUtils.assertEquals(correctRanks, ranks, 0d);
        ranks = ranking.rank(allSame);
        correctRanks = new double[] { 2.5, 2.5, 2.5, 2.5 };
        TestUtils.assertEquals(correctRanks, ranks, 0d);
    }

// org.apache.commons.math.stat.ranking.NaturalRankingTest::testNaNsMaximalTiesMinimum
    public void testNaNsMaximalTiesMinimum() {
        NaturalRanking ranking = new NaturalRanking(TiesStrategy.MINIMUM);
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

// org.apache.commons.math.stat.ranking.NaturalRankingTest::testNaNsRemovedTiesSequential
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

// org.apache.commons.math.stat.ranking.NaturalRankingTest::testNaNsMinimalTiesMaximum
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

// org.apache.commons.math.stat.ranking.NaturalRankingTest::testNaNsMinimalTiesAverage
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

// org.apache.commons.math.stat.ranking.NaturalRankingTest::testNaNsFixedTiesRandom
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

// org.apache.commons.math.stat.ranking.NaturalRankingTest::testNaNsAndInfs
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

// org.apache.commons.math.stat.regression.GLSMultipleLinearRegressionTest::cannotAddXSampleData
    public void cannotAddXSampleData() {
        createRegression().newSampleData(new double[]{}, null, null);
    }

// org.apache.commons.math.stat.regression.GLSMultipleLinearRegressionTest::cannotAddNullYSampleData
    public void cannotAddNullYSampleData() {
        createRegression().newSampleData(null, new double[][]{}, null);
    }

// org.apache.commons.math.stat.regression.GLSMultipleLinearRegressionTest::cannotAddSampleDataWithSizeMismatch
    public void cannotAddSampleDataWithSizeMismatch() {
        double[] y = new double[]{1.0, 2.0};
        double[][] x = new double[1][];
        x[0] = new double[]{1.0, 0};
        createRegression().newSampleData(y, x, null);
    }

// org.apache.commons.math.stat.regression.GLSMultipleLinearRegressionTest::cannotAddNullCovarianceData
    public void cannotAddNullCovarianceData() {
        createRegression().newSampleData(new double[]{}, new double[][]{}, null);
    }

// org.apache.commons.math.stat.regression.GLSMultipleLinearRegressionTest::notEnoughData
    public void notEnoughData() {
        double[]   reducedY = new double[y.length - 1];
        double[][] reducedX = new double[x.length - 1][];
        double[][] reducedO = new double[omega.length - 1][];
        System.arraycopy(y,     0, reducedY, 0, reducedY.length);
        System.arraycopy(x,     0, reducedX, 0, reducedX.length);
        System.arraycopy(omega, 0, reducedO, 0, reducedO.length);
        createRegression().newSampleData(reducedY, reducedX, reducedO);
    }

// org.apache.commons.math.stat.regression.GLSMultipleLinearRegressionTest::cannotAddCovarianceDataWithSampleSizeMismatch
    public void cannotAddCovarianceDataWithSampleSizeMismatch() {
        double[] y = new double[]{1.0, 2.0};
        double[][] x = new double[2][];
        x[0] = new double[]{1.0, 0};
        x[1] = new double[]{0, 1.0};
        double[][] omega = new double[1][];
        omega[0] = new double[]{1.0, 0};
        createRegression().newSampleData(y, x, omega);
    }

// org.apache.commons.math.stat.regression.GLSMultipleLinearRegressionTest::cannotAddCovarianceDataThatIsNotSquare
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

// org.apache.commons.math.stat.regression.GLSMultipleLinearRegressionTest::testYVariance
    public void testYVariance() {

        

        GLSMultipleLinearRegression model = new GLSMultipleLinearRegression();
        model.newSampleData(y, x, omega);
        TestUtils.assertEquals(model.calculateYVariance(), 3.5, 0);
    }

// org.apache.commons.math.stat.regression.GLSMultipleLinearRegressionTest::testNewSample2
    public void testNewSample2() throws Exception {
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
        RealMatrix combinedX = regression.X.copy();
        RealVector combinedY = regression.Y.copy();
        RealMatrix combinedCovInv = regression.getOmegaInverse();
        regression.newXSampleData(x);
        regression.newYSampleData(y);
        assertEquals(combinedX, regression.X);
        assertEquals(combinedY, regression.Y);
        assertEquals(combinedCovInv, regression.getOmegaInverse());
    }

// org.apache.commons.math.stat.regression.GLSMultipleLinearRegressionTest::testGLSOLSConsistency
    public void testGLSOLSConsistency() throws Exception {      
        RealMatrix identityCov = MatrixUtils.createRealIdentityMatrix(16);
        GLSMultipleLinearRegression glsModel = new GLSMultipleLinearRegression();
        OLSMultipleLinearRegression olsModel = new OLSMultipleLinearRegression();
        glsModel.newSampleData(longley, 16, 6);
        olsModel.newSampleData(longley, 16, 6);
        glsModel.newCovarianceData(identityCov.getData());
        double[] olsBeta = olsModel.calculateBeta().getData();
        double[] glsBeta = glsModel.calculateBeta().getData();
        
        
        for (int i = 0; i < olsBeta.length; i++) {
            TestUtils.assertRelativelyEquals(olsBeta[i], glsBeta[i], 10E-7);
        }
    }

// org.apache.commons.math.stat.regression.GLSMultipleLinearRegressionTest::testGLSEfficiency
    public void testGLSEfficiency() throws Exception {
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
        final RealMatrix x = ols.X.copy();
        
        
        GLSMultipleLinearRegression gls = new GLSMultipleLinearRegression();
        gls.newSampleData(longley, nObs, 6);
        gls.newCovarianceData(cov.getData());
        
        
        DescriptiveStatistics olsBetaStats = new DescriptiveStatistics();
        DescriptiveStatistics glsBetaStats = new DescriptiveStatistics();
        
        
        
        final int nModels = 10000;
        for (int i = 0; i < nModels; i++) {
            
            
            RealVector u = MatrixUtils.createRealVector(gen.nextVector());
            double[] y = u.add(x.operate(b)).getData();
            
            
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

// org.apache.commons.math.stat.regression.OLSMultipleLinearRegressionTest::cannotAddSampleDataWithSizeMismatch
    public void cannotAddSampleDataWithSizeMismatch() {
        double[] y = new double[]{1.0, 2.0};
        double[][] x = new double[1][];
        x[0] = new double[]{1.0, 0};
        createRegression().newSampleData(y, x);
    }

// org.apache.commons.math.stat.regression.OLSMultipleLinearRegressionTest::testPerfectFit
    public void testPerfectFit() throws Exception {
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
       assertEquals(0.0,
                     errors.subtract(referenceVariance).getNorm(),
                     5.0e-16 * referenceVariance.getNorm());
       assertEquals(1, ((OLSMultipleLinearRegression) regression).calculateRSquared(), 1E-12);
    }

// org.apache.commons.math.stat.regression.OLSMultipleLinearRegressionTest::testLongly
    public void testLongly() throws Exception {
        
        
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
        
        
        assertEquals(304.8540735619638, model.estimateRegressionStandardError(), 1E-10);
        
        
        assertEquals(0.995479004577296, model.calculateRSquared(), 1E-12);
        assertEquals(0.992465007628826, model.calculateAdjustedRSquared(), 1E-12);
        
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
        
        
        assertEquals(475.1655079819517, model.estimateRegressionStandardError(), 1E-10);
        
        
        assertEquals(0.9999670130706, model.calculateRSquared(), 1E-12);
        assertEquals(0.999947220913, model.calculateAdjustedRSquared(), 1E-12);
         
    }

// org.apache.commons.math.stat.regression.OLSMultipleLinearRegressionTest::testSwissFertility
    public void testSwissFertility() throws Exception {
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
        
        
        assertEquals(7.73642194433223, model.estimateRegressionStandardError(), 1E-12);
        
        
        assertEquals(0.649789742860228, model.calculateRSquared(), 1E-12);
        assertEquals(0.6164363850373927, model.calculateAdjustedRSquared(), 1E-12);
        
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
        
        
        assertEquals(17.24710630547, model.estimateRegressionStandardError(), 1E-10);
        
        
        assertEquals(0.946350722085, model.calculateRSquared(), 1E-12);
        assertEquals(0.9413600915813, model.calculateAdjustedRSquared(), 1E-12);
    }

// org.apache.commons.math.stat.regression.OLSMultipleLinearRegressionTest::testHat
    public void testHat() throws Exception {

        
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
                assertEquals(referenceData[k], hat.getEntry(i, j), 10e-3);
                assertEquals(hat.getEntry(i, j), hat.getEntry(j, i), 10e-12);
                k++;
            }
        }

        
        double[] residuals = model.estimateResiduals();
        RealMatrix I = MatrixUtils.createRealIdentityMatrix(10);
        double[] hatResiduals = I.subtract(hat).operate(model.Y).getData();
        TestUtils.assertEquals(residuals, hatResiduals, 10e-12);
    }

// org.apache.commons.math.stat.regression.OLSMultipleLinearRegressionTest::testYVariance
    public void testYVariance() {

        

        OLSMultipleLinearRegression model = new OLSMultipleLinearRegression();
        model.newSampleData(y, x);
        TestUtils.assertEquals(model.calculateYVariance(), 3.5, 0);
    }

// org.apache.commons.math.stat.regression.OLSMultipleLinearRegressionTest::testNewSample2
    public void testNewSample2() throws Exception {
        double[] y = new double[] {1, 2, 3, 4}; 
        double[][] x = new double[][] {
          {19, 22, 33},
          {20, 30, 40},
          {25, 35, 45},
          {27, 37, 47}   
        };
        OLSMultipleLinearRegression regression = new OLSMultipleLinearRegression();
        regression.newSampleData(y, x);
        RealMatrix combinedX = regression.X.copy();
        RealVector combinedY = regression.Y.copy();
        regression.newXSampleData(x);
        regression.newYSampleData(y);
        assertEquals(combinedX, regression.X);
        assertEquals(combinedY, regression.Y);
        
        
        regression.setNoIntercept(true);
        regression.newSampleData(y, x);
        combinedX = regression.X.copy();
        combinedY = regression.Y.copy();
        regression.newXSampleData(x);
        regression.newYSampleData(y);
        assertEquals(combinedX, regression.X);
        assertEquals(combinedY, regression.Y);
    }

// org.apache.commons.math.stat.regression.OLSMultipleLinearRegressionTest::testNewSampleDataYNull
    public void testNewSampleDataYNull() {
        createRegression().newSampleData(null, new double[][] {});
    }

// org.apache.commons.math.stat.regression.OLSMultipleLinearRegressionTest::testNewSampleDataXNull
    public void testNewSampleDataXNull() {
        createRegression().newSampleData(new double[] {}, null);
    }

// org.apache.commons.math.stat.regression.SimpleRegressionTest::testNorris
    public void testNorris() {
        SimpleRegression regression = new SimpleRegression();
        for (int i = 0; i < data.length; i++) {
            regression.addData(data[i][1], data[i][0]);
        }
        
        
        assertEquals("slope", 1.00211681802045, regression.getSlope(), 10E-12);
        assertEquals("slope std err", 0.429796848199937E-03,
                regression.getSlopeStdErr(),10E-12);
        assertEquals("number of observations", 36, regression.getN());
        assertEquals( "intercept", -0.262323073774029,
            regression.getIntercept(),10E-12);
        assertEquals("std err intercept", 0.232818234301152,
            regression.getInterceptStdErr(),10E-12);
        assertEquals("r-square", 0.999993745883712,
            regression.getRSquare(), 10E-12);
        assertEquals("SSR", 4255954.13232369,
            regression.getRegressionSumSquares(), 10E-9);
        assertEquals("MSE", 0.782864662630069,
            regression.getMeanSquareError(), 10E-10);
        assertEquals("SSE", 26.6173985294224,
            regression.getSumSquaredErrors(),10E-9);
        

        assertEquals( "predict(0)",  -0.262323073774029,
            regression.predict(0), 10E-12);
        assertEquals("predict(1)", 1.00211681802045 - 0.262323073774029,
            regression.predict(1), 10E-12);
    }

// org.apache.commons.math.stat.regression.SimpleRegressionTest::testCorr
    public void testCorr() {
        SimpleRegression regression = new SimpleRegression();
        regression.addData(corrData);
        assertEquals("number of observations", 17, regression.getN());
        assertEquals("r-square", .896123, regression.getRSquare(), 10E-6);
        assertEquals("r", -0.94663767742, regression.getR(), 1E-10);
    }

// org.apache.commons.math.stat.regression.SimpleRegressionTest::testNaNs
    public void testNaNs() {
        SimpleRegression regression = new SimpleRegression();
        assertTrue("intercept not NaN", Double.isNaN(regression.getIntercept()));
        assertTrue("slope not NaN", Double.isNaN(regression.getSlope()));
        assertTrue("slope std err not NaN", Double.isNaN(regression.getSlopeStdErr()));
        assertTrue("intercept std err not NaN", Double.isNaN(regression.getInterceptStdErr()));
        assertTrue("MSE not NaN", Double.isNaN(regression.getMeanSquareError()));
        assertTrue("e not NaN", Double.isNaN(regression.getR()));
        assertTrue("r-square not NaN", Double.isNaN(regression.getRSquare()));
        assertTrue( "RSS not NaN", Double.isNaN(regression.getRegressionSumSquares()));
        assertTrue("SSE not NaN",Double.isNaN(regression.getSumSquaredErrors()));
        assertTrue("SSTO not NaN", Double.isNaN(regression.getTotalSumSquares()));
        assertTrue("predict not NaN", Double.isNaN(regression.predict(0)));

        regression.addData(1, 2);
        regression.addData(1, 3);

        
        assertTrue("intercept not NaN", Double.isNaN(regression.getIntercept()));
        assertTrue("slope not NaN", Double.isNaN(regression.getSlope()));
        assertTrue("slope std err not NaN", Double.isNaN(regression.getSlopeStdErr()));
        assertTrue("intercept std err not NaN", Double.isNaN(regression.getInterceptStdErr()));
        assertTrue("MSE not NaN", Double.isNaN(regression.getMeanSquareError()));
        assertTrue("e not NaN", Double.isNaN(regression.getR()));
        assertTrue("r-square not NaN", Double.isNaN(regression.getRSquare()));
        assertTrue("RSS not NaN", Double.isNaN(regression.getRegressionSumSquares()));
        assertTrue("SSE not NaN", Double.isNaN(regression.getSumSquaredErrors()));
        assertTrue("predict not NaN", Double.isNaN(regression.predict(0)));

        
        assertTrue("SSTO NaN", !Double.isNaN(regression.getTotalSumSquares()));

        regression = new SimpleRegression();

        regression.addData(1, 2);
        regression.addData(3, 3);

        
        assertTrue("interceptNaN", !Double.isNaN(regression.getIntercept()));
        assertTrue("slope NaN", !Double.isNaN(regression.getSlope()));
        assertTrue ("slope std err not NaN", Double.isNaN(regression.getSlopeStdErr()));
        assertTrue("intercept std err not NaN", Double.isNaN(regression.getInterceptStdErr()));
        assertTrue("MSE not NaN", Double.isNaN(regression.getMeanSquareError()));
        assertTrue("r NaN", !Double.isNaN(regression.getR()));
        assertTrue("r-square NaN", !Double.isNaN(regression.getRSquare()));
        assertTrue("RSS NaN", !Double.isNaN(regression.getRegressionSumSquares()));
        assertTrue("SSE NaN", !Double.isNaN(regression.getSumSquaredErrors()));
        assertTrue("SSTO NaN", !Double.isNaN(regression.getTotalSumSquares()));
        assertTrue("predict NaN", !Double.isNaN(regression.predict(0)));

        regression.addData(1, 4);

        
        assertTrue("MSE NaN", !Double.isNaN(regression.getMeanSquareError()));
        assertTrue("slope std err NaN", !Double.isNaN(regression.getSlopeStdErr()));
        assertTrue("intercept std err NaN", !Double.isNaN(regression.getInterceptStdErr()));
    }

// org.apache.commons.math.stat.regression.SimpleRegressionTest::testClear
    public void testClear() {
        SimpleRegression regression = new SimpleRegression();
        regression.addData(corrData);
        assertEquals("number of observations", 17, regression.getN());
        regression.clear();
        assertEquals("number of observations", 0, regression.getN());
        regression.addData(corrData);
        assertEquals("r-square", .896123, regression.getRSquare(), 10E-6);
        regression.addData(data);
        assertEquals("number of observations", 53, regression.getN());
    }

// org.apache.commons.math.stat.regression.SimpleRegressionTest::testInference
    public void testInference() throws Exception {
        
        
        SimpleRegression regression = new SimpleRegression();
        regression.addData(infData);
        assertEquals("slope std err", 0.011448491,
                regression.getSlopeStdErr(), 1E-10);
        assertEquals("std err intercept", 0.286036932,
                regression.getInterceptStdErr(),1E-8);
        assertEquals("significance", 4.596e-07,
                regression.getSignificance(),1E-8);
        assertEquals("slope conf interval half-width", 0.0270713794287,
                regression.getSlopeConfidenceInterval(),1E-8);
        
        regression = new SimpleRegression();
        regression.addData(infData2);
        assertEquals("slope std err", 1.07260253,
                regression.getSlopeStdErr(), 1E-8);
        assertEquals("std err intercept",4.17718672,
                regression.getInterceptStdErr(),1E-8);
        assertEquals("significance", 0.261829133982,
                regression.getSignificance(),1E-11);
        assertEquals("slope conf interval half-width", 2.97802204827,
                regression.getSlopeConfidenceInterval(),1E-8);
        

        
        assertTrue("tighter means wider",
                regression.getSlopeConfidenceInterval() < regression.getSlopeConfidenceInterval(0.01));

        try {
            regression.getSlopeConfidenceInterval(1);
            fail("expecting MathIllegalArgumentException for alpha = 1");
        } catch (MathIllegalArgumentException ex) {
            
        }

    }

// org.apache.commons.math.stat.regression.SimpleRegressionTest::testPerfect
    public void testPerfect() throws Exception {
        SimpleRegression regression = new SimpleRegression();
        int n = 100;
        for (int i = 0; i < n; i++) {
            regression.addData(((double) i) / (n - 1), i);
        }
        assertEquals(0.0, regression.getSignificance(), 1.0e-5);
        assertTrue(regression.getSlope() > 0.0);
        assertTrue(regression.getSumSquaredErrors() >= 0.0);
    }

// org.apache.commons.math.stat.regression.SimpleRegressionTest::testPerfectNegative
    public void testPerfectNegative() throws Exception {
        SimpleRegression regression = new SimpleRegression();
        int n = 100;
        for (int i = 0; i < n; i++) {
            regression.addData(- ((double) i) / (n - 1), i);
        }

        assertEquals(0.0, regression.getSignificance(), 1.0e-5);
        assertTrue(regression.getSlope() < 0.0);
    }

// org.apache.commons.math.stat.regression.SimpleRegressionTest::testRandom
    public void testRandom() throws Exception {
        SimpleRegression regression = new SimpleRegression();
        Random random = new Random(1);
        int n = 100;
        for (int i = 0; i < n; i++) {
            regression.addData(((double) i) / (n - 1), random.nextDouble());
        }

        assertTrue( 0.0 < regression.getSignificance()
                    && regression.getSignificance() < 1.0);
    }

// org.apache.commons.math.stat.regression.SimpleRegressionTest::testSSENonNegative
    public void testSSENonNegative() {
        double[] y = { 8915.102, 8919.302, 8923.502 };
        double[] x = { 1.107178495E2, 1.107264895E2, 1.107351295E2 };
        SimpleRegression reg = new SimpleRegression();
        for (int i = 0; i < x.length; i++) {
            reg.addData(x[i], y[i]);
        }
        assertTrue(reg.getSumSquaredErrors() >= 0.0);
    }

// org.apache.commons.math.stat.regression.SimpleRegressionTest::testRemoveXY
    public void testRemoveXY() throws Exception {
        
        SimpleRegression regression = new SimpleRegression();
        regression.addData(infData);
        regression.removeData(removeX, removeY);
        regression.addData(removeX, removeY);
        
        assertEquals("slope std err", 0.011448491,
                regression.getSlopeStdErr(), 1E-10);
        assertEquals("std err intercept", 0.286036932,
                regression.getInterceptStdErr(),1E-8);
        assertEquals("significance", 4.596e-07,
                regression.getSignificance(),1E-8);
        assertEquals("slope conf interval half-width", 0.0270713794287,
                regression.getSlopeConfidenceInterval(),1E-8);
     }

// org.apache.commons.math.stat.regression.SimpleRegressionTest::testRemoveSingle
    public void testRemoveSingle() throws Exception {
        
        SimpleRegression regression = new SimpleRegression();
        regression.addData(infData);
        regression.removeData(removeSingle);
        regression.addData(removeSingle);
        
        assertEquals("slope std err", 0.011448491,
                regression.getSlopeStdErr(), 1E-10);
        assertEquals("std err intercept", 0.286036932,
                regression.getInterceptStdErr(),1E-8);
        assertEquals("significance", 4.596e-07,
                regression.getSignificance(),1E-8);
        assertEquals("slope conf interval half-width", 0.0270713794287,
                regression.getSlopeConfidenceInterval(),1E-8);
     }

// org.apache.commons.math.stat.regression.SimpleRegressionTest::testRemoveMultiple
    public void testRemoveMultiple() throws Exception {
        
        SimpleRegression regression = new SimpleRegression();
        regression.addData(infData);
        regression.removeData(removeMultiple);
        regression.addData(removeMultiple);
        
        assertEquals("slope std err", 0.011448491,
                regression.getSlopeStdErr(), 1E-10);
        assertEquals("std err intercept", 0.286036932,
                regression.getInterceptStdErr(),1E-8);
        assertEquals("significance", 4.596e-07,
                regression.getSignificance(),1E-8);
        assertEquals("slope conf interval half-width", 0.0270713794287,
                regression.getSlopeConfidenceInterval(),1E-8);
     }

// org.apache.commons.math.stat.regression.SimpleRegressionTest::testRemoveObsFromEmpty
    public void testRemoveObsFromEmpty() {
        SimpleRegression regression = new SimpleRegression();
        regression.removeData(removeX, removeY);
        assertEquals(regression.getN(), 0);
    }

// org.apache.commons.math.stat.regression.SimpleRegressionTest::testRemoveObsFromSingle
    public void testRemoveObsFromSingle() {
        SimpleRegression regression = new SimpleRegression();
        regression.addData(removeX, removeY);
        regression.removeData(removeX, removeY);
        assertEquals(regression.getN(), 0);
    }

// org.apache.commons.math.stat.regression.SimpleRegressionTest::testRemoveMultipleToEmpty
    public void testRemoveMultipleToEmpty() {
        SimpleRegression regression = new SimpleRegression();
        regression.addData(removeMultiple);
        regression.removeData(removeMultiple);
        assertEquals(regression.getN(), 0);
    }

// org.apache.commons.math.stat.regression.SimpleRegressionTest::testRemoveMultiplePastEmpty
    public void testRemoveMultiplePastEmpty() {
        SimpleRegression regression = new SimpleRegression();
        regression.addData(removeX, removeY);
        regression.removeData(removeMultiple);
        assertEquals(regression.getN(), 0);
    }

// org.apache.commons.math.transform.FastCosineTransformerTest::testAdHocData
    public void testAdHocData() {
        FastCosineTransformer transformer = new FastCosineTransformer();
        double result[], tolerance = 1E-12;

        double x[] = { 0.0, 1.0, 4.0, 9.0, 16.0, 25.0, 36.0, 49.0, 64.0 };
        double y[] = { 172.0, -105.096569476353, 27.3137084989848,
                      -12.9593152353742, 8.0, -5.78585076868676,
                       4.68629150101524, -4.15826451958632, 4.0 };

        result = transformer.transform(x);
        for (int i = 0; i < result.length; i++) {
            assertEquals(y[i], result[i], tolerance);
        }

        result = transformer.inversetransform(y);
        for (int i = 0; i < result.length; i++) {
            assertEquals(x[i], result[i], tolerance);
        }

        FastFourierTransformer.scaleArray(x, FastMath.sqrt(0.5 * (x.length-1)));

        result = transformer.transform2(y);
        for (int i = 0; i < result.length; i++) {
            assertEquals(x[i], result[i], tolerance);
        }

        result = transformer.inversetransform2(x);
        for (int i = 0; i < result.length; i++) {
            assertEquals(y[i], result[i], tolerance);
        }
    }

// org.apache.commons.math.transform.FastCosineTransformerTest::testSinFunction
    public void testSinFunction() {
        UnivariateRealFunction f = new SinFunction();
        FastCosineTransformer transformer = new FastCosineTransformer();
        double min, max, result[], tolerance = 1E-12; int N = 9;

        double expected[] = { 0.0, 3.26197262739567, 0.0,
                             -2.17958042710327, 0.0, -0.648846697642915,
                              0.0, -0.433545502649478, 0.0 };
        min = 0.0; max = 2.0 * FastMath.PI * N / (N-1);
        result = transformer.transform(f, min, max, N);
        for (int i = 0; i < N; i++) {
            assertEquals(expected[i], result[i], tolerance);
        }

        min = -FastMath.PI; max = FastMath.PI * (N+1) / (N-1);
        result = transformer.transform(f, min, max, N);
        for (int i = 0; i < N; i++) {
            assertEquals(-expected[i], result[i], tolerance);
        }
    }

// org.apache.commons.math.transform.FastCosineTransformerTest::testParameters
    public void testParameters() throws Exception {
        UnivariateRealFunction f = new SinFunction();
        FastCosineTransformer transformer = new FastCosineTransformer();

        try {
            
            transformer.transform(f, 1, -1, 65);
            fail("Expecting IllegalArgumentException - bad interval");
        } catch (IllegalArgumentException ex) {
            
        }
        try {
            
            transformer.transform(f, -1, 1, 1);
            fail("Expecting IllegalArgumentException - bad samples number");
        } catch (IllegalArgumentException ex) {
            
        }
        try {
            
            transformer.transform(f, -1, 1, 64);
            fail("Expecting IllegalArgumentException - bad samples number");
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.transform.FastFourierTransformerTest::testAdHocData
    public void testAdHocData() {
        FastFourierTransformer transformer = new FastFourierTransformer();
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

        result = transformer.transform(x);
        for (int i = 0; i < result.length; i++) {
            assertEquals(y[i].getReal(), result[i].getReal(), tolerance);
            assertEquals(y[i].getImaginary(), result[i].getImaginary(), tolerance);
        }

        result = transformer.inversetransform(y);
        for (int i = 0; i < result.length; i++) {
            assertEquals(x[i], result[i].getReal(), tolerance);
            assertEquals(0.0, result[i].getImaginary(), tolerance);
        }

        double x2[] = {10.4, 21.6, 40.8, 13.6, 23.2, 32.8, 13.6, 19.2};
        FastFourierTransformer.scaleArray(x2, 1.0 / FastMath.sqrt(x2.length));
        Complex y2[] = y;

        result = transformer.transform2(y2);
        for (int i = 0; i < result.length; i++) {
            assertEquals(x2[i], result[i].getReal(), tolerance);
            assertEquals(0.0, result[i].getImaginary(), tolerance);
        }

        result = transformer.inversetransform2(x2);
        for (int i = 0; i < result.length; i++) {
            assertEquals(y2[i].getReal(), result[i].getReal(), tolerance);
            assertEquals(y2[i].getImaginary(), result[i].getImaginary(), tolerance);
        }
    }

// org.apache.commons.math.transform.FastFourierTransformerTest::test2DData
    public void test2DData() {
        FastFourierTransformer transformer = new FastFourierTransformer();
        double tolerance = 1E-12;
        Complex[][] input = new Complex[][] {new Complex[] {new Complex(1, 0),
                                                            new Complex(2, 0)},
                                             new Complex[] {new Complex(3, 1),
                                                            new Complex(4, 2)}};
        Complex[][] goodOutput = new Complex[][] {new Complex[] {new Complex(5,
                1.5), new Complex(-1, -.5)}, new Complex[] {new Complex(-2,
                -1.5), new Complex(0, .5)}};
        Complex[][] output = (Complex[][])transformer.mdfft(input, true);
        Complex[][] output2 = (Complex[][])transformer.mdfft(output, false);

        assertEquals(input.length, output.length);
        assertEquals(input.length, output2.length);
        assertEquals(input[0].length, output[0].length);
        assertEquals(input[0].length, output2[0].length);
        assertEquals(input[1].length, output[1].length);
        assertEquals(input[1].length, output2[1].length);

        for (int i = 0; i < input.length; i++) {
            for (int j = 0; j < input[0].length; j++) {
                assertEquals(input[i][j].getImaginary(), output2[i][j].getImaginary(),
                             tolerance);
                assertEquals(input[i][j].getReal(), output2[i][j].getReal(), tolerance);
                assertEquals(goodOutput[i][j].getImaginary(), output[i][j].getImaginary(),
                             tolerance);
                assertEquals(goodOutput[i][j].getReal(), output[i][j].getReal(), tolerance);
            }
        }
    }

// org.apache.commons.math.transform.FastFourierTransformerTest::testSinFunction
    public void testSinFunction() {
        UnivariateRealFunction f = new SinFunction();
        FastFourierTransformer transformer = new FastFourierTransformer();
        Complex result[]; int N = 1 << 8;
        double min, max, tolerance = 1E-12;

        min = 0.0; max = 2.0 * FastMath.PI;
        result = transformer.transform(f, min, max, N);
        assertEquals(0.0, result[1].getReal(), tolerance);
        assertEquals(-(N >> 1), result[1].getImaginary(), tolerance);
        assertEquals(0.0, result[N-1].getReal(), tolerance);
        assertEquals(N >> 1, result[N-1].getImaginary(), tolerance);
        for (int i = 0; i < N-1; i += (i == 0 ? 2 : 1)) {
            assertEquals(0.0, result[i].getReal(), tolerance);
            assertEquals(0.0, result[i].getImaginary(), tolerance);
        }

        min = -FastMath.PI; max = FastMath.PI;
        result = transformer.inversetransform(f, min, max, N);
        assertEquals(0.0, result[1].getReal(), tolerance);
        assertEquals(-0.5, result[1].getImaginary(), tolerance);
        assertEquals(0.0, result[N-1].getReal(), tolerance);
        assertEquals(0.5, result[N-1].getImaginary(), tolerance);
        for (int i = 0; i < N-1; i += (i == 0 ? 2 : 1)) {
            assertEquals(0.0, result[i].getReal(), tolerance);
            assertEquals(0.0, result[i].getImaginary(), tolerance);
        }
    }

// org.apache.commons.math.transform.FastFourierTransformerTest::testParameters
    public void testParameters() throws Exception {
        UnivariateRealFunction f = new SinFunction();
        FastFourierTransformer transformer = new FastFourierTransformer();

        try {
            
            transformer.transform(f, 1, -1, 64);
            fail("Expecting IllegalArgumentException - bad interval");
        } catch (IllegalArgumentException ex) {
            
        }
        try {
            
            transformer.transform(f, -1, 1, 0);
            fail("Expecting IllegalArgumentException - bad samples number");
        } catch (IllegalArgumentException ex) {
            
        }
        try {
            
            transformer.transform(f, -1, 1, 100);
            fail("Expecting IllegalArgumentException - bad samples number");
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.transform.FastSineTransformerTest::testAdHocData
    public void testAdHocData() {
        FastSineTransformer transformer = new FastSineTransformer();
        double result[], tolerance = 1E-12;

        double x[] = { 0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0 };
        double y[] = { 0.0, 20.1093579685034, -9.65685424949238,
                       5.98642305066196, -4.0, 2.67271455167720,
                      -1.65685424949238, 0.795649469518633 };

        result = transformer.transform(x);
        for (int i = 0; i < result.length; i++) {
            assertEquals(y[i], result[i], tolerance);
        }

        result = transformer.inversetransform(y);
        for (int i = 0; i < result.length; i++) {
            assertEquals(x[i], result[i], tolerance);
        }

        FastFourierTransformer.scaleArray(x, FastMath.sqrt(x.length / 2.0));

        result = transformer.transform2(y);
        for (int i = 0; i < result.length; i++) {
            assertEquals(x[i], result[i], tolerance);
        }

        result = transformer.inversetransform2(x);
        for (int i = 0; i < result.length; i++) {
            assertEquals(y[i], result[i], tolerance);
        }
    }

// org.apache.commons.math.transform.FastSineTransformerTest::testSinFunction
    public void testSinFunction() {
        UnivariateRealFunction f = new SinFunction();
        FastSineTransformer transformer = new FastSineTransformer();
        double min, max, result[], tolerance = 1E-12; int N = 1 << 8;

        min = 0.0; max = 2.0 * FastMath.PI;
        result = transformer.transform(f, min, max, N);
        assertEquals(N >> 1, result[2], tolerance);
        for (int i = 0; i < N; i += (i == 1 ? 2 : 1)) {
            assertEquals(0.0, result[i], tolerance);
        }

        min = -FastMath.PI; max = FastMath.PI;
        result = transformer.transform(f, min, max, N);
        assertEquals(-(N >> 1), result[2], tolerance);
        for (int i = 0; i < N; i += (i == 1 ? 2 : 1)) {
            assertEquals(0.0, result[i], tolerance);
        }
    }

// org.apache.commons.math.transform.FastSineTransformerTest::testParameters
    public void testParameters() throws Exception {
        UnivariateRealFunction f = new SinFunction();
        FastSineTransformer transformer = new FastSineTransformer();

        try {
            
            transformer.transform(f, 1, -1, 64);
            fail("Expecting IllegalArgumentException - bad interval");
        } catch (IllegalArgumentException ex) {
            
        }
        try {
            
            transformer.transform(f, -1, 1, 0);
            fail("Expecting IllegalArgumentException - bad samples number");
        } catch (IllegalArgumentException ex) {
            
        }
        try {
            
            transformer.transform(f, -1, 1, 100);
            fail("Expecting IllegalArgumentException - bad samples number");
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.util.ContinuedFractionTest::testGoldenRatio
    public void testGoldenRatio(){
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

        try {
            double gr = cf.evaluate(0.0, 10e-9);
            assertEquals(1.61803399, gr, 10e-9);
        } catch (MathException e) {
            fail(e.getMessage());
        }
    }

// org.apache.commons.math.util.FastMathTest::testMinMaxDouble
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
            { MathUtils.SAFE_MIN, MathUtils.EPSILON }
        };
        for (double[] pair : pairs) {
            Assert.assertEquals("min(" + pair[0] + ", " + pair[1] + ")",
                                Math.min(pair[0], pair[1]),
                                FastMath.min(pair[0], pair[1]),
                                MathUtils.EPSILON);
            Assert.assertEquals("min(" + pair[1] + ", " + pair[0] + ")",
                                Math.min(pair[1], pair[0]),
                                FastMath.min(pair[1], pair[0]),
                                MathUtils.EPSILON);
            Assert.assertEquals("max(" + pair[0] + ", " + pair[1] + ")",
                                Math.max(pair[0], pair[1]),
                                FastMath.max(pair[0], pair[1]),
                                MathUtils.EPSILON);
            Assert.assertEquals("max(" + pair[1] + ", " + pair[0] + ")",
                                Math.max(pair[1], pair[0]),
                                FastMath.max(pair[1], pair[0]),
                                MathUtils.EPSILON);
        }
    }

// org.apache.commons.math.util.FastMathTest::testMinMaxFloat
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
                                MathUtils.EPSILON);
            Assert.assertEquals("min(" + pair[1] + ", " + pair[0] + ")",
                                Math.min(pair[1], pair[0]),
                                FastMath.min(pair[1], pair[0]),
                                MathUtils.EPSILON);
            Assert.assertEquals("max(" + pair[0] + ", " + pair[1] + ")",
                                Math.max(pair[0], pair[1]),
                                FastMath.max(pair[0], pair[1]),
                                MathUtils.EPSILON);
            Assert.assertEquals("max(" + pair[1] + ", " + pair[0] + ")",
                                Math.max(pair[1], pair[0]),
                                FastMath.max(pair[1], pair[0]),
                                MathUtils.EPSILON);
        }
    }

// org.apache.commons.math.util.FastMathTest::testConstants
    public void testConstants() {
        Assert.assertEquals(Math.PI, FastMath.PI, 1.0e-20);
        Assert.assertEquals(Math.E, FastMath.E, 1.0e-20);
    }

// org.apache.commons.math.util.FastMathTest::testAtan2
    public void testAtan2() {
        double y1 = 1.2713504628280707e10;
        double x1 = -5.674940885228782e-10;
        Assert.assertEquals(Math.atan2(y1, x1), FastMath.atan2(y1, x1), 2 * MathUtils.EPSILON);
        double y2 = 0.0;
        double x2 = Double.POSITIVE_INFINITY;
        Assert.assertEquals(Math.atan2(y2, x2), FastMath.atan2(y2, x2), MathUtils.SAFE_MIN);
    }

// org.apache.commons.math.util.FastMathTest::testHyperbolic
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

// org.apache.commons.math.util.FastMathTest::testHyperbolicInverses
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
        for (double x = -1 + MathUtils.EPSILON; x < 1 - MathUtils.EPSILON; x += 0.0001) {
            maxErr = FastMath.max(maxErr, FastMath.abs(x - FastMath.tanh(FastMath.atanh(x))) / (2 * FastMath.ulp(x)));
        }
        Assert.assertEquals(0, maxErr, 2);

    }

// org.apache.commons.math.util.FastMathTest::testLogAccuracy
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

// org.apache.commons.math.util.FastMathTest::testLog10Accuracy
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

// org.apache.commons.math.util.FastMathTest::testLog1pAccuracy
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

// org.apache.commons.math.util.FastMathTest::testLogSpecialCases
    public void testLogSpecialCases() {
        double x;

        x = FastMath.log(0.0);
        if (x != Double.NEGATIVE_INFINITY)
            throw new RuntimeException("Log of zero should be -Inf");

        x = FastMath.log(-0.0);
        if (x != Double.NEGATIVE_INFINITY)
            throw new RuntimeException("Log of zero should be -Inf");

        x = FastMath.log(Double.NaN);
        if (x == x)
            throw new RuntimeException("Log of NaN should be NaN");

        x = FastMath.log(-1.0);
        if (x == x)
            throw new RuntimeException("Log of negative number should be NaN");

        x = FastMath.log(Double.MIN_VALUE);
        if (x != -744.4400719213812)
            throw new RuntimeException(
                                       "Log of Double.MIN_VALUE should be -744.4400719213812");

        x = FastMath.log(-1.0);
        if (x == x)
            throw new RuntimeException("Log of negative number should be NaN");

        x = FastMath.log(Double.POSITIVE_INFINITY);
        if (x != Double.POSITIVE_INFINITY)
            throw new RuntimeException("Log of infinity should be infinity");
    }

// org.apache.commons.math.util.FastMathTest::testExpSpecialCases
    public void testExpSpecialCases() {
        double x;

        
        x = FastMath.exp(-745.1332191019411);
        if (x != Double.MIN_VALUE)
            throw new RuntimeException(
                                       "exp(-745.1332191019411) should be Double.MIN_VALUE");

        x = FastMath.exp(-745.1332191019412);
        if (x != 0.0)
            throw new RuntimeException("exp(-745.1332191019412) should be 0.0");

        x = FastMath.exp(Double.NaN);
        if (x == x)
            throw new RuntimeException("exp of NaN should be NaN");

        x = FastMath.exp(Double.POSITIVE_INFINITY);
        if (x != Double.POSITIVE_INFINITY)
            throw new RuntimeException("exp of infinity should be infinity");

        x = FastMath.exp(Double.NEGATIVE_INFINITY);
        if (x != 0.0)
            throw new RuntimeException("exp of -infinity should be 0.0");

        x = FastMath.exp(1.0);
        if (x != Math.E)
            throw new RuntimeException("exp(1) should be Math.E");
    }

// org.apache.commons.math.util.FastMathTest::testPowSpecialCases
    public void testPowSpecialCases() {
        double x;

        x = FastMath.pow(-1.0, 0.0);
        if (x != 1.0)
            throw new RuntimeException("pow(x, 0) should be 1.0");

        x = FastMath.pow(-1.0, -0.0);
        if (x != 1.0)
            throw new RuntimeException("pow(x, -0) should be 1.0");

        x = FastMath.pow(Math.PI, 1.0);
        if (x != Math.PI)
            throw new RuntimeException("pow(PI, 1.0) should be PI");

        x = FastMath.pow(-Math.PI, 1.0);
        if (x != -Math.PI)
            throw new RuntimeException("pow(-PI, 1.0) should be PI");

        x = FastMath.pow(Math.PI, Double.NaN);
        if (x == x)
            throw new RuntimeException("pow(PI, NaN) should be NaN");

        x = FastMath.pow(Double.NaN, Math.PI);
        if (x == x)
            throw new RuntimeException("pow(NaN, PI) should be NaN");

        x = FastMath.pow(2.0, Double.POSITIVE_INFINITY);
        if (x != Double.POSITIVE_INFINITY)
            throw new RuntimeException("pow(2.0, Infinity) should be Infinity");

        x = FastMath.pow(0.5, Double.NEGATIVE_INFINITY);
        if (x != Double.POSITIVE_INFINITY)
            throw new RuntimeException("pow(0.5, -Infinity) should be Infinity");

        x = FastMath.pow(0.5, Double.POSITIVE_INFINITY);
        if (x != 0.0)
            throw new RuntimeException("pow(0.5, Infinity) should be 0.0");

        x = FastMath.pow(2.0, Double.NEGATIVE_INFINITY);
        if (x != 0.0)
            throw new RuntimeException("pow(2.0, -Infinity) should be 0.0");

        x = FastMath.pow(0.0, 0.5);
        if (x != 0.0)
            throw new RuntimeException("pow(0.0, 0.5) should be 0.0");

        x = FastMath.pow(Double.POSITIVE_INFINITY, -0.5);
        if (x != 0.0)
            throw new RuntimeException("pow(Inf, -0.5) should be 0.0");

        x = FastMath.pow(0.0, -0.5);
        if (x != Double.POSITIVE_INFINITY)
            throw new RuntimeException("pow(0.0, -0.5) should be Inf");

        x = FastMath.pow(Double.POSITIVE_INFINITY, 0.5);
        if (x != Double.POSITIVE_INFINITY)
            throw new RuntimeException("pow(Inf, 0.5) should be Inf");

        x = FastMath.pow(-0.0, -3.0);
        if (x != Double.NEGATIVE_INFINITY)
            throw new RuntimeException("pow(-0.0, -3.0) should be -Inf");

        x = FastMath.pow(Double.NEGATIVE_INFINITY, 3.0);
        if (x != Double.NEGATIVE_INFINITY)
            throw new RuntimeException("pow(-Inf, -3.0) should be -Inf");

        x = FastMath.pow(-0.0, -3.5);
        if (x != Double.POSITIVE_INFINITY)
            throw new RuntimeException("pow(-0.0, -3.5) should be Inf");

        x = FastMath.pow(Double.POSITIVE_INFINITY, 3.5);
        if (x != Double.POSITIVE_INFINITY)
            throw new RuntimeException("pow(Inf, 3.5) should be Inf");

        x = FastMath.pow(-2.0, 3.0);
        if (x != -8.0)
            throw new RuntimeException("pow(-2.0, 3.0) should be -8.0");

        x = FastMath.pow(-2.0, 3.5);
        if (x == x)
            throw new RuntimeException("pow(-2.0, 3.5) should be NaN");
    }

// org.apache.commons.math.util.FastMathTest::testAtan2SpecialCases
    public void testAtan2SpecialCases() {
        double x;

        x = FastMath.atan2(Double.NaN, 0.0);
        if (x == x)
            throw new RuntimeException("atan2(NaN, 0.0) should be NaN");

        x = FastMath.atan2(0.0, Double.NaN);
        if (x == x)
            throw new RuntimeException("atan2(0.0, NaN) should be NaN");

        x = FastMath.atan2(0.0, 0.0);
        if (x != 0.0 || 1 / x != Double.POSITIVE_INFINITY)
            throw new RuntimeException("atan2(0.0, 0.0) should be 0.0");

        x = FastMath.atan2(0.0, 0.001);
        if (x != 0.0 || 1 / x != Double.POSITIVE_INFINITY)
            throw new RuntimeException("atan2(0.0, 0.001) should be 0.0");

        x = FastMath.atan2(0.1, Double.POSITIVE_INFINITY);
        if (x != 0.0 || 1 / x != Double.POSITIVE_INFINITY)
            throw new RuntimeException("atan2(0.1, +Inf) should be 0.0");

        x = FastMath.atan2(-0.0, 0.0);
        if (x != 0.0 || 1 / x != Double.NEGATIVE_INFINITY)
            throw new RuntimeException("atan2(-0.0, 0.0) should be -0.0");

        x = FastMath.atan2(-0.0, 0.001);
        if (x != 0.0 || 1 / x != Double.NEGATIVE_INFINITY)
            throw new RuntimeException("atan2(-0.0, 0.001) should be -0.0");

        x = FastMath.atan2(-0.1, Double.POSITIVE_INFINITY);
        if (x != 0.0 || 1 / x != Double.NEGATIVE_INFINITY)
            throw new RuntimeException("atan2(-0.0, +Inf) should be -0.0");

        x = FastMath.atan2(0.0, -0.0);
        if (x != Math.PI)
            throw new RuntimeException("atan2(0.0, -0.0) should be PI");

        x = FastMath.atan2(0.1, Double.NEGATIVE_INFINITY);
        if (x != Math.PI)
            throw new RuntimeException("atan2(0.1, -Inf) should be PI");

        x = FastMath.atan2(-0.0, -0.0);
        if (x != -Math.PI)
            throw new RuntimeException("atan2(-0.0, -0.0) should be -PI");

        x = FastMath.atan2(-0.1, Double.NEGATIVE_INFINITY);
        if (x != -Math.PI)
            throw new RuntimeException("atan2(0.1, -Inf) should be -PI");

        x = FastMath.atan2(0.1, 0.0);
        if (x != Math.PI / 2)
            throw new RuntimeException("atan2(0.1, 0.0) should be PI/2");

        x = FastMath.atan2(0.1, -0.0);
        if (x != Math.PI / 2)
            throw new RuntimeException("atan2(0.1, -0.0) should be PI/2");

        x = FastMath.atan2(Double.POSITIVE_INFINITY, 0.1);
        if (x != Math.PI / 2)
            throw new RuntimeException("atan2(Inf, 0.1) should be PI/2");

        x = FastMath.atan2(Double.POSITIVE_INFINITY, -0.1);
        if (x != Math.PI / 2)
            throw new RuntimeException("atan2(Inf, -0.1) should be PI/2");

        x = FastMath.atan2(-0.1, 0.0);
        if (x != -Math.PI / 2)
            throw new RuntimeException("atan2(-0.1, 0.0) should be -PI/2");

        x = FastMath.atan2(-0.1, -0.0);
        if (x != -Math.PI / 2)
            throw new RuntimeException("atan2(-0.1, -0.0) should be -PI/2");

        x = FastMath.atan2(Double.NEGATIVE_INFINITY, 0.1);
        if (x != -Math.PI / 2)
            throw new RuntimeException("atan2(-Inf, 0.1) should be -PI/2");

        x = FastMath.atan2(Double.NEGATIVE_INFINITY, -0.1);
        if (x != -Math.PI / 2)
            throw new RuntimeException("atan2(-Inf, -0.1) should be -PI/2");

        x = FastMath.atan2(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        if (x != Math.PI / 4)
            throw new RuntimeException("atan2(Inf, Inf) should be PI/4");

        x = FastMath.atan2(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY);
        if (x != Math.PI * 3.0 / 4.0)
            throw new RuntimeException("atan2(Inf, -Inf) should be PI * 3/4");

        x = FastMath.atan2(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
        if (x != -Math.PI / 4)
            throw new RuntimeException("atan2(-Inf, Inf) should be -PI/4");

        x = FastMath.atan2(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
        if (x != -Math.PI * 3.0 / 4.0)
            throw new RuntimeException("atan2(-Inf, -Inf) should be -PI * 3/4");
    }

// org.apache.commons.math.util.FastMathTest::testPowAccuracy
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

// org.apache.commons.math.util.FastMathTest::testExpAccuracy
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

// org.apache.commons.math.util.FastMathTest::testSinAccuracy
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

// org.apache.commons.math.util.FastMathTest::testCosAccuracy
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

// org.apache.commons.math.util.FastMathTest::testTanAccuracy
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

// org.apache.commons.math.util.FastMathTest::testAtanAccuracy
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

// org.apache.commons.math.util.FastMathTest::testAtan2Accuracy
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

// org.apache.commons.math.util.FastMathTest::testExpm1Accuracy
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

// org.apache.commons.math.util.FastMathTest::testAsinAccuracy
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

// org.apache.commons.math.util.FastMathTest::testAcosAccuracy
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

// org.apache.commons.math.util.FastMathTest::testSinhAccuracy
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

// org.apache.commons.math.util.FastMathTest::testCoshAccuracy
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

// org.apache.commons.math.util.FastMathTest::testTanhAccuracy
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

// org.apache.commons.math.util.FastMathTest::testCbrtAccuracy
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

// org.apache.commons.math.util.FastMathTest::testToDegrees
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

// org.apache.commons.math.util.FastMathTest::testToRadians
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

// org.apache.commons.math.util.FastMathTest::testPerformance
    public void testPerformance() {
        final int numberOfRuns = 10000000;
        for (int j = 0; j < 10; j++) {
            double x = 0;
            long time = System.currentTimeMillis();
            for (int i = 0; i < numberOfRuns; i++)
                x += StrictMath.log(Math.PI + i);
            time = System.currentTimeMillis() - time;
            System.out.print("StrictMath.log " + time + "\t" + x + "\t");

            x = 0;
            time = System.currentTimeMillis();
            for (int i = 0; i < numberOfRuns; i++)
                x += FastMath.log(Math.PI + i);
            time = System.currentTimeMillis() - time;
            System.out.println("FastMath.log " + time + "\t" + x);

            x = 0;
            time = System.currentTimeMillis();
            for (int i = 0; i < numberOfRuns; i++)
                x += StrictMath.pow(Math.PI + i / 1e6, i / 1e6);
            time = System.currentTimeMillis() - time;
            System.out.print("StrictMath.pow " + time + "\t" + x + "\t");

            x = 0;
            time = System.currentTimeMillis();
            for (int i = 0; i < numberOfRuns; i++)
                x += FastMath.pow(Math.PI + i / 1e6, i / 1e6);
            time = System.currentTimeMillis() - time;
            System.out.println("FastMath.pow " + time + "\t" + x);

            x = 0;
            time = System.currentTimeMillis();
            for (int i = 0; i < numberOfRuns; i++)
                x += StrictMath.exp(i / 1000000.0);
            time = System.currentTimeMillis() - time;
            System.out.print("StrictMath.exp " + time + "\t" + x + "\t");

            x = 0;
            time = System.currentTimeMillis();
            for (int i = 0; i < numberOfRuns; i++)
                x += FastMath.exp(i / 1000000.0);
            time = System.currentTimeMillis() - time;
            System.out.println("FastMath.exp " + time + "\t" + x);

            x = 0;
            time = System.currentTimeMillis();
            for (int i = 0; i < numberOfRuns; i++)
                x += StrictMath.sin(i / 1000000.0);
            time = System.currentTimeMillis() - time;
            System.out.print("StrictMath.sin " + time + "\t" + x + "\t");

            x = 0;
            time = System.currentTimeMillis();
            for (int i = 0; i < numberOfRuns; i++)
                x += FastMath.sin(i / 1000000.0);
            time = System.currentTimeMillis() - time;
            System.out.println("FastMath.sin " + time + "\t" + x);

            x = 0;
            time = System.currentTimeMillis();
            for (int i = 0; i < numberOfRuns; i++)
                x += StrictMath.asin(i / 10000000.0);
            time = System.currentTimeMillis() - time;
            System.out.print("StrictMath.asin " + time + "\t" + x + "\t");

            x = 0;
            time = System.currentTimeMillis();
            for (int i = 0; i < numberOfRuns; i++)
                x += FastMath.asin(i / 10000000.0);
            time = System.currentTimeMillis() - time;
            System.out.println("FastMath.asin " + time + "\t" + x);

            x = 0;
            time = System.currentTimeMillis();
            for (int i = 0; i < numberOfRuns; i++)
                x += StrictMath.cos(i / 1000000.0);
            time = System.currentTimeMillis() - time;
            System.out.print("StrictMath.cos " + time + "\t" + x + "\t");

            x = 0;
            time = System.currentTimeMillis();
            for (int i = 0; i < numberOfRuns; i++)
                x += FastMath.cos(i / 1000000.0);
            time = System.currentTimeMillis() - time;
            System.out.println("FastMath.cos " + time + "\t" + x);

            x = 0;
            time = System.currentTimeMillis();
            for (int i = 0; i < numberOfRuns; i++)
                x += StrictMath.acos(i / 10000000.0);
            time = System.currentTimeMillis() - time;
            System.out.print("StrictMath.acos " + time + "\t" + x + "\t");

            x = 0;
            time = System.currentTimeMillis();
            for (int i = 0; i < numberOfRuns; i++)
                x += FastMath.acos(i / 10000000.0);
            time = System.currentTimeMillis() - time;
            System.out.println("FastMath.acos " + time + "\t" + x);

            x = 0;
            time = System.currentTimeMillis();
            for (int i = 0; i < numberOfRuns; i++)
                x += StrictMath.tan(i / 1000000.0);
            time = System.currentTimeMillis() - time;
            System.out.print("StrictMath.tan " + time + "\t" + x + "\t");

            x = 0;
            time = System.currentTimeMillis();
            for (int i = 0; i < numberOfRuns; i++)
                x += FastMath.tan(i / 1000000.0);
            time = System.currentTimeMillis() - time;
            System.out.println("FastMath.tan " + time + "\t" + x);

            x = 0;
            time = System.currentTimeMillis();
            for (int i = 0; i < numberOfRuns; i++)
                x += StrictMath.atan(i / 1000000.0);
            time = System.currentTimeMillis() - time;
            System.out.print("StrictMath.atan " + time + "\t" + x + "\t");

            x = 0;
            time = System.currentTimeMillis();
            for (int i = 0; i < numberOfRuns; i++)
                x += FastMath.atan(i / 1000000.0);
            time = System.currentTimeMillis() - time;
            System.out.println("FastMath.atan " + time + "\t" + x);

            x = 0;
            time = System.currentTimeMillis();
            for (int i = 0; i < numberOfRuns; i++)
                x += StrictMath.cbrt(i / 1000000.0);
            time = System.currentTimeMillis() - time;
            System.out.print("StrictMath.cbrt " + time + "\t" + x + "\t");

            x = 0;
            time = System.currentTimeMillis();
            for (int i = 0; i < numberOfRuns; i++)
                x += FastMath.cbrt(i / 1000000.0);
            time = System.currentTimeMillis() - time;
            System.out.println("FastMath.cbrt " + time + "\t" + x);

            x = 0;
            time = System.currentTimeMillis();
            for (int i = 0; i < numberOfRuns; i++)
                x += StrictMath.cosh(i / 1000000.0);
            time = System.currentTimeMillis() - time;
            System.out.print("StrictMath.cosh " + time + "\t" + x + "\t");

            x = 0;
            time = System.currentTimeMillis();
            for (int i = 0; i < numberOfRuns; i++)
                x += FastMath.cosh(i / 1000000.0);
            time = System.currentTimeMillis() - time;
            System.out.println("FastMath.cosh " + time + "\t" + x);

            x = 0;
            time = System.currentTimeMillis();
            for (int i = 0; i < numberOfRuns; i++)
                x += StrictMath.sinh(i / 1000000.0);
            time = System.currentTimeMillis() - time;
            System.out.print("StrictMath.sinh " + time + "\t" + x + "\t");

            x = 0;
            time = System.currentTimeMillis();
            for (int i = 0; i < numberOfRuns; i++)
                x += FastMath.sinh(i / 1000000.0);
            time = System.currentTimeMillis() - time;
            System.out.println("FastMath.sinh " + time + "\t" + x);

            x = 0;
            time = System.currentTimeMillis();
            for (int i = 0; i < numberOfRuns; i++)
                x += StrictMath.tanh(i / 1000000.0);
            time = System.currentTimeMillis() - time;
            System.out.print("StrictMath.tanh " + time + "\t" + x + "\t");

            x = 0;
            time = System.currentTimeMillis();
            for (int i = 0; i < numberOfRuns; i++)
                x += FastMath.tanh(i / 1000000.0);
            time = System.currentTimeMillis() - time;
            System.out.println("FastMath.tanh " + time + "\t" + x);

            x = 0;
            time = System.currentTimeMillis();
            for (int i = 0; i < numberOfRuns; i++)
                x += StrictMath.expm1(-i / 100000.0);
            time = System.currentTimeMillis() - time;
            System.out.print("StrictMath.expm1 " + time + "\t" + x + "\t");

            x = 0;
            time = System.currentTimeMillis();
            for (int i = 0; i < numberOfRuns; i++)
                x += FastMath.expm1(-i / 100000.0);
            time = System.currentTimeMillis() - time;
            System.out.println("FastMath.expm1 " + time + "\t" + x);

            x = 0;
            time = System.currentTimeMillis();
            for (int i = 0; i < numberOfRuns; i++)
                x += FastMath.expm1(-i / 100000.0);
            time = System.currentTimeMillis() - time;
            System.out.println("FastMath.expm1 " + time + "\t" + x);
        }
    }

// org.apache.commons.math.util.MathUtilsTest::test0Choose0
    public void test0Choose0() {
        assertEquals(MathUtils.binomialCoefficientDouble(0, 0), 1d, 0);
        assertEquals(MathUtils.binomialCoefficientLog(0, 0), 0d, 0);
        assertEquals(MathUtils.binomialCoefficient(0, 0), 1);
    }

// org.apache.commons.math.util.MathUtilsTest::testAddAndCheck
    public void testAddAndCheck() {
        int big = Integer.MAX_VALUE;
        int bigNeg = Integer.MIN_VALUE;
        assertEquals(big, MathUtils.addAndCheck(big, 0));
        try {
            MathUtils.addAndCheck(big, 1);
            fail("Expecting MathArithmeticException");
        } catch (MathArithmeticException ex) {
        }
        try {
            MathUtils.addAndCheck(bigNeg, -1);
            fail("Expecting MathArithmeticException");
        } catch (MathArithmeticException ex) {
        }
    }

// org.apache.commons.math.util.MathUtilsTest::testAddAndCheckLong
    public void testAddAndCheckLong() {
        long max = Long.MAX_VALUE;
        long min = Long.MIN_VALUE;
        assertEquals(max, MathUtils.addAndCheck(max, 0L));
        assertEquals(min, MathUtils.addAndCheck(min, 0L));
        assertEquals(max, MathUtils.addAndCheck(0L, max));
        assertEquals(min, MathUtils.addAndCheck(0L, min));
        assertEquals(1, MathUtils.addAndCheck(-1L, 2L));
        assertEquals(1, MathUtils.addAndCheck(2L, -1L));
        assertEquals(-3, MathUtils.addAndCheck(-2L, -1L));
        assertEquals(min, MathUtils.addAndCheck(min + 1, -1L));
        testAddAndCheckLongFailure(max, 1L);
        testAddAndCheckLongFailure(min, -1L);
        testAddAndCheckLongFailure(1L, max);
        testAddAndCheckLongFailure(-1L, min);
    }

// org.apache.commons.math.util.MathUtilsTest::testBinomialCoefficient
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
            assertEquals("5 choose " + i, bcoef5[i], MathUtils.binomialCoefficient(5, i));
        }
        for (int i = 0; i < 7; i++) {
            assertEquals("6 choose " + i, bcoef6[i], MathUtils.binomialCoefficient(6, i));
        }

        for (int n = 1; n < 10; n++) {
            for (int k = 0; k <= n; k++) {
                assertEquals(n + " choose " + k, binomialCoefficient(n, k), MathUtils.binomialCoefficient(n, k));
                assertEquals(n + " choose " + k, binomialCoefficient(n, k), MathUtils.binomialCoefficientDouble(n, k), Double.MIN_VALUE);
                assertEquals(n + " choose " + k, FastMath.log(binomialCoefficient(n, k)), MathUtils.binomialCoefficientLog(n, k), 10E-12);
            }
        }

        int[] n = { 34, 66, 100, 1500, 1500 };
        int[] k = { 17, 33, 10, 1500 - 4, 4 };
        for (int i = 0; i < n.length; i++) {
            long expected = binomialCoefficient(n[i], k[i]);
            assertEquals(n[i] + " choose " + k[i], expected,
                MathUtils.binomialCoefficient(n[i], k[i]));
            assertEquals(n[i] + " choose " + k[i], expected,
                MathUtils.binomialCoefficientDouble(n[i], k[i]), 0.0);
            assertEquals("log(" + n[i] + " choose " + k[i] + ")", FastMath.log(expected),
                MathUtils.binomialCoefficientLog(n[i], k[i]), 0.0);
        }
    }

// org.apache.commons.math.util.MathUtilsTest::testBinomialCoefficientLarge
    public void testBinomialCoefficientLarge() throws Exception {
        
        for (int n = 0; n <= 200; n++) {
            for (int k = 0; k <= n; k++) {
                long ourResult = -1;
                long exactResult = -1;
                boolean shouldThrow = false;
                boolean didThrow = false;
                try {
                    ourResult = MathUtils.binomialCoefficient(n, k);
                } catch (MathArithmeticException ex) {
                    didThrow = true;
                }
                try {
                    exactResult = binomialCoefficient(n, k);
                } catch (MathArithmeticException ex) {
                    shouldThrow = true;
                }
                assertEquals(n + " choose " + k, exactResult, ourResult);
                assertEquals(n + " choose " + k, shouldThrow, didThrow);
                assertTrue(n + " choose " + k, (n > 66 || !didThrow));

                if (!shouldThrow && exactResult > 1) {
                    assertEquals(n + " choose " + k, 1.,
                        MathUtils.binomialCoefficientDouble(n, k) / exactResult, 1e-10);
                    assertEquals(n + " choose " + k, 1,
                        MathUtils.binomialCoefficientLog(n, k) / FastMath.log(exactResult), 1e-10);
                }
            }
        }

        long ourResult = MathUtils.binomialCoefficient(300, 3);
        long exactResult = binomialCoefficient(300, 3);
        assertEquals(exactResult, ourResult);

        ourResult = MathUtils.binomialCoefficient(700, 697);
        exactResult = binomialCoefficient(700, 697);
        assertEquals(exactResult, ourResult);

        
        try {
            MathUtils.binomialCoefficient(700, 300);
            fail("Expecting MathArithmeticException");
        } catch (MathArithmeticException ex) {
            
        }

        int n = 10000;
        ourResult = MathUtils.binomialCoefficient(n, 3);
        exactResult = binomialCoefficient(n, 3);
        assertEquals(exactResult, ourResult);
        assertEquals(1, MathUtils.binomialCoefficientDouble(n, 3) / exactResult, 1e-10);
        assertEquals(1, MathUtils.binomialCoefficientLog(n, 3) / FastMath.log(exactResult), 1e-10);

    }

// org.apache.commons.math.util.MathUtilsTest::testBinomialCoefficientFail
    public void testBinomialCoefficientFail() {
        try {
            MathUtils.binomialCoefficient(4, 5);
            fail("expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }

        try {
            MathUtils.binomialCoefficientDouble(4, 5);
            fail("expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }

        try {
            MathUtils.binomialCoefficientLog(4, 5);
            fail("expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }

        try {
            MathUtils.binomialCoefficient(-1, -2);
            fail("expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
        try {
            MathUtils.binomialCoefficientDouble(-1, -2);
            fail("expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
        try {
            MathUtils.binomialCoefficientLog(-1, -2);
            fail("expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }

        try {
            MathUtils.binomialCoefficient(67, 30);
            fail("expecting MathArithmeticException");
        } catch (MathArithmeticException ex) {
            
        }
        try {
            MathUtils.binomialCoefficient(67, 34);
            fail("expecting MathArithmeticException");
        } catch (MathArithmeticException ex) {
            
        }
        double x = MathUtils.binomialCoefficientDouble(1030, 515);
        assertTrue("expecting infinite binomial coefficient", Double
            .isInfinite(x));
    }

// org.apache.commons.math.util.MathUtilsTest::testCompareTo
    public void testCompareTo() {
      assertEquals(0, MathUtils.compareTo(152.33, 152.32, .011));
      assertTrue(MathUtils.compareTo(152.308, 152.32, .011) < 0);
      assertTrue(MathUtils.compareTo(152.33, 152.318, .011) > 0);
    }

// org.apache.commons.math.util.MathUtilsTest::testCosh
    public void testCosh() {
        double x = 3.0;
        double expected = 10.06766;
        assertEquals(expected, MathUtils.cosh(x), 1.0e-5);
    }

// org.apache.commons.math.util.MathUtilsTest::testCoshNaN
    public void testCoshNaN() {
        assertTrue(Double.isNaN(MathUtils.cosh(Double.NaN)));
    }

// org.apache.commons.math.util.MathUtilsTest::testEqualsIncludingNaN
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
                    assertTrue(MathUtils.equalsIncludingNaN(testArray[i], testArray[j]));
                    assertTrue(MathUtils.equalsIncludingNaN(testArray[j], testArray[i]));
                } else {
                    assertTrue(!MathUtils.equalsIncludingNaN(testArray[i], testArray[j]));
                    assertTrue(!MathUtils.equalsIncludingNaN(testArray[j], testArray[i]));
                }
            }
        }
    }

// org.apache.commons.math.util.MathUtilsTest::testEqualsWithAllowedDelta
    public void testEqualsWithAllowedDelta() {
        assertTrue(MathUtils.equals(153.0000, 153.0000, .0625));
        assertTrue(MathUtils.equals(153.0000, 153.0625, .0625));
        assertTrue(MathUtils.equals(152.9375, 153.0000, .0625));
        assertFalse(MathUtils.equals(153.0000, 153.0625, .0624));
        assertFalse(MathUtils.equals(152.9374, 153.0000, .0625));
        assertFalse(MathUtils.equals(Double.NaN, Double.NaN, 1.0));
        assertTrue(MathUtils.equals(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, 1.0));
        assertTrue(MathUtils.equals(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, 1.0));
        assertFalse(MathUtils.equals(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 1.0));
    }

// org.apache.commons.math.util.MathUtilsTest::testMath475
    public void testMath475() {
        final double a = 1.7976931348623182E16;
        final double b = FastMath.nextUp(a);

        double diff = FastMath.abs(a - b);
        
        
        
        assertTrue(MathUtils.equals(a, b, 0.5 * diff));

        final double c = FastMath.nextUp(b);
        diff = FastMath.abs(a - c);
        
        
        assertTrue(MathUtils.equals(a, c, diff));
        assertFalse(MathUtils.equals(a, c, (1 - 1e-16) * diff));
    }

// org.apache.commons.math.util.MathUtilsTest::testEqualsIncludingNaNWithAllowedDelta
    public void testEqualsIncludingNaNWithAllowedDelta() {
        assertTrue(MathUtils.equalsIncludingNaN(153.0000, 153.0000, .0625));
        assertTrue(MathUtils.equalsIncludingNaN(153.0000, 153.0625, .0625));
        assertTrue(MathUtils.equalsIncludingNaN(152.9375, 153.0000, .0625));
        assertTrue(MathUtils.equalsIncludingNaN(Double.NaN, Double.NaN, 1.0));
        assertTrue(MathUtils.equalsIncludingNaN(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, 1.0));
        assertTrue(MathUtils.equalsIncludingNaN(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, 1.0));
        assertFalse(MathUtils.equalsIncludingNaN(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 1.0));
        assertFalse(MathUtils.equalsIncludingNaN(153.0000, 153.0625, .0624));
        assertFalse(MathUtils.equalsIncludingNaN(152.9374, 153.0000, .0625));
    }

// org.apache.commons.math.util.MathUtilsTest::testFloatEqualsWithAllowedUlps
    public void testFloatEqualsWithAllowedUlps() {
        assertTrue("+0.0f == -0.0f",MathUtils.equals(0.0f, -0.0f));
        assertTrue("+0.0f == -0.0f (1 ulp)",MathUtils.equals(0.0f, -0.0f, 1));
        float oneFloat = 1.0f;
        assertTrue("1.0f == 1.0f + 1 ulp",MathUtils.equals(oneFloat, Float.intBitsToFloat(1 + Float.floatToIntBits(oneFloat))));
        assertTrue("1.0f == 1.0f + 1 ulp (1 ulp)",MathUtils.equals(oneFloat, Float.intBitsToFloat(1 + Float.floatToIntBits(oneFloat)), 1));
        assertFalse("1.0f != 1.0f + 2 ulp (1 ulp)",MathUtils.equals(oneFloat, Float.intBitsToFloat(2 + Float.floatToIntBits(oneFloat)), 1));

        assertTrue(MathUtils.equals(153.0f, 153.0f, 1));

        

        assertTrue(MathUtils.equals(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, 1));
        assertTrue(MathUtils.equals(Double.MAX_VALUE, Float.POSITIVE_INFINITY, 1));

        assertTrue(MathUtils.equals(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY, 1));
        assertTrue(MathUtils.equals(-Float.MAX_VALUE, Float.NEGATIVE_INFINITY, 1));

        assertFalse(MathUtils.equals(Float.NaN, Float.NaN, 1));

        assertFalse(MathUtils.equals(Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY, 100000));
    }

// org.apache.commons.math.util.MathUtilsTest::testEqualsWithAllowedUlps
    public void testEqualsWithAllowedUlps() {
        assertTrue(MathUtils.equals(0.0, -0.0, 1));

        assertTrue(MathUtils.equals(1.0, 1 + FastMath.ulp(1d), 1));
        assertFalse(MathUtils.equals(1.0, 1 + 2 * FastMath.ulp(1d), 1));

        final double nUp1 = FastMath.nextAfter(1d, Double.POSITIVE_INFINITY);
        final double nnUp1 = FastMath.nextAfter(nUp1, Double.POSITIVE_INFINITY);
        assertTrue(MathUtils.equals(1.0, nUp1, 1));
        assertTrue(MathUtils.equals(nUp1, nnUp1, 1));
        assertFalse(MathUtils.equals(1.0, nnUp1, 1));

        assertTrue(MathUtils.equals(0.0, FastMath.ulp(0d), 1));
        assertTrue(MathUtils.equals(0.0, -FastMath.ulp(0d), 1));

        assertTrue(MathUtils.equals(153.0, 153.0, 1));

        assertTrue(MathUtils.equals(153.0, 153.00000000000003, 1));
        assertFalse(MathUtils.equals(153.0, 153.00000000000006, 1));
        assertTrue(MathUtils.equals(153.0, 152.99999999999997, 1));
        assertFalse(MathUtils.equals(153, 152.99999999999994, 1));

        assertTrue(MathUtils.equals(-128.0, -127.99999999999999, 1));
        assertFalse(MathUtils.equals(-128.0, -127.99999999999997, 1));
        assertTrue(MathUtils.equals(-128.0, -128.00000000000003, 1));
        assertFalse(MathUtils.equals(-128.0, -128.00000000000006, 1));

        assertTrue(MathUtils.equals(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, 1));
        assertTrue(MathUtils.equals(Double.MAX_VALUE, Double.POSITIVE_INFINITY, 1));

        assertTrue(MathUtils.equals(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, 1));
        assertTrue(MathUtils.equals(-Double.MAX_VALUE, Double.NEGATIVE_INFINITY, 1));

        assertFalse(MathUtils.equals(Double.NaN, Double.NaN, 1));

        assertFalse(MathUtils.equals(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 100000));
    }

// org.apache.commons.math.util.MathUtilsTest::testEqualsIncludingNaNWithAllowedUlps
    public void testEqualsIncludingNaNWithAllowedUlps() {
        assertTrue(MathUtils.equalsIncludingNaN(0.0, -0.0, 1));

        assertTrue(MathUtils.equalsIncludingNaN(1.0, 1 + FastMath.ulp(1d), 1));
        assertFalse(MathUtils.equalsIncludingNaN(1.0, 1 + 2 * FastMath.ulp(1d), 1));

        final double nUp1 = FastMath.nextAfter(1d, Double.POSITIVE_INFINITY);
        final double nnUp1 = FastMath.nextAfter(nUp1, Double.POSITIVE_INFINITY);
        assertTrue(MathUtils.equalsIncludingNaN(1.0, nUp1, 1));
        assertTrue(MathUtils.equalsIncludingNaN(nUp1, nnUp1, 1));
        assertFalse(MathUtils.equalsIncludingNaN(1.0, nnUp1, 1));

        assertTrue(MathUtils.equalsIncludingNaN(0.0, FastMath.ulp(0d), 1));
        assertTrue(MathUtils.equalsIncludingNaN(0.0, -FastMath.ulp(0d), 1));

        assertTrue(MathUtils.equalsIncludingNaN(153.0, 153.0, 1));

        assertTrue(MathUtils.equalsIncludingNaN(153.0, 153.00000000000003, 1));
        assertFalse(MathUtils.equalsIncludingNaN(153.0, 153.00000000000006, 1));
        assertTrue(MathUtils.equalsIncludingNaN(153.0, 152.99999999999997, 1));
        assertFalse(MathUtils.equalsIncludingNaN(153, 152.99999999999994, 1));

        assertTrue(MathUtils.equalsIncludingNaN(-128.0, -127.99999999999999, 1));
        assertFalse(MathUtils.equalsIncludingNaN(-128.0, -127.99999999999997, 1));
        assertTrue(MathUtils.equalsIncludingNaN(-128.0, -128.00000000000003, 1));
        assertFalse(MathUtils.equalsIncludingNaN(-128.0, -128.00000000000006, 1));

        assertTrue(MathUtils.equalsIncludingNaN(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, 1));
        assertTrue(MathUtils.equalsIncludingNaN(Double.MAX_VALUE, Double.POSITIVE_INFINITY, 1));

        assertTrue(MathUtils.equalsIncludingNaN(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, 1));
        assertTrue(MathUtils.equalsIncludingNaN(-Double.MAX_VALUE, Double.NEGATIVE_INFINITY, 1));

        assertTrue(MathUtils.equalsIncludingNaN(Double.NaN, Double.NaN, 1));

        assertFalse(MathUtils.equalsIncludingNaN(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 100000));
    }

// org.apache.commons.math.util.MathUtilsTest::testArrayEquals
    public void testArrayEquals() {
        assertFalse(MathUtils.equals(new double[] { 1d }, null));
        assertFalse(MathUtils.equals(null, new double[] { 1d }));
        assertTrue(MathUtils.equals((double[]) null, (double[]) null));

        assertFalse(MathUtils.equals(new double[] { 1d }, new double[0]));
        assertTrue(MathUtils.equals(new double[] { 1d }, new double[] { 1d }));
        assertTrue(MathUtils.equals(new double[] {
                                      Double.POSITIVE_INFINITY,
                                      Double.NEGATIVE_INFINITY, 1d, 0d
                                    }, new double[] {
                                      Double.POSITIVE_INFINITY,
                                      Double.NEGATIVE_INFINITY, 1d, 0d
                                    }));
        assertFalse(MathUtils.equals(new double[] { Double.NaN },
                                     new double[] { Double.NaN }));
        assertFalse(MathUtils.equals(new double[] { Double.POSITIVE_INFINITY },
                                     new double[] { Double.NEGATIVE_INFINITY }));
        assertFalse(MathUtils.equals(new double[] { 1d },
                                     new double[] { FastMath.nextAfter(FastMath.nextAfter(1d, 2d), 2d) }));

    }

// org.apache.commons.math.util.MathUtilsTest::testArrayEqualsIncludingNaN
    public void testArrayEqualsIncludingNaN() {
        assertFalse(MathUtils.equalsIncludingNaN(new double[] { 1d }, null));
        assertFalse(MathUtils.equalsIncludingNaN(null, new double[] { 1d }));
        assertTrue(MathUtils.equalsIncludingNaN((double[]) null, (double[]) null));

        assertFalse(MathUtils.equalsIncludingNaN(new double[] { 1d }, new double[0]));
        assertTrue(MathUtils.equalsIncludingNaN(new double[] { 1d }, new double[] { 1d }));
        assertTrue(MathUtils.equalsIncludingNaN(new double[] {
                    Double.NaN, Double.POSITIVE_INFINITY,
                    Double.NEGATIVE_INFINITY, 1d, 0d
                }, new double[] {
                    Double.NaN, Double.POSITIVE_INFINITY,
                    Double.NEGATIVE_INFINITY, 1d, 0d
                }));
        assertFalse(MathUtils.equalsIncludingNaN(new double[] { Double.POSITIVE_INFINITY },
                                                 new double[] { Double.NEGATIVE_INFINITY }));
        assertFalse(MathUtils.equalsIncludingNaN(new double[] { 1d },
                                                 new double[] { FastMath.nextAfter(FastMath.nextAfter(1d, 2d), 2d) }));
    }

// org.apache.commons.math.util.MathUtilsTest::testFactorial
    public void testFactorial() {
        for (int i = 1; i < 21; i++) {
            assertEquals(i + "! ", factorial(i), MathUtils.factorial(i));
            assertEquals(i + "! ", factorial(i), MathUtils.factorialDouble(i), Double.MIN_VALUE);
            assertEquals(i + "! ", FastMath.log(factorial(i)), MathUtils.factorialLog(i), 10E-12);
        }

        assertEquals("0", 1, MathUtils.factorial(0));
        assertEquals("0", 1.0d, MathUtils.factorialDouble(0), 1E-14);
        assertEquals("0", 0.0d, MathUtils.factorialLog(0), 1E-14);
    }

// org.apache.commons.math.util.MathUtilsTest::testFactorialFail
    public void testFactorialFail() {
        try {
            MathUtils.factorial(-1);
            fail("expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
        try {
            MathUtils.factorialDouble(-1);
            fail("expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
        try {
            MathUtils.factorialLog(-1);
            fail("expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
        try {
            MathUtils.factorial(21);
            fail("expecting MathArithmeticException");
        } catch (MathArithmeticException ex) {
            
        }
        assertTrue("expecting infinite factorial value", Double.isInfinite(MathUtils.factorialDouble(171)));
    }

// org.apache.commons.math.util.MathUtilsTest::testGcd
    public void testGcd() {
        int a = 30;
        int b = 50;
        int c = 77;

        assertEquals(0, MathUtils.gcd(0, 0));

        assertEquals(b, MathUtils.gcd(0, b));
        assertEquals(a, MathUtils.gcd(a, 0));
        assertEquals(b, MathUtils.gcd(0, -b));
        assertEquals(a, MathUtils.gcd(-a, 0));

        assertEquals(10, MathUtils.gcd(a, b));
        assertEquals(10, MathUtils.gcd(-a, b));
        assertEquals(10, MathUtils.gcd(a, -b));
        assertEquals(10, MathUtils.gcd(-a, -b));

        assertEquals(1, MathUtils.gcd(a, c));
        assertEquals(1, MathUtils.gcd(-a, c));
        assertEquals(1, MathUtils.gcd(a, -c));
        assertEquals(1, MathUtils.gcd(-a, -c));

        assertEquals(3 * (1<<15), MathUtils.gcd(3 * (1<<20), 9 * (1<<15)));

        assertEquals(Integer.MAX_VALUE, MathUtils.gcd(Integer.MAX_VALUE, 0));
        assertEquals(Integer.MAX_VALUE, MathUtils.gcd(-Integer.MAX_VALUE, 0));
        assertEquals(1<<30, MathUtils.gcd(1<<30, -Integer.MIN_VALUE));
        try {
            
            MathUtils.gcd(Integer.MIN_VALUE, 0);
            fail("expecting MathArithmeticException");
        } catch (MathArithmeticException expected) {
            
        }
        try {
            
            MathUtils.gcd(0, Integer.MIN_VALUE);
            fail("expecting MathArithmeticException");
        } catch (MathArithmeticException expected) {
            
        }
        try {
            
            MathUtils.gcd(Integer.MIN_VALUE, Integer.MIN_VALUE);
            fail("expecting MathArithmeticException");
        } catch (MathArithmeticException expected) {
            
        }
    }

// org.apache.commons.math.util.MathUtilsTest::testGcdLong
    public void  testGcdLong(){
        long a = 30;
        long b = 50;
        long c = 77;

        assertEquals(0, MathUtils.gcd(0L, 0));

        assertEquals(b, MathUtils.gcd(0, b));
        assertEquals(a, MathUtils.gcd(a, 0));
        assertEquals(b, MathUtils.gcd(0, -b));
        assertEquals(a, MathUtils.gcd(-a, 0));

        assertEquals(10, MathUtils.gcd(a, b));
        assertEquals(10, MathUtils.gcd(-a, b));
        assertEquals(10, MathUtils.gcd(a, -b));
        assertEquals(10, MathUtils.gcd(-a, -b));

        assertEquals(1, MathUtils.gcd(a, c));
        assertEquals(1, MathUtils.gcd(-a, c));
        assertEquals(1, MathUtils.gcd(a, -c));
        assertEquals(1, MathUtils.gcd(-a, -c));

        assertEquals(3L * (1L<<45), MathUtils.gcd(3L * (1L<<50), 9L * (1L<<45)));

        assertEquals(1L<<45, MathUtils.gcd(1L<<45, Long.MIN_VALUE));

        assertEquals(Long.MAX_VALUE, MathUtils.gcd(Long.MAX_VALUE, 0L));
        assertEquals(Long.MAX_VALUE, MathUtils.gcd(-Long.MAX_VALUE, 0L));
        assertEquals(1, MathUtils.gcd(60247241209L, 153092023L));
        try {
            
            MathUtils.gcd(Long.MIN_VALUE, 0);
            fail("expecting MathArithmeticException");
        } catch (MathArithmeticException expected) {
            
        }
        try {
            
            MathUtils.gcd(0, Long.MIN_VALUE);
            fail("expecting MathArithmeticException");
        } catch (MathArithmeticException expected) {
            
        }
        try {
            
            MathUtils.gcd(Long.MIN_VALUE, Long.MIN_VALUE);
            fail("expecting MathArithmeticException");
        } catch (MathArithmeticException expected) {
            
        }
    }

// org.apache.commons.math.util.MathUtilsTest::testGcdConsistency
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
            assertEquals(gcd, MathUtils.gcd(i1, i2));
            long l1 = i1;
            long l2 = i2;
            assertEquals(gcd, MathUtils.gcd(l1, l2));
        }
    }

// org.apache.commons.math.util.MathUtilsTest::testHash
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
                    assertEquals(MathUtils.hash(testArray[i]), MathUtils.hash(testArray[j]));
                    assertEquals(MathUtils.hash(testArray[j]), MathUtils.hash(testArray[i]));
                } else {
                    assertTrue(MathUtils.hash(testArray[i]) != MathUtils.hash(testArray[j]));
                    assertTrue(MathUtils.hash(testArray[j]) != MathUtils.hash(testArray[i]));
                }
            }
        }
    }

// org.apache.commons.math.util.MathUtilsTest::testArrayHash
    public void testArrayHash() {
        assertEquals(0, MathUtils.hash((double[]) null));
        assertEquals(MathUtils.hash(new double[] {
                                      Double.NaN, Double.POSITIVE_INFINITY,
                                      Double.NEGATIVE_INFINITY, 1d, 0d
                                    }),
                     MathUtils.hash(new double[] {
                                      Double.NaN, Double.POSITIVE_INFINITY,
                                      Double.NEGATIVE_INFINITY, 1d, 0d
                                    }));
        assertFalse(MathUtils.hash(new double[] { 1d }) ==
                    MathUtils.hash(new double[] { FastMath.nextAfter(1d, 2d) }));
        assertFalse(MathUtils.hash(new double[] { 1d }) ==
                    MathUtils.hash(new double[] { 1d, 1d }));
    }

// org.apache.commons.math.util.MathUtilsTest::testPermutedArrayHash
    public void testPermutedArrayHash() {
        double[] original = new double[10];
        double[] permuted = new double[10];
        RandomDataImpl random = new RandomDataImpl();

        
        for (int i = 0; i < 10; i++) {
            original[i] = random.nextUniform(i + 0.5, i + 0.75);
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

        
        assertFalse(MathUtils.hash(original) == MathUtils.hash(permuted));
    }

// org.apache.commons.math.util.MathUtilsTest::testIndicatorByte
    public void testIndicatorByte() {
        assertEquals((byte)1, MathUtils.indicator((byte)2));
        assertEquals((byte)1, MathUtils.indicator((byte)0));
        assertEquals((byte)(-1), MathUtils.indicator((byte)(-2)));
    }

// org.apache.commons.math.util.MathUtilsTest::testIndicatorDouble
    public void testIndicatorDouble() {
        double delta = 0.0;
        assertEquals(1.0, MathUtils.indicator(2.0), delta);
        assertEquals(1.0, MathUtils.indicator(0.0), delta);
        assertEquals(-1.0, MathUtils.indicator(-2.0), delta);
        assertEquals(Double.NaN, MathUtils.indicator(Double.NaN));
    }

// org.apache.commons.math.util.MathUtilsTest::testIndicatorFloat
    public void testIndicatorFloat() {
        float delta = 0.0F;
        assertEquals(1.0F, MathUtils.indicator(2.0F), delta);
        assertEquals(1.0F, MathUtils.indicator(0.0F), delta);
        assertEquals(-1.0F, MathUtils.indicator(-2.0F), delta);
    }

// org.apache.commons.math.util.MathUtilsTest::testIndicatorInt
    public void testIndicatorInt() {
        assertEquals(1, MathUtils.indicator((2)));
        assertEquals(1, MathUtils.indicator((0)));
        assertEquals((-1), MathUtils.indicator((-2)));
    }

// org.apache.commons.math.util.MathUtilsTest::testIndicatorLong
    public void testIndicatorLong() {
        assertEquals(1L, MathUtils.indicator(2L));
        assertEquals(1L, MathUtils.indicator(0L));
        assertEquals(-1L, MathUtils.indicator(-2L));
    }

// org.apache.commons.math.util.MathUtilsTest::testIndicatorShort
    public void testIndicatorShort() {
        assertEquals((short)1, MathUtils.indicator((short)2));
        assertEquals((short)1, MathUtils.indicator((short)0));
        assertEquals((short)(-1), MathUtils.indicator((short)(-2)));
    }

// org.apache.commons.math.util.MathUtilsTest::testLcm
    public void testLcm() {
        int a = 30;
        int b = 50;
        int c = 77;

        assertEquals(0, MathUtils.lcm(0, b));
        assertEquals(0, MathUtils.lcm(a, 0));
        assertEquals(b, MathUtils.lcm(1, b));
        assertEquals(a, MathUtils.lcm(a, 1));
        assertEquals(150, MathUtils.lcm(a, b));
        assertEquals(150, MathUtils.lcm(-a, b));
        assertEquals(150, MathUtils.lcm(a, -b));
        assertEquals(150, MathUtils.lcm(-a, -b));
        assertEquals(2310, MathUtils.lcm(a, c));

        
        
        assertEquals((1<<20)*15, MathUtils.lcm((1<<20)*3, (1<<20)*5));

        
        assertEquals(0, MathUtils.lcm(0, 0));

        try {
            
            MathUtils.lcm(Integer.MIN_VALUE, 1);
            fail("Expecting MathArithmeticException");
        } catch (MathArithmeticException expected) {
            
        }

        try {
            
            MathUtils.lcm(Integer.MIN_VALUE, 1<<20);
            fail("Expecting MathArithmeticException");
        } catch (MathArithmeticException expected) {
            
        }

        try {
            MathUtils.lcm(Integer.MAX_VALUE, Integer.MAX_VALUE - 1);
            fail("Expecting MathArithmeticException");
        } catch (MathArithmeticException expected) {
            
        }
    }

// org.apache.commons.math.util.MathUtilsTest::testLcmLong
    public void testLcmLong() {
        long a = 30;
        long b = 50;
        long c = 77;

        assertEquals(0, MathUtils.lcm(0, b));
        assertEquals(0, MathUtils.lcm(a, 0));
        assertEquals(b, MathUtils.lcm(1, b));
        assertEquals(a, MathUtils.lcm(a, 1));
        assertEquals(150, MathUtils.lcm(a, b));
        assertEquals(150, MathUtils.lcm(-a, b));
        assertEquals(150, MathUtils.lcm(a, -b));
        assertEquals(150, MathUtils.lcm(-a, -b));
        assertEquals(2310, MathUtils.lcm(a, c));

        assertEquals(Long.MAX_VALUE, MathUtils.lcm(60247241209L, 153092023L));

        
        
        assertEquals((1L<<50)*15, MathUtils.lcm((1L<<45)*3, (1L<<50)*5));

        
        assertEquals(0L, MathUtils.lcm(0L, 0L));

        try {
            
            MathUtils.lcm(Long.MIN_VALUE, 1);
            fail("Expecting MathArithmeticException");
        } catch (MathArithmeticException expected) {
            
        }

        try {
            
            MathUtils.lcm(Long.MIN_VALUE, 1<<20);
            fail("Expecting MathArithmeticException");
        } catch (MathArithmeticException expected) {
            
        }

        assertEquals((long) Integer.MAX_VALUE * (Integer.MAX_VALUE - 1),
            MathUtils.lcm((long)Integer.MAX_VALUE, Integer.MAX_VALUE - 1));
        try {
            MathUtils.lcm(Long.MAX_VALUE, Long.MAX_VALUE - 1);
            fail("Expecting MathArithmeticException");
        } catch (MathArithmeticException expected) {
            
        }
    }

// org.apache.commons.math.util.MathUtilsTest::testLog
    public void testLog() {
        assertEquals(2.0, MathUtils.log(2, 4), 0);
        assertEquals(3.0, MathUtils.log(2, 8), 0);
        assertTrue(Double.isNaN(MathUtils.log(-1, 1)));
        assertTrue(Double.isNaN(MathUtils.log(1, -1)));
        assertTrue(Double.isNaN(MathUtils.log(0, 0)));
        assertEquals(0, MathUtils.log(0, 10), 0);
        assertEquals(Double.NEGATIVE_INFINITY, MathUtils.log(10, 0), 0);
    }

// org.apache.commons.math.util.MathUtilsTest::testMulAndCheck
    public void testMulAndCheck() {
        int big = Integer.MAX_VALUE;
        int bigNeg = Integer.MIN_VALUE;
        assertEquals(big, MathUtils.mulAndCheck(big, 1));
        try {
            MathUtils.mulAndCheck(big, 2);
            fail("Expecting MathArithmeticException");
        } catch (MathArithmeticException ex) {
        }
        try {
            MathUtils.mulAndCheck(bigNeg, 2);
            fail("Expecting MathArithmeticException");
        } catch (MathArithmeticException ex) {
        }
    }

// org.apache.commons.math.util.MathUtilsTest::testMulAndCheckLong
    public void testMulAndCheckLong() {
        long max = Long.MAX_VALUE;
        long min = Long.MIN_VALUE;
        assertEquals(max, MathUtils.mulAndCheck(max, 1L));
        assertEquals(min, MathUtils.mulAndCheck(min, 1L));
        assertEquals(0L, MathUtils.mulAndCheck(max, 0L));
        assertEquals(0L, MathUtils.mulAndCheck(min, 0L));
        assertEquals(max, MathUtils.mulAndCheck(1L, max));
        assertEquals(min, MathUtils.mulAndCheck(1L, min));
        assertEquals(0L, MathUtils.mulAndCheck(0L, max));
        assertEquals(0L, MathUtils.mulAndCheck(0L, min));
        assertEquals(1L, MathUtils.mulAndCheck(-1L, -1L));
        assertEquals(min, MathUtils.mulAndCheck(min / 2, 2));
        testMulAndCheckLongFailure(max, 2L);
        testMulAndCheckLongFailure(2L, max);
        testMulAndCheckLongFailure(min, 2L);
        testMulAndCheckLongFailure(2L, min);
        testMulAndCheckLongFailure(min, -1L);
        testMulAndCheckLongFailure(-1L, min);
    }

// org.apache.commons.math.util.MathUtilsTest::testNextAfter
    public void testNextAfter() {
        
        assertEquals(16.0, FastMath.nextAfter(15.999999999999998, 34.27555555555555), 0.0);

        
        assertEquals(-15.999999999999996, FastMath.nextAfter(-15.999999999999998, 34.27555555555555), 0.0);

        
        assertEquals(15.999999999999996, FastMath.nextAfter(15.999999999999998, 2.142222222222222), 0.0);

        
        assertEquals(-15.999999999999996, FastMath.nextAfter(-15.999999999999998, 2.142222222222222), 0.0);

        
        assertEquals(8.000000000000002, FastMath.nextAfter(8.0, 34.27555555555555), 0.0);

        
        assertEquals(-7.999999999999999, FastMath.nextAfter(-8.0, 34.27555555555555), 0.0);

        
        assertEquals(7.999999999999999, FastMath.nextAfter(8.0, 2.142222222222222), 0.0);

        
        assertEquals(-7.999999999999999, FastMath.nextAfter(-8.0, 2.142222222222222), 0.0);

        
        assertEquals(2.308922399667661E-4, FastMath.nextAfter(2.3089223996676606E-4, 2.308922399667661E-4), 0.0);

        
        assertEquals(2.308922399667661E-4, FastMath.nextAfter(2.3089223996676606E-4, 2.3089223996676606E-4), 0.0);

        
        assertEquals(2.3089223996676603E-4, FastMath.nextAfter(2.3089223996676606E-4, 2.3089223996676603E-4), 0.0);

        
        assertEquals(2.3089223996676603E-4, FastMath.nextAfter(2.3089223996676606E-4, -2.308922399667661E-4), 0.0);

        
        assertEquals(2.3089223996676603E-4, FastMath.nextAfter(2.3089223996676606E-4, -2.3089223996676606E-4), 0.0);

        
        assertEquals(2.3089223996676603E-4, FastMath.nextAfter(2.3089223996676606E-4, -2.3089223996676603E-4), 0.0);

        
        assertEquals(-2.3089223996676603E-4, FastMath.nextAfter(-2.3089223996676606E-4, 2.308922399667661E-4), 0.0);

        
        assertEquals(-2.3089223996676603E-4, FastMath.nextAfter(-2.3089223996676606E-4, 2.3089223996676606E-4), 0.0);

        
        assertEquals(-2.3089223996676603E-4, FastMath.nextAfter(-2.3089223996676606E-4, 2.3089223996676603E-4), 0.0);

        
        assertEquals(-2.308922399667661E-4, FastMath.nextAfter(-2.3089223996676606E-4, -2.308922399667661E-4), 0.0);

        
        assertEquals(-2.308922399667661E-4, FastMath.nextAfter(-2.3089223996676606E-4, -2.3089223996676606E-4), 0.0);

        
        assertEquals(-2.3089223996676603E-4, FastMath.nextAfter(-2.3089223996676606E-4, -2.3089223996676603E-4), 0.0);

    }

// org.apache.commons.math.util.MathUtilsTest::testNextAfterSpecialCases
    public void testNextAfterSpecialCases() {
        assertTrue(Double.isInfinite(FastMath.nextAfter(Double.NEGATIVE_INFINITY, 0)));
        assertTrue(Double.isInfinite(FastMath.nextAfter(Double.POSITIVE_INFINITY, 0)));
        assertTrue(Double.isNaN(FastMath.nextAfter(Double.NaN, 0)));
        assertTrue(Double.isInfinite(FastMath.nextAfter(Double.MAX_VALUE, Double.POSITIVE_INFINITY)));
        assertTrue(Double.isInfinite(FastMath.nextAfter(-Double.MAX_VALUE, Double.NEGATIVE_INFINITY)));
        assertEquals(Double.MIN_VALUE, FastMath.nextAfter(0, 1), 0);
        assertEquals(-Double.MIN_VALUE, FastMath.nextAfter(0, -1), 0);
        assertEquals(0, FastMath.nextAfter(Double.MIN_VALUE, -1), 0);
        assertEquals(0, FastMath.nextAfter(-Double.MIN_VALUE, 1), 0);
    }

// org.apache.commons.math.util.MathUtilsTest::testScalb
    public void testScalb() {
        assertEquals( 0.0, MathUtils.scalb(0.0, 5), 1.0e-15);
        assertEquals(32.0, MathUtils.scalb(1.0, 5), 1.0e-15);
        assertEquals(1.0 / 32.0, MathUtils.scalb(1.0,  -5), 1.0e-15);
        assertEquals(FastMath.PI, MathUtils.scalb(FastMath.PI, 0), 1.0e-15);
        assertTrue(Double.isInfinite(MathUtils.scalb(Double.POSITIVE_INFINITY, 1)));
        assertTrue(Double.isInfinite(MathUtils.scalb(Double.NEGATIVE_INFINITY, 1)));
        assertTrue(Double.isNaN(MathUtils.scalb(Double.NaN, 1)));
    }

// org.apache.commons.math.util.MathUtilsTest::testNormalizeAngle
    public void testNormalizeAngle() {
        for (double a = -15.0; a <= 15.0; a += 0.1) {
            for (double b = -15.0; b <= 15.0; b += 0.2) {
                double c = MathUtils.normalizeAngle(a, b);
                assertTrue((b - FastMath.PI) <= c);
                assertTrue(c <= (b + FastMath.PI));
                double twoK = FastMath.rint((a - c) / FastMath.PI);
                assertEquals(c, a - twoK * FastMath.PI, 1.0e-14);
            }
        }
    }

// org.apache.commons.math.util.MathUtilsTest::testNormalizeArray
    public void testNormalizeArray() {
        double[] testValues1 = new double[] {1, 1, 2};
        TestUtils.assertEquals(
                new double[] {.25, .25, .5},
                MathUtils.normalizeArray(testValues1, 1),
                Double.MIN_VALUE);

        double[] testValues2 = new double[] {-1, -1, 1};
        TestUtils.assertEquals(
                new double[] {1, 1, -1},
                MathUtils.normalizeArray(testValues2, 1),
                Double.MIN_VALUE);

        
        double[] testValues3 = new double[] {-1, -1, Double.NaN, 1, Double.NaN};
        TestUtils.assertEquals(
                new double[] {1, 1,Double.NaN, -1, Double.NaN},
                MathUtils.normalizeArray(testValues3, 1),
                Double.MIN_VALUE);

        
        double[] zeroSum = new double[] {-1, 1};
        try {
            MathUtils.normalizeArray(zeroSum, 1);
            fail("expecting MathArithmeticException");
        } catch (MathArithmeticException ex) {}

        
        double[] hasInf = new double[] {1, 2, 1, Double.NEGATIVE_INFINITY};
        try {
            MathUtils.normalizeArray(hasInf, 1);
            fail("expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {}

        
        try {
            MathUtils.normalizeArray(testValues1, Double.POSITIVE_INFINITY);
            fail("expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {}

        
        try {
            MathUtils.normalizeArray(testValues1, Double.NaN);
            fail("expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {}

    }

// org.apache.commons.math.util.MathUtilsTest::testRoundDouble
    public void testRoundDouble() {
        double x = 1.234567890;
        assertEquals(1.23, MathUtils.round(x, 2), 0.0);
        assertEquals(1.235, MathUtils.round(x, 3), 0.0);
        assertEquals(1.2346, MathUtils.round(x, 4), 0.0);

        
        assertEquals(39.25, MathUtils.round(39.245, 2), 0.0);
        assertEquals(39.24, MathUtils.round(39.245, 2, BigDecimal.ROUND_DOWN), 0.0);
        double xx = 39.0;
        xx = xx + 245d / 1000d;
        assertEquals(39.25, MathUtils.round(xx, 2), 0.0);

        
        assertEquals(30.1d, MathUtils.round(30.095d, 2), 0.0d);
        assertEquals(30.1d, MathUtils.round(30.095d, 1), 0.0d);
        assertEquals(33.1d, MathUtils.round(33.095d, 1), 0.0d);
        assertEquals(33.1d, MathUtils.round(33.095d, 2), 0.0d);
        assertEquals(50.09d, MathUtils.round(50.085d, 2), 0.0d);
        assertEquals(50.19d, MathUtils.round(50.185d, 2), 0.0d);
        assertEquals(50.01d, MathUtils.round(50.005d, 2), 0.0d);
        assertEquals(30.01d, MathUtils.round(30.005d, 2), 0.0d);
        assertEquals(30.65d, MathUtils.round(30.645d, 2), 0.0d);

        assertEquals(1.24, MathUtils.round(x, 2, BigDecimal.ROUND_CEILING), 0.0);
        assertEquals(1.235, MathUtils.round(x, 3, BigDecimal.ROUND_CEILING), 0.0);
        assertEquals(1.2346, MathUtils.round(x, 4, BigDecimal.ROUND_CEILING), 0.0);
        assertEquals(-1.23, MathUtils.round(-x, 2, BigDecimal.ROUND_CEILING), 0.0);
        assertEquals(-1.234, MathUtils.round(-x, 3, BigDecimal.ROUND_CEILING), 0.0);
        assertEquals(-1.2345, MathUtils.round(-x, 4, BigDecimal.ROUND_CEILING), 0.0);

        assertEquals(1.23, MathUtils.round(x, 2, BigDecimal.ROUND_DOWN), 0.0);
        assertEquals(1.234, MathUtils.round(x, 3, BigDecimal.ROUND_DOWN), 0.0);
        assertEquals(1.2345, MathUtils.round(x, 4, BigDecimal.ROUND_DOWN), 0.0);
        assertEquals(-1.23, MathUtils.round(-x, 2, BigDecimal.ROUND_DOWN), 0.0);
        assertEquals(-1.234, MathUtils.round(-x, 3, BigDecimal.ROUND_DOWN), 0.0);
        assertEquals(-1.2345, MathUtils.round(-x, 4, BigDecimal.ROUND_DOWN), 0.0);

        assertEquals(1.23, MathUtils.round(x, 2, BigDecimal.ROUND_FLOOR), 0.0);
        assertEquals(1.234, MathUtils.round(x, 3, BigDecimal.ROUND_FLOOR), 0.0);
        assertEquals(1.2345, MathUtils.round(x, 4, BigDecimal.ROUND_FLOOR), 0.0);
        assertEquals(-1.24, MathUtils.round(-x, 2, BigDecimal.ROUND_FLOOR), 0.0);
        assertEquals(-1.235, MathUtils.round(-x, 3, BigDecimal.ROUND_FLOOR), 0.0);
        assertEquals(-1.2346, MathUtils.round(-x, 4, BigDecimal.ROUND_FLOOR), 0.0);

        assertEquals(1.23, MathUtils.round(x, 2, BigDecimal.ROUND_HALF_DOWN), 0.0);
        assertEquals(1.235, MathUtils.round(x, 3, BigDecimal.ROUND_HALF_DOWN), 0.0);
        assertEquals(1.2346, MathUtils.round(x, 4, BigDecimal.ROUND_HALF_DOWN), 0.0);
        assertEquals(-1.23, MathUtils.round(-x, 2, BigDecimal.ROUND_HALF_DOWN), 0.0);
        assertEquals(-1.235, MathUtils.round(-x, 3, BigDecimal.ROUND_HALF_DOWN), 0.0);
        assertEquals(-1.2346, MathUtils.round(-x, 4, BigDecimal.ROUND_HALF_DOWN), 0.0);
        assertEquals(1.234, MathUtils.round(1.2345, 3, BigDecimal.ROUND_HALF_DOWN), 0.0);
        assertEquals(-1.234, MathUtils.round(-1.2345, 3, BigDecimal.ROUND_HALF_DOWN), 0.0);

        assertEquals(1.23, MathUtils.round(x, 2, BigDecimal.ROUND_HALF_EVEN), 0.0);
        assertEquals(1.235, MathUtils.round(x, 3, BigDecimal.ROUND_HALF_EVEN), 0.0);
        assertEquals(1.2346, MathUtils.round(x, 4, BigDecimal.ROUND_HALF_EVEN), 0.0);
        assertEquals(-1.23, MathUtils.round(-x, 2, BigDecimal.ROUND_HALF_EVEN), 0.0);
        assertEquals(-1.235, MathUtils.round(-x, 3, BigDecimal.ROUND_HALF_EVEN), 0.0);
        assertEquals(-1.2346, MathUtils.round(-x, 4, BigDecimal.ROUND_HALF_EVEN), 0.0);
        assertEquals(1.234, MathUtils.round(1.2345, 3, BigDecimal.ROUND_HALF_EVEN), 0.0);
        assertEquals(-1.234, MathUtils.round(-1.2345, 3, BigDecimal.ROUND_HALF_EVEN), 0.0);
        assertEquals(1.236, MathUtils.round(1.2355, 3, BigDecimal.ROUND_HALF_EVEN), 0.0);
        assertEquals(-1.236, MathUtils.round(-1.2355, 3, BigDecimal.ROUND_HALF_EVEN), 0.0);

        assertEquals(1.23, MathUtils.round(x, 2, BigDecimal.ROUND_HALF_UP), 0.0);
        assertEquals(1.235, MathUtils.round(x, 3, BigDecimal.ROUND_HALF_UP), 0.0);
        assertEquals(1.2346, MathUtils.round(x, 4, BigDecimal.ROUND_HALF_UP), 0.0);
        assertEquals(-1.23, MathUtils.round(-x, 2, BigDecimal.ROUND_HALF_UP), 0.0);
        assertEquals(-1.235, MathUtils.round(-x, 3, BigDecimal.ROUND_HALF_UP), 0.0);
        assertEquals(-1.2346, MathUtils.round(-x, 4, BigDecimal.ROUND_HALF_UP), 0.0);
        assertEquals(1.235, MathUtils.round(1.2345, 3, BigDecimal.ROUND_HALF_UP), 0.0);
        assertEquals(-1.235, MathUtils.round(-1.2345, 3, BigDecimal.ROUND_HALF_UP), 0.0);

        assertEquals(-1.23, MathUtils.round(-1.23, 2, BigDecimal.ROUND_UNNECESSARY), 0.0);
        assertEquals(1.23, MathUtils.round(1.23, 2, BigDecimal.ROUND_UNNECESSARY), 0.0);

        try {
            MathUtils.round(1.234, 2, BigDecimal.ROUND_UNNECESSARY);
            fail();
        } catch (MathRuntimeException ex) { 
            
        }

        assertEquals(1.24, MathUtils.round(x, 2, BigDecimal.ROUND_UP), 0.0);
        assertEquals(1.235, MathUtils.round(x, 3, BigDecimal.ROUND_UP), 0.0);
        assertEquals(1.2346, MathUtils.round(x, 4, BigDecimal.ROUND_UP), 0.0);
        assertEquals(-1.24, MathUtils.round(-x, 2, BigDecimal.ROUND_UP), 0.0);
        assertEquals(-1.235, MathUtils.round(-x, 3, BigDecimal.ROUND_UP), 0.0);
        assertEquals(-1.2346, MathUtils.round(-x, 4, BigDecimal.ROUND_UP), 0.0);

        try {
            MathUtils.round(1.234, 2, 1923);
            fail();
        } catch (MathRuntimeException ex) { 
            
        }

        
        assertEquals(39.25, MathUtils.round(39.245, 2, BigDecimal.ROUND_HALF_UP), 0.0);

        
        TestUtils.assertEquals(Double.NaN, MathUtils.round(Double.NaN, 2), 0.0);
        assertEquals(0.0, MathUtils.round(0.0, 2), 0.0);
        assertEquals(Double.POSITIVE_INFINITY, MathUtils.round(Double.POSITIVE_INFINITY, 2), 0.0);
        assertEquals(Double.NEGATIVE_INFINITY, MathUtils.round(Double.NEGATIVE_INFINITY, 2), 0.0);
    }

// org.apache.commons.math.util.MathUtilsTest::testRoundFloat
    public void testRoundFloat() {
        float x = 1.234567890f;
        assertEquals(1.23f, MathUtils.round(x, 2), 0.0);
        assertEquals(1.235f, MathUtils.round(x, 3), 0.0);
        assertEquals(1.2346f, MathUtils.round(x, 4), 0.0);

        
        assertEquals(30.1f, MathUtils.round(30.095f, 2), 0.0f);
        assertEquals(30.1f, MathUtils.round(30.095f, 1), 0.0f);
        assertEquals(50.09f, MathUtils.round(50.085f, 2), 0.0f);
        assertEquals(50.19f, MathUtils.round(50.185f, 2), 0.0f);
        assertEquals(50.01f, MathUtils.round(50.005f, 2), 0.0f);
        assertEquals(30.01f, MathUtils.round(30.005f, 2), 0.0f);
        assertEquals(30.65f, MathUtils.round(30.645f, 2), 0.0f);

        assertEquals(1.24f, MathUtils.round(x, 2, BigDecimal.ROUND_CEILING), 0.0);
        assertEquals(1.235f, MathUtils.round(x, 3, BigDecimal.ROUND_CEILING), 0.0);
        assertEquals(1.2346f, MathUtils.round(x, 4, BigDecimal.ROUND_CEILING), 0.0);
        assertEquals(-1.23f, MathUtils.round(-x, 2, BigDecimal.ROUND_CEILING), 0.0);
        assertEquals(-1.234f, MathUtils.round(-x, 3, BigDecimal.ROUND_CEILING), 0.0);
        assertEquals(-1.2345f, MathUtils.round(-x, 4, BigDecimal.ROUND_CEILING), 0.0);

        assertEquals(1.23f, MathUtils.round(x, 2, BigDecimal.ROUND_DOWN), 0.0);
        assertEquals(1.234f, MathUtils.round(x, 3, BigDecimal.ROUND_DOWN), 0.0);
        assertEquals(1.2345f, MathUtils.round(x, 4, BigDecimal.ROUND_DOWN), 0.0);
        assertEquals(-1.23f, MathUtils.round(-x, 2, BigDecimal.ROUND_DOWN), 0.0);
        assertEquals(-1.234f, MathUtils.round(-x, 3, BigDecimal.ROUND_DOWN), 0.0);
        assertEquals(-1.2345f, MathUtils.round(-x, 4, BigDecimal.ROUND_DOWN), 0.0);

        assertEquals(1.23f, MathUtils.round(x, 2, BigDecimal.ROUND_FLOOR), 0.0);
        assertEquals(1.234f, MathUtils.round(x, 3, BigDecimal.ROUND_FLOOR), 0.0);
        assertEquals(1.2345f, MathUtils.round(x, 4, BigDecimal.ROUND_FLOOR), 0.0);
        assertEquals(-1.24f, MathUtils.round(-x, 2, BigDecimal.ROUND_FLOOR), 0.0);
        assertEquals(-1.235f, MathUtils.round(-x, 3, BigDecimal.ROUND_FLOOR), 0.0);
        assertEquals(-1.2346f, MathUtils.round(-x, 4, BigDecimal.ROUND_FLOOR), 0.0);

        assertEquals(1.23f, MathUtils.round(x, 2, BigDecimal.ROUND_HALF_DOWN), 0.0);
        assertEquals(1.235f, MathUtils.round(x, 3, BigDecimal.ROUND_HALF_DOWN), 0.0);
        assertEquals(1.2346f, MathUtils.round(x, 4, BigDecimal.ROUND_HALF_DOWN), 0.0);
        assertEquals(-1.23f, MathUtils.round(-x, 2, BigDecimal.ROUND_HALF_DOWN), 0.0);
        assertEquals(-1.235f, MathUtils.round(-x, 3, BigDecimal.ROUND_HALF_DOWN), 0.0);
        assertEquals(-1.2346f, MathUtils.round(-x, 4, BigDecimal.ROUND_HALF_DOWN), 0.0);
        assertEquals(1.234f, MathUtils.round(1.2345f, 3, BigDecimal.ROUND_HALF_DOWN), 0.0);
        assertEquals(-1.234f, MathUtils.round(-1.2345f, 3, BigDecimal.ROUND_HALF_DOWN), 0.0);

        assertEquals(1.23f, MathUtils.round(x, 2, BigDecimal.ROUND_HALF_EVEN), 0.0);
        assertEquals(1.235f, MathUtils.round(x, 3, BigDecimal.ROUND_HALF_EVEN), 0.0);
        assertEquals(1.2346f, MathUtils.round(x, 4, BigDecimal.ROUND_HALF_EVEN), 0.0);
        assertEquals(-1.23f, MathUtils.round(-x, 2, BigDecimal.ROUND_HALF_EVEN), 0.0);
        assertEquals(-1.235f, MathUtils.round(-x, 3, BigDecimal.ROUND_HALF_EVEN), 0.0);
        assertEquals(-1.2346f, MathUtils.round(-x, 4, BigDecimal.ROUND_HALF_EVEN), 0.0);
        assertEquals(1.234f, MathUtils.round(1.2345f, 3, BigDecimal.ROUND_HALF_EVEN), 0.0);
        assertEquals(-1.234f, MathUtils.round(-1.2345f, 3, BigDecimal.ROUND_HALF_EVEN), 0.0);
        assertEquals(1.236f, MathUtils.round(1.2355f, 3, BigDecimal.ROUND_HALF_EVEN), 0.0);
        assertEquals(-1.236f, MathUtils.round(-1.2355f, 3, BigDecimal.ROUND_HALF_EVEN), 0.0);

        assertEquals(1.23f, MathUtils.round(x, 2, BigDecimal.ROUND_HALF_UP), 0.0);
        assertEquals(1.235f, MathUtils.round(x, 3, BigDecimal.ROUND_HALF_UP), 0.0);
        assertEquals(1.2346f, MathUtils.round(x, 4, BigDecimal.ROUND_HALF_UP), 0.0);
        assertEquals(-1.23f, MathUtils.round(-x, 2, BigDecimal.ROUND_HALF_UP), 0.0);
        assertEquals(-1.235f, MathUtils.round(-x, 3, BigDecimal.ROUND_HALF_UP), 0.0);
        assertEquals(-1.2346f, MathUtils.round(-x, 4, BigDecimal.ROUND_HALF_UP), 0.0);
        assertEquals(1.235f, MathUtils.round(1.2345f, 3, BigDecimal.ROUND_HALF_UP), 0.0);
        assertEquals(-1.235f, MathUtils.round(-1.2345f, 3, BigDecimal.ROUND_HALF_UP), 0.0);

        assertEquals(-1.23f, MathUtils.round(-1.23f, 2, BigDecimal.ROUND_UNNECESSARY), 0.0);
        assertEquals(1.23f, MathUtils.round(1.23f, 2, BigDecimal.ROUND_UNNECESSARY), 0.0);

        try {
            MathUtils.round(1.234f, 2, BigDecimal.ROUND_UNNECESSARY);
            fail();
        } catch (MathArithmeticException ex) {
            
        }

        assertEquals(1.24f, MathUtils.round(x, 2, BigDecimal.ROUND_UP), 0.0);
        assertEquals(1.235f, MathUtils.round(x, 3, BigDecimal.ROUND_UP), 0.0);
        assertEquals(1.2346f, MathUtils.round(x, 4, BigDecimal.ROUND_UP), 0.0);
        assertEquals(-1.24f, MathUtils.round(-x, 2, BigDecimal.ROUND_UP), 0.0);
        assertEquals(-1.235f, MathUtils.round(-x, 3, BigDecimal.ROUND_UP), 0.0);
        assertEquals(-1.2346f, MathUtils.round(-x, 4, BigDecimal.ROUND_UP), 0.0);

        try {
            MathUtils.round(1.234f, 2, 1923);
            fail();
        } catch (MathIllegalArgumentException ex) {
            
        }

        
        TestUtils.assertEquals(Float.NaN, MathUtils.round(Float.NaN, 2), 0.0f);
        assertEquals(0.0f, MathUtils.round(0.0f, 2), 0.0f);
        assertEquals(Float.POSITIVE_INFINITY, MathUtils.round(Float.POSITIVE_INFINITY, 2), 0.0f);
        assertEquals(Float.NEGATIVE_INFINITY, MathUtils.round(Float.NEGATIVE_INFINITY, 2), 0.0f);
    }

// org.apache.commons.math.util.MathUtilsTest::testSignByte
    public void testSignByte() {
        assertEquals((byte) 1, MathUtils.sign((byte) 2));
        assertEquals((byte) 0, MathUtils.sign((byte) 0));
        assertEquals((byte) (-1), MathUtils.sign((byte) (-2)));
    }

// org.apache.commons.math.util.MathUtilsTest::testSignDouble
    public void testSignDouble() {
        double delta = 0.0;
        assertEquals(1.0, MathUtils.sign(2.0), delta);
        assertEquals(0.0, MathUtils.sign(0.0), delta);
        assertEquals(-1.0, MathUtils.sign(-2.0), delta);
        TestUtils.assertSame(-0. / 0., MathUtils.sign(Double.NaN));
    }

// org.apache.commons.math.util.MathUtilsTest::testSignFloat
    public void testSignFloat() {
        float delta = 0.0F;
        assertEquals(1.0F, MathUtils.sign(2.0F), delta);
        assertEquals(0.0F, MathUtils.sign(0.0F), delta);
        assertEquals(-1.0F, MathUtils.sign(-2.0F), delta);
        TestUtils.assertSame(Float.NaN, MathUtils.sign(Float.NaN));
    }

// org.apache.commons.math.util.MathUtilsTest::testSignInt
    public void testSignInt() {
        assertEquals(1, MathUtils.sign(2));
        assertEquals(0, MathUtils.sign(0));
        assertEquals((-1), MathUtils.sign((-2)));
    }

// org.apache.commons.math.util.MathUtilsTest::testSignLong
    public void testSignLong() {
        assertEquals(1L, MathUtils.sign(2L));
        assertEquals(0L, MathUtils.sign(0L));
        assertEquals(-1L, MathUtils.sign(-2L));
    }

// org.apache.commons.math.util.MathUtilsTest::testSignShort
    public void testSignShort() {
        assertEquals((short) 1, MathUtils.sign((short) 2));
        assertEquals((short) 0, MathUtils.sign((short) 0));
        assertEquals((short) (-1), MathUtils.sign((short) (-2)));
    }

// org.apache.commons.math.util.MathUtilsTest::testSinh
    public void testSinh() {
        double x = 3.0;
        double expected = 10.01787;
        assertEquals(expected, MathUtils.sinh(x), 1.0e-5);
    }

// org.apache.commons.math.util.MathUtilsTest::testSinhNaN
    public void testSinhNaN() {
        assertTrue(Double.isNaN(MathUtils.sinh(Double.NaN)));
    }

// org.apache.commons.math.util.MathUtilsTest::testSubAndCheck
    public void testSubAndCheck() {
        int big = Integer.MAX_VALUE;
        int bigNeg = Integer.MIN_VALUE;
        assertEquals(big, MathUtils.subAndCheck(big, 0));
        assertEquals(bigNeg + 1, MathUtils.subAndCheck(bigNeg, -1));
        assertEquals(-1, MathUtils.subAndCheck(bigNeg, -big));
        try {
            MathUtils.subAndCheck(big, -1);
            fail("Expecting MathArithmeticException");
        } catch (MathArithmeticException ex) {
        }
        try {
            MathUtils.subAndCheck(bigNeg, 1);
            fail("Expecting MathArithmeticException");
        } catch (MathArithmeticException ex) {
        }
    }

// org.apache.commons.math.util.MathUtilsTest::testSubAndCheckErrorMessage
    public void testSubAndCheckErrorMessage() {
        int big = Integer.MAX_VALUE;
        try {
            MathUtils.subAndCheck(big, -1);
            fail("Expecting MathArithmeticException");
        } catch (MathArithmeticException ex) {
            assertTrue(ex.getMessage().length() > 1);
        }
    }

// org.apache.commons.math.util.MathUtilsTest::testSubAndCheckLong
    public void testSubAndCheckLong() {
        long max = Long.MAX_VALUE;
        long min = Long.MIN_VALUE;
        assertEquals(max, MathUtils.subAndCheck(max, 0));
        assertEquals(min, MathUtils.subAndCheck(min, 0));
        assertEquals(-max, MathUtils.subAndCheck(0, max));
        assertEquals(min + 1, MathUtils.subAndCheck(min, -1));
        
        assertEquals(-1, MathUtils.subAndCheck(-max - 1, -max));
        assertEquals(max, MathUtils.subAndCheck(-1, -1 - max));
        testSubAndCheckLongFailure(0L, min);
        testSubAndCheckLongFailure(max, -1L);
        testSubAndCheckLongFailure(min, 1L);
    }

// org.apache.commons.math.util.MathUtilsTest::testPow
    public void testPow() {

        assertEquals(1801088541, MathUtils.pow(21, 7));
        assertEquals(1, MathUtils.pow(21, 0));
        try {
            MathUtils.pow(21, -7);
            fail("Expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException e) {
            
        }

        assertEquals(1801088541, MathUtils.pow(21, 7l));
        assertEquals(1, MathUtils.pow(21, 0l));
        try {
            MathUtils.pow(21, -7l);
            fail("Expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException e) {
            
        }

        assertEquals(1801088541l, MathUtils.pow(21l, 7));
        assertEquals(1l, MathUtils.pow(21l, 0));
        try {
            MathUtils.pow(21l, -7);
            fail("Expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException e) {
            
        }

        assertEquals(1801088541l, MathUtils.pow(21l, 7l));
        assertEquals(1l, MathUtils.pow(21l, 0l));
        try {
            MathUtils.pow(21l, -7l);
            fail("Expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException e) {
            
        }

        BigInteger twentyOne = BigInteger.valueOf(21l);
        assertEquals(BigInteger.valueOf(1801088541l), MathUtils.pow(twentyOne, 7));
        assertEquals(BigInteger.ONE, MathUtils.pow(twentyOne, 0));
        try {
            MathUtils.pow(twentyOne, -7);
            fail("Expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException e) {
            
        }

        assertEquals(BigInteger.valueOf(1801088541l), MathUtils.pow(twentyOne, 7l));
        assertEquals(BigInteger.ONE, MathUtils.pow(twentyOne, 0l));
        try {
            MathUtils.pow(twentyOne, -7l);
            fail("Expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException e) {
            
        }

        assertEquals(BigInteger.valueOf(1801088541l), MathUtils.pow(twentyOne, BigInteger.valueOf(7l)));
        assertEquals(BigInteger.ONE, MathUtils.pow(twentyOne, BigInteger.ZERO));
        try {
            MathUtils.pow(twentyOne, BigInteger.valueOf(-7l));
            fail("Expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException e) {
            
        }

        BigInteger bigOne =
            new BigInteger("1543786922199448028351389769265814882661837148" +
                           "4763915343722775611762713982220306372888519211" +
                           "560905579993523402015636025177602059044911261");
        assertEquals(bigOne, MathUtils.pow(twentyOne, 103));
        assertEquals(bigOne, MathUtils.pow(twentyOne, 103l));
        assertEquals(bigOne, MathUtils.pow(twentyOne, BigInteger.valueOf(103l)));

    }

// org.apache.commons.math.util.MathUtilsTest::testL1DistanceDouble
    public void testL1DistanceDouble() {
        double[] p1 = { 2.5,  0.0 };
        double[] p2 = { -0.5, 4.0 };
        assertEquals(7.0, MathUtils.distance1(p1, p2));
    }

// org.apache.commons.math.util.MathUtilsTest::testL1DistanceInt
    public void testL1DistanceInt() {
        int[] p1 = { 3, 0 };
        int[] p2 = { 0, 4 };
        assertEquals(7, MathUtils.distance1(p1, p2));
    }

// org.apache.commons.math.util.MathUtilsTest::testL2DistanceDouble
    public void testL2DistanceDouble() {
        double[] p1 = { 2.5,  0.0 };
        double[] p2 = { -0.5, 4.0 };
        assertEquals(5.0, MathUtils.distance(p1, p2));
    }

// org.apache.commons.math.util.MathUtilsTest::testL2DistanceInt
    public void testL2DistanceInt() {
        int[] p1 = { 3, 0 };
        int[] p2 = { 0, 4 };
        assertEquals(5.0, MathUtils.distance(p1, p2));
    }

// org.apache.commons.math.util.MathUtilsTest::testLInfDistanceDouble
    public void testLInfDistanceDouble() {
        double[] p1 = { 2.5,  0.0 };
        double[] p2 = { -0.5, 4.0 };
        assertEquals(4.0, MathUtils.distanceInf(p1, p2));
    }

// org.apache.commons.math.util.MathUtilsTest::testLInfDistanceInt
    public void testLInfDistanceInt() {
        int[] p1 = { 3, 0 };
        int[] p2 = { 0, 4 };
        assertEquals(4, MathUtils.distanceInf(p1, p2));
    }

// org.apache.commons.math.util.MathUtilsTest::testCheckOrder
    public void testCheckOrder() {
        MathUtils.checkOrder(new double[] {-15, -5.5, -1, 2, 15},
                             MathUtils.OrderDirection.INCREASING, true);
        MathUtils.checkOrder(new double[] {-15, -5.5, -1, 2, 2},
                             MathUtils.OrderDirection.INCREASING, false);
        MathUtils.checkOrder(new double[] {3, -5.5, -11, -27.5},
                             MathUtils.OrderDirection.DECREASING, true);
        MathUtils.checkOrder(new double[] {3, 0, 0, -5.5, -11, -27.5},
                             MathUtils.OrderDirection.DECREASING, false);

        try {
            MathUtils.checkOrder(new double[] {-15, -5.5, -1, -1, 2, 15},
                                 MathUtils.OrderDirection.INCREASING, true);
            fail("an exception should have been thrown");
        } catch (NonMonotonousSequenceException e) {
            
        }
        try {
            MathUtils.checkOrder(new double[] {-15, -5.5, -1, -2, 2},
                                 MathUtils.OrderDirection.INCREASING, false);
            fail("an exception should have been thrown");
        } catch (NonMonotonousSequenceException e) {
            
        }
        try {
            MathUtils.checkOrder(new double[] {3, 3, -5.5, -11, -27.5},
                                 MathUtils.OrderDirection.DECREASING, true);
            fail("an exception should have been thrown");
        } catch (NonMonotonousSequenceException e) {
            
        }
        try {
            MathUtils.checkOrder(new double[] {3, -1, 0, -5.5, -11, -27.5},
                                 MathUtils.OrderDirection.DECREASING, false);
            fail("an exception should have been thrown");
        } catch (NonMonotonousSequenceException e) {
            
        }
    }

// org.apache.commons.math.util.MathUtilsTest::testCheckFinite
    public void testCheckFinite() {
        try {
            MathUtils.checkFinite(Double.POSITIVE_INFINITY);
            fail("an exception should have been thrown");
        } catch (NotFiniteNumberException e) {
            
        }
        try {
            MathUtils.checkFinite(Double.NEGATIVE_INFINITY);
            fail("an exception should have been thrown");
        } catch (NotFiniteNumberException e) {
            
        }
        try {
            MathUtils.checkFinite(Double.NaN);
            fail("an exception should have been thrown");
        } catch (NotFiniteNumberException e) {
            
        }

        try {
            MathUtils.checkFinite(new double[] {0, -1, Double.POSITIVE_INFINITY, -2, 3});
            fail("an exception should have been thrown");
        } catch (NotFiniteNumberException e) {
            
        }
        try {
            MathUtils.checkFinite(new double[] {1, Double.NEGATIVE_INFINITY, -2, 3});
            fail("an exception should have been thrown");
        } catch (NotFiniteNumberException e) {
            
        }
        try {
            MathUtils.checkFinite(new double[] {4, 3, -1, Double.NaN, -2, 1});
            fail("an exception should have been thrown");
        } catch (NotFiniteNumberException e) {
            
        }
    }

// org.apache.commons.math.util.MathUtilsTest::testSortInPlace
    public void testSortInPlace() {
        final double[] x1 = {2,   5,  -3, 1,  4};
        final double[] x2 = {4,  25,   9, 1, 16};
        final double[] x3 = {8, 125, -27, 1, 64};
        
        MathUtils.sortInPlace(x1, x2, x3);

        assertEquals(-3,  x1[0], Math.ulp(1d));
        assertEquals(9,   x2[0], Math.ulp(1d));
        assertEquals(-27, x3[0], Math.ulp(1d));

        assertEquals(1, x1[1], Math.ulp(1d));
        assertEquals(1, x2[1], Math.ulp(1d));
        assertEquals(1, x3[1], Math.ulp(1d));

        assertEquals(2, x1[2], Math.ulp(1d));
        assertEquals(4, x2[2], Math.ulp(1d));
        assertEquals(8, x3[2], Math.ulp(1d));

        assertEquals(4,  x1[3], Math.ulp(1d));
        assertEquals(16, x2[3], Math.ulp(1d));
        assertEquals(64, x3[3], Math.ulp(1d));

        assertEquals(5,   x1[4], Math.ulp(1d));
        assertEquals(25,  x2[4], Math.ulp(1d));
        assertEquals(125, x3[4], Math.ulp(1d));
    }

// org.apache.commons.math.util.OpenIntToDoubleHashMapTest::testPutAndGetWith0ExpectedSize
    public void testPutAndGetWith0ExpectedSize() {
        OpenIntToDoubleHashMap map = new OpenIntToDoubleHashMap(0);
        assertPutAndGet(map);
    }

// org.apache.commons.math.util.OpenIntToDoubleHashMapTest::testPutAndGetWithExpectedSize
    public void testPutAndGetWithExpectedSize() {
        OpenIntToDoubleHashMap map = new OpenIntToDoubleHashMap(500);
        assertPutAndGet(map);
    }

// org.apache.commons.math.util.OpenIntToDoubleHashMapTest::testPutAndGet
    public void testPutAndGet() {
        OpenIntToDoubleHashMap map = new OpenIntToDoubleHashMap();
        assertPutAndGet(map);
    }

// org.apache.commons.math.util.OpenIntToDoubleHashMapTest::testPutAbsentOnExisting
    public void testPutAbsentOnExisting() {
        OpenIntToDoubleHashMap map = createFromJavaMap();
        int size = javaMap.size();
        for (Map.Entry<Integer, Double> mapEntry : generateAbsent().entrySet()) {
            map.put(mapEntry.getKey(), mapEntry.getValue());
            assertEquals(++size, map.size());
            assertEquals(mapEntry.getValue(), map.get(mapEntry.getKey()));
        }
    }

// org.apache.commons.math.util.OpenIntToDoubleHashMapTest::testPutOnExisting
    public void testPutOnExisting() {
        OpenIntToDoubleHashMap map = createFromJavaMap();
        for (Map.Entry<Integer, Double> mapEntry : javaMap.entrySet()) {
            map.put(mapEntry.getKey(), mapEntry.getValue());
            assertEquals(javaMap.size(), map.size());
            assertEquals(mapEntry.getValue(), map.get(mapEntry.getKey()));
        }
    }

// org.apache.commons.math.util.OpenIntToDoubleHashMapTest::testGetAbsent
    public void testGetAbsent() {
        Map<Integer, Double> generated = generateAbsent();
        OpenIntToDoubleHashMap map = createFromJavaMap();

        for (Map.Entry<Integer, Double> mapEntry : generated.entrySet())
            assertTrue(Double.isNaN(map.get(mapEntry.getKey())));
    }

// org.apache.commons.math.util.OpenIntToDoubleHashMapTest::testGetFromEmpty
    public void testGetFromEmpty() {
        OpenIntToDoubleHashMap map = new OpenIntToDoubleHashMap();
        assertTrue(Double.isNaN(map.get(5)));
        assertTrue(Double.isNaN(map.get(0)));
        assertTrue(Double.isNaN(map.get(50)));
    }

// org.apache.commons.math.util.OpenIntToDoubleHashMapTest::testRemove
    public void testRemove() {
        OpenIntToDoubleHashMap map = createFromJavaMap();
        int mapSize = javaMap.size();
        assertEquals(mapSize, map.size());
        for (Map.Entry<Integer, Double> mapEntry : javaMap.entrySet()) {
            map.remove(mapEntry.getKey());
            assertEquals(--mapSize, map.size());
            assertTrue(Double.isNaN(map.get(mapEntry.getKey())));
        }

        
        assertPutAndGet(map);
    }

// org.apache.commons.math.util.OpenIntToDoubleHashMapTest::testRemove2
    public void testRemove2() {
        OpenIntToDoubleHashMap map = createFromJavaMap();
        int mapSize = javaMap.size();
        int count = 0;
        Set<Integer> keysInMap = new HashSet<Integer>(javaMap.keySet());
        for (Map.Entry<Integer, Double> mapEntry : javaMap.entrySet()) {
            keysInMap.remove(mapEntry.getKey());
            map.remove(mapEntry.getKey());
            assertEquals(--mapSize, map.size());
            assertTrue(Double.isNaN(map.get(mapEntry.getKey())));
            if (count++ > 5)
                break;
        }

        
        assertPutAndGet(map, mapSize, keysInMap);
    }

// org.apache.commons.math.util.OpenIntToDoubleHashMapTest::testRemoveFromEmpty
    public void testRemoveFromEmpty() {
        OpenIntToDoubleHashMap map = new OpenIntToDoubleHashMap();
        assertTrue(Double.isNaN(map.remove(50)));
    }

// org.apache.commons.math.util.OpenIntToDoubleHashMapTest::testRemoveAbsent
    public void testRemoveAbsent() {
        Map<Integer, Double> generated = generateAbsent();

        OpenIntToDoubleHashMap map = createFromJavaMap();
        int mapSize = map.size();

        for (Map.Entry<Integer, Double> mapEntry : generated.entrySet()) {
            map.remove(mapEntry.getKey());
            assertEquals(mapSize, map.size());
            assertTrue(Double.isNaN(map.get(mapEntry.getKey())));
        }
    }

// org.apache.commons.math.util.OpenIntToDoubleHashMapTest::testCopy
    public void testCopy() {
        OpenIntToDoubleHashMap copy =
            new OpenIntToDoubleHashMap(createFromJavaMap());
        assertEquals(javaMap.size(), copy.size());

        for (Map.Entry<Integer, Double> mapEntry : javaMap.entrySet())
            assertEquals(mapEntry.getValue(), copy.get(mapEntry.getKey()));
    }

// org.apache.commons.math.util.OpenIntToDoubleHashMapTest::testContainsKey
    public void testContainsKey() {
        OpenIntToDoubleHashMap map = createFromJavaMap();
        for (Map.Entry<Integer, Double> mapEntry : javaMap.entrySet()) {
            assertTrue(map.containsKey(mapEntry.getKey()));
        }
        for (Map.Entry<Integer, Double> mapEntry : generateAbsent().entrySet()) {
            assertFalse(map.containsKey(mapEntry.getKey()));
        }
        for (Map.Entry<Integer, Double> mapEntry : javaMap.entrySet()) {
            int key = mapEntry.getKey();
            assertTrue(map.containsKey(key));
            map.remove(key);
            assertFalse(map.containsKey(key));
        }
    }

// org.apache.commons.math.util.OpenIntToDoubleHashMapTest::testIterator
    public void testIterator() {
        OpenIntToDoubleHashMap map = createFromJavaMap();
        OpenIntToDoubleHashMap.Iterator iterator = map.iterator();
        for (int i = 0; i < map.size(); ++i) {
            assertTrue(iterator.hasNext());
            iterator.advance();
            int key = iterator.key();
            assertTrue(map.containsKey(key));
            assertEquals(javaMap.get(key), map.get(key), 0);
            assertEquals(javaMap.get(key), iterator.value(), 0);
            assertTrue(javaMap.containsKey(key));
        }
        assertFalse(iterator.hasNext());
        try {
            iterator.advance();
            fail("an exception should have been thrown");
        } catch (NoSuchElementException nsee) {
            
        }
    }

// org.apache.commons.math.util.OpenIntToDoubleHashMapTest::testConcurrentModification
    public void testConcurrentModification() {
        OpenIntToDoubleHashMap map = createFromJavaMap();
        OpenIntToDoubleHashMap.Iterator iterator = map.iterator();
        map.put(3, 3);
        try {
            iterator.advance();
            fail("an exception should have been thrown");
        } catch (ConcurrentModificationException cme) {
            
        }
    }

// org.apache.commons.math.util.OpenIntToDoubleHashMapTest::testPutKeysWithCollisions
    public void testPutKeysWithCollisions() {
        OpenIntToDoubleHashMap map = new OpenIntToDoubleHashMap();
        int key1 = -1996012590;
        double value1 = 1.0;
        map.put(key1, value1);
        int key2 = 835099822;
        map.put(key2, value1);
        int key3 = 1008859686;
        map.put(key3, value1);
        assertEquals(value1, map.get(key3));
        assertEquals(3, map.size());

        map.remove(key2);
        double value2 = 2.0;
        map.put(key3, value2);
        assertEquals(value2, map.get(key3));
        assertEquals(2, map.size());
    }

// org.apache.commons.math.util.OpenIntToDoubleHashMapTest::testPutKeysWithCollision2
    public void testPutKeysWithCollision2() {
        OpenIntToDoubleHashMap map = new OpenIntToDoubleHashMap();
        int key1 = 837989881;
        double value1 = 1.0;
        map.put(key1, value1);
        int key2 = 476463321;
        map.put(key2, value1);
        assertEquals(2, map.size());
        assertEquals(value1, map.get(key2));

        map.remove(key1);
        double value2 = 2.0;
        map.put(key2, value2);
        assertEquals(1, map.size());
        assertEquals(value2, map.get(key2));
    }

// org.apache.commons.math.util.OpenIntToFieldTest::testPutAndGetWith0ExpectedSize
    public void testPutAndGetWith0ExpectedSize() {
        OpenIntToFieldHashMap<Fraction> map = new OpenIntToFieldHashMap<Fraction>(field,0);
        assertPutAndGet(map);
    }

// org.apache.commons.math.util.OpenIntToFieldTest::testPutAndGetWithExpectedSize
    public void testPutAndGetWithExpectedSize() {
        OpenIntToFieldHashMap<Fraction> map = new OpenIntToFieldHashMap<Fraction>(field,500);
        assertPutAndGet(map);
    }

// org.apache.commons.math.util.OpenIntToFieldTest::testPutAndGet
    public void testPutAndGet() {
        OpenIntToFieldHashMap<Fraction> map = new OpenIntToFieldHashMap<Fraction>(field);
        assertPutAndGet(map);
    }

// org.apache.commons.math.util.OpenIntToFieldTest::testPutAbsentOnExisting
    public void testPutAbsentOnExisting() {
        OpenIntToFieldHashMap<Fraction> map = createFromJavaMap(field);
        int size = javaMap.size();
        for (Map.Entry<Integer, Fraction> mapEntry : generateAbsent().entrySet()) {
            map.put(mapEntry.getKey(), mapEntry.getValue());
            assertEquals(++size, map.size());
            assertEquals(mapEntry.getValue(), map.get(mapEntry.getKey()));
        }
    }

// org.apache.commons.math.util.OpenIntToFieldTest::testPutOnExisting
    public void testPutOnExisting() {
        OpenIntToFieldHashMap<Fraction> map = createFromJavaMap(field);
        for (Map.Entry<Integer, Fraction> mapEntry : javaMap.entrySet()) {
            map.put(mapEntry.getKey(), mapEntry.getValue());
            assertEquals(javaMap.size(), map.size());
            assertEquals(mapEntry.getValue(), map.get(mapEntry.getKey()));
        }
    }

// org.apache.commons.math.util.OpenIntToFieldTest::testGetAbsent
    public void testGetAbsent() {
        Map<Integer, Fraction> generated = generateAbsent();
        OpenIntToFieldHashMap<Fraction> map = createFromJavaMap(field);

        for (Map.Entry<Integer, Fraction> mapEntry : generated.entrySet())
            assertTrue(field.getZero().equals(map.get(mapEntry.getKey())));
    }

// org.apache.commons.math.util.OpenIntToFieldTest::testGetFromEmpty
    public void testGetFromEmpty() {
        OpenIntToFieldHashMap<Fraction> map = new OpenIntToFieldHashMap<Fraction>(field);
        assertTrue(field.getZero().equals(map.get(5)));
        assertTrue(field.getZero().equals(map.get(0)));
        assertTrue(field.getZero().equals(map.get(50)));
    }

// org.apache.commons.math.util.OpenIntToFieldTest::testRemove
    public void testRemove() {
        OpenIntToFieldHashMap<Fraction> map = createFromJavaMap(field);
        int mapSize = javaMap.size();
        assertEquals(mapSize, map.size());
        for (Map.Entry<Integer, Fraction> mapEntry : javaMap.entrySet()) {
            map.remove(mapEntry.getKey());
            assertEquals(--mapSize, map.size());
            assertTrue(field.getZero().equals(map.get(mapEntry.getKey())));
        }

        
        assertPutAndGet(map);
    }

// org.apache.commons.math.util.OpenIntToFieldTest::testRemove2
    public void testRemove2() {
        OpenIntToFieldHashMap<Fraction> map = createFromJavaMap(field);
        int mapSize = javaMap.size();
        int count = 0;
        Set<Integer> keysInMap = new HashSet<Integer>(javaMap.keySet());
        for (Map.Entry<Integer, Fraction> mapEntry : javaMap.entrySet()) {
            keysInMap.remove(mapEntry.getKey());
            map.remove(mapEntry.getKey());
            assertEquals(--mapSize, map.size());
            assertTrue(field.getZero().equals(map.get(mapEntry.getKey())));
            if (count++ > 5)
                break;
        }

        
        assertPutAndGet(map, mapSize, keysInMap);
    }

// org.apache.commons.math.util.OpenIntToFieldTest::testRemoveFromEmpty
    public void testRemoveFromEmpty() {
        OpenIntToFieldHashMap<Fraction> map = new OpenIntToFieldHashMap<Fraction>(field);
        assertTrue(field.getZero().equals(map.remove(50)));
    }

// org.apache.commons.math.util.OpenIntToFieldTest::testRemoveAbsent
    public void testRemoveAbsent() {
        Map<Integer, Fraction> generated = generateAbsent();

        OpenIntToFieldHashMap<Fraction> map = createFromJavaMap(field);
        int mapSize = map.size();

        for (Map.Entry<Integer, Fraction> mapEntry : generated.entrySet()) {
            map.remove(mapEntry.getKey());
            assertEquals(mapSize, map.size());
            assertTrue(field.getZero().equals(map.get(mapEntry.getKey())));
        }
    }

// org.apache.commons.math.util.OpenIntToFieldTest::testCopy
    public void testCopy() {
        OpenIntToFieldHashMap<Fraction> copy =
            new OpenIntToFieldHashMap<Fraction>(createFromJavaMap(field));
        assertEquals(javaMap.size(), copy.size());

        for (Map.Entry<Integer, Fraction> mapEntry : javaMap.entrySet())
            assertEquals(mapEntry.getValue(), copy.get(mapEntry.getKey()));
    }

// org.apache.commons.math.util.OpenIntToFieldTest::testContainsKey
    public void testContainsKey() {
        OpenIntToFieldHashMap<Fraction> map = createFromJavaMap(field);
        for (Entry<Integer, Fraction> mapEntry : javaMap.entrySet()) {
            assertTrue(map.containsKey(mapEntry.getKey()));
        }
        for (Map.Entry<Integer, Fraction> mapEntry : generateAbsent().entrySet()) {
            assertFalse(map.containsKey(mapEntry.getKey()));
        }
        for (Entry<Integer, Fraction> mapEntry : javaMap.entrySet()) {
            int key = mapEntry.getKey();
            assertTrue(map.containsKey(key));
            map.remove(key);
            assertFalse(map.containsKey(key));
        }
    }

// org.apache.commons.math.util.OpenIntToFieldTest::testIterator
    public void testIterator() {
        OpenIntToFieldHashMap<Fraction> map = createFromJavaMap(field);
        OpenIntToFieldHashMap<Fraction>.Iterator iterator = map.iterator();
        for (int i = 0; i < map.size(); ++i) {
            assertTrue(iterator.hasNext());
            iterator.advance();
            int key = iterator.key();
            assertTrue(map.containsKey(key));
            assertEquals(javaMap.get(key), map.get(key));
            assertEquals(javaMap.get(key), iterator.value());
            assertTrue(javaMap.containsKey(key));
        }
        assertFalse(iterator.hasNext());
        try {
            iterator.advance();
            fail("an exception should have been thrown");
        } catch (NoSuchElementException nsee) {
            
        }
    }

// org.apache.commons.math.util.OpenIntToFieldTest::testConcurrentModification
    public void testConcurrentModification() {
        OpenIntToFieldHashMap<Fraction> map = createFromJavaMap(field);
        OpenIntToFieldHashMap<Fraction>.Iterator iterator = map.iterator();
        map.put(3, new Fraction(3));
        try {
            iterator.advance();
            fail("an exception should have been thrown");
        } catch (ConcurrentModificationException cme) {
            
        }
    }

// org.apache.commons.math.util.OpenIntToFieldTest::testPutKeysWithCollisions
    public void testPutKeysWithCollisions() {
        OpenIntToFieldHashMap<Fraction> map = new OpenIntToFieldHashMap<Fraction>(field);
        int key1 = -1996012590;
        Fraction value1 = new Fraction(1);
        map.put(key1, value1);
        int key2 = 835099822;
        map.put(key2, value1);
        int key3 = 1008859686;
        map.put(key3, value1);
        assertEquals(value1, map.get(key3));
        assertEquals(3, map.size());

        map.remove(key2);
        Fraction value2 = new Fraction(2);
        map.put(key3, value2);
        assertEquals(value2, map.get(key3));
        assertEquals(2, map.size());
    }

// org.apache.commons.math.util.OpenIntToFieldTest::testPutKeysWithCollision2
    public void testPutKeysWithCollision2() {
        OpenIntToFieldHashMap<Fraction>map = new OpenIntToFieldHashMap<Fraction>(field);
        int key1 = 837989881;
        Fraction value1 = new Fraction(1);
        map.put(key1, value1);
        int key2 = 476463321;
        map.put(key2, value1);
        assertEquals(2, map.size());
        assertEquals(value1, map.get(key2));

        map.remove(key1);
        Fraction value2 = new Fraction(2);
        map.put(key2, value2);
        assertEquals(1, map.size());
        assertEquals(value2, map.get(key2));
    }

// org.apache.commons.math.util.ResizableDoubleArrayTest::testConstructors
    public void testConstructors() {
        float defaultExpansionFactor = 2.0f;
        float defaultContractionCriteria = 2.5f;
        int defaultMode = ResizableDoubleArray.MULTIPLICATIVE_MODE;

        ResizableDoubleArray testDa = new ResizableDoubleArray(2);
        assertEquals(0, testDa.getNumElements());
        assertEquals(2, testDa.getInternalLength());
        assertEquals(defaultExpansionFactor, testDa.getExpansionFactor(), 0);
        assertEquals(defaultContractionCriteria, testDa.getContractionCriteria(), 0);
        assertEquals(defaultMode, testDa.getExpansionMode());
        try {
            da = new ResizableDoubleArray(-1);
            fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }
        
        testDa = new ResizableDoubleArray((double[]) null);
        assertEquals(0, testDa.getNumElements());
        
        double[] initialArray = new double[] { 0, 1, 2 };        
        testDa = new ResizableDoubleArray(initialArray);
        assertEquals(3, testDa.getNumElements());

        testDa = new ResizableDoubleArray(2, 2.0f);
        assertEquals(0, testDa.getNumElements());
        assertEquals(2, testDa.getInternalLength());
        assertEquals(defaultExpansionFactor, testDa.getExpansionFactor(), 0);
        assertEquals(defaultContractionCriteria, testDa.getContractionCriteria(), 0);
        assertEquals(defaultMode, testDa.getExpansionMode());

        try {
            da = new ResizableDoubleArray(2, 0.5f);
            fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }

        testDa = new ResizableDoubleArray(2, 3.0f);
        assertEquals(3.0f, testDa.getExpansionFactor(), 0);
        assertEquals(3.5f, testDa.getContractionCriteria(), 0);

        testDa = new ResizableDoubleArray(2, 2.0f, 3.0f);
        assertEquals(0, testDa.getNumElements());
        assertEquals(2, testDa.getInternalLength());
        assertEquals(defaultExpansionFactor, testDa.getExpansionFactor(), 0);
        assertEquals(3.0f, testDa.getContractionCriteria(), 0);
        assertEquals(defaultMode, testDa.getExpansionMode());

        try {
            da = new ResizableDoubleArray(2, 2.0f, 1.5f);
            fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }

        testDa = new ResizableDoubleArray(2, 2.0f, 3.0f,
                ResizableDoubleArray.ADDITIVE_MODE);
        assertEquals(0, testDa.getNumElements());
        assertEquals(2, testDa.getInternalLength());
        assertEquals(defaultExpansionFactor, testDa.getExpansionFactor(), 0);
        assertEquals(3.0f, testDa.getContractionCriteria(), 0);
        assertEquals(ResizableDoubleArray.ADDITIVE_MODE,
                testDa.getExpansionMode());

        try {
            da = new ResizableDoubleArray(2, 2.0f, 2.5f, -1);
            fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }

        
        testDa = new ResizableDoubleArray(2, 2.0f, 3.0f,
                ResizableDoubleArray.ADDITIVE_MODE);
        testDa.addElement(2.0);
        testDa.addElement(3.2);
        ResizableDoubleArray copyDa = new ResizableDoubleArray(testDa);
        assertEquals(copyDa, testDa);
        assertEquals(testDa, copyDa);
    }

// org.apache.commons.math.util.ResizableDoubleArrayTest::testSetElementArbitraryExpansion
    public void testSetElementArbitraryExpansion() {

        
        da.addElement(2.0);
        da.addElement(4.0);
        da.addElement(6.0);
        da.setElement(1, 3.0);

        
        da.setElement(1000, 3.4);

        assertEquals( "The number of elements should now be 1001, it isn't",
                da.getNumElements(), 1001);

        assertEquals( "Uninitialized Elements are default value of 0.0, index 766 wasn't", 0.0,
                da.getElement( 760 ), Double.MIN_VALUE );

        assertEquals( "The 1000th index should be 3.4, it isn't", 3.4, da.getElement(1000),
                Double.MIN_VALUE );
        assertEquals( "The 0th index should be 2.0, it isn't", 2.0, da.getElement(0),
                Double.MIN_VALUE);

        
        da.clear();
        da.addElement(2.0);
        da.addElement(4.0);
        da.addElement(6.0);
        assertEquals(4, ((ResizableDoubleArray) da).getInternalLength());
        assertEquals(3, da.getNumElements());
        da.setElement(3, 7.0);
        assertEquals(4, ((ResizableDoubleArray) da).getInternalLength());
        assertEquals(4, da.getNumElements());
        da.setElement(10, 10.0);
        assertEquals(11, ((ResizableDoubleArray) da).getInternalLength());
        assertEquals(11, da.getNumElements());
        da.setElement(9, 10.0);
        assertEquals(11, ((ResizableDoubleArray) da).getInternalLength());
        assertEquals(11, da.getNumElements());

        try {
            da.setElement(-2, 3);
            fail("Expecting ArrayIndexOutOfBoundsException for negative index");
        } catch (ArrayIndexOutOfBoundsException ex) {
            
        }

        

        ResizableDoubleArray testDa = new ResizableDoubleArray(2, 2.0f, 3.0f,
                ResizableDoubleArray.ADDITIVE_MODE);
        assertEquals(2, testDa.getInternalLength());
        testDa.addElement(1d);
        testDa.addElement(1d);
        assertEquals(2, testDa.getInternalLength());
        testDa.addElement(1d);
        assertEquals(4, testDa.getInternalLength());
    }

// org.apache.commons.math.util.ResizableDoubleArrayTest::testAdd1000
    public void testAdd1000() {
        super.testAdd1000();
        assertEquals("Internal Storage length should be 1024 if we started out with initial capacity of " +
                "16 and an expansion factor of 2.0",
                1024, ((ResizableDoubleArray) da).getInternalLength());
    }

// org.apache.commons.math.util.ResizableDoubleArrayTest::testAddElements
    public void testAddElements() {
        ResizableDoubleArray testDa = new ResizableDoubleArray();
        
        
        testDa.addElements(new double[] {4, 5, 6});
        assertEquals(3, testDa.getNumElements(), 0);
        assertEquals(4, testDa.getElement(0), 0);
        assertEquals(5, testDa.getElement(1), 0);
        assertEquals(6, testDa.getElement(2), 0);
        
        testDa.addElements(new double[] {4, 5, 6});
        assertEquals(6, testDa.getNumElements());

        
        testDa = new ResizableDoubleArray(2, 2.0f, 2.5f,
                ResizableDoubleArray.ADDITIVE_MODE);        
        assertEquals(2, testDa.getInternalLength());
        testDa.addElements(new double[] { 1d }); 
        testDa.addElements(new double[] { 2d }); 
        testDa.addElements(new double[] { 3d }); 
        assertEquals(1d, testDa.getElement(0), 0);
        assertEquals(2d, testDa.getElement(1), 0);
        assertEquals(3d, testDa.getElement(2), 0);
        assertEquals(4, testDa.getInternalLength());  
        assertEquals(3, testDa.getNumElements());
    }

// org.apache.commons.math.util.ResizableDoubleArrayTest::testAddElementRolling
    public void testAddElementRolling() {
        super.testAddElementRolling();

        
        da.clear();
        da.addElement(1);
        da.addElement(2);
        da.addElementRolling(3);
        assertEquals(3, da.getElement(1), 0);
        da.addElementRolling(4);
        assertEquals(3, da.getElement(0), 0);
        assertEquals(4, da.getElement(1), 0);
        da.addElement(5);
        assertEquals(5, da.getElement(2), 0);
        da.addElementRolling(6);
        assertEquals(4, da.getElement(0), 0);
        assertEquals(5, da.getElement(1), 0);
        assertEquals(6, da.getElement(2), 0);

        
        ResizableDoubleArray testDa = new ResizableDoubleArray(2, 2.0f, 2.5f,
                ResizableDoubleArray.ADDITIVE_MODE);
        assertEquals(2, testDa.getInternalLength());
        testDa.addElement(1d); 
        testDa.addElement(2d); 
        testDa.addElement(3d); 
        assertEquals(1d, testDa.getElement(0), 0);
        assertEquals(2d, testDa.getElement(1), 0);
        assertEquals(3d, testDa.getElement(2), 0);
        assertEquals(4, testDa.getInternalLength());  
        assertEquals(3, testDa.getNumElements());
        testDa.addElementRolling(4d);
        assertEquals(2d, testDa.getElement(0), 0);
        assertEquals(3d, testDa.getElement(1), 0);
        assertEquals(4d, testDa.getElement(2), 0);
        assertEquals(4, testDa.getInternalLength());  
        assertEquals(3, testDa.getNumElements());
        testDa.addElementRolling(5d);   
        assertEquals(3d, testDa.getElement(0), 0);
        assertEquals(4d, testDa.getElement(1), 0);
        assertEquals(5d, testDa.getElement(2), 0);
        assertEquals(4, testDa.getInternalLength());  
        assertEquals(3, testDa.getNumElements());
        try {
            testDa.getElement(4);
            fail("Expecting ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException ex) {
            
        }
        try {
            testDa.getElement(-1);
            fail("Expecting ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException ex) {
            
        }
    }

// org.apache.commons.math.util.ResizableDoubleArrayTest::testSetNumberOfElements
    public void testSetNumberOfElements() {
        da.addElement( 1.0 );
        da.addElement( 1.0 );
        da.addElement( 1.0 );
        da.addElement( 1.0 );
        da.addElement( 1.0 );
        da.addElement( 1.0 );
        assertEquals( "Number of elements should equal 6", da.getNumElements(), 6);

        ((ResizableDoubleArray) da).setNumElements( 3 );
        assertEquals( "Number of elements should equal 3", da.getNumElements(), 3);

        try {
            ((ResizableDoubleArray) da).setNumElements( -3 );
            fail( "Setting number of elements to negative should've thrown an exception");
        } catch( IllegalArgumentException iae ) {
        }

        ((ResizableDoubleArray) da).setNumElements(1024);
        assertEquals( "Number of elements should now be 1024", da.getNumElements(), 1024);
        assertEquals( "Element 453 should be a default double", da.getElement( 453 ), 0.0, Double.MIN_VALUE);

    }

// org.apache.commons.math.util.ResizableDoubleArrayTest::testWithInitialCapacity
    public void testWithInitialCapacity() {

        ResizableDoubleArray eDA2 = new ResizableDoubleArray(2);
        assertEquals("Initial number of elements should be 0", 0, eDA2.getNumElements());

        RandomData randomData = new RandomDataImpl();
        int iterations = randomData.nextInt(100, 1000);

        for( int i = 0; i < iterations; i++) {
            eDA2.addElement( i );
        }

        assertEquals("Number of elements should be equal to " + iterations, iterations, eDA2.getNumElements());

        eDA2.addElement( 2.0 );

        assertEquals("Number of elements should be equals to " + (iterations +1),
                iterations + 1 , eDA2.getNumElements() );
    }

// org.apache.commons.math.util.ResizableDoubleArrayTest::testWithInitialCapacityAndExpansionFactor
    public void testWithInitialCapacityAndExpansionFactor() {

        ResizableDoubleArray eDA3 = new ResizableDoubleArray(3, 3.0f, 3.5f);
        assertEquals("Initial number of elements should be 0", 0, eDA3.getNumElements() );

        RandomData randomData = new RandomDataImpl();
        int iterations = randomData.nextInt(100, 3000);

        for( int i = 0; i < iterations; i++) {
            eDA3.addElement( i );
        }

        assertEquals("Number of elements should be equal to " + iterations, iterations,eDA3.getNumElements());

        eDA3.addElement( 2.0 );

        assertEquals("Number of elements should be equals to " + (iterations +1),
                iterations +1, eDA3.getNumElements() );

        assertEquals("Expansion factor should equal 3.0", 3.0f, eDA3.getExpansionFactor(), Double.MIN_VALUE);
    }

// org.apache.commons.math.util.ResizableDoubleArrayTest::testDiscard
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
        assertEquals( "Number of elements should be 11", 11, da.getNumElements());

        ((ResizableDoubleArray)da).discardFrontElements(5);
        assertEquals( "Number of elements should be 6", 6, da.getNumElements());

        da.addElement(2.0);
        da.addElement(2.0);
        da.addElement(2.0);
        da.addElement(2.0);
        assertEquals( "Number of elements should be 10", 10, da.getNumElements());

        ((ResizableDoubleArray)da).discardMostRecentElements(2);
        assertEquals( "Number of elements should be 8", 8, da.getNumElements());

        try {
            ((ResizableDoubleArray)da).discardFrontElements(-1);
            fail( "Trying to discard a negative number of element is not allowed");
        } catch( Exception e ){
        }

        try {
            ((ResizableDoubleArray)da).discardMostRecentElements(-1);
            fail( "Trying to discard a negative number of element is not allowed");
        } catch( Exception e ){
        }

        try {
            ((ResizableDoubleArray)da).discardFrontElements( 10000 );
            fail( "You can't discard more elements than the array contains");
        } catch( Exception e ){
        }

        try {
            ((ResizableDoubleArray)da).discardMostRecentElements( 10000 );
            fail( "You can't discard more elements than the array contains");
        } catch( Exception e ){
        }

    }

// org.apache.commons.math.util.ResizableDoubleArrayTest::testSubstitute
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
        assertEquals( "Number of elements should be 11", 11, da.getNumElements());

        ((ResizableDoubleArray)da).substituteMostRecentElement(24);

        assertEquals( "Number of elements should be 11", 11, da.getNumElements());

        try {
            ((ResizableDoubleArray)da).discardMostRecentElements(10);
        } catch( Exception e ){
            fail( "Trying to discard a negative number of element is not allowed");
        }

        ((ResizableDoubleArray)da).substituteMostRecentElement(24);

        assertEquals( "Number of elements should be 1", 1, da.getNumElements());

    }

// org.apache.commons.math.util.ResizableDoubleArrayTest::testMutators
    public void testMutators() {
        ((ResizableDoubleArray)da).setContractionCriteria(10f);
        assertEquals(10f, ((ResizableDoubleArray)da).getContractionCriteria(), 0);
        ((ResizableDoubleArray)da).setExpansionFactor(8f);
        assertEquals(8f, ((ResizableDoubleArray)da).getExpansionFactor(), 0);
        try {
            ((ResizableDoubleArray)da).setExpansionFactor(11f);  
            fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }
        ((ResizableDoubleArray)da).setExpansionMode(
                ResizableDoubleArray.ADDITIVE_MODE);
        assertEquals(ResizableDoubleArray.ADDITIVE_MODE,
                ((ResizableDoubleArray)da).getExpansionMode());
        try {
            ((ResizableDoubleArray)da).setExpansionMode(-1);
            fail ("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.util.ResizableDoubleArrayTest::testEqualsAndHashCode
    public void testEqualsAndHashCode() throws Exception {

        
        ResizableDoubleArray first = new ResizableDoubleArray();
        Double other = new Double(2);
        assertFalse(first.equals(other));

        
        other = null;
        assertFalse(first.equals(other));

        
        assertTrue(first.equals(first));

        
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
