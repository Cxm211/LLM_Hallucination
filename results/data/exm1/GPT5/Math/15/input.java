// buggy code
    public static double pow(double x, double y) {
        final double lns[] = new double[2];

        if (y == 0.0) {
            return 1.0;
        }

        if (x != x) { // X is NaN
            return x;
        }


        if (x == 0) {
            long bits = Double.doubleToLongBits(x);
            if ((bits & 0x8000000000000000L) != 0) {
                // -zero
                long yi = (long) y;

                if (y < 0 && y == yi && (yi & 1) == 1) {
                    return Double.NEGATIVE_INFINITY;
                }

                if (y > 0 && y == yi && (yi & 1) == 1) {
                    return -0.0;
                }
            }

            if (y < 0) {
                return Double.POSITIVE_INFINITY;
            }
            if (y > 0) {
                return 0.0;
            }

            return Double.NaN;
        }

        if (x == Double.POSITIVE_INFINITY) {
            if (y != y) { // y is NaN
                return y;
            }
            if (y < 0.0) {
                return 0.0;
            } else {
                return Double.POSITIVE_INFINITY;
            }
        }

        if (y == Double.POSITIVE_INFINITY) {
            if (x * x == 1.0) {
                return Double.NaN;
            }

            if (x * x > 1.0) {
                return Double.POSITIVE_INFINITY;
            } else {
                return 0.0;
            }
        }

        if (x == Double.NEGATIVE_INFINITY) {
            if (y != y) { // y is NaN
                return y;
            }

            if (y < 0) {
                long yi = (long) y;
                if (y == yi && (yi & 1) == 1) {
                    return -0.0;
                }

                return 0.0;
            }

            if (y > 0)  {
                long yi = (long) y;
                if (y == yi && (yi & 1) == 1) {
                    return Double.NEGATIVE_INFINITY;
                }

                return Double.POSITIVE_INFINITY;
            }
        }

        if (y == Double.NEGATIVE_INFINITY) {

            if (x * x == 1.0) {
                return Double.NaN;
            }

            if (x * x < 1.0) {
                return Double.POSITIVE_INFINITY;
            } else {
                return 0.0;
            }
        }

        /* Handle special case x<0 */
        if (x < 0) {
            // y is an even integer in this case
            if (y >= TWO_POWER_52 || y <= -TWO_POWER_52) {
                return pow(-x, y);
            }

            if (y == (long) y) {
                // If y is an integer
                return ((long)y & 1) == 0 ? pow(-x, y) : -pow(-x, y);
            } else {
                return Double.NaN;
            }
        }

        /* Split y into ya and yb such that y = ya+yb */
        double ya;
        double yb;
        if (y < 8e298 && y > -8e298) {
            double tmp1 = y * HEX_40000000;
            ya = y + tmp1 - tmp1;
            yb = y - ya;
        } else {
            double tmp1 = y * 9.31322574615478515625E-10;
            double tmp2 = tmp1 * 9.31322574615478515625E-10;
            ya = (tmp1 + tmp2 - tmp1) * HEX_40000000 * HEX_40000000;
            yb = y - ya;
        }

        /* Compute ln(x) */
        final double lores = log(x, lns);
        if (Double.isInfinite(lores)){ // don't allow this to be converted to NaN
            return lores;
        }

        double lna = lns[0];
        double lnb = lns[1];

        /* resplit lns */
        double tmp1 = lna * HEX_40000000;
        double tmp2 = lna + tmp1 - tmp1;
        lnb += lna - tmp2;
        lna = tmp2;

        // y*ln(x) = (aa+ab)
        final double aa = lna * ya;
        final double ab = lna * yb + lnb * ya + lnb * yb;

        lna = aa+ab;
        lnb = -(lna - aa - ab);

        double z = 1.0 / 120.0;
        z = z * lnb + (1.0 / 24.0);
        z = z * lnb + (1.0 / 6.0);
        z = z * lnb + 0.5;
        z = z * lnb + 1.0;
        z = z * lnb;

        final double result = exp(lna, z, null);
        //result = result + result * z;
        return result;
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
                    double f = x + 5 * x * y - 2 * z + FastMath.pow(8 * z * x - y, 3);
                    Assert.assertEquals(f, ds.getValue(),
                                        FastMath.abs(epsilon * f));

                    
                    double dfdx = 1 + 5 * y + 24 * z * FastMath.pow(8 * z * x - y, 2);
                    Assert.assertEquals(dfdx, ds.getPartialDerivative(1, 0, 0),
                                        FastMath.abs(epsilon * dfdx));

                    
                    double dfdxdy = 5 + 48 * z * (y - 8 * z * x);
                    Assert.assertEquals(dfdxdy, ds.getPartialDerivative(1, 1, 0),
                                        FastMath.abs(epsilon * dfdxdy));

                    
                    double dfdxdydz = 48 * (y - 16 * z * x);
                    Assert.assertEquals(dfdxdydz, ds.getPartialDerivative(1, 1, 1),
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
            Assert.assertEquals(2 - 3 * x, y.getValue(), 1.0e-20);
            Assert.assertEquals(-3.0, y.getPartialDerivative(1), 4.0e-13);
            Assert.assertEquals( 0.0, y.getPartialDerivative(2), 5.0e-11);
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
            2.776e-17, 1.742e-15, 2.385e-13, 1.329e-11, 2.668e-9, 8.873e-8
        };
       double[] maxError = new double[expectedError.length];
        for (double x = -10; x < 10; x += 0.1) {
            DerivativeStructure dsX  = new DerivativeStructure(1, maxError.length - 1, 0, x);
            DerivativeStructure yRef = gaussian.value(dsX);
            DerivativeStructure y    = f.value(dsX);
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
            1.792e-22, 6.926e-5, 56.25, 1.783e8, 2.468e14, 3.056e20, 5.857e26            
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
            DerivativeStructure[] y = f.value(new DerivativeStructure(1, 2, 0, x));
            double cos = FastMath.cos(x);
            double sin = FastMath.sin(x);
            Assert.assertEquals(cos, y[0].getValue(), 2.0e-16);
            Assert.assertEquals(sin, y[1].getValue(), 2.0e-16);
            Assert.assertEquals(-sin, y[0].getPartialDerivative(1), 5.0e-14);
            Assert.assertEquals( cos, y[1].getPartialDerivative(1), 5.0e-14);
            Assert.assertEquals(-cos, y[0].getPartialDerivative(2), 6.0e-12);
            Assert.assertEquals(-sin, y[1].getPartialDerivative(2), 6.0e-12);
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
            DerivativeStructure[][] y = f.value(new DerivativeStructure(1, 2, 0, x));
            double cos = FastMath.cos(x);
            double sin = FastMath.sin(x);
            double cosh = FastMath.cosh(x);
            double sinh = FastMath.sinh(x);
            Assert.assertEquals(cos,   y[0][0].getValue(), 7.0e-18);
            Assert.assertEquals(sin,   y[0][1].getValue(), 7.0e-18);
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
            1.110e-16, 2.66e-12, 4.803e-9, 5.486e-5
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

// org.apache.commons.math3.analysis.function.StepFunctionTest::testPreconditions1
    public void testPreconditions1() {
        new StepFunction(null, new double[] {0, -1, -2});
    }

// org.apache.commons.math3.analysis.function.StepFunctionTest::testPreconditions2
    public void testPreconditions2() {
        new StepFunction(new double[] {0, 1}, null);
    }

// org.apache.commons.math3.analysis.function.StepFunctionTest::testPreconditions3
    public void testPreconditions3() {
        new StepFunction(new double[] {0}, new double[] {});
    }

// org.apache.commons.math3.analysis.function.StepFunctionTest::testPreconditions4
    public void testPreconditions4() {
        new StepFunction(new double[] {}, new double[] {0});
    }

// org.apache.commons.math3.analysis.function.StepFunctionTest::testPreconditions5
    public void testPreconditions5() {
        new StepFunction(new double[] {0, 1}, new double[] {0, -1, -2});
    }

// org.apache.commons.math3.analysis.function.StepFunctionTest::testPreconditions6
    public void testPreconditions6() {
        new StepFunction(new double[] {1, 0, 1}, new double[] {0, -1, -2});
    }

// org.apache.commons.math3.analysis.function.StepFunctionTest::testSomeValues
    public void testSomeValues() {
        final double[] x = { -2, -0.5, 0, 1.9, 7.4, 21.3 };
        final double[] y = { 4, -1, -5.5, 0.4, 5.8, 51.2 };

        final UnivariateFunction f = new StepFunction(x, y);

        Assert.assertEquals(4, f.value(Double.NEGATIVE_INFINITY), EPS);
        Assert.assertEquals(4, f.value(-10), EPS);
        Assert.assertEquals(-1, f.value(-0.4), EPS);
        Assert.assertEquals(-5.5, f.value(0), EPS);
        Assert.assertEquals(0.4, f.value(2), EPS);
        Assert.assertEquals(5.8, f.value(10), EPS);
        Assert.assertEquals(51.2, f.value(30), EPS);
        Assert.assertEquals(51.2, f.value(Double.POSITIVE_INFINITY), EPS);
    }

// org.apache.commons.math3.analysis.function.StepFunctionTest::testEndpointBehavior
    public void testEndpointBehavior() {
        final double[] x = {0, 1, 2, 3};
        final double[] xp = {-8, 1, 2, 3};
        final double[] y = {1, 2, 3, 4};
        final UnivariateFunction f = new StepFunction(x, y);
        final UnivariateFunction fp = new StepFunction(xp, y);
        Assert.assertEquals(f.value(-8), fp.value(-8), EPS);
        Assert.assertEquals(f.value(-10), fp.value(-10), EPS);
        Assert.assertEquals(f.value(0), fp.value(0), EPS);
        Assert.assertEquals(f.value(0.5), fp.value(0.5), EPS);
        for (int i = 0; i < x.length; i++) {
           Assert.assertEquals(y[i], f.value(x[i]), EPS);
           if (i > 0) {
               Assert.assertEquals(y[i - 1], f.value(x[i] - 0.5), EPS); 
           } else {
               Assert.assertEquals(y[0], f.value(x[i] - 0.5), EPS); 
           }
        }
    }

// org.apache.commons.math3.analysis.function.StepFunctionTest::testHeaviside
    public void testHeaviside() {   
        final UnivariateFunction h = new StepFunction(new double[] {-1, 0},
                                                          new double[] {0, 1});

        Assert.assertEquals(0, h.value(Double.NEGATIVE_INFINITY), 0);
        Assert.assertEquals(0, h.value(-Double.MAX_VALUE), 0);
        Assert.assertEquals(0, h.value(-2), 0);
        Assert.assertEquals(0, h.value(-Double.MIN_VALUE), 0);
        Assert.assertEquals(1, h.value(0), 0);
        Assert.assertEquals(1, h.value(2), 0);
        Assert.assertEquals(1, h.value(Double.POSITIVE_INFINITY), 0);
    }

// org.apache.commons.math3.analysis.integration.IterativeLegendreGaussIntegratorTest::testSinFunction
    public void testSinFunction() {
        UnivariateFunction f = new Sin();
        BaseAbstractUnivariateIntegrator integrator
            = new IterativeLegendreGaussIntegrator(5, 1.0e-14, 1.0e-10, 2, 15);
        double min, max, expected, result, tolerance;

        min = 0; max = FastMath.PI; expected = 2;
        tolerance = FastMath.max(integrator.getAbsoluteAccuracy(),
                             FastMath.abs(expected * integrator.getRelativeAccuracy()));
        result = integrator.integrate(10000, f, min, max);
        Assert.assertEquals(expected, result, tolerance);

        min = -FastMath.PI/3; max = 0; expected = -0.5;
        tolerance = FastMath.max(integrator.getAbsoluteAccuracy(),
                FastMath.abs(expected * integrator.getRelativeAccuracy()));
        result = integrator.integrate(10000, f, min, max);
        Assert.assertEquals(expected, result, tolerance);
    }

// org.apache.commons.math3.analysis.integration.IterativeLegendreGaussIntegratorTest::testQuinticFunction
    public void testQuinticFunction() {
        UnivariateFunction f = new QuinticFunction();
        UnivariateIntegrator integrator =
                new IterativeLegendreGaussIntegrator(3,
                                                     BaseAbstractUnivariateIntegrator.DEFAULT_RELATIVE_ACCURACY,
                                                     BaseAbstractUnivariateIntegrator.DEFAULT_ABSOLUTE_ACCURACY,
                                                     BaseAbstractUnivariateIntegrator.DEFAULT_MIN_ITERATIONS_COUNT,
                                                     64);
        double min, max, expected, result;

        min = 0; max = 1; expected = -1.0/48;
        result = integrator.integrate(10000, f, min, max);
        Assert.assertEquals(expected, result, 1.0e-16);

        min = 0; max = 0.5; expected = 11.0/768;
        result = integrator.integrate(10000, f, min, max);
        Assert.assertEquals(expected, result, 1.0e-16);

        min = -1; max = 4; expected = 2048/3.0 - 78 + 1.0/48;
        result = integrator.integrate(10000, f, min, max);
        Assert.assertEquals(expected, result, 1.0e-16);
    }

// org.apache.commons.math3.analysis.integration.IterativeLegendreGaussIntegratorTest::testExactIntegration
    public void testExactIntegration() {
        Random random = new Random(86343623467878363l);
        for (int n = 2; n < 6; ++n) {
            IterativeLegendreGaussIntegrator integrator =
                new IterativeLegendreGaussIntegrator(n,
                                                     BaseAbstractUnivariateIntegrator.DEFAULT_RELATIVE_ACCURACY,
                                                     BaseAbstractUnivariateIntegrator.DEFAULT_ABSOLUTE_ACCURACY,
                                                     BaseAbstractUnivariateIntegrator.DEFAULT_MIN_ITERATIONS_COUNT,
                                                     64);

            
            for (int degree = 0; degree <= 2 * n - 1; ++degree) {
                for (int i = 0; i < 10; ++i) {
                    double[] coeff = new double[degree + 1];
                    for (int k = 0; k < coeff.length; ++k) {
                        coeff[k] = 2 * random.nextDouble() - 1;
                    }
                    PolynomialFunction p = new PolynomialFunction(coeff);
                    double result    = integrator.integrate(10000, p, -5.0, 15.0);
                    double reference = exactIntegration(p, -5.0, 15.0);
                    Assert.assertEquals(n + " " + degree + " " + i, reference, result, 1.0e-12 * (1.0 + FastMath.abs(reference)));
                }
            }

        }
    }

// org.apache.commons.math3.analysis.integration.IterativeLegendreGaussIntegratorTest::testIssue464
    public void testIssue464() {
        final double value = 0.2;
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return (x >= 0 && x <= 5) ? value : 0.0;
            }
        };
        IterativeLegendreGaussIntegrator gauss
            = new IterativeLegendreGaussIntegrator(5, 3, 100);

        
        double maxX = 0.32462367623786328;
        Assert.assertEquals(maxX * value, gauss.integrate(Integer.MAX_VALUE, f, -10, maxX), 1.0e-7);
        Assert.assertTrue(gauss.getEvaluations() > 37000000);
        Assert.assertTrue(gauss.getIterations() < 30);

        
        try {
            gauss.integrate(1000, f, -10, maxX);
            Assert.fail("expected TooManyEvaluationsException");
        } catch (TooManyEvaluationsException tmee) {
            
            Assert.assertEquals(1000, tmee.getMax());
        }

        
        double sum1 = gauss.integrate(1000, f, -10, 0);
        int eval1   = gauss.getEvaluations();
        double sum2 = gauss.integrate(1000, f, 0, maxX);
        int eval2   = gauss.getEvaluations();
        Assert.assertEquals(maxX * value, sum1 + sum2, 1.0e-7);
        Assert.assertTrue(eval1 + eval2 < 200);

    }

