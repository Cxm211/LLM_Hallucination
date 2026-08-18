// buggy code
    public Complex reciprocal() {
        if (isNaN) {
            return NaN;
        }

        if (real == 0.0 && imaginary == 0.0) {
            return NaN;
        }

        if (isInfinite) {
            return ZERO;
        }

        if (FastMath.abs(real) < FastMath.abs(imaginary)) {
            double q = real / imaginary;
            double scale = 1. / (real * q + imaginary);
            return createComplex(scale * q, -scale);
        } else {
            double q = imaginary / real;
            double scale = 1. / (imaginary * q + real);
            return createComplex(scale, -scale * q);
        }
    }

// relevant test
// org.apache.commons.math3.analysis.solvers.LaguerreSolverTest::testLinearFunction
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

// org.apache.commons.math3.analysis.solvers.LaguerreSolverTest::testQuadraticFunction
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

// org.apache.commons.math3.analysis.solvers.LaguerreSolverTest::testQuinticFunction
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

// org.apache.commons.math3.analysis.solvers.LaguerreSolverTest::testQuinticFunction2
    public void testQuinticFunction2() {
        
        final double[] coefficients = { 4.0, 0.0, 1.0, 4.0, 0.0, 1.0 };
        final LaguerreSolver solver = new LaguerreSolver();
        final Complex[] result = solver.solveAllComplex(coefficients, 0);

        for (Complex expected : new Complex[] { new Complex(0, -2),
                                                new Complex(0, 2),
                                                new Complex(0.5, 0.5 * FastMath.sqrt(3)),
                                                new Complex(-1, 0),
                                                new Complex(0.5, -0.5 * FastMath.sqrt(3.0)) }) {
            final double tolerance = FastMath.max(solver.getAbsoluteAccuracy(),
                                                  FastMath.abs(expected.abs() * solver.getRelativeAccuracy()));
            TestUtils.assertContains(result, expected, tolerance);
        }
    }

// org.apache.commons.math3.analysis.solvers.LaguerreSolverTest::testParameters
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

// org.apache.commons.math3.complex.ComplexFieldTest::testZero
    public void testZero() {
        Assert.assertEquals(Complex.ZERO, ComplexField.getInstance().getZero());
    }

// org.apache.commons.math3.complex.ComplexFieldTest::testOne
    public void testOne() {
        Assert.assertEquals(Complex.ONE, ComplexField.getInstance().getOne());
    }

// org.apache.commons.math3.complex.ComplexFieldTest::testSerial
    public void testSerial() {
        
        ComplexField field = ComplexField.getInstance();
        Assert.assertTrue(field == TestUtils.serializeAndRecover(field));
    }

// org.apache.commons.math3.complex.ComplexTest::testConstructor
    public void testConstructor() {
        Complex z = new Complex(3.0, 4.0);
        Assert.assertEquals(3.0, z.getReal(), 1.0e-5);
        Assert.assertEquals(4.0, z.getImaginary(), 1.0e-5);
    }

// org.apache.commons.math3.complex.ComplexTest::testConstructorNaN
    public void testConstructorNaN() {
        Complex z = new Complex(3.0, Double.NaN);
        Assert.assertTrue(z.isNaN());

        z = new Complex(nan, 4.0);
        Assert.assertTrue(z.isNaN());

        z = new Complex(3.0, 4.0);
        Assert.assertFalse(z.isNaN());
    }

// org.apache.commons.math3.complex.ComplexTest::testAbs
    public void testAbs() {
        Complex z = new Complex(3.0, 4.0);
        Assert.assertEquals(5.0, z.abs(), 1.0e-5);
    }

// org.apache.commons.math3.complex.ComplexTest::testAbsNaN
    public void testAbsNaN() {
        Assert.assertTrue(Double.isNaN(Complex.NaN.abs()));
        Complex z = new Complex(inf, nan);
        Assert.assertTrue(Double.isNaN(z.abs()));
    }

// org.apache.commons.math3.complex.ComplexTest::testAbsInfinite
    public void testAbsInfinite() {
        Complex z = new Complex(inf, 0);
        Assert.assertEquals(inf, z.abs(), 0);
        z = new Complex(0, neginf);
        Assert.assertEquals(inf, z.abs(), 0);
        z = new Complex(inf, neginf);
        Assert.assertEquals(inf, z.abs(), 0);
    }

// org.apache.commons.math3.complex.ComplexTest::testAdd
    public void testAdd() {
        Complex x = new Complex(3.0, 4.0);
        Complex y = new Complex(5.0, 6.0);
        Complex z = x.add(y);
        Assert.assertEquals(8.0, z.getReal(), 1.0e-5);
        Assert.assertEquals(10.0, z.getImaginary(), 1.0e-5);
    }

// org.apache.commons.math3.complex.ComplexTest::testAddNaN
    public void testAddNaN() {
        Complex x = new Complex(3.0, 4.0);
        Complex z = x.add(Complex.NaN);
        Assert.assertSame(Complex.NaN, z);
        z = new Complex(1, nan);
        Complex w = x.add(z);
        Assert.assertSame(Complex.NaN, w);
    }

// org.apache.commons.math3.complex.ComplexTest::testAddInf
    public void testAddInf() {
        Complex x = new Complex(1, 1);
        Complex z = new Complex(inf, 0);
        Complex w = x.add(z);
        Assert.assertEquals(w.getImaginary(), 1, 0);
        Assert.assertEquals(inf, w.getReal(), 0);

        x = new Complex(neginf, 0);
        Assert.assertTrue(Double.isNaN(x.add(z).getReal()));
    }

// org.apache.commons.math3.complex.ComplexTest::testScalarAdd
    public void testScalarAdd() {
        Complex x = new Complex(3.0, 4.0);
        double yDouble = 2.0;
        Complex yComplex = new Complex(yDouble);
        Assert.assertEquals(x.add(yComplex), x.add(yDouble));
    }

// org.apache.commons.math3.complex.ComplexTest::testScalarAddNaN
    public void testScalarAddNaN() {
        Complex x = new Complex(3.0, 4.0);
        double yDouble = Double.NaN;
        Complex yComplex = new Complex(yDouble);
        Assert.assertEquals(x.add(yComplex), x.add(yDouble));
    }

// org.apache.commons.math3.complex.ComplexTest::testScalarAddInf
    public void testScalarAddInf() {
        Complex x = new Complex(1, 1);
        double yDouble = Double.POSITIVE_INFINITY;

        Complex yComplex = new Complex(yDouble);
        Assert.assertEquals(x.add(yComplex), x.add(yDouble));

        x = new Complex(neginf, 0);
        Assert.assertEquals(x.add(yComplex), x.add(yDouble));
    }

// org.apache.commons.math3.complex.ComplexTest::testConjugate
    public void testConjugate() {
        Complex x = new Complex(3.0, 4.0);
        Complex z = x.conjugate();
        Assert.assertEquals(3.0, z.getReal(), 1.0e-5);
        Assert.assertEquals(-4.0, z.getImaginary(), 1.0e-5);
    }

// org.apache.commons.math3.complex.ComplexTest::testConjugateNaN
    public void testConjugateNaN() {
        Complex z = Complex.NaN.conjugate();
        Assert.assertTrue(z.isNaN());
    }

// org.apache.commons.math3.complex.ComplexTest::testConjugateInfiinite
    public void testConjugateInfiinite() {
        Complex z = new Complex(0, inf);
        Assert.assertEquals(neginf, z.conjugate().getImaginary(), 0);
        z = new Complex(0, neginf);
        Assert.assertEquals(inf, z.conjugate().getImaginary(), 0);
    }

// org.apache.commons.math3.complex.ComplexTest::testDivide
    public void testDivide() {
        Complex x = new Complex(3.0, 4.0);
        Complex y = new Complex(5.0, 6.0);
        Complex z = x.divide(y);
        Assert.assertEquals(39.0 / 61.0, z.getReal(), 1.0e-5);
        Assert.assertEquals(2.0 / 61.0, z.getImaginary(), 1.0e-5);
    }

// org.apache.commons.math3.complex.ComplexTest::testDivideReal
    public void testDivideReal() {
        Complex x = new Complex(2d, 3d);
        Complex y = new Complex(2d, 0d);
        Assert.assertEquals(new Complex(1d, 1.5), x.divide(y));

    }

// org.apache.commons.math3.complex.ComplexTest::testDivideImaginary
    public void testDivideImaginary() {
        Complex x = new Complex(2d, 3d);
        Complex y = new Complex(0d, 2d);
        Assert.assertEquals(new Complex(1.5d, -1d), x.divide(y));
    }

// org.apache.commons.math3.complex.ComplexTest::testDivideInf
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

// org.apache.commons.math3.complex.ComplexTest::testDivideZero
    public void testDivideZero() {
        Complex x = new Complex(3.0, 4.0);
        Complex z = x.divide(Complex.ZERO);
        
        Assert.assertEquals(z, Complex.NaN);
    }

// org.apache.commons.math3.complex.ComplexTest::testDivideZeroZero
    public void testDivideZeroZero() {
        Complex x = new Complex(0.0, 0.0);
        Complex z = x.divide(Complex.ZERO);
        Assert.assertEquals(z, Complex.NaN);
    }

// org.apache.commons.math3.complex.ComplexTest::testDivideNaN
    public void testDivideNaN() {
        Complex x = new Complex(3.0, 4.0);
        Complex z = x.divide(Complex.NaN);
        Assert.assertTrue(z.isNaN());
    }

// org.apache.commons.math3.complex.ComplexTest::testDivideNaNInf
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

// org.apache.commons.math3.complex.ComplexTest::testScalarDivide
    public void testScalarDivide() {
        Complex x = new Complex(3.0, 4.0);
        double yDouble = 2.0;
        Complex yComplex = new Complex(yDouble);
        Assert.assertEquals(x.divide(yComplex), x.divide(yDouble));
    }

// org.apache.commons.math3.complex.ComplexTest::testScalarDivideNaN
    public void testScalarDivideNaN() {
        Complex x = new Complex(3.0, 4.0);
        double yDouble = Double.NaN;
        Complex yComplex = new Complex(yDouble);
        Assert.assertEquals(x.divide(yComplex), x.divide(yDouble));
    }

