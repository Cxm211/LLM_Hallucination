// ===== FIXED org.mockito.internal.util.reflection.GenericMaster :: getGenericType(Field) [lines 16-29] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Mockito/Mockito-12-fixed/src/org/mockito/internal/util/reflection/GenericMaster.java =====
    public Class getGenericType(Field field) {        
        Type generic = field.getGenericType();
        if (generic != null && generic instanceof ParameterizedType) {
            Type actual = ((ParameterizedType) generic).getActualTypeArguments()[0];
            if (actual instanceof Class) {
                return (Class) actual;
            } else if (actual instanceof ParameterizedType) {
                //in case of nested generics we don't go deep
                return (Class) ((ParameterizedType) actual).getRawType();
            }
        }
        
        return Object.class;
    }
