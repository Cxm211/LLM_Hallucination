public JavaType constructType(Type type, JavaType contextType) {
    TypeBindings bindings;
    if (contextType == null) {
        bindings = TypeBindings.emptyBindings();
    } else {
        bindings = contextType.getBindings();
    }
    return _fromAny(null, type, bindings);
}