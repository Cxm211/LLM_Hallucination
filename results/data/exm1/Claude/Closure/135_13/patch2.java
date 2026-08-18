boolean defineProperty(String name, JSType type,
    boolean inferred, boolean inExterns) {
  if ("prototype".equals(name)) {
    ObjectType objType = type.toObjectType();
    if (objType != null) {
      if (prototypeSlot == null) {
        setPrototype(
            new FunctionPrototypeType(
                registry, this, objType, isNativeObjectType()));
      }
      return true;
    } else {
      return false;
    }
  }
  return super.defineProperty(name, type, inferred, inExterns);
}