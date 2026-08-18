    private FunctionType findOverriddenFunction(
        ObjectType ownerType, String propName) {
      // First, check to see if the property is implemented
      // on a superclass (walk up the implicit prototype chain).
      ObjectType proto = ownerType == null ? null : ownerType.getImplicitPrototype();
      while (proto != null) {
        JSType propType = proto.getPropertyType(propName);
        if (propType instanceof FunctionType) {
          return (FunctionType) propType;
        }
        proto = proto.getImplicitPrototype();
      }
      // If it's not, then check to see if it's implemented
      // on an implemented interface.
      FunctionType ctor = ownerType == null ? null : ownerType.getConstructor();
      if (ctor != null) {
        for (ObjectType iface : ctor.getImplementedInterfaces()) {
          JSType t = iface.getPropertyType(propName);
          if (t instanceof FunctionType) {
            return (FunctionType) t;
          }
        }
      }
      return null;
    }