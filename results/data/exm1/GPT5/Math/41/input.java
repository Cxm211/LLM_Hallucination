// buggy code
    public double evaluate(final double[] values, final double[] weights,
                           final double mean, final int begin, final int length) {

        double var = Double.NaN;

        if (test(values, weights, begin, length)) {
            if (length == 1) {
                var = 0.0;
            } else if (length > 1) {
                double accum = 0.0;
                double dev = 0.0;
                double accum2 = 0.0;
                for (int i = begin; i < begin + length; i++) {
                    dev = values[i] - mean;
                    accum += weights[i] * (dev * dev);
                    accum2 += weights[i] * dev;
                }

                double sumWts = 0;
                for (int i = 0; i < weights.length; i++) {
                    sumWts += weights[i];
                }

                if (isBiasCorrected) {
                    var = (accum - (accum2 * accum2 / sumWts)) / (sumWts - 1.0);
                } else {
                    var = (accum - (accum2 * accum2 / sumWts)) / sumWts;
                }
            }
        }
        return var;
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

// org.apache.commons.math.optimization.univariate.BrentOptimizerTest::testSinMin
    public void testSinMin() {
        UnivariateFunction f = new SinFunction();
        UnivariateRealOptimizer optimizer = new BrentOptimizer(1e-10, 1e-14);
        Assert.assertEquals(3 * Math.PI / 2, optimizer.optimize(200, f, GoalType.MINIMIZE, 4, 5).getPoint(),1e-8);
        Assert.assertTrue(optimizer.getEvaluations() <= 50);
        Assert.assertEquals(200, optimizer.getMaxEvaluations());
        Assert.assertEquals(3 * Math.PI / 2, optimizer.optimize(200, f, GoalType.MINIMIZE, 1, 5).getPoint(), 1e-8);
        Assert.assertTrue(optimizer.getEvaluations() <= 100);
        Assert.assertTrue(optimizer.getEvaluations() >= 15);
        try {
            optimizer.optimize(10, f, GoalType.MINIMIZE, 4, 5);
            Assert.fail("an exception should have been thrown");
        } catch (TooManyEvaluationsException fee) {
            
        }
    }

// org.apache.commons.math.optimization.univariate.BrentOptimizerTest::testQuinticMin
    public void testQuinticMin() {
        
        UnivariateFunction f = new QuinticFunction();
        UnivariateRealOptimizer optimizer = new BrentOptimizer(1e-10, 1e-14);
        Assert.assertEquals(-0.27195613, optimizer.optimize(200, f, GoalType.MINIMIZE, -0.3, -0.2).getPoint(), 1.0e-8);
        Assert.assertEquals( 0.82221643, optimizer.optimize(200, f, GoalType.MINIMIZE,  0.3,  0.9).getPoint(), 1.0e-8);
        Assert.assertTrue(optimizer.getEvaluations() <= 50);

        
        Assert.assertEquals(-0.27195613, optimizer.optimize(200, f, GoalType.MINIMIZE, -1.0, 0.2).getPoint(), 1.0e-8);
        Assert.assertTrue(optimizer.getEvaluations() <= 50);
    }

// org.apache.commons.math.optimization.univariate.BrentOptimizerTest::testQuinticMinStatistics
    public void testQuinticMinStatistics() {
        
        UnivariateFunction f = new QuinticFunction();
        UnivariateRealOptimizer optimizer = new BrentOptimizer(1e-11, 1e-14);

        final DescriptiveStatistics[] stat = new DescriptiveStatistics[2];
        for (int i = 0; i < stat.length; i++) {
            stat[i] = new DescriptiveStatistics();
        }

        final double min = -0.75;
        final double max = 0.25;
        final int nSamples = 200;
        final double delta = (max - min) / nSamples;
        for (int i = 0; i < nSamples; i++) {
            final double start = min + i * delta;
            stat[0].addValue(optimizer.optimize(40, f, GoalType.MINIMIZE, min, max, start).getPoint());
            stat[1].addValue(optimizer.getEvaluations());
        }

        final double meanOptValue = stat[0].getMean();
        final double medianEval = stat[1].getPercentile(50);
        Assert.assertTrue(meanOptValue > -0.2719561281);
        Assert.assertTrue(meanOptValue < -0.2719561280);
        Assert.assertEquals(23, (int) medianEval);
    }

// org.apache.commons.math.optimization.univariate.BrentOptimizerTest::testQuinticMax
    public void testQuinticMax() {
        
        
        UnivariateFunction f = new QuinticFunction();
        UnivariateRealOptimizer optimizer = new BrentOptimizer(1e-12, 1e-14);
        Assert.assertEquals(0.27195613, optimizer.optimize(100, f, GoalType.MAXIMIZE, 0.2, 0.3).getPoint(), 1e-8);
        try {
            optimizer.optimize(5, f, GoalType.MAXIMIZE, 0.2, 0.3);
            Assert.fail("an exception should have been thrown");
        } catch (TooManyEvaluationsException miee) {
            
        }
    }

// org.apache.commons.math.optimization.univariate.BrentOptimizerTest::testMinEndpoints
    public void testMinEndpoints() {
        UnivariateFunction f = new SinFunction();
        UnivariateRealOptimizer optimizer = new BrentOptimizer(1e-8, 1e-14);

        
        double result = optimizer.optimize(50, f, GoalType.MINIMIZE, 3 * Math.PI / 2, 5).getPoint();
        Assert.assertEquals(3 * Math.PI / 2, result, 1e-6);

        result = optimizer.optimize(50, f, GoalType.MINIMIZE, 4, 3 * Math.PI / 2).getPoint();
        Assert.assertEquals(3 * Math.PI / 2, result, 1e-6);
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

// org.apache.commons.math.stat.StatUtilsTest::testStats
    public void testStats() {
        double[] values = new double[] { one, two, two, three };
        Assert.assertEquals("sum", sum, StatUtils.sum(values), tolerance);
        Assert.assertEquals("sumsq", sumSq, StatUtils.sumSq(values), tolerance);
        Assert.assertEquals("var", var, StatUtils.variance(values), tolerance);
        Assert.assertEquals("var with mean", var, StatUtils.variance(values, mean), tolerance);
        Assert.assertEquals("mean", mean, StatUtils.mean(values), tolerance);
        Assert.assertEquals("min", min, StatUtils.min(values), tolerance);
        Assert.assertEquals("max", max, StatUtils.max(values), tolerance);
    }

// org.apache.commons.math.stat.StatUtilsTest::testN0andN1Conditions
    public void testN0andN1Conditions() throws Exception {
        double[] values = new double[0];

        Assert.assertTrue(
            "Mean of n = 0 set should be NaN",
            Double.isNaN(StatUtils.mean(values)));
        Assert.assertTrue(
            "Variance of n = 0 set should be NaN",
            Double.isNaN(StatUtils.variance(values)));

        values = new double[] { one };

        Assert.assertTrue(
            "Mean of n = 1 set should be value of single item n1",
            StatUtils.mean(values) == one);
        Assert.assertTrue(
            "Variance of n = 1 set should be zero",
            StatUtils.variance(values) == 0);
    }

// org.apache.commons.math.stat.StatUtilsTest::testArrayIndexConditions
    public void testArrayIndexConditions() throws Exception {
        double[] values = { 1.0, 2.0, 3.0, 4.0 };

        Assert.assertEquals(
            "Sum not expected",
            5.0,
            StatUtils.sum(values, 1, 2),
            Double.MIN_VALUE);
        Assert.assertEquals(
            "Sum not expected",
            3.0,
            StatUtils.sum(values, 0, 2),
            Double.MIN_VALUE);
        Assert.assertEquals(
            "Sum not expected",
            7.0,
            StatUtils.sum(values, 2, 2),
            Double.MIN_VALUE);

        try {
            StatUtils.sum(values, 2, 3);
            Assert.fail("Expected RuntimeException");
        } catch (RuntimeException e) {
            
        }

        try {
            StatUtils.sum(values, -1, 2);
            Assert.fail("Expected RuntimeException");
        } catch (RuntimeException e) {
            
        }

    }

// org.apache.commons.math.stat.StatUtilsTest::testSumSq
    public void testSumSq() {
        double[] x = null;

        
        try {
            StatUtils.sumSq(x);
            Assert.fail("null is not a valid data array.");
        } catch (MathIllegalArgumentException ex) {
            
        }

        try {
            StatUtils.sumSq(x, 0, 4);
            Assert.fail("null is not a valid data array.");
        } catch (MathIllegalArgumentException ex) {
            
        }

        
        x = new double[] {};
        TestUtils.assertEquals(0, StatUtils.sumSq(x), tolerance);
        TestUtils.assertEquals(0, StatUtils.sumSq(x, 0, 0), tolerance);

        
        x = new double[] {two};
        TestUtils.assertEquals(4, StatUtils.sumSq(x), tolerance);
        TestUtils.assertEquals(4, StatUtils.sumSq(x, 0, 1), tolerance);

        
        x = new double[] {one, two, two, three};
        TestUtils.assertEquals(18, StatUtils.sumSq(x), tolerance);
        TestUtils.assertEquals(8, StatUtils.sumSq(x, 1, 2), tolerance);
    }

// org.apache.commons.math.stat.StatUtilsTest::testProduct
    public void testProduct() {
        double[] x = null;

        
        try {
            StatUtils.product(x);
            Assert.fail("null is not a valid data array.");
        } catch (MathIllegalArgumentException ex) {
            
        }

        try {
            StatUtils.product(x, 0, 4);
            Assert.fail("null is not a valid data array.");
        } catch (MathIllegalArgumentException ex) {
            
        }

        
        x = new double[] {};
        TestUtils.assertEquals(1, StatUtils.product(x), tolerance);
        TestUtils.assertEquals(1, StatUtils.product(x, 0, 0), tolerance);

        
        x = new double[] {two};
        TestUtils.assertEquals(two, StatUtils.product(x), tolerance);
        TestUtils.assertEquals(two, StatUtils.product(x, 0, 1), tolerance);

        
        x = new double[] {one, two, two, three};
        TestUtils.assertEquals(12, StatUtils.product(x), tolerance);
        TestUtils.assertEquals(4, StatUtils.product(x, 1, 2), tolerance);
    }

// org.apache.commons.math.stat.StatUtilsTest::testSumLog
    public void testSumLog() {
        double[] x = null;

        
        try {
            StatUtils.sumLog(x);
            Assert.fail("null is not a valid data array.");
        } catch (MathIllegalArgumentException ex) {
            
        }

        try {
            StatUtils.sumLog(x, 0, 4);
            Assert.fail("null is not a valid data array.");
        } catch (MathIllegalArgumentException ex) {
            
        }

        
        x = new double[] {};
        TestUtils.assertEquals(0, StatUtils.sumLog(x), tolerance);
        TestUtils.assertEquals(0, StatUtils.sumLog(x, 0, 0), tolerance);

        
        x = new double[] {two};
        TestUtils.assertEquals(FastMath.log(two), StatUtils.sumLog(x), tolerance);
        TestUtils.assertEquals(FastMath.log(two), StatUtils.sumLog(x, 0, 1), tolerance);

        
        x = new double[] {one, two, two, three};
        TestUtils.assertEquals(FastMath.log(one) + 2.0 * FastMath.log(two) + FastMath.log(three), StatUtils.sumLog(x), tolerance);
        TestUtils.assertEquals(2.0 * FastMath.log(two), StatUtils.sumLog(x, 1, 2), tolerance);
    }

// org.apache.commons.math.stat.StatUtilsTest::testMean
    public void testMean() {
        double[] x = null;

        try {
            StatUtils.mean(x, 0, 4);
            Assert.fail("null is not a valid data array.");
        } catch (MathIllegalArgumentException ex) {
            
        }

        
        x = new double[] {};
        TestUtils.assertEquals(Double.NaN, StatUtils.mean(x, 0, 0), tolerance);

        
        x = new double[] {two};
        TestUtils.assertEquals(two, StatUtils.mean(x, 0, 1), tolerance);

        
        x = new double[] {one, two, two, three};
        TestUtils.assertEquals(2.5, StatUtils.mean(x, 2, 2), tolerance);
    }

// org.apache.commons.math.stat.StatUtilsTest::testVariance
    public void testVariance() {
        double[] x = null;

        try {
            StatUtils.variance(x, 0, 4);
            Assert.fail("null is not a valid data array.");
        } catch (MathIllegalArgumentException ex) {
            
        }

        
        x = new double[] {};
        TestUtils.assertEquals(Double.NaN, StatUtils.variance(x, 0, 0), tolerance);

        
        x = new double[] {two};
        TestUtils.assertEquals(0.0, StatUtils.variance(x, 0, 1), tolerance);

        
        x = new double[] {one, two, two, three};
        TestUtils.assertEquals(0.5, StatUtils.variance(x, 2, 2), tolerance);

        
        x = new double[] {one, two, two, three};
        TestUtils.assertEquals(0.5, StatUtils.variance(x,2.5, 2, 2), tolerance);
    }

// org.apache.commons.math.stat.StatUtilsTest::testPopulationVariance
    public void testPopulationVariance() {
        double[] x = null;

        try {
            StatUtils.variance(x, 0, 4);
            Assert.fail("null is not a valid data array.");
        } catch (MathIllegalArgumentException ex) {
            
        }

        
        x = new double[] {};
        TestUtils.assertEquals(Double.NaN, StatUtils.populationVariance(x, 0, 0), tolerance);

        
        x = new double[] {two};
        TestUtils.assertEquals(0.0, StatUtils.populationVariance(x, 0, 1), tolerance);

        
        x = new double[] {one, two, two, three};
        TestUtils.assertEquals(0.25, StatUtils.populationVariance(x, 0, 2), tolerance);

        
        x = new double[] {one, two, two, three};
        TestUtils.assertEquals(0.25, StatUtils.populationVariance(x, 2.5, 2, 2), tolerance);
    }

// org.apache.commons.math.stat.StatUtilsTest::testMax
    public void testMax() {
        double[] x = null;

        try {
            StatUtils.max(x, 0, 4);
            Assert.fail("null is not a valid data array.");
        } catch (MathIllegalArgumentException ex) {
            
        }

        
        x = new double[] {};
        TestUtils.assertEquals(Double.NaN, StatUtils.max(x, 0, 0), tolerance);

        
        x = new double[] {two};
        TestUtils.assertEquals(two, StatUtils.max(x, 0, 1), tolerance);

        
        x = new double[] {one, two, two, three};
        TestUtils.assertEquals(three, StatUtils.max(x, 1, 3), tolerance);

        
        x = new double[] {nan, two, three};
        TestUtils.assertEquals(three, StatUtils.max(x), tolerance);

        
        x = new double[] {one, nan, three};
        TestUtils.assertEquals(three, StatUtils.max(x), tolerance);

        
        x = new double[] {one, two, nan};
        TestUtils.assertEquals(two, StatUtils.max(x), tolerance);

        
        x = new double[] {nan, nan, nan};
        TestUtils.assertEquals(nan, StatUtils.max(x), tolerance);
    }

// org.apache.commons.math.stat.StatUtilsTest::testMin
    public void testMin() {
        double[] x = null;

        try {
            StatUtils.min(x, 0, 4);
            Assert.fail("null is not a valid data array.");
        } catch (MathIllegalArgumentException ex) {
            
        }

        
        x = new double[] {};
        TestUtils.assertEquals(Double.NaN, StatUtils.min(x, 0, 0), tolerance);

        
        x = new double[] {two};
        TestUtils.assertEquals(two, StatUtils.min(x, 0, 1), tolerance);

        
        x = new double[] {one, two, two, three};
        TestUtils.assertEquals(two, StatUtils.min(x, 1, 3), tolerance);

        
        x = new double[] {nan, two, three};
        TestUtils.assertEquals(two, StatUtils.min(x), tolerance);

        
        x = new double[] {one, nan, three};
        TestUtils.assertEquals(one, StatUtils.min(x), tolerance);

        
        x = new double[] {one, two, nan};
        TestUtils.assertEquals(one, StatUtils.min(x), tolerance);

        
        x = new double[] {nan, nan, nan};
        TestUtils.assertEquals(nan, StatUtils.min(x), tolerance);
    }

// org.apache.commons.math.stat.StatUtilsTest::testPercentile
    public void testPercentile() {
        double[] x = null;

        
        try {
            StatUtils.percentile(x, .25);
            Assert.fail("null is not a valid data array.");
        } catch (MathIllegalArgumentException ex) {
            
        }

        try {
            StatUtils.percentile(x, 0, 4, 0.25);
            Assert.fail("null is not a valid data array.");
        } catch (MathIllegalArgumentException ex) {
            
        }

        
        x = new double[] {};
        TestUtils.assertEquals(Double.NaN, StatUtils.percentile(x, 25), tolerance);
        TestUtils.assertEquals(Double.NaN, StatUtils.percentile(x, 0, 0, 25), tolerance);

        
        x = new double[] {two};
        TestUtils.assertEquals(two, StatUtils.percentile(x, 25), tolerance);
        TestUtils.assertEquals(two, StatUtils.percentile(x, 0, 1, 25), tolerance);

        
        x = new double[] {one, two, two, three};
        TestUtils.assertEquals(2.5, StatUtils.percentile(x, 70), tolerance);
        TestUtils.assertEquals(2.5, StatUtils.percentile(x, 1, 3, 62.5), tolerance);
    }

// org.apache.commons.math.stat.StatUtilsTest::testDifferenceStats
    public void testDifferenceStats() throws Exception {
        double sample1[] = {1d, 2d, 3d, 4d};
        double sample2[] = {1d, 3d, 4d, 2d};
        double diff[] = {0d, -1d, -1d, 2d};
        double small[] = {1d, 4d};
        double meanDifference = StatUtils.meanDifference(sample1, sample2);
        Assert.assertEquals(StatUtils.sumDifference(sample1, sample2), StatUtils.sum(diff), tolerance);
        Assert.assertEquals(meanDifference, StatUtils.mean(diff), tolerance);
        Assert.assertEquals(StatUtils.varianceDifference(sample1, sample2, meanDifference),
                StatUtils.variance(diff), tolerance);
        try {
            StatUtils.meanDifference(sample1, small);
            Assert.fail("Expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
        try {
            StatUtils.varianceDifference(sample1, small, meanDifference);
            Assert.fail("Expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
        try {
            double[] single = {1.0};
            StatUtils.varianceDifference(single, single, meanDifference);
            Assert.fail("Expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.stat.StatUtilsTest::testGeometricMean
    public void testGeometricMean() throws Exception {
        double[] test = null;
        try {
            StatUtils.geometricMean(test);
            Assert.fail("Expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
        test = new double[] {2, 4, 6, 8};
        Assert.assertEquals(FastMath.exp(0.25d * StatUtils.sumLog(test)),
                StatUtils.geometricMean(test), Double.MIN_VALUE);
        Assert.assertEquals(FastMath.exp(0.5 * StatUtils.sumLog(test, 0, 2)),
                StatUtils.geometricMean(test, 0, 2), Double.MIN_VALUE);
    }

// org.apache.commons.math.stat.StatUtilsTest::testNormalize1
    public void testNormalize1() {
        double sample[] = { 50, 100 };
        double expectedSample[] = { -25 / Math.sqrt(1250), 25 / Math.sqrt(1250) };
        double[] out = StatUtils.normalize(sample);
        for (int i = 0; i < out.length; i++) {
            Assert.assertTrue(Precision.equals(out[i], expectedSample[i], 1));
        }

    }

// org.apache.commons.math.stat.StatUtilsTest::testNormalize2
    public void testNormalize2() {
        
        int length = 77;
        double sample[] = new double[length];
        for (int i = 0; i < length; i++) {
            sample[i] = Math.random();
        }
        
        double standardizedSample[] = StatUtils.normalize(sample);

        DescriptiveStatistics stats = new DescriptiveStatistics();
        
        for (int i = 0; i < length; i++) {
            stats.addValue(standardizedSample[i]);
        }
        
        double distance = 1E-10;
        
        Assert.assertEquals(0.0, stats.getMean(), distance);
        Assert.assertEquals(1.0, stats.getStandardDeviation(), distance);

    }

// org.apache.commons.math.stat.clustering.KMeansPlusPlusClustererTest::dimension2
    public void dimension2() {
        KMeansPlusPlusClusterer<EuclideanIntegerPoint> transformer =
            new KMeansPlusPlusClusterer<EuclideanIntegerPoint>(new Random(1746432956321l));
        EuclideanIntegerPoint[] points = new EuclideanIntegerPoint[] {

                
                new EuclideanIntegerPoint(new int[] { -15,  3 }),
                new EuclideanIntegerPoint(new int[] { -15,  4 }),
                new EuclideanIntegerPoint(new int[] { -15,  5 }),
                new EuclideanIntegerPoint(new int[] { -14,  3 }),
                new EuclideanIntegerPoint(new int[] { -14,  5 }),
                new EuclideanIntegerPoint(new int[] { -13,  3 }),
                new EuclideanIntegerPoint(new int[] { -13,  4 }),
                new EuclideanIntegerPoint(new int[] { -13,  5 }),

                
                new EuclideanIntegerPoint(new int[] { -1,  0 }),
                new EuclideanIntegerPoint(new int[] { -1, -1 }),
                new EuclideanIntegerPoint(new int[] {  0, -1 }),
                new EuclideanIntegerPoint(new int[] {  1, -1 }),
                new EuclideanIntegerPoint(new int[] {  1, -2 }),

                
                new EuclideanIntegerPoint(new int[] { 13,  3 }),
                new EuclideanIntegerPoint(new int[] { 13,  4 }),
                new EuclideanIntegerPoint(new int[] { 14,  4 }),
                new EuclideanIntegerPoint(new int[] { 14,  7 }),
                new EuclideanIntegerPoint(new int[] { 16,  5 }),
                new EuclideanIntegerPoint(new int[] { 16,  6 }),
                new EuclideanIntegerPoint(new int[] { 17,  4 }),
                new EuclideanIntegerPoint(new int[] { 17,  7 })

        };
        List<Cluster<EuclideanIntegerPoint>> clusters =
            transformer.cluster(Arrays.asList(points), 3, 5, 10);

        Assert.assertEquals(3, clusters.size());
        boolean cluster1Found = false;
        boolean cluster2Found = false;
        boolean cluster3Found = false;
        for (Cluster<EuclideanIntegerPoint> cluster : clusters) {
            int[] center = cluster.getCenter().getPoint();
            if (center[0] < 0) {
                cluster1Found = true;
                Assert.assertEquals(8, cluster.getPoints().size());
                Assert.assertEquals(-14, center[0]);
                Assert.assertEquals( 4, center[1]);
            } else if (center[1] < 0) {
                cluster2Found = true;
                Assert.assertEquals(5, cluster.getPoints().size());
                Assert.assertEquals( 0, center[0]);
                Assert.assertEquals(-1, center[1]);
            } else {
                cluster3Found = true;
                Assert.assertEquals(8, cluster.getPoints().size());
                Assert.assertEquals(15, center[0]);
                Assert.assertEquals(5, center[1]);
            }
        }
        Assert.assertTrue(cluster1Found);
        Assert.assertTrue(cluster2Found);
        Assert.assertTrue(cluster3Found);

    }

// org.apache.commons.math.stat.clustering.KMeansPlusPlusClustererTest::testPerformClusterAnalysisDegenerate
    public void testPerformClusterAnalysisDegenerate() {
        KMeansPlusPlusClusterer<EuclideanIntegerPoint> transformer = new KMeansPlusPlusClusterer<EuclideanIntegerPoint>(
                new Random(1746432956321l));
        EuclideanIntegerPoint[] points = new EuclideanIntegerPoint[] {
                new EuclideanIntegerPoint(new int[] { 1959, 325100 }),
                new EuclideanIntegerPoint(new int[] { 1960, 373200 }), };
        List<Cluster<EuclideanIntegerPoint>> clusters = transformer.cluster(Arrays.asList(points), 1, 1);
        Assert.assertEquals(1, clusters.size());
        Assert.assertEquals(2, (clusters.get(0).getPoints().size()));
        EuclideanIntegerPoint pt1 = new EuclideanIntegerPoint(new int[] { 1959, 325100 });
        EuclideanIntegerPoint pt2 = new EuclideanIntegerPoint(new int[] { 1960, 373200 });
        Assert.assertTrue(clusters.get(0).getPoints().contains(pt1));
        Assert.assertTrue(clusters.get(0).getPoints().contains(pt2));

    }

// org.apache.commons.math.stat.clustering.KMeansPlusPlusClustererTest::testCertainSpace
    public void testCertainSpace() {
        KMeansPlusPlusClusterer.EmptyClusterStrategy[] strategies = {
            KMeansPlusPlusClusterer.EmptyClusterStrategy.LARGEST_VARIANCE,
            KMeansPlusPlusClusterer.EmptyClusterStrategy.LARGEST_POINTS_NUMBER,
            KMeansPlusPlusClusterer.EmptyClusterStrategy.FARTHEST_POINT
        };
        for (KMeansPlusPlusClusterer.EmptyClusterStrategy strategy : strategies) {
            KMeansPlusPlusClusterer<EuclideanIntegerPoint> transformer =
                new KMeansPlusPlusClusterer<EuclideanIntegerPoint>(new Random(1746432956321l), strategy);
            int numberOfVariables = 27;
            
            int position1 = 1;
            int position2 = position1 + numberOfVariables;
            int position3 = position2 + numberOfVariables;
            int position4 = position3 + numberOfVariables;
            
            int multiplier = 1000000;

            EuclideanIntegerPoint[] breakingPoints = new EuclideanIntegerPoint[numberOfVariables];
            
            for (int i = 0; i < numberOfVariables; i++) {
                int points[] = { position1, position2, position3, position4 };
                
                for (int j = 0; j < points.length; j++) {
                    points[j] = points[j] * multiplier;
                }
                EuclideanIntegerPoint euclideanIntegerPoint = new EuclideanIntegerPoint(points);
                breakingPoints[i] = euclideanIntegerPoint;
                position1 = position1 + numberOfVariables;
                position2 = position2 + numberOfVariables;
                position3 = position3 + numberOfVariables;
                position4 = position4 + numberOfVariables;
            }

            for (int n = 2; n < 27; ++n) {
                List<Cluster<EuclideanIntegerPoint>> clusters =
                    transformer.cluster(Arrays.asList(breakingPoints), n, 100);
                Assert.assertEquals(n, clusters.size());
                int sum = 0;
                for (Cluster<EuclideanIntegerPoint> cluster : clusters) {
                    sum += cluster.getPoints().size();
                }
                Assert.assertEquals(numberOfVariables, sum);
            }
        }

    }

// org.apache.commons.math.stat.clustering.KMeansPlusPlusClustererTest::testSmallDistances
    public void testSmallDistances() {
        
        
        int[] repeatedArray = { 0 };
        int[] uniqueArray = { 1 };
        CloseIntegerPoint repeatedPoint =
            new CloseIntegerPoint(new EuclideanIntegerPoint(repeatedArray));
        CloseIntegerPoint uniquePoint =
            new CloseIntegerPoint(new EuclideanIntegerPoint(uniqueArray));

        Collection<CloseIntegerPoint> points = new ArrayList<CloseIntegerPoint>();
        final int NUM_REPEATED_POINTS = 10 * 1000;
        for (int i = 0; i < NUM_REPEATED_POINTS; ++i) {
            points.add(repeatedPoint);
        }
        points.add(uniquePoint);

        
        
        final long RANDOM_SEED = 0;
        final int NUM_CLUSTERS = 2;
        final int NUM_ITERATIONS = 0;
        KMeansPlusPlusClusterer<CloseIntegerPoint> clusterer =
            new KMeansPlusPlusClusterer<CloseIntegerPoint>(new Random(RANDOM_SEED));
        List<Cluster<CloseIntegerPoint>> clusters =
            clusterer.cluster(points, NUM_CLUSTERS, NUM_ITERATIONS);

        
        boolean uniquePointIsCenter = false;
        for (Cluster<CloseIntegerPoint> cluster : clusters) {
            if (cluster.getCenter().equals(uniquePoint)) {
                uniquePointIsCenter = true;
            }
        }
        Assert.assertTrue(uniquePointIsCenter);
    }

// org.apache.commons.math.stat.clustering.KMeansPlusPlusClustererTest::testPerformClusterAnalysisToManyClusters
    public void testPerformClusterAnalysisToManyClusters() {
        KMeansPlusPlusClusterer<EuclideanIntegerPoint> transformer = 
            new KMeansPlusPlusClusterer<EuclideanIntegerPoint>(
                    new Random(1746432956321l));
        
        EuclideanIntegerPoint[] points = new EuclideanIntegerPoint[] {
            new EuclideanIntegerPoint(new int[] {
                1959, 325100
            }), new EuclideanIntegerPoint(new int[] {
                1960, 373200
            })
        };
        
        transformer.cluster(Arrays.asList(points), 3, 1);

    }

// org.apache.commons.math.stat.correlation.CovarianceTest::testLongly
    public void testLongly() {
        RealMatrix matrix = createRealMatrix(longleyData, 16, 7);
        RealMatrix covarianceMatrix = new Covariance(matrix).getCovarianceMatrix();
        double[] rData = new double[] {
         12333921.73333333246, 3.679666000000000e+04, 343330206.333333313,
         1649102.666666666744, 1117681.066666666651, 23461965.733333334, 16240.93333333333248,
         36796.66000000000, 1.164576250000000e+02, 1063604.115416667,
         6258.666250000000, 3490.253750000000, 73503.000000000, 50.92333333333334,
         343330206.33333331347, 1.063604115416667e+06, 9879353659.329166412,
         56124369.854166664183, 30880428.345833335072, 685240944.600000024, 470977.90000000002328,
         1649102.66666666674, 6.258666250000000e+03, 56124369.854166664,
         873223.429166666698, -115378.762499999997, 4462741.533333333, 2973.03333333333330,
         1117681.06666666665, 3.490253750000000e+03, 30880428.345833335,
         -115378.762499999997, 484304.095833333326, 1764098.133333333, 1382.43333333333339,
         23461965.73333333433, 7.350300000000000e+04, 685240944.600000024,
         4462741.533333333209, 1764098.133333333302, 48387348.933333330, 32917.40000000000146,
         16240.93333333333, 5.092333333333334e+01, 470977.900000000,
         2973.033333333333, 1382.433333333333, 32917.40000000, 22.66666666666667
        };

        TestUtils.assertEquals("covariance matrix", createRealMatrix(rData, 7, 7), covarianceMatrix, 10E-9);

    }

// org.apache.commons.math.stat.correlation.CovarianceTest::testSwissFertility
    public void testSwissFertility() {
         RealMatrix matrix = createRealMatrix(swissData, 47, 5);
         RealMatrix covarianceMatrix = new Covariance(matrix).getCovarianceMatrix();
         double[] rData = new double[] {
           156.0424976873265, 100.1691489361702, -64.36692876965772, -79.7295097132285, 241.5632030527289,
           100.169148936170251, 515.7994172062905, -124.39283071230344, -139.6574005550416, 379.9043755781684,
           -64.3669287696577, -124.3928307123034, 63.64662349676226, 53.5758556891767, -190.5606105457909,
           -79.7295097132285, -139.6574005550416, 53.57585568917669, 92.4560592044403, -61.6988297872340,
            241.5632030527289, 379.9043755781684, -190.56061054579092, -61.6988297872340, 1739.2945371877890
         };

         TestUtils.assertEquals("covariance matrix", createRealMatrix(rData, 5, 5), covarianceMatrix, 10E-13);
    }

// org.apache.commons.math.stat.correlation.CovarianceTest::testConstant
    public void testConstant() {
        double[] noVariance = new double[] {1, 1, 1, 1};
        double[] values = new double[] {1, 2, 3, 4};
        Assert.assertEquals(0d, new Covariance().covariance(noVariance, values, true), Double.MIN_VALUE);
        Assert.assertEquals(0d, new Covariance().covariance(noVariance, noVariance, true), Double.MIN_VALUE);
    }

// org.apache.commons.math.stat.correlation.CovarianceTest::testInsufficientData
    public void testInsufficientData() {
        double[] one = new double[] {1};
        double[] two = new double[] {2};
        try {
            new Covariance().covariance(one, two, false);
            Assert.fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }
        RealMatrix matrix = new Array2DRowRealMatrix(new double[][] {{0},{1}});
        try {
            new Covariance(matrix);
            Assert.fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.stat.correlation.CovarianceTest::testConsistency
    public void testConsistency() {
        final RealMatrix matrix = createRealMatrix(swissData, 47, 5);
        final RealMatrix covarianceMatrix = new Covariance(matrix).getCovarianceMatrix();

        
        Variance variance = new Variance();
        for (int i = 0; i < 5; i++) {
            Assert.assertEquals(variance.evaluate(matrix.getColumn(i)), covarianceMatrix.getEntry(i,i), 10E-14);
        }

        
        Assert.assertEquals(covarianceMatrix.getEntry(2, 3),
                new Covariance().covariance(matrix.getColumn(2), matrix.getColumn(3), true), 10E-14);
        Assert.assertEquals(covarianceMatrix.getEntry(2, 3), covarianceMatrix.getEntry(3, 2), Double.MIN_VALUE);

        
        RealMatrix repeatedColumns = new Array2DRowRealMatrix(47, 3);
        for (int i = 0; i < 3; i++) {
            repeatedColumns.setColumnMatrix(i, matrix.getColumnMatrix(0));
        }
        RealMatrix repeatedCovarianceMatrix = new Covariance(repeatedColumns).getCovarianceMatrix();
        double columnVariance = variance.evaluate(matrix.getColumn(0));
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                Assert.assertEquals(columnVariance, repeatedCovarianceMatrix.getEntry(i, j), 10E-14);
            }
        }

        
        double[][] data = matrix.getData();
        TestUtils.assertEquals("Covariances",
                covarianceMatrix, new Covariance().computeCovarianceMatrix(data),Double.MIN_VALUE);
        TestUtils.assertEquals("Covariances",
                covarianceMatrix, new Covariance().computeCovarianceMatrix(data, true),Double.MIN_VALUE);

        double[] x = data[0];
        double[] y = data[1];
        Assert.assertEquals(new Covariance().covariance(x, y),
                new Covariance().covariance(x, y, true), Double.MIN_VALUE);
    }

// org.apache.commons.math.stat.correlation.PearsonsCorrelationTest::testLongly
    public void testLongly() throws Exception {
        RealMatrix matrix = createRealMatrix(longleyData, 16, 7);
        PearsonsCorrelation corrInstance = new PearsonsCorrelation(matrix);
        RealMatrix correlationMatrix = corrInstance.getCorrelationMatrix();
        double[] rData = new double[] {
                1.000000000000000, 0.9708985250610560, 0.9835516111796693, 0.5024980838759942,
                0.4573073999764817, 0.960390571594376, 0.9713294591921188,
                0.970898525061056, 1.0000000000000000, 0.9915891780247822, 0.6206333925590966,
                0.4647441876006747, 0.979163432977498, 0.9911491900672053,
                0.983551611179669, 0.9915891780247822, 1.0000000000000000, 0.6042609398895580,
                0.4464367918926265, 0.991090069458478, 0.9952734837647849,
                0.502498083875994, 0.6206333925590966, 0.6042609398895580, 1.0000000000000000,
                -0.1774206295018783, 0.686551516365312, 0.6682566045621746,
                0.457307399976482, 0.4647441876006747, 0.4464367918926265, -0.1774206295018783,
                1.0000000000000000, 0.364416267189032, 0.4172451498349454,
                0.960390571594376, 0.9791634329774981, 0.9910900694584777, 0.6865515163653120,
                0.3644162671890320, 1.000000000000000, 0.9939528462329257,
                0.971329459192119, 0.9911491900672053, 0.9952734837647849, 0.6682566045621746,
                0.4172451498349454, 0.993952846232926, 1.0000000000000000
        };
        TestUtils.assertEquals("correlation matrix", createRealMatrix(rData, 7, 7), correlationMatrix, 10E-15);

        double[] rPvalues = new double[] {
                4.38904690369668e-10,
                8.36353208910623e-12, 7.8159700933611e-14,
                0.0472894097790304, 0.01030636128354301, 0.01316878049026582,
                0.0749178049642416, 0.06971758330341182, 0.0830166169296545, 0.510948586323452,
                3.693245043123738e-09, 4.327782576751815e-11, 1.167954621905665e-13, 0.00331028281967516, 0.1652293725106684,
                3.95834476307755e-10, 1.114663916723657e-13, 1.332267629550188e-15, 0.00466039138541463, 0.1078477071581498, 7.771561172376096e-15
        };
        RealMatrix rPMatrix = createLowerTriangularRealMatrix(rPvalues, 7);
        fillUpper(rPMatrix, 0d);
        TestUtils.assertEquals("correlation p values", rPMatrix, corrInstance.getCorrelationPValues(), 10E-15);
    }

// org.apache.commons.math.stat.correlation.PearsonsCorrelationTest::testSwissFertility
    public void testSwissFertility() throws Exception {
         RealMatrix matrix = createRealMatrix(swissData, 47, 5);
         PearsonsCorrelation corrInstance = new PearsonsCorrelation(matrix);
         RealMatrix correlationMatrix = corrInstance.getCorrelationMatrix();
         double[] rData = new double[] {
               1.0000000000000000, 0.3530791836199747, -0.6458827064572875, -0.6637888570350691,  0.4636847006517939,
                 0.3530791836199747, 1.0000000000000000,-0.6865422086171366, -0.6395225189483201, 0.4010950530487398,
                -0.6458827064572875, -0.6865422086171366, 1.0000000000000000, 0.6984152962884830, -0.5727418060641666,
                -0.6637888570350691, -0.6395225189483201, 0.6984152962884830, 1.0000000000000000, -0.1538589170909148,
                 0.4636847006517939, 0.4010950530487398, -0.5727418060641666, -0.1538589170909148, 1.0000000000000000
         };
         TestUtils.assertEquals("correlation matrix", createRealMatrix(rData, 5, 5), correlationMatrix, 10E-15);

         double[] rPvalues = new double[] {
                 0.01491720061472623,
                 9.45043734069043e-07, 9.95151527133974e-08,
                 3.658616965962355e-07, 1.304590105694471e-06, 4.811397236181847e-08,
                 0.001028523190118147, 0.005204433539191644, 2.588307925380906e-05, 0.301807756132683
         };
         RealMatrix rPMatrix = createLowerTriangularRealMatrix(rPvalues, 5);
         fillUpper(rPMatrix, 0d);
         TestUtils.assertEquals("correlation p values", rPMatrix, corrInstance.getCorrelationPValues(), 10E-15);
    }

// org.apache.commons.math.stat.correlation.PearsonsCorrelationTest::testPValueNearZero
    public void testPValueNearZero() throws Exception {
        
        int dimension = 120;
        double[][] data = new double[dimension][2];
        for (int i = 0; i < dimension; i++) {
            data[i][0] = i;
            data[i][1] = i + 1/((double)i + 1);
        }
        PearsonsCorrelation corrInstance = new PearsonsCorrelation(data);
        Assert.assertTrue(corrInstance.getCorrelationPValues().getEntry(0, 1) > 0);
    }

// org.apache.commons.math.stat.correlation.PearsonsCorrelationTest::testConstant
    public void testConstant() {
        double[] noVariance = new double[] {1, 1, 1, 1};
        double[] values = new double[] {1, 2, 3, 4};
        Assert.assertTrue(Double.isNaN(new PearsonsCorrelation().correlation(noVariance, values)));
    }

// org.apache.commons.math.stat.correlation.PearsonsCorrelationTest::testInsufficientData
    public void testInsufficientData() {
        double[] one = new double[] {1};
        double[] two = new double[] {2};
        try {
            new PearsonsCorrelation().correlation(one, two);
            Assert.fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }
        RealMatrix matrix = new BlockRealMatrix(new double[][] {{0},{1}});
        try {
            new PearsonsCorrelation(matrix);
            Assert.fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.stat.correlation.PearsonsCorrelationTest::testStdErrorConsistency
    public void testStdErrorConsistency() throws Exception {
        TDistribution tDistribution = new TDistribution(45);
        RealMatrix matrix = createRealMatrix(swissData, 47, 5);
        PearsonsCorrelation corrInstance = new PearsonsCorrelation(matrix);
        RealMatrix rValues = corrInstance.getCorrelationMatrix();
        RealMatrix pValues = corrInstance.getCorrelationPValues();
        RealMatrix stdErrors = corrInstance.getCorrelationStandardErrors();
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < i; j++) {
                double t = FastMath.abs(rValues.getEntry(i, j)) / stdErrors.getEntry(i, j);
                double p = 2 * (1 - tDistribution.cumulativeProbability(t));
                Assert.assertEquals(p, pValues.getEntry(i, j), 10E-15);
            }
        }
    }

// org.apache.commons.math.stat.correlation.PearsonsCorrelationTest::testCovarianceConsistency
    public void testCovarianceConsistency() throws Exception {
        RealMatrix matrix = createRealMatrix(longleyData, 16, 7);
        PearsonsCorrelation corrInstance = new PearsonsCorrelation(matrix);
        Covariance covInstance = new Covariance(matrix);
        PearsonsCorrelation corrFromCovInstance = new PearsonsCorrelation(covInstance);
        TestUtils.assertEquals("correlation values", corrInstance.getCorrelationMatrix(),
                corrFromCovInstance.getCorrelationMatrix(), 10E-15);
        TestUtils.assertEquals("p values", corrInstance.getCorrelationPValues(),
                corrFromCovInstance.getCorrelationPValues(), 10E-15);
        TestUtils.assertEquals("standard errors", corrInstance.getCorrelationStandardErrors(),
                corrFromCovInstance.getCorrelationStandardErrors(), 10E-15);

        PearsonsCorrelation corrFromCovInstance2 =
            new PearsonsCorrelation(covInstance.getCovarianceMatrix(), 16);
        TestUtils.assertEquals("correlation values", corrInstance.getCorrelationMatrix(),
                corrFromCovInstance2.getCorrelationMatrix(), 10E-15);
        TestUtils.assertEquals("p values", corrInstance.getCorrelationPValues(),
                corrFromCovInstance2.getCorrelationPValues(), 10E-15);
        TestUtils.assertEquals("standard errors", corrInstance.getCorrelationStandardErrors(),
                corrFromCovInstance2.getCorrelationStandardErrors(), 10E-15);
    }

// org.apache.commons.math.stat.correlation.PearsonsCorrelationTest::testConsistency
    public void testConsistency() {
        RealMatrix matrix = createRealMatrix(longleyData, 16, 7);
        PearsonsCorrelation corrInstance = new PearsonsCorrelation(matrix);
        double[][] data = matrix.getData();
        double[] x = matrix.getColumn(0);
        double[] y = matrix.getColumn(1);
        Assert.assertEquals(new PearsonsCorrelation().correlation(x, y),
                corrInstance.getCorrelationMatrix().getEntry(0, 1), Double.MIN_VALUE);
        TestUtils.assertEquals("Correlation matrix", corrInstance.getCorrelationMatrix(),
                new PearsonsCorrelation().computeCorrelationMatrix(data), Double.MIN_VALUE);
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

// org.apache.commons.math.stat.descriptive.ListUnivariateImplTest::testStats
    public void testStats() {
        List<Object> externalList = new ArrayList<Object>();

        DescriptiveStatistics u = new ListUnivariateImpl( externalList );

        Assert.assertEquals("total count",0,u.getN(),tolerance);
        u.addValue(one);
        u.addValue(two);
        u.addValue(two);
        u.addValue(three);
        Assert.assertEquals("N",n,u.getN(),tolerance);
        Assert.assertEquals("sum",sum,u.getSum(),tolerance);
        Assert.assertEquals("sumsq",sumSq,u.getSumsq(),tolerance);
        Assert.assertEquals("var",var,u.getVariance(),tolerance);
        Assert.assertEquals("std",std,u.getStandardDeviation(),tolerance);
        Assert.assertEquals("mean",mean,u.getMean(),tolerance);
        Assert.assertEquals("min",min,u.getMin(),tolerance);
        Assert.assertEquals("max",max,u.getMax(),tolerance);
        u.clear();
        Assert.assertEquals("total count",0,u.getN(),tolerance);
    }

// org.apache.commons.math.stat.descriptive.ListUnivariateImplTest::testN0andN1Conditions
    public void testN0andN1Conditions() throws Exception {
        List<Object> list = new ArrayList<Object>();

        DescriptiveStatistics u = new ListUnivariateImpl( list );

        Assert.assertTrue("Mean of n = 0 set should be NaN", Double.isNaN( u.getMean() ) );
        Assert.assertTrue("Standard Deviation of n = 0 set should be NaN", Double.isNaN( u.getStandardDeviation() ) );
        Assert.assertTrue("Variance of n = 0 set should be NaN", Double.isNaN(u.getVariance() ) );

        list.add( Double.valueOf(one));

        Assert.assertTrue( "Mean of n = 1 set should be value of single item n1", u.getMean() == one);
        Assert.assertTrue( "StdDev of n = 1 set should be zero, instead it is: " + u.getStandardDeviation(), u.getStandardDeviation() == 0);
        Assert.assertTrue( "Variance of n = 1 set should be zero", u.getVariance() == 0);
    }

// org.apache.commons.math.stat.descriptive.ListUnivariateImplTest::testSkewAndKurtosis
    public void testSkewAndKurtosis() {
        DescriptiveStatistics u = new DescriptiveStatistics();

        double[] testArray = { 12.5, 12, 11.8, 14.2, 14.9, 14.5, 21, 8.2, 10.3, 11.3, 14.1,
                                             9.9, 12.2, 12, 12.1, 11, 19.8, 11, 10, 8.8, 9, 12.3 };
        for( int i = 0; i < testArray.length; i++) {
            u.addValue( testArray[i]);
        }

        Assert.assertEquals("mean", 12.40455, u.getMean(), 0.0001);
        Assert.assertEquals("variance", 10.00236, u.getVariance(), 0.0001);
        Assert.assertEquals("skewness", 1.437424, u.getSkewness(), 0.0001);
        Assert.assertEquals("kurtosis", 2.37719, u.getKurtosis(), 0.0001);
    }

// org.apache.commons.math.stat.descriptive.ListUnivariateImplTest::testProductAndGeometricMean
    public void testProductAndGeometricMean() throws Exception {
        ListUnivariateImpl u = new ListUnivariateImpl(new ArrayList<Object>());
        u.setWindowSize(10);

        u.addValue( 1.0 );
        u.addValue( 2.0 );
        u.addValue( 3.0 );
        u.addValue( 4.0 );

        Assert.assertEquals( "Geometric mean not expected", 2.213364, u.getGeometricMean(), 0.00001 );

        
        
        for( int i = 0; i < 10; i++ ) {
            u.addValue( i + 2 );
        }
        

        Assert.assertEquals( "Geometric mean not expected", 5.755931, u.getGeometricMean(), 0.00001 );

    }

// org.apache.commons.math.stat.descriptive.ListUnivariateImplTest::testSerialization
    public void testSerialization() {

        DescriptiveStatistics u = new ListUnivariateImpl();

        Assert.assertEquals("total count",0,u.getN(),tolerance);
        u.addValue(one);
        u.addValue(two);

        DescriptiveStatistics u2 = (DescriptiveStatistics)TestUtils.serializeAndRecover(u);

        u2.addValue(two);
        u2.addValue(three);

        Assert.assertEquals("N",n,u2.getN(),tolerance);
        Assert.assertEquals("sum",sum,u2.getSum(),tolerance);
        Assert.assertEquals("sumsq",sumSq,u2.getSumsq(),tolerance);
        Assert.assertEquals("var",var,u2.getVariance(),tolerance);
        Assert.assertEquals("std",std,u2.getStandardDeviation(),tolerance);
        Assert.assertEquals("mean",mean,u2.getMean(),tolerance);
        Assert.assertEquals("min",min,u2.getMin(),tolerance);
        Assert.assertEquals("max",max,u2.getMax(),tolerance);

        u2.clear();
        Assert.assertEquals("total count",0,u2.getN(),tolerance);
    }

// org.apache.commons.math.stat.descriptive.MixedListUnivariateImplTest::testStats
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

// org.apache.commons.math.stat.descriptive.MixedListUnivariateImplTest::testN0andN1Conditions
    public void testN0andN1Conditions() throws Exception {
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

// org.apache.commons.math.stat.descriptive.MixedListUnivariateImplTest::testSkewAndKurtosis
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

// org.apache.commons.math.stat.descriptive.MixedListUnivariateImplTest::testProductAndGeometricMean
    public void testProductAndGeometricMean() throws Exception {
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

// org.apache.commons.math.stat.descriptive.moment.InteractionTest::testInteraction
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

// org.apache.commons.math.stat.descriptive.moment.KurtosisTest::testNaN
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

// org.apache.commons.math.stat.descriptive.moment.SemiVarianceTest::testInsufficientData
    public void testInsufficientData() {
        double[] nothing = null;
        SemiVariance sv = new SemiVariance();
        try {
            sv.evaluate(nothing);
            Assert.fail("null is not a valid data array.");
        } catch (MathIllegalArgumentException iae) {
        }

        try {
            sv.setVarianceDirection(SemiVariance.UPSIDE_VARIANCE);
            sv.evaluate(nothing);
            Assert.fail("null is not a valid data array.");
        } catch (MathIllegalArgumentException iae) {
        }
        nothing = new double[] {};
        Assert.assertTrue(Double.isNaN(sv.evaluate(nothing)));
    }

// org.apache.commons.math.stat.descriptive.moment.SemiVarianceTest::testSingleDown
    public void testSingleDown() {
        SemiVariance sv = new SemiVariance();
        double[] values = { 50.0d };
        double singletest = sv.evaluate(values);
        Assert.assertEquals(0.0d, singletest, 0);
    }

// org.apache.commons.math.stat.descriptive.moment.SemiVarianceTest::testSingleUp
    public void testSingleUp() {
        SemiVariance sv = new SemiVariance(SemiVariance.UPSIDE_VARIANCE);
        double[] values = { 50.0d };
        double singletest = sv.evaluate(values);
        Assert.assertEquals(0.0d, singletest, 0);
    }

// org.apache.commons.math.stat.descriptive.moment.SemiVarianceTest::testSample
    public void testSample() {
        final double[] values = { -2.0d, 2.0d, 4.0d, -2.0d, 22.0d, 11.0d, 3.0d, 14.0d, 5.0d };
        final int length = values.length;
        final double mean = StatUtils.mean(values); 
        final SemiVariance sv = new SemiVariance();  
        final double downsideSemiVariance = sv.evaluate(values); 
        Assert.assertEquals(TestUtils.sumSquareDev(new double[] {-2d, 2d, 4d, -2d, 3d, 5d}, mean) / (length - 1),
                downsideSemiVariance, 1E-14);

        sv.setVarianceDirection(SemiVariance.UPSIDE_VARIANCE);
        final double upsideSemiVariance = sv.evaluate(values);
        Assert.assertEquals(TestUtils.sumSquareDev(new double[] {22d, 11d, 14d}, mean) / (length - 1),
                upsideSemiVariance, 1E-14);

        
        Assert.assertEquals(StatUtils.variance(values), downsideSemiVariance + upsideSemiVariance, 10e-12);
    }

// org.apache.commons.math.stat.descriptive.moment.SemiVarianceTest::testPopulation
    public void testPopulation() {
        double[] values = { -2.0d, 2.0d, 4.0d, -2.0d, 22.0d, 11.0d, 3.0d, 14.0d, 5.0d };
        SemiVariance sv = new SemiVariance(false);

        double singletest = sv.evaluate(values);
        Assert.assertEquals(19.556d, singletest, 0.01d);

        sv.setVarianceDirection(SemiVariance.UPSIDE_VARIANCE);
        singletest = sv.evaluate(values);
        Assert.assertEquals(36.222d, singletest, 0.01d);
    }

// org.apache.commons.math.stat.descriptive.moment.SemiVarianceTest::testNonMeanCutoffs
    public void testNonMeanCutoffs() {
        double[] values = { -2.0d, 2.0d, 4.0d, -2.0d, 22.0d, 11.0d, 3.0d, 14.0d, 5.0d };
        SemiVariance sv = new SemiVariance(false); 

        double singletest = sv.evaluate(values, 1.0d, SemiVariance.DOWNSIDE_VARIANCE, false, 0, values.length);
        Assert.assertEquals(TestUtils.sumSquareDev(new double[] { -2d, -2d }, 1.0d) / values.length,
                singletest, 0.01d);

        singletest = sv.evaluate(values, 3.0d, SemiVariance.UPSIDE_VARIANCE, false, 0, values.length);
        Assert.assertEquals(TestUtils.sumSquareDev(new double[] { 4d, 22d, 11d, 14d, 5d }, 3.0d) / values.length, singletest,
                0.01d);
    }

// org.apache.commons.math.stat.descriptive.moment.SemiVarianceTest::testVarianceDecompMeanCutoff
    public void testVarianceDecompMeanCutoff() {
        double[] values = { -2.0d, 2.0d, 4.0d, -2.0d, 22.0d, 11.0d, 3.0d, 14.0d, 5.0d };
        double variance = StatUtils.variance(values);
        SemiVariance sv = new SemiVariance(true); 
        sv.setVarianceDirection(SemiVariance.DOWNSIDE_VARIANCE);
        final double lower = sv.evaluate(values);
        sv.setVarianceDirection(SemiVariance.UPSIDE_VARIANCE);
        final double upper = sv.evaluate(values);
        Assert.assertEquals(variance, lower + upper, 10e-12);
    }

// org.apache.commons.math.stat.descriptive.moment.SemiVarianceTest::testVarianceDecompNonMeanCutoff
    public void testVarianceDecompNonMeanCutoff() {
        double[] values = { -2.0d, 2.0d, 4.0d, -2.0d, 22.0d, 11.0d, 3.0d, 14.0d, 5.0d };
        double target = 0;
        double totalSumOfSquares = TestUtils.sumSquareDev(values, target);
        SemiVariance sv = new SemiVariance(true); 
        sv.setVarianceDirection(SemiVariance.DOWNSIDE_VARIANCE);
        double lower = sv.evaluate(values, target);
        sv.setVarianceDirection(SemiVariance.UPSIDE_VARIANCE);
        double upper = sv.evaluate(values, target);
        Assert.assertEquals(totalSumOfSquares / (values.length - 1), lower + upper, 10e-12);
    }

// org.apache.commons.math.stat.descriptive.moment.SemiVarianceTest::testNoVariance
    public void testNoVariance() {
        final double[] values = {100d, 100d, 100d, 100d};
        SemiVariance sv = new SemiVariance();
        Assert.assertEquals(0, sv.evaluate(values), 10E-12);
        Assert.assertEquals(0, sv.evaluate(values, 100d), 10E-12);
        Assert.assertEquals(0, sv.evaluate(values, 100d, SemiVariance.UPSIDE_VARIANCE, false, 0, values.length), 10E-12);
    }

// org.apache.commons.math.stat.descriptive.moment.StandardDeviationTest::testNaN
    public void testNaN() {
        StandardDeviation std = new StandardDeviation();
        Assert.assertTrue(Double.isNaN(std.getResult()));
        std.increment(1d);
        Assert.assertEquals(0d, std.getResult(), 0);
    }

// org.apache.commons.math.stat.descriptive.moment.StandardDeviationTest::testPopulation
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

// org.apache.commons.math.stat.descriptive.moment.VarianceTest::testNaN
    public void testNaN() {
        StandardDeviation std = new StandardDeviation();
        Assert.assertTrue(Double.isNaN(std.getResult()));
        std.increment(1d);
        Assert.assertEquals(0d, std.getResult(), 0);
    }

// org.apache.commons.math.stat.descriptive.moment.VarianceTest::testPopulation
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

// org.apache.commons.math.stat.descriptive.moment.VarianceTest::testWeightedVariance
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
        Assert.assertEquals(combinedX, regression.X);
        Assert.assertEquals(combinedY, regression.Y);
        Assert.assertEquals(combinedCovInv, regression.getOmegaInverse());
    }

// org.apache.commons.math.stat.regression.GLSMultipleLinearRegressionTest::testGLSOLSConsistency
    public void testGLSOLSConsistency() throws Exception {      
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
       Assert.assertEquals(0.0,
                     errors.subtract(referenceVariance).getNorm(),
                     5.0e-16 * referenceVariance.getNorm());
       Assert.assertEquals(1, ((OLSMultipleLinearRegression) regression).calculateRSquared(), 1E-12);
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
                Assert.assertEquals(referenceData[k], hat.getEntry(i, j), 10e-3);
                Assert.assertEquals(hat.getEntry(i, j), hat.getEntry(j, i), 10e-12);
                k++;
            }
        }

        
        double[] residuals = model.estimateResiduals();
        RealMatrix I = MatrixUtils.createRealIdentityMatrix(10);
        double[] hatResiduals = I.subtract(hat).operate(model.Y).toArray();
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
        Assert.assertEquals(combinedX, regression.X);
        Assert.assertEquals(combinedY, regression.Y);
        
        
        regression.setNoIntercept(true);
        regression.newSampleData(y, x);
        combinedX = regression.X.copy();
        combinedY = regression.Y.copy();
        regression.newXSampleData(x);
        regression.newYSampleData(y);
        Assert.assertEquals(combinedX, regression.X);
        Assert.assertEquals(combinedY, regression.Y);
    }

// org.apache.commons.math.stat.regression.OLSMultipleLinearRegressionTest::testNewSampleDataYNull
    public void testNewSampleDataYNull() {
        createRegression().newSampleData(null, new double[][] {});
    }

// org.apache.commons.math.stat.regression.OLSMultipleLinearRegressionTest::testNewSampleDataXNull
    public void testNewSampleDataXNull() {
        createRegression().newSampleData(new double[] {}, null);
    }

// org.apache.commons.math.stat.regression.OLSMultipleLinearRegressionTest::testWampler1
    public void testWampler1() throws Exception {
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

// org.apache.commons.math.stat.regression.OLSMultipleLinearRegressionTest::testWampler2
    public void testWampler2() throws Exception {
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

// org.apache.commons.math.stat.regression.OLSMultipleLinearRegressionTest::testWampler3
    public void testWampler3() throws Exception {
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

// org.apache.commons.math.stat.regression.OLSMultipleLinearRegressionTest::testWampler4
    public void testWampler4() throws Exception {
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

// org.apache.commons.math.util.ResizableDoubleArrayTest::testConstructors
    public void testConstructors() {
        float defaultExpansionFactor = 2.0f;
        float defaultContractionCriteria = 2.5f;
        int defaultMode = ResizableDoubleArray.MULTIPLICATIVE_MODE;

        ResizableDoubleArray testDa = new ResizableDoubleArray(2);
        Assert.assertEquals(0, testDa.getNumElements());
        Assert.assertEquals(2, testDa.getInternalLength());
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
        Assert.assertEquals(2, testDa.getInternalLength());
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
        Assert.assertEquals(2, testDa.getInternalLength());
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
        Assert.assertEquals(2, testDa.getInternalLength());
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

// org.apache.commons.math.util.ResizableDoubleArrayTest::testSetElementArbitraryExpansion
    public void testSetElementArbitraryExpansion() {

        
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

        
        da.clear();
        da.addElement(2.0);
        da.addElement(4.0);
        da.addElement(6.0);
        Assert.assertEquals(4, ((ResizableDoubleArray) da).getInternalLength());
        Assert.assertEquals(3, da.getNumElements());
        da.setElement(3, 7.0);
        Assert.assertEquals(4, ((ResizableDoubleArray) da).getInternalLength());
        Assert.assertEquals(4, da.getNumElements());
        da.setElement(10, 10.0);
        Assert.assertEquals(11, ((ResizableDoubleArray) da).getInternalLength());
        Assert.assertEquals(11, da.getNumElements());
        da.setElement(9, 10.0);
        Assert.assertEquals(11, ((ResizableDoubleArray) da).getInternalLength());
        Assert.assertEquals(11, da.getNumElements());

        try {
            da.setElement(-2, 3);
            Assert.fail("Expecting ArrayIndexOutOfBoundsException for negative index");
        } catch (ArrayIndexOutOfBoundsException ex) {
            
        }

        

        ResizableDoubleArray testDa = new ResizableDoubleArray(2, 2.0f, 3.0f,
                ResizableDoubleArray.ADDITIVE_MODE);
        Assert.assertEquals(2, testDa.getInternalLength());
        testDa.addElement(1d);
        testDa.addElement(1d);
        Assert.assertEquals(2, testDa.getInternalLength());
        testDa.addElement(1d);
        Assert.assertEquals(4, testDa.getInternalLength());
    }

// org.apache.commons.math.util.ResizableDoubleArrayTest::testAdd1000
    public void testAdd1000() {
        super.testAdd1000();
        Assert.assertEquals("Internal Storage length should be 1024 if we started out with initial capacity of " +
                "16 and an expansion factor of 2.0",
                1024, ((ResizableDoubleArray) da).getInternalLength());
    }

// org.apache.commons.math.util.ResizableDoubleArrayTest::testAddElements
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
        Assert.assertEquals(2, testDa.getInternalLength());
        testDa.addElements(new double[] { 1d }); 
        testDa.addElements(new double[] { 2d }); 
        testDa.addElements(new double[] { 3d }); 
        Assert.assertEquals(1d, testDa.getElement(0), 0);
        Assert.assertEquals(2d, testDa.getElement(1), 0);
        Assert.assertEquals(3d, testDa.getElement(2), 0);
        Assert.assertEquals(4, testDa.getInternalLength());  
        Assert.assertEquals(3, testDa.getNumElements());
    }

// org.apache.commons.math.util.ResizableDoubleArrayTest::testAddElementRolling
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
        Assert.assertEquals(2, testDa.getInternalLength());
        testDa.addElement(1d); 
        testDa.addElement(2d); 
        testDa.addElement(3d); 
        Assert.assertEquals(1d, testDa.getElement(0), 0);
        Assert.assertEquals(2d, testDa.getElement(1), 0);
        Assert.assertEquals(3d, testDa.getElement(2), 0);
        Assert.assertEquals(4, testDa.getInternalLength());  
        Assert.assertEquals(3, testDa.getNumElements());
        testDa.addElementRolling(4d);
        Assert.assertEquals(2d, testDa.getElement(0), 0);
        Assert.assertEquals(3d, testDa.getElement(1), 0);
        Assert.assertEquals(4d, testDa.getElement(2), 0);
        Assert.assertEquals(4, testDa.getInternalLength());  
        Assert.assertEquals(3, testDa.getNumElements());
        testDa.addElementRolling(5d);   
        Assert.assertEquals(3d, testDa.getElement(0), 0);
        Assert.assertEquals(4d, testDa.getElement(1), 0);
        Assert.assertEquals(5d, testDa.getElement(2), 0);
        Assert.assertEquals(4, testDa.getInternalLength());  
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

// org.apache.commons.math.util.ResizableDoubleArrayTest::testSetNumberOfElements
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

// org.apache.commons.math.util.ResizableDoubleArrayTest::testWithInitialCapacity
    public void testWithInitialCapacity() {

        ResizableDoubleArray eDA2 = new ResizableDoubleArray(2);
        Assert.assertEquals("Initial number of elements should be 0", 0, eDA2.getNumElements());

        RandomData randomData = new RandomDataImpl();
        int iterations = randomData.nextInt(100, 1000);

        for( int i = 0; i < iterations; i++) {
            eDA2.addElement( i );
        }

        Assert.assertEquals("Number of elements should be equal to " + iterations, iterations, eDA2.getNumElements());

        eDA2.addElement( 2.0 );

        Assert.assertEquals("Number of elements should be equals to " + (iterations +1),
                iterations + 1 , eDA2.getNumElements() );
    }

// org.apache.commons.math.util.ResizableDoubleArrayTest::testWithInitialCapacityAndExpansionFactor
    public void testWithInitialCapacityAndExpansionFactor() {

        ResizableDoubleArray eDA3 = new ResizableDoubleArray(3, 3.0f, 3.5f);
        Assert.assertEquals("Initial number of elements should be 0", 0, eDA3.getNumElements() );

        RandomData randomData = new RandomDataImpl();
        int iterations = randomData.nextInt(100, 3000);

        for( int i = 0; i < iterations; i++) {
            eDA3.addElement( i );
        }

        Assert.assertEquals("Number of elements should be equal to " + iterations, iterations,eDA3.getNumElements());

        eDA3.addElement( 2.0 );

        Assert.assertEquals("Number of elements should be equals to " + (iterations +1),
                iterations +1, eDA3.getNumElements() );

        Assert.assertEquals("Expansion factor should equal 3.0", 3.0f, eDA3.getExpansionFactor(), Double.MIN_VALUE);
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

// org.apache.commons.math.util.ResizableDoubleArrayTest::testMutators
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

// org.apache.commons.math.util.ResizableDoubleArrayTest::testEqualsAndHashCode
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

// org.apache.commons.math.stat.descriptive.UnivariateStatisticAbstractTest::testEvaluation
    public void testEvaluation() throws Exception {
        Assert.assertEquals(
            expectedValue(),
            getUnivariateStatistic().evaluate(testArray),
            getTolerance());
    }

// org.apache.commons.math.stat.descriptive.UnivariateStatisticAbstractTest::testEvaluateArraySegment
    public void testEvaluateArraySegment() {
        final UnivariateStatistic stat = getUnivariateStatistic();
        final double[] arrayZero = new double[5];
        System.arraycopy(testArray, 0, arrayZero, 0, 5);
        Assert.assertEquals(stat.evaluate(arrayZero), stat.evaluate(testArray, 0, 5), 0);
        final double[] arrayOne = new double[5];
        System.arraycopy(testArray, 5, arrayOne, 0, 5);
        Assert.assertEquals(stat.evaluate(arrayOne), stat.evaluate(testArray, 5, 5), 0);
        final double[] arrayEnd = new double[5];
        System.arraycopy(testArray, testArray.length - 5, arrayEnd, 0, 5);
        Assert.assertEquals(stat.evaluate(arrayEnd), stat.evaluate(testArray, testArray.length - 5, 5), 0);
    }

// org.apache.commons.math.stat.descriptive.UnivariateStatisticAbstractTest::testEvaluateArraySegmentWeighted
    public void testEvaluateArraySegmentWeighted() {
        
        
        UnivariateStatistic statistic = getUnivariateStatistic();
        if (!(statistic instanceof WeightedEvaluation)) {
            return;
        }
        final WeightedEvaluation stat = (WeightedEvaluation) getUnivariateStatistic();
        final double[] arrayZero = new double[5];
        final double[] weightZero = new double[5];
        System.arraycopy(testArray, 0, arrayZero, 0, 5);
        System.arraycopy(testWeightsArray, 0, weightZero, 0, 5);
        Assert.assertEquals(stat.evaluate(arrayZero, weightZero),
                stat.evaluate(testArray, testWeightsArray, 0, 5), 0);
        final double[] arrayOne = new double[5];
        final double[] weightOne = new double[5];
        System.arraycopy(testArray, 5, arrayOne, 0, 5);
        System.arraycopy(testWeightsArray, 5, weightOne, 0, 5);
        Assert.assertEquals(stat.evaluate(arrayOne, weightOne),
                stat.evaluate(testArray, testWeightsArray, 5, 5), 0);
        final double[] arrayEnd = new double[5];
        final double[] weightEnd = new double[5];
        System.arraycopy(testArray, testArray.length - 5, arrayEnd, 0, 5);
        System.arraycopy(testWeightsArray, testArray.length - 5, weightEnd, 0, 5);
        Assert.assertEquals(stat.evaluate(arrayEnd, weightEnd),
                stat.evaluate(testArray, testWeightsArray, testArray.length - 5, 5), 0);
    }

// org.apache.commons.math.stat.descriptive.UnivariateStatisticAbstractTest::testCopy
    public void testCopy() throws Exception {
        UnivariateStatistic original = getUnivariateStatistic();
        UnivariateStatistic copy = original.copy();
        Assert.assertEquals(
                expectedValue(),
                copy.evaluate(testArray),
                getTolerance());
    }

// org.apache.commons.math.stat.descriptive.UnivariateStatisticAbstractTest::testWeightedConsistency
    public void testWeightedConsistency() throws Exception {

        
        
        UnivariateStatistic statistic = getUnivariateStatistic();
        if (!(statistic instanceof WeightedEvaluation)) {
            return;
        }

        
        
        final int len = 10;        
        final double mu = 0;       
        final double sigma = 5;    
        double[] values = new double[len];
        double[] weights = new double[len];
        RandomData randomData = new RandomDataImpl();

        
        int[] intWeights = new int[len];
        for (int i = 0; i < len; i++) {
            intWeights[i] = randomData.nextInt(1, 5);
            weights[i] = intWeights[i];
        }

        
        
        
        List<Double> valuesList = new ArrayList<Double>();
        for (int i = 0; i < len; i++) {
            double value = randomData.nextGaussian(mu, sigma);
            values[i] = value;
            for (int j = 0; j < intWeights[i]; j++) {
                valuesList.add(new Double(value));
            }
        }

        
        int sumWeights = valuesList.size();
        double[] repeatedValues = new double[sumWeights];
        for (int i = 0; i < sumWeights; i++) {
            repeatedValues[i] = valuesList.get(i);
        }

        
        
        WeightedEvaluation weightedStatistic = (WeightedEvaluation) statistic;
        TestUtils.assertRelativelyEquals(statistic.evaluate(repeatedValues),
                weightedStatistic.evaluate(values, weights, 0, values.length),
                10E-14);

        
        Assert.assertEquals(weightedStatistic.evaluate(values, weights, 0, values.length),
                weightedStatistic.evaluate(values, weights), Double.MIN_VALUE);

    }
