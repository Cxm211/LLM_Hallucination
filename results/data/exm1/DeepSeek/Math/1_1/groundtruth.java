// ===== FIXED org.apache.commons.math3.fraction.BigFraction :: BigFraction [lines 107-109] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Math/Math-1-fixed/src/main/java/org/apache/commons/math3/fraction/BigFraction.java =====
    public BigFraction(final BigInteger num) {
        this(num, BigInteger.ONE);
    }

// ===== FIXED org.apache.commons.math3.fraction.Fraction :: Fraction [lines 101-103] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Math/Math-1-fixed/src/main/java/org/apache/commons/math3/fraction/Fraction.java =====
    public Fraction(double value) throws FractionConversionException {
        this(value, DEFAULT_EPSILON, 100);
    }
