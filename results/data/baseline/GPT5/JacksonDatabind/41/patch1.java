public JavaType constructType(Type type, JavaType contextType) {
        return _fromAny(null, type, (contextType == null) ? null : contextType.getBindings());
    }