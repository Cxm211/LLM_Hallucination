public Object computeValue(org.apache.commons.jxpath.ri.EvalContext context) {
    return equal(context, args[0], args[1]) ? Boolean.FALSE : Boolean.TRUE;
}