// org.apache.commons.math3.complex.ComplexTest::testScalarDivideInf
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

// org.apache.commons.math3.complex.ComplexTest::testScalarDivideZero
    public void testScalarDivideZero() {
        Complex x = new Complex(1,1);
        TestUtils.assertEquals(x.divide(Complex.ZERO), x.divide(0), 0);
    }

// org.apache.commons.math3.complex.ComplexTest::testReciprocal
    public void testReciprocal() {
        Complex z = new Complex(5.0, 6.0);
        Complex act = z.reciprocal();
        double expRe = 5.0 / 61.0;
        double expIm = -6.0 / 61.0;
        Assert.assertEquals(expRe, act.getReal(), FastMath.ulp(expRe));
        Assert.assertEquals(expIm, act.getImaginary(), FastMath.ulp(expIm));
    }

// org.apache.commons.math3.complex.ComplexTest::testReciprocalReal
    public void testReciprocalReal() {
        Complex z = new Complex(-2.0, 0.0);
        Assert.assertEquals(new Complex(-0.5, 0.0), z.reciprocal());
    }

// org.apache.commons.math3.complex.ComplexTest::testReciprocalImaginary
    public void testReciprocalImaginary() {
        Complex z = new Complex(0.0, -2.0);
        Assert.assertEquals(new Complex(0.0, 0.5), z.reciprocal());
    }

// org.apache.commons.math3.complex.ComplexTest::testReciprocalInf
    public void testReciprocalInf() {
        Complex z = new Complex(neginf, inf);
        Assert.assertTrue(z.reciprocal().equals(Complex.ZERO));

        z = new Complex(1, inf).reciprocal();
        Assert.assertEquals(z, Complex.ZERO);
    }

// org.apache.commons.math3.complex.ComplexTest::testReciprocalZero
    public void testReciprocalZero() {
        Assert.assertEquals(Complex.ZERO.reciprocal(), Complex.INF);
    }

// org.apache.commons.math3.complex.ComplexTest::testReciprocalNaN
    public void testReciprocalNaN() {
        Assert.assertTrue(Complex.NaN.reciprocal().isNaN());
    }

// org.apache.commons.math3.complex.ComplexTest::testMultiply
    public void testMultiply() {
        Complex x = new Complex(3.0, 4.0);
        Complex y = new Complex(5.0, 6.0);
        Complex z = x.multiply(y);
        Assert.assertEquals(-9.0, z.getReal(), 1.0e-5);
        Assert.assertEquals(38.0, z.getImaginary(), 1.0e-5);
    }

// org.apache.commons.math3.complex.ComplexTest::testMultiplyNaN
    public void testMultiplyNaN() {
        Complex x = new Complex(3.0, 4.0);
        Complex z = x.multiply(Complex.NaN);
        Assert.assertSame(Complex.NaN, z);
        z = Complex.NaN.multiply(5);
        Assert.assertSame(Complex.NaN, z);
    }

// org.apache.commons.math3.complex.ComplexTest::testMultiplyInfInf
    public void testMultiplyInfInf() {
        
        Assert.assertTrue(infInf.multiply(infInf).isInfinite());
    }

// org.apache.commons.math3.complex.ComplexTest::testMultiplyNaNInf
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

// org.apache.commons.math3.complex.ComplexTest::testScalarMultiply
    public void testScalarMultiply() {
        Complex x = new Complex(3.0, 4.0);
        double yDouble = 2.0;
        Complex yComplex = new Complex(yDouble);
        Assert.assertEquals(x.multiply(yComplex), x.multiply(yDouble));
        int zInt = -5;
        Complex zComplex = new Complex(zInt);
        Assert.assertEquals(x.multiply(zComplex), x.multiply(zInt));
    }

// org.apache.commons.math3.complex.ComplexTest::testScalarMultiplyNaN
    public void testScalarMultiplyNaN() {
        Complex x = new Complex(3.0, 4.0);
        double yDouble = Double.NaN;
        Complex yComplex = new Complex(yDouble);
        Assert.assertEquals(x.multiply(yComplex), x.multiply(yDouble));
    }

// org.apache.commons.math3.complex.ComplexTest::testScalarMultiplyInf
    public void testScalarMultiplyInf() {
        Complex x = new Complex(1, 1);
        double yDouble = Double.POSITIVE_INFINITY;
        Complex yComplex = new Complex(yDouble);
        Assert.assertEquals(x.multiply(yComplex), x.multiply(yDouble));

        yDouble = Double.NEGATIVE_INFINITY;
        yComplex = new Complex(yDouble);
        Assert.assertEquals(x.multiply(yComplex), x.multiply(yDouble));
    }

// org.apache.commons.math3.complex.ComplexTest::testNegate
    public void testNegate() {
        Complex x = new Complex(3.0, 4.0);
        Complex z = x.negate();
        Assert.assertEquals(-3.0, z.getReal(), 1.0e-5);
        Assert.assertEquals(-4.0, z.getImaginary(), 1.0e-5);
    }

// org.apache.commons.math3.complex.ComplexTest::testNegateNaN
    public void testNegateNaN() {
        Complex z = Complex.NaN.negate();
        Assert.assertTrue(z.isNaN());
    }

// org.apache.commons.math3.complex.ComplexTest::testSubtract
    public void testSubtract() {
        Complex x = new Complex(3.0, 4.0);
        Complex y = new Complex(5.0, 6.0);
        Complex z = x.subtract(y);
        Assert.assertEquals(-2.0, z.getReal(), 1.0e-5);
        Assert.assertEquals(-2.0, z.getImaginary(), 1.0e-5);
    }

// org.apache.commons.math3.complex.ComplexTest::testSubtractNaN
    public void testSubtractNaN() {
        Complex x = new Complex(3.0, 4.0);
        Complex z = x.subtract(Complex.NaN);
        Assert.assertSame(Complex.NaN, z);
        z = new Complex(1, nan);
        Complex w = x.subtract(z);
        Assert.assertSame(Complex.NaN, w);
    }

// org.apache.commons.math3.complex.ComplexTest::testSubtractInf
    public void testSubtractInf() {
        Complex x = new Complex(1, 1);
        Complex z = new Complex(neginf, 0);
        Complex w = x.subtract(z);
        Assert.assertEquals(w.getImaginary(), 1, 0);
        Assert.assertEquals(inf, w.getReal(), 0);

        x = new Complex(neginf, 0);
        Assert.assertTrue(Double.isNaN(x.subtract(z).getReal()));
    }

// org.apache.commons.math3.complex.ComplexTest::testScalarSubtract
    public void testScalarSubtract() {
        Complex x = new Complex(3.0, 4.0);
        double yDouble = 2.0;
        Complex yComplex = new Complex(yDouble);
        Assert.assertEquals(x.subtract(yComplex), x.subtract(yDouble));
    }

// org.apache.commons.math3.complex.ComplexTest::testScalarSubtractNaN
    public void testScalarSubtractNaN() {
        Complex x = new Complex(3.0, 4.0);
        double yDouble = Double.NaN;
        Complex yComplex = new Complex(yDouble);
        Assert.assertEquals(x.subtract(yComplex), x.subtract(yDouble));
    }

// org.apache.commons.math3.complex.ComplexTest::testScalarSubtractInf
    public void testScalarSubtractInf() {
        Complex x = new Complex(1, 1);
        double yDouble = Double.POSITIVE_INFINITY;
        Complex yComplex = new Complex(yDouble);
        Assert.assertEquals(x.subtract(yComplex), x.subtract(yDouble));

        x = new Complex(neginf, 0);
        Assert.assertEquals(x.subtract(yComplex), x.subtract(yDouble));
    }

// org.apache.commons.math3.complex.ComplexTest::testEqualsNull
    public void testEqualsNull() {
        Complex x = new Complex(3.0, 4.0);
        Assert.assertFalse(x.equals(null));
    }

// org.apache.commons.math3.complex.ComplexTest::testEqualsClass
    public void testEqualsClass() {
        Complex x = new Complex(3.0, 4.0);
        Assert.assertFalse(x.equals(this));
    }

// org.apache.commons.math3.complex.ComplexTest::testEqualsSame
    public void testEqualsSame() {
        Complex x = new Complex(3.0, 4.0);
        Assert.assertTrue(x.equals(x));
    }

// org.apache.commons.math3.complex.ComplexTest::testEqualsTrue
    public void testEqualsTrue() {
        Complex x = new Complex(3.0, 4.0);
        Complex y = new Complex(3.0, 4.0);
        Assert.assertTrue(x.equals(y));
    }

// org.apache.commons.math3.complex.ComplexTest::testEqualsRealDifference
    public void testEqualsRealDifference() {
        Complex x = new Complex(0.0, 0.0);
        Complex y = new Complex(0.0 + Double.MIN_VALUE, 0.0);
        Assert.assertFalse(x.equals(y));
    }

// org.apache.commons.math3.complex.ComplexTest::testEqualsImaginaryDifference
    public void testEqualsImaginaryDifference() {
        Complex x = new Complex(0.0, 0.0);
        Complex y = new Complex(0.0, 0.0 + Double.MIN_VALUE);
        Assert.assertFalse(x.equals(y));
    }

// org.apache.commons.math3.complex.ComplexTest::testEqualsNaN
    public void testEqualsNaN() {
        Complex realNaN = new Complex(Double.NaN, 0.0);
        Complex imaginaryNaN = new Complex(0.0, Double.NaN);
        Complex complexNaN = Complex.NaN;
        Assert.assertTrue(realNaN.equals(imaginaryNaN));
        Assert.assertTrue(imaginaryNaN.equals(complexNaN));
        Assert.assertTrue(realNaN.equals(complexNaN));
    }

// org.apache.commons.math3.complex.ComplexTest::testHashCode
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

// org.apache.commons.math3.complex.ComplexTest::testAcos
    public void testAcos() {
        Complex z = new Complex(3, 4);
        Complex expected = new Complex(0.936812, -2.30551);
        TestUtils.assertEquals(expected, z.acos(), 1.0e-5);
        TestUtils.assertEquals(new Complex(FastMath.acos(0), 0),
                Complex.ZERO.acos(), 1.0e-12);
    }

// org.apache.commons.math3.complex.ComplexTest::testAcosInf
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

