public static MapType construct(Class<?> rawType, JavaType keyT, JavaType valueT)
{
    TypeBindings bindings = TypeBindings.emptyBindings();
    return new MapType(rawType, bindings, _bogusSuperClass(rawType), null,
            keyT, valueT, null, null, false);
}