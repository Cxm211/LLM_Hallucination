// org/apache/commons/math3/complex/ComplexTest.java
@Test
    public void testReciprocalNegativeZero() {
        Complex negZero = new Complex(-0.0, -0.0);
        Assert.assertEquals(Complex.INF, negZero.reciprocal());
    }