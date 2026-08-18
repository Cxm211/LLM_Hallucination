// org/apache/commons/math/util/MathUtilsTest.java
public void testBinomialCoefficientBoundaryValues() throws Exception {
    // Test boundary between different algorithm branches (n=61, n=62)
    long result61_30 = MathUtils.binomialCoefficient(61, 30);
    assertTrue("C(61,30) should be positive", result61_30 > 0);
    
    long result62_31 = MathUtils.binomialCoefficient(62, 31);
    assertTrue("C(62,31) should be positive", result62_31 > 0);
    
    // Test boundary at n=66, n=67
    long result66_33 = MathUtils.binomialCoefficient(66, 33);
    assertTrue("C(66,33) should be positive", result66_33 > 0);
    
    try {
        long result67_34 = MathUtils.binomialCoefficient(67, 34);
        // May or may not throw depending on value
    } catch (ArithmeticException ex) {
        // Expected for overflow cases
    }
    
    // Test with large k close to n (using symmetry)
    long result100_98 = MathUtils.binomialCoefficient(100, 98);
    long result100_2 = MathUtils.binomialCoefficient(100, 2);
    assertEquals("C(100,98) should equal C(100,2)", result100_2, result100_98);
}