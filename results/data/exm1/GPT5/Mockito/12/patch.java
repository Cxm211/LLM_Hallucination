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
            }
        }
        
        return Object.class;
    }