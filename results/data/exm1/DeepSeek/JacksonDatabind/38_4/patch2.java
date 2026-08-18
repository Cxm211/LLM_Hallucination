public static SimpleType construct(Class<?> cls) {
    if (Map.class.isAssignableFrom(cls)) {
        throw new IllegalArgumentException("Can not construct SimpleType for a Map (class: " + cls.getName() + ")");
    }
    if (Collection.class.isAssignableFrom(cls)) {
        throw new IllegalArgumentException("Can not construct SimpleType for a Collection (class: " + cls.getName() + ")");
    }
    if (cls.isArray()) {
        throw new IllegalArgumentException("Can not construct SimpleType for an array (class: " + cls.getName() + ")");
    }
    JavaType superClass = (cls.getSuperclass() == null) ? null : TypeFactory.defaultInstance().constructType(cls.getSuperclass());
    JavaType[] superInterfaces = null;
    Class<?>[] interfaces = cls.getInterfaces();
    if (interfaces != null && interfaces.length > 0) {
        superInterfaces = new JavaType[interfaces.length];
        for (int i = 0; i < interfaces.length; i++) {
            superInterfaces[i] = TypeFactory.defaultInstance().constructType(interfaces[i]);
        }
    }
    return new SimpleType(cls, TypeBindings.emptyBindings(), superClass, superInterfaces, null, null, false);
}