public final Object computeValue(EvalContext context) {
    Object l = args[0].computeValue(context);
    Object r = args[1].computeValue(context);
    if (l == null || r == null) {
        return Boolean.FALSE;
    }
    return compute(l, r) ? Boolean.TRUE : Boolean.FALSE;
}