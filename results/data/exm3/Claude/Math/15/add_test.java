// org/apache/commons/math3/util/FastMathTest.java
@Test
public void testMath904NegativeExponent() {
    final double x = -1;
    final double y = -((5 + 1e-15) * 1e15);
    Assert.assertEquals(Math.pow(x, y),
                        FastMath.pow(x, y), 0);
}