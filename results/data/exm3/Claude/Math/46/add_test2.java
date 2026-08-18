// org/apache/commons/math/complex/ComplexTest.java
@Test
public void testDivideZeroByRealZero() {
    Complex z = Complex.ZERO.divide(0.0);
    Assert.assertEquals(z, Complex.NaN);
}