public Class getGenericType(Field field) {        
        Type generic = field.getGenericType();
        if (generic != null && generic instanceof ParameterizedType) {
            Type actual = ((ParameterizedType) generic).getActualTypeArguments()[0];
            if (actual instanceof Class) {
                return (Class) actual;
            } else if (actual instanceof ParameterizedType) {
                Type raw = ((ParameterizedType) actual).getRawType();
                if (raw instanceof Class) {
                    return (Class) raw;
                }
            } else if (actual instanceof WildcardType) {
                Type[] upper = ((WildcardType) actual).getUpperBounds();
                if (upper.length > 0 && upper[0] instanceof Class) {
                    return (Class) upper[0];
                }
            } else if (actual instanceof TypeVariable) {
                Type[] bounds = ((TypeVariable<?>) actual).getBounds();
                if (bounds.length > 0 && bounds[0] instanceof Class) {
                    return (Class) bounds[0];
                }
            }
            // in case of nested generics we don't go deep
        }
        
        return Object.class;
    }