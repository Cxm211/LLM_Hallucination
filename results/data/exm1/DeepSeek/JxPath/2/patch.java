public Iterator iterate(EvalContext context) {
    Object result = compute(context);
    if (result instanceof EvalContext) {
        return (EvalContext) result;
    }
    return ValueUtils.iterate(result);
}