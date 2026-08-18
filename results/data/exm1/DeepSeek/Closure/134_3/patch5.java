private FunctionType findOverriddenFunction(
        ObjectType ownerType, String propName) {
      // First, check to see if the property is implemented
      // on a superclass.
      JSType propType = ownerType.getPropertyType(propName);
      if (propType instanceof FunctionType) {
        return (FunctionType) propType;
      }
      // If it's not, then check to see if it's implemented
      // on an implemented interface.
      for (ObjectType interfaceType : ownerType.getCtorImplementedInterfaces()) {
        JSType interfacePropType = interfaceType.getPropertyType(propName);
        if (interfacePropType instanceof FunctionType) {
          return (FunctionType) interfacePropType;
        }
      }
      return null;
    }