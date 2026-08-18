// org/apache/commons/math/util/MathUtilsTest.java
public void testBinomialCoefficientSymmetry() throws Exception {
    int[] ns = {100, 200, 300};
    for (int n : ns) {
        for (int k = 0; k <= n; k++) {
            long result1 = MathUtils.binomialCoefficient(n, k);
            long result2 = MathUtils.binomialCoefficient(n, n - k);
            assertEquals("n="+n+", k="+k, result1, result2);
        }
    }
}
