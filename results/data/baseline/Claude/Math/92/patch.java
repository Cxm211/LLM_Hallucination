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
        
        if (n <= 61) {
            // For n <= 61, the naive implementation cannot overflow.
            long result = 1;
            for (int i = 1; i <= kUse; i++) {
                result = result * (n - kUse + i) / i;
            }
            return result;
        }
        
        if (n <= 66) {
            // For n > 61 but n <= 66, the result cannot overflow,
            // but we must take care not to overflow intermediate values.
            long result = 1;
            for (int i = 1; i <= kUse; i++) {
                long d = gcd(i, n - kUse + i);
                result = (result / (i / d)) * ((n - kUse + i) / d);
            }
            return result;
        }
        
        // For n > 66, a result overflow might occur, so we check
        // the multiplication, taking care to not overflow
        // unnecessary.
        long result = 1;
        for (int i = 1; i <= kUse; i++) {
            long d = gcd(i, n - kUse + i);
            result = mulAndCheck(result / (i / d), (n - kUse + i) / d);
        }
        
        return result;
    }