public static CollectionType construct(Class<?> rawType, JavaType elemT) {
        // First: may need to fabricate TypeBindings (needed for refining into
        // concrete collection types, as per [databind#1102])
        TypeBindings bindings = TypeBindings.create(rawType, elemT);
        return new CollectionType(rawType, bindings,
                _bogusSuperClass(rawType), null, elemT,
                null, null, false);
    }