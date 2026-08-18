    protected Object functionRound(EvalContext context) {
        assertArgCount(1);
        double v = InfoSetUtil.doubleValue(getArg1().computeValue(context));
        if (Double.isNaN(v)) {
            return new Double(Double.NaN);
        }
        if (Double.isInfinite(v)) {
            return new Double(v);
        }
        if (v == 0.0) {
            return new Double(v);
        }
        double floor = Math.floor(v);
        double fraction = v - floor;
        if (fraction < 0.5) {
            return new Double(floor);
        } else if (fraction > 0.5) {
            return new Double(floor + 1.0);
        } else {
            // fraction == 0.5
            double result = floor + 1.0;
            if (result == 0.0 && v < 0.0) {
                return new Double(-0.0);
            }
            return new Double(result);
        }
    }