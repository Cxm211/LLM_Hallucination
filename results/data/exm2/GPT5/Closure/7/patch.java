public JSType caseObjectType(ObjectType type) {
  if (value.equals("function")) {
    JSType ctorType = getNativeType(U2U_CONSTRUCTOR_TYPE);
    if (!resultEqualsValue) {
      return null;
    }
    if (type.isSubtype(ctorType)) {
      return type;
    }
    if (ctorType.isSubtype(type)) {
      return ctorType;
    }
    return null;
  }
  return matchesExpectation("object") ? type : null;
}