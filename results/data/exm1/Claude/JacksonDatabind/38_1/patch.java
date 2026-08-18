public static CollectionType construct(Class<?> rawType, JavaType elemT) {
    TypeBindings bindings = TypeBindings.create(rawType, elemT);
    return new CollectionType(rawType, bindings,
            _findSuperType(rawType, Collection.class, bindings), null, elemT,
            null, null, false);
}