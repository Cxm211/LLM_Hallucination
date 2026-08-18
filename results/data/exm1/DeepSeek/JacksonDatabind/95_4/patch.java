public JavaType constructSpecializedType(JavaType baseType, Class<?> subclass) {
    final Class<?> rawBase = baseType.getRawClass();
    if (rawBase == subclass) {
        return baseType;
    }
    if (!rawBase.isAssignableFrom(subclass)) {
        throw new IllegalArgumentException(String.format("Class %s not subtype of %s", subclass.getName(), baseType));
    }
    int typeParamCount = subclass.getTypeParameters().length;
    JavaType newType;
    if (rawBase == Object.class) {
        if (typeParamCount == 0) {
            newType = _fromClass(null, subclass, TypeBindings.emptyBindings());
        } else {
            TypeBindings tb = _bindingsForSubtype(baseType, typeParamCount, subclass);
            newType = _fromClass(null, subclass, tb);
        }
        newType = newType.withHandlersFrom(baseType);
        return newType;
    }
    if (baseType.getBindings().isEmpty()) {
        if (typeParamCount == 0) {
            newType = _fromClass(null, subclass, TypeBindings.emptyBindings());
        } else {
            TypeBindings tb = _bindingsForSubtype(baseType, typeParamCount, subclass);
            newType = _fromClass(null, subclass, tb);
        }
        newType = newType.withHandlersFrom(baseType);
        return newType;
    }
    if (baseType.isContainerType()) {
        if (baseType.isMapLikeType()) {
            if ((subclass == HashMap.class) || (subclass == LinkedHashMap.class) || (subclass == EnumMap.class) || (subclass == TreeMap.class)) {
                newType = _fromClass(null, subclass, TypeBindings.create(subclass, baseType.getKeyType(), baseType.getContentType()));
                newType = newType.withHandlersFrom(baseType);
                return newType;
            }
        } else if (baseType.isCollectionLikeType()) {
            if ((subclass == ArrayList.class) || (subclass == LinkedList.class) || (subclass == HashSet.class) || (subclass == TreeSet.class)) {
                newType = _fromClass(null, subclass, TypeBindings.create(subclass, baseType.getContentType()));
                newType = newType.withHandlersFrom(baseType);
                return newType;
            }
            if (rawBase == EnumSet.class) {
                return baseType;
            }
        }
    }
    if (typeParamCount == 0) {
        newType = _fromClass(null, subclass, TypeBindings.emptyBindings());
    } else {
        TypeBindings tb = _bindingsForSubtype(baseType, typeParamCount, subclass);
        newType = _fromClass(null, subclass, tb);
    }
    newType = newType.withHandlersFrom(baseType);
    return newType;
}