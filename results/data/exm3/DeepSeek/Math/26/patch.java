    private Fraction(double value, double epsilon, int maxDenominator, int maxIterations)
        throws FractionConversionException
    {
        long overflow = Integer.MAX_VALUE;
        long minInt = Integer.MIN_VALUE;
        double r0 = value;
        long a0 = (long)FastMath.floor(r0);
        if (a0 > overflow || a0 < minInt) {
            throw new FractionConversionException(value, a0, 1l);
        }

        // check for (almost) integer arguments, which should not go
        // to iterations.
        if (FastMath.abs(a0 - value) < epsilon) {
            this.numerator = (int) a0;
            this.denominator = 1;
            return;
        }

        long p0 = 1;
        long q0 = 0;
        long p1 = a0;
        long q1 = 1;

        long p2 = 0;
        long q2 = 1;

        int n = 0;
        boolean stop = false;
        do {
            ++n;
            double r1 = 1.0 / (r0 - a0);
            long a1 = (long)FastMath.floor(r1);
            
            // Compute p2 = a1 * p1 + p0 with overflow checks
            long productP;
            if (a1 == 0) {
                productP = 0;
            } else {
                // Check multiplication overflow for a1 * p1
                if (a1 > 0) {
                    if (p1 > 0) {
                        if (a1 > Long.MAX_VALUE / p1) {
                            throw new FractionConversionException(value, a1, p1);
                        }
                    } else if (p1 < 0) {
                        // a1 positive, p1 negative
                        if (a1 > Long.MIN_VALUE / p1) {
                            throw new FractionConversionException(value, a1, p1);
                        }
                    }
                } else {
                    // a1 < 0 (should not happen, but handle)
                    if (a1 < 0) {
                        if (p1 > 0) {
                            if (a1 < Long.MIN_VALUE / p1) {
                                throw new FractionConversionException(value, a1, p1);
                            }
                        } else if (p1 < 0) {
                            if (a1 < Long.MAX_VALUE / p1) {
                                throw new FractionConversionException(value, a1, p1);
                            }
                        }
                    }
                }
                productP = a1 * p1;
            }
            // Check addition overflow for productP + p0
            if (productP > 0) {
                if (p0 > Long.MAX_VALUE - productP) {
                    throw new FractionConversionException(value, a1, p1);
                }
            } else if (productP < 0) {
                if (p0 < Long.MIN_VALUE - productP) {
                    throw new FractionConversionException(value, a1, p1);
                }
            }
            p2 = productP + p0;
            
            // Compute q2 = a1 * q1 + q0 with overflow checks
            long productQ;
            if (a1 == 0) {
                productQ = 0;
            } else {
                // a1 >= 0 and q1 > 0
                if (a1 > Long.MAX_VALUE / q1) {
                    throw new FractionConversionException(value, a1, q1);
                }
                productQ = a1 * q1;
            }
            // q0 >= 0 and productQ >= 0
            if (productQ > Long.MAX_VALUE - q0) {
                throw new FractionConversionException(value, a1, q1);
            }
            q2 = productQ + q0;
            
            // Check if p2 and q2 fit into int range
            if (p2 > overflow || p2 < minInt || q2 > overflow) {
                throw new FractionConversionException(value, p2, q2);
            }

            double convergent = (double)p2 / (double)q2;
            if (n < maxIterations && FastMath.abs(convergent - value) > epsilon && q2 < maxDenominator) {
                p0 = p1;
                p1 = p2;
                q0 = q1;
                q1 = q2;
                a0 = a1;
                r0 = r1;
            } else {
                stop = true;
            }
        } while (!stop);

        if (n >= maxIterations) {
            throw new FractionConversionException(value, maxIterations);
        }

        if (q2 < maxDenominator) {
            this.numerator = (int) p2;
            this.denominator = (int) q2;
        } else {
            this.numerator = (int) p1;
            this.denominator = (int) q1;
        }

    }