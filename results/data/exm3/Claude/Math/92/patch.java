public static long binomialCoefficient(final int n, final int k) {
    if (n < k) {
        throw new IllegalArgumentException(
            "must have n >= k for binomial coefficient (n,k)");
    }
    if (n < 0) {
        throw new IllegalArgumentException(
            "must have n >= 0 for binomial coefficient (n,k)");
    }
    if ((n == k) || (k == 0)) {
        return 1;
    }
    if ((k == 1) || (k == n - 1)) {
        return n;
    }
    
    // Use symmetry for large k
    int kUse = k;
    if (k > n / 2) {
        kUse = n - k;
    }
    
    // For n <= 61, the naive implementation cannot overflow.
    if (n <= 61) {
        long result = 1;
        for (int i = 1; i <= kUse; i++) {
            result = result * (n - kUse + i) / i;
        }
        return result;
    }
    
    // For n > 61 but n <= 66, the result cannot overflow,
    // but we must take care not to overflow intermediate values.
    if (n <= 66) {
        long result = 1;
        for (int i = 1; i <= kUse; i++) {
            long d = gcd(n - kUse + i, i);
            result = (result / (i / d)) * ((n - kUse + i) / d);
        }
        return result;
    }
    
    // For n > 66, a result overflow might occur, so we check
    // the multiplication, taking care to not overflow
    // unnecessary.
    long result = Math.round(binomialCoefficientDouble(n, kUse));
    if (result == Long.MAX_VALUE) {
        throw new ArithmeticException(
            "result too large to represent in a long integer");
    }
    return result;
}

private static long gcd(long a, long b) {
    long absA = Math.abs(a);
    long absB = Math.abs(b);
    if (absA == 0 || absB == 0) {
        return absA + absB;
    }
    while (absB != 0) {
        long temp = absB;
        absB = absA % absB;
        absA = temp;
    }
    return absA;
}