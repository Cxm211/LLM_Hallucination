protected Object functionRound(EvalContext context) {
        assertArgCount(1);
        double v = InfoSetUtil.doubleValue(getArg1().computeValue(context));
        double rounded;
        if (v >= 0) {
            rounded = Math.floor(v + 0.5);
        } else {
            rounded = Math.ceil(v - 0.5);
        }
        return new Double(rounded);
    }