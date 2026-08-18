// buggy code
    public static float max(final float a, final float b) {
        return (a <= b) ? b : (Float.isNaN(a + b) ? Float.NaN : b);
    }

// relevant test
// org.apache.commons.math.analysis.FunctionUtilsTest::testCompose
    public void testCompose() {
        UnivariateRealFunction id = new Identity();
        Assert.assertEquals(3, FunctionUtils.compose(id, id, id).value(3), EPS);

        UnivariateRealFunction c = new Constant(4);
        Assert.assertEquals(4, FunctionUtils.compose(id, c).value(3), EPS);
        Assert.assertEquals(4, FunctionUtils.compose(c, id).value(3), EPS);

        UnivariateRealFunction m = new Minus();
        Assert.assertEquals(-3, FunctionUtils.compose(m).value(3), EPS);
        Assert.assertEquals(3, FunctionUtils.compose(m, m).value(3), EPS);

        UnivariateRealFunction inv = new Inverse();
        Assert.assertEquals(-0.25, FunctionUtils.compose(inv, m, c, id).value(3), EPS);

        UnivariateRealFunction pow = new Power(2);
        Assert.assertEquals(81, FunctionUtils.compose(pow, pow).value(3), EPS);
    }

// org.apache.commons.math.analysis.FunctionUtilsTest::testAdd
    public void testAdd() {
        UnivariateRealFunction id = new Identity();
        UnivariateRealFunction c = new Constant(4);
        UnivariateRealFunction m = new Minus();
        UnivariateRealFunction inv = new Inverse();

        Assert.assertEquals(4.5, FunctionUtils.add(inv, m, c, id).value(2), EPS);
        Assert.assertEquals(4 + 2, FunctionUtils.add(c, id).value(2), EPS);
        Assert.assertEquals(4 - 2, FunctionUtils.add(c, FunctionUtils.compose(m, id)).value(2), EPS);
    }

// org.apache.commons.math.analysis.FunctionUtilsTest::testMultiply
    public void testMultiply() {
        UnivariateRealFunction c = new Constant(4);
        Assert.assertEquals(16, FunctionUtils.multiply(c, c).value(12345), EPS);

        UnivariateRealFunction inv = new Inverse();
        UnivariateRealFunction pow = new Power(2);
        Assert.assertEquals(1, FunctionUtils.multiply(FunctionUtils.compose(inv, pow), pow).value(3.5), EPS);
    }

// org.apache.commons.math.analysis.FunctionUtilsTest::testCombine
    public void testCombine() {
        BivariateRealFunction bi = new Add();
        UnivariateRealFunction id = new Identity();
        UnivariateRealFunction m = new Minus();
        UnivariateRealFunction c = FunctionUtils.combine(bi, id, m);
        Assert.assertEquals(0, c.value(2.3456), EPS);

        bi = new Multiply();
        UnivariateRealFunction inv = new Inverse();
        c = FunctionUtils.combine(bi, id, inv);
        Assert.assertEquals(1, c.value(2.3456), EPS);
    }

// org.apache.commons.math.analysis.FunctionUtilsTest::testCollector
    public void testCollector() {
        BivariateRealFunction bi = new Add();
        MultivariateRealFunction coll = FunctionUtils.collector(bi, 0);
        Assert.assertEquals(10, coll.value(new double[] {1, 2, 3, 4}), EPS);

        bi = new Multiply();
        coll = FunctionUtils.collector(bi, 1);
        Assert.assertEquals(24, coll.value(new double[] {1, 2, 3, 4}), EPS);

        bi = new Max();
        coll = FunctionUtils.collector(bi, Double.NEGATIVE_INFINITY);
        Assert.assertEquals(10, coll.value(new double[] {1, -2, 7.5, 10, -24, 9.99}), 0);

        bi = new Min();
        coll = FunctionUtils.collector(bi, Double.POSITIVE_INFINITY);
        Assert.assertEquals(-24, coll.value(new double[] {1, -2, 7.5, 10, -24, 9.99}), 0);
    }

// org.apache.commons.math.analysis.FunctionUtilsTest::testSinc
    public void testSinc() {
        BivariateRealFunction div = new Divide();
        UnivariateRealFunction sin = new Sin();
        UnivariateRealFunction id = new Identity();
        UnivariateRealFunction sinc1 = FunctionUtils.combine(div, sin, id);
        UnivariateRealFunction sinc2 = new Sinc();

        for (int i = 0; i < 10; i++) {
            double x = Math.random();
            Assert.assertEquals(sinc1.value(x), sinc2.value(x), EPS);
        }
    }

// org.apache.commons.math.analysis.FunctionUtilsTest::testFixingArguments
    public void testFixingArguments() {
        UnivariateRealFunction scaler = FunctionUtils.fix1stArgument(new Multiply(), 10);
        Assert.assertEquals(1.23456, scaler.value(0.123456), EPS);

        UnivariateRealFunction pow1 = new Power(2);
        UnivariateRealFunction pow2 = FunctionUtils.fix2ndArgument(new Pow(), 2);

        for (int i = 0; i < 10; i++) {
            double x = Math.random() * 10;
            Assert.assertEquals(pow1.value(x), pow2.value(x), 0);
        }
    }

// org.apache.commons.math.analysis.integration.LegendreGaussIntegratorTest::testSinFunction
    public void testSinFunction() throws MathException {
        UnivariateRealFunction f = new SinFunction();
        UnivariateRealIntegrator integrator = new LegendreGaussIntegrator(5, 64);
        integrator.setAbsoluteAccuracy(1.0e-10);
        integrator.setRelativeAccuracy(1.0e-14);
        integrator.setMinimalIterationCount(2);
        integrator.setMaximalIterationCount(15);
        double min, max, expected, result, tolerance;

        min = 0; max = FastMath.PI; expected = 2;
        tolerance = FastMath.max(integrator.getAbsoluteAccuracy(),
                             FastMath.abs(expected * integrator.getRelativeAccuracy()));
        result = integrator.integrate(f, min, max);
        assertEquals(expected, result, tolerance);

        min = -FastMath.PI/3; max = 0; expected = -0.5;
        tolerance = FastMath.max(integrator.getAbsoluteAccuracy(),
                FastMath.abs(expected * integrator.getRelativeAccuracy()));
        result = integrator.integrate(f, min, max);
        assertEquals(expected, result, tolerance);
    }

// org.apache.commons.math.analysis.integration.LegendreGaussIntegratorTest::testQuinticFunction
    public void testQuinticFunction() throws MathException {
        UnivariateRealFunction f = new QuinticFunction();
        UnivariateRealIntegrator integrator = new LegendreGaussIntegrator(3, 64);
        double min, max, expected, result;

        min = 0; max = 1; expected = -1.0/48;
        result = integrator.integrate(f, min, max);
        assertEquals(expected, result, 1.0e-16);

        min = 0; max = 0.5; expected = 11.0/768;
        result = integrator.integrate(f, min, max);
        assertEquals(expected, result, 1.0e-16);

        min = -1; max = 4; expected = 2048/3.0 - 78 + 1.0/48;
        result = integrator.integrate(f, min, max);
        assertEquals(expected, result, 1.0e-16);
    }

// org.apache.commons.math.analysis.integration.LegendreGaussIntegratorTest::testExactIntegration
    public void testExactIntegration()
        throws ConvergenceException, MathUserException {
        Random random = new Random(86343623467878363l);
        for (int n = 2; n < 6; ++n) {
            LegendreGaussIntegrator integrator =
                new LegendreGaussIntegrator(n, 64);

            
            for (int degree = 0; degree <= 2 * n - 1; ++degree) {
                for (int i = 0; i < 10; ++i) {
                    double[] coeff = new double[degree + 1];
                    for (int k = 0; k < coeff.length; ++k) {
                        coeff[k] = 2 * random.nextDouble() - 1;
                    }
                    PolynomialFunction p = new PolynomialFunction(coeff);
                    double result    = integrator.integrate(p, -5.0, 15.0);
                    double reference = exactIntegration(p, -5.0, 15.0);
                    assertEquals(n + " " + degree + " " + i, reference, result, 1.0e-12 * (1.0 + FastMath.abs(reference)));
                }
            }

        }
    }

// org.apache.commons.math.analysis.integration.RombergIntegratorTest::testSinFunction
    public void testSinFunction() throws MathException {
        UnivariateRealFunction f = new SinFunction();
        UnivariateRealIntegrator integrator = new RombergIntegrator();
        double min, max, expected, result, tolerance;

        min = 0; max = FastMath.PI; expected = 2;
        tolerance = FastMath.abs(expected * integrator.getRelativeAccuracy());
        result = integrator.integrate(f, min, max);
        assertEquals(expected, result, tolerance);

        min = -FastMath.PI/3; max = 0; expected = -0.5;
        tolerance = FastMath.abs(expected * integrator.getRelativeAccuracy());
        result = integrator.integrate(f, min, max);
        assertEquals(expected, result, tolerance);
    }

// org.apache.commons.math.analysis.integration.RombergIntegratorTest::testQuinticFunction
    public void testQuinticFunction() throws MathException {
        UnivariateRealFunction f = new QuinticFunction();
        UnivariateRealIntegrator integrator = new RombergIntegrator();
        double min, max, expected, result, tolerance;

        min = 0; max = 1; expected = -1.0/48;
        tolerance = FastMath.abs(expected * integrator.getRelativeAccuracy());
        result = integrator.integrate(f, min, max);
        assertEquals(expected, result, tolerance);

        min = 0; max = 0.5; expected = 11.0/768;
        tolerance = FastMath.abs(expected * integrator.getRelativeAccuracy());
        result = integrator.integrate(f, min, max);
        assertEquals(expected, result, tolerance);

        min = -1; max = 4; expected = 2048/3.0 - 78 + 1.0/48;
        tolerance = FastMath.abs(expected * integrator.getRelativeAccuracy());
        result = integrator.integrate(f, min, max);
        assertEquals(expected, result, tolerance);
    }

