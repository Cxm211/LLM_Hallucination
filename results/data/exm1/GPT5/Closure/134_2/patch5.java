private FunctionType findOverriddenFunction(
        ObjectType ownerType, String propName) {
      // Check the prototype chain (superclasses).
      ObjectType proto = ownerType == null ? null : ownerType.getImplicitPrototype();
      while (proto != null) {
        JSType propType = proto.getPropertyType(propName);
        if (propType instanceof FunctionType) {
          return (FunctionType) propType;
        }
        proto = proto.getImplicitPrototype();
      }
      // Check implemented interfaces, if any.
      if (ownerType != null) {
        for (ObjectType iface : ownerType.getImplementedInterfaces()) {
          JSType propType = iface.getPropertyType(propName);
          if (propType instanceof FunctionType) {
            return (FunctionType) propType;
          }
        }
      }
      return null;
    }