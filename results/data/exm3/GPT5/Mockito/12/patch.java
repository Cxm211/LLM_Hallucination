public Class getGenericType(Field field) {
        Type generic = field.getGenericType();
        if (generic instanceof ParameterizedType) {
            Type actual = ((ParameterizedType) generic).getActualTypeArguments()[0];

            if (actual instanceof Class) {
                return (Class) actual;
            }
            if (actual instanceof ParameterizedType) {
                Type raw = ((ParameterizedType) actual).getRawType();
                if (raw instanceof Class) {
                    return (Class) raw;
                }
                return Object.class;
            }
            if (actual instanceof WildcardType) {
                Type[] uppers = ((WildcardType) actual).getUpperBounds();
                if (uppers != null && uppers.length > 0) {
                    Type u = uppers[0];
                    if (u instanceof Class) {
                        return (Class) u;
                    }
                    if (u instanceof ParameterizedType) {
                        Type raw = ((ParameterizedType) u).getRawType();
                        if (raw instanceof Class) {
                            return (Class) raw;
                        }
                    }
                }
                return Object.class;
            }
            if (actual instanceof TypeVariable) {
                Type[] bounds = ((TypeVariable<?>) actual).getBounds();
                if (bounds != null && bounds.length > 0) {
                    Type b = bounds[0];
                    if (b instanceof Class) {
                        return (Class) b;
                    }
                    if (b instanceof ParameterizedType) {
                        Type raw = ((ParameterizedType) b).getRawType();
                        if (raw instanceof Class) {
                            return (Class) raw;
                        }
                    }
                }
                return Object.class;
            }
        }
        return Object.class;
    }