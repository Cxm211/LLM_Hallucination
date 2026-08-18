public static CollectionType construct(Class<?> rawType, JavaType elemT) {
    // First: may need to fabricate TypeBindings (needed for refining into
    // concrete collection types, as per [databind#1102])
    JavaType superClassType = null;
    Class<?> superClass = rawType.getSuperclass();
    if (superClass != null) {
        if (Collection.class.isAssignableFrom(superClass)) {
            superClassType = CollectionType.construct(superClass, elemT);
        } else if (Map.class.isAssignableFrom(superClass)) {
            superClassType = MapType.construct(superClass, null, null);
        } else {
            try {
                superClassType = SimpleType.construct(superClass);
            } catch (IllegalArgumentException e) {
                superClassType = null;
            }
        }
    }

    Class<?>[] interfaces = rawType.getInterfaces();
    JavaType[] superInterfaceTypes = null;
    if (interfaces.length > 0) {
        superInterfaceTypes = new JavaType[interfaces.length];
        for (int i = 0; i < interfaces.length; i++) {
            Class<?> iface = interfaces[i];
            if (Collection.class.isAssignableFrom(iface)) {
                superInterfaceTypes[i] = CollectionType.construct(iface, elemT);
            } else if (Map.class.isAssignableFrom(iface)) {
                superInterfaceTypes[i] = MapType.construct(iface, null, null);
            } else {
                try {
                    superInterfaceTypes[i] = SimpleType.construct(iface);
                } catch (IllegalArgumentException e) {
                    superInterfaceTypes[i] = null;
                }
            }
        }
    }

    return new CollectionType(rawType, null,
            superClassType, superInterfaceTypes, elemT,
            null, null, false);
}