static Type getSupertype(Type context, Class<?> contextRawType, Class<?> supertype) {
      // wildcards are useless for resolving supertypes. As the upper bound has the same raw type, use it instead
    if (context instanceof WildcardType) {
      Type upperBound = ((WildcardType) context).getUpperBounds()[0];
      context = $Gson$Types.canonicalize(upperBound);
      contextRawType = $Gson$Types.getRawType(context);
    }
    checkArgument(supertype.isAssignableFrom(contextRawType));
    return resolve(context, contextRawType,
        $Gson$Types.getGenericSupertype(context, contextRawType, supertype));
  }