// org.apache.commons.math3.complex.ComplexTest::testAcosNaN
    public void testAcosNaN() {
        Assert.assertTrue(Complex.NaN.acos().isNaN());
    }

// org.apache.commons.math3.complex.ComplexTest::testAsin
    public void testAsin() {
        Complex z = new Complex(3, 4);
        Complex expected = new Complex(0.633984, 2.30551);
        TestUtils.assertEquals(expected, z.asin(), 1.0e-5);
    }

// org.apache.commons.math3.complex.ComplexTest::testAsinNaN
    public void testAsinNaN() {
        Assert.assertTrue(Complex.NaN.asin().isNaN());
    }

// org.apache.commons.math3.complex.ComplexTest::testAsinInf
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

// org.apache.commons.math3.complex.ComplexTest::testAtan
    public void testAtan() {
        Complex z = new Complex(3, 4);
        Complex expected = new Complex(1.44831, 0.158997);
        TestUtils.assertEquals(expected, z.atan(), 1.0e-5);
    }

// org.apache.commons.math3.complex.ComplexTest::testAtanInf
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

// org.apache.commons.math3.complex.ComplexTest::testAtanI
    public void testAtanI() {
        Assert.assertTrue(Complex.I.atan().isNaN());
    }

// org.apache.commons.math3.complex.ComplexTest::testAtanNaN
    public void testAtanNaN() {
        Assert.assertTrue(Complex.NaN.atan().isNaN());
    }

// org.apache.commons.math3.complex.ComplexTest::testCos
    public void testCos() {
        Complex z = new Complex(3, 4);
        Complex expected = new Complex(-27.03495, -3.851153);
        TestUtils.assertEquals(expected, z.cos(), 1.0e-5);
    }

// org.apache.commons.math3.complex.ComplexTest::testCosNaN
    public void testCosNaN() {
        Assert.assertTrue(Complex.NaN.cos().isNaN());
    }

// org.apache.commons.math3.complex.ComplexTest::testCosInf
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

// org.apache.commons.math3.complex.ComplexTest::testCosh
    public void testCosh() {
        Complex z = new Complex(3, 4);
        Complex expected = new Complex(-6.58066, -7.58155);
        TestUtils.assertEquals(expected, z.cosh(), 1.0e-5);
    }

// org.apache.commons.math3.complex.ComplexTest::testCoshNaN
    public void testCoshNaN() {
        Assert.assertTrue(Complex.NaN.cosh().isNaN());
    }

// org.apache.commons.math3.complex.ComplexTest::testCoshInf
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

// org.apache.commons.math3.complex.ComplexTest::testExp
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

// org.apache.commons.math3.complex.ComplexTest::testExpNaN
    public void testExpNaN() {
        Assert.assertTrue(Complex.NaN.exp().isNaN());
    }

// org.apache.commons.math3.complex.ComplexTest::testExpInf
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

// org.apache.commons.math3.complex.ComplexTest::testLog
    public void testLog() {
        Complex z = new Complex(3, 4);
        Complex expected = new Complex(1.60944, 0.927295);
        TestUtils.assertEquals(expected, z.log(), 1.0e-5);
    }

// org.apache.commons.math3.complex.ComplexTest::testLogNaN
    public void testLogNaN() {
        Assert.assertTrue(Complex.NaN.log().isNaN());
    }

// org.apache.commons.math3.complex.ComplexTest::testLogInf
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

// org.apache.commons.math3.complex.ComplexTest::testLogZero
    public void testLogZero() {
        TestUtils.assertSame(negInfZero, Complex.ZERO.log());
    }

// org.apache.commons.math3.complex.ComplexTest::testPow
    public void testPow() {
        Complex x = new Complex(3, 4);
        Complex y = new Complex(5, 6);
        Complex expected = new Complex(-1.860893, 11.83677);
        TestUtils.assertEquals(expected, x.pow(y), 1.0e-5);
    }

// org.apache.commons.math3.complex.ComplexTest::testPowNaNBase
    public void testPowNaNBase() {
        Complex x = new Complex(3, 4);
        Assert.assertTrue(Complex.NaN.pow(x).isNaN());
    }

// org.apache.commons.math3.complex.ComplexTest::testPowNaNExponent
    public void testPowNaNExponent() {
        Complex x = new Complex(3, 4);
        Assert.assertTrue(x.pow(Complex.NaN).isNaN());
    }

// org.apache.commons.math3.complex.ComplexTest::testPowInf
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

// org.apache.commons.math3.complex.ComplexTest::testPowZero
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

// org.apache.commons.math3.complex.ComplexTest::testScalarPow
    public void testScalarPow() {
        Complex x = new Complex(3, 4);
        double yDouble = 5.0;
        Complex yComplex = new Complex(yDouble);
        Assert.assertEquals(x.pow(yComplex), x.pow(yDouble));
    }

// org.apache.commons.math3.complex.ComplexTest::testScalarPowNaNBase
    public void testScalarPowNaNBase() {
        Complex x = Complex.NaN;
        double yDouble = 5.0;
        Complex yComplex = new Complex(yDouble);
        Assert.assertEquals(x.pow(yComplex), x.pow(yDouble));
    }

// org.apache.commons.math3.complex.ComplexTest::testScalarPowNaNExponent
    public void testScalarPowNaNExponent() {
        Complex x = new Complex(3, 4);
        double yDouble = Double.NaN;
        Complex yComplex = new Complex(yDouble);
        Assert.assertEquals(x.pow(yComplex), x.pow(yDouble));
    }

// org.apache.commons.math3.complex.ComplexTest::testScalarPowInf
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

// org.apache.commons.math3.complex.ComplexTest::testScalarPowZero
   public void testScalarPowZero() {
       TestUtils.assertSame(Complex.NaN, Complex.ZERO.pow(1.0));
       TestUtils.assertSame(Complex.NaN, Complex.ZERO.pow(0.0));
       TestUtils.assertEquals(Complex.ONE, Complex.ONE.pow(0.0), 10e-12);
       TestUtils.assertEquals(Complex.ONE, Complex.I.pow(0.0), 10e-12);
       TestUtils.assertEquals(Complex.ONE, new Complex(-1, 3).pow(0.0), 10e-12);
   }

// org.apache.commons.math3.complex.ComplexTest::testpowNull
    public void testpowNull() {
        Complex.ONE.pow(null);
    }

// org.apache.commons.math3.complex.ComplexTest::testSin
    public void testSin() {
        Complex z = new Complex(3, 4);
        Complex expected = new Complex(3.853738, -27.01681);
        TestUtils.assertEquals(expected, z.sin(), 1.0e-5);
    }

// org.apache.commons.math3.complex.ComplexTest::testSinInf
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

// org.apache.commons.math3.complex.ComplexTest::testSinNaN
    public void testSinNaN() {
        Assert.assertTrue(Complex.NaN.sin().isNaN());
    }

// org.apache.commons.math3.complex.ComplexTest::testSinh
    public void testSinh() {
        Complex z = new Complex(3, 4);
        Complex expected = new Complex(-6.54812, -7.61923);
        TestUtils.assertEquals(expected, z.sinh(), 1.0e-5);
    }

// org.apache.commons.math3.complex.ComplexTest::testSinhNaN
    public void testSinhNaN() {
        Assert.assertTrue(Complex.NaN.sinh().isNaN());
    }

// org.apache.commons.math3.complex.ComplexTest::testSinhInf
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

// org.apache.commons.math3.complex.ComplexTest::testSqrtRealPositive
    public void testSqrtRealPositive() {
        Complex z = new Complex(3, 4);
        Complex expected = new Complex(2, 1);
        TestUtils.assertEquals(expected, z.sqrt(), 1.0e-5);
    }

// org.apache.commons.math3.complex.ComplexTest::testSqrtRealZero
    public void testSqrtRealZero() {
        Complex z = new Complex(0.0, 4);
        Complex expected = new Complex(1.41421, 1.41421);
        TestUtils.assertEquals(expected, z.sqrt(), 1.0e-5);
    }

// org.apache.commons.math3.complex.ComplexTest::testSqrtRealNegative
    public void testSqrtRealNegative() {
        Complex z = new Complex(-3.0, 4);
        Complex expected = new Complex(1, 2);
        TestUtils.assertEquals(expected, z.sqrt(), 1.0e-5);
    }

// org.apache.commons.math3.complex.ComplexTest::testSqrtImaginaryZero
    public void testSqrtImaginaryZero() {
        Complex z = new Complex(-3.0, 0.0);
        Complex expected = new Complex(0.0, 1.73205);
        TestUtils.assertEquals(expected, z.sqrt(), 1.0e-5);
    }

// org.apache.commons.math3.complex.ComplexTest::testSqrtImaginaryNegative
    public void testSqrtImaginaryNegative() {
        Complex z = new Complex(-3.0, -4.0);
        Complex expected = new Complex(1.0, -2.0);
        TestUtils.assertEquals(expected, z.sqrt(), 1.0e-5);
    }

// org.apache.commons.math3.complex.ComplexTest::testSqrtPolar
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

// org.apache.commons.math3.complex.ComplexTest::testSqrtNaN
    public void testSqrtNaN() {
        Assert.assertTrue(Complex.NaN.sqrt().isNaN());
    }

// org.apache.commons.math3.complex.ComplexTest::testSqrtInf
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

// org.apache.commons.math3.complex.ComplexTest::testSqrt1z
    public void testSqrt1z() {
        Complex z = new Complex(3, 4);
        Complex expected = new Complex(4.08033, -2.94094);
        TestUtils.assertEquals(expected, z.sqrt1z(), 1.0e-5);
    }

// org.apache.commons.math3.complex.ComplexTest::testSqrt1zNaN
    public void testSqrt1zNaN() {
        Assert.assertTrue(Complex.NaN.sqrt1z().isNaN());
    }

// org.apache.commons.math3.complex.ComplexTest::testTan
    public void testTan() {
        Complex z = new Complex(3, 4);
        Complex expected = new Complex(-0.000187346, 0.999356);
        TestUtils.assertEquals(expected, z.tan(), 1.0e-5);
        
        Complex actual = new Complex(3.0, 1E10).tan();
        expected = new Complex(0, 1);
        TestUtils.assertEquals(expected, actual, 1.0e-5);
        actual = new Complex(3.0, -1E10).tan();
        expected = new Complex(0, -1);
        TestUtils.assertEquals(expected, actual, 1.0e-5);
    }

