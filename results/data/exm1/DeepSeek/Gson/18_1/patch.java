import java.lang.reflect.WildcardType;

  static Type getSupertype(Type context, Class<?> contextRawType, Class<?> supertype) {
    checkArgument(supertype.isAssignableFrom(contextRawType));
    Type resolvedContext = context;
    if (context instanceof WildcardType) {
      resolvedContext = ((WildcardType) context).getUpperBounds()[0];
    }
    return resolve(resolvedContext, contextRawType,
        $Gson$Types.getGenericSupertype(resolvedContext, contextRawType, supertype));
  }