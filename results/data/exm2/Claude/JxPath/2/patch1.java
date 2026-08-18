public Iterator iteratePointers(EvalContext context) {
    Object result = compute(context);
    if (result == null) {
        return Collections.EMPTY_LIST.iterator();
    }
    if (result instanceof EvalContext) {
        return new PointerIterator(
            (EvalContext) result,
            new QName(null, "value"),
            context.getRootContext().getCurrentNodePointer().getLocale());
    }
    return new PointerIterator(ValueUtils.iterate(result),
            new QName(null, "value"),
            context.getRootContext().getCurrentNodePointer().getLocale());
}