static Type getSupertype(Type context, Class<?> contextRawType, Class<?> supertype) {
    // wildcards are useless for resolving supertypes. As the upper bound has the same raw type, use it instead
    checkArgument(supertype.isAssignableFrom(contextRawType));
    if (context instanceof java.lang.reflect.WildcardType) {
      context = ((java.lang.reflect.WildcardType) context).getUpperBounds()[0];
    }
    return resolve(context, contextRawType,
        $Gson$Types.getGenericSupertype(context, contextRawType, supertype));
  }