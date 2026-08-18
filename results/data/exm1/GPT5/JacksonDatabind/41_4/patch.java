public JavaType constructType(Type type, Class<?> contextClass) {
        return constructType(type, (contextClass == null) ? null : constructType(contextClass));
    }