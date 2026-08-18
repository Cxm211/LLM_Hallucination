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
    long result = Math.round(binomialCoefficientDouble(n, k));
    if (result == Long.MAX_VALUE) {
        throw new ArithmeticException(
            "result too large to represent in a long integer");
    }
    // Use symmetry for large k
    int kUse = k;
    if (k > n / 2) {
        kUse = n - k;
    }
    // We use the formula
    // (n choose k) = n! / (n-k)! / k!
    // (n choose k) == ((n-k+1)*...*n) / (1*...*k)
    // which could be written
    // (n choose k) == (n-1 choose k-1) * n / k
    if (n <= 61) {
        // For n <= 61, the naive implementation cannot overflow.
        result = 1;
        for (int i = n - kUse + 1; i <= n; i++) {
            result *= i;
        }
        for (int i = 2; i <= kUse; i++) {
            result /= i;
        }
    } else if (n <= 66) {
        // For n > 61 but n <= 66, the result cannot overflow,
        // but we must take care not to overflow intermediate values.
        result = 1;
        for (int i = 1; i <= kUse; i++) {
            // We know that (result * i) is divisible by j,
            // but (result * i) may overflow, so we split j:
            // Filter out the gcd, d, so j/d and i/d are integer.
            // result is divisible by (j/d) because (j/d)
            // is relative prime to (i/d) and is a divisor of
            // result * (i/d).
            long d = gcd(n - kUse + i, i);
            result = (result / (i / d)) * ((n - kUse + i) / d);
        }
    } else {
        // For n > 66, a result overflow might occur, so we check
        // the multiplication, taking care to not overflow
        // unnecessary.
        result = 1;
        for (int i = 1; i <= kUse; i++) {
            long d = gcd(n - kUse + i, i);
            long r = result / (i / d);
            long m = (n - kUse + i) / d;
            if (r > Long.MAX_VALUE / m) {
                throw new ArithmeticException(
                    "result too large to represent in a long integer");
            }
            result = r * m;
        }
    }
    return result;
}