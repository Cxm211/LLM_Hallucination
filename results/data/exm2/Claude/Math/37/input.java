    public Complex tan() {
        if (isNaN) {
            return NaN;
        }

        double real2 = 2.0 * real;
        double imaginary2 = 2.0 * imaginary;
        double d = FastMath.cos(real2) + FastMath.cosh(imaginary2);

        return createComplex(FastMath.sin(real2) / d,
                             FastMath.sinh(imaginary2) / d);
    }

    public Complex tanh() {
        if (isNaN) {
            return NaN;
        }
        double real2 = 2.0 * real;
        double imaginary2 = 2.0 * imaginary;
        double d = FastMath.cosh(real2) + FastMath.cos(imaginary2);

        return createComplex(FastMath.sinh(real2) / d,
                             FastMath.sin(imaginary2) / d);
    }

// trigger testcase
@Test
    public void testTan() {
        Complex z = new Complex(3, 4);
        Complex expected = new Complex(-0.000187346, 0.999356);
        TestUtils.assertEquals(expected, z.tan(), 1.0e-5);
        /* Check that no overflow occurs (MATH-722) */
        Complex actual = new Complex(3.0, 1E10).tan();
        expected = new Complex(0, 1);
        TestUtils.assertEquals(expected, actual, 1.0e-5);
        actual = new Complex(3.0, -1E10).tan();
        expected = new Complex(0, -1);
        TestUtils.assertEquals(expected, actual, 1.0e-5);
    }

@Test
    public void testTanInf() {
        TestUtils.assertSame(Complex.valueOf(0.0, 1.0), oneInf.tan());
        TestUtils.assertSame(Complex.valueOf(0.0, -1.0), oneNegInf.tan());
        TestUtils.assertSame(Complex.NaN, infOne.tan());
        TestUtils.assertSame(Complex.NaN, negInfOne.tan());
        TestUtils.assertSame(Complex.NaN, infInf.tan());
        TestUtils.assertSame(Complex.NaN, infNegInf.tan());
        TestUtils.assertSame(Complex.NaN, negInfInf.tan());
        TestUtils.assertSame(Complex.NaN, negInfNegInf.tan());
    }

@Test
    public void testTanh() {
        Complex z = new Complex(3, 4);
        Complex expected = new Complex(1.00071, 0.00490826);
        TestUtils.assertEquals(expected, z.tanh(), 1.0e-5);
        /* Check that no overflow occurs (MATH-722) */
        Complex actual = new Complex(1E10, 3.0).tanh();
        expected = new Complex(1, 0);
        TestUtils.assertEquals(expected, actual, 1.0e-5);
        actual = new Complex(-1E10, 3.0).tanh();
        expected = new Complex(-1, 0);
        TestUtils.assertEquals(expected, actual, 1.0e-5);
    }

@Test
    public void testTanhInf() {
        TestUtils.assertSame(Complex.NaN, oneInf.tanh());
        TestUtils.assertSame(Complex.NaN, oneNegInf.tanh());
        TestUtils.assertSame(Complex.valueOf(1.0, 0.0), infOne.tanh());
        TestUtils.assertSame(Complex.valueOf(-1.0, 0.0), negInfOne.tanh());
        TestUtils.assertSame(Complex.NaN, infInf.tanh());
        TestUtils.assertSame(Complex.NaN, infNegInf.tanh());
        TestUtils.assertSame(Complex.NaN, negInfInf.tanh());
        TestUtils.assertSame(Complex.NaN, negInfNegInf.tanh());
    }
