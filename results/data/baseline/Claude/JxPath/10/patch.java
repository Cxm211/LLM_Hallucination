public final Object computeValue(EvalContext context) {
    Object left = args[0].computeValue(context);
    Object right = args[1].computeValue(context);
    return compute(left, right) ? Boolean.TRUE : Boolean.FALSE;
}