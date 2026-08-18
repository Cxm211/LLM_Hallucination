    private static boolean isAssignable(Type type, ParameterizedType toParameterizedType,
            Map<TypeVariable<?>, Type> typeVarAssigns) {
        if (type == null) {
            return true;
        }

        if (toParameterizedType == null) {
            return false;
        }

        if (toParameterizedType.equals(type)) {
            return true;
        }

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

            if (fromTypeArg != null && !TypeUtils.isAssignable(fromTypeArg, toTypeArg, typeVarAssigns)) {
                return false;
            }
        }

        return true;
    }