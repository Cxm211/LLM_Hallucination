public boolean apply(JSType type) {
      // Coerce out null/undefined before checking if this is an object type.
      JSType nonNullable = type.restrictByNotNullOrUndefined();
      if (!nonNullable.isSubtype(
              typeRegistry.getNativeType(OBJECT_TYPE))) {
        reportWarning(THIS_TYPE_NON_OBJECT, type.toString());
        return false;
      }
      return true;
    }