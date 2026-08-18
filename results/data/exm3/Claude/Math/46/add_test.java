// org/apache/commons/math/complex/ComplexTest.java
@Test
public void testDivideRealZero() {
    Complex x = new Complex(3.0, 4.0);
    Complex z = x.divide(0.0);
    Assert.assertEquals(z, Complex.NaN);
}