public static CollectionType construct(Class<?> rawType, JavaType elemT) {
    JavaType superClass = (rawType.getSuperclass() == null) ? null : TypeFactory.defaultInstance().constructType(rawType.getSuperclass());
    JavaType[] superInterfaces = null;
    Class<?>[] interfaces = rawType.getInterfaces();
    if (interfaces != null && interfaces.length > 0) {
        superInterfaces = new JavaType[interfaces.length];
        for (int i = 0; i < interfaces.length; i++) {
            superInterfaces[i] = TypeFactory.defaultInstance().constructType(interfaces[i]);
        }
    }
    return new CollectionType(rawType, null, superClass, superInterfaces, elemT, null, null, false);
}