public JSType caseObjectType(ObjectType type) {
  if (value.equals("function")) {
    JSType ctorType = getNativeType(U2U_CONSTRUCTOR_TYPE);
    if (resultEqualsValue) {
      return ctorType.isSubtype(type) ? ctorType : null;
    } else {
      // When comparing against 'function' and the result is inequality, retain the object type
      // (do not narrow to functions, but also do not eliminate the object type).
      return type;
    }
  }
  return matchesExpectation("object") ? type : null;
}