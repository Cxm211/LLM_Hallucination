public JSType caseObjectType(ObjectType type) {
  if (value.equals("function")) {
    JSType ctorType = getNativeType(U2U_CONSTRUCTOR_TYPE);
    if (resultEqualsValue) {
      return type.isSubtype(ctorType) ? type : null;
    } else {
      return type.isSubtype(ctorType) ? null : type;
    }
  }
  return matchesExpectation("object") ? type : null;
}