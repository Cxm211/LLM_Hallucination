// org/apache/commons/math3/util/FastMathTest.java
@Test
public void testMath904DifferentBase() {
    final double x = -2.0;
    final double y = (5 + 1e-15) * 1e15;
    Assert.assertEquals(Math.pow(x, y),
                        FastMath.pow(x, y), 0);
}