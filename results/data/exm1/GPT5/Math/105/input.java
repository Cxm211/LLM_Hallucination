// buggy code
    public double getSumSquaredErrors() {
        return sumYY - sumXY * sumXY / sumXX;
    }

// relevant test
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
            double x = regression.getSlopeConfidenceInterval(1);
            fail("expecting IllegalArgumentException for alpha = 1");
        } catch (IllegalArgumentException ex) {
            ;
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
