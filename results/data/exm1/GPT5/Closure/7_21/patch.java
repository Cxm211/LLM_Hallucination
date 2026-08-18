public JSType caseObjectType(ObjectType type) {
      if (value.equals("function")) {
        JSType ctorType = getNativeType(U2U_CONSTRUCTOR_TYPE);
        if (resultEqualsValue) {
          return ctorType.isSubtype(type) ? ctorType : null;
        } else {
          // For typeof != 'function', filter out function subtypes only
          return ctorType.isSubtype(type) ? null : type;
        }
      }
      return matchesExpectation("object") ? type : null;
    }