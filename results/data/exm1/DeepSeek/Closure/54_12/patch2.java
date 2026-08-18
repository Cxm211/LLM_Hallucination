  public boolean setPrototype(PrototypeObjectType prototype) {
    if (prototype == null) {
      return false;
    }
    
    if (isConstructor() && prototype == getInstanceType()) {
      return false;
    }

    boolean replacedPrototype = prototype != null;

    this.prototype = prototype;
    this.prototypeSlot = new SimpleSlot("prototype", prototype, true);
    this.prototype.setOwnerFunction(this);

      

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