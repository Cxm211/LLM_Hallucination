public Object computeValue(org.apache.commons.jxpath.ri.EvalContext context) {
        Object left = args[0].computeValue(context);
        Object right = args[1].computeValue(context);

        if (left instanceof org.apache.commons.jxpath.NodeSet) {
            java.util.List values = ((org.apache.commons.jxpath.NodeSet) left).getValues();
            for (Object lv : (java.util.List<Object>) values) {
                double l = org.apache.commons.jxpath.ri.InfoSetUtil.doubleValue(lv);
                if (right instanceof org.apache.commons.jxpath.NodeSet) {
                    java.util.List rvalues = ((org.apache.commons.jxpath.NodeSet) right).getValues();
                    for (Object rv : (java.util.List<Object>) rvalues) {
                        double r = org.apache.commons.jxpath.ri.InfoSetUtil.doubleValue(rv);
                        if (l >= r) {
                            return Boolean.TRUE;
                        }
                    }
                } else {
                    double r = org.apache.commons.jxpath.ri.InfoSetUtil.doubleValue(right);
                    if (l >= r) {
                        return Boolean.TRUE;
                    }
                }
            }
            return Boolean.FALSE;
        }
        if (right instanceof org.apache.commons.jxpath.NodeSet) {
            java.util.List rvalues = ((org.apache.commons.jxpath.NodeSet) right).getValues();
            double l = org.apache.commons.jxpath.ri.InfoSetUtil.doubleValue(left);
            for (Object rv : (java.util.List<Object>) rvalues) {
                double r = org.apache.commons.jxpath.ri.InfoSetUtil.doubleValue(rv);
                if (l >= r) {
                    return Boolean.TRUE;
                }
            }
            return Boolean.FALSE;
        }

        double l = org.apache.commons.jxpath.ri.InfoSetUtil.doubleValue(left);
        double r = org.apache.commons.jxpath.ri.InfoSetUtil.doubleValue(right);
        return l >= r ? Boolean.TRUE : Boolean.FALSE;
    }