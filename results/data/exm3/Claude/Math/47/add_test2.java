// org/apache/commons/math/complex/ComplexTest.java
@Test
    public void testDivideNaNByZero() {
        Complex x = Complex.NaN;
        Complex z = x.divide(Complex.ZERO);
        Assert.assertEquals(z, Complex.NaN);
    }