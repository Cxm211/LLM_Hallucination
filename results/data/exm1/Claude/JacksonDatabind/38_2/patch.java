public static CollectionType construct(Class<?> rawType, JavaType elemT) {
    TypeBindings bindings = TypeBindings.emptyBindings();
    JavaType superClass = _findSuperClass(rawType, bindings);
    return new CollectionType(rawType, bindings,
            superClass, null, elemT,
            null, null, false);
}