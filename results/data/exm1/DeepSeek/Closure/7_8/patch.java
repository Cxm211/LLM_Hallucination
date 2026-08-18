public JSType caseObjectType(ObjectType type) {
      if (value.equals("function")) {
        JSType ctorType = getNativeType(U2U_CONSTRUCTOR_TYPE);
        if (resultEqualsValue) {
          return type.equals(ctorType) ? ctorType : null;
        } else {
          return type.isSubtype(ctorType) ? ctorType : null;
        }
      }
      return matchesExpectation("object") ? type : null;
    }