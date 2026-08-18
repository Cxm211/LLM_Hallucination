    public static int gcd(final int p, final int q) {
        int u = p;
        int v = q;
        if ((u == 0) || (v == 0)) {
            return (Math.abs(u) + Math.abs(v));
        }
        // keep u and v negative, as negative integers range down to
        // -2^31, while positive numbers can only be as large as 2^31-1
        // (i.e. we can't necessarily negate a negative number without
        // overflow)
        /* assert u!=0 && v!=0; */
        if (u > 0) {
            u = -u;
        } // make u negative
        if (v > 0) {
            v = -v;
        } // make v negative
        // B1. [Find power of 2]
        int k = 0;
        while ((u & 1) == 0 && (v & 1) == 0 && k < 31) { // while u and v are
                                                            // both even...
            u /= 2;
            v /= 2;
            k++; // cast out twos.
        }
        if (k == 31) {
            throw MathRuntimeException.createArithmeticException(
                    "overflow: gcd({0}, {1}) is 2^31",
                    new Object[] { p, q });
        }
        // B2. Initialize: u and v have been divided by 2^k and at least
        // one is odd.
        int t = ((u & 1) == 1) ? v : -(u / 2)/* B3 */;
        // t negative: u was odd, v may be even (t replaces v)
        // t positive: u was even, v is odd (t replaces u)
        do {
            /* assert u<0 && v<0; */
            // B4/B3: cast out twos from t.
            while ((t & 1) == 0) { // while t is even..
                t /= 2; // cast out twos
            }
            // B5 [reset max(u,v)]
            if (t > 0) {
                u = -t;
            } else {
                v = t;
            }
            // B6/B3. at this point both u and v should be odd.
            t = (v - u) / 2;
            // |u| larger: t positive (replace u)
            // |v| larger: t negative (replace v)
        } while (t != 0);
        return -u * (1 << k); // gcd is u*2^k
    }

    public static int lcm(int a, int b) {
        if (a==0 || b==0){
            return 0;
        }
        int lcm = Math.abs(mulAndCheck(a / gcd(a, b), b));
        return lcm;
    }

// trigger testcase
public void testGcd() {
        int a = 30;
        int b = 50;
        int c = 77;

        assertEquals(0, MathUtils.gcd(0, 0));

        assertEquals(b, MathUtils.gcd(0, b));
        assertEquals(a, MathUtils.gcd(a, 0));
        assertEquals(b, MathUtils.gcd(0, -b));
        assertEquals(a, MathUtils.gcd(-a, 0));

        assertEquals(10, MathUtils.gcd(a, b));
        assertEquals(10, MathUtils.gcd(-a, b));
        assertEquals(10, MathUtils.gcd(a, -b));
        assertEquals(10, MathUtils.gcd(-a, -b));

        assertEquals(1, MathUtils.gcd(a, c));
        assertEquals(1, MathUtils.gcd(-a, c));
        assertEquals(1, MathUtils.gcd(a, -c));
        assertEquals(1, MathUtils.gcd(-a, -c));

        assertEquals(3 * (1<<15), MathUtils.gcd(3 * (1<<20), 9 * (1<<15)));

        assertEquals(Integer.MAX_VALUE, MathUtils.gcd(Integer.MAX_VALUE, 0));
        assertEquals(Integer.MAX_VALUE, MathUtils.gcd(-Integer.MAX_VALUE, 0));
        assertEquals(1<<30, MathUtils.gcd(1<<30, -Integer.MIN_VALUE));
        try {
            // gcd(Integer.MIN_VALUE, 0) > Integer.MAX_VALUE
            MathUtils.gcd(Integer.MIN_VALUE, 0);
            fail("expecting ArithmeticException");
        } catch (ArithmeticException expected) {
            // expected
        }
        try {
            // gcd(0, Integer.MIN_VALUE) > Integer.MAX_VALUE
            MathUtils.gcd(0, Integer.MIN_VALUE);
            fail("expecting ArithmeticException");
        } catch (ArithmeticException expected) {
            // expected
        }
        try {
            // gcd(Integer.MIN_VALUE, Integer.MIN_VALUE) > Integer.MAX_VALUE
            MathUtils.gcd(Integer.MIN_VALUE, Integer.MIN_VALUE);
            fail("expecting ArithmeticException");
        } catch (ArithmeticException expected) {
            // expected
        }
    }

public void testLcm() {
        int a = 30;
        int b = 50;
        int c = 77;

        assertEquals(0, MathUtils.lcm(0, b));
        assertEquals(0, MathUtils.lcm(a, 0));
        assertEquals(b, MathUtils.lcm(1, b));
        assertEquals(a, MathUtils.lcm(a, 1));
        assertEquals(150, MathUtils.lcm(a, b));
        assertEquals(150, MathUtils.lcm(-a, b));
        assertEquals(150, MathUtils.lcm(a, -b));
        assertEquals(150, MathUtils.lcm(-a, -b));
        assertEquals(2310, MathUtils.lcm(a, c));

        // Assert that no intermediate value overflows:
        // The naive implementation of lcm(a,b) would be (a*b)/gcd(a,b)
        assertEquals((1<<20)*15, MathUtils.lcm((1<<20)*3, (1<<20)*5));

        // Special case
        assertEquals(0, MathUtils.lcm(0, 0));

        try {
            // lcm == abs(MIN_VALUE) cannot be represented as a nonnegative int
            MathUtils.lcm(Integer.MIN_VALUE, 1);
            fail("Expecting ArithmeticException");
        } catch (ArithmeticException ex) {
            // expected
        }
        
        try {
            // lcm == abs(MIN_VALUE) cannot be represented as a nonnegative int
            MathUtils.lcm(Integer.MIN_VALUE, 1<<20);
            fail("Expecting ArithmeticException");
        } catch (ArithmeticException ex) {
            // expected
        }

        try {
            MathUtils.lcm(Integer.MAX_VALUE, Integer.MAX_VALUE - 1);
            fail("Expecting ArithmeticException");
        } catch (ArithmeticException ex) {
            // expected
        }
    }
