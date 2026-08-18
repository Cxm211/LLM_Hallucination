  private FlowScope traverseNew(Node n, FlowScope scope) {

    Node constructor = n.getFirstChild();
    scope = traverse(constructor, scope);
    JSType constructorType = constructor.getJSType();
    JSType type = getNativeType(UNKNOWN_TYPE);
    if (constructorType != null) {
      constructorType = constructorType.restrictByNotNullOrUndefined();
      if (!constructorType.isUnknownType()) {
        FunctionType ct = constructorType.toMaybeFunctionType();
        if (ct == null && constructorType instanceof FunctionType) {
          // If constructorType is a NoObjectType, then toMaybeFunctionType will
          // return null. But NoObjectType implements the FunctionType
          // interface, precisely because it can validly construct objects.
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
