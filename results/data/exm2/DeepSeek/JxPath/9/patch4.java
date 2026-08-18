    public Object computeValue(org.apache.commons.jxpath.ri.EvalContext context) {
        Object l = args[0].computeValue(context);
        Object r = args[1].computeValue(context);
        // Unwrap pointers
        if (l instanceof Pointer) {
            l = ((Pointer) l).getValue();
        }
        if (r instanceof Pointer) {
            r = ((Pointer) r).getValue();
        }
        // Check for NaN
        if (l instanceof Number) {
            double dl = InfoSetUtil.doubleValue(l);
            if (Double.isNaN(dl)) {
                return Boolean.FALSE;
            }
        }
        if (r instanceof Number) {
            double dr = InfoSetUtil.doubleValue(r);
            if (Double.isNaN(dr)) {
                return Boolean.FALSE;
            }
        }
        return equal(l, r) ? Boolean.FALSE : Boolean.TRUE;
    }