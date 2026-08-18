    public Dfp multiply(final int x) {
        if (isNaN()) {
            return this;
        }
        if (x == 0) {
            if (isInfinite()) {
                field.setIEEEFlagsBits(DfpField.FLAG_INVALID);
                return getNaN();
            }
            return getZero().copySign(this);
        }
        if (isInfinite()) {
            Dfp result = getInfinity().copySign(this);
            if (x < 0) {
                result = result.negate();
            }
            return result;
        }
        return multiplyFast(x);
    }