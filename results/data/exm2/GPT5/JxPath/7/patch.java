    public Object computeValue(org.apache.commons.jxpath.ri.EvalContext context) {
        Object lv = args[0].computeValue(context);
        Object rv = args[1].computeValue(context);
        if (lv instanceof java.util.Collection) {
            java.util.Iterator it = ((java.util.Collection) lv).iterator();
            while (it.hasNext()) {
                double l = org.apache.commons.jxpath.ri.InfoSetUtil.doubleValue(it.next());
                double r = org.apache.commons.jxpath.ri.InfoSetUtil.doubleValue(rv);
                if (l > r) {
                    return Boolean.TRUE;
                }
            }
            return Boolean.FALSE;
        }
        if (rv instanceof java.util.Collection) {
            java.util.Iterator it = ((java.util.Collection) rv).iterator();
            while (it.hasNext()) {
                double l = org.apache.commons.jxpath.ri.InfoSetUtil.doubleValue(lv);
                double r = org.apache.commons.jxpath.ri.InfoSetUtil.doubleValue(it.next());
                if (l > r) {
                    return Boolean.TRUE;
                }
            }
            return Boolean.FALSE;
        }
        double l = org.apache.commons.jxpath.ri.InfoSetUtil.doubleValue(lv);
        double r = org.apache.commons.jxpath.ri.InfoSetUtil.doubleValue(rv);
        return l > r ? Boolean.TRUE : Boolean.FALSE;
    }