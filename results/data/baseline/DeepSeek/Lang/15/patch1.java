private static Map<TypeVariable<?>, Type> getTypeArguments(Type type, Class<?> toClass,
        Map<TypeVariable<?>, Type> subtypeVarAssigns) {
    if (type instanceof Class<?>) {
        Class<?> cls = (Class<?>) type;
        if (!isAssignable(cls, toClass)) {
            return null;
        }
        if (cls.isPrimitive()) {
            if (toClass.isPrimitive()) {
                return new HashMap<TypeVariable<?>, Type>();
            }
            cls = ClassUtils.primitiveToWrapper(cls);
        }
        HashMap<TypeVariable<?>, Type> typeVarAssigns = subtypeVarAssigns == null
                ? new HashMap<TypeVariable<?>, Type>()
                : new HashMap<TypeVariable<?>, Type>(subtypeVarAssigns);
        if (toClass.equals(cls)) {
            return typeVarAssigns;
        }
        return getTypeArguments(getClosestParentType(cls, toClass), toClass, typeVarAssigns);
    }

    if (type instanceof ParameterizedType) {
        ParameterizedType pType = (ParameterizedType) type;
        Class<?> rawType = getRawType(pType);
        Type[] actualTypeArgs = pType.getActualTypeArguments();
        TypeVariable<?>[] typeParams = rawType.getTypeParameters();
        HashMap<TypeVariable<?>, Type> typeVarAssigns = subtypeVarAssigns == null
                ? new HashMap<TypeVariable<?>, Type>()
                : new HashMap<TypeVariable<?>, Type>(subtypeVarAssigns);
        for (int i = 0; i < typeParams.length; i++) {
            typeVarAssigns.put(typeParams[i], actualTypeArgs[i]);
        }
        return getTypeArguments(rawType, toClass, typeVarAssigns);
    }

    if (type instanceof TypeVariable<?>) {
        TypeVariable<?> typeVar = (TypeVariable<?>) type;
        if (subtypeVarAssigns != null && subtypeVarAssigns.containsKey(typeVar)) {
            return getTypeArguments(subtypeVarAssigns.get(typeVar), toClass, subtypeVarAssigns);
        }
        Type[] bounds = typeVar.getBounds();
        if (bounds.length > 0) {
            return getTypeArguments(bounds[0], toClass, subtypeVarAssigns);
        }
        return null;
    }

    // For other types (e.g., GenericArrayType), return null or handle as needed
    return null;
}