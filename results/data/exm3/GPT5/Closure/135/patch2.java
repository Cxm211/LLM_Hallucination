boolean defineProperty(String name, JSType type,
      boolean inferred, boolean inExterns) {
    if ("prototype".equals(name)) {
      ObjectType objType = type.toObjectType();
      if (objType != null) {
        // Determine the appropriate prototype object to use.
        ObjectType proto = objType instanceof FunctionPrototypeType
            ? objType
            : objType.getImplicitPrototype();
        if (proto == null) {
          return false;
        }
        if (proto instanceof FunctionPrototypeType) {
          return setPrototype((FunctionPrototypeType) proto);
        } else {
          return setPrototype(
              new FunctionPrototypeType(
                  registry, this, proto, isNativeObjectType()));
        }
      } else {
        return false;
      }
    }
    return super.defineProperty(name, type, inferred, inExterns);
  }