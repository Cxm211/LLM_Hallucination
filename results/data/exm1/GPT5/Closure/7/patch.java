public JSType caseObjectType(ObjectType type) {
      if (value.equals("function")) {
        JSType ctorType = getNativeType(U2U_CONSTRUCTOR_TYPE);
        if (resultEqualsValue) {
          // Keep only function subtypes, preserve the original type when it is a function.
          return type.isSubtype(ctorType) ? type : null;
        } else {
          // Filter out functions when typeof != 'function'.
          return type.isSubtype(ctorType) ? null : type;
        }
      }
      return matchesExpectation("object") ? type : null;
    }