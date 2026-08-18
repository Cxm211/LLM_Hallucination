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
        ct = (FunctionType) constructorType;
      }
      if (ct != null && ct.isConstructor()) {
        type = ct.getInstanceType();
      }
    }
  }
  n.setJSType(type);
  int i = 0;
  for (Node arg = constructor.getNext(); arg != null; arg = arg.getNext(), i++) {
    if (ct != null) {
      JSType expected = ct.getParameterType(i);
      if (expected != null) {
        arg.setJSType(expected);
      }
    }
    scope = traverse(arg, scope);
  }
  return scope;
}