public static long binomialCoefficient(final int n, final int k) {
        if (n < k) {
            throw new IllegalArgumentException(
                "must have n >= k for binomial coefficient (n,k)");
        }
        if (n < 0) {
            throw new IllegalArgumentException(
                "must have n >= 0 for binomial coefficient (n,k)");
        }
        final int k2 = Math.min(k, n - k);
        if (k2 == 0) {
            return 1;
        }
        if (k2 == 1) {
            return n;
        }
        if (n <= 66) {
            // compute exactly using BigInteger
            java.math.BigInteger result = java.math.BigInteger.ONE;
            for (int i = 1; i <= k2; i++) {
                java.math.BigInteger numerator = java.math.BigInteger.valueOf(n - k2 + i);
                java.math.BigInteger denominator = java.math.BigInteger.valueOf(i);
                result = result.multiply(numerator).divide(denominator);
            }
            if (result.compareTo(java.math.BigInteger.valueOf(Long.MAX_VALUE)) > 0) {
                throw new ArithmeticException(
                    "result too large to represent in a long integer");
            }
            return result.longValue();
        } else {
            long result = Math.round(binomialCoefficientDouble(n, k));
            if (result == Long.MAX_VALUE) {
                throw new ArithmeticException(
                    "result too large to represent in a long integer");
            }
            return result;
        }
    }