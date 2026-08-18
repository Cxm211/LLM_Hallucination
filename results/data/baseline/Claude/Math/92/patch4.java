private static long mulAndCheck(long a, long b) {
        if (a == 0 || b == 0) {
            return 0;
        }
        if (a > 0) {
            if (b > 0) {
                if (a > Long.MAX_VALUE / b) {
                    throw new ArithmeticException("result too large to represent in a long integer");
                }
            } else {
                if (b < Long.MIN_VALUE / a) {
                    throw new ArithmeticException("result too large to represent in a long integer");
                }
            }
        } else {
            if (b > 0) {
                if (a < Long.MIN_VALUE / b) {
                    throw new ArithmeticException("result too large to represent in a long integer");
                }
            } else {
                if (a != 0 && b < Long.MAX_VALUE / a) {
                    throw new ArithmeticException("result too large to represent in a long integer");
                }
            }
        }
        return a * b;
    }