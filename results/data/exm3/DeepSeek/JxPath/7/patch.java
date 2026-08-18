    public Object computeValue(org.apache.commons.jxpath.ri.EvalContext context) {
        Object left = args[0].computeValue(context);
        Object right = args[1].computeValue(context);
        if (left instanceof java.util.Iterator && right instanceof java.util.Iterator) {
            java.util.Iterator leftIter = (java.util.Iterator) left;
            while (leftIter.hasNext()) {
                Object leftNode = leftIter.next();
                double l = org.apache.commons.jxpath.ri.InfoSetUtil.doubleValue(leftNode);
                Object rightObj = args[1].computeValue(context);
                if (rightObj instanceof java.util.Iterator) {
                    java.util.Iterator rightIter = (java.util.Iterator) rightObj;
                    while (rightIter.hasNext()) {
                        Object rightNode = rightIter.next();
                        double r = org.apache.commons.jxpath.ri.InfoSetUtil.doubleValue(rightNode);
                        if (l > r) {
                            return Boolean.TRUE;
                        }
                    }
                } else {
                    double r = org.apache.commons.jxpath.ri.InfoSetUtil.doubleValue(rightObj);
                    if (l > r) {
                        return Boolean.TRUE;
                    }
                }
            }
            return Boolean.FALSE;
        }
        else if (left instanceof java.util.Iterator) {
            java.util.Iterator iter = (java.util.Iterator) left;
            while (iter.hasNext()) {
                Object node = iter.next();
                double l = org.apache.commons.jxpath.ri.InfoSetUtil.doubleValue(node);
                double r = org.apache.commons.jxpath.ri.InfoSetUtil.doubleValue(right);
                if (l > r) {
                    return Boolean.TRUE;
                }
            }
            return Boolean.FALSE;
        }
        else if (right instanceof java.util.Iterator) {
            java.util.Iterator iter = (java.util.Iterator) right;
            while (iter.hasNext()) {
                Object node = iter.next();
                double l = org.apache.commons.jxpath.ri.InfoSetUtil.doubleValue(left);
                double r = org.apache.commons.jxpath.ri.InfoSetUtil.doubleValue(node);
                if (l > r) {
                    return Boolean.TRUE;
                }
            }
            return Boolean.FALSE;
        }
        else {
            double l = org.apache.commons.jxpath.ri.InfoSetUtil.doubleValue(left);
            double r = org.apache.commons.jxpath.ri.InfoSetUtil.doubleValue(right);
            return l > r ? Boolean.TRUE : Boolean.FALSE;
        }
    }