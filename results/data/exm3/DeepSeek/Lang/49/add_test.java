// org/apache/commons/lang/math/FractionTest.java
public void testReduceEdgeCases() {
        Fraction f = null;
        Fraction result = null;
        
        f = Fraction.getFraction(Integer.MIN_VALUE, 1);
        result = f.reduce();
        assertEquals(Integer.MIN_VALUE, result.getNumerator());
        assertEquals(1, result.getDenominator());
        
        f = Fraction.getFraction(Integer.MIN_VALUE, 2);
        result = f.reduce();
        assertEquals(-1073741824, result.getNumerator());
        assertEquals(1, result.getDenominator());
        
        f = Fraction.getFraction(Integer.MIN_VALUE, -2);
        result = f.reduce();
        assertEquals(1073741824, result.getNumerator());
        assertEquals(1, result.getDenominator());
        
        f = Fraction.getFraction(Integer.MIN_VALUE, Integer.MIN_VALUE);
        result = f.reduce();
        assertEquals(1, result.getNumerator());
        assertEquals(1, result.getDenominator());
        
        f = Fraction.getFraction(2, Integer.MIN_VALUE);
        result = f.reduce();
        assertEquals(-1, result.getNumerator());
        assertEquals(1073741824, result.getDenominator());
        
        f = Fraction.getFraction(0, -100);
        result = f.reduce();
        assertEquals(0, result.getNumerator());
        assertEquals(1, result.getDenominator());
        assertSame(result, Fraction.ZERO);
    }
