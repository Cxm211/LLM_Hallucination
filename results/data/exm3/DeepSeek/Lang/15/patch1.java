    private static Map<TypeVariable<?>, Type> getTypeArguments(Class<?> cls, Class<?> toClass,
            Map<TypeVariable<?>, Type> subtypeVarAssigns) {
        if (!isAssignable(cls, toClass)) {
            return null;
        }

        if (cls.isPrimitive()) {
            if (toClass.isPrimitive()) {
                return new HashMap<TypeVariable<?>, Type>();
            }

            cls = ClassUtils.primitiveToWrapper(cls);
        }

        HashMap<TypeVariable<?>, Type> typeVarAssigns = subtypeVarAssigns == null ? new HashMap<TypeVariable<?>, Type>()
                : new HashMap<TypeVariable<?>, Type>(subtypeVarAssigns);

        if (cls.getTypeParameters().length > 0 || toClass.equals(cls)) {
            return typeVarAssigns;
        }

        return getTypeArguments(getClosestParentType(cls, toClass), toClass, typeVarAssigns);
    }