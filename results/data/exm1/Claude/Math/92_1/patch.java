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
        int kMod = k;
        if (k > n / 2) {
            kMod = n - k;
        }
        
        // We use the formula
        // (n choose k) = n! / (n-k)! / k!
        // (n choose k) == ((n-k+1)*...*n) / (1*...*k)
        // which could be written
        // (n choose k) == (n-1 choose k-1) * n / k
        if (n <= 61) {
            // For n <= 61, the naive implementation cannot overflow.
            long result = 1;
            for (int i = n - kMod + 1; i <= n; i++) {
                result *= i;
            }
            for (int i = 2; i <= kMod; i++) {
                result /= i;
            }
            return result;
        } else if (n <= 66) {
            // For n > 61 but n <= 66, the result cannot overflow,
            // but we must take care not to overflow intermediate values.
            long result = 1;
            for (int i = 1; i <= kMod; i++) {
                // We know that (result * i) is divisible by j,
                // but (result * i) may overflow, so we split j:
                // Filter out the gcd, d, so j/d and i/d are integer.
                // result is divisible by (j/d) because (j/d)
                // is relative prime to (i/d) and is a divisor of
                // result * (i/d).
                long d = gcd(i, n - kMod + i);
                result /= (i / d);
                result *= ((n - kMod + i) / d);
            }
            return result;
        } else {
            // For n > 66, a result overflow might occur, so we check
            // the multiplication, taking care to not overflow
            // unnecessary.
            long result = 1;
            for (int i = 1; i <= kMod; i++) {
                long d = gcd(i, n - kMod + i);
                result /= (i / d);
                long temp = result * ((n - kMod + i) / d);
                if (temp / result != ((n - kMod + i) / d)) {
                    throw new ArithmeticException(
                        "result too large to represent in a long integer");
                }
                result = temp;
            }
            return result;
        }
    }