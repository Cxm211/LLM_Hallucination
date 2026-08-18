// buggy code
    public boolean equals(Object other) {
        boolean ret;
        
        if (this == other) { 
            ret = true;
        } else if (other == null) {
            ret = false;
        } else  {
            try {
                Complex rhs = (Complex)other;
                if (rhs.isNaN()) {
                    ret = this.isNaN();
                } else {
                    ret = (Double.doubleToRawLongBits(real) == Double.doubleToRawLongBits(rhs.getReal())) && (Double.doubleToRawLongBits(imaginary) == Double.doubleToRawLongBits(rhs.getImaginary())); 
                }
            } catch (ClassCastException ex) {
                // ignore exception
                ret = false;
            }
        }
      
        return ret;
    }

// relevant test
// org.apache.commons.math.analysis.LaguerreSolverTest::testLinearFunction
    public void testLinearFunction() throws MathException {
        double min, max, expected, result, tolerance;

        
        double coefficients[] = { -1.0, 4.0 };
        PolynomialFunction f = new PolynomialFunction(coefficients);
        UnivariateRealSolver solver = new LaguerreSolver(f);

        min = 0.0; max = 1.0; expected = 0.25;
        tolerance = Math.max(solver.getAbsoluteAccuracy(),
                    Math.abs(expected * solver.getRelativeAccuracy()));
        result = solver.solve(min, max);
        assertEquals(expected, result, tolerance);
    }

// org.apache.commons.math.analysis.LaguerreSolverTest::testQuadraticFunction
    public void testQuadraticFunction() throws MathException {
        double min, max, expected, result, tolerance;

        
        double coefficients[] = { -3.0, 5.0, 2.0 };
        PolynomialFunction f = new PolynomialFunction(coefficients);
        UnivariateRealSolver solver = new LaguerreSolver(f);

        min = 0.0; max = 2.0; expected = 0.5;
        tolerance = Math.max(solver.getAbsoluteAccuracy(),
                    Math.abs(expected * solver.getRelativeAccuracy()));
        result = solver.solve(min, max);
        assertEquals(expected, result, tolerance);

        min = -4.0; max = -1.0; expected = -3.0;
        tolerance = Math.max(solver.getAbsoluteAccuracy(),
                    Math.abs(expected * solver.getRelativeAccuracy()));
        result = solver.solve(min, max);
        assertEquals(expected, result, tolerance);
    }

// org.apache.commons.math.analysis.LaguerreSolverTest::testQuinticFunction
    public void testQuinticFunction() throws MathException {
        double min, max, expected, result, tolerance;

        
        double coefficients[] = { -12.0, -1.0, 1.0, -12.0, -1.0, 1.0 };
        PolynomialFunction f = new PolynomialFunction(coefficients);
        UnivariateRealSolver solver = new LaguerreSolver(f);

        min = -2.0; max = 2.0; expected = -1.0;
        tolerance = Math.max(solver.getAbsoluteAccuracy(),
                    Math.abs(expected * solver.getRelativeAccuracy()));
        result = solver.solve(min, max);
        assertEquals(expected, result, tolerance);

        min = -5.0; max = -2.5; expected = -3.0;
        tolerance = Math.max(solver.getAbsoluteAccuracy(),
                    Math.abs(expected * solver.getRelativeAccuracy()));
        result = solver.solve(min, max);
        assertEquals(expected, result, tolerance);

        min = 3.0; max = 6.0; expected = 4.0;
        tolerance = Math.max(solver.getAbsoluteAccuracy(),
                    Math.abs(expected * solver.getRelativeAccuracy()));
        result = solver.solve(min, max);
        assertEquals(expected, result, tolerance);
    }

