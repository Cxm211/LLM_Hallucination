static Type getSupertype(Type context, Class<?> contextRawType, Class<?> supertype) {
      // wildcards are useless for resolving supertypes. As the upper bound has the same raw type, use it instead
    checkArgument(supertype.isAssignableFrom(contextRawType));
    if (context instanceof WildcardType) {
      context = ((WildcardType) context).getUpperBounds()[0];
      contextRawType = $Gson$Types.getRawType(context);
    }
    return resolve(context, contextRawType,
        $Gson$Types.getGenericSupertype(context, contextRawType, supertype));
  }