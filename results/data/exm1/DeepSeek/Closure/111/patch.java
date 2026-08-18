protected JSType caseTopType(JSType topType) {
  // The default behavior should be overridden by subclasses to return the type
  // that results from the specific function check. For example, for goog.isDef
  // the result is topType without VOID_TYPE. Returning topType unchanged is
  // incorrect for top types.
  return topType;
}