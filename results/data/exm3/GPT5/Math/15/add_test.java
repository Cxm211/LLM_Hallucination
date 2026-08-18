// org/apache/commons/math3/util/FastMathTest.java
@Test
public void testMath904_additionalOddLarge() {
    final double x = -2.0;
    final double y = Math.pow(2.0, 52) + 1.0; // 2^52 + 1, exact odd integer in double
    Assert.assertEquals(Math.pow(x, y), FastMath.pow(x, y), 0);
    Assert.assertEquals(Math.pow(x, -y), FastMath.pow(x, -y), 0);
}