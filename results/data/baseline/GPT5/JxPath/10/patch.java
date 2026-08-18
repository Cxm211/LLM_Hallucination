public final Object computeValue(EvalContext context) {
        Object v1 = args[0].computeValue(context);
        boolean rIfFalse = compute(v1, Boolean.FALSE);
        boolean rIfTrue = compute(v1, Boolean.TRUE);
        if (rIfFalse == rIfTrue) {
            return Boolean.valueOf(rIfFalse);
        }
        Object v2 = args[1].computeValue(context);
        return Boolean.valueOf(compute(v1, v2));
    }