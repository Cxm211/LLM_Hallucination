public Object computeValue(org.apache.commons.jxpath.ri.EvalContext context) {
        double l = org.apache.commons.jxpath.ri.InfoSetUtil.doubleValue(args[0].computeValue(context));
        double r = org.apache.commons.jxpath.ri.InfoSetUtil.doubleValue(args[1].computeValue(context));
        if (Double.isNaN(l) || Double.isNaN(r)) {
            return Boolean.FALSE;
        }
        return l >= r ? Boolean.TRUE : Boolean.FALSE;
    }