// org.apache.commons.math3.analysis.integration.LegendreGaussIntegratorTest::testSinFunction
    public void testSinFunction() {
        UnivariateFunction f = new Sin();
        BaseAbstractUnivariateIntegrator integrator = new LegendreGaussIntegrator(5, 1.0e-14, 1.0e-10, 2, 15);
        double min, max, expected, result, tolerance;

        min = 0; max = FastMath.PI; expected = 2;
        tolerance = FastMath.max(integrator.getAbsoluteAccuracy(),
                             FastMath.abs(expected * integrator.getRelativeAccuracy()));
        result = integrator.integrate(10000, f, min, max);
        Assert.assertEquals(expected, result, tolerance);

        min = -FastMath.PI/3; max = 0; expected = -0.5;
        tolerance = FastMath.max(integrator.getAbsoluteAccuracy(),
                FastMath.abs(expected * integrator.getRelativeAccuracy()));
        result = integrator.integrate(10000, f, min, max);
        Assert.assertEquals(expected, result, tolerance);
    }

// org.apache.commons.math3.analysis.integration.LegendreGaussIntegratorTest::testQuinticFunction
    public void testQuinticFunction() {
        UnivariateFunction f = new QuinticFunction();
        UnivariateIntegrator integrator =
                new LegendreGaussIntegrator(3,
                                            BaseAbstractUnivariateIntegrator.DEFAULT_RELATIVE_ACCURACY,
                                            BaseAbstractUnivariateIntegrator.DEFAULT_ABSOLUTE_ACCURACY,
                                            BaseAbstractUnivariateIntegrator.DEFAULT_MIN_ITERATIONS_COUNT,
                                            64);
        double min, max, expected, result;

        min = 0; max = 1; expected = -1.0/48;
        result = integrator.integrate(10000, f, min, max);
        Assert.assertEquals(expected, result, 1.0e-16);

        min = 0; max = 0.5; expected = 11.0/768;
        result = integrator.integrate(10000, f, min, max);
        Assert.assertEquals(expected, result, 1.0e-16);

        min = -1; max = 4; expected = 2048/3.0 - 78 + 1.0/48;
        result = integrator.integrate(10000, f, min, max);
        Assert.assertEquals(expected, result, 1.0e-16);
    }

// org.apache.commons.math3.analysis.integration.LegendreGaussIntegratorTest::testExactIntegration
    public void testExactIntegration() {
        Random random = new Random(86343623467878363l);
        for (int n = 2; n < 6; ++n) {
            LegendreGaussIntegrator integrator =
                new LegendreGaussIntegrator(n,
                                            BaseAbstractUnivariateIntegrator.DEFAULT_RELATIVE_ACCURACY,
                                            BaseAbstractUnivariateIntegrator.DEFAULT_ABSOLUTE_ACCURACY,
                                            BaseAbstractUnivariateIntegrator.DEFAULT_MIN_ITERATIONS_COUNT,
                                            64);

            
            for (int degree = 0; degree <= 2 * n - 1; ++degree) {
                for (int i = 0; i < 10; ++i) {
                    double[] coeff = new double[degree + 1];
                    for (int k = 0; k < coeff.length; ++k) {
                        coeff[k] = 2 * random.nextDouble() - 1;
                    }
                    PolynomialFunction p = new PolynomialFunction(coeff);
                    double result    = integrator.integrate(10000, p, -5.0, 15.0);
                    double reference = exactIntegration(p, -5.0, 15.0);
                    Assert.assertEquals(n + " " + degree + " " + i, reference, result, 1.0e-12 * (1.0 + FastMath.abs(reference)));
                }
            }

        }
    }

// org.apache.commons.math3.analysis.integration.LegendreGaussIntegratorTest::testIssue464
    public void testIssue464() {
        final double value = 0.2;
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return (x >= 0 && x <= 5) ? value : 0.0;
            }
        };
        LegendreGaussIntegrator gauss = new LegendreGaussIntegrator(5, 3, 100);

        
        double maxX = 0.32462367623786328;
        Assert.assertEquals(maxX * value, gauss.integrate(Integer.MAX_VALUE, f, -10, maxX), 1.0e-7);
        Assert.assertTrue(gauss.getEvaluations() > 37000000);
        Assert.assertTrue(gauss.getIterations() < 30);

        
        try {
            gauss.integrate(1000, f, -10, maxX);
            Assert.fail("expected TooManyEvaluationsException");
        } catch (TooManyEvaluationsException tmee) {
            
            Assert.assertEquals(1000, tmee.getMax());
        }

        
        double sum1 = gauss.integrate(1000, f, -10, 0);
        int eval1   = gauss.getEvaluations();
        double sum2 = gauss.integrate(1000, f, 0, maxX);
        int eval2   = gauss.getEvaluations();
        Assert.assertEquals(maxX * value, sum1 + sum2, 1.0e-7);
        Assert.assertTrue(eval1 + eval2 < 200);

    }

