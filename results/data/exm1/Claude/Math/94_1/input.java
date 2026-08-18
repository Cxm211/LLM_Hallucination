// buggy code
    public static int gcd(int u, int v) {
        if (u * v == 0) {
            return (Math.abs(u) + Math.abs(v));
        }
        // keep u and v negative, as negative integers range down to
        // -2^31, while positive numbers can only be as large as 2^31-1
        // (i.e. we can't necessarily negate a negative number without
        // overflow)
        /* assert u!=0 && v!=0; */
        if (u > 0) {
            u = -u;
        } // make u negative
        if (v > 0) {
            v = -v;
        } // make v negative
        // B1. [Find power of 2]
        int k = 0;
        while ((u & 1) == 0 && (v & 1) == 0 && k < 31) { // while u and v are
                                                            // both even...
            u /= 2;
            v /= 2;
            k++; // cast out twos.
        }
        if (k == 31) {
            throw new ArithmeticException("overflow: gcd is 2^31");
        }
        // B2. Initialize: u and v have been divided by 2^k and at least
        // one is odd.
        int t = ((u & 1) == 1) ? v : -(u / 2)/* B3 */;
        // t negative: u was odd, v may be even (t replaces v)
        // t positive: u was even, v is odd (t replaces u)
        do {
            /* assert u<0 && v<0; */
            // B4/B3: cast out twos from t.
            while ((t & 1) == 0) { // while t is even..
                t /= 2; // cast out twos
            }
            // B5 [reset max(u,v)]
            if (t > 0) {
                u = -t;
            } else {
                v = t;
            }
            // B6/B3. at this point both u and v should be odd.
            t = (v - u) / 2;
            // |u| larger: t positive (replace u)
            // |v| larger: t negative (replace v)
        } while (t != 0);
        return -u * (1 << k); // gcd is u*2^k
    }

// relevant test
// org.apache.commons.math.ode.nonstiff.AdamsMoultonIntegratorTest::testDecreasingSteps
    public void testDecreasingSteps()
        throws DerivativeException, IntegratorException {

        TestProblemAbstract[] problems = TestProblemFactory.getProblems();
        for (int k = 0; k < problems.length; ++k) {

            double previousError = Double.NaN;
            for (int i = 6; i < 10; ++i) {

                TestProblemAbstract pb  = (TestProblemAbstract) problems[k].clone();
                double step = (pb.getFinalTime() - pb.getInitialTime()) * Math.pow(2.0, -i);
                if (pb instanceof TestProblem3) {
                    step /= 8;
                }

                FirstOrderIntegrator integ = new AdamsMoultonIntegrator(5, step);
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
                    assertEquals(pb.getFinalTime(), stopTime, 1.0e-10);
                }

                double error = handler.getMaximalValueError();
                if (i > 6) {
                    assertTrue(error < Math.abs(previousError));
                }
                previousError = error;

            }

        }

    }

// org.apache.commons.math.ode.nonstiff.AdamsMoultonIntegratorTest::testSmallStep
    public void testSmallStep()
        throws DerivativeException, IntegratorException {

        TestProblem1 pb  = new TestProblem1();
        double step = (pb.getFinalTime() - pb.getInitialTime()) * 0.001;

        FirstOrderIntegrator integ = new AdamsMoultonIntegrator(3, step);
        TestProblemHandler handler = new TestProblemHandler(pb, integ);
        integ.addStepHandler(handler);
        integ.integrate(pb,
                pb.getInitialTime(), pb.getInitialState(),
                pb.getFinalTime(), new double[pb.getDimension()]);

        assertTrue(handler.getLastError() < 7.0e-12);
        assertTrue(handler.getMaximalValueError() < 4.0e-11);
        assertEquals(0, handler.getMaximalTimeError(), 1.0e-14);
        assertEquals("Adams-Moulton", integ.getName());

    }

// org.apache.commons.math.ode.nonstiff.AdamsMoultonIntegratorTest::testBigStep
    public void testBigStep()
        throws DerivativeException, IntegratorException {

        TestProblem1 pb  = new TestProblem1();
        double step = (pb.getFinalTime() - pb.getInitialTime()) * 0.2;

        FirstOrderIntegrator integ = new AdamsMoultonIntegrator(3, step);
        TestProblemHandler handler = new TestProblemHandler(pb, integ);
        integ.addStepHandler(handler);
        integ.integrate(pb,
                pb.getInitialTime(), pb.getInitialState(),
                pb.getFinalTime(), new double[pb.getDimension()]);

        assertTrue(handler.getLastError() > 0.01);
        assertTrue(handler.getMaximalValueError() > 0.03);
        assertEquals(0, handler.getMaximalTimeError(), 1.0e-14);

    }

// org.apache.commons.math.ode.nonstiff.AdamsMoultonIntegratorTest::testBackward
    public void testBackward()
        throws DerivativeException, IntegratorException {

        TestProblem5 pb = new TestProblem5();
        double step = Math.abs(pb.getFinalTime() - pb.getInitialTime()) * 0.001;

        FirstOrderIntegrator integ = new AdamsMoultonIntegrator(5, step);
        TestProblemHandler handler = new TestProblemHandler(pb, integ);
        integ.addStepHandler(handler);
        integ.integrate(pb, pb.getInitialTime(), pb.getInitialState(),
                        pb.getFinalTime(), new double[pb.getDimension()]);

        assertTrue(handler.getLastError() < 5.0e-10);
        assertTrue(handler.getMaximalValueError() < 7.0e-10);
        assertEquals(0, handler.getMaximalTimeError(), 1.0e-12);
        assertEquals("Adams-Moulton", integ.getName());
    }

// org.apache.commons.math.stat.descriptive.DescriptiveStatisticsTest::testSetterInjection
    public void testSetterInjection() throws Exception {
        DescriptiveStatistics stats = new DescriptiveStatistics();
        stats.addValue(1);
        stats.addValue(3);
        assertEquals(2, stats.getMean(), 1E-10);
        
        stats.setMeanImpl(new deepMean());
        assertEquals(42, stats.getMean(), 1E-10);
    }

