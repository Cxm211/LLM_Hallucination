    public static long factorial(final int n) {
        if (n < 0) {
            throw new IllegalArgumentException("must have n >= 0 for n!");
        }
        if (n < factorials.length) {
            return factorials[n];
        }
        long result = Math.round(factorialDouble(n));
        if (result == Long.MAX_VALUE) {
            throw new ArithmeticException(
                    "factorial value is too large to fit in a long");
        }
        return result;
    }