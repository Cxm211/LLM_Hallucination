private FunctionType findOverriddenFunction(
    ObjectType ownerType, String propName) {
  if (ownerType == null) {
    return null;
  }
  // Check the prototype chain (superclasses).
  ObjectType current = ownerType.getImplicitPrototype();
  while (current != null) {
    JSType propType = current.getPropertyType(propName);
    if (propType instanceof FunctionType) {
      return (FunctionType) propType;
    }
    current = current.getImplicitPrototype();
  }
  // Check implemented interfaces.
  FunctionType ctor = ownerType.getConstructor();
  if (ctor != null) {
    Iterable<ObjectType> interfaces = ctor.getImplementedInterfaces();
    if (interfaces != null) {
      for (ObjectType iface : interfaces) {
        if (iface == null) {
          continue;
        }
        JSType propType = iface.getPropertyType(propName);
        if (propType instanceof FunctionType) {
          return (FunctionType) propType;
        }
      }
    }
  }
  return null;
}