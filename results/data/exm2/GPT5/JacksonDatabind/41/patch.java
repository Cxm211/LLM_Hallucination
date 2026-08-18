public JavaType constructType(Type type, Class<?> contextClass) {
    return constructType(type, (contextClass == null) ? (JavaType) null : constructType(contextClass));
}