// org.apache.commons.math3.complex.ComplexTest::testTanNaN
    public void testTanNaN() {
        Assert.assertTrue(Complex.NaN.tan().isNaN());
    }

// org.apache.commons.math3.complex.ComplexTest::testTanInf
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

// org.apache.commons.math3.complex.ComplexTest::testTanCritical
   public void testTanCritical() {
        TestUtils.assertSame(infNaN, new Complex(pi/2, 0).tan());
        TestUtils.assertSame(negInfNaN, new Complex(-pi/2, 0).tan());
    }

// org.apache.commons.math3.complex.ComplexTest::testTanh
    public void testTanh() {
        Complex z = new Complex(3, 4);
        Complex expected = new Complex(1.00071, 0.00490826);
        TestUtils.assertEquals(expected, z.tanh(), 1.0e-5);
        
        Complex actual = new Complex(1E10, 3.0).tanh();
        expected = new Complex(1, 0);
        TestUtils.assertEquals(expected, actual, 1.0e-5);
        actual = new Complex(-1E10, 3.0).tanh();
        expected = new Complex(-1, 0);
        TestUtils.assertEquals(expected, actual, 1.0e-5);
    }

// org.apache.commons.math3.complex.ComplexTest::testTanhNaN
    public void testTanhNaN() {
        Assert.assertTrue(Complex.NaN.tanh().isNaN());
    }

// org.apache.commons.math3.complex.ComplexTest::testTanhInf
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

// org.apache.commons.math3.complex.ComplexTest::testTanhCritical
    public void testTanhCritical() {
        TestUtils.assertSame(nanInf, new Complex(0, pi/2).tanh());
    }

// org.apache.commons.math3.complex.ComplexTest::testMath221
    public void testMath221() {
        Assert.assertEquals(new Complex(0,-1), new Complex(0,1).multiply(new Complex(-1,0)));
    }

// org.apache.commons.math3.complex.ComplexTest::testNthRoot_normal_thirdRoot
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

// org.apache.commons.math3.complex.ComplexTest::testNthRoot_normal_fourthRoot
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

// org.apache.commons.math3.complex.ComplexTest::testNthRoot_cornercase_thirdRoot_imaginaryPartEmpty
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

// org.apache.commons.math3.complex.ComplexTest::testNthRoot_cornercase_thirdRoot_realPartZero
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

// org.apache.commons.math3.complex.ComplexTest::testNthRoot_cornercase_NAN_Inf
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

// org.apache.commons.math3.complex.ComplexTest::testGetArgument
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

// org.apache.commons.math3.complex.ComplexTest::testGetArgumentInf
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

// org.apache.commons.math3.complex.ComplexTest::testGetArgumentNaN
    public void testGetArgumentNaN() {
        Assert.assertTrue(Double.isNaN(nanZero.getArgument()));
        Assert.assertTrue(Double.isNaN(zeroNaN.getArgument()));
        Assert.assertTrue(Double.isNaN(Complex.NaN.getArgument()));
    }

// org.apache.commons.math3.complex.ComplexTest::testSerial
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

// org.apache.commons.math3.complex.ComplexUtilsTest::testPolar2Complex
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

// org.apache.commons.math3.complex.ComplexUtilsTest::testPolar2ComplexIllegalModulus
    public void testPolar2ComplexIllegalModulus() {
        ComplexUtils.polar2Complex(-1, 0);
    }

// org.apache.commons.math3.complex.ComplexUtilsTest::testPolar2ComplexNaN
    public void testPolar2ComplexNaN() {
        TestUtils.assertSame(Complex.NaN, ComplexUtils.polar2Complex(nan, 1));
        TestUtils.assertSame(Complex.NaN, ComplexUtils.polar2Complex(1, nan));
        TestUtils.assertSame(Complex.NaN,
                ComplexUtils.polar2Complex(nan, nan));
    }

// org.apache.commons.math3.complex.ComplexUtilsTest::testPolar2ComplexInf
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

// org.apache.commons.math3.complex.ComplexUtilsTest::testConvertToComplex
    public void testConvertToComplex() {
        final double[] real = new double[] { negInf, -123.45, 0, 1, 234.56, pi, inf };
        final Complex[] complex = ComplexUtils.convertToComplex(real);

        for (int i = 0; i < real.length; i++) {
            Assert.assertEquals(real[i], complex[i].getReal(), 0d);
        }
    }

// org.apache.commons.math3.linear.EigenDecompositionTest::testDimension1
    public void testDimension1() {
        RealMatrix matrix =
            MatrixUtils.createRealMatrix(new double[][] { { 1.5 } });
        EigenDecomposition ed;
        ed = new EigenDecomposition(matrix);
        Assert.assertEquals(1.5, ed.getRealEigenvalue(0), 1.0e-15);
    }

// org.apache.commons.math3.linear.EigenDecompositionTest::testDimension2
    public void testDimension2() {
        RealMatrix matrix =
            MatrixUtils.createRealMatrix(new double[][] {
                    { 59.0, 12.0 },
                    { 12.0, 66.0 }
            });
        EigenDecomposition ed;
        ed = new EigenDecomposition(matrix);
        Assert.assertEquals(75.0, ed.getRealEigenvalue(0), 1.0e-15);
        Assert.assertEquals(50.0, ed.getRealEigenvalue(1), 1.0e-15);
    }

// org.apache.commons.math3.linear.EigenDecompositionTest::testDimension3
    public void testDimension3() {
        RealMatrix matrix =
            MatrixUtils.createRealMatrix(new double[][] {
                                   {  39632.0, -4824.0, -16560.0 },
                                   {  -4824.0,  8693.0,   7920.0 },
                                   { -16560.0,  7920.0,  17300.0 }
                               });
        EigenDecomposition ed;
        ed = new EigenDecomposition(matrix);
        Assert.assertEquals(50000.0, ed.getRealEigenvalue(0), 3.0e-11);
        Assert.assertEquals(12500.0, ed.getRealEigenvalue(1), 3.0e-11);
        Assert.assertEquals( 3125.0, ed.getRealEigenvalue(2), 3.0e-11);
    }

// org.apache.commons.math3.linear.EigenDecompositionTest::testDimension3MultipleRoot
    public void testDimension3MultipleRoot() {
        RealMatrix matrix =
            MatrixUtils.createRealMatrix(new double[][] {
                    {  5,   10,   15 },
                    { 10,   20,   30 },
                    { 15,   30,   45 }
            });
        EigenDecomposition ed;
        ed = new EigenDecomposition(matrix);
        Assert.assertEquals(70.0, ed.getRealEigenvalue(0), 3.0e-11);
        Assert.assertEquals(0.0,  ed.getRealEigenvalue(1), 3.0e-11);
        Assert.assertEquals(0.0,  ed.getRealEigenvalue(2), 3.0e-11);
    }

// org.apache.commons.math3.linear.EigenDecompositionTest::testDimension4WithSplit
    public void testDimension4WithSplit() {
        RealMatrix matrix =
            MatrixUtils.createRealMatrix(new double[][] {
                                   {  0.784, -0.288,  0.000,  0.000 },
                                   { -0.288,  0.616,  0.000,  0.000 },
                                   {  0.000,  0.000,  0.164, -0.048 },
                                   {  0.000,  0.000, -0.048,  0.136 }
                               });
        EigenDecomposition ed;
        ed = new EigenDecomposition(matrix);
        Assert.assertEquals(1.0, ed.getRealEigenvalue(0), 1.0e-15);
        Assert.assertEquals(0.4, ed.getRealEigenvalue(1), 1.0e-15);
        Assert.assertEquals(0.2, ed.getRealEigenvalue(2), 1.0e-15);
        Assert.assertEquals(0.1, ed.getRealEigenvalue(3), 1.0e-15);
    }

// org.apache.commons.math3.linear.EigenDecompositionTest::testDimension4WithoutSplit
    public void testDimension4WithoutSplit() {
        RealMatrix matrix =
            MatrixUtils.createRealMatrix(new double[][] {
                                   {  0.5608, -0.2016,  0.1152, -0.2976 },
                                   { -0.2016,  0.4432, -0.2304,  0.1152 },
                                   {  0.1152, -0.2304,  0.3088, -0.1344 },
                                   { -0.2976,  0.1152, -0.1344,  0.3872 }
                               });
        EigenDecomposition ed;
        ed = new EigenDecomposition(matrix);
        Assert.assertEquals(1.0, ed.getRealEigenvalue(0), 1.0e-15);
        Assert.assertEquals(0.4, ed.getRealEigenvalue(1), 1.0e-15);
        Assert.assertEquals(0.2, ed.getRealEigenvalue(2), 1.0e-15);
        Assert.assertEquals(0.1, ed.getRealEigenvalue(3), 1.0e-15);
    }

// org.apache.commons.math3.linear.EigenDecompositionTest::testMath308
    public void testMath308() {

        double[] mainTridiagonal = {
            22.330154644539597, 46.65485522478641, 17.393672330044705, 54.46687435351116, 80.17800767709437
        };
        double[] secondaryTridiagonal = {
            13.04450406501361, -5.977590941539671, 2.9040909856707517, 7.1570352792841225
        };

        
        
        double[] refEigenValues = {
            82.044413207204002, 53.456697699894512, 52.536278520113882, 18.847969733754262, 14.138204224043099
        };
        RealVector[] refEigenVectors = {
            new ArrayRealVector(new double[] { -0.000462690386766, -0.002118073109055,  0.011530080757413,  0.252322434584915,  0.967572088232592 }),
            new ArrayRealVector(new double[] {  0.314647769490148,  0.750806415553905, -0.167700312025760, -0.537092972407375,  0.143854968127780 }),
            new ArrayRealVector(new double[] {  0.222368839324646,  0.514921891363332, -0.021377019336614,  0.801196801016305, -0.207446991247740 }),
            new ArrayRealVector(new double[] { -0.713933751051495,  0.190582113553930, -0.671410443368332,  0.056056055955050, -0.006541576993581 }),
            new ArrayRealVector(new double[] { -0.584677060845929,  0.367177264979103,  0.721453187784497, -0.052971054621812,  0.005740715188257 })
        };

        EigenDecomposition decomposition;
        decomposition = new EigenDecomposition(mainTridiagonal, secondaryTridiagonal);

        double[] eigenValues = decomposition.getRealEigenvalues();
        for (int i = 0; i < refEigenValues.length; ++i) {
            Assert.assertEquals(refEigenValues[i], eigenValues[i], 1.0e-5);
            Assert.assertEquals(0, refEigenVectors[i].subtract(decomposition.getEigenvector(i)).getNorm(), 2.0e-7);
        }

    }

