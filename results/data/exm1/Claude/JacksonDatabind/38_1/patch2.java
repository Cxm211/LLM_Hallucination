public static SimpleType construct(Class<?> cls)
{
    if (Map.class.isAssignableFrom(cls)) {
        throw new IllegalArgumentException("Can not construct SimpleType for a Map (class: "+cls.getName()+")");
    }
    if (Collection.class.isAssignableFrom(cls)) {
        throw new IllegalArgumentException("Can not construct SimpleType for a Collection (class: "+cls.getName()+")");
    }
    if (cls.isArray()) {
        throw new IllegalArgumentException("Can not construct SimpleType for an array (class: "+cls.getName()+")");
    }
    TypeBindings bindings = TypeBindings.emptyBindings();
    return new SimpleType(cls, bindings,
            _findSuperType(cls, Object.class, bindings), null, null, null, false);
}