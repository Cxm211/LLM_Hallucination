public static CollectionType construct(Class<?> rawType, JavaType elemT) {
    TypeBindings bindings = TypeBindings.emptyBindings();
    JavaType superClass = null;
    JavaType[] superInterfaces = null;
    return new CollectionType(rawType, bindings,
            superClass, superInterfaces, elemT,
            null, null, false);
}