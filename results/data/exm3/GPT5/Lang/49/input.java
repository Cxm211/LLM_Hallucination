// buggy function
    public Fraction reduce() {
        int gcd = greatestCommonDivisor(Math.abs(numerator), denominator);
        if (gcd == 1) {
            return this;
        }
        return Fraction.getFraction(numerator / gcd, denominator / gcd);
    }

// trigger testcase
// org/apache/commons/lang/math/FractionTest.java::testReduce
public void testReduce() {
        Fraction f = null;
        
        f = Fraction.getFraction(50, 75);
        Fraction result = f.reduce();
        assertEquals(2, result.getNumerator());
        assertEquals(3, result.getDenominator());

        f = Fraction.getFraction(-2, -3);
        result = f.reduce();
        assertEquals(2, result.getNumerator());
        assertEquals(3, result.getDenominator());

        f = Fraction.getFraction(2, -3);
        result = f.reduce();
        assertEquals(-2, result.getNumerator());
        assertEquals(3, result.getDenominator());

        f = Fraction.getFraction(-2, 3);
        result = f.reduce();
        assertEquals(-2, result.getNumerator());
        assertEquals(3, result.getDenominator());
        assertSame(f, result);

        f = Fraction.getFraction(2, 3);
        result = f.reduce();
        assertEquals(2, result.getNumerator());
        assertEquals(3, result.getDenominator());
        assertSame(f, result);

        f = Fraction.getFraction(0, 1);
        result = f.reduce();
        assertEquals(0, result.getNumerator());
        assertEquals(1, result.getDenominator());
        assertSame(f, result);

        f = Fraction.getFraction(0, 100);
        result = f.reduce();
        assertEquals(0, result.getNumerator());
        assertEquals(1, result.getDenominator());
        assertSame(result, Fraction.ZERO);
    }
