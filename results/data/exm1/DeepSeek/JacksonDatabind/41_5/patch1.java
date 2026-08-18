public JavaType constructType(Type type, JavaType contextType) {
    if (contextType == null || contextType.getBindings() == null) {
        return _fromAny(null, type, TypeBindings.emptyBindings());
    }
    return _fromAny(null, type, contextType.getBindings());
}