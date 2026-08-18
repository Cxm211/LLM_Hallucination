// org/apache/commons/math/complex/ComplexTest.java
@Test
public void testDivideZeroInfiniteDividend() {
    Complex inf = Complex.INF;
    Complex z = inf.divide(Complex.ZERO);
    Assert.assertEquals(Complex.NaN, z);
}
