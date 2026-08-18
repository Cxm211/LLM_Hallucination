public JSType caseObjectType(ObjectType type) {
      if (value.equals("function")) {
        JSType ctorType = getNativeType(U2U_CONSTRUCTOR_TYPE);
        if (resultEqualsValue) {
          // If the current type is already a subtype of Function, keep it.
          if (type.isSubtype(ctorType)) {
            return type;
          }
          // If Function is a subtype of the current type, narrow to Function.
          if (ctorType.isSubtype(type)) {
            return ctorType;
          }
        }
        return null;
          // Objects are restricted to "Function", subtypes are left
          // Only filter out subtypes of "function"
      }
      return matchesExpectation("object") ? type : null;
    }