// ===== FIXED org.apache.commons.math.fraction.Fraction :: compareTo(Fraction) [lines 258-262] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Math/Math-91-fixed/src/java/org/apache/commons/math/fraction/Fraction.java =====
    public int compareTo(Fraction object) {
        long nOd = ((long) numerator) * object.denominator;
        long dOn = ((long) denominator) * object.numerator;
        return (nOd < dOn) ? -1 : ((nOd > dOn) ? +1 : 0);
    }
