public JSType caseObjectType(ObjectType type) {
  if (value.equals("function")) {
    JSType ctorType = getNativeType(U2U_CONSTRUCTOR_TYPE);
    if (resultEqualsValue) {
      return ctorType.isSubtype(type) ? ctorType : null;
    } else {
      // For typeof x != 'function', keep non-function object types, but
      // filter out types that are subtypes of Function.
      return type.isSubtype(ctorType) ? null : type;
    }
  }
  return matchesExpectation("object") ? type : null;
}
