public Object computeValue(org.apache.commons.jxpath.ri.EvalContext context) {
        double l = org.apache.commons.jxpath.ri.InfoSetUtil.doubleValue(args[0].computeValue(context));
        double r = org.apache.commons.jxpath.ri.InfoSetUtil.doubleValue(args[1].computeValue(context));
        return Double.isNaN(l) || Double.isNaN(r) ? Boolean.FALSE : (l <= r ? Boolean.TRUE : Boolean.FALSE);
    }