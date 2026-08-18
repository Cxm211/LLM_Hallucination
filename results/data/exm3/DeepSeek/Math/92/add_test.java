// org/apache/commons/math/util/MathUtilsTest.java
public void testBinomialCoefficient66_33() throws Exception {
    int n = 66;
    int k = 33;
    long expected = binomialCoefficient(n, k); // reference
    long actual = MathUtils.binomialCoefficient(n, k);
    assertEquals(expected, actual);
}
