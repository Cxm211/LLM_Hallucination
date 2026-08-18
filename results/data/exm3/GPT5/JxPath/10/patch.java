public final Object computeValue(EvalContext context) {
        Object left = args[0].computeValue(context);
        if (left == null) {
            return Boolean.FALSE;
        }
        Object right = args[1].computeValue(context);
        if (right == null) {
            return Boolean.FALSE;
        }
        return compute(left, right) ? Boolean.TRUE : Boolean.FALSE;
    }