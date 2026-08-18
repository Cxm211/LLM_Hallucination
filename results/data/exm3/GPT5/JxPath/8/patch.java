private boolean compute(Object left, Object right) {
        left = reduce(left);
        right = reduce(right);

        if (left instanceof InitialContext) {
            ((InitialContext) left).reset();
        }
        if (right instanceof InitialContext) {
            ((InitialContext) right).reset();
        }
        if (left instanceof Iterator && right instanceof Iterator) {
            return findMatch((Iterator) left, (Iterator) right);
        }
        if (left instanceof Iterator) {
            return containsMatch((Iterator) left, right);
        }
        if (right instanceof Iterator) {
            return containsMatch((Iterator) right, left);
        }
        double ld = InfoSetUtil.doubleValue(left);
        double rd = InfoSetUtil.doubleValue(right);
        // Handle NaN: comparisons with NaN should be false except NaN = NaN should be true
        boolean lNaN = Double.isNaN(ld);
        boolean rNaN = Double.isNaN(rd);
        if (lNaN || rNaN) {
            return lNaN && rNaN ? evaluateCompare(0) : false;
        }
        return evaluateCompare(ld == rd ? 0 : ld < rd ? -1 : 1);
    }