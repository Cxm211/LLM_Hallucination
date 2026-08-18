public static MapType construct(Class<?> rawType, JavaType keyT, JavaType valueT)
    {
        // First: may need to fabricate TypeBindings (needed for refining into
        // concrete collection types, as per [databind#1102])
        // !!! TODO: Wrong, does have supertypes
        TypeBindings bindings = TypeBindings.create(rawType, keyT, valueT);
        return new MapType(rawType, bindings, _bogusSuperClass(rawType), null,
                keyT, valueT, null, null, false);
    }