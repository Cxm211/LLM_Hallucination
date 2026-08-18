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
                // logger.log("For '" + parameterizedType + "' found type variable : { '" + typeParameter + "(" + System.identityHashCode(typeParameter) + ")" + "' : '" + actualTypeArgument + "(" + System.identityHashCode(typeParameter) + ")" + "' }");
            }
            return;
        }
        if (classType instanceof Class<?>) {
            TypeVariable<?>[] typeParameters = ((Class<?>) classType).getTypeParameters();
            for (TypeVariable<?> typeParameter : typeParameters) {
                Type[] bounds = typeParameter.getBounds();
                if (bounds != null && bounds.length > 0) {
                    Type bound = bounds[0];
                    if (bound instanceof WildcardType) {
                        contextualActualTypeParameters.put(typeParameter, boundsOf((WildcardType) bound));
                    } else {
                        contextualActualTypeParameters.put(typeParameter, bound);
                    }
                }
            }
        }
    }