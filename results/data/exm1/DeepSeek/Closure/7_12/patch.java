public JSType caseObjectType(ObjectType type) {
      if (value.equals("function")) {
        JSType ctorType = getNativeType(U2U_CONSTRUCTOR_TYPE);
        return resultEqualsValue && type.isSubtype(ctorType) ? type : null;
      }
      return matchesExpectation("object") ? type : null;
    }