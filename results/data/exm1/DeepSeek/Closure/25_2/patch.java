private FlowScope traverseNew(Node n, FlowScope scope) {
    Node constructor = n.getFirstChild();
    scope = traverse(constructor, scope);
    JSType constructorType = constructor.getJSType();
    JSType type = null;
    if (constructorType != null) {
      JSType narrowedType = constructorType.restrictByNotNullOrUndefined();
      if (narrowedType.isUnknownType()) {
        type = getNativeType(UNKNOWN_TYPE);
      } else {
        FunctionType ct = narrowedType.toMaybeFunctionType();
        if (ct == null && constructorType instanceof FunctionType) {
          // Use the original constructorType before restrict because NoObjectType
          // implements FunctionType but toMaybeFunctionType returns null.
          ct = (FunctionType) constructorType;
        }
        if (ct != null && ct.isConstructor()) {
          type = ct.getInstanceType();
        }
      }
    }
    n.setJSType(type);
    for (Node arg = constructor.getNext(); arg != null; arg = arg.getNext()) {
      scope = traverse(arg, scope);
    }
    return scope;
  }