protected Object functionRound(EvalContext context) {
        assertArgCount(1);
        double v = InfoSetUtil.doubleValue(getArg1().computeValue(context));
        if (Double.isNaN(v)) {
            return new Double(Double.NaN);
        }
        if (v == Double.POSITIVE_INFINITY) {
            return new Double(Double.POSITIVE_INFINITY);
        }
        if (v == Double.NEGATIVE_INFINITY) {
            return new Double(Double.NEGATIVE_INFINITY);
        }
        return new Double(Math.floor(v + 0.5d));
    }