// org.apache.commons.math.analysis.LaguerreSolverTest::testQuinticFunction2
    public void testQuinticFunction2() throws MathException {
        double initial = 0.0, tolerance;
        Complex expected, result[];

        
        double coefficients[] = { 4.0, 0.0, 1.0, 4.0, 0.0, 1.0 };
        PolynomialFunction f = new PolynomialFunction(coefficients);
        LaguerreSolver solver = new LaguerreSolver(f);
        result = solver.solveAll(coefficients, initial);

        expected = new Complex(0.0, -2.0);
        tolerance = Math.max(solver.getAbsoluteAccuracy(),
                    Math.abs(expected.abs() * solver.getRelativeAccuracy()));
        TestUtils.assertContains(result, expected, tolerance);

        expected = new Complex(0.0, 2.0);
        tolerance = Math.max(solver.getAbsoluteAccuracy(),
                    Math.abs(expected.abs() * solver.getRelativeAccuracy()));
        TestUtils.assertContains(result, expected, tolerance);

        expected = new Complex(0.5, 0.5 * Math.sqrt(3.0));
        tolerance = Math.max(solver.getAbsoluteAccuracy(),
                    Math.abs(expected.abs() * solver.getRelativeAccuracy()));
        TestUtils.assertContains(result, expected, tolerance);

        expected = new Complex(-1.0, 0.0);
        tolerance = Math.max(solver.getAbsoluteAccuracy(),
                    Math.abs(expected.abs() * solver.getRelativeAccuracy()));
        TestUtils.assertContains(result, expected, tolerance);
        
        expected = new Complex(0.5, -0.5 * Math.sqrt(3.0));
        tolerance = Math.max(solver.getAbsoluteAccuracy(),
                    Math.abs(expected.abs() * solver.getRelativeAccuracy()));
        TestUtils.assertContains(result, expected, tolerance);
    }

