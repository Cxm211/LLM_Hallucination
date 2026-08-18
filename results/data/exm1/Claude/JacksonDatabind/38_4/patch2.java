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
    JavaType superClass = null;
    JavaType[] superInterfaces = null;
    return new SimpleType(cls, TypeBindings.emptyBindings(),
            superClass, superInterfaces, null, null, false);
}