// org.apache.commons.math3.analysis.integration.RombergIntegratorTest::testSinFunction
    public void testSinFunction() {
        UnivariateFunction f = new Sin();
        UnivariateIntegrator integrator = new RombergIntegrator();
        double min, max, expected, result, tolerance;

        min = 0; max = FastMath.PI; expected = 2;
        tolerance = FastMath.abs(expected * integrator.getRelativeAccuracy());
        result = integrator.integrate(100, f, min, max);
        Assert.assertTrue(integrator.getEvaluations() < 50);
        Assert.assertTrue(integrator.getIterations()  < 10);
        Assert.assertEquals(expected, result, tolerance);

        min = -FastMath.PI/3; max = 0; expected = -0.5;
        tolerance = FastMath.abs(expected * integrator.getRelativeAccuracy());
        result = integrator.integrate(100, f, min, max);
        Assert.assertTrue(integrator.getEvaluations() < 50);
        Assert.assertTrue(integrator.getIterations()  < 10);
        Assert.assertEquals(expected, result, tolerance);
    }

// org.apache.commons.math3.analysis.integration.RombergIntegratorTest::testQuinticFunction
    public void testQuinticFunction() {
        UnivariateFunction f = new QuinticFunction();
        UnivariateIntegrator integrator = new RombergIntegrator();
        double min, max, expected, result, tolerance;

        min = 0; max = 1; expected = -1.0/48;
        tolerance = FastMath.abs(expected * integrator.getRelativeAccuracy());
        result = integrator.integrate(100, f, min, max);
        Assert.assertTrue(integrator.getEvaluations() < 10);
        Assert.assertTrue(integrator.getIterations()  < 5);
        Assert.assertEquals(expected, result, tolerance);

        min = 0; max = 0.5; expected = 11.0/768;
        tolerance = FastMath.abs(expected * integrator.getRelativeAccuracy());
        result = integrator.integrate(100, f, min, max);
        Assert.assertTrue(integrator.getEvaluations() < 10);
        Assert.assertTrue(integrator.getIterations()  < 5);
        Assert.assertEquals(expected, result, tolerance);

        min = -1; max = 4; expected = 2048/3.0 - 78 + 1.0/48;
        tolerance = FastMath.abs(expected * integrator.getRelativeAccuracy());
        result = integrator.integrate(100, f, min, max);
        Assert.assertTrue(integrator.getEvaluations() < 10);
        Assert.assertTrue(integrator.getIterations()  < 5);
        Assert.assertEquals(expected, result, tolerance);
    }

// org.apache.commons.math3.analysis.integration.RombergIntegratorTest::testParameters
    public void testParameters() {
        UnivariateFunction f = new Sin();

        try {
            
            new RombergIntegrator().integrate(1000, f, 1, -1);
            Assert.fail("Expecting NumberIsTooLargeException - bad interval");
        } catch (NumberIsTooLargeException ex) {
            
        }
        try {
            
            new RombergIntegrator(5, 4);
            Assert.fail("Expecting NumberIsTooSmallException - bad iteration limits");
        } catch (NumberIsTooSmallException ex) {
            
        }
        try {
            
            new RombergIntegrator(10, 50);
            Assert.fail("Expecting NumberIsTooLargeException - bad iteration limits");
        } catch (NumberIsTooLargeException ex) {
            
        }
    }

// org.apache.commons.math3.analysis.integration.SimpsonIntegratorTest::testSinFunction
    public void testSinFunction() {
        UnivariateFunction f = new Sin();
        UnivariateIntegrator integrator = new SimpsonIntegrator();
        double min, max, expected, result, tolerance;

        min = 0; max = FastMath.PI; expected = 2;
        tolerance = FastMath.abs(expected * integrator.getRelativeAccuracy());
        result = integrator.integrate(1000, f, min, max);
        Assert.assertTrue(integrator.getEvaluations() < 100);
        Assert.assertTrue(integrator.getIterations()  < 10);
        Assert.assertEquals(expected, result, tolerance);

        min = -FastMath.PI/3; max = 0; expected = -0.5;
        tolerance = FastMath.abs(expected * integrator.getRelativeAccuracy());
        result = integrator.integrate(1000, f, min, max);
        Assert.assertTrue(integrator.getEvaluations() < 50);
        Assert.assertTrue(integrator.getIterations()  < 10);
        Assert.assertEquals(expected, result, tolerance);
    }

// org.apache.commons.math3.analysis.integration.SimpsonIntegratorTest::testQuinticFunction
    public void testQuinticFunction() {
        UnivariateFunction f = new QuinticFunction();
        UnivariateIntegrator integrator = new SimpsonIntegrator();
        double min, max, expected, result, tolerance;

        min = 0; max = 1; expected = -1.0/48;
        tolerance = FastMath.abs(expected * integrator.getRelativeAccuracy());
        result = integrator.integrate(1000, f, min, max);
        Assert.assertTrue(integrator.getEvaluations() < 150);
        Assert.assertTrue(integrator.getIterations()  < 10);
        Assert.assertEquals(expected, result, tolerance);

        min = 0; max = 0.5; expected = 11.0/768;
        tolerance = FastMath.abs(expected * integrator.getRelativeAccuracy());
        result = integrator.integrate(1000, f, min, max);
        Assert.assertTrue(integrator.getEvaluations() < 100);
        Assert.assertTrue(integrator.getIterations()  < 10);
        Assert.assertEquals(expected, result, tolerance);

        min = -1; max = 4; expected = 2048/3.0 - 78 + 1.0/48;
        tolerance = FastMath.abs(expected * integrator.getRelativeAccuracy());
        result = integrator.integrate(1000, f, min, max);
        Assert.assertTrue(integrator.getEvaluations() < 150);
        Assert.assertTrue(integrator.getIterations()  < 10);
        Assert.assertEquals(expected, result, tolerance);
    }

// org.apache.commons.math3.analysis.integration.SimpsonIntegratorTest::testParameters
    public void testParameters() {
        UnivariateFunction f = new Sin();
        try {
            
            new SimpsonIntegrator().integrate(1000, f, 1, -1);
            Assert.fail("Expecting NumberIsTooLargeException - bad interval");
        } catch (NumberIsTooLargeException ex) {
            
        }
        try {
            
            new SimpsonIntegrator(5, 4);
            Assert.fail("Expecting NumberIsTooSmallException - bad iteration limits");
        } catch (NumberIsTooSmallException ex) {
            
        }
        try {
            
            new SimpsonIntegrator(10, 99);
            Assert.fail("Expecting NumberIsTooLargeException - bad iteration limits");
        } catch (NumberIsTooLargeException ex) {
            
        }
    }

// org.apache.commons.math3.analysis.integration.TrapezoidIntegratorTest::testSinFunction
    public void testSinFunction() {
        UnivariateFunction f = new Sin();
        UnivariateIntegrator integrator = new TrapezoidIntegrator();
        double min, max, expected, result, tolerance;

        min = 0; max = FastMath.PI; expected = 2;
        tolerance = FastMath.abs(expected * integrator.getRelativeAccuracy());
        result = integrator.integrate(10000, f, min, max);
        Assert.assertTrue(integrator.getEvaluations() < 2500);
        Assert.assertTrue(integrator.getIterations()  < 15);
        Assert.assertEquals(expected, result, tolerance);

        min = -FastMath.PI/3; max = 0; expected = -0.5;
        tolerance = FastMath.abs(expected * integrator.getRelativeAccuracy());
        result = integrator.integrate(10000, f, min, max);
        Assert.assertTrue(integrator.getEvaluations() < 2500);
        Assert.assertTrue(integrator.getIterations()  < 15);
        Assert.assertEquals(expected, result, tolerance);
    }

// org.apache.commons.math3.analysis.integration.TrapezoidIntegratorTest::testQuinticFunction
    public void testQuinticFunction() {
        UnivariateFunction f = new QuinticFunction();
        UnivariateIntegrator integrator = new TrapezoidIntegrator();
        double min, max, expected, result, tolerance;

        min = 0; max = 1; expected = -1.0/48;
        tolerance = FastMath.abs(expected * integrator.getRelativeAccuracy());
        result = integrator.integrate(10000, f, min, max);
        Assert.assertTrue(integrator.getEvaluations() < 5000);
        Assert.assertTrue(integrator.getIterations()  < 15);
        Assert.assertEquals(expected, result, tolerance);

        min = 0; max = 0.5; expected = 11.0/768;
        tolerance = FastMath.abs(expected * integrator.getRelativeAccuracy());
        result = integrator.integrate(10000, f, min, max);
        Assert.assertTrue(integrator.getEvaluations() < 2500);
        Assert.assertTrue(integrator.getIterations()  < 15);
        Assert.assertEquals(expected, result, tolerance);

        min = -1; max = 4; expected = 2048/3.0 - 78 + 1.0/48;
        tolerance = FastMath.abs(expected * integrator.getRelativeAccuracy());
        result = integrator.integrate(10000, f, min, max);
        Assert.assertTrue(integrator.getEvaluations() < 5000);
        Assert.assertTrue(integrator.getIterations()  < 15);
        Assert.assertEquals(expected, result, tolerance);

    }

// org.apache.commons.math3.analysis.integration.TrapezoidIntegratorTest::testParameters
    public void testParameters() {
        UnivariateFunction f = new Sin();

        try {
            
            new TrapezoidIntegrator().integrate(1000, f, 1, -1);
            Assert.fail("Expecting NumberIsTooLargeException - bad interval");
        } catch (NumberIsTooLargeException ex) {
            
        }
        try {
            
            new TrapezoidIntegrator(5, 4);
            Assert.fail("Expecting NumberIsTooSmallException - bad iteration limits");
        } catch (NumberIsTooSmallException ex) {
            
        }
        try {
            
            new TrapezoidIntegrator(10,99);
            Assert.fail("Expecting NumberIsTooLargeException - bad iteration limits");
        } catch (NumberIsTooLargeException ex) {
            
        }
    }

// org.apache.commons.math3.analysis.integration.gauss.LegendreHighPrecisionTest::testCos
    public void testCos() {
        final UnivariateFunction cos = new Cos();

        final GaussIntegrator integrator = factory.legendreHighPrecision(7, 0, Math.PI / 2);
        final double s = integrator.integrate(cos);
        
        Assert.assertEquals(1, s, Math.ulp(1d));
    }

// org.apache.commons.math3.analysis.integration.gauss.LegendreHighPrecisionTest::testInverse
    public void testInverse() {
        final UnivariateFunction inv = new Inverse();
        final UnivariateFunction log = new Log();

        final double lo = 12.34;
        final double hi = 456.78;

        final GaussIntegrator integrator = factory.legendreHighPrecision(60, lo, hi);
        final double s = integrator.integrate(inv);
        final double expected = log.value(hi) - log.value(lo);
        
        Assert.assertEquals(expected, s, 1e-15);
    }

