public boolean setPrototype(ObjectType prototype) {
    if (prototype == null) {
      return false;
    }
    // If the prototype is not a PrototypeObjectType, wrap it as one.
    PrototypeObjectType proto = null;
    if (prototype instanceof PrototypeObjectType) {
      proto = (PrototypeObjectType) prototype;
    } else {
      proto = new PrototypeObjectType(
          registry, this.getReferenceName() + ".prototype", prototype);
    }
    // getInstanceType fails if the function is not a constructor
    if (isConstructor() && proto == getInstanceType()) {
      return false;
    }

    boolean replacedPrototype = true;

    this.prototype = proto;
    this.prototypeSlot = new SimpleSlot("prototype", proto, true);
    this.prototype.setOwnerFunction(this);

      // Disassociating the old prototype makes this easier to debug--
      // we don't have to worry about two prototypes running around.

    if (isConstructor() || isInterface()) {
      FunctionType superClass = getSuperClassConstructor();
      if (superClass != null) {
        superClass.addSubType(this);
      }

      if (isInterface()) {
        for (ObjectType interfaceType : getExtendedInterfaces()) {
          if (interfaceType.getConstructor() != null) {
            interfaceType.getConstructor().addSubType(this);
          }
        }
      }
    }

    if (replacedPrototype) {
      clearCachedValues();
    }

    return true;
  }