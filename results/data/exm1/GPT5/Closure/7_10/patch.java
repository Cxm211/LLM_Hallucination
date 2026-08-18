public JSType caseObjectType(ObjectType type) {
      if (value.equals("function")) {
        JSType ctorType = getNativeType(U2U_CONSTRUCTOR_TYPE);
        boolean isFunctionSubtype = type.isSubtype(ctorType);
        if (resultEqualsValue) {
          // Keep only function subtypes when expecting 'function'
          return isFunctionSubtype ? type : null;
        } else {
          // Filter out function subtypes when expecting not 'function'
          return isFunctionSubtype ? null : type;
        }
      }
      return matchesExpectation("object") ? type : null;
    }