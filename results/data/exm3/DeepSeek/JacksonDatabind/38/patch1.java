public static MapType construct(Class<?> rawType, JavaType keyT, JavaType valueT)
{
    JavaType superClassType = null;
    Class<?> superClass = rawType.getSuperclass();
    if (superClass != null) {
        if (Map.class.isAssignableFrom(superClass)) {
            superClassType = MapType.construct(superClass, keyT, valueT);
        } else if (Collection.class.isAssignableFrom(superClass)) {
            superClassType = CollectionType.construct(superClass, null);
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
            if (Map.class.isAssignableFrom(iface)) {
                superInterfaceTypes[i] = MapType.construct(iface, keyT, valueT);
            } else if (Collection.class.isAssignableFrom(iface)) {
                superInterfaceTypes[i] = CollectionType.construct(iface, null);
            } else {
                try {
                    superInterfaceTypes[i] = SimpleType.construct(iface);
                } catch (IllegalArgumentException e) {
                    superInterfaceTypes[i] = null;
                }
            }
        }
    }

    return new MapType(rawType, null, superClassType, superInterfaceTypes,
            keyT, valueT, null, null, false);
}