public JavaType constructType(Type type, JavaType contextType) {
        if (contextType == null) {
            return constructType(type);
        }
        return _fromAny(null, type, contextType.getBindings());
    }