// org.apache.commons.math3.analysis.integration.gauss.LegendreTest::testCos
    public void testCos() {
        final UnivariateFunction cos = new Cos();

        final GaussIntegrator integrator = factory.legendre(7, 0, Math.PI / 2);
        final double s = integrator.integrate(cos);
        
        Assert.assertEquals(1, s, Math.ulp(1d));
    }

// org.apache.commons.math3.analysis.integration.gauss.LegendreTest::testInverse
    public void testInverse() {
        final UnivariateFunction inv = new Inverse();
        final UnivariateFunction log = new Log();

        final double lo = 12.34;
        final double hi = 456.78;

        final GaussIntegrator integrator = factory.legendre(60, lo, hi);
        final double s = integrator.integrate(inv);
        final double expected = log.value(hi) - log.value(lo);
        
        Assert.assertEquals(expected, s, 1e-14);
    }

// org.apache.commons.math3.analysis.interpolation.DividedDifferenceInterpolatorTest::testSinFunction
    public void testSinFunction() {
        UnivariateFunction f = new Sin();
        UnivariateInterpolator interpolator = new DividedDifferenceInterpolator();
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
        UnivariateFunction p = interpolator.interpolate(x, y);

        z = FastMath.PI / 4; expected = f.value(z); result = p.value(z);
        tolerance = FastMath.abs(derivativebound * partialerror(x, z));
        Assert.assertEquals(expected, result, tolerance);

        z = FastMath.PI * 1.5; expected = f.value(z); result = p.value(z);
        tolerance = FastMath.abs(derivativebound * partialerror(x, z));
        Assert.assertEquals(expected, result, tolerance);
    }

// org.apache.commons.math3.analysis.interpolation.DividedDifferenceInterpolatorTest::testExpm1Function
    public void testExpm1Function() {
        UnivariateFunction f = new Expm1();
        UnivariateInterpolator interpolator = new DividedDifferenceInterpolator();
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
        UnivariateFunction p = interpolator.interpolate(x, y);

        z = 0.0; expected = f.value(z); result = p.value(z);
        tolerance = FastMath.abs(derivativebound * partialerror(x, z));
        Assert.assertEquals(expected, result, tolerance);

        z = 0.5; expected = f.value(z); result = p.value(z);
        tolerance = FastMath.abs(derivativebound * partialerror(x, z));
        Assert.assertEquals(expected, result, tolerance);

        z = -0.5; expected = f.value(z); result = p.value(z);
        tolerance = FastMath.abs(derivativebound * partialerror(x, z));
        Assert.assertEquals(expected, result, tolerance);
    }

