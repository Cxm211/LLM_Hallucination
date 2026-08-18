public boolean apply(JSType type) {
      if (type.isEmptyType() || type.isUnknownType() ||
          type.isSubtype(typeRegistry.getNativeType(OBJECT_TYPE)) ||
          (type.isUnionType() && type.restrictByNotNullOrUndefined().isSubtype(
              typeRegistry.getNativeType(OBJECT_TYPE)))) {
        return true;
      }
      reportWarning(THIS_TYPE_NON_OBJECT, type.toString());
      return false;
    }