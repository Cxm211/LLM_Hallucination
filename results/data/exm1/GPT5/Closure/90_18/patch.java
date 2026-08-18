public boolean apply(JSType type) {
      // TODO(user): Doing an instanceof check here is too
      // restrictive as (Date,Error) is, for instance, an object type
      // even though its implementation is a UnionType. Would need to
      // create interfaces JSType, ObjectType, FunctionType etc and have
      // separate implementation instead of the class hierarchy, so that
      // union types can also be object types, etc.
      JSType objectType = typeRegistry.getNativeType(OBJECT_TYPE);
      if (type.isUnionType()) {
        UnionType union = type.toMaybeUnionType();
        for (JSType alt : union.getAlternates()) {
          if (!alt.isSubtype(objectType)) {
            reportWarning(THIS_TYPE_NON_OBJECT, type.toString());
            return false;
          }
        }
        return true;
      }
      if (!type.isSubtype(objectType)) {
        reportWarning(THIS_TYPE_NON_OBJECT, type.toString());
        return false;
      }
      return true;
    }