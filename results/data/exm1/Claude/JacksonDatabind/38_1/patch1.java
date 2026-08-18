public static MapType construct(Class<?> rawType, JavaType keyT, JavaType valueT)
{
    TypeBindings bindings = TypeBindings.create(rawType, keyT, valueT);
    return new MapType(rawType, bindings, _findSuperType(rawType, Map.class, bindings), null,
            keyT, valueT, null, null, false);
}