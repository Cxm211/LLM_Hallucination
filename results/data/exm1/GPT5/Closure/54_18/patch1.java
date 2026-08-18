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
    // In the first case, we create a new PrototypeObjectType and set
    // its implicit prototype to the type being assigned. This ensures
    // that Bar will not get any properties of Foo.prototype, but properties
    // later assigned to Bar will get inherited properly.
    //
    // In the second case, we just use the anonymous object as the prototype.

    // Special case: if this function already has an anonymous object literal
    // as its prototype, and we're setting its prototype based on another
    // function's prototype (e.g., via __proto__), then just change the
    // implicit prototype of the existing object instead of replacing it,
    // so we don't lose properties already defined on the object literal.
    if (this.prototype instanceof PrototypeObjectType) {
      PrototypeObjectType currentProto = (PrototypeObjectType) this.prototype;
      if (!currentProto.hasReferenceName() &&
          (baseType.hasReferenceName() ||
           baseType.isUnknownType() ||
           isNativeObjectType() ||
           baseType.isFunctionPrototypeType() ||
           !(baseType instanceof PrototypeObjectType))) {
        currentProto.setImplicitPrototype(baseType);
        clearCachedValues();
        return;
      }
    }

    if (baseType.hasReferenceName() ||
        baseType.isUnknownType() ||
        isNativeObjectType() ||
        baseType.isFunctionPrototypeType() ||
        !(baseType instanceof PrototypeObjectType)) {

      baseType = new PrototypeObjectType(
          registry, this.getReferenceName() + ".prototype", baseType);
    }
    setPrototype((PrototypeObjectType) baseType);
  }