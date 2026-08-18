public JavaType constructType(Type type, Class<?> contextClass) {
    if (contextClass == null) {
        return constructType(type);
    }
    return constructType(type, constructType(contextClass));
}