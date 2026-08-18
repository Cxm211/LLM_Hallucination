public boolean apply(JSType type) {
      // Allow object types and unions that include any object type.
      if (type.isSubtype(
              typeRegistry.getNativeType(OBJECT_TYPE))) {
        return true;
      }
      if (type instanceof UnionType) {
        UnionType union = (UnionType) type;
        for (JSType alt : union.getAlternates()) {
          if (alt.isSubtype(typeRegistry.getNativeType(OBJECT_TYPE))) {
            return true;
          }
        }
      }
      reportWarning(THIS_TYPE_NON_OBJECT, type.toString());
      return false;
    }