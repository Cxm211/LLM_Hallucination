protected boolean equal(org.apache.commons.jxpath.ri.EvalContext context, Expression arg1, Expression arg2) {
    Object l = arg1.computeValue(context);
    Object r = arg2.computeValue(context);
    if (l instanceof Pointer && r instanceof Pointer) {
        if (l.equals(r)) {
            return true;
        }
    }
    if (l instanceof Pointer) {
        l = ((Pointer) l).getValue();
    }

    if (r instanceof Pointer) {
        r = ((Pointer) r).getValue();
    }

    if (l == r) {
        return true;
    }
    if (l instanceof Boolean || r instanceof Boolean) {
        return (InfoSetUtil.booleanValue(l) == InfoSetUtil.booleanValue(r));
    }
    if (l instanceof Number || r instanceof Number) {
        return (InfoSetUtil.doubleValue(l) == InfoSetUtil.doubleValue(r));
    }
    if (l instanceof String || r instanceof String) {
        return (InfoSetUtil.stringValue(l).equals(InfoSetUtil.stringValue(r)));
    }
    return l != null && l.equals(r);
}