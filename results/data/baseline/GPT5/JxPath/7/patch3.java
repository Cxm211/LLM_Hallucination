public Object computeValue(org.apache.commons.jxpath.ri.EvalContext context) {
        double l = org.apache.commons.jxpath.ri.InfoSetUtil.doubleValue(args[0].computeValue(context));
        double r = org.apache.commons.jxpath.ri.InfoSetUtil.doubleValue(args[1].computeValue(context));
        String name = this.getClass().getSimpleName();
        boolean result;
        if ("CoreOperationGreaterThan".equals(name)) {
            result = l > r;
        } else if ("CoreOperationGreaterThanOrEqual".equals(name) || "CoreOperationGreaterOrEqual".equals(name)) {
            result = l >= r;
        } else if ("CoreOperationLessThan".equals(name)) {
            result = l < r;
        } else if ("CoreOperationLessThanOrEqual".equals(name) || "CoreOperationLessOrEqual".equals(name)) {
            result = l <= r;
        } else {
            result = false;
        }
        return result ? Boolean.TRUE : Boolean.FALSE;
    }