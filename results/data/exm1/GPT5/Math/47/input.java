// buggy code
    public Complex(double real, double imaginary) {
        this.real = real;
        this.imaginary = imaginary;

        isNaN = Double.isNaN(real) || Double.isNaN(imaginary);
        isInfinite = !isNaN &&
            (Double.isInfinite(real) || Double.isInfinite(imaginary));
    }

    public Complex divide(Complex divisor)
        throws NullArgumentException {
        MathUtils.checkNotNull(divisor);
        if (isNaN || divisor.isNaN) {
            return NaN;
        }

        if (divisor.getReal() == 0.0 && divisor.getImaginary() == 0.0) {
            return NaN;
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
            return NaN;
        }
        if (Double.isInfinite(divisor)) {
            return !isInfinite() ? ZERO : NaN;
        }
        return createComplex(real / divisor,
                             imaginary  / divisor);
    }

// relevant test
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

// org.apache.commons.math.complex.ComplexFieldTest::testZero
    public void testZero() {
        Assert.assertEquals(Complex.ZERO, ComplexField.getInstance().getZero());
    }

// org.apache.commons.math.complex.ComplexFieldTest::testOne
    public void testOne() {
        Assert.assertEquals(Complex.ONE, ComplexField.getInstance().getOne());
    }

// org.apache.commons.math.complex.ComplexFieldTest::testSerial
    public void testSerial() {
        
        ComplexField field = ComplexField.getInstance();
        Assert.assertTrue(field == TestUtils.serializeAndRecover(field));
    }

// org.apache.commons.math.complex.ComplexTest::testConstructor
    public void testConstructor() {
        Complex z = new Complex(3.0, 4.0);
        Assert.assertEquals(3.0, z.getReal(), 1.0e-5);
        Assert.assertEquals(4.0, z.getImaginary(), 1.0e-5);
    }

// org.apache.commons.math.complex.ComplexTest::testConstructorNaN
    public void testConstructorNaN() {
        Complex z = new Complex(3.0, Double.NaN);
        Assert.assertTrue(z.isNaN());

        z = new Complex(nan, 4.0);
        Assert.assertTrue(z.isNaN());

        z = new Complex(3.0, 4.0);
        Assert.assertFalse(z.isNaN());
    }

// org.apache.commons.math.complex.ComplexTest::testAbs
    public void testAbs() {
        Complex z = new Complex(3.0, 4.0);
        Assert.assertEquals(5.0, z.abs(), 1.0e-5);
    }

// org.apache.commons.math.complex.ComplexTest::testAbsNaN
    public void testAbsNaN() {
        Assert.assertTrue(Double.isNaN(Complex.NaN.abs()));
        Complex z = new Complex(inf, nan);
        Assert.assertTrue(Double.isNaN(z.abs()));
    }

// org.apache.commons.math.complex.ComplexTest::testAbsInfinite
    public void testAbsInfinite() {
        Complex z = new Complex(inf, 0);
        Assert.assertEquals(inf, z.abs(), 0);
        z = new Complex(0, neginf);
        Assert.assertEquals(inf, z.abs(), 0);
        z = new Complex(inf, neginf);
        Assert.assertEquals(inf, z.abs(), 0);
    }

// org.apache.commons.math.complex.ComplexTest::testAdd
    public void testAdd() {
        Complex x = new Complex(3.0, 4.0);
        Complex y = new Complex(5.0, 6.0);
        Complex z = x.add(y);
        Assert.assertEquals(8.0, z.getReal(), 1.0e-5);
        Assert.assertEquals(10.0, z.getImaginary(), 1.0e-5);
    }

// org.apache.commons.math.complex.ComplexTest::testAddNaN
    public void testAddNaN() {
        Complex x = new Complex(3.0, 4.0);
        Complex z = x.add(Complex.NaN);
        Assert.assertSame(Complex.NaN, z);
        z = new Complex(1, nan);
        Complex w = x.add(z);
        Assert.assertSame(Complex.NaN, w);
    }

// org.apache.commons.math.complex.ComplexTest::testAddInf
    public void testAddInf() {
        Complex x = new Complex(1, 1);
        Complex z = new Complex(inf, 0);
        Complex w = x.add(z);
        Assert.assertEquals(w.getImaginary(), 1, 0);
        Assert.assertEquals(inf, w.getReal(), 0);

        x = new Complex(neginf, 0);
        Assert.assertTrue(Double.isNaN(x.add(z).getReal()));
    }

// org.apache.commons.math.complex.ComplexTest::testScalarAdd
    public void testScalarAdd() {
        Complex x = new Complex(3.0, 4.0);
        double yDouble = 2.0;
        Complex yComplex = new Complex(yDouble);
        Assert.assertEquals(x.add(yComplex), x.add(yDouble));
    }

// org.apache.commons.math.complex.ComplexTest::testScalarAddNaN
    public void testScalarAddNaN() {
        Complex x = new Complex(3.0, 4.0);
        double yDouble = Double.NaN;
        Complex yComplex = new Complex(yDouble);
        Assert.assertEquals(x.add(yComplex), x.add(yDouble));
    }

// org.apache.commons.math.complex.ComplexTest::testScalarAddInf
    public void testScalarAddInf() {
        Complex x = new Complex(1, 1);
        double yDouble = Double.POSITIVE_INFINITY;
        
        Complex yComplex = new Complex(yDouble);
        Assert.assertEquals(x.add(yComplex), x.add(yDouble));

        x = new Complex(neginf, 0);
        Assert.assertEquals(x.add(yComplex), x.add(yDouble));
    }

// org.apache.commons.math.complex.ComplexTest::testConjugate
    public void testConjugate() {
        Complex x = new Complex(3.0, 4.0);
        Complex z = x.conjugate();
        Assert.assertEquals(3.0, z.getReal(), 1.0e-5);
        Assert.assertEquals(-4.0, z.getImaginary(), 1.0e-5);
    }

// org.apache.commons.math.complex.ComplexTest::testConjugateNaN
    public void testConjugateNaN() {
        Complex z = Complex.NaN.conjugate();
        Assert.assertTrue(z.isNaN());
    }

