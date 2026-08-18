public JavaType constructSpecializedType(JavaType baseType, Class<?> subclass) {
    final Class<?> rawBase = baseType.getRawClass();
    if (rawBase == subclass) {
        return baseType;
    }

    JavaType newType;

    do { // bogus loop to be able to break
        if (rawBase == Object.class) {
            newType = _fromClass(null, subclass, TypeBindings.emptyBindings());
            break;
        }
        if (!rawBase.isAssignableFrom(subclass)) {
            throw new IllegalArgumentException(String.format(
                    "Class %s not subtype of %s", subclass.getName(), baseType));
        }
        // A few special cases where we can simplify handling:

        // (1) Original target type has no generics -- just resolve subtype
        if (baseType.getBindings().isEmpty()) {
            newType = _fromClass(null, subclass, TypeBindings.emptyBindings());     
            break;
        }
        // (2) A small set of "well-known" List/Map subtypes where can take a short-cut
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
                // 29-Oct-2015, tatu: One further shortcut: there are variants of `EnumSet`,
                //    but they are impl details and we basically do not care...
                if (rawBase == EnumSet.class) {
                    return baseType;
                }
            }
        }
        // (3) Sub-class does not take type parameters -- just resolve subtype
        int typeParamCount = subclass.getTypeParameters().length;
        if (typeParamCount == 0) {
            // Use bindings from base type if available, otherwise empty
            TypeBindings tb;
            if (baseType.containedTypeCount() == 1) {
                tb = TypeBindings.create(subclass, baseType.containedType(0));
            } else if (baseType.containedTypeCount() == 2) {
                tb = TypeBindings.create(subclass, baseType.containedType(0),
                        baseType.containedType(1));
            } else {
                tb = TypeBindings.emptyBindings();
            }
            newType = _fromClass(null, subclass, tb);
            break;
        }
        
        // If not, we'll need to do more thorough forward+backwards resolution. Sigh.
        // !!! TODO (as of 28-Jan-2016, at least)
        
        // 20-Oct-2015, tatu: Container, Map-types somewhat special. There is
        //    a way to fully resolve and merge hierarchies; but that gets expensive
        //    so let's, for now, try to create close-enough approximation that
        //    is not 100% same, structurally, but has equivalent information for
        //    our specific neeeds.
        // 29-Mar-2016, tatu: See [databind#1173]  (and test `TypeResolverTest`)
        //  for a case where this code does get invoked: not ideal
        // 29-Jun-2016, tatu: As to bindings, this works for [databind#1215], but
        //  not certain it would reliably work... but let's hope for best for now
        if (baseType.isInterface()) {
            newType = baseType.refine(subclass, TypeBindings.emptyBindings(), null, new JavaType[] { baseType });
        } else {
            newType = baseType.refine(subclass, TypeBindings.emptyBindings(), baseType, NO_TYPES);
        }
        // Only SimpleType returns null, but if so just resolve regularly
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