public Iterator iteratePointers(EvalContext context) {
        Object result = compute(context);
        if (result == null) {
            return Collections.EMPTY_LIST.iterator();
        }
        if (result instanceof EvalContext) {
            return (EvalContext) result;
        }
        if (result instanceof org.apache.commons.jxpath.NodeSet) {
            return ((org.apache.commons.jxpath.NodeSet) result).getPointers().iterator();
        }
        return new PointerIterator(
                ValueUtils.iterate(result),
                new QName(null, "value"),
                context.getRootContext().getCurrentNodePointer().getLocale());
    }