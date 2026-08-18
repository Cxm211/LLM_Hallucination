private FunctionType findOverriddenFunction(
        ObjectType ownerType, String propName) {
      JSType propType = ownerType.getPropertyType(propName);
      if (propType instanceof FunctionType) {
        return (FunctionType) propType;
      }
      return null;
    }