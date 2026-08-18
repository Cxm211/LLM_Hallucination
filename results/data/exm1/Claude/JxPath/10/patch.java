public final Object computeValue(EvalContext context) {
    Object left = args[0].computeValue(context);
    Object right = args[1].computeValue(context);
    
    // Check for NaN values
    boolean leftIsNaN = false;
    boolean rightIsNaN = false;
    
    if (left instanceof Number) {
        double leftVal = ((Number) left).doubleValue();
        if (Double.isNaN(leftVal)) {
            leftIsNaN = true;
        }
    }
    
    if (right instanceof Number) {
        double rightVal = ((Number) right).doubleValue();
        if (Double.isNaN(rightVal)) {
            rightIsNaN = true;
        }
    }
    
    // If either operand is NaN, comparison should return false
    if (leftIsNaN || rightIsNaN) {
        return Boolean.FALSE;
    }
    
    return compute(left, right) ? Boolean.TRUE : Boolean.FALSE;
}