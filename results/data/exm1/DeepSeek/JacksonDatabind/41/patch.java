public JavaType constructType(Type type, Class<?> contextClass) {
        if (contextClass == null) {
            return _fromAny(null, type, null);
        }
        return constructType(type, constructType(contextClass));
    }