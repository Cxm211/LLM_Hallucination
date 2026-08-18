private FunctionType findOverriddenFunction(
        ObjectType ownerType, String propName) {
      // First, check to see if the property is implemented
      // on a superclass.
      ObjectType iProto = ownerType.getImplicitPrototype();
      while (iProto != null) {
        JSType propType = iProto.getPropertyType(propName);
        if (propType instanceof FunctionType) {
          return (FunctionType) propType;
        }
        iProto = iProto.getImplicitPrototype();
      }
      // If it's not, then check to see if it's implemented
      // on an implemented interface.
      FunctionType ctor = ownerType.getConstructor();
      if (ctor != null) {
        for (ObjectType iface : ctor.getImplementedInterfaces()) {
          JSType propType = iface.getPropertyType(propName);
          if (propType instanceof FunctionType) {
            return (FunctionType) propType;
          }
        }
      }

      return null;
    }