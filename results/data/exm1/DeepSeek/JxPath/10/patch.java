public final Object computeValue(EvalContext context) {
    Object left = args[0].computeValue(context);
    Object right = args[1].computeValue(context);
    if ((left instanceof Double && Double.isNaN((Double) left)) ||
        (right instanceof Double && Double.isNaN((Double) right))) {
        return Boolean.FALSE;
    }
    if ((left instanceof NodeSet && !((NodeSet) left).iterate().hasNext()) ||
        (right instanceof NodeSet && !((NodeSet) right).iterate().hasNext())) {
        return Boolean.FALSE;
    }
    return compute(left, right) ? Boolean.TRUE : Boolean.FALSE;
}