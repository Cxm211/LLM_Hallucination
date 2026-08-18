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

        // Use symmetry to reduce k
        final int m = Math.min(k, n - k);

        long result = 1L;
        if (n <= 61) {
            // For n <= 61, straightforward multiplicative formula is safe
            for (int i = 1; i <= m; i++) {
                result = (result * (n - m + i)) / i;
            }
            return result;
        } else if (n <= 66) {
            // For 62 <= n <= 66, the result fits in long but intermediates may overflow
            // Reduce at each step using gcd to keep intermediates small
            for (int i = 1; i <= m; i++) {
                long numerator = n - m + i;
                long denominator = i;

                // gcd reduction between numerator and denominator
                long a = numerator;
                long b = denominator;
                while (b != 0) {
                    long t = a % b;
                    a = b;
                    b = t;
                }
                long d = Math.abs(a);
                numerator /= d;
                denominator /= d;

                // Now divide result by remaining denominator (must divide exactly)
                a = result;
                b = denominator;
                while (b != 0) {
                    long t = a % b;
                    a = b;
                    b = t;
                }
                long d2 = Math.abs(a);
                result /= d2;
                denominator /= d2;

                // At this point, denominator should be 1
                // Safe multiply as final result still fits in long for n <= 66
                result *= numerator;
            }
            return result;
        } else {
            // For n > 66, overflow may occur; carefully check multiplication
            for (int i = 1; i <= m; i++) {
                long numerator = n - m + i;
                long denominator = i;

                // Reduce numerator/denominator by their gcd first
                long a = numerator;
                long b = denominator;
                while (b != 0) {
                    long t = a % b;
                    a = b;
                    b = t;
                }
                long d = Math.abs(a);
                numerator /= d;
                denominator /= d;

                // Further reduce denominator with current result to avoid overflow
                a = result;
                b = denominator;
                while (b != 0) {
                    long t = a % b;
                    a = b;
                    b = t;
                }
                long d2 = Math.abs(a);
                result /= d2;
                denominator /= d2;

                // denominator should now be 1
                if (denominator != 1L) {
                    // As a safety net, though mathematically this should not occur
                    // If it does, continuing would risk incorrect result
                    throw new ArithmeticException(
                        "result too large to represent in a long integer");
                }

                // Check for multiplication overflow: result * numerator
                if (numerator != 0 && result > Long.MAX_VALUE / numerator) {
                    throw new ArithmeticException(
                        "result too large to represent in a long integer");
                }
                result *= numerator;
            }
            return result;
        }
    }