public JavaType constructSpecializedType(JavaType baseType, Class<?> subclass)
{
    final Class<?> rawBase = baseType.getRawClass();
    if (rawBase == subclass) {
        return baseType;
    }

    JavaType newType;

    do {
        if (rawBase == Object.class) {
            newType = _fromClass(null, subclass, TypeBindings.emptyBindings());
            break;
        }
        if (!rawBase.isAssignableFrom(subclass)) {
            throw new IllegalArgumentException(String.format(
                    "Class %s not subtype of %s", subclass.getName(), baseType));
        }

        if (baseType.getBindings().isEmpty()) {
            newType = _fromClass(null, subclass, TypeBindings.emptyBindings());
            break;
        }
        if (baseType.isContainerType()) {
            if (baseType.isMapLikeType()) {
                if ((subclass == HashMap.class)
                        || (subclass == LinkedHashMap.class)
                        || (subclass == EnumMap.class)
                        || (subclass == TreeMap.class)) {
                    newType = _fromClass(null, subclass,
                            TypeBindings.create(subclass, baseType.getKeyType(), baseType.getContentType()));
                    break;
                }
            } else if (baseType.isCollectionLikeType()) {
                if ((subclass == ArrayList.class)
                        || (subclass == LinkedList.class)
                        || (subclass == HashSet.class)
                        || (subclass == TreeSet.class)) {
                    newType = _fromClass(null, subclass,
                            TypeBindings.create(subclass, baseType.getContentType()));
                    break;
                }
                if (rawBase == EnumSet.class) {
                    return baseType;
                }
            }
        }
        int typeParamCount = subclass.getTypeParameters().length;
        if (typeParamCount == 0) {
            newType = _fromClass(null, subclass, TypeBindings.emptyBindings());
            break;
        }

        if (baseType.isInterface()) {
            newType = baseType.refine(subclass, TypeBindings.emptyBindings(), null, new JavaType[] { baseType });
        } else {
            newType = baseType.refine(subclass, TypeBindings.emptyBindings(), baseType, NO_TYPES);
        }
        if (newType == null) {
            TypeBindings tb = null;
            if (baseType.containedTypeCount() == typeParamCount) {
                if (typeParamCount == 1) {
                    tb = TypeBindings.create(subclass, baseType.containedType(0));
                } else if (typeParamCount == 2) {
                    tb = TypeBindings.create(subclass, baseType.containedType(0),
                            baseType.containedType(1));
                }
            }
            newType = _fromClass(null, subclass,
                    (tb == null) ? TypeBindings.emptyBindings() : tb);
        }
    } while (false);

    return newType;
}