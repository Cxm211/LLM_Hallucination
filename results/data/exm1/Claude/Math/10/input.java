// buggy code
    public void atan2(final double[] y, final int yOffset,
                      final double[] x, final int xOffset,
                      final double[] result, final int resultOffset) {

        // compute r = sqrt(x^2+y^2)
        double[] tmp1 = new double[getSize()];
        multiply(x, xOffset, x, xOffset, tmp1, 0);      // x^2
        double[] tmp2 = new double[getSize()];
        multiply(y, yOffset, y, yOffset, tmp2, 0);      // y^2
        add(tmp1, 0, tmp2, 0, tmp2, 0);                 // x^2 + y^2
        rootN(tmp2, 0, 2, tmp1, 0);                     // r = sqrt(x^2 + y^2)

        if (x[xOffset] >= 0) {

            // compute atan2(y, x) = 2 atan(y / (r + x))
            add(tmp1, 0, x, xOffset, tmp2, 0);          // r + x
            divide(y, yOffset, tmp2, 0, tmp1, 0);       // y /(r + x)
            atan(tmp1, 0, tmp2, 0);                     // atan(y / (r + x))
            for (int i = 0; i < tmp2.length; ++i) {
                result[resultOffset + i] = 2 * tmp2[i]; // 2 * atan(y / (r + x))
            }

        } else {

            // compute atan2(y, x) = +/- pi - 2 atan(y / (r - x))
            subtract(tmp1, 0, x, xOffset, tmp2, 0);     // r - x
            divide(y, yOffset, tmp2, 0, tmp1, 0);       // y /(r - x)
            atan(tmp1, 0, tmp2, 0);                     // atan(y / (r - x))
            result[resultOffset] =
                    ((tmp2[0] <= 0) ? -FastMath.PI : FastMath.PI) - 2 * tmp2[0]; // +/-pi - 2 * atan(y / (r - x))
            for (int i = 1; i < tmp2.length; ++i) {
                result[resultOffset + i] = -2 * tmp2[i]; // +/-pi - 2 * atan(y / (r - x))
            }

        }

        // fix value to take special cases (+0/+0, +0/-0, -0/+0, -0/-0, +/-infinity) correctly

    }

// relevant test
// org.apache.commons.math3.analysis.FunctionUtilsTest::testCompose
    public void testCompose() {
        UnivariateFunction id = new Identity();
        Assert.assertEquals(3, FunctionUtils.compose(id, id, id).value(3), EPS);

        UnivariateFunction c = new Constant(4);
        Assert.assertEquals(4, FunctionUtils.compose(id, c).value(3), EPS);
        Assert.assertEquals(4, FunctionUtils.compose(c, id).value(3), EPS);

        UnivariateFunction m = new Minus();
        Assert.assertEquals(-3, FunctionUtils.compose(m).value(3), EPS);
        Assert.assertEquals(3, FunctionUtils.compose(m, m).value(3), EPS);

        UnivariateFunction inv = new Inverse();
        Assert.assertEquals(-0.25, FunctionUtils.compose(inv, m, c, id).value(3), EPS);

        UnivariateFunction pow = new Power(2);
        Assert.assertEquals(81, FunctionUtils.compose(pow, pow).value(3), EPS);
    }

// org.apache.commons.math3.analysis.FunctionUtilsTest::testComposeDifferentiable
    public void testComposeDifferentiable() {
        UnivariateDifferentiableFunction id = new Identity();
        Assert.assertEquals(1, FunctionUtils.compose(id, id, id).value(new DerivativeStructure(1, 1, 0, 3)).getPartialDerivative(1), EPS);

        UnivariateDifferentiableFunction c = new Constant(4);
        Assert.assertEquals(0, FunctionUtils.compose(id, c).value(new DerivativeStructure(1, 1, 0, 3)).getPartialDerivative(1), EPS);
        Assert.assertEquals(0, FunctionUtils.compose(c, id).value(new DerivativeStructure(1, 1, 0, 3)).getPartialDerivative(1), EPS);

        UnivariateDifferentiableFunction m = new Minus();
        Assert.assertEquals(-1, FunctionUtils.compose(m).value(new DerivativeStructure(1, 1, 0, 3)).getPartialDerivative(1), EPS);
        Assert.assertEquals(1, FunctionUtils.compose(m, m).value(new DerivativeStructure(1, 1, 0, 3)).getPartialDerivative(1), EPS);

        UnivariateDifferentiableFunction inv = new Inverse();
        Assert.assertEquals(0.25, FunctionUtils.compose(inv, m, id).value(new DerivativeStructure(1, 1, 0, 2)).getPartialDerivative(1), EPS);

        UnivariateDifferentiableFunction pow = new Power(2);
        Assert.assertEquals(108, FunctionUtils.compose(pow, pow).value(new DerivativeStructure(1, 1, 0, 3)).getPartialDerivative(1), EPS);

        UnivariateDifferentiableFunction log = new Log();
        double a = 9876.54321;
        Assert.assertEquals(pow.value(new DerivativeStructure(1, 1, 0, a)).getPartialDerivative(1) / pow.value(a),
                            FunctionUtils.compose(log, pow).value(new DerivativeStructure(1, 1, 0, a)).getPartialDerivative(1), EPS);
    }

// org.apache.commons.math3.analysis.FunctionUtilsTest::testAdd
    public void testAdd() {
        UnivariateFunction id = new Identity();
        UnivariateFunction c = new Constant(4);
        UnivariateFunction m = new Minus();
        UnivariateFunction inv = new Inverse();

        Assert.assertEquals(4.5, FunctionUtils.add(inv, m, c, id).value(2), EPS);
        Assert.assertEquals(4 + 2, FunctionUtils.add(c, id).value(2), EPS);
        Assert.assertEquals(4 - 2, FunctionUtils.add(c, FunctionUtils.compose(m, id)).value(2), EPS);
    }

// org.apache.commons.math3.analysis.FunctionUtilsTest::testAddDifferentiable
    public void testAddDifferentiable() {
        UnivariateDifferentiableFunction sin = new Sin();
        UnivariateDifferentiableFunction c = new Constant(4);
        UnivariateDifferentiableFunction m = new Minus();
        UnivariateDifferentiableFunction inv = new Inverse();

        final double a = 123.456;
        Assert.assertEquals(- 1 / (a * a) -1 + Math.cos(a),
                            FunctionUtils.add(inv, m, c, sin).value(new DerivativeStructure(1, 1, 0, a)).getPartialDerivative(1),
                            EPS);
    }

// org.apache.commons.math3.analysis.FunctionUtilsTest::testMultiply
    public void testMultiply() {
        UnivariateFunction c = new Constant(4);
        Assert.assertEquals(16, FunctionUtils.multiply(c, c).value(12345), EPS);

        UnivariateFunction inv = new Inverse();
        UnivariateFunction pow = new Power(2);
        Assert.assertEquals(1, FunctionUtils.multiply(FunctionUtils.compose(inv, pow), pow).value(3.5), EPS);
    }

// org.apache.commons.math3.analysis.FunctionUtilsTest::testMultiplyDifferentiable
    public void testMultiplyDifferentiable() {
        UnivariateDifferentiableFunction c = new Constant(4);
        UnivariateDifferentiableFunction id = new Identity();
        final double a = 1.2345678;
        Assert.assertEquals(8 * a, FunctionUtils.multiply(c, id, id).value(new DerivativeStructure(1, 1, 0, a)).getPartialDerivative(1), EPS);

        UnivariateDifferentiableFunction inv = new Inverse();
        UnivariateDifferentiableFunction pow = new Power(2.5);
        UnivariateDifferentiableFunction cos = new Cos();
        Assert.assertEquals(1.5 * Math.sqrt(a) * Math.cos(a) - Math.pow(a, 1.5) * Math.sin(a),
                            FunctionUtils.multiply(inv, pow, cos).value(new DerivativeStructure(1, 1, 0, a)).getPartialDerivative(1), EPS);

        UnivariateDifferentiableFunction cosh = new Cosh();
        Assert.assertEquals(1.5 * Math.sqrt(a) * Math.cosh(a) + Math.pow(a, 1.5) * Math.sinh(a),
                            FunctionUtils.multiply(inv, pow, cosh).value(new DerivativeStructure(1, 1, 0, a)).getPartialDerivative(1), 8 * EPS);
    }

// org.apache.commons.math3.analysis.FunctionUtilsTest::testCombine
    public void testCombine() {
        BivariateFunction bi = new Add();
        UnivariateFunction id = new Identity();
        UnivariateFunction m = new Minus();
        UnivariateFunction c = FunctionUtils.combine(bi, id, m);
        Assert.assertEquals(0, c.value(2.3456), EPS);

        bi = new Multiply();
        UnivariateFunction inv = new Inverse();
        c = FunctionUtils.combine(bi, id, inv);
        Assert.assertEquals(1, c.value(2.3456), EPS);
    }

