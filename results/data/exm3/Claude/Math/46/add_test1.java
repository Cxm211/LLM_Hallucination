// org/apache/commons/math/complex/ComplexTest.java
@Test
public void testDivideZeroByZero() {
    Complex z = Complex.ZERO.divide(Complex.ZERO);
    Assert.assertEquals(z, Complex.NaN);
}