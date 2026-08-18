    public Class getGenericType(Field field) {        
        Type generic = field.getGenericType();
        if (generic != null && generic instanceof ParameterizedType) {
            Type actual = ((ParameterizedType) generic).getActualTypeArguments()[0];
            if (actual instanceof Class) {
                return (Class) actual;
            } else if (actual instanceof ParameterizedType) {
                // nested generics: return raw type of the parameterized type
                return (Class) ((ParameterizedType) actual).getRawType();
            } else {
                // TypeVariable, WildcardType, etc.
                return Object.class;
            }
        }
        
        return Object.class;
    }