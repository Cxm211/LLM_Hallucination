public JavaType constructType(Type type, Class<?> contextClass) {
    JavaType contextType = (contextClass == null) ? null : constructType(contextClass);
    return constructType(type, contextType);
}