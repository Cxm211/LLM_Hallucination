public Iterator iterate(EvalContext context) {
        Object result = compute(context);
        if (result instanceof EvalContext) {
            return new ValueIterator((EvalContext) result);
        }
        if (result instanceof org.apache.commons.jxpath.NodeSet) {
            return ((org.apache.commons.jxpath.NodeSet) result).getValues().iterator();
        }
        return ValueUtils.iterate(result);
    }