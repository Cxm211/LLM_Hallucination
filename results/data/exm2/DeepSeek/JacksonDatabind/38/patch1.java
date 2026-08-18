    public static MapType construct(Class<?> rawType, JavaType keyT, JavaType valueT)
    {
        // First: may need to fabricate TypeBindings (needed for refining into
        // concrete collection types, as per [databind#1102])
        TypeBindings bindings = TypeBindings.create(rawType, new JavaType[]{keyT, valueT});
        return new MapType(rawType, bindings, _bogusSuperClass(rawType), null,
                keyT, valueT, null, null, false);
    }