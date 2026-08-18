private JSType getNativeType(JSTypeNative nativeType) {
    return (typeRegistry == null) ? null : typeRegistry.getNativeType(nativeType);
  }