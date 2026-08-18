static Type getSupertype(Type context, Class<?> contextRawType, Class<?> supertype) {
    checkArgument(supertype.isAssignableFrom(contextRawType));
    Type genericSupertype = $Gson$Types.getGenericSupertype(context, contextRawType, supertype);
    if (genericSupertype instanceof WildcardType) {
      genericSupertype = ((WildcardType) genericSupertype).getUpperBounds()[0];
    }
    return resolve(context, contextRawType, genericSupertype);
  }