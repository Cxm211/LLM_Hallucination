protected JSType caseTopType(JSType topType) {
  return topType == null ? null : topType.restrictByNotNullOrUndefined();
}