// org.apache.commons.math.stat.descriptive.DescriptiveStatisticsTest::testPercentileSetter
    public void testPercentileSetter() throws Exception {
        DescriptiveStatistics stats = new DescriptiveStatistics();
        stats.addValue(1);
        stats.addValue(2);
        stats.addValue(3);
        assertEquals(2, stats.getPercentile(50.0), 1E-10);
        
        
        stats.setPercentileImpl(new goodPercentile());
        assertEquals(2, stats.getPercentile(50.0), 1E-10);
        
        
        stats.setPercentileImpl(new subPercentile());
        assertEquals(10.0, stats.getPercentile(10.0), 1E-10);
        
        
        try {
            stats.setPercentileImpl(new badPercentile()); 
            fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.stat.descriptive.DescriptiveStatisticsTest::testRemoval
    public void testRemoval() {

        final DescriptiveStatistics dstat = new DescriptiveStatistics();

        checkremoval(dstat, 1, 6.0, 0.0, Double.NaN);
        checkremoval(dstat, 3, 5.0, 3.0, 4.5);
        checkremoval(dstat, 6, 3.5, 2.5, 3.0);
        checkremoval(dstat, 9, 3.5, 2.5, 3.0);
        checkremoval(dstat, DescriptiveStatistics.INFINITE_WINDOW, 3.5, 2.5, 3.0);

    }

// org.apache.commons.math.stat.descriptive.MultivariateSummaryStatisticsTest::testSetterInjection
    public void testSetterInjection() throws Exception {
        MultivariateSummaryStatistics u = new MultivariateSummaryStatistics(2, true);
        u.setMeanImpl(new StorelessUnivariateStatistic[] {
                        new sumMean(), new sumMean()
                      });
        u.addValue(new double[] { 1, 2 });
        u.addValue(new double[] { 3, 4 });
        assertEquals(4, u.getMean()[0], 1E-14);
        assertEquals(6, u.getMean()[1], 1E-14);
        u.clear();
        u.addValue(new double[] { 1, 2 });
        u.addValue(new double[] { 3, 4 });
        assertEquals(4, u.getMean()[0], 1E-14);
        assertEquals(6, u.getMean()[1], 1E-14);
        u.clear();
        u.setMeanImpl(new StorelessUnivariateStatistic[] {
                        new Mean(), new Mean()
                      }); 
        u.addValue(new double[] { 1, 2 });
        u.addValue(new double[] { 3, 4 });
        assertEquals(2, u.getMean()[0], 1E-14);
        assertEquals(3, u.getMean()[1], 1E-14);
    }

// org.apache.commons.math.stat.descriptive.MultivariateSummaryStatisticsTest::testSetterIllegalState
    public void testSetterIllegalState() throws Exception {
        MultivariateSummaryStatistics u = new MultivariateSummaryStatistics(2, true);
        u.addValue(new double[] { 1, 2 });
        u.addValue(new double[] { 3, 4 });
        try {
            u.setMeanImpl(new StorelessUnivariateStatistic[] {
                            new sumMean(), new sumMean()
                          });
            fail("Expecting IllegalStateException");
        } catch (IllegalStateException ex) {
            
        }
    }

// org.apache.commons.math.stat.descriptive.MultivariateSummaryStatisticsTest::testDimension
    public void testDimension() {
        try {
            new MultivariateSummaryStatistics(2, true).addValue(new double[3]);
        } catch (DimensionMismatchException dme) {
            
        } catch (Exception e) {
            fail("wrong exception caught");
        }
    }

// org.apache.commons.math.stat.descriptive.MultivariateSummaryStatisticsTest::testStats
    public void testStats() throws DimensionMismatchException {
        MultivariateSummaryStatistics u = new MultivariateSummaryStatistics(2, true);
        assertEquals(0, u.getN());
        u.addValue(new double[] { 1, 2 });
        u.addValue(new double[] { 2, 3 });
        u.addValue(new double[] { 2, 3 });
        u.addValue(new double[] { 3, 4 });
        assertEquals( 4, u.getN());
        assertEquals( 8, u.getSum()[0], 1.0e-10);
        assertEquals(12, u.getSum()[1], 1.0e-10);
        assertEquals(18, u.getSumSq()[0], 1.0e-10);
        assertEquals(38, u.getSumSq()[1], 1.0e-10);
        assertEquals( 1, u.getMin()[0], 1.0e-10);
        assertEquals( 2, u.getMin()[1], 1.0e-10);
        assertEquals( 3, u.getMax()[0], 1.0e-10);
        assertEquals( 4, u.getMax()[1], 1.0e-10);
        assertEquals(2.4849066497880003102, u.getSumLog()[0], 1.0e-10);
        assertEquals( 4.276666119016055311, u.getSumLog()[1], 1.0e-10);
        assertEquals( 1.8612097182041991979, u.getGeometricMean()[0], 1.0e-10);
        assertEquals( 2.9129506302439405217, u.getGeometricMean()[1], 1.0e-10);
        assertEquals( 2, u.getMean()[0], 1.0e-10);
        assertEquals( 3, u.getMean()[1], 1.0e-10);
        assertEquals(Math.sqrt(2.0 / 3.0), u.getStandardDeviation()[0], 1.0e-10);
        assertEquals(Math.sqrt(2.0 / 3.0), u.getStandardDeviation()[1], 1.0e-10);
        assertEquals(2.0 / 3.0, u.getCovariance().getEntry(0, 0), 1.0e-10);
        assertEquals(2.0 / 3.0, u.getCovariance().getEntry(0, 1), 1.0e-10);
        assertEquals(2.0 / 3.0, u.getCovariance().getEntry(1, 0), 1.0e-10);
        assertEquals(2.0 / 3.0, u.getCovariance().getEntry(1, 1), 1.0e-10);
        u.clear();
        assertEquals(0, u.getN());    
    }

// org.apache.commons.math.stat.descriptive.MultivariateSummaryStatisticsTest::testN0andN1Conditions
    public void testN0andN1Conditions() throws Exception {
        MultivariateSummaryStatistics u = new MultivariateSummaryStatistics(1, true);
        assertTrue(Double.isNaN(u.getMean()[0]));
        assertTrue(Double.isNaN(u.getStandardDeviation()[0]));

        
        u.addValue(new double[] { 1 });
        assertEquals(1.0, u.getMean()[0], 1.0e-10);
        assertEquals(1.0, u.getGeometricMean()[0], 1.0e-10);
        assertEquals(0.0, u.getStandardDeviation()[0], 1.0e-10);

                       
        u.addValue(new double[] { 2 });
        assertTrue(u.getStandardDeviation()[0] > 0);

    }

// org.apache.commons.math.stat.descriptive.MultivariateSummaryStatisticsTest::testNaNContracts
    public void testNaNContracts() throws DimensionMismatchException {
        MultivariateSummaryStatistics u = new MultivariateSummaryStatistics(1, true);
        assertTrue(Double.isNaN(u.getMean()[0])); 
        assertTrue(Double.isNaN(u.getMin()[0])); 
        assertTrue(Double.isNaN(u.getStandardDeviation()[0])); 
        assertTrue(Double.isNaN(u.getGeometricMean()[0]));

        u.addValue(new double[] { 1.0 });
        assertFalse(Double.isNaN(u.getMean()[0])); 
        assertFalse(Double.isNaN(u.getMin()[0])); 
        assertFalse(Double.isNaN(u.getStandardDeviation()[0])); 
        assertFalse(Double.isNaN(u.getGeometricMean()[0]));

    }

// org.apache.commons.math.stat.descriptive.MultivariateSummaryStatisticsTest::testSerialization
    public void testSerialization() throws DimensionMismatchException {
        MultivariateSummaryStatistics u = new MultivariateSummaryStatistics(2, true);
        
        TestUtils.checkSerializedEquality(u);
        MultivariateSummaryStatistics s = (MultivariateSummaryStatistics) TestUtils.serializeAndRecover(u);
        assertEquals(u, s);

        
        u.addValue(new double[] { 2d, 1d });
        u.addValue(new double[] { 1d, 1d });
        u.addValue(new double[] { 3d, 1d });
        u.addValue(new double[] { 4d, 1d });
        u.addValue(new double[] { 5d, 1d });

        
        TestUtils.checkSerializedEquality(u);
        s = (MultivariateSummaryStatistics) TestUtils.serializeAndRecover(u);
        assertEquals(u, s);

    }

// org.apache.commons.math.stat.descriptive.MultivariateSummaryStatisticsTest::testEqualsAndHashCode
    public void testEqualsAndHashCode() throws DimensionMismatchException {
        MultivariateSummaryStatistics u = new MultivariateSummaryStatistics(2, true);
        MultivariateSummaryStatistics t = null;
        int emptyHash = u.hashCode();
        assertTrue(u.equals(u));
        assertFalse(u.equals(t));
        assertFalse(u.equals(Double.valueOf(0)));
        t = new MultivariateSummaryStatistics(2, true);
        assertTrue(t.equals(u));
        assertTrue(u.equals(t));
        assertEquals(emptyHash, t.hashCode());

        
        u.addValue(new double[] { 2d, 1d });
        u.addValue(new double[] { 1d, 1d });
        u.addValue(new double[] { 3d, 1d });
        u.addValue(new double[] { 4d, 1d });
        u.addValue(new double[] { 5d, 1d });
        assertFalse(t.equals(u));
        assertFalse(u.equals(t));
        assertTrue(u.hashCode() != t.hashCode());

        
        t.addValue(new double[] { 2d, 1d });
        t.addValue(new double[] { 1d, 1d });
        t.addValue(new double[] { 3d, 1d });
        t.addValue(new double[] { 4d, 1d });
        t.addValue(new double[] { 5d, 1d });
        assertTrue(t.equals(u));
        assertTrue(u.equals(t));
        assertEquals(u.hashCode(), t.hashCode());   

        
        u.clear();
        t.clear();
        assertTrue(t.equals(u));
        assertTrue(u.equals(t));
        assertEquals(emptyHash, t.hashCode());
        assertEquals(emptyHash, u.hashCode());
    }

// org.apache.commons.math.stat.descriptive.StatisticalSummaryValuesTest::testSerialization
    public void testSerialization() {
        StatisticalSummaryValues u = new StatisticalSummaryValues(1, 2, 3, 4, 5, 6);
        TestUtils.checkSerializedEquality(u); 
        StatisticalSummaryValues t = (StatisticalSummaryValues) TestUtils.serializeAndRecover(u);
        verifyEquality(u, t);
    }

// org.apache.commons.math.stat.descriptive.StatisticalSummaryValuesTest::testEqualsAndHashCode
    public void testEqualsAndHashCode() {
        StatisticalSummaryValues u  = new StatisticalSummaryValues(1, 2, 3, 4, 5, 6);
        StatisticalSummaryValues t = null;
        assertTrue("reflexive", u.equals(u));
        assertFalse("non-null compared to null", u.equals(t));
        assertFalse("wrong type", u.equals(Double.valueOf(0)));
        t = new StatisticalSummaryValues(1, 2, 3, 4, 5, 6);
        assertTrue("instances with same data should be equal", t.equals(u));
        assertEquals("hash code", u.hashCode(), t.hashCode());
        
        u = new StatisticalSummaryValues(Double.NaN, 2, 3, 4, 5, 6);
        t = new StatisticalSummaryValues(1, Double.NaN, 3, 4, 5, 6);
        assertFalse("instances based on different data should be different", 
                (u.equals(t) ||t.equals(u)));
    }

// org.apache.commons.math.stat.descriptive.SummaryStatisticsTest::testStats
    public void testStats() {
        SummaryStatistics u = createSummaryStatistics();
        assertEquals("total count",0,u.getN(),tolerance);
        u.addValue(one);
        u.addValue(twoF);
        u.addValue(twoL);
        u.addValue(three);
        assertEquals("N",n,u.getN(),tolerance);
        assertEquals("sum",sum,u.getSum(),tolerance);
        assertEquals("sumsq",sumSq,u.getSumsq(),tolerance);
        assertEquals("var",var,u.getVariance(),tolerance);
        assertEquals("std",std,u.getStandardDeviation(),tolerance);
        assertEquals("mean",mean,u.getMean(),tolerance);
        assertEquals("min",min,u.getMin(),tolerance);
        assertEquals("max",max,u.getMax(),tolerance);
        u.clear();
        assertEquals("total count",0,u.getN(),tolerance);    
    }

// org.apache.commons.math.stat.descriptive.SummaryStatisticsTest::testN0andN1Conditions
    public void testN0andN1Conditions() throws Exception {
        SummaryStatistics u = createSummaryStatistics();
        assertTrue("Mean of n = 0 set should be NaN", 
                Double.isNaN( u.getMean() ) );
        assertTrue("Standard Deviation of n = 0 set should be NaN", 
                Double.isNaN( u.getStandardDeviation() ) );
        assertTrue("Variance of n = 0 set should be NaN", 
                Double.isNaN(u.getVariance() ) );

        
        u.addValue(one);
        assertTrue("mean should be one (n = 1)", 
                u.getMean() == one);
        assertTrue("geometric should be one (n = 1) instead it is " + u.getGeometricMean(), 
                u.getGeometricMean() == one);
        assertTrue("Std should be zero (n = 1)", 
                u.getStandardDeviation() == 0.0);
        assertTrue("variance should be zero (n = 1)", 
                u.getVariance() == 0.0);

                       
        u.addValue(twoF);
        assertTrue("Std should not be zero (n = 2)", 
                u.getStandardDeviation() != 0.0);
        assertTrue("variance should not be zero (n = 2)", 
                u.getVariance() != 0.0);

    }

// org.apache.commons.math.stat.descriptive.SummaryStatisticsTest::testProductAndGeometricMean
    public void testProductAndGeometricMean() throws Exception {
        SummaryStatistics u = createSummaryStatistics();
        u.addValue( 1.0 );
        u.addValue( 2.0 );
        u.addValue( 3.0 );
        u.addValue( 4.0 );

        assertEquals( "Geometric mean not expected", 2.213364, 
                u.getGeometricMean(), 0.00001 );
    }

// org.apache.commons.math.stat.descriptive.SummaryStatisticsTest::testNaNContracts
    public void testNaNContracts() {
        SummaryStatistics u = createSummaryStatistics();
        assertTrue("mean not NaN",Double.isNaN(u.getMean())); 
        assertTrue("min not NaN",Double.isNaN(u.getMin())); 
        assertTrue("std dev not NaN",Double.isNaN(u.getStandardDeviation())); 
        assertTrue("var not NaN",Double.isNaN(u.getVariance())); 
        assertTrue("geom mean not NaN",Double.isNaN(u.getGeometricMean()));

        u.addValue(1.0);

        assertEquals( "mean not expected", 1.0, 
                u.getMean(), Double.MIN_VALUE);
        assertEquals( "variance not expected", 0.0, 
                u.getVariance(), Double.MIN_VALUE);
        assertEquals( "geometric mean not expected", 1.0, 
                u.getGeometricMean(), Double.MIN_VALUE);

        u.addValue(-1.0);

        assertTrue("geom mean not NaN",Double.isNaN(u.getGeometricMean()));

        u.addValue(0.0);

        assertTrue("geom mean not NaN",Double.isNaN(u.getGeometricMean()));

        
    }

// org.apache.commons.math.stat.descriptive.SummaryStatisticsTest::testGetSummary
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

// org.apache.commons.math.stat.descriptive.SummaryStatisticsTest::testSerialization
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

// org.apache.commons.math.stat.descriptive.SummaryStatisticsTest::testEqualsAndHashCode
    public void testEqualsAndHashCode() {
        SummaryStatistics u = createSummaryStatistics();
        SummaryStatistics t = null;
        int emptyHash = u.hashCode();
        assertTrue("reflexive", u.equals(u));
        assertFalse("non-null compared to null", u.equals(t));
        assertFalse("wrong type", u.equals(Double.valueOf(0)));
        t = createSummaryStatistics();
        assertTrue("empty instances should be equal", t.equals(u));
        assertTrue("empty instances should be equal", u.equals(t));
        assertEquals("empty hash code", emptyHash, t.hashCode());

        
        u.addValue(2d);
        u.addValue(1d);
        u.addValue(3d);
        u.addValue(4d);
        assertFalse("different n's should make instances not equal", t.equals(u));
        assertFalse("different n's should make instances not equal", u.equals(t));
        assertTrue("different n's should make hashcodes different", 
                u.hashCode() != t.hashCode());

        
        t.addValue(2d);
        t.addValue(1d);
        t.addValue(3d);
        t.addValue(4d);
        assertTrue("summaries based on same data should be equal", t.equals(u));
        assertTrue("summaries based on same data should be equal", u.equals(t));
        assertEquals("summaries based on same data should have same hashcodes", 
                u.hashCode(), t.hashCode());   

        
        u.clear();
        t.clear();
        assertTrue("empty instances should be equal", t.equals(u));
        assertTrue("empty instances should be equal", u.equals(t));
        assertEquals("empty hash code", emptyHash, t.hashCode());
        assertEquals("empty hash code", emptyHash, u.hashCode());
    }

// org.apache.commons.math.stat.descriptive.SummaryStatisticsTest::testCopy
    public void testCopy() throws Exception {
        SummaryStatistics u = createSummaryStatistics();
        u.addValue(2d);
        u.addValue(1d);
        u.addValue(3d);
        u.addValue(4d);
        SummaryStatistics v = new SummaryStatistics(u);
        assertEquals(u, v);
        assertEquals(v, u);
        assertTrue(v.geoMean == v.getGeoMeanImpl());
        assertTrue(v.mean == v.getMeanImpl());
        assertTrue(v.min == v.getMinImpl());
        assertTrue(v.max == v.getMaxImpl());
        assertTrue(v.sum == v.getSumImpl());
        assertTrue(v.sumsq == v.getSumsqImpl());
        assertTrue(v.sumLog == v.getSumLogImpl());
        assertTrue(v.variance == v.getVarianceImpl());
        
        
        u.addValue(7d);
        u.addValue(9d);
        u.addValue(11d);
        u.addValue(23d);
        v.addValue(7d);
        v.addValue(9d);
        v.addValue(11d);
        v.addValue(23d);
        assertEquals(u, v);
        assertEquals(v, u);
        
        
        u.clear();
        u.setSumImpl(new Sum());
        SummaryStatistics.copy(u,v);
        assertEquals(u.sum, v.sum);
        assertEquals(u.getSumImpl(), v.getSumImpl());
        
    }

// org.apache.commons.math.stat.descriptive.SummaryStatisticsTest::testSetterInjection
    public void testSetterInjection() throws Exception {
        SummaryStatistics u = createSummaryStatistics();
        u.setMeanImpl(new Sum());
        u.setSumLogImpl(new Sum());
        u.addValue(1);
        u.addValue(3);
        assertEquals(4, u.getMean(), 1E-14);
        assertEquals(4, u.getSumOfLogs(), 1E-14);
        assertEquals(Math.exp(2), u.getGeometricMean(), 1E-14);
        u.clear();
        u.addValue(1);
        u.addValue(2);
        assertEquals(3, u.getMean(), 1E-14);
        u.clear();
        u.setMeanImpl(new Mean()); 
    }

// org.apache.commons.math.stat.descriptive.SummaryStatisticsTest::testSetterIllegalState
    public void testSetterIllegalState() throws Exception {
        SummaryStatistics u = createSummaryStatistics();
        u.addValue(1);
        u.addValue(3);
        try {
            u.setMeanImpl(new Sum());
            fail("Expecting IllegalStateException");
        } catch (IllegalStateException ex) {
            
        }
    }

// org.apache.commons.math.stat.descriptive.moment.GeometricMeanTest::testSpecialValues
    public void testSpecialValues() {
        GeometricMean mean = new GeometricMean();
        
        assertTrue(Double.isNaN(mean.getResult()));
        
        
        mean.increment(1d);
        assertFalse(Double.isNaN(mean.getResult()));
        
        
        mean.increment(0d);
        assertEquals(0d, mean.getResult(), 0);
        
        
        mean.increment(Double.POSITIVE_INFINITY);
        assertTrue(Double.isNaN(mean.getResult()));
        
        
        mean.clear();
        assertTrue(Double.isNaN(mean.getResult()));
        
        
        mean.increment(Double.POSITIVE_INFINITY);
        assertEquals(Double.POSITIVE_INFINITY, mean.getResult(), 0);
        
        
        mean.increment(-2d);
        assertTrue(Double.isNaN(mean.getResult()));
    }

// org.apache.commons.math.stat.descriptive.moment.KurtosisTest::testNaN
    public void testNaN() {
        Kurtosis kurt = new Kurtosis();
        assertTrue(Double.isNaN(kurt.getResult()));
        kurt.increment(1d);
        assertTrue(Double.isNaN(kurt.getResult()));
        kurt.increment(1d);
        assertTrue(Double.isNaN(kurt.getResult()));
        kurt.increment(1d);
        assertTrue(Double.isNaN(kurt.getResult()));
        kurt.increment(1d);
        assertFalse(Double.isNaN(kurt.getResult()));      
    }

// org.apache.commons.math.stat.descriptive.moment.MeanTest::testSmallSamples
    public void testSmallSamples() {
        Mean mean = new Mean();
        assertTrue(Double.isNaN(mean.getResult()));
        mean.increment(1d);
        assertEquals(1d, mean.getResult(), 0);
    }

// org.apache.commons.math.stat.descriptive.moment.SkewnessTest::testNaN
    public void testNaN() {
        Skewness skew = new Skewness();
        assertTrue(Double.isNaN(skew.getResult()));
        skew.increment(1d);
        assertTrue(Double.isNaN(skew.getResult()));
        skew.increment(1d);
        assertTrue(Double.isNaN(skew.getResult()));
        skew.increment(1d);
        assertFalse(Double.isNaN(skew.getResult()));      
    }

// org.apache.commons.math.stat.descriptive.moment.StandardDeviationTest::testNaN
    public void testNaN() {
        StandardDeviation std = new StandardDeviation();
        assertTrue(Double.isNaN(std.getResult()));
        std.increment(1d);
        assertEquals(0d, std.getResult(), 0);
    }

// org.apache.commons.math.stat.descriptive.moment.StandardDeviationTest::testPopulation
    public void testPopulation() {
        double[] values = {-1.0d, 3.1d, 4.0d, -2.1d, 22d, 11.7d, 3d, 14d};
        double sigma = populationStandardDeviation(values);
        SecondMoment m = new SecondMoment();
        m.evaluate(values);  
        StandardDeviation s1 = new StandardDeviation();
        s1.setBiasCorrected(false);
        assertEquals(sigma, s1.evaluate(values), 1E-14);
        s1.incrementAll(values);
        assertEquals(sigma, s1.getResult(), 1E-14);
        s1 = new StandardDeviation(false, m);
        assertEquals(sigma, s1.getResult(), 1E-14);     
        s1 = new StandardDeviation(false);
        assertEquals(sigma, s1.evaluate(values), 1E-14);
        s1.incrementAll(values);
        assertEquals(sigma, s1.getResult(), 1E-14);     
    }

// org.apache.commons.math.stat.descriptive.moment.VarianceTest::testNaN
    public void testNaN() {
        StandardDeviation std = new StandardDeviation();
        assertTrue(Double.isNaN(std.getResult()));
        std.increment(1d);
        assertEquals(0d, std.getResult(), 0);
    }

// org.apache.commons.math.stat.descriptive.moment.VarianceTest::testPopulation
    public void testPopulation() {
        double[] values = {-1.0d, 3.1d, 4.0d, -2.1d, 22d, 11.7d, 3d, 14d};
        SecondMoment m = new SecondMoment();
        m.evaluate(values);  
        Variance v1 = new Variance();
        v1.setBiasCorrected(false);
        assertEquals(populationVariance(values), v1.evaluate(values), 1E-14);
        v1.incrementAll(values);
        assertEquals(populationVariance(values), v1.getResult(), 1E-14);
        v1 = new Variance(false, m);
        assertEquals(populationVariance(values), v1.getResult(), 1E-14);     
        v1 = new Variance(false);
        assertEquals(populationVariance(values), v1.evaluate(values), 1E-14);
        v1.incrementAll(values);
        assertEquals(populationVariance(values), v1.getResult(), 1E-14);     
    }

// org.apache.commons.math.stat.descriptive.rank.MaxTest::testSpecialValues
    public void testSpecialValues() {
        double[] testArray = {0d, Double.NaN, Double.NEGATIVE_INFINITY, 
                Double.POSITIVE_INFINITY};
        Max max = new Max();
        assertTrue(Double.isNaN(max.getResult()));
        max.increment(testArray[0]);
        assertEquals(0d, max.getResult(), 0);
        max.increment(testArray[1]);
        assertEquals(0d, max.getResult(), 0);
        max.increment(testArray[2]);
        assertEquals(0d, max.getResult(), 0);
        max.increment(testArray[3]);
        assertEquals(Double.POSITIVE_INFINITY, max.getResult(), 0);
        assertEquals(Double.POSITIVE_INFINITY, max.evaluate(testArray), 0);     
    }

// org.apache.commons.math.stat.descriptive.rank.MaxTest::testNaNs
    public void testNaNs() {
        Max max = new Max();
        double nan = Double.NaN;
        assertEquals(3d, max.evaluate(new double[]{nan, 2d, 3d}), 0);     
        assertEquals(3d, max.evaluate(new double[]{1d, nan, 3d}), 0);     
        assertEquals(2d, max.evaluate(new double[]{1d, 2d, nan}), 0);     
        assertTrue(Double.isNaN(max.evaluate(new double[]{nan, nan, nan})));     
    }

// org.apache.commons.math.stat.descriptive.rank.MinTest::testSpecialValues
    public void testSpecialValues() {
        double[] testArray = {0d, Double.NaN, Double.POSITIVE_INFINITY, 
                Double.NEGATIVE_INFINITY};
        Min min = new Min();
        assertTrue(Double.isNaN(min.getResult()));
        min.increment(testArray[0]);
        assertEquals(0d, min.getResult(), 0);
        min.increment(testArray[1]);
        assertEquals(0d, min.getResult(), 0);
        min.increment(testArray[2]);
        assertEquals(0d, min.getResult(), 0);
        min.increment(testArray[3]);
        assertEquals(Double.NEGATIVE_INFINITY, min.getResult(), 0);
        assertEquals(Double.NEGATIVE_INFINITY, min.evaluate(testArray), 0);     
    }

// org.apache.commons.math.stat.descriptive.rank.MinTest::testNaNs
    public void testNaNs() {
        Min min = new Min();
        double nan = Double.NaN;
        assertEquals(2d, min.evaluate(new double[]{nan, 2d, 3d}), 0);     
        assertEquals(1d, min.evaluate(new double[]{1d, nan, 3d}), 0);     
        assertEquals(1d, min.evaluate(new double[]{1d, 2d, nan}), 0);     
        assertTrue(Double.isNaN(min.evaluate(new double[]{nan, nan, nan})));     
    }

// org.apache.commons.math.stat.descriptive.summary.ProductTest::testSpecialValues
    public void testSpecialValues() {
        Product product = new Product();
        assertTrue(Double.isNaN(product.getResult()));
        product.increment(1);
        assertEquals(1, product.getResult(), 0);
        product.increment(Double.POSITIVE_INFINITY);
        assertEquals(Double.POSITIVE_INFINITY, product.getResult(), 0);
        product.increment(Double.NEGATIVE_INFINITY);
        assertEquals(Double.NEGATIVE_INFINITY, product.getResult(), 0);
        product.increment(Double.NaN);
        assertTrue(Double.isNaN(product.getResult())); 
        product.increment(1);
        assertTrue(Double.isNaN(product.getResult())); 
    }

// org.apache.commons.math.stat.descriptive.summary.SumLogTest::testSpecialValues
    public void testSpecialValues() {
        SumOfLogs sum = new SumOfLogs();
        
        assertTrue(Double.isNaN(sum.getResult()));
        
        
        sum.increment(1d);
        assertFalse(Double.isNaN(sum.getResult()));
        
        
        sum.increment(0d);
        assertEquals(Double.NEGATIVE_INFINITY, sum.getResult(), 0);
        
        
        sum.increment(Double.POSITIVE_INFINITY);
        assertTrue(Double.isNaN(sum.getResult()));
        
        
        sum.clear();
        assertTrue(Double.isNaN(sum.getResult()));
        
        
        sum.increment(Double.POSITIVE_INFINITY);
        assertEquals(Double.POSITIVE_INFINITY, sum.getResult(), 0);
        
        
        sum.increment(-2d);
        assertTrue(Double.isNaN(sum.getResult()));
    }

// org.apache.commons.math.stat.descriptive.summary.SumSqTest::testSpecialValues
    public void testSpecialValues() {
        SumOfSquares sumSq = new SumOfSquares();
        assertTrue(Double.isNaN(sumSq.getResult()));
        sumSq.increment(2d);
        assertEquals(4d, sumSq.getResult(), 0);
        sumSq.increment(Double.POSITIVE_INFINITY);
        assertEquals(Double.POSITIVE_INFINITY, sumSq.getResult(), 0);
        sumSq.increment(Double.NEGATIVE_INFINITY);
        assertEquals(Double.POSITIVE_INFINITY, sumSq.getResult(), 0);
        sumSq.increment(Double.NaN);
        assertTrue(Double.isNaN(sumSq.getResult())); 
        sumSq.increment(1);
        assertTrue(Double.isNaN(sumSq.getResult())); 
    }

// org.apache.commons.math.stat.descriptive.summary.SumTest::testSpecialValues
    public void testSpecialValues() {
        Sum sum = new Sum();
        assertTrue(Double.isNaN(sum.getResult()));
        sum.increment(1);
        assertEquals(1, sum.getResult(), 0);
        sum.increment(Double.POSITIVE_INFINITY);
        assertEquals(Double.POSITIVE_INFINITY, sum.getResult(), 0);
        sum.increment(Double.NEGATIVE_INFINITY);
        assertTrue(Double.isNaN(sum.getResult()));
        sum.increment(1);
        assertTrue(Double.isNaN(sum.getResult())); 
    }

// org.apache.commons.math.stat.descriptive.summary.SumTest::testWeightedConsistency
    public void testWeightedConsistency() {}

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
            fail("Expecting ArithmeticException");
        } catch (ArithmeticException ex) {
        }
        try {
            MathUtils.addAndCheck(bigNeg, -1);
            fail("Expecting ArithmeticException");
        } catch (ArithmeticException ex) {
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
                assertEquals(n + " choose " + k, (double)binomialCoefficient(n, k), MathUtils.binomialCoefficientDouble(n, k), Double.MIN_VALUE);
                assertEquals(n + " choose " + k, Math.log((double)binomialCoefficient(n, k)), MathUtils.binomialCoefficientLog(n, k), 10E-12);
            }
        }

        
    }

