protected void registerTypeVariablesOn(Type classType) {
    if (!(classType instanceof ParameterizedType)) {
        return;
    }
    ParameterizedType parameterizedType = (ParameterizedType) classType;
    Type rawType = parameterizedType.getRawType();
    if (!(rawType instanceof Class)) {
        return;
    }
    Class<?> rawClass = (Class<?>) rawType;
    TypeVariable[] typeParameters = rawClass.getTypeParameters();
    Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
    for (int i = 0; i < actualTypeArguments.length; i++) {
        TypeVariable typeParameter = typeParameters[i];
        Type actualTypeArgument = actualTypeArguments[i];
        if (actualTypeArgument instanceof WildcardType) {
            contextualActualTypeParameters.put(typeParameter, boundsOf((WildcardType) actualTypeArgument));
        } else {
            contextualActualTypeParameters.put(typeParameter, actualTypeArgument);
        }
    }
}