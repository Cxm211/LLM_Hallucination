public Object computeValue(org.apache.commons.jxpath.ri.EvalContext context) {
        // Ensure NaN comparisons always return false, even for "!="
        Object l = args[0] != null ? args[0].computeValue(context) : null;
        Object r = args[1] != null ? args[1].computeValue(context) : null;
        if ((l instanceof Number || r instanceof Number)) {
            double dl = InfoSetUtil.doubleValue(l);
            double dr = InfoSetUtil.doubleValue(r);
            if (Double.isNaN(dl) || Double.isNaN(dr)) {
                return Boolean.FALSE;
            }
        }
        return equal(context, args[0], args[1]) ? Boolean.FALSE : Boolean.TRUE;
    }