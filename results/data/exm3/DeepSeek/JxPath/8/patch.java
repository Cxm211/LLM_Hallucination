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
        int result;
        if (Double.isNaN(ld) || Double.isNaN(rd)) {
            if (this instanceof CoreOperationEqual) {
                result = (Double.isNaN(ld) && Double.isNaN(rd)) ? 0 : 1;
            } else {
                result = 0;
            }
        } else {
            result = ld == rd ? 0 : (ld < rd ? -1 : 1);
        }
        return evaluateCompare(result);
    }