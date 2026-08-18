private static boolean isAssignable(Type type, Type toType,
        Map<TypeVariable<?>, Type> typeVarAssigns) {
    if (type == null) {
        return true;
    }

    if (toType == null) {
        return false;
    }

    if (toType.equals(type)) {
        return true;
    }

    if (toType instanceof ParameterizedType) {
        ParameterizedType toParameterizedType = (ParameterizedType) toType;
        Class<?> toClass = getRawType(toParameterizedType);
        Map<TypeVariable<?>, Type> fromTypeVarAssigns = getTypeArguments(type, toClass, null);

        if (fromTypeVarAssigns == null) {
            return false;
        }

        if (fromTypeVarAssigns.isEmpty()) {
            return true;
        }

        Map<TypeVariable<?>, Type> toTypeVarAssigns = getTypeArguments(toParameterizedType,
                toClass, typeVarAssigns);

        for (Map.Entry<TypeVariable<?>, Type> entry : toTypeVarAssigns.entrySet()) {
            Type toTypeArg = entry.getValue();
            Type fromTypeArg = fromTypeVarAssigns.get(entry.getKey());

            if (fromTypeArg != null && !toTypeArg.equals(fromTypeArg)) {
                if (!(toTypeArg instanceof WildcardType && isAssignable(fromTypeArg, toTypeArg, typeVarAssigns))) {
                    return false;
                }
            }
        }

        return true;
    }

    if (toType instanceof Class<?>) {
        Class<?> toClass = (Class<?>) toType;
        // use isAssignable for Class
        return isAssignable(type, toClass);
    }

    if (toType instanceof WildcardType) {
        WildcardType wc = (WildcardType) toType;
        // Check upper bounds
        for (Type upperBound : wc.getUpperBounds()) {
            if (!isAssignable(type, upperBound, typeVarAssigns)) {
                return false;
            }
        }
        // Check lower bounds
        for (Type lowerBound : wc.getLowerBounds()) {
            if (!isAssignable(lowerBound, type, typeVarAssigns)) {
                return false;
            }
        }
        return true;
    }

    // fallback: not assignable
    return false;
}