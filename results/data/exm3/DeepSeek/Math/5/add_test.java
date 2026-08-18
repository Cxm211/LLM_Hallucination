// org/apache/commons/math3/complex/ComplexTest.java
@Test
    public void testReciprocalVarious() {
        // NaN
        Complex nan = Complex.NaN;
        Assert.assertEquals(nan.reciprocal(), Complex.NaN);
        // Infinite
        Complex inf = Complex.INF;
        Assert.assertEquals(inf.reciprocal(), Complex.ZERO);
        // Zero
        Complex zero = Complex.ZERO;
        Assert.assertEquals(zero.reciprocal(), Complex.INF);
        // Normal complex number: 1+2i (triggers |real| < |imag|)
        Complex c1 = new Complex(1.0, 2.0);
        Complex rec1 = c1.reciprocal();
        Assert.assertEquals(rec1.getReal(), 0.2, 1e-10);
        Assert.assertEquals(rec1.getImaginary(), -0.4, 1e-10);
        // Another normal: 2+1i (triggers |real| >= |imag|)
        Complex c2 = new Complex(2.0, 1.0);
        Complex rec2 = c2.reciprocal();
        Assert.assertEquals(rec2.getReal(), 0.4, 1e-10);
        Assert.assertEquals(rec2.getImaginary(), -0.2, 1e-10);
    }
