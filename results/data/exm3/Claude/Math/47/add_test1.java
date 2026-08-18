// org/apache/commons/math/complex/ComplexTest.java
@Test
    public void testDivideInfByZero() {
        Complex x = Complex.INF;
        Complex z = x.divide(Complex.ZERO);
        Assert.assertEquals(z, Complex.INF);
    }