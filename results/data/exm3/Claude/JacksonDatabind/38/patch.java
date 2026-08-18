public static CollectionType construct(Class<?> rawType, JavaType elemT) {
    TypeBindings bindings = TypeBindings.emptyBindings();
    return new CollectionType(rawType, bindings,
            _bogusSuperClass(rawType), null, elemT,
            null, null, false);
}