static Type getSupertype(Type context, Class<?> contextRawType, Class<?> supertype) {
    checkArgument(contextRawType.isAssignableFrom(supertype));
    return resolve(context, contextRawType,
        $Gson$Types.getGenericSupertype(context, contextRawType, supertype));
  }