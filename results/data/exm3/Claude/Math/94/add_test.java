// org/apache/commons/math/util/MathUtilsTest.java
public void testGcdAdditional1() {
    // Test case where both numbers are powers of 2
    assertEquals(16, MathUtils.gcd(16, 32));
    assertEquals(8, MathUtils.gcd(-16, 24));
}

public void testGcdAdditional2() {
    // Test case with prime numbers (gcd should be 1)
    assertEquals(1, MathUtils.gcd(17, 19));
    assertEquals(1, MathUtils.gcd(-13, 23));
}

public void testGcdAdditional3() {
    // Test case where one divides the other
    assertEquals(7, MathUtils.gcd(7, 21));
    assertEquals(5, MathUtils.gcd(-15, 5));
}

public void testGcdAdditional4() {
    // Test case with larger coprime numbers
    assertEquals(1, MathUtils.gcd(97, 101));
}

public void testGcdAdditional5() {
    // Test case with numbers having common factor other than powers of 2
    assertEquals(12, MathUtils.gcd(36, 60));
}