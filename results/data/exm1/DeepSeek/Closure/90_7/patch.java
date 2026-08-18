  public boolean apply(JSType type) {
    if (type instanceof UnionType) {
      UnionType union = (UnionType) type;
      for (JSType alt : union.getAlternates()) {
        if (!alt.isSubtype(
                typeRegistry.getNativeType(OBJECT_TYPE))) {
          reportWarning(THIS_TYPE_NON_OBJECT, type.toString());
          return false;
        }
      }
      return true;
    }
    if (!type.isSubtype(
            typeRegistry.getNativeType(OBJECT_TYPE))) {
      reportWarning(THIS_TYPE_NON_OBJECT, type.toString());
      return false;
    }
    return true;
  }