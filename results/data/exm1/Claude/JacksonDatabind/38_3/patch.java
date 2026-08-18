public static CollectionType construct(Class<?> rawType, JavaType elemT) {
        return new CollectionType(rawType, null,
                _bogusSuperClass(rawType), null, elemT,
                null, null, false);
    }