// org.apache.commons.math3.analysis.FunctionUtilsTest::testCollector
    public void testCollector() {
        BivariateFunction bi = new Add();
        MultivariateFunction coll = FunctionUtils.collector(bi, 0);
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

// org.apache.commons.math3.analysis.FunctionUtilsTest::testSinc
    public void testSinc() {
        BivariateFunction div = new Divide();
        UnivariateFunction sin = new Sin();
        UnivariateFunction id = new Identity();
        UnivariateFunction sinc1 = FunctionUtils.combine(div, sin, id);
        UnivariateFunction sinc2 = new Sinc();

        for (int i = 0; i < 10; i++) {
            double x = Math.random();
            Assert.assertEquals(sinc1.value(x), sinc2.value(x), EPS);
        }
    }

// org.apache.commons.math3.analysis.FunctionUtilsTest::testFixingArguments
    public void testFixingArguments() {
        UnivariateFunction scaler = FunctionUtils.fix1stArgument(new Multiply(), 10);
        Assert.assertEquals(1.23456, scaler.value(0.123456), EPS);

        UnivariateFunction pow1 = new Power(2);
        UnivariateFunction pow2 = FunctionUtils.fix2ndArgument(new Pow(), 2);

        for (int i = 0; i < 10; i++) {
            double x = Math.random() * 10;
            Assert.assertEquals(pow1.value(x), pow2.value(x), 0);
        }
    }

// org.apache.commons.math3.analysis.FunctionUtilsTest::testSampleWrongBounds
    public void testSampleWrongBounds(){
        FunctionUtils.sample(new Sin(), Math.PI, 0.0, 10);
    }

// org.apache.commons.math3.analysis.FunctionUtilsTest::testSampleNegativeNumberOfPoints
    public void testSampleNegativeNumberOfPoints(){
        FunctionUtils.sample(new Sin(), 0.0, Math.PI, -1);
    }

// org.apache.commons.math3.analysis.FunctionUtilsTest::testSampleNullNumberOfPoints
    public void testSampleNullNumberOfPoints(){
        FunctionUtils.sample(new Sin(), 0.0, Math.PI, 0);
    }

// org.apache.commons.math3.analysis.FunctionUtilsTest::testSample
    public void testSample() {
        final int n = 11;
        final double min = 0.0;
        final double max = Math.PI;
        final double[] actual = FunctionUtils.sample(new Sin(), min, max, n);
        for (int i = 0; i < n; i++) {
            final double x = min + (max - min) / n * i;
            Assert.assertEquals("x = " + x, FastMath.sin(x), actual[i], 0.0);
        }
    }

// org.apache.commons.math3.analysis.FunctionUtilsTest::testToDifferentiableUnivariateFunction
    public void testToDifferentiableUnivariateFunction() {

        
        Sin sin = new Sin();
        DifferentiableUnivariateFunction converted = FunctionUtils.toDifferentiableUnivariateFunction(sin);
        for (double x = 0.1; x < 0.5; x += 0.01) {
            Assert.assertEquals(sin.value(x), converted.value(x), 1.0e-10);
            Assert.assertEquals(sin.derivative().value(x), converted.derivative().value(x), 1.0e-10);
        }

    }

// org.apache.commons.math3.analysis.FunctionUtilsTest::testToUnivariateDifferential
    public void testToUnivariateDifferential() {

        
        Sin sin = new Sin();
        UnivariateDifferentiableFunction converted = FunctionUtils.toUnivariateDifferential(sin);
        for (double x = 0.1; x < 0.5; x += 0.01) {
            DerivativeStructure t = new DerivativeStructure(2, 1, x, 1.0, 2.0);
            Assert.assertEquals(sin.value(t).getValue(), converted.value(t).getValue(), 1.0e-10);
            Assert.assertEquals(sin.value(t).getPartialDerivative(1, 0),
                                converted.value(t).getPartialDerivative(1, 0),
                                1.0e-10);
            Assert.assertEquals(sin.value(t).getPartialDerivative(0, 1),
                                converted.value(t).getPartialDerivative(0, 1),
                                1.0e-10);
        }

    }

// org.apache.commons.math3.analysis.FunctionUtilsTest::testToDifferentiableMultivariateFunction
    public void testToDifferentiableMultivariateFunction() {

        MultivariateDifferentiableFunction hypot = new MultivariateDifferentiableFunction() {
            
            public double value(double[] point) {
                return FastMath.hypot(point[0], point[1]);
            }
            
            public DerivativeStructure value(DerivativeStructure[] point) {
                return DerivativeStructure.hypot(point[0], point[1]);
            }
        };

        DifferentiableMultivariateFunction converted = FunctionUtils.toDifferentiableMultivariateFunction(hypot);
        for (double x = 0.1; x < 0.5; x += 0.01) {
            for (double y = 0.1; y < 0.5; y += 0.01) {
                double[] point = new double[] { x, y };
                Assert.assertEquals(hypot.value(point), converted.value(point), 1.0e-10);
                Assert.assertEquals(x / hypot.value(point), converted.gradient().value(point)[0], 1.0e-10);
                Assert.assertEquals(y / hypot.value(point), converted.gradient().value(point)[1], 1.0e-10);
            }
        }

    }

// org.apache.commons.math3.analysis.FunctionUtilsTest::testToMultivariateDifferentiableFunction
    public void testToMultivariateDifferentiableFunction() {

        DifferentiableMultivariateFunction hypot = new DifferentiableMultivariateFunction() {
            
            public double value(double[] point) {
                return FastMath.hypot(point[0], point[1]);
            }

            public MultivariateFunction partialDerivative(final int k) {
                return new MultivariateFunction() {
                    public double value(double[] point) {
                        return point[k] / FastMath.hypot(point[0], point[1]);
                    }
                };
            }

            public MultivariateVectorFunction gradient() {
                return new MultivariateVectorFunction() {
                    public double[] value(double[] point) {
                        final double h = FastMath.hypot(point[0], point[1]);
                        return new double[] { point[0] / h, point[1] / h };
                    }
                };
            }
            
        };

        MultivariateDifferentiableFunction converted = FunctionUtils.toMultivariateDifferentiableFunction(hypot);
        for (double x = 0.1; x < 0.5; x += 0.01) {
            for (double y = 0.1; y < 0.5; y += 0.01) {
                DerivativeStructure[] t = new DerivativeStructure[] {
                    new DerivativeStructure(3, 1, x, 1.0, 2.0, 3.0 ),
                    new DerivativeStructure(3, 1, y, 4.0, 5.0, 6.0 )
                };
                DerivativeStructure h = DerivativeStructure.hypot(t[0], t[1]);
                Assert.assertEquals(h.getValue(), converted.value(t).getValue(), 1.0e-10);
                Assert.assertEquals(h.getPartialDerivative(1, 0, 0),
                                    converted.value(t).getPartialDerivative(1, 0, 0),
                                    1.0e-10);
                Assert.assertEquals(h.getPartialDerivative(0, 1, 0),
                                    converted.value(t).getPartialDerivative(0, 1, 0),
                                    1.0e-10);
                Assert.assertEquals(h.getPartialDerivative(0, 0, 1),
                                    converted.value(t).getPartialDerivative(0, 0, 1),
                                    1.0e-10);
            }
        }
    }

// org.apache.commons.math3.analysis.differentiation.DSCompilerTest::testSize
    public void testSize() {
        for (int i = 0; i < 6; ++i) {
            for (int j = 0; j < 6; ++j) {
                long expected = ArithmeticUtils.binomialCoefficient(i + j, i);
                Assert.assertEquals(expected, DSCompiler.getCompiler(i, j).getSize());
                Assert.assertEquals(expected, DSCompiler.getCompiler(j, i).getSize());
            }
        }
    }

// org.apache.commons.math3.analysis.differentiation.DSCompilerTest::testIndices
    public void testIndices() {

        DSCompiler c = DSCompiler.getCompiler(0, 0);
        checkIndices(c.getPartialDerivativeOrders(0), new int[0]);

        c = DSCompiler.getCompiler(0, 1);
        checkIndices(c.getPartialDerivativeOrders(0), new int[0]);

        c = DSCompiler.getCompiler(1, 0);
        checkIndices(c.getPartialDerivativeOrders(0), 0);

        c = DSCompiler.getCompiler(1, 1);
        checkIndices(c.getPartialDerivativeOrders(0), 0);
        checkIndices(c.getPartialDerivativeOrders(1), 1);

        c = DSCompiler.getCompiler(1, 2);
        checkIndices(c.getPartialDerivativeOrders(0), 0);
        checkIndices(c.getPartialDerivativeOrders(1), 1);
        checkIndices(c.getPartialDerivativeOrders(2), 2);

        c = DSCompiler.getCompiler(2, 1);
        checkIndices(c.getPartialDerivativeOrders(0), 0, 0);
        checkIndices(c.getPartialDerivativeOrders(1), 1, 0);
        checkIndices(c.getPartialDerivativeOrders(2), 0, 1);

        c = DSCompiler.getCompiler(1, 3);
        checkIndices(c.getPartialDerivativeOrders(0), 0);
        checkIndices(c.getPartialDerivativeOrders(1), 1);
        checkIndices(c.getPartialDerivativeOrders(2), 2);
        checkIndices(c.getPartialDerivativeOrders(3), 3);

        c = DSCompiler.getCompiler(2, 2);
        checkIndices(c.getPartialDerivativeOrders(0), 0, 0);
        checkIndices(c.getPartialDerivativeOrders(1), 1, 0);
        checkIndices(c.getPartialDerivativeOrders(2), 2, 0);
        checkIndices(c.getPartialDerivativeOrders(3), 0, 1);
        checkIndices(c.getPartialDerivativeOrders(4), 1, 1);
        checkIndices(c.getPartialDerivativeOrders(5), 0, 2);

        c = DSCompiler.getCompiler(3, 1);
        checkIndices(c.getPartialDerivativeOrders(0), 0, 0, 0);
        checkIndices(c.getPartialDerivativeOrders(1), 1, 0, 0);
        checkIndices(c.getPartialDerivativeOrders(2), 0, 1, 0);
        checkIndices(c.getPartialDerivativeOrders(3), 0, 0, 1);

        c = DSCompiler.getCompiler(1, 4);
        checkIndices(c.getPartialDerivativeOrders(0), 0);
        checkIndices(c.getPartialDerivativeOrders(1), 1);
        checkIndices(c.getPartialDerivativeOrders(2), 2);
        checkIndices(c.getPartialDerivativeOrders(3), 3);
        checkIndices(c.getPartialDerivativeOrders(4), 4);

        c = DSCompiler.getCompiler(2, 3);
        checkIndices(c.getPartialDerivativeOrders(0), 0, 0);
        checkIndices(c.getPartialDerivativeOrders(1), 1, 0);
        checkIndices(c.getPartialDerivativeOrders(2), 2, 0);
        checkIndices(c.getPartialDerivativeOrders(3), 3, 0);
        checkIndices(c.getPartialDerivativeOrders(4), 0, 1);
        checkIndices(c.getPartialDerivativeOrders(5), 1, 1);
        checkIndices(c.getPartialDerivativeOrders(6), 2, 1);
        checkIndices(c.getPartialDerivativeOrders(7), 0, 2);
        checkIndices(c.getPartialDerivativeOrders(8), 1, 2);
        checkIndices(c.getPartialDerivativeOrders(9), 0, 3);

        c = DSCompiler.getCompiler(3, 2);
        checkIndices(c.getPartialDerivativeOrders(0), 0, 0, 0);
        checkIndices(c.getPartialDerivativeOrders(1), 1, 0, 0);
        checkIndices(c.getPartialDerivativeOrders(2), 2, 0, 0);
        checkIndices(c.getPartialDerivativeOrders(3), 0, 1, 0);
        checkIndices(c.getPartialDerivativeOrders(4), 1, 1, 0);
        checkIndices(c.getPartialDerivativeOrders(5), 0, 2, 0);
        checkIndices(c.getPartialDerivativeOrders(6), 0, 0, 1);
        checkIndices(c.getPartialDerivativeOrders(7), 1, 0, 1);
        checkIndices(c.getPartialDerivativeOrders(8), 0, 1, 1);
        checkIndices(c.getPartialDerivativeOrders(9), 0, 0, 2);

        c = DSCompiler.getCompiler(4, 1);
        checkIndices(c.getPartialDerivativeOrders(0), 0, 0, 0, 0);
        checkIndices(c.getPartialDerivativeOrders(1), 1, 0, 0, 0);
        checkIndices(c.getPartialDerivativeOrders(2), 0, 1, 0, 0);
        checkIndices(c.getPartialDerivativeOrders(3), 0, 0, 1, 0);
        checkIndices(c.getPartialDerivativeOrders(4), 0, 0, 0, 1);

    }

// org.apache.commons.math3.analysis.differentiation.DSCompilerTest::testIncompatibleParams
    public void testIncompatibleParams() {
        DSCompiler.getCompiler(3, 2).checkCompatibility(DSCompiler.getCompiler(4, 2));
    }

// org.apache.commons.math3.analysis.differentiation.DSCompilerTest::testIncompatibleOrder
    public void testIncompatibleOrder() {
        DSCompiler.getCompiler(3, 3).checkCompatibility(DSCompiler.getCompiler(3, 2));
    }

// org.apache.commons.math3.analysis.differentiation.DSCompilerTest::testSymmetry
    public void testSymmetry() {
        for (int i = 0; i < 6; ++i) {
            for (int j = 0; j < 6; ++j) {
                DSCompiler c = DSCompiler.getCompiler(i, j);
                for (int k = 0; k < c.getSize(); ++k) {
                    Assert.assertEquals(k, c.getPartialDerivativeIndex(c.getPartialDerivativeOrders(k)));
                }
            }
        }
    }

// org.apache.commons.math3.analysis.differentiation.DSCompilerTest::testMultiplicationRules
    @Test public void testMultiplicationRules()
        throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {

        Map<String,String> referenceRules = new HashMap<String, String>();
        referenceRules.put("(f*g)",          "f * g");
        referenceRules.put("d(f*g)/dx",      "f * dg/dx + df/dx * g");
        referenceRules.put("d(f*g)/dy",      referenceRules.get("d(f*g)/dx").replaceAll("x", "y"));
        referenceRules.put("d(f*g)/dz",      referenceRules.get("d(f*g)/dx").replaceAll("x", "z"));
        referenceRules.put("d(f*g)/dt",      referenceRules.get("d(f*g)/dx").replaceAll("x", "t"));
        referenceRules.put("d2(f*g)/dx2",    "f * d2g/dx2 + 2 * df/dx * dg/dx + d2f/dx2 * g");
        referenceRules.put("d2(f*g)/dy2",    referenceRules.get("d2(f*g)/dx2").replaceAll("x", "y"));
        referenceRules.put("d2(f*g)/dz2",    referenceRules.get("d2(f*g)/dx2").replaceAll("x", "z"));
        referenceRules.put("d2(f*g)/dt2",    referenceRules.get("d2(f*g)/dx2").replaceAll("x", "t"));
        referenceRules.put("d2(f*g)/dxdy",   "f * d2g/dxdy + df/dy * dg/dx + df/dx * dg/dy + d2f/dxdy * g");
        referenceRules.put("d2(f*g)/dxdz",   referenceRules.get("d2(f*g)/dxdy").replaceAll("y", "z"));
        referenceRules.put("d2(f*g)/dxdt",   referenceRules.get("d2(f*g)/dxdy").replaceAll("y", "t"));
        referenceRules.put("d2(f*g)/dydz",   referenceRules.get("d2(f*g)/dxdz").replaceAll("x", "y"));
        referenceRules.put("d2(f*g)/dydt",   referenceRules.get("d2(f*g)/dxdt").replaceAll("x", "y"));
        referenceRules.put("d2(f*g)/dzdt",   referenceRules.get("d2(f*g)/dxdt").replaceAll("x", "z"));
        referenceRules.put("d3(f*g)/dx3",    "f * d3g/dx3 +" +
                                             " 3 * df/dx * d2g/dx2 +" +
                                             " 3 * d2f/dx2 * dg/dx +" +
                                             " d3f/dx3 * g");
        referenceRules.put("d3(f*g)/dy3",   referenceRules.get("d3(f*g)/dx3").replaceAll("x", "y"));
        referenceRules.put("d3(f*g)/dz3",   referenceRules.get("d3(f*g)/dx3").replaceAll("x", "z"));
        referenceRules.put("d3(f*g)/dt3",   referenceRules.get("d3(f*g)/dx3").replaceAll("x", "t"));
        referenceRules.put("d3(f*g)/dx2dy",  "f * d3g/dx2dy +" +
                                             " df/dy * d2g/dx2 +" +
                                             " 2 * df/dx * d2g/dxdy +" +
                                             " 2 * d2f/dxdy * dg/dx +" +
                                             " d2f/dx2 * dg/dy +" +
                                             " d3f/dx2dy * g");
        referenceRules.put("d3(f*g)/dxdy2",  "f * d3g/dxdy2 +" +
                                             " 2 * df/dy * d2g/dxdy +" +
                                             " d2f/dy2 * dg/dx +" +
                                             " df/dx * d2g/dy2 +" +
                                             " 2 * d2f/dxdy * dg/dy +" +
                                             " d3f/dxdy2 * g");
        referenceRules.put("d3(f*g)/dx2dz",   referenceRules.get("d3(f*g)/dx2dy").replaceAll("y", "z"));
        referenceRules.put("d3(f*g)/dy2dz",   referenceRules.get("d3(f*g)/dx2dz").replaceAll("x", "y"));
        referenceRules.put("d3(f*g)/dxdz2",   referenceRules.get("d3(f*g)/dxdy2").replaceAll("y", "z"));
        referenceRules.put("d3(f*g)/dydz2",   referenceRules.get("d3(f*g)/dxdz2").replaceAll("x", "y"));
        referenceRules.put("d3(f*g)/dx2dt",   referenceRules.get("d3(f*g)/dx2dz").replaceAll("z", "t"));
        referenceRules.put("d3(f*g)/dy2dt",   referenceRules.get("d3(f*g)/dx2dt").replaceAll("x", "y"));
        referenceRules.put("d3(f*g)/dz2dt",   referenceRules.get("d3(f*g)/dx2dt").replaceAll("x", "z"));
        referenceRules.put("d3(f*g)/dxdt2",   referenceRules.get("d3(f*g)/dxdy2").replaceAll("y", "t"));
        referenceRules.put("d3(f*g)/dydt2",   referenceRules.get("d3(f*g)/dxdt2").replaceAll("x", "y"));
        referenceRules.put("d3(f*g)/dzdt2",   referenceRules.get("d3(f*g)/dxdt2").replaceAll("x", "z"));
        referenceRules.put("d3(f*g)/dxdydz", "f * d3g/dxdydz +" +
                                             " df/dz * d2g/dxdy +" +
                                             " df/dy * d2g/dxdz +" +
                                             " d2f/dydz * dg/dx +" +
                                             " df/dx * d2g/dydz +" +
                                             " d2f/dxdz * dg/dy +" +
                                             " d2f/dxdy * dg/dz +" +
                                             " d3f/dxdydz * g");
        referenceRules.put("d3(f*g)/dxdydt", referenceRules.get("d3(f*g)/dxdydz").replaceAll("z", "t"));
        referenceRules.put("d3(f*g)/dxdzdt", referenceRules.get("d3(f*g)/dxdydt").replaceAll("y", "z"));
        referenceRules.put("d3(f*g)/dydzdt", referenceRules.get("d3(f*g)/dxdzdt").replaceAll("x", "y"));

        Field multFieldArrayField = DSCompiler.class.getDeclaredField("multIndirection");
        multFieldArrayField.setAccessible(true);
        for (int i = 0; i < 5; ++i) {
            for (int j = 0; j < 4; ++j) {
                DSCompiler compiler = DSCompiler.getCompiler(i, j);
                int[][][] multIndirection = (int[][][]) multFieldArrayField.get(compiler);
                for (int k = 0; k < multIndirection.length; ++k) {
                    String product = ordersToString(compiler.getPartialDerivativeOrders(k),
                                                    "(f*g)", "x", "y", "z", "t");
                    StringBuilder rule = new StringBuilder();
                    for (int[] term : multIndirection[k]) {
                        if (rule.length() > 0) {
                            rule.append(" + ");
                        }
                        if (term[0] > 1) {
                            rule.append(term[0]).append(" * ");
                        }
                        rule.append(ordersToString(compiler.getPartialDerivativeOrders(term[1]),
                                                   "f", "x", "y", "z", "t"));
                        rule.append(" * ");
                        rule.append(ordersToString(compiler.getPartialDerivativeOrders(term[2]),
                                                   "g", "x", "y", "z", "t"));
                    }
                    Assert.assertEquals(product, referenceRules.get(product), rule.toString());
                }
            }
        }
    }

// org.apache.commons.math3.analysis.differentiation.DSCompilerTest::testCompositionRules
    @Test public void testCompositionRules()
        throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {

        
        
        Map<String,String> referenceRules = new HashMap<String, String>();
        referenceRules.put("(f(g))",              "(f(g))");
        referenceRules.put("d(f(g))/dx",          "d(f(g))/dg * dg/dx");
        referenceRules.put("d(f(g))/dy",          referenceRules.get("d(f(g))/dx").replaceAll("x", "y"));
        referenceRules.put("d(f(g))/dz",          referenceRules.get("d(f(g))/dx").replaceAll("x", "z"));
        referenceRules.put("d(f(g))/dt",          referenceRules.get("d(f(g))/dx").replaceAll("x", "t"));
        referenceRules.put("d2(f(g))/dx2",        "d2(f(g))/dg2 * dg/dx * dg/dx + d(f(g))/dg * d2g/dx2");
        referenceRules.put("d2(f(g))/dy2",        referenceRules.get("d2(f(g))/dx2").replaceAll("x", "y"));
        referenceRules.put("d2(f(g))/dz2",        referenceRules.get("d2(f(g))/dx2").replaceAll("x", "z"));
        referenceRules.put("d2(f(g))/dt2",        referenceRules.get("d2(f(g))/dx2").replaceAll("x", "t"));
        referenceRules.put("d2(f(g))/dxdy",       "d2(f(g))/dg2 * dg/dx * dg/dy + d(f(g))/dg * d2g/dxdy");
        referenceRules.put("d2(f(g))/dxdz",       referenceRules.get("d2(f(g))/dxdy").replaceAll("y", "z"));
        referenceRules.put("d2(f(g))/dxdt",       referenceRules.get("d2(f(g))/dxdy").replaceAll("y", "t"));
        referenceRules.put("d2(f(g))/dydz",       referenceRules.get("d2(f(g))/dxdz").replaceAll("x", "y"));
        referenceRules.put("d2(f(g))/dydt",       referenceRules.get("d2(f(g))/dxdt").replaceAll("x", "y"));
        referenceRules.put("d2(f(g))/dzdt",       referenceRules.get("d2(f(g))/dxdt").replaceAll("x", "z"));
        referenceRules.put("d3(f(g))/dx3",        "d3(f(g))/dg3 * dg/dx * dg/dx * dg/dx +" +
                                                  " 3 * d2(f(g))/dg2 * dg/dx * d2g/dx2 +" +
                                                  " d(f(g))/dg * d3g/dx3");
        referenceRules.put("d3(f(g))/dy3",        referenceRules.get("d3(f(g))/dx3").replaceAll("x", "y"));
        referenceRules.put("d3(f(g))/dz3",        referenceRules.get("d3(f(g))/dx3").replaceAll("x", "z"));
        referenceRules.put("d3(f(g))/dt3",        referenceRules.get("d3(f(g))/dx3").replaceAll("x", "t"));
        referenceRules.put("d3(f(g))/dxdy2",      "d3(f(g))/dg3 * dg/dx * dg/dy * dg/dy +" +
                                                  " 2 * d2(f(g))/dg2 * dg/dy * d2g/dxdy +" +
                                                  " d2(f(g))/dg2 * dg/dx * d2g/dy2 +" +
                                                  " d(f(g))/dg * d3g/dxdy2");
        referenceRules.put("d3(f(g))/dxdz2",      referenceRules.get("d3(f(g))/dxdy2").replaceAll("y", "z"));
        referenceRules.put("d3(f(g))/dxdt2",      referenceRules.get("d3(f(g))/dxdy2").replaceAll("y", "t"));
        referenceRules.put("d3(f(g))/dydz2",      referenceRules.get("d3(f(g))/dxdz2").replaceAll("x", "y"));
        referenceRules.put("d3(f(g))/dydt2",      referenceRules.get("d3(f(g))/dxdt2").replaceAll("x", "y"));
        referenceRules.put("d3(f(g))/dzdt2",      referenceRules.get("d3(f(g))/dxdt2").replaceAll("x", "z"));
        referenceRules.put("d3(f(g))/dx2dy",      "d3(f(g))/dg3 * dg/dx * dg/dx * dg/dy +" +
                                                  " 2 * d2(f(g))/dg2 * dg/dx * d2g/dxdy +" +
                                                  " d2(f(g))/dg2 * d2g/dx2 * dg/dy +" +
                                                  " d(f(g))/dg * d3g/dx2dy");
        referenceRules.put("d3(f(g))/dx2dz",      referenceRules.get("d3(f(g))/dx2dy").replaceAll("y", "z"));
        referenceRules.put("d3(f(g))/dx2dt",      referenceRules.get("d3(f(g))/dx2dy").replaceAll("y", "t"));
        referenceRules.put("d3(f(g))/dy2dz",      referenceRules.get("d3(f(g))/dx2dz").replaceAll("x", "y"));
        referenceRules.put("d3(f(g))/dy2dt",      referenceRules.get("d3(f(g))/dx2dt").replaceAll("x", "y"));
        referenceRules.put("d3(f(g))/dz2dt",      referenceRules.get("d3(f(g))/dx2dt").replaceAll("x", "z"));
        referenceRules.put("d3(f(g))/dxdydz",     "d3(f(g))/dg3 * dg/dx * dg/dy * dg/dz +" +
                                                  " d2(f(g))/dg2 * dg/dy * d2g/dxdz +" +
                                                  " d2(f(g))/dg2 * dg/dx * d2g/dydz +" +
                                                  " d2(f(g))/dg2 * d2g/dxdy * dg/dz +" +
                                                  " d(f(g))/dg * d3g/dxdydz");
        referenceRules.put("d3(f(g))/dxdydt",     referenceRules.get("d3(f(g))/dxdydz").replaceAll("z", "t"));
        referenceRules.put("d3(f(g))/dxdzdt",     referenceRules.get("d3(f(g))/dxdydt").replaceAll("y", "z"));
        referenceRules.put("d3(f(g))/dydzdt",     referenceRules.get("d3(f(g))/dxdzdt").replaceAll("x", "y"));
        referenceRules.put("d4(f(g))/dx4",        "d4(f(g))/dg4 * dg/dx * dg/dx * dg/dx * dg/dx +" +
                                                  " 6 * d3(f(g))/dg3 * dg/dx * dg/dx * d2g/dx2 +" +
                                                  " 3 * d2(f(g))/dg2 * d2g/dx2 * d2g/dx2 +" +
                                                  " 4 * d2(f(g))/dg2 * dg/dx * d3g/dx3 +" +
                                                  " d(f(g))/dg * d4g/dx4");
        referenceRules.put("d4(f(g))/dy4",        referenceRules.get("d4(f(g))/dx4").replaceAll("x", "y"));
        referenceRules.put("d4(f(g))/dz4",        referenceRules.get("d4(f(g))/dx4").replaceAll("x", "z"));
        referenceRules.put("d4(f(g))/dt4",        referenceRules.get("d4(f(g))/dx4").replaceAll("x", "t"));
        referenceRules.put("d4(f(g))/dx3dy",      "d4(f(g))/dg4 * dg/dx * dg/dx * dg/dx * dg/dy +" +
                                                  " 3 * d3(f(g))/dg3 * dg/dx * dg/dx * d2g/dxdy +" +
                                                  " 3 * d3(f(g))/dg3 * dg/dx * d2g/dx2 * dg/dy +" +
                                                  " 3 * d2(f(g))/dg2 * d2g/dx2 * d2g/dxdy +" +
                                                  " 3 * d2(f(g))/dg2 * dg/dx * d3g/dx2dy +" +
                                                  " d2(f(g))/dg2 * d3g/dx3 * dg/dy +" +
                                                  " d(f(g))/dg * d4g/dx3dy");
        referenceRules.put("d4(f(g))/dx3dz",      referenceRules.get("d4(f(g))/dx3dy").replaceAll("y", "z"));
        referenceRules.put("d4(f(g))/dx3dt",      referenceRules.get("d4(f(g))/dx3dy").replaceAll("y", "t"));
        referenceRules.put("d4(f(g))/dxdy3",      "d4(f(g))/dg4 * dg/dx * dg/dy * dg/dy * dg/dy +" +
                                                  " 3 * d3(f(g))/dg3 * dg/dy * dg/dy * d2g/dxdy +" +
                                                  " 3 * d3(f(g))/dg3 * dg/dx * dg/dy * d2g/dy2 +" +
                                                  " 3 * d2(f(g))/dg2 * d2g/dxdy * d2g/dy2 +" +
                                                  " 3 * d2(f(g))/dg2 * dg/dy * d3g/dxdy2 +" +
                                                  " d2(f(g))/dg2 * dg/dx * d3g/dy3 +" +
                                                  " d(f(g))/dg * d4g/dxdy3");
        referenceRules.put("d4(f(g))/dxdz3",      referenceRules.get("d4(f(g))/dxdy3").replaceAll("y", "z"));
        referenceRules.put("d4(f(g))/dxdt3",      referenceRules.get("d4(f(g))/dxdy3").replaceAll("y", "t"));
        referenceRules.put("d4(f(g))/dy3dz",      referenceRules.get("d4(f(g))/dx3dz").replaceAll("x", "y"));
        referenceRules.put("d4(f(g))/dy3dt",      referenceRules.get("d4(f(g))/dx3dt").replaceAll("x", "y"));
        referenceRules.put("d4(f(g))/dydz3",      referenceRules.get("d4(f(g))/dxdz3").replaceAll("x", "y"));
        referenceRules.put("d4(f(g))/dydt3",      referenceRules.get("d4(f(g))/dxdt3").replaceAll("x", "y"));
        referenceRules.put("d4(f(g))/dz3dt",      referenceRules.get("d4(f(g))/dx3dt").replaceAll("x", "z"));
        referenceRules.put("d4(f(g))/dzdt3",      referenceRules.get("d4(f(g))/dxdt3").replaceAll("x", "z"));
        referenceRules.put("d4(f(g))/dx2dy2",     "d4(f(g))/dg4 * dg/dx * dg/dx * dg/dy * dg/dy +" +
                                                  " 4 * d3(f(g))/dg3 * dg/dx * dg/dy * d2g/dxdy +" +
                                                  " d3(f(g))/dg3 * dg/dx * dg/dx * d2g/dy2 +" +
                                                  " 2 * d2(f(g))/dg2 * d2g/dxdy * d2g/dxdy +" +
                                                  " 2 * d2(f(g))/dg2 * dg/dx * d3g/dxdy2 +" +
                                                  " d3(f(g))/dg3 * d2g/dx2 * dg/dy * dg/dy +" +
                                                  " 2 * d2(f(g))/dg2 * dg/dy * d3g/dx2dy +" +
                                                  " d2(f(g))/dg2 * d2g/dx2 * d2g/dy2 +" +
                                                  " d(f(g))/dg * d4g/dx2dy2");
        referenceRules.put("d4(f(g))/dx2dz2",     referenceRules.get("d4(f(g))/dx2dy2").replaceAll("y", "z"));
        referenceRules.put("d4(f(g))/dx2dt2",     referenceRules.get("d4(f(g))/dx2dy2").replaceAll("y", "t"));
        referenceRules.put("d4(f(g))/dy2dz2",     referenceRules.get("d4(f(g))/dx2dz2").replaceAll("x", "y"));
        referenceRules.put("d4(f(g))/dy2dt2",     referenceRules.get("d4(f(g))/dx2dt2").replaceAll("x", "y"));
        referenceRules.put("d4(f(g))/dz2dt2",     referenceRules.get("d4(f(g))/dx2dt2").replaceAll("x", "z"));

        referenceRules.put("d4(f(g))/dx2dydz",    "d4(f(g))/dg4 * dg/dx * dg/dx * dg/dy * dg/dz +" +
                                                  " 2 * d3(f(g))/dg3 * dg/dx * dg/dy * d2g/dxdz +" +
                                                  " d3(f(g))/dg3 * dg/dx * dg/dx * d2g/dydz +" +
                                                  " 2 * d3(f(g))/dg3 * dg/dx * d2g/dxdy * dg/dz +" +
                                                  " 2 * d2(f(g))/dg2 * d2g/dxdy * d2g/dxdz +" +
                                                  " 2 * d2(f(g))/dg2 * dg/dx * d3g/dxdydz +" +
                                                  " d3(f(g))/dg3 * d2g/dx2 * dg/dy * dg/dz +" +
                                                  " d2(f(g))/dg2 * dg/dy * d3g/dx2dz +" +
                                                  " d2(f(g))/dg2 * d2g/dx2 * d2g/dydz +" +
                                                  " d2(f(g))/dg2 * d3g/dx2dy * dg/dz +" +
                                                  " d(f(g))/dg * d4g/dx2dydz");
        referenceRules.put("d4(f(g))/dx2dydt",    referenceRules.get("d4(f(g))/dx2dydz").replaceAll("z", "t"));
        referenceRules.put("d4(f(g))/dx2dzdt",    referenceRules.get("d4(f(g))/dx2dydt").replaceAll("y", "z"));
        referenceRules.put("d4(f(g))/dxdy2dz",    "d4(f(g))/dg4 * dg/dx * dg/dy * dg/dy * dg/dz +" +
                                                  " d3(f(g))/dg3 * dg/dy * dg/dy * d2g/dxdz +" +
                                                  " 2 * d3(f(g))/dg3 * dg/dx * dg/dy * d2g/dydz +" +
                                                  " 2 * d3(f(g))/dg3 * dg/dy * d2g/dxdy * dg/dz +" +
                                                  " 2 * d2(f(g))/dg2 * d2g/dxdy * d2g/dydz +" +
                                                  " 2 * d2(f(g))/dg2 * dg/dy * d3g/dxdydz +" +
                                                  " d3(f(g))/dg3 * dg/dx * d2g/dy2 * dg/dz +" +
                                                  " d2(f(g))/dg2 * d2g/dy2 * d2g/dxdz +" +
                                                  " d2(f(g))/dg2 * dg/dx * d3g/dy2dz +" +
                                                  " d2(f(g))/dg2 * d3g/dxdy2 * dg/dz +" +
                                                  " d(f(g))/dg * d4g/dxdy2dz");
        referenceRules.put("d4(f(g))/dxdy2dt",    referenceRules.get("d4(f(g))/dxdy2dz").replaceAll("z", "t"));
        referenceRules.put("d4(f(g))/dy2dzdt",    referenceRules.get("d4(f(g))/dx2dzdt").replaceAll("x", "y"));
        referenceRules.put("d4(f(g))/dxdydz2",    "d4(f(g))/dg4 * dg/dx * dg/dy * dg/dz * dg/dz +" +
                                                  " 2 * d3(f(g))/dg3 * dg/dy * dg/dz * d2g/dxdz +" +
                                                  " 2 * d3(f(g))/dg3 * dg/dx * dg/dz * d2g/dydz +" +
                                                  " d3(f(g))/dg3 * dg/dx * dg/dy * d2g/dz2 +" +
                                                  " 2 * d2(f(g))/dg2 * d2g/dxdz * d2g/dydz +" +
                                                  " d2(f(g))/dg2 * dg/dy * d3g/dxdz2 +" +
                                                  " d2(f(g))/dg2 * dg/dx * d3g/dydz2 +" +
                                                  " d3(f(g))/dg3 * d2g/dxdy * dg/dz * dg/dz +" +
                                                  " 2 * d2(f(g))/dg2 * dg/dz * d3g/dxdydz +" +
                                                  " d2(f(g))/dg2 * d2g/dxdy * d2g/dz2 +" +
                                                  " d(f(g))/dg * d4g/dxdydz2");
        referenceRules.put("d4(f(g))/dxdz2dt",    referenceRules.get("d4(f(g))/dxdy2dt").replaceAll("y", "z"));
        referenceRules.put("d4(f(g))/dydz2dt",    referenceRules.get("d4(f(g))/dxdz2dt").replaceAll("x", "y"));
        referenceRules.put("d4(f(g))/dxdydt2",    referenceRules.get("d4(f(g))/dxdydz2").replaceAll("z", "t"));
        referenceRules.put("d4(f(g))/dxdzdt2",    referenceRules.get("d4(f(g))/dxdydt2").replaceAll("y", "z"));
        referenceRules.put("d4(f(g))/dydzdt2",    referenceRules.get("d4(f(g))/dxdzdt2").replaceAll("x", "y"));
        referenceRules.put("d4(f(g))/dxdydzdt",   "d4(f(g))/dg4 * dg/dx * dg/dy * dg/dz * dg/dt +" +
                                                  " d3(f(g))/dg3 * dg/dy * dg/dz * d2g/dxdt +" +
                                                  " d3(f(g))/dg3 * dg/dx * dg/dz * d2g/dydt +" +
                                                  " d3(f(g))/dg3 * dg/dx * dg/dy * d2g/dzdt +" +
                                                  " d3(f(g))/dg3 * dg/dy * d2g/dxdz * dg/dt +" +
                                                  " d2(f(g))/dg2 * d2g/dxdz * d2g/dydt +" +
                                                  " d2(f(g))/dg2 * dg/dy * d3g/dxdzdt +" +
                                                  " d3(f(g))/dg3 * dg/dx * d2g/dydz * dg/dt +" +
                                                  " d2(f(g))/dg2 * d2g/dydz * d2g/dxdt +" +
                                                  " d2(f(g))/dg2 * dg/dx * d3g/dydzdt +" +
                                                  " d3(f(g))/dg3 * d2g/dxdy * dg/dz * dg/dt +" +
                                                  " d2(f(g))/dg2 * dg/dz * d3g/dxdydt +" +
                                                  " d2(f(g))/dg2 * d2g/dxdy * d2g/dzdt +" +
                                                  " d2(f(g))/dg2 * d3g/dxdydz * dg/dt +" +
                                                  " d(f(g))/dg * d4g/dxdydzdt");

        Field compFieldArrayField = DSCompiler.class.getDeclaredField("compIndirection");
        compFieldArrayField.setAccessible(true);
        for (int i = 0; i < 5; ++i) {
            for (int j = 0; j < 5; ++j) {
                DSCompiler compiler = DSCompiler.getCompiler(i, j);
                int[][][] compIndirection = (int[][][]) compFieldArrayField.get(compiler);
                for (int k = 0; k < compIndirection.length; ++k) {
                    String product = ordersToString(compiler.getPartialDerivativeOrders(k),
                                                    "(f(g))", "x", "y", "z", "t");
                    StringBuilder rule = new StringBuilder();
                    for (int[] term : compIndirection[k]) {
                        if (rule.length() > 0) {
                            rule.append(" + ");
                        }
                        if (term[0] > 1) {
                            rule.append(term[0]).append(" * ");
                        }
                        rule.append(orderToString(term[1], "(f(g))", "g"));
                        for (int l = 2; l < term.length; ++l) {
                            rule.append(" * ");
                            rule.append(ordersToString(compiler.getPartialDerivativeOrders(term[l]),
                                                       "g", "x", "y", "z", "t"));
                        }
                    }
                    Assert.assertEquals(product, referenceRules.get(product), rule.toString());
                }
            }
        }
    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testWrongVariableIndex
    public void testWrongVariableIndex() {
        new DerivativeStructure(3, 1, 3, 1.0);
    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testMissingOrders
    public void testMissingOrders() {
        new DerivativeStructure(3, 1, 0, 1.0).getPartialDerivative(0, 1);
    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testTooLargeOrder
    public void testTooLargeOrder() {
        new DerivativeStructure(3, 1, 0, 1.0).getPartialDerivative(1, 1, 2);
    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testVariableWithoutDerivative0
    public void testVariableWithoutDerivative0() {
        DerivativeStructure v = new DerivativeStructure(1, 0, 0, 1.0);
        Assert.assertEquals(1.0, v.getValue(), 1.0e-15);
    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testVariableWithoutDerivative1
    public void testVariableWithoutDerivative1() {
        DerivativeStructure v = new DerivativeStructure(1, 0, 0, 1.0);
        Assert.assertEquals(1.0, v.getPartialDerivative(1), 1.0e-15);
    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testVariable
    public void testVariable() {
        for (int maxOrder = 1; maxOrder < 5; ++maxOrder) {
            checkF0F1(new DerivativeStructure(3, maxOrder, 0, 1.0),
                      1.0, 1.0, 0.0, 0.0);
            checkF0F1(new DerivativeStructure(3, maxOrder, 1, 2.0),
                      2.0, 0.0, 1.0, 0.0);
            checkF0F1(new DerivativeStructure(3, maxOrder, 2, 3.0),
                      3.0, 0.0, 0.0, 1.0);
        }
    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testConstant
    public void testConstant() {
        for (int maxOrder = 1; maxOrder < 5; ++maxOrder) {
            checkF0F1(new DerivativeStructure(3, maxOrder, FastMath.PI),
                      FastMath.PI, 0.0, 0.0, 0.0);
        }
    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testPrimitiveAdd
    public void testPrimitiveAdd() {
        for (int maxOrder = 1; maxOrder < 5; ++maxOrder) {
            checkF0F1(new DerivativeStructure(3, maxOrder, 0, 1.0).add(5), 6.0, 1.0, 0.0, 0.0);
            checkF0F1(new DerivativeStructure(3, maxOrder, 1, 2.0).add(5), 7.0, 0.0, 1.0, 0.0);
            checkF0F1(new DerivativeStructure(3, maxOrder, 2, 3.0).add(5), 8.0, 0.0, 0.0, 1.0);
        }
    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testAdd
    public void testAdd() {
        for (int maxOrder = 1; maxOrder < 5; ++maxOrder) {
            DerivativeStructure x = new DerivativeStructure(3, maxOrder, 0, 1.0);
            DerivativeStructure y = new DerivativeStructure(3, maxOrder, 1, 2.0);
            DerivativeStructure z = new DerivativeStructure(3, maxOrder, 2, 3.0);
            DerivativeStructure xyz = x.add(y.add(z));
            checkF0F1(xyz, x.getValue() + y.getValue() + z.getValue(), 1.0, 1.0, 1.0);
        }
    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testPrimitiveSubtract
    public void testPrimitiveSubtract() {
        for (int maxOrder = 1; maxOrder < 5; ++maxOrder) {
            checkF0F1(new DerivativeStructure(3, maxOrder, 0, 1.0).subtract(5), -4.0, 1.0, 0.0, 0.0);
            checkF0F1(new DerivativeStructure(3, maxOrder, 1, 2.0).subtract(5), -3.0, 0.0, 1.0, 0.0);
            checkF0F1(new DerivativeStructure(3, maxOrder, 2, 3.0).subtract(5), -2.0, 0.0, 0.0, 1.0);
        }
    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testSubtract
    public void testSubtract() {
        for (int maxOrder = 1; maxOrder < 5; ++maxOrder) {
            DerivativeStructure x = new DerivativeStructure(3, maxOrder, 0, 1.0);
            DerivativeStructure y = new DerivativeStructure(3, maxOrder, 1, 2.0);
            DerivativeStructure z = new DerivativeStructure(3, maxOrder, 2, 3.0);
            DerivativeStructure xyz = x.subtract(y.subtract(z));
            checkF0F1(xyz, x.getValue() - (y.getValue() - z.getValue()), 1.0, -1.0, 1.0);
        }
    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testPrimitiveMultiply
    public void testPrimitiveMultiply() {
        for (int maxOrder = 1; maxOrder < 5; ++maxOrder) {
            checkF0F1(new DerivativeStructure(3, maxOrder, 0, 1.0).multiply(5),  5.0, 5.0, 0.0, 0.0);
            checkF0F1(new DerivativeStructure(3, maxOrder, 1, 2.0).multiply(5), 10.0, 0.0, 5.0, 0.0);
            checkF0F1(new DerivativeStructure(3, maxOrder, 2, 3.0).multiply(5), 15.0, 0.0, 0.0, 5.0);
        }
    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testMultiply
    public void testMultiply() {
        for (int maxOrder = 1; maxOrder < 5; ++maxOrder) {
            DerivativeStructure x = new DerivativeStructure(3, maxOrder, 0, 1.0);
            DerivativeStructure y = new DerivativeStructure(3, maxOrder, 1, 2.0);
            DerivativeStructure z = new DerivativeStructure(3, maxOrder, 2, 3.0);
            DerivativeStructure xyz = x.multiply(y.multiply(z));
            for (int i = 0; i <= maxOrder; ++i) {
                for (int j = 0; j <= maxOrder; ++j) {
                    for (int k = 0; k <= maxOrder; ++k) {
                        if (i + j + k <= maxOrder) {
                            Assert.assertEquals((i == 0 ? x.getValue() : (i == 1 ? 1.0 : 0.0)) *
                                                (j == 0 ? y.getValue() : (j == 1 ? 1.0 : 0.0)) *
                                                (k == 0 ? z.getValue() : (k == 1 ? 1.0 : 0.0)),
                                                xyz.getPartialDerivative(i, j, k),
                                                1.0e-15);
                        }
                    }
                }
            }
        }
    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testNegate
    public void testNegate() {
        for (int maxOrder = 1; maxOrder < 5; ++maxOrder) {
            checkF0F1(new DerivativeStructure(3, maxOrder, 0, 1.0).negate(), -1.0, -1.0, 0.0, 0.0);
            checkF0F1(new DerivativeStructure(3, maxOrder, 1, 2.0).negate(), -2.0, 0.0, -1.0, 0.0);
            checkF0F1(new DerivativeStructure(3, maxOrder, 2, 3.0).negate(), -3.0, 0.0, 0.0, -1.0);
        }
    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testReciprocal
    public void testReciprocal() {
        for (double x = 0.1; x < 1.2; x += 0.1) {
            DerivativeStructure r = new DerivativeStructure(1, 6, 0, x).reciprocal();
            Assert.assertEquals(1 / x, r.getValue(), 1.0e-15);
            for (int i = 1; i < r.getOrder(); ++i) {
                double expected = ArithmeticUtils.pow(-1, i) * ArithmeticUtils.factorial(i) /
                                  FastMath.pow(x, i + 1);
                Assert.assertEquals(expected, r.getPartialDerivative(i), 1.0e-15 * FastMath.abs(expected));
            }
        }
    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testPow
    public void testPow() {
        for (int maxOrder = 1; maxOrder < 5; ++maxOrder) {
            for (int n = 0; n < 10; ++n) {

                DerivativeStructure x = new DerivativeStructure(3, maxOrder, 0, 1.0);
                DerivativeStructure y = new DerivativeStructure(3, maxOrder, 1, 2.0);
                DerivativeStructure z = new DerivativeStructure(3, maxOrder, 2, 3.0);
                List<DerivativeStructure> list = Arrays.asList(x, y, z,
                                                               x.add(y).add(z),
                                                               x.multiply(y).multiply(z));

                if (n == 0) {
                    for (DerivativeStructure ds : list) {
                        checkEquals(ds.getField().getOne(), ds.pow(n), 1.0e-15);
                    }
                } else if (n == 1) {
                    for (DerivativeStructure ds : list) {
                        checkEquals(ds, ds.pow(n), 1.0e-15);
                    }
                } else {
                    for (DerivativeStructure ds : list) {
                        DerivativeStructure p = ds.getField().getOne();
                        for (int i = 0; i < n; ++i) {
                            p = p.multiply(ds);
                        }
                        checkEquals(p, ds.pow(n), 1.0e-15);
                    }
                }
            }
        }
    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testExpression
    public void testExpression() {
        double epsilon = 2.5e-13;
        for (double x = 0; x < 2; x += 0.2) {
            DerivativeStructure dsX = new DerivativeStructure(3, 5, 0, x);
            for (double y = 0; y < 2; y += 0.2) {
                DerivativeStructure dsY = new DerivativeStructure(3, 5, 1, y);
                for (double z = 0; z >- 2; z -= 0.2) {
                    DerivativeStructure dsZ = new DerivativeStructure(3, 5, 2, z);

                    
                    DerivativeStructure ds =
                            new DerivativeStructure(1, dsX,
                                                    5, dsX.multiply(dsY),
                                                    -2, dsZ,
                                                    1, new DerivativeStructure(8, dsZ.multiply(dsX),
                                                                               -1, dsY).pow(3));
                    DerivativeStructure dsOther =
                            new DerivativeStructure(1, dsX,
                                                    5, dsX.multiply(dsY),
                                                    -2, dsZ).add(new DerivativeStructure(8, dsZ.multiply(dsX),
                                                                                         -1, dsY).pow(3));
                    double f = x + 5 * x * y - 2 * z + FastMath.pow(8 * z * x - y, 3);
                    Assert.assertEquals(f, ds.getValue(),
                                        FastMath.abs(epsilon * f));
                    Assert.assertEquals(f, dsOther.getValue(),
                                        FastMath.abs(epsilon * f));

                    
                    double dfdx = 1 + 5 * y + 24 * z * FastMath.pow(8 * z * x - y, 2);
                    Assert.assertEquals(dfdx, ds.getPartialDerivative(1, 0, 0),
                                        FastMath.abs(epsilon * dfdx));
                    Assert.assertEquals(dfdx, dsOther.getPartialDerivative(1, 0, 0),
                                        FastMath.abs(epsilon * dfdx));

                    
                    double dfdxdy = 5 + 48 * z * (y - 8 * z * x);
                    Assert.assertEquals(dfdxdy, ds.getPartialDerivative(1, 1, 0),
                                        FastMath.abs(epsilon * dfdxdy));
                    Assert.assertEquals(dfdxdy, dsOther.getPartialDerivative(1, 1, 0),
                                        FastMath.abs(epsilon * dfdxdy));

                    
                    double dfdxdydz = 48 * (y - 16 * z * x);
                    Assert.assertEquals(dfdxdydz, ds.getPartialDerivative(1, 1, 1),
                                        FastMath.abs(epsilon * dfdxdydz));
                    Assert.assertEquals(dfdxdydz, dsOther.getPartialDerivative(1, 1, 1),
                                        FastMath.abs(epsilon * dfdxdydz));

                }
                
            }
        }
    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testCompositionOneVariableX
    public void testCompositionOneVariableX() {
        double epsilon = 1.0e-13;
        for (int maxOrder = 0; maxOrder < 5; ++maxOrder) {
            for (double x = 0.1; x < 1.2; x += 0.1) {
                DerivativeStructure dsX = new DerivativeStructure(1, maxOrder, 0, x);
                for (double y = 0.1; y < 1.2; y += 0.1) {
                    DerivativeStructure dsY = new DerivativeStructure(1, maxOrder, y);
                    DerivativeStructure f = dsX.divide(dsY).sqrt();
                    double f0 = FastMath.sqrt(x / y);
                    Assert.assertEquals(f0, f.getValue(), FastMath.abs(epsilon * f0));
                    if (f.getOrder() > 0) {
                        double f1 = 1 / (2 * FastMath.sqrt(x * y));
                        Assert.assertEquals(f1, f.getPartialDerivative(1), FastMath.abs(epsilon * f1));
                        if (f.getOrder() > 1) {
                            double f2 = -f1 / (2 * x); 
                            Assert.assertEquals(f2, f.getPartialDerivative(2), FastMath.abs(epsilon * f2));
                            if (f.getOrder() > 2) {
                                double f3 = (f0 + x / (2 * y * f0)) / (4 * x * x * x); 
                                Assert.assertEquals(f3, f.getPartialDerivative(3), FastMath.abs(epsilon * f3));
                            }
                        }
                    }
                }
            }
        }        
    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testTrigo
    public void testTrigo() {
        double epsilon = 2.0e-12;
        for (int maxOrder = 0; maxOrder < 5; ++maxOrder) {
            for (double x = 0.1; x < 1.2; x += 0.1) {
                DerivativeStructure dsX = new DerivativeStructure(3, maxOrder, 0, x);
                for (double y = 0.1; y < 1.2; y += 0.1) {
                    DerivativeStructure dsY = new DerivativeStructure(3, maxOrder, 1, y);
                    for (double z = 0.1; z < 1.2; z += 0.1) {
                        DerivativeStructure dsZ = new DerivativeStructure(3, maxOrder, 2, z);
                        DerivativeStructure f = dsX.divide(dsY.cos().add(dsZ.tan())).sin();
                        double a = FastMath.cos(y) + FastMath.tan(z);
                        double f0 = FastMath.sin(x / a);
                        Assert.assertEquals(f0, f.getValue(), FastMath.abs(epsilon * f0));
                        if (f.getOrder() > 0) {
                            double dfdx = FastMath.cos(x / a) / a;
                            Assert.assertEquals(dfdx, f.getPartialDerivative(1, 0, 0), FastMath.abs(epsilon * dfdx));
                            double dfdy =  x * FastMath.sin(y) * dfdx / a;
                            Assert.assertEquals(dfdy, f.getPartialDerivative(0, 1, 0), FastMath.abs(epsilon * dfdy));
                            double cz = FastMath.cos(z);
                            double cz2 = cz * cz;
                            double dfdz = -x * dfdx / (a * cz2);
                            Assert.assertEquals(dfdz, f.getPartialDerivative(0, 0, 1), FastMath.abs(epsilon * dfdz));
                            if (f.getOrder() > 1) {
                                double df2dx2 = -(f0 / (a * a));
                                Assert.assertEquals(df2dx2, f.getPartialDerivative(2, 0, 0), FastMath.abs(epsilon * df2dx2));
                                double df2dy2 = x * FastMath.cos(y) * dfdx / a -
                                                x * x * FastMath.sin(y) * FastMath.sin(y) * f0 / (a * a * a * a) +
                                                2 * FastMath.sin(y) * dfdy / a;
                                Assert.assertEquals(df2dy2, f.getPartialDerivative(0, 2, 0), FastMath.abs(epsilon * df2dy2));
                                double c4 = cz2 * cz2;
                                double df2dz2 = x * (2 * a * (1 - a * cz * FastMath.sin(z)) * dfdx - x * f0 / a ) / (a * a * a * c4);
                                Assert.assertEquals(df2dz2, f.getPartialDerivative(0, 0, 2), FastMath.abs(epsilon * df2dz2));
                                double df2dxdy = dfdy / x  - x * FastMath.sin(y) * f0 / (a * a * a);
                                Assert.assertEquals(df2dxdy, f.getPartialDerivative(1, 1, 0), FastMath.abs(epsilon * df2dxdy));
                            }
                        }
                    }
                }
            }        
        }
    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testSqrtDefinition
    public void testSqrtDefinition() {
        double[] epsilon = new double[] { 5.0e-16, 5.0e-16, 2.0e-15, 5.0e-14, 2.0e-12 };
        for (int maxOrder = 0; maxOrder < 5; ++maxOrder) {
            for (double x = 0.1; x < 1.2; x += 0.001) {
                DerivativeStructure dsX = new DerivativeStructure(1, maxOrder, 0, x);
                DerivativeStructure sqrt1 = dsX.pow(0.5);
                DerivativeStructure sqrt2 = dsX.sqrt();
                DerivativeStructure zero = sqrt1.subtract(sqrt2);
                for (int n = 0; n <= maxOrder; ++n) {
                    Assert.assertEquals(0, zero.getPartialDerivative(n), epsilon[n]);
                }
            }
        }
    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testRootNSingularity
    public void testRootNSingularity() {
        for (int n = 2; n < 10; ++n) {
            for (int maxOrder = 0; maxOrder < 12; ++maxOrder) {
                DerivativeStructure dsZero = new DerivativeStructure(1, maxOrder, 0, 0.0);
                DerivativeStructure rootN  = dsZero.rootN(n);
                Assert.assertEquals(0.0, rootN.getValue(), 1.0e-20);
                if (maxOrder > 0) {
                    Assert.assertTrue(Double.isInfinite(rootN.getPartialDerivative(1)));
                    Assert.assertTrue(rootN.getPartialDerivative(1) > 0);
                    for (int order = 2; order <= maxOrder; ++order) {
                        
                        
                        
                        
                        
                        
                        
                        
                        
                        
                        
                        
                        
                        
                        
                        
                        
                        
                        
                        Assert.assertTrue(Double.isNaN(rootN.getPartialDerivative(order)));
                    }
                }

                
                
                
                double[] gDerivatives = new double[ 1 + maxOrder];
                gDerivatives[0] = 0.0;
                for (int k = 1; k <= maxOrder; ++k) {
                    gDerivatives[k] = FastMath.pow(-1.0, k + 1);
                }
                DerivativeStructure correctRoot = new DerivativeStructure(1, maxOrder, gDerivatives).rootN(n);
                Assert.assertEquals(0.0, correctRoot.getValue(), 1.0e-20);
                if (maxOrder > 0) {
                    Assert.assertTrue(Double.isInfinite(correctRoot.getPartialDerivative(1)));
                    Assert.assertTrue(correctRoot.getPartialDerivative(1) > 0);
                    for (int order = 2; order <= maxOrder; ++order) {
                        Assert.assertTrue(Double.isInfinite(correctRoot.getPartialDerivative(order)));
                        if ((order % 2) == 0) {
                            Assert.assertTrue(correctRoot.getPartialDerivative(order) < 0);
                        } else {
                            Assert.assertTrue(correctRoot.getPartialDerivative(order) > 0);
                        }
                    }
                }

            }

        }

    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testSqrtPow2
    public void testSqrtPow2() {
        double[] epsilon = new double[] { 1.0e-16, 3.0e-16, 2.0e-15, 6.0e-14, 6.0e-12 };
        for (int maxOrder = 0; maxOrder < 5; ++maxOrder) {
            for (double x = 0.1; x < 1.2; x += 0.001) {
                DerivativeStructure dsX = new DerivativeStructure(1, maxOrder, 0, x);
                DerivativeStructure rebuiltX = dsX.multiply(dsX).sqrt();
                DerivativeStructure zero = rebuiltX.subtract(dsX);
                for (int n = 0; n <= maxOrder; ++n) {
                    Assert.assertEquals(0.0, zero.getPartialDerivative(n), epsilon[n]);
                }
            }
        }
    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testCbrtDefinition
    public void testCbrtDefinition() {
        double[] epsilon = new double[] { 4.0e-16, 9.0e-16, 6.0e-15, 2.0e-13, 4.0e-12 };
        for (int maxOrder = 0; maxOrder < 5; ++maxOrder) {
            for (double x = 0.1; x < 1.2; x += 0.001) {
                DerivativeStructure dsX = new DerivativeStructure(1, maxOrder, 0, x);
                DerivativeStructure cbrt1 = dsX.pow(1.0 / 3.0);
                DerivativeStructure cbrt2 = dsX.cbrt();
                DerivativeStructure zero = cbrt1.subtract(cbrt2);
                for (int n = 0; n <= maxOrder; ++n) {
                    Assert.assertEquals(0, zero.getPartialDerivative(n), epsilon[n]);
                }
            }
        }
    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testCbrtPow3
    public void testCbrtPow3() {
        double[] epsilon = new double[] { 1.0e-16, 5.0e-16, 8.0e-15, 3.0e-13, 4.0e-11 };
        for (int maxOrder = 0; maxOrder < 5; ++maxOrder) {
            for (double x = 0.1; x < 1.2; x += 0.001) {
                DerivativeStructure dsX = new DerivativeStructure(1, maxOrder, 0, x);
                DerivativeStructure rebuiltX = dsX.multiply(dsX.multiply(dsX)).cbrt();
                DerivativeStructure zero = rebuiltX.subtract(dsX);
                for (int n = 0; n <= maxOrder; ++n) {
                    Assert.assertEquals(0.0, zero.getPartialDerivative(n), epsilon[n]);
                }
            }
        }
    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testPowReciprocalPow
    public void testPowReciprocalPow() {
        double[] epsilon = new double[] { 2.0e-15, 2.0e-14, 3.0e-13, 8.0e-12, 3.0e-10 };
        for (int maxOrder = 0; maxOrder < 5; ++maxOrder) {
            for (double x = 0.1; x < 1.2; x += 0.01) {
                DerivativeStructure dsX = new DerivativeStructure(2, maxOrder, 0, x);
                for (double y = 0.1; y < 1.2; y += 0.01) {
                    DerivativeStructure dsY = new DerivativeStructure(2, maxOrder, 1, y);
                    DerivativeStructure rebuiltX = dsX.pow(dsY).pow(dsY.reciprocal());
                    DerivativeStructure zero = rebuiltX.subtract(dsX);
                    for (int n = 0; n <= maxOrder; ++n) {
                        for (int m = 0; m <= maxOrder; ++m) {
                            if (n + m <= maxOrder) {
                                Assert.assertEquals(0.0, zero.getPartialDerivative(n, m), epsilon[n + m]);
                            }
                        }
                    }
                }
            }
        }
    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testHypotDefinition
    public void testHypotDefinition() {
        double epsilon = 1.0e-20;
        for (int maxOrder = 0; maxOrder < 5; ++maxOrder) {
            for (double x = -1.7; x < 2; x += 0.2) {
                DerivativeStructure dsX = new DerivativeStructure(2, maxOrder, 0, x);
                for (double y = -1.7; y < 2; y += 0.2) {
                    DerivativeStructure dsY = new DerivativeStructure(2, maxOrder, 1, y);
                    DerivativeStructure hypot = DerivativeStructure.hypot(dsY, dsX);
                    DerivativeStructure ref = dsX.multiply(dsX).add(dsY.multiply(dsY)).sqrt();
                    DerivativeStructure zero = hypot.subtract(ref);
                    for (int n = 0; n <= maxOrder; ++n) {
                        for (int m = 0; m <= maxOrder; ++m) {
                            if (n + m <= maxOrder) {
                                Assert.assertEquals(0, zero.getPartialDerivative(n, m), epsilon);
                            }
                        }
                    }
                }
            }
        }
    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testHypotNoOverflow
    public void testHypotNoOverflow() {

        DerivativeStructure dsX = new DerivativeStructure(2, 5, 0, +3.0e250);
        DerivativeStructure dsY = new DerivativeStructure(2, 5, 1, -4.0e250);
        DerivativeStructure hypot = DerivativeStructure.hypot(dsX, dsY);
        Assert.assertEquals(5.0e250, hypot.getValue(), 1.0e235);
        Assert.assertEquals(dsX.getValue() / hypot.getValue(), hypot.getPartialDerivative(1, 0), 1.0e-10);
        Assert.assertEquals(dsY.getValue() / hypot.getValue(), hypot.getPartialDerivative(0, 1), 1.0e-10);

        DerivativeStructure sqrt  = dsX.multiply(dsX).add(dsY.multiply(dsY)).sqrt();
        Assert.assertTrue(Double.isInfinite(sqrt.getValue()));

    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testPrimitiveRemainder
    public void testPrimitiveRemainder() {
        double epsilon = 1.0e-15;
        for (int maxOrder = 0; maxOrder < 5; ++maxOrder) {
            for (double x = -1.7; x < 2; x += 0.2) {
                DerivativeStructure dsX = new DerivativeStructure(2, maxOrder, 0, x);
                for (double y = -1.7; y < 2; y += 0.2) {
                    DerivativeStructure remainder = dsX.remainder(y);
                    DerivativeStructure ref = dsX.subtract(x - (x % y));
                    DerivativeStructure zero = remainder.subtract(ref);
                    for (int n = 0; n <= maxOrder; ++n) {
                        for (int m = 0; m <= maxOrder; ++m) {
                            if (n + m <= maxOrder) {
                                Assert.assertEquals(0, zero.getPartialDerivative(n, m), epsilon);
                            }
                        }
                    }
                }
            }
        }
    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testRemainder
    public void testRemainder() {
        double epsilon = 1.0e-15;
        for (int maxOrder = 0; maxOrder < 5; ++maxOrder) {
            for (double x = -1.7; x < 2; x += 0.2) {
                DerivativeStructure dsX = new DerivativeStructure(2, maxOrder, 0, x);
                for (double y = -1.7; y < 2; y += 0.2) {
                    DerivativeStructure dsY = new DerivativeStructure(2, maxOrder, 1, y);
                    DerivativeStructure remainder = dsX.remainder(dsY);
                    DerivativeStructure ref = dsX.subtract(dsY.multiply((x - (x % y)) / y));
                    DerivativeStructure zero = remainder.subtract(ref);
                    for (int n = 0; n <= maxOrder; ++n) {
                        for (int m = 0; m <= maxOrder; ++m) {
                            if (n + m <= maxOrder) {
                                Assert.assertEquals(0, zero.getPartialDerivative(n, m), epsilon);
                            }
                        }
                    }
                }
            }
        }
    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testExp
    public void testExp() {
        double[] epsilon = new double[] { 1.0e-16, 1.0e-16, 1.0e-16, 1.0e-16, 1.0e-16 };
        for (int maxOrder = 0; maxOrder < 5; ++maxOrder) {
            for (double x = 0.1; x < 1.2; x += 0.001) {
                double refExp = FastMath.exp(x);
                DerivativeStructure exp = new DerivativeStructure(1, maxOrder, 0, x).exp();
                for (int n = 0; n <= maxOrder; ++n) {
                    Assert.assertEquals(refExp, exp.getPartialDerivative(n), epsilon[n]);
                }
            }
        }
    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testExpm1Definition
    public void testExpm1Definition() {
        double epsilon = 3.0e-16;
        for (int maxOrder = 0; maxOrder < 5; ++maxOrder) {
            for (double x = 0.1; x < 1.2; x += 0.001) {
                DerivativeStructure dsX = new DerivativeStructure(1, maxOrder, 0, x);
                DerivativeStructure expm11 = dsX.expm1();
                DerivativeStructure expm12 = dsX.exp().subtract(dsX.getField().getOne());
                DerivativeStructure zero = expm11.subtract(expm12);
                for (int n = 0; n <= maxOrder; ++n) {
                    Assert.assertEquals(0, zero.getPartialDerivative(n), epsilon);
                }
            }
        }
    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testLog
    public void testLog() {
        double[] epsilon = new double[] { 1.0e-16, 1.0e-16, 3.0e-14, 7.0e-13, 3.0e-11 };
        for (int maxOrder = 0; maxOrder < 5; ++maxOrder) {
            for (double x = 0.1; x < 1.2; x += 0.001) {
                DerivativeStructure log = new DerivativeStructure(1, maxOrder, 0, x).log();
                Assert.assertEquals(FastMath.log(x), log.getValue(), epsilon[0]);
                for (int n = 1; n <= maxOrder; ++n) {
                    double refDer = -ArithmeticUtils.factorial(n - 1) / FastMath.pow(-x, n);
                    Assert.assertEquals(refDer, log.getPartialDerivative(n), epsilon[n]);
                }
            }
        }
    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testLog1pDefinition
    public void testLog1pDefinition() {
        double epsilon = 3.0e-16;
        for (int maxOrder = 0; maxOrder < 5; ++maxOrder) {
            for (double x = 0.1; x < 1.2; x += 0.001) {
                DerivativeStructure dsX = new DerivativeStructure(1, maxOrder, 0, x);
                DerivativeStructure log1p1 = dsX.log1p();
                DerivativeStructure log1p2 = dsX.add(dsX.getField().getOne()).log();
                DerivativeStructure zero = log1p1.subtract(log1p2);
                for (int n = 0; n <= maxOrder; ++n) {
                    Assert.assertEquals(0, zero.getPartialDerivative(n), epsilon);
                }
            }
        }
    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testLog10Definition
    public void testLog10Definition() {
        double[] epsilon = new double[] { 3.0e-16, 3.0e-16, 8.0e-15, 3.0e-13, 8.0e-12 };
        for (int maxOrder = 0; maxOrder < 5; ++maxOrder) {
            for (double x = 0.1; x < 1.2; x += 0.001) {
                DerivativeStructure dsX = new DerivativeStructure(1, maxOrder, 0, x);
                DerivativeStructure log101 = dsX.log10();
                DerivativeStructure log102 = dsX.log().divide(FastMath.log(10.0));
                DerivativeStructure zero = log101.subtract(log102);
                for (int n = 0; n <= maxOrder; ++n) {
                    Assert.assertEquals(0, zero.getPartialDerivative(n), epsilon[n]);
                }
            }
        }
    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testLogExp
    public void testLogExp() {
        double[] epsilon = new double[] { 2.0e-16, 2.0e-16, 3.0e-16, 2.0e-15, 6.0e-15 };
        for (int maxOrder = 0; maxOrder < 5; ++maxOrder) {
            for (double x = 0.1; x < 1.2; x += 0.001) {
                DerivativeStructure dsX = new DerivativeStructure(1, maxOrder, 0, x);
                DerivativeStructure rebuiltX = dsX.exp().log();
                DerivativeStructure zero = rebuiltX.subtract(dsX);
                for (int n = 0; n <= maxOrder; ++n) {
                    Assert.assertEquals(0.0, zero.getPartialDerivative(n), epsilon[n]);
                }
            }
        }
    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testLog1pExpm1
    public void testLog1pExpm1() {
        double[] epsilon = new double[] { 6.0e-17, 3.0e-16, 5.0e-16, 9.0e-16, 6.0e-15 };
        for (int maxOrder = 0; maxOrder < 5; ++maxOrder) {
            for (double x = 0.1; x < 1.2; x += 0.001) {
                DerivativeStructure dsX = new DerivativeStructure(1, maxOrder, 0, x);
                DerivativeStructure rebuiltX = dsX.expm1().log1p();
                DerivativeStructure zero = rebuiltX.subtract(dsX);
                for (int n = 0; n <= maxOrder; ++n) {
                    Assert.assertEquals(0.0, zero.getPartialDerivative(n), epsilon[n]);
                }
            }
        }
    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testLog10Power
    public void testLog10Power() {
        double[] epsilon = new double[] { 3.0e-16, 3.0e-16, 9.0e-16, 6.0e-15, 6.0e-14 };
        for (int maxOrder = 0; maxOrder < 5; ++maxOrder) {
            for (double x = 0.1; x < 1.2; x += 0.001) {
                DerivativeStructure dsX = new DerivativeStructure(1, maxOrder, 0, x);
                DerivativeStructure rebuiltX = new DerivativeStructure(1, maxOrder, 10.0).pow(dsX).log10();
                DerivativeStructure zero = rebuiltX.subtract(dsX);
                for (int n = 0; n <= maxOrder; ++n) {
                    Assert.assertEquals(0, zero.getPartialDerivative(n), epsilon[n]);
                }
            }
        }
    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testSinCos
    public void testSinCos() {
        double epsilon = 5.0e-16;
        for (int maxOrder = 0; maxOrder < 6; ++maxOrder) {
            for (double x = 0.1; x < 1.2; x += 0.001) {
                DerivativeStructure dsX = new DerivativeStructure(1, maxOrder, 0, x);
                DerivativeStructure sin = dsX.sin();
                DerivativeStructure cos = dsX.cos();
                double s = FastMath.sin(x);
                double c = FastMath.cos(x);
                for (int n = 0; n <= maxOrder; ++n) {
                    switch (n % 4) {
                    case 0 :
                        Assert.assertEquals( s, sin.getPartialDerivative(n), epsilon);
                        Assert.assertEquals( c, cos.getPartialDerivative(n), epsilon);
                        break;
                    case 1 :
                        Assert.assertEquals( c, sin.getPartialDerivative(n), epsilon);
                        Assert.assertEquals(-s, cos.getPartialDerivative(n), epsilon);
                        break;
                    case 2 :
                        Assert.assertEquals(-s, sin.getPartialDerivative(n), epsilon);
                        Assert.assertEquals(-c, cos.getPartialDerivative(n), epsilon);
                        break;
                    default :
                        Assert.assertEquals(-c, sin.getPartialDerivative(n), epsilon);
                        Assert.assertEquals( s, cos.getPartialDerivative(n), epsilon);
                        break;
                    }
                }
            }
        }
    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testSinAsin
    public void testSinAsin() {
        double[] epsilon = new double[] { 3.0e-16, 5.0e-16, 3.0e-15, 2.0e-14, 4.0e-13 };
        for (int maxOrder = 0; maxOrder < 5; ++maxOrder) {
            for (double x = 0.1; x < 1.2; x += 0.001) {
                DerivativeStructure dsX = new DerivativeStructure(1, maxOrder, 0, x);
                DerivativeStructure rebuiltX = dsX.sin().asin();
                DerivativeStructure zero = rebuiltX.subtract(dsX);
                for (int n = 0; n <= maxOrder; ++n) {
                    Assert.assertEquals(0.0, zero.getPartialDerivative(n), epsilon[n]);
                }
            }
        }
    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testCosAcos
    public void testCosAcos() {
        double[] epsilon = new double[] { 6.0e-16, 6.0e-15, 2.0e-13, 4.0e-12, 2.0e-10 };
        for (int maxOrder = 0; maxOrder < 5; ++maxOrder) {
            for (double x = 0.1; x < 1.2; x += 0.001) {
                DerivativeStructure dsX = new DerivativeStructure(1, maxOrder, 0, x);
                DerivativeStructure rebuiltX = dsX.cos().acos();
                DerivativeStructure zero = rebuiltX.subtract(dsX);
                for (int n = 0; n <= maxOrder; ++n) {
                    Assert.assertEquals(0.0, zero.getPartialDerivative(n), epsilon[n]);
                }
            }
        }
    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testTanAtan
    public void testTanAtan() {
        double[] epsilon = new double[] { 6.0e-17, 2.0e-16, 2.0e-15, 4.0e-14, 2.0e-12 };
        for (int maxOrder = 0; maxOrder < 5; ++maxOrder) {
            for (double x = 0.1; x < 1.2; x += 0.001) {
                DerivativeStructure dsX = new DerivativeStructure(1, maxOrder, 0, x);
                DerivativeStructure rebuiltX = dsX.tan().atan();
                DerivativeStructure zero = rebuiltX.subtract(dsX);
                for (int n = 0; n <= maxOrder; ++n) {
                    Assert.assertEquals(0.0, zero.getPartialDerivative(n), epsilon[n]);
                }
            }
        }
    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testTangentDefinition
    public void testTangentDefinition() {
        double[] epsilon = new double[] { 5.0e-16, 2.0e-15, 3.0e-14, 5.0e-13, 2.0e-11 };
        for (int maxOrder = 0; maxOrder < 5; ++maxOrder) {
            for (double x = 0.1; x < 1.2; x += 0.001) {
                DerivativeStructure dsX = new DerivativeStructure(1, maxOrder, 0, x);
                DerivativeStructure tan1 = dsX.sin().divide(dsX.cos());
                DerivativeStructure tan2 = dsX.tan();
                DerivativeStructure zero = tan1.subtract(tan2);
                for (int n = 0; n <= maxOrder; ++n) {
                    Assert.assertEquals(0, zero.getPartialDerivative(n), epsilon[n]);
                }
            }
        }
    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testAtan2
    public void testAtan2() {
        double[] epsilon = new double[] { 5.0e-16, 3.0e-15, 2.2e-14, 1.0e-12, 8.0e-11 };
        for (int maxOrder = 0; maxOrder < 5; ++maxOrder) {
            for (double x = -1.7; x < 2; x += 0.2) {
                DerivativeStructure dsX = new DerivativeStructure(2, maxOrder, 0, x);
                for (double y = -1.7; y < 2; y += 0.2) {
                    DerivativeStructure dsY = new DerivativeStructure(2, maxOrder, 1, y);
                    DerivativeStructure atan2 = DerivativeStructure.atan2(dsY, dsX);
                    DerivativeStructure ref = dsY.divide(dsX).atan();
                    if (x < 0) {
                        ref = (y < 0) ? ref.subtract(FastMath.PI) : ref.add(FastMath.PI);
                    }
                    DerivativeStructure zero = atan2.subtract(ref);
                    for (int n = 0; n <= maxOrder; ++n) {
                        for (int m = 0; m <= maxOrder; ++m) {
                            if (n + m <= maxOrder) {
                                Assert.assertEquals(0, zero.getPartialDerivative(n, m), epsilon[n + m]);
                            }
                        }
                    }
                }
            }
        }
    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testAtan2SpecialCases
    public void testAtan2SpecialCases() {

        DerivativeStructure pp =
                DerivativeStructure.atan2(new DerivativeStructure(2, 2, 1, +0.0),
                                          new DerivativeStructure(2, 2, 1, +0.0));
        Assert.assertEquals(0, pp.getValue(), 1.0e-15);
        Assert.assertEquals(+1, FastMath.copySign(1, pp.getValue()), 1.0e-15);

        DerivativeStructure pn =
                DerivativeStructure.atan2(new DerivativeStructure(2, 2, 1, +0.0),
                                          new DerivativeStructure(2, 2, 1, -0.0));
        Assert.assertEquals(FastMath.PI, pn.getValue(), 1.0e-15);

        DerivativeStructure np =
                DerivativeStructure.atan2(new DerivativeStructure(2, 2, 1, -0.0),
                                          new DerivativeStructure(2, 2, 1, +0.0));
        Assert.assertEquals(0, np.getValue(), 1.0e-15);
        Assert.assertEquals(-1, FastMath.copySign(1, np.getValue()), 1.0e-15);

        DerivativeStructure nn =
                DerivativeStructure.atan2(new DerivativeStructure(2, 2, 1, -0.0),
                                          new DerivativeStructure(2, 2, 1, -0.0));
        Assert.assertEquals(-FastMath.PI, nn.getValue(), 1.0e-15);

    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testSinhDefinition
    public void testSinhDefinition() {
        double[] epsilon = new double[] { 3.0e-16, 3.0e-16, 5.0e-16, 2.0e-15, 6.0e-15 };
        for (int maxOrder = 0; maxOrder < 5; ++maxOrder) {
            for (double x = 0.1; x < 1.2; x += 0.001) {
                DerivativeStructure dsX = new DerivativeStructure(1, maxOrder, 0, x);
                DerivativeStructure sinh1 = dsX.exp().subtract(dsX.exp().reciprocal()).multiply(0.5);
                DerivativeStructure sinh2 = dsX.sinh();
                DerivativeStructure zero = sinh1.subtract(sinh2);
                for (int n = 0; n <= maxOrder; ++n) {
                    Assert.assertEquals(0, zero.getPartialDerivative(n), epsilon[n]);
                }
            }
        }
    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testCoshDefinition
    public void testCoshDefinition() {
        double[] epsilon = new double[] { 3.0e-16, 3.0e-16, 5.0e-16, 2.0e-15, 6.0e-15 };
        for (int maxOrder = 0; maxOrder < 5; ++maxOrder) {
            for (double x = 0.1; x < 1.2; x += 0.001) {
                DerivativeStructure dsX = new DerivativeStructure(1, maxOrder, 0, x);
                DerivativeStructure cosh1 = dsX.exp().add(dsX.exp().reciprocal()).multiply(0.5);
                DerivativeStructure cosh2 = dsX.cosh();
                DerivativeStructure zero = cosh1.subtract(cosh2);
                for (int n = 0; n <= maxOrder; ++n) {
                    Assert.assertEquals(0, zero.getPartialDerivative(n), epsilon[n]);
                }
            }
        }
    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testTanhDefinition
    public void testTanhDefinition() {
        double[] epsilon = new double[] { 3.0e-16, 5.0e-16, 7.0e-16, 3.0e-15, 2.0e-14 };
        for (int maxOrder = 0; maxOrder < 5; ++maxOrder) {
            for (double x = 0.1; x < 1.2; x += 0.001) {
                DerivativeStructure dsX = new DerivativeStructure(1, maxOrder, 0, x);
                DerivativeStructure tanh1 = dsX.exp().subtract(dsX.exp().reciprocal()).divide(dsX.exp().add(dsX.exp().reciprocal()));
                DerivativeStructure tanh2 = dsX.tanh();
                DerivativeStructure zero = tanh1.subtract(tanh2);
                for (int n = 0; n <= maxOrder; ++n) {
                    Assert.assertEquals(0, zero.getPartialDerivative(n), epsilon[n]);
                }
            }
        }
    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testSinhAsinh
    public void testSinhAsinh() {
        double[] epsilon = new double[] { 3.0e-16, 3.0e-16, 4.0e-16, 7.0e-16, 3.0e-15, 8.0e-15 };
        for (int maxOrder = 0; maxOrder < 6; ++maxOrder) {
            for (double x = 0.1; x < 1.2; x += 0.001) {
                DerivativeStructure dsX = new DerivativeStructure(1, maxOrder, 0, x);
                DerivativeStructure rebuiltX = dsX.sinh().asinh();
                DerivativeStructure zero = rebuiltX.subtract(dsX);
                for (int n = 0; n <= maxOrder; ++n) {
                    Assert.assertEquals(0.0, zero.getPartialDerivative(n), epsilon[n]);
                }
            }
        }
    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testCoshAcosh
    public void testCoshAcosh() {
        double[] epsilon = new double[] { 2.0e-15, 1.0e-14, 2.0e-13, 6.0e-12, 3.0e-10, 2.0e-8 };
        for (int maxOrder = 0; maxOrder < 6; ++maxOrder) {
            for (double x = 0.1; x < 1.2; x += 0.001) {
                DerivativeStructure dsX = new DerivativeStructure(1, maxOrder, 0, x);
                DerivativeStructure rebuiltX = dsX.cosh().acosh();
                DerivativeStructure zero = rebuiltX.subtract(dsX);
                for (int n = 0; n <= maxOrder; ++n) {
                    Assert.assertEquals(0.0, zero.getPartialDerivative(n), epsilon[n]);
                }
            }
        }
    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testTanhAtanh
    public void testTanhAtanh() {
        double[] epsilon = new double[] { 3.0e-16, 2.0e-16, 7.0e-16, 4.0e-15, 3.0e-14, 4.0e-13 };
        for (int maxOrder = 0; maxOrder < 6; ++maxOrder) {
            for (double x = 0.1; x < 1.2; x += 0.001) {
                DerivativeStructure dsX = new DerivativeStructure(1, maxOrder, 0, x);
                DerivativeStructure rebuiltX = dsX.tanh().atanh();
                DerivativeStructure zero = rebuiltX.subtract(dsX);
                for (int n = 0; n <= maxOrder; ++n) {
                    Assert.assertEquals(0.0, zero.getPartialDerivative(n), epsilon[n]);
                }
            }
        }
    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testCompositionOneVariableY
    public void testCompositionOneVariableY() {
        double epsilon = 1.0e-13;
        for (int maxOrder = 0; maxOrder < 5; ++maxOrder) {
            for (double x = 0.1; x < 1.2; x += 0.1) {
                DerivativeStructure dsX = new DerivativeStructure(1, maxOrder, x);
                for (double y = 0.1; y < 1.2; y += 0.1) {
                    DerivativeStructure dsY = new DerivativeStructure(1, maxOrder, 0, y);
                    DerivativeStructure f = dsX.divide(dsY).sqrt();
                    double f0 = FastMath.sqrt(x / y);
                    Assert.assertEquals(f0, f.getValue(), FastMath.abs(epsilon * f0));
                    if (f.getOrder() > 0) {
                        double f1 = -x / (2 * y * y * f0);
                        Assert.assertEquals(f1, f.getPartialDerivative(1), FastMath.abs(epsilon * f1));
                        if (f.getOrder() > 1) {
                            double f2 = (f0 - x / (4 * y * f0)) / (y * y); 
                            Assert.assertEquals(f2, f.getPartialDerivative(2), FastMath.abs(epsilon * f2));
                            if (f.getOrder() > 2) {
                                double f3 = (x / (8 * y * f0) - 2 * f0) / (y * y * y); 
                                Assert.assertEquals(f3, f.getPartialDerivative(3), FastMath.abs(epsilon * f3));
                            }
                        }
                    }
                }
            }
        }        
    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testTaylorPolynomial
    public void testTaylorPolynomial() {
        for (double x = 0; x < 1.2; x += 0.1) {
            DerivativeStructure dsX = new DerivativeStructure(3, 4, 0, x);
            for (double y = 0; y < 1.2; y += 0.2) {
                DerivativeStructure dsY = new DerivativeStructure(3, 4, 1, y);
                for (double z = 0; z < 1.2; z += 0.2) {
                    DerivativeStructure dsZ = new DerivativeStructure(3, 4, 2, z);
                    DerivativeStructure f = dsX.multiply(dsY).add(dsZ).multiply(dsX).multiply(dsY);
                    for (double dx = -0.2; dx < 0.2; dx += 0.2) {
                        for (double dy = -0.2; dy < 0.2; dy += 0.1) {
                            for (double dz = -0.2; dz < 0.2; dz += 0.1) {
                                double ref = (x + dx) * (y + dy) * ((x + dx) * (y + dy) + (z + dz));
                                Assert.assertEquals(ref, f.taylor(dx, dy, dz), 2.0e-15);
                            }
                        }
                    }
                }
            }
        }
    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testTaylorAtan2
    public void testTaylorAtan2() {
        double[] expected = new double[] { 0.214, 0.0241, 0.00422, 6.48e-4, 8.04e-5 };
        double x0 =  0.1;
        double y0 = -0.3;
        for (int maxOrder = 0; maxOrder < 5; ++maxOrder) {
            DerivativeStructure dsX   = new DerivativeStructure(2, maxOrder, 0, x0);
            DerivativeStructure dsY   = new DerivativeStructure(2, maxOrder, 1, y0);
            DerivativeStructure atan2 = DerivativeStructure.atan2(dsY, dsX);
            double maxError = 0;
            for (double dx = -0.05; dx < 0.05; dx += 0.001) {
                for (double dy = -0.05; dy < 0.05; dy += 0.001) {
                    double ref = FastMath.atan2(y0 + dy, x0 + dx);
                    maxError = FastMath.max(maxError, FastMath.abs(ref - atan2.taylor(dx, dy)));
                }
            }
            Assert.assertEquals(0.0, expected[maxOrder] - maxError, 0.01 * expected[maxOrder]);
        }
    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testAbs
    public void testAbs() {

        DerivativeStructure minusOne = new DerivativeStructure(1, 1, 0, -1.0);
        Assert.assertEquals(+1.0, minusOne.abs().getPartialDerivative(0), 1.0e-15);
        Assert.assertEquals(-1.0, minusOne.abs().getPartialDerivative(1), 1.0e-15);

        DerivativeStructure plusOne = new DerivativeStructure(1, 1, 0, +1.0);
        Assert.assertEquals(+1.0, plusOne.abs().getPartialDerivative(0), 1.0e-15);
        Assert.assertEquals(+1.0, plusOne.abs().getPartialDerivative(1), 1.0e-15);

        DerivativeStructure minusZero = new DerivativeStructure(1, 1, 0, -0.0);
        Assert.assertEquals(+0.0, minusZero.abs().getPartialDerivative(0), 1.0e-15);
        Assert.assertEquals(-1.0, minusZero.abs().getPartialDerivative(1), 1.0e-15);

        DerivativeStructure plusZero = new DerivativeStructure(1, 1, 0, +0.0);
        Assert.assertEquals(+0.0, plusZero.abs().getPartialDerivative(0), 1.0e-15);
        Assert.assertEquals(+1.0, plusZero.abs().getPartialDerivative(1), 1.0e-15);

    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testSignum
    public void testSignum() {

        DerivativeStructure minusOne = new DerivativeStructure(1, 1, 0, -1.0);
        Assert.assertEquals(-1.0, minusOne.signum().getPartialDerivative(0), 1.0e-15);
        Assert.assertEquals( 0.0, minusOne.signum().getPartialDerivative(1), 1.0e-15);

        DerivativeStructure plusOne = new DerivativeStructure(1, 1, 0, +1.0);
        Assert.assertEquals(+1.0, plusOne.signum().getPartialDerivative(0), 1.0e-15);
        Assert.assertEquals( 0.0, plusOne.signum().getPartialDerivative(1), 1.0e-15);

        DerivativeStructure minusZero = new DerivativeStructure(1, 1, 0, -0.0);
        Assert.assertEquals(-0.0, minusZero.signum().getPartialDerivative(0), 1.0e-15);
        Assert.assertTrue(Double.doubleToLongBits(minusZero.signum().getValue()) < 0);
        Assert.assertEquals( 0.0, minusZero.signum().getPartialDerivative(1), 1.0e-15);

        DerivativeStructure plusZero = new DerivativeStructure(1, 1, 0, +0.0);
        Assert.assertEquals(+0.0, plusZero.signum().getPartialDerivative(0), 1.0e-15);
        Assert.assertTrue(Double.doubleToLongBits(plusZero.signum().getValue()) == 0);
        Assert.assertEquals( 0.0, plusZero.signum().getPartialDerivative(1), 1.0e-15);

    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testCeilFloorRintLong
    public void testCeilFloorRintLong() {

        DerivativeStructure x = new DerivativeStructure(1, 1, 0, -1.5);
        Assert.assertEquals(-1.5, x.getPartialDerivative(0), 1.0e-15);
        Assert.assertEquals(+1.0, x.getPartialDerivative(1), 1.0e-15);
        Assert.assertEquals(-1.0, x.ceil().getPartialDerivative(0), 1.0e-15);
        Assert.assertEquals(+0.0, x.ceil().getPartialDerivative(1), 1.0e-15);
        Assert.assertEquals(-2.0, x.floor().getPartialDerivative(0), 1.0e-15);
        Assert.assertEquals(+0.0, x.floor().getPartialDerivative(1), 1.0e-15);
        Assert.assertEquals(-2.0, x.rint().getPartialDerivative(0), 1.0e-15);
        Assert.assertEquals(+0.0, x.rint().getPartialDerivative(1), 1.0e-15);
        Assert.assertEquals(-2.0, x.subtract(x.getField().getOne()).rint().getPartialDerivative(0), 1.0e-15);
        Assert.assertEquals(-1l, x.round());

    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testCopySign
    public void testCopySign() {
        DerivativeStructure minusOne = new DerivativeStructure(1, 1, 0, -1.0);
        Assert.assertEquals(+1.0, minusOne.copySign(+1.0).getPartialDerivative(0), 1.0e-15);
        Assert.assertEquals(-1.0, minusOne.copySign(+1.0).getPartialDerivative(1), 1.0e-15);
        Assert.assertEquals(-1.0, minusOne.copySign(-1.0).getPartialDerivative(0), 1.0e-15);
        Assert.assertEquals(+1.0, minusOne.copySign(-1.0).getPartialDerivative(1), 1.0e-15);
        Assert.assertEquals(+1.0, minusOne.copySign(+0.0).getPartialDerivative(0), 1.0e-15);
        Assert.assertEquals(-1.0, minusOne.copySign(+0.0).getPartialDerivative(1), 1.0e-15);
        Assert.assertEquals(-1.0, minusOne.copySign(-0.0).getPartialDerivative(0), 1.0e-15);
        Assert.assertEquals(+1.0, minusOne.copySign(-0.0).getPartialDerivative(1), 1.0e-15);
        Assert.assertEquals(+1.0, minusOne.copySign(Double.NaN).getPartialDerivative(0), 1.0e-15);
        Assert.assertEquals(-1.0, minusOne.copySign(Double.NaN).getPartialDerivative(1), 1.0e-15);
    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testToDegreesDefinition
    public void testToDegreesDefinition() {
        double epsilon = 3.0e-16;
        for (int maxOrder = 0; maxOrder < 6; ++maxOrder) {
            for (double x = 0.1; x < 1.2; x += 0.001) {
                DerivativeStructure dsX = new DerivativeStructure(1, maxOrder, 0, x);
                Assert.assertEquals(FastMath.toDegrees(x), dsX.toDegrees().getValue(), epsilon);
                for (int n = 1; n <= maxOrder; ++n) {
                    if (n == 1) {
                        Assert.assertEquals(180 / FastMath.PI, dsX.toDegrees().getPartialDerivative(1), epsilon);
                    } else {
                        Assert.assertEquals(0.0, dsX.toDegrees().getPartialDerivative(n), epsilon);
                    }
                }
            }
        }
    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testToRadiansDefinition
    public void testToRadiansDefinition() {
        double epsilon = 3.0e-16;
        for (int maxOrder = 0; maxOrder < 6; ++maxOrder) {
            for (double x = 0.1; x < 1.2; x += 0.001) {
                DerivativeStructure dsX = new DerivativeStructure(1, maxOrder, 0, x);
                Assert.assertEquals(FastMath.toRadians(x), dsX.toRadians().getValue(), epsilon);
                for (int n = 1; n <= maxOrder; ++n) {
                    if (n == 1) {
                        Assert.assertEquals(FastMath.PI / 180, dsX.toRadians().getPartialDerivative(1), epsilon);
                    } else {
                        Assert.assertEquals(0.0, dsX.toRadians().getPartialDerivative(n), epsilon);
                    }
                }
            }
        }
    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testDegRad
    public void testDegRad() {
        double epsilon = 3.0e-16;
        for (int maxOrder = 0; maxOrder < 6; ++maxOrder) {
            for (double x = 0.1; x < 1.2; x += 0.001) {
                DerivativeStructure dsX = new DerivativeStructure(1, maxOrder, 0, x);
                DerivativeStructure rebuiltX = dsX.toDegrees().toRadians();
                DerivativeStructure zero = rebuiltX.subtract(dsX);
                for (int n = 0; n <= maxOrder; ++n) {
                    Assert.assertEquals(0.0, zero.getPartialDerivative(n), epsilon);
                }
            }
        }
    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testComposeMismatchedDimensions
    public void testComposeMismatchedDimensions() {
        new DerivativeStructure(1, 3, 0, 1.2).compose(new double[3]);
    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testCompose
    public void testCompose() {
        double[] epsilon = new double[] { 1.0e-20, 5.0e-14, 2.0e-13, 3.0e-13, 2.0e-13, 1.0e-20 };
        PolynomialFunction poly =
                new PolynomialFunction(new double[] { 1.0, 2.0, 3.0, 4.0, 5.0, 6.0 });
        for (int maxOrder = 0; maxOrder < 6; ++maxOrder) {
            PolynomialFunction[] p = new PolynomialFunction[maxOrder + 1];
            p[0] = poly;
            for (int i = 1; i <= maxOrder; ++i) {
                p[i] = p[i - 1].polynomialDerivative();
            }
            for (double x = 0.1; x < 1.2; x += 0.001) {
                DerivativeStructure dsX = new DerivativeStructure(1, maxOrder, 0, x);
                DerivativeStructure dsY1 = dsX.getField().getZero();
                for (int i = poly.degree(); i >= 0; --i) {
                    dsY1 = dsY1.multiply(dsX).add(poly.getCoefficients()[i]);
                }
                double[] f = new double[maxOrder + 1];
                for (int i = 0; i < f.length; ++i) {
                    f[i] = p[i].value(x);
                }
                DerivativeStructure dsY2 = dsX.compose(f);
                DerivativeStructure zero = dsY1.subtract(dsY2);
                for (int n = 0; n <= maxOrder; ++n) {
                    Assert.assertEquals(0.0, zero.getPartialDerivative(n), epsilon[n]);
                }
            }
        }
    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testField
    public void testField() {
        for (int maxOrder = 1; maxOrder < 5; ++maxOrder) {
            DerivativeStructure x = new DerivativeStructure(3, maxOrder, 0, 1.0);
            checkF0F1(x.getField().getZero(), 0.0, 0.0, 0.0, 0.0);
            checkF0F1(x.getField().getOne(), 1.0, 0.0, 0.0, 0.0);
            Assert.assertEquals(maxOrder, x.getField().getZero().getOrder());
            Assert.assertEquals(3, x.getField().getZero().getFreeParameters());
            Assert.assertEquals(DerivativeStructure.class, x.getField().getRuntimeClass());
        }
    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testOneParameterConstructor
    public void testOneParameterConstructor() {
        double x = 1.2;
        double cos = FastMath.cos(x);
        double sin = FastMath.sin(x);
        DerivativeStructure yRef = new DerivativeStructure(1, 4, 0, x).cos();
        try {
            new DerivativeStructure(1, 4, 0.0, 0.0);
            Assert.fail("an exception should have been thrown");
        } catch (DimensionMismatchException dme) {
            
        } catch (Exception e) {
            Assert.fail("wrong exceptionc caught " + e.getClass().getName());
        }
        double[] derivatives = new double[] { cos, -sin, -cos, sin, cos };
        DerivativeStructure y = new DerivativeStructure(1,  4, derivatives);
        checkEquals(yRef, y, 1.0e-15);
        TestUtils.assertEquals(derivatives, y.getAllDerivatives(), 1.0e-15);
    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testOneOrderConstructor
    public void testOneOrderConstructor() {
        double x =  1.2;
        double y =  2.4;
        double z = 12.5;
        DerivativeStructure xRef = new DerivativeStructure(3, 1, 0, x);
        DerivativeStructure yRef = new DerivativeStructure(3, 1, 1, y);
        DerivativeStructure zRef = new DerivativeStructure(3, 1, 2, z);
        try {
            new DerivativeStructure(3, 1, x + y - z, 1.0, 1.0);
            Assert.fail("an exception should have been thrown");
        } catch (DimensionMismatchException dme) {
            
        } catch (Exception e) {
            Assert.fail("wrong exceptionc caught " + e.getClass().getName());
        }
        double[] derivatives = new double[] { x + y - z, 1.0, 1.0, -1.0 };
        DerivativeStructure t = new DerivativeStructure(3, 1, derivatives);
        checkEquals(xRef.add(yRef.subtract(zRef)), t, 1.0e-15);
        TestUtils.assertEquals(derivatives, xRef.add(yRef.subtract(zRef)).getAllDerivatives(), 1.0e-15);
    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testSerialization
    public void testSerialization() {
        DerivativeStructure a = new DerivativeStructure(3, 2, 0, 1.3);
        DerivativeStructure b = (DerivativeStructure) TestUtils.serializeAndRecover(a);
        Assert.assertEquals(a.getFreeParameters(), b.getFreeParameters());
        Assert.assertEquals(a.getOrder(), b.getOrder());
        checkEquals(a, b, 1.0e-15);
    }

// org.apache.commons.math3.analysis.differentiation.FiniteDifferencesDifferentiatorTest::testWrongNumberOfPoints
    public void testWrongNumberOfPoints() {
        new FiniteDifferencesDifferentiator(1, 1.0);
    }

// org.apache.commons.math3.analysis.differentiation.FiniteDifferencesDifferentiatorTest::testWrongStepSize
    public void testWrongStepSize() {
        new FiniteDifferencesDifferentiator(3, 0.0);
    }

// org.apache.commons.math3.analysis.differentiation.FiniteDifferencesDifferentiatorTest::testSerialization
    public void testSerialization() {
        FiniteDifferencesDifferentiator differentiator =
                new FiniteDifferencesDifferentiator(3, 1.0e-3);
        FiniteDifferencesDifferentiator recovered =
                (FiniteDifferencesDifferentiator) TestUtils.serializeAndRecover(differentiator);
        Assert.assertEquals(differentiator.getNbPoints(), recovered.getNbPoints());
        Assert.assertEquals(differentiator.getStepSize(), recovered.getStepSize(), 1.0e-15);
    }

// org.apache.commons.math3.analysis.differentiation.FiniteDifferencesDifferentiatorTest::testConstant
    public void testConstant() {
        FiniteDifferencesDifferentiator differentiator =
                new FiniteDifferencesDifferentiator(5, 0.01);
        UnivariateDifferentiableFunction f =
                differentiator.differentiate(new UnivariateFunction() {
                    public double value(double x) {
                        return 42.0;
                    }
                });
        for (double x = -10; x < 10; x += 0.1) {
            DerivativeStructure y = f.value(new DerivativeStructure(1, 2, 0, x));
            Assert.assertEquals(42.0, y.getValue(), 1.0e-15);
            Assert.assertEquals( 0.0, y.getPartialDerivative(1), 1.0e-15);
            Assert.assertEquals( 0.0, y.getPartialDerivative(2), 1.0e-15);
        }
    }

// org.apache.commons.math3.analysis.differentiation.FiniteDifferencesDifferentiatorTest::testLinear
    public void testLinear() {
        FiniteDifferencesDifferentiator differentiator =
                new FiniteDifferencesDifferentiator(5, 0.01);
        UnivariateDifferentiableFunction f =
                differentiator.differentiate(new UnivariateFunction() {
                    public double value(double x) {
                        return 2 - 3 * x;
                    }
                });
        for (double x = -10; x < 10; x += 0.1) {
            DerivativeStructure y = f.value(new DerivativeStructure(1, 2, 0, x));
            Assert.assertEquals("" + (2 - 3 * x - y.getValue()), 2 - 3 * x, y.getValue(), 2.0e-15);
            Assert.assertEquals(-3.0, y.getPartialDerivative(1), 4.0e-13);
            Assert.assertEquals( 0.0, y.getPartialDerivative(2), 9.0e-11);
        }
    }

// org.apache.commons.math3.analysis.differentiation.FiniteDifferencesDifferentiatorTest::testGaussian
    public void testGaussian() {
        FiniteDifferencesDifferentiator differentiator =
                new FiniteDifferencesDifferentiator(9, 0.02);
        UnivariateDifferentiableFunction gaussian = new Gaussian(1.0, 2.0);
        UnivariateDifferentiableFunction f =
                differentiator.differentiate(gaussian);
        double[] expectedError = new double[] {
            6.939e-18, 1.284e-15, 2.477e-13, 1.168e-11, 2.840e-9, 7.971e-8
        };
       double[] maxError = new double[expectedError.length];
        for (double x = -10; x < 10; x += 0.1) {
            DerivativeStructure dsX  = new DerivativeStructure(1, maxError.length - 1, 0, x);
            DerivativeStructure yRef = gaussian.value(dsX);
            DerivativeStructure y    = f.value(dsX);
            Assert.assertEquals(f.value(dsX.getValue()), f.value(dsX).getValue(), 1.0e-15);
            for (int order = 0; order <= yRef.getOrder(); ++order) {
                maxError[order] = FastMath.max(maxError[order],
                                        FastMath.abs(yRef.getPartialDerivative(order) -
                                                     y.getPartialDerivative(order)));
            }
        }
        for (int i = 0; i < maxError.length; ++i) {
            Assert.assertEquals(expectedError[i], maxError[i], 0.01 * expectedError[i]);
        }
    }

// org.apache.commons.math3.analysis.differentiation.FiniteDifferencesDifferentiatorTest::testStepSizeUnstability
    public void testStepSizeUnstability() {
        UnivariateDifferentiableFunction quintic = new QuinticFunction();
        UnivariateDifferentiableFunction goodStep =
                new FiniteDifferencesDifferentiator(7, 0.25).differentiate(quintic);
        UnivariateDifferentiableFunction badStep =
                new FiniteDifferencesDifferentiator(7, 1.0e-6).differentiate(quintic);
        double[] maxErrorGood = new double[7];
        double[] maxErrorBad  = new double[7];
        for (double x = -10; x < 10; x += 0.1) {
            DerivativeStructure dsX  = new DerivativeStructure(1, 6, 0, x);
            DerivativeStructure yRef  = quintic.value(dsX);
            DerivativeStructure yGood = goodStep.value(dsX);
            DerivativeStructure yBad  = badStep.value(dsX);
            for (int order = 0; order <= 6; ++order) {
                maxErrorGood[order] = FastMath.max(maxErrorGood[order],
                                                   FastMath.abs(yRef.getPartialDerivative(order) -
                                                                yGood.getPartialDerivative(order)));
                maxErrorBad[order]  = FastMath.max(maxErrorBad[order],
                                                   FastMath.abs(yRef.getPartialDerivative(order) -
                                                                yBad.getPartialDerivative(order)));
            }
        }

        
        
        final double[] expectedGood = new double[] {
            7.276e-12, 7.276e-11, 9.968e-10, 3.092e-9, 5.432e-8, 8.196e-8, 1.818e-6
        };

        
        
        final double[] expectedBad = new double[] {
            2.910e-11, 2.087e-5, 147.7, 3.820e7, 6.354e14, 6.548e19, 1.543e27            
        };

        for (int i = 0; i < maxErrorGood.length; ++i) {
            Assert.assertEquals(expectedGood[i], maxErrorGood[i], 0.01 * expectedGood[i]);
            Assert.assertEquals(expectedBad[i],  maxErrorBad[i],  0.01 * expectedBad[i]);
        }

    }

// org.apache.commons.math3.analysis.differentiation.FiniteDifferencesDifferentiatorTest::testWrongOrder
    public void testWrongOrder() {
        UnivariateDifferentiableFunction f =
                new FiniteDifferencesDifferentiator(3, 0.01).differentiate(new UnivariateFunction() {
                    public double value(double x) {
                        
                        
                        throw new MathInternalError();
                    }
                });
        f.value(new DerivativeStructure(1, 3, 0, 1.0));
    }

// org.apache.commons.math3.analysis.differentiation.FiniteDifferencesDifferentiatorTest::testWrongOrderVector
    public void testWrongOrderVector() {
        UnivariateDifferentiableVectorFunction f =
                new FiniteDifferencesDifferentiator(3, 0.01).differentiate(new UnivariateVectorFunction() {
                    public double[] value(double x) {
                        
                        
                        throw new MathInternalError();
                    }
                });
        f.value(new DerivativeStructure(1, 3, 0, 1.0));
    }

// org.apache.commons.math3.analysis.differentiation.FiniteDifferencesDifferentiatorTest::testWrongOrderMatrix
    public void testWrongOrderMatrix() {
        UnivariateDifferentiableMatrixFunction f =
                new FiniteDifferencesDifferentiator(3, 0.01).differentiate(new UnivariateMatrixFunction() {
                    public double[][] value(double x) {
                        
                        
                        throw new MathInternalError();
                    }
                });
        f.value(new DerivativeStructure(1, 3, 0, 1.0));
    }

// org.apache.commons.math3.analysis.differentiation.FiniteDifferencesDifferentiatorTest::testTooLargeStep
    public void testTooLargeStep() {
        new FiniteDifferencesDifferentiator(3, 2.5, 0.0, 1.0);
    }

// org.apache.commons.math3.analysis.differentiation.FiniteDifferencesDifferentiatorTest::testBounds
    public void testBounds() {

        final double slope = 2.5;
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                if (x < 0) {
                    throw new NumberIsTooSmallException(x, 0, true);
                } else if (x > 1) {
                    throw new NumberIsTooLargeException(x, 1, true);
                } else {
                    return slope * x;
                }
            }
        };

        UnivariateDifferentiableFunction missingBounds =
                new FiniteDifferencesDifferentiator(3, 0.1).differentiate(f);
        UnivariateDifferentiableFunction properlyBounded =
                new FiniteDifferencesDifferentiator(3, 0.1, 0.0, 1.0).differentiate(f);
        DerivativeStructure tLow  = new DerivativeStructure(1, 1, 0, 0.05);
        DerivativeStructure tHigh = new DerivativeStructure(1, 1, 0, 0.95);

        try {
            
            
            missingBounds.value(tLow);
            Assert.fail("an exception should have been thrown");
        } catch (NumberIsTooSmallException nse) {
            Assert.assertEquals(-0.05, nse.getArgument().doubleValue(), 1.0e-10);
        } catch (Exception e) {
            Assert.fail("wrong exception caught: " + e.getClass().getName());
        }

        try {
            
            
            missingBounds.value(tHigh);
            Assert.fail("an exception should have been thrown");
        } catch (NumberIsTooLargeException nle) {
            Assert.assertEquals(1.05, nle.getArgument().doubleValue(), 1.0e-10);
        } catch (Exception e) {
            Assert.fail("wrong exception caught: " + e.getClass().getName());
        }

        
        
        Assert.assertEquals(slope, properlyBounded.value(tLow).getPartialDerivative(1), 1.0e-10);
        
        
        
        Assert.assertEquals(slope, properlyBounded.value(tHigh).getPartialDerivative(1), 1.0e-10);
        
    }

// org.apache.commons.math3.analysis.differentiation.FiniteDifferencesDifferentiatorTest::testBoundedSqrt
    public void testBoundedSqrt() {

        UnivariateFunctionDifferentiator differentiator =
                new FiniteDifferencesDifferentiator(9, 1.0 / 32, 0.0, Double.POSITIVE_INFINITY);
        UnivariateDifferentiableFunction sqrt = differentiator.differentiate(new UnivariateFunction() {
            public double value(double x) {
                return FastMath.sqrt(x);
            }
        });

        
        DerivativeStructure t001 = new DerivativeStructure(1, 1, 0, 0.01);
        Assert.assertEquals(0.5 / FastMath.sqrt(t001.getValue()), sqrt.value(t001).getPartialDerivative(1), 1.6);
        DerivativeStructure t01 = new DerivativeStructure(1, 1, 0, 0.1);
        Assert.assertEquals(0.5 / FastMath.sqrt(t01.getValue()), sqrt.value(t01).getPartialDerivative(1), 7.0e-3);
        DerivativeStructure t03 = new DerivativeStructure(1, 1, 0, 0.3);
        Assert.assertEquals(0.5 / FastMath.sqrt(t03.getValue()), sqrt.value(t03).getPartialDerivative(1), 2.1e-7);

    }

// org.apache.commons.math3.analysis.differentiation.FiniteDifferencesDifferentiatorTest::testVectorFunction
    public void testVectorFunction() {

        FiniteDifferencesDifferentiator differentiator =
                new FiniteDifferencesDifferentiator(7, 0.01);
        UnivariateDifferentiableVectorFunction f =
                differentiator.differentiate(new UnivariateVectorFunction() {
            
            public double[] value(double x) {
                return new double[] { FastMath.cos(x), FastMath.sin(x) };
            }
            
        });

        for (double x = -10; x < 10; x += 0.1) {
            DerivativeStructure dsX = new DerivativeStructure(1, 2, 0, x);
            DerivativeStructure[] y = f.value(dsX);
            double cos = FastMath.cos(x);
            double sin = FastMath.sin(x);
            double[] f1 = f.value(dsX.getValue());
            DerivativeStructure[] f2 = f.value(dsX);
            Assert.assertEquals(f1.length, f2.length);
            for (int i = 0; i < f1.length; ++i) {
                Assert.assertEquals(f1[i], f2[i].getValue(), 1.0e-15);
            }
            Assert.assertEquals( cos, y[0].getValue(), 7.0e-16);
            Assert.assertEquals( sin, y[1].getValue(), 7.0e-16);
            Assert.assertEquals(-sin, y[0].getPartialDerivative(1), 6.0e-14);
            Assert.assertEquals( cos, y[1].getPartialDerivative(1), 6.0e-14);
            Assert.assertEquals(-cos, y[0].getPartialDerivative(2), 2.0e-11);
            Assert.assertEquals(-sin, y[1].getPartialDerivative(2), 2.0e-11);
        }

    }

// org.apache.commons.math3.analysis.differentiation.FiniteDifferencesDifferentiatorTest::testMatrixFunction
    public void testMatrixFunction() {

        FiniteDifferencesDifferentiator differentiator =
                new FiniteDifferencesDifferentiator(7, 0.01);
        UnivariateDifferentiableMatrixFunction f =
                differentiator.differentiate(new UnivariateMatrixFunction() {
            
            public double[][] value(double x) {
                return new double[][] {
                    { FastMath.cos(x),  FastMath.sin(x)  },
                    { FastMath.cosh(x), FastMath.sinh(x) }
                };
            }
            
        });

        for (double x = -1; x < 1; x += 0.02) {
            DerivativeStructure dsX = new DerivativeStructure(1, 2, 0, x);
            DerivativeStructure[][] y = f.value(dsX);
            double cos = FastMath.cos(x);
            double sin = FastMath.sin(x);
            double cosh = FastMath.cosh(x);
            double sinh = FastMath.sinh(x);
            double[][] f1 = f.value(dsX.getValue());
            DerivativeStructure[][] f2 = f.value(dsX);
            Assert.assertEquals(f1.length, f2.length);
            for (int i = 0; i < f1.length; ++i) {
                Assert.assertEquals(f1[i].length, f2[i].length);
                for (int j = 0; j < f1[i].length; ++j) {
                    Assert.assertEquals(f1[i][j], f2[i][j].getValue(), 1.0e-15);
                }
            }
            Assert.assertEquals(cos,   y[0][0].getValue(), 7.0e-18);
            Assert.assertEquals(sin,   y[0][1].getValue(), 6.0e-17);
            Assert.assertEquals(cosh,  y[1][0].getValue(), 3.0e-16);
            Assert.assertEquals(sinh,  y[1][1].getValue(), 3.0e-16);
            Assert.assertEquals(-sin,  y[0][0].getPartialDerivative(1), 2.0e-14);
            Assert.assertEquals( cos,  y[0][1].getPartialDerivative(1), 2.0e-14);
            Assert.assertEquals( sinh, y[1][0].getPartialDerivative(1), 3.0e-14);
            Assert.assertEquals( cosh, y[1][1].getPartialDerivative(1), 3.0e-14);
            Assert.assertEquals(-cos,  y[0][0].getPartialDerivative(2), 3.0e-12);
            Assert.assertEquals(-sin,  y[0][1].getPartialDerivative(2), 3.0e-12);
            Assert.assertEquals( cosh, y[1][0].getPartialDerivative(2), 6.0e-12);
            Assert.assertEquals( sinh, y[1][1].getPartialDerivative(2), 6.0e-12);
        }

    }

// org.apache.commons.math3.analysis.differentiation.FiniteDifferencesDifferentiatorTest::testSeveralFreeParameters
    public void testSeveralFreeParameters() {
        FiniteDifferencesDifferentiator differentiator =
                new FiniteDifferencesDifferentiator(5, 0.001);
        UnivariateDifferentiableFunction sine = new Sin();
        UnivariateDifferentiableFunction f =
                differentiator.differentiate(sine);
        double[] expectedError = new double[] {
            6.696e-16, 1.371e-12, 2.007e-8, 1.754e-5
        };
        double[] maxError = new double[expectedError.length];
       for (double x = -2; x < 2; x += 0.1) {
           for (double y = -2; y < 2; y += 0.1) {
               DerivativeStructure dsX  = new DerivativeStructure(2, maxError.length - 1, 0, x);
               DerivativeStructure dsY  = new DerivativeStructure(2, maxError.length - 1, 1, y);
               DerivativeStructure dsT  = dsX.multiply(3).subtract(dsY.multiply(2));
               DerivativeStructure sRef = sine.value(dsT);
               DerivativeStructure s    = f.value(dsT);
               for (int xOrder = 0; xOrder <= sRef.getOrder(); ++xOrder) {
                   for (int yOrder = 0; yOrder <= sRef.getOrder(); ++yOrder) {
                       if (xOrder + yOrder <= sRef.getOrder()) {
                           maxError[xOrder +yOrder] = FastMath.max(maxError[xOrder + yOrder],
                                                                    FastMath.abs(sRef.getPartialDerivative(xOrder, yOrder) -
                                                                                 s.getPartialDerivative(xOrder, yOrder)));
                       }
                   }
               }
           }
       }
       for (int i = 0; i < maxError.length; ++i) {
           Assert.assertEquals(expectedError[i], maxError[i], 0.01 * expectedError[i]);
       }
    }

// org.apache.commons.math3.analysis.differentiation.GradientFunctionTest::test2DDistance
    public void test2DDistance() {
        EuclideanDistance f = new EuclideanDistance();
        GradientFunction g = new GradientFunction(f);
        for (double x = -10; x < 10; x += 0.5) {
            for (double y = -10; y < 10; y += 0.5) {
                double[] point = new double[] { x, y };
                TestUtils.assertEquals(f.gradient(point), g.value(point), 1.0e-15);
            }
        }
    }

// org.apache.commons.math3.analysis.differentiation.GradientFunctionTest::test3DDistance
    public void test3DDistance() {
        EuclideanDistance f = new EuclideanDistance();
        GradientFunction g = new GradientFunction(f);
        for (double x = -10; x < 10; x += 0.5) {
            for (double y = -10; y < 10; y += 0.5) {
                for (double z = -10; z < 10; z += 0.5) {
                    double[] point = new double[] { x, y, z };
                    TestUtils.assertEquals(f.gradient(point), g.value(point), 1.0e-15);
                }
            }
        }
    }

// org.apache.commons.math3.analysis.differentiation.JacobianFunctionTest::testSphere
    public void testSphere() {
        SphereMapping    f = new SphereMapping(10.0);
        JacobianFunction j = new JacobianFunction(f);
        for (double latitude = -1.5; latitude < 1.5; latitude += 0.1) {
            for (double longitude = -3.1; longitude < 3.1; longitude += 0.1) {
                double[] point = new double[] { latitude, longitude };
                double[][] referenceJacobian  = f.jacobian(point);
                double[][] testJacobian       = j.value(point);
                Assert.assertEquals(referenceJacobian.length, testJacobian.length);
                for (int i = 0; i < 3; ++i) {
                    TestUtils.assertEquals(referenceJacobian[i], testJacobian[i], 2.0e-15);
                }
            }
        }
    }

// org.apache.commons.math3.analysis.function.GaussianTest::testPreconditions
    public void testPreconditions() {
        new Gaussian(1, 2, -1);
    }

// org.apache.commons.math3.analysis.function.GaussianTest::testSomeValues
    public void testSomeValues() {
        final UnivariateFunction f = new Gaussian();

        Assert.assertEquals(1 / FastMath.sqrt(2 * Math.PI), f.value(0), EPS);
    }

// org.apache.commons.math3.analysis.function.GaussianTest::testLargeArguments
    public void testLargeArguments() {
        final UnivariateFunction f = new Gaussian();

        Assert.assertEquals(0, f.value(Double.NEGATIVE_INFINITY), 0);
        Assert.assertEquals(0, f.value(-Double.MAX_VALUE), 0);
        Assert.assertEquals(0, f.value(-1e2), 0);
        Assert.assertEquals(0, f.value(1e2), 0);
        Assert.assertEquals(0, f.value(Double.MAX_VALUE), 0);
        Assert.assertEquals(0, f.value(Double.POSITIVE_INFINITY), 0);
    }

// org.apache.commons.math3.analysis.function.GaussianTest::testDerivatives
    public void testDerivatives() {
        final UnivariateDifferentiableFunction gaussian = new Gaussian(2.0, 0.9, 3.0);
        final DerivativeStructure dsX = new DerivativeStructure(1, 4, 0, 1.1);
        final DerivativeStructure dsY = gaussian.value(dsX);
        Assert.assertEquals( 1.9955604901712128349,   dsY.getValue(),              EPS);
        Assert.assertEquals(-0.044345788670471396332, dsY.getPartialDerivative(1), EPS);
        Assert.assertEquals(-0.22074348138190206174,  dsY.getPartialDerivative(2), EPS);
        Assert.assertEquals( 0.014760030401924800557, dsY.getPartialDerivative(3), EPS);
        Assert.assertEquals( 0.073253159785035691678, dsY.getPartialDerivative(4), EPS);
    }

// org.apache.commons.math3.analysis.function.GaussianTest::testDerivativeLargeArguments
    public void testDerivativeLargeArguments() {
        final Gaussian f = new Gaussian(0, 1e-50);

        Assert.assertEquals(0, f.value(new DerivativeStructure(1, 1, 0, Double.NEGATIVE_INFINITY)).getPartialDerivative(1), 0);
        Assert.assertEquals(0, f.value(new DerivativeStructure(1, 1, 0, -Double.MAX_VALUE)).getPartialDerivative(1), 0);
        Assert.assertEquals(0, f.value(new DerivativeStructure(1, 1, 0, -1e50)).getPartialDerivative(1), 0);
        Assert.assertEquals(0, f.value(new DerivativeStructure(1, 1, 0, -1e2)).getPartialDerivative(1), 0);
        Assert.assertEquals(0, f.value(new DerivativeStructure(1, 1, 0, 1e2)).getPartialDerivative(1), 0);
        Assert.assertEquals(0, f.value(new DerivativeStructure(1, 1, 0, 1e50)).getPartialDerivative(1), 0);
        Assert.assertEquals(0, f.value(new DerivativeStructure(1, 1, 0, Double.MAX_VALUE)).getPartialDerivative(1), 0);
        Assert.assertEquals(0, f.value(new DerivativeStructure(1, 1, 0, Double.POSITIVE_INFINITY)).getPartialDerivative(1), 0);        
    }

// org.apache.commons.math3.analysis.function.GaussianTest::testDerivativesNaN
    public void testDerivativesNaN() {
        final Gaussian f = new Gaussian(0, 1e-50);
        final DerivativeStructure fx = f.value(new DerivativeStructure(1, 5, 0, Double.NaN));
        for (int i = 0; i <= fx.getOrder(); ++i) {
            Assert.assertTrue(Double.isNaN(fx.getPartialDerivative(i)));
        }
    }

// org.apache.commons.math3.analysis.function.GaussianTest::testParametricUsage1
    public void testParametricUsage1() {
        final Gaussian.Parametric g = new Gaussian.Parametric();
        g.value(0, null);
    }

// org.apache.commons.math3.analysis.function.GaussianTest::testParametricUsage2
    public void testParametricUsage2() {
        final Gaussian.Parametric g = new Gaussian.Parametric();
        g.value(0, new double[] {0});
    }

// org.apache.commons.math3.analysis.function.GaussianTest::testParametricUsage3
    public void testParametricUsage3() {
        final Gaussian.Parametric g = new Gaussian.Parametric();
        g.value(0, new double[] {0, 1, 0});
    }

// org.apache.commons.math3.analysis.function.GaussianTest::testParametricUsage4
    public void testParametricUsage4() {
        final Gaussian.Parametric g = new Gaussian.Parametric();
        g.gradient(0, null);
    }

// org.apache.commons.math3.analysis.function.GaussianTest::testParametricUsage5
    public void testParametricUsage5() {
        final Gaussian.Parametric g = new Gaussian.Parametric();
        g.gradient(0, new double[] {0});
    }

// org.apache.commons.math3.analysis.function.GaussianTest::testParametricUsage6
    public void testParametricUsage6() {
        final Gaussian.Parametric g = new Gaussian.Parametric();
        g.gradient(0, new double[] {0, 1, 0});
    }

// org.apache.commons.math3.analysis.function.GaussianTest::testParametricValue
    public void testParametricValue() {
        final double norm = 2;
        final double mean = 3;
        final double sigma = 4;
        final Gaussian f = new Gaussian(norm, mean, sigma);

        final Gaussian.Parametric g = new Gaussian.Parametric();
        Assert.assertEquals(f.value(-1), g.value(-1, new double[] {norm, mean, sigma}), 0);
        Assert.assertEquals(f.value(0), g.value(0, new double[] {norm, mean, sigma}), 0);
        Assert.assertEquals(f.value(2), g.value(2, new double[] {norm, mean, sigma}), 0);
    }

// org.apache.commons.math3.analysis.function.GaussianTest::testParametricGradient
    public void testParametricGradient() {
        final double norm = 2;
        final double mean = 3;
        final double sigma = 4;
        final Gaussian.Parametric f = new Gaussian.Parametric();

        final double x = 1;
        final double[] grad = f.gradient(1, new double[] {norm, mean, sigma});
        final double diff = x - mean;
        final double n = FastMath.exp(-diff * diff / (2 * sigma * sigma));
        Assert.assertEquals(n, grad[0], EPS);
        final double m = norm * n * diff / (sigma * sigma);
        Assert.assertEquals(m, grad[1], EPS);
        final double s = m * diff / sigma;
        Assert.assertEquals(s, grad[2], EPS);
    }

// org.apache.commons.math3.analysis.function.HarmonicOscillatorTest::testSomeValues
    public void testSomeValues() {
        final double a = -1.2;
        final double w = 0.34;
        final double p = 5.6;
        final UnivariateFunction f = new HarmonicOscillator(a, w, p);

        final double d = 0.12345;
        for (int i = 0; i < 10; i++) {
            final double v = i * d;
            Assert.assertEquals(a * FastMath.cos(w * v + p), f.value(v), 0);
        }
    }

// org.apache.commons.math3.analysis.function.HarmonicOscillatorTest::testDerivative
    public void testDerivative() {
        final double a = -1.2;
        final double w = 0.34;
        final double p = 5.6;
        final HarmonicOscillator f = new HarmonicOscillator(a, w, p);

        for (int maxOrder = 0; maxOrder < 6; ++maxOrder) {
            final double d = 0.12345;
            for (int i = 0; i < 10; i++) {
                final double v = i * d;
                final DerivativeStructure h = f.value(new DerivativeStructure(1, maxOrder, 0, v));
                for (int k = 0; k <= maxOrder; ++k) {
                    final double trigo;
                    switch (k % 4) {
                        case 0:
                            trigo = +FastMath.cos(w * v + p);
                            break;
                        case 1:
                            trigo = -FastMath.sin(w * v + p);
                            break;
                        case 2:
                            trigo = -FastMath.cos(w * v + p);
                            break;
                        default:
                            trigo = +FastMath.sin(w * v + p);
                            break;
                    }
                    Assert.assertEquals(a * FastMath.pow(w, k) * trigo,
                                        h.getPartialDerivative(k),
                                        Precision.EPSILON);
                }
            }
        }
    }

// org.apache.commons.math3.analysis.function.HarmonicOscillatorTest::testParametricUsage1
    public void testParametricUsage1() {
        final HarmonicOscillator.Parametric g = new HarmonicOscillator.Parametric();
        g.value(0, null);
    }

// org.apache.commons.math3.analysis.function.HarmonicOscillatorTest::testParametricUsage2
    public void testParametricUsage2() {
        final HarmonicOscillator.Parametric g = new HarmonicOscillator.Parametric();
        g.value(0, new double[] {0});
    }

// org.apache.commons.math3.analysis.function.HarmonicOscillatorTest::testParametricUsage3
    public void testParametricUsage3() {
        final HarmonicOscillator.Parametric g = new HarmonicOscillator.Parametric();
        g.gradient(0, null);
    }

// org.apache.commons.math3.analysis.function.HarmonicOscillatorTest::testParametricUsage4
    public void testParametricUsage4() {
        final HarmonicOscillator.Parametric g = new HarmonicOscillator.Parametric();
        g.gradient(0, new double[] {0});
    }

// org.apache.commons.math3.analysis.function.HarmonicOscillatorTest::testParametricValue
    public void testParametricValue() {
        final double amplitude = 2;
        final double omega = 3;
        final double phase = 4;
        final HarmonicOscillator f = new HarmonicOscillator(amplitude, omega, phase);

        final HarmonicOscillator.Parametric g = new HarmonicOscillator.Parametric();
        Assert.assertEquals(f.value(-1), g.value(-1, new double[] {amplitude, omega, phase}), 0);
        Assert.assertEquals(f.value(0), g.value(0, new double[] {amplitude, omega, phase}), 0);
        Assert.assertEquals(f.value(2), g.value(2, new double[] {amplitude, omega, phase}), 0);
    }

// org.apache.commons.math3.analysis.function.HarmonicOscillatorTest::testParametricGradient
    public void testParametricGradient() {
        final double amplitude = 2;
        final double omega = 3;
        final double phase = 4;
        final HarmonicOscillator.Parametric f = new HarmonicOscillator.Parametric();

        final double x = 1;
        final double[] grad = f.gradient(1, new double[] {amplitude, omega, phase});
        final double xTimesOmegaPlusPhase = omega * x + phase;
        final double a = FastMath.cos(xTimesOmegaPlusPhase);
        Assert.assertEquals(a, grad[0], EPS);
        final double w = -amplitude * x * FastMath.sin(xTimesOmegaPlusPhase);
        Assert.assertEquals(w, grad[1], EPS);
        final double p = -amplitude * FastMath.sin(xTimesOmegaPlusPhase);
        Assert.assertEquals(p, grad[2], EPS);
    }

// org.apache.commons.math3.analysis.function.LogisticTest::testPreconditions1
    public void testPreconditions1() {
        new Logistic(1, 0, 1, 1, 0, -1);
    }

// org.apache.commons.math3.analysis.function.LogisticTest::testPreconditions2
    public void testPreconditions2() {
        new Logistic(1, 0, 1, 1, 0, 0);
    }

// org.apache.commons.math3.analysis.function.LogisticTest::testCompareSigmoid
    public void testCompareSigmoid() {
        final UnivariateFunction sig = new Sigmoid();
        final UnivariateFunction sigL = new Logistic(1, 0, 1, 1, 0, 1);

        final double min = -2;
        final double max = 2;
        final int n = 100;
        final double delta = (max - min) / n;
        for (int i = 0; i < n; i++) {
            final double x = min + i * delta;
            Assert.assertEquals("x=" + x, sig.value(x), sigL.value(x), EPS);
        }
    }

// org.apache.commons.math3.analysis.function.LogisticTest::testSomeValues
    public void testSomeValues() {
        final double k = 4;
        final double m = 5;
        final double b = 2;
        final double q = 3;
        final double a = -1;
        final double n = 2;

        final UnivariateFunction f = new Logistic(k, m, b, q, a, n);

        double x;
        x = m;
        Assert.assertEquals("x=" + x, a + (k - a) / FastMath.sqrt(1 + q), f.value(x), EPS);

        x = Double.NEGATIVE_INFINITY;
        Assert.assertEquals("x=" + x, a, f.value(x), EPS);

        x = Double.POSITIVE_INFINITY;
        Assert.assertEquals("x=" + x, k, f.value(x), EPS);
    }

// org.apache.commons.math3.analysis.function.LogisticTest::testCompareDerivativeSigmoid
    public void testCompareDerivativeSigmoid() {
        final double k = 3;
        final double a = 2;

        final Logistic f = new Logistic(k, 0, 1, 1, a, 1);
        final Sigmoid g = new Sigmoid(a, k);
        
        final double min = -10;
        final double max = 10;
        final double n = 20;
        final double delta = (max - min) / n;
        for (int i = 0; i < n; i++) {
            final DerivativeStructure x = new DerivativeStructure(1, 5, 0, min + i * delta);
            for (int order = 0; order <= x.getOrder(); ++order) {
                Assert.assertEquals("x=" + x.getValue(),
                                    g.value(x).getPartialDerivative(order),
                                    f.value(x).getPartialDerivative(order),
                                    3.0e-15);
            }
        }
    }

// org.apache.commons.math3.analysis.function.LogisticTest::testParametricUsage1
    public void testParametricUsage1() {
        final Logistic.Parametric g = new Logistic.Parametric();
        g.value(0, null);
    }

// org.apache.commons.math3.analysis.function.LogisticTest::testParametricUsage2
    public void testParametricUsage2() {
        final Logistic.Parametric g = new Logistic.Parametric();
        g.value(0, new double[] {0});
    }

// org.apache.commons.math3.analysis.function.LogisticTest::testParametricUsage3
    public void testParametricUsage3() {
        final Logistic.Parametric g = new Logistic.Parametric();
        g.gradient(0, null);
    }

// org.apache.commons.math3.analysis.function.LogisticTest::testParametricUsage4
    public void testParametricUsage4() {
        final Logistic.Parametric g = new Logistic.Parametric();
        g.gradient(0, new double[] {0});
    }

// org.apache.commons.math3.analysis.function.LogisticTest::testParametricUsage5
    public void testParametricUsage5() {
        final Logistic.Parametric g = new Logistic.Parametric();
        g.value(0, new double[] {1, 0, 1, 1, 0 ,0});
    }

// org.apache.commons.math3.analysis.function.LogisticTest::testParametricUsage6
    public void testParametricUsage6() {
        final Logistic.Parametric g = new Logistic.Parametric();
        g.gradient(0, new double[] {1, 0, 1, 1, 0 ,0});
    }

// org.apache.commons.math3.analysis.function.LogisticTest::testGradientComponent0Component4
    public void testGradientComponent0Component4() {
        final double k = 3;
        final double a = 2;

        final Logistic.Parametric f = new Logistic.Parametric();
        
        final Sigmoid.Parametric g = new Sigmoid.Parametric();
        
        final double x = 0.12345;
        final double[] gf = f.gradient(x, new double[] {k, 0, 1, 1, a, 1});
        final double[] gg = g.gradient(x, new double[] {a, k});

        Assert.assertEquals(gg[0], gf[4], EPS);
        Assert.assertEquals(gg[1], gf[0], EPS);
    }

// org.apache.commons.math3.analysis.function.LogisticTest::testGradientComponent5
    public void testGradientComponent5() {
        final double m = 1.2;
        final double k = 3.4;
        final double a = 2.3;
        final double q = 0.567;
        final double b = -FastMath.log(q);
        final double n = 3.4;

        final Logistic.Parametric f = new Logistic.Parametric();
        
        final double x = m - 1;
        final double qExp1 = 2;

        final double[] gf = f.gradient(x, new double[] {k, m, b, q, a, n});

        Assert.assertEquals((k - a) * FastMath.log(qExp1) / (n * n * FastMath.pow(qExp1, 1 / n)),
                            gf[5], EPS);
    }

// org.apache.commons.math3.analysis.function.LogisticTest::testGradientComponent1Component2Component3
    public void testGradientComponent1Component2Component3() {
        final double m = 1.2;
        final double k = 3.4;
        final double a = 2.3;
        final double b = 0.567;
        final double q = 1 / FastMath.exp(b * m);
        final double n = 3.4;

        final Logistic.Parametric f = new Logistic.Parametric();
        
        final double x = 0;
        final double qExp1 = 2;

        final double[] gf = f.gradient(x, new double[] {k, m, b, q, a, n});

        final double factor = (a - k) / (n * FastMath.pow(qExp1, 1 / n + 1));
        Assert.assertEquals(factor * b, gf[1], EPS);
        Assert.assertEquals(factor * m, gf[2], EPS);
        Assert.assertEquals(factor / q, gf[3], EPS);
    }

// org.apache.commons.math3.analysis.function.LogitTest::testPreconditions1
    public void testPreconditions1() {
        final double lo = -1;
        final double hi = 2;
        final UnivariateFunction f = new Logit(lo, hi);

        f.value(lo - 1);
    }

// org.apache.commons.math3.analysis.function.LogitTest::testPreconditions2
    public void testPreconditions2() {
        final double lo = -1;
        final double hi = 2;
        final UnivariateFunction f = new Logit(lo, hi);

        f.value(hi + 1);
    }

// org.apache.commons.math3.analysis.function.LogitTest::testSomeValues
    public void testSomeValues() {
        final double lo = 1;
        final double hi = 2;
        final UnivariateFunction f = new Logit(lo, hi);

        Assert.assertEquals(Double.NEGATIVE_INFINITY, f.value(1), EPS);
        Assert.assertEquals(Double.POSITIVE_INFINITY, f.value(2), EPS);
        Assert.assertEquals(0, f.value(1.5), EPS);
    }

// org.apache.commons.math3.analysis.function.LogitTest::testDerivative
    public void testDerivative() {
        final double lo = 1;
        final double hi = 2;
        final Logit f = new Logit(lo, hi);
        final DerivativeStructure f15 = f.value(new DerivativeStructure(1, 1, 0, 1.5));

        Assert.assertEquals(4, f15.getPartialDerivative(1), EPS);
    }

// org.apache.commons.math3.analysis.function.LogitTest::testDerivativeLargeArguments
    public void testDerivativeLargeArguments() {
        final Logit f = new Logit(1, 2);

        for (double arg : new double[] {
            Double.NEGATIVE_INFINITY, -Double.MAX_VALUE, -1e155, 1e155, Double.MAX_VALUE, Double.POSITIVE_INFINITY
            }) {
            try {
                f.value(new DerivativeStructure(1, 1, 0, arg));
                Assert.fail("an exception should have been thrown");
            } catch (OutOfRangeException ore) {
                
            } catch (Exception e) {
                Assert.fail("wrong exception caught: " + e.getMessage());
            }
        }
    }

// org.apache.commons.math3.analysis.function.LogitTest::testDerivativesHighOrder
    public void testDerivativesHighOrder() {
        DerivativeStructure l = new Logit(1, 3).value(new DerivativeStructure(1, 5, 0, 1.2));
        Assert.assertEquals(-2.1972245773362193828, l.getPartialDerivative(0), 1.0e-16);
        Assert.assertEquals(5.5555555555555555555,  l.getPartialDerivative(1), 9.0e-16);
        Assert.assertEquals(-24.691358024691358025, l.getPartialDerivative(2), 2.0e-14);
        Assert.assertEquals(250.34293552812071331,  l.getPartialDerivative(3), 2.0e-13);
        Assert.assertEquals(-3749.4284407864654778, l.getPartialDerivative(4), 4.0e-12);
        Assert.assertEquals(75001.270131585632282,  l.getPartialDerivative(5), 8.0e-11);
    }

// org.apache.commons.math3.analysis.function.LogitTest::testParametricUsage1
    public void testParametricUsage1() {
        final Logit.Parametric g = new Logit.Parametric();
        g.value(0, null);
    }

// org.apache.commons.math3.analysis.function.LogitTest::testParametricUsage2
    public void testParametricUsage2() {
        final Logit.Parametric g = new Logit.Parametric();
        g.value(0, new double[] {0});
    }

// org.apache.commons.math3.analysis.function.LogitTest::testParametricUsage3
    public void testParametricUsage3() {
        final Logit.Parametric g = new Logit.Parametric();
        g.gradient(0, null);
    }

// org.apache.commons.math3.analysis.function.LogitTest::testParametricUsage4
    public void testParametricUsage4() {
        final Logit.Parametric g = new Logit.Parametric();
        g.gradient(0, new double[] {0});
    }

// org.apache.commons.math3.analysis.function.LogitTest::testParametricUsage5
    public void testParametricUsage5() {
        final Logit.Parametric g = new Logit.Parametric();
        g.value(-1, new double[] {0, 1});
    }

// org.apache.commons.math3.analysis.function.LogitTest::testParametricUsage6
    public void testParametricUsage6() {
        final Logit.Parametric g = new Logit.Parametric();
        g.value(2, new double[] {0, 1});
    }

// org.apache.commons.math3.analysis.function.LogitTest::testParametricValue
    public void testParametricValue() {
        final double lo = 2;
        final double hi = 3;
        final Logit f = new Logit(lo, hi);

        final Logit.Parametric g = new Logit.Parametric();
        Assert.assertEquals(f.value(2), g.value(2, new double[] {lo, hi}), 0);
        Assert.assertEquals(f.value(2.34567), g.value(2.34567, new double[] {lo, hi}), 0);
        Assert.assertEquals(f.value(3), g.value(3, new double[] {lo, hi}), 0);
    }

// org.apache.commons.math3.analysis.function.LogitTest::testValueWithInverseFunction
    public void testValueWithInverseFunction() {
        final double lo = 2;
        final double hi = 3;
        final Logit f = new Logit(lo, hi);
        final Sigmoid g = new Sigmoid(lo, hi);
        RandomGenerator random = new Well1024a(0x49914cdd9f0b8db5l);
        final UnivariateDifferentiableFunction id = FunctionUtils.compose((UnivariateDifferentiableFunction) g,
                                                                (UnivariateDifferentiableFunction) f);

        for (int i = 0; i < 10; i++) {
            final double x = lo + random.nextDouble() * (hi - lo);
            Assert.assertEquals(x, id.value(new DerivativeStructure(1, 1, 0, x)).getValue(), EPS);
        }

        Assert.assertEquals(lo, id.value(new DerivativeStructure(1, 1, 0, lo)).getValue(), EPS);
        Assert.assertEquals(hi, id.value(new DerivativeStructure(1, 1, 0, hi)).getValue(), EPS);
    }

// org.apache.commons.math3.analysis.function.LogitTest::testDerivativesWithInverseFunction
    public void testDerivativesWithInverseFunction() {
        double[] epsilon = new double[] { 1.0e-20, 4.0e-16, 3.0e-15, 2.0e-11, 3.0e-9, 1.0e-6 };
        final double lo = 2;
        final double hi = 3;
        final Logit f = new Logit(lo, hi);
        final Sigmoid g = new Sigmoid(lo, hi);
        RandomGenerator random = new Well1024a(0x96885e9c1f81cea5l);
        final UnivariateDifferentiableFunction id =
                FunctionUtils.compose((UnivariateDifferentiableFunction) g, (UnivariateDifferentiableFunction) f);
        for (int maxOrder = 0; maxOrder < 6; ++maxOrder) {
            double max = 0;
            for (int i = 0; i < 10; i++) {
                final double x = lo + random.nextDouble() * (hi - lo);
                final DerivativeStructure dsX = new DerivativeStructure(1, maxOrder, 0, x);
                max = FastMath.max(max, FastMath.abs(dsX.getPartialDerivative(maxOrder) -
                                                     id.value(dsX).getPartialDerivative(maxOrder)));
                Assert.assertEquals(dsX.getPartialDerivative(maxOrder),
                                    id.value(dsX).getPartialDerivative(maxOrder),
                                    epsilon[maxOrder]);
            }

            
            
            final DerivativeStructure dsLo = new DerivativeStructure(1, maxOrder, 0, lo);
            if (maxOrder == 0) {
                Assert.assertTrue(Double.isInfinite(f.value(dsLo).getPartialDerivative(maxOrder)));
                Assert.assertEquals(lo, id.value(dsLo).getPartialDerivative(maxOrder), epsilon[maxOrder]);
            } else if (maxOrder == 1) {
                Assert.assertTrue(Double.isInfinite(f.value(dsLo).getPartialDerivative(maxOrder)));
                Assert.assertTrue(Double.isNaN(id.value(dsLo).getPartialDerivative(maxOrder)));
            } else {
                Assert.assertTrue(Double.isNaN(f.value(dsLo).getPartialDerivative(maxOrder)));
                Assert.assertTrue(Double.isNaN(id.value(dsLo).getPartialDerivative(maxOrder)));
            }

            final DerivativeStructure dsHi = new DerivativeStructure(1, maxOrder, 0, hi);
            if (maxOrder == 0) {
                Assert.assertTrue(Double.isInfinite(f.value(dsHi).getPartialDerivative(maxOrder)));
                Assert.assertEquals(hi, id.value(dsHi).getPartialDerivative(maxOrder), epsilon[maxOrder]);
            } else if (maxOrder == 1) {
                Assert.assertTrue(Double.isInfinite(f.value(dsHi).getPartialDerivative(maxOrder)));
                Assert.assertTrue(Double.isNaN(id.value(dsHi).getPartialDerivative(maxOrder)));
            } else {
                Assert.assertTrue(Double.isNaN(f.value(dsHi).getPartialDerivative(maxOrder)));
                Assert.assertTrue(Double.isNaN(id.value(dsHi).getPartialDerivative(maxOrder)));
            }

        }
    }

// org.apache.commons.math3.analysis.function.SigmoidTest::testSomeValues
    public void testSomeValues() {
        final UnivariateFunction f = new Sigmoid();

        Assert.assertEquals(0.5, f.value(0), EPS);
        Assert.assertEquals(0, f.value(Double.NEGATIVE_INFINITY), EPS);
        Assert.assertEquals(1, f.value(Double.POSITIVE_INFINITY), EPS);
    }

// org.apache.commons.math3.analysis.function.SigmoidTest::testDerivative
    public void testDerivative() {
        final Sigmoid f = new Sigmoid();
        final DerivativeStructure f0 = f.value(new DerivativeStructure(1, 1, 0, 0.0));

        Assert.assertEquals(0.25, f0.getPartialDerivative(1), 0);
    }

// org.apache.commons.math3.analysis.function.SigmoidTest::testDerivativesHighOrder
    public void testDerivativesHighOrder() {
        DerivativeStructure s = new Sigmoid(1, 3).value(new DerivativeStructure(1, 5, 0, 1.2));
        Assert.assertEquals(2.5370495669980352859, s.getPartialDerivative(0), 5.0e-16);
        Assert.assertEquals(0.35578888129361140441, s.getPartialDerivative(1), 6.0e-17);
        Assert.assertEquals(-0.19107626464144938116,  s.getPartialDerivative(2), 6.0e-17);
        Assert.assertEquals(-0.02396830286286711696,  s.getPartialDerivative(3), 4.0e-17);
        Assert.assertEquals(0.21682059798981049049,   s.getPartialDerivative(4), 3.0e-17);
        Assert.assertEquals(-0.19186320234632658055,  s.getPartialDerivative(5), 2.0e-16);
    }

// org.apache.commons.math3.analysis.function.SigmoidTest::testDerivativeLargeArguments
    public void testDerivativeLargeArguments() {
        final Sigmoid f = new Sigmoid(1, 2);

        Assert.assertEquals(0, f.value(new DerivativeStructure(1, 1, 0, Double.NEGATIVE_INFINITY)).getPartialDerivative(1), 0);
        Assert.assertEquals(0, f.value(new DerivativeStructure(1, 1, 0, -Double.MAX_VALUE)).getPartialDerivative(1), 0);
        Assert.assertEquals(0, f.value(new DerivativeStructure(1, 1, 0, -1e50)).getPartialDerivative(1), 0);
        Assert.assertEquals(0, f.value(new DerivativeStructure(1, 1, 0, -1e3)).getPartialDerivative(1), 0);
        Assert.assertEquals(0, f.value(new DerivativeStructure(1, 1, 0, 1e3)).getPartialDerivative(1), 0);
        Assert.assertEquals(0, f.value(new DerivativeStructure(1, 1, 0, 1e50)).getPartialDerivative(1), 0);
        Assert.assertEquals(0, f.value(new DerivativeStructure(1, 1, 0, Double.MAX_VALUE)).getPartialDerivative(1), 0);
        Assert.assertEquals(0, f.value(new DerivativeStructure(1, 1, 0, Double.POSITIVE_INFINITY)).getPartialDerivative(1), 0);        
    }

// org.apache.commons.math3.analysis.function.SigmoidTest::testParametricUsage1
    public void testParametricUsage1() {
        final Sigmoid.Parametric g = new Sigmoid.Parametric();
        g.value(0, null);
    }

// org.apache.commons.math3.analysis.function.SigmoidTest::testParametricUsage2
    public void testParametricUsage2() {
        final Sigmoid.Parametric g = new Sigmoid.Parametric();
        g.value(0, new double[] {0});
    }

// org.apache.commons.math3.analysis.function.SigmoidTest::testParametricUsage3
    public void testParametricUsage3() {
        final Sigmoid.Parametric g = new Sigmoid.Parametric();
        g.gradient(0, null);
    }

// org.apache.commons.math3.analysis.function.SigmoidTest::testParametricUsage4
    public void testParametricUsage4() {
        final Sigmoid.Parametric g = new Sigmoid.Parametric();
        g.gradient(0, new double[] {0});
    }

// org.apache.commons.math3.analysis.function.SigmoidTest::testParametricValue
    public void testParametricValue() {
        final double lo = 2;
        final double hi = 3;
        final Sigmoid f = new Sigmoid(lo, hi);

        final Sigmoid.Parametric g = new Sigmoid.Parametric();
        Assert.assertEquals(f.value(-1), g.value(-1, new double[] {lo, hi}), 0);
        Assert.assertEquals(f.value(0), g.value(0, new double[] {lo, hi}), 0);
        Assert.assertEquals(f.value(2), g.value(2, new double[] {lo, hi}), 0);
    }

// org.apache.commons.math3.analysis.function.SincTest::testShortcut
   public void testShortcut() {
       final Sinc s = new Sinc();
       final UnivariateFunction f = new UnivariateFunction() {
           public double value(double x) {
               Dfp dfpX = new DfpField(25).newDfp(x);
               return DfpMath.sin(dfpX).divide(dfpX).toDouble();
           }
       };

       for (double x = 1e-30; x < 1e10; x *= 2) {
           final double fX = f.value(x);
           final double sX = s.value(x);
           Assert.assertEquals("x=" + x, fX, sX, 2.0e-16);
       }
   }

// org.apache.commons.math3.analysis.function.SincTest::testCrossings
   public void testCrossings() {
       final Sinc s = new Sinc(true);
       final int numCrossings = 1000;
       final double tol = 2e-16;
       for (int i = 1; i <= numCrossings; i++) {
           Assert.assertEquals("i=" + i, 0, s.value(i), tol);
       }
   }

// org.apache.commons.math3.analysis.function.SincTest::testZero
   public void testZero() {
       final Sinc s = new Sinc();
       Assert.assertEquals(1d, s.value(0), 0);
   }

// org.apache.commons.math3.analysis.function.SincTest::testEuler
   public void testEuler() {
       final Sinc s = new Sinc();
       final double x = 123456.789;
       double prod = 1;
       double xOverPow2 = x / 2;
       while (xOverPow2 > 0) {
           prod *= FastMath.cos(xOverPow2);
           xOverPow2 /= 2;
       }
       Assert.assertEquals(prod, s.value(x), 1e-13);
   }

// org.apache.commons.math3.analysis.function.SincTest::testDerivativeZero
   public void testDerivativeZero() {
       final DerivativeStructure s0 = new Sinc(true).value(new DerivativeStructure(1, 1, 0, 0.0));
       Assert.assertEquals(0, s0.getPartialDerivative(1), 0);
   }

// org.apache.commons.math3.analysis.function.SincTest::testDerivatives1Dot2Unnormalized
   public void testDerivatives1Dot2Unnormalized() {
       DerivativeStructure s = new Sinc(false).value(new DerivativeStructure(1, 5, 0, 1.2));
       Assert.assertEquals( 0.77669923830602195806, s.getPartialDerivative(0), 1.0e-16);
       Assert.assertEquals(-0.34528456985779031701, s.getPartialDerivative(1), 1.0e-16);
       Assert.assertEquals(-0.2012249552097047631,  s.getPartialDerivative(2), 1.0e-16);
       Assert.assertEquals( 0.2010975926270339262,  s.getPartialDerivative(3), 4.0e-16);
       Assert.assertEquals( 0.106373929549242204,   s.getPartialDerivative(4), 1.0e-15);
       Assert.assertEquals(-0.1412599110579478695,  s.getPartialDerivative(5), 3.0e-15);
   }

// org.apache.commons.math3.analysis.function.SincTest::testDerivatives1Dot2Normalized
   public void testDerivatives1Dot2Normalized() {
       DerivativeStructure s = new Sinc(true).value(new DerivativeStructure(1, 5, 0, 1.2));
       Assert.assertEquals(-0.15591488063143983888, s.getPartialDerivative(0), 6.0e-17);
       Assert.assertEquals(-0.54425176145292298767, s.getPartialDerivative(1), 2.0e-16);
       Assert.assertEquals(2.4459044611635856107,   s.getPartialDerivative(2), 9.0e-16);
       Assert.assertEquals(0.5391369206235909586,   s.getPartialDerivative(3), 7.0e-16);
       Assert.assertEquals(-16.984649869728849865,  s.getPartialDerivative(4), 8.0e-15);
       Assert.assertEquals(5.0980327462666316586,   s.getPartialDerivative(5), 9.0e-15);
   }

// org.apache.commons.math3.analysis.function.SincTest::testDerivativeShortcut
   public void testDerivativeShortcut() {
       final Sinc sinc = new Sinc();
       final UnivariateFunction f = new UnivariateFunction() {
               public double value(double x) {
                   Dfp dfpX = new DfpField(25).newDfp(x);
                   return DfpMath.cos(dfpX).subtract(DfpMath.sin(dfpX).divide(dfpX)).divide(dfpX).toDouble();
               }
           };

       for (double x = 1e-30; x < 1e10; x *= 2) {
           final double fX = f.value(x);
           final DerivativeStructure sX = sinc.value(new DerivativeStructure(1, 1, 0, x));
           Assert.assertEquals("x=" + x, fX, sX.getPartialDerivative(1), 3.0e-13);
       }
   }

// org.apache.commons.math3.analysis.function.SqrtTest::testComparison
   public void testComparison() {
       final Sqrt s = new Sqrt();
       final UnivariateFunction f = new UnivariateFunction() {
               public double value(double x) {
                   return Math.sqrt(x);
               }
           };

       for (double x = 1e-30; x < 1e10; x *= 2) {
           final double fX = f.value(x);
           final double sX = s.value(x);
           Assert.assertEquals("x=" + x, fX, sX, 0);
       }
   }

// org.apache.commons.math3.analysis.function.SqrtTest::testDerivativeComparison
   public void testDerivativeComparison() {
       final UnivariateDifferentiableFunction sPrime = new Sqrt();
       final UnivariateFunction f = new UnivariateFunction() {
               public double value(double x) {
                   return 1 / (2 * Math.sqrt(x));
               }
           };

       for (double x = 1e-30; x < 1e10; x *= 2) {
           final double fX = f.value(x);
           final double sX = sPrime.value(new DerivativeStructure(1, 1, 0, x)).getPartialDerivative(1);
           Assert.assertEquals("x=" + x, fX, sX, FastMath.ulp(fX));
       }
   }

// org.apache.commons.math3.analysis.function.SqrtTest::testDerivativesHighOrder
   public void testDerivativesHighOrder() {
       DerivativeStructure s = new Sqrt().value(new DerivativeStructure(1, 5, 0, 1.2));
       Assert.assertEquals(1.0954451150103322269, s.getPartialDerivative(0), 1.0e-16);
       Assert.assertEquals(0.45643546458763842789, s.getPartialDerivative(1), 1.0e-16);
       Assert.assertEquals(-0.1901814435781826783,  s.getPartialDerivative(2), 1.0e-16);
       Assert.assertEquals(0.23772680447272834785,  s.getPartialDerivative(3), 1.0e-16);
       Assert.assertEquals(-0.49526417598485072465,   s.getPartialDerivative(4), 1.0e-16);
       Assert.assertEquals(1.4445205132891479465,  s.getPartialDerivative(5), 5.0e-16);
   }

// org.apache.commons.math3.analysis.interpolation.HermiteInterpolatorTest::testZero
    public void testZero() {
        HermiteInterpolator interpolator = new HermiteInterpolator();
        interpolator.addSamplePoint(0.0, new double[] { 0.0 });
        for (double x = -10; x < 10; x += 1.0) {
            DerivativeStructure y = interpolator.value(new DerivativeStructure(1, 1, 0, x))[0];
            Assert.assertEquals(0.0, y.getValue(), 1.0e-15);
            Assert.assertEquals(0.0, y.getPartialDerivative(1), 1.0e-15);
        }
        checkPolynomial(new PolynomialFunction(new double[] { 0.0 }),
                        interpolator.getPolynomials()[0]);
    }

// org.apache.commons.math3.analysis.interpolation.HermiteInterpolatorTest::testQuadratic
    public void testQuadratic() {
        HermiteInterpolator interpolator = new HermiteInterpolator();
        interpolator.addSamplePoint(0.0, new double[] { 2.0 });
        interpolator.addSamplePoint(1.0, new double[] { 0.0 });
        interpolator.addSamplePoint(2.0, new double[] { 0.0 });
        for (double x = -10; x < 10; x += 1.0) {
            DerivativeStructure y = interpolator.value(new DerivativeStructure(1, 1, 0, x))[0];
            Assert.assertEquals((x - 1.0) * (x - 2.0), y.getValue(), 1.0e-15);
            Assert.assertEquals(2 * x - 3.0, y.getPartialDerivative(1), 1.0e-15);
        }
        checkPolynomial(new PolynomialFunction(new double[] { 2.0, -3.0, 1.0 }),
                        interpolator.getPolynomials()[0]);
    }

// org.apache.commons.math3.analysis.interpolation.HermiteInterpolatorTest::testMixedDerivatives
    public void testMixedDerivatives() {
        HermiteInterpolator interpolator = new HermiteInterpolator();
        interpolator.addSamplePoint(0.0, new double[] { 1.0 }, new double[] { 2.0 });
        interpolator.addSamplePoint(1.0, new double[] { 4.0 });
        interpolator.addSamplePoint(2.0, new double[] { 5.0 }, new double[] { 2.0 });
        Assert.assertEquals(4, interpolator.getPolynomials()[0].degree());
        DerivativeStructure y0 = interpolator.value(new DerivativeStructure(1, 1, 0, 0.0))[0];
        Assert.assertEquals(1.0, y0.getValue(), 1.0e-15);
        Assert.assertEquals(2.0, y0.getPartialDerivative(1), 1.0e-15);
        Assert.assertEquals(4.0, interpolator.value(1.0)[0], 1.0e-15);
        DerivativeStructure y2 = interpolator.value(new DerivativeStructure(1, 1, 0, 2.0))[0];
        Assert.assertEquals(5.0, y2.getValue(), 1.0e-15);
        Assert.assertEquals(2.0, y2.getPartialDerivative(1), 1.0e-15);
        checkPolynomial(new PolynomialFunction(new double[] { 1.0, 2.0, 4.0, -4.0, 1.0 }),
                        interpolator.getPolynomials()[0]);
    }

// org.apache.commons.math3.analysis.interpolation.HermiteInterpolatorTest::testRandomPolynomialsValuesOnly
    public void testRandomPolynomialsValuesOnly() {

        Random random = new Random(0x42b1e7dbd361a932l);

        for (int i = 0; i < 100; ++i) {

            int maxDegree = 0;
            PolynomialFunction[] p = new PolynomialFunction[5];
            for (int k = 0; k < p.length; ++k) {
                int degree = random.nextInt(7);
                p[k] = randomPolynomial(degree, random);
                maxDegree = FastMath.max(maxDegree, degree);
            }

            HermiteInterpolator interpolator = new HermiteInterpolator();
            for (int j = 0; j < 1 + maxDegree; ++j) {
                double x = 0.1 * j;
                double[] values = new double[p.length];
                for (int k = 0; k < p.length; ++k) {
                    values[k] = p[k].value(x);
                }
                interpolator.addSamplePoint(x, values);
            }

            for (double x = 0; x < 2; x += 0.1) {
                double[] values = interpolator.value(x);
                Assert.assertEquals(p.length, values.length);
                for (int k = 0; k < p.length; ++k) {
                    Assert.assertEquals(p[k].value(x), values[k], 1.0e-8 * FastMath.abs(p[k].value(x)));
                }
            }

            PolynomialFunction[] result = interpolator.getPolynomials();
            for (int k = 0; k < p.length; ++k) {
                checkPolynomial(p[k], result[k]);
            }

        }
    }

// org.apache.commons.math3.analysis.interpolation.HermiteInterpolatorTest::testRandomPolynomialsFirstDerivative
    public void testRandomPolynomialsFirstDerivative() {

        Random random = new Random(0x570803c982ca5d3bl);

        for (int i = 0; i < 100; ++i) {

            int maxDegree = 0;
            PolynomialFunction[] p      = new PolynomialFunction[5];
            PolynomialFunction[] pPrime = new PolynomialFunction[5];
            for (int k = 0; k < p.length; ++k) {
                int degree = random.nextInt(7);
                p[k]      = randomPolynomial(degree, random);
                pPrime[k] = p[k].polynomialDerivative();
                maxDegree = FastMath.max(maxDegree, degree);
            }

            HermiteInterpolator interpolator = new HermiteInterpolator();
            for (int j = 0; j < 1 + maxDegree / 2; ++j) {
                double x = 0.1 * j;
                double[] values      = new double[p.length];
                double[] derivatives = new double[p.length];
                for (int k = 0; k < p.length; ++k) {
                    values[k]      = p[k].value(x);
                    derivatives[k] = pPrime[k].value(x);
                }
                interpolator.addSamplePoint(x, values, derivatives);
            }

            for (double x = 0; x < 2; x += 0.1) {
                DerivativeStructure[] y = interpolator.value(new DerivativeStructure(1, 1, 0, x));
                Assert.assertEquals(p.length, y.length);
                for (int k = 0; k < p.length; ++k) {
                    Assert.assertEquals(p[k].value(x), y[k].getValue(), 1.0e-8 * FastMath.abs(p[k].value(x)));
                    Assert.assertEquals(pPrime[k].value(x), y[k].getPartialDerivative(1), 4.0e-8 * FastMath.abs(p[k].value(x)));
                }
            }

            PolynomialFunction[] result = interpolator.getPolynomials();
            for (int k = 0; k < p.length; ++k) {
                checkPolynomial(p[k], result[k]);
            }

        }
    }

// org.apache.commons.math3.analysis.interpolation.HermiteInterpolatorTest::testSine
    public void testSine() {
        HermiteInterpolator interpolator = new HermiteInterpolator();
        for (double x = 0; x < FastMath.PI; x += 0.5) {
            interpolator.addSamplePoint(x, new double[] { FastMath.sin(x) });
        }
        for (double x = 0.1; x <= 2.9; x += 0.01) {
            DerivativeStructure y = interpolator.value(new DerivativeStructure(1, 2, 0, x))[0];
            Assert.assertEquals( FastMath.sin(x), y.getValue(), 3.5e-5);
            Assert.assertEquals( FastMath.cos(x), y.getPartialDerivative(1), 1.3e-4);
            Assert.assertEquals(-FastMath.sin(x), y.getPartialDerivative(2), 2.9e-3);
        }
    }

// org.apache.commons.math3.analysis.interpolation.HermiteInterpolatorTest::testSquareRoot
    public void testSquareRoot() {
        HermiteInterpolator interpolator = new HermiteInterpolator();
        for (double x = 1.0; x < 3.6; x += 0.5) {
            interpolator.addSamplePoint(x, new double[] { FastMath.sqrt(x) });
        }
        for (double x = 1.1; x < 3.5; x += 0.01) {
            DerivativeStructure y = interpolator.value(new DerivativeStructure(1, 1, 0, x))[0];
            Assert.assertEquals(FastMath.sqrt(x), y.getValue(), 1.5e-4);
            Assert.assertEquals(0.5 / FastMath.sqrt(x), y.getPartialDerivative(1), 8.5e-4);
        }
    }

// org.apache.commons.math3.analysis.interpolation.HermiteInterpolatorTest::testWikipedia
    public void testWikipedia() {
        
        
        HermiteInterpolator interpolator = new HermiteInterpolator();
        interpolator.addSamplePoint(-1, new double[] { 2 }, new double[] { -8 }, new double[] { 56 });
        interpolator.addSamplePoint( 0, new double[] { 1 }, new double[] {  0 }, new double[] {  0 });
        interpolator.addSamplePoint( 1, new double[] { 2 }, new double[] {  8 }, new double[] { 56 });
        for (double x = -1.0; x <= 1.0; x += 0.125) {
            DerivativeStructure y = interpolator.value(new DerivativeStructure(1, 1, 0, x))[0];
            double x2 = x * x;
            double x4 = x2 * x2;
            double x8 = x4 * x4;
            Assert.assertEquals(x8 + 1, y.getValue(), 1.0e-15);
            Assert.assertEquals(8 * x4 * x2 * x, y.getPartialDerivative(1), 1.0e-15);
        }
        checkPolynomial(new PolynomialFunction(new double[] { 1, 0, 0, 0, 0, 0, 0, 0, 1 }),
                        interpolator.getPolynomials()[0]);
    }

// org.apache.commons.math3.analysis.interpolation.HermiteInterpolatorTest::testOnePointParabola
    public void testOnePointParabola() {
        HermiteInterpolator interpolator = new HermiteInterpolator();
        interpolator.addSamplePoint(0, new double[] { 1 }, new double[] { 1 }, new double[] { 2 });
        for (double x = -1.0; x <= 1.0; x += 0.125) {
            DerivativeStructure y = interpolator.value(new DerivativeStructure(1, 1, 0, x))[0];
            Assert.assertEquals(1 + x * (1 + x), y.getValue(), 1.0e-15);
            Assert.assertEquals(1 + 2 * x, y.getPartialDerivative(1), 1.0e-15);
        }
        checkPolynomial(new PolynomialFunction(new double[] { 1, 1, 1 }),
                        interpolator.getPolynomials()[0]);
    }

// org.apache.commons.math3.analysis.interpolation.HermiteInterpolatorTest::testEmptySample
    public void testEmptySample() {
        new HermiteInterpolator().value(0.0);
    }

// org.apache.commons.math3.analysis.interpolation.HermiteInterpolatorTest::testDuplicatedAbscissa
    public void testDuplicatedAbscissa() {
        HermiteInterpolator interpolator = new HermiteInterpolator();
        interpolator.addSamplePoint(1.0, new double[] { 0.0 });
        interpolator.addSamplePoint(1.0, new double[] { 1.0 });
    }

// org.apache.commons.math3.analysis.polynomials.PolynomialFunctionNewtonFormTest::testLinearFunction
    public void testLinearFunction() {
        PolynomialFunctionNewtonForm p;
        double coefficients[], z, expected, result, tolerance = 1E-12;

        
        double a[] = { 2.0, 1.5 };
        double c[] = { 4.0 };
        p = new PolynomialFunctionNewtonForm(a, c);

        z = 2.0; expected = -1.0; result = p.value(z);
        Assert.assertEquals(expected, result, tolerance);

        z = 4.5; expected = 2.75; result = p.value(z);
        Assert.assertEquals(expected, result, tolerance);

        z = 6.0; expected = 5.0; result = p.value(z);
        Assert.assertEquals(expected, result, tolerance);

        Assert.assertEquals(1, p.degree());

        coefficients = p.getCoefficients();
        Assert.assertEquals(2, coefficients.length);
        Assert.assertEquals(-4.0, coefficients[0], tolerance);
        Assert.assertEquals(1.5, coefficients[1], tolerance);
    }

// org.apache.commons.math3.analysis.polynomials.PolynomialFunctionNewtonFormTest::testQuadraticFunction
    public void testQuadraticFunction() {
        PolynomialFunctionNewtonForm p;
        double coefficients[], z, expected, result, tolerance = 1E-12;

        
        double a[] = { 4.0, 3.0, 2.0 };
        double c[] = { 1.0, -2.0 };
        p = new PolynomialFunctionNewtonForm(a, c);

        z = 1.0; expected = 4.0; result = p.value(z);
        Assert.assertEquals(expected, result, tolerance);

        z = 2.5; expected = 22.0; result = p.value(z);
        Assert.assertEquals(expected, result, tolerance);

        z = -2.0; expected = -5.0; result = p.value(z);
        Assert.assertEquals(expected, result, tolerance);

        Assert.assertEquals(2, p.degree());

        coefficients = p.getCoefficients();
        Assert.assertEquals(3, coefficients.length);
        Assert.assertEquals(-3.0, coefficients[0], tolerance);
        Assert.assertEquals(5.0, coefficients[1], tolerance);
        Assert.assertEquals(2.0, coefficients[2], tolerance);
    }

// org.apache.commons.math3.analysis.polynomials.PolynomialFunctionNewtonFormTest::testQuinticFunction
    public void testQuinticFunction() {
        PolynomialFunctionNewtonForm p;
        double coefficients[], z, expected, result, tolerance = 1E-12;

        
        
        double a[] = { 0.0, 6.0, -6.0, -6.0, 1.0, 1.0 };
        double c[] = { 0.0, 0.0, 1.0, -1.0, 2.0 };
        p = new PolynomialFunctionNewtonForm(a, c);

        z = 0.0; expected = 0.0; result = p.value(z);
        Assert.assertEquals(expected, result, tolerance);

        z = -2.0; expected = 0.0; result = p.value(z);
        Assert.assertEquals(expected, result, tolerance);

        z = 4.0; expected = 360.0; result = p.value(z);
        Assert.assertEquals(expected, result, tolerance);

        Assert.assertEquals(5, p.degree());

        coefficients = p.getCoefficients();
        Assert.assertEquals(6, coefficients.length);
        Assert.assertEquals(0.0, coefficients[0], tolerance);
        Assert.assertEquals(6.0, coefficients[1], tolerance);
        Assert.assertEquals(1.0, coefficients[2], tolerance);
        Assert.assertEquals(-7.0, coefficients[3], tolerance);
        Assert.assertEquals(-1.0, coefficients[4], tolerance);
        Assert.assertEquals(1.0, coefficients[5], tolerance);
    }

// org.apache.commons.math3.analysis.polynomials.PolynomialFunctionNewtonFormTest::testDerivative
    public void testDerivative() {

        
        PolynomialFunctionNewtonForm p =
                new PolynomialFunctionNewtonForm(new double[] { 0, 1, 3, 1 },
                                                 new double[] { 0, 1, 2 });

        double eps = 2.0e-14;
        for (double t = 0.0; t < 10.0; t += 0.1) {
            DerivativeStructure x = new DerivativeStructure(1, 4, 0, t);
            DerivativeStructure y = p.value(x);
            Assert.assertEquals(t * t * t,   y.getValue(),              eps * t * t * t);
            Assert.assertEquals(3.0 * t * t, y.getPartialDerivative(1), eps * 3.0 * t * t);
            Assert.assertEquals(6.0 * t,     y.getPartialDerivative(2), eps * 6.0 * t);
            Assert.assertEquals(6.0,         y.getPartialDerivative(3), eps * 6.0);
            Assert.assertEquals(0.0,         y.getPartialDerivative(4), eps);
        }

    }

// org.apache.commons.math3.analysis.polynomials.PolynomialFunctionNewtonFormTest::testParameters
    public void testParameters() {

        try {
            
            double a[] = { 1.0 };
            double c[] = { 2.0 };
            new PolynomialFunctionNewtonForm(a, c);
            Assert.fail("Expecting MathIllegalArgumentException - bad input array length");
        } catch (MathIllegalArgumentException ex) {
            
        }
        try {
            
            double a[] = { 1.0, 2.0, 3.0, 4.0 };
            double c[] = { 4.0, 3.0, 2.0, 1.0 };
            new PolynomialFunctionNewtonForm(a, c);
            Assert.fail("Expecting MathIllegalArgumentException - mismatch input arrays");
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math3.analysis.solvers.BracketingNthOrderBrentSolverTest::testInsufficientOrder1
    public void testInsufficientOrder1() {
        new BracketingNthOrderBrentSolver(1.0e-10, 1);
    }

// org.apache.commons.math3.analysis.solvers.BracketingNthOrderBrentSolverTest::testInsufficientOrder2
    public void testInsufficientOrder2() {
        new BracketingNthOrderBrentSolver(1.0e-10, 1.0e-10, 1);
    }

// org.apache.commons.math3.analysis.solvers.BracketingNthOrderBrentSolverTest::testInsufficientOrder3
    public void testInsufficientOrder3() {
        new BracketingNthOrderBrentSolver(1.0e-10, 1.0e-10, 1.0e-10, 1);
    }

// org.apache.commons.math3.analysis.solvers.BracketingNthOrderBrentSolverTest::testConstructorsOK
    public void testConstructorsOK() {
        Assert.assertEquals(2, new BracketingNthOrderBrentSolver(1.0e-10, 2).getMaximalOrder());
        Assert.assertEquals(2, new BracketingNthOrderBrentSolver(1.0e-10, 1.0e-10, 2).getMaximalOrder());
        Assert.assertEquals(2, new BracketingNthOrderBrentSolver(1.0e-10, 1.0e-10, 1.0e-10, 2).getMaximalOrder());
    }

// org.apache.commons.math3.analysis.solvers.BracketingNthOrderBrentSolverTest::testConvergenceOnFunctionAccuracy
    public void testConvergenceOnFunctionAccuracy() {
        BracketingNthOrderBrentSolver solver =
                new BracketingNthOrderBrentSolver(1.0e-12, 1.0e-10, 0.001, 3);
        QuinticFunction f = new QuinticFunction();
        double result = solver.solve(20, f, 0.2, 0.9, 0.4, AllowedSolution.BELOW_SIDE);
        Assert.assertEquals(0, f.value(result), solver.getFunctionValueAccuracy());
        Assert.assertTrue(f.value(result) <= 0);
        Assert.assertTrue(result - 0.5 > solver.getAbsoluteAccuracy());
        result = solver.solve(20, f, -0.9, -0.2,  -0.4, AllowedSolution.ABOVE_SIDE);
        Assert.assertEquals(0, f.value(result), solver.getFunctionValueAccuracy());
        Assert.assertTrue(f.value(result) >= 0);
        Assert.assertTrue(result + 0.5 < -solver.getAbsoluteAccuracy());
    }

// org.apache.commons.math3.analysis.solvers.BracketingNthOrderBrentSolverTest::testIssue716
    public void testIssue716() {
        BracketingNthOrderBrentSolver solver =
                new BracketingNthOrderBrentSolver(1.0e-12, 1.0e-10, 1.0e-22, 5);
        UnivariateFunction sharpTurn = new UnivariateFunction() {
            public double value(double x) {
                return (2 * x + 1) / (1.0e9 * (x + 1));
            }
        };
        double result = solver.solve(100, sharpTurn, -0.9999999, 30, 15, AllowedSolution.RIGHT_SIDE);
        Assert.assertEquals(0, sharpTurn.value(result), solver.getFunctionValueAccuracy());
        Assert.assertTrue(sharpTurn.value(result) >= 0);
        Assert.assertEquals(-0.5, result, 1.0e-10);
    }

// org.apache.commons.math3.analysis.solvers.BracketingNthOrderBrentSolverTest::testFasterThanNewton
    public void testFasterThanNewton() {
        
        
        
        
        
        
        compare(new TestFunction(0.0, -2, 2) {
            @Override
            public DerivativeStructure value(DerivativeStructure x) {
                return x.sin().subtract(x.multiply(0.5));
            }
        });
        compare(new TestFunction(6.3087771299726890947, -5, 10) {
            @Override
            public DerivativeStructure value(DerivativeStructure x) {
                return x.pow(5).add(x).subtract(10000);
            }
        });
        compare(new TestFunction(9.6335955628326951924, 0.001, 10) {
            @Override
            public DerivativeStructure value(DerivativeStructure x) {
                return x.sqrt().subtract(x.reciprocal()).subtract(3);
            }
        });
        compare(new TestFunction(2.8424389537844470678, -5, 5) {
            @Override
            public DerivativeStructure value(DerivativeStructure x) {
                return x.exp().add(x).subtract(20);
            }
        });
        compare(new TestFunction(8.3094326942315717953, 0.001, 10) {
            @Override
            public DerivativeStructure value(DerivativeStructure x) {
                return x.log().add(x.sqrt()).subtract(5);
            }
        });
        compare(new TestFunction(1.4655712318767680266, -0.5, 1.5) {
            @Override
            public DerivativeStructure value(DerivativeStructure x) {
                return x.subtract(1).multiply(x).multiply(x).subtract(1);
            }
        });

    }

// org.apache.commons.math3.analysis.solvers.BrentSolverTest::testSinZero
    public void testSinZero() {
        
        
        
        UnivariateFunction f = new Sin();
        double result;
        UnivariateSolver solver = new BrentSolver();
        
        result = solver.solve(100, f, 3, 4);
        
        
        Assert.assertEquals(result, FastMath.PI, solver.getAbsoluteAccuracy());
        Assert.assertTrue(solver.getEvaluations() <= 7);
        
        result = solver.solve(100, f, 1, 4);
        
        
        Assert.assertEquals(result, FastMath.PI, solver.getAbsoluteAccuracy());
        Assert.assertTrue(solver.getEvaluations() <= 8);
    }

// org.apache.commons.math3.analysis.solvers.BrentSolverTest::testQuinticZero
    public void testQuinticZero() {
        
        
        
        
        
        
        
        UnivariateFunction f = new QuinticFunction();
        double result;
        
        UnivariateSolver solver = new BrentSolver();
        
        
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
            Assert.fail("Expected TooManyEvaluationsException");
        } catch (TooManyEvaluationsException e) {
            
        }
    }

// org.apache.commons.math3.analysis.solvers.BrentSolverTest::testRootEndpoints
    public void testRootEndpoints() {
        UnivariateFunction f = new Sin();
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

// org.apache.commons.math3.analysis.solvers.BrentSolverTest::testBadEndpoints
    public void testBadEndpoints() {
        UnivariateFunction f = new Sin();
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

// org.apache.commons.math3.analysis.solvers.BrentSolverTest::testInitialGuess
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

// org.apache.commons.math3.analysis.solvers.BrentSolverTest::testMath832
    public void testMath832() {
        final UnivariateFunction f = new UnivariateFunction() {
                private final UnivariateDifferentiableFunction sqrt = new Sqrt();
                private final UnivariateDifferentiableFunction inv = new Inverse();
                private final UnivariateDifferentiableFunction func
                    = FunctionUtils.add(FunctionUtils.multiply(new Constant(1e2), sqrt),
                                        FunctionUtils.multiply(new Constant(1e6), inv),
                                        FunctionUtils.multiply(new Constant(1e4),
                                                               FunctionUtils.compose(inv, sqrt)));

                public double value(double x) {
                    return func.value(new DerivativeStructure(1, 1, 0, x)).getPartialDerivative(1);
                }

            };

        BrentSolver solver = new BrentSolver();
        final double result = solver.solve(99, f, 1, 1e30, 1 + 1e-10);
        Assert.assertEquals(804.93558250, result, 1e-8);
    }

// org.apache.commons.math3.analysis.solvers.NewtonRaphsonSolverTest::testSinZero
    public void testSinZero() {
        UnivariateDifferentiableFunction f = new Sin();
        double result;

        NewtonRaphsonSolver solver = new NewtonRaphsonSolver();
        result = solver.solve(100, f, 3, 4);
        Assert.assertEquals(result, FastMath.PI, solver.getAbsoluteAccuracy());

        result = solver.solve(100, f, 1, 4);
        Assert.assertEquals(result, FastMath.PI, solver.getAbsoluteAccuracy());

        Assert.assertTrue(solver.getEvaluations() > 0);
    }

// org.apache.commons.math3.analysis.solvers.NewtonRaphsonSolverTest::testQuinticZero
    public void testQuinticZero() {
        final UnivariateDifferentiableFunction f = new QuinticFunction();
        double result;

        NewtonRaphsonSolver solver = new NewtonRaphsonSolver();
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

// org.apache.commons.math3.analysis.solvers.NewtonSolverTest::testSinZero
    public void testSinZero() {
        DifferentiableUnivariateFunction f = new Sin();
        double result;

        NewtonSolver solver = new NewtonSolver();
        result = solver.solve(100, f, 3, 4);
        Assert.assertEquals(result, FastMath.PI, solver.getAbsoluteAccuracy());

        result = solver.solve(100, f, 1, 4);
        Assert.assertEquals(result, FastMath.PI, solver.getAbsoluteAccuracy());

        Assert.assertTrue(solver.getEvaluations() > 0);
    }

// org.apache.commons.math3.analysis.solvers.NewtonSolverTest::testQuinticZero
    public void testQuinticZero() {
        final UnivariateDifferentiableFunction q = new QuinticFunction();
        DifferentiableUnivariateFunction f = new DifferentiableUnivariateFunction() {

            public double value(double x) {
                return q.value(x);
            }

            public UnivariateFunction derivative() {
                return new UnivariateFunction() {
                    public double value(double x) {
                        return q.value(new DerivativeStructure(1, 1, 0, x)).getPartialDerivative(1);
                    }
                };
            }

        };
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

// org.apache.commons.math3.geometry.euclidean.threed.SphericalCoordinatesTest::testCoordinatesStoC
    public void testCoordinatesStoC() throws DimensionMismatchException {
        double piO2 = 0.5 * FastMath.PI;
        SphericalCoordinates sc1 = new SphericalCoordinates(2.0, 0, piO2);
        Assert.assertEquals(0, sc1.getCartesian().distance(new Vector3D(2, 0, 0)), 1.0e-10);
        SphericalCoordinates sc2 = new SphericalCoordinates(2.0, piO2, piO2);
        Assert.assertEquals(0, sc2.getCartesian().distance(new Vector3D(0, 2, 0)), 1.0e-10);
        SphericalCoordinates sc3 = new SphericalCoordinates(2.0, FastMath.PI, piO2);
        Assert.assertEquals(0, sc3.getCartesian().distance(new Vector3D(-2, 0, 0)), 1.0e-10);
        SphericalCoordinates sc4 = new SphericalCoordinates(2.0, -piO2, piO2);
        Assert.assertEquals(0, sc4.getCartesian().distance(new Vector3D(0, -2, 0)), 1.0e-10);
        SphericalCoordinates sc5 = new SphericalCoordinates(2.0, 1.23456, 0);
        Assert.assertEquals(0, sc5.getCartesian().distance(new Vector3D(0, 0, 2)), 1.0e-10);
        SphericalCoordinates sc6 = new SphericalCoordinates(2.0, 6.54321, FastMath.PI);
        Assert.assertEquals(0, sc6.getCartesian().distance(new Vector3D(0, 0, -2)), 1.0e-10);
    }

// org.apache.commons.math3.geometry.euclidean.threed.SphericalCoordinatesTest::testCoordinatesCtoS
    public void testCoordinatesCtoS() throws DimensionMismatchException {
        double piO2 = 0.5 * FastMath.PI;
        SphericalCoordinates sc1 = new SphericalCoordinates(new Vector3D(2, 0, 0));
        Assert.assertEquals(2,           sc1.getR(),     1.0e-10);
        Assert.assertEquals(0,           sc1.getTheta(), 1.0e-10);
        Assert.assertEquals(piO2,        sc1.getPhi(),   1.0e-10);
        SphericalCoordinates sc2 = new SphericalCoordinates(new Vector3D(0, 2, 0));
        Assert.assertEquals(2,           sc2.getR(),     1.0e-10);
        Assert.assertEquals(piO2,        sc2.getTheta(), 1.0e-10);
        Assert.assertEquals(piO2,        sc2.getPhi(),   1.0e-10);
        SphericalCoordinates sc3 = new SphericalCoordinates(new Vector3D(-2, 0, 0));
        Assert.assertEquals(2,           sc3.getR(),     1.0e-10);
        Assert.assertEquals(FastMath.PI, sc3.getTheta(), 1.0e-10);
        Assert.assertEquals(piO2,        sc3.getPhi(),   1.0e-10);
        SphericalCoordinates sc4 = new SphericalCoordinates(new Vector3D(0, -2, 0));
        Assert.assertEquals(2,           sc4.getR(),     1.0e-10);
        Assert.assertEquals(-piO2,       sc4.getTheta(), 1.0e-10);
        Assert.assertEquals(piO2,        sc4.getPhi(),   1.0e-10);
        SphericalCoordinates sc5 = new SphericalCoordinates(new Vector3D(0, 0, 2));
        Assert.assertEquals(2,           sc5.getR(),     1.0e-10);
        
        Assert.assertEquals(0,           sc5.getPhi(),   1.0e-10);
        SphericalCoordinates sc6 = new SphericalCoordinates(new Vector3D(0, 0, -2));
        Assert.assertEquals(2,           sc6.getR(),     1.0e-10);
        
        Assert.assertEquals(FastMath.PI, sc6.getPhi(),   1.0e-10);
    }

// org.apache.commons.math3.geometry.euclidean.threed.SphericalCoordinatesTest::testGradient
    public void testGradient() {
        for (double r = 0.2; r < 10; r += 0.5) {
            for (double theta = 0; theta < 2 * FastMath.PI; theta += 0.1) {
                for (double phi = 0.1; phi < FastMath.PI; phi += 0.1) {
                    SphericalCoordinates sc = new SphericalCoordinates(r, theta, phi);

                    DerivativeStructure svalue = valueSpherical(new DerivativeStructure(3, 1, 0, r),
                                                                new DerivativeStructure(3, 1, 1, theta),
                                                                new DerivativeStructure(3, 1, 2, phi));
                    double[] sGradient = new double[] {
                        svalue.getPartialDerivative(1, 0, 0),
                        svalue.getPartialDerivative(0, 1, 0),
                        svalue.getPartialDerivative(0, 0, 1),
                    };

                    DerivativeStructure cvalue = valueCartesian(new DerivativeStructure(3, 1, 0, sc.getCartesian().getX()),
                                                                new DerivativeStructure(3, 1, 1, sc.getCartesian().getY()),
                                                                new DerivativeStructure(3, 1, 2, sc.getCartesian().getZ()));
                    Vector3D refCGradient = new Vector3D(cvalue.getPartialDerivative(1, 0, 0),
                                                         cvalue.getPartialDerivative(0, 1, 0),
                                                         cvalue.getPartialDerivative(0, 0, 1));

                    Vector3D testCGradient = new Vector3D(sc.toCartesianGradient(sGradient));
                    
                    Assert.assertEquals(0, testCGradient.distance(refCGradient) / refCGradient.getNorm(), 5.0e-14);

                }
            }
        }
    }

// org.apache.commons.math3.geometry.euclidean.threed.SphericalCoordinatesTest::testHessian
    public void testHessian() {
        for (double r = 0.2; r < 10; r += 0.5) {
            for (double theta = 0; theta < 2 * FastMath.PI; theta += 0.2) {
                for (double phi = 0.1; phi < FastMath.PI; phi += 0.2) {
                    SphericalCoordinates sc = new SphericalCoordinates(r, theta, phi);

                    DerivativeStructure svalue = valueSpherical(new DerivativeStructure(3, 2, 0, r),
                                                                new DerivativeStructure(3, 2, 1, theta),
                                                                new DerivativeStructure(3, 2, 2, phi));
                    double[] sGradient = new double[] {
                        svalue.getPartialDerivative(1, 0, 0),
                        svalue.getPartialDerivative(0, 1, 0),
                        svalue.getPartialDerivative(0, 0, 1),
                    };
                    double[][] sHessian = new double[3][3];
                    sHessian[0][0] = svalue.getPartialDerivative(2, 0, 0); 
                    sHessian[1][0] = svalue.getPartialDerivative(1, 1, 0); 
                    sHessian[2][0] = svalue.getPartialDerivative(1, 0, 1); 
                    sHessian[0][1] = Double.NaN; 
                    sHessian[1][1] = svalue.getPartialDerivative(0, 2, 0); 
                    sHessian[2][1] = svalue.getPartialDerivative(0, 1, 1); 
                    sHessian[0][2] = Double.NaN; 
                    sHessian[1][2] = Double.NaN; 
                    sHessian[2][2] = svalue.getPartialDerivative(0, 0, 2); 

                    DerivativeStructure cvalue = valueCartesian(new DerivativeStructure(3, 2, 0, sc.getCartesian().getX()),
                                                                new DerivativeStructure(3, 2, 1, sc.getCartesian().getY()),
                                                                new DerivativeStructure(3, 2, 2, sc.getCartesian().getZ()));
                    double[][] refCHessian = new double[3][3];
                    refCHessian[0][0] = cvalue.getPartialDerivative(2, 0, 0); 
                    refCHessian[1][0] = cvalue.getPartialDerivative(1, 1, 0); 
                    refCHessian[2][0] = cvalue.getPartialDerivative(1, 0, 1); 
                    refCHessian[0][1] = refCHessian[1][0];
                    refCHessian[1][1] = cvalue.getPartialDerivative(0, 2, 0); 
                    refCHessian[2][1] = cvalue.getPartialDerivative(0, 1, 1); 
                    refCHessian[0][2] = refCHessian[2][0];
                    refCHessian[1][2] = refCHessian[2][1];
                    refCHessian[2][2] = cvalue.getPartialDerivative(0, 0, 2); 
                    double norm =  0;
                    for (int i = 0; i < 3; ++i) {
                        for (int j = 0; j < 3; ++j) {
                            norm = FastMath.max(norm, FastMath.abs(refCHessian[i][j]));
                        }
                    }

                    double[][] testCHessian = sc.toCartesianHessian(sHessian, sGradient);
                    for (int i = 0; i < 3; ++i) {
                        for (int j = 0; j < 3; ++j) {
                            Assert.assertEquals("" + FastMath.abs((refCHessian[i][j] - testCHessian[i][j]) / norm),
                                                refCHessian[i][j], testCHessian[i][j], 1.0e-14 * norm);
                        }
                    }

                }
            }
        }
    }

// org.apache.commons.math3.geometry.euclidean.threed.SphericalCoordinatesTest::testSerialization
    public void testSerialization() {
        SphericalCoordinates a = new SphericalCoordinates(3, 2, 1);
        SphericalCoordinates b = (SphericalCoordinates) TestUtils.serializeAndRecover(a);
        Assert.assertEquals(0, a.getCartesian().distance(b.getCartesian()), 1.0e-10);
        Assert.assertEquals(a.getR(),     b.getR(),     1.0e-10);
        Assert.assertEquals(a.getTheta(), b.getTheta(), 1.0e-10);
        Assert.assertEquals(a.getPhi(),   b.getPhi(),   1.0e-10);
    }

// org.apache.commons.math3.optimization.MultivariateDifferentiableMultiStartOptimizerTest::testCircleFitting
    public void testCircleFitting() {
        CircleScalar circle = new CircleScalar();
        circle.addPoint( 30.0,  68.0);
        circle.addPoint( 50.0,  -6.0);
        circle.addPoint(110.0, -20.0);
        circle.addPoint( 35.0,  15.0);
        circle.addPoint( 45.0,  97.0);
        
        
        
        MultivariateDifferentiableOptimizer underlying =
                new MultivariateDifferentiableOptimizer() {

            private final NonLinearConjugateGradientOptimizer cg =
                    new NonLinearConjugateGradientOptimizer(ConjugateGradientFormula.POLAK_RIBIERE,
                                                            new SimpleValueChecker(1.0e-10, 1.0e-10));
            public PointValuePair optimize(int maxEval,
                                           MultivariateDifferentiableFunction f,
                                           GoalType goalType,
                                           double[] startPoint) {
                return cg.optimize(maxEval, f, goalType, startPoint);
            }

            public int getMaxEvaluations() {
                return cg.getMaxEvaluations();
            }

            public int getEvaluations() {
                return cg.getEvaluations();
            }

            public ConvergenceChecker<PointValuePair> getConvergenceChecker() {
                return cg.getConvergenceChecker();
            }
        };
        JDKRandomGenerator g = new JDKRandomGenerator();
        g.setSeed(753289573253l);
        RandomVectorGenerator generator =
            new UncorrelatedRandomVectorGenerator(new double[] { 50.0, 50.0 }, new double[] { 10.0, 10.0 },
                                                  new GaussianRandomGenerator(g));
        MultivariateDifferentiableMultiStartOptimizer optimizer =
            new MultivariateDifferentiableMultiStartOptimizer(underlying, 10, generator);
        PointValuePair optimum =
            optimizer.optimize(200, circle, GoalType.MINIMIZE, new double[] { 98.680, 47.345 });
        Assert.assertEquals(200, optimizer.getMaxEvaluations());
        PointValuePair[] optima = optimizer.getOptima();
        for (PointValuePair o : optima) {
            Vector2D center = new Vector2D(o.getPointRef()[0], o.getPointRef()[1]);
            Assert.assertEquals(69.960161753, circle.getRadius(center), 1.0e-8);
            Assert.assertEquals(96.075902096, center.getX(), 1.0e-8);
            Assert.assertEquals(48.135167894, center.getY(), 1.0e-8);
        }
        Assert.assertTrue(optimizer.getEvaluations() > 70);
        Assert.assertTrue(optimizer.getEvaluations() < 90);
        Assert.assertEquals(3.1267527, optimum.getValue(), 1.0e-8);
    }

// org.apache.commons.math3.optimization.MultivariateDifferentiableVectorMultiStartOptimizerTest::testTrivial
    public void testTrivial() {
        LinearProblem problem =
            new LinearProblem(new double[][] { { 2 } }, new double[] { 3 });
        
        
        
        MultivariateDifferentiableVectorOptimizer underlyingOptimizer =
                new MultivariateDifferentiableVectorOptimizer() {
            private GaussNewtonOptimizer gn =
                    new GaussNewtonOptimizer(true,
                                             new SimpleVectorValueChecker(1.0e-6, 1.0e-6));

            public PointVectorValuePair optimize(int maxEval,
                                                 MultivariateDifferentiableVectorFunction f,
                                                 double[] target,
                                                 double[] weight,
                                                 double[] startPoint) {
                return gn.optimize(maxEval, f, target, weight, startPoint);
            }

            public int getMaxEvaluations() {
                return gn.getMaxEvaluations();
            }

            public int getEvaluations() {
                return gn.getEvaluations();
            }

            public ConvergenceChecker<PointVectorValuePair> getConvergenceChecker() {
                return gn.getConvergenceChecker();
            }
        };
        JDKRandomGenerator g = new JDKRandomGenerator();
        g.setSeed(16069223052l);
        RandomVectorGenerator generator =
            new UncorrelatedRandomVectorGenerator(1, new GaussianRandomGenerator(g));
        MultivariateDifferentiableVectorMultiStartOptimizer optimizer =
            new MultivariateDifferentiableVectorMultiStartOptimizer(underlyingOptimizer,
                                                                       10, generator);

        
        try {
            optimizer.getOptima();
            Assert.fail("an exception should have been thrown");
        } catch (MathIllegalStateException ise) {
            
        }
        PointVectorValuePair optimum =
            optimizer.optimize(100, problem, problem.target, new double[] { 1 }, new double[] { 0 });
        Assert.assertEquals(1.5, optimum.getPoint()[0], 1.0e-10);
        Assert.assertEquals(3.0, optimum.getValue()[0], 1.0e-10);
        PointVectorValuePair[] optima = optimizer.getOptima();
        Assert.assertEquals(10, optima.length);
        for (int i = 0; i < optima.length; ++i) {
            Assert.assertEquals(1.5, optima[i].getPoint()[0], 1.0e-10);
            Assert.assertEquals(3.0, optima[i].getValue()[0], 1.0e-10);
        }
        Assert.assertTrue(optimizer.getEvaluations() > 20);
        Assert.assertTrue(optimizer.getEvaluations() < 50);
        Assert.assertEquals(100, optimizer.getMaxEvaluations());
    }

// org.apache.commons.math3.optimization.MultivariateDifferentiableVectorMultiStartOptimizerTest::testNoOptimum
    public void testNoOptimum() {

        
        
        
        MultivariateDifferentiableVectorOptimizer underlyingOptimizer =
                new MultivariateDifferentiableVectorOptimizer() {
            private GaussNewtonOptimizer gn =
                    new GaussNewtonOptimizer(true,
                                             new SimpleVectorValueChecker(1.0e-6, 1.0e-6));

            public PointVectorValuePair optimize(int maxEval,
                                                 MultivariateDifferentiableVectorFunction f,
                                                 double[] target,
                                                 double[] weight,
                                                 double[] startPoint) {
                return gn.optimize(maxEval, f, target, weight, startPoint);
            }

            public int getMaxEvaluations() {
                return gn.getMaxEvaluations();
            }

            public int getEvaluations() {
                return gn.getEvaluations();
            }

            public ConvergenceChecker<PointVectorValuePair> getConvergenceChecker() {
                return gn.getConvergenceChecker();
            }
        };
        JDKRandomGenerator g = new JDKRandomGenerator();
        g.setSeed(12373523445l);
        RandomVectorGenerator generator =
            new UncorrelatedRandomVectorGenerator(1, new GaussianRandomGenerator(g));
        MultivariateDifferentiableVectorMultiStartOptimizer optimizer =
            new MultivariateDifferentiableVectorMultiStartOptimizer(underlyingOptimizer,
                                                                       10, generator);
        optimizer.optimize(100, new MultivariateDifferentiableVectorFunction() {
            public double[] value(double[] point) {
                throw new TestException();
            }
            public DerivativeStructure[] value(DerivativeStructure[] point) {
                return point;
            }
            }, new double[] { 2 }, new double[] { 1 }, new double[] { 0 });
    }

// org.apache.commons.math3.optimization.fitting.CurveFitterTest::testMath303
    public void testMath303() {

        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        CurveFitter<ParametricUnivariateFunction> fitter = new CurveFitter<ParametricUnivariateFunction>(optimizer);
        fitter.addObservedPoint(2.805d, 0.6934785852953367d);
        fitter.addObservedPoint(2.74333333333333d, 0.6306772025518496d);
        fitter.addObservedPoint(1.655d, 0.9474675497289684);
        fitter.addObservedPoint(1.725d, 0.9013594835804194d);

        ParametricUnivariateFunction sif = new SimpleInverseFunction();

        double[] initialguess1 = new double[1];
        initialguess1[0] = 1.0d;
        Assert.assertEquals(1, fitter.fit(sif, initialguess1).length);

        double[] initialguess2 = new double[2];
        initialguess2[0] = 1.0d;
        initialguess2[1] = .5d;
        Assert.assertEquals(2, fitter.fit(sif, initialguess2).length);

    }

// org.apache.commons.math3.optimization.fitting.CurveFitterTest::testMath304
    public void testMath304() {

        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        CurveFitter<ParametricUnivariateFunction> fitter = new CurveFitter<ParametricUnivariateFunction>(optimizer);
        fitter.addObservedPoint(2.805d, 0.6934785852953367d);
        fitter.addObservedPoint(2.74333333333333d, 0.6306772025518496d);
        fitter.addObservedPoint(1.655d, 0.9474675497289684);
        fitter.addObservedPoint(1.725d, 0.9013594835804194d);

        ParametricUnivariateFunction sif = new SimpleInverseFunction();

        double[] initialguess1 = new double[1];
        initialguess1[0] = 1.0d;
        Assert.assertEquals(1.6357215104109237, fitter.fit(sif, initialguess1)[0], 1.0e-14);

        double[] initialguess2 = new double[1];
        initialguess2[0] = 10.0d;
        Assert.assertEquals(1.6357215104109237, fitter.fit(sif, initialguess1)[0], 1.0e-14);

    }

// org.apache.commons.math3.optimization.fitting.CurveFitterTest::testMath372
    public void testMath372() {
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        CurveFitter<ParametricUnivariateFunction> curveFitter = new CurveFitter<ParametricUnivariateFunction>(optimizer);

        curveFitter.addObservedPoint( 15,  4443);
        curveFitter.addObservedPoint( 31,  8493);
        curveFitter.addObservedPoint( 62, 17586);
        curveFitter.addObservedPoint(125, 30582);
        curveFitter.addObservedPoint(250, 45087);
        curveFitter.addObservedPoint(500, 50683);

        ParametricUnivariateFunction f = new ParametricUnivariateFunction() {

            public double value(double x, double ... parameters) {

                double a = parameters[0];
                double b = parameters[1];
                double c = parameters[2];
                double d = parameters[3];

                return d + ((a - d) / (1 + FastMath.pow(x / c, b)));
            }

            public double[] gradient(double x, double ... parameters) {

                double a = parameters[0];
                double b = parameters[1];
                double c = parameters[2];
                double d = parameters[3];

                double[] gradients = new double[4];
                double den = 1 + FastMath.pow(x / c, b);

                
                gradients[0] = 1 / den;

                
                
                gradients[1] = -((a - d) * FastMath.pow(x / c, b) * FastMath.log(x / c)) / (den * den);

                
                gradients[2] = (b * FastMath.pow(x / c, b - 1) * (x / (c * c)) * (a - d)) / (den * den);

                
                gradients[3] = 1 - (1 / den);

                return gradients;

            }
        };

        double[] initialGuess = new double[] { 1500, 0.95, 65, 35000 };
        double[] estimatedParameters = curveFitter.fit(f, initialGuess);

        Assert.assertEquals( 2411.00, estimatedParameters[0], 500.00);
        Assert.assertEquals(    1.62, estimatedParameters[1],   0.04);
        Assert.assertEquals(  111.22, estimatedParameters[2],   0.30);
        Assert.assertEquals(55347.47, estimatedParameters[3], 300.00);
        Assert.assertTrue(optimizer.getRMS() < 600.0);

    }

// org.apache.commons.math3.optimization.fitting.GaussianFitterTest::testFit01
    public void testFit01() {
        GaussianFitter fitter = new GaussianFitter(new LevenbergMarquardtOptimizer());
        addDatasetToGaussianFitter(DATASET1, fitter);
        double[] parameters = fitter.fit();

        Assert.assertEquals(3496978.1837704973, parameters[0], 1e-4);
        Assert.assertEquals(4.054933085999146, parameters[1], 1e-4);
        Assert.assertEquals(0.015039355620304326, parameters[2], 1e-4);
    }

// org.apache.commons.math3.optimization.fitting.GaussianFitterTest::testFit02
    public void testFit02() {
        GaussianFitter fitter = new GaussianFitter(new LevenbergMarquardtOptimizer());
        fitter.fit();
    }

// org.apache.commons.math3.optimization.fitting.GaussianFitterTest::testFit03
    public void testFit03() {
        GaussianFitter fitter = new GaussianFitter(new LevenbergMarquardtOptimizer());
        addDatasetToGaussianFitter(new double[][] {
            {4.0254623,  531026.0},
            {4.02804905, 664002.0}},
            fitter);
        fitter.fit();
    }

// org.apache.commons.math3.optimization.fitting.GaussianFitterTest::testFit04
    public void testFit04() {
        GaussianFitter fitter = new GaussianFitter(new LevenbergMarquardtOptimizer());
        addDatasetToGaussianFitter(DATASET2, fitter);
        double[] parameters = fitter.fit();

        Assert.assertEquals(233003.2967252038, parameters[0], 1e-4);
        Assert.assertEquals(-10.654887521095983, parameters[1], 1e-4);
        Assert.assertEquals(4.335937353196641, parameters[2], 1e-4);
    }

// org.apache.commons.math3.optimization.fitting.GaussianFitterTest::testFit05
    public void testFit05() {
        GaussianFitter fitter = new GaussianFitter(new LevenbergMarquardtOptimizer());
        addDatasetToGaussianFitter(DATASET3, fitter);
        double[] parameters = fitter.fit();

        Assert.assertEquals(283863.81929180305, parameters[0], 1e-4);
        Assert.assertEquals(-13.29641995105174, parameters[1], 1e-4);
        Assert.assertEquals(1.7297330293549908, parameters[2], 1e-4);
    }

// org.apache.commons.math3.optimization.fitting.GaussianFitterTest::testFit06
    public void testFit06() {
        GaussianFitter fitter = new GaussianFitter(new LevenbergMarquardtOptimizer());
        addDatasetToGaussianFitter(DATASET4, fitter);
        double[] parameters = fitter.fit();

        Assert.assertEquals(285250.66754309234, parameters[0], 1e-4);
        Assert.assertEquals(-13.528375695228455, parameters[1], 1e-4);
        Assert.assertEquals(1.5204344894331614, parameters[2], 1e-4);
    }

// org.apache.commons.math3.optimization.fitting.GaussianFitterTest::testFit07
    public void testFit07() {
        GaussianFitter fitter = new GaussianFitter(new LevenbergMarquardtOptimizer());
        addDatasetToGaussianFitter(DATASET5, fitter);
        double[] parameters = fitter.fit();

        Assert.assertEquals(3514384.729342235, parameters[0], 1e-4);
        Assert.assertEquals(4.054970307455625, parameters[1], 1e-4);
        Assert.assertEquals(0.015029412832160017, parameters[2], 1e-4);
    }

// org.apache.commons.math3.optimization.fitting.GaussianFitterTest::testMath519
    public void testMath519() {
        
        

        final double[] data = { 
            1.1143831578403364E-29,
            4.95281403484594E-28,
            1.1171347211930288E-26,
            1.7044813962636277E-25,
            1.9784716574832164E-24,
            1.8630236407866774E-23,
            1.4820532905097742E-22,
            1.0241963854632831E-21,
            6.275077366673128E-21,
            3.461808994532493E-20,
            1.7407124684715706E-19,
            8.056687953553974E-19,
            3.460193945992071E-18,
            1.3883326374011525E-17,
            5.233894983671116E-17,
            1.8630791465263745E-16,
            6.288759227922111E-16,
            2.0204433920597856E-15,
            6.198768938576155E-15,
            1.821419346860626E-14,
            5.139176445538471E-14,
            1.3956427429045787E-13,
            3.655705706448139E-13,
            9.253753324779779E-13,
            2.267636001476696E-12,
            5.3880460095836855E-12,
            1.2431632654852931E-11
        };

        GaussianFitter fitter = new GaussianFitter(new LevenbergMarquardtOptimizer());
        for (int i = 0; i < data.length; i++) {
            fitter.addObservedPoint(i, data[i]);
        }
        final double[] p = fitter.fit();

        Assert.assertEquals(53.1572792, p[1], 1e-7);
        Assert.assertEquals(5.75214622, p[2], 1e-8);
    }

// org.apache.commons.math3.optimization.fitting.GaussianFitterTest::testMath798
    public void testMath798() {
        final GaussianFitter fitter = new GaussianFitter(new LevenbergMarquardtOptimizer());

        
        
        
        

        fitter.addObservedPoint(0.23, 395.0);
        
        fitter.addObservedPoint(1.14, 376.0);
        
        fitter.addObservedPoint(2.05, 163.0);
        
        fitter.addObservedPoint(2.95, 49.0);
        
        fitter.addObservedPoint(3.86, 16.0);
        
        fitter.addObservedPoint(4.77, 1.0);

        final double[] p = fitter.fit();

        
        Assert.assertEquals(420.8397296167364, p[0], 1e-12);
        Assert.assertEquals(0.603770729862231, p[1], 1e-15);
        Assert.assertEquals(1.0786447936766612, p[2], 1e-14);
    }

// org.apache.commons.math3.optimization.fitting.HarmonicFitterTest::testPreconditions1
    public void testPreconditions1() {
        HarmonicFitter fitter =
            new HarmonicFitter(new LevenbergMarquardtOptimizer());

        fitter.fit();
    }

// org.apache.commons.math3.optimization.fitting.HarmonicFitterTest::testNoError
    public void testNoError() {
        final double a = 0.2;
        final double w = 3.4;
        final double p = 4.1;
        HarmonicOscillator f = new HarmonicOscillator(a, w, p);

        HarmonicFitter fitter =
            new HarmonicFitter(new LevenbergMarquardtOptimizer());
        for (double x = 0.0; x < 1.3; x += 0.01) {
            fitter.addObservedPoint(1, x, f.value(x));
        }

        final double[] fitted = fitter.fit();
        Assert.assertEquals(a, fitted[0], 1.0e-13);
        Assert.assertEquals(w, fitted[1], 1.0e-13);
        Assert.assertEquals(p, MathUtils.normalizeAngle(fitted[2], p), 1e-13);

        HarmonicOscillator ff = new HarmonicOscillator(fitted[0], fitted[1], fitted[2]);

        for (double x = -1.0; x < 1.0; x += 0.01) {
            Assert.assertTrue(FastMath.abs(f.value(x) - ff.value(x)) < 1e-13);
        }
    }

// org.apache.commons.math3.optimization.fitting.HarmonicFitterTest::test1PercentError
    public void test1PercentError() {
        Random randomizer = new Random(64925784252l);
        final double a = 0.2;
        final double w = 3.4;
        final double p = 4.1;
        HarmonicOscillator f = new HarmonicOscillator(a, w, p);

        HarmonicFitter fitter =
            new HarmonicFitter(new LevenbergMarquardtOptimizer());
        for (double x = 0.0; x < 10.0; x += 0.1) {
            fitter.addObservedPoint(1, x,
                                    f.value(x) + 0.01 * randomizer.nextGaussian());
        }

        final double[] fitted = fitter.fit();
        Assert.assertEquals(a, fitted[0], 7.6e-4);
        Assert.assertEquals(w, fitted[1], 2.7e-3);
        Assert.assertEquals(p, MathUtils.normalizeAngle(fitted[2], p), 1.3e-2);
    }

// org.apache.commons.math3.optimization.fitting.HarmonicFitterTest::testTinyVariationsData
    public void testTinyVariationsData() {
        Random randomizer = new Random(64925784252l);

        HarmonicFitter fitter =
            new HarmonicFitter(new LevenbergMarquardtOptimizer());
        for (double x = 0.0; x < 10.0; x += 0.1) {
            fitter.addObservedPoint(1, x, 1e-7 * randomizer.nextGaussian());
        }

        fitter.fit();
        
        
    }

// org.apache.commons.math3.optimization.fitting.HarmonicFitterTest::testInitialGuess
    public void testInitialGuess() {
        Random randomizer = new Random(45314242l);
        final double a = 0.2;
        final double w = 3.4;
        final double p = 4.1;
        HarmonicOscillator f = new HarmonicOscillator(a, w, p);

        HarmonicFitter fitter =
            new HarmonicFitter(new LevenbergMarquardtOptimizer());
        for (double x = 0.0; x < 10.0; x += 0.1) {
            fitter.addObservedPoint(1, x,
                                    f.value(x) + 0.01 * randomizer.nextGaussian());
        }

        final double[] fitted = fitter.fit(new double[] { 0.15, 3.6, 4.5 });
        Assert.assertEquals(a, fitted[0], 1.2e-3);
        Assert.assertEquals(w, fitted[1], 3.3e-3);
        Assert.assertEquals(p, MathUtils.normalizeAngle(fitted[2], p), 1.7e-2);
    }

// org.apache.commons.math3.optimization.fitting.HarmonicFitterTest::testUnsorted
    public void testUnsorted() {
        Random randomizer = new Random(64925784252l);
        final double a = 0.2;
        final double w = 3.4;
        final double p = 4.1;
        HarmonicOscillator f = new HarmonicOscillator(a, w, p);

        HarmonicFitter fitter =
            new HarmonicFitter(new LevenbergMarquardtOptimizer());

        
        int size = 100;
        double[] xTab = new double[size];
        double[] yTab = new double[size];
        for (int i = 0; i < size; ++i) {
            xTab[i] = 0.1 * i;
            yTab[i] = f.value(xTab[i]) + 0.01 * randomizer.nextGaussian();
        }

        
        for (int i = 0; i < size; ++i) {
            int i1 = randomizer.nextInt(size);
            int i2 = randomizer.nextInt(size);
            double xTmp = xTab[i1];
            double yTmp = yTab[i1];
            xTab[i1] = xTab[i2];
            yTab[i1] = yTab[i2];
            xTab[i2] = xTmp;
            yTab[i2] = yTmp;
        }

        
        for (int i = 0; i < size; ++i) {
            fitter.addObservedPoint(1, xTab[i], yTab[i]);
        }

        final double[] fitted = fitter.fit();
        Assert.assertEquals(a, fitted[0], 7.6e-4);
        Assert.assertEquals(w, fitted[1], 3.5e-3);
        Assert.assertEquals(p, MathUtils.normalizeAngle(fitted[2], p), 1.5e-2);
    }

// org.apache.commons.math3.optimization.fitting.HarmonicFitterTest::testMath844
    public void testMath844() {
        final double[] y = { 0, 1, 2, 3, 2, 1,
                             0, -1, -2, -3, -2, -1,
                             0, 1, 2, 3, 2, 1,
                             0, -1, -2, -3, -2, -1,
                             0, 1, 2, 3, 2, 1, 0 };
        final int len = y.length;
        final WeightedObservedPoint[] points = new WeightedObservedPoint[len];
        for (int i = 0; i < len; i++) {
            points[i] = new WeightedObservedPoint(1, i, y[i]);
        }

        
        
        
        
        
        final HarmonicFitter.ParameterGuesser guesser
            = new HarmonicFitter.ParameterGuesser(points);
    }

// org.apache.commons.math3.optimization.fitting.PolynomialFitterTest::testFit
    public void testFit() {
        final RealDistribution rng = new UniformRealDistribution(-100, 100);
        rng.reseedRandomGenerator(64925784252L);

        final LevenbergMarquardtOptimizer optim = new LevenbergMarquardtOptimizer();
        final PolynomialFitter fitter = new PolynomialFitter(optim);
        final double[] coeff = { 12.9, -3.4, 2.1 }; 
        final PolynomialFunction f = new PolynomialFunction(coeff);

        
        for (int i = 0; i < 100; i++) {
            final double x = rng.sample();
            fitter.addObservedPoint(x, f.value(x));
        }

        
        final double[] best = fitter.fit(new double[] { -1e-20, 3e15, -5e25 });

        TestUtils.assertEquals("best != coeff", coeff, best, 1e-12);
    }

// org.apache.commons.math3.optimization.fitting.PolynomialFitterTest::testNoError
    public void testNoError() {
        Random randomizer = new Random(64925784252l);
        for (int degree = 1; degree < 10; ++degree) {
            PolynomialFunction p = buildRandomPolynomial(degree, randomizer);

            PolynomialFitter fitter = new PolynomialFitter(new LevenbergMarquardtOptimizer());
            for (int i = 0; i <= degree; ++i) {
                fitter.addObservedPoint(1.0, i, p.value(i));
            }

            final double[] init = new double[degree + 1];
            PolynomialFunction fitted = new PolynomialFunction(fitter.fit(init));

            for (double x = -1.0; x < 1.0; x += 0.01) {
                double error = FastMath.abs(p.value(x) - fitted.value(x)) /
                               (1.0 + FastMath.abs(p.value(x)));
                Assert.assertEquals(0.0, error, 1.0e-6);
            }
        }
    }

// org.apache.commons.math3.optimization.fitting.PolynomialFitterTest::testSmallError
    public void testSmallError() {
        Random randomizer = new Random(53882150042l);
        double maxError = 0;
        for (int degree = 0; degree < 10; ++degree) {
            PolynomialFunction p = buildRandomPolynomial(degree, randomizer);

            PolynomialFitter fitter = new PolynomialFitter(new LevenbergMarquardtOptimizer());
            for (double x = -1.0; x < 1.0; x += 0.01) {
                fitter.addObservedPoint(1.0, x,
                                        p.value(x) + 0.1 * randomizer.nextGaussian());
            }

            final double[] init = new double[degree + 1];
            PolynomialFunction fitted = new PolynomialFunction(fitter.fit(init));

            for (double x = -1.0; x < 1.0; x += 0.01) {
                double error = FastMath.abs(p.value(x) - fitted.value(x)) /
                              (1.0 + FastMath.abs(p.value(x)));
                maxError = FastMath.max(maxError, error);
                Assert.assertTrue(FastMath.abs(error) < 0.1);
            }
        }
        Assert.assertTrue(maxError > 0.01);
    }

// org.apache.commons.math3.optimization.fitting.PolynomialFitterTest::testMath798
    public void testMath798() {
        final double tol = 1e-14;
        final SimpleVectorValueChecker checker = new SimpleVectorValueChecker(tol, tol);
        final double[] init = new double[] { 0, 0 };
        final int maxEval = 3;

        final double[] lm = doMath798(new LevenbergMarquardtOptimizer(checker), maxEval, init);
        final double[] gn = doMath798(new GaussNewtonOptimizer(checker), maxEval, init);

        for (int i = 0; i <= 1; i++) {
            Assert.assertEquals(lm[i], gn[i], tol);
        }
    }

// org.apache.commons.math3.optimization.fitting.PolynomialFitterTest::testMath798WithToleranceTooLow
    public void testMath798WithToleranceTooLow() {
        final double tol = 1e-100;
        final SimpleVectorValueChecker checker = new SimpleVectorValueChecker(tol, tol);
        final double[] init = new double[] { 0, 0 };
        final int maxEval = 10000; 

        final double[] gn = doMath798(new GaussNewtonOptimizer(checker), maxEval, init);
    }

// org.apache.commons.math3.optimization.fitting.PolynomialFitterTest::testMath798WithToleranceTooLowButNoException
    public void testMath798WithToleranceTooLowButNoException() {
        final double tol = 1e-100;
        final double[] init = new double[] { 0, 0 };
        final int maxEval = 10000; 
        final SimpleVectorValueChecker checker = new SimpleVectorValueChecker(tol, tol, maxEval);

        final double[] lm = doMath798(new LevenbergMarquardtOptimizer(checker), maxEval, init);
        final double[] gn = doMath798(new GaussNewtonOptimizer(checker), maxEval, init);

        for (int i = 0; i <= 1; i++) {
            Assert.assertEquals(lm[i], gn[i], 1e-15);
        }
    }

// org.apache.commons.math3.optimization.fitting.PolynomialFitterTest::testRedundantSolvable
    public void testRedundantSolvable() {
        
        checkUnsolvableProblem(new LevenbergMarquardtOptimizer(), true);
    }

// org.apache.commons.math3.optimization.fitting.PolynomialFitterTest::testRedundantUnsolvable
    public void testRedundantUnsolvable() {
        
        checkUnsolvableProblem(new GaussNewtonOptimizer(true, new SimpleVectorValueChecker(1e-15, 1e-15)), false);
    }

// org.apache.commons.math3.optimization.fitting.PolynomialFitterTest::testLargeSample
    public void testLargeSample() {
        Random randomizer = new Random(0x5551480dca5b369bl);
        double maxError = 0;
        for (int degree = 0; degree < 10; ++degree) {
            PolynomialFunction p = buildRandomPolynomial(degree, randomizer);

            PolynomialFitter fitter = new PolynomialFitter(new LevenbergMarquardtOptimizer());
            for (int i = 0; i < 40000; ++i) {
                double x = -1.0 + i / 20000.0;
                fitter.addObservedPoint(1.0, x,
                                        p.value(x) + 0.1 * randomizer.nextGaussian());
            }

            final double[] init = new double[degree + 1];
            PolynomialFunction fitted = new PolynomialFunction(fitter.fit(init));

            for (double x = -1.0; x < 1.0; x += 0.01) {
                double error = FastMath.abs(p.value(x) - fitted.value(x)) /
                              (1.0 + FastMath.abs(p.value(x)));
                maxError = FastMath.max(maxError, error);
                Assert.assertTrue(FastMath.abs(error) < 0.01);
            }
        }
        Assert.assertTrue(maxError > 0.001);
    }

// org.apache.commons.math3.optimization.general.GaussNewtonOptimizerTest::testMoreEstimatedParametersSimple
    public void testMoreEstimatedParametersSimple() {
        
        super.testMoreEstimatedParametersSimple();
    }

// org.apache.commons.math3.optimization.general.GaussNewtonOptimizerTest::testMoreEstimatedParametersUnsorted
    public void testMoreEstimatedParametersUnsorted() {
        
        super.testMoreEstimatedParametersUnsorted();
    }

// org.apache.commons.math3.optimization.general.GaussNewtonOptimizerTest::testMaxEvaluations
    public void testMaxEvaluations() throws Exception {
        CircleVectorial circle = new CircleVectorial();
        circle.addPoint( 30.0,  68.0);
        circle.addPoint( 50.0,  -6.0);
        circle.addPoint(110.0, -20.0);
        circle.addPoint( 35.0,  15.0);
        circle.addPoint( 45.0,  97.0);

        GaussNewtonOptimizer optimizer
            = new GaussNewtonOptimizer(new SimpleVectorValueChecker(1.0e-30, 1.0e-30));

        optimizer.optimize(100, circle, new double[] { 0, 0, 0, 0, 0 },
                           new double[] { 1, 1, 1, 1, 1 },
                           new double[] { 98.680, 47.345 });
    }

// org.apache.commons.math3.optimization.general.GaussNewtonOptimizerTest::testCircleFittingBadInit
    public void testCircleFittingBadInit() {
        
        super.testCircleFittingBadInit();
    }

// org.apache.commons.math3.optimization.general.GaussNewtonOptimizerTest::testHahn1
    public void testHahn1()
        throws IOException {
        
        super.testHahn1();
    }

// org.apache.commons.math3.optimization.general.LevenbergMarquardtOptimizerTest::testNonInvertible
    public void testNonInvertible() {
        
        LinearProblem problem = new LinearProblem(new double[][] {
                {  1, 2, -3 },
                {  2, 1,  3 },
                { -3, 0, -9 }
        }, new double[] { 1, 1, 1 });

        AbstractLeastSquaresOptimizer optimizer = createOptimizer();
        PointVectorValuePair optimum = optimizer.optimize(100, problem, problem.target, new double[] { 1, 1, 1 }, new double[] { 0, 0, 0 });
        Assert.assertTrue(FastMath.sqrt(problem.target.length) * optimizer.getRMS() > 0.6);

        optimizer.computeCovariances(optimum.getPoint(), 1.5e-14);
    }

// org.apache.commons.math3.optimization.general.LevenbergMarquardtOptimizerTest::testControlParameters
    public void testControlParameters() {
        CircleVectorial circle = new CircleVectorial();
        circle.addPoint( 30.0,  68.0);
        circle.addPoint( 50.0,  -6.0);
        circle.addPoint(110.0, -20.0);
        circle.addPoint( 35.0,  15.0);
        circle.addPoint( 45.0,  97.0);
        checkEstimate(circle, 0.1, 10, 1.0e-14, 1.0e-16, 1.0e-10, false);
        checkEstimate(circle, 0.1, 10, 1.0e-15, 1.0e-17, 1.0e-10, true);
        checkEstimate(circle, 0.1,  5, 1.0e-15, 1.0e-16, 1.0e-10, true);
        circle.addPoint(300, -300);
        checkEstimate(circle, 0.1, 20, 1.0e-18, 1.0e-16, 1.0e-10, true);
    }

// org.apache.commons.math3.optimization.general.LevenbergMarquardtOptimizerTest::testMath199
    public void testMath199() {
        try {
            QuadraticProblem problem = new QuadraticProblem();
            problem.addPoint (0, -3.182591015485607);
            problem.addPoint (1, -2.5581184967730577);
            problem.addPoint (2, -2.1488478161387325);
            problem.addPoint (3, -1.9122489313410047);
            problem.addPoint (4, 1.7785661310051026);
            LevenbergMarquardtOptimizer optimizer
                = new LevenbergMarquardtOptimizer(100, 1e-10, 1e-10, 1e-10, 0);
            optimizer.optimize(100, problem,
                               new double[] { 0, 0, 0, 0, 0 },
                               new double[] { 0.0, 4.4e-323, 1.0, 4.4e-323, 0.0 },
                               new double[] { 0, 0, 0 });
            Assert.fail("an exception should have been thrown");
        } catch (ConvergenceException ee) {
            
        }
    }

// org.apache.commons.math3.optimization.general.LevenbergMarquardtOptimizerTest::testBevington
    public void testBevington() {
        final double[][] dataPoints = {
            
            { 15, 30, 45, 60, 75, 90, 105, 120, 135, 150,
              165, 180, 195, 210, 225, 240, 255, 270, 285, 300,
              315, 330, 345, 360, 375, 390, 405, 420, 435, 450,
              465, 480, 495, 510, 525, 540, 555, 570, 585, 600,
              615, 630, 645, 660, 675, 690, 705, 720, 735, 750,
              765, 780, 795, 810, 825, 840, 855, 870, 885, },
            
            { 775, 479, 380, 302, 185, 157, 137, 119, 110, 89,
              74, 61, 66, 68, 48, 54, 51, 46, 55, 29,
              28, 37, 49, 26, 35, 29, 31, 24, 25, 35,
              24, 30, 26, 28, 21, 18, 20, 27, 17, 17,
              14, 17, 24, 11, 22, 17, 12, 10, 13, 16,
              9, 9, 14, 21, 17, 13, 12, 18, 10, },
        };

        final BevingtonProblem problem = new BevingtonProblem();

        final int len = dataPoints[0].length;
        final double[] weights = new double[len];
        for (int i = 0; i < len; i++) {
            problem.addPoint(dataPoints[0][i],
                             dataPoints[1][i]);

            weights[i] = 1 / dataPoints[1][i];
        }

        final LevenbergMarquardtOptimizer optimizer
            = new LevenbergMarquardtOptimizer();

        final PointVectorValuePair optimum
            = optimizer.optimize(100, problem, dataPoints[1], weights,
                               new double[] { 10, 900, 80, 27, 225 });

        final double[] solution = optimum.getPoint();
        final double[] expectedSolution = { 10.4, 958.3, 131.4, 33.9, 205.0 };

        final double[][] covarMatrix = optimizer.computeCovariances(solution, 1e-14);
        final double[][] expectedCovarMatrix = {
            { 3.38, -3.69, 27.98, -2.34, -49.24 },
            { -3.69, 2492.26, 81.89, -69.21, -8.9 },
            { 27.98, 81.89, 468.99, -44.22, -615.44 },
            { -2.34, -69.21, -44.22, 6.39, 53.80 },
            { -49.24, -8.9, -615.44, 53.8, 929.45 }
        };

        final int numParams = expectedSolution.length;

        
        for (int i = 0; i < numParams; i++) {
            final double error = FastMath.sqrt(expectedCovarMatrix[i][i]);
            Assert.assertEquals("Parameter " + i, expectedSolution[i], solution[i], error);
        }

        
        
        for (int i = 0; i < numParams; i++) {
            for (int j = 0; j < numParams; j++) {
                Assert.assertEquals("Covariance matrix [" + i + "][" + j + "]",
                                    expectedCovarMatrix[i][j],
                                    covarMatrix[i][j],
                                    FastMath.abs(0.1 * expectedCovarMatrix[i][j]));
            }
        }
    }

// org.apache.commons.math3.optimization.general.LevenbergMarquardtOptimizerTest::testCircleFitting2
    public void testCircleFitting2() {
        final double xCenter = 123.456;
        final double yCenter = 654.321;
        final double xSigma = 10;
        final double ySigma = 15;
        final double radius = 111.111;
        
        final long seed = 59421061L;
        final RandomCirclePointGenerator factory
            = new RandomCirclePointGenerator(xCenter, yCenter, radius,
                                             xSigma, ySigma,
                                             seed);
        final CircleProblem circle = new CircleProblem(xSigma, ySigma);

        final int numPoints = 10;
        for (Vector2D p : factory.generate(numPoints)) {
            circle.addPoint(p);
            
        }

        
        final double[] init = { 90, 659, 115 };

        final LevenbergMarquardtOptimizer optimizer
            = new LevenbergMarquardtOptimizer();
        final PointVectorValuePair optimum = optimizer.optimize(100, circle,
                                                                circle.target(), circle.weight(),
                                                                init);

        final double[] paramFound = optimum.getPoint();

        
        final double[][] covMatrix = optimizer.computeCovariances(paramFound, 1e-14);
        final double[] asymptoticStandardErrorFound = optimizer.guessParametersErrors();
        final double[] sigmaFound = new double[covMatrix.length];
        for (int i = 0; i < covMatrix.length; i++) {
            sigmaFound[i] = FastMath.sqrt(covMatrix[i][i]);

        }

        

        
        Assert.assertEquals(xCenter, paramFound[0], asymptoticStandardErrorFound[0]);
        Assert.assertEquals(yCenter, paramFound[1], asymptoticStandardErrorFound[1]);
        Assert.assertEquals(radius, paramFound[2], asymptoticStandardErrorFound[2]);
    }

// org.apache.commons.math3.optimization.general.MinpackTest::testMinpackLinearFullRank
  public void testMinpackLinearFullRank() {
    minpackTest(new LinearFullRankFunction(10, 5, 1.0,
                                           5.0, 2.23606797749979), false);
    minpackTest(new LinearFullRankFunction(50, 5, 1.0,
                                           8.06225774829855, 6.70820393249937), false);
  }

// org.apache.commons.math3.optimization.general.MinpackTest::testMinpackLinearRank1
  public void testMinpackLinearRank1() {
    minpackTest(new LinearRank1Function(10, 5, 1.0,
                                        291.521868819476, 1.4638501094228), false);
    minpackTest(new LinearRank1Function(50, 5, 1.0,
                                        3101.60039334535, 3.48263016573496), false);
  }

// org.apache.commons.math3.optimization.general.MinpackTest::testMinpackLinearRank1ZeroColsAndRows
  public void testMinpackLinearRank1ZeroColsAndRows() {
    minpackTest(new LinearRank1ZeroColsAndRowsFunction(10, 5, 1.0), false);
    minpackTest(new LinearRank1ZeroColsAndRowsFunction(50, 5, 1.0), false);
  }

// org.apache.commons.math3.optimization.general.MinpackTest::testMinpackRosenbrok
  public void testMinpackRosenbrok() {
    minpackTest(new RosenbrockFunction(new double[] { -1.2, 1.0 },
                                       FastMath.sqrt(24.2)), false);
    minpackTest(new RosenbrockFunction(new double[] { -12.0, 10.0 },
                                       FastMath.sqrt(1795769.0)), false);
    minpackTest(new RosenbrockFunction(new double[] { -120.0, 100.0 },
                                       11.0 * FastMath.sqrt(169000121.0)), false);
  }

// org.apache.commons.math3.optimization.general.MinpackTest::testMinpackHelicalValley
  public void testMinpackHelicalValley() {
    minpackTest(new HelicalValleyFunction(new double[] { -1.0, 0.0, 0.0 },
                                          50.0), false);
    minpackTest(new HelicalValleyFunction(new double[] { -10.0, 0.0, 0.0 },
                                          102.95630140987), false);
    minpackTest(new HelicalValleyFunction(new double[] { -100.0, 0.0, 0.0},
                                          991.261822123701), false);
  }

// org.apache.commons.math3.optimization.general.MinpackTest::testMinpackPowellSingular
  public void testMinpackPowellSingular() {
    minpackTest(new PowellSingularFunction(new double[] { 3.0, -1.0, 0.0, 1.0 },
                                           14.6628782986152), false);
    minpackTest(new PowellSingularFunction(new double[] { 30.0, -10.0, 0.0, 10.0 },
                                           1270.9838708654), false);
    minpackTest(new PowellSingularFunction(new double[] { 300.0, -100.0, 0.0, 100.0 },
                                           126887.903284750), false);
  }

// org.apache.commons.math3.optimization.general.MinpackTest::testMinpackFreudensteinRoth
  public void testMinpackFreudensteinRoth() {
    minpackTest(new FreudensteinRothFunction(new double[] { 0.5, -2.0 },
                                             20.0124960961895, 6.99887517584575,
                                             new double[] {
                                               11.4124844654993,
                                               -0.896827913731509
                                             }), false);
    minpackTest(new FreudensteinRothFunction(new double[] { 5.0, -20.0 },
                                             12432.833948863, 6.9988751744895,
                                             new double[] {
                                                11.41300466147456,
                                                -0.896796038685959
                                             }), false);
    minpackTest(new FreudensteinRothFunction(new double[] { 50.0, -200.0 },
                                             11426454.595762, 6.99887517242903,
                                             new double[] {
                                                 11.412781785788564,
                                                 -0.8968051074920405
                                             }), false);
  }

// org.apache.commons.math3.optimization.general.MinpackTest::testMinpackBard
  public void testMinpackBard() {
    minpackTest(new BardFunction(1.0, 6.45613629515967, 0.0906359603390466,
                                 new double[] {
                                   0.0824105765758334,
                                   1.1330366534715,
                                   2.34369463894115
                                 }), false);
    minpackTest(new BardFunction(10.0, 36.1418531596785, 4.17476870138539,
                                 new double[] {
                                   0.840666673818329,
                                   -158848033.259565,
                                   -164378671.653535
                                 }), false);
    minpackTest(new BardFunction(100.0, 384.114678637399, 4.17476870135969,
                                 new double[] {
                                   0.840666673867645,
                                   -158946167.205518,
                                   -164464906.857771
                                 }), false);
  }

// org.apache.commons.math3.optimization.general.MinpackTest::testMinpackKowalikOsborne
  public void testMinpackKowalikOsborne() {
    minpackTest(new KowalikOsborneFunction(new double[] { 0.25, 0.39, 0.415, 0.39 },
                                           0.0728915102882945,
                                           0.017535837721129,
                                           new double[] {
                                             0.192807810476249,
                                             0.191262653354071,
                                             0.123052801046931,
                                             0.136053221150517
                                           }), false);
    minpackTest(new KowalikOsborneFunction(new double[] { 2.5, 3.9, 4.15, 3.9 },
                                           2.97937007555202,
                                           0.032052192917937,
                                           new double[] {
                                             728675.473768287,
                                             -14.0758803129393,
                                             -32977797.7841797,
                                             -20571594.1977912
                                           }), false);
    minpackTest(new KowalikOsborneFunction(new double[] { 25.0, 39.0, 41.5, 39.0 },
                                           29.9590617016037,
                                           0.0175364017658228,
                                           new double[] {
                                             0.192948328597594,
                                             0.188053165007911,
                                             0.122430604321144,
                                             0.134575665392506
                                           }), false);
  }

// org.apache.commons.math3.optimization.general.MinpackTest::testMinpackMeyer
  public void testMinpackMeyer() {
    minpackTest(new MeyerFunction(new double[] { 0.02, 4000.0, 250.0 },
                                  41153.4665543031, 9.37794514651874,
                                  new double[] {
                                    0.00560963647102661,
                                    6181.34634628659,
                                    345.223634624144
                                  }), false);
    minpackTest(new MeyerFunction(new double[] { 0.2, 40000.0, 2500.0 },
                                  4168216.89130846, 792.917871779501,
                                  new double[] {
                                    1.42367074157994e-11,
                                    33695.7133432541,
                                    901.268527953801
                                  }), true);
  }

// org.apache.commons.math3.optimization.general.MinpackTest::testMinpackWatson
  public void testMinpackWatson() {

    minpackTest(new WatsonFunction(6, 0.0,
                                   5.47722557505166, 0.0478295939097601,
                                   new double[] {
                                     -0.0157249615083782, 1.01243488232965,
                                     -0.232991722387673,  1.26043101102818,
                                     -1.51373031394421,   0.99299727291842
                                   }), false);
    minpackTest(new WatsonFunction(6, 10.0,
                                   6433.12578950026, 0.0478295939096951,
                                   new double[] {
                                     -0.0157251901386677, 1.01243485860105,
                                     -0.232991545843829,  1.26042932089163,
                                     -1.51372776706575,   0.99299573426328
                                   }), false);
    minpackTest(new WatsonFunction(6, 100.0,
                                   674256.040605213, 0.047829593911544,
                                   new double[] {
                                    -0.0157247019712586, 1.01243490925658,
                                    -0.232991922761641,  1.26043292929555,
                                    -1.51373320452707,   0.99299901922322
                                   }), false);

    minpackTest(new WatsonFunction(9, 0.0,
                                   5.47722557505166, 0.00118311459212420,
                                   new double[] {
                                    -0.153070644166722e-4, 0.999789703934597,
                                     0.0147639634910978,   0.146342330145992,
                                     1.00082109454817,    -2.61773112070507,
                                     4.10440313943354,    -3.14361226236241,
                                     1.05262640378759
                                   }), false);
    minpackTest(new WatsonFunction(9, 10.0,
                                   12088.127069307, 0.00118311459212513,
                                   new double[] {
                                   -0.153071334849279e-4, 0.999789703941234,
                                    0.0147639629786217,   0.146342334818836,
                                    1.00082107321386,    -2.61773107084722,
                                    4.10440307655564,    -3.14361222178686,
                                    1.05262639322589
                                   }), false);
    minpackTest(new WatsonFunction(9, 100.0,
                                   1269109.29043834, 0.00118311459212384,
                                   new double[] {
                                    -0.153069523352176e-4, 0.999789703958371,
                                     0.0147639625185392,   0.146342341096326,
                                     1.00082104729164,    -2.61773101573645,
                                     4.10440301427286,    -3.14361218602503,
                                     1.05262638516774
                                   }), false);

    minpackTest(new WatsonFunction(12, 0.0,
                                   5.47722557505166, 0.217310402535861e-4,
                                   new double[] {
                                    -0.660266001396382e-8, 1.00000164411833,
                                    -0.000563932146980154, 0.347820540050756,
                                    -0.156731500244233,    1.05281515825593,
                                    -3.24727109519451,     7.2884347837505,
                                   -10.271848098614,       9.07411353715783,
                                    -4.54137541918194,     1.01201187975044
                                   }), false);
    minpackTest(new WatsonFunction(12, 10.0,
                                   19220.7589790951, 0.217310402518509e-4,
                                   new double[] {
                                    -0.663710223017410e-8, 1.00000164411787,
                                    -0.000563932208347327, 0.347820540486998,
                                    -0.156731503955652,    1.05281517654573,
                                    -3.2472711515214,      7.28843489430665,
                                   -10.2718482369638,      9.07411364383733,
                                    -4.54137546533666,     1.01201188830857
                                   }), false);
    minpackTest(new WatsonFunction(12, 100.0,
                                   2018918.04462367, 0.217310402539845e-4,
                                   new double[] {
                                    -0.663806046485249e-8, 1.00000164411786,
                                    -0.000563932210324959, 0.347820540503588,
                                    -0.156731504091375,    1.05281517718031,
                                    -3.24727115337025,     7.28843489775302,
                                   -10.2718482410813,      9.07411364688464,
                                    -4.54137546660822,     1.0120118885369
                                   }), false);

  }

// org.apache.commons.math3.optimization.general.MinpackTest::testMinpackBox3Dimensional
  public void testMinpackBox3Dimensional() {
    minpackTest(new Box3DimensionalFunction(10, new double[] { 0.0, 10.0, 20.0 },
                                            32.1115837449572), false);
  }

// org.apache.commons.math3.optimization.general.MinpackTest::testMinpackJennrichSampson
  public void testMinpackJennrichSampson() {
    minpackTest(new JennrichSampsonFunction(10, new double[] { 0.3, 0.4 },
                                            64.5856498144943, 11.1517793413499,
                                            new double[] {
 
                                               0.2578199266368004, 0.25782997676455244
                                            }), false);
  }

// org.apache.commons.math3.optimization.general.MinpackTest::testMinpackBrownDennis
  public void testMinpackBrownDennis() {
    minpackTest(new BrownDennisFunction(20,
                                        new double[] { 25.0, 5.0, -5.0, -1.0 },
                                        2815.43839161816, 292.954288244866,
                                        new double[] {
                                         -11.59125141003, 13.2024883984741,
                                         -0.403574643314272, 0.236736269844604
                                        }), false);
    minpackTest(new BrownDennisFunction(20,
                                        new double[] { 250.0, 50.0, -50.0, -10.0 },
                                        555073.354173069, 292.954270581415,
                                        new double[] {
                                         -11.5959274272203, 13.2041866926242,
                                         -0.403417362841545, 0.236771143410386
                                       }), false);
    minpackTest(new BrownDennisFunction(20,
                                        new double[] { 2500.0, 500.0, -500.0, -100.0 },
                                        61211252.2338581, 292.954306151134,
                                        new double[] {
                                         -11.5902596937374, 13.2020628854665,
                                         -0.403688070279258, 0.236665033746463
                                        }), false);
  }

// org.apache.commons.math3.optimization.general.MinpackTest::testMinpackChebyquad
  public void testMinpackChebyquad() {
    minpackTest(new ChebyquadFunction(1, 8, 1.0,
                                      1.88623796907732, 1.88623796907732,
                                      new double[] { 0.5 }), false);
    minpackTest(new ChebyquadFunction(1, 8, 10.0,
                                      5383344372.34005, 1.88424820499951,
                                      new double[] { 0.9817314924684 }), false);
    minpackTest(new ChebyquadFunction(1, 8, 100.0,
                                      0.118088726698392e19, 1.88424820499347,
                                      new double[] { 0.9817314852934 }), false);
    minpackTest(new ChebyquadFunction(8, 8, 1.0,
                                      0.196513862833975, 0.0593032355046727,
                                      new double[] {
                                        0.0431536648587336, 0.193091637843267,
                                        0.266328593812698,  0.499999334628884,
                                        0.500000665371116,  0.733671406187302,
                                        0.806908362156733,  0.956846335141266
                                      }), false);
    minpackTest(new ChebyquadFunction(9, 9, 1.0,
                                      0.16994993465202, 0.0,
                                      new double[] {
                                        0.0442053461357828, 0.199490672309881,
                                        0.23561910847106,   0.416046907892598,
                                        0.5,                0.583953092107402,
                                        0.764380891528940,  0.800509327690119,
                                        0.955794653864217
                                      }), false);
    minpackTest(new ChebyquadFunction(10, 10, 1.0,
                                      0.183747831178711, 0.0806471004038253,
                                      new double[] {
                                        0.0596202671753563, 0.166708783805937,
                                        0.239171018813509,  0.398885290346268,
                                        0.398883667870681,  0.601116332129320,
                                        0.60111470965373,   0.760828981186491,
                                        0.833291216194063,  0.940379732824644
                                      }), false);
  }

// org.apache.commons.math3.optimization.general.MinpackTest::testMinpackBrownAlmostLinear
  public void testMinpackBrownAlmostLinear() {
    minpackTest(new BrownAlmostLinearFunction(10, 0.5,
                                              16.5302162063499, 0.0,
                                              new double[] {
                                                0.979430303349862, 0.979430303349862,
                                                0.979430303349862, 0.979430303349862,
                                                0.979430303349862, 0.979430303349862,
                                                0.979430303349862, 0.979430303349862,
                                                0.979430303349862, 1.20569696650138
                                              }), false);
    minpackTest(new BrownAlmostLinearFunction(10, 5.0,
                                              9765624.00089211, 0.0,
                                              new double[] {
                                               0.979430303349865, 0.979430303349865,
                                               0.979430303349865, 0.979430303349865,
                                               0.979430303349865, 0.979430303349865,
                                               0.979430303349865, 0.979430303349865,
                                               0.979430303349865, 1.20569696650135
                                              }), false);
    minpackTest(new BrownAlmostLinearFunction(10, 50.0,
                                              0.9765625e17, 0.0,
                                              new double[] {
                                                1.0, 1.0, 1.0, 1.0, 1.0,
                                                1.0, 1.0, 1.0, 1.0, 1.0
                                              }), false);
    minpackTest(new BrownAlmostLinearFunction(30, 0.5,
                                              83.476044467848, 0.0,
                                              new double[] {
                                                0.997754216442807, 0.997754216442807,
                                                0.997754216442807, 0.997754216442807,
                                                0.997754216442807, 0.997754216442807,
                                                0.997754216442807, 0.997754216442807,
                                                0.997754216442807, 0.997754216442807,
                                                0.997754216442807, 0.997754216442807,
                                                0.997754216442807, 0.997754216442807,
                                                0.997754216442807, 0.997754216442807,
                                                0.997754216442807, 0.997754216442807,
                                                0.997754216442807, 0.997754216442807,
                                                0.997754216442807, 0.997754216442807,
                                                0.997754216442807, 0.997754216442807,
                                                0.997754216442807, 0.997754216442807,
                                                0.997754216442807, 0.997754216442807,
                                                0.997754216442807, 1.06737350671578
                                              }), false);
    minpackTest(new BrownAlmostLinearFunction(40, 0.5,
                                              128.026364472323, 0.0,
                                              new double[] {
                                                1.00000000000002, 1.00000000000002,
                                                1.00000000000002, 1.00000000000002,
                                                1.00000000000002, 1.00000000000002,
                                                1.00000000000002, 1.00000000000002,
                                                1.00000000000002, 1.00000000000002,
                                                1.00000000000002, 1.00000000000002,
                                                1.00000000000002, 1.00000000000002,
                                                1.00000000000002, 1.00000000000002,
                                                1.00000000000002, 1.00000000000002,
                                                1.00000000000002, 1.00000000000002,
                                                1.00000000000002, 1.00000000000002,
                                                1.00000000000002, 1.00000000000002,
                                                1.00000000000002, 1.00000000000002,
                                                1.00000000000002, 1.00000000000002,
                                                1.00000000000002, 1.00000000000002,
                                                1.00000000000002, 1.00000000000002,
                                                1.00000000000002, 1.00000000000002,
                                                0.999999999999121
                                              }), false);
    }

// org.apache.commons.math3.optimization.general.MinpackTest::testMinpackOsborne1
  public void testMinpackOsborne1() {
      minpackTest(new Osborne1Function(new double[] { 0.5, 1.5, -1.0, 0.01, 0.02, },
                                       0.937564021037838, 0.00739249260904843,
                                       new double[] {
                                         0.375410049244025, 1.93584654543108,
                                        -1.46468676748716, 0.0128675339110439,
                                         0.0221227011813076
                                       }), false);
    }

// org.apache.commons.math3.optimization.general.MinpackTest::testMinpackOsborne2
  public void testMinpackOsborne2() {

    minpackTest(new Osborne2Function(new double[] {
                                       1.3, 0.65, 0.65, 0.7, 0.6,
                                       3.0, 5.0, 7.0, 2.0, 4.5, 5.5
                                     },
                                     1.44686540984712, 0.20034404483314,
                                     new double[] {
                                       1.30997663810096,  0.43155248076,
                                       0.633661261602859, 0.599428560991695,
                                       0.754179768272449, 0.904300082378518,
                                       1.36579949521007, 4.82373199748107,
                                       2.39868475104871, 4.56887554791452,
                                       5.67534206273052
                                     }), false);
  }

// org.apache.commons.math3.optimization.general.NonLinearConjugateGradientOptimizerTest::testTrivial
    public void testTrivial() {
        LinearProblem problem =
            new LinearProblem(new double[][] { { 2 } }, new double[] { 3 });
        NonLinearConjugateGradientOptimizer optimizer =
            new NonLinearConjugateGradientOptimizer(ConjugateGradientFormula.POLAK_RIBIERE,
                                                    new SimpleValueChecker(1e-6, 1e-6));
        PointValuePair optimum =
            optimizer.optimize(100, problem, GoalType.MINIMIZE, new double[] { 0 });
        Assert.assertEquals(1.5, optimum.getPoint()[0], 1.0e-10);
        Assert.assertEquals(0.0, optimum.getValue(), 1.0e-10);
    }

// org.apache.commons.math3.optimization.general.NonLinearConjugateGradientOptimizerTest::testColumnsPermutation
    public void testColumnsPermutation() {
        LinearProblem problem =
            new LinearProblem(new double[][] { { 1.0, -1.0 }, { 0.0, 2.0 }, { 1.0, -2.0 } },
                              new double[] { 4.0, 6.0, 1.0 });

        NonLinearConjugateGradientOptimizer optimizer =
            new NonLinearConjugateGradientOptimizer(ConjugateGradientFormula.POLAK_RIBIERE,
                                                    new SimpleValueChecker(1e-6, 1e-6));
        PointValuePair optimum =
            optimizer.optimize(100, problem, GoalType.MINIMIZE, new double[] { 0, 0 });
        Assert.assertEquals(7.0, optimum.getPoint()[0], 1.0e-10);
        Assert.assertEquals(3.0, optimum.getPoint()[1], 1.0e-10);
        Assert.assertEquals(0.0, optimum.getValue(), 1.0e-10);

    }

// org.apache.commons.math3.optimization.general.NonLinearConjugateGradientOptimizerTest::testNoDependency
    public void testNoDependency() {
        LinearProblem problem = new LinearProblem(new double[][] {
                { 2, 0, 0, 0, 0, 0 },
                { 0, 2, 0, 0, 0, 0 },
                { 0, 0, 2, 0, 0, 0 },
                { 0, 0, 0, 2, 0, 0 },
                { 0, 0, 0, 0, 2, 0 },
                { 0, 0, 0, 0, 0, 2 }
        }, new double[] { 0.0, 1.1, 2.2, 3.3, 4.4, 5.5 });
        NonLinearConjugateGradientOptimizer optimizer =
            new NonLinearConjugateGradientOptimizer(ConjugateGradientFormula.POLAK_RIBIERE,
                                                    new SimpleValueChecker(1e-6, 1e-6));
        PointValuePair optimum =
            optimizer.optimize(100, problem, GoalType.MINIMIZE, new double[] { 0, 0, 0, 0, 0, 0 });
        for (int i = 0; i < problem.target.length; ++i) {
            Assert.assertEquals(0.55 * i, optimum.getPoint()[i], 1.0e-10);
        }
    }

// org.apache.commons.math3.optimization.general.NonLinearConjugateGradientOptimizerTest::testOneSet
    public void testOneSet() {
        LinearProblem problem = new LinearProblem(new double[][] {
                {  1,  0, 0 },
                { -1,  1, 0 },
                {  0, -1, 1 }
        }, new double[] { 1, 1, 1});
        NonLinearConjugateGradientOptimizer optimizer =
            new NonLinearConjugateGradientOptimizer(ConjugateGradientFormula.POLAK_RIBIERE,
                                                    new SimpleValueChecker(1e-6, 1e-6));
        PointValuePair optimum =
            optimizer.optimize(100, problem, GoalType.MINIMIZE, new double[] { 0, 0, 0 });
        Assert.assertEquals(1.0, optimum.getPoint()[0], 1.0e-10);
        Assert.assertEquals(2.0, optimum.getPoint()[1], 1.0e-10);
        Assert.assertEquals(3.0, optimum.getPoint()[2], 1.0e-10);

    }

// org.apache.commons.math3.optimization.general.NonLinearConjugateGradientOptimizerTest::testTwoSets
    public void testTwoSets() {
        final double epsilon = 1.0e-7;
        LinearProblem problem = new LinearProblem(new double[][] {
                {  2,  1,   0,  4,       0, 0 },
                { -4, -2,   3, -7,       0, 0 },
                {  4,  1,  -2,  8,       0, 0 },
                {  0, -3, -12, -1,       0, 0 },
                {  0,  0,   0,  0, epsilon, 1 },
                {  0,  0,   0,  0,       1, 1 }
        }, new double[] { 2, -9, 2, 2, 1 + epsilon * epsilon, 2});

        final Preconditioner preconditioner
            = new Preconditioner() {
                    public double[] precondition(double[] point, double[] r) {
                        double[] d = r.clone();
                        d[0] /=  72.0;
                        d[1] /=  30.0;
                        d[2] /= 314.0;
                        d[3] /= 260.0;
                        d[4] /= 2 * (1 + epsilon * epsilon);
                        d[5] /= 4.0;
                        return d;
                    }
                };

        NonLinearConjugateGradientOptimizer optimizer =
            new NonLinearConjugateGradientOptimizer(ConjugateGradientFormula.POLAK_RIBIERE,
                                                    new SimpleValueChecker(1e-13, 1e-13),
                                                    new BrentSolver(),
                                                    preconditioner);
                                                    
        PointValuePair optimum =
            optimizer.optimize(100, problem, GoalType.MINIMIZE, new double[] { 0, 0, 0, 0, 0, 0 });
        Assert.assertEquals( 3.0, optimum.getPoint()[0], 1.0e-10);
        Assert.assertEquals( 4.0, optimum.getPoint()[1], 1.0e-10);
        Assert.assertEquals(-1.0, optimum.getPoint()[2], 1.0e-10);
        Assert.assertEquals(-2.0, optimum.getPoint()[3], 1.0e-10);
        Assert.assertEquals( 1.0 + epsilon, optimum.getPoint()[4], 1.0e-10);
        Assert.assertEquals( 1.0 - epsilon, optimum.getPoint()[5], 1.0e-10);

    }

// org.apache.commons.math3.optimization.general.NonLinearConjugateGradientOptimizerTest::testNonInversible
    public void testNonInversible() {
        LinearProblem problem = new LinearProblem(new double[][] {
                {  1, 2, -3 },
                {  2, 1,  3 },
                { -3, 0, -9 }
        }, new double[] { 1, 1, 1 });
        NonLinearConjugateGradientOptimizer optimizer =
            new NonLinearConjugateGradientOptimizer(ConjugateGradientFormula.POLAK_RIBIERE,
                                                    new SimpleValueChecker(1e-6, 1e-6));
        PointValuePair optimum =
                optimizer.optimize(100, problem, GoalType.MINIMIZE, new double[] { 0, 0, 0 });
        Assert.assertTrue(optimum.getValue() > 0.5);
    }

// org.apache.commons.math3.optimization.general.NonLinearConjugateGradientOptimizerTest::testIllConditioned
    public void testIllConditioned() {
        LinearProblem problem1 = new LinearProblem(new double[][] {
                { 10.0, 7.0,  8.0,  7.0 },
                {  7.0, 5.0,  6.0,  5.0 },
                {  8.0, 6.0, 10.0,  9.0 },
                {  7.0, 5.0,  9.0, 10.0 }
        }, new double[] { 32, 23, 33, 31 });
        NonLinearConjugateGradientOptimizer optimizer =
            new NonLinearConjugateGradientOptimizer(ConjugateGradientFormula.POLAK_RIBIERE,
                                                    new SimpleValueChecker(1e-13, 1e-13),
                                                    new BrentSolver(1e-15, 1e-15));
        PointValuePair optimum1 =
            optimizer.optimize(200, problem1, GoalType.MINIMIZE, new double[] { 0, 1, 2, 3 });
        Assert.assertEquals(1.0, optimum1.getPoint()[0], 1.0e-4);
        Assert.assertEquals(1.0, optimum1.getPoint()[1], 1.0e-4);
        Assert.assertEquals(1.0, optimum1.getPoint()[2], 1.0e-4);
        Assert.assertEquals(1.0, optimum1.getPoint()[3], 1.0e-4);

        LinearProblem problem2 = new LinearProblem(new double[][] {
                { 10.00, 7.00, 8.10, 7.20 },
                {  7.08, 5.04, 6.00, 5.00 },
                {  8.00, 5.98, 9.89, 9.00 },
                {  6.99, 4.99, 9.00, 9.98 }
        }, new double[] { 32, 23, 33, 31 });
        PointValuePair optimum2 =
            optimizer.optimize(200, problem2, GoalType.MINIMIZE, new double[] { 0, 1, 2, 3 });
        Assert.assertEquals(-81.0, optimum2.getPoint()[0], 1.0e-1);
        Assert.assertEquals(137.0, optimum2.getPoint()[1], 1.0e-1);
        Assert.assertEquals(-34.0, optimum2.getPoint()[2], 1.0e-1);
        Assert.assertEquals( 22.0, optimum2.getPoint()[3], 1.0e-1);

    }

// org.apache.commons.math3.optimization.general.NonLinearConjugateGradientOptimizerTest::testMoreEstimatedParametersSimple
    public void testMoreEstimatedParametersSimple() {
        LinearProblem problem = new LinearProblem(new double[][] {
                { 3.0, 2.0,  0.0, 0.0 },
                { 0.0, 1.0, -1.0, 1.0 },
                { 2.0, 0.0,  1.0, 0.0 }
        }, new double[] { 7.0, 3.0, 5.0 });

        NonLinearConjugateGradientOptimizer optimizer =
            new NonLinearConjugateGradientOptimizer(ConjugateGradientFormula.POLAK_RIBIERE,
                                                    new SimpleValueChecker(1e-6, 1e-6));
        PointValuePair optimum =
            optimizer.optimize(100, problem, GoalType.MINIMIZE, new double[] { 7, 6, 5, 4 });
        Assert.assertEquals(0, optimum.getValue(), 1.0e-10);

    }

// org.apache.commons.math3.optimization.general.NonLinearConjugateGradientOptimizerTest::testMoreEstimatedParametersUnsorted
    public void testMoreEstimatedParametersUnsorted() {
        LinearProblem problem = new LinearProblem(new double[][] {
                 { 1.0, 1.0,  0.0,  0.0, 0.0,  0.0 },
                 { 0.0, 0.0,  1.0,  1.0, 1.0,  0.0 },
                 { 0.0, 0.0,  0.0,  0.0, 1.0, -1.0 },
                 { 0.0, 0.0, -1.0,  1.0, 0.0,  1.0 },
                 { 0.0, 0.0,  0.0, -1.0, 1.0,  0.0 }
        }, new double[] { 3.0, 12.0, -1.0, 7.0, 1.0 });
        NonLinearConjugateGradientOptimizer optimizer =
            new NonLinearConjugateGradientOptimizer(ConjugateGradientFormula.POLAK_RIBIERE,
                                                    new SimpleValueChecker(1e-6, 1e-6));
        PointValuePair optimum =
            optimizer.optimize(100, problem, GoalType.MINIMIZE, new double[] { 2, 2, 2, 2, 2, 2 });
        Assert.assertEquals(0, optimum.getValue(), 1.0e-10);
    }

// org.apache.commons.math3.optimization.general.NonLinearConjugateGradientOptimizerTest::testRedundantEquations
    public void testRedundantEquations() {
        LinearProblem problem = new LinearProblem(new double[][] {
                { 1.0,  1.0 },
                { 1.0, -1.0 },
                { 1.0,  3.0 }
        }, new double[] { 3.0, 1.0, 5.0 });

        NonLinearConjugateGradientOptimizer optimizer =
            new NonLinearConjugateGradientOptimizer(ConjugateGradientFormula.POLAK_RIBIERE,
                                                    new SimpleValueChecker(1e-6, 1e-6));
        PointValuePair optimum =
            optimizer.optimize(100, problem, GoalType.MINIMIZE, new double[] { 1, 1 });
        Assert.assertEquals(2.0, optimum.getPoint()[0], 1.0e-8);
        Assert.assertEquals(1.0, optimum.getPoint()[1], 1.0e-8);

    }

// org.apache.commons.math3.optimization.general.NonLinearConjugateGradientOptimizerTest::testInconsistentEquations
    public void testInconsistentEquations() {
        LinearProblem problem = new LinearProblem(new double[][] {
                { 1.0,  1.0 },
                { 1.0, -1.0 },
                { 1.0,  3.0 }
        }, new double[] { 3.0, 1.0, 4.0 });

        NonLinearConjugateGradientOptimizer optimizer =
            new NonLinearConjugateGradientOptimizer(ConjugateGradientFormula.POLAK_RIBIERE,
                                                    new SimpleValueChecker(1e-6, 1e-6));
        PointValuePair optimum =
            optimizer.optimize(100, problem, GoalType.MINIMIZE, new double[] { 1, 1 });
        Assert.assertTrue(optimum.getValue() > 0.1);

    }

// org.apache.commons.math3.optimization.general.NonLinearConjugateGradientOptimizerTest::testCircleFitting
    public void testCircleFitting() {
        CircleScalar circle = new CircleScalar();
        circle.addPoint( 30.0,  68.0);
        circle.addPoint( 50.0,  -6.0);
        circle.addPoint(110.0, -20.0);
        circle.addPoint( 35.0,  15.0);
        circle.addPoint( 45.0,  97.0);
        NonLinearConjugateGradientOptimizer optimizer =
            new NonLinearConjugateGradientOptimizer(ConjugateGradientFormula.POLAK_RIBIERE,
                                                    new SimpleValueChecker(1e-30, 1e-30),
                                                    new BrentSolver(1e-15, 1e-13));
        PointValuePair optimum =
            optimizer.optimize(100, circle, GoalType.MINIMIZE, new double[] { 98.680, 47.345 });
        Vector2D center = new Vector2D(optimum.getPointRef()[0], optimum.getPointRef()[1]);
        Assert.assertEquals(69.960161753, circle.getRadius(center), 1.0e-8);
        Assert.assertEquals(96.075902096, center.getX(), 1.0e-8);
        Assert.assertEquals(48.135167894, center.getY(), 1.0e-8);
    }

// org.apache.commons.math3.util.MathArraysTest::testEbeAddPrecondition
    public void testEbeAddPrecondition() {
        MathArrays.ebeAdd(new double[3], new double[4]);
    }

// org.apache.commons.math3.util.MathArraysTest::testEbeSubtractPrecondition
    public void testEbeSubtractPrecondition() {
        MathArrays.ebeSubtract(new double[3], new double[4]);
    }

// org.apache.commons.math3.util.MathArraysTest::testEbeMultiplyPrecondition
    public void testEbeMultiplyPrecondition() {
        MathArrays.ebeMultiply(new double[3], new double[4]);
    }

// org.apache.commons.math3.util.MathArraysTest::testEbeDividePrecondition
    public void testEbeDividePrecondition() {
        MathArrays.ebeDivide(new double[3], new double[4]);
    }

// org.apache.commons.math3.util.MathArraysTest::testEbeAdd
    public void testEbeAdd() {
        final double[] a = { 0, 1, 2 };
        final double[] b = { 3, 5, 7 };
        final double[] r = MathArrays.ebeAdd(a, b);

        for (int i = 0; i < a.length; i++) {
            Assert.assertEquals(a[i] + b[i], r[i], 0);
        }
    }

// org.apache.commons.math3.util.MathArraysTest::testEbeSubtract
    public void testEbeSubtract() {
        final double[] a = { 0, 1, 2 };
        final double[] b = { 3, 5, 7 };
        final double[] r = MathArrays.ebeSubtract(a, b);

        for (int i = 0; i < a.length; i++) {
            Assert.assertEquals(a[i] - b[i], r[i], 0);
        }
    }

// org.apache.commons.math3.util.MathArraysTest::testEbeMultiply
    public void testEbeMultiply() {
        final double[] a = { 0, 1, 2 };
        final double[] b = { 3, 5, 7 };
        final double[] r = MathArrays.ebeMultiply(a, b);

        for (int i = 0; i < a.length; i++) {
            Assert.assertEquals(a[i] * b[i], r[i], 0);
        }
    }

// org.apache.commons.math3.util.MathArraysTest::testEbeDivide
    public void testEbeDivide() {
        final double[] a = { 0, 1, 2 };
        final double[] b = { 3, 5, 7 };
        final double[] r = MathArrays.ebeDivide(a, b);

        for (int i = 0; i < a.length; i++) {
            Assert.assertEquals(a[i] / b[i], r[i], 0);
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
