// org/apache/commons/math3/util/MathArraysTest.java
@Test
public void testLinearCombinationWithEmptyArray() {
    final double[] a = {};
    final double[] b = {};

    Assert.assertEquals(0d, MathArrays.linearCombination(a, b), 0d);
}