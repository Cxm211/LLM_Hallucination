public JSType caseObjectType(ObjectType type) {
  if (value.equals("function")) {
    JSType ctorType = getNativeType(U2U_CONSTRUCTOR_TYPE);
    boolean isFunction = type.isSubtype(ctorType);
    if (resultEqualsValue) {
      return isFunction ? type : null;
    } else {
      return isFunction ? null : type;
    }
  }
  return matchesExpectation("object") ? type : null;
}
