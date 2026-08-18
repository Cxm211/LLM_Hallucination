public final Object computeValue(EvalContext context) {
        // Short-circuit for AND/OR to avoid unnecessary evaluation of the second argument
        if (this instanceof CoreOperationAnd) {
            Object left = args[0].computeValue(context);
            // For AND, overall result equals left when right is true
            boolean leftBool = compute(left, Boolean.TRUE);
            if (!leftBool) {
                return Boolean.FALSE;
            }
            Object right = args[1].computeValue(context);
            return compute(left, right) ? Boolean.TRUE : Boolean.FALSE;
        }
        if (this instanceof CoreOperationOr) {
            Object left = args[0].computeValue(context);
            // For OR, overall result equals left when right is false
            boolean leftBool = compute(left, Boolean.FALSE);
            if (leftBool) {
                return Boolean.TRUE;
            }
            Object right = args[1].computeValue(context);
            return compute(left, right) ? Boolean.TRUE : Boolean.FALSE;
        }
        return compute(args[0].computeValue(context), args[1].computeValue(context))
                ? Boolean.TRUE : Boolean.FALSE;
    }