// org.apache.commons.math3.linear.EigenDecompositionTest::testMathpbx02
    public void testMathpbx02() {

        double[] mainTridiagonal = {
              7484.860960227216, 18405.28129035345, 13855.225609560746,
             10016.708722343366, 559.8117399576674, 6750.190788301587,
                71.21428769782159
        };
        double[] secondaryTridiagonal = {
             -4175.088570476366,1975.7955858241994,5193.178422374075,
              1995.286659169179,75.34535882933804,-234.0808002076056
        };

        
        
        double[] refEigenValues = {
                20654.744890306974412,16828.208208485466457,
                6893.155912634994820,6757.083016675340332,
                5887.799885688558788,64.309089923240379,
                57.992628792736340
        };
        RealVector[] refEigenVectors = {
                new ArrayRealVector(new double[] {-0.270356342026904, 0.852811091326997, 0.399639490702077, 0.198794657813990, 0.019739323307666, 0.000106983022327, -0.000001216636321}),
                new ArrayRealVector(new double[] {0.179995273578326,-0.402807848153042,0.701870993525734,0.555058211014888,0.068079148898236,0.000509139115227,-0.000007112235617}),
                new ArrayRealVector(new double[] {-0.399582721284727,-0.056629954519333,-0.514406488522827,0.711168164518580,0.225548081276367,0.125943999652923,-0.004321507456014}),
                new ArrayRealVector(new double[] {0.058515721572821,0.010200130057739,0.063516274916536,-0.090696087449378,-0.017148420432597,0.991318870265707,-0.034707338554096}),
                new ArrayRealVector(new double[] {0.855205995537564,0.327134656629775,-0.265382397060548,0.282690729026706,0.105736068025572,-0.009138126622039,0.000367751821196}),
                new ArrayRealVector(new double[] {-0.002913069901144,-0.005177515777101,0.041906334478672,-0.109315918416258,0.436192305456741,0.026307315639535,0.891797507436344}),
                new ArrayRealVector(new double[] {-0.005738311176435,-0.010207611670378,0.082662420517928,-0.215733886094368,0.861606487840411,-0.025478530652759,-0.451080697503958})
        };

        
        EigenDecomposition decomposition;
        decomposition = new EigenDecomposition(mainTridiagonal, secondaryTridiagonal);

        double[] eigenValues = decomposition.getRealEigenvalues();
        for (int i = 0; i < refEigenValues.length; ++i) {
            Assert.assertEquals(refEigenValues[i], eigenValues[i], 1.0e-3);
            if (refEigenVectors[i].dotProduct(decomposition.getEigenvector(i)) < 0) {
                Assert.assertEquals(0, refEigenVectors[i].add(decomposition.getEigenvector(i)).getNorm(), 1.0e-5);
            } else {
                Assert.assertEquals(0, refEigenVectors[i].subtract(decomposition.getEigenvector(i)).getNorm(), 1.0e-5);
            }
        }

    }

// org.apache.commons.math3.linear.EigenDecompositionTest::testMathpbx03
    public void testMathpbx03() {

        double[] mainTridiagonal = {
            1809.0978259647177,3395.4763425956166,1832.1894584712693,3804.364873592377,
            806.0482458637571,2403.656427234185,28.48691431556015
        };
        double[] secondaryTridiagonal = {
            -656.8932064545833,-469.30804108920734,-1021.7714889369421,
            -1152.540497328983,-939.9765163817368,-12.885877015422391
        };

        
        
        double[] refEigenValues = {
            4603.121913685183245,3691.195818048970978,2743.442955402465032,1657.596442107321764,
            1336.797819095331306,30.129865209677519,17.035352085224986
        };

        RealVector[] refEigenVectors = {
            new ArrayRealVector(new double[] {-0.036249830202337,0.154184732411519,-0.346016328392363,0.867540105133093,-0.294483395433451,0.125854235969548,-0.000354507444044}),
            new ArrayRealVector(new double[] {-0.318654191697157,0.912992309960507,-0.129270874079777,-0.184150038178035,0.096521712579439,-0.070468788536461,0.000247918177736}),
            new ArrayRealVector(new double[] {-0.051394668681147,0.073102235876933,0.173502042943743,-0.188311980310942,-0.327158794289386,0.905206581432676,-0.004296342252659}),
            new ArrayRealVector(new double[] {0.838150199198361,0.193305209055716,-0.457341242126146,-0.166933875895419,0.094512811358535,0.119062381338757,-0.000941755685226}),
            new ArrayRealVector(new double[] {0.438071395458547,0.314969169786246,0.768480630802146,0.227919171600705,-0.193317045298647,-0.170305467485594,0.001677380536009}),
            new ArrayRealVector(new double[] {-0.003726503878741,-0.010091946369146,-0.067152015137611,-0.113798146542187,-0.313123000097908,-0.118940107954918,0.932862311396062}),
            new ArrayRealVector(new double[] {0.009373003194332,0.025570377559400,0.170955836081348,0.291954519805750,0.807824267665706,0.320108347088646,0.360202112392266}),
        };

        
        EigenDecomposition decomposition;
        decomposition = new EigenDecomposition(mainTridiagonal, secondaryTridiagonal);

        double[] eigenValues = decomposition.getRealEigenvalues();
        for (int i = 0; i < refEigenValues.length; ++i) {
            Assert.assertEquals(refEigenValues[i], eigenValues[i], 1.0e-4);
            if (refEigenVectors[i].dotProduct(decomposition.getEigenvector(i)) < 0) {
                Assert.assertEquals(0, refEigenVectors[i].add(decomposition.getEigenvector(i)).getNorm(), 1.0e-5);
            } else {
                Assert.assertEquals(0, refEigenVectors[i].subtract(decomposition.getEigenvector(i)).getNorm(), 1.0e-5);
            }
        }

    }

// org.apache.commons.math3.linear.EigenDecompositionTest::testTridiagonal
    public void testTridiagonal() {
        Random r = new Random(4366663527842l);
        double[] ref = new double[30];
        for (int i = 0; i < ref.length; ++i) {
            if (i < 5) {
                ref[i] = 2 * r.nextDouble() - 1;
            } else {
                ref[i] = 0.0001 * r.nextDouble() + 6;
            }
        }
        Arrays.sort(ref);
        TriDiagonalTransformer t =
            new TriDiagonalTransformer(createTestMatrix(r, ref));
        EigenDecomposition ed;
        ed = new EigenDecomposition(t.getMainDiagonalRef(), t.getSecondaryDiagonalRef());
        double[] eigenValues = ed.getRealEigenvalues();
        Assert.assertEquals(ref.length, eigenValues.length);
        for (int i = 0; i < ref.length; ++i) {
            Assert.assertEquals(ref[ref.length - i - 1], eigenValues[i], 2.0e-14);
        }

    }

// org.apache.commons.math3.linear.EigenDecompositionTest::testDimensions
    public void testDimensions() {
        final int m = matrix.getRowDimension();
        EigenDecomposition ed;
        ed = new EigenDecomposition(matrix);
        Assert.assertEquals(m, ed.getV().getRowDimension());
        Assert.assertEquals(m, ed.getV().getColumnDimension());
        Assert.assertEquals(m, ed.getD().getColumnDimension());
        Assert.assertEquals(m, ed.getD().getColumnDimension());
        Assert.assertEquals(m, ed.getVT().getRowDimension());
        Assert.assertEquals(m, ed.getVT().getColumnDimension());
    }

// org.apache.commons.math3.linear.EigenDecompositionTest::testEigenvalues
    public void testEigenvalues() {
        EigenDecomposition ed;
        ed = new EigenDecomposition(matrix);
        double[] eigenValues = ed.getRealEigenvalues();
        Assert.assertEquals(refValues.length, eigenValues.length);
        for (int i = 0; i < refValues.length; ++i) {
            Assert.assertEquals(refValues[i], eigenValues[i], 3.0e-15);
        }
    }

// org.apache.commons.math3.linear.EigenDecompositionTest::testBigMatrix
    public void testBigMatrix() {
        Random r = new Random(17748333525117l);
        double[] bigValues = new double[200];
        for (int i = 0; i < bigValues.length; ++i) {
            bigValues[i] = 2 * r.nextDouble() - 1;
        }
        Arrays.sort(bigValues);
        EigenDecomposition ed;
        ed = new EigenDecomposition(createTestMatrix(r, bigValues));
        double[] eigenValues = ed.getRealEigenvalues();
        Assert.assertEquals(bigValues.length, eigenValues.length);
        for (int i = 0; i < bigValues.length; ++i) {
            Assert.assertEquals(bigValues[bigValues.length - i - 1], eigenValues[i], 2.0e-14);
        }
    }

// org.apache.commons.math3.linear.EigenDecompositionTest::testSymmetric
    public void testSymmetric() {
        RealMatrix symmetric = MatrixUtils.createRealMatrix(new double[][] {
                {4, 1, 1},
                {1, 2, 3},
                {1, 3, 6}
        });

        EigenDecomposition ed;
        ed = new EigenDecomposition(symmetric);
        
        RealMatrix d = ed.getD();
        RealMatrix v = ed.getV();
        RealMatrix vT = ed.getVT();

        double norm = v.multiply(d).multiply(vT).subtract(symmetric).getNorm();
        Assert.assertEquals(0, norm, 6.0e-13);
    }

