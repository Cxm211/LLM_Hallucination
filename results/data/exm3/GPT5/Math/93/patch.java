public static long factorial(final int n) {
        if (n < 0) {
            throw new IllegalArgumentException("must have n >= 0 for n!");
        }
        long result = 1L;
        for (int i = 2; i <= n; i++) {
            long prev = result;
            result *= i;
            if (result / i != prev) {
                throw new ArithmeticException(
                        "factorial value is too large to fit in a long");
            }
        }
        return result;
    }