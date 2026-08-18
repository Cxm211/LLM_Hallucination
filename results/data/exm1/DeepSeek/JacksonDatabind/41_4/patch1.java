public JavaType constructType(Type type, JavaType contextType) {
    TypeBindings bindings = contextType.getBindings();
    if (bindings == null) {
        bindings = TypeBindings.emptyBindings();
    }
    return _fromAny(null, type, bindings);
}