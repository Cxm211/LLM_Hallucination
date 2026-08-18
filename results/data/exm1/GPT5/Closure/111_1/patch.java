protected JSType caseTopType(JSType topType) {
  return topType.restrictByNotNullOrUndefined();
}