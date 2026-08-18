public Object computeValue(org.apache.commons.jxpath.ri.EvalContext context) {
        Object l = args[0].computeValue(context);
        Object r = args[1].computeValue(context);
        if (l instanceof Pointer) {
            l = ((Pointer) l).getValue();
        }
        if (r instanceof Pointer) {
            r = ((Pointer) r).getValue();
        }
        if (l instanceof Number || r instanceof Number) {
            double dl = InfoSetUtil.doubleValue(l);
            double dr = InfoSetUtil.doubleValue(r);
            if (Double.isNaN(dl) || Double.isNaN(dr)) {
                return Boolean.FALSE;
            }
        }
        return equal(l, r) ? Boolean.FALSE : Boolean.TRUE;
    }