// org.apache.commons.math.complex.ComplexTest::testConjugateInfiinite
    public void testConjugateInfiinite() {
        Complex z = new Complex(0, inf);
        Assert.assertEquals(neginf, z.conjugate().getImaginary(), 0);
        z = new Complex(0, neginf);
        Assert.assertEquals(inf, z.conjugate().getImaginary(), 0);
    }

// org.apache.commons.math.complex.ComplexTest::testDivide
    public void testDivide() {
        Complex x = new Complex(3.0, 4.0);
        Complex y = new Complex(5.0, 6.0);
        Complex z = x.divide(y);
        Assert.assertEquals(39.0 / 61.0, z.getReal(), 1.0e-5);
        Assert.assertEquals(2.0 / 61.0, z.getImaginary(), 1.0e-5);
    }

// org.apache.commons.math.complex.ComplexTest::testDivideReal
    public void testDivideReal() {
        Complex x = new Complex(2d, 3d);
        Complex y = new Complex(2d, 0d);
        Assert.assertEquals(new Complex(1d, 1.5), x.divide(y));

    }

// org.apache.commons.math.complex.ComplexTest::testDivideImaginary
    public void testDivideImaginary() {
        Complex x = new Complex(2d, 3d);
        Complex y = new Complex(0d, 2d);
        Assert.assertEquals(new Complex(1.5d, -1d), x.divide(y));
    }

// org.apache.commons.math.complex.ComplexTest::testDivideInf
    public void testDivideInf() {
        Complex x = new Complex(3, 4);
        Complex w = new Complex(neginf, inf);
        Assert.assertTrue(x.divide(w).equals(Complex.ZERO));

        Complex z = w.divide(x);
        Assert.assertTrue(Double.isNaN(z.getReal()));
        Assert.assertEquals(inf, z.getImaginary(), 0);

        w = new Complex(inf, inf);
        z = w.divide(x);
        Assert.assertTrue(Double.isNaN(z.getImaginary()));
        Assert.assertEquals(inf, z.getReal(), 0);

        w = new Complex(1, inf);
        z = w.divide(w);
        Assert.assertTrue(Double.isNaN(z.getReal()));
        Assert.assertTrue(Double.isNaN(z.getImaginary()));
    }

// org.apache.commons.math.complex.ComplexTest::testDivideZero
    public void testDivideZero() {
        Complex x = new Complex(3.0, 4.0);
        Complex z = x.divide(Complex.ZERO);
        Assert.assertEquals(z, Complex.INF);
    }

// org.apache.commons.math.complex.ComplexTest::testDivideZeroZero
    public void testDivideZeroZero() {
        Complex x = new Complex(0.0, 0.0);
        Complex z = x.divide(Complex.ZERO);
        Assert.assertEquals(z, Complex.NaN);
    }

// org.apache.commons.math.complex.ComplexTest::testDivideNaN
    public void testDivideNaN() {
        Complex x = new Complex(3.0, 4.0);
        Complex z = x.divide(Complex.NaN);
        Assert.assertTrue(z.isNaN());
    }

// org.apache.commons.math.complex.ComplexTest::testDivideNaNInf
    public void testDivideNaNInf() {
       Complex z = oneInf.divide(Complex.ONE);
       Assert.assertTrue(Double.isNaN(z.getReal()));
       Assert.assertEquals(inf, z.getImaginary(), 0);

       z = negInfNegInf.divide(oneNaN);
       Assert.assertTrue(Double.isNaN(z.getReal()));
       Assert.assertTrue(Double.isNaN(z.getImaginary()));

       z = negInfInf.divide(Complex.ONE);
       Assert.assertTrue(Double.isNaN(z.getReal()));
       Assert.assertTrue(Double.isNaN(z.getImaginary()));
    }

// org.apache.commons.math.complex.ComplexTest::testScalarDivide
    public void testScalarDivide() {
        Complex x = new Complex(3.0, 4.0);
        double yDouble = 2.0;
        Complex yComplex = new Complex(yDouble);
        Assert.assertEquals(x.divide(yComplex), x.divide(yDouble));
    }

// org.apache.commons.math.complex.ComplexTest::testScalarDivideNaN
    public void testScalarDivideNaN() {
        Complex x = new Complex(3.0, 4.0);
        double yDouble = Double.NaN;
        Complex yComplex = new Complex(yDouble);
        Assert.assertEquals(x.divide(yComplex), x.divide(yDouble));
    }

// org.apache.commons.math.complex.ComplexTest::testScalarDivideInf
    public void testScalarDivideInf() {
        Complex x = new Complex(1,1);
        double yDouble = Double.POSITIVE_INFINITY;
        Complex yComplex = new Complex(yDouble);
        TestUtils.assertEquals(x.divide(yComplex), x.divide(yDouble), 0);

        yDouble = Double.NEGATIVE_INFINITY;
        yComplex = new Complex(yDouble);
        TestUtils.assertEquals(x.divide(yComplex), x.divide(yDouble), 0);
        
        x = new Complex(1, Double.NEGATIVE_INFINITY);
        TestUtils.assertEquals(x.divide(yComplex), x.divide(yDouble), 0);
    }

// org.apache.commons.math.complex.ComplexTest::testScalarDivideZero
    public void testScalarDivideZero() {
        Complex x = new Complex(1,1);
        TestUtils.assertEquals(x.divide(Complex.ZERO), x.divide(0), 0);
    }

// org.apache.commons.math.complex.ComplexTest::testMultiply
    public void testMultiply() {
        Complex x = new Complex(3.0, 4.0);
        Complex y = new Complex(5.0, 6.0);
        Complex z = x.multiply(y);
        Assert.assertEquals(-9.0, z.getReal(), 1.0e-5);
        Assert.assertEquals(38.0, z.getImaginary(), 1.0e-5);
    }

// org.apache.commons.math.complex.ComplexTest::testMultiplyNaN
    public void testMultiplyNaN() {
        Complex x = new Complex(3.0, 4.0);
        Complex z = x.multiply(Complex.NaN);
        Assert.assertSame(Complex.NaN, z);
    }

