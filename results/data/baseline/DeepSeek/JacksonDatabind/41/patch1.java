public JavaType constructType(Type type, JavaType contextType) {
        if (contextType == null) {
            return _fromAny(null, type, null);
        }
        return _fromAny(null, type, contextType.getBindings());
    }