// buggy code
    protected double getInitialDomain(double p) {
        double ret;
        double d = getDenominatorDegreesOfFreedom();
            // use mean
            ret = d / (d - 2.0);
        return ret;
    }

// relevant test
// org.apache.commons.math.distribution.FDistributionTest::testCumulativeProbabilityExtremes
    public void testCumulativeProbabilityExtremes() throws Exception {
        setCumulativeTestPoints(new double[] {-2, 0});
        setCumulativeTestValues(new double[] {0, 0});
        verifyCumulativeProbabilities();
    }

// org.apache.commons.math.distribution.FDistributionTest::testInverseCumulativeProbabilityExtremes
    public void testInverseCumulativeProbabilityExtremes() throws Exception {
        setInverseCumulativeTestPoints(new double[] {0, 1});
        setInverseCumulativeTestValues(new double[] {0, Double.POSITIVE_INFINITY});
        verifyInverseCumulativeProbabilities();
    }

// org.apache.commons.math.distribution.FDistributionTest::testDfAccessors
    public void testDfAccessors() {
        FDistribution distribution = (FDistribution) getDistribution();
        assertEquals(5d, distribution.getNumeratorDegreesOfFreedom(), Double.MIN_VALUE);
        distribution.setNumeratorDegreesOfFreedom(4d);
        assertEquals(4d, distribution.getNumeratorDegreesOfFreedom(), Double.MIN_VALUE);
        assertEquals(6d, distribution.getDenominatorDegreesOfFreedom(), Double.MIN_VALUE);
        distribution.setDenominatorDegreesOfFreedom(4d);
        assertEquals(4d, distribution.getDenominatorDegreesOfFreedom(), Double.MIN_VALUE);
        try {
            distribution.setNumeratorDegreesOfFreedom(0d);
            fail("Expecting IllegalArgumentException for df = 0");
        } catch (IllegalArgumentException ex) {
            
        }
        try {
            distribution.setDenominatorDegreesOfFreedom(0d);
            fail("Expecting IllegalArgumentException for df = 0");
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.distribution.FDistributionTest::testLargeDegreesOfFreedom
    public void testLargeDegreesOfFreedom() throws Exception {
        org.apache.commons.math.distribution.FDistributionImpl fd =
            new org.apache.commons.math.distribution.FDistributionImpl(
                100000., 100000.);
        double p = fd.cumulativeProbability(.999);
        double x = fd.inverseCumulativeProbability(p);
        assertEquals(.999, x, 1.0e-5);
    }

// org.apache.commons.math.distribution.FDistributionTest::testSmallDegreesOfFreedom
    public void testSmallDegreesOfFreedom() throws Exception {
        org.apache.commons.math.distribution.FDistributionImpl fd =
            new org.apache.commons.math.distribution.FDistributionImpl(
                1.0, 1.0);
        double p = fd.cumulativeProbability(0.975);
        double x = fd.inverseCumulativeProbability(p);
        assertEquals(0.975, x, 1.0e-5);

        fd.setDenominatorDegreesOfFreedom(2.0);
        p = fd.cumulativeProbability(0.975);
        x = fd.inverseCumulativeProbability(p);
        assertEquals(0.975, x, 1.0e-5);
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
            fail("alpha out of range, IllegalArgumentException expected");
        } catch (IllegalArgumentException ex) {
            
        }  
        
        long[] tooShortObs = { 0 };
        double[] tooShortEx = { 1 };
        try {
            TestUtils.chiSquare(tooShortEx, tooShortObs);
            fail("arguments too short, IllegalArgumentException expected");
        } catch (IllegalArgumentException ex) {
            
        }

        
        long[] unMatchedObs = { 0, 1, 2, 3 };
        double[] unMatchedEx = { 1, 1, 2 };
        try {
            TestUtils.chiSquare(unMatchedEx, unMatchedObs);
            fail("arrays have different lengths, IllegalArgumentException expected");
        } catch (IllegalArgumentException ex) {
            
        }
        
        
        expected[0] = 0;
        try {
            TestUtils.chiSquareTest(expected, observed, .01);
            fail("bad expected count, IllegalArgumentException expected");
        } catch (IllegalArgumentException ex) {
            
        } 
        
        
        expected[0] = 1;
        observed[0] = -1;
        try {
            TestUtils.chiSquareTest(expected, observed, .01);
            fail("bad expected count, IllegalArgumentException expected");
        } catch (IllegalArgumentException ex) {
            
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
            fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }
        
        
        long[][] counts4 = {{40, 22, 43}};
        try {
            TestUtils.chiSquare(counts4);
            fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        } 
        long[][] counts5 = {{40}, {40}, {30}, {10}};
        try {
            TestUtils.chiSquare(counts5);
            fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        } 
        
        
        long[][] counts6 = {{10, -2}, {30, 40}, {60, 90} };
        try {
            TestUtils.chiSquare(counts6);
            fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        } 
        
        
        try {
            TestUtils.chiSquareTest(counts, 0);
            fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
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
            fail("arguments too short, IllegalArgumentException expected");
        } catch (IllegalArgumentException ex) {
            
        }

        try {
            TestUtils.t(mu, (SummaryStatistics) null);
            fail("arguments too short, IllegalArgumentException expected");
        } catch (IllegalArgumentException ex) {
            
        }

        try {
            TestUtils.t(mu, emptyObs);
            fail("arguments too short, IllegalArgumentException expected");
        } catch (IllegalArgumentException ex) {
            
        }
        
        try {
            TestUtils.t(mu, emptyStats);
            fail("arguments too short, IllegalArgumentException expected");
        } catch (IllegalArgumentException ex) {
            
        }

        try {
            TestUtils.t(mu, tooShortObs);
            fail("insufficient data to compute t statistic, IllegalArgumentException expected");
        } catch (IllegalArgumentException ex) {
            
        }
        try {
            TestUtils.tTest(mu, tooShortObs);
            fail("insufficient data to perform t test, IllegalArgumentException expected");
        } catch (IllegalArgumentException ex) {
            
        }  

        try {
            TestUtils.t(mu, (SummaryStatistics) null);
            fail("insufficient data to compute t statistic, IllegalArgumentException expected");
        } catch (IllegalArgumentException ex) {
            
        }
        try {
            TestUtils.tTest(mu, (SummaryStatistics) null);
            fail("insufficient data to perform t test, IllegalArgumentException expected");
        } catch (IllegalArgumentException ex) {
            
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
            fail("alpha out of range, IllegalArgumentException expected");
        } catch (IllegalArgumentException ex) {
            
        }  
        
        try {
            TestUtils.tTest(0d, oneSidedPStats, 95);
            fail("alpha out of range, IllegalArgumentException expected");
        } catch (IllegalArgumentException ex) {
            
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
            fail("alpha out of range, IllegalArgumentException expected");
        } catch (IllegalArgumentException ex) {
            
        } 
        
        try {
            TestUtils.tTest(sampleStats1, sampleStats2, .95);
            fail("alpha out of range, IllegalArgumentException expected");
        } catch (IllegalArgumentException ex) {
            
        }  
        
        try {
            TestUtils.tTest(sample1, tooShortObs, .01);
            fail("insufficient data, IllegalArgumentException expected");
        } catch (IllegalArgumentException ex) {
            
        }  
        
        try {
            TestUtils.tTest(sampleStats1, (SummaryStatistics) null, .01);
            fail("insufficient data, IllegalArgumentException expected");
        } catch (IllegalArgumentException ex) {
            
        }  
        
        try {
            TestUtils.tTest(sample1, tooShortObs);
            fail("insufficient data, IllegalArgumentException expected");
        } catch (IllegalArgumentException ex) {
            
        }  
        
        try {
            TestUtils.tTest(sampleStats1, (SummaryStatistics) null);
            fail("insufficient data, IllegalArgumentException expected");
        } catch (IllegalArgumentException ex) {
            
        }  
        
        try {
            TestUtils.t(sample1, tooShortObs);
            fail("insufficient data, IllegalArgumentException expected");
        } catch (IllegalArgumentException ex) {
            
        }
        
        try {
            TestUtils.t(sampleStats1, (SummaryStatistics) null);
            fail("insufficient data, IllegalArgumentException expected");
        } catch (IllegalArgumentException ex) {
            
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
