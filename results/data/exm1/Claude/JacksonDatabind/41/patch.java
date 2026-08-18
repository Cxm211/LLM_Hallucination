public JavaType constructType(Type type, Class<?> contextClass) {
    if (contextClass == null) {
        return constructType(type, (TypeBindings) null);
    }
    return constructType(type, constructType(contextClass));
}