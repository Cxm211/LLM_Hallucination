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
    TypeBindings bindings = TypeBindings.emptyBindings();
    if (cls.getTypeParameters().length > 0) {
        bindings = TypeBindings.create(cls, (JavaType[]) null);
    }
    return new SimpleType(cls, bindings,
            _bogusSuperClass(cls), null, null, null, false);
}