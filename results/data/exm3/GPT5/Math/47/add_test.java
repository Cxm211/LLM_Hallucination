// org/apache/commons/math/complex/ComplexTest.java::testDivideZero
@Test
    public void testDivideZeroScalar() {
        Complex x = new Complex(3.0, -5.0);
        Complex z = x.divide(0d);
        Assert.assertEquals(Complex.INF, z);
    }