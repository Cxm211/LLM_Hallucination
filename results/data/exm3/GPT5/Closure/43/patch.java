private JSType getNativeType(JSTypeNative nativeType) {
    if (typeRegistry == null) {
      return null;
    }
    JSType t = typeRegistry.getNativeType(nativeType);
    if (t == null) {
      return typeRegistry.getNativeType(JSTypeNative.UNKNOWN_TYPE);
    }
    return t;
  }