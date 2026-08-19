// ===== FIXED org.apache.commons.math.util.MathUtils :: factorial(int) [lines 344-353] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Math/Math-93-fixed/src/java/org/apache/commons/math/util/MathUtils.java =====
    public static long factorial(final int n) {
        if (n < 0) {
            throw new IllegalArgumentException("must have n >= 0 for n!");
        }
        if (n > 20) {
            throw new ArithmeticException(
                    "factorial value is too large to fit in a long");
        }
        return factorials[n];
    }

// ===== FIXED org.apache.commons.math.util.MathUtils :: factorialDouble(int) [lines 375-383] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Math/Math-93-fixed/src/java/org/apache/commons/math/util/MathUtils.java =====
    public static double factorialDouble(final int n) {
        if (n < 0) {
            throw new IllegalArgumentException("must have n >= 0 for n!");
        }
        if (n < 21) {
            return factorial(n);
        }
        return Math.floor(Math.exp(factorialLog(n)) + 0.5);
    }

// ===== FIXED org.apache.commons.math.util.MathUtils :: factorialLog(int) [lines 398-410] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Math/Math-93-fixed/src/java/org/apache/commons/math/util/MathUtils.java =====
    public static double factorialLog(final int n) {
        if (n < 0) {
            throw new IllegalArgumentException("must have n > 0 for n!");
        }
        if (n < 21) {
            return Math.log(factorial(n));
        }
        double logSum = 0;
        for (int i = 2; i <= n; i++) {
            logSum += Math.log((double)i);
        }
        return logSum;
    }
