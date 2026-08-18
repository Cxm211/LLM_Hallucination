public static MapType construct(Class<?> rawType, JavaType keyT, JavaType valueT)
    {
        // First: create proper TypeBindings for map key/value
        TypeBindings bindings = TypeBindings.createIfNeeded(rawType, keyT, valueT);
        return new MapType(rawType, bindings, null, null,
                keyT, valueT, null, null, false);
    }