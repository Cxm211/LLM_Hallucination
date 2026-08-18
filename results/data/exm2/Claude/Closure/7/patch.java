public JSType caseObjectType(ObjectType type) {
  if (value.equals("function")) {
    JSType ctorType = getNativeType(U2U_CONSTRUCTOR_TYPE);
    if (resultEqualsValue && ctorType.isSubtype(type)) {
      return ctorType;
    }
    // Objects are restricted to "Function", subtypes are left
    // Only filter out subtypes of "function"
    return resultEqualsValue ? null : type;
  }
  return matchesExpectation("object") ? type : null;
}