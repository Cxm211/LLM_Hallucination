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
        } else {
          // Not a valid constructor; report an error and mark the type unknown
          // so that type checking can continue gracefully.
          // Avoid reporting if the type is totally unknown.
          report(n, TypeCheck.NOT_A_CONSTRUCTOR);
          type = getNativeType(UNKNOWN_TYPE);
        }
      }
    } else {
      // No type information available; treat as error to match behavior of
      // non-constructors.
      report(n, TypeCheck.NOT_A_CONSTRUCTOR);
      type = getNativeType(UNKNOWN_TYPE);
    }

    n.setJSType(type);
    for (Node arg = constructor.getNext(); arg != null; arg = arg.getNext()) {
      scope = traverse(arg, scope);
    }

    // After traversing arguments, check the call signature if we have one.
    if (ct != null && ct.isConstructor()) {
      checkCall(n, ct);
    }

    return scope;
  }