// org.apache.commons.math.analysis.integration.RombergIntegratorTest::testParameters
    public void testParameters() throws Exception {
        UnivariateRealFunction f = new SinFunction();
        UnivariateRealIntegrator integrator = new RombergIntegrator();

        try {
            
            integrator.integrate(f, 1, -1);
            fail("Expecting IllegalArgumentException - bad interval");
        } catch (IllegalArgumentException ex) {
            
        }
        try {
            
            integrator.setMinimalIterationCount(5);
            integrator.setMaximalIterationCount(4);
            integrator.integrate(f, -1, 1);
            fail("Expecting IllegalArgumentException - bad iteration limits");
        } catch (IllegalArgumentException ex) {
            
        }
        try {
            
            integrator.setMinimalIterationCount(10);
            integrator.setMaximalIterationCount(50);
            integrator.integrate(f, -1, 1);
            fail("Expecting IllegalArgumentException - bad iteration limits");
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.analysis.integration.SimpsonIntegratorTest::testSinFunction
    public void testSinFunction() throws MathException {
        UnivariateRealFunction f = new SinFunction();
        UnivariateRealIntegrator integrator = new SimpsonIntegrator();
        double min, max, expected, result, tolerance;

        min = 0; max = FastMath.PI; expected = 2;
        tolerance = FastMath.abs(expected * integrator.getRelativeAccuracy());
        result = integrator.integrate(f, min, max);
        assertEquals(expected, result, tolerance);

        min = -FastMath.PI/3; max = 0; expected = -0.5;
        tolerance = FastMath.abs(expected * integrator.getRelativeAccuracy());
        result = integrator.integrate(f, min, max);
        assertEquals(expected, result, tolerance);
    }

// org.apache.commons.math.analysis.integration.SimpsonIntegratorTest::testQuinticFunction
    public void testQuinticFunction() throws MathException {
        UnivariateRealFunction f = new QuinticFunction();
        UnivariateRealIntegrator integrator = new SimpsonIntegrator();
        double min, max, expected, result, tolerance;

        min = 0; max = 1; expected = -1.0/48;
        tolerance = FastMath.abs(expected * integrator.getRelativeAccuracy());
        result = integrator.integrate(f, min, max);
        assertEquals(expected, result, tolerance);

        min = 0; max = 0.5; expected = 11.0/768;
        tolerance = FastMath.abs(expected * integrator.getRelativeAccuracy());
        result = integrator.integrate(f, min, max);
        assertEquals(expected, result, tolerance);

        min = -1; max = 4; expected = 2048/3.0 - 78 + 1.0/48;
        tolerance = FastMath.abs(expected * integrator.getRelativeAccuracy());
        result = integrator.integrate(f, min, max);
        assertEquals(expected, result, tolerance);
    }

// org.apache.commons.math.analysis.integration.SimpsonIntegratorTest::testParameters
    public void testParameters() throws Exception {
        UnivariateRealFunction f = new SinFunction();
        UnivariateRealIntegrator integrator = new SimpsonIntegrator();

        try {
            
            integrator.integrate(f, 1, -1);
            fail("Expecting IllegalArgumentException - bad interval");
        } catch (IllegalArgumentException ex) {
            
        }
        try {
            
            integrator.setMinimalIterationCount(5);
            integrator.setMaximalIterationCount(4);
            integrator.integrate(f, -1, 1);
            fail("Expecting IllegalArgumentException - bad iteration limits");
        } catch (IllegalArgumentException ex) {
            
        }
        try {
            
            integrator.setMinimalIterationCount(10);
            integrator.setMaximalIterationCount(99);
            integrator.integrate(f, -1, 1);
            fail("Expecting IllegalArgumentException - bad iteration limits");
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.analysis.integration.TrapezoidIntegratorTest::testSinFunction
    public void testSinFunction() throws MathException {
        UnivariateRealFunction f = new SinFunction();
        UnivariateRealIntegrator integrator = new TrapezoidIntegrator();
        double min, max, expected, result, tolerance;

        min = 0; max = FastMath.PI; expected = 2;
        tolerance = FastMath.abs(expected * integrator.getRelativeAccuracy());
        result = integrator.integrate(f, min, max);
        assertEquals(expected, result, tolerance);

        min = -FastMath.PI/3; max = 0; expected = -0.5;
        tolerance = FastMath.abs(expected * integrator.getRelativeAccuracy());
        result = integrator.integrate(f, min, max);
        assertEquals(expected, result, tolerance);
    }

// org.apache.commons.math.analysis.integration.TrapezoidIntegratorTest::testQuinticFunction
    public void testQuinticFunction() throws MathException {
        UnivariateRealFunction f = new QuinticFunction();
        UnivariateRealIntegrator integrator = new TrapezoidIntegrator();
        double min, max, expected, result, tolerance;

        min = 0; max = 1; expected = -1.0/48;
        tolerance = FastMath.abs(expected * integrator.getRelativeAccuracy());
        result = integrator.integrate(f, min, max);
        assertEquals(expected, result, tolerance);

        min = 0; max = 0.5; expected = 11.0/768;
        tolerance = FastMath.abs(expected * integrator.getRelativeAccuracy());
        result = integrator.integrate(f, min, max);
        assertEquals(expected, result, tolerance);

        min = -1; max = 4; expected = 2048/3.0 - 78 + 1.0/48;
        tolerance = FastMath.abs(expected * integrator.getRelativeAccuracy());
        result = integrator.integrate(f, min, max);
        assertEquals(expected, result, tolerance);
    }

// org.apache.commons.math.analysis.integration.TrapezoidIntegratorTest::testParameters
    public void testParameters() throws Exception {
        UnivariateRealFunction f = new SinFunction();
        UnivariateRealIntegrator integrator = new TrapezoidIntegrator();

        try {
            
            integrator.integrate(f, 1, -1);
            fail("Expecting IllegalArgumentException - bad interval");
        } catch (IllegalArgumentException ex) {
            
        }
        try {
            
            integrator.setMinimalIterationCount(5);
            integrator.setMaximalIterationCount(4);
            integrator.integrate(f, -1, 1);
            fail("Expecting IllegalArgumentException - bad iteration limits");
        } catch (IllegalArgumentException ex) {
            
        }
        try {
            
            integrator.setMinimalIterationCount(10);
            integrator.setMaximalIterationCount(99);
            integrator.integrate(f, -1, 1);
            fail("Expecting IllegalArgumentException - bad iteration limits");
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.analysis.interpolation.DividedDifferenceInterpolatorTest::testSinFunction
    public void testSinFunction() throws MathException {
        UnivariateRealFunction f = new SinFunction();
        UnivariateRealInterpolator interpolator = new DividedDifferenceInterpolator();
        double x[], y[], z, expected, result, tolerance;

        
        int n = 6;
        double min = 0.0, max = 2 * FastMath.PI;
        x = new double[n];
        y = new double[n];
        for (int i = 0; i < n; i++) {
            x[i] = min + i * (max - min) / n;
            y[i] = f.value(x[i]);
        }
        double derivativebound = 1.0;
        UnivariateRealFunction p = interpolator.interpolate(x, y);

        z = FastMath.PI / 4; expected = f.value(z); result = p.value(z);
        tolerance = FastMath.abs(derivativebound * partialerror(x, z));
        assertEquals(expected, result, tolerance);

        z = FastMath.PI * 1.5; expected = f.value(z); result = p.value(z);
        tolerance = FastMath.abs(derivativebound * partialerror(x, z));
        assertEquals(expected, result, tolerance);
    }

// org.apache.commons.math.analysis.interpolation.DividedDifferenceInterpolatorTest::testExpm1Function
    public void testExpm1Function() throws MathException {
        UnivariateRealFunction f = new Expm1Function();
        UnivariateRealInterpolator interpolator = new DividedDifferenceInterpolator();
        double x[], y[], z, expected, result, tolerance;

        
        int n = 5;
        double min = -1.0, max = 1.0;
        x = new double[n];
        y = new double[n];
        for (int i = 0; i < n; i++) {
            x[i] = min + i * (max - min) / n;
            y[i] = f.value(x[i]);
        }
        double derivativebound = FastMath.E;
        UnivariateRealFunction p = interpolator.interpolate(x, y);

        z = 0.0; expected = f.value(z); result = p.value(z);
        tolerance = FastMath.abs(derivativebound * partialerror(x, z));
        assertEquals(expected, result, tolerance);

        z = 0.5; expected = f.value(z); result = p.value(z);
        tolerance = FastMath.abs(derivativebound * partialerror(x, z));
        assertEquals(expected, result, tolerance);

        z = -0.5; expected = f.value(z); result = p.value(z);
        tolerance = FastMath.abs(derivativebound * partialerror(x, z));
        assertEquals(expected, result, tolerance);
    }

// org.apache.commons.math.analysis.interpolation.DividedDifferenceInterpolatorTest::testParameters
    public void testParameters() throws Exception {
        UnivariateRealInterpolator interpolator = new DividedDifferenceInterpolator();

        try {
            
            double x[] = { 1.0, 2.0, 2.0, 4.0 };
            double y[] = { 0.0, 4.0, 4.0, 2.5 };
            UnivariateRealFunction p = interpolator.interpolate(x, y);
            p.value(0.0);
            fail("Expecting NonMonotonousSequenceException - bad abscissas array");
        } catch (NonMonotonousSequenceException ex) {
            
        }
    }

// org.apache.commons.math.analysis.interpolation.LoessInterpolatorTest::testOnOnePoint
    public void testOnOnePoint() {
        double[] xval = {0.5};
        double[] yval = {0.7};
        double[] res = new LoessInterpolator().smooth(xval, yval);
        Assert.assertEquals(1, res.length);
        Assert.assertEquals(0.7, res[0], 0.0);
    }

// org.apache.commons.math.analysis.interpolation.LoessInterpolatorTest::testOnTwoPoints
    public void testOnTwoPoints() {
        double[] xval = {0.5, 0.6};
        double[] yval = {0.7, 0.8};
        double[] res = new LoessInterpolator().smooth(xval, yval);
        Assert.assertEquals(2, res.length);
        Assert.assertEquals(0.7, res[0], 0.0);
        Assert.assertEquals(0.8, res[1], 0.0);
    }

// org.apache.commons.math.analysis.interpolation.LoessInterpolatorTest::testOnStraightLine
    public void testOnStraightLine() {
        double[] xval = {1,2,3,4,5};
        double[] yval = {2,4,6,8,10};
        LoessInterpolator li = new LoessInterpolator(0.6, 2, 1e-12);
        double[] res = li.smooth(xval, yval);
        Assert.assertEquals(5, res.length);
        for(int i = 0; i < 5; ++i) {
            Assert.assertEquals(yval[i], res[i], 1e-8);
        }
    }

// org.apache.commons.math.analysis.interpolation.LoessInterpolatorTest::testOnDistortedSine
    public void testOnDistortedSine() {
        int numPoints = 100;
        double[] xval = new double[numPoints];
        double[] yval = new double[numPoints];
        double xnoise = 0.1;
        double ynoise = 0.2;

        generateSineData(xval, yval, xnoise, ynoise);

        LoessInterpolator li = new LoessInterpolator(0.3, 4, 1e-12);

        double[] res = li.smooth(xval, yval);

        
        

        double noisyResidualSum = 0;
        double fitResidualSum = 0;

        for(int i = 0; i < numPoints; ++i) {
            double expected = FastMath.sin(xval[i]);
            double noisy = yval[i];
            double fit = res[i];

            noisyResidualSum += FastMath.pow(noisy - expected, 2);
            fitResidualSum += FastMath.pow(fit - expected, 2);
        }

        Assert.assertTrue(fitResidualSum < noisyResidualSum);
    }

// org.apache.commons.math.analysis.interpolation.LoessInterpolatorTest::testIncreasingBandwidthIncreasesSmoothness
    public void testIncreasingBandwidthIncreasesSmoothness() {
        int numPoints = 100;
        double[] xval = new double[numPoints];
        double[] yval = new double[numPoints];
        double xnoise = 0.1;
        double ynoise = 0.1;

        generateSineData(xval, yval, xnoise, ynoise);

        

        double[] bandwidths = {0.1, 0.5, 1.0};
        double[] variances = new double[bandwidths.length];
        for (int i = 0; i < bandwidths.length; i++) {
            double bw = bandwidths[i];

            LoessInterpolator li = new LoessInterpolator(bw, 4, 1e-12);

            double[] res = li.smooth(xval, yval);

            for (int j = 1; j < res.length; ++j) {
                variances[i] += FastMath.pow(res[j] - res[j-1], 2);
            }
        }

        for(int i = 1; i < variances.length; ++i) {
            Assert.assertTrue(variances[i] < variances[i-1]);
        }
    }

// org.apache.commons.math.analysis.interpolation.LoessInterpolatorTest::testIncreasingRobustnessItersIncreasesSmoothnessWithOutliers
    public void testIncreasingRobustnessItersIncreasesSmoothnessWithOutliers() {
        int numPoints = 100;
        double[] xval = new double[numPoints];
        double[] yval = new double[numPoints];
        double xnoise = 0.1;
        double ynoise = 0.1;

        generateSineData(xval, yval, xnoise, ynoise);

        
        yval[numPoints/3] *= 100;
        yval[2 * numPoints/3] *= -100;

        
        

        double[] variances = new double[4];
        for (int i = 0; i < 4; i++) {
            LoessInterpolator li = new LoessInterpolator(0.3, i, 1e-12);

            double[] res = li.smooth(xval, yval);

            for (int j = 1; j < res.length; ++j) {
                variances[i] += FastMath.abs(res[j] - res[j-1]);
            }
        }

        for(int i = 1; i < variances.length; ++i) {
            Assert.assertTrue(variances[i] < variances[i-1]);
        }
    }

// org.apache.commons.math.analysis.interpolation.LoessInterpolatorTest::testUnequalSizeArguments
    public void testUnequalSizeArguments() {
        new LoessInterpolator().smooth(new double[] {1,2,3}, new double[] {1,2,3,4});
    }

// org.apache.commons.math.analysis.interpolation.LoessInterpolatorTest::testEmptyData
    public void testEmptyData() {
        new LoessInterpolator().smooth(new double[] {}, new double[] {});
    }

// org.apache.commons.math.analysis.interpolation.LoessInterpolatorTest::testNonStrictlyIncreasing1
    public void testNonStrictlyIncreasing1() {
        new LoessInterpolator().smooth(new double[] {4,3,1,2}, new double[] {3,4,5,6});
    }

// org.apache.commons.math.analysis.interpolation.LoessInterpolatorTest::testNonStrictlyIncreasing2
    public void testNonStrictlyIncreasing2() {
        new LoessInterpolator().smooth(new double[] {1,2,2,3}, new double[] {3,4,5,6});
    }

// org.apache.commons.math.analysis.interpolation.LoessInterpolatorTest::testNotAllFiniteReal1
    public void testNotAllFiniteReal1() {
        new LoessInterpolator().smooth(new double[] {1,2,Double.NaN}, new double[] {3,4,5});
    }

// org.apache.commons.math.analysis.interpolation.LoessInterpolatorTest::testNotAllFiniteReal2
    public void testNotAllFiniteReal2() {
        new LoessInterpolator().smooth(new double[] {1,2,Double.POSITIVE_INFINITY}, new double[] {3,4,5});
    }

// org.apache.commons.math.analysis.interpolation.LoessInterpolatorTest::testNotAllFiniteReal3
    public void testNotAllFiniteReal3() {
        new LoessInterpolator().smooth(new double[] {1,2,Double.NEGATIVE_INFINITY}, new double[] {3,4,5});
    }

// org.apache.commons.math.analysis.interpolation.LoessInterpolatorTest::testNotAllFiniteReal4
    public void testNotAllFiniteReal4() {
        new LoessInterpolator().smooth(new double[] {3,4,5}, new double[] {1,2,Double.NaN});
    }

// org.apache.commons.math.analysis.interpolation.LoessInterpolatorTest::testNotAllFiniteReal5
    public void testNotAllFiniteReal5() {
        new LoessInterpolator().smooth(new double[] {3,4,5}, new double[] {1,2,Double.POSITIVE_INFINITY});
    }

// org.apache.commons.math.analysis.interpolation.LoessInterpolatorTest::testNotAllFiniteReal6
    public void testNotAllFiniteReal6() {
        new LoessInterpolator().smooth(new double[] {3,4,5}, new double[] {1,2,Double.NEGATIVE_INFINITY});
    }

// org.apache.commons.math.analysis.interpolation.LoessInterpolatorTest::testInsufficientBandwidth
    public void testInsufficientBandwidth() {
        LoessInterpolator li = new LoessInterpolator(0.1, 3, 1e-12);
        li.smooth(new double[] {1,2,3,4,5,6,7,8,9,10,11,12}, new double[] {1,2,3,4,5,6,7,8,9,10,11,12});
    }

// org.apache.commons.math.analysis.interpolation.LoessInterpolatorTest::testCompletelyIncorrectBandwidth1
    public void testCompletelyIncorrectBandwidth1() {
        new LoessInterpolator(-0.2, 3, 1e-12);
    }

// org.apache.commons.math.analysis.interpolation.LoessInterpolatorTest::testCompletelyIncorrectBandwidth2
    public void testCompletelyIncorrectBandwidth2() {
        new LoessInterpolator(1.1, 3, 1e-12);
    }

// org.apache.commons.math.analysis.interpolation.LoessInterpolatorTest::testMath296withoutWeights
    public void testMath296withoutWeights() {
        double[] xval = {
                0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0,
                 1.1, 1.2, 1.3, 1.4, 1.5, 1.6, 1.7, 1.8, 1.9, 2.0};
        double[] yval = {
                0.47, 0.48, 0.55, 0.56, -0.08, -0.04, -0.07, -0.07,
                -0.56, -0.46, -0.56, -0.52, -3.03, -3.08, -3.09,
                -3.04, 3.54, 3.46, 3.36, 3.35};
        
        double[] yref = {
                0.461, 0.499, 0.541, 0.308, 0.175, -0.042, -0.072,
                -0.196, -0.311, -0.446, -0.557, -1.497, -2.133,
                -3.08, -3.09, -0.621, 0.982, 3.449, 3.389, 3.336
        };
        LoessInterpolator li = new LoessInterpolator(0.3, 4, 1e-12);
        double[] res = li.smooth(xval, yval);
        Assert.assertEquals(xval.length, res.length);
        for(int i = 0; i < res.length; ++i) {
            Assert.assertEquals(yref[i], res[i], 0.02);
        }
    }

// org.apache.commons.math.analysis.interpolation.MicrosphereInterpolatorTest::testLinearFunction2D
    public void testLinearFunction2D() throws MathException {
        MultivariateRealFunction f = new MultivariateRealFunction() {
                public double value(double[] x) {
                    if (x.length != 2) {
                        throw new IllegalArgumentException();
                    }
                    return 2 * x[0] - 3 * x[1] + 5;
                }
            };

        MultivariateRealInterpolator interpolator = new MicrosphereInterpolator();

        
        final int n = 9;
        final int dim = 2;
        double[][] x = new double[n][dim];
        double[] y = new double[n];
        int index = 0;
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                x[index][0] = i;
                x[index][1] = j;
                y[index] = f.value(x[index]);
                ++index;
            }
        }

        MultivariateRealFunction p = interpolator.interpolate(x, y);

        double[] c = new double[dim];
        double expected, result;

        c[0] = 0;
        c[1] = 0;
        expected = f.value(c);
        result = p.value(c);
        Assert.assertEquals("On sample point", expected, result, FastMath.ulp(1d));

        c[0] = 0 + 1e-5;
        c[1] = 1 - 1e-5;
        expected = f.value(c);
        result = p.value(c);
        Assert.assertEquals("1e-5 away from sample point", expected, result, 1e-4);
    }

// org.apache.commons.math.analysis.interpolation.MicrosphereInterpolatorTest::testParaboloid2D
    public void testParaboloid2D() throws MathException {
        MultivariateRealFunction f = new MultivariateRealFunction() {
                public double value(double[] x) {
                    if (x.length != 2) {
                        throw new IllegalArgumentException();
                    }
                    return 2 * x[0] * x[0] - 3 * x[1] * x[1] + 4 * x[0] * x[1] - 5;
                }
            };

        MultivariateRealInterpolator interpolator = new MicrosphereInterpolator();

        
        final int n = 121;
        final int dim = 2;
        double[][] x = new double[n][dim];
        double[] y = new double[n];
        int index = 0;
        for (int i = -10; i <= 10; i += 2) {
            for (int j = -10; j <= 10; j += 2) {
                x[index][0] = i;
                x[index][1] = j;
                y[index] = f.value(x[index]);
                ++index;
            }
        }

        MultivariateRealFunction p = interpolator.interpolate(x, y);

        double[] c = new double[dim];
        double expected, result;

        c[0] = 0;
        c[1] = 0;
        expected = f.value(c);
        result = p.value(c);
        Assert.assertEquals("On sample point", expected, result, FastMath.ulp(1d));

        c[0] = 2 + 1e-5;
        c[1] = 2 - 1e-5;
        expected = f.value(c);
        result = p.value(c);
        Assert.assertEquals("1e-5 away from sample point", expected, result, 1e-3);
    }

// org.apache.commons.math.analysis.interpolation.NevilleInterpolatorTest::testSinFunction
    public void testSinFunction() throws MathException {
        UnivariateRealFunction f = new SinFunction();
        UnivariateRealInterpolator interpolator = new NevilleInterpolator();
        double x[], y[], z, expected, result, tolerance;

        
        int n = 6;
        double min = 0.0, max = 2 * FastMath.PI;
        x = new double[n];
        y = new double[n];
        for (int i = 0; i < n; i++) {
            x[i] = min + i * (max - min) / n;
            y[i] = f.value(x[i]);
        }
        double derivativebound = 1.0;
        UnivariateRealFunction p = interpolator.interpolate(x, y);

        z = FastMath.PI / 4; expected = f.value(z); result = p.value(z);
        tolerance = FastMath.abs(derivativebound * partialerror(x, z));
        assertEquals(expected, result, tolerance);

        z = FastMath.PI * 1.5; expected = f.value(z); result = p.value(z);
        tolerance = FastMath.abs(derivativebound * partialerror(x, z));
        assertEquals(expected, result, tolerance);
    }

// org.apache.commons.math.analysis.interpolation.NevilleInterpolatorTest::testExpm1Function
    public void testExpm1Function() throws MathException {
        UnivariateRealFunction f = new Expm1Function();
        UnivariateRealInterpolator interpolator = new NevilleInterpolator();
        double x[], y[], z, expected, result, tolerance;

        
        int n = 5;
        double min = -1.0, max = 1.0;
        x = new double[n];
        y = new double[n];
        for (int i = 0; i < n; i++) {
            x[i] = min + i * (max - min) / n;
            y[i] = f.value(x[i]);
        }
        double derivativebound = FastMath.E;
        UnivariateRealFunction p = interpolator.interpolate(x, y);

        z = 0.0; expected = f.value(z); result = p.value(z);
        tolerance = FastMath.abs(derivativebound * partialerror(x, z));
        assertEquals(expected, result, tolerance);

        z = 0.5; expected = f.value(z); result = p.value(z);
        tolerance = FastMath.abs(derivativebound * partialerror(x, z));
        assertEquals(expected, result, tolerance);

        z = -0.5; expected = f.value(z); result = p.value(z);
        tolerance = FastMath.abs(derivativebound * partialerror(x, z));
        assertEquals(expected, result, tolerance);
    }

// org.apache.commons.math.analysis.interpolation.NevilleInterpolatorTest::testParameters
    public void testParameters() throws Exception {
        UnivariateRealInterpolator interpolator = new NevilleInterpolator();

        try {
            
            double x[] = { 1.0, 2.0, 2.0, 4.0 };
            double y[] = { 0.0, 4.0, 4.0, 2.5 };
            UnivariateRealFunction p = interpolator.interpolate(x, y);
            p.value(0.0);
            fail("Expecting NonMonotonousSequenceException - bad abscissas array");
        } catch (NonMonotonousSequenceException ex) {
            
        }
    }

// org.apache.commons.math.analysis.interpolation.SmoothingPolynomialBicubicSplineInterpolatorTest::testPreconditions
    public void testPreconditions() throws MathException {
        double[] xval = new double[] {3, 4, 5, 6.5};
        double[] yval = new double[] {-4, -3, -1, 2.5};
        double[][] zval = new double[xval.length][yval.length];

        BivariateRealGridInterpolator interpolator = new SmoothingPolynomialBicubicSplineInterpolator(0);
        
        @SuppressWarnings("unused")
        BivariateRealFunction p = interpolator.interpolate(xval, yval, zval);
        
        double[] wxval = new double[] {3, 2, 5, 6.5};
        try {
            p = interpolator.interpolate(wxval, yval, zval);
            Assert.fail("an exception should have been thrown");
        } catch (MathIllegalArgumentException e) {
            
        }

        double[] wyval = new double[] {-4, -3, -1, -1};
        try {
            p = interpolator.interpolate(xval, wyval, zval);
            Assert.fail("an exception should have been thrown");
        } catch (MathIllegalArgumentException e) {
            
        }

        double[][] wzval = new double[xval.length][yval.length + 1];
        try {
            p = interpolator.interpolate(xval, yval, wzval);
            Assert.fail("an exception should have been thrown");
        } catch (DimensionMismatchException e) {
            
        }
        wzval = new double[xval.length - 1][yval.length];
        try {
            p = interpolator.interpolate(xval, yval, wzval);
            Assert.fail("an exception should have been thrown");
        } catch (DimensionMismatchException e) {
            
        }
        wzval = new double[xval.length][yval.length - 1];
        try {
            p = interpolator.interpolate(xval, yval, wzval);
            Assert.fail("an exception should have been thrown");
        } catch (DimensionMismatchException e) {
            
        }
    }

// org.apache.commons.math.analysis.interpolation.SmoothingPolynomialBicubicSplineInterpolatorTest::testPlane
    public void testPlane() throws MathException {
        BivariateRealFunction f = new BivariateRealFunction() {
                public double value(double x, double y) {
                    return 2 * x - 3 * y + 5
                        + ((int) (FastMath.abs(5 * x + 3 * y)) % 2 == 0 ? 1 : -1);
                }
            };

        BivariateRealGridInterpolator interpolator = new SmoothingPolynomialBicubicSplineInterpolator(1);

        double[] xval = new double[] {3, 4, 5, 6.5};
        double[] yval = new double[] {-4, -3, -1, 2, 2.5};
        double[][] zval = new double[xval.length][yval.length];
        for (int i = 0; i < xval.length; i++) {
            for (int j = 0; j < yval.length; j++) {
                zval[i][j] = f.value(xval[i], yval[j]);
            }
        }

        BivariateRealFunction p = interpolator.interpolate(xval, yval, zval);
        double x, y;
        double expected, result;
        
        x = 4;
        y = -3;
        expected = f.value(x, y);
        result = p.value(x, y);
        Assert.assertEquals("On sample point", expected, result, 2);

        x = 4.5;
        y = -1.5;
        expected = f.value(x, y);
        result = p.value(x, y);
        Assert.assertEquals("half-way between sample points (middle of the patch)", expected, result, 2);

        x = 3.5;
        y = -3.5;
        expected = f.value(x, y);
        result = p.value(x, y);
        Assert.assertEquals("half-way between sample points (border of the patch)", expected, result, 2);
    }

// org.apache.commons.math.analysis.interpolation.SmoothingPolynomialBicubicSplineInterpolatorTest::testParaboloid
    public void testParaboloid() throws MathException {
        BivariateRealFunction f = new BivariateRealFunction() {
                public double value(double x, double y) {
                    return 2 * x * x - 3 * y * y + 4 * x * y - 5
                        + ((int) (FastMath.abs(5 * x + 3 * y)) % 2 == 0 ? 1 : -1);
                }
            };

        BivariateRealGridInterpolator interpolator = new SmoothingPolynomialBicubicSplineInterpolator(4);

        double[] xval = new double[] {3, 4, 5, 6.5};
        double[] yval = new double[] {-4, -3, -2, -1, 0.5, 2.5};
        double[][] zval = new double[xval.length][yval.length];
        for (int i = 0; i < xval.length; i++) {
            for (int j = 0; j < yval.length; j++) {
                zval[i][j] = f.value(xval[i], yval[j]);
            }
        }

        BivariateRealFunction p = interpolator.interpolate(xval, yval, zval);
        double x, y;
        double expected, result;

        x = 5;
        y = 0.5;
        expected = f.value(x, y);
        result = p.value(x, y);
        Assert.assertEquals("On sample point", expected, result, 2);

        x = 4.5;
        y = -1.5;
        expected = f.value(x, y);
        result = p.value(x, y);
        Assert.assertEquals("half-way between sample points (middle of the patch)", expected, result, 2);

        x = 3.5;
        y = -3.5;
        expected = f.value(x, y);
        result = p.value(x, y);
        Assert.assertEquals("half-way between sample points (border of the patch)", expected, result, 2);
    }

// org.apache.commons.math.analysis.interpolation.SplineInterpolatorTest::testInterpolateLinearDegenerateTwoSegment
    public void testInterpolateLinearDegenerateTwoSegment()
        throws Exception {
        double x[] = { 0.0, 0.5, 1.0 };
        double y[] = { 0.0, 0.5, 1.0 };
        UnivariateRealInterpolator i = new SplineInterpolator();
        UnivariateRealFunction f = i.interpolate(x, y);
        verifyInterpolation(f, x, y);
        verifyConsistency((PolynomialSplineFunction) f, x);

        
        PolynomialFunction polynomials[] = ((PolynomialSplineFunction) f).getPolynomials();
        double target[] = {y[0], 1d};
        TestUtils.assertEquals(polynomials[0].getCoefficients(), target, coefficientTolerance);
        target = new double[]{y[1], 1d};
        TestUtils.assertEquals(polynomials[1].getCoefficients(), target, coefficientTolerance);

        
        Assert.assertEquals(0.0,f.value(0.0), interpolationTolerance);
        Assert.assertEquals(0.4,f.value(0.4), interpolationTolerance);
        Assert.assertEquals(1.0,f.value(1.0), interpolationTolerance);
    }

// org.apache.commons.math.analysis.interpolation.SplineInterpolatorTest::testInterpolateLinearDegenerateThreeSegment
    public void testInterpolateLinearDegenerateThreeSegment()
        throws Exception {
        double x[] = { 0.0, 0.5, 1.0, 1.5 };
        double y[] = { 0.0, 0.5, 1.0, 1.5 };
        UnivariateRealInterpolator i = new SplineInterpolator();
        UnivariateRealFunction f = i.interpolate(x, y);
        verifyInterpolation(f, x, y);

        
        PolynomialFunction polynomials[] = ((PolynomialSplineFunction) f).getPolynomials();
        double target[] = {y[0], 1d};
        TestUtils.assertEquals(polynomials[0].getCoefficients(), target, coefficientTolerance);
        target = new double[]{y[1], 1d};
        TestUtils.assertEquals(polynomials[1].getCoefficients(), target, coefficientTolerance);
        target = new double[]{y[2], 1d};
        TestUtils.assertEquals(polynomials[2].getCoefficients(), target, coefficientTolerance);

        
        Assert.assertEquals(0,f.value(0), interpolationTolerance);
        Assert.assertEquals(1.4,f.value(1.4), interpolationTolerance);
        Assert.assertEquals(1.5,f.value(1.5), interpolationTolerance);
    }

// org.apache.commons.math.analysis.interpolation.SplineInterpolatorTest::testInterpolateLinear
    public void testInterpolateLinear() throws Exception {
        double x[] = { 0.0, 0.5, 1.0 };
        double y[] = { 0.0, 0.5, 0.0 };
        UnivariateRealInterpolator i = new SplineInterpolator();
        UnivariateRealFunction f = i.interpolate(x, y);
        verifyInterpolation(f, x, y);
        verifyConsistency((PolynomialSplineFunction) f, x);

        
        PolynomialFunction polynomials[] = ((PolynomialSplineFunction) f).getPolynomials();
        double target[] = {y[0], 1.5d, 0d, -2d};
        TestUtils.assertEquals(polynomials[0].getCoefficients(), target, coefficientTolerance);
        target = new double[]{y[1], 0d, -3d, 2d};
        TestUtils.assertEquals(polynomials[1].getCoefficients(), target, coefficientTolerance);
    }

// org.apache.commons.math.analysis.interpolation.SplineInterpolatorTest::testInterpolateSin
    public void testInterpolateSin() throws Exception {
        double x[] =
            {
                0.0,
                FastMath.PI / 6d,
                FastMath.PI / 2d,
                5d * FastMath.PI / 6d,
                FastMath.PI,
                7d * FastMath.PI / 6d,
                3d * FastMath.PI / 2d,
                11d * FastMath.PI / 6d,
                2.d * FastMath.PI };
        double y[] = { 0d, 0.5d, 1d, 0.5d, 0d, -0.5d, -1d, -0.5d, 0d };
        UnivariateRealInterpolator i = new SplineInterpolator();
        UnivariateRealFunction f = i.interpolate(x, y);
        verifyInterpolation(f, x, y);
        verifyConsistency((PolynomialSplineFunction) f, x);

        
        PolynomialFunction polynomials[] = ((PolynomialSplineFunction) f).getPolynomials();
        double target[] = {y[0], 1.002676d, 0d, -0.17415829d};
        TestUtils.assertEquals(polynomials[0].getCoefficients(), target, coefficientTolerance);
        target = new double[]{y[1], 8.594367e-01, -2.735672e-01, -0.08707914};
        TestUtils.assertEquals(polynomials[1].getCoefficients(), target, coefficientTolerance);
        target = new double[]{y[2], 1.471804e-17,-5.471344e-01, 0.08707914};
        TestUtils.assertEquals(polynomials[2].getCoefficients(), target, coefficientTolerance);
        target = new double[]{y[3], -8.594367e-01, -2.735672e-01, 0.17415829};
        TestUtils.assertEquals(polynomials[3].getCoefficients(), target, coefficientTolerance);
        target = new double[]{y[4], -1.002676, 6.548562e-17, 0.17415829};
        TestUtils.assertEquals(polynomials[4].getCoefficients(), target, coefficientTolerance);
        target = new double[]{y[5], -8.594367e-01, 2.735672e-01, 0.08707914};
        TestUtils.assertEquals(polynomials[5].getCoefficients(), target, coefficientTolerance);
        target = new double[]{y[6], 3.466465e-16, 5.471344e-01, -0.08707914};
        TestUtils.assertEquals(polynomials[6].getCoefficients(), target, coefficientTolerance);
        target = new double[]{y[7], 8.594367e-01, 2.735672e-01, -0.17415829};
        TestUtils.assertEquals(polynomials[7].getCoefficients(), target, coefficientTolerance);

        
        Assert.assertEquals(FastMath.sqrt(2d) / 2d,f.value(FastMath.PI/4d),interpolationTolerance);
        Assert.assertEquals(FastMath.sqrt(2d) / 2d,f.value(3d*FastMath.PI/4d),interpolationTolerance);
    }

// org.apache.commons.math.analysis.interpolation.SplineInterpolatorTest::testIllegalArguments
    public void testIllegalArguments() throws MathException {
        
        UnivariateRealInterpolator i = new SplineInterpolator();
        try {
            double xval[] = { 0.0, 1.0 };
            double yval[] = { 0.0, 1.0, 2.0 };
            i.interpolate(xval, yval);
            Assert.fail("Failed to detect data set array with different sizes.");
        } catch (DimensionMismatchException iae) {
            
        }
        
        try {
            double xval[] = { 0.0, 1.0, 0.5 };
            double yval[] = { 0.0, 1.0, 2.0 };
            i.interpolate(xval, yval);
            Assert.fail("Failed to detect unsorted arguments.");
        } catch (NonMonotonousSequenceException iae) {
            
        }
        
        try {
            double xval[] = { 0.0, 1.0 };
            double yval[] = { 0.0, 1.0 };
            i.interpolate(xval, yval);
            Assert.fail("Failed to detect unsorted arguments.");
        } catch (NumberIsTooSmallException iae) {
            
        }
    }

// org.apache.commons.math.analysis.interpolation.TricubicSplineInterpolatingFunctionTest::testPreconditions
    public void testPreconditions() throws Exception {
        double[] xval = new double[] {3, 4, 5, 6.5};
        double[] yval = new double[] {-4, -3, -1, 2.5};
        double[] zval = new double[] {-12, -8, -5.5, -3, 0, 2.5};
        double[][][] fval = new double[xval.length][yval.length][zval.length];

        @SuppressWarnings("unused")
        TrivariateRealFunction tcf = new TricubicSplineInterpolatingFunction(xval, yval, zval,
                                                                             fval, fval, fval, fval,
                                                                             fval, fval, fval, fval);
        
        double[] wxval = new double[] {3, 2, 5, 6.5};
        try {
            tcf = new TricubicSplineInterpolatingFunction(wxval, yval, zval,
                                                          fval, fval, fval, fval,
                                                          fval, fval, fval, fval);
            Assert.fail("an exception should have been thrown");
        } catch (MathIllegalArgumentException e) {
            
        }
        double[] wyval = new double[] {-4, -1, -1, 2.5};
        try {
            tcf = new TricubicSplineInterpolatingFunction(xval, wyval, zval,
                                                          fval, fval, fval, fval,
                                                          fval, fval, fval, fval);
            Assert.fail("an exception should have been thrown");
        } catch (MathIllegalArgumentException e) {
            
        }
        double[] wzval = new double[] {-12, -8, -9, -3, 0, 2.5};
        try {
            tcf = new TricubicSplineInterpolatingFunction(xval, yval, wzval,
                                                          fval, fval, fval, fval,
                                                          fval, fval, fval, fval);
            Assert.fail("an exception should have been thrown");
        } catch (MathIllegalArgumentException e) {
            
        }
        double[][][] wfval = new double[xval.length - 1][yval.length - 1][zval.length];
        try {
            tcf = new TricubicSplineInterpolatingFunction(xval, yval, zval,
                                                          wfval, fval, fval, fval,
                                                          fval, fval, fval, fval);
            Assert.fail("an exception should have been thrown");
        } catch (DimensionMismatchException e) {
            
        }
        try {
            tcf = new TricubicSplineInterpolatingFunction(xval, yval, zval,
                                                          fval, wfval, fval, fval,
                                                          fval, fval, fval, fval);
            Assert.fail("an exception should have been thrown");
        } catch (DimensionMismatchException e) {
            
        }
        try {
            tcf = new TricubicSplineInterpolatingFunction(xval, yval, zval,
                                                          fval, fval, wfval, fval,
                                                          fval, fval, fval, fval);
            Assert.fail("an exception should have been thrown");
        } catch (DimensionMismatchException e) {
            
        }
        try {
            tcf = new TricubicSplineInterpolatingFunction(xval, yval, zval,
                                                          fval, fval, fval, wfval,
                                                          fval, fval, fval, fval);
            Assert.fail("an exception should have been thrown");
        } catch (DimensionMismatchException e) {
            
        }
        try {
            tcf = new TricubicSplineInterpolatingFunction(xval, yval, zval,
                                                          fval, fval, fval, fval,
                                                          wfval, fval, fval, fval);
            Assert.fail("an exception should have been thrown");
        } catch (DimensionMismatchException e) {
            
        }
        try {
            tcf = new TricubicSplineInterpolatingFunction(xval, yval, zval,
                                                          fval, fval, fval, fval,
                                                          fval, wfval, fval, fval);
            Assert.fail("an exception should have been thrown");
        } catch (DimensionMismatchException e) {
            
        }
        try {
            tcf = new TricubicSplineInterpolatingFunction(xval, yval, zval,
                                                          fval, fval, fval, fval,
                                                          fval, fval, wfval, fval);
            Assert.fail("an exception should have been thrown");
        } catch (DimensionMismatchException e) {
            
        }
        try {
            tcf = new TricubicSplineInterpolatingFunction(xval, yval, zval,
                                                          fval, fval, fval, fval,
                                                          fval, fval, fval, wfval);
            Assert.fail("an exception should have been thrown");
        } catch (DimensionMismatchException e) {
            
        }
        wfval = new double[xval.length][yval.length - 1][zval.length];
        try {
            tcf = new TricubicSplineInterpolatingFunction(xval, yval, zval,
                                                          wfval, fval, fval, fval,
                                                          fval, fval, fval, fval);
            Assert.fail("an exception should have been thrown");
        } catch (DimensionMismatchException e) {
            
        }
        try {
            tcf = new TricubicSplineInterpolatingFunction(xval, yval, zval,
                                                          fval, wfval, fval, fval,
                                                          fval, fval, fval, fval);
            Assert.fail("an exception should have been thrown");
        } catch (DimensionMismatchException e) {
            
        }
        try {
            tcf = new TricubicSplineInterpolatingFunction(xval, yval, zval,
                                                          fval, fval, wfval, fval,
                                                          fval, fval, fval, fval);
            Assert.fail("an exception should have been thrown");
        } catch (DimensionMismatchException e) {
            
        }
        try {
            tcf = new TricubicSplineInterpolatingFunction(xval, yval, zval,
                                                          fval, fval, fval, wfval,
                                                          fval, fval, fval, fval);
            Assert.fail("an exception should have been thrown");
        } catch (DimensionMismatchException e) {
            
        }
        try {
            tcf = new TricubicSplineInterpolatingFunction(xval, yval, zval,
                                                          fval, fval, fval, fval,
                                                          wfval, fval, fval, fval);
            Assert.fail("an exception should have been thrown");
        } catch (DimensionMismatchException e) {
            
        }
        try {
            tcf = new TricubicSplineInterpolatingFunction(xval, yval, zval,
                                                          fval, fval, fval, fval,
                                                          fval, wfval, fval, fval);
            Assert.fail("an exception should have been thrown");
        } catch (DimensionMismatchException e) {
            
        }
        try {
            tcf = new TricubicSplineInterpolatingFunction(xval, yval, zval,
                                                          fval, fval, fval, fval,
                                                          fval, fval, wfval, fval);
            Assert.fail("an exception should have been thrown");
        } catch (DimensionMismatchException e) {
            
        }
        try {
            tcf = new TricubicSplineInterpolatingFunction(xval, yval, zval,
                                                          fval, fval, fval, fval,
                                                          fval, fval, fval, wfval);
            Assert.fail("an exception should have been thrown");
        } catch (DimensionMismatchException e) {
            
        }
        wfval = new double[xval.length][yval.length][zval.length - 1];
        try {
            tcf = new TricubicSplineInterpolatingFunction(xval, yval, zval,
                                                          wfval, fval, fval, fval,
                                                          fval, fval, fval, fval);
            Assert.fail("an exception should have been thrown");
        } catch (DimensionMismatchException e) {
            
        }
        try {
            tcf = new TricubicSplineInterpolatingFunction(xval, yval, zval,
                                                          fval, wfval, fval, fval,
                                                          fval, fval, fval, fval);
            Assert.fail("an exception should have been thrown");
        } catch (DimensionMismatchException e) {
            
        }
        try {
            tcf = new TricubicSplineInterpolatingFunction(xval, yval, zval,
                                                          fval, fval, wfval, fval,
                                                          fval, fval, fval, fval);
            Assert.fail("an exception should have been thrown");
        } catch (DimensionMismatchException e) {
            
        }
        try {
            tcf = new TricubicSplineInterpolatingFunction(xval, yval, zval,
                                                          fval, fval, fval, wfval,
                                                          fval, fval, fval, fval);
            Assert.fail("an exception should have been thrown");
        } catch (DimensionMismatchException e) {
            
        }
        try {
            tcf = new TricubicSplineInterpolatingFunction(xval, yval, zval,
                                                          fval, fval, fval, fval,
                                                          wfval, fval, fval, fval);
            Assert.fail("an exception should have been thrown");
        } catch (DimensionMismatchException e) {
            
        }
        try {
            tcf = new TricubicSplineInterpolatingFunction(xval, yval, zval,
                                                          fval, fval, fval, fval,
                                                          fval, wfval, fval, fval);
            Assert.fail("an exception should have been thrown");
        } catch (DimensionMismatchException e) {
            
        }
        try {
            tcf = new TricubicSplineInterpolatingFunction(xval, yval, zval,
                                                          fval, fval, fval, fval,
                                                          fval, fval, wfval, fval);
            Assert.fail("an exception should have been thrown");
        } catch (DimensionMismatchException e) {
            
        }
        try {
            tcf = new TricubicSplineInterpolatingFunction(xval, yval, zval,
                                                          fval, fval, fval, fval,
                                                          fval, fval, fval, wfval);
            Assert.fail("an exception should have been thrown");
        } catch (DimensionMismatchException e) {
            
        }
    }

// org.apache.commons.math.analysis.interpolation.TricubicSplineInterpolatingFunctionTest::testPlane
    public void testPlane() {
        double[] xval = new double[] {3, 4, 5, 6.5};
        double[] yval = new double[] {-4, -3, -1, 2, 2.5};
        double[] zval = new double[] {-12, -8, -5.5, -3, 0, 2.5};

        
        TrivariateRealFunction f = new TrivariateRealFunction() {
                public double value(double x, double y, double z) {
                    return 2 * x - 3 * y - 4 * z + 5;
                }
            };

        double[][][] fval = new double[xval.length][yval.length][zval.length];

        for (int i = 0; i < xval.length; i++) {
            for (int j = 0; j < yval.length; j++) {
                for (int k = 0; k < zval.length; k++) {
                    fval[i][j][k] = f.value(xval[i], yval[j], zval[k]);
                }
            }
        }
        
        double[][][] dFdX = new double[xval.length][yval.length][zval.length];
        for (int i = 0; i < xval.length; i++) {
            for (int j = 0; j < yval.length; j++) {
                for (int k = 0; k < zval.length; k++) {
                    dFdX[i][j][k] = 2;
                }
            }
        }
        
        double[][][] dFdY = new double[xval.length][yval.length][zval.length];
        for (int i = 0; i < xval.length; i++) {
            for (int j = 0; j < yval.length; j++) {
                for (int k = 0; k < zval.length; k++) {
                    dFdY[i][j][k] = -3;
                }
            }
        }

        
        double[][][] dFdZ = new double[xval.length][yval.length][zval.length];
        for (int i = 0; i < xval.length; i++) {
            for (int j = 0; j < yval.length; j++) {
                for (int k = 0; k < zval.length; k++) {
                    dFdZ[i][j][k] = -4;
                }
            }
        }
        
        double[][][] d2FdXdY = new double[xval.length][yval.length][zval.length];
        double[][][] d2FdXdZ = new double[xval.length][yval.length][zval.length];
        double[][][] d2FdYdZ = new double[xval.length][yval.length][zval.length];
        double[][][] d3FdXdYdZ = new double[xval.length][yval.length][zval.length];
        for (int i = 0; i < xval.length; i++) {
            for (int j = 0; j < yval.length; j++) {
                for (int k = 0; k < zval.length; k++) {
                    d2FdXdY[i][j][k] = 0;
                    d2FdXdZ[i][j][k] = 0;
                    d2FdYdZ[i][j][k] = 0;
                    d3FdXdYdZ[i][j][k] = 0;
                }
            }
        }

        TrivariateRealFunction tcf = new TricubicSplineInterpolatingFunction(xval, yval, zval,
                                                                             fval, dFdX, dFdY, dFdZ,
                                                                             d2FdXdY, d2FdXdZ, d2FdYdZ,
                                                                             d3FdXdYdZ);
        double x, y, z;
        double expected, result;

        x = 4;
        y = -3;
        z = 0;
        expected = f.value(x, y, z);
        result = tcf.value(x, y, z);
        Assert.assertEquals("On sample point",
                            expected, result, 1e-15);

        x = 4.5;
        y = -1.5;
        z = -4.25;
        expected = f.value(x, y, z);
        result = tcf.value(x, y, z);
        Assert.assertEquals("Half-way between sample points (middle of the patch)",
                            expected, result, 0.3);

        x = 3.5;
        y = -3.5;
        z = -10;
        expected = f.value(x, y, z);
        result = tcf.value(x, y, z);
        Assert.assertEquals("Half-way between sample points (border of the patch)",
                            expected, result, 0.3);
    }

// org.apache.commons.math.analysis.interpolation.TricubicSplineInterpolatingFunctionTest::testWave
    public void testWave() {
        double[] xval = new double[] {3, 4, 5, 6.5};
        double[] yval = new double[] {-4, -3, -1, 2, 2.5};
        double[] zval = new double[] {-12, -8, -5.5, -3, 0, 4};
        
        final double a = 0.2;
        final double omega = 0.5;
        final double kx = 2;
        final double ky = 1;
        
        
        TrivariateRealFunction f = new TrivariateRealFunction() {
                public double value(double x, double y, double z) {
                    return a * FastMath.cos(omega * z - kx * x - ky * y);
                }
            };
        
        double[][][] fval = new double[xval.length][yval.length][zval.length];
        for (int i = 0; i < xval.length; i++) {
            for (int j = 0; j < yval.length; j++) {
                for (int k = 0; k < zval.length; k++) {
                    fval[i][j][k] = f.value(xval[i], yval[j], zval[k]);
                }
            }
        }
        
        
        double[][][] dFdX = new double[xval.length][yval.length][zval.length];
        TrivariateRealFunction dFdX_f = new TrivariateRealFunction() {
                public double value(double x, double y, double z) {
                    return a * FastMath.sin(omega * z - kx * x - ky * y) * kx;
                }
            };
        for (int i = 0; i < xval.length; i++) {
            for (int j = 0; j < yval.length; j++) {
                for (int k = 0; k < zval.length; k++) {
                    dFdX[i][j][k] = dFdX_f.value(xval[i], yval[j], zval[k]);
                }
            }
        }
            
        
        double[][][] dFdY = new double[xval.length][yval.length][zval.length];
        TrivariateRealFunction dFdY_f = new TrivariateRealFunction() {
                public double value(double x, double y, double z) {
                    return a * FastMath.sin(omega * z - kx * x - ky * y) * ky;
                }
            };
        for (int i = 0; i < xval.length; i++) {
            for (int j = 0; j < yval.length; j++) {
                for (int k = 0; k < zval.length; k++) {
                    dFdY[i][j][k] = dFdY_f.value(xval[i], yval[j], zval[k]);
                }
            }
        }

        
        double[][][] dFdZ = new double[xval.length][yval.length][zval.length];
        TrivariateRealFunction dFdZ_f = new TrivariateRealFunction() {
                public double value(double x, double y, double z) {
                    return -a * FastMath.sin(omega * z - kx * x - ky * y) * omega;
                }
            };
        for (int i = 0; i < xval.length; i++) {
            for (int j = 0; j < yval.length; j++) {
                for (int k = 0; k < zval.length; k++) {
                    dFdZ[i][j][k] = dFdZ_f.value(xval[i], yval[j], zval[k]);
                }
            }
        }

        
        double[][][] d2FdXdY = new double[xval.length][yval.length][zval.length];
        TrivariateRealFunction d2FdXdY_f = new TrivariateRealFunction() {
                public double value(double x, double y, double z) {
                    return -a * FastMath.cos(omega * z - kx * x - ky * y) * kx * ky;
                }
            };
        for (int i = 0; i < xval.length; i++) {
            for (int j = 0; j < yval.length; j++) {
                for (int k = 0; k < zval.length; k++) {
                    d2FdXdY[i][j][k] = d2FdXdY_f.value(xval[i], yval[j], zval[k]);
                }
            }
        }

        
        double[][][] d2FdXdZ = new double[xval.length][yval.length][zval.length];
        TrivariateRealFunction d2FdXdZ_f = new TrivariateRealFunction() {
                public double value(double x, double y, double z) {
                    return a * FastMath.cos(omega * z - kx * x - ky * y) * kx * omega;
                }
            };
        for (int i = 0; i < xval.length; i++) {
            for (int j = 0; j < yval.length; j++) {
                for (int k = 0; k < zval.length; k++) {
                    d2FdXdZ[i][j][k] = d2FdXdZ_f.value(xval[i], yval[j], zval[k]);
                }
            }
        }

        
        double[][][] d2FdYdZ = new double[xval.length][yval.length][zval.length];
        TrivariateRealFunction d2FdYdZ_f = new TrivariateRealFunction() {
                public double value(double x, double y, double z) {
                    return a * FastMath.cos(omega * z - kx * x - ky * y) * ky * omega;
                }
            };
        for (int i = 0; i < xval.length; i++) {
            for (int j = 0; j < yval.length; j++) {
                for (int k = 0; k < zval.length; k++) {
                    d2FdYdZ[i][j][k] = d2FdYdZ_f.value(xval[i], yval[j], zval[k]);
                }
            }
        }

        
        double[][][] d3FdXdYdZ = new double[xval.length][yval.length][zval.length];
        TrivariateRealFunction d3FdXdYdZ_f = new TrivariateRealFunction() {
                public double value(double x, double y, double z) {
                    return a * FastMath.sin(omega * z - kx * x - ky * y) * kx * ky * omega;
                }
            };
        for (int i = 0; i < xval.length; i++) {
            for (int j = 0; j < yval.length; j++) {
                for (int k = 0; k < zval.length; k++) {
                    d3FdXdYdZ[i][j][k] = d3FdXdYdZ_f.value(xval[i], yval[j], zval[k]);
                }
            }
        }

        TrivariateRealFunction tcf = new TricubicSplineInterpolatingFunction(xval, yval, zval,
                                                                             fval, dFdX, dFdY, dFdZ,
                                                                             d2FdXdY, d2FdXdZ, d2FdYdZ,
                                                                             d3FdXdYdZ);
        double x, y, z;
        double expected, result;
        
        x = 4;
        y = -3;
        z = 0;
        expected = f.value(x, y, z);
        result = tcf.value(x, y, z);
        Assert.assertEquals("On sample point",
                            expected, result, 1e-14);

        x = 4.5;
        y = -1.5;
        z = -4.25;
        expected = f.value(x, y, z);
        result = tcf.value(x, y, z);
        Assert.assertEquals("Half-way between sample points (middle of the patch)",
                            expected, result, 0.1);

        x = 3.5;
        y = -3.5;
        z = -10;
        expected = f.value(x, y, z);
        result = tcf.value(x, y, z);
        Assert.assertEquals("Half-way between sample points (border of the patch)",
                            expected, result, 0.1);
    }

// org.apache.commons.math.analysis.interpolation.TricubicSplineInterpolatorTest::testPreconditions
    public void testPreconditions() throws MathException {
        double[] xval = new double[] {3, 4, 5, 6.5};
        double[] yval = new double[] {-4, -3, -1, 2.5};
        double[] zval = new double[] {-12, -8, -5.5, -3, 0, 2.5};
        double[][][] fval = new double[xval.length][yval.length][zval.length];

        TrivariateRealGridInterpolator interpolator = new TricubicSplineInterpolator();
        
        @SuppressWarnings("unused")
        TrivariateRealFunction p = interpolator.interpolate(xval, yval, zval, fval);
        
        double[] wxval = new double[] {3, 2, 5, 6.5};
        try {
            p = interpolator.interpolate(wxval, yval, zval, fval);
            Assert.fail("an exception should have been thrown");
        } catch (MathIllegalArgumentException e) {
            
        }

        double[] wyval = new double[] {-4, -3, -1, -1};
        try {
            p = interpolator.interpolate(xval, wyval, zval, fval);
            Assert.fail("an exception should have been thrown");
        } catch (MathIllegalArgumentException e) {
            
        }

        double[] wzval = new double[] {-12, -8, -5.5, -3, -4, 2.5};
        try {
            p = interpolator.interpolate(xval, yval, wzval, fval);
            Assert.fail("an exception should have been thrown");
        } catch (MathIllegalArgumentException e) {
            
        }

        double[][][] wfval = new double[xval.length][yval.length + 1][zval.length];
        try {
            p = interpolator.interpolate(xval, yval, zval, wfval);
            Assert.fail("an exception should have been thrown");
        } catch (DimensionMismatchException e) {
            
        }
        wfval = new double[xval.length - 1][yval.length][zval.length];
        try {
            p = interpolator.interpolate(xval, yval, zval, wfval);
            Assert.fail("an exception should have been thrown");
        } catch (DimensionMismatchException e) {
            
        }
        wfval = new double[xval.length][yval.length][zval.length - 1];
        try {
            p = interpolator.interpolate(xval, yval, zval, wfval);
            Assert.fail("an exception should have been thrown");
        } catch (DimensionMismatchException e) {
            
        }
    }

// org.apache.commons.math.analysis.interpolation.TricubicSplineInterpolatorTest::testPlane
    public void testPlane() throws MathException {
        TrivariateRealFunction f = new TrivariateRealFunction() {
                public double value(double x, double y, double z) {
                    return 2 * x - 3 * y - z + 5;
                }
            };

        TrivariateRealGridInterpolator interpolator = new TricubicSplineInterpolator();

        double[] xval = new double[] {3, 4, 5, 6.5};
        double[] yval = new double[] {-4, -3, -1, 2, 2.5};
        double[] zval = new double[] {-12, -8, -5.5, -3, 0, 2.5};
        double[][][] fval = new double[xval.length][yval.length][zval.length];
        for (int i = 0; i < xval.length; i++) {
            for (int j = 0; j < yval.length; j++) {
                for (int k = 0; k < zval.length; k++) {
                    fval[i][j][k] = f.value(xval[i], yval[j], zval[k]);
                }
            }
        }

        TrivariateRealFunction p = interpolator.interpolate(xval, yval, zval, fval);
        double x, y, z;
        double expected, result;
        
        x = 4;
        y = -3;
        z = 0;
        expected = f.value(x, y, z);
        result = p.value(x, y, z);
        Assert.assertEquals("On sample point", expected, result, 1e-15);

        x = 4.5;
        y = -1.5;
        z = -4.25;
        expected = f.value(x, y, z);
        result = p.value(x, y, z);
        Assert.assertEquals("half-way between sample points (middle of the patch)", expected, result, 0.3);

        x = 3.5;
        y = -3.5;
        z = -10;
        expected = f.value(x, y, z);
        result = p.value(x, y, z);
        Assert.assertEquals("half-way between sample points (border of the patch)", expected, result, 0.3);
    }

// org.apache.commons.math.analysis.interpolation.TricubicSplineInterpolatorTest::testWave
    public void testWave() throws MathException {
        double[] xval = new double[] {3, 4, 5, 6.5};
        double[] yval = new double[] {-4, -3, -1, 2, 2.5};
        double[] zval = new double[] {-12, -8, -5.5, -3, 0, 4};

        final double a = 0.2;
        final double omega = 0.5;
        final double kx = 2;
        final double ky = 1;

        
        TrivariateRealFunction f = new TrivariateRealFunction() {
                public double value(double x, double y, double z) {
                    return a * FastMath.cos(omega * z - kx * x - ky * y);
                }
            };
        
        double[][][] fval = new double[xval.length][yval.length][zval.length];
        for (int i = 0; i < xval.length; i++) {
            for (int j = 0; j < yval.length; j++) {
                for (int k = 0; k < zval.length; k++) {
                    fval[i][j][k] = f.value(xval[i], yval[j], zval[k]);
                }
            }
        }

        TrivariateRealGridInterpolator interpolator = new TricubicSplineInterpolator();

        TrivariateRealFunction p = interpolator.interpolate(xval, yval, zval, fval);
        double x, y, z;
        double expected, result;
        
        x = 4;
        y = -3;
        z = 0;
        expected = f.value(x, y, z);
        result = p.value(x, y, z);
        Assert.assertEquals("On sample point",
                            expected, result, 1e-12);

        x = 4.5;
        y = -1.5;
        z = -4.25;
        expected = f.value(x, y, z);
        result = p.value(x, y, z);
        Assert.assertEquals("Half-way between sample points (middle of the patch)",
                            expected, result, 0.1);

        x = 3.5;
        y = -3.5;
        z = -10;
        expected = f.value(x, y, z);
        result = p.value(x, y, z);
        Assert.assertEquals("Half-way between sample points (border of the patch)",
                            expected, result, 0.1);
    }

// org.apache.commons.math.analysis.polynomials.PolynomialFunctionLagrangeFormTest::testLinearFunction
    public void testLinearFunction() {
        PolynomialFunctionLagrangeForm p;
        double c[], z, expected, result, tolerance = 1E-12;

        
        double x[] = { 0.0, 3.0 };
        double y[] = { -4.0, 0.5 };
        p = new PolynomialFunctionLagrangeForm(x, y);

        z = 2.0; expected = -1.0; result = p.value(z);
        assertEquals(expected, result, tolerance);

        z = 4.5; expected = 2.75; result = p.value(z);
        assertEquals(expected, result, tolerance);

        z = 6.0; expected = 5.0; result = p.value(z);
        assertEquals(expected, result, tolerance);

        assertEquals(1, p.degree());

        c = p.getCoefficients();
        assertEquals(2, c.length);
        assertEquals(-4.0, c[0], tolerance);
        assertEquals(1.5, c[1], tolerance);
    }

// org.apache.commons.math.analysis.polynomials.PolynomialFunctionLagrangeFormTest::testQuadraticFunction
    public void testQuadraticFunction() {
        PolynomialFunctionLagrangeForm p;
        double c[], z, expected, result, tolerance = 1E-12;

        
        double x[] = { 0.0, -1.0, 0.5 };
        double y[] = { -3.0, -6.0, 0.0 };
        p = new PolynomialFunctionLagrangeForm(x, y);

        z = 1.0; expected = 4.0; result = p.value(z);
        assertEquals(expected, result, tolerance);

        z = 2.5; expected = 22.0; result = p.value(z);
        assertEquals(expected, result, tolerance);

        z = -2.0; expected = -5.0; result = p.value(z);
        assertEquals(expected, result, tolerance);

        assertEquals(2, p.degree());

        c = p.getCoefficients();
        assertEquals(3, c.length);
        assertEquals(-3.0, c[0], tolerance);
        assertEquals(5.0, c[1], tolerance);
        assertEquals(2.0, c[2], tolerance);
    }

// org.apache.commons.math.analysis.polynomials.PolynomialFunctionLagrangeFormTest::testQuinticFunction
    public void testQuinticFunction() {
        PolynomialFunctionLagrangeForm p;
        double c[], z, expected, result, tolerance = 1E-12;

        
        double x[] = { 1.0, -1.0, 2.0, 3.0, -3.0, 0.5 };
        double y[] = { 0.0, 0.0, -24.0, 0.0, -144.0, 2.34375 };
        p = new PolynomialFunctionLagrangeForm(x, y);

        z = 0.0; expected = 0.0; result = p.value(z);
        assertEquals(expected, result, tolerance);

        z = -2.0; expected = 0.0; result = p.value(z);
        assertEquals(expected, result, tolerance);

        z = 4.0; expected = 360.0; result = p.value(z);
        assertEquals(expected, result, tolerance);

        assertEquals(5, p.degree());

        c = p.getCoefficients();
        assertEquals(6, c.length);
        assertEquals(0.0, c[0], tolerance);
        assertEquals(6.0, c[1], tolerance);
        assertEquals(1.0, c[2], tolerance);
        assertEquals(-7.0, c[3], tolerance);
        assertEquals(-1.0, c[4], tolerance);
        assertEquals(1.0, c[5], tolerance);
    }

// org.apache.commons.math.analysis.polynomials.PolynomialFunctionLagrangeFormTest::testParameters
    public void testParameters() throws Exception {

        try {
            
            double x[] = { 1.0 };
            double y[] = { 2.0 };
            new PolynomialFunctionLagrangeForm(x, y);
            fail("Expecting MathIllegalArgumentException - bad input array length");
        } catch (MathIllegalArgumentException ex) {
            
        }
        try {
            
            double x[] = { 1.0, 2.0, 3.0, 4.0 };
            double y[] = { 0.0, -4.0, -24.0 };
            new PolynomialFunctionLagrangeForm(x, y);
            fail("Expecting MathIllegalArgumentException - mismatch input arrays");
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.analysis.polynomials.PolynomialFunctionTest::testConstants
    public void testConstants() {
        double[] c = { 2.5 };
        PolynomialFunction f = new PolynomialFunction( c );

        
        assertEquals( f.value( 0.0), c[0], tolerance );
        assertEquals( f.value( -1.0), c[0], tolerance );
        assertEquals( f.value( -123.5), c[0], tolerance );
        assertEquals( f.value( 3.0), c[0], tolerance );
        assertEquals( f.value( 456.89), c[0], tolerance );

        assertEquals(f.degree(), 0);
        assertEquals(f.derivative().value(0), 0, tolerance);

        assertEquals(f.polynomialDerivative().derivative().value(0), 0, tolerance);
    }

// org.apache.commons.math.analysis.polynomials.PolynomialFunctionTest::testLinear
    public void testLinear() {
        double[] c = { -1.5, 3.0 };
        PolynomialFunction f = new PolynomialFunction( c );

        
        assertEquals( f.value( 0.0), c[0], tolerance );

        
        assertEquals( -4.5, f.value( -1.0), tolerance );
        assertEquals( -9.0, f.value( -2.5), tolerance );
        assertEquals( 0.0, f.value( 0.5), tolerance );
        assertEquals( 3.0, f.value( 1.5), tolerance );
        assertEquals( 7.5, f.value( 3.0), tolerance );

        assertEquals(f.degree(), 1);

        assertEquals(f.polynomialDerivative().derivative().value(0), 0, tolerance);

    }

// org.apache.commons.math.analysis.polynomials.PolynomialFunctionTest::testQuadratic
    public void testQuadratic() {
        double[] c = { -2.0, -3.0, 2.0 };
        PolynomialFunction f = new PolynomialFunction( c );

        
        assertEquals( f.value( 0.0), c[0], tolerance );

        
        assertEquals( 0.0, f.value( -0.5), tolerance );
        assertEquals( 0.0, f.value( 2.0), tolerance );
        assertEquals( -2.0, f.value( 1.5), tolerance );
        assertEquals( 7.0, f.value( -1.5), tolerance );
        assertEquals( 265.5312, f.value( 12.34), tolerance );

    }

// org.apache.commons.math.analysis.polynomials.PolynomialFunctionTest::testQuintic
    public void testQuintic() {
        double[] c = { 0.0, 0.0, 15.0, -13.0, -3.0, 1.0 };
        PolynomialFunction f = new PolynomialFunction( c );

        
        assertEquals( f.value( 0.0), c[0], tolerance );

        
        assertEquals( 0.0, f.value( 5.0), tolerance );
        assertEquals( 0.0, f.value( 1.0), tolerance );
        assertEquals( 0.0, f.value( -3.0), tolerance );
        assertEquals( 54.84375, f.value( -1.5), tolerance );
        assertEquals( -8.06637, f.value( 1.3), tolerance );

        assertEquals(f.degree(), 5);

    }

// org.apache.commons.math.analysis.polynomials.PolynomialFunctionTest::testfirstDerivativeComparison
    public void testfirstDerivativeComparison() {
        double[] f_coeff = { 3.0, 6.0, -2.0, 1.0 };
        double[] g_coeff = { 6.0, -4.0, 3.0 };
        double[] h_coeff = { -4.0, 6.0 };

        PolynomialFunction f = new PolynomialFunction( f_coeff );
        PolynomialFunction g = new PolynomialFunction( g_coeff );
        PolynomialFunction h = new PolynomialFunction( h_coeff );

        
        assertEquals( f.derivative().value(0.0), g.value(0.0), tolerance );
        assertEquals( f.derivative().value(1.0), g.value(1.0), tolerance );
        assertEquals( f.derivative().value(100.0), g.value(100.0), tolerance );
        assertEquals( f.derivative().value(4.1), g.value(4.1), tolerance );
        assertEquals( f.derivative().value(-3.25), g.value(-3.25), tolerance );

        
        assertEquals( g.derivative().value(FastMath.PI), h.value(FastMath.PI), tolerance );
        assertEquals( g.derivative().value(FastMath.E),  h.value(FastMath.E),  tolerance );

    }

// org.apache.commons.math.analysis.polynomials.PolynomialFunctionTest::testString
    public void testString() {
        PolynomialFunction p = new PolynomialFunction(new double[] { -5.0, 3.0, 1.0 });
        checkPolynomial(p, "-5.0 + 3.0 x + x^2");
        checkPolynomial(new PolynomialFunction(new double[] { 0.0, -2.0, 3.0 }),
                        "-2.0 x + 3.0 x^2");
        checkPolynomial(new PolynomialFunction(new double[] { 1.0, -2.0, 3.0 }),
                      "1.0 - 2.0 x + 3.0 x^2");
        checkPolynomial(new PolynomialFunction(new double[] { 0.0,  2.0, 3.0 }),
                       "2.0 x + 3.0 x^2");
        checkPolynomial(new PolynomialFunction(new double[] { 1.0,  2.0, 3.0 }),
                     "1.0 + 2.0 x + 3.0 x^2");
        checkPolynomial(new PolynomialFunction(new double[] { 1.0,  0.0, 3.0 }),
                     "1.0 + 3.0 x^2");
        checkPolynomial(new PolynomialFunction(new double[] { 0.0 }),
                     "0");
    }

// org.apache.commons.math.analysis.polynomials.PolynomialFunctionTest::testAddition
    public void testAddition() {

        PolynomialFunction p1 = new PolynomialFunction(new double[] { -2.0, 1.0 });
        PolynomialFunction p2 = new PolynomialFunction(new double[] { 2.0, -1.0, 0.0 });
        checkNullPolynomial(p1.add(p2));

        p2 = p1.add(p1);
        checkPolynomial(p2, "-4.0 + 2.0 x");

        p1 = new PolynomialFunction(new double[] { 1.0, -4.0, 2.0 });
        p2 = new PolynomialFunction(new double[] { -1.0, 3.0, -2.0 });
        p1 = p1.add(p2);
        assertEquals(1, p1.degree());
        checkPolynomial(p1, "-x");

    }

// org.apache.commons.math.analysis.polynomials.PolynomialFunctionTest::testSubtraction
    public void testSubtraction() {

        PolynomialFunction p1 = new PolynomialFunction(new double[] { -2.0, 1.0 });
        checkNullPolynomial(p1.subtract(p1));

        PolynomialFunction p2 = new PolynomialFunction(new double[] { -2.0, 6.0 });
        p2 = p2.subtract(p1);
        checkPolynomial(p2, "5.0 x");

        p1 = new PolynomialFunction(new double[] { 1.0, -4.0, 2.0 });
        p2 = new PolynomialFunction(new double[] { -1.0, 3.0, 2.0 });
        p1 = p1.subtract(p2);
        assertEquals(1, p1.degree());
        checkPolynomial(p1, "2.0 - 7.0 x");

    }

// org.apache.commons.math.analysis.polynomials.PolynomialFunctionTest::testMultiplication
    public void testMultiplication() {

        PolynomialFunction p1 = new PolynomialFunction(new double[] { -3.0, 2.0 });
        PolynomialFunction p2 = new PolynomialFunction(new double[] { 3.0, 2.0, 1.0 });
        checkPolynomial(p1.multiply(p2), "-9.0 + x^2 + 2.0 x^3");

        p1 = new PolynomialFunction(new double[] { 0.0, 1.0 });
        p2 = p1;
        for (int i = 2; i < 10; ++i) {
            p2 = p2.multiply(p1);
            checkPolynomial(p2, "x^" + i);
        }

    }

// org.apache.commons.math.analysis.polynomials.PolynomialFunctionTest::testSerial
    public void testSerial() {
        PolynomialFunction p2 = new PolynomialFunction(new double[] { 3.0, 2.0, 1.0 });
        assertEquals(p2, TestUtils.serializeAndRecover(p2));
    }

// org.apache.commons.math.analysis.polynomials.PolynomialFunctionTest::testMath341
    public void testMath341() {
        double[] f_coeff = { 3.0, 6.0, -2.0, 1.0 };
        double[] g_coeff = { 6.0, -4.0, 3.0 };
        double[] h_coeff = { -4.0, 6.0 };

        PolynomialFunction f = new PolynomialFunction( f_coeff );
        PolynomialFunction g = new PolynomialFunction( g_coeff );
        PolynomialFunction h = new PolynomialFunction( h_coeff );

        
        assertEquals( f.derivative().value(0.0), g.value(0.0), tolerance );
        assertEquals( f.derivative().value(1.0), g.value(1.0), tolerance );
        assertEquals( f.derivative().value(100.0), g.value(100.0), tolerance );
        assertEquals( f.derivative().value(4.1), g.value(4.1), tolerance );
        assertEquals( f.derivative().value(-3.25), g.value(-3.25), tolerance );

        
        assertEquals( g.derivative().value(FastMath.PI), h.value(FastMath.PI), tolerance );
        assertEquals( g.derivative().value(FastMath.E),  h.value(FastMath.E),  tolerance );
    }

// org.apache.commons.math.analysis.polynomials.PolynomialsUtilsTest::testFirstChebyshevPolynomials
    public void testFirstChebyshevPolynomials() {

        checkPolynomial(PolynomialsUtils.createChebyshevPolynomial(3), "-3.0 x + 4.0 x^3");
        checkPolynomial(PolynomialsUtils.createChebyshevPolynomial(2), "-1.0 + 2.0 x^2");
        checkPolynomial(PolynomialsUtils.createChebyshevPolynomial(1), "x");
        checkPolynomial(PolynomialsUtils.createChebyshevPolynomial(0), "1.0");

        checkPolynomial(PolynomialsUtils.createChebyshevPolynomial(7), "-7.0 x + 56.0 x^3 - 112.0 x^5 + 64.0 x^7");
        checkPolynomial(PolynomialsUtils.createChebyshevPolynomial(6), "-1.0 + 18.0 x^2 - 48.0 x^4 + 32.0 x^6");
        checkPolynomial(PolynomialsUtils.createChebyshevPolynomial(5), "5.0 x - 20.0 x^3 + 16.0 x^5");
        checkPolynomial(PolynomialsUtils.createChebyshevPolynomial(4), "1.0 - 8.0 x^2 + 8.0 x^4");

    }

// org.apache.commons.math.analysis.polynomials.PolynomialsUtilsTest::testChebyshevBounds
    public void testChebyshevBounds() {
        for (int k = 0; k < 12; ++k) {
            PolynomialFunction Tk = PolynomialsUtils.createChebyshevPolynomial(k);
            for (double x = -1.0; x <= 1.0; x += 0.02) {
                assertTrue(k + " " + Tk.value(x), FastMath.abs(Tk.value(x)) < (1.0 + 1.0e-12));
            }
        }
    }

// org.apache.commons.math.analysis.polynomials.PolynomialsUtilsTest::testChebyshevDifferentials
    public void testChebyshevDifferentials() {
        for (int k = 0; k < 12; ++k) {

            PolynomialFunction Tk0 = PolynomialsUtils.createChebyshevPolynomial(k);
            PolynomialFunction Tk1 = Tk0.polynomialDerivative();
            PolynomialFunction Tk2 = Tk1.polynomialDerivative();

            PolynomialFunction g0 = new PolynomialFunction(new double[] { k * k });
            PolynomialFunction g1 = new PolynomialFunction(new double[] { 0, -1});
            PolynomialFunction g2 = new PolynomialFunction(new double[] { 1, 0, -1 });

            PolynomialFunction Tk0g0 = Tk0.multiply(g0);
            PolynomialFunction Tk1g1 = Tk1.multiply(g1);
            PolynomialFunction Tk2g2 = Tk2.multiply(g2);

            checkNullPolynomial(Tk0g0.add(Tk1g1.add(Tk2g2)));

        }
    }

// org.apache.commons.math.analysis.polynomials.PolynomialsUtilsTest::testFirstHermitePolynomials
    public void testFirstHermitePolynomials() {

        checkPolynomial(PolynomialsUtils.createHermitePolynomial(3), "-12.0 x + 8.0 x^3");
        checkPolynomial(PolynomialsUtils.createHermitePolynomial(2), "-2.0 + 4.0 x^2");
        checkPolynomial(PolynomialsUtils.createHermitePolynomial(1), "2.0 x");
        checkPolynomial(PolynomialsUtils.createHermitePolynomial(0), "1.0");

        checkPolynomial(PolynomialsUtils.createHermitePolynomial(7), "-1680.0 x + 3360.0 x^3 - 1344.0 x^5 + 128.0 x^7");
        checkPolynomial(PolynomialsUtils.createHermitePolynomial(6), "-120.0 + 720.0 x^2 - 480.0 x^4 + 64.0 x^6");
        checkPolynomial(PolynomialsUtils.createHermitePolynomial(5), "120.0 x - 160.0 x^3 + 32.0 x^5");
        checkPolynomial(PolynomialsUtils.createHermitePolynomial(4), "12.0 - 48.0 x^2 + 16.0 x^4");

    }

// org.apache.commons.math.analysis.polynomials.PolynomialsUtilsTest::testHermiteDifferentials
    public void testHermiteDifferentials() {
        for (int k = 0; k < 12; ++k) {

            PolynomialFunction Hk0 = PolynomialsUtils.createHermitePolynomial(k);
            PolynomialFunction Hk1 = Hk0.polynomialDerivative();
            PolynomialFunction Hk2 = Hk1.polynomialDerivative();

            PolynomialFunction g0 = new PolynomialFunction(new double[] { 2 * k });
            PolynomialFunction g1 = new PolynomialFunction(new double[] { 0, -2 });
            PolynomialFunction g2 = new PolynomialFunction(new double[] { 1 });

            PolynomialFunction Hk0g0 = Hk0.multiply(g0);
            PolynomialFunction Hk1g1 = Hk1.multiply(g1);
            PolynomialFunction Hk2g2 = Hk2.multiply(g2);

            checkNullPolynomial(Hk0g0.add(Hk1g1.add(Hk2g2)));

        }
    }

// org.apache.commons.math.analysis.polynomials.PolynomialsUtilsTest::testFirstLaguerrePolynomials
    public void testFirstLaguerrePolynomials() {

        checkPolynomial(PolynomialsUtils.createLaguerrePolynomial(3), 6l, "6.0 - 18.0 x + 9.0 x^2 - x^3");
        checkPolynomial(PolynomialsUtils.createLaguerrePolynomial(2), 2l, "2.0 - 4.0 x + x^2");
        checkPolynomial(PolynomialsUtils.createLaguerrePolynomial(1), 1l, "1.0 - x");
        checkPolynomial(PolynomialsUtils.createLaguerrePolynomial(0), 1l, "1.0");

        checkPolynomial(PolynomialsUtils.createLaguerrePolynomial(7), 5040l,
                "5040.0 - 35280.0 x + 52920.0 x^2 - 29400.0 x^3"
                + " + 7350.0 x^4 - 882.0 x^5 + 49.0 x^6 - x^7");
        checkPolynomial(PolynomialsUtils.createLaguerrePolynomial(6),  720l,
                "720.0 - 4320.0 x + 5400.0 x^2 - 2400.0 x^3 + 450.0 x^4"
                + " - 36.0 x^5 + x^6");
        checkPolynomial(PolynomialsUtils.createLaguerrePolynomial(5),  120l,
        "120.0 - 600.0 x + 600.0 x^2 - 200.0 x^3 + 25.0 x^4 - x^5");
        checkPolynomial(PolynomialsUtils.createLaguerrePolynomial(4),   24l,
        "24.0 - 96.0 x + 72.0 x^2 - 16.0 x^3 + x^4");

    }

// org.apache.commons.math.analysis.polynomials.PolynomialsUtilsTest::testLaguerreDifferentials
    public void testLaguerreDifferentials() {
        for (int k = 0; k < 12; ++k) {

            PolynomialFunction Lk0 = PolynomialsUtils.createLaguerrePolynomial(k);
            PolynomialFunction Lk1 = Lk0.polynomialDerivative();
            PolynomialFunction Lk2 = Lk1.polynomialDerivative();

            PolynomialFunction g0 = new PolynomialFunction(new double[] { k });
            PolynomialFunction g1 = new PolynomialFunction(new double[] { 1, -1 });
            PolynomialFunction g2 = new PolynomialFunction(new double[] { 0, 1 });

            PolynomialFunction Lk0g0 = Lk0.multiply(g0);
            PolynomialFunction Lk1g1 = Lk1.multiply(g1);
            PolynomialFunction Lk2g2 = Lk2.multiply(g2);

            checkNullPolynomial(Lk0g0.add(Lk1g1.add(Lk2g2)));

        }
    }

// org.apache.commons.math.analysis.polynomials.PolynomialsUtilsTest::testFirstLegendrePolynomials
    public void testFirstLegendrePolynomials() {

        checkPolynomial(PolynomialsUtils.createLegendrePolynomial(3),  2l, "-3.0 x + 5.0 x^3");
        checkPolynomial(PolynomialsUtils.createLegendrePolynomial(2),  2l, "-1.0 + 3.0 x^2");
        checkPolynomial(PolynomialsUtils.createLegendrePolynomial(1),  1l, "x");
        checkPolynomial(PolynomialsUtils.createLegendrePolynomial(0),  1l, "1.0");

        checkPolynomial(PolynomialsUtils.createLegendrePolynomial(7), 16l, "-35.0 x + 315.0 x^3 - 693.0 x^5 + 429.0 x^7");
        checkPolynomial(PolynomialsUtils.createLegendrePolynomial(6), 16l, "-5.0 + 105.0 x^2 - 315.0 x^4 + 231.0 x^6");
        checkPolynomial(PolynomialsUtils.createLegendrePolynomial(5),  8l, "15.0 x - 70.0 x^3 + 63.0 x^5");
        checkPolynomial(PolynomialsUtils.createLegendrePolynomial(4),  8l, "3.0 - 30.0 x^2 + 35.0 x^4");

    }

// org.apache.commons.math.analysis.polynomials.PolynomialsUtilsTest::testLegendreDifferentials
    public void testLegendreDifferentials() {
        for (int k = 0; k < 12; ++k) {

            PolynomialFunction Pk0 = PolynomialsUtils.createLegendrePolynomial(k);
            PolynomialFunction Pk1 = Pk0.polynomialDerivative();
            PolynomialFunction Pk2 = Pk1.polynomialDerivative();

            PolynomialFunction g0 = new PolynomialFunction(new double[] { k * (k + 1) });
            PolynomialFunction g1 = new PolynomialFunction(new double[] { 0, -2 });
            PolynomialFunction g2 = new PolynomialFunction(new double[] { 1, 0, -1 });

            PolynomialFunction Pk0g0 = Pk0.multiply(g0);
            PolynomialFunction Pk1g1 = Pk1.multiply(g1);
            PolynomialFunction Pk2g2 = Pk2.multiply(g2);

            checkNullPolynomial(Pk0g0.add(Pk1g1.add(Pk2g2)));

        }
    }

// org.apache.commons.math.analysis.polynomials.PolynomialsUtilsTest::testHighDegreeLegendre
    public void testHighDegreeLegendre() {
        PolynomialsUtils.createLegendrePolynomial(40);
        double[] l40 = PolynomialsUtils.createLegendrePolynomial(40).getCoefficients();
        double denominator = 274877906944.0;
        double[] numerators = new double[] {
                          +34461632205.0,            -28258538408100.0,          +3847870979902950.0,        -207785032914759300.0,
                  +5929294332103310025.0,     -103301483474866556880.0,    +1197358103913226000200.0,    -9763073770369381232400.0,
              +58171647881784229843050.0,  -260061484647976556945400.0,  +888315281771246239250340.0, -2345767627188139419665400.0,
            +4819022625419112503443050.0, -7710436200670580005508880.0, +9566652323054238154983240.0, -9104813935044723209570256.0,
            +6516550296251767619752905.0, -3391858621221953912598660.0, +1211378079007840683070950.0,  -265365894974690562152100.0,
              +26876802183334044115405.0
        };
        for (int i = 0; i < l40.length; ++i) {
            if (i % 2 == 0) {
                double ci = numerators[i / 2] / denominator;
                assertEquals(ci, l40[i], FastMath.abs(ci) * 1.0e-15);
            } else {
                assertEquals(0.0, l40[i], 0.0);
            }
        }
    }

// org.apache.commons.math.analysis.solvers.BisectionSolverTest::testSinZero
    public void testSinZero() {
        UnivariateRealFunction f = new SinFunction();
        double result;

        BisectionSolver solver = new BisectionSolver();
        result = solver.solve(100, f, 3, 4);
        Assert.assertEquals(result, FastMath.PI, solver.getAbsoluteAccuracy());

        result = solver.solve(100, f, 1, 4);
        Assert.assertEquals(result, FastMath.PI, solver.getAbsoluteAccuracy());
    }

// org.apache.commons.math.analysis.solvers.BisectionSolverTest::testQuinticZero
    public void testQuinticZero() {
        UnivariateRealFunction f = new QuinticFunction();
        double result;

        BisectionSolver solver = new BisectionSolver();
        result = solver.solve(100, f, -0.2, 0.2);
        Assert.assertEquals(result, 0, solver.getAbsoluteAccuracy());

        result = solver.solve(100, f, -0.1, 0.3);
        Assert.assertEquals(result, 0, solver.getAbsoluteAccuracy());

        result = solver.solve(100, f, -0.3, 0.45);
        Assert.assertEquals(result, 0, solver.getAbsoluteAccuracy());

        result = solver.solve(100, f, 0.3, 0.7);
        Assert.assertEquals(result, 0.5, solver.getAbsoluteAccuracy());

        result = solver.solve(100, f, 0.2, 0.6);
        Assert.assertEquals(result, 0.5, solver.getAbsoluteAccuracy());

        result = solver.solve(100, f, 0.05, 0.95);
        Assert.assertEquals(result, 0.5, solver.getAbsoluteAccuracy());

        result = solver.solve(100, f, 0.85, 1.25);
        Assert.assertEquals(result, 1.0, solver.getAbsoluteAccuracy());

        result = solver.solve(100, f, 0.8, 1.2);
        Assert.assertEquals(result, 1.0, solver.getAbsoluteAccuracy());

        result = solver.solve(100, f, 0.85, 1.75);
        Assert.assertEquals(result, 1.0, solver.getAbsoluteAccuracy());

        result = solver.solve(100, f, 0.55, 1.45);
        Assert.assertEquals(result, 1.0, solver.getAbsoluteAccuracy());

        result = solver.solve(100, f, 0.85, 5);
        Assert.assertEquals(result, 1.0, solver.getAbsoluteAccuracy());

        Assert.assertTrue(solver.getEvaluations() > 0);
    }

// org.apache.commons.math.analysis.solvers.BisectionSolverTest::testMath369
    public void testMath369() {
        UnivariateRealFunction f = new SinFunction();
        BisectionSolver solver = new BisectionSolver();
        Assert.assertEquals(FastMath.PI, solver.solve(100, f, 3.0, 3.2, 3.1), solver.getAbsoluteAccuracy());
    }

// org.apache.commons.math.analysis.solvers.BrentSolverTest::testSinZero
    public void testSinZero() {
        
        
        
        UnivariateRealFunction f = new SinFunction();
        double result;
        UnivariateRealSolver solver = new BrentSolver();
        
        result = solver.solve(100, f, 3, 4);
        
        
        Assert.assertEquals(result, FastMath.PI, solver.getAbsoluteAccuracy());
        Assert.assertTrue(solver.getEvaluations() <= 7);
        
        result = solver.solve(100, f, 1, 4);
        
        
        Assert.assertEquals(result, FastMath.PI, solver.getAbsoluteAccuracy());
        Assert.assertTrue(solver.getEvaluations() <= 8);
    }

// org.apache.commons.math.analysis.solvers.BrentSolverTest::testQuinticZero
    public void testQuinticZero() {
        
        
        
        
        
        
        
        UnivariateRealFunction f = new QuinticFunction();
        double result;
        
        UnivariateRealSolver solver = new BrentSolver();
        
        
        result = solver.solve(100, f, -0.2, 0.2);
        
        
        Assert.assertEquals(result, 0, solver.getAbsoluteAccuracy());
        Assert.assertTrue(solver.getEvaluations() <= 3);
        
        
        result = solver.solve(100, f, -0.1, 0.3);
        
        
        Assert.assertEquals(result, 0, solver.getAbsoluteAccuracy());
        
        Assert.assertTrue(solver.getEvaluations() <= 7);
        
        result = solver.solve(100, f, -0.3, 0.45);
        
        
        Assert.assertEquals(result, 0, solver.getAbsoluteAccuracy());
        
        Assert.assertTrue(solver.getEvaluations() <= 8);
        
        result = solver.solve(100, f, 0.3, 0.7);
        
        
        Assert.assertEquals(result, 0.5, solver.getAbsoluteAccuracy());
        
        Assert.assertTrue(solver.getEvaluations() <= 9);
        
        result = solver.solve(100, f, 0.2, 0.6);
        
        
        Assert.assertEquals(result, 0.5, solver.getAbsoluteAccuracy());
        Assert.assertTrue(solver.getEvaluations() <= 10);
        
        result = solver.solve(100, f, 0.05, 0.95);
        
        
        Assert.assertEquals(result, 0.5, solver.getAbsoluteAccuracy());
        Assert.assertTrue(solver.getEvaluations() <= 11);
        
        
        result = solver.solve(100, f, 0.85, 1.25);
        
        
        Assert.assertEquals(result, 1.0, solver.getAbsoluteAccuracy());
        Assert.assertTrue(solver.getEvaluations() <= 11);
        
        result = solver.solve(100, f, 0.8, 1.2);
        
        
        Assert.assertEquals(result, 1.0, solver.getAbsoluteAccuracy());
        Assert.assertTrue(solver.getEvaluations() <= 11);
        
        result = solver.solve(100, f, 0.85, 1.75);
        
        
        Assert.assertEquals(result, 1.0, solver.getAbsoluteAccuracy());
        Assert.assertTrue(solver.getEvaluations() <= 13);
        
        result = solver.solve(100, f, 0.55, 1.45);
        
        
        Assert.assertEquals(result, 1.0, solver.getAbsoluteAccuracy());
        Assert.assertTrue(solver.getEvaluations() <= 10);
        
        result = solver.solve(100, f, 0.85, 5);
        
       
        Assert.assertEquals(result, 1.0, solver.getAbsoluteAccuracy());
        Assert.assertTrue(solver.getEvaluations() <= 15);

        try {
            result = solver.solve(5, f, 0.85, 5);
        } catch (TooManyEvaluationsException e) {
            
        }
    }

// org.apache.commons.math.analysis.solvers.BrentSolverTest::testRootEndpoints
    public void testRootEndpoints() {
        UnivariateRealFunction f = new SinFunction();
        BrentSolver solver = new BrentSolver();

        
        double result = solver.solve(100, f, FastMath.PI, 4);
        Assert.assertEquals(FastMath.PI, result, solver.getAbsoluteAccuracy());

        result = solver.solve(100, f, 3, FastMath.PI);
        Assert.assertEquals(FastMath.PI, result, solver.getAbsoluteAccuracy());

        result = solver.solve(100, f, FastMath.PI, 4, 3.5);
        Assert.assertEquals(FastMath.PI, result, solver.getAbsoluteAccuracy());

        result = solver.solve(100, f, 3, FastMath.PI, 3.07);
        Assert.assertEquals(FastMath.PI, result, solver.getAbsoluteAccuracy());
    }

// org.apache.commons.math.analysis.solvers.BrentSolverTest::testBadEndpoints
    public void testBadEndpoints() {
        UnivariateRealFunction f = new SinFunction();
        BrentSolver solver = new BrentSolver();
        try {  
            solver.solve(100, f, 1, -1);
            Assert.fail("Expecting NumberIsTooLargeException - bad interval");
        } catch (NumberIsTooLargeException ex) {
            
        }
        try {  
            solver.solve(100, f, 1, 1.5);
            Assert.fail("Expecting NoBracketingException - non-bracketing");
        } catch (NoBracketingException ex) {
            
        }
        try {  
            solver.solve(100, f, 1, 1.5, 1.2);
            Assert.fail("Expecting NoBracketingException - non-bracketing");
        } catch (NoBracketingException ex) {
            
        }
    }

// org.apache.commons.math.analysis.solvers.BrentSolverTest::testInitialGuess
    public void testInitialGuess() {
        MonitoredFunction f = new MonitoredFunction(new QuinticFunction());
        BrentSolver solver = new BrentSolver();
        double result;

        
        result = solver.solve(100, f, 0.6, 7.0);
        Assert.assertEquals(result, 1.0, solver.getAbsoluteAccuracy());
        int referenceCallsCount = f.getCallsCount();
        Assert.assertTrue(referenceCallsCount >= 13);

        
        try {
          result = solver.solve(100, f, 0.6, 7.0, 0.0);
          Assert.fail("a NumberIsTooLargeException was expected");
        } catch (NumberIsTooLargeException iae) {
            
        }

        
        f.setCallsCount(0);
        result = solver.solve(100, f, 0.6, 7.0, 0.61);
        Assert.assertEquals(result, 1.0, solver.getAbsoluteAccuracy());
        Assert.assertTrue(f.getCallsCount() > referenceCallsCount);

        
        f.setCallsCount(0);
        result = solver.solve(100, f, 0.6, 7.0, 0.999999);
        Assert.assertEquals(result, 1.0, solver.getAbsoluteAccuracy());
        Assert.assertTrue(f.getCallsCount() < referenceCallsCount);

        
        f.setCallsCount(0);
        result = solver.solve(100, f, 0.6, 7.0, 1.0);
        Assert.assertEquals(result, 1.0, solver.getAbsoluteAccuracy());
        Assert.assertEquals(1, solver.getEvaluations());
        Assert.assertEquals(1, f.getCallsCount());
    }

// org.apache.commons.math.analysis.solvers.LaguerreSolverTest::testLinearFunction
    public void testLinearFunction() {
        double min, max, expected, result, tolerance;

        
        double coefficients[] = { -1.0, 4.0 };
        PolynomialFunction f = new PolynomialFunction(coefficients);
        LaguerreSolver solver = new LaguerreSolver();

        min = 0.0; max = 1.0; expected = 0.25;
        tolerance = FastMath.max(solver.getAbsoluteAccuracy(),
                    FastMath.abs(expected * solver.getRelativeAccuracy()));
        result = solver.solve(100, f, min, max);
        Assert.assertEquals(expected, result, tolerance);
    }

// org.apache.commons.math.analysis.solvers.LaguerreSolverTest::testQuadraticFunction
    public void testQuadraticFunction() {
        double min, max, expected, result, tolerance;

        
        double coefficients[] = { -3.0, 5.0, 2.0 };
        PolynomialFunction f = new PolynomialFunction(coefficients);
        LaguerreSolver solver = new LaguerreSolver();

        min = 0.0; max = 2.0; expected = 0.5;
        tolerance = FastMath.max(solver.getAbsoluteAccuracy(),
                    FastMath.abs(expected * solver.getRelativeAccuracy()));
        result = solver.solve(100, f, min, max);
        Assert.assertEquals(expected, result, tolerance);

        min = -4.0; max = -1.0; expected = -3.0;
        tolerance = FastMath.max(solver.getAbsoluteAccuracy(),
                    FastMath.abs(expected * solver.getRelativeAccuracy()));
        result = solver.solve(100, f, min, max);
        Assert.assertEquals(expected, result, tolerance);
    }

// org.apache.commons.math.analysis.solvers.LaguerreSolverTest::testQuinticFunction
    public void testQuinticFunction() {
        double min, max, expected, result, tolerance;

        
        double coefficients[] = { -12.0, -1.0, 1.0, -12.0, -1.0, 1.0 };
        PolynomialFunction f = new PolynomialFunction(coefficients);
        LaguerreSolver solver = new LaguerreSolver();

        min = -2.0; max = 2.0; expected = -1.0;
        tolerance = FastMath.max(solver.getAbsoluteAccuracy(),
                    FastMath.abs(expected * solver.getRelativeAccuracy()));
        result = solver.solve(100, f, min, max);
        Assert.assertEquals(expected, result, tolerance);

        min = -5.0; max = -2.5; expected = -3.0;
        tolerance = FastMath.max(solver.getAbsoluteAccuracy(),
                    FastMath.abs(expected * solver.getRelativeAccuracy()));
        result = solver.solve(100, f, min, max);
        Assert.assertEquals(expected, result, tolerance);

        min = 3.0; max = 6.0; expected = 4.0;
        tolerance = FastMath.max(solver.getAbsoluteAccuracy(),
                    FastMath.abs(expected * solver.getRelativeAccuracy()));
        result = solver.solve(100, f, min, max);
        Assert.assertEquals(expected, result, tolerance);
    }

// org.apache.commons.math.analysis.solvers.LaguerreSolverTest::testParameters
    public void testParameters() {
        double coefficients[] = { -3.0, 5.0, 2.0 };
        PolynomialFunction f = new PolynomialFunction(coefficients);
        LaguerreSolver solver = new LaguerreSolver();

        try {
            
            solver.solve(100, f, 1, -1);
            Assert.fail("Expecting NumberIsTooLargeException - bad interval");
        } catch (NumberIsTooLargeException ex) {
            
        }
        try {
            
            solver.solve(100, f, 2, 3);
            Assert.fail("Expecting NoBracketingException - no bracketing");
        } catch (NoBracketingException ex) {
            
        }
    }

// org.apache.commons.math.analysis.solvers.MullerSolver2Test::testSinFunction
    public void testSinFunction() {
        UnivariateRealFunction f = new SinFunction();
        UnivariateRealSolver solver = new MullerSolver2();
        double min, max, expected, result, tolerance;

        min = 3.0; max = 4.0; expected = FastMath.PI;
        tolerance = FastMath.max(solver.getAbsoluteAccuracy(),
                    FastMath.abs(expected * solver.getRelativeAccuracy()));
        result = solver.solve(100, f, min, max);
        Assert.assertEquals(expected, result, tolerance);

        min = -1.0; max = 1.5; expected = 0.0;
        tolerance = FastMath.max(solver.getAbsoluteAccuracy(),
                    FastMath.abs(expected * solver.getRelativeAccuracy()));
        result = solver.solve(100, f, min, max);
        Assert.assertEquals(expected, result, tolerance);
    }

// org.apache.commons.math.analysis.solvers.MullerSolver2Test::testQuinticFunction
    public void testQuinticFunction() {
        UnivariateRealFunction f = new QuinticFunction();
        UnivariateRealSolver solver = new MullerSolver2();
        double min, max, expected, result, tolerance;

        min = -0.4; max = 0.2; expected = 0.0;
        tolerance = FastMath.max(solver.getAbsoluteAccuracy(),
                    FastMath.abs(expected * solver.getRelativeAccuracy()));
        result = solver.solve(100, f, min, max);
        Assert.assertEquals(expected, result, tolerance);

        min = 0.75; max = 1.5; expected = 1.0;
        tolerance = FastMath.max(solver.getAbsoluteAccuracy(),
                    FastMath.abs(expected * solver.getRelativeAccuracy()));
        result = solver.solve(100, f, min, max);
        Assert.assertEquals(expected, result, tolerance);

        min = -0.9; max = -0.2; expected = -0.5;
        tolerance = FastMath.max(solver.getAbsoluteAccuracy(),
                    FastMath.abs(expected * solver.getRelativeAccuracy()));
        result = solver.solve(100, f, min, max);
        Assert.assertEquals(expected, result, tolerance);
    }

// org.apache.commons.math.analysis.solvers.MullerSolver2Test::testExpm1Function
    public void testExpm1Function() {
        UnivariateRealFunction f = new Expm1Function();
        UnivariateRealSolver solver = new MullerSolver2();
        double min, max, expected, result, tolerance;

        min = -1.0; max = 2.0; expected = 0.0;
        tolerance = FastMath.max(solver.getAbsoluteAccuracy(),
                    FastMath.abs(expected * solver.getRelativeAccuracy()));
        result = solver.solve(100, f, min, max);
        Assert.assertEquals(expected, result, tolerance);

        min = -20.0; max = 10.0; expected = 0.0;
        tolerance = FastMath.max(solver.getAbsoluteAccuracy(),
                    FastMath.abs(expected * solver.getRelativeAccuracy()));
        result = solver.solve(100, f, min, max);
        Assert.assertEquals(expected, result, tolerance);

        min = -50.0; max = 100.0; expected = 0.0;
        tolerance = FastMath.max(solver.getAbsoluteAccuracy(),
                    FastMath.abs(expected * solver.getRelativeAccuracy()));
        result = solver.solve(100, f, min, max);
        Assert.assertEquals(expected, result, tolerance);
    }

// org.apache.commons.math.analysis.solvers.MullerSolver2Test::testParameters
    public void testParameters() throws Exception {
        UnivariateRealFunction f = new SinFunction();
        UnivariateRealSolver solver = new MullerSolver2();

        try {
            
            solver.solve(100, f, 1, -1);
            Assert.fail("Expecting NumberIsTooLargeException - bad interval");
        } catch (NumberIsTooLargeException ex) {
            
        }
        try {
            
            solver.solve(100, f, 2, 3);
            Assert.fail("Expecting NoBracketingException - no bracketing");
        } catch (NoBracketingException ex) {
            
        }
    }

// org.apache.commons.math.analysis.solvers.MullerSolverTest::testSinFunction
    public void testSinFunction() {
        UnivariateRealFunction f = new SinFunction();
        UnivariateRealSolver solver = new MullerSolver();
        double min, max, expected, result, tolerance;

        min = 3.0; max = 4.0; expected = FastMath.PI;
        tolerance = FastMath.max(solver.getAbsoluteAccuracy(),
                    FastMath.abs(expected * solver.getRelativeAccuracy()));
        result = solver.solve(100, f, min, max);
        Assert.assertEquals(expected, result, tolerance);

        min = -1.0; max = 1.5; expected = 0.0;
        tolerance = FastMath.max(solver.getAbsoluteAccuracy(),
                    FastMath.abs(expected * solver.getRelativeAccuracy()));
        result = solver.solve(100, f, min, max);
        Assert.assertEquals(expected, result, tolerance);
    }

// org.apache.commons.math.analysis.solvers.MullerSolverTest::testQuinticFunction
    public void testQuinticFunction() {
        UnivariateRealFunction f = new QuinticFunction();
        UnivariateRealSolver solver = new MullerSolver();
        double min, max, expected, result, tolerance;

        min = -0.4; max = 0.2; expected = 0.0;
        tolerance = FastMath.max(solver.getAbsoluteAccuracy(),
                    FastMath.abs(expected * solver.getRelativeAccuracy()));
        result = solver.solve(100, f, min, max);
        Assert.assertEquals(expected, result, tolerance);

        min = 0.75; max = 1.5; expected = 1.0;
        tolerance = FastMath.max(solver.getAbsoluteAccuracy(),
                    FastMath.abs(expected * solver.getRelativeAccuracy()));
        result = solver.solve(100, f, min, max);
        Assert.assertEquals(expected, result, tolerance);

        min = -0.9; max = -0.2; expected = -0.5;
        tolerance = FastMath.max(solver.getAbsoluteAccuracy(),
                    FastMath.abs(expected * solver.getRelativeAccuracy()));
        result = solver.solve(100, f, min, max);
        Assert.assertEquals(expected, result, tolerance);
    }

// org.apache.commons.math.analysis.solvers.MullerSolverTest::testExpm1Function
    public void testExpm1Function() {
        UnivariateRealFunction f = new Expm1Function();
        UnivariateRealSolver solver = new MullerSolver();
        double min, max, expected, result, tolerance;

        min = -1.0; max = 2.0; expected = 0.0;
        tolerance = FastMath.max(solver.getAbsoluteAccuracy(),
                    FastMath.abs(expected * solver.getRelativeAccuracy()));
        result = solver.solve(100, f, min, max);
        Assert.assertEquals(expected, result, tolerance);

        min = -20.0; max = 10.0; expected = 0.0;
        tolerance = FastMath.max(solver.getAbsoluteAccuracy(),
                    FastMath.abs(expected * solver.getRelativeAccuracy()));
        result = solver.solve(100, f, min, max);
        Assert.assertEquals(expected, result, tolerance);

        min = -50.0; max = 100.0; expected = 0.0;
        tolerance = FastMath.max(solver.getAbsoluteAccuracy(),
                    FastMath.abs(expected * solver.getRelativeAccuracy()));
        result = solver.solve(100, f, min, max);
        Assert.assertEquals(expected, result, tolerance);
    }

// org.apache.commons.math.analysis.solvers.MullerSolverTest::testParameters
    public void testParameters() throws Exception {
        UnivariateRealFunction f = new SinFunction();
        UnivariateRealSolver solver = new MullerSolver();

        try {
            
            double root = solver.solve(100, f, 1, -1);
            System.out.println("root=" + root);
            Assert.fail("Expecting NumberIsTooLargeException - bad interval");
        } catch (NumberIsTooLargeException ex) {
            
        }
        try {
            
            solver.solve(100, f, 2, 3);
            Assert.fail("Expecting NoBracketingException - no bracketing");
        } catch (NoBracketingException ex) {
            
        }
    }

// org.apache.commons.math.analysis.solvers.NewtonSolverTest::testSinZero
    public void testSinZero() {
        DifferentiableUnivariateRealFunction f = new SinFunction();
        double result;

        NewtonSolver solver = new NewtonSolver();
        result = solver.solve(100, f, 3, 4);
        Assert.assertEquals(result, FastMath.PI, solver.getAbsoluteAccuracy());

        result = solver.solve(100, f, 1, 4);
        Assert.assertEquals(result, FastMath.PI, solver.getAbsoluteAccuracy());

        Assert.assertTrue(solver.getEvaluations() > 0);
    }

// org.apache.commons.math.analysis.solvers.NewtonSolverTest::testQuinticZero
    public void testQuinticZero() {
        DifferentiableUnivariateRealFunction f = new QuinticFunction();
        double result;

        NewtonSolver solver = new NewtonSolver();
        result = solver.solve(100, f, -0.2, 0.2);
        Assert.assertEquals(result, 0, solver.getAbsoluteAccuracy());

        result = solver.solve(100, f, -0.1, 0.3);
        Assert.assertEquals(result, 0, solver.getAbsoluteAccuracy());

        result = solver.solve(100, f, -0.3, 0.45);
        Assert.assertEquals(result, 0, solver.getAbsoluteAccuracy());

        result = solver.solve(100, f, 0.3, 0.7);
        Assert.assertEquals(result, 0.5, solver.getAbsoluteAccuracy());

        result = solver.solve(100, f, 0.2, 0.6);
        Assert.assertEquals(result, 0.5, solver.getAbsoluteAccuracy());

        result = solver.solve(100, f, 0.05, 0.95);
        Assert.assertEquals(result, 0.5, solver.getAbsoluteAccuracy());

        result = solver.solve(100, f, 0.85, 1.25);
        Assert.assertEquals(result, 1.0, solver.getAbsoluteAccuracy());

        result = solver.solve(100, f, 0.8, 1.2);
        Assert.assertEquals(result, 1.0, solver.getAbsoluteAccuracy());

        result = solver.solve(100, f, 0.85, 1.75);
        Assert.assertEquals(result, 1.0, solver.getAbsoluteAccuracy());

        result = solver.solve(100, f, 0.55, 1.45);
        Assert.assertEquals(result, 1.0, solver.getAbsoluteAccuracy());

        result = solver.solve(100, f, 0.85, 5);
        Assert.assertEquals(result, 1.0, solver.getAbsoluteAccuracy());
    }

// org.apache.commons.math.analysis.solvers.RiddersSolverTest::testSinFunction
    public void testSinFunction() {
        UnivariateRealFunction f = new SinFunction();
        UnivariateRealSolver solver = new RiddersSolver();
        double min, max, expected, result, tolerance;

        min = 3.0; max = 4.0; expected = FastMath.PI;
        tolerance = FastMath.max(solver.getAbsoluteAccuracy(),
                    FastMath.abs(expected * solver.getRelativeAccuracy()));
        result = solver.solve(100, f, min, max);
        Assert.assertEquals(expected, result, tolerance);

        min = -1.0; max = 1.5; expected = 0.0;
        tolerance = FastMath.max(solver.getAbsoluteAccuracy(),
                    FastMath.abs(expected * solver.getRelativeAccuracy()));
        result = solver.solve(100, f, min, max);
        Assert.assertEquals(expected, result, tolerance);
    }

// org.apache.commons.math.analysis.solvers.RiddersSolverTest::testQuinticFunction
    public void testQuinticFunction() {
        UnivariateRealFunction f = new QuinticFunction();
        UnivariateRealSolver solver = new RiddersSolver();
        double min, max, expected, result, tolerance;

        min = -0.4; max = 0.2; expected = 0.0;
        tolerance = FastMath.max(solver.getAbsoluteAccuracy(),
                    FastMath.abs(expected * solver.getRelativeAccuracy()));
        result = solver.solve(100, f, min, max);
        Assert.assertEquals(expected, result, tolerance);

        min = 0.75; max = 1.5; expected = 1.0;
        tolerance = FastMath.max(solver.getAbsoluteAccuracy(),
                    FastMath.abs(expected * solver.getRelativeAccuracy()));
        result = solver.solve(100, f, min, max);
        Assert.assertEquals(expected, result, tolerance);

        min = -0.9; max = -0.2; expected = -0.5;
        tolerance = FastMath.max(solver.getAbsoluteAccuracy(),
                    FastMath.abs(expected * solver.getRelativeAccuracy()));
        result = solver.solve(100, f, min, max);
        Assert.assertEquals(expected, result, tolerance);
    }

// org.apache.commons.math.analysis.solvers.RiddersSolverTest::testExpm1Function
    public void testExpm1Function() {
        UnivariateRealFunction f = new Expm1Function();
        UnivariateRealSolver solver = new RiddersSolver();
        double min, max, expected, result, tolerance;

        min = -1.0; max = 2.0; expected = 0.0;
        tolerance = FastMath.max(solver.getAbsoluteAccuracy(),
                    FastMath.abs(expected * solver.getRelativeAccuracy()));
        result = solver.solve(100, f, min, max);
        Assert.assertEquals(expected, result, tolerance);

        min = -20.0; max = 10.0; expected = 0.0;
        tolerance = FastMath.max(solver.getAbsoluteAccuracy(),
                    FastMath.abs(expected * solver.getRelativeAccuracy()));
        result = solver.solve(100, f, min, max);
        Assert.assertEquals(expected, result, tolerance);

        min = -50.0; max = 100.0; expected = 0.0;
        tolerance = FastMath.max(solver.getAbsoluteAccuracy(),
                    FastMath.abs(expected * solver.getRelativeAccuracy()));
        result = solver.solve(100, f, min, max);
        Assert.assertEquals(expected, result, tolerance);
    }

// org.apache.commons.math.analysis.solvers.RiddersSolverTest::testParameters
    public void testParameters() {
        UnivariateRealFunction f = new SinFunction();
        UnivariateRealSolver solver = new RiddersSolver();

        try {
            
            solver.solve(100, f, 1, -1);
            Assert.fail("Expecting NumberIsTooLargeException - bad interval");
        } catch (NumberIsTooLargeException ex) {
            
        }
        try {
            
            solver.solve(100, f, 2, 3);
            Assert.fail("Expecting NoBracketingException - no bracketing");
        } catch (NoBracketingException ex) {
            
        }
    }

// org.apache.commons.math.analysis.solvers.SecantSolverTest::testSinZero
    public void testSinZero() {
        
        
        
        UnivariateRealFunction f = new SinFunction();
        double result;
        UnivariateRealSolver solver = new SecantSolver();

        result = solver.solve(100, f, 3, 4);
        
        
        Assert.assertEquals(result, FastMath.PI, solver.getAbsoluteAccuracy());
        Assert.assertTrue(solver.getEvaluations() <= 6);
        result = solver.solve(100, f, 1, 4);
        
        
        Assert.assertEquals(result, FastMath.PI, solver.getAbsoluteAccuracy());
        Assert.assertTrue(solver.getEvaluations() <= 7);
    }

// org.apache.commons.math.analysis.solvers.SecantSolverTest::testQuinticZero
    public void testQuinticZero() {
        
        
        
        
        
        
        
        UnivariateRealFunction f = new QuinticFunction();
        double result;
        
        UnivariateRealSolver solver = new SecantSolver();
        result = solver.solve(100, f, -0.2, 0.2);
        
        
        Assert.assertEquals(result, 0, solver.getAbsoluteAccuracy());
        Assert.assertTrue(solver.getEvaluations() <= 3);
        result = solver.solve(100, f, -0.1, 0.3);
        
        
        Assert.assertEquals(result, 0, solver.getAbsoluteAccuracy());
        Assert.assertTrue(solver.getEvaluations() <= 7);
        result = solver.solve(100, f, -0.3, 0.45);
        
        
        Assert.assertEquals(result, 0, solver.getAbsoluteAccuracy());
        Assert.assertTrue(solver.getEvaluations() <= 8);
        result = solver.solve(100, f, 0.3, 0.7);
        
        
        Assert.assertEquals(result, 0.5, solver.getAbsoluteAccuracy());
        Assert.assertTrue(solver.getEvaluations() <= 9);
        result = solver.solve(100, f, 0.2, 0.6);
        
        
        Assert.assertEquals(result, 0.5, solver.getAbsoluteAccuracy());
        Assert.assertTrue(solver.getEvaluations() <= 8);
        result = solver.solve(100, f, 0.05, 0.95);
        
        
        Assert.assertEquals(result, 0.5, solver.getAbsoluteAccuracy());
        Assert.assertTrue(solver.getEvaluations() <= 10);
        result = solver.solve(100, f, 0.85, 1.25);
        
        
        Assert.assertEquals(result, 1.0, solver.getAbsoluteAccuracy());
        Assert.assertTrue(solver.getEvaluations() <= 12);
        result = solver.solve(100, f, 0.8, 1.2);
        
        
        Assert.assertEquals(result, 1.0, solver.getAbsoluteAccuracy());
        Assert.assertTrue(solver.getEvaluations() <= 10);
        result = solver.solve(100, f, 0.85, 1.75);
        
        
        Assert.assertEquals(result, 1.0, solver.getAbsoluteAccuracy());
        Assert.assertTrue(solver.getEvaluations() <= 16);
        
        
        result = solver.solve(100, f, 0.55, 1.45);
        
        
        Assert.assertEquals(result, 1.0, solver.getAbsoluteAccuracy());
        Assert.assertTrue(solver.getEvaluations() <= 9);
        result = solver.solve(100, f, 0.85, 5);
        
        
        Assert.assertEquals(result, 1.0, solver.getAbsoluteAccuracy());
        Assert.assertTrue(solver.getEvaluations() <= 16);
    }

// org.apache.commons.math.analysis.solvers.SecantSolverTest::testRootEndpoints
    public void testRootEndpoints() {
        UnivariateRealFunction f = new SinFunction();
        SecantSolver solver = new SecantSolver();

        
        double result = solver.solve(100, f, FastMath.PI, 4);
        Assert.assertEquals(FastMath.PI, result, solver.getAbsoluteAccuracy());

        result = solver.solve(100, f, 3, FastMath.PI);
        Assert.assertEquals(FastMath.PI, result, solver.getAbsoluteAccuracy());

        result = solver.solve(100, f, FastMath.PI, 4, 3.5);
        Assert.assertEquals(FastMath.PI, result, solver.getAbsoluteAccuracy());

        result = solver.solve(100, f, 3, FastMath.PI, 3.07);
        Assert.assertEquals(FastMath.PI, result, solver.getAbsoluteAccuracy());

    }

// org.apache.commons.math.analysis.solvers.SecantSolverTest::testBadEndpoints
    public void testBadEndpoints() {
        UnivariateRealFunction f = new SinFunction();
        SecantSolver solver = new SecantSolver();
        try {  
            solver.solve(100, f, 1, -1);
            Assert.fail("Expecting NumberIsTooLargeException - bad interval");
        } catch (NumberIsTooLargeException ex) {
            
        }
        try {  
            solver.solve(100, f, 1, 1.5);
            Assert.fail("Expecting NoBracketingException - non-bracketing");
        } catch (NoBracketingException ex) {
            
        }
        try {  
            solver.solve(100, f, 1, 1.5, 1.2);
            Assert.fail("Expecting NoBracketingException - non-bracketing");
        } catch (NoBracketingException ex) {
            
        }
    }

// org.apache.commons.math.analysis.solvers.UnivariateRealSolverUtilsTest::testSolveNull
    public void testSolveNull() {
        try {
            UnivariateRealSolverUtils.solve(null, 0.0, 4.0);
            Assert.fail();
        } catch(MathIllegalArgumentException ex){
            
        }
    }

// org.apache.commons.math.analysis.solvers.UnivariateRealSolverUtilsTest::testSolveBadEndpoints
    public void testSolveBadEndpoints() {
        try { 
            double root = UnivariateRealSolverUtils.solve(sin, 4.0, -0.1, 1e-6);
            System.out.println("root=" + root);
            Assert.fail("Expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.analysis.solvers.UnivariateRealSolverUtilsTest::testSolveBadAccuracy
    public void testSolveBadAccuracy() {
        try { 
            UnivariateRealSolverUtils.solve(sin, 0.0, 4.0, 0.0);

        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.analysis.solvers.UnivariateRealSolverUtilsTest::testSolveSin
    public void testSolveSin() {
        double x = UnivariateRealSolverUtils.solve(sin, 1.0, 4.0);
        Assert.assertEquals(FastMath.PI, x, 1.0e-4);
    }

// org.apache.commons.math.analysis.solvers.UnivariateRealSolverUtilsTest::testSolveAccuracyNull
    public void testSolveAccuracyNull()  {
        try {
            double accuracy = 1.0e-6;
            UnivariateRealSolverUtils.solve(null, 0.0, 4.0, accuracy);
            Assert.fail();
        } catch(MathIllegalArgumentException ex){
            
        }
    }

// org.apache.commons.math.analysis.solvers.UnivariateRealSolverUtilsTest::testSolveAccuracySin
    public void testSolveAccuracySin() {
        double accuracy = 1.0e-6;
        double x = UnivariateRealSolverUtils.solve(sin, 1.0,
                4.0, accuracy);
        Assert.assertEquals(FastMath.PI, x, accuracy);
    }

// org.apache.commons.math.analysis.solvers.UnivariateRealSolverUtilsTest::testSolveNoRoot
    public void testSolveNoRoot() {
        try {
            UnivariateRealSolverUtils.solve(sin, 1.0, 1.5);
            Assert.fail("Expecting MathIllegalArgumentException ");
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.analysis.solvers.UnivariateRealSolverUtilsTest::testBracketSin
    public void testBracketSin() {
        double[] result = UnivariateRealSolverUtils.bracket(sin,
                0.0, -2.0, 2.0);
        Assert.assertTrue(sin.value(result[0]) < 0);
        Assert.assertTrue(sin.value(result[1]) > 0);
    }

// org.apache.commons.math.analysis.solvers.UnivariateRealSolverUtilsTest::testBracketEndpointRoot
    public void testBracketEndpointRoot() {
        double[] result = UnivariateRealSolverUtils.bracket(sin, 1.5, 0, 2.0);
        Assert.assertEquals(0.0, sin.value(result[0]), 1.0e-15);
        Assert.assertTrue(sin.value(result[1]) > 0);
    }

// org.apache.commons.math.analysis.solvers.UnivariateRealSolverUtilsTest::testNullFunction
    public void testNullFunction() {
        try { 
            UnivariateRealSolverUtils.bracket(null, 1.5, 0, 2.0);
            Assert.fail("Expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.analysis.solvers.UnivariateRealSolverUtilsTest::testBadInitial
    public void testBadInitial() {
        try { 
            UnivariateRealSolverUtils.bracket(sin, 2.5, 0, 2.0);
            Assert.fail("Expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.analysis.solvers.UnivariateRealSolverUtilsTest::testBadEndpoints
    public void testBadEndpoints() {
        try { 
            UnivariateRealSolverUtils.bracket(sin, 1.5, 2.0, 1.0);
            Assert.fail("Expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.analysis.solvers.UnivariateRealSolverUtilsTest::testBadMaximumIterations
    public void testBadMaximumIterations() {
        try { 
            UnivariateRealSolverUtils.bracket(sin, 1.5, 0, 2.0, 0);
            Assert.fail("Expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.analysis.solvers.UnivariateRealSolverUtilsTest::testMisc
    public void testMisc() {
        UnivariateRealFunction f = new QuinticFunction();
        double result;
        
        result = UnivariateRealSolverUtils.solve(f, -0.2, 0.2);
        Assert.assertEquals(result, 0, 1E-8);
        result = UnivariateRealSolverUtils.solve(f, -0.1, 0.3);
        Assert.assertEquals(result, 0, 1E-8);
        result = UnivariateRealSolverUtils.solve(f, -0.3, 0.45);
        Assert.assertEquals(result, 0, 1E-6);
        result = UnivariateRealSolverUtils.solve(f, 0.3, 0.7);
        Assert.assertEquals(result, 0.5, 1E-6);
        result = UnivariateRealSolverUtils.solve(f, 0.2, 0.6);
        Assert.assertEquals(result, 0.5, 1E-6);
        result = UnivariateRealSolverUtils.solve(f, 0.05, 0.95);
        Assert.assertEquals(result, 0.5, 1E-6);
        result = UnivariateRealSolverUtils.solve(f, 0.85, 1.25);
        Assert.assertEquals(result, 1.0, 1E-6);
        result = UnivariateRealSolverUtils.solve(f, 0.8, 1.2);
        Assert.assertEquals(result, 1.0, 1E-6);
        result = UnivariateRealSolverUtils.solve(f, 0.85, 1.75);
        Assert.assertEquals(result, 1.0, 1E-6);
        result = UnivariateRealSolverUtils.solve(f, 0.55, 1.45);
        Assert.assertEquals(result, 1.0, 1E-6);
        result = UnivariateRealSolverUtils.solve(f, 0.85, 5);
        Assert.assertEquals(result, 1.0, 1E-6);
    }

// org.apache.commons.math.complex.ComplexTest::testConstructor
    public void testConstructor() {
        Complex z = new Complex(3.0, 4.0);
        assertEquals(3.0, z.getReal(), 1.0e-5);
        assertEquals(4.0, z.getImaginary(), 1.0e-5);
    }

// org.apache.commons.math.complex.ComplexTest::testConstructorNaN
    public void testConstructorNaN() {
        Complex z = new Complex(3.0, Double.NaN);
        assertTrue(z.isNaN());

        z = new Complex(nan, 4.0);
        assertTrue(z.isNaN());

        z = new Complex(3.0, 4.0);
        assertFalse(z.isNaN());
    }

// org.apache.commons.math.complex.ComplexTest::testAbs
    public void testAbs() {
        Complex z = new Complex(3.0, 4.0);
        assertEquals(5.0, z.abs(), 1.0e-5);
    }

// org.apache.commons.math.complex.ComplexTest::testAbsNaN
    public void testAbsNaN() {
        assertTrue(Double.isNaN(Complex.NaN.abs()));
        Complex z = new Complex(inf, nan);
        assertTrue(Double.isNaN(z.abs()));
    }

// org.apache.commons.math.complex.ComplexTest::testAbsInfinite
    public void testAbsInfinite() {
        Complex z = new Complex(inf, 0);
        assertEquals(inf, z.abs(), 0);
        z = new Complex(0, neginf);
        assertEquals(inf, z.abs(), 0);
        z = new Complex(inf, neginf);
        assertEquals(inf, z.abs(), 0);
    }

// org.apache.commons.math.complex.ComplexTest::testAdd
    public void testAdd() {
        Complex x = new Complex(3.0, 4.0);
        Complex y = new Complex(5.0, 6.0);
        Complex z = x.add(y);
        assertEquals(8.0, z.getReal(), 1.0e-5);
        assertEquals(10.0, z.getImaginary(), 1.0e-5);
    }

// org.apache.commons.math.complex.ComplexTest::testAddNaN
    public void testAddNaN() {
        Complex x = new Complex(3.0, 4.0);
        Complex z = x.add(Complex.NaN);
        assertTrue(z.isNaN());
        z = new Complex(1, nan);
        Complex w = x.add(z);
        assertEquals(w.getReal(), 4.0, 0);
        assertTrue(Double.isNaN(w.getImaginary()));
    }

// org.apache.commons.math.complex.ComplexTest::testAddInfinite
    public void testAddInfinite() {
        Complex x = new Complex(1, 1);
        Complex z = new Complex(inf, 0);
        Complex w = x.add(z);
        assertEquals(w.getImaginary(), 1, 0);
        assertEquals(inf, w.getReal(), 0);

        x = new Complex(neginf, 0);
        assertTrue(Double.isNaN(x.add(z).getReal()));
    }

// org.apache.commons.math.complex.ComplexTest::testConjugate
    public void testConjugate() {
        Complex x = new Complex(3.0, 4.0);
        Complex z = x.conjugate();
        assertEquals(3.0, z.getReal(), 1.0e-5);
        assertEquals(-4.0, z.getImaginary(), 1.0e-5);
    }

// org.apache.commons.math.complex.ComplexTest::testConjugateNaN
    public void testConjugateNaN() {
        Complex z = Complex.NaN.conjugate();
        assertTrue(z.isNaN());
    }

// org.apache.commons.math.complex.ComplexTest::testConjugateInfiinite
    public void testConjugateInfiinite() {
        Complex z = new Complex(0, inf);
        assertEquals(neginf, z.conjugate().getImaginary(), 0);
        z = new Complex(0, neginf);
        assertEquals(inf, z.conjugate().getImaginary(), 0);
    }

// org.apache.commons.math.complex.ComplexTest::testDivide
    public void testDivide() {
        Complex x = new Complex(3.0, 4.0);
        Complex y = new Complex(5.0, 6.0);
        Complex z = x.divide(y);
        assertEquals(39.0 / 61.0, z.getReal(), 1.0e-5);
        assertEquals(2.0 / 61.0, z.getImaginary(), 1.0e-5);
    }

// org.apache.commons.math.complex.ComplexTest::testDivideReal
    public void testDivideReal() {
        Complex x = new Complex(2d, 3d);
        Complex y = new Complex(2d, 0d);
        assertEquals(new Complex(1d, 1.5), x.divide(y));

    }

// org.apache.commons.math.complex.ComplexTest::testDivideImaginary
    public void testDivideImaginary() {
        Complex x = new Complex(2d, 3d);
        Complex y = new Complex(0d, 2d);
        assertEquals(new Complex(1.5d, -1d), x.divide(y));
    }

// org.apache.commons.math.complex.ComplexTest::testDivideInfinite
    public void testDivideInfinite() {
        Complex x = new Complex(3, 4);
        Complex w = new Complex(neginf, inf);
        assertTrue(x.divide(w).equals(Complex.ZERO));

        Complex z = w.divide(x);
        assertTrue(Double.isNaN(z.getReal()));
        assertEquals(inf, z.getImaginary(), 0);

        w = new Complex(inf, inf);
        z = w.divide(x);
        assertTrue(Double.isNaN(z.getImaginary()));
        assertEquals(inf, z.getReal(), 0);

        w = new Complex(1, inf);
        z = w.divide(w);
        assertTrue(Double.isNaN(z.getReal()));
        assertTrue(Double.isNaN(z.getImaginary()));
    }

// org.apache.commons.math.complex.ComplexTest::testDivideZero
    public void testDivideZero() {
        Complex x = new Complex(3.0, 4.0);
        Complex z = x.divide(Complex.ZERO);
        assertEquals(z, Complex.NaN);
    }

// org.apache.commons.math.complex.ComplexTest::testDivideNaN
    public void testDivideNaN() {
        Complex x = new Complex(3.0, 4.0);
        Complex z = x.divide(Complex.NaN);
        assertTrue(z.isNaN());
    }

// org.apache.commons.math.complex.ComplexTest::testDivideNaNInf
    public void testDivideNaNInf() {
       Complex z = oneInf.divide(Complex.ONE);
       assertTrue(Double.isNaN(z.getReal()));
       assertEquals(inf, z.getImaginary(), 0);

       z = negInfNegInf.divide(oneNaN);
       assertTrue(Double.isNaN(z.getReal()));
       assertTrue(Double.isNaN(z.getImaginary()));

       z = negInfInf.divide(Complex.ONE);
       assertTrue(Double.isNaN(z.getReal()));
       assertTrue(Double.isNaN(z.getImaginary()));
    }

// org.apache.commons.math.complex.ComplexTest::testMultiply
    public void testMultiply() {
        Complex x = new Complex(3.0, 4.0);
        Complex y = new Complex(5.0, 6.0);
        Complex z = x.multiply(y);
        assertEquals(-9.0, z.getReal(), 1.0e-5);
        assertEquals(38.0, z.getImaginary(), 1.0e-5);
    }

// org.apache.commons.math.complex.ComplexTest::testMultiplyNaN
    public void testMultiplyNaN() {
        Complex x = new Complex(3.0, 4.0);
        Complex z = x.multiply(Complex.NaN);
        assertTrue(z.isNaN());
    }

// org.apache.commons.math.complex.ComplexTest::testMultiplyNaNInf
    public void testMultiplyNaNInf() {
        Complex z = new Complex(1,1);
        Complex w = z.multiply(infOne);
        assertEquals(w.getReal(), inf, 0);
        assertEquals(w.getImaginary(), inf, 0);

        
        assertTrue(new Complex( 1,0).multiply(infInf).equals(Complex.INF));
        assertTrue(new Complex(-1,0).multiply(infInf).equals(Complex.INF));
        assertTrue(new Complex( 1,0).multiply(negInfZero).equals(Complex.INF));

        w = oneInf.multiply(oneNegInf);
        assertEquals(w.getReal(), inf, 0);
        assertEquals(w.getImaginary(), inf, 0);

        w = negInfNegInf.multiply(oneNaN);
        assertTrue(Double.isNaN(w.getReal()));
        assertTrue(Double.isNaN(w.getImaginary()));
    }

// org.apache.commons.math.complex.ComplexTest::testScalarMultiply
    public void testScalarMultiply() {
        Complex x = new Complex(3.0, 4.0);
        double y = 2.0;
        Complex z = x.multiply(y);
        assertEquals(6.0, z.getReal(), 1.0e-5);
        assertEquals(8.0, z.getImaginary(), 1.0e-5);
    }

// org.apache.commons.math.complex.ComplexTest::testScalarMultiplyNaN
    public void testScalarMultiplyNaN() {
        Complex x = new Complex(3.0, 4.0);
        Complex z = x.multiply(Double.NaN);
        assertTrue(z.isNaN());
    }

// org.apache.commons.math.complex.ComplexTest::testScalarMultiplyInf
    public void testScalarMultiplyInf() {
        Complex z = new Complex(1,1);
        Complex w = z.multiply(Double.POSITIVE_INFINITY);
        assertEquals(w.getReal(), inf, 0);
        assertEquals(w.getImaginary(), inf, 0);

        w = z.multiply(Double.NEGATIVE_INFINITY);
        assertEquals(w.getReal(), inf, 0);
        assertEquals(w.getImaginary(), inf, 0);
    }

// org.apache.commons.math.complex.ComplexTest::testNegate
    public void testNegate() {
        Complex x = new Complex(3.0, 4.0);
        Complex z = x.negate();
        assertEquals(-3.0, z.getReal(), 1.0e-5);
        assertEquals(-4.0, z.getImaginary(), 1.0e-5);
    }

// org.apache.commons.math.complex.ComplexTest::testNegateNaN
    public void testNegateNaN() {
        Complex z = Complex.NaN.negate();
        assertTrue(z.isNaN());
    }

// org.apache.commons.math.complex.ComplexTest::testSubtract
    public void testSubtract() {
        Complex x = new Complex(3.0, 4.0);
        Complex y = new Complex(5.0, 6.0);
        Complex z = x.subtract(y);
        assertEquals(-2.0, z.getReal(), 1.0e-5);
        assertEquals(-2.0, z.getImaginary(), 1.0e-5);
    }

// org.apache.commons.math.complex.ComplexTest::testSubtractNaN
    public void testSubtractNaN() {
        Complex x = new Complex(3.0, 4.0);
        Complex z = x.subtract(Complex.NaN);
        assertTrue(z.isNaN());
    }

// org.apache.commons.math.complex.ComplexTest::testEqualsNull
    public void testEqualsNull() {
        Complex x = new Complex(3.0, 4.0);
        assertFalse(x.equals(null));
    }

// org.apache.commons.math.complex.ComplexTest::testEqualsClass
    public void testEqualsClass() {
        Complex x = new Complex(3.0, 4.0);
        assertFalse(x.equals(this));
    }

// org.apache.commons.math.complex.ComplexTest::testEqualsSame
    public void testEqualsSame() {
        Complex x = new Complex(3.0, 4.0);
        assertTrue(x.equals(x));
    }

// org.apache.commons.math.complex.ComplexTest::testEqualsTrue
    public void testEqualsTrue() {
        Complex x = new Complex(3.0, 4.0);
        Complex y = new Complex(3.0, 4.0);
        assertTrue(x.equals(y));
    }

// org.apache.commons.math.complex.ComplexTest::testEqualsRealDifference
    public void testEqualsRealDifference() {
        Complex x = new Complex(0.0, 0.0);
        Complex y = new Complex(0.0 + Double.MIN_VALUE, 0.0);
        assertFalse(x.equals(y));
    }

// org.apache.commons.math.complex.ComplexTest::testEqualsImaginaryDifference
    public void testEqualsImaginaryDifference() {
        Complex x = new Complex(0.0, 0.0);
        Complex y = new Complex(0.0, 0.0 + Double.MIN_VALUE);
        assertFalse(x.equals(y));
    }

// org.apache.commons.math.complex.ComplexTest::testEqualsNaN
    public void testEqualsNaN() {
        Complex realNaN = new Complex(Double.NaN, 0.0);
        Complex imaginaryNaN = new Complex(0.0, Double.NaN);
        Complex complexNaN = Complex.NaN;
        assertTrue(realNaN.equals(imaginaryNaN));
        assertTrue(imaginaryNaN.equals(complexNaN));
        assertTrue(realNaN.equals(complexNaN));
    }

// org.apache.commons.math.complex.ComplexTest::testHashCode
    public void testHashCode() {
        Complex x = new Complex(0.0, 0.0);
        Complex y = new Complex(0.0, 0.0 + Double.MIN_VALUE);
        assertFalse(x.hashCode()==y.hashCode());
        y = new Complex(0.0 + Double.MIN_VALUE, 0.0);
        assertFalse(x.hashCode()==y.hashCode());
        Complex realNaN = new Complex(Double.NaN, 0.0);
        Complex imaginaryNaN = new Complex(0.0, Double.NaN);
        assertEquals(realNaN.hashCode(), imaginaryNaN.hashCode());
        assertEquals(imaginaryNaN.hashCode(), Complex.NaN.hashCode());
    }

// org.apache.commons.math.complex.ComplexTest::testAcos
    public void testAcos() {
        Complex z = new Complex(3, 4);
        Complex expected = new Complex(0.936812, -2.30551);
        TestUtils.assertEquals(expected, z.acos(), 1.0e-5);
        TestUtils.assertEquals(new Complex(FastMath.acos(0), 0),
                Complex.ZERO.acos(), 1.0e-12);
    }

// org.apache.commons.math.complex.ComplexTest::testAcosInf
    public void testAcosInf() {
        TestUtils.assertSame(Complex.NaN, oneInf.acos());
        TestUtils.assertSame(Complex.NaN, oneNegInf.acos());
        TestUtils.assertSame(Complex.NaN, infOne.acos());
        TestUtils.assertSame(Complex.NaN, negInfOne.acos());
        TestUtils.assertSame(Complex.NaN, infInf.acos());
        TestUtils.assertSame(Complex.NaN, infNegInf.acos());
        TestUtils.assertSame(Complex.NaN, negInfInf.acos());
        TestUtils.assertSame(Complex.NaN, negInfNegInf.acos());
    }

// org.apache.commons.math.complex.ComplexTest::testAcosNaN
    public void testAcosNaN() {
        assertTrue(Complex.NaN.acos().isNaN());
    }

// org.apache.commons.math.complex.ComplexTest::testAsin
    public void testAsin() {
        Complex z = new Complex(3, 4);
        Complex expected = new Complex(0.633984, 2.30551);
        TestUtils.assertEquals(expected, z.asin(), 1.0e-5);
    }

// org.apache.commons.math.complex.ComplexTest::testAsinNaN
    public void testAsinNaN() {
        assertTrue(Complex.NaN.asin().isNaN());
    }

// org.apache.commons.math.complex.ComplexTest::testAsinInf
    public void testAsinInf() {
        TestUtils.assertSame(Complex.NaN, oneInf.asin());
        TestUtils.assertSame(Complex.NaN, oneNegInf.asin());
        TestUtils.assertSame(Complex.NaN, infOne.asin());
        TestUtils.assertSame(Complex.NaN, negInfOne.asin());
        TestUtils.assertSame(Complex.NaN, infInf.asin());
        TestUtils.assertSame(Complex.NaN, infNegInf.asin());
        TestUtils.assertSame(Complex.NaN, negInfInf.asin());
        TestUtils.assertSame(Complex.NaN, negInfNegInf.asin());
    }

// org.apache.commons.math.complex.ComplexTest::testAtan
    public void testAtan() {
        Complex z = new Complex(3, 4);
        Complex expected = new Complex(1.44831, 0.158997);
        TestUtils.assertEquals(expected, z.atan(), 1.0e-5);
    }

// org.apache.commons.math.complex.ComplexTest::testAtanInf
    public void testAtanInf() {
        TestUtils.assertSame(Complex.NaN, oneInf.atan());
        TestUtils.assertSame(Complex.NaN, oneNegInf.atan());
        TestUtils.assertSame(Complex.NaN, infOne.atan());
        TestUtils.assertSame(Complex.NaN, negInfOne.atan());
        TestUtils.assertSame(Complex.NaN, infInf.atan());
        TestUtils.assertSame(Complex.NaN, infNegInf.atan());
        TestUtils.assertSame(Complex.NaN, negInfInf.atan());
        TestUtils.assertSame(Complex.NaN, negInfNegInf.atan());
    }

// org.apache.commons.math.complex.ComplexTest::testAtanNaN
    public void testAtanNaN() {
        assertTrue(Complex.NaN.atan().isNaN());
        assertTrue(Complex.I.atan().isNaN());
    }

// org.apache.commons.math.complex.ComplexTest::testCos
    public void testCos() {
        Complex z = new Complex(3, 4);
        Complex expected = new Complex(-27.03495, -3.851153);
        TestUtils.assertEquals(expected, z.cos(), 1.0e-5);
    }

// org.apache.commons.math.complex.ComplexTest::testCosNaN
    public void testCosNaN() {
        assertTrue(Complex.NaN.cos().isNaN());
    }

// org.apache.commons.math.complex.ComplexTest::testCosInf
    public void testCosInf() {
        TestUtils.assertSame(infNegInf, oneInf.cos());
        TestUtils.assertSame(infInf, oneNegInf.cos());
        TestUtils.assertSame(Complex.NaN, infOne.cos());
        TestUtils.assertSame(Complex.NaN, negInfOne.cos());
        TestUtils.assertSame(Complex.NaN, infInf.cos());
        TestUtils.assertSame(Complex.NaN, infNegInf.cos());
        TestUtils.assertSame(Complex.NaN, negInfInf.cos());
        TestUtils.assertSame(Complex.NaN, negInfNegInf.cos());
    }

// org.apache.commons.math.complex.ComplexTest::testCosh
    public void testCosh() {
        Complex z = new Complex(3, 4);
        Complex expected = new Complex(-6.58066, -7.58155);
        TestUtils.assertEquals(expected, z.cosh(), 1.0e-5);
    }

// org.apache.commons.math.complex.ComplexTest::testCoshNaN
    public void testCoshNaN() {
        assertTrue(Complex.NaN.cosh().isNaN());
    }

// org.apache.commons.math.complex.ComplexTest::testCoshInf
    public void testCoshInf() {
        TestUtils.assertSame(Complex.NaN, oneInf.cosh());
        TestUtils.assertSame(Complex.NaN, oneNegInf.cosh());
        TestUtils.assertSame(infInf, infOne.cosh());
        TestUtils.assertSame(infNegInf, negInfOne.cosh());
        TestUtils.assertSame(Complex.NaN, infInf.cosh());
        TestUtils.assertSame(Complex.NaN, infNegInf.cosh());
        TestUtils.assertSame(Complex.NaN, negInfInf.cosh());
        TestUtils.assertSame(Complex.NaN, negInfNegInf.cosh());
    }

// org.apache.commons.math.complex.ComplexTest::testExp
    public void testExp() {
        Complex z = new Complex(3, 4);
        Complex expected = new Complex(-13.12878, -15.20078);
        TestUtils.assertEquals(expected, z.exp(), 1.0e-5);
        TestUtils.assertEquals(Complex.ONE,
                Complex.ZERO.exp(), 10e-12);
        Complex iPi = Complex.I.multiply(new Complex(pi,0));
        TestUtils.assertEquals(Complex.ONE.negate(),
                iPi.exp(), 10e-12);
    }

// org.apache.commons.math.complex.ComplexTest::testExpNaN
    public void testExpNaN() {
        assertTrue(Complex.NaN.exp().isNaN());
    }

// org.apache.commons.math.complex.ComplexTest::testExpInf
    public void testExpInf() {
        TestUtils.assertSame(Complex.NaN, oneInf.exp());
        TestUtils.assertSame(Complex.NaN, oneNegInf.exp());
        TestUtils.assertSame(infInf, infOne.exp());
        TestUtils.assertSame(Complex.ZERO, negInfOne.exp());
        TestUtils.assertSame(Complex.NaN, infInf.exp());
        TestUtils.assertSame(Complex.NaN, infNegInf.exp());
        TestUtils.assertSame(Complex.NaN, negInfInf.exp());
        TestUtils.assertSame(Complex.NaN, negInfNegInf.exp());
    }

// org.apache.commons.math.complex.ComplexTest::testLog
    public void testLog() {
        Complex z = new Complex(3, 4);
        Complex expected = new Complex(1.60944, 0.927295);
        TestUtils.assertEquals(expected, z.log(), 1.0e-5);
    }

// org.apache.commons.math.complex.ComplexTest::testLogNaN
    public void testLogNaN() {
        assertTrue(Complex.NaN.log().isNaN());
    }

// org.apache.commons.math.complex.ComplexTest::testLogInf
    public void testLogInf() {
        TestUtils.assertEquals(new Complex(inf, pi / 2),
                oneInf.log(), 10e-12);
        TestUtils.assertEquals(new Complex(inf, -pi / 2),
                oneNegInf.log(), 10e-12);
        TestUtils.assertEquals(infZero, infOne.log(), 10e-12);
        TestUtils.assertEquals(new Complex(inf, pi),
                negInfOne.log(), 10e-12);
        TestUtils.assertEquals(new Complex(inf, pi / 4),
                infInf.log(), 10e-12);
        TestUtils.assertEquals(new Complex(inf, -pi / 4),
                infNegInf.log(), 10e-12);
        TestUtils.assertEquals(new Complex(inf, 3d * pi / 4),
                negInfInf.log(), 10e-12);
        TestUtils.assertEquals(new Complex(inf, - 3d * pi / 4),
                negInfNegInf.log(), 10e-12);
    }

// org.apache.commons.math.complex.ComplexTest::testLogZero
    public void testLogZero() {
        TestUtils.assertSame(negInfZero, Complex.ZERO.log());
    }

// org.apache.commons.math.complex.ComplexTest::testPow
    public void testPow() {
        Complex x = new Complex(3, 4);
        Complex y = new Complex(5, 6);
        Complex expected = new Complex(-1.860893, 11.83677);
        TestUtils.assertEquals(expected, x.pow(y), 1.0e-5);
    }

// org.apache.commons.math.complex.ComplexTest::testPowNaNBase
    public void testPowNaNBase() {
        Complex x = new Complex(3, 4);
        assertTrue(Complex.NaN.pow(x).isNaN());
    }

// org.apache.commons.math.complex.ComplexTest::testPowNaNExponent
    public void testPowNaNExponent() {
        Complex x = new Complex(3, 4);
        assertTrue(x.pow(Complex.NaN).isNaN());
    }

// org.apache.commons.math.complex.ComplexTest::testPowInf
   public void testPowInf() {
       TestUtils.assertSame(Complex.NaN,Complex.ONE.pow(oneInf));
       TestUtils.assertSame(Complex.NaN,Complex.ONE.pow(oneNegInf));
       TestUtils.assertSame(Complex.NaN,Complex.ONE.pow(infOne));
       TestUtils.assertSame(Complex.NaN,Complex.ONE.pow(infInf));
       TestUtils.assertSame(Complex.NaN,Complex.ONE.pow(infNegInf));
       TestUtils.assertSame(Complex.NaN,Complex.ONE.pow(negInfInf));
       TestUtils.assertSame(Complex.NaN,Complex.ONE.pow(negInfNegInf));
       TestUtils.assertSame(Complex.NaN,infOne.pow(Complex.ONE));
       TestUtils.assertSame(Complex.NaN,negInfOne.pow(Complex.ONE));
       TestUtils.assertSame(Complex.NaN,infInf.pow(Complex.ONE));
       TestUtils.assertSame(Complex.NaN,infNegInf.pow(Complex.ONE));
       TestUtils.assertSame(Complex.NaN,negInfInf.pow(Complex.ONE));
       TestUtils.assertSame(Complex.NaN,negInfNegInf.pow(Complex.ONE));
       TestUtils.assertSame(Complex.NaN,negInfNegInf.pow(infNegInf));
       TestUtils.assertSame(Complex.NaN,negInfNegInf.pow(negInfNegInf));
       TestUtils.assertSame(Complex.NaN,negInfNegInf.pow(infInf));
       TestUtils.assertSame(Complex.NaN,infInf.pow(infNegInf));
       TestUtils.assertSame(Complex.NaN,infInf.pow(negInfNegInf));
       TestUtils.assertSame(Complex.NaN,infInf.pow(infInf));
       TestUtils.assertSame(Complex.NaN,infNegInf.pow(infNegInf));
       TestUtils.assertSame(Complex.NaN,infNegInf.pow(negInfNegInf));
       TestUtils.assertSame(Complex.NaN,infNegInf.pow(infInf));
   }

// org.apache.commons.math.complex.ComplexTest::testPowZero
   public void testPowZero() {
       TestUtils.assertSame(Complex.NaN,
               Complex.ZERO.pow(Complex.ONE));
       TestUtils.assertSame(Complex.NaN,
               Complex.ZERO.pow(Complex.ZERO));
       TestUtils.assertSame(Complex.NaN,
               Complex.ZERO.pow(Complex.I));
       TestUtils.assertEquals(Complex.ONE,
               Complex.ONE.pow(Complex.ZERO), 10e-12);
       TestUtils.assertEquals(Complex.ONE,
               Complex.I.pow(Complex.ZERO), 10e-12);
       TestUtils.assertEquals(Complex.ONE,
               new Complex(-1, 3).pow(Complex.ZERO), 10e-12);
   }

// org.apache.commons.math.complex.ComplexTest::testpowNull
    public void testpowNull() {
        try {
            Complex.ONE.pow(null);
            fail("Expecting NullPointerException");
        } catch (NullPointerException ex) {
            
        }
    }

// org.apache.commons.math.complex.ComplexTest::testSin
    public void testSin() {
        Complex z = new Complex(3, 4);
        Complex expected = new Complex(3.853738, -27.01681);
        TestUtils.assertEquals(expected, z.sin(), 1.0e-5);
    }

// org.apache.commons.math.complex.ComplexTest::testSinInf
    public void testSinInf() {
        TestUtils.assertSame(infInf, oneInf.sin());
        TestUtils.assertSame(infNegInf, oneNegInf.sin());
        TestUtils.assertSame(Complex.NaN, infOne.sin());
        TestUtils.assertSame(Complex.NaN, negInfOne.sin());
        TestUtils.assertSame(Complex.NaN, infInf.sin());
        TestUtils.assertSame(Complex.NaN, infNegInf.sin());
        TestUtils.assertSame(Complex.NaN, negInfInf.sin());
        TestUtils.assertSame(Complex.NaN, negInfNegInf.sin());
    }

// org.apache.commons.math.complex.ComplexTest::testSinNaN
    public void testSinNaN() {
        assertTrue(Complex.NaN.sin().isNaN());
    }

// org.apache.commons.math.complex.ComplexTest::testSinh
    public void testSinh() {
        Complex z = new Complex(3, 4);
        Complex expected = new Complex(-6.54812, -7.61923);
        TestUtils.assertEquals(expected, z.sinh(), 1.0e-5);
    }

// org.apache.commons.math.complex.ComplexTest::testSinhNaN
    public void testSinhNaN() {
        assertTrue(Complex.NaN.sinh().isNaN());
    }

// org.apache.commons.math.complex.ComplexTest::testSinhInf
    public void testSinhInf() {
        TestUtils.assertSame(Complex.NaN, oneInf.sinh());
        TestUtils.assertSame(Complex.NaN, oneNegInf.sinh());
        TestUtils.assertSame(infInf, infOne.sinh());
        TestUtils.assertSame(negInfInf, negInfOne.sinh());
        TestUtils.assertSame(Complex.NaN, infInf.sinh());
        TestUtils.assertSame(Complex.NaN, infNegInf.sinh());
        TestUtils.assertSame(Complex.NaN, negInfInf.sinh());
        TestUtils.assertSame(Complex.NaN, negInfNegInf.sinh());
    }

// org.apache.commons.math.complex.ComplexTest::testSqrtRealPositive
    public void testSqrtRealPositive() {
        Complex z = new Complex(3, 4);
        Complex expected = new Complex(2, 1);
        TestUtils.assertEquals(expected, z.sqrt(), 1.0e-5);
    }

// org.apache.commons.math.complex.ComplexTest::testSqrtRealZero
    public void testSqrtRealZero() {
        Complex z = new Complex(0.0, 4);
        Complex expected = new Complex(1.41421, 1.41421);
        TestUtils.assertEquals(expected, z.sqrt(), 1.0e-5);
    }

// org.apache.commons.math.complex.ComplexTest::testSqrtRealNegative
    public void testSqrtRealNegative() {
        Complex z = new Complex(-3.0, 4);
        Complex expected = new Complex(1, 2);
        TestUtils.assertEquals(expected, z.sqrt(), 1.0e-5);
    }

// org.apache.commons.math.complex.ComplexTest::testSqrtImaginaryZero
    public void testSqrtImaginaryZero() {
        Complex z = new Complex(-3.0, 0.0);
        Complex expected = new Complex(0.0, 1.73205);
        TestUtils.assertEquals(expected, z.sqrt(), 1.0e-5);
    }

// org.apache.commons.math.complex.ComplexTest::testSqrtImaginaryNegative
    public void testSqrtImaginaryNegative() {
        Complex z = new Complex(-3.0, -4.0);
        Complex expected = new Complex(1.0, -2.0);
        TestUtils.assertEquals(expected, z.sqrt(), 1.0e-5);
    }

// org.apache.commons.math.complex.ComplexTest::testSqrtPolar
    public void testSqrtPolar() {
        double r = 1;
        for (int i = 0; i < 5; i++) {
            r += i;
            double theta = 0;
            for (int j =0; j < 11; j++) {
                theta += pi /12;
                Complex z = ComplexUtils.polar2Complex(r, theta);
                Complex sqrtz = ComplexUtils.polar2Complex(FastMath.sqrt(r), theta / 2);
                TestUtils.assertEquals(sqrtz, z.sqrt(), 10e-12);
            }
        }
    }

// org.apache.commons.math.complex.ComplexTest::testSqrtNaN
    public void testSqrtNaN() {
        assertTrue(Complex.NaN.sqrt().isNaN());
    }

// org.apache.commons.math.complex.ComplexTest::testSqrtInf
    public void testSqrtInf() {
        TestUtils.assertSame(infNaN, oneInf.sqrt());
        TestUtils.assertSame(infNaN, oneNegInf.sqrt());
        TestUtils.assertSame(infZero, infOne.sqrt());
        TestUtils.assertSame(zeroInf, negInfOne.sqrt());
        TestUtils.assertSame(infNaN, infInf.sqrt());
        TestUtils.assertSame(infNaN, infNegInf.sqrt());
        TestUtils.assertSame(nanInf, negInfInf.sqrt());
        TestUtils.assertSame(nanNegInf, negInfNegInf.sqrt());
    }

// org.apache.commons.math.complex.ComplexTest::testSqrt1z
    public void testSqrt1z() {
        Complex z = new Complex(3, 4);
        Complex expected = new Complex(4.08033, -2.94094);
        TestUtils.assertEquals(expected, z.sqrt1z(), 1.0e-5);
    }

// org.apache.commons.math.complex.ComplexTest::testSqrt1zNaN
    public void testSqrt1zNaN() {
        assertTrue(Complex.NaN.sqrt1z().isNaN());
    }

// org.apache.commons.math.complex.ComplexTest::testTan
    public void testTan() {
        Complex z = new Complex(3, 4);
        Complex expected = new Complex(-0.000187346, 0.999356);
        TestUtils.assertEquals(expected, z.tan(), 1.0e-5);
    }

// org.apache.commons.math.complex.ComplexTest::testTanNaN
    public void testTanNaN() {
        assertTrue(Complex.NaN.tan().isNaN());
    }

// org.apache.commons.math.complex.ComplexTest::testTanInf
    public void testTanInf() {
        TestUtils.assertSame(zeroNaN, oneInf.tan());
        TestUtils.assertSame(zeroNaN, oneNegInf.tan());
        TestUtils.assertSame(Complex.NaN, infOne.tan());
        TestUtils.assertSame(Complex.NaN, negInfOne.tan());
        TestUtils.assertSame(Complex.NaN, infInf.tan());
        TestUtils.assertSame(Complex.NaN, infNegInf.tan());
        TestUtils.assertSame(Complex.NaN, negInfInf.tan());
        TestUtils.assertSame(Complex.NaN, negInfNegInf.tan());
    }

// org.apache.commons.math.complex.ComplexTest::testTanCritical
   public void testTanCritical() {
        TestUtils.assertSame(infNaN, new Complex(pi/2, 0).tan());
        TestUtils.assertSame(negInfNaN, new Complex(-pi/2, 0).tan());
    }

// org.apache.commons.math.complex.ComplexTest::testTanh
    public void testTanh() {
        Complex z = new Complex(3, 4);
        Complex expected = new Complex(1.00071, 0.00490826);
        TestUtils.assertEquals(expected, z.tanh(), 1.0e-5);
    }

// org.apache.commons.math.complex.ComplexTest::testTanhNaN
    public void testTanhNaN() {
        assertTrue(Complex.NaN.tanh().isNaN());
    }

// org.apache.commons.math.complex.ComplexTest::testTanhInf
    public void testTanhInf() {
        TestUtils.assertSame(Complex.NaN, oneInf.tanh());
        TestUtils.assertSame(Complex.NaN, oneNegInf.tanh());
        TestUtils.assertSame(nanZero, infOne.tanh());
        TestUtils.assertSame(nanZero, negInfOne.tanh());
        TestUtils.assertSame(Complex.NaN, infInf.tanh());
        TestUtils.assertSame(Complex.NaN, infNegInf.tanh());
        TestUtils.assertSame(Complex.NaN, negInfInf.tanh());
        TestUtils.assertSame(Complex.NaN, negInfNegInf.tanh());
    }

// org.apache.commons.math.complex.ComplexTest::testTanhCritical
    public void testTanhCritical() {
        TestUtils.assertSame(nanInf, new Complex(0, pi/2).tanh());
    }

// org.apache.commons.math.complex.ComplexTest::testMath221
    public void testMath221() {
        assertEquals(new Complex(0,-1), new Complex(0,1).multiply(new Complex(-1,0)));
    }

// org.apache.commons.math.complex.ComplexTest::testNthRoot_normal_thirdRoot
    public void testNthRoot_normal_thirdRoot() {
        
        Complex z = new Complex(-2,2);
        
        Complex[] thirdRootsOfZ = z.nthRoot(3).toArray(new Complex[0]);
        
        assertEquals(3, thirdRootsOfZ.length);
        
        assertEquals(1.0,                  thirdRootsOfZ[0].getReal(),      1.0e-5);
        assertEquals(1.0,                  thirdRootsOfZ[0].getImaginary(), 1.0e-5);
        
        assertEquals(-1.3660254037844386,  thirdRootsOfZ[1].getReal(),      1.0e-5);
        assertEquals(0.36602540378443843,  thirdRootsOfZ[1].getImaginary(), 1.0e-5);
        
        assertEquals(0.366025403784439,    thirdRootsOfZ[2].getReal(),      1.0e-5);
        assertEquals(-1.3660254037844384,  thirdRootsOfZ[2].getImaginary(), 1.0e-5);
    }

// org.apache.commons.math.complex.ComplexTest::testNthRoot_normal_fourthRoot
    public void testNthRoot_normal_fourthRoot() {
        
        Complex z = new Complex(5,-2);
        
        Complex[] fourthRootsOfZ = z.nthRoot(4).toArray(new Complex[0]);
        
        assertEquals(4, fourthRootsOfZ.length);
        
        assertEquals(1.5164629308487783,     fourthRootsOfZ[0].getReal(),      1.0e-5);
        assertEquals(-0.14469266210702247,   fourthRootsOfZ[0].getImaginary(), 1.0e-5);
        
        assertEquals(0.14469266210702256,    fourthRootsOfZ[1].getReal(),      1.0e-5);
        assertEquals(1.5164629308487783,     fourthRootsOfZ[1].getImaginary(), 1.0e-5);
        
        assertEquals(-1.5164629308487783,    fourthRootsOfZ[2].getReal(),      1.0e-5);
        assertEquals(0.14469266210702267,    fourthRootsOfZ[2].getImaginary(), 1.0e-5);
        
        assertEquals(-0.14469266210702275,   fourthRootsOfZ[3].getReal(),      1.0e-5);
        assertEquals(-1.5164629308487783,    fourthRootsOfZ[3].getImaginary(), 1.0e-5);
    }

// org.apache.commons.math.complex.ComplexTest::testNthRoot_cornercase_thirdRoot_imaginaryPartEmpty
    public void testNthRoot_cornercase_thirdRoot_imaginaryPartEmpty() {
        
        
        Complex z = new Complex(8,0);
        
        Complex[] thirdRootsOfZ = z.nthRoot(3).toArray(new Complex[0]);
        
        assertEquals(3, thirdRootsOfZ.length);
        
        assertEquals(2.0,                thirdRootsOfZ[0].getReal(),      1.0e-5);
        assertEquals(0.0,                thirdRootsOfZ[0].getImaginary(), 1.0e-5);
        
        assertEquals(-1.0,               thirdRootsOfZ[1].getReal(),      1.0e-5);
        assertEquals(1.7320508075688774, thirdRootsOfZ[1].getImaginary(), 1.0e-5);
        
        assertEquals(-1.0,               thirdRootsOfZ[2].getReal(),      1.0e-5);
        assertEquals(-1.732050807568877, thirdRootsOfZ[2].getImaginary(), 1.0e-5);
    }

// org.apache.commons.math.complex.ComplexTest::testNthRoot_cornercase_thirdRoot_realPartZero
    public void testNthRoot_cornercase_thirdRoot_realPartZero() {
        
        Complex z = new Complex(0,2);
        
        Complex[] thirdRootsOfZ = z.nthRoot(3).toArray(new Complex[0]);
        
        assertEquals(3, thirdRootsOfZ.length);
        
        assertEquals(1.0911236359717216,      thirdRootsOfZ[0].getReal(),      1.0e-5);
        assertEquals(0.6299605249474365,      thirdRootsOfZ[0].getImaginary(), 1.0e-5);
        
        assertEquals(-1.0911236359717216,     thirdRootsOfZ[1].getReal(),      1.0e-5);
        assertEquals(0.6299605249474365,      thirdRootsOfZ[1].getImaginary(), 1.0e-5);
        
        assertEquals(-2.3144374213981936E-16, thirdRootsOfZ[2].getReal(),      1.0e-5);
        assertEquals(-1.2599210498948732,     thirdRootsOfZ[2].getImaginary(), 1.0e-5);
    }

// org.apache.commons.math.complex.ComplexTest::testNthRoot_cornercase_NAN_Inf
    public void testNthRoot_cornercase_NAN_Inf() {
        
        List<Complex> roots = oneNaN.nthRoot(3);
        assertEquals(1,roots.size());
        assertEquals(Complex.NaN, roots.get(0));

        roots = nanZero.nthRoot(3);
        assertEquals(1,roots.size());
        assertEquals(Complex.NaN, roots.get(0));

        
        roots = nanInf.nthRoot(3);
        assertEquals(1,roots.size());
        assertEquals(Complex.NaN, roots.get(0));

        
        roots = oneInf.nthRoot(3);
        assertEquals(1,roots.size());
        assertEquals(Complex.INF, roots.get(0));

        
        roots = negInfInf.nthRoot(3);
        assertEquals(1,roots.size());
        assertEquals(Complex.INF, roots.get(0));
    }

// org.apache.commons.math.complex.ComplexTest::testGetArgument
    public void testGetArgument() {
        Complex z = new Complex(1, 0);
        assertEquals(0.0, z.getArgument(), 1.0e-12);

        z = new Complex(1, 1);
        assertEquals(FastMath.PI/4, z.getArgument(), 1.0e-12);

        z = new Complex(0, 1);
        assertEquals(FastMath.PI/2, z.getArgument(), 1.0e-12);

        z = new Complex(-1, 1);
        assertEquals(3 * FastMath.PI/4, z.getArgument(), 1.0e-12);

        z = new Complex(-1, 0);
        assertEquals(FastMath.PI, z.getArgument(), 1.0e-12);

        z = new Complex(-1, -1);
        assertEquals(-3 * FastMath.PI/4, z.getArgument(), 1.0e-12);

        z = new Complex(0, -1);
        assertEquals(-FastMath.PI/2, z.getArgument(), 1.0e-12);

        z = new Complex(1, -1);
        assertEquals(-FastMath.PI/4, z.getArgument(), 1.0e-12);

    }

// org.apache.commons.math.complex.ComplexTest::testGetArgumentInf
    public void testGetArgumentInf() {
        assertEquals(FastMath.PI/4, infInf.getArgument(), 1.0e-12);
        assertEquals(FastMath.PI/2, oneInf.getArgument(), 1.0e-12);
        assertEquals(0.0, infOne.getArgument(), 1.0e-12);
        assertEquals(FastMath.PI/2, zeroInf.getArgument(), 1.0e-12);
        assertEquals(0.0, infZero.getArgument(), 1.0e-12);
        assertEquals(FastMath.PI, negInfOne.getArgument(), 1.0e-12);
        assertEquals(-3.0*FastMath.PI/4, negInfNegInf.getArgument(), 1.0e-12);
        assertEquals(-FastMath.PI/2, oneNegInf.getArgument(), 1.0e-12);
    }

// org.apache.commons.math.complex.ComplexTest::testGetArgumentNaN
    public void testGetArgumentNaN() {
        assertEquals(nan, nanZero.getArgument());
        assertEquals(nan, zeroNaN.getArgument());
        assertEquals(nan, Complex.NaN.getArgument());
    }

// org.apache.commons.math.complex.ComplexTest::testSerial
    public void testSerial() {
        Complex z = new Complex(3.0, 4.0);
        assertEquals(z, TestUtils.serializeAndRecover(z));
        Complex ncmplx = (Complex)TestUtils.serializeAndRecover(oneNaN);
        assertEquals(nanZero, ncmplx);
        assertTrue(ncmplx.isNaN());
        Complex infcmplx = (Complex)TestUtils.serializeAndRecover(infInf);
        assertEquals(infInf, infcmplx);
        assertTrue(infcmplx.isInfinite());
        TestComplex tz = new TestComplex(3.0, 4.0);
        assertEquals(tz, TestUtils.serializeAndRecover(tz));
        TestComplex ntcmplx = (TestComplex)TestUtils.serializeAndRecover(new TestComplex(oneNaN));
        assertEquals(nanZero, ntcmplx);
        assertTrue(ntcmplx.isNaN());
        TestComplex inftcmplx = (TestComplex)TestUtils.serializeAndRecover(new TestComplex(infInf));
        assertEquals(infInf, inftcmplx);
        assertTrue(inftcmplx.isInfinite());
    }

// org.apache.commons.math.complex.ComplexUtilsTest::testPolar2Complex
    public void testPolar2Complex() {
        TestUtils.assertEquals(Complex.ONE,
                ComplexUtils.polar2Complex(1, 0), 10e-12);
        TestUtils.assertEquals(Complex.ZERO,
                ComplexUtils.polar2Complex(0, 1), 10e-12);
        TestUtils.assertEquals(Complex.ZERO,
                ComplexUtils.polar2Complex(0, -1), 10e-12);
        TestUtils.assertEquals(Complex.I,
                ComplexUtils.polar2Complex(1, pi/2), 10e-12);
        TestUtils.assertEquals(Complex.I.negate(),
                ComplexUtils.polar2Complex(1, -pi/2), 10e-12);
        double r = 0;
        for (int i = 0; i < 5; i++) {
          r += i;
          double theta = 0;
          for (int j =0; j < 20; j++) {
              theta += pi / 6;
              TestUtils.assertEquals(altPolar(r, theta),
                      ComplexUtils.polar2Complex(r, theta), 10e-12);
          }
          theta = -2 * pi;
          for (int j =0; j < 20; j++) {
              theta -= pi / 6;
              TestUtils.assertEquals(altPolar(r, theta),
                      ComplexUtils.polar2Complex(r, theta), 10e-12);
          }
        }
    }

// org.apache.commons.math.complex.ComplexUtilsTest::testPolar2ComplexIllegalModulus
    public void testPolar2ComplexIllegalModulus() {
        try {
            ComplexUtils.polar2Complex(-1, 0);
            fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.complex.ComplexUtilsTest::testPolar2ComplexNaN
    public void testPolar2ComplexNaN() {
        TestUtils.assertSame(Complex.NaN, ComplexUtils.polar2Complex(nan, 1));
        TestUtils.assertSame(Complex.NaN, ComplexUtils.polar2Complex(1, nan));
        TestUtils.assertSame(Complex.NaN,
                ComplexUtils.polar2Complex(nan, nan));
    }

// org.apache.commons.math.complex.ComplexUtilsTest::testPolar2ComplexInf
    public void testPolar2ComplexInf() {
        TestUtils.assertSame(Complex.NaN, ComplexUtils.polar2Complex(1, inf));
        TestUtils.assertSame(Complex.NaN,
                ComplexUtils.polar2Complex(1, negInf));
        TestUtils.assertSame(Complex.NaN, ComplexUtils.polar2Complex(inf, inf));
        TestUtils.assertSame(Complex.NaN,
                ComplexUtils.polar2Complex(inf, negInf));
        TestUtils.assertSame(infInf, ComplexUtils.polar2Complex(inf, pi/4));
        TestUtils.assertSame(infNaN, ComplexUtils.polar2Complex(inf, 0));
        TestUtils.assertSame(infNegInf, ComplexUtils.polar2Complex(inf, -pi/4));
        TestUtils.assertSame(negInfInf, ComplexUtils.polar2Complex(inf, 3*pi/4));
        TestUtils.assertSame(negInfNegInf, ComplexUtils.polar2Complex(inf, 5*pi/4));
    }

// org.apache.commons.math.distribution.BetaDistributionTest::testCumulative
    public void testCumulative() throws MathException {
        double[] x = new double[]{-0.1, 0.0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0, 1.1};
        
        checkCumulative(0.1, 0.1,
                x, new double[]{
                0.0000000000, 0.0000000000, 0.4063850939, 0.4397091902, 0.4628041861,
                0.4821200456, 0.5000000000, 0.5178799544, 0.5371958139, 0.5602908098,
                0.5936149061, 1.0000000000, 1.0000000000});
        checkCumulative(0.1, 0.5,
                x, new double[]{
                0.0000000000, 0.0000000000, 0.7048336221, 0.7593042194, 0.7951765304,
                0.8234948385, 0.8480017124, 0.8706034370, 0.8926585878, 0.9156406404,
                0.9423662883, 1.0000000000, 1.0000000000});
        checkCumulative(0.1, 1.0,
                x, new double[]{
                0.0000000000, 0.0000000000, 0.7943282347, 0.8513399225, 0.8865681506,
                0.9124435366, 0.9330329915, 0.9502002165, 0.9649610951, 0.9779327685,
                0.9895192582, 1.0000000000, 1.0000000000});
        checkCumulative(0.1, 2.0,
                x, new double[]{
                0.0000000000, 0.0000000000, 0.8658177758, 0.9194471163, 0.9486279211,
                0.9671901487, 0.9796846411, 0.9882082252, 0.9939099280, 0.9974914239,
                0.9994144508, 1.0000000000, 1.0000000000});
        checkCumulative(0.1, 4.0,
                x, new double[]{
                0.0000000000, 0.0000000000, 0.9234991121, 0.9661958941, 0.9842285085,
                0.9928444112, 0.9970040660, 0.9989112804, 0.9996895625, 0.9999440793,
                0.9999967829, 1.0000000000, 1.0000000000});
        checkCumulative(0.5, 0.1,
                x, new double[]{
                0.00000000000, 0.00000000000, 0.05763371168, 0.08435935962,
                0.10734141216, 0.12939656302, 0.15199828760, 0.17650516146,
                0.20482346963, 0.24069578055, 0.29516637795, 1.00000000000, 1.00000000000});

        checkCumulative(0.5, 0.5,
                x, new double[]{
                0.0000000000, 0.0000000000, 0.2048327647, 0.2951672353, 0.3690101196,
                0.4359057832, 0.5000000000, 0.5640942168, 0.6309898804, 0.7048327647,
                0.7951672353, 1.0000000000, 1.0000000000});
        checkCumulative(0.5, 1.0,
                x, new double[]{
                0.0000000000, 0.0000000000, 0.3162277660, 0.4472135955, 0.5477225575,
                0.6324555320, 0.7071067812, 0.7745966692, 0.8366600265, 0.8944271910,
                0.9486832981, 1.0000000000, 1.0000000000});
        checkCumulative(0.5, 2.0,
                x, new double[]{
                0.0000000000, 0.0000000000, 0.4585302607, 0.6260990337, 0.7394254526,
                0.8221921916, 0.8838834765, 0.9295160031, 0.9621590305, 0.9838699101,
                0.9961174630, 1.0000000000, 1.0000000000});
        checkCumulative(0.5, 4.0,
                x, new double[]{
                0.0000000000, 0.0000000000, 0.6266250826, 0.8049844719, 0.8987784842,
                0.9502644369, 0.9777960959, 0.9914837366, 0.9974556254, 0.9995223859,
                0.9999714889, 1.0000000000, 1.0000000000});
        checkCumulative(1.0, 0.1,
                x, new double[]{
                0.00000000000, 0.00000000000, 0.01048074179, 0.02206723146,
                0.03503890488, 0.04979978349, 0.06696700846, 0.08755646344,
                0.11343184943, 0.14866007748, 0.20567176528, 1.00000000000, 1.00000000000});
        checkCumulative(1.0, 0.5,
                x, new double[]{
                0.00000000000, 0.00000000000, 0.05131670195, 0.10557280900,
                0.16333997347, 0.22540333076, 0.29289321881, 0.36754446797,
                0.45227744249, 0.55278640450, 0.68377223398, 1.00000000000, 1.00000000000});
        checkCumulative(1, 1,
                x, new double[]{
                0.0, 0.0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0, 1.0});
        checkCumulative(1, 2,
                x, new double[]{
                0.00, 0.00, 0.19, 0.36, 0.51, 0.64, 0.75, 0.84, 0.91, 0.96, 0.99, 1.00, 1.00});
        checkCumulative(1, 4,
                x, new double[]{
                0.0000, 0.0000, 0.3439, 0.5904, 0.7599, 0.8704, 0.9375, 0.9744, 0.9919,
                0.9984, 0.9999, 1.0000, 1.0000});
        checkCumulative(2.0, 0.1,
                x, new double[]{
                0.0000000000000, 0.0000000000000, 0.0005855492117, 0.0025085760862,
                0.0060900720266, 0.0117917748341, 0.0203153588864, 0.0328098512512,
                0.0513720788952, 0.0805528836776, 0.1341822241505, 1.0000000000000, 1.0000000000000});
        checkCumulative(2, 1,
                x, new double[]{
                0.00, 0.00, 0.01, 0.04, 0.09, 0.16, 0.25, 0.36, 0.49, 0.64, 0.81, 1.00, 1.00});
        checkCumulative(2.0, 0.5,
                x, new double[]{
                0.000000000000, 0.000000000000, 0.003882537047, 0.016130089900,
                0.037840969486, 0.070483996910, 0.116116523517, 0.177807808356,
                0.260574547368, 0.373900966300, 0.541469739276, 1.000000000000, 1.000000000000});
        checkCumulative(2, 2,
                x, new double[]{
                0.000, 0.000, 0.028, 0.104, 0.216, 0.352, 0.500, 0.648, 0.784, 0.896, 0.972, 1.000, 1.000});
        checkCumulative(2, 4,
                x, new double[]{
                0.00000, 0.00000, 0.08146, 0.26272, 0.47178, 0.66304, 0.81250, 0.91296,
                0.96922, 0.99328, 0.99954, 1.00000, 1.00000});
        checkCumulative(4.0, 0.1,
                x, new double[]{
                0.000000000e+00, 0.000000000e+00, 3.217128269e-06, 5.592070271e-05,
                3.104375474e-04, 1.088719595e-03, 2.995933981e-03, 7.155588777e-03,
                1.577149153e-02, 3.380410585e-02, 7.650088789e-02, 1.000000000e+00, 1.000000000e+00});
        checkCumulative(4.0, 0.5,
                x, new double[]{
                0.000000000e+00, 0.000000000e+00, 2.851114863e-05, 4.776140576e-04,
                2.544374616e-03, 8.516263371e-03, 2.220390414e-02, 4.973556312e-02,
                1.012215158e-01, 1.950155281e-01, 3.733749174e-01, 1.000000000e+00, 1.000000000e+00});
        checkCumulative(4, 1,
                x, new double[]{
                0.0000, 0.0000, 0.0001, 0.0016, 0.0081, 0.0256, 0.0625, 0.1296, 0.2401,
                0.4096, 0.6561, 1.0000, 1.0000});
        checkCumulative(4, 2,
                x, new double[]{
                0.00000, 0.00000, 0.00046, 0.00672, 0.03078, 0.08704, 0.18750, 0.33696,
                0.52822, 0.73728, 0.91854, 1.00000, 1.00000});
        checkCumulative(4, 4,
                x, new double[]{
                0.000000, 0.000000, 0.002728, 0.033344, 0.126036, 0.289792, 0.500000,
                0.710208, 0.873964, 0.966656, 0.997272, 1.000000, 1.000000});

    }

// org.apache.commons.math.distribution.BetaDistributionTest::testDensity
    public void testDensity() {
        double[] x = new double[]{1e-6, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9};
        checkDensity(0.1, 0.1,
                x, new double[]{
                12741.2357380649, 0.4429889586665234, 2.639378715e-01, 2.066393611e-01,
                1.832401831e-01, 1.766302780e-01, 1.832404579e-01, 2.066400696e-01,
                2.639396531e-01, 4.429925026e-01});
        checkDensity(0.1, 0.5,
                x, new double[]{
                2.218377102e+04, 7.394524202e-01, 4.203020268e-01, 3.119435533e-01,
                2.600787829e-01, 2.330648626e-01, 2.211408259e-01, 2.222728708e-01,
                2.414013907e-01, 3.070567405e-01});
        checkDensity(0.1, 1.0,
                x, new double[]{
                2.511886432e+04, 7.943210858e-01, 4.256680458e-01, 2.955218303e-01,
                2.281103709e-01, 1.866062624e-01, 1.583664652e-01, 1.378514078e-01,
                1.222414585e-01, 1.099464743e-01});
        checkDensity(0.1, 2.0,
                x, new double[]{
                2.763072312e+04, 7.863770012e-01, 3.745874120e-01, 2.275514842e-01,
                1.505525939e-01, 1.026332391e-01, 6.968107049e-02, 4.549081293e-02,
                2.689298641e-02, 1.209399123e-02});
        checkDensity(0.1, 4.0,
                x, new double[]{
                2.997927462e+04, 6.911058917e-01, 2.601128486e-01, 1.209774010e-01,
                5.880564714e-02, 2.783915474e-02, 1.209657335e-02, 4.442148268e-03,
                1.167143939e-03, 1.312171805e-04});
        checkDensity(0.5, 0.1,
                x, new double[]{
                88.3152184726, 0.3070542841, 0.2414007269, 0.2222727015,
                0.2211409364, 0.2330652355, 0.2600795198, 0.3119449793,
                0.4203052841, 0.7394649088});
        checkDensity(0.5, 0.5,
                x, new double[]{
                318.3100453389, 1.0610282383, 0.7957732234, 0.6946084565,
                0.6497470636, 0.6366197724, 0.6497476051, 0.6946097796,
                0.7957762075, 1.0610376697});
        checkDensity(0.5, 1.0,
                x, new double[]{
                500.0000000000, 1.5811309244, 1.1180311937, 0.9128694077,
                0.7905684268, 0.7071060741, 0.6454966865, 0.5976138778,
                0.5590166450, 0.5270459839});
        checkDensity(0.5, 2.0,
                x, new double[]{
                749.99925000000, 2.134537420613655, 1.34163575536, 0.95851150881,
                0.71151039830, 0.53032849490, 0.38729704363, 0.26892534859,
                0.16770415497, 0.07905610701});
        checkDensity(0.5, 4.0,
                x, new double[]{
                1.093746719e+03, 2.52142232809988, 1.252190241e+00, 6.849343920e-01,
                3.735417140e-01, 1.933481570e-01, 9.036885833e-02, 3.529621669e-02,
                9.782644546e-03, 1.152878503e-03});
        checkDensity(1.0, 0.1,
                x, new double[]{
                0.1000000900, 0.1099466942, 0.1222417336, 0.1378517623, 0.1583669403,
                0.1866069342, 0.2281113974, 0.2955236034, 0.4256718768,
                0.7943353837});
        checkDensity(1.0, 0.5,
                x, new double[]{
                0.5000002500, 0.5270465695, 0.5590173438, 0.5976147315, 0.6454977623,
                0.7071074883, 0.7905704033, 0.9128724506,
                1.1180367838, 1.5811467358});
        checkDensity(1, 1,
                x, new double[]{
                1, 1, 1,
                1, 1, 1, 1,
                1, 1, 1});
        checkDensity(1, 2,
                x, new double[]{
                1.999998, 1.799998, 1.599998, 1.399998, 1.199998, 0.999998, 0.799998,
                0.599998, 0.399998,
                0.199998});
        checkDensity(1, 4,
                x, new double[]{
                3.999988000012, 2.915990280011, 2.047992320010, 1.371994120008,
                0.863995680007, 0.499997000006, 0.255998080005, 0.107998920004,
                0.031999520002, 0.003999880001});
        checkDensity(2.0, 0.1,
                x, new double[]{
                1.100000990e-07, 1.209425730e-02, 2.689331586e-02, 4.549123318e-02,
                6.968162794e-02, 1.026340191e-01, 1.505537732e-01, 2.275534997e-01,
                3.745917198e-01, 7.863929037e-01});
        checkDensity(2.0, 0.5,
                x, new double[]{
                7.500003750e-07, 7.905777599e-02, 1.677060417e-01, 2.689275256e-01,
                3.872996256e-01, 5.303316769e-01, 7.115145488e-01, 9.585174425e-01,
                1.341645818e+00, 2.134537420613655});
        checkDensity(2, 1,
                x, new double[]{
                0.000002, 0.200002, 0.400002, 0.600002, 0.800002, 1.000002, 1.200002,
                1.400002, 1.600002,
                1.800002});
        checkDensity(2, 2,
                x, new double[]{
                5.9999940e-06, 5.4000480e-01, 9.6000360e-01, 1.2600024e+00,
                1.4400012e+00, 1.5000000e+00, 1.4399988e+00, 1.2599976e+00,
                9.5999640e-01, 5.3999520e-01});
        checkDensity(2, 4,
                x, new double[]{
                0.00001999994, 1.45800971996, 2.04800255997, 2.05799803998,
                1.72799567999, 1.24999500000, 0.76799552000, 0.37799676001,
                0.12799824001, 0.01799948000});
        checkDensity(4.0, 0.1,
                x, new double[]{
                1.193501074e-19, 1.312253162e-04, 1.167181580e-03, 4.442248535e-03,
                1.209679109e-02, 2.783958903e-02, 5.880649983e-02, 1.209791638e-01,
                2.601171405e-01, 6.911229392e-01});
        checkDensity(4.0, 0.5,
                x, new double[]{
                1.093750547e-18, 1.152948959e-03, 9.782950259e-03, 3.529697305e-02,
                9.037036449e-02, 1.933508639e-01, 3.735463833e-01, 6.849425461e-01,
                1.252205894e+00, 2.52142232809988});
        checkDensity(4, 1,
                x, new double[]{
                4.000000000e-18, 4.000120001e-03, 3.200048000e-02, 1.080010800e-01,
                2.560019200e-01, 5.000030000e-01, 8.640043200e-01, 1.372005880e+00,
                2.048007680e+00, 2.916009720e+00});
        checkDensity(4, 2,
                x, new double[]{
                1.999998000e-17, 1.800052000e-02, 1.280017600e-01, 3.780032400e-01,
                7.680044800e-01, 1.250005000e+00, 1.728004320e+00, 2.058001960e+00,
                2.047997440e+00, 1.457990280e+00});
        checkDensity(4, 4,
                x, new double[]{
                1.399995800e-16, 1.020627216e-01, 5.734464512e-01, 1.296547409e+00,
                1.935364838e+00, 2.187500000e+00, 1.935355162e+00, 1.296532591e+00,
                5.734335488e-01, 1.020572784e-01});

    }

// org.apache.commons.math.distribution.BetaDistributionTest::testMomonts
    public void testMomonts() {
        final double tol = 1e-9;
        BetaDistribution dist;
        
        dist = new BetaDistributionImpl(1, 1);
        assertEquals(dist.getNumericalMean(), 0.5, tol);
        assertEquals(dist.getNumericalVariance(), 1.0 / 12.0, tol); 
        
        dist = new BetaDistributionImpl(2, 5);
        assertEquals(dist.getNumericalMean(), 2.0 / 7.0, tol);
        assertEquals(dist.getNumericalVariance(), 10.0 / (49.0 * 8.0), tol); 
    }

// org.apache.commons.math.distribution.BinomialDistributionTest::testDegenerate0
    public void testDegenerate0() throws Exception {
        setDistribution(new BinomialDistributionImpl(5, 0.0d));
        setCumulativeTestPoints(new int[] { -1, 0, 1, 5, 10 });
        setCumulativeTestValues(new double[] { 0d, 1d, 1d, 1d, 1d });
        setDensityTestPoints(new int[] { -1, 0, 1, 10, 11 });
        setDensityTestValues(new double[] { 0d, 1d, 0d, 0d, 0d });
        setInverseCumulativeTestPoints(new double[] { 0.1d, 0.5d });
        setInverseCumulativeTestValues(new int[] { -1, -1 });
        verifyDensities();
        verifyCumulativeProbabilities();
        verifyInverseCumulativeProbabilities();
    }

// org.apache.commons.math.distribution.BinomialDistributionTest::testDegenerate1
    public void testDegenerate1() throws Exception {
        setDistribution(new BinomialDistributionImpl(5, 1.0d));
        setCumulativeTestPoints(new int[] { -1, 0, 1, 2, 5, 10 });
        setCumulativeTestValues(new double[] { 0d, 0d, 0d, 0d, 1d, 1d });
        setDensityTestPoints(new int[] { -1, 0, 1, 2, 5, 10 });
        setDensityTestValues(new double[] { 0d, 0d, 0d, 0d, 1d, 0d });
        setInverseCumulativeTestPoints(new double[] { 0.1d, 0.5d });
        setInverseCumulativeTestValues(new int[] { 4, 4 });
        verifyDensities();
        verifyCumulativeProbabilities();
        verifyInverseCumulativeProbabilities();
    }

// org.apache.commons.math.distribution.BinomialDistributionTest::testMomonts
    public void testMomonts() {
        final double tol = 1e-9;
        BinomialDistribution dist;
        
        dist = new BinomialDistributionImpl(10, 0.5);
        assertEquals(dist.getNumericalMean(), 10d * 0.5d, tol);
        assertEquals(dist.getNumericalVariance(), 10d * 0.5d * 0.5d, tol); 
        
        dist = new BinomialDistributionImpl(30, 0.3);
        assertEquals(dist.getNumericalMean(), 30d * 0.3d, tol);
        assertEquals(dist.getNumericalVariance(), 30d * 0.3d * (1d - 0.3d), tol);
    }

// org.apache.commons.math.distribution.CauchyDistributionTest::testInverseCumulativeProbabilityExtremes
    public void testInverseCumulativeProbabilityExtremes() throws Exception {
        setInverseCumulativeTestPoints(new double[] {0.0, 1.0});
        setInverseCumulativeTestValues(
                new double[] {Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY});
        verifyInverseCumulativeProbabilities();
    }

// org.apache.commons.math.distribution.CauchyDistributionTest::testMedian
    public void testMedian() {
        CauchyDistribution distribution = (CauchyDistribution) getDistribution();
        assertEquals(1.2, distribution.getMedian(), 0.0);
    }

// org.apache.commons.math.distribution.CauchyDistributionTest::testScale
    public void testScale() {
        CauchyDistribution distribution = (CauchyDistribution) getDistribution();
        assertEquals(2.1, distribution.getScale(), 0.0);
    }

// org.apache.commons.math.distribution.CauchyDistributionTest::testPreconditions
    public void testPreconditions() {
        try {
            new CauchyDistributionImpl(0, 0);
            fail("Cannot have zero scale");
        } catch (NotStrictlyPositiveException ex) {
            
        }
        try {
            new CauchyDistributionImpl(0, -1);
            fail("Cannot have negative scale");
        } catch (NotStrictlyPositiveException ex) {
            
        }
    }

// org.apache.commons.math.distribution.CauchyDistributionTest::testMomonts
    public void testMomonts() {
        CauchyDistribution dist;
        
        dist = new CauchyDistributionImpl(10.2, 0.15);        
        assertTrue(Double.isNaN(dist.getNumericalMean()));
        assertTrue(Double.isNaN(dist.getNumericalVariance()));
        
        dist = new CauchyDistributionImpl(23.12, 2.12);
        assertTrue(Double.isNaN(dist.getNumericalMean()));
        assertTrue(Double.isNaN(dist.getNumericalVariance()));
    }

// org.apache.commons.math.distribution.CauchyDistributionTest::testSampling
    public void testSampling() {}

// org.apache.commons.math.distribution.ChiSquareDistributionTest::testSmallDf
    public void testSmallDf() throws Exception {
        setDistribution(new ChiSquaredDistributionImpl(0.1d));
        setTolerance(1E-4);
        
        setCumulativeTestPoints(new double[] {1.168926E-60, 1.168926E-40, 1.063132E-32,
                1.144775E-26, 1.168926E-20, 5.472917, 2.175255, 1.13438,
                0.5318646, 0.1526342});
        setInverseCumulativeTestValues(getCumulativeTestPoints());
        setInverseCumulativeTestPoints(getCumulativeTestValues());
        verifyCumulativeProbabilities();
        verifyInverseCumulativeProbabilities();
    }

// org.apache.commons.math.distribution.ChiSquareDistributionTest::testDfAccessors
    public void testDfAccessors() {
        ChiSquaredDistribution distribution = (ChiSquaredDistribution) getDistribution();
        assertEquals(5d, distribution.getDegreesOfFreedom(), Double.MIN_VALUE);
    }

// org.apache.commons.math.distribution.ChiSquareDistributionTest::testDensity
    public void testDensity() {
        double[] x = new double[]{-0.1, 1e-6, 0.5, 1, 2, 5};
        
        checkDensity(1, x, new double[]{0.00000000000, 398.94208093034, 0.43939128947, 0.24197072452, 0.10377687436, 0.01464498256});
        
        checkDensity(0.1, x, new double[]{0.000000000e+00, 2.486453997e+04, 7.464238732e-02, 3.009077718e-02, 9.447299159e-03, 8.827199396e-04});
        
        checkDensity(2, x, new double[]{0.00000000000, 0.49999975000, 0.38940039154, 0.30326532986, 0.18393972059, 0.04104249931});
        
        checkDensity(10, x, new double[]{0.000000000e+00, 1.302082682e-27, 6.337896998e-05, 7.897534632e-04, 7.664155024e-03, 6.680094289e-02});
    }

// org.apache.commons.math.distribution.ChiSquareDistributionTest::testMomonts
    public void testMomonts() {
        final double tol = 1e-9;
        ChiSquaredDistribution dist;
        
        dist = new ChiSquaredDistributionImpl(1500);
        assertEquals(dist.getNumericalMean(), 1500, tol);
        assertEquals(dist.getNumericalVariance(), 3000, tol); 
        
        dist = new ChiSquaredDistributionImpl(1.12);
        assertEquals(dist.getNumericalMean(), 1.12, tol);
        assertEquals(dist.getNumericalVariance(), 2.24, tol);
    }

// org.apache.commons.math.distribution.ChiSquareDistributionTest::testSampling
    public void testSampling() {}

// org.apache.commons.math.distribution.ExponentialDistributionTest::testCumulativeProbabilityExtremes
    public void testCumulativeProbabilityExtremes() throws Exception {
        setCumulativeTestPoints(new double[] {-2, 0});
        setCumulativeTestValues(new double[] {0, 0});
        verifyCumulativeProbabilities();
    }

// org.apache.commons.math.distribution.ExponentialDistributionTest::testInverseCumulativeProbabilityExtremes
    public void testInverseCumulativeProbabilityExtremes() throws Exception {
         setInverseCumulativeTestPoints(new double[] {0, 1});
         setInverseCumulativeTestValues(new double[] {0, Double.POSITIVE_INFINITY});
         verifyInverseCumulativeProbabilities();
    }

// org.apache.commons.math.distribution.ExponentialDistributionTest::testCumulativeProbability2
    public void testCumulativeProbability2() throws Exception {
        double actual = getDistribution().cumulativeProbability(0.25, 0.75);
        assertEquals(0.0905214, actual, 10e-4);
    }

// org.apache.commons.math.distribution.ExponentialDistributionTest::testDensity
    public void testDensity() {
        ExponentialDistribution d1 = new ExponentialDistributionImpl(1);
        assertEquals(0.0, d1.density(-1e-9));
        assertEquals(1.0, d1.density(0.0));
        assertEquals(0.0, d1.density(1000.0));
        assertEquals(FastMath.exp(-1), d1.density(1.0));
        assertEquals(FastMath.exp(-2), d1.density(2.0));

        ExponentialDistribution d2 = new ExponentialDistributionImpl(3);
        assertEquals(1/3.0, d2.density(0.0));
        
        assertEquals(0.2388437702, d2.density(1.0), 1e-8);

        
        assertEquals(0.1711390397, d2.density(2.0), 1e-8);
    }

// org.apache.commons.math.distribution.ExponentialDistributionTest::testMeanAccessors
    public void testMeanAccessors() {
        ExponentialDistribution distribution = (ExponentialDistribution) getDistribution();
        assertEquals(5d, distribution.getMean(), Double.MIN_VALUE);
    }

// org.apache.commons.math.distribution.ExponentialDistributionTest::testPreconditions
    public void testPreconditions() {
        try {
            new ExponentialDistributionImpl(0);
            fail("Should have generated NotStrictlyPositiveException");
        } catch (NotStrictlyPositiveException e) {
            
        }
    }

// org.apache.commons.math.distribution.ExponentialDistributionTest::testMomonts
    public void testMomonts() {
        final double tol = 1e-9;
        ExponentialDistribution dist;
        
        dist = new ExponentialDistributionImpl(11d);
        assertEquals(dist.getNumericalMean(), 11d, tol);
        assertEquals(dist.getNumericalVariance(), 11d * 11d, tol);
        
        dist = new ExponentialDistributionImpl(10.5d);
        assertEquals(dist.getNumericalMean(), 10.5d, tol);
        assertEquals(dist.getNumericalVariance(), 10.5d * 10.5d, tol);
    }

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
        FDistribution dist = (FDistribution) getDistribution();
        assertEquals(5d, dist.getNumeratorDegreesOfFreedom(), Double.MIN_VALUE);
        assertEquals(6d, dist.getDenominatorDegreesOfFreedom(), Double.MIN_VALUE);
    }

// org.apache.commons.math.distribution.FDistributionTest::testPreconditions
    public void testPreconditions() {
        try {
            new FDistributionImpl(0, 1);
            fail("Expecting NotStrictlyPositiveException for df = 0");
        } catch (NotStrictlyPositiveException ex) {
            
        }
        try {
            new FDistributionImpl(1, 0);
            fail("Expecting NotStrictlyPositiveException for df = 0");
        } catch (NotStrictlyPositiveException ex) {
            
        }
    }

// org.apache.commons.math.distribution.FDistributionTest::testLargeDegreesOfFreedom
    public void testLargeDegreesOfFreedom() throws Exception {
        FDistributionImpl fd = new FDistributionImpl(100000, 100000);
        double p = fd.cumulativeProbability(.999);
        double x = fd.inverseCumulativeProbability(p);
        assertEquals(.999, x, 1.0e-5);
    }

// org.apache.commons.math.distribution.FDistributionTest::testSmallDegreesOfFreedom
    public void testSmallDegreesOfFreedom() throws Exception {
        FDistributionImpl fd = new FDistributionImpl(1, 1);
        double p = fd.cumulativeProbability(0.975);
        double x = fd.inverseCumulativeProbability(p);
        assertEquals(0.975, x, 1.0e-5);

        fd = new FDistributionImpl(1, 2);
        p = fd.cumulativeProbability(0.975);
        x = fd.inverseCumulativeProbability(p);
        assertEquals(0.975, x, 1.0e-5);
    }

// org.apache.commons.math.distribution.FDistributionTest::testMomonts
    public void testMomonts() {
        final double tol = 1e-9;
        FDistribution dist;
        
        dist = new FDistributionImpl(1, 2);
        assertTrue(Double.isNaN(dist.getNumericalMean()));
        assertTrue(Double.isNaN(dist.getNumericalVariance()));
        
        dist = new FDistributionImpl(1, 3);
        assertEquals(dist.getNumericalMean(), 3d / (3d - 2d), tol);
        assertTrue(Double.isNaN(dist.getNumericalVariance()));
        
        dist = new FDistributionImpl(1, 5);
        assertEquals(dist.getNumericalMean(), 5d / (5d - 2d), tol);
        assertEquals(dist.getNumericalVariance(), (2d * 5d * 5d * 4d) / 9d, tol);        
    }

// org.apache.commons.math.distribution.GammaDistributionTest::testParameterAccessors
    public void testParameterAccessors() {
        GammaDistribution distribution = (GammaDistribution) getDistribution();
        assertEquals(4d, distribution.getAlpha(), 0);
        assertEquals(2d, distribution.getBeta(), 0);
    }

// org.apache.commons.math.distribution.GammaDistributionTest::testPreconditions
    public void testPreconditions() {
        try {
            new GammaDistributionImpl(0, 1);
            fail("Expecting NotStrictlyPositiveException for alpha = 0");
        } catch (NotStrictlyPositiveException ex) {
            
        }
        try {
            new GammaDistributionImpl(1, 0);
            fail("Expecting NotStrictlyPositiveException for alpha = 0");
        } catch (NotStrictlyPositiveException ex) {
            
        }
    }

// org.apache.commons.math.distribution.GammaDistributionTest::testProbabilities
    public void testProbabilities() throws Exception {
        testProbability(-1.000, 4.0, 2.0, .0000);
        testProbability(15.501, 4.0, 2.0, .9499);
        testProbability(0.504, 4.0, 1.0, .0018);
        testProbability(10.011, 1.0, 2.0, .9933);
        testProbability(5.000, 2.0, 2.0, .7127);
    }

// org.apache.commons.math.distribution.GammaDistributionTest::testValues
    public void testValues() throws Exception {
        testValue(15.501, 4.0, 2.0, .9499);
        testValue(0.504, 4.0, 1.0, .0018);
        testValue(10.011, 1.0, 2.0, .9933);
        testValue(5.000, 2.0, 2.0, .7127);
    }

// org.apache.commons.math.distribution.GammaDistributionTest::testDensity
    public void testDensity() {
        double[] x = new double[]{-0.1, 1e-6, 0.5, 1, 2, 5};
        
        checkDensity(1, 1, x, new double[]{0.000000000000, 0.999999000001, 0.606530659713, 0.367879441171, 0.135335283237, 0.006737946999});
        
        checkDensity(2, 1, x, new double[]{0.000000000000, 0.000000999999, 0.303265329856, 0.367879441171, 0.270670566473, 0.033689734995});
        
        checkDensity(4, 1, x, new double[]{0.000000000e+00, 1.666665000e-19, 1.263605541e-02, 6.131324020e-02, 1.804470443e-01, 1.403738958e-01});
        
        checkDensity(4, 10, x, new double[]{0.000000000e+00, 1.666650000e-15, 1.403738958e+00, 7.566654960e-02, 2.748204830e-05, 4.018228850e-17});
        
        checkDensity(0.1, 10, x, new double[]{0.000000000e+00, 3.323953832e+04, 1.663849010e-03, 6.007786726e-06, 1.461647647e-10, 5.996008322e-24});
        
        checkDensity(0.1, 20, x, new double[]{0.000000000e+00, 3.562489883e+04, 1.201557345e-05, 2.923295295e-10, 3.228910843e-19, 1.239484589e-45});
        
        checkDensity(0.1, 4, x, new double[]{0.000000000e+00, 3.032938388e+04, 3.049322494e-02, 2.211502311e-03, 2.170613371e-05, 5.846590589e-11});
        
        checkDensity(0.1, 1, x, new double[]{0.000000000e+00, 2.640334143e+04, 1.189704437e-01, 3.866916944e-02, 7.623306235e-03, 1.663849010e-04});
    }

// org.apache.commons.math.distribution.GammaDistributionTest::testInverseCumulativeProbabilityExtremes
    public void testInverseCumulativeProbabilityExtremes() throws Exception {
        setInverseCumulativeTestPoints(new double[] {0, 1});
        setInverseCumulativeTestValues(new double[] {0, Double.POSITIVE_INFINITY});
        verifyInverseCumulativeProbabilities();
    }

// org.apache.commons.math.distribution.GammaDistributionTest::testMomonts
    public void testMomonts() {
        final double tol = 1e-9;
        GammaDistribution dist;
        
        dist = new GammaDistributionImpl(1, 2);
        assertEquals(dist.getNumericalMean(), 2, tol);
        assertEquals(dist.getNumericalVariance(), 4, tol); 
        
        dist = new GammaDistributionImpl(1.1, 4.2);
        assertEquals(dist.getNumericalMean(), 1.1d * 4.2d, tol);
        assertEquals(dist.getNumericalVariance(), 1.1d * 4.2d * 4.2d, tol);
    }

// org.apache.commons.math.distribution.HypergeometricDistributionTest::testDegenerateNoFailures
    public void testDegenerateNoFailures() throws Exception {
        setDistribution(new HypergeometricDistributionImpl(5,5,3));
        setCumulativeTestPoints(new int[] {-1, 0, 1, 3, 10 });
        setCumulativeTestValues(new double[] {0d, 0d, 0d, 1d, 1d});
        setDensityTestPoints(new int[] {-1, 0, 1, 3, 10});
        setDensityTestValues(new double[] {0d, 0d, 0d, 1d, 0d});
        setInverseCumulativeTestPoints(new double[] {0.1d, 0.5d});
        setInverseCumulativeTestValues(new int[] {2, 2});
        verifyDensities();
        verifyCumulativeProbabilities();
        verifyInverseCumulativeProbabilities();
    }

// org.apache.commons.math.distribution.HypergeometricDistributionTest::testDegenerateNoSuccesses
    public void testDegenerateNoSuccesses() throws Exception {
        setDistribution(new HypergeometricDistributionImpl(5,0,3));
        setCumulativeTestPoints(new int[] {-1, 0, 1, 3, 10 });
        setCumulativeTestValues(new double[] {0d, 1d, 1d, 1d, 1d});
        setDensityTestPoints(new int[] {-1, 0, 1, 3, 10});
        setDensityTestValues(new double[] {0d, 1d, 0d, 0d, 0d});
        setInverseCumulativeTestPoints(new double[] {0.1d, 0.5d});
        setInverseCumulativeTestValues(new int[] {-1, -1});
        verifyDensities();
        verifyCumulativeProbabilities();
        verifyInverseCumulativeProbabilities();
    }

// org.apache.commons.math.distribution.HypergeometricDistributionTest::testDegenerateFullSample
    public void testDegenerateFullSample() throws Exception {
        setDistribution(new HypergeometricDistributionImpl(5,3,5));
        setCumulativeTestPoints(new int[] {-1, 0, 1, 3, 10 });
        setCumulativeTestValues(new double[] {0d, 0d, 0d, 1d, 1d});
        setDensityTestPoints(new int[] {-1, 0, 1, 3, 10});
        setDensityTestValues(new double[] {0d, 0d, 0d, 1d, 0d});
        setInverseCumulativeTestPoints(new double[] {0.1d, 0.5d});
        setInverseCumulativeTestValues(new int[] {2, 2});
        verifyDensities();
        verifyCumulativeProbabilities();
        verifyInverseCumulativeProbabilities();
    }

// org.apache.commons.math.distribution.HypergeometricDistributionTest::testPreconditions
    public void testPreconditions() {
        try {
            new HypergeometricDistributionImpl(0, 3, 5);
            fail("negative population size. NotStrictlyPositiveException expected");
        } catch(NotStrictlyPositiveException ex) {
            
        }
        try {
            new HypergeometricDistributionImpl(5, -1, 5);
            fail("negative number of successes. NotPositiveException expected");
        } catch(NotPositiveException ex) {
            
        }
        try {
            new HypergeometricDistributionImpl(5, 3, -1);
            fail("negative sample size. NotPositiveException expected");
        } catch(NotPositiveException ex) {
            
        }
        try {
            new HypergeometricDistributionImpl(5, 6, 5);
            fail("numberOfSuccesses > populationSize. NumberIsTooLargeException expected");
        } catch(NumberIsTooLargeException ex) {
            
        }
        try {
            new HypergeometricDistributionImpl(5, 3, 6);
            fail("sampleSize > populationSize. NumberIsTooLargeException expected");
        } catch(NumberIsTooLargeException ex) {
            
        }
    }

// org.apache.commons.math.distribution.HypergeometricDistributionTest::testAccessors
    public void testAccessors() {
        HypergeometricDistribution dist = new HypergeometricDistributionImpl(5, 3, 4);
        assertEquals(5, dist.getPopulationSize());
        assertEquals(3, dist.getNumberOfSuccesses());
        assertEquals(4, dist.getSampleSize());
    }

// org.apache.commons.math.distribution.HypergeometricDistributionTest::testLargeValues
    public void testLargeValues() {
        int populationSize = 3456;
        int sampleSize = 789;
        int numberOfSucceses = 101;
        double[][] data = {
            {0.0, 2.75646034603961e-12, 2.75646034603961e-12, 1.0},
            {1.0, 8.55705370142386e-11, 8.83269973602783e-11, 0.999999999997244},
            {2.0, 1.31288129219665e-9, 1.40120828955693e-9, 0.999999999911673},
            {3.0, 1.32724172984193e-8, 1.46736255879763e-8, 0.999999998598792},
            {4.0, 9.94501711734089e-8, 1.14123796761385e-7, 0.999999985326375},
            {5.0, 5.89080768883643e-7, 7.03204565645028e-7, 0.999999885876203},
            {20.0, 0.0760051397707708, 0.27349758476299, 0.802507555007781},
            {21.0, 0.087144222047629, 0.360641806810619, 0.72650241523701},
            {22.0, 0.0940378846881819, 0.454679691498801, 0.639358193189381},
            {23.0, 0.0956897500614809, 0.550369441560282, 0.545320308501199},
            {24.0, 0.0919766921922999, 0.642346133752582, 0.449630558439718},
            {25.0, 0.083641637261095, 0.725987771013677, 0.357653866247418},
            {96.0, 5.93849188852098e-57, 1.0, 6.01900244560712e-57},
            {97.0, 7.96593036832547e-59, 1.0, 8.05105570861321e-59},
            {98.0, 8.44582921934367e-61, 1.0, 8.5125340287733e-61},
            {99.0, 6.63604297068222e-63, 1.0, 6.670480942963e-63},
            {100.0, 3.43501099007557e-65, 1.0, 3.4437972280786e-65},
            {101.0, 8.78623800302957e-68, 1.0, 8.78623800302957e-68},
        };

        testHypergeometricDistributionProbabilities(populationSize, sampleSize, numberOfSucceses, data);
    }

// org.apache.commons.math.distribution.HypergeometricDistributionTest::testMoreLargeValues
    public void testMoreLargeValues() {
        int populationSize = 26896;
        int sampleSize = 895;
        int numberOfSucceses = 55;
        double[][] data = {
            {0.0, 0.155168304750504, 0.155168304750504, 1.0},
            {1.0, 0.29437545000746, 0.449543754757964, 0.844831695249496},
            {2.0, 0.273841321577003, 0.723385076334967, 0.550456245242036},
            {3.0, 0.166488572570786, 0.889873648905753, 0.276614923665033},
            {4.0, 0.0743969744713231, 0.964270623377076, 0.110126351094247},
            {5.0, 0.0260542785784855, 0.990324901955562, 0.0357293766229237},
            {20.0, 3.57101101678792e-16, 1.0, 3.78252101622096e-16},
            {21.0, 2.00551638598312e-17, 1.0, 2.11509999433041e-17},
            {22.0, 1.04317070180562e-18, 1.0, 1.09583608347287e-18},
            {23.0, 5.03153504903308e-20, 1.0, 5.266538166725e-20},
            {24.0, 2.2525984149695e-21, 1.0, 2.35003117691919e-21},
            {25.0, 9.3677424515947e-23, 1.0, 9.74327619496943e-23},
            {50.0, 9.83633962945521e-69, 1.0, 9.8677629437617e-69},
            {51.0, 3.13448949497553e-71, 1.0, 3.14233143064882e-71},
            {52.0, 7.82755221928122e-74, 1.0, 7.84193567329055e-74},
            {53.0, 1.43662126065532e-76, 1.0, 1.43834540093295e-76},
            {54.0, 1.72312692517348e-79, 1.0, 1.7241402776278e-79},
            {55.0, 1.01335245432581e-82, 1.0, 1.01335245432581e-82},
        };
        testHypergeometricDistributionProbabilities(populationSize, sampleSize, numberOfSucceses, data);
    }

// org.apache.commons.math.distribution.HypergeometricDistributionTest::testMomonts
    public void testMomonts() {
        final double tol = 1e-9;
        HypergeometricDistribution dist;
        
        dist = new HypergeometricDistributionImpl(1500, 40, 100);
        assertEquals(dist.getNumericalMean(), 40d * 100d / 1500d, tol);
        assertEquals(dist.getNumericalVariance(), ( 100d * 40d * (1500d - 100d) * (1500d - 40d) ) / ( (1500d * 1500d * 1499d) ), tol); 
        
        dist = new HypergeometricDistributionImpl(3000, 55, 200);
        assertEquals(dist.getNumericalMean(), 55d * 200d / 3000d, tol);
        assertEquals(dist.getNumericalVariance(), ( 200d * 55d * (3000d - 200d) * (3000d - 55d) ) / ( (3000d * 3000d * 2999d) ), tol);
    }

// org.apache.commons.math.distribution.NormalDistributionTest::testQuantiles
    public void testQuantiles() throws Exception {
        setDensityTestValues(new double[] {0.0385649760808, 0.172836231799, 0.284958771715, 0.172836231799, 0.0385649760808,
                0.00316560600853, 9.55930184035e-05, 1.06194251052e-06});
        verifyQuantiles();
        verifyDensities();

        setDistribution(new NormalDistributionImpl(0, 1));
        setDensityTestValues(new double[] {0.0539909665132, 0.241970724519, 0.398942280401, 0.241970724519, 0.0539909665132,
                0.00443184841194, 0.000133830225765, 1.48671951473e-06});
        verifyQuantiles();
        verifyDensities();

        setDistribution(new NormalDistributionImpl(0, 0.1));
        setDensityTestValues(new double[] {0.539909665132, 2.41970724519, 3.98942280401, 2.41970724519,
                0.539909665132, 0.0443184841194, 0.00133830225765, 1.48671951473e-05});
        verifyQuantiles();
        verifyDensities();
    }

// org.apache.commons.math.distribution.NormalDistributionTest::testInverseCumulativeProbabilityExtremes
    public void testInverseCumulativeProbabilityExtremes() throws Exception {
        setInverseCumulativeTestPoints(new double[] {0, 1});
        setInverseCumulativeTestValues(
                new double[] {Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY});
        verifyInverseCumulativeProbabilities();
    }

// org.apache.commons.math.distribution.NormalDistributionTest::testGetMean
    public void testGetMean() {
        NormalDistribution distribution = (NormalDistribution) getDistribution();
        assertEquals(2.1, distribution.getMean(), 0);
    }

// org.apache.commons.math.distribution.NormalDistributionTest::testGetStandardDeviation
    public void testGetStandardDeviation() {
        NormalDistribution distribution = (NormalDistribution) getDistribution();
        assertEquals(1.4, distribution.getStandardDeviation(), 0);
    }

// org.apache.commons.math.distribution.NormalDistributionTest::testPreconditions
    public void testPreconditions() {
        try {
            new NormalDistributionImpl(1, 0);
            fail("Should have generated NotStrictlyPositiveException");
        } catch (NotStrictlyPositiveException e) {
            
        }
    }

// org.apache.commons.math.distribution.NormalDistributionTest::testDensity
    public void testDensity() {
        double [] x = new double[]{-2, -1, 0, 1, 2};
        
        checkDensity(0, 1, x, new double[]{0.05399096651, 0.24197072452, 0.39894228040, 0.24197072452, 0.05399096651});
        
        checkDensity(1.1, 1, x, new double[]{0.003266819056,0.043983595980,0.217852177033,0.396952547477,0.266085249899});
    }

// org.apache.commons.math.distribution.NormalDistributionTest::testExtremeValues
    public void testExtremeValues() throws Exception {
        NormalDistribution distribution = new NormalDistributionImpl(0, 1);
        for (int i = 0; i < 100; i++) { 
            double lowerTail = distribution.cumulativeProbability(-i);
            double upperTail = distribution.cumulativeProbability(i);
            if (i < 9) { 
                
                
                assertTrue(lowerTail > 0.0d);
                assertTrue(upperTail < 1.0d);
            }
            else { 
                assertTrue(lowerTail < 0.00001);
                assertTrue(upperTail > 0.99999);
            }
        }
        
        assertEquals(distribution.cumulativeProbability(Double.MAX_VALUE), 1, 0);
        assertEquals(distribution.cumulativeProbability(-Double.MAX_VALUE), 0, 0);
        assertEquals(distribution.cumulativeProbability(Double.POSITIVE_INFINITY), 1, 0);
        assertEquals(distribution.cumulativeProbability(Double.NEGATIVE_INFINITY), 0, 0);
        
   }

// org.apache.commons.math.distribution.NormalDistributionTest::testMath280
    public void testMath280() throws MathException {
        NormalDistribution normal = new NormalDistributionImpl(0,1);
        double result = normal.inverseCumulativeProbability(0.9986501019683698);
        assertEquals(3.0, result, defaultTolerance);
        result = normal.inverseCumulativeProbability(0.841344746068543);
        assertEquals(1.0, result, defaultTolerance);
        result = normal.inverseCumulativeProbability(0.9999683287581673);
        assertEquals(4.0, result, defaultTolerance);
        result = normal.inverseCumulativeProbability(0.9772498680518209);
        assertEquals(2.0, result, defaultTolerance);
    }

// org.apache.commons.math.distribution.NormalDistributionTest::testMomonts
    public void testMomonts() {
        final double tol = 1e-9;
        NormalDistribution dist;
        
        dist = new NormalDistributionImpl(0, 1);        
        assertEquals(dist.getNumericalMean(), 0, tol);
        assertEquals(dist.getNumericalVariance(), 1, tol);        
 
        dist = new NormalDistributionImpl(2.2, 1.4);
        assertEquals(dist.getNumericalMean(), 2.2, tol);
        assertEquals(dist.getNumericalVariance(), 1.4 * 1.4, tol);
        
        dist = new NormalDistributionImpl(-2000.9, 10.4);
        assertEquals(dist.getNumericalMean(), -2000.9, tol);
        assertEquals(dist.getNumericalVariance(), 10.4 * 10.4, tol);
    }

// org.apache.commons.math.distribution.PascalDistributionTest::testDegenerate0
    public void testDegenerate0() throws Exception {
        setDistribution(new PascalDistributionImpl(5,0.0d));
        setCumulativeTestPoints(new int[] {-1, 0, 1, 5, 10 });
        setCumulativeTestValues(new double[] {0d, 0d, 0d, 0d, 0d});
        setDensityTestPoints(new int[] {-1, 0, 1, 10, 11});
        setDensityTestValues(new double[] {0d, 0d, 0d, 0d, 0d});
        setInverseCumulativeTestPoints(new double[] {0.1d, 0.5d});
        setInverseCumulativeTestValues(new int[] {Integer.MAX_VALUE - 1, Integer.MAX_VALUE - 1});
        verifyDensities();
        verifyCumulativeProbabilities();
        verifyInverseCumulativeProbabilities();
    }

// org.apache.commons.math.distribution.PascalDistributionTest::testDegenerate1
    public void testDegenerate1() throws Exception {
        setDistribution(new PascalDistributionImpl(5,1.0d));
        setCumulativeTestPoints(new int[] {-1, 0, 1, 2, 5, 10 });
        setCumulativeTestValues(new double[] {0d, 1d, 1d, 1d, 1d, 1d});
        setDensityTestPoints(new int[] {-1, 0, 1, 2, 5, 10});
        setDensityTestValues(new double[] {0d, 1d, 0d, 0d, 0d, 0d});
        setInverseCumulativeTestPoints(new double[] {0.1d, 0.5d});
        setInverseCumulativeTestValues(new int[] {-1, -1});
        verifyDensities();
        verifyCumulativeProbabilities();
        verifyInverseCumulativeProbabilities();
    }

// org.apache.commons.math.distribution.PascalDistributionTest::testMomonts
    public void testMomonts() {
        final double tol = 1e-9;
        PascalDistribution dist;
        
        dist = new PascalDistributionImpl(10, 0.5);
        assertEquals(dist.getNumericalMean(), ( 10d * 0.5d ) / 0.5d, tol);
        assertEquals(dist.getNumericalVariance(), ( 10d * 0.5d ) / (0.5d * 0.5d), tol); 
        
        dist = new PascalDistributionImpl(25, 0.3);
        assertEquals(dist.getNumericalMean(), ( 25d * 0.3d ) / 0.7d, tol);
        assertEquals(dist.getNumericalVariance(), ( 25d * 0.3d ) / (0.7d * 0.7d), tol);
    }

// org.apache.commons.math.distribution.PoissonDistributionTest::testNormalApproximateProbability
    public void testNormalApproximateProbability() throws Exception {
        PoissonDistribution dist = new PoissonDistributionImpl(100);
        double result = dist.normalApproximateProbability(110)
                - dist.normalApproximateProbability(89);
        assertEquals(0.706281887248, result, 1E-10);

        dist = new PoissonDistributionImpl(10000);
        result = dist.normalApproximateProbability(10200)
        - dist.normalApproximateProbability(9899);
        assertEquals(0.820070051552, result, 1E-10);
    }

// org.apache.commons.math.distribution.PoissonDistributionTest::testDegenerateInverseCumulativeProbability
    public void testDegenerateInverseCumulativeProbability() throws Exception {
        PoissonDistribution dist = new PoissonDistributionImpl(DEFAULT_TEST_POISSON_PARAMETER);
        assertEquals(Integer.MAX_VALUE, dist.inverseCumulativeProbability(1.0d));
        assertEquals(-1, dist.inverseCumulativeProbability(0d));
    }

// org.apache.commons.math.distribution.PoissonDistributionTest::testMean
    public void testMean() {
        PoissonDistribution dist;
        try {
            dist = new PoissonDistributionImpl(-1);
            fail("negative mean: NotStrictlyPositiveException expected");
        } catch(NotStrictlyPositiveException ex) {
            
        }

        dist = new PoissonDistributionImpl(10.0);
        assertEquals(10.0, dist.getMean(), 0.0);
    }

// org.apache.commons.math.distribution.PoissonDistributionTest::testLargeMeanCumulativeProbability
    public void testLargeMeanCumulativeProbability() {
        double mean = 1.0;
        while (mean <= 10000000.0) {
            PoissonDistribution dist = new PoissonDistributionImpl(mean);

            double x = mean * 2.0;
            double dx = x / 10.0;
            double p = Double.NaN;
            double sigma = FastMath.sqrt(mean);
            while (x >= 0) {
                try {
                    p = dist.cumulativeProbability(x);
                    assertFalse("NaN cumulative probability returned for mean = " +
                            mean + " x = " + x,Double.isNaN(p));
                    if (x > mean - 2 * sigma) {
                        assertTrue("Zero cum probaility returned for mean = " +
                                mean + " x = " + x, p > 0);
                    }
                } catch (MathException ex) {
                    fail("mean of " + mean + " and x of " + x + " caused " + ex.getMessage());
                }
                x -= dx;
            }

            mean *= 10.0;
        }
    }

// org.apache.commons.math.distribution.PoissonDistributionTest::testCumulativeProbabilitySpecial
    public void testCumulativeProbabilitySpecial() throws Exception {
        PoissonDistribution dist;
        dist = new PoissonDistributionImpl(9120);
        checkProbability(dist, 9075);
        checkProbability(dist, 9102);
        dist = new PoissonDistributionImpl(5058);
        checkProbability(dist, 5044);
        dist = new PoissonDistributionImpl(6986);
        checkProbability(dist, 6950);
    }

// org.apache.commons.math.distribution.PoissonDistributionTest::testLargeMeanInverseCumulativeProbability
    public void testLargeMeanInverseCumulativeProbability() throws Exception {
        double mean = 1.0;
        while (mean <= 100000.0) { 
            PoissonDistribution dist = new PoissonDistributionImpl(mean);
            double p = 0.1;
            double dp = p;
            while (p < .99) {
                double ret = Double.NaN;
                try {
                    ret = dist.inverseCumulativeProbability(p);
                    
                    assertTrue(p >= dist.cumulativeProbability(ret));
                    assertTrue(p < dist.cumulativeProbability(ret + 1));
                } catch (MathException ex) {
                    fail("mean of " + mean + " and p of " + p + " caused " + ex.getMessage());
                }
                p += dp;
            }
            mean *= 10.0;
        }
    }

// org.apache.commons.math.distribution.PoissonDistributionTest::testMomonts
    public void testMomonts() {
        final double tol = 1e-9;
        PoissonDistribution dist;
        
        dist = new PoissonDistributionImpl(1);
        assertEquals(dist.getNumericalMean(), 1, tol);
        assertEquals(dist.getNumericalVariance(), 1, tol); 
        
        dist = new PoissonDistributionImpl(11.23);
        assertEquals(dist.getNumericalMean(), 11.23, tol);
        assertEquals(dist.getNumericalVariance(), 11.23, tol);
    }

// org.apache.commons.math.distribution.TDistributionTest::testCumulativeProbabilityAgaintStackOverflow
    public void testCumulativeProbabilityAgaintStackOverflow() throws Exception {
        TDistributionImpl td = new TDistributionImpl(5.);
        td.cumulativeProbability(.1);
        td.cumulativeProbability(.01);
    }

// org.apache.commons.math.distribution.TDistributionTest::testSmallDf
    public void testSmallDf() throws Exception {
        setDistribution(new TDistributionImpl(1d));
        
        setCumulativeTestPoints(new double[] {-318.308838986, -31.8205159538, -12.7062047362,
                -6.31375151468, -3.07768353718, 318.308838986, 31.8205159538, 12.7062047362,
                 6.31375151468, 3.07768353718});
        setDensityTestValues(new double[] {3.14158231817e-06, 0.000314055924703, 0.00195946145194,
                0.00778959736375, 0.0303958893917, 3.14158231817e-06, 0.000314055924703,
                0.00195946145194, 0.00778959736375, 0.0303958893917});
        setInverseCumulativeTestValues(getCumulativeTestPoints());
        verifyCumulativeProbabilities();
        verifyInverseCumulativeProbabilities();
        verifyDensities();
    }

// org.apache.commons.math.distribution.TDistributionTest::testInverseCumulativeProbabilityExtremes
    public void testInverseCumulativeProbabilityExtremes() throws Exception {
        setInverseCumulativeTestPoints(new double[] {0, 1});
        setInverseCumulativeTestValues(
                new double[] {Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY});
        verifyInverseCumulativeProbabilities();
    }
