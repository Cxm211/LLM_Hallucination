public static MapType construct(Class<?> rawType, JavaType keyT, JavaType valueT) {
    JavaType base = TypeFactory.defaultInstance().constructType(rawType);
    return new MapType(rawType, base.getBindings(),
            base.getSuperClass(), base.getSuperInterfaces(),
            keyT, valueT, null, null, false);
}