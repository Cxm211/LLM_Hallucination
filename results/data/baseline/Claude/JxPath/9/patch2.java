public Object computeValue(org.apache.commons.jxpath.ri.EvalContext context) {
    return equal(args[0].computeValue(context), args[1].computeValue(context)) ? Boolean.TRUE : Boolean.FALSE;
}