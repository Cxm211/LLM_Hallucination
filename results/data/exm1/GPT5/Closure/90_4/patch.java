public boolean apply(JSType type) {
      // TODO(user): Doing an instanceof check here is too
      // restrictive as (Date,Error) is, for instance, an object type
      // even though its implementation is a UnionType. Would need to
      // create interfaces JSType, ObjectType, FunctionType etc and have
      // separate implementation instead of the class hierarchy, so that
      // union types can also be object types, etc.
      // Coerce out null/undefined, since ES3 coerces null to the global object
      // and we do not want nullable this-types to fail this check.
      JSType nonNullType = type == null ? null : type.restrictByNotNullOrUndefined();
      if (nonNullType == null || !nonNullType.isSubtype(
              typeRegistry.getNativeType(OBJECT_TYPE))) {
        reportWarning(THIS_TYPE_NON_OBJECT, type.toString());
        return false;
      }
      return true;
    }