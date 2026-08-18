protected Object functionRound(EvalContext context) {
    assertArgCount(1);
    double v = InfoSetUtil.doubleValue(getArg1().computeValue(context));
    if (Double.isNaN(v)) {
        return new Double(Double.NaN);
    } else if (Double.isInfinite(v)) {
        return new Double(v);
    } else if (v == 0.0) {
        return new Double(v);
    }
    return new Double(Math.round(v));
}