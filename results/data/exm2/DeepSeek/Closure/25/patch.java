  private FlowScope traverseNew(Node n, FlowScope scope) {

    Node constructor = n.getFirstChild();
    scope = traverse(constructor, scope);
    JSType constructorType = constructor.getJSType();
    JSType type = null;
    FunctionType ct = null;
    if (constructorType != null) {
      constructorType = constructorType.restrictByNotNullOrUndefined();
      if (constructorType.isUnknownType()) {
        type = getNativeType(UNKNOWN_TYPE);
      } else {
        ct = constructorType.toMaybeFunctionType();
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
    Node arg = constructor.getNext();
    int i = 0;
    while (arg != null) {
      if (ct != null && ct.isConstructor() && i < ct.getMaxArguments()) {
        JSType paramType = ct.getParameterType(i);
        if (paramType != null) {
          arg.setJSType(paramType);
        }
      }
      scope = traverse(arg, scope);
      arg = arg.getNext();
      i++;
    }
    return scope;
  }