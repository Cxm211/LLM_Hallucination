// org/apache/commons/math/complex/ComplexTest.java
@Test
public void testDivideByZeroMore() {
    Complex inf = new Complex(Double.POSITIVE_INFINITY, 0);
    Complex result1 = inf.divide(Complex.ZERO);
    Assert.assertEquals(Complex.INF, result1);

    Complex x = new Complex(3.0, 4.0);
    Complex result2 = x.divide(0.0);
    Assert.assertEquals(Complex.INF, result2);

    Complex result3 = inf.divide(0.0);
    Assert.assertEquals(Complex.INF, result3);
}
