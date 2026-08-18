public static MapType construct(Class<?> rawType, JavaType keyT, JavaType valueT) {
        TypeBindings bindings = TypeBindings.emptyBindings();
        Class<?> superClass = rawType.getSuperclass();
        return new MapType(rawType, bindings, superClass, null, keyT, valueT, null, null, false);
    }