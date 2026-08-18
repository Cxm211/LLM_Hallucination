    protected Object functionRound(EvalContext context) {
        assertArgCount(1);
        double v = InfoSetUtil.doubleValue(getArg1().computeValue(context));
        if (Double.isNaN(v) || v == Double.POSITIVE_INFINITY || v == Double.NEGATIVE_INFINITY) {
            return new Double(v);
        }
        if (v == 0.0d) {
            return new Double(v);
        }
        double r = Math.floor(v + 0.5d);
        if (r == 0.0d) {
            return new Double(v < 0.0d ? -0.0d : 0.0d);
        }
        return new Double(r);
    }
