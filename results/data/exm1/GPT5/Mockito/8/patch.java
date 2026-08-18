protected void registerTypeVariablesOn(Type classType) {
        if (classType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) classType;
            TypeVariable[] typeParameters = ((Class<?>) parameterizedType.getRawType()).getTypeParameters();
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
            return;
        }
        if (classType instanceof Class<?>) {
            TypeVariable[] typeParameters = ((Class<?>) classType).getTypeParameters();
            for (TypeVariable typeParameter : typeParameters) {
                // Map the class type variable to itself so its bounds can be resolved later
                contextualActualTypeParameters.put(typeParameter, typeParameter);
            }
        }
    }