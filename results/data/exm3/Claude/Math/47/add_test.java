// org/apache/commons/math/complex/ComplexTest.java
@Test
    public void testDivideByZeroDouble() {
        Complex x = new Complex(5.0, -3.0);
        Complex z = x.divide(0.0);
        Assert.assertEquals(z, Complex.INF);
    }