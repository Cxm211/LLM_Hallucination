public JSType caseObjectType(ObjectType type) {
  if (value.equals("function")) {
    JSType ctorType = getNativeType(U2U_CONSTRUCTOR_TYPE);
    if (resultEqualsValue) {
      // Keep only function subtypes
      if (type.isSubtype(ctorType)) {
        return type;
      } else if (ctorType.isSubtype(type)) {
        return ctorType;
      } else {
        return null;
      }
    } else {
      // Exclude function subtypes
      return type.isSubtype(ctorType) ? null : type;
    }
  }
  return matchesExpectation("object") ? type : null;
}