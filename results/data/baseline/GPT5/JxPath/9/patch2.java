public Object computeValue(org.apache.commons.jxpath.ri.EvalContext context) {
        Object l = args[0].computeValue(context);
        Object r = args[1].computeValue(context);
        return equal(l, r) ? Boolean.TRUE : Boolean.FALSE;
    }