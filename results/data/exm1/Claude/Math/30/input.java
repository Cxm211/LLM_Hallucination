// buggy code
    private double calculateAsymptoticPValue(final double Umin,
                                             final int n1,
                                             final int n2)
        throws ConvergenceException, MaxCountExceededException {

        final int n1n2prod = n1 * n2;

        // http://en.wikipedia.org/wiki/Mann%E2%80%93Whitney_U#Normal_approximation
        final double EU = n1n2prod / 2.0;
        final double VarU = n1n2prod * (n1 + n2 + 1) / 12.0;

        final double z = (Umin - EU) / FastMath.sqrt(VarU);

        final NormalDistribution standardNormal = new NormalDistribution(0, 1);

        return 2 * standardNormal.cumulativeProbability(z);
    }

// relevant test
// org.apache.commons.math3.stat.inference.MannWhitneyUTestTest::testMannWhitneyUSimple
    public void testMannWhitneyUSimple() throws Exception {
        
        final double x[] = {19, 22, 16, 29, 24};
        final double y[] = {20, 11, 17, 12};
        
        Assert.assertEquals(17, testStatistic.mannWhitneyU(x, y), 1e-10);
        Assert.assertEquals(0.08641, testStatistic.mannWhitneyUTest(x, y), 1e-5);
    }

// org.apache.commons.math3.stat.inference.MannWhitneyUTestTest::testMannWhitneyUInputValidation
    public void testMannWhitneyUInputValidation() throws Exception {
        
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
    public void testBigDataSet() throws Exception {
        double[] d1 = new double[1500];
        double[] d2 = new double[1500];
        for (int i = 0; i < 1500; i++) {
            d1[i] = 2 * i;
            d2[i] = 2 * i + 1;
        }
        double result = testStatistic.mannWhitneyUTest(d1, d2);
        Assert.assertTrue(result > 0.1);
    }
