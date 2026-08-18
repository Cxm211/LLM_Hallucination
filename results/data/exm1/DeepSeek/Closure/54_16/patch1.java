public void setPrototypeBasedOn(ObjectType baseType) {
  if (baseType.hasReferenceName() ||
      baseType.isUnknownType() ||
      isNativeObjectType() ||
      !(baseType instanceof PrototypeObjectType)) {
    baseType = new PrototypeObjectType(
        registry, this.getReferenceName() + ".prototype", baseType);
  }
  setPrototype((PrototypeObjectType) baseType);
}