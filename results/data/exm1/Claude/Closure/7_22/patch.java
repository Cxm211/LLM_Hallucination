public JSType caseObjectType(ObjectType type) {
      if (value.equals("function")) {
        JSType ctorType = getNativeType(U2U_CONSTRUCTOR_TYPE);
        return resultEqualsValue && type.isSubtype(ctorType) ? type : null;
          // Objects are restricted to "Function", subtypes are left
          // Only filter out subtypes of "function"
      }
      return matchesExpectation("object") ? type : null;
    }