// buggy function
    public Complex divide(Complex divisor)
        throws NullArgumentException {
        MathUtils.checkNotNull(divisor);
        if (isNaN || divisor.isNaN) {
            return NaN;
        }

        if (divisor.isZero) {
            // return isZero ? NaN : INF; // See MATH-657
            return isZero ? NaN : INF;
        }

        if (divisor.isInfinite() && !isInfinite()) {
            return ZERO;
        }

        final double c = divisor.getReal();
        final double d = divisor.getImaginary();

        if (FastMath.abs(c) < FastMath.abs(d)) {
            double q = c / d;
            double denominator = c * q + d;
            return createComplex((real * q + imaginary) / denominator,
                (imaginary * q - real) / denominator);
        } else {
            double q = d / c;
            double denominator = d * q + c;
            return createComplex((imaginary * q + real) / denominator,
                (imaginary - real * q) / denominator);
        }
    }

    public Complex divide(double divisor) {
        if (isNaN || Double.isNaN(divisor)) {
            return NaN;
        }
        if (divisor == 0d) {
            // return isZero ? NaN : INF; // See MATH-657
            return isZero ? NaN : INF;
        }
        if (Double.isInfinite(divisor)) {
            return !isInfinite() ? ZERO : NaN;
        }
        return createComplex(real / divisor,
                             imaginary  / divisor);
    }

// trigger testcase
// org/apache/commons/math/complex/ComplexTest.java::testAtanI
@Test
    public void testAtanI() {
        Assert.assertTrue(Complex.I.atan().isNaN());
    }

// org/apache/commons/math/complex/ComplexTest.java::testDivideZero
@Test
    public void testDivideZero() {
        Complex x = new Complex(3.0, 4.0);
        Complex z = x.divide(Complex.ZERO);
        // Assert.assertEquals(z, Complex.INF); // See MATH-657
        Assert.assertEquals(z, Complex.NaN);
    }
