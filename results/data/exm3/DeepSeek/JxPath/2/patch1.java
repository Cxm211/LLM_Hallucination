    public Iterator iteratePointers(EvalContext context) {
        Object result = compute(context);
        if (result == null) {
            return Collections.EMPTY_LIST.iterator();
        }
        if (result instanceof EvalContext) {
            return (EvalContext) result;
        }
        NodePointer current = context.getRootContext().getCurrentNodePointer();
        Locale locale = current != null ? current.getLocale() : null;
        return new PointerIterator(ValueUtils.iterate(result),
                new QName(null, "value"),
                locale);
    }