// buggy code
    public double chiSquare(double[] expected, long[] observed)
        throws IllegalArgumentException {
        if ((expected.length < 2) || (expected.length != observed.length)) {
            throw new IllegalArgumentException(
                    "observed, expected array lengths incorrect");
        }
        if (!isPositive(expected) || !isNonNegative(observed)) {
            throw new IllegalArgumentException(
                "observed counts must be non-negative and expected counts must be postive");
        }
        double sumSq = 0.0d;
        double dev = 0.0d;
        for (int i = 0; i < observed.length; i++) {
                dev = ((double) observed[i] - expected[i]);
                sumSq += dev * dev / expected[i];
        }
        return sumSq;
    }

// relevant test
// org.apache.commons.math.stat.inference.ChiSquareTestTest::testChiSquare
    public void testChiSquare() throws Exception {
 
        
        
        
        
        
        long[] observed = {10, 9, 11};
        double[] expected = {10, 10, 10};
        assertEquals("chi-square statistic", 0.2,  testStatistic.chiSquare(expected, observed), 10E-12);
        assertEquals("chi-square p-value", 0.904837418036, testStatistic.chiSquareTest(expected, observed), 1E-10);
        
        long[] observed1 = { 500, 623, 72, 70, 31 };
        double[] expected1 = { 485, 541, 82, 61, 37 };
        assertEquals( "chi-square test statistic", 9.023307936427388, testStatistic.chiSquare(expected1, observed1), 1E-10);
        assertEquals("chi-square p-value", 0.06051952647453607, testStatistic.chiSquareTest(expected1, observed1), 1E-9);
        assertTrue("chi-square test reject", testStatistic.chiSquareTest(expected1, observed1, 0.08));
        assertTrue("chi-square test accept", !testStatistic.chiSquareTest(expected1, observed1, 0.05));

        try {
            testStatistic.chiSquareTest(expected1, observed1, 95);
            fail("alpha out of range, IllegalArgumentException expected");
        } catch (IllegalArgumentException ex) {
            
        }  
        
        long[] tooShortObs = { 0 };
        double[] tooShortEx = { 1 };
        try {
            testStatistic.chiSquare(tooShortEx, tooShortObs);
            fail("arguments too short, IllegalArgumentException expected");
        } catch (IllegalArgumentException ex) {
            
        }

        
        long[] unMatchedObs = { 0, 1, 2, 3 };
        double[] unMatchedEx = { 1, 1, 2 };
        try {
            testStatistic.chiSquare(unMatchedEx, unMatchedObs);
            fail("arrays have different lengths, IllegalArgumentException expected");
        } catch (IllegalArgumentException ex) {
            
        }
        
        
        expected[0] = 0;
        try {
            testStatistic.chiSquareTest(expected, observed, .01);
            fail("bad expected count, IllegalArgumentException expected");
        } catch (IllegalArgumentException ex) {
            
        } 
        
        
        expected[0] = 1;
        observed[0] = -1;
        try {
            testStatistic.chiSquareTest(expected, observed, .01);
            fail("bad expected count, IllegalArgumentException expected");
        } catch (IllegalArgumentException ex) {
            
        } 
        
    }

// org.apache.commons.math.stat.inference.ChiSquareTestTest::testChiSquareIndependence
    public void testChiSquareIndependence() throws Exception {
        
        
        
        long[][] counts = { {40, 22, 43}, {91, 21, 28}, {60, 10, 22}};
        assertEquals( "chi-square test statistic", 22.709027688, testStatistic.chiSquare(counts), 1E-9);
        assertEquals("chi-square p-value", 0.000144751460134, testStatistic.chiSquareTest(counts), 1E-9);
        assertTrue("chi-square test reject", testStatistic.chiSquareTest(counts, 0.0002));
        assertTrue("chi-square test accept", !testStatistic.chiSquareTest(counts, 0.0001));    
        
        long[][] counts2 = {{10, 15}, {30, 40}, {60, 90} };
        assertEquals( "chi-square test statistic", 0.168965517241, testStatistic.chiSquare(counts2), 1E-9);
        assertEquals("chi-square p-value",0.918987499852, testStatistic.chiSquareTest(counts2), 1E-9);
        assertTrue("chi-square test accept", !testStatistic.chiSquareTest(counts2, 0.1)); 
        
        
        long[][] counts3 = { {40, 22, 43}, {91, 21, 28}, {60, 10}};
        try {
            testStatistic.chiSquare(counts3);
            fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }
        
        
        long[][] counts4 = {{40, 22, 43}};
        try {
            testStatistic.chiSquare(counts4);
            fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        } 
        long[][] counts5 = {{40}, {40}, {30}, {10}};
        try {
            testStatistic.chiSquare(counts5);
            fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        } 
        
        
        long[][] counts6 = {{10, -2}, {30, 40}, {60, 90} };
        try {
            testStatistic.chiSquare(counts6);
            fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        } 
        
        
        try {
            testStatistic.chiSquareTest(counts, 0);
            fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        } 
    }

