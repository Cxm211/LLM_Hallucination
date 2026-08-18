public boolean apply(JSType type) {
      if (type == null || type.isUnknownType() || type.isEmptyType()) {
        return true;
      }
      JSType restrictedType = type.restrictByNotNullOrUndefined();
      if (!restrictedType.isSubtype(
              typeRegistry.getNativeType(OBJECT_TYPE))) {
        reportWarning(THIS_TYPE_NON_OBJECT, type.toString());
        return false;
      }
      return true;
    }