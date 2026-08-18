public boolean apply(JSType type) {
      // TODO(user): Doing an instanceof check here is too
      // restrictive as (Date,Error) is, for instance, an object type
      // even though its implementation is a UnionType. Would need to
      // create interfaces JSType, ObjectType, FunctionType etc and have
      // separate implementation instead of the class hierarchy, so that
      // union types can also be object types, etc.
      JSType objectNative = typeRegistry.getNativeType(OBJECT_TYPE);
      if (type.isSubtype(objectNative)) {
        return true;
      }
      if (type.isUnionType()) {
        UnionType ut = type.toMaybeUnionType();
        for (JSType alt : ut.getAlternates()) {
          if (alt.isSubtype(objectNative)) {
            continue;
          }
          if (alt.isNullType() || alt.isVoidType()) {
            // Allow nullable/undefined union with object types.
            continue;
          }
          reportWarning(THIS_TYPE_NON_OBJECT, type.toString());
          return false;
        }
        return true;
      }
      if (!type.isSubtype(objectNative)) {
        reportWarning(THIS_TYPE_NON_OBJECT, type.toString());
        return false;
      }
      return true;
    }