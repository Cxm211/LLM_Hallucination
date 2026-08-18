public static CollectionType construct(Class<?> rawType, JavaType elemT) {
    TypeBindings bindings = TypeBindings.emptyBindings();
    if (elemT != null) {
        bindings = TypeBindings.create(rawType, elemT);
    }
    return new CollectionType(rawType, bindings,
            _bogusSuperClass(rawType), null, elemT,
            null, null, false);
}