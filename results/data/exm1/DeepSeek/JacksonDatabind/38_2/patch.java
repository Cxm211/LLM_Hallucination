public static CollectionType construct(Class<?> rawType, JavaType elemT) {
    JavaType base = TypeFactory.defaultInstance().constructType(rawType);
    return new CollectionType(rawType, base.getBindings(),
            base.getSuperClass(), base.getSuperInterfaces(),
            elemT, null, null, false);
}