// org.apache.commons.math.complex.ComplexTest::testMultiplyNaNInf
    public void testMultiplyNaNInf() {
        Complex z = new Complex(1,1);
        Complex w = z.multiply(infOne);
        Assert.assertEquals(w.getReal(), inf, 0);
        Assert.assertEquals(w.getImaginary(), inf, 0);

        
        Assert.assertTrue(new Complex( 1,0).multiply(infInf).equals(Complex.INF));
        Assert.assertTrue(new Complex(-1,0).multiply(infInf).equals(Complex.INF));
        Assert.assertTrue(new Complex( 1,0).multiply(negInfZero).equals(Complex.INF));

        w = oneInf.multiply(oneNegInf);
        Assert.assertEquals(w.getReal(), inf, 0);
        Assert.assertEquals(w.getImaginary(), inf, 0);

        w = negInfNegInf.multiply(oneNaN);
        Assert.assertTrue(Double.isNaN(w.getReal()));
        Assert.assertTrue(Double.isNaN(w.getImaginary()));
        
        z = new Complex(1, neginf);
        Assert.assertSame(Complex.INF, z.multiply(z));
    }

// org.apache.commons.math.complex.ComplexTest::testScalarMultiply
    public void testScalarMultiply() {
        Complex x = new Complex(3.0, 4.0);
        double yDouble = 2.0;
        Complex yComplex = new Complex(yDouble);
        Assert.assertEquals(x.multiply(yComplex), x.multiply(yDouble));
    }

// org.apache.commons.math.complex.ComplexTest::testScalarMultiplyNaN
    public void testScalarMultiplyNaN() {
        Complex x = new Complex(3.0, 4.0);
        double yDouble = Double.NaN;
        Complex yComplex = new Complex(yDouble);
        Assert.assertEquals(x.multiply(yComplex), x.multiply(yDouble));
    }

// org.apache.commons.math.complex.ComplexTest::testScalarMultiplyInf
    public void testScalarMultiplyInf() {
        Complex x = new Complex(1, 1);
        double yDouble = Double.POSITIVE_INFINITY;
        Complex yComplex = new Complex(yDouble);
        Assert.assertEquals(x.multiply(yComplex), x.multiply(yDouble));
        
        yDouble = Double.NEGATIVE_INFINITY;
        yComplex = new Complex(yDouble);
        Assert.assertEquals(x.multiply(yComplex), x.multiply(yDouble));
    }

// org.apache.commons.math.complex.ComplexTest::testNegate
    public void testNegate() {
        Complex x = new Complex(3.0, 4.0);
        Complex z = x.negate();
        Assert.assertEquals(-3.0, z.getReal(), 1.0e-5);
        Assert.assertEquals(-4.0, z.getImaginary(), 1.0e-5);
    }

// org.apache.commons.math.complex.ComplexTest::testNegateNaN
    public void testNegateNaN() {
        Complex z = Complex.NaN.negate();
        Assert.assertTrue(z.isNaN());
    }

// org.apache.commons.math.complex.ComplexTest::testSubtract
    public void testSubtract() {
        Complex x = new Complex(3.0, 4.0);
        Complex y = new Complex(5.0, 6.0);
        Complex z = x.subtract(y);
        Assert.assertEquals(-2.0, z.getReal(), 1.0e-5);
        Assert.assertEquals(-2.0, z.getImaginary(), 1.0e-5);
    }

// org.apache.commons.math.complex.ComplexTest::testSubtractNaN
    public void testSubtractNaN() {
        Complex x = new Complex(3.0, 4.0);
        Complex z = x.subtract(Complex.NaN);
        Assert.assertSame(Complex.NaN, z);
        z = new Complex(1, nan);
        Complex w = x.subtract(z);
        Assert.assertSame(Complex.NaN, w);
    }

// org.apache.commons.math.complex.ComplexTest::testSubtractInf
    public void testSubtractInf() {
        Complex x = new Complex(1, 1);
        Complex z = new Complex(neginf, 0);
        Complex w = x.subtract(z);
        Assert.assertEquals(w.getImaginary(), 1, 0);
        Assert.assertEquals(inf, w.getReal(), 0);

        x = new Complex(neginf, 0);
        Assert.assertTrue(Double.isNaN(x.subtract(z).getReal()));
    }

// org.apache.commons.math.complex.ComplexTest::testScalarSubtract
    public void testScalarSubtract() {
        Complex x = new Complex(3.0, 4.0);
        double yDouble = 2.0;
        Complex yComplex = new Complex(yDouble);
        Assert.assertEquals(x.subtract(yComplex), x.subtract(yDouble));
    }

// org.apache.commons.math.complex.ComplexTest::testScalarSubtractNaN
    public void testScalarSubtractNaN() {
        Complex x = new Complex(3.0, 4.0);
        double yDouble = Double.NaN;
        Complex yComplex = new Complex(yDouble);
        Assert.assertEquals(x.subtract(yComplex), x.subtract(yDouble));
    }

// org.apache.commons.math.complex.ComplexTest::testScalarSubtractInf
    public void testScalarSubtractInf() {
        Complex x = new Complex(1, 1);
        double yDouble = Double.POSITIVE_INFINITY;
        Complex yComplex = new Complex(yDouble);
        Assert.assertEquals(x.subtract(yComplex), x.subtract(yDouble));

        x = new Complex(neginf, 0);
        Assert.assertEquals(x.subtract(yComplex), x.subtract(yDouble));
    }

// org.apache.commons.math.complex.ComplexTest::testEqualsNull
    public void testEqualsNull() {
        Complex x = new Complex(3.0, 4.0);
        Assert.assertFalse(x.equals(null));
    }

// org.apache.commons.math.complex.ComplexTest::testEqualsClass
    public void testEqualsClass() {
        Complex x = new Complex(3.0, 4.0);
        Assert.assertFalse(x.equals(this));
    }

// org.apache.commons.math.complex.ComplexTest::testEqualsSame
    public void testEqualsSame() {
        Complex x = new Complex(3.0, 4.0);
        Assert.assertTrue(x.equals(x));
    }

// org.apache.commons.math.complex.ComplexTest::testEqualsTrue
    public void testEqualsTrue() {
        Complex x = new Complex(3.0, 4.0);
        Complex y = new Complex(3.0, 4.0);
        Assert.assertTrue(x.equals(y));
    }

