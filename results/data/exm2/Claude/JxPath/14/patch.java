protected Object functionFloor(EvalContext context) {
    assertArgCount(1);
    double v = InfoSetUtil.doubleValue(getArg1().computeValue(context));
    if (Double.isNaN(v)) {
        return new Double(Double.NaN);
    } else if (Double.isInfinite(v)) {
        return new Double(v);
    }
    return new Double(Math.floor(v));
}