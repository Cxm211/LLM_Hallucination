// org/apache/commons/math/fraction/FractionTest.java
public void testCompareToEdgeCases() {
    // Test fractions that are equal when converted to double but different as exact fractions
    Fraction f1 = new Fraction(1, Integer.MAX_VALUE);
    Fraction f2 = new Fraction(2, Integer.MAX_VALUE);
    assertTrue(f1.compareTo(f2) < 0);
    assertTrue(f2.compareTo(f1) > 0);
    assertEquals(0.0, f1.doubleValue() - f2.doubleValue(), 1.0e-20);
    
    // Test negative fractions
    Fraction neg1 = new Fraction(-1, 2);
    Fraction neg2 = new Fraction(-1, 3);
    assertTrue(neg1.compareTo(neg2) < 0);
    assertTrue(neg2.compareTo(neg1) > 0);
    
    // Test zero
    Fraction zero1 = new Fraction(0, 1);
    Fraction zero2 = new Fraction(0, 5);
    assertEquals(0, zero1.compareTo(zero2));
    
    // Test mixed positive and negative
    Fraction pos = new Fraction(1, 4);
    Fraction neg = new Fraction(-1, 4);
    assertTrue(pos.compareTo(neg) > 0);
    assertTrue(neg.compareTo(pos) < 0);
}