// org.apache.commons.math.complex.ComplexTest::testEqualsRealDifference
    public void testEqualsRealDifference() {
        Complex x = new Complex(0.0, 0.0);
        Complex y = new Complex(0.0 + Double.MIN_VALUE, 0.0);
        Assert.assertFalse(x.equals(y));
    }

// org.apache.commons.math.complex.ComplexTest::testEqualsImaginaryDifference
    public void testEqualsImaginaryDifference() {
        Complex x = new Complex(0.0, 0.0);
        Complex y = new Complex(0.0, 0.0 + Double.MIN_VALUE);
        Assert.assertFalse(x.equals(y));
    }

// org.apache.commons.math.complex.ComplexTest::testEqualsNaN
    public void testEqualsNaN() {
        Complex realNaN = new Complex(Double.NaN, 0.0);
        Complex imaginaryNaN = new Complex(0.0, Double.NaN);
        Complex complexNaN = Complex.NaN;
        Assert.assertTrue(realNaN.equals(imaginaryNaN));
        Assert.assertTrue(imaginaryNaN.equals(complexNaN));
        Assert.assertTrue(realNaN.equals(complexNaN));
    }

// org.apache.commons.math.complex.ComplexTest::testHashCode
    public void testHashCode() {
        Complex x = new Complex(0.0, 0.0);
        Complex y = new Complex(0.0, 0.0 + Double.MIN_VALUE);
        Assert.assertFalse(x.hashCode()==y.hashCode());
        y = new Complex(0.0 + Double.MIN_VALUE, 0.0);
        Assert.assertFalse(x.hashCode()==y.hashCode());
        Complex realNaN = new Complex(Double.NaN, 0.0);
        Complex imaginaryNaN = new Complex(0.0, Double.NaN);
        Assert.assertEquals(realNaN.hashCode(), imaginaryNaN.hashCode());
        Assert.assertEquals(imaginaryNaN.hashCode(), Complex.NaN.hashCode());
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
        Assert.assertTrue(Complex.NaN.acos().isNaN());
    }

// org.apache.commons.math.complex.ComplexTest::testAsin
    public void testAsin() {
        Complex z = new Complex(3, 4);
        Complex expected = new Complex(0.633984, 2.30551);
        TestUtils.assertEquals(expected, z.asin(), 1.0e-5);
    }

