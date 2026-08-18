public JSType caseObjectType(ObjectType type) {
      if (value.equals("function")) {
        JSType ctorType = getNativeType(U2U_CONSTRUCTOR_TYPE);
        if (resultEqualsValue) {
          // If we expect typeof x == 'function', keep only function subtypes.
          return type.isSubtype(ctorType) ? type : null;
        } else {
          // If we expect typeof x != 'function', filter out function subtypes only.
          return type.isSubtype(ctorType) ? null : type;
        }
      }
      return matchesExpectation("object") ? type : null;
    }