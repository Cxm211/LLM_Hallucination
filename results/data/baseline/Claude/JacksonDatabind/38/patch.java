public static CollectionType construct(Class<?> rawType, JavaType elemT) {
        // First: may need to fabricate TypeBindings (needed for refining into
        // concrete collection types, as per [databind#1102])
        TypeBindings bindings = TypeBindings.emptyBindings();
        return new CollectionType(rawType, bindings,
                // !!! TODO: Wrong, does have supertypes, but:
                _bogusSuperClass(rawType), null, elemT,
                null, null, false);
    }