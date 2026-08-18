private static Type getActualType(Type typeToEvaluate, Type parentType, Class<?> rawParentClass) {
    if (typeToEvaluate instanceof Class<?>) {
      return typeToEvaluate;
    } else if (typeToEvaluate instanceof ParameterizedType) {
      ParameterizedType castedType = (ParameterizedType) typeToEvaluate;
      Type owner = castedType.getOwnerType();
      Type[] actualTypeParameters = extractRealTypes(castedType.getActualTypeArguments(), parentType, rawParentClass);
      Type rawType = castedType.getRawType();
      return new ParameterizedTypeImpl(rawType, actualTypeParameters, owner);
    } else if (typeToEvaluate instanceof GenericArrayType) {
      GenericArrayType castedType = (GenericArrayType) typeToEvaluate;
      Type componentType = castedType.getGenericComponentType();
      Type actualType = getActualType(componentType, parentType, rawParentClass);
      if (componentType.equals(actualType)) {
        return castedType;
      }
      return actualType instanceof Class<?> ?
          TypeUtils.wrapWithArray(TypeUtils.toRawClass(actualType))
          : new GenericArrayTypeImpl(actualType);
    } else if (typeToEvaluate instanceof TypeVariable<?>) {
      TypeVariable<?> typeVariable = (TypeVariable<?>) typeToEvaluate;
      Type resolved = resolveTypeVariable(typeVariable, parentType, rawParentClass);
      if (resolved != null) {
        return resolved;
      }
      throw new UnsupportedOperationException("Cannot resolve type variable " + typeVariable);
    } else if (typeToEvaluate instanceof WildcardType) {
      WildcardType castedType = (WildcardType) typeToEvaluate;
      return getActualType(castedType.getUpperBounds()[0], parentType, rawParentClass);
    } else {
      throw new IllegalArgumentException("Type \'" + typeToEvaluate + "\' is not a Class, "
          + "ParameterizedType, GenericArrayType or TypeVariable. Can\'t extract type.");
    }
  }

  private static Type resolveTypeVariable(TypeVariable<?> typeVariable, Type parentType, Class<?> rawParentClass) {
    if (parentType instanceof ParameterizedType) {
      ParameterizedType parameterizedParent = (ParameterizedType) parentType;
      GenericDeclaration declaration = typeVariable.getGenericDeclaration();
      if (declaration instanceof Class) {
        Class<?> declaringClass = (Class<?>) declaration;
        // Find the index of this type variable in the declaring class
        TypeVariable<?>[] typeParams = declaringClass.getTypeParameters();
        int index = -1;
        for (int i = 0; i < typeParams.length; i++) {
          if (typeParams[i].equals(typeVariable)) {
            index = i;
            break;
          }
        }
        if (index == -1) {
          // Should not happen, but fall back to erasure
          return typeVariable.getBounds()[0];
        }
        // Walk up the class hierarchy to find the actual type argument
        Class<?> current = rawParentClass;
        while (current != null && current != declaringClass) {
          Type genericSuper = current.getGenericSuperclass();
          if (genericSuper instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) genericSuper;
            if (pt.getRawType() == declaringClass) {
              return pt.getActualTypeArguments()[index];
            }
          }
          current = current.getSuperclass();
        }
        // If the declaring class is the same as rawParentClass, use parent's actual types directly
        if (current == declaringClass) {
          return parameterizedParent.getActualTypeArguments()[index];
        }
      }
    }
    return null;
  }