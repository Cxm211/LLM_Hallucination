// buggy code
    public JavaType constructType(Type type, Class<?> contextClass) {
        return constructType(type, constructType(contextClass));
    }

    public JavaType constructType(Type type, JavaType contextType) {
        return _fromAny(null, type, contextType.getBindings());
    }