// org.apache.commons.math3.linear.EigenDecompositionTest::testSquareRoot
    public void testSquareRoot() {
        final double[][] data = {
            { 33, 24,  7 },
            { 24, 57, 11 },
            {  7, 11,  9 }
        };

        final EigenDecomposition dec = new EigenDecomposition(MatrixUtils.createRealMatrix(data));
        final RealMatrix sqrtM = dec.getSquareRoot();

        
        final RealMatrix m = sqrtM.multiply(sqrtM);

        final int dim = data.length;
        for (int r = 0; r < dim; r++) {
            for (int c = 0; c < dim; c++) {
                Assert.assertEquals("m[" + r + "][" + c + "]",
                                    data[r][c], m.getEntry(r, c), 1e-13);
            }
        }
    }

// org.apache.commons.math3.linear.EigenDecompositionTest::testSquareRootNonSymmetric
    public void testSquareRootNonSymmetric() {
        final double[][] data = {
            { 1,  2, 4 },
            { 2,  3, 5 },
            { 11, 5, 9 }
        };

        final EigenDecomposition dec = new EigenDecomposition(MatrixUtils.createRealMatrix(data));
        final RealMatrix sqrtM = dec.getSquareRoot();
    }

// org.apache.commons.math3.linear.EigenDecompositionTest::testSquareRootNonPositiveDefinite
    public void testSquareRootNonPositiveDefinite() {
        final double[][] data = {
            { 1, 2,  4 },
            { 2, 3,  5 },
            { 4, 5, -9 }
        };

        final EigenDecomposition dec = new EigenDecomposition(MatrixUtils.createRealMatrix(data));
        final RealMatrix sqrtM = dec.getSquareRoot();
    }

// org.apache.commons.math3.linear.EigenDecompositionTest::testUnsymmetric
    public void testUnsymmetric() {
        
        double[][] vData = { { -1.0, 1.0, -1.0, 1.0 },
                             { -8.0, 4.0, -2.0, 1.0 },
                             { 27.0, 9.0,  3.0, 1.0 },
                             { 64.0, 16.0, 4.0, 1.0 } };
        checkUnsymmetricMatrix(MatrixUtils.createRealMatrix(vData));
      
        RealMatrix randMatrix = MatrixUtils.createRealMatrix(new double[][] {
                {0,  1,     0,     0},
                {1,  0,     2.e-7, 0},
                {0, -2.e-7, 0,     1},
                {0,  0,     1,     0}
        });
        checkUnsymmetricMatrix(randMatrix);

        
        double[][] randData2 = {
                {  0.680, -0.3300, -0.2700, -0.717, -0.687,  0.0259 },
                { -0.211,  0.5360,  0.0268,  0.214, -0.198,  0.6780 },
                {  0.566, -0.4440,  0.9040, -0.967, -0.740,  0.2250 },
                {  0.597,  0.1080,  0.8320, -0.514, -0.782, -0.4080 },
                {  0.823, -0.0452,  0.2710, -0.726,  0.998,  0.2750 },
                { -0.605,  0.2580,  0.4350,  0.608, -0.563,  0.0486 }
        };
        checkUnsymmetricMatrix(MatrixUtils.createRealMatrix(randData2));
    }

// org.apache.commons.math3.linear.EigenDecompositionTest::testRandomUnsymmetricMatrix
    public void testRandomUnsymmetricMatrix() {
        for (int run = 0; run < 100; run++) {
            Random r = new Random(System.currentTimeMillis());

            
            int size = r.nextInt(20) + 4;

            double[][] data = new double[size][size];
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    data[i][j] = r.nextInt(100);
                }
            }

            RealMatrix m = MatrixUtils.createRealMatrix(data);
            checkUnsymmetricMatrix(m);
        }        
    }

// org.apache.commons.math3.linear.EigenDecompositionTest::testNormalDistributionUnsymmetricMatrix
    public void testNormalDistributionUnsymmetricMatrix() {
        for (int run = 0; run < 100; run++) {
            Random r = new Random(System.currentTimeMillis());
            NormalDistribution dist = new NormalDistribution(0.0, r.nextDouble() * 5);

            
            int size = r.nextInt(20) + 4;

            double[][] data = new double[size][size];
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    data[i][j] = dist.sample();
                }
            }

            RealMatrix m = MatrixUtils.createRealMatrix(data);
            checkUnsymmetricMatrix(m);
        }
    }

// org.apache.commons.math3.linear.EigenDecompositionTest::testMath848
    public void testMath848() {
        double[][] data = {
                { 0.1849449280, -0.0646971046,  0.0774755812, -0.0969651755, -0.0692648806,  0.3282344352, -0.0177423074,  0.2063136340},
                {-0.0742700134, -0.0289063030, -0.0017269460, -0.0375550146, -0.0487737922, -0.2616837868, -0.0821201295, -0.2530000167},
                { 0.2549910127,  0.0995733692, -0.0009718388,  0.0149282808,  0.1791878897, -0.0823182816,  0.0582629256,  0.3219545182},
                {-0.0694747557, -0.1880649148, -0.2740630911,  0.0720096468, -0.1800836914, -0.3518996425,  0.2486747833,  0.6257938167},
                { 0.0536360918, -0.1339297778,  0.2241579764, -0.0195327484, -0.0054103808,  0.0347564518,  0.5120802482, -0.0329902864},
                {-0.5933332356, -0.2488721082,  0.2357173629,  0.0177285473,  0.0856630593, -0.3567126300, -0.1600668126, -0.1010899621},
                {-0.0514349819, -0.0854319435,  0.1125050061,  0.0063453560, -0.2250000688, -0.2209343090,  0.1964623477, -0.1512329924},
                { 0.0197395947, -0.1997170581, -0.1425959019, -0.2749477910, -0.0969467073,  0.0603688520, -0.2826905192,  0.1794315473}};
        RealMatrix m = MatrixUtils.createRealMatrix(data);
        checkUnsymmetricMatrix(m);
    }

// org.apache.commons.math3.linear.EigenDecompositionTest::testEigenvectors
    public void testEigenvectors() {
        EigenDecomposition ed;
        ed = new EigenDecomposition(matrix);
        for (int i = 0; i < matrix.getRowDimension(); ++i) {
            double lambda = ed.getRealEigenvalue(i);
            RealVector v  = ed.getEigenvector(i);
            RealVector mV = matrix.operate(v);
            Assert.assertEquals(0, mV.subtract(v.mapMultiplyToSelf(lambda)).getNorm(), 1.0e-13);
        }
    }

// org.apache.commons.math3.linear.EigenDecompositionTest::testAEqualVDVt
    public void testAEqualVDVt() {
        EigenDecomposition ed;
        ed = new EigenDecomposition(matrix);
        RealMatrix v  = ed.getV();
        RealMatrix d  = ed.getD();
        RealMatrix vT = ed.getVT();
        double norm = v.multiply(d).multiply(vT).subtract(matrix).getNorm();
        Assert.assertEquals(0, norm, 6.0e-13);
    }

// org.apache.commons.math3.linear.EigenDecompositionTest::testVOrthogonal
    public void testVOrthogonal() {
        RealMatrix v = new EigenDecomposition(matrix).getV();
        RealMatrix vTv = v.transpose().multiply(v);
        RealMatrix id  = MatrixUtils.createRealIdentityMatrix(vTv.getRowDimension());
        Assert.assertEquals(0, vTv.subtract(id).getNorm(), 2.0e-13);
    }

// org.apache.commons.math3.linear.EigenDecompositionTest::testDiagonal
    public void testDiagonal() {
        double[] diagonal = new double[] { -3.0, -2.0, 2.0, 5.0 };
        RealMatrix m = MatrixUtils.createRealDiagonalMatrix(diagonal);
        EigenDecomposition ed;
        ed = new EigenDecomposition(m);
        Assert.assertEquals(diagonal[0], ed.getRealEigenvalue(3), 2.0e-15);
        Assert.assertEquals(diagonal[1], ed.getRealEigenvalue(2), 2.0e-15);
        Assert.assertEquals(diagonal[2], ed.getRealEigenvalue(1), 2.0e-15);
        Assert.assertEquals(diagonal[3], ed.getRealEigenvalue(0), 2.0e-15);
    }

// org.apache.commons.math3.linear.EigenDecompositionTest::testRepeatedEigenvalue
    public void testRepeatedEigenvalue() {
        RealMatrix repeated = MatrixUtils.createRealMatrix(new double[][] {
                {3,  2,  4},
                {2,  0,  2},
                {4,  2,  3}
        });
        EigenDecomposition ed;
        ed = new EigenDecomposition(repeated);
        checkEigenValues((new double[] {8, -1, -1}), ed, 1E-12);
        checkEigenVector((new double[] {2, 1, 2}), ed, 1E-12);
    }

// org.apache.commons.math3.linear.EigenDecompositionTest::testDistinctEigenvalues
    public void testDistinctEigenvalues() {
        RealMatrix distinct = MatrixUtils.createRealMatrix(new double[][] {
                {3, 1, -4},
                {1, 3, -4},
                {-4, -4, 8}
        });
        EigenDecomposition ed;
        ed = new EigenDecomposition(distinct);
        checkEigenValues((new double[] {2, 0, 12}), ed, 1E-12);
        checkEigenVector((new double[] {1, -1, 0}), ed, 1E-12);
        checkEigenVector((new double[] {1, 1, 1}), ed, 1E-12);
        checkEigenVector((new double[] {-1, -1, 2}), ed, 1E-12);
    }

// org.apache.commons.math3.linear.EigenDecompositionTest::testZeroDivide
    public void testZeroDivide() {
        RealMatrix indefinite = MatrixUtils.createRealMatrix(new double [][] {
                { 0.0, 1.0, -1.0 },
                { 1.0, 1.0, 0.0 },
                { -1.0,0.0, 1.0 }
        });
        EigenDecomposition ed;
        ed = new EigenDecomposition(indefinite);
        checkEigenValues((new double[] {2, 1, -1}), ed, 1E-12);
        double isqrt3 = 1/FastMath.sqrt(3.0);
        checkEigenVector((new double[] {isqrt3,isqrt3,-isqrt3}), ed, 1E-12);
        double isqrt2 = 1/FastMath.sqrt(2.0);
        checkEigenVector((new double[] {0.0,-isqrt2,-isqrt2}), ed, 1E-12);
        double isqrt6 = 1/FastMath.sqrt(6.0);
        checkEigenVector((new double[] {2*isqrt6,-isqrt6,isqrt6}), ed, 1E-12);
    }

