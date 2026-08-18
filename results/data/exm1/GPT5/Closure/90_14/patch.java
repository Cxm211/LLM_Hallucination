public boolean apply(JSType type) {
      JSType nonNullable = type == null ? null : type.restrictByNotNullOrUndefined();
      if (nonNullable == null || !nonNullable.isSubtype(
              typeRegistry.getNativeType(OBJECT_TYPE))) {
        reportWarning(THIS_TYPE_NON_OBJECT, type == null ? "null" : type.toString());
        return false;
      }
      return true;
    }