protected JSType caseTopType(JSType topType) {
  if ("goog.isArray".equals(closureFunction)) {
    return getNativeType(ARRAY_TYPE);
  }
  return super.caseTopType(topType);
}