// org.apache.commons.math.stat.inference.ChiSquareTestTest::testChiSquareLargeTestStatistic
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
                114875.90421929007, testStatistic.chiSquare(exp, obs), 1E-9);
    }

// org.apache.commons.math.stat.inference.ChiSquareTestTest::testChiSquareZeroCount
    public void testChiSquareZeroCount() throws Exception {
        
        long[][] counts = { {40, 0, 4}, {91, 1, 2}, {60, 2, 0}};
        assertEquals( "chi-square test statistic", 9.67444662263,
                testStatistic.chiSquare(counts), 1E-9);
        assertEquals("chi-square p-value", 0.0462835770603,
                testStatistic.chiSquareTest(counts), 1E-9);       
    }

// org.apache.commons.math.stat.inference.ChiSquareTestTest::testChiSquareDataSetsComparisonEqualCounts
    public void testChiSquareDataSetsComparisonEqualCounts()
    throws Exception {
        long[] observed1 = {10, 12, 12, 10};
        long[] observed2 = {5, 15, 14, 10};    
        assertEquals("chi-square p value", 0.541096, 
                testStatistic.chiSquareTestDataSetsComparison(
                observed1, observed2), 1E-6);
        assertEquals("chi-square test statistic", 2.153846,
                testStatistic.chiSquareDataSetsComparison(
                observed1, observed2), 1E-6);
        assertFalse("chi-square test result", 
                testStatistic.chiSquareTestDataSetsComparison(
                observed1, observed2, 0.4));
    }

// org.apache.commons.math.stat.inference.ChiSquareTestTest::testChiSquareDataSetsComparisonUnEqualCounts
    public void testChiSquareDataSetsComparisonUnEqualCounts()
    throws Exception {
        long[] observed1 = {10, 12, 12, 10, 15};
        long[] observed2 = {15, 10, 10, 15, 5};    
        assertEquals("chi-square p value", 0.124115, 
                testStatistic.chiSquareTestDataSetsComparison(
                observed1, observed2), 1E-6);
        assertEquals("chi-square test statistic", 7.232189,
                testStatistic.chiSquareDataSetsComparison(
                observed1, observed2), 1E-6);
        assertTrue("chi-square test result", 
                testStatistic.chiSquareTestDataSetsComparison(
                observed1, observed2, 0.13));
        assertFalse("chi-square test result", 
                testStatistic.chiSquareTestDataSetsComparison(
                observed1, observed2, 0.12));
    }

// org.apache.commons.math.stat.inference.ChiSquareTestTest::testChiSquareDataSetsComparisonBadCounts
    public void testChiSquareDataSetsComparisonBadCounts()
    throws Exception {
        long[] observed1 = {10, -1, 12, 10, 15};
        long[] observed2 = {15, 10, 10, 15, 5};
        try {
            testStatistic.chiSquareTestDataSetsComparison(
                    observed1, observed2);
            fail("Expecting IllegalArgumentException - negative count");
        } catch (IllegalArgumentException ex) {
            
        }
        long[] observed3 = {10, 0, 12, 10, 15};
        long[] observed4 = {15, 0, 10, 15, 5};
        try {
            testStatistic.chiSquareTestDataSetsComparison(
                    observed3, observed4);
            fail("Expecting IllegalArgumentException - double 0's");
        } catch (IllegalArgumentException ex) {
            
        }
        long[] observed5 = {10, 10, 12, 10, 15};
        long[] observed6 = {0, 0, 0, 0, 0};
        try {
            testStatistic.chiSquareTestDataSetsComparison(
                    observed5, observed6);
            fail("Expecting IllegalArgumentException - vanishing counts");
        } catch (IllegalArgumentException ex) {
            
        }
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
        sampleStats = SummaryStatistics.newInstance();
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
        SummaryStatistics oneSidedPStats = SummaryStatistics.newInstance();    
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
        SummaryStatistics sampleStats1 = SummaryStatistics.newInstance();  
        for (int i = 0; i < sample1.length; i++) {
            sampleStats1.addValue(sample1[i]);
        }
        SummaryStatistics sampleStats2 = SummaryStatistics.newInstance();    
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
        SummaryStatistics sampleStats1 = SummaryStatistics.newInstance();  
        for (int i = 0; i < sample1.length; i++) {
            sampleStats1.addValue(sample1[i]);
        }
        SummaryStatistics sampleStats2 = SummaryStatistics.newInstance();    
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
