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

    // Backwards-infer argument types from constructor parameter types when possible.
    Node param = null;
    if (ct != null && ct.isConstructor()) {
      Node params = ct.getParametersNode();
      if (params != null) {
        param = params.getFirstChild();
      }
    }

    for (Node arg = constructor.getNext(); arg != null; arg = arg.getNext()) {
      if (param != null) {
        JSType paramType = param.getJSType();
        if (paramType != null) {
          // Provide expected type to the argument before traversal to enable
          // backwards inference.
          arg.setJSType(paramType);
        }
        param = param.getNext();
      }
      scope = traverse(arg, scope);
    }
    return scope;
  }