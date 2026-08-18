private boolean compute(Object left, Object right) {
        // Ensure any InitialContext is reset before reduction to avoid side effects during reduce
        if (left instanceof InitialContext) {
            ((InitialContext) left).reset();
        }
        if (right instanceof InitialContext) {
            ((InitialContext) right).reset();
        }

        left = reduce(left);
        right = reduce(right);

        if (left instanceof Iterator && right instanceof Iterator) {
            return findMatch((Iterator) left, (Iterator) right);
        }
        if (left instanceof Iterator) {
            return containsMatch((Iterator) left, right);
        }
        if (right instanceof Iterator) {
            // Preserve operand order: compare left against each element of right
            Iterator it = (Iterator) right;
            while (it.hasNext()) {
                Object element = it.next();
                if (compute(left, element)) {
                    return true;
                }
            }
            return false;
        }
        double ld = InfoSetUtil.doubleValue(left);
        if (Double.isNaN(ld)) {
            return false;
        }
        double rd = InfoSetUtil.doubleValue(right);
        if (Double.isNaN(rd)) {
            return false;
        }
        return evaluateCompare(ld == rd ? 0 : ld < rd ? -1 : 1);
    }