// buggy code
    public void addValue(double value) {
        sumImpl.increment(value);
        sumsqImpl.increment(value);
        minImpl.increment(value);
        maxImpl.increment(value);
        sumLogImpl.increment(value);
        secondMoment.increment(value);
        // If mean, variance or geomean have been overridden,
        // need to increment these
        if (!(meanImpl instanceof Mean)) {
            meanImpl.increment(value);
        }
        if (!(varianceImpl instanceof Variance)) {
            varianceImpl.increment(value);
        }
        if (!(geoMeanImpl instanceof GeometricMean)) {
            geoMeanImpl.increment(value);
        }
        n++;
    }

// relevant test
// org.apache.commons.math.ode.JacobianMatricesTest::testLowAccuracyExternalDifferentiation
    public void testLowAccuracyExternalDifferentiation() {
        
        
        
        
        
        
        
        
        
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

// org.apache.commons.math.ode.JacobianMatricesTest::testHighAccuracyExternalDifferentiation
    public void testHighAccuracyExternalDifferentiation() {
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

// org.apache.commons.math.ode.JacobianMatricesTest::testInternalDifferentiation
    public void testInternalDifferentiation() {
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

// org.apache.commons.math.ode.JacobianMatricesTest::testAnalyticalDifferentiation
    public void testAnalyticalDifferentiation() {
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

// org.apache.commons.math.ode.JacobianMatricesTest::testFinalResult
    public void testFinalResult() {

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

// org.apache.commons.math.ode.JacobianMatricesTest::testParameterizable
    public void testParameterizable() {

        AbstractIntegrator integ =
            new DormandPrince54Integrator(1.0e-8, 100.0, new double[] { 1.0e-10, 1.0e-10 }, new double[] { 1.0e-10, 1.0e-10 });
        double[] y = new double[] { 0.0, 1.0 };
        ParameterizedCircle pcircle = new ParameterizedCircle(y, 1.0, 1.0, 0.1);

        double hP = 1.0e-12;
        double hY = 1.0e-12;

        JacobianMatrices jacob = new JacobianMatrices(pcircle, new double[] { hY, hY },
                                                      Circle.CX, Circle.OMEGA);
        jacob.addParameterJacobianProvider(pcircle);
        jacob.setParameterizedODE(pcircle);
        jacob.setParameterStep(Circle.OMEGA, hP);
        jacob.setInitialMainStateJacobian(pcircle.exactDyDy0(0));
        jacob.setInitialParameterJacobian(Circle.CX, pcircle.exactDyDcx(0));

        jacob.setInitialParameterJacobian(Circle.OMEGA, pcircle.exactDyDom(0));

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
        jacob.getCurrentParameterJacobian(Circle.CX, dydp0);
        for (int i = 0; i < dydp0.length; ++i) {
            Assert.assertEquals(pcircle.exactDyDcx(t)[i], dydp0[i], 5.0e-4);
        }

        double[] dydp1 = new double[2];
        jacob.getCurrentParameterJacobian(Circle.OMEGA, dydp1);
        for (int i = 0; i < dydp1.length; ++i) {
            Assert.assertEquals(pcircle.exactDyDom(t)[i], dydp1[i], 1.0e-2);
        }
    }

// org.apache.commons.math.stat.CertifiedDataTest::testSummaryStatistics
    public void testSummaryStatistics() throws Exception {
        SummaryStatistics u = new SummaryStatistics();
        loadStats("data/PiDigits.txt", u);
        Assert.assertEquals("PiDigits: std", std, u.getStandardDeviation(), 1E-13);
        Assert.assertEquals("PiDigits: mean", mean, u.getMean(), 1E-13);

        loadStats("data/Mavro.txt", u);
        Assert.assertEquals("Mavro: std", std, u.getStandardDeviation(), 1E-14);
        Assert.assertEquals("Mavro: mean", mean, u.getMean(), 1E-14);

        loadStats("data/Michelso.txt", u);
        Assert.assertEquals("Michelso: std", std, u.getStandardDeviation(), 1E-13);
        Assert.assertEquals("Michelso: mean", mean, u.getMean(), 1E-13);

        loadStats("data/NumAcc1.txt", u);
        Assert.assertEquals("NumAcc1: std", std, u.getStandardDeviation(), 1E-14);
        Assert.assertEquals("NumAcc1: mean", mean, u.getMean(), 1E-14);

        loadStats("data/NumAcc2.txt", u);
        Assert.assertEquals("NumAcc2: std", std, u.getStandardDeviation(), 1E-14);
        Assert.assertEquals("NumAcc2: mean", mean, u.getMean(), 1E-14);
    }

// org.apache.commons.math.stat.CertifiedDataTest::testDescriptiveStatistics
    public void testDescriptiveStatistics() throws Exception {

        DescriptiveStatistics u = new DescriptiveStatistics();

        loadStats("data/PiDigits.txt", u);
        Assert.assertEquals("PiDigits: std", std, u.getStandardDeviation(), 1E-14);
        Assert.assertEquals("PiDigits: mean", mean, u.getMean(), 1E-14);

        loadStats("data/Mavro.txt", u);
        Assert.assertEquals("Mavro: std", std, u.getStandardDeviation(), 1E-14);
        Assert.assertEquals("Mavro: mean", mean, u.getMean(), 1E-14);

        loadStats("data/Michelso.txt", u);
        Assert.assertEquals("Michelso: std", std, u.getStandardDeviation(), 1E-14);
        Assert.assertEquals("Michelso: mean", mean, u.getMean(), 1E-14);

        loadStats("data/NumAcc1.txt", u);
        Assert.assertEquals("NumAcc1: std", std, u.getStandardDeviation(), 1E-14);
        Assert.assertEquals("NumAcc1: mean", mean, u.getMean(), 1E-14);

        loadStats("data/NumAcc2.txt", u);
        Assert.assertEquals("NumAcc2: std", std, u.getStandardDeviation(), 1E-14);
        Assert.assertEquals("NumAcc2: mean", mean, u.getMean(), 1E-14);
    }

// org.apache.commons.math.stat.descriptive.AggregateSummaryStatisticsTest::testAggregation
    public void testAggregation() {
        AggregateSummaryStatistics aggregate = new AggregateSummaryStatistics();
        SummaryStatistics setOneStats = aggregate.createContributingStatistics();
        SummaryStatistics setTwoStats = aggregate.createContributingStatistics();

        Assert.assertNotNull("The set one contributing stats are null", setOneStats);
        Assert.assertNotNull("The set two contributing stats are null", setTwoStats);
        Assert.assertNotSame("Contributing stats objects are the same", setOneStats, setTwoStats);

        setOneStats.addValue(2);
        setOneStats.addValue(3);
        setOneStats.addValue(5);
        setOneStats.addValue(7);
        setOneStats.addValue(11);
        Assert.assertEquals("Wrong number of set one values", 5, setOneStats.getN());
        Assert.assertTrue("Wrong sum of set one values", Precision.equals(28.0, setOneStats.getSum(), 1));

        setTwoStats.addValue(2);
        setTwoStats.addValue(4);
        setTwoStats.addValue(8);
        Assert.assertEquals("Wrong number of set two values", 3, setTwoStats.getN());
        Assert.assertTrue("Wrong sum of set two values", Precision.equals(14.0, setTwoStats.getSum(), 1));

        Assert.assertEquals("Wrong number of aggregate values", 8, aggregate.getN());
        Assert.assertTrue("Wrong aggregate sum", Precision.equals(42.0, aggregate.getSum(), 1));
    }

// org.apache.commons.math.stat.descriptive.AggregateSummaryStatisticsTest::testAggregationConsistency
    public void testAggregationConsistency() throws Exception {

        
        double[] totalSample = generateSample();
        double[][] subSamples = generatePartition(totalSample);
        int nSamples = subSamples.length;

        
        AggregateSummaryStatistics aggregate = new AggregateSummaryStatistics();
        SummaryStatistics totalStats = new SummaryStatistics();

        
        SummaryStatistics componentStats[] = new SummaryStatistics[nSamples];

        for (int i = 0; i < nSamples; i++) {

            
            componentStats[i] = aggregate.createContributingStatistics();

            
            for (int j = 0; j < subSamples[i].length; j++) {
                componentStats[i].addValue(subSamples[i][j]);
            }
        }

        
        for (int i = 0; i < totalSample.length; i++) {
            totalStats.addValue(totalSample[i]);
        }

        
        Assert.assertEquals(totalStats.getSummary(), aggregate.getSummary());

    }

// org.apache.commons.math.stat.descriptive.AggregateSummaryStatisticsTest::testAggregate
    public void testAggregate() throws Exception {

        
        double[] totalSample = generateSample();
        double[][] subSamples = generatePartition(totalSample);
        int nSamples = subSamples.length;

        
        SummaryStatistics totalStats = new SummaryStatistics();
        for (int i = 0; i < totalSample.length; i++) {
            totalStats.addValue(totalSample[i]);
        }

        
        SummaryStatistics[] subSampleStats = new SummaryStatistics[nSamples];
        for (int i = 0; i < nSamples; i++) {
            subSampleStats[i] = new SummaryStatistics();
        }
        Collection<SummaryStatistics> aggregate = new ArrayList<SummaryStatistics>();
        for (int i = 0; i < nSamples; i++) {
            for (int j = 0; j < subSamples[i].length; j++) {
                subSampleStats[i].addValue(subSamples[i][j]);
            }
            aggregate.add(subSampleStats[i]);
        }

        
        StatisticalSummary aggregatedStats = AggregateSummaryStatistics.aggregate(aggregate);
        assertEquals(totalStats.getSummary(), aggregatedStats, 10E-12);
    }

// org.apache.commons.math.stat.descriptive.AggregateSummaryStatisticsTest::testAggregateDegenerate
    public void testAggregateDegenerate() throws Exception {
        double[] totalSample = {1, 2, 3, 4, 5};
        double[][] subSamples = {{1}, {2}, {3}, {4}, {5}};

        
        SummaryStatistics totalStats = new SummaryStatistics();
        for (int i = 0; i < totalSample.length; i++) {
            totalStats.addValue(totalSample[i]);
        }

        
        SummaryStatistics[] subSampleStats = new SummaryStatistics[5];
        for (int i = 0; i < 5; i++) {
            subSampleStats[i] = new SummaryStatistics();
        }
        Collection<SummaryStatistics> aggregate = new ArrayList<SummaryStatistics>();
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < subSamples[i].length; j++) {
                subSampleStats[i].addValue(subSamples[i][j]);
            }
            aggregate.add(subSampleStats[i]);
        }

        
        StatisticalSummaryValues aggregatedStats = AggregateSummaryStatistics.aggregate(aggregate);
        assertEquals(totalStats.getSummary(), aggregatedStats, 10E-12);
    }

// org.apache.commons.math.stat.descriptive.AggregateSummaryStatisticsTest::testAggregateSpecialValues
    public void testAggregateSpecialValues() throws Exception {
        double[] totalSample = {Double.POSITIVE_INFINITY, 2, 3, Double.NaN, 5};
        double[][] subSamples = {{Double.POSITIVE_INFINITY, 2}, {3}, {Double.NaN}, {5}};

        
        SummaryStatistics totalStats = new SummaryStatistics();
        for (int i = 0; i < totalSample.length; i++) {
            totalStats.addValue(totalSample[i]);
        }

        
        SummaryStatistics[] subSampleStats = new SummaryStatistics[5];
        for (int i = 0; i < 4; i++) {
            subSampleStats[i] = new SummaryStatistics();
        }
        Collection<SummaryStatistics> aggregate = new ArrayList<SummaryStatistics>();
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < subSamples[i].length; j++) {
                subSampleStats[i].addValue(subSamples[i][j]);
            }
            aggregate.add(subSampleStats[i]);
        }

        
        StatisticalSummaryValues aggregatedStats = AggregateSummaryStatistics.aggregate(aggregate);
        assertEquals(totalStats.getSummary(), aggregatedStats, 10E-12);

    }

// org.apache.commons.math.stat.descriptive.DescriptiveStatisticsTest::testSetterInjection
    public void testSetterInjection() {
        DescriptiveStatistics stats = createDescriptiveStatistics();
        stats.addValue(1);
        stats.addValue(3);
        Assert.assertEquals(2, stats.getMean(), 1E-10);
        
        stats.setMeanImpl(new deepMean());
        Assert.assertEquals(42, stats.getMean(), 1E-10);
    }

// org.apache.commons.math.stat.descriptive.DescriptiveStatisticsTest::testCopy
    public void testCopy() {
        DescriptiveStatistics stats = createDescriptiveStatistics();
        stats.addValue(1);
        stats.addValue(3);
        DescriptiveStatistics copy = new DescriptiveStatistics(stats);
        Assert.assertEquals(2, copy.getMean(), 1E-10);
        
        stats.setMeanImpl(new deepMean());
        copy = stats.copy();
        Assert.assertEquals(42, copy.getMean(), 1E-10);
    }

// org.apache.commons.math.stat.descriptive.DescriptiveStatisticsTest::testWindowSize
    public void testWindowSize() {
        DescriptiveStatistics stats = createDescriptiveStatistics();
        stats.setWindowSize(300);
        for (int i = 0; i < 100; ++i) {
            stats.addValue(i + 1);
        }
        int refSum = (100 * 101) / 2;
        Assert.assertEquals(refSum / 100.0, stats.getMean(), 1E-10);
        Assert.assertEquals(300, stats.getWindowSize());
        try {
            stats.setWindowSize(-3);
            Assert.fail("an exception should have been thrown");
        } catch (IllegalArgumentException iae) {
            
        }
        Assert.assertEquals(300, stats.getWindowSize());
        stats.setWindowSize(50);
        Assert.assertEquals(50, stats.getWindowSize());
        int refSum2 = refSum - (50 * 51) / 2;
        Assert.assertEquals(refSum2 / 50.0, stats.getMean(), 1E-10);
    }

// org.apache.commons.math.stat.descriptive.DescriptiveStatisticsTest::testGetValues
    public void testGetValues() {
        DescriptiveStatistics stats = createDescriptiveStatistics();
        for (int i = 100; i > 0; --i) {
            stats.addValue(i);
        }
        int refSum = (100 * 101) / 2;
        Assert.assertEquals(refSum / 100.0, stats.getMean(), 1E-10);
        double[] v = stats.getValues();
        for (int i = 0; i < v.length; ++i) {
            Assert.assertEquals(100.0 - i, v[i], 1.0e-10);
        }
        double[] s = stats.getSortedValues();
        for (int i = 0; i < s.length; ++i) {
            Assert.assertEquals(i + 1.0, s[i], 1.0e-10);
        }
        Assert.assertEquals(12.0, stats.getElement(88), 1.0e-10);
    }

// org.apache.commons.math.stat.descriptive.DescriptiveStatisticsTest::testToString
    public void testToString() {
        DescriptiveStatistics stats = createDescriptiveStatistics();
        stats.addValue(1);
        stats.addValue(2);
        stats.addValue(3);
        Locale d = Locale.getDefault();
        Locale.setDefault(Locale.US);
        Assert.assertEquals("DescriptiveStatistics:\n" +
                     "n: 3\n" +
                     "min: 1.0\n" +
                     "max: 3.0\n" +
                     "mean: 2.0\n" +
                     "std dev: 1.0\n" +
                     "median: 2.0\n" +
                     "skewness: 0.0\n" +
                     "kurtosis: NaN\n",  stats.toString());
        Locale.setDefault(d);
    }

// org.apache.commons.math.stat.descriptive.DescriptiveStatisticsTest::testShuffledStatistics
    public void testShuffledStatistics() {
        
        
        
        DescriptiveStatistics reference = createDescriptiveStatistics();
        DescriptiveStatistics shuffled  = createDescriptiveStatistics();

        UnivariateStatistic tmp = shuffled.getGeometricMeanImpl();
        shuffled.setGeometricMeanImpl(shuffled.getMeanImpl());
        shuffled.setMeanImpl(shuffled.getKurtosisImpl());
        shuffled.setKurtosisImpl(shuffled.getSkewnessImpl());
        shuffled.setSkewnessImpl(shuffled.getVarianceImpl());
        shuffled.setVarianceImpl(shuffled.getMaxImpl());
        shuffled.setMaxImpl(shuffled.getMinImpl());
        shuffled.setMinImpl(shuffled.getSumImpl());
        shuffled.setSumImpl(shuffled.getSumsqImpl());
        shuffled.setSumsqImpl(tmp);

        for (int i = 100; i > 0; --i) {
            reference.addValue(i);
            shuffled.addValue(i);
        }

        Assert.assertEquals(reference.getMean(),          shuffled.getGeometricMean(), 1.0e-10);
        Assert.assertEquals(reference.getKurtosis(),      shuffled.getMean(),          1.0e-10);
        Assert.assertEquals(reference.getSkewness(),      shuffled.getKurtosis(), 1.0e-10);
        Assert.assertEquals(reference.getVariance(),      shuffled.getSkewness(), 1.0e-10);
        Assert.assertEquals(reference.getMax(),           shuffled.getVariance(), 1.0e-10);
        Assert.assertEquals(reference.getMin(),           shuffled.getMax(), 1.0e-10);
        Assert.assertEquals(reference.getSum(),           shuffled.getMin(), 1.0e-10);
        Assert.assertEquals(reference.getSumsq(),         shuffled.getSum(), 1.0e-10);
        Assert.assertEquals(reference.getGeometricMean(), shuffled.getSumsq(), 1.0e-10);

    }

// org.apache.commons.math.stat.descriptive.DescriptiveStatisticsTest::testPercentileSetter
    public void testPercentileSetter() throws Exception {
        DescriptiveStatistics stats = createDescriptiveStatistics();
        stats.addValue(1);
        stats.addValue(2);
        stats.addValue(3);
        Assert.assertEquals(2, stats.getPercentile(50.0), 1E-10);

        
        stats.setPercentileImpl(new goodPercentile());
        Assert.assertEquals(2, stats.getPercentile(50.0), 1E-10);

        
        stats.setPercentileImpl(new subPercentile());
        Assert.assertEquals(10.0, stats.getPercentile(10.0), 1E-10);

        
        try {
            stats.setPercentileImpl(new badPercentile());
            Assert.fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.stat.descriptive.DescriptiveStatisticsTest::test20090720
    public void test20090720() {
        DescriptiveStatistics descriptiveStatistics = new DescriptiveStatistics(100);
        for (int i = 0; i < 161; i++) {
            descriptiveStatistics.addValue(1.2);
        }
        descriptiveStatistics.clear();
        descriptiveStatistics.addValue(1.2);
        Assert.assertEquals(1, descriptiveStatistics.getN());
    }

// org.apache.commons.math.stat.descriptive.DescriptiveStatisticsTest::testRemoval
    public void testRemoval() {

        final DescriptiveStatistics dstat = createDescriptiveStatistics();

        checkremoval(dstat, 1, 6.0, 0.0, Double.NaN);
        checkremoval(dstat, 3, 5.0, 3.0, 4.5);
        checkremoval(dstat, 6, 3.5, 2.5, 3.0);
        checkremoval(dstat, 9, 3.5, 2.5, 3.0);
        checkremoval(dstat, DescriptiveStatistics.INFINITE_WINDOW, 3.5, 2.5, 3.0);

    }

// org.apache.commons.math.stat.descriptive.DescriptiveStatisticsTest::testSummaryConsistency
    public void testSummaryConsistency() {
        final DescriptiveStatistics dstats = new DescriptiveStatistics();
        final SummaryStatistics sstats = new SummaryStatistics();
        final int windowSize = 5;
        dstats.setWindowSize(windowSize);
        final double tol = 1E-12;
        for (int i = 0; i < 20; i++) {
            dstats.addValue(i);
            sstats.clear();
            double[] values = dstats.getValues();
            for (int j = 0; j < values.length; j++) {
                sstats.addValue(values[j]);
            }
            TestUtils.assertEquals(dstats.getMean(), sstats.getMean(), tol);
            TestUtils.assertEquals(new Mean().evaluate(values), dstats.getMean(), tol);
            TestUtils.assertEquals(dstats.getMax(), sstats.getMax(), tol);
            TestUtils.assertEquals(new Max().evaluate(values), dstats.getMax(), tol);
            TestUtils.assertEquals(dstats.getGeometricMean(), sstats.getGeometricMean(), tol);
            TestUtils.assertEquals(new GeometricMean().evaluate(values), dstats.getGeometricMean(), tol);
            TestUtils.assertEquals(dstats.getMin(), sstats.getMin(), tol);
            TestUtils.assertEquals(new Min().evaluate(values), dstats.getMin(), tol);
            TestUtils.assertEquals(dstats.getStandardDeviation(), sstats.getStandardDeviation(), tol);
            TestUtils.assertEquals(dstats.getVariance(), sstats.getVariance(), tol);
            TestUtils.assertEquals(new Variance().evaluate(values), dstats.getVariance(), tol);
            TestUtils.assertEquals(dstats.getSum(), sstats.getSum(), tol);
            TestUtils.assertEquals(new Sum().evaluate(values), dstats.getSum(), tol);
            TestUtils.assertEquals(dstats.getSumsq(), sstats.getSumsq(), tol);
            TestUtils.assertEquals(new SumOfSquares().evaluate(values), dstats.getSumsq(), tol);
            TestUtils.assertEquals(dstats.getPopulationVariance(), sstats.getPopulationVariance(), tol);
            TestUtils.assertEquals(new Variance(false).evaluate(values), dstats.getPopulationVariance(), tol);
        }
    }

// org.apache.commons.math.stat.descriptive.SummaryStatisticsTest::testStats
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

// org.apache.commons.math.stat.descriptive.SummaryStatisticsTest::testN0andN1Conditions
    public void testN0andN1Conditions() throws Exception {
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

// org.apache.commons.math.stat.descriptive.SummaryStatisticsTest::testProductAndGeometricMean
    public void testProductAndGeometricMean() throws Exception {
        SummaryStatistics u = createSummaryStatistics();
        u.addValue( 1.0 );
        u.addValue( 2.0 );
        u.addValue( 3.0 );
        u.addValue( 4.0 );

        Assert.assertEquals( "Geometric mean not expected", 2.213364,
                u.getGeometricMean(), 0.00001 );
    }

// org.apache.commons.math.stat.descriptive.SummaryStatisticsTest::testNaNContracts
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

// org.apache.commons.math.stat.descriptive.SummaryStatisticsTest::testCopy
    public void testCopy() throws Exception {
        SummaryStatistics u = createSummaryStatistics();
        u.addValue(2d);
        u.addValue(1d);
        u.addValue(3d);
        u.addValue(4d);
        SummaryStatistics v = new SummaryStatistics(u);
        Assert.assertEquals(u, v);
        Assert.assertEquals(v, u);
        Assert.assertTrue(v.geoMean == v.getGeoMeanImpl());
        Assert.assertTrue(v.mean == v.getMeanImpl());
        Assert.assertTrue(v.min == v.getMinImpl());
        Assert.assertTrue(v.max == v.getMaxImpl());
        Assert.assertTrue(v.sum == v.getSumImpl());
        Assert.assertTrue(v.sumsq == v.getSumsqImpl());
        Assert.assertTrue(v.sumLog == v.getSumLogImpl());
        Assert.assertTrue(v.variance == v.getVarianceImpl());

        
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
        Assert.assertEquals(u.sum, v.sum);
        Assert.assertEquals(u.getSumImpl(), v.getSumImpl());

    }

// org.apache.commons.math.stat.descriptive.SummaryStatisticsTest::testSetterInjection
    public void testSetterInjection() throws Exception {
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

// org.apache.commons.math.stat.descriptive.SummaryStatisticsTest::testSetterIllegalState
    public void testSetterIllegalState() throws Exception {
        SummaryStatistics u = createSummaryStatistics();
        u.addValue(1);
        u.addValue(3);
        try {
            u.setMeanImpl(new Sum());
            Assert.fail("Expecting IllegalStateException");
        } catch (IllegalStateException ex) {
            
        }
    }

// org.apache.commons.math.stat.descriptive.SummaryStatisticsTest::testOverrideVarianceWithMathClass
    public void testOverrideVarianceWithMathClass() throws Exception {
        double[] scores = {1, 2, 3, 4};
        SummaryStatistics stats = new SummaryStatistics();
        stats.setVarianceImpl(new Variance(false)); 
        for(double i : scores) {
          stats.addValue(i);
        }
        Assert.assertEquals((new Variance(false)).evaluate(scores),stats.getVariance(), 0); 
    }

// org.apache.commons.math.stat.descriptive.SummaryStatisticsTest::testOverrideMeanWithMathClass
    public void testOverrideMeanWithMathClass() throws Exception {
        double[] scores = {1, 2, 3, 4};
        SummaryStatistics stats = new SummaryStatistics();
        stats.setMeanImpl(new Mean()); 
        for(double i : scores) {
          stats.addValue(i);
        }
        Assert.assertEquals((new Mean()).evaluate(scores),stats.getMean(), 0); 
    }

// org.apache.commons.math.stat.descriptive.SummaryStatisticsTest::testOverrideGeoMeanWithMathClass
    public void testOverrideGeoMeanWithMathClass() throws Exception {
        double[] scores = {1, 2, 3, 4};
        SummaryStatistics stats = new SummaryStatistics();
        stats.setGeoMeanImpl(new GeometricMean()); 
        for(double i : scores) {
          stats.addValue(i);
        }
        Assert.assertEquals((new GeometricMean()).evaluate(scores),stats.getGeometricMean(), 0); 
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
            Assert.fail("arguments too short, MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }

        try {
            testStatistic.t(mu, (SummaryStatistics) null);
            Assert.fail("arguments too short, MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }

        try {
            testStatistic.t(mu, emptyObs);
            Assert.fail("arguments too short, MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }

        try {
            testStatistic.t(mu, emptyStats);
            Assert.fail("arguments too short, MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }

        try {
            testStatistic.t(mu, tooShortObs);
            Assert.fail("insufficient data to compute t statistic, MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }
        try {
            testStatistic.tTest(mu, tooShortObs);
            Assert.fail("insufficient data to perform t test, MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
           
        }

        try {
            testStatistic.t(mu, tooShortStats);
            Assert.fail("insufficient data to compute t statistic, MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }
        try {
            testStatistic.tTest(mu, tooShortStats);
            Assert.fail("insufficient data to perform t test, MathIllegalArgumentException expected");
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
            Assert.fail("alpha out of range, MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }

        try {
            testStatistic.tTest(0d, oneSidedPStats, 95);
            Assert.fail("alpha out of range, MathIllegalArgumentException expected");
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
            Assert.fail("alpha out of range, MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }

        try {
            testStatistic.tTest(sampleStats1, sampleStats2, .95);
            Assert.fail("alpha out of range, MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }

        try {
            testStatistic.tTest(sample1, tooShortObs, .01);
            Assert.fail("insufficient data, MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }

        try {
            testStatistic.tTest(sampleStats1, tooShortStats, .01);
            Assert.fail("insufficient data, MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }

        try {
            testStatistic.tTest(sample1, tooShortObs);
            Assert.fail("insufficient data, MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
           
        }

        try {
            testStatistic.tTest(sampleStats1, tooShortStats);
            Assert.fail("insufficient data, MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }

        try {
            testStatistic.t(sample1, tooShortObs);
            Assert.fail("insufficient data, MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }

        try {
            testStatistic.t(sampleStats1, tooShortStats);
            Assert.fail("insufficient data, MathIllegalArgumentException expected");
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

        
        Assert.assertEquals("two sample homoscedastic t stat", 0.73096310086,
              testStatistic.homoscedasticT(sample1, sample2), 10E-11);
        Assert.assertEquals("two sample homoscedastic p value", 0.4833963785,
                testStatistic.homoscedasticTTest(sampleStats1, sampleStats2), 1E-10);
        Assert.assertTrue("two sample homoscedastic t-test reject",
                testStatistic.homoscedasticTTest(sample1, sample2, 0.49));
        Assert.assertTrue("two sample homoscedastic t-test accept",
                !testStatistic.homoscedasticTTest(sample1, sample2, 0.48));
    }

// org.apache.commons.math.stat.inference.TTestTest::testSmallSamples
    public void testSmallSamples() throws Exception {
        double[] sample1 = {1d, 3d};
        double[] sample2 = {4d, 5d};

        
        Assert.assertEquals(-2.2360679775, testStatistic.t(sample1, sample2),
                1E-10);
        Assert.assertEquals(0.198727388935, testStatistic.tTest(sample1, sample2),
                1E-10);
    }

// org.apache.commons.math.stat.inference.TTestTest::testPaired
    public void testPaired() throws Exception {
        double[] sample1 = {1d, 3d, 5d, 7d};
        double[] sample2 = {0d, 6d, 11d, 2d};
        double[] sample3 = {5d, 7d, 8d, 10d};

        
        Assert.assertEquals(-0.3133, testStatistic.pairedT(sample1, sample2), 1E-4);
        Assert.assertEquals(0.774544295819, testStatistic.pairedTTest(sample1, sample2), 1E-10);
        Assert.assertEquals(0.001208, testStatistic.pairedTTest(sample1, sample3), 1E-6);
        Assert.assertFalse(testStatistic.pairedTTest(sample1, sample3, .001));
        Assert.assertTrue(testStatistic.pairedTTest(sample1, sample3, .002));
    }

// org.apache.commons.math.stat.inference.TestUtilsTest::testChiSquare
    public void testChiSquare() throws Exception {

        
        
        
        

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
            Assert.fail("alpha out of range, MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }

        long[] tooShortObs = { 0 };
        double[] tooShortEx = { 1 };
        try {
            TestUtils.chiSquare(tooShortEx, tooShortObs);
            Assert.fail("arguments too short, MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }

        
        long[] unMatchedObs = { 0, 1, 2, 3 };
        double[] unMatchedEx = { 1, 1, 2 };
        try {
            TestUtils.chiSquare(unMatchedEx, unMatchedObs);
            Assert.fail("arrays have different lengths, MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }

        
        expected[0] = 0;
        try {
            TestUtils.chiSquareTest(expected, observed, .01);
            Assert.fail("bad expected count, MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }

        
        expected[0] = 1;
        observed[0] = -1;
        try {
            TestUtils.chiSquareTest(expected, observed, .01);
            Assert.fail("bad expected count, MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }

    }

// org.apache.commons.math.stat.inference.TestUtilsTest::testChiSquareIndependence
    public void testChiSquareIndependence() throws Exception {

        

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
            Assert.fail("Expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }

        
        long[][] counts4 = {{40, 22, 43}};
        try {
            TestUtils.chiSquare(counts4);
            Assert.fail("Expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
        long[][] counts5 = {{40}, {40}, {30}, {10}};
        try {
            TestUtils.chiSquare(counts5);
            Assert.fail("Expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }

        
        long[][] counts6 = {{10, -2}, {30, 40}, {60, 90} };
        try {
            TestUtils.chiSquare(counts6);
            Assert.fail("Expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }

        
        try {
            TestUtils.chiSquareTest(counts, 0);
            Assert.fail("Expecting MathIllegalArgumentException");
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
        Assert.assertEquals("chi-square p-value", 0.0, cst, 1E-3);
        Assert.assertEquals( "chi-square test statistic",
                114875.90421929007, TestUtils.chiSquare(exp, obs), 1E-9);
    }

// org.apache.commons.math.stat.inference.TestUtilsTest::testChiSquareZeroCount
    public void testChiSquareZeroCount() throws Exception {
        
        long[][] counts = { {40, 0, 4}, {91, 1, 2}, {60, 2, 0}};
        Assert.assertEquals( "chi-square test statistic", 9.67444662263,
                TestUtils.chiSquare(counts), 1E-9);
        Assert.assertEquals("chi-square p-value", 0.0462835770603,
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
            Assert.fail("arguments too short, MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }

        try {
            TestUtils.t(mu, (SummaryStatistics) null);
            Assert.fail("arguments too short, MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }

        try {
            TestUtils.t(mu, emptyObs);
            Assert.fail("arguments too short, MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }

        try {
            TestUtils.t(mu, emptyStats);
            Assert.fail("arguments too short, MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }

        try {
            TestUtils.t(mu, tooShortObs);
            Assert.fail("insufficient data to compute t statistic, MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }
        try {
            TestUtils.tTest(mu, tooShortObs);
            Assert.fail("insufficient data to perform t test, MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }

        try {
            TestUtils.t(mu, (SummaryStatistics) null);
            Assert.fail("insufficient data to compute t statistic, MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }
        try {
            TestUtils.tTest(mu, (SummaryStatistics) null);
            Assert.fail("insufficient data to perform t test, MathIllegalArgumentException expected");
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
            Assert.fail("alpha out of range, MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }

        try {
            TestUtils.tTest(0d, oneSidedPStats, 95);
            Assert.fail("alpha out of range, MathIllegalArgumentException expected");
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
            Assert.fail("alpha out of range, MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }

        try {
            TestUtils.tTest(sampleStats1, sampleStats2, .95);
            Assert.fail("alpha out of range, MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }

        try {
            TestUtils.tTest(sample1, tooShortObs, .01);
            Assert.fail("insufficient data, MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }

        try {
            TestUtils.tTest(sampleStats1, (SummaryStatistics) null, .01);
            Assert.fail("insufficient data, MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }

        try {
            TestUtils.tTest(sample1, tooShortObs);
            Assert.fail("insufficient data, MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }

        try {
            TestUtils.tTest(sampleStats1, (SummaryStatistics) null);
            Assert.fail("insufficient data, MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }

        try {
            TestUtils.t(sample1, tooShortObs);
            Assert.fail("insufficient data, MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }

        try {
            TestUtils.t(sampleStats1, (SummaryStatistics) null);
            Assert.fail("insufficient data, MathIllegalArgumentException expected");
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

        
        Assert.assertEquals("two sample homoscedastic t stat", 0.73096310086,
                TestUtils.homoscedasticT(sample1, sample2), 10E-11);
        Assert.assertEquals("two sample homoscedastic p value", 0.4833963785,
                TestUtils.homoscedasticTTest(sampleStats1, sampleStats2), 1E-10);
        Assert.assertTrue("two sample homoscedastic t-test reject",
                TestUtils.homoscedasticTTest(sample1, sample2, 0.49));
        Assert.assertTrue("two sample homoscedastic t-test accept",
                !TestUtils.homoscedasticTTest(sample1, sample2, 0.48));
    }

// org.apache.commons.math.stat.inference.TestUtilsTest::testSmallSamples
    public void testSmallSamples() throws Exception {
        double[] sample1 = {1d, 3d};
        double[] sample2 = {4d, 5d};

        
        Assert.assertEquals(-2.2360679775, TestUtils.t(sample1, sample2),
                1E-10);
        Assert.assertEquals(0.198727388935, TestUtils.tTest(sample1, sample2),
                1E-10);
    }

// org.apache.commons.math.stat.inference.TestUtilsTest::testPaired
    public void testPaired() throws Exception {
        double[] sample1 = {1d, 3d, 5d, 7d};
        double[] sample2 = {0d, 6d, 11d, 2d};
        double[] sample3 = {5d, 7d, 8d, 10d};

        
        Assert.assertEquals(-0.3133, TestUtils.pairedT(sample1, sample2), 1E-4);
        Assert.assertEquals(0.774544295819, TestUtils.pairedTTest(sample1, sample2), 1E-10);
        Assert.assertEquals(0.001208, TestUtils.pairedTTest(sample1, sample3), 1E-6);
        Assert.assertFalse(TestUtils.pairedTTest(sample1, sample3, .001));
        Assert.assertTrue(TestUtils.pairedTTest(sample1, sample3, .002));
    }

// org.apache.commons.math.stat.inference.TestUtilsTest::testOneWayAnovaUtils
    public void testOneWayAnovaUtils() throws Exception {
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
