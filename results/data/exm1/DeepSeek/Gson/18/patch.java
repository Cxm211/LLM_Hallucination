static Type getSupertype(Type context, Class<?> contextRawType, Class<?> supertype) {
      checkArgument(supertype.isAssignableFrom(contextRawType));
      if (context instanceof WildcardType) {
        context = ((WildcardType) context).getUpperBounds()[0];
      }
      return resolve(context, contextRawType,
          $Gson$Types.getGenericSupertype(context, contextRawType, supertype));
    }