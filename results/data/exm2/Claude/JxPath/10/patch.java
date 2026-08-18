public final Object computeValue(EvalContext context) {
    Object left = args[0].computeValue(context);
    Object right = args[1].computeValue(context);
    
    if (left instanceof java.util.List) {
        java.util.List<?> leftList = (java.util.List<?>) left;
        if (leftList.isEmpty()) {
            return Boolean.FALSE;
        }
    }
    
    if (right instanceof java.util.List) {
        java.util.List<?> rightList = (java.util.List<?>) right;
        if (rightList.isEmpty()) {
            return Boolean.FALSE;
        }
    }
    
    return compute(left, right) ? Boolean.TRUE : Boolean.FALSE;
}