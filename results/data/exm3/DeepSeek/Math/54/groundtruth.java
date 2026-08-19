// ===== FIXED org.apache.commons.math.dfp.Dfp :: Dfp [lines 181-187] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Math/Math-54-fixed/src/main/java/org/apache/commons/math/dfp/Dfp.java =====
    protected Dfp(final DfpField field) {
        mant = new int[field.getRadixDigits()];
        sign = 1;
        exp = 0;
        nans = FINITE;
        this.field = field;
    }

// ===== FIXED org.apache.commons.math.dfp.Dfp :: toDouble() [lines 2306-2390] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Math/Math-54-fixed/src/main/java/org/apache/commons/math/dfp/Dfp.java =====
    public double toDouble() {

        if (isInfinite()) {
            if (lessThan(getZero())) {
                return Double.NEGATIVE_INFINITY;
            } else {
                return Double.POSITIVE_INFINITY;
            }
        }

        if (isNaN()) {
            return Double.NaN;
        }

        Dfp y = this;
        boolean negate = false;
        int cmp0 = compare(this, getZero());
        if (cmp0 == 0) {
            return sign < 0 ? -0.0 : +0.0;
        } else if (cmp0 < 0) {
            y = negate();
            negate = true;
        }

        /* Find the exponent, first estimate by integer log10, then adjust.
         Should be faster than doing a natural logarithm.  */
        int exponent = (int)(y.log10() * 3.32);
        if (exponent < 0) {
            exponent--;
        }

        Dfp tempDfp = DfpMath.pow(getTwo(), exponent);
        while (tempDfp.lessThan(y) || tempDfp.equals(y)) {
            tempDfp = tempDfp.multiply(2);
            exponent++;
        }
        exponent--;

        /* We have the exponent, now work on the mantissa */

        y = y.divide(DfpMath.pow(getTwo(), exponent));
        if (exponent > -1023) {
            y = y.subtract(getOne());
        }

        if (exponent < -1074) {
            return 0;
        }

        if (exponent > 1023) {
            return negate ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
        }


        y = y.multiply(newInstance(4503599627370496l)).rint();
        String str = y.toString();
        str = str.substring(0, str.length()-1);
        long mantissa = Long.parseLong(str);

        if (mantissa == 4503599627370496L) {
            // Handle special case where we round up to next power of two
            mantissa = 0;
            exponent++;
        }

        /* Its going to be subnormal, so make adjustments */
        if (exponent <= -1023) {
            exponent--;
        }

        while (exponent < -1023) {
            exponent++;
            mantissa >>>= 1;
        }

        long bits = mantissa | ((exponent + 1023L) << 52);
        double x = Double.longBitsToDouble(bits);

        if (negate) {
            x = -x;
        }

        return x;

    }
