public static MapType construct(Class<?> rawType, JavaType keyT, JavaType valueT)
    {
        // First: may need to fabricate TypeBindings (needed for refining into
        // concrete collection types, as per [databind#1102])
        return new MapType(rawType, TypeBindings.emptyBindings(), null, null,
                keyT, valueT, null, null, false);
    }