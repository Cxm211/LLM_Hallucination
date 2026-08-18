public Iterator iterate(EvalContext context) {
        Object result = compute(context);
        if (result instanceof EvalContext) {
            return new ValueIterator((EvalContext) result);
        }
        if (result instanceof NodeSet) {
            return ((NodeSet) result).getValues().iterator();
        }
        return ValueUtils.iterate(result);
    }