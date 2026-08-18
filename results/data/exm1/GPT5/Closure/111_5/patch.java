protected JSType caseTopType(JSType topType) {
  // Top type should be treated as unknown in this context to avoid
  // overly permissive type propagation.
  return topType.getNativeType(JSTypeNative.UNKNOWN_TYPE);
}