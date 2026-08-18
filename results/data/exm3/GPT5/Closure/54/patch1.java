public void setPrototypeBasedOn(ObjectType baseType) {
    // This is a bit weird. We need to successfully handle these
    // two cases:
    // Foo.prototype = new Bar();
    // and
    // Foo.prototype = {baz: 3};
    // In the first case, we do not want new properties to get
    // added to Bar. In the second case, we do want new properties
    // to get added to the type of the anonymous object.
    //
    // We handle this by breaking it into two cases:
    //
    // In the first case, we avoid mutating the base type by ensuring
    // the function's prototype object inherits from it, while preserving
    // any existing properties already defined on the function's prototype.
    //
    // In the second case, we just use the anonymous object as the prototype.
    if (baseType.hasReferenceName() ||
        baseType.isUnknownType() ||
        isNativeObjectType() ||
        baseType.isFunctionPrototypeType()) {

      // Preserve existing properties on this.prototype by updating its
      // implicit prototype rather than replacing the whole prototype object.
      PrototypeObjectType currentProto = this.prototype;
      if (currentProto == null) {
        currentProto = new PrototypeObjectType(
            registry, this.getReferenceName() + ".prototype", null);
        setPrototype(currentProto);
      }
      currentProto.setImplicitPrototype(baseType);
      return;
    }

    // For anonymous object literals used as prototypes, use them directly
    // so that their own properties remain own properties of the prototype.
    if (!(baseType instanceof PrototypeObjectType)) {
      baseType = new PrototypeObjectType(
          registry, this.getReferenceName() + ".prototype", baseType);
    }
    setPrototype((PrototypeObjectType) baseType);
  }