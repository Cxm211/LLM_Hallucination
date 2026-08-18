public static long factorial(final int n) {
        if (n < 0) {
            throw new IllegalArgumentException("must have n >= 0 for n!");
        }
        if (n > 20) {
            throw new ArithmeticException("factorial value is too large to fit in a long");
        }
        long result = 1L;
        for (int i = 2; i <= n; i++) {
            result *= i;
        }
        return result;
    }