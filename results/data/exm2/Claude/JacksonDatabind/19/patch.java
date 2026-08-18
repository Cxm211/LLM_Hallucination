private JavaType _mapType(Class<?> rawClass)
{
    // 28-May-2015, tatu: Properties are special, as per [databind#810]
    // Properties extends Hashtable<Object,Object> but should be treated as Map<String,String>
    if (rawClass == java.util.Properties.class) {
        return MapType.construct(rawClass, _fromClass(null, String.class, EMPTY_BINDINGS),
                _fromClass(null, String.class, EMPTY_BINDINGS));
    }
    JavaType[] typeParams = findTypeParameters(rawClass, Map.class);
    // ok to have no types ("raw")
    if (typeParams == null) {
        return MapType.construct(rawClass, _unknownType(), _unknownType());
    }
    // but exactly 2 types if any found
    if (typeParams.length != 2) {
        throw new IllegalArgumentException("Strange Map type "+rawClass.getName()+": can not determine type parameters");
    }
    return MapType.construct(rawClass, typeParams[0], typeParams[1]);
}