// org.apache.commons.math.complex.ComplexTest::testAsinNaN
    public void testAsinNaN() {
        Assert.assertTrue(Complex.NaN.asin().isNaN());
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

// org.apache.commons.math.complex.ComplexTest::testAtanI
    public void testAtanI() {
        for (int i = -10; i < 10; i++) {
            System.out.println(new Complex(0, 1 - 0.1 * i).atan());
        }
        Assert.assertTrue(Complex.I.atan().isInfinite());
    }

// org.apache.commons.math.complex.ComplexTest::testAtanNaN
    public void testAtanNaN() {
        Assert.assertTrue(Complex.NaN.atan().isNaN());
    }

// org.apache.commons.math.complex.ComplexTest::testCos
    public void testCos() {
        Complex z = new Complex(3, 4);
        Complex expected = new Complex(-27.03495, -3.851153);
        TestUtils.assertEquals(expected, z.cos(), 1.0e-5);
    }

// org.apache.commons.math.complex.ComplexTest::testCosNaN
    public void testCosNaN() {
        Assert.assertTrue(Complex.NaN.cos().isNaN());
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
        Assert.assertTrue(Complex.NaN.cosh().isNaN());
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
        Assert.assertTrue(Complex.NaN.exp().isNaN());
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
        Assert.assertTrue(Complex.NaN.log().isNaN());
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
        Assert.assertTrue(Complex.NaN.pow(x).isNaN());
    }

// org.apache.commons.math.complex.ComplexTest::testPowNaNExponent
    public void testPowNaNExponent() {
        Complex x = new Complex(3, 4);
        Assert.assertTrue(x.pow(Complex.NaN).isNaN());
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

// org.apache.commons.math.complex.ComplexTest::testScalarPow
    public void testScalarPow() {
        Complex x = new Complex(3, 4);
        double yDouble = 5.0;
        Complex yComplex = new Complex(yDouble);
        Assert.assertEquals(x.pow(yComplex), x.pow(yDouble));
    }

// org.apache.commons.math.complex.ComplexTest::testScalarPowNaNBase
    public void testScalarPowNaNBase() {
        Complex x = Complex.NaN;
        double yDouble = 5.0;
        Complex yComplex = new Complex(yDouble);
        Assert.assertEquals(x.pow(yComplex), x.pow(yDouble));
    }

// org.apache.commons.math.complex.ComplexTest::testScalarPowNaNExponent
    public void testScalarPowNaNExponent() {
        Complex x = new Complex(3, 4);
        double yDouble = Double.NaN;
        Complex yComplex = new Complex(yDouble);
        Assert.assertEquals(x.pow(yComplex), x.pow(yDouble));
    }

// org.apache.commons.math.complex.ComplexTest::testScalarPowInf
   public void testScalarPowInf() {
       TestUtils.assertSame(Complex.NaN,Complex.ONE.pow(Double.POSITIVE_INFINITY));
       TestUtils.assertSame(Complex.NaN,Complex.ONE.pow(Double.NEGATIVE_INFINITY));
       TestUtils.assertSame(Complex.NaN,infOne.pow(1.0));
       TestUtils.assertSame(Complex.NaN,negInfOne.pow(1.0));
       TestUtils.assertSame(Complex.NaN,infInf.pow(1.0));
       TestUtils.assertSame(Complex.NaN,infNegInf.pow(1.0));
       TestUtils.assertSame(Complex.NaN,negInfInf.pow(10));
       TestUtils.assertSame(Complex.NaN,negInfNegInf.pow(1.0));
       TestUtils.assertSame(Complex.NaN,negInfNegInf.pow(Double.POSITIVE_INFINITY));
       TestUtils.assertSame(Complex.NaN,negInfNegInf.pow(Double.POSITIVE_INFINITY));
       TestUtils.assertSame(Complex.NaN,infInf.pow(Double.POSITIVE_INFINITY));
       TestUtils.assertSame(Complex.NaN,infInf.pow(Double.NEGATIVE_INFINITY));
       TestUtils.assertSame(Complex.NaN,infNegInf.pow(Double.NEGATIVE_INFINITY));
       TestUtils.assertSame(Complex.NaN,infNegInf.pow(Double.POSITIVE_INFINITY));
   }

// org.apache.commons.math.complex.ComplexTest::testScalarPowZero
   public void testScalarPowZero() {
       TestUtils.assertSame(Complex.NaN, Complex.ZERO.pow(1.0));
       TestUtils.assertSame(Complex.NaN, Complex.ZERO.pow(0.0));
       TestUtils.assertEquals(Complex.ONE, Complex.ONE.pow(0.0), 10e-12);
       TestUtils.assertEquals(Complex.ONE, Complex.I.pow(0.0), 10e-12);
       TestUtils.assertEquals(Complex.ONE, new Complex(-1, 3).pow(0.0), 10e-12);
   }

// org.apache.commons.math.complex.ComplexTest::testpowNull
    public void testpowNull() {
        Complex.ONE.pow(null);
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
        Assert.assertTrue(Complex.NaN.sin().isNaN());
    }

// org.apache.commons.math.complex.ComplexTest::testSinh
    public void testSinh() {
        Complex z = new Complex(3, 4);
        Complex expected = new Complex(-6.54812, -7.61923);
        TestUtils.assertEquals(expected, z.sinh(), 1.0e-5);
    }

// org.apache.commons.math.complex.ComplexTest::testSinhNaN
    public void testSinhNaN() {
        Assert.assertTrue(Complex.NaN.sinh().isNaN());
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
        Assert.assertTrue(Complex.NaN.sqrt().isNaN());
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
        Assert.assertTrue(Complex.NaN.sqrt1z().isNaN());
    }

// org.apache.commons.math.complex.ComplexTest::testTan
    public void testTan() {
        Complex z = new Complex(3, 4);
        Complex expected = new Complex(-0.000187346, 0.999356);
        TestUtils.assertEquals(expected, z.tan(), 1.0e-5);
    }

// org.apache.commons.math.complex.ComplexTest::testTanNaN
    public void testTanNaN() {
        Assert.assertTrue(Complex.NaN.tan().isNaN());
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
        Assert.assertTrue(Complex.NaN.tanh().isNaN());
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
        Assert.assertEquals(new Complex(0,-1), new Complex(0,1).multiply(new Complex(-1,0)));
    }

// org.apache.commons.math.complex.ComplexTest::testNthRoot_normal_thirdRoot
    public void testNthRoot_normal_thirdRoot() {
        
        Complex z = new Complex(-2,2);
        
        Complex[] thirdRootsOfZ = z.nthRoot(3).toArray(new Complex[0]);
        
        Assert.assertEquals(3, thirdRootsOfZ.length);
        
        Assert.assertEquals(1.0,                  thirdRootsOfZ[0].getReal(),      1.0e-5);
        Assert.assertEquals(1.0,                  thirdRootsOfZ[0].getImaginary(), 1.0e-5);
        
        Assert.assertEquals(-1.3660254037844386,  thirdRootsOfZ[1].getReal(),      1.0e-5);
        Assert.assertEquals(0.36602540378443843,  thirdRootsOfZ[1].getImaginary(), 1.0e-5);
        
        Assert.assertEquals(0.366025403784439,    thirdRootsOfZ[2].getReal(),      1.0e-5);
        Assert.assertEquals(-1.3660254037844384,  thirdRootsOfZ[2].getImaginary(), 1.0e-5);
    }

// org.apache.commons.math.complex.ComplexTest::testNthRoot_normal_fourthRoot
    public void testNthRoot_normal_fourthRoot() {
        
        Complex z = new Complex(5,-2);
        
        Complex[] fourthRootsOfZ = z.nthRoot(4).toArray(new Complex[0]);
        
        Assert.assertEquals(4, fourthRootsOfZ.length);
        
        Assert.assertEquals(1.5164629308487783,     fourthRootsOfZ[0].getReal(),      1.0e-5);
        Assert.assertEquals(-0.14469266210702247,   fourthRootsOfZ[0].getImaginary(), 1.0e-5);
        
        Assert.assertEquals(0.14469266210702256,    fourthRootsOfZ[1].getReal(),      1.0e-5);
        Assert.assertEquals(1.5164629308487783,     fourthRootsOfZ[1].getImaginary(), 1.0e-5);
        
        Assert.assertEquals(-1.5164629308487783,    fourthRootsOfZ[2].getReal(),      1.0e-5);
        Assert.assertEquals(0.14469266210702267,    fourthRootsOfZ[2].getImaginary(), 1.0e-5);
        
        Assert.assertEquals(-0.14469266210702275,   fourthRootsOfZ[3].getReal(),      1.0e-5);
        Assert.assertEquals(-1.5164629308487783,    fourthRootsOfZ[3].getImaginary(), 1.0e-5);
    }

// org.apache.commons.math.complex.ComplexTest::testNthRoot_cornercase_thirdRoot_imaginaryPartEmpty
    public void testNthRoot_cornercase_thirdRoot_imaginaryPartEmpty() {
        
        
        Complex z = new Complex(8,0);
        
        Complex[] thirdRootsOfZ = z.nthRoot(3).toArray(new Complex[0]);
        
        Assert.assertEquals(3, thirdRootsOfZ.length);
        
        Assert.assertEquals(2.0,                thirdRootsOfZ[0].getReal(),      1.0e-5);
        Assert.assertEquals(0.0,                thirdRootsOfZ[0].getImaginary(), 1.0e-5);
        
        Assert.assertEquals(-1.0,               thirdRootsOfZ[1].getReal(),      1.0e-5);
        Assert.assertEquals(1.7320508075688774, thirdRootsOfZ[1].getImaginary(), 1.0e-5);
        
        Assert.assertEquals(-1.0,               thirdRootsOfZ[2].getReal(),      1.0e-5);
        Assert.assertEquals(-1.732050807568877, thirdRootsOfZ[2].getImaginary(), 1.0e-5);
    }

// org.apache.commons.math.complex.ComplexTest::testNthRoot_cornercase_thirdRoot_realPartZero
    public void testNthRoot_cornercase_thirdRoot_realPartZero() {
        
        Complex z = new Complex(0,2);
        
        Complex[] thirdRootsOfZ = z.nthRoot(3).toArray(new Complex[0]);
        
        Assert.assertEquals(3, thirdRootsOfZ.length);
        
        Assert.assertEquals(1.0911236359717216,      thirdRootsOfZ[0].getReal(),      1.0e-5);
        Assert.assertEquals(0.6299605249474365,      thirdRootsOfZ[0].getImaginary(), 1.0e-5);
        
        Assert.assertEquals(-1.0911236359717216,     thirdRootsOfZ[1].getReal(),      1.0e-5);
        Assert.assertEquals(0.6299605249474365,      thirdRootsOfZ[1].getImaginary(), 1.0e-5);
        
        Assert.assertEquals(-2.3144374213981936E-16, thirdRootsOfZ[2].getReal(),      1.0e-5);
        Assert.assertEquals(-1.2599210498948732,     thirdRootsOfZ[2].getImaginary(), 1.0e-5);
    }

// org.apache.commons.math.complex.ComplexTest::testNthRoot_cornercase_NAN_Inf
    public void testNthRoot_cornercase_NAN_Inf() {
        
        List<Complex> roots = oneNaN.nthRoot(3);
        Assert.assertEquals(1,roots.size());
        Assert.assertEquals(Complex.NaN, roots.get(0));

        roots = nanZero.nthRoot(3);
        Assert.assertEquals(1,roots.size());
        Assert.assertEquals(Complex.NaN, roots.get(0));

        
        roots = nanInf.nthRoot(3);
        Assert.assertEquals(1,roots.size());
        Assert.assertEquals(Complex.NaN, roots.get(0));

        
        roots = oneInf.nthRoot(3);
        Assert.assertEquals(1,roots.size());
        Assert.assertEquals(Complex.INF, roots.get(0));

        
        roots = negInfInf.nthRoot(3);
        Assert.assertEquals(1,roots.size());
        Assert.assertEquals(Complex.INF, roots.get(0));
    }

// org.apache.commons.math.complex.ComplexTest::testGetArgument
    public void testGetArgument() {
        Complex z = new Complex(1, 0);
        Assert.assertEquals(0.0, z.getArgument(), 1.0e-12);

        z = new Complex(1, 1);
        Assert.assertEquals(FastMath.PI/4, z.getArgument(), 1.0e-12);

        z = new Complex(0, 1);
        Assert.assertEquals(FastMath.PI/2, z.getArgument(), 1.0e-12);

        z = new Complex(-1, 1);
        Assert.assertEquals(3 * FastMath.PI/4, z.getArgument(), 1.0e-12);

        z = new Complex(-1, 0);
        Assert.assertEquals(FastMath.PI, z.getArgument(), 1.0e-12);

        z = new Complex(-1, -1);
        Assert.assertEquals(-3 * FastMath.PI/4, z.getArgument(), 1.0e-12);

        z = new Complex(0, -1);
        Assert.assertEquals(-FastMath.PI/2, z.getArgument(), 1.0e-12);

        z = new Complex(1, -1);
        Assert.assertEquals(-FastMath.PI/4, z.getArgument(), 1.0e-12);

    }

// org.apache.commons.math.complex.ComplexTest::testGetArgumentInf
    public void testGetArgumentInf() {
        Assert.assertEquals(FastMath.PI/4, infInf.getArgument(), 1.0e-12);
        Assert.assertEquals(FastMath.PI/2, oneInf.getArgument(), 1.0e-12);
        Assert.assertEquals(0.0, infOne.getArgument(), 1.0e-12);
        Assert.assertEquals(FastMath.PI/2, zeroInf.getArgument(), 1.0e-12);
        Assert.assertEquals(0.0, infZero.getArgument(), 1.0e-12);
        Assert.assertEquals(FastMath.PI, negInfOne.getArgument(), 1.0e-12);
        Assert.assertEquals(-3.0*FastMath.PI/4, negInfNegInf.getArgument(), 1.0e-12);
        Assert.assertEquals(-FastMath.PI/2, oneNegInf.getArgument(), 1.0e-12);
    }

// org.apache.commons.math.complex.ComplexTest::testGetArgumentNaN
    public void testGetArgumentNaN() {
        Assert.assertTrue(Double.isNaN(nanZero.getArgument()));
        Assert.assertTrue(Double.isNaN(zeroNaN.getArgument()));
        Assert.assertTrue(Double.isNaN(Complex.NaN.getArgument()));
    }

// org.apache.commons.math.complex.ComplexTest::testSerial
    public void testSerial() {
        Complex z = new Complex(3.0, 4.0);
        Assert.assertEquals(z, TestUtils.serializeAndRecover(z));
        Complex ncmplx = (Complex)TestUtils.serializeAndRecover(oneNaN);
        Assert.assertEquals(nanZero, ncmplx);
        Assert.assertTrue(ncmplx.isNaN());
        Complex infcmplx = (Complex)TestUtils.serializeAndRecover(infInf);
        Assert.assertEquals(infInf, infcmplx);
        Assert.assertTrue(infcmplx.isInfinite());
        TestComplex tz = new TestComplex(3.0, 4.0);
        Assert.assertEquals(tz, TestUtils.serializeAndRecover(tz));
        TestComplex ntcmplx = (TestComplex)TestUtils.serializeAndRecover(new TestComplex(oneNaN));
        Assert.assertEquals(nanZero, ntcmplx);
        Assert.assertTrue(ntcmplx.isNaN());
        TestComplex inftcmplx = (TestComplex)TestUtils.serializeAndRecover(new TestComplex(infInf));
        Assert.assertEquals(infInf, inftcmplx);
        Assert.assertTrue(inftcmplx.isInfinite());
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
            Assert.fail("Expecting IllegalArgumentException");
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

// org.apache.commons.math.transform.FastCosineTransformerTest::testAdHocData
    public void testAdHocData() {
        FastCosineTransformer transformer = new FastCosineTransformer();
        double result[], tolerance = 1E-12;

        double x[] = { 0.0, 1.0, 4.0, 9.0, 16.0, 25.0, 36.0, 49.0, 64.0 };
        double y[] = { 172.0, -105.096569476353, 27.3137084989848,
                      -12.9593152353742, 8.0, -5.78585076868676,
                       4.68629150101524, -4.15826451958632, 4.0 };

        result = transformer.transform(x);
        for (int i = 0; i < result.length; i++) {
            Assert.assertEquals(y[i], result[i], tolerance);
        }

        result = transformer.inversetransform(y);
        for (int i = 0; i < result.length; i++) {
            Assert.assertEquals(x[i], result[i], tolerance);
        }

        FastFourierTransformer.scaleArray(x, FastMath.sqrt(0.5 * (x.length-1)));

        result = transformer.transform2(y);
        for (int i = 0; i < result.length; i++) {
            Assert.assertEquals(x[i], result[i], tolerance);
        }

        result = transformer.inversetransform2(x);
        for (int i = 0; i < result.length; i++) {
            Assert.assertEquals(y[i], result[i], tolerance);
        }
    }

// org.apache.commons.math.transform.FastCosineTransformerTest::testSinFunction
    public void testSinFunction() {
        UnivariateRealFunction f = new SinFunction();
        FastCosineTransformer transformer = new FastCosineTransformer();
        double min, max, result[], tolerance = 1E-12; int N = 9;

        double expected[] = { 0.0, 3.26197262739567, 0.0,
                             -2.17958042710327, 0.0, -0.648846697642915,
                              0.0, -0.433545502649478, 0.0 };
        min = 0.0; max = 2.0 * FastMath.PI * N / (N-1);
        result = transformer.transform(f, min, max, N);
        for (int i = 0; i < N; i++) {
            Assert.assertEquals(expected[i], result[i], tolerance);
        }

        min = -FastMath.PI; max = FastMath.PI * (N+1) / (N-1);
        result = transformer.transform(f, min, max, N);
        for (int i = 0; i < N; i++) {
            Assert.assertEquals(-expected[i], result[i], tolerance);
        }
    }

// org.apache.commons.math.transform.FastCosineTransformerTest::testParameters
    public void testParameters() throws Exception {
        UnivariateRealFunction f = new SinFunction();
        FastCosineTransformer transformer = new FastCosineTransformer();

        try {
            
            transformer.transform(f, 1, -1, 65);
            Assert.fail("Expecting IllegalArgumentException - bad interval");
        } catch (IllegalArgumentException ex) {
            
        }
        try {
            
            transformer.transform(f, -1, 1, 1);
            Assert.fail("Expecting IllegalArgumentException - bad samples number");
        } catch (IllegalArgumentException ex) {
            
        }
        try {
            
            transformer.transform(f, -1, 1, 64);
            Assert.fail("Expecting IllegalArgumentException - bad samples number");
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.transform.FastFourierTransformerTest::testAdHocData
    public void testAdHocData() {
        FastFourierTransformer transformer = new FastFourierTransformer();
        Complex result[]; double tolerance = 1E-12;

        double x[] = {1.3, 2.4, 1.7, 4.1, 2.9, 1.7, 5.1, 2.7};
        Complex y[] = {
            new Complex(21.9, 0.0),
            new Complex(-2.09497474683058, 1.91507575950825),
            new Complex(-2.6, 2.7),
            new Complex(-1.10502525316942, -4.88492424049175),
            new Complex(0.1, 0.0),
            new Complex(-1.10502525316942, 4.88492424049175),
            new Complex(-2.6, -2.7),
            new Complex(-2.09497474683058, -1.91507575950825)};

        result = transformer.transform(x);
        for (int i = 0; i < result.length; i++) {
            Assert.assertEquals(y[i].getReal(), result[i].getReal(), tolerance);
            Assert.assertEquals(y[i].getImaginary(), result[i].getImaginary(), tolerance);
        }

        result = transformer.inversetransform(y);
        for (int i = 0; i < result.length; i++) {
            Assert.assertEquals(x[i], result[i].getReal(), tolerance);
            Assert.assertEquals(0.0, result[i].getImaginary(), tolerance);
        }

        double x2[] = {10.4, 21.6, 40.8, 13.6, 23.2, 32.8, 13.6, 19.2};
        FastFourierTransformer.scaleArray(x2, 1.0 / FastMath.sqrt(x2.length));
        Complex y2[] = y;

        result = transformer.transform2(y2);
        for (int i = 0; i < result.length; i++) {
            Assert.assertEquals(x2[i], result[i].getReal(), tolerance);
            Assert.assertEquals(0.0, result[i].getImaginary(), tolerance);
        }

        result = transformer.inversetransform2(x2);
        for (int i = 0; i < result.length; i++) {
            Assert.assertEquals(y2[i].getReal(), result[i].getReal(), tolerance);
            Assert.assertEquals(y2[i].getImaginary(), result[i].getImaginary(), tolerance);
        }
    }

// org.apache.commons.math.transform.FastFourierTransformerTest::test2DData
    public void test2DData() {
        FastFourierTransformer transformer = new FastFourierTransformer();
        double tolerance = 1E-12;
        Complex[][] input = new Complex[][] {new Complex[] {new Complex(1, 0),
                                                            new Complex(2, 0)},
                                             new Complex[] {new Complex(3, 1),
                                                            new Complex(4, 2)}};
        Complex[][] goodOutput = new Complex[][] {new Complex[] {new Complex(5,
                1.5), new Complex(-1, -.5)}, new Complex[] {new Complex(-2,
                -1.5), new Complex(0, .5)}};
        Complex[][] output = (Complex[][])transformer.mdfft(input, true);
        Complex[][] output2 = (Complex[][])transformer.mdfft(output, false);

        Assert.assertEquals(input.length, output.length);
        Assert.assertEquals(input.length, output2.length);
        Assert.assertEquals(input[0].length, output[0].length);
        Assert.assertEquals(input[0].length, output2[0].length);
        Assert.assertEquals(input[1].length, output[1].length);
        Assert.assertEquals(input[1].length, output2[1].length);

        for (int i = 0; i < input.length; i++) {
            for (int j = 0; j < input[0].length; j++) {
                Assert.assertEquals(input[i][j].getImaginary(), output2[i][j].getImaginary(),
                             tolerance);
                Assert.assertEquals(input[i][j].getReal(), output2[i][j].getReal(), tolerance);
                Assert.assertEquals(goodOutput[i][j].getImaginary(), output[i][j].getImaginary(),
                             tolerance);
                Assert.assertEquals(goodOutput[i][j].getReal(), output[i][j].getReal(), tolerance);
            }
        }
    }

// org.apache.commons.math.transform.FastFourierTransformerTest::testSinFunction
    public void testSinFunction() {
        UnivariateRealFunction f = new SinFunction();
        FastFourierTransformer transformer = new FastFourierTransformer();
        Complex result[]; int N = 1 << 8;
        double min, max, tolerance = 1E-12;

        min = 0.0; max = 2.0 * FastMath.PI;
        result = transformer.transform(f, min, max, N);
        Assert.assertEquals(0.0, result[1].getReal(), tolerance);
        Assert.assertEquals(-(N >> 1), result[1].getImaginary(), tolerance);
        Assert.assertEquals(0.0, result[N-1].getReal(), tolerance);
        Assert.assertEquals(N >> 1, result[N-1].getImaginary(), tolerance);
        for (int i = 0; i < N-1; i += (i == 0 ? 2 : 1)) {
            Assert.assertEquals(0.0, result[i].getReal(), tolerance);
            Assert.assertEquals(0.0, result[i].getImaginary(), tolerance);
        }

        min = -FastMath.PI; max = FastMath.PI;
        result = transformer.inversetransform(f, min, max, N);
        Assert.assertEquals(0.0, result[1].getReal(), tolerance);
        Assert.assertEquals(-0.5, result[1].getImaginary(), tolerance);
        Assert.assertEquals(0.0, result[N-1].getReal(), tolerance);
        Assert.assertEquals(0.5, result[N-1].getImaginary(), tolerance);
        for (int i = 0; i < N-1; i += (i == 0 ? 2 : 1)) {
            Assert.assertEquals(0.0, result[i].getReal(), tolerance);
            Assert.assertEquals(0.0, result[i].getImaginary(), tolerance);
        }
    }

// org.apache.commons.math.transform.FastFourierTransformerTest::testParameters
    public void testParameters() throws Exception {
        UnivariateRealFunction f = new SinFunction();
        FastFourierTransformer transformer = new FastFourierTransformer();

        try {
            
            transformer.transform(f, 1, -1, 64);
            Assert.fail("Expecting IllegalArgumentException - bad interval");
        } catch (IllegalArgumentException ex) {
            
        }
        try {
            
            transformer.transform(f, -1, 1, 0);
            Assert.fail("Expecting IllegalArgumentException - bad samples number");
        } catch (IllegalArgumentException ex) {
            
        }
        try {
            
            transformer.transform(f, -1, 1, 100);
            Assert.fail("Expecting IllegalArgumentException - bad samples number");
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.transform.FastSineTransformerTest::testAdHocData
    public void testAdHocData() {
        FastSineTransformer transformer = new FastSineTransformer();
        double result[], tolerance = 1E-12;

        double x[] = { 0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0 };
        double y[] = { 0.0, 20.1093579685034, -9.65685424949238,
                       5.98642305066196, -4.0, 2.67271455167720,
                      -1.65685424949238, 0.795649469518633 };

        result = transformer.transform(x);
        for (int i = 0; i < result.length; i++) {
            Assert.assertEquals(y[i], result[i], tolerance);
        }

        result = transformer.inversetransform(y);
        for (int i = 0; i < result.length; i++) {
            Assert.assertEquals(x[i], result[i], tolerance);
        }

        FastFourierTransformer.scaleArray(x, FastMath.sqrt(x.length / 2.0));

        result = transformer.transform2(y);
        for (int i = 0; i < result.length; i++) {
            Assert.assertEquals(x[i], result[i], tolerance);
        }

        result = transformer.inversetransform2(x);
        for (int i = 0; i < result.length; i++) {
            Assert.assertEquals(y[i], result[i], tolerance);
        }
    }

// org.apache.commons.math.transform.FastSineTransformerTest::testSinFunction
    public void testSinFunction() {
        UnivariateRealFunction f = new SinFunction();
        FastSineTransformer transformer = new FastSineTransformer();
        double min, max, result[], tolerance = 1E-12; int N = 1 << 8;

        min = 0.0; max = 2.0 * FastMath.PI;
        result = transformer.transform(f, min, max, N);
        Assert.assertEquals(N >> 1, result[2], tolerance);
        for (int i = 0; i < N; i += (i == 1 ? 2 : 1)) {
            Assert.assertEquals(0.0, result[i], tolerance);
        }

        min = -FastMath.PI; max = FastMath.PI;
        result = transformer.transform(f, min, max, N);
        Assert.assertEquals(-(N >> 1), result[2], tolerance);
        for (int i = 0; i < N; i += (i == 1 ? 2 : 1)) {
            Assert.assertEquals(0.0, result[i], tolerance);
        }
    }

// org.apache.commons.math.transform.FastSineTransformerTest::testParameters
    public void testParameters() throws Exception {
        UnivariateRealFunction f = new SinFunction();
        FastSineTransformer transformer = new FastSineTransformer();

        try {
            
            transformer.transform(f, 1, -1, 64);
            Assert.fail("Expecting IllegalArgumentException - bad interval");
        } catch (IllegalArgumentException ex) {
            
        }
        try {
            
            transformer.transform(f, -1, 1, 0);
            Assert.fail("Expecting IllegalArgumentException - bad samples number");
        } catch (IllegalArgumentException ex) {
            
        }
        try {
            
            transformer.transform(f, -1, 1, 100);
            Assert.fail("Expecting IllegalArgumentException - bad samples number");
        } catch (IllegalArgumentException ex) {
            
        }
    }
