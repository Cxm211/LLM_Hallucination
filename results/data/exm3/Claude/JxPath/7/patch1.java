public Object computeValue(org.apache.commons.jxpath.ri.EvalContext context) {
    Object leftValue = args[0].computeValue(context);
    Object rightValue = args[1].computeValue(context);
    
    if (leftValue instanceof org.apache.commons.jxpath.NodeSet) {
        org.apache.commons.jxpath.NodeSet nodeSet = (org.apache.commons.jxpath.NodeSet) leftValue;
        java.util.Iterator iterator = nodeSet.getPointers().iterator();
        while (iterator.hasNext()) {
            Object nodeValue = ((org.apache.commons.jxpath.Pointer) iterator.next()).getValue();
            double l = org.apache.commons.jxpath.ri.InfoSetUtil.doubleValue(nodeValue);
            double r = org.apache.commons.jxpath.ri.InfoSetUtil.doubleValue(rightValue);
            if (l >= r) {
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
    } else if (rightValue instanceof org.apache.commons.jxpath.NodeSet) {
        org.apache.commons.jxpath.NodeSet nodeSet = (org.apache.commons.jxpath.NodeSet) rightValue;
        java.util.Iterator iterator = nodeSet.getPointers().iterator();
        while (iterator.hasNext()) {
            Object nodeValue = ((org.apache.commons.jxpath.Pointer) iterator.next()).getValue();
            double l = org.apache.commons.jxpath.ri.InfoSetUtil.doubleValue(leftValue);
            double r = org.apache.commons.jxpath.ri.InfoSetUtil.doubleValue(nodeValue);
            if (l >= r) {
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
    }
    
    double l = org.apache.commons.jxpath.ri.InfoSetUtil.doubleValue(leftValue);
    double r = org.apache.commons.jxpath.ri.InfoSetUtil.doubleValue(rightValue);
    return l >= r ? Boolean.TRUE : Boolean.FALSE;
}