public static CollectionType construct(Class<?> rawType, JavaType elemT) {
        TypeBindings bindings = TypeBindings.emptyBindings();
        Class<?> superClass = rawType.getSuperclass();
        return new CollectionType(rawType, bindings, superClass, null, elemT, null, null, false);
    }