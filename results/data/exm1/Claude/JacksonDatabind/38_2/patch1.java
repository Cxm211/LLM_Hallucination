public static MapType construct(Class<?> rawType, JavaType keyT, JavaType valueT)
{
    TypeBindings bindings = TypeBindings.emptyBindings();
    JavaType superClass = _findSuperClass(rawType, bindings);
    return new MapType(rawType, bindings, superClass, null,
            keyT, valueT, null, null, false);
}