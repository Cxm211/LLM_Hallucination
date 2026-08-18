public JSType caseObjectType(ObjectType type) {
  if (value.equals("function")) {
    JSType fnType = getNativeType(U2U_FUNCTION_TYPE);
    return resultEqualsValue && fnType.isSubtype(type) ? fnType : null;
      // Objects are restricted to "Function", subtypes are left
      // Only filter out subtypes of "function"
  }
  return matchesExpectation("object") ? type : null;
}