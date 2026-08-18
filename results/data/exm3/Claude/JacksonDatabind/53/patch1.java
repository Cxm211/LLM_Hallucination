public static TypeVariable<?>[] paramsFor2(Class<?> erasedType)
{
    if (erasedType == Map.class) {
        return VARS_MAP;
    }
    if (erasedType == HashMap.class) {
        return VARS_HASH_MAP;
    }
    if (erasedType == LinkedHashMap.class) {
        return VARS_LINKED_HASH_MAP;
    }
    return erasedType.getTypeParameters();
}