public JavaType constructType(Type type, Class<?> contextClass) {
        return constructType(type, (contextClass == null) ? null : constructType(contextClass));
    }

    public JavaType constructType(Type type, JavaType contextType) {
        TypeBindings bindings = (contextType == null) ? TypeBindings.emptyBindings() : contextType.getBindings();
        return _fromAny(null, type, bindings);
    }