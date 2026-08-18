public boolean apply(JSType type) {
      // Allow union types that are composed entirely of object subtypes.
      // Previously, we only checked subtype against OBJECT, which incorrectly
      // rejected unions of object types.
      JSType objectNative = typeRegistry.getNativeType(OBJECT_TYPE);
      if (type.isUnionType()) {
        UnionType union = type.toMaybeUnionType();
        for (JSType alt : union.getAlternates()) {
          if (!alt.isSubtype(objectNative)) {
            reportWarning(THIS_TYPE_NON_OBJECT, type.toString());
            return false;
          }
        }
        return true;
      }

      if (!type.isSubtype(objectNative)) {
        reportWarning(THIS_TYPE_NON_OBJECT, type.toString());
        return false;
      }
      return true;
    }