private FunctionType findOverriddenFunction(
        ObjectType ownerType, String propName) {
      // Check superclasses via the implicit prototype chain.
      ObjectType proto = ownerType == null ? null : ownerType.getImplicitPrototype();
      while (proto != null) {
        JSType propType = proto.getPropertyType(propName);
        if (propType instanceof FunctionType) {
          return (FunctionType) propType;
        }
        proto = proto.getImplicitPrototype();
      }
      // If not found on superclasses, check implemented interfaces.
      FunctionType ctor = ownerType == null ? null : ownerType.getConstructor();
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