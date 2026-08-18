// buggy code
  private static Type getActualType(
      Type typeToEvaluate, Type parentType, Class<?> rawParentClass) {
    if (typeToEvaluate instanceof Class<?>) {
      return typeToEvaluate;
    } else if (typeToEvaluate instanceof ParameterizedType) {
      ParameterizedType castedType = (ParameterizedType) typeToEvaluate;
      Type owner = castedType.getOwnerType();
      Type[] actualTypeParameters =
          extractRealTypes(castedType.getActualTypeArguments(), parentType, rawParentClass);
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
      if (parentType instanceof ParameterizedType) {
        TypeVariable<?> fieldTypeVariable = (TypeVariable<?>) typeToEvaluate;

        // Find the parameterized type that corresponds to the class where this type variable
        // was declared, in the context of rawParentClass/parentType.
        Class<?> declaringClass = (Class<?>) fieldTypeVariable.getGenericDeclaration();

        ParameterizedType resolvedParamType = null;
        Class<?> currentClass = rawParentClass;
        Type currentType = parentType;

        // If currentType is a ParameterizedType of currentClass, keep it; otherwise null.
        if (!(currentType instanceof ParameterizedType) ||
            TypeUtils.toRawClass(((ParameterizedType) currentType).getRawType()) != currentClass) {
          currentType = null; // ensure we only use matching pairs
        }

        while (currentClass != null && currentClass != Object.class) {
          if (currentClass == declaringClass) {
            if (currentType instanceof ParameterizedType) {
              resolvedParamType = (ParameterizedType) currentType;
            }
            break;
          }

          // Traverse interfaces
          Type[] genericIfaces = currentClass.getGenericInterfaces();
          Class<?>[] ifaces = currentClass.getInterfaces();
          boolean found = false;
          for (int i = 0; i < ifaces.length; i++) {
            Type gi = genericIfaces[i];
            Class<?> iface = ifaces[i];
            if (iface == declaringClass) {
              if (gi instanceof ParameterizedType) {
                resolvedParamType = (ParameterizedType) gi;
              }
              found = true;
              break;
            }
          }
          if (found) {
            break;
          }

          // Traverse superclass
          Type gs = currentClass.getGenericSuperclass();
          Class<?> sc = currentClass.getSuperclass();
          if (sc == null) {
            break;
          }
          currentClass = sc;
          currentType = gs;
          if (!(currentType instanceof ParameterizedType) ||
              TypeUtils.toRawClass(((ParameterizedType) currentType).getRawType()) != currentClass) {
            currentType = null; // reset if not parameterized or mismatch
          }
        }

        // If we could not find a matching parameterized type, fall back to parent if applicable
        if (resolvedParamType == null && rawParentClass == declaringClass && parentType instanceof ParameterizedType) {
          resolvedParamType = (ParameterizedType) parentType;
        }

        if (resolvedParamType != null) {
          TypeVariable<?>[] classTypeVariables = declaringClass.getTypeParameters();
          int indexOfActualTypeArgument = getIndex(classTypeVariables, fieldTypeVariable);
          if (indexOfActualTypeArgument >= 0) {
            Type[] actualTypeArguments = resolvedParamType.getActualTypeArguments();
            Type actual = actualTypeArguments[indexOfActualTypeArgument];
            // Recursively resolve in case it is itself a TypeVariable or contains them
            return getActualType(actual, resolvedParamType, declaringClass);
          }
        }

        // Could not resolve; fall through to error below
      }

      throw new UnsupportedOperationException("Expecting parameterized type, got " + parentType
          + ".\n Are you missing the use of TypeToken idiom?\n See "
          + "http://sites.google.com/site/gson/gson-user-guide#TOC-Serializing-and-Deserializing-Gener");
    } else if (typeToEvaluate instanceof WildcardType) {
      WildcardType castedType = (WildcardType) typeToEvaluate;
      return getActualType(castedType.getUpperBounds()[0], parentType, rawParentClass);
    } else {
      throw new IllegalArgumentException("Type '" + typeToEvaluate + "' is not a Class, "
          + "ParameterizedType, GenericArrayType or TypeVariable. Can't extract type.");
    }
  }