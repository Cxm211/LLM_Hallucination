// org/apache/commons/math3/fraction/FractionTest.java
@Test
    public void testDigitLimitConstructor() throws ConvergenceException  {
        assertFraction(2, 5, new Fraction(0.4,   9));
        assertFraction(2, 5, new Fraction(0.4,  99));
        assertFraction(2, 5, new Fraction(0.4, 999));

        assertFraction(3, 5,      new Fraction(0.6152,    9));
        assertFraction(8, 13,     new Fraction(0.6152,   99));
        assertFraction(510, 829,  new Fraction(0.6152,  999));
        assertFraction(769, 1250, new Fraction(0.6152, 9999));

        // MATH-996
        assertFraction(1, 2, new Fraction(0.5000000001, 10));

        // Additional test case for bug fix
        assertFraction(1, 2, new Fraction(0.5, 2));
    }