// org.apache.commons.math3.transform.FastCosineTransformerTest::testAdHocData
    public void testAdHocData() {
        FastCosineTransformer transformer;
        transformer = new FastCosineTransformer(DctNormalization.STANDARD_DCT_I);
        double result[], tolerance = 1E-12;

        double x[] = {
            0.0, 1.0, 4.0, 9.0, 16.0, 25.0, 36.0, 49.0, 64.0
        };
        double y[] =
            {
                172.0, -105.096569476353, 27.3137084989848, -12.9593152353742,
                8.0, -5.78585076868676, 4.68629150101524, -4.15826451958632,
                4.0
            };

        result = transformer.transform(x, TransformType.FORWARD);
        for (int i = 0; i < result.length; i++) {
            Assert.assertEquals(y[i], result[i], tolerance);
        }

        result = transformer.transform(y, TransformType.INVERSE);
        for (int i = 0; i < result.length; i++) {
            Assert.assertEquals(x[i], result[i], tolerance);
        }

        TransformUtils.scaleArray(x, FastMath.sqrt(0.5 * (x.length - 1)));

        transformer = new FastCosineTransformer(DctNormalization.ORTHOGONAL_DCT_I);
        result = transformer.transform(y, TransformType.FORWARD);
        for (int i = 0; i < result.length; i++) {
            Assert.assertEquals(x[i], result[i], tolerance);
        }

        result = transformer.transform(x, TransformType.INVERSE);
        for (int i = 0; i < result.length; i++) {
            Assert.assertEquals(y[i], result[i], tolerance);
        }
    }

