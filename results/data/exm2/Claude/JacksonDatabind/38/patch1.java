public static MapType construct(Class<?> rawType, JavaType keyT, JavaType valueT)
{
    TypeBindings bindings = TypeBindings.emptyBindings();
    if (keyT != null && valueT != null) {
        bindings = TypeBindings.create(rawType, keyT, valueT);
    }
    return new MapType(rawType, bindings, _bogusSuperClass(rawType), null,
            keyT, valueT, null, null, false);
}