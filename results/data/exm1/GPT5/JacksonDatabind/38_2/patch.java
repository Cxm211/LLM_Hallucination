public static CollectionType construct(Class<?> rawType, JavaType elemT) {
        // First: create proper TypeBindings for collection element
        TypeBindings bindings = TypeBindings.createIfNeeded(rawType, elemT);
        return new CollectionType(rawType, bindings,
                null, null, elemT,
                null, null, false);
    }