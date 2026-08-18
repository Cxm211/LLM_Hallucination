private JSType getNativeType(JSTypeNative nativeType) {
    // Return null safely if the type registry is not available (e.g., when type checking is disabled).
    if (typeRegistry == null) {
      return null;
    }
    return typeRegistry.getNativeType(nativeType);
  }