public JavaType constructParametricType(Class<?> parametrized, Class<?>... parameterClasses) {
        int len = parameterClasses.length;
        JavaType[] pt = new JavaType[len];
        for (int i = 0; i < len; ++i) {
            pt[i] = _fromClass(null, parameterClasses[i], TypeBindings.emptyBindings());
        }
        return constructParametricType(parametrized, pt);
    }