// org.apache.commons.math.util.MathUtilsTest::testBinomialCoefficientFail
    public void testBinomialCoefficientFail() {
        try {
            MathUtils.binomialCoefficient(4, 5);
            fail("expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            ;
        }

        try {
            MathUtils.binomialCoefficientDouble(4, 5);
            fail("expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            ;
        }

        try {
            MathUtils.binomialCoefficientLog(4, 5);
            fail("expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            ;
        }
        try {
            MathUtils.binomialCoefficient(67, 34);
            fail("expecting ArithmeticException");
        } catch (ArithmeticException ex) {
            ;
        }
        double x = MathUtils.binomialCoefficientDouble(1030, 515);
        assertTrue("expecting infinite binomial coefficient", Double.isInfinite(x));
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

// org.apache.commons.math.util.MathUtilsTest::testEquals
    public void testEquals() {
        double[] testArray = {
            Double.NaN,
            Double.POSITIVE_INFINITY,
            Double.NEGATIVE_INFINITY,
            1d,
            0d };
        for (int i = 0; i < testArray.length; i++) {
            for (int j = 0; j < testArray.length; j++) {
                if (i == j) {
                    assertTrue(MathUtils.equals(testArray[i], testArray[j]));
                    assertTrue(MathUtils.equals(testArray[j], testArray[i]));
                } else {
                    assertTrue(!MathUtils.equals(testArray[i], testArray[j]));
                    assertTrue(!MathUtils.equals(testArray[j], testArray[i]));
                }
            }
        }
    }

// org.apache.commons.math.util.MathUtilsTest::testArrayEquals
    public void testArrayEquals() {
        assertFalse(MathUtils.equals(new double[] { 1d }, null));
        assertFalse(MathUtils.equals(null, new double[] { 1d }));
        assertTrue(MathUtils.equals((double[]) null, (double[]) null));

        assertFalse(MathUtils.equals(new double[] { 1d }, new double[0]));
        assertTrue(MathUtils.equals(new double[] { 1d }, new double[] { 1d }));
        assertTrue(MathUtils.equals(new double[] {
                                      Double.NaN, Double.POSITIVE_INFINITY,
                                      Double.NEGATIVE_INFINITY, 1d, 0d
                                    }, new double[] {
                                      Double.NaN, Double.POSITIVE_INFINITY,
                                      Double.NEGATIVE_INFINITY, 1d, 0d
                                    }));
        assertFalse(MathUtils.equals(new double[] { Double.POSITIVE_INFINITY },
                                     new double[] { Double.NEGATIVE_INFINITY }));
        assertFalse(MathUtils.equals(new double[] { 1d },
                                     new double[] { MathUtils.nextAfter(1d, 2d) }));

    }

// org.apache.commons.math.util.MathUtilsTest::testFactorial
    public void testFactorial() {
        for (int i = 1; i < 10; i++) {
            assertEquals(i + "! ", factorial(i), MathUtils.factorial(i));
            assertEquals(i + "! ", (double)factorial(i), MathUtils.factorialDouble(i), Double.MIN_VALUE);
            assertEquals(i + "! ", Math.log((double)factorial(i)), MathUtils.factorialLog(i), 10E-12);
        }
        assertEquals("0", 1, MathUtils.factorial(0));
        assertEquals("0", 1.0d, MathUtils.factorialDouble(0), 1E-14);
        assertEquals("0", 0.0d, MathUtils.factorialLog(0), 1E-14);
    }

// org.apache.commons.math.util.MathUtilsTest::testFactorialFail
    public void testFactorialFail() {
        try {
            MathUtils.factorial(-1);
            fail("expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            ;
        }
        try {
            MathUtils.factorialDouble(-1);
            fail("expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            ;
        }
        try {
            MathUtils.factorialLog(-1);
            fail("expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            ;
        }
        try {
            MathUtils.factorial(21);
            fail("expecting ArithmeticException");
        } catch (ArithmeticException ex) {
            ;
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
                    MathUtils.hash(new double[] { MathUtils.nextAfter(1d, 2d) }));
        assertFalse(MathUtils.hash(new double[] { 1d }) ==
                    MathUtils.hash(new double[] { 1d, 1d }));
    }

// org.apache.commons.math.util.MathUtilsTest::testPermutedArrayHash
    public void testPermutedArrayHash() {
        double[] original = new double[10];
        double[] permuted = new double[10];
        RandomDataImpl random = new RandomDataImpl();
        
        
        for (int i = 0; i < 10; i++) {
            original[i] = random.nextUniform((double)i + 0.5, (double)i + 0.75);
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
        assertEquals((int)1, MathUtils.indicator((int)(2)));
        assertEquals((int)1, MathUtils.indicator((int)(0)));
        assertEquals((int)(-1), MathUtils.indicator((int)(-2)));
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
        assertEquals(2310, MathUtils.lcm(a, c));

        try {
            MathUtils.lcm(Integer.MAX_VALUE, Integer.MAX_VALUE - 1);
            fail("Expecting ArithmeticException");
        } catch (ArithmeticException ex) {
            
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
            fail("Expecting ArithmeticException");
        } catch (ArithmeticException ex) {
        }
        try {
            MathUtils.mulAndCheck(bigNeg, 2);
            fail("Expecting ArithmeticException");
        } catch (ArithmeticException ex) {
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
        testMulAndCheckLongFailure(max, 2L);
        testMulAndCheckLongFailure(2L, max);
        testMulAndCheckLongFailure(min, 2L);
        testMulAndCheckLongFailure(2L, min);
        testMulAndCheckLongFailure(min, -1L);
        testMulAndCheckLongFailure(-1L, min);
    }

// org.apache.commons.math.util.MathUtilsTest::testNextAfter
    public void testNextAfter() {
        
        assertEquals(16.0, MathUtils.nextAfter(15.999999999999998, 34.27555555555555), 0.0);

        
        assertEquals(-15.999999999999996, MathUtils.nextAfter(-15.999999999999998, 34.27555555555555), 0.0);

        
        assertEquals(15.999999999999996, MathUtils.nextAfter(15.999999999999998, 2.142222222222222), 0.0);

        
        assertEquals(-15.999999999999996, MathUtils.nextAfter(-15.999999999999998, 2.142222222222222), 0.0);

        
        assertEquals(8.000000000000002, MathUtils.nextAfter(8.0, 34.27555555555555), 0.0);

        
        assertEquals(-7.999999999999999, MathUtils.nextAfter(-8.0, 34.27555555555555), 0.0);

        
        assertEquals(7.999999999999999, MathUtils.nextAfter(8.0, 2.142222222222222), 0.0);

        
        assertEquals(-7.999999999999999, MathUtils.nextAfter(-8.0, 2.142222222222222), 0.0);

        
        assertEquals(2.308922399667661E-4, MathUtils.nextAfter(2.3089223996676606E-4, 2.308922399667661E-4), 0.0);

        
        assertEquals(2.308922399667661E-4, MathUtils.nextAfter(2.3089223996676606E-4, 2.3089223996676606E-4), 0.0);

        
        assertEquals(2.3089223996676603E-4, MathUtils.nextAfter(2.3089223996676606E-4, 2.3089223996676603E-4), 0.0);

        
        assertEquals(2.3089223996676603E-4, MathUtils.nextAfter(2.3089223996676606E-4, -2.308922399667661E-4), 0.0);

        
        assertEquals(2.3089223996676603E-4, MathUtils.nextAfter(2.3089223996676606E-4, -2.3089223996676606E-4), 0.0);

        
        assertEquals(2.3089223996676603E-4, MathUtils.nextAfter(2.3089223996676606E-4, -2.3089223996676603E-4), 0.0);

        
        assertEquals(-2.3089223996676603E-4, MathUtils.nextAfter(-2.3089223996676606E-4, 2.308922399667661E-4), 0.0);

        
        assertEquals(-2.3089223996676603E-4, MathUtils.nextAfter(-2.3089223996676606E-4, 2.3089223996676606E-4), 0.0);

        
        assertEquals(-2.3089223996676603E-4, MathUtils.nextAfter(-2.3089223996676606E-4, 2.3089223996676603E-4), 0.0);

        
        assertEquals(-2.308922399667661E-4, MathUtils.nextAfter(-2.3089223996676606E-4, -2.308922399667661E-4), 0.0);

        
        assertEquals(-2.308922399667661E-4, MathUtils.nextAfter(-2.3089223996676606E-4, -2.3089223996676606E-4), 0.0);

        
        assertEquals(-2.3089223996676603E-4, MathUtils.nextAfter(-2.3089223996676606E-4, -2.3089223996676603E-4), 0.0);

    }

// org.apache.commons.math.util.MathUtilsTest::testNextAfterSpecialCases
    public void testNextAfterSpecialCases() {
        assertTrue(Double.isInfinite(MathUtils.nextAfter(Double.NEGATIVE_INFINITY, 0)));
        assertTrue(Double.isInfinite(MathUtils.nextAfter(Double.POSITIVE_INFINITY, 0)));
        assertTrue(Double.isNaN(MathUtils.nextAfter(Double.NaN, 0)));
        assertTrue(Double.isInfinite(MathUtils.nextAfter(Double.MAX_VALUE, Double.POSITIVE_INFINITY)));
        assertTrue(Double.isInfinite(MathUtils.nextAfter(-Double.MAX_VALUE, Double.NEGATIVE_INFINITY)));
        assertEquals(Double.MIN_VALUE, MathUtils.nextAfter(0, 1), 0);
        assertEquals(-Double.MIN_VALUE, MathUtils.nextAfter(0, -1), 0);
        assertEquals(0, MathUtils.nextAfter(Double.MIN_VALUE, -1), 0);
        assertEquals(0, MathUtils.nextAfter(-Double.MIN_VALUE, 1), 0);
    }

// org.apache.commons.math.util.MathUtilsTest::testScalb
    public void testScalb() {
        assertEquals( 0.0, MathUtils.scalb(0.0, 5), 1.0e-15);
        assertEquals(32.0, MathUtils.scalb(1.0, 5), 1.0e-15);
        assertEquals(1.0 / 32.0, MathUtils.scalb(1.0,  -5), 1.0e-15);
        assertEquals(Math.PI, MathUtils.scalb(Math.PI, 0), 1.0e-15);
        assertTrue(Double.isInfinite(MathUtils.scalb(Double.POSITIVE_INFINITY, 1)));
        assertTrue(Double.isInfinite(MathUtils.scalb(Double.NEGATIVE_INFINITY, 1)));
        assertTrue(Double.isNaN(MathUtils.scalb(Double.NaN, 1)));
    }

// org.apache.commons.math.util.MathUtilsTest::testNormalizeAngle
    public void testNormalizeAngle() {
        for (double a = -15.0; a <= 15.0; a += 0.1) {
            for (double b = -15.0; b <= 15.0; b += 0.2) {
                double c = MathUtils.normalizeAngle(a, b);
                assertTrue((b - Math.PI) <= c);
                assertTrue(c <= (b + Math.PI));
                double twoK = Math.rint((a - c) / Math.PI);
                assertEquals(c, a - twoK * Math.PI, 1.0e-14);
            }
        }
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
        } catch (ArithmeticException ex) {
            
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
        } catch (IllegalArgumentException ex) {
            
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
        } catch (ArithmeticException ex) {
            
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
        } catch (IllegalArgumentException ex) {
            
        }

        
        TestUtils.assertEquals(Float.NaN, MathUtils.round(Float.NaN, 2), 0.0f);
        assertEquals(0.0f, MathUtils.round(0.0f, 2), 0.0f);
        assertEquals(Float.POSITIVE_INFINITY, MathUtils.round(Float.POSITIVE_INFINITY, 2), 0.0f);
        assertEquals(Float.NEGATIVE_INFINITY, MathUtils.round(Float.NEGATIVE_INFINITY, 2), 0.0f);
    }

// org.apache.commons.math.util.MathUtilsTest::testSignByte
    public void testSignByte() {
        assertEquals((byte)1, MathUtils.indicator((byte)2));
        assertEquals((byte)(-1), MathUtils.indicator((byte)(-2)));
    }

// org.apache.commons.math.util.MathUtilsTest::testSignDouble
    public void testSignDouble() {
        double delta = 0.0;
        assertEquals(1.0, MathUtils.indicator(2.0), delta);
        assertEquals(-1.0, MathUtils.indicator(-2.0), delta);
    }

// org.apache.commons.math.util.MathUtilsTest::testSignFloat
    public void testSignFloat() {
        float delta = 0.0F;
        assertEquals(1.0F, MathUtils.indicator(2.0F), delta);
        assertEquals(-1.0F, MathUtils.indicator(-2.0F), delta);
    }

// org.apache.commons.math.util.MathUtilsTest::testSignInt
    public void testSignInt() {
        assertEquals((int)1, MathUtils.indicator((int)(2)));
        assertEquals((int)(-1), MathUtils.indicator((int)(-2)));
    }

// org.apache.commons.math.util.MathUtilsTest::testSignLong
    public void testSignLong() {
        assertEquals(1L, MathUtils.indicator(2L));
        assertEquals(-1L, MathUtils.indicator(-2L));
    }

// org.apache.commons.math.util.MathUtilsTest::testSignShort
    public void testSignShort() {
        assertEquals((short)1, MathUtils.indicator((short)2));
        assertEquals((short)(-1), MathUtils.indicator((short)(-2)));
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
        try {
            MathUtils.subAndCheck(big, -1);
            fail("Expecting ArithmeticException");
        } catch (ArithmeticException ex) {
        }
        try {
            MathUtils.subAndCheck(bigNeg, 1);
            fail("Expecting ArithmeticException");
        } catch (ArithmeticException ex) {
        }
    }

// org.apache.commons.math.util.MathUtilsTest::testSubAndCheckErrorMessage
    public void testSubAndCheckErrorMessage() {
        int big = Integer.MAX_VALUE;
        try {
            MathUtils.subAndCheck(big, -1);
            fail("Expecting ArithmeticException");
        } catch (ArithmeticException ex) {
            assertEquals("overflow: subtract", ex.getMessage());
        }
    }

// org.apache.commons.math.util.MathUtilsTest::testSubAndCheckLong
    public void testSubAndCheckLong() {
        long max = Long.MAX_VALUE;
        long min = Long.MIN_VALUE;
        assertEquals(max, MathUtils.subAndCheck(max, 0));
        assertEquals(min, MathUtils.subAndCheck(min, 0));
        assertEquals(-max, MathUtils.subAndCheck(0, max));
        testSubAndCheckLongFailure(0L, min);
        testSubAndCheckLongFailure(max, -1L);
        testSubAndCheckLongFailure(min, 1L);
    }
