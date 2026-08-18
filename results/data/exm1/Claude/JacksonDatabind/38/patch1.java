public static MapType construct(Class<?> rawType, JavaType keyT, JavaType valueT)
    {
        return new MapType(rawType, null, _bogusSuperClass(rawType), null,
                keyT, valueT, null, null, false);
    }