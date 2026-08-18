public static SimpleType construct(Class<?> cls)
{
    /* Let's add sanity checks, just to ensure no
     * Map/Collection entries are constructed
     */
    if (Map.class.isAssignableFrom(cls)) {
        throw new IllegalArgumentException("Can not construct SimpleType for a Map (class: "+cls.getName()+")");
    }
    if (Collection.class.isAssignableFrom(cls)) {
        throw new IllegalArgumentException("Can not construct SimpleType for a Collection (class: "+cls.getName()+")");
    }
    // ... and while we are at it, not array types either
    if (cls.isArray()) {
        throw new IllegalArgumentException("Can not construct SimpleType for an array (class: "+cls.getName()+")");
    }
    JavaType superClassType = null;
    Class<?> superClass = cls.getSuperclass();
    if (superClass != null) {
        if (Map.class.isAssignableFrom(superClass)) {
            superClassType = MapType.construct(superClass, null, null);
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

    Class<?>[] interfaces = cls.getInterfaces();
    JavaType[] superInterfaceTypes = null;
    if (interfaces.length > 0) {
        superInterfaceTypes = new JavaType[interfaces.length];
        for (int i = 0; i < interfaces.length; i++) {
            Class<?> iface = interfaces[i];
            if (Map.class.isAssignableFrom(iface)) {
                superInterfaceTypes[i] = MapType.construct(iface, null, null);
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

    return new SimpleType(cls, TypeBindings.emptyBindings(),
            superClassType, superInterfaceTypes, null, null, false);
}