// org.apache.commons.math3.transform.FastCosineTransformerTest::testParameters
    public void testParameters()
        throws Exception {
        UnivariateFunction f = new Sin();
        FastCosineTransformer transformer;
        transformer = new FastCosineTransformer(DctNormalization.STANDARD_DCT_I);

        try {
            
            transformer.transform(f, 1, -1, 65, TransformType.FORWARD);
            Assert.fail("Expecting IllegalArgumentException - bad interval");
        } catch (IllegalArgumentException ex) {
            
        }
        try {
            
            transformer.transform(f, -1, 1, 1, TransformType.FORWARD);
            Assert
                .fail("Expecting IllegalArgumentException - bad samples number");
        } catch (IllegalArgumentException ex) {
            
        }
        try {
            
            transformer.transform(f, -1, 1, 64, TransformType.FORWARD);
            Assert
                .fail("Expecting IllegalArgumentException - bad samples number");
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math3.transform.FastCosineTransformerTest::testSinFunction
    public void testSinFunction() {
        UnivariateFunction f = new Sin();
        FastCosineTransformer transformer;
        transformer = new FastCosineTransformer(DctNormalization.STANDARD_DCT_I);
        double min, max, result[], tolerance = 1E-12;
        int N = 9;

        double expected[] =
            {
                0.0, 3.26197262739567, 0.0, -2.17958042710327, 0.0,
                -0.648846697642915, 0.0, -0.433545502649478, 0.0
            };
        min = 0.0;
        max = 2.0 * FastMath.PI * N / (N - 1);
        result = transformer.transform(f, min, max, N, TransformType.FORWARD);
        for (int i = 0; i < N; i++) {
            Assert.assertEquals(expected[i], result[i], tolerance);
        }

        min = -FastMath.PI;
        max = FastMath.PI * (N + 1) / (N - 1);
        result = transformer.transform(f, min, max, N, TransformType.FORWARD);
        for (int i = 0; i < N; i++) {
            Assert.assertEquals(-expected[i], result[i], tolerance);
        }
    }

// org.apache.commons.math3.transform.FastFourierTransformerTest::testTransformComplexSizeNotAPowerOfTwo
    public void testTransformComplexSizeNotAPowerOfTwo() {
        final int n = 127;
        final Complex[] x = createComplexData(n);
        final DftNormalization[] norm;
        norm = DftNormalization.values();
        final TransformType[] type;
        type = TransformType.values();
        for (int i = 0; i < norm.length; i++) {
            for (int j = 0; j < type.length; j++) {
                final FastFourierTransformer fft;
                fft = new FastFourierTransformer(norm[i]);
                try {
                    fft.transform(x, type[j]);
                    Assert.fail(norm[i] + ", " + type[j] +
                        ": MathIllegalArgumentException was expected");
                } catch (MathIllegalArgumentException e) {
                    
                }
            }
        }
    }

// org.apache.commons.math3.transform.FastFourierTransformerTest::testTransformRealSizeNotAPowerOfTwo
    public void testTransformRealSizeNotAPowerOfTwo() {
        final int n = 127;
        final double[] x = createRealData(n);
        final DftNormalization[] norm;
        norm = DftNormalization.values();
        final TransformType[] type;
        type = TransformType.values();
        for (int i = 0; i < norm.length; i++) {
            for (int j = 0; j < type.length; j++) {
                final FastFourierTransformer fft;
                fft = new FastFourierTransformer(norm[i]);
                try {
                    fft.transform(x, type[j]);
                    Assert.fail(norm[i] + ", " + type[j] +
                        ": MathIllegalArgumentException was expected");
                } catch (MathIllegalArgumentException e) {
                    
                }
            }
        }
    }

// org.apache.commons.math3.transform.FastFourierTransformerTest::testTransformFunctionSizeNotAPowerOfTwo
    public void testTransformFunctionSizeNotAPowerOfTwo() {
        final int n = 127;
        final UnivariateFunction f = new Sin();
        final DftNormalization[] norm;
        norm = DftNormalization.values();
        final TransformType[] type;
        type = TransformType.values();
        for (int i = 0; i < norm.length; i++) {
            for (int j = 0; j < type.length; j++) {
                final FastFourierTransformer fft;
                fft = new FastFourierTransformer(norm[i]);
                try {
                    fft.transform(f, 0.0, Math.PI, n, type[j]);
                    Assert.fail(norm[i] + ", " + type[j] +
                        ": MathIllegalArgumentException was expected");
                } catch (MathIllegalArgumentException e) {
                    
                }
            }
        }
    }

// org.apache.commons.math3.transform.FastFourierTransformerTest::testTransformFunctionNotStrictlyPositiveNumberOfSamples
    public void testTransformFunctionNotStrictlyPositiveNumberOfSamples() {
        final int n = -128;
        final UnivariateFunction f = new Sin();
        final DftNormalization[] norm;
        norm = DftNormalization.values();
        final TransformType[] type;
        type = TransformType.values();
        for (int i = 0; i < norm.length; i++) {
            for (int j = 0; j < type.length; j++) {
                final FastFourierTransformer fft;
                fft = new FastFourierTransformer(norm[i]);
                try {
                    fft.transform(f, 0.0, Math.PI, n, type[j]);
                    fft.transform(f, 0.0, Math.PI, n, type[j]);
                    Assert.fail(norm[i] + ", " + type[j] +
                        ": NotStrictlyPositiveException was expected");
                } catch (NotStrictlyPositiveException e) {
                    
                }
            }
        }
    }

// org.apache.commons.math3.transform.FastFourierTransformerTest::testTransformFunctionInvalidBounds
    public void testTransformFunctionInvalidBounds() {
        final int n = 128;
        final UnivariateFunction f = new Sin();
        final DftNormalization[] norm;
        norm = DftNormalization.values();
        final TransformType[] type;
        type = TransformType.values();
        for (int i = 0; i < norm.length; i++) {
            for (int j = 0; j < type.length; j++) {
                final FastFourierTransformer fft;
                fft = new FastFourierTransformer(norm[i]);
                try {
                    fft.transform(f, Math.PI, 0.0, n, type[j]);
                    Assert.fail(norm[i] + ", " + type[j] +
                        ": NumberIsTooLargeException was expected");
                } catch (NumberIsTooLargeException e) {
                    
                }
            }
        }
    }

// org.apache.commons.math3.transform.FastFourierTransformerTest::testTransformComplex
    public void testTransformComplex() {
        final DftNormalization[] norm;
        norm = DftNormalization.values();
        final TransformType[] type;
        type = TransformType.values();
        for (int i = 0; i < norm.length; i++) {
            for (int j = 0; j < type.length; j++) {
                doTestTransformComplex(2, 1.0E-15, norm[i], type[j]);
                doTestTransformComplex(4, 1.0E-14, norm[i], type[j]);
                doTestTransformComplex(8, 1.0E-14, norm[i], type[j]);
                doTestTransformComplex(16, 1.0E-13, norm[i], type[j]);
                doTestTransformComplex(32, 1.0E-13, norm[i], type[j]);
                doTestTransformComplex(64, 1.0E-12, norm[i], type[j]);
                doTestTransformComplex(128, 1.0E-12, norm[i], type[j]);
            }
        }
    }

// org.apache.commons.math3.transform.FastFourierTransformerTest::testStandardTransformReal
    public void testStandardTransformReal() {
        final DftNormalization[] norm;
        norm = DftNormalization.values();
        final TransformType[] type;
        type = TransformType.values();
        for (int i = 0; i < norm.length; i++) {
            for (int j = 0; j < type.length; j++) {
                doTestTransformReal(2, 1.0E-15, norm[i], type[j]);
                doTestTransformReal(4, 1.0E-14, norm[i], type[j]);
                doTestTransformReal(8, 1.0E-14, norm[i], type[j]);
                doTestTransformReal(16, 1.0E-13, norm[i], type[j]);
                doTestTransformReal(32, 1.0E-13, norm[i], type[j]);
                doTestTransformReal(64, 1.0E-13, norm[i], type[j]);
                doTestTransformReal(128, 1.0E-11, norm[i], type[j]);
            }
        }
    }

// org.apache.commons.math3.transform.FastFourierTransformerTest::testStandardTransformFunction
    public void testStandardTransformFunction() {
        final UnivariateFunction f = new Sinc();
        final double min = -FastMath.PI;
        final double max = FastMath.PI;
        final DftNormalization[] norm;
        norm = DftNormalization.values();
        final TransformType[] type;
        type = TransformType.values();
        for (int i = 0; i < norm.length; i++) {
            for (int j = 0; j < type.length; j++) {
                doTestTransformFunction(f, min, max, 2, 1.0E-15, norm[i], type[j]);
                doTestTransformFunction(f, min, max, 4, 1.0E-14, norm[i], type[j]);
                doTestTransformFunction(f, min, max, 8, 1.0E-14, norm[i], type[j]);
                doTestTransformFunction(f, min, max, 16, 1.0E-13, norm[i], type[j]);
                doTestTransformFunction(f, min, max, 32, 1.0E-13, norm[i], type[j]);
                doTestTransformFunction(f, min, max, 64, 1.0E-12, norm[i], type[j]);
                doTestTransformFunction(f, min, max, 128, 1.0E-11, norm[i], type[j]);
            }
        }
    }

// org.apache.commons.math3.transform.FastFourierTransformerTest::testAdHocData
    public void testAdHocData() {
        FastFourierTransformer transformer;
        transformer = new FastFourierTransformer(DftNormalization.STANDARD);
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

        result = transformer.transform(x, TransformType.FORWARD);
        for (int i = 0; i < result.length; i++) {
            Assert.assertEquals(y[i].getReal(), result[i].getReal(), tolerance);
            Assert.assertEquals(y[i].getImaginary(), result[i].getImaginary(), tolerance);
        }

        result = transformer.transform(y, TransformType.INVERSE);
        for (int i = 0; i < result.length; i++) {
            Assert.assertEquals(x[i], result[i].getReal(), tolerance);
            Assert.assertEquals(0.0, result[i].getImaginary(), tolerance);
        }

        double x2[] = {10.4, 21.6, 40.8, 13.6, 23.2, 32.8, 13.6, 19.2};
        TransformUtils.scaleArray(x2, 1.0 / FastMath.sqrt(x2.length));
        Complex y2[] = y;

        transformer = new FastFourierTransformer(DftNormalization.UNITARY);
        result = transformer.transform(y2, TransformType.FORWARD);
        for (int i = 0; i < result.length; i++) {
            Assert.assertEquals(x2[i], result[i].getReal(), tolerance);
            Assert.assertEquals(0.0, result[i].getImaginary(), tolerance);
        }

        result = transformer.transform(x2, TransformType.INVERSE);
        for (int i = 0; i < result.length; i++) {
            Assert.assertEquals(y2[i].getReal(), result[i].getReal(), tolerance);
            Assert.assertEquals(y2[i].getImaginary(), result[i].getImaginary(), tolerance);
        }
    }

// org.apache.commons.math3.transform.FastFourierTransformerTest::testSinFunction
    public void testSinFunction() {
        UnivariateFunction f = new Sin();
        FastFourierTransformer transformer;
        transformer = new FastFourierTransformer(DftNormalization.STANDARD);
        Complex result[]; int N = 1 << 8;
        double min, max, tolerance = 1E-12;

        min = 0.0; max = 2.0 * FastMath.PI;
        result = transformer.transform(f, min, max, N, TransformType.FORWARD);
        Assert.assertEquals(0.0, result[1].getReal(), tolerance);
        Assert.assertEquals(-(N >> 1), result[1].getImaginary(), tolerance);
        Assert.assertEquals(0.0, result[N-1].getReal(), tolerance);
        Assert.assertEquals(N >> 1, result[N-1].getImaginary(), tolerance);
        for (int i = 0; i < N-1; i += (i == 0 ? 2 : 1)) {
            Assert.assertEquals(0.0, result[i].getReal(), tolerance);
            Assert.assertEquals(0.0, result[i].getImaginary(), tolerance);
        }

        min = -FastMath.PI; max = FastMath.PI;
        result = transformer.transform(f, min, max, N, TransformType.INVERSE);
        Assert.assertEquals(0.0, result[1].getReal(), tolerance);
        Assert.assertEquals(-0.5, result[1].getImaginary(), tolerance);
        Assert.assertEquals(0.0, result[N-1].getReal(), tolerance);
        Assert.assertEquals(0.5, result[N-1].getImaginary(), tolerance);
        for (int i = 0; i < N-1; i += (i == 0 ? 2 : 1)) {
            Assert.assertEquals(0.0, result[i].getReal(), tolerance);
            Assert.assertEquals(0.0, result[i].getImaginary(), tolerance);
        }
    }

// org.apache.commons.math3.transform.FastFourierTransformerTest::test2DData
    public void test2DData() {
        FastFourierTransformer transformer;
        transformer = new FastFourierTransformer(DftNormalization.STANDARD);

        double tolerance = 1E-12;
        Complex[][] input = new Complex[][] {new Complex[] {new Complex(1, 0),
                                                            new Complex(2, 0)},
                                             new Complex[] {new Complex(3, 1),
                                                            new Complex(4, 2)}};
        Complex[][] goodOutput = new Complex[][] {new Complex[] {new Complex(5,
                1.5), new Complex(-1, -.5)}, new Complex[] {new Complex(-2,
                -1.5), new Complex(0, .5)}};
        for (int i = 0; i < goodOutput.length; i++) {
            TransformUtils.scaleArray(
                goodOutput[i],
                FastMath.sqrt(goodOutput[i].length) *
                    FastMath.sqrt(goodOutput.length));
        }
        Complex[][] output = (Complex[][])transformer.mdfft(input, TransformType.FORWARD);
        Complex[][] output2 = (Complex[][])transformer.mdfft(output, TransformType.INVERSE);

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

// org.apache.commons.math3.transform.FastFourierTransformerTest::test2DDataUnitary
    public void test2DDataUnitary() {
        FastFourierTransformer transformer;
        transformer = new FastFourierTransformer(DftNormalization.UNITARY);
        double tolerance = 1E-12;
        Complex[][] input = new Complex[][] {new Complex[] {new Complex(1, 0),
                                                            new Complex(2, 0)},
                                             new Complex[] {new Complex(3, 1),
                                                            new Complex(4, 2)}};
        Complex[][] goodOutput = new Complex[][] {new Complex[] {new Complex(5,
                1.5), new Complex(-1, -.5)}, new Complex[] {new Complex(-2,
                -1.5), new Complex(0, .5)}};
        Complex[][] output = (Complex[][])transformer.mdfft(input, TransformType.FORWARD);
        Complex[][] output2 = (Complex[][])transformer.mdfft(output, TransformType.INVERSE);

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

// org.apache.commons.math3.transform.FastSineTransformerTest::testTransformRealFirstElementNotZero
    public void testTransformRealFirstElementNotZero() {
        final TransformType[] type = TransformType.values();
        final double[] data = new double[] {
            1.0, 1.0, 1.0, 1.0
        };
        final RealTransformer transformer = createRealTransformer();
        for (int j = 0; j < type.length; j++) {
            try {
                transformer.transform(data, type[j]);
                Assert.fail(type[j].toString());
            } catch (MathIllegalArgumentException e) {
                
            }
        }
    }

// org.apache.commons.math3.transform.FastSineTransformerTest::testAdHocData
    public void testAdHocData() {
        FastSineTransformer transformer;
        transformer = new FastSineTransformer(DstNormalization.STANDARD_DST_I);
        double result[], tolerance = 1E-12;

        double x[] = { 0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0 };
        double y[] = { 0.0, 20.1093579685034, -9.65685424949238,
                       5.98642305066196, -4.0, 2.67271455167720,
                      -1.65685424949238, 0.795649469518633 };

        result = transformer.transform(x, TransformType.FORWARD);
        for (int i = 0; i < result.length; i++) {
            Assert.assertEquals(y[i], result[i], tolerance);
        }

        result = transformer.transform(y, TransformType.INVERSE);
        for (int i = 0; i < result.length; i++) {
            Assert.assertEquals(x[i], result[i], tolerance);
        }

        TransformUtils.scaleArray(x, FastMath.sqrt(x.length / 2.0));
        transformer = new FastSineTransformer(DstNormalization.ORTHOGONAL_DST_I);

        result = transformer.transform(y, TransformType.FORWARD);
        for (int i = 0; i < result.length; i++) {
            Assert.assertEquals(x[i], result[i], tolerance);
        }

        result = transformer.transform(x, TransformType.INVERSE);
        for (int i = 0; i < result.length; i++) {
            Assert.assertEquals(y[i], result[i], tolerance);
        }
    }

// org.apache.commons.math3.transform.FastSineTransformerTest::testSinFunction
    public void testSinFunction() {
        UnivariateFunction f = new Sin();
        FastSineTransformer transformer;
        transformer = new FastSineTransformer(DstNormalization.STANDARD_DST_I);
        double min, max, result[], tolerance = 1E-12; int N = 1 << 8;

        min = 0.0; max = 2.0 * FastMath.PI;
        result = transformer.transform(f, min, max, N, TransformType.FORWARD);
        Assert.assertEquals(N >> 1, result[2], tolerance);
        for (int i = 0; i < N; i += (i == 1 ? 2 : 1)) {
            Assert.assertEquals(0.0, result[i], tolerance);
        }

        min = -FastMath.PI; max = FastMath.PI;
        result = transformer.transform(f, min, max, N, TransformType.FORWARD);
        Assert.assertEquals(-(N >> 1), result[2], tolerance);
        for (int i = 0; i < N; i += (i == 1 ? 2 : 1)) {
            Assert.assertEquals(0.0, result[i], tolerance);
        }
    }

// org.apache.commons.math3.transform.FastSineTransformerTest::testParameters
    public void testParameters() throws Exception {
        UnivariateFunction f = new Sin();
        FastSineTransformer transformer;
        transformer = new FastSineTransformer(DstNormalization.STANDARD_DST_I);

        try {
            
            transformer.transform(f, 1, -1, 64, TransformType.FORWARD);
            Assert.fail("Expecting IllegalArgumentException - bad interval");
        } catch (IllegalArgumentException ex) {
            
        }
        try {
            
            transformer.transform(f, -1, 1, 0, TransformType.FORWARD);
            Assert.fail("Expecting IllegalArgumentException - bad samples number");
        } catch (IllegalArgumentException ex) {
            
        }
        try {
            
            transformer.transform(f, -1, 1, 100, TransformType.FORWARD);
            Assert.fail("Expecting IllegalArgumentException - bad samples number");
        } catch (IllegalArgumentException ex) {
            
        }
    }
