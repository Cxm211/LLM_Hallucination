public boolean apply(JSType type) {
  if (type == null || type.isUnknownType()) {
    return true;
  }
  if (!type.isSubtype(
          typeRegistry.getNativeType(OBJECT_TYPE))) {
    return false;
  }
  return true;
}