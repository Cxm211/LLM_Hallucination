// org/apache/commons/math/complex/ComplexTest.java::testDivideZero
@Test
    public void testDivideDoubleZero() {
        Complex x = new Complex(3.0, 4.0);
        Complex z = x.divide(0d);
        Assert.assertEquals(Complex.NaN, z);
    }