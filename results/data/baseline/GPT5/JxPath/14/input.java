// buggy code
    protected Object functionFloor(EvalContext context) {
        assertArgCount(1);
        double v = InfoSetUtil.doubleValue(getArg1().computeValue(context));
        return new Double(Math.floor(v));
    }

    protected Object functionCeiling(EvalContext context) {
        assertArgCount(1);
        double v = InfoSetUtil.doubleValue(getArg1().computeValue(context));
        return new Double(Math.ceil(v));
    }

    protected Object functionRound(EvalContext context) {
        assertArgCount(1);
        double v = InfoSetUtil.doubleValue(getArg1().computeValue(context));
        return new Double(Math.round(v));
    }

