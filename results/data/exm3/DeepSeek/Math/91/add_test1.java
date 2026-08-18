// org/apache/commons/math/fraction/FractionTest.java
public void testCompareToNegativeCloseFractions() {
        Fraction a = new Fraction(-400000000, 400000001);
        Fraction b = new Fraction(-400000001, 400000002);
        assertEquals(1, a.compareTo(b));
        assertEquals(-1, b.compareTo(a));
    }
