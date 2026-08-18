public static MapType construct(Class<?> rawType, JavaType keyT, JavaType valueT) {
    JavaType superClass = (rawType.getSuperclass() == null) ? null : TypeFactory.defaultInstance().constructType(rawType.getSuperclass());
    JavaType[] superInterfaces = null;
    Class<?>[] interfaces = rawType.getInterfaces();
    if (interfaces != null && interfaces.length > 0) {
        superInterfaces = new JavaType[interfaces.length];
        for (int i = 0; i < interfaces.length; i++) {
            superInterfaces[i] = TypeFactory.defaultInstance().constructType(interfaces[i]);
        }
    }
    return new MapType(rawType, null, superClass, superInterfaces, keyT, valueT, null, null, false);
}