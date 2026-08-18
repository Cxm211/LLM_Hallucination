// org/apache/commons/math3/complex/ComplexTest.java
@Test
    public void testReciprocalMixedZero() {
        Complex mixedZero1 = new Complex(0.0, -0.0);
        Complex mixedZero2 = new Complex(-0.0, 0.0);
        Assert.assertEquals(Complex.INF, mixedZero1.reciprocal());
        Assert.assertEquals(Complex.INF, mixedZero2.reciprocal());
    }