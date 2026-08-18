  private void visitNew(NodeTraversal t, Node n) {
    Node constructor = n.getFirstChild();
    JSType type = getJSType(constructor).restrictByNotNullOrUndefined();
    FunctionType fnType = type.toMaybeFunctionType();
    if (fnType != null || type.isEmptyType() || type.isUnknownType() || type.isConstructor()) {
      if (fnType != null) {
        visitParameterList(t, n, fnType);
        JSType instanceType = fnType.getInstanceType();
        if (instanceType != null) {
          ensureTyped(t, n, instanceType);
        } else {
          ensureTyped(t, n);
        }
      } else {
        ensureTyped(t, n);
      }
    } else {
      report(t, n, NOT_A_CONSTRUCTOR);
      ensureTyped(t, n);
    }
  }