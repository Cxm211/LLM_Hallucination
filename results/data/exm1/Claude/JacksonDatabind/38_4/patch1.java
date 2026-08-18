public static MapType construct(Class<?> rawType, JavaType keyT, JavaType valueT)
{
    TypeBindings bindings = TypeBindings.emptyBindings();
    JavaType superClass = null;
    JavaType[] superInterfaces = null;
    return new MapType(rawType, bindings, superClass, superInterfaces,
            keyT, valueT, null, null, false);
}