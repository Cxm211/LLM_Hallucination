public Object computeValue(org.apache.commons.jxpath.ri.EvalContext context) {
    Object left = args[0].computeValue(context);
    Object right = args[1].computeValue(context);
    if (left instanceof org.apache.commons.jxpath.NodeSet) {
        org.apache.commons.jxpath.NodeSet leftSet = (org.apache.commons.jxpath.NodeSet) left;
        java.util.Iterator leftIter = leftSet.iterator();
        if (right instanceof org.apache.commons.jxpath.NodeSet) {
            org.apache.commons.jxpath.NodeSet rightSet = (org.apache.commons.jxpath.NodeSet) right;
            java.util.Iterator rightIter;
            while (leftIter.hasNext()) {
                Object leftNode = leftIter.next();
                double l = org.apache.commons.jxpath.ri.InfoSetUtil.doubleValue(leftNode);
                rightIter = rightSet.iterator();
                while (rightIter.hasNext()) {
                    Object rightNode = rightIter.next();
                    double r = org.apache.commons.jxpath.ri.InfoSetUtil.doubleValue(rightNode);
                    if (l <= r) {
                        return Boolean.TRUE;
                    }
                }
            }
            return Boolean.FALSE;
        } else {
            double r = org.apache.commons.jxpath.ri.InfoSetUtil.doubleValue(right);
            while (leftIter.hasNext()) {
                Object leftNode = leftIter.next();
                double l = org.apache.commons.jxpath.ri.InfoSetUtil.doubleValue(leftNode);
                if (l <= r) {
                    return Boolean.TRUE;
                }
            }
            return Boolean.FALSE;
        }
    } else if (right instanceof org.apache.commons.jxpath.NodeSet) {
        org.apache.commons.jxpath.NodeSet rightSet = (org.apache.commons.jxpath.NodeSet) right;
        java.util.Iterator rightIter = rightSet.iterator();
        double l = org.apache.commons.jxpath.ri.InfoSetUtil.doubleValue(left);
        while (rightIter.hasNext()) {
            Object rightNode = rightIter.next();
            double r = org.apache.commons.jxpath.ri.InfoSetUtil.doubleValue(rightNode);
            if (l <= r) {
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
    } else {
        double l = org.apache.commons.jxpath.ri.InfoSetUtil.doubleValue(left);
        double r = org.apache.commons.jxpath.ri.InfoSetUtil.doubleValue(right);
        return l <= r ? Boolean.TRUE : Boolean.FALSE;
    }
}