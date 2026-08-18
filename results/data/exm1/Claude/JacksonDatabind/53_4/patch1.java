public static TypeVariable<?>[] paramsFor2(Class<?> erasedType)
{
    if (erasedType == Map.class) {
        return VARS_MAP;
    }
    return erasedType.getTypeParameters();
}