// org.apache.commons.math.analysis.LaguerreSolverTest::testParameters
    public void testParameters() throws Exception {
        double coefficients[] = { -3.0, 5.0, 2.0 };
        PolynomialFunction f = new PolynomialFunction(coefficients);
        UnivariateRealSolver solver = new LaguerreSolver(f);

        try {
            
            solver.solve(1, -1);
            fail("Expecting IllegalArgumentException - bad interval");
        } catch (IllegalArgumentException ex) {
            
        }
        try {
            
            solver.solve(2, 3);
            fail("Expecting IllegalArgumentException - no bracketing");
        } catch (IllegalArgumentException ex) {
            
        }
        try {
            
            UnivariateRealFunction f2 = new SinFunction();
            new LaguerreSolver(f2);
            fail("Expecting IllegalArgumentException - bad function");
        } catch (IllegalArgumentException ex) {
            
        }
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
        TestUtils.assertEquals(new Complex(Math.acos(0), 0), 
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
                Complex sqrtz = ComplexUtils.polar2Complex(Math.sqrt(r), theta / 2);
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

// org.apache.commons.math.transform.FastCosineTransformerTest::testAdHocData
    public void testAdHocData() throws MathException {
        FastCosineTransformer transformer = new FastCosineTransformer();
        double result[], tolerance = 1E-12;

        double x[] = { 0.0, 1.0, 4.0, 9.0, 16.0, 25.0, 36.0, 49.0, 64.0 };
        double y[] = { 172.0, -105.096569476353, 27.3137084989848,
                      -12.9593152353742, 8.0, -5.78585076868676,
                       4.68629150101524, -4.15826451958632, 4.0 };

        result = transformer.transform(x);
        for (int i = 0; i < result.length; i++) {
            assertEquals(y[i], result[i], tolerance);
        }

        result = transformer.inversetransform(y);
        for (int i = 0; i < result.length; i++) {
            assertEquals(x[i], result[i], tolerance);
        }

        FastFourierTransformer.scaleArray(x, Math.sqrt(0.5 * (x.length-1)));

        result = transformer.transform2(y);
        for (int i = 0; i < result.length; i++) {
            assertEquals(x[i], result[i], tolerance);
        }

        result = transformer.inversetransform2(x);
        for (int i = 0; i < result.length; i++) {
            assertEquals(y[i], result[i], tolerance);
        }
    }

// org.apache.commons.math.transform.FastCosineTransformerTest::testSinFunction
    public void testSinFunction() throws MathException {
        UnivariateRealFunction f = new SinFunction();
        FastCosineTransformer transformer = new FastCosineTransformer();
        double min, max, result[], tolerance = 1E-12; int N = 9;

        double expected[] = { 0.0, 3.26197262739567, 0.0,
                             -2.17958042710327, 0.0, -0.648846697642915,
                              0.0, -0.433545502649478, 0.0 };
        min = 0.0; max = 2.0 * Math.PI * N / (N-1);
        result = transformer.transform(f, min, max, N);
        for (int i = 0; i < N; i++) {
            assertEquals(expected[i], result[i], tolerance);
        }

        min = -Math.PI; max = Math.PI * (N+1) / (N-1);
        result = transformer.transform(f, min, max, N);
        for (int i = 0; i < N; i++) {
            assertEquals(-expected[i], result[i], tolerance);
        }
    }

// org.apache.commons.math.transform.FastCosineTransformerTest::testParameters
    public void testParameters() throws Exception {
        UnivariateRealFunction f = new SinFunction();
        FastCosineTransformer transformer = new FastCosineTransformer();

        try {
            
            transformer.transform(f, 1, -1, 65);
            fail("Expecting IllegalArgumentException - bad interval");
        } catch (IllegalArgumentException ex) {
            
        }
        try {
            
            transformer.transform(f, -1, 1, 1);
            fail("Expecting IllegalArgumentException - bad samples number");
        } catch (IllegalArgumentException ex) {
            
        }
        try {
            
            transformer.transform(f, -1, 1, 64);
            fail("Expecting IllegalArgumentException - bad samples number");
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.transform.FastFourierTransformerTest::testAdHocData
    public void testAdHocData() throws MathException {
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
            assertEquals(y[i].getReal(), result[i].getReal(), tolerance);
            assertEquals(y[i].getImaginary(), result[i].getImaginary(), tolerance);
        }

        result = transformer.inversetransform(y);
        for (int i = 0; i < result.length; i++) {
            assertEquals(x[i], result[i].getReal(), tolerance);
            assertEquals(0.0, result[i].getImaginary(), tolerance);
        }

        double x2[] = {10.4, 21.6, 40.8, 13.6, 23.2, 32.8, 13.6, 19.2};
        FastFourierTransformer.scaleArray(x2, 1.0 / Math.sqrt(x2.length));
        Complex y2[] = y;

        result = transformer.transform2(y2);
        for (int i = 0; i < result.length; i++) {
            assertEquals(x2[i], result[i].getReal(), tolerance);
            assertEquals(0.0, result[i].getImaginary(), tolerance);
        }

        result = transformer.inversetransform2(x2);
        for (int i = 0; i < result.length; i++) {
            assertEquals(y2[i].getReal(), result[i].getReal(), tolerance);
            assertEquals(y2[i].getImaginary(), result[i].getImaginary(), tolerance);
        }
    }

// org.apache.commons.math.transform.FastFourierTransformerTest::testSinFunction
    public void testSinFunction() throws MathException {
        UnivariateRealFunction f = new SinFunction();
        FastFourierTransformer transformer = new FastFourierTransformer();
        Complex result[]; int N = 1 << 8;
        double min, max, tolerance = 1E-12;

        min = 0.0; max = 2.0 * Math.PI;
        result = transformer.transform(f, min, max, N);
        assertEquals(0.0, result[1].getReal(), tolerance);
        assertEquals(-(N >> 1), result[1].getImaginary(), tolerance);
        assertEquals(0.0, result[N-1].getReal(), tolerance);
        assertEquals(N >> 1, result[N-1].getImaginary(), tolerance);
        for (int i = 0; i < N-1; i += (i == 0 ? 2 : 1)) {
            assertEquals(0.0, result[i].getReal(), tolerance);
            assertEquals(0.0, result[i].getImaginary(), tolerance);
        }

        min = -Math.PI; max = Math.PI;
        result = transformer.inversetransform(f, min, max, N);
        assertEquals(0.0, result[1].getReal(), tolerance);
        assertEquals(-0.5, result[1].getImaginary(), tolerance);
        assertEquals(0.0, result[N-1].getReal(), tolerance);
        assertEquals(0.5, result[N-1].getImaginary(), tolerance);
        for (int i = 0; i < N-1; i += (i == 0 ? 2 : 1)) {
            assertEquals(0.0, result[i].getReal(), tolerance);
            assertEquals(0.0, result[i].getImaginary(), tolerance);
        }
    }

// org.apache.commons.math.transform.FastFourierTransformerTest::testParameters
    public void testParameters() throws Exception {
        UnivariateRealFunction f = new SinFunction();
        FastFourierTransformer transformer = new FastFourierTransformer();

        try {
            
            transformer.transform(f, 1, -1, 64);
            fail("Expecting IllegalArgumentException - bad interval");
        } catch (IllegalArgumentException ex) {
            
        }
        try {
            
            transformer.transform(f, -1, 1, 0);
            fail("Expecting IllegalArgumentException - bad samples number");
        } catch (IllegalArgumentException ex) {
            
        }
        try {
            
            transformer.transform(f, -1, 1, 100);
            fail("Expecting IllegalArgumentException - bad samples number");
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.transform.FastSineTransformerTest::testAdHocData
    public void testAdHocData() throws MathException {
        FastSineTransformer transformer = new FastSineTransformer();
        double result[], tolerance = 1E-12;

        double x[] = { 0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0 };
        double y[] = { 0.0, 20.1093579685034, -9.65685424949238,
                       5.98642305066196, -4.0, 2.67271455167720,
                      -1.65685424949238, 0.795649469518633 };

        result = transformer.transform(x);
        for (int i = 0; i < result.length; i++) {
            assertEquals(y[i], result[i], tolerance);
        }

        result = transformer.inversetransform(y);
        for (int i = 0; i < result.length; i++) {
            assertEquals(x[i], result[i], tolerance);
        }

        FastFourierTransformer.scaleArray(x, Math.sqrt(x.length / 2.0));

        result = transformer.transform2(y);
        for (int i = 0; i < result.length; i++) {
            assertEquals(x[i], result[i], tolerance);
        }

        result = transformer.inversetransform2(x);
        for (int i = 0; i < result.length; i++) {
            assertEquals(y[i], result[i], tolerance);
        }
    }

// org.apache.commons.math.transform.FastSineTransformerTest::testSinFunction
    public void testSinFunction() throws MathException {
        UnivariateRealFunction f = new SinFunction();
        FastSineTransformer transformer = new FastSineTransformer();
        double min, max, result[], tolerance = 1E-12; int N = 1 << 8;

        min = 0.0; max = 2.0 * Math.PI;
        result = transformer.transform(f, min, max, N);
        assertEquals(N >> 1, result[2], tolerance);
        for (int i = 0; i < N; i += (i == 1 ? 2 : 1)) {
            assertEquals(0.0, result[i], tolerance);
        }

        min = -Math.PI; max = Math.PI;
        result = transformer.transform(f, min, max, N);
        assertEquals(-(N >> 1), result[2], tolerance);
        for (int i = 0; i < N; i += (i == 1 ? 2 : 1)) {
            assertEquals(0.0, result[i], tolerance);
        }
    }

// org.apache.commons.math.transform.FastSineTransformerTest::testParameters
    public void testParameters() throws Exception {
        UnivariateRealFunction f = new SinFunction();
        FastSineTransformer transformer = new FastSineTransformer();

        try {
            
            transformer.transform(f, 1, -1, 64);
            fail("Expecting IllegalArgumentException - bad interval");
        } catch (IllegalArgumentException ex) {
            
        }
        try {
            
            transformer.transform(f, -1, 1, 0);
            fail("Expecting IllegalArgumentException - bad samples number");
        } catch (IllegalArgumentException ex) {
            
        }
        try {
            
            transformer.transform(f, -1, 1, 100);
            fail("Expecting IllegalArgumentException - bad samples number");
        } catch (IllegalArgumentException ex) {
            
        }
    }
