public boolean apply(JSType type) {
  if (type.isNoType() || type.isUnknownType()) {
    return true;
  }
  if (!type.isSubtype(
          typeRegistry.getNativeType(OBJECT_TYPE))) {
    reportWarning(THIS_TYPE_NON_OBJECT, type.toString());
    return false;
  }
  return true;
}