// org.apache.commons.math3.analysis.interpolation.DividedDifferenceInterpolatorTest::testParameters
    public void testParameters() {
        UnivariateInterpolator interpolator = new DividedDifferenceInterpolator();

        try {
            
            double x[] = { 1.0, 2.0, 2.0, 4.0 };
            double y[] = { 0.0, 4.0, 4.0, 2.5 };
            UnivariateFunction p = interpolator.interpolate(x, y);
            p.value(0.0);
            Assert.fail("Expecting NonMonotonicSequenceException - bad abscissas array");
        } catch (NonMonotonicSequenceException ex) {
            
        }
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

// org.apache.commons.math3.analysis.interpolation.LinearInterpolatorTest::testInterpolateLinearDegenerateTwoSegment
    public void testInterpolateLinearDegenerateTwoSegment()
        {
        double x[] = { 0.0, 0.5, 1.0 };
        double y[] = { 0.0, 0.5, 1.0 };
        UnivariateInterpolator i = new LinearInterpolator();
        UnivariateFunction f = i.interpolate(x, y);
        verifyInterpolation(f, x, y);

        
        PolynomialFunction polynomials[] = ((PolynomialSplineFunction) f).getPolynomials();
        double target[] = {y[0], 1d};
        TestUtils.assertEquals(polynomials[0].getCoefficients(), target, coefficientTolerance);
        target = new double[]{y[1], 1d};
        TestUtils.assertEquals(polynomials[1].getCoefficients(), target, coefficientTolerance);

        
        Assert.assertEquals(0.0,f.value(0.0), interpolationTolerance);
        Assert.assertEquals(0.4,f.value(0.4), interpolationTolerance);
        Assert.assertEquals(1.0,f.value(1.0), interpolationTolerance);
    }

// org.apache.commons.math3.analysis.interpolation.LinearInterpolatorTest::testInterpolateLinearDegenerateThreeSegment
    public void testInterpolateLinearDegenerateThreeSegment()
        {
        double x[] = { 0.0, 0.5, 1.0, 1.5 };
        double y[] = { 0.0, 0.5, 1.0, 1.5 };
        UnivariateInterpolator i = new LinearInterpolator();
        UnivariateFunction f = i.interpolate(x, y);
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

// org.apache.commons.math3.analysis.interpolation.LinearInterpolatorTest::testInterpolateLinear
    public void testInterpolateLinear() {
        double x[] = { 0.0, 0.5, 1.0 };
        double y[] = { 0.0, 0.5, 0.0 };
        UnivariateInterpolator i = new LinearInterpolator();
        UnivariateFunction f = i.interpolate(x, y);
        verifyInterpolation(f, x, y);

        
        PolynomialFunction polynomials[] = ((PolynomialSplineFunction) f).getPolynomials();
        double target[] = {y[0], 1d};
        TestUtils.assertEquals(polynomials[0].getCoefficients(), target, coefficientTolerance);
        target = new double[]{y[1], -1d};
        TestUtils.assertEquals(polynomials[1].getCoefficients(), target, coefficientTolerance);
    }

// org.apache.commons.math3.analysis.interpolation.LinearInterpolatorTest::testIllegalArguments
    public void testIllegalArguments() {
        
        UnivariateInterpolator i = new LinearInterpolator();
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
        } catch (NonMonotonicSequenceException iae) {
            
        }
        
        try {
            double xval[] = { 0.0 };
            double yval[] = { 0.0 };
            i.interpolate(xval, yval);
            Assert.fail("Failed to detect unsorted arguments.");
        } catch (NumberIsTooSmallException iae) {
            
        }
    }

// org.apache.commons.math3.analysis.interpolation.LoessInterpolatorTest::testOnOnePoint
    public void testOnOnePoint() {
        double[] xval = {0.5};
        double[] yval = {0.7};
        double[] res = new LoessInterpolator().smooth(xval, yval);
        Assert.assertEquals(1, res.length);
        Assert.assertEquals(0.7, res[0], 0.0);
    }

// org.apache.commons.math3.analysis.interpolation.LoessInterpolatorTest::testOnTwoPoints
    public void testOnTwoPoints() {
        double[] xval = {0.5, 0.6};
        double[] yval = {0.7, 0.8};
        double[] res = new LoessInterpolator().smooth(xval, yval);
        Assert.assertEquals(2, res.length);
        Assert.assertEquals(0.7, res[0], 0.0);
        Assert.assertEquals(0.8, res[1], 0.0);
    }

// org.apache.commons.math3.analysis.interpolation.LoessInterpolatorTest::testOnStraightLine
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

// org.apache.commons.math3.analysis.interpolation.LoessInterpolatorTest::testOnDistortedSine
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

// org.apache.commons.math3.analysis.interpolation.LoessInterpolatorTest::testIncreasingBandwidthIncreasesSmoothness
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

// org.apache.commons.math3.analysis.interpolation.LoessInterpolatorTest::testIncreasingRobustnessItersIncreasesSmoothnessWithOutliers
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

// org.apache.commons.math3.analysis.interpolation.LoessInterpolatorTest::testUnequalSizeArguments
    public void testUnequalSizeArguments() {
        new LoessInterpolator().smooth(new double[] {1,2,3}, new double[] {1,2,3,4});
    }

// org.apache.commons.math3.analysis.interpolation.LoessInterpolatorTest::testEmptyData
    public void testEmptyData() {
        new LoessInterpolator().smooth(new double[] {}, new double[] {});
    }

// org.apache.commons.math3.analysis.interpolation.LoessInterpolatorTest::testNonStrictlyIncreasing1
    public void testNonStrictlyIncreasing1() {
        new LoessInterpolator().smooth(new double[] {4,3,1,2}, new double[] {3,4,5,6});
    }

// org.apache.commons.math3.analysis.interpolation.LoessInterpolatorTest::testNonStrictlyIncreasing2
    public void testNonStrictlyIncreasing2() {
        new LoessInterpolator().smooth(new double[] {1,2,2,3}, new double[] {3,4,5,6});
    }

// org.apache.commons.math3.analysis.interpolation.LoessInterpolatorTest::testNotAllFiniteReal1
    public void testNotAllFiniteReal1() {
        new LoessInterpolator().smooth(new double[] {1,2,Double.NaN}, new double[] {3,4,5});
    }

// org.apache.commons.math3.analysis.interpolation.LoessInterpolatorTest::testNotAllFiniteReal2
    public void testNotAllFiniteReal2() {
        new LoessInterpolator().smooth(new double[] {1,2,Double.POSITIVE_INFINITY}, new double[] {3,4,5});
    }

// org.apache.commons.math3.analysis.interpolation.LoessInterpolatorTest::testNotAllFiniteReal3
    public void testNotAllFiniteReal3() {
        new LoessInterpolator().smooth(new double[] {1,2,Double.NEGATIVE_INFINITY}, new double[] {3,4,5});
    }

// org.apache.commons.math3.analysis.interpolation.LoessInterpolatorTest::testNotAllFiniteReal4
    public void testNotAllFiniteReal4() {
        new LoessInterpolator().smooth(new double[] {3,4,5}, new double[] {1,2,Double.NaN});
    }

// org.apache.commons.math3.analysis.interpolation.LoessInterpolatorTest::testNotAllFiniteReal5
    public void testNotAllFiniteReal5() {
        new LoessInterpolator().smooth(new double[] {3,4,5}, new double[] {1,2,Double.POSITIVE_INFINITY});
    }

// org.apache.commons.math3.analysis.interpolation.LoessInterpolatorTest::testNotAllFiniteReal6
    public void testNotAllFiniteReal6() {
        new LoessInterpolator().smooth(new double[] {3,4,5}, new double[] {1,2,Double.NEGATIVE_INFINITY});
    }

// org.apache.commons.math3.analysis.interpolation.LoessInterpolatorTest::testInsufficientBandwidth
    public void testInsufficientBandwidth() {
        LoessInterpolator li = new LoessInterpolator(0.1, 3, 1e-12);
        li.smooth(new double[] {1,2,3,4,5,6,7,8,9,10,11,12}, new double[] {1,2,3,4,5,6,7,8,9,10,11,12});
    }

// org.apache.commons.math3.analysis.interpolation.LoessInterpolatorTest::testCompletelyIncorrectBandwidth1
    public void testCompletelyIncorrectBandwidth1() {
        new LoessInterpolator(-0.2, 3, 1e-12);
    }

// org.apache.commons.math3.analysis.interpolation.LoessInterpolatorTest::testCompletelyIncorrectBandwidth2
    public void testCompletelyIncorrectBandwidth2() {
        new LoessInterpolator(1.1, 3, 1e-12);
    }

// org.apache.commons.math3.analysis.interpolation.LoessInterpolatorTest::testMath296withoutWeights
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

// org.apache.commons.math3.analysis.interpolation.MicrosphereInterpolatorTest::testLinearFunction2D
    public void testLinearFunction2D() {
        MultivariateFunction f = new MultivariateFunction() {
                public double value(double[] x) {
                    if (x.length != 2) {
                        throw new IllegalArgumentException();
                    }
                    return 2 * x[0] - 3 * x[1] + 5;
                }
            };

        MultivariateInterpolator interpolator = new MicrosphereInterpolator();

        
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

        MultivariateFunction p = interpolator.interpolate(x, y);

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

// org.apache.commons.math3.analysis.interpolation.MicrosphereInterpolatorTest::testParaboloid2D
    public void testParaboloid2D() {
        MultivariateFunction f = new MultivariateFunction() {
                public double value(double[] x) {
                    if (x.length != 2) {
                        throw new IllegalArgumentException();
                    }
                    return 2 * x[0] * x[0] - 3 * x[1] * x[1] + 4 * x[0] * x[1] - 5;
                }
            };

        MultivariateInterpolator interpolator = new MicrosphereInterpolator();

        
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

        MultivariateFunction p = interpolator.interpolate(x, y);

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

// org.apache.commons.math3.analysis.interpolation.NevilleInterpolatorTest::testSinFunction
    public void testSinFunction() {
        UnivariateFunction f = new Sin();
        UnivariateInterpolator interpolator = new NevilleInterpolator();
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
        UnivariateFunction p = interpolator.interpolate(x, y);

        z = FastMath.PI / 4; expected = f.value(z); result = p.value(z);
        tolerance = FastMath.abs(derivativebound * partialerror(x, z));
        Assert.assertEquals(expected, result, tolerance);

        z = FastMath.PI * 1.5; expected = f.value(z); result = p.value(z);
        tolerance = FastMath.abs(derivativebound * partialerror(x, z));
        Assert.assertEquals(expected, result, tolerance);
    }

// org.apache.commons.math3.analysis.interpolation.NevilleInterpolatorTest::testExpm1Function
    public void testExpm1Function() {
        UnivariateFunction f = new Expm1();
        UnivariateInterpolator interpolator = new NevilleInterpolator();
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
        UnivariateFunction p = interpolator.interpolate(x, y);

        z = 0.0; expected = f.value(z); result = p.value(z);
        tolerance = FastMath.abs(derivativebound * partialerror(x, z));
        Assert.assertEquals(expected, result, tolerance);

        z = 0.5; expected = f.value(z); result = p.value(z);
        tolerance = FastMath.abs(derivativebound * partialerror(x, z));
        Assert.assertEquals(expected, result, tolerance);

        z = -0.5; expected = f.value(z); result = p.value(z);
        tolerance = FastMath.abs(derivativebound * partialerror(x, z));
        Assert.assertEquals(expected, result, tolerance);
    }

// org.apache.commons.math3.analysis.interpolation.NevilleInterpolatorTest::testParameters
    public void testParameters() {
        UnivariateInterpolator interpolator = new NevilleInterpolator();

        try {
            
            double x[] = { 1.0, 2.0, 2.0, 4.0 };
            double y[] = { 0.0, 4.0, 4.0, 2.5 };
            UnivariateFunction p = interpolator.interpolate(x, y);
            p.value(0.0);
            Assert.fail("Expecting NonMonotonicSequenceException - bad abscissas array");
        } catch (NonMonotonicSequenceException ex) {
            
        }
    }

// org.apache.commons.math3.analysis.interpolation.SmoothingPolynomialBicubicSplineInterpolatorTest::testPreconditions
    public void testPreconditions() {
        double[] xval = new double[] {3, 4, 5, 6.5};
        double[] yval = new double[] {-4, -3, -1, 2.5};
        double[][] zval = new double[xval.length][yval.length];

        BivariateGridInterpolator interpolator = new SmoothingPolynomialBicubicSplineInterpolator(0);
        
        @SuppressWarnings("unused")
        BivariateFunction p = interpolator.interpolate(xval, yval, zval);
        
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

// org.apache.commons.math3.analysis.interpolation.SmoothingPolynomialBicubicSplineInterpolatorTest::testPlane
    public void testPlane() {
        BivariateFunction f = new BivariateFunction() {
                public double value(double x, double y) {
                    return 2 * x - 3 * y + 5
                        + ((int) (FastMath.abs(5 * x + 3 * y)) % 2 == 0 ? 1 : -1);
                }
            };

        BivariateGridInterpolator interpolator = new SmoothingPolynomialBicubicSplineInterpolator(1);

        double[] xval = new double[] {3, 4, 5, 6.5};
        double[] yval = new double[] {-4, -3, -1, 2, 2.5};
        double[][] zval = new double[xval.length][yval.length];
        for (int i = 0; i < xval.length; i++) {
            for (int j = 0; j < yval.length; j++) {
                zval[i][j] = f.value(xval[i], yval[j]);
            }
        }

        BivariateFunction p = interpolator.interpolate(xval, yval, zval);
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

// org.apache.commons.math3.analysis.interpolation.SmoothingPolynomialBicubicSplineInterpolatorTest::testParaboloid
    public void testParaboloid() {
        BivariateFunction f = new BivariateFunction() {
                public double value(double x, double y) {
                    return 2 * x * x - 3 * y * y + 4 * x * y - 5
                        + ((int) (FastMath.abs(5 * x + 3 * y)) % 2 == 0 ? 1 : -1);
                }
            };

        BivariateGridInterpolator interpolator = new SmoothingPolynomialBicubicSplineInterpolator(4);

        double[] xval = new double[] {3, 4, 5, 6.5};
        double[] yval = new double[] {-4, -3, -2, -1, 0.5, 2.5};
        double[][] zval = new double[xval.length][yval.length];
        for (int i = 0; i < xval.length; i++) {
            for (int j = 0; j < yval.length; j++) {
                zval[i][j] = f.value(xval[i], yval[j]);
            }
        }

        BivariateFunction p = interpolator.interpolate(xval, yval, zval);
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

// org.apache.commons.math3.analysis.interpolation.SplineInterpolatorTest::testInterpolateLinearDegenerateTwoSegment
    public void testInterpolateLinearDegenerateTwoSegment()
        {
        double x[] = { 0.0, 0.5, 1.0 };
        double y[] = { 0.0, 0.5, 1.0 };
        UnivariateInterpolator i = new SplineInterpolator();
        UnivariateFunction f = i.interpolate(x, y);
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

// org.apache.commons.math3.analysis.interpolation.SplineInterpolatorTest::testInterpolateLinearDegenerateThreeSegment
    public void testInterpolateLinearDegenerateThreeSegment()
        {
        double x[] = { 0.0, 0.5, 1.0, 1.5 };
        double y[] = { 0.0, 0.5, 1.0, 1.5 };
        UnivariateInterpolator i = new SplineInterpolator();
        UnivariateFunction f = i.interpolate(x, y);
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

// org.apache.commons.math3.analysis.interpolation.SplineInterpolatorTest::testInterpolateLinear
    public void testInterpolateLinear() {
        double x[] = { 0.0, 0.5, 1.0 };
        double y[] = { 0.0, 0.5, 0.0 };
        UnivariateInterpolator i = new SplineInterpolator();
        UnivariateFunction f = i.interpolate(x, y);
        verifyInterpolation(f, x, y);
        verifyConsistency((PolynomialSplineFunction) f, x);

        
        PolynomialFunction polynomials[] = ((PolynomialSplineFunction) f).getPolynomials();
        double target[] = {y[0], 1.5d, 0d, -2d};
        TestUtils.assertEquals(polynomials[0].getCoefficients(), target, coefficientTolerance);
        target = new double[]{y[1], 0d, -3d, 2d};
        TestUtils.assertEquals(polynomials[1].getCoefficients(), target, coefficientTolerance);
    }

// org.apache.commons.math3.analysis.interpolation.SplineInterpolatorTest::testInterpolateSin
    public void testInterpolateSin() {
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
        UnivariateInterpolator i = new SplineInterpolator();
        UnivariateFunction f = i.interpolate(x, y);
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

// org.apache.commons.math3.analysis.interpolation.SplineInterpolatorTest::testIllegalArguments
    public void testIllegalArguments() {
        
        UnivariateInterpolator i = new SplineInterpolator();
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
        } catch (NonMonotonicSequenceException iae) {
            
        }
        
        try {
            double xval[] = { 0.0, 1.0 };
            double yval[] = { 0.0, 1.0 };
            i.interpolate(xval, yval);
            Assert.fail("Failed to detect unsorted arguments.");
        } catch (NumberIsTooSmallException iae) {
            
        }
    }

// org.apache.commons.math3.analysis.interpolation.TricubicSplineInterpolatingFunctionTest::testPreconditions
    public void testPreconditions() {
        double[] xval = new double[] {3, 4, 5, 6.5};
        double[] yval = new double[] {-4, -3, -1, 2.5};
        double[] zval = new double[] {-12, -8, -5.5, -3, 0, 2.5};
        double[][][] fval = new double[xval.length][yval.length][zval.length];

        @SuppressWarnings("unused")
        TrivariateFunction tcf = new TricubicSplineInterpolatingFunction(xval, yval, zval,
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

// org.apache.commons.math3.analysis.interpolation.TricubicSplineInterpolatingFunctionTest::testPlane
    public void testPlane() {
        double[] xval = new double[] {3, 4, 5, 6.5};
        double[] yval = new double[] {-4, -3, -1, 2, 2.5};
        double[] zval = new double[] {-12, -8, -5.5, -3, 0, 2.5};

        
        TrivariateFunction f = new TrivariateFunction() {
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

        TrivariateFunction tcf = new TricubicSplineInterpolatingFunction(xval, yval, zval,
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

// org.apache.commons.math3.analysis.interpolation.TricubicSplineInterpolatingFunctionTest::testWave
    public void testWave() {
        double[] xval = new double[] {3, 4, 5, 6.5};
        double[] yval = new double[] {-4, -3, -1, 2, 2.5};
        double[] zval = new double[] {-12, -8, -5.5, -3, 0, 4};
        
        final double a = 0.2;
        final double omega = 0.5;
        final double kx = 2;
        final double ky = 1;
        
        
        TrivariateFunction f = new TrivariateFunction() {
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
        TrivariateFunction dFdX_f = new TrivariateFunction() {
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
        TrivariateFunction dFdY_f = new TrivariateFunction() {
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
        TrivariateFunction dFdZ_f = new TrivariateFunction() {
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
        TrivariateFunction d2FdXdY_f = new TrivariateFunction() {
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
        TrivariateFunction d2FdXdZ_f = new TrivariateFunction() {
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
        TrivariateFunction d2FdYdZ_f = new TrivariateFunction() {
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
        TrivariateFunction d3FdXdYdZ_f = new TrivariateFunction() {
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

        TrivariateFunction tcf = new TricubicSplineInterpolatingFunction(xval, yval, zval,
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

// org.apache.commons.math3.analysis.interpolation.TricubicSplineInterpolatorTest::testPreconditions
    public void testPreconditions() {
        double[] xval = new double[] {3, 4, 5, 6.5};
        double[] yval = new double[] {-4, -3, -1, 2.5};
        double[] zval = new double[] {-12, -8, -5.5, -3, 0, 2.5};
        double[][][] fval = new double[xval.length][yval.length][zval.length];

        TrivariateGridInterpolator interpolator = new TricubicSplineInterpolator();
        
        @SuppressWarnings("unused")
        TrivariateFunction p = interpolator.interpolate(xval, yval, zval, fval);
        
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

// org.apache.commons.math3.analysis.interpolation.TricubicSplineInterpolatorTest::testPlane
    public void testPlane() {
        TrivariateFunction f = new TrivariateFunction() {
                public double value(double x, double y, double z) {
                    return 2 * x - 3 * y - z + 5;
                }
            };

        TrivariateGridInterpolator interpolator = new TricubicSplineInterpolator();

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

        TrivariateFunction p = interpolator.interpolate(xval, yval, zval, fval);
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

// org.apache.commons.math3.analysis.interpolation.TricubicSplineInterpolatorTest::testWave
    public void testWave() {
        double[] xval = new double[] {3, 4, 5, 6.5};
        double[] yval = new double[] {-4, -3, -1, 2, 2.5};
        double[] zval = new double[] {-12, -8, -5.5, -3, 0, 4};

        final double a = 0.2;
        final double omega = 0.5;
        final double kx = 2;
        final double ky = 1;

        
        TrivariateFunction f = new TrivariateFunction() {
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

        TrivariateGridInterpolator interpolator = new TricubicSplineInterpolator();

        TrivariateFunction p = interpolator.interpolate(xval, yval, zval, fval);
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

// org.apache.commons.math3.analysis.interpolation.UnivariatePeriodicInterpolatorTest::testSine
    public void testSine() {
        final int n = 30;
        final double[] xval = new double[n];
        final double[] yval = new double[n];
        final double period = 12.3;
        final double offset = 45.67;

        double delta = 0;
        for (int i = 0; i < n; i++) {
            delta += rng.nextDouble() * period / n;
            xval[i] = offset + delta;
            yval[i] = FastMath.sin(xval[i]);
        }

        final UnivariateInterpolator inter = new LinearInterpolator();
        final UnivariateFunction f = inter.interpolate(xval, yval);

        final UnivariateInterpolator interP
            = new UnivariatePeriodicInterpolator(new LinearInterpolator(),
                                                     period, 1);
        final UnivariateFunction fP = interP.interpolate(xval, yval);

        
        final double xMin = xval[0];
        final double xMax = xval[n - 1];
        for (int i = 0; i < n; i++) {
            final double x = xMin + (xMax - xMin) * rng.nextDouble();
            final double y = f.value(x);
            final double yP = fP.value(x);

            Assert.assertEquals("x=" + x, y, yP, Math.ulp(1d));
        }

        
        for (int i = 0; i < n; i++) {
            final double xIn = offset + rng.nextDouble() * period;
            final double xOut = xIn + rng.nextInt(123456789) * period;
            final double yIn = fP.value(xIn);
            final double yOut = fP.value(xOut);

            Assert.assertEquals(yIn, yOut, 1e-7);
        }
    }

// org.apache.commons.math3.analysis.interpolation.UnivariatePeriodicInterpolatorTest::testLessThanOnePeriodCoverage
    public void testLessThanOnePeriodCoverage() {
        final int n = 30;
        final double[] xval = new double[n];
        final double[] yval = new double[n];
        final double period = 12.3;
        final double offset = 45.67;

        double delta = period / 2;
        for (int i = 0; i < n; i++) {
            delta += period / (2 * n) * rng.nextDouble();
            xval[i] = offset + delta;
            yval[i] = FastMath.sin(xval[i]);
        }

        final UnivariateInterpolator interP
            = new UnivariatePeriodicInterpolator(new LinearInterpolator(),
                                                     period, 1);
        final UnivariateFunction fP = interP.interpolate(xval, yval);

        
        for (int i = 0; i < n; i++) {
            final double xIn = offset + rng.nextDouble() * period;
            final double xOut = xIn + rng.nextInt(123456789) * period;
            final double yIn = fP.value(xIn);
            final double yOut = fP.value(xOut);

            Assert.assertEquals(yIn, yOut, 1e-7);
        }
    }

// org.apache.commons.math3.analysis.interpolation.UnivariatePeriodicInterpolatorTest::testMoreThanOnePeriodCoverage
    public void testMoreThanOnePeriodCoverage() {
        final int n = 30;
        final double[] xval = new double[n];
        final double[] yval = new double[n];
        final double period = 12.3;
        final double offset = 45.67;

        double delta = period / 2;
        for (int i = 0; i < n; i++) {
            delta += 10 * period / n * rng.nextDouble();
            xval[i] = offset + delta;
            yval[i] = FastMath.sin(xval[i]);
        }

        final UnivariateInterpolator interP
            = new UnivariatePeriodicInterpolator(new LinearInterpolator(),
                                                     period, 1);
        final UnivariateFunction fP = interP.interpolate(xval, yval);

        
        for (int i = 0; i < n; i++) {
            final double xIn = offset + rng.nextDouble() * period;
            final double xOut = xIn + rng.nextInt(123456789) * period;
            final double yIn = fP.value(xIn);
            final double yOut = fP.value(xOut);

            Assert.assertEquals(yIn, yOut, 1e-6);
        }
    }

// org.apache.commons.math3.analysis.interpolation.UnivariatePeriodicInterpolatorTest::testTooFewSamples
    public void testTooFewSamples() {
        final double[] xval = { 2, 3, 7 };
        final double[] yval = { 1, 6, 5 };
        final double period = 10;

        final UnivariateInterpolator interpolator
            = new UnivariatePeriodicInterpolator(new LinearInterpolator(), period);
        interpolator.interpolate(xval, yval);
    }

// org.apache.commons.math3.analysis.interpolation.UnivariatePeriodicInterpolatorTest::testUnsortedSamples
    public void testUnsortedSamples() {
        final double[] xval = { 2, 3, 7, 4, 6 };
        final double[] yval = { 1, 6, 5, -1, -2 };
        final double period = 10;

        final UnivariateInterpolator interpolator
            = new UnivariatePeriodicInterpolator(new LinearInterpolator(), period);
        interpolator.interpolate(xval, yval);
    }

// org.apache.commons.math3.analysis.polynomials.PolynomialFunctionLagrangeFormTest::testLinearFunction
    public void testLinearFunction() {
        PolynomialFunctionLagrangeForm p;
        double c[], z, expected, result, tolerance = 1E-12;

        
        double x[] = { 0.0, 3.0 };
        double y[] = { -4.0, 0.5 };
        p = new PolynomialFunctionLagrangeForm(x, y);

        z = 2.0; expected = -1.0; result = p.value(z);
        Assert.assertEquals(expected, result, tolerance);

        z = 4.5; expected = 2.75; result = p.value(z);
        Assert.assertEquals(expected, result, tolerance);

        z = 6.0; expected = 5.0; result = p.value(z);
        Assert.assertEquals(expected, result, tolerance);

        Assert.assertEquals(1, p.degree());

        c = p.getCoefficients();
        Assert.assertEquals(2, c.length);
        Assert.assertEquals(-4.0, c[0], tolerance);
        Assert.assertEquals(1.5, c[1], tolerance);
    }

// org.apache.commons.math3.analysis.polynomials.PolynomialFunctionLagrangeFormTest::testQuadraticFunction
    public void testQuadraticFunction() {
        PolynomialFunctionLagrangeForm p;
        double c[], z, expected, result, tolerance = 1E-12;

        
        double x[] = { 0.0, -1.0, 0.5 };
        double y[] = { -3.0, -6.0, 0.0 };
        p = new PolynomialFunctionLagrangeForm(x, y);

        z = 1.0; expected = 4.0; result = p.value(z);
        Assert.assertEquals(expected, result, tolerance);

        z = 2.5; expected = 22.0; result = p.value(z);
        Assert.assertEquals(expected, result, tolerance);

        z = -2.0; expected = -5.0; result = p.value(z);
        Assert.assertEquals(expected, result, tolerance);

        Assert.assertEquals(2, p.degree());

        c = p.getCoefficients();
        Assert.assertEquals(3, c.length);
        Assert.assertEquals(-3.0, c[0], tolerance);
        Assert.assertEquals(5.0, c[1], tolerance);
        Assert.assertEquals(2.0, c[2], tolerance);
    }

// org.apache.commons.math3.analysis.polynomials.PolynomialFunctionLagrangeFormTest::testQuinticFunction
    public void testQuinticFunction() {
        PolynomialFunctionLagrangeForm p;
        double c[], z, expected, result, tolerance = 1E-12;

        
        double x[] = { 1.0, -1.0, 2.0, 3.0, -3.0, 0.5 };
        double y[] = { 0.0, 0.0, -24.0, 0.0, -144.0, 2.34375 };
        p = new PolynomialFunctionLagrangeForm(x, y);

        z = 0.0; expected = 0.0; result = p.value(z);
        Assert.assertEquals(expected, result, tolerance);

        z = -2.0; expected = 0.0; result = p.value(z);
        Assert.assertEquals(expected, result, tolerance);

        z = 4.0; expected = 360.0; result = p.value(z);
        Assert.assertEquals(expected, result, tolerance);

        Assert.assertEquals(5, p.degree());

        c = p.getCoefficients();
        Assert.assertEquals(6, c.length);
        Assert.assertEquals(0.0, c[0], tolerance);
        Assert.assertEquals(6.0, c[1], tolerance);
        Assert.assertEquals(1.0, c[2], tolerance);
        Assert.assertEquals(-7.0, c[3], tolerance);
        Assert.assertEquals(-1.0, c[4], tolerance);
        Assert.assertEquals(1.0, c[5], tolerance);
    }

// org.apache.commons.math3.analysis.polynomials.PolynomialFunctionLagrangeFormTest::testParameters
    public void testParameters() {

        try {
            
            double x[] = { 1.0 };
            double y[] = { 2.0 };
            new PolynomialFunctionLagrangeForm(x, y);
            Assert.fail("Expecting MathIllegalArgumentException - bad input array length");
        } catch (MathIllegalArgumentException ex) {
            
        }
        try {
            
            double x[] = { 1.0, 2.0, 3.0, 4.0 };
            double y[] = { 0.0, -4.0, -24.0 };
            new PolynomialFunctionLagrangeForm(x, y);
            Assert.fail("Expecting MathIllegalArgumentException - mismatch input arrays");
        } catch (MathIllegalArgumentException ex) {
            
        }
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

// org.apache.commons.math3.analysis.polynomials.PolynomialFunctionTest::testConstants
    public void testConstants() {
        double[] c = { 2.5 };
        PolynomialFunction f = new PolynomialFunction(c);

        
        Assert.assertEquals(f.value(0), c[0], tolerance);
        Assert.assertEquals(f.value(-1), c[0], tolerance);
        Assert.assertEquals(f.value(-123.5), c[0], tolerance);
        Assert.assertEquals(f.value(3), c[0], tolerance);
        Assert.assertEquals(f.value(456.89), c[0], tolerance);

        Assert.assertEquals(f.degree(), 0);
        Assert.assertEquals(f.derivative().value(0), 0, tolerance);

        Assert.assertEquals(f.polynomialDerivative().derivative().value(0), 0, tolerance);
    }

// org.apache.commons.math3.analysis.polynomials.PolynomialFunctionTest::testLinear
    public void testLinear() {
        double[] c = { -1.5, 3 };
        PolynomialFunction f = new PolynomialFunction(c);

        
        Assert.assertEquals(f.value(0), c[0], tolerance);

        
        Assert.assertEquals(-4.5, f.value(-1), tolerance);
        Assert.assertEquals(-9, f.value(-2.5), tolerance);
        Assert.assertEquals(0, f.value(0.5), tolerance);
        Assert.assertEquals(3, f.value(1.5), tolerance);
        Assert.assertEquals(7.5, f.value(3), tolerance);

        Assert.assertEquals(f.degree(), 1);

        Assert.assertEquals(f.polynomialDerivative().derivative().value(0), 0, tolerance);
    }

// org.apache.commons.math3.analysis.polynomials.PolynomialFunctionTest::testQuadratic
    public void testQuadratic() {
        double[] c = { -2, -3, 2 };
        PolynomialFunction f = new PolynomialFunction(c);

        
        Assert.assertEquals(f.value(0), c[0], tolerance);

        
        Assert.assertEquals(0, f.value(-0.5), tolerance);
        Assert.assertEquals(0, f.value(2), tolerance);
        Assert.assertEquals(-2, f.value(1.5), tolerance);
        Assert.assertEquals(7, f.value(-1.5), tolerance);
        Assert.assertEquals(265.5312, f.value(12.34), tolerance);
    }

// org.apache.commons.math3.analysis.polynomials.PolynomialFunctionTest::testQuintic
    public void testQuintic() {
        double[] c = { 0, 0, 15, -13, -3, 1 };
        PolynomialFunction f = new PolynomialFunction(c);

        
        Assert.assertEquals(f.value(0), c[0], tolerance);

        
        Assert.assertEquals(0, f.value(5), tolerance);
        Assert.assertEquals(0, f.value(1), tolerance);
        Assert.assertEquals(0, f.value(-3), tolerance);
        Assert.assertEquals(54.84375, f.value(-1.5), tolerance);
        Assert.assertEquals(-8.06637, f.value(1.3), tolerance);

        Assert.assertEquals(f.degree(), 5);
    }

// org.apache.commons.math3.analysis.polynomials.PolynomialFunctionTest::testfirstDerivativeComparison
    public void testfirstDerivativeComparison() {
        double[] f_coeff = { 3, 6, -2, 1 };
        double[] g_coeff = { 6, -4, 3 };
        double[] h_coeff = { -4, 6 };

        PolynomialFunction f = new PolynomialFunction(f_coeff);
        PolynomialFunction g = new PolynomialFunction(g_coeff);
        PolynomialFunction h = new PolynomialFunction(h_coeff);

        
        Assert.assertEquals(f.derivative().value(0), g.value(0), tolerance);
        Assert.assertEquals(f.derivative().value(1), g.value(1), tolerance);
        Assert.assertEquals(f.derivative().value(100), g.value(100), tolerance);
        Assert.assertEquals(f.derivative().value(4.1), g.value(4.1), tolerance);
        Assert.assertEquals(f.derivative().value(-3.25), g.value(-3.25), tolerance);

        
        Assert.assertEquals(g.derivative().value(FastMath.PI), h.value(FastMath.PI), tolerance);
        Assert.assertEquals(g.derivative().value(FastMath.E),  h.value(FastMath.E),  tolerance);
    }

// org.apache.commons.math3.analysis.polynomials.PolynomialFunctionTest::testString
    public void testString() {
        PolynomialFunction p = new PolynomialFunction(new double[] { -5, 3, 1 });
        checkPolynomial(p, "-5 + 3 x + x^2");
        checkPolynomial(new PolynomialFunction(new double[] { 0, -2, 3 }),
                        "-2 x + 3 x^2");
        checkPolynomial(new PolynomialFunction(new double[] { 1, -2, 3 }),
                      "1 - 2 x + 3 x^2");
        checkPolynomial(new PolynomialFunction(new double[] { 0,  2, 3 }),
                       "2 x + 3 x^2");
        checkPolynomial(new PolynomialFunction(new double[] { 1,  2, 3 }),
                     "1 + 2 x + 3 x^2");
        checkPolynomial(new PolynomialFunction(new double[] { 1,  0, 3 }),
                     "1 + 3 x^2");
        checkPolynomial(new PolynomialFunction(new double[] { 0 }),
                     "0");
    }

// org.apache.commons.math3.analysis.polynomials.PolynomialFunctionTest::testAddition
    public void testAddition() {
        PolynomialFunction p1 = new PolynomialFunction(new double[] { -2, 1 });
        PolynomialFunction p2 = new PolynomialFunction(new double[] { 2, -1, 0 });
        checkNullPolynomial(p1.add(p2));

        p2 = p1.add(p1);
        checkPolynomial(p2, "-4 + 2 x");

        p1 = new PolynomialFunction(new double[] { 1, -4, 2 });
        p2 = new PolynomialFunction(new double[] { -1, 3, -2 });
        p1 = p1.add(p2);
        Assert.assertEquals(1, p1.degree());
        checkPolynomial(p1, "-x");
    }

// org.apache.commons.math3.analysis.polynomials.PolynomialFunctionTest::testSubtraction
    public void testSubtraction() {
        PolynomialFunction p1 = new PolynomialFunction(new double[] { -2, 1 });
        checkNullPolynomial(p1.subtract(p1));

        PolynomialFunction p2 = new PolynomialFunction(new double[] { -2, 6 });
        p2 = p2.subtract(p1);
        checkPolynomial(p2, "5 x");

        p1 = new PolynomialFunction(new double[] { 1, -4, 2 });
        p2 = new PolynomialFunction(new double[] { -1, 3, 2 });
        p1 = p1.subtract(p2);
        Assert.assertEquals(1, p1.degree());
        checkPolynomial(p1, "2 - 7 x");
    }

// org.apache.commons.math3.analysis.polynomials.PolynomialFunctionTest::testMultiplication
    public void testMultiplication() {
        PolynomialFunction p1 = new PolynomialFunction(new double[] { -3, 2 });
        PolynomialFunction p2 = new PolynomialFunction(new double[] { 3, 2, 1 });
        checkPolynomial(p1.multiply(p2), "-9 + x^2 + 2 x^3");

        p1 = new PolynomialFunction(new double[] { 0, 1 });
        p2 = p1;
        for (int i = 2; i < 10; ++i) {
            p2 = p2.multiply(p1);
            checkPolynomial(p2, "x^" + i);
        }
    }

// org.apache.commons.math3.analysis.polynomials.PolynomialFunctionTest::testSerial
    public void testSerial() {
        PolynomialFunction p2 = new PolynomialFunction(new double[] { 3, 2, 1 });
        Assert.assertEquals(p2, TestUtils.serializeAndRecover(p2));
    }

// org.apache.commons.math3.analysis.polynomials.PolynomialFunctionTest::testMath341
    public void testMath341() {
        double[] f_coeff = { 3, 6, -2, 1 };
        double[] g_coeff = { 6, -4, 3 };
        double[] h_coeff = { -4, 6 };

        PolynomialFunction f = new PolynomialFunction(f_coeff);
        PolynomialFunction g = new PolynomialFunction(g_coeff);
        PolynomialFunction h = new PolynomialFunction(h_coeff);

        
        Assert.assertEquals(f.derivative().value(0), g.value(0), tolerance);
        Assert.assertEquals(f.derivative().value(1), g.value(1), tolerance);
        Assert.assertEquals(f.derivative().value(100), g.value(100), tolerance);
        Assert.assertEquals(f.derivative().value(4.1), g.value(4.1), tolerance);
        Assert.assertEquals(f.derivative().value(-3.25), g.value(-3.25), tolerance);

        
        Assert.assertEquals(g.derivative().value(FastMath.PI), h.value(FastMath.PI), tolerance);
        Assert.assertEquals(g.derivative().value(FastMath.E),  h.value(FastMath.E),  tolerance);
    }

// org.apache.commons.math3.analysis.polynomials.PolynomialsUtilsTest::testFirstChebyshevPolynomials
    public void testFirstChebyshevPolynomials() {
        checkPolynomial(PolynomialsUtils.createChebyshevPolynomial(3), "-3 x + 4 x^3");
        checkPolynomial(PolynomialsUtils.createChebyshevPolynomial(2), "-1 + 2 x^2");
        checkPolynomial(PolynomialsUtils.createChebyshevPolynomial(1), "x");
        checkPolynomial(PolynomialsUtils.createChebyshevPolynomial(0), "1");

        checkPolynomial(PolynomialsUtils.createChebyshevPolynomial(7), "-7 x + 56 x^3 - 112 x^5 + 64 x^7");
        checkPolynomial(PolynomialsUtils.createChebyshevPolynomial(6), "-1 + 18 x^2 - 48 x^4 + 32 x^6");
        checkPolynomial(PolynomialsUtils.createChebyshevPolynomial(5), "5 x - 20 x^3 + 16 x^5");
        checkPolynomial(PolynomialsUtils.createChebyshevPolynomial(4), "1 - 8 x^2 + 8 x^4");

    }

// org.apache.commons.math3.analysis.polynomials.PolynomialsUtilsTest::testChebyshevBounds
    public void testChebyshevBounds() {
        for (int k = 0; k < 12; ++k) {
            PolynomialFunction Tk = PolynomialsUtils.createChebyshevPolynomial(k);
            for (double x = -1; x <= 1; x += 0.02) {
                Assert.assertTrue(k + " " + Tk.value(x), FastMath.abs(Tk.value(x)) < (1 + 1e-12));
            }
        }
    }

// org.apache.commons.math3.analysis.polynomials.PolynomialsUtilsTest::testChebyshevDifferentials
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

// org.apache.commons.math3.analysis.polynomials.PolynomialsUtilsTest::testChebyshevOrthogonality
    public void testChebyshevOrthogonality() {
        UnivariateFunction weight = new UnivariateFunction() {
            public double value(double x) {
                return 1 / FastMath.sqrt(1 - x * x);
            }
        };
        for (int i = 0; i < 10; ++i) {
            PolynomialFunction pi = PolynomialsUtils.createChebyshevPolynomial(i);
            for (int j = 0; j <= i; ++j) {
                PolynomialFunction pj = PolynomialsUtils.createChebyshevPolynomial(j);
                checkOrthogonality(pi, pj, weight, -0.9999, 0.9999, 1.5, 0.03);
            }
        }
    }

// org.apache.commons.math3.analysis.polynomials.PolynomialsUtilsTest::testFirstHermitePolynomials
    public void testFirstHermitePolynomials() {
        checkPolynomial(PolynomialsUtils.createHermitePolynomial(3), "-12 x + 8 x^3");
        checkPolynomial(PolynomialsUtils.createHermitePolynomial(2), "-2 + 4 x^2");
        checkPolynomial(PolynomialsUtils.createHermitePolynomial(1), "2 x");
        checkPolynomial(PolynomialsUtils.createHermitePolynomial(0), "1");

        checkPolynomial(PolynomialsUtils.createHermitePolynomial(7), "-1680 x + 3360 x^3 - 1344 x^5 + 128 x^7");
        checkPolynomial(PolynomialsUtils.createHermitePolynomial(6), "-120 + 720 x^2 - 480 x^4 + 64 x^6");
        checkPolynomial(PolynomialsUtils.createHermitePolynomial(5), "120 x - 160 x^3 + 32 x^5");
        checkPolynomial(PolynomialsUtils.createHermitePolynomial(4), "12 - 48 x^2 + 16 x^4");

    }

// org.apache.commons.math3.analysis.polynomials.PolynomialsUtilsTest::testHermiteDifferentials
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

// org.apache.commons.math3.analysis.polynomials.PolynomialsUtilsTest::testHermiteOrthogonality
    public void testHermiteOrthogonality() {
        UnivariateFunction weight = new UnivariateFunction() {
            public double value(double x) {
                return FastMath.exp(-x * x);
            }
        };
        for (int i = 0; i < 10; ++i) {
            PolynomialFunction pi = PolynomialsUtils.createHermitePolynomial(i);
            for (int j = 0; j <= i; ++j) {
                PolynomialFunction pj = PolynomialsUtils.createHermitePolynomial(j);
                checkOrthogonality(pi, pj, weight, -50, 50, 1.5, 1.0e-8);
            }
        }
    }

// org.apache.commons.math3.analysis.polynomials.PolynomialsUtilsTest::testFirstLaguerrePolynomials
    public void testFirstLaguerrePolynomials() {
        checkPolynomial(PolynomialsUtils.createLaguerrePolynomial(3), 6l, "6 - 18 x + 9 x^2 - x^3");
        checkPolynomial(PolynomialsUtils.createLaguerrePolynomial(2), 2l, "2 - 4 x + x^2");
        checkPolynomial(PolynomialsUtils.createLaguerrePolynomial(1), 1l, "1 - x");
        checkPolynomial(PolynomialsUtils.createLaguerrePolynomial(0), 1l, "1");

        checkPolynomial(PolynomialsUtils.createLaguerrePolynomial(7), 5040l,
                "5040 - 35280 x + 52920 x^2 - 29400 x^3"
                + " + 7350 x^4 - 882 x^5 + 49 x^6 - x^7");
        checkPolynomial(PolynomialsUtils.createLaguerrePolynomial(6),  720l,
                "720 - 4320 x + 5400 x^2 - 2400 x^3 + 450 x^4"
                + " - 36 x^5 + x^6");
        checkPolynomial(PolynomialsUtils.createLaguerrePolynomial(5),  120l,
        "120 - 600 x + 600 x^2 - 200 x^3 + 25 x^4 - x^5");
        checkPolynomial(PolynomialsUtils.createLaguerrePolynomial(4),   24l,
        "24 - 96 x + 72 x^2 - 16 x^3 + x^4");

    }

// org.apache.commons.math3.analysis.polynomials.PolynomialsUtilsTest::testLaguerreDifferentials
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

// org.apache.commons.math3.analysis.polynomials.PolynomialsUtilsTest::testLaguerreOrthogonality
    public void testLaguerreOrthogonality() {
        UnivariateFunction weight = new UnivariateFunction() {
            public double value(double x) {
                return FastMath.exp(-x);
            }
        };
        for (int i = 0; i < 10; ++i) {
            PolynomialFunction pi = PolynomialsUtils.createLaguerrePolynomial(i);
            for (int j = 0; j <= i; ++j) {
                PolynomialFunction pj = PolynomialsUtils.createLaguerrePolynomial(j);
                checkOrthogonality(pi, pj, weight, 0.0, 100.0, 0.99999, 1.0e-13);
            }
        }
    }

// org.apache.commons.math3.analysis.polynomials.PolynomialsUtilsTest::testFirstLegendrePolynomials
    public void testFirstLegendrePolynomials() {
        checkPolynomial(PolynomialsUtils.createLegendrePolynomial(3),  2l, "-3 x + 5 x^3");
        checkPolynomial(PolynomialsUtils.createLegendrePolynomial(2),  2l, "-1 + 3 x^2");
        checkPolynomial(PolynomialsUtils.createLegendrePolynomial(1),  1l, "x");
        checkPolynomial(PolynomialsUtils.createLegendrePolynomial(0),  1l, "1");

        checkPolynomial(PolynomialsUtils.createLegendrePolynomial(7), 16l, "-35 x + 315 x^3 - 693 x^5 + 429 x^7");
        checkPolynomial(PolynomialsUtils.createLegendrePolynomial(6), 16l, "-5 + 105 x^2 - 315 x^4 + 231 x^6");
        checkPolynomial(PolynomialsUtils.createLegendrePolynomial(5),  8l, "15 x - 70 x^3 + 63 x^5");
        checkPolynomial(PolynomialsUtils.createLegendrePolynomial(4),  8l, "3 - 30 x^2 + 35 x^4");

    }

// org.apache.commons.math3.analysis.polynomials.PolynomialsUtilsTest::testLegendreDifferentials
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

// org.apache.commons.math3.analysis.polynomials.PolynomialsUtilsTest::testLegendreOrthogonality
    public void testLegendreOrthogonality() {
        UnivariateFunction weight = new UnivariateFunction() {
            public double value(double x) {
                return 1;
            }
        };
        for (int i = 0; i < 10; ++i) {
            PolynomialFunction pi = PolynomialsUtils.createLegendrePolynomial(i);
            for (int j = 0; j <= i; ++j) {
                PolynomialFunction pj = PolynomialsUtils.createLegendrePolynomial(j);
                checkOrthogonality(pi, pj, weight, -1, 1, 0.1, 1.0e-13);
            }
        }
    }

// org.apache.commons.math3.analysis.polynomials.PolynomialsUtilsTest::testHighDegreeLegendre
    public void testHighDegreeLegendre() {
        PolynomialsUtils.createLegendrePolynomial(40);
        double[] l40 = PolynomialsUtils.createLegendrePolynomial(40).getCoefficients();
        double denominator = 274877906944d;
        double[] numerators = new double[] {
                          +34461632205d,            -28258538408100d,          +3847870979902950d,        -207785032914759300d,
                  +5929294332103310025d,     -103301483474866556880d,    +1197358103913226000200d,    -9763073770369381232400d,
              +58171647881784229843050d,  -260061484647976556945400d,  +888315281771246239250340d, -2345767627188139419665400d,
            +4819022625419112503443050d, -7710436200670580005508880d, +9566652323054238154983240d, -9104813935044723209570256d,
            +6516550296251767619752905d, -3391858621221953912598660d, +1211378079007840683070950d,  -265365894974690562152100d,
              +26876802183334044115405d
        };
        for (int i = 0; i < l40.length; ++i) {
            if (i % 2 == 0) {
                double ci = numerators[i / 2] / denominator;
                Assert.assertEquals(ci, l40[i], FastMath.abs(ci) * 1e-15);
            } else {
                Assert.assertEquals(0, l40[i], 0);
            }
        }
    }

// org.apache.commons.math3.analysis.polynomials.PolynomialsUtilsTest::testJacobiLegendre
    public void testJacobiLegendre() {
        for (int i = 0; i < 10; ++i) {
            PolynomialFunction legendre = PolynomialsUtils.createLegendrePolynomial(i);
            PolynomialFunction jacobi   = PolynomialsUtils.createJacobiPolynomial(i, 0, 0);
            checkNullPolynomial(legendre.subtract(jacobi));
        }
    }
