public static int lcm(int a, int b) {
        if (a == 0 || b == 0) {
            return 0;
        }
        int g = gcd(a, b);
        int div = a / g;
        long l = Math.abs((long) div) * Math.abs((long) b);
        if (l > Integer.MAX_VALUE) {
            throw new ArithmeticException("overflow: lcm(" + a + ", " + b + ")");
        }
        return (int) l;
    }