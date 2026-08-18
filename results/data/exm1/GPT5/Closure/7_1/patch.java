public JSType caseObjectType(ObjectType type) {
  if (value.equals("function")) {
    JSType ctorType = getNativeType(U2U_CONSTRUCTOR_TYPE);
    if (!resultEqualsValue) {
      return null;
    }
    if (type.isSubtype(ctorType)) {
      // Keep more specific function subtypes as-is.
      return type;
    }
    // If the current type is a supertype of Function, narrow to Function.
    return ctorType.isSubtype(type) ? ctorType : null;
  }
  return matchesExpectation("object") ? type : null;
}
