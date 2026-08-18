static Type getSupertype(Type context, Class<?> contextRawType, Class<?> supertype) {
    // wildcards are useless for resolving supertypes. As the upper bound has the same raw type, use it instead
    if (context instanceof java.lang.reflect.WildcardType) {
      Type upper = ((java.lang.reflect.WildcardType) context).getUpperBounds()[0];
      context = upper;
      contextRawType = $Gson$Types.getRawType(upper);
    }
    checkArgument(supertype.isAssignableFrom(contextRawType));
    return resolve(context, contextRawType,
        $Gson$Types.getGenericSupertype(context, contextRawType, supertype));
  }