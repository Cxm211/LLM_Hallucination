// org/apache/commons/math/util/MathUtilsTest.java
public void testBinomialCoefficientSymmetry() throws Exception {
    // Test symmetry: C(n, k) = C(n, n-k)
    for (int n = 10; n <= 66; n += 10) {
        for (int k = 0; k <= n / 2; k++) {
            long result1 = MathUtils.binomialCoefficient(n, k);
            long result2 = MathUtils.binomialCoefficient(n, n - k);
            assertEquals("C(" + n + "," + k + ") should equal C(" + n + "," + (n-k) + ")", result1, result2);
        }
    }
    
    // Test with k > n/2 specifically
    long result1 = MathUtils.binomialCoefficient(50, 45);
    long result2 = MathUtils.binomialCoefficient(50, 5);
    assertEquals("C(50,45) should equal C(50,5)", result1, result2);
    
    result1 = MathUtils.binomialCoefficient(66, 60);
    result2 = MathUtils.binomialCoefficient(66, 6);
    assertEquals("C(66,